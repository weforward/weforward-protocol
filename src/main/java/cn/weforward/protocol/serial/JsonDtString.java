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
package cn.weforward.protocol.serial;

import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtString;

/**
 * Json格式的字符串
 * 
 * @author zhangpengji
 *
 */
public class JsonDtString implements DtString {

	String m_Str;
	int m_Begin;
	int m_End;

	public JsonDtString(String json, int begin, int end) {
		m_Str = json;
		m_Begin = begin;
		m_End = end;
	}

	@Override
	public DataType type() {
		return DataType.STRING;
	}

	@Override
	public String value() {
		return m_Str.substring(m_Begin, m_End);
	}

	@Override
	public String toString() {
		return type().toString() + ' ' + value();
	}
}
