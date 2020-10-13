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

import cn.weforward.protocol.datatype.DataType;
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

/**
 * 包装DtList，提供友好的接口
 * 
 * @author zhangpengji
 *
 */
public class FriendlyList {

	static final FriendlyList NIL = new FriendlyList(null);

	static final int[] EMPTY_INT_ARRAY = new int[0];
	static final long[] EMPTY_LONG_ARRAY = new long[0];
	static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
	static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

	protected final DtList m_List;
	protected final int m_Size;
	protected final boolean m_Suppress;

	public FriendlyList(DtList list) {
		this(list, false);
	}

	/**
	 * 构造
	 * 
	 * @param list
	 *            列表
	 * @param suppress
	 *            压制类型转换错误、下标越界错误
	 */
	public FriendlyList(DtList list, boolean suppress) {
		m_List = list;
		m_Size = sizeOf(list);
		m_Suppress = suppress;
	}

	public static FriendlyList valueOf(DtList list) {
		return valueOf(list, false);
	}

	public static FriendlyList valueOf(DtList list, boolean suppress) {
		if (null == list || 0 == list.size()) {
			return NIL;
		}
		return new FriendlyList(list, suppress);
	}

	public int size() {
		return m_Size;
	}

	public FriendlyObject getFriendlyObject(int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getFriendlyObject(m_List, index, m_Suppress);
	}

	public static FriendlyObject getFriendlyObject(DtList list, int index, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		DtObject obj = getObject(list, index, suppress);
		return FriendlyObject.valueOf(obj, suppress);
	}

	public DtObject getObject(int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getItem(m_List, index, DataType.OBJECT, m_Suppress);
	}

	public static DtObject getObject(DtList list, int index, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getItem(list, index, DataType.OBJECT, suppress);
	}

	public <E> E getObject(int index, Class<E> clazz, ObjectMapperSet mappers)
			throws DataTypeCastExecption, ObjectMappingException, IndexOutOfBoundsException {
		DtObject obj = getObject(index);
		if (null == obj) {
			return null;
		}
		ObjectMapper<E> mapper = mappers.getObjectMapper(clazz);
		if (null == mapper) {
			throw new ObjectMappingException("不支持映射此类型：" + clazz);
		}
		return mapper.fromDtObject(obj);
	}

	public static <E> E getObject(DtList list, int index, ObjectMapper<E> mapper, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		DtObject obj = getObject(list, index, suppress);
		if (null == obj) {
			return null;
		}
		return mapper.fromDtObject(obj);
	}

	public FriendlyList getFriendlyList(int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		// return FriendlyList.valueOf(getList(index), m_Suppress);
		return getFriendlyList(m_List, index, m_Suppress);
	}

	public static FriendlyList getFriendlyList(DtList list, int index)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getFriendlyList(list, index, false);
	}

	public static FriendlyList getFriendlyList(DtList list, int index, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		DtList sub = getItem(list, index, DataType.LIST, suppress);
		return FriendlyList.valueOf(sub, suppress);
	}

	public DtList getList(int index) throws DataTypeCastExecption {
		return getItem(m_List, index, DataType.LIST, m_Suppress);
	}

	public static DtList getList(DtList list, int index, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getItem(list, index, DataType.LIST, suppress);
	}

	public <E> List<E> getList(int index, Class<E> clazz, ObjectMapperSet mappers)
			throws ClassCastException, IndexOutOfBoundsException {
		final DtList list = getList(index);
		if (null == list) {
			return null;
		}
		if (0 == list.size()) {
			return Collections.emptyList();
		}
		final ObjectMapper<E> mapper = mappers.getObjectMapper(clazz);
		if (null == mapper) {
			throw new ObjectMappingException("不支持映射此类型：" + clazz);
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

	public <E> List<E> getList(int index, ObjectMapper<E> mapper) {
		return getList(m_List, index, mapper, m_Suppress);
	}

	public static <E> List<E> getList(DtList list, int index, ObjectMapper<E> mapper) {
		return getList(list, index, mapper, false);
	}

	public static <E> List<E> getList(DtList list, int index, final ObjectMapper<E> mapper, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		final DtList sub = getList(list, index, suppress);
		if (null == sub) {
			return null;
		}
		if (0 == sub.size()) {
			return Collections.emptyList();
		}
		return new AbstractList<E>() {

			@Override
			public E get(int index) {
				DtObject obj = (DtObject) sub.getItem(index);
				return mapper.fromDtObject(obj);
			}

			@Override
			public int size() {
				return sub.size();
			}
		};
	}

	public <E> List<E> toList(Class<E> clazz, ObjectMapperSet mappers) {
		return toList(m_List, clazz, mappers);
	}

	public static <E> List<E> toList(DtList list, Class<E> clazz, ObjectMapperSet mappers) {
		final int size = sizeOf(list);
		if (0 == size) {
			return Collections.emptyList();
		}
		final ObjectMapper<E> mapper = mappers.getObjectMapper(clazz);
		if (null == mapper) {
			throw new ObjectMappingException("不支持映射此类型：" + clazz);
		}
		return toList(list, mapper);
	}

	public <E> List<E> toList(final ObjectMapper<E> mapper) {
		return toList(m_List, mapper);
	}

	public static <E> List<E> toList(final DtList list, final ObjectMapper<E> mapper) {
		final int size = sizeOf(list);
		if (0 == size) {
			return Collections.emptyList();
		}
		return new AbstractList<E>() {

			@Override
			public E get(int index) {
				DtObject obj = (DtObject) list.getItem(index, DataType.OBJECT);
				return mapper.fromDtObject(obj);
			}

			@Override
			public int size() {
				return size;
			}
		};
	}

	public List<String> toStringList() {
		return toStringList(m_List, m_Suppress);
	}

	public static List<String> toStringList(final DtList list) {
		return toStringList(list, false);
	}

	public static List<String> toStringList(final DtList list, final boolean suppress) {
		final int size = sizeOf(list);
		if (0 == size) {
			return Collections.emptyList();
		}
		return new AbstractList<String>() {

			@Override
			public String get(int index) {
				return getString(list, index, suppress);
			}

			@Override
			public int size() {
				return size;
			}
		};
	}

	public List<Date> toDateList() {
		return toDateList(m_List, m_Suppress);
	}

	public static List<Date> toDateList(final DtList list) {
		return toDateList(list, false);
	}

	public static List<Date> toDateList(final DtList list, final boolean suppress) {
		final int size = sizeOf(list);
		if (0 == size) {
			return Collections.emptyList();
		}
		return new AbstractList<Date>() {

			@Override
			public Date get(int index) {
				return getDate(list, index, suppress);
			}

			@Override
			public int size() {
				return size;
			}
		};
	}

	public List<Integer> toIntList() {
		return toIntList(m_List, m_Suppress);
	}

	public static List<Integer> toIntList(final DtList list) {
		return toIntList(list, false);
	}

	public static List<Integer> toIntList(final DtList list, final boolean suppress) {
		final int size = sizeOf(list);
		if (0 == size) {
			return Collections.emptyList();
		}
		return new AbstractList<Integer>() {

			@Override
			public Integer get(int index) {
				return getInt(list, index, 0, suppress);
			}

			@Override
			public int size() {
				return size;
			}
		};
	}

	public List<Number> toNumberList() {
		return toNumberList(m_List, m_Suppress);
	}

	public static List<Number> toNumberList(final DtList list) {
		return toNumberList(list, false);
	}

	public static List<Number> toNumberList(final DtList list, final boolean suppress) {
		final int size = sizeOf(list);
		if (0 == size) {
			return Collections.emptyList();
		}
		return new AbstractList<Number>() {

			@Override
			public Number get(int index) {
				return getNumber(list, index, suppress).valueNumber();
			}

			@Override
			public int size() {
				return size;
			}
		};
	}

	public int[] toIntArray() {
		return toIntArray(m_List, m_Suppress);
	}

	public static int[] toIntArray(DtList list) {
		return toIntArray(list, false);
	}

	public static int[] toIntArray(DtList list, boolean suppress) {
		int size = sizeOf(list);
		if (0 == size) {
			return EMPTY_INT_ARRAY;
		}
		int[] arr = new int[size];
		for (int i = 0; i < size; i++) {
			arr[i] = getInt(list, i, 0, suppress);
		}
		return arr;
	}

	public long[] toLongArray() {
		return toLongArray(m_List, m_Suppress);
	}

	public static long[] toLongArray(DtList list) {
		return toLongArray(list, false);
	}

	public static long[] toLongArray(DtList list, boolean suppress) {
		int size = sizeOf(list);
		if (0 == size) {
			return EMPTY_LONG_ARRAY;
		}
		long[] arr = new long[size];
		for (int i = 0; i < size; i++) {
			arr[i] = getLong(list, i, 0, suppress);
		}
		return arr;
	}

	public double[] toDoubleArray() {
		return toDoubleArray(m_List, m_Suppress);
	}

	public static double[] toDoubleArray(DtList list) {
		return toDoubleArray(list, false);
	}

	public static double[] toDoubleArray(DtList list, boolean suppress) {
		int size = sizeOf(list);
		if (0 == size) {
			return EMPTY_DOUBLE_ARRAY;
		}
		double[] arr = new double[size];
		for (int i = 0; i < size; i++) {
			arr[i] = getDouble(list, i, 0, suppress);
		}
		return arr;
	}

	public boolean[] toBooleanArray() {
		return toBooleanArray(m_List, m_Suppress);
	}

	public static boolean[] toBooleanArray(DtList list) {
		return toBooleanArray(list, false);
	}

	public static boolean[] toBooleanArray(DtList list, boolean suppress) {
		int size = sizeOf(list);
		if (0 == size) {
			return EMPTY_BOOLEAN_ARRAY;
		}
		boolean[] arr = new boolean[size];
		for (int i = 0; i < size; i++) {
			arr[i] = getBoolean(list, i, false, suppress);
		}
		return arr;
	}

	protected <E extends DtBase> E getItem(int index, DataType type) {
		return getItem(m_List, index, type, m_Suppress);
	}

	protected static int sizeOf(DtList list) {
		return (null == list ? 0 : list.size());
	}

	protected static <E extends DtBase> E getItem(DtList list, int index, DataType type, boolean suppress) {
		int size = sizeOf(list);
		if (index < 0 || index >= size) {
			if (suppress) {
				return null;
			}
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
		try {
			E item = list.getItem(index, type);
			return item;
		} catch (DataTypeCastExecption e) {
			if (suppress) {
				return null;
			}
			throw e;
			// } catch (ClassCastException | IllegalArgumentException e) {
		} catch (RuntimeException e) {
			if (suppress) {
				return null;
			}
			throw new DataTypeCastExecption(e);
		}
	}

	public int getInt(int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		// return getNumber(index).valueInt();
		return getInt(m_List, index, 0, m_Suppress);
	}

	public int getInt(int index, int defalutValue) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getInt(m_List, index, defalutValue, m_Suppress);
	}

	public static int getInt(DtList list, int index, int defalutValue)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getInt(list, index, defalutValue, false);
	}

	public static int getInt(DtList list, int index, int defalutValue, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		DtNumber num = getNumber(list, index, suppress);
		if (null == num) {
			return defalutValue;
		}
		return num.valueInt();
	}

	public long getLong(int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		// return getNumber(index).valueLong();
		return getLong(m_List, index, 0, m_Suppress);
	}

	public long getLong(int index, long defalutValue) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getLong(m_List, index, defalutValue, m_Suppress);
	}

	public static long getLong(DtList list, int index, long defalutValue)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getLong(list, index, defalutValue, false);
	}

	public static long getLong(DtList list, int index, long defalutValue, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		DtNumber num = getNumber(list, index, suppress);
		if (null == num) {
			return defalutValue;
		}
		return num.valueLong();
	}

	public double getDouble(int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		// return getNumber(index).valueDouble();
		return getDouble(m_List, index, 0, m_Suppress);
	}

	public double getDouble(int index, double defalutValue) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getDouble(m_List, index, defalutValue, m_Suppress);
	}

	public static double getDouble(DtList list, int index, double defalutValue)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getDouble(list, index, defalutValue, false);
	}

	public static double getDouble(DtList list, int index, double defalutValue, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		DtNumber num = getNumber(list, index, suppress);
		if (null == num) {
			return defalutValue;
		}
		return num.valueDouble();
	}

	public DtNumber getNumber(int index) {
		return getItem(m_List, index, DataType.NUMBER, m_Suppress);
	}

	public static DtNumber getNumber(DtList list, int index) {
		return getItem(list, index, DataType.NUMBER, false);
	}

	public static DtNumber getNumber(DtList list, int index, boolean suppress) {
		return getItem(list, index, DataType.NUMBER, suppress);
	}

	public String getString(int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getString(m_List, index, m_Suppress);
	}

	public static String getString(DtList list, int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getString(list, index, false);
	}

	public static String getString(DtList list, int index, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		DtString str = getItem(list, index, DataType.STRING, suppress);
		if (null == str) {
			return null;
		}
		return str.value();
	}

	public Date getDate(int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getDate(m_List, index, m_Suppress);
	}

	public static Date getDate(DtList list, int index) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getDate(list, index, false);
	}

	public static Date getDate(DtList list, int index, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		DtDate date = getItem(list, index, DataType.DATE, suppress);
		if (null == date) {
			return null;
		}
		return date.valueDate();
	}

	public boolean getBoolean(int index) throws NullPointerException, DataTypeCastExecption, IndexOutOfBoundsException {
		// DtBoolean bool = getItem(index, DataType.BOOLEAN);
		// return bool.value();
		return getBoolean(m_List, index, false, m_Suppress);
	}

	public boolean getBoolean(int index, boolean defalutValue) throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getBoolean(m_List, index, defalutValue, m_Suppress);
	}

	public static boolean getBoolean(DtList list, int index, boolean defalutValue)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		return getBoolean(list, index, defalutValue, false);
	}

	public static boolean getBoolean(DtList list, int index, boolean defalutValue, boolean suppress)
			throws DataTypeCastExecption, IndexOutOfBoundsException {
		DtBoolean bool = getItem(list, index, DataType.BOOLEAN, suppress);
		if (null == bool) {
			return defalutValue;
		}
		return bool.value();
	}
}
