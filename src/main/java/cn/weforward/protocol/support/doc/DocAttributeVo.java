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
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.doc.DocAttribute;
import cn.weforward.protocol.doc.DocObject;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * {@link DocAttribute}'s view object
 * 
 * @author zhangpengji
 *
 */
public class DocAttributeVo implements DocAttribute, Comparable<DocAttributeVo> {

	/** {@link DocAttribute#getName()} */
	@Resource
	public String name;
	/** {@link DocAttribute#getType()} */
	@Resource
	public String type;
	/** {@link DocAttribute#getComponent()} */
	@Resource
	public String component;
	/** {@link DocAttribute#getDetail()} */
	@Resource
	public DocObjectVo detail;
	/** {@link DocAttribute#getDescription()} */
	@Resource
	public String description;
	/** {@link DocAttribute#getExample()} */
	@Resource
	public String example;
	/** {@link DocAttribute#getMarks()} */
	@Resource
	public int marks;
	/** 位置 */
	public int index;

	public static final ObjectMapper<DocAttributeVo> MAPPER = new ObjectMapper<DocAttributeVo>() {

		@Override
		public String getName() {
			return DocAttributeVo.class.getSimpleName();
		}

		@Override
		public DtObject toDtObject(DocAttributeVo att) throws ObjectMappingException {
			if (null == att) {
				return null;
			}
			SimpleDtObject obj = new SimpleDtObject();
			obj.put("name", att.name);
			obj.put("type", att.type);
			obj.put("component", att.component);
			obj.put("detail", DocObjectVo.MAPPER.toDtObject(att.detail));
			obj.put("description", att.description);
			obj.put("marks", att.marks);
			obj.put("example", att.example);
			return obj;
		}

		@Override
		public DocAttributeVo fromDtObject(DtObject hyObject) throws ObjectMappingException {
			if (null == hyObject) {
				return null;
			}
			DocAttributeVo att = new DocAttributeVo();
			DtString name = hyObject.getString("name");
			if (null != name) {
				att.name = name.value();
			}
			DtString type = hyObject.getString("type");
			if (null != type) {
				att.type = type.value();
			}
			DtString component = hyObject.getString("component");
			if (null != component) {
				att.component = component.value();
			}
			DtObject detail = hyObject.getObject("detail");
			if (null != detail) {
				att.detail = DocObjectVo.MAPPER.fromDtObject(detail);
			}
			DtString description = hyObject.getString("description");
			if (null != description) {
				att.description = description.value();
			}
			DtNumber marks = hyObject.getNumber("marks");
			if (null != marks) {
				att.marks = marks.valueInt();
			}
			DtString example = hyObject.getString("example");
			if (null != example) {
				att.example = example.value();
			}
			return att;
		}

	};

	public DocAttributeVo() {

	}

	public DocAttributeVo(DocAttribute att) {
		this.name = att.getName();
		this.type = att.getType();
		this.component = att.getComponent();
		this.detail = DocObjectVo.valueOf(att.getDetail());
		this.description = att.getDescription();
		this.example = att.getExample();
		this.marks = att.getMarks();
	}

	public static DocAttributeVo valueOf(DocAttribute att) {
		if (null == att) {
			return null;
		}
		if (att instanceof DocAttributeVo) {
			return (DocAttributeVo) att;
		}
		return new DocAttributeVo(att);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getExample() {
		return example;
	}

	@Override
	public int getMarks() {
		return marks;
	}

	@Override
	public boolean isMark(int mark) {
		return mark == (mark & marks);
	}

	public static List<DocAttributeVo> toVoList(List<DocAttribute> list) {
		if (null == list || 0 == list.size()) {
			return Collections.emptyList();
		}
		List<DocAttributeVo> vos = new ArrayList<>(list.size());
		for (DocAttribute att : list) {
			vos.add(DocAttributeVo.valueOf(att));
		}
		return vos;
	}

	@Override
	public String getComponent() {
		return component;
	}

	@Override
	public DocObject getDetail() {
		return detail;
	}

	@Override
	public int compareTo(DocAttributeVo o) {
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
