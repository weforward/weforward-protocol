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

import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.ResponseAware;

/**
 * 值对象代理
 * 
 * @author daibo
 *
 * @param <V>
 */
public interface VoProxy<V> {
	/**
	 * 主键
	 */
	String getKey();

	/**
	 * 取得VO（可能会阻塞及触发异步刷新处理）
	 *
	 */
	V getVo();

	/**
	 * 更新vo
	 * 
	 * @param vo
	 * @param info
	 */
	void updateVo(V vo, ServiceInfo info);

	/**
	 * 强过期(必须重新加载VO)
	 */
	void expirePersistent();

	/**
	 * 弱过期(加载失败重用旧VO)
	 */
	void weakExpirePersistent();

	/**
	 * vo加载者
	 * 
	 * @author daibo
	 *
	 * @param <V>
	 */
	interface VoLoader<V> {
		/**
		 * 加载vo
		 * 
		 * @param key
		 *            主键
		 * @param old
		 *            旧vo
		 */
		LoadResult<V> loadVo(String key, V old, ServiceInfo info);
	}

	/**
	 * 视图（属性）对象所在服务的信息
	 * 
	 * @author zhangpengji
	 *
	 */
	static class ServiceInfo {
		/** 标签 */
		String tag;
		
		public ServiceInfo() {
			
		}
		
		public static ServiceInfo tagOf(String tag) {
			ServiceInfo info = new ServiceInfo();
			info.tag = tag;
			return info;
		}
	}

	/**
	 * 加载结果封装
	 * 
	 * @author zhangpengji
	 *
	 */
	static class LoadResult<V> implements ResponseAware {
		V vo;
		ServiceInfo info;

		public LoadResult() {

		}

		public LoadResult(V vo) {
			this.vo = vo;
		}

		public LoadResult(V vo, ServiceInfo info) {
			this.vo = vo;
			this.info = info;
		}

		@Override
		public void onResponse(Response response) {
			this.info = ServiceInfo.tagOf(response.getHeader().getTag());
		}
	}

}
