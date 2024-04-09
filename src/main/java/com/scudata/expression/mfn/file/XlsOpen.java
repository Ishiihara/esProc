package com.scudata.expression.mfn.file;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.excel.FileXls;
import com.scudata.excel.FileXlsR;
import com.scudata.excel.XlsFileObject;
import com.scudata.expression.FileFunction;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.AppMessage;
import com.scudata.resources.EngineMessage;

/**
 * ����f.xlsopen(p) ����Excel�ļ�f���سɶ��� p�����룻���ض������Ϊֻ�����
 * stname��ҳ����,nrows��������,ncols��������
 * 
 * @r ��ʽ�������ڳ��򵼳���xls�п������������ز���ȷ
 * @w ��ʽд����ʱ���ܷ���������Ϣ����@r����
 * 
 *
 */
public class XlsOpen extends FileFunction {

	/**
	 * ����
	 */
	public Object calculate(Context ctx) {
		String opt = option;
		boolean isR = opt != null && opt.indexOf("r") > -1;
		boolean isW = opt != null && opt.indexOf("w") > -1;

		if (isR && isW) {
			// @w��@r����ss
			MessageManager mm = AppMessage.get();
			throw new RQException("xlsopen" + mm.getMessage("filexls.notrw")); // ��ѡ��w��r����ͬʱ����
		}

		if (param == null) {
			// ����Excel�ļ�����
			return xlsOpen(null, isR, isW, ctx);
		}

		String pwd = null;
		if (param.getType() != IParam.Normal) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlsopen"
					+ mm.getMessage("function.invalidParam"));
		} else {
			if (!param.isLeaf()) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("xlsopen"
						+ mm.getMessage("function.invalidParam"));
			}

			IParam pwdParam = param;
			if (pwdParam != null) {
				Object tmp = pwdParam.getLeafExpression().calculate(ctx);
				if (tmp != null) {
					pwd = tmp.toString();
				}
				if ("".equals(pwd))
					pwd = null;
			}
		}
		try {
			return xlsOpen(pwd, isR, isW, ctx);
		} catch (RQException e) {
			throw e;
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
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

	/**
	 * ����xo�ļ�����
	 * 
	 * @param pwd
	 *            ����
	 * @param isR
	 *            ѡ��@r
	 * @param isW
	 * @return
	 */
	private XlsFileObject xlsOpen(String pwd, boolean isR, boolean isW,
			Context ctx) {
		if (isR) {
			FileXlsR xo = new FileXlsR(file, pwd);
			ctx.addResource(xo);
			return xo;
		}
		byte type;
		if (isW) {
			type = XlsFileObject.TYPE_WRITE;
		} else {
			type = XlsFileObject.TYPE_NORMAL;
		}
		FileXls xo = new FileXls(file, pwd, type);
		ctx.addResource(xo);
		return xo;
	}

}
