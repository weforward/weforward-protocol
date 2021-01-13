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
package cn.weforward.protocol.aio.netty;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;

import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.protocol.aio.http.HttpHeaders;

/**
 * 基于netty的HTTP头封装
 * 
 * @author liangyi
 *
 */
public class NettyHttpHeaders implements HttpHeaders {
	io.netty.handler.codec.http.HttpHeaders m_Headers;

	public NettyHttpHeaders(io.netty.handler.codec.http.HttpHeaders headers) {
		m_Headers = headers;
	}

	@Override
	public String get(String name) {
		return m_Headers.get(name);
	}

	@Override
	public String getHeaderRaw(String name) {
		return m_Headers.get(name);
	}

	@Override
	public String toString() {
		StringBuilder builder = StringBuilderPool._8k.poll();
		try {

			Iterator<Entry<CharSequence, CharSequence>> it = m_Headers.iteratorCharSequence();
			while (it.hasNext()) {
				Entry<CharSequence, CharSequence> p = it.next();
				if (null != p) {
					builder.append(p.getKey()).append(": ").append(p.getValue()).append('\n');
				}
			}
			return builder.toString();
		} finally {
			StringBuilderPool._8k.offer(builder);
		}
	}

	@Override
	public Enumeration<String> names() {
		if (m_Headers.size() == 0) {
			return Collections.emptyEnumeration();
		}
		// final Iterator<Entry<String, String>> it =
		// m_Headers.iteratorAsString();
		final Iterator<Entry<CharSequence, CharSequence>> it = m_Headers.iteratorCharSequence();
		return new Enumeration<String>() {
			@Override
			public boolean hasMoreElements() {
				return it.hasNext();
			}

			@Override
			public String nextElement() {
				CharSequence key = it.next().getKey();
				return null == key ? null : key.toString();
			}
		};
	}

	@Override
	public int size() {
		return m_Headers.size();
	}

	static public HttpHeaders valueOf(io.netty.handler.codec.http.HttpHeaders headers) {
		if (headers.size() == 0) {
			return HttpHeaders._Empty;
		}
		return new NettyHttpHeaders(headers);
	}
}
