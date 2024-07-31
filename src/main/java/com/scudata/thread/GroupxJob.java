package com.scudata.thread;

import java.io.IOException;
import java.util.TreeMap;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BFileWriter;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.op.IGroupsResult;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CursorUtil;

/**
 * ִ�����������������
 * @author RunQian
 *
 */
public class GroupxJob extends Job {
	private ICursor cursor; // �����α�
	private Expression gexp; // @gѡ��ʱ�ķֶα��ʽ
	private Expression[] exps; // �����ֶα��ʽ����
	private String[] names; // �����ֶ�������
	private Expression[] calcExps; // �����ֶα��ʽ����
	private String[] calcNames; // �����ֶ�������
	
	private Context ctx; // ����������
	private int fetchCount; // ÿ��ȡ��������
	private int capacity; // �ڴ��ܹ���ŵķ�����������������groupx@n
	
	private TreeMap<Object, BFileWriter> fileMap; // �����ֵ����ʱ���ļ�ӳ��
	
	// @gѡ��ʹ��
	public GroupxJob(ICursor cursor, Expression gexp, Expression[] exps, String[] names,
			Expression[] calcExps, String[] calcNames, Context ctx, 
			int fetchCount, TreeMap<Object, BFileWriter> fileMap) {
		this.cursor = cursor;
		this.gexp = gexp;
		this.exps = exps;
		this.names = names;
		this.calcExps = calcExps;
		this.calcNames = calcNames;
		this.ctx = ctx;
		this.fileMap = fileMap;
		
		if (fetchCount > ICursor.FETCHCOUNT) {
			this.fetchCount = fetchCount;
		} else {
			this.fetchCount = ICursor.FETCHCOUNT;
		}
	}

	// @nѡ��ʹ��
	public GroupxJob(ICursor cursor, Expression[] exps, String[] names,
			Expression[] calcExps, String[] calcNames, Context ctx, 
			int capacity, int fetchCount, TreeMap<Object, BFileWriter> fileMap) {
		this.cursor = cursor;
		this.exps = exps;
		this.names = names;
		this.calcExps = calcExps;
		this.calcNames = calcNames;
		this.ctx = ctx;
		this.fileMap = fileMap;
		this.capacity = capacity;
		if (fetchCount > ICursor.FETCHCOUNT) {
			this.fetchCount = fetchCount;
		} else {
			this.fetchCount = ICursor.FETCHCOUNT;
		}
	}
	
	private void groupx_g() {
		MessageManager mm = EngineMessage.get();
		String msg = mm.getMessage("engine.createTmpFile");
		ICursor cursor = this.cursor;
		Expression gexp = this.gexp;
		Expression[] exps = this.exps;
		String[] names = this.names;
		Expression[] calcExps = this.calcExps;
		String[] calcNames = this.calcNames;
		Context ctx = this.ctx;
		int fetchCount = this.fetchCount;
		TreeMap<Object, BFileWriter> fileMap = this.fileMap;
		DataStruct ds = cursor.getDataStruct();
		
		try {
			// �����α�����
			while (true) {
				Sequence seq = cursor.fetch(fetchCount);
				if (seq == null || seq.length() == 0) {
					break;
				}

				// ���������ʽ�����ݽ��з���
				Sequence groups = seq.group(gexp, null, ctx);
				int gcount = groups.length();
				for (int i = 1; i <= gcount; ++i) {
					// ��ÿ�����������״λ���
					Sequence group = (Sequence)groups.getMem(i);
					Object gval = group.calc(1, gexp, ctx);
					IGroupsResult gresult = IGroupsResult.instance(exps, names, calcExps, calcNames, ds, null, ctx);
					gresult.push(group, ctx);
					group = gresult.getTempResult();
					
					// ���ļ�ӳ����ͬ��ȡ����Ӧ����ʱ�ļ�
					BFileWriter writer = null;
					synchronized(fileMap) {
						writer = fileMap.get(gval);
						if (writer == null) {
							FileObject fo = FileObject.createTempFileObject();
							Logger.info(msg + fo.getFileName());
							writer = new BFileWriter(fo, null);
							writer.prepareWrite(gresult.getResultDataStruct(), false);
							fileMap.put(gval, writer);
						}
					}
					
					// ��ס��ʱ�ļ������״η�����д����ʱ�ļ���
					synchronized(writer) {
						writer.write(group);
					}
				}
			}
		} catch (IOException e) {
			throw new RQException(e);
		}
	}
	
	private void groupx_n() {
		MessageManager mm = EngineMessage.get();
		String msg = mm.getMessage("engine.createTmpFile");
		ICursor cursor = this.cursor;
		Expression[] exps = this.exps;
		String[] names = this.names;
		Expression[] calcExps = this.calcExps;
		String[] calcNames = this.calcNames;
		Context ctx = this.ctx;
		int capacity = this.capacity;
		int fetchCount = this.fetchCount;
		TreeMap<Object, BFileWriter> fileMap = this.fileMap;
		DataStruct ds = cursor.getDataStruct();
		
		try {
			// �����α�����
			while (true) {
				Sequence seq = cursor.fetch(fetchCount);
				if (seq == null || seq.length() == 0) {
					break;
				}

				// �����״λ��ܻ���
				IGroupsResult gresult = IGroupsResult.instance(exps, names, calcExps, calcNames, ds, null, ctx);
				gresult.push(seq, ctx);
				seq = gresult.getTempResult();
				
				Sequence groups = CursorUtil.group_n(seq, capacity);
				int gcount = groups.length();
				for (int i = 1; i <= gcount; ++i) {
					Sequence group = (Sequence)groups.getMem(i);
					if (group.length() == 0) {
						continue;
					}

					BaseRecord r = (BaseRecord)group.getMem(1);
					int index = ((Number)r.getNormalFieldValue(0)).intValue() / capacity + 1;
					Integer gval = new Integer(index);
					
					// ���ļ�ӳ����ͬ��ȡ����Ӧ����ʱ�ļ�
					BFileWriter writer = null;
					synchronized(fileMap) {
						writer = fileMap.get(gval);
						if (writer == null) {
							FileObject fo = FileObject.createTempFileObject();
							Logger.info(msg + fo.getFileName());
							writer = new BFileWriter(fo, null);
							writer.prepareWrite(gresult.getResultDataStruct(), false);
							fileMap.put(gval, writer);
						}
					}
					
					// ��ס��ʱ�ļ������״η�����д����ʱ�ļ���
					synchronized(writer) {
						writer.write(group);
					}
				}
			}
		} catch (IOException e) {
			throw new RQException(e);
		}
	}
	
	public void run() {
		if (gexp == null) {
			groupx_n();
		} else {
			groupx_g();
		}
	}
}
