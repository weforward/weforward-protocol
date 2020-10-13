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
package cn.weforward.protocol.aio.netty.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.crypto.Hex;
import cn.weforward.common.util.Bytes;
import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.protocol.aio.netty.NettyHttpContext;
import cn.weforward.protocol.aio.netty.NettyHttpServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * 
 * @author liangyi
 *
 */
public class WebSocketContext extends ChannelInboundHandlerAdapter {
	static final Logger _Logger = LoggerFactory.getLogger(NettyHttpContext.class);

	protected final NettyHttpServer m_Server;
	/** netty连接的上下文 */
	protected ChannelHandlerContext m_Ctx;
	/** 客户端地址 */
	protected String m_RemoteAddr;
	/** 多路复用的通道 */
	protected Map<String, WebSocketInvoke> m_Multiplex;
	/** 请求序号生成器 */
	protected AtomicLong m_Sequencer;

	public WebSocketContext(NettyHttpServer server) {
		m_Server = server;
		m_Multiplex = new HashMap<String, WebSocketInvoke>();
		m_Sequencer = new AtomicLong();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		m_Ctx = ctx;
		// 获取调用端信息（IP+端口）
		InetSocketAddress ip = (InetSocketAddress) ctx.channel().remoteAddress();
		m_RemoteAddr = ip.getAddress().getHostAddress() + ':' + ip.getPort();
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("channelActive"));
		}
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (isDebugEnabled()) {
			_Logger.info(formatMessage("channelInactive"));
		}
		for (Map.Entry<String, WebSocketInvoke> entry : m_Multiplex.entrySet()) {
			WebSocketInvoke channel = entry.getValue();
			if (null != channel) {
				channel.close();
			}
		}
		m_Multiplex.clear();
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (null != m_Ctx && ctx != m_Ctx) {
				_Logger.error("不一样的Context？" + m_Ctx + "!=" + ctx);
				ctx.close();
				return;
			}
			// if (_Logger.isTraceEnabled()) {
			// _Logger.trace("[" + ctx.hashCode() + "]" + msg);
			// }
			if (msg instanceof WebSocketFrame) {
				// XXX
				_Logger.info(msg.toString());
				WebSocketFrame wsframe = (WebSocketFrame) msg;
				if (wsframe instanceof BinaryWebSocketFrame
						|| wsframe instanceof TextWebSocketFrame) {
					readable(wsframe);
					// 略过除了BinaryWebSocketFrame的其它类型的帧
					return;
				} else if (isDebugEnabled()) {
					_Logger.warn(formatMessage("未知帧类型？" + msg));
				}
			}
		} finally {
			super.channelRead(ctx, msg);
		}
	}

	/**
	 * websocket服务端（w）或客户端（z）标识
	 */
	protected char getSideMarker() {
		// 服务端
		return 'w';
	}

	/**
	 * 生成请求序号
	 */
	public String genRequestSequence() {
		StringBuilder builder = StringBuilderPool._128.poll();
		try {
			builder.append('P');
			builder.append(getSideMarker());
			Hex.toHex(m_Sequencer.incrementAndGet(), builder);
			return builder.toString();
		} finally {
			StringBuilderPool._128.offer(builder);
		}
	}

	/**
	 * 生成响应序号
	 */
	public String genResponseSequence(String seq) {
		StringBuilder builder = StringBuilderPool._128.poll();
		try {
			builder.append('R');
			builder.append(seq, 1, seq.length());
			return builder.toString();
		} finally {
			StringBuilderPool._128.offer(builder);
		}
	}

	private void readable(WebSocketFrame wsframe) throws IOException {
		/**
		 * <pre>
		 * 对数据包进行二次封装：
		 * 1. 加上请求/响应序号用于多路复用，序号由一个用于标识请求（P）或响应（R）的字符加流水号组成，在连接中必须保持唯一<br/>
		 *     流水号由请求生成，对应的响应匹配此序号，序号最大长度不超过128字节<br/>
		 *     websocket服务端发起的请求流水号首个字符为w，客户端为z，即：服务端为“Pw*”，客户端为“Rz*”<br/>
		 * 2. 类似HTTP协议，把数据包封装为head及body ，head使用HTTP head规范，同样使用两个换行符表示head结束<br/>
		 * 3. 把请求/响应序号放在head，方便业务层读取，标识为：WS-RPC-ID
		 * 4. 使用Binary或Text帧，每个帧Payload由序号部分与head或body部分组成，包含head的帧必须完整在一个帧内，不允许分帧，Payload格式如下：
		 * |不定长的UTF-8字符集字串并以换行符“\n”结束的请求/响应序号|head或body的各部分...
		 * </pre>
		 */
		ByteBuf payload = wsframe.content();
		// 先读取序号
		payload.markReaderIndex();
		String seq = null;
		byte seqBuf[] = Bytes.Pool._512.poll();
		int packetState = 0;
		try {
			for (int i = 0; i < seqBuf.length && payload.isReadable(); i++) {
				seqBuf[i] = payload.readByte();
				if ('\n' == seqBuf[i]) {
					// 碰到换行符，序号已完成
					if ('P' == seqBuf[0]) {
						// 这是请求，那就按server处理
						packetState = WebSocketInvoke.PACKET_REQUEST;
					} else if ('R' == seqBuf[0]) {
						// 这是响应，那就是按client处理
						packetState = WebSocketInvoke.PACKET_RESPONSE;
					} else {
						// 标识有误
						_Logger.error("帧格式异常，序号标识错误：" + Hex.encode(seqBuf, 0, i));
						close();
						return;
					}
					seq = new String(seqBuf, 1, i, "UTF-8");
					break;
				}
			}
			if (null == seq) {
				// 没有序号的帧？
				_Logger.error("帧格式异常，序号没有/不合格" + seqBuf);
				close();
				return;
			}
		} finally {
			Bytes.Pool._512.offer(seqBuf);
		}
		seqBuf = null;
		if (wsframe.isFinalFragment()) {
			// 是最后的帧
			packetState |= WebSocketInvoke.PACKET_FINAL;
		}
		WebSocketInvoke invoke = openInvoke(seq);
		invoke.readable(payload, packetState);
	}

	synchronized private WebSocketInvoke openInvoke(String seq) {
		WebSocketInvoke channel = m_Multiplex.get(seq);
		if (null == channel) {
			channel = new WebSocketInvoke(this, seq);
			m_Multiplex.put(seq, channel);
		}
		return channel;
	}

	synchronized protected WebSocketInvoke removeInvoke(String seq) {
		return m_Multiplex.remove(seq);
	}

	public void close() {
		ChannelHandlerContext ctx = m_Ctx;
		if (null != ctx) {
			m_Ctx = null;
			ctx.close();
		}
	}

	protected CompositeByteBuf compositeBuffer() {
		return m_Ctx.alloc().compositeBuffer();
	}

	public boolean isDebugEnabled() {
		return m_Server.isDebugEnabled();
	}

	private String formatMessage(String caption) {
		StringBuilder sb = new StringBuilder(128);
		if (null != caption) {
			sb.append(caption);
		}
		toString(sb);
		return sb.toString();
	}

	public StringBuilder toString(StringBuilder sb) {
		sb.append("{").append(m_Server.getName()).append(":");
		if (null != m_RemoteAddr) {
			sb.append(m_RemoteAddr);
		}
		sb.append(",mul:").append(m_Multiplex.size());
		sb.append("}");
		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		return toString(sb).toString();
	}
}
