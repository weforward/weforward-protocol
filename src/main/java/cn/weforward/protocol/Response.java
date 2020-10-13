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

import java.util.List;

import cn.weforward.protocol.datatype.DtObject;

/**
 * 请求响应
 * 
 * @author zhangpengji
 *
 */
public interface Response {

	/** 标识 - 指示网关返回此微服务实例的标识给调用方，后续调用将优先访问此实例 */
	int MARK_KEEP_SERVICE_ORIGIN = 1 << 0;
	/** 标识 - 指示网关清除调用方所持有的微服务实例标识。 */
	int MARK_FORGET_SERVICE_ORIGIN = 1 << 1;

	/**
	 * 获取响应头
	 * 
	 * @return 响应头
	 */
	Header getHeader();

	/**
	 * 设置响应头
	 * 
	 * @param header
	 */
	void setHeader(Header header);

	/**
	 * 获取响应码
	 * 
	 * @return 响应码
	 */
	int getResponseCode();

	/**
	 * 设置响应码
	 * 
	 * @param responseCode
	 */
	void setResponseCode(int responseCode);

	/**
	 * 获取响应码的描述
	 * 
	 * @return 响应码的描述
	 */
	String getResponseMsg();

	/**
	 * 设置响应码的描述
	 * 
	 * @param responseMsg
	 */
	void setResponseMsg(String responseMsg);

	/**
	 * 获取微服务的返回值
	 * 
	 * @return 微服务的返回值
	 */
	DtObject getServiceResult();

	/**
	 * 设置微服务的返回值
	 * 
	 * @param serviceResult
	 */
	void setServiceResult(DtObject serviceResult);

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
	 * 获取资源过期时间（自1970年1月1日起的秒数）
	 * 
	 * @return 资源过期时间
	 */
	long getResourceExpire();

	/**
	 * 设置资源过期时间（自1970年1月1日起的秒数）
	 * 
	 * @param resourceExpire
	 */
	void setResourceExpire(long resourceExpire);

	/**
	 * 获取资源所在的微服务
	 * 
	 * @return 源所在的微服务
	 */
	String getResourceService();

	/**
	 * 设置资源所在的微服务
	 * 
	 * @param resourceService
	 */
	void setResourceService(String resourceService);

	/**
	 * 获取资源的URL
	 * 
	 * @return 资源的URL
	 */
	String getResourceUrl();

	/**
	 * 设置资源的URL
	 * 
	 * @param resourceUrl
	 */
	void setResourceUrl(String resourceUrl);

	/**
	 * 获取“转发至”实例的编号
	 * 
	 * @return “转发至”实例的编号
	 */
	String getForwardTo();

	/**
	 * 设置“转发至”实例的编号
	 * 
	 * @param to
	 */
	void setForwardTo(String to);

	/**
	 * 获取接收到通知的微服务编号列表
	 * 
	 * @return 接收到通知的微服务编号列表
	 */
	List<String> getNotifyReceives();

	/**
	 * 设置接收到通知的微服务编号列表
	 * 
	 * @param serviceNos
	 */
	void setNotifyReceives(List<String> serviceNos);

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
		 * @return 有返回true
		 */
		public static boolean isMark(int mark, int marks) {
			return mark == (mark & marks);
		}

		/**
		 * resp是否包含mark
		 * 
		 * @return 有返回true
		 */
		public static boolean isMark(int mark, Response resp) {
			return mark == (mark & resp.getMarks());
		}
	}

}