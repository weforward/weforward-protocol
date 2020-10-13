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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * HTTP服务端处理上下文
 * 
 * @author liangyi
 *
 */
public interface HttpContext {
	/**
	 * 请求方法，如：GET,POST,HEAD...
	 */
	String getMethod();

	/**
	 * 请求的URI
	 */
	String getUri();

	/**
	 * URL中的参数
	 */
	String getQueryString();

	/**
	 * 客户端地址
	 */
	String getRemoteAddr();

	/**
	 * 获取请求头
	 */
	HttpHeaders getRequestHeaders();

	/**
	 * 建立传输管道直接输出请求内容
	 * 
	 * @param writer
	 *            输出请求内容的流
	 * @param skipBytes
	 *            >0则跳过已在缓冲区的部分内容
	 */
	void requestTransferTo(OutputStream writer, int skipBytes) throws IOException;

	/**
	 * 创建请求内容镜像流，当请求接收完整后不受上下文状态影响，也不受requestTransferTo影响，但必须得在requestTransferTo前调用
	 * 
	 * @param skipBytes
	 *            >0则跳过已在缓冲区的部分内容
	 */
	public InputStream mirrorRequestStream(int skipBytes) throws IOException;

	/**
	 * 获取请求内容流
	 * 
	 * @throws IOException
	 */
	InputStream getRequestStream() throws IOException;

	/**
	 * 副本当前已收到请求的数据（不影响之后的getRequestStream或requestTransferTo）
	 * 
	 * @return 已收到的前部分请求数据的流封装，若已经执行过getRequestStream则返回null
	 * 
	 * @throws IOException
	 */
	public InputStream duplicateRequestStream() throws IOException;

	/**
	 * 请求是否已（接收）完整
	 */
	boolean isRequestCompleted();

	/**
	 * 设置响应超时值
	 * 
	 * @param millis
	 *            超时值（毫秒），若=Integer.MAX_VALUE则使用空闲超时值
	 */
	public void setResponseTimeout(int millis);

	/**
	 * 设置收到HTTP请求的最大字节数
	 * 
	 * @param size
	 *            最大字节数
	 */
	public void setMaxHttpSize(int size);

	/**
	 * 设置响应头
	 * 
	 * @param name
	 *            名称
	 * @param value
	 *            值（若为null则移除该项）
	 */
	void setResponseHeader(String name, String value) throws IOException;

	/**
	 * 打开响应输出
	 * 
	 * @param statusCode
	 *            状态码
	 * @param reasonPhrase
	 *            原因简述
	 * 
	 * @return 响应输出流
	 */
	OutputStream openResponseWriter(int statusCode, String reasonPhrase) throws IOException;

	/**
	 * 快速输出响应
	 * 
	 * @param statusCode
	 *            HTTP状态码
	 * @param content
	 *            内容（可为null），若为RESPONSE_AND_CLOSE则指示无内容响应及关闭连接
	 */
	void response(int statusCode, byte[] content) throws IOException;

	/**
	 * 当前所估算到的传输速率（每秒字节数）
	 */
	int bps();

	/**
	 * 是否（正在/已经）响应
	 */
	boolean isRespond();

	/**
	 * 断开连接
	 */
	void disconnect();

	/** 标识无内容响应后关闭连接 */
	public static final byte[] RESPONSE_AND_CLOSE = new byte[0];
}
