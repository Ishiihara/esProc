package com.raqsoft.expression;

import com.raqsoft.dm.Sequence;

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
	 * �жϵ�ǰ�ڵ��Ƿ������к���
	 * �������������Ҳ�ڵ������к��������ڵ�������������Ҫ����ת������
	 * @return
	 */
	public boolean isSequenceFunction() {
		return true;
	}
}