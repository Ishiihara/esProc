package com.scudata.expression.mfn.xo;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.excel.ExcelUtils;
import com.scudata.excel.XlsFileObject;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.expression.XOFunction;
import com.scudata.resources.AppMessage;
import com.scudata.resources.EngineMessage;

/**
 * xo.xlsmove(s,s��;xo��) 
 * 
 * ��xo����Ϊs��sheet�ƶ���xo��������Ϊs����
 * xo��ʡ�ԣ���ʾsheet������s��Ҳʡ�Ա�ʾɾ����
 * xo��δʡ�ԣ�s��ʡ�Ա�ʾ��s��ԭ��
 * 
 * @c ����
 */
public class XlsMove extends XOFunction {

	/**
	 * ����
	 */
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlsmove"
					+ mm.getMessage("function.missingParam"));
		}

		IParam param0;
		IParam param1 = null;
		if (param.getType() == IParam.Semicolon) {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("xlsmove"
						+ mm.getMessage("function.invalidParam"));
			}

			param0 = param.getSub(0);
			param1 = param.getSub(1);
			if (param0 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("xlsmove"
						+ mm.getMessage("function.invalidParam"));
			}
		} else {
			param0 = param;
		}

		Object s = null, s1 = null;

		if (param0.isLeaf()) {
			s = param0.getLeafExpression().calculate(ctx);
		} else if (param0.getType() != IParam.Comma || param0.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlsmove"
					+ mm.getMessage("function.invalidParam"));
		} else {
			s = param0.getSub(0).getLeafExpression().calculate(ctx);
			s1 = param0.getSub(1).getLeafExpression().calculate(ctx);
		}

		Object xo1 = null;
		if (param1 != null) {
			xo1 = param1.getLeafExpression().calculate(ctx);
		}

		String opt = option;
		boolean isCopy = false;
		if (opt != null) {
			if (opt.indexOf('c') != -1)
				isCopy = true;
		}

		if (xo1 != null) {
			if (!(xo1 instanceof XlsFileObject)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("xlsmove"
						+ mm.getMessage("function.paramTypeError"));
			}
			if (file == xo1) { // ���xo��xo'��ͬ����Ϊ��ͬ������
				xo1 = null;
			}
		}

		if (!StringUtils.isValidString(s)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlsmove"
					+ mm.getMessage("function.invalidParam"));
		}

		// ���sheet����
		ExcelUtils.checkSheetName(s);
		ExcelUtils.checkSheetName(s1);

		// ͬ������û��s'�����ø���ѡ��
		if (xo1 == null && !StringUtils.isValidString(s1) && isCopy) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlsmove"
					+ mm.getMessage("function.invalidParam"));
		}

		if (file.getFileType() != XlsFileObject.TYPE_NORMAL
				|| (xo1 != null && ((XlsFileObject) xo1).getFileType() != XlsFileObject.TYPE_NORMAL)) {
			// : xlsopen@r or @w does not support xlsmove
			throw new RQException("xlsmove"
					+ AppMessage.get().getMessage("filexls.rwcell"));
		}

		try {
			file.xlsmove((String) s,
					StringUtils.isValidString(s1) ? (String) s1 : null,
					(XlsFileObject) xo1, isCopy);
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * �Խڵ����Ż�
	 * @param ctx ����������
	 * @param Node �Ż���Ľڵ�
	 */
	public Node optimize(Context ctx) {
		if (param != null) {
			// �Բ������Ż�
			param.optimize(ctx);
		}

		return this;
	}

}