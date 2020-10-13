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

import java.util.Date;

import cn.weforward.protocol.Service;

/**
 * <code>Service</code>的扩展
 * 
 * @author zhangpengji
 *
 */
public interface ServiceExt extends Service {

	/** 状态 - 实例心跳超时 */
	int STATE_TIMEOUT = 0x10000;
	/** 标识 - 实例不可达 */
	int STATE_INACCESSIBLE = 0x20000;
	/** 标识 - 实例过载 */
	int STATE_OVERLOAD = 0x40000;
	/** 标识 - 实例不可用 */
	int STATE_UNAVAILABLE = 0x80000;

	/**
	 * 所有者的Access Id
	 */
	String getOwner();

	/**
	 * 状态
	 */
	int getState();

	/**
	 * 最后心跳时间
	 */
	Date getHeartbeat();

	/**
	 * 是否心跳超时
	 */
	boolean isTimeout();

	/**
	 * 是否不可达
	 */
	boolean isInaccessible();

	/**
	 * 是否不可用
	 */
	boolean isUnavailable();

	/**
	 * 是否过载
	 */
	boolean isOverload();
}
