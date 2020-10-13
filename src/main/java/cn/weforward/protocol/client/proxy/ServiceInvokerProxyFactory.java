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
package cn.weforward.protocol.client.proxy;

import java.util.concurrent.ConcurrentHashMap;

import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.ext.ObjectMapperSet;

/**
 * 服务调用代理
 * 
 * @author daibo
 *
 */
public class ServiceInvokerProxyFactory {
	/** 网关异常装箱方法映射表 */
	private ConcurrentHashMap<NameAndGroup, ServiceInvokerProxy> m_Services = new ConcurrentHashMap<>();

	protected String m_PreUrl;
	protected String m_AccessId;
	protected String m_AccessKey;

	/**
	 * 构造
	 * 
	 * @param preUrl
	 * @param accessId
	 * @param accessKey
	 */
	public ServiceInvokerProxyFactory(String preUrl, String accessId, String accessKey) {
		m_PreUrl = preUrl;
		m_AccessId = accessId;
		m_AccessKey = accessKey;
	}

	/**
	 * 打开代理
	 * 
	 * @param serviceName 服务名
	 * @return 服务调用代理
	 */
	public ServiceInvokerProxy openProxy(String serviceName) {
		return openProxy(serviceName, "");
	}

	/**
	 * 打开代理
	 * 
	 * @param serviceName 服务名
	 * @param methodGroup 方法组
	 * @return 服务调用代理
	 */
	public ServiceInvokerProxy openProxy(String serviceName, String methodGroup) {
		return openProxy(serviceName, methodGroup, null);
	}

	/**
	 * 打开代理
	 * 
	 * @param serviceName 服务名
	 * @param methodGroup 方法组
	 * @param set         映射表
	 * @return 服务调用代理
	 */
	public ServiceInvokerProxy openProxy(String serviceName, String methodGroup, ObjectMapperSet set) {
		NameAndGroup key = new NameAndGroup(serviceName, methodGroup);
		ServiceInvokerProxy proxy = m_Services.get(key);
		if (null == proxy) {
			JdkServiceInvokerProxy jdk = new JdkServiceInvokerProxy(serviceName, m_PreUrl, m_AccessId, m_AccessKey);
			jdk.setMethodGroup(methodGroup);
			jdk.setMapperSet(set);
			ServiceInvokerProxy old = m_Services.putIfAbsent(key, jdk);
			if (null != old) {
				proxy = old;
			} else {
				proxy = jdk;
			}
		}
		return proxy;
	}

	/**
	 * 名称还方法组
	 * 
	 * @author daibo
	 *
	 */
	class NameAndGroup {

		public String serviceName;

		public String methodGroup;

		public NameAndGroup(String serviceName, String methodGroup) {
			this.serviceName = serviceName;
			this.methodGroup = StringUtil.toString(methodGroup);
		}

		public int hashCode() {
			return this.serviceName.hashCode() + 31 * this.methodGroup.hashCode();
		}

		@Override
		public boolean equals(Object v) {
			if (v instanceof NameAndGroup) {
				return StringUtil.eq(serviceName, ((NameAndGroup) v).serviceName)
						&& StringUtil.eq(methodGroup, ((NameAndGroup) v).methodGroup);
			}
			return false;
		}
	}
}
