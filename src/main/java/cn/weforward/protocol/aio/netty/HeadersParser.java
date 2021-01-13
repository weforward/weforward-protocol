package cn.weforward.protocol.aio.netty;

import cn.weforward.common.util.StringPool;
import cn.weforward.protocol.aio.http.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;

/**
 * 分析HTTP HEADER
 * 
 * @author liangyi
 *
 */
public class HeadersParser implements ByteProcessor {
	/** 名称池 */
	public static StringPool _NamePool = new StringPool(128);

	protected final AppendableCharSequence m_Line;
	protected final int m_MaxLength;
	protected int m_Size;
	protected CharSequence m_Name;
	protected CharSequence m_Value;

	public HeadersParser(int maxLength) {
		this.m_Line = new AppendableCharSequence(1024);
		m_MaxLength = maxLength;
		// this.headers = new DefaultHttpHeaders(false);
	}

	public void reset() {
		m_Size = 0;
		m_Name = null;
		m_Value = null;
		m_Line.setLength(0);
	}

	@Override
	public boolean process(byte value) throws Exception {
		char nextByte = (char) (value & 0xFF);
		if (nextByte == HttpConstants.CR) {
			return true;
		}
		if (nextByte == HttpConstants.LF) {
			return false;
		}

		if (++m_Size > m_MaxLength) {
			new TooLongFrameException("HTTP header is larger than " + m_MaxLength + " bytes.");
		}
		m_Line.append(nextByte);
		return true;
	}

	private io.netty.handler.codec.http.HttpHeaders openHeaders(
			io.netty.handler.codec.http.HttpHeaders headers) {
		if (EmptyHttpHeaders.INSTANCE == headers) {
			headers = new DefaultHttpHeaders(false);
		}
		return headers;
	}

	public HttpHeaders parse(ByteBuf buffer) {
		AppendableCharSequence line;
		line = parseLine(buffer);
		if (null == line) {
			// 结束
			return HttpHeaders._Empty;
		}
		io.netty.handler.codec.http.HttpHeaders headers = EmptyHttpHeaders.INSTANCE;
		while (line.length() > 0) {
			char firstChar = line.charAt(0);
			if (m_Name != null && (firstChar == ' ' || firstChar == '\t')) {
				String trimmedLine = line.toString().trim();
				String valueStr = String.valueOf(m_Value);
				m_Value = valueStr + ' ' + trimmedLine;
			} else {
				if (m_Name != null) {
					headers = openHeaders(headers);
					headers.add(m_Name, m_Value);
				}
				splitHeader(line);
			}

			line = parseLine(buffer);
			if (null == line) {
				// 结束
				return NettyHttpHeaders.valueOf(headers);
			}
		}
		if (null != m_Name) {
			headers = openHeaders(headers);
			headers.add(m_Name, m_Value);
		}
		m_Name = null;
		m_Value = null;
		return NettyHttpHeaders.valueOf(headers);
	}

	private AppendableCharSequence parseLine(ByteBuf buffer) {
		final int oldSize = m_Size;
		m_Line.reset();
		int i = buffer.forEachByte(this);
		if (i == -1) {
			m_Size = oldSize;
			return null;
		}
		buffer.readerIndex(i + 1);
		return m_Line;
	}

	private void splitHeader(AppendableCharSequence sb) {
		final int length = sb.length();
		int nameStart;
		int nameEnd;
		int colonEnd;
		int valueStart;
		int valueEnd;

		nameStart = findNonWhitespace(sb, 0);
		for (nameEnd = nameStart; nameEnd < length; nameEnd++) {
			char ch = sb.charAt(nameEnd);
			if (ch == ':' || Character.isWhitespace(ch)) {
				break;
			}
		}

		for (colonEnd = nameEnd; colonEnd < length; colonEnd++) {
			if (sb.charAt(colonEnd) == ':') {
				colonEnd++;
				break;
			}
		}

		// name = sb.subStringUnsafe(nameStart, nameEnd);
		m_Name = _NamePool.intern(sb, nameStart, nameEnd);
		valueStart = findNonWhitespace(sb, colonEnd);
		if (valueStart == length) {
			m_Value = "";
		} else {
			valueEnd = findEndOfString(sb);
			m_Value = sb.subStringUnsafe(valueStart, valueEnd);
		}
	}

	private static int findNonWhitespace(AppendableCharSequence sb, int offset) {
		for (int result = offset; result < sb.length(); ++result) {
			if (!Character.isWhitespace(sb.charAtUnsafe(result))) {
				return result;
			}
		}
		return sb.length();
	}

	private static int findEndOfString(AppendableCharSequence sb) {
		for (int result = sb.length() - 1; result > 0; --result) {
			if (!Character.isWhitespace(sb.charAtUnsafe(result))) {
				return result + 1;
			}
		}
		return 0;
	}
}
