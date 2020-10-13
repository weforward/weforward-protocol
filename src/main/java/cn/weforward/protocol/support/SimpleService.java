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

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import cn.weforward.common.util.ListUtil;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Service;

/**
 * <code>Service</code>的简单实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleService implements Service {

	/** @see #getName() */
	@Resource
	protected String m_Name;
	/** @see #getDomain() */
	@Resource
	protected String m_Domain;
	/** @see #getPort() */
	@Resource
	protected int m_Port;
	/** @see #getUrls() */
	@Resource
	protected List<String> m_Urls;
	/** @see #getNo() */
	@Resource
	protected String m_No;
	/** @see #getVersion() */
	@Resource
	protected String m_Version;
	/** @see #getCompatibleVersion() */
	@Resource
	protected String m_CompatibleVersion;
	/** @see #getHeartbeatPeriod() */
	@Resource
	protected int m_HeartbeatPeriod;
	/** @see #getBuildVersion() */
	@Resource
	protected String m_BuildVersion;
	/** @see #getNote() */
	@Resource
	protected String m_Note;
	/** @see #getDocumentMethod() */
	@Resource
	protected String m_DocumentMethod;
	/** @see #getDebugMethod() */
	@Resource
	protected String m_DebugMethod;
	/** @see #getRunningId() */
	@Resource
	protected String m_RunningId;
	/** @see #getRequestMaxSize() */
	@Resource
	protected int m_RequestMaxSize;
	/** @see #getMarks() */
	@Resource
	protected int m_Marks;

	public SimpleService() {

	}

	public SimpleService(Service service) {
		m_Name = service.getName();
		m_Domain = service.getDomain();
		m_Port = service.getPort();
		m_Urls = service.getUrls();
		if (ListUtil.isEmpty(m_Urls)) {
			if (!StringUtil.isEmpty(m_Domain) && m_Port > 0) {
				String url = "http://" + m_Domain + ":" + m_Port + "/" + m_Name;
				m_Urls = Collections.singletonList(url);
			}
		}
		m_No = service.getNo();
		m_Version = service.getVersion();
		m_CompatibleVersion = service.getCompatibleVersion();
		m_BuildVersion = service.getBuildVersion();
		m_HeartbeatPeriod = service.getHeartbeatPeriod();
		m_Note = service.getNote();
		m_DocumentMethod = service.getDocumentMethod();
		m_RunningId = service.getRunningId();
		m_RequestMaxSize = service.getRequestMaxSize();
		m_Marks = service.getMarks();
		m_DebugMethod = service.getDebugMethod();
	}

	@Override
	public String getName() {
		return m_Name;
	}

	@Override
	public List<String> getUrls() {
		return m_Urls;
	}

	@Override
	public String getNo() {
		return m_No;
	}

	@Override
	public String getVersion() {
		return m_Version;
	}

	@Override
	public String getCompatibleVersion() {
		return m_CompatibleVersion;
	}

	@Override
	public String getBuildVersion() {
		return m_BuildVersion;
	}

	@Override
	public int getHeartbeatPeriod() {
		return m_HeartbeatPeriod;
	}

	@Override
	public String getNote() {
		return m_Note;
	}

	@Override
	public String getDocumentMethod() {
		return m_DocumentMethod;
	}

	@Override
	public String getRunningId() {
		return m_RunningId;
	}

	// @Override
	// public String getOwner() {
	// return m_Owner;
	// }
	//
	// @Override
	// public Date getHeartbeat() {
	// return m_Heartbeat;
	// }
	//
	// @Override
	// public int getMarks() {
	// return m_Marks;
	// }
	//
	// public boolean isMark(int mark) {
	// return mark == (mark & m_Marks);
	// }
	//
	// @Override
	// public boolean isTimeout() {
	// return isMark(MARK_TIMEOUT);
	// }
	//
	// @Override
	// public boolean isUnavailable() {
	// return isMark(MARK_UNAVAILABLE);
	// }
	//
	// @Override
	// public boolean isInaccessible() {
	// return isMark(MARK_INACCESSIBLE);
	// }
	//
	// public void setTimeout(boolean bool) {
	// setMark(bool ? MARK_TIMEOUT : -MARK_TIMEOUT);
	// }
	//
	// public void setUnavailable(boolean bool) {
	// setMark(bool ? MARK_UNAVAILABLE : -MARK_UNAVAILABLE);
	// }
	//
	// public void setInaccessible(boolean bool) {
	// setMark(bool ? MARK_INACCESSIBLE : -MARK_INACCESSIBLE);
	// }

	public void setName(String name) {
		m_Name = name;
	}

	public void setUrls(List<String> urls) {
		m_Urls = urls;
	}

	public void setNo(String no) {
		m_No = no;
	}

	public void setVersion(String version) {
		m_Version = version;
	}

	public void setCompatibleVersion(String compatibleVersion) {
		m_CompatibleVersion = compatibleVersion;
	}

	public void setHeartbeatPeriod(int heartbeatPeriod) {
		m_HeartbeatPeriod = heartbeatPeriod;
	}

	public void setBuildVersion(String buildVersion) {
		m_BuildVersion = buildVersion;
	}

	public void setNote(String note) {
		m_Note = note;
	}

	public void setDocumentMethod(String documentMethod) {
		m_DocumentMethod = documentMethod;
	}

	public void setRunningId(String runningId) {
		m_RunningId = runningId;
	}

	@Override
	public String getDomain() {
		return m_Domain;
	}

	@Override
	public int getPort() {
		return m_Port;
	}

	@Override
	public int getRequestMaxSize() {
		return m_RequestMaxSize;
	}

	@Override
	public int getMarks() {
		return m_Marks;
	}

	@Override
	public String getDebugMethod() {
		return m_DebugMethod;
	}

}
