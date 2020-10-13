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
package cn.weforward.protocol.support.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.doc.DocAttribute;
import cn.weforward.protocol.doc.DocObject;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.support.datatype.SimpleDtList;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * {@link DocObject}'s view object
 * 
 * @author zhangpengji
 *
 */
public class DocObjectVo implements DocObject, Comparable<DocObjectVo> {

	/** {@link DocObject#getName()} */
	@Resource
	public String name;
	/** {@link DocObject#getDescription()} */
	@Resource
	public String description;
	/** {@link DocObject#getName()} */
	@Resource
	public List<DocAttributeVo> attributes;
	/** 位置 */
	public int index;

	public static final ObjectMapper<DocObjectVo> MAPPER = new ObjectMapper<DocObjectVo>() {

		@Override
		public String getName() {
			return DocObjectVo.class.getSimpleName();
		}

		@Override
		public DtObject toDtObject(DocObjectVo object) throws ObjectMappingException {
			if (null == object) {
				return null;
			}
			SimpleDtObject obj = new SimpleDtObject();
			obj.put("name", object.name);
			obj.put("description", object.description);
			obj.put("attributes", SimpleDtList.toDtList(object.attributes, DocAttributeVo.MAPPER));
			return obj;
		}

		@Override
		public DocObjectVo fromDtObject(DtObject hyObject) throws ObjectMappingException {
			if (null == hyObject) {
				return null;
			}
			DocObjectVo object = new DocObjectVo();
			DtString name = hyObject.getString("name");
			if (null != name) {
				object.name = name.value();
			}
			DtString description = hyObject.getString("description");
			if (null != description) {
				object.description = description.value();
			}
			DtList attributes = hyObject.getList("attributes");
			if (null != attributes) {
				object.attributes = SimpleDtList.fromDtList(attributes, DocAttributeVo.MAPPER);
			}
			return object;
		}
	};

	/**
	 * 通用的分页结果集对象
	 */
	public static final DocObjectVo RESULT_PAGE;
	static {
		RESULT_PAGE = new DocObjectVo();
		RESULT_PAGE.name = "ResultPage";
		RESULT_PAGE.description = "分页数据集合";

		List<DocAttributeVo> atts = new ArrayList<DocAttributeVo>();
		DocAttributeVo count = new DocAttributeVo();
		count.name = "count";
		count.description = "总项数";
		count.type = DataType.NUMBER.value;
		atts.add(count);

		DocAttributeVo page = new DocAttributeVo();
		page.name = "page";
		page.description = "当前页";
		page.type = DataType.NUMBER.value;
		atts.add(page);

		DocAttributeVo pageCount = new DocAttributeVo();
		pageCount.name = "page_count";
		pageCount.description = "总页数";
		pageCount.type = DataType.NUMBER.value;
		atts.add(pageCount);

		DocAttributeVo pageSize = new DocAttributeVo();
		pageSize.name = "page_size";
		pageSize.description = "每页项数";
		pageSize.type = DataType.NUMBER.value;
		atts.add(pageSize);

		DocAttributeVo items = new DocAttributeVo();
		items.name = "items";
		items.description = "当前页元素列表";
		items.type = DataType.LIST.value;
		atts.add(items);

		RESULT_PAGE.attributes = atts;
	}

	public DocObjectVo() {

	}

	public DocObjectVo(DocObject obj) {
		this.name = obj.getName();
		this.description = obj.getDescription();
		this.attributes = DocAttributeVo.toVoList(obj.getAttributes());
	}

	public static DocObjectVo valueOf(DocObject obj) {
		if (null == obj) {
			return null;
		}
		if (obj instanceof DocObjectVo) {
			return (DocObjectVo) obj;
		}
		return new DocObjectVo(obj);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DocAttribute> getAttributes() {
		return (List<DocAttribute>) (List<?>) attributes;
	}

	public static List<DocObjectVo> toVoList(List<DocObject> list) {
		if (null == list || 0 == list.size()) {
			return Collections.emptyList();
		}
		List<DocObjectVo> vos = new ArrayList<>(list.size());
		for (DocObject o : list) {
			vos.add(DocObjectVo.valueOf(o));
		}
		return vos;
	}

	@Override
	public int compareTo(DocObjectVo o) {
		int v1 = index == 0 ? Integer.MAX_VALUE : index;
		int v2 = o.index == 0 ? Integer.MAX_VALUE : o.index;
		int v = v1 - v2;
		if (v == 0) {
			String n1 = StringUtil.toString(name);
			String n2 = StringUtil.toString(o.name);
			return n1.compareTo(n2);
		} else {
			return v;
		}
	}
}
