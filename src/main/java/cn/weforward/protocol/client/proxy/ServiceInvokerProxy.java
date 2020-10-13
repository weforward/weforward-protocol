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
package cn.weforward.protocol.client.proxy;

/**
 * 服务调用代理
 * 
 * @author daibo
 *
 */
public interface ServiceInvokerProxy {
	/**
	 * 创建代理类
	 * 
	 * @param <E>
	 * @param myInterface 代理类接口类名
	 * @return 代理类
	 */
	<E> E newProxy(String myInterface);

	/**
	 * 创建代理类
	 * 
	 * @param <E>
	 * @param myInterface 代理类接口
	 * @return 代理类
	 */
	<E> E newProxy(Class<E> myInterface);

	/**
	 * 创建代理类
	 * 
	 * @param <E>
	 * @param methodGroup 方法组名
	 * @param myInterface 代理类接口类名
	 * @return 代理类
	 */
	<E> E newProxy(String methodGroup, String myInterface);

	/**
	 * 创建代理类
	 * 
	 * @param <E>
	 * @param methodGroup 方法组名
	 * @param myInterface 代理类接口
	 * @return 代理类
	 */
	<E> E newProxy(String methodGroup, Class<E> myInterface);

}
