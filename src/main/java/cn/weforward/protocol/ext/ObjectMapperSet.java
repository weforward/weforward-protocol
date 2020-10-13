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
package cn.weforward.protocol.ext;

import java.util.Enumeration;

/**
 * 对象映射器集合
 * 
 * @author zhangpengji
 *
 */
public interface ObjectMapperSet {

	/**
	 * 注册一个映射器
	 * 
	 * @param mapper
	 * @param name
	 */
	public void register(ObjectMapper<?> mapper, String name);

	/**
	 * 注册Class的映射器
	 * 
	 * @param <E>
	 * @param mapper
	 * @param clazz
	 */
	public <E> void register(ObjectMapper<? extends E> mapper, Class<E> clazz);

	/**
	 * 由映射元数据取得相应的映射器，如果无法匹配返回null
	 * 
	 * @param name 映射对象类型名
	 * @return 适合的映射器
	 */
	public ObjectMapper<?> getObjectMapper(String name);

	/**
	 * 由Class取得相应的映射器
	 * 
	 * @param <E>   映射对象类型
	 * @param clazz 映射对象类
	 * @return 适合的映射器
	 */
	public <E> ObjectMapper<E> getObjectMapper(Class<E> clazz);

	/**
	 * 获取全部映射器
	 */
	public Enumeration<ObjectMapper<?>> getMappers();

	/**
	 * 注册另一个集合中的全部映射器
	 * 
	 * @param set
	 */
	public void registerAll(ObjectMapperSet set);

	/**
	 * 按名称注销一个映射器
	 * 
	 * @param name
	 * @return 已注销的映射器，若无匹配此名称的映射器则返回null
	 */
	public ObjectMapper<?> unregister(String name);
}
