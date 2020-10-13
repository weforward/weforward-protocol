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
package cn.weforward.aio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.weforward.common.io.CachedInputStream;
import cn.weforward.common.restful.RestfulRequest;
import cn.weforward.common.restful.RestfulResponse;
import cn.weforward.common.restful.RestfulService;
import cn.weforward.common.sys.Shutdown;
import cn.weforward.common.util.Bytes;
import cn.weforward.common.util.ThreadPool;
import cn.weforward.protocol.aio.http.RestfulServer;
import cn.weforward.protocol.aio.netty.NettyHttpServer;

/**
 * RESTful示例
 * 
 * <pre>
 * curl -i -H 'Content-Type: application/json' -d '@stations.csv' http://127.0.0.1:8080/abc.jspx
 * curl -i -H 'Content-Type: application/json' --data-binary '@stations.csv' http://127.0.0.1:8080/abc.jspx
 * curl --compressed -v -i -H Accept-Encoding:gzip,defalte -H 'Content-Type: application/json' -H 'Authorization: HY-Sign id1234:abcdefg' -d '{"hy_req":{"client_access":"H-12345678-1234"},"invoke":{"p1":"v1","p2":"v2"}}' http://127.0.0.1:8080/test
 * curl -w '\nconnect:%{time_connect}\nstart:%{time_starttransfer}\ntotal:%{time_total}\n' -i -H 'Content-Type: application/json' --data-binary '@stations.csv' http://127.0.0.1:8080/abc.jspx
 * </pre>
 * 
 * 
 * @author liangyi
 *
 */
public class Test_RestfulService implements RestfulService {

	@Override
	public void precheck(RestfulRequest request, RestfulResponse response) throws IOException {
		// if (String.valueOf(request.getHeaders().get("Content-Type"))
		// .indexOf("application/json") < 0) {
		// // Content-Type不匹配
		// response.setStatus(RestfulResponse.STATUS_UNSUPPORTED_MEDIA_TYPE);
		// response.openOutput().close();
		// return;
		// }
	}

	@Override
	public void service(RestfulRequest request, RestfulResponse response) throws IOException {
		InputStream content = request.getContent();
		Bytes bytes = CachedInputStream.toBytes(content);
		System.out.println(bytes);
		response.setStatus(RestfulResponse.STATUS_OK);
		response.setHeader("Content-Type", "application/json");
		OutputStream out = response.openOutput();
		out.write("{\"say\":\"hello world\"}".getBytes("UTF-8"));
		out.close();
	}

	public static void main(String args[]) throws IOException {
		RestfulServer restfulServer = new RestfulServer(new Test_RestfulService());
		restfulServer.setProxyIps("127.0.0.1");
		restfulServer.setAllowIps("127.0.0.1;192.168.0.0-192.168.0.100");
		restfulServer.setQuickHandle(true);
		ThreadPool tp = new ThreadPool(10, "test");
		Shutdown.register(tp);
		restfulServer.setExecutor(tp);

		NettyHttpServer httpServer;
		httpServer = new NettyHttpServer(8080);
		httpServer.setName("ww");
		httpServer.setMaxHttpSize(100 * 1024 * 1024);
		// httpServer.setIdle(30);
		httpServer.setHandlerFactory(restfulServer);
		httpServer.setWebSocket("ws://127.0.0.1");
		httpServer.start();
		System.out.println("'q' key stop");
		while ('q' != System.in.read()) {
		}
		httpServer.close();
		tp.shutdown();
		System.out.println("done.");
	}

	@Override
	public void timeout(RestfulRequest request, RestfulResponse response) throws IOException {
		// TODO Auto-generated method stub
	}

}
