package com.scudata.expression;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.IComputeItem;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ����ȡ���еĵ�ǰԪ��
 * r.(~) A.(~)  A.(A.~)
 * @author WangXiaoJun
 *
 */
public class CurrentElement extends Node {
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
		ComputeStack stack = ctx.getComputeStack();
		if (sequence == null) { // ~
			return stack.getTopObject().getCurrent();
		} else { // A.~
			return stack.getCurrentValue(sequence);
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
		if (left == null) {
			ComputeStack stack = ctx.getComputeStack();
			IComputeItem item = stack.getTopObject();
			Sequence sequence = item.getCurrentSequence();
			
			if (sequence != null) {
				return sequence.getCurrentMems();
			} else {
				sequence = stack.getTopSequence();
				Object value = item.getCurrent();
				return new ConstArray(value, sequence.length());
			}
		}

		ComputeStack stack = ctx.getComputeStack();
		Sequence topSequence = stack.getTopSequence();
		IArray leftArray = left.calculateAll(ctx);
		
		if (leftArray instanceof ConstArray) {
			Object leftValue = ((ConstArray)leftArray).getData();
			if (!(leftValue instanceof Sequence)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("\".\"" + mm.getMessage("dot.seriesLeft"));
			}
			
			Sequence sequence = (Sequence)leftValue;			
			if (topSequence == sequence) {
				return sequence.getCurrentMems();
			} else {
				// A.(B.(A.~))
				Object value = stack.getCurrentValue(sequence);
				return new ConstArray(value, topSequence.length());
			}
		} else {
			int len = topSequence.length();
			ObjectArray result = new ObjectArray(len);
			result.setTemporary(true);
			for (int i = 1; i <= len; ++i) {
				Object leftValue = leftArray.get(i);
				if (!(leftValue instanceof Sequence)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("\".\"" + mm.getMessage("dot.seriesLeft"));
				}
				
				Object cur = stack.getCurrentValue((Sequence)leftValue);
				result.push(cur);
			}
			
			return result;
		}
	}
	
	public Object assign(Object value, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		if (sequence == null) { // ~
			IComputeItem temp = stack.getTopObject();
			if (temp instanceof Current) {
				((Current)temp).assign(value);
				return value;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + "~");
			}
		} else { // A.~
			Current current = stack.getSequenceCurrent(sequence);
			if (current == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("~" + mm.getMessage("engine.seriesNotInStack"));
			}

			current.assign(value);
			return value;
		}
	}
	
	public Object addAssign(Object value, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		if (sequence == null) { // ~
			IComputeItem temp = stack.getTopObject();
			if (temp instanceof Current) {
				Object result = Variant.add(((Current)temp).getCurrent(), value);
				((Current)temp).assign(result);
				return result;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + "~");
			}
		} else { // A.~
			Current current = stack.getSequenceCurrent(sequence);
			if (current == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("~" + mm.getMessage("engine.seriesNotInStack"));
			}

			Object result = Variant.add(current.getCurrent(), value);
			current.assign(result);
			return result;
		}
	}

	public Object move(Move node, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		if (sequence == null) { // ~
			IComputeItem temp = stack.getTopObject();
			if (temp instanceof Current) {
				Current current = (Current)temp;
				int pos = node.calculateIndex(current, ctx);
				return pos > 0 ? current.get(pos) : null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + "~");
			}
		} else { // A.~
			Current current = stack.getSequenceCurrent(sequence);
			if (current == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("\"[]\"" + mm.getMessage("engine.seriesNotInStack"));
			}

			int pos = node.calculateIndex(current, ctx);
			return pos > 0 ? current.get(pos) : null;
		}
	}

	public Object moveAssign(Move node, Object value, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		if (sequence == null) { // ~
			IComputeItem temp = stack.getTopObject();
			if (temp instanceof Current) {
				Current current = (Current)temp;
				int pos = node.calculateIndex(current, ctx);
				if (pos > 0) current.assign(pos, value);
				return value;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + "~");
			}
		} else { // A.~
			Current current = stack.getSequenceCurrent(sequence);
			if (current == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("\"[]\"" + mm.getMessage("engine.seriesNotInStack"));
			}

			int pos = node.calculateIndex(current, ctx);
			if (pos > 0) current.assign(pos, value);
			return value;
		}
	}
	
	public Object moves(Move node, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		if (sequence == null) { // ~
			IComputeItem temp = stack.getTopObject();
			if (temp instanceof Current) {
				Current current = (Current)temp;
				int []range = node.calculateIndexRange(current, ctx);
				if (range == null) {
					return new Sequence(0);
				}

				int startSeq = range[0];
				int endSeq = range[1];
				Sequence result = new Sequence(endSeq - startSeq + 1);
				for (; startSeq <= endSeq; ++startSeq) {
					result.add(current.get(startSeq));
				}

				return result;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + "~");
			}
		} else { // A.~
			Current current = stack.getSequenceCurrent(sequence);
			if (current == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("\"{}\"" + mm.getMessage("engine.seriesNotInStack"));
			}

			int []range = node.calculateIndexRange(current, ctx);
			if (range == null) return new Sequence(0);

			int startSeq = range[0];
			int endSeq = range[1];
			Sequence result = new Sequence(endSeq - startSeq + 1);
			for (; startSeq <= endSeq; ++startSeq) {
				result.add(current.get(startSeq));
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
