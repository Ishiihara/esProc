package com.raqsoft.ide.dfx.etl.element;

import com.raqsoft.ide.dfx.etl.EtlConsts;
import com.raqsoft.ide.dfx.etl.ObjectElement;
import com.raqsoft.ide.dfx.etl.ParamInfo;
import com.raqsoft.ide.dfx.etl.ParamInfoList;

/**
 * ���������༭ xls.xlsclose()
 * ������ǰ׺X��ʾ xls�ļ�����
 * 
 * @author Joancy
 *
 */
public class XXlsClose extends ObjectElement {

	/**
	 * ��ȡ���ڽ���༭�Ĳ�����Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(XXlsClose.class, this);
		
		return paramInfos;
	}

	/**
	 * ��ȡ������
	 * ���͵ĳ�������Ϊ
	 * EtlConsts.TYPE_XXX
	 * @return EtlConsts.TYPE_XLS
	 */
	public byte getParentType() {
		return EtlConsts.TYPE_XLS;
	}

	/**
	 * ��ȡ�ú����ķ�������
	 * @return EtlConsts.TYPE_EMPTY
	 */
	public byte getReturnType() {
		return EtlConsts.TYPE_EMPTY;
	}

	/**
	 * ��ȡ��������SPL����ʽ�ĺ�����
	 */
	public String getFuncName(){
		return "xlsclose";
	}
	
	/**
	 * ��ȡ��������SPL����ʽ��ѡ�
	 */
	public String optionString(){
		return null;
	}
	
	/**
	 * ��ȡ��������SPL����ʽ�ĺ�����
	 * ��setFuncBody���溯����Ȼ�����ʽ�ĸ�ֵҲ���ǻ����
	 */
	public String getFuncBody() {
		StringBuffer sb = new StringBuffer();
		return sb.toString();
	}

	/**
	 * ���ú�����
	 * @param funcBody ������
	 */
	public boolean setFuncBody(String funcBody) {
		return true;
	}

}