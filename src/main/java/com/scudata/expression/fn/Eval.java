package com.scudata.expression.fn;

import com.scudata.cellset.ICellSet;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Sentence;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

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

		if (option == null || option.indexOf('s') == -1) {
			return calc((String)expStr, arg, cs, ctx);
		} else {
			if (arg == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("eval" + mm.getMessage("function.missingParam"));
			}
			
			return evalString((String)expStr, arg);
		}
	}

	private static String evalString(String expStr, Sequence arg) {
		int len = expStr.length();
		StringBuffer sb = new StringBuffer(len * 2);
		int q = 0;
		int argCount = arg.length();
		
		for (int i = 0; i < len;) {
			char c = expStr.charAt(i);
			if (c == '?') {
				int numEnd = len;
				for (int j = ++i; j < len; ++j) {
					c = expStr.charAt(j);
					if (c < '0' || c > '9') {
						numEnd = j;
						break;
					}
				}
				
				if (numEnd == i) {
					q++;
					if (q > argCount) {
						q = 1;
					}
					
					Object obj = arg.get(q);
					String str = Variant.toString(obj);
					sb.append(str);
				} else {
					String str = expStr.substring(i, numEnd);
					q = Integer.parseInt(str);
					
					Object obj = arg.get(q);
					str = Variant.toString(obj);
					sb.append(str);
					i = numEnd;
				}
			} else if (c == '"' || c == '\'') {
				int index = Sentence.scanQuotation(expStr, i);
				if (index == -1) {
					sb.append(c);
					i++;
				} else {
					index++;
					sb.append(expStr.substring(i, index));
					i = index;
				}
			} else /*if (KeyWord.isSymbol(c))*/ {
				sb.append(c);
				i++;
			}/* else {
				sb.append(c);
				for (++i; i < len;) {
					c = expStr.charAt(i++);
					sb.append(c);
					if (KeyWord.isSymbol(c)) {
						break;
					}
				}
			}*/
		}
		
		return sb.toString();
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
