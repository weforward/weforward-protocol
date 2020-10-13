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

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.weforward.protocol.gateway.ServiceRegister;
import cn.weforward.protocol.gateway.exception.ServiceRegisterException;
import cn.weforward.protocol.gateway.http.HttpServiceRegister;
import cn.weforward.protocol.gateway.vo.ServiceVo;

public class TestService {
	Logger logger = LoggerFactory.getLogger(TestService.class);

	int port = 6667;

	String honinyunUrl = "http://127.0.0.1:5661/";
	String accessId = "H-0947f4f50120-0947f4f50120";
	String accessKey = "0abdc16ef73b3a02f31b5cc7b67467cfb02e30fb4a32bc9f2e08d12e80f6ee54";

	ServiceRegister recorder;
	ServiceVo info;

	@Before
	public void init() throws Exception {
		recorder = new HttpServiceRegister(honinyunUrl, accessId, accessKey);

		info = new ServiceVo();
		info.name = "test";
		info.domain = "127.0.0.1";// "192.168.0.32";
		info.port = port;
		info.version = "2.0";
		info.no = "x1000";
		info.documentMethod = "document";
		// ServerVo server = new ServerVo(255, Arrays.asList("/", "/media/daibo/work"));
		// info.server = server;
	}

	@Test
	public void main() throws Exception {
		// com.ourlinc.web.netty.ApiServer server = new ApiServer(port, 10);
		// ServletMapping sm1 = new ServletMapping(new RestfulService(this.rpc),
		// "/test");
		// ServletMapping sm2 = new ServletMapping(new RestfulService(this.stream),
		// "/test/dl/");
		// server.setServlets(Arrays.asList(sm1, sm2));
		// Shutdown.register(server);

		register();

		System.out.println("输入任意字符结束");
		System.in.read();

		unregister();
	}

	public void register() throws ServiceRegisterException {
		recorder.registerService(info);
	}

	public void unregister() throws ServiceRegisterException {
		recorder.unregisterService(info);
	}
}
