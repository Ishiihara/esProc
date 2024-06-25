package com.scudata.dm;

/**
 * ��������Ŀ�õ���������ͱ�ʶ��
 * @author WangXiaoJun
 *
 */
public final class KeyWord {
	public static final String SUBCODEBLOCK = "??"; // �����Ե�ǰ��Ϊ����Ĵ����
	public static final String ARGPREFIX = "?"; // ����ǰ׺��eval�����ı��ʽ������?1��?2������ʽ���ò���
	public static final char OPTION = '@'; // ����ѡ��ָ���

	public static final String CURRENTELEMENT = "~"; // ���е�ǰԪ�ر�ʶ��
	public static final String CURRENTSEQ = "#"; // ���еĵ�ǰѭ�����
	public static final String ITERATEPARAM = "~~"; // ��������������iterate���������õ���ֵ
	public static final String FIELDIDPREFIX = "#"; // ����������ֶ�#1, #2...
	public static final String CURRENTCELL = "@"; // ��ǰ��
	public static final String CURRENTCELLSEQ = "#@"; // ��ǰ��ѭ�����

	public static final char CELLPREFIX = '#'; // #A1 #C ��Ԫ��ǰ׺
	public static final String CONSTSTRINGPREFIX = "'"; // �����ַ���ǰ׺

	/**
	 * �ж�ָ���ַ��Ƿ��ǿհ׻���������ȷ���
	 * @param c �ַ�
	 * @return true���ǣ�false������
	 */
	public static boolean isSymbol(char c) {
		return (Character.isWhitespace(c) ||
			c == '+' || c == '-' || c == '*' || c == '/' || c == '%' ||
			c == '=' || c == '&' || c == '|' || c == '!' || c == '\\' ||
			c == ',' || c == '>' || c == '<' || c == '(' || c == ')' ||
			c == '[' || c == ']' || c == ':' || c == '{' || c == '}' ||
			c == '^' || c == '.' || c == '"' || c == '\'' || c == ';');
	}

	/**
	 * �жϱ�ʶ���Ƿ����Ӵ�����ʶ��
	 * @param id ��ʶ��
	 * @return true���ǣ�false������
	 */
	public static boolean isSubCodeBlock(String id) {
		return SUBCODEBLOCK.equals(id);
	}
	
	/**
	 * �жϱ�ʶ���Ƿ������еĵ�ǰԪ��
	 * @param id ��ʶ
	 * @return true���ǣ�false������
	 */
	public static boolean isCurrentElement(String id) {
		return CURRENTELEMENT.equals(id);
	}
	
	/**
	 * �жϱ�ʶ���Ƿ������еĵ�ǰѭ�����
	 * @param id ��ʶ
	 * @return true���ǣ�false������
	 */
	public static boolean isCurrentSeq(String id) {
		return CURRENTSEQ.equals(id);
	}
	
	/**
	 * �жϱ�ʶ���Ƿ��ǵ�ǰ��
	 * @param id ��ʶ
	 * @return true���ǣ�false������
	 */
	public static boolean isCurrentCell(String id) {
		return CURRENTCELL.equals(id);
	}
	
	/**
	 * �жϱ�ʶ���Ƿ��ǵ�ǰ��ѭ�����
	 * @param id ��ʶ
	 * @return true���ǣ�false������
	 */
	public static boolean isCurrentCellSeq(String id) {
		return CURRENTCELLSEQ.equals(id);
	}
	
	/**
	 * �жϱ�ʶ���Ƿ��ǵ�������
	 * @param id ��ʶ
	 * @return true���ǣ�false������
	 */
	public static boolean isIterateParam(String id) {
		return ITERATEPARAM.equals(id);
	}

	/**
	 * �жϱ�ʶ���Ƿ����ֶε���ŷ�����
	 * @param id ��ʶ
	 * @return true���ǣ�false������
	 */
	public static boolean isFieldId(String id) {
		if (id == null || id.length() < 2 || !id.startsWith(FIELDIDPREFIX)) {
			return false;
		}
		
		for (int i = 1, len = id.length(); i < len; ++i) {
			char c = id.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * �жϱ�ʶ���Ƿ��ǵ�ǰ���г�Ա���ã�~n��
	 * @param id ��ʶ
	 * @return true���ǣ�false������
	 */
	public static boolean isElementId(String id) {
		if (id == null || id.length() < 2 || !id.startsWith(CURRENTELEMENT)) {
			return false;
		}
		
		for (int i = 1, len = id.length(); i < len; ++i) {
			char c = id.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * ȡ�ֶε���ţ�id������#1�������ַ���
	 * @param id �ֶε�#n��ʾ�Ĵ�
	 * @return �ֶ����
	 */
	public static int getFiledId(String id) {
		return Integer.parseInt(id.substring(FIELDIDPREFIX.length()));
	}

	/**
	 * �жϱ�ʶ���Ƿ��ǲ���������������?���ã�Ҳ������?1��?2����ָ�����õڼ�������
	 * @param id ��ʶ��
	 * @return true���ǣ�false������
	 */
	public static boolean isArg(String id) {
		if (id == null || !id.startsWith(ARGPREFIX)) {
			return false;
		}
		
		for (int i = 1, len = id.length(); i < len; ++i) {
			char c = id.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * ɨ���ʶ�������ؽ���λ��
	 * @param expStr ���ʽ�ַ���
	 * @param start ��ʼɨ��λ�ã�������
	 * @return int ��ʶ���Ľ���λ�ã���������
	 */
	public static int scanId(String expStr, int start) {
		int len = expStr.length();
		for (; start < len; ++start) {
			if (isSymbol(expStr.charAt(start))) {
				break;
			}
		}

		return start;
	}

	/**
	 * ���ַ����еĺ���ѡ��ֲ������Դ�����Ų��ѡ���Ĵ���ѡ����Ϊ����ֵ����
	 * @param strs �ַ�������
	 * @return ѡ������
	 */
	public static String[] parseStringOptions(String []strs) {
		int size = strs.length;
		String []opts = new String[size];

		for (int i = 0; i < size; ++i) {
			String tmp = strs[i];
			if (tmp != null) {
				int optIndex = tmp.indexOf(OPTION);
				if (optIndex != -1) {
					strs[i] = tmp.substring(0, optIndex);
					opts[i] = tmp.substring(optIndex + 1);
				}
			}
		}

		return opts;
	}
}
