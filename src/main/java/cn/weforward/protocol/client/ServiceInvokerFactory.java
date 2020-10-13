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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.AccessLoader;
import cn.weforward.protocol.ext.Producer;
import cn.weforward.protocol.support.SimpleProducer;

/**
 * 调用器类工厂
 * 
 * @author zhangpengji
 *
 */
public class ServiceInvokerFactory {

	/**
	 * 根据Http前缀链接与服务名创建调用器
	 * 
	 * @param serviceName 微服务名
	 * @param preUrl      前缀链接，支持按“;”拼接多个
	 * @param accessId    访问id
	 * @param accessKey   访问key（16进制或base64格式）
	 * @return 服务调用器
	 */
	public static ServiceInvoker create(String serviceName, String preUrl, String accessId, String accessKey) {
		List<String> urls = Collections.emptyList();
		if (!StringUtil.isEmpty(preUrl)) {
			urls = Arrays.asList(preUrl.split(";"));
		}
		return create(serviceName, urls, accessId, accessKey);
	}

	/**
	 * 根据Http前缀链接与服务名创建调用器
	 * 
	 * @param serviceName 微服务名
	 * @param preUrls     前缀链接列表
	 * @param accessId    访问id
	 * @param accessKey   访问key（16进制或base64格式）
	 * @return 服务调用器
	 */
	public static ServiceInvoker create(String serviceName, List<String> preUrls, String accessId, String accessKey) {
		AccessLoader accessLoader;
		if (StringUtil.isEmpty(accessId)) {
			accessLoader = AccessLoader.EMPTY;
		} else {
			accessLoader = new AccessLoader.Single(accessId, accessKey);
		}
		SimpleProducer producer = new SimpleProducer(accessLoader);
		DefaultServiceInvoker invoker = (DefaultServiceInvoker) create(preUrls, serviceName, producer);
		invoker.setAccessId(accessId);
		return invoker;
	}

	/**
	 * 根据Http链接创建调用器
	 * 
	 * @param urls      链接列表
	 * @param accessId  访问id
	 * @param accessKey 访问key（16进制或base64格式）
	 * @return 服务调用器
	 */
	public static ServiceInvoker create(List<String> urls, String accessId, String accessKey) {
		String url = urls.get(0);
		int index = url.lastIndexOf('/');
		if (-1 == index || index == url.length() - 1) {
			throw new IllegalArgumentException("链接未包含服务名");
		}
		String serviceName = url.substring(index + 1);
		return create(serviceName, urls, accessId, accessKey);
	}

	/**
	 * 创建
	 * 
	 * @param urls
	 * @param serviceName
	 * @param producer
	 * @return 服务调用器
	 */
	public static ServiceInvoker create(List<String> urls, String serviceName, Producer producer) {
		if (null == urls || 0 == urls.size()) {
			throw new IllegalArgumentException("Url列表不能为空");
		}
		List<AbstractServiceInvoker> invokers = new ArrayList<>(urls.size());
		for (String url : urls) {
			if (StringUtil.isEmpty(url)) {
				continue;
			}
			invokers.add(createInvoker(url, serviceName, producer));
		}
		DefaultServiceInvoker invoker = new DefaultServiceInvoker(invokers);
		return invoker;
	}

	protected static AbstractServiceInvoker createInvoker(String url, String serviceName, Producer producer) {
		SingleServiceInvoker invoker = new SingleServiceInvoker(url, serviceName);
		// TODO 使用Netty
		// NettyServiceInvoker invoker = new NettyServiceInvoker(url, serviceName);
		invoker.setProducer(producer);
		return invoker;
	}
}
