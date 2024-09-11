package com.scudata.server.unit;

import java.util.List;

import com.scudata.common.Logger;
import com.scudata.common.UUID;
import com.scudata.dm.Context;
import com.scudata.dm.JobSpaceManager;
import com.scudata.server.ConnectionProxyManager;
import com.scudata.server.IProxy;
import com.scudata.util.DatabaseUtil;

/**
 * ���Ӵ������
 * һ�����Ӷ�Ӧһ��spaceId��statement֮�乲��spaceId���������
 * statement�ر�ʱ�����ͷ���Դ��connection�ر�ʱ���Źر�space 2024��9��11��
 * @author Joancy
 *
 */
public class ConnectionProxy extends IProxy
{
	String spaceId;
	Context context;
	boolean closed = false;

	/**
	 * ����һ�����Ӵ���
	 * @param cpm ���Ӵ��������
	 * @param id ������
	 * @param spaceId �ռ���
	 */
	public ConnectionProxy(ConnectionProxyManager cpm, int id){
		super(cpm, id);
		context = new Context();
		List<String> connectedDsNames = null;
		UnitServer us = UnitServer.instance;
		if( us != null ){
			if(us.getRaqsoftConfig()!=null){
				connectedDsNames = us.getRaqsoftConfig().getAutoConnectList();
			}
		}
		DatabaseUtil.connectAutoDBs(context, connectedDsNames);
		spaceId = UUID.randomUUID().toString();
		context.setJobSpace(JobSpaceManager.getSpace(spaceId));

		access();
		Logger.debug(this+" connected.");
	}
	
	public String getSpaceId() {
		return spaceId;
	}
	/**
	 * ����id��ȡStatement������
	 * @param id ������
	 * @return Statement������
	 * @throws Exception
	 */
	public StatementProxy getStatementProxy(int id) throws Exception{
		StatementProxy sp = (StatementProxy)getProxy(id); 
		if(sp==null){
			throw new Exception("Statement "+id+" is not exist or out of time!");
		}
		return sp; 
	}
	
	/**
	 * ��ȡ���㻷��������
	 * @return ����������
	 */
	public Context getContext(){
		return context;
	}
	
	/**
	 * �ж������Ƿ��ѹر�
	 * @return �رշ���true�����򷵻�false
	 */
	public boolean isClosed(){
		return closed;
	}
	
	/**
	 * �ص���ǰ���Ӵ�����
	 */
	public void close() {
		JobSpaceManager.closeSpace(spaceId);
		DatabaseUtil.closeAutoDBs(context);
		closed =  true;
		Logger.debug(this+" closed.");
	}

	/**
	 * ʵ��toString�ӿ�
	 */
	public String toString() {
		return "Connection "+getId();
	}
	
}