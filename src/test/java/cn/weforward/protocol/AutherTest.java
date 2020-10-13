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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.protocol.auth.AutherInputStream;
import cn.weforward.protocol.auth.AutherOutputStream;

public class AutherTest {

	@Test
	public void input() throws IOException {
		byte[] data = new byte[20 * 1024];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		AutherOutputStream autherOutput = AutherOutputStream.getInstance(Header.AUTH_TYPE_NONE);
		autherOutput.init(AutherOutputStream.MODE_DECODE, null);
		AutherInputStream autherInput = new AutherInputStream(in, autherOutput);
		// BytesOutputStream bos = new BytesOutputStream(autherInput);
		// byte[] data2 = bos.toByteArray();
		// bos.close();
		BytesOutputStream bos = new BytesOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while (-1 != (len = autherInput.read(buf))) {
			bos.write(buf, 0, len);
		}
		byte[] data2 = bos.toByteArray();
		bos.close();
		autherInput.close();
		assertTrue(Arrays.equals(data, data2));
	}

	@Test
	public void input2() throws IOException {
		byte[] data = new byte[20 * 1024];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		AutherOutputStream autherOutput = AutherOutputStream.getInstance(Header.AUTH_TYPE_NONE);
		autherOutput.init(AutherOutputStream.MODE_DECODE, null);
		AutherInputStream autherInput = new AutherInputStream(in, autherOutput);
		// BytesOutputStream bos = new BytesOutputStream(autherInput);
		// byte[] data2 = bos.toByteArray();
		// bos.close();
		BytesOutputStream bos = new BytesOutputStream();
		int ret;
		while (-1 != (ret = autherInput.read())) {
			bos.write(ret);
		}
		byte[] data2 = bos.toByteArray();
		bos.close();
		autherInput.close();
		assertTrue(Arrays.equals(data, data2));
	}
}
