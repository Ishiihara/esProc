package com.raqsoft.expression.operator;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Operator;
import com.raqsoft.resources.EngineMessage;
import com.raqsoft.util.Variant;

/**
 * �������==
 * @author RunQian
 *
 */
public class Equals extends Operator {
	public Equals() {
		priority = PRI_EQ;
	}

	public Object calculate(Context ctx) {
		if (left == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"==\"" + mm.getMessage("operator.missingLeftOperation"));
		}
		
		if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"==\"" + mm.getMessage("operator.missingRightOperation"));
		}

		if (Variant.isEquals(left.calculate(ctx), right.calculate(ctx))) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
}