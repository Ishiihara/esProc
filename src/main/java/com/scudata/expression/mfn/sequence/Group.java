package com.scudata.expression.mfn.sequence;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.expression.ParamInfo2;
import com.scudata.expression.SequenceFunction;
import com.scudata.expression.fn.algebra.Var;
import com.scudata.resources.EngineMessage;

/**
 * ��������������߷������
 * A.group(xi,��) A.group(x:F,��;y:G��)
 * @author RunQian
 *
 */
public class Group extends SequenceFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			return srcSequence.group(option);
		} else if (param.isLeaf()) {
			Expression exp = param.getLeafExpression();
			return srcSequence.group(exp, option, ctx);
		} else if (param.getType() == IParam.Comma) { // ,
			int size = param.getSubSize();
			Expression []exps = new Expression[size];
			for (int i = 0; i < size; ++i) {
				IParam sub = param.getSub(i);
				if (sub == null || !sub.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("group" + mm.getMessage("function.invalidParam"));
				}
				exps[i] = sub.getLeafExpression();
			}

			return srcSequence.group(exps, option, ctx);
		} else if (param.getType() == IParam.Semicolon) { // ;
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("group" + mm.getMessage("function.invalidParam"));
			}

			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);

			Expression []exps = null;
			String []names = null;
			if (sub0 != null) {
				ParamInfo2 pi0 = ParamInfo2.parse(sub0, "group", true, false);
				exps = pi0.getExpressions1();
				names = pi0.getExpressionStrs2();
			}

			Expression []newExps = null;
			String []newNames = null;
			if (sub1 != null) {
				ParamInfo2 pi1 = ParamInfo2.parse(sub1, "group", true, false);
				newExps = pi1.getExpressions1();
				newNames = pi1.getExpressionStrs2();
			}

			if (option != null && option.indexOf('s') != -1) {
				// ������ʽ���Ⱥ;ۺϱ��ʽ����
				int elen = exps == null ? 0 : exps.length;
				int clen = newExps == null ? 0 : newExps.length;
				ArrayList<Object> gathers = new ArrayList<Object>(); // ͳ�ƾۺϺ���
				ArrayList<Integer> poss = new ArrayList<Integer>(); // ������ʽ����Ӧ�ۺϺ����б�ĵڼ����ۺϺ���
				
				// �������ʽ�еľۺϺ���
				for (int i = 0; i < clen; i++) {
					int size = gathers.size();
					gathers.addAll(Expression.getSpecFunc(newExps[i], Gather.class));
					
					if (size == gathers.size()) {
						Node home = newExps[i].getHome();
						if (home instanceof Var) {
							Var var = (Var)home;
							String param = var.getParamString();
							String opt = var.getOption();
							String sumStr = "sum(" + param + ")";
							Expression exp = new Expression(cs, ctx, sumStr);
							gathers.add(exp.getHome());
							
							String countStr = "count(" + param + ")";
							exp = new Expression(cs, ctx, countStr);
							gathers.add(exp.getHome());
							
							String sum2Str = "sum(power(" + param + "))";
							exp = new Expression(cs, ctx, sum2Str);
							gathers.add(exp.getHome());
							
							// sum2+count*power(sum/count) - 2*sum*sum/count
							String expStr = sum2Str + "+" + countStr + "*power(" + sumStr + "/" + countStr + 
									")-2*" + sumStr + "*" + sumStr + "/" + countStr;
							if (opt == null || opt.indexOf('s') == -1) {
								expStr = "(" + expStr + ")/" + countStr;
							} else {
								expStr = "(" + expStr + ")/(" + countStr + "-1)";
							}
							
							newExps[i] = new Expression(cs, ctx, expStr);
							if (newNames[i] == null || newNames[i].length() == 0) {
								newNames[i] = "var";
							}
						} else {
							gathers.add(newExps[i]);
						}
					}
					
					poss.add(gathers.size());
				}
				
				// �����м�ۺϱ��ʽ
				Expression[] tempExps = new Expression[gathers.size()];
				for (int i = 0; i < tempExps.length; i++) {
					Object obj = gathers.get(i);
					if (obj instanceof Gather) {
						Gather gather = (Gather)gathers.get(i);
						tempExps[i] = new Expression(cs, ctx, gather.getFunctionString());
					} else {
						tempExps[i] = (Expression)gathers.get(i);
					}
				}
				
				// new �α�ı��ʽ
				Expression[] senExps = new Expression[elen+clen];
				String strExp = null;	// �ϱ��ʽ�ַ�����
				int index = 0;	// �ϱ��ʽ������
				
				// �����ϱ��ʽ��ת��Ϊ�µ�new��ͳ���б��ʽ
				boolean exCal	= false;	// �ж��Ƿ��־ۺϱ��ʽ
				if (newExps != null) {
					strExp = newExps[index].toString();
				}
				
				for (int i = 0; i < tempExps.length; i++) {
					if (i >= poss.get(index)) {
						senExps[index+elen] = new Expression(cs, ctx, strExp);
						index++;
						strExp = newExps[index].toString();
					}
					
					String funStr = "#" + (i+elen+1);
					strExp = Expression.replaceFunc(strExp, tempExps[i].toString(), funStr);
					if (!strExp.equals(funStr)) {
						exCal = true;
					}
				}
				
				String[] senNames	= null;	// ͳһ������
				String[] tempNames	= newNames;	// ��ʱ������
				if (exCal) {
					tempNames = null;	// ����Ҫ��new����������ʱ������Ϊ��
					// ��д������ʽ
					for (int i = 1; i <= elen; i++) {
						String funStr = "#" + i;
						senExps[i-1] = new Expression(cs, ctx, funStr);
					}
					
					if (senExps.length > 0)	{// ���һ�����ʽ������
						senExps[index+elen] = new Expression(cs, ctx, strExp);
					}
					
					// ����ͳһ������
					senNames = new String[elen + newNames.length];
					for (int i = 0; i < elen; i++) {
						senNames[i] = names[i];
					}
					
					for (int i =  0; i < clen; i++) {
						if (null == newNames[i]) {
							senNames[i+elen] = newExps[i].toString();
						} else {
							senNames[i+elen] = newNames[i];
						}
					}
				}
				
				if (senNames == null) {
					return srcSequence.group(exps, names, newExps, newNames, option, ctx);
				} else {
					Sequence result = srcSequence.groups(exps, names, tempExps, tempNames, option, ctx);		
					result = result.newTable(senNames, senExps, ctx);
					if (elen > 0 && result instanceof Table) {
						String []pk = new String[elen];
						for (int i = 1; i <= elen; ++i) {
							pk[i - 1] = "#" + i;
						}
						
						((Table)result).setPrimary(pk);
					}
					
					return result;
				}
			} else {
				return srcSequence.group(exps, names, newExps, newNames, option, ctx);
			}
		} else {
			ParamInfo2 pi0 = ParamInfo2.parse(param, "group", true, false);
			Expression []exps0 = pi0.getExpressions1();
			String []names0 = pi0.getExpressionStrs2();
			return srcSequence.group(exps0, names0, null, null, option, ctx);
		}
	}
}
