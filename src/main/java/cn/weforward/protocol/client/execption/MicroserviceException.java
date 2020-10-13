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
package cn.weforward.protocol.client.execption;

import cn.weforward.protocol.Response;
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.support.datatype.FriendlyObject;

/**
 * 微服务异常
 * 
 * 微服务端返回的异常
 * 
 * @author daibo
 *
 */
public class MicroserviceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 错误码 */
	protected int m_Code;
	/** 错误信息 */
	protected String m_Msg;
	/** 自定义码-开始 */
	public static final int CUSTOM_CODE_START = 100000;
	/** 自定义码-结束 */
	public static final int CUSTOM_CODE_END = 999999;

	public MicroserviceException(DtObject serviceResult) {
		this(FriendlyObject.valueOf(serviceResult));
	}

	public MicroserviceException(FriendlyObject serviceResult) {
		this(serviceResult.getInt("code"), serviceResult.getString("msg"));
	}

	public MicroserviceException(int code, String msg) {
		super("微服务响应异常:" + code + "/" + msg);
		m_Code = code;
		m_Msg = msg;
	}

	/** 错误码 */
	public int getCode() {
		return m_Code;
	}

	/** 错误信息 */
	public String getMsg() {
		return m_Msg;
	}

	/**
	 * 是否自定义码
	 * 
	 * @return 是返回true
	 */
	public boolean isCustom() {
		return m_Code >= CUSTOM_CODE_START && m_Code < CUSTOM_CODE_END;
	}

	/**
	 * 检查异常
	 * 
	 * @param response
	 * @throws GatewayException
	 */
	public static void checkException(Response response) throws MicroserviceException {
		checkException(response.getServiceResult());
	}

	/***
	 * 是否成功
	 * 
	 * @param serviceResult
	 * @return 成功返回true
	 */
	public static boolean isSuccess(FriendlyObject serviceResult) {
		return serviceResult.getInt("code") == 0;
	}

	/***
	 * 是否成功
	 * 
	 * @param serviceResult
	 * @return 成功返回true
	 */
	public static boolean isSuccess(DtObject serviceResult) {
		DtNumber code = serviceResult.getNumber("code");
		if (null == code) {
			return false;
		}
		return code.valueInt() == 0;
	}

	/**
	 * 检查异常
	 * 
	 * @param serviceResult
	 * @throws MicroserviceException
	 */
	public static void checkException(DtObject serviceResult) throws MicroserviceException {
		checkException(FriendlyObject.valueOf(serviceResult));
	}

	/**
	 * 检查异常
	 * 
	 * @param serviceResult
	 * @throws MicroserviceException
	 */
	public static void checkException(FriendlyObject serviceResult) throws MicroserviceException {
		if (serviceResult.getInt("code") != 0) {
			throw new MicroserviceException(serviceResult);
		}
	}

}
