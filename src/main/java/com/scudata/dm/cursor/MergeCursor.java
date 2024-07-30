package com.scudata.dm.cursor;

import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.dm.op.Operable;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.util.LoserTree;
import com.scudata.util.LoserTreeNode_CS;
import com.scudata.util.LoserTreeNode_CS1;
import com.scudata.util.Variant;

/**
 * ���ṹ�Ķ���α�������鲢�����γɵ��α�
 * CS.mergex(xi,��)
 * @author RunQian
 *
 */
public class MergeCursor extends ICursor {
	private ICursor []cursors; // �α��������Ѿ����鲢�ֶ���������
	private int []fields; // �鲢�ֶ�
	private boolean isNullMin = true; // null�Ƿ���Сֵ
	
	private LoserTree loserTree; // ÿһ·�α���Ϊ���Ľڵ㰴�鲢�ֶ�ֵ���ɰ�����
	
	// һ�����������α�������Ĵ���
	private Sequence []tables;	// ���ݻ����������ڻ�������α�
	private int []seqs;	// ��ǰ����������ڸ��Ի�����������
	private int groupFieldCount; // �����ֶ���
	private Sequence resultCache; // ������
	private boolean isEnd = false; // �Ƿ�ȡ�����
	private boolean isGroupOne; // ÿ���Ƿ�ֻȡһ��
	
	/**
	 * ������Ч�鲢�α�
	 * @param cursors �α�����
	 * @param fields �����ֶ�����
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public MergeCursor(ICursor []cursors, int []fields, String opt, Context ctx) {
		this.cursors = cursors;
		this.fields = fields;
		this.ctx = ctx;
		dataStruct = cursors[0].getDataStruct();
		
		if (opt != null && opt.indexOf('0') !=-1) {
			isNullMin = false;
		}
	}
	
	/**
	 * ȡ�ֶ��α����ʼֵ������зֶ��ֶ��򷵻طֶ��ֶε�ֵ��û���򷵻�ά�ֶε�ֵ
	 * @return �ֶ��α�������¼�ķֶ��ֶε�ֵ�������ǰ����Ϊ0�򷵻�null
	 */
	public Object[] getSegmentStartValues(String option) {
		return cursors[0].getSegmentStartValues(option);
	}
	
	/**
	 * ���������������
	 * @param function ��Ӧ�ĺ���
	 * @param exps ������ʽ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable group(Function function, Expression []exps, String opt, Context ctx) {
		// ����Ѿ������������򲻽����Ż�
		if (opList != null && opList.size() > 0) {
			return super.group(function, exps, opt, ctx);
		}
		
		// ��������ֶ��ǹ鲢�ֶε�ǰ�벿����ѷ���鲢ͬʱ����
		int groupFieldCount = exps.length;
		if (groupFieldCount <= fields.length) {
			DataStruct ds = cursors[0].dataStruct;
			if (ds == null) {
				return super.group(function, exps, opt, ctx);
			}
			
			for (int i = 0; i < groupFieldCount; ++i) {
				int findex = exps[i].getFieldIndex(ds);
				if (findex != fields[i]) {
					return super.group(function, exps, opt, ctx);
				}
			}
			
			if (fields.length > groupFieldCount) {
				int []tmp = new int[groupFieldCount];
				System.arraycopy(fields, 0, tmp, 0, groupFieldCount);
				fields = tmp;
			}
			
			//groupOption = opt;
			this.groupFieldCount = groupFieldCount;
			isGroupOne = opt != null && opt.indexOf('1') != -1;
			return this;
		} else {
			return super.group(function, exps, opt, ctx);
		}
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	public void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			for (ICursor cursor : cursors) {
				cursor.resetContext(ctx);
			}

			super.resetContext(ctx);
		}
	}
	/**
	 * �������α�Ļ�������������������������ֱ�ӷ��ء�
	 */
	private void init() {
		if (groupFieldCount < 1) {
			if (loserTree != null) {
				return;
			}
			
			int count = cursors.length;
			if (fields.length == 1) {
				LoserTreeNode_CS1 []nodes = new LoserTreeNode_CS1[count];
				for (int i = 0; i < count; ++i) {
					nodes[i] = new LoserTreeNode_CS1(cursors[i], fields[0], isNullMin);
				}
				
				loserTree = new LoserTree(nodes);
			} else {
				LoserTreeNode_CS []nodes = new LoserTreeNode_CS[count];
				for (int i = 0; i < count; ++i) {
					nodes[i] = new LoserTreeNode_CS(cursors[i], fields, isNullMin);
				}
				
				loserTree = new LoserTree(nodes);
			}
		} else {
			if (tables != null) {
				return;
			}
	
			int tcount = cursors.length;
			tables = new Sequence[tcount];
			seqs = new int[tcount];
			isEnd = true;
			
			for (int i = 0; i < tcount; ++i) {
				Sequence table = cursors[i].fuzzyFetch(FETCHCOUNT_M);
				if (table != null && table.length() > 0) {
					tables[i] = table;
					seqs[i] = 1;
					isEnd = false;
				}
			}
		}
	}
	
	private void getGroupData(int path, Sequence result) {
		Sequence table = tables[path];
		int len = table.length();
		int seq = seqs[path];
		
		int []mergeFields = this.fields;
		int mergeFieldCount = mergeFields.length;
		
		if (mergeFieldCount == 1) {
			int field = mergeFields[0];
			BaseRecord r = (BaseRecord)table.getMem(seq);
			result.add(r);
			Object value = r.getNormalFieldValue(field);
			
			for (int i = seq + 1; i <= len; ++i) {
				r = (BaseRecord)table.getMem(i);
				if (Variant.isEquals(value, r.getNormalFieldValue(field))) {
					result.add(r);
				} else {
					seqs[path] = i;
					return;
				}
			}
			
			table = cursors[path].fuzzyFetch(FETCHCOUNT_M);
			if (table != null && table.length() > 0) {
				tables[path] = table;
				seqs[path] = 1;
				r = (BaseRecord)table.getMem(1);
				
				if (Variant.isEquals(value, r.getNormalFieldValue(field))) {
					getGroupData(path, result);
				}
			} else {
				tables[path] = null;
				seqs[path] = 0;
			}
		} else {
			BaseRecord r1 = (BaseRecord)table.getMem(seq);
			result.add(r1);
			
			for (int i = seq + 1; i <= len; ++i) {
				BaseRecord r = (BaseRecord)table.getMem(i);
				if (r1.isEquals(r, mergeFields)) {
					result.add(r);
				} else {
					seqs[path] = i;
					return;
				}
			}
						
			table = cursors[path].fuzzyFetch(FETCHCOUNT_M);
			if (table != null && table.length() > 0) {
				tables[path] = table;
				seqs[path] = 1;
				BaseRecord r = (BaseRecord)table.getMem(1);
				
				if (r1.isEquals(r, mergeFields)) {
					getGroupData(path, result);
				}
			} else {
				tables[path] = null;
				seqs[path] = 0;
			}
		}
	}
	
	// �ݹ��ȡ����·�ķ���ֵ��ͬ���飬������ֵ��С����
	private void fetchGroups(int path, Sequence group) {
		int nextPath = path + 1;
		if (group == null) {
			group = new Sequence();
			getGroupData(path, group);
			
			if (nextPath < seqs.length) {
				fetchGroups(nextPath, group);
			}
			
			if (isGroupOne) {
				resultCache.add(group.getMem(1));
			} else {
				resultCache.add(group);
			}
			
			return;
		}
		
		while (seqs[path] > 0) {
			BaseRecord r1 = (BaseRecord)group.getMem(1);
			BaseRecord r2 = (BaseRecord)tables[path].getMem(seqs[path]);
			
			// �ȽϷ����ֶ�ֵ�Ƿ����
			int cmp = r2.compare(r1, fields);
			if (cmp == 0) {
				getGroupData(path, group);
				break;
			} else if (cmp > 0) {
				break;
			} else {
				Sequence newGroup = new Sequence();
				getGroupData(path, newGroup);
				
				if (nextPath < seqs.length) {
					fetchGroups(nextPath, newGroup);
				}
				
				if (isGroupOne) {
					resultCache.add(newGroup.getMem(1));
				} else {
					resultCache.add(newGroup);
				}
			}
		}
		
		if (nextPath < seqs.length) {
			fetchGroups(nextPath, group);
		}
	}
	
	private void fetchGroupsToCache(int n) {
		int pathCount = seqs.length;
		if (resultCache == null) {
			int size;
			if (n > FETCHCOUNT && n < MAXSIZE) {
				size = n;
			} else {
				size = FETCHCOUNT;
			}
			
			resultCache = new Sequence(size);
		}
		
		Next:
		while (resultCache.length() < n) {
			for (int i = 0; i < pathCount; ++i) {
				if (seqs[i] > 0) {
					fetchGroups(i, null);
					continue Next;
				}
			}
			
			isEnd = true;
			break;
		}
	}
	
	/**
	 * ģ��ȡ��¼�����صļ�¼�����Բ��������������ͬ
	 * @param n Ҫȡ�ļ�¼��
	 * @return Sequence
	 */
	protected Sequence fuzzyGet(int n) {
		if (n < 1) {
			return null;
		} else if (isEnd) {
			Sequence result = resultCache;
			resultCache = null;
			return result;
		}

		init();
		
		if (groupFieldCount > 0) {
			fetchGroupsToCache(n);
			if (resultCache == null || resultCache.length() == 0) {
				return null;
			} else {
				Sequence result = resultCache;
				resultCache = null;
				return result;
			}
		} else {
			LoserTree loserTree = this.loserTree;
			Sequence table;
			if (n > INITSIZE) {
				table = new Sequence(INITSIZE);
			} else {
				table = new Sequence(n);
			}

			// ѭ��ȡ������仺������ѭ�������жԸ�·�α��ȡ�����������鲢��
			for (int i = 0; i < n && loserTree.hasNext(); ++i) {
				table.add(loserTree.pop());
			}
			
			if (table.length() < n) {
				isEnd = true;
			}

			if (table.length() > 0) {
				return table;
			} else {
				return null;
			}
		}
	}
	
	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		Sequence result = fuzzyGet(n);
		
		if (result == null) {
			return null;
		} else if (result.length() > n) {
			resultCache = result.split(n + 1);
		}

		return result;
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (isEnd) {
			if (resultCache == null) {
				return 0;
			}

			int count = resultCache.length();
			if (count <= n) {
				resultCache = null;
				return count;
			} else {
				resultCache = resultCache.split((int)(n - count) + 1);
				return n;
			}
		}
		
		init();
		
		if (groupFieldCount > 0) {
			long count = 0;
			while (count < n) {
				Sequence seq = fuzzyGet(FETCHCOUNT);
				if (seq == null) {
					break;
				}
				
				int len = seq.length();
				if (count + len > n) {
					resultCache = seq.split((int)(n - count) + 1);
					return n;
				}
			}
			
			return count;
		} else {
			LoserTree loserTree = this.loserTree;
			if (loserTree == null || n < 1) return 0;
			
			long i = 0;
			for (; i < n && loserTree.hasNext(); ++i) {
				loserTree.pop();
			}
	
			if (i < n) {
				isEnd = true;
			}
			
			return i;
		}
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (cursors != null) {
			for (int i = 0, count = cursors.length; i < count; ++i) {
				cursors[i].close();
			}

			loserTree = null;
			tables = null;
			seqs = null;
			resultCache = null;
			isEnd = true;
		}
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		ICursor []cursors = this.cursors;
		int count = cursors.length;
		for (int i = 0; i < count; ++i) {
			if (!cursors[i].reset()) {
				return false;
			}
		}
		
		isEnd = false;
		return true;
	}
	
	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	public String[] getSortFields() {
		return cursors[0].getSortFields();
	}
	
	public ICursor[] getCursors() {
		return cursors;
	}
}
