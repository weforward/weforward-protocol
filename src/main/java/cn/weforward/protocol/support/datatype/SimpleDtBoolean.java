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
import cn.weforward.protocol.datatype.DtBoolean;

/**
 * 布尔值的简单实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleDtBoolean implements DtBoolean {

	public static final SimpleDtBoolean TRUE = new SimpleDtBoolean(true);
	public static final SimpleDtBoolean FALSE = new SimpleDtBoolean(false);
	
	protected final boolean m_Value;

	public SimpleDtBoolean(boolean value) {
		m_Value = value;
	}

	public static SimpleDtBoolean valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}

	public static SimpleDtBoolean valueOf(Boolean value) {
		return Boolean.TRUE.equals(value) ? TRUE : FALSE;
	}

	public static SimpleDtBoolean valueOf(String str) {
		return valueOf(Boolean.valueOf(str));
	}

	@Override
	public DataType type() {
		return DataType.BOOLEAN;
	}

	@Override
	public boolean value() {
		return m_Value;
	}

	@Override
	public String toString() {
		return type().toString() + ' ' + m_Value;
	}
}
