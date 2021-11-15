package com.raqsoft.expression.mfn.cursor;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.cursor.ICursor;
import com.raqsoft.dm.cursor.IMultipath;
import com.raqsoft.expression.CursorFunction;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.IParam;
import com.raqsoft.resources.EngineMessage;
import com.raqsoft.util.CursorUtil;

/**
 * �Ѷ�·�α갴ָ������ʽ�鲢�ɵ�·�α꣬ÿһ·�α궼������ʽ����
 * mcs.mergex(xi,��)
 * @author RunQian
 *
 */
public class Mergex extends CursorFunction {
	public Object calculate(Context ctx) {
		if (!(cursor instanceof IMultipath)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\".\"" + mm.getMessage("dot.seriesLeft"));
		}

		ICursor []cursors = ((IMultipath)cursor).getParallelCursors();
		if (cursors.length == 1) {
			return cursors[0];
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
		
		return CursorUtil.merge(cursors, exps, option, ctx);
	}
}