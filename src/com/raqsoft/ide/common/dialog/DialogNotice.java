package com.raqsoft.ide.common.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.raqsoft.ide.common.GM;
import com.raqsoft.ide.common.resources.IdeCommonMessage;

/**
 * ������ʾ�Ի���
 */
public class DialogNotice extends RQDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * ���캯��
	 * 
	 * @param owner
	 *            �����
	 * @param message
	 *            ��ʾ��Ϣ
	 */
	public DialogNotice(JFrame owner, String message) {
		super(owner, "��ʾ", 400, 120);
		init(message);
		setTitle(IdeCommonMessage.get().getMessage("dialognotice.title"));
	}

	/**
	 * �����Ƿ���ʾ
	 * 
	 * @param notice
	 */
	public void setNotice(boolean notice) {
		jCBNotNotice.setSelected(!notice);
	}

	/**
	 * ȡ�Ƿ���ʾ
	 * 
	 * @return
	 */
	public boolean isNotice() {
		return !jCBNotNotice.isSelected();
	}

	/**
	 * ��ʼ���ؼ�
	 * 
	 * @param message
	 *            ��ʾ��Ϣ
	 */
	private void init(String message) {
		JTextArea jtext = new JTextArea();
		jtext.setLineWrap(true);
		jtext.setText(message);
		JScrollPane jsp = new JScrollPane(jtext);
		jsp.setBorder(null);
		panelCenter.add(jsp, BorderLayout.CENTER);
		jtext.setBackground(new JLabel().getBackground());
		jtext.setEditable(false);
		jtext.setBorder(null);
		panelSouth.removeAll();
		panelSouth.setLayout(new GridBagLayout());
		panelSouth.add(jCBNotNotice, GM.getGBC(0, 0));
		panelSouth.add(new JLabel(), GM.getGBC(0, 1, true));
		panelSouth.add(jBOK, GM.getGBC(0, 2, false, false, 5));

		jCBNotNotice.setText(IdeCommonMessage.get().getMessage(
				"dialognotice.notnotice"));
		jCBNotNotice.setSelected(true);
	}

	/**
	 * ������ʾ��ѡ��
	 */
	private JCheckBox jCBNotNotice = new JCheckBox();
}