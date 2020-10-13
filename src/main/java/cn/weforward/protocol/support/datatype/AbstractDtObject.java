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

import java.util.Collections;
import java.util.Enumeration;

import cn.weforward.common.KvPair;
import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtBoolean;
import cn.weforward.protocol.datatype.DtDate;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;

/**
 * DtObject抽象实现
 * 
 * @author zhangpengji
 *
 */
public abstract class AbstractDtObject implements DtObject {

	protected AbstractDtObject() {

	}

	@Override
	public DataType type() {
		return DataType.OBJECT;
	}

	@Override
	public <E extends DtBase> E getAttribute(String name) {
		return getAttribute(name, null);
	}

	@SuppressWarnings("unchecked")
	public <E extends DtBase> E getAttribute(String name, DataType refType) {
		DtBase att = getAttributeInner(name);
		if (null == refType) {
			return (E) att;
		}
		return DataTypeConverter.convert(att, refType);
	}

	protected abstract DtBase getAttributeInner(String name);

	@Override
	public Enumeration<KvPair<String, DtBase>> getAttributes() {
		final Enumeration<String> names = getAttributeNames();
		if (!names.hasMoreElements()) {
			return Collections.emptyEnumeration();
		}
		return new Enumeration<KvPair<String, DtBase>>() {

			@Override
			public KvPair<String, DtBase> nextElement() {
				final String name = names.nextElement();
				return new KvPair<String, DtBase>() {

					@Override
					public String getKey() {
						return name;
					}

					@Override
					public DtBase getValue() {
						return getAttributeInner(name);
					}

				};
			}

			@Override
			public boolean hasMoreElements() {
				return names.hasMoreElements();
			}
		};
	}

	@Override
	public DtString getString(String name) {
		return getAttribute(name, DataType.STRING);
	}

	@Override
	public DtBoolean getBoolean(String name) {
		return getAttribute(name, DataType.BOOLEAN);
	}

	@Override
	public DtDate getDate(String name) {
		return getAttribute(name, DataType.DATE);
	}

	@Override
	public DtList getList(String name) {
		return getAttribute(name, DataType.LIST);
	}

	@Override
	public DtObject getObject(String name) {
		return getAttribute(name, DataType.OBJECT);
	}

	@Override
	public DtNumber getNumber(String name) {
		return getAttribute(name, DataType.NUMBER);
	}
}
