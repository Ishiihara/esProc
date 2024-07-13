package com.scudata.expression;

import java.util.ArrayList;
import java.util.List;

import com.scudata.cellset.ICellSet;
import com.scudata.cellset.INormalCell;
import com.scudata.common.ArgumentTokenizer;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Sentence;
import com.scudata.dm.Context;
import com.scudata.dm.ParamList;
import com.scudata.resources.EngineMessage;

/**
 * �������������ɶ����
 * @author RunQian
 *
 */
public final class ParamParser {
	// �ָ����ڵ� ';' ',' ':'
	private static class SymbolParam implements IParam {
		private char level;
		private ArrayList<IParam> paramList = new ArrayList<IParam>(3);

		public SymbolParam(char level) {
			this.level = level;
		}

		public boolean isLeaf() {
			return false;
		}

		public int getSubSize() {
			return paramList.size();
		}

		public IParam getSub(int index) {
			return paramList.get(index);
		}

		public char getType() {
			return level;
		}

		public Expression getLeafExpression() {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("function.invalidParam"));
		}
		

		public void getAllLeafExpression(ArrayList<Expression> list) {
			for (int i = 0, size = getSubSize(); i < size; ++i) {
				IParam sub = getSub(i);
				if (sub == null) {
					list.add(null);
				} else {
					sub.getAllLeafExpression(list);
				}
			}
		}
		
		/**
		 * ���ر��ʽ���飬ֻ֧�ֵ���Ĳ���
		 * @param function �������������׳��쳣
		 * @param canNull �����Ƿ�ɿ�
		 * @return
		 */
		public Expression[] toArray(String function, boolean canNull) {
			int size = getSubSize();
			Expression []exps = new Expression[size];
			for (int i = 0; i < size; ++i) {
				IParam sub = getSub(i);
				if (sub != null) {
					if (sub.isLeaf()) {
						exps[i] = sub.getLeafExpression();
					} else {
						MessageManager mm = EngineMessage.get();
						throw new RQException(function + mm.getMessage("function.invalidParam"));
					}
				} else if (!canNull) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(function + mm.getMessage("function.invalidParam"));
				}
			}
			
			return exps;
		}
		
		/**
		 * ���ر��ʽ�ַ������飬ֻ֧�ֵ���Ĳ���
		 * @param function �������������׳��쳣
		 * @param canNull �����Ƿ�ɿ�
		 * @return ���ʽ������
		 */
		public String []toStringArray(String function, boolean canNull) {
			int size = getSubSize();
			String []expStrs = new String[size];
			for (int i = 0; i < size; ++i) {
				IParam sub = getSub(i);
				if (sub != null) {
					if (sub.isLeaf()) {
						expStrs[i] = sub.getLeafExpression().toString();
					} else {
						MessageManager mm = EngineMessage.get();
						throw new RQException(function + mm.getMessage("function.invalidParam"));
					}
				} else if (!canNull) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(function + mm.getMessage("function.invalidParam"));
				}
			}
			
			return expStrs;
		}

		public IParam create(int start, int end) {
			if (end == start + 1) {
				return getSub(start);
			}

			SymbolParam param = new SymbolParam(level);
			for (; start < end; ++start) {
				param.paramList.add(paramList.get(start));
			}
			
			return param;
		}

		void addSub(IParam param) {
			paramList.add(param);
		}

		/**
		 * �����Ƿ����ָ������
		 * @param name String
		 * @return boolean
		 */
		public boolean containParam(String name) {
			for (int i = 0, size = getSubSize(); i < size; ++i) {
				IParam sub = getSub(i);
				if (sub != null && sub.containParam(name)) return true;
			}
			return false;
		}

		public void getUsedParams(Context ctx, ParamList resultList) {
			for (int i = 0, size = getSubSize(); i < size; ++i) {
				IParam sub = getSub(i);
				if (sub != null) sub.getUsedParams(ctx, resultList);
			}
		}
		
		public void getUsedFields(Context ctx, List<String> resultList) {
			for (int i = 0, size = getSubSize(); i < size; ++i) {
				IParam sub = getSub(i);
				if (sub != null) sub.getUsedFields(ctx, resultList);
			}
		}
		
		public void getUsedCells(List<INormalCell> resultList) {
			for (int i = 0, size = getSubSize(); i < size; ++i) {
				IParam sub = getSub(i);
				if (sub != null) sub.getUsedCells(resultList);
			}
		}
		
		/**
		 * ���ñ��ʽ�����ڱ��ʽ���棬���ִ��ʹ�ò�ͬ�������ģ�������������йصĻ�����Ϣ
		 */
		public void reset() {
			for (int i = 0, size = getSubSize(); i < size; ++i) {
				IParam sub = getSub(i);
				if (sub != null) sub.reset();
			}
		}

		public boolean optimize(Context ctx) {
			boolean opt = true;
			for (int i = 0, size = getSubSize(); i < size; ++i) {
				IParam sub = getSub(i);
				if (sub != null && !sub.optimize(ctx)) {
					opt = false;
				}
			}

			return opt;
		}
		
		/**
		 * �ж��Ƿ���Լ���ȫ����ֵ���и�ֵ����ʱֻ��һ���м���
		 * @return
		 */
		public boolean canCalculateAll() {
			for (int i = 0, size = getSubSize(); i < size; ++i) {
				IParam sub = getSub(i);
				if (sub != null && !sub.canCalculateAll()) {
					return false;
				}
			}
			
			return true;
		}
	}

	// Ҷ�ӽڵ�
	private static class LeafParam implements IParam {
		private Expression exp;

		public LeafParam(Expression exp) {
			this.exp = exp;
		}

		public boolean isLeaf() {
			return true;
		}

		public int getSubSize() {
			return 0;
		}

		public IParam getSub(int index) {
			throw new RuntimeException();
		}

		public char getType() {
			return Normal;
		}

		public Expression getLeafExpression() {
			return exp;
		}

		public void getAllLeafExpression(ArrayList<Expression> list) {
			list.add(exp);
		}
		
		/**
		 * ���ر��ʽ���飬ֻ֧�ֵ���Ĳ���
		 * @param function �������������׳��쳣
		 * @param canNull �����Ƿ�ɿ�
		 * @return
		 */
		public Expression[] toArray(String function, boolean canNull) {
			return new Expression[]{exp};
		}
		
		/**
		 * ���ر��ʽ�ַ������飬ֻ֧�ֵ���Ĳ���
		 * @param function �������������׳��쳣
		 * @param canNull �����Ƿ�ɿ�
		 * @return ���ʽ������
		 */
		public String []toStringArray(String function, boolean canNull) {
			return new String[]{exp.toString()};
		}

		public IParam create(int start, int end) {
			return start > 0 ? this : null;
		}

		/**
		 * �����Ƿ����ָ������
		 * @param name String
		 * @return boolean
		 */
		public boolean containParam(String name) {
			return exp.containParam(name);
		}

		public void getUsedParams(Context ctx, ParamList resultList) {
			exp.getUsedParams(ctx, resultList);
		}
		
		public void getUsedFields(Context ctx, List<String> resultList) {
			exp.getUsedFields(ctx, resultList);
		}
		
		public void getUsedCells(List<INormalCell> resultList) {
			exp.getUsedCells(resultList);
		}
		
		/**
		 * ���ñ��ʽ�����ڱ��ʽ���棬���ִ��ʹ�ò�ͬ�������ģ�������������йصĻ�����Ϣ
		 */
		public void reset() {
			exp.reset();
		}

		public boolean optimize(Context ctx) {
			exp.optimize(ctx);
			Node home = exp.getHome();
			return home instanceof Constant;
		}
		
		/**
		 * �ж��Ƿ���Լ���ȫ����ֵ���и�ֵ����ʱֻ��һ���м���
		 * @return
		 */
		public boolean canCalculateAll() {
			return exp.canCalculateAll();
		}
	}

	/**
	 * ����һ��Ҷ�ӽڵ����
	 * @param paramStr
	 * @param cs
	 * @param ctx
	 * @return
	 */
	public static IParam newLeafParam(String paramStr, ICellSet cs, Context ctx) {
		if (paramStr == null) {
			return null;
		}
		
		paramStr = paramStr.trim();
		if (paramStr.length() == 0) {
			return null;
		}
		
		return new LeafParam(new Expression(cs, ctx, paramStr, Expression.DoOptimize, false));
	}
	
	/**
	 * ������β��������ظ��ڵ㣬�������滻
	 * @param cs �������
	 * @param ctx ����������
	 * @param paramStr ������
	 * @return IParam �������ڵ㣬û�в�����Ϊ��
	 */
	public static IParam parse(String paramStr, ICellSet cs, Context ctx) {
		return parse(paramStr, cs, ctx, IParam.NONE, false, Expression.DoOptimize);
	}

	/**
	 * ������β��������ظ��ڵ�
	 * @param cs �������
	 * @param ctx ����������
	 * @param paramStr ������
	 * @param doMacro �Ƿ������滻��true������false������
	 * @return IParam �������ڵ㣬û�в�����Ϊ��
	 */
	public static IParam parse(String paramStr, ICellSet cs, Context ctx, boolean doMacro) {
		return parse(paramStr, cs, ctx, IParam.NONE, doMacro, Expression.DoOptimize);
	}

	/**
	 * ������β��������ظ��ڵ�
	 * @param paramStr ������
	 * @param cs �������
	 * @param ctx ����������
	 * @param doMacro �Ƿ������滻��true������false������
	 * @param doOpt �Ƿ����Ż���true������false������
	 * @return IParam �������ڵ㣬û�в�����Ϊ��
	 */
	public static IParam parse(String paramStr, ICellSet cs, Context ctx, boolean doMacro, boolean doOpt) {
		return parse(paramStr, cs, ctx, IParam.NONE, doMacro, doOpt);
	}

	private static IParam parse(String paramStr, ICellSet cs, Context ctx, char prevLevel, boolean doMacro, boolean doOpt) {
		if (paramStr == null) {
			return null;
		}
		
		paramStr = paramStr.trim();
		if (paramStr.length() == 0) {
			return null;
		}

		// ð�ŷָ�����ֻ����Ҷ�ӽڵ�
		if (prevLevel == IParam.Colon) {
			return new LeafParam(new Expression(cs, ctx, paramStr, doOpt, doMacro));
		}

		// �ҵ��������д��ڵķָ���
		char level = getNextLevel(prevLevel);
		while (!hasSeparator(paramStr, level)) {
			// ð�ŷָ�����ֻ����Ҷ�ӽڵ�
			if (level == IParam.Colon) {
				return new LeafParam(new Expression(cs, ctx, paramStr, doOpt, doMacro));
			} else {
				level = getNextLevel(level);
			}
		}

		// ���ɷָ����ڵ㣬Ȼ��ݹ�����ӽڵ�
		SymbolParam param = new SymbolParam(level);
		ArgumentTokenizer arg = new ArgumentTokenizer(paramStr, level);
		while (arg.hasMoreElements()) {
			param.addSub(parse(arg.nextToken(), cs, ctx, level, doMacro, doOpt));
		}

		return param;
	}

	// ȡ�ָ�������һ��ָ���
	private static char getNextLevel(char prevLevel) {
		switch (prevLevel) {
		case IParam.NONE:
			return IParam.Semicolon;
		case IParam.Semicolon:
			return IParam.Comma;
		case IParam.Comma:
			return IParam.Colon;
		default:
			throw new RQException();
		}
	}

	// �жϲ��������Ƿ����ָ���ָ���
	private static boolean hasSeparator(String str, char separator) {
		int len = str.length();
		int index = 0;
		
		while (index < len) {
			// ��Ҫ�������š����ź�ת���
			char ch = str.charAt(index);

			if (ch == separator) {
				return true;
			} if ( ch == '\\' ) {
				index += 2;
			} else if ( ch == '\"' || ch == '\'' ) {
				int tmp = Sentence.scanQuotation(str, index);
				if (tmp < 0) {
					return false;
				} else {
					index = tmp + 1;
				}
			} else if (ch == '(' ) {
				int tmp = Expression.scanParenthesis(str, index);
				if (tmp < 0) {
					return false;
				} else {
					index = tmp + 1;
				}
			} else if (ch == '[') {
				int tmp = Sentence.scanBracket(str, index);
				if (tmp < 0) {
					return false;
				} else {
					index = tmp + 1;
				}
			} else if (ch == '{') {
				int tmp = Sentence.scanBrace(str, index);
				if (tmp < 0) {
					return false;
				} else {
					index = tmp + 1;
				}
			} else {
				index++;
			}
		}

		return false;
	}
}
