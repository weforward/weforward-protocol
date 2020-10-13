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
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.exception.AuthException;
import cn.weforward.protocol.exception.WeforwardException;

/**
 * {@link Header#AUTH_TYPE_SIGN}的验证引擎
 *
 * @author zhangpengji
 *
 */
public class SignAuthEngine implements AuthEngine {

	protected Random m_Random = new Random();

	public SignAuthEngine() {

	}

	@Override
	public String getType() {
		return Header.AUTH_TYPE_SIGN;
	}

	@Override
	public Output encode(Input in) throws AuthException {
		String noise = in.noise;
		if (StringUtil.isEmpty(noise)) {
			// XXX 不能保证不重复
			long n = System.currentTimeMillis() << 20;
			int i = m_Random.nextInt();
			n |= (i & 0xfffff);
			noise = Hex.toHex64(n);
		}
		String sign;
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
			md.update(in.data, in.dataOffset, in.dataLength);
			md.update(noise.getBytes("utf-8"));
			md.update(in.accessKey);
			sign = Base64.encode(md.digest());
		} catch (Exception e) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, e.toString());
		}
		Output out = new Output();
		out.sign = sign;
		out.noise = noise;
		out.data = in.data;
		out.dataOffset = in.dataOffset;
		out.dataLength = in.dataLength;
		return out;
	}

	@Override
	public Output decode(Input in) throws AuthException {
		if (StringUtil.isEmpty(in.noise)) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "Noise值不能为空");
		}
		if (StringUtil.isEmpty(in.sign)) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "Sign值不能为空");
		}
		String inputSign = in.sign;
		if (inputSign.length() <= 32 || inputSign.length() > 64) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "Sign值异常:" + StringUtil.limit(inputSign, 100));
		}
		if (64 == inputSign.length()) {
			try {
				// 应该是hex格式，转换一下
				inputSign = Base64.encode(Hex.decode(inputSign));
			} catch (Exception e) {
				//_Logger.warn("sign格式转换失败:" + inputSign, e);
				throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "Sign值异常:" + inputSign);
			}
		}
		String sign;
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
			md.update(in.data, in.dataOffset, in.dataLength);
			md.update(in.noise.getBytes("utf-8"));
			md.update(in.accessKey);
			sign = Base64.encode(md.digest());
		} catch (Exception e) {
			// _Logger.error(e.toString(), e);
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, e);
		}
		if (!sign.equals(inputSign)) {
			throw new AuthException(WeforwardException.CODE_AUTH_FAIL, "签名不一致：" + sign + " != " + inputSign);
		}
		Output out = new Output();
		out.sign = sign;
		out.noise = in.noise;
		out.data = in.data;
		out.dataOffset = in.dataOffset;
		out.dataLength = in.dataLength;
		return out;
	}

}
