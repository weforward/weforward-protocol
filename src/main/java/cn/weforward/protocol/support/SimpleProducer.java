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
package cn.weforward.protocol.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cn.weforward.common.Dictionary;
import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.util.Bytes;
import cn.weforward.common.util.ListUtil;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.Access;
import cn.weforward.protocol.AccessLoader;
import cn.weforward.protocol.Header;
import cn.weforward.protocol.Request;
import cn.weforward.protocol.RequestConstants;
import cn.weforward.protocol.Response;
import cn.weforward.protocol.ResponseConstants;
import cn.weforward.protocol.aio.http.HttpClient;
import cn.weforward.protocol.aio.http.HttpContext;
import cn.weforward.protocol.aio.http.HttpHeaderHelper;
import cn.weforward.protocol.aio.http.HttpHeaderOutput;
import cn.weforward.protocol.auth.AuthEngine;
import cn.weforward.protocol.auth.AuthExceptionWrap;
import cn.weforward.protocol.auth.AuthInput;
import cn.weforward.protocol.auth.AuthOutput;
import cn.weforward.protocol.auth.Auther;
import cn.weforward.protocol.auth.AutherOutputStream;
import cn.weforward.protocol.datatype.DataType;
import cn.weforward.protocol.datatype.DtList;
import cn.weforward.protocol.datatype.DtNumber;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.datatype.DtString;
import cn.weforward.protocol.exception.AuthException;
import cn.weforward.protocol.exception.SerialException;
import cn.weforward.protocol.exception.WeforwardException;
import cn.weforward.protocol.ext.Producer;
import cn.weforward.protocol.serial.SerialEngine;
import cn.weforward.protocol.serial.Serializer;
import cn.weforward.protocol.support.datatype.SimpleDtList;
import cn.weforward.protocol.support.datatype.SimpleDtNumber;
import cn.weforward.protocol.support.datatype.SimpleDtObject;
import cn.weforward.protocol.support.datatype.SimpleDtString;

/**
 * 数据制作器实现
 * 
 * @author zhangpengji
 *
 */
public class SimpleProducer implements Producer {

	protected AccessLoader m_AccessLoader;
	protected Auther m_Auther;
	protected Serializer m_Serializer;

	static final Comparator<String> COMP_REQ = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			if (RequestConstants.WF_REQ.equals(o1)) {
				return -1;
			}
			if (RequestConstants.WF_REQ.equals(o2)) {
				return 1;
			}
			return 0;
		}
	};

	protected static final Comparator<String> COMP_RESP = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			if (ResponseConstants.WF_RESP.equals(o1)) {
				return -1;
			}
			if (ResponseConstants.WF_RESP.equals(o2)) {
				return 1;
			}
			return 0;
		}
	};

	public SimpleProducer(AccessLoader loader) {
		m_AccessLoader = loader;
		m_Auther = new Auther(loader);
		m_Serializer = new Serializer();
	}

	public void setAuthEngines(List<AuthEngine> authEngines) {
		m_Auther.setEngines(authEngines);
	}

	public void setSerialEngines(List<SerialEngine> serialEngines) {
		m_Serializer.setEngines(serialEngines);
	}

	// public SimpleProducer(Auther auther, Serializer serializer) {
	// m_Auther = auther;
	// m_Serializer = serializer;
	// }

	public Auther getAuther() {
		return m_Auther;
	}

	public Serializer getSerializer() {
		return m_Serializer;
	}

	@Override
	public void make(Request request, OutputStream out)
			throws IOException, SerialException, AuthException {
		BytesOutputStream bos = null;
		ByteArrayInputStream bis = null;
		try {
			Header header = request.getHeader();
			String contentType = header.getContentType();
			String charset = header.getCharset();
			DtObject contentObj = toDtObject(request);
			/* 序列化 */
			bos = new BytesOutputStream(8 * 1024);
			m_Serializer.serial(contentObj, contentType, charset, bos);
			Bytes bytes = bos.getBytes();
			bis = new ByteArrayInputStream(bytes.getBytes(), bytes.getOffset(), bytes.getSize());
			bytes = null;
			/* 生成验证 */
			AuthOutput authOutput = new AuthOutput(header, out);
			AuthInput authInput = new AuthInput(header, bis);
			m_Auther.encode(authInput, authOutput);
		} finally {
			if (null != bos) {
				try {
					bos.close();
				} catch (IOException e) {
				}
			}
			if (null != bis) {
				try {
					bis.close();
				} catch (IOException e) {
				}
			}
		}
	}

	protected DtObject toDtObject(Request request) {
		SimpleDtObject contentObj = new SimpleDtObject(false);
		// 排序属性名，保证先输出wf_req节点
		contentObj.setAttributeComparator(COMP_REQ);
		SimpleDtObject reqObj = new SimpleDtObject(false);
		Access clientAccess = request.getAccess();
		if (null != clientAccess) {
			reqObj.put(RequestConstants.CLIENT_ACCESS,
					SimpleDtString.valueOf(clientAccess.getAccessId()));
			String tenant = clientAccess.getTenant();
			if (!StringUtil.isEmpty(tenant)) {
				reqObj.put(RequestConstants.TENANT, tenant);
			}
			String openid = clientAccess.getOpenid();
			if (!StringUtil.isEmpty(openid)) {
				reqObj.put(RequestConstants.OPENID, openid);
			}
		}
		String addr = request.getAddr();
		if (!StringUtil.isEmpty(addr)) {
			reqObj.put(RequestConstants.CLIENT_ADDR, SimpleDtString.valueOf(addr));
		}
		String version = request.getVersion();
		if (!StringUtil.isEmpty(version)) {
			reqObj.put(RequestConstants.VERSION, SimpleDtString.valueOf(version));
		}
		String resId = request.getResourceId();
		if (!StringUtil.isEmpty(resId)) {
			reqObj.put(RequestConstants.RESOURCE_ID, SimpleDtString.valueOf(resId));
		}
		int resRight = request.getResourceRight();
		if (0 != resRight) {
			reqObj.put(RequestConstants.RESOURCE_RIGHT, SimpleDtNumber.valueOf(resRight));
		}
		String traceToken = request.getTraceToken();
		if (!StringUtil.isEmpty(traceToken)) {
			reqObj.put(RequestConstants.TRACE_TOKEN, traceToken);
		}
		int waitTimeout = request.getWaitTimeout();
		if (0 != waitTimeout) {
			reqObj.put(RequestConstants.WAIT_TIMEOUT, waitTimeout);
		}
		int marks = request.getMarks();
		if (0 != marks) {
			reqObj.put(RequestConstants.MARKS, marks);
		}
		contentObj.put(RequestConstants.WF_REQ, reqObj);
		contentObj.put(RequestConstants.INVOKE, request.getServiceInvoke());
		return contentObj;
	}

	@Override
	public void make(Response response, OutputStream out)
			throws IOException, SerialException, AuthException {
		BytesOutputStream bos = null;
		ByteArrayInputStream bis = null;
		try {
			Header header = response.getHeader();
			String contentType = header.getContentType();
			String charset = header.getCharset();
			DtObject contentObj = toDtObject(response);
			/* 序列化 */
			bos = new BytesOutputStream(8 * 1024);
			m_Serializer.serial(contentObj, contentType, charset, bos);
			Bytes bytes = bos.getBytes();
			bis = new ByteArrayInputStream(bytes.getBytes(), bytes.getOffset(), bytes.getSize());
			bytes = null;

			/* 生成验证 */
			AuthOutput authOutput = new AuthOutput(header, out);
			AuthInput authInput = new AuthInput(header, bis);
			m_Auther.encode(authInput, authOutput);
		} finally {
			if (null != bos) {
				try {
					bos.close();
				} catch (IOException e) {
				}
			}
			if (null != bis) {
				try {
					bis.close();
				} catch (IOException e) {
				}
			}
		}
	}

	protected DtObject toDtObject(Response response) {
		SimpleDtObject contentObj = new SimpleDtObject(false);
		// 排序属性名，保证先输出wf_resp节点
		contentObj.setAttributeComparator(COMP_RESP);
		SimpleDtObject respObj = new SimpleDtObject(false);
		respObj.put(ResponseConstants.WF_CODE, SimpleDtNumber.valueOf(response.getResponseCode()));
		respObj.put(ResponseConstants.WF_MSG, SimpleDtString.valueOf(response.getResponseMsg()));
		String resId = response.getResourceId();
		if (!StringUtil.isEmpty(resId)) {
			respObj.put(ResponseConstants.RESOURCE_ID, SimpleDtString.valueOf(resId));
			respObj.put(ResponseConstants.RESOURCE_EXPIRE,
					SimpleDtNumber.valueOf(response.getResourceExpire()));
			String resService = response.getResourceService();
			if (!StringUtil.isEmpty(resService)) {
				respObj.put(ResponseConstants.RESOURCE_SERVICE, resService);
			}
		}
		String resUrl = response.getResourceUrl();
		if (!StringUtil.isEmpty(resUrl)) {
			respObj.put(ResponseConstants.RESOURCE_URL, SimpleDtString.valueOf(resUrl));
		}
		String forwardTo = response.getForwardTo();
		if (!StringUtil.isEmpty(forwardTo)) {
			respObj.put(ResponseConstants.FORWARD_TO, forwardTo);
		}
		List<String> notifyReceives = response.getNotifyReceives();
		if (!ListUtil.isEmpty(notifyReceives)) {
			SimpleDtList list = new SimpleDtList();
			for (String r : notifyReceives) {
				list.add(r);
			}
			respObj.put(ResponseConstants.NOTIFY_RECEIVES, list);
		}
		int marks = response.getMarks();
		if (0 != marks) {
			respObj.put(ResponseConstants.MARKS, SimpleDtNumber.valueOf(marks));
		}
		contentObj.put(ResponseConstants.WF_RESP, respObj);
		contentObj.put(ResponseConstants.RESULT, response.getServiceResult());
		return contentObj;
	}

	@Override
	public Response fetchResponse(Header header, InputStream in)
			throws IOException, SerialException, AuthException {
		BytesOutputStream bos = null;
		ByteArrayInputStream bis = null;
		try {
			AuthInput authInput = new AuthInput(header, in);
			bos = new BytesOutputStream(8 * 1024);
			AuthOutput authOutput = new AuthOutput(bos);
			m_Auther.decode(authInput, authOutput);
			Bytes bytes = bos.getBytes();
			bis = new ByteArrayInputStream(bytes.getBytes(), bytes.getOffset(), bytes.getSize());
			bytes = null;
			DtObject contentObj = m_Serializer.unserial(bis, header.getContentType(),
					header.getCharset());
			Response response = toResponse(header, contentObj);
			return response;
		} finally {
			if (null != bos) {
				try {
					bos.close();
				} catch (IOException e) {
				}
			}
			if (null != bis) {
				try {
					bis.close();
				} catch (IOException e) {
				}
			}
		}
	}

	protected Response toResponse(Header header, DtObject contentObj) throws SerialException {
		try {
			Response response = new SimpleResponse(header);
			DtObject respObj = contentObj.getObject(ResponseConstants.WF_RESP);
			DtNumber hyCode = (DtNumber) respObj.getNumber(ResponseConstants.WF_CODE);
			response.setResponseCode(hyCode.valueInt());
			DtString hyMsg = (DtString) respObj.getString(ResponseConstants.WF_MSG);
			response.setResponseMsg(hyMsg.value());
			DtString resId = (DtString) respObj.getString(ResponseConstants.RESOURCE_ID);
			if (null != resId) {
				response.setResourceId(resId.value());
			}
			DtNumber resExpire = (DtNumber) respObj.getNumber(ResponseConstants.RESOURCE_EXPIRE);
			if (null != resExpire) {
				response.setResourceExpire(resExpire.valueLong());
			}
			DtString resUrl = (DtString) respObj.getString(ResponseConstants.RESOURCE_URL);
			if (null != resUrl) {
				response.setResourceUrl(resUrl.value());
			}
			DtString resService = (DtString) respObj.getString(ResponseConstants.RESOURCE_SERVICE);
			if (null != resService) {
				response.setResourceService(resService.value());
			}
			DtString forwardTo = (DtString) respObj.getString(ResponseConstants.FORWARD_TO);
			if (null != forwardTo) {
				response.setForwardTo(forwardTo.value());
			}
			DtList notifyReceives = (DtList) respObj.getList(ResponseConstants.NOTIFY_RECEIVES);
			if (null != notifyReceives) {
				List<String> list = new ArrayList<String>(notifyReceives.size());
				for (int i = 0; i < notifyReceives.size(); i++) {
					DtString str = notifyReceives.getItem(i, DataType.STRING);
					list.add(str.value());
				}
				response.setNotifyReceives(list);
			}
			DtNumber marks = (DtNumber) respObj.getNumber(ResponseConstants.MARKS);
			if (null != marks) {
				response.setMarks(marks.valueInt());
			}
			DtObject serviceResult = contentObj.getObject(ResponseConstants.RESULT);
			response.setServiceResult(serviceResult);
			return response;
		} catch (Exception e) {
			throw new SerialException(WeforwardException.CODE_SERIAL_ERROR, "不符合Response标准格式", e);
		}
	}

	@Override
	public Request fetchRequest(Header header, InputStream in)
			throws IOException, SerialException, AuthException {
		BytesOutputStream bos = null;
		ByteArrayInputStream bis = null;
		try {
			AuthInput authInput = new AuthInput(header, in);
			bos = new BytesOutputStream(8 * 1024);
			AuthOutput authOutput = new AuthOutput(bos);
			m_Auther.decode(authInput, authOutput);
			Bytes bytes = bos.getBytes();
			bis = new ByteArrayInputStream(bytes.getBytes(), bytes.getOffset(), bytes.getSize());
			bytes = null;
			DtObject contentObj = m_Serializer.unserial(bis, header.getContentType(),
					header.getCharset());
			Request request = toRequest(header, contentObj);
			return request;
		} finally {
			if (null != bos) {
				try {
					bos.close();
				} catch (IOException e) {
				}
			}
			if (null != bis) {
				try {
					bis.close();
				} catch (IOException e) {
				}
			}
		}
	}

	protected Request toRequest(Header header, DtObject contentObj) throws SerialException {
		try {
			Request request = new SimpleRequest(header);
			// request.setAccess(authOutput.access);
			DtObject reqObj = contentObj.getObject(RequestConstants.WF_REQ);
			if (null != reqObj) {
				DtString clientAccess = reqObj.getString(RequestConstants.CLIENT_ACCESS);
				if (null != clientAccess) {
					SimpleAccess acc = new SimpleAccess();
					acc.setAccessId(clientAccess.value());
					acc.setValid(true);
					DtString tenant = reqObj.getString(RequestConstants.TENANT);
					if (null != tenant) {
						acc.setTenant(tenant.value());
					}
					DtString openid = reqObj.getString(RequestConstants.OPENID);
					if (null != openid) {
						acc.setOpenid(openid.value());
					}
					request.setAccess(acc);
					// request.setClientAccess(clientAccess.value());
				}
				DtString addr = reqObj.getString(RequestConstants.CLIENT_ADDR);
				if (null != addr) {
					request.setAddr(addr.value());
				}
				DtString version = reqObj.getString(RequestConstants.VERSION);
				if (null != version) {
					request.setVersion(version.value());
				}
				DtString resId = reqObj.getString(RequestConstants.RESOURCE_ID);
				if (null != resId) {
					request.setResourceId(resId.value());
				}
				DtNumber resRight = reqObj.getNumber(RequestConstants.RESOURCE_RIGHT);
				if (null != resRight) {
					request.setResourceRight(resRight.valueInt());
				}
				DtString traceToken = reqObj.getString(RequestConstants.TRACE_TOKEN);
				if (null != traceToken) {
					request.setTraceToken(traceToken.value());
				}
				DtNumber waitTimeout = reqObj.getNumber(RequestConstants.WAIT_TIMEOUT);
				if (null != waitTimeout) {
					request.setWaitTimeout(waitTimeout.valueInt());
				}
				DtNumber marks = reqObj.getNumber(RequestConstants.MARKS);
				if (null != marks) {
					request.setMarks(marks.valueInt());
				}
			}
			DtObject serviceInvoke = contentObj.getObject(RequestConstants.INVOKE);
			request.setServiceInvoke(serviceInvoke);
			return request;
		} catch (Exception e) {
			throw new SerialException(WeforwardException.CODE_SERIAL_ERROR, "不符合Request标准格式", e);
		}
	}

	@Override
	public void make(Request request, Output out)
			throws IOException, SerialException, AuthException {
		Header header = request.getHeader();
		AutherOutputStream auther = AutherOutputStream.getInstance(header.getAuthType());
		if (null == auther) {
			throw new AuthException(AuthException.CODE_AUTH_TYPE_INVALID,
					"验证类型无效：" + header.getAuthType());
		}
		auther.init(AutherOutputStream.MODE_ENCODE, m_AccessLoader, false);
		auther.auth(header);
		auther.setTransferTo(out, out.getOutputStream());
		DtObject contentObj = toDtObject(request);
		try {
			m_Serializer.serial(contentObj, header.getContentType(), header.getCharset(), auther);
			auther.finish();
		} catch (AuthExceptionWrap e) {
			throw e.getCause();
		}
	}

	@Override
	public void make(Response response, Output out)
			throws IOException, SerialException, AuthException {
		Header header = response.getHeader();
		AutherOutputStream auther = AutherOutputStream.getInstance(header.getAuthType());
		if (null == auther) {
			throw new AuthException(AuthException.CODE_AUTH_TYPE_INVALID,
					"验证类型无效：" + header.getAuthType());
		}
		auther.init(AutherOutputStream.MODE_ENCODE, m_AccessLoader, false);
		auther.auth(header);
		auther.setTransferTo(out, out.getOutputStream());
		DtObject contentObj = toDtObject(response);
		try {
			m_Serializer.serial(contentObj, header.getContentType(), header.getCharset(), auther);
			auther.finish();
		} catch (AuthExceptionWrap e) {
			throw e.getCause();
		}
	}

	@Override
	public Request fetchRequest(Input in) throws IOException, SerialException, AuthException {
		Header header = in.readHeader();
		DtObject obj = fetch(header, in);
		return toRequest(header, obj);
	}

	@Override
	public Response fetchResponse(Input in) throws IOException, SerialException, AuthException {
		Header header = in.readHeader();
		DtObject obj = fetch(header, in);
		return toResponse(header, obj);
	}

	DtObject fetch(Header header, Input in) throws IOException, SerialException, AuthException {
		// FIXME 实现AutherInputStream，避免数据拷贝
		AutherOutputStream auther = AutherOutputStream.getInstance(header.getAuthType());
		if (null == auther) {
			throw new AuthException(AuthException.CODE_AUTH_TYPE_INVALID,
					"验证类型无效：" + header.getAuthType());
		}
		auther.init(AutherOutputStream.MODE_DECODE, m_AccessLoader, false);
		auther.auth(header);
		BytesOutputStream bos = null;
		ByteArrayInputStream bis = null;
		try {
			// FIXME 这里应该使用RingBuffer之类的环型缓冲区来转接输出->输入流，或者使用pipe启用独立的读写线程并行处理
			bos = new BytesOutputStream();
			auther.setTransferTo(null, bos);
			auther.write(in.getInputStream(), -1);
			auther.finish();
			Bytes bytes = bos.getBytes();
			bis = new ByteArrayInputStream(bytes.getBytes(), bytes.getOffset(), bytes.getSize());
			return m_Serializer.unserial(bis, header.getContentType(), header.getCharset());
		} catch (AuthExceptionWrap e) {
			throw e.getCause();
		} finally {
			if (null != bos) {
				try {
					bos.close();
				} catch (IOException e) {
				}
			}
			if (null != bis) {
				try {
					bis.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static class HttpContextOutput implements Producer.Output {

		HttpContext m_Context;
		OutputStream m_Output;

		public HttpContextOutput(HttpContext ctx, OutputStream output) {
			m_Context = ctx;
			m_Output = output;
		}

		@Override
		public void writeHeader(Header header) throws IOException {
			HttpHeaderHelper.responseHeaders(header, m_Context);
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return m_Output;
		}

		// protected OutputStream openOutput() throws IOException {
		// if (null == m_Output) {
		// synchronized (this) {
		// if (null == m_Output) {
		// m_Output = m_Context.openResponseWriter(HttpConstants.OK, null);
		// }
		// }
		// }
		// return m_Output;
		// }
	}

	public static class HttpContextInput implements Producer.Input {

		HttpContext m_Context;
		String m_ServiceName;
		Header m_Header;

		public HttpContextInput(HttpContext ctx) {
			m_Context = ctx;
			m_ServiceName = HttpHeaderHelper.getServiceName(ctx.getUri());
		}

		public HttpContextInput(HttpContext ctx, String serviceName) {
			m_Context = ctx;
			m_ServiceName = serviceName;
		}

		@Override
		public Header readHeader() throws IOException {
			if (null == m_Header) {
				Header header = new Header(m_ServiceName);
				HttpHeaderHelper.fromHttpHeaders(m_Context.getRequestHeaders(), header);
				m_Header = header;
			}
			return m_Header;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return m_Context.getRequestStream();
		}
	}

	public static class SimpleProducerOutput implements Producer.Output {
		HttpHeaderOutput m_HeaderOutput;
		OutputStream m_Output;

		public SimpleProducerOutput(HttpClient client, OutputStream output) {
			m_HeaderOutput = new HttpHeaderOutput.HttpClientOutput(client);
			m_Output = output;
		}

		public SimpleProducerOutput(HttpURLConnection client, OutputStream output) {
			m_HeaderOutput = new HttpHeaderOutput.HttpURLConnectionOutput(client);
			m_Output = output;
		}

		@Override
		public void writeHeader(Header header) throws IOException {
			HttpHeaderHelper.outHeaders(header, m_HeaderOutput);
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return m_Output;
		}
	}

	public static class SimpleProducerInput implements Producer.Input {

		Dictionary<String, String> m_HttpHeaders;
		InputStream m_Input;
		String m_ServiceName;

		Header m_Header;

		public SimpleProducerInput(Dictionary<String, String> httpHeaders, InputStream input,
				String serviceName) {
			m_HttpHeaders = httpHeaders;
			m_Input = input;
			m_ServiceName = serviceName;
		}

		@Override
		public Header readHeader() throws IOException {
			if (null == m_Header) {
				Header header = new Header(m_ServiceName);
				HttpHeaderHelper.fromHttpHeaders(m_HttpHeaders, header);
				m_Header = header;
			}
			return m_Header;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return m_Input;
		}
	}
}
