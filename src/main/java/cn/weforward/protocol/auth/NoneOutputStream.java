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

import cn.weforward.protocol.Header;
import cn.weforward.protocol.exception.AuthException;

/**
 * {@link Header#AUTH_TYPE_NONE}验证器
 * 
 * @author zhangpengji
 *
 */
public class NoneOutputStream extends AutherOutputStream {

	@Override
	protected Header authHeader(Header header) throws AuthException {
		header.setAccessId(null);
		return header;
	}

	@Override
	protected boolean isIgnoreContent() {
		return true;
	}

	@Override
	protected void doFinal() throws AuthException {

	}

	@Override
	protected int update(ByteBuffer src) throws IOException, AuthException {
		return forward(src);
	}

	@Override
	protected void update(int b) throws AuthException, IOException {
		forward(b);
	}

	@Override
	protected void update(byte[] data, int off, int len) throws AuthException, IOException {
		forward(data, off, len);
	}

	@Override
	protected int update(InputStream src, int count) throws IOException, AuthException {
		return forward(src, count);
	}

}
