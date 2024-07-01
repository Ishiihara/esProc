package com.scudata.expression;

import com.scudata.dm.BaseRecord;

/**
 * ��¼��Ա��������
 * r.f()
 * @author RunQian
 *
 */
public abstract class RecordFunction extends MemberFunction {
	protected BaseRecord srcRecord; // Դ��¼
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof BaseRecord;
	}
	
	public void setDotLeftObject(Object obj) {
		srcRecord = (BaseRecord)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		srcRecord = null;
	}
}
