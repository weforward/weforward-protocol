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
 * {@link Header#AUTH_TYPE_NONE}的验证器
 * 
 * @author zhangpengji
 *
 */
public class NoneAuthEngine implements AuthEngine {

	@Override
	public String getType() {
		return Header.AUTH_TYPE_NONE;
	}

	@Override
	public Output encode(Input in) throws AuthException {
		// 什么都不做
		Output out = new Output();
		out.data = in.data;
		out.dataOffset = in.dataOffset;
		out.dataLength = in.dataLength;
		return out;
	}

	@Override
	public Output decode(Input in) throws AuthException {
		return encode(in);
	}

}
