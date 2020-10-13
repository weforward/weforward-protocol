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

import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.execption.ServiceInvokeException;
import cn.weforward.protocol.datatype.DtObject;

/**
 * 服务调用器
 * 
 * @author zhangpengji
 *
 */
public interface ServiceInvoker {

	/**
	 * 获取内容格式
	 * 
	 * @return 内容格式
	 */
	String getContentType();

	/**
	 * 设置内容格式
	 * 
	 * @param type
	 */
	void setContentType(String type);

	/**
	 * 获取验证类型
	 * 
	 * @return 验证类型
	 */
	String getAuthType();

	/**
	 * 设置验证类型
	 * 
	 * @param type
	 */
	void setAuthType(String type);

	/**
	 * 获取连接超时时间
	 * 
	 * @return 连接超时时间
	 */
	int getConnectTimeout();

	/**
	 * 设置连接超时时间
	 * 
	 * @param ms 超时时间，单位：毫秒
	 */
	void setConnectTimeout(int ms);

	/**
	 * 获取读取超时时间
	 * 
	 * @return 读取超时时间
	 */
	int getReadTimeout();

	/**
	 * 设置读取超时时间
	 * 
	 * @param ms 超时时间，单位：毫秒
	 */
	void setReadTimeout(int ms);

	/**
	 * 调用
	 * 
	 * @param request
	 * @return 响应对象
	 * @throws ServiceInvokeException 当出现网络异常，或者浩宁云状态非成功时抛出
	 */
	Response invoke(Request request) throws ServiceInvokeException;

	/**
	 * 调用
	 * 
	 * @param invokeObj 包含method、params的调用信息
	 * @return 响应对象
	 */
	Response invoke(DtObject invokeObj) throws ServiceInvokeException;

	/**
	 * 调用
	 *
	 * @param method
	 * @return 响应对象
	 * @throws ServiceInvokeException
	 */
	Response invoke(String method) throws ServiceInvokeException;

	/**
	 * 调用
	 *
	 * @param method
	 * @param params
	 * @return 响应对象
	 * @throws ServiceInvokeException
	 */
	Response invoke(String method, DtObject params) throws ServiceInvokeException;

	/**
	 * 创建请求
	 * 
	 * @param invokeObj 包含method、params的调用信息
	 * @return 请求
	 */
	Request createRequest(DtObject invokeObj);

	/**
	 * 创建请求
	 *
	 * @param method
	 * @return 请求
	 */
	Request createRequest(String method);

	/**
	 * 创建请求
	 *
	 * @param method
	 * @param params
	 * @return 请求
	 */
	Request createRequest(String method, DtObject params);

}
