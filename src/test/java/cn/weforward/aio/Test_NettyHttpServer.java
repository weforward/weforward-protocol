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
package cn.weforward.aio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.io.OutputStreamNio;
import cn.weforward.protocol.aio.ServerHandler;
import cn.weforward.protocol.aio.http.HttpContext;
import cn.weforward.protocol.aio.http.HttpHeaders;
import cn.weforward.protocol.aio.http.ServerHandlerFactory;
import cn.weforward.protocol.aio.netty.NettyHttpServer;
import cn.weforward.protocol.aio.netty.NettyOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderNames;

/**
 * 测试 NettyHttpServer
 * 
 * @see NettyHttpServer
 * @author liangyi
 *
 */
public class Test_NettyHttpServer {
	static final Logger _Logger = LoggerFactory.getLogger(Test_NettyHttpServer.class);
	NettyHttpServer server;

	Test_NettyHttpServer() {
		server = new NettyHttpServer(8080);
		server.setName("ww");
		server.setMaxHttpSize(100 * 1024 * 1024);
		server.setIdle(120);
		server.setGzipEnabled(true);
		server.setGzipMinSize(10);
		server.setDebugEnabled(true);
		server.setHandlerFactory(new ServerHandlerFactory() {
			@Override
			public ServerHandler handle(HttpContext httpContext) {
				return new Handler(httpContext);
			}
		});
	}

	public static void main(String args[]) throws IOException {
		Test_NettyHttpServer test = new Test_NettyHttpServer();
		// test.server.run();
		test.server.start();
		System.out.println("'q' key stop");
		while ('q' != System.in.read()) {
		}
		test.server.close();
		System.out.println("done.");
	}

	static int STATE_FAIL = 0x1000;

	static int STATE_VERIFY_HEADER = 0x01;
	static int STATE_VERIFY_HY_REQ = 0x02;
	static int STATE_VERIFY_FORWARD = 0x04;

	/**
	 * 测试处理
	 * 
	 * @author liangyi
	 *
	 */
	class Handler implements ServerHandler {
		HttpContext ctx;
		int state;
		NettyOutputStream forward;

		Handler(HttpContext ctx) {
			this.ctx = ctx;
			forward = new NettyOutputStream() {
				int total = 0;

				@Override
				public void write(ByteBuf buf) throws IOException {
					// System.out.println(buf);
					byte[] bytes = new byte[buf.readableBytes()];
					buf.readBytes(bytes);
					// System.out.println(bytes);
					total += bytes.length;
				}

				@Override
				public void close() throws IOException {
					System.out.println("收到转传数据:" + total);
					// Handler.this.ctx.response(200,
					// "{\"hy_resp\":{\"hy_code\": 0,\"hy_msg\": \"test
					// ok.\"}}".getBytes());
					Handler.this.ctx.setResponseHeader("Content-Type", "application/json");
					OutputStream writer = Handler.this.ctx.openResponseWriter(200, null);
					byte[] result = "{\"hy_resp\":{\"hy_code\": 0,\"hy_msg\": \"test ok.\"}}"
							.getBytes();
					writer.write(result, 0, result.length);
					writer.close();
				}

				@Override
				public void cancel() throws IOException {
					// TODO Auto-generated method stub
				}

				@Override
				protected ByteBuf allocBuffer(int len) {
					return PooledByteBufAllocator.DEFAULT.heapBuffer(len);
				}

				@Override
				public boolean isOpen() {
					return true;
				}

				@Override
				protected void ensureOpen() throws IOException {
					// TODO Auto-generated method stub
					
				}
			};
		}

		void asyncRead() {
			Thread therad = new Thread() {
				@Override
				public void run() {
					InputStream in = null;
					long total = 0;
					try {
						int ret;
						byte[] buf = new byte[4096];
						in = ctx.getRequestStream();
						do {
							ret = in.read(buf);
							if (ret > 0) {
								total += ret;
							}
							// _Logger.info("读取：" + ret + "/" + total);
						} while (ret >= 0);
						in.close();
						in = null;
						_Logger.info("读取（总）：" + total);
						forward.close();
					} catch (Exception e) {
						// e.printStackTrace();
						_Logger.error(String.valueOf(ctx), e);
					} finally {
						if (null != in) {
							try {
								in.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			};
			therad.start();
		}

		boolean verifyHeader(HttpHeaders headers) throws IOException {
			if ("application/json".equalsIgnoreCase(headers.get("Content-Type"))) {
				this.state |= STATE_VERIFY_HEADER;
				// asyncRead();
				String len = headers.get(HttpHeaderNames.CONTENT_LENGTH.toString());
				System.out.println("Content-Length:" + len);
				return true;
			}
			// 不匹配
			this.state = STATE_FAIL;
			ctx.response(200, "{\"hy_resp\":{\"hy_code\": 1001,\"hy_msg\":\"Access Id invaild\"}}"
					.getBytes());
			// ctx.setResponseTimeout(20 * 1000);
			return false;
		}

		boolean verifyHyReq(InputStream stream) throws IOException {
			try {
				if (STATE_FAIL == this.state) {
					return false;
				}
				if (STATE_VERIFY_HY_REQ == (STATE_VERIFY_HY_REQ & this.state)) {
					return true;
				}
				if (null == stream) {
					// return false;
					return true;
				}
				// 读取前端内容预解析及校验
				// CachedInputStream in = new CachedInputStream(stream);
				// in.fullyCached(1024);
				// byte[] bytes = in.getFitBytes();
				// in.close();
				// String str = new String(bytes, "UTF-8");
				// System.out.println(Misc.limit(str, 100));
				// if (str.indexOf("\"hy_req\":") < 0) {
				// this.state = STATE_FAIL;
				// ctx.response(200,
				// "{\"hy_resp\":{\"hy_code\": 1102,\"hy_msg\": \"request
				// invaild\"}}"
				// .getBytes());
				// ctx.close();
				// return false;
				// }
			} finally {
				if (null != stream) {
					stream.close();
				}
			}
			this.state |= STATE_VERIFY_HY_REQ;
			// asyncRead();
			ctx.requestTransferTo(forward, 0);
			return true;
		}

		@Override
		public void requestHeader() {
			// System.out.println("requestHeader:" + ctx.getUri());
			// // 校验HTTP头
			// try {
			// verifyHeader(ctx.getRequestHeaders());
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}

		@Override
		public void prepared(int available) {
			if (available > 1024) {
				try {
					if (0 == this.state && verifyHyReq(ctx.duplicateRequestStream())) {
						// 启动转发
						this.state |= STATE_VERIFY_FORWARD;
						// ctx.requestTransferTo(forward);
					}
				} catch (IOException e) {
					// e.printStackTrace();
					System.out.println(e);
				}
			}
		}

		@Override
		public void requestAbort() {
			// _Logger.info("requestAbort");
		}

		// void responseFail() throws IOException {
		// ctx.response(200,
		// "{\"hy_resp\":{\"hy_code\": 1102,\"hy_msg\": \"request
		// invaild\"}}".getBytes());
		// }

		@Override
		public void requestCompleted() {
			// _Logger.info("requestCompleted " + ctx);

			try {
				if (STATE_FAIL == state) {
					// responseFail();
					return;
				}
				if (STATE_VERIFY_HEADER != (STATE_VERIFY_HEADER & state)
						&& !verifyHeader(ctx.getRequestHeaders())) {
					// responseFail();
					return;
				}
				if (STATE_VERIFY_HY_REQ != (STATE_VERIFY_HY_REQ & state)
						&& !verifyHyReq(ctx.duplicateRequestStream())) {
					// responseFail();
					return;
				}
				if (STATE_VERIFY_FORWARD != (STATE_VERIFY_FORWARD & state)) {
					this.state |= STATE_VERIFY_FORWARD;
					// ctx.requestTransferTo(forward);
				}
				forward.close();
				// asyncRead();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void responseCompleted() {
			_Logger.info("responseCompleted " + ctx);
		}

		@Override
		public void errorRequestTransferTo(IOException e, Object msg, OutputStream writer) {
			_Logger.error(String.valueOf(msg), e);
			try {
				((OutputStreamNio) writer).cancel();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void responseTimeout() {
			// TODO Auto-generated method stub
		}
	}

}
