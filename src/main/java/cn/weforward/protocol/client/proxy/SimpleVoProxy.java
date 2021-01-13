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
package cn.weforward.protocol.client.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.common.sys.ClockTick;
import cn.weforward.common.sys.StackTracer;

/**
 * 值对象代理简单实现
 * 
 * @author daibo
 * 
 * @param <V> 视图（属性）对象
 */
public class SimpleVoProxy<V> implements VoProxy<V> {
	/** 日志记录器 */
	final static Logger _Logger = LoggerFactory.getLogger(SimpleVoProxy.class);
	/** 用时间戳标记VO缓存已过期 */
	protected final static int TIMESTAMP_EXPIRED = Integer.MIN_VALUE + 1;
	/** 用时间戳标记VO缓存已弱过期 */
	protected final static int TIMESTAMP_WEAK_EXPIRED = Integer.MIN_VALUE + 2;
	/** 时钟（0.1秒的周期） */
	private final static ClockTick _Tick = ClockTick.getInstance(0.1);

	/** 对象Key */
	protected String m_Key;
	/** 视图（属性）对象 */
	protected V m_Vo;
	/** 视图（属性）对象所在服务的信息 */
	protected ServiceInfo m_ServiceInfo;
	/** 有效时间 */
	private int m_Expiry;
	/** 最后刷新的时间戳（毫秒） */
	private volatile long m_Timestamp;
	/** vo加载者 */
	protected VoLoader<V> m_Loader;

	/**
	 * 构造对象
	 * 
	 * @param key
	 * @param loader
	 * @param expiry
	 */
	public SimpleVoProxy(String key, int expiry, VoLoader<V> loader) {
		m_Key = key;
		m_Expiry = expiry;
		m_Loader = loader;
	}

	@Override
	public String getKey() {
		return m_Key;
	}

	/**
	 * 缓存有效期（单位：毫秒）
	 */
	public int getExpiry() {
		return m_Expiry;
	}

	/**
	 * 只是简单直接返回VO
	 */
	public V getVoFast() {
		return m_Vo;
	}

	/**
	 * 时间戳
	 * 
	 */
	public long getTimestamp() {
		return m_Timestamp;
	}

	/**
	 * 检查VO缓存是否过期，且根据di的配置值触发软刷新（异步刷新）
	 * 
	 * @return 若返回true表马上过期迫使getVo阻塞调用reload加载VO，否则返回false（若软过期则put入异步队列刷新VO）
	 */
	protected boolean checkExpiry() {
		int expiry = getExpiry();
		if (expiry == 0) {
			// 0为永不过期
			return false;
		}
		long ts = getTimestamp();
		long tk = _Tick.getMills();
		// 计算过期值
		if (tk <= (ts + expiry)) {
			// 未过期，直接返回就好了
			return false;
		}
		// VO已过期
		return true;
	}

	@Override
	public V getVo() {
		V v = null;
		if (TIMESTAMP_EXPIRED != m_Timestamp) {
			// VO没有标记强过期
			v = m_Vo;
			if (TIMESTAMP_WEAK_EXPIRED != m_Timestamp && null != v) {
				// 若有VO且没有标记弱过期，由checkExpiry决定是否reload
				if (!checkExpiry()) {
					return v;
				}
			}
		}
		// 若显式标记过期或VO为null，重新加载VO
		V old = getVoFast();
		ServiceInfo info = m_ServiceInfo;
		try {
			LoadResult<V> res = remoteLoad(getKey(), old, info);
			if (null == res) {
				updateVo(null, null);
			} else {
				updateVo(res.vo, res.info);
			}
		} catch (Throwable e) {
			if (null == v) {
				_Logger.warn("无法获取[" + m_Key + "]VO");
				// 只有VO为null的情况下直接抛出加载异常（也就是会略过VO过期时重加载的异常）
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					throw new RuntimeException(e);
				}
			}
			// VO还有值的情况下更新时间戳避免雪崩加载重试
			m_Timestamp = _Tick.getMills();
			StringBuilder sb = new StringBuilder(512);
			sb.append("[").append(m_Key).append("]刷新失败继续使用旧VO\t");
			StackTracer.printStackTrace(e, sb);
			_Logger.warn(sb.toString());
		}
		v = m_Vo;
		return v;
	}

	/**
	 * 远程加载对象
	 * 
	 * @param key
	 * @param old 旧对象
	 * @return 新对象
	 */
	protected LoadResult<V> remoteLoad(String key, V old, ServiceInfo info) {
		return m_Loader.loadVo(key, old, info);
	}

	@Override
	public void updateVo(V vo, ServiceInfo info) {
		boolean isChanged = false;
		synchronized (this) {
			if (null != vo) {
				isChanged = vo != m_Vo;
				if (isChanged) {
					m_Vo = vo;
				}
			}
			m_ServiceInfo = info;
			m_Timestamp = _Tick.getMills();
			notifyAll();
		}
	}

	@Override
	synchronized public void expirePersistent() {
		m_Timestamp = TIMESTAMP_EXPIRED;
	}

	@Override
	synchronized public void weakExpirePersistent() {
		m_Timestamp = TIMESTAMP_WEAK_EXPIRED;
	}

	@Override
	public String toString() {
		return "{key:" + m_Key + ",v:" + getVoFast() + ",ts:" + getTimestamp() + "}";
	}

}
