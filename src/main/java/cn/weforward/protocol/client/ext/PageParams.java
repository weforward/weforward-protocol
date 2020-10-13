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
package cn.weforward.protocol.client.ext;

import java.util.Enumeration;

import cn.weforward.common.KvPair;
import cn.weforward.protocol.client.util.MultiEnumeration;
import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.support.datatype.AbstractDtObject;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

public class PageParams extends AbstractDtObject implements DtObject {

	protected DtObject m_Params;

	protected DtObject m_PageParams;

	protected Enumeration<String> m_AttributeNames;

	protected Enumeration<KvPair<String, DtBase>> m_Attributes;

	@SuppressWarnings("unchecked")
	public PageParams(DtObject params, int page, int pageSize) {
		m_Params = params;
		SimpleDtObject pageParams = new SimpleDtObject();
		pageParams.put("page", page);
		pageParams.put("page_size", pageSize);
		m_PageParams = pageParams;
		if (null == params) {
			m_AttributeNames = m_PageParams.getAttributeNames();
			m_Attributes = m_PageParams.getAttributes();
		} else {
			m_AttributeNames = MultiEnumeration.valueOf(m_Params.getAttributeNames(), m_PageParams.getAttributeNames());
			m_Attributes = MultiEnumeration.valueOf(m_Params.getAttributes(), m_PageParams.getAttributes());
		}
	}

	@Override
	public DataType type() {
		return DataType.OBJECT;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return m_AttributeNames;
	}

	@Override
	public Enumeration<KvPair<String, DtBase>> getAttributes() {
		return m_Attributes;
	}

	@Override
	public int getAttributeSize() {
		return null == m_Params ? 0 : m_Params.getAttributeSize() + m_PageParams.getAttributeSize();
	}

	@Override
	protected DtBase getAttributeInner(String name) {
		DtBase v = null;
		if (null != m_Params) {
			v = m_Params.getAttribute(name);
		}
		if (null == v) {
			return m_PageParams.getAttribute(name);
		}
		return v;
	}

}
