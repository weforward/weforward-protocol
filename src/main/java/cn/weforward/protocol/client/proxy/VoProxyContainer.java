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

import cn.weforward.common.GcCleanable;
import cn.weforward.common.sys.GcCleaner;
import cn.weforward.common.util.LruCache;
import cn.weforward.common.util.LruCache.CacheNode;

/**
 * 值代理对象容器
 * 
 * @author daibo
 *
 * @param <E>
 */
public abstract class VoProxyContainer<E, V> implements LruCache.Loader<String, E>, VoProxy.VoLoader<V>, GcCleanable {
	/** 缓存 */
	protected LruCache<String, E> m_Cache;
	/** VO值有效时间（毫秒），上次加载后超过此时会重新加载 */
	protected int m_Expiry;

	public VoProxyContainer(String name) {
		m_Cache = new LruCache<String, E>(name);
		GcCleaner.register(this);
		setExpiry(5000);
	}

	/**
	 * 空值超时值，单位秒
	 * 
	 * @param seconds
	 */
	public void setNullTimeout(int seconds) {
		m_Cache.setNullTimeout(seconds);
	}

	/**
	 * VO值有效时间（毫秒），上次加载后超过此时会重新加载
	 * 
	 * @param mills 毫秒的缓存过期值，为0则永不过期
	 */
	public void setExpiry(int mills) {
		m_Expiry = mills;

		if (mills < 0) {
			mills = -mills;
		}
		if (mills > 0) {
			// 在缓存里过期的时间为VO过期的5倍且不少于60秒
			int ss = mills / (1000 / 5);
			if (ss < 60) {
				ss = 60;
			}
			m_Cache.setTimeout(ss);
		}
	}

	/**
	 * 最大容量
	 * 
	 * @param maxCapacity
	 */
	public void setMaxCapacity(int maxCapacity) {
		m_Cache.setMaxCapacity(maxCapacity);
	}

	/**
	 * 获取对象
	 * 
	 * @param key
	 * @return 对象
	 */
	public E get(String key) {
		return m_Cache.getHintLoad(key, this);
	}

	@Override
	public V loadVo(String key, V old) {
		return load(key, old);
	}

	/**
	 * 强过期(必须重新加载VO)
	 * 
	 * @param id 对象需要实现VoProxyHolder接口
	 * @return 是否过期成功
	 */
	public boolean expirePersistent(String id) {
		E e = m_Cache.get(id);
		if (e instanceof VoProxyHolder) {
			VoProxy<?> proxy = ((VoProxyHolder<?>) e).getProxy();
			if (null != proxy) {
				proxy.expirePersistent();
				return true;
			}
		}
		return false;
	}

	/**
	 * 弱过期(加载失败重用旧VO)
	 * 
	 * @param id 对象需要实现VoProxyHolder接口
	 * @return 是否过期成功
	 */
	public boolean weakExpirePersistent(String id) {
		E e = m_Cache.get(id);
		if (e instanceof VoProxyHolder) {
			VoProxy<?> proxy = ((VoProxyHolder<?>) e).getProxy();
			if (null != proxy) {
				proxy.weakExpirePersistent();
				return true;
			}
		}
		return false;
	}

	@Override
	public E load(String key, CacheNode<String, E> node) {
		VoProxy<V> proxyVo = VoProxyFactory.create(key, m_Expiry, this);
		V vo = proxyVo.getVo();
		if (null == vo) {
			return null;
		}
		return create(key, proxyVo);
	}

	@Override
	public void onGcCleanup(int policy) {
		m_Cache.onGcCleanup(policy);
	}

	@Override
	public String toString() {
		return m_Cache.toString();
	}

	/**
	 * 创建对象
	 * 
	 * @param key
	 * @param vo
	 * @return 对象
	 */
	protected abstract E create(String key, VoProxy<V> vo);

	/**
	 * 加载vo
	 * 
	 * @param key
	 * @param old
	 * @return 值对象
	 */
	protected abstract V load(String key, V old);
}
