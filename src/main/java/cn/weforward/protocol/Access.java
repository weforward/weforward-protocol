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

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import cn.weforward.common.util.StringUtil;

/**
 * 访问凭证
 * 
 * @author zhangpengji
 *
 */
public interface Access {

	/** Access Id组成部分的分隔符 */
	char SPEARATOR = '-';
	/** Access Id组成部分的分隔符，字符串格式 */
	String SPEARATOR_STR = "" + SPEARATOR;

	/**
	 * 种类 - 服务凭证
	 * <p>
	 * 用于微服务注册、微服务相互调用、授权外部系统调用等
	 */
	String KIND_SERVICE = "H";
	/**
	 * 种类 - 用户凭证
	 * <p>
	 * 授权给个人的微服务调用凭证
	 */
	String KIND_USER = "US";
	/**
	 * 种类 - 网关凭证
	 * <p>
	 * 用于网关自身通信、网关调用微服务
	 */
	String KIND_GATEWAY = "GW";
	/** 种类集合 */
	List<String> KIND_ALL = Arrays.asList(KIND_SERVICE, KIND_USER, KIND_GATEWAY);

	/**
	 * access id
	 * 
	 * @return 凭证id
	 */
	String getAccessId();

	/**
	 * access key
	 * 
	 * @return 凭证key
	 */
	byte[] getAccessKey();

	/**
	 * 获取Base16(HEX)表示的access key
	 * 
	 * @return 凭证key
	 */
	String getAccessKeyHex();

	/**
	 * 获取Base64表示的access key
	 * 
	 * @return 凭证key
	 */
	String getAccessKeyBase64();

	/**
	 * 所属种类
	 * 
	 * @return 所属种类
	 */
	String getKind();

	/**
	 * 租户标识
	 * 
	 * @return 租户标识
	 */
	String getTenant();

	/**
	 * 基于OAuth协议生成的用户身份
	 * 
	 * @return 用户身份
	 */
	String getOpenid();

	/**
	 * 是否有效
	 * 
	 * @return 有效返回true
	 */
	boolean isValid();

	final class Helper {

		/**
		 * 获取Access Id的Kind
		 * 
		 * @param accessId
		 * @return 凭证种类
		 */
		public static String getKind(String accessId) {
			if (null == accessId || 0 == accessId.length()) {
				return null;
			}
			int idx = accessId.indexOf(SPEARATOR);
			if (-1 == idx || 0 == idx) {
				return null;
			}
			return accessId.substring(0, idx);
		}

		/**
		 * 根据secret字串计算access key
		 * 
		 * @param secret
		 * @return 字节数组
		 * @throws NoSuchAlgorithmException
		 */
		public static byte[] secretToAccessKey(String secret) throws NoSuchAlgorithmException {
			java.security.MessageDigest md;
			md = java.security.MessageDigest.getInstance("SHA-256");
			if (!StringUtil.isEmpty(secret)) {
				md.update(secret.getBytes());
			}
			return md.digest();
		}
	}
}
