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
package cn.weforward.protocol.aio.netty;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.io.InputStreamNio;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakTracker;

/**
 * 封装ByteBuf（能边输出转输入）
 * 
 * @author liangyi
 *
 */
public class ByteBufInput extends InputStream implements InputStreamNio, ByteBufStream, Leakable {
	static final Logger _Logger = LoggerFactory.getLogger(ByteBufInput.class);

	/** 数据缓冲区 */
	protected ByteBuf m_Buffer;
	/** 数据是否完整 */
	protected boolean m_Completed;

	ResourceLeakTracker<Leakable> m_Leak;

	public ByteBufInput(ByteBuf byteBuf, boolean completed) {
		m_Buffer = byteBuf;
		m_Completed = completed;
		if (ResourceLeakDetector.Level.ADVANCED == ResourceLeakDetector.getLevel()
				|| ResourceLeakDetector.Level.PARANOID == ResourceLeakDetector.getLevel()) {
			m_Leak = _LeakDetector.track(this);
		}
	}

	private ByteBuf getByteBuf() throws EOFException {
		ByteBuf buf = m_Buffer;
		if (null == buf) {
			throw new EOFException("closed");
		}
		return buf;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		ByteBuf buf = getByteBuf();
		synchronized (buf) {
			if (!readying(buf)) {
				return -1;
			}
			if (len > buf.readableBytes()) {
				len = buf.readableBytes();
			}
			buf.readBytes(b, off, len);
			return len;
		}
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		ByteBuf buf = getByteBuf();
		synchronized (buf) {
			if (!readying(buf)) {
				return -1;
			}
			if (dst.remaining() > buf.readableBytes()) {
				dst.limit(dst.position() + buf.readableBytes());
			}
			int len = dst.remaining();
			buf.readBytes(dst);
			return len;
		}
	}

	@Override
	public long skip(long n) throws IOException {
		ByteBuf buf = getByteBuf();
		synchronized (buf) {
			if (!readying(buf)) {
				return -1;
			}
			if (n > buf.readableBytes()) {
				n = buf.readableBytes();
			}
			buf.skipBytes((int) n);
		}
		return n;
	}

	@Override
	public int available() throws IOException {
		ByteBuf buf = getByteBuf();
		synchronized (buf) {
			return buf.readableBytes();
		}
	}

	@Override
	public void close() throws IOException {
		// if (null == m_Buffer) {
		// throw new IOException("closed");
		// }
		end();
	}

	@Override
	public int read() throws IOException {
		ByteBuf buf = getByteBuf();
		synchronized (buf) {
			if (!readying(buf)) {
				return -1;
			}
			// return buf.readByte();
			byte ret = buf.readByte();
			return ret < 0 ? 0x100 + ret : ret;
		}
	}

	/**
	 * 等待数据就绪，子类覆盖此方法加上等待数据的阻塞
	 * 
	 * @return 返回true为有数据可读，否则为已结束
	 * @throws IOException
	 */
	protected boolean readying(ByteBuf buf) throws IOException {
		for (;;) {
			if (null == m_Buffer) {
				throw new EOFException("closed");
			}
			if (buf.readableBytes() > 0) {
				return true;
			}
			if (m_Completed) {
				return false;
			}
			try {
				// 阻塞等待
				buf.wait();
				// if (null == m_Buffer) {
				// throw new EOFException("closed");
				// }
				// if (m_Buffer.readableBytes() <= 0) {
				// // 若等到信号还是没数据可读，则认为流结束
				// return false;
				// }
			} catch (InterruptedException e) {
				throw new InterruptedIOException();
			}
		}
	}

	/**
	 * 通知有数据可读
	 */
	public void readable() {
		ByteBuf buf = m_Buffer;
		if (null != buf) {
			synchronized (buf) {
				buf.notify();
			}
		}
	}

	/**
	 * 有数据输入
	 */
	public void readable(ByteBuf data) {
		ByteBuf buf = m_Buffer;
		if (null != buf) {
			synchronized (buf) {
				if (buf instanceof CompositeByteBuf) {
					((CompositeByteBuf) buf).addComponent(true, data.retain());
				} else {
					buf.writeBytes(data);
				}
				buf.notify();
			}
		}
	}

	/**
	 * 数据已完整
	 */
	public void completed() {
		m_Completed = true;
		ByteBuf buf = m_Buffer;
		if (null != buf) {
			synchronized (buf) {
				buf.notify();
			}
		}
	}

	public boolean isCompleted() {
		return m_Completed;
	}

	public void abort() {
		end();
	}

	@Override
	public void touch(Object hint) {
		if (null != m_Leak) {
			m_Leak.record(hint);
		}
	}

	public void end() {
		ByteBuf buf = m_Buffer;
		if (null != buf) {
			synchronized (buf) {
				if (buf == m_Buffer) {
					m_Buffer = null;
				}
				buf.release();
				buf.notifyAll();
			}
			if (null != m_Leak) {
				m_Leak.close(this);
			}
		}
	}

	// @Override
	// protected void finalize() throws Throwable {
	// super.finalize();
	//
	// String trace = m_Trace;
	// m_Trace = null;
	//
	// ByteBuf buf = m_Buffer;
	// if (null != buf) {
	// if (StringUtil.isEmpty(trace)) {
	// _Logger.warn("流没有关闭：" + buf);
	// } else {
	// _Logger.warn("流没有关闭：" + buf + "\ntrace:\n" + trace);
	// }
	//
	// m_Buffer = null;
	// buf.release();
	// }
	// }

	public InputStreamNio duplicate() throws IOException {
		if (!isCompleted()) {
			throw new IOException("数据未完整，无法创建副本");
		}
		return new ByteBufInput(m_Buffer.retainedDuplicate(), true);
	}

	/**
	 * 无内容流
	 */
	static public InputStream _empty = new InputStream() {
		@Override
		public int read() throws IOException {
			return -1;
		}
	};

	/**
	 * 标记已完整
	 */
	static public ByteBufInput _completed = new ByteBufInput(Unpooled.buffer(0), true) {

		@Override
		public void readable() {
		}

		@Override
		public void completed() {
		}

		@Override
		public void end() {
		}

		@Override
		public String toString() {
			return "_completed";
		}
	};

	/**
	 * 标记中止
	 */
	static public ByteBufInput _aborted = new ByteBufInput(Unpooled.buffer(0), false) {

		@Override
		public void readable() {
		}

		@Override
		public void completed() {
		}

		@Override
		public void end() {
		}

		@Override
		public String toString() {
			return "_aborted";
		}
	};
}
