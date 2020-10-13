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
package cn.weforward.protocol.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cn.weforward.common.ResultPage;
import cn.weforward.common.util.ClassUtil;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.client.FriendlyServiceInvoker.Option;
import cn.weforward.protocol.client.ServiceInvokerFactory;
import cn.weforward.protocol.client.SimpleFriendlyServiceInvoker;
import cn.weforward.protocol.client.execption.GatewayException;
import cn.weforward.protocol.client.execption.MicroserviceException;
import cn.weforward.protocol.ext.ObjectMapperSet;
import cn.weforward.protocol.support.NamingConverter;

/**
 * 基于jdk的服务调用代理
 * 
 * @author daibo
 *
 */
public class JdkServiceInvokerProxy implements ServiceInvokerProxy {
	/** 调用器 */
	protected SimpleFriendlyServiceInvoker m_Invoker;
	/** 网关异常装箱方法映射表 */
	final static ConcurrentHashMap<String, ValueOf> GATEWAYEXCEPTION_VALUEOF = new ConcurrentHashMap<>();
	/** 微服务异常装箱方法映射表 */
	final static ConcurrentHashMap<String, ValueOf> MICROSERVICEEXCEPTION_VALUEOF = new ConcurrentHashMap<>();

	public JdkServiceInvokerProxy(String serviceName, String preUrl, String accessId, String accessKey) {
		m_Invoker = new SimpleFriendlyServiceInvoker(
				ServiceInvokerFactory.create(serviceName, preUrl, accessId, accessKey));

	}

	/**
	 * 设置方法名
	 * 
	 * @param methodGroup
	 */
	public void setMethodGroup(String methodGroup) {
		m_Invoker.setMethodGroup(methodGroup);
	}

	/**
	 * 设置映射表
	 * 
	 * @param set
	 */
	public void setMapperSet(ObjectMapperSet set) {
		m_Invoker.setMapperSet(set);
	}

	private static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
		}
		if (cl == null) {
			cl = JdkServiceInvokerProxy.class.getClassLoader();
			if (cl == null) {
				try {
					cl = ClassLoader.getSystemClassLoader();
				} catch (Throwable ex) {
				}
			}
		}
		return cl;
	}

	private Class<?> forName(String myInterface) throws ClassNotFoundException {
		return Class.forName(myInterface, true, getDefaultClassLoader());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E newProxy(String myInterface) {
		try {
			return (E) newProxy(forName(myInterface));
		} catch (ClassNotFoundException e) {
			throw new UnsupportedOperationException("找不到" + myInterface + "类");
		}
	}

	@Override
	public <E> E newProxy(Class<E> myInterface) {
		String clazzName = myInterface.getSimpleName();
		String methodGroup;
		if (clazzName.endsWith("Methods")) {
			methodGroup = Character.toLowerCase(clazzName.charAt(0)) + clazzName.substring(1, clazzName.length() - 7);
		} else {
			methodGroup = Character.toLowerCase(clazzName.charAt(0)) + clazzName.substring(1);
		}
		methodGroup += "/";
		return newProxy(methodGroup, myInterface);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E newProxy(String methodGroup, String myInterface) {
		try {
			return (E) newProxy(methodGroup, forName(myInterface));
		} catch (ClassNotFoundException e) {
			throw new UnsupportedOperationException("找不到" + myInterface + "类");
		}
	}

	@Override
	public <E> E newProxy(String methodGroup, Class<E> myInterface) {
		return newProxy(methodGroup, myInterface, getDefaultClassLoader());
	}

	@SuppressWarnings("unchecked")
	public <E> E newProxy(String methodGroup, Class<E> myInterface, ClassLoader classLoader) {
		if (null != methodGroup) {
			methodGroup = NamingConverter.camelToWf(methodGroup);
		}
		InvocationHandler h = new MyInvocationHandler(methodGroup);
		return (E) Proxy.newProxyInstance(classLoader, new Class[] { myInterface }, h);
	}

	/* 异常处理 */
	private Exception onException(Class<?>[] exceptionTypes, MicroserviceException e) throws Throwable {
		return onException(exceptionTypes, e, MICROSERVICEEXCEPTION_VALUEOF);
	}

	/* 异常处理 */
	private Exception onException(Class<?>[] exceptionTypes, GatewayException e) throws Throwable {
		return onException(exceptionTypes, e, GATEWAYEXCEPTION_VALUEOF);
	}

	/* 异常处理 */
	private Exception onException(Class<?>[] exceptionTypes, Exception e, ConcurrentHashMap<String, ValueOf> map)
			throws Throwable {
		if (null != exceptionTypes) {
			for (Class<?> clazz : exceptionTypes) {
				String key = clazz.getName();
				ValueOf value = map.get(key);
				if (null == value) {
					value = createValueOf(clazz, e);
					ValueOf old = map.putIfAbsent(key, value);
					if (null != old) {
						value = old;
					}
				}
				return value.valueOf(e);
			}
		}
		return e;
	}

	/* 创建装箱方法 */
	private ValueOf createValueOf(Class<?> clazz, Exception exception) {
		try {
			Method m = clazz.getMethod("valueOf", exception.getClass());
			int mod = m.getModifiers();
			if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
				return new ValueOf(m);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			// 忽略异常
		}
		return EMPTY_VALUEOF;
	}

	/* 空装箱方法 */
	static final ValueOf EMPTY_VALUEOF = new ValueOf();

	/**
	 * 装箱方法类
	 * 
	 * @author daibo
	 *
	 */
	static class ValueOf {

		protected Method m_Method;

		public ValueOf() {
			m_Method = null;
		}

		public ValueOf(Method m) {
			m_Method = m;
		}

		public Exception valueOf(Exception exception) throws Throwable {
			try {
				return null == m_Method ? exception : (Exception) m_Method.invoke(null, exception);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				return null;// 忽略参数异常
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

	}

	/**
	 * 调用处理类
	 * 
	 * @author daibo
	 *
	 */
	class MyInvocationHandler implements InvocationHandler {

		protected String m_MethodGroup;

		public MyInvocationHandler(String methodGroup) {
			m_MethodGroup = StringUtil.toString(methodGroup);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String name = method.getName();
			String methodName = m_MethodGroup + NamingConverter.camelToWf(name);
			if (null == args || args.length == 0) {
				if (name.equals("toString")) {
					return methodName;
				} else if (name.equals("hashCode")) {
					return methodName.hashCode();
				}
			} else if (args.length == 1) {
				if (name.equals("equals")) {
					return this == args[0];
				}
			}

			Object params;
			Option option;
			if (null == args) {
				params = null;
				option = null;
			} else if (args.length == 1) {
				if (args[0] instanceof Option) {
					option = (Option) args[0];
					params = null;
				} else {
					option = null;
					params = args[0];
				}
			} else if (args.length == 2) {
				params = args[0];
				option = (Option) args[1];
			} else {
				throw new UnsupportedOperationException("格式异常，方法只参数只允许(params),(option),(params,option)三种");
			}
			Class<?> resultClass = method.getReturnType();
			Class<?> componentClass;
			if (List.class.isAssignableFrom(resultClass)) {
				componentClass = ClassUtil.find(method.getGenericReturnType(), 0);
			} else if (ResultPage.class.isAssignableFrom(resultClass)) {
				componentClass = ClassUtil.find(method.getGenericReturnType(), 0);
			} else {
				componentClass = null;
			}
			try {
				return m_Invoker.invoke(methodName, params, resultClass, componentClass, option);
			} catch (GatewayException e) {
				throw onException(method.getExceptionTypes(), e);
			} catch (MicroserviceException e) {
				throw onException(method.getExceptionTypes(), e);
			}
		}

	}

}
