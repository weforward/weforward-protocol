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
package cn.weforward.protocol.client.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import cn.weforward.common.util.ClassUtil;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtBoolean;
import cn.weforward.protocol.datatype.DtDate;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.support.NamingConverter;
import cn.weforward.protocol.support.PageData;
import cn.weforward.protocol.support.datatype.SimpleDtBoolean;
import cn.weforward.protocol.support.datatype.SimpleDtDate;
import cn.weforward.protocol.support.datatype.SimpleDtList;
import cn.weforward.protocol.support.datatype.SimpleDtNumber;
import cn.weforward.protocol.support.datatype.SimpleDtObject;
import cn.weforward.protocol.support.datatype.SimpleDtString;

/**
 * Mapped的工具方法
 * 
 * @author daibo
 *
 */
public class MappedUtil {
	/** 映射表 */
	private static ConcurrentHashMap<Class<?>, JavaBeanMapper> MAPPER = new ConcurrentHashMap<>();

	/**
	 * 从参数中解析出对象
	 * 
	 * @param <E>
	 * @param clazz  对象类
	 * @param params 参数
	 * @return 对象
	 */
	@SuppressWarnings("unchecked")
	public static <E> E fromBase(Class<?> clazz, DtBase params) {
		return (E) fromBase(clazz, null, params, null);
	}

	/**
	 * 从参数中解析出对象
	 * 
	 * @param <E>
	 * @param clazz     对象类
	 * @param component 对象组件
	 * @param params    参数
	 * @return 对象
	 */
	@SuppressWarnings("unchecked")
	public static <E> E fromBase(Class<?> clazz, Class<?> component, DtBase params) {
		return (E) fromBase(clazz, component, params, null);
	}

	/**
	 * 从参数中解析出对象
	 * 
	 * @param clazz     对象类
	 * @param component 对象组件
	 * @param params    参数
	 * @param set       映射器集合
	 * @return 对象
	 */
	public static Object fromBase(Class<?> clazz, Class<?> component, DtBase params, ObjectMapperSet set) {
		if (null == params) {
			if (int.class.isAssignableFrom(clazz)) {
				return 0;
			}
			if (double.class.isAssignableFrom(clazz)) {
				return 0d;
			}
			if (long.class.isAssignableFrom(clazz)) {
				return 0l;
			}
			if (boolean.class.isAssignableFrom(clazz)) {
				return false;
			}
			return null;
		}
		if (DtBase.class.isAssignableFrom(clazz)) {
			return params;
		}
		if (String.class.isAssignableFrom(clazz)) {
			if (params instanceof DtString) {
				return ((DtString) params).value();
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz)) {
			if (params instanceof DtNumber) {
				return (short) ((DtNumber) params).valueInt();
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
			if (params instanceof DtNumber) {
				return ((DtNumber) params).valueInt();
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
			if (params instanceof DtNumber) {
				return ((DtNumber) params).valueDouble();
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
			if (params instanceof DtNumber) {
				return ((DtNumber) params).valueLong();
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (Date.class.isAssignableFrom(clazz)) {
			if (params instanceof DtDate) {
				return ((DtDate) params).valueDate();
			} else if (params instanceof DtString) {
				try {
					return DtDate.Formater.parse(((DtString) params).value());
				} catch (ParseException e) {
					throw new UnsupportedOperationException("解析数据异常:" + params);
				}
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
			if (params instanceof DtBoolean) {
				return ((DtBoolean) params).value();
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (BigInteger.class.isAssignableFrom(clazz)) {
			if (params instanceof DtString) {
				return new BigInteger(((DtString) params).value());
			} else if (params instanceof DtNumber) {
				return BigInteger.valueOf(((DtNumber) params).valueLong());
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (BigDecimal.class.isAssignableFrom(clazz)) {
			if (params instanceof DtString) {
				return new BigDecimal(((DtString) params).value());
			} else if (params instanceof DtNumber) {
				return BigDecimal.valueOf(((DtNumber) params).valueDouble());
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (List.class.isAssignableFrom(clazz)) {
			if (params instanceof DtList) {
				if (null == component) {
					throw new UnsupportedOperationException("请使用@ResourceExt(component=xxx)指定");
				}
				DtList items = (DtList) params;
				Object[] elements = new Object[items.size()];
				for (int i = 0; i < items.size(); i++) {
					elements[i] = fromBase(component, null, items.getItem(i), set);
				}
				return Arrays.asList(elements);
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else if (Map.class.isAssignableFrom(clazz)) {
			if (params instanceof DtObject) {
				if (null == component) {
					throw new UnsupportedOperationException("请使用@ResourceExt(component=xxx)指定");
				}
				DtObject object = (DtObject) params;
				HashMap<String, Object> map = new HashMap<>();
				Enumeration<String> e = object.getAttributeNames();
				while (e.hasMoreElements()) {
					String name = e.nextElement();
					map.put(name, fromBase(component, null, object.getAttribute(name), set));
				}
				return map;
			} else {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
		} else {
			if (!(params instanceof DtObject)) {
				throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
			}
			if (null != set) {
				@SuppressWarnings("unchecked")
				ObjectMapper<Object> mapper = (ObjectMapper<Object>) set.getObjectMapper(clazz);
				if (null != mapper) {
					if (params instanceof DtObject) {
						return mapper.fromDtObject((DtObject) params);
					} else {
						throw new UnsupportedOperationException("不支持的类型转换:" + params + "=>" + clazz);
					}
				}
			}
			JavaBeanMapper mapper = MAPPER.get(clazz);
			if (null == mapper) {
				mapper = new JavaBeanMapper(clazz);
				JavaBeanMapper old = MAPPER.putIfAbsent(clazz, mapper);
				if (null != old) {
					mapper = old;
				}
			}
			DtObject dtobject = (DtObject) params;
			return mapper.formBase(dtobject, set);

		}

	}

	/**
	 * 将对象转换成数据
	 * 
	 * @param val 值对象
	 * @return 数据
	 */
	public static DtBase toBase(Object val) {
		return toBase(val, null);
	}

	/**
	 * 将对象转换成数据
	 * 
	 * @param val 值对象
	 * @param set 映射器集合
	 * @return 数据
	 */
	public static DtBase toBase(Object val, ObjectMapperSet set) {
		if (null == val) {
			return null;
		}
		if (val instanceof DtBase) {
			return (DtBase) val;
		} else if (val instanceof Boolean) {
			return SimpleDtBoolean.valueOf((Boolean) val);
		} else if (val instanceof String) {
			return SimpleDtString.valueOf(StringUtil.toString(val));
		} else if (val instanceof Short) {
			return SimpleDtNumber.valueOf((short) val);
		} else if (val instanceof Integer) {
			return SimpleDtNumber.valueOf((int) val);
		} else if (val instanceof Long) {
			return SimpleDtNumber.valueOf((long) val);
		} else if (val instanceof Float) {
			return SimpleDtNumber.valueOf((float) val);
		} else if (val instanceof Double) {
			return SimpleDtNumber.valueOf((double) val);
		} else if (val instanceof Date) {
			return SimpleDtDate.valueOf((Date) val);
		} else if (val instanceof BigInteger) {
			return SimpleDtString.valueOf(val.toString());
		} else if (val instanceof BigDecimal) {
			return SimpleDtString.valueOf(val.toString());
		} else if (val instanceof Iterable<?>) {
			List<DtBase> list = new ArrayList<>();
			for (Object v : (Iterable<?>) val) {
				list.add(toBase(v, set));
			}
			return SimpleDtList.valueOf(list);
		} else if (val instanceof Iterator<?>) {
			List<DtBase> list = new ArrayList<>();
			Iterator<?> it = (Iterator<?>) val;
			while (it.hasNext()) {
				list.add(toBase(it.next(), set));
			}
			return SimpleDtList.valueOf(list);
		} else if (val instanceof Collection<?>) {
			List<DtBase> list = new ArrayList<>();
			for (Object v : (Collection<?>) val) {
				list.add(toBase(v, set));
			}
			return SimpleDtList.valueOf(list);
		} else if (val instanceof Map<?, ?>) {
			SimpleDtObject map = new SimpleDtObject();
			for (Map.Entry<?, ?> e : ((Map<?, ?>) val).entrySet()) {
				Object v = e.getKey();
				String key = null;
				if (v != null) {
					key = v.toString();
				}
				map.put(key, toBase(e.getValue(), set));
			}
			return map;
		} else if (val instanceof PageData) {
			PageData data = (PageData) val;
			SimpleDtObject object = new SimpleDtObject();
			object.put("count", data.getCount());
			object.put("page", data.getPage());
			object.put("page_size", data.getPageSize());
			object.put("page_count", data.getPageCount());
			object.put("pos", data.getPos());
			object.put("items", toBase(data.getItems(), set));
			return object;
		} else {
			if (null != set) {
				@SuppressWarnings("unchecked")
				ObjectMapper<Object> mapper = (ObjectMapper<Object>) set.getObjectMapper(val.getClass());
				if (null != mapper) {
					return mapper.toDtObject(val);
				}
			}
			Class<?> clazz = val.getClass();
			JavaBeanMapper mapper = MAPPER.get(clazz);
			if (null == mapper) {
				mapper = new JavaBeanMapper(clazz);
				JavaBeanMapper old = MAPPER.putIfAbsent(clazz, mapper);
				if (null != old) {
					mapper = old;
				}
			}
			return mapper.toBase(val, set);

		}
	}

	/**
	 * 装箱
	 * 
	 * <pre>
	 * 基于规则为：调用目标对象的valueOf(xxx)静态方法生成目标对象
	 * 如
	 * String=>UniteId
	 * 则调用
	 * UniteId.valueOf(String)方法
	 * 
	 * 如
	 * Double=>UniteId
	 * 则调用
	 *	UniteId.valueOf(Double)方法
	 * </pre>
	 * 
	 * @param sourceObject 转换前源对象
	 * @param sourceType   转换前源类
	 * @param targetType   转换后的对象
	 * @return 对象
	 */
	protected static Object boxing(Object sourceObject, Class<?> sourceType, Class<?> targetType) {
		if (null == sourceObject) {
			return null;
		}
		Method method;
		String methodName = "valueOf";
		try {
			method = targetType.getMethod(methodName, sourceType);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ObjectMappingException(sourceType + "无" + methodName + "(" + sourceType + ")方法");
		}
		Object object;
		try {
			object = method.invoke(null, sourceObject);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof RuntimeException) {
				throw (RuntimeException) target;
			}
			throw new ObjectMappingException("调用" + targetType + "." + method + "方法异常", target);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new ObjectMappingException("调用" + targetType + "." + method + "方法异常", e);
		}
		return object;
	}

	/**
	 * 拆箱
	 * 
	 * <pre>
	 * 基于规则为：调用源对象的XXXValue方法转换成目标对象
	 * 如
	 * UniteId=>String
	 * 则调用
	 * UniteId.stringValue()方法
	 * 
	 * 如
	 * UniteId=>Double
	 * 则调用
	 * UniteId.doubleValue()方法
	 * </pre>
	 * 
	 * @param sourceObject 转换前源对象
	 * @param sourceType   转换前源类
	 * @param targetType   转换后的对象
	 * @return 对象
	 */
	protected static Object unboxing(Object sourceObject, Class<?> sourceType, Class<?> targetType) {
		if (null == sourceObject) {
			return null;
		}
		Method method;
		String name = targetType.getSimpleName();
		String methodName = Character.toLowerCase(name.charAt(0)) + name.substring(1) + "Value";
		try {
			method = sourceType.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ObjectMappingException(sourceType + "无" + methodName + "方法");
		}
		Object object;
		try {
			object = method.invoke(sourceObject);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof RuntimeException) {
				throw (RuntimeException) target;
			}
			throw new ObjectMappingException("调用" + sourceType + "." + method + "方法异常", target);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new ObjectMappingException("调用" + sourceType + "." + method + "方法异常", e);
		}
		return object;
	}

	static class JavaBeanMapper {

		protected Class<?> m_Clazz;

		public JavaBeanMapper(Class<?> clazz) {
			m_Clazz = clazz;
		}

		public Object formBase(DtObject dtobject, ObjectMapperSet set) {
			Method[] ms = m_Clazz.getMethods();
			Constructor<?> c;
			try {
				c = m_Clazz.getDeclaredConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new UnsupportedOperationException(m_Clazz + "无空构造");
			}
			Object object;
			try {
				if (!c.isAccessible()) {
					c.setAccessible(true);
				}
				object = c.newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException("构造异常", e);
			}
			for (int i = 0; i < ms.length; i++) {
				Method m = ms[i];
				if (m.getParameterTypes().length != 1) {
					continue;
				}
				String name = m.getName();
				boolean isSet = name.startsWith("set");
				if (!isSet) {
					continue;
				}
				Class<?> resourceType = null;
				Resource res = m.getAnnotation(Resource.class);
				if (null != res) {
					resourceType = res.type();
				}
				Class<?> methodType = m.getParameterTypes()[0];
				if (resourceType == null || resourceType == Object.class) {
					resourceType = methodType;
				}
				name = NamingConverter.camelToWf(Character.toLowerCase(name.charAt(3)) + name.substring(4));
				Class<?> methodComponent;
				if (List.class.isAssignableFrom(methodType)) {
					methodComponent = ClassUtil.find(m.getGenericParameterTypes()[0], 0);
				} else if (Map.class.isAssignableFrom(methodType)) {
					methodComponent = ClassUtil.find(m.getGenericParameterTypes()[0], 1);
				} else {
					methodComponent = null;
				}
				try {
					DtBase base = dtobject.getAttribute(name);
					if (null != base) {
						Object v = MappedUtil.fromBase(resourceType, methodComponent, base, set);
						if (resourceType != methodType) {
							v = boxing(v, resourceType, methodType);
						}
						m.invoke(object, v);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException("设置异常", e);
				}
			}
			return object;
		}

		public DtBase toBase(Object val, ObjectMapperSet set) {
			SimpleDtObject object = new SimpleDtObject();
			Method[] ms = m_Clazz.getMethods();
			for (Method m : ms) {
				if (m.getParameterTypes().length > 0) {
					continue;// 有参数
				}
				String name = m.getName();
				if (StringUtil.eq(name, "getClass")) {
					continue;
				}
				boolean isGet = name.startsWith("get");
				boolean isIs = name.startsWith("is");
				if (isGet) {
					name = NamingConverter.camelToWf(Character.toLowerCase(name.charAt(3)) + name.substring(4));
				} else if (isIs) {
					name = NamingConverter.camelToWf(Character.toLowerCase(name.charAt(2)) + name.substring(3));
				} else {
					continue;
				}
				Class<?> resourceType = null;
				Resource res = m.getAnnotation(Resource.class);
				if (null != res) {
					resourceType = res.type();
				}
				Class<?> returnType = m.getReturnType();
				if (resourceType == null || resourceType == Object.class) {
					resourceType = returnType;
				}
				try {
					Object v = m.invoke(val);
					if (returnType != resourceType) {
						v = unboxing(v, returnType, resourceType);
					}
					DtBase dt = MappedUtil.toBase(v, set);
					object.put(name, dt);
				} catch (InvocationTargetException e) {
					Throwable target = e.getTargetException();
					if (target instanceof RuntimeException) {
						throw (RuntimeException) target;
					} else {
						throw new IllegalArgumentException("方法调用失败", e);
					}
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw new IllegalArgumentException("方法调用失败", e);
				}
			}
			return object;
		}

	}
}
