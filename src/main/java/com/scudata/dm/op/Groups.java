package com.scudata.dm.op;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.DataStruct;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.Gather;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;
import com.scudata.util.HashUtil;
import com.scudata.util.Variant;

/**
 * ��������������ִ�����������ܣ����ڹܵ����α�������ֶε�group�ӳټ��㺯��
 * @author RunQian
 *
 */
public class Groups extends Operation {
	private Expression []exps; // ������ʽ����
	private String []names; // �����ֶ�������
	private Expression[] newExps; // ���ܱ��ʽ����
	private String []newNames; // �����ֶ�������
	
	private Node []gathers; // ���ܱ��ʽ��Ӧ�Ļ��ܺ���
	private DataStruct newDs; // ��������ݽṹ
	private String opt; // ѡ��
	private boolean iopt = false; // �Ƿ���@iѡ��
	private boolean sopt = false; // �Ƿ���@sѡ����ۻ���ʽ����
	private boolean eopt = false; // �Ƿ���@eѡ��
	
	// ����ʱ�����ۻ�������ʹ��
	private Record r; // ��ǰ������ܵ��ļ�¼
	
	// group@q�����Ѱ�ǰ�벿�ֱ��ʽ����
	private Expression []sortExps; // ��벿����Ҫ������ʽ
	private Expression []groupExps; // ���Ƶ��ܵķ�����ʽ
	private String []sortNames;
	private Sequence data;
	private Object []values;
	
	// ǰ�벿������ʱ�����ۻ�������ʹ��
	private HashUtil hashUtil; // �ṩ��ϣ����Ĺ�ϣ��
	private ListBase1 []groups; // ��ϣ��
	private Table tempResult; // ������ܽ��
	private int []sortFields; // �����Է�������������
	
	private String []alterFields; // @bѡ��ʱ�������Ҫȥ�������ֶ�
	
	/**
	 * ��������������
	 * @param exps ǰ�벿������ķ����ֶα��ʽ
	 * @param names �ֶ�������
	 * @param newExps ���ܱ��ʽ
	 * @param newNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public Groups(Expression[] exps, String []names, 
			Expression[] newExps, String []newNames, String opt, Context ctx) {
		this(null, exps, names, newExps, newNames, opt, ctx);
	}
	
	/**
	 * ��������������
	 * @param function
	 * @param exps ǰ�벿������ķ����ֶα��ʽ
	 * @param names �ֶ�������
	 * @param newExps ���ܱ��ʽ
	 * @param newNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public Groups(Function function, Expression[] exps, String []names, 
			Expression[] newExps, String []newNames, String opt, Context ctx) {
		super(function);
		int count = exps.length;
		int newCount = newExps == null ? 0 : newExps.length;

		if (names == null) names = new String[count];
		for (int i = 0; i < count; ++i) {
			if (names[i] == null || names[i].length() == 0) {
				names[i] = exps[i].getFieldName();
			}
		}

		if (newNames == null) newNames = new String[newCount];
		for (int i = 0; i < newCount; ++i) {
			if (newNames[i] == null || newNames[i].length() == 0) {
				newNames[i] = newExps[i].getFieldName();
			}
		}

		String []totalNames = new String[count + newCount];
		System.arraycopy(names, 0, totalNames, 0, count);
		System.arraycopy(newNames, 0, totalNames, count, newCount);
		newDs = new DataStruct(totalNames);
		newDs.setPrimary(names);
		gathers = Sequence.prepareGatherMethods(newExps, ctx);
		
		if (opt != null) {
			if (opt.indexOf('i') != -1) iopt = true;
			if (opt.indexOf('s') != -1) sopt = true;
			if (opt.indexOf('e') != -1) eopt = true;
			if (newCount > 0 && opt.indexOf('b') != -1) {
				alterFields = newNames;
			}
		}

		this.exps = exps;
		this.names = names;
		this.newExps = newExps;
		this.newNames = newNames;
		this.opt = opt;
		
		if (sopt) {
			// �����ۻ�������
			gathers = Sequence.prepareGatherMethods(newExps, ctx);
		} else {
			for (int i = 0; i < newCount; ++i) {
				if (newExps[i].getHome() instanceof Gather) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("group" + mm.getMessage("function.invalidParam"));
				}
			}
			
			data = new Sequence();
			values = new Object[count];
		}
	}
	
	/**
	 * ��������������
	 * @param function
	 * @param exps ǰ�벿������ķ����ֶα��ʽ
	 * @param names �ֶ�������
	 * @param sortExps ��벿������ķ����ֶα��ʽ
	 * @param sortNames �ֶ�������
	 * @param newExps ���ܱ��ʽ
	 * @param newNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public Groups(Function function, Expression[] exps, String []names, 
			Expression[] sortExps, String []sortNames, 
			Expression[] newExps, String []newNames, String opt, Context ctx) {
		super(function);
		int count = exps.length;
		int sortCount = sortExps.length;
		int newCount = newExps == null ? 0 : newExps.length;
		int keyCount = count + sortCount;
		groupExps = new Expression[keyCount];
		
		if (names == null) names = new String[count];
		for (int i = 0; i < count; ++i) {
			groupExps[i] = dupExpression(exps[i], ctx);
			if (names[i] == null || names[i].length() == 0) {
				names[i] = exps[i].getFieldName();
			}
		}

		if (sortNames == null) sortNames = new String[sortCount];
		for (int i = 0; i < sortCount; ++i) {
			groupExps[i + count] = dupExpression(sortExps[i], ctx);
			if (sortNames[i] == null || sortNames[i].length() == 0) {
				sortNames[i] = sortExps[i].getFieldName();
			}
		}

		if (newNames == null) newNames = new String[newCount];
		for (int i = 0; i < newCount; ++i) {
			if (newNames[i] == null || newNames[i].length() == 0) {
				newNames[i] = newExps[i].getFieldName();
			}
		}

		String []pks = new String[keyCount];
		System.arraycopy(names, 0, pks, 0, count);
		System.arraycopy(sortNames, 0, pks, count, sortCount);
		
		String []totalNames = new String[keyCount + newCount];
		System.arraycopy(pks, 0, totalNames, 0, keyCount);
		System.arraycopy(newNames, 0, totalNames, keyCount, newCount);
		newDs = new DataStruct(totalNames);
		newDs.setPrimary(pks);

		if (opt != null) {
			if (opt.indexOf('i') != -1) iopt = true;
			if (opt.indexOf('s') != -1) sopt = true;
			if (opt.indexOf('e') != -1) eopt = true;
			if (newCount > 0 && opt.indexOf('b') != -1) {
				alterFields = newNames;
			}
		}
		
		this.exps = exps;
		this.names = names;
		this.sortExps = sortExps;
		this.sortNames = sortNames;
		this.newExps = newExps;
		this.newNames = newNames;
		this.opt = opt;
		
		if (sopt) {
			// �����ۻ�������
			gathers = Sequence.prepareGatherMethods(newExps, ctx);
			hashUtil = new HashUtil(1031);
			groups = new ListBase1[hashUtil.getCapacity()];
			tempResult = new Table(newDs, 1024);
			values = new Object[keyCount];
			
			sortFields = new int[keyCount];
			for (int i = 0; i < keyCount; ++i) {
				sortFields[i] = i;
			}
		} else {
			for (int i = 0; i < newCount; ++i) {
				if (newExps[i].getHome() instanceof Gather) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("group" + mm.getMessage("function.invalidParam"));
				}
			}
			
			data = new Sequence();
			values = new Object[count];
		}
	}
	
	/**
	 * ȡ�����Ƿ�����Ԫ������������˺�������ټ�¼
	 * �˺��������α�ľ�ȷȡ����������ӵĲ�������ʹ��¼��������ֻ�谴���������ȡ������
	 * @return true���ᣬfalse������
	 */
	public boolean isDecrease() {
		return true;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression []dupExps = dupExpressions(exps, ctx);
		Expression []dupNewExps = dupExpressions(newExps, ctx);
		if (sortExps == null) {
			return new Groups(function, dupExps, names, dupNewExps, newNames, opt, ctx);
		} else {
			Expression []dupSortExps = dupExpressions(sortExps, ctx);
			return new Groups(function, dupExps, names, 
					dupSortExps, sortNames, dupNewExps, newNames, opt, ctx);
		}
	}
	
	/**
	 * ����ȫ���������ʱ���ã��������һ�������
	 * @param ctx ����������
	 * @return Sequence
	 */
	public Sequence finish(Context ctx) {
		if (r != null) {
			// @i��@sѡ��
			Table result = new Table(r.dataStruct(), 1);
			result.getMems().add(r);
			r = null;
			return finishGroupsResult(result);
		} else if (data != null && data.length() > 0) {
			Table result = new Table(newDs);
			if (sortExps == null) {
				group1(data, values, newExps, ctx, result);
			} else {
				group(data, groupExps, newExps, ctx, result);
			}
			
			if (eopt) {
				result = result.fieldValues(result.getFieldCount() - 1).derive("o");
			} else if (alterFields != null) {
				result.alter(alterFields, null);
			}
			
			data = new Sequence(); //null; Ϊ���α�reset�ظ�ʹ����Ҫ���´�������
			return result;
		} else if (tempResult != null) {
			// @qsѡ��
			Table result = tempResult;
			tempResult = null;
			groups = null;
			
			result.sortFields(sortFields);
			return finishGroupsResult(result);
		} else {
			return null;
		}
	}

	private Sequence finishGroupsResult(Table result) {
		if (gathers != null) {
			result.finishGather(gathers);
		}
		
		if (eopt) {
			return result.fieldValues(result.getFieldCount() - 1).derive("o");
		}

		if (alterFields != null) {
			result.alter(alterFields, null);
		}
		
		return result;
	}
	
	private Sequence groups_i(Sequence seq, Context ctx) {
		DataStruct newDs = this.newDs;
		Expression boolExp = exps[0];
		Node []gathers = this.gathers;
		int valCount = gathers == null ? 0 : gathers.length;
		
		Table result = new Table(newDs);
		IArray mems = result.getMems();
		Record r = this.r;
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(seq);
		stack.push(current);

		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				current.setCurrent(i);
				if (r != null) {
					if (Variant.isTrue(boolExp.calculate(ctx))) {
						mems.add(r);
						r = new Record(newDs);
						r.setNormalFieldValue(0, Boolean.TRUE);
						for (int v = 0, f = 1; v < valCount; ++v, ++f) {
							r.setNormalFieldValue(f, gathers[v].gather(ctx));
						}
					} else {
						for (int v = 0, f = 1; v < valCount; ++v, ++f) {
							r.setNormalFieldValue(f, gathers[v].gather(r.getNormalFieldValue(f), ctx));
						}
					}
				} else {
					r = new Record(newDs);
					r.setNormalFieldValue(0, boolExp.calculate(ctx));
					for (int v = 0, f = 1; v < valCount; ++v, ++f) {
						r.setNormalFieldValue(f, gathers[v].gather(ctx));
					}
				}
			}
		} finally {
			stack.pop();
		}

		this.r = r;
		if (result.length() > 0) {
			return finishGroupsResult(result);
		} else {
			return null;
		}
	}
	
	// ��һ�����ݼ������
	private static void group1(Sequence data, Object []groupValues, Expression []newExps, Context ctx, Table result) {
		if (newExps == null) {
			result.newLast(groupValues);
			return;
		}
		
		Sequence seq = new Sequence(1);
		seq.add(data);
		int keyCount = groupValues.length;
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(seq);
		current.setCurrent(1);
		stack.push(current);

		// ����ۺ��ֶ�ֵ
		try {
			BaseRecord r = result.newLast(groupValues);
			for (Expression newExp : newExps) {
				r.setNormalFieldValue(keyCount++, newExp.calculate(ctx));
			}
		} finally {
			stack.pop();
		}
	}
	
	private Sequence groups_o(Sequence seq, Context ctx) {
		Expression[] exps = this.exps;
		int fcount1 = exps.length;
		Sequence data = this.data;
		Object []values = this.values;
		Object []prevValues = new Object[fcount1];
		Table result = new Table(newDs);

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(seq);
		stack.push(current);

		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				current.setCurrent(i);
				boolean isSame = true;
				
				// ����ǰ��α��ʽ������Ƿ���ǰһ����¼��ͬ
				for (int v = 0; v < fcount1; ++v) {
					if (isSame) {
						Object value = exps[v].calculate(ctx);
						if (!Variant.isEquals(values[v], value)) {
							System.arraycopy(values, 0, prevValues, 0, fcount1);
							isSame = false;
							values[v] = value;
						}
					} else {
						values[v] = exps[v].calculate(ctx);
					}
				}

				if (isSame || data.length() == 0) {
					data.add(current.getCurrent());
				} else {
					group1(data, prevValues, newExps, ctx, result);
					data = new Sequence(); // newExps����Ϊ~����Ҫ���´�������
					data.add(current.getCurrent());
				}
			}
		} finally {
			stack.pop();
		}

		this.data = data;
		if (result.length() > 0) {
			if (eopt) {
				result = result.fieldValues(result.getFieldCount() - 1).derive("o");
			} else if (alterFields != null) {
				result.alter(alterFields, null);
			}

			return result;
		} else {
			return null;
		}
	}
	
	// �����ֶ�����ʱ���ۻ���ʽ�������ֵ
	private Sequence groups_os(Sequence seq, Context ctx) {
		DataStruct newDs = this.newDs;
		Expression[] exps = this.exps;
		Node []gathers = this.gathers;
		int keyCount = exps.length;
		int valCount = gathers == null ? 0 : gathers.length;
		
		Table result = new Table(newDs);
		IArray mems = result.getMems();
		Record r = this.r;
		Object []keys = new Object[keyCount];

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(seq);
		stack.push(current);

		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				current.setCurrent(i);
				for (int k = 0; k < keyCount; ++k) {
					keys[k] = exps[k].calculate(ctx);
				}
				
				if (r != null) {
					if (Variant.compareArrays(r.getFieldValues(), keys, keyCount) == 0) {
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							r.setNormalFieldValue(f, gathers[v].gather(r.getNormalFieldValue(f), ctx));
						}
					} else {
						mems.add(r);
						r = new Record(newDs, keys);
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							r.setNormalFieldValue(f, gathers[v].gather(ctx));
						}
					}
				} else {
					r = new Record(newDs, keys);
					for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
						r.setNormalFieldValue(f, gathers[v].gather(ctx));
					}
				}
			}
		} finally {
			stack.pop();
		}

		this.r = r;
		if (result.length() > 0) {
			return finishGroupsResult(result);
		} else {
			return null;
		}
	}
	
	// �ȷ����ٶ�ÿ�����ݼ������
	private static void group(Sequence data, Expression []groupExps, Expression []newExps, Context ctx, Table result) {
		Sequence groups = data.group(groupExps, null, ctx);
		int len = groups.length();
		Sequence keyGroups = new Sequence(len);
		for (int i = 1; i <= len; ++i) {
			Sequence seq = (Sequence)groups.getMem(i);
			keyGroups.add(seq.getMem(1));
		}

		int oldCount = result.length();
		int keyCount = groupExps.length;
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(keyGroups);
		stack.push(current);

		// ��������ֶ�ֵ
		try {
			for (int i = 1; i <= len; ++i) {
				BaseRecord r = result.newLast();
				current.setCurrent(i);
				for (int c = 0; c < keyCount; ++c) {
					r.setNormalFieldValue(c, groupExps[c].calculate(ctx));
				}
			}
		} finally {
			stack.pop();
		}
		
		if (newExps == null) {
			return;
		}
		
		int valCount = newExps.length;
		current = new Current(groups);
		stack.push(current);

		// ����ۺ��ֶ�ֵ
		try {
			for (int i = 1; i <= len; ++i) {
				BaseRecord r = (BaseRecord)result.getMem(++oldCount);
				current.setCurrent(i);
				for (int c = 0; c < valCount; ++c) {
					r.setNormalFieldValue(c + keyCount, newExps[c].calculate(ctx));
				}
			}
		} finally {
			stack.pop();
		}
	}
	
	// ǰ���������ʱ��������÷����������ֵ
	private Sequence groups_q(Sequence seq, Context ctx) {
		Expression[] exps = this.exps;
		int fcount1 = exps.length;
		Sequence data = this.data;
		Object []values = this.values;
		Table result = new Table(newDs);

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(seq);
		stack.push(current);

		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				current.setCurrent(i);
				boolean isSame = true;
				
				// ����ǰ��α��ʽ������Ƿ���ǰһ����¼��ͬ
				for (int v = 0; v < fcount1; ++v) {
					if (isSame) {
						Object value = exps[v].calculate(ctx);
						if (!Variant.isEquals(values[v], value)) {
							isSame = false;
							values[v] = value;
						}
					} else {
						values[v] = exps[v].calculate(ctx);
					}
				}

				if (isSame || data.length() == 0) {
					data.add(current.getCurrent());
				} else {
					group(data, groupExps, newExps, ctx, result);
					data.clear();
					data.add(current.getCurrent());
				}
			}
		} finally {
			stack.pop();
		}

		if (result.length() > 0) {
			if (eopt) {
				result = result.fieldValues(result.getFieldCount() - 1).derive("o");
			} else if (alterFields != null) {
				result.alter(alterFields, null);
			}

			return result;
		} else {
			return null;
		}
	}
	
	// ǰ���������ʱ�����ۻ�������
	private Sequence groups_qs(Sequence seq, Context ctx) {
		int fcount1 = exps.length;
		Expression[] groupExps = this.groupExps;
		Node []gathers = this.gathers;
		int keyCount = groupExps.length;
		int valCount = gathers == null ? 0 : gathers.length;
		Object []values = this.values;
		HashUtil hashUtil = this.hashUtil;
		ListBase1 []groups = this.groups;
		Table tempResult = this.tempResult;
		Table result = new Table(newDs);
		
		final int INIT_GROUPSIZE = HashUtil.getInitGroupSize(); // ��ϣ��Ĵ�С
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(seq);
		stack.push(current);

		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				current.setCurrent(i);
				boolean isSame = true;
				
				// ����ǰ��α��ʽ������Ƿ���ǰһ����¼��ͬ
				for (int v = 0; v < fcount1; ++v) {
					if (isSame) {
						Object value = groupExps[v].calculate(ctx);
						if (!Variant.isEquals(values[v], value)) {
							isSame = false;
							values[v] = value;
						}
					} else {
						values[v] = groupExps[v].calculate(ctx);
					}
				}

				for (int v = fcount1; v < keyCount; ++v) {
					values[v] = groupExps[v].calculate(ctx);
				}
				
				if (!isSame) {
					// ��ǰ��������������ʱ����ֵ���浽�����
					tempResult.sortFields(sortFields);
					tempResult.finishGather(gathers);
					result.addAll(tempResult);
					tempResult.clear();
					
					for (int h = 0, capacity = groups.length; h < capacity; ++h) {
						groups[h] = null;
					}
				}
				
				// �ѵ�ǰ��¼�ۻ�����������
				int hash = hashUtil.hashCode(values);
				if (groups[hash] == null) {
					groups[hash] = new ListBase1(INIT_GROUPSIZE);
					BaseRecord r = tempResult.newLast(values);
					groups[hash].add(r);
					
					for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(ctx);
						r.setNormalFieldValue(f, val);
					}
				} else {
					int index = HashUtil.bsearch_r(groups[hash], values);
					if (index < 1) {
						BaseRecord r = tempResult.newLast(values);
						groups[hash].add(-index, r);
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(ctx);
							r.setNormalFieldValue(f, val);
						}
					} else {
						BaseRecord r = (BaseRecord)groups[hash].get(index);
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(r.getNormalFieldValue(f), ctx);
							r.setNormalFieldValue(f, val);
						}
					}
				}
			}
		} finally {
			stack.pop();
		}

		if (result.length() > 0) {
			if (eopt) {
				result = result.fieldValues(result.getFieldCount() - 1).derive("o");
			} else if (alterFields != null) {
				result.alter(alterFields, null);
			}

			return result;
		} else {
			return null;
		}
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		if (iopt) {
			return groups_i(seq, ctx);
		} else if (sortExps != null) {
			if (sopt) {
				return groups_qs(seq, ctx);
			} else {
				return groups_q(seq, ctx);
			}
		} else {
			if (sopt) {
				return groups_os(seq, ctx);
			} else {
				return groups_o(seq, ctx);
			}
		}
	}
}
