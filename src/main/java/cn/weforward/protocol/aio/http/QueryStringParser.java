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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.weforward.common.crypto.Hex;
import cn.weforward.common.sys.GcCleaner;
import cn.weforward.common.util.RingBuffer;
import cn.weforward.common.util.StringBuilderPool;

/**
 * 分解QueryString参数
 * 
 * @author liangyi
 *
 */
public class QueryStringParser {
	ByteBuffer m_ByteBuffer;
	CharBuffer m_CharBuffer;
	// StringBuilder strBuf;
	CharsetDecoder m_Decoder;

	/**
	 * 分析请求串参数
	 * 
	 * @param queryString 请求串（如 ?a=x&b=x&c=...）
	 * @param offset      串的开始位置
	 * @param charset     字符集
	 * @param paramsLimit 限制分析参数项数
	 * @return 映射表
	 */
	public Map<String, String> parse(CharSequence queryString, int offset, Charset charset, int paramsLimit) {
		int len = queryString.length();
		if (offset >= len) {
			return Collections.emptyMap();
		}
		if (null == m_Decoder || m_Decoder.charset() != charset) {
			m_Decoder = charset.newDecoder();
		}
		if (queryString.charAt(offset) == '?') {
			offset++;
		}
		Map<String, String> params = new HashMap<String, String>();
		int nameStart = offset;
		int valueStart = -1;
		int i;
		loop: for (i = offset; i < len; i++) {
			switch (queryString.charAt(i)) {
			case '=':
				if (nameStart == i) {
					nameStart = i + 1;
				} else if (valueStart < nameStart) {
					valueStart = i + 1;
				}
				break;
			case '&':
				if (addParam(queryString, nameStart, valueStart, i, params)) {
					paramsLimit--;
					if (paramsLimit == 0) {
						return params;
					}
				}
				nameStart = i + 1;
				break;
			case '#':
				break loop;
			default:
				// continue
			}
		}
		addParam(queryString, nameStart, valueStart, i, params);
		return params;
	}

	private boolean addParam(CharSequence queryString, int nameStart, int valueStart, int valueEnd,
			Map<String, String> params) {
		if (nameStart >= valueEnd) {
			return false;
		}
		if (valueStart <= nameStart) {
			valueStart = valueEnd + 1;
		}
		String name = decodeComponent(queryString, nameStart, valueStart - 1).toString();
		String value = decodeComponent(queryString, valueStart, valueEnd).toString();
		// List<String> values = params.get(name);
		// if (values == null) {
		// values = Collections.singletonList(value);
		// } else {
		// values = FreezedList.addToFreezed(values, values.size(), value);
		// }
		// params.put(name, values);
		params.put(name, value);
		return true;
	}

	private void prepareBuffer(int capacity) {
		if (null != m_ByteBuffer && m_ByteBuffer.capacity() >= capacity) {
			m_ByteBuffer.clear();
		} else {
			m_ByteBuffer = ByteBuffer.allocate(capacity);
		}
		if (null != m_CharBuffer && m_CharBuffer.capacity() >= capacity) {
			m_CharBuffer.clear();
		} else {
			m_CharBuffer = CharBuffer.allocate(capacity);
		}
	}

	public CharSequence decodeComponent(CharSequence s, int from, int toExcluded) {
		int len = toExcluded - from;
		if (len <= 0) {
			return "";
		}
		int firstEscaped = -1;
		for (int i = from; i < toExcluded; i++) {
			char c = s.charAt(i);
			if (c == '%' || c == '+') {
				firstEscaped = i;
				break;
			}
		}
		if (firstEscaped == -1) {
			return s.subSequence(from, toExcluded);
		}
		// UTF-8很可能占用3个字节
		int decodedCapacity = (toExcluded - firstEscaped) / 3;
		prepareBuffer(decodedCapacity);
		StringBuilder strBuf = StringBuilderPool._8k.poll();
		try {
			strBuf.append(s, from, firstEscaped);
			for (int i = firstEscaped; i < toExcluded; i++) {
				char c = s.charAt(i);
				if (c != '%') {
					strBuf.append(c != '+' ? c : ' ');
					continue;
				}

				m_ByteBuffer.clear();
				do {
					if (i + 3 > toExcluded) {
						throw new IllegalArgumentException("unterminated escape sequence at index " + i + " of: " + s);
					}
					m_ByteBuffer.put(Hex.decodeByte(s, i + 1));
					i += 3;
				} while (i < toExcluded && s.charAt(i) == '%');
				i--;

				m_ByteBuffer.flip();
				m_CharBuffer.clear();
				CoderResult result = m_Decoder.reset().decode(m_ByteBuffer, m_CharBuffer, true);
				try {
					if (!result.isUnderflow()) {
						result.throwException();
					}
					result = m_Decoder.flush(m_CharBuffer);
					if (!result.isUnderflow()) {
						result.throwException();
					}
				} catch (CharacterCodingException ex) {
					throw new IllegalStateException(ex);
				}
				strBuf.append(m_CharBuffer.flip());
			}
			return strBuf.toString();
		} finally {
			StringBuilderPool._8k.offer(strBuf);
		}
	}

	public static final Charset UTF_8 = Charset.forName("UTF-8");

	/**
	 * 池化QueryStringParser
	 */
	public static RingBuffer<QueryStringParser> _Pool = new RingBuffer<QueryStringParser>(512) {

		@Override
		protected QueryStringParser onEmpty() {
			return new QueryStringParser();
		}

		@Override
		protected void onInit() {
			GcCleaner.register(this);
		}
	};
}
