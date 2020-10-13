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
package cn.weforward.protocol.datatype;

import java.util.Enumeration;

import cn.weforward.common.KvPair;

/**
 * 数据类型 - 对象
 * 
 * @author zhangpengji
 *
 */
public interface DtObject extends DtBase {

	/**
	 * 属性名集合
	 * 
	 * @return 属性名集合
	 */
	Enumeration<String> getAttributeNames();

	/**
	 * 属性集合
	 * 
	 * @return 属性集合
	 */
	Enumeration<KvPair<String, DtBase>> getAttributes();

	/**
	 * 属性个数
	 * 
	 * @return 个数
	 */
	int getAttributeSize();

	/**
	 * 获取属性
	 * 
	 * @param <E>
	 * @param name 属性名
	 * @return 属性
	 */
	<E extends DtBase> E getAttribute(String name);

	// /**
	// * 获取属性
	// *
	// * @param <E>
	// * @param name
	// * 属性名
	// * @param refType
	// * 参考类型
	// * @return
	// */
	// <E extends DtBase> E getAttribute(String name, DataType refType);

	DtNumber getNumber(String name);

	DtString getString(String name);

	DtBoolean getBoolean(String name);

	DtDate getDate(String name);

	DtList getList(String name);

	DtObject getObject(String name);

}
