package com.raqsoft.expression.fn;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Function;
import com.raqsoft.expression.IParam;
import com.raqsoft.resources.EngineMessage;
import com.raqsoft.util.Variant;

/**
 * �Բ���ִ���߼�������
 * cand(x1,��)
 * @author RunQian
 *
 */
public class Cand extends Function {
	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cand" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			return Boolean.valueOf(Variant.isTrue(obj));
		} else {
			int size = param.getSubSize();
			for (int i = 0; i < size; ++i) {
				IParam sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("cand" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (Variant.isFalse(obj)) {
					return Boolean.FALSE;
				}
			}

			return Boolean.TRUE;
		}
	}
}