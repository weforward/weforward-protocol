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

import cn.weforward.common.util.StringUtil;

/**
 * 简单的ServiceTraceToken实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleServiceTraceToken implements ServiceTraceToken {

	static final SimpleServiceTraceToken NIL = new SimpleServiceTraceToken("");

	protected String m_Token;

	protected String m_TraceId;
	protected String m_SpanId;
	protected String m_ParentId;
	protected int m_Depth;

	protected SimpleServiceTraceToken(String token) {
		m_Token = token;
		int bi = 0;
		int ei = token.indexOf(SPEARATOR);
		if (-1 != ei) {
			m_TraceId = token.substring(bi, ei);
			bi = ei + 1;
			ei = token.indexOf(SPEARATOR, bi);
			if (-1 != ei) {
				m_SpanId = token.substring(bi, ei);
				bi = ei + 1;
				ei = token.indexOf(SPEARATOR, bi);
				if (-1 != ei) {
					m_ParentId = token.substring(bi, ei);
					if (ei <= token.length() - 3) {
						try {
							m_Depth = Integer.parseInt(token.substring(ei + 1), 16);
						} catch (NumberFormatException e) {
						}
					}
				} else if (bi <= token.length() - 2) {
					try {
						m_Depth = Integer.parseInt(token.substring(bi), 16);
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}

	public static SimpleServiceTraceToken valueOf(String token) {
		if (StringUtil.isEmpty(token)) {
			return NIL;
		}
		return new SimpleServiceTraceToken(token);
	}

	@Override
	public String getToken() {
		return m_Token;
	}

	@Override
	public String getTraceId() {
		return m_TraceId;
	}

	@Override
	public String getSpanId() {
		return m_SpanId;
	}

	@Override
	public String getParentId() {
		return m_ParentId;
	}

	@Override
	public int getDepth() {
		return m_Depth;
	}

}
