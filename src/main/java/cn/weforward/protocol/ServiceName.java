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
package cn.weforward.protocol;

/**
 * 服务名称
 * 
 * @author zhangpengji
 *
 */
public enum ServiceName {

	/** 保留的名称 - 网关服务注册Api */
	SERVICE_REGISTER("__" + Header.WEFORWARD_PREFIX.toLowerCase() + "_service_register"),
	/** 保留的名称 - 网关管理Api */
	KEEPER("__" + Header.WEFORWARD_PREFIX.toLowerCase() + "_keeper"),
	/** 保留的名称 - 网关分布式Api */
	DISTRIBUTED("__" + Header.WEFORWARD_PREFIX.toLowerCase() + "_distributed"),
	/** 保留的名称 - 网关调试Api */
	DEBUGER("__" + Header.WEFORWARD_PREFIX.toLowerCase() + "_debuger"),
	/** 保留的名称 - 网关流信道入口 */
	STREAM("__" + Header.WEFORWARD_PREFIX.toLowerCase() + "_stream"),
	/** 保留的名称 - 网关微服务文档入口 */
	DOC("__" + Header.WEFORWARD_PREFIX.toLowerCase() + "_doc"),
	/** 保留的名称 - 网关插件入口前缀 */
	PLUGIN("__pl_");

	public final String name;

	private ServiceName(String name) {
		this.name = name;
	}
}
