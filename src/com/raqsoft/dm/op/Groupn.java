package com.raqsoft.dm.op;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.ComputeStack;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.FileObject;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.cursor.ICursor;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.Function;
import com.raqsoft.resources.EngineMessage;

public class Groupn extends Operation {
	private Expression exp; // �������ʽ
	
	private IPipe []pipes; // �ܵ����飬���ݰ�����ֵд����Ӧ�Ĺܵ�
	private Sequence []groups; // ���ڴ��ÿ�����������
	
	public Groupn(Expression exp, Sequence result) {
		this(null, exp, result);
	}

	private Groupn(Function function, Expression exp, IPipe []pipes) {
		super(function);
		this.exp = exp;
		this.pipes = pipes;
		
		int count = pipes.length;
		groups = new Sequence[count];
		for (int i = 1; i < count; ++i) {
			groups[i] = new Sequence(ICursor.FETCHCOUNT);
		}
	}
	
	public Groupn(Function function, Expression exp, Sequence out) {
		super(function);
		this.exp = exp;
		//this.out = out;
		
		int count = out.length();
		if (count == 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("groupn" + mm.getMessage("function.invalidParam"));
		}
		
		pipes = new IPipe[count + 1];
		if (out.getMem(1) instanceof Channel) {
			for (int i = 1; i <= count; ++i) {
				Object obj = out.getMem(i);
				if (!(obj instanceof Channel)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("groupn" + mm.getMessage("function.paramTypeError"));
				}
				
				pipes[i] = (Channel)obj;
			}
		} else {
			for (int i = 1; i <= count; ++i) {
				Object obj = out.getMem(i);
				if (obj instanceof String) {
					FileObject fo = new FileObject((String)obj);
					pipes[i] = new FilePipe(fo);
				} else if (obj instanceof FileObject) {
					pipes[i] = new FilePipe((FileObject)obj);
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("groupn" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		
		groups = new Sequence[count + 1];
		for (int i = 1; i <= count; ++i) {
			groups[i] = new Sequence(ICursor.FETCHCOUNT);
		}
	}
		
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ����ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression dupExp = dupExpression(exp, ctx);
		return new Groupn(function, dupExp, pipes);
	}
	
	/**
	 * ����ȫ���������ʱ����
	 * @param ctx ����������
	 * @return Sequence
	 */
	public Sequence finish(Context ctx) {
		for (int i = 1, len = pipes.length; i < len; ++i) {
			pipes[i].finish(ctx);
		}
		
		return null;
	}
	
	private static void group_n(Sequence table, Expression exp, Context ctx, Sequence []groups) {
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = table.new Current();
		stack.push(current);
		int gcount = groups.length;
		
		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				Object obj = exp.calculate(ctx);
				if (!(obj instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("groupn: " + mm.getMessage("engine.needIntExp"));
				}

				int index = ((Number)obj).intValue();
				if (index < 1 || index > gcount) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(index + mm.getMessage("engine.indexOutofBound"));
				}

				groups[index].add(table.getMem(i));
			}
		} finally {
			stack.pop();
		}
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		Sequence []groups = this.groups;
		group_n(seq, exp, ctx, groups);
		
		IPipe []pipes = this.pipes;
		for (int i = 1, count = pipes.length; i < count; ++i) {
			pipes[i].push(groups[i], ctx);
			groups[i].clear();
		}
				
		return seq;
	}
}