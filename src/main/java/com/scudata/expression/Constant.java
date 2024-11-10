package com.scudata.expression;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.dm.Context;
import com.scudata.dm.DBObject;
import com.scudata.dm.Sequence;
import com.scudata.util.Variant;

/**
 * �����ڵ�
 * @author WangXiaoJun
 *
 */
public class Constant extends Node {
	protected Object value;

	public Constant(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public byte calcExpValueType(Context ctx) {
		if (value instanceof DBObject) {
			return Expression.TYPE_DB;
		} else {
			return Expression.TYPE_OTHER;
		}
	}

	// �ϲ��ַ�������
	public boolean append(Constant c) {
		if (value instanceof String && c.value instanceof String) {
			value = (String)value + (String)c.value;
			return true;
		} else {
			return false;
		}
	}

	public Object calculate(Context ctx) {
		return value;
	}
	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		Sequence sequence = ctx.getComputeStack().getTopSequence();
		return new ConstArray(value, sequence.length());
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
		
		if (Variant.isFalse(value)) {
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

	public void setValue(Object value) {
		this.value = value;
	}
}
