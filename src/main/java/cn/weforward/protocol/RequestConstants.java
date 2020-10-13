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

/**
 * 调用请求相关常量定义
 * 
 * @author zhangpengji
 *
 */
public interface RequestConstants {

	/** wf_req节点名称 */
	String WF_REQ = Header.WEFORWARD_PREFIX.toLowerCase() + "_req";
	/** 指定微服务版本号 */
	String VERSION = "ver";
	/** 资源标识（由微服务定义） */
	String RESOURCE_ID = "res_id";
	/** 调用方（客户端）对此资源的权限 */
	String RESOURCE_RIGHT = "res_right";
	/** 跟踪微服务调用的标识 */
	String TRACE_TOKEN = "trace_token";
	/** 租户标识。通常跟随于用户凭证 */
	String TENANT = "tenant";
	/** 基于OAuth协议生成的用户身份。通常跟随于用户凭证 */
	String OPENID = "openid";
	/** 调用方（客户端）的access id */
	String CLIENT_ACCESS = "client_access";
	/** 调用方（客户端）的ip地址 */
	String CLIENT_ADDR = "client_addr";
	/** 标识请求由其他实例转发而来，多个实例采用“,”分隔 */
	String FORWARD_FROM = "forward_from";
	/** 请求标识（序号） */
	String ID = "id";
	/** 请求等待时间（单位：秒） */
	String WAIT_TIMEOUT = "wait_timeout";
	/** 标识 */
	String MARKS = "marks";

	/** invoke节点名称 */
	String INVOKE = "invoke";
	/** 调用的方法名称 */
	String METHOD = "method";
	/** 调用的方法参数 */
	String PARAMS = "params";
}
