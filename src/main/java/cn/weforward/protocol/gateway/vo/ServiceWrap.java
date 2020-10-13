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

import cn.weforward.protocol.Service;

public class ServiceWrap implements Service {

	ServiceVo m_Vo;

	public ServiceWrap(ServiceVo vo) {
		m_Vo = vo;
	}
	
	protected ServiceVo getVo() {
		return m_Vo;
	}

	@Override
	public String getName() {
		return m_Vo.name;
	}

	@Override
	public String getDomain() {
		return m_Vo.domain;
	}

	@Override
	public int getPort() {
		return m_Vo.port;
	}

	@Override
	public List<String> getUrls() {
		return m_Vo.urls;
	}

	@Override
	public String getNo() {
		return m_Vo.no;
	}

	@Override
	public String getVersion() {
		return m_Vo.version;
	}

	@Override
	public String getCompatibleVersion() {
		return m_Vo.compatibleVersion;
	}

	@Override
	public String getBuildVersion() {
		return m_Vo.buildVersion;
	}

	@Override
	public int getHeartbeatPeriod() {
		return m_Vo.heartbeatPeriod;
	}

	@Override
	public String getNote() {
		return m_Vo.note;
	}

	@Override
	public String getDocumentMethod() {
		return m_Vo.documentMethod;
	}

	@Override
	public String getRunningId() {
		return m_Vo.runningId;
	}

	@Override
	public int getRequestMaxSize() {
		return m_Vo.requestMaxSize;
	}

	@Override
	public int getMarks() {
		return m_Vo.marks;
	}

	@Override
	public String getDebugMethod() {
		return m_Vo.debugMethod;
	}
}
