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
package cn.weforward.proxy.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.io.OutputStreamNio;
import cn.weforward.common.json.JsonInput;
import cn.weforward.common.json.JsonInputStream;
import cn.weforward.common.json.JsonNode;
import cn.weforward.common.json.JsonOutput;
import cn.weforward.common.json.JsonParseAbort;
import cn.weforward.common.json.JsonUtil;
import cn.weforward.common.json.StringOutput;
import cn.weforward.protocol.aio.ServerHandler;
import cn.weforward.protocol.aio.http.HttpConstants;
import cn.weforward.protocol.aio.http.HttpContext;
import cn.weforward.proxy.AccessHandler;
import cn.weforward.proxy.EndpointPipe;
import cn.weforward.proxy.Tunnel;

/**
 * HTTP转发通道
 * 
 * @author liangyi
 *
 */
public class HttpTunnel implements ServerHandler, Tunnel {
	HttpProxy m_Proxy;
	HttpContext m_Context;
	JsonListener m_JsonListener;
	AccessHandler m_Access;
	EndpointPipe m_Endpoint;
	RespoinseOut m_Response;

	public HttpTunnel(HttpProxy httpProxy, HttpContext httpContext) {
		m_Proxy = httpProxy;
		m_Context = httpContext;
	}

	///////////// Tunnel implement //////////
	@Override
	public void permit(AccessHandler accessHandler) throws IOException {
		m_Access = accessHandler;
		if (null != m_Access && null != m_JsonListener && null != m_JsonListener.hyReq) {
			m_Access.joint(m_JsonListener.hyReq, this);
		}
	}

	@Override
	public void established(EndpointPipe endpoint) throws IOException {
		// 服务端点就绪后
		m_Endpoint = endpoint;
		// 替换hy_req节点后把请求转发过去
		OutputStream writer = m_Endpoint.openRequest();
		putHyReq(writer);
		m_Context.requestTransferTo(writer, m_JsonListener.nextPosition);
		if (m_Context.isRequestCompleted()) {
			completeRequest();
		}
	}

	@Override
	public OutputStream openResponse(EndpointPipe endpoint) throws IOException {
		m_Response = new RespoinseOut(m_Context.openResponseWriter(HttpConstants.OK, null));
		return m_Response;
	}

	@Override
	public void cancelResponse() throws IOException {
		if (null != m_Response) {
			m_Response.cancel();
			m_Response = null;
		}
		m_Context.disconnect();
	}

	@Override
	public void responseError(byte[] msg) throws IOException {
		m_Context.setResponseHeader("Content-Type", "application/json;charset=utf-8");
		m_Context.response(HttpConstants.OK, msg);
		m_Context.disconnect();
	}

	///////////// ServerHandler implement //////////
	@Override
	public void requestHeader() {
		String serviceName = m_Context.getUri();
		if ('/' == serviceName.charAt(0)) {
			serviceName = serviceName.substring(1);
		}
		try {
			m_Proxy.getHandler().auth(this, serviceName, m_Context.getRequestHeaders());
		} catch (IOException e) {
			// 校验失败
			byte[] msg;
			try {
				msg = "{\"hy_resp\":{\"hy_code\":1002,\"hy_msg\":\"验证失败\"}}".getBytes("UTF-8");
				responseError(msg);
			} catch (IOException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}
			return;
		}
	}

	@Override
	public void prepared(int available) {
		if (null == m_JsonListener && available > 1024) {
			// 尝试解包hy_req
			parseHyReq();
		}
	}

	@Override
	public void requestAbort() {
		// TODO Auto-generated method stub
		if (null != m_Endpoint) {
			m_Endpoint.cancelRequest();
			try {
				byte[] msg;
				msg = "{\"hy_resp\":{\"hy_code\":5002,\"hy_msg\":\"网络异常\"}}".getBytes("UTF-8");
				responseError(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
	}

	@Override
	public void requestCompleted() {
		if (null == m_JsonListener) {
			// 尝试解包hy_req
			parseHyReq();
		}
		if (null != m_Endpoint) {
			completeRequest();
		}
	}

	@Override
	public void responseCompleted() {
		// TODO Auto-generated method stub
	}

	@Override
	public void errorRequestTransferTo(IOException e, Object msg, OutputStream writer) {
		// TODO Auto-generated method stub
	}

	///////////////////////////////////////

	private boolean parseHyReq() {
		JsonInput in = null;
		try {
			// in = new JsonInputStream(m_Context.duplicateRequestStream(),
			// "UTF-8");
			in = new JsonInputStream(m_Context.duplicateRequestStream(), "ISO-8859-1");
			if (null == m_JsonListener) {
				m_JsonListener = new JsonListener();
			}
			try {
				JsonUtil.parse(in, m_JsonListener);
			} catch (JsonParseAbort e) {
			}
			if (null == m_JsonListener.hyReq) {
				// 估计请求的json内容不对
				byte[] msg;
				msg = "{\"hy_resp\":{\"hy_code\":1102,\"hy_msg\":\"请求无效\"}}".getBytes("UTF-8");
				responseError(msg);
				return false;
			}
			// 读取到“,”分隔符
			char ch = JsonUtil.skipBlank(in, 100);
			if (',' != ch) {
				// 格式有问题
				byte[] msg;
				msg = "{\"hy_resp\":{\"hy_code\":1102,\"hy_msg\":\"请求无效\"}}".getBytes("UTF-8");
				responseError(msg);
				return false;
			}
			// 记下读取的位置（XXX 必须保证hy_req节点没有中文才行）
			m_JsonListener.nextPosition = in.position();
			if (null != m_Access) {
				m_Access.joint(m_JsonListener.hyReq, this);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return true;
	}

	/**
	 * 向微服务输出（替换的）hy_req节点
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void putHyReq(OutputStream writer) throws IOException {
		// TODO Auto-generated method stub
		JsonOutput out = new StringOutput();
		out.append("{\"hy_req\":{");
		out.append("\"client_access\":\"").append("H-12345678-1234").append("\"");
		out.append(",\"user_session\":\"").append("asdfghjkl1234567890").append("\"");
		out.append(",\"res_id\":\"");
		JsonUtil.escape("File$abc123", out);
		out.append("\"");
		out.append(",\"res_right\":").append('7');
		out.append(",\"trace_id\":\"").append("1234567890abcdef").append("\"");
		out.append(",\"forward_from\":\"").append("x0002,x0003").append("\"");
		out.append("},");
		byte[] hyReq = out.toString().getBytes("UTF-8");
		writer.write(hyReq, 0, hyReq.length);
	}

	/**
	 * 向客户端输出HTTP响应头
	 * 
	 * @throws IOException
	 */
	private void responseHeaders() throws IOException {
		m_Context.setResponseHeader("Content-Type", "application/json;charset=utf-8");
		m_Context.setResponseHeader("HY-Noise", "");
		m_Context.setResponseHeader("HY-Content-Sign", "");
		m_Context.setResponseHeader("HY-Tag", "");
		m_Context.setResponseHeader("HY-Channel", "");
		m_Context.setResponseHeader("HY-Secure", "");
		m_Context.setResponseHeader("Authorization", "HY-None");
	}

	private void completeRequest() {
		try {
			m_Endpoint.openRequest().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// 转送请求失败
			try {
				byte[] msg;
				msg = "{\"hy_resp\":{\"hy_code\":5002,\"hy_msg\":\"网络异常\"}}".getBytes("UTF-8");
				responseError(msg);
			} catch (IOException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}
		}
	}

	/**
	 * 监听JSON解析
	 * 
	 * @author liangyi
	 *
	 */
	static class JsonListener implements JsonUtil.Listener {
		/** 已匹配到的hy_req节点 */
		JsonNode hyReq;
		/** 下个节点的位置 */
		int nextPosition;

		@Override
		public void foundNode(JsonNode value, String name, int depth) throws JsonParseAbort {
			if (1 == depth && "hy_req".equals(name)) {
				hyReq = value;
				throw JsonParseAbort.MATCHED;
			}
		}
	}

	class RespoinseOut extends OutputStream implements OutputStreamNio {
		OutputStream writer;
		boolean headed;

		private void headed() throws IOException {
			if (!headed) {
				// 先输出HTTP headers
				responseHeaders();
				headed = true;
			}
		}

		public RespoinseOut(OutputStream writer) {
			this.writer = writer;
		}

		@Override
		public int write(ByteBuffer src) throws IOException {
			headed();
			if (this.writer instanceof OutputStreamNio) {
				return ((OutputStreamNio) this.writer).write(src);
			}

			int i = 0;
			while (src.hasRemaining()) {
				writer.write(src.get());
				++i;
			}
			return i;
		}

		@Override
		public void write(int b) throws IOException {
			headed();
			this.writer.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			headed();
			this.writer.write(b, off, len);
		}

		@Override
		public void close() throws IOException {
			headed();
			this.writer.close();
		}

		@Override
		public void cancel() throws IOException {
			if (this.writer instanceof OutputStreamNio) {
				((OutputStreamNio) this.writer).cancel();
			} else {
				this.writer.close();
			}
		}

		public int write(InputStream src) throws IOException {
			return BytesOutputStream.transfer(src, this, -1);
		}
	}

	@Override
	public void responseTimeout() {
		// TODO Auto-generated method stub

	}
}
