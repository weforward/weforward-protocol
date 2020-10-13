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

import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.exception.DataTypeCastExecption;

/**
 * 数字的简单实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleDtNumber implements DtNumber {

	public static final SimpleDtNumber ZERO = new SimpleDtNumber(0);
	public static final SimpleDtNumber ONE = new SimpleDtNumber(1);
	public static final SimpleDtNumber TWO = new SimpleDtNumber(2);

	protected static final int MARK_INT = 1;
	protected static final int MARK_LONG = 2;
	protected static final int MARK_DOUBLE = 4;

	protected final int m_IntValue;
	protected final long m_LongValue;
	protected final double m_DoubleValue;
	protected final int m_Marks;

	public SimpleDtNumber(int value) {
		m_IntValue = value;
		m_LongValue = 0;
		m_DoubleValue = 0;
		m_Marks = MARK_INT;
	}

	public SimpleDtNumber(long value) {
		m_IntValue = 0;
		m_LongValue = value;
		m_DoubleValue = 0;
		m_Marks = MARK_LONG;
	}

	public SimpleDtNumber(double value) {
		m_IntValue = 0;
		m_LongValue = 0;
		m_DoubleValue = value;
		m_Marks = MARK_DOUBLE;
	}

	public static SimpleDtNumber valueOf(int value) {
		if (0 == value) {
			return ZERO;
		}
		if (1 == value) {
			return ONE;
		}
		if (2 == value) {
			return TWO;
		}
		return new SimpleDtNumber(value);
	}

	public static SimpleDtNumber valueOf(long value) {
		if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
			return valueOf((int) value);
		}
		return new SimpleDtNumber(value);
	}

	public static SimpleDtNumber valueOf(double value) {
		if (0 == value) {
			return ZERO;
		}
		if (1 == value) {
			return ONE;
		}
		if (2 == value) {
			return TWO;
		}
		return new SimpleDtNumber(value);
	}

	public static SimpleDtNumber valueOf(Number num) {
		if (Double.class.isInstance(num) || Float.class.isInstance(num)) {
			return valueOf(num.doubleValue());
		}
		if (Long.class.isInstance(num)) {
			return valueOf(num.longValue());
		}
		return valueOf(num.intValue());
	}

	public static SimpleDtNumber valueOf(String str) {
		if ("0".equals(str)) {
			return ZERO;
		}
		if ("1".equals(str)) {
			return ONE;
		}
		if ("2".equals(str)) {
			return TWO;
		}
		Number num;
		try {
			if (-1 != str.indexOf('.')) {
				num = Double.valueOf(str);
			} else {
				num = Long.valueOf(str);
			}
		} catch (NumberFormatException e) {
			throw new DataTypeCastExecption(
					"转为[" + DataType.NUMBER + "]类型失败，value:" + StringUtil.limit(str, 100));
		}
		return valueOf(num);
	}

	@Override
	public DataType type() {
		return DataType.NUMBER;
	}

	@Override
	public int valueInt() {
		if (isInt()) {
			return m_IntValue;
		}
		if (isLong()) {
			return (int) m_LongValue;
		}
		return (int) m_DoubleValue;
	}

	@Override
	public long valueLong() {
		if (isInt()) {
			return m_IntValue;
		}
		if (isLong()) {
			return m_LongValue;
		}
		return (long) m_DoubleValue;
	}

	@Override
	public double valueDouble() {
		if (isInt()) {
			return m_IntValue;
		}
		if (isLong()) {
			return m_LongValue;
		}
		return m_DoubleValue;
	}

	@Override
	public Number valueNumber() {
		if (isInt()) {
			return m_IntValue;
		}
		if (isLong()) {
			return m_LongValue;
		}
		return m_DoubleValue;
	}

	protected boolean isMark(int mark) {
		return mark == (mark & m_Marks);
	}

	public static int getInt(DtNumber number, int defaultValue) {
		return null == number ? defaultValue : number.valueInt();
	}

	public static long getLong(DtNumber number, long defaultValue) {
		return null == number ? defaultValue : number.valueLong();
	}

	public static double getInt(DtNumber number, double defaultValue) {
		return null == number ? defaultValue : number.valueDouble();
	}

	@Override
	public boolean isInt() {
		return isMark(MARK_INT);
	}

	@Override
	public boolean isLong() {
		return isMark(MARK_LONG);
	}

	@Override
	public boolean isDouble() {
		return isMark(MARK_DOUBLE);
	}

	@Override
	public String toString() {
		StringBuilder sb = StringBuilderPool._128.poll();
		try {
			sb.append(type().toString()).append(' ');
			if (isInt()) {
				sb.append("int ").append(valueInt());
			} else if (isLong()) {
				sb.append("long ").append(valueLong());
			} else {
				sb.append("double ").append(valueDouble());
			}
			return sb.toString();
		} finally {
			StringBuilderPool._128.offer(sb);
		}
	}
}
