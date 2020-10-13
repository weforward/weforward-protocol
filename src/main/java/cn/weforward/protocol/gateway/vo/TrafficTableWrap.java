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
import cn.weforward.protocol.ops.traffic.TrafficTable;
import cn.weforward.protocol.ops.traffic.TrafficTableItem;

public class TrafficTableWrap implements TrafficTable {

	TrafficTableVo m_Vo;
	Keeper m_Keeper;

	public TrafficTableWrap(TrafficTableVo vo) {
		this(vo, null);
	}

	public TrafficTableWrap(TrafficTableVo vo, Keeper keeper) {
		m_Vo = vo;
		m_Keeper = keeper;
	}

	public TrafficTableVo getVo() {
		return m_Vo;
	}

	@Override
	public String getName() {
		return getVo().getName();
	}

	@Override
	public List<TrafficTableItem> getItems() {
		final List<TrafficTableItemVo> items = getVo().getItems();
		if (null == items || 0 == items.size()) {
			return Collections.emptyList();
		}
		return new AbstractList<TrafficTableItem>() {

			@Override
			public TrafficTableItem get(int index) {
				return new TrafficTableItemWrap(items.get(index));
			}

			@Override
			public int size() {
				return items.size();
			}
		};
	}

	@Override
	public void appendItem(TrafficTableItem item) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		TrafficTable table = m_Keeper.appendTrafficRule(getName(), item);
		m_Vo = TrafficTableVo.valueOf(table);
	}

	@Override
	public void insertItem(TrafficTableItem item, int index) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		TrafficTable table = m_Keeper.insertTrafficRule(getName(), item, index);
		m_Vo = TrafficTableVo.valueOf(table);
	}

	@Override
	public void replaceItem(TrafficTableItem item, int index, String name) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		TrafficTable table = m_Keeper.replaceTrafficRule(getName(), item, index, name);
		m_Vo = TrafficTableVo.valueOf(table);
	}

	@Override
	public void moveItem(int from, int to) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		TrafficTable table = m_Keeper.moveTrafficRule(getName(), from, to);
		m_Vo = TrafficTableVo.valueOf(table);
	}

	@Override
	public void removeItem(int index, String name) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		TrafficTable table = m_Keeper.removeTrafficRule(getName(), index, name);
		m_Vo = TrafficTableVo.valueOf(table);
	}

}
