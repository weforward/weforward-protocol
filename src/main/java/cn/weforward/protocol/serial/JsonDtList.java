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

import java.io.IOException;
import java.util.List;

import cn.weforward.common.execption.InvalidFormatException;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.support.datatype.AbstractDtList;

/**
 * 基于json串的DtList实现
 * 
 * @author zhangpengji
 *
 */
public class JsonDtList extends AbstractDtList {

	protected String m_Json;
	protected int m_Offset;
	protected int m_Lenght;

	protected List<DtBase> m_Items;

	public JsonDtList(String json) {
		this(json, 0, json.length());
	}

	public JsonDtList(String json, int offset, int length) {
		m_Json = json;
		m_Offset = offset;
		m_Lenght = length;
	}

	private void parse() {
		if (null != m_Items) {
			return;
		}
		synchronized (this) {
			if (null != m_Items) {
				return;
			}
			try {
				m_Items = JsonParser.parseArray(m_Json, m_Offset, m_Lenght);
			} catch (IOException e) {
				throw new InvalidFormatException("Json串格式不正确", e);
			}
			m_Json = null;
		}
	}

	@Override
	public int size() {
		parse();
		return m_Items.size();
	}

	@Override
	protected DtBase getItemInner(int index) {
		parse();
		return m_Items.get(index);
	}

	/**
	 * 获取原生Json字串。若已经解析为DtList则返回null
	 * 
	 */
	public String getJsonString() {
		String json = m_Json;
		if (null == json) {
			return null;
		}
		if (0 == m_Offset && m_Json.length() == m_Lenght) {
			return json;
		}
		return json.substring(m_Offset, m_Offset + m_Lenght);
	}

	@Override
	public String toString() {
		String str = getJsonString();
		if (null == str) {
			List<DtBase> items = m_Items;
			if (null != items) {
				str = items.toString();
			}
		}
		return type().toString() + ' ' + str;
	}
}
