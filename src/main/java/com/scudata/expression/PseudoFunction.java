package com.scudata.expression;

import com.scudata.dw.pseudo.IPseudo;

public abstract class PseudoFunction extends MemberFunction {
	protected IPseudo pseudo;
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof IPseudo;
	}

	public void setDotLeftObject(Object obj) {
		pseudo = (IPseudo)obj;
	}

	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		pseudo = null;
	}
}
