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
package cn.weforward.protocol.aio.http;

import cn.weforward.protocol.Header;

/**
 * Http相关常量
 * 
 * @author zhangpengji
 *
 */
public interface HttpConstants {

	/** GET Method */
	public static final String METHOD_GET = "GET";

	/** POST Method */
	public static final String METHOD_POST = "POST";

	/** OPTIONS Method */
	public static final String METHOD_OPTIONS = "OPTIONS";

	/**
	 * 200 OK
	 */
	public static final int OK = 200;

	/**
	 * 202 Accepted
	 */
	public static final int ACCEPTED = 202;

	/**
	 * 203 Non-Authoritative Information (since HTTP/1.1)
	 */
	public static final int NON_AUTHORITATIVE_INFORMATION = 203;

	/**
	 * 204 No Content
	 */
	public static final int NO_CONTENT = 204;

	/**
	 * 206 Partial Content
	 */
	public static final int PARTIAL_CONTENT = 206;

	/**
	 * 300 Multiple Choices
	 */
	public static final int MULTIPLE_CHOICES = 300;

	/**
	 * 301 Moved Permanently
	 */
	public static final int MOVED_PERMANENTLY = 301;

	/**
	 * 302 Moved Temporarily
	 */
	public static final int MOVED_TEMPORARILY = 302;

	/**
	 * 303 See Other (since HTTP/1.1)
	 */
	public static final int SEE_OTHER = 303;

	/**
	 * 304 Not Modified
	 */
	public static final int NOT_MODIFIED = 304;

	/**
	 * 307 Temporary Redirect (since HTTP/1.1)
	 */
	public static final int TEMPORARY_REDIRECT = 307;

	/**
	 * 308 Permanent Redirect (RFC7538)
	 */
	public static final int PERMANENT_REDIRECT = 308;

	/**
	 * 400 Bad Request
	 */
	public static final int BAD_REQUEST = 400;

	/**
	 * 401 Unauthorized
	 */
	public static final int UNAUTHORIZED = 401;

	/**
	 * 402 Payment Required
	 */
	public static final int PAYMENT_REQUIRED = 402;

	/**
	 * 403 Forbidden
	 */
	public static final int FORBIDDEN = 403;

	/**
	 * 404 Not Found
	 */
	public static final int NOT_FOUND = 404;

	/**
	 * 405 Method Not Allowed
	 */
	public static final int METHOD_NOT_ALLOWED = 405;

	/**
	 * 406 Not Acceptable
	 */
	public static final int NOT_ACCEPTABLE = 406;

	/**
	 * 408 Request Timeout
	 */
	public static final int REQUEST_TIMEOUT = 408;

	/**
	 * 409 Conflict
	 */
	public static final int CONFLICT = 409;

	/**
	 * 410 Gone
	 */
	public static final int GONE = 410;

	// /**
	// * 411 Length Required
	// */
	// public static final int LENGTH_REQUIRED = 411;
	//
	// /**
	// * 412 Precondition Failed
	// */
	// public static final int PRECONDITION_FAILED = 412;

	/**
	 * 413 Request Entity Too Large
	 */
	public static final int REQUEST_ENTITY_TOO_LARGE = 413;

	/**
	 * 414 Request-URI Too Long
	 */
	public static final int REQUEST_URI_TOO_LONG = 414;

	/**
	 * 415 Unsupported Media Type
	 */
	public static final int UNSUPPORTED_MEDIA_TYPE = 415;

	// /**
	// * 416 Requested Range Not Satisfiable
	// */
	// public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;

	/**
	 * 417 Expectation Failed
	 */
	public static final int EXPECTATION_FAILED = 417;

	/**
	 * 429 Too Many Requests (RFC6585)
	 */
	public static final int TOO_MANY_REQUESTS = 429;

	/**
	 * 431 Request Header Fields Too Large (RFC6585)
	 */
	public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

	/**
	 * 500 Internal Server Error
	 */
	public static final int INTERNAL_SERVER_ERROR = 500;

	/**
	 * 501 Not Implemented
	 */
	public static final int NOT_IMPLEMENTED = 501;

	/**
	 * 502 Bad Gateway
	 */
	public static final int BAD_GATEWAY = 502;

	/**
	 * 503 Service Unavailable
	 */
	public static final int SERVICE_UNAVAILABLE = 503;

	/**
	 * 504 Gateway Timeout
	 */
	public static final int GATEWAY_TIMEOUT = 504;

	/**
	 * 505 HTTP Version Not Supported
	 */
	public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

	/**
	 * 内容类型
	 */
	public static final String CONTENT_TYPE = "Content-Type";
	/**
	 * 认证信息
	 */
	public static final String AUTHORIZATION = "Authorization";
	/**
	 * noise值
	 */
	public static final String WF_NOISE = Header.WEFORWARD_PREFIX + "-Noise";
	/**
	 * 回源标签
	 */
	public static final String WF_TAG = Header.WEFORWARD_PREFIX + "-Tag";
	/**
	 * 信道
	 */
	public static final String WF_CHANNEL = Header.WEFORWARD_PREFIX + "-Channel";
	/**
	 * 网关版本号
	 */
	public static final String WF_GW_VERSION = Header.WEFORWARD_PREFIX + "-GwVer";
	/**
	 * 是否安全通道标识（用于安全通信、可信的前端代理传入），取值为：on/off
	 */
	public static final String WF_SECURE = Header.WEFORWARD_PREFIX + "-Secure";
	/**
	 * 客户端信息
	 */
	public static final String USER_AGENT = "User-Agent";
	/**
	 * 内容签名
	 */
	public static final String WF_CONTENT_SIGN = Header.WEFORWARD_PREFIX + "-Content-Sign";
	/**
	 * 内容长度
	 */
	public static final String CONTENT_LENGTH = "Content-Length";
	/**
	 * MIME扩展信息
	 */
	public static final String CONTENT_DISPOSITION = "Content-Disposition";
}
