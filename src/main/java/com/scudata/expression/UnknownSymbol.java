package com.scudata.expression;

import java.util.List;

import com.scudata.array.BoolArray;
import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.DBSession;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.DBObject;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IComputeItem;
import com.scudata.dm.LinkEntry;
import com.scudata.dm.Param;
import com.scudata.dm.ParamList;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.resources.EngineMessage;
import com.scudata.util.EnvUtil;
import com.scudata.util.Variant;

/**
 * �������ʽ��ʱ��û����������ʶ����ʲô�������Ǳ���Ҳ�������ֶΡ�
 * ���й������ٸ��ݼ�����������ȷ�����ֶλ��Ǳ���
 * @author WangXiaoJun
 *
 */
public class UnknownSymbol extends Node {
	private String name;
	
	private IComputeItem computeItem; // �ϴμ���ʱ��Ӧ�ļ������
	private int col = -1; // �ϴμ����Ӧ���ֶ����������������Ż�
	private DataStruct prevDs; // �ϴμ����Ӧ�����ݽṹ�����������Ż�
	
	private Param param; // �ϴμ����Ӧ�ı���
	private DBObject db;  // �ϴμ����Ӧ�����ݿ�����
	private boolean isField = false; // �Ƿ��Ӧ�����ֶ�
	
	public UnknownSymbol(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	protected boolean containParam(String name) {
		return name.equals(this.name);
	}

	protected void getUsedParams(Context ctx, ParamList resultList) {
		Param param = EnvUtil.getParam(name, ctx);
		if (param != null && resultList.get(name) == null) {
			resultList.addVariable(name, param.getValue());
		}
	}
	
	public void getUsedFields(Context ctx, List<String> resultList) {
		if (EnvUtil.getParam(name, ctx) == null && ctx.getDBSession(name) == null) {
			resultList.add(name);
		}
	}
	
	/**
	 * ���ñ��ʽ�����ڱ��ʽ���棬���ִ��ʹ�ò�ͬ�������ģ�������������йصĻ�����Ϣ
	 */
	public void reset() {
		computeItem = null;
		col = -1; //
		prevDs = null;
		param = null;
		db = null;
		isField = false;
	}

	public byte calcExpValueType(Context ctx) {
		return Expression.TYPE_UNKNOWN;
	}

	public Object calculate(Context ctx) {
		// ����ϴμ�����ʽʱ��Ӧ�ļ�¼�����л��ڶ�ջ����ʹ���ϴε�
		ComputeStack stack = ctx.getComputeStack();
		if (computeItem != null && computeItem.isInStack(stack)) {
			Object obj = computeItem.getCurrent();
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
			} else if (obj instanceof Sequence) {
				// �����ǰԪ����������ȡ���һ��Ԫ��
				if (((Sequence)obj).length() == 0) {
					return null;
				}
				
				obj = ((Sequence)obj).get(1);
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
					return null;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
				}
			} else if (obj == null) {
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
			}
		} else {
			// ��������������ֶ������ұ���
			if (!isField) {
				if (param != null) {
					return param.getValue();
				} else if (db != null) {
					return db;
				}
				
				param = EnvUtil.getParam(name, ctx);
				if (param != null) { // ����
					return param.getValue();
				}
				
				// ���ݿ�����
				DBSession dbs = ctx.getDBSession(name);
				if (dbs != null) {
					db = new DBObject(dbs);
					return db;
				}
				
				isField = true;
			}

			// �ֶ�
			// ��һ����������㻷���Ѹı�
			boolean hasNull = false; // �Ƿ������е�һ����ԱΪ��
			for (LinkEntry<IComputeItem> entry = stack.getStackHeadEntry(); entry != null; entry = entry.getNext()) {
				IComputeItem item = entry.getElement();
				Object cur = item.getCurrent();
				if (cur instanceof BaseRecord) {
					BaseRecord r = (BaseRecord) cur;
					col = r.getFieldIndex(name);

					if (col >= 0) {
						computeItem = item;
						prevDs = r.dataStruct();
						return r.getNormalFieldValue(col);
					}
				} else if (cur instanceof Sequence) {
					// �����ǰԪ����������ȡ���һ��Ԫ��
					if (((Sequence)cur).length() == 0) {
						computeItem = item;
						return null;
					}
					
					cur = ((Sequence)cur).get(1);
					if (cur instanceof BaseRecord) {
						BaseRecord r = (BaseRecord) cur;
						col = r.getFieldIndex(name);

						if (col >= 0) {
							computeItem = item;
							prevDs = r.dataStruct();
							return r.getNormalFieldValue(col);
						}
					} else if (cur == null) {
						hasNull = true;
					}
				} else if (cur == null) {
					hasNull = true;
				}
			}

			if (hasNull) {
				return null;
			}
			
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
		}
	}

	// '=' ��ֵ����
	public Object assign(Object value, Context ctx) {
		// ����ϴμ�����ʽʱ��Ӧ�ļ�¼�����л��ڶ�ջ����ʹ���ϴε�
		ComputeStack stack = ctx.getComputeStack();
		if (computeItem != null && computeItem.isInStack(stack)) {
			Object obj = computeItem.getCurrent();
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

				cur.setNormalFieldValue(col, value);
			} else if (obj instanceof Sequence) {
				// �����ǰԪ����������ȡ���һ��Ԫ��
				if (((Sequence)obj).length() == 0) {
					return value;
				}
				
				obj = ((Sequence)obj).get(1);
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

					cur.setNormalFieldValue(col, value);
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
				}
			} else if (obj != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
			}
			
			return value;
		} else if (param != null) {
			param.setValue(value);
			return value;
		} else {
			if (computeItem == null) {
				param = EnvUtil.getParam(name, ctx); // ����
				if (param != null) {
					param.setValue(value);
					return value;
				}
			}

			// ��һ����������㻷���Ѹı�
			for (LinkEntry<IComputeItem> entry = stack.getStackHeadEntry(); entry != null; entry = entry.getNext()) {
				IComputeItem item = entry.getElement();
				Object cur = item.getCurrent();
				
				// ��ֵ�����������������Ԫ��Ϊnull��
				if (cur instanceof BaseRecord) {
					BaseRecord r = (BaseRecord) cur;
					col = r.getFieldIndex(name);

					if (col >= 0) {
						computeItem = item;
						prevDs = r.dataStruct();
						r.setNormalFieldValue(col, value);
						return value;
					}
				} else if (cur instanceof Table) {
					Table table = (Table)cur;
					DataStruct ds = table.dataStruct();
					col = ds.getFieldIndex(name);
					if (col >= 0) {
						computeItem = item;
						prevDs = ds;
						
						if (table.length() > 0) {
							table.getRecord(1).setNormalFieldValue(col, value);
						}
						
						return value;
					}
				} else if (cur instanceof Sequence) {
					// �����ǰԪ����������ȡ���һ��Ԫ��
					if (((Sequence)cur).length() == 0) {
						computeItem = item;
						return value;
					}

					cur = ((Sequence)cur).get(1);
					if (cur instanceof BaseRecord) {
						BaseRecord r = (BaseRecord) cur;
						col = r.getFieldIndex(name);

						if (col >= 0) {
							computeItem = item;
							prevDs = r.dataStruct();
							r.setNormalFieldValue(col, value);
							return value;
						}
					}
				}
			}

			// û���ҵ��ֶ����������������������
			param = new Param(name, Param.VAR, value);
			ctx.addParam(param);
			return value;
		}
	}

	// '+=' ��ֵ����
	public Object addAssign(Object value, Context ctx) {
		// ����ϴμ�����ʽʱ��Ӧ�ļ�¼�����л��ڶ�ջ����ʹ���ϴε�
		ComputeStack stack = ctx.getComputeStack();
		if (computeItem != null && computeItem.isInStack(stack)) {
			Object obj = computeItem.getCurrent();
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

				Object result = Variant.add(cur.getNormalFieldValue(col), value);
				cur.setNormalFieldValue(col, result);
				return result;
			} else if (obj instanceof Sequence) {
				// �����ǰԪ����������ȡ���һ��Ԫ��
				if (((Sequence)obj).length() == 0) {
					return value;
				}
				
				obj = ((Sequence)obj).get(1);
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

					Object result = Variant.add(cur.getNormalFieldValue(col), value);
					cur.setNormalFieldValue(col, result);
					return result;
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
				}
			} else if (obj != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
			}
			
			return value;
		} else if (param != null) {
			Object result = Variant.add(param.getValue(), value);
			param.setValue(result);
			return result;
		} else {
			if (computeItem == null) {
				param = EnvUtil.getParam(name, ctx); // ����
				if (param != null) {
					Object result = Variant.add(param.getValue(), value);
					param.setValue(result);
					return result;
				}
			}

			// ��һ����������㻷���Ѹı�
			for (LinkEntry<IComputeItem> entry = stack.getStackHeadEntry(); entry != null; entry = entry.getNext()) {
				IComputeItem item = entry.getElement();
				Object cur = item.getCurrent();
				
				// ��ֵ�����������������Ԫ��Ϊnull��
				if (cur instanceof BaseRecord) {
					BaseRecord r = (BaseRecord) cur;
					col = r.getFieldIndex(name);

					if (col >= 0) {
						computeItem = item;
						prevDs = r.dataStruct();

						Object result = Variant.add(r.getNormalFieldValue(col), value);
						r.setNormalFieldValue(col, result);
						return result;
					}
				} else if (cur instanceof Sequence) {
					// �����ǰԪ����������ȡ���һ��Ԫ��
					if (((Sequence)cur).length() == 0) {
						computeItem = item;
						return value;
					}

					cur = ((Sequence)cur).get(1);
					if (cur instanceof BaseRecord) {
						BaseRecord r = (BaseRecord) cur;
						col = r.getFieldIndex(name);

						if (col >= 0) {
							computeItem = item;
							prevDs = r.dataStruct();

							Object result = Variant.add(r.getNormalFieldValue(col), value);
							r.setNormalFieldValue(col, result);
							return result;
						}
					}
				}
			}

			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
		}
	}

	public Object move(Move node, Context ctx) {
		// ����ϴμ�����ʽʱ��Ӧ�����л��ڶ�ջ����ʹ���ϴε�
		ComputeStack stack = ctx.getComputeStack();
		if (computeItem != null && computeItem.isInStack(stack)) {
			Current current = (Current)computeItem;
			int pos = node.calculateIndex(current, ctx);
			if (pos < 1) {
				return null;
			}

			// �����ǰԪ����������ȡ���һ��Ԫ��
			Object obj = current.get(pos);
			if (obj instanceof Sequence) {
				if (((Sequence)obj).length() == 0) {
					return null;
				} else {
					obj = ((Sequence)obj).get(1);
				}
			}

			if (obj instanceof BaseRecord) {
				BaseRecord r = (BaseRecord)obj;
				if (prevDs != r.dataStruct()) {
					prevDs = r.dataStruct();
					col = prevDs.getFieldIndex(name);
					if (col < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
					}
				}

				return r.getNormalFieldValue(col);
			} else if (obj == null) {
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
			}
		}

		if (param != null || (computeItem == null && (param = EnvUtil.getParam(name, ctx)) != null)) {
			Object value = param.getValue();
			if (!(value instanceof Sequence)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("[]" + mm.getMessage("dot.seriesLeft"));
			}

			Current current = stack.getSequenceCurrent((Sequence)value);
			if (current == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("[]" + mm.getMessage("engine.seriesNotInStack"));
			}

			int pos = node.calculateIndex(current, ctx);
			return pos > 0 ? current.get(pos) : null;
		}

		// �ֶ�
		// ��һ����������㻷���Ѹı�
		for (LinkEntry<IComputeItem> entry = stack.getStackHeadEntry(); entry != null; entry = entry.getNext()) {
			IComputeItem item = entry.getElement();
			if (item instanceof Current) { // series.(...)
				Current current = (Current) item;
				Object curObj = current.getCurrent();

				// �����ǰԪ����������ȡ���һ��Ԫ��
				if (curObj instanceof Sequence) {
					if (((Sequence)curObj).length() > 0) {
						curObj = ((Sequence)curObj).get(1);
					} else {
						curObj = null;
					}
				}

				if (curObj instanceof BaseRecord) {
					BaseRecord cur = (BaseRecord) curObj;
					col = cur.getFieldIndex(name);

					if (col >= 0) {
						computeItem = item;
						prevDs = cur.dataStruct();

						int pos = node.calculateIndex(current, ctx);
						if (pos < 1) return null;

						Object obj = current.get(pos);
						if (obj == null) return null;
						if (!(obj instanceof BaseRecord)) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
						}

						BaseRecord r = (BaseRecord)obj;
						if (prevDs != r.dataStruct()) {
							prevDs = r.dataStruct();
							col = prevDs.getFieldIndex(name);
							if (col < 0) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
							}
						}

						return r.getNormalFieldValue(col);
					}
				} else if (curObj == null) {
					computeItem = item;
					int pos = node.calculateIndex(current, ctx);
					if (pos < 1) return null;

					Object obj = current.get(pos);
					if (obj instanceof Sequence) {
						if (((Sequence)obj).length() == 0) {
							return null;
						} else {
							obj = ((Sequence)obj).get(1);
						}
					}

					if (obj == null) return null;
					if (!(obj instanceof BaseRecord)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
					}

					BaseRecord r = ((BaseRecord)obj);
					prevDs = r.dataStruct();
					col = prevDs.getFieldIndex(name);
					if (col < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
					}

					return r.getNormalFieldValue(col);
				}
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
	}

	public Object moveAssign(Move node, Object value, Context ctx) {
		 // ����ϴμ�����ʽʱ��Ӧ�����л��ڶ�ջ����ʹ���ϴε�
		ComputeStack stack = ctx.getComputeStack();
		if (computeItem != null && computeItem.isInStack(stack)) {
			Current current = (Current)computeItem;
			int pos = node.calculateIndex(current, ctx);
			if (pos < 1) return value;

			 // �����ǰԪ����������ȡ���һ��Ԫ��
			Object obj = current.get(pos);
			if (obj instanceof Sequence) {
				if (((Sequence)obj).length() == 0) {
					return value;
				} else {
					obj = ((Sequence)obj).get(1);
				}
			}

			if (obj instanceof BaseRecord) {
				BaseRecord r = (BaseRecord)obj;
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
			} else if (obj == null) {
				return value;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
			}
		 }

		if (param != null || (computeItem == null && (param = EnvUtil.getParam(name, ctx)) != null)) {
			 Object obj = param.getValue();
			 if (!(obj instanceof Sequence)) {
				 MessageManager mm = EngineMessage.get();
				 throw new RQException("[]" + mm.getMessage("dot.seriesLeft"));
			 }

			 Current current = stack.getSequenceCurrent((Sequence)obj);
			 if (current == null) {
				 MessageManager mm = EngineMessage.get();
				 throw new RQException("[]" + mm.getMessage("engine.seriesNotInStack"));
			 }

			 int pos = node.calculateIndex(current, ctx);
			 if (pos > 0) current.assign(pos, value);
			 return value;
		 }

		 // �ֶ�
		 // ��һ����������㻷���Ѹı�
		for (LinkEntry<IComputeItem> entry = stack.getStackHeadEntry(); entry != null; entry = entry.getNext()) {
			IComputeItem item = entry.getElement();
			if (item instanceof Current) { // series.(...)
				Current current = (Current) item;
				Object curObj = current.getCurrent();

				// �����ǰԪ����������ȡ���һ��Ԫ��
				if (curObj instanceof Sequence) {
					if (((Sequence)curObj).length() > 0) {
						curObj = ((Sequence)curObj).get(1);
					} else {
						curObj = null;
					}
				}

				if (curObj instanceof BaseRecord) {
					BaseRecord cur = (BaseRecord) curObj;
					col = cur.getFieldIndex(name);

					if (col >= 0) {
						computeItem = item;
						prevDs = cur.dataStruct();

						int pos = node.calculateIndex(current, ctx);
						if (pos < 1) return value;

						Object obj = current.get(pos);
						if (obj == null) return value;
						if (!(obj instanceof BaseRecord)) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
						}

						BaseRecord r = (BaseRecord)obj;
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
				} else if (curObj == null) {
					computeItem = item;
					int pos = node.calculateIndex(current, ctx);
					if (pos < 1) return value;

					Object obj = current.get(pos);
					if (obj instanceof Sequence) {
						if (((Sequence)obj).length() == 0) {
							return value;
						} else {
							obj = ((Sequence)obj).get(1);
						}
					}

					if (obj == null) return value;
					if (!(obj instanceof BaseRecord)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
					}

					BaseRecord r = (BaseRecord)obj;
					prevDs = r.dataStruct();
					col = prevDs.getFieldIndex(name);
					if (col < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
					}

					r.setNormalFieldValue(col, value);
					return value;
				}
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
	}
	
	public Object moves(Move node, Context ctx) {
		// ����ϴμ�����ʽʱ��Ӧ�����л��ڶ�ջ����ʹ���ϴε�
		ComputeStack stack = ctx.getComputeStack();
		if (computeItem != null && computeItem.isInStack(stack)) {
			Current current = (Current)computeItem;
			int []range = node.calculateIndexRange(current, ctx);
			if (range == null) return new Sequence(0);
			return Move.getFieldValues(current, name, range[0], range[1]);
		}

		if (param != null || (computeItem == null && (param = EnvUtil.getParam(name, ctx)) != null)) {
			Object value = param.getValue();
			if (!(value instanceof Sequence)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("[]" + mm.getMessage("dot.seriesLeft"));
			}

			Current current = stack.getSequenceCurrent((Sequence)value);
			if (current == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("[]" + mm.getMessage("engine.seriesNotInStack"));
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

		// �ֶ�
		// ��һ����������㻷���Ѹı�
		for (LinkEntry<IComputeItem> entry = stack.getStackHeadEntry(); entry != null; entry = entry.getNext()) {
			IComputeItem item = entry.getElement();
			if (item instanceof Current) { // series.(...)
				Current current = (Current) item;
				Object curObj = current.getCurrent();

				if (curObj instanceof Sequence) {
					if (((Sequence)curObj).length() == 0) {
						curObj = null;
					} else {
						curObj = ((Sequence)curObj).get(1);
					}
				}

				if (curObj instanceof BaseRecord) {
					BaseRecord cur = (BaseRecord) curObj;
					col = cur.getFieldIndex(name);

					if (col >= 0) {
						computeItem = item;
						//prevRecord = cur;

						int []range = node.calculateIndexRange(current, ctx);
						if (range == null) return new Sequence(0);
						return Move.getFieldValues(current, name, range[0], range[1]);
					}
				} else if (curObj == null) {
					computeItem = item;
					int []range = node.calculateIndexRange(current, ctx);
					if (range == null) return new Sequence(0);
					return Move.getFieldValues(current, name, range[0], range[1]);
				}
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
	}

	public int getCol() {
		return col;
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
		if (!isField) {
			if (param != null) { // ����
				Sequence sequence = ctx.getComputeStack().getTopSequence();
				return new ConstArray(param.getValue(), sequence.length());
			} else if (db != null) { // ���ݿ�����
				Sequence sequence = ctx.getComputeStack().getTopSequence();
				return new ConstArray(db, sequence.length());
			}
			
			param = EnvUtil.getParam(name, ctx);
			if (param != null) { // ����
				Sequence sequence = ctx.getComputeStack().getTopSequence();
				return new ConstArray(param.getValue(), sequence.length());
			}
			
			// ���ݿ�����
			DBSession dbs = ctx.getDBSession(name);
			if (dbs != null) {
				Sequence sequence = ctx.getComputeStack().getTopSequence();
				db = new DBObject(dbs);
				return new ConstArray(db, sequence.length());
			}
			
			isField = true;
		}
		
		// ����calculateAllʱջ������϶�������
		ComputeStack stack = ctx.getComputeStack();
		Sequence sequence = stack.getTopSequence();
		
		if (sequence.containField(name)) {
			return sequence.getFieldValueArray(name);
		}

		int len = sequence.length();
		if (len == 0) {
			ObjectArray array = new ObjectArray(0);
			array.setTemporary(true);
			return array;
		}
		
		int i = 1;
		IArray result = null;
		DataStruct ds = null;
		int col = -1;
		
		// �ҳ���һ���ǿյĳ�Ա���ж��Ƿ��е�ǰ�ֶ�
		// ��Ա�����Ǽ�¼������
		for (; i <= len; ++i) {
			Object mem = sequence.getMem(i);
			if (mem instanceof Sequence) {
				// �����ǰԪ����������ȡ���һ��Ԫ��
				Sequence tmp = (Sequence)mem;
				if (tmp.length() > 0) {
					mem = tmp.getMem(1);
				} else {
					mem = null;
				}
			}

			if (mem instanceof BaseRecord) {
				BaseRecord r = (BaseRecord)mem;
				ds = r.dataStruct();
				col = ds.getFieldIndex(name);
				
				if (col != -1) {
					result = new ObjectArray(len);
					result.setTemporary(true);
					
					for (int j = 1; j < i; ++j) {
						result.push(null);
					}
					
					result.push(r.getNormalFieldValue(col));
				}
				
				break;
			} else if (mem != null) {
				// A.(B.(...))�����ϲ����е��ֶ�
				break;
			}
		}
		
		if (result != null) {
			// ��һ���ǿյĳ�Ա���е�ǰ�ֶ�
			for (++i; i <= len; ++i) {
				Object mem = sequence.getMem(i);
				if (mem instanceof Sequence) {
					// �����ǰԪ����������ȡ���һ��Ԫ��
					Sequence tmp = (Sequence)mem;
					if (tmp.length() > 0) {
						mem = tmp.getMem(1);
					} else {
						mem = null;
					}
				}

				if (mem instanceof BaseRecord) {
					BaseRecord r = (BaseRecord)mem;
					if (r.dataStruct() != ds) {
						ds = r.dataStruct();
						col = ds.getFieldIndex(name);
						if (col == -1) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
						}
					}
					
					result.push(r.getNormalFieldValue(col));
				} else if (mem != null) {
					result.push(null);
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}
			
			return result;
		} else {
			// A.(B.(...))�����ϲ����е��ֶ�
			Object value = getOuterFieldValue(ctx);
			result = new ConstArray(value, len);
			result.setTemporary(true);
			return result;
		}
	}
	
	// ȡ������е��ֶ�ֵ
	private Object getOuterFieldValue(Context ctx) {
		// ����ϴμ�����ʽʱ��Ӧ�ļ�¼�����л��ڶ�ջ����ʹ���ϴε�
		ComputeStack stack = ctx.getComputeStack();
		if (computeItem != null && computeItem.isInStack(stack)) {
			Object obj = computeItem.getCurrent();
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
			} else if (obj instanceof Sequence) {
				// �����ǰԪ����������ȡ���һ��Ԫ��
				if (((Sequence)obj).length() == 0) {
					return null;
				}
				
				obj = ((Sequence)obj).get(1);
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
					return null;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
				}
			} else if (obj == null) {
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
			}
		} else {
			// �ֶ�
			// ��һ����������㻷���Ѹı�
			boolean hasNull = false; // �Ƿ������е�һ����ԱΪ��
			LinkEntry<IComputeItem> entry = stack.getStackHeadEntry();
			for (entry = entry.getNext(); entry != null; entry = entry.getNext()) {
				IComputeItem item = entry.getElement();
				Object cur = item.getCurrent();
				if (cur instanceof BaseRecord) {
					BaseRecord r = (BaseRecord) cur;
					col = r.getFieldIndex(name);

					if (col >= 0) {
						computeItem = item;
						prevDs = r.dataStruct();
						return r.getNormalFieldValue(col);
					}
				} else if (cur instanceof Sequence) {
					// �����ǰԪ����������ȡ���һ��Ԫ��
					if (((Sequence)cur).length() == 0) {
						computeItem = item;
						return null;
					}
					
					cur = ((Sequence)cur).get(1);
					if (cur instanceof BaseRecord) {
						BaseRecord r = (BaseRecord) cur;
						col = r.getFieldIndex(name);

						if (col >= 0) {
							computeItem = item;
							prevDs = r.dataStruct();
							return r.getNormalFieldValue(col);
						}
					} else if (cur == null) {
						hasNull = true;
					}
				} else if (cur == null) {
					hasNull = true;
				}
			}

			if (hasNull) {
				return null;
			}
			
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
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
