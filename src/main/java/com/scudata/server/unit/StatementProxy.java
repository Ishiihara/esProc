package com.scudata.server.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.scudata.common.Logger;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.dm.JobSpaceManager;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.parallel.ITask;
import com.scudata.parallel.RemoteCursor;
import com.scudata.parallel.RemoteCursorProxy;
import com.scudata.parallel.RemoteCursorProxyManager;
import com.scudata.parallel.TaskManager;
import com.scudata.parallel.UnitContext;
import com.scudata.server.IProxy;

/**
 * Statement ������
 * 
 * @author Joancy
 *
 */
public class StatementProxy extends IProxy implements ITask {
	String spaceId;
	String cmd = null;
	ArrayList params = null;

	JdbcTask task = null;
	Context ctx;
	RemoteCursorProxyManager rcpm = null;

	/**
	 * ����һ��Statement������
	 * @param cp ���Ӵ���
	 * @param id ������
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
		this.ctx = new Context(cp.getContext());
		spaceId = cp.getSpaceId();
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
	 * ��ȡ��������α���������
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
		JobSpaceManager.getSpace(spaceId).closeResource();
		TaskManager.delTask(getId());
		ctx = null;
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