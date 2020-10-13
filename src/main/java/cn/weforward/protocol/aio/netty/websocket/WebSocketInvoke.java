/**
 * Copyright (c) 2019,2020 honintech
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. * 
 * 
 */
package cn.weforward.protocol.aio.netty.websocket;

import java.io.IOException;
import java.io.InputStream;

import cn.weforward.protocol.aio.http.HttpHeaders;
import cn.weforward.protocol.aio.netty.ByteBufInput;
import cn.weforward.protocol.aio.netty.ByteBufStream;
import cn.weforward.protocol.aio.netty.CompositeByteBufStream;
import io.netty.buffer.ByteBuf;

/**
 * WebSocket多路复用的一次Request/Response模式调用
 * 
 * @author liangyi
 *
 */
public class WebSocketInvoke {
	/** 调用请求的数据包 */
	public final static int PACKET_REQUEST = 0x01;
	/** 调用响应的数据包 */
	public final static int PACKET_RESPONSE = 0x02;
	/** 最后的数据包 */
	public final static int PACKET_FINAL = 0x10;

	protected String m_Id;
	protected WebSocketContext m_Context;
	protected Request m_Request;
	protected Response m_Response;

	public WebSocketInvoke(WebSocketContext ctx, String id) {
		m_Context = ctx;
		m_Id = id;
	}

	public void readable(ByteBuf payload, int packetState) throws IOException {
		int type = packetState & ~PACKET_FINAL;
		if (PACKET_REQUEST == type) {
			if (null == m_Request) {
				m_Request = new Request(analyseHead(payload));
			}
			m_Request.readable(payload);
			if (PACKET_FINAL == (PACKET_FINAL & packetState)) {
				m_Request.complete();
			}
			return;
		}
		if (PACKET_RESPONSE == type) {
			if (null == m_Response) {
				m_Response = new Response(analyseHead(payload));
			}
			m_Response.readable(payload);
			if (PACKET_FINAL == (PACKET_FINAL & packetState)) {
				m_Response.complete();
			}
			return;
		}
		throw new IOException("包类型异常:" + packetState);
	}

	/**
	 * 分析HTTP头格式
	 * 
	 * @param payload
	 *            数据
	 * @return 分析到结果
	 */
	private HttpHeaders analyseHead(ByteBuf payload) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
		// TODO Auto-generated method stub
	}

	/**
	 * 请求/响应消息抽象类
	 *
	 */
	abstract class Message {
		/** 消息头 */
		HttpHeaders m_Headers;
		/** 消息体 */
		ByteBufStream m_Body;
		// /** 消息体流包装 */
		// ByteBufInput m_Stream;

		public Message(HttpHeaders headers) {
			m_Headers = headers;
		}

		public void readable(ByteBuf payload) throws IOException {
			ByteBufStream body;
			synchronized (this) {
				body = m_Body;
				if (null == body) {
					body = new CompositeByteBufStream(m_Context.compositeBuffer());
				}
			}
			body.readable(payload);

			// synchronized (this) {
			// CompositeByteBuf buffers;
			// buffers = openBuffers();
			// // 写入缓冲区
			// synchronized (buffers) {
			// buffers.addComponent(true, payload.retain());
			// }
			// }
			// // 通知流
			// ByteBufInput stream = m_Stream;
			// if (null != stream) {
			// stream.readable();
			// }
		}

		// synchronized public void complete() {
		// ByteBufInput stream = m_Stream;
		// if (null != stream) {
		// // 释放缓冲区
		// m_Body.release();
		// m_Body = null;
		// // 确认流内容完整
		// m_Stream = null;
		// stream.completed();
		// } else {
		// // 只作标记
		// m_Stream = ByteBufInput._completed;
		// }
		// }

		public void complete() throws IOException {
			ByteBufStream body = m_Body;
			if (null != body) {
				body.completed();
			}
		}

		synchronized public InputStream getStream() throws IOException {
			if (m_Body instanceof ByteBufInput) {
				return (ByteBufInput) m_Body;
			}
			ByteBufInput stream;
			if (null == m_Body) {
				stream = new ByteBufInput(m_Context.compositeBuffer(), false);
			} else {
				CompositeByteBufStream buffers = (CompositeByteBufStream) m_Body;
				stream = new ByteBufInput(buffers.detach(), buffers.isCompleted());
			}
			m_Body = stream;
			return stream;
		}

		synchronized void cleanup() {
			if (null != m_Body) {
				m_Body.abort();
				m_Body = null;
			}
			m_Headers = null;
		}

		public void abort() {
			cleanup();
			// m_Body = ByteBufStream._aborted;
		}

		// synchronized public InputStream getStream() throws IOException {
		// if (null == m_Stream) {
		// ByteBuf buf = openBuffers().retain();
		// m_Stream = new ByteBufInput(buf, false);
		// } else if (ByteBufInput._completed == m_Stream) {
		// ByteBuf buf = m_Body;
		// m_Body = null;
		// if (null == buf) {
		// // 没内容
		// return ByteBufInput._empty;
		// }
		// m_Stream = new ByteBufInput(buf, true);
		// }
		// return m_Stream;
		// }
		//
		// private CompositeByteBuf openBuffers() {
		// if (null != m_Body) {
		// m_Body = m_Context.compositeBuffer();
		// }
		// return m_Body;
		// }
		//
		// synchronized void cleanup() {
		// if (null != m_Body) {
		// m_Body.release();
		// m_Body = null;
		// }
		// if (null != m_Stream) {
		// m_Stream.end();
		// m_Stream = null;
		// }
		// }
		//
		// public synchronized void abort() {
		// cleanup();
		// m_Stream = ByteBufInput._aborted;
		// }
	}

	/**
	 * 调用请求
	 */
	class Request extends Message {
		public Request(HttpHeaders headers) {
			super(headers);
		}
	}

	/**
	 * 调用响应
	 */
	class Response extends Message {
		public Response(HttpHeaders headers) {
			super(headers);
		}
	}

}
