package com.scudata.expression.fn;

import com.scudata.cellset.ICellSet;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;

/**
 * ��������ı��ʽ�ַ��������㣬���ؼ�����
 * eval(x,��) ��x��?1��?2���ַ�ʽ���ô���Ĳ���
 * @author RunQian
 *
 */
public class Eval extends Function {
	//�Ż�
	public Node optimize(Context ctx) {
		if (param != null) param.optimize(ctx);
		return this;
	}

	public byte calcExpValueType(Context ctx) {
		return Expression.TYPE_UNKNOWN;
	}

	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("eval" + mm.getMessage("function.missingParam"));
		}
	}

	public Object calculate(Context ctx) {
		Object expStr;
		Sequence arg = null;
		if (param.isLeaf()) {
			expStr = param.getLeafExpression().calculate(ctx);
			if (!(expStr instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("eval" + mm.getMessage("function.paramTypeError"));
			}
		} else {
			int size = param.getSubSize();
			IParam sub = param.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("eval" + mm.getMessage("function.invalidParam"));
			}
			
			expStr = sub.getLeafExpression().calculate(ctx);
			if (!(expStr instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("eval" + mm.getMessage("function.paramTypeError"));
			}
			
			arg = new Sequence(size);
			for (int i = 1; i < size; ++i) {
				sub = param.getSub(i);
				if (sub != null) {
					arg.add(sub.getLeafExpression().calculate(ctx));
				} else {
					arg.add(null);
				}
			}
		}

		return calc((String)expStr, arg, cs, ctx);
	}

	/**
	 * ������ʽ
	 * @param expStr String ���ʽ�ַ���
	 * @param arg ISequence �������ɵ����У�û�в����ɿ�
	 * @param cs ICellSet ���ʽ�õ��������ɿ�
	 * @param ctx Context ���������ģ����ɿ�
	 * @return Object ���ر��ʽ������
	 */
	public static Object calc(String expStr, Sequence arg, ICellSet cs, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();

		try {
			stack.pushArg(arg);
			Expression exp = new Expression(cs, ctx, expStr);
			return exp.calculate(ctx);
		} finally {
			stack.popArg();
		}
	}
}
