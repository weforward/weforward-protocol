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

import java.util.List;

import cn.weforward.common.ResultPage;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.execption.GatewayException;
import cn.weforward.protocol.client.execption.MicroserviceException;
import cn.weforward.protocol.client.ext.TransRemoteResultPage;
import cn.weforward.protocol.client.util.MappedUtil;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.support.NamingConverter;
import cn.weforward.protocol.support.datatype.FriendlyList;
import cn.weforward.protocol.support.datatype.FriendlyObject;

/**
 * 简单的友好服务调用器实现
 * 
 * @author daibo
 *
 */
public class SimpleFriendlyServiceInvoker implements FriendlyServiceInvoker {

	protected ServiceInvoker m_Invoker;

	protected String m_MethodGroup = "";

	protected ObjectMapperSet m_MapperSet;

	public SimpleFriendlyServiceInvoker(ServiceInvoker invoker) {
		m_Invoker = invoker;

	}

	/**
	 * 设置方法组
	 * 
	 * @param methodGroup
	 */
	public void setMethodGroup(String methodGroup) {
		m_MethodGroup = null == methodGroup ? "" : NamingConverter.camelToWf(methodGroup);
	}

	/**
	 * 设置映射器
	 * 
	 * @param set
	 */
	public void setMapperSet(ObjectMapperSet set) {
		m_MapperSet = set;
	}

	/**
	 * 调用方法
	 * 
	 * @param <E>
	 * @param method      方法名
	 * @param params      请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param resultClass 返回类的class 可选值为DtBase和JavaBean
	 * @return 调用结果
	 */
	public <E> E invoke(String method, Object params, Class<? extends E> resultClass) {
		return invoke(method, params, resultClass, null, null);
	}

	/**
	 * 调用方法
	 * 
	 * @param <E>
	 * @param method      方法名
	 * @param params      请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param option      选项
	 * @param resultClass 返回类的class 可选值为DtBase和JavaBean
	 * @return 调用结果
	 */
	public <E> E invoke(String method, Object params, Class<? extends E> resultClass,
			FriendlyServiceInvoker.Option option) {
		return invoke(method, params, resultClass, null, option);
	}

	/**
	 * 调用方法
	 * 
	 * @param <E>
	 * @param method         方法名
	 * @param params         请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param resultClass    返回类的class
	 * @param componentClass 返回类的组件，当resultClass为List,ResultPage时需要只指定，该类为其内部元素的类
	 * @return 调用结果
	 */
	public <E, V> E invoke(String method, Object params, Class<? extends E> resultClass, Class<V> componentClass) {
		return invoke(method, params, resultClass, componentClass, null);
	}

	/**
	 * 调用方法
	 * 
	 * @param <E>
	 * @param method         方法名
	 * @param params         请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param resultClass    返回类的class
	 * @param componentClass 返回类的组件，当resultClass为List,ResultPage时需要只指定，该类为其内部元素的类
	 * @param option         选项
	 * @return 调用结果
	 */
	@SuppressWarnings("unchecked")
	public <E, V> E invoke(String method, Object params, Class<? extends E> resultClass, final Class<V> componentClass,
			FriendlyServiceInvoker.Option option) {
		if (null == option && (params instanceof CustomOption)) {
			option = ((CustomOption) params).customOption();
		}
		DtObject realParams = realParams(params);
		method = genMethod(method);
		if (ResultPage.class == resultClass) {
			final FriendlyServiceInvoker.Option myoption = option;
			return (E) new TransRemoteResultPage<V>(m_Invoker, method, realParams) {
				@Override
				protected Request createRequest(String method, DtObject params) {
					Request request = super.createRequest(method, params);
					return optionRequest(request, myoption);
				}

				@Override
				protected V trans(DtBase item) {
					return (V) MappedUtil.fromBase(componentClass, null, item, m_MapperSet);
				}

			};
		}
		Request request = m_Invoker.createRequest(method, realParams);
		request = optionRequest(request, option);
		Response response = m_Invoker.invoke(request);
		GatewayException.checkException(response);
		DtObject serviceResult = response.getServiceResult();
		MicroserviceException.checkException(serviceResult);
		E e;
		if (null == resultClass || void.class == resultClass || Void.class == resultClass) {
			e = null;
		} else if (DtObject.class == resultClass) {
			e = (E) serviceResult.getObject("content");
		} else if (DtList.class == resultClass) {
			e = (E) serviceResult.getList("content");
		} else if (FriendlyObject.class == resultClass) {
			e = (E) FriendlyObject.valueOf(serviceResult.getObject("content"));
		} else if (FriendlyList.class == resultClass) {
			e = (E) FriendlyList.valueOf(serviceResult.getList("content"));
		} else {
			e = (E) MappedUtil.fromBase(resultClass, componentClass, serviceResult.getAttribute("content"),
					m_MapperSet);
		}
		if (e instanceof ResponseAware) {
			((ResponseAware) e).onResponse(response);
		}
		return e;
	}

	/*
	 * 配置请求选项
	 */
	private Request optionRequest(Request request, FriendlyServiceInvoker.Option option) {
		if (null == option) {
			return request;
		}
		if (option.getWaitTimeout() > 0) {
			request.setWaitTimeout(option.getWaitTimeout());
		}
		if (!StringUtil.isEmpty(option.getVersion())) {
			request.setVersion(option.getVersion());
		}
		if (!StringUtil.isEmpty(option.getTag())) {
			request.getHeader().setTag(option.getTag());
		}
		return request;
	}

	/* 生成方法名 */
	private String genMethod(String method) {
		return m_MethodGroup + NamingConverter.camelToWf(method);
	}

	/* 解析请求参数 */
	private DtObject realParams(Object params) {
		DtObject realParams;
		if (null == params) {
			realParams = null;
		} else if (params instanceof DtObject) {
			realParams = (DtObject) params;
		} else if (params instanceof FriendlyObject) {
			realParams = ((FriendlyObject) params).dtObjectValue();
		} else {
			DtBase base = MappedUtil.toBase(params, m_MapperSet);
			if (base instanceof DtObject) {
				realParams = (DtObject) base;
			} else {
				throw new UnsupportedOperationException("不支持的类型:" + params.getClass());
			}
		}
		return realParams;
	}

	@Override
	public <E> List<E> invokeList(String method, Object params, Class<? extends E> componentClass) {
		return invokeList(method, params, componentClass, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> invokeList(String method, Object params, Class<? extends E> componentClass, Option option) {
		return invoke(method, params, List.class, componentClass);
	}

	@Override
	public <E> ResultPage<E> invokeResultPage(String method, Object params, Class<? extends E> componentClass) {
		return invokeResultPage(method, params, componentClass, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> ResultPage<E> invokeResultPage(String method, Object params, Class<? extends E> componentClass,
			Option option) {
		return invoke(method, params, ResultPage.class, componentClass);
	}

}
