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
package cn.weforward.protocol;

import cn.weforward.protocol.datatype.DtObject;

/**
 * 调用请求
 * 
 * @author zhangpengji
 *
 */
public interface Request {
	/** 标识 - 请求支持转发 */
	int MARK_SUPPORT_FORWARD = 1 << 0;
	/** 标识 - 通知使用广播模式（配合<code>notify</code>信道使用） */
	int MARK_NOTIFY_BROADCAST = 1 << 1;

	/**
	 * 获取请求头
	 * 
	 * @return 请求头
	 */
	Header getHeader();

	/**
	 * 设置请求头
	 * 
	 * @param header
	 */
	void setHeader(Header header);
	
	/**
	 * 设置请求等待时间，单位：秒
	 * 
	 * @param timeout
	 */
	void setWaitTimeout(int timeout);

	/**
	 * 获取请求等待时间，单位：秒
	 * 
	 * @return 请求等待时间
	 */
	int getWaitTimeout();

	/**
	 * 获取请求方（客户端）的Access
	 * 
	 * @return Access
	 */
	Access getAccess();

	/**
	 * 设置请求方（客户端）的Access
	 * 
	 * @param access
	 */
	void setAccess(Access access);

	/**
	 * 获取请求方（客户端）的地址
	 * 
	 * @return 地址
	 */
	String getAddr();

	/**
	 * 设置请求方（客户端）的地址
	 * 
	 * @param addr
	 */
	void setAddr(String addr);

	/**
	 * 获取服务的请求参数
	 * 
	 * @return 请求参数
	 */
	DtObject getServiceInvoke();

	/**
	 * 设置服务的请求参数
	 * 
	 * @param invoke
	 */
	void setServiceInvoke(DtObject invoke);

	/**
	 * 获取资源标识
	 * 
	 * @return 资源标识
	 */
	String getResourceId();

	/**
	 * 设置资源标识
	 * 
	 * @param resourceId
	 */
	void setResourceId(String resourceId);

	/**
	 * 获取资源权限
	 * 
	 * @return 资源权限
	 */
	int getResourceRight();

	/**
	 * 设置资源权限
	 * 
	 * @param resourceRight
	 */
	void setResourceRight(int resourceRight);

	/**
	 * 获取微服务版本号
	 * 
	 * @return 版本号
	 */
	String getVersion();

	/**
	 * 设置微服务版本号
	 * 
	 * @param version
	 */
	void setVersion(String version);

	/**
	 * 获取微服务调用跟踪令牌
	 * 
	 * @return 跟踪令牌
	 */
	String getTraceToken();

	/**
	 * 设置微服务调用跟踪令牌
	 * 
	 * @param token
	 */
	void setTraceToken(String token);

	/**
	 * 获取标识
	 * 
	 * @return 标识
	 */
	int getMarks();

	/**
	 * 设置标识
	 * 
	 * @param marks
	 */
	void setMarks(int marks);

	/**
	 * 是否含有此标识
	 * 
	 * @return 有返回true
	 */
	boolean isMark(int mark);

	static class Helper {

		/**
		 * marks是否包含mark
		 * 
		 * @return 包含返回true
		 */
		public static boolean isMark(int mark, int marks) {
			return mark == (mark & marks);
		}

		/**
		 * resp是否包含mark
		 * 
		 * @return 包含返回true
		 */
		public static boolean isMark(int mark, Request req) {
			return mark == (mark & req.getMarks());
		}
	}

}