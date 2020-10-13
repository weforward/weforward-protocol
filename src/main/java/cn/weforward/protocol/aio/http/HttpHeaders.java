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
package cn.weforward.protocol.aio.http;

import java.util.Enumeration;

import cn.weforward.common.Dictionary;

/**
 * HTTP头信息（元数据）
 * 
 * @author liangyi
 * 
 */
public interface HttpHeaders extends Dictionary<String, String> {

	/**
	 * 取得指定名称的原生（未urldecode）头信息
	 * 
	 * @param name 名称
	 * @return 相应的值
	 */
	public String getHeaderRaw(String name);

	/**
	 * HTTP头的个数
	 * 
	 * @return 个数
	 */
	public int size();

	/**
	 * HTTP头名称列表
	 */
	public Enumeration<String> names();
}
