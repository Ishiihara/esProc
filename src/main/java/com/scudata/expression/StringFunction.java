package com.scudata.expression;

/**
 * �ַ�����Ա��������
 * S.f()
 * @author RunQian
 *
 */
public abstract class StringFunction extends MemberFunction {
	protected String srcStr; // Դ��
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof String;
	}
	
	public void setDotLeftObject(Object obj) {
		srcStr = (String)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		srcStr = null;
	}
}