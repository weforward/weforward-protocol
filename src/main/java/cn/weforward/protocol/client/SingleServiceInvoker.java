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
package cn.weforward.protocol.client;

import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.execption.ServiceInvokeException;
import cn.weforward.protocol.ext.Producer;

/**
 * 单一链接的服务调用器
 * 
 * @author zhangpengji
 *
 */
public class SingleServiceInvoker extends AbstractServiceInvoker {

	protected String m_ServiceName;
	protected Transport m_Transport;

	protected String m_Charset = Header.CHARSET_UTF8;
	protected String m_ContentType = Header.CONTENT_TYPE_JSON;
	protected String m_AuthType = Header.AUTH_TYPE_SHA2;

	protected String m_AccessId;
	protected Producer m_Producer;

	public SingleServiceInvoker(String preUrl, String serviceName, Producer producer) {
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
		HttpTransport transport = new HttpTransport(url);
		m_Transport = transport;
		m_ServiceName = serviceName;
		m_Producer = producer;
	}

	public String getServiceName() {
		return m_ServiceName;
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

	public String getAccessId() {
		return m_AccessId;
	}

	public void setAccessId(String id) {
		m_AccessId = id;
	}

	public void setProducer(Producer producer) {
		m_Producer = producer;
	}

	public String getCharset() {
		return m_Charset;
	}

	@Override
	public int getConnectTimeout() {
		return m_Transport.getConnectTimeout();
	}

	@Override
	public void setConnectTimeout(int ms) {
		m_Transport.setConnectTimeout(ms);
	}

	@Override
	public int getReadTimeout() {
		return m_Transport.getReadTimeout();
	}

	@Override
	public void setReadTimeout(int ms) {
		m_Transport.setReadTimeout(ms);
	}

	@Override
	public Response invoke(Request request) throws ServiceInvokeException {
		try {
			return m_Transport.rpc(request, m_Producer);
		} catch (Exception e) {
			throw new ServiceInvokeException(e);
		}
	}

	@Override
	public String toString() {
		return "{tp:" + m_Transport + ",acc:" + m_AccessId + ",cs:" + m_Charset + ",ct:" + m_ContentType + ",at:"
				+ m_AuthType + "}";
	}
}
