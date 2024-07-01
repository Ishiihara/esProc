package com.scudata.expression.mfn;

import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.expression.MemberFunction;

/**
 * ���ؼ�¼�ļ������û�������򷵻������ֶ���ɵ����У�������Ǽ�¼�򷵻ر���
 * v.v()
 * @author RunQian
 *
 */
public class Value extends MemberFunction {
	protected Object src;
	
	public boolean isLeftTypeMatch(Object obj) {
		return true;
	}

	public void setDotLeftObject(Object obj) {
		src = obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		src = null;
	}

	public Object calculate(Context ctx) {
		if (src instanceof BaseRecord) {
			return ((BaseRecord)src).value();
		} else {
			return src;
		}
	}
}
