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

import java.util.Date;

import cn.weforward.protocol.ops.ServiceExt;

public class ServiceExtWrap extends ServiceWrap implements ServiceExt {

	public ServiceExtWrap(ServiceExtVo vo) {
		super(vo);
	}

	protected ServiceExtVo getVo() {
		return (ServiceExtVo) m_Vo;
	}

	@Override
	public String getOwner() {
		return getVo().owner;
	}

	@Override
	public int getMarks() {
		return getVo().marks;
	}

	@Override
	public Date getHeartbeat() {
		return getVo().heartbeat;
	}

	public boolean isMark(int mark) {
		return mark == (mark & getVo().marks);
	}

	@Override
	public boolean isTimeout() {
		return isState(STATE_TIMEOUT);
	}

	@Override
	public boolean isUnavailable() {
		return isState(STATE_UNAVAILABLE);
	}

	@Override
	public boolean isInaccessible() {
		return isState(STATE_INACCESSIBLE);
	}

	@Override
	public int getState() {
		return getVo().state;
	}

	public boolean isState(int state) {
		return state == (state & getVo().state);
	}

	@Override
	public boolean isOverload() {
		return isState(STATE_OVERLOAD);
	}

}
