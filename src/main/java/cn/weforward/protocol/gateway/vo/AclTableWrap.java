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
package cn.weforward.protocol.gateway.vo;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.ops.secure.AclTable;
import cn.weforward.protocol.ops.secure.AclTableItem;

public class AclTableWrap implements AclTable {

	AclTableVo m_Vo;
	Keeper m_Keeper;

	public AclTableWrap(AclTableVo vo) {
		this(vo, null);
	}

	public AclTableWrap(AclTableVo vo, Keeper keeper) {
		m_Vo = vo;
		m_Keeper = keeper;
	}

	protected AclTableVo getVo() {
		return m_Vo;
	}

	@Override
	public String getName() {
		return m_Vo.getName();
	}

	@Override
	public List<AclTableItem> getItems() {
		final List<AclTableItemVo> items = getVo().getItems();
		if (null == items || 0 == items.size()) {
			return Collections.emptyList();
		}
		return new AbstractList<AclTableItem>() {

			@Override
			public AclTableItem get(int index) {
				return new AclTableItemWrap(items.get(index));
			}

			@Override
			public int size() {
				return items.size();
			}
		};
	}

	@Override
	public void appendItem(AclTableItem item) {
		// TODO Auto-generated method stub
	}

	@Override
	public void insertItem(AclTableItem item, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void replaceItem(AclTableItem item, int index, String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveItem(int from, int to) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeItem(int index, String name) {
		// TODO Auto-generated method stub

	}

}
