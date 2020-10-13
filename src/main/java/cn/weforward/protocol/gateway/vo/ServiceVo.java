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
package cn.weforward.protocol.gateway.vo;

import java.util.List;

import javax.annotation.Resource;

import cn.weforward.protocol.Service;
import cn.weforward.protocol.ext.ServiceRuntime;

/**
 * 服务的基本信息
 * 
 * @author zhangpengji
 *
 */
public class ServiceVo implements Service {

	/** @see #getName() */
	@Resource
	public String name;
	/** @see #getDomain() */
	@Resource
	public String domain;
	/** @see #getPort() */
	@Resource
	public int port;
	/** @see #getUrls() */
	public List<String> urls;
	/** @see #getNo() */
	@Resource
	public String no;
	/** @see #getVersion() */
	@Resource
	public String version;
	/** @see #getCompatibleVersion() */
	@Resource
	public String compatibleVersion;
	/** @see #getHeartbeatPeriod() */
	@Resource
	public int heartbeatPeriod;
	/** @see #getBuildVersion() */
	@Resource
	public String buildVersion;
	/** @see #getNote() */
	@Resource
	public String note;
	/** @see #getDocumentMethod() */
	@Resource
	public String documentMethod;
	/** @see #getDebugMethod() */
	@Resource
	public String debugMethod;
	/** @see #getRunningId() */
	@Resource
	public String runningId;
	/** @see #getRequestMaxSize() */
	@Resource
	public int requestMaxSize;
	/** @see #getMarks() */
	@Resource
	public int marks;
	@Resource
	public ServiceRuntime serviceRuntime;

	public ServiceVo() {

	}

	public ServiceVo(Service service) {
		this.name = service.getName();
		this.domain = service.getDomain();
		this.port = service.getPort();
		this.urls = service.getUrls();
		this.no = service.getNo();
		this.version = service.getVersion();
		this.compatibleVersion = service.getCompatibleVersion();
		this.buildVersion = service.getBuildVersion();
		this.heartbeatPeriod = service.getHeartbeatPeriod();
		this.note = service.getNote();
		this.documentMethod = service.getDocumentMethod();
		this.runningId = service.getRunningId();
		this.requestMaxSize = service.getRequestMaxSize();
		this.marks = service.getMarks();
		this.debugMethod = service.getDebugMethod();
	}

	public static ServiceVo valueOf(Service service) {
		if (null == service) {
			return null;
		}
		if (service instanceof ServiceWrap) {
			return ((ServiceWrap) service).getVo();
		}
		return new ServiceVo(service);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDomain() {
		return this.domain;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public List<String> getUrls() {
		return this.urls;
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public String getNote() {
		return this.note;
	}

	@Override
	public String getNo() {
		return this.no;
	}

	@Override
	public int getHeartbeatPeriod() {
		return this.heartbeatPeriod;
	}

	@Override
	public String getBuildVersion() {
		return this.buildVersion;
	}

	@Override
	public String getDocumentMethod() {
		return this.documentMethod;
	}

	@Override
	public String getRunningId() {
		return this.runningId;
	}

	@Override
	public String getCompatibleVersion() {
		return this.compatibleVersion;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setCompatibleVersion(String compatibleVersion) {
		this.compatibleVersion = compatibleVersion;
	}

	public void setHeartbeatPeriod(int heartbeatPeriod) {
		this.heartbeatPeriod = heartbeatPeriod;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setDocumentMethod(String documentMethod) {
		this.documentMethod = documentMethod;
	}

	public void setRunningId(String runningId) {
		this.runningId = runningId;
	}

	public ServiceRuntime getServiceRuntime() {
		return serviceRuntime;
	}

	public void setServiceRuntime(ServiceRuntime serviceRuntime) {
		this.serviceRuntime = serviceRuntime;
	}

	@Override
	public int getRequestMaxSize() {
		return requestMaxSize;
	}
	
	public void setRequestMaxSize(int size) {
		this.requestMaxSize = size;
	}

	@Override
	public int getMarks() {
		return marks;
	}
	
	public void setMarks(int marks) {
		this.marks = marks;
	}

	@Override
	public String getDebugMethod() {
		return this.debugMethod;
	}
	
	public void setDebugMethod(String method) {
		this.debugMethod = method;
	}
}
