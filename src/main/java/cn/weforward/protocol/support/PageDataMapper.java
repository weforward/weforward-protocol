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

import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.support.datatype.FriendlyObject;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * PageData的映射器
 * 
 * @author zhangpengji
 *
 */
public class PageDataMapper extends AbstractObjectMapper<PageData> {

	protected Class<?> m_ElementClazz;
	protected ObjectMapper<?> m_ElementMapper;

	/**
	 * 构造
	 * <p>
	 * 集合中元素必须是基本类型，如：String、Number、Date、Boolean
	 */
	public PageDataMapper() {

	}

	/**
	 * 构造
	 * 
	 * @param elementClazz
	 *            元素类型，需符合JavaBean规范
	 */
	public PageDataMapper(Class<?> elementClazz) {
		if (String.class == elementClazz) {
			// 兼容字串类型
			return;
		}
		m_ElementClazz = elementClazz;
		setMappers(BeanObjectMapperSet.INSTANCE);
	}

	/**
	 * 构造。
	 * <p>
	 * 不指定具体元素类型。若元素非基础类型，则无法从<code>DtObject</code>映射到<code>Object</code>
	 * 
	 * @param mappers
	 * 
	 * @see #PageDataMapper(Class, ObjectMapperSet)
	 */
	public PageDataMapper(ObjectMapperSet mappers) {
		this(null, mappers);
	}

	/**
	 * 构造
	 * 
	 * @param elementClazz
	 *            元素类型
	 * @param mappers
	 *            支持此元素类型的映射器集
	 */
	public PageDataMapper(Class<?> elementClazz, ObjectMapperSet mappers) {
		m_ElementClazz = elementClazz;
		setMappers(mappers);
	}

	/**
	 * 构造
	 * 
	 * @param elementClazz
	 *            元素类型
	 * @param mapper
	 *            元素的映射器
	 */
	public PageDataMapper(Class<?> elementClazz, ObjectMapper<?> mapper) {
		m_ElementClazz = elementClazz;
		m_ElementMapper = mapper;
	}

	@Override
	protected ObjectMapper<?> getObjectMapper(Class<?> clazz) {
		if (null != m_ElementMapper && clazz == m_ElementClazz) {
			return m_ElementMapper;
		}
		return super.getObjectMapper(clazz);
	}

	@Override
	public String getName() {
		return PageData.class.getSimpleName();
	}

	@Override
	public DtObject toDtObject(PageData data) throws ObjectMappingException {
		SimpleDtObject result = new SimpleDtObject(false);
		result.put("count", data.getCount());
		result.put("page", data.getPage());
		result.put("page_size", data.getPageSize());
		result.put("page_count", data.getPageCount());
		DtList items = toDtList(data.getItems());
		result.put("items", items);
		return result;
	}

	@Override
	public PageData fromDtObject(DtObject _obj) throws ObjectMappingException {
		FriendlyObject obj = FriendlyObject.valueOf(_obj);
		PageData data = new PageData();
		data.m_Count = obj.getInt("count", 0);
		data.m_Page = obj.getInt("page", 0);
		data.m_PageSize = obj.getInt("page_size", 0);
		data.m_PageCount = obj.getInt("page_count", 0);
		data.m_ListItems = fromDtList(obj.getList("items"), m_ElementClazz);
		return data;
	}

}
