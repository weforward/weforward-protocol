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
package cn.weforward.protocol.support;

import cn.weforward.common.util.StringUtil;

/**
 * 浩宁云规范检查
 * 
 * @author zhangpengji
 *
 */
public class SpecificationChecker {
	/**
	 * 检查名称是否合法
	 * 
	 * @param name
	 * @return 合法则返回null，否则返回错误描述
	 */
	public static String checkServiceName(String name) {
		if (StringUtil.isEmpty(name)) {
			return "名称不能为空";
		}
		if (name.length() > 200) {
			return "名称长度不能大于200";
		}
		if ('.' == name.charAt(0) || '.' == name.charAt(name.length() - 1)) {
			return "名称不能以[.]开头或结尾";
		}
		if ('_' == name.charAt(0)) {
			if (1 == name.length()) {
				return "名称不能为'_'";
			}
			if ('_' == name.charAt(1)) {
				return "名称不能以[__]开头";
			}
		}
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || '_' == ch || (ch >= '0' && ch <= '9') || '_' == ch || '.' == ch) {
				continue;
			}
			return "名称只能包含[a-z|0-9|_|.]";
		}
		return null;
	}

	/**
	 * 检查微服务的实例编号
	 * 
	 * @param no
	 * @return 合法则返回null，否则返回错误描述
	 */
	public static String checkServiceNo(String no) {
		if (null == no || 0 == no.length()) {
			return null;
		}
		if (no.length() > 10) {
			return "实例编号长度不能大于10";
		}
		for (int i = 0; i < no.length(); i++) {
			char ch = no.charAt(i);
			// if (0 == i) {
			// if ('x' != ch && 'm' != ch) {
			// return "实例编号必须以[x|m]开头";
			// }
			// continue;
			// }
			if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
				continue;
			}
			return "实例编号只能包含[a-z|0-9]";
		}
		return null;
	}
}
