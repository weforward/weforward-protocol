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
package cn.weforward.protocol.client;

import java.util.List;

import cn.weforward.common.ResultPage;

/**
 * 友好服务调用器
 * 
 * @author daibo
 *
 */
public interface FriendlyServiceInvoker {

	/**
	 * 调用方法
	 * 
	 * @param <E>
	 * @param method      方法名
	 * @param params      请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param resultClass 返回类的class 可选值为DtBase和JavaBean
	 * @return 调用结果
	 */
	<E> E invoke(String method, Object params, Class<? extends E> resultClass);

	/**
	 * 调用方法
	 * 
	 * @param <E>
	 * @param method      方法名
	 * @param params      请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param option      选项
	 * @param resultClass 返回类的class 可选值为DtBase和JavaBean
	 * @return 调用结果
	 */
	<E> E invoke(String method, Object params, Class<? extends E> resultClass, FriendlyServiceInvoker.Option option);

	/**
	 * 调用方法
	 * 
	 * @param <E>
	 * @param method         方法名
	 * @param params         请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param resultClass    返回类的class
	 * @param componentClass 返回类的组件，当resultClass为List,ResultPage时需要只指定，该类为其内部元素的类
	 * @return 调用结果
	 */
	<E, V> E invoke(String method, Object params, Class<? extends E> resultClass, Class<V> componentClass);

	/**
	 * 调用方法
	 * 
	 * @param <E>
	 * @param method         方法名
	 * @param params         请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param resultClass    返回类的class
	 * @param componentClass 返回类的组件，当resultClass为List,ResultPage时需要只指定，该类为其内部元素的类
	 * @param option         选项
	 * @return 调用结果
	 */
	<E, V> E invoke(String method, Object params, Class<? extends E> resultClass, final Class<V> componentClass,
			FriendlyServiceInvoker.Option option);

	/**
	 * 调用方法 返回列表
	 * 
	 * @param <E>
	 * @param method         方法名
	 * @param params         请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param componentClass 返回类的组件 可选值为DtBase,FriendlyObject和JavaBean
	 * @return 调用结果
	 */
	<E> List<E> invokeList(String method, Object params, Class<? extends E> componentClass);

	/**
	 * 调用方法 返回列表
	 * 
	 * @param <E>
	 * @param method         方法名
	 * @param params         请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param componentClass 返回类的组件 可选值为DtBase,FriendlyObject和JavaBean
	 * @param option         选项
	 * @return 调用结果
	 */
	<E> List<E> invokeList(String method, Object params, Class<? extends E> componentClass,
			FriendlyServiceInvoker.Option option);

	/**
	 * 调用方法 返回结果集
	 * 
	 * @param <E>
	 * @param method         方法名
	 * @param params         请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param componentClass 返回类的组件 可选值为DtBase,FriendlyObject和JavaBean
	 * @return 调用结果
	 */
	<E> ResultPage<E> invokeResultPage(String method, Object params, Class<? extends E> componentClass);

	/**
	 * 调用方法 返回结果集
	 * 
	 * @param <E>
	 * @param method         方法名
	 * @param params         请求参数可选类型为DtOjbect,FriendlyObject和JavaBean类
	 * @param componentClass 返回类的组件 可选值为DtBase,FriendlyObject和JavaBean
	 * @param option         选项
	 * @return 调用结果
	 */
	<E> ResultPage<E> invokeResultPage(String method, Object params, Class<? extends E> componentClass,
			FriendlyServiceInvoker.Option option);

	/**
	 * 选项
	 * 
	 * @author daibo
	 *
	 */
	public static class Option {
		/** 请求等待时间 */
		protected int m_WaitTimeout;
		/** 微服务版本号 */
		protected String m_Version;
		/** 回源标签 */
		protected String m_Tag;
		/** 响应发现 */
		protected ResponseAware m_ResponseAware;

		/**
		 * 请求等待时间，单位：秒
		 * 
		 * @return 请求等待时间
		 */
		public int getWaitTimeout() {
			return m_WaitTimeout;
		}

		/**
		 * 设置请求等待时间，单位：秒
		 * 
		 * @param timeout
		 */
		public void setWaitTimeout(int timeout) {
			m_WaitTimeout = timeout;
		}

		/**
		 * 微服务版本号
		 * 
		 * @return 版本号
		 */
		public String getVersion() {
			return m_Version;
		}

		/**
		 * 设置微服务版本号
		 * 
		 * @param version
		 */
		public void setVersion(String version) {
			m_Version = version;
		}

		/**
		 * 设置回源标签
		 * 
		 * @param tag
		 */
		public void setTag(String tag) {
			m_Tag = tag;
		}

		/**
		 * 获取回源标签
		 * 
		 * @return 回源标签
		 */
		public String getTag() {
			return m_Tag;
		}

		public ResponseAware getResponseAware() {
			return m_ResponseAware;
		}

		public void setResponseAware(ResponseAware responseAware) {
			m_ResponseAware = responseAware;
		}
	}
}
