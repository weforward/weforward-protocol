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
package cn.weforward.protocol.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.weforward.protocol.Header;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.Header.HeaderOutput;
import cn.weforward.protocol.exception.AuthException;
import cn.weforward.protocol.exception.SerialException;

/**
 * 数据制作器。对Request、Response的封包与解包
 * 
 * @author zhangpengji
 *
 */
public interface Producer {

	/**
	 * 把请求输出到流，并改变request的header
	 * 
	 * @param request
	 * @param out
	 * @throws IOException
	 * @throws SerialException
	 */
	void make(Request request, OutputStream out) throws IOException, SerialException, AuthException;

	/**
	 * 把响应输出到流，并改变response的header
	 * 
	 * @param response
	 * @param out
	 * @throws IOException
	 * @throws SerialException
	 */
	void make(Response response, OutputStream out) throws IOException, SerialException, AuthException;

	/**
	 * 根据头信息及输入流，提取请求
	 * 
	 * @param header
	 * @param in
	 */
	Request fetchRequest(Header header, InputStream in) throws IOException, SerialException, AuthException;

	/**
	 * 根据头信息及输入流，提取响应
	 * 
	 * @param header
	 * @param in
	 */
	Response fetchResponse(Header header, InputStream in) throws IOException, SerialException, AuthException;

	/**
	 * 把请求转为输出。
	 * <p>
	 * 自动根据Header所指定的AuthType补充其中的Noise，Sign等
	 * 
	 * @param request
	 * @param out
	 * @throws IOException
	 * @throws SerialException
	 * @throws AuthException
	 */
	void make(Request request, Output out) throws IOException, SerialException, AuthException;

	/**
	 * 把响应转为输出。
	 * <p>
	 * 自动根据Header所指定的AuthType补充其中的Noise，Sign等
	 * 
	 * @param response
	 * @param out
	 * @throws IOException
	 * @throws SerialException
	 */
	void make(Response response, Output out) throws IOException, SerialException, AuthException;

	/**
	 * 根据输入内容，提取请求
	 * 
	 * @param in
	 */
	Request fetchRequest(Input in) throws IOException, SerialException, AuthException;

	/**
	 * 根据输入内容，提取响应
	 * 
	 * @param in
	 */
	Response fetchResponse(Input in) throws IOException, SerialException, AuthException;

	interface Output extends HeaderOutput {

		/**
		 * 获取输出流。
		 * <p>
		 * 调用方不负责close
		 * 
		 * @throws IOException
		 */
		OutputStream getOutputStream() throws IOException;
	}

	interface Input {

		Header readHeader() throws IOException;

		/**
		 * 获取输入流。
		 * <p>
		 * 调用方不负责close
		 * 
		 * @throws IOException
		 */
		InputStream getInputStream() throws IOException;
	}
}
