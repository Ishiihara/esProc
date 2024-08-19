package com.scudata.dm.cursor;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.util.Variant;

/**
 * ���α�������鲢���ӣ��α갴�����ֶ�����
 * joinx(csi:Fi,xj,..;��)
 * @author RunQian
 *
 */
public class JoinxCursor extends ICursor {
	private ICursor []cursors; // �α�����
	private Expression[][] exps; // �������ʽ����
	private DataStruct ds; // ��������ݽṹ
	private int type = 0; // 0:JOIN, 1:LEFTJOIN, 2:FULLJOIN
	private boolean isEnd = false; // �Ƿ�ȡ������

	private Sequence []tables; // ÿ���α�ȡ�������ݻ���
	private Object [][]values; // �����ֶ�ֵ����
	private int []seqs; // ÿ���α굱ǰ������������
	private int []ranks; // ��ǰԪ�ص�������0��1��-1
	private Sequence []nextTables; // �����������ʱȡ���ĺ�������
	private Object [][]nextValues; // ���������һ����¼��Ӧ�Ĺ����ֶε�ֵ
	
	private Context []ctxs; // ÿ�������exps���Լ��������ģ�ÿ����ȡ�����ݺ���ѹջ
	private Current []currents; // ���еĵ�ǰ�����������ѹջ

	/**
	 * ��������鲢�����α�
	 * @param cursors �α����飬�α갴�����ֶ�����
	 * @param exps �������ʽ����
	 * @param names ������ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public JoinxCursor(ICursor []cursors, Expression[][] exps, String []names, String opt, Context ctx) {
		this.cursors = cursors;
		this.exps = exps;
		this.ctx = ctx;

		if (names == null) {
			names = new String[cursors.length];
		}

		ds = new DataStruct(names);
		setDataStruct(ds);
		
		if (opt != null) {
			if (opt.indexOf('1') != -1) {
				type = 1;
			} else if (opt.indexOf('f') != -1) {
				type = 2;
			}
		}
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	public void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			for (ICursor cursor : cursors) {
				cursor.resetContext(ctx);
			}

			exps = Operation.dupExpressions(exps, ctx);
			super.resetContext(ctx);
		}
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (isEnd || n < 1) {
			return null;
		}
		
		getData();

		Sequence []tables = this.tables;
		int []seqs = this.seqs;
		int []ranks = this.ranks;
		int tcount = tables.length;

		Table newTable;
		if (n > INITSIZE) {
			newTable = new Table(ds, INITSIZE);
		} else {
			newTable = new Table(ds, n);
		}

		Object []records = new Object[tcount];
		if (type == 0) { // join
			Next:
			for (; n != 0;) {
				for (int i = 0; i < tcount; ++i) {
					if (ranks[i] == 0) {
						records[i] = tables[i].getMem(seqs[i]);
					} else if (ranks[i] == -1) {
						break Next;
					} else {
						popAll();
						continue Next;
					}
				}

				--n;
				popRepeated();
				newTable.newLast(records);
			}
		} else if (type == 1) { // left join
			for (; n != 0;) {
				if (ranks[0] == 0) {
					records[0] = tables[0].getMem(seqs[0]);
				} else if (ranks[0] == -1) {
					break;
				} else {
					popAll();
					continue;
				}

				for (int i = 1; i < tcount; ++i) {
					if (ranks[i] == 0) {
						records[i] = tables[i].getMem(seqs[i]);
					} else {
						records[i] = null;
					}
				}

				--n;
				popRepeated();
				newTable.newLast(records);
			}
		} else { // full join
			for (; n != 0;) {
				boolean sign = false;
				for (int i = 0; i < tcount; ++i) {
					if (ranks[i] == 0) {
						sign = true;
						records[i] = tables[i].getMem(seqs[i]);
					} else {
						 records[i] = null;
					}
				}

				if (sign) {
					--n;
					popRepeated();
					newTable.newLast(records);
				} else {
					break;
				}
			}
		}

		if (newTable.length() > 0) {
			return newTable;
		} else {
			return null;
		}
	}

	// �ѵ�ǰ����������һ���α��Ԫ������
	private void popAll() {
		Sequence []tables = this.tables;
		Object [][]values = this.values;
		int []seqs = this.seqs;
		int []ranks = this.ranks;
		int count = tables.length;

		for (int i = 0; i < count; ++i) {
			Object []curValues = values[i];
			if (ranks[i] == 0) {
				int next = seqs[i] + 1;
				if (next > tables[i].length()) {
					tables[i] = cursors[i].fuzzyFetch(FETCHCOUNT);
					if (tables[i] != null && tables[i].length() > 0) {
						ComputeStack stack = ctxs[i].getComputeStack();
						stack.pop();
	
						currents[i] = new Current(tables[i], 1);
						stack.push(currents[i]);

						calc(exps[i], ctxs[i], curValues);
						seqs[i] = 1;
					} else {
						curValues = null;
						values[i] = null;
						ranks[i] = -1;
					}
				} else {
					currents[i].setCurrent(next);
					calc(exps[i], ctxs[i], curValues);
					seqs[i] = next;
				}
			}

			if (curValues != null) {
				ranks[i] = 0;
				for (int j = 0; j < i; ++j) {
					if (ranks[j] == 0) {
						int cmp = Variant.compareArrays(curValues, values[j]);
						if (cmp < 0) {
							ranks[j] = 1;
							for (++j; j < i; ++j) {
								if (ranks[j] == 0) {
									ranks[j] = 1;
								}
							}
						} else if (cmp > 0) {
							ranks[i] = 1;
						}

						break;
					}
				}
			}
		}
	}

	// ����ͬԪ�ص����е���ջ��Ԫ�أ������û����ͬ���򶼵���
	private void popRepeated() {
		Sequence []tables = this.tables;
		Object [][]values = this.values;
		int []seqs = this.seqs;
		int []ranks = this.ranks;
		int count = tables.length;
		Sequence []nextTables = this.nextTables;
		Object [][]nextValues = this.nextValues;
		Context []ctxs = this.ctxs;
		
		boolean hasRepeated = false;
		for (int i = 0; i < count; ++i) {
			if (ranks[i] == 0) {
				int next = seqs[i] + 1;
				if (next > tables[i].length()) {
					nextTables[i] = cursors[i].fuzzyFetch(FETCHCOUNT);
					if (nextTables[i] != null && nextTables[i].length() > 0) {
						ComputeStack stack = ctxs[i].getComputeStack();
						stack.pop();
						
						currents[i] = new Current(nextTables[i], 1);
						stack.push(currents[i]);
						
						calc(exps[i], ctxs[i], nextValues[i]);
						if (Variant.compareArrays(nextValues[i], values[i]) == 0) {
							hasRepeated = true;
							tables[i] = nextTables[i];
							seqs[i] = 1;
							nextTables[i] = null;
						}
					} else {
						nextTables[i] = null;
						nextValues[i] = null;
					}
				} else {
					currents[i].setCurrent(next);
					calc(exps[i], ctxs[i], nextValues[i]);
					if (Variant.compareArrays(nextValues[i], values[i]) == 0) {
						seqs[i] = next;
						hasRepeated = true;
					}
				}
			}
		}

		if (hasRepeated) {
			for (int i = 0; i < count; ++i) {
				if (ranks[i] == 0 && nextTables[i] != null) {
					Sequence table = nextTables[i];
					nextTables[i] = null;

					table.getMems().insert(1, tables[i].getMem(seqs[i]));
					tables[i] = table;
					seqs[i] = 1;
				}
			}
		} else {
			for (int i = 0; i < count; ++i) {
				if (ranks[i] == 0) {
					if(nextTables[i] != null) {
						System.arraycopy(nextValues[i], 0, values[i], 0, values[i].length);
						tables[i] = nextTables[i];
						nextTables[i] = null;
						seqs[i] = 1;
					} else if (nextValues[i] != null) {
						System.arraycopy(nextValues[i], 0, values[i], 0, values[i].length);
						seqs[i]++;
					} else {
						tables[i] = null;
						values[i] = null;
						ranks[i] = -1;
					}
				}

				if (values[i] != null) {
					ranks[i] = 0;
					for (int j = 0; j < i; ++j) {
						if (ranks[j] == 0) {
							int cmp = Variant.compareArrays(values[i], values[j]);
							if (cmp < 0) {
								ranks[j] = 1;
								for (++j; j < i; ++j) {
									if (ranks[j] == 0) {
										ranks[j] = 1;
									}
								}
							} else if (cmp > 0) {
								ranks[i] = 1;
							}

							break;
						}
					}
				}
			}
		}
	}

	private static void calc(Expression []exps, Context ctx, Object []outValues) {
		for (int i = 0, len = exps.length; i < len; ++i) {
			outValues[i] = exps[i].calculate(ctx);
		}
	}
	
	private void getData() {
		if (tables != null) {
			return;
		}

		// ���ñ��ʽ
		for (Expression []curExps : exps) {
			for (Expression exp : curExps) {
				exp.reset();
			}
		}
		
		int tcount = cursors.length;
		int valCount = exps[0].length;
		tables = new Sequence[tcount];
		values = new Object[tcount][];
		seqs = new int[tcount];
		ranks = new int[tcount]; // ���еĵ�ǰԪ�ص�����

		nextTables = new Sequence[tcount];
		nextValues = new Object[tcount][];
		ctxs = new Context[tcount];
		currents = new Current[tcount];

		for (int i = 0; i < tcount; ++i) {
			ctxs[i] = ctx.newComputeContext();
			Sequence table = cursors[i].fuzzyFetch(FETCHCOUNT);
			
			if (table != null && table.length() > 0) {
				Object []curValues = new Object[valCount];
				seqs[i] = 1;

				currents[i] = new Current(table, 1);
				ctxs[i].getComputeStack().push(currents[i]);
				calc(exps[i], ctxs[i], curValues);
				
				tables[i] = table;
				values[i] = curValues;
				ranks[i] = 0;
				nextValues[i] = new Object[valCount];

				for (int j = 0; j < i; ++j) {
					if (ranks[j] == 0) {
						int cmp = Variant.compareArrays(curValues, values[j]);
						if (cmp < 0) {
							ranks[j] = 1;
							for (++j; j < i; ++j) {
								if (ranks[j] == 0) {
									ranks[j] = 1;
								}
							}
						} else if (cmp > 0) {
							ranks[i] = 1;
						}

						break;
					}
				}
			} else {
				ranks[i] = -1;
			}
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (isEnd || n < 1) {
			return 0;
		}
		
		getData();

		int []ranks = this.ranks;
		int tcount = tables.length;
		long count = 0;

		if (type == 0) { // join
			Next:
			while (count < n) {
				for (int i = 0; i < tcount; ++i) {
					if (ranks[i] == 0) {
					} else if (ranks[i] == -1) {
						break Next;
					} else {
						popAll();
						continue Next;
					}
				}

				++count;
				popRepeated();
			}
		} else if (type == 1) { // left join
			while (count < n) {
				if (ranks[0] == 0) {
					count++;
					popRepeated();
				} else if (ranks[0] == -1) {
					break;
				} else {
					popAll();
					continue;
				}
			}
		} else { // full join
			while (count < n) {
				boolean sign = false;
				for (int i = 0; i < tcount; ++i) {
					if (ranks[i] == 0) {
						sign = true;
						break;
					}
				}

				if (sign) {
					count++;
					popRepeated();
				} else {
					break;
				}
			}
		}

		return count;
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

			tables = null;
			values = null;

			nextTables = null;
			nextValues = null;
			isEnd = true;
		}
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		for (int i = 0, count = cursors.length; i < count; ++i) {
			if (!cursors[i].reset()) {
				return false;
			}
		}
		
		isEnd = false;
		return true;
	}
}
