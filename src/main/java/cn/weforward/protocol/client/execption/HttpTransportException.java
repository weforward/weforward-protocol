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

/**
 * HTTP传输异常
 * 
 * @author zhangpengji
 *
 */
public class HttpTransportException extends TransportException {
	private static final long serialVersionUID = 1L;

	protected int m_HttpCode;
	protected String m_HttpMessage;

	public HttpTransportException(int statusCode, String message) {
		super(statusCode + "/" + message);
		m_Type = TYPE_ERROR_STATUS_ERROR;
		m_HttpCode = statusCode;
		m_HttpMessage = message;
	}

	public int getHttpCode() {
		return m_HttpCode;
	}

	public String getHttpMessage() {
		return m_HttpMessage;
	}
}
