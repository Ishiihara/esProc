package com.scudata.common;

import java.util.*;

/**
*     ��ν�μ���"[key]=[value]"��ʽ�ļ�ֵ��,���μ�����ָ��һ���ָ����ָ��Ķ����.�ڶ���,
* ��˫���ż�Բ�����ڵķָ�����������,���Ҽ������ظ���Ҫע��,���м���ֵ��Ϊnull����Ϊ�հ��ַ�
* ��ɵĴ�����Ϊ�մ�,������isCaseSensitiveΪfalseʱ������Сд��ʽ���������.
* @see java.util.Map �μ�Map
*/
public final class SegmentSet {

	/**
	 * ���캯��
	 */
	public SegmentSet()  {
		this(null, false, ';', true);
	}

	/**
	 * ���캯��
	 * @param str ���ڷֶε��ַ���,��Ϊnull����һ���յĶμ�
	 */
	public SegmentSet(String str)  {
		this(str, false, ';', true);
	}

	/**
	 * ���캯��
	 * @param str ���ڷֶε��ַ���,��Ϊnull����һ���յĶμ�
	 * @param delim ���ڷֶε��ַ�
	 */
	public SegmentSet(String str, char delim)  {
		this(str, false, delim, true);
	}

	/**
	 * ���캯��
	 * @param keys ���keyֵ,��delim�ָ�
	 * @param values ���valueֵ,��delim�ָ�,��Ҫ��keys��key���Ӧ
	 * @param delim ���ڷֶε��ַ�
	 * @param isCaseSensitive key�Ƿ��Сд����
	 */
	public SegmentSet(String keys, String values, char delim)  {
		this(keys, values, false, delim);
	}

	public SegmentSet(boolean caseSensitive) {
		this(null, caseSensitive, ';', true);
	}

	public SegmentSet(String str, boolean caseSensitive)  {
		this(str, caseSensitive, ';', true);
	}

	public SegmentSet(String str, boolean caseSensitive, char delim)  {
		this(str, caseSensitive, delim, true);
	}

	public SegmentSet(String str, boolean caseSensitive, char delim, boolean trimBlank) {
		this.caseSensitive = caseSensitive;
		this.delim = delim;
		this.trimBlank = trimBlank;
		parseSegmentSet(str);
	}

	public SegmentSet(String keys, String values, boolean caseSensitive, char delim) {
		this(keys, values, caseSensitive, delim, true);
	}

	public SegmentSet(String keys, String values,
		boolean caseSensitive, char delim, boolean trimBlank)  {
		this.caseSensitive = caseSensitive;
		this.delim = delim;
		this.trimBlank = trimBlank;
		ArgumentTokenizer atkey = new ArgumentTokenizer(keys, delim);
		ArgumentTokenizer atval = new ArgumentTokenizer(values, delim);
		while( atkey.hasNext() && atval.hasNext() ) {
			String key = checkKey(atkey.next());
			String value = checkValue(atval.next());
			put(key, value);
		}
	}


	private String checkKey(String key) {
		if (key == null) return "";
		if (trimBlank) key = key.trim();
		if (!caseSensitive)
			key = key.toLowerCase();
		return key;
	}

	private String checkValue(String value) {
		if (value == null) return "";
		if (trimBlank) value = value.trim();
		return value;
	}

	/**
	 * ����μ��е����ж�
	 */
	public void clear()  {
		segs.clear();
	}

	/**
	 * �ڶμ������Ƿ���ָ�����Ķ�
	 * @param key ָ���ļ�
	 * @return �ҵ��򷵻�true,����false
	 */
	public boolean containsKey(String key)  {
		key = checkKey(key);
		return segs.containsKey(key);
	}

	/**
	 * �ڶμ��в����Ƿ���ָ��ֵ�Ķ�
	 * @param key ָ����ֵ
	 * @return �ҵ�����true,����false
	 */
	public boolean containsValue(String value)  {
		value = checkValue(value);
		return segs.containsValue(value);
	}

	/**
	 * ��ȡ���м�ֵ�Եļ���
	 */
	public Set entrySet()  {
		return segs.entrySet();
	}

	/**
	 * �Ƚ�����һ���μ��Ƿ����
	 * @param obj ���ڱȽϵĶ���
	 * @return ��ȷ���true,����false
	 */
	public boolean equals(Object obj)  {
		if (obj == null)  return false;
		return segs.equals(obj);
	}

	/**
	 * ȡָ������Ӧ��ֵ
	 * @param key ָ���ļ�
	 * @return ���ض�Ӧ��ֵ
	 */
	public String get(String key)  {
		key = checkKey(key);
		return (String)segs.get(key);
	}

	/**
	 * ȡ�ñ������HASH��
	 * @return �������HASH��
	 */
	public int hashCode()  {
		return segs.hashCode();
	}

	/**
	 * ����Ƿ��м�ֵ��
	 * @return true��false
	 */
	public boolean isEmpty()  {
		return segs.isEmpty();
	}

	/**
	 * ȡ���м�
	 * @return ���м���
	 */
	public Set keySet()  {
		return segs.keySet();
	}

	/**
	 * ����һ����ֵ��
	 * @param key ��
	 * @param value ֵ
	 * @return ���μ�������ָ���ļ�,�򷵻�����Ӧ�ľ�ֵ,���򷵻�null
	 */
	public String put(String key, String value)  {
		key = checkKey(key);
		value = checkValue(value);
		return (String) segs.put(key, value);
	}

	/**
	 * ��������ֵ��
	 */
	public void putAll(Map t)  {
		if ( t == null || t.isEmpty() )  return ;
		Iterator it = t.keySet().iterator();
		while(it.hasNext())  {
			String key = checkKey((String)it.next());
			String value = checkValue((String)t.get(key));
			segs.put(key, value);
		}
	}

	/**
	 * ɾ��ָ�����Ķ�
	 * @param key ָ���ļ�
	 * @return ��ָ�����ж�Ӧ��ֵ,�򷵻����Ӧֵ,���򷵻�null
	 */
	public String remove(String key)  {
		key = checkKey(key);
		return (String)segs.remove(key);
	}

	/**
	 * ȡ�μ��жεĸ���
	 * @return �μ��жεĸ���
	 */
	public int size()  {
		return segs.size();
	}

	/**
	 * ȡ�μ�������ֵ�ļ���
	 * @return �μ�������ֵ�ļ���
	 */
	public Collection values()  {
		return segs.values();
	}

	/**
	 * �����Էֺŷָ��ļ�ֵ�ԵĴ�
	 * @return �Էֺŷָ��ļ�ֵ�ԵĴ�
	 */
	public String toString()  {
		return toString(";");
	}

	/**
	 * ���μ����Ƿ����ָ���Ķ����
	 * @param keys ��delim�ָ��Ķ����
	 * @param delim �ָ���
	 * @return ���������򷵻�true,���򷵻�false
	 */
	public boolean containsKeys(String keys, char delim)  {
		ArgumentTokenizer at = new ArgumentTokenizer(keys, delim);
		while(at.hasNext())
			if (!containsKey(at.next()))  return false;
		return true;
	}

	/**
	 * ���μ����Ƿ����ָ�����������м�
	 * @param keys ����
	 * @return ��keysΪnull����򷵻�true,���������򷵻�true,���򷵻�false
	 */
	public boolean containsKeys(Set keys)  {
		if (keys == null || keys.isEmpty())  return true;
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			if (!containsKey(key)) return false;
		}
		return true;
	}

	/**
	 * ����delim�ָ��Ķ���������Ӧ�Ķ��ֵ����μ�
	 * @param keys ��delim�ָ��Ķ����
	 * @param values ��delim�ָ��Ķ��ֵ
	 * @param delim �ָ���
	 */
	public void putAll(String keys, String values, char delim)  {
		ArgumentTokenizer atkey = new ArgumentTokenizer(keys, delim);
		ArgumentTokenizer atval = new ArgumentTokenizer(values, delim);
		while( atkey.hasNext() && atval.hasNext() )
			put( checkKey(atkey.next()), checkValue(atval.next()) );
	}

	/**
	 * ����һ���μ������еĶμ���μ�
	 * @param segs Ҫ����Ķμ�
	 */
	public void putAll(SegmentSet segs)  {
		Iterator it = segs.keySet().iterator();
		while( it.hasNext() )  {
			String key = (String)it.next();
			put(key, segs.get(key));
		}
	}

	/**
	 * ȡ��delim�ָ��Ķ������Ӧ�Ķ��ֵ
	 * @param keys ָ���Ķ����
	 * @param valueIfBlank ��ָ���ļ���Ӧ��ֵΪ��ֵ,�򽫷��صĶ�Ӧֵ��Ϊ��
	 * @param delim �ָ���
	 * @return ������delim�ָ��Ķ��ֵ,���в���Ϊnull,�򷵻�null
	 */
	public String getValues(String keys, String valueIfBlank, char delim)  {
		if (keys == null || valueIfBlank == null)  return null;
		StringBuffer values = new StringBuffer(200);
		ArgumentTokenizer atkey = new ArgumentTokenizer(keys, delim);
		while( atkey.hasNext() )  {
			String key = atkey.next();
			String val = get(key);
			if (val.equals(""))  val = valueIfBlank;
			values.append(delim).append(val);
		}
		if (values.length() > 0)  {
			return values.substring(1);
		} else {
			return null;
		}
	}

	/**
	 * ȡ��delim�ָ������м�ֵ��,�����ڷ���SQL����ж�Ӧ��WHERE�Ӿ�,��μ�����������ֵ��,
	 * �ֱ�Ϊ"id1"��"01", "id2"��"1111",�����ʹ��toString(" AND ")����
	 * "id1=01 AND id2=1111"
	 * @param delim �ָ���
	 */
	public String toString(String delim)  {
		StringBuffer str = new StringBuffer(300);
		Iterator it = segs.keySet().iterator();
		while( it.hasNext() )  {
			String key = ((String)it.next()).trim();
			str.append(delim).append(key).append("=").append(get(key));
		}
		if (str.length() > 0)  {
			return str.substring(delim.length());
		} else {
			return null;
		}
	}

	/**
	 * ���μ������ӳ���
	 * @return ����һ��ӳ���
	 */
	public Map toMap()  {
		return (Map)segs.clone();
	}

	private void parseSegmentSet(String str)   {
		if (str == null) return;
		ArgumentTokenizer at = new ArgumentTokenizer(str, delim);
		while( at.hasNext() )  {
			String oneSeg = (String) at.next();
			int pos = oneSeg.indexOf('=');
			String key = oneSeg.substring(0, pos < 0 ? oneSeg.length() : pos);
			String value = pos < 0 ? null : oneSeg.substring(pos + 1);
			segs.put( checkKey(key), checkValue(value) );
		}
	}

	private LinkedHashMap segs = new LinkedHashMap();
	private char delim = ';';
	private boolean caseSensitive = false;
	private boolean trimBlank = true;

	public static void main(String[] args) {
		SegmentSet ss = new SegmentSet("a = 13;;=343;=; Bc = 234;", true, ';', true);
		Iterator it = ss.keySet().iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			System.out.println("[" + key + "=" + ss.get(key) + "]");
		}
	}
}
