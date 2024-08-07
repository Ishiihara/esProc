package com.scudata.common;

/**
 * ����Դ������صĻ���������
 */
public class DBTypes {
	/**
	 * δ֪����Դ����
	 */
	public static final int UNKNOWN = 0;

	public static final int ORACLE = 1;

	public static final int SQLSVR = 2;

	public static final int SYBASE = 3;

	public static final int SQLANY = 4;

	public static final int INFMIX = 5;

	public static final int FOXPRO = 6;

	public static final int ACCESS = 7;

	public static final int FOXBAS = 8;

	public static final int DB2 = 9;

	public static final int MYSQL = 10;

	public static final int KINGBASE = 11;

	public static final int DERBY = 12;

	public static final int HSQL = 13;

	public static final int TERADATA = 14;

	public static final int POSTGRES = 15;

	public static final int DATALOGIC = 16;

	public static final int IMPALA = 17;

	public static final int HIVE = 18;

	public static final int GREENPLUM = 19;

	public static final int DBONE = 20;

	public static final int ESPROC = 21;

	public static final int DAMENG = 22;

	public static final int ESSBASE = 101;

	/**
	 * �г����п�֧�ֵ�����Դ����
	 */
	public static int[] listSupportedDBTypes() {
		return new int[] { ORACLE, SQLSVR, SYBASE, SQLANY, INFMIX, FOXPRO,
				ACCESS, FOXBAS, DB2, MYSQL, KINGBASE, DERBY, HSQL, TERADATA,
				POSTGRES, DATALOGIC, IMPALA, HIVE, GREENPLUM, DBONE, ESPROC,
				DAMENG, ESSBASE };
	}

	/**
	 * �г����п�֧�ֵĹ�ϵ������Դ��������
	 */
	public static String[] listSupportedRDBNames() {
		return new String[] { "ORACLE", "SQLSVR", "SYBASE", "SQLANY", "INFMIX",
				"FOXPRO", "ACCESS", "FOXBAS", "DB2", "MYSQL", "KINGBASE",
				"DERBY", "HSQL", "TERADATA", "POSTGRES", "DATALOGIC", "IMPALA",
				"HIVE", "GREENPLUM", "DBONE", "ESPROC", "DAMENG" };

	}

	/**
	 * �г����п�֧�ֵ����ݲֿ���������
	 */
	public static String[] listSupportedMDBNames() {
		return new String[] { "ESSBASE" };

	}

	/**
	 * �г����п�֧�ֵ�����Դ��������
	 */
	public static String[] listSupportedDBNames() {
		return new String[] { "ORACLE", "SQLSVR", "SYBASE", "SQLANY", "INFMIX",
				"FOXPRO", "ACCESS", "FOXBAS", "DB2", "MYSQL", "KINGBASE",
				"DERBY", "HSQL", "TERADATA", "POSTGRES", "DATALOGIC", "IMPALA",
				"HIVE", "GREENPLUM", "DBONE", "ESPROC", "DAMENG", "ESSBASE" };

	}

	/**
	 * ��������Դ��������ȡ����Դ����
	 * 
	 * @param dbTypeName
	 *            ����Դ��������
	 * @return ����Դ����
	 */
	public static int getDBType(String dbTypeName) {
		if (dbTypeName == null) {
			return UNKNOWN;
		}
		String[] dbNames = listSupportedDBNames();
		int[] dbTypes = listSupportedDBTypes();
		String dtn = dbTypeName.trim();
		for (int i = 0; i < dbNames.length; i++) {
			if (dbNames[i].equalsIgnoreCase(dtn)) {
				return dbTypes[i];
			}
		}
		return UNKNOWN;
	}

	/**
	 * ��������Դ����ȡ����Դ��������
	 * 
	 * @param dbType
	 *            ����Դ����
	 * @return ����Դ��������
	 */
	public static String getDBTypeName(int dbType) {
		int[] dbTypes = listSupportedDBTypes();
		String[] dbNames = listSupportedDBNames();
		for (int i = 0; i < dbTypes.length; i++) {
			if (dbType == dbTypes[i]) {
				return dbNames[i];
			}
		}
		return "UNKNOWN";
	}

	/**
	 * �������ݿ����Ͳ������ڲ��ַ���ʹ�õ�����
	 * 
	 * @param dbType
	 *            ���ݿ�����
	 * @return �����ַ�(����˫)
	 */
	public static char getQuotation(int dbType) {
		return (dbType == INFMIX || dbType == FOXPRO || dbType == ACCESS || dbType == FOXBAS) ? '\"'
				: '\'';
	}

	/**
	 * �������ݿ�����Ϊ�ַ����������
	 * 
	 * @param dbType
	 *            ���ݿ�����
	 * @param ��Ҫ������ŵĴ�
	 * @return ������ŵĴ�
	 */
	public static String addQuotation(int dbType, String value) {
		if (value == null || value.length() == 0) {
			return "null";
		}
		char quote = '\'';
		if (dbType == INFMIX || dbType == FOXPRO || dbType == FOXBAS) {
			quote = '\"';
		}

		int len = value.length();
		StringBuffer sb = new StringBuffer(len + 10);
		sb.append(quote);
		for (int i = 0; i < len; i++) {
			char ch = value.charAt(i);
			if (ch == quote) {
				sb.append(ch);
			}
			sb.append(ch);
		}
		sb.append(quote);
		return sb.toString();
	}

	/**
	 * �����󣨱��ֶεȣ����ƺ��������ַ�ʱ�����޶���
	 */
	public static String getLeftTilde(int dbType) {
		if (dbType == SQLSVR || dbType == ACCESS)
			return "[";
		if (dbType == MYSQL)
			return "`";
		if (dbType == INFMIX)
			return "\'";
		return "\"";
	}

	/**
	 * �����󣨱��ֶεȣ����ƺ��������ַ�ʱ�����޶���
	 */
	public static String getRightTilde(int dbType) {
		if (dbType == SQLSVR || dbType == ACCESS)
			return "]";
		if (dbType == MYSQL)
			return "`";
		if (dbType == INFMIX)
			return "\'";
		return "\"";
	}

	// ���º����ڹ��ʻ�������»᲻��ȷ
	/**
	 * �������ݿ����ͽ��ַ���ת��SQL��ʹ�õ��ַ����͵Ĵ�
	 * 
	 * @param dbType
	 *            ���ݿ�����
	 * @param value
	 *            ��Ҫת���Ĵ�
	 * @return ת����Ĵ�
	 */

	/*
	 * public static String getCharConst( int dbType, String value ) { return
	 * addQuotation( dbType, value ); }
	 */
	/**
	 * �������ݿ����ͽ��ַ���(Ҫ���ʽΪyyyy-MM-dd)ת��SQL��ʹ�õ��������͵Ĵ�
	 * 
	 * @param dbType
	 *            ���ݿ�����
	 * @param value
	 *            ��Ҫת���Ĵ�
	 * @return ת����Ĵ�
	 */
	/*
	 * public static String getDateConst( int dbType, String value ) { if (
	 * value == null || value.length() == 0 ) { return "null"; } switch ( dbType
	 * ) { case ORACLE: return "to_date('" + value + "','yyyy-mm-dd')"; default:
	 * return "'" + value + "'"; } }
	 */

	/**
	 * �������ݿ����ͽ��ַ���(Ҫ���ʽΪHH:mm:ss)ת��SQL��ʹ�õ�ʱ�����͵Ĵ�
	 * 
	 * @param dbType
	 *            ���ݿ�����
	 * @param value
	 *            ��Ҫת���Ĵ�
	 * @return ת����Ĵ�
	 */
	/*
	 * public static String getTimeConst( int dbType, String value ) { if (
	 * value == null || value.length() == 0 ) { return "null"; }
	 * 
	 * switch ( dbType ) { case ORACLE: return "to_date('" + value +
	 * "','hh24:mi:ss')"; default: return "'" + value + "'"; } }
	 */

	/**
	 * �������ݿ����ͽ��ַ���(Ҫ���ʽΪ"yyyy-MM-dd HH:mm:ss")ת��SQL��ʹ�� ������ʱ�����͵Ĵ�
	 * 
	 * @param dbType
	 *            ���ݿ�����
	 * @param value
	 *            ��Ҫת���Ĵ�
	 * @return ת����Ĵ�
	 */
	/*
	 * public static String getTimestampConst( int dbType, String value ) { if (
	 * value == null || value.length() == 0 ) { return "null"; } switch ( dbType
	 * ) { case ORACLE: return "to_date('" + value +
	 * "','yyyy-mm-dd hh24:mi:ss')"; default: return "'" + value + "'"; } }
	 */
	/**
	 * ��ȡ�ַ�����ָ�����ݿ��¶�Ӧ��SQL���ʽ
	 * 
	 * @param dbType
	 *            ���ݿ�����
	 * @param value
	 *            ����ֵ����
	 * @param datatype
	 *            ��������
	 * @return ����SQL�ı��ʽ
	 */
	/*
	 * public static String getDBConst( int dbType, Object value, int datatype )
	 * { if ( value == null ) { return "null"; }
	 * 
	 * switch ( datatype ) { case Types.CHAR: case Types.VARCHAR: case
	 * Types.LONGVARCHAR: return getCharConst( dbType, value.toString() ); case
	 * Types.DATE: return getDateConst( dbType, value.toString() ); case
	 * Types.TIME: if ( value instanceof java.sql.Time ) { DateFormat df =
	 * DateFormat.getTimeInstance( DateFormat.MEDIUM ); value = df.format( value
	 * ); } return getTimeConst( dbType, value.toString() ); case
	 * Types.TIMESTAMP: if ( value instanceof java.sql.Timestamp ) { DateFormat
	 * df = DateFormat.getDateTimeInstance( DateFormat.DEFAULT,
	 * DateFormat.MEDIUM ); value = df.format( value ); } return
	 * getTimestampConst( dbType, value.toString() ); case Types.BIGINT: case
	 * Types.DECIMAL: case Types.DOUBLE: case Types.FLOAT: case Types.INTEGER:
	 * case Types.NUMERIC: case Types.REAL: case Types.SMALLINT: case
	 * Types.TINYINT: case Types.BIT: String s = value.toString(); return (
	 * s.length() == 0 ) ? "null" : s; default: System.out.println(
	 * "The column dataType is " + datatype ); throw new
	 * IllegalArgumentException(); } }
	 */

}
