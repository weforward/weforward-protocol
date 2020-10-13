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
package cn.weforward.protocol.ext;

/**
 * 微服务实例的运行时信息
 * 
 * @author zhangpengji
 *
 */
public class ServiceRuntime {

	/** 内存上限 */
	public long memoryMax;
	/** （最大）可用的内存 */
	public long memoryUsable;
	/** 已分配的内存 */
	public long memoryAlloc;

	/** Full-GC次数 */
	public int gcFullCount;
	/** Full-GC消耗时间（秒） */
	public int gcFullTime;
	/** Young-GC次数 */
	public int gcYoungCount;
	/** Young-GC消耗时间（秒） */
	public int gcYoungTime;

	/** 线程数 */
	public int threadCount;

	/** CPU使用率 */
	public int cpuUsageRate;

	/** 启动时间 */
	public long startTime;
	/** 持续运行时间 */
	public long upTime;

	/** 信息收集时间 */
	public long timestamp;

	public ServiceRuntime() {

	}

	public long getMemoryMax() {
		return memoryMax;
	}

	public void setMemoryMax(long memoryMax) {
		this.memoryMax = memoryMax;
	}

	public long getMemoryUsable() {
		return memoryUsable;
	}

	public long getMemoryUsed() {
		return memoryMax - memoryUsable;
	}

	public void setMemoryUsable(long memoryUsable) {
		this.memoryUsable = memoryUsable;
	}

	public long getMemoryAlloc() {
		return memoryAlloc;
	}

	public void setMemoryAlloc(long memoryAlloc) {
		this.memoryAlloc = memoryAlloc;
	}

	public int getGcFullCount() {
		return gcFullCount;
	}

	public void setGcFullCount(int gcFullCount) {
		this.gcFullCount = gcFullCount;
	}

	public int getGcFullTime() {
		return gcFullTime;
	}

	public void setGcFullTime(int gcFullTime) {
		this.gcFullTime = gcFullTime;
	}

	public int getGcYoungCount() {
		return gcYoungCount;
	}

	public void setGcYoungCount(int gcYoungCount) {
		this.gcYoungCount = gcYoungCount;
	}

	public int getGcYoungTime() {
		return gcYoungTime;
	}

	public void setGcYoungTime(int gcYoungTime) {
		this.gcYoungTime = gcYoungTime;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public int getCpuUsageRate() {
		return cpuUsageRate;
	}

	public void setCpuUsageRate(int cpuUsageRate) {
		this.cpuUsageRate = cpuUsageRate;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getUpTime() {
		return upTime;
	}

	public void setUpTime(long upTime) {
		this.upTime = upTime;
	}
}
