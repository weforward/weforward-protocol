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
package cn.weforward.protocol.ops.secure;

/**
 * AclTable中的资源
 * 
 * @author zhangpengji
 *
 */
public interface AclTableResource {

	/** 权限 - 执行 */
	int RIGHT_EXECUTE = 1;
	/** 权限 - 写 */
	int RIGHT_WRITE = 2;
	/** 权限 - 读 */
	int RIGHT_READ = 4;

	/** 匹配模式 - ant风格 */
	int MATCH_TYPE_ANT = 1;
	/** 匹配模式 - 正则表达式 */
	int MATCH_TYPE_REGULAR = 2;

	/**
	 * 资源标识的模式串
	 */
	String getPattern();

	/**
	 * 权限
	 */
	int getRight();

	/**
	 * 匹配模式
	 */
	int getMatchType();
}
