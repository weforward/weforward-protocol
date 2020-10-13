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
package cn.weforward.protocol;

import cn.weforward.common.crypto.Base64;
import cn.weforward.common.crypto.Hex;
import cn.weforward.protocol.support.SimpleAccess;

/**
 * Access加载器
 * 
 * @author zhangpengji
 *
 */
public interface AccessLoader {

	/**
	 * 获取有效的access
	 * 
	 * @param accessId
	 * @return 凭证对象
	 */
	Access getValidAccess(String accessId);

	static class Single implements AccessLoader {

		protected Access m_Access;

		public Single(String id, String key) {
			String accessId = id;
			byte[] accessKey;
			try {
				accessKey = Hex.decode(key);
			} catch (Exception e) {
				accessKey = Base64.decode(key);
			}
			SimpleAccess acc = new SimpleAccess();
			acc.setAccessId(accessId);
			acc.setAccessKey(accessKey);
			acc.setValid(true);
			m_Access = acc;
		}

		public Single(Access acc) {
			m_Access = acc;
		}

		@Override
		public Access getValidAccess(String accessId) {
			if (null != m_Access && null != accessId && m_Access.getAccessId().equals(accessId)) {
				return m_Access;
			}
			return null;
		}

	}

	static final AccessLoader EMPTY = new AccessLoader() {

		@Override
		public Access getValidAccess(String accessId) {
			return null;
		}
	};
}
