package com.raqsoft.expression.mfn.sequence;

import java.util.ArrayList;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.Table;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.Gather;
import com.raqsoft.expression.IParam;
import com.raqsoft.expression.ParamInfo2;
import com.raqsoft.expression.SequenceFunction;
import com.raqsoft.resources.EngineMessage;
import com.raqsoft.util.CursorUtil;

/**
 * �����ۼƷ�ʽ�����н��з���ۺ�
 * A.groups(x:F��;y:G��)
 * @author RunQian
 *
 */
public class Groups extends SequenceFunction {
	public Object calculate(Context ctx) {
		// ����������Ϊ��
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("groups" + mm.getMessage("function.missingParam"));
		}

		ArrayList<Object> gathers = new ArrayList<Object>(); // ͳ�ƾۺϺ���
		ArrayList<Integer> poss = new ArrayList<Integer>(); // �������ʽ����Ӧ�ۺϺ����б��ĵڼ����ۺϺ���
		
		Expression[] exps; // �������ʽ����
		String[] names = null; // �������ʽ��������
		Expression[] calcExps = null; // �ۺϱ���ʽ����
		String[] calcNames = null; // �ۺ��ֶε�����

		//	��groups�Ĳ����ֽ�� �������ʽ���������ʽ�����֡�ͳ�Ʊ���ʽ��ͳ�Ʊ���ʽ������
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
			if (param.getSubSize() > 2) {
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
		
		// �������ʽ���Ⱥ;ۺϱ���ʽ����
		int elen = exps == null ? 0 : exps.length;
		int clen = calcExps == null ? 0 : calcExps.length;
		
		// ��������ʽ�еľۺϺ���
		for (int i = 0; i < clen; i++) {
			int size = gathers.size();
			gathers.addAll(Expression.getSpecFunc(calcExps[i], Gather.class));
			if (size == gathers.size())
				gathers.add(calcExps[i]);
			poss.add(gathers.size());
		}
		
		// �����м�ۺϱ���ʽ
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
		
		// new �α�ı���ʽ
		Expression[] senExps = new Expression[elen+clen];
		String strExp = null;	// �ϱ���ʽ�ַ�����
		int index = 0;	// �ϱ���ʽ������
		
		// �����ϱ���ʽ��ת��Ϊ�µ�new��ͳ���б���ʽ
		boolean exCal	= false;	// �ж��Ƿ��־ۺϱ���ʽ
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
			tempNames = null;	// ����Ҫ��new������������ʱ������Ϊ��
			// ��д�������ʽ
			for (int i = 1; i <= elen; i++) {
				String funStr = "#" + i;
				senExps[i-1] = new Expression(cs, ctx, funStr);
			}
			
			if (senExps.length > 0)	{// ���һ������ʽ������
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
		
		Sequence result = null;
		if (option == null || option.indexOf('m') == -1) {	// ���̼߳���
			result = srcSequence.groups(exps, names, tempExps, tempNames, option, ctx);
		} else {	// ���̼߳���
			result = CursorUtil.groups_m(srcSequence, exps, names, tempExps, tempNames, option, ctx);
		}
		
		if (senNames != null && result != null) {
			result = result.newTable(senNames, senExps, ctx);
			if (!bopt && elen > 0 && result instanceof Table) {
				String []pk = new String[elen];
				for (int i = 1; i <= elen; ++i) {
					pk[i - 1] = "#" + i;
				}
				
				((Table)result).setPrimary(pk);
			}
		}

		return result;
	}	
}