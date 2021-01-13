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
package cn.weforward.protocol.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import cn.weforward.common.Dictionary;
import cn.weforward.common.DictionaryExt;
import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.util.Bytes;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.aio.http.HttpHeaderHelper;
import cn.weforward.protocol.client.execption.HttpTransportException;
import cn.weforward.protocol.client.execption.TransportException;
import cn.weforward.protocol.exception.AuthException;
import cn.weforward.protocol.exception.SerialException;
import cn.weforward.protocol.ext.Producer;

/**
 * 基于HTTP调用传送器
 * 
 * @author zhangpengji
 *
 */
public class HttpTransport implements Transport {

	protected String m_UrlStr;
	protected URL m_Url;

	protected int m_ConnectTimeout = 5 * 1000;
	protected int m_ReadTimeout = 60 * 1000;

	public HttpTransport(String url) {
		m_UrlStr = url;
		try {
			m_Url = new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Url格式异常：" + url, e);
		}
	}

	@Override
	public void setConnectTimeout(int timeout) {
		m_ConnectTimeout = timeout;
	}

	@Override
	public int getConnectTimeout() {
		return m_ConnectTimeout;
	}

	@Override
	public void setReadTimeout(int timeout) {
		m_ReadTimeout = timeout;
	}

	@Override
	public int getReadTimeout() {
		return m_ReadTimeout;
	}

	protected HttpURLConnection createConnection() throws IOException {
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) m_Url.openConnection();
		conn.setRequestMethod("POST");

		// post不自动重发
		// System.setProperty("sun.net.http.retryPost", "false");
		// conn.setChunkedStreamingMode(0); 会导致conn.getOutputStream()变得巨慢无比

		conn.setConnectTimeout(m_ConnectTimeout);
		conn.setReadTimeout(m_ReadTimeout);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		return conn;
	}

	@Override
	public Response rpc(Request request, Producer producer) throws IOException, AuthException, SerialException {
		BytesOutputStream bos = null;
		OutputStream out = null;
		InputStream in = null;
		try {
			bos = new BytesOutputStream(8 * 1024);
			producer.make(request, bos);

			HttpURLConnection conn = createConnection();
			if (request.getWaitTimeout() > 0) {
				conn.setReadTimeout(request.getWaitTimeout() * 1000);
			}
			DictionaryExt<String, String> hs = HttpHeaderHelper.toHttpHeaders(request.getHeader());
			Enumeration<String> names = hs.keys();
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				conn.setRequestProperty(name, hs.get(name));
			}

			out = conn.getOutputStream();
			Bytes bytes = bos.getBytes();
			out.write(bytes.getBytes(), bytes.getOffset(), bytes.getSize());
			bos.close();
			bos = null;
			out.close();
			out = null;

			int responseCode = conn.getResponseCode();
			if (200 != responseCode) {
				// 确保finally能执行close，及时释放连接
				if (responseCode >= 400) {
					in = conn.getErrorStream();
				} else {
					in = conn.getInputStream();
				}
				// if (503 == responseCode) {
				// throw new UnavailableException(responseCode + "/" +
				// conn.getResponseMessage());
				// }
				throw new HttpTransportException(responseCode, conn.getResponseMessage());
			}

			in = conn.getInputStream();
			Header respHeader = parseHeader(request.getHeader().getService(), conn.getHeaderFields());
			Response resp = producer.fetchResponse(respHeader, in);
			in.close();
			in = null;
			return resp;
		} catch (ConnectException | UnknownHostException | SocketTimeoutException e) {
			throw TransportException.valueOf(e, m_UrlStr);
		} finally {
			close(bos);
			close(out);
			close(in);
		}
	}

	static void close(Closeable close) {
		if (null == close) {
			return;
		}
		try {
			close.close();
		} catch (IOException e) {
		}
	}

	protected Header parseHeader(String serviceName, final Map<String, List<String>> map) {
		Header header = new Header(serviceName);
		HttpHeaderHelper.fromHttpHeaders(toDictionary(map), header);
		return header;
	}

	protected static Dictionary<String, String> toDictionary(final Map<String, List<String>> map) {
		return new Dictionary<String, String>() {

			@Override
			public String get(String key) {
				List<String> list = map.get(key);
				if (null != list && list.size() > 0) {
					return list.get(0);
				}
				return null;
			}

		};
	}

	@Override
	public String toString() {
		return "{url:" + m_Url + ",ct:" + m_ConnectTimeout + ",rt:" + m_ReadTimeout + "}";
	}
}
