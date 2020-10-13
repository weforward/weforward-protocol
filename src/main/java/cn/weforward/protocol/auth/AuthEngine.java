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

import cn.weforward.protocol.Header;
import cn.weforward.protocol.exception.AuthException;

/**
 * 验证引擎
 * 
 * @author zhangpengji
 *
 */
public interface AuthEngine {

	/**
	 * 验证方式。如：{@link Header#AUTH_TYPE_SIGN}
	 * 
	 * @return 验证方式
	 */
	String getType();

	/**
	 * 编码/加密
	 */
	Output encode(Input in) throws AuthException;

	/**
	 * 解码/解密
	 */
	Output decode(Input in) throws AuthException;

	/**
	 * 输入的参数
	 * 
	 * @author zhangpengji
	 *
	 */
	class Input {
		public String serviceName;
		public String accessId;
		public byte[] accessKey;
		public String noise;
		public String tag;
		public String channel;
		public String contentSign;
		public String sign;
		public byte[] data;
		public int dataOffset;
		public int dataLength;
	}

	/**
	 * 输出的返回值
	 * 
	 * @author zhangpengji
	 *
	 */
	class Output {
		public String noise;
		public String contentSign;
		public String sign;
		public byte[] data;
		public int dataOffset;
		public int dataLength;
	}
}
