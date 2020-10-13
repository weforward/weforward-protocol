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

import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.ops.traffic.TrafficTableItem;

public class TrafficTableItemVo {
	public String name;
	public String serviceNo;
	public String serviceVersion;
	public int weight;
	public int maxFails;
	public int failTimeout;
	public int maxConcurrent;
	public int connectTimeout;
	public int readTimeout;

	public TrafficTableItemVo() {

	}

	public TrafficTableItemVo(TrafficTableItem item) {
		this.serviceNo = item.getServiceNo();
		this.serviceVersion = item.getServiceVersion();
		this.name = item.getName();
		this.weight = item.getWeight();
		this.maxFails = item.getMaxFails();
		this.failTimeout = item.getFailTimeout();
		this.maxConcurrent = item.getMaxConcurrent();
		this.connectTimeout = item.getConnectTimeout();
		this.readTimeout = item.getReadTimeout();
	}

	/**
	 * 使用默认值构造
	 * 
	 * @param no
	 *            微服务实例编号，可空
	 * @param version
	 *            微服务版本号，可空
	 */
	public TrafficTableItemVo(String no, String version) {
		this.serviceNo = no;
		this.serviceVersion = version;
		String name;
		if (!StringUtil.isEmpty(no) && !StringUtil.isEmpty(version)) {
			name = no + "_" + version;
		} else if (!StringUtil.isEmpty(no)) {
			name = no;
		} else if (!StringUtil.isEmpty(version)) {
			name = version;
		} else {
			name = "_ALL_";
		}
		this.name = name;
		this.weight = TrafficTableItem.WEIGHT_DEFAULT;
		this.maxFails = TrafficTableItem.MAX_FAILS_DEFAULT;
		this.failTimeout = TrafficTableItem.FAIL_TIMEOUT_DEFAULT;
		this.maxConcurrent = TrafficTableItem.MAX_CONCURRENT_DEFAULT;
		this.connectTimeout = TrafficTableItem.CONNECT_TIMEOUT_DEFAULT;
		this.readTimeout = TrafficTableItem.READ_TIMEOUT_DEFAULT;
	}

	public static TrafficTableItemVo valueOf(TrafficTableItem item) {
		if (null == item) {
			return null;
		}
		if (item instanceof TrafficTableItemWrap) {
			return ((TrafficTableItemWrap) item).getVo();
		}
		return new TrafficTableItemVo(item);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceNo() {
		return serviceNo;
	}

	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getMaxFails() {
		return maxFails;
	}

	public void setMaxFails(int maxFails) {
		this.maxFails = maxFails;
	}

	public int getFailTimeout() {
		return failTimeout;
	}

	public void setFailTimeout(int failTimeout) {
		this.failTimeout = failTimeout;
	}

	public int getMaxConcurrent() {
		return maxConcurrent;
	}

	public void setMaxConcurrent(int maxConcurrent) {
		this.maxConcurrent = maxConcurrent;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
}
