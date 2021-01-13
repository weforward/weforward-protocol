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
package cn.weforward.protocol.gateway.http;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import cn.weforward.common.ResultPage;
import cn.weforward.common.crypto.Hex;
import cn.weforward.common.util.StringUtil;
import cn.weforward.common.util.TransResultPage;
import cn.weforward.protocol.Access;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.ServiceName;
import cn.weforward.protocol.client.ServiceInvoker;
import cn.weforward.protocol.client.ServiceInvokerFactory;
import cn.weforward.protocol.client.ext.RemoteResultPage;
import cn.weforward.protocol.client.ext.RequestInvokeObject;
import cn.weforward.protocol.client.ext.RequestInvokeParam;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.gateway.SearchServiceParams;
import cn.weforward.protocol.gateway.ServiceSummary;
import cn.weforward.protocol.gateway.exception.KeeperException;
import cn.weforward.protocol.gateway.vo.AccessExtVo;
import cn.weforward.protocol.gateway.vo.AccessExtWrap;
import cn.weforward.protocol.gateway.vo.AclTableItemVo;
import cn.weforward.protocol.gateway.vo.AclTableResourceVo;
import cn.weforward.protocol.gateway.vo.AclTableVo;
import cn.weforward.protocol.gateway.vo.AclTableWrap;
import cn.weforward.protocol.gateway.vo.RightTableItemVo;
import cn.weforward.protocol.gateway.vo.RightTableVo;
import cn.weforward.protocol.gateway.vo.RightTableWrap;
import cn.weforward.protocol.gateway.vo.ServiceExtVo;
import cn.weforward.protocol.gateway.vo.ServiceExtWrap;
import cn.weforward.protocol.gateway.vo.TrafficTableItemVo;
import cn.weforward.protocol.gateway.vo.TrafficTableVo;
import cn.weforward.protocol.gateway.vo.TrafficTableWrap;
import cn.weforward.protocol.ops.AccessExt;
import cn.weforward.protocol.ops.ServiceExt;
import cn.weforward.protocol.ops.secure.AclTable;
import cn.weforward.protocol.ops.secure.AclTableItem;
import cn.weforward.protocol.ops.secure.RightTable;
import cn.weforward.protocol.ops.secure.RightTableItem;
import cn.weforward.protocol.ops.traffic.TrafficTable;
import cn.weforward.protocol.ops.traffic.TrafficTableItem;
import cn.weforward.protocol.support.BeanObjectMapper;
import cn.weforward.protocol.support.SimpleObjectMapperSet;
import cn.weforward.protocol.support.datatype.FriendlyList;
import cn.weforward.protocol.support.datatype.FriendlyObject;

/**
 * 基于Http协议的<code>Keeper</code>实现
 * 
 * @author zhangpengji
 *
 */
public class HttpKeeper implements Keeper {

	protected String m_AccessId;
	protected ServiceInvoker m_Invoker;
	protected ObjectMapperSet m_Mappers;

	/**
	 * 使用网关内置access构造
	 * 
	 * @param preUrl
	 * @param secret
	 * @throws NoSuchAlgorithmException
	 */
	public HttpKeeper(String preUrl, String secret) throws NoSuchAlgorithmException {
		this(preUrl, AccessExt.GATEWAY_INTERNAL_ACCESS_ID, Hex.encode(Access.Helper.secretToAccessKey(secret)));
	}

	public HttpKeeper(String preUrl, String accessId, String accessKey) {
		if (!StringUtil.isEmpty(accessId)) {
			accessId = accessId.trim();
		}
		if (!StringUtil.isEmpty(accessKey)) {
			accessKey = accessKey.trim();
		}
		m_AccessId = accessId;
		m_Invoker = ServiceInvokerFactory.create(ServiceName.KEEPER.name, preUrl, accessId, accessKey);

		SimpleObjectMapperSet mappers = new SimpleObjectMapperSet();
		mappers.register(BeanObjectMapper.getInstance(AccessExtVo.class));
		mappers.register(BeanObjectMapper.getInstance(RightTableVo.class));
		mappers.register(BeanObjectMapper.getInstance(RightTableItemVo.class));
		mappers.register(BeanObjectMapper.getInstance(TrafficTableVo.class));
		mappers.register(BeanObjectMapper.getInstance(TrafficTableItemVo.class));
		mappers.register(BeanObjectMapper.getInstance(AclTableVo.class));
		mappers.register(BeanObjectMapper.getInstance(AclTableItemVo.class));
		mappers.register(BeanObjectMapper.getInstance(AclTableResourceVo.class));
		m_Mappers = mappers;
	}

	public ServiceInvoker getInvoker() {
		return m_Invoker;
	}

	@Override
	public ResultPage<AccessExt> listAccess(String kind, String group) {
		return listAccess(kind, group, null);
	}

	@Override
	public ResultPage<AccessExt> listAccess(String kind, String group, String keyword) {
		String method = "list_access";
		RequestInvokeParam[] params = { RequestInvokeParam.valueOf("kind", kind),
				RequestInvokeParam.valueOf("group", group), RequestInvokeParam.valueOf("keyword", keyword) };
		ResultPage<AccessExtVo> rp = new RemoteResultPage<AccessExtVo>(AccessExtVo.class, getInvoker(), method, params);
		return new TransResultPage<AccessExt, AccessExtVo>(rp) {

			@Override
			protected AccessExt trans(AccessExtVo src) {
				return new AccessExtWrap(src);
			}
		};
	}

	@Override
	public List<String> listAccessGroup(String kind) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("list_access_group",
				RequestInvokeParam.valueOf("kind", kind));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		FriendlyList groups = result.getFriendlyList("content");
		if (null == groups) {
			return Collections.emptyList();
		}
		return groups.toStringList();
	}

	Response invoke(RequestInvokeObject invokeObj) {
		return getInvoker().invoke(invokeObj.toDtObject());
	}

	@Override
	public AccessExt createAccess(String kind, String group, String summary) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("create_access",
				RequestInvokeParam.valueOf("kind", kind), RequestInvokeParam.valueOf("group", group),
				RequestInvokeParam.valueOf("summary", summary));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		AccessExtVo access = result.getObject("content", AccessExtVo.class, m_Mappers);
		return new AccessExtWrap(access);
	}

	private FriendlyObject checkResponse(Response resp) {
		if (0 != resp.getResponseCode()) {
			throw new KeeperException(resp);
		}
		FriendlyObject result = FriendlyObject.valueOf(resp.getServiceResult());
		int code = result.getInt("code", 0);
		if (0 != code) {
			throw new KeeperException(code, result.getString("msg"));
		}
		return result;
	}

	@Override
	public AccessExt updateAccess(String accessId, String summary, Boolean valid) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("update_access");
		invokeObj.putParam("id", accessId);
		if (!StringUtil.isEmpty(summary)) {
			invokeObj.putParam("summary", summary);
		}
		if (null != valid) {
			invokeObj.putParam("valid", valid.toString());
		}
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		AccessExtVo access = result.getObject("content", AccessExtVo.class, m_Mappers);
		return new AccessExtWrap(access);
	}

	@Override
	public ResultPage<String> listServiceName(String keyword) {
		String method = "list_service_name";
		RequestInvokeParam[] params = { RequestInvokeParam.valueOf("keyword", keyword) };
		return new RemoteResultPage<>(getInvoker(), method, params);
	}

	@Override
	public ResultPage<ServiceExt> listService(String name) {
		String method = "list_service";
		RequestInvokeParam[] params = { RequestInvokeParam.valueOf("name", name) };
		ResultPage<ServiceExtVo> rp = new RemoteResultPage<ServiceExtVo>(ServiceExtVo.class, getInvoker(), method,
				params);
		return new TransResultPage<ServiceExt, ServiceExtVo>(rp) {

			@Override
			protected ServiceExt trans(ServiceExtVo src) {
				return new ServiceExtWrap(src);
			}
		};
	}

	@Override
	public ResultPage<ServiceExt> searchService(SearchServiceParams searchParams) {
		String method = "search_service";
		RequestInvokeParam[] params = { RequestInvokeParam.valueOf("keyword", searchParams.getKeyword()),
				RequestInvokeParam.valueOf("running_id", searchParams.getRunningId()) };
		ResultPage<ServiceExtVo> rp = new RemoteResultPage<ServiceExtVo>(ServiceExtVo.class, getInvoker(), method,
				params);
		return new TransResultPage<ServiceExt, ServiceExtVo>(rp) {

			@Override
			protected ServiceExt trans(ServiceExtVo src) {
				return new ServiceExtWrap(src);
			}
		};
	}

	@Override
	public RightTable getRightTable(String name) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("get_right_table",
				RequestInvokeParam.valueOf("name", name));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		RightTableVo vo = result.getObject("content", RightTableVo.class, m_Mappers);
		return openRightTable(vo);
	}

	RightTable openRightTable(RightTableVo vo) {
		if (null == vo) {
			return null;
		}
		return new RightTableWrap(vo, this);
	}

	@Override
	public RightTable appendRightRule(String name, RightTableItem item) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("append_right_rule", m_Mappers);
		invokeObj.putParam("name", name);
		invokeObj.putParam("item", RightTableItemVo.valueOf(item));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		RightTableVo vo = result.getObject("content", RightTableVo.class, m_Mappers);
		return openRightTable(vo);
	}

	@Override
	public RightTable insertRightRule(String name, RightTableItem item, int index) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("insert_right_rule", m_Mappers);
		invokeObj.putParam("name", name);
		invokeObj.putParam("item", RightTableItemVo.valueOf(item));
		invokeObj.putParam("index", index);
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		RightTableVo vo = result.getObject("content", RightTableVo.class, m_Mappers);
		return openRightTable(vo);
	}

	@Override
	public RightTable replaceRightRule(String name, RightTableItem item, int index, String replaceName) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("replace_right_rule", m_Mappers);
		invokeObj.putParams(RequestInvokeParam.valueOf("name", name), RequestInvokeParam.valueOf("index", index),
				RequestInvokeParam.valueOf("replace_name", replaceName));
		invokeObj.putParam("item", RightTableItemVo.valueOf(item));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		RightTableVo vo = result.getObject("content", RightTableVo.class, m_Mappers);
		return openRightTable(vo);
	}

	@Override
	public RightTable removeRightRule(String name, int index, String removeName) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("remove_right_rule");
		invokeObj.putParams(RequestInvokeParam.valueOf("name", name), RequestInvokeParam.valueOf("index", index),
				RequestInvokeParam.valueOf("remove_name", removeName));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		RightTableVo vo = result.getObject("content", RightTableVo.class, m_Mappers);
		return openRightTable(vo);
	}

	@Override
	public RightTable moveRightRule(String name, int from, int to) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("move_right_rule");
		invokeObj.putParams(RequestInvokeParam.valueOf("name", name), RequestInvokeParam.valueOf("from", from),
				RequestInvokeParam.valueOf("to", to));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		RightTableVo vo = result.getObject("content", RightTableVo.class, m_Mappers);
		return openRightTable(vo);
	}

	@Override
	public TrafficTable getTrafficTable(String name) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("get_traffic_table");
		invokeObj.putParams(RequestInvokeParam.valueOf("name", name));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		TrafficTableVo vo = result.getObject("content", TrafficTableVo.class, m_Mappers);
		return openTrafficTable(vo);
	}

	TrafficTable openTrafficTable(TrafficTableVo vo) {
		if (null == vo) {
			return null;
		}
		return new TrafficTableWrap(vo, this);
	}

	@Override
	public TrafficTable appendTrafficRule(String name, TrafficTableItem item) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("append_traffic_rule", m_Mappers);
		invokeObj.putParam("item", TrafficTableItemVo.valueOf(item));
		invokeObj.putParam("name", name);
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		TrafficTableVo vo = result.getObject("content", TrafficTableVo.class, m_Mappers);
		return openTrafficTable(vo);
	}

	@Override
	public TrafficTable insertTrafficRule(String name, TrafficTableItem item, int index) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("insert_traffic_rule", m_Mappers);
		invokeObj.putParam("name", name);
		invokeObj.putParam("item", TrafficTableItemVo.valueOf(item));
		invokeObj.putParam("index", index);
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		TrafficTableVo vo = result.getObject("content", TrafficTableVo.class, m_Mappers);
		return openTrafficTable(vo);
	}

	@Override
	public TrafficTable replaceTrafficRule(String name, TrafficTableItem item, int index, String replaceName) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("replace_traffic_rule", m_Mappers);
		invokeObj.putParam("item", TrafficTableItemVo.valueOf(item));
		invokeObj.putParams(RequestInvokeParam.valueOf("name", name), RequestInvokeParam.valueOf("index", index),
				RequestInvokeParam.valueOf("replace_name", replaceName));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		TrafficTableVo vo = result.getObject("content", TrafficTableVo.class, m_Mappers);
		return openTrafficTable(vo);
	}

	@Override
	public TrafficTable removeTrafficRule(String name, int index, String removeName) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("remove_traffic_rule");
		invokeObj.putParams(RequestInvokeParam.valueOf("name", name), RequestInvokeParam.valueOf("index", index),
				RequestInvokeParam.valueOf("remove_name", removeName));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		TrafficTableVo vo = result.getObject("content", TrafficTableVo.class, m_Mappers);
		return openTrafficTable(vo);
	}

	@Override
	public TrafficTable moveTrafficRule(String name, int from, int to) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("move_traffic_rule");
		invokeObj.putParams(RequestInvokeParam.valueOf("name", name), RequestInvokeParam.valueOf("from", from),
				RequestInvokeParam.valueOf("to", to));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		TrafficTableVo vo = result.getObject("content", TrafficTableVo.class, m_Mappers);
		return openTrafficTable(vo);
	}

	@Override
	public AclTable getAclTable(String name) {
		RequestInvokeObject invokeObj = new RequestInvokeObject("get_acl_table");
		invokeObj.putParams(RequestInvokeParam.valueOf("name", name));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		AclTableVo vo = result.getObject("content", AclTableVo.class, m_Mappers);
		return openAclTable(vo);
	}

	AclTable openAclTable(AclTableVo vo) {
		if (null == vo) {
			return null;
		}
		return new AclTableWrap(vo, this);
	}

	@Override
	public DtObject debugService(String serviceName, String serviceNo, String source, String name, String args)
			throws KeeperException {
		RequestInvokeObject invokeObj = new RequestInvokeObject("debug_service",
				RequestInvokeParam.valueOf("service_name", serviceName),
				RequestInvokeParam.valueOf("service_no", serviceNo),
				RequestInvokeParam.valueOf("script-source", source), RequestInvokeParam.valueOf("script-name", name),
				RequestInvokeParam.valueOf("script-args", args));
		Response resp = invoke(invokeObj);
		FriendlyObject result = checkResponse(resp);
		return result.getObject("content");
	}

	@Override
	public ResultPage<ServiceSummary> listServiceSummary(String keyword) {
		String method = "list_service_summary";
		RequestInvokeParam[] params = { RequestInvokeParam.valueOf("keyword", keyword) };
		return new RemoteResultPage<ServiceSummary>(ServiceSummary.class, getInvoker(), method, params);
	}

	@Override
	public AclTable appendAclRule(String name, AclTableItem item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AclTable replaceAclRule(String name, AclTableItem item, int index, String replaceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AclTable removeAclRule(String name, int index, String removeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AclTable moveAclRule(String name, int from, int to) {
		// TODO Auto-generated method stub
		return null;
	}

}
