package com.raqsoft.ide.common.dialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.raqsoft.common.DBConfig;
import com.raqsoft.common.MessageManager;
import com.raqsoft.common.ODBCUtil;
import com.raqsoft.ide.common.DataSource;
import com.raqsoft.ide.common.GM;
import com.raqsoft.ide.common.GV;
import com.raqsoft.ide.common.resources.IdeCommonMessage;
import com.raqsoft.ide.common.swing.JListEx;
import com.raqsoft.ide.common.swing.VFlowLayout;

/**
 * ѡ������Դ�Ի���
 *
 */
public class DialogSelectDataSource extends JDialog {
	private static final long serialVersionUID = 1L;

	/**
	 * ȡ����ť
	 */
	private JButton jBCancel = new JButton();

	/**
	 * ȷ�ϰ�ť
	 */
	private JButton jBOK = new JButton();

	/**
	 * ����Դ�б��ؼ�
	 */
	private JListEx listDS = new JListEx();

	/**
	 * TAB���
	 */
	private JTabbedPane tabMain = new JTabbedPane();

	/**
	 * ODBC����Դ�б�
	 */
	private JListEx listODBC = new JListEx();

	/**
	 * Common��Դ������
	 */
	private MessageManager mm = IdeCommonMessage.get();

	/**
	 * �˳�ѡ��
	 */
	private int m_option = JOptionPane.CANCEL_OPTION;

	/**
	 * ����Դ����ҳ
	 */
	private final byte TAB_CONFIG = 0;

	/**
	 * ODBCҳ
	 */
	private final byte TAB_ODBC = 1;

	/**
	 * �û���
	 */
	private JLabel labelUser = new JLabel();
	/**
	 * �û����ı���
	 */
	private JTextField jUser = new JTextField();

	/**
	 * ����
	 */
	private JLabel labelPwd = new JLabel();
	/**
	 * �����
	 */
	private JPasswordField jPassword = new JPasswordField();

	/**
	 * �Ƿ���ֹ�仯
	 */
	private boolean preventChange = false;

	/**
	 * ����
	 */
	private byte type = TYPE_ALL;
	/** ȫ�� */
	public static final byte TYPE_ALL = 0;
	/** SQL */
	public static final byte TYPE_SQL = 1;
	/** DQL */
	public static final byte TYPE_DQL = 2;

	/**
	 * ����Դ����
	 */
	private DataSource ds;

	/**
	 * ���캯��
	 */
	public DialogSelectDataSource() {
		this(TYPE_ALL);
	}

	/**
	 * ���캯��
	 * 
	 * @param type
	 *            ����
	 */
	public DialogSelectDataSource(byte type) {
		super(GV.appFrame, "", true);
		try {
			this.type = type;
			preventChange = true;
			init();
			setSize(400, 300);
			GM.setDialogDefaultButton(this, jBOK, jBCancel);
			resetLangText();
			setResizable(true);
		} catch (Exception ex) {
			GM.showException(ex);
		} finally {
			preventChange = false;
		}
	}

	/**
	 * ����������Դ
	 */
	private void resetLangText() {
		this.setTitle(mm.getMessage("dialogselectdatasource.title"));
		jBOK.setText(mm.getMessage("button.ok")); // ȷ��(O)
		jBCancel.setText(mm.getMessage("button.cancel")); // ȡ��(C)
		labelUser.setText(mm.getMessage("dialogodbcdatasource.user")); // �û���
		labelPwd.setText(mm.getMessage("dialogodbcdatasource.password")); // ����
	}

	/**
	 * ȡ�˳�ѡ��
	 * 
	 * @return
	 */
	public int getOption() {
		return m_option;
	}

	/**
	 * ȡ����Դ����
	 * 
	 * @return
	 */
	public DataSource getDataSource() {
		return ds;
	}

	/**
	 * ��ʼ��
	 */
	private void init() {
		JPanel jPanel2 = new JPanel();
		VFlowLayout verticalFlowLayout1 = new VFlowLayout();
		JScrollPane jScrollPane1 = new JScrollPane();
		JScrollPane jScrollPane2 = new JScrollPane();
		jPanel2.setLayout(verticalFlowLayout1);
		jBCancel.setMnemonic('C');
		jBCancel.setText("Cancel");
		jBCancel.addActionListener(new DialogSelectDataSource_jBCancel_actionAdapter(
				this));
		jBOK.setMnemonic('O');
		jBOK.setText("OK");
		jBOK.addActionListener(new DialogSelectDataSource_jBOK_actionAdapter(
				this));
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new DialogSelectDataSource_this_windowAdapter(
				this));
		tabMain.add(jScrollPane1, mm.getMessage("dialogselectdatasource.tab1"));
		JSplitPane spODBC = new JSplitPane();
		spODBC.setOrientation(JSplitPane.VERTICAL_SPLIT);
		spODBC.setOneTouchExpandable(true);
		spODBC.setDividerSize(8);
		spODBC.setDividerLocation(175);
		if (type != TYPE_DQL) {
			tabMain.add(spODBC, "ODBC");
		}
		spODBC.add(jScrollPane2, JSplitPane.TOP);
		JPanel panelBottom = new JPanel(new GridBagLayout());
		spODBC.add(panelBottom, JSplitPane.BOTTOM);
		panelBottom.add(labelUser, GM.getGBC(1, 1));
		panelBottom.add(jUser, GM.getGBC(1, 2, true));
		panelBottom.add(labelPwd, GM.getGBC(2, 1));
		panelBottom.add(jPassword, GM.getGBC(2, 2, true));
		jScrollPane2.getViewport().add(listODBC, null);
		jScrollPane1.getViewport().add(listDS, null);
		this.getContentPane().add(jPanel2, BorderLayout.EAST);
		jPanel2.add(jBOK, null);
		jPanel2.add(jBCancel, null);
		this.getContentPane().add(tabMain, BorderLayout.CENTER);

		listDS.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listODBC.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		Vector<DataSource> code = new Vector<DataSource>();
		Vector<String> disp = new Vector<String>();
		if (GV.dsModel != null) {
			Vector<String> dsNames = GV.dsModel.listNames();
			if (dsNames != null) {
				DataSource ds;
				for (int i = 0; i < dsNames.size(); i++) {
					ds = (DataSource) GV.dsModel.get(i);
					switch (type) {
					case TYPE_SQL:
						if (GM.isDataLogicDS(ds)) {
							continue;
						}
						break;
					case TYPE_DQL:
						if (!GM.isDataLogicDS(ds)) {
							continue;
						}
						break;
					}
					code.add(ds);
					disp.add(dsNames.get(i));
				}
			}
		}
		listDS.x_setData(code, disp);
		if (code.size() > 0) {
			listDS.setSelectedIndex(0);
		}

		ArrayList dsList = ODBCUtil.getDataSourcesName(ODBCUtil.SYS_DSN
				| ODBCUtil.USER_DSN);
		listODBC.setListData(dsList.toArray());
		listODBC.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (preventChange) {
					return;
				}
				odbcChanged();
			}
		});
		if (dsList.size() > 0) {
			listODBC.setSelectedIndex(0);
		}
		odbcChanged();
	}

	/**
	 * ODBC��ѡ��仯
	 */
	private void odbcChanged() {
		boolean isDSSelected = !listODBC.isSelectionEmpty();
		labelUser.setEnabled(isDSSelected);
		jUser.setEnabled(isDSSelected);
		labelPwd.setEnabled(isDSSelected);
		jPassword.setEnabled(isDSSelected);
	}

	/**
	 * ȡ����ť�¼�
	 * 
	 * @param e
	 */
	void jBCancel_actionPerformed(ActionEvent e) {
		GM.setWindowDimension(this);
		dispose();
	}

	/**
	 * ȷ�ϰ�ť�¼�
	 * 
	 * @param e
	 */
	void jBOK_actionPerformed(ActionEvent e) {
		switch (tabMain.getSelectedIndex()) {
		case TAB_CONFIG:
			if (listDS.isSelectionEmpty()) {
				JOptionPane.showMessageDialog(GV.appFrame,
						mm.getMessage("dialogselectdatasource.selectds"));
				return;
			}
			ds = (DataSource) listDS.x_getSelectedValues()[0];
			break;
		case TAB_ODBC:
			if (listODBC.isSelectionEmpty()) {
				JOptionPane.showMessageDialog(GV.appFrame,
						mm.getMessage("dialogselectdatasource.selectds"));
				return;
			}
			String odbcName = listODBC.getSelectedItems();
			DBConfig config = new DBConfig();
			config.setName(odbcName);
			config.setDBCharset(DialogODBCDataSource.ODBC_CHARSET);
			config.setClientCharset(DialogODBCDataSource.ODBC_CHARSET);
			config.setDriver(DialogODBCDataSource.ODBC_DRIVER);
			config.setUrl(DialogODBCDataSource.ODBC_URL + odbcName);
			config.setUser(jUser.getText());
			config.setPassword(new String(jPassword.getPassword()));
			ds = new DataSource(config);
			break;
		}
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			ds.getDBSession();
		} catch (Throwable x) {
			GM.showException(GM.handleDSException(ds, x));
			return;
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		m_option = JOptionPane.OK_OPTION;
		GM.setWindowDimension(this);
		dispose();
	}

	/**
	 * ���ڹر��¼�
	 * 
	 * @param e
	 */
	void this_windowClosing(WindowEvent e) {
		GM.setWindowDimension(this);
		dispose();
	}
}

class DialogSelectDataSource_jBCancel_actionAdapter implements
		java.awt.event.ActionListener {
	DialogSelectDataSource adaptee;

	DialogSelectDataSource_jBCancel_actionAdapter(DialogSelectDataSource adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBCancel_actionPerformed(e);
	}
}

class DialogSelectDataSource_jBOK_actionAdapter implements
		java.awt.event.ActionListener {
	DialogSelectDataSource adaptee;

	DialogSelectDataSource_jBOK_actionAdapter(DialogSelectDataSource adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jBOK_actionPerformed(e);
	}
}

class DialogSelectDataSource_this_windowAdapter extends
		java.awt.event.WindowAdapter {
	DialogSelectDataSource adaptee;

	DialogSelectDataSource_this_windowAdapter(DialogSelectDataSource adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.this_windowClosing(e);
	}
}