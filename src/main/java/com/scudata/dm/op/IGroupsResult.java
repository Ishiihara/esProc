package com.scudata.dm.op;

import com.scudata.array.IntArray;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.GroupsSyncReader;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;

/**
 * �ڴ������ܽ������
 * @author RunQian
 *
 */
abstract public class IGroupsResult implements IResult {
	/**
	 * ���ݲ���ȡ�ڴ������ܴ�����
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param srcDs Դ���ݽṹ�����ڰ�#n�����Ӧ�ֶ���
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return IGroupsResult
	 */
	public static IGroupsResult instance(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, DataStruct srcDs, String opt, Context ctx) {
		if (exps != null) {
			int count = exps.length;
			if (names == null) {
				names = new String[count];
			}
			
			for (int i = 0; i < count; ++i) {
				if (names[i] == null || names[i].length() == 0) {
					names[i] = exps[i].getFieldName(srcDs);
				}
			}
		}

		if (calcExps != null) {			
			int valCount = calcExps.length;
			if (calcNames == null) {
				calcNames = new String[valCount];
			}
			
			for (int i = 0; i < valCount; ++i) {
				if (calcNames[i] == null || calcNames[i].length() == 0) {
					calcNames[i] = calcExps[i].getFieldName(srcDs);
				}
			}
		}
		
		boolean XOpt = false;
		if (opt != null && opt.indexOf('X') != -1)
			XOpt = true;
		if (exps != null && exps.length == 1 && !XOpt) {
			String gname = names == null ? null : names[0];
			return new Groups1Result(exps[0], gname, calcExps, calcNames, opt, ctx);
		} else {
			return new GroupsResult(exps, names, calcExps, calcNames, opt, ctx);
		}
	}
	
	/**
	 * ���ݲ���ȡ�ڴ������ܴ�����
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param srcDs Դ���ݽṹ�����ڰ�#n�����Ӧ�ֶ���
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @param capacity ���ڷ�������Ĺ�ϣ������
	 * @return IGroupsResult
	 */
	public static IGroupsResult instance(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, DataStruct srcDs, String opt, Context ctx, int capacity) {
		if (exps != null) {
			int count = exps.length;
			if (names == null) {
				names = new String[count];
			}
			
			for (int i = 0; i < count; ++i) {
				if (names[i] == null || names[i].length() == 0) {
					names[i] = exps[i].getFieldName(srcDs);
				}
			}
		}

		if (calcExps != null) {			
			int valCount = calcExps.length;
			if (calcNames == null) {
				calcNames = new String[valCount];
			}
			
			for (int i = 0; i < valCount; ++i) {
				if (calcNames[i] == null || calcNames[i].length() == 0) {
					calcNames[i] = calcExps[i].getFieldName(srcDs);
				}
			}
		}
		
		if (exps != null && exps.length == 1) {
			String gname = names == null ? null : names[0];
			return new Groups1Result(exps[0], gname, calcExps, calcNames, opt, ctx, capacity);
		} else {
			return new GroupsResult(exps, names, calcExps, calcNames, opt, ctx, capacity);
		}
	}
	
	/**
	 * ȡ������ʽ
	 * @return ���ʽ����
	 */
	abstract public Expression[] getExps();

	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	abstract public String[] getNames();

	/**
	 * ȡ���ܱ��ʽ
	 * @return ���ʽ����
	 */
	abstract public Expression[] getCalcExps();

	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	abstract public String[] getCalcNames();

	/**
	 * ȡѡ��
	 * @return
	 */
	abstract public String getOption();
	
	/**
	 * ȡ�Ƿ����������
	 * @return true���ǣ����ݰ������ֶ�����false������
	 */
	abstract public boolean isSortedGroup();

	/**
	 * ȡ���λ��ܱ��ʽ�����ڶ��̷߳���
	 * @return
	 */
	abstract public Expression[] getRegatherExpressions();
	
	/**
	 * ȡ���λ������ݽṹ
	 * @return DataStruct
	 */
	abstract public DataStruct getRegatherDataStruct();
	
	/**
	 * ȡ���λ��ܺ����ڼ������ս���ı��ʽ��avg���ܱ��ֳ�sum��count���н��м���
	 * @return
	 */
	abstract public Expression[] getResultExpressions();

	/**
	 * ȡ��������ݽṹ
	 * @return DataStruct
	 */
	abstract public DataStruct getResultDataStruct();
	
	/**
	 * ��������ʱ��ȡ��ÿ���̵߳��м������������Ҫ���ж��λ���
	 * @return Table
	 */
	abstract public Table getTempResult();
	
	/**
	 * ȡ������ܽ��
	 * @return Table
	 */
	abstract public Table getResultTable();
	
	 /**
	  * �������ͽ�����ȡ���յļ�����
	  * @return
	  */
	public Object result() {
		return getResultTable();
	}
	
	/**
	 * �������͹��������ݣ��ۻ������յĽ����
	 * @param seq ����
	 * @param ctx ����������
	 */
	abstract public void push(Sequence table, Context ctx);

	/**
	 * �������͹������α����ݣ��ۻ������յĽ����
	 * @param cursor �α�����
	 */
	abstract public void push(ICursor cursor);

	/**
	 * �������͹������α����ݣ��ۻ������յĽ����
	 * @param reader �α�����
	 */
	public void push(GroupsSyncReader reader, int hashStart, int hashEnd) {
		throw new RuntimeException();
	}
	
	public void push(Sequence table, Object key, IntArray hashValue, int hashStart, int hashEnd) {
		throw new RuntimeException();
	}
	
	/**
	 * ���÷�������@zѡ��ʹ��
	 * @param groupCount
	 */
	public void setCapacity(int capacity) {
	}
	
	/**
	 * ���÷�������@nѡ��ʹ��
	 * @param groupCount
	 */
	abstract public void setGroupCount(int groupCount);
	
	/**
	 * ��·����ʱ�԰�����·���������ϲ����ж��η�����ܣ��õ����յĻ��ܽ��
	 * @param results ����·�ķ��������ɵ�����
	 * @return ���յĻ��ܽ��
	 */
	abstract public Object combineResult(Object []results);
	
	/**
	 * ��·�α겢�з�����ɺ���кϲ����������շ�����
	 * @param others
	 * @param ctx ����������
	 * @return
	 */
	public Table combineGroupsResult(IGroupsResult []others, Context ctx) {
		Table result = getTempResult();
		for (IGroupsResult other : others) {
			if (result == null) {
				result = other.getTempResult();
			} else {
				result.addAll(other.getTempResult());
			}
		}
		
		if (result == null || result.length() == 0) {
			return result;
		}
		
		// ���ɶ��η�����ܱ��ʽ��avg���ܱ��ֳ�sum��count���н��м���
		Expression []valExps = getRegatherExpressions();
		DataStruct tempDs = getRegatherDataStruct();
		int tempFieldCount = tempDs.getFieldCount();
		
		int keyCount;
		if (valExps != null) {
			keyCount = tempFieldCount - valExps.length;
		} else {
			keyCount = tempFieldCount;
		}
		
		// ���ɶ��η��������ʽ
		Expression []keyExps = null;
		String []names = null;
		if (keyCount > 0) {
			keyExps = new Expression[keyCount];
			names = new String[keyCount];
			for (int i = 0, q = 1; i < keyCount; ++i, ++q) {
				keyExps[i] = new Expression(ctx, "#" + q);
				names[i] = tempDs.getFieldName(i);
			}
		}

		String []calcNames = null;
		if (tempFieldCount > keyCount) {
			int gatherCount = tempFieldCount - keyCount;
			calcNames = new String[gatherCount];
			for (int i = 0; i < gatherCount; ++i) {
				calcNames[i] = tempDs.getFieldName(keyCount + i);
			}
		}
		
		// ���ж��η���
		result = result.groups(keyExps, names, valExps, calcNames, getOption(), ctx);
		Expression []newExps = getResultExpressions();
		if (newExps != null) {
			return result.newTable(getResultDataStruct(), newExps, null, ctx);
		} else {
			return result;
		}
	}
}
