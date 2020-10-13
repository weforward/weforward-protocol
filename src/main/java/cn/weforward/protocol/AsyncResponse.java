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
package cn.weforward.protocol;

import java.io.IOException;

import cn.weforward.protocol.datatype.DtBase;

/**
 * 请求异步响应
 * 
 * @author liangyi
 *
 */
public interface AsyncResponse extends Response {
	/**
	 * 置入微服务的返回结果
	 * 
	 * @param code
	 *            结果码
	 * @param message
	 *            结果码对应的描述
	 * @param content
	 *            内容
	 */
	void setServiceResult(int code, String message, DtBase content);

	/**
	 * 启用异步响应
	 */
	public void setAsync() throws IOException;

	/**
	 * 设置响应超时值
	 * 
	 * @param millis
	 *            超时值（毫秒），若=Integer.MAX_VALUE则使用空闲超时值
	 */
	public void setResponseTimeout(int millis) throws IOException;

	/**
	 * 响应超时值
	 */
	public int getResponseTimeout();

	/**
	 * 完成响应
	 */
	void complete() throws IOException;

	// /**
	// * 取消响应
	// */
	// void cancel();
}