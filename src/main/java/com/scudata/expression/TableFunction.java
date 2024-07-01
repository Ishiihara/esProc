package com.scudata.expression;

import com.scudata.dm.Table;

/**
 * ����Ա��������
 * T.f()
 * @author RunQian
 *
 */
public abstract class TableFunction extends MemberFunction {
	protected Table srcTable; // Դ���
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof Table;
	}
	
	public void setDotLeftObject(Object obj) {
		srcTable = (Table)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		srcTable = null;
	}
}
