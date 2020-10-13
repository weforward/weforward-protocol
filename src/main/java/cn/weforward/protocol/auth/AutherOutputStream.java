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

import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.io.OutputStreamNio;
import cn.weforward.protocol.AccessLoader;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Header.HeaderOutput;
import cn.weforward.protocol.exception.AuthException;

/**
 * 封装成输出流形式的验证器。
 * <p>
 * 先验证头信息，再write内容，最终finish完成验证。
 * <p>
 * 注意，验证器是非线程安全的，并发调用write将导致错误
 * 
 * @author zhangpengji
 *
 */
public abstract class AutherOutputStream extends OutputStream implements OutputStreamNio {

	/** 模式 - 生成内容的验证信息 */
	public static final int MODE_ENCODE = 1;
	/** 模式 - 校验内容的验证信息 */
	public static final int MODE_DECODE = 2;

	protected int m_Mode;
	protected AccessLoader m_AccessLoader;
	protected boolean m_IgnoreContent;
	protected volatile boolean m_Cancel;

	protected Header m_Header;
	protected HeaderOutput m_HeaderOutput;
	protected OutputStream m_Forward;

	public static final AutherOutputStream getInstance(String authType) {
		if (Header.AUTH_TYPE_NONE.equals(authType)) {
			return new NoneOutputStream();
		}
		if (Header.AUTH_TYPE_SHA2.equals(authType)) {
			return new Sha2OutputStream();
		}
		if (Header.AUTH_TYPE_SIGN.equals(authType)) {
			return new SignOutputStream();
		}
		return null;
	}

	/**
	 * 初始化
	 * 
	 * @param mode   模式，如：MODE_ENCODE
	 * @param loader
	 */
	public void init(int mode, AccessLoader loader) {
		init(mode, loader, false);
	}

	/**
	 * 初始化
	 * 
	 * @param mode          模式，如：MODE_ENCODE
	 * @param loader
	 * @param ignoreContent 可以指定是否忽略内容，但最终取决于验证器的实现
	 */
	public void init(int mode, AccessLoader loader, boolean ignoreContent) {
		if (mode != MODE_DECODE && mode != MODE_ENCODE) {
			throw new IllegalArgumentException("无效模式：" + mode);
		}
		m_Mode = mode;
		m_AccessLoader = loader;
		m_IgnoreContent = ignoreContent;

		onInit();
	}

	protected void onInit() {
		// 子类可重载
	}

	protected boolean isIgnoreContent() {
		// 子类可重载
		return m_IgnoreContent;
	}

	public boolean isMode(int mode) {
		return mode == m_Mode;
	}

	/**
	 * 验证头信息。
	 * <p>
	 * 验证器将修正AccessId，补全Noise、Sign等信息，并持有此header的引用
	 * 
	 * @param header 头信息
	 */
	public void auth(Header header) throws AuthException {
		if (0 == m_Mode) {
			throw new IllegalStateException("请先初始化");
		}
		m_Header = authHeader(header);
	}

	/**
	 * 返回用于输出的头信息
	 * 
	 * @return 头信息
	 */
	protected Header getHeader() {
		return m_Header;
	}

	protected abstract Header authHeader(Header header) throws AuthException;

	/**
	 * 将验证后的内容转发到此输出流。
	 * <p>
	 * <code>MODE_ENCODE</code>下，验证器完成header后，通过headerOutput输出。<br/>
	 * 验证器将保证头信息在内容之前输出。
	 * <p>
	 * 在write前未调用此方法，内容将被丢弃。
	 * 
	 * @param headerOutput 输出头信息（可空）
	 * @param forward      输出验证后的内容
	 */
	public void setTransferTo(HeaderOutput headerOutput, OutputStream forward) throws IOException {
		if (isMode(MODE_ENCODE)) {
			m_HeaderOutput = headerOutput;
		}
		m_Forward = forward;
	}

	/**
	 * 验证内容已输出完毕
	 */
	public void finish() throws IOException {
		if (m_Cancel) {
			throw new IOException("已经取消/关闭了");
			// return;
		}
		if (isIgnoreContent()) {
			return;
		}
		try {
			doFinal();
		} catch (AuthException e) {
			throw new AuthExceptionWrap(e);
		}
	}

	protected abstract void doFinal() throws AuthException, IOException;

	@Override
	public int write(ByteBuffer src) throws IOException {
		if (isIgnoreContent()) {
			return forward(src);
		}
		try {
			return update(src);
		} catch (AuthException e) {
			throw new AuthExceptionWrap(e);
		}
	}

	protected abstract int update(ByteBuffer src) throws IOException, AuthException;

	protected int forward(ByteBuffer src) throws IOException {
		writeHeader();
		if (null != m_Forward) {
			if (m_Forward instanceof OutputStreamNio) {
				return ((OutputStreamNio) m_Forward).write(src);
			} else {
				BytesOutputStream.transfer(src, m_Forward, -1);
			}
		}
		return 0;
	}

	protected void writeHeader() throws IOException {
		if (null != m_HeaderOutput) {
			Header header = getHeader();
			if (null != header) {
				m_HeaderOutput.writeHeader(header);
				m_HeaderOutput = null;
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (isIgnoreContent()) {
			forward(b);
			return;
		}
		try {
			update(b);
		} catch (AuthException e) {
			throw new AuthExceptionWrap(e);
		}
	}

	protected abstract void update(int b) throws IOException, AuthException;

	protected void forward(int b) throws IOException {
		writeHeader();
		if (null != m_Forward) {
			m_Forward.write(b);
		}
	}

	@Override
	public void write(byte[] data, int off, int len) throws IOException {
		if (isIgnoreContent()) {
			forward(data, off, len);
			return;
		}
		try {
			update(data, off, len);
		} catch (AuthException e) {
			throw new AuthExceptionWrap(e);
		}
	}

	@Override
	public int write(InputStream src) throws IOException {
		return write(src, -1);
	}

	/**
	 * 写入
	 * 
	 * @param src
	 * @param count 从in读取的字节总数，-1为不限制
	 * @throws IOException
	 */
	public int write(InputStream src, int count) throws IOException {
		if (isIgnoreContent()) {
			return forward(src, count);
		}
		try {
			return update(src, count);
		} catch (AuthException e) {
			throw new AuthExceptionWrap(e);
		}
	}

	protected abstract int update(InputStream src, int count) throws IOException, AuthException;

	protected int forward(InputStream src, int count) throws IOException {
		writeHeader();
		if (null == m_Forward) {
			return 0;
		}
		if (m_Forward instanceof OutputStreamNio) {
			return ((OutputStreamNio) m_Forward).write(src);
		}
		return BytesOutputStream.transfer(src, m_Forward, count);
	}

	protected abstract void update(byte[] data, int off, int len) throws AuthException, IOException;

	protected void forward(byte[] data, int off, int len) throws IOException {
		writeHeader();
		if (null != m_Forward) {
			m_Forward.write(data, off, len);
		}
	}

	@Override
	public void cancel() throws IOException {
		m_Cancel = true;
		if (null != m_Forward && m_Forward instanceof OutputStreamNio) {
			((OutputStreamNio) m_Forward).cancel();
		}
	}

	@Override
	public void close() throws IOException {
		m_Cancel = true;
		if (null != m_Forward) {
			m_Forward.close();
		}
	}
}
