package com.scudata.expression;

import com.scudata.dm.Sequence;

/**
 * ���г�Ա��������
 * A.f()
 * @author RunQian
 *
 */
public abstract class SequenceFunction extends MemberFunction {
	protected Sequence srcSequence; // ����

	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof Sequence;
	}
	
	public void setDotLeftObject(Object obj) {
		srcSequence = (Sequence)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		srcSequence = null;
	}

	/**
	 * �жϵ�ǰ�ڵ��Ƿ������к���
	 * �������������Ҳ�ڵ������к��������ڵ�������������Ҫ����ת������
	 * @return
	 */
	public boolean isSequenceFunction() {
		return true;
	}
}
