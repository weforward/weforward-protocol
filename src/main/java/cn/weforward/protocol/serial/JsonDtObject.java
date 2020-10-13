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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import cn.weforward.common.execption.InvalidFormatException;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.support.datatype.AbstractDtObject;

/**
 * 基于json串的DtObject实现
 * 
 * @author zhangpengji
 *
 */
public class JsonDtObject extends AbstractDtObject {

	protected String m_Json;
	protected int m_Offset;
	protected int m_Lenght;

	protected Map<String, DtBase> m_Attributes;

	public JsonDtObject(String json) {
		this(json, 0, json.length());
	}

	public JsonDtObject(String json, int offset, int length) {
		m_Json = json;
		m_Offset = offset;
		m_Lenght = length;
	}

	private void parse() {
		if (null != m_Attributes) {
			return;
		}
		synchronized (this) {
			if (null != m_Attributes) {
				return;
			}
			try {
				m_Attributes = JsonParser.parseObject(m_Json, m_Offset, m_Lenght);
			} catch (IOException e) {
				throw new InvalidFormatException("Json串格式不正确", e);
			}
			m_Json = null;
		}
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		parse();
		Set<String> keys = m_Attributes.keySet();
		return Collections.enumeration(keys);
	}

	@Override
	protected DtBase getAttributeInner(String name) {
		parse();
		return m_Attributes.get(name);
	}

	@Override
	public int getAttributeSize() {
		parse();
		return m_Attributes.size();
	}

	/**
	 * 获取原生Json字串。若已经解析为DtObject则返回null
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
			Map<String, DtBase> atts = m_Attributes;
			if (null != atts) {
				str = atts.toString();
			}
		}
		return type().toString() + ' ' + str;
	}
}
