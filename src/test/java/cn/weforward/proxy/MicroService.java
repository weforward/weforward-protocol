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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 微服务
 * 
 * @author liangyi
 *
 */
public class MicroService {
	String m_Name;
	/** 微服务端点表 */
	List<AbstractEndpoint> m_Endpoints;

	public MicroService(String name) {
		m_Name = name;
		m_Endpoints = new ArrayList<AbstractEndpoint>();
	}

	public String getName() {
		return m_Name;
	}

	/**
	 * 检查服务是否可用，不可用直接抛出异常
	 * 
	 * @throws BusyException
	 */
	public void checkAvailable() throws BusyException {
		// TODO Auto-generated method stub
	}

	/**
	 * 是否过载
	 */
	private boolean isOverload(AbstractEndpoint ep) {
		if (ep.isOverload()) {
			return true;
		}
		// int c = ep.concurrent;
		// if (c > 0) {
		// Quotas quotas = m_Quotas;
		// if (null != quotas) {
		// int quota = quotas.getQuota(ep, c);
		// if (c >= quota) {
		// _Logger.trace("{超额:{}/{},res:{}}", c, quota, ep);
		// return true;
		// }
		// }
		// }
		return false;
	}

	AbstractEndpoint get(String referto) throws BusyException {
		if (1 == m_Endpoints.size()) {
			// 只有一项时，其它的逻辑都是多余的
			AbstractEndpoint best = m_Endpoints.get(0);
			if (null == best) {
				// 还有这种事？
				return null;
			}
			synchronized (lock()) {
				if (isOverload(best)) {
					// if (best.isOverload()) {
					// throw new OverloadException(String.valueOf(best));
					throw new BusyException(String.valueOf(best));
				}
				if (best.isFailDuring()) {
					// 先重罢失败状态，避免下次还是没能获取
					best.resetAtFail();
					// throw new FailException(String.valueOf(best));
					throw new BusyException(String.valueOf(best));
				}
				best.use();
				return best;
			}
		}

		AbstractEndpoint best = null;
		int overloadCount = 0;
		int failCount = 0;
		int ew;
		int total = 0;
		boolean haveBackup = false;
		boolean isReferto = false;

		synchronized (lock()) {
			// 以下这段算法参考自Nginx RR算法
			// ngx_http_upstream_get_peer(ngx_http_upstream_rr_peer_data_t *rrp)
			for (int i = 0; i < m_Endpoints.size(); i++) {
				AbstractEndpoint element = m_Endpoints.get(i);
				// if (element.isOverload()) {
				if (isOverload(element)) {
					++overloadCount;
					// 略过过载项
					// _Logger.warn("overload:" + element);
					if (element.isBackup()) {
						haveBackup = true;
					}
					continue;
				}
				if (element.isFailDuring()) {
					++failCount;
					// 略过失败项
					// _Logger.warn("overload:" + element);
					if (element.isBackup()) {
						haveBackup = true;
					}
					continue;
				}

				ew = (element.effectiveWeight > 0) ? element.effectiveWeight : 0;
				element.currentWeight += ew;
				total += ew;
				if (element.effectiveWeight < element.weight) {
					element.effectiveWeight++;
				}

				if (null != referto && element.matchTag(referto)) {
					// 使用符合referto的资源项
					if (!isReferto || element.currentWeight > best.currentWeight) {
						best = element;
						isReferto = true;
					}
					continue;
				}
				if (element.isBackup()) {
					haveBackup = true;
					continue;
				}

				if (best == null || (!isReferto && element.currentWeight > best.currentWeight)) {
					best = element;
				}
			}

			if (best == null && haveBackup) {
				// 只好在在后备资源中找
				for (int i = m_Endpoints.size() - 1; i >= 0; i--) {
					AbstractEndpoint element = m_Endpoints.get(i);
					// if (element.isOverload()) {
					if (isOverload(element)) {
						// 过载保护
						if (element.isBackup()) {
							++overloadCount;
						}
						continue;
					}

					// 略过失败的项
					if (element.isFailDuring()) {
						if (element.isBackup()) {
							++failCount;
						}
						continue;
					}
					ew = (element.effectiveWeight > 0) ? element.effectiveWeight : 0;
					element.currentWeight += ew;
					// if (element.isBackup()) {
					total += ew;
					// }
					if (element.effectiveWeight < element.weight) {
						element.effectiveWeight++;
					} else if (0 == element.effectiveWeight) {
						element.effectiveWeight = 1;
					}

					if (best == null || element.currentWeight > best.currentWeight) {
						best = element;
					}
				}
			}

			if (best == null) {
				// 没有best，重置所有失败项，避免下次还是没能获取
				if (m_Endpoints.size() == failCount) {
					for (int i = m_Endpoints.size() - 1; i >= 0; i--) {
						AbstractEndpoint element = m_Endpoints.get(i);
						element.resetAtFail();
					}
					String err;
					// Quotas quotas = m_Quotas;
					// if (null == quotas) {
					err = "全失败{fail:" + failCount + ",over:" + overloadCount + "}";
					// } else {
					// err = "全失败{fail:" + failCount + ",over:" + overloadCount
					// + ",quotas:"
					// + quotas + "}";
					// }
					// throw new FailException(err);
					throw new BusyException(err);
				}
				if (m_Endpoints.size() == overloadCount) {
					String err;
					// Quotas quotas = m_Quotas;
					// if (null == quotas) {
					err = "全过载{fail:" + failCount + ",over:" + overloadCount + "}";
					// } else {
					// err = "全过载{fail:" + failCount + ",over:" + overloadCount
					// + ",quotas:"
					// + quotas + "}";
					// }
					// throw new OverloadException(err);
					throw new BusyException(err);
				}
				String err;
				// Quotas quotas = m_Quotas;
				AbstractEndpoint element = (null != m_Endpoints && m_Endpoints.size() > 0)
						? m_Endpoints.get(0)
						: null;
				// if (null == quotas) {
				err = "全忙{fail:" + failCount + ",over:" + overloadCount + "res:" + element + "}";
				// } else {
				// err = "全忙{fail:" + failCount + ",over:" + overloadCount +
				// ",quotas:" + quotas
				// + "res:" + element + "}";
				// }
				throw new BusyException(err);
			}

			best.currentWeight -= total;
			best.use();
			return best;
		}
	}

	private Object lock() {
		return m_Endpoints;
	}

	public EndpointPipe openPipe(Tunnel tunnel, String hyTag) throws IOException {
		// 找到合适的端点
		Endpoint ep = get(hyTag);
		// 然后建立管道
		return ep.connect(tunnel);
	}
}
