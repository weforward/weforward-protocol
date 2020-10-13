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
package cn.weforward.protocol.serial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.weforward.common.execption.InvalidFormatException;
import cn.weforward.common.json.JsonUtil;
import cn.weforward.common.json.StringInput;
import cn.weforward.common.util.StringBuilderPool;
import cn.weforward.protocol.RequestConstants;
import cn.weforward.protocol.ResponseConstants;
import cn.weforward.protocol.datatype.DtBase;
import cn.weforward.protocol.support.datatype.SimpleDtBoolean;
import cn.weforward.protocol.support.datatype.SimpleDtNumber;
import cn.weforward.protocol.support.datatype.SimpleDtString;

/**
 * 延时解析json object、json array的解析器。<br/>
 * 且只认第一个{@linkplain RequestConstants#WF_REQ}、{@linkplain ResponseConstants#WF_RESP}节点
 * 
 * @author zhangpengji
 *
 */
class JsonParser extends JsonUtil {

	static final int SKIP_BLANK_LIMIT = 100;
	static final char INVALID_CHAR = 0xffff;

	static class Context {
		// String str;
		StringInput in;
		// StringBuilder nameBuilder;
		// StringBuilder valueBuilder;
		/** 由流读取过头的残余字符（若值不为INVALID_CHAR时） */
		char remainChar;

		Context(String str, int pos, int length) {
			// this.str = str;
			this.in = new StringInput(str, pos, length);
			// this.nameBuilder = new StringBuilder(128);
			// this.valueBuilder = new StringBuilder();
			this.remainChar = INVALID_CHAR;
		}
	}

	static Map<String, DtBase> parseObject(String str, int offset, int length) throws IOException {
		Context ctx = new Context(str, offset, length);
		char ch;
		ch = JsonUtil.skipBlank(ctx.in, SKIP_BLANK_LIMIT);
		if ('{' != ch) {
			// 不以“{”开头的不是对象
			throw new InvalidFormatException("不是预期的'" + ch + "' " + ctx.in);
		}
		// 看是否“{}”（没有属性的）空对象
		// ch = ctx.in.readChar();
		ch = JsonUtil.skipBlank(ctx.in, SKIP_BLANK_LIMIT);
		if ('}' == ch) {
			// 是“}”，空对象
			return Collections.emptyMap();
		}
		Map<String, DtBase> atts = new HashMap<>();
		String name;
		DtBase value;
		char nameQuot;
		while (ctx.in.available() >= 0) {
			StringBuilder nameBuilder = JsonUtil._NameBuilderPool.poll();
			try {
				// nameBuilder.setLength(0);
				// 读取名称，首先看是否有引号（单或双）开首
				nameQuot = ch;
				if ('"' != nameQuot && '\'' != nameQuot) {
					// 没有引号开首
					nameBuilder.append(nameQuot);
					nameQuot = 0;
				}
				for (;;) {
					ch = ctx.in.readChar();
					if ('\\' == ch) {
						// 转义符，处理转义符
						JsonUtil.unescape(ctx.in, nameBuilder);
						continue;
					}
					if (nameQuot == ch) {
						// 碰到引号结束
						break;
					} else if (':' == ch && 0 == nameQuot) {
						// 碰到“:”符结束，修整掉sbName后部的空格
						JsonUtil.rtrim(nameBuilder);
						break;
					}
					if (nameBuilder.length() == nameBuilder.capacity()) {
						// 名称超长了？
						throw new InvalidFormatException("名称过长(>128) " + ctx.in);
					}
					nameBuilder.append(ch);
				}
				// XXX 名称入池
				// name = nameBuilder.toString();
				name = JsonUtil._NamePool.intern(nameBuilder);
			} finally {
				JsonUtil._NameBuilderPool.offer(nameBuilder);
			}

			// 找到“:”分隔符
			while (':' != ch) {
				ch = ctx.in.readChar();
			}
			// ctx.valueBuilder.setLength(0);
			value = parseValue(ctx, name);
			// 对象属性
			if ((RequestConstants.WF_REQ.equals(name) && atts.containsKey(name))
					|| (ResponseConstants.WF_RESP.equals(name) && atts.containsKey(name))) {
				// 只认第一个WF_REQ、WF_RESP
			} else {
				atts.put(name, value);
			}
			if (INVALID_CHAR != ctx.remainChar) {
				ch = ctx.remainChar;
				ctx.remainChar = INVALID_CHAR;
			} else {
				// 跳过空格等
				ch = JsonUtil.skipBlank(ctx.in, SKIP_BLANK_LIMIT);
			}
			// ch = ctx.in.readChar();
			if (',' == ch) {
				// 下个兄弟节点
				ch = JsonUtil.skipBlank(ctx.in, SKIP_BLANK_LIMIT);
				continue;
			}

			if ('}' == ch) {
				// 对象结束
				break;
			}

			if (']' == ch) {
				// 数组结束，但这应该是对象啊
				throw new InvalidFormatException("不是预期的']' " + ctx.in);
			}
		}
		return atts;
	}

	/**
	 * 解析Json数组
	 * 
	 * @param ctx
	 *            分析过程环境
	 * @return 解析得到的JosnArray
	 * @throws IOException
	 */
	static List<DtBase> parseArray(String str, int offset, int length) throws IOException {
		Context ctx = new Context(str, offset, length);
		char ch;
		ch = JsonUtil.skipBlank(ctx.in, SKIP_BLANK_LIMIT);
		if ('[' != ch) {
			// 不以“[”开头的不是数组
			throw new InvalidFormatException("不是预期的'" + ch + "' " + ctx.in);
		}
		ch = JsonUtil.skipBlank(ctx.in, SKIP_BLANK_LIMIT);
		// 看是否“[]”空数组
		// ch = ctx.in.readChar();
		if (']' == ch) {
			return Collections.emptyList();
		}
		ctx.remainChar = ch;
		List<DtBase> ret = new ArrayList<>();
		DtBase value;
		while (ctx.in.available() >= 0) {
			// ctx.valueBuilder.setLength(0);
			value = parseValue(ctx, null);
			// 数组项
			ret.add(value);
			if (INVALID_CHAR != ctx.remainChar) {
				ch = ctx.remainChar;
				ctx.remainChar = INVALID_CHAR;
			} else {
				// 跳过空格等
				ch = JsonUtil.skipBlank(ctx.in, SKIP_BLANK_LIMIT);
			}
			if (',' == ch) {
				// 下个兄弟节点
				continue;
			}

			if (']' == ch) {
				// 数组结束
				break;
			}

			if ('}' == ch) {
				// 对象结束，但这应该是数组啊
				throw new InvalidFormatException("不是预期的'}' " + ctx.in);
			}
		}
		return ret;
	}

	private static DtBase parseValue(Context ctx, String name) throws IOException {
		char ch, first;
		int startIndex;
		if (INVALID_CHAR != ctx.remainChar) {
			ch = first = ctx.remainChar;
			ctx.remainChar = INVALID_CHAR;
		} else {
			// 跳过空格等
			ch = first = JsonUtil.skipBlank(ctx.in, SKIP_BLANK_LIMIT);
		}
		startIndex = ctx.in.position() - 1;
		// char first = ctx.in.readChar();
		String v;
		StringBuilder valueBuilder = StringBuilderPool._8k.poll();
		try {
			if ('"' == first || '\'' == first) {
				// 由双引号（也兼容不标准的单引号吧）开始的，是字串
				int si = -1;
				for (;;) {
					ch = ctx.in.readChar();
					if ('\\' == ch) {
						// 转义符，处理转义符
						if (si >= 0) {
							valueBuilder.append(ctx.in.getString(), si, ctx.in.position() - 1);
						}
						si = -2;
						JsonUtil.unescape(ctx.in, valueBuilder);
						continue;
					}
					if (0 != first && first == ch) {
						// 碰到引号结束
						break;
					}
					if (-1 == si) {
						si = ctx.in.position() - 1;
					}
					if (si >= 0) {
						continue;
					}
					// if (valueBuilder.length() == valueBuilder.capacity()) {
					// // 超长了？
					// throw illegalFormat(in, "字串太长");
					// }
					valueBuilder.append(ch);
				}
				if (si < 0) {
					v = valueBuilder.toString();
					return SimpleDtString.valueOf(v);
				} else {
					return new JsonDtString(ctx.in.getString(), si, ctx.in.position() - 1);
				}
			}
			if ('{' == first) {
				// 子对象
				findEndChar(ctx.in, '{', '}');
				JsonDtObject ret = new JsonDtObject(ctx.in.getString(), startIndex, ctx.in.position() - startIndex);
				return ret;
			}
			if ('[' == first) {
				// 数组
				findEndChar(ctx.in, '[', ']');
				JsonDtList ret = new JsonDtList(ctx.in.getString(), startIndex, ctx.in.position() - startIndex);
				return ret;
			}
			// 其它类型
			for (;;) {
				if (' ' == ch || '\r' == ch || '\n' == ch) {
					// 空白符（结束）
					break;
				}
				if (',' == ch || '}' == ch || ']' == ch) {
					// 分隔符（也结束），要把它放回残余字符供上游读取
					ctx.remainChar = ch;
					break;
				}
				valueBuilder.append(Character.toLowerCase(ch));
				ch = ctx.in.readChar();
			}
			if (JsonUtil.equalsIgnoreCase(valueBuilder, "true")) {
				// true
				return SimpleDtBoolean.TRUE;
			}
			if (JsonUtil.equalsIgnoreCase(valueBuilder, "false")) {
				// false
				return SimpleDtBoolean.FALSE;
			}
			if (JsonUtil.equalsIgnoreCase(valueBuilder, "null")) {
				return null;
			}

			// 是数值
			v = valueBuilder.toString();
		} finally {
			StringBuilderPool._8k.offer(valueBuilder);
		}
		Number num;
		if (-1 != v.indexOf('.')) {
			// 有小数位
			num = Double.parseDouble(v);
		} else {
			num = Long.parseLong(v);
		}
		return SimpleDtNumber.valueOf(num);
	}

	private static void findEndChar(StringInput in, char begin, char end) throws IOException {
		boolean escape = false;
		char quot = 0;
		int flag = 0;
		while (true) {
			char ch = in.readChar();
			if (escape) {
				// 上一个字符是转义符
				escape = false;
				continue;
			}
			if ('\\' == ch) {
				// 转义符
				escape = true;
				continue;
			}
			if ('"' == ch || '\'' == ch) {
				if (0 == quot) {
					// 引号开始
					quot = ch;
				} else if (quot == ch) {
					// 引号结束
					quot = 0;
				}
				continue;
			}
			if (0 != quot) {
				// 在引号中
				continue;
			}
			if (begin == ch) {
				flag++;
			} else if (end == ch) {
				if (0 == flag) {
					// 找到了
					break;
				} else {
					flag--;
				}
			}
		}
	}
}
