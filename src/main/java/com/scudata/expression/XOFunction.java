package com.scudata.expression;

import com.scudata.excel.XlsFileObject;

/**
 * xo.func() xls���������Դ���
 *
 */
public abstract class XOFunction extends MemberFunction {
	/**
	 * xo�ļ�����
	 */
	protected XlsFileObject file;

	/**
	 * ��������XlsFileObject�����
	 */
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof XlsFileObject;
	}

	public void setDotLeftObject(Object obj) {
		file = (XlsFileObject) obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		file = null;
	}
}
