package cn.weforward.protocol.aio.netty.websocket;

import java.io.IOException;
import java.io.InputStream;

import cn.weforward.protocol.aio.http.HttpHeaders;
import cn.weforward.protocol.aio.netty.ByteBufInput;
import cn.weforward.protocol.aio.netty.ByteBufStream;
import cn.weforward.protocol.aio.netty.CompositeByteBufStream;
import io.netty.buffer.ByteBuf;

/**
 * Websocket下的类似模拟HTTP请求/响应的封装
 * 
 * @author liangyi
 *
 */
public abstract class WebSocketMessage {
	WebSocketInvoke m_Invoke;
	/** 消息头 */
	HttpHeaders m_Headers;
	/** 消息体 */
	ByteBufStream m_Body;

	public WebSocketMessage(WebSocketInvoke invoke, HttpHeaders headers) {
		m_Headers = headers;
		m_Invoke = invoke;
	}

	public void readable(ByteBuf payload) throws IOException {
		ByteBufStream body;
		synchronized (this) {
			body = m_Body;
			if (null == body) {
				body = new CompositeByteBufStream(m_Invoke.compositeBuffer());
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
			stream = new ByteBufInput(m_Invoke.compositeBuffer(), false);
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
