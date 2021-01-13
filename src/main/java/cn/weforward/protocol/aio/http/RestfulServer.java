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
package cn.weforward.protocol.aio.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.DictionaryExt;
import cn.weforward.common.restful.RestfulRequest;
import cn.weforward.common.restful.RestfulResponse;
import cn.weforward.common.restful.RestfulService;
import cn.weforward.common.util.IpRanges;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.aio.ServerHandler;

/**
 * 提供RestfulService接入HTTP server的支持
 * 
 * @author liangyi
 *
 */
public class RestfulServer implements ServerHandlerFactory {
	public static final Logger _Logger = LoggerFactory.getLogger(RestfulServer.class);

	/** RESTful服务 */
	protected RestfulService m_Service;
	/** 只允许指定访问的IP段 */
	protected IpRanges m_AllowIps;
	/** 可信的前端代理IP段 */
	protected IpRanges m_ProxyIps;
	/** 业务线程池 */
	protected Executor m_Executor;
	/** 是否更快（调用请求数据未接收完）进入业务处理 */
	protected boolean m_QuickHandle;

	public RestfulServer(RestfulService service) {
		m_Service = service;
	}

	/**
	 * 指定业务线程池
	 * 
	 * @param executor
	 *            业务线程池
	 */
	public void setExecutor(Executor executor) {
		m_Executor = executor;
	}

	/**
	 * 设置允许匿名访问的IP（段）列表
	 * 
	 * @param ipList
	 *            IP（段）列表，每IP（段）项以分号分隔，如：“127.0.0.1;192.168.0.0-192.168.0.100”
	 */
	public void setAllowIps(String ipList) {
		if (StringUtil.isEmpty(ipList)) {
			m_AllowIps = null;
			return;
		}
		IpRanges iprs = new IpRanges(ipList);
		setAllowIpRanges(iprs);
	}

	public void setAllowIpRanges(IpRanges iprs) {
		m_AllowIps = iprs;
	}

	/**
	 * 设置可信的前端代理IP段
	 * 
	 * @param ipList
	 *            IP（段）列表，每IP（段）项以分号分隔，如：“127.0.0.1;192.168.0.0-192.168.0.100”
	 */
	public void setProxyIps(String ipList) {
		if (StringUtil.isEmpty(ipList)) {
			m_ProxyIps = null;
			return;
		}
		IpRanges iprs = new IpRanges(ipList);
		setProxyIpsRanges(iprs);
	}

	public void setProxyIpsRanges(IpRanges iprs) {
		m_ProxyIps = iprs;
	}

	@Override
	public String toString() {
		return String.valueOf(m_Service);
	}

	@Override
	public ServerHandler handle(HttpContext httpContext) throws IOException {
		if (isDeny(httpContext)) {
			return null;
		}
		if (isQuickHandle() && null != m_Executor) {
			return new QuickHandler(httpContext);
		}
		return new Handler(httpContext);
	}

	/**
	 * 是否更快（调用请求数据未接收完）进入业务处理
	 */
	public boolean isQuickHandle() {
		return m_QuickHandle;
	}

	/**
	 * 开启更快（调用请求数据未接收完）进入业务处理，这同时需要指定独立的业务线程池才会生效
	 * 
	 * @param enabled
	 *            是否开启
	 */
	public void setQuickHandle(boolean enabled) {
		m_QuickHandle = enabled;
	}

	/* 是否拒绝 */
	protected boolean isDeny(HttpContext httpContext) throws IOException {
		if (null != m_AllowIps) {
			String ip = getRealIp(httpContext);
			if (null == m_AllowIps.find(ip)) {
				// 不在允许IP列表，拒绝掉
				_Logger.warn("deny-ip: " + ip);
				// 不在允许IP列表，拒绝掉
				httpContext.response(RestfulResponse.STATUS_FORBIDDEN,
						HttpContext.RESPONSE_AND_CLOSE);
				return true;
			}
		}
		return false;
	}

	/**
	 * 取得真实的IP，当服务可能运行于代理后面时，需要通过X-Forwarded-For 获取
	 * 
	 * @param httpContext
	 *            HTTP服务端处理上下文
	 * 
	 * @return 访问者的真实IP
	 */
	public String getRealIp(HttpContext httpContext) {
		String ip = httpContext.getRemoteAddr();
		if (null == ip || ip.length() < 7) {
			return ip;
		}
		int idx = ip.lastIndexOf(':');
		if (idx > 0) {
			// 去掉最后面的“:端口”
			ip = ip.substring(0, idx);
		}
		if (null != m_ProxyIps && null != m_ProxyIps.find(ip)) {
			// 经过了代理服务器的地址，由X-Forwarded-For取得
			HttpHeaders headers = httpContext.getRequestHeaders();
			if (null != headers) {
				String fip = headers.get("X-Forwarded-For");
				if (null != fip && fip.length() > 0) {
					// 取第一个IP，如：client, proxy1, proxy2, ...
					idx = fip.indexOf(',') - 1;
					// 且去除空格
					while (idx > 7 && fip.charAt(idx) == ' ') {
						--idx;
					}
					ip = (idx > 0) ? fip.substring(0, idx + 1) : fip;
				}
			}
		}
		return ip;
	}

	/**
	 * 用于封装响应输出流
	 * 
	 * @param context
	 *            HTTP服务端处理上下文
	 * @param out
	 *            要封装的响应输出流
	 * @return 封装后或不封装的响应输出流
	 * @throws IOException
	 */
	protected OutputStream wrapResponseOutput(HttpContext context, OutputStream out)
			throws IOException {
		return out;
	}

	/**
	 * 封装为RestfulRequest
	 * 
	 * @author liangyi
	 *
	 */
	static class Request implements RestfulRequest {
		final RestfulServer m_Server;
		final HttpContext m_Context;
		DictionaryExt<String, String> m_Params;

		Request(HttpContext ctx, RestfulServer server) {
			m_Context = ctx;
			m_Server = server;
		}

		@Override
		public String getVerb() {
			return m_Context.getMethod();
		}

		@Override
		public String getUri() {
			return m_Context.getUri();
		}

		@SuppressWarnings("unchecked")
		@Override
		public DictionaryExt<String, String> getParams() {
			if (null == m_Params) {
				String queryString = m_Context.getQueryString();
				if (null == queryString || 0 == queryString.length()) {
					m_Params = (DictionaryExt<String, String>) DictionaryExt._Empty;
				} else {
					QueryStringParser parser = QueryStringParser._Pool.poll();
					try {
						m_Params = new DictionaryExt.WrapMap<String, String>(
								parser.parse(queryString, 0, QueryStringParser.UTF_8, 50));
					} finally {
						QueryStringParser._Pool.offer(parser);
					}
				}
			}
			return m_Params;
		}

		@Override
		public DictionaryExt<String, String> getHeaders() {
			final HttpHeaders headers = m_Context.getRequestHeaders();
			return new DictionaryExt<String, String>() {

				@Override
				public String get(String key) {
					return headers.get(key);
				}

				@Override
				public int size() {
					return headers.size();
				}

				@Override
				public Enumeration<String> keys() {
					return headers.names();
				}

			};

		}

		@Override
		public InputStream getContent() throws IOException {
			return m_Context.getRequestStream();
		}

		@Override
		public String getClientIp() {
			return m_Server.getRealIp(m_Context);
		}
	}

	/**
	 * 封装为RestfulResponse
	 * 
	 * @author liangyi
	 *
	 */
	static class Response implements RestfulResponse {
		final RestfulServer m_Server;
		final HttpContext m_Context;
		int m_Status;

		Response(HttpContext ctx, RestfulServer server) {
			this.m_Context = ctx;
			m_Server = server;
		}

		@Override
		public void setResponse(int timeout) throws IOException {
			m_Context.setResponseTimeout(timeout);
		}

		@Override
		public void setHeader(String name, String value) throws IOException {
			m_Context.setResponseHeader(name, value);
		}

		@Override
		public void setStatus(int status) throws IOException {
			m_Status = status;
		}

		@Override
		public OutputStream openOutput() throws IOException {
			return m_Server.wrapResponseOutput(m_Context,
					m_Context.openResponseWriter(m_Status, null));
		}

		@Override
		public void close() {
			m_Context.disconnect();
		}

		@Override
		public boolean isRespond() {
			return m_Context.isRespond();
		}
	}

	/**
	 * 桥接请求到RESTful服务
	 * 
	 * @author liangyi
	 *
	 */
	class Handler implements ServerHandler, Runnable {
		HttpContext m_Context;
		Request m_Request;
		Response m_Response;

		Handler(HttpContext ctx) {
			this.m_Context = ctx;
		}

		protected void exception(Throwable e) {
			if (e instanceof ResponseEndException) {
				// 略过
				_Logger.error(String.valueOf(e), e);
				return;
			}
			try {
				byte[] content = null;
				String msg = e.getMessage();
				if (null != msg && msg.length() > 0) {
					content = msg.getBytes("UTF-8");
				}
				_Logger.error(String.valueOf(e), e);
				m_Context.response(RestfulResponse.STATUS_INTERNAL_SERVER_ERROR, content);
			} catch (IOException ee) {
				_Logger.warn(String.valueOf(ee), ee);
			}
		}

		@Override
		public void requestHeader() {
			try {
				m_Service.precheck(openRequest(), openResponse());
			} catch (Exception e) {
				exception(e);
			}
		}

		protected Response openResponse() {
			if (null == m_Response) {
				m_Response = new Response(m_Context, RestfulServer.this);
			}
			return m_Response;
		}

		protected Request openRequest() {
			if (null == m_Request) {
				m_Request = new Request(m_Context, RestfulServer.this);
			}
			return m_Request;
		}

		@Override
		public void prepared(int available) {
			if (_Logger.isTraceEnabled()) {
				_Logger.trace("prepared:" + available + "," + m_Context);
			}
		}

		@Override
		public void requestAbort() {
		}

		@Override
		public void requestCompleted() {
			if (taskRun(m_Executor)) {
				// 使用业务线程池执行
				return;
			}
			// 在当前线程中执行
			run();
		}

		protected boolean taskRun(Executor executor) {
			if (null == executor) {
				return false;
			}
			// 使用业务线程池执行
			try {
				executor.execute(this);
			} catch (RejectedExecutionException e) {
				// 线程池忙？有两种选择：1.直接返回忙，2.在当前线程执行
				try {
					m_Context.response(RestfulResponse.STATUS_TOO_MANY_REQUESTS,
							HttpContext.RESPONSE_AND_CLOSE);
					_Logger.warn(String.valueOf(e), e);
				} catch (IOException ee) {
					_Logger.error(String.valueOf(e), ee);
				}
			}
			return true;
		}

		@Override
		public void responseTimeout() {
			try {
				m_Service.timeout(openRequest(), openResponse());
			} catch (IOException e) {
				_Logger.error(String.valueOf(e), e);
			}
		}

		@Override
		public void responseCompleted() {
		}

		@Override
		public void errorRequestTransferTo(IOException e, Object msg, OutputStream writer) {
		}

		@Override
		public void run() {
			try {
				m_Service.service(openRequest(), openResponse());
			} catch (Throwable e) {
				exception(e);
			}
		}

		@Override
		public String toString() {
			return String.valueOf(m_Context);
		}
	}

	/**
	 * 更快进入到到RESTful服务业务处理（在requestHeader就开始）
	 * 
	 * @author liangyi
	 *
	 */
	class QuickHandler extends Handler {
		boolean runing;

		QuickHandler(HttpContext ctx) {
			super(ctx);
			// runing = false;
		}

		@Override
		public void requestHeader() {
			super.requestHeader();
			// 及时使用线程池执行业务处理
			if (!runing && taskRun(m_Executor)) {
				runing = true;
			}
		}

		@Override
		public void requestCompleted() {
			if (runing) {
				// 已在requestHeader开始业务处理了
				return;
			}
			_Logger.warn("没能更早开始业务处理？" + this);
			// 什么情况？requestHeader没开始业务
			super.requestCompleted();
		}

	}
}
