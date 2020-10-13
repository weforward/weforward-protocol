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

import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.doc.DocStatusCode;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * {@link DocStatusCode}'s view object
 * 
 * @author zhangpengji
 *
 */
public class DocStatusCodeVo implements DocStatusCode {

	/** {@link DocStatusCode#getCode()} */
	@Resource
	public int code;
	/** {@link DocStatusCode#getMessage()} */
	@Resource
	public String message;

	public static final ObjectMapper<DocStatusCodeVo> MAPPER = new ObjectMapper<DocStatusCodeVo>() {

		@Override
		public DtObject toDtObject(DocStatusCodeVo code) throws ObjectMappingException {
			if(null == code) {
				return null;
			}
			SimpleDtObject obj = new SimpleDtObject();
			obj.put("code", code.code);
			obj.put("message", code.message);
			return obj;
		}

		@Override
		public String getName() {
			return DocStatusCodeVo.class.getSimpleName();
		}

		@Override
		public DocStatusCodeVo fromDtObject(DtObject hyObject) throws ObjectMappingException {
			if(null == hyObject) {
				return null;
			}
			DocStatusCodeVo sc = new DocStatusCodeVo();
			DtNumber code = hyObject.getNumber("code");
			if (null != code) {
				sc.code = code.valueInt();
			}
			DtString message = hyObject.getString("message");
			if (null != message) {
				sc.message = message.value();
			}
			return sc;
		}
	};

	public DocStatusCodeVo() {

	}

	public DocStatusCodeVo(DocStatusCode code) {
		this.code = code.getCode();
		this.message = code.getMessage();
	}

	public static DocStatusCodeVo valueOf(DocStatusCode code) {
		if (null == code) {
			return null;
		}
		if (code instanceof DocStatusCodeVo) {
			return (DocStatusCodeVo) code;
		}
		return new DocStatusCodeVo(code);
	}

	public static List<DocStatusCodeVo> toVoList(List<DocStatusCode> list) {
		if (null == list || 0 == list.size()) {
			return Collections.emptyList();
		}
		List<DocStatusCodeVo> vos = new ArrayList<>(list.size());
		for (DocStatusCode code : list) {
			vos.add(DocStatusCodeVo.valueOf(code));
		}
		return vos;
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
