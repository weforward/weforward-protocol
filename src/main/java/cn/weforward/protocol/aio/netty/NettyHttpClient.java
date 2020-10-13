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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.io.OutputStreamStay;
import cn.weforward.common.io.StayException;
import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.protocol.aio.ClientHandler;
import cn.weforward.protocol.aio.http.HttpClient;
import cn.weforward.protocol.aio.http.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * 基于netty的HTTP client
 * 
 * @author liangyi
 *
 */
public class NettyHttpClient extends ChannelInboundHandlerAdapter implements HttpClient {
	static final Logger _Logger = LoggerFactory.getLogger(NettyHttpClient.class);

	final NettyHttpClientFactory m_Factory;
	ClientHandler m_Handler;
	ChannelHandlerContext m_Ctx;

	/** 请求头 */
	io.netty.handler.codec.http.HttpHeaders m_RequestHeaders;
	/** 请求消息 */
	HttpRequest m_Request;
	/** 输出请求体字节流 */
	NettyOutputStream m_RequestWriter;
	/** 发起请求的时间点 */
	long m_RequestTimepoint;
	/** 从发起连接到收到完整响应的超时值（毫秒） */
	int m_Timeout;
	/** 超时检查任务 */
	ScheduledFuture<?> m_TimeoutTask;

	/** 收到的请求或发送的响应体总长度 */
	long m_BodyLength;
	/** 请求/响应传输的时间点（传输完请求头或响应头后） */
	long m_TransferTimepoint;
	/** 估算的传输速率 */
	int m_Bps;

	/** 响应消息 */
	HttpResponse m_Response;
	/** 响应内容 */
	ByteBufStream m_ResponseBody;
	/** 直接转传响应的内容（收到即转传） */
	NettyOutputStream m_ResponseTransferTo;
	/** 阻塞读超时 */
	int m_ReadTimeout;

	protected NettyHttpClient(NettyHttpClientFactory factory, ClientHandler handler) {
		m_Factory = factory;
		m_Handler = handler;
		if (ClientHandler.SYNC == handler) {
			// 同步模式下默认超时值为60秒
			setReadTimeout(60 * 1000);
		}
	}

	public void connectFail(Throwable cause) {
		if (isDebugEnabled()) {
			_Logger.warn("连接失败", cause);
		}
		ClientHandler handler = m_Handler;
		if (null != handler) {
			m_Handler = null;
			handler.connectFail();
		}
		cleanup();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("channelInactive"));
		}
		super.channelInactive(ctx);
		m_Ctx = null;
		close();
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		m_Ctx = ctx;
		super.handlerAdded(ctx);
		beginRequest();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (msg instanceof HttpResponse) {
				// 响应头完整
				responseHeader((HttpResponse) msg);
			}
			if (msg instanceof HttpContent) {
				// 响应体数据片段
				readable(((HttpContent) msg).content());
			}
			if (msg instanceof LastHttpContent) {
				// 请求完整
				responseCompleted();
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	private io.netty.handler.codec.http.HttpHeaders openRequestHeaders() {
		io.netty.handler.codec.http.HttpHeaders headers = m_RequestHeaders;
		if (null == headers) {
			headers = new DefaultHttpHeaders();
			m_RequestHeaders = headers;
		}
		return headers;
	}

	/**
	 * 已就绪可开始提交请求消息
	 */
	private void beginRequest() {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("requesting"));
		}
		synchronized (this) {
			if (null == m_Request) {
				// 关掉了？
				_Logger.error(formatMessage("请求未开始就取消了？"));
				return;
			}
			m_TransferTimepoint = System.currentTimeMillis();
			if (m_Request instanceof FullHttpRequest) {
				// 若有完整的请求，直接提交
				m_Ctx.writeAndFlush(m_Request).addListener(getSubmitListener());
			}

			if (m_Timeout > 0) {
				long remaind = m_Timeout - (System.currentTimeMillis() - m_RequestTimepoint);
				if (remaind <= 0) {
					// 已经超时了
					responseTimeout();
				} else {
					setTimeoutTask(m_Ctx, remaind);
				}
			}
			this.notifyAll();
		}
		m_Handler.established();
	}

	private void setTimeoutTask(ChannelHandlerContext ctx, long millis) {
		ScheduledFuture<?> task = m_TimeoutTask;
		if (null != task) {
			// 先取消上个任务
			m_TimeoutTask = null;
			task.cancel(false);
		}
		if (null == ctx || millis < 1) {
			return;
		}
		m_TimeoutTask = ctx.executor().schedule(new TimeoutChecker(), millis,
				TimeUnit.MILLISECONDS);
	}

	public int getReadTimeout() {
		return m_ReadTimeout;
	}

	/**
	 * 设置阻塞读超时
	 * <p>
	 * 仅同步模式有效
	 * 
	 * @param mills
	 */
	public void setReadTimeout(int mills) {
		m_ReadTimeout = mills;
	}

	synchronized private void waitConnect(int timeout) throws IOException {
		long ts = System.currentTimeMillis();
		int mills;
		while (null == m_Ctx) {
			mills = timeout - (int) (System.currentTimeMillis() - ts);
			if (mills <= 0) {
				throw new ConnectException("超时");
			}
			try {
				this.wait(mills);
			} catch (InterruptedException e) {
				throw new InterruptedIOException(e.getMessage());
			}
		}
	}

	synchronized private void waitResponse(int timeout) throws IOException {
		if (isClosed()) {
			throw new EOFException("closed");
		}
		long ts = System.currentTimeMillis();
		int mills;
		// while (null == m_Response) {
		while (!isResponseCompleted()) {
			mills = timeout - (int) (System.currentTimeMillis() - ts);
			if (mills <= 0) {
				throw new IOException("超时");
			}
			try {
				this.wait(mills);
			} catch (InterruptedException e) {
				throw new InterruptedIOException(e.getMessage());
			}
		}
	}

	/**
	 * 请求（发送）完成
	 */
	private void requestCompleted() {
		if (isDebugEnabled() && NettyOutputStream._end != m_RequestWriter) {
			_Logger.info(formatMessage("requestCompleted"));
		}
		ClientHandler handler = m_Handler;
		synchronized (this) {
			if (NettyOutputStream._end == m_RequestWriter) {
				// 已经是end状态，也就是说已通知过handler
				handler = null;
			}
			m_RequestWriter = NettyOutputStream._end;
			m_Request = null;
			m_RequestHeaders = null;
		}
		if (null != handler) {
			handler.requestCompleted();
		}
	}

	/**
	 * 请求（失败）中止
	 */
	private void requestAbort() {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("requestAbort"));
		}
		ClientHandler handler = m_Handler;
		if (null != handler) {
			m_Handler = null; // requestAbort结束掉handler
			disconnect();
			handler.requestAbort();
		}
	}

	private void calcBsp() {
		long ts = System.currentTimeMillis() - m_TransferTimepoint;
		if (ts > 0) {
			m_Bps = (int) ((m_BodyLength * 1000) / ts);
		}
	}

	/**
	 * 响应数据已收完
	 */
	private void responseCompleted() {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("responseCompleted"));
		}
		ClientHandler handler;
		synchronized (this) {
			m_ResponseTransferTo = null;
			handler = m_Handler;
			if (null == handler) {
				_Logger.warn(formatMessage("收到响应前已关闭！"));
				cleanup();
				return;
			}
			if (null == m_Response) {
				_Logger.error(formatMessage("无响应头结束？"));
				disconnect();
				return;
			}
			if (null != m_ResponseBody) {
				m_ResponseBody.completed();
			}
			m_Handler = null;
			free();
		}
		handler.responseCompleted();
	}

	private void responseTimeout() {
		// 仅通知handler，交由其确定是否中止调用
		m_Handler.responseTimeout();
	}

	private void readable(ByteBuf data) throws IOException {
		assert (data.readableBytes() > 0);
		m_BodyLength += data.readableBytes();
		calcBsp();
		if (_Logger.isTraceEnabled()) {
			_Logger.trace("{收到:" + data.readableBytes() + ",total:" + m_BodyLength + ",bps:" + m_Bps
					+ "}");
		}
		// XXX 需要控制响应内容的大小吗？

		ByteBufStream body;
		synchronized (this) {
			// 优先转传
			if (forwardResponse(data)) {
				return;
			}
			// 不转传则写入缓冲区
			body = m_ResponseBody;
			if (null != body) {
				body.readable(data);
			}
		}

		// 通知Handler
		ClientHandler handler = m_Handler;
		if (null != handler && null != body) {
			handler.prepared(body.available());
		}
	}

	private void responseHeader(HttpResponse rsp) {
		m_Response = rsp;
		m_BodyLength = 0;
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("responseHeader"));
		}
		ByteBufStream body = m_ResponseBody;
		if (null != body) {
			// 这是什么情况？
			_Logger.error(formatMessage("responseBody!=null？" + body));
			body.abort();
		}
		// m_ResponseBody = new
		// CompositeByteBufStream(Unpooled.compositeBuffer());
		m_ResponseBody = new CompositeByteBufStream(m_Ctx.alloc().compositeBuffer());
		m_TransferTimepoint = System.currentTimeMillis();
		// 可能收到响应头的时间比writeAndFlush的回调事件还要快，所以要调用下requestCompleted
		requestCompleted();
		if (ClientHandler.SYNC == m_Handler) {
			synchronized (this) {
				this.notifyAll();
			}
		} else {
			m_Handler.responseHeader();
		}
	}

	private ByteBuf allocBuffer(int len) {
		ChannelHandlerContext ctx = m_Ctx;
		if (null != ctx) {
			return ctx.alloc().buffer(len);
		}
		return Unpooled.buffer(len);
	}

	/**
	 * 转发请求体
	 * 
	 * @param data
	 *            请求体数据片段
	 */
	boolean forwardResponse(ByteBuf data) {
		NettyOutputStream out = m_ResponseTransferTo;
		if (null == out) {
			return false;
		}
		ClientHandler handler = m_Handler;
		try {
			out.write(data);
			return true;
		} catch (Exception e) {
			// 转传失败，关闭连接
			disconnect();
			if (null == handler) {
				// 没有handler，只好直接取消输出
				try {
					out.cancel();
				} catch (IOException ee) {
				}
				_Logger.error(out.toString(), e);
			} else {
				// 通知handler
				if (e instanceof IOException) {
					handler.errorResponseTransferTo((IOException) e, data, out);
				} else {
					handler.errorResponseTransferTo(new IOException(e), data, out);
				}
			}
		}
		return false;
	}

	/**
	 * 确认已有响应
	 */
	private void ensureResponse() throws IOException {
		if (null == m_Response || null == m_ResponseBody) {
			throw new IOException("未有响应");
		}
	}

	/**
	 * 确认可以打开响应流/缓冲区
	 *
	 * @throws IOException
	 */
	private void ensureResponseStream() throws IOException {
		ensureResponse();
		if (null != m_ResponseTransferTo) {
			throw new IOException("已设为转传");
		}
	}

	private GenericFutureListener<Future<Void>> getSubmitListener() {
		return new GenericFutureListener<Future<Void>>() {
			@Override
			public void operationComplete(Future<Void> future) throws Exception {
				if (future.isSuccess()) {
					// 已经提交完请求
					requestCompleted();
				} else {
					// 失败了:(
					requestAbort();
				}
			}
		};
	}

	public boolean isDebugEnabled() {
		return m_Factory.isDebugEnabled();
	}

	private boolean isClosed() {
		return null == m_Handler;
	}

	private boolean isRequestCompleted() {
		return NettyOutputStream._end == m_RequestWriter;
	}

	@Override
	public void request(String url, String method) throws IOException {
		request(url, method, 0);
	}

	@Override
	public void request(String url, String method, int timeout) throws IOException {
		URL uri = new URL(url);
		int port = uri.getPort();
		String protocol = uri.getProtocol().toLowerCase();
		boolean ssl = false;
		if ("http".equals(protocol)) {
			if (port < 1) {
				port = 80;
			}
		} else if ("https".equals(protocol)) {
			if (port < 1) {
				port = 443;
				ssl = true;
			}
		} else {
			throw new MalformedURLException("不支持的协议" + uri.getProtocol());
		}

		synchronized (this) {
			if (null != m_Request) {
				throw new IOException("请求在处理中");
			}
			m_RequestTimepoint = System.currentTimeMillis();
			m_Timeout = timeout;

			// 置入HTTP请求头
			io.netty.handler.codec.http.HttpHeaders headers = openRequestHeaders();
			headers.set(HttpHeaderNames.HOST, uri.getHost());
			headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			headers.set(HttpHeaderNames.USER_AGENT, "netty/4.1");
			headers.set(HttpHeaderNames.ACCEPT, "*/*");
			headers.set(HttpHeaderNames.ACCEPT_CHARSET, "UTF-8");

			method = method.toUpperCase();
			HttpMethod httpMethod = HttpMethod.valueOf(method);
			if (HttpMethod.GET.equals(httpMethod) || HttpMethod.HEAD.equals(httpMethod)) {
				// GET请求，直接构造完整的无内容请求
				headers.set(HttpHeaderNames.CONTENT_LENGTH, HttpHeaderValues.ZERO);
				m_Request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod,
						uri.getFile(), Unpooled.buffer(0), m_RequestHeaders,
						EmptyHttpHeaders.INSTANCE);
			} else {
				// if (!headers.contains(HttpHeaderNames.CONTENT_LENGTH)) {
				// headers.set(HttpHeaderNames.TRANSFER_ENCODING,
				// HttpHeaderValues.CHUNKED);
				// }
				m_Request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, uri.getFile(),
						m_RequestHeaders);
			}
		}
		m_Factory.connect(this, uri.getHost(), port, ssl);
	}

	@Override
	public void setRequestHeader(String name, String value) throws IOException {
		if (null != value) {
			openRequestHeaders().set(name, value);
		} else {
			io.netty.handler.codec.http.HttpHeaders headers = m_RequestHeaders;
			if (null != headers) {
				headers.remove(name);
			}
		}
	}

	@Override
	synchronized public NettyOutputStream openRequestWriter() throws IOException {
		if (null == m_Request) {
			throw new IOException("未请求或已中断");
		}
		if (ClientHandler.SYNC == m_Handler) {
			// 同步模式下，要等待连接就绪
			waitConnect(m_Factory.getConnectTimeout());
		}
		if (null == m_Ctx) {
			throw new IOException("连接未就绪或已关闭");
		}

		// if (null != m_RequestWriter) {
		// if (NettyOutputStream._end == m_RequestWriter) {
		// throw new IOException("请求已提交");
		// }
		// // throw new IOException("opened");
		// // throw new IOException("状态异常");
		// return m_RequestWriter;
		// }
		if (null != m_RequestWriter) {
			throw new IOException("请求流已打开");
		}
		m_RequestWriter = new RequestOutput();
		return m_RequestWriter;
	}

	public int bps() {
		return m_Bps;
	}

	public int getResponseCode() throws IOException {
		if (ClientHandler.SYNC == m_Handler) {
			// 同步模式下，要等待响应
			waitResponse(getReadTimeout());
		}
		ensureResponse();
		return m_Response.status().code();
	}

	@Override
	public HttpHeaders getResponseHeaders() throws IOException {
		if (ClientHandler.SYNC == m_Handler) {
			// 同步模式下，要等待响应
			waitResponse(getReadTimeout());
		}
		ensureResponse();
		return new NettyHttpHeaders(m_Response.headers());
	}

	@Override
	synchronized public void responseTransferTo(OutputStream writer, int skipBytes)
			throws IOException {
		ensureResponseStream();
		// if (null != m_ResponseInput && ByteBufInput._completed !=
		// m_ResponseInput) {
		if (!(m_ResponseBody instanceof CompositeByteBufStream)) {
			throw new IOException("只能在getResponseStream前调用");
		}
		CompositeByteBufStream bufStream = (CompositeByteBufStream) m_ResponseBody;
		int readableBytes = bufStream.available();
		if (skipBytes > 0) {
			if (skipBytes > readableBytes) {
				throw new IOException("超过范围" + skipBytes + ">" + readableBytes);
			}
			bufStream.skipBytes(skipBytes);
		}
		// 包装转传器
		m_ResponseTransferTo = NettyOutputStream.wrap(writer);
		// 写入已接收到缓冲区的数据
		ByteBuf buf = bufStream.detach();
		if (null != buf) {
			try {
				m_ResponseBody = null;
				forwardResponse(buf);
			} finally {
				buf.release();
			}
		}
	}

	@Override
	synchronized public InputStream getResponseStream() throws IOException {
		if (ClientHandler.SYNC == m_Handler) {
			// 同步模式下，要等待响应
			waitResponse(getReadTimeout());
		}
		ensureResponse();
		if (m_ResponseBody instanceof CompositeByteBufStream) {
			CompositeByteBufStream bufStream = (CompositeByteBufStream) m_ResponseBody;
			ByteBufInput stream = bufStream.detachToStream();
			m_ResponseBody = stream;
			return stream;
		}
		return (ByteBufInput) m_ResponseBody;
	}

	@Override
	synchronized public InputStream duplicateResponseStream() throws IOException {
		ensureResponseStream();
		if (!(m_ResponseBody instanceof CompositeByteBufStream)) {
			throw new IOException("只能在getResponseStream前使用");
		}
		CompositeByteBufStream bufStream = (CompositeByteBufStream) m_ResponseBody;
		return bufStream.snapshot();
	}

	@Override
	public boolean isResponseCompleted() {
		ByteBufStream input = m_ResponseBody;
		return (null != m_Response && null != input && input.isCompleted());
	}

	/**
	 * 由channel流水线移除client，释放回池
	 */
	synchronized private void free() {
		if (null != m_Ctx) {
			setTimeoutTask(null, 0);
			m_Ctx.pipeline().remove(this);
			m_Factory.free(m_Ctx.channel());
			m_Ctx = null;
		}
		// if (ClientHandler.SYNC == m_Handler) {
		this.notifyAll();
		// }
	}

	synchronized private void cleanup() {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("cleanup"));
		}
		setTimeoutTask(null, 0);
		// try {
		NettyOutputStream requestWriter = m_RequestWriter;
		if (null != requestWriter) {
			m_RequestWriter = null;
			try {
				requestWriter.cancel();
			} catch (IOException e) {
				// _Logger.warn(e.toString(), e);
			}
		}
		if (null != m_ResponseBody) {
			m_ResponseBody.abort();
			m_ResponseBody = null;
		}
		NettyOutputStream responseTransferTo = m_ResponseTransferTo;
		if (null != responseTransferTo) {
			m_ResponseTransferTo = null;
			try {
				responseTransferTo.cancel();
			} catch (IOException e) {
				// _Logger.warn(e.toString(), e);
			}
		}
		m_Request = null;
		m_RequestHeaders = null;
		m_Response = null;
		m_Handler = null;
		m_Ctx = null;
	}

	@Override
	public void close() {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("close"));
		}
		ClientHandler handler = null;
		synchronized (this) {
			if (!isResponseCompleted()) {
				// 未响应完的，断开连接比较安全
				handler = m_Handler;
				disconnect();
			}
		}
		cleanup();
		if (null != handler) {
			handler.requestAbort();
		}
	}

	@Override
	public void disconnect() {
		ChannelHandlerContext ctx = m_Ctx;
		if (null != ctx) {
			m_Ctx = null;
			if (isDebugEnabled()) {
				_Logger.info(formatMessage("disconnect"));
			}
			ctx.close();
		}
		cleanup();
	}

	private String formatMessage(String caption) {
		StringBuilder builder = StringBuilderPool._128.poll();
		try {
			if (null != caption) {
				builder.append(caption);
			}
			toString(builder);
			return builder.toString();
		} finally {
			StringBuilderPool._128.offer(builder);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = StringBuilderPool._128.poll();
		try {
			return toString(builder).toString();
		} finally {
			StringBuilderPool._128.offer(builder);
		}
	}

	public StringBuilder toString(StringBuilder builder) {
		Channel channel = (null == m_Ctx) ? null : m_Ctx.channel();
		builder.append("{hash:").append(hashCode());
		if (null != channel) {
			builder.append(",remote:").append(channel.remoteAddress());
			builder.append(",local:").append(channel.localAddress());
		}
		builder.append(",state:");
		if (isResponseCompleted()) {
			builder.append("completed");
		} else if (null == m_Ctx) {
			builder.append("init");
		} else if (isRequestCompleted()) {
			builder.append("requested");
		} else if (null != m_Request) {
			builder.append("request");
		} else {
			builder.append("connect");
		}
		builder.append("}");
		return builder;
	}

	/**
	 * 输出请求
	 * 
	 * @author liangyi
	 *
	 */
	class RequestOutput extends NettyOutputStream implements OutputStreamStay {
		/** 暂留缓冲区 */
		CompositeByteBuf m_StayBuffers;
		/** 最后的数据块 */
		ByteBuf m_Last;
		/** 请求头是否已发送 */
		boolean m_RequestHeader = false;

		private void sendHeaders() {
			io.netty.handler.codec.http.HttpHeaders headers = m_Request.headers();
			if (!headers.contains(HttpHeaderNames.CONTENT_LENGTH)) {
				headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
			}
			m_Ctx.write(m_Request);
			m_RequestHeader = true;
		}

		synchronized protected void cleanup() {
			ByteBuf buf;
			buf = m_Last;
			if (null != buf) {
				m_Last = null;
				buf.release();
			}
			buf = m_StayBuffers;
			if (null != buf) {
				m_StayBuffers = null;
				buf.release();
			}
			super.cleanup();
		}

		//// OutputStreamStay /////
		@Override
		synchronized public void stay() throws StayException {
			if (m_RequestHeader) {
				throw new StayException("已输出HTTP头");
			}
			if (null != m_Ctx) {
				m_StayBuffers = m_Ctx.alloc().compositeBuffer();
			} else {
				m_StayBuffers = PooledByteBufAllocator.DEFAULT.compositeBuffer();
			}
		}

		//// Channel ////
		@Override
		public boolean isOpen() {
			return (this == m_RequestWriter);
		}

		//// NettyOutputStream ////
		protected ByteBuf allocBuffer(int len) {
			return NettyHttpClient.this.allocBuffer(len);
		}

		protected void ensureOpen() throws IOException {
			if (!isOpen()) {
				throw new IOException("closed");
			}
		}

		synchronized public void write(ByteBuf buf) throws IOException {
			ensureOpen();
			if (null != m_StayBuffers) {
				// 写到暂留缓存
				flushBuffer();
				m_StayBuffers.addComponent(true, buf.retain());
				return;
			}
			// 总保留最后一个数据块先不发送
			ByteBuf last = m_Last;
			m_Last = buf.retain();
			try {
				if (null != last) {
					m_BodyLength += last.readableBytes();
					if (!m_RequestHeader) {
						// 先发送请求头
						sendHeaders();
					}
					flushBuffer();
					// m_Ctx.write(last);
					m_Ctx.writeAndFlush(last);
					last = null;
				}
			} finally {
				if (null != last) {
					last.release();
				}
			}
		}

		@Override
		synchronized public void flush() throws IOException {
			super.flush();
			ByteBuf buf = m_StayBuffers;
			if (null != buf) {
				// 刷写暂留缓存
				m_StayBuffers = null;
				if (buf.isReadable()) {
					write(buf);
				}
				buf.release();
			}
		}

		@Override
		synchronized public void close() throws IOException {
			try {
				if (this != m_RequestWriter) {
					throw new EOFException();
				}
				flush();
				// ByteBuf last = m_Last;
				LastHttpContent content;
				if (!m_RequestHeader) {
					// 请求头未发送的，构造完整请求
					io.netty.handler.codec.http.HttpHeaders headers = openRequestHeaders();
					if (null == m_Last) {
						// 无内容
						headers.set(HttpHeaderNames.CONTENT_LENGTH, HttpHeaderValues.ZERO);
						content = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
								m_Request.method(), m_Request.uri(), Unpooled.buffer(0), headers,
								EmptyHttpHeaders.INSTANCE);
					} else {
						headers.set(HttpHeaderNames.CONTENT_LENGTH,
								String.valueOf(m_Last.readableBytes()));
						content = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
								m_Request.method(), m_Request.uri(), m_Last, headers,
								EmptyHttpHeaders.INSTANCE);
					}
				} else {
					if (null != m_Last) {
						content = new DefaultLastHttpContent(m_Last);
					} else {
						content = LastHttpContent.EMPTY_LAST_CONTENT;
					}
				}
				m_RequestWriter = NettyOutputStream._pending;
				ChannelFuture future = m_Ctx.writeAndFlush(content);
				m_Last = null;
				future.addListener(getSubmitListener());
			} finally {
				cleanup();
			}
		}

		@Override
		synchronized public void cancel() throws IOException {
			super.cancel();
			if (this == m_RequestWriter) {
				disconnect();
			}
		}
	}

	/**
	 * 超时检查
	 * 
	 * @author liangyi
	 *
	 */
	class TimeoutChecker implements Runnable {
		@Override
		public void run() {
			long remaind;
			try {
				int timeout = m_Timeout;
				if (timeout <= 0) {
					// 超时值为，略过
					return;
				}
				remaind = timeout - System.currentTimeMillis() - m_RequestTimepoint;
				if (remaind <= 0) {
					// 超时了
					responseTimeout();
					return;
				}
				// 还没到时间，要再等等
			} finally {
				m_TimeoutTask = null;
			}
			if (_Logger.isTraceEnabled()) {
				_Logger.trace("timeout-check(" + remaind + "ms)");
			}
			// N毫秒后再检查
			setTimeoutTask(m_Ctx, remaind);
		}
	}
}
