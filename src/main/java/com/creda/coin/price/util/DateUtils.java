package com.creda.coin.price.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	public static Date stringToDate(String timeString) {
		// 定义时间格式
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

		// 解析时间字符串
		ZonedDateTime zdt = ZonedDateTime.parse(timeString, formatter);

		// 将 ZonedDateTime 转换为 Date
		return Date.from(zdt.toInstant());

	}

	public static Date getCutOffTime() {
		// 定义 2024 年 1 月 1 日 0:00 UTC
		Calendar calendar = Calendar.getInstance();
		calendar.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date cutoffTime = calendar.getTime();
		return cutoffTime;
	}

}
