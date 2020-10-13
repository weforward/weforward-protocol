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
package cn.weforward.protocol.ops;

/**
 * 权限
 * 
 * @author zhangpengji
 *
 */
public interface Right {

	/** 未指定 */
	public final short RULE_NONE = 0x00;
	/** 允许 */
	public final short RULE_ALLOW = 0x01;
	/** 禁止 */
	public final short RULE_DISALLOW = 0x80;

	/**
	 * 规则
	 * 
	 */
	short getRule();

	/**
	 * 权限URI的模式串。<br/>
	 * 支持通配符"*"、"**"；"*"匹配"/"以外的字符；"**"放在最后，表示匹配全部字符。
	 * 
	 * <pre>
	 * URI样例：
	 * /abc/*.jspx
	 * /abc/**
	 * /abc/def/*.jspx
	 * /ab-*
	 * /ab-**
	 * **
	 * </pre>
	 * 
	 */
	String getUriPattern();
}
