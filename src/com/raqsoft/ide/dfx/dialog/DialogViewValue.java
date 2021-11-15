package com.raqsoft.ide.dfx.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.raqsoft.common.MessageManager;
import com.raqsoft.ide.common.GC;
import com.raqsoft.ide.common.GM;
import com.raqsoft.ide.common.GV;
import com.raqsoft.ide.dfx.base.JTableView;
import com.raqsoft.ide.dfx.resources.IdeDfxMessage;

/**
 * �鿴����ֵ�Ի���
 *
 */
public class DialogViewValue extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	/**
	 * ���˰�ť
	 */
	private JButton jBUndo = new JButton();
	/**
	 * ǰ����ť
	 */
	private JButton jBRedo = new JButton();
	/**
	 * ���ư�ť
	 */
	private JButton jBCopy = new JButton();
	/**
	 * �رհ�ť
	 */
	private JButton jBClose = new JButton();

	/**
	 * ����ؼ�
	 */
	private JTableView tableValue = new JTableView() {
		private static final long serialVersionUID = 1L;

		public void refreshValueButton() {
			jBUndo.setEnabled(tableValue.canUndo());
			jBRedo.setEnabled(tableValue.canRedo());
			jBCopy.setEnabled(!tableValue.valueIsNull());
		}

	};

	/**
	 * ��������Դ������
	 */
	private MessageManager dfxMM = IdeDfxMessage.get();

	/**
	 * ���캯��
	 */
	public DialogViewValue() {
		super(GV.appFrame, IdeDfxMessage.get().getMessage(
				"dialogviewvalue.title"), true);// �鿴����ֵ
		init();
		setSize(400, 300);
		GM.setDialogDefaultButton(this, new JButton(), jBClose);
		this.setResizable(true);
	}

	/**
	 * ����Ҫ�鿴��ֵ
	 * 
	 * @param value
	 */
	public void setValue(Object value) {
		tableValue.setValue(value);
	}

	/**
	 * ��ʼ��
	 */
	private void init() {
		this.getContentPane().setLayout(new BorderLayout());
		JPanel panelNorth = new JPanel(new GridBagLayout());
		panelNorth.add(new JLabel(), getGBC(1, 1, true));
		jBUndo.setIcon(GM.getImageIcon(GC.IMAGES_PATH + "m_pmtundo.gif"));
		jBUndo.setToolTipText(dfxMM.getMessage("panelvaluebar.undo")); // ����
		initButton(jBUndo);
		panelNorth.add(jBUndo, getGBC(1, 2));
		jBRedo.setIcon(GM.getImageIcon(GC.IMAGES_PATH + "m_pmtredo.gif"));
		jBRedo.setToolTipText(dfxMM.getMessage("panelvaluebar.redo")); // ǰ��
		initButton(jBRedo);
		panelNorth.add(jBRedo, getGBC(1, 3));
		jBCopy.setIcon(GM.getMenuImageIcon("copy"));
		jBCopy.setToolTipText(dfxMM.getMessage("panelvaluebar.copy")); // ����
		initButton(jBCopy);
		panelNorth.add(jBCopy, getGBC(1, 4));
		jBClose.setIcon(GM.getMenuImageIcon("quit"));
		jBClose.setToolTipText(dfxMM.getMessage("panelvaluebar.quit")); // �˳�
		initButton(jBClose);
		jBClose.setEnabled(true);
		panelNorth.add(jBClose, getGBC(1, 5));
		this.getContentPane().add(panelNorth, BorderLayout.NORTH);
		this.getContentPane().add(new JScrollPane(tableValue),
				BorderLayout.CENTER);
	}

	/**
	 * ��ʼ����ť
	 * 
	 * @param button
	 */
	private void initButton(AbstractButton button) {
		Dimension bSize = new Dimension(25, 25);
		button.setMinimumSize(bSize);
		button.setMaximumSize(bSize);
		button.setPreferredSize(bSize);
		button.addActionListener(this);
		button.setEnabled(false);
	}

	/**
	 * ȡ���ɲ���
	 * 
	 * @param r
	 *            �к�
	 * @param c
	 *            �к�
	 * @return
	 */
	private GridBagConstraints getGBC(int r, int c) {
		return getGBC(r, c, false);
	}

	/**
	 * ȡ���ɲ���
	 *
	 * @param r
	 *            �к�
	 * @param c
	 *            �к�
	 * @param b
	 *            �Ƿ��������
	 * @return
	 */
	private GridBagConstraints getGBC(int r, int c, boolean b) {
		GridBagConstraints gbc = GM.getGBC(r, c, b);
		gbc.insets = new Insets(3, 3, 3, 3);
		return gbc;
	}

	/**
	 * �ؼ����¼�
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (jBUndo.equals(src)) {
			tableValue.undo();
		} else if (jBRedo.equals(src)) {
			tableValue.redo();
		} else if (jBCopy.equals(src)) {
			tableValue.copyValue();
		} else if (jBClose.equals(src)) {
			GM.setWindowDimension(this);
			dispose();
		}
	}
}