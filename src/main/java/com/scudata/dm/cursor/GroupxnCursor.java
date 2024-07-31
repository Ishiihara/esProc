package com.scudata.dm.cursor;

import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.dm.op.IGroupsResult;
import com.scudata.expression.Expression;
import com.scudata.expression.Node;

/**
 * ����������cs.groupx@n()��cs.groupx@g()�Ķ��η���
 * groupx��@n��@gѡ��ʱ��ͬһ����м����ݻ��ŵ�ͬһ����ʱ�ļ�������ֻ���ÿ����ʱ�ļ��������¶��η�����ܵõ����յķ�����
 * @author RunQian
 *
 */
public class GroupxnCursor extends ICursor {
	private FileObject []files; // �״η����������ʱ���ļ�
	private int fileIndex = -1; // ��ǰҪ������ʱ�ļ�����
	private Expression[] exps; // ������ʽ����
	private String[] names; // �����ֶ�������
	private Expression[] calcExps; // ���ܱ��ʽ����
	private String[] calcNames; // �����ֶ�������
	
	private MemoryCursor cursor; // �ڴ��α꣬���ڱ��浱ǰ��ʱ�ļ��Ķ��η�����
	
	/**
	 * ����������Ķ��λ����α�
	 * @param files �״η����������ʱ���ļ�����
	 * @param exps ������ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps ���ܱ��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param ctx ����������
	 */
	public GroupxnCursor(FileObject []files, Expression[] exps, String[] names, 
			Expression[] calcExps, String[] calcNames, Context ctx) {
		this.files = files;
		this.names = names;
		this.calcNames = calcNames;
		this.ctx = ctx;
		
		int keyCount = exps.length;
		int valCount = calcExps == null ? 0 : calcExps.length;
		String[] colNames = new String[keyCount + valCount];
		System.arraycopy(names, 0, colNames, 0, keyCount);
		if (this.calcNames != null) {
			System.arraycopy(this.calcNames, 0, colNames, keyCount, valCount);
		}

		DataStruct ds = new DataStruct(colNames);
		ds.setPrimary(names);
		setDataStruct(ds);
		
		// ȡ���ξۺ���Ҫ�õı��ʽ
		Node[] gathers = Sequence.prepareGatherMethods(calcExps, ctx);
		Expression []keyExps = new Expression[keyCount];
		for (int i = 0, q = 1; i < keyCount; ++i, ++q) {
			keyExps[i] = new Expression(ctx, "#" + q);
		}
		
		Expression []valExps = new Expression[valCount];
		for (int i = 0, q = keyCount + 1; i < valCount; ++i, ++q) {
			valExps[i] = gathers[i].getRegatherExpression(q);
		}
		
		this.exps = keyExps;
		this.calcExps = valExps;
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	public void resetContext(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (files == null || n < 1) return null;
		
		if (fileIndex == -1) {
			fileIndex++;
			BFileCursor cs = new BFileCursor(files[0], null, "x", ctx);
			DataStruct ds = cs.getDataStruct();
			IGroupsResult groups = IGroupsResult.instance(exps, names, calcExps, calcNames, ds, "", ctx);
			groups.push(cs);
			Sequence seq = groups.getResultTable();
			cursor = new MemoryCursor(seq);
		}
		
		Sequence table = cursor.fetch(n);
		if (table == null || table.length() < n) {
			fileIndex++;
			if (fileIndex < files.length) {
				BFileCursor cs = new BFileCursor(files[fileIndex], null, "x", ctx);
				DataStruct ds = cs.getDataStruct();
				IGroupsResult groups = IGroupsResult.instance(exps, names, calcExps, calcNames, ds, "", ctx);
				groups.push(cs);
				Sequence seq = groups.getResultTable();
				cursor = new MemoryCursor(seq);
				
				if (table == null) {
					return get(n);
				} else {
					Sequence rest;
					if (n == MAXSIZE) {
						rest = get(n);
					} else {
						rest = get(n - table.length());
					}
					
					table = append(table, rest);
				}
			} else {
				files = null;
				cursor = null;
			}
		}

		return table;
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (files == null || n < 1) return 0;
		
		if (fileIndex == -1) {
			fileIndex++;
			BFileCursor cs = new BFileCursor(files[0], null, "x", ctx);
			DataStruct ds = cs.getDataStruct();
			IGroupsResult groups = IGroupsResult.instance(exps, names, calcExps, calcNames, ds, "", ctx);
			groups.push(cs);
			Sequence seq = groups.getResultTable();
			cursor = new MemoryCursor(seq);
		}

		long count = cursor.skip(n);
		if (count < n) {
			fileIndex++;
			if (fileIndex < files.length) {
				BFileCursor cs = new BFileCursor(files[fileIndex], null, "x", ctx);
				DataStruct ds = cs.getDataStruct();
				IGroupsResult groups = IGroupsResult.instance(exps, names, calcExps, calcNames, ds, "", ctx);
				groups.push(cs);
				Sequence seq = groups.getResultTable();
				cursor = new MemoryCursor(seq);
				
				count += skipOver(n - count);
			}
		}

		return count;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		
		if (files != null) {
			for (FileObject file : files) {
				if (file != null) {
					file.delete();
				}
			}
			
			files = null;
			cursor = null;
		}
	}
}
