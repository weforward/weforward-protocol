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
package cn.weforward.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.weforward.common.Dictionary;
import cn.weforward.common.json.JsonNode;
import cn.weforward.protocol.aio.netty.NettyHttpClientFactory;
import cn.weforward.protocol.aio.netty.NettyHttpServer;
import cn.weforward.proxy.http.HttpEndpoint;
import cn.weforward.proxy.http.HttpProxy;

/**
 * 网关
 * 
 * @author liangyi
 *
 */
public class Gateway implements ProxyHandler {
	Map<String, MicroService> m_Services;
	Map<String, AccessToken> m_AccessTokens;
	NettyHttpClientFactory m_HttpClientFactory;

	public Gateway() {
		m_Services = new HashMap<String, MicroService>();
		m_AccessTokens = new HashMap<String, AccessToken>();
		m_HttpClientFactory = new NettyHttpClientFactory();
		// m_HttpClientFactory.setSsl(true);
		load();
	}

	private void load() {
		AccessToken at = new AccessToken("id1234", "key1234");
		m_AccessTokens.put(at.accessId, at);

		MicroService service = new MicroService("test");
		HttpEndpoint ep = new HttpEndpoint(1, m_HttpClientFactory, "http://127.0.0.1:8080/" + service.getName());
		service.m_Endpoints.add(ep);
		m_Services.put(service.getName(), service);
	}

	@Override
	public boolean checkFrom(String address) {
		return true;
	}

	@Override
	public void auth(Tunnel tunnel, String serviceName, Dictionary<String, String> headers) throws IOException {
		String authorization = headers.get("Authorization");
		if (null == authorization || authorization.length() < 1) {
			// 校验失败
			byte[] msg;
			msg = "{\"hy_resp\":{\"hy_code\":1002,\"hy_msg\":\"验证失败\"}}".getBytes("UTF-8");
			tunnel.responseError(msg);
			return;
		}
		String accessId = null;
		// String sign = null;
		int idx = authorization.indexOf(' ');
		String author;
		if (idx > 0) {
			author = authorization.substring(0, idx);
			// authorization=authorization.substring(idx+1);
			idx = authorization.indexOf(':', author.length() + 1);
			if (idx > 1) {
				accessId = authorization.substring(author.length() + 1, idx);
				// sign = authorization.substring(idx + 1);
			} else {
				accessId = authorization;
			}
		} else {
			author = authorization;
		}
		String hyTag = headers.get("HY-Tag");
		AccessToken at = getAccessToken(accessId);
		if (null == at) {
			// 校验失败
			byte[] msg;
			msg = "{\"hy_resp\":{\"hy_code\":1002,\"hy_msg\":\"验证失败\"}}".getBytes("UTF-8");
			tunnel.responseError(msg);
			return;
		}
		MicroService service = getService(serviceName);
		if (null == service) {
			// throw new FileNotFoundException("无服务" + serviceName);
			byte[] msg;
			msg = "{\"hy_resp\":{\"hy_code\":5001,\"hy_msg\":\"微服务不存在\"}}".getBytes("UTF-8");
			tunnel.responseError(msg);
			return;
		}
		try {
			service.checkAvailable();
		} catch (BusyException e) {
			// TODO 微服务忙等
			byte[] msg;
			msg = "{\"hy_resp\":{\"hy_code\":5003,\"hy_msg\":\"微服务忙\"}}".getBytes("UTF-8");
			tunnel.responseError(msg);
			return;
		}
		Accessor accessor = new Accessor(at, service, hyTag);
		tunnel.permit(accessor);
	}

	public MicroService getService(String name) {
		return m_Services.get(name);
	}

	public AccessToken getAccessToken(String accessId) {
		return m_AccessTokens.get(accessId);
	}

	public void close() {
		if (null != m_HttpClientFactory) {
			m_HttpClientFactory.close();
			m_HttpClientFactory = null;
		}
	}

	/**
	 * 访问帐号信息
	 * 
	 * @author liangyi
	 *
	 */
	class AccessToken {
		String accessId;
		String passcode;

		public AccessToken(String accessId, String passcode) {
			this.accessId = accessId;
			this.passcode = passcode;
		}
	}

	/**
	 * 访问处理器
	 * 
	 * @author liangyi
	 *
	 */
	class Accessor implements AccessHandler {
		AccessToken m_AccessToken;
		MicroService m_Service;
		String m_HyTag;

		public Accessor(AccessToken accessToken, MicroService service, String hyTag) {
			m_Service = service;
			m_AccessToken = accessToken;
			m_HyTag = hyTag;
		}

		@Override
		public void joint(JsonNode hyReq, Tunnel tunnel) {
			// TODO 校验hy-req
			// 创建连接管道
			try {
				m_Service.openPipe(tunnel, m_HyTag);
			} catch (BusyException e) {
				// TODO 微服务忙等
				byte[] msg;
				try {
					msg = "{\"hy_resp\":{\"hy_code\":5003,\"hy_msg\":\"微服务忙\"}}".getBytes("UTF-8");
					tunnel.responseError(msg);
				} catch (IOException ee) {
				}
				return;
			} catch (IOException e) {
				// TODO 微服务忙等
				byte[] msg;
				try {
					msg = "{\"hy_resp\":{\"hy_code\":9999,\"hy_msg\":\"未知错误\"}}".getBytes("UTF-8");
					tunnel.responseError(msg);
				} catch (IOException ee) {
				}
				return;
			}
		}
	}

	////////////////////////////////
	public static void main(String args[]) throws Exception {
		Gateway gateway = new Gateway();
		HttpProxy proxy = new HttpProxy(gateway);
		NettyHttpServer server = new NettyHttpServer(8081);
		server.setName("gw");
		server.setMaxHttpSize(100 * 1024 * 1024);
		server.setIdle(30);
		server.setHandlerFactory(proxy);
		server.start();
		System.out.println("'q' key stop");
		while ('q' != System.in.read()) {
		}
		server.close();
		gateway.close();
		System.out.println("done.");
	}
}
