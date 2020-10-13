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
package cn.weforward.protocol.gateway.vo;

import java.util.ArrayList;
import java.util.List;

import cn.weforward.protocol.ops.secure.AclTableItem;
import cn.weforward.protocol.ops.secure.AclTableResource;

public class AclTableItemVo {
	public String accessId;
	public String accessKind;
	public String accessGroup;
	public String name;
	public String description;
	public List<AclTableResourceVo> resources;

	public AclTableItemVo() {

	}

	public AclTableItemVo(AclTableItem item) {
		this.accessId = item.getAccessId();
		this.accessKind = item.getAccessKind();
		this.accessGroup = item.getAccessGroup();
		this.name = item.getName();
		this.description = item.getDescription();
		List<AclTableResource> reses = item.getResources();
		if (reses.size() > 0) {
			List<AclTableResourceVo> vos = new ArrayList<AclTableResourceVo>(reses.size());
			for (AclTableResource res : reses) {
				AclTableResourceVo vo = AclTableResourceVo.valueOf(res);
				vos.add(vo);
			}
			this.resources = vos;
		}
	}

	public static AclTableItemVo valueOf(AclTableItem item) {
		if (null == item) {
			return null;
		}
		if (item instanceof AclTableItemWrap) {
			return ((AclTableItemWrap) item).getVo();
		}
		return new AclTableItemVo(item);
	}

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public String getAccessKind() {
		return accessKind;
	}

	public void setAccessKind(String accessKind) {
		this.accessKind = accessKind;
	}

	public String getAccessGroup() {
		return accessGroup;
	}

	public void setAccessGroup(String accessGroup) {
		this.accessGroup = accessGroup;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<AclTableResourceVo> getResources() {
		return resources;
	}

	public void setResources(List<AclTableResourceVo> resources) {
		this.resources = resources;
	}
}
