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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.util.NumberUtil;
import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.protocol.aio.ServerHandler;
import cn.weforward.protocol.aio.http.HttpConstants;
import cn.weforward.protocol.aio.netty.websocket.WebSocketContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.OutOfDirectMemoryError;

/**
 * 基于netty的HTTP服务端处理上下文
 * 
 * @author liangyi
 *
 */
public class NettyHttpHandler extends ChannelInboundHandlerAdapter {
	static final Logger _Logger = LoggerFactory.getLogger(NettyHttpHandler.class);

	/** HTTP server */
	protected final NettyHttpServer m_Server;
	/** 连接复用数 */
	protected volatile int m_Reuse;
	/** netty连接的上下文 */
	protected ChannelHandlerContext m_Ctx;

	/** 当次HTTP调用的上下文 */
	protected NettyHttpContext m_HttpContext;

	/** 客户端地址 */
	protected String m_RemoteAddr;
	/** 估算的传输速率累计（每秒字节数） */
	protected long m_BpsTotal;
	/** 传输速率累计数 */
	protected int m_BpsTimes;
	/** 连接空闲时间检查任务 */
	protected ScheduledFuture<?> m_IdleTask;

	public NettyHttpHandler(NettyHttpServer server) {
		m_Server = server;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (null != m_Ctx && ctx != m_Ctx) {
				_Logger.error("不一样的Context？" + m_Ctx + "!=" + ctx);
				// super.channelRead(ctx, msg);
				ctx.close();
				return;
			}
			if (msg instanceof HttpRequest) {
				if (null == m_HttpContext) {
					// 收到完整的请求头
					requestHeader((HttpRequest) msg);
				} else if (msg != m_HttpContext.m_Request && _Logger.isInfoEnabled()) {
					_Logger.warn(formatMessage("发生什么情况！上个调用未完成？ " + msg));
					// 关闭连接比较安全
					// super.channelRead(ctx, msg);
					ctx.close();
					return;
				}
			}

			NettyHttpContext hc = m_HttpContext;
			if (null != m_HttpContext && msg instanceof HttpContent) {
				// 请求体数据片段
				m_HttpContext.readable(((HttpContent) msg).content());
			}
			if (null != m_HttpContext && msg instanceof LastHttpContent) {
				// 请求已完整
				m_HttpContext.requestCompleted();
			}
			if (isDebugEnabled() && null == hc) {
				_Logger.warn(formatMessage("调用提前结束/响应？" + msg));
			}
		} finally {
			super.channelRead(ctx, msg);
		}
	}

	// @Override
	// public void channelReadComplete(ChannelHandlerContext ctx) throws
	// Exception {
	// super.channelReadComplete(ctx);
	// }

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("channelActive"));
		}
		m_Reuse = 0;
		m_Ctx = ctx;
		startIdleTask(ctx);
		// 获取调用端信息（IP+端口）
		InetSocketAddress ip = (InetSocketAddress) ctx.channel().remoteAddress();
		// m_RemoteAddr = ip.getAddress().getHostAddress();
		m_RemoteAddr = ip.getAddress().getHostAddress() + ':' + ip.getPort();
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("channelInactive"));
		}
		if (null != m_IdleTask) {
			m_IdleTask.cancel(false);
			m_IdleTask = null;
		}
		NettyHttpContext hc = m_HttpContext;
		if (null != hc) {
			m_HttpContext = null;
			hc.inactive();
		}
		m_Ctx = null;
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof OutOfDirectMemoryError || cause instanceof OutOfMemoryError) {
			ctx.close();
		}
		// if (_Logger.isTraceEnabled()) {
		// _Logger.trace(toString(), cause);
		// }
		// return;
		super.exceptionCaught(ctx, cause);
	}

	/**
	 * 已收到完整请求头
	 * 
	 * @param request
	 *            调用请求
	 * @throws IOException
	 */
	private void requestHeader(HttpRequest request) throws IOException {
		++m_Reuse;
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("requestHeader"));
		}
		HttpHeaders headers = request.headers();
		int max = getMaxHttpSize();// m_Server.getMaxHttpSize();
		if (max > 0) {
			if (null != headers) {
				int length = NumberUtil.toInt(headers.get(HttpConstants.CONTENT_LENGTH), 0);
				if (length > max) {
					_Logger.warn(formatMessage("请求体太大：" + length + ">" + max));
					// 请求体太大
					responseAndClose(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, null);
					return;
				}
			}
		}

		// 处理websocket升级请求头
		WebSocketServerHandshakerFactory wsFactory = m_Server.getWebSocketFactory();
		if (null != wsFactory) {
			String upgrade = headers.get("Upgrade");
			if ("websocket".equals(upgrade)) {
				WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);// 创建一个
				if (handshaker == null) {
					// 不支持
					// WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(m_Ctx.channel());
					// close();
					responseAndClose(HttpResponseStatus.UPGRADE_REQUIRED, null);
					return;
				}
				// 握手
				ChannelFuture future = handshaker.handshake(m_Ctx.channel(), request);
				// // 在pipe移除当前的NettyHttpContext
				// m_Ctx.channel().pipeline().remove(NettyHttpContext.this);
				future.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (!future.isSuccess()) {
							m_Ctx.fireExceptionCaught(future.cause());
							return;
						}
						ChannelPipeline pl = future.channel().pipeline();
						// 切换处理的handler
						WebSocketContext handler = new WebSocketContext(m_Server);
						pl.addLast("ws-ctx", handler);
						// 在pipe移除当前的NettyHttpContext
						pl.remove(NettyHttpHandler.this);
						// XXX 测试
						for (java.util.Map.Entry<String, ChannelHandler> e : pl) {
							_Logger.trace(String.valueOf(e));
						}
					}
				});
				return;
			}
		}
		// m_TransferTimepoint = System.currentTimeMillis();
		NettyHttpContext hc = new NettyHttpContext(this, request);
		m_HttpContext = hc;
		ServerHandler handler = null;
		try {
			handler = m_Server.handle(hc);
		} finally {
			if (null == handler) {
				if (isRespond()) {
					// 已经响应
					return;
				}
				// 没有业务处理，直接返回501，且主动关闭
				responseAndClose(HttpResponseStatus.NOT_IMPLEMENTED, null);
				return;
			}
		}
		hc.request(handler);
		// m_Handler.requestHeader();
	}

	/**
	 * 已经响应或响应中
	 */
	private boolean isRespond() {
		return (null == m_HttpContext);
	}

	/**
	 * 已响应
	 * 
	 * @param hc
	 *            HTTP服务端处理上下文
	 */
	public void respond(NettyHttpContext hc) {
		if (hc == m_HttpContext) {
			m_HttpContext = null;
			startIdleTask(m_Ctx);
			// 累计bps，用于估算平均值
			int bps = hc.bps();
			if (bps > 0) {
				m_BpsTotal += bps;
				++m_BpsTimes;
			}
		}
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("respond"));
		}
	}

	// private void init() {
	// NettyHttpContext hc = m_HttpContext;
	// if (null != hc) {
	// m_HttpContext = null;
	// // TODO
	// }
	// // m_BodyLength = 0;
	// m_RemoteAddr = null;
	// // m_TransferTimepoint = 0;
	// m_Bps = 0;
	//// m_MaxHttpSize = 0;
	// }

	// private void calcBsp() {
	// long ts = System.currentTimeMillis() - m_TransferTimepoint;
	// if (ts > 0) {
	// m_Bps = (int) ((m_BodyLength * (8 * 1000)) / ts);
	// }
	// }

	public int bps() {
		if (m_BpsTotal < 1) {
			return 0;
		}
		return (int) (m_BpsTotal / m_BpsTimes);
	}

	// public void setMaxHttpSize(int size) {
	// m_MaxHttpSize = size;
	// }

	public int getMaxHttpSize() {
		// if (m_MaxHttpSize > 0) {
		// return m_MaxHttpSize;
		// }
		return m_Server.getMaxHttpSize();
	}

	public boolean isDebugEnabled() {
		return m_Server.isDebugEnabled();
	}

	public void close() {
		ChannelHandlerContext ctx = m_Ctx;
		if (null != ctx) {
			m_Ctx = null;
			ctx.close();
		}
	}

	// private GenericFutureListener<Future<Void>> getEndListener() {
	// if (null == m_EndListener) {
	// m_EndListener = new GenericFutureListener<Future<Void>>() {
	// @Override
	// public void operationComplete(Future<Void> future) throws Exception {
	// if (future.isSuccess()) {
	// startIdleTask(m_Ctx);
	// }
	// }
	// };
	// }
	// return m_EndListener;
	// }
	//
	// private ByteBuf allocBuffer(int len) {
	// if (null != m_Ctx) {
	// return m_Ctx.alloc().buffer(len);
	// }
	// return PooledByteBufAllocator.DEFAULT.directBuffer(len);
	// }

	// private void beginResponse() {
	// // 先取消请求流
	// ByteBufInput input = m_RequestInput;
	// if (null != input) {
	// input.end();
	// }
	// // m_State = STATE_RESPONDING;
	// // 标记响应中
	// // m_ResponseWriter = NettyOutputStream._pending;
	// responding();
	// m_TransferTimepoint = System.currentTimeMillis();
	// m_BodyLength = 0;
	// }

	/**
	 * 直接无内容响应且主动关闭连接
	 * 
	 * @param status
	 *            响应状态
	 * @param responseHeaders
	 *            可选的响应头
	 */
	protected void responseAndClose(HttpResponseStatus status, HttpHeaders responseHeaders) {
		responseAndClose(status, HttpVersion.HTTP_1_0, responseHeaders);
	}

	/**
	 * 直接无内容响应且主动关闭连接
	 * 
	 * @param status
	 *            响应状态
	 * @param httpVersion
	 *            HTTP版本（如：HTTP 1.0，HTTP 1.1）
	 */
	private void responseAndClose(HttpResponseStatus status, HttpVersion httpVersion,
			HttpHeaders responseHeaders) {
		m_HttpContext = null;
		FullHttpResponse msg;
		// HttpVersion httpVersion;
		// httpVersion = m_Request.protocolVersion();
		if (null == responseHeaders) {
			msg = new DefaultFullHttpResponse(httpVersion, status);
		} else {
			msg = new DefaultFullHttpResponse(httpVersion, status, Unpooled.buffer(0),
					responseHeaders, EmptyHttpHeaders.INSTANCE);
		}
		io.netty.handler.codec.http.HttpHeaders headers = msg.headers();
		headers.set(HttpHeaderNames.CONTENT_LENGTH, HttpHeaderValues.ZERO);
		m_Ctx.writeAndFlush(msg).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				ChannelHandlerContext ctx = m_Ctx;
				if (null != ctx) {
					ctx.close();
				}
			}
		});
	}

	/**
	 * 连接空闲时间过长
	 */
	protected void idleTimeout() {
		// （空闲）断开连接
		if (null != m_HttpContext) {
			_Logger.error(formatMessage("Idle timeout(not respond)"));
		} else if (isDebugEnabled()) {
			_Logger.trace(formatMessage("Idle timeout"));
		}
		close();
	}

	/**
	 * 启动HTTP连接空闲检查
	 * 
	 * @param ctx
	 */
	private void startIdleTask(ChannelHandlerContext ctx) {
		int timeout = getIdleMillis();
		if (timeout <= 0) {
			return;
		}
		if (null == ctx || null == ctx.channel() || !ctx.channel().isActive()) {
			if (_Logger.isDebugEnabled()) {
				_Logger.debug(formatMessage("不具条件启动HTTP连接空闲检查"));
			}
			return;
		}
		if (null != m_IdleTask) {
			m_IdleTask.cancel(false);
			m_IdleTask = null;
		}
		// 启动空闲检查任务
		m_IdleTask = ctx.executor().schedule(new IdleChecker(), timeout, TimeUnit.MILLISECONDS);
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
		sb.append("{").append(m_Server.getName()).append(":");
		if (null != m_HttpContext) {
			sb.append("request");
		} else {
			sb.append("idle");
		}
		sb.append(",reuse:").append(m_Reuse);
		int bps = bps();
		if (bps > 0) {
			sb.append(",bps:").append(bps);
		}
		if (null != m_RemoteAddr) {
			sb.append(",ip:").append(m_RemoteAddr);
		}
		sb.append("}");
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
	 * 空闲超时检查
	 * 
	 * @author liangyi
	 *
	 */
	class IdleChecker implements Runnable {
		@Override
		public void run() {
			// long interval;
			try {
				// if (null != m_ResponseTimeoutTask) {
				// // 有请求超时任务在控制，略过
				// return;
				// }
				if (null != m_HttpContext) {
					// 还有调用在处理，下一轮
					if (null != m_Ctx) {
						m_IdleTask = m_Ctx.executor().schedule(this, getIdleMillis(),
								TimeUnit.MILLISECONDS);
					}
					return;
				}
				// int timeout = m_Server.getIdleMillis();
				// if (timeout <= 0 || m_TransferTimepoint <= 0 || null ==
				// m_Ctx) {
				// // 空闲超时值、请求时间点为0或已关闭，略过
				// return;
				// }
				// interval = System.currentTimeMillis() - m_TransferTimepoint;
				// if (interval >= timeout) {
				// // 超时了
				// idleTimeout();
				// return;
				// }
				// // 还没到时间，要再等等
				// interval = timeout - interval;

				// 超时了
				idleTimeout();
			} finally {
				m_IdleTask = null;
			}
			// if (_Logger.isTraceEnabled()) {
			// // _Logger.trace(formatMessage(m_Response, "idel-check(" +
			// // interval + "ms)"));
			// _Logger.trace(formatMessage("idel-check(" + interval + "ms)"));
			// }
			// // N毫秒后再检查
			// m_IdleTask = m_Ctx.executor().schedule(this, interval,
			// TimeUnit.MILLISECONDS);
		}
	}

	protected ByteBuf allocBuffer(int len) {
		if (null != m_Ctx) {
			return m_Ctx.alloc().buffer(len);
		}
		return Unpooled.buffer(len);
		// return UnpooledByteBufAllocator.DEFAULT.directBuffer(len);
	}

	protected CompositeByteBuf compositeBuffer() {
		return m_Ctx.alloc().compositeBuffer();
	}

	public String getRemoteAddr() {
		return m_RemoteAddr;
	}

	public int getIdleMillis() {
		return m_Server.getIdleMillis();
	}

	public void write(Object msg) {
		m_Ctx.write(msg);
	}

	public ChannelFuture writeAndFlush(Object msg) {
		return m_Ctx.writeAndFlush(msg);
	}

	// public void flush() {
	// m_Ctx.flush();
	// }

	public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
		ChannelHandlerContext ctx = m_Ctx;
		if (null == ctx) {
			throw new IllegalStateException(formatMessage("inactived"));
		}
		return ctx.executor().schedule(runnable, delay, unit);
	}
}
