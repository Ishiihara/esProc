package com.raqsoft.expression.fn.algebra;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Sequence;
import com.raqsoft.expression.Function;
import com.raqsoft.resources.EngineMessage;

/**
 * norm(A)��������������Ĺ�һ��normalize����
 * @author bd
 *
 */
public class Normalize extends Function {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("norm" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object result1 = param.getLeafExpression().calculate(ctx);
			if (!(result1 instanceof Sequence)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("norm" + mm.getMessage("function.paramTypeError"));
			}
			boolean norm = option == null || option.indexOf('0')<0;
			Matrix A = normalize((Sequence) result1, norm);
			return A.toSequence(option, true);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("norm" + mm.getMessage("function.invalidParam"));
		}
	}
	
	protected Matrix normalize(Sequence result, boolean norm) {
		Matrix A = new Matrix(result);
		double[][] vs = A.getArray();
		int rows = A.getRows();
		int cols = A.getCols();
		for (int r = 0; r < rows; r++) {
			double[] row = vs[r];
			double avg = 0; 
			double sqrsum = 0;
			for (int c = 0; c < cols; c++) {
				avg += row[c];
				if (norm) {
					sqrsum += row[c]*row[c];
				}
			}
			avg = avg/cols;
			if (norm) {
				sqrsum = Math.sqrt(sqrsum);
			}
			for (int c = 0; c < cols; c++) {
				vs[r][c] = vs[r][c]-avg;
				if (norm && sqrsum!=0) {
					vs[r][c] = vs[r][c]/sqrsum;
				}
			}
		}
		return A;
	}
	
	protected static Vector normalize(Vector A) {
		double[] vs = A.getValue();
		int cols = A.len();
		double avg = 0;
		for (int c = 0; c < cols; c++) {
			avg += vs[c];
		}
		avg = avg/cols;
		for (int c = 0; c < cols; c++) {
			vs[c] = vs[c]-avg;
		}
		return A;
	}

}