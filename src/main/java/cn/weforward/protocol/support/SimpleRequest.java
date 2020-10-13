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
package cn.weforward.protocol.support;

import cn.weforward.protocol.Access;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.datatype.DtObject;

/**
 * 调用请求的简单实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleRequest implements Request {

	/** 当前请求的Access */
	protected Access m_Access;
	/** 请求头信息 */
	protected Header m_Header;
	/** 客户端的access id */
	protected String m_ClientAccess;
	/** 客户端的ip地址 */
	protected String m_ClientIp;
	/** 资源标识 */
	protected String m_ResourceId;
	/** 资源权限 */
	protected int m_ResourceRight;
	/** 指定微服务版本 */
	protected String m_Version;
	/** @see #getTraceToken() */
	protected String m_TraceToken;
	/** @see Access#getTenant() */
	protected String m_Tenant;
	/** @see Access#getOpenid() */
	protected String m_Openid;
	/** 请求等待时间，单位：秒 */
	protected int m_WaitTimeout;
	/** @see #getMarks() */
	protected int m_Marks;

	/** 服务的请求参数 */
	protected DtObject m_Invoke;

	public SimpleRequest() {
	}

	public SimpleRequest(Header header) {
		m_Header = header;
	}

	@Override
	public Header getHeader() {
		return m_Header;
	}

	@Override
	public void setHeader(Header header) {
		m_Header = header;
	}

	@Override
	public void setWaitTimeout(int timeout) {
		m_WaitTimeout = timeout;
	}

	@Override
	public int getWaitTimeout() {
		return m_WaitTimeout;
	}

	// @Override
	// public String getClientAccess() {
	// return m_ClientAccess;
	// }

	// @Override
	// public void setClientAccess(String clientAccess) {
	// m_ClientAccess = clientAccess;
	// }

	@Override
	public DtObject getServiceInvoke() {
		return m_Invoke;
	}

	@Override
	public void setServiceInvoke(DtObject invoke) {
		m_Invoke = invoke;
	}

	@Override
	public Access getAccess() {
		return m_Access;
	}

	@Override
	public void setAccess(Access access) {
		m_Access = access;
	}

	@Override
	public String getResourceId() {
		return m_ResourceId;
	}

	@Override
	public void setResourceId(String resourceId) {
		m_ResourceId = resourceId;
	}

	@Override
	public int getResourceRight() {
		return m_ResourceRight;
	}

	@Override
	public void setResourceRight(int resourceRight) {
		m_ResourceRight = resourceRight;
	}

	@Override
	public String getVersion() {
		return m_Version;
	}

	@Override
	public void setVersion(String version) {
		m_Version = version;
	}

	@Override
	public String getTraceToken() {
		return m_TraceToken;
	}

	@Override
	public void setTraceToken(String traceId) {
		m_TraceToken = traceId;
	}

	@Override
	public String getAddr() {
		return m_ClientIp;
	}

	@Override
	public void setAddr(String ip) {
		m_ClientIp = ip;
	}

	@Override
	public int getMarks() {
		return m_Marks;
	}

	@Override
	public void setMarks(int marks) {
		m_Marks = marks;
	}

	@Override
	public boolean isMark(int mark) {
		return Helper.isMark(mark, this);
	}

}
