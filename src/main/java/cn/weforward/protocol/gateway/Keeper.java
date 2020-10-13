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
package cn.weforward.protocol.gateway;

import java.util.List;

import cn.weforward.common.ResultPage;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.exception.WeforwardException;
import cn.weforward.protocol.gateway.exception.KeeperException;
import cn.weforward.protocol.ops.AccessExt;
import cn.weforward.protocol.ops.ServiceExt;
import cn.weforward.protocol.ops.secure.AclTable;
import cn.weforward.protocol.ops.secure.AclTableItem;
import cn.weforward.protocol.ops.secure.RightTable;
import cn.weforward.protocol.ops.secure.RightTableItem;
import cn.weforward.protocol.ops.traffic.TrafficTable;
import cn.weforward.protocol.ops.traffic.TrafficTableItem;

/**
 * （网关）管理接口
 * 
 * @author zhangpengji
 *
 */
public interface Keeper {

	/**
	 * 按类型与组列举Access
	 * 
	 * @param kind
	 * @param group
	 * 
	 */
	ResultPage<AccessExt> listAccess(String kind, String group);

	/**
	 * 按类型、组、关键字列举Access
	 * 
	 * @param kind
	 * @param group
	 * @param keyword
	 * 
	 */
	ResultPage<AccessExt> listAccess(String kind, String group, String keyword);

	/**
	 * 按类型列举Access组
	 * 
	 * @param kind
	 * 
	 */
	List<String> listAccessGroup(String kind);

	/**
	 * 创建Access
	 * 
	 * @param kind    类型
	 * @param group   组
	 * @param summary 描述信息
	 * 
	 */
	AccessExt createAccess(String kind, String group, String summary);

	/**
	 * 更新Access
	 * 
	 * @param accessId Access的唯一标识
	 * @param summary  描述信息。null表示不更改
	 * @param valid    是否有效。null表示不更改
	 * 
	 */
	AccessExt updateAccess(String accessId, String summary, Boolean valid);

	/**
	 * 列举已注册的微服务名称
	 * 
	 * 
	 */
	ResultPage<String> listServiceName(String keyword);

	/**
	 * 列举已注册的微服务实例
	 * 
	 * @param name 微服务名，可空
	 * 
	 */
	ResultPage<ServiceExt> listService(String name);

	/**
	 * 查询微服务实例
	 * 
	 * @param params
	 * 
	 */
	ResultPage<ServiceExt> searchService(SearchServiceParams params);

	/**
	 * 获取服务的权限表
	 * 
	 * @param name
	 * 
	 * @throws WeforwardException
	 */
	RightTable getRightTable(String name);

	/**
	 * 追加服务的权限规则
	 * 
	 * @param name 服务名
	 * @param item 规则项
	 * 
	 */
	RightTable appendRightRule(String name, RightTableItem item);

	/**
	 * 插入服务的权限规则
	 * 
	 * @param name  服务名
	 * @param item  规则项
	 * @param index 插入的位置
	 * 
	 */
	RightTable insertRightRule(String name, RightTableItem item, int index);

	/**
	 * 替换服务的权限规则
	 * 
	 * @param name        服务名
	 * @param item        新规则项
	 * @param index       替换的位置
	 * @param replaceName 替换项的名称。用于校验，可空
	 * 
	 */
	RightTable replaceRightRule(String name, RightTableItem item, int index, String replaceName);

	/**
	 * 移除服务的权限规则
	 * 
	 * @param name       服务名
	 * @param index      移除的位置
	 * @param removeName 移除项的名称
	 * 
	 */
	RightTable removeRightRule(String name, int index, String removeName);

	/**
	 * 移动权限规则的位置
	 * 
	 * @param name
	 * @param from
	 * @param to
	 * 
	 */
	RightTable moveRightRule(String name, int from, int to);

	/**
	 * 获取服务的流量表
	 * 
	 * @param name
	 * 
	 */
	TrafficTable getTrafficTable(String name);

	/**
	 * 追加服务的流量规则
	 * 
	 * @param name 服务名
	 * @param item 规则项
	 * 
	 */
	TrafficTable appendTrafficRule(String name, TrafficTableItem item);

	/**
	 * 插入服务的流量规则
	 * 
	 * @param name  服务名
	 * @param item  规则项
	 * @param index 插入的位置
	 * 
	 */
	TrafficTable insertTrafficRule(String name, TrafficTableItem item, int index);

	/**
	 * 替换服务的流量规则
	 * 
	 * @param name        服务名
	 * @param item        新规则项
	 * @param index       替换的位置
	 * @param replaceName 替换项的名称。用于校验，可空
	 * 
	 */
	TrafficTable replaceTrafficRule(String name, TrafficTableItem item, int index, String replaceName);

	/**
	 * 移除服务的流量规则
	 * 
	 * @param name       服务名
	 * @param index      移除的位置
	 * @param removeName 移除项的名称。用于校验，可空
	 * 
	 */
	TrafficTable removeTrafficRule(String name, int index, String removeName);

	/**
	 * 移动流量规则的位置
	 * 
	 * @param name
	 * @param from
	 * @param to
	 * 
	 */
	TrafficTable moveTrafficRule(String name, int from, int to);

	// /**
	// * 获取微服务（多版本）的文档
	// *
	// * @param name
	//
	// */
	// List<ServiceDocument> getDocuments(String name);

	/**
	 * 获取服务的ACL表
	 * 
	 * @param name
	 * 
	 */
	AclTable getAclTable(String name);

	/**
	 * 追加服务的ACL规则项
	 * 
	 * @param name 服务名
	 * @param item 规则项
	 * 
	 * @throws WeforwardException
	 */
	AclTable appendAclRule(String name, AclTableItem item);

	/**
	 * 替换服务的ACL规则项
	 * 
	 * @param name        服务名
	 * @param item        新规则项
	 * @param index       替换的位置
	 * @param replaceName 替换项的名称
	 * 
	 */
	AclTable replaceAclRule(String name, AclTableItem item, int index, String replaceName);

	/**
	 * 移除服务的ACL规则项
	 * 
	 * @param name       服务名
	 * @param index      移除的位置
	 * @param removeName 移除项的名称
	 * 
	 */
	AclTable removeAclRule(String name, int index, String removeName);

	/**
	 * 移动ACL规则项的位置
	 * 
	 * @param name
	 * @param from
	 * @param to
	 * 
	 */
	AclTable moveAclRule(String name, int from, int to);

	/**
	 * 服务调试（执行脚本代码）
	 * 
	 * @param serviceName  微服务名
	 * @param serviceNo    微服务编号
	 * @param scriptSource 脚本源代码
	 * @param scriptName   脚本类名
	 * @param scriptArgs   脚本参数，表单格式（application/x-www-form-urlencoded）
	 * 
	 * @throws KeeperException
	 */
	DtObject debugService(String serviceName, String serviceNo, String scriptSource, String scriptName,
			String scriptArgs) throws KeeperException;
}
