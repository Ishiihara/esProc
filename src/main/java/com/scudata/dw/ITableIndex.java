package com.scudata.dw;

import com.scudata.dm.Context;
import com.scudata.dm.LongArray;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;

/**
 * ��������ӿ���
 * @author runqian
 *
 */
public interface ITableIndex {
	public static final String INDEX_FIELD_NAMES[] = { "name", "hash", "keys", "field", "where" };
	public static final String INDEX_FIELD_NAMES2[] = { "hash", "keys", "field", "where" };
	public static final int TEMP_FILE_SIZE = 50 * 1024 * 1024;//����ʱ�Ļ����ļ���С
	public static int MIN_ICURSOR_REC_COUNT = 1000;//��С�����ֵʱ���ٽ��н��������ǿ�ʼ����
	public static int MIN_ICURSOR_BLOCK_COUNT = 10;//��С�����ֵʱ���ٽ��н��������ǿ�ʼ����
	public ICursor select(Expression exp, String []fields, String opt, Context ctx);
	public LongArray select(Expression exp, String opt, Context ctx);
	
	/**
	 * ��ȡ������������Ϣ���ڴ�
	 */
	public void loadAllBlockInfo();
	
	/**
	 * ��ȡ������������Ϣ���ڴ棨��������
	 */
	public void loadAllKeys();
	
	/**
	 * �ͷ��ڴ����������Ϣ
	 */
	public void unloadAllBlockInfo();
	
	/**
	 * ��������������
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * ����ȡ���ֶκ������ֶ�
	 * @param ifields �����ֶ�
	 * @param vfields ȡ���ֶ�
	 */
	public void setFields(String[] ifields, String[] vfields);
	
	/**
	 * �д�ʱ����ȡ��ʱ�����size���д�ʱ������
	 * @return
	 */
	public int getMaxRecordLen();
	
	/**
	 * �Ƿ���ڵڶ���������
	 * @return
	 */
	public boolean hasSecIndex();
	
	/**
	 * ����һ����¼��Ӧ�ĵ�ַ����
	 * һ�㶼��1��ֻ�ڸ���ʱ�ſ��ܶ��
	 * @return
	 */
	public int getPositionCount();
	
	/**
	 * ��������Ϣд����һ���±�table
	 * @param table
	 */
	public void dup(PhyTable table);
	
	/**
	 * ��������Ľṹ��Ϣ
	 * @return
	 */
	public Object getIndexStruct();
}