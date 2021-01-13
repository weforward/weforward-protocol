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
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.util.NumberUtil;
import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.protocol.aio.ServerHandler;
import cn.weforward.protocol.aio.http.HttpContext;
import cn.weforward.protocol.aio.http.ServerHandlerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 基于netty的HTTP server
 * 
 * @author liangyi
 *
 */
public class NettyHttpServer {
	public static final Logger _Logger = LoggerFactory.getLogger(NettyHttpServer.class);

	/** 服务器名 */
	protected String m_Name;
	protected int m_Port;
	protected int m_Backlog;
	protected int m_AcceptThreads;
	protected int m_WorkThreads;
	/** 指定的Keep-Alive头 */
	protected String m_KeepAlive;
	/** 限制请求的数据量（字节），默认4M */
	protected int m_MaxHttpSize = 4 * 1024 * 1024;
	/** 允许GZIP压缩输出 */
	protected boolean m_GzipEnabled = false;
	/** 最小压缩的size */
	protected int m_GzipMinSize = 512;
	/** 是否debug模式 */
	protected boolean m_DebugEnabled = false;

	/** 监听中的服务器Channel */
	protected volatile Channel m_Channel;
	/** 业务处理器工厂 */
	protected ServerHandlerFactory m_HandlerFactory = _unassigned;
	/** 空闲超时值（毫秒） */
	protected int m_IdleMillis = 10 * 60 * 1000;
	/** 支持WebSocket的Factory（若开启） */
	protected WebSocketServerHandshakerFactory m_WebSocketFactory;

	public NettyHttpServer(int port) {
		m_Port = port;
		// m_AcceptThreads = NettyRuntime.availableProcessors();
		m_AcceptThreads = 1;
		m_WorkThreads = NettyRuntime.availableProcessors();
		if (m_WorkThreads < 2) {
			m_WorkThreads = 2;
		}
	}

	public void setName(String name) {
		m_Name = name;
		// if (m_Executor instanceof ThreadPool) {
		// ((ThreadPool) m_Executor).setName(name);
		// }
	}

	public void setBacklog(int backlog) {
		m_Backlog = backlog;
	}

	public void setDebugEnabled(boolean enabled) {
		m_DebugEnabled = enabled;
	}

	public boolean isDebugEnabled() {
		return m_DebugEnabled;
	}

	/**
	 * 指定连接建立的处理线程数，默认1个
	 */
	synchronized public void setAcceptThreads(int maxThreads) {
		if (maxThreads == m_AcceptThreads) {
			return;
		}
		m_AcceptThreads = maxThreads;
		if (isRunning()) {
			restart();
		}
	}

	/**
	 * 指定IO（读写）的工作线程数，默认CPU数x2个
	 */
	synchronized public void setWorkThreads(int maxThreads) {
		if (maxThreads == m_WorkThreads) {
			return;
		}
		m_WorkThreads = maxThreads;
		if (isRunning()) {
			restart();
		}
	}

	public String getName() {
		return m_Name;
	}

	public int getPort() {
		return m_Port;
	}

	/**
	 * 指定Keep-Alive头， 如“timeout=300,
	 * max=200”，表示要求客户端保持300秒长连接、重用次数不超过200，JDK中sun实现的HttpURLConnection支持这个头
	 * 
	 * @param keepAlive
	 *            Keep-Alive头内容
	 */
	public void setKeepAlive(String keepAlive) {
		m_KeepAlive = (keepAlive);
	}

	public String getKeepAlive() {
		return m_KeepAlive;
	}

	/** 限制请求的数据量（字节），默认4M */
	public void setMaxHttpSize(int maxHttpSize) {
		m_MaxHttpSize = maxHttpSize;
	}

	public int getMaxHttpSize() {
		return m_MaxHttpSize;
	}

	public boolean isRunning() {
		return (null != m_Channel);
	}

	/**
	 * 是否允许GZIP压缩输出
	 * 
	 * @param enabled
	 *            是则允许
	 */
	public void setGzipEnabled(boolean enabled) {
		m_GzipEnabled = enabled;
	}

	public boolean isGzipEnabled() {
		return m_GzipEnabled;
	}

	/**
	 * GZIP的最小输出大小（字节），默认为512字节
	 * 
	 * @param minSize
	 *            最小字节
	 */
	public void setGzipMinSize(int minSize) {
		m_GzipMinSize = minSize;
	}

	// /**
	// * 设置记录超过最大消耗时间的请求
	// *
	// * @param mills
	// * 最大消耗时间（毫秒）
	// */
	// public void setElapseTime(int mills) {
	// // TODO
	// }

	public void setHandlerFactory(ServerHandlerFactory factory) {
		m_HandlerFactory = factory;
	}

	public ServerHandlerFactory getHandlerFactory() {
		return m_HandlerFactory;
	}

	/**
	 * 空闲超时值（秒），默认10分钟
	 * 
	 * @param secs
	 *            空闲秒数
	 */
	public void setIdle(int secs) {
		m_IdleMillis = secs * 1000;
	}

	public int getIdleMillis() {
		return m_IdleMillis;
	}

	/**
	 * 启用或禁止WebSocket支持
	 * 
	 * @param uri
	 *            websocket的URI，响应给客户端的sec-websocket-location头，若指定则启用支持，若null或空字串则禁止，如：ws://127.0.0.1
	 * 
	 */
	public void setWebSocket(String uri) {
		if (null == uri || uri.length() == 0) {
			// 禁止
			m_WebSocketFactory = null;
			return;
		}
		// 开启
		if (null == m_WebSocketFactory) {
			m_WebSocketFactory = new WebSocketServerHandshakerFactory(uri, null, false);
		}
	}

	public boolean close() {
		Channel channel = m_Channel;
		if (null == channel) {
			return false;
		}
		_Logger.info("closing...");
		try {
			channel.close().sync();
		} catch (InterruptedException e) {
			_Logger.warn("close execption", e);
		}
		return true;
	}

	synchronized public void restart() {
		close();
		start();
	}

	synchronized public boolean start() {
		if (null != m_Channel) {
			// 已启动
			return true;
		}

		_Logger.info("ApiServer is starting... ");
		// 处理accept连接的线程池
		final EventLoopGroup bossGroup;
		String name = getName();
		name = (null != name && name.length() > 0) ? "at" : (name + "-at");
		ThreadFactory acceptorThreadFactory = new DefaultThreadFactory(name);
		bossGroup = new NioEventLoopGroup(m_AcceptThreads, acceptorThreadFactory);
		// 处理IO的工作线程池
		final EventLoopGroup workerGroup;
		name = getName();
		name = (null != name && name.length() > 0) ? "wk" : (name + "-wk");
		ThreadFactory workThreadFactory = new DefaultThreadFactory(name);
		workerGroup = new NioEventLoopGroup(m_WorkThreads, workThreadFactory);
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new Initializer()).option(ChannelOption.SO_REUSEADDR, true)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.TCP_NODELAY, true);
		if (m_Backlog > 0) {
			// 指定Backlog
			b.option(ChannelOption.SO_BACKLOG, m_Backlog);
			_Logger.info("set backlog=" + m_Backlog);
		}
		try {
			ChannelFuture f = b.bind(m_Port).sync();
			_Logger.info("started " + NettyHttpServer.this);
			m_Channel = f.channel();
			// 监听关闭事件
			m_Channel.closeFuture().addListener(new GenericFutureListener<Future<Void>>() {
				@Override
				public void operationComplete(Future<Void> future) throws Exception {
					_Logger.info("closed " + NettyHttpServer.this);
					m_Channel = null;
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
				}
			});
			NettyMemMonitor.getInstance().log();
			return true;
		} catch (InterruptedException e) {
			_Logger.error("Bind server socket failed! " + NettyHttpServer.this, e);
		}
		return false;
	}

	public void run() {
		if (!start()) {
			return;
		}
		try {
			m_Channel.closeFuture().sync();
		} catch (InterruptedException e) {
			// logger.error("HttpServer start @port[" + m_Port + "] FAIL!",
			// e);
			_Logger.error("Interrupted! " + this, e);
		} finally {
		}
		_Logger.info("Done. " + this);
	}

	@Override
	public String toString() {
		StringBuilder sb = StringBuilderPool._128.poll();
		try {
			sb.append("{port:").append(m_Port);
			String name = getName();
			if (null != name && name.length() > 0) {
				sb.append(",name:").append(name);
			}
			sb.append(",m-at:").append(m_AcceptThreads).append(",m-wk:").append(m_WorkThreads)
					.append(",c:");
			// if (null != m_Executor) {
			// sb.append(",executor:").append(m_Executor);
			// }
			sb.append("}");
			return sb.toString();
		} finally {
			StringBuilderPool._128.offer(sb);
		}
	}

	/**
	 * 创建/指定处理HTTP请求的业务处理器
	 * 
	 * @param httpContext
	 *            HTTP服务端上下文
	 * @return 业务处理器
	 */
	public ServerHandler handle(HttpContext httpContext) throws IOException {
		return m_HandlerFactory.handle(httpContext);
	}

	/**
	 * 取得支持WebSocket的Factory
	 * 
	 * @return 若不支持则返回null
	 */
	public WebSocketServerHandshakerFactory getWebSocketFactory() {
		return m_WebSocketFactory;
	}

	/**
	 * 连接初始器
	 * 
	 * @author liangyi
	 *
	 */
	class Initializer extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast("s-decoder", new HttpRequestDecoder());
			pipeline.addLast("s-encoder", new HttpResponseEncoder());
			if (isGzipEnabled()) {
				pipeline.addLast("s-deflater", new Compressor());
			}
			pipeline.addLast("http-ctx", new NettyHttpHandler(NettyHttpServer.this));
		}
	}

	/**
	 * GZIP支持
	 * 
	 * @author liangyi
	 *
	 */
	class Compressor extends HttpContentCompressor {

		@Override
		protected Result beginEncode(HttpResponse headers, String acceptEncoding) throws Exception {
			if (m_GzipMinSize > 0) {
				int length = NumberUtil.toInt(headers.headers().get(HttpHeaderNames.CONTENT_LENGTH),
						0);
				if (length < m_GzipMinSize) {
					// 内容太少，不压缩
					return null;
				}
			}
			return super.beginEncode(headers, acceptEncoding);
		}

	}

	/**
	 * 未指定前占位用
	 */
	static protected ServerHandlerFactory _unassigned = new ServerHandlerFactory() {

		@Override
		public ServerHandler handle(HttpContext httpContext) {
			return null;
		}
	};
}
