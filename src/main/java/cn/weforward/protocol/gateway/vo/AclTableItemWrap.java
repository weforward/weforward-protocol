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

import cn.weforward.protocol.ops.secure.AclTableItem;
import cn.weforward.protocol.ops.secure.AclTableResource;

public class AclTableItemWrap implements AclTableItem {

	AclTableItemVo m_Vo;
	
	public AclTableItemWrap(AclTableItemVo vo) {
		m_Vo = vo;
	}
	
	protected AclTableItemVo getVo() {
		return m_Vo;
	}
	
	@Override
	public String getName() {
		return m_Vo.getName();
	}

	@Override
	public String getDescription() {
		return m_Vo.getDescription();
	}

	@Override
	public String getAccessId() {
		return m_Vo.getAccessId();
	}

	@Override
	public String getAccessKind() {
		return m_Vo.getAccessKind();
	}

	@Override
	public String getAccessGroup() {
		return m_Vo.getAccessGroup();
	}

	@Override
	public List<AclTableResource> getResources() {
		final List<AclTableResourceVo> reses = getVo().getResources();
		if (null == reses || 0 == reses.size()) {
			return Collections.emptyList();
		}
		return new AbstractList<AclTableResource>() {

			@Override
			public AclTableResource get(int index) {
				return new AclTableResourceWrap(reses.get(index));
			}

			@Override
			public int size() {
				return reses.size();
			}
		};
	}

}
