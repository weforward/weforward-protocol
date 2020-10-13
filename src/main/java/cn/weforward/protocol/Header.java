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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 协议头信息
 * 
 * @author zhangpengji
 *
 */
public class Header implements Cloneable {

	public static final String WEFORWARD_PREFIX = "WF";

	/** 验证类型 - 无验证 */
	public static final String AUTH_TYPE_NONE = WEFORWARD_PREFIX + "-None";
	/** 验证类型 - 签名（基于SHA256），已逐渐废弃 */
	public static final String AUTH_TYPE_SIGN = WEFORWARD_PREFIX + "-Sign";
	/** 验证类型 - SHA2签名（基于SHA256） */
	public static final String AUTH_TYPE_SHA2 = WEFORWARD_PREFIX + "-SHA2";
	/** 验证类型 - AES加密（基于AES/OFB/NoPadding与CRC32） */
	public static final String AUTH_TYPE_AES = WEFORWARD_PREFIX + "-AES";

	/** 内容格式 - json */
	public static final String CONTENT_TYPE_JSON = "json";

	/** 内容编码 - utf-8 */
	public static final String CHARSET_UTF8 = "utf-8";
	/** 内容编码 - 默认 */
	public static final String CHARSET_DEFAULT = CHARSET_UTF8;

	/** 信道 - 远程过程调用 */
	public static final String CHANNEL_RPC = "rpc";
	/** 信道 - 数据流 */
	public static final String CHANNEL_STREAM = "stream";
	/** 信道 - 通知 */
	public static final String CHANNEL_NOTIFY = "notify";
	/** 全部信道 */
	public static final List<String> CHANNEL_ALL = Arrays.asList(CHANNEL_RPC, CHANNEL_STREAM, CHANNEL_NOTIFY);

	/**
	 * 实现此接口，支持根据通讯协议输出<code>Header</code>
	 * 
	 * @author zhangpengji
	 *
	 */
	public interface HeaderOutput {

		/**
		 * 输出头信息
		 * 
		 * @param header
		 */
		void writeHeader(Header header) throws IOException;
	}

	protected String m_Service;
	protected String m_ContentType;
	protected String m_Charset;
	protected String m_AccessId;
	protected String m_AuthType;
	protected String m_Noise;
	protected String m_Sign;
	protected String m_ContentSign;
	protected String m_Tag;
	protected String m_Channel;
	protected String m_UserAgent;

	public Header(String serviceName) {
		m_Service = serviceName;
	}

	/**
	 * 使用<code>AUTH_TYPE_NONE</code>,<code>CONTENT_TYPE_JSON</code>,<code>CHARSET_UTF8</code>,<code>CHANNEL_RPC</code>构造Header
	 * 
	 * @param serviceName
	 * @return 头对象
	 */
	public static Header valueOf(String serviceName) {
		Header header = new Header(serviceName);
		header.setAuthType(AUTH_TYPE_NONE);
		header.setContentType(CONTENT_TYPE_JSON);
		header.setCharset(CHARSET_UTF8);
		header.setChannel(CHANNEL_RPC);
		return header;
	}

	/**
	 * 获取调用的服务
	 */
	public String getService() {
		return m_Service;
	}

	/**
	 * 设置调用的服务
	 */
	public void setService(String service) {
		m_Service = service;
	}

	/**
	 * 获取内容格式
	 * 
	 * @return 内容格式
	 */
	public String getContentType() {
		return m_ContentType;
	}

	/**
	 * 设置内容格式
	 * 
	 * @param type
	 */
	public void setContentType(String type) {
		m_ContentType = type;
	}

	/**
	 * 获取内容编码的字符集
	 * 
	 * @return 字符集
	 */
	public String getCharset() {
		return m_Charset;
	}

	/**
	 * 设置内容编码的字符集
	 * 
	 * @param charset
	 */
	public void setCharset(String charset) {
		m_Charset = charset;
	}

	/**
	 * 获取noise值（长度为16，包含小写字母与数字，且不重复的字串）
	 * 
	 * @return noise值
	 */
	public String getNoise() {
		return m_Noise;
	}

	/**
	 * 设置noise值
	 * 
	 * @param noise
	 */
	public void setNoise(String noise) {
		m_Noise = noise;
	}

	/**
	 * 获取验证类型
	 * 
	 * @return 类型
	 */
	public String getAuthType() {
		return m_AuthType;
	}

	/**
	 * 设置验证类型
	 * 
	 * @param type
	 */
	public void setAuthType(String type) {
		m_AuthType = type;
	}

	/**
	 * 获取访问id
	 * 
	 * @return 访问id
	 */
	public String getAccessId() {
		return m_AccessId;
	}

	/**
	 * 设置访问id
	 * 
	 * @param id
	 */
	public void setAccessId(String id) {
		m_AccessId = id;
	}

	/**
	 * 获取签名（某些验证类型有此值，如：AUTH_TYPE_SIGN）
	 * 
	 * @return 签名
	 */
	public String getSign() {
		return m_Sign;
	}

	/**
	 * 设置
	 * 
	 * @param sign
	 */
	public void setSign(String sign) {
		m_Sign = sign;
	}

	public String getUserAgent() {
		return m_UserAgent;
	}

	public void setUserAgent(String agent) {
		m_UserAgent = agent;
	}

	/**
	 * 获取WF-Tag
	 * <p>
	 * 通常配合{@linkplain Response}的<code>MARK_KEEP_SERVICE_ORIGIN</code>、
	 * <code>MARK_FORGET_SERVICE_ORIGIN</code>，返回<code>tag</code>给调用方，起到回源的效果
	 * 
	 * @see Response#MARK_KEEP_SERVICE_ORIGIN
	 * @see Response#MARK_FORGET_SERVICE_ORIGIN
	 * @return Tag
	 */
	public String getTag() {
		return m_Tag;
	}

	/**
	 * 设置回源标签WF-Tag
	 * 
	 * @param tag
	 */
	public void setTag(String tag) {
		m_Tag = tag;
	}

	/**
	 * 获取信道，如：{@linkplain #CHANNEL_RPC}
	 * 
	 * @return 信道
	 */
	public String getChannel() {
		return m_Channel;
	}

	/**
	 * 设置信道，如：{@linkplain #CHANNEL_RPC}
	 * 
	 * @param channel
	 */
	public void setChannel(String channel) {
		m_Channel = channel;
	}

	/**
	 * 获取内容签名
	 * 
	 * @return 内容签名
	 */
	public String getContentSign() {
		return m_ContentSign;
	}

	/**
	 * 设置内容签名
	 * 
	 * @param contentSign
	 */
	public void setContentSign(String contentSign) {
		m_ContentSign = contentSign;
	}

	/**
	 * 输出详细信息，用于日志记录
	 * 
	 * @return 详细信息
	 */
	public String getLogDetail() {
		return "{s:" + m_Service + ",acc:" + m_AccessId + ",at:" + m_AuthType + "}";
	}

	@Override
	protected Header clone() throws CloneNotSupportedException {
		return (Header) super.clone();
	}

	public static Header copy(Header other) {
		try {
			return other.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
