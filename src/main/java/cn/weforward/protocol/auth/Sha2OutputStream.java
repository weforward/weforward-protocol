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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import cn.weforward.common.crypto.Base64;
import cn.weforward.common.crypto.Hex;
import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.io.OutputStreamStay;
import cn.weforward.common.util.SimpleUtf8Encoder;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Access;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Header.HeaderOutput;
import cn.weforward.protocol.exception.AuthException;

/**
 * {@link Header#AUTH_TYPE_SHA2}验证器
 * 
 * @author zhangpengji
 *
 */
public class Sha2OutputStream extends AutherOutputStream {

	protected static final Random RANDOM = new Random();

	protected String m_ContentSign;
	protected MessageDigest m_ContentDigest;

	@Override
	protected Header authHeader(Header header) throws AuthException {
		if (MODE_DECODE == m_Mode) {
			decodeHeader(header);
		} else {
			encodeHeader(header);
		}
		return header;
	}

	void decodeHeader(Header header) throws AuthException {
		String contentSign = header.getContentSign();
		if (StringUtil.isEmpty(contentSign) && !m_IgnoreContent) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "缺少'content sign'");
		}
		String service = header.getService();
		if (StringUtil.isEmpty(service)) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "缺少'service'");
		}
		String accessId = header.getAccessId();
		if (StringUtil.isEmpty(accessId)) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "缺少'access id'");
		}
		String noise = header.getNoise();
		if (StringUtil.isEmpty(noise)) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "缺少'noise'");
		}
		String sign = header.getSign();
		if (StringUtil.isEmpty(sign)) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "缺少'sign'");
		}
		if (sign.length() <= 32 || sign.length() > 64) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "'sign'值异常:" + StringUtil.limit(sign, 100));
		}
		Access access = m_AccessLoader.getValidAccess(accessId);
		if (null == access) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "'access id'无效:" + accessId);
		}
		String sign2;
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			SimpleUtf8Encoder utf8Encoder = new SimpleUtf8Encoder(new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					md.update((byte) b);
				}
			});
			utf8Encoder.encode(service);
			utf8Encoder.encode(accessId);
			utf8Encoder.encode(access.getAccessKeyBase64());
			utf8Encoder.encode(noise);
			if (!StringUtil.isEmpty(header.getTag())) {
				utf8Encoder.encode(header.getTag());
			}
			if (!StringUtil.isEmpty(header.getChannel())) {
				utf8Encoder.encode(header.getChannel());
			}
			if (!StringUtil.isEmpty(contentSign)) {
				utf8Encoder.encode(contentSign);
			}
			sign2 = Base64.encode(md.digest());
		} catch (Exception e) {
			// _Logger.error(e.toString(), e);
			throw new AuthException(AuthException.CODE_AUTH_FAIL, e);
		}
		if (!sign2.equals(sign)) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "签名不一致：" + sign2 + " != " + sign);
		}

		if (!isIgnoreContent()) {
			m_ContentSign = contentSign;
			m_ContentDigest = openMessageDigest();
		}
	}

	MessageDigest openMessageDigest() throws AuthException {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// 应该不会发生
			throw new AuthException(AuthException.CODE_AUTH_FAIL, e);
		}
	}

	void encodeHeader(Header header) throws AuthException {
		String service = header.getService();
		if (StringUtil.isEmpty(service)) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "缺少'service'");
		}
		String accessId = header.getAccessId();
		if (StringUtil.isEmpty(accessId)) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "缺少'access id'");
		}

		String noise;
		// XXX 不能保证不重复
		long n = System.currentTimeMillis() << 20;
		int i = RANDOM.nextInt();
		n |= (i & 0xfffff);
		noise = Hex.toHex64(n);
		header.setNoise(noise);

		if (isIgnoreContent()) {
			String sign = genSign(header);
			header.setSign(sign);
		} else {
			// 需要校验内容，等到doFinal再生成sign
			m_ContentDigest = openMessageDigest();
		}
	}

	protected String genSign(Header header) throws AuthException {
		Access access = m_AccessLoader.getValidAccess(header.getAccessId());
		if (null == access) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, "'access id'无效");
		}
		try {
			final java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
			SimpleUtf8Encoder utf8Encoder = new SimpleUtf8Encoder(new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					md.update((byte) b);
				}
			});
			utf8Encoder.encode(header.getService());
			utf8Encoder.encode(access.getAccessId());
			utf8Encoder.encode(Base64.encode(access.getAccessKey()));
			utf8Encoder.encode(header.getNoise());
			if (!StringUtil.isEmpty(header.getTag())) {
				utf8Encoder.encode(header.getTag());
			}
			if (!StringUtil.isEmpty(header.getChannel())) {
				utf8Encoder.encode(header.getChannel());
			}
			if (!StringUtil.isEmpty(header.getContentSign())) {
				utf8Encoder.encode(header.getContentSign());
			}
			return Base64.encode(md.digest());
		} catch (Exception e) {
			throw new AuthException(AuthException.CODE_AUTH_FAIL, e);
		}
	}

	@Override
	public void setTransferTo(HeaderOutput headerOutput, OutputStream forward) throws IOException {
		if (isMode(MODE_ENCODE)) {
			OutputStream stay = OutputStreamStay.Wrap.wrap(forward);
			((OutputStreamStay) stay).stay();
			forward = stay;
		}
		super.setTransferTo(headerOutput, forward);
	}

	@Override
	protected void doFinal() throws AuthException, IOException {
		if (isMode(MODE_DECODE)) {
			String contentSign = Base64.encode(m_ContentDigest.digest());
			if (!contentSign.equals(m_ContentSign)) {
				throw new AuthException(AuthException.CODE_AUTH_FAIL,
						"内容签名不一致：" + contentSign + " != " + m_ContentSign);
			}
		} else {
			String contentSign = Base64.encode(m_ContentDigest.digest());
			m_Header.setContentSign(contentSign);
			String sign = genSign(m_Header);
			m_Header.setSign(sign);
			writeHeader();
			if (null != m_Forward) {
				m_Forward.flush();
			}
		}
	}

	@Override
	protected void writeHeader() throws IOException {
		if (isMode(MODE_ENCODE) && StringUtil.isEmpty(m_Header.getSign())) {
			// 等doFinal后再输出
			return;
		}
		super.writeHeader();
	}

	@Override
	protected int update(ByteBuffer src) throws IOException, AuthException {
		if (null != m_ContentDigest) {
			// src.mark();
			int mark = src.position();
			m_ContentDigest.update(src);
			// src.reset();
			src.position(mark);
		}
		return forward(src);
	}

	@Override
	protected void update(int b) throws AuthException, IOException {
		if (null != m_ContentDigest) {
			m_ContentDigest.update((byte) b);
		}
		forward(b);
	}

	@Override
	protected void update(byte[] data, int off, int len) throws AuthException, IOException {
		if (null != m_ContentDigest) {
			m_ContentDigest.update(data, off, len);
		}
		forward(data, off, len);
	}

	@Override
	protected int update(InputStream src, int count) throws IOException, AuthException {
		return BytesOutputStream.transfer(src, this, count);
	}

	// @Override
	// protected int update(ByteBuffer src) throws IOException, AuthException {
	// if (MODE_ENCODE == m_Mode) {
	// throw new AuthException(AuthException.CODE_AUTH_FAIL,
	// "'encode'模式不支持验证内容");
	// }
	// int len = src.remaining();
	// m_ContentDigest.update(src);
	// forward(src);
	// return len;
	// }
}
