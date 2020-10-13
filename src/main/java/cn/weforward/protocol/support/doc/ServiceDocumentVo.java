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

import java.util.List;

import javax.annotation.Resource;

import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.doc.DocMethod;
import cn.weforward.protocol.doc.DocModify;
import cn.weforward.protocol.doc.DocObject;
import cn.weforward.protocol.doc.DocSpecialWord;
import cn.weforward.protocol.doc.DocStatusCode;
import cn.weforward.protocol.doc.ServiceDocument;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.support.datatype.SimpleDtList;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * {@link ServiceDocument}'s view object
 * 
 * @author zhangpengji
 *
 */
public class ServiceDocumentVo implements ServiceDocument {

	/** {@link ServiceDocument#getName()} */
	@Resource
	public String name;
	/** {@link ServiceDocument#getVersion()} */
	@Resource
	public String version;
	/** {@link ServiceDocument#getDescription()} */
	@Resource
	public String description;
	/** {@link ServiceDocument#getModifies()} */
	@Resource
	public List<DocModifyVo> modifies;
	/** {@link ServiceDocument#getMethods()} */
	@Resource
	public List<DocMethodVo> methods;
	/** {@link ServiceDocument#getObjects()} */
	@Resource
	public List<DocObjectVo> objects;
	/** {@link ServiceDocument#getStatusCodes()} */
	@Resource
	public List<DocStatusCodeVo> statusCodes;
	/** {@link ServiceDocument#getSpecialWords()} */
	@Resource
	public List<DocSpecialWordVo> specialWords;
	/** {@link ServiceDocument#getMarks()} */
	@Resource
	public int marks;

	public static final ObjectMapper<ServiceDocumentVo> MAPPER = new ObjectMapper<ServiceDocumentVo>() {

		@Override
		public DtObject toDtObject(ServiceDocumentVo doc) throws ObjectMappingException {
			if (null == doc) {
				return null;
			}
			SimpleDtObject obj = new SimpleDtObject();
			obj.put("name", doc.name);
			obj.put("version", doc.version);
			obj.put("description", doc.description);
			obj.put("modifies", SimpleDtList.toDtList(doc.modifies, DocModifyVo.MAPPER));
			obj.put("methods", SimpleDtList.toDtList(doc.methods, DocMethodVo.MAPPER));
			obj.put("objects", SimpleDtList.toDtList(doc.objects, DocObjectVo.MAPPER));
			obj.put("status_codes", SimpleDtList.toDtList(doc.statusCodes, DocStatusCodeVo.MAPPER));
			obj.put("special_words", SimpleDtList.toDtList(doc.specialWords, DocSpecialWordVo.MAPPER));
			obj.put("marks", doc.marks);
			return obj;
		}

		@Override
		public String getName() {
			return ServiceDocumentVo.class.getSimpleName();
		}

		@Override
		public ServiceDocumentVo fromDtObject(DtObject hyObject) throws ObjectMappingException {
			if (null == hyObject) {
				return null;
			}
			ServiceDocumentVo doc = new ServiceDocumentVo();
			DtString name = hyObject.getString("name");
			if (null != name) {
				doc.name = name.value();
			}
			DtString version = hyObject.getString("version");
			if (null != version) {
				doc.version = version.value();
			}
			DtString description = hyObject.getString("description");
			if (null != description) {
				doc.description = description.value();
			}
			DtList modifies = hyObject.getList("modifies");
			if (null != modifies) {
				doc.modifies = SimpleDtList.fromDtList(modifies, DocModifyVo.MAPPER);
			}
			DtList methods = hyObject.getList("methods");
			if (null != methods) {
				doc.methods = SimpleDtList.fromDtList(methods, DocMethodVo.MAPPER);
			}
			DtList objects = hyObject.getList("objects");
			if (null != objects) {
				doc.objects = SimpleDtList.fromDtList(objects, DocObjectVo.MAPPER);
			}
			DtList statusCodes = hyObject.getList("status_codes");
			if (null != statusCodes) {
				doc.statusCodes = SimpleDtList.fromDtList(statusCodes, DocStatusCodeVo.MAPPER);
			}
			DtList specialWords = hyObject.getList("special_words");
			if (null != specialWords) {
				doc.specialWords = SimpleDtList.fromDtList(specialWords, DocSpecialWordVo.MAPPER);
			}
			DtNumber marks = hyObject.getNumber("marks");
			if (null != marks) {
				doc.marks = marks.valueInt();
			}
			return doc;
		}
	};

	public ServiceDocumentVo() {

	}

	public ServiceDocumentVo(ServiceDocument doc) {
		this.name = doc.getName();
		this.version = doc.getVersion();
		this.description = doc.getDescription();
		this.modifies = DocModifyVo.toVoList(doc.getModifies());
		this.methods = DocMethodVo.toVoList(doc.getMethods());
		this.objects = DocObjectVo.toVoList(doc.getObjects());
		this.statusCodes = DocStatusCodeVo.toVoList(doc.getStatusCodes());
		this.specialWords = DocSpecialWordVo.toVoList(doc.getSpecialWords());
		this.marks = doc.getMarks();
	}

	public static ServiceDocumentVo valueOf(ServiceDocument doc) {
		if (null == doc) {
			return null;
		}
		if (doc instanceof ServiceDocumentVo) {
			return (ServiceDocumentVo) doc;
		}
		return new ServiceDocumentVo(doc);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DocModify> getModifies() {
		return (List<DocModify>) (List<?>) modifies;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DocMethod> getMethods() {
		return (List<DocMethod>) (List<?>) methods;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DocObject> getObjects() {
		return (List<DocObject>) (List<?>) objects;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DocStatusCode> getStatusCodes() {
		return (List<DocStatusCode>) (List<?>) statusCodes;
	}

	@Override
	public int getMarks() {
		return marks;
	}

	@Override
	public boolean isMark(int mark) {
		return mark == (mark & marks);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DocSpecialWord> getSpecialWords() {
		return (List<DocSpecialWord>) (List<?>) specialWords;
	}
}
