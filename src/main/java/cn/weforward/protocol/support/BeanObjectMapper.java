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
package cn.weforward.protocol.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * 符合JavaBean规范的类（包含public的空构造，以及public set/get方法)的映射器
 * 
 * @author zhangpengji
 *
 */
public class BeanObjectMapper<T> extends AbstractObjectMapper<T> implements Cloneable {

	protected final Class<T> m_Clazz;
	protected final Constructor<T> m_Constructor;
	protected final Map<String, Method> m_GetMethods;
	protected final Map<String, Method> m_SetMethods;

	public static <T> BeanObjectMapper<T> getInstance(Class<T> clazz) {
		return BeanObjectMapperSet.INSTANCE.getObjectMapper(clazz);
	}

	BeanObjectMapper(Class<T> clazz) {
		m_Clazz = clazz;
		try {
			m_Constructor = m_Clazz.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("类[" + m_Clazz.getName() + "]缺少空构造方法", e);
		}
		m_GetMethods = new HashMap<String, Method>();
		m_SetMethods = new HashMap<String, Method>();
		findMethods(clazz, m_SetMethods, m_GetMethods);
	}

	private static void findMethods(Class<?> clazz, Map<String, Method> sets, Map<String, Method> gets) {
		for (Method method : clazz.getMethods()) {
			String name = method.getName();
			if (name.startsWith("set")) {
				if (name.length() > 3 && Character.isUpperCase(name.charAt(3))
						&& 1 == method.getParameterTypes().length) {
					name = NamingConverter.camelToWf(name, "set");
					sets.put(name, method);
				}
			} else if (name.startsWith("get") && Character.isUpperCase(name.charAt(3)) && !"getClass".equals(name)) {
				if (name.length() > 3 && 0 == method.getParameterTypes().length) {
					name = NamingConverter.camelToWf(name, "get");
					gets.put(name, method);
				}
			} else if (name.startsWith("is")) {
				if (name.length() > 2 && 0 == method.getParameterTypes().length) {
					name = NamingConverter.camelToWf(name, "is");
					gets.put(name, method);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public BeanObjectMapper<T> clone(ObjectMapperSet mappers) {
		BeanObjectMapper<T> copy;
		try {
			copy = (BeanObjectMapper<T>) this.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		copy.setMappers(mappers);
		return copy;
	}

	@Override
	public String getName() {
		return m_Clazz.getName();
	}

	@Override
	public DtObject toDtObject(T object) throws ObjectMappingException {
		SimpleDtObject result = new SimpleDtObject(false);
		for (Entry<String, Method> entry : m_GetMethods.entrySet()) {
			Method method = entry.getValue();
			Object ret;
			try {
				ret = method.invoke(object);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ObjectMappingException("方法[" + method.getName() + "]调用出错", e);
			}
			DtBase att = toDtBase(ret);
			result.put(entry.getKey(), att);
		}
		return result;
	}

	@Override
	public T fromDtObject(DtObject obj) throws ObjectMappingException {
		T result;
		try {
			result = m_Constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ObjectMappingException("构造[" + m_Clazz.getName() + "]对象失败", e);
		}
		for (Entry<String, Method> entry : m_SetMethods.entrySet()) {
			DtBase att = obj.getAttribute(entry.getKey());
			if (null == att) {
				continue;
			}
			Method method = entry.getValue();
			Object arg;
			Class<?> cls = method.getParameterTypes()[0];
			if (Collection.class.isAssignableFrom(cls)) {
				arg = fromDtBase(att, method.getGenericParameterTypes()[0]);
			} else {
				arg = fromDtBase(att, cls);
			}
			try {
				method.invoke(result, arg);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ObjectMappingException("方法[" + method.getName() + "]调用出错", e);
			}
		}
		return result;
	}
}
