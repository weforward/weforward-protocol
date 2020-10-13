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
package cn.weforward.protocol.datatype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cn.weforward.common.util.StringUtil;

/**
 * 数据类型 - 日期
 * 
 * @author zhangpengji
 *
 */
public interface DtDate extends DtString {

	Date valueDate();

	class Formater {
		static final TimeZone TZ_GMT = TimeZone.getTimeZone("GMT");
		static final SimpleDateFormat DATE_TIME_FORMAT;
		static {
			DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			DATE_TIME_FORMAT.setTimeZone(TZ_GMT);
		}

		public static String formatDateTime(Date date) {
			if (null == date) {
				return null;
			}
			// FIXME 并发时会导致阻塞
			synchronized (DATE_TIME_FORMAT) {
				return DATE_TIME_FORMAT.format(date);
			}
		}

		public static Date parse(String source) throws ParseException {
			if (null == source) {
				return null;
			}
			if (check(source)) {
				synchronized (DATE_TIME_FORMAT) {
					return DATE_TIME_FORMAT.parse(source);
				}
			}
			// return null;
			throw new ParseException("无法转换为Date类型：" + StringUtil.limit(source, 30), 0);
		}
		
		public static boolean check(String source) {
			if(null == source) {
				return false;
			}
			return 24 == source.length() && 'T' == source.charAt(10) && 'Z' == source.charAt(23);
		}
	}
}
