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
package cn.weforward.protocol.client.ext;

import cn.weforward.protocol.StatusCode;

/**
 * 微服务返回结果的封装
 * 
 * @author zhangpengji
 *
 */
public class ResponseResultObject {

	static final ResponseResultObject SUCCESS_EMPTY = new ResponseResultObject(0, null, null);

	public final int code;
	public final String msg;
	public final Object content;

	public ResponseResultObject(int code, String msg, Object content) {
		this.code = code;
		this.msg = msg;
		this.content = content;
	}

	public static ResponseResultObject error(StatusCode code) {
		return error(code.code, code.msg);
	}

	public static ResponseResultObject error(StatusCode code, String msg) {
		return error(code.code, msg);
	}

	public static ResponseResultObject error(int code, String msg) {
		return new ResponseResultObject(code, msg, null);
	}

	public static ResponseResultObject success(Object content) {
		if (null == content) {
			return SUCCESS_EMPTY;
		}
		return new ResponseResultObject(0, null, content);
	}
}
