package com.raqsoft.expression.mfn.cursor;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.CursorFunction;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.IParam;
import com.raqsoft.resources.EngineMessage;

/**
 * ��ȡ�α���ָ������ʽ�Ĳ�ֵͬ�γɵ����е����з���
 * cs.id(xi,��;n) ֻ��һ��xiʱ���س�һ�����У�ÿ��xi�ҵ�n�������ң�nʡ�Է�������
 * @author RunQian
 *
 */
public class Id extends CursorFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("id" + mm.getMessage("function.missingParam"));
		}

		// û������n������ȡ������
		int n = Integer.MAX_VALUE;
		IParam expParam;
		if (param.getType() == IParam.Semicolon) {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("id" + mm.getMessage("function.invalidParam"));
			}
			
			expParam = param.getSub(0);
			if (expParam == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("id" + mm.getMessage("function.missingParam"));
			}
			
			IParam countParam = param.getSub(1);
			if (countParam == null || !countParam.isLeaf()) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("id" + mm.getMessage("function.invalidParam"));
			}
		
			Object count = countParam.getLeafExpression().calculate(ctx);
			if (!(count instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("id" + mm.getMessage("function.paramTypeError"));
			}
			
			n = ((Number)count).intValue();
			if (n < 1) {
				return null;
			}
		} else {
			expParam = param;
		}
		
		Expression []exps;
		if (expParam.isLeaf()) {
			exps = new Expression[] {expParam.getLeafExpression()};
		} else {
			int size = expParam.getSubSize();
			exps = new Expression[size];
			for (int i = 0; i < size; ++i) {
				IParam sub = expParam.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("id" + mm.getMessage("function.missingParam"));
				}
				
				exps[i] = sub.getLeafExpression();
			}
		}
		
		return cursor.id(exps, n, ctx);
	}
}