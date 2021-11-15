package com.raqsoft.ide.dfx.etl.element;

import com.raqsoft.ide.dfx.etl.EtlConsts;
import com.raqsoft.ide.dfx.etl.ObjectElement;
import com.raqsoft.ide.dfx.etl.ParamInfoList;

/**
 * ���������༭ t.close()
 * ������ǰ׺T��ʾCTXʵ��
 * 
 * @author Joancy
 *
 */
public class TClose extends ObjectElement {

	/**
	 * ��ȡ���ڽ���༭�Ĳ�����Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		return paramInfos;
	}

	/**
	 * ��ȡ������
	 * ���͵ĳ�������Ϊ
	 * EtlConsts.TYPE_XXX
	 * @return EtlConsts.TYPE_CTX
	 */
	public byte getParentType() {
		return EtlConsts.TYPE_CTX;
	}

	/**
	 * ��ȡ�ú����ķ�������
	 * @return EtlConsts.TYPE_EMPTY
	 */
	public byte getReturnType() {
		return EtlConsts.TYPE_EMPTY;
	}
	/**
	 * ��ȡ��������SPL����ʽ��ѡ�
	 */
	public String optionString(){
		return "";
	}

	/**
	 * ��ȡ��������SPL����ʽ�ĺ�����
	 */
	public String getFuncName() {
		return "close";
	}

	/**
	 * ��ȡ��������SPL����ʽ�ĺ�����
	 * ��setFuncBody���溯����Ȼ�����ʽ�ĸ�ֵҲ���ǻ����
	 */
	public String getFuncBody() {
		return null;
	}
	/**
	 * ���ú�����
	 * @param funcBody ������
	 */
	public boolean setFuncBody(String funcBody) {
		return true;
	}


}