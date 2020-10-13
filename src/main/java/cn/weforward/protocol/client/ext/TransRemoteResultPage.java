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

import java.util.List;

import cn.weforward.common.util.AbstractResultPage;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.ServiceInvoker;
import cn.weforward.protocol.client.execption.GatewayException;
import cn.weforward.protocol.client.execption.MicroserviceException;
import cn.weforward.protocol.client.util.TransDtList;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.support.PageData;
import cn.weforward.protocol.support.datatype.FriendlyObject;

/**
 * 远端分页结果集
 * 
 * @author daibo
 *
 * @param <E>
 */
public abstract class TransRemoteResultPage<E> extends AbstractResultPage<E> {

	static final int DEFAULT_PAGE = 0;
	static final int DEFAULT_PAGE_SIZE = 50;

	/** 调用器 */
	protected ServiceInvoker m_Invoker;
	/** 分页数据 */
	protected PageData m_Data;
	/** 方法名 */
	protected String m_Method;
	/** 请求参数 */
	protected DtObject m_Params;

	/**
	 * 构造
	 * 
	 * @param invoker 调整器
	 * @param method  方法名
	 * @param params  请求参数
	 */
	public TransRemoteResultPage(ServiceInvoker invoker, String method, DtObject params) {
		m_Invoker = invoker;
		m_Method = method;
		m_Params = params;
		loadData(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
	}

	void loadData() {
		loadData(getPage(), getPageSize());
	}

	void loadData(int page, int pageSize) {
		Request request = createRequest(m_Method, new PageParams(m_Params, page, pageSize));
		Response response = m_Invoker.invoke(request);
		try {
			GatewayException.checkException(response);
		} catch (GatewayException e) {
			onGatewayException(e);
		}
		DtObject serviceResult = response.getServiceResult();
		try {
			MicroserviceException.checkException(serviceResult);
		} catch (MicroserviceException e) {
			onMicroserviceException(e);
		}
		DtObject content = serviceResult.getObject("content");
		FriendlyObject obj = FriendlyObject.valueOf(content);
		List<E> items = new TransDtList<E>(obj.getList("items")) {

			@Override
			protected E trans(DtBase item) {
				return TransRemoteResultPage.this.trans(item);
			}
		};
		m_Data = PageData.valueOf(obj.getInt("count", 0), obj.getInt("page", 0), obj.getInt("page_size", 0),
				obj.getInt("page_count", 0), items);
	}

	/**
	 * 转换对象
	 * 
	 * @param item
	 * @return 转换后的对象
	 */
	protected abstract E trans(DtBase item);

	protected void onMicroserviceException(MicroserviceException e) {
		throw e;
	}

	protected void onGatewayException(GatewayException e) {
		throw e;
	}

	protected Request createRequest(String method, DtObject params) {
		return m_Invoker.createRequest(method, params);
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
