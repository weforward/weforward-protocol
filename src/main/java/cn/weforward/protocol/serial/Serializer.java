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
package cn.weforward.protocol.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.weforward.protocol.Header;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.exception.SerialException;
import cn.weforward.protocol.exception.WeforwardException;

/**
 * 序列化/反序列化处理器
 * 
 * @author zhangpengji
 *
 */
public class Serializer {

	Map<String, SerialEngine> m_Engines;

	public Serializer() {
		m_Engines = new ConcurrentHashMap<String, SerialEngine>();
	}

	public void setEngines(List<SerialEngine> engines) {
		m_Engines.clear();
		if (null == engines || engines.isEmpty()) {
			return;
		}
		for (SerialEngine e : engines) {
			putEngine(e);
		}
	}

	public SerialEngine putEngine(SerialEngine engine) {
		return m_Engines.put(engine.getType().toLowerCase(), engine);
	}

	public SerialEngine getEngine(String type) {
		if (null == type || 0 == type.length()) {
			return null;
		}
		SerialEngine e = m_Engines.get(type.toLowerCase());
		if (null == e) {
			if (Header.CONTENT_TYPE_JSON.equals(type)) {
				e = new JsonSerialEngine();
			}
			if (null != e) {
				putEngine(e);
			}
		}
		return e;
	}

	/**
	 * 按指定格式序列化并输出
	 * 
	 * @param object
	 * @param contentType
	 * @param charset
	 * @param output
	 * @throws SerialException
	 * @throws IOException
	 */
	public void serial(DtObject object, String contentType, String charset, OutputStream output)
			throws SerialException, IOException {
		SerialEngine engine = getEngine(contentType);
		if (null == engine) {
			throw new SerialException(WeforwardException.CODE_SERIAL_ERROR, "不支持的格式:" + contentType);
		}
		engine.serial(object, charset, output);
	}

	/**
	 * 按指定格式反序列化为Mapped
	 * 
	 * @param in
	 * @param contentType
	 * @param charset
	 * @throws SerialException
	 * @throws IOException
	 */
	public DtObject unserial(InputStream in, String contentType, String charset) throws SerialException, IOException {
		SerialEngine engine = getEngine(contentType);
		if (null == engine) {
			throw new SerialException(WeforwardException.CODE_SERIAL_ERROR, "不支持的格式：" + contentType);
		}
		return engine.unserial(in, charset);
	}
}
