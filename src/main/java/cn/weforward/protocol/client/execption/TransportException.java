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

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 网络传输异常
 * 
 * @author zhangpengji
 *
 */
public class TransportException extends IOException {
	private static final long serialVersionUID = 1L;

	protected int m_Type;

	/** 类型 - 未定义 */
	public static final int TYPE_ERROR_UNDEFINED = 0;
	/** 类型 - 状态码异常 */
	public static final int TYPE_ERROR_STATUS_ERROR = 1;
	/** 类型 - 读取超时 */
	public static final int TYPE_ERROR_READ_TIMEOUT = 2;
	/** 类型 - 连接超时 */
	public static final int TYPE_ERROR_CONNECT_TIMEOUT = 4;
	/** 类型 - 连接异常 */
	public static final int TYPE_ERROR_CONNECT_ERROR = 8;
	/** 类型 - 地址解析失败 */
	public static final int TYPE_ERROR_UNKNOWN_HOST = 0x10;

	public TransportException(String message) {
		super(message);
	}

	public TransportException(IOException cause) {
		super(cause);
	}

	public TransportException(int type, String message, IOException cause) {
		super(message, cause);
		m_Type = type;
	}

	public static TransportException valueOf(IOException cause, String url) {
		int type;
		String message;
		if (cause instanceof SocketTimeoutException) {
			String str = cause.getMessage();
			if (null != str && str.contains("Read timed out")) {
				type = TYPE_ERROR_READ_TIMEOUT;
				message = "读取超时,url:" + url;
			} else {
				type = TYPE_ERROR_CONNECT_TIMEOUT;
				message = "连接超时,url:" + url;
			}
			// message = "socket超时:" + str;
		} else if (cause instanceof ConnectException) {
			type = TYPE_ERROR_CONNECT_ERROR;
			message = "连接异常:" + cause.getMessage() + " ,url:" + url;
		} else if (cause instanceof UnknownHostException) {
			type = TYPE_ERROR_UNKNOWN_HOST;
			message = "解析ip地址失败:" + cause.getMessage();
		} else {
			type = TYPE_ERROR_UNDEFINED;
			message = cause.toString();
		}
		return new TransportException(type, message, cause);
	}

	public int getType() {
		return m_Type;
	}
	
	public boolean isType(int type) {
		return m_Type == type;
	}
}
