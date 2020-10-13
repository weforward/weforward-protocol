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
package cn.weforward.protocol.gateway.exception;

import cn.weforward.protocol.Response;

/**
 * 微服务注册Api的异常类
 * 
 * @author zhangpengji
 *
 */
public class ServiceRegisterException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/** 状态码 */
	protected int m_Code;
	/** 状态码描述 */
	protected String m_Msg;

	public ServiceRegisterException(Response resp) {
		this(resp.getResponseCode(), resp.getResponseMsg());
	}

	public ServiceRegisterException(int code, String msg) {
		super(code + "/" + msg);
		m_Code = code;
		m_Msg = msg;
	}

	public int getCode() {
		return m_Code;
	}

	public void setCode(int code) {
		m_Code = code;
	}

	public String getMsg() {
		return m_Msg;
	}

	public void setMsg(String msg) {
		m_Msg = msg;
	}
}
