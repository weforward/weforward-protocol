package cn.weforward.client;

import org.junit.Test;

import cn.weforward.protocol.Response;
import cn.weforward.protocol.client.ServiceInvoker;
import cn.weforward.protocol.client.ServiceInvokerFactory;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.support.datatype.SimpleDtObject;

public class ClientTest {
	@Test
	public void netty() {
		System.setProperty("cn.weforward.protocol.client.invoker", "netty");
		ServiceInvoker invoker = ServiceInvokerFactory.create(System.getProperty("name"), System.getProperty("apiUrl"),
				System.getProperty("accessId"), System.getProperty("accessKey"));
		DtObject params = new SimpleDtObject();
		Response response = invoker.invoke("/merchant/load", params);
		System.out.println(response.getResponseCode());
		System.out.println(response.getResponseMsg());
		System.out.println(response.getServiceResult());
	}
}
