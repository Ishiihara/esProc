package com.raqsoft.expression.mfn.vdb;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.VSFunction;
import com.raqsoft.resources.EngineMessage;

/**
 * ��������·����
 * h.rename(p,F)
 * @author RunQian
 *
 */
public class Rename extends VSFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rename" + mm.getMessage("function.missingParam"));
		} else if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rename" + mm.getMessage("function.invalidParam"));
		}
		
		Expression []exps = param.toArray("rename", false);
		Object path = exps[0].calculate(ctx);
		Object name = exps[1].calculate(ctx);
		if (!(name instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("rename" + mm.getMessage("function.paramTypeError"));
		}
		
		return vs.rename(path, (String)name);
	}
}