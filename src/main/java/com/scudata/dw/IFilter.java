package com.scudata.dw;

import java.util.List;

import com.scudata.array.IArray;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;

/**
 * �ֶι��˱��ʽ
 * @author runqian
 *
 */
public abstract class IFilter implements Comparable<IFilter> {
	public static final int EQUAL = 1;
	public static final int GREATER = 2;
	public static final int GREATER_EQUAL = 3;
	public static final int LESS  = 4;
	public static final int LESS_EQUAL = 5;
	public static final int NOT_EQUAL = 6;
	
	public static final int AND = 10;
	public static final int OR = 11;
	
	protected ColumnMetaData column;
	protected int priority; // ���ȼ������������ȹ������ȼ��ߵ��ֶΣ�����ԽС���ȼ�Խ��
	protected String columnName;//�����д�
	
	public int colCount = 1; //filter�漰���еĸ���
	protected Expression exp;//filter��Ӧ�ı��ʽ
	protected List<ColumnMetaData> columns;//�е�filter��Ӧ�����
	
	protected IArray dictMatchResult;
	
	public IFilter() {
		
	}
	
	public IFilter(ColumnMetaData column, int priority) {
		this.column = column;
		this.priority = priority;
		columnName = column.getColName();
	}
	
	/**
	 * ȡ��������෴����������� v ? fʱ��ת������ʱ�ֶ�����
	 * @param op
	 * @return
	 */
	public static int getInverseOP(int op) {
		switch (op) {
		case GREATER:
			return LESS;
		case GREATER_EQUAL:
			return LESS_EQUAL;
		case LESS:
			return GREATER;
		case LESS_EQUAL:
			return GREATER_EQUAL;
		default:
			return op;
		}
	}

	public ColumnMetaData getColumn() {
		return column;
	}
	
	public String getColumnName() {
		if (columnName != null) {
			return columnName;
		}
		return column.getColName();
	}
	
	public int getPriority() {
		return priority;
	}
	
	public boolean isSameColumn(IFilter other) {
		if (columnName != null) {
			return columnName.equals(other.columnName);
		}
		return column == other.column;
	}
	
	/**
	 * ����value�Ƿ�ƥ��˹��˱��ʽ
	 * @param value
	 * @return
	 */
	public abstract boolean match(Object value);
	
	/**
	 * ��������[minValue, maxValue]�Ƿ���ֵƥ��˹��˱��ʽ
	 * @param minValue
	 * @param maxValue
	 * @return
	 */
	public abstract boolean match(Object minValue, Object maxValue);
	
	public int compareTo(IFilter o) {
		if (priority < o.priority) {
			return -1;
		} else if (priority > o.priority) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * �Ƿ��Ƕ��ֶ�or
	 * @return
	 */
	public boolean isMultiFieldOr() {
		return false;
	}
	
	public int getColCount() {
		return colCount;
	}

	public void setColCount(int colCount) {
		this.colCount = colCount;
	}
	
	public IArray calculateAll(Context ctx) {
		return exp.calculateAll(ctx);
	}
	
	public IArray calculateAnd(Context ctx, IArray leftResult) {
		return exp.calculateAnd(ctx, leftResult);
	}
	
	public int isValueRangeMatch(Context ctx) {
		return exp.isValueRangeMatch(ctx);
	}
	
	public List<ColumnMetaData> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnMetaData> columns) {
		this.columns = columns;
	}
	
	public void initExp() {
	}
	
	public Expression getExp() {
		return exp;
	}
	
	public void deepOptimize(Context ctx) {
		if (exp != null) {
			exp.deepOptimize(ctx);
		}
	}
	
	public IArray getDictMatchResult() {
		return dictMatchResult;
	}

	public void setDictMatchResult(IArray dictMatchResult) {
		this.dictMatchResult = dictMatchResult;
	}

	//�Ƿ��������
	public boolean canSkipRow() {
		return dictMatchResult != null;
	}
}
