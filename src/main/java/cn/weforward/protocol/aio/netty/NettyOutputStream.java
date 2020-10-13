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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.io.OutputStreamNio;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakTracker;

/**
 * 基于Netty ByteBuf的输出流
 * 
 * @author liangyi
 *
 */
public abstract class NettyOutputStream extends OutputStream
		implements OutputStreamNio, WritableByteChannel, Leakable {
	static final Logger _Logger = LoggerFactory.getLogger(NettyOutputStream.class);

	/** 单字节写入临时缓冲区 */
	protected ByteBuf m_Buffer;
	// /** 写入流监察者 */
	// protected OutputStream m_Observer;

	ResourceLeakTracker<Leakable> m_Leak;

	/**
	 * 分配ByteBuf
	 * 
	 * @param len
	 *            长度
	 */
	protected abstract ByteBuf allocBuffer(int len);

	/**
	 * 确认流是打开的状态
	 * 
	 * @throws IOException
	 */
	protected abstract void ensureOpen() throws IOException;

	/**
	 * 写出
	 * 
	 * @param src
	 *            要写出内容的ByteBuf
	 * @throws IOException
	 */
	public abstract void write(ByteBuf src) throws IOException;

	protected NettyOutputStream() {
		if (ResourceLeakDetector.Level.ADVANCED == ResourceLeakDetector.getLevel()
				|| ResourceLeakDetector.Level.PARANOID == ResourceLeakDetector.getLevel()) {
			m_Leak = _LeakDetector.track(this);
		}
	}

	protected NettyOutputStream(boolean noLeak) {
	}

	@Override
	public void touch(Object hint) {
		if (null != m_Leak) {
			m_Leak.record(hint);
		}
	}

	/**
	 * 刷写临时缓冲区
	 * 
	 * @throws IOException
	 */
	synchronized protected void flushBuffer() throws IOException {
		ensureOpen();
		ByteBuf buf = m_Buffer;
		// if (null != buf && buf.isReadable()) {
		if (null != buf) {
			m_Buffer = null;
			try {
				if (buf.isReadable()) {
					write(buf);
				}
			} finally {
				buf.release();
			}
		}
	}

	ByteBuf getBuffer(int capacity) throws IOException {
		ensureOpen();
		ByteBuf buf = m_Buffer;
		if (null != buf) {
			// 有临时缓冲区
			if (buf.writableBytes() >= capacity) {
				// 临时缓冲区空间足够
				return buf;
			}
			// 临时缓冲区空间不足，写出
			m_Buffer = null;
			try {
				write(buf);
			} finally {
				buf.release();
			}
		}
		if (capacity < 256) {
			capacity = 256;
		}
		buf = allocBuffer(capacity);
		m_Buffer = buf;
		return buf;
	}

	// @Override
	// public OutputStream setObserver(OutputStream observer) throws IOException
	// {
	// if (!isOpen()) {
	// throw new EOFException("已关闭");
	// }
	// OutputStream older = m_Observer;
	// m_Observer = observer;
	// return older;
	// }
	//
	// protected void observe(byte data) {
	// OutputStream observer = m_Observer;
	// if (null != observer) {
	// try {
	// observer.write(data);
	// } catch (IOException e) {
	// // 直接略过
	// _Logger.warn(String.valueOf(observer), e);
	// }
	// }
	// }
	//
	// protected void observe(byte[] data, int off, int len) {
	// OutputStream observer = m_Observer;
	// if (null != observer) {
	// try {
	// observer.write(data, off, len);
	// } catch (IOException e) {
	// // 直接略过
	// _Logger.warn(String.valueOf(observer), e);
	// }
	// }
	// }

	@Override
	synchronized public void write(byte[] b, int off, int len) throws IOException {
		ByteBuf buf;
		int count;
		// observe(b, off, len);
		while (len > 0) {
			buf = getBuffer(len);
			count = buf.writableBytes();
			if (count > len) {
				count = len;
			}
			len -= count;
			buf.writeBytes(b, off, count);
			off += count;
		}
	}

	@Override
	synchronized public int write(ByteBuffer src) throws IOException {
		int len = src.remaining();
		if (len < 1) {
			return 0;
		}
		// OutputStream observer = m_Observer;
		// if (null != observer) {
		// try {
		// int limit = src.limit();
		// for (int i = src.position(); i < limit; i++) {
		// observer.write(src.get(i));
		// }
		// // src.mark();
		// // while (src.hasRemaining()) {
		// // observer.write(src.get());
		// // }
		// // src.reset();
		// } catch (IOException e) {
		// // 直接略过
		// _Logger.warn(String.valueOf(observer), e);
		// }
		// }
		ByteBuf buf = getBuffer(len);
		buf.writeBytes(src);
		return len;
	}

	@Override
	synchronized public void write(int b) throws IOException {
		// observe((byte) b);
		ByteBuf buf = getBuffer(1);
		buf.writeByte(b);
	}

	public int write(InputStream src) throws IOException {
		return BytesOutputStream.transfer(src, this, -1);
	}

	@Override
	public void flush() throws IOException {
		flushBuffer();
	}

	@Override
	public void close() throws IOException {
		flushBuffer();
		cleanup();
	}

	@Override
	public void cancel() throws IOException {
		// if (null != m_Buffer) {
		// m_Buffer.release();
		// m_Buffer = null;
		// }
		cleanup();
	}

	synchronized protected void cleanup() {
		ByteBuf buf = m_Buffer;
		if (null != buf) {
			m_Buffer = null;
			buf.release();
		}
		if (null != m_Leak) {
			m_Leak.close(this);
		}
	}

	// @Override
	// protected void finalize() throws Throwable {
	// super.finalize();
	// if (null != m_Buffer) {
	// cleanup();
	// _Logger.warn("流没有关闭或取消：" + this);
	// }
	// }

	@Override
	public String toString() {
		if (null != m_Buffer) {
			return getClass().getSimpleName() + "." + hashCode() + " " + m_Buffer;
		}
		return getClass().getSimpleName() + "." + hashCode();
	}

	/**
	 * 把OutputStream封装为NettyOutputStream
	 * 
	 * @param source
	 *            要封装的OutputStream
	 * @return 封装后的NettyOutputStream
	 */
	public static NettyOutputStream wrap(OutputStream source) {
		if (source instanceof NettyOutputStream) {
			return (NettyOutputStream) source;
		}
		if (source instanceof OutputStreamNio) {
			return new WrapNio((OutputStreamNio) source);
		}
		return new Wrap(source);
	}

	/** 标记结束中 */
	static public final NettyOutputStream _pending = new Fake();
	/** 标记已结束 */
	static public final NettyOutputStream _end = new Fake();

	/**
	 * 标记状态用
	 * 
	 * @author liangyi
	 *
	 */
	static class Fake extends NettyOutputStream {
		Fake() {
			super(true);
		}

		@Override
		public boolean isOpen() {
			return false;
		}

		@Override
		public int write(ByteBuffer src) throws IOException {
			throw new EOFException("不可用");
		}

		@Override
		public void cancel() throws IOException {
		}

		@Override
		public void write(int b) throws IOException {
			throw new EOFException("不可用");
		}

		@Override
		public void write(ByteBuf src) throws IOException {
			throw new EOFException("不可用");
		}

		@Override
		protected ByteBuf allocBuffer(int len) {
			return null;
		}

		public int write(InputStream src) throws IOException {
			throw new EOFException("不可用");
		}

		@Override
		protected void ensureOpen() throws IOException {
		}
	}

	/**
	 * 封装OutputStream
	 * 
	 * @author liangyi
	 *
	 */
	static class Wrap extends NettyOutputStream {
		OutputStream m_Output;

		public Wrap(OutputStream source) {
			super(true);
			m_Output = source;
		}

		@Override
		public void cancel() throws IOException {
			m_Output.close();
		}

		@Override
		protected ByteBuf allocBuffer(int len) {
			return PooledByteBufAllocator.DEFAULT.heapBuffer(len);
		}

		@Override
		public void write(ByteBuf src) throws IOException {
			while (src.isReadable()) {
				m_Output.write(src.readByte());
			}
		}

		@Override
		public int write(ByteBuffer src) throws IOException {
			int i = 0;
			while (src.hasRemaining()) {
				m_Output.write(src.get());
				++i;
			}
			return i;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			m_Output.write(b, off, len);
		}

		@Override
		public void write(int b) throws IOException {
			m_Output.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			m_Output.write(b);
		}

		@Override
		public void flush() throws IOException {
			m_Output.flush();
		}

		@Override
		public void close() throws IOException {
			OutputStream out = m_Output;
			if (null != out) {
				m_Output = null;
				out.close();
			}
		}

		@Override
		public boolean isOpen() {
			return null != m_Output;
		}

		@Override
		protected void ensureOpen() throws IOException {
			if (!isOpen()) {
				throw new IOException("closed");
			}
		}
	}

	/**
	 * 封装OutputStreamNio
	 * 
	 * @author liangyi
	 *
	 */
	static class WrapNio extends NettyOutputStream {
		OutputStreamNio m_Output;

		public WrapNio(OutputStreamNio source) {
			super(true);
			m_Output = source;
		}

		@Override
		public void cancel() throws IOException {
			m_Output.cancel();
		}

		@Override
		protected ByteBuf allocBuffer(int len) {
			return PooledByteBufAllocator.DEFAULT.heapBuffer(len);
		}

		@Override
		public void write(ByteBuf src) throws IOException {
			flushBuffer();
			m_Output.write(src.nioBuffer());
		}

		@Override
		public int write(ByteBuffer src) throws IOException {
			flushBuffer();
			return m_Output.write(src);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			flushBuffer();
			m_Output.write(b, off, len);
		}

		@Override
		public void close() throws IOException {
			OutputStreamNio out = m_Output;
			if (null != out) {
				flushBuffer();
				m_Output = null;
				out.close();
			}
		}

		@Override
		public boolean isOpen() {
			return null != m_Output;
		}

		public int write(InputStream src) throws IOException {
			return m_Output.write(src);
		}

		@Override
		protected void ensureOpen() throws IOException {
			if (!isOpen()) {
				throw new IOException("closed");
			}
		}
	}
}
