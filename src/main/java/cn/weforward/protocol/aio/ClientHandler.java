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
package cn.weforward.protocol.aio;

import java.io.IOException;
import java.io.OutputStream;

/**
 * （异步IO）客户端处理器
 * 
 * @author liangyi
 *
 */
public interface ClientHandler {

	/**
	 * 连接失败
	 */
	void connectFail();

	/**
	 * 连接已就绪，可以开始提交请求
	 */
	void established();

	/**
	 * 请求已完成
	 */
	void requestCompleted();

	/**
	 * 请求被中断（未完成）
	 */
	void requestAbort();

	/**
	 * 响应头已就绪
	 */
	void responseHeader();

	/**
	 * 有响应数据就绪（可接收）
	 * 
	 * @param available
	 *            可读取的数据量（字节）
	 */
	void prepared(int available);

	/**
	 * 响应已完整
	 */
	void responseCompleted();

	/**
	 * 响应超时。
	 * <p>
	 * 由ClientHandler控制是否结束请求
	 */
	void responseTimeout();

	/**
	 * 直接传输异常
	 * 
	 * @param e
	 *            发生的异常
	 * @param msg
	 *            将传输的消息/数据
	 * @param writer
	 *            将传输到输出流
	 */
	void errorResponseTransferTo(IOException e, Object msg, OutputStream writer);

	/**
	 * 用于标记同步操作
	 */
	final static ClientHandler SYNC = new ClientHandler() {

		@Override
		public void connectFail() {
		}

		@Override
		public void established() {
		}
		
		@Override
		public void requestCompleted() {
		}

		@Override
		public void requestAbort() {
		}

		@Override
		public void responseHeader() {
		}

		@Override
		public void prepared(int available) {
		}

		@Override
		public void responseCompleted() {
		}

		@Override
		public void errorResponseTransferTo(IOException e, Object msg, OutputStream writer) {
		}

		@Override
		public void responseTimeout() {
		}
	};
}
