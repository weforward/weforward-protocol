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
package cn.weforward.protocol.client.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * 多个Enum合并
 * 
 * @author daibo
 *
 * @param <E>
 */
public class MultiEnumeration<E> implements Enumeration<E> {

	protected List<Enumeration<E>> m_List;
	protected int m_Index;

	protected MultiEnumeration(@SuppressWarnings("unchecked") Enumeration<E>... arr) {
		m_List = Arrays.asList(arr);
		m_Index = 0;
	}

	public static <E> MultiEnumeration<E> valueOf(@SuppressWarnings("unchecked") Enumeration<E>... arr) {
		return new MultiEnumeration<E>(arr);
	}

	@Override
	public boolean hasMoreElements() {
		while (m_List.size() > m_Index) {
			boolean has = m_List.get(m_Index).hasMoreElements();
			if (has) {
				return true;
			} else {
				m_Index++;
			}
		}
		return false;
	}

	@Override
	public E nextElement() {
		Enumeration<E> e = m_List.get(m_Index);
		if (e.hasMoreElements()) {
			return e.nextElement();
		}
		// 下一个列表
		m_Index++;
		if (m_List.size() < m_Index) {
			e = m_List.get(m_Index);
			if (e.hasMoreElements()) {
				return e.nextElement();
			}
		}
		return null;
	}

}
