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
package cn.weforward.protocol.support;

import cn.weforward.common.crypto.Base64;
import cn.weforward.common.crypto.Hex;
import cn.weforward.protocol.Access;

/**
 * <code>Access</code>的简单实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleAccess implements Access {

	protected String m_AccessId;
	protected byte[] m_AccessKey;
	// protected String m_GroupId;
	// protected String m_Summary;
	protected String m_Tenant;
	protected String m_Openid;
	protected boolean m_Valid;

	private String m_AccessKeyHex;
	private String m_AccessKeyBase64;

	@Override
	public String getAccessId() {
		return m_AccessId;
	}

	@Override
	public byte[] getAccessKey() {
		return m_AccessKey;
	}

	@Override
	public String getKind() {
		return Access.Helper.getKind(getAccessId());
	}

	@Override
	public boolean isValid() {
		return m_Valid;
	}

	// @Override
	// public String getGroupId() {
	// return m_GroupId;
	// }
	//
	// @Override
	// public String getSummary() {
	// return m_Summary;
	// }
	//
	// @Override
	// public void setSummary(String summary) {
	// m_Summary = summary;
	// }
	//
	@Override
	public String getAccessKeyHex() {
		if (null == m_AccessKeyHex) {
			byte[] key = getAccessKey();
			if (null != key && key.length > 0) {
				m_AccessKeyHex = Hex.encode(key);
			}
		}
		return m_AccessKeyHex;
	}
	
	@Override
	public String getAccessKeyBase64() {
		if (null == m_AccessKeyBase64) {
			byte[] key = getAccessKey();
			if (null != key && key.length > 0) {
				m_AccessKeyBase64 = Base64.encode(key);
			}
		}
		return m_AccessKeyBase64;
	}

	public void setAccessId(String accessId) {
		m_AccessId = accessId;
	}

	public void setAccessKey(byte[] accessKey) {
		m_AccessKey = accessKey;
	}

	// public void setGroupId(String groupId) {
	// m_GroupId = groupId;
	// }

	public void setValid(boolean valid) {
		m_Valid = valid;
	}

	@Override
	public String getTenant() {
		return m_Tenant;
	}

	public void setTenant(String tenant) {
		m_Tenant = tenant;
	}

	@Override
	public String getOpenid() {
		return m_Openid;
	}

	public void setOpenid(String openid) {
		m_Openid = openid;
	}

}
