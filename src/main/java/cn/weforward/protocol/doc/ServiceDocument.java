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
package cn.weforward.protocol.doc;

import java.util.List;

/**
 * 微服务的文档。以微服务名+版本号为唯一标识
 * 
 * @author zhangpengji
 *
 */
public interface ServiceDocument {

	/**
	 * 微服务名称
	 */
	String getName();

	/**
	 * 微服务版本
	 */
	String getVersion();

	/**
	 * 微服务说明
	 */
	String getDescription();

	/**
	 * 修改记录
	 */
	List<DocModify> getModifies();

	/**
	 * 方法集
	 */
	List<DocMethod> getMethods();

	/**
	 * 通用对象
	 */
	List<DocObject> getObjects();

	/**
	 * 状态码
	 */
	List<DocStatusCode> getStatusCodes();

	/**
	 * 特殊名词
	 */
	List<DocSpecialWord> getSpecialWords();

	/**
	 * 所带的标识
	 */
	int getMarks();

	/**
	 * 是否带有此标识
	 * 
	 * @param mark
	 * @return 是返回true
	 */
	boolean isMark(int mark);
}
