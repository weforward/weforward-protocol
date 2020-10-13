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

import java.util.Date;

import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.support.datatype.SimpleDtBoolean;
import cn.weforward.protocol.support.datatype.SimpleDtDate;
import cn.weforward.protocol.support.datatype.SimpleDtNumber;
import cn.weforward.protocol.support.datatype.SimpleDtString;

/**
 * 调用请求的invoke的参数封装
 * 
 * @author zhangpengji
 *
 */
public class RequestInvokeParam {

	/** 参数名 */
	public final String name;
	/** 参数值 */
	public final DtBase value;

	/**
	 * 构造调用参数
	 * 
	 * @param name
	 *            参数名
	 * @param value
	 *            参数值
	 */
	public RequestInvokeParam(String name, DtBase value) {
		this.name = name;
		this.value = value;
	}

	public static RequestInvokeParam valueOf(String name, boolean value) {
		return new RequestInvokeParam(name, SimpleDtBoolean.valueOf(value));
	}

	public static RequestInvokeParam valueOf(String name, short value) {
		return new RequestInvokeParam(name, SimpleDtNumber.valueOf(value));
	}

	public static RequestInvokeParam valueOf(String name, int value) {
		return new RequestInvokeParam(name, SimpleDtNumber.valueOf(value));
	}

	public static RequestInvokeParam valueOf(String name, long value) {
		return new RequestInvokeParam(name, SimpleDtNumber.valueOf(value));
	}

	public static RequestInvokeParam valueOf(String name, double value) {
		return new RequestInvokeParam(name, SimpleDtNumber.valueOf(value));
	}

	public static RequestInvokeParam valueOf(String name, String value) {
		return new RequestInvokeParam(name, SimpleDtString.valueOf(value));
	}

	public static RequestInvokeParam valueOf(String name, Date value) {
		return new RequestInvokeParam(name, SimpleDtDate.valueOf(value));
	}

	public static RequestInvokeParam valueOf(String name, DtBase value) {
		return new RequestInvokeParam(name, value);
	}

	@Override
	public String toString() {
		return "{name:\"" + name + "\",value:" + value + "}";
	}
}
