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

import java.util.List;

import cn.weforward.common.util.FreezedList;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.datatype.DtObject;

/**
 * 请求响应的简单实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleResponse implements Response {

	/** 浩宁云的响应头 */
	protected Header m_Header;
	/** 浩宁云响应码 */
	protected int m_RespCode;
	/** 浩宁云响应码的描述 */
	protected String m_RespMsg;
	/** 微服务的资源标识 */
	protected String m_ResourceId;
	/** 微服务的资源过期时间（自1970年1月1日起的秒数） */
	protected long m_ResourceExpire;
	/** 浩宁云根据微服务的资源标识生成的URL */
	protected String m_ResourceUrl;
	/** @see #getResourceService() */
	protected String m_ResourceService;
	/** @see #getForwardTo() */
	protected String m_ForwardTo;
	/** @see #getNotifyReceives() */
	protected List<String> m_EventReceives;
	/** 标识 */
	protected int m_Marks;

	/** 微服务的返回值 */
	protected DtObject m_ServiceResult;

	/**
	 * 构造空的响应
	 */
	public SimpleResponse() {
		this(null);
	}

	/**
	 * 根据响应头构造
	 * 
	 * @param header
	 */
	public SimpleResponse(Header header) {
		m_Header = header;
	}

	// /**
	// * 根据服务的返回值构造
	// *
	// * @param serviceResult
	// */
	// public HyResponse(Mapped serviceResult) {
	// m_ServiceResult = serviceResult;
	// }

	@Override
	public Header getHeader() {
		return m_Header;
	}

	@Override
	public void setHeader(Header header) {
		m_Header = header;
	}

	@Override
	public int getResponseCode() {
		return m_RespCode;
	}

	@Override
	public void setResponseCode(int responseCode) {
		m_RespCode = responseCode;
	}

	@Override
	public String getResponseMsg() {
		return m_RespMsg;
	}

	@Override
	public void setResponseMsg(String responseMsg) {
		m_RespMsg = responseMsg;
	}

	@Override
	public DtObject getServiceResult() {
		return m_ServiceResult;
	}

	@Override
	public void setServiceResult(DtObject serviceResult) {
		m_ServiceResult = serviceResult;
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
	public long getResourceExpire() {
		return m_ResourceExpire;
	}

	@Override
	public void setResourceExpire(long resourceExpire) {
		m_ResourceExpire = resourceExpire;
	}

	@Override
	public String getResourceUrl() {
		return m_ResourceUrl;
	}

	@Override
	public void setResourceUrl(String resourceUrl) {
		m_ResourceUrl = resourceUrl;
	}

	@Override
	public String getForwardTo() {
		return m_ForwardTo;
	}

	@Override
	public void setForwardTo(String to) {
		m_ForwardTo = to;
	}

	@Override
	public List<String> getNotifyReceives() {
		return FreezedList.freezed(m_EventReceives);
	}

	@Override
	public void setNotifyReceives(List<String> serviceNos) {
		m_EventReceives = FreezedList.freezed(serviceNos);
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
		// return mark == (mark & m_Marks);
		return Helper.isMark(mark, this);
	}

	@Override
	public String toString() {
		return "{wf_code:" + m_RespCode + ",wf_msg:" + m_RespMsg + ",res_id:" + m_ResourceId + ",res_expire:"
				+ m_ResourceExpire + ",res_url:" + m_ResourceUrl + ",marks=" + m_Marks + ",head:" + m_Header
				+ ",result:" + m_ServiceResult + "}";
	}

	@Override
	public String getResourceService() {
		return m_ResourceService;
	}

	@Override
	public void setResourceService(String resourceService) {
		m_ResourceService = resourceService;
	}

}
