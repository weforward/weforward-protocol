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
import java.util.HashMap;
import java.util.Map;

import cn.weforward.common.Dictionary;
import cn.weforward.common.DictionaryExt;
import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Header;

/**
 * Http协议下的头信息辅助类
 * 
 * @author zhangpengji
 *
 */
public class HttpHeaderHelper {

	public static String getServiceName(String uri) {
		int idx = uri.indexOf("://");
		if (-1 != idx) {
			idx = uri.indexOf('/', idx + 3);
		} else {
			idx = uri.indexOf('/');
		}
		if (-1 != idx) {
			return uri.substring(idx + 1);
		}
		return uri;
	}

	/**
	 * 将Header转换为http头信息
	 * 
	 * @param header
	 * @return http头信息
	 */
	public static DictionaryExt<String, String> toHttpHeaders(Header header) {
		final Map<String, String> hs = new HashMap<String, String>();
		try {
			outHeaders(header, new HttpHeaderOutput.MapOutput(hs));
		} catch (IOException e) {
		}
		return new DictionaryExt.WrapMap<String, String>(hs);
		// return new HttpHeaders() {
		//
		// @Override
		// public String get(String key) {
		// return hs.get(key);
		// }
		//
		// @Override
		// public Enumeration<String> names() {
		// if (hs.size() == 0) {
		// return Collections.emptyEnumeration();
		// }
		// final Iterator<String> names = hs.keySet().iterator();
		// return new Enumeration<String>() {
		//
		// @Override
		// public String nextElement() {
		// return names.next();
		// }
		//
		// @Override
		// public boolean hasMoreElements() {
		// return names.hasNext();
		// }
		// };
		// }
		//
		// @Override
		// public String getHeaderRaw(String name) {
		// return hs.get(name);
		// }
		// };
	}

	public static void responseHeaders(Header header, HttpContext ctx) throws IOException {
		outHeaders(header, new HttpHeaderOutput.HttpContextOutput(ctx));
	}

	public static void requestHeaders(Header header, HttpClient client) throws IOException {
		outHeaders(header, new HttpHeaderOutput.HttpClientOutput(client));
	}

	/**
	 * 输出头信息
	 * 
	 * @param header
	 * @param out
	 * @throws IOException
	 */
	public static void outHeaders(Header header, HttpHeaderOutput out) throws IOException {
		out.put(HttpConstants.CONTENT_TYPE,
				"application/" + header.getContentType() + ";charset=" + header.getCharset());
		String authType = header.getAuthType();
		if (!StringUtil.isEmpty(authType)) {
			StringBuilder authHeader = StringBuilderPool._8k.poll();
			try {
				authHeader.append(authType);
				if (null != header.getAccessId() && header.getAccessId().length() > 0) {
					authHeader.append(' ').append(header.getAccessId());
					if (null != header.getSign() && header.getSign().length() > 0) {
						authHeader.append(':').append(header.getSign());
					}
				}
				out.put(HttpConstants.AUTHORIZATION, authHeader.toString());
			} finally {
				StringBuilderPool._8k.offer(authHeader);
			}
		}
		String noise = header.getNoise();
		if (!StringUtil.isEmpty(noise)) {
			out.put(HttpConstants.WF_NOISE, noise);
		}
		String tag = header.getTag();
		if (!StringUtil.isEmpty(tag)) {
			out.put(HttpConstants.WF_TAG, tag);
		}
		String channel = header.getChannel();
		if (!StringUtil.isEmpty(channel)) {
			out.put(HttpConstants.WF_CHANNEL, channel);
		}
		// String userAgent = header.getUserAgent();
		// if (!StringUtil.isEmpty(userAgent)) {
		// out.put(HttpConstants.USER_AGENT, userAgent);
		// }
		String contentSign = header.getContentSign();
		if (!StringUtil.isEmpty(contentSign)) {
			out.put(HttpConstants.WF_CONTENT_SIGN, contentSign);
		}
	}

	/**
	 * 由http头信息创建Header
	 * 
	 * @param hs
	 * @param header
	 */
	public static void fromHttpHeaders(Dictionary<String, String> hs, Header header) {
		int idx;
		String contentType = hs.get(HttpConstants.CONTENT_TYPE);
		if (null != contentType && contentType.length() > 0) {
			contentType = contentType.toLowerCase();
			if (contentType.contains("json")) {
				header.setContentType(Header.CONTENT_TYPE_JSON);
			}
			idx = contentType.indexOf("charset=");
			if (-1 != idx) {
				header.setCharset(contentType.substring(idx + 8));
			}
		}
		String auth = hs.get(HttpConstants.AUTHORIZATION);
		if (null != auth && auth.length() > 0) {
			auth = auth.trim();
			idx = auth.indexOf(' ');
			if (-1 == idx) {
				header.setAuthType(auth);
			} else {
				header.setAuthType(auth.substring(0, idx));
				int idx2 = auth.indexOf(':');
				if (-1 == idx2) {
					header.setAccessId(auth.substring(idx + 1));
				} else {
					header.setAccessId(auth.substring(idx + 1, idx2));
					header.setSign(auth.substring(idx2 + 1));
				}
			}
		}
		String noise = hs.get(HttpConstants.WF_NOISE);
		header.setNoise(noise);
		String tag = hs.get(HttpConstants.WF_TAG);
		header.setTag(tag);
		String channel = hs.get(HttpConstants.WF_CHANNEL);
		header.setChannel(channel);
		String userAgent = hs.get(HttpConstants.USER_AGENT);
		header.setUserAgent(userAgent);
		String contentSign = hs.get(HttpConstants.WF_CONTENT_SIGN);
		header.setContentSign(contentSign);
	}
}
