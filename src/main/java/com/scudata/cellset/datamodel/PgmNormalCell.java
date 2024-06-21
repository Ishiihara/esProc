package com.scudata.cellset.datamodel;

import java.lang.ref.WeakReference;
import java.util.List;

import com.scudata.cellset.INormalCell;
import com.scudata.dm.Context;
import com.scudata.dm.DBObject;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.KeyWord;
import com.scudata.dm.ParamList;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.util.Variant;

/**
 * ��������Ԫ�����
 * @author WangXiaoJun
 *
 */
public class PgmNormalCell extends NormalCell {
	private static final long serialVersionUID = 0x02010014;

	transient protected WeakReference<Expression> expRef; // ���ʽ�����ã�Ϊ�˻�����ʽ
	transient private int sign = TYPE_BLANK_CELL; // ��Ԫ������
	transient private Command command; // ����˵�Ԫ��Ϊ������Ӧ��Ӧ����䣬����Ϊ��
	transient private boolean containMacro = false;;

	// ����ʱʹ��
	public PgmNormalCell() {
	}

	public PgmNormalCell(CellSet cs, int r, int c) {
		super(cs, r, c);
	}

	public int getType() {
		return sign;
	}

	public void reset() {
		if ((sign & TYPE_CONST_CELL) != 0) {
			// ����ֵ���ܱ��޸���
			if (expStr.startsWith(KeyWord.CONSTSTRINGPREFIX)) { // �ַ�������'
				value = expStr.substring(1);
			} else {
				value = Variant.parse(expStr, false);
			}
		} else {
			value = null;
			expRef = null;
			command = null;
		}
	}

	/**
	 * ���õ�Ԫ����ʽ
	 * @param exp String
	 */
	public void setExpString(String exp) {
		if (exp != null && exp.equals(this.expStr)) {
			return;
		}

		this.expStr = exp;
		value = null;
		expRef = null;
		command = null;

		if (exp != null && exp.length() > 0) {
			if (exp.startsWith("==")) { // �����
				sign = TYPE_CALCULABLE_BLOCK;
				containMacro = Expression.containMacro(exp);
			} else if (exp.startsWith("=")) { // �����
				sign = TYPE_CALCULABLE_CELL;
				containMacro = Expression.containMacro(exp);
			} else if (exp.startsWith(">>")) { // ִ�п�
				sign = TYPE_EXECUTABLE_BLOCK;
				containMacro = Expression.containMacro(exp);
			} else if (exp.startsWith(">")) { // ִ�и�
				sign = TYPE_EXECUTABLE_CELL;
				containMacro = Expression.containMacro(exp);
			} else if (exp.startsWith("//")) { // ע�Ϳ�
				sign = TYPE_NOTE_BLOCK;
				//value = exp.substring(2);
			} else if (exp.startsWith("/")) { // ע�͸�
				sign = TYPE_NOTE_CELL;
				//value = exp.substring(1);
			} else if (Command.isCommand(exp)) { // ����
				sign = TYPE_COMMAND_CELL;
				containMacro = Expression.containMacro(exp);
			} else {
				value = parseConstValue(exp);
				sign = TYPE_CONST_CELL;
			}
		} else {
			sign = TYPE_BLANK_CELL;
		}
	}

	public static Object parseConstValue(String str) {
		if (str.startsWith(KeyWord.CONSTSTRINGPREFIX)) { // �ַ�������'
			return str.substring(1);
		} else {
			return Variant.parse(str, false);
		}
	}
	
	// Ԥ����$(c)
	public String getMacroReplaceString() {
		String exp = expStr;
		if (exp == null) return null;
		int len = exp.length();
		if (len == 0) return null;

		int c1 = exp.charAt(0);
		if (c1 == '=') {
			char c2 = len > 1 ? exp.charAt(1) : 0;
			if (c2 == '=') { // ==
				return getCellId() + exp.substring(1);
			} else {
				return getCellId() + exp;
			}
		} else if (c1 == '>') {
			if (len > 1 && exp.charAt(1) == '>') {
				return exp.substring(2);
			} else {
				return expStr.substring(1);
			}
		} else if (c1 == '/') {
			return null;
		} else {
			return exp;
		}
	}

	public Object getValue(boolean doCalc) {
		return getValue();
	}

	/**
	 * ����˵�Ԫ��
	 */
	public void calculate(){
		Context ctx = cs.getContext();
		if ((sign & TYPE_CALCULABLE_CELL) != 0) { // =
			Expression exp;
			if (expRef == null || (exp = expRef.get()) == null) {
				cs.setParseCurrent(row, col);
				exp = new Expression(cs, ctx, expStr.substring(1) + getSubExpString());
				
				if (!containMacro()) {
					expRef = new WeakReference<Expression>(exp);
				}
			}

			//value = null; // ���ͷ�֮ǰ��ֵ������ѭ�������ͷ��ڴ�
			value = exp.calculate(ctx);
		} else if ((sign & TYPE_CALCULABLE_BLOCK) != 0) { // ==
			Expression exp;
			if (expRef == null || (exp = expRef.get()) == null) {
				cs.setParseCurrent(row, col);
				exp = new Expression(cs, ctx, expStr.substring(2) + getSubExpString());
				
				if (!containMacro()) {
					expRef = new WeakReference<Expression>(exp);
				}
			}

			//value = null; // ���ͷ�֮ǰ��ֵ������ѭ�������ͷ��ڴ�
			value = exp.calculate(ctx);
		} else if ((sign & TYPE_EXECUTABLE_CELL) != 0) { // >
			Expression exp;
			if (expRef == null || (exp = expRef.get()) == null) {
				cs.setParseCurrent(row, col);
				if (expStr.charAt(0) == '>') {
					exp = new Expression(cs, ctx, expStr.substring(1) + getSubExpString());
				} else {
					exp = new Expression(cs, ctx, expStr + getSubExpString());
				}

				if (!containMacro()) {
					expRef = new WeakReference<Expression>(exp);
				}
			}

			exp.calculate(ctx);
		} else if ((sign & TYPE_EXECUTABLE_BLOCK) != 0) { // >>
			Expression exp;
			if (expRef == null || (exp = expRef.get()) == null) {
				cs.setParseCurrent(row, col);
				exp = new Expression(cs, ctx, expStr.substring(2) + getSubExpString());
				
				if (!containMacro()) {
					expRef = new WeakReference<Expression>(exp);
				}
			}

			exp.calculate(ctx);
		}
	}

	private String getSubExpString() {
		char lastChar = expStr.charAt(expStr.length() - 1);
		if (lastChar != ',' && lastChar != ';' && lastChar != '(') return "";

		StringBuffer sb = new StringBuffer(256);
		PgmCellSet pcs = (PgmCellSet)cs;
		int endRow = pcs.getCodeBlockEndRow(row, col);
		int colCount = cs.getColCount();
		int startCol = col + 1;

		Next:
		for (int r = row; r <= endRow; ++r) {
			for (int c = startCol; c <= colCount; ++c) {
				PgmNormalCell cell = pcs.getPgmNormalCell(r, c);
				if (cell == null || cell.isNoteCell()) {
					// ����ע�͸�
				} else if (cell.isNoteBlock()) {
					// ����ע�Ϳ�
					r = pcs.getCodeBlockEndRow(r, c);
					break;
				} else {
					String subStr = cell.getExpString();
					if (subStr != null && subStr.length() > 0) {
						sb.append(subStr);
						lastChar = subStr.charAt(subStr.length() - 1);
						if (lastChar != ',' && lastChar != ';' && lastChar != '(') {
							break Next;
						}
					}
				}
			}
		}

		return sb.toString();
	}

	public Expression getExpression() {
		Expression exp = null;
		if (expRef != null && (exp = expRef.get()) != null) return exp;

		Context ctx = cs.getContext();
		if ((sign & TYPE_CALCULABLE_CELL) != 0) { // =
			cs.setParseCurrent(row, col);
			exp = new Expression(cs, ctx, expStr.substring(1) + getSubExpString());
			if (!containMacro()) {
				expRef = new WeakReference<Expression>(exp);
			}
		} else if ((sign & TYPE_CALCULABLE_BLOCK) != 0) { // ==
			cs.setParseCurrent(row, col);
			exp = new Expression(cs, ctx, expStr.substring(2) + getSubExpString());
			if (!containMacro()) {
				expRef = new WeakReference<Expression>(exp);
			}
		} else if ((sign & TYPE_EXECUTABLE_CELL) != 0) { // >
			cs.setParseCurrent(row, col);
			if (expStr.charAt(0) == '>') {
				exp = new Expression(cs, ctx, expStr.substring(1) + getSubExpString());
			} else {
				exp = new Expression(cs, ctx, expStr + getSubExpString());
			}
			
			if (!containMacro()) {
				expRef = new WeakReference<Expression>(exp);
			}
		} else if ((sign & TYPE_EXECUTABLE_BLOCK) != 0) { // >>
			cs.setParseCurrent(row, col);
			exp = new Expression(cs, ctx, expStr.substring(2) + getSubExpString());
			if (!containMacro()) {
				expRef = new WeakReference<Expression>(exp);
			}
		}

		return exp;
	}

	/**
	 * ���ص�Ԫ������Ӧ��������
	 * @return Command
	 */
	public Command getCommand() {
		if (command == null && (sign & TYPE_COMMAND_CELL) != 0) {
			if (containMacro()) {
				return Command.parse(expStr);
			} else {
				command = Command.parse(expStr);
			}
		}

		return command;
	}

	/**
	 * �����Ƿ�����
	 * @return boolean
	 */
	public boolean isCommandCell() {
		return (sign & TYPE_COMMAND_CELL) != 0;
	}

	/**
	 * �����Ƿ�����
	 * @return boolean
	 */
	public boolean isResultCell() {
		return isCommandCell() && Command.isResultCommand(expStr);
	}

	// ���ص�Ԫ���Ƿ���Ҫ����
	public boolean needCalculate() {
		int tmp = TYPE_CALCULABLE_CELL | TYPE_CALCULABLE_BLOCK |
			TYPE_EXECUTABLE_CELL | TYPE_EXECUTABLE_BLOCK | TYPE_COMMAND_CELL;
		return (sign & tmp) != 0;
	}

	/**
	 * ���ص�Ԫ���Ƿ��ǿհ׸񣬼�û�б��ʽ
	 * @return boolean
	 */
	public boolean isBlankCell() {
		return (sign & TYPE_BLANK_CELL) != 0;
	}

	// ���ʽ���Ƿ�������滻s
	private boolean containMacro() {
		return containMacro;
	}
	
	// ���ص�Ԫ���Ƿ��ǳ�����
	public boolean isConstCell() {
		return (sign & TYPE_CONST_CELL) != 0;
	}

	/**
	 * ���ص�Ԫ���Ƿ���ע�͸�
	 * @return boolean
	 */
	public boolean isNoteCell() {
		return (sign & TYPE_NOTE_CELL) != 0;
	}

	/**
	 * ���ص�Ԫ���Ƿ���ע�Ϳ�
	 * @return boolean
	 */
	public boolean isNoteBlock() {
		return (sign & TYPE_NOTE_BLOCK) != 0;
	}

	// �����Ƿ��Ǽ����
	public boolean isCalculableBlock() {
		return (sign & TYPE_CALCULABLE_BLOCK) != 0;
	}

	// �����Ƿ��Ǽ����
	public boolean isCalculableCell() {
		return (sign & TYPE_CALCULABLE_CELL) != 0;
	}

	// �����Ƿ���ִ�п�
	public boolean isExecutableBlock() {
		return (sign & TYPE_EXECUTABLE_BLOCK) != 0;
	}

	public Object deepClone(){
		PgmNormalCell cell = new PgmNormalCell(cs, row, col);
		cell.setExpString(expStr);
		cell.tip = tip;
		cell.value = value;
		return cell;
	}

	public boolean needRegulateString() {
		return (sign & TYPE_CONST_CELL) == 0 && (sign & TYPE_BLANK_CELL) == 0 && 
				((sign & TYPE_NOTE_CELL) == 0 && (sign & TYPE_NOTE_BLOCK) == 0 || Env.isAdjustNoteCell());
	}

	public byte calcExpValueType(Context ctx) {
		Object val = getValue();
		if (val == null) {
			Expression exp = getExpression();
			return exp == null ? Expression.TYPE_OTHER : exp.getExpValueType(ctx);
		} else {
			if (val instanceof DBObject) {
				return Expression.TYPE_DB;
			} else if (val instanceof FileObject) {
				return Expression.TYPE_FILE;
			} else {
				return Expression.TYPE_OTHER;
			}
		}
	}

	public void getUsedParamsAndCells(ParamList usedParams, List<INormalCell> usedCells) {
		Context ctx = cs.getContext();
		Expression exp;
		
		if ((sign & TYPE_CALCULABLE_CELL) != 0) { // =
			cs.setParseCurrent(row, col);
			exp = new Expression(cs, ctx, expStr.substring(1) + getSubExpString());
		} else if ((sign & TYPE_CALCULABLE_BLOCK) != 0) { // ==
			cs.setParseCurrent(row, col);
			exp = new Expression(cs, ctx, expStr.substring(2) + getSubExpString());
		} else if ((sign & TYPE_EXECUTABLE_CELL) != 0) { // >
			cs.setParseCurrent(row, col);
			if (expStr.charAt(0) == '>') {
				exp = new Expression(cs, ctx, expStr.substring(1) + getSubExpString());
			} else {
				exp = new Expression(cs, ctx, expStr + getSubExpString());
			}
		} else if ((sign & TYPE_EXECUTABLE_BLOCK) != 0) { // >>
			cs.setParseCurrent(row, col);
			exp = new Expression(cs, ctx, expStr.substring(2) + getSubExpString());
		} else {
			Command cmd = getCommand();
			if (cmd != null) {
				IParam param = cmd.getParam(cs, ctx);
				if (param != null) {
					param.getUsedParams(ctx, usedParams);
					param.getUsedCells(usedCells);
				}
			}
			
			return;
		}
		
		exp.getUsedParams(ctx, usedParams);
		exp.getUsedCells(usedCells);
	}
	
	/**
	 * ȡ��ǰ��Ԫ�����õ��ĵ�Ԫ��
	 * @param resultList
	 */
	public void getUsedCells(List<INormalCell> resultList) {
		Context ctx = cs.getContext();
		Expression exp;
		
		if ((sign & TYPE_CALCULABLE_CELL) != 0) { // =
			cs.setParseCurrent(row, col);
			exp = new Expression(cs, ctx, expStr.substring(1) + getSubExpString());
		} else if ((sign & TYPE_CALCULABLE_BLOCK) != 0) { // ==
			cs.setParseCurrent(row, col);
			exp = new Expression(cs, ctx, expStr.substring(2) + getSubExpString());
		} else if ((sign & TYPE_EXECUTABLE_CELL) != 0) { // >
			cs.setParseCurrent(row, col);
			if (expStr.charAt(0) == '>') {
				exp = new Expression(cs, ctx, expStr.substring(1) + getSubExpString());
			} else {
				exp = new Expression(cs, ctx, expStr + getSubExpString());
			}
		} else if ((sign & TYPE_EXECUTABLE_BLOCK) != 0) { // >>
			cs.setParseCurrent(row, col);
			exp = new Expression(cs, ctx, expStr.substring(2) + getSubExpString());
		} else {
			Command cmd = getCommand();
			if (cmd != null) {
				cmd.getUsedCells(cs, ctx, resultList);
			}
			
			return;
		}
		
		exp.getUsedCells(resultList);
	}
	
	/**
	 * �����Ԫ��ֵ�ͼ�����ʽ
	 */
	public void clear() {
		value = null;
		expRef = null;
		command = null;
	}
}
