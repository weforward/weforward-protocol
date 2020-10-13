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
package cn.weforward.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负载均衡基本信息（如：权值，状态等）
 * 
 * @author liangyi
 *
 */
public class Balance {
	protected final static Logger _Logger = LoggerFactory.getLogger(Balance.class);

	/** 这次对资源的使用状态是 - 成功 */
	public final static int STATE_OK = 0;
	/** 这次对资源的使用状态是 - 失败 */
	public final static int STATE_FAIL = 1;
	/** 这次对资源的使用状态是 - 超时 */
	public final static int STATE_TIMEOUT = 2;
	/** 这次对资源的使用状态是 - 忙 */
	public final static int STATE_BUSY = 3;
	/** 这次对资源的使用状态是 - 异常 */
	public final static int STATE_EXCEPTION = 4;
	/** 资源不可用:( */
	public final static int STATE_UNAVAILABLE = 5;

	/** 权重 - 后备资源 */
	public final static int WEIGHT_BACKUP = -100;
	/** 权重 - 资源忙 */
	public final static int WEIGHT_BUSY = 0;
	/** 权重 - 资源使用异常 */
	public final static int WEIGHT_EXCEPTION = 0;

	/** 标记资源为不可用的失败次数 */
	public final static int UNAVAILABLE_FAILS = Integer.MAX_VALUE / 2;

	/** 分配请求的权重（1~100，越高被分配到的几率越大），WEIGHT_BACKUP表示作后备 */
	protected final int weight;
	/** 允许的最大并发数 */
	protected int maxConcurrent;
	/** 最大失败次数 */
	protected int maxFails;
	/** 在达到最大失败次数时，此期间不使用资源 */
	protected int failTimeout;
	// /** 恢复资源权重的启动时间 */
	// int slowStart;

	/** 当前选择权重 */
	protected int currentWeight;
	/** 有效权重 */
	protected volatile int effectiveWeight;

	// /** 占用中的线程 */
	// volatile Thread thread;
	// /** 使用次数 */
	// volatile int times;
	// /** 失败总次数 */
	// volatile int failTotal;
	/** 并发数 */
	protected volatile int concurrent;
	/** 失败时间 */
	protected volatile long failLast;
	/** 连续失败次数 */
	protected volatile int fails;

	/** 当前统计 */
	protected volatile Stat current;
	/** 前一轮的统计 */
	protected volatile Stat previous;
	/** 总数 */
	protected Stat total;

	public Balance(int weight) {
		this.weight = weight;
		this.effectiveWeight = (weight > 0) ? weight : 1;
		this.maxFails = 1;
		this.failTimeout = 1 * 60 * 1000;
		this.total = this.previous = new Stat();
		this.current = new Stat();
	}

	/**
	 * 最大失败数
	 * 
	 * @param maxFails
	 *            失败数（默认是1次）
	 */
	public void setMaxFails(int maxFails) {
		this.maxFails = maxFails;
	}

	/**
	 * 失效时间
	 * 
	 * @param failTimeout
	 *            失效时间（毫秒，默认是1分钟）
	 */
	public void setFailTimeout(int failTimeout) {
		this.failTimeout = failTimeout;
	}

	/**
	 * 控制最大并发数
	 * 
	 * @param maxConcurrent
	 *            要控制的并发数（为0则不控制）
	 */
	public void setMaxConcurrent(int maxConcurrent) {
		this.maxConcurrent = maxConcurrent;
	}

	/**
	 * 周期更新统计
	 */
	synchronized protected void tick() {
		this.previous = this.current;
		this.current = new Stat();
	}

	/**
	 * 使用资源
	 */
	synchronized protected void use() {
		++concurrent;
		// ++times;
		++total.count;
		++current.count;
	}

	/**
	 * 用完资源
	 * 
	 * @param state
	 *            使用资源的状态 STATE_xxx
	 */
	synchronized protected void free(int state) {
		--concurrent;
		// thread = null;
		if (STATE_OK == state) {
			// 是成功的
			++current.ok;
			++total.ok;
			// reset();

			// 清除失败计数
			fails = 0;
			failLast = 0;
			// 恢复所设置的权重
			effectiveWeight = weight;
			return;
		}
		// 是不成功的
		if (STATE_FAIL == state) {
			// 若状态是失败的，计数加一
			++fails;
			// ++failTotal;
			// 标记最后失败时间
			failLast = System.currentTimeMillis();
			if (maxFails > 0 && weight > 0) {
				effectiveWeight -= (weight / maxFails);
			} else {
				--effectiveWeight;
			}
			if (effectiveWeight < 0) {
				effectiveWeight = 0;
			}
			_Logger.warn("failed " + this);
		} else if (STATE_UNAVAILABLE == state) {
			// 不可用，直接置为最大失败值
			fails = UNAVAILABLE_FAILS;
			// ++failTotal;
			// 标记最后失败时间
			failLast = System.currentTimeMillis();
			if (maxFails > 0 && weight > 0) {
				effectiveWeight -= (weight / maxFails);
			} else {
				--effectiveWeight;
			}
			if (effectiveWeight < 0) {
				effectiveWeight = 0;
			}
			_Logger.warn("unavailable " + this);
		} else if (STATE_EXCEPTION == state) {
			// 当资源使用异常
			// 把选择权重减小有效权重
			if (effectiveWeight > 0) {
				currentWeight -= effectiveWeight;
			} else if (currentWeight > 0) {
				currentWeight = 0;
			}
			// 降低权重
			if (effectiveWeight > WEIGHT_EXCEPTION) {
				effectiveWeight = WEIGHT_EXCEPTION;
			}
			_Logger.warn("abnormal " + this);
		} else if (STATE_BUSY == state) {
			// 资源忙，降低权重
			if (effectiveWeight > WEIGHT_BUSY) {
				effectiveWeight = WEIGHT_BUSY;
			}
			_Logger.warn("busy " + this);
		} else {
			// 降低权重
			int lowWeight = effectiveWeight / 2;
			if (lowWeight > 0) {
				effectiveWeight = lowWeight;
			}
			_Logger.warn("timeout " + this);
		}
	}

	/**
	 * 是否失效期内
	 */
	boolean isFailDuring() {
		if (UNAVAILABLE_FAILS == fails || (maxFails > 0 && fails >= maxFails)) {
			// 大于或等于最大失败次数时
			long now = System.currentTimeMillis();
			if (now > (failLast + failTimeout)) {
				// 若已超出失效期，重置失败数
				fails = 0;
				// 返回不是失效期
				_Logger.info("regain(expire) " + this);
				return false;
			}
			// 还在失效期内
			return true;
		}
		return false;
	}

	/**
	 * 是否过载
	 */
	boolean isOverload() {
		if (maxConcurrent > 0 && concurrent >= maxConcurrent) {
			return true;
		}
		return false;
	}

	/**
	 * 是否后备资源
	 */
	boolean isBackup() {
		return (WEIGHT_BACKUP == weight);
	}

	// /**
	// * 重置失败的状态
	// */
	// protected void reset() {
	// // 清除失败计数
	// fails = 0;
	// failLast = 0;
	// // 恢复所设置的权重
	// effectiveWeight = weight;
	// }

	synchronized protected void resetAtFail() {
		if (fails > 0 && fails >= maxFails) {
			// reset();
			// 清除失败计数
			fails = 0;
			failLast = 0;
		}
	}

	// /**
	// * 使用次数
	// */
	// public long getTimes() {
	// // return times;
	// return total.count;
	// }
	//
	// public int getWeight() {
	// return weight;
	// }
	//
	// public int getMaxConcurrent() {
	// return maxConcurrent;
	// }
	//
	// public int getMaxFails() {
	// return maxFails;
	// }
	//
	// public int getFailTimeout() {
	// return failTimeout;
	// }
	//
	// public int getCurrentWeight() {
	// return currentWeight;
	// }
	//
	// public int getEffectiveWeight() {
	// return effectiveWeight;
	// }
	//
	// public long getFailTotal() {
	// // return failTotal;
	// return total.count - total.ok;
	// }
	//
	// public int getConcurrent() {
	// return concurrent;
	// }
	//
	// public int getFails() {
	// return fails;
	// }

	@Override
	public String toString() {
		return toString(new StringBuilder()).toString();
	}

	public StringBuilder toString(StringBuilder sb) {
		sb.append("{ts:").append(total.count).append(",c:").append(concurrent).append(",max-c:")
				.append(maxConcurrent).append(",w:").append(weight).append(",c-w:")
				.append(currentWeight).append(",e-w:").append(effectiveWeight).append(",f:")
				.append(fails).append(",max-f:").append(maxFails).append(",t-f:")
				.append(total.count - total.ok).append(",f-to:").append(failTimeout).append("}");
		return sb;
	}

	/**
	 * 统计项
	 * 
	 * @author liangyi
	 *
	 */
	public static class Stat {
		/** 总次数 */
		long count;
		/** 成功次数 */
		long ok;
		// /** 总耗时（毫秒），平均耗时=elapse/count */
		// long elapse;

		public long fails() {
			return count - ok;
		}
	}
}
