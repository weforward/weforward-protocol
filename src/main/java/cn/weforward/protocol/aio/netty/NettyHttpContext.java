/**
 * Copyright (c) 2019,2020 honintech
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package cn.weforward.protocol.aio.netty;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.io.OutputStreamStay;
import cn.weforward.common.io.StayException;
import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.protocol.aio.ServerHandler;
import cn.weforward.protocol.aio.http.HttpContext;
import cn.weforward.protocol.aio.http.HttpHeaders;
import cn.weforward.protocol.aio.http.ResponseEndException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * 基于netty的HTTP服务端处理上下文
 * 
 * @author liangyi
 *
 */
public class NettyHttpContext implements HttpContext {
	static final Logger _Logger = LoggerFactory.getLogger(NettyHttpContext.class);

	/** HTTP Handler */
	NettyHttpHandler m_HttpHandler;
	/** 收到的请求 */
	final HttpRequest m_Request;
	/** 不带参数的URI */
	protected String m_Uri;
	/** 在URL中的参数(?xxx=xxx...部分) */
	protected String m_QueryString;
	/** 收到的请求头 */
	NettyHttpHeaders m_RequestHeaders;
	/** 请求内容 */
	ByteBufStream m_RequestBody;
	/** 镜像的请求内容流 */
	ByteBufInput m_MirrorRequestBody;

	/** 服务端业务处理器 */
	ServerHandler m_Handler;
	/** 直接转传请求的内容（收到即转传） */
	NettyOutputStream m_RequestTransferTo;

	/** 响应状态 */
	HttpResponseStatus m_ResponseStatus;
	/** 响应头 */
	io.netty.handler.codec.http.HttpHeaders m_ResponseHeaders;
	/** 响应输出 */
	NettyOutputStream m_ResponseWriter;

	/** 收到的请求或发送的响应体总长度 */
	long m_BodyLength;
	/** 请求/响应传输的时间点（传输完请求头或响应头后） */
	long m_TransferTimepoint;
	/** 估算的传输速率（每秒字节数） */
	int m_Bps;

	/** 响应超时检查任务 */
	ScheduledFuture<?> m_ResponseTimeoutTask;
	/** 响应超时值（毫秒） */
	int m_ResponseTimeout;
	/** 收到HTTP请求的最大字节数 */
	int m_MaxHttpSize;

	public NettyHttpContext(NettyHttpHandler httpHandler, HttpRequest request) {
		m_HttpHandler = httpHandler;
		m_Request = request;
		m_Handler = ServerHandler._init;
	}

	/**
	 * 调用开始
	 * 
	 * @param handler
	 *            业务处理器
	 */
	protected void request(ServerHandler handler) {
		m_Handler = handler;
		m_RequestBody = new CompositeByteBufStream(m_HttpHandler.compositeBuffer());
		handler.requestHeader();
	}

	/**
	 * 收到调用请求的数据体
	 * 
	 * @param data
	 *            请求体数据
	 * @throws IOException
	 */
	protected void readable(ByteBuf data) throws IOException {
		assert (data.readableBytes() > 0);
		m_BodyLength += data.readableBytes();
		calcBsp();
		if (_Logger.isTraceEnabled()) {
			_Logger.trace("{len:" + data.readableBytes() + ",total:" + m_BodyLength + ",bps:"
					+ m_Bps + "}");
		}
		int max = getMaxHttpSize();
		if (max > 0 && m_BodyLength > max) {
			_Logger.warn(formatMessage("请求体太大：" + m_BodyLength + ">" + max));
			// 请求体太大
			responseAndClose(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
			return;
		}

		// 先写到镜像流
		ByteBufInput mirror = m_MirrorRequestBody;
		if (null != mirror) {
			mirror.readable(data.duplicate());
		}

		ByteBufStream body;
		synchronized (this) {
			// 优先转传
			if (forwardRequest(data)) {
				return;
			}
			// 不转传则写入缓冲区
			body = m_RequestBody;
			if (null != body) {
				body.readable(data);
			}
		}

		ServerHandler handler = m_Handler;
		// 通知Handler
		if (null != handler && null != body) {
			handler.prepared(body.available());
		}
	}

	/**
	 * 中止调用
	 */
	private void abortRequest() {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("abortRequest"));
		}
		if (null != m_RequestBody) {
			m_RequestBody.abort();
		}
		m_RequestBody = ByteBufInput._aborted;

		if (null != m_MirrorRequestBody) {
			m_MirrorRequestBody.end();
			m_MirrorRequestBody = null;
		}
		if (null != m_RequestTransferTo) {
			try {
				m_RequestTransferTo.cancel();
				m_RequestTransferTo = null;
			} catch (IOException e) {
				_Logger.warn(String.valueOf(m_RequestTransferTo), e);
			}
		}
	}

	/**
	 * 请求已完整
	 */
	protected void requestCompleted() {
		synchronized (this) {
			if (isRespond()) {
				// 已经输出响应，只能是中止请求
				abortRequest();
				return;
			}

			m_RequestTransferTo = null;
			// 请求内容流标记为完成
			if (null == m_RequestBody) {
				m_RequestBody = ByteBufInput._completed;
			} else {
				m_RequestBody.completed();
			}
			if (null != m_MirrorRequestBody) {
				// 镜像流标记为完成
				m_MirrorRequestBody.completed();
				m_MirrorRequestBody = null;
			}
		}
		m_Handler.requestCompleted();
	}

	/**
	 * 开始响应
	 */
	synchronized protected void responding() throws IOException {
		if (isClosed() || isResponded()) {
			throw new ResponseEndException("已响应/关闭");
		}
		if (null == m_RequestBody || !m_RequestBody.isCompleted()) {
			// 请求内容未接收完整就响应了，只好取消请求流
			abortRequest();
		}

		// 标记为响应中
		m_ResponseWriter = NettyOutputStream._pending;
		m_TransferTimepoint = System.currentTimeMillis();
		m_BodyLength = 0;

		// 关闭超时响应任务
		ScheduledFuture<?> task = m_ResponseTimeoutTask;
		if (null != task) {
			m_ResponseTimeoutTask = null;
			task.cancel(false);
		}
	}

	/**
	 * 已结束响应输出（但可能还在IO中，是否成功的结果未知）
	 */
	synchronized private void respond(LastHttpContent msg) throws IOException {
		ByteBuf content = msg.content();
		try {
			if (isClosed() || isResponded()) {
				throw new IOException("早已结束");
			}
			if (isDebugEnabled()) {
				_Logger.info(formatMessage("respond"));
			}
			m_ResponseWriter = NettyOutputStream._end;
			m_HttpHandler.respond(this);
			ChannelFuture future = m_HttpHandler.writeAndFlush(msg);
			content = null;
			future.addListener(getEndListener());
		} finally {
			if (null != content) {
				content.release();
			}
		}
	}

	protected void responseTimeout() {
		// if (_Logger.isTraceEnabled()) {
		// _Logger.trace(formatMessage("Respond timeout"));
		// }
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("Respond timeout"));
		}
		try {
			m_Handler.responseTimeout();
		} finally {
			if (!isRespond()) {
				// 响应回HTTP 202
				responseAndClose(HttpResponseStatus.ACCEPTED);
			}
		}
	}

	/**
	 * 连接已关闭
	 */
	protected void inactive() {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("inactive"));
		}
		end();
	}

	private boolean isDebugEnabled() {
		NettyHttpHandler hh = m_HttpHandler;
		return (null != hh && hh.isDebugEnabled());
	}

	/**
	 * 响应结束
	 */
	private void end() {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("end"));
		}
		ServerHandler handler;
		synchronized (this) {
			handler = m_Handler;
			if (null == handler) {
				// 一切都结束了
				return;
			}
			m_Handler = null;
		}
		try {
			if (isResponded()) {
				// 已完成响应，回调responseCompleted
				handler.responseCompleted();
			} else {
				// 否则只好requestAbort
				if (isDebugEnabled()) {
					_Logger.info(formatMessage("requestAbort"));
				}
				handler.requestAbort();
			}
		} finally {
			cleanup();
		}
	}

	synchronized private void cleanup() {
		m_Handler = null;
		if (null != m_MirrorRequestBody) {
			m_MirrorRequestBody = null;
		}
		if (null != m_RequestBody) {
			m_RequestBody.abort();
			m_RequestBody = null;
		}

		NettyOutputStream out = m_ResponseWriter;
		if (null != out && NettyOutputStream._end != out) {
			try {
				m_ResponseWriter = null;
				out.cancel();
			} catch (IOException e) {
				_Logger.warn(String.valueOf(out), e);
			}
		}
		out = m_RequestTransferTo;
		if (null != out) {
			try {
				m_RequestTransferTo = null;
				out.cancel();
			} catch (IOException e) {
				_Logger.warn(String.valueOf(out), e);
			}
		}
	}

	/**
	 * 调用是否已结束响应（已把LastHttpContent写入发送队列）
	 */
	private boolean isResponded() {
		return (NettyOutputStream._end == m_ResponseWriter);
	}

	/**
	 * 调用是否已响应或开始响应
	 */
	public boolean isRespond() {
		return (NettyOutputStream._pending == m_ResponseWriter
				|| NettyOutputStream._end == m_ResponseWriter);
	}

	/**
	 * 调用是否已关闭
	 */
	private boolean isClosed() {
		return (null == m_Handler || null == m_HttpHandler);
	}

	/**
	 * 确认可以打开调用请求输入内容流
	 * 
	 * @throws IOException
	 */
	private void ensureRequestStream() throws IOException {
		if (null != m_ResponseWriter) {
			throw new IOException("调用在响应或已结束");
		}
		if (null != m_RequestTransferTo) {
			throw new IOException("已设为转传");
		}
		if (isClosed()) {
			throw new EOFException("调用已关闭");
		}
	}

	private void calcBsp() {
		long ts = System.currentTimeMillis() - m_TransferTimepoint;
		if (ts > 0) {
			m_Bps = (int) ((m_BodyLength * 1000) / ts);
		}
	}

	/**
	 * 转传输入的请求内容
	 * 
	 * @param data
	 *            请求内容数据片段
	 */
	private boolean forwardRequest(ByteBuf data) {
		NettyOutputStream out = m_RequestTransferTo;
		if (null == out) {
			return false;
		}
		ServerHandler handler = m_Handler;
		try {
			out.write(data);
			return true;
		} catch (Exception e) {
			// 转传失败，关闭连接
			m_HttpHandler.close();
			if (null == handler) {
				// 没有handler，只好直接取消输出
				try {
					out.cancel();
				} catch (IOException ee) {
				}
				_Logger.error(out.toString(), e);
			} else {
				if (e instanceof IOException) {
					handler.errorRequestTransferTo((IOException) e, data, out);
				} else {
					handler.errorRequestTransferTo(new IOException(e), data, out);
				}
			}
		}
		return false;
	}

	private io.netty.handler.codec.http.HttpHeaders openResponseHeaders() {
		io.netty.handler.codec.http.HttpHeaders headers = m_ResponseHeaders;
		if (null == headers) {
			headers = new DefaultHttpHeaders();
			m_ResponseHeaders = headers;
		}
		return headers;
	}

	public int bps() {
		return m_Bps;
	}

	@Override
	public String getMethod() {
		if (null == m_Request) {
			return null;
		}
		return m_Request.method().name();
	}

	@Override
	public String getUri() {
		String uri = m_Uri;
		if (null == uri) {
			if (null == m_Request) {
				return null;
			}
			uri = m_Request.uri();
			int idx = uri.indexOf('?');
			if (idx >= 0) {
				m_QueryString = uri.substring(idx + 1);
				uri = uri.substring(0, idx);
			} else {
				m_QueryString = "";
			}
			m_Uri = uri;
		}
		return uri;
	}

	@Override
	public String getQueryString() {
		if ("" == m_QueryString) {
			return m_QueryString;
		}
		getUri();
		return m_QueryString;
	}

	@Override
	public String getRemoteAddr() {
		return (null == m_HttpHandler) ? null : m_HttpHandler.getRemoteAddr();
	}

	@Override
	public HttpHeaders getRequestHeaders() {
		if (null == m_RequestHeaders && null != m_Request) {
			m_RequestHeaders = new NettyHttpHeaders(m_Request.headers());
		}
		return m_RequestHeaders;
	}

	@Override
	synchronized public void requestTransferTo(OutputStream writer, int skipBytes)
			throws IOException {
		ensureRequestStream();
		if (!(m_RequestBody instanceof CompositeByteBufStream)) {
			throw new IOException("只能在getRequestStream前使用");
		}

		CompositeByteBufStream bufStream = (CompositeByteBufStream) m_RequestBody;
		int readableBytes = bufStream.available();
		if (skipBytes > 0) {
			if (skipBytes > readableBytes) {
				throw new IOException("超过范围" + skipBytes + ">" + readableBytes);
			}
			bufStream.skipBytes(skipBytes);
		}
		// 包装转传器
		m_RequestTransferTo = NettyOutputStream.wrap(writer);
		if (bufStream.isCompleted()) {
			m_RequestBody = ByteBufInput._completed;
		}
		// 写入已接收到缓冲区的数据
		ByteBuf buf = bufStream.detach();
		if (null != buf) {
			try {
				if (ByteBufInput._completed != m_RequestBody) {
					m_RequestBody = null;
				}
				forwardRequest(buf);
			} finally {
				buf.release();
			}
		}
	}

	synchronized public InputStream mirrorRequestStream(int skipBytes) throws IOException {
		ensureRequestStream();
		if (!(m_RequestBody instanceof CompositeByteBufStream)) {
			throw new IOException("只能在getRequestStream前调用mirrorRequestStream");
		}
		if (null != m_MirrorRequestBody) {
			if (skipBytes > 0) {
				throw new IOException("不能重复指定skipBytes获取mirrorRequestStream");
			}
			return m_MirrorRequestBody;
		}
		CompositeByteBufStream bufStream = (CompositeByteBufStream) m_RequestBody;
		m_MirrorRequestBody = bufStream.toStream(skipBytes);
		return m_MirrorRequestBody;
	}

	@Override
	synchronized public InputStream getRequestStream() throws IOException {
		ensureRequestStream();
		if (m_RequestBody instanceof CompositeByteBufStream) {
			CompositeByteBufStream bufStream = (CompositeByteBufStream) m_RequestBody;
			ByteBufInput stream = bufStream.detachToStream();
			m_RequestBody = stream;
			return stream;
		}
		return (ByteBufInput) m_RequestBody;
	}

	synchronized public InputStream duplicateRequestStream() throws IOException {
		ensureRequestStream();
		if (!(m_RequestBody instanceof CompositeByteBufStream)) {
			throw new IOException("只能在getRequestStream前使用");
			// return null;
		}
		CompositeByteBufStream bufStream = (CompositeByteBufStream) m_RequestBody;
		return bufStream.snapshot();
	}

	public boolean isRequestCompleted() {
		ByteBufStream body = m_RequestBody;
		return (null != body && body.isCompleted());
	}

	public void setResponseTimeout(int millis) {
		if (millis == m_ResponseTimeout && null != m_ResponseTimeoutTask) {
			return;
		}
		if (null != m_ResponseTimeoutTask) {
			m_ResponseTimeoutTask.cancel(false);
			m_ResponseTimeoutTask = null;
		}
		if (Integer.MAX_VALUE == millis) {
			// 使用空闲超时值
			millis = m_HttpHandler.getIdleMillis();
		}
		m_ResponseTimeout = millis;
		if (null != m_HttpHandler && millis > 0) {
			m_ResponseTimeoutTask = m_HttpHandler.schedule(new ResponseTimeoutChecker(), millis,
					TimeUnit.MILLISECONDS);
		}
	}

	public void setMaxHttpSize(int size) {
		m_MaxHttpSize = size;
	}

	public int getMaxHttpSize() {
		if (m_MaxHttpSize > 0) {
			return m_MaxHttpSize;
		}
		return m_HttpHandler.getMaxHttpSize();
	}

	@Override
	public void setResponseHeader(String name, String value) throws IOException {
		if (null != value) {
			openResponseHeaders().set(name, value);
		} else {
			io.netty.handler.codec.http.HttpHeaders headers = m_ResponseHeaders;
			if (null != headers) {
				headers.remove(name);
			}
		}
	}

	@Override
	synchronized public OutputStream openResponseWriter(int statusCode, String reasonPhrase)
			throws IOException {
		if (isClosed()) {
			throw new ResponseEndException("已关闭");
		}
		if (null != m_ResponseWriter) {
			if (isRespond()) {
				throw new ResponseEndException("已响应");
			}
			throw new IOException("不能重复打开响应输出流：" + m_ResponseWriter);
		}

		if (null != reasonPhrase) {
			m_ResponseStatus = new HttpResponseStatus(statusCode, reasonPhrase);
		} else if (null == m_ResponseStatus || m_ResponseStatus.code() != statusCode) {
			m_ResponseStatus = HttpResponseStatus.valueOf(statusCode);
		}
		m_ResponseWriter = new ResponseOutput();
		return m_ResponseWriter;
	}

	@Override
	synchronized public void response(int statusCode, byte[] content) throws IOException {
		if (isClosed()) {
			throw new ResponseEndException("已关闭");
		}
		if (isRespond()) {
			throw new ResponseEndException("已响应");
		}

		if (null == m_ResponseStatus || statusCode != m_ResponseStatus.code()) {
			m_ResponseStatus = HttpResponseStatus.valueOf(statusCode);
		}
		if (RESPONSE_AND_CLOSE == content) {
			responseAndClose(m_ResponseStatus);
			return;
		}

		if (null == content) {
			response(null);
			return;
		}

		ByteBuf buf = Unpooled.wrappedBuffer(content);
		try {
			response(buf);
		} finally {
			buf.release();
		}
	}

	private void responseAndClose(HttpResponseStatus status) {
		if (!isRespond() && !isClosed()) {
			m_HttpHandler.responseAndClose(status, m_ResponseHeaders);
		}
	}

	/**
	 * 响应简单内容
	 * 
	 * @param content
	 * @throws IOException
	 */
	synchronized private void response(ByteBuf content) throws IOException {
		HttpVersion httpVersion;
		httpVersion = m_Request.protocolVersion();
		FullHttpResponse msg;
		io.netty.handler.codec.http.HttpHeaders headers = openResponseHeaders();
		responding();
		if (null != content) {
			headers.set(HttpHeaderNames.CONTENT_LENGTH.toString(),
					String.valueOf(content.readableBytes()));
			msg = new DefaultFullHttpResponse(httpVersion, m_ResponseStatus, content.retain(),
					headers, EmptyHttpHeaders.INSTANCE);
		} else {
			headers.set(HttpHeaderNames.CONTENT_LENGTH, HttpHeaderValues.ZERO);
			msg = new DefaultFullHttpResponse(httpVersion, m_ResponseStatus, Unpooled.buffer(0),
					headers, EmptyHttpHeaders.INSTANCE);
		}
		respond(msg);
	}

	@Override
	public void disconnect() {
		if (null != m_HttpHandler) {
			m_HttpHandler.close();
			m_HttpHandler = null;
		}
		cleanup();
	}

	private GenericFutureListener<Future<Void>> getEndListener() {
		return new GenericFutureListener<Future<Void>>() {
			@Override
			public void operationComplete(Future<Void> future) throws Exception {
				// if (future.isSuccess()) {
				// // 成功
				// responseCompleted();
				// } else {
				// // 失败？
				// }
				NettyHttpHandler hh = null;
				if (!isRequestCompleted()) {
					// 请求未完成前的响应，需要关闭连接才安全
					hh = m_HttpHandler;
				}
				end();
				if (null != hh) {
					hh.close();
				}
			}
		};
	}

	private String formatMessage(String caption) {
		StringBuilder sb = StringBuilderPool._128.poll();
		try {
			if (null != caption) {
				sb.append(caption);
			}
			toString(sb);
			return sb.toString();
		} finally {
			StringBuilderPool._128.offer(sb);
		}
	}

	public StringBuilder toString(StringBuilder sb) {
		sb.append("{hash:").append(hashCode());
		sb.append(",state:");
		if (isClosed()) {
			sb.append("closed");
		} else if (isResponded()) {
			sb.append("responded");
		} else if (isRespond()) {
			sb.append("respond");
		} else if (isRequestCompleted()) {
			sb.append("requested");
		} else {
			sb.append("head");
		}
		if (m_BodyLength > 0) {
			sb.append(",body-len:").append(m_BodyLength);
		}
		if (m_Bps > 0) {
			sb.append(",bps:").append(m_Bps);
		}
		sb.append("}");
		NettyHttpHandler hh = m_HttpHandler;
		if (null != hh) {
			hh.toString(sb);
		}
		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = StringBuilderPool._128.poll();
		try {
			return toString(sb).toString();
		} finally {
			StringBuilderPool._128.offer(sb);
		}
	}

	/**
	 * 响应输出
	 * 
	 * @author liangyi
	 *
	 */
	class ResponseOutput extends NettyOutputStream implements OutputStreamStay {
		/** 暂留缓冲区 */
		CompositeByteBuf m_StayBuffers;

		/**
		 * 发送headers
		 */
		private void sendHeaders() throws IOException {
			HttpVersion httpVersion;
			httpVersion = m_Request.protocolVersion();
			DefaultHttpResponse msg;
			io.netty.handler.codec.http.HttpHeaders headers = openResponseHeaders();
			if (!headers.contains(HttpHeaderNames.CONTENT_LENGTH)) {
				headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
			}
			msg = new DefaultHttpResponse(httpVersion, m_ResponseStatus, m_ResponseHeaders);
			responding();
			m_HttpHandler.write(msg);
		}

		protected void cleanup() {
			ByteBuf buf;
			buf = m_StayBuffers;
			if (null != buf) {
				m_StayBuffers = null;
				buf.release();
			}
			super.cleanup();
		}

		/**
		 * 是否已发送/输出HTTP headers
		 */
		private boolean isHead() {
			return this != m_ResponseWriter;
		}

		//// OutputStreamStay /////
		@Override
		public void stay() throws StayException {
			if (isRespond() || isClosed()) {
				throw new StayException("已响应/关闭");
			}
			// if (isHead()) {
			// throw new StayException("已输出HTTP头");
			// }
			m_StayBuffers = m_HttpHandler.compositeBuffer();
		}

		//// Channel ////
		@Override
		public boolean isOpen() {
			return (null != m_HttpHandler && (this == m_ResponseWriter
					|| NettyOutputStream._pending == m_ResponseWriter));
		}

		//// NettyOutputStream ////
		protected ByteBuf allocBuffer(int len) {
			return m_HttpHandler.allocBuffer(len);
		}

		synchronized public void write(ByteBuf buf) throws IOException {
			ensureOpen();

			if (null != m_StayBuffers) {
				// 写到暂留缓存
				flushBuffer();
				m_StayBuffers.addComponent(true, buf.retain());
				return;
			}
			if (!isHead()) {
				// 先发送响应头
				sendHeaders();
			}
			flushBuffer();
			if (buf.isReadable()) {
				buf = buf.retain();
				m_BodyLength += buf.readableBytes();
				m_HttpHandler.writeAndFlush(buf);
				calcBsp();
			}
		}

		@Override
		public void flush() throws IOException {
			super.flush();
			ByteBuf buf = m_StayBuffers;
			if (null != buf) {
				// 刷写暂留缓存
				m_StayBuffers = null;
				try {
					if (buf.isReadable()) {
						write(buf);
					}
				} finally {
					buf.release();
				}
			}
		}

		@Override
		protected void ensureOpen() throws IOException {
			if (!isOpen()) {
				throw new IOException("closed");
			}
		}

		@Override
		synchronized public void close() throws IOException {
			if (isHead()) {
				try {
					flush();
					respond(LastHttpContent.EMPTY_LAST_CONTENT);
				} finally {
					cleanup();
				}
				return;
			}

			// 未输出（包含响应头）内容
			ByteBuf content = null;
			try {
				if (null != m_Buffer) {
					// 临时区不为空
					if (null == m_StayBuffers) {
						content = m_Buffer;
						m_Buffer = null;
					} else {
						// 写入暂留区
						m_StayBuffers.addComponent(true, m_Buffer);
						m_Buffer = null;
						// 要输出的内容都在暂留区
						content = m_StayBuffers;
						m_StayBuffers = null;
					}
				} else if (null != m_StayBuffers) {
					// 临时区为空但有暂留区，那么要输出的内容都在暂留区
					content = m_StayBuffers;
					m_StayBuffers = null;
				}
				response(content);
			} finally {
				if (null != content) {
					content.release();
				}
				cleanup();
			}
			return;
		}

		@Override
		synchronized public void cancel() throws IOException {
			cleanup();
			if (NettyOutputStream._pending == m_ResponseWriter || this == m_ResponseWriter) {
				NettyHttpContext.this.disconnect();
			}
		}
	}

	/**
	 * 响应超时检查
	 * 
	 * @author liangyi
	 *
	 */
	class ResponseTimeoutChecker implements Runnable {
		@Override
		public void run() {
			long interval;
			try {
				int timeout = m_ResponseTimeout;
				if (timeout <= 0 || m_TransferTimepoint <= 0) {
					// 超时值为0或传输时间点为0，略过
					return;
				}
				interval = System.currentTimeMillis() - m_TransferTimepoint;
				if (interval >= timeout) {
					// 超时了
					responseTimeout();
					return;
				}
				// 还没到时间，要再等等
				interval = timeout - interval;
			} finally {
				m_ResponseTimeoutTask = null;
			}
			if (_Logger.isTraceEnabled()) {
				_Logger.trace(formatMessage("response-check(" + interval + "ms)"));
			}
			// N毫秒后再检查
			m_ResponseTimeoutTask = m_HttpHandler.schedule(this, interval, TimeUnit.MILLISECONDS);
		}
	}
}
