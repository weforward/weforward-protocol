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
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * 用于输出http头
 * 
 * @author zhangpengji
 *
 */
public interface HttpHeaderOutput {

	void put(String name, String value) throws IOException;

	class HttpContextOutput implements HttpHeaderOutput {

		HttpContext m_Ctx;

		public HttpContextOutput(HttpContext context) {
			m_Ctx = context;
		}

		@Override
		public void put(String name, String value) throws IOException {
			m_Ctx.setResponseHeader(name, value);
		}

	}

	class HttpClientOutput implements HttpHeaderOutput {

		HttpClient m_Client;

		public HttpClientOutput(HttpClient client) {
			m_Client = client;
		}

		@Override
		public void put(String name, String value) throws IOException {
			m_Client.setRequestHeader(name, value);
		}

	}

	class HttpURLConnectionOutput implements HttpHeaderOutput {
		HttpURLConnection m_Client;

		public HttpURLConnectionOutput(HttpURLConnection client) {
			m_Client = client;
		}

		@Override
		public void put(String name, String value) throws IOException {
			m_Client.setRequestProperty(name, value);
		}
	}

	class MapOutput implements HttpHeaderOutput {

		Map<String, String> m_Map;

		public MapOutput(Map<String, String> map) {
			m_Map = map;
		}

		@Override
		public void put(String name, String value) {
			m_Map.put(name, value);
		}

	}
}
