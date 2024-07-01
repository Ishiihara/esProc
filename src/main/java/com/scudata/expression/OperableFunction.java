package com.scudata.expression;

import com.scudata.dm.op.Operable;

/**
 * �ɸ��Ӳ��������Ա��������
 * cs.f()��ch.f()
 * @author RunQian
 *
 */
public abstract class OperableFunction extends MemberFunction {
	protected Operable operable;

	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof Operable;
	}
	
	public void setDotLeftObject(Object obj) {
		operable = (Operable)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		operable = null;
	}
}