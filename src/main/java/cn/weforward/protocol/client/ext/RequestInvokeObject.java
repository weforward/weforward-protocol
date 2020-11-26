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

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import cn.weforward.common.KvPair;
import cn.weforward.protocol.RequestConstants;
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
	 * @param method
	 *            方法名
	 */
	public RequestInvokeObject(String method) {
		m_Invoke = new SimpleDtObject(false);
		m_Invoke.put(RequestConstants.METHOD, method);
	}

	/**
	 * 根据另一个RequestInvokeObject构造，复制其内容
	 * 
	 * @param method
	 *            方法名
	 */
	public RequestInvokeObject(RequestInvokeObject other) {
		m_Invoke = other.m_Invoke;
		m_Mappers = other.m_Mappers;
		if (null != other.m_Params) {
			Enumeration<KvPair<String, DtBase>> dtObjAtts = other.m_Params.getAttributes();
			while (dtObjAtts.hasMoreElements()) {
				KvPair<String, DtBase> att = dtObjAtts.nextElement();
				putParam(att.getKey(), att.getValue());
			}
		}
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

	public void putParam(String name, long value) {
		getParams().put(name, value);
	}

	public void putParam(String name, double value) {
		getParams().put(name, value);
	}

	public void putParam(String name, Date value) {
		getParams().put(name, value);
	}

	public void putParam(String name, boolean value) {
		getParams().put(name, value);
	}

	public void putParamStringList(String name, Iterable<String> value) {
		SimpleDtList list = SimpleDtList.stringOf(value);
		getParams().put(name, list);
	}

	public void putParamDateList(String name, Iterable<Date> value) {
		SimpleDtList list = SimpleDtList.dateOf(value);
		getParams().put(name, list);
	}

	public void putParamNumberList(String name, Iterable<? extends Number> value) {
		SimpleDtList list = SimpleDtList.numberOf(value);
		getParams().put(name, list);
	}

	public void putParamBooleanList(String name, Iterable<Boolean> value) {
		SimpleDtList list = SimpleDtList.booleanOf(value);
		getParams().put(name, list);
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
	public <E> void putParam(String name, Iterable<E> it) {
		if (null == it) {
			getParams().put(name, (DtBase) null);
			return;
		}
		Iterator<E> iterator = it.iterator();
		if (!iterator.hasNext()) {
			getParams().put(name, SimpleDtList.empty());
			return;
		}
		SimpleDtList dtList = new SimpleDtList();
		ObjectMapper<E> mapper = null;
		while (iterator.hasNext()) {
			E obj = iterator.next();
			if (null == mapper) {
				mapper = getMapper((Class<E>) obj.getClass());
				if (null == mapper) {
					throw new ObjectMappingException("不支持映射此对象：" + obj);
				}
			}
			DtObject hyObj = mapper.toDtObject(obj);
			dtList.addItem(hyObj);
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

	/**
	 * 将obj的所有属性做为参数
	 * 
	 * @param obj
	 */
	public void putParams(Object obj) {
		if (null == obj) {
			return;
		}
		@SuppressWarnings("unchecked")
		ObjectMapper<Object> mapper = getMapper((Class<Object>) obj.getClass());
		if (null == mapper) {
			throw new ObjectMappingException("不支持映射此对象：" + obj);
		}
		DtObject dtObj = mapper.toDtObject(obj);
		Enumeration<KvPair<String, DtBase>> dtObjAtts = dtObj.getAttributes();
		while (dtObjAtts.hasMoreElements()) {
			KvPair<String, DtBase> att = dtObjAtts.nextElement();
			if (null == att.getValue()) {
				continue;
			}
			putParam(att.getKey(), att.getValue());
		}

	}

	public DtObject toDtObject() {
		return m_Invoke;
	}

}
