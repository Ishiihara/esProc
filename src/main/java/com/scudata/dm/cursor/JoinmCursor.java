package com.scudata.dm.cursor;

import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.util.Variant;

/**
 * �����α�������鲢���ӣ��α갴�����ֶ��������ڶ�Զ��������
 * joinx(cs1:f1,x1;cs2:f2,x2)
 * @author RunQian
 *
 */
public class JoinmCursor extends ICursor {
	private ICursor cursor1; // ��һ���α�
	private ICursor cursor2; // �ڶ����α�
	private Expression exp1; // ��һ���α�Ĺ������ʽ
	private Expression exp2; // �ڶ����α�Ĺ������ʽ
	private DataStruct ds; // ��������ݽṹ
	private boolean isEnd = false; // �Ƿ�ȡ������

	private Sequence data1; // ��һ���α�Ļ�������
	private Sequence data2; // �ڶ����α�Ļ�������
	private Sequence value1; // ��һ���α껺�����ݵĹ����ֶ�ֵ
	private Sequence value2; // �ڶ����α껺�����ݵĹ����ֶ�ֵ
	
	private int cur1 = -1; // ��һ���α굱ǰ������������
	private int cur2 = -1; // �ڶ����α굱ǰ������������
	private int count1; // ��һ���α굱ǰ�����ֶ�ֵ��ͬ������
	private int count2; // �ڶ����α굱ǰ�����ֶ�ֵ��ͬ������
	
	// �α��Ǵ��Ĳ��ҹ������ʽ���ֶα��ʽʱʹ�ã���ʱֱ�����ֶ�����ȡ����
	private int col1 = -1;
	private int col2 = -1;
	private Sequence cache; // ���������
	
	/**
	 * ���������α������������
	 * @param cursor1 ��һ���α�
	 * @param exp1 ��һ���α�Ĺ������ʽ
	 * @param cursor2 �ڶ����α�
	 * @param exp2 �ڶ����α�Ĺ������ʽ
	 * @param names ������ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public JoinmCursor(ICursor cursor1, Expression exp1, ICursor cursor2, Expression exp2, 
			String []names, String opt, Context ctx) {
		this.cursor1 = cursor1;
		this.cursor2 = cursor2;
		this.exp1 = exp1;
		this.exp2 = exp2;
		this.ctx = ctx;

		if (names == null) {
			names = new String[2];
		}

		ds = new DataStruct(names);
		setDataStruct(ds);
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	public void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			cursor1.resetContext(ctx);
			cursor2.resetContext(ctx);
			exp1 = Operation.dupExpression(exp1, ctx);
			exp2 = Operation.dupExpression(exp2, ctx);
			super.resetContext(ctx);
		}
	}
	
	private void init() {
		if (cur1 != -1) {
			return;
		}
		
		data1 = cursor1.fuzzyFetch(FETCHCOUNT);
		if (data1 != null && data1.length() > 0) {
			cur1 = 1;
		} else {
			cur1 = 0;
			cursor2.close();
			isEnd = true;
			return;
		}
		
		data2 = cursor2.fuzzyFetch(FETCHCOUNT);
		if (data2 != null && data2.length() > 0) {
			cur2 = 1;
		} else {
			cur2 = 0;
			isEnd = true;
			return;
		}

		// ����α��Ǵ������жϹ������ʽ�Ƿ����ֶα��ʽ
		if (cur1 > 0 && cur2 > 0) {
			DataStruct ds1 = cursor1.getDataStruct();
			DataStruct ds2 = cursor2.getDataStruct();
			if (ds1 != null && ds2 != null) {
				// �α긽���˲������ܸı������ݽṹ
				Object r1 = data1.getMem(1);
				Object r2 = data2.getMem(1);
				if (r1 instanceof BaseRecord && ds1.isCompatible(((BaseRecord)r1).dataStruct()) &&
					r2 instanceof BaseRecord && ds2.isCompatible(((BaseRecord)r2).dataStruct())) {
					col1 = exp1.getFieldIndex(ds1);
					if (col1 != -1) {
						col2 = exp2.getFieldIndex(ds2);
						if (col2 == -1) {
							col1 = -1;
						}
					}
				}
			}
		} else {
			return;
		}
		
		if (col1 == -1) {
			value1 = data1.calc(exp1, ctx);
			value2 = data2.calc(exp2, ctx);
		}
		
		calcGroup1();
		calcGroup2();
	}
	
	private void calcGroup1() {
		if (col1 == -1) {
			Sequence value = value1;
			int len = value.length();
			if (cur1 > len) {
				isEnd = true;
				return;
			}
			
			Object val = value.getMem(cur1);
			int sameCount = 1;
			
			Next:
			while (true) {
				// ȡ�����ֶ�ֵ��ͬ�ļ�¼��
				for (int i = cur1 + sameCount; i < len; ++i) {
					if (Variant.isEquals(value.getMem(i), val)) {
						sameCount++;
					} else {
						break Next;
					}
				}
				
				// ����β����ͬ�������������
				Sequence seq = cursor1.fuzzyFetch(FETCHCOUNT);
				if (seq != null && seq.length() > 0) {
					if (cur1 > 1) {
						int count = value.length() - cur1 + 1;
						Sequence tmp = new Sequence(count + seq.length());
						tmp.append(data1, cur1, count);
						tmp.append(seq);
						data1 = tmp;
						
						seq = seq.calc(exp1, ctx);
						tmp = new Sequence(count + seq.length());
						tmp.append(value, cur1, count);
						tmp.append(seq);
						value1 = value = tmp;
						cur1 = 1;
					} else {
						data1.append(seq);
						seq = seq.calc(exp1, ctx);
						value.append(seq);
					}
					
					len = value.length();
				} else {
					break;
				}
			}

			count1 = sameCount;
		} else {
			Sequence data = data1;
			int len = data.length();
			if (cur1 > len) {
				isEnd = true;
				return;
			}
			
			int col = col1;
			BaseRecord r = (BaseRecord)data.getMem(cur1);
			Object val = r.getNormalFieldValue(col);
			int sameCount = 1;
			
			Next:
			while (true) {
				// ȡ�����ֶ�ֵ��ͬ�ļ�¼��
				for (int i = cur1 + sameCount; i < len; ++i) {
					r = (BaseRecord)data.getMem(i);
					if (Variant.isEquals(r.getNormalFieldValue(col), val)) {
						sameCount++;
					} else {
						break Next;
					}
				}
				
				// ����β����ͬ�������������
				Sequence seq = cursor1.fuzzyFetch(FETCHCOUNT);
				if (seq != null && seq.length() > 0) {
					if (cur1 > 1) {
						int count = data.length() - cur1 + 1;
						Sequence tmp = new Sequence(count + seq.length());
						tmp.append(data, cur1, count);
						tmp.append(seq);
						data = data1 = tmp;
						cur1 = 1;
					} else {
						data.append(seq);
					}
					
					len = data.length();
				} else {
					break;
				}
			}

			count1 = sameCount;
		}
	}
	
	private void calcGroup2() {
		if (col2 == -1) {
			Sequence value = value2;
			int len = value.length();
			if (cur2 > len) {
				isEnd = true;
				return;
			}
			
			Object val = value.getMem(cur2);
			int sameCount = 1;
			
			Next:
			while (true) {
				// ȡ�����ֶ�ֵ��ͬ�ļ�¼��
				for (int i = cur2 + sameCount; i < len; ++i) {
					if (Variant.isEquals(value.getMem(i), val)) {
						sameCount++;
					} else {
						break Next;
					}
				}
				
				// ����β����ͬ�������������
				Sequence seq = cursor2.fuzzyFetch(FETCHCOUNT);
				if (seq != null && seq.length() > 0) {
					if (cur2 > 1) {
						int count = value.length() - cur2 + 1;
						Sequence tmp = new Sequence(count + seq.length());
						tmp.append(data2, cur2, count);
						tmp.append(seq);
						data2 = tmp;
						
						seq = seq.calc(exp2, ctx);
						tmp = new Sequence(count + seq.length());
						tmp.append(value, cur2, count);
						tmp.append(seq);
						value2 = value = tmp;
						cur2 = 1;
					} else {
						data2.append(seq);
						seq = seq.calc(exp2, ctx);
						value.append(seq);
					}
					
					len = value.length();
				} else {
					break;
				}
			}
			
			count2 = sameCount;
		} else {
			Sequence data = data2;
			int len = data.length();
			if (cur2 > len) {
				isEnd = true;
				return;
			}
			
			int col = col2;
			BaseRecord r = (BaseRecord)data.getMem(cur2);
			Object val = r.getNormalFieldValue(col);
			int sameCount = 1;
			
			Next:
			while (true) {
				// ȡ�����ֶ�ֵ��ͬ�ļ�¼��
				for (int i = cur2 + sameCount; i < len; ++i) {
					r = (BaseRecord)data.getMem(i);
					if (Variant.isEquals(r.getNormalFieldValue(col), val)) {
						sameCount++;
					} else {
						break Next;
					}
				}
				
				// ����β����ͬ�������������
				Sequence seq = cursor2.fuzzyFetch(FETCHCOUNT);
				if (seq != null && seq.length() > 0) {
					if (cur2 > 1) {
						int count = data.length() - cur2 + 1;
						Sequence tmp = new Sequence(count + seq.length());
						tmp.append(data, cur2, count);
						tmp.append(seq);
						data = data2 = tmp;
						cur2 = 1;
					} else {
						data.append(seq);
					}

					len = data.length();
				} else {
					break;
				}
			}
			
			count2 = sameCount;
		}
	}
	
	protected Sequence fuzzyGet(int n) {
		if (cache != null) {
			Sequence result = cache;
			cache = null;
			return result;
		} else if (isEnd) {
			return null;
		}

		init();
		
		Table newTable;
		if (n > INITSIZE) {
			newTable = new Table(ds, INITSIZE);
		} else {
			newTable = new Table(ds, n);
		}
		
		if (col2 != -1) {
			int col1 = this.col1;
			int col2 = this.col2;
			for (; n > 0 && !isEnd;) {
				Object val1 = ((BaseRecord)data1.getMem(cur1)).getNormalFieldValue(col1);
				Object val2 = ((BaseRecord)data2.getMem(cur2)).getNormalFieldValue(col2);
				
				int cmp = Variant.compare(val1, val2, true);
				if (cmp == 0) {
					Sequence data1 = this.data1;
					Sequence data2 = this.data2;
					int count1 = this.count1;
					int count2 = this.count2;
					int cur1 = this.cur1;
					
					for (int i = 0; i < count1; ++i, ++cur1) {
						Object r1 = data1.getMem(cur1);
						int cur2 = this.cur2;
						for (int j = 0; j < count2; ++j, ++cur2) {
							BaseRecord r = newTable.newLast();
							r.setNormalFieldValue(0, r1);
							r.setNormalFieldValue(1, data2.getMem(cur2));
						}
					}
					
					n -= count1 * count2;
					this.cur1 += count1;
					this.cur2 += count2;
					calcGroup1();
					calcGroup2();
				} else if (cmp > 0) {
					this.cur2 += count2;
					calcGroup2();
				} else {
					this.cur1 += count1;
					calcGroup1();
				}
			}
		} else {
			for (; n > 0 && !isEnd;) {
				int cmp = Variant.compare(value1.getMem(cur1), value2.getMem(cur2), true);
				if (cmp == 0) {
					Sequence data1 = this.data1;
					Sequence data2 = this.data2;
					int count1 = this.count1;
					int count2 = this.count2;
					int cur1 = this.cur1;
					
					for (int i = 0; i < count1; ++i, ++cur1) {
						Object r1 = data1.getMem(cur1);
						int cur2 = this.cur2;
						for (int j = 0; j < count2; ++j, ++cur2) {
							BaseRecord r = newTable.newLast();
							r.setNormalFieldValue(0, r1);
							r.setNormalFieldValue(1, data2.getMem(cur2));
						}
					}
					
					n -= count1 * count2;
					this.cur1 += count1;
					this.cur2 += count2;
					calcGroup1();
					calcGroup2();
				} else if (cmp > 0) {
					this.cur2 += count2;
					calcGroup2();
				} else {
					this.cur1 += count1;
					calcGroup1();
				}
			}
		}
				
		if (newTable.length() > 0) {
			return newTable;
		} else {
			return null;
		}
	}
	
	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		Sequence result = fuzzyGet(n);
		if (result == null || result.length() <= n) {
			return result;
		} else {
			cache = result.split(n + 1);
			return result;
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		Sequence data;
		long rest = n;
		long count = 0;
		
		while (rest != 0) {
			if (rest > FETCHCOUNT) {
				data = get(FETCHCOUNT);
			} else {
				data = get((int)rest);
			}
			
			if (data == null) {
				break;
			} else {
				count += data.length();
			}
			
			rest -= data.length();
		}
		
		return count;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (cursor1 != null) {
			cursor1.close();
			cursor2.close();
			
			data1 = null;
			data2 = null;

			value1 = null;
			value2 = null;
			isEnd = true;
		}
	}

	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		if (!cursor1.reset() || !cursor2.reset()) {
			return false;
		} else {
			isEnd = false;
			cur1 = -1;
			cur2 = -1;
			return true;
		}
	}
}
