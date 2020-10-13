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

import java.io.InputStream;

import cn.weforward.protocol.Header;

/**
 * 验证的输入内容
 * 
 * @author zhangpengji
 *
 */
public class AuthInput {

	/** 内容 */
	public final InputStream input;
	/** 头信息 */
	public final Header header;
	// /** 验证类型，如：{@linkplain Header#AUTH_TYPE_AES} */
	// public String type;
	// public String serviceName;
	// public String accessId;;
	// public String noise;
	// public String tag;
	// public String channel;
	// public String sign;
	// public String contentSign;

	public AuthInput(Header header, InputStream in) {
		this.input = in;
		this.header = header;
		// this.type = header.getAuthType();
		// this.serviceName = header.getService();
		// this.accessId = header.getAccessId();
		// this.noise = header.getNoise();
		// this.tag = header.getTag();
		// this.channel = header.getChannel();
		// this.sign = header.getSign();
		// this.contentSign = header.getContentSign();
	}

	public String getType() {
		// return type;
		return header.getAuthType();
	}

	// public void setType(String type) {
	// this.type = type;
	// }

	public String getAccessId() {
		// return accessId;
		return header.getAccessId();
	}

	// public void setAccessId(String accessId) {
	// this.accessId = accessId;
	// }

	public String getNoise() {
		// return noise;
		return header.getNoise();
	}

	// public void setNoise(String noise) {
	// this.noise = noise;
	// }

	public String getSign() {
		// return sign;
		return header.getSign();
	}

	// public void setSign(String sign) {
	// this.sign = sign;
	// }

	public InputStream getInput() {
		return input;
	}

	public String getContentSign() {
		// return contentSign;
		return header.getContentSign();
	}

	// public void setContentSign(String dataSign) {
	// this.contentSign = dataSign;
	// }

	public String getServiceName() {
		// return serviceName;
		return header.getService();
	}

	// public void setServiceName(String serviceName) {
	// this.serviceName = serviceName;
	// }

	public String getTag() {
		// return tag;
		return header.getTag();
	}

	// public void setTag(String tag) {
	// this.tag = tag;
	// }

	public String getChannel() {
		// return channel;
		return header.getChannel();
	}

	// public void setChannel(String channel) {
	// this.channel = channel;
	// }
}
