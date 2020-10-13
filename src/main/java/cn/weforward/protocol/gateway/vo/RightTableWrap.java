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
import cn.weforward.protocol.ops.secure.RightTable;
import cn.weforward.protocol.ops.secure.RightTableItem;

public class RightTableWrap implements RightTable {

	RightTableVo m_Vo;
	Keeper m_Keeper;

	public RightTableWrap(RightTableVo vo) {
		this(vo, null);
	}

	public RightTableWrap(RightTableVo vo, Keeper keeper) {
		m_Vo = vo;
		m_Keeper = keeper;
	}

	protected RightTableVo getVo() {
		return m_Vo;
	}

	public void setKeeper(Keeper keeper) {
		m_Keeper = keeper;
	}

	@Override
	public String getName() {
		return getVo().getName();
	}

	@Override
	public List<RightTableItem> getItems() {
		final List<RightTableItemVo> items = getVo().getItems();
		if (null == items || 0 == items.size()) {
			return Collections.emptyList();
		}
		return new AbstractList<RightTableItem>() {

			@Override
			public RightTableItem get(int index) {
				return new RightTableItemWrap(items.get(index));
			}

			@Override
			public int size() {
				return items.size();
			}
		};
	}

	@Override
	public void appendItem(RightTableItem item) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		RightTable table = m_Keeper.appendRightRule(getName(), item);
		m_Vo = RightTableVo.valueOf(table);
	}

	@Override
	public void insertItem(RightTableItem item, int index) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		RightTable table = m_Keeper.insertRightRule(getName(), item, index);
		m_Vo = RightTableVo.valueOf(table);
	}

	@Override
	public void replaceItem(RightTableItem item, int index, String name) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		RightTable table = m_Keeper.replaceRightRule(getName(), item, index, name);
		m_Vo = RightTableVo.valueOf(table);
	}

	@Override
	public void moveItem(int from, int to) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		RightTable table = m_Keeper.moveRightRule(getName(), from, to);
		m_Vo = RightTableVo.valueOf(table);
	}

	@Override
	public void removeItem(int index, String name) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		RightTable table = m_Keeper.removeRightRule(getName(), index, name);
		m_Vo = RightTableVo.valueOf(table);
	}

}
