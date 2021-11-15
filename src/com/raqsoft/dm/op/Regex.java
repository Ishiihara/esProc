package com.raqsoft.dm.op;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.DataStruct;
import com.raqsoft.dm.ListBase1;
import com.raqsoft.dm.Record;
import com.raqsoft.dm.Sequence;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.Function;
import com.raqsoft.resources.EngineMessage;

/**
 * �α��ܵ����ӵ�ģʽƥ�����㴦����
 * op.regex(rs,F) op���α��ܵ�
 * @author RunQian
 *
 */
public class Regex extends Operation {
	private Pattern pattern; // ģʽ
	private String []names; // �ֶ�������
	private Expression exp; // ��Ҫ��ƥ����ֶΣ�ȱʡ��~
	private DataStruct ds; // ��������ݽṹ�����ڿ�ʱ����Դ��¼

	public Regex(Pattern pattern, String []names, Expression exp) {
		this(null, pattern, names, exp);
	}
	
	public Regex(Function function, Pattern pattern, String []names, Expression exp) {
		super(function);
		this.pattern = pattern;
		this.names = names;
		this.exp = exp;
		
		if (names != null) {
			ds = new DataStruct(names);
		}
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ����ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression dupExp = dupExpression(exp, ctx);
		return new Regex(function, pattern, names, dupExp);
	}

	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		Pattern pattern = this.pattern;
		DataStruct ds = this.ds;
		int gcount = ds == null ? 0 : ds.getFieldCount();

		int len = seq.length();
		Sequence data = new Sequence(len);
		Sequence strs;
		if (exp == null) {
			strs = seq; // .fieldValues(0)
		} else {
			strs = seq.calc(exp, ctx);
		}
		
		ListBase1 strMems = strs.getMems();
		ListBase1 srcMems = seq.getMems();
		
		for (int i = 1; i <= len; ++i) {
			Object obj = strMems.get(i);
			if (obj instanceof String) {
				Matcher m = pattern.matcher((String)obj);
				if (m.find()) {
					if (ds == null) {
						data.add(srcMems.get(i));
					} else {
						Record r = new Record(ds);
						data.add(r);
						for (int g = 1; g <= gcount; ++g) {
							r.setNormalFieldValue(g - 1, m.group(g));
						}
					}
				}
			} else if (obj != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.needStringExp"));
			}
		}
					
		if (data.length() != 0) {
			return data;
		} else {
			return null;
		}
	}
}