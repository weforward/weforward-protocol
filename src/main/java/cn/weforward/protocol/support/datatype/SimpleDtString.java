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
package cn.weforward.protocol.support.datatype;

import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtString;

/**
 * 字串的简单实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleDtString implements DtString {

	static final SimpleDtString NIL = new SimpleDtString(null);
	static final SimpleDtString EMPTY = new SimpleDtString("");

	protected final String m_Value;

	public SimpleDtString(String value) {
		m_Value = value;
	}

	public static SimpleDtString valueOf(String value) {
		if (null == value) {
			return NIL;
		}
		if ("".equals(value)) {
			return EMPTY;
		}
		return new SimpleDtString(value);
	}

	@Override
	public DataType type() {
		return DataType.STRING;
	}

	@Override
	public String value() {
		return m_Value;
	}

	public static String getString(DtString str) {
		return null == str ? null : str.value();
	}

	@Override
	public String toString() {
		return type().toString() + ' ' + m_Value;
	}
}
