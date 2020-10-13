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

import io.netty.buffer.ByteBuf;

/**
 * 由ByteBuf承载的边进边出流
 * 
 * @author liangyi
 *
 */
public interface ByteBufStream {
	/**
	 * 有数据进入
	 * 
	 * @param data
	 *            要进入的数据
	 * @throws IOException
	 */
	void readable(ByteBuf data) throws IOException;

	/**
	 * 数据已（进入）完整
	 */
	void completed();

	/**
	 * 中止
	 */
	void abort();

	/**
	 * （当前）可读字节数
	 */
	int available() throws IOException;

	/**
	 * 是否完整的数据在缓冲区中
	 */
	boolean isCompleted();
}
