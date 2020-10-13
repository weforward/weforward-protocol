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
package cn.weforward.protocol.datatype;

import java.util.Collections;
import java.util.Enumeration;

/**
 * 数据类型 - 列表
 * 
 * @author zhangpengji
 *
 */
public interface DtList extends DtBase {

	DtList EMPTY = new DtList() {

		@Override
		public DataType type() {
			return DataType.LIST;
		}

		// @Override
		// public Iterator<DtBase> iterator() {
		// return Collections.<DtBase>emptyIterator();
		// }

		@Override
		public DtBase getItem(int index) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		@Override
		public <E extends DtBase> E getItem(int index, DataType refType) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public Enumeration<DtBase> items() {
			return Collections.emptyEnumeration();
		}

		@Override
		public String toString() {
			return type().toString() + " []";
		}

	};

	// /**
	// * 列表元素集合
	// *
	// * @param <E>
	// * @return
	// */
	// List<E> getItems();

	// HyDtNumber getNumber(int index);
	//
	// HyDtString getString(int index);
	//
	// HyDtBoolean getBoolean(int index);
	//
	// HyDtDate getDate(int index);
	//
	// HyDtList getList(int index);
	//
	// HyDtObject getObject(int index);

	/**
	 * 获取指定下标的元素
	 * 
	 * @param index
	 * @return 元素
	 */
	DtBase getItem(int index);

	/**
	 * 获取指定下标的元素
	 * 
	 * @param <E>
	 * @param index   属性名
	 * @param refType 参考类型
	 * @return 元素
	 */
	<E extends DtBase> E getItem(int index, DataType refType);

	/**
	 * 列表大小
	 * 
	 * @return 大小
	 */
	int size();

	/**
	 * 获取全部元素
	 * 
	 * @return 全部元素
	 */
	Enumeration<DtBase> items();
}
