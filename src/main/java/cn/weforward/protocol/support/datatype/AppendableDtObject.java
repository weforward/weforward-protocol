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
package cn.weforward.protocol.support.datatype;

import java.util.Enumeration;

import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtObject;

/**
 * 封装DtObject，允许追加成员属性
 * 
 * @author zhangpengji
 *
 */
public class AppendableDtObject extends SimpleDtObject {

	protected DtObject m_Original;

	public AppendableDtObject(DtObject obj) {
		this(obj, true);
	}

	public AppendableDtObject(DtObject obj, boolean autoNaming) {
		super(autoNaming);
		m_Original = obj;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		final Enumeration<String> originals = m_Original.getAttributeNames();
		final Enumeration<String> appends = super.getAttributeNames();
		return new Enumeration<String>() {

			@Override
			public boolean hasMoreElements() {
				return originals.hasMoreElements() || appends.hasMoreElements();
			}

			@Override
			public String nextElement() {
				if (originals.hasMoreElements()) {
					return originals.nextElement();
				}
				return appends.nextElement();
			}
		};
	}

	@Override
	public int getAttributeSize() {
		return m_Original.getAttributeSize() + super.getAttributeSize();
	}

	@Override
	protected DtBase getAttributeInner(String name) {
		DtBase b = m_Original.getAttribute(name);
		if (null != b) {
			return b;
		}
		return super.getAttributeInner(name);
	}

}
