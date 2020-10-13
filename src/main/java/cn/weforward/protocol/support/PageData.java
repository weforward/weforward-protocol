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
package cn.weforward.protocol.support;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import cn.weforward.common.ResultPage;

/**
 * 分页数据
 * 
 * @author zhangpengji
 *
 */
public class PageData implements Closeable {

	static final PageData EMPTY = new PageData();

	protected int m_Count;
	protected int m_Page;
	protected int m_PageSize;
	protected int m_PageCount;
	protected Iterable<?> m_IterableItems;
	protected List<?> m_ListItems;

	protected int m_Pos = -1;

	PageData() {
		m_Count = 0;
		m_IterableItems = Collections.emptyList();
	}

	public PageData(ResultPage<?> rp) {
		m_Count = rp.getCount();
		m_Page = rp.getPage();
		m_PageSize = rp.getPageSize();
		m_PageCount = rp.getPageCount();
		m_IterableItems = rp;
	}

	public static PageData valueOf(ResultPage<?> rp) {
		if (0 == rp.getCount()) {
			return PageData.EMPTY;
		}
		return new PageData(rp);
	}

	public static PageData valueOf(int count, int page, int pageSize, int pageCount, List<?> items) {
		PageData data = new PageData();
		data.m_Count = count;
		data.m_Page = page;
		data.m_PageSize = pageSize;
		data.m_PageCount = pageCount;
		data.m_ListItems = items;
		return data;
	}

	public boolean isEmpty() {
		// return null == this.m_Items || !m_Items.iterator().hasNext();
		return 0 == getCount();
	}

	public int getCount() {
		return m_Count;
	}

	public int getPage() {
		return m_Page;
	}

	public int getPageSize() {
		return m_PageSize;
	}

	public int getPageCount() {
		return m_PageCount;
	}

	@SuppressWarnings("unchecked")
	public <E> Iterable<E> getItems() {
		if (null != m_ListItems) {
			return (Iterable<E>) m_ListItems;
		}
		return (Iterable<E>) m_IterableItems;
	}

	// /**
	// * 检查是否越界
	// *
	// * @param index
	// * @return 越界时返回true
	// */
	// public boolean checkIndex(int index) {
	// if (null == m_ListItems) {
	// throw new IllegalStateException();
	// }
	// int pos = (m_Page - 1) * m_PageSize;
	// if (index < pos || index > (pos + m_ListItems.size())) {
	// return true;
	// }
	// return false;
	// }

	public int getItemSize() {
		if (null == m_ListItems) {
			throw new IllegalStateException();
		}
		return m_ListItems.size();
	}

	public int getPos() {
		if (-1 == m_Pos) {
			m_Pos = (this.m_Page - 1) * m_PageSize;
		}
		return m_Pos;
	}

	@SuppressWarnings("unchecked")
	public <E> E getItem(int idx) {
		if (null == m_ListItems) {
			throw new IllegalStateException();
		}
		return (E) m_ListItems.get(idx);
	}

	@Override
	public void close() throws IOException {
		if (m_IterableItems instanceof Closeable) {
			((Closeable) m_IterableItems).close();
		}

	}
}
