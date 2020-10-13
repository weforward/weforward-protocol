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

import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtObject;

/**
 * 事件调用器
 * 
 * @author daibo
 *
 */
public interface EventInvoker {
	/** 事件协议(根据权值随机调用一个实例。使用相当于普通的‘rpc’调用) */
	String ENENT_PROTOCOL = "event://";
	/** 通知协议 (通知目标微服务的一个实例。与‘rpc’不同的是，当通知请求抵达一个微服务实例时，将立即返回，不等待微服务端处理。) */
	String NOTIFY_PROTOCOL = "notify://";
	/** 广播协议(通知目标微服务的所有实例。与‘rpc’不同的是，当通知请求抵达所有微服务实例时，将立即返回，不等待微服务端处理。) */
	String BROADCAST_PROTOCOL = "broadcast://";
	/** 方法组参数名 */
	String GROUP_PARAMETER_NAME = "_group";

	/**
	 * 事件
	 * 
	 * @param name 事件名 如onPaySuccess
	 * @param uri  访问uri 如 event://微服务名?_group=方法组&参数1=值1&参数2=值2
	 * @return 事件协议返回调用返回的内容，通知和广播返回接收到通知的实例列表
	 */
	DtBase invoke(String name, String uri);

	/**
	 * 事件
	 * 
	 * @param name   事件名 如onPaySuccess
	 * @param uri    访问uri 如 event://微服务名?_group=方法组&参数1=值1&参数2=值2
	 * @param params 参数
	 * @return 事件协议返回调用返回的内容，通知和广播返回接收到通知的实例列表
	 */
	DtBase invoke(String name, String uri, DtObject params);
}
