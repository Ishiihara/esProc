package com.scudata.dm.cursor;

import java.io.IOException;
import java.util.Arrays;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BFileReader;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.resources.EngineMessage;

/**
 * �������ļ�¼�ڼ��ļ��е�λ�ù����α�
 * @author RunQian
 *
 */
public class PFileCursor extends ICursor {
	private FileObject fo; // ���ļ�����
	private long []pos; // ��¼�ڼ��ļ��е�λ��
	private int bufSize; // ���ļ��õĻ�������С
	private String []selFields; // ѡ���ֶ�
	
	private BFileReader reader; // ���ļ���ȡ��
	private DataStruct ds; // Դ�ļ����ݽṹ
	private DataStruct selDs; // ��������ݽṹ
	private int []selIndex; // Դ�ṹ��ÿ���ֶ��ڽ�����ṹ�е����
	private int index = 0; // ��ǰҪ���ļ�¼�����
	
	// ���skip��������Ҫ����
	private boolean isSorted = false; // λ�������Ƿ�������
	private boolean isEnd = false; // �Ƿ�ȡ������
	private boolean isExist = true; // �ֶ��Ƿ����ļ���
	
	/**
	 * ������ȡָ��λ�ü�¼���α�
	 * @param fo ���ļ�����
	 * @param pos ��¼λ������
	 * @param bufSize ���ļ��õĻ������Ĵ�С
	 * @param fields ѡ���ֶ�������
	 * @param opt ѡ�u����¼λ������û�а��մ�С��������Ĭ�����ź����
	 * @param ctx ����������
	 */
	public PFileCursor(FileObject fo, long []pos, int bufSize, String []fields, String opt, Context ctx) {
		this.fo = fo;
		this.pos = pos;
		this.bufSize = bufSize;
		this.selFields = fields;
		this.ctx = ctx;
		
		if (opt != null) {
			if (opt.indexOf('u') != -1) isSorted = true;
			if (opt.indexOf('e') != -1) isExist = false;
		}
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (n < 1) return null;
		
		BFileReader reader = open();
		if (reader == null) return null;
		
		long []pos = this.pos;
		int index = this.index;
		int rest = pos.length - index;
		if (rest < 1) {
			return null;
		}
		
		if (rest <= n) {
			n = rest;
		}
		
		try {
			Table table;
			if (selFields == null) {
				int fcount = ds.getFieldCount();
				table = new Table(ds, n);
				Object []values = new Object[fcount];
				
				for (int i = 0; i < n; ++i, ++index) {
					reader.seek(pos[index]);
					reader.readRecord(values);
					table.newLast(values);
				}
			} else {
				int []selIndex = this.selIndex;
				table = new Table(selDs, n);
				Object []values = new Object[selDs.getFieldCount()];
				
				for (int i = 0; i < n; ++i, ++index) {
					reader.seek(pos[index]);
					reader.readRecord(selIndex, values);
					table.newLast(values);
				}
			}
			
			this.index += n;
			return table;
		} catch (Exception e) {
			close();
			throw new RQException(e.getMessage(), e);
		}
	}

	private BFileReader open() {
		if (reader != null) return reader;
		if (isEnd) return null;

		try {
			if (!isSorted) {
				Arrays.sort(pos);
				isSorted = true;
			}
			
			reader = new BFileReader(fo);
			reader.open(bufSize);
			ds = reader.getFileDataStruct();
			if (ctx != null) {
				ctx.addResource(this);
			}

			if (selFields != null) {
				int fcount = ds.getFieldCount();
				selIndex = new int[fcount];
				for (int i = 0; i < fcount; ++i) {
					selIndex[i] = -1;
				}

				for (int i = 0, count = selFields.length; i < count; ++i) {
					int q = ds.getFieldIndex(selFields[i]);
					if (q >= 0) {
						if (selIndex[q] != -1) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(selFields[i] + mm.getMessage("ds.colNameRepeat"));
						}
	
						selIndex[q] = i;
						selFields[i] = ds.getFieldName(q);
					} else if (isExist) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(selFields[i] + mm.getMessage("ds.fieldNotExist"));
					}
				}

				selDs = new DataStruct(selFields);
				setDataStruct(selDs);
			} else {
				setDataStruct(ds);
			}

			return reader;
		} catch (IOException e) {
			close();
			throw new RQException(e.getMessage(), e);
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (n < 1) {
			return 0;
		}
		
		int len = pos.length;
		if (len - index > n) {
			if (!isSorted) {
				Arrays.sort(pos);
				isSorted = true;
			}
			
			index += n;
		} else {
			n = len - index;
			index = len;
			close();
		}

		return n;
	}
	
	/**
	 * �ر��α�
	 */
	public void close() {
		super.close();
		
		isEnd = true;
		if (reader != null) {
			if (ctx != null) ctx.removeResource(this);
			try {
				reader.close();
			} catch (IOException e) {
			}
			
			reader = null;
		}
		
		ds = null;
		selDs = null;
	}

	protected void finalize() throws Throwable {
		close();
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		index = 0;
		isEnd = false;
		return true;
	}
}
