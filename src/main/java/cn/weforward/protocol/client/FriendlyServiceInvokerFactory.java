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

/**
 * 友好调用器类工厂
 * 
 * @author daibo
 *
 */
public class FriendlyServiceInvokerFactory {

	/**
	 * 根据Http前缀链接与服务名创建调用器
	 * 
	 * @param serviceName 微服务名
	 * @param preUrl      前缀链接，支持按“;”拼接多个
	 * @param accessId    访问id
	 * @param accessKey   访问key（16进制或base64格式）
	 * @return 调用器对象
	 */
	public static FriendlyServiceInvoker create(String serviceName, String preUrl, String accessId, String accessKey) {
		return new SimpleFriendlyServiceInvoker(ServiceInvokerFactory.create(serviceName, preUrl, accessId, accessKey));
	}
}
