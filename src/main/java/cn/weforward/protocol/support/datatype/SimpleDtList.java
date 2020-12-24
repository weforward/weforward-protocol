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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import cn.weforward.common.util.ListUtil;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.ext.ObjectMapper;

/**
 * 列表的简单实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleDtList extends AbstractDtList implements DtList, Iterable<DtBase> {

	protected final List<DtBase> m_Items;

	public SimpleDtList() {
		this(10);
	}

	public SimpleDtList(int initialCapacity) {
		this(new ArrayList<DtBase>(initialCapacity));
	}

	protected SimpleDtList(List<DtBase> list) {
		// m_Value = new ArrayList<>(list);
		m_Items = list;
	}

	public static SimpleDtList valueOf(List<DtBase> list) {
		if (ListUtil.isEmpty(list)) {
			return EMPTY;
		}
		return new SimpleDtList(new ArrayList<>(list));
	}

	public static SimpleDtList stringOf(Iterable<String> it) {
		int size = getSize(it);
		if (0 == size) {
			return EMPTY;
		}
		Iterator<String> iterator = it.iterator();
		SimpleDtList dtList = new SimpleDtList(size);
		while (iterator.hasNext()) {
			dtList.add(iterator.next());
		}
		return dtList;
	}

	static int getSize(Iterable<?> it) {
		if(null == it) {
			return 0;
		}
		if (it instanceof Collection) {
			return ((Collection<?>) it).size();
		}
		if (!it.iterator().hasNext()) {
			return 0;
		}
		return 10;
	}

	public static SimpleDtList dateOf(Iterable<Date> it) {
		int size = getSize(it);
		if (0 == size) {
			return EMPTY;
		}
		Iterator<Date> iterator = it.iterator();
		SimpleDtList dtList = new SimpleDtList(size);
		while (iterator.hasNext()) {
			dtList.add(iterator.next());
		}
		return dtList;
	}

	public static SimpleDtList numberOf(Iterable<? extends Number> it) {
		int size = getSize(it);
		if (0 == size) {
			return EMPTY;
		}
		Iterator<? extends Number> iterator = it.iterator();
		SimpleDtList dtList = new SimpleDtList(size);
		while (iterator.hasNext()) {
			dtList.add(iterator.next());
		}
		return dtList;
	}

	public static SimpleDtList booleanOf(Iterable<Boolean> it) {
		int size = getSize(it);
		if (0 == size) {
			return EMPTY;
		}
		Iterator<Boolean> iterator = it.iterator();
		SimpleDtList dtList = new SimpleDtList(size);
		while (iterator.hasNext()) {
			dtList.add(iterator.next());
		}
		return dtList;
	}

	static final SimpleDtList EMPTY = new SimpleDtList(Collections.<DtBase>emptyList());

	public static SimpleDtList empty() {
		return EMPTY;
	}

	/**
	 * 转为DtList
	 * 
	 * @param <E>
	 * @param list
	 *            集合
	 * @param size
	 *            集合个数，-1表示未知
	 * @param mapper
	 *            集合元素的映射器
	 * @return 转换后的DtList
	 */
	public static <E> DtList toDtList(Iterable<? extends E> list, int size, ObjectMapper<E> mapper) {
		if (null == list) {
			return null;
		}
		if (0 == size) {
			return DtList.EMPTY;
		}
		SimpleDtList sl = new SimpleDtList(size < 0 ? 10 : size);
		for (E obj : list) {
			sl.addItem(mapper.toDtObject(obj));
		}
		if (0 == sl.size()) {
			return DtList.EMPTY;
		}
		return sl;
	}

	public static <E> DtList toDtList(List<? extends E> list, ObjectMapper<E> mapper) {
		if (null == list) {
			return null;
		}
		if (list.isEmpty()) {
			return SimpleDtList.EMPTY;
		}
		return toDtList(list, list.size(), mapper);
	}

	public static <E> List<E> fromDtList(DtList list, ObjectMapper<E> mapper) {
		if (null == list) {
			return null;
		}
		if (0 == list.size()) {
			return Collections.emptyList();
		}
		List<E> result = new ArrayList<>(list.size());
		Enumeration<DtBase> objs = list.items();
		while (objs.hasMoreElements()) {
			DtBase obj = objs.nextElement();
			result.add(mapper.fromDtObject((DtObject) obj));
		}
		return result;
	}

	@Override
	public int size() {
		return m_Items.size();
	}

	@Override
	protected DtBase getItemInner(int index) {
		return m_Items.get(index);
	}

	@Override
	public Iterator<DtBase> iterator() {
		return m_Items.iterator();
	}

	@Override
	public Enumeration<DtBase> items() {
		return Collections.enumeration(m_Items);
	}

	public void addItem(DtBase item) {
		m_Items.add(item);
	}

	public void add(int value) {
		addItem(SimpleDtNumber.valueOf(value));
	}

	public void add(long value) {
		addItem(SimpleDtNumber.valueOf(value));
	}

	public void add(double value) {
		addItem(SimpleDtNumber.valueOf(value));
	}

	public void add(Number value) {
		addItem(SimpleDtNumber.valueOf(value));
	}

	public void add(String value) {
		addItem(SimpleDtString.valueOf(value));
	}

	public void add(Date value) {
		addItem(SimpleDtDate.valueOf(value));
	}

	public void add(boolean value) {
		addItem(SimpleDtBoolean.valueOf(value));
	}

	public DtBase removeItem(int index) {
		return m_Items.remove(index);
	}

	@Override
	public String toString() {
		return type().toString() + ' ' + m_Items;
	}

	// @Override
	// public DtNumber getNumber(int index) {
	// return (DtNumber) get(index);
	// }

	// @Override
	// public DtString getString(int index) {
	// return (DtString) get(index);
	// }
	//
	// @Override
	// public DtBoolean getBoolean(int index) {
	// return (DtBoolean) get(index);
	// }
	//
	// @Override
	// public DtDate getDate(int index) {
	// return (DtDate) get(index);
	// }
	//
	// @Override
	// public DtList getList(int index) {
	// return (DtList) get(index);
	// }
	//
	// @Override
	// public DtObject getObject(int index) {
	// return (DtObject) get(index);
	// }

}
