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

import java.io.InputStream;

import cn.weforward.common.Dictionary;
import cn.weforward.protocol.StreamEntity;

/**
 * HTTP的流实体实现
 * 
 * @author zhangpengji
 *
 */
public class HttpSteamEntity implements StreamEntity {

	protected String m_Name;
	protected String m_Type;
	protected long m_Length;
	protected InputStream m_Stream;

	public HttpSteamEntity(Dictionary<String, String> headers, InputStream in) {
		m_Type = headers.get(HttpConstants.CONTENT_TYPE);
		String disp = headers.get(HttpConstants.CONTENT_DISPOSITION);
		if (null != disp) {
			int bi = disp.indexOf("filename=");
			if (-1 != bi) {
				int ei = disp.indexOf(';', bi);
				if (-1 == ei) {
					ei = disp.length();
				}
				m_Name = disp.substring(bi + 9, ei);
			}
		}
		String lengthStr = headers.get(HttpConstants.CONTENT_LENGTH);
		if (null != lengthStr && lengthStr.length() > 0) {
			m_Length = Long.parseLong(lengthStr);
		}
		m_Stream = in;
	}

	@Override
	public String getName() {
		return m_Name;
	}

	@Override
	public String getContentType() {
		return m_Type;
	}

	@Override
	public InputStream getStream() {
		return m_Stream;
	}

	@Override
	public long getLength() {
		return m_Length;
	}

}
