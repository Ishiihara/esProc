package com.scudata.expression;

import com.scudata.dm.SerialBytes;

/**
 * �źų�Ա��������
 * k.f()
 * @author RunQian
 *
 */
public abstract class SerialFunction extends MemberFunction {
	protected SerialBytes sb; // �ź�

	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof SerialBytes;
	}
	
	public void setDotLeftObject(Object obj) {
		sb = (SerialBytes)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		sb = null;
	}
}
