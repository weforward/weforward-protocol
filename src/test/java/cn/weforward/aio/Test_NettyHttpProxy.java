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
import cn.weforward.protocol.aio.ClientHandler;
import cn.weforward.protocol.aio.ServerHandler;
import cn.weforward.protocol.aio.http.HttpClient;
import cn.weforward.protocol.aio.http.HttpContext;
import cn.weforward.protocol.aio.http.HttpHeaders;
import cn.weforward.protocol.aio.http.ServerHandlerFactory;
import cn.weforward.protocol.aio.netty.NettyHttpClientFactory;
import cn.weforward.protocol.aio.netty.NettyHttpServer;
import io.netty.handler.codec.http.HttpHeaderNames;

/**
 * 测试 NettyHttpServer
 * 
 * @see NettyHttpServer
 * @author liangyi
 *
 */
public class Test_NettyHttpProxy {
	static final Logger _Logger = LoggerFactory.getLogger(Test_NettyHttpProxy.class);
	NettyHttpServer server;
	NettyHttpClientFactory factory;

	Test_NettyHttpProxy() {
		server = new NettyHttpServer(8081);
		server.setName("ww");
		server.setMaxHttpSize(100 * 1024 * 1024);
		server.setHandlerFactory(new ServerHandlerFactory() {
			@Override
			public ServerHandler handle(HttpContext httpContext) {
				return new Handler(httpContext);
			}
		});
		factory = new NettyHttpClientFactory();
	}

	public static void main(String args[]) throws IOException {
		Test_NettyHttpProxy test = new Test_NettyHttpProxy();
		// test.server.run();
		test.server.start();
		System.out.println("'q' key stop");
		while ('q' != System.in.read()) {
		}
		test.server.close();
		test.factory.close();
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
		int state;
		HttpContext ctx;
		OutputStream forwardRequest;
		/** 要跳过的hy_req部分 */
		int skipHyReq;
		boolean requestCompleted;
		HttpClient client;
		OutputStream forwardResponse;

		class Proxy implements ClientHandler {
			
			@Override
			public void connectFail() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void established() {
				try {
					forwardRequest = client.openRequestWriter();
					// // XXX 要换掉hy_req部分
					// byte[] hy_req =
					// "{\"hy_req\":{\"client_access\":\"H-haha\"}".getBytes("UTF-8");
					// forwardRequest.write(hy_req, 0, hy_req.length);
					// ctx.requestTransferTo(forward,skipHyReq);
					ctx.requestTransferTo(forwardRequest, 0);
					if (requestCompleted) {
						// 哦，请求已经是完整的
						forwardRequest.close();
						forwardRequest = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void requestAbort() {
				ctx.disconnect();
			}

			@Override
			public void responseHeader() {
				// 校验响应头，转发响应
				try {
					forwardResponse = ctx.openResponseWriter(client.getResponseCode(), null);
					client.responseTransferTo(forwardResponse, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void prepared(int available) {
				// TODO Auto-generated method stub
			}

			@Override
			public void responseCompleted() {
				// 响应结束
				if (null != forwardResponse) {
					try {
						forwardResponse.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					forwardResponse = null;
				}
			}

			@Override
			public void errorResponseTransferTo(IOException e, Object msg, OutputStream writer) {
				// TODO Auto-generated method stub

			}

			@Override
			public void responseTimeout() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void requestCompleted() {
				// TODO Auto-generated method stub
				
			}
		}

		Handler(HttpContext ctx) {
			this.ctx = ctx;
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
			ctx.response(200, "{\"hy_resp\":{\"hy_code\": 1001,\"hy_msg\": \"Access Id invaild\"}}"
					.getBytes());
			return false;
		}

		boolean verifyHyReq(InputStream stream) throws IOException {
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
//			CachedInputStream in = new CachedInputStream(stream);
//			in.fullyCached(1024);
//			byte[] bytes = in.getFitBytes();
//			in.close();
//			String str = new String(bytes, "UTF-8");
//			System.out.println(Misc.limit(str, 100));
//			if (str.indexOf("\"hy_req\":") < 0) {
//				this.state = STATE_FAIL;
//				ctx.response(200,
//						"{\"hy_resp\":{\"hy_code\": 1102,\"hy_msg\": \"request invaild\"}}"
//								.getBytes());
//				ctx.close();
//				return false;
//			}
			this.state |= STATE_VERIFY_HY_REQ;

			// 转发
			client = factory.open(new Proxy());
			HttpHeaders headers = ctx.getRequestHeaders();
			client.setRequestHeader("Content-Type", headers.get("Content-Type"));
			client.request("http://127.0.0.1:8080/proxy?op=forward", "POST");
			return true;
		}

		@Override
		public void requestHeader() {
			System.out.println("requestHeader:" + ctx.getUri());
			// 校验HTTP头
			try {
				verifyHeader(ctx.getRequestHeaders());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void prepared(int available) {
			if (available > 1024) {
				try {
					if (verifyHyReq(ctx.duplicateRequestStream())) {
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
			_Logger.info("requestAbort");
		}

		// void responseFail() throws IOException {
		// ctx.response(200,
		// "{\"hy_resp\":{\"hy_code\": 1102,\"hy_msg\": \"request
		// invaild\"}}".getBytes());
		// }

		@Override
		public void requestCompleted() {
			_Logger.info("requestCompleted " + ctx);
			requestCompleted = true;

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
				if (null != forwardRequest) {
					forwardRequest.close();
					forwardRequest = null;
				}
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
