package com.scudata.dm.cursor;

import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.GroupsSyncReader;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.op.Channel;
import com.scudata.dm.op.IGroupsResult;
import com.scudata.dm.op.IHugeGroupsResult;
import com.scudata.dm.op.IPipe;
import com.scudata.dm.op.MultipathChannel;
import com.scudata.dm.op.Operable;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.GroupsJob;
import com.scudata.thread.GroupsJob2;
import com.scudata.thread.ThreadPool;
import com.scudata.util.CursorUtil;
import com.scudata.util.HashUtil;

/**
 * ��·�α꣬���ڶ��̼߳���
 * @author WangXiaoJun
 *
 */
public class MultipathCursors extends ICursor implements IMultipath {
	private ICursor []cursors; // ÿһ·���α깹�ɵ�����
	
	// ���³�Ա�����α��fetch������ͨ����·�α��fetch�ǲ��ᱻ���õ�
	private Sequence table; // �����ļ�¼����
	private CursorReader []readers; // ÿһ·�α��ȡ�����񣬲��ö��߳�
	private boolean isEnd = false; // �Ƿ�ȡ������
	
	/**
	 * ������·�α�
	 * @param cursors �α�����
	 * @param ctx ����������
	 */
	public MultipathCursors(ICursor []cursors, Context ctx) {
		setDataStruct(cursors[0].getDataStruct());
		
		if (hasSame(cursors)) {
			int len = cursors.length;
			for (int i = 0; i < len; ++i) {
				cursors[i] = new SyncCursor(cursors[i]);
				cursors[i].resetContext(ctx.newComputeContext());
			}
		} else {
			for (ICursor cursor : cursors) {
				cursor.resetContext(ctx.newComputeContext());
			}
		}
		
		this.cursors = cursors;
	}
	
	/**
	 * ������·�α�
	 * @param cursors �α�����
	 * @param ctx ����������
	 */
	public MultipathCursors(ICursor []cursors) {
		this.cursors = cursors;
		setDataStruct(cursors[0].getDataStruct());
	}

	/**
	 * ��������·�α���ɵ�����
	 * @return �α�����
	 */
	public ICursor[] getCursors() {
		return cursors;
	}
	
	/**
	 * ȡָ��·���α�
	 * @param p
	 * @return
	 */
	public ICursor getPathCursor(int p) {
		return cursors[p];
	}
	
	/**
	 * ȡ��·�α�·��
	 * @return ·��
	 */
	public int getPathCount() {
		return cursors.length;
	}
	
	private boolean hasSame(ICursor []cursors) {
		int len = cursors.length;
		for (int i = 0; i < len; ++i) {
			ICursor cursor = cursors[i];
			for (int j = i + 1; j < len; ++j) {
				if (cursor == cursors[j]) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Ϊ�α긽������
	 * @param op ����
	 * @param ctx ����������
	 */
	public Operable addOperation(Operation op, Context ctx) {
		for (ICursor cursor : cursors) {
			ctx = cursor.getContext();
			Operation dup = op.duplicate(ctx);
			cursor.addOperation(dup, ctx);
		}
		
		return this;
	}
	
	/**
	 * ��������·�α���ɵ�����
	 * @return �α�����
	 */
	public ICursor[] getParallelCursors() {
		if (readers != null) {
			int len = cursors.length;
			for (int i = 0; i < len; ++i) {
				Sequence seq = readers[i].getCatch();
				if (cache != null) {
					cache.addAll(seq);
					seq = cache;
					cache = null;
				}
				
				if (cursors[i].cache == null) {
					cursors[i].cache = seq;
				} else {
					cursors[i].cache.addAll(seq);
				}
			}
			
			readers = null;
		}
		
		return cursors;
	}
		
	private Sequence getData() {
		if (table != null) return table;

		CursorReader []readers = this.readers;
		int tcount = readers.length;
				
		for (int i = 0; i < tcount; ++i) {
			if (readers[i] != null) {
				Sequence cur = readers[i].getTable();
				if (cur != null) {
					if (table == null) {
						table = cur;
					} else {
						table = append(table, cur);
					}
				} else {
					readers[i] = null;
				}
			}
		}
		
		return table;
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (isEnd || n < 1) return null;
		
		if (readers == null) {
			ICursor []cursors = this.cursors;
			int tcount = cursors.length;
			CursorReader []readers = new CursorReader[tcount];
			this.readers = readers;
			ThreadPool threadPool = ThreadPool.instance();
			
			int avg;
			if (n == ICursor.MAXSIZE) {
				avg = n;
			} else {
				avg = n / tcount;
				if (avg < FETCHCOUNT) {
					avg = FETCHCOUNT;
				} else if (n % tcount != 0) {
					avg++;
				}
			}
			
			for (int i = 0; i < tcount; ++i) {
				readers[i] = new CursorReader(threadPool, cursors[i], avg);
			}
		}
		
		Sequence result = getData();
		if (result == null) {
			return null;
		}
		
		int len = result.length();
		if (len > n) {
			return result.split(1, n);
		} else if (len == n) {
			this.table = null;
			return result;
		}
		
		this.table = null;
		while (true) {
			Sequence cur = getData();
			if (cur == null || cur.length() == 0) {
				return result;
			}
			
			int curLen = cur.length();
			if (len + curLen > n) {
				return append(result, cur.split(1, n - len));
			} else if (len + curLen == n) {
				this.table = null;
				return append(result, cur);
			} else {
				this.table = null;
				result = append(result, cur);
				len += curLen;
			}
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (isEnd || n < 1) return 0;
		
		if (readers == null) {
			if (n == MAXSKIPSIZE) {
				ICursor []cursors = this.cursors;
				int tcount = cursors.length;
				CursorSkipper []skipper = new CursorSkipper[tcount];
				ThreadPool threadPool = ThreadPool.instance();
							
				for (int i = 0; i < tcount; ++i) {
					skipper[i] = new CursorSkipper(threadPool, cursors[i], MAXSKIPSIZE);
				}
				
				long total = 0;
				for (int i = 0; i < tcount; ++i) {
					total += skipper[i].getActualSkipCount();
				}
				
				return total;
			}
			
			ICursor []cursors = this.cursors;
			int tcount = cursors.length;
			CursorReader []readers = new CursorReader[tcount];
			this.readers = readers;
			ThreadPool threadPool = ThreadPool.instance();
						
			for (int i = 0; i < tcount; ++i) {
				readers[i] = new CursorReader(threadPool, cursors[i], FETCHCOUNT);
			}
		}

		Sequence result = getData();
		if (result == null) {
			return 0;
		}
		
		long len = result.length();
		if (len > n) {
			result.split(1, (int)n);
			return n;
		} else if (len == n) {
			this.table = null;
			return n;
		}
		
		this.table = null;
		while (true) {
			Sequence cur = getData();
			if (cur == null || cur.length() == 0) {
				return len;
			}
			
			int curLen = cur.length();
			if (len + curLen > n) {
				cur.split(1, (int)(n - len));
				return n;
			} else if (len + curLen == n) {
				this.table = null;
				return n;
			} else {
				this.table = null;
				len += curLen;
			}
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

			//cursors = null;
			table = null;
			readers = null;
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

	private static Table groups(ICursor []cursors, Expression[] exps, String[] names, 
			Expression[] calcExps, String[] calcNames, String opt, Context ctx, int groupCount) {
		// ���ɷ��������ύ���̳߳�
		int cursorCount = cursors.length;		
		ThreadPool pool = ThreadPool.newInstance(cursorCount);
		GroupsJob []jobs = new GroupsJob[cursorCount];
		
		IGroupsResult groupsResult = null;;
		IGroupsResult []groupsResults = new IGroupsResult[cursorCount - 1];

		try {
			for (int i = 0; i < cursorCount; ++i) {
				Context tmpCtx = ctx.newComputeContext();
				Expression []tmpExps = Operation.dupExpressions(exps, tmpCtx);
				Expression []tmpCalcExps = Operation.dupExpressions(calcExps, tmpCtx);
				
				jobs[i] = new GroupsJob(cursors[i], tmpExps, names, tmpCalcExps, calcNames, opt, tmpCtx);
				if (groupCount > 1) {
					jobs[i].setGroupCount(groupCount);
				}
				
				pool.submit(jobs[i]);
			}
			
			// �ȴ���������ִ����ϣ����ѽ����ӵ�һ�����
			for (int i = 0; i < cursorCount; ++i) {
				jobs[i].join();
				
				if (i == 0) {
					groupsResult = jobs[i].getGroupsResult();
				} else {
					groupsResults[i - 1] = jobs[i].getGroupsResult();
				}
			}
		} finally {
			pool.shutdown();
		}
		
		return groupsResult.combineGroupsResult(groupsResults, ctx);
	}
	
	private static Table groups2(ICursor []cursors, Expression[] exps, String[] names, 
			Expression[] calcExps, String[] calcNames, String opt, Context ctx, int groupCount) {
		int capacity = groupCount > 0 ? groupCount : 30000000;//Ĭ��3000��
		HashUtil hashUtil = new HashUtil(capacity);
		GroupsSyncReader cursorReader = new GroupsSyncReader(cursors, exps, hashUtil, ctx);
		capacity = hashUtil.getCapacity();
		
		// ���ɷ��������ύ���̳߳�
		int cursorCount = cursors.length / 2;
		if (cursorCount < 2) cursorCount = 2;
		ThreadPool pool = ThreadPool.newInstance(cursorCount);
		GroupsJob2 []jobs = new GroupsJob2[cursorCount];
		
		Table groupsResult = null;

		try {
			for (int i = 0; i < cursorCount; ++i) {
				Context tmpCtx = ctx.newComputeContext();
				Expression []tmpExps = Operation.dupExpressions(exps, tmpCtx);
				Expression []tmpCalcExps = Operation.dupExpressions(calcExps, tmpCtx);
				
				GroupsJob2 job = new GroupsJob2(cursorReader, tmpExps, names, tmpCalcExps, calcNames, opt, tmpCtx, capacity);
				job.setHashStart(i);
				job.setHashEnd(cursorCount);
				jobs[i] = job;
				
				pool.submit(jobs[i]);
			}
			
			// �ȴ���������ִ����ϣ����ѽ����ӵ�һ�����
			for (int i = 0; i < cursorCount; ++i) {
				jobs[i].join();
				
				if (i == 0) {
					groupsResult = jobs[i].getGroupsResult().getResultTable();
				} else {
					Table t = jobs[i].getGroupsResult().getResultTable();
					groupsResult.addAll(t);
				}
			}
		} finally {
			pool.shutdown();
		}
		
		if (opt == null || opt.indexOf('u') == -1) {
			int keyCount = exps.length;
			int []fields = new int[keyCount];
			for (int i = 0; i < keyCount; ++i) {
				fields[i] = i;
			}

			groupsResult.sortFields(fields);
		}
		return groupsResult;
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
		return cursors[0].getGroupsResult(exps, names, calcExps, calcNames, opt, ctx);
	}
	
	/**
	 * ȡ����������������
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps �����ֶα��ʽ����
	 * @param calcNames �����ֶ�������
	 * @param opt ѡ��
	 * @param capacity ��ʼ����
	 * @param ctx ����������
	 * @return IHugeGroupsResult
	 */
	public IHugeGroupsResult getHugeGroupsResult(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, String opt, int capacity, Context ctx) {
		return cursors[0].getHugeGroupsResult(exps, names, calcExps, calcNames, opt, capacity, ctx);
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
	public Table groups(Expression[] exps, String[] names, Expression[] calcExps, String[] calcNames, String opt, Context ctx) {
		if (cursors.length == 1) {
			return cursors[0].groups(exps, names, calcExps, calcNames, opt, ctx);
		} else if (opt != null && opt.indexOf('s') != -1) {
			IHugeGroupsResult groupsResult = cursors[0].getHugeGroupsResult(exps, names, calcExps, calcNames, opt, 0, ctx);
			return groupsResult.groups(cursors);
		} else {
			return groups(cursors, exps, names, calcExps, calcNames, opt, ctx, -1);
		}
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
		if (cursors.length == 1) {
			return cursors[0].groups(exps, names, calcExps, calcNames, opt, ctx, groupCount);
		} else if (opt != null && opt.indexOf('s') != -1) {
			IHugeGroupsResult groupsResult = cursors[0].getHugeGroupsResult(exps, names, calcExps, calcNames, opt, groupCount, ctx);
			return groupsResult.groups(cursors);
		} else if (opt != null && opt.indexOf('z') != -1) {
			return groups2(cursors, exps, names, calcExps, calcNames, opt, ctx, groupCount);
		} else if (groupCount < 1 || exps == null || exps.length == 0) {
			return groups(cursors, exps, names, calcExps, calcNames, opt, ctx, -1);
		} else if (opt != null && opt.indexOf('n') != -1) {
			return groups(cursors, exps, names, calcExps, calcNames, opt, ctx, groupCount);
		} else {
			return CursorUtil.fuzzyGroups(this, exps, names, calcExps, calcNames, opt, ctx, groupCount);
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
			ICursor []codeCursors, Expression[][] codeExps, 
			Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		int pathCount = cursors.length;
		int tableCount = codeCursors.length;
		
		for (int p = 0; p < pathCount; ++p) {
			ICursor []curCodeCursors = new ICursor[tableCount];
			for (int t = 0; t < tableCount; ++t) {
				// ���ӱ���Ҫͬ���ֶΣ�ͬһ·��������
				if (codeCursors[t] == null) {
					continue;
				} else if (!(codeCursors[t] instanceof MultipathCursors)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException( mm.getMessage("dw.mcsNotMatch"));
				}
				
				MultipathCursors mcs = (MultipathCursors)codeCursors[t];
				if (mcs.getPathCount() != pathCount) {
					MessageManager mm = EngineMessage.get();
					throw new RQException( mm.getMessage("dw.mcsNotMatch"));
				}
				
				curCodeCursors[t] = mcs.getPathCursor(p);
			}
			
			// ���Ʊ��ʽ
			ctx = cursors[p].getContext();
			Expression [][]curExps = Operation.dupExpressions(exps, ctx);
			Expression [][]curCodeExps = Operation.dupExpressions(codeExps, ctx);
			Expression [][]curNewExps = Operation.dupExpressions(newExps, ctx);
			cursors[p] = (ICursor) cursors[p].mergeJoinx(function, curExps, curCodeCursors, curCodeExps, curNewExps, newNames, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * ������
	 * @param function ��Ӧ�ĺ���
	 * @param fname
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param codes ά������
	 * @param dataExps ά������ֶα��ʽ����
	 * @param newExps
	 * @param newNames
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable join(Function function, String fname, Expression[][] exps, Sequence[] codes,
			  Expression[][] dataExps, Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			// ���Ʊ��ʽ
			ctx = subCursor.getContext();
			Expression [][]curExps = Operation.dupExpressions(exps, ctx);
			Expression [][]curDataExps = Operation.dupExpressions(dataExps, ctx);
			Expression [][]curNewExps = Operation.dupExpressions(newExps, ctx);
			subCursor.join(function, fname, curExps, codes, curDataExps, curNewExps, newNames, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * ��Զ�̱�������
	 * @param function ��Ӧ�ĺ���
	 * @param fname
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param codes ά������
	 * @param dataExps ά������ֶα��ʽ����
	 * @param newExps
	 * @param newNames
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable joinRemote(Function function, String fname, Expression[][] exps, 
			Object[] codes, Expression[][] dataExps, 
			Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			// ���Ʊ��ʽ
			ctx = subCursor.getContext();
			Expression [][]curExps = Operation.dupExpressions(exps, ctx);
			Expression [][]curDataExps = Operation.dupExpressions(dataExps, ctx);
			Expression [][]curNewExps = Operation.dupExpressions(newExps, ctx);
			subCursor.joinRemote(function, fname, curExps, codes, curDataExps, curNewExps, newNames, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * �����ʽ����
	 * @param function
	 * @param dimExps ���ӱ��ʽ����
	 * @param aliasNames ά���¼����
	 * @param newExps �²����ֶα��ʽ����
	 * @param newNames �²����ֶ�������
	 * @param opt ѡ�i����������
	 * @param ctx
	 * @return
	 */
	public Operable fjoin(Function function, Expression[] dimExps, String []aliasNames, 
			Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			// ���Ʊ��ʽ
			ctx = subCursor.getContext();
			Expression []curDimExps = Operation.dupExpressions(dimExps, ctx);
			Expression [][]curNewExps = Operation.dupExpressions(newExps, ctx);
			subCursor.fjoin(function, curDimExps, aliasNames, curNewExps, newNames, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * �α갴��������������
	 * @param function
	 * @param srcKeyExps ���ӱ��ʽ����
	 * @param srcNewExps
	 * @param srcNewNames
	 * @param joinCursors �����α�����
	 * @param options ����ѡ��
	 * @param keyExps ���ӱ��ʽ����
	 * @param newExps
	 * @param newNames
	 * @param opt
	 * @param ctx
	 * @return
	 */
	public Operable pjoin(Function function, Expression []srcKeyExps, Expression []srcNewExps, String []srcNewNames, 
			ICursor []joinCursors, String []options, Expression [][]keyExps, 
			Expression [][]newExps, String [][]newNames, String opt, Context ctx) {
		int pathCount = cursors.length;
		int tableCount = joinCursors.length;
		
		for (int p = 0; p < pathCount; ++p) {
			ICursor []curCursors = new ICursor[tableCount];
			for (int t = 0; t < tableCount; ++t) {
				// ���ӱ���Ҫͬ���ֶΣ�ͬһ·��������
				if (joinCursors[t] == null) {
					continue;
				} else if (!(joinCursors[t] instanceof MultipathCursors)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException( mm.getMessage("dw.mcsNotMatch"));
				}
				
				MultipathCursors mcs = (MultipathCursors)joinCursors[t];
				if (mcs.getPathCount() != pathCount) {
					MessageManager mm = EngineMessage.get();
					throw new RQException( mm.getMessage("dw.mcsNotMatch"));
				}
				
				curCursors[t] = mcs.getPathCursor(p);
			}
			
			// ���Ʊ��ʽ
			ctx = cursors[p].getContext();
			Expression []curSrcKeyExps = Operation.dupExpressions(srcKeyExps, ctx);
			Expression []curSrcNewExps = Operation.dupExpressions(srcNewExps, ctx);
			Expression [][]curKeyExps = Operation.dupExpressions(keyExps, ctx);
			Expression [][]curNewExps = Operation.dupExpressions(newExps, ctx);
			cursors[p].pjoin(function, curSrcKeyExps, curSrcNewExps, srcNewNames, 
					curCursors, options, curKeyExps, curNewExps, newNames, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * �������ӹ��ˣ������ܹ����ϵ�
	 * @param function ��Ӧ�ĺ���
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param codes ά������
	 * @param dataExps ά������ֶα��ʽ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable filterJoin(Function function, Expression[][] exps, Sequence[] codes, Expression[][] dataExps, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			// ���Ʊ��ʽ
			ctx = subCursor.getContext();
			Expression [][]curExps = Operation.dupExpressions(exps, ctx);
			Expression [][]curDataExps = Operation.dupExpressions(dataExps, ctx);
			subCursor.filterJoin(function, curExps, codes, curDataExps, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * �������ӹ��ˣ������ܹ������ϵ�
	 * @param function ��Ӧ�ĺ���
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param codes ά������
	 * @param dataExps ά������ֶα��ʽ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable diffJoin(Function function, Expression[][] exps, Sequence[] codes, Expression[][] dataExps, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			// ���Ʊ��ʽ
			ctx = subCursor.getContext();
			Expression [][]curExps = Operation.dupExpressions(exps, ctx);
			Expression [][]curDataExps = Operation.dupExpressions(dataExps, ctx);
			subCursor.diffJoin(function, curExps, codes, curDataExps, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * ����
	 * @param function ��Ӧ�ĺ���
	 * @param fltExp ��������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable select(Function function, Expression fltExp, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			ctx = subCursor.getContext();
			Expression curFilter = Operation.dupExpression(fltExp, ctx);
			subCursor.select(function, curFilter, opt, ctx);
		}
		
		return this;
	}
	/**
	 * ����
	 * @param function ��Ӧ�ĺ���
	 * @param fltExp ��������
	 * @param opt ѡ��
	 * @param pipe ���ڴ������������ĳ�Ա
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable select(Function function, Expression fltExp, String opt, IPipe pipe, Context ctx) {
		for (ICursor subCursor : cursors) {
			ctx = subCursor.getContext();
			Expression curFilter = Operation.dupExpression(fltExp, ctx);
			subCursor.select(function, curFilter, opt, pipe, ctx);
		}
		
		return this;
	}
	
	/**
	 * ��Ӽ�����
	 * @param function ��Ӧ�ĺ���
	 * @param exps ������ʽ����
	 * @param names �ֶ�������
	 * @param opt ѡ��
	 * @param level
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable derive(Function function, Expression []exps, String []names, String opt, int level, Context ctx) {
		for (ICursor subCursor : cursors) {
			ctx = subCursor.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			subCursor.derive(function, curExps, names, opt, level, ctx);
		}
		
		return this;
	}
	
	/**
	 * ���������
	 * @param function ��Ӧ�ĺ���
	 * @param newExps ������ʽ����
	 * @param names �ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable newTable(Function function, Expression []newExps, String []names, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			ctx = subCursor.getContext();
			Expression []curExps = Operation.dupExpressions(newExps, ctx);
			subCursor.newTable(function, curExps, names, opt, ctx);
		}
		
		return this;
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
		for (ICursor subCursor : cursors) {
			ctx = subCursor.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			subCursor.group(function, curExps, opt, ctx);
		}
		
		return this;
	}
	/**
	 * ���������������
	 * @param function ��Ӧ�ĺ���
	 * @param exps ǰ�벿������ķ����ֶα��ʽ
	 * @param sortExps ��벿������ķ����ֶα��ʽ
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable group(Function function, Expression []exps, Expression []sortExps, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			ctx = subCursor.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			Expression []curSortExps = Operation.dupExpressions(sortExps, ctx);
			subCursor.group(function, curExps, curSortExps, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * ���������������
	 * @param function ��Ӧ�ĺ���
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param newExps ���ܱ��ʽ
	 * @param newNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable group(Function function, Expression[] exps, String []names, 
			Expression[] newExps, String []newNames, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			ctx = subCursor.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			Expression []curNewExps = Operation.dupExpressions(newExps, ctx);
			subCursor.group(function, curExps, names, curNewExps, newNames, opt, ctx);
		}
		
		return this;
	}

	/**
	 * ���������������
	 * @param function ��Ӧ�ĺ���
	 * @param exps ǰ�벿������ķ����ֶα��ʽ
	 * @param names �ֶ�������
	 * @param sortExps ��벿������ķ����ֶα��ʽ
	 * @param sortNames �ֶ�������
	 * @param newExps ���ܱ��ʽ
	 * @param newNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable group(Function function, Expression[] exps, String []names, 
			Expression[] sortExps, String []sortNames, 
			Expression[] newExps, String []newNames, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			ctx = subCursor.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			Expression []curSortExps = Operation.dupExpressions(sortExps, ctx);
			Expression []curNewExps = Operation.dupExpressions(newExps, ctx);
			subCursor.group(function, curExps, names, curSortExps, sortNames, curNewExps, newNames, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * ���Ӽ���
	 * @param function �����ĺ�������
	 * @param fkNames ����ֶ�������
	 * @param timeFkNames ʱ�����������
	 * @param codes ά������
	 * @param exps ά����������
	 * @param timeExps ά���ʱ����¼�����
	 * @param opt ѡ��
	 */
	public Operable switchFk(Function function, String[] fkNames, String[] timeFkNames, Sequence[] codes, Expression[] exps, Expression[] timeExps, String opt, Context ctx) {
		for (ICursor subCursor : cursors) {
			ctx = subCursor.getContext();
			Expression []curexps = Operation.dupExpressions(exps, ctx);
			Expression []curTimeExps = Operation.dupExpressions(timeExps, ctx);
			subCursor.switchFk(function, fkNames, timeFkNames, codes, curexps, curTimeExps, opt, ctx);
		}
		
		return this;
	}

	/**
	 * ����һ���뵱ǰ�α���ƥ��Ĺܵ�
	 * @param ctx ����������
	 * @param doPush �Ƿ���α�����push����
	 * @return Channel
	 */
	public Channel newChannel(Context ctx, boolean doPush) {
		return new MultipathChannel(ctx, this, doPush);
	}
	
	/**
	 * �α��Ƿ��������
	 * @return
	 */
	public boolean canSkipBlock() {
		if (cursors != null) {
			for (ICursor subCursor : cursors) {
				if (!subCursor.canSkipBlock()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * �õ��α������ڿ�ķ�Χ
	 * @param key
	 * @return
	 */
	public IArray[] getSkipBlockInfo(String key) {
		int count = cursors.length;
		ObjectArray[] result = new ObjectArray[] {new ObjectArray(count)};
		for (int i = 0; i < count; i++) {
			Object obj = cursors[i].getSkipBlockInfo(key);
			result[0].add(obj);
		}
		return result;
	}
	
	/**
	 * ���α�����Ϊ����key�ֶ����� ��pjoinʱʹ�ã�
	 * ���ú��α�ᰴ��values���ֵ�������顣
	 * @param key ά�ֶ���
	 * @param values [[minValue, maxValue],[minValue, maxValue],����] 
	 */
	public void setSkipBlockInfo(String key, IArray[] values) {
		if (key == null || values == null) return;
		ObjectArray valueArray = (ObjectArray) values[0];
		int count = cursors.length;
		for (int i = 0; i < count; i++) {
			IArray[] val = (IArray[]) valueArray.get(i + 1);
			cursors[i].setSkipBlockInfo(key, val);
		}
	}
}
