package com.scudata.dm.op;

import java.util.ArrayList;

import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;

/**
 * �ܵ����󣬹ܵ����Ը��Ӷ������㣬��ֻ�ܶ���һ�ֽ��������
 * @author WangXiaoJun
 *
 */
public class Channel extends Operable implements IPipe {
	protected Context ctx; // �ö��߳��α�ȡ��ʱ��Ҫ���������Ĳ����½������ʽ
	private ArrayList<Operation> opList; // ���Ӳ����б�
	protected IResult result; // �ܵ����յĽ��������
	
	// ������ʽ�������sum(...)+sum(...)�����Ļ��������groups(...).new(...)�����ڴ�ź����new
	protected New resultNew;
	protected int pkCount; // �����ֶ���
	
	/**
	 * �����ܵ�
	 * @param ctx ����������
	 */
	public Channel(Context ctx) {
		this.ctx = ctx;
	}
	
	/**
	 * ���α깹���ܵ����α�����ݽ����Ƹ��˹ܵ�
	 * @param ctx ����������
	 * @param cs �α�
	 */
	public Channel(Context ctx, ICursor cs) {
		this.ctx = ctx;
		Push push = new Push(this);
		cs.addOperation(push, ctx);
	}
	
	/**
	 * ���α����push���ݵ��ܵ��Ĳ���
	 * @param cs
	 */
	public void addPushToCursor(ICursor cs) {
		Push push = new Push(this);
		cs.addOperation(push, ctx);
	}
	
	/**
	 * Ϊ�ܵ���������
	 * @param op ����
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable addOperation(Operation op, Context ctx) {
		checkResultChannel();
		
		this.ctx = ctx;
		if (opList == null) {
			opList = new ArrayList<Operation>();
		}
		
		opList.add(op);
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
		if (opList != null) {
			for (Operation op : opList) {
				if (seq == null || seq.length() == 0) {
					return;
				}
				
				seq = op.process(seq, ctx);
			}
		}
		
		if (result != null && seq != null) {
			result.push(seq, ctx);
		}
	}
	
	/**
	 * �������ͽ���ʱ���ã���Щ���ӵĲ����Ỻ�����ݣ���Ҫ����finish�������Ĵ���
	 * @param ctx ����������
	 */
	public void finish(Context ctx) {
		if (opList == null) {
			if (result != null) {
				result.finish(ctx);
			}
		} else {
			Sequence seq = null;
			for (Operation op : opList) {
				if (seq == null) {
					seq = op.finish(ctx);
				} else {
					seq = op.process(seq, ctx);
					Sequence tmp = op.finish(ctx);
					if (tmp != null) {
						if (seq != null) {
							seq = ICursor.append(seq, tmp);
						} else {
							seq = tmp;
						}
					}
				}
			}
			
			if (result != null) {
				if (seq != null) {
					result.push(seq, ctx);
				}
				
				result.finish(ctx);
			}
		}
	}
	
	/**
	 * ���عܵ��ļ�����
	 * @return
	 */
	public Object result() {
		if (result == null) {
			return null;
		}
		
		Object val = result.result();
		result = null;
		
		if (resultNew == null) {
			return val;
		} else {
			if (val instanceof Sequence) {
				Sequence table = resultNew.process((Sequence)val, ctx);
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
			} else {
				return val;
			}
		}
	}
	
	/**
	 * �����ܵ���ǰ������Ϊ�����
	 * @return this
	 */
	public Channel fetch() {
		checkResultChannel();
		result = new FetchResult();
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
		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ��л������㲢��Ϊ�����
	 * @param calcExps ���ܱ��ʽ
	 * @return
	 */
	public Channel total(Expression[] calcExps) {
		groups(null, null, calcExps, null, null);
		result = new TotalResult(calcExps, ctx, (IGroupsResult)result);
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
		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ���ȥ�����㲢��Ϊ�����
	 * @param exps ȥ�ر��ʽ
	 * @param count �����Ľ����
	 * @param opt ѡ��
	 * @return
	 */
	public Channel id(Expression []exps, int count, String opt) {
		checkResultChannel();
		result = new IDResult(exps, count, opt, ctx);
		return this;
	}
	
	/**
	 * ȡ�ܵ��Ľ������
	 * @return IResult
	 */
	public IResult getResult() {
		return result;
	}
	
	/**
	 * groups������������ֶβ��ǵ����ľۺϱ������Ҫ��newһ��
	 * @param op new����
	 * @param pkCount
	 */
	public void setResultNew(New op, int pkCount) {
		resultNew = op;
		this.pkCount = pkCount;
	}
	
	/**
	 * ȡ����������
	 * @return Context
	 */
	public Context getContext() {
		return ctx;
	}
}