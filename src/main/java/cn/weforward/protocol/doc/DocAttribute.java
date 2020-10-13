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

/**
 * 微服务文档的属性
 * 
 * @author zhangpengji
 *
 */
public interface DocAttribute {

	/** 标识 - 本属性为必须 */
	static final int MARK_NECESSARY = 1;

	/**
	 * 名称
	 */
	String getName();

	/**
	 * 类型。<br/>
	 * 取值：浩宁云基础类型名、自定义类名
	 */
	String getType();

	/**
	 * 当Type为List、ResultPage时，需要描述集合元素的类型。<br/>
	 * 取值同Type，排除List、ResultPage
	 */
	String getComponent();

	/**
	 * type的详细描述。<br/>
	 * 当Type非基础类型和非通用对象时，需要提供
	 */
	DocObject getDetail();

	/**
	 * 说明
	 */
	String getDescription();

	/**
	 * 示例值。<br/>
	 * 做为方法入参，且Type为基础类型、List时，可以返回示例值，示例值将显示在页面中。示例值写法参考：
	 * <p>
	 * String : abc
	 * </p>
	 * <p>
	 * Number : 1
	 * </p>
	 * <p>
	 * Boolean : true
	 * </p>
	 * <p>
	 * Date : 2020-01-01T01:23:45.678Z
	 * </p>
	 * <p>
	 * List&lt;String&gt; : ["a","b","c"]
	 * </p>
	 * <p>
	 * List&lt;Number&gt; : [1,2,3]
	 * </p>
	 * <p>
	 * List&lt;Boolean&gt; : [true,false]
	 * </p>
	 * <p>
	 * List&lt;Date&gt; : ["2020-01-01T01:23:45.678Z","2020-01-02T01:23:45.678Z"]
	 * </p>
	 */
	String getExample();

	/**
	 * 所带的标识
	 */
	int getMarks();

	/**
	 * 是否带有此标识
	 * 
	 * @param mark
	 * @return 有返回true
	 */
	boolean isMark(int mark);
}
