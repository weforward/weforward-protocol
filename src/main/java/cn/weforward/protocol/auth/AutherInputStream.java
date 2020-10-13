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
import java.nio.BufferOverflowException;

import cn.weforward.common.util.Bytes;

/**
 * 封装成输入流形式的验证器。
 * 
 * @author zhangpengji
 *
 */
public class AutherInputStream extends InputStream {

	InputStream m_InputStream;
	AutherOutputStream m_AutherOutputStream;
	byte[] m_RemainBuf;
	int m_RemainBufReadPos;
	int m_RemainBufWritePos;
	int m_RemainBufReadable;
	byte[] m_InputBuf;
	int m_InputBufStart;
	int m_InputBufEnd;

	OutputStream m_Forward = new OutputStream() {

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			int fill = 0;
			if (null != m_InputBuf && m_InputBufStart < m_InputBufEnd) {
				fill = m_InputBufEnd - m_InputBufStart;
				if (fill > len) {
					fill = len;
				}
				System.arraycopy(b, off, m_InputBuf, m_InputBufStart, fill);
				m_InputBufStart += fill;
			}
			if (fill >= len) {
				return;
			}
			putRemainBuf(b, off + fill, len - fill);
		};

		@Override
		public void write(int b) throws IOException {
			if (null != m_InputBuf && m_InputBufStart < m_InputBufEnd) {
				m_InputBuf[m_InputBufStart++] = (byte) b;
				return;
			}
			putRemainBuf((byte) b);
		}
	};

	/**
	 * 使用已验证头信息的AutherOutputStream构造
	 * 
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public AutherInputStream(InputStream input, AutherOutputStream output) throws IOException {
		m_InputStream = input;
		m_AutherOutputStream = output;
		m_AutherOutputStream.setTransferTo(null, m_Forward);
		m_RemainBuf = new byte[10 * 1024];
		m_RemainBufReadPos = 0;
		m_RemainBufWritePos = 0;
		m_RemainBufReadable = 0;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int ret = readRemainBuf(b, off, len);
		if (-1 != ret || null == m_AutherOutputStream) {
			return ret;
		}
		byte[] buf = Bytes.Pool._8k.poll();
		m_InputBuf = b;
		m_InputBufStart = off;
		m_InputBufEnd = off + len;
		try {
			ret = m_InputStream.read(buf, 0, buf.length);
			if (-1 == ret) {
				m_AutherOutputStream.finish();
				m_AutherOutputStream = null;
			} else {
				m_AutherOutputStream.write(buf, 0, ret);
			}
			return m_InputBufStart - off;
		} finally {
			m_InputBuf = null;
			m_InputBufStart = 0;
			m_InputBufEnd = 0;
			Bytes.Pool._8k.offer(buf);
		}
	}

	@Override
	public int read() throws IOException {
		int ret = getRemainBuf();
		if (-1 != ret || null == m_AutherOutputStream) {
			return ret;
		}
		byte[] buf = Bytes.Pool._1k.poll();
		try {
			ret = m_InputStream.read(buf, 0, buf.length);
			if (-1 == ret) {
				m_AutherOutputStream.finish();
				m_AutherOutputStream = null;
			} else {
				m_AutherOutputStream.write(buf, 0, ret);
			}
			return getRemainBuf();
		} finally {
			Bytes.Pool._1k.offer(buf);
		}
	}

	private int getRemainBuf() {
		if (0 == m_RemainBufReadable) {
			return -1;
		}
		byte b = m_RemainBuf[m_RemainBufReadPos++];
		m_RemainBufReadable--;
		if (m_RemainBufReadPos >= m_RemainBuf.length) {
			m_RemainBufReadPos = 0;
		}
		return b < 0 ? 0x100 + b : b;
	}

	private void putRemainBuf(byte b) {
		if (m_RemainBufReadable >= m_RemainBuf.length) {
			throw new BufferOverflowException();
		}
		m_RemainBuf[m_RemainBufWritePos++] = b;
		m_RemainBufReadable++;
		if (m_RemainBufWritePos >= m_RemainBuf.length) {
			m_RemainBufWritePos = 0;
		}
	}

	private int readRemainBuf(byte[] b, int off, int len) {
		if (0 == m_RemainBufReadable) {
			return -1;
		}
		int count = Math.min(m_RemainBufReadable, len);
		if (m_RemainBufReadable > (m_RemainBuf.length - m_RemainBufReadPos)) {
			int readable = m_RemainBuf.length - m_RemainBufReadPos;
			if (readable >= len) {
				System.arraycopy(m_RemainBuf, m_RemainBufReadPos, b, off, len);
				m_RemainBufReadable -= len;
				m_RemainBufReadPos += len;
			} else {
				System.arraycopy(m_RemainBuf, m_RemainBufReadPos, b, off, readable);
				m_RemainBufReadable -= readable;
				off += readable;
				len -= readable;
				readable = (m_RemainBufReadable > len) ? len : m_RemainBufReadable;
				System.arraycopy(m_RemainBuf, 0, b, off, readable);
				m_RemainBufReadable -= readable;
				m_RemainBufReadPos = readable;
			}
		} else {
			int readable = (m_RemainBufReadable > len) ? len : m_RemainBufReadable;
			System.arraycopy(m_RemainBuf, 0, b, off, readable);
			m_RemainBufReadable -= readable;
			m_RemainBufReadPos = readable;
		}
		if (m_RemainBufReadPos >= m_RemainBuf.length) {
			m_RemainBufReadPos = 0;
		}
		return count;
	}

	private void putRemainBuf(byte[] b, int off, int len) {
		if (m_RemainBuf.length - m_RemainBufReadable < len) {
			throw new BufferOverflowException();
		}
		if (len > (m_RemainBuf.length - m_RemainBufWritePos)) {
			int remain = m_RemainBuf.length - m_RemainBufWritePos;
			System.arraycopy(b, off, m_RemainBuf, m_RemainBufWritePos, remain);
			System.arraycopy(b, off + remain, m_RemainBuf, 0, len - remain);
			m_RemainBufWritePos = len - remain;
		} else {
			System.arraycopy(b, off, m_RemainBuf, m_RemainBufWritePos, len);
			m_RemainBufWritePos += len;
		}
		if (m_RemainBufWritePos >= m_RemainBuf.length) {
			m_RemainBufWritePos = 0;
		}
		m_RemainBufReadable += len;
	}

	@Override
	public void close() throws IOException {
		super.close();
		m_RemainBuf = null;
		if (null != m_AutherOutputStream) {
			m_AutherOutputStream.close();
		}
		m_InputStream.close();
	}
}
