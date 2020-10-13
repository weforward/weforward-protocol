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
package cn.weforward.protocol.auth;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.util.Bytes;
import cn.weforward.protocol.Access;
import cn.weforward.protocol.AccessLoader;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.exception.AuthException;
import cn.weforward.protocol.exception.WeforwardException;

/**
 * 身份验证器
 * 
 * @author zhangpengji
 *
 */
public class Auther {
	static final Logger _Logger = LoggerFactory.getLogger(Auther.class);

	AccessLoader m_Loader;
	Map<String, AuthEngine> m_Engines;

	public Auther(AccessLoader loader) {
		m_Loader = loader;
		m_Engines = new ConcurrentHashMap<>();
	}

	public void setEngines(List<AuthEngine> engines) {
		m_Engines.clear();
		if (null == engines || engines.isEmpty()) {
			return;
		}
		for (AuthEngine e : engines) {
			putEngine(e);
		}
	}

	public AuthEngine putEngine(AuthEngine engine) {
		return m_Engines.put(engine.getType().toLowerCase(), engine);
	}

	public AuthEngine getEngine(String type) {
		if (null == type || 0 == type.length()) {
			return null;
		}
		AuthEngine e = m_Engines.get(type.toLowerCase());
		if (null == e) {
			// FIXME 临时兼容旧名称，新网关完成后再删除
			if (Header.AUTH_TYPE_NONE.equals(type) || type.endsWith("-None")) {
				e = new NoneAuthEngine();
			} else if (Header.AUTH_TYPE_SHA2.equals(type) || type.endsWith("-SHA2")) {
				e = new Sha2AuthEngine();
			} else if (Header.AUTH_TYPE_SIGN.equals(type) || type.endsWith("-Sign")) {
				e = new SignAuthEngine();
			}
			if (null != e) {
				putEngine(e);
			}
		}
		return e;
	}

	/**
	 * 对输入内容解码并输出
	 * 
	 * @param input
	 * @param output
	 * @throws AuthException
	 * @throws IOException
	 */
	public void decode(AuthInput input, AuthOutput output) throws AuthException, IOException {
		execute(input, output, false);
	}

	private void execute(AuthInput input, AuthOutput output, boolean encode) throws AuthException, IOException {
		String authType = input.getType();
		AuthEngine engine = getEngine(authType);
		if (null == engine) {
			throw new AuthException(WeforwardException.CODE_AUTH_TYPE_INVALID, "无效的auth_type:" + authType);
		}
		BytesOutputStream bos = new BytesOutputStream(input.input);
		Bytes bytes = bos.getBytes();
		bos.close();
		bos = null;
		AuthEngine.Input in = new AuthEngine.Input();
		Access acc = getAccess(input);
		if (null != acc) {
			in.accessId = acc.getAccessId();
			in.accessKey = acc.getAccessKey();
		}
		in.serviceName = input.getServiceName();
		in.noise = input.getNoise();
		in.sign = input.getSign();
		in.contentSign = input.getContentSign();
		in.channel = input.getChannel();
		in.tag = input.getTag();
		in.data = bytes.getBytes();
		in.dataOffset = bytes.getOffset();
		in.dataLength = bytes.getSize();
		bytes = null;

		AuthEngine.Output out;
		if (encode) {
			out = engine.encode(in);
		} else {
			out = engine.decode(in);
		}
		output.setAccess(acc);
		output.setType(authType);
		output.setNoise(out.noise);
		output.setSign(out.sign);
		output.setContentSign(out.contentSign);
		output.output.write(out.data, out.dataOffset, out.dataLength);
	}

	Access getAccess(AuthInput input) throws AuthException {
		// FIXME 临时兼容旧名称，新网关完成后再删除
		if (Header.AUTH_TYPE_NONE.equals(input.getType()) || input.getType().endsWith("-None")) {
			return null;
		}
		Access acc = null;
		if (null != input.getAccessId()) {
			acc = m_Loader.getValidAccess(input.getAccessId());
		}
		if (null == acc) {
			throw new AuthException(WeforwardException.CODE_ACCESS_ID_INVALID, "无效的access_id:" + input.getAccessId());
		}
		return acc;
	}

	/**
	 * 对输入内容编码并输出
	 * 
	 * @param input
	 * @param output
	 * @throws IOException
	 * @throws AuthException
	 */
	public void encode(AuthInput input, AuthOutput output) throws AuthException, IOException {
		execute(input, output, true);
	}
}
