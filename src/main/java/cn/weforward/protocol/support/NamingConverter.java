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

import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.common.util.StringUtil;

/**
 * 命名规范转换
 * 
 * @author zhangpengji
 *
 */
public class NamingConverter {

	/**
	 * 驼峰式转weforward式
	 * 
	 * @param name
	 */
	public static String camelToWf(String name) {
		return camelToWf(name, null);
	}

	/**
	 * 驼峰式转weforward式
	 * 
	 * @param name
	 * @param prefix 前缀。若不为空，返回值不包含此前缀，且此前缀后的第一个字符直接转为小写
	 */
	public static String camelToWf(String name, String prefix) {
		if (null == name || 0 == name.length() || (null != prefix && prefix.length() >= name.length())) {
			return name;
		}
		int offset = (null == prefix ? 0 : prefix.length());
		int i = offset;
		for (; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (Character.isUpperCase(ch)) {
				break;
			}
		}
		if (i >= name.length()) {
			return name;
		}
		StringBuilder sb = StringBuilderPool._128.poll();
		try {
			if (i > offset) {
				sb.append(name.substring(offset, i));
			}
			for (; i < name.length(); i++) {
				char ch = name.charAt(i);
				if (Character.isUpperCase(ch)) {
					if (i > offset) {
						sb.append('_');
					}
					sb.append(Character.toLowerCase(ch));
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		} finally {
			StringBuilderPool._128.offer(sb);
		}
	}

	/**
	 * weforward式转驼峰式
	 * 
	 * @param name
	 */
	public static String wfToCamel(String name) {
		return wfToCamel(name, null);
	}

	/**
	 * weforward式转驼峰式
	 * 
	 * @param name
	 * @param prefix 前缀。前缀。若不为空，返回值以此前缀开头，且此前缀后的第一个字符直接转为大写
	 */
	public static String wfToCamel(String name, String prefix) {
		if (null == name || 0 == name.length()) {
			return name;
		}
		int i = 0;
		for (; i < name.length(); i++) {
			char ch = name.charAt(i);
			if ('_' == ch) {
				break;
			}
		}
		if (i >= name.length()) {
			if (!StringUtil.isEmpty(prefix)) {
				return prefix + Character.toUpperCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
			}
			return name;
		}
		StringBuilder sb = StringBuilderPool._128.poll();
		try {
			if (!StringUtil.isEmpty(prefix)) {
				sb.append(prefix);
			}
			if (i > 0) {
				sb.append(Character.toUpperCase(name.charAt(0)));
				if (i > 1) {
					sb.append(name.substring(1, i));
				}
			}
			boolean flag = false;
			for (; i < name.length(); i++) {
				char ch = name.charAt(i);
				if ('_' == ch) {
					flag = true;
					continue;
				}
				if (flag) {
					sb.append(Character.toUpperCase(ch));
					flag = false;
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		} finally {
			StringBuilderPool._128.offer(sb);
		}
	}

}
