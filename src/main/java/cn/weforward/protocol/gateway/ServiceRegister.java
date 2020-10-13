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
package cn.weforward.protocol.gateway;

import cn.weforward.protocol.Service;
import cn.weforward.protocol.ext.ServiceRuntime;
import cn.weforward.protocol.gateway.exception.ServiceRegisterException;

/**
 * 微服务注册接口
 * 
 * @author zhangpengji
 *
 */
public interface ServiceRegister {
	/**
	 * 注册一个微服务
	 * 
	 * @param service
	 *            微服务信息
	 */
	void registerService(Service service) throws ServiceRegisterException;

	/**
	 * 注册一个微服务
	 * 
	 * @param service
	 *            微服务信息
	 * @param runtime
	 */
	void registerService(Service service, ServiceRuntime runtime) throws ServiceRegisterException;

	/**
	 * 注销一个微服务
	 * 
	 * @param service
	 *            微服务信息
	 */
	void unregisterService(Service service) throws ServiceRegisterException;
}
