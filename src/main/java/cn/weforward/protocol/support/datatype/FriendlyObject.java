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
package cn.weforward.protocol.support.datatype;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtBoolean;
import cn.weforward.protocol.datatype.DtDate;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.exception.DataTypeCastExecption;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.support.NamingConverter;

/**
 * 包装DtObject，提供友好的接口
 * 
 * @author zhangpengji
 *
 */
public class FriendlyObject {

	static final FriendlyObject NIL = new FriendlyObject(null);

	protected final DtObject m_Object;
	protected final boolean m_Suppress;

	public FriendlyObject(DtObject obj) {
		this(obj, false);
	}

	/**
	 * 构造
	 * 
	 * @param obj
	 * @param suppress 是否压制（不抛出）DataTypeCastExecption
	 */
	public FriendlyObject(DtObject obj, boolean suppress) {
		m_Object = obj;
		m_Suppress = suppress;
	}

	/**
	 * 构造
	 * 
	 * @param obj
	 */
	public static FriendlyObject valueOf(DtObject obj) {
		return valueOf(obj, false);
	}

	/**
	 * 构造
	 * 
	 * @param obj
	 */
	public static FriendlyObject valueOf(DtObject obj, boolean suppress) {
		if (null == obj) {
			return NIL;
		}
		return new FriendlyObject(obj, suppress);
	}

	public DtObject dtObjectValue() {
		return m_Object;
	}

	public int getInt(String name) throws DataTypeCastExecption {
		// return getNumber(name).valueInt();
		return getInt(name, 0);
	}

	public int getInt(String name, int defalutValue) throws DataTypeCastExecption {
		return getInt(m_Object, name, defalutValue, m_Suppress);
	}

	public static int getInt(DtObject object, String name) throws DataTypeCastExecption {
		return getInt(object, name, 0, false);
	}

	public static int getInt(DtObject object, String name, int defalutValue) throws DataTypeCastExecption {
		return getInt(object, name, defalutValue, false);
	}

	public static int getInt(DtObject object, String name, int defalutValue, boolean suppress)
			throws DataTypeCastExecption {
		DtNumber num = getNumber(object, name, suppress);
		if (null == num) {
			return defalutValue;
		}
		return num.valueInt();
	}

	public long getLong(String name) throws DataTypeCastExecption {
		// return getNumber(name).valueLong();
		return getLong(name, 0);
	}

	public long getLong(String name, long defalutValue) throws DataTypeCastExecption {
		return getLong(m_Object, name, defalutValue, m_Suppress);
	}

	public static long getLong(DtObject object, String name) throws DataTypeCastExecption {
		return getLong(object, name, 0, false);
	}

	public static long getLong(DtObject object, String name, long defalutValue) throws DataTypeCastExecption {
		return getLong(object, name, defalutValue, false);
	}

	public static long getLong(DtObject object, String name, long defalutValue, boolean suppress)
			throws DataTypeCastExecption {
		DtNumber num = getNumber(object, name, suppress);
		if (null == num) {
			return defalutValue;
		}
		return num.valueLong();
	}

	public double getDouble(String name) throws DataTypeCastExecption {
		// return getNumber(name).valueDouble();
		return getDouble(name, 0);
	}

	public double getDouble(String name, double defalutValue) throws DataTypeCastExecption {
		return getDouble(m_Object, name, defalutValue, m_Suppress);
	}

	public static double getDouble(DtObject object, String name) throws DataTypeCastExecption {
		return getDouble(object, name, 0, false);
	}

	public static double getDouble(DtObject object, String name, double defalutValue) throws DataTypeCastExecption {
		return getDouble(object, name, defalutValue, false);
	}

	public static double getDouble(DtObject object, String name, double defalutValue, boolean suppress)
			throws DataTypeCastExecption {
		DtNumber num = getNumber(object, name, suppress);
		if (null == num) {
			return defalutValue;
		}
		return num.valueDouble();
	}

	// protected DtNumber getNumber(String name) throws DataTypeCastExecption {
	// return getNumber(m_Object, name, m_Suppress);
	// }

	protected static DtNumber getNumber(DtObject object, String name, boolean suppress) {
		if (null == object) {
			return null;
		}
		name = NamingConverter.camelToWf(name);
		try {
			return object.getNumber(name);
		} catch (DataTypeCastExecption e) {
			if (suppress) {
				return null;
			}
			throw e;
		} catch (RuntimeException e) {
			if (suppress) {
				return null;
			}
			throw new DataTypeCastExecption(e);
		}
	}

	public String getString(String name) throws DataTypeCastExecption {
		return getString(m_Object, name, m_Suppress);
	}

	public static String getString(DtObject object, String name) {
		return getString(object, name, false);
	}

	public static String getString(DtObject object, String name, boolean suppress) throws DataTypeCastExecption {
		if (null == object) {
			return null;
		}
		name = NamingConverter.camelToWf(name);
		DtString str;
		try {
			str = object.getString(name);
		} catch (DataTypeCastExecption e) {
			if (suppress) {
				return null;
			}
			throw e;
		} catch (RuntimeException e) {
			if (suppress) {
				return null;
			}
			throw new DataTypeCastExecption(e);
		}
		if (null == str) {
			return null;
		}
		return str.value();
	}

	public Date getDate(String name) throws DataTypeCastExecption {
		return getDate(m_Object, name, m_Suppress);
	}

	public static Date getDate(DtObject object, String name) throws DataTypeCastExecption {
		return getDate(object, name, false);
	}

	public static Date getDate(DtObject object, String name, boolean suppress) throws DataTypeCastExecption {
		if (null == object) {
			return null;
		}
		name = NamingConverter.camelToWf(name);
		DtDate d;
		try {
			d = object.getDate(name);
		} catch (DataTypeCastExecption e) {
			if (suppress) {
				return null;
			}
			throw e;
		} catch (RuntimeException e) {
			if (suppress) {
				return null;
			}
			throw new DataTypeCastExecption(e);
		}
		if (null == d) {
			return null;
		}
		return d.valueDate();
	}

	public boolean getBoolean(String name, boolean defalutValue) {
		return getBoolean(m_Object, name, defalutValue, m_Suppress);
	}

	public static boolean getBoolean(DtObject object, String name, boolean defalutValue) {
		return getBoolean(object, name, defalutValue, false);
	}

	public static boolean getBoolean(DtObject object, String name, boolean defalutValue, boolean suppress) {
		if (null == object) {
			return defalutValue;
		}
		name = NamingConverter.camelToWf(name);
		DtBoolean bool;
		try {
			bool = object.getBoolean(name);
		} catch (DataTypeCastExecption e) {
			if (suppress) {
				return defalutValue;
			}
			throw e;
		} catch (RuntimeException e) {
			if (suppress) {
				return defalutValue;
			}
			throw new DataTypeCastExecption(e);
		}
		if (null == bool) {
			return defalutValue;
		}
		return bool.value();
	}

	public DtObject getObject(String name) {
		return getObject(m_Object, name, m_Suppress);
	}

	public static DtObject getObject(DtObject object, String name) {
		return getObject(object, name, false);
	}

	public static DtObject getObject(DtObject object, String name, boolean suppress) {
		if (null == object) {
			return null;
		}
		name = NamingConverter.camelToWf(name);
		DtObject obj;
		try {
			obj = object.getObject(name);
		} catch (DataTypeCastExecption e) {
			if (suppress) {
				return null;
			}
			throw e;
		} catch (RuntimeException e) {
			if (suppress) {
				return null;
			}
			throw new DataTypeCastExecption(e);
		}
		return obj;
	}

	public DtBase getBase(String name) {
		return getBase(m_Object, name);
	}

	public static DtBase getBase(DtObject object, String name) {
		name = NamingConverter.camelToWf(name);
		// try {
		return object.getAttribute(name);
		// } catch (ClassCastException | IllegalArgumentException e) {
		// if (m_Suppress) {
		// return null;
		// }
		// throw new DataTypeCastExecption(e);
		// }
	}

	public <E> E getObject(String name, Class<E> clazz, ObjectMapperSet mappers)
			throws DataTypeCastExecption, ObjectMappingException {
		DtObject obj = getObject(name);
		if (null == obj) {
			return null;
		}
		ObjectMapper<E> mapper = mappers.getObjectMapper(clazz);
		if (null == mapper) {
			throw new ObjectMappingException("不支持映射此类型：" + clazz);
		}
		return mapper.fromDtObject(obj);
	}

	public <E> E getObject(String name, ObjectMapper<E> mapper) {
		return getObject(m_Object, name, mapper, m_Suppress);
	}

	public static <E> E getObject(DtObject object, String name, ObjectMapper<E> mapper, boolean suppress) {
		DtObject obj = getObject(object, name, suppress);
		if (null == obj) {
			return null;
		}
		return mapper.fromDtObject(obj);
	}

	public DtList getList(String name) {
		return getList(m_Object, name, m_Suppress);
	}

	public static DtList getList(DtObject object, String name) {
		return getList(object, name, false);
	}

	public static DtList getList(DtObject object, String name, boolean suppress) {
		if (null == object) {
			return null;
		}
		name = NamingConverter.camelToWf(name);
		DtList list;
		try {
			list = object.getList(name);
		} catch (ClassCastException | IllegalArgumentException e) {
			if (suppress) {
				return null;
			}
			throw new DataTypeCastExecption(e);
		}
		return list;
	}

	public <E> List<E> getList(String name, Class<E> clazz, ObjectMapperSet mappers) {
		ObjectMapper<E> mapper = mappers.getObjectMapper(clazz);
		if (null == mapper) {
			throw new ObjectMappingException("不支持映射此类型：" + clazz);
		}
		return getList(name, mapper);
	}

	public <E> List<E> getList(String name, final ObjectMapper<E> mapper) {
		return getList(m_Object, name, mapper, m_Suppress);
	}

	public static <E> List<E> getList(DtObject object, String name, final ObjectMapper<E> mapper) {
		return getList(object, name, mapper, false);
	}

	public static <E> List<E> getList(DtObject object, String name, final ObjectMapper<E> mapper, boolean suppress) {
		final DtList list = getList(object, name, suppress);
		if (null == list) {
			return null;
		}
		if (0 == list.size()) {
			return Collections.emptyList();
		}
		return new AbstractList<E>() {

			@Override
			public E get(int index) {
				DtObject obj = (DtObject) list.getItem(index);
				return mapper.fromDtObject(obj);
			}

			@Override
			public int size() {
				return list.size();
			}
		};
	}

	public <E> E toObject(Class<E> clazz, ObjectMapperSet mappers) throws ObjectMappingException {
		return toObject(m_Object, clazz, mappers);
	}

	public static <E> E toObject(DtObject object, Class<E> clazz, ObjectMapperSet mappers)
			throws ObjectMappingException {
		if (null == object) {
			return null;
		}
		ObjectMapper<E> mapper = mappers.getObjectMapper(clazz);
		if (null == mapper) {
			throw new ObjectMappingException("不支持映射此类型：" + clazz);
		}
		return mapper.fromDtObject(object);
	}

	public <E> E toObject(ObjectMapper<E> mapper) throws ObjectMappingException {
		return toObject(m_Object, mapper);
	}

	public static <E> E toObject(DtObject object, ObjectMapper<E> mapper) throws ObjectMappingException {
		if (null == object) {
			return null;
		}
		return mapper.fromDtObject(object);
	}

	public boolean isNull() {
		return null == m_Object;
	}

	public FriendlyObject getFriendlyObject(String name) {
		return FriendlyObject.valueOf(getObject(name), m_Suppress);
	}

	public static FriendlyObject getFriendlyObject(DtObject object, String name) {
		return getFriendlyObject(object, name, false);
	}

	public static FriendlyObject getFriendlyObject(DtObject object, String name, boolean suppress) {
		DtObject sub = getObject(object, name, suppress);
		return FriendlyObject.valueOf(sub, suppress);
	}

	public FriendlyList getFriendlyList(String name) {
		return FriendlyList.valueOf(getList(name), m_Suppress);
	}

	public static FriendlyList getFriendlyList(DtObject object, String name) {
		return getFriendlyList(object, name, false);
	}

	public static FriendlyList getFriendlyList(DtObject object, String name, boolean suppress) {
		DtList list = getList(object, name, suppress);
		return FriendlyList.valueOf(list, suppress);
	}
}
