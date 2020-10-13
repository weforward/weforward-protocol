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
package cn.weforward.protocol.auth;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.weforward.common.crypto.Base64;
import cn.weforward.common.crypto.Hex;
import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Access;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.exception.AuthException;
import cn.weforward.protocol.exception.WeforwardException;

/**
 * {@link Header#AUTH_TYPE_SIGN}验证器
 * 
 * @author zhangpengji
 *
 */
public class SignOutputStream extends AutherOutputStream {

	String m_Noise;
	String m_Sign;
	byte[] m_AccessKey;
	MessageDigest m_MessageDigest;

	@Override
	protected void onInit() {
		if (isMode(MODE_ENCODE)) {
			throw new IllegalArgumentException("WF-Sign不支持encode模式");
		}
	}

	@Override
	protected Header authHeader(Header header) throws AuthException {
		decodeHeader(header);
		return header;
	}

	void decodeHeader(Header header) throws AuthException {
		String noise = header.getNoise();
		if (StringUtil.isEmpty(noise)) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "缺少'noise'");
		}
		String sign = header.getSign();
		if (StringUtil.isEmpty(header.getSign())) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "缺少'sign'");
		}
		if (sign.length() <= 32 || sign.length() > 64) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "'sign'值异常:" + StringUtil.limit(sign, 100));
		}
		if (64 == sign.length()) {
			try {
				// 应该是hex格式，转换一下
				sign = Base64.encode(Hex.decode(sign));
			} catch (Exception e) {
				// _Logger.warn("sign格式转换失败:" + inputSign, e);
				throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "'sign'值异常:" + sign);
			}
		}
		String accessId = header.getAccessId();
		if (StringUtil.isEmpty(accessId)) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "缺少'access id'");
		}
		Access access = m_AccessLoader.getValidAccess(accessId);
		if (null == access) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "'access id'无效");
		}

		m_Noise = noise;
		m_Sign = sign;
		m_AccessKey = access.getAccessKey();
		try {
			m_MessageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// 应该不会发生
			throw new AuthException(AuthException.CODE_AUTH_FAIL, e);
		}
	}

	@Override
	protected boolean isIgnoreContent() {
		// 总是验证内容
		return false;
	}

	@Override
	protected void doFinal() throws AuthException, IOException {
		m_MessageDigest.update(m_Noise.getBytes("utf-8"));
		m_MessageDigest.update(m_AccessKey);
		String sign = Base64.encode(m_MessageDigest.digest());
		if (!sign.equals(m_Sign)) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "内容签名不一致：" + sign + " != " + m_Sign);
		}
	}

	@Override
	protected int update(ByteBuffer src) throws IOException, AuthException {
		// src.mark();
		int mark = src.position();
		m_MessageDigest.update(src);
		// src.reset();
		src.position(mark);

		return forward(src);
	}

	@Override
	protected void update(int b) throws IOException, AuthException {
		m_MessageDigest.update((byte) b);

		forward(b);
	}

	@Override
	protected int update(InputStream src, int count) throws IOException, AuthException {
		return BytesOutputStream.transfer(src, this, count);
	}

	@Override
	protected void update(byte[] data, int off, int len) throws AuthException, IOException {
		m_MessageDigest.update(data, off, len);

		forward(data, off, len);
	}

}
