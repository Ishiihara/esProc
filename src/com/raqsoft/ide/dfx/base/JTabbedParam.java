package com.raqsoft.ide.dfx.base;

import java.awt.Dimension;

import javax.swing.JTabbedPane;

import com.raqsoft.common.MessageManager;
import com.raqsoft.dm.Env;
import com.raqsoft.dm.ParamList;
import com.raqsoft.ide.common.DataSource;
import com.raqsoft.ide.common.GM;
import com.raqsoft.ide.common.GV;
import com.raqsoft.ide.common.control.PanelConsole;
import com.raqsoft.ide.common.resources.IdeCommonMessage;
import com.raqsoft.ide.dfx.GVDfx;
import com.raqsoft.ide.dfx.resources.IdeDfxMessage;

/**
 * IDE���½ǵĶ��ǩҳ��塣�б���������ʽ�ȱ�ǩҳ
 *
 */
public abstract class JTabbedParam extends JTabbedPane {
	private static final long serialVersionUID = 1L;

	/**
	 * Common��Դ������
	 */
	private MessageManager mm = IdeCommonMessage.get();

	/**
	 * �������
	 */
	private final String STR_CS_VAR = mm.getMessage("jtabbedparam.csvar");

	/**
	 * ����ռ����
	 */
	private final String STR_SPACE_VAR = mm.getMessage("jtabbedparam.spacevar");

	/**
	 * ȫ�ֱ���
	 */
	private final String STR_GB_VAR = mm.getMessage("jtabbedparam.globalvar");

	/**
	 * �鿴����ʽ
	 */
	private final String STR_WATCH = mm.getMessage("jtabbedparam.watch");

	/**
	 * ����Դ
	 */
	private final String STR_DB = mm.getMessage("jtabbedparam.db");

	/**
	 * ���
	 */
	private final String STR_CONSOLE = IdeDfxMessage.get().getMessage(
			"dfx.tabconsole");

	/**
	 * �������ؼ�
	 */
	private TableVar tableCsVar = new TableVar() {
		private static final long serialVersionUID = 1L;

		public void select(Object val, String varName) {
			selectVar(val, varName);
		}
	};

	/**
	 * ����ռ�������ؼ�
	 */
	private JTableJobSpace tableSpaceVar = new JTableJobSpace() {
		private static final long serialVersionUID = 1L;

		public void select(Object val, String varName) {
			selectVar(val, varName);
		}
	};

	/**
	 * ȫ�ֱ������ؼ�
	 */
	private TableVar tableGbVar = new TableVar() {
		private static final long serialVersionUID = 1L;

		public void select(Object val, String varName) {
			selectVar(val, varName);
		}
	};

	/**
	 * ѡ���ֶ����
	 */
	private PanelSelectField psf = new PanelSelectField();
	/**
	 * �������
	 */
	private PanelConsole panelConsole;

	/**
	 * ���캯��
	 */
	public JTabbedParam() {
		this.setMinimumSize(new Dimension(0, 0));
		try {
			addTab(STR_CS_VAR, tableCsVar);
			addTab(STR_SPACE_VAR, tableSpaceVar);
			addTab(STR_GB_VAR, tableGbVar);
			addTab(STR_WATCH, GVDfx.panelDfxWatch);
			resetEnv();
		} catch (Exception e) {
			GM.showException(e);
		}
	}

	/**
	 * ���û���
	 */
	public void resetEnv() {
		boolean allClosed = true;
		if (GV.dsModel != null) {
			DataSource ds;
			for (int i = 0; i < GV.dsModel.getSize(); i++) {
				ds = (DataSource) GV.dsModel.get(i);
				if (ds != null && !ds.isClosed()) {
					allClosed = false;
					break;
				}
			}
		}
		int index = getTabIndex(STR_DB);
		if (allClosed) {
			if (index > -1) {
				this.remove(index);
			}
		} else {
			if (index < 0) {
				addTab(STR_DB, psf);
			}
			psf.resetEnv();
		}
	}

	/**
	 * ȡ������
	 * 
	 * @return
	 */
	public PanelConsole getPanelConsole() {
		return panelConsole;
	}

	/**
	 * �����������Ƿ����
	 * 
	 * @param isVisible
	 */
	public void consoleVisible(boolean isVisible) {
		int index = getTabIndex(STR_CONSOLE);
		if (isVisible) {
			if (index < 0) {
				if (panelConsole == null) {
					panelConsole = new PanelConsole(GV.console, true);
				} else {
					GV.console.clear();
				}
				addTab(STR_CONSOLE, panelConsole);
				showConsoleTab();
			}
		} else {
			if (index > -1) {
				this.remove(index);
			}
		}
	}

	/**
	 * ��ʾ���ҳ
	 */
	public void showConsoleTab() {
		int index = getTabIndex(STR_CONSOLE);
		if (index > -1)
			this.setSelectedIndex(index);
	}

	/**
	 * ȡָ�����Ʊ�ǩ�����
	 * 
	 * @param tabName
	 *            ��ǩ����
	 * @return
	 */
	private int getTabIndex(String tabName) {
		int count = getTabCount();
		for (int i = 0; i < count; i++) {
			if (getTitleAt(i).equals(tabName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * ѡ�����
	 * 
	 * @param val
	 * @param varName
	 */
	public abstract void selectVar(Object val, String varName);

	/**
	 * ���ò����б�
	 * 
	 * @param pl �����б�
	 */
	public void resetParamList(ParamList pl) {
		tableCsVar.setParamList(pl);
		tableSpaceVar.resetJobSpaces();
		tableGbVar.setParamList(Env.getParamList());
	}

}