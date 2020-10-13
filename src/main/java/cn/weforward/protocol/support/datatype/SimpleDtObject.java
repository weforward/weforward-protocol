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
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cn.weforward.common.KvPair;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.support.NamingConverter;

/**
 * 对象的简单实现。<br/>
 * 设置/获取属性时，自动将属性名称转换，以符合浩宁云命名规范
 * 
 * @author zhangpengji
 *
 */
public class SimpleDtObject extends AbstractDtObject implements Iterable<KvPair<String, DtBase>> {

	protected Map<String, DtBase> m_Map;
	// protected boolean m_AutoNaming;
	protected boolean m_SortAttribute;
	// 属性名比较器（用于排序）
	protected Comparator<String> m_Comparator;

	public SimpleDtObject() {
		this(true);
	}

	/**
	 * 构造
	 * 
	 * @param autoNaming
	 *            是否转换属性名称
	 */
	public SimpleDtObject(boolean autoNaming) {
		this((autoNaming ? new AutoNamingMap() : new HashMap<String, DtBase>()));
	}

	protected SimpleDtObject(Map<String, DtBase> map) {
		m_Map = map;
	}

	static final SimpleDtObject EMPTY = new SimpleDtObject(Collections.<String, DtBase>emptyMap());

	public static SimpleDtObject empty() {
		return EMPTY;
	}

	static class AutoNamingMap extends HashMap<String, DtBase> {
		private static final long serialVersionUID = 1L;

		@Override
		public DtBase put(String key, DtBase value) {
			return super.put(NamingConverter.camelToWf(key), value);
		}

		@Override
		public DtBase remove(Object key) {
			return super.remove(NamingConverter.camelToWf((String) key));
		}

		@Override
		public DtBase get(Object key) {
			return super.get(NamingConverter.camelToWf((String) key));
		}
	}

	/**
	 * 属性名是否使用字典序排序
	 * 
	 * @param sort
	 */
	public void setSortAttribute(boolean sort) {
		m_SortAttribute = sort;
	}

	/**
	 * 设置属性名排序器
	 * 
	 * @param comp
	 */
	public void setAttributeComparator(Comparator<String> comp) {
		m_Comparator = comp;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		Collection<String> atts = m_Map.keySet();
		if (atts.isEmpty()) {
			return Collections.emptyEnumeration();
		}
		if (null != m_Comparator) {
			ArrayList<String> list = new ArrayList<>(atts);
			Collections.sort(list, m_Comparator);
			atts = list;
		} else if (m_SortAttribute) {
			ArrayList<String> list = new ArrayList<>(atts);
			Collections.sort(list);
			atts = list;
		}
		return Collections.enumeration(atts);
	}

	@Override
	protected DtBase getAttributeInner(String name) {
		return m_Map.get(name);
	}

	@Override
	public Iterator<KvPair<String, DtBase>> iterator() {
		final Iterator<Entry<String, DtBase>> it = m_Map.entrySet().iterator();
		return new Iterator<KvPair<String, DtBase>>() {

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public KvPair<String, DtBase> next() {
				final Entry<String, DtBase> entry = it.next();
				return new KvPair<String, DtBase>() {

					@Override
					public DtBase getValue() {
						return entry.getValue();
					}

					@Override
					public String getKey() {
						return entry.getKey();
					}
				};
			}

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
		};
	}

	@Override
	public int getAttributeSize() {
		return m_Map.size();
	}

	/**
	 * 置入属性
	 * 
	 * @param name
	 * @param attribute
	 */
	public void put(String name, DtBase attribute) {
		m_Map.put(name, attribute);
	}

	public void put(String name, int value) {
		put(name, SimpleDtNumber.valueOf(value));
	}

	public void put(String name, long value) {
		put(name, SimpleDtNumber.valueOf(value));
	}

	public void put(String name, double value) {
		put(name, SimpleDtNumber.valueOf(value));
	}

	public void put(String name, String value) {
		put(name, SimpleDtString.valueOf(value));
	}

	public void put(String name, Date value) {
		put(name, SimpleDtDate.valueOf(value));
	}

	public void put(String name, boolean value) {
		put(name, SimpleDtBoolean.valueOf(value));
	}

	/**
	 * 移除属性
	 * 
	 * @param name
	 *            属性名
	 * @return 若名称对应的属性存在，则返回，否则为null
	 */
	public DtBase remove(String name) {
		return m_Map.remove(name);
	}

	@Override
	public String toString() {
		return type().toString() + ' ' + m_Map;
	}

}
