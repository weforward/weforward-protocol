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

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.weforward.common.util.ClassUtil;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.ext.ObjectMapperSet;

/**
 * 简单对象映射集
 * 
 * @author daibo
 *
 */
public class SimpleObjectMapperSet implements ObjectMapperSet {

	Map<String, ObjectMapper<?>> m_ObjectMappers;

	public SimpleObjectMapperSet() {
		super();
		m_ObjectMappers = new ConcurrentHashMap<>();
		onInit();
	}

	/**
	 * 子类可覆盖其进行注册映射器
	 */
	protected void onInit() {
	}

	/**
	 * 
	 * 注册映射集合
	 * 
	 * @param mapper
	 */
	public void register(ObjectMapper<?> mapper) {
		register(mapper, mapper.getName());
	}

	public void register(ObjectMapper<?> mapper, String name) {
		m_ObjectMappers.put(name, mapper);
	}

	public <E> void register(ObjectMapper<? extends E> mapper, Class<E> clazz) {
		register(mapper, clazz.getName());
		String sn = ClassUtil.getSimpleName(clazz);
		if (null == getObjectMapper(sn)) {
			// 注册短名的映射器
			register(mapper, sn);
		}
	}

	/**
	 * 注册映射集合
	 * 
	 * @param <E>
	 * @param mapper
	 * @param clazz
	 */
	public <E> void regsiter(ObjectMapper<? extends E> mapper, Class<E> clazz) {
		register(mapper, clazz.getName());
		String sn = ClassUtil.getSimpleName(clazz);
		if (null == getObjectMapper(sn)) {
			// 注册短名的映射器
			register(mapper, sn);
		}
	}

	@Override
	public ObjectMapper<?> getObjectMapper(String name) {
		return m_ObjectMappers.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> ObjectMapper<E> getObjectMapper(Class<E> clazz) {
		ObjectMapper<?> mapper = m_ObjectMappers.get(clazz.getName());
		if (null == mapper) {
			// 试试短名
			mapper = m_ObjectMappers.get(ClassUtil.getSimpleName(clazz));
		}
		return (ObjectMapper<E>) mapper;
	}

	@Override
	public Enumeration<ObjectMapper<?>> getMappers() {
		@SuppressWarnings("unchecked")
		Collection<ObjectMapper<?>> mappers = (Collection<ObjectMapper<?>>) (Collection<?>) m_ObjectMappers.values();
		return Collections.enumeration(mappers);
	}

	@Override
	public void registerAll(ObjectMapperSet set) {
		if (null == set) {
			return;
		}
		Enumeration<ObjectMapper<?>> mappers = set.getMappers();
		while (mappers.hasMoreElements()) {
			ObjectMapper<?> mapper = mappers.nextElement();
			register(mapper);
		}
	}

	@Override
	public ObjectMapper<?> unregister(String name) {
		return m_ObjectMappers.remove(name);
	}
}
