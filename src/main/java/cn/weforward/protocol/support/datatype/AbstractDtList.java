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

import java.util.Enumeration;

import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtList;

/**
 * 抽象的DtList
 * 
 * @author zhangpengji
 *
 */
public abstract class AbstractDtList implements DtList {

	@Override
	public DataType type() {
		return DataType.LIST;
	}

	@Override
	public DtBase getItem(int index) {
		return getItem(index, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends DtBase> E getItem(int index, DataType refType) {
		DtBase item = getItemInner(index);
		if (null == refType) {
			return (E) item;
		}
		return DataTypeConverter.convert(item, refType);
	}

	protected abstract DtBase getItemInner(int index);

	@Override
	public Enumeration<DtBase> items() {
		final int size = size();
		return new Enumeration<DtBase>() {
			int idx = 0;

			@Override
			public boolean hasMoreElements() {
				return idx < size;
			}

			@Override
			public DtBase nextElement() {
				return getItemInner(idx++);
			}

		};
	}

}
