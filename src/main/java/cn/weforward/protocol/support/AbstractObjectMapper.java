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

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import cn.weforward.protocol.datatype.DataType;
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
import cn.weforward.protocol.support.datatype.DataTypeConverter;
import cn.weforward.protocol.support.datatype.SimpleDtBoolean;
import cn.weforward.protocol.support.datatype.SimpleDtDate;
import cn.weforward.protocol.support.datatype.SimpleDtList;
import cn.weforward.protocol.support.datatype.SimpleDtNumber;
import cn.weforward.protocol.support.datatype.SimpleDtString;

/**
 * 抽象对象映射器
 * 
 * @author zhangpengji
 *
 * @param <E>
 */
public abstract class AbstractObjectMapper<E> implements ObjectMapper<E> {

	protected ObjectMapperSet m_Mappers;

	protected AbstractObjectMapper() {

	}

	protected ObjectMapperSet getMappers() {
		return m_Mappers;
	}

	protected void setMappers(ObjectMapperSet mappers) {
		m_Mappers = mappers;
	}

	protected ObjectMapper<?> getObjectMapper(Class<?> clazz) {
		ObjectMapperSet mappers = getMappers();
		if (null == mappers) {
			return null;
		}
		return mappers.getObjectMapper(clazz);
	}

	protected ObjectMapper<?> getObjectMapper(String name) {
		ObjectMapperSet mappers = getMappers();
		if (null == mappers) {
			return null;
		}
		return mappers.getObjectMapper(name);
	}

	protected DtList toDtList(Iterable<?> items) {
		return (DtList) toDtBase(items);
	}

	protected DtBase toDtBase(Object obj) {
		if (null == obj) {
			return null;
		}
		if (obj instanceof DtBase) {
			return (DtBase) obj;
		} else if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer) {
			return SimpleDtNumber.valueOf(((Number) obj).intValue());
		} else if (obj instanceof Long) {
			return SimpleDtNumber.valueOf(((Number) obj).longValue());
		} else if (obj instanceof Float || obj instanceof Double) {
			return SimpleDtNumber.valueOf(((Number) obj).doubleValue());
		} else if (obj instanceof String) {
			return SimpleDtString.valueOf((String) obj);
		} else if (obj instanceof Date) {
			return SimpleDtDate.valueOf((Date) obj);
		} else if (obj instanceof Boolean) {
			return SimpleDtBoolean.valueOf((Boolean) obj);
		} else if (obj instanceof Collection) {
			Collection<?> coll = (Collection<?>) obj;
			if (coll.isEmpty()) {
				return DtList.EMPTY;
			}
			SimpleDtList list = new SimpleDtList(coll.size());
			for (Object o : coll) {
				list.addItem(toDtBase(o));
			}
			return list;
		} else if (obj instanceof Iterable) {
			Iterable<?> it = (Iterable<?>) obj;
			SimpleDtList list = new SimpleDtList();
			for (Object o : it) {
				list.addItem(toDtBase(o));
			}
			return list;
		} else if (obj.getClass().isArray()) {
			int len = Array.getLength(obj);
			if (0 == len) {
				return DtList.EMPTY;
			}
			SimpleDtList list = new SimpleDtList(len);
			for (int i = 0; i < len; i++) {
				list.addItem(toDtBase(Array.get(obj, i)));
			}
			return list;
		} else {
			@SuppressWarnings("unchecked")
			ObjectMapper<Object> mapper = (ObjectMapper<Object>) getObjectMapper(obj.getClass());
			if (null == mapper) {
				throw new ObjectMappingException("缺少[" + obj.getClass().getName() + "]的Mapper");
			}
			return mapper.toDtObject(obj);
		}
	}

	/**
	 * 将DtList(元素非简单类型)转为List
	 * 
	 * @param list
	 * @param itemClazz 集合元素类型
	 * @return List对象
	 */
	protected List<?> fromDtList(DtList list, Class<?> itemClazz) {
		if (null == list) {
			return null;
		}
		if (0 == list.size()) {
			return Collections.emptyList();
		}
		return fromDtList(list, (Type) itemClazz);
	}

	/**
	 * 将DtList(元素非简单类型)转为List
	 * 
	 * @param list
	 * @param arrayType 数组类型
	 */
	protected Object fromDtArray(DtList list, Class<?> arrayType) {
		if (null == list) {
			return null;
		}
		Class<?> componentType = arrayType.getComponentType();
		Object array = Array.newInstance(componentType, list.size());
		if (0 == list.size()) {
			return array;
		}
		for (int i = 0; i < list.size(); i++) {
			DtBase b = list.getItem(i);
			// 多维数组嵌套
			Array.set(array, i, fromDtBase(b, (Type) componentType));
		}
		return array;
	}

	/**
	 * 将DtBase转为Object
	 * 
	 * @param value
	 * @param type  参考类型
	 * @throws ObjectMappingException
	 */
	protected Object fromDtBase(DtBase value, Type type) throws ObjectMappingException {
		if (null == value) {
			return null;
		}
		if (null == type) {
			// 未知类型，只能当作基础类型处理
			return fromDtBase(value);
		}

		if (DataType.LIST == value.type()) {
			DtList list = (DtList) value;
			if (type instanceof ParameterizedType) {
				// 带有泛型声明
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type raw = parameterizedType.getRawType();
				if (raw instanceof Class) {
					Class<?> cls = (Class<?>) raw;
					if (List.class.isAssignableFrom(cls)) {
						Type[] subs = parameterizedType.getActualTypeArguments();
						if (0 == subs.length) {
							// 未知元素类型
							return fromDtList(list, null);
						}
						return fromDtList(list, subs[0]);
					}
				}
			} else if (type instanceof Class) {
				if (List.class.isAssignableFrom((Class<?>) type)) {
					// 未知元素类型
					return fromDtList(list, null);
				} else if (((Class<?>) type).isArray()) {
					return fromDtArray(list, (Class<?>) type);
				}
			}
		} else {
			if (type instanceof Class) {
				// 是一个类
				return fromDtBase(value, (Class<?>) type);
			} else if (type instanceof ParameterizedType) {
				// 带有泛型声明
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type raw = parameterizedType.getRawType();
				if (raw instanceof Class) {
					return fromDtBase(value, (Class<?>) raw);
				}
			}
		}

		throw new ObjectMappingException("不支持映射此类型：" + value.type() + " -> " + type);
	}

	private List<?> fromDtList(DtList list, Type itemType) {
		List<Object> result = new ArrayList<>(list.size());
		Enumeration<DtBase> items = list.items();
		while (items.hasMoreElements()) {
			DtBase e = items.nextElement();
			result.add(fromDtBase(e, itemType));
		}
		return result;
	}

	private Object fromDtBase(DtBase value) {
		DataType type = value.type();
		if (DataType.NUMBER == type) {
			DtNumber number = (DtNumber) value;
			if (number.isInt()) {
				return number.valueInt();
			}
			if (number.isLong()) {
				return number.valueLong();
			}
			return number.valueDouble();
		} else if (DataType.STRING == type) {
			String str = ((DtString) value).value();
			if (null == str) {
				return null;
			}
			if (DtDate.Formater.check(str)) {
				try {
					return DtDate.Formater.parse(str);
				} catch (Exception e) {
					// 忽略，返回原字串
				}
			}
			return str;
		} else if (DataType.DATE == type) {
			return ((DtDate) value).valueDate();
		} else if (DataType.BOOLEAN == type) {
			return ((DtBoolean) value).value();
		} else if (DataType.LIST == type) {
			DtList list = (DtList) value;
			if (0 == list.size()) {
				return Collections.emptyList();
			}
			List<Object> result = new ArrayList<>();
			Enumeration<DtBase> items = list.items();
			while (items.hasMoreElements()) {
				Object obj = fromDtBase(items.nextElement());
				result.add(obj);
			}
			return result;
		} else {
			throw new ObjectMappingException("未知对象类型无法映射");
		}
	}

	private Object fromDtBase(DtBase value, Class<?> cls) throws ObjectMappingException {
		try {
			if (Byte.class.isAssignableFrom(cls) || byte.class.isAssignableFrom(cls)) {
				int i = ((DtNumber) value).valueInt();
				return Byte.valueOf((byte) i);
			} else if (Short.class.isAssignableFrom(cls) || short.class.isAssignableFrom(cls)) {
				int i = ((DtNumber) value).valueInt();
				return Short.valueOf((short) i);
			} else if (Integer.class.isAssignableFrom(cls) || int.class.isAssignableFrom(cls)) {
				return ((DtNumber) value).valueInt();
			} else if (Long.class.isAssignableFrom(cls) || long.class.isAssignableFrom(cls)) {
				return ((DtNumber) value).valueLong();
			} else if (Double.class.isAssignableFrom(cls) || double.class.isAssignableFrom(cls)) {
				return ((DtNumber) value).valueDouble();
			} else if (Float.class.isAssignableFrom(cls) || float.class.isAssignableFrom(cls)) {
				double d = ((DtNumber) value).valueDouble();
				return Float.valueOf((float) d);
			} else if (Number.class.isAssignableFrom(cls)) {
				return ((DtNumber) value).valueNumber();
			} else if (String.class.isAssignableFrom(cls)) {
				return ((DtString) value).value();
			} else if (Date.class.isAssignableFrom(cls)) {
				DtDate date;
				if (DataType.STRING == value.type()) {
					date = DataTypeConverter.convert(value, DataType.DATE);
				} else {
					date = (DtDate) value;
				}
				return date.valueDate();
			} else if (Boolean.class.isAssignableFrom(cls) || boolean.class.isAssignableFrom(cls)) {
				return ((DtBoolean) value).value();
			} else {
				DtObject obj = (DtObject) value;
				ObjectMapper<?> mapper = getObjectMapper(cls);
				if (null == mapper) {
					throw new ObjectMappingException("缺少[" + cls.getName() + "]的Mapper");
				}
				return mapper.fromDtObject(obj);
			}
		} catch (ClassCastException e) {
			throw new ObjectMappingException("不支持的类型转换：" + value.type() + " -> " + cls.getName());
		}
	}

	// protected static DataType getType(Object obj) {
	// if (Number.class.isInstance(obj)) {
	// return DataType.NUMBER;
	// }
	// if (String.class.isInstance(obj)) {
	// return DataType.STRING;
	// }
	// if (Date.class.isInstance(obj)) {
	// return DataType.DATE;
	// }
	// if (Boolean.class.isInstance(obj)) {
	// return DataType.BOOLEAN;
	// }
	// return DataType.OBJECT;
	// }
}
