package com.raqsoft.ide.dfx;

import com.raqsoft.ide.common.AppMenu;
import com.raqsoft.ide.common.GV;
import com.raqsoft.ide.common.PrjxAppToolBar;
import com.raqsoft.ide.common.ToolBarPropertyBase;
import com.raqsoft.ide.common.control.JWindowNames;
import com.raqsoft.ide.dfx.base.JTabbedParam;
import com.raqsoft.ide.dfx.base.PanelDfxWatch;
import com.raqsoft.ide.dfx.base.PanelValue;
import com.raqsoft.ide.dfx.control.DfxEditor;
import com.raqsoft.ide.dfx.dialog.DialogSearch;

/**
 * ������IDE�еĳ���
 *
 */
public class GVDfx extends GV {
	/**
	 * ����༭��
	 */
	public static DfxEditor dfxEditor = null;

	/**
	 * IDE���½ǵĶ��ǩ�ؼ�,���������������ʽ�ȱ�ǩҳ
	 */
	public static JTabbedParam tabParam = null;

	/**
	 * ��Ԫ��ֵ���
	 */
	public static PanelValue panelValue = null;

	/**
	 * �������ʽ�������
	 */
	public static PanelDfxWatch panelDfxWatch = null;

	/**
	 * �����Ի���
	 */
	public static DialogSearch searchDialog = null;

	/**
	 * ƥ��Ĵ�������
	 */
	public static JWindowNames matchWindow = null;

	/**
	 * ȡ�������˵�
	 * 
	 * @return
	 */
	public static AppMenu getDfxMenu() {
		appMenu = new MenuDfx();
		return appMenu;
	}

	/**
	 * ȡ������������
	 * 
	 * @return
	 */
	public static ToolBarDfx getDfxTool() {
		appTool = new ToolBarDfx();
		return (ToolBarDfx) appTool;
	}

	/**
	 * ȡ���Թ�����
	 * 
	 * @return
	 */
	public static ToolBarPropertyBase getDfxProperty() {
		toolBarProperty = new ToolBarProperty();
		return toolBarProperty;
	}

	/**
	 * ȡ�����˵������ļ���ʱ��
	 * 
	 * @return
	 */
	public static AppMenu getBaseMenu() {
		appMenu = new MenuBase();
		return appMenu;
	}

	/**
	 * ȡ���������������ļ���ʱ��
	 * 
	 * @return
	 */
	public static PrjxAppToolBar getBaseTool() {
		appTool = new ToolBarBase();
		return appTool;
	}

	/**
	 * ȡ�������Թ����������ļ���ʱ��
	 * 
	 * @return
	 */
	public static ToolBarPropertyBase getBaseProperty() {
		toolBarProperty = new ToolBarProperty();
		return toolBarProperty;
	}
}