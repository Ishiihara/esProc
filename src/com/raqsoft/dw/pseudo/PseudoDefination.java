package com.raqsoft.dw.pseudo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.raqsoft.dm.Context;
import com.raqsoft.dm.FileObject;
import com.raqsoft.dm.Record;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dw.GroupTable;
import com.raqsoft.dw.ITableMetaData;

//���ڶ������������
public class PseudoDefination {
	private static final String PD_FILE = "file";
	private static final String PD_ZONE = "zone";
	private static final String PD_DATE = "date";
	private static final String PD_USER = "user";
	private static final String PD_COLUMN = "column";
	
	private Object file;//�ļ��������ļ���������
	private Sequence zone;//����������б�
	private String date;//�����ֶ�
	private String user;//�ʻ��ֶ�
	private List<PseudoColumn> columns;//���������ֶζ���
	private List<ITableMetaData> tables;//�������ļ���table����
	
	public PseudoDefination(Record pd, Context ctx) {
		file = getFieldValue(pd, PD_FILE);
		zone = (Sequence) getFieldValue(pd, PD_ZONE);
		date = (String) getFieldValue(pd, PD_DATE);
		user = (String) getFieldValue(pd, PD_USER);
		Sequence seq = (Sequence) getFieldValue(pd, PD_COLUMN);
		if (seq != null) {
			columns = new ArrayList<PseudoColumn>();
			int size = seq.length();
			for (int i = 1; i <= size; i++) {
				Record rec = (Record) seq.get(i);
				columns.add(new PseudoColumn(rec));
			}
		}
		parseFileToTable(ctx);
	}
	
	public Object getFile() {
		return file;
	}

	public void setFile(Object file) {
		this.file = file;
	}

	public Sequence getZone() {
		return zone;
	}

	public void setZone(Sequence zone) {
		this.zone = zone;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<PseudoColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<PseudoColumn> columns) {
		this.columns = columns;
	}


	public List<ITableMetaData> getTables() {
		return tables;
	}

	public void setTables(List<ITableMetaData> tables) {
		this.tables = tables;
	}

	protected static Object getFieldValue(Record pd, String name) {
		int index = pd.getFieldIndex(name);
		if (index != -1) {
			return pd.getFieldValue(index);
		} else {
			return null;
		}
	}
	
	/**
	 * �����ֶ�������α��
	 * @param pname �ֶ���
	 * @return
	 */
	public PseudoColumn findColumnByName(String name) {
		if (name == null || columns == null || columns.size() == 0) {
			return null;
		} else {
			for (PseudoColumn col : columns) {
				if (col.getName() != null && name.equals(col.getName())) {
					return col;
				}
			}
		}
		return null;
	}
	
	/**
	 * ����α�ֶ�������α��
	 * @param pname α�ֶ�����Ҳ�����Ƕ�ֵbits������ֶ���
	 * @return
	 */
	public PseudoColumn findColumnByPseudoName(String pname) {
		if (pname == null || columns == null || columns.size() == 0) {
			return null;
		} else {
			for (PseudoColumn col : columns) {
				if (col.getPseudo() != null && pname.equals(col.getPseudo())) {
					return col;
				}
				if (col.getBits() != null && col.getBits().firstIndexOf(pname) != 0) {
					return col;
				}
			}
		}
		return null;
	}

	private void parseFileToTable(String fn, Context ctx) {
		FileObject fo = new FileObject(fn, null, null, ctx);
		File f = fo.getLocalFile().file();
		tables.add(GroupTable.openBaseTable(f, ctx));
	}
	
	private void parseFileToTable(String fn, int partitions[], Context ctx) {
		if (partitions == null) {
			parseFileToTable(fn, ctx);
		} else {
			for (int partition : partitions) {
				FileObject fo = new FileObject(fn, null, null, ctx);
				fo.setPartition(partition);
				File f = fo.getLocalFile().file();
				tables.add(GroupTable.openBaseTable(f, ctx));
			}
		}
	}
	
	private void parseFileToTable(Context ctx) {
		Object file = this.file;
		int partitions[] = null;

		Sequence zone = getZone();
		if (zone != null) {
			partitions = zone.toIntArray();
		}
		
		tables = new ArrayList<ITableMetaData>();
		if (file instanceof String) {
			parseFileToTable((String) file, partitions, ctx);
		} else {
			Sequence seq = (Sequence) file;
			int size = seq.length();
			for (int i = 1; i <= size; i++) {
				parseFileToTable((String) seq.get(i), partitions, ctx);
			}
		}
	
	
	}

	public String[] getAllColNames() {
		return tables.get(0).getAllColNames();
	}

	public String[] getAllSortedColNames() {
		return tables.get(0).getAllSortedColNames();
	}
}