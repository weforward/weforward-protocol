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
package cn.weforward.protocol.client.ext;

import java.util.Collection;

import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.support.datatype.SimpleDtList;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * 调用请求的invoke信息封装
 * 
 * @author zhangpengji
 *
 */
public class RequestInvokeObject {// implements DtObject

	SimpleDtObject m_Invoke;
	SimpleDtObject m_Params;

	ObjectMapperSet m_Mappers;

	/**
	 * 构造
	 * 
	 * @param method 方法名
	 */
	public RequestInvokeObject(String method) {
		m_Invoke = new SimpleDtObject(false);
		m_Invoke.put("method", method);
	}

	public RequestInvokeObject(String method, ObjectMapperSet mappers) {
		this(method);
		setMappers(mappers);
	}

	public RequestInvokeObject(String method, RequestInvokeParam... params) {
		this(method);
		putParams(params);
	}

	public void setMappers(ObjectMapperSet mappers) {
		m_Mappers = mappers;
	}

	public <E> ObjectMapper<E> getMapper(Class<E> clazz) {
		if (null == m_Mappers) {
			return null;
		}
		return m_Mappers.getObjectMapper(clazz);
	}

	protected SimpleDtObject getParams() {
		if (null == m_Params) {
			synchronized (this) {
				// double check
				if (null == m_Params) {
					m_Params = new SimpleDtObject();
					m_Invoke.put("params", m_Params);
				}
			}
		}
		return m_Params;
	}

	public void putParam(String name, DtBase value) {
		getParams().put(name, value);
	}

	public void putParam(String name, String value) {
		getParams().put(name, value);
	}

	public void putParam(String name, int value) {
		getParams().put(name, value);
	}

	public <E> void putParam(String name, E obj) {
		DtBase dtObj = null;
		if (null != obj) {
			@SuppressWarnings("unchecked")
			ObjectMapper<E> mapper = getMapper((Class<E>) obj.getClass());
			if (null == mapper) {
				throw new ObjectMappingException("不支持映射此对象：" + obj);
			}
			dtObj = mapper.toDtObject(obj);
		}
		getParams().put(name, dtObj);
	}

	@SuppressWarnings("unchecked")
	public <E> void putParam(String name, Collection<E> list) {
		SimpleDtList dtList = new SimpleDtList();
		if (null != list && !list.isEmpty()) {
			ObjectMapper<E> mapper = null;
			for (E obj : list) {
				if (null == mapper) {
					mapper = getMapper((Class<E>) obj.getClass());
				}
				DtObject hyObj = mapper.toDtObject(obj);
				dtList.addItem(hyObj);
			}
		}
		getParams().put(name, dtList);
	}

	public void putParams(RequestInvokeParam... params) {
		if (null == params || 0 == params.length) {
			return;
		}
		for (RequestInvokeParam p : params) {
			putParam(p.name, p.value);
		}
	}

	public DtObject toDtObject() {
		return m_Invoke;
	}

}
