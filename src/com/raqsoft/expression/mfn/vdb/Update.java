package com.raqsoft.expression.mfn.vdb;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.IParam;
import com.raqsoft.expression.ParamInfo2;
import com.raqsoft.expression.VSFunction;
import com.raqsoft.resources.EngineMessage;

/**
 * �ҳ����������ĵ��ݺ��д���ݵ��ֶ�ֵ
 * h.update(F:v,��;x:F,��;w)
 * @author RunQian
 *
 */
public class Update extends VSFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("update" + mm.getMessage("function.missingParam"));
		}
		
		IParam param = this.param;
		String []dirNames = null;
		Object []dirValues = null;
		Object []fvals;
		String []fields;
		Expression filter = null;
		
		if (param.getType() == IParam.Semicolon) {
			int size = param.getSubSize();
			if (size > 3) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("update" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub = param.getSub(0);
			if (sub != null) {
				ParamInfo2 pi = ParamInfo2.parse(sub, "update", false, false);
				dirNames = pi.getExpressionStrs1();
				dirValues = pi.getValues2(ctx);
			}
			
			sub = param.getSub(1);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("update" + mm.getMessage("function.invalidParam"));
			}
			
			ParamInfo2 pi = ParamInfo2.parse(sub, "update", false, false);
			fvals = pi.getValues1(ctx);
			fields = pi.getExpressionStrs2();
			
			if (size > 2) {
				sub = param.getSub(2);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("update" + mm.getMessage("function.invalidParam"));
				}
				
				filter = sub.getLeafExpression();
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("update" + mm.getMessage("function.invalidParam"));
		}
		
		return vs.update(dirNames, dirValues, fvals, fields, filter, option, ctx);
	}
}