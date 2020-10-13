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
package cn.weforward.protocol.client;

import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.RequestConstants;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.execption.ServiceInvokeException;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.ops.trace.ServiceTraceToken;
import cn.weforward.protocol.support.SimpleRequest;
import cn.weforward.protocol.support.datatype.SimpleDtObject;
import cn.weforward.protocol.support.datatype.SimpleDtString;

/**
 * 服务调用器的抽象类，提供基础实现
 * 
 * @author zhangpengji
 *
 */
public abstract class AbstractServiceInvoker implements ServiceInvoker {

	protected abstract String getServiceName();

	protected abstract String getCharset();

	protected abstract void setAccessId(String id);

	protected abstract String getAccessId();

	@Override
	public Response invoke(DtObject invokeInfo) throws ServiceInvokeException {
		return invoke(createRequest(invokeInfo));
	}

	@Override
	public Response invoke(String method, DtObject params) throws ServiceInvokeException {
		return invoke(createRequest(method, params));
	}

	@Override
	public Request createRequest(String method, DtObject params) {
		SimpleDtObject invoke = new SimpleDtObject(false);
		invoke.put(RequestConstants.METHOD, SimpleDtString.valueOf(method));
		if (null != params) {
			invoke.put(RequestConstants.PARAMS, params);
		}
		return createRequest(invoke);
	}

	@Override
	public Request createRequest(DtObject invokeInfo) {
		Header header = new Header(getServiceName());
		header.setContentType(getContentType());
		header.setAuthType(getAuthType());
		header.setCharset(getCharset());
		header.setAccessId(getAccessId());

		Request request = new SimpleRequest();
		request.setHeader(header);
		String traceToken = ServiceTraceToken.TTT.get();
		if (!StringUtil.isEmpty(traceToken)) {
			request.setTraceToken(traceToken);
		}
		request.setServiceInvoke(invokeInfo);
		return request;
	}

	@Override
	public Response invoke(String method) throws ServiceInvokeException {
		return invoke(method, null);
	}

	@Override
	public Request createRequest(String method) {
		return createRequest(method, null);
	}

}
