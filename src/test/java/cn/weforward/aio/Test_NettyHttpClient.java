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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Random;

import cn.weforward.common.io.OutputStreamNio;
import cn.weforward.protocol.aio.ClientHandler;
import cn.weforward.protocol.aio.netty.NettyHttpClient;
import cn.weforward.protocol.aio.netty.NettyHttpClientFactory;
import cn.weforward.protocol.aio.netty.NettyHttpServer;

/**
 * 测试 NettyHttpServer
 * 
 * @see NettyHttpServer
 * @author liangyi
 *
 */
public class Test_NettyHttpClient {
	NettyHttpClientFactory factory;
	byte[] testBody;

	Test_NettyHttpClient() {
		factory = new NettyHttpClientFactory();
		// factory.setDebugEnabled(true);
		StringBuilder builder = new StringBuilder();
		builder.append("{\"hy_req\":{\"client_access\":\"H-12345678-1234\"},\"invoke\":{\"p1\":\"");
		Random ran = new Random();
		for (int i = 0; i < 100000; i++) {
			builder.append(ran.nextInt(9999999));
		}
		builder.append("\",\"p2\":\"v2\"}}");
		this.testBody = builder.toString().getBytes();
	}

	int testInvoke() throws IOException {
		NettyHttpClient client;
		client = factory.open(ClientHandler.SYNC);
		client.request("http://127.0.0.1:8080/test", "POST");
		client.setRequestHeader("Content-Type", "application/json");
		client.setRequestHeader("Authorization", "HY-Sign id1234:abcdefg");
		OutputStream out = client.openRequestWriter();
		out.write(testBody);
		out.close();
		int code = client.getResponseCode();
		InputStream rep = client.getResponseStream();
		if (null != rep) {
			rep.read();
			rep.close();
		}
		client.close();
		return code;
	}

	public static void main(String args[]) throws Exception {
		Test_NettyHttpClient test = new Test_NettyHttpClient();
		test.factory.setSsl(true);
		 test.factory.setIdle(60);
		NettyHttpClient client;

		String cmd;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		for (;;) {
			System.out.println("输入（q 退出，p POST测试，t 多次调用测试，其它为URL的GET测试）：");
			cmd = reader.readLine();
			if (cmd.length() > 1) {
				System.out.println("GET " + cmd);
				Handler handler = new Handler();
				client = test.factory.open(handler);
				handler.client = client;
				client.request(cmd, "GET");
				continue;
			}
			if (1 != cmd.length()) {
				continue;
			}

			char ch = cmd.charAt(0);
			if ('q' == ch) {
				break;
			} else if ('p' == ch) {
				// POST测试
				cmd = "http://127.0.0.1:8080/post.jspx";
				System.out.println("POST " + cmd);
				Handler handler = new PostHandler();
				client = test.factory.open(handler);
				handler.client = client;
				client.request(cmd, "POST");
				continue;
			} else if ('t' == ch) {
				for (int i = 0; i < 10000; i++) {
					// for (int i = 0; i < 1; i++) {
					try {
						System.out.println(i + ".code:" + test.testInvoke());
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		}
		test.factory.close();
		System.out.println("done.");
	}

	/**
	 * 
	 * @author liangyi
	 *
	 */
	static class Handler implements ClientHandler {
		NettyHttpClient client;

		@Override
		public void connectFail() {
			System.out.println("- connectFail");
		}

		@Override
		public void established() {
			System.out.println("- established");
		}

		@Override
		public void requestAbort() {
			System.out.println("- requestAbort");
		}

		@Override
		public void responseHeader() {
			try {
				System.out.println("- responseHeader[" + client.getResponseCode() + "]");
				System.out.println(client.getResponseHeaders());
			} catch (IOException e) {
				e.printStackTrace();
			}
			// asyncRead();
		}

		@Override
		public void prepared(int available) {
			System.out.println("- prepared:" + available);
		}

		@Override
		public void responseCompleted() {
			System.out.println("- responseCompleted");
			asyncRead();
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
						in = client.getResponseStream();
						do {
							ret = in.read(buf);
							if (ret > 0) {
								total += ret;
							}
							System.out.println("读取：" + ret + "/" + total);
						} while (ret >= 0);
						in.close();
						in = null;
					} catch (Exception e) {
						e.printStackTrace();
						// _Logger.error(String.valueOf(client), e);
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

		@Override
		public void errorResponseTransferTo(IOException e, Object msg, OutputStream writer) {
			System.out.println("- errorResponseTransferTo");
		}

		@Override
		public void responseTimeout() {
			System.out.println("- responseTimeout");
		}

		@Override
		public void requestCompleted() {
			System.out.println("- requestCompleted");
		}
	}

	static class PostHandler extends Handler {

		@Override
		public void established() {
			super.established();
			asyncPost();
		}

		void asyncPost() {
			Thread therad = new Thread() {
				@Override
				public void run() {
					OutputStreamNio writer = null;
					try {
						client.setRequestHeader("Content-Type", "application/json");
						writer = client.openRequestWriter();
						byte[] data = "{\"hy_req\":{\"client_access\":\"H-12345678-1234\"},\"invoke\":{\"p1\":\"v1\",\"p2\":\"v2\"}}"
								.getBytes("UTF-8");
						writer.write(data, 0, data.length);
						data = "\n----------\n".getBytes("UTF-8");
						writer.write(data, 0, data.length);
						writer.close();
						writer = null;
					} catch (Exception e) {
						e.printStackTrace();
						// _Logger.error(String.valueOf(client), e);
					} finally {
						if (null != writer) {
							try {
								writer.cancel();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			};
			therad.start();
		}
	}

}
