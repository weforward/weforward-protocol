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

import java.io.OutputStream;

import cn.weforward.protocol.Access;
import cn.weforward.protocol.Header;

/**
 * 验证的输出内容
 * 
 * @author zhangpengji
 *
 */
public class AuthOutput {

	public final OutputStream output;
	public final Header header;
	public Access access;
	// public String type;
	// public String noise;
	// public String sign;
	// public String contentSign;

	public AuthOutput(OutputStream output) {
		this(null, output);
	}

	public AuthOutput(Header header, OutputStream output) {
		this.output = output;
		this.header = header;
	}

	public Access getAccess() {
		return access;
	}

	public void setAccess(Access access) {
		this.access = access;
	}

	// public String getType() {
	// return type;
	// }

	public void setType(String type) {
		// this.type = type;
		if (null != header) {
			header.setAuthType(type);
		}
	}

	// public String getNoise() {
	// return noise;
	// }

	public void setNoise(String noise) {
		// this.noise = noise;
		if (null != header) {
			header.setNoise(noise);
		}
	}

	// public String getSign() {
	// return sign;
	// }

	public void setSign(String sign) {
		// this.sign = sign;
		if (null != header) {
			header.setSign(sign);
		}
	}

	public OutputStream getOutput() {
		return output;
	}

	// public String getContentSign() {
	// return contentSign;
	// }

	public void setContentSign(String contentSign) {
		// this.contentSign = dataSign;
		if (null != header) {
			header.setContentSign(contentSign);
		}
	}

}
