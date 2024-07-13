package com.scudata.expression;

import java.util.List;

import com.scudata.cellset.ICellSet;
import com.scudata.cellset.INormalCell;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.ParamList;
import com.scudata.resources.EngineMessage;

/**
 * �����ڵ����
 * @author WangXiaoJun
 *
 */
public abstract class Function extends Node {
	protected String functionName; // ������
	protected String option; // ѡ��
	protected String strParam; // ���������ַ��������ڼ�Ⱥ�����Ѳ����ַ��������ڵ��
	protected IParam param; // ��������

	protected ICellSet cs = null; // �������
	protected static final int Default_Size = 4;

	/**
	 * ���ú�������
	 * @param cs �������
	 * @param ctx ����������
	 * @param param ���������ַ���
	 */
	public void setParameter(ICellSet cs, Context ctx, String param) {
		strParam = param;
		this.cs = cs;
		this.param = ParamParser.parse(param, cs, ctx);
	}

	/**
	 * ȡ���������ַ���
	 * @return
	 */
	public String getParamString() {
		return strParam;
	}
	
	/**
	 * ȡ������
	 * @return String
	 */
	public String getFunctionName() {
		return functionName;
	}
	
	 /**
	  * ���ú�����
	  * @param name ������
	  */
	public void setFunctionName(String name) {
		this.functionName = name;
	}
	
	/**
	 * ���غ�������
	 * @return IParam
	 */
	public IParam getParam() {
		return param;
	}

	/**
	 * ���ú�������
	 * @param param ����
	 */
	public void setParam(IParam param) {
		this.param = param;
	}

	/**
	 * ���ú���ѡ��
	 * @param opt ѡ��
	 */
	public void setOption(String opt) {
		option = opt;
	}

	/**
	 * ȡ����ѡ��
	 * @return String
	 */
	public String getOption() {
		return option;
	}

	/**
	 * �����Ƿ����ָ������
	 * @param name ������
	 * @return boolean true��������false��������
	 */
	protected boolean containParam(String name) {
		if (param != null) {
			return param.containParam(name);
		} else {
			return false;
		}
	}

	/**
	 * ���ұ��ʽ���õ�����
	 * @param ctx ����������
	 * @param resultList ���ֵ���õ��Ĳ�������ӵ�������
	 */
	protected void getUsedParams(Context ctx, ParamList resultList) {
		if (param != null) {
			param.getUsedParams(ctx, resultList);
		}
	}
	
	public void getUsedFields(Context ctx, List<String> resultList) {
		if (param != null) {
			param.getUsedFields(ctx, resultList);
		}
	}
	
	/**
	 * ���ñ��ʽ�����ڱ��ʽ���棬���ִ��ʹ�ò�ͬ�������ģ�������������йصĻ�����Ϣ
	 */
	public void reset() {
		if (param != null) {
			param.reset();
		}
	}

	protected void getUsedCells(List<INormalCell> resultList) {
		if (param != null) {
			param.getUsedCells(resultList);
		}
	}

	/**
	 * �Խڵ����Ż�
	 * @param ctx ����������
	 * @param Node �Ż���Ľڵ�
	 */
	public Node optimize(Context ctx) {
		boolean opt = true;
		if (param != null) {
			// �Բ������Ż�
			opt = param.optimize(ctx);
		}

		// ������������ǳ�������������ֵ
		// ��������������뱻�Ż��ɳ�����Ҫ���ش˷���
		if (opt) {
			return new Constant(calculate(ctx));
		} else {
			return this;
		}
	}
	
	public Expression[] getParamExpressions(String funcName, boolean canNull) {
		IParam param = this.param;
		if (param == null) {
			if (canNull) {
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(funcName + mm.getMessage("function.missingParam"));
			}
		}
		
		Expression []exps;
		if (param.isLeaf()) {
			exps = new Expression[]{param.getLeafExpression()};
		} else {
			int count = param.getSubSize();
			exps = new Expression[count];
			
			for (int i = 0; i < count; ++i) {
				IParam sub = param.getSub(i);
				if (sub != null) {
					exps[i] = sub.getLeafExpression();
				} else if (!canNull) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(funcName + mm.getMessage("function.invalidParam"));
				}
			}
		}
		
		return exps;
	}
	
	/**
	 * ȡ��һ��������������������ַ���
	 * @return	�ַ���
	 */
	public String getFunctionString() {
		String strRes = this.getFunctionName();
		
		if (null != option && 0 != option.length())
			strRes += "@" + option;
		
		strRes += "(";
		strRes += this.strParam;
		strRes += ")";
		
		return strRes;
	}

	/**
	 * �жϽڵ��Ƿ���ָ������
	 * @param name ������
	 * @return true����ָ��������false������
	 */
	public boolean isFunction(String name) {
		return name.equals(functionName);
	}
	
	/**
	 * �ж��Ƿ���Լ���ȫ����ֵ���и�ֵ����ʱֻ��һ���м���
	 * @return
	 */
	public boolean canCalculateAll() {
		if (param != null) {
			return param.canCalculateAll();
		} else {
			return true;
		}
	}

	public ICellSet getCellSet() {
		return cs;
	}
}
