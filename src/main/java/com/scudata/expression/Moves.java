package com.scudata.expression;

import java.util.List;

import com.scudata.cellset.INormalCell;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.ParamList;
import com.scudata.resources.EngineMessage;

/**
 * ȡ�������¼�������
 * T{x:C,����} 
 * @author RunQian
 *
 */
public class Moves extends Function {
	private Node left;
	
	public Moves() {
		priority = PRI_SUF;
	}

	public void setLeft(Node node) {
		left = node;
	}

	public Node getLeft() {
		return left;
	}

	protected boolean containParam(String name) {
		if (left.containParam(name)) return true;
		return super.containParam(name);
	}

	protected void getUsedParams(Context ctx, ParamList resultList) {
		left.getUsedParams(ctx, resultList);
		super.getUsedParams(ctx, resultList);
	}
	
	public void getUsedFields(Context ctx, List<String> resultList) {
		left.getUsedFields(ctx, resultList);
		super.getUsedFields(ctx, resultList);
	}

	protected void getUsedCells(List<INormalCell> resultList) {
		left.getUsedCells(resultList);
		super.getUsedCells(resultList);
	}
	
	/**
	 * ���ñ��ʽ�����ڱ��ʽ���棬���ִ��ʹ�ò�ͬ�������ģ�������������йصĻ�����Ϣ
	 */
	public void reset() {
		getLeft().reset();
		super.reset();
	}
	
	public Node optimize(Context ctx) {
		param.optimize(ctx);
		left = left.optimize(ctx);
		
		return this;
	}
	
	public Object calculate(Context ctx) {
		MessageManager mm = EngineMessage.get();
		throw new RQException("{}" + mm.getMessage("function.invalidParam"));
	}
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("{}" + mm.getMessage("function.missingParam"));
		}
		
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"{}\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		left.checkValidity();
	}
	
	/**
	 * �ж��Ƿ���Լ���ȫ����ֵ���и�ֵ����ʱֻ��һ���м���
	 * @return
	 */
	public boolean canCalculateAll() {
		if (!left.canCalculateAll()) {
			return false;
		}
		
		return param.canCalculateAll();
	}
}
