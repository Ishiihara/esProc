package com.scudata.dw;

import java.util.ArrayList;
import java.util.List;

import com.scudata.dm.Context;
import com.scudata.dm.Param;
import com.scudata.expression.Expression;
import com.scudata.expression.FieldRef;
import com.scudata.expression.Node;
import com.scudata.expression.Operator;
import com.scudata.util.Variant;

public class UnknownNodeFilter extends IFilter {
	private Node node;
	private Param param;
	private Context ctx;
	private boolean needSelect;
	protected List<String> otherNames;//������
	
	public UnknownNodeFilter(ColPhyTable table, Node node, Context ctx) {
		this.node = node;
		this.ctx = ctx;
		this.exp = new Expression(node);
		init(table, node, null, null, ctx);
	}
	
	public UnknownNodeFilter(ColPhyTable table, Expression exp, Context ctx) {
		this.exp = exp;
		this.ctx = ctx;
		init(table, exp.getHome(), null, null, ctx);
	}
	
	/**
	 * ������
	 * @param table ���
	 * @param node ���˱��ʽ�Ľڵ�
	 * @param exps ������Ӧ�ı��ʽ����
	 * @param names ����
	 * @param ctx
	 */
	public UnknownNodeFilter(ColPhyTable table, Node node, Expression[] exps, String[] names, Context ctx) {
		this.node = node;
		this.ctx = ctx;
		this.exp = new Expression(node);
		init(table, node, exps, names, ctx);
	}
	
	/**
	 * 
	 * @param table ���
	 * @param exp ���˱��ʽ
	 * @param exps ������Ӧ�ı��ʽ����
	 * @param names ����
	 * @param ctx
	 */
	public UnknownNodeFilter(ColPhyTable table, Expression exp, Expression[] exps, String[] names, Context ctx) {
		this.exp = exp;
		this.ctx = ctx;
		init(table, exp.getHome(), exps, names, ctx);
	}

	private static void getUsedFields(Node node, Context ctx, List<String> resultList) {
		if (node instanceof Operator) {
			Node left = node.getLeft();
			Node right = node.getRight();
			if (left != null) getUsedFields(left, ctx, resultList);
			if (right != null) getUsedFields(right, ctx, resultList);
			return;
		} else {
			if (node instanceof FieldRef) {
				return;
			} else {
				node.getUsedFields(ctx, resultList);
			}
		}
		
	}
	
	private void init(ColPhyTable table, Node node, Expression[] exps, String[] names, Context ctx) {
		List<String> resultList = new ArrayList<String>();
		getUsedFields(node, ctx, resultList);
		int size = resultList.size();
		if (size > 0) {
			List<ColumnMetaData> columns = new ArrayList<ColumnMetaData>(size);
			this.columns = columns;
			otherNames = new ArrayList<String>();
			for (String name : resultList) {
				ColumnMetaData col = table.getColumn(name);
				if (col != null) {
					if (!columns.contains(col)) {
						columns.add(col);
					}
				} else if (names != null) {
					//������
					int idx = containName(names, name);
					if (idx != -1) {
						//���ʹ���˱���
						otherNames.add(name);
						//������������ж��������
						List<String> tempList = new ArrayList<String>();
						exps[idx].getUsedFields(ctx, tempList);
						for (String str : tempList) {
							ColumnMetaData tempCol = table.getColumn(str);
							if (tempCol != null) {
								if (!columns.contains(tempCol)) {
									columns.add(tempCol);
								}
							}
						}
						
					} else {
						needSelect = true;
					}
				} else {
					needSelect = true;
				}
			}
			colCount = columns.size() + otherNames.size();
		} else {
			colCount = 0;
		}
		priority = Integer.MAX_VALUE;
	}
	
	public boolean match(Object value) {
		param.setValue(value);
		return Variant.isTrue(node.calculate(ctx));
	}
	
	public boolean match(Object minValue, Object maxValue) {
		if (Variant.isEquals(minValue, maxValue)) {
			return match(minValue);
		} else {
			return true;
		}
	}
	
	public Node getNode() {
		return node;
	}
	
	public void setNode(Node node) {
		this.node = node;
	}
	
	public Param getParam() {
		return param;
	}

	public Context getCtx() {
		return ctx;
	}
	
	public List<ColumnMetaData> getColumns() {
		return columns;
	}

	public boolean isNeedSelect() {
		return needSelect;
	}
	
	public int isValueRangeMatch(Context ctx) {
		if (colCount == 0) return 1;
		return 0;
		//if (colCount == 0) return 0;
		//return exp.isValueRangeMatch(ctx);
	}
	
	private static int containName(String[] names, String name) {
		int fcount = names.length;
		for (int i = 0; i < fcount; ++i) {
			if (name.equals(names[i])) {
				return i;
			}
		}
		return -1;
	}
	
	public List<String> getOtherNames() {
		return otherNames;
	}
}
