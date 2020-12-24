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
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.doc.DocAttribute;
import cn.weforward.protocol.doc.DocMethod;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.support.datatype.FriendlyList;
import cn.weforward.protocol.support.datatype.SimpleDtList;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * {@link DocMethod}'s view object
 * 
 * @author zhangpengji
 *
 */
public class DocMethodVo implements DocMethod, Comparable<DocMethodVo> {

	/** {@link DocMethod#getName()} */
	@Resource
	public String name;
	/** {@link DocMethod#getTitle()} */
	@Resource
	public String title;
	/** {@link DocMethod#getDescription()} */
	@Resource
	public String description;
	/** {@link DocMethod#getParams()} */
	@Resource
	public List<DocAttributeVo> params;
	/** {@link DocMethod#getReturns()} */
	@Resource
	public List<DocAttributeVo> returns;
	/** 再方法列表中的位置 */
	public int index;
	/** {@link #getAccessKinds()} */
	@Resource
	public List<String> accessKinds;

	public static final ObjectMapper<DocMethodVo> MAPPER = new ObjectMapper<DocMethodVo>() {

		@Override
		public DtObject toDtObject(DocMethodVo method) throws ObjectMappingException {
			if (null == method) {
				return null;
			}
			SimpleDtObject obj = new SimpleDtObject();
			obj.put("name", method.name);
			obj.put("title", method.title);
			obj.put("description", method.description);
			obj.put("params", SimpleDtList.toDtList(method.params, DocAttributeVo.MAPPER));
			obj.put("returns", SimpleDtList.toDtList(method.returns, DocAttributeVo.MAPPER));
			obj.put("access_kinds", SimpleDtList.stringOf(method.accessKinds));
			return obj;
		}

		@Override
		public String getName() {
			return DocMethodVo.class.getSimpleName();
		}

		@Override
		public DocMethodVo fromDtObject(DtObject dtObject) throws ObjectMappingException {
			if (null == dtObject) {
				return null;
			}
			DocMethodVo method = new DocMethodVo();
			DtString name = dtObject.getString("name");
			if (null != name) {
				method.name = name.value();
			}
			DtString title = dtObject.getString("title");
			if (null != title) {
				method.title = title.value();
			}
			DtString description = dtObject.getString("description");
			if (null != description) {
				method.description = description.value();
			}
			DtList params = dtObject.getList("params");
			if (null != params) {
				method.params = FriendlyList.toList(params, DocAttributeVo.MAPPER);
			}
			DtList returns = dtObject.getList("returns");
			if (null != returns) {
				method.returns = FriendlyList.toList(returns, DocAttributeVo.MAPPER);
			}
			DtList accessKinds = dtObject.getList("access_kinds");
			if (null != accessKinds) {
				method.accessKinds = FriendlyList.toStringList(accessKinds);
			}
			return method;
		}
	};

	public DocMethodVo() {

	}

	public DocMethodVo(DocMethod method) {
		this.name = method.getName();
		this.title = method.getTitle();
		this.description = method.getDescription();
		this.params = DocAttributeVo.toVoList(method.getParams());
		this.returns = DocAttributeVo.toVoList(method.getReturns());
		this.accessKinds = method.getAccessKinds();
	}

	public static DocMethodVo valueOf(DocMethod method) {
		if (null == method) {
			return null;
		}
		if (method instanceof DocMethodVo) {
			return (DocMethodVo) method;
		}
		return new DocMethodVo(method);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DocAttribute> getParams() {
		return (List<DocAttribute>) (List<?>) params;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DocAttribute> getReturns() {
		return (List<DocAttribute>) (List<?>) returns;
	}

	public static List<DocMethodVo> toVoList(List<DocMethod> list) {
		if (null == list || 0 == list.size()) {
			return Collections.emptyList();
		}
		List<DocMethodVo> vos = new ArrayList<>(list.size());
		for (DocMethod m : list) {
			vos.add(DocMethodVo.valueOf(m));
		}
		return vos;
	}

	@Override
	public int compareTo(DocMethodVo o) {
		int v1 = index == 0 ? Integer.MAX_VALUE : index;
		int v2 = o.index == 0 ? Integer.MAX_VALUE : o.index;
		int v = v1 - v2;
		if (v == 0) {
			String n1 = StringUtil.toString(name);
			String n2 = StringUtil.toString(o.name);
			return compareTo(n1, n2);
		} else {
			return v;
		}
	}

	public int compareTo(String n1, String n2) {
		String arr1[] = n1.split("\\/");
		String arr2[] = n2.split("\\/");
		int goupsoff = arr1.length - arr2.length;
		if (0 == goupsoff) {
			for (int i = 0; i < arr1.length; i++) {
				int c = arr1[i].compareTo(arr2[i]);
				if (c != 0) {
					return c;
				}
			}
			return 0;
		}
		return goupsoff;
	}

	@Override
	public List<String> getAccessKinds() {
		return accessKinds;
	}
}
