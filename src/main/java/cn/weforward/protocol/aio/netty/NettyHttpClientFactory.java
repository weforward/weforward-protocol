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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.protocol.aio.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.OutOfDirectMemoryError;

/**
 * NettyHttpClient工厂
 * 
 * @author liangyi
 *
 */
public class NettyHttpClientFactory {
	static final Logger _Logger = LoggerFactory.getLogger(NettyHttpClientFactory.class);

	Bootstrap m_Bootstrap;
	EventLoopGroup m_EventLoopGroup;
	/** 按主机+端口组织的连接池 */
	Map<String, Service> m_Services;
	/** SSL支持 */
	SslContext m_SslContext;
	/** 名称 */
	String m_Name;
	/** 工作线程数 */
	int m_Threads;
	/** 空闲超时值（毫秒） */
	protected int m_IdleMillis = 10 * 60 * 1000;
	/** 是否debug模式 */
	protected boolean m_DebugEnabled = false;

	public NettyHttpClientFactory() {
		m_Services = new HashMap<String, Service>();
	}

	public void setName(String name) {
		m_Name = name;
	}

	public String getName() {
		return m_Name;
	}

	public void setThreads(int threads) {
		m_Threads = threads;
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

	private String genKey(String host, int port) {
		return host + ":" + port;
	}

	private Service openService(String host, int port) {
		String key = genKey(host, port);
		Service service;
		synchronized (m_Services) {
			service = m_Services.get(key);
			if (null == service) {
				service = new Service(host, port);
				m_Services.put(key, service);
			}
		}
		return service;
	}

	private Channel getIdelChannel(String host, int port) {
		String key = genKey(host, port);
		Service service;
		synchronized (m_Services) {
			service = m_Services.get(key);
			if (null != service) {
				return service.get();
			}
		}
		return null;
	}

	/**
	 * 创建异步HTTP客户端
	 * 
	 * @param handler
	 *            客户端处理器，若指定ClientHandler.SYNC则可工作在同步模式
	 * @return HTTP客户端
	 */
	public NettyHttpClient open(ClientHandler handler) {
		NettyHttpClient client = new NettyHttpClient(this, handler);
		return client;
	}

	public void connect(final NettyHttpClient client, String host, int port, final boolean ssl)
			throws IOException {
		if (ssl && null == m_SslContext) {
			throw new SSLException("不支持");
		}
		start();

		Channel channel = getIdelChannel(host, port);
		if (null != channel) {
			// 有空闲连接，直接关联
			channel.pipeline().addLast("client", client);
			return;
		}
		ChannelFuture f = m_Bootstrap.connect(host, port);
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				Channel channel = future.channel();
				if (future.isSuccess()) {
					InetSocketAddress ia = (InetSocketAddress) channel.remoteAddress();
					String host = ia.getHostString();
					int port = ia.getPort();
					ChannelPipeline pipeline = channel.pipeline();
					if (ssl) {
						pipeline.addFirst("ssl", m_SslContext.newHandler(channel.alloc()));
					}
					pipeline.addLast("service", openService(host, port));
					pipeline.addLast("client", client);
					if (_Logger.isTraceEnabled()) {
						_Logger.trace("已连接：" + channel);
					}
				} else {
					// 连接失败？
					// _Logger.error(future.toString(), future.cause());
					client.connectFail(future.cause());
					channel.close();
				}
			}
		});
	}

	public void free(Channel channel) {
		if (null == channel || !channel.isActive()) {
			return;
		}
		InetSocketAddress ia = (InetSocketAddress) channel.remoteAddress();
		String host = ia.getHostString();
		int port = ia.getPort();
		Service service = openService(host, port);
		service.free(channel);
	}

	/**
	 * 指定连接超时值，默认值是5秒
	 * 
	 * @param millis
	 *            超时值（毫秒）
	 */
	synchronized public void setConnectTimeout(int millis) {
		if (null == m_Bootstrap) {
			m_Bootstrap = new Bootstrap();
		}
		m_Bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, millis);
	}

	public int getConnectTimeout() {
		Integer v = (Integer) m_Bootstrap.config().options()
				.get(ChannelOption.CONNECT_TIMEOUT_MILLIS);
		return (null == v) ? 0 : v;
	}

	public void setSsl(boolean enabled) throws SSLException {
		if (enabled) {
			SslContextBuilder builder = SslContextBuilder.forClient();
			m_SslContext = builder.build();
		} else {
			m_SslContext = null;
		}
	}

	public void setDebugEnabled(boolean enabled) {
		m_DebugEnabled = enabled;
	}

	public boolean isDebugEnabled() {
		return m_DebugEnabled;
	}

	synchronized public void close() {
		if (null != m_EventLoopGroup) {
			m_EventLoopGroup.shutdownGracefully();
		}
		m_EventLoopGroup = null;
		m_Bootstrap = null;
	}

	synchronized private void start() {
		if (null != m_EventLoopGroup) {
			return;
		}
		String name = getName();
		if (null == name || 0 == name.length()) {
			name = "hc";
		} else {
			name = name + "-hc";
		}
		ThreadFactory threadFactory = new DefaultThreadFactory(name);
		m_EventLoopGroup = new NioEventLoopGroup(m_Threads, threadFactory);
		m_Bootstrap = new Bootstrap();
		m_Bootstrap.group(m_EventLoopGroup);
		m_Bootstrap.channel(NioSocketChannel.class);
		m_Bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		m_Bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
		m_Bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("c-encoder", new HttpRequestEncoder());
				pipeline.addLast("c-decoder", new HttpResponseDecoder());
			}
		});
		NettyMemMonitor.getInstance().log();
	}

	/**
	 * 包装Channel及相关任务等（如：空闲检查）
	 * 
	 * @author liangyi
	 *
	 */
	static class ServiceChannel {
		final Service m_Service;
		final Channel m_Channel;
		ScheduledFuture<?> m_IdleTask;

		ServiceChannel(Service service, Channel channel) {
			m_Service = service;
			m_Channel = channel;
			startIdleTask();
		}

		Channel take() {
			if (null != m_IdleTask) {
				m_IdleTask.cancel(true);
				m_IdleTask = null;
			}
			if (m_Channel.isActive() && m_Channel.isOpen()) {
				return m_Channel;
			}
			return null;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof Channel && m_Channel == obj) {
				return true;
			}
			return false;
		}

		private void startIdleTask() {
			int timeout = m_Service.getIdleMillis();
			if (timeout > 0 && null != m_Channel && m_Channel.isActive()) {
				// 启动空闲检查任务
				m_IdleTask = m_Channel.eventLoop().schedule(new IdleChecker(), timeout,
						TimeUnit.MILLISECONDS);
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
				boolean ret;
				// synchronized (Service.this) {
				// ret = m_Channels.remove(channel);
				// }
				ret = m_Service.remove(ServiceChannel.this);
				if (ret) {
					_Logger.info("Idle " + m_Channel);
					// 关闭
					m_Channel.close();
				} else if (_Logger.isTraceEnabled()) {
					_Logger.trace("not free " + m_Channel);
				}
			}
		}
	}

	/**
	 * 由host+port标识的一组连接
	 * 
	 * @author liangyi
	 *
	 */
	@Sharable
	class Service extends ChannelInboundHandlerAdapter {
		/** 空闲连接 */
		List<ServiceChannel> m_Channels;
		/** 主机 */
		String m_Host;
		/** 端口 */
		int m_Port;

		Service(String host, int port) {
			m_Host = host;
			m_Port = port;
			m_Channels = new LinkedList<ServiceChannel>();
		}

		synchronized boolean remove(ServiceChannel serviceChannel) {
			return m_Channels.remove(serviceChannel);
		}

		@SuppressWarnings("unlikely-arg-type")
		synchronized boolean remove(Channel channel) {
			return m_Channels.remove(channel);
		}

		public int getIdleMillis() {
			return NettyHttpClientFactory.this.getIdleMillis();
		}

		synchronized public Channel get() {
			if (m_Channels.size() > 0) {
				ServiceChannel sc = m_Channels.remove(m_Channels.size() - 1);
				if (null != sc) {
					return sc.take();
				}
			}
			return null;
		}

		synchronized public void free(Channel channel) {
			m_Channels.add(new ServiceChannel(this, channel));
		}

		// @Override
		// public void channelActive(ChannelHandlerContext ctx) throws Exception
		// {
		// super.channelActive(ctx);
		// }

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			_Logger.info("断开：" + ctx.channel());
			remove(ctx.channel());
			super.channelInactive(ctx);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			if (cause instanceof OutOfDirectMemoryError) {
				ctx.close();
			}
			super.exceptionCaught(ctx, cause);
		}
	}
}
