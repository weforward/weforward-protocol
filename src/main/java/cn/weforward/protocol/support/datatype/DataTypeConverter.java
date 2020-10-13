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
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtBoolean;
import cn.weforward.protocol.datatype.DtDate;
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.exception.DataTypeCastExecption;

/**
 * 类型转换
 * 
 * @author zhangpengji
 *
 */
public class DataTypeConverter {

	@SuppressWarnings("unchecked")
	public static <E extends DtBase> E convert(DtBase src, DataType type) {
		if (null == src) {
			return null;
		}
		if (null == type) {
			return (E) src;
		}
		if (src.type() == type) {
			return (E) src;
		}
		// 字串转基础类型
		if (DataType.STRING == src.type()) {
			DtString str = (DtString) src;
			if (DataType.DATE == type) {
				return (E) SimpleDtDate.valueOf(str.value());
			}
			if (DataType.NUMBER == type) {
				return (E) SimpleDtNumber.valueOf(str.value());
			}
			if (DataType.BOOLEAN == type) {
				return (E) SimpleDtBoolean.valueOf(str.value());
			}
		}
		// 基础类型转字串
		if (DataType.STRING == type) {
			String str = null;
			if (DataType.DATE == src.type()) {
				DtDate date = (DtDate) src;
				str = date.value();
			}
			if (DataType.BOOLEAN == src.type()) {
				DtBoolean bool = (DtBoolean) src;
				str = Boolean.toString(bool.value());
			}
			if (DataType.NUMBER == src.type()) {
				DtNumber num = (DtNumber) src;
				str = num.valueNumber().toString();
			}
			if (null != str) {
				return (E) SimpleDtString.valueOf(str);
			}
		}
		throw new DataTypeCastExecption("不支持的类型转换：" + src.type() + " -> " + type);
	}
}
