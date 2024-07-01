package com.scudata.expression;

import com.scudata.dw.IPhyTable;

/**
 * ����Ա��������
 * @author RunQian
 *
 */
public abstract class PhyTableFunction extends MemberFunction {
	protected IPhyTable table;
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof IPhyTable;
	}

	public void setDotLeftObject(Object obj) {
		table = (IPhyTable)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		table = null;
	}
}