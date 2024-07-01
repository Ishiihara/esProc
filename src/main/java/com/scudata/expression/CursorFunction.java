package com.scudata.expression;

import com.scudata.dm.cursor.ICursor;

/**
 * �α��Ա��������
 * cs.f()
 * @author RunQian
 *
 */
public abstract class CursorFunction extends MemberFunction {
	protected ICursor cursor;

	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof ICursor;
	}
	
	public void setDotLeftObject(Object obj) {
		cursor = (ICursor)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		cursor = null;
	}
}