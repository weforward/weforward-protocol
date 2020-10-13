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
 * HTTP客户端
 * 
 * @author liangyi
 *
 */
public interface HttpClient {
	/**
	 * 设置请求头
	 * 
	 * @param name
	 *            名称
	 * @param value
	 *            值（若为null则移除该项）
	 * @throws IOException
	 */
	void setRequestHeader(String name, String value) throws IOException;

	/**
	 * 发起请求
	 * 
	 * @param url
	 *            要请求的URL
	 * @param method
	 *            方法，如：GET，POST
	 * @throws IOException
	 */
	void request(String url, String method) throws IOException;

	/**
	 * 发起请求
	 * 
	 * @param url
	 *            要请求的URL
	 * @param method
	 *            方法，如：GET，POST
	 * @param timeout
	 *            超时值（毫秒）
	 * @throws IOException
	 */
	void request(String url, String method, int timeout) throws IOException;

	/**
	 * 打开请求输出
	 * 
	 * @throws IOException
	 */
	OutputStream openRequestWriter() throws IOException;

	/**
	 * 响应状态码
	 * 
	 * @throws IOException
	 */
	int getResponseCode() throws IOException;

	/**
	 * 取得响应头
	 * 
	 * @throws IOException
	 */
	HttpHeaders getResponseHeaders() throws IOException;

	/**
	 * 建立传输管道直接输出响应内容
	 * 
	 * @param writer
	 *            输出响应内容的流
	 * @param skipBytes
	 *            >0则跳过已在缓冲区的部分内容
	 */
	void responseTransferTo(OutputStream writer, int skipBytes) throws IOException;

	/**
	 * 获取响应内容流
	 * 
	 * @throws IOException
	 */
	InputStream getResponseStream() throws IOException;

	/**
	 * 副本当前已收到响应的数据（不影响之后的getRequestStream或requestTransferTo）
	 * 
	 * @return 已收到的前部分请求数据的流封装，若已经执行过getRequestStream则返回null
	 * 
	 * @throws IOException
	 */
	public InputStream duplicateResponseStream() throws IOException;

	/**
	 * 响应是否已（接收）完整
	 */
	boolean isResponseCompleted();

	/**
	 * 当前所估算到的传输速率（每秒字节数）
	 */
	int bps();

	/**
	 * 关闭客户端（结束当次调用）
	 */
	void close();

	/**
	 * 断开连接
	 */
	void disconnect();
}
