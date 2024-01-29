package com.scudata.parallel;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import com.scudata.common.Logger;
import com.scudata.common.RQException;
import com.scudata.dm.Env;
import com.scudata.dm.JobSpace;
import com.scudata.dm.JobSpaceManager;
import com.scudata.dm.Param;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;

/**
 * �ֻ��ķ��ʿͻ���
 * @author Joancy
 *
 */
public class UnitClient implements Serializable {
	private static final long serialVersionUID = 1L;

	String host = null;
	int port = 0;

	boolean isDispatchUC = false;
	// ȡ����
	transient SocketData socketData = null;
	/**
	 * ���ӳ�ʱʱ������λms
	 */
	private int connectTimeout = 5000;

	/**
	 * ���ݵ�ַ�Ͷ˿ڹ���һ���ֻ��ͻ���
	 * @param host ����IP
	 * @param port �˿ں�
	 */
	public UnitClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * ��������������(���磺  host:ip)����ֻ��ͻ���
	 * @param add ����������
	 */
	public UnitClient(String add) {
		if (add != null) {
			int index = add.lastIndexOf(':');
			if (index > 0) {
				host = add.substring(0, index).trim();
				port = Integer.parseInt(add.substring(index + 1).trim());
			}
		}
	}

	/**
	 * ���÷ֻ�Ϊ���ط���Ŀͻ���
	 * ע�⣺ֻ��ͨ��getClient����getRandomClient������ȡ��UC������Ϊtrue��
	 * Ϊtrue��ʾ��uc���ڶ�������ά������������ģ�ͣ�����ʱ���ٶ��У�����������ͷŵ����С�
	 */
	public void setDispatchable() {
		isDispatchUC = true;
	}

	/**
	 * �жϷֻ��Ƿ���ط�����ҵ
	 * @return ������true�����򷵻�false
	 */
	public boolean isDispatchable() {
		return isDispatchUC;
	}

	/**
	 * ��ȡ����IP
	 * @return IP��
	 */
	public String getHost() {
		return host;
	}

	/**
	 * ��ȡ�����˿ں�
	 * @return �˿ں�
	 */
	public int getPort() {
		return port;
	}

	/**
	 * ��¡һ���ֻ��ͻ���
	 * @return ��¡��ķֻ��ͻ���
	 */
	public UnitClient clone() {
		UnitClient uc = new UnitClient(host, port);
		return uc;
	}

	/**
	 * �ж������ֻ��ͻ����Ƿ����
	 * @param nodeHost ��һ���ֻ�IP
	 * @param nodePort ��һ���ֻ��˿ں�
	 * @return ��ͬʱ����true�����򷵻�false
	 */
	public boolean equals(String nodeHost, int nodePort) {
		return host.equalsIgnoreCase(nodeHost) && port == nodePort;
	}

	/**
	 * �жϵ�ǰ�ֻ��Ƿ��Ѿ��������ɷ���
	 * @return �ɷ��ʷ���true�����򷵻�false
	 */
	public boolean isReachable() {
		try {
			InetAddress address = InetAddress.getByName(host);
			boolean isArrived = address.isReachable(2000);
			return isArrived;
		} catch (Exception x) {
			Logger.debug("Ping " + host + " failure.", x);
			return false;
		}
	}

	/**
	 * ��鵱ǰ�ֻ��Ƿ���
	 * @return ���ʱ����true�����򷵻�false
	 */
	public boolean isAlive() {
		return isAlive(null);
	}

	/**
	 * �жϵ�ǰ�ֻ��Ƿ�λ�ڱ���
	 * @return �Ǳ��ػ�����true�����򷵻�false
	 */
	public boolean isEqualToLocal() {
		// callx֧�� :, ""д������ʹ�ñ����߳�
		if (host == null)
			return true;

		HostManager hostManager = HostManager.instance();
		return host.equals(hostManager.getHost())
				&& port == hostManager.getPort();
	}

	/**
	 * �жϷֻ��Ƿ������쳣�����쳣ԭ��д��ԭ�򻺳�reason
	 * @param reason ԭ�򻺳�
	 * @return ���ʱ����true�����򷵻�false
	 */
	public boolean isAlive(StringBuffer reason) {
		if (isEqualToLocal()) {
			return true;
		}
		SocketData sd = null;

		try {
			sd = newSocketData();
		} catch (Exception x) {
			if (reason != null) {
				if (!isReachable()) {
					reason.append(this + " is not exist.\n");
				} else {
					reason.append("UnitServer or UnitServerConsole is not started on "
							+ this + "\n");
				}
			}
			return false;
		} finally {
			if (sd != null) {
				try {
					sd.clientClose();
				} catch (Exception x) {
				}
				sd = null;
			}
		}
		return true;
	}

	/**
	 * ���ӵ��ֻ�
	 * @throws Exception ���ӳ���ʱ�׳��쳣
	 */
	public void connect() throws Exception {
		if (!isEqualToLocal()) {
			socketData = newSocketData();
		}
	}

	/**
	 * �ж��Ƿ��Ѿ����ӵ��ֻ�
	 * @return ���Ӻú󷵻�true�����򷵻�false
	 */
	public boolean isConnected() {
		return !isClosed() || isEqualToLocal();// �����߳�Ҳ������״̬
	}

	/**
	 * ���ֻ�д��һ������
	 * @param obj ���ݶ���
	 * @throws Exception д����ʱ�׳��쳣
	 */
	public void write(Object obj) throws Exception {
		socketData.write(obj);
	}

	/**
	 * �ӷֻ�����һ������
	 * @return ��������
	 * @throws Exception ������ʱ�׳��쳣
	 */
	public Object read() throws Exception {
		return socketData.read();
	}
	
	/**
	 * �������ӳ�ʱʱ������λ����
	 * @param timeout
	 */
	public void setConnectTimeout(int timeout){
		this.connectTimeout = timeout;
	}

	/**
	 * ����һ���µ�ͨѶ�׽���
	 * @return ͨѶ�׽���
	 * @throws Exception ����ʱ�׳��쳣
	 */
	public SocketData newSocketData() throws Exception {
		Socket s = new Socket();
		SocketData sd = new SocketData(s);
		InetSocketAddress isa = new InetSocketAddress(host, port);
		sd.connect(isa, connectTimeout);

		return sd;
	}

	/**
	 * ȡ����ǰ����
	 * @param taskId ������
	 * @param reason ȡ��ԭ��
	 */
	public void cancel(Integer taskId, String reason) {
		Request req = new Request(Request.DFX_CANCEL);
		req.setAttr(Request.CANCEL_TaskId, taskId);
		req.setAttr(Request.CANCEL_Reason, reason);
		try {
			sendByNewSocket(req);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	/**
	 * ֹͣ������
	 */
	public void shutDown() {
		Request req = new Request(Request.SERVER_SHUTDOWN);
		try {
			sendByNewSocket(req);
		} catch (Exception x) {
		}
	}

	/**
	 * �г���ǰ�ֻ��������ҵ��
	 * @return �����ҵ��
	 */
	public int getUnitMaxNum() {
		if (isEqualToLocal()) {
			return HostManager.maxTaskNum;
		}
		Request req = new Request(Request.SERVER_GETUNITS_MAXNUM);
		try {
			Response res = send(req);
			return (Integer) res.getResult();
		} catch (Exception x) {
			throw new RQException(x);
		}
	}

	/**
	 * ��ȡ�ֻ�������J���ڴ�����
	 * @param J ��������
	 * @return �ڴ�����
	 */
	public Integer getAreaNo(String J) {
		Request req = new Request(Request.SERVER_GETAREANO);
		req.setAttr(Request.GETAREANO_TaskName, J);
		try {
			Response res = sendByNewSocket(req);
			return (Integer) res.getResult();
		} catch (Exception x) {
			throw new RQException(x);
		}
	}

	/**
	 * spaceId - Param[]
	 * 
	 * @return HashMap
	 */
	public Table getEnvParamList() {
		Request req = new Request(Request.SERVER_LISTPARAM);
		try {
			Response res = sendByNewSocket(req);
			if (res.getException() != null) {
				throw res.getException();
			}
			return (Table) res.getResult();
		} catch (Exception x) {
			x.printStackTrace();
		}
		return null;
	}

	public Table getTaskList() {
		Request req = new Request(Request.SERVER_LISTTASK);
		try {
			Response res = sendByNewSocket(req);
			if (res.getException() != null) {
				throw res.getException();
			}
			return (Table) res.getResult();
		} catch (Exception x) {
			x.printStackTrace();
		}
		return null;
	}

	public void closeSpace(String spaceId) {
		if (!isAlive()) {
			return;
		}
		Request req = new Request(Request.SERVER_CLOSESPACE);
		req.setAttr(Request.CLOSESPACE_SpaceId, spaceId);
		try {
			Response res = sendByNewSocket(req);
			if (res.getException() != null) {
				throw res.getException();
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public void initNode(int i, int N, String j) throws Exception {
		Logger.debug("Before init zone: " + i + " on " + this);

		Request req = new Request(Request.ZONE_INITDFX);
		// req.setAttr(Request.EXECDFX_DfxName, "init.dfx");
		ArrayList<Object> args = new ArrayList<Object>();
		args.add(i);
		args.add(N);
		args.add(j);
		req.setAttr(Request.EXECDFX_ArgList, args);
		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		Logger.debug("Init zone: " + i + " on " + this + " OK.");
	}

	public int getMemoryTableLength(String spaceId, String table)
			throws Exception {
		Request req = new Request(Request.SERVER_GETTABLEMEMBERS);
		req.setAttr(Request.GETTABLEMEMBERS_SpaceId, spaceId);
		req.setAttr(Request.GETTABLEMEMBERS_TableName, table);
		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return ((Integer) res.getResult()).intValue();
	}

	/**
	 * ��ȡ����ռ���ڷֻ��ϵ�reduce���
	 * @param spaceId ����ռ��
	 * @return reduce��ļ�����
	 * @throws Exception
	 */
	public Object getReduceResult(String spaceId) throws Exception {
		Request req = new Request(Request.DFX_GET_REDUCE);
		req.setAttr(Request.GET_REDUCE_SpaceId, spaceId);
		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return res.getResult();
	}

	/**
	 * �ܷ�������񣬱�������ʱ�����ܵ��� connect(); canAcceptTask(); close();
	 * 
	 * @param count
	 *            Integer
	 * @throws Exception
	 * @return boolean
	 */
	private transient Request accTask = null;

	public int[] getTaskNums() throws Exception {
		if (accTask == null) {
			accTask = new Request(Request.SERVER_GETTASKNUMS);
		} else {
			accTask.setAction(Request.SERVER_GETTASKNUMS);
		}
		Response res = send(accTask);

		if (res.getException() != null) {
			throw res.getException();
		}
		return (int[]) res.getResult();
	}

	public static Sequence getMemoryTable(String spaceId, String tableName,
			String nodeDesc) throws Exception {
		JobSpace space = JobSpaceManager.getSpace(spaceId);
		Param param = space.getParam(tableName);
		if (param == null) {
			param = Env.getParam(tableName);
		}
		if (param == null) {
			throw new Exception("Table:" + tableName
					+ " is not exist in space:" + spaceId
					+ " or Env of machine:" + nodeDesc);
		}
		Sequence dimTable = (Sequence) param.getValue();
		if (dimTable == null) {
			throw new Exception("Table:" + tableName
					+ " can not be null in space:" + spaceId
					+ " or Env of machine:" + nodeDesc);
		}
		return dimTable;
	}

	/**
	 * ��ȡָ���ֻ����������е�������Ŀ
	 * 
	 * @return
	 */
	public int getCurrentTasks() {
		Request req = new Request(Request.SERVER_GETCONCURRENTCOUNT);

		try {
			Response res = sendByNewSocket(req);
			if (res.getException() != null) {
				throw res.getException();
			}
			return ((Integer) res.getResult()).intValue();
		} catch (Exception x) {
			throw new RQException(x);
		}
	}

	public Response sendByNewSocket(Request req) throws Exception {
		SocketData tmp = null;
		try {
			tmp = newSocketData();
			tmp.write(req);
			Response res = (Response) tmp.read();
			res.setFromHost(this.toString());
			return res;
		} finally {
			if (tmp != null) {
				tmp.clientClose();
			}
		}
	}

	public Response send(Request req) throws Exception {
		// Logger.debug(req);
		if (socketData == null) {
			connect();
		}
		socketData.write(req);
		Response res = (Response) socketData.read();
		res.setFromHost(this.toString());
		return res;
	}

	public Response send(UnitCommand command) {
		try {
			Request req = new Request(Request.UNITCOMMAND_EXE);
			req.setAttr(Request.EXE_Object, command);
			Response res = sendByNewSocket(req);
			// Logger.debug("after unitcmd");
			return res;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public boolean isClosed() {
		return (socketData == null || socketData.isClosed());
	}

	public void close() {
		if (socketData != null) {
			try {
				socketData.clientClose();
			} catch (Exception x) {
			}
			socketData = null;
		}
	}

	public static String parseHost(String hostDesc) {
		boolean isIp4 = (hostDesc.indexOf(".") > 0);
		int colon;
		if (isIp4) {
			colon = hostDesc.indexOf(":");
		} else {
			colon = hostDesc.lastIndexOf(":");
		}
		if (colon < 0) {
			return null;
		}
		return hostDesc.substring(0, colon);
	}

	public static int parsePort(String hostDesc) {
		boolean isIp4 = (hostDesc.indexOf(".") > 0);
		int colon;
		if (isIp4) {
			colon = hostDesc.indexOf(":");
		} else {
			colon = hostDesc.lastIndexOf(":");
		}
		if (colon < 0) {
			return -1;
		}
		int cPort = Integer.parseInt(hostDesc.substring(colon + 1));
		return cPort;
	}

	private transient String tmpString = null;

	public String toString() {
		if (host == null) {
			return "Local";
		}
		if (tmpString == null) {
			tmpString = host + ":" + port;
		}
		return tmpString;
	}

	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof UnitClient)) {
			return false;
		}
		UnitClient otherUc = (UnitClient) other;
		if (otherUc.getHost() == null) {// ���Ǳ���ʱ
			return (host == null);
		}
		return otherUc.getHost().equalsIgnoreCase(host)
				&& otherUc.getPort() == port;
	}

	// JDBC�����ӿڲ���
	public int JDBCConnect() throws Exception {
		Request req = new Request(Request.JDBC_CONNECT);
//		req.setAttr(Request.CONNECT_spaceID, spaceId);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Integer) res.getResult();
	}

	public Table JDBCGetTables(int connId, String tableNamePattern,
			boolean isPlus) throws Exception {
		Request req = new Request(Request.JDBC_GETTABLES);
		req.setAttr(Request.GETTABLES_connID, connId);
		req.setAttr(Request.GETTABLES_tableNamePattern, tableNamePattern);
		req.setAttr(Request.JDBC_ISPLUS, isPlus);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Table) res.getResult();
	}

	public Table JDBCGetColumns(int connId, String tableNamePattern,
			String columnNamePattern, boolean isPlus) throws Exception {
		Request req = new Request(Request.JDBC_GETCOLUMNS);
		req.setAttr(Request.GETCOLUMNS_connID, connId);
		req.setAttr(Request.GETCOLUMNS_tableNamePattern, tableNamePattern);
		req.setAttr(Request.GETCOLUMNS_columnNamePattern, columnNamePattern);
		req.setAttr(Request.JDBC_ISPLUS, isPlus);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Table) res.getResult();
	}

	public Table JDBCGetProcedures(int connId, String procedureNamePattern,
			boolean isPlus) throws Exception {
		Request req = new Request(Request.JDBC_GETPROCEDURES);
		req.setAttr(Request.GETPROC_connID, connId);
		req.setAttr(Request.GETPROC_procedureNamePattern, procedureNamePattern);
		req.setAttr(Request.JDBC_ISPLUS, isPlus);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Table) res.getResult();
	}

	public Table JDBCGetProcedureColumns(int connId,
			String procedureNamePattern, String columnNamePattern,
			boolean isPlus) throws Exception {
		Request req = new Request(Request.JDBC_GETPROCECOLUMNS);
		req.setAttr(Request.GETPROCCOLUMNS_connID, connId);
		req.setAttr(Request.GETPROCCOLUMNS_procedureNamePattern,
				procedureNamePattern);
		req.setAttr(Request.GETPROCCOLUMNS_columnNamePattern, columnNamePattern);
		req.setAttr(Request.JDBC_ISPLUS, isPlus);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Table) res.getResult();
	}

	public Table JDBCGetSplParams(int connId, String procedureNamePattern,
			boolean isPlus) throws Exception {
		Request req = new Request(Request.JDBC_GETSPLPARAMS);
		req.setAttr(Request.GETSPLPARAMS_connID, connId);
		req.setAttr(Request.GETSPLPARAMS_splPath, procedureNamePattern);
		req.setAttr(Request.JDBC_ISPLUS, isPlus);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Table) res.getResult();
	}

	public int JDBCPrepare(int connId, String cmd, Object[] args,
			Map<String, Object> envParams) throws Exception {
		Request req = new Request(Request.JDBC_PREPARE);
		req.setAttr(Request.PREPARE_connID, connId);
		req.setAttr(Request.PREPARE_CMD, cmd);
		req.setAttr(Request.PREPARE_Args, args);
		req.setAttr(Request.PREPARE_ENV, envParams);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Integer) res.getResult();
	}

	public Sequence JDBCExecute(int connId, int stateId) throws Exception {
		Request req = new Request(Request.JDBC_EXECUTE);
		req.setAttr(Request.EXECUTE_connID, connId);
		req.setAttr(Request.EXECUTE_stateID, stateId);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		Sequence results = (Sequence) res.getResult();
		return results;
	}

	public boolean JDBCCancel(int connId, int stateId) throws Exception {
		Request req = new Request(Request.JDBC_CANCEL);
		req.setAttr(Request.CANCEL_connID, connId);
		req.setAttr(Request.CANCEL_stateID, stateId);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Boolean) res.getResult();
	}

	public boolean JDBCCloseStatement(int connId, int stateId) throws Exception {
		Request req = new Request(Request.JDBC_CLOSESTATEMENT);
		req.setAttr(Request.CLOSE_connID, connId);
		req.setAttr(Request.CLOSE_stateID, stateId);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Boolean) res.getResult();
	}

	public boolean JDBCCloseConnection(int connId) throws Exception {
		Request req = new Request(Request.JDBC_CLOSECONNECTION);
		req.setAttr(Request.CLOSE_connID, connId);

		Response res = sendByNewSocket(req);
		if (res.getException() != null) {
			throw res.getException();
		}
		return (Boolean) res.getResult();
	}

	public static String getHostPath(String host) {
		String path = host.replaceAll("::", ".");
		path = path.replaceAll(":", ".");// ��ipv6��ð�Ż��ɵ�
		return path;
	}

}
