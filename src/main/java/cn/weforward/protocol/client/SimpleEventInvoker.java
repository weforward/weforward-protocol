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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.weforward.common.KvPair;
import cn.weforward.common.util.ListUtil;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.execption.GatewayException;
import cn.weforward.protocol.client.execption.MicroserviceException;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.support.NamingConverter;
import cn.weforward.protocol.support.datatype.FriendlyObject;
import cn.weforward.protocol.support.datatype.SimpleDtList;
import cn.weforward.protocol.support.datatype.SimpleDtObject;
import cn.weforward.protocol.support.datatype.SimpleDtString;

/**
 * 简单的服务调用实现
 * 
 * @author daibo
 *
 */
public class SimpleEventInvoker implements EventInvoker {
	/** 调用器 */
	protected ConcurrentMap<String, ServiceInvoker> m_Invokers;
	/** 链接 */
	protected String m_PreUrl;
	/** 凭证id */
	protected String m_AccessId;
	/** 凭证key */
	protected String m_AccessKey;

	public SimpleEventInvoker(String preUrl, String accessId, String accessKey) {
		m_Invokers = new ConcurrentHashMap<>();
		m_PreUrl = preUrl;
		m_AccessId = accessId;
		m_AccessKey = accessKey;
	}

	@Override
	public DtBase invoke(String name, String uri) {
		return invoke(name, uri, null);
	}

	@Override
	public DtBase invoke(String name, String uri, DtObject myparams) {
		if (null == uri) {
			throw new NullPointerException("uri不能为空");
		}
		int protocolLength = 0;
		String channel;
		int marks = 0;
		if (uri.startsWith(ENENT_PROTOCOL)) {
			protocolLength = ENENT_PROTOCOL.length();
			channel = Header.CHANNEL_RPC;
		} else if (uri.startsWith(NOTIFY_PROTOCOL)) {
			protocolLength = NOTIFY_PROTOCOL.length();
			channel = Header.CHANNEL_NOTIFY;
		} else if (uri.startsWith(BROADCAST_PROTOCOL)) {
			protocolLength = BROADCAST_PROTOCOL.length();
			channel = Header.CHANNEL_NOTIFY;
			marks |= Request.MARK_NOTIFY_BROADCAST;
		} else {
			throw new UnsupportedOperationException("不支持的协议[" + uri + "]");
		}
		int queryIndex = uri.indexOf("?");
		String serviceName;
		SimpleDtObject params = new SimpleDtObject();
		String group = "";
		if (queryIndex > 0) {
			serviceName = uri.substring(protocolLength, queryIndex);
			String query = uri.substring(queryIndex + 1);
			String[] queryParams = query.split("&");
			for (String queryParam : queryParams) {
				int index = queryParam.indexOf('=');
				String key;
				String value;
				if (index > 0) {
					key = queryParam.substring(0, index);
					value = queryParam.substring(index + 1);
				} else {
					key = query;
					value = "";
				}
				if (StringUtil.eq(key, GROUP_PARAMETER_NAME)) {
					group = value;
				} else {
					params.put(key, value);
				}
			}
		} else {
			serviceName = uri.substring(protocolLength);
		}
		if (null != myparams) {
			Enumeration<KvPair<String, DtBase>> e = myparams.getAttributes();
			while (e.hasMoreElements()) {
				KvPair<String, DtBase> param = e.nextElement();
				if (null == param) {
					continue;
				}
				params.put(param.getKey(), param.getValue());
			}
		}
		ServiceInvoker invoker = getInvoker(serviceName);
		name = group + NamingConverter.camelToWf(name);
		Request request = invoker.createRequest(name, params);
		request.getHeader().setChannel(channel);
		request.setMarks(marks);
		Response response = invoker.invoke(request);
		GatewayException.checkException(response);
		FriendlyObject result = FriendlyObject.valueOf(response.getServiceResult());
		MicroserviceException.checkException(result);
		if (StringUtil.eq(Header.CHANNEL_NOTIFY, channel)) {
			List<String> list = response.getNotifyReceives();
			if (ListUtil.isEmpty(list)) {
				return SimpleDtList.empty();
			} else {
				List<DtBase> resultlist = new ArrayList<>(list.size());
				for (String v : list) {
					resultlist.add(new SimpleDtString(v));
				}
				return SimpleDtList.valueOf(resultlist);
			}
		} else {

			return result.getBase("content");
		}
	}

	/* 获取调用器 */
	private ServiceInvoker getInvoker(String serviceName) {
		ServiceInvoker invoker = m_Invokers.get(serviceName);
		if (null == invoker) {
			invoker = ServiceInvokerFactory.create(serviceName, m_PreUrl, m_AccessId, m_AccessKey);
			ServiceInvoker old = m_Invokers.putIfAbsent(serviceName, invoker);
			if (null != old) {
				invoker = old;// 被人抢先了，用别人的
			}
		}
		return invoker;
	}

}
