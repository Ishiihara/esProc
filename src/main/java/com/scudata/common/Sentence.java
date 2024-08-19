package com.scudata.common;

import java.io.*;

/**
 * ����ʹ��ת���ַ�ʱ��ע�ⲻ��ʹ�õ����š�˫���š�Բ���š������źʹ����š� ����֧��ת���ַ�\����\������������Ų������ã���������
 */

public final class Sentence {
	/**
	 * ����ʱ���Դ�Сд
	 */
	public static final int IGNORE_CASE = 1;
	/**
	 * ����ʱ����Բ����,��Ҫɨ������������
	 */
	public static final int IGNORE_PARS = 2;
	/**
	 * ����ʱ�������ҵ�һ��ƥ�䴮
	 */
	public static final int ONLY_FIRST = 4;
	/**
	 * ����ʱ������ƥ�䴮�Ǳ�ʶ��ʱ�ɹ�
	 */
	public static final int ONLY_PHRASE = 8;
	/**
	 * ����ʱ��������,��Ҫɨ������������
	 */
	public static final int IGNORE_QUOTE = 16;
	/**
	 * ָʾɾ���ַ����пհ��ַ�ʱ����������ַ���д
	 */
	public static final int UPPER_WHEN_TRIM = 16;
	/**
	 * ָʾɾ���ַ����пհ��ַ�ʱ����������ַ�Сд
	 */
	public static final int LOWER_WHEN_TRIM = 32;

	private final static boolean LOG = false;

	public static void log(Object o) {
		if (LOG)
			System.out.println(o);
	}

	/**
	 * ���캯��
	 * 
	 * @param str
	 *            ��Ҫ���о䷨�������ַ���
	 */
	public Sentence(String str) {
		this.str = str;
	}

	/**
	 * ���캯��
	 * 
	 * @param str
	 *            ��Ҫ���о䷨�������ַ���
	 * @param escapeChar
	 *            ת���ַ�
	 */
	public Sentence(String str, char escapeChar) {
		this.str = str;
		this.escapeChar = escapeChar;
	}

	/**
	 * ������ʶ��
	 * 
	 * @param str
	 *            ��Ҫ������ʶ��ԭ��
	 * @param start
	 *            ��������ʼλ��
	 * @return ���ر�ʶ�����һ���ַ���ԭ���е�λ��
	 */
	public static int scanIdentifier(String str, int start) {
		int len = str.length();
		char ch = str.charAt(start);
		if (!Character.isJavaIdentifierStart(ch))
			return -1;
		int i = start + 1;
		while (i < len) {
			ch = str.charAt(i);
			if (ch == (char) 0)
				break;
			if (ch == (char) 1)
				break;
			if (!Character.isJavaIdentifierPart(ch))
				break;
			i++;
		}
		return i - 1;
	}

	public static boolean checkIdentifier(String ident) {
		if (ident == null)
			return false;
		int len = ident.length();
		if (len == 0)
			return false;
		char c = ident.charAt(0);
		if (!Character.isJavaIdentifierStart(c))
			return false;

		for (int i = 1; i < len; i++) {
			c = ident.charAt(i);
			if (c == (char) 0)
				return false;
			if (c == (char) 1)
				return false;
			if (!Character.isJavaIdentifierPart(c))
				return false;
		}
		return true;
	}

	/**
	 * ������һ��ƥ�������'��"
	 * 
	 * @param str
	 *            ��Ҫ�������ŵ�ԭ��
	 * @param start
	 *            ��ʼλ��,��ͷһ������ԭ���е�λ��
	 * @param escapeChar
	 *            ת���ַ�
	 * @return ���ҵ�,�򷵻�ƥ���������ԭ���е�λ��,���򷵻�-1
	 */
	public static int scanQuotation(String str, int start, char escapeChar) {
		char quote = str.charAt(start);
		if (quote != '\"' && quote != '\'')
			return -1;
		int idx = start + 1, len = str.length();
		while (idx < len) {
			/*
			 * idx = str.indexOf(quote, idx); if (idx < 0) break; if (
			 * str.charAt(idx - 1) != escapeChar) return idx; idx++;
			 */
			char ch = str.charAt(idx);
			if (ch == escapeChar)
				idx += 2;
			else if (ch == quote)
				return idx;
			else
				idx++;
		}
		return -1;
	}

	/**
	 * ������һ��ƥ�������'��",ȱʡת���ַ�Ϊ\
	 * 
	 * @param str
	 *            ��Ҫ�������ŵ�ԭ��
	 * @param start
	 *            ��ʼλ��,��ͷһ������ԭ���е�λ��
	 * @return ���ҵ�,�򷵻�ƥ���������ԭ���е�λ��,���򷵻�-1
	 */
	public static int scanQuotation(String str, int start) {
		return scanQuotation(str, start, '\\');
	}

	/**
	 * ������һ��ƥ���Բ���ţ��������ڵ�Բ���ű�����
	 * 
	 * @param str
	 *            ��Ҫ�������ŵ�ԭ��
	 * @param start
	 *            ��ʼλ��,����Բ����(��ԭ���е�λ��
	 * @param escapeChar
	 *            ת���ַ�
	 * @return ���ҵ�,�򷵻�ƥ�����Բ������ԭ���е�λ��,���򷵻�-1
	 */
	public static int scanParenthesis(String str, int start, char escapeChar) {
		if (str.charAt(start) != '(')
			return -1;

		int len = str.length();
		for (int i = start + 1; i < len;) {
			char ch = str.charAt(i);
			switch (ch) {
			case '(':
				i = scanParenthesis(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				break;
			case '\"':
			case '\'':
				int q = scanQuotation(str, i, escapeChar);
				if (q < 0) {
					i++;
				} else {
					i = q + 1;
				}
				break;
			case '[': // $[str]
				if (i > start && str.charAt(i - 1) == '$') {
					q = scanBracket(str, i, escapeChar);
					if (q < 0) {
						i++;
					} else {
						i = q + 1;
					}
				} else {
					i++;
				}
				break;
			case ')':
				return i;
			default:
				if (ch == escapeChar)
					i++;
				i++;
				break;
			}
		}
		return -1;
	}

	/**
	 * ������һ��ƥ���Բ����,ȱʡת���ַ�Ϊ\���������ڵ�Բ���ű�����
	 * 
	 * @param str
	 *            ��Ҫ�������ŵ�ԭ��
	 * @param start
	 *            ��ʼλ��,����Բ����(��ԭ���е�λ��
	 * @return ���ҵ�,�򷵻�ƥ�����Բ������ԭ���е�λ��,���򷵻�-1
	 */
	public static int scanParenthesis(String str, int start) {
		return scanParenthesis(str, start, '\\');
	}

	/**
	 * ������һ��ƥ��������ţ��������ڵ������ű�����
	 * 
	 * @param str
	 *            ��Ҫ�������ŵ�ԭ��
	 * @param start
	 *            ��ʼλ��,������������ԭ���е�λ��
	 * @param escapeChar
	 *            ת���ַ�
	 * @return ���ҵ�,�򷵻�ƥ�������������ԭ���е�λ��,���򷵻�-1
	 */
	public static int scanBracket(String str, int start, char escapeChar) {
		if (str.charAt(start) != '[')
			return -1;

		int len = str.length();
		for (int i = start + 1; i < len;) {
			char ch = str.charAt(i);
			switch (ch) {
			case '[':
				i = scanBracket(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				break;
			case '\"':
			case '\'':
				int q = scanQuotation(str, i, escapeChar);
				if (q < 0) {
					i++;
				} else {
					i = q + 1;
				}
				break;
			case ']':
				return i;
			default:
				if (ch == escapeChar)
					i++;
				i++;
				break;
			}
		}
		return -1;
	}

	/**
	 * ������һ��ƥ���������,ȱʡת���ַ�Ϊ\���������ڵ������ű�����
	 * 
	 * @param str
	 *            ��Ҫ�������ŵ�ԭ��
	 * @param start
	 *            ��ʼλ��,������������ԭ���е�λ��
	 * @return ���ҵ�,�򷵻�ƥ�������������ԭ���е�λ��,���򷵻�-1
	 */
	public static int scanBracket(String str, int start) {
		return scanBracket(str, start, '\\');
	}

	/**
	 * ������һ��ƥ��Ļ����ţ��������ڵĻ����ű�����
	 * 
	 * @param str
	 *            ��Ҫ�������ŵ�ԭ��
	 * @param start
	 *            ��ʼλ��,����������ԭ���е�λ��
	 * @param escapeChar
	 *            ת���ַ�
	 * @return ���ҵ�,�򷵻�ƥ����һ�������ԭ���е�λ��,���򷵻�-1
	 */
	public static int scanBrace(String str, int start, char escapeChar) {
		if (str.charAt(start) != '{')
			return -1;

		int len = str.length();
		for (int i = start + 1; i < len;) {
			char ch = str.charAt(i);
			switch (ch) {
			case '{':
				i = scanBrace(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				break;
			case '\"':
			case '\'':
				int q = scanQuotation(str, i, escapeChar);
				if (q < 0) {
					i++;
				} else {
					i = q + 1;
				}
				break;
			case '}':
				return i;
			default:
				if (ch == escapeChar)
					i++;
				i++;
				break;
			}
		}
		return -1;
	}

	/**
	 * ������һ��ƥ��Ļ�����,ȱʡת���ַ�Ϊ\���������ڵĻ����ű�����
	 * 
	 * @param str
	 *            ��Ҫ�������ŵ�ԭ��
	 * @param start
	 *            ��ʼλ��,��������(��ԭ���е�λ��
	 * @return ���ҵ�,�򷵻�ƥ����һ�������ԭ���е�λ��,���򷵻�-1
	 */
	public static int scanBrace(String str, int start) {
		return scanBrace(str, start, '\\');
	}

	/**
	 * ɨ����������ƥ�䣬����������������<
	 * @param str
	 * @param start
	 * @return
	 */
	public static int scanChineseBracket(String str, int start) {
		char cb = str.charAt(start);
		char matchChar;
		if (cb == '��') {
			matchChar = '��';
		} else if (cb == '��') {
			matchChar = '��';
		} else if (cb == '��') {
			matchChar = '��';
		} else {
			matchChar = '>';
		}

		int len = str.length();
		for (int i = start + 1; i < len;) {
			char c = str.charAt(i);
			if (c == matchChar) {
				return i;
			} else if (c == '"' || c == '\'') {
				int q = scanQuotation(str, i, '\\');
				if (q < 0) {
					i++;
				} else {
					i = q + 1;
				}
			} else if (c == cb) {
				i = scanChineseBracket(str, i);
				if (i < 0)
					return -1;
				i++;
			} else if (c == '\\') {
				i += 2;
			} else {
				i++;
			}
		}
		
		return -1;
	}
	
	/**
	 * ��ԭ���еĿհ��ַ�ɾ��,������ifcase������ԭ���еķ��������ַ���д,Сд�򲻶�
	 * 
	 * @param str
	 *            ��Ҫɾ���հ��ַ���ԭ��
	 * @param ifcase
	 *            ����ʹ��0, UPPER_WHEN_TRIM, LOWER_WHEN_TRIM,
	 *            UPPER_WHEN_TRIM+LOWER_WHEN_TRIM
	 * @param escapeChar
	 *            ת���ַ�
	 * @return ɾ���հ��ַ���Ĵ�
	 */
	public static String trim(String str, int ifcase, char escapeChar) {
		int idx = 0, len = str.length();
		int flag = 0;
		// 0 - ignore following whitespace
		// 1 - identifier char
		// 2 - whitespace
		StringBuffer dst = new StringBuffer(len);
		while (idx < len) {
			char ch = str.charAt(idx);
			if ((ch == '\"' || ch == '\'')
					&& ((idx > 0 && str.charAt(idx - 1) != escapeChar) || idx == 0)) {
				if (flag == 2)
					dst.append(' ');
				int i = scanQuotation(str, idx, escapeChar);
				if (i < 0)
					throw new RuntimeException("δ�ҵ�λ��" + idx + "����Ӧ������");
				i++;
				for (int j = idx; j < i; j++)
					dst.append(str.charAt(j));
				idx = i;
				continue;
			} else if (Character.isWhitespace(ch)) {
				do {
					idx++;
				} while (idx < len && Character.isWhitespace(str.charAt(idx)));
				if (flag > 0)
					flag = 2;
				continue;
			} else if (isWordChar(ch)) {
				if (flag == 2)
					dst.append(' ');
				flag = 1;
			} else {
				flag = 0;
			}
			switch (ifcase) {
			case UPPER_WHEN_TRIM:
				dst.append(Character.toUpperCase(ch));
				break;
			case LOWER_WHEN_TRIM:
				dst.append(Character.toLowerCase(ch));
				break;
			default:
				dst.append(ch);
			}
			idx++;
		}

		return dst.toString();
	}

	/**
	 * ��ԭ���еĿհ��ַ�ɾ��,������ifcase������ԭ���еķ��������ַ���д,Сд�򲻶�,ȱʡת���ַ�Ϊ\
	 * 
	 * @param str
	 *            ��Ҫɾ���հ��ַ���ԭ��
	 * @param ifcase
	 *            ����Ϊ0, UPPER_WHEN_TRIM, LOWER_WHEN_TRIM,
	 *            UPPER_WHEN_TRIM+LOWER_WHEN_TRIM
	 * @return ɾ���հ��ַ���Ĵ�
	 */
	public static String trim(String str, int ifcase) {
		return trim(str, ifcase, '\\');
	}

	/**
	 * ��ԭ���������������������������
	 * 
	 * @param str
	 *            ��Ҫ���������ԭ��
	 * @param phrase
	 *            ��Ҫ�����Ķ���
	 * @param start
	 *            ��ԭ���е���ʼλ��
	 * @param flag
	 *            ����Ϊ0,IGNORE_CASE,IGNORE_PARS,IGNORE_CASE+IGNORE_PARS
	 * @param escapeChar
	 *            ת���ַ�
	 * @return ���ҵ�,�򷵻ض�����ԭ���е�λ��,���򷵻�-1
	 */
	public static int phraseAt(String str, String phrase, int start, int flag,
			char escapeChar) {
		int slen = str.length(), plen = phrase.length();
		boolean iswordchar = false;
		for (int i = start; i < slen;) {
			char ch = str.charAt(i);
			if ((ch == '\"' || ch == '\'')
					&& ((i > 0 && str.charAt(i - 1) != '\\') || i == 0)) {
				i = scanQuotation(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				iswordchar = false;
				continue;
			}
			if ((flag & IGNORE_PARS) == 0 && ch == '(') {
				i = scanParenthesis(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				iswordchar = false;
				continue;
			}
			if (!iswordchar) {
				if (phrase.regionMatches((flag & IGNORE_CASE) > 0, 0, str, i,
						plen)) {
					if ((i + plen) >= slen || !isWordChar(str.charAt(i + plen)))
						return i;
				}
			}
			iswordchar = isWordChar(ch);
			i++;
		}
		return -1;
	}

	/**
	 * ��ԭ������������,ȱʡת���ַ�Ϊ\
	 * 
	 * @param str
	 *            ��Ҫ���������ԭ��
	 * @param phrase
	 *            ��Ҫ�����Ķ���
	 * @param start
	 *            ��ԭ���е���ʼλ��
	 * @param flag
	 *            ����Ϊ0,IGNORE_CASE,IGNORE_PARS����ӷ����
	 * @return ���ҵ�,�򷵻ض�����ԭ���е�λ��,���򷵻�-1
	 */
	public static int phraseAt(String str, String phrase, int start, int flag) {
		return phraseAt(str, phrase, start, flag, '\\');
	}

	/**
	 * �滻ԭ���е��Ӵ�
	 * 
	 * @param str
	 *            ��Ҫ�滻��ԭ��
	 * @param start
	 *            ��ԭ����ʼ�滻����ʼλ��
	 * @param sold
	 *            ��Ҫ�滻���Ӵ�
	 * @param snew
	 *            �滻��
	 * @param flag
	 *            ��Ϊ0,IGNORE_CASE,IGNORE_PARS,IGNORE_QUOTE,ONLY_FIRST,
	 *            ONLY_PHRASE����ӷ����
	 * @param escapeChar
	 *            ת���ַ�
	 * @return �滻��Ĵ�
	 */
	public static String replace(String str, int start, String sold,
			String snew, int flag, char escapeChar) {
		int strlen = str.length(), len = sold.length();
		StringBuffer dst = null;
		char preChar = '*'; // ָʾƥ�䴮ǰһ���ַ�������Ч��ʶ���ַ�
		int i = start;
		while (i < strlen) {
			char ch = str.charAt(i);
			if ((ch == '\'' || ch == '\"') && (flag & IGNORE_QUOTE) == 0
					&& (i == 0 || (i > 0 && str.charAt(i - 1) != escapeChar))) {
				int idx = scanQuotation(str, i, escapeChar);
				if (idx < 0)
					throw new RuntimeException("δ�ҵ�λ��" + i + "����Ӧ������");
				idx++;
				if (dst != null)
					for (int j = i; j < idx; j++)
						dst.append(str.charAt(j));
				i = idx;
				preChar = '*';
				continue;
			}
			if (((flag & IGNORE_PARS) == 0) && (ch == '(')) {
				int idx = scanParenthesis(str, i, escapeChar);
				if (idx < 0)
					throw new RuntimeException("δ�ҵ�λ��" + i + "����Ӧ��Բ����");
				idx++;
				if (dst != null)
					for (int j = i; j < idx; j++)
						dst.append(str.charAt(j));
				i = idx;
				preChar = '*';
				continue;
			}
			boolean lb;
			lb = sold.regionMatches((flag & IGNORE_CASE) > 0, 0, str, i, len);
			if (lb && (flag & ONLY_PHRASE) > 0) {
				// ��ƥ�䴮�ĵ�һ�ַ���ǰһ�ַ���һ�����Ǳ�ʶ���ַ�ʱΪ��
				lb = !isWordChar(sold.charAt(0)) || !isWordChar(preChar);
				// ��ƥ�䴮������ַ�����һ�ַ���һ�����Ǳ�ʶ���ַ�ʱ
				if ((i + len) < strlen)
					lb = lb
							&& (!isWordChar(sold.charAt(len - 1)) || !isWordChar(str
									.charAt(i + len)));
			}
			if (lb) {
				if (dst == null) {
					dst = new StringBuffer(strlen << 2);
					for (int j = 0; j < i; j++)
						dst.append(str.charAt(j));
				}
				dst.append(snew);
				i += len;
				preChar = str.charAt(i - 1);
				if ((flag & ONLY_FIRST) > 0) {
					while (i < strlen)
						dst.append(str.charAt(i++));
					break;
				}
			} else {
				if (dst != null)
					dst.append(ch);
				preChar = ch;
				i++;
			}

		} // while(i<strlen)
		return (dst == null) ? str : dst.toString();
	}

	/**
	 * �滻ԭ���е��Ӵ�,ȱʡת���ַ�Ϊ\
	 * 
	 * @param str
	 *            ��Ҫ�滻��ԭ��
	 * @param start
	 *            ��ԭ����ʼ�滻����ʼλ��
	 * @param sold
	 *            ��Ҫ�滻���Ӵ�
	 * @param snew
	 *            �滻��
	 * @param flag
	 *            ��Ϊ0,IGNORE_CASE,IGNORE_PARS,ONLY_FIRST,ONLY_PHRASE����ӷ����
	 * @return �滻��Ĵ�
	 */
	public static String replace(String str, int start, String sold,
			String snew, int flag) {
		return replace(str, start, sold, snew, flag, '\\');
	}

	/**
	 * �滻ԭ���е��Ӵ�,ȱʡת���ַ�Ϊ\
	 * 
	 * @param str
	 *            ��Ҫ�滻��ԭ��
	 * @param sold
	 *            ��Ҫ�滻���Ӵ�
	 * @param snew
	 *            �滻��
	 * @param flag
	 *            ��Ϊ0,IGNORE_CASE,IGNORE_PARS,ONLY_FIRST,ONLY_PHRASE����ӷ����
	 * @return �滻��Ĵ�
	 */
	public static String replace(String str, String sold, String snew, int flag) {
		return replace(str, 0, sold, snew, flag, '\\');
	}

	/**
	 * ������һ��ƥ�������'��"
	 * 
	 * @param start
	 *            ��ʼλ��,��ͷһ������ԭ���е�λ��
	 * @return ��һ��ƥ���������ԭ���е�λ��
	 */
	public int scanQuotation(int start) {
		return scanQuotation(this.str, start, escapeChar);
	}

	/**
	 * ��ԭ���еĿհ��ַ�ɾ��,������ifcase������ԭ���еķ��������ַ���д,Сд�򲻶��� ��getSentence()���صĴ�����Ϊ�����
	 * 
	 * @param ifcase
	 *            ����Ϊ0, UPPER_WHEN_TRIM, LOWER_WHEN_TRIM����ӷ����
	 * @return ɾ���հ��ַ���Ĵ�
	 */
	public void trim(int ifcase) {
		this.str = trim(this.str, ifcase, escapeChar);
	}

	/**
	 * ��ԭ������������
	 * 
	 * @param phrase
	 *            ��Ҫ�����Ķ���
	 * @param start
	 *            ��ԭ���е���ʼλ��
	 * @param flag
	 *            ����Ϊ0,IGNORE_CASE,IGNORE_PARS,IGNORE_CASE+IGNORE_PARS
	 * @return ������ԭ���е�λ��
	 */
	public int phraseAt(String phrase, int start, int flag) {
		return phraseAt(this.str, phrase, start, flag, escapeChar);
	}

	/**
	 * �滻ԭ���е��Ӵ�������getSentence()���ص��ַ�������Ϊ�����
	 * 
	 * @param start
	 *            ��ԭ����ʼ�滻����ʼλ��
	 * @param sold
	 *            ��Ҫ�滻���Ӵ�
	 * @param snew
	 *            �滻��
	 * @param flag
	 *            ��Ϊ0,IGNORE_CASE,IGNORE_PARS,ONLY_FIRST,ONLY_PHRASE����ӷ����
	 * @return ���滻�Ӵ��ĸ���
	 */
	public int replace(int start, String sold, String snew, int flag) {
		int strlen = str.length(), len = sold.length(), count = 0;
		StringBuffer dst = null;
		char preChar = '*'; // ָʾƥ�䴮ǰһ���ַ�������Ч��ʶ���ַ�
		int i = start;
		while (i < strlen) {
			char ch = str.charAt(i);
			if ((ch == '\"' || ch == '\'')
					&& ((i > 0 && str.charAt(i - 1) != '\\') || i == 0)) {
				int idx = scanQuotation(str, i, escapeChar);
				if (idx < 0)
					return 0;
				idx++;
				if (dst != null)
					for (int j = i; j < idx; j++)
						dst.append(str.charAt(j));
				i = idx;
				preChar = '*';
				continue;
			}
			if (((flag & IGNORE_PARS) == 0) && (ch == '(')) {
				int idx = scanParenthesis(str, i, escapeChar);
				if (idx < 0)
					return 0;
				idx++;
				if (dst != null)
					for (int j = i; j < idx; j++)
						dst.append(str.charAt(j));
				i = idx;
				preChar = '*';
				continue;
			}

			boolean lb;
			lb = sold.regionMatches((flag & IGNORE_CASE) > 0, 0, str, i, len);
			if (lb && (flag & ONLY_PHRASE) > 0) {
				// ��ƥ�䴮�ĵ�һ�ַ���ǰһ�ַ���һ�����Ǳ�ʶ���ַ�ʱΪ��
				lb = !isWordChar(sold.charAt(0)) || !isWordChar(preChar);
				// ��ƥ�䴮������ַ�����һ�ַ���һ�����Ǳ�ʶ���ַ�ʱ
				if ((i + len) < strlen)
					lb = lb
							&& (!isWordChar(sold.charAt(len - 1)) || !isWordChar(str
									.charAt(i + len)));
			}
			if (lb) {
				if (dst == null) {
					dst = new StringBuffer(strlen << 2);
					for (int j = 0; j < i; j++)
						dst.append(str.charAt(j));
				}
				dst.append(snew);
				i += len;
				preChar = str.charAt(i - 1);
				count++;
				if ((flag & ONLY_FIRST) > 0) {
					while (i < strlen)
						dst.append(str.charAt(i++));
					break;
				}
			} else {
				if (dst != null)
					dst.append(ch);
				i++;
			}
		}
		if (dst != null)
			str = dst.toString();
		return count;
	}

	/**
	 * ȡ������Ĵ�
	 * 
	 * @return ������Ĵ�
	 */
	public String toString() {
		return this.str;
	}

	/**
	 * ȡ������Ĵ�
	 * 
	 * @return ������Ĵ�
	 */
	public String getSentence() {
		return this.str;
	}

	/**
	 * ��ԭ���������ַ���(���Ա������ж�����)
	 * 
	 * @param phrase
	 *            ��Ҫ�������ַ���
	 * @param start
	 *            ��ԭ���е���ʼλ��
	 * @param flag
	 *            ����Ϊ0,IGNORE_CASE,IGNORE_PARS,IGNORE_CASE+IGNORE_PARS
	 * @return �ַ�����ԭ���е�λ��
	 */
	public static int indexOf(String str, String find, int start, int flag,
			char escapeChar) {
		int slen = str.length(), plen = find.length();
		for (int i = start; i < slen;) {
			char ch = str.charAt(i);
			if ((flag & IGNORE_QUOTE) == 0
					&& // xq add,����֧������������ 2010.8.25
					(ch == '\"' || ch == '\'')
					&& ((i > 0 && str.charAt(i - 1) != '\\') || i == 0)) {
				i = scanQuotation(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				continue;
			}
			if ((flag & IGNORE_PARS) == 0 && ch == '(') {
				i = scanParenthesis(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				continue;
			}
			if (find.regionMatches((flag & IGNORE_CASE) > 0, 0, str, i, plen)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public int indexOf(String find, int start, int flag) {
		return indexOf(this.str, find, start, flag, escapeChar);
	}

	public static int indexOf(String str, String find, int start, int flag) {
		return indexOf(str, find, start, flag, '\\');
	}

	/**
	 * ����������ר�ã�Ϊ��֧��ONLY_PHRASE��wunan�� ��ԭ���������ַ���(���Ա������ж�����)
	 * 
	 * @param phrase
	 *            ��Ҫ�������ַ���
	 * @param start
	 *            ��ԭ���е���ʼλ��
	 * @param flag
	 *            ����Ϊ0,IGNORE_CASE,IGNORE_PARS,IGNORE_CASE+IGNORE_PARS
	 * @return �ַ�����ԭ���е�λ��
	 */
	public static int indexOf(String str, int start, String find, int flag) {
		char escapeChar = '\\';
		int slen = str.length(), plen = find.length();
		char preChar = '*';
		for (int i = start; i < slen;) {
			char ch = str.charAt(i);
			if ((flag & IGNORE_QUOTE) == 0
					&& // ����֧������������ 2018.10.9 wunan
					(ch == '\"' || ch == '\'')
					&& ((i > 0 && str.charAt(i - 1) != '\\') || i == 0)) {
				i = scanQuotation(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				preChar = ch;
				continue;
			}
			if ((flag & IGNORE_PARS) == 0 && (ch == '\"' || ch == '\'')
					&& ((i > 0 && str.charAt(i - 1) != '\\') || i == 0)) {
				i = scanQuotation(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				preChar = ch;
				continue;
			}
			if ((flag & IGNORE_PARS) == 0 && ch == '(') {
				i = scanParenthesis(str, i, escapeChar);
				if (i < 0)
					return -1;
				i++;
				preChar = ch;
				continue;
			}
			if (find.regionMatches((flag & IGNORE_CASE) > 0, 0, str, i, plen)) {
				if ((flag & ONLY_PHRASE) > 0) {
					// ��ƥ�䴮�ĵ�һ�ַ���ǰһ�ַ���һ�����Ǳ�ʶ���ַ�ʱΪ��
					boolean lb = !isWordChar(find.charAt(0))
							|| !isWordChar(preChar);
					// ��ƥ�䴮������ַ�����һ�ַ���һ�����Ǳ�ʶ���ַ�ʱ
					if ((i + plen) < slen)
						lb = lb
								&& (!isWordChar(find.charAt(plen - 1)) || !isWordChar(str
										.charAt(i + plen)));
					if (!lb) {
						preChar = ch;
						i++;
						continue;
					}
				}
				return i;
			}
			i++;
			preChar = ch;
		}
		return -1;
	}
	
	/**
	 * �Ӻ���ǰ�����ַ����������š�����ƥ��
	 * @param src Դ��
	 * @param find Ҫ���ҵĴ�
	 * @return �Ҳ�������-1
	 */
	public static int lastIndexOf(String src, String find) {
		int end = src.length() - 1;
		int findLen = find.length();
		
		if (findLen == 1) {
			char tc = find.charAt(0);
			while (end >= 0) {
				char c = src.charAt(end);
				if (c == tc) {
					if (end > 0 && src.charAt(end - 1) == '\\') {
						break;
					} else {
						return end;
					}
				} else if (c == '"' || c == '\'' || c == ')' || c == ']' || c == '}' || c == '\\') {
					// ��ͷ��ʼ��ƥ��
					break;
				} else {
					end--;
				}
			}
		}
		
		int pos = -1;
		int i = 0;
		while (i <= end) {
			char c = src.charAt(i);
			switch (c) {
			case '"':
			case '\'':
				int match = scanQuotation(src, i, '\\');
				if (match == -1) {
					return -1;
				} else {
					i = match + 1;
					continue; // ���������ڵ�����
				}
			case '(':
				match = Sentence.scanParenthesis(src, i, '\\');
				if (match == -1) {
					return -1;
				} else {
					i = match + 1;
					continue; // ���������ڵ�����
				}
			case '[':
				match = Sentence.scanBracket(src, i, '\\');
				if (match == -1) {
					return -1;
				} else {
					i = match + 1;
					continue; // ���������ڵ�����
				}
			case '{':
				match = Sentence.scanBrace(src, i, '\\');
				if (match == -1) {
					return -1;
				} else {
					i = match + 1;
					continue; // ���������ڵ�����
				}
			case '\\':
				i += 2;
				continue;
			}

			if (src.startsWith(find, i)) {
				pos = i;
				i += findLen;
			} else {
				i++;
			}
		}

		return pos;
	}
	
	public static boolean isWordChar(char ch) {
		return Character.isJavaIdentifierStart(ch)
				|| Character.isJavaIdentifierPart(ch);
	}
	
	//������һ��\r��\n��λ�ã�δ�ҵ����س���
	private static int scanCRLF(String str, int start) {
		int len = str.length();
		while(start<len){
			char ch = str.charAt(start);
			if(ch=='\r' || ch=='\n') 
				return start;
			start++;
		}
		return len;
	}
	
	//����*/��λ�ã�δ�ҵ����س���
	private static int scanCommentEnd(String str, int start) {
		int len = str.length();
		while(start<len) {
			char ch = str.charAt(start);
			if(ch=='*' && start<len-1 && str.charAt(start+1)=='/')
				return start;
			start++;
		}
		return len;
	}
	
	/**
	 * ɾ������java������ע�ͺͶ���ע�� 
	 */
	public static String removeComment(String str){
		int idx=0, len=str.length();
		StringBuffer buf = new StringBuffer(len);
		while(idx<len){
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') {
				int tmp = Sentence.scanQuotation(str, idx);
				if(tmp<0) {
					buf.append(str.substring(idx));
					break;
				}else{
					buf.append(str.substring(idx, tmp+1));
					idx=tmp+1;
				}
			}else if(ch == '/') {
				if(idx==len-1){
					buf.append('/');
					break;
				}
				char ch2 = str.charAt(idx+1);
				if(ch2=='/') {
					idx = scanCRLF(str, idx+2);  //λ�ò��ӣ������س����������
				}else if(ch2=='*'){
					idx = scanCommentEnd(str, idx+2)+2;
				}else{
					buf.append('/');
					idx++;
				}
			}else{
				buf.append(ch);
				idx++;
			}
			
		}
		return buf.toString();
	}

	private String str;
	private char escapeChar = '\\';

	public static void main(String[] args) throws Exception {
		StringBuffer buf = new StringBuffer(); 
		BufferedReader br = new BufferedReader(new FileReader("d:\\1.txt"));
		while(true){
			String line = br.readLine();
			if(line==null) break;
			buf.append(line).append("\r\n");
		}
		br.close();
		String s = removeComment(buf.toString());
		System.out.println( s );
		System.out.println( "..." + System.getProperty("line.separator"));
	}
}
