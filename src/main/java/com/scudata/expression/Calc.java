package com.scudata.expression;

import com.scudata.cellset.ICellSet;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.op.Calculate;
import com.scudata.dm.op.Operable;
import com.scudata.resources.EngineMessage;

/**
 * ѭ������ִ�б��ʽ������������
 * A.(x)
 * @author WangXiaoJun
 *
 */
public class Calc extends MemberFunction {
	private Object srcObj;
	
	/**
	 * ���ú�������
	 * @param cs �������
	 * @param ctx ����������
	 * @param param ���������ַ���
	 */
	public void setParameter(ICellSet cs, Context ctx, String param) {
		strParam = param;
		this.cs = cs;
		
		// A.(x,��)�Ѳ�������һ�����崴���ɶ��ű��ʽ
		this.param = ParamParser.newLeafParam(param, cs, ctx);
		//this.param = ParamParser.parse(param, cs, ctx);
	}
	
	public boolean isLeftTypeMatch(Object obj) {
		return true;
	}

	public void setDotLeftObject(Object obj) {
		srcObj = obj;
	}
	
	/**
	 * �жϵ�ǰ�ڵ��Ƿ������к���
	 * �������������Ҳ�ڵ������к��������ڵ�������������Ҫ����ת������
	 * @return
	 */
	public boolean isSequenceFunction() {
		return true;
	}
	
	public Object calculate(Context ctx) {
		if (param == null) {
			return srcObj;
		} else if (param.isLeaf()) {
			Expression exp = param.getLeafExpression();
			if (srcObj instanceof Sequence) {
				return ((Sequence)srcObj).calc(exp, option, ctx);
			} else if (srcObj instanceof BaseRecord) {
				return ((BaseRecord)srcObj).calc(exp, ctx);
			} else if (srcObj instanceof Operable) {
				Calculate calculate = new Calculate(this, exp);
				((Operable)srcObj).addOperation(calculate, ctx);
				return srcObj;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("\".\"" + mm.getMessage("dot.s2rLeft"));
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("()" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object getSrcObj() {
		return srcObj;
	}
}
