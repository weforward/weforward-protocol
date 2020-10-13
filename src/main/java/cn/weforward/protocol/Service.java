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

import java.util.Comparator;
import java.util.List;

import cn.weforward.common.util.StringUtil;

/**
 * 微服务（的一个实例）
 * 
 * @author zhangpengji
 *
 */
public interface Service {

	/** 按名称排序 */
	Comparator<Service> CMP_BY_NAME = new Comparator<Service>() {

		@Override
		public int compare(Service o1, Service o2) {
			return StringUtil.compareTo(o1.getName(), o2.getName());
		}
	};

	/** 标识 - 启用转发 */
	int MARK_FORWARD_ENABLE = 1;

	/** 服务名（规则：小写字母+下划线+数字） */
	String getName();

	/** 域名/ip */
	String getDomain();

	/** 端口 */
	int getPort();

	/** 服务访问链接 */
	List<String> getUrls();

	/** 编号，微服务实例间不应重复。一般使用服务器标识做为编号 */
	String getNo();

	/** 版本号 */
	String getVersion();

	/** 向下兼容的最低版本号 */
	String getCompatibleVersion();

	/** 构建版本号 */
	String getBuildVersion();

	/**
	 * 心跳周期时间，单位：秒。<br/>
	 * 默认（值为0）为60秒，-1表示无心跳
	 */
	int getHeartbeatPeriod();

	/** 备注 */
	String getNote();

	/**
	 * 获取文档的方法名。<br/>
	 * 若不为空时，网关会调用此方法获取文档。方法无入参，返回值为ServiceDocument对象。
	 * 
	 * @return 文档的方法名
	 */
	String getDocumentMethod();

	/**
	 * 用于调试（执行脚本）的方法名。<br/>
	 * 不为空时，网关调用此方法，传入源代码（参数名：src），返回值同一般方法
	 * 
	 * @return 调试（执行脚本）的方法名
	 */
	String getDebugMethod();

	/**
	 * 运行实例标识
	 * 
	 * @return 运行实例标识
	 */
	String getRunningId();

	/**
	 * 请求内容的最大字节数。
	 * <p>
	 * 带有标识<code>MARK_FORWARD_ENABLE</code>时，必须指定此字节数
	 * 
	 * @return 最大字节数
	 */
	int getRequestMaxSize();

	/**
	 * 标识
	 * 
	 * @return 标识
	 */
	int getMarks();
}
