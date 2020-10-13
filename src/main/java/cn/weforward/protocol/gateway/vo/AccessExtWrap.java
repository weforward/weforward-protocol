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

import cn.weforward.common.crypto.Base64;
import cn.weforward.common.crypto.Hex;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.ops.AccessExt;

public class AccessExtWrap implements AccessExt {

	AccessExtVo m_Vo;
	Keeper m_Keeper;

	byte[] m_AccessKeyBytes;
	String m_AccessKeyBase64;

	public AccessExtWrap(AccessExtVo vo) {
		m_Vo = vo;
	}

	public AccessExtVo getVo() {
		return m_Vo;
	}

	public void setKeeper(Keeper keeper) {
		m_Keeper = keeper;
	}

	@Override
	public String getAccessId() {
		return m_Vo.id;
	}

	@Override
	public byte[] getAccessKey() {
		if (null == m_AccessKeyBytes) {
			m_AccessKeyBytes = Hex.decode(m_Vo.key);
		}
		return m_AccessKeyBytes;
	}

	@Override
	public String getKind() {
		return m_Vo.kind;
	}

	@Override
	public boolean isValid() {
		return m_Vo.valid;
	}

	@Override
	public String getGroupId() {
		return m_Vo.group;
	}

	@Override
	public String getSummary() {
		return m_Vo.summary;
	}

	@Override
	public String getAccessKeyHex() {
		return m_Vo.key;
	}

	@Override
	public String getAccessKeyBase64() {
		if (null == m_AccessKeyBase64) {
			m_AccessKeyBase64 = Base64.encode(getAccessKey());
		}
		return m_AccessKeyBase64;
	}

	@Override
	public void setSummary(String summary) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		AccessExt acc = m_Keeper.updateAccess(getAccessId(), summary, isValid());
		m_Vo = AccessExtVo.valueOf(acc);
	}

	@Override
	public void setValid(boolean valid) {
		if (null == m_Keeper) {
			throw new IllegalStateException("尚未注入Keeper");
		}
		AccessExt acc = m_Keeper.updateAccess(getAccessId(), getSummary(), valid);
		m_Vo = AccessExtVo.valueOf(acc);
	}

	@Override
	public String getTenant() {
		return m_Vo.tenant;
	}

	@Override
	public String getOpenid() {
		return m_Vo.openid;
	}
}
