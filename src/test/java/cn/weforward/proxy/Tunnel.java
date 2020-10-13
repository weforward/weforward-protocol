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
import java.io.OutputStream;

/**
 * 转发通道
 * 
 * @author liangyi
 *
 */
public interface Tunnel {
	/**
	 * 允许访问
	 * 
	 * @param accessHandler
	 *            访问控制器
	 */
	void permit(AccessHandler accessHandler) throws IOException;

	/**
	 * 端点已就绪
	 * 
	 * @param endpoint
	 *            已就绪的端点
	 */
	void established(EndpointPipe endpoint) throws IOException;

	/**
	 * 打开响应输出
	 * 
	 * @param endpoint
	 *            要输出响应的端点
	 * @return 输出流
	 */
	OutputStream openResponse(EndpointPipe endpoint) throws IOException;

	/**
	 * 取消响应
	 */
	void cancelResponse() throws IOException;

	/**
	 * 响应错误信息并结束
	 * 
	 * @param msg
	 *            错误信息
	 * @throws IOException
	 */
	void responseError(byte[] msg) throws IOException;

}
