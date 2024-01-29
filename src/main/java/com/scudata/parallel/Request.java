package com.scudata.parallel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ��������
 * �����Ϊn��,����Ϊȫ����д��ǰ׺Ϊ���ͣ���׺Ϊ�����ʶ ��������������ʶ��ǰ׺����׺Ϊ����ĸ��д�Ĳ�����ʶ
 */
public class Request implements Serializable {
	private static final long serialVersionUID = 559160970976632495L;

	// �������Ͷ���
	public static final int TYPE_SERVER = 0; // �������������
	public static final int TYPE_DFX = 10000; // ִ��dfx
	public static final int TYPE_CURSOR = 20000; // �α����
	public static final int TYPE_FILE = 30000; // Զ���ļ�����
	public static final int TYPE_PARTITION = 40000; // Զ�̷����ļ�����
	public static final int TYPE_ZONE = 50000; // �ڴ����������
	public static final int TYPE_UNITCOMMAND = 60000; // �����õ�UnitCommand
	public static final int TYPE_JDBC = 70000; // JDBC�ӿ�

	// �������������
	public static final int SERVER_SHUTDOWN = 1 + TYPE_SERVER; // ֹͣ������
	public static final int SERVER_LISTTASK = 2 + TYPE_SERVER; // �г������б�,Ӧ��Table
	public static final int SERVER_CANACCEPTTASK = 3 + TYPE_SERVER; // ̽��ֻ��ܷ��������Ӧ��boolean
	public static final int SERVER_GETTASKNUMS = 4 + TYPE_SERVER; // ��ȡ�ʺ���ҵ���͵�ǰ��ҵ����Ӧ��int[2]
	public static final String CANACCEPTTASK_DispatchedCount = "Dispatched count";// �Ѿ������л����Ϸ����˵�����������̨�����Ų�������������Ѿ������

	public static final int SERVER_LISTPARAM = 5 + TYPE_SERVER; // �г��������ȫ�ֱ�����,Ӧ��HashMap:
																// String
																// spaceId-
																// Param[]
	public static final int SERVER_GETCONCURRENTCOUNT = 6 + TYPE_SERVER; // �г��������������Ŀ,Ӧ��Integer

	// ����������˵�����������(�û�û�������رյĴ����������ϵ���Ĵ���)
	public static final int SERVER_CLOSESPACE = 10 + TYPE_SERVER; // �رշֻ��ı����ռ�
	public static final String CLOSESPACE_SpaceId = "Space id";

	public static final int SERVER_FETCHDIMS = 20 + TYPE_SERVER; // �ӷֻ���ȡά��,Ӧ��
																	// Sequence
	public static final String FETCHDIMS_SpaceId = "Space id";// String
	public static final String FETCHDIMS_DimVarName = "Dim var name";// String
	public static final String FETCHDIMS_KeySequence = "Key sequence";// Sequence
	public static final String FETCHDIMS_NewExps = "New exps";// String[]
	public static final String FETCHDIMS_NewNames = "New names";// String[]

	public static final int SERVER_FETCHCLUSTERTABLE = 21 + TYPE_SERVER; // �ӷֻ���ȡ��Ⱥ��,Ӧ��
																			// Sequence
	public static final String FETCHCLUSTERTABLE_SpaceId = "Space id";// String
	public static final String FETCHCLUSTERTABLE_TableName = "Table name";// String
	public static final String FETCHCLUSTERTABLE_Seqs = "Seqs";// int[],��ļ�¼���
	public static final String FETCHCLUSTERTABLE_KeySequence = "Key sequence";// Sequence
	public static final String FETCHCLUSTERTABLE_NewExps = "New exps";// String[]
	public static final String FETCHCLUSTERTABLE_NewNames = "New names";// String[]
	public static final String FETCHCLUSTERTABLE_Filter = "Filter";// String

	public static final int SERVER_GETTABLEMEMBERS = 22 + TYPE_SERVER; // ��ȡ�ֻ����ڴ��ĳ�Ա����,Ӧ��
																		// int
	public static final String GETTABLEMEMBERS_SpaceId = "Space id";// String
	public static final String GETTABLEMEMBERS_TableName = "Table name";// String

	public static final int SERVER_GETUNITS_MAXNUM = 50 + TYPE_SERVER; // �г���ǰ�ֻ��������ҵ��

	public static final int SERVER_GETAREANO = 54 + TYPE_SERVER; // ��ȡ�ֻ����ڴ�����
	public static final String GETAREANO_TaskName = "Task name";// String

	// DFX���
	public static final int DFX_TASK = 1 + TYPE_DFX; // ����һ��dfx����Ӧ��Integer�������
	public static final String TASK_DfxName = "Dfx name";
	public static final String TASK_ArgList = "Arg list";
	public static final String TASK_SpaceId = "Space id";
	public static final String TASK_IsProcessCaller = "TASK_IsProcessCaller";
	public static final String TASK_ProcessTaskId = "Process task id";
	public static final String TASK_Reduce = "Reduce";
	public static final String TASK_AccumulateLocation = "AccumulateLocation";
	public static final String TASK_CurrentLocation = "CurrentLocation";

	public static final int DFX_CALCULATE = 2 + TYPE_DFX; // ����һ������Ӧ��Object�������ִ�н��
	public static final String CALCULATE_TaskId = "Task id";

	public static final int DFX_CANCEL = 3 + TYPE_DFX; // ȡ��һ������ִ�е�dfx�ļ���Ӧ��boolean��ȡ���Ƿ�ɹ�
	public static final String CANCEL_TaskId = "Task id";
	public static final String CANCEL_Reason = "Task reason";

	public static final int DFX_GET_REDUCE = 4 + TYPE_DFX; // ��ȡ�����ڷֻ��ϵ�reduce�����Ӧ��Object
	public static final String GET_REDUCE_SpaceId = "Space id";

	// �α����
	public static final int CURSOR_METHOD = 1 + TYPE_CURSOR;// ִ���α��һ����������Ӧ��:Object�����ķ���ֵ
	public static final String METHOD_TaskId = "Task id";
	public static final String METHOD_ProxyId = "Proxy id";
	public static final String METHOD_MethodName = "Method name";
	public static final String METHOD_ArgValues = "Arg values";

	// Զ���ļ�����
	public static final int FILE_GETPROPERTY = 1 + TYPE_FILE; // ��ȡ�ļ���������ԣ�Ӧ��HashMap�ļ�����Ӧ����
	public static final String GETPROPERTY_FileName = "File name";
	public static final String GETPROPERTY_Opt = "Options";

	public static final int FILE_DELETE = 2 + TYPE_FILE; // ɾ��ָ���ļ���Ӧ��boolean
	public static final String DELETE_FileName = "File name";

	public static final int FILE_OPEN = 3 + TYPE_FILE; // ��Զ���ļ���Ӧ��int �ļ����
	public static final String OPEN_Partition = "Partition";
	public static final String OPEN_FileName = "File name";
	public static final String OPEN_Opt = "Opt";
	public static final String OPEN_IsAppend = "Is append";

	public static final int FILE_READ = 4 + TYPE_FILE; // ��ȡ�ļ���һ�����ݣ�Ӧ��byte[]
	public static final String READ_Handle = "handle";
	public static final String READ_BufferSize = "Buffer size";

	public static final int FILE_CLOSE = 5 + TYPE_FILE; // �ر��ļ������Ӧ��boolean
	public static final String CLOSE_Handle = "handle";

	public static final int FILE_WRITE = 6 + TYPE_FILE; // д�ļ���һ�����ݣ�Ӧ����
	public static final String WRITE_Handle = "handle";
	public static final String WRITE_Bytes = "bytes";

	public static final int FILE_POSITION = 7 + TYPE_FILE; // ��ȡ�������ļ��ĵ�ǰλ�ã�Ӧ��long��λ��
	public static final String POSITION_Handle = "handle";

	public static final int FILE_SETPOSITION = 8 + TYPE_FILE; // �����������ļ��ĵ�ǰλ�ã�Ӧ����
	public static final String SETPOSITION_Handle = "handle";
	public static final String SETPOSITION_Position = "position";

	public static final int FILE_TRYLOCK = 9 + TYPE_FILE; // ���������ļ���Ӧ�𣺲���ֵ
	public static final String TRYLOCK_Handle = "handle";

	public static final int FILE_FROM_HANDLE = 10 + TYPE_FILE; // ��Զ��������򿪵��ļ��У���������
	public static final String FROM_Handle = "From handle";
	public static final String FROM_Pos = "From position";

	public static final int FILE_LOCK = 11 + TYPE_FILE; // �����ļ���Ӧ�𣺲���ֵ
	public static final String LOCK_Handle = "handle";

	public static final int FILE_DIRECTREAD = 100 + TYPE_FILE; // ֱ�ӿ��ٶ�ȡ�ļ�
	public static final String DIRECTREAD_FileName = "fileName";
	public static final String DIRECTREAD_Partition = "partition";

	// Զ�̷����ļ�����
	public static final int PARTITION_LISTFILES = 2 + TYPE_PARTITION; // �г��ֻ����ļ���Ϣ��Ӧ��List<FileInfo>,�����������ļ��б�
	public static final String LISTFILES_Path = "path";

	public static final int PARTITION_DELETE = 3 + TYPE_PARTITION; // ɾ���ֻ����ļ�
	// public static final String DELETE_FileName = "FILE NAME";//�Լ���ͬ������
	public static final String DELETE_Option = "Option";

	public static final int PARTITION_UPLOAD = 8 + TYPE_PARTITION; // ���ֻ��ϴ�һ���ļ�
	public static final String UPLOAD_DstPath = "Dest path";
	public static final String UPLOAD_LastModified = "Last Modified";
	public static final String UPLOAD_IsMove = "Is move";// ������ƶ�ģʽ���򲻱Ƚ�LastModified
	public static final String UPLOAD_IsY = "Is y";// IsY״̬ʱ��ǿ�Ƹ��ǣ����򱨴�

	public static final int PARTITION_SYNCTO = 11 + TYPE_PARTITION; // ������·��p�µ��ļ�ͬ�����ֻ���machines
	public static final String SYNC_Machines = "Machines";
	public static final String SYNC_Path = "Path";

	public static final int PARTITION_MOVEFILE = 14 + TYPE_PARTITION; // MOVEFILE
	public static final String MOVEFILE_Machines = "machines";
	public static final String MOVEFILE_Filename = "file name";
	public static final String MOVEFILE_Partition = "Partition";
	public static final String MOVEFILE_DstPath = "Dest path";
	public static final String MOVEFILE_Option = "Option";

	public static final int PARTITION_UPLOAD_DFX = 20 + TYPE_PARTITION;
	// �������ϴ�һ���ļ����ֻ�����·��Env.getMainPath()�£��������޹أ�����Ҳ�Ǹ�����һ�����ϴ��ļ���������һ�£����ڷ�����������
	// ��������ϴ�����������ͬ��PARTITION_UPLOAD
	public static final String UPLOAD_DFX_RelativePath = "Relative Path";// �ϴ���Ŀ�Ļ��ĸ�·���£�ֱ���������·��ʱ���ò���Ϊ��
	public static final String UPLOAD_DFX_LastModified = UPLOAD_LastModified;

	public static final int PARTITION_UPLOAD_CTX = 30 + TYPE_PARTITION;
	// �������ϴ�һ����������ļ����ֻ�����·�����ϴ��н����Ż���ֻͬ���������޸ĵĲ���
	public static final String UPLOAD_FileSize = "File size";
	public static final String UPLOAD_FileType = "File_type";
	public static final String UPLOAD_BlockLinkInfo = "Block link info";
	public static final String UPLOAD_HasExtFile = "Has Ext File";
	public static final String UPLOAD_ExtFileLastModified = "Ext File Last Modified";

	// �ڴ����������
	public static final int ZONE_INITDFX = 2 + TYPE_ZONE; // �ڷֻ���ִ��һ�������ڴ�����dfx����,����boolean��ִ���Ƿ����
	public static final String EXECDFX_ArgList = "Arg list";
	public static final String EXECDFX_SpaceId = "Space id";

	// ����UnitCommand
	public static final int UNITCOMMAND_EXE = 1 + TYPE_UNITCOMMAND; // UnitCommand�������
	public static final String EXE_Object = "Command Object";

	// JDBC�ӿ�
	public static final int JDBC_CONNECT = 1 + TYPE_JDBC; // ��ȡ���Ӻţ��������Ӵ�������ֵ�����������Ӻ�
//	public static final String CONNECT_spaceID = "connect spaceId";// spaceId

	public static final int JDBC_PREPARE = 2 + TYPE_JDBC; // prepareStatement,����ֵ��������statement��
	public static final String PREPARE_connID = "prepare connId";// connId
	public static final String PREPARE_CMD = "prepare cmd";// JDBC cmds
	public static final String PREPARE_Args = "prepare args";// Object[]
	public static final String PREPARE_ENV = "prepare env";// Map<String,
															// Object> envParams
	public static final String PREPARE_ENV_SQLFIRST = "sqlfirst";
	public static final String PREPARE_ENV_GATEWAY = "gateway";

	public static final int JDBC_EXECUTE = 3 + TYPE_JDBC; // ִ��dfx,����ֵ�������飬�������
	public static final String EXECUTE_connID = "execute connId";// connId
	public static final String EXECUTE_stateID = "execute stateId";// stateId

	public static final int JDBC_CANCEL = 4 + TYPE_JDBC; // ȡ��ִ��dfx,����ֵ����
	public static final String CANCEL_connID = EXECUTE_connID;// connId
	public static final String CANCEL_stateID = EXECUTE_stateID;// stateId

	public static final String JDBC_ISPLUS = "isplus";

	public static final int JDBC_GETTABLES = 5 + TYPE_JDBC; // ��ȡ����Ϣ
	public static final String GETTABLES_connID = "getTables connId";// connId
	public static final String GETTABLES_tableNamePattern = "tableNamePattern";

	public static final int JDBC_GETCOLUMNS = 6 + TYPE_JDBC; // ��ȡ�ֶ���Ϣ
	public static final String GETCOLUMNS_connID = "getTableColumns connId";// connId
	public static final String GETCOLUMNS_tableNamePattern = "tableNamePattern";
	public static final String GETCOLUMNS_columnNamePattern = "columnNamePattern";

	public static final int JDBC_GETPROCEDURES = 7 + TYPE_JDBC; // ��ȡSPLX�ļ���Ϣ
	public static final String GETPROC_connID = "getProcedures connId";// connId
	public static final String GETPROC_procedureNamePattern = "procedureNamePattern";

	public static final int JDBC_GETPROCECOLUMNS = 8 + TYPE_JDBC; // ��ȡSPLX�ļ��Ĳ�����Ϣ
	public static final String GETPROCCOLUMNS_connID = "getProcedureColumns connId";// connId
	public static final String GETPROCCOLUMNS_procedureNamePattern = "procedureNamePattern";
	public static final String GETPROCCOLUMNS_columnNamePattern = "procedureColumnNamePattern";

	public static final int JDBC_GETSPLPARAMS = 9 + TYPE_JDBC; // ��ȡSPLX�ļ��Ĳ�����Ϣ
	public static final String GETSPLPARAMS_connID = "getProcedureColumns connId";// connId
	public static final String GETSPLPARAMS_splPath = "splPath";

	public static final int JDBC_CLOSESTATEMENT = 11 + TYPE_JDBC; // �ر�Statement
	public static final int JDBC_CLOSECONNECTION = 12 + TYPE_JDBC; // �ر�Connection
	public static final String CLOSE_connID = EXECUTE_connID;// connId
	public static final String CLOSE_stateID = EXECUTE_stateID;// stateId

	private int action;
	private Map attrs = new HashMap();

	/**
	 * ����һ������
	 * @param action ��������
	 */
	public Request(int action) {
		this.action = action;
	}

	/**
	 * �����Ƿ������
	 * @return true
	 */
	public boolean isShortConnectCmd() {
		return true;
	}

	/**
	 * ���ݵ�ǰ�������ţ�ȡ����������
	 * @return ��������
	 */
	public int getActionType() {
		if (action > TYPE_JDBC) {
			return TYPE_JDBC;
		}
		if (action > TYPE_UNITCOMMAND) {
			return TYPE_UNITCOMMAND;
		}
		if (action > TYPE_ZONE) {
			return TYPE_ZONE;
		}
		if (action > TYPE_PARTITION) {
			return TYPE_PARTITION;
		}
		if (action > TYPE_FILE) {
			return TYPE_FILE;
		}
		if (action > TYPE_CURSOR) {
			return TYPE_CURSOR;
		}
		if (action > TYPE_DFX) {
			return TYPE_DFX;
		}
		return TYPE_SERVER;
	}

	/**
	 * ȡ��������
	 * @return ������
	 */
	public int getAction() {
		return action;
	}

	/**
	 * ������������
	 * @param action ������
	 */
	public void setAction(int action) {
		this.action = action;
	}

	/**
	 * ȡ��������Ա�
	 * @return ���Ա�
	 */
	public Map getAttrs() {
		return attrs;
	}

	/**
	 * ȡ���������ֵ
	 * @param attr ����
	 * @return ����ֵ
	 */
	public Object getAttr(String attr) {
		return attrs.get(attr);
	}

	/**
	 * �������Ե�ֵ
	 * @param attr ����
	 * @param value ����ֵ
	 */
	public void setAttr(String attr, Object value) {
		attrs.put(attr, value);
	}

	private String getAttrString() {
		if (attrs.isEmpty())
			return "Attr is empty/";
		Iterator it = attrs.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		while (it.hasNext()) {
			sb.append("\r\n");
			String key = (String) it.next();
			sb.append(key);
			sb.append("=");
			Object val = attrs.get(key);
			sb.append(val);
		}
		return sb.toString();
	}

	/**
	 * ʵ��toString������Ϣ
	 */
	public String toString() {
		return "Request action:" + action + getAttrString();
	}
}
