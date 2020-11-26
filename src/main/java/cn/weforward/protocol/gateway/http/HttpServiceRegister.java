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
package cn.weforward.protocol.gateway.http;

import cn.weforward.common.ResultPage;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.Service;
import cn.weforward.protocol.ServiceName;
import cn.weforward.protocol.client.ServiceInvoker;
import cn.weforward.protocol.client.ServiceInvokerFactory;
import cn.weforward.protocol.client.ext.RemoteResultPage;
import cn.weforward.protocol.client.ext.RequestInvokeParam;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.ext.ServiceRuntime;
import cn.weforward.protocol.gateway.ServiceRegister;
import cn.weforward.protocol.gateway.exception.ServiceRegisterException;
import cn.weforward.protocol.gateway.vo.ServiceVo;
import cn.weforward.protocol.support.BeanObjectMapper;
import cn.weforward.protocol.support.datatype.FriendlyObject;

/**
 * 基于Http协议的<code>ServiceRegister</code>实现
 * 
 * @author zhangpengji
 *
 */
public class HttpServiceRegister implements ServiceRegister {

	protected String m_AccessId;
	protected ServiceInvoker m_Invoker;
	protected ObjectMapperSet m_Mappers;

	public HttpServiceRegister(String preUrl, String accessId, String accessKey) {
		if (!StringUtil.isEmpty(accessId)) {
			accessId = accessId.trim();
		}
		if (!StringUtil.isEmpty(accessKey)) {
			accessKey = accessKey.trim();
		}
		m_AccessId = accessId;
		m_Invoker = ServiceInvokerFactory.create(ServiceName.SERVICE_REGISTER.name, preUrl, accessId, accessKey);
	}

	@Override
	public void registerService(Service service) {
		registerService(service, null);
	}

	@Override
	public void registerService(Service service, ServiceRuntime runtime) throws ServiceRegisterException {
		if (null == service) {
			throw new NullPointerException();
		}
		ServiceVo vo = new ServiceVo(service);
		vo.setServiceRuntime(runtime);
		ObjectMapper<ServiceVo> mapper = BeanObjectMapper.getInstance(ServiceVo.class);
		DtObject params = mapper.toDtObject(vo);
		Response resp = m_Invoker.invoke("register", params);
		if (0 != resp.getResponseCode()) {
			throw new ServiceRegisterException(resp);
		}
		FriendlyObject obj = FriendlyObject.valueOf(resp.getServiceResult());
		int code = obj.getInt("code", 0);
		if (0 != code) {
			throw new ServiceRegisterException(code, obj.getString("msg"));
		}
	}

	@Override
	public void unregisterService(Service service) {
		if (null == service) {
			throw new NullPointerException();
		}
		ServiceVo vo = new ServiceVo(service);
		ObjectMapper<ServiceVo> mapper = BeanObjectMapper.getInstance(ServiceVo.class);
		DtObject params = mapper.toDtObject(vo);
		Response resp = m_Invoker.invoke("unregister", params);
		if (0 != resp.getResponseCode()) {
			throw new ServiceRegisterException(resp);
		}
		FriendlyObject obj = FriendlyObject.valueOf(resp.getServiceResult());
		int code = obj.getInt("code", 0);
		if (0 != code) {
			throw new ServiceRegisterException(code, obj.getString("msg"));
		}
	}

	@Override
	public ResultPage<String> listServiceName(String keyword) {
		String method = "list_service_name";
		RequestInvokeParam[] params = { RequestInvokeParam.valueOf("keyword", keyword) };
		return new RemoteResultPage<>(m_Invoker, method, params);
	}
}
