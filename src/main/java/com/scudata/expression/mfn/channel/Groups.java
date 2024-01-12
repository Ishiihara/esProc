package com.scudata.expression.mfn.channel;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.op.New;
import com.scudata.expression.ChannelFunction;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.expression.ParamInfo2;
import com.scudata.expression.fn.algebra.Var;
import com.scudata.resources.EngineMessage;

/**
 * Ϊ�ܵ���������ۼƷ�ʽ���з�����ܵĽ��������
 * ch.groups(x:F��;y:G��)
 * @author RunQian
 *
 */
public class Groups extends ChannelFunction {
	public Object calculate(Context ctx) {
		// ����������Ϊ��
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("groups" + mm.getMessage("function.missingParam"));
		}

		ArrayList<Object> gathers = new ArrayList<Object>(); // ͳ�ƾۺϺ���
		ArrayList<Integer> poss = new ArrayList<Integer>(); // ������ʽ����Ӧ�ۺϺ����б�ĵڼ����ۺϺ���
		Expression[] exps; // ������ʽ����
		String[] names = null; // ������ʽ��������
		Expression[] calcExps = null; // �ۺϱ��ʽ����
		String[] calcNames = null; // �ۺ��ֶε�����

		//	��groups�Ĳ����ֽ�� ������ʽ��������ʽ�����֡�ͳ�Ʊ��ʽ��ͳ�Ʊ��ʽ������
		char type = param.getType();
		if (type == IParam.Normal) { // ֻ��һ������
			exps = new Expression[]{param.getLeafExpression()};
		} else if (type == IParam.Colon) { // :
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("groups" + mm.getMessage("function.invalidParam"));
			}

			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			if (sub0 == null || sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("groups" + mm.getMessage("function.invalidParam"));
			}

			exps = new Expression[]{sub0.getLeafExpression()};
			names = new String[]{sub1.getLeafExpression().getIdentifierName()};
		} else if (type == IParam.Comma) { // ,
			ParamInfo2 pi = ParamInfo2.parse(param, "groups", true, false);
			exps = pi.getExpressions1();
			names = pi.getExpressionStrs2();
		} else { // ;
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("groups" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);

			if (sub0 == null) {
				exps = null;
				names = null;
			} else {
				ParamInfo2 pi = ParamInfo2.parse(sub0, "groups", true, false);
				exps = pi.getExpressions1();
				names = pi.getExpressionStrs2();
			}

			if (sub1 != null) {
				ParamInfo2 pi = ParamInfo2.parse(sub1, "groups", true, false);
				calcExps = pi.getExpressions1();
				calcNames = pi.getExpressionStrs2();
			}
		}
		
		// ������ʽ���Ⱥ;ۺϱ��ʽ����
		int elen = exps == null ? 0 : exps.length;
		int clen = calcExps == null ? 0 : calcExps.length;
		
		// �������ʽ�еľۺϺ���
		for (int i = 0; i < clen; i++) {
			int size = gathers.size();
			gathers.addAll(Expression.getSpecFunc(calcExps[i], Gather.class));

			if (size == gathers.size()) {
				Node home = calcExps[i].getHome();
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
					
					calcExps[i] = new Expression(cs, ctx, expStr);
					if (calcNames[i] == null || calcNames[i].length() == 0) {
						calcNames[i] = "var";
					}
				} else {
					gathers.add(calcExps[i]);
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
		if (calcExps != null) {
			strExp = calcExps[index].toString();
		}
		
		for (int i = 0; i < tempExps.length; i++) {
			if (i >= poss.get(index)) {
				senExps[index+elen] = new Expression(cs, ctx, strExp);
				index++;
				strExp = calcExps[index].toString();
			}
			
			String funStr = "#" + (i+elen+1);
			strExp = Expression.replaceFunc(strExp, tempExps[i].toString(), funStr);
			if (!strExp.equals(funStr)) {
				exCal = true;
			}
		}
		
		// @b�������ȥ�������ֶ�
		boolean bopt = option != null && option.indexOf('b') != -1;
		
		String[] senNames	= null;	// ͳһ������
		String[] tempNames	= calcNames;	// ��ʱ������
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
			senNames = new String[elen + calcNames.length];
			for (int i = 0; i < elen; i++) {
				senNames[i] = names[i];
			}
			
			for (int i =  0; i < clen; i++) {
				if (null == calcNames[i]) {
					senNames[i+elen] = calcExps[i].toString();
				} else {
					senNames[i+elen] = calcNames[i];
				}
			}
			
			if (bopt) {
				Expression []alterExps = new Expression[clen];
				String []alterNames = new String[clen];
				System.arraycopy(senExps, elen, alterExps, 0, clen);
				System.arraycopy(senNames, elen, alterNames, 0, clen);
				senExps = alterExps;
				senNames = alterNames;
			}
		} else if (bopt) {
			senNames = new String[clen];
			senExps = new Expression[clen];
			for (int i = 0, q = elen + 1; i < clen; ++i, ++q) {
				senExps[i] = new Expression(ctx, "#" + q);
			}
		}
		
		channel.groups(exps, names, tempExps, tempNames, option);
		
		// ����new������
		if (senNames != null) {
			int pkCount = 0;
			if (!bopt && elen > 0) {
				pkCount = elen;
			}
			
			New op = new New(this, senExps, senNames, option);
			channel.setResultNew(op, pkCount);
		}
		
		return channel;
	}	
}
