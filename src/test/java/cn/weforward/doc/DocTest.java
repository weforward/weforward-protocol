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
package cn.weforward.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import cn.weforward.protocol.support.doc.DocAttributeVo;

public class DocTest {

	@Test
	public void test() {
		DocAttributeVo v1 = new DocAttributeVo();
		v1.index = 0;
		v1.name = "a";
		DocAttributeVo v2 = new DocAttributeVo();
		v2.index = 0;
		v2.name = "b";

		DocAttributeVo v3 = new DocAttributeVo();
		v3.index = 1;
		v3.name = "b";
		DocAttributeVo v4 = new DocAttributeVo();
		v4.index = 2;
		v4.name = "a";
		List<DocAttributeVo> list = new ArrayList<>();
		list.add(v1);
		list.add(v2);
		list.add(v3);
		list.add(v4);
		Collections.sort(list);
		for (DocAttributeVo vo : list) {
			System.out.println(vo.index + "," + vo.name);
		}
	}

}
