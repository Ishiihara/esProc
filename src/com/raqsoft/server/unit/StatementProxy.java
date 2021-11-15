package com.raqsoft.server.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.raqsoft.common.Logger;
import com.raqsoft.common.StringUtils;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.cursor.ICursor;
import com.raqsoft.parallel.ITask;
import com.raqsoft.parallel.RemoteCursor;
import com.raqsoft.parallel.RemoteCursorProxy;
import com.raqsoft.parallel.RemoteCursorProxyManager;
import com.raqsoft.parallel.UnitContext;
import com.raqsoft.server.IProxy;

/**
 * Statement ������
 * 
 * @author Joancy
 *
 */
public class StatementProxy extends IProxy implements ITask {
	String cmd = null;
	ArrayList params = null;

	JdbcTask task = null;
	Context ctx;
	RemoteCursorProxyManager rcpm = null;

	/**
	 * ����һ��Statement������
	 * @param cp ���Ӵ���
	 * @param id �������
	 * @param cmd ִ�е�����
	 * @param params �����б�
	 * @param envParams ��������
	 * @throws Exception
	 */
	public StatementProxy(ConnectionProxy cp, int id, String cmd,
			ArrayList<Object> params, Map<String, Object> envParams)
			throws Exception {
		super(cp, id);
		this.cmd = cmd;
		if (!StringUtils.isValidString(cmd)) {
			throw new Exception("Prepare statement cmd is empty!");
		}
		Logger.debug("StatementProxy cmd:\r\n" + cmd);
		this.params = params;
		this.ctx = cp.getContext();
		task = new JdbcTask(cmd, params, ctx, envParams);
		access();
	}

	/**
	 * ��ȡ���Ӵ�����
	 * @return ���Ӵ�����
	 */
	public ConnectionProxy getConnectionProxy() {
		return (ConnectionProxy) getParent();
	}

	/**
	 * ��ȡҪִ�е�����
	 * @return ����
	 */
	public String getCmd() {
		return cmd;
	}

	/**
	 * ��ȡ����ֵ
	 * @return �����б�
	 */
	public List<String> getParams() {
		return params;
	}

	/**
	 * ��ȡ��������α����������
	 */
	public RemoteCursorProxyManager getCursorManager() {
		if (rcpm == null) {
			rcpm = new RemoteCursorProxyManager(this);
		}
		return rcpm;
	}

	/**
	 * ִ�е�ǰ����
	 * @return ������
	 * @throws Exception
	 */
	public Sequence execute() throws Exception {
		Sequence seq = task.execute();
		if (seq == null) {
			return null;
		}
		Sequence results = new Sequence();
		UnitServer server = UnitServer.instance;
		UnitContext uc = server.getUnitContext();

		for (int i = 1; i <= seq.length(); i++) {
			Object tmp = seq.get(i);
			if (tmp instanceof ICursor) {
				int proxyId = UnitServer.nextId();
				rcpm = getCursorManager();
				RemoteCursorProxy rcp = new RemoteCursorProxy(rcpm,
						(ICursor) tmp, proxyId);
				rcpm.addProxy(rcp);
				RemoteCursor rc = new RemoteCursor(uc.getLocalHost(),
						uc.getLocalPort(), getId(), proxyId);
				ctx.addResource(rc);
				results.add(rc);
			} else {
				results.add(tmp);
			}
		}

		return results;
	}

	/**
	 * ȡ����ǰ����
	 * @return �ɹ�ȡ������true
	 * @throws Exception
	 */
	public boolean cancel() throws Exception {
		task.cancel();
		return true;
	}

	/**
	 * �رյ�ǰ������
	 */
	public void close() {
	}

	/**
	 * ʵ��toString�ӿ�
	 */
	public String toString() {
		return "Statement " + getId();
	}

	/**
	 * ��ȡ����ID
	 */
	public int getTaskID() {
		return getId();
	}

}