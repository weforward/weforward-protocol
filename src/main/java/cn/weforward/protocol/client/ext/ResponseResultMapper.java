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
package cn.weforward.protocol.client.ext;

import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.support.AbstractObjectMapper;
import cn.weforward.protocol.support.datatype.FriendlyObject;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * 微服务返回结果的映射器
 * 
 * @author zhangpengji
 *
 */
public class ResponseResultMapper extends AbstractObjectMapper<ResponseResultObject> {

	public ResponseResultMapper(ObjectMapperSet mappers) {
		setMappers(mappers);
	}

	@Override
	public String getName() {
		return ResponseResultObject.class.getSimpleName();
	}

	@Override
	public DtObject toDtObject(ResponseResultObject object) throws ObjectMappingException {
		SimpleDtObject result = new SimpleDtObject(false);
		result.put("code", object.code);
		result.put("msg", object.msg);
		if (null == object.content) {
			return result;
		}
		result.put("content", toDtBase(object.content));
		return result;
	}

	@Override
	public ResponseResultObject fromDtObject(DtObject hyObject) throws ObjectMappingException {
		FriendlyObject obj = FriendlyObject.valueOf(hyObject);
		int code = obj.getInt("code", 0);
		String msg = obj.getString("msg");
		DtObject content = obj.getObject("content");
		return new ResponseResultObject(code, msg, content);
	}
}
