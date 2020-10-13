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

import cn.weforward.protocol.ops.secure.AclTableResource;

public class AclTableResourceWrap implements AclTableResource {

	AclTableResourceVo m_Vo;

	public AclTableResourceWrap(AclTableResourceVo vo) {
		m_Vo = vo;
	}

	protected AclTableResourceVo getVo() {
		return m_Vo;
	}

	@Override
	public String getPattern() {
		return m_Vo.getPattern();
	}

	@Override
	public int getRight() {
		return m_Vo.getRight();
	}

	@Override
	public int getMatchType() {
		return m_Vo.getMatchType();
	}

}
