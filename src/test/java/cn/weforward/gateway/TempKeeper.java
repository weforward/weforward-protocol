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
package cn.weforward.gateway;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import cn.weforward.common.ResultPage;
import cn.weforward.common.util.ResultPageHelper;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.gateway.ServiceSummary;
import cn.weforward.protocol.gateway.http.HttpKeeper;
import cn.weforward.protocol.gateway.vo.RightTableItemVo;
import cn.weforward.protocol.gateway.vo.RightTableItemWrap;
import cn.weforward.protocol.gateway.vo.TrafficTableItemVo;
import cn.weforward.protocol.gateway.vo.TrafficTableItemWrap;
import cn.weforward.protocol.ops.AccessExt;
import cn.weforward.protocol.ops.ServiceExt;
import cn.weforward.protocol.ops.secure.RightTable;
import cn.weforward.protocol.ops.secure.RightTableItem;
import cn.weforward.protocol.ops.traffic.TrafficTable;
import cn.weforward.protocol.ops.traffic.TrafficTableItem;

public class TempKeeper {

	HttpKeeper keeper;

	public TempKeeper(String id, String key, String url) {
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

		keeper = new HttpKeeper(url, id, key);
		if (StringUtil.isEmpty(id) && StringUtil.isEmpty(key)) {
			keeper.getInvoker().setAuthType(Header.AUTH_TYPE_NONE);
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("请输入环境：");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String env = reader.readLine();
		InputStream file = new FileInputStream("private/keeper." + env + ".properties");
		Properties prop = new Properties();
		prop.load(file);

		TempKeeper tmp = new TempKeeper(prop.getProperty("id"), prop.getProperty("key"), prop.getProperty("url"));
		String cmd = reader.readLine();
		while (null != cmd) {
			if (cmd.startsWith("acc ") || cmd.startsWith("access ")) {
				String[] arr = cmd.split(" ");
				if ("list".equals(arr[1])) {
					// access list H _HY_INIT_
					if (arr.length >= 4) {
						tmp.listAccess(arr[2], arr[3]);
					} else {
						tmp.listAccess(arr[2], null);
					}
				} else if ("groups".equals(arr[1])) {
					// access groups H
					tmp.listAccessGroups(arr[2]);
				} else if ("create".equals(arr[1])) {
					// access create H CommonCompany$08cf445c-062a ias
					tmp.createAccess(arr[2], arr[3], arr[4]);
				}
			} else if (cmd.startsWith("sc ")) {
				String[] arr = cmd.split(" ");
				if ("list".equals(arr[1])) {
					tmp.listService();
				} else if ("names".equals(arr[1])) {
					tmp.listServiceName(arr.length > 2 ? arr[2] : null);
				} else if ("summary".equals(arr[1])) {
					tmp.listServiceSummary(arr.length > 2 ? arr[2] : null);
				}
			} else if (cmd.startsWith("rt ")) {
				String[] arr = cmd.split(" ");
				if ("show".equals(arr[1])) {
					// rt show test
					tmp.showServiceRule(arr[2]);
				} else if ("append".equals(arr[1])) {
					// rt append test 允许全部 * * * true
					tmp.appendServiceRule(arr[2], arr[3], arr[4], arr[5], arr[6], Boolean.valueOf(arr[7]));
				} else if ("replace".equals(arr[1])) {
					// rt replace test 允许全部 * * * true 0 允许全部
					tmp.replaceServiceRule(arr[2], arr[3], arr[4], arr[5], arr[6], Boolean.valueOf(arr[7]),
							Integer.valueOf(arr[8]), arr[9]);
				} else if ("remove".equals(arr[1])) {
					// rt remove test 0 允许全部
					tmp.removeServiceRule(arr[2], Integer.valueOf(arr[3]), arr[4]);
				} else if ("move".equals(arr[1])) {
					// rt move test 0 1
					tmp.moveServiceRule(arr[2], Integer.valueOf(arr[3]), Integer.valueOf(arr[4]));
				}
			} else if (cmd.startsWith("tt ")) {
				String[] arr = cmd.split(" ");
				if ("show".equals(arr[1])) {
					// tt show test
					tmp.showTrafficTable(arr[2]);
				} else if ("append".equals(arr[1])) {
					// tt append test name=all&no=s1&ver=&weight=1&maxFails=3&failTimeout=60
					tmp.appendTrafficRule(arr[2], arr[3]);
				} else if ("replace".equals(arr[1])) {
					// tt replace test name=all&no=1&ver=&weight=1&maxFails=3&failTimeout=60 0 all
					tmp.replaceTrafficRule(arr[2], arr[3], Integer.valueOf(arr[4]), arr[5]);
				} else if ("remove".equals(arr[1])) {
					// tt remove test 0 s1
					tmp.removeTrafficRule(arr[2], Integer.valueOf(arr[3]), arr[4]);
				} else if ("move".equals(arr[1])) {
					// tt move test 0 1
					tmp.moveTrafficRule(arr[2], Integer.valueOf(arr[3]), Integer.valueOf(arr[4]));
				}
			} else if (cmd.startsWith("doc ")) {
				// String[] arr = cmd.split(" ");
				// tmp.documents(arr[1]);
			} else if (cmd.startsWith("acl ")) {
				// String[] arr = cmd.split(" ");
				// if ("show".equals(arr[1])) {
				// // acl show test
				// tmp.showAclRule(arr[2]);
				// } else if ("append".equals(arr[1])) {
				// // acl append test all * * * Config$123-??,7,1;Config$456-??,7,1
				// tmp.appendAclRule(arr[2], arr[3], arr[4], arr[5], arr[6], arr[7]);
				// } else if ("replace".equals(arr[1])) {
				// // acl replace test all * * * Config$123-??,7,1;Config$456-??,6,1 0 all
				// tmp.replaceAclRule(arr[2], arr[3], arr[4], arr[5], arr[6], arr[7],
				// Integer.valueOf(arr[8]), arr[9]);
				// } else if ("remove".equals(arr[1])) {
				// // acl remove test 0 all
				// tmp.removeAclRule(arr[2], Integer.valueOf(arr[3]), arr[4]);
				// } else if ("move".equals(arr[1])) {
				// // acl move test 0 1
				// tmp.moveAclRule(arr[2], Integer.valueOf(arr[3]), Integer.valueOf(arr[4]));
				// }
			}

			System.out.println();
			System.out.print("");
			cmd = reader.readLine();
		}
	}

	public void listAccess(String kind, String group) {
		ResultPage<AccessExt> rp = keeper.listAccess(kind, group);
		if (0 == rp.getCount()) {
			return;
		}
		int max = 1000;
		int count = 0;
		System.out.println("access list:");
		// rp.setPageSize(3);
		// for (int p = 1; rp.gotoPage(p); p++)
		// for (AccessExt i : rp) {
		for (AccessExt i : ResultPageHelper.toForeach(rp)) {
			System.out.println(i.getAccessId() + "," + i.getAccessKeyHex() + "," + i.getSummary() + "," + i.isValid());
			if (count++ > max) {
				System.out.println("too many item : " + rp.getCount());
				return;
			}
		}
	}

	public void listAccessGroups(String kind) {
		List<String> groups = keeper.listAccessGroup(kind);
		System.out.println("access group list:");
		for (String g : groups) {
			System.out.println(g);
		}
	}

	public void createAccess(String kind, String group, String summary) {
		AccessExt acc = keeper.createAccess(kind, group, summary);
		System.out.println(acc.getAccessId() + "," + acc.getAccessKeyHex() + "," + acc.getSummary());
	}

	public void listService() {
		ResultPage<ServiceExt> rp = keeper.listService(null);
		rp.setPageSize(100);
		System.out.println("service list:");
		int page = 1;
		while (rp.gotoPage(page++)) {
			for (ServiceExt i : rp) {
				System.out.println(i.getName() + "(" + i.getNo() + "), urls:" + i.getUrls());
			}
		}

	}

	public void listServiceName(String keyword) {
		ResultPage<String> rp = keeper.listServiceName(keyword);
		rp.setPageSize(100);
		rp.gotoPage(1);
		System.out.println("service list:");
		int page = 1;
		while (rp.gotoPage(page++)) {
			for (String name : rp) {
				System.out.println(name);
			}
		}
	}

	public void listServiceSummary(String keyword) {
		ResultPage<ServiceSummary> rp = keeper.listServiceSummary(keyword);
		rp.setPageSize(100);
		rp.gotoPage(1);
		System.out.println("service list:");
		int page = 1;
		while (rp.gotoPage(page++)) {
			for (ServiceSummary sum : rp) {
				System.out.println(sum.getName()+","+sum.getStatus()+","+sum.getSummary());
			}
		}
	}

	public void showServiceRule(String name) {
		RightTable table = keeper.getRightTable(name);
		show(table);
	}

	public void show(RightTable table) {
		if (null == table) {
			System.out.println("not found!");
			return;
		}
		List<RightTableItem> items = table.getItems();
		if (null == items || 0 == items.size()) {
			System.out.println("empty!");
			return;
		}
		System.out.println("idx|name|access_id|access_kind|access_group|allow");
		for (int i = 0; i < items.size(); i++) {
			RightTableItem item = items.get(i);
			if (null == item) {
				System.out.println("null");
				continue;
			}
			System.out.println(i + "|" + item.getName() + "|" + item.getAccessId() + "|" + item.getAccessKind() + "|"
					+ item.getAccessGroup() + "|" + item.isAllow());
		}
	}

	public void appendServiceRule(String name, String itemName, String accId, String accKind, String accGroup,
			boolean allow) {
		RightTableItemVo item = new RightTableItemVo();
		item.setName(itemName);
		if (!"*".equals(accId)) {
			item.setAccessId(accId);
		}
		if (!"*".equals(accKind)) {
			item.setAccessKind(accKind);
		}
		if (!"*".equals(accGroup)) {
			item.setAccessGroup(accGroup);
		}
		item.setAllow(allow);
		RightTable table = keeper.appendRightRule(name, new RightTableItemWrap(item));
		show(table);
	}

	public void replaceServiceRule(String name, String itemName, String accId, String accKind, String accGroup,
			boolean allow, int index, String replaceName) {
		RightTableItemVo item = new RightTableItemVo();
		item.setName(itemName);
		if (!"*".equals(accId)) {
			item.setAccessId(accId);
		}
		if (!"*".equals(accKind)) {
			item.setAccessKind(accKind);
		}
		if (!"*".equals(accGroup)) {
			item.setAccessGroup(accGroup);
		}
		item.setAllow(allow);
		RightTable table = keeper.replaceRightRule(name, new RightTableItemWrap(item), index, replaceName);
		show(table);
	}

	public void removeServiceRule(String name, int index, String removeName) {
		RightTable table = keeper.removeRightRule(name, index, removeName);
		show(table);
	}

	public void moveServiceRule(String name, int from, int to) {
		RightTable table = keeper.moveRightRule(name, from, to);
		show(table);
	}

	public void showTrafficTable(String name) {
		TrafficTable table = keeper.getTrafficTable(name);
		show(table);
	}

	public void show(TrafficTable table) {
		if (null == table) {
			System.out.println("not found!");
			return;
		}
		List<TrafficTableItem> items = table.getItems();
		if (null == items || 0 == items.size()) {
			System.out.println("empty!");
			return;
		}
		System.out.println(
				"idx|name|no|version|weight|max_fails|fail_timeout|max_concurrent|connect_timeout|read_timeout");
		for (int i = 0; i < items.size(); i++) {
			TrafficTableItem item = items.get(i);
			System.out.println(i + "|" + item.getName() + "|" + item.getServiceNo() + "|" + item.getServiceVersion()
					+ "|" + item.getWeight() + "|" + item.getMaxFails() + "|" + item.getFailTimeout() + "|"
					+ item.getMaxConcurrent() + "|" + item.getConnectTimeout() + "|" + item.getReadTimeout());
		}
	}

	public void appendTrafficRule(String name, String itemFormat) {
		TrafficTableItem item = parseTrafficRule(itemFormat);
		TrafficTable table = keeper.appendTrafficRule(name, item);
		show(table);
	}

	public TrafficTableItem parseTrafficRule(String itemFormat) {
		String[] kvs = itemFormat.split("&");
		TrafficTableItemVo item = new TrafficTableItemVo();
		for (String kv : kvs) {
			String[] arr = kv.split("=");
			if (2 != arr.length) {
				continue;
			}
			String k = arr[0];
			String v = arr[1];
			if ("name".equals(k)) {
				item.setName(v);
			} else if ("no".equals(k)) {
				item.setServiceNo(v);
			} else if ("ver".equals(k)) {
				item.setServiceVersion(v);
			} else if ("maxFails".equals(k)) {
				item.setMaxFails(Integer.valueOf(v));
			} else if ("failTimeout".equals(k)) {
				item.setFailTimeout(Integer.valueOf(v));
			} else if ("maxConcurrent".equals(k)) {
				item.setMaxConcurrent(Integer.valueOf(v));
			} else if ("connectTimeout".equals(k)) {
				item.setConnectTimeout(Integer.valueOf(v));
			} else if ("readTimeout".equals(k)) {
				item.setReadTimeout(Integer.valueOf(v));
			} else if ("weight".equals(k)) {
				item.setWeight(Integer.valueOf(v));
			}
		}
		return new TrafficTableItemWrap(item);
	}

	public void replaceTrafficRule(String name, String itemFormat, int index, String replaceName) {
		TrafficTableItem item = parseTrafficRule(itemFormat);
		TrafficTable table = keeper.replaceTrafficRule(name, item, index, replaceName);
		show(table);
	}

	public void removeTrafficRule(String name, int index, String removeName) {
		TrafficTable table = keeper.removeTrafficRule(name, index, removeName);
		show(table);
	}

	public void moveTrafficRule(String name, int from, int to) {
		TrafficTable table = keeper.moveTrafficRule(name, from, to);
		show(table);
	}
	//
	// public void documents(String name) {
	// List<ServiceDocument> docs = keeper.getDocuments(name);
	// for (ServiceDocument doc : docs) {
	// show(doc);
	// }
	// }
	//
	// public void show(ServiceDocument doc) {
	// System.out.println("===============" + doc.getName() + "-" + doc.getVersion()
	// + "===============");
	// System.out.println("desc:" + doc.getDescription());
	// System.out.print("methods:");
	// for (DocMethod m : doc.getMethods()) {
	// System.out.print(m.getName() + ",");
	// }
	// System.out.println();
	// }
	//
	// public void showAclRule(String name) {
	// HyAclTableInfo table = keeper.getAclTable(name);
	// show(table);
	// }
	//
	// public void show(HyAclTableInfo table) {
	// if (null == table) {
	// System.out.println("not found!");
	// return;
	// }
	// if (null == table.items || 0 == table.items.size()) {
	// System.out.println("empty!");
	// return;
	// }
	// for (int i = 0; i < table.items.size(); i++) {
	// AccessRuleItem acc = table.items.get(i);
	// if (null == acc) {
	// System.out.println("null");
	// continue;
	// }
	// System.out.println(i + "\t" + acc.getName() + "\t" + acc.getAccessId() + "\t"
	// + acc.getAccessKind() + "\t"
	// + acc.getAccessGroup());
	// for (ResourceItem res : acc.getResources()) {
	// System.out.println("\t" + res.getId() + " , right:" + res.getRight() + " ,
	// marks:" + res.getMarks());
	// }
	// }
	// }
	//
	// public void appendAclRule(String name, String itemName, String accId, String
	// accKind, String accGroup,
	// String format) {
	// AclTable.AccessRuleItem item = new AclTable.AccessRuleItem(itemName);
	// if (!"*".equals(accId)) {
	// item.setAccessId(accId);
	// }
	// if (!"*".equals(accKind)) {
	// item.setAccessKind(accKind);
	// }
	// if (!"*".equals(accGroup)) {
	// item.setAccessGroup(accGroup);
	// }
	// String[] resArrs = format.split(";");
	// List<ResourceItem> resItems = new ArrayList<>(resArrs.length);
	// for (String resItem : resArrs) {
	// String[] arr = resItem.split(",");
	// ResourceItem res = new ResourceItem();
	// res.setId(arr[0]);
	// res.setRight(Integer.parseInt(arr[1]));
	// res.setMarks(Integer.parseInt(arr[2]));
	// resItems.add(res);
	// }
	// item.setResources(resItems);
	// HyAclTableInfo table = keeper.appendAclRule(name, item);
	// show(table);
	// }
	//
	// public void replaceAclRule(String name, String itemName, String accId, String
	// accKind, String accGroup,
	// String format, int index, String replaceName) {
	// AclTable.AccessRuleItem item = new AclTable.AccessRuleItem(itemName);
	// if (!"*".equals(accId)) {
	// item.setAccessId(accId);
	// }
	// if (!"*".equals(accKind)) {
	// item.setAccessKind(accKind);
	// }
	// if (!"*".equals(accGroup)) {
	// item.setAccessGroup(accGroup);
	// }
	// String[] resArrs = format.split(";");
	// List<ResourceItem> resItems = new ArrayList<>(resArrs.length);
	// for (String resItem : resArrs) {
	// String[] arr = resItem.split(",");
	// ResourceItem res = new ResourceItem();
	// res.setId(arr[0]);
	// res.setRight(Integer.parseInt(arr[1]));
	// res.setMarks(Integer.parseInt(arr[2]));
	// resItems.add(res);
	// }
	// item.setResources(resItems);
	// HyAclTableInfo table = keeper.replaceAclRule(name, item, index, replaceName);
	// show(table);
	// }
	//
	// public void removeAclRule(String name, int index, String removeName) {
	// HyAclTableInfo table = keeper.removeAclRule(name, index, removeName);
	// show(table);
	// }
	//
	// public void moveAclRule(String name, int from, int to) {
	// HyAclTableInfo table = keeper.moveAclRule(name, from, to);
	// show(table);
	// }
}
