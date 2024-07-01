package com.scudata.expression;

import com.scudata.vdb.VDB;

/**
 * ���׿��Ա��������
 * v.f()
 * @author RunQian
 *
 */
public abstract class VDBFunction extends MemberFunction {
	protected VDB vdb; // Դ���ݿ�
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof VDB;
	}

	public void setDotLeftObject(Object obj) {
		vdb = (VDB)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		vdb = null;
	}
}
