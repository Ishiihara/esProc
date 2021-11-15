package com.raqsoft.expression.mfn.sequence;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.cursor.ICursor;
import com.raqsoft.dm.cursor.IMultipath;
import com.raqsoft.dm.cursor.MultipathCursors;
import com.raqsoft.dm.op.Operation;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.IParam;
import com.raqsoft.expression.SequenceFunction;
import com.raqsoft.resources.EngineMessage;
import com.raqsoft.util.CursorUtil;

/**
 * ���еĳ�Ա���α꣬���Ұ�ָ������ʽ���򣬶��α��������鲢�ϲ���һ�����α�
 * CS.merge(xi,��)
 * @author RunQian
 *
 */
public class Mergex extends SequenceFunction {
	public Object calculate(Context ctx) {
		int srcLen = srcSequence.length();
		int count = srcLen;
		for (int i = 1; i <= srcLen; ++i) {
			Object obj = srcSequence.getMem(i);
			if (obj == null) {
				count--;
			} else if (!(obj instanceof ICursor)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("\".\"" + mm.getMessage("dot.cursorLeft"));
			}
		}
		
		if (count < 1) {
			return null;
		} else if (count == 1) {
			return srcSequence.ifn();
		}

		ICursor []cursors = new ICursor[count];
		boolean isMultipath = false;
		int pathCount = 1;

		for (int i = 1, q = 0; i <= srcLen; ++i) {
			Object obj = srcSequence.get(i);
			if (obj != null) {
				ICursor cursor = (ICursor)obj;
				cursors[q++] = cursor;
				
				if (cursor instanceof IMultipath) {
					if (q == 1) {
						isMultipath = true;
						pathCount = ((IMultipath)cursor).getPathCount();
					} else if (pathCount != ((IMultipath)cursor).getPathCount()) {
						isMultipath = false;
					}
				}
			}
		}

		Expression []exps = null;
		if (param == null) {
		} else if (param.isLeaf()) { // ֻ��һ������
			exps = new Expression[]{ param.getLeafExpression() };
		} else if (param.getType() == IParam.Comma) { // ,
			int size = param.getSubSize();
			exps = new Expression[size];
			for (int i = 0; i < size; ++i) {
				IParam sub = param.getSub(i);
				if (sub == null || !sub.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("mergex" + mm.getMessage("function.invalidParam"));
				}
				exps[i] = sub.getLeafExpression();
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("mergex" + mm.getMessage("function.invalidParam"));
		}

		if (isMultipath && pathCount > 1) {
			ICursor []result = new ICursor[pathCount];
			ICursor [][]multiCursors = new ICursor[count][];
			for (int i = 0; i < count; ++i) {
				IMultipath multipath = (IMultipath)cursors[i];
				multiCursors[i] = multipath.getParallelCursors();
			}
			
			for (int i = 0; i < pathCount; ++i) {
				ICursor []curs = new ICursor[count];
				for (int c = 0; c < count; ++c) {
					curs[c] = multiCursors[c][i];
				}

				Context tmpCtx = ctx.newComputeContext();
				Expression []tmpExps = Operation.dupExpressions(exps, tmpCtx);
				result[i] = CursorUtil.merge(curs, tmpExps, option, tmpCtx);
			}
			
			return new MultipathCursors(result, ctx);
		} else {
			return CursorUtil.merge(cursors, exps, option, ctx);
		}
	}
}