package com.scudata.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import com.scudata.array.IArray;
import com.scudata.common.Escape;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Env;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;

//import org.jdom.input.SAXBuilder;
//import org.jdom.Document;
//import org.jdom.Element;
//import org.jdom.Attribute;

/**
 * ���ڰ�����ת��XML��ʽ�����߰�XML��ʽ����������
 * @author RunQian
 *
 */
final public class XMLUtil {
	private static final String ID_Table = "xml"; // ��Ĭ�ϵı�ǩ��
	private static final String ID_Row = "row"; // ��¼Ĭ�ϵı�ǩ��

	private static AttributesImpl attr = new AttributesImpl();

	// �ַ���ת�������obj�������ַ��������˫����
	private static String toTextNodeString(Object obj) {
		if (obj == null) {
			return ""; //"null";
		} else if (obj instanceof String) {
			return Escape.addEscAndQuote((String)obj);
		} else if (obj instanceof Sequence) {
			IArray mems = ((Sequence)obj).getMems();
			StringBuffer sb = new StringBuffer(1024);
			sb.append('[');
			
			for (int i = 1, len = mems.size(); i <= len; ++i) {
				if (i > 1) sb.append(',');
				sb.append(toTextNodeString(mems.get(i)));
			}

			sb.append(']');
			
			return sb.toString();
		} else {
			return Variant.toString(obj);
		}
	}
	
	// �ж��Ƿ�����ŷָ�����֣����磺1,234.56
	private static String convertNumber(String text) {
		if (text == null || text.length() == 0) {
			return null;
		}
		
		int len = text.length();
		char []chars = new char[len];
		int index = 0;
		boolean hasComma = false; // ȥ������
		
		for (int i = 0; i < len; ++i) {
			char c = text.charAt(i);
			if (c >= '0' && c <= '9') {
				chars[index++] = c;
			} else if (c == ',') {
				hasComma = true;
			} else if (c == '.' || c == '%') {
				chars[index++] = c;
			} else {
				return null;
			}
		}
		
		if (hasComma) {
			return new String(chars, 0, index);
		} else {
			return null;
		}
	}
	
	/**
	 * �����ı�����ֵ
	 * @param text �ı���
	 * @return
	 */
	public static Object parseText(String text) {
		// ���������ŵ����ִ������磺7,531.04
		String strNum = convertNumber(text);
		if (strNum != null) {
			Object value = Variant.parse(strNum, true);
			if (value instanceof Number) {
				return value;
			}
		}
		
		return Variant.parse(text, true);
	}
	
	/**
	 * ��XML��ʽ�����ɶ���¼�����
	 * <>�ڵı�ʶ��Ϊ�ֶ������ظ���ͬ����ʶ����Ϊ���
	 * ������<K F=v F=v ��>D</K>��XML������Ϊ��K,F,��Ϊ�ֶεļ�¼��
	 * KȡֵΪD��D�Ƕ��XML����ʱ����Ϊ���У�<K ��./K>ʱD����Ϊnull��<K��></K>ʱD����Ϊ�մ�
	 * @param src XML��
	 * @param levels ���ʶ�������/�ָ�
	 * @return
	 */
	public static Object parseXml(String src, String levels) {
		return parseXml(src, levels, null);
	}
	
	/**
	 * ��XML��ʽ�����ɶ���¼�����
	 * <>�ڵı�ʶ��Ϊ�ֶ������ظ���ͬ����ʶ����Ϊ���
	 * ������<K F=v F=v ��>D</K>��XML������Ϊ��K,F,��Ϊ�ֶεļ�¼��
	 * KȡֵΪD��D�Ƕ��XML����ʱ����Ϊ���У�<K ��./K>ʱD����Ϊnull��<K��></K>ʱD����Ϊ�մ�
	 * @param src XML��
	 * @param levels ���ʶ�������/�ָ�
	 * @param opt ѡ�s����ȡ����ֵ
	 * @return
	 */
	public static Object parseXml(String src, String levels, String opt) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser saxParser = spf.newSAXParser();
			SAXTableHandler handler = new SAXTableHandler(opt);
			
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(handler);
			StringReader reader = new StringReader(src);
			xmlReader.parse(new InputSource(reader));
			
			Object table = handler.getResult(parseLevels(levels));
			return table;
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}

	/**
	 * �����б��XML��ʽ��
	 * @param sequence ����
	 * @param charset �ַ���
	 * @param levels ���ʶ����ʽΪ"TableName/RecordName"�������/�ָ������ʡ������"xml/row"
	 * @return
	 */
	public static String toXml(Sequence sequence, String charset, String levels) {
		if (charset == null || charset.length() == 0) {
			charset = Env.getDefaultCharsetName();
		}

		DataStruct ds = sequence.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}

		try {
			SAXTransformerFactory fac = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
			TransformerHandler handler = fac.newTransformerHandler();
			Transformer transformer = handler.getTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, charset);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			
			StringWriter writer = new StringWriter(8192);
			Result resultxml = new StreamResult(writer);
			handler.setResult(resultxml);
			handler.startDocument();
			
			String []strs = parseLevels(levels);
			String idTable = ID_Row;
			int count = strs == null ? 0 : strs.length;
			
			if (count > 1) {
				idTable = strs[count - 1];
				for (int i = 0; i < count - 1; ++i) {
					handler.startElement("", "", strs[i], attr);
				}
			} else {
				if (count == 1) {
					handler.startElement("", "", strs[0], attr);
				} else {
					handler.startElement("", "", ID_Table, attr);
				}
			}
			
			toXml(handler, sequence, 0, idTable);
			
			if (count > 1) {
				for (int i = count - 2; i >= 0; --i) {
					handler.endElement("", "", strs[i]);
				}
			} else {
				if (count == 1) {
					handler.endElement("", "", strs[0]);
				} else {
					handler.endElement("", "", ID_Table);
				}
			}
			
			handler.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	/**
	 * �Ѽ�¼���XML��ʽ��
	 * @param r ��¼
	 * @param charset �ַ���
	 * @param levels ���ʶ�������/�ָ�
	 * @return
	 */
	public static String toXml(BaseRecord r, String charset, String levels) {
		if (charset == null || charset.length() == 0) {
			charset = Env.getDefaultCharsetName();
		}
		
		try {
			SAXTransformerFactory fac = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
			TransformerHandler handler = fac.newTransformerHandler();
			Transformer transformer = handler.getTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, charset);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			
			StringWriter writer = new StringWriter(8192);
			Result resultxml = new StreamResult(writer);
			handler.setResult(resultxml);
			handler.startDocument();
			
			String []strs = parseLevels(levels);
			int count = strs == null ? 0 : strs.length;
			for (int i = 0; i < count; ++i) {
				handler.startElement("", "", strs[i], attr);
			}
			
			toXml(handler, r, count);
			
			for (int i = 0; i < count; ++i) {
				handler.endElement("", "", strs[i]);
			}

			handler.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	private static void appendTab(TransformerHandler handler, int level) throws SAXException {
		// ����Ҫ���룿
		/*if (false) {
			StringBuffer sb = new StringBuffer(ENTER);
			for (int i = 0; i < level; i++) {
				sb.append(TAB);
			}
			
			String indent = sb.toString();
			handler.characters(indent.toCharArray(), 0, indent.length());
		}*/
	}
	
	private static void toXml(TransformerHandler handler, BaseRecord r, int level) throws SAXException {
		Object []vals = r.getFieldValues();
		String []names = r.getFieldNames();
		for (int f = 0, fcount = vals.length; f < fcount; ++f) {
			appendTab(handler, level);

			Object val = vals[f];
			if (val instanceof BaseRecord) {
				handler.startElement("", "", names[f], attr);
				toXml(handler, (BaseRecord)val, level + 1);
				handler.endElement("", "", names[f]);
			} else if (val instanceof Sequence && ((Sequence)val).isPurePmt()) {
				toXml(handler, (Sequence)val, level + 1, names[f]);
			} else {
				handler.startElement("", "", names[f], attr);
				String valStr = toTextNodeString(val);
				handler.characters(valStr.toCharArray(), 0, valStr.length());
				handler.endElement("", "", names[f]);
			}
		}
	}
	
	private static void toXml(TransformerHandler handler, Sequence table, int level, String idTable) throws SAXException {
		if (level > 0) appendTab(handler, level);
		
		IArray mems = table.getMems();
		for (int i = 1, len = mems.size(); i <= len; ++i) {
			handler.startElement("", "", idTable, attr);
			BaseRecord r = (BaseRecord)mems.get(i);
			toXml(handler, r, level + 1);
			handler.endElement("", "", idTable);
		}
	}
	
	private static String[] parseLevels(String levels) {
		if (levels == null || levels.length() == 0) {
			return null;
		}
	
		ArrayList<String> list = new ArrayList<String>();
		int s = 0;
		int len = levels.length();
		while (s < len) {
			int i = levels.indexOf('/', s);
			if (i < 0) {
				list.add(levels.substring(s));
				break;
			} else {
				list.add(levels.substring(s, i));
				s = i + 1;
			}
		}
		
		String []strs = new String[list.size()];
		list.toArray(strs);
		return strs;
	}
}
