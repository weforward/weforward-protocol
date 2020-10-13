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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * （异步IO）服务端处理器
 * 
 * @author liangyi
 *
 */
public interface ServerHandler {
	/**
	 * 请求头已就绪
	 */
	void requestHeader();

	/**
	 * 有请求数据就绪（可接收）
	 * 
	 * @param available 可读取的数据量（字节）
	 */
	void prepared(int available);

	/**
	 * 请求被中断（未完成）
	 */
	void requestAbort();

	/**
	 * 请求已完整就绪
	 */
	void requestCompleted();

	/**
	 * 响应超时
	 */
	void responseTimeout();

	/**
	 * 响应结束
	 */
	void responseCompleted();

	/**
	 * 直接传输异常
	 * 
	 * @param e      发生的异常
	 * @param msg    将传输的消息/数据
	 * @param writer 将传输到输出流
	 */
	void errorRequestTransferTo(IOException e, Object msg, OutputStream writer);

	/**
	 * 用于标记为初始化
	 */
	final static ServerHandler _init = new ServerHandler() {
		final Logger _Logger = LoggerFactory.getLogger("ServerHandler._init");

		@Override
		public void requestHeader() {
			if (_Logger.isDebugEnabled()) {
				_Logger.debug("requestHeader");
			}
		}

		@Override
		public void prepared(int available) {
			if (_Logger.isDebugEnabled()) {
				_Logger.debug("prepared:" + available);
			}
		}

		@Override
		public void requestAbort() {
			if (_Logger.isDebugEnabled()) {
				_Logger.debug("requestAbort");
			}
		}

		@Override
		public void requestCompleted() {
			if (_Logger.isDebugEnabled()) {
				_Logger.debug("requestCompleted");
			}
		}

		@Override
		public void responseTimeout() {
			if (_Logger.isDebugEnabled()) {
				_Logger.debug("responseTimeout");
			}
		}

		@Override
		public void responseCompleted() {
			if (_Logger.isDebugEnabled()) {
				_Logger.debug("responseCompleted");
			}
		}

		@Override
		public void errorRequestTransferTo(IOException e, Object msg, OutputStream writer) {
			_Logger.error(String.valueOf(msg), e);
		}
	};
}
