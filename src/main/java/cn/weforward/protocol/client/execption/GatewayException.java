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
package cn.weforward.protocol.client.execption;

import cn.weforward.protocol.Response;

/**
 * 浩宁云网关响应异常
 * 
 * 网关返回的错误
 * 
 * @author daibo
 *
 */
@SuppressWarnings("serial")
public class GatewayException extends RuntimeException {

	protected int m_ResponseCode;
	protected String m_ResponseMsg;

	public GatewayException(Response response) {
		this(response.getResponseCode(), response.getResponseMsg());
	}

	public GatewayException(int code, String msg) {
		super("网关响应异常:" + code + "/" + msg);
		m_ResponseCode = code;
		m_ResponseMsg = msg;
	}

	public GatewayException(String exceptionMessage, int code, String msg) {
		super(exceptionMessage);
		m_ResponseCode = code;
		m_ResponseMsg = msg;
	}

	public int getResponseCode() {
		return m_ResponseCode;
	}

	public String getResponseMsg() {
		return m_ResponseMsg;
	}

	/**
	 * 检查异常
	 * 
	 * @param response
	 * @throws GatewayException
	 */
	public static void checkException(Response response) throws GatewayException {
		if (response.getResponseCode() != 0) {
			throw new GatewayException(response);
		}
	}

}
