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

import cn.weforward.protocol.ops.traffic.TrafficTableItem;

public class TrafficTableItemWrap implements TrafficTableItem {

	TrafficTableItemVo m_Vo;
	
	public TrafficTableItemWrap(TrafficTableItemVo vo) {
		m_Vo = vo;
	}
	
	public TrafficTableItemVo getVo() {
		return m_Vo;
	}
	
	@Override
	public String getName() {
		return getVo().getName();
	}

	@Override
	public String getServiceNo() {
		return getVo().getServiceNo();
	}

	@Override
	public String getServiceVersion() {
		return getVo().getServiceVersion();
	}

	@Override
	public int getWeight() {
		return getVo().getWeight();
	}

	@Override
	public int getMaxFails() {
		return getVo().getMaxFails();
	}

	@Override
	public int getFailTimeout() {
		return getVo().getFailTimeout();
	}

	@Override
	public int getMaxConcurrent() {
		return getVo().getMaxConcurrent();
	}

	@Override
	public int getConnectTimeout() {
		return getVo().getConnectTimeout();
	}

	@Override
	public int getReadTimeout() {
		return getVo().getReadTimeout();
	}

}
