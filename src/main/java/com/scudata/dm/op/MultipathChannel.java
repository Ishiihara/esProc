package com.scudata.dm.op;

import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;

/**
 * ��·�ܵ����󣬹ܵ����Ը��Ӷ������㣬��ֻ�ܶ���һ�ֽ��������
 * @author WangXiaoJun
 *
 */
public class MultipathChannel extends Channel {
	private Channel []channels;
	
	/**
	 * �ɶ�·�α깹����·�ܵ�
	 * @param ctx ����������
	 * @param mcs ��·�α�
	 */
	public MultipathChannel(Context ctx, MultipathCursors mcs) {
		this(ctx, mcs, true);
	}
	
	/**
	 * �ɶ�·�α깹����·�ܵ�
	 * @param ctx ����������
	 * @param mcs ��·�α�
	 * @param doPush �Ƿ���α�����push����
		�α����fetch@0������һ�������ݣ�����ܵ������㻹û������͸��α긽��push���ᵼ�»��������û�б��ܵ��������ӵ��������
	 */
	public MultipathChannel(Context ctx, MultipathCursors mcs, boolean doPush) {
		super(ctx);
		
		ICursor []cursors = mcs.getCursors();
		int count = cursors.length;
		channels = new Channel[count];
		
		for (int i = 0; i < count; ++i) {
			channels[i] = cursors[i].newChannel(cursors[i].getContext(), doPush);
		}
	}
	
	/**
	 * ���α����push���ݵ��ܵ��Ĳ���
	 * @param cs
	 */
	public void addPushToCursor(ICursor cs) {
		MultipathCursors mcs = (MultipathCursors)cs;
		ICursor []cursors = mcs.getCursors();
		int count = cursors.length;
		
		for (int i = 0; i < count; ++i) {
			Push push = new Push(channels[i]);
			cursors[i].addOperation(push, cursors[i].getContext());
		}
	}
	
	/**
	 * Ϊ�ܵ���������
	 * @param op ����
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable addOperation(Operation op, Context ctx) {
		checkResultChannel();
		for (Channel channel : channels) {
			ctx = channel.getContext();
			channel.addOperation(op.duplicate(ctx), ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression curFilter = Operation.dupExpression(fltExp, ctx);
			channel.select(function, curFilter, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression curFilter = Operation.dupExpression(fltExp, ctx);
			channel.select(function, curFilter, opt, pipe, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression [][]curExps = Operation.dupExpressions(exps, ctx);
			Expression [][]curDataExps = Operation.dupExpressions(dataExps, ctx);
			channel.filterJoin(function, curExps, codes, curDataExps, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression [][]curExps = Operation.dupExpressions(exps, ctx);
			Expression [][]curDataExps = Operation.dupExpressions(dataExps, ctx);
			channel.diffJoin(function, curExps, codes, curDataExps, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression [][]curExps = Operation.dupExpressions(exps, ctx);
			Expression [][]curDataExps = Operation.dupExpressions(dataExps, ctx);
			Expression [][]curNewExps = Operation.dupExpressions(newExps, ctx);
			channel.join(function, fname, curExps, codes, curDataExps, curNewExps, newNames, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression [][]curExps = Operation.dupExpressions(exps, ctx);
			Expression [][]curDataExps = Operation.dupExpressions(dataExps, ctx);
			Expression [][]curNewExps = Operation.dupExpressions(newExps, ctx);
			channel.joinRemote(function, fname, curExps, codes, curDataExps, curNewExps, newNames, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression []curDimExps = Operation.dupExpressions(dimExps, ctx);
			Expression [][]curNewExps = Operation.dupExpressions(newExps, ctx);
			channel.fjoin(function, curDimExps, aliasNames, curNewExps, newNames, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			channel.derive(function, curExps, names, opt, level, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression []curExps = Operation.dupExpressions(newExps, ctx);
			channel.newTable(function, curExps, names, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			channel.group(function, curExps, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			Expression []curSortExps = Operation.dupExpressions(sortExps, ctx);
			channel.group(function, curExps, curSortExps, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			Expression []curNewExps = Operation.dupExpressions(newExps, ctx);
			channel.group(function, curExps, names, curNewExps, newNames, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression []curExps = Operation.dupExpressions(exps, ctx);
			Expression []curSortExps = Operation.dupExpressions(sortExps, ctx);
			Expression []curNewExps = Operation.dupExpressions(newExps, ctx);
			channel.group(function, curExps, names, curSortExps, sortNames, curNewExps, newNames, opt, ctx);
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
		checkResultChannel();
		
		for (Channel channel : channels) {
			ctx = channel.getContext();
			Expression []curexps = Operation.dupExpressions(exps, ctx);
			Expression []curTimeExps = Operation.dupExpressions(timeExps, ctx);
			channel.switchFk(function, fkNames, timeFkNames, codes, curexps, curTimeExps, opt, ctx);
		}
		
		return this;
	}
	
	/**
	 * ����Ƿ��Ѿ��н����������
	 */
	protected void checkResultChannel() {
		if (result != null) {
			throw new RQException("���ӽ����֮�����ټ���������������");
		}
	}
	
	/**
	 * ���ܵ��������ݣ����ܻ��ж��Դͬʱ���ܵ���������
	 * @param seq ����
	 * @param ctx ����������
	 */
	public synchronized void push(Sequence seq, Context ctx) {
		if (result != null && seq != null) {
			result.push(seq, ctx);
		}
	}
	
	/**
	 * �������ͽ���ʱ���ã���Щ���ӵĲ����Ỻ�����ݣ���Ҫ����finish�������Ĵ���
	 * @param ctx ����������
	 */
	public void finish(Context ctx) {
		// ÿ·�Ĺܵ��Ѿ����ù�finish
		//for (Channel channel : channels) {
		//	channel.finish(ctx);
		//}
	}
	
	/**
	 * ���عܵ��ļ�����
	 * @return
	 */
	public Object result() {
		if (result instanceof IGroupsResult) {
			int count = channels.length;
			IGroupsResult groupsResult = (IGroupsResult)channels[0].getResult();
			IGroupsResult []groupsResults = new IGroupsResult[count - 1];
			
			for (int i = 1; i < count; ++i) {
				groupsResults[i - 1] = (IGroupsResult)channels[i].getResult();
			}
			
			Table value = groupsResult.combineGroupsResult(groupsResults, ctx);
			if (resultNew == null) {
				return value;
			} else {
				Sequence table = resultNew.process(value, ctx);
				if (pkCount > 0 && table instanceof Table) {
					String []pks = new String[pkCount];
					for (int i = 1; i <= pkCount; ++i) {
						pks[i - 1] = "#" + i;
					}
					
					((Table)table).setPrimary(pks);
					return table;
				} else {
					return table;
				}
			}
		} else if (result instanceof TotalResult) {
			int count = channels.length;
			IGroupsResult groupsResult = (IGroupsResult)channels[0].getResult();
			IGroupsResult []groupsResults = new IGroupsResult[count - 1];
			
			for (int i = 1; i < count; ++i) {
				groupsResults[i - 1] = (IGroupsResult)channels[i].getResult();
			}
			
			Table table = groupsResult.combineGroupsResult(groupsResults, ctx);
			if (table == null || table.length() == 0) {
				return null;
			} else {
				TotalResult total = (TotalResult)result;
				BaseRecord r = table.getRecord(1);
				int valCount = total.getCalcExps().length;
				if (valCount == 1) {
					return r.getNormalFieldValue(0);
				} else {
					Sequence seq = new Sequence(valCount);
					for (int i = 0; i < valCount; ++i) {
						seq.add(r.getNormalFieldValue(i));
					}
					
					return seq;
				}
			}
		} else if (result != null) {
			Object val = result.result();
			result = null;
			return val;
		} else {
			return null;
		}
	}
	
	/**
	 * �����ܵ���ǰ������Ϊ�����
	 * @return
	 */
	public Channel fetch() {
		checkResultChannel(); 	
		result = new FetchResult();

		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}
		
		return this;
	}
	
	/**
	 * �����ܵ���ǰ���ݵ����ļ�
	 * @param file ���ļ�
	 * @return this
	 */
	public Channel fetch(FileObject file) {
		checkResultChannel();
		result = new FetchResult(file);

		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ��з������㲢��Ϊ�����
	 * @param exps ������ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps ���ܱ��ʽ����
	 * @param calcNames �����ֶ���
	 * @param opt ѡ��
	 * @return
	 */
	public Channel groups(Expression[] exps, String[] names,
			   Expression[] calcExps, String[] calcNames, String opt) {
		checkResultChannel();
		result = IGroupsResult.instance(exps, names, calcExps, calcNames, null, opt, ctx);
		
		for (Channel channel : channels) {
			Context ctx = channel.getContext();
			exps = Operation.dupExpressions(exps, ctx);
			calcExps = Operation.dupExpressions(calcExps, ctx);
			channel.groups(exps, names, calcExps, calcNames, opt);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ��л������㲢��Ϊ�����
	 * @param calcExps ���ܱ��ʽ
	 * @return
	 */
	public Channel total(Expression[] calcExps) {
		checkResultChannel();
		result = new TotalResult(calcExps, ctx, null);
		
		for (Channel channel : channels) {
			Context ctx = channel.getContext();
			calcExps = Operation.dupExpressions(calcExps, ctx);
			channel.groups(null, null, calcExps, null, null);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ������������㲢��Ϊ�����
	 * @param exps ������ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps ���ܱ��ʽ����
	 * @param calcNames �����ֶ���
	 * @param opt ѡ��
	 * @param capacity �ڴ���Դ�ŵķ���������
	 * @return
	 */
	public Channel groupx(Expression[] exps, String[] names,
			   Expression[] calcExps, String[] calcNames, String opt, int capacity) {
		checkResultChannel();
		result = new GroupxResult(exps, names, calcExps, calcNames, opt, ctx, capacity);

		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ�������������㲢��Ϊ�����
	 * @param exps ������ʽ����
	 * @param capacity �ڴ���Դ�ŵļ�¼����
	 * @param opt ѡ��
	 * @return
	 */
	public Channel sortx(Expression[] exps, int capacity, String opt) {
		checkResultChannel();
		result = new SortxResult(exps, ctx, capacity, opt);

		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ����������㲢��Ϊ�����
	 * @param fields
	 * @param fileTable
	 * @param keys
	 * @param exps
	 * @param expNames
	 * @param fname
	 * @param ctx
	 * @param option
	 * @param capacity
	 * @return
	 */
	public Channel joinx(Expression [][]fields, Object []fileTable, Expression[][] keys, 
			Expression[][] exps, String[][] expNames, String fname, Context ctx, String option, int capacity) {
		checkResultChannel();
		result = new CsJoinxResult(fields, fileTable, keys, exps, expNames, fname, ctx, option, capacity);

		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ��е������㲢��Ϊ�����
	 * @param exp �������ʽ
	 * @param initVal ��ʼֵ
	 * @param c �������ʽ������������cΪ������ǰ����
	 * @param ctx ����������
	 * @return
	 */
	public Channel iterate(Expression exp, Object initVal, Expression c, Context ctx) {
		checkResultChannel();
		result = new IterateResult(exp, initVal, c, ctx);
		
		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ���ȥ�����㲢��Ϊ�����
	 * @param exps ȥ�ر��ʽ
	 * @param count
	 * @param opt ѡ��
	 * @return
	 */
	public Channel id(Expression []exps, int count, String opt) {
		checkResultChannel();
		result = new IDResult(exps, count, opt, ctx);
		
		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
}