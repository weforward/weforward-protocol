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
 * 请求响应相关常量定义
 * 
 * @author zhangpengji
 *
 */
public interface ResponseConstants {

	/** wf_resp节点名称 */
	String WF_RESP = Header.WEFORWARD_PREFIX.toLowerCase() + "_resp";
	/** 网关响应码 */
	String WF_CODE = Header.WEFORWARD_PREFIX.toLowerCase() + "_code";
	/** 网关响应码说明 */
	String WF_MSG = Header.WEFORWARD_PREFIX.toLowerCase() + "_msg";
	/** 微服务的资源标识 */
	String RESOURCE_ID = "res_id";
	/** 微服务的资源到期时间。取值：自1970年1月1日起的秒数，0表示永不过期 */
	String RESOURCE_EXPIRE = "res_expire";
	/** 资源所在的微服务 */
	String RESOURCE_SERVICE = "res_service";
	/** 微服务资源的访问链接 */
	String RESOURCE_URL = "res_url";
	/** 标识 */
	String MARKS = "marks";
	/** 转发请求至此编号的实例。响应码为5006时，此值有效；若为空，网关将随机转发到其他实例 */
	String FORWARD_TO = "forward_to";
	/** 转发请求的备用实例标识（<code>FORWARD_TO</code>的特殊值） */
	String FORWARD_TO_BACKUP = "__backup";
	/** 接收到通知的微服务编号列表 */
	String NOTIFY_RECEIVES = "notify_receives";
	// /** 请求标识（序号） */
	// String ID = "id";

	/** result节点名称 */
	String RESULT = "result";
	/** 微服务方法的响应码 */
	String CODE = "code";
	/** 微服务方法的响应码说明 */
	String MSG = "msg";
	/** 微服务方法的具体返回值 */
	String CONTENT = "content";
}
