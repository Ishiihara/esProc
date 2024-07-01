package com.scudata.expression;

import java.util.List;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IComputeItem;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �ֶ�����
 * A.f r.f ~.f
 * @author WangXiaoJun
 *
 */
public class FieldRef extends Node {
	protected String name;
	protected Object s2r; // ���л��¼
	protected int col; // �ֶ�����
	protected DataStruct prevDs;

	private Node left; // ������������ڵ�	

	public FieldRef(String fieldName) {
		name = fieldName;
	}

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
	
	public String getName() {
		return name;
	}
	
	public void getUsedFields(Context ctx, List<String> resultList) {
		resultList.add(name);
	}
	
	public boolean isLeftTypeMatch(Object obj) {
		return true;
	}

	public void setDotLeftObject(Object obj) {
		s2r = obj;
	}
	
	/**
	 * �ͷŽڵ����õĵ���������Ķ���
	 */
	public void releaseDotLeftObject() {
		s2r = null;
	}

	public Object calculate(Context ctx) {
		if (s2r instanceof Sequence) {
			ComputeStack stack = ctx.getComputeStack();
			Object obj = stack.getCurrentValue((Sequence)s2r);

			// �����ǰԪ����������ȡ���һ��Ԫ��
			if (obj instanceof Sequence) {
				if (((Sequence)obj).length() == 0) {
					return null;
				} else {
					obj = ((Sequence)obj).get(1);
				}
			}

			if (obj instanceof BaseRecord) {
				BaseRecord cur = (BaseRecord)obj;
				if (prevDs != cur.dataStruct()) {
					prevDs = cur.dataStruct();
					col = prevDs.getFieldIndex(name);
					if (col < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
					}
				}

				return cur.getNormalFieldValue(col);
			} else if (obj == null) {
				// ���һ���Ƿ��������ֶΣ���ֹT.f(...)д���������ͳ�ȡf�ĳ�Ա��
				if (s2r instanceof Table) {
					col = ((Table)s2r).dataStruct().getFieldIndex(name);
					if (col < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
					}					
				}
				
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
			}
		} else if (s2r instanceof BaseRecord) {
			BaseRecord cur = (BaseRecord)s2r;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}

			return cur.getNormalFieldValue(col);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
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

	private IArray getField(Object leftObj, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		Sequence top = stack.getTopSequence();
		
		if (leftObj == top) {
			// T.fn(T.field)
			return top.getFieldValueArray(name);
		} else if (leftObj instanceof Sequence) {
			Sequence sequence = (Sequence)leftObj;
			int n = stack.getCurrentIndex(sequence);
			
			if (n > 0) {
				leftObj = sequence.getMem(n);
				if (leftObj instanceof Sequence) {
					// �����ǰԪ����������ȡ���һ��Ԫ��
					if (((Sequence)leftObj).length() > 0) {
						leftObj = ((Sequence)leftObj).getMem(1);
					} else {
						leftObj = null;
					}
				}
			} else if (sequence.length() > 0) {
				leftObj = sequence.getMem(1);
			} else {
				leftObj = null;
			}
			
			if (leftObj instanceof BaseRecord) {
				BaseRecord r = (BaseRecord)leftObj;
				if (prevDs != r.dataStruct()) {
					prevDs = r.dataStruct();
					col = prevDs.getFieldIndex(name);
					if (col < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
					}
				}
				
				leftObj = r.getNormalFieldValue(col);
			} else if (leftObj != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
			}
			
			return new ConstArray(leftObj, top.length());
		} else if (leftObj instanceof BaseRecord) {
			BaseRecord r = (BaseRecord)leftObj;
			if (prevDs != r.dataStruct()) {
				prevDs = r.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}
			
			return new ConstArray(r.getNormalFieldValue(col), top.length());
		} else if (leftObj == null) {
			return new ConstArray(null, top.length());
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
		}
	}
	
	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		if (left instanceof CurrentElement) {
			// ~.f
			ComputeStack stack = ctx.getComputeStack();
			IComputeItem item = stack.getTopObject();
			return item.getCurrentSequence().getFieldValueArray(name);
		} else if (left instanceof ElementRef) {
			return ((ElementRef)left).getFieldArray(ctx, this);
		}
		
		// A.f
		IArray leftArray = left.calculateAll(ctx);
		if (leftArray instanceof ConstArray) {
			Object leftObj = leftArray.get(1);
			return getField(leftObj, ctx);
		} else {
			return getFieldArray(leftArray);
		}
	}
	
	public IArray getFieldArray(IArray leftArray) {
		// ���������ʽ����fk.f��A.f
		int len = leftArray.size();
		Object src = leftArray.get(1);
		IArray result;
		
		if (src instanceof BaseRecord) {
			BaseRecord r = (BaseRecord)src;
			if (prevDs != r.dataStruct()) {
				prevDs = r.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}
			
			result = r.createFieldValueArray(col, len);
		} else {
			result = new ObjectArray(len);
		}
		
		if (result instanceof ObjectArray) {
			for (int i = 1; i <= len; ++i) {
				src = leftArray.get(i);
				if (src instanceof Sequence) {
					Sequence seq = (Sequence)src;
					if (seq.length() > 0) {
						src = seq.getMem(1);
					} else {
						src = null;
					}
				}
	
				if (src instanceof BaseRecord) {
					BaseRecord r = (BaseRecord)src;
					if (prevDs != r.dataStruct()) {
						prevDs = r.dataStruct();
						col = prevDs.getFieldIndex(name);
						if (col < 0) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
						}
					}
					
					r.getNormalFieldValue(col, result);
				} else if (src == null) {
					result.push(null);
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}
		} else {
			// �����
			for (int i = 1; i <= len; ++i) {
				src = leftArray.get(i);
				if (src instanceof BaseRecord) {
					BaseRecord r = (BaseRecord)src;
					r.getNormalFieldValue(col, result);
				} else if (src == null) {
					result.push(null);
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}
		}
		
		result.setTemporary(true);
		return result;
	}
	
	// '=' ���ֶν��и�ֵ
	public Object assign(Object value, Context ctx) {
		if (s2r instanceof Sequence) {
			ComputeStack stack = ctx.getComputeStack();
			Object obj = stack.getCurrentValue((Sequence)s2r);
			if (obj == null) return value;

			if (!(obj instanceof BaseRecord)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
			}

			BaseRecord cur = (BaseRecord)obj;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}

			cur.setNormalFieldValue(col, value);
		} else if (s2r instanceof BaseRecord) {
			BaseRecord cur = (BaseRecord)s2r;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}
			
			cur.setNormalFieldValue(col, value);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
		}

		return value;
	}

	// '+=' ���ֶν��и�ֵ
	public Object addAssign(Object value, Context ctx) {
		if (s2r instanceof Sequence) {
			ComputeStack stack = ctx.getComputeStack();
			Object obj = stack.getCurrentValue((Sequence)s2r);
			if (obj == null) return value;

			if (!(obj instanceof BaseRecord)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
			}

			BaseRecord cur = (BaseRecord)obj;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}

			Object result = Variant.add(cur.getNormalFieldValue(col), value);
			cur.setNormalFieldValue(col, result);
			return result;
		} else if (s2r instanceof BaseRecord) {
			BaseRecord cur = (BaseRecord)s2r;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}
			
			Object result = Variant.add(cur.getNormalFieldValue(col), value);
			cur.setNormalFieldValue(col, result);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
		}
	}
	
	public Object move(Move node, Context ctx) {
		if (!(s2r instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("dot.seriesLeft"));
		}

		ComputeStack stack = ctx.getComputeStack();
		Current current = stack.getSequenceCurrent((Sequence)s2r);
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("engine.seriesNotInStack"));
		}

		int pos = node.calculateIndex(current, ctx);
		if (pos < 1) return null;

		Object mem = current.get(pos);
		if (mem == null) return null;
		if (mem instanceof BaseRecord) {
			BaseRecord r = (BaseRecord)mem;
			if (prevDs != r.dataStruct()) {
				prevDs = r.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}

			return r.getNormalFieldValue(col);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
		}
	}

	public Object moveAssign(Move node, Object value, Context ctx) {
		if (!(s2r instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("dot.seriesLeft"));
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = stack.getSequenceCurrent((Sequence)s2r);
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("engine.seriesNotInStack"));
		}
		
		int pos = node.calculateIndex(current, ctx);
		if (pos < 1) return value;
		
		Object mem = current.get(pos);
		if (mem == null) return value;
		if (!(mem instanceof BaseRecord)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
		}
		
		BaseRecord r = (BaseRecord)mem;
		if (prevDs != r.dataStruct()) {
			prevDs = r.dataStruct();
			col = prevDs.getFieldIndex(name);
			if (col < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
			}
		}

		r.setNormalFieldValue(col, value);
		return value;
	}

	public Object moves(Move node, Context ctx) {
		if (!(s2r instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("dot.seriesLeft"));
		}

		ComputeStack stack = ctx.getComputeStack();
		Current current = stack.getSequenceCurrent((Sequence)s2r);
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("engine.seriesNotInStack"));
		}

		int []range = node.calculateIndexRange(current, ctx);
		if (range == null) return new Sequence(0);
		return Move.getFieldValues(current, name, range[0], range[1]);
	}
	
	public int getCol() {
		return col;
	}
	
	/**
	 * ���ؽڵ��Ƿ񵥵�������
	 * @return true���ǵ��������ģ�false������
	 */
	public boolean isMonotone() {
		return true;
	}
}
