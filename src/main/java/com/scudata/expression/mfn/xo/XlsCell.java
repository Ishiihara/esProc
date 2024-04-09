package com.scudata.expression.mfn.xo;

import com.scudata.common.CellLocation;
import com.scudata.common.Escape;
import com.scudata.common.Matrix;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.StringUtils;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.excel.ExcelUtils;
import com.scudata.excel.XlsFileObject;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.expression.XOFunction;
import com.scudata.resources.AppMessage;
import com.scudata.resources.EngineMessage;

/**
 * ����xo.xlscell(a:b,s;t)����ҳs�ĸ�a�����봮t��t�ǻس�/tab�ָ��Ĵ������е����У��ֱ����������к����У�sʡ��Ϊ��һҳ��xo������@r@w��ʽ��
 * ��t����ʱ��������a��b�ĸ��ı���ʽ�����أ�ֻ��aֻ��a��a:������β��a:bʡ�Ա�ʾ��ҳs������Ϊt��
 * 
 * @i �в���ʽ���룬ȱʡ�Ǹ���ʽ
 * @w ����ʱ���سɸ�ֵ�����е����С�
 * @p @w��ת�ã����е��������к��У��Ǵ�ʱ����
 * @n ȡ��������ʱ��trim��@wʱ�ѿմ�����null
 * @g ����������ͼƬ����:b������t�Ǹ�blob��Ŀǰ֧��jpg/png��ʽ
 * 
 *
 */
public class XlsCell extends XOFunction {
	/**
	 * ����
	 */
	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlscell"
					+ mm.getMessage("function.missingParam"));
		}

		IParam sheetParam;
		Object content = null; // �����Ǵ�Ҳ������ͼƬ
		if (param.getType() == IParam.Semicolon) {
			if (param.getSubSize() != 2) { // ����һ��֮ǰ��
				MessageManager mm = EngineMessage.get();
				throw new RQException("xlscell"
						+ mm.getMessage("function.invalidParam"));
			}
			sheetParam = param.getSub(0);
			IParam contentParam = param.getSub(1);
			if (contentParam != null) {
				content = contentParam.getLeafExpression().calculate(ctx);
			}
		} else {
			sheetParam = param;
		}
		IParam param0;
		IParam param1;
		if (sheetParam.isLeaf() || sheetParam.getType() == IParam.Colon) { // ʡ��s
			param0 = sheetParam;
			param1 = null;
		} else if (sheetParam.getType() != IParam.Comma
				|| sheetParam.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlscell"
					+ mm.getMessage("function.invalidParam"));
		} else {
			param0 = sheetParam.getSub(0);
			param1 = sheetParam.getSub(1);
		}
		Object s = null;
		if (param1 != null) {
			s = param1.getLeafExpression().calculate(ctx);
		} else {
			s = 1;
		}

		if (param0 == null) {
			// û��a:b,��sheet����
			if (!StringUtils.isValidString(content)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("xlscell"
						+ mm.getMessage("function.invalidParam"));
			}

			// ���sheet����
			ExcelUtils.checkSheetName(content);

			try {
				file.rename(s, (String) content);
			} catch (Exception e) {
				throw new RQException(e.getMessage(), e);
			}
			return null;
		}

		if (param0 == null || (param1 != null && !param1.isLeaf())) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("xlscell"
					+ mm.getMessage("function.invalidParam"));
		}

		String cell1, cell2;
		if (param0.isLeaf()) {
			Object val = param0.getLeafExpression().calculate(ctx); // .toString();
			if (!(val instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("xlscell"
						+ mm.getMessage("function.paramTypeError"));
			}

			cell1 = (String) val;
			cell1 = removeQuota(cell1);
			cell2 = cell1; // :b��û�У���ʾa������
		} else {
			IParam sub = param0.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("xlscell"
						+ mm.getMessage("function.invalidParam"));
			}

			Object val = sub.getLeafExpression().calculate(ctx); // .toString();
			if (!(val instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("xlscell"
						+ mm.getMessage("function.paramTypeError"));
			}

			cell1 = (String) val;
			cell1 = removeQuota(cell1);
			sub = param0.getSub(1);
			if (sub != null) {
				val = sub.getLeafExpression().calculate(ctx); // .toString();
				if (!(val instanceof String)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xlscell"
							+ mm.getMessage("function.paramTypeError"));
				}
				cell2 = (String) val;
				cell2 = removeQuota(cell2);
			} else {
				cell2 = null; // a:����ʾȡ����β
			}
		}
		String opt = option;
		boolean isRowInsert = opt != null && opt.indexOf('i') != -1;
		boolean isGraph = opt != null && opt.indexOf('g') != -1;
		boolean isW = opt != null && opt.indexOf('w') != -1;
		boolean isP = opt != null && opt.indexOf("p") > -1;
		boolean isN = opt != null && opt.indexOf("n") > -1;
		if (!isW) {
			if (isP) {
				// ѡ��@{0}ֻ�ܺ�ѡ��@wͬʱʹ�á�
				throw new RQException(AppMessage.get().getMessage(
						"xlsimport.pnnotw", "p"));
			}
		}
		Object matrix;
		if (content == null || "".equals(content.toString().trim())) { // ���t�ǿյģ�����Ϊ��c��Ϊ��
			matrix = null;
		} else {
			if (isGraph) {
				if (!(content instanceof byte[])) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("xlscell"
							+ mm.getMessage("function.paramTypeError"));
				}
				matrix = content;
			} else if (content instanceof Sequence) { // ���е�����
				matrix = content;
			} else if (content instanceof BaseRecord) {
				Sequence seq = new Sequence();
				seq.add(content);
				matrix = seq;
			} else if (content instanceof String) {
				matrix = ExcelUtils.getStringMatrix((String) content, true);
			} else {
				Matrix m = new Matrix(1, 1);
				m.set(0, 0, content);
				matrix = m;
				// MessageManager mm = EngineMessage.get();
				// throw new RQException("xlscell"
				// + mm.getMessage("function.paramTypeError"));
			}
		}

		if (matrix != null)
			if (matrix instanceof Sequence) {
				if (isP) {
					matrix = ExcelUtils.transpose((Sequence) matrix);
				}
			}

		CellLocation pos1 = CellLocation.parse(cell1);
		if (pos1 == null) {
			throw new RQException(AppMessage.get().getMessage(
					"excel.invalidcell", cell1));
		}
		CellLocation pos2 = null;
		if (StringUtils.isValidString(cell2)) {
			pos2 = CellLocation.parse(cell2);
			if (pos2 == null) {
				throw new RQException(AppMessage.get().getMessage(
						"excel.invalidcell", cell1));
			}
		}
		if (isGraph) {
			if (pos2 != null) {
				if (pos2.getRow() != pos1.getRow()
						|| pos2.getCol() != pos1.getCol())
					throw new RQException(AppMessage.get().getMessage(
							"excel.graphwithb"));
			}
		}

		if (file.getFileType() != XlsFileObject.TYPE_NORMAL) {
			// : xlsopen@r or @w does not support xlscell
			throw new RQException("xlscell"
					+ AppMessage.get().getMessage("filexls.rwcell"));
		}

		try {
			return file.xlscell(pos1, pos2, s, matrix, isRowInsert, isGraph,
					isW, isP, isN);
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}

	}

	/**
	 * �Խڵ����Ż�
	 * @param ctx ����������
	 * @param Node �Ż���Ľڵ�
	 */
	public Node optimize(Context ctx) {
		if (param != null) {
			// �Բ������Ż�
			param.optimize(ctx);
		}

		return this;
	}

	/**
	 * ɾ�����ӵ�����
	 * 
	 * @param cell
	 * @return
	 */
	private String removeQuota(String cell) {
		if (cell == null)
			return null;
		cell = Escape.removeEscAndQuote(cell, '\'');
		return Escape.removeEscAndQuote(cell, '\"');
	}
}
