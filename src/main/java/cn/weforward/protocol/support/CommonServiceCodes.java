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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.weforward.protocol.StatusCode;

/**
 * 通用的微服务状态码集合
 * 
 * @author zhangpengji
 *
 */
public class CommonServiceCodes {

	/** 状态码 - 成功 */
	public static final StatusCode SUCCESS = new StatusCode(0, "成功");
	/** 状态码 - 方法不存在 */
	public static final StatusCode METHOD_NOT_FOUND = new StatusCode(10001, "方法不存在");
	/** 状态码 - 无权限调用此方法 */
	public static final StatusCode INSUFFICIENT_PERMISSIONS = new StatusCode(10002, "无权限调用此方法");
	/** 状态码 - 未登录 */
	public static final StatusCode NO_LOGIN = new StatusCode(10003, "未登录");
	/** 状态码 - 参数不合法 */
	public static final StatusCode ILLEGAL_ARGUMENT = new StatusCode(20001, "参数不合法");
	/** 状态码 - 内部错误 */
	public static final StatusCode INTERNAL_ERROR = new StatusCode(30001, "内部错误");

	protected static List<StatusCode> CODES = Arrays.asList(SUCCESS, METHOD_NOT_FOUND, INSUFFICIENT_PERMISSIONS,
			NO_LOGIN, ILLEGAL_ARGUMENT, INTERNAL_ERROR);

	protected CommonServiceCodes() {
	}

	/**
	 * 获取全部状态码
	 * 
	 */
	public static final List<StatusCode> getCodes() {
		return CODES;
	}

	/**
	 * 追加状态码
	 * 
	 * @param codes
	 */
	protected static final void append(StatusCode... codes) {
		// cow
		List<StatusCode> list = new ArrayList<StatusCode>(CODES.size() + codes.length);
		list.addAll(CODES);
		for (StatusCode code : codes) {
			list.add(code);
		}
		CODES = list;
	}
}
