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
 * 微服务的ACL表。<br/>
 * 描述Access对微服务资源的访问权限。 Access支持按id、kind、group匹配，若为空，则忽略匹配；
 * 依次按AclTableItem的顺序匹配，匹配成功则忽略后面的规则项；无匹配时，权限为0。
 * 
 * @author zhangpengji
 *
 */
public interface AclTable {

	/**
	 * 服务名称
	 * 
	 */
	String getName();

	/**
	 * Access项列表
	 * 
	 */
	List<AclTableItem> getItems();

	/**
	 * 在列表末尾添加一个项
	 * 
	 * @param item
	 */
	void appendItem(AclTableItem item);

	/**
	 * 在指定位置插入项。若index小于0，插入队首；若index大于项数，则插入队尾
	 * 
	 * @param item
	 * @param index
	 */
	void insertItem(AclTableItem item, int index);

	/**
	 * 替换指定位置的规则
	 * 
	 * @param item
	 * @param index
	 * @param name  原位置规则的名称
	 */
	void replaceItem(AclTableItem item, int index, String name);

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
