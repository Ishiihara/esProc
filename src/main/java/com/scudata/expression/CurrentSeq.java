package com.scudata.expression;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.IComputeItem;
import com.scudata.dm.LinkEntry;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;

/**
 * ���еĵ�ǰѭ�����
 * #  A.#
 * @author RunQian
 *
 */
public class CurrentSeq extends Node {
	private Sequence sequence;
	private Node left; // ������������ڵ�	

	/**
	 * ȡ�ڵ�����ڵ㣬û�з��ؿ�
	 * @return Node
	 */
	public Node getLeft() {
		return left;
	}

	/**
	 * ���ýڵ�����ڵ�
	 * @param node �ڵ�
	 */
	public void setLeft(Node node) {
		left = node;
	}

	public void setDotLeftObject(Object obj) {
		if (obj instanceof Sequence) {
			sequence = (Sequence)obj;
		} else if (obj == null) {
			sequence = null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\".\"" + mm.getMessage("dot.seriesLeft"));
		}
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		sequence = null;
	}

	public Object calculate(Context ctx) {
		if (sequence == null) { // #
			ComputeStack stack = ctx.getComputeStack();
			for (LinkEntry<IComputeItem> entry = stack.getStackHeadEntry(); entry != null; entry = entry.getNext()) {
				IComputeItem temp = entry.getElement();
				if (temp instanceof Current) {
					return ObjectCache.getInteger(((Current)temp).getCurrentIndex());
				}
			}

			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + "#");
		} else { // A.#
			ComputeStack stack = ctx.getComputeStack();
			return ObjectCache.getInteger(stack.getCurrentIndex(sequence));
		}
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
		IArray array = calculateAll(ctx);
		
		for (int i = 1, size = result.size(); i <= size; ++i) {
			if (result.isTrue(i) && array.isFalse(i)) {
				result.set(i, false);
			}
		}
		
		return result;
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		Sequence topSequence = stack.getTopSequence();
		if (left == null) {
			IntArray array = new IntArray(1, topSequence.length());
			array.setTemporary(true);
			return array;
		}

		IArray leftArray = left.calculateAll(ctx);
		if (leftArray instanceof ConstArray) {
			Object leftValue = ((ConstArray)leftArray).getData();
			if (!(leftValue instanceof Sequence)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("\".\"" + mm.getMessage("dot.seriesLeft"));
			}
			
			Sequence sequence = (Sequence)leftValue;			
			if (topSequence == sequence) {
				IntArray array = new IntArray(1, topSequence.length());
				array.setTemporary(true);
				return array;
			} else {
				// A.(B.(A.~))
				Object value = ObjectCache.getInteger(stack.getCurrentIndex(sequence));
				return new ConstArray(value, topSequence.length());
			}
		} else {
			int len = topSequence.length();
			IntArray result = new IntArray(len);
			result.setTemporary(true);
			for (int i = 1; i <= len; ++i) {
				Object leftValue = leftArray.get(i);
				if (!(leftValue instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("\".\"" + mm.getMessage("dot.seriesLeft"));
				}
				
				int seq = stack.getCurrentIndex((Sequence)leftValue);
				result.pushInt(seq);
			}
			
			return result;
		}
	}
	
	/**
	 * ���ؽڵ��Ƿ񵥵�������
	 * @return true���ǵ��������ģ�false������
	 */
	public boolean isMonotone() {
		return true;
	}
}
