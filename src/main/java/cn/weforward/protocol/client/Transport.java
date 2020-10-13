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

import java.io.IOException;

import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.exception.AuthException;
import cn.weforward.protocol.exception.SerialException;
import cn.weforward.protocol.ext.Producer;

/**
 * 调用传送器
 * 
 * @author zhangpengji
 * 
 */
public interface Transport {

	/**
	 * 连接超时值
	 * 
	 * @param timeout
	 *            超时值（毫秒）
	 */
	void setConnectTimeout(int timeout);

	/**
	 * 连接超时值（毫秒）
	 */
	int getConnectTimeout();

	/**
	 * 读/等待结果超时值
	 * 
	 * @param timeout
	 *            超时值（毫秒）
	 */
	void setReadTimeout(int timeout);

	/**
	 * 读/等待结果超时值（毫秒）
	 */
	int getReadTimeout();

	/**
	 * 远程过程调用
	 * 
	 * @param request
	 *            请求
	 * @param producer
	 *            数据制作器
	 */
	Response rpc(Request request, Producer producer) throws IOException, AuthException, SerialException;

}
