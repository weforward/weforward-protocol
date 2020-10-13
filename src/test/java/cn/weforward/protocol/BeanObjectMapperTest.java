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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.junit.Test;

import cn.weforward.common.KvPair;
import cn.weforward.common.json.JsonNode;
import cn.weforward.common.json.JsonUtil;
import cn.weforward.common.json.StringInput;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.exception.SerialException;
import cn.weforward.protocol.serial.JsonDtObject;
import cn.weforward.protocol.serial.JsonSerialEngine;
import cn.weforward.protocol.support.BeanObjectMapper;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

public class BeanObjectMapperTest {

	public static class Parent {
		int m_ChildrenCount;
		int[][] intArray;
		ChildA m_ChildA;
		List<ChildB> m_ChildBs;
		List<List<ChildC>> m_ChildCs;

		public int getChildrenCount() {
			return m_ChildrenCount;
		}

		public void setChildrenCount(int count) {
			m_ChildrenCount = count;
		}

		public ChildA getChildA() {
			return m_ChildA;
		}

		public void setChildA(ChildA childA) {
			m_ChildA = childA;
		}

		public List<ChildB> getChildBs() {
			return m_ChildBs;
		}

		public void setChildBs(List<ChildB> childBs) {
			m_ChildBs = childBs;
		}

		public List<List<ChildC>> getChildCs() {
			return m_ChildCs;
		}

		public void setChildCs(List<List<ChildC>> childCs) {
			m_ChildCs = childCs;
		}

		public int[][] getIntArray() {
			return intArray;
		}

		public void setIntArray(int[][] array) {
			this.intArray = array;
		}
	}

	public static class ChildA {
		String m_Name;
		Date m_BrithDay;
		String m_Summary;
		ChildB m_Son;
		String[] m_StringArray;

		public String getName() {
			return m_Name;
		}

		public void setName(String name) {
			m_Name = name;
		}

		public Date getBrithDay() {
			return m_BrithDay;
		}

		public void setBrithDay(Date brithDay) {
			m_BrithDay = brithDay;
		}

		public String getSummary() {
			return m_Summary;
		}

		public void setSummary(String summary) {
			m_Summary = summary;
		}

		public ChildB getSon() {
			return m_Son;
		}

		public void setSon(ChildB son) {
			m_Son = son;
		}

		public String[] getStringArray() {
			return m_StringArray;
		}

		public void setStringArray(String[] stringArray) {
			m_StringArray = stringArray;
		}
	}

	public static class ChildB {
		short m_Age;

		public int getAge() {
			return m_Age;
		}

		public void setAge(short age) {
			m_Age = age;
		}
	}

	public static class ChildC {
		boolean m_Fat;
		float m_Weight;
		float m_Height;

		public boolean isFat() {
			return m_Fat;
		}

		public void setFar(boolean far) {
			m_Fat = far;
		}

		public float getWeight() {
			return m_Weight;
		}

		public void setWeight(float weight) {
			m_Weight = weight;
		}

		public float getHeight() {
			return m_Height;
		}

		public void setHeight(float height) {
			m_Height = height;
		}
	}

	@Test
	public void main() throws SerialException, IOException {
		ChildA a = new ChildA();
		a.setName("childa");
		a.setBrithDay(new Date());
		a.setSummary("'xxx'}\"aaa}编码😄");
		ChildB son = new ChildB();
		son.setAge((short) -1);
		a.setSon(son);
		a.setStringArray(new String[0]);

		List<ChildB> listB = new ArrayList<BeanObjectMapperTest.ChildB>();
		for (int i = 0; i < 3; i++) {
			ChildB b = new ChildB();
			b.setAge((short) i);
			listB.add(b);
		}

		List<List<ChildC>> listListC = new ArrayList<List<ChildC>>();
		for (int i = 0; i < 2; i++) {
			List<ChildC> listC = new ArrayList<BeanObjectMapperTest.ChildC>();
			for (int j = 1; j < 4; j++) {
				ChildC c = new ChildC();
				c.setHeight(j * 2);
				c.setWeight(j * j);
				c.setFar(c.getWeight() > c.getHeight());
				listC.add(c);
			}
			listListC.add(listC);
		}

		Parent p = new Parent();
		p.setChildrenCount(100);
		p.setChildA(a);
		p.setChildBs(listB);
		p.setChildCs(listListC);
		p.setIntArray(new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } });

		BeanObjectMapper<Parent> mapper = BeanObjectMapper.getInstance(Parent.class);
		DtObject dto = mapper.toDtObject(p);
		System.out.println(dto);

		p = mapper.fromDtObject(dto);
		System.out.println(p);

		JsonSerialEngine serialEngine = new JsonSerialEngine();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		serialEngine.serial(dto, Header.CHARSET_UTF8, output);
		byte[] data = output.toByteArray();
		output.close();
		output = null;
		String json = new String(data, Header.CHARSET_UTF8);
		System.out.println(json);

		JsonNode node = JsonUtil.parse(new StringInput(json), null);
		System.out.println(node);

		dto = serialEngine.unserial(new ByteArrayInputStream(data), Header.CHARSET_UTF8);
		dump(dto, "");

		p = mapper.fromDtObject(dto);
		System.out.println(p);

		json = "{\"content\":{\"id\":\"user$00062a3a\",\"name\":\"15886480041\",\"right\":[]},\"code\":0,\"msg\":{}}";
		// node = JsonUtil.parse(new StringInput(json), null);
		// System.out.println(node);
		dto = serialEngine.unserial(new ByteArrayInputStream(json.getBytes(Header.CHARSET_UTF8)), Header.CHARSET_UTF8);
		System.out.println(dto);

		SimpleDtObject so = new SimpleDtObject();
		so.put("json", new JsonDtObject(json));
		serialEngine.serial(so, Header.CHARSET_UTF8, new PrintStream(System.out));
		System.out.flush();
	}

	static void dump(DtObject obj, String indentation) {
		indentation += "\t";
		System.out.println(indentation + "{");
		Enumeration<KvPair<String, DtBase>> atts = obj.getAttributes();
		while (atts.hasMoreElements()) {
			KvPair<String, DtBase> kv = atts.nextElement();
			if (kv.getValue() instanceof DtObject) {
				System.out.println(indentation + kv.getKey() + " : ");
				dump((DtObject) kv.getValue(), indentation);
			} else if (kv.getValue() instanceof DtList) {
				System.out.println(indentation + kv.getKey() + " : ");
				dump((DtList) kv.getValue(), indentation);
			} else {
				System.out.println(indentation + kv.getKey() + " : " + kv.getValue());
			}
		}
		System.out.println(indentation + "}");
	}

	static void dump(DtList list, String indentation) {
		indentation += "\t";
		System.out.println(indentation + "[");
		Enumeration<DtBase> items = list.items();
		while (items.hasMoreElements()) {
			DtBase item = items.nextElement();
			if (item instanceof DtObject) {
				dump((DtObject) item, indentation);
			} else if (item instanceof DtList) {
				dump((DtList) item, indentation);
			} else {
				System.out.println(indentation + item);
			}
		}
		System.out.println(indentation + "]");
	}
}
