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
package cn.weforward.protocol.ops.secure;

import java.util.List;

/**
 * 微服务调用权限规则表。
 * <p>
 * 规则项描述了微服务对调用方Access的访问控制，默认（未配置规则时）禁止所有访问。
 * 
 * <pre>
 * - 规则项的名称与描述只是帮助记忆，无特殊意义；
 * - 每个规则项的id、kind、group依次匹配调用方Access的id、kind、group；
 * - 顺序匹配各个规则项，当匹配时，应用规则项的访问策略；
 * 
 * !! 特殊：空规则项（未指定id、kind、group）仅匹配无调用方Access的情况
 * </pre>
 * 
 * @author zhangpengji
 *
 */
public interface RightTable {

	/**
	 * 服务名称
	 * 
	 */
	String getName();

	/**
	 * 规则项列表
	 */
	List<RightTableItem> getItems();

	/**
	 * 在列表末尾添加一个项
	 * 
	 * @param item
	 */
	void appendItem(RightTableItem item);

	/**
	 * 在指定位置插入项。若index小于0，插入队首；若index大于项数，则插入队尾
	 * 
	 * @param item
	 * @param index
	 */
	void insertItem(RightTableItem item, int index);

	/**
	 * 替换指定位置的规则
	 * 
	 * @param item
	 * @param index
	 * @param name  原位置规则的名称
	 */
	void replaceItem(RightTableItem item, int index, String name);

	/**
	 * 移动规则的位置
	 * 
	 * @param from
	 * @param to
	 */
	void moveItem(int from, int to);

	/**
	 * 删除指定位置的规则
	 * 
	 * @param index
	 * @param name  原位置规则的名称
	 */
	void removeItem(int index, String name);
}
