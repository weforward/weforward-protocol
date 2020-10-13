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
package cn.weforward.protocol.ops.trace;

import cn.weforward.common.crypto.Hex;
import cn.weforward.common.util.StringUtil;

/**
 * 微服务调用跟踪的令牌
 * <p>
 * 令牌组成格式：&lt;trace_id&gt;-&lt;span_id&gt;-&lt;parent_id&gt;-&lt;depth&gt，
 * 或&lt;trace_id&gt;-&lt;span_id&gt;-&lt;-&lt;depth&gt。
 * 其中trace_id、span_id、parent_id由小写字母与数字组成；depth为Hex格式，长度为2；
 * <p>
 * trace_id为一个完整调用链的标识；<br/>
 * span_id为当前调用节点的标识；<br/>
 * parent_id为上一个调用节点的标识；<br/>
 * depth为调用链的栈深度
 * 
 * @author zhangpengji
 *
 */
public interface ServiceTraceToken {
	/** 分隔符 */
	char SPEARATOR = '-';
	/** 分隔符 */
	String SPEARATOR_STR = String.valueOf(SPEARATOR);

	/**
	 * 跟踪令牌
	 * 
	 */
	String getToken();

	/**
	 * trace id
	 */
	String getTraceId();

	/**
	 * span id
	 */
	String getSpanId();

	/**
	 * parent id
	 */
	String getParentId();

	/**
	 * depth
	 */
	int getDepth();

	/** 当前线程中的跟踪令牌 */
	ThreadTraceToken TTT = new ThreadTraceToken();

	/**
	 * 线程中的跟踪令牌
	 * 
	 * @author zhangpengji
	 *
	 */
	class ThreadTraceToken {
		ThreadLocal<String> m_TraceToken;

		ThreadTraceToken() {

		}

		/**
		 * 放置当前线程的跟踪令牌
		 * 
		 * @param traceToken
		 * @return 之前的跟踪令牌
		 */
		public String put(String traceToken) {
			if (StringUtil.isEmpty(traceToken) && null == m_TraceToken) {
				return null;
			}
			String old;
			if (null == m_TraceToken) {
				m_TraceToken = new ThreadLocal<>();
				old = null;
			} else {
				old = m_TraceToken.get();
			}
			m_TraceToken.set(traceToken);
			return old;
		}

		/**
		 * 获取当前线程的跟踪标识
		 * 
		 */
		public String get() {
			if (null == m_TraceToken) {
				return null;
			}
			return m_TraceToken.get();
		}
	}

	class Helper {

		public static String toTokenId(ServiceTraceToken token) {
			StringBuilder sb = new StringBuilder(100);
			sb.append(token.getTraceId()).append(SPEARATOR).append(token.getSpanId()).append(SPEARATOR);
			if (!StringUtil.isEmpty(token.getParentId())) {
				sb.append(token.getParentId()).append(SPEARATOR);
			}
			Hex.toHexFixed((byte) token.getDepth(), sb);
			return sb.toString();
		}

		public static String getTraceId(String token) {
			if (StringUtil.isEmpty(token)) {
				return null;
			}
			int idx = token.indexOf(SPEARATOR);
			if (-1 == idx) {
				return null;
			}
			return token.substring(0, idx);
		}

		public static String getSpanId(String token) {
			if (StringUtil.isEmpty(token)) {
				return null;
			}
			int bi = token.indexOf(SPEARATOR);
			if (-1 == bi) {
				return null;
			}
			int ei = token.indexOf(SPEARATOR, bi + 1);
			if (-1 == ei || (ei - bi) <= 1) {
				return null;
			}
			return token.substring(bi + 1, ei);
		}

		public static int getDepth(String token) {
			if (StringUtil.isEmpty(token)) {
				return 0;
			}
			int idx = token.lastIndexOf(SPEARATOR);
			if (-1 == idx || idx >= (token.length() - 1)) {
				return 0;
			}
			try {
				return Integer.parseInt(token.substring(idx + 1), 16);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
	}
}
