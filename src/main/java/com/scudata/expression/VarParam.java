package com.scudata.expression;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.dm.Context;
import com.scudata.dm.DBObject;
import com.scudata.dm.FileObject;
import com.scudata.dm.Param;
import com.scudata.dm.ParamList;
import com.scudata.dm.Sequence;
import com.scudata.util.Variant;

/**
 * �����ڵ�
 * @author RunQian
 *
 */
public class VarParam extends Node {
	private Param param;

	public VarParam(Param param) {
		this.param = param;
	}
	
	/**
	 * �Խڵ�������Ż���������Ԫ��Ͳ������ã����������ʽ����ɳ���
	 * @param ctx ����������
	 * @param Node �Ż���Ľڵ�
	 */
	public Node deepOptimize(Context ctx) {
		return new Constant(calculate(ctx));
	}

	public Object calculate(Context ctx) {
		return param.getValue();
	}

	public Object assign(Object value, Context ctx) {
		param.setValue(value);
		return value;
	}
	
	public Object addAssign(Object value, Context ctx) {
		Object result = Variant.add(param.getValue(), value);
		param.setValue(result);
		return result;
	}

	protected boolean containParam(String name) {
		return name.equals(param.getName());
	}

	protected void getUsedParams(Context ctx, ParamList resultList) {
		if (resultList.get(param.getName()) == null) {
			resultList.addVariable(param.getName(), param.getValue());
		}
	}

	public byte calcExpValueType(Context ctx) {
		Object val = param.getValue();
		if (val instanceof DBObject) {
			return Expression.TYPE_DB;
		} else if (val instanceof FileObject) {
			return Expression.TYPE_FILE;
		} else {
			return Expression.TYPE_OTHER;
		}
	}
	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		Sequence sequence = ctx.getComputeStack().getTopSequence();
		return new ConstArray(param.getValue(), sequence.length());
	}
	
	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		return calculateAll(ctx);
	}
	
	/**
	 * �����߼��������&&���Ҳ���ʽ
	 * @param ctx ����������
	 * @param leftResult &&�����ʽ�ļ�����
	 * @return BoolArray
	 */
	public BoolArray calculateAnd(Context ctx, IArray leftResult) {
		BoolArray result = leftResult.isTrue();
		
		if (Variant.isFalse(param.getValue())) {
			int size = result.size();
			for (int i = 1; i <= size; ++i) {
				result.set(i, false);
			}
		}
		
		return result;
	}
	
	/**
	 * ���ؽڵ��Ƿ񵥵�������
	 * @return true���ǵ��������ģ�false������
	 */
	public boolean isMonotone() {
		return true;
	}
}
