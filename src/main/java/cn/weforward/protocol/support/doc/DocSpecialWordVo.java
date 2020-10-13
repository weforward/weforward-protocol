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
package cn.weforward.protocol.support.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.doc.DocSpecialWord;
import cn.weforward.protocol.exception.ObjectMappingException;
import cn.weforward.protocol.ext.ObjectMapper;
import cn.weforward.protocol.support.datatype.FriendlyObject;
import cn.weforward.protocol.support.datatype.SimpleDtList;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

/**
 * {@link DocSpecialWord}'s view object
 * 
 * @author zhangpengji
 *
 */
public class DocSpecialWordVo implements DocSpecialWord {

	public String name;
	public String description;
	public ItemVo tableHeader;
	public List<ItemVo> tableItems;

	public static final ObjectMapper<ItemVo> ITEM_MAPPER = new ObjectMapper<ItemVo>() {

		@Override
		public String getName() {
			return ItemVo.class.getSimpleName();
		}

		@Override
		public DtObject toDtObject(ItemVo item) throws ObjectMappingException {
			if (null == item) {
				return null;
			}
			SimpleDtObject obj = new SimpleDtObject();
			obj.put("key", item.key);
			obj.put("value", item.value);
			obj.put("description", item.description);
			return obj;
		}

		@Override
		public ItemVo fromDtObject(DtObject obj) throws ObjectMappingException {
			if (null == obj) {
				return null;
			}
			FriendlyObject fobj = new FriendlyObject(obj);
			ItemVo item = new ItemVo();
			item.key = fobj.getString("key");
			item.value = fobj.getString("value");
			item.description = fobj.getString("description");
			return item;
		}

	};

	public static final ObjectMapper<DocSpecialWordVo> MAPPER = new ObjectMapper<DocSpecialWordVo>() {

		@Override
		public DtObject toDtObject(DocSpecialWordVo word) throws ObjectMappingException {
			if (null == word) {
				return null;
			}
			SimpleDtObject obj = new SimpleDtObject();
			obj.put("name", word.name);
			obj.put("description", word.description);
			obj.put("table_header", ITEM_MAPPER.toDtObject(word.tableHeader));
			obj.put("table_items", SimpleDtList.toDtList(word.tableItems, ITEM_MAPPER));
			return obj;
		}

		@Override
		public String getName() {
			return DocSpecialWordVo.class.getSimpleName();
		}

		@Override
		public DocSpecialWordVo fromDtObject(DtObject obj) throws ObjectMappingException {
			if (null == obj) {
				return null;
			}
			FriendlyObject fobj = new FriendlyObject(obj);
			DocSpecialWordVo word = new DocSpecialWordVo();
			word.name = fobj.getString("name");
			word.description = fobj.getString("description");
			word.tableHeader = fobj.getObject("table_header", ITEM_MAPPER);
			word.tableItems = fobj.getList("table_items", ITEM_MAPPER);
			return word;
		}
	};

	public static List<DocSpecialWordVo> toVoList(List<DocSpecialWord> list) {
		if (null == list || 0 == list.size()) {
			return Collections.emptyList();
		}
		List<DocSpecialWordVo> vos = new ArrayList<>(list.size());
		for (DocSpecialWord o : list) {
			vos.add(DocSpecialWordVo.valueOf(o));
		}
		return vos;
	}

	public DocSpecialWordVo() {

	}

	public DocSpecialWordVo(DocSpecialWord word) {
		this.name = word.getName();
		this.description = word.getDescription();
		this.tableHeader = ItemVo.valueOf(word.getTableHeader());
		this.tableItems = ItemVo.toVoList(word.getTableItems());
	}

	public static DocSpecialWordVo valueOf(DocSpecialWord word) {
		if (null == word) {
			return null;
		}
		if (word instanceof DocSpecialWordVo) {
			return (DocSpecialWordVo) word;
		}
		return new DocSpecialWordVo(word);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Item getTableHeader() {
		return tableHeader;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Item> getTableItems() {
		return (List<Item>) (List<?>) tableItems;
	}

	public static class ItemVo implements Item {

		public String key;
		public String value;
		public String description;

		public ItemVo() {

		}

		public ItemVo(Item item) {
			this.key = item.getKey();
			this.value = item.getValue();
			this.description = item.getDescription();
		}

		public static ItemVo valueOf(Item item) {
			if (null == item) {
				return null;
			}
			if (item instanceof ItemVo) {
				return (ItemVo) item;
			}
			return new ItemVo(item);
		}

		public static List<ItemVo> toVoList(List<Item> list) {
			if (null == list || 0 == list.size()) {
				return Collections.emptyList();
			}
			List<ItemVo> vos = new ArrayList<>(list.size());
			for (Item o : list) {
				vos.add(ItemVo.valueOf(o));
			}
			return vos;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public String getDescription() {
			return description;
		}

	}
}
