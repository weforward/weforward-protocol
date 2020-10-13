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

import java.util.List;

/**
 * 微服务的流量规则表，描述微服务实例的流量规则（负载均衡与熔断保护）。
 * <p>
 * 根据微服务实例的编号与版本号，依次按规则项的顺序匹配，无匹配规则时，相当于屏蔽某实例的流量。
 * <p>
 * - 负载均衡：<br/>
 * 采用加权轮询(WRR)的方式， 初始状态（未配置任何规则）为轮询(RR)。<br/>
 * - 熔断保护：<br/>
 * 1.设置微服务示例的最大并发数。当并发数超过最大值，将不再将请求交给此实例。<br/>
 * 2.设置最大连续失败次数与达到次数后的重置时间。当实例的失败次数达到最大值，在重置时间内，将不再将请求交给此实例。<br/>
 * 特殊情况，当全部实例的失败次数都达到最大值，将马上重置全部实例。<br/>
 * - 请求转发：<br/>
 * 当网关访问一个微服务实例失败时，默认不会将请求交给其他实例，若要实现类似功能，请参考微服务请求转发。
 * 
 * @author zhangpengji
 *
 */
public interface TrafficTable {

	/**
	 * 服务名
	 * 
	 */
	String getName();

	/**
	 * 规则项列表。由前到后匹配
	 * 
	 */
	List<TrafficTableItem> getItems();

	/**
	 * 在列表末尾添加一个项
	 * 
	 * @param item
	 */
	void appendItem(TrafficTableItem item);

	/**
	 * 在指定位置插入项。若index小于0，插入队首；若index大于项数，则插入队尾
	 * 
	 * @param item
	 * @param index
	 */
	void insertItem(TrafficTableItem item, int index);

	/**
	 * 替换指定位置的规则
	 * 
	 * @param item
	 * @param index
	 * @param name  原位置规则的名称
	 */
	void replaceItem(TrafficTableItem item, int index, String name);

	/**
	 * 移动规则的位置
	 * 
	 * @param from
	 * @param to
	 */
	void moveItem(int from, int to);

	/**
	 * 删除指定位置的规则
	 * 
	 * @param index
	 * @param name  原位置规则的名称
	 */
	void removeItem(int index, String name);
}
