package com.scudata.dm.cursor;

import com.scudata.dm.BaseRecord;
import com.scudata.dm.Sequence;

/**
 * ���α����ݰ��������±�Ĺ���ȥ��
 * @author WangXiaoJun
 *
 */
public class UpdateIdCursor extends ICursor {
	private ICursor cs; // ���������α�
	private int []keys; // �����ֶ�
	private int deleteField; // ɾ����ʶ�ֶΣ����û��ɾ����ʶ�ֶ���Ϊ-1
	
	private Sequence data; // �α껺�������
	private int cur; // �α굱ǰ��¼�ڻ��������е�����
	
	/**
	 * ��������ȥ���α�
	 * @param cs ���������α�
	 * @param keys �����ֶ�
	 * @param deleteField ɾ����ʶ�ֶΣ����û��ɾ����ʶ�ֶ���Ϊ-1
	 */
	public UpdateIdCursor(ICursor cs, int []keys, int deleteField) {
		this.cs = cs;
		this.keys = keys;
		this.deleteField = deleteField;
	}
	
	protected Sequence get(int n) {
		if (cs == null) {
			return null;
		}
		
		if (data == null) {
			data = cs.fetch(n);
			if (data == null || data.length() == 0) {
				cs = null;
				return null;
			}
			
			cur = 1;
		}
		
		int []keys = this.keys;
		int deleteField = this.deleteField;
		Sequence seq = data;
		int len = seq.length();
		BaseRecord prev = (BaseRecord)seq.getMem(cur);
		Sequence result = new Sequence(n);
		
		if (deleteField == -1) {
			for (int i = cur + 1; i <= len; ++i) {
				BaseRecord r = (BaseRecord)seq.getMem(i);
				if (!prev.isEquals(r, keys)) {
					result.add(prev);
				}
				
				prev = r;
			}
			
			data = cs.fetch(n);
			if (data == null || data.length() == 0) {
				cs = null;
				result.add(prev);
			} else {
				cur = 1;
				BaseRecord r = (BaseRecord)data.getMem(1);
				if (!prev.isEquals(r, keys)) {
					result.add(prev);
				}
			}
		} else {
			// ��ɾ����־�ĸ���
			for (int i = cur + 1; i <= len; ++i) {
				BaseRecord r = (BaseRecord)seq.getMem(i);
				if (!prev.isEquals(r, keys)) {
					result.add(prev);
				} else {
					// ����false�������ǰ��¼
					if (!UpdateMergeCursor.merge(prev, r, deleteField)) {
						i++;
						if (i <= len) {
							r = (BaseRecord)seq.getMem(i);
						} else {
							data = null;
							return result;
						}
					}
				}
				
				prev = r;
			}
			
			data = cs.fetch(n);
			if (data == null || data.length() == 0) {
				cs = null;
				result.add(prev);
			} else {
				cur = 1;
				BaseRecord r = (BaseRecord)data.getMem(1);
				if (!prev.isEquals(r, keys)) {
					result.add(prev);
				} else {
					// ����false�������ǰ��¼
					if (!UpdateMergeCursor.merge(prev, r, deleteField)) {
						cur++;
						if (cur > data.length()) {
							cs = null;
						}
					}
				}
			}
		}
		
		return result;
	}

	protected long skipOver(long n) {
		Sequence data = get((int)n);
		if (data != null) {
			return data.length();
		} else {
			return 0;
		}
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		cs = null;
	}
}
