package com.raqsoft.expression.fn.gather;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.ObjectCache;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Sequence;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.Gather;
import com.raqsoft.expression.IParam;
import com.raqsoft.resources.EngineMessage;
import com.raqsoft.util.Variant;

/**
 * ��ȡֵΪ��Ĳ�������
 * count(x1,��)
 * @author RunQian
 *
 */
public class Count extends Gather {
	private Expression exp;

	public void prepare(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("count" + mm.getMessage("function.invalidParam"));
		}

		exp = param.getLeafExpression();
	}

	public Object gather(Context ctx) {
		Object val = exp.calculate(ctx);
		if (Variant.isTrue(val)) {
			return new Long(1);
		} else {
			return new Long(0);
		}
	}

	public Object gather(Object oldValue, Context ctx) {
		Object val =  exp.calculate(ctx);
		if (Variant.isTrue(val)) {
			if (oldValue == null) {
				return new Long(1);
			} else {
				return new Long(1 + ((Number)oldValue).longValue());
			}
		} else {
			if (oldValue == null) {
				return new Long(0);
			} else {
				return oldValue;
			}
		}
	}

	public Expression getRegatherExpression(int q) {
		String str = "sum(#" + q + ")";
		return new Expression(str);
	}

	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("count" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (obj instanceof Sequence) {
				return ((Sequence)obj).count(option);
			} else {
				if (Variant.isTrue(obj)) {
					return ObjectCache.getInteger(1);
				} else {
					return ObjectCache.getInteger(0);
				}
			}
		}

		int count = 0;
		for (int i = 0, size = param.getSubSize(); i < size; ++i) {
			IParam sub = param.getSub(i);
			if (sub != null) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (Variant.isTrue(obj)) {
					count++;
				}
			}
		}

		return count;
	}
	
	// ������seq��һ�»���ֵ
	public Object gather(Sequence seq) {
		return seq.count(option);
	}
	
	public Expression getExp() {
		return exp;
	}
}