package com.raqsoft.ide.dfx.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.raqsoft.app.common.AppUtil;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.ide.common.GM;
import com.raqsoft.ide.common.resources.IdeCommonMessage;
import com.raqsoft.ide.dfx.resources.IdeDfxMessage;
import com.raqsoft.parallel.UnitContext;
import com.raqsoft.server.http.HttpContext;

/**
 * HTTP�������ļ����ô���
 * 
 * @author Joancy
 *
 */
public class DialogInputPort extends JDialog {
	private static final long serialVersionUID = 1L;
	JPanel jPanel1 = new JPanel();
	JPanel jPanel2 = new JPanel();
	FlowLayout flowLayout1 = new FlowLayout();
	JButton jBOK = new JButton();
	JButton jBCancel = new JButton();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel jLabel1 = new JLabel();
	JLabel lbHost = new JLabel("Host");
	JComboBox<String> cbHosts = new JComboBox<String>();
	JSpinner jSPort = new JSpinner();

	JLabel labelMaxLinks = new JLabel("���������");
	JSpinner jSMaxLinks = new JSpinner();
	JCheckBox cbAutoStart = new JCheckBox(IdeCommonMessage.get().getMessage(
			"dialogodbcconfig.autostart"));

	private int m_option = JOptionPane.CLOSED_OPTION;
	private MessageManager mm = IdeDfxMessage.get();
	HttpContext hc = null;
	JFrame parent;

	/**
	 * ���캯��
	 * @param parent ������
	 * @param title ����
	 */
	public DialogInputPort(JFrame parent, String title) {
		super(parent, title, true);
		this.parent = parent;
		try {
			setSize(350, 150);
			init();
			resetText();
			this.setResizable(false);
			GM.setDialogDefaultButton(this, jBOK, jBCancel);
		} catch (Exception ex) {
			GM.showException(ex);
		}
	}

	private void resetText() {
		jBOK.setText(IdeCommonMessage.get().getMessage("button.ok"));
		jBCancel.setText(IdeCommonMessage.get().getMessage("button.cancel"));
		lbHost.setText(IdeCommonMessage.get().getMessage(
				"dialogjdbcconfig.host"));
		labelMaxLinks.setText(IdeCommonMessage.get().getMessage(
				"dialogoptions.maxlinks")); // ���������
		jLabel1.setText(mm.getMessage("dialoginputport.inputport")); // �˿�
	}

	/**
	 * ��ȡ���ڶ���ѡ��
	 * @return ѡ��
	 */
	public int getOption() {
		return m_option;
	}

	private void loadHttp() {
		InputStream is = null;
		try {
			hc = new HttpContext(false);
			is = UnitContext.getUnitInputStream(HttpContext.HTTP_CONFIG_FILE);
			if (is == null) {
				return;
			}
			hc.load(is);
			is.close();
		} catch (Exception x) {
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

	private void init() throws Exception {
		jSPort.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		jSMaxLinks.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

		loadHttp();
		String[] allHosts = AppUtil.getLocalIps();
		for (int i = 0; i < allHosts.length; i++) {
			String host = allHosts[i];
			cbHosts.addItem(host);
		}
		cbHosts.setSelectedItem(hc.getHost());
		jSPort.setValue(hc.getPort());
		cbAutoStart.setSelected(hc.isAutoStart());
		jSMaxLinks.setValue(hc.getMaxLinks());

		flowLayout1.setAlignment(FlowLayout.RIGHT);
		jPanel1.setLayout(flowLayout1);
		jBOK.setMnemonic('O');
		jBOK.setText("ȷ��(O)");
		jBOK.addActionListener(new DialogInputPort_jBOK_actionAdapter(this));
		jBCancel.setMnemonic('C');
		jBCancel.setText("ȡ��(C)");
		jBCancel.addActionListener(new DialogInputPort_jBCancel_actionAdapter(
				this));
		jPanel2.setLayout(gridBagLayout1);
		jLabel1.setText("�˿�");
		this.addWindowListener(new DialogInputPort_this_windowAdapter(this));
		this.getContentPane().add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(jBOK, null);
		jPanel1.add(jBCancel, null);
		this.getContentPane().add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(lbHost, GM.getGBC(1, 1));
		GridBagConstraints gbc = GM.getGBC(1, 2, true);
		gbc.gridwidth = 3;
		jPanel2.add(cbHosts, gbc);
		jPanel2.add(jLabel1, GM.getGBC(2, 1));
		jPanel2.add(jSPort, GM.getGBC(2, 2, true));

		jPanel2.add(labelMaxLinks, GM.getGBC(2, 3));
		jPanel2.add(jSMaxLinks, GM.getGBC(2, 4, true));
		gbc = GM.getGBC(3, 2, true);
		gbc.gridwidth = 3;
		jPanel2.add(cbAutoStart, gbc);
	}

	private boolean save() {
		try {
			// HttpContext
			hc.setHost(cbHosts.getSelectedItem().toString());
			hc.setPort(((Number) jSPort.getValue()).intValue());
			hc.setMaxLinks(((Number) jSMaxLinks.getValue()).intValue());
			hc.setAutoStart(cbAutoStart.isSelected());

			String filePath = GM.getAbsolutePath("config/"
					+ HttpContext.HTTP_CONFIG_FILE);
			File f = new File(filePath);
			if (f.exists() && !f.canWrite()) {
				String msg = IdeCommonMessage.get().getMessage(
						"public.readonly", f.getName());
				throw new RQException(msg);
			}
			FileOutputStream fos = new FileOutputStream(f);
			hc.save(fos);
			fos.close();
			return true;
		} catch (Exception ex) {
			GM.showException(ex);
		}
		return false;
	}

	void jBOK_actionPerformed(ActionEvent e) {
		if (save()) {
			m_option = JOptionPane.OK_OPTION;
			GM.setWindowDimension(this);
			dispose();
		}
	}

	void jBCancel_actionPerformed(ActionEvent e) {
		GM.setWindowDimension(this);
		dispose();
	}

	void this_windowClosing(WindowEvent e) {
		GM.setWindowDimension(this);
		dispose();
	}
}

class DialogInputPort_jBOK_actionAdapter implements
		java.awt.event.ActionListener {
	DialogInputPort adaptee;

	DialogInputPort_jBOK_actionAdapter(DialogInputPort adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBOK_actionPerformed(e);
	}
}

class DialogInputPort_jBCancel_actionAdapter implements
		java.awt.event.ActionListener {
	DialogInputPort adaptee;

	DialogInputPort_jBCancel_actionAdapter(DialogInputPort adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBCancel_actionPerformed(e);
	}
}

class DialogInputPort_this_windowAdapter extends java.awt.event.WindowAdapter {
	DialogInputPort adaptee;

	DialogInputPort_this_windowAdapter(DialogInputPort adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.this_windowClosing(e);
	}
}