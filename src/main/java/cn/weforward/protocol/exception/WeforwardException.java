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
package cn.weforward.protocol.exception;

/**
 * 异常基类
 * 
 * @author zhangpengji
 *
 */
public class WeforwardException extends Exception {
	private static final long serialVersionUID = 1L;

	/** 错误码 - Access Id 无效 */
	public static final int CODE_ACCESS_ID_INVALID = 1001;
	/** 错误码 - 验证失败 */
	public static final int CODE_AUTH_FAIL = 1002;
	/** 错误码 - 验证类型无效 */
	public static final int CODE_AUTH_TYPE_INVALID = 1003;
	/** 错误码 - 序列化/反序列化异常（内容格式解析异常） */
	public static final int CODE_SERIAL_ERROR = 1101;
	/** 错误码 - 无效请求内容 */
	public static final int CODE_ILLEGAL_CONTENT = 1102;
	/** 错误码 - 拒绝调用 */
	public static final int CODE_INVOKE_DENIED = 1501;

	/** 错误码 - 网关API不存在 */
	public static final int CODE_API_NOT_FOUND = 2000;
	/** 错误码 - （网关与调用端之间的）网络异常 */
	public static final int CODE_NETWORK_ERROR = 2001;
	/** 错误码 - 网关忙 */
	public static final int CODE_GATEWAY_BUSY = 2002;

	/** 错误码 - 微服务不存在 */
	public static final int CODE_SERVICE_NOT_FOUND = 5001;
	/** 错误码 - 微服务调用异常 */
	public static final int CODE_SERVICE_INVOKE_ERROR = 5002;
	/** 错误码 - 微服务忙 */
	public static final int CODE_SERVICE_BUSY = 5003;
	/** 错误码 - 微服务不可用 */
	public static final int CODE_SERVICE_UNAVAILABLE = 5004;
	/** 错误码 - 微服务响应超时（已收到请求，但未在限制时间内返回） */
	public static final int CODE_SERVICE_TIMEOUT = 5005;
	/** 错误码 - 微服务请求转发 */
	public static final int CODE_SERVICE_FORWARD = 5006;
	/** 错误码 - 微服务调用栈过深 */
	public static final int CODE_SERVICE_TOO_DEPTH = 5007;
	/** 错误码 - 微服务连接失败 */
	public static final int CODE_SERVICE_CONNECT_FAIL = 5008;

	/** 错误码 - 内部错误 */
	public static final int CODE_INTERNAL_ERROR = 9001;
	// /** 错误码 - 未预料（运行时）错误 */
	// public static final int CODE_UNPREDICTED_ERROR = 9002;
	/** 错误码 - 未就绪 */
	public static final int CODE_UNREADY = 9003;
	/** 错误码 - 未知异常 */
	public static final int CODE_UNDEFINED = 9999;

	protected int m_Code;

	public WeforwardException(int code) {
		super();
		m_Code = code;
	}

	public WeforwardException(int code, String message, Throwable cause) {
		super(message, cause);
		m_Code = code;
	}

	public WeforwardException(int code, String message) {
		super(message);
		m_Code = code;
	}

	public WeforwardException(int code, Throwable cause) {
		super(cause);
		m_Code = code;
	}

	protected WeforwardException(int code, String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		m_Code = code;
	}

	public int getCode() {
		return m_Code;
	}

	public static WeforwardException undefined(String msg) {
		return new WeforwardException(CODE_UNDEFINED, msg);
	}

}
