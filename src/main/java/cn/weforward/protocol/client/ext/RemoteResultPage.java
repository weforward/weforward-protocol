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
package cn.weforward.protocol.client.ext;

import cn.weforward.common.util.AbstractResultPage;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.ServiceInvoker;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.support.PageData;
import cn.weforward.protocol.support.PageDataMapper;

/**
 * 远端分页结果集
 * 
 * @author zhangpengji
 *
 * @param <E>
 */
public class RemoteResultPage<E> extends AbstractResultPage<E> {

	static final int DEFAULT_PAGE = 0;
	static final int DEFAULT_PAGE_SIZE = 50;

	/** 调用器 */
	protected ServiceInvoker m_Invoker;
	// /** 调用的方法 */
	// String m_Method;
	// /** 调用的参数 */
	// RequestInvokeParam[] m_Params;
	protected RequestInvokeObject m_InvokeObj;
	/** 分页数据 */
	protected PageData m_Data;
	/** 分页数据映射器 */
	protected PageDataMapper m_DataMapper;

	/**
	 * 构造。元素为基础类型
	 * 
	 * @param invoker
	 * @param method
	 * @param params
	 */
	public RemoteResultPage(ServiceInvoker invoker, String method, RequestInvokeParam... params) {
		this(new PageDataMapper(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE, invoker, method, params);
	}

	/**
	 * 构造。元素为基础类型
	 * 
	 * @param page
	 * @param pageSize
	 * @param invoker
	 * @param method
	 * @param params
	 */
	public RemoteResultPage(int page, int pageSize, ServiceInvoker invoker, String method,
			RequestInvokeParam... params) {
		this(new PageDataMapper(), page, pageSize, invoker, method, params);
	}

	/**
	 * 构造
	 * 
	 * @param elementClazz 符合JavaBean规范的类
	 * @param invoker
	 * @param method
	 * @param params
	 */
	public RemoteResultPage(Class<? extends E> elementClazz, ServiceInvoker invoker, String method,
			RequestInvokeParam... params) {
		this(new PageDataMapper(elementClazz), DEFAULT_PAGE, DEFAULT_PAGE_SIZE, invoker, method, params);
	}

	/**
	 * 构造
	 * 
	 * @param elementClazz 符合JavaBean规范的类
	 * @param page
	 * @param pageSize
	 * @param invoker
	 * @param method
	 * @param params
	 */
	public RemoteResultPage(Class<? extends E> elementClazz, int page, int pageSize, ServiceInvoker invoker,
			String method, RequestInvokeParam... params) {
		this(new PageDataMapper(elementClazz), page, pageSize, invoker, method, params);
	}

	// public RemoteResultPage(ObjectMapper<E> elementMapper, ServiceInvoker
	// invoker, String method,
	// RequestInvokeParam... params) {
	// this(new PageDataMapper(elementMapper), DEFAULT_PAGE, DEFAULT_PAGE_SIZE,
	// invoker, method, params);
	// }

	// public RemoteResultPage(ObjectMapper<E> elementMapper, int page, int
	// pageSize, ServiceInvoker invoker,
	// String method, RequestInvokeParam... params) {
	// this(new PageDataMapper(elementMapper), page, pageSize, invoker, method,
	// params);
	// }

	public RemoteResultPage(PageDataMapper mapper, ServiceInvoker invoker, String method,
			RequestInvokeParam... params) {
		this(mapper, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, invoker, method, params);
	}

	/**
	 * 构造
	 * 
	 * @param mapper   PageData的映射器
	 * @param page     初始页
	 * @param pageSize 每页项数
	 * @param invoker
	 * @param method
	 * @param params
	 */
	public RemoteResultPage(PageDataMapper mapper, int page, int pageSize, ServiceInvoker invoker, String method,
			RequestInvokeParam... params) {
		m_Invoker = invoker;
		m_InvokeObj = new RequestInvokeObject(method);
		m_InvokeObj.putParams(params);
		m_DataMapper = mapper;

		loadData(page, pageSize);
	}

	// protected void init() {
	// loadData(m_InvokeObj);
	//
	// m_PageSize = m_Data.getPageSize();
	// if (0 == m_PageSize) {
	// m_PageSize = 50;
	// }
	// }

	void loadData() {
		loadData(getPage(), getPageSize());
	}

	void loadData(int page, int pageSize) {
		m_InvokeObj.putParam("page", page);
		m_InvokeObj.putParam("page_size", pageSize);
		loadData(m_InvokeObj);
	}

	void loadData(RequestInvokeObject invokeObj) {
		Response ret;
		try {
			ret = m_Invoker.invoke(invokeObj.toDtObject());
		} catch (RuntimeException e) {
			throw onInvokeException(e);
		}
		if (0 != ret.getResponseCode()) {
			throw onGetWayExcepion(ret.getResponseCode(), ret.getResponseMsg());
		}
		DtObject result = ret.getServiceResult();
		int code = result.getNumber("code").valueInt();
		if (0 != code) {
			String msg = result.getString("msg").value();
			throw onServiceException(code, msg);
		}
		DtObject content = result.getObject("content");
		m_Data = m_DataMapper.fromDtObject(content);
	}

	protected RuntimeException onInvokeException(RuntimeException e) {
		// 子类重载
		return e;
	}

	protected RuntimeException onGetWayExcepion(int code, String msg) {
		// 子类重载
		return new RuntimeException("加载失败：" + code + "/" + msg);
	}

	protected RuntimeException onServiceException(int code, String msg) {
		// 子类重载
		return new RuntimeException("加载失败：" + code + "/" + msg);
	}

	@Override
	public int getCount() {
		return m_Data.getCount();
	}

	@Override
	protected E get(int idx) {
		if (null == m_Data || idx < m_Data.getPos() || idx >= m_Data.getPos() + m_Data.getItemSize()) {
			loadData();
		}
		return m_Data.getItem(idx - m_Data.getPos());
	}
}
