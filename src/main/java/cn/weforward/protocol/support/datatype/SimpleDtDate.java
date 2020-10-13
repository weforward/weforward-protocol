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

import java.text.ParseException;
import java.util.Date;

import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtDate;
import cn.weforward.protocol.exception.DataTypeCastExecption;

/**
 * 日期的简单实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleDtDate implements DtDate {

	protected Date m_DateValue;
	protected String m_StringValue;

	public SimpleDtDate(Date value) {
		m_DateValue = value;
	}

	public SimpleDtDate(String value) {
		m_StringValue = value;
		try {
			m_DateValue = Formater.parse(m_StringValue);
		} catch (ParseException e) {
			throw new DataTypeCastExecption("转为[" + type() + "]类型失败，value:" + StringUtil.limit(m_StringValue, 100), e);
		}
	}

	public static SimpleDtDate valueOf(Date value) {
		if (null == value) {
			return null;
		}
		return new SimpleDtDate(value);
	}

	public static SimpleDtDate valueOf(String value) {
		if (null == value) {
			return null;
		}
		return new SimpleDtDate(value);
	}

	@Override
	public DataType type() {
		return DataType.DATE;
	}

	@Override
	public String value() {
		if (null == m_StringValue) {
			m_StringValue = Formater.formatDateTime(m_DateValue);
		}
		return m_StringValue;
	}

	@Override
	public Date valueDate() {
		return m_DateValue;
	}

	@Override
	public String toString() {
		return type().toString() + ' ' + m_DateValue;
	}
}
