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

import java.util.List;

import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.execption.HttpTransportException;
import cn.weforward.protocol.client.execption.ServiceInvokeException;

/**
 * 默认的服务调用器实现。<br/>
 * 集成多个Invoker，自动选择最优的
 * 
 * @author zhangpengji
 *
 */
public class DefaultServiceInvoker extends AbstractServiceInvoker {

	protected List<AbstractServiceInvoker> m_Invokers;
	private volatile int m_InvokerOffset;

	/**
	 * 根据http url构造
	 * 
	 * @param preUrls
	 * @param serviceName
	 */
	DefaultServiceInvoker(List<AbstractServiceInvoker> invvokers) {
		m_Invokers = invvokers;
		m_InvokerOffset = 0;
	}

	public String getAccessId() {
		return m_Invokers.get(0).getAccessId();
	}

	public void setAccessId(String id) {
		for (AbstractServiceInvoker invoker : m_Invokers) {
			invoker.setAccessId(id);
		}
	}

	public String getServiceName() {
		return m_Invokers.get(0).getServiceName();
	}

	@Override
	public String getContentType() {
		return m_Invokers.get(0).getContentType();
	}

	@Override
	public void setContentType(String type) {
		for (AbstractServiceInvoker invoker : m_Invokers) {
			invoker.setContentType(type);
		}
	}

	@Override
	public String getAuthType() {
		return m_Invokers.get(0).getAuthType();
	}

	@Override
	public void setAuthType(String type) {
		for (AbstractServiceInvoker invoker : m_Invokers) {
			invoker.setAuthType(type);
		}
	}

	public String getCharset() {
		return m_Invokers.get(0).getCharset();
	}

	@Override
	public int getConnectTimeout() {
		return m_Invokers.get(0).getConnectTimeout();
	}

	@Override
	public void setConnectTimeout(int timeout) {
		for (AbstractServiceInvoker invoker : m_Invokers) {
			invoker.setConnectTimeout(timeout);
		}
	}

	@Override
	public int getReadTimeout() {
		return m_Invokers.get(0).getReadTimeout();
	}

	@Override
	public void setReadTimeout(int timeout) {
		for (AbstractServiceInvoker invoker : m_Invokers) {
			invoker.setReadTimeout(timeout);
		}
	}

	@Override
	public Response invoke(Request request) throws ServiceInvokeException {
		// FIXME 暂时先简单轮询一遍
		ServiceInvokeException err = null;
		int size = m_Invokers.size();
		int offset = m_InvokerOffset;
		for (int i = 0; i < size; i++) {
			int idx = i + offset;
			idx = (idx < size ? idx : (idx - size));
			ServiceInvoker invoker = m_Invokers.get(idx);
			try {
				Response resp = invoker.invoke(request);
				m_InvokerOffset = idx;
				return resp;
			} catch (ServiceInvokeException e) {
				err = e;
				Throwable cause = e.getCause();
				if (cause instanceof HttpTransportException
						&& ((HttpTransportException) cause).isType(HttpTransportException.TYPE_ERROR_READ_TIMEOUT)) {
					break;
				}
			}
		}
		throw err;
	}

	@Override
	public String toString() {
		return "{offset:" + m_InvokerOffset + " ,invokers:" + m_Invokers + "}";
	}
}
