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

import cn.weforward.protocol.aio.ClientHandler;
import cn.weforward.protocol.aio.ServerHandler;
import cn.weforward.protocol.aio.http.HttpContext;
import cn.weforward.protocol.aio.http.HttpHeaders;
import cn.weforward.protocol.aio.netty.HeadersParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;

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
	public final static int PACKET_MARK_FINAL = 0x10;
	/** 已初始化header */
	public final static int PACKET_MARK_HEADER = 0x20;

	protected String m_Id;
	protected WebSocketContext m_Context;
	protected WebSocketRequest m_Request;
	protected WebSocketResponse m_Response;
	protected HeadersParser m_HeadersParser;

	public WebSocketInvoke(WebSocketContext ctx, String id) {
		m_Context = ctx;
		m_Id = id;
	}

	public int readable(ByteBuf payload, int packetState) throws IOException {
		int type = packetState & ~PACKET_MARK_FINAL;
		if (PACKET_REQUEST == type) {
			if (null == m_Request) {
				m_Request = new WebSocketRequest(this, analyseHead(payload));
				packetState |= PACKET_MARK_HEADER;
			}
			m_Request.readable(payload);
			if (PACKET_MARK_FINAL == (PACKET_MARK_FINAL & packetState)) {
				m_Request.complete();
			}
			return packetState;
		}
		if (PACKET_RESPONSE == type) {
			if (null == m_Response) {
				m_Response = new WebSocketResponse(this, analyseHead(payload));
				packetState |= PACKET_MARK_HEADER;
			}
			m_Response.readable(payload);
			if (PACKET_MARK_FINAL == (PACKET_MARK_FINAL & packetState)) {
				m_Response.complete();
			}
			return packetState;
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
		if (null == m_HeadersParser) {
			m_HeadersParser = new HeadersParser(8192);
		}
		HttpHeaders headers = m_HeadersParser.parse(payload);
		m_HeadersParser.reset();
		return headers;
	}

	public void close() {
		WebSocketRequest req = m_Request;
		WebSocketResponse rsp = m_Response;
		if (null != req) {
			req.abort();
		}
		if (null != rsp) {
			rsp.abort();
		}
	}

	public HttpContext httpContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRespond() {
		// TODO Auto-generated method stub
		return false;
	}

	public void response(HttpResponseStatus status) {
		// TODO Auto-generated method stub

	}

	public void request(ServerHandler handler) {
		// TODO Auto-generated method stub
		handler.requestHeader();
	}

	public CompositeByteBuf compositeBuffer() {
		return m_Context.compositeBuffer();
	}

	public void openRequest(ClientHandler handler, String url, String method) {
		// TODO Auto-generated method stub
		
	}

}
