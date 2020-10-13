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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.protocol.aio.ServerHandler;
import cn.weforward.protocol.aio.http.HttpConstants;
import cn.weforward.protocol.aio.http.HttpContext;
import cn.weforward.protocol.aio.http.ServerHandlerFactory;
import cn.weforward.protocol.aio.netty.NettyHttpClientFactory;
import cn.weforward.protocol.aio.netty.NettyHttpServer;
import cn.weforward.proxy.ProxyHandler;

/**
 * AIO模式的proxy示例
 * 
 * @author liangyi
 *
 */
public class HttpProxy implements ServerHandlerFactory {
	static final Logger _Logger = LoggerFactory.getLogger(HttpProxy.class);
	NettyHttpServer m_HttpServer;
	NettyHttpClientFactory m_ClientFactory;
	ProxyHandler m_Handler;

	public HttpProxy(ProxyHandler handler) {
		m_Handler = handler;
	}

	@Override
	public ServerHandler handle(HttpContext httpContext) throws IOException {
		if (!m_Handler.checkFrom(httpContext.getRemoteAddr())) {
			// 拒绝此IP访问
			httpContext.response(HttpConstants.FORBIDDEN, null);
			httpContext.disconnect();
			return null;
		}
		return new HttpTunnel(this, httpContext);
	}

	public ProxyHandler getHandler() {
		return m_Handler;
	}
}
