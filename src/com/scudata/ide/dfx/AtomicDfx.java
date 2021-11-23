package com.scudata.ide.dfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.scudata.cellset.CellRefUtil;
import com.scudata.cellset.IColCell;
import com.scudata.cellset.IRowCell;
import com.scudata.cellset.datamodel.CellSet;
import com.scudata.cellset.datamodel.ColCell;
import com.scudata.cellset.datamodel.NormalCell;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.cellset.datamodel.RowCell;
import com.scudata.common.Area;
import com.scudata.common.ByteMap;
import com.scudata.common.CellLocation;
import com.scudata.common.IByteMap;
import com.scudata.common.Matrix;
import com.scudata.dm.ParamList;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.IAtomicCmd;
import com.scudata.ide.common.control.CellRect;
import com.scudata.ide.common.control.CellSelection;
import com.scudata.ide.dfx.control.CellSetParser;
import com.scudata.ide.dfx.control.ControlUtils;
import com.scudata.ide.dfx.control.DfxControl;

/**
 * ����ԭ�Ӳ���
 *
 */
public class AtomicDfx implements IAtomicCmd {

	/** ������ */
	public static final byte INSERT_ROW = 1;
	/** ������ */
	public static final byte INSERT_COL = 2;
	/** ɾ���� */
	public static final byte REMOVE_ROW = 3;
	/** ɾ���� */
	public static final byte REMOVE_COL = 4;
	/** ������ */
	public static final byte ADD_ROW = 5;
	/** ������ */
	public static final byte ADD_COL = 6;
	/** ���ò��� */
	public static final byte SET_PARAM = 7;
	/** �������� */
	public static final byte SET_PASSWORD = 8;
	/** ���ó��� */
	public static final byte SET_CONST = 9;
	/** �ƶ����� */
	public static final byte MOVE_RECT = 10;
	/** UNDO�ƶ����� */
	public static final byte UNDO_MOVE_RECT = 11;
	/** ����������� */
	public static final byte SET_RECTCELLS = 12;
	/** ճ�� */
	public static final byte PASTE_SELECTION = 13;
	/** ճ���� */
	public static final byte PASTE_STRINGSELECTION = 14;
	/** �ƶ����� */
	public static final byte MOVE_COPY = 15;

	/**
	 * �����ļ�
	 */
	public static final String COLS = "COLS";
	public static final String ISDESC = "ISDESC";
	public static final String STARTROW = "STARTROW";
	public static final String ENDROW = "ENDROW";
	public static final String LEVEL = "LEVEL";
	public static final String ISSUBDOWN = "ISSUBDOWN";
	public static final String SEQS = "SEQS";

	public static final String POSITION = "POSITION";

	public static final String CELL_SELECTION = "CELL_SELECTION";
	public static final String MOVE_TYPE = "MOVE_TYPE";

	/**
	 * ����ؼ�
	 */
	private DfxControl control;
	/**
	 * ����
	 */
	private byte type;
	/**
	 * ��������
	 */
	public CellRect rect;
	/**
	 * ֵ
	 */
	private Object value;
	/**
	 * ѡ������
	 */
	private Vector<Object> selectedAreas;

	/**
	 * ���캯��
	 * 
	 * @param control
	 *            ����ؼ�
	 */
	public AtomicDfx(DfxControl control) {
		this.control = control;
		selectedAreas = new Vector<Object>();
		selectedAreas.addAll(control.getSelectedAreas());
		if (selectedAreas != null && !selectedAreas.isEmpty()) {
			rect = new CellRect((Area) selectedAreas.get(0));
		}
	}

	/**
	 * ��������
	 * 
	 * @param type
	 */
	public void setType(byte type) {
		this.type = type;
	}

	/**
	 * ȡ����
	 * 
	 * @return
	 */
	public byte getType() {
		return this.type;
	}

	/**
	 * ��������
	 * 
	 * @param cr
	 */
	public void setRect(CellRect cr) {
		this.rect = cr;
	}

	/**
	 * ����ֵ
	 * 
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * ȡֵ
	 * 
	 * @return
	 */
	public Object getValue() {
		return this.value;
	}

	/**
	 * ����ѡ������
	 * 
	 * @param areas
	 */
	public void setSelectedAreas(Vector<Object> areas) {
		this.selectedAreas = areas;
	}

	/**
	 * ��¡
	 */
	public Object clone() {
		AtomicDfx cmd = new AtomicDfx(control);
		cmd.setType(type);
		cmd.setValue(value);
		cmd.setRect(rect);
		return cmd;
	}

	/**
	 * ִ��
	 */
	public IAtomicCmd execute() {
		AtomicDfx reverseCmd = (AtomicDfx) this.clone();
		Object oldValue = null;
		Vector<Object> cells;
		CellSetParser csp = new CellSetParser(control.dfx);
		try {
			switch (type) {
			case INSERT_ROW:
				reverseCmd.setType(REMOVE_ROW);
				oldValue = null;
				// �����в�����
				int rCount = 0;
				if (value != null && value instanceof Vector) {
					// �����undo��Ҫ��һ��ԭʼ���и��Ƿ����
					Vector headCells = (Vector) ((Vector) value).get(0);
					for (int i = 0; i < headCells.size(); i++) {
						RowCell rc = (RowCell) headCells.get(i);
						if (rc.getVisible() == RowCell.VISIBLE_ALWAYSNOT) {
							continue;
						}
						rCount++;
					}
				} else {
					for (int r = rect.getBeginRow(); r <= rect.getEndRow(); r++) {
						if (r <= control.dfx.getRowCount()
								&& !csp.isRowVisible(r)) {
							continue;
						}
						rCount++;
					}
				}

				if (rect.getBeginRow() <= control.dfx.getRowCount()) {
					control.insertRow(rect.getBeginRow(), rCount);
				} else {
					control.addRow(rCount);
				}

				if (value != null) {
					// �û������valueΪArrayList����
					if (value instanceof ArrayList) {
						setRowCells(rect, (ArrayList) value);
					} else {
						// undo���ɵĲ���valueΪVector���Ӽ���
						setHeaderRectCells(rect, (Vector) value, true);
					}
				}

				break;
			case ADD_ROW:
				reverseCmd.setType(REMOVE_ROW);
				oldValue = null;
				rect = new CellRect(control.dfx.getRowCount() + 1, (int) 1,
						rect.getRowCount(), (int) control.dfx.getColCount());
				reverseCmd.setRect(rect);

				control.addRow(rect.getRowCount());
				if (value != null) {
					// �û������valueΪArrayList ���ͣ�
					if (value instanceof ArrayList) {
						setRowCells(rect, (ArrayList) value);
					} else {
						// undo���ɵĲ���valueΪVector���Ӽ���
						setHeaderRectCells(rect, (Vector) value, true);
					}
				}
				break;
			case INSERT_COL:
				reverseCmd.setType(REMOVE_COL);
				oldValue = null;
				// �����в�����
				int cCols = 0;
				if (value != null && value instanceof Vector) {
					// �����undo��Ҫ��һ��ԭʼ���и��Ƿ����
					Vector headCells = (Vector) ((Vector) value).get(0);
					for (int i = 0; i < headCells.size(); i++) {
						ColCell cc = (ColCell) headCells.get(i);
						if (cc.getVisible() == ColCell.VISIBLE_ALWAYSNOT) {
							continue;
						}
						cCols++;
					}
				} else {
					for (int c = rect.getBeginCol(); c <= rect.getEndCol(); c++) {
						if (!csp.isColVisible(c)) {
							continue;
						}
						cCols++;
					}
				}

				if (rect.getBeginCol() <= control.dfx.getColCount()) {
					control.insertColumn(rect.getBeginCol(), cCols);
				} else {
					control.addColumn(cCols);
				}

				if (value != null) {
					// �û������valueΪArrayList ���ͣ�
					if (value instanceof ArrayList) {
						setColCells(rect, (ArrayList) value);
					} else {
						// undo���ɵĲ���valueΪVector���Ӽ���
						setHeaderRectCells(rect, (Vector) value, false);
					}
				}
				break;
			case ADD_COL:
				reverseCmd.setType(REMOVE_COL);
				oldValue = null;
				rect = new CellRect(1, (int) (control.dfx.getColCount() + 1),
						control.dfx.getRowCount(), rect.getColCount());
				reverseCmd.setRect(rect);
				control.addColumn(rect.getColCount());
				if (value != null) {
					// �û������valueΪArrayList ���ͣ�
					if (value instanceof ArrayList) {
						setColCells(rect, (ArrayList) value);
					} else {
						// undo���ɵĲ���valueΪVector���Ӽ���
						setHeaderRectCells(rect, (Vector) value, false);
					}
				}
				break;
			case REMOVE_ROW:
				cells = getHeaderRectCells(rect, true);
				oldValue = cells;
				reverseCmd.setType(INSERT_ROW);

				ArrayList<NormalCell> adjustCells = new ArrayList<NormalCell>();

				// �����в�ɾ��
				int rows = 0;
				for (int r = rect.getEndRow(); r >= rect.getBeginRow(); r--) {
					if (!csp.isRowVisible(r)) {
						if (rows != 0) {
							List<NormalCell> temp = control.removeRow(r + 1,
									rows);
							if (temp != null) {
								adjustCells.addAll(temp);
							}
						}
						rows = 0;
						continue;
					}
					rows++;
				}
				if (rows != 0) {
					List<NormalCell> temp = control.removeRow(
							rect.getBeginRow(), rows);
					if (temp != null) {
						adjustCells.addAll(temp);
					}
				}

				cells.add(adjustCells);
				break;
			case REMOVE_COL:
				cells = getHeaderRectCells(rect, false);
				oldValue = cells;
				reverseCmd.setType(INSERT_COL);
				adjustCells = new ArrayList<NormalCell>();

				// �����в�ɾ��
				int cCount = 0;
				for (int c = rect.getEndCol(); c >= rect.getBeginCol(); c--) {
					if (!csp.isColVisible(c)) {
						if (cCount != 0) {
							List<NormalCell> temp = control.removeColumn(c + 1,
									cCount);
							if (temp != null) {
								adjustCells.addAll(temp);
							}
						}
						cCount = 0;
						continue;
					}
					cCount++;
				}
				if (cCount != 0) {
					List<NormalCell> temp = control.removeColumn(
							rect.getBeginCol(), (int) cCount);
					if (temp != null) {
						adjustCells.addAll(temp);
					}
				}

				cells.add(adjustCells);
				break;
			case SET_RECTCELLS: {
				NormalCell nc;
				Matrix newMatrix = null;
				if (value instanceof Matrix) {
					newMatrix = (Matrix) value;
					oldValue = getRealMatrixCells(control.dfx, rect);
					for (int r = 0; r < rect.getRowCount(); r++) {
						if (!csp.isRowVisible(rect.getBeginRow() + r)) {
							continue;
						}
						for (int c = 0; c < rect.getColCount(); c++) {
							if (!csp.isColVisible((int) (rect.getBeginCol() + c))) {
								continue;
							}
							nc = (NormalCell) newMatrix.get(r, c);
							control.dfx.setCell(rect.getBeginRow() + r,
									(int) (rect.getBeginCol() + c), nc);
						}
					}
					break;
				}
				// ���׸����CellSelection����
				CellSelection cs = (CellSelection) value;
				Matrix oldMatrix = getMatrixCells(control.dfx, cs.rect);
				rect = cs.rect;
				newMatrix = cs.matrix;
				CellSet cellSet = control.dfx;
				CellSelection oldCs = new CellSelection(oldMatrix, rect,
						cs.srcCellSet);
				ArrayList oldHeaders = new ArrayList();
				if (GVDfx.dfxEditor.selectState == GCDfx.SELECT_STATE_ROW) {
					ArrayList headerList = cs.rowHeaderList;
					if (headerList != null) {
						for (int i = 0; i < headerList.size(); i++) {
							Object header = headerList.get(i);
							if (header instanceof RowCell) {
								RowCell rowCell = (RowCell) header;
								int row = rowCell.getRow();
								oldHeaders.add(cellSet.getRowCell(row));
								cellSet.setRowCell(row, rowCell);
							}
						}
						oldCs.rowHeaderList = oldHeaders;
					}
				} else if (GVDfx.dfxEditor.selectState == GCDfx.SELECT_STATE_COL) {
					ArrayList headerList = cs.colHeaderList;
					if (headerList != null) {
						for (int i = 0; i < headerList.size(); i++) {
							Object header = headerList.get(i);
							if (header instanceof ColCell) {
								ColCell colCell = (ColCell) header;
								int col = colCell.getCol();
								oldHeaders.add(cellSet.getColCell(col));
								cellSet.setColCell(col, colCell);
							}
						}
						oldCs.colHeaderList = oldHeaders;
					}
				}
				oldValue = oldCs;
				int row = 0;
				for (int r = 0; r < rect.getRowCount(); r++) {
					while (!csp.isRowVisible(rect.getBeginRow() + row)) {
						row++;
					}
					int col = 0;
					for (int c = 0; c < rect.getColCount(); c++) {
						while (!csp
								.isColVisible((int) (rect.getBeginCol() + col))) {
							col++;
						}
						nc = (NormalCell) newMatrix.get(r, c);
						if (cs.isAdjustSelf()) {
							control.dfx.adjustCell(control.dfx, nc, 1, 0);
						}
						control.dfx.setCell(rect.getBeginRow() + row,
								(int) (rect.getBeginCol() + col), nc);
						col++;
					}
					row++;
				}
			}
				break;
			case PASTE_SELECTION: {
				/*
				 * ճ��ʱ�㷨�������ƣ������� ����ʱ��ճ���������������õı���ʽ
				 */
				Area area = control.getSelectedArea(0);
				if (area == null) {
					break;
				}

				CellSelection cs = (CellSelection) value;
				if (area.getEndRow() == area.getBeginRow()
						&& area.getBeginCol() == area.getEndCol()) {
					// ֻѡ��һ�����ӵ������ճ��ȫ��
					area = new Area(rect.getBeginRow(), rect.getBeginCol(),
							rect.getBeginRow() + rect.getRowCount() - 1,
							(int) (rect.getBeginCol() + rect.getColCount() - 1));
				} else if (GVDfx.dfxEditor.selectState == cs.selectState) {
					// ѡ��һ�л���һ�У�����Դ����Ҳ�Ƕ�Ӧ��ѡ�����е������Ҳճ��ȫ��
					if ((GVDfx.dfxEditor.selectState == GCDfx.SELECT_STATE_ROW && GVDfx.dfxEditor.selectedRows
							.size() == 1)
							|| (GVDfx.dfxEditor.selectState == GCDfx.SELECT_STATE_COL && GVDfx.dfxEditor.selectedCols
									.size() == 1)) {
						area = new Area(
								rect.getBeginRow(),
								rect.getBeginCol(),
								rect.getBeginRow() + rect.getRowCount() - 1,
								(int) (rect.getBeginCol() + rect.getColCount() - 1));

					}
				}

				NormalCell nc;
				CellSet cellSet = control.dfx;
				int rc = 0, cc = 0; // ����������Ҫ�ų�
				for (int r = area.getBeginRow(); r <= area.getEndRow(); r++) {
					if (csp.isRowVisible(r)) {
						rc++;
					}
				}
				for (int c = area.getBeginCol(); c <= area.getEndCol(); c++) {
					if (csp.isColVisible(c)) {
						cc++;
					}
				}

				CellRect realRect;
				Matrix oldMatrix;
				realRect = new CellRect(rect.getBeginRow(), rect.getBeginCol(),
						rc, (int) cc);
				oldMatrix = getMatrixCells(control.dfx, realRect);
				NormalCell ncClone;

				int rn = -1;
				for (int row = area.getBeginRow(); row <= area.getEndRow(); row++) {
					if (!csp.isRowVisible(row)) {
						continue;
					}
					rn++;
					int cn = -1;
					for (int col = area.getBeginCol(); col <= area.getEndCol(); col++) {
						if (!csp.isColVisible(col)) {
							continue;
						}
						cn++;
						nc = (NormalCell) cs.matrix.get(
								rn % rect.getRowCount(),
								cn % rect.getColCount());
						if (nc == null) {
							cellSet.setCell(row, col, null);
							continue;
						}
						ncClone = (NormalCell) nc.deepClone(); // �����¡,�����¸��Ӹ�������һ������
						NormalCell targetCell = (NormalCell) cellSet.getCell(
								row, col);
						if (cs.isAdjustSelf()) {
							control.dfx.adjustCell(cs.srcCellSet, ncClone, row
									- nc.getRow(), (int) (col - nc.getCol())); // ����ʱ�����Լ�
						}
						String exp;
						if (cs.isCopyValue()) {
							exp = com.scudata.util.Variant.toExportString(nc
									.getValue());
						} else {
							exp = ncClone.getExpString();
						}
						targetCell.setTip(nc.getTip());
						exp = GM.getOptionTrimChar0String(exp);
						targetCell.setExpString(exp);
					}
				}

				// �����׸�
				CellSelection oldCs = new CellSelection(oldMatrix, realRect,
						cs.srcCellSet, cs.isCopyValue());
				ArrayList oldHeaders = new ArrayList();
				if (GVDfx.dfxEditor.selectState == GCDfx.SELECT_STATE_ROW) {
					ArrayList headerList = cs.rowHeaderList;
					if (headerList != null) {
						rn = -1;
						for (int row = area.getBeginRow(); row <= area
								.getEndRow(); row++) {
							if (!csp.isRowVisible(row)) {
								continue;
							}
							rn++;

							Object header = headerList.get(rn
									% headerList.size());
							if (header instanceof RowCell) {
								RowCell rowCell = (RowCell) header;
								oldHeaders.add(cellSet.getRowCell(row));
								cellSet.setRowCell(row,
										(RowCell) rowCell.deepClone());
							}
						}
						oldCs.rowHeaderList = oldHeaders;
					}
				} else if (GVDfx.dfxEditor.selectState == GCDfx.SELECT_STATE_COL) {
					ArrayList headerList = cs.colHeaderList;
					if (headerList != null) {
						int cn = -1;
						for (int col = area.getBeginCol(); col <= area
								.getEndCol(); col++) {
							if (!csp.isColVisible(col)) {
								continue;
							}
							cn++;
							Object header = headerList.get(cn
									% headerList.size());
							if (header instanceof ColCell) {
								ColCell colCell = (ColCell) header;
								oldHeaders.add(cellSet.getColCell(col));
								cellSet.setColCell(col,
										(ColCell) colCell.deepClone());
							}
						}
						oldCs.colHeaderList = oldHeaders;
					}
				}
				rect = realRect;
				oldValue = oldCs;

				if (cs.isCutStatus()) { // ����ʱ��������
					// Ŀ�����������ϣ���������һ����Ա���ʽ
					for (int r = 0; r < cs.rect.getRowCount(); r++) {
						for (int c = 0; c < cs.rect.getColCount(); c++) {
							CellLocation src = new CellLocation(
									cs.rect.getBeginRow() + r,
									(int) (cs.rect.getBeginCol() + c));
							CellLocation target = new CellLocation(
									rect.getBeginRow() + r,
									(int) (rect.getBeginCol() + c));
							control.dfx.adjustReference(src, target);
						}
					}
				}
				reverseCmd.setType(AtomicDfx.SET_RECTCELLS);
			}

				break;
			case PASTE_STRINGSELECTION: {
				oldValue = GMDfx.getMatrixCells(control.dfx, rect);
				Matrix matrixData = (Matrix) value;
				CellLocation cp;
				String data;
				NormalCell nc;
				Area area = control.getSelectedArea(0);
				if (area == null) {
					break;
				}
				CellSetParser parser = new CellSetParser(control.dfx);
				int rc = area.getEndRow() - area.getBeginRow() + 1;
				int cc = area.getEndCol() - area.getBeginCol() + 1;
				for (int r = area.getBeginRow(); r <= area.getEndRow(); r++) {
					if (!parser.isRowVisible(r)) {
						rc--;
					}
				}
				int rScale = 1, cScale = 1;
				if (rc % rect.getRowCount() == 0
						&& cc % rect.getColCount() == 0) {
					rScale = rc / rect.getRowCount();
					cScale = cc / rect.getColCount();
				}
				for (int i = 0; i < rScale; i++) {
					for (int r = 0; r < rect.getRowCount(); r++) {
						int tarRow = rect.getBeginRow() + i
								* rect.getRowCount() + r;
						if (!parser.isRowVisible(tarRow))
							continue;
						for (int j = 0; j < cScale; j++) {
							for (int c = 0; c < rect.getColCount(); c++) {
								data = (String) matrixData.get(r, c);
								cp = new CellLocation(tarRow,
										(int) (rect.getBeginCol() + j
												* rect.getColCount() + c));
								nc = (NormalCell) control.dfx.getCell(
										cp.getRow(), cp.getCol());
								data = GM.getOptionTrimChar0String(data);
								nc.setExpString(data);
							}
						}
					}
				}
				reverseCmd.setType(AtomicDfx.SET_RECTCELLS);
			}
				break;
			case SET_PARAM:
				reverseCmd.setType(SET_PARAM);
				oldValue = control.dfx.getParamList();
				control.dfx.setParamList((ParamList) value);
				break;
			case SET_CONST:
				reverseCmd.setType(SET_CONST);
				PgmCellSet cellSet = GVDfx.dfxEditor.getComponent().dfx;
				oldValue = cellSet.getParamList();
				cellSet.setParamList((ParamList) value);
				break;
			case MOVE_COPY: {
				reverseCmd.setType(MOVE_COPY);
				Map map = (Map) value;
				CellSelection cs = (CellSelection) map.get(CELL_SELECTION);
				short moveType = ((Short) map.get(MOVE_TYPE)).shortValue();
				short oldType = moveType;
				CellSetParser parser = new CellSetParser(control.dfx);
				NormalCell nc;
				CellRect newRect = null;
				selectedAreas.clear();
				Area newArea = null;
				switch (moveType) {
				case GCDfx.iMOVE_COPY_UP: {
					oldType = GCDfx.iMOVE_COPY_DOWN;
					int tarRow = getPreVisibleRow(parser, rect.getBeginRow());
					if (tarRow == -1)
						return null;
					Matrix srcData = GMDfx.getMatrixCells(
							this.control.dfx,
							new CellRect(this.rect.getBeginRow(), 1, this.rect
									.getRowCount(), this.control.dfx
									.getColCount()));
					Matrix tarData = GMDfx.getMatrixCells(
							control.dfx,
							new CellRect(tarRow, 1, 1, control.dfx
									.getColCount()));
					// Ŀ���и߶�
					float tarHeight = control.dfx.getRowCell(tarRow)
							.getHeight();
					// Դ�����и�
					float[] rowHeights = new float[srcData.getRowSize()];
					int rowIndex = 0;
					for (int r = rect.getBeginRow(); r <= rect.getEndRow(); r++) {
						if (!parser.isRowVisible(r)) {
							continue;
						}
						rowHeights[rowIndex] = control.dfx.getRowCell(r)
								.getHeight();
						rowIndex++;
					}
					// ѡ�и�������
					rowIndex = 0;
					for (int r = tarRow; r < rect.getEndRow(); r++) {
						if (!parser.isRowVisible(r))
							continue;
						int colIndex = 0;
						for (int c = 1; c <= control.dfx.getColCount(); c++) {
							if (!parser.isColVisible(c))
								continue;
							nc = (NormalCell) srcData.get(rowIndex, colIndex);
							control.dfx.setCell(r, c, nc);
							colIndex++;
						}
						control.dfx.getRowCell(r).setHeight(
								rowHeights[rowIndex]);
						rowIndex++;
					}
					// Ŀ����������
					int colIndex = 0;
					for (int c = 1; c <= control.dfx.getColCount(); c++) {
						if (!parser.isColVisible(c))
							continue;
						nc = (NormalCell) tarData.get(0, colIndex);
						control.dfx.setCell(rect.getEndRow(), c, nc);
						colIndex++;
					}
					control.dfx.getRowCell(rect.getEndRow()).setHeight(
							tarHeight);

					// ����������ϣ���������һ����Ա���ʽ
					for (int r = rect.getBeginRow(); r <= rect.getEndRow(); r++) {
						if (!parser.isRowVisible(r))
							continue;
						int tRow = getPreVisibleRow(parser, r);
						for (int c = 1; c <= control.dfx.getColCount(); c++) {
							if (!parser.isColVisible(c))
								continue;
							CellLocation clSrc = new CellLocation(r, c);
							CellLocation clTar = new CellLocation(tRow, c);
							control.dfx.exchangeReference(clSrc, clTar);
						}
					}
					int endRow = getPreVisibleRow(parser, rect.getEndRow());
					newArea = new Area(tarRow, rect.getBeginCol(), endRow,
							rect.getEndCol());
					break;
				}
				case GCDfx.iMOVE_COPY_DOWN: {
					oldType = GCDfx.iMOVE_COPY_UP;
					int tarRow = getNextVisibleRow(parser,
							this.rect.getEndRow());
					if (tarRow == -1)
						return null;
					Matrix srcData = GMDfx.getMatrixCells(
							this.control.dfx,
							new CellRect(this.rect.getBeginRow(), 1, this.rect
									.getRowCount(), this.control.dfx
									.getColCount()));
					Matrix tarData = GMDfx.getMatrixCells(
							this.control.dfx,
							new CellRect(tarRow, 1, 1, this.control.dfx
									.getColCount()));
					
					// Ŀ���и߶�
					float tarHeight = control.dfx.getRowCell(tarRow)
							.getHeight();
					// Դ�����и�
					float[] rowHeights = new float[srcData.getRowSize()];
					int rowIndex = 0;
					for (int r = rect.getBeginRow(); r <= rect.getEndRow(); r++) {
						if (!parser.isRowVisible(r)) {
							continue;
						}
						rowHeights[rowIndex] = control.dfx.getRowCell(r)
								.getHeight();
						rowIndex++;
					}

					rowIndex = 0;
					for (int r = this.rect.getBeginRow() + 1; r <= tarRow; r++) {
						if (parser.isRowVisible(r)) {
							int colIndex = 0;
							for (int c = 1; c <= this.control.dfx.getColCount(); c++)
								if (parser.isColVisible(c)) {
									nc = (NormalCell) srcData.get(rowIndex,
											colIndex);
									this.control.dfx.setCell(r, c, nc);
									colIndex++;
								}
							control.dfx.getRowCell(r).setHeight(
									rowHeights[rowIndex]);
							rowIndex++;
						}
					}
					int colIndex = 0;
					for (int c = 1; c <= this.control.dfx.getColCount(); c++) {
						if (parser.isColVisible(c)) {
							nc = (NormalCell) tarData.get(0, colIndex);
							this.control.dfx.setCell(this.rect.getBeginRow(),
									c, nc);
							colIndex++;
						}
					}
					control.dfx.getRowCell(rect.getBeginRow()).setHeight(
							tarHeight);

					for (int r = this.rect.getEndRow(); r >= this.rect
							.getBeginRow(); r--) {
						if (parser.isRowVisible(r)) {
							int tRow = getNextVisibleRow(parser, r);
							for (int c = 1; c <= this.control.dfx.getColCount(); c++)
								if (parser.isColVisible(c)) {
									CellLocation clSrc = new CellLocation(r, c);
									CellLocation clTar = new CellLocation(tRow,
											c);
									this.control.dfx.exchangeReference(clSrc,
											clTar);
								}
						}
					}
					int startRow = getNextVisibleRow(parser,
							this.rect.getBeginRow());
					newArea = new Area(startRow, this.rect.getBeginCol(),
							tarRow, this.rect.getEndCol());
					break;
				}
				case GCDfx.iMOVE_COPY_LEFT: {
					oldType = GCDfx.iMOVE_COPY_RIGHT;
					int tarCol = getPreVisibleCol(parser,
							this.rect.getBeginCol());
					if (tarCol == -1)
						return null;
					Matrix srcData = GMDfx.getMatrixCells(
							this.control.dfx,
							new CellRect(this.rect.getBeginRow(), this.rect
									.getBeginCol(), this.rect.getRowCount(),
									this.rect.getColCount()));
					Matrix tarData = GMDfx.getMatrixCells(this.control.dfx,
							new CellRect(this.rect.getBeginRow(), tarCol,
									this.rect.getRowCount(), 1));

					int colIndex = 0;
					for (int c = tarCol; c < this.rect.getEndCol(); c++) {
						if (parser.isColVisible(c)) {
							int rowIndex = 0;
							for (int r = this.rect.getBeginRow(); r <= this.rect
									.getEndRow(); r++)
								if (parser.isRowVisible(r)) {
									nc = (NormalCell) srcData.get(rowIndex,
											colIndex);
									this.control.dfx.setCell(r, c, nc);
									rowIndex++;
								}
							colIndex++;
						}
					}
					int rowIndex = 0;
					for (int r = this.rect.getBeginRow(); r <= this.rect
							.getEndRow(); r++) {
						if (parser.isRowVisible(r)) {
							nc = (NormalCell) tarData.get(rowIndex, 0);
							this.control.dfx.setCell(r, this.rect.getEndCol(),
									nc);
							rowIndex++;
						}
					}

					for (int c = this.rect.getBeginCol(); c <= this.rect
							.getEndCol(); c++) {
						if (parser.isColVisible(c)) {
							int tCol = getPreVisibleCol(parser, c);
							for (int r = this.rect.getBeginRow(); r <= this.rect
									.getEndRow(); r++)
								if (parser.isRowVisible(r)) {
									CellLocation clSrc = new CellLocation(r, c);
									CellLocation clTar = new CellLocation(r,
											tCol);
									System.out
											.println("exchangeReference() from:"
													+ c + "; to:" + tCol);
									this.control.dfx.exchangeReference(clSrc,
											clTar);
								}
						}
					}
					int endCol = getPreVisibleCol(parser, this.rect.getEndCol());
					newArea = new Area(this.rect.getBeginRow(), tarCol,
							this.rect.getEndRow(), endCol);
					break;
				}
				case GCDfx.iMOVE_COPY_RIGHT: {
					oldType = GCDfx.iMOVE_COPY_LEFT;
					int tarCol = getNextVisibleCol(parser,
							this.rect.getEndCol());
					if (tarCol == -1)
						return null;
					Matrix srcData = GMDfx.getMatrixCells(
							this.control.dfx,
							new CellRect(this.rect.getBeginRow(), this.rect
									.getBeginCol(), this.rect.getRowCount(),
									this.rect.getColCount()));
					Matrix tarData = GMDfx.getMatrixCells(this.control.dfx,
							new CellRect(this.rect.getBeginRow(), tarCol,
									this.rect.getRowCount(), 1));

					int colIndex = 0;
					for (int c = this.rect.getBeginCol() + 1; c <= tarCol; c++) {
						if (parser.isColVisible(c)) {
							int rowIndex = 0;
							for (int r = this.rect.getBeginRow(); r <= this.rect
									.getEndRow(); r++)
								if (parser.isRowVisible(r)) {
									nc = (NormalCell) srcData.get(rowIndex,
											colIndex);
									this.control.dfx.setCell(r, c, nc);
									rowIndex++;
								}
							colIndex++;
						}
					}
					int rowIndex = 0;
					for (int r = this.rect.getBeginRow(); r <= this.rect
							.getEndRow(); r++) {
						if (parser.isRowVisible(r)) {
							nc = (NormalCell) tarData.get(rowIndex, 0);
							this.control.dfx.setCell(r,
									this.rect.getBeginCol(), nc);
							rowIndex++;
						}
					}

					for (int c = this.rect.getEndCol(); c >= this.rect
							.getBeginCol(); c--) {
						if (parser.isColVisible(c)) {
							int tCol = getNextVisibleCol(parser, c);
							for (int r = this.rect.getBeginRow(); r <= this.rect
									.getEndRow(); r++)
								if (parser.isRowVisible(r)) {
									CellLocation clSrc = new CellLocation(r, c);
									CellLocation clTar = new CellLocation(r,
											tCol);
									System.out
											.println("exchangeReference() from:"
													+ c + "; to:" + tCol);
									this.control.dfx.exchangeReference(clSrc,
											clTar);
								}
						}
					}
					int startCol = getNextVisibleCol(parser,
							this.rect.getBeginCol());
					newArea = new Area(this.rect.getBeginRow(), startCol,
							this.rect.getEndRow(), tarCol);
					break;
				}
				}
				selectedAreas.add(newArea);
				newRect = new CellRect(newArea);
				CellSelection oldCs = new CellSelection(null, newRect,
						cs.srcCellSet);
				reverseCmd.setRect(newRect);
				Map oldMap = new HashMap();
				oldMap.put(CELL_SELECTION, oldCs);
				oldMap.put(MOVE_TYPE, new Short(oldType));
				oldValue = oldMap;
				break;
			}
			case MOVE_RECT: {
				CellSelection cs = (CellSelection) value;
				Matrix fromData = GMDfx.getMatrixCells((CellSet) cs.srcCellSet,
						cs.rect);
				CellSelection rcs = new CellSelection(fromData, rect,
						cs.srcCellSet);
				rcs.oldData = GMDfx.getMatrixCells(control.dfx, rect);
				List errorCells = new ArrayList();
				reverseCmd.setRect(cs.rect);

				NormalCell nc;
				int dr = rect.getBeginRow() - cs.rect.getBeginRow();
				int dc = (int) (rect.getBeginCol() - cs.rect.getBeginCol());

				// �Ȼ�ԭԴ���ӣ��������ص�ʱ�������Ȼ�ԭ
				for (int r = 0; r < cs.rect.getRowCount(); r++) {
					for (int c = 0; c < cs.rect.getColCount(); c++) {
						if (cs.oldData != null) {
							nc = (NormalCell) cs.oldData.get(r, c);
						} else {
							nc = control.dfx.newCell(r + 1, c + 1);
						}
						control.dfx.setCell(cs.rect.getBeginRow() + r,
								(int) (cs.rect.getBeginCol() + c), nc);
					}
				}

				// ����Ŀ�����
				for (int r = 0; r < rect.getRowCount(); r++) {
					for (int c = 0; c < rect.getColCount(); c++) {
						nc = (NormalCell) fromData.get(r, c);
						if (nc == null) {
							control.dfx.setCell(rect.getBeginRow() + r,
									(int) (rect.getBeginCol() + c), null);
						} else {
							control.dfx.setCell(rect.getBeginRow() + r,
									(int) (rect.getBeginCol() + c),
									(NormalCell) nc.deepClone());
						}
					}
				}

				// Ŀ�����������ϣ���������һ����Ա���ʽ
				if (dr >= 0) {
					if (dc >= 0) {
						for (int r = rect.getRowCount() - 1; r >= 0; r--) {
							for (int c = (int) (rect.getColCount() - 1); c >= 0; c--) {
								List tmp = exeAdjust(
										cs.rect,
										rect,
										r,
										c,
										fromData == null ? null
												: (NormalCell) fromData.get(r,
														c));
								if (tmp != null)
									errorCells.addAll(tmp);
							}
						}
					} else {
						for (int r = rect.getRowCount() - 1; r >= 0; r--) {
							for (int c = 0; c < rect.getColCount(); c++) {
								List tmp = exeAdjust(
										cs.rect,
										rect,
										r,
										c,
										fromData == null ? null
												: (NormalCell) fromData.get(r,
														c));
								if (tmp != null)
									errorCells.addAll(tmp);
							}
						}
					}
				} else {
					if (dc >= 0) {
						for (int r = 0; r < rect.getRowCount(); r++) {
							for (int c = (int) (rect.getColCount() - 1); c >= 0; c--) {
								List tmp = exeAdjust(
										cs.rect,
										rect,
										r,
										c,
										fromData == null ? null
												: (NormalCell) fromData.get(r,
														c));
								if (tmp != null)
									errorCells.addAll(tmp);
							}
						}
					} else {
						for (int r = 0; r < rect.getRowCount(); r++) {
							for (int c = 0; c < rect.getColCount(); c++) {
								List tmp = exeAdjust(
										cs.rect,
										rect,
										r,
										c,
										fromData == null ? null
												: (NormalCell) fromData.get(r,
														c));
								if (tmp != null)
									errorCells.addAll(tmp);
							}
						}
					}
				}
				reverseCmd.setType(UNDO_MOVE_RECT);
				Vector values = new Vector();
				values.add(rcs);
				values.add(errorCells);
				oldValue = values;
			}
				break;
			case UNDO_MOVE_RECT: {
				CellSelection cs;
				List oldErrorCells = null;
				Vector oldValues = (Vector) value;
				cs = (CellSelection) oldValues.get(0);
				oldErrorCells = (List) oldValues.get(1);
				Matrix fromData = GMDfx.getMatrixCells(
						(PgmCellSet) cs.srcCellSet, cs.rect);
				CellSelection rcs = new CellSelection(fromData, rect,
						cs.srcCellSet);
				rcs.oldData = GMDfx.getMatrixCells(control.dfx, rect);
				List errorCells = new ArrayList();
				reverseCmd.setRect(cs.rect);

				NormalCell nc;
				int dr = rect.getBeginRow() - cs.rect.getBeginRow();
				int dc = (int) (rect.getBeginCol() - cs.rect.getBeginCol());
				// Ŀ�����������ϣ���������һ����Ա���ʽ
				if (dr >= 0) {
					if (dc >= 0) {
						for (int r = rect.getRowCount() - 1; r >= 0; r--) {
							for (int c = (int) (rect.getColCount() - 1); c >= 0; c--) {
								List tmp = exeAdjust(
										cs.rect,
										rect,
										r,
										c,
										cs.matrix == null ? null
												: (NormalCell) cs.matrix.get(r,
														c));
								if (tmp != null)
									errorCells.addAll(tmp);
							}
						}
					} else {
						for (int r = rect.getRowCount() - 1; r >= 0; r--) {
							for (int c = 0; c < rect.getColCount(); c++) {
								List tmp = exeAdjust(
										cs.rect,
										rect,
										r,
										c,
										cs.matrix == null ? null
												: (NormalCell) cs.matrix.get(r,
														c));
								if (tmp != null)
									errorCells.addAll(tmp);
							}
						}
					}
				} else {
					if (dc >= 0) {
						for (int r = 0; r < rect.getRowCount(); r++) {
							for (int c = (int) (rect.getColCount() - 1); c >= 0; c--) {
								List tmp = exeAdjust(
										cs.rect,
										rect,
										r,
										c,
										cs.matrix == null ? null
												: (NormalCell) cs.matrix.get(r,
														c));
								if (tmp != null)
									errorCells.addAll(tmp);
							}
						}
					} else {
						for (int r = 0; r < rect.getRowCount(); r++) {
							for (int c = 0; c < rect.getColCount(); c++) {
								List tmp = exeAdjust(
										cs.rect,
										rect,
										r,
										c,
										cs.matrix == null ? null
												: (NormalCell) cs.matrix.get(r,
														c));
								if (tmp != null)
									errorCells.addAll(tmp);
							}
						}
					}
				}
				// �Ȼ�ԭԴ���ӣ��������ص�ʱ�������Ȼ�ԭ
				for (int r = 0; r < cs.rect.getRowCount(); r++) {
					for (int c = 0; c < cs.rect.getColCount(); c++) {
						if (cs.oldData != null) {
							nc = (NormalCell) cs.oldData.get(r, c);
						} else {
							nc = control.dfx.newCell(r + 1, c + 1);
						}
						control.dfx.setCell(cs.rect.getBeginRow() + r,
								(int) (cs.rect.getBeginCol() + c), nc);
					}
				}

				// ����Ŀ�����
				for (int r = 0; r < rect.getRowCount(); r++) {
					for (int c = 0; c < rect.getColCount(); c++) {
						nc = (NormalCell) cs.matrix.get(r, c);
						if (nc == null) {
							control.dfx.setCell(rect.getBeginRow() + r,
									(int) (rect.getBeginCol() + c), null);
						} else {
							control.dfx.setCell(rect.getBeginRow() + r,
									(int) (rect.getBeginCol() + c),
									(NormalCell) nc.deepClone());
						}
					}
				}

				if (oldErrorCells != null) {
					for (int i = 0; i < oldErrorCells.size(); i++) {
						NormalCell nc1 = (NormalCell) oldErrorCells.get(i);
						if (nc1.getRow() >= cs.rect.getBeginRow()
								&& nc1.getRow() <= cs.rect.getEndRow()) {
							if (nc1.getCol() >= cs.rect.getBeginCol()
									&& nc1.getCol() <= cs.rect.getEndCol()) {
								nc1.setRow(nc1.getRow() + dr);
								nc1.setCol(nc1.getCol() + dc);
							}
						}
						nc1.undoErrorRef();
					}
				}
				rcs.setAdjustSelf(false);
				Vector<Object> values = new Vector<Object>();
				values.add(rcs);
				values.add(errorCells);
				oldValue = values;
			}
				break;
			}
		} finally {
			ControlUtils.extractDfxEditor(control).setSelectedAreas(
					selectedAreas);
		}
		reverseCmd.setValue(oldValue);

		return reverseCmd;
	}

	/**
	 * ȡ��һ�����ӵ���
	 * 
	 * @param parser
	 * @param row
	 * @return
	 */
	private int getPreVisibleRow(CellSetParser parser, int row) {
		for (int r = row - 1; r >= 1; r--) {
			if (parser.isRowVisible(r))
				return r;
		}
		return -1;
	}

	/**
	 * ȡ��һ�����ӵ���
	 * 
	 * @param parser
	 * @param row
	 * @return
	 */
	private int getNextVisibleRow(CellSetParser parser, int row) {
		for (int r = row + 1; r <= parser.getRowCount(); r++) {
			if (parser.isRowVisible(r))
				return r;
		}
		return -1;
	}

	/**
	 * ȡǰһ�����ӵ���
	 * 
	 * @param parser
	 * @param col
	 * @return
	 */
	private int getPreVisibleCol(CellSetParser parser, int col) {
		for (int c = col - 1; c >= 1; c--) {
			if (parser.isColVisible(c))
				return c;
		}
		return -1;
	}

	/**
	 * ȡ��һ�����ӵ���
	 * 
	 * @param parser
	 * @param col
	 * @return
	 */
	private int getNextVisibleCol(CellSetParser parser, int col) {
		for (int c = col + 1; c <= parser.getColCount(); c++) {
			if (parser.isColVisible(c))
				return c;
		}
		return -1;
	}

	/**
	 * CellRect������������
	 *
	 * @param cellSet
	 *            CellSet
	 * @param rect
	 *            CellRect
	 * @return Matrix
	 */
	private Matrix getMatrixCells(CellSet cellSet, CellRect rect) {
		Matrix m = new Matrix(rect.getRowCount(), rect.getColCount());
		NormalCell nc;
		CellSetParser csp = new CellSetParser(cellSet);
		int row = rect.getBeginRow();
		for (int i = 0; i < rect.getRowCount(); i++) {
			while (!csp.isRowVisible(row)) {
				row++;
			}
			int col = (int) (rect.getBeginCol());
			for (int j = 0; j < rect.getColCount(); j++) {
				if (!csp.isColVisible(col)) {
					col++;
				}
				NormalCell temp = (NormalCell) cellSet.getCell(row, col);
				nc = (NormalCell) temp.deepClone();
				nc.setValue(GM.getOptionTrimChar0Value(temp.getValue()));
				m.set(i, j, nc);
				col++;
			}
			row++;
		}
		return m;
	}

	/**
	 * ȡ����ĸ��Ӿ���
	 * 
	 * @param cellSet
	 * @param rect
	 * @return
	 */
	private Matrix getRealMatrixCells(CellSet cellSet, CellRect rect) {
		Matrix m = new Matrix(rect.getRowCount(), rect.getColCount());
		NormalCell nc;
		for (int row = rect.getBeginRow(); row <= rect.getEndRow(); row++) {
			for (int col = rect.getBeginCol(); col <= rect.getEndCol(); col++) {
				NormalCell temp = (NormalCell) cellSet.getCell(row, col);
				nc = (NormalCell) temp.deepClone();
				nc.setValue(GM.getOptionTrimChar0Value(temp.getValue()));
				m.set(row - rect.getBeginRow(), col - rect.getBeginCol(), nc);
			}
		}
		return m;
	}

	/**
	 * ��������
	 * 
	 * @param csr
	 *            Դ����
	 * @param rect
	 *            Ŀ������
	 * @param r
	 *            �к�
	 * @param c
	 *            �к�
	 * @param cell
	 *            ��Ԫ��
	 * @return
	 */
	private List exeAdjust(CellRect csr, CellRect rect, int r, int c,
			NormalCell cell) {
		if (cell == null)
			return null;
		String exp = cell.getExpString();
		if (exp != null && exp.startsWith(CellRefUtil.ERRORREF)) {
			return null;
		}
		CellLocation src = new CellLocation(csr.getBeginRow() + r,
				(int) (csr.getBeginCol() + c));
		CellLocation target = new CellLocation(rect.getBeginRow() + r,
				(int) (rect.getBeginCol() + c));
		return control.dfx.adjustReference(src, target);
	}

	/**
	 * �����и���
	 * 
	 * @param cr
	 * @param aRowCells
	 */
	private void setRowCells(CellRect cr, ArrayList aRowCells) {
		// aRowCells, 0:�׸�,��������Ϊ��Ӧ�еĸ���
		RowCell rc = (RowCell) aRowCells.get(0);
		for (int r = cr.getBeginRow(); r <= cr.getEndRow(); r++) {
			control.dfx.setRowCell(r, (RowCell) rc.deepClone());
		}
		if (aRowCells.size() == 1) {
			return;
		}
		for (int c = 0; c < cr.getColCount(); c++) {
			IByteMap partMap = (IByteMap) aRowCells.get(c + 1);
			for (int r = 0; r < cr.getRowCount(); r++) {
				NormalCell nc = (NormalCell) control.dfx.getCell(
						cr.getBeginRow() + r, (int) (cr.getBeginCol() + c));
				IByteMap ncMap = new ByteMap();
				for (int i = 0; i < partMap.size(); i++) {
					byte p = partMap.getKey(i);
					Object v = partMap.getValue(i);
					ncMap.put(p, v);
				}

				for (int i = 0; i < ncMap.size(); i++) {
					AtomicCell.setCellProperty(nc, ncMap.getKey(i),
							ncMap.getValue(i));
				}
			}
		}
	}

	/**
	 * �����и���
	 * 
	 * @param cr
	 * @param aColCells
	 */
	private void setColCells(CellRect cr, ArrayList aColCells) {
		// aColCells, 0:�׸�,��������Ϊ��Ӧ�еĸ���
		ColCell cc = (ColCell) aColCells.get(0);
		for (int c = cr.getBeginCol(); c <= cr.getEndCol(); c++) {
			control.dfx.setColCell(c, (ColCell) cc.deepClone());
		}
		if (aColCells.size() == 1) {
			return;
		}
		for (int r = 0; r < cr.getRowCount(); r++) {
			IByteMap partMap = (IByteMap) aColCells.get(r + 1);
			for (int c = 0; c < cr.getColCount(); c++) {
				NormalCell nc = (NormalCell) control.dfx.getCell(
						cr.getBeginRow() + r, (int) (cr.getBeginCol() + c));
				IByteMap ncMap = new ByteMap();
				for (int i = 0; i < partMap.size(); i++) {
					byte p = partMap.getKey(i);
					Object v = partMap.getValue(i);
					ncMap.put(p, v);
				}

				for (int i = 0; i < ncMap.size(); i++) {
					AtomicCell.setCellProperty(nc, ncMap.getKey(i),
							ncMap.getValue(i));
				}
			}
		}
	}

	/**
	 * ���ô������׸�ľ���
	 *
	 * @param cr
	 *            CellRect
	 * @param cells
	 *            Vector
	 * @param isRow
	 *            boolean
	 */
	private void setHeaderRectCells(CellRect cr, Vector cells, boolean isRow) {
		Vector headCells = (Vector) cells.get(0);
		if (isRow) {
			for (int i = 0; i < headCells.size(); i++) {
				control.dfx.setRowCell(cr.getBeginRow() + i,
						(RowCell) headCells.get(i));
			}
		} else {
			for (int i = 0; i < headCells.size(); i++) {
				control.dfx.setColCell((int) (cr.getBeginCol() + i),
						(ColCell) headCells.get(i));
			}
		}

		Matrix areaCells = (Matrix) cells.get(1);
		List srcCells = (List) cells.get(2);

		CellSetParser csp = new CellSetParser(control.dfx);
		int row = cr.getBeginRow();
		for (int r = 0; r < areaCells.getRowSize(); r++) {
			while (!csp.isRowVisible(row)) {
				row++;
			}
			int col = (int) cr.getBeginCol();
			for (int c = 0; c < areaCells.getColSize(); c++) {
				while (!csp.isColVisible(col)) {
					col++;
				}
				NormalCell nc = (NormalCell) areaCells.get(r, c);
				control.dfx.setCell(row, col, nc);
				col++;
			}
			row++;
		}

		if (srcCells != null) {
			for (int i = 0; i < srcCells.size(); i++) {
				NormalCell nc = (NormalCell) srcCells.get(i);
				nc.undoErrorRef();
			}
		}
	}

	/**
	 * ȡ����ͷ�ĸ���
	 * 
	 * @param cr
	 *            ����
	 * @param isRow
	 *            trueȡ�У�falseȡ��
	 * @return
	 */
	private Vector<Object> getHeaderRectCells(CellRect cr, boolean isRow) {
		Vector<Object> v = new Vector<Object>();
		if (isRow) {
			v.add(getRowCells(cr.getBeginRow(), cr.getRowCount()));
		} else {
			v.add(getColCells(cr.getBeginCol(), cr.getColCount()));
		}
		v.add(GMDfx.getMatrixCells(control.dfx, cr, false));
		return v;
	}

	/**
	 * ȡ�и���
	 * 
	 * @param beginRow
	 *            ��ʼ��
	 * @param rowCount
	 *            ����
	 * @return
	 */
	private Vector<IRowCell> getRowCells(int beginRow, int rowCount) {
		Vector<IRowCell> v = new Vector<IRowCell>();
		for (int i = 0; i < rowCount; i++) {
			v.add(control.dfx.getRowCell(beginRow + i));
		}
		return v;
	}

	/**
	 * ȡ�и���
	 * 
	 * @param beginCol
	 *            ��ʼ��
	 * @param colCount
	 *            ����
	 * @return
	 */
	private Vector<IColCell> getColCells(int beginCol, int colCount) {
		Vector<IColCell> v = new Vector<IColCell>();
		for (int i = 0; i < colCount; i++) {
			v.add(control.dfx.getColCell((int) (beginCol + i)));
		}
		return v;
	}
}