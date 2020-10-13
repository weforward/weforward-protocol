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

import java.util.Random;

import cn.weforward.common.crypto.Base64;
import cn.weforward.common.crypto.Hex;
import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.exception.AuthException;
import cn.weforward.protocol.exception.WeforwardException;

/**
 * {@link Header#AUTH_TYPE_SHA2}的验证引擎
 * 
 * @author zhangpengji
 *
 */
public class Sha2AuthEngine implements AuthEngine {

	protected Random m_Random = new Random();

	public Sha2AuthEngine() {

	}

	@Override
	public String getType() {
		return Header.AUTH_TYPE_SHA2;
	}

	@Override
	public Output encode(Input in) throws AuthException {
		if (StringUtil.isEmpty(in.accessId)) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "access id值不能为空");
		}
		if (null == in.accessKey || 0 == in.accessKey.length) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "access key值不能为空");
		}
		String noise = in.noise;
		if (StringUtil.isEmpty(noise)) {
			// XXX 不能保证不重复
			long n = System.currentTimeMillis() << 20;
			int i = m_Random.nextInt();
			n |= (i & 0xfffff);
			noise = Hex.toHex64(n);
		}
		// XXX 识别通信是否安全（例如已使用https），不生成内容签名
		String dataSign;
		String sign;
		try {
			java.security.MessageDigest md;
			StringBuilder sb = StringBuilderPool._8k.poll();
			try {
				sb.append(in.serviceName);
				sb.append(in.accessId);
				sb.append(Base64.encode(in.accessKey));
				sb.append(noise);
				if (!StringUtil.isEmpty(in.tag)) {
					sb.append(in.tag);
				}
				if (!StringUtil.isEmpty(in.channel)) {
					sb.append(in.channel);
				}

				md = java.security.MessageDigest.getInstance("SHA-256");
				if (in.dataLength > 0) {
					md.update(in.data, in.dataOffset, in.dataLength);
				}
				dataSign = Base64.encode(md.digest());
				sb.append(dataSign);

				md.reset();
				md.update(sb.toString().getBytes("utf-8"));
			} finally {
				StringBuilderPool._8k.offer(sb);
			}
			sign = Base64.encode(md.digest());
		} catch (Exception e) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, e);
		}
		Output out = new Output();
		out.contentSign = dataSign;
		out.sign = sign;
		out.noise = noise;
		out.data = in.data;
		out.dataOffset = in.dataOffset;
		out.dataLength = in.dataLength;
		return out;
	}

	@Override
	public Output decode(Input in) throws AuthException {
		if (StringUtil.isEmpty(in.accessId)) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "access id值不能为空");
		}
		if (null == in.accessKey || 0 == in.accessKey.length) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "access key值不能为空");
		}
		if (StringUtil.isEmpty(in.noise)) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "Noise值不能为空");
		}
		if (StringUtil.isEmpty(in.sign)) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "Sign值不能为空");
		}
		if (in.sign.length() <= 32 || in.sign.length() > 64) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL,
					"Sign值异常:" + StringUtil.limit(in.sign, 100));
		}
		String sign;
		try {
			java.security.MessageDigest md;
			StringBuilder sb = StringBuilderPool._8k.poll();
			try {
				sb.append(in.serviceName);
				sb.append(in.accessId);
				// sb.append(Base64.encode(in.accessKey));
				Base64.encode(sb, in.accessKey, 0, in.accessKey.length);
				sb.append(in.noise);
				if (!StringUtil.isEmpty(in.tag)) {
					sb.append(in.tag);
				}
				if (!StringUtil.isEmpty(in.channel)) {
					sb.append(in.channel);
				}
				if (!StringUtil.isEmpty(in.contentSign)) {
					sb.append(in.contentSign);
				}
				md = java.security.MessageDigest.getInstance("SHA-256");
				md.update(sb.toString().getBytes("utf-8"));
			} finally {
				StringBuilderPool._8k.offer(sb);
			}
			sign = Base64.encode(md.digest());
		} catch (Exception e) {
			// _Logger.error(e.toString(), e);
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, e);
		}
		if (!sign.equals(in.sign)) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL,
					"签名不一致：" + sign + " != " + in.sign);
		}
		String contentSign = null;
		if (!StringUtil.isEmpty(in.contentSign)) {
			// 校验内容签名
			try {
				java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
				md.update(in.data, in.dataOffset, in.dataLength);
				contentSign = Base64.encode(md.digest());
			} catch (Exception e) {
				throw new AuthException(WeforwardException.CODE_AUTH_FAIL, e);
			}
			if (!contentSign.equals(in.contentSign)) {
				throw new AuthException(WeforwardException.CODE_AUTH_FAIL,
						"内容签名不一致：" + contentSign + " != " + in.contentSign);
			}
		}
		Output out = new Output();
		out.sign = sign;
		out.contentSign = contentSign;
		out.noise = in.noise;
		out.data = in.data;
		out.dataOffset = in.dataOffset;
		out.dataLength = in.dataLength;
		return out;
	}

}
