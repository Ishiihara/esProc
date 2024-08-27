package com.scudata.expression.mfn.file;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.FileObject;
import com.scudata.expression.FileFunction;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.BFileUtil;

/**
 * �Լ��ļ������������
 * f.sortx(Fi,��;f��)
 * @author LW
 *
 */
public class Sortx extends FileFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sortx" + mm.getMessage("function.missingParam"));
		}

		IParam sortParam = null;
		FileObject out = null;
		
		if (param.getType() == IParam.Semicolon) {
			if (param.getSubSize() > 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("sortx" + mm.getMessage("function.invalidParam"));
			}

			sortParam = param.getSub(0);
			if (sortParam == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("sortx" + mm.getMessage("function.invalidParam"));
			}

			IParam sub = param.getSub(1);
			if (sub == null) {
			} else if (sub.isLeaf()) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (obj instanceof FileObject) {
					out = (FileObject) obj;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("sortx" + mm.getMessage("function.paramTypeError"));
				}
			
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("sortx" + mm.getMessage("function.invalidParam"));
			}
		} else {
			sortParam = param;
		}

		String []fields;
		if (sortParam.isLeaf()) { // ֻ��һ������
			fields = new String[]{ sortParam.getLeafExpression().toString() };
		} else if (sortParam.getType() == IParam.Comma) { // ,
			int size = sortParam.getSubSize();
			fields = new String[size];
			for (int i = 0; i < size; ++i) {
				IParam sub = sortParam.getSub(i);
				if (sub == null || !sub.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("sortx" + mm.getMessage("function.invalidParam"));
				}
				fields[i] = sub.getLeafExpression().toString();
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("sortx" + mm.getMessage("function.invalidParam"));
		}

		return BFileUtil.sortx(file, out, fields, option, ctx);
	}
}
