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

import java.util.ArrayList;
import java.util.List;

import cn.weforward.protocol.ops.traffic.TrafficTable;
import cn.weforward.protocol.ops.traffic.TrafficTableItem;

public class TrafficTableVo {
	public String name;
	public List<TrafficTableItemVo> items;

	public TrafficTableVo() {

	}

	public TrafficTableVo(TrafficTable table) {
		this.name = table.getName();
		List<TrafficTableItem> items = table.getItems();
		if (items.size() > 0) {
			List<TrafficTableItemVo> vos = new ArrayList<TrafficTableItemVo>(items.size());
			for (TrafficTableItem item : items) {
				TrafficTableItemVo vo = new TrafficTableItemVo(item);
				vos.add(vo);
			}
			this.items = vos;
		}
	}

	public static TrafficTableVo valueOf(TrafficTable table) {
		if (null == table) {
			return null;
		}
		if (table instanceof TrafficTableWrap) {
			return ((TrafficTableWrap) table).getVo();
		}
		return new TrafficTableVo(table);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TrafficTableItemVo> getItems() {
		return items;
	}

	public void setItems(List<TrafficTableItemVo> items) {
		this.items = items;
	}
}
