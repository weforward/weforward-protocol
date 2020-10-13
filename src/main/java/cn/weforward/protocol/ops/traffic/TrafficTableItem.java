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
package cn.weforward.protocol.ops.traffic;

/**
 * TrafficTable的项
 * 
 * @author zhangpengji
 *
 */
public interface TrafficTableItem {

	int WEIGHT_DEFAULT = 1;
	int MAX_FAILS_DEFAULT = 3;
	int FAIL_TIMEOUT_DEFAULT = 180;
	int MAX_CONCURRENT_DEFAULT = 0;
	int CONNECT_TIMEOUT_DEFAULT = 5;
	int READ_TIMEOUT_DEFAULT = 50;

	/** 项的名称 */
	String getName();

	/** 微服务编号 */
	String getServiceNo();

	/** 微服务版本 */
	String getServiceVersion();

	/** 权重。由1~100，-100表示其为后备，0表示屏蔽。默认为{@linkplain #WEIGHT_DEFAULT} */
	int getWeight();

	/**
	 * 最大连续失败次数。连续失败此值后将标记项为失败且不使用，直至fail_timeout后才重新使用。默认为{@linkplain #MAX_FAILS_DEFAULT}
	 */
	int getMaxFails();

	/** 标记为失败的项重新再使用的时间（秒）。默认为{@linkplain #FAIL_TIMEOUT_DEFAULT} */
	int getFailTimeout();

	/** 最大并发数。控制并发量防止过载。默认为{@linkplain #MAX_CONCURRENT_DEFAULT} */
	int getMaxConcurrent();

	/** 连接超时值（秒）。默认为{@linkplain #CONNECT_TIMEOUT_DEFAULT} */
	int getConnectTimeout();

	/** 网关等待微服务响应的超时值（秒）。默认为{@linkplain #READ_TIMEOUT_DEFAULT} */
	int getReadTimeout();

	TrafficTableItem DEFAULT = new TrafficTableItem() {

		@Override
		public int getWeight() {
			return WEIGHT_DEFAULT;
		}

		@Override
		public String getServiceVersion() {
			return null;
		}

		@Override
		public String getServiceNo() {
			return null;
		}

		@Override
		public int getReadTimeout() {
			return READ_TIMEOUT_DEFAULT;
		}

		@Override
		public String getName() {
			return "global_default";
		}

		@Override
		public int getMaxFails() {
			return MAX_FAILS_DEFAULT;
		}

		@Override
		public int getMaxConcurrent() {
			return MAX_CONCURRENT_DEFAULT;
		}

		@Override
		public int getFailTimeout() {
			return FAIL_TIMEOUT_DEFAULT;
		}

		@Override
		public int getConnectTimeout() {
			return CONNECT_TIMEOUT_DEFAULT;
		}
	};
}
