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
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import cn.weforward.protocol.datatype.DtDate;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.doc.DocModify;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * {@link DocModify}'s view object
 * 
 * @author zhangpengji
 *
 */
public class DocModifyVo implements DocModify {

	/** {@link DocModify#getAuthor()} */
	@Resource
	public String author;
	/** {@link DocModify#getDate()} */
	@Resource
	public Date date;
	/** {@link DocModify#getContent()} */
	@Resource
	public String content;

	public static final ObjectMapper<DocModifyVo> MAPPER = new ObjectMapper<DocModifyVo>() {

		@Override
		public String getName() {
			return ObjectMapper.class.getSimpleName();
		}

		@Override
		public DtObject toDtObject(DocModifyVo modify) throws ObjectMappingException {
			if(null == modify) {
				return null;
			}
			SimpleDtObject obj = new SimpleDtObject();
			obj.put("author", modify.author);
			obj.put("date", modify.date);
			obj.put("content", modify.content);
			return obj;
		}

		@Override
		public DocModifyVo fromDtObject(DtObject hyObject) throws ObjectMappingException {
			if(null == hyObject) {
				return null;
			}
			DocModifyVo modify = new DocModifyVo();
			DtString author = hyObject.getString("author");
			if (null != author) {
				modify.author = author.value();
			}
			DtDate date = hyObject.getDate("date");
			if (null != date) {
				modify.date = date.valueDate();
			}
			DtString content = hyObject.getString("content");
			if (null != content) {
				modify.content = content.value();
			}
			return modify;
		}
	};

	public DocModifyVo() {

	}

	public DocModifyVo(DocModify modify) {
		this.author = modify.getAuthor();
		this.date = modify.getDate();
		this.content = modify.getContent();
	}

	public static DocModifyVo valueOf(DocModify modify) {
		if (null == modify) {
			return null;
		}
		if (modify instanceof DocModifyVo) {
			return (DocModifyVo) modify;
		}
		return new DocModifyVo(modify);
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public String getContent() {
		return content;
	}

	public static List<DocModifyVo> toVoList(List<DocModify> list) {
		if (null == list || 0 == list.size()) {
			return Collections.emptyList();
		}
		List<DocModifyVo> vos = new ArrayList<>(list.size());
		for (DocModify m : list) {
			vos.add(DocModifyVo.valueOf(m));
		}
		return vos;
	}
}
