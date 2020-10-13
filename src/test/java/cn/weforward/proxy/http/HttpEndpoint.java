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
package cn.weforward.proxy.http;

import java.io.IOException;
import java.io.OutputStream;

import cn.weforward.common.io.OutputStreamNio;
import cn.weforward.protocol.aio.ClientHandler;
import cn.weforward.protocol.aio.netty.NettyHttpClient;
import cn.weforward.protocol.aio.netty.NettyHttpClientFactory;
import cn.weforward.proxy.AbstractEndpoint;
import cn.weforward.proxy.EndpointPipe;
import cn.weforward.proxy.Tunnel;

/**
 * 
 * @author liangyi
 *
 */
public class HttpEndpoint extends AbstractEndpoint {
	NettyHttpClientFactory m_Factory;
	String m_ServiceUrl;

	public HttpEndpoint(int weight, NettyHttpClientFactory factory, String serviceUrl) {
		super(weight);
		m_Factory = factory;
		m_ServiceUrl = serviceUrl;
	}

	@Override
	public Pipe connect(Tunnel tunnel) throws IOException {
		Pipe pipe = new Pipe(tunnel);
		pipe.open();
		return pipe;
	}

	@Override
	public boolean matchTag(String tag) {
		// TODO Auto-generated method stub
		return true;
	}

	protected void end(Pipe pipe, int state) {
		free(state);
	}

	/**
	 * HTTP服务端点与转发通道建立的管道
	 * 
	 * @author liangyi
	 *
	 */
	class Pipe implements ClientHandler, EndpointPipe {
		OutputStream m_Forward;
		Tunnel m_Tunnel;
		NettyHttpClient m_Client;

		public Pipe(Tunnel tunnel) {
			m_Tunnel = tunnel;
		}

		public void open() throws IOException {
			m_Client = m_Factory.open(this);
			m_Client.request(m_ServiceUrl, "POST");
		}

		private void end(int state) {
			if (null == m_Client) {
				return;
			}
			HttpEndpoint.this.end(this, state);
			m_Client.close();
			m_Tunnel = null;
			m_Client = null;
			try {
				if (m_Forward instanceof OutputStreamNio) {
					((OutputStreamNio) m_Forward).cancel();
					m_Forward = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * 转发响应
		 * 
		 * @throws IOException
		 */
		private boolean forward() throws IOException {
			if (null == m_Tunnel) {
				return false;
			}
			if (null == m_Forward) {
				m_Forward = m_Tunnel.openResponse(this);
				m_Client.responseTransferTo(m_Forward, 0);
			}
			return true;
		}

		/////////// Endpoint implement ///////////////
		@Override
		public OutputStream openRequest() throws IOException {
			// TODO ...
			m_Client.setRequestHeader("Content-Type", "application/json;charset=utf-8");
			return m_Client.openRequestWriter();
		}

		@Override
		public void cancelRequest() {
			end(STATE_FAIL);
		}

		/////////// ClientHandler implement ///////////////

		@Override
		public void connectFail() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void established() {
			try {
				m_Tunnel.established(this);
			} catch (IOException e) {
				end(STATE_EXCEPTION);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void requestAbort() {
			try {
				byte[] msg;
				msg = "{\"hy_resp\":{\"hy_code\":5004,\"hy_msg\":\"网络异常\"}}".getBytes("UTF-8");
				m_Tunnel.responseError(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				end(STATE_FAIL);
			}
		}

		@Override
		public void responseHeader() {
			// TODO 校验响应头
		}

		@Override
		public void prepared(int available) {
			// TODO 校验hy_resp，正确则替换hy_resp并向tunnel输出结果
			// response();
		}

		@Override
		public void responseCompleted() {
			try {
				if (forward()) {
					m_Forward.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			m_Forward = null;

			end(STATE_OK);
		}

		@Override
		public void errorResponseTransferTo(IOException e, Object msg, OutputStream writer) {
			end(STATE_FAIL);
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
}
