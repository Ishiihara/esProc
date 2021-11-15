package com.raqsoft.expression.fn.convert;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.common.StringUtils;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Function;
import com.raqsoft.resources.EngineMessage;

/**
 * ������תΪ��������д����
 * @author runqian
 *
 */
public class ToChinese extends Function {
	public Object calculate(Context ctx) {
		if (param == null || !param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("chn" + mm.getMessage("function.invalidParam"));
		}

		Object result = param.getLeafExpression().calculate(ctx);
		if (result instanceof Number) {
			boolean abbreviate = false, uppercase = false, rmb = false;
			if (option != null) {
				if (option.indexOf('a') != -1) abbreviate = true;
				if (option.indexOf('u') != -1) uppercase = true;
				if (option.indexOf('b') != -1) rmb = true;
			}
	
			if (rmb) {
				double d = ((Number)result).doubleValue();
				return StringUtils.toRMB(d, abbreviate, uppercase);
			} else {
				long l = ((Number)result).longValue();
				return StringUtils.toChinese(l, abbreviate, uppercase);
			}
		} else if (result == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("chn" + mm.getMessage("function.paramTypeError"));
		}
	}
}