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
import java.util.HashMap;
import java.util.Map;

import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.ext.ObjectMapperSet;

/**
 * BeanObjectMapper的集合
 * 
 * @author zhangpengji
 *
 */
public class BeanObjectMapperSet implements ObjectMapperSet {

	public static final BeanObjectMapperSet INSTANCE = new BeanObjectMapperSet();

	private Map<String, BeanObjectMapper<?>> m_ObjectMappers;

	private BeanObjectMapperSet() {
		m_ObjectMappers = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> BeanObjectMapper<E> getObjectMapper(Class<E> clazz) {
		BeanObjectMapper<?> mapper = m_ObjectMappers.get(clazz.getName());
		if (null == mapper) {
			synchronized (m_ObjectMappers) {
				mapper = m_ObjectMappers.get(clazz.getName());
				if (null == mapper) {
					mapper = new BeanObjectMapper<>(clazz);
					mapper.setMappers(this);
					m_ObjectMappers.put(clazz.getName(), mapper);
				}
			}
		}
		return (BeanObjectMapper<E>) mapper;
	}

	@Override
	public ObjectMapper<?> getObjectMapper(String name) {
		return m_ObjectMappers.get(name);
	}

	@Override
	public void register(ObjectMapper<?> mapper, String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <E> void register(ObjectMapper<? extends E> mapper, Class<E> clazz) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<ObjectMapper<?>> getMappers() {
		@SuppressWarnings("unchecked")
		Collection<ObjectMapper<?>> mappers = (Collection<ObjectMapper<?>>) (Collection<?>) m_ObjectMappers.values();
		return Collections.enumeration(mappers);
	}

	@Override
	public void registerAll(ObjectMapperSet set) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectMapper<?> unregister(String name) {
		throw new UnsupportedOperationException();
	}
}
