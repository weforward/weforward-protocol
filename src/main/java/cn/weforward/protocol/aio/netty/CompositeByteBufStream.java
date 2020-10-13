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

import java.io.IOException;
import java.io.InputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakTracker;

/**
 * CompositeByteBuf的流封装
 * 
 * @author liangyi
 *
 */
public class CompositeByteBufStream implements ByteBufStream, Leakable {
	protected CompositeByteBuf m_Buffer;
	protected boolean m_Completed;

	ResourceLeakTracker<Leakable> m_Leak;

	public CompositeByteBufStream(CompositeByteBuf buffer) {
		m_Buffer = buffer;
		if (ResourceLeakDetector.Level.ADVANCED == ResourceLeakDetector.getLevel()
				|| ResourceLeakDetector.Level.PARANOID == ResourceLeakDetector.getLevel()) {
			m_Leak = _LeakDetector.track(this);
		}
	}

	@Override
	synchronized public void readable(ByteBuf data) {
		m_Buffer.addComponent(true, data.retain());
	}

	@Override
	public int available() {
		return null == m_Buffer ? 0 : m_Buffer.readableBytes();
	}

	@Override
	synchronized public void completed() {
		if (null != m_Buffer) {
			m_Completed = true;
		}
	}

	@Override
	synchronized public void abort() {
		if (null != m_Buffer) {
			m_Buffer.release();
			m_Buffer = null;
			cleanup();
		}
	}

	@Override
	public void touch(Object hint) {
		if (null != m_Leak) {
			m_Leak.record(hint);
		}
	}

	synchronized public ByteBuf detach() {
		CompositeByteBuf buffer = m_Buffer;
		m_Buffer = null;
		touch(null);
		cleanup();
		return buffer;
	}

	synchronized public ByteBufInput detachToStream() {
		ByteBufInput stream = new ByteBufInput(detach(), isCompleted());
		return stream;
	}

	public boolean isCompleted() {
		return m_Completed;
	}

	/**
	 * 快照当前缓冲区已有内容到流（很可能只有部分内容）
	 */
	synchronized public InputStream snapshot() {
		if (null == m_Buffer || !m_Buffer.isReadable()) {
			// 没内容
			return ByteBufInput._empty;
		}
		touch(null);
		ByteBufInput stream = new ByteBufInput(m_Buffer.retainedDuplicate(), true);
		return stream;
	}

	/**
	 * 复制缓冲区且转包装为流
	 * 
	 * @param skipBytes
	 *            >0则跳过已在缓冲区的部分内容
	 * @return 包装缓冲区的流
	 * @throws IOException
	 */
	synchronized public ByteBufInput toStream(int skipBytes) throws IOException {
		int readableBytes = available();
		if (skipBytes > 0 && skipBytes > readableBytes) {
			throw new IOException("超过范围" + skipBytes + ">" + readableBytes);
		}
		CompositeByteBuf mirrorBuffer = Unpooled.compositeBuffer();
		for (int i = 0; i < m_Buffer.numComponents(); i++) {
			mirrorBuffer.addComponent(true, m_Buffer.component(i).retain());
		}
		if (skipBytes > 0) {
			mirrorBuffer.skipBytes(skipBytes);
		}
		return new ByteBufInput(mirrorBuffer, isCompleted());
	}

	public void skipBytes(int skipBytes) {
		m_Buffer.skipBytes(skipBytes);
	}

	protected void cleanup() {
		if (null != m_Leak) {
			m_Leak.close(this);
		}
	}
}
