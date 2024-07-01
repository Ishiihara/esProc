package com.scudata.expression.mfn;

import com.scudata.dm.Context;
import com.scudata.dm.IResource;
import com.scudata.expression.MemberFunction;
import com.scudata.vdb.VDB;

/**
 * �ر���Դ
 * db.close() T.close() cs.close()��
 * @author RunQian
 *
 */
public class Close extends MemberFunction {
	protected IResource resource;
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof IResource;
	}

	public void setDotLeftObject(Object obj) {
		resource = (IResource)obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		resource = null;
	}

	public Object calculate(Context ctx) {
		if (option != null && option.indexOf('p') != -1 && resource instanceof VDB) {
			((VDB)resource).getLibrary().stop();
		} else {
			resource.close();
		}

		return null;
	}
}
