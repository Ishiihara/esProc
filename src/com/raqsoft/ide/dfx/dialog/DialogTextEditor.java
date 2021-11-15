package com.raqsoft.ide.dfx.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.raqsoft.common.MessageManager;
import com.raqsoft.ide.common.ConfigOptions;
import com.raqsoft.ide.common.GC;
import com.raqsoft.ide.common.GM;
import com.raqsoft.ide.common.GV;
import com.raqsoft.ide.dfx.resources.IdeDfxMessage;

/**
 * �ı��༭�Ի���
 *
 */
public class DialogTextEditor extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	/**
	 * ȷ�ϰ�ť
	 */
	private JButton okButton = new JButton();
	/**
	 * ȡ����ť
	 */
	private JButton cancelButton = new JButton();

	/**
	 * �ı��༭�ؼ�
	 */
	protected RSyntaxTextArea textEditor = new RSyntaxTextArea() {
		private static final long serialVersionUID = 1L;

		public Rectangle modelToView(int pos) throws BadLocationException {
			try {
				return super.modelToView(pos);
			} catch (Exception ex) {
			}
			return null;
		}
	};

	/**
	 * �������
	 */
	protected RTextScrollPane spEditor = new RTextScrollPane(textEditor);
	/**
	 * ��������Դ������
	 */
	private MessageManager mm = IdeDfxMessage.get();
	/**
	 * �Զ����и�ѡ��
	 */
	private JCheckBox jCBLineWrap = new JCheckBox(
			mm.getMessage("dialogtexteditor.linewrap"));

	/**
	 * �˳�ѡ��
	 */
	private int option = JOptionPane.CLOSED_OPTION;

	/**
	 * ���캯��
	 */
	public DialogTextEditor() {
		this(true);
	}

	/**
	 * ���캯��
	 * 
	 * @param isEditable
	 *            �Ƿ���Ա༭
	 */
	public DialogTextEditor(boolean isEditable) {
		super(GV.appFrame, "", true);
		init();

		GM.setWindowToolSize(this);
		GM.setDialogDefaultButton(this, okButton, cancelButton);
		this.setResizable(true);
		jCBLineWrap.setSelected(ConfigOptions.bTextEditorLineWrap
				.booleanValue());
		textEditor.setLineWrap(jCBLineWrap.isSelected());
		if (!isEditable) {
			textEditor.setEditable(false);
			okButton.setVisible(false);
			setTitle(mm.getMessage("dialogtexteditor.title1"));
		} else {
			setTitle(mm.getMessage("dialogtexteditor.title"));
		}
	}

	/**
	 * ȡ�˳�ѡ��
	 * 
	 * @return
	 */
	public int getOption() {
		return option;
	}

	/**
	 * �����ı�
	 * 
	 * @param text
	 */
	public void setText(String text) {
		textEditor.setText(text);

	}

	/**
	 * ȡ�ı�
	 * 
	 * @return
	 */
	public String getText() {
		return textEditor.getText();
	}

	/**
	 * ��ʼ��
	 */
	private void init() {
		this.getContentPane().setLayout(new BorderLayout());
		okButton.addActionListener(this);
		okButton.setMnemonic('O');
		cancelButton.addActionListener(this);
		cancelButton.setMnemonic('C');
		okButton.setText(mm.getMessage("button.ok"));
		cancelButton.setText(mm.getMessage("button.cancel"));
		JPanel panelSouth = new JPanel(new GridBagLayout());
		panelSouth.add(jCBLineWrap, GM.getGBC(0, 0));
		panelSouth.add(new JLabel(""), GM.getGBC(0, 1, true));
		panelSouth.add(okButton, GM.getGBC(0, 2, false, false, 0));
		panelSouth.add(cancelButton, GM.getGBC(0, 3));
		this.getContentPane().add(panelSouth, BorderLayout.SOUTH);
		JPanel panelCenter = new JPanel(new BorderLayout());
		panelCenter.add(spEditor, BorderLayout.CENTER);
		this.getContentPane().add(panelCenter, BorderLayout.CENTER);
		textEditor.setEditable(true);
		textEditor.setCodeFoldingEnabled(true);
		textEditor.setFont(GC.font);
		textEditor.setToolTipText(mm.getMessage("toolbarproperty.cellexp")); // ��Ԫ�����ʽ
		textEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
		jCBLineWrap.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				textEditor.setLineWrap(jCBLineWrap.isSelected());
			}

		});
	}

	/**
	 * �رմ���
	 */
	private void closeWindow() {
		ConfigOptions.bTextEditorLineWrap = jCBLineWrap.isSelected();
		GM.setWindowDimension(this);
		dispose();
	}

	/**
	 * �ؼ��¼�
	 */
	public void actionPerformed(ActionEvent e) {
		Object c = e.getSource();
		if (c == okButton) {
			option = JOptionPane.OK_OPTION;
			closeWindow();
		} else if (c == cancelButton) {
			closeWindow();
		}
	}
}