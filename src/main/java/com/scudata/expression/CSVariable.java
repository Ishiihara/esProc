package com.scudata.expression;

import java.util.List;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.cellset.INormalCell;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.util.Variant;

/**
 * ��Ԫ������
 * A1
 * @author RunQian
 *
 */
public class CSVariable extends Node {
	private INormalCell cell;

	public CSVariable(INormalCell cell) {
		this.cell = cell;
	}

	public Object assign(Object value, Context ctx) {
		cell.setValue(value);
		return value;
	}
	
	public Object addAssign(Object value, Context ctx) {
		Object result = Variant.add(cell.getValue(true), value);
		cell.setValue(result);
		return result;
	}

	public INormalCell getSourceCell() {
		return cell;
	}
	
	protected void getUsedCells(List<INormalCell> resultList) {
		if (!resultList.contains(cell)) {
			resultList.add(cell);
		}
	}
	
	public byte calcExpValueType(Context ctx) {
		return cell.calcExpValueType(ctx);
	}
	
	/**
	 * �Խڵ�������Ż���������Ԫ��Ͳ������ã����������ʽ����ɳ���
	 * @param ctx ����������
	 * @param Node �Ż���Ľڵ�
	 */
	public Node deepOptimize(Context ctx) {
		return new Constant(calculate(ctx));
	}

	public INormalCell calculateCell(Context ctx) {
		return cell;
	}

	public Object calculate(Context ctx) {
		return cell.getValue(true);
	}
	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		Sequence sequence = ctx.getComputeStack().getTopSequence();
		return new ConstArray(cell.getValue(true), sequence.length());
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
		Object value = cell.getValue(true);
		
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
}
