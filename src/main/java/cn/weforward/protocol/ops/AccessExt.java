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
package cn.weforward.protocol.ops;

import cn.weforward.protocol.Access;

/**
 * Access的扩展
 * 
 * @author zhangpengji
 *
 */
public interface AccessExt extends Access {

	/** 网关内置access */
	String GATEWAY_INTERNAL_ACCESS_ID = Access.KIND_GATEWAY + Access.SPEARATOR_STR + "gateway" + Access.SPEARATOR_STR
			+ "internal";

	/** 默认access组 */
	String DEFAULT_GROUP = "default";

	/**
	 * 所属组
	 */
	String getGroupId();

	/**
	 * 摘要信息
	 */
	String getSummary();

	/**
	 * 设置摘要信息
	 *
	 * @param summary
	 */
	void setSummary(String summary);

	/**
	 * 设置有效与否
	 * 
	 * @param valid
	 */
	void setValid(boolean valid);
}
