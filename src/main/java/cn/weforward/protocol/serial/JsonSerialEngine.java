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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import cn.weforward.common.KvPair;
import cn.weforward.common.execption.InvalidFormatException;
import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.json.JsonOutputStream;
import cn.weforward.common.json.JsonUtil;
import cn.weforward.common.util.Bytes;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtBoolean;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.exception.SerialException;

/**
 * Json格式序列化
 * 
 * @author zhangpengji
 *
 */
public class JsonSerialEngine implements SerialEngine {

	@Override
	public String getType() {
		return Header.CONTENT_TYPE_JSON;
	}

	@Override
	public void serial(DtObject object, String charset, OutputStream output) throws SerialException, IOException {
		// FIXME JsonOutputStream暂时只支持utf-8
		if (!Header.CHARSET_UTF8.equalsIgnoreCase(charset)) {
			throw new SerialException("不支持的编码：" + charset);
		}
		JsonOutputStream jos = new JsonOutputStream(output);
		formatObject(object, jos);
	}

	public static void formatObject(DtObject object, JsonOutputStream output) throws IOException {
		if (object instanceof JsonDtObject) {
			String json = ((JsonDtObject) object).getJsonString();
			if (null != json) {
				output.append(json);
				return;
			}
		}
		output.append('{');
		boolean first = true;
		Enumeration<KvPair<String, DtBase>> atts = object.getAttributes();
		while (atts.hasMoreElements()) {
			KvPair<String, DtBase> att = atts.nextElement();
			if (first) {
				first = false;
			} else {
				output.append(',');
			}
			output.append('"');
			JsonUtil.escape(att.getKey(), output);
			output.append('"');
			output.append(':');
			formatValue(att.getValue(), output);
		}
		output.append('}');
	}

	public static void formatList(DtList list, JsonOutputStream output) throws IOException {
		if (list instanceof JsonDtList) {
			String json = ((JsonDtList) list).getJsonString();
			if (null != json) {
				output.append(json);
				return;
			}
		}
		output.append('[');
		boolean first = true;
		Enumeration<DtBase> values = list.items();
		while (values.hasMoreElements()) {
			DtBase value = values.nextElement();
			if (first) {
				first = false;
			} else {
				output.append(',');
			}
			formatValue(value, output);
		}
		output.append(']');
	}

	public static void formatValue(DtBase value, JsonOutputStream output) throws IOException {
		if (null == value) {
			output.append("null");
			return;
		}
		if (DataType.STRING == value.type() || DataType.DATE == value.type()) {
			DtString v = (DtString) value;
			output.append('"');
			JsonUtil.escape(v.value(), output);
			output.append('"');
			return;
		}
		if (DataType.NUMBER == value.type()) {
			DtNumber v = (DtNumber) value;
			output.append(v.valueNumber().toString());
			return;
		}
		if (DataType.BOOLEAN == value.type()) {
			DtBoolean v = (DtBoolean) value;
			if (v.value()) {
				output.append("true");
			} else {
				output.append("false");
			}
			return;
		}
		if (DataType.OBJECT == value.type()) {
			formatObject((DtObject) value, output);
			return;
		}
		if (DataType.LIST == value.type()) {
			formatList((DtList) value, output);
			return;
		}
		throw new InvalidFormatException("JSON值类型不支持" + value);
	}

	static void writeChar(OutputStream output, char ch, Charset charset) throws IOException {
		if (StandardCharsets.UTF_8 == charset) {
			output.write(ch);
		} else {
			output.write(String.valueOf(ch).getBytes(charset));
		}
	}

	@Override
	public DtObject unserial(InputStream in, String charset) throws SerialException, IOException {
		// JsonInputStream jsonInputStream = new JsonInputStream(in, charset);
		// JsonObjectAdapter adapter = (JsonObjectAdapter)
		// JsonUtil.parse(jsonInputStream, null, JsonNodeBuilder.INSTANCE);
		// return adapter.getDtObject();
		return parseObject(in, charset);
	}

	public static DtObject parseObject(InputStream in, String charset) throws SerialException, IOException {
		BytesOutputStream bos = null;
		try {
			bos = new BytesOutputStream(in);
			Bytes bytes = bos.getBytes();
			String json = new String(bytes.getBytes(), bytes.getOffset(), bytes.getSize(), charset);
			return new JsonDtObject(json);
		} finally {
			if (null != bos) {
				try {
					bos.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
