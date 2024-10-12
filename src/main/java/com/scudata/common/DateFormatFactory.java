package com.scudata.common;
import java.text.*;
import java.util.*;

public class DateFormatFactory {
	private static String dateFormat = "yyyy-MM-dd";
	private static String timeFormat = "HH:mm:ss";
	private static String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

	private static ThreadLocal<DateFormatFactory> local = new ThreadLocal<DateFormatFactory>() {
		protected synchronized DateFormatFactory initialValue() {
			return new DateFormatFactory();
		}
	};
	
	public static DateFormatFactory get() {
		return (DateFormatFactory) local.get();
	}

	/**
	 * ��ȡʱ���ʽ
	 * @return String ʱ���ʽ�趨
	 */
	public static String getDefaultTimeFormat() {
		return timeFormat;
	}

	/**
	 * ����ʱ���ʽ
	 * @param format String ʱ���ʽ�趨
	 */
	public static void setDefaultTimeFormat(String format) {
		timeFormat = format;
	}

	/**
	 * ��ȡ���ڸ�ʽ
	 * @return String ���ڸ�ʽ�趨
	 */
	public static String getDefaultDateFormat() {
		return dateFormat;
	}

	/**
	 * �������ڸ�ʽ
	 * @param format String ���ڸ�ʽ�趨
	 */
	public static void setDefaultDateFormat(String format) {
		dateFormat = format;
	}

	/**
	 * ��ȡ����ʱ���ʽ
	 * @return String ����ʱ���ʽ�趨
	 */
	public static String getDefaultDateTimeFormat() {
		return dateTimeFormat;
	}

	/**
	 * ��������ʱ���ʽ
	 * @param format String ����ʱ���ʽ�趨
	 */
	public static void setDefaultDateTimeFormat(String format) {
		dateTimeFormat = format;
	}

	private HashMap<String, DateFormat> map = new HashMap<String, DateFormat>();
	private HashMap<String, DateFormatX> xmap = new HashMap<String, DateFormatX>();

	/**
	 * ȡָ����ʽ���ĸ�ʽ����
	 */
	public DateFormat getFormat(String fmt) {
		DateFormat df = map.get(fmt);
		if(df == null) {
			df = new SimpleDateFormat(fmt);
			df.getCalendar().setLenient(false);
			map.put(fmt, df);
		}
		
		return df;
	}

	public DateFormat getFormat(String fmt, String locale) {
		if (locale == null) {
			return getFormat(fmt);
		}
		
		String key = locale + fmt;
		DateFormat df = map.get(key);
		if(df == null) {
			df = new SimpleDateFormat(fmt, Locale.forLanguageTag(locale)); // new Locale(locale)
			df.getCalendar().setLenient(false);
			map.put(key, df);
		}
		
		return df;
	}
	
	/**
	 * ȡϵͳ�趨�����ڸ�ʽ����Ӧ�ĸ�ʽ����
	 */
	public DateFormat getDateFormat() {
		return getFormat(dateFormat);
	}

	/**
	 * ȡϵͳ�趨��ʱ���ʽ����Ӧ�ĸ�ʽ����
	 */
	public DateFormat getTimeFormat() {
		return getFormat(timeFormat);
	}

	/**
	 * ȡϵͳ�趨������ʱ���ʽ����Ӧ�ĸ�ʽ����
	 */
	public DateFormat getDateTimeFormat() {
		return getFormat(dateTimeFormat);
	}

	/**
	 * ȡָ����ʽ���ĸ�ʽ����
	 */
	public DateFormatX getFormatX(String fmt) {
		DateFormatX df = xmap.get(fmt);
		if(df == null) {
			df = new DateFormatX(fmt);
			df.getCalendar().setLenient(false);
			xmap.put(fmt, df);
		}
		
		return df;
	}

	/**
	 * ȡϵͳ�趨�����ڸ�ʽ����Ӧ�ĸ�ʽ����
	 */
	public DateFormatX getDateFormatX() {
		return getFormatX(dateFormat);
	}
	
	/**
	 * ȡϵͳ�趨��ʱ���ʽ����Ӧ�ĸ�ʽ����
	 */
	public DateFormatX getTimeFormatX() {
		return getFormatX(timeFormat);
	}

	/**
	 * ȡϵͳ�趨������ʱ���ʽ����Ӧ�ĸ�ʽ����
	 */
	public DateFormatX getDateTimeFormatX() {
		return getFormatX(dateTimeFormat);
	}
	
	/**
	 * ȡָ����ʽ���ĸ�ʽ����
	 */
	public static DateFormatX newFormatX(String fmt) {
		DateFormatX df = new DateFormatX(fmt);
		df.getCalendar().setLenient(false);
		return df;
	}

	/**
	 * �²���һ��ϵͳ�趨�����ڸ�ʽ����Ӧ�ĸ�ʽ����
	 * @return
	 */
	public static DateFormatX newDateFormatX() {
		return newFormatX(dateFormat);
	}

	/**
	 * �²���һ��ϵͳ�趨������ʱ���ʽ����Ӧ�ĸ�ʽ����
	 * @return
	 */
	public static DateFormatX newDateTimeFormatX() {
		return newFormatX(dateTimeFormat);
	}
	
	/**
	 * �²���һ��ϵͳ�趨��ʱ���ʽ����Ӧ�ĸ�ʽ����
	 */
	public static DateFormatX newTimeFormatX() {
		return newFormatX(timeFormat);
	}
}
