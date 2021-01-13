package cn.weforward.protocol.gateway;

import java.util.Comparator;

import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.doc.annotation.DocAttribute;
import cn.weforward.protocol.doc.annotation.DocObject;

/**
 * 微服务概要
 * 
 * @author zhangpengji
 *
 */
@DocObject(description = "微服务概要")
public class ServiceSummary {

	@DocAttribute(description = "名称")
	public String name;
	@DocAttribute(description = "状态。0~9，0表示正常，9表示不可用")
	public int status;
	@DocAttribute(description = "概要描述")
	public String summary;
	
	public static final Comparator<ServiceSummary> CMP_DEFAULT = new Comparator<ServiceSummary>() {
		
		@Override
		public int compare(ServiceSummary o1, ServiceSummary o2) {
			// 先按状态倒序
			int ret = Integer.compare(o2.status, o1.status);
			if(0 != ret) {
				return ret;
			}
			// 再按名称字典序
			return StringUtil.compareTo(o1.name, o2.name);
		}
	};

	public ServiceSummary() {

	}
	
	public ServiceSummary(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
}
