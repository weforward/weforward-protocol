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
package cn.weforward.protocol.client.netty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.aio.ClientHandler;
import cn.weforward.protocol.aio.http.HttpConstants;
import cn.weforward.protocol.aio.netty.NettyHttpClient;
import cn.weforward.protocol.aio.netty.NettyHttpClientFactory;
import cn.weforward.protocol.client.AbstractServiceInvoker;
import cn.weforward.protocol.client.AioServiceInvoker;
import cn.weforward.protocol.client.execption.ServiceInvokeException;
import cn.weforward.protocol.exception.AuthException;
import cn.weforward.protocol.exception.SerialException;
import cn.weforward.protocol.ext.Producer;
import cn.weforward.protocol.support.SimpleProducer.SimpleProducerInput;
import cn.weforward.protocol.support.SimpleProducer.SimpleProducerOutput;

/**
 * netty服务调用器
 * 
 * @author daibo
 *
 */
public class NettyServiceInvoker extends AbstractServiceInvoker implements AioServiceInvoker {
	private final static NettyHttpClientFactory FACTORY = new NettyHttpClientFactory();

	protected String m_Charset = Header.CHARSET_UTF8;
	protected String m_ContentType = Header.CONTENT_TYPE_JSON;
	protected String m_AuthType = Header.AUTH_TYPE_SHA2;
	protected String m_ServiceUrl;
	protected String m_ServiceName;
	protected String m_AccessId;
	protected int m_ConnectTimeout = 5 * 1000;
	protected int m_ReadTimeout = 50 * 1000;

	protected Producer m_Producer;

	public NettyServiceInvoker(String preUrl, String serviceName, Producer producer) {
		if (StringUtil.isEmpty(preUrl) || StringUtil.isEmpty(serviceName)) {
			throw new IllegalArgumentException("链接与服务名不能为空");
		}
		String url;
		if (preUrl.endsWith(serviceName)) {
			url = preUrl;
		} else if (preUrl.endsWith("/")) {
			url = preUrl + serviceName;
		} else {
			url = preUrl + "/" + serviceName;
		}
		m_ServiceUrl = url;
		m_ServiceName = serviceName;
		m_Producer = producer;
	}

	public void setProducer(Producer producer) {
		m_Producer = producer;
	}

	@Override
	public String getContentType() {
		return m_ContentType;
	}

	@Override
	public void setContentType(String type) {
		m_ContentType = type;
	}

	@Override
	public String getAuthType() {
		return m_AuthType;
	}

	@Override
	public void setAuthType(String type) {
		m_AuthType = type;
	}

	@Override
	public int getConnectTimeout() {
		return m_ConnectTimeout;
	}

	@Override
	public void setConnectTimeout(int ms) {
		m_ConnectTimeout = ms;
	}

	@Override
	public int getReadTimeout() {
		return m_ReadTimeout;
	}

	@Override
	public void setReadTimeout(int ms) {
		m_ReadTimeout = ms;
	}

	@Override
	protected String getServiceName() {
		return m_ServiceName;
	}

	@Override
	protected String getCharset() {
		return m_Charset;
	}

	public void setAccessId(String accessId) {
		m_AccessId = accessId;
	}

	@Override
	protected String getAccessId() {
		return m_AccessId;
	}

	@Override
	public Response invoke(Request request) throws ServiceInvokeException {
		NettyHttpClient client = null;
		try {
			client = FACTORY.open(ClientHandler.SYNC);
			client.setReadTimeout(getReadTimeout());
			client.request(m_ServiceUrl, HttpConstants.METHOD_POST);
			OutputStream out = client.openRequestWriter();
			m_Producer.make(request, new SimpleProducerOutput(client, out));
			out.close();
			int responseCode = client.getResponseCode();
			if (200 != responseCode) {
//				int expect = NumberUtil.toInt(client.getResponseHeaders().get(HttpConstants.CONTENT_LENGTH), 0);
//				int limit = 1024;
//				String msg = CachedInputStream.readString(client.getResponseStream(), expect, limit, m_Charset);
//				throw new HttpTransportException(responseCode, msg);
				throw new ServiceInvokeException("响应异常:" + responseCode);
			}
			String service = request.getHeader().getService();
			InputStream in = client.getResponseStream();
			Response res = m_Producer.fetchResponse(new SimpleProducerInput(client.getResponseHeaders(), in, service));
			in.close();
			return res;
		} catch (AuthException e) {
			throw new ServiceInvokeException("验证异常", e);
		} catch (SerialException e) {
			throw new ServiceInvokeException("序列化异常", e);
		} catch (IOException e) {
			throw new ServiceInvokeException("IO异常", e);
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}

}
