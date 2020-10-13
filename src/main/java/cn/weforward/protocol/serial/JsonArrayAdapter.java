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
package cn.weforward.protocol.serial;

import cn.weforward.common.json.JsonArray;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.support.datatype.SimpleDtList;

/**
 * <code>JsonArray</code>适配器
 * 
 * @author zhangpengji
 *
 */
class JsonArrayAdapter implements JsonArray.Appendable {

	SimpleDtList m_List;

	JsonArrayAdapter() {
		this(new SimpleDtList());
	}

	JsonArrayAdapter(SimpleDtList list) {
		m_List = list;
	}

	static final JsonArrayAdapter EMPTY = new JsonArrayAdapter(SimpleDtList.empty());

	@Override
	public int size() {
		return m_List.size();
	}

	@Override
	public Object item(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Object value) {
		m_List.addItem(JsonObjectAdapter.toDtBase(value));
	}

	DtList getDtList() {
		return m_List;
	}
}
