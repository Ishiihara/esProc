package com.scudata.expression.mfn.file;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.excel.XlsFileObject;
import com.scudata.expression.FileFunction;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;

/**
 * ����f.xlswrite(xo,p)����Excel����д���ļ���xo������@r@w��ʽ��
 *
 */
public class XlsWrite extends FileFunction {

	/**
	 * ����
	 */
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlswrite"
					+ mm.getMessage("function.missingParam"));
		}

		Object xo = null;
		String pwd = null;
		if (param.getType() == IParam.Semicolon) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlswrite"
					+ mm.getMessage("function.invalidParam"));
		} else {
			IParam param1 = param;

			if (param1 == null) {
			} else if (param1.isLeaf()) {
				xo = param1.getLeafExpression().calculate(ctx);
			} else {
				if (param1.getSubSize() != 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xlswrite"
							+ mm.getMessage("function.invalidParam"));
				}

				IParam sParam = param1.getSub(0);
				if (sParam != null) {
					xo = sParam.getLeafExpression().calculate(ctx);
				}

				IParam pwdParam = param1.getSub(1);
				if (pwdParam == null) {
				} else if (pwdParam.isLeaf()) {
					Object obj = pwdParam.getLeafExpression().calculate(ctx);
					if (obj != null && !"".equals(obj))
						pwd = obj.toString();
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xlswrite"
							+ mm.getMessage("function.invalidParam"));
				}
			}
		}

		if (xo == null || !(xo instanceof XlsFileObject)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlswrite"
					+ mm.getMessage("function.paramTypeError"));
		}
		try {
			((XlsFileObject) xo).xlswrite(file, pwd);
		} catch (RQException e) {
			throw e;
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