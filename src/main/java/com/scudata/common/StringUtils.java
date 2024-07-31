package com.scudata.common;

import java.awt.FontMetrics;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;


/**
 * �ַ���������
 * @author RunQian
 *
 */
public class StringUtils {
	private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static String[] excelLabels = { "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };

	private static String toExcel(int index) {
		if (index < 26) {
			return excelLabels[index];
		}

		int shang = index / 26;
		int yu = index % 26;

		return toExcel(shang - 1) + excelLabels[yu];
	}

	/**
	 * ������ת��ΪExcel���б�ǩ
	 * @param index int,Ҫת��������,��1��ʼ,��1��ΪA
	 * @return String,ת������б�ǩ,ע��Excel��ǩ����26����,26���Ƶ�Z��Ӧ��ΪBA;��Excel��ǩZ��ΪAA
	 */
	public static String toExcelLabel(int index) {
		return toExcel(index - 1);
	}

	/**
	 * �����ַ����Ƿ��ǿհ׷�
	 * @param s �ַ���
	 * @return true���ǣ�false������
	 */
	public final static boolean isSpaceString(String s) {
		if (s == null) {
			return true;
		}

		for (int i = 0, len = s.length(); i < len; i++) {
			char c = s.charAt(i);
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * ��һ���������е�ָ���ֽ���ת����16���ƴ�
	 * @param l ������
	 * @param byteNum �������еĵ��ֽ���(������������ֽڸ���)
	 * @return String
	 */
	public final static String toHexString(long l, int byteNum) {
		StringBuffer sb = new StringBuffer(16);
		appendHexString(sb, l, byteNum);
		return sb.toString();
	}

	/**
	 * ��һ���������е�ָ���ֽ���ת����16���ƴ��������ӵ��ַ�����������
	 * @param sb �ַ���������
	 * @param l ������
	 * @param byteNum �������еĵ��ֽ���(������������ֽڸ���)
	 */
	public final static void appendHexString(StringBuffer sb, long l,
			int byteNum) {
		for (int i = byteNum * 2 - 1; i >= 0; i--) {
			long x = (l >> (i * 4)) & 0xf;
			sb.append(hexDigits[(int) x]);
		}
	}

	/**
	 * ���ַ�����unicode�ַ�ת��Ϊ&#92;uxxxx��ʽ������'\\','\t','\n','\r','\f'����
	 * ��������specialChars���κ��ַ���ǰ��\
	 * @param s ��Ҫ������ַ���
	 * @param sb ׷�Ӵ������Ļ�����
	 * @param specialChars ��Ҫ��ǰ��\���ر��ַ���
	 * @return ��sb!=null�򷵻�sb�����򷵻�׷���˴���������StringBuffer
	 */
	public final static StringBuffer deunicode(String s, StringBuffer sb,
			String specialChars) {
		int len = s.length();
		if (sb == null) {
			sb = new StringBuffer(len * 2);

		}

		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '\\':
				sb.append('\\').append('\\');
				break;
			case '\t':
				sb.append('\\').append('t');
				break;
			case '\n':
				sb.append('\\').append('n');
				break;
			case '\r':
				sb.append('\\').append('r');
				break;
			case '\f':
				sb.append('\\').append('f');
				break;
			default:
				if ((ch < 0x0020) || (ch > 0x007e)) {
					sb.append('\\').append('u');
					sb.append(hexDigits[(ch >> 12) & 0xF]);
					sb.append(hexDigits[(ch >> 8) & 0xF]);
					sb.append(hexDigits[(ch >> 4) & 0xF]);
					sb.append(hexDigits[ch & 0xF]);
				} else {
					if (specialChars != null && specialChars.indexOf(ch) != -1) {
						sb.append('\\');
					}

					sb.append(ch);
				}
			}
		}

		return sb;
	}

	/**
	 * ���ַ�����unicode�ַ�ת��Ϊ&#92;uxxxx��ʽ������'\\','\t','\n','\r','\f'����
	 * ��������specialChars���κ��ַ���ǰ��\
	 * @param s ��Ҫ������ַ���
	 * @param sb ׷�Ӵ������Ļ�����
	 * @return ��sb!=null�򷵻�sb�����򷵻�׷���˴���������StringBuffer
	 */
	public final static StringBuffer deunicode(String s, StringBuffer sb) {
		return deunicode(s, sb, null);
	}

	/**
	 * ���ַ�����unicode�ַ�ת��Ϊ&#92;uxxxx��ʽ������'\\','\t','\n','\r','\f'����
	 * ��������specialChars���κ��ַ���ǰ��\
	 * @param s ��Ҫ������ַ���
	 * @param specialChars ��Ҫ��ǰ��\���ر��ַ���
	 * @return String
	 */
	public final static String deunicode(String s, String specialChars) {
		return deunicode(s, null, specialChars).toString();
	}

	/**
	 * ���ַ�����unicode�ַ�ת��Ϊ&#92;uxxxx��ʽ������'\\','\t','\n','\r','\f'����
	 * ��������specialChars���κ��ַ���ǰ��\
	 * @param s ��Ҫ������ַ���
	 * @return String
	 */
	public final static String deunicode(String s) {
		return deunicode(s, null, null).toString();
	}

	/**
	 * ����Զ����к����γɵ��ı��м���
	 * @param text �ı�
	 * @param fm FontMetrics
	 * @param w ���
	 * @return �ַ����б�
	 */
	public static ArrayList<String> wrapString(String text, FontMetrics fm,
			float w) {
		w = (float) Math.ceil(w) - 1.01f;
		ArrayList<String> al = new ArrayList<String>();
		text = StringUtils.replace(text, "\\n", "\n");
		ArgumentTokenizer at = new ArgumentTokenizer(text, '\n', true, true,
				true);

		while (at.hasNext()) {
			String line = at.next();
			if (at.hasNext()) {
				line += "\n";
			}

			int len = line.length();
			String tmp = "";
			for (int i = 0; i < len; i++) {
				char c = line.charAt(i);
				tmp += String.valueOf(c);
				if (fm.stringWidth(tmp) > w) {
					int cut = cutLine(tmp, c);
					al.add(tmp.substring(0, cut));
					tmp = tmp.substring(cut);
				}
			}

			al.add(tmp);
		}

		return al;
	}

	// ����s��β�����У�����������ɺ����е��ַ���
	private static int cutLine(String s, char c) {
		// edited by bd, 2018.4.4, ����Ļ��й���ȽϾ��ˣ������ж�Ϊ"һ������"������Ӣ���ַ�������ϣ����ÿ����Ƿ��β
		// �����ǰ���г���len��֪���ǾͲ�ȥ���㣬������㵱ǰ����ʱ�����ַ���
		int len = s.length() - 1;

		// ���β�ַ�c��֪���ǾͲ���ȥ���㣬�������β�ַ�
		if (c == 0) {
			c = s.charAt(len);

			// ����Ҫ���ַ�cǰ����ʱ������Ҫ����c�Ƿ��Ǳ����ַ�
			// edited by bdl, 2011.5.17, �������ַ�Ҳ��Ӣ���ַ�ʱ����ʵ����С���㣩
		}
		boolean canBeHead = canBeHead(c);
		boolean isEnglishChar = isEnglishChar(c);
		if (!canBeHead && isEnglishChar) {
			// ����������Ӣ���ַ���Ҫ��Ϊһ�����ʹ�ͬ���У�������Ҫ�ж��Ƿ���ȫ��Ϊ����Ӣ���ַ�
			int seek = len - 1;
			int loc = 0;
			boolean hasHead = canBeHead(c);
			boolean letterbreak = false;
			while (seek >= 0 && loc == 0) {
				char seekChar = s.charAt(seek);
				if (!isEnglishChar(seekChar)) {
					letterbreak = true;
					if (!hasHead) {
						if (canBeHead(seekChar)) {
							// ��������г��ַǱ����ַ����ų����֣�����ô�趨������Ϊtrue
							hasHead = true;
						}
						seek--;
					} else {
						// ��������г��ַ�Ӣ���ַ�����ô�жϸ��ַ��Ƿ��Ǳ�β�ַ�
						if (canBeFoot(seekChar)) {
							// ����ǷǱ�β�ַ�����ô������ַ������м���
							loc = seek + 1;
						} else {
							if (canBeHead(seekChar)) {
								hasHead = true;
							} else {
								hasHead = false;
							}
							seek--;
						}
					}
				} else if (letterbreak) {
					// ������ֱ�β�ַ�֮�����ҵ�Ӣ���ַ�����ô�Ӹ�Ӣ���ַ���Ͽ��ͺ�
					loc = seek + 1;
				} else {
					if (canBeHead(seekChar)) {
						hasHead = true;
					} else {
						hasHead = false;
					}
					seek--;
				}
			}
			if (loc > 0) {
				// ����������з�Ӣ���ַ�
				return loc;
			} else {
				// ���������ȫ����Ӣ���ַ������������ı�β�ַ�����ô�����з�
				return len;
			}
		} else if (!canBeHead) {
			// c�Ǳ����ַ�
			int seek = len - 1;
			int loc = 0;
			boolean hasHead = false;
			// �ҵ���һ���Ǳ����ַ�
			while (seek >= 0 && loc == 0) {
				char seekChar = s.charAt(seek);
				if (!hasHead) {
					if (canBeHead(seekChar)) {
						// ��������г��ַǱ����ַ����ų����֣�����ô�趨������Ϊtrue
						hasHead = true;
					}
					seek--;
				} else {
					if (isEnglishChar(seekChar)) {
						// ����Ӣ���ַ�����ǰ��ѯ����һ��������Ӣ���ַ�Ϊֹ
						int eseek = seek;
						boolean eng = true;
						while (eng && seek > 0) {
							seek--;
							eng = isEnglishChar(s.charAt(seek));
						}
						// added by bdl, 2011.8.12, ����ӵ�ǰ�ַ�ֱ����һ������������Ӣ���ַ���
						// ��ô�����һ��Ӣ���ַ�ǰ����
						if (seek == 0) {
							loc = eseek + 1;
						}
					}
					// ����Ѿ������֣���ô�ж��Ƿ�Ҫ��β
					else if (canBeFoot(seekChar)) {
						// �������β����ô�ڸ��ַ�����м���
						loc = seek + 1;
					} else {
						// ��Ҫ��β
						seek--;
					}
				}
			}
			if (loc > 0) {
				// ��������п���������
				return loc;
			} else {
				// ���������ȫ���Ǳ��׻����������ı�β�ַ�����ô�����з�
				return len;
			}
		}
		// Ȼ�����ж�c�Ƿ���Ӣ���ַ�
		else if (isEnglishChar) {
			// ����������Ӣ���ַ���Ҫ��Ϊһ�����ʹ�ͬ���У�������Ҫ�ж��Ƿ���ȫ��Ϊ����Ӣ���ַ�
			int seek = len - 1;
			int loc = 0;
			boolean hasHead = canBeHead(c);
			boolean letterbreak = false;
			while (seek >= 0 && loc == 0) {
				char seekChar = s.charAt(seek);
				if (!isEnglishChar(seekChar)) {
					// edited by bdl, 20111.5.17, ����ʱ�����жϵ�ǰ���ַ��Ƿ����
					letterbreak = true;
					if (!hasHead) {
						if (canBeHead(seekChar)) {
							hasHead = true;
						}
						seek--;
					}
					// ��������г��ַ�Ӣ���ַ�����ô�жϸ��ַ��Ƿ��Ǳ�β�ַ�
					else if (canBeFoot(seekChar)) {
						// ����ǷǱ�β�ַ�����ô������ַ������м���
						loc = seek + 1;
					} else {
						if (canBeHead(seekChar)) {
							hasHead = true;
						} else {
							hasHead = false;
						}
						seek--;
					}
				} else if (letterbreak) {
					// edited by bdl, 20111.5.17, ������ֹ���Ӣ�ĵı����ַ�
					// ������ֱ�β�ַ�֮�����ҵ�Ӣ���ַ�����ô�Ӹ�Ӣ���ַ���Ͽ��ͺ�
					loc = seek + 1;
				} else {
					// edited by bdl, 20111.5.17, ���Ӣ���г��ֱ����ַ�
					if (canBeHead(seekChar)) {
						hasHead = true;
					} else {
						hasHead = false;
					}
					seek--;
				}
			}
			if (loc > 0) {
				// ����������з�Ӣ���ַ�
				return loc;
			} else {
				// ���������ȫ����Ӣ���ַ������������ı�β�ַ�����ô�����з�
				return len;
			}
		}
		return seekCanBeFoot(s.substring(0, len), len);
	}

	// added by bdl, 2008.5.21�����ַ���s�������һ�������ڱ�����ĩ���ַ��ĺ���Ͽ������ضϿ������ַ���
	private static int seekCanBeFoot(String s, int len) {
		// �����ǰ���г���len��֪���ǾͲ�ȥ���㣬������㵱ǰ����ʱ�����ַ���
		if (len == -1) {
			len = s.length();
		}
		if (len <= 1) {
			return len;
		}

		int seek = len - 1;
		int loc = 0;
		while (seek >= 0 && loc == 0) {
			char seekChar = s.charAt(seek);
			if (canBeFoot(seekChar)) {
				loc = seek + 1;
			} else {
				seek--;
			}
		}
		if (loc > 0) {
			return loc;
		}
		// ���s�������ַ����Ǳ�β�ַ�����ô���б���
		return len;
	}

	// added by bdl, 2008.5.21���ж�ĳ�ַ�ͨ��������Ƿ�����Ϊ��β
	private static boolean canBeFoot(char c) {
		// ���е�ʱ��Ӧ�ÿ��ǵı�β�ַ�
		String cannotFoot = "([{�������������������������ۣ��꣤";
		return cannotFoot.indexOf(c) < 0;
	}

	// added by bdl, 2008.5.21���ж�ĳ�ַ�ͨ��������Ƿ�����Ϊ����
	private static boolean canBeHead(char c) {
		// ���е�ʱ��Ӧ�ÿ��ǵı����ַ�
		String cannotHead = "!),.:;?]}���������D���������á����������������������������������������ݣ��������";
		return cannotHead.indexOf(c) < 0;
	}

	private static boolean isEnglishChar(char c) {
		// return ( (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
		// (c >= '0' && c <= '9'));
		// edited by bd, 2018.4.4, ��һ����ĸ���ֹ��ɵ�"������"�У�word�����������Ӣ�ķ��ŵ�
		// ������жϷ�����һ�����������ո������ַ��Ȳ�����Ӣ�ĵ��ʽ���
		return (c <= '~' && c > ' ');
	}

	/**
	 * ��&#92;uxxxxת��Ϊunicode�ַ�������'\\','\t','\n','\r','\f'���д���
	 *
	 * @params s ��Ҫ������ַ���
	 * @params sb ׷�Ӵ������Ļ�����
	 * @return ��sb!=null�򷵻�sb�����򷵻�׷���˴���������StringBuffer
	 */
	public final static StringBuffer unicode(String s, StringBuffer sb) {
		int len = s.length();
		if (sb == null) {
			sb = new StringBuffer(len);

		}
		char ch;
		for (int i = 0; i < len;) {
			ch = s.charAt(i++);
			if (ch != '\\') {
				sb.append(ch);
				continue;
			}
			ch = s.charAt(i++);
			if (ch == 'u') {
				// Read the xxxx
				int value = 0;
				for (int j = 0; j < 4; j++) {
					ch = s.charAt(i++);
					switch (ch) {
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						value = (value << 4) + ch - '0';
						break;
					case 'a':
					case 'b':
					case 'c':
					case 'd':
					case 'e':
					case 'f':
						value = (value << 4) + 10 + ch - 'a';
						break;
					case 'A':
					case 'B':
					case 'C':
					case 'D':
					case 'E':
					case 'F':
						value = (value << 4) + 10 + ch - 'A';
						break;
					default:
						throw new IllegalArgumentException("���Ϸ���\\uxxxx����");
					} // switch(ch)
				} // for(int j)
				sb.append((char) value);
			} else {
				switch (ch) {
				case 't':
					ch = '\t';
					break;
				case 'r':
					ch = '\r';
					break;
				case 'n':
					ch = '\n';
					break;
				case 'f':
					ch = '\f';
					break;
				}
				sb.append(ch);
			}
		} // for(int i)
		return sb;
	}

	public final static String unicode(String s) {
		return unicode(s, null).toString();
	}

	public final static String unicode2String(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed \\uxxxx encoding.");
						}
					}
					outBuffer.append((char) value);
				} // if(aChar)
			} else {
				outBuffer.append(aChar);
			}
		} // for(int x)
		return outBuffer.toString();
	}

	final static char[] c1Digit = { '��', 'Ҽ', '��', '��', '��', '��', '½', '��',
			'��', '��' };
	final static char[] c2Digit = { '��', 'һ', '��', '��', '��', '��', '��', '��',
			'��', '��' };
	final static char[] c1Unit = { 'ʰ', '��', 'Ǫ' };
	final static char[] c2Unit = { 'ʮ', '��', 'ǧ' };
	final static String[] chinaUnit = { "��", "��", "����" };

	private final static StringBuffer toRMB2(long l, char[] cDigit, char[] cUnit) {
		int unit = 0, bit = 0, d;
		boolean hasZero = false, sf = false;
		StringBuffer sb = new StringBuffer(64);
		while (l > 0) {
			if (bit == 4) {
				if (unit > 2) {
					throw new IllegalArgumentException("��д��֧�ִ���һ�����ڵ���");
				}

				if (sf) {
					if (hasZero || l % 10 == 0) {
						sb.append(cDigit[0]);
						hasZero = false;
					}
				} else {
					int len = sb.length();
					if (len > 0) {
						sb.deleteCharAt(len - 1);
					}
				}

				sb.append(chinaUnit[unit]);
				unit++;
				bit = 0;
				sf = false;
			}

			d = (int) (l % 10);
			if (d > 0) {
				sf = true;
				if (hasZero) {
					sb.append(cDigit[0]);
					hasZero = false;
				}
				if (bit != 0) {
					sb.append(cUnit[bit - 1]);
				}
				sb.append(cDigit[d]);
			} else {
				if (sf) { // ����β����0����
					hasZero = true;
				}
			}

			bit++;
			l /= 10;
		}
		return sb.reverse();
	}

	/**
	 * ����������ʽ������Ҵ�д��ʽ
	 *
	 * @param money
	 *            ������
	 * @return ��ʽ�����ַ���
	 * @exception IllegalArgumentException
	 *                ��money<0��money>=һ������ʱ
	 */
	public final static String toRMB(double money) {
		return toRMB(money, true, true);
	}

	public final static String toRMB(double money, boolean abbreviate,
			boolean uppercase) {
		char[] cDigit = uppercase ? c1Digit : c2Digit;
		char[] cUnit = uppercase ? c1Unit : c2Unit;
		StringBuffer sb = new StringBuffer(64);
		if (money < 0) {
			sb.append("��");
			money = -money;
		}
		long yuan = (long) money; // Ԫ

		if (yuan == 0) {
			sb.append("��");
		} else {
			sb.append(toRMB2(yuan, cDigit, cUnit));
		}
		sb.append('Ԫ');

		int jaoFeng = (int) ((money + 0.001 - (long) money) * 100) % 100;
		int jao = jaoFeng / 10;
		int feng = jaoFeng % 10;
		if (jao > 0) {
			sb.append(cDigit[jao]);
			sb.append('��');
		}
		if (feng > 0) {
			if (jao == 0) {
				sb.append('��');
			}
			sb.append(cDigit[feng]);
			sb.append('��');
		} else {
			sb.append('��');
		}
		return sb.toString();
	}

	public final static String toChinese(long l, boolean abbreviate,
			boolean uppercase) {
		String fu = "";
		if (l == 0) {
			return "��";
		} else if (l < 0) {
			fu = "��";
			l = -l;
		}
		char[] cDigit = uppercase ? c1Digit : c2Digit;
		char[] cUnit = uppercase ? c1Unit : c2Unit;
		if (abbreviate) {
			return fu + toRMB2(l, cDigit, cUnit).toString();
		} else {
			StringBuffer sb = new StringBuffer(64);
			for (; l > 0; l /= 10) {
				int digit = (int) l % 10;
				sb.append(cDigit[digit]);
			}
			sb = sb.reverse();
			return fu + sb.toString();
		} // if ( abbreviate )
	}

	/**
	 * ���ִ�Сд�ĸ�ʽƥ��
	 * @param src Դ��
	 * @param pos1 Դ����ʼλ��
	 * @param fmt ��ʽ��
	 * @param pos2 ��ʽ����ʼλ��
	 * @return true��ƥ�䣬false����ƥ��
	 */
	private final static boolean matches(String src, int pos1, String fmt,
			int pos2) {
		int len1 = src.length(), len2 = fmt.length();
		boolean any = false; // �Ƿ����Ǻ�

		while (pos2 < len2) {
			char ch = fmt.charAt(pos2);
			if (ch == '*') {
				pos2++;
				any = true;
			} else if (ch == '?') {
				// Դ����Ҫ������һ���ַ���?ƥ��
				if (++pos1 > len1) {
					return false;
				}

				pos2++;
			} else if (any) {
				// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
				if (ch == '\\' && pos2 + 1 < len2) {
					char c = fmt.charAt(pos2 + 1);
					if (c == '*' || c == '?') {
						ch = c;
						pos2++;
					}
				}

				while (pos1 < len1) {
					// �ҵ��׸�ƥ����ַ�
					if (src.charAt(pos1++) == ch) {
						// �ж�ʣ�µĴ��Ƿ�ƥ�䣬�����ƥ������Դ��һ���ַ����ʽ������ƥ��
						if (matches(src, pos1, fmt, pos2 + 1)) {
							return true;
						}
					}
				}

				return false;
			} else {
				// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
				if (ch == '\\' && pos2 + 1 < len2) {
					char c = fmt.charAt(pos2 + 1);
					if (c == '*' || c == '?') {
						ch = c;
						pos2++;
					}
				}

				// Դ����ǰ�ַ���Ҫ���ʽ����ǰ�ַ�ƥ��
				if (pos1 == len1 || src.charAt(pos1++) != ch) {
					return false;
				}

				for (++pos2; pos2 < len2;) {
					ch = fmt.charAt(pos2);
					if (ch == '*') {
						any = true;
						pos2++;
						break;
					} else if (ch == '?') {
						if (++pos1 > len1) {
							return false;
						}

						pos2++;
					} else {
						// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
						if (ch == '\\' && pos2 + 1 < len2) {
							char c = fmt.charAt(pos2 + 1);
							if (c == '*' || c == '?') {
								ch = c;
								pos2++;
							}
						}

						if (pos1 == len1 || src.charAt(pos1++) != ch) {
							return false;
						}

						pos2++;
					}
				}
			}
		}

		return any || pos1 == len1;
	}

	/**
	 * �����ִ�Сд�ĸ�ʽƥ��
	 * @param src Դ��
	 * @param pos1 Դ����ʼλ��
	 * @param fmt ��ʽ��
	 * @param pos2 ��ʽ����ʼλ��
	 * @return true��ƥ�䣬false����ƥ��
	 */
	private final static boolean matchesIgnoreCase(String src, int pos1,
			String fmt, int pos2) {
		int len1 = src.length(), len2 = fmt.length();
		boolean any = false; // �Ƿ����Ǻ�

		while (pos2 < len2) {
			char ch = fmt.charAt(pos2);
			if (ch == '*') {
				pos2++;
				any = true;
			} else if (ch == '?') {
				// Դ����Ҫ������һ���ַ���?ƥ��
				if (++pos1 > len1) {
					return false;
				}

				pos2++;
			} else if (any) {
				// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
				if (ch == '\\' && pos2 + 1 < len2) {
					char c = fmt.charAt(pos2 + 1);
					if (c == '*' || c == '?') {
						ch = c;
						pos2++;
					}
				}

				while (pos1 < len1) {
					// �ҵ��׸�ƥ����ַ�
					if (Character.toUpperCase(src.charAt(pos1++)) == Character
							.toUpperCase(ch)) {
						// �ж�ʣ�µĴ��Ƿ�ƥ�䣬�����ƥ������Դ��һ���ַ����ʽ������ƥ��
						if (matchesIgnoreCase(src, pos1, fmt, pos2 + 1)) {
							return true;
						}
					}
				}

				return false;
			} else {
				// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
				if (ch == '\\' && pos2 + 1 < len2) {
					char c = fmt.charAt(pos2 + 1);
					if (c == '*' || c == '?') {
						ch = c;
						pos2++;
					}
				}

				// Դ����ǰ�ַ���Ҫ���ʽ����ǰ�ַ�ƥ��
				if (pos1 == len1
						|| Character.toUpperCase(src.charAt(pos1++)) != Character
								.toUpperCase(ch)) {
					return false;
				}

				for (++pos2; pos2 < len2;) {
					ch = fmt.charAt(pos2);
					if (ch == '*') {
						any = true;
						pos2++;
						break;
					} else if (ch == '?') {
						if (++pos1 > len1) {
							return false;
						}

						pos2++;
					} else {
						// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
						if (ch == '\\' && pos2 + 1 < len2) {
							char c = fmt.charAt(pos2 + 1);
							if (c == '*' || c == '?') {
								ch = c;
								pos2++;
							}
						}

						if (pos1 == len1
								|| Character.toUpperCase(src.charAt(pos1++)) != Character
										.toUpperCase(ch)) {
							return false;
						}

						pos2++;
					}
				}
			}
		}

		return any || pos1 == len1;
	}

	/**
	 * �ж��ַ����Ƿ����ָ���ĸ�ʽ
	 * @param value  �ַ���
	 * @param fmt ��ʽ��(*��ʾ0�������ַ���?��ʾ�����ַ�)
	 * @param ignoreCase true�����Դ�Сд��false����Сд����
	 * @return ��value��fmtΪnullʱ����false������ƥ��ʱҲ����false�����򷵻�true
	 */
	public final static boolean matches(String value, String fmt,
			boolean ignoreCase) {
		if (value == null || fmt == null) {
			return false;
		}

		if (ignoreCase) {
			return matchesIgnoreCase(value, 0, fmt, 0);
		} else {
			return matches(value, 0, fmt, 0);
		}
	}

	private static boolean like(String src, int pos1, String fmt, int pos2) {
		int len1 = src.length();
		int len2 = fmt.length();
		boolean any = false; // �Ƿ����Ǻ�
		
		while (pos2 < len2) {
			char ch = fmt.charAt(pos2);
			if (ch == '%') {
				pos2++;
				any = true;
			} else if (ch == '_') {
				// Դ����Ҫ������һ���ַ���?ƥ��
				if (++pos1 > len1) {
					return false;
				}

				pos2++;
			} else if (any) {
				// \% ��ʾ��λ����Ҫ�ַ�'%'��������ͨ���%
				if (ch == '\\' && pos2 + 1 < len2) {
					char c = fmt.charAt(pos2 + 1);
					if (c == '%' || c == '_') {
						ch = c;
						pos2++;
					}
				}

				while (pos1 < len1) {
					// �ҵ��׸�ƥ����ַ�
					if (src.charAt(pos1++) == ch) {
						// �ж�ʣ�µĴ��Ƿ�ƥ�䣬�����ƥ������Դ��һ���ַ����ʽ������ƥ��
						if (like(src, pos1, fmt, pos2 + 1)) {
							return true;
						}
					}
				}

				return false;
			} else {
				// \% ��ʾ��λ����Ҫ�ַ�'%'��������ͨ���%
				if (ch == '\\' && pos2 + 1 < len2) {
					char c = fmt.charAt(pos2 + 1);
					if (c == '%' || c == '_') {
						ch = c;
						pos2++;
					}
				}

				// Դ����ǰ�ַ���Ҫ���ʽ����ǰ�ַ�ƥ��
				if (pos1 == len1 || src.charAt(pos1++) != ch) {
					return false;
				}

				for (++pos2; pos2 < len2;) {
					ch = fmt.charAt(pos2);
					if (ch == '%') {
						any = true;
						pos2++;
						break;
					} else if (ch == '_') {
						if (++pos1 > len1) {
							return false;
						}

						pos2++;
					} else {
						// \% ��ʾ��λ����Ҫ�ַ�'%'��������ͨ���%
						if (ch == '\\' && pos2 + 1 < len2) {
							char c = fmt.charAt(pos2 + 1);
							if (c == '%' || c == '_') {
								ch = c;
								pos2++;
							}
						}

						if (pos1 == len1 || src.charAt(pos1++) != ch) {
							return false;
						}

						pos2++;
					}
				}
			}
		}

		return any || pos1 == len1;
	}
	
	/**
	 * ����SQLͨ���������ƥ��
	 * @param src Ҫƥ��Ĵ�
	 * @param fmt ͨ�����SQL��׼��%:���0�������ַ���_�������һ���ַ�
	 * @return true��ƥ�䣬false����ƥ��
	 */
	public static boolean like(String src, String fmt) {
		if (src == null ) {
			return false;
		} else {
			return like(src, 0, fmt, 0);
		}
	}
	
	private final static String[] provinces = { null, null, null, null, null,
			null, null, null, null, null, null, "����", "���", "�ӱ�", "ɽ��", "���ɹ�",
			null, null, null, null, null, "����", "����", "������", null, null, null,
			null, null, null, null, "�Ϻ�", "����", "�㽭", "��΢", "����", "����", "ɽ��",
			null, null, null, "����", "����", "����", "�㶫", "����", "����", null, null,
			null, "����", "�Ĵ�", "����", "����", "����", null, null, null, null, null,
			null, "����", "����", "�ຣ", "����", "�½�", null, null, null, null, null,
			"̨��", null, null, null, null, null, null, null, null, null, "���",
			"����", null, null, null, null, null, null, null, null, "����" };

	private final static int[] wi = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10,
			5, 8, 4, 2, 1 };
	private final static char[] codes = { '1', '0', 'X', '9', '8', '7', '6',
			'5', '4', '3', '2' };

	/**
	 * ����GB11643-1999<<������ݺ���>>��GB11643-1989<<��ᱣ�Ϻ���>>�涨������֤���Ƿ���Ϲ淶
	 */
	public final static boolean identify(String ident) {
		if (ident == null) {
			return false;
		}

		int len = ident.length();
		if (len != 15 && len != 18) {
			return false;
		}

		for (int i = 0; i < ((len == 15) ? 15 : 17); i++) {
			char ch = ident.charAt(i);
			if (ch < '0' || ch > '9') {
				return false;
			}
		}

		// ��黧�������ص����������� GB/T2260
		int p = (ident.charAt(0) - '0') * 10 + (ident.charAt(1) - '0');
		if (p >= provinces.length || provinces[p] == null) {
			return false;
		}

		// ������������ GB/T7408
		int year = 0, month = 0, day = 0;
		if (len == 15) {
			year = 1900 + (ident.charAt(6) - '0') * 10
					+ (ident.charAt(7) - '0');
			month = (ident.charAt(8) - '0') * 10 + (ident.charAt(9) - '0');
			day = (ident.charAt(10) - '0') * 10 + (ident.charAt(11) - '0');
		} else {
			year = (ident.charAt(6) - '0') * 1000 + (ident.charAt(7) - '0')
					* 100 + (ident.charAt(8) - '0') * 10
					+ (ident.charAt(9) - '0');
			month = (ident.charAt(10) - '0') * 10 + (ident.charAt(11) - '0');
			day = (ident.charAt(12) - '0') * 10 + (ident.charAt(13) - '0');
		}
		if (month == 2) {
			if ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))) {
				// ����2��29��
				if (day > 29) {
					return false;
				}
			} else {
				if (day > 28) {
					return false;
				}
			}
		} else if (month == 4 || month == 6 || month == 9 || month == 11) {
			if (day > 30) {
				return false;
			}
		} else if (month <= 12) {
			if (day > 31) {
				return false;
			}
		} else {
			return false;
		}

		// ���У����
		if (len == 18) {
			int[] w = wi;
			int mod = 0;
			for (int i = 0; i < 17; i++) {
				mod += (ident.charAt(i) - '0') * w[i];
			}
			mod = mod % 11;
			if (ident.charAt(17) != codes[mod]) {
				return false;
			}
		}
		return true;
	}

	public static String replace(String src, String findString,
			String replaceString) {
		if (src == null || findString == null) {
			return src;
		}

		int len = src.length();
		if (len == 0) {
			return src;
		}

		int len1 = findString.length();
		if (len1 == 0) {
			return src;
		}

		if (replaceString == null) {
			return src;
		}

		int start = 0;
		StringBuffer sb = null;
		while (true) {
			int pos = src.indexOf(findString, start);
			if (pos >= 0) {
				if (sb == null) {
					sb = new StringBuffer(len + 100);
				}
				for (int i = start; i < pos; i++) {
					sb.append(src.charAt(i));
				}
				sb.append(replaceString);
				start = pos + len1;
			} else {
				if (sb != null) {
					for (int i = start; i < len; i++) {
						sb.append(src.charAt(i));
					}
				}
				break;
			}
		}

		if (sb != null) {
			return sb.toString();
		} else {
			return src;
		}
	}

	/**
	 * �Ƿ���Ч�Ŀɼ��ַ���
	 *
	 * @param str
	 *            Object��Ҫ�жϵ��ַ�������
	 * @return boolean����Ч����true�����򷵻�false
	 */
	public static boolean isValidString(Object str) {
		if (!(str instanceof String)) {
			return false;
		}
		return !isSpaceString((String) str);
	}

	/**
	 * �ж�str�Ƿ�һ�����ϱ༭�淶�ı��ʽ��ֻ�������Ҫ��Ե����ţ����š���Щ��Ժ󣬲�����ɱ���ı��ʽ��������Ի���
	 * ����û��������淶��顣 ����  123)  �༭�淶�Ϸ������Ǽ���Ƿ���
	 * @param str
	 * @return
	 */
	public static boolean isValidExpression(String str) {
		if (!isValidString(str)) {
			return true;
		}

		int len = str.length();
		int index = 0;
		while (index < len) {
			char ch = str.charAt(index);
			if (ch == '\\') {
				index += 2;
			} else if (ch == '\"' || ch == '\'') {
				int tmp = Sentence.scanQuotation(str, index);
				if (tmp < 0) {
					return false;
				} else {
					index = tmp + 1;
				}
			} else if (ch == '(') {
				int tmp = Sentence.scanParenthesis(str, index);
				if (tmp < 0) {
					return false;
				} else {
					index = tmp + 1;
				}
			} else if (ch == '[') {
				int tmp = Sentence.scanBracket(str, index);
				if (tmp < 0) {
					return false;
				} else {
					index = tmp + 1;
				}
			} else if (ch == '{') {
				int tmp = Sentence.scanBrace(str, index);
				if (tmp < 0) {
					return false;
				} else {
					index = tmp + 1;
				}
			} else {
				index++;
			}
		}
		return true;
	}

	/**
	 * �ҳ�compare��keys�е�λ��
	 *
	 * @param keys
	 *            String[]
	 * @param compare
	 *            String
	 * @return int
	 */
	public static int indexOf(String[] keys, String compare) {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].equals(compare)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * �����ŷֿ���ÿһ��Ԫ����ӵ�Vector,�մ�����Ϊnull����
	 *
	 * @param str
	 *            String,Ҫת�����ַ���
	 * @return Vector,������Ӧ��Vector,Vector����SizeΪ0,���ö��󲻿���Ϊnull
	 */
	public static Vector string2Vector(String str) {
		return string2Vector(str, ',', false);
	}

	/**
	 * ��delim�ֿ���ÿһ��Ԫ����ӵ�Vector,�մ�����Ϊnull����
	 *
	 * @param str
	 *            String,Ҫת�����ַ���
	 * @param delim
	 *            char,�ָ����
	 * @return Vector,������Ӧ��Vector,Vector����SizeΪ0,���ö��󲻿���Ϊnull
	 */
	public static Vector string2Vector(String str, char delim) {
		return string2Vector(str, delim, false);
	}

	/**
	 * ��delim�ֿ���ÿһ��Ԫ����ӵ�Vector,�մ�����Ϊnull����
	 *
	 * @param str
	 *            String,Ҫת�����ַ���
	 * @param delim
	 *            char,�ָ����
	 * @param removeEsc
	 *            boolean,�Ƿ��滻ÿһ�ڵ�ת��
	 * @return Vector,������Ӧ��Vector,Vector����SizeΪ0,���ö��󲻿���Ϊnull
	 */
	public static Vector string2Vector(String str, char delim, boolean removeEsc) {
		Vector v = new Vector();
		if (str == null) {
			return v;
		}
		ArgumentTokenizer st = new ArgumentTokenizer(str, delim);
		String s;
		while (st.hasMoreTokens()) {
			s = st.nextToken();
			if (removeEsc) {
				s = Escape.removeEscAndQuote(s);
			}
			if (!isValidString(s)) {
				s = null;
			}
			v.add(s);
		}
		return v;
	}

	/**
	 * �������б�תΪ�ַ�������
	 *
	 * @param list List
	 * @return String[]
	 */
	// ���õ�utilĿ¼,û����������ע�͵�,wunan
	// ���ü�toString
	public static String[] toStringArray(ArrayList list) {
		int c = list.size();
		if (c == 0)
			return null;
		String[] array = new String[c];
		for (int i = 0; i < c; i++) {
			Object o = list.get(i);
			if (o != null) {
				array[i] = o.toString();
			}
		}
		return array;
	}

	/**
	 * ��鵱ǰ�����Ƿ���ָ����hosts��Χ
	 * @param hosts��ָ�����������ʱ����Ӣ�Ķ��ŷֿ���hostsΪ��ʱ������飬ֱ�ӷ���true
	 * @return
	 */
	public static boolean checkHosts(String hosts) {
		String host = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}

		if (!isValidString(hosts)) {
			return true;
		}
		String thisIp = host;
		boolean found = false;
		ArgumentTokenizer at = new ArgumentTokenizer(hosts, ',');
		while (at.hasMoreTokens()) {
			String tmpIp = at.nextToken().trim();
			if (thisIp.equals(tmpIp)) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * ����ֺŵı����Ŵ󣬱���ͼ�õ�ˮӡ����ʱ��Ҳ��Ҫ�Ŵ���Ӧ����
	 * @param fontSize
	 * @param scale
	 * @return
	 */
	public static int getScaledFontSize(int fontSize, float scale) {
		int size = fontSize;
		// ��������Ŵ��ֻȡ�������֣�����ɲ��ֽӽ�Ceiling��һЩ�ֺŻ�ƫС�����Լ���0.3�Ժ���ȡ��
		return (int) (size * scale + 0.3f);
	}
	
	private static boolean startsWithIgnoreCase(String source, String target) {
		int targetCount = target.length();
		if (targetCount == 0) {
			return true;
		}
		
		int sourceCount = source.length();
		if (sourceCount < targetCount) {
			return false;
		}
		
		for (int j = 0, k = 0; k < targetCount; ++j, ++k) {
			if (source.charAt(j) != target.charAt(k) && Character.toUpperCase(source.charAt(j)) != Character.toUpperCase(target.charAt(k))) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean endsWithIgnoreCase(String source, String target) {
		int targetCount = target.length();
		if (targetCount == 0) {
			return true;
		}
		
		int sourceCount = source.length();
		if (sourceCount < targetCount) {
			return false;
		}
		
		for (int j = sourceCount - targetCount, k = 0; k < targetCount; ++j, ++k) {
			if (source.charAt(j) != target.charAt(k) && Character.toUpperCase(source.charAt(j)) != Character.toUpperCase(target.charAt(k))) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * �����Ӵ���Դ���е�λ��
	 * @param source Դ��
	 * @param target �Ӵ�
	 * @param fromIndex Դ������ʼ����λ��
	 * @param ignoreCase �Ƿ���Դ�Сд
	 * @param headOnly �Ƿ�ֻ�Ƚ�ͷ��
	 * @param isLast �Ƿ�Ӻ�ʼ��
	 * @param skipQuotation �Ƿ����������ڵ��ַ�
	 * @return λ�ã��Ҳ�������-1
	 */
	public static int pos(String source, String target, int fromIndex, 
			boolean ignoreCase, boolean headOnly, boolean isLast, boolean skipQuotation) {
		if (ignoreCase) {
			if (headOnly) {
				if (isLast) {
					if (endsWithIgnoreCase(source, target)) {
						return source.length() - target.length();
					} else {
						return -1;
					}
				} else {
					if (startsWithIgnoreCase(source, target)) {
						return 0;
					} else {
						return -1;
					}
				}
			} else if (skipQuotation) {
				if (isLast) {
					return lastIndexOf(source, target, fromIndex, true, true);
				} else {
					return indexOf(source, target, fromIndex, true, true);
				}
			} else if (isLast) {
				return lastIndexOf(source, target, fromIndex, true, false);
			} else {
				return indexOf(source, target, fromIndex, true, false);
			}
		} else if (skipQuotation) {
			if (isLast) {
				if (headOnly) {
					int index = source.length() - target.length();
					if (source.startsWith(target, index)) {
						return index;
					} else {
						return -1;
					}
				} else {
					return lastIndexOf(source, target, fromIndex, false, true);
				}
			} else if (headOnly) {
				if (source.startsWith(target, 0)) {
					return 0;
				} else {
					return -1;
				}
			} else {
				return indexOf(source, target, fromIndex, false, true);
			}
		} else if (isLast) {
			if (headOnly) {
				int index = source.length() - target.length();
				if (source.startsWith(target, index)) {
					return index;
				} else {
					return -1;
				}
			} else {
				return source.lastIndexOf(target, fromIndex);
			}
		} else if (headOnly) {
			if (source.startsWith(target, 0)) {
				return 0;
			} else {
				return -1;
			}
		} else {
			return source.indexOf(target, fromIndex);
		}
	}
	
	/**
	 * ������������Դ���е�λ�ã�����Դ�������Ҳ�������ĸ�����֡��»���
	 * @param source Դ��
	 * @param target ��
	 * @param fromIndex Դ������ʼ����λ��
	 * @param ignoreCase �Ƿ���Դ�Сд
	 * @param headOnly �Ƿ�ֻ�Ƚ�ͷ��
	 * @param isLast �Ƿ�Ӻ�ʼ��
	 * @param skipQuotation �Ƿ����������ڵ��ַ�
	 * @return λ�ã��Ҳ�������-1
	 */
	public static int wholePos(String source, String target, int fromIndex, 
			boolean ignoreCase, boolean headOnly, boolean isLast, boolean skipQuotation) {
		if (ignoreCase) {
			source = source.toUpperCase();
			target = target.toUpperCase();
		}
		
		if (headOnly) {
			if (isLast) {
				int index = source.length() - target.length();
				if (source.startsWith(target, index)) {
					if (index == 0 || !Character.isJavaIdentifierPart(source.charAt(index - 1))) {
						return index;
					} else {
						return -1;
					}
				} else {
					return -1;
				}
			} else {
				if (source.startsWith(target, 0)) {
					int len = target.length();
					if (source.length() == len || !Character.isJavaIdentifierPart(source.charAt(len))) {
						return 0;
					} else {
						return -1;
					}
				} else {
					return -1;
				}
			}
		}

		int srcLen = source.length();
		int len = target.length();
		if (skipQuotation) {
			if (isLast) {
				while (true) {
					int pos = lastIndexOf(source, target, fromIndex, false, true);
					if (pos == -1) {
						return -1;
					} else if ((pos == 0 || !Character.isJavaIdentifierPart(source.charAt(pos - 1))) && 
							(pos + len == srcLen || !Character.isJavaIdentifierPart(source.charAt(pos + len)))) {
						return pos;
					} else {
						fromIndex -= len;
					}
				}
			} else {
				while (true) {
					int pos = indexOf(source, target, fromIndex, false, true);
					if (pos == -1) {
						return -1;
					} else if ((pos == 0 || !Character.isJavaIdentifierPart(source.charAt(pos - 1))) && 
							(pos + len == srcLen || !Character.isJavaIdentifierPart(source.charAt(pos + len)))) {
						return pos;
					} else {
						fromIndex += len;
					}
				}
			}
		} else if (isLast) {
			while (true) {
				int pos = source.lastIndexOf(target, fromIndex);
				if (pos == -1) {
					return -1;
				} else if ((pos == 0 || !Character.isJavaIdentifierPart(source.charAt(pos - 1))) && 
						(pos + len == srcLen || !Character.isJavaIdentifierPart(source.charAt(pos + len)))) {
					return pos;
				} else {
					fromIndex -= len;
				}
			}
		} else {
			while (true) {
				int pos = source.indexOf(target, fromIndex);
				if (pos == -1) {
					return -1;
				} else if ((pos == 0 || !Character.isJavaIdentifierPart(source.charAt(pos - 1))) && 
						(pos + len == srcLen || !Character.isJavaIdentifierPart(source.charAt(pos + len)))) {
					return pos;
				} else {
					fromIndex += len;
				}
			}
		}
	}
	
	private static int indexOf(String source, String target, int fromIndex, 
			boolean ignoreCase, boolean skipQuotation) {
		int sourceCount = source.length();
		int targetCount = target.length();
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}

		if (fromIndex < 0) {
			fromIndex = 0;
		}

		if (targetCount == 0) {
			return fromIndex;
		}

		char first = target.charAt(0);
		int max = sourceCount - targetCount;
		
		if (ignoreCase) {
			char upFirst = Character.toUpperCase(first);

			if (skipQuotation) {
				Next:
				for (int i = fromIndex; i <= max; ++i) {
					// Look for first character.
					while(true) {
						char c = source.charAt(i);
						if (c == first || Character.toUpperCase(c) == upFirst) {
							break;
						} else if (c == '"' || c == '\'') {
							i = source.indexOf(c, i + 1);
							if (i == -1) {
								return -1;
							} else if (++i > max) {
								return -1;
							}
						} else if (++i > max) {
							return -1;
						}
					}

					// Found first character, now look at the rest of v2
					for (int j = i + 1, k = 1; k < targetCount; ++j, ++k) {
						if (source.charAt(j) != target.charAt(k)
								&& Character.toUpperCase(source.charAt(j)) != Character
										.toUpperCase(target.charAt(k))) {
							continue Next;
						}
					}

					// Found whole string.
					return i;
				}

				return -1;
			} else {
				Next:
				for (int i = fromIndex; i <= max; ++i) {
					// Look for first character.
					while (source.charAt(i) != first
							&& Character.toUpperCase(source.charAt(i)) != upFirst) {
						if (++i > max) {
							return -1;
						}
					}

					// Found first character, now look at the rest of v2
					for (int j = i + 1, k = 1; k < targetCount; ++j, ++k) {
						if (source.charAt(j) != target.charAt(k)
								&& Character.toUpperCase(source.charAt(j)) != Character
										.toUpperCase(target.charAt(k))) {
							continue Next;
						}
					}

					// Found whole string.
					return i;
				}

				return -1;
			}
		} else { // skipQuotationΪtrue
			Next:
			for (int i = fromIndex; i <= max; ++i) {
				// Look for first character.
				while(true) {
					char c = source.charAt(i);
					if (c == first) {
						break;
					} else if (c == '"' || c == '\'') {
						i = source.indexOf(c, i + 1);
						if (i == -1) {
							return -1;
						} else if (++i > max) {
							return -1;
						}
					} else if (++i > max) {
						return -1;
					}
				}

				// Found first character, now look at the rest of v2
				for (int j = i + 1, k = 1; k < targetCount; ++j, ++k) {
					if (source.charAt(j) != target.charAt(k)) {
						continue Next;
					}
				}

				// Found whole string.
				return i;
			}

			return -1;
		}
	}
	
	private static int lastIndexOf(String source, String target, int fromIndex, 
			boolean ignoreCase, boolean skipQuotation) {
		if (fromIndex < 0) {
			return -1;
		}
		
		int targetCount = target.length();
		int sourceCount = source.length();
		int rightIndex = sourceCount - targetCount;
		
		if (fromIndex > rightIndex) {
			fromIndex = rightIndex;
		}
		
		if (targetCount == 0) {
			return fromIndex;
		}
		
		int lastIndex = targetCount - 1;
		char lastChar = target.charAt(lastIndex);
		int i = lastIndex + fromIndex;
		
		if (ignoreCase) {
			char upLast = Character.toUpperCase(lastChar);
			if (skipQuotation) {
		        startSearchForLastChar:
		        while (i >= lastIndex) {
		        	while(true) {
		        		char c = source.charAt(i);
						if (c == lastChar || Character.toUpperCase(c) == upLast) {
							break;
						} else if (c == '"' || c == '\'') {
							i = source.lastIndexOf(c, i - 1);
							if (i == -1) {
								return -1;
							} else if (--i < lastIndex) {
								return -1;
							}
						} else if (--i < lastIndex) {
							return -1;
						}
		        	}
		        	
		        	int j = i;
		        	int k = lastIndex;
					while (k > 0) {
						char s = source.charAt(--j);
						char t = target.charAt(--k);
						if (s != t && Character.toUpperCase(s) != Character.toUpperCase(t)) {
							--i;
							continue startSearchForLastChar;
						}
					}
					
		            return j;
		        }
		        
		        return -1;
			} else {
		        startSearchForLastChar:
		        while (i >= lastIndex) {
		        	while(true) {
		        		char c = source.charAt(i);
						if (c == lastChar || Character.toUpperCase(c) == upLast) {
							break;
						} else if (--i < lastIndex) {
							return -1;
						}
		        	}
		        	
		        	int j = i;
		        	int k = lastIndex;
					while (k > 0) {
						char s = source.charAt(--j);
						char t = target.charAt(--k);
						if (s != t && Character.toUpperCase(s) != Character.toUpperCase(t)) {
							--i;
							continue startSearchForLastChar;
						}
					}
					
		            return j;
		        }
		        
		        return -1;
			}
		} else { // skipQuotationΪtrue
	        startSearchForLastChar:
	        while (i >= lastIndex) {
	        	while(true) {
	        		char c = source.charAt(i);
					if (c == lastChar) {
						break;
					} else if (c == '"' || c == '\'') {
						i = source.lastIndexOf(c, i - 1);
						if (i == -1) {
							return -1;
						} else if (--i < lastIndex) {
							return -1;
						}
					} else if (--i < lastIndex) {
						return -1;
					}
	        	}
	        	
	        	int j = i;
	        	int k = lastIndex;
				while (k > 0) {
					if (source.charAt(--j) != target.charAt(--k)) {
						--i;
						continue startSearchForLastChar;
					}
				}
				
	            return j;
	        }
	        
	        return -1;
		}
	}
	
	/**
	 * �����Ӵ���λ�ã����Դ�Сд
	 * @param source Դ��
	 * @param target Ŀ���Ӵ�
	 * @param fromIndex Դ������ʼ����λ��
	 * @return λ�ã��Ҳ�������-1
	 */
	public static int indexOfIgnoreCase(String source, String target, int fromIndex) {
		int sourceCount = source.length();
		int targetCount = target.length();
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}

		if (fromIndex < 0) {
			fromIndex = 0;
		}

		if (targetCount == 0) {
			return fromIndex;
		}

		char first = target.charAt(0);
		char upFirst = Character.toUpperCase(first);
		int max = sourceCount - targetCount;

		Next:
		for (int i = fromIndex; i <= max; ++i) {
			// Look for first character.
			while (source.charAt(i) != first
					&& Character.toUpperCase(source.charAt(i)) != upFirst) {
				if (++i > max) {
					return -1;
				}
			}

			// Found first character, now look at the rest of v2
			for (int j = i + 1, k = 1; k < targetCount; ++j, ++k) {
				if (source.charAt(j) != target.charAt(k)
						&& Character.toUpperCase(source.charAt(j)) != Character
								.toUpperCase(target.charAt(k))) {
					continue Next;
				}
			}

			// Found whole string.
			return i;
		}

		return -1;
	}

	/**
	 *   ������ǰ׺pre����һ������֪���ַ�ΧexistsNames�ڵ�Ψһ������
	 * @param pre ����ǰ׺
	 * @param existsNames ��֪�����ַ�Χ
	 * @return ��֪��Χ��Ψһ��������
	 */
	public static String getNewName(String pre,String[] existsNames) {
		ArrayList<String> names = new ArrayList<String>();
		if(existsNames!=null) {
			int size = existsNames.length;
			for(int i=0;i<size; i++) {
				names.add( existsNames[i]);
			}
		}
		return getNewName( pre, names);
	}
	/**
	 * ��ǰ׺pre���������е�names������һ���µĲ��ظ�������
	 * @param pre ǰ׺
	 * @param names ���е������б�
	 * @return
	 */
	public static String getNewName(String pre, ArrayList<String> names) {
		if (names == null) {
			names = new ArrayList<String>();
		}
		if (!names.contains(pre)) {
			return pre;
		}
		int index = 1;
		while (names.contains(pre + index)) {
			index++;
		}
		return pre + index;
	}

	public final static boolean matches(byte[] value, int pos1, int len1, byte[] fmt) {
		if (value == null || fmt == null) {
			return false;
		}
		return matches(value, pos1, len1, fmt, 0);
	}
	
	//fastģʽ����������\
	public final static boolean matches_fast(byte[] value, int pos1, int len1, byte[] fmt) {
		if (value == null || fmt == null) {
			return false;
		}
		return matches_fast(value, pos1, len1, fmt, 0);
	}
	
	private final static boolean matches_fast(byte[] src, int pos1, int len1, byte[] fmt, int pos2) {
		int len2 = fmt.length;
		boolean any = false; // �Ƿ����Ǻ�

		while (pos2 < len2) {
			byte ch = fmt[pos2];
			if (ch == '*') {
				pos2++;
				any = true;
			} else if (any) {
				// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*

				while (pos1 < len1) {
					// �ҵ��׸�ƥ����ַ�
					if (src[pos1++] == ch) {
						// �ж�ʣ�µĴ��Ƿ�ƥ�䣬�����ƥ������Դ��һ���ַ����ʽ������ƥ��
						if (matches_fast(src, pos1, len1, fmt, pos2 + 1)) {
							return true;
						}
					}
				}

				return false;
			} else {

				// Դ����ǰ�ַ���Ҫ���ʽ����ǰ�ַ�ƥ��
				if (pos1 == len1 || src[pos1++] != ch) {
					return false;
				}

				for (++pos2; pos2 < len2;) {
					ch = fmt[pos2];
					if (ch == '*') {
						any = true;
						pos2++;
						break;
					} else {

						if (pos1 == len1 || src[pos1++] != ch) {
							return false;
						}

						pos2++;
					}
				}
			}
		}

		return any || pos1 == len1;
	}
	
	private final static boolean matches(byte[] src, int pos1, int len1, byte[] fmt, int pos2) {
		int len2 = fmt.length;
		boolean any = false; // �Ƿ����Ǻ�

		while (pos2 < len2) {
			byte ch = fmt[pos2];
			if (ch == '*') {
				pos2++;
				any = true;
			} else if (ch == '?') {
				// Դ����Ҫ������һ���ַ���?ƥ��
				if (++pos1 > len1) {
					return false;
				}

				pos2++;
			} else if (any) {
				// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
				if (ch == '\\' && pos2 + 1 < len2) {
					byte c = fmt[pos2 + 1];
					if (c == '*' || c == '?') {
						ch = c;
						pos2++;
					}
				}

				while (pos1 < len1) {
					// �ҵ��׸�ƥ����ַ�
					if (src[pos1++] == ch) {
						// �ж�ʣ�µĴ��Ƿ�ƥ�䣬�����ƥ������Դ��һ���ַ����ʽ������ƥ��
						if (matches(src, pos1, len1, fmt, pos2 + 1)) {
							return true;
						}
					}
				}

				return false;
			} else {
				// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
				if (ch == '\\' && pos2 + 1 < len2) {
					byte c = fmt[pos2 + 1];
					if (c == '*' || c == '?') {
						ch = c;
						pos2++;
					}
				}

				// Դ����ǰ�ַ���Ҫ���ʽ����ǰ�ַ�ƥ��
				if (pos1 == len1 || src[pos1++] != ch) {
					return false;
				}

				for (++pos2; pos2 < len2;) {
					ch = fmt[pos2];
					if (ch == '*') {
						any = true;
						pos2++;
						break;
					} else if (ch == '?') {
						if (++pos1 > len1) {
							return false;
						}

						pos2++;
					} else {
						// \* ��ʾ��λ����Ҫ�ַ�'*'��������ͨ���*
						if (ch == '\\' && pos2 + 1 < len2) {
							byte c = fmt[pos2 + 1];
							if (c == '*' || c == '?') {
								ch = c;
								pos2++;
							}
						}

						if (pos1 == len1 || src[pos1++] != ch) {
							return false;
						}

						pos2++;
					}
				}
			}
		}

		return any || pos1 == len1;
	}
	
	public static boolean isAssicString(String str) {
		if (str == null) return true;
		int len = str.length();
		if (len == 0) return true;
		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);
			if (ch < 0 || ch >= 0x80)
				return false;
		}
		return true;
	}
}
