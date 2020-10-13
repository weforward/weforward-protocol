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
package cn.weforward.protocol.aio.netty;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.LoggerFactory;

import cn.weforward.common.GcCleanable;
import cn.weforward.common.sys.GcCleaner;
import cn.weforward.common.util.Bytes;
import io.netty.util.internal.PlatformDependent;

/**
 * 监控netty中direct memory的使用量
 * 
 * @author liangyi
 *
 */
public class NettyMemMonitor implements GcCleanable {
	/** PlatformDependent.DIRECT_MEMORY_COUNTER */
	AtomicLong m_DirectMemoryCounter;

	private NettyMemMonitor() {
		// 反射获取PlatformDependent.DIRECT_MEMORY_COUNTER
		try {
			Field field = PlatformDependent.class.getDeclaredField("DIRECT_MEMORY_COUNTER");
			field.setAccessible(true);
			m_DirectMemoryCounter = (AtomicLong) field.get(null);
			GcCleaner.register(this);
		} catch (Exception e) {
			// 获取失败
			m_DirectMemoryCounter = null;
			LoggerFactory.getLogger(NettyMemMonitor.class)
					.warn("reflect 'PlatformDependent.DIRECT_MEMORY_COUNTER' fail!");
		}
	}

	public void log() {
		StringBuilder builder = new StringBuilder(64);
		builder.append("netty direct memory: ");
		Bytes.formatHumanReadable(builder, m_DirectMemoryCounter.get());
		LoggerFactory.getLogger(NettyMemMonitor.class).info(builder.toString());
	}

	@Override
	public void onGcCleanup(int policy) {
		log();
	}

	public static NettyMemMonitor getInstance() {
		return Singleton._singleton;
	}

	private static class Singleton {
		static NettyMemMonitor _singleton = new NettyMemMonitor();
	}
}
