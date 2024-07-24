package com.scudata.dm.cursor;

import java.util.ArrayList;

import com.scudata.array.IArray;
import com.scudata.cellset.INormalCell;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IResource;
import com.scudata.dm.Param;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.Current;
import com.scudata.dm.op.Channel;
import com.scudata.dm.op.GroupxResult;
import com.scudata.dm.op.IDResult;
import com.scudata.dm.op.IGroupsResult;
import com.scudata.dm.op.Operable;
import com.scudata.dm.op.Operation;
import com.scudata.dw.ColPhyTable;
import com.scudata.dw.JoinCursor;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CursorUtil;
import com.scudata.util.Variant;

/**
 * �α���࣬������Ҫʵ��get��skipOver����
 * @author WangXiaoJun
 *
 */
abstract public class ICursor extends Operable implements IResource {
	public static final int MAXSIZE = Integer.MAX_VALUE - 1; // ���fetch�������ڴ�ֵ��ʾȡ����
	public static final long MAXSKIPSIZE = Long.MAX_VALUE; // ���skip�Ĳ������ڴ�ֵ��ʾ��������
	
	public static int INITSIZE = 99999; // ȡ��������ʱ���������л����ĳ�ʼ��С
	public static int FETCHCOUNT = 9999; // �������ÿ�δ��α��ȡ���ݵ�����
	public static final int FETCHCOUNT_M = 999; // ��·�α겢�м���ʱÿһ·��ȡ���ݵ�����

	protected Sequence cache; // �и��Ӳ������ߵ�����peek����˳�Ա��Ž�����Ҫ����ȡ�Ĳ�������
	protected ArrayList<Operation> opList; // ���Ӳ����б�
	protected Context ctx; // �ö��߳��α�ȡ��ʱ��Ҫ���������Ĳ����½������ʽ
	
	protected DataStruct dataStruct; // ��������ݽṹ
	private boolean isDecrease = false; // ���ӵ������Ƿ��ʹ���ݱ��٣�����select
	private boolean isFinished = false; // �Ƿ������finish
	
	/**
	 * ȡ�α��Ĭ��ȡ����С
	 * @return
	 */
	public static int getFetchCount() {
		return FETCHCOUNT;
	}

	/**
	 * �����α��Ĭ��ȡ����С
	 * @param count
	 */
	public static void setFetchCount(int count) {
		FETCHCOUNT = count;
	}

	
	public static int getInitSize() {
		return INITSIZE;
	}

	public static void setInitSize(int size) {
		INITSIZE = size;
	}

	/**
	 * ���м���ʱ��Ҫ�ı�������
	 * ��������õ��˱��ʽ����Ҫ�������������½������ʽ
	 * �������ش˷���ʱ��Ҫ����һ�¸���ķ���
	 * @param ctx
	 */
	public void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			this.ctx = ctx;
			opList = duplicateOperations(ctx);
		}
	}
	
	/**
	 * ȡ����������
	 * @return
	 */
	public Context getContext() {
		return ctx;
	}
	
	private ArrayList<Operation> duplicateOperations(Context ctx) {
		ArrayList<Operation> opList = this.opList;
		if (opList == null) return null;
				
		ArrayList<Operation> newList = new ArrayList<Operation>(opList.size());
		for (Operation op : opList) {
			newList.add(op.duplicate(ctx));
		}
		
		return newList;
	}
	
	/**
	 * �����α��������
	 * ��·�α���߳�����ʱ��Ҫ����������
	 * @param ctx
	 */
	public void setContext(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * Ϊ�α긽������
	 * @param op ����
	 * @param ctx ����������
	 */
	public Operable addOperation(Operation op, Context ctx) {
		if (opList == null) {
			opList = new ArrayList<Operation>();
		}
		
		opList.add(op);
		if (op.isDecrease()) {
			isDecrease = true;
		}
		
		if (this.ctx == null) {
			this.ctx = ctx;
		}
		
		// �ֶβ����α�ʱ���ڶ����α��ʱ����ܻ�����������ݻ�����
		if (cache != null) {
			cache = op.process(cache, ctx);
		}
		
		return this;
	}
	
	/**
	 * ���������кϲ���һ�����У����ݽṹ�Ƿ�����������Ƿ����������
	 * ���ںϲ��α���ȡ���õ��Ľ��
	 * @param dest ����
	 * @param src ����
	 * @return Sequence
	 */
	public static Sequence append(Sequence dest, Sequence src) {
		if (src != null) {
			return dest.append(src);
		} else {
			return dest;
		}
	}
	
	protected Sequence doOperation(Sequence result, ArrayList<Operation> opList, Context ctx) {
		for (Operation op : opList) {
			if (result == null || result.length() == 0) {
				return null;
			}
			
			try {
				result = op.process(result, ctx);
			} catch (RQException e) {
				INormalCell cell = op.getCurrentCell();
				if (cell != null) {
					MessageManager mm = EngineMessage.get();
					e.setMessage(mm.getMessage("error.cell", cell.getCellId()) + e.getMessage());
				}
				
				throw e;
			} catch (RuntimeException e) {
				INormalCell cell = op.getCurrentCell();
				if (cell != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("error.cell", cell.getCellId()) + e.getMessage(), e);
				} else {
					throw e;
				}
			}
		}
		
		return result;
	}
	
	protected Sequence finish(ArrayList<Operation> opList, Context ctx) {
		isFinished = true;
		Sequence result = null;
		for (Operation op : opList) {
			if (result == null || result.length() == 0) {
				result = op.finish(ctx);
			} else {
				result = op.process(result, ctx);
				Sequence tmp = op.finish(ctx);
				if (tmp != null) {
					if (result != null) {
						result = append(result, tmp);
					} else {
						result = tmp;
					}
				}
			}
		}
		
		return result;
	}
	
	public synchronized Sequence peek(int n) {
		ArrayList<Operation> opList = this.opList;
		if (opList == null) {
			if (cache == null) {
				cache = get(n);
			} else if (cache.length() < n) {
				cache = append(cache, get(n - cache.length()));
			} else if (cache.length() > n) {
				return cache.get(1, n + 1);
			}
			
			return cache;
		}
		
		int size;
		if (n > FETCHCOUNT && n < MAXSIZE) {
			size = n;
		} else {
			size = FETCHCOUNT;
		}
		
		while (cache == null || cache.length() < n) {
			Sequence cur = get(size);
			if (cur == null || cur.length() == 0) {
				Sequence tmp = finish(opList, ctx);
				if (tmp != null) {
					if (cache == null) {
						cache = tmp;
					} else {
						cache = append(cache, tmp);
					}
				}

				return cache;
			} else {
				cur = doOperation(cur, opList, ctx);
				if (cache == null) {
					cache = cur;
				} else if (cur != null) {
					cache = append(cache, cur);
				}
			}
		}
		
		if (cache.length() == n) {
			return cache;
		} else {
			if (cache instanceof Table) {
				Table table = new Table(cache.dataStruct(), n);
				table.getMems().addAll(cache.getMems(), n);
				return table;
			} else {
				Sequence seq = new Sequence(n);
				seq.getMems().addAll(cache.getMems(), n);
				return seq;
			}
		}
	}

	/**
	 * ���ȡָ�������ļ�¼��ȡ�ļ�¼�����ܲ�����n
	 * @param n ����
	 * @return Sequence
	 */
	public Sequence fuzzyFetch(int n) {
		if (cache == null) {
			Sequence result = null;
			ArrayList<Operation> opList = this.opList;
			
			do {
				Sequence cur = fuzzyGet(n);
				if (cur != null) {
					if (opList != null) {
						cur = doOperation(cur, opList, ctx);
						if (result == null) {
							result = cur;
						} else if (cur != null) {
							result = append(result, cur);
						}
					} else {
						if (result == null) {
							result = cur;
						} else {
							result = append(result, cur);
						}
					}
				} else {
					if (opList != null) {
						Sequence tmp = finish(opList, ctx);
						if (tmp != null) {
							if (result == null) {
								result = tmp;
							} else {
								result = append(result, tmp);
							}
						}
					}

					close();
					return result;
				}
			} while (result == null || result.length() < n);
			
			return result;
		} else {
			Sequence result = cache;
			cache = null;
			return result;
		}
	}
	
	/**
	 * ����ʣ��ļ�¼���ر��α�
	 * @return Sequence
	 */
	public Sequence fetch() {
		return fetch(MAXSIZE);
	}

	/**
	 * ȡָ�������ļ�¼
	 * @param n Ҫȡ�ļ�¼��
	 * @return Sequence
	 */
	public synchronized Sequence fetch(int n) {
		if (n < 1) {
			return null;
		}
		
		ArrayList<Operation> opList = this.opList;
		Sequence result = cache;
		if (opList == null) {
			if (result == null) {
				result = get(n);
				if (result == null || result.length() < n) {
					close();
				}
				
				return result;
			} else if (result.length() > n) {
				return result.split(1, n);
			} else if (result.length() == n) {
				cache = null;
				return result;
			} else {
				cache = null;
				result = append(result, get(n - result.length()));
				if (result == null || result.length() < n) {
					close();
				}
				
				return result;
			}
		}
		
		// ����������˵���¼�ֲ���ȡ��������ʵ����ȡ
		int size;
		if ((n > FETCHCOUNT || !isDecrease) && n < MAXSIZE) {
			size = n;
		} else {
			size = FETCHCOUNT;
		}
		
		while (result == null || result.length() < n) {
			Sequence cur = get(size);
			if (cur == null) {
				Sequence tmp = finish(opList, ctx);
				if (tmp != null) {
					if (result == null) {
						result = tmp;
					} else {
						result = append(result, tmp);
					}
				}
				
				close();
				return result;
			} else {
				int len = cur.length();
				cur = doOperation(cur, opList, ctx);
				if (result == null) {
					result = cur;
				} else if (cur != null) {
					result = append(result, cur);
				}
				
				if (len < size) {
					Sequence tmp = finish(opList, ctx);
					if (tmp != null) {
						if (result == null) {
							result = tmp;
						} else {
							result = append(result, tmp);
						}
					}
					
					if (result == null || result.length() < n) {
						close();
						return result;
					}
				}
			}
		}
		
		if (result.length() == n) {
			cache = null;
			return result;
		} else {
			cache = result.split(n + 1);
			return result;
		}
	}
	
	/**
	 * ��ָ�����ʽȡn������
	 * @param exps ���ʽ����
	 * @param n ����
	 * @param ctx ����������
	 * @return Sequence
	 */
	public synchronized Sequence fetchGroup(Expression[] exps, int n, Context ctx) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		Sequence newTable = null;
		int keyCount = exps.length; 
		Object []keys = new Object[keyCount];

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(data);
		stack.push(current);
		current.setCurrent(1);
		int index = 2;
		int count = 0;

		try {
			for (int k = 0; k < keyCount; ++k) {
				keys[k] = exps[k].calculate(ctx);
			}

			End:
			while (true) {
				for (int len = data.length(); index <= len; ++index) {
					current.setCurrent(index);
					for (int k = 0; k < keyCount; ++k) {
						if (!Variant.isEquals(keys[k], exps[k].calculate(ctx))) {
							if (count + index >= n) {
								break End;
							} else {
								for (int j = 0; j < keyCount; ++j) {
									keys[j] = exps[j].calculate(ctx);
								}
							}
						}
					}
				}

				if (newTable == null) {
					newTable = data;
				} else {
					newTable.getMems().addAll(data.getMems());
				}
				count = newTable.length();
				
				data = fuzzyFetch(FETCHCOUNT);
				if (data == null) break;

				index = 1;
				stack.pop();
				current = new Current(data);
				stack.push(current);
			}
		} finally {
			stack.pop();
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(data.getMems());
			}
		}

		return newTable;
	}

	/**
	 * ��ָ�����ʽȡһ������
	 * @param exps ���ʽ����
	 * @param ctx ����������
	 * @return Sequence
	 */
	public synchronized Sequence fetchGroup(Expression[] exps, Context ctx) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		Sequence newTable = null;
		int keyCount = exps.length; 
		Object []keys = new Object[keyCount];

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(data);
		stack.push(current);
		current.setCurrent(1);
		int index = 2;

		try {
			for (int k = 0; k < keyCount; ++k) {
				keys[k] = exps[k].calculate(ctx);
			}

			End:
			while (true) {
				for (int len = data.length(); index <= len; ++index) {
					current.setCurrent(index);
					for (int k = 0; k < keyCount; ++k) {
						if (!Variant.isEquals(keys[k], exps[k].calculate(ctx))) {
							break End;
						}
					}
				}

				if (newTable == null) {
					newTable = data;
				} else {
					newTable.getMems().addAll(data.getMems());
				}

				data = fuzzyFetch(FETCHCOUNT);
				if (data == null) break;

				index = 1;
				stack.pop();
				current = new Current(data);
				stack.push(current);
			}
		} finally {
			stack.pop();
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(data.getMems());
			}
		}

		return newTable;
	}

	/**
	 * ��ָ�����ʽȡһ������
	 * @param exp ���ʽ
	 * @param ctx ����������
	 * @return Sequence
	 */
	public synchronized Sequence fetchGroup(Expression exp, Context ctx) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		Sequence newTable = null;
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(data);
		stack.push(current);
		current.setCurrent(1);
		int index = 2;

		try {
			Object key = exp.calculate(ctx);
			if (key instanceof Boolean) {
				End:
				while (true) {
					for (int len = data.length(); index <= len; ++index) {
						current.setCurrent(index);
						if (Variant.isTrue(exp.calculate(ctx))) {
							break End;
						}
					}

					if (newTable == null) {
						newTable = data;
					} else {
						newTable.getMems().addAll(data.getMems());
					}

					data = fuzzyFetch(FETCHCOUNT);
					if (data == null) break;

					index = 1;
					stack.pop();
					current = new Current(data);
					stack.push(current);
				}
			} else {
				End:
				while (true) {
					for (int len = data.length(); index <= len; ++index) {
						current.setCurrent(index);
						if (!Variant.isEquals(key, exp.calculate(ctx))) {
							break End;
						}
					}

					if (newTable == null) {
						newTable = data;
					} else {
						newTable.getMems().addAll(data.getMems());
					}

					data = fuzzyFetch(FETCHCOUNT);
					if (data == null) break;

					index = 1;
					stack.pop();
					current = new Current(data);
					stack.push(current);
				}
			}
		} finally {
			stack.pop();
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(data.getMems());
			}
		}
		
		return newTable;
	}

	/**
	 * ��ָ���ֶκ�ȡһ�����ݣ�������̵߳���
	 * @param field �ֶ����
	 * @return Sequence
	 */
	public Sequence fetchGroup(int field) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		IArray mems = data.getMems();
		BaseRecord r = (BaseRecord)mems.get(1);
		Sequence newTable = null;
		Object key = r.getNormalFieldValue(field);
		int index = 2;

		End:
		while (true) {
			for (int len = data.length(); index <= len; ++index) {
				r = (BaseRecord)mems.get(index);;
				if (!Variant.isEquals(key, r.getNormalFieldValue(field))) {
					break End;
				}
			}

			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
			
			data = fuzzyFetch(FETCHCOUNT);
			if (data == null) break;
			
			mems = data.getMems();
			index = 1;
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
		}

		return newTable;
	}

	/**
	 * ��ָ���ֶκ�ȡһ�����ݣ�������̵߳���
	 * @param fields �ֶ��������
	 * @return Sequence
	 */
	public Sequence fetchGroup(int []fields) {
		int keyCount = fields.length;
		if (keyCount == 1) {
			return fetchGroup(fields[0]);
		}
		
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		Sequence newTable = null;
		IArray mems = data.getMems();
		BaseRecord r = (BaseRecord)mems.get(1);
		int index = 2;
		
		Object []keys = new Object[keyCount];
		for (int i = 0; i < keyCount; ++i) {
			keys[i] = r.getNormalFieldValue(fields[i]);
		}

		End:
		while (true) {
			for (int len = data.length(); index <= len; ++index) {
				r = (BaseRecord)mems.get(index);
				for (int i = 0; i < keyCount; ++i) {
					if (!Variant.isEquals(keys[i], r.getNormalFieldValue(fields[i]))) {
						break End;
					}
				}
			}

			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
			
			data = fuzzyFetch(FETCHCOUNT);
			if (data == null) break;
			
			mems = data.getMems();
			index = 1;
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
		}

		return newTable;
	}

	/**
	 * ��ָ���ֶκ�ȡһ�����ݣ���������limit��ȡ����һ��Ҳ�᷵�أ�������̵߳���
	 * @param fields �ֶ��������
	 * @param limit ����¼��
	 * @return Sequence
	 */
	public Sequence fetchGroup(int []fields, int limit) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return null;
		}

		int keyCount = fields.length;
		Sequence newTable = null;
		IArray mems = data.getMems();
		BaseRecord r = (BaseRecord)mems.get(1);
		int index = 2;
		int count = 0;
		
		Object []keys = new Object[keyCount];
		for (int i = 0; i < keyCount; ++i) {
			keys[i] = r.getNormalFieldValue(fields[i]);
		}

		End:
		while (true) {
			for (int len = data.length(); index <= len; ++index) {
				r = (BaseRecord)mems.get(index);
				for (int i = 0; i < keyCount; ++i) {
					if (!Variant.isEquals(keys[i], r.getNormalFieldValue(fields[i]))) {
						break End;
					}
				}
				if (count + index >= limit + 1) {
					break End;
				}
			}

			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
			count = newTable.length();
			
			data = fuzzyFetch(FETCHCOUNT);
			if (data == null) break;
			
			mems = data.getMems();
			index = 1;
		}

		if (data != null && data.length() >= index) {
			cache = data.split(index);
			if (newTable == null) {
				newTable = data;
			} else {
				newTable.getMems().addAll(mems);
			}
		}

		return newTable;
	}
	
	/**
	 * ����һ������
	 * @param exps
	 * @param ctx
	 * @return
	 */
	public synchronized int skipGroup(Expression[] exps, Context ctx) {
		Sequence data = fuzzyFetch(FETCHCOUNT);
		if (data == null) {
			return 0;
		}

		int keyCount = exps.length;
		Object []keys = new Object[keyCount];

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(data);
		stack.push(current);
		current.setCurrent(1);

		int count = 1;
		int index = 2;

		try {
			for (int k = 0; k < keyCount; ++k) {
				keys[k] = exps[k].calculate(ctx);
			}

			End:
			while (true) {
				for (int len = data.length(); index <= len; ++index, ++count) {
					current.setCurrent(index);
					for (int k = 0; k < keyCount; ++k) {
						if (!Variant.isEquals(keys[k], exps[k].calculate(ctx))) {
							break End;
						}
					}
				}

				data = fuzzyFetch(FETCHCOUNT);
				if (data == null) break;

				index = 1;
				stack.pop();
				current = new Current(data);
				stack.push(current);
			}
		} finally {
			stack.pop();
		}

		if (data != null && data.length() > index) {
			cache = data.split(index);
		}

		return count;
	}

	/**
	 * �������м�¼
	 * @return ʵ�������ļ�¼��
	 */
	public long skip() {
		return skip(MAXSKIPSIZE);
	}
	
	/**
	 * ����ָ����¼��
	 * @param n ��¼��
	 * @return long ʵ�������ļ�¼��
	 */
	public synchronized long skip(long n) {
		if (opList == null) {
			if (cache == null) {
				long count = skipOver(n);
				if (count < n) {
					close();
				}
				
				return count;
			} else {
				int len = cache.length();
				if (len == n) {
					cache = null;
					return n;
				} else if (len > n) {
					cache.split(1, (int)n);
					return n;
				} else {
					cache = null;
					long count = n + skipOver(n - len);
					if (count < n) {
						close();
					}
					
					return count;
				}
			}
		} else {
			long total = 0;
			while (n > 0) {
				Sequence seq;
				if (n > FETCHCOUNT) {
					seq = fetch(FETCHCOUNT);
				} else {
					seq = fetch((int)n);
				}
				
				if (seq == null || seq.length() == 0) {
					close();
					break;
				}
				
				total += seq.length();
				n -= seq.length();
			}
			
			return total;
		}
	}

	/**
	 * �ر��α�
	 */
	public void close() {
		cache = null;
		
		if (isFinished) {
			// ������reset
			isFinished = false;
		} else if (opList != null) {
			finish(opList, ctx);
		}
	}
	
	/**
	 * ȡ��¼��������Ҫʵ�ִ˷���
	 * @param n Ҫȡ�ļ�¼��
	 * @return Sequence
	 */
	protected abstract Sequence get(int n);

	/**
	 * ģ��ȡ��¼�����صļ�¼�����Բ��������������ͬ
	 * @param n Ҫȡ�ļ�¼��
	 * @return Sequence
	 */
	protected Sequence fuzzyGet(int n) {
		return get(n);
	}

	/**
	 * ����ָ����¼����������Ҫʵ�ִ˷���
	 * @param n ��¼��
	 * @return long
	 */
	protected abstract long skipOver(long n);
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		return false;
	}
	
	/**
	 * ���ؽ�������ݽṹ
	 * @return DataStruct
	 */
	public DataStruct getDataStruct() {
		return dataStruct;
	}
	
	/**
	 * ���ý�������ݽṹ
	 * @param ds ���ݽṹ
	 */
	public void setDataStruct(DataStruct ds) {
		dataStruct = ds;
	}
	
	// �����α�������ֶΣ���������򷵻�null
	public String[] getSortFields() {
		return null;
	}
	
	/**
	 * ȡ����������
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return IGroupsResult
	 */
	public IGroupsResult getGroupsResult(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, String opt, Context ctx) {
		return IGroupsResult.instance(exps, names, calcExps, calcNames, opt, ctx);
	}
	
	/**
	 * ���α���з������
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return ������
	 */
	public Table groups(Expression[] exps, String[] names, Expression[] calcExps, String[] calcNames, 
			String opt, Context ctx) {
		IGroupsResult groups = IGroupsResult.instance(exps, names, calcExps, calcNames, opt, ctx);
		groups.push(this);
		return groups.getResultTable();
	}
	
	/**
	 * ���α���з������
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @param groupCount ���������
	 * @return ������
	 */
	public Table groups(Expression[] exps, String[] names, Expression[] calcExps, String[] calcNames, 
			String opt, Context ctx, int groupCount) {
		if (groupCount < 1 || exps == null || exps.length == 0) {
			return groups(exps, names, calcExps, calcNames, opt, ctx);
		} else if (opt != null && opt.indexOf('n') != -1) {
			IGroupsResult groups = IGroupsResult.instance(exps, names, calcExps, calcNames, opt, ctx);
			groups.setGroupCount(groupCount);
			groups.push(this);
			return groups.getResultTable();
		} else {
			return CursorUtil.fuzzyGroups(this, exps, names, calcExps, calcNames, opt, ctx, groupCount);
		}
	}

	/**
	 * ���α�������������
	 * @param exps ������ʽ����
	 * @param names	�����ֶ�������
	 * @param calcExps ���ܱ��ʽ	����
	 * @param calcNames	�����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @param capacity	�ڴ��б��������������
	 * @return ICursor �������α�
	 */
	public ICursor groupx(Expression[] exps, String []names, 
			Expression[] calcExps, String []calcNames, String opt, Context ctx, int capacity) {
		if (opt != null && opt.indexOf('n') != -1) {
			return CursorUtil.groupx_n(this, exps, names, calcExps, calcNames, ctx, capacity);
		}
		
		GroupxResult groupx = new GroupxResult(exps, names, calcExps, calcNames, opt, ctx, capacity);
		while (true) {
			Sequence src = fetch(INITSIZE);
			if (src == null || src.length() == 0) break;
			
			groupx.push(src, ctx);
		}
		
		return groupx.getResultCursor();
	}

	/**
	 * ��ÿ�����ʽ���й�ϣȥ�أ�����count����ֵͬ
	 * @param exps ���ʽ����
	 * @param count ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Sequence �������е����У����countΪ1��������
	 */
	public Sequence id(Expression []exps, int count, String opt, Context ctx) {
		IDResult id = new IDResult(exps, count, opt, ctx);
		id.push(this);
		return id.getResultSequence();
	}

	/**
	 * ���������α�
	 * @param exp �������ʽ
	 * @param initVal ��ʼֵ
	 * @param c �������ʽ��Ϊtrue��ֹͣ
	 * @param ctx ����������
	 * @return �������
	 */
	public Object iterator(Expression exp, Object initVal, Expression c, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		Param param = ctx.getIterateParam();
		Object oldVal = param.getValue();
		param.setValue(initVal);
		
		try {
			while (true) {
				// ���α���ȡ��һ�����ݡ�
				Sequence src = fuzzyFetch(FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				Current current = new Current(src);
				stack.push(current);
				try {
					if (c == null) {
						for (int i = 1, size = src.length(); i <= size; ++i) {
							current.setCurrent(i);
							initVal = exp.calculate(ctx);
							param.setValue(initVal);
						}
					} else {
						for (int i = 1, size = src.length(); i <= size; ++i) {
							current.setCurrent(i);
							Object obj = c.calculate(ctx);
							
							// �������Ϊ���򷵻�
							if (obj instanceof Boolean && ((Boolean)obj).booleanValue()) {
								return initVal;
							}
							
							initVal = exp.calculate(ctx);
							param.setValue(initVal);
						}
					}
				} finally {
					stack.pop();
				}
			}
		} finally {
			param.setValue(oldVal);
		}
		
		return initVal;
	}

	/**
	 * ���α�����������
	 * @param cursor �α�
	 * @param exps �����ֶα��ʽ����
	 * @param ctx ����������
	 * @param capacity �ڴ����ܹ�����ļ�¼�������û���������Զ�����һ��
	 * @param opt ѡ�� 0��null�����
	 * @return �ź�����α�
	 */
	public ICursor sortx(Expression[] exps, Context ctx, int capacity, String opt) {
		return CursorUtil.sortx(this, exps, ctx, capacity, opt);
	}

	/**
	 * ������������ֶ�ֵ��ͬ�ļ�¼��ֵ��ͬ��ͬ��
	 * ��ֵ��ͬ�ļ�¼���浽һ����ʱ�ļ���Ȼ��ÿ����ʱ�ļ���������
	 * @param exps ������ʽ
	 * @param gexp ����ʽ
	 * @param ctx ����������
	 * @param opt ѡ��
	 * @return �ź�����α�
	 */
	public ICursor sortx(Expression[] exps, Expression gexp, Context ctx, String opt) {
		return CursorUtil.sortx(this, exps, gexp, ctx, opt);
	}

	/**
	 * ���α���л���
	 * @param calcExps ���ܱ��ʽ����
	 * @param ctx ����������
	 * @return ���ֻ��һ�����ܱ��ʽ���ػ��ܽ�������򷵻ػ��ܽ�����ɵ�����
	 */
	public Object total(Expression[] calcExps, Context ctx) {
		//TotalResult total = new TotalResult(calcExps, ctx);
		//total.push(this);
		//return total.result();
		Table table = groups(null, null, calcExps, null, null, ctx);
		
		if (table == null || table.length() == 0) {
			return null;
		} else {
			BaseRecord r = table.getRecord(1);
			int count = calcExps.length;
			if (count == 1) {
				return r.getNormalFieldValue(0);
			} else {
				Sequence seq = new Sequence(count);
				for (int i = 0; i < count; ++i) {
					seq.add(r.getNormalFieldValue(i));
				}
				
				return seq;
			}
		}
	}
	
	/**
	 * ���α�������鲢����
	 * @param function ��Ӧ�ĺ���
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param cursors ά���α�����
	 * @param codeExps ά������ֶα��ʽ����
	 * @param newExps
	 * @param newNames
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable mergeJoinx(Function function, Expression[][] exps, 
			ICursor []cursors, Expression[][] codeExps, 
			Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		//TODO �Ժ�Ҫ�޸�ΪOperable
		//return CSJoinxCursor3.MergeJoinx(this, exps, cursors, codeExps, newExps, newNames, null, ctx, FETCHCOUNT, opt);
		return new MergeJoinxCursor(this, exps, cursors, codeExps, newExps, newNames, opt, ctx);
	}
	
	/**
	 * �õ�����news������α� T.news(this)
	 * @param table ����
	 * @param exps ȡ�����ʽ
	 * @param fields ȡ���ֶ�����
	 * @param csNames ����K��ָ��A/cs�������ӵ��ֶ�
	 * @param type	�������ͣ�0:derive; 1:new; 2:news; 0x1X ��ʾ����;
	 * @param option ѡ��	
	 * @param filter ��table�Ĺ�������
	 * @param fkNames ��table��Switch��������
	 * @param codes
	 * @param ctx
	 */
	public ICursor attachNews(ColPhyTable table, String[] csNames, 
			Expression filter, Expression []exps, String[] names, String []fkNames, 
			Sequence []codes, String[] opts, String option, int type, Context ctx) {
		return new JoinCursor(table, exps, names, this, csNames, type, option, filter, fkNames, codes, opts, ctx); 
	}
	
	/**
	 * ȡ�ֶ��α����ʼֵ������зֶ��ֶ��򷵻طֶ��ֶε�ֵ��û���򷵻�ά�ֶε�ֵ
	 * @return �ֶ��α�������¼�ķֶ��ֶε�ֵ�������ǰ����Ϊ0�򷵻�null
	 */
	public Object[] getSegmentStartValues(String option) {
		throw new RQException();
	}
	
	/**
	 * ����һ���뵱ǰ�α���ƥ��Ĺܵ�
	 * @param ctx ����������
	 * @param doPush �Ƿ���α�����push����
	 * @return Channel
	 */
	public Channel newChannel(Context ctx, boolean doPush) {
		if (doPush) {
			return new Channel(ctx, this);
		} else {
			return new Channel(ctx);
		}
	}
	
	/**
	 * �α��Ƿ��������
	 * @return
	 */
	public boolean canSkipBlock() {
		return false;
	}
	
	/**
	 * �õ��α������ڿ�ķ�Χ
	 * @param key
	 * @return
	 */
	public IArray[] getSkipBlockInfo(String key) {
		return null;
	}
	
	/**
	 * ���α�����Ϊ����key�ֶ����� ��pjoinʱʹ�ã�
	 * ���ú��α�ᰴ��values���ֵ�������顣
	 * @param key ά�ֶ���
	 * @param values [minValue, maxValue] 
	 */
	public void setSkipBlockInfo(String key, IArray[] values) {
	}

	/**
	 * ����������Ϣ
	 * @param srcKeyExps ���ӱ��ʽ����
	 * @param cursors �����α�����
	 * @param options ����ѡ��
	 * @param keyExps ���ӱ��ʽ����
	 * @param newExps
	 * @param opt
	 */
	public void setSkipBlock(Expression []srcKeyExps, ICursor []cursors, String []options, Expression [][]keyExps, Expression [][]newExps, String option) {
		int tableCount = cursors.length;
		String key = srcKeyExps[0].getFieldName();
		if (option == null || option.indexOf('f') == -1) {
			if ((option == null || option.indexOf('r') == -1) && canSkipBlock()) {
				// �����α���������α��������
				IArray []values = null;
				boolean isGet = false;
				
				for (int t = 0; t < tableCount; ++t) {
					if (cursors[t] == null) {
						continue;
					}
					
					String opt = options[t];
					if (opt == null || !opt.equals("null") || newExps[t] != null) {
						if (!isGet) {
							isGet = true;
							values = getSkipBlockInfo(key);
							if (values == null) {
								break;
							}
						}
						
						cursors[t].setSkipBlockInfo(keyExps[t][0].getFieldName(), values);
					}
				}
			} else {
				// �����α���������α��������
				IArray []values = null;
				for (int t = 0; t < tableCount; ++t) {
					if (cursors[t] == null || !cursors[t].canSkipBlock()) {
						continue;
					}
					
					String opt = options[t];
					if (opt == null || !opt.equals("null")) {
						values = cursors[t].getSkipBlockInfo(keyExps[t][0].getFieldName());
						if (values != null) {
							setSkipBlockInfo(key, values);
							break;
						}
					}
				}
			}
		}
	}
}
