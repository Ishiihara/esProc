package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;

/**
 * �α��ܵ��ĸ��ӵĲ��������㴦����
 * cs.new(...)
 * @author RunQian
 *
 */
public class New extends Operation  {
	private Expression []newExps; // �ֶα��ʽ����
	private String []names; // �ֶ�������
	private String opt; // ѡ��
	private DataStruct newDs; // �ṹ�����ݽṹ
	
	public New(Expression []newExps, String []names, String opt) {
		this(null, newExps, names, opt);
	}
	
	public New(Function function, Expression []newExps, String []names, String opt) {
		super(function);
		int colCount = newExps.length;
		if (names == null) names = new String[colCount];
		
		this.newExps = newExps;
		this.names = names;
		this.opt = opt;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression []dupExps = dupExpressions(newExps, ctx);
		return new New(function, dupExps, names, opt);
	}

	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		if (newDs == null) {
			seq.getNewFieldNames(newExps, names, "new");
			newDs = new DataStruct(names);
			
			// ����Ƿ���Լ̳�����
			DataStruct ds = seq.dataStruct();
			if (ds != null && ds.getPrimary() != null) {
				String []keyNames = ds.getPrimary();
				int keyCount = keyNames.length;
				String []newKeyNames = new String[keyCount];
				int fcount = newExps.length;
				
				Next:
				for (int i = 0; i < keyCount; ++i) {
					String keyName = keyNames[i];
					for (int f = 0; f < fcount; ++f) {
						String fname = newExps[f].getFieldName(ds);
						if (keyName.equals(fname)) {
							newKeyNames[i] = names[f];
							continue Next;
						}
					}
					
					newKeyNames = null;
					break;
				}
				
				if (newKeyNames != null) {
					newDs.setPrimary(newKeyNames);
				}
			}
		}
		
		return seq.newTable(newDs, newExps, opt, ctx);
	}
}
