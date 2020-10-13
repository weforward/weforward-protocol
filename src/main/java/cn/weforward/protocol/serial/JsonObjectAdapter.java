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

import java.text.ParseException;
import java.util.Date;

import cn.weforward.common.json.JsonObject;
import cn.weforward.common.json.JsonPair;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtDate;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.support.datatype.SimpleDtBoolean;
import cn.weforward.protocol.support.datatype.SimpleDtDate;
import cn.weforward.protocol.support.datatype.SimpleDtNumber;
import cn.weforward.protocol.support.datatype.SimpleDtObject;
import cn.weforward.protocol.support.datatype.SimpleDtString;

/**
 * <code>JsonObject</code>适配器
 * 
 * @author zhangpengji
 *
 */
class JsonObjectAdapter implements JsonObject.Appendable {

	SimpleDtObject m_Object;

	JsonObjectAdapter() {
		this(new SimpleDtObject(false));
	}

	JsonObjectAdapter(SimpleDtObject obj) {
		m_Object = obj;
	}

	static final JsonObjectAdapter EMPTY = new JsonObjectAdapter(SimpleDtObject.empty());

	@Override
	public JsonPair property(String name) {
		JsonPair pair = new JsonPair(name, m_Object.getAttribute(name));
		return pair;
	}

	@Override
	public int size() {
		return m_Object.getAttributeSize();
	}

	@Override
	public Iterable<JsonPair> items() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(String name, Object value) {
		m_Object.put(name, toDtBase(value));
	}

	static DtBase toDtBase(Object value) {
		if (null == value) {
			return null;
		}
		if (value instanceof Number) {
			return SimpleDtNumber.valueOf((Number) value);
		}
		if (value instanceof String) {
			// 先尝试转为date
			try {
				Date date = DtDate.Formater.parse((String) value);
				if (null != date) {
					return SimpleDtDate.valueOf(date);
				}
			} catch (ParseException e) {
			}
			return SimpleDtString.valueOf((String) value);
		}
		if (value instanceof Boolean) {
			return SimpleDtBoolean.valueOf((Boolean) value);
		}
		if (value instanceof JsonObjectAdapter) {
			return ((JsonObjectAdapter) value).getDtObject();
		}
		if (value instanceof JsonArrayAdapter) {
			return ((JsonArrayAdapter) value).getDtList();
		}
		throw new IllegalArgumentException("不支持此类型：" + value);
	}

	DtObject getDtObject() {
		return m_Object;
	}
}
