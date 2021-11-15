package com.raqsoft.ide.common.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.raqsoft.cellset.datamodel.PgmNormalCell;
import com.raqsoft.common.Matrix;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.StringUtils;
import com.raqsoft.dm.Param;
import com.raqsoft.dm.ParamList;
import com.raqsoft.ide.common.GM;
import com.raqsoft.ide.common.GV;
import com.raqsoft.ide.common.resources.IdeCommonMessage;
import com.raqsoft.ide.common.swing.JTableEx;
import com.raqsoft.ide.common.swing.VFlowLayout;

/**
 * ��������Ի���
 */
public class DialogArgument extends DialogMaxmizable {
	private static final long serialVersionUID = 1L;
	/**
	 * Common��Դ������
	 */
	private MessageManager mm = IdeCommonMessage.get();

	/** ����� */
	private final byte COL_INDEX = 0;
	/** ������ */
	private final byte COL_NAME = 1;
	/** ֵ�� */
	private final byte COL_VALUE = 2;
	/** ��ע�� */
	private final byte COL_REMARK = 3;

	/** ����б��� */
	private final String TITLE_INDEX = mm.getMessage("dialogargument.index");
	/** �����б��� */
	private final String TITLE_NAME = mm.getMessage("dialogargument.name");
	/** ֵ�б��� */
	private final String TITLE_VALUE = mm.getMessage("dialogargument.value");
	/** ��ע�б��� */
	private final String TITLE_REMARK = mm.getMessage("dialogparameter.remark");

	/**
	 * �������ؼ������,����,ֵ
	 */
	public JTableEx paraTable = new JTableEx(TITLE_INDEX + "," + TITLE_NAME
			+ "," + TITLE_VALUE + "," + TITLE_REMARK) {
		private static final long serialVersionUID = 1L;

		public void doubleClicked(int xpos, int ypos, int row, int col,
				MouseEvent e) {
			if (col != COL_INDEX) {
				GM.dialogEditTableText(paraTable, row, col);
			}
		}

		public void setValueAt(Object aValue, int row, int column) {
			if (!isItemDataChanged(row, column, aValue)) {
				return;
			}
			super.data.setValueAt(aValue, row, column);
		}
	};

	/**
	 * ȷ�ϰ�ť
	 */
	private JButton jBOK = new JButton();
	/**
	 * ȡ����ť
	 */
	private JButton jBCancel = new JButton();
	/**
	 * ���Ӱ�ť
	 */
	private JButton jBAdd = new JButton();
	/**
	 * ɾ����ť
	 */
	private JButton jBDel = new JButton();
	/**
	 * ���ư�ť
	 */
	private JButton jBUp = new JButton();
	/**
	 * ���ư�ť
	 */
	private JButton jBDown = new JButton();
	/**
	 * ȫѡ��ť
	 */
	private JButton buttonAll = new JButton(mm.getMessage("button.selectall"));
	/**
	 * ���ư�ť
	 */
	private JButton buttonCopy = new JButton(
			mm.getMessage("dialogparameter.copy"));
	/**
	 * ճ����ť
	 */
	private JButton buttonPaste = new JButton(mm.getMessage("button.paste"));
	/**
	 * ÿ������ǰ���ò���
	 */
	private JCheckBox jcbUserChange = new JCheckBox(
			mm.getMessage("dialogparameter.setbeforerun"));
	/**
	 * ���ڵĹرն���
	 */
	private int m_option = JOptionPane.CLOSED_OPTION;
	/**
	 * �����б�
	 */
	private ParamList pl;

	/**
	 * ���캯��
	 */
	public DialogArgument() {
		super(GV.appFrame, "�����༭", true);
		try {
			initUI();
			init();
			resetLangText();
			setSize(450, 350);
			GM.setDialogDefaultButton(this, jBOK, jBCancel);
			this.setResizable(true);
		} catch (Exception e) {
			GM.showException(e);
		}
	}

	/**
	 * ��ʼ��
	 */
	private void init() {
		paraTable.setIndexCol(COL_INDEX);
		paraTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paraTable.setRowHeight(20);

		paraTable.setColumnWidth(COL_NAME, 100);
		paraTable.setClickCountToStart(1);

	}

	/**
	 * ���ش��ڹرն���
	 * 
	 * @return
	 */
	public int getOption() {
		return m_option;
	}

	/**
	 * ���ò����б�
	 * 
	 * @param pl
	 *            �����б�
	 */
	public void setParameter(ParamList pl) {
		if (pl == null) {
			return;
		}
		this.pl = pl;
		jcbUserChange.setSelected(pl.isUserChangeable());
		ParamList argList = new ParamList();
		pl.getAllVarParams(argList);
		if (argList.count() == 0)
			pl.getAllArguments(argList);
		Param p;
		for (int i = 0; i < argList.count(); i++) {
			p = argList.get(i);
			if (p == null) {
				continue;
			}
			int row = paraTable.addRow();
			paraTable.data.setValueAt(p.getName(), row, COL_NAME);
			paraTable.data.setValueAt(p.getEditValue(), row, COL_VALUE);
			paraTable.data.setValueAt(p.getRemark(), row, COL_REMARK);
		}
		paraTable.resetIndex();
	}

	/**
	 * ȡ�����б�
	 * 
	 * @return �����б�
	 */
	public ParamList getParameter() {
		if (paraTable.getRowCount() < 1) {
			return null;
		}
		ParamList plist = new ParamList();
		ParamList otherList = new ParamList();
		if (pl != null) {
			pl.getAllConsts(otherList);
		}
		plist.setUserChangeable(jcbUserChange.isSelected());
		Object o;
		for (int i = 0; i < paraTable.getRowCount(); i++) {
			String name = (String) paraTable.getValueAt(i, COL_NAME);
			if (!StringUtils.isValidString(name)) {
				continue;
			}
			Param v = new Param();
			v.setKind(Param.VAR);
			v.setName(name);
			o = paraTable.data.getValueAt(i, COL_VALUE);
			Object editValue = o;
			if (!StringUtils.isValidString(o))
				editValue = null;
			v.setEditValue(editValue);
			if (editValue == null) {
				v.setValue(null);
			} else {
				v.setValue(PgmNormalCell.parseConstValue((String) editValue));
			}
			o = paraTable.data.getValueAt(i, COL_REMARK);
			if (!StringUtils.isValidString(o)) {
				v.setRemark(null);
			} else {
				v.setRemark((String) o);
			}
			plist.add(v);
		}
		int count = otherList.count();
		for (int i = 0; i < count; i++) {
			plist.add(otherList.get(i));
		}
		return plist;
	}

	/**
	 * ��������������ʾ�ı�
	 */
	private void resetLangText() {
		setTitle(mm.getMessage("dialogparameter.title"));
		jBOK.setText(mm.getMessage("button.ok"));
		jBCancel.setText(mm.getMessage("button.cancel"));
		jBAdd.setText(mm.getMessage("button.add"));
		jBDel.setText(mm.getMessage("button.delete"));
		jBUp.setText(mm.getMessage("button.shiftup"));
		jBDown.setText(mm.getMessage("button.shiftdown"));
	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 * @throws Exception
	 */
	private void initUI() throws Exception {
		this.addWindowListener(new DialogArgument_this_windowAdapter(this));
		this.getContentPane().setLayout(new BorderLayout());
		JPanel jPanel1 = new JPanel();
		jPanel1.setLayout(new VFlowLayout());
		jBOK.setText("ȷ��(O)");
		jBOK.addActionListener(new DialogArgument_jBOK_actionAdapter(this));
		jBOK.setMnemonic('O');
		jBCancel.setMnemonic('C');
		jBCancel.setText("ȡ��(C)");
		jBCancel.addFocusListener(new DialogArgument_jBCancel_focusAdapter(this));
		jBCancel.addActionListener(new DialogArgument_jBCancel_actionAdapter(
				this));
		jBAdd.setAlignmentX((float) 0.0);
		jBAdd.setAlignmentY((float) 5.0);
		jBAdd.setMnemonic('A');
		jBAdd.setText("����(A)");
		jBAdd.addActionListener(new DialogArgument_jBAdd_actionAdapter(this));
		jBDel.setMnemonic('D');
		jBDel.setText("ɾ��(D)");
		jBDel.addActionListener(new DialogArgument_jBDel_actionAdapter(this));
		jBUp.setActionCommand("");
		jBUp.setMnemonic('U');
		jBUp.setText("����(U)");
		jBUp.addActionListener(new DialogArgument_jBUp_actionAdapter(this));
		jBDown.setToolTipText("");
		jBDown.setMnemonic('W');
		jBDown.setText("����(W)");
		jBDown.addActionListener(new DialogArgument_jBDown_actionAdapter(this));
		buttonAll.setMnemonic('A');
		buttonCopy.setMnemonic('X');
		buttonPaste.setMnemonic('P');
		buttonAll.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				paraTable.selectAll();
			}

		});
		buttonCopy.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int[] rows = paraTable.getSelectedRows();
				if (rows == null || rows.length == 0) {
					JOptionPane.showMessageDialog(GV.appFrame,
							mm.getMessage("dialogparameter.selectrow"));
					return;
				}
				paraTable.acceptText();
				StringBuffer buf = new StringBuffer();
				String rowStr;
				for (int i = 0; i < rows.length; i++) {
					if (i != 0)
						buf.append('\n');
					rowStr = paraTable.getRowData(rows[i]);
					buf.append(rowStr == null ? "" : rowStr);
				}
				GM.clipBoard(buf.toString());
			}

		});
		buttonPaste.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String str = GM.clipBoard();
				if (!StringUtils.isValidString(str)) {
					JOptionPane.showMessageDialog(GV.appFrame,
							mm.getMessage("dialogparameter.copyrow"));
					return;
				}
				try {
					paraTable.acceptText();
					Matrix m = GM.string2Matrix(str, false);
					if (m.getColSize() != paraTable.getColumnCount()) {
						JOptionPane.showMessageDialog(GV.appFrame,
								mm.getMessage("dialogparameter.copyrow"));
						return;
					}
					int count = m.getRowSize();
					for (int i = 0; i < count; i++) {
						paraTable.addRow(m.getRow(i));
					}
				} catch (Exception ex) {
					GM.showException(ex);
				}
			}

		});
		JLabel jLabel1 = new JLabel();
		jLabel1.setText(" ");
		jPanel1.add(jBOK, null);
		jPanel1.add(jBCancel, null);
		jPanel1.add(jLabel1, null);
		jPanel1.add(jBAdd, null);
		jPanel1.add(jBDel, null);
		jPanel1.add(jBUp, null);
		jPanel1.add(jBDown, null);
		jPanel1.add(new JLabel(), null);
		jPanel1.add(buttonAll, null);
		jPanel1.add(buttonCopy, null);
		jPanel1.add(buttonPaste, null);
		JPanel panelMain = new JPanel();
		panelMain.setLayout(new GridBagLayout());
		panelMain.add(jcbUserChange, GM.getGBC(1, 1, true));
		panelMain.add(new JScrollPane(paraTable), GM.getGBC(2, 1, true, true));
		this.getContentPane().add(panelMain, BorderLayout.CENTER);
		this.getContentPane().add(jPanel1, BorderLayout.EAST);
	}

	/**
	 * ɾ������
	 * 
	 * @param e
	 */
	void jBDel_actionPerformed(ActionEvent e) {
		paraTable.deleteSelectedRows();
	}

	/**
	 * ��������
	 * 
	 * @param e
	 */
	void jBAdd_actionPerformed(ActionEvent e) {
		String name = GM.getTableUniqueName(paraTable, COL_NAME, "arg");
		int r = paraTable.addRow();
		paraTable.clearSelection();

		paraTable.selectRow(r);
		paraTable.data.setValueAt(name, r, COL_NAME);
	}

	/**
	 * ȷ������
	 * 
	 * @param e
	 */
	void jBOK_actionPerformed(ActionEvent e) {
		if (!paraTable.verifyColumnData(COL_NAME, TITLE_NAME)) {
			return;
		}
		GM.setWindowDimension(this);
		m_option = JOptionPane.OK_OPTION;
		dispose();
	}

	/**
	 * ȡ������
	 * 
	 * @param e
	 */
	void jBCancel_actionPerformed(ActionEvent e) {
		GM.setWindowDimension(this);
		m_option = JOptionPane.CANCEL_OPTION;
		dispose();
	}

	/**
	 * ��������
	 * 
	 * @param e
	 */
	void jBUp_actionPerformed(ActionEvent e) {
		paraTable.shiftRowUp(-1);
	}

	/**
	 * ��������
	 * 
	 * @param e
	 */
	void jBDown_actionPerformed(ActionEvent e) {
		paraTable.shiftRowDown(-1);
	}

	/**
	 * ���ڹر�����
	 * 
	 * @param e
	 */
	void this_windowClosing(WindowEvent e) {
		jBCancel_actionPerformed(null);
	}

	/**
	 * ȡ������
	 * 
	 * @param e
	 */
	void jBCancel_focusGained(FocusEvent e) {
		jBCancel.requestFocus();
	}
}

class DialogArgument_jBDel_actionAdapter implements
		java.awt.event.ActionListener {
	DialogArgument adaptee;

	DialogArgument_jBDel_actionAdapter(DialogArgument adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBDel_actionPerformed(e);
	}
}

class DialogArgument_jBAdd_actionAdapter implements
		java.awt.event.ActionListener {
	DialogArgument adaptee;

	DialogArgument_jBAdd_actionAdapter(DialogArgument adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBAdd_actionPerformed(e);
	}
}

class DialogArgument_jBOK_actionAdapter implements
		java.awt.event.ActionListener {
	DialogArgument adaptee;

	DialogArgument_jBOK_actionAdapter(DialogArgument adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBOK_actionPerformed(e);
	}
}

class DialogArgument_jBCancel_actionAdapter implements
		java.awt.event.ActionListener {
	DialogArgument adaptee;

	DialogArgument_jBCancel_actionAdapter(DialogArgument adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBCancel_actionPerformed(e);
	}
}

class DialogArgument_jBUp_actionAdapter implements
		java.awt.event.ActionListener {
	DialogArgument adaptee;

	DialogArgument_jBUp_actionAdapter(DialogArgument adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBUp_actionPerformed(e);
	}
}

class DialogArgument_jBDown_actionAdapter implements
		java.awt.event.ActionListener {
	DialogArgument adaptee;

	DialogArgument_jBDown_actionAdapter(DialogArgument adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBDown_actionPerformed(e);
	}
}

class DialogArgument_this_windowAdapter extends java.awt.event.WindowAdapter {
	DialogArgument adaptee;

	DialogArgument_this_windowAdapter(DialogArgument adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.this_windowClosing(e);
	}
}

class DialogArgument_jBCancel_focusAdapter extends java.awt.event.FocusAdapter {
	DialogArgument adaptee;

	DialogArgument_jBCancel_focusAdapter(DialogArgument adaptee) {
		this.adaptee = adaptee;
	}

	public void focusGained(FocusEvent e) {
		adaptee.jBCancel_focusGained(e);
	}
}