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
package cn.weforward.proxy;

import java.io.IOException;

import cn.weforward.common.Dictionary;

/**
 * proxy业务处理器
 *
 * 校验hy头->校验签名 -> 解析请求内容 -> 检查access权限 -> 调用微服务实例 -> 包装微服务的返回内容 -> 生成签名 -> 生成hy头
 * -> 返回响应
 *
 * @author liangyi
 *
 */
public interface ProxyHandler {
	/**
	 * 检查访问源
	 * 
	 * @param address
	 *            访问者地址，如“127.0.0.1:12345”
	 * @return
	 */
	boolean checkFrom(String address);

	/**
	 * 校验hy头，获取访问，同时可以考虑检查微服务的状态
	 * 
	 * @throws BusyException
	 *             服务忙、服务不可用、过载等
	 */
	void auth(Tunnel tunnel, String serviceName, Dictionary<String, String> headers)
			throws IOException;
}
