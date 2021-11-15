package com.raqsoft.expression.mfn.file;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.FileFunction;
import com.raqsoft.expression.IParam;
import com.raqsoft.resources.EngineMessage;

/**
 * ��ȡ�ļ����ݳ��ַ��������ֽ�����
 * f.read() f.read(b:e)
 * @author RunQian
 *
 */
public class Read extends FileFunction {
	public Object calculate(Context ctx) {
		long start = 0, end = -1;
		if (param == null) {
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (!(obj instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("read" + mm.getMessage("function.paramTypeError"));
			}

			start = ((Number)obj).longValue();
			if (start < 0) start = 0;
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("read" + mm.getMessage("function.invalidParam"));
			}

			IParam sub0 = param.getSub(0);
			if (sub0 != null) {
				Object obj = sub0.getLeafExpression().calculate(ctx);
				if (!(obj instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("read" + mm.getMessage("function.paramTypeError"));
				}

				start = ((Number)obj).longValue();
				if (start < 0) start = 0;
			}

			IParam sub1 = param.getSub(1);
			if (sub1 != null) {
				Object obj = sub1.getLeafExpression().calculate(ctx);
				if (!(obj instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("read" + mm.getMessage("function.paramTypeError"));
				}

				end = ((Number)obj).longValue();
			}
		}

		try {
			return file.read(start, end, option);
		} catch( Exception e) {
			throw new RQException(e.getMessage(), e);
		}
	}
}