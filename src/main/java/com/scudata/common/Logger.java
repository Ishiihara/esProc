package com.scudata.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Properties;

import com.scudata.app.common.AppUtil;


/**
 * ʹ��SLF4J API-2.0.1ʵ����־���,Ҳ֧�ֲ�����slf4j,��ֱ��ʹ��ScudataLogger
 * @author Joancy
 *
 */
public class Logger {
//	��־����Ĵ�д��������Ӧ��properties�ļ��У��������泣������д������
	public static String OFF = ScudataLogger.OFF;
	public static String SEVERE = ScudataLogger.SEVERE;
	public static String WARNING = ScudataLogger.WARNING;
	public static String INFO = ScudataLogger.INFO;
	public static String DEBUG = ScudataLogger.DEBUG;

//	��־�����ڱ����ж�Ӧ�ļ����	
	public static int iDOLOG = ScudataLogger.iDOLOG;
	public static int iOFF = ScudataLogger.iOFF;
	public static int iSEVERE = ScudataLogger.iSEVERE;
	public static int iWARNING = ScudataLogger.iWARNING;
	public static int iINFO = ScudataLogger.iINFO;
	public static int iDEBUG = ScudataLogger.iDEBUG;
	
	public static boolean isExistSLF4J = detectSLF4J(); 


	private static boolean detectSLF4J() {
		try {
			Class cls = Class.forName("org.slf4j.LoggerFactory");
			return true;
		}catch(Exception x) {
			return false;
		}
	}
	
	private static void slf4jLog(int level,String msg) {
		try {
			Class<?> factoryClazz = Class.forName("org.slf4j.LoggerFactory");
			Method getLogger = factoryClazz.getMethod("getLogger", String.class);
			Object logger = getLogger.invoke(null,"ScudataLogger");

			//			Object logger = AppUtil.invokeMethod(cls, "getLogger", new Object[] {"ScudataLogger"});
			
			String method="debug";
			if(level==iINFO) {
				method = "info";
			}else if(level==iWARNING) {
				method="warn";
			}else if(level==iSEVERE) {
				method="error";
			}
			AppUtil.invokeMethod(logger, method, new Object[] {msg});
		}catch(Exception x) {
			x.printStackTrace();
		}
	}
	
	/**
	 * �г�����֧�ֵ���־������ı���д���������ڽ���༭�ȡ�
	 * @return	����ȫ����־������ַ�������
	 */
	public static String[] listLevelNames() {
		return ScudataLogger.listLevelNames();
	}

	/**
	 * ��ȡ��־�ı���д����Ӧ����־�����
	 * @param level	Ҫ��Ӧ����־����
	 * @return	��Ӧ����־�����
	 */
	public static int getLevel(String level) {
		return ScudataLogger.getLevel(level);
	}

	/**
	 * ��ȡ��־����ŵ��ı���д��
	 * @param level Ҫ��Ӧ����־�����
	 * @return	��Ӧ����־����
	 */
	public static String getLevelName(int level) {
		return ScudataLogger.getLevelName(level);
	}

	private static String toString(Object obj,Throwable t) {
		StringBuffer sb = new StringBuffer();
		if(obj!=null) {
			if(obj instanceof Throwable) {
				Throwable th = (Throwable)obj;
				sb.append(th.getMessage());
			}else {
				sb.append(obj);
			}
			sb.append(ScudataLogger.lineSeparator);
		}
		if (t != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}

	/**
	 * ���ڼ�¼�����е����س����ü������Ϣʱ�û�����鿴��־��ȷ������ԭ��
	 * @param msg ����¼����־��Ϣ��ͨ�����ڼ�����߲����쳣����ϸ����
	 * @param t	��Ӧ����ϸ�쳣
	 */
	public static void error(Object msg) {
		severe(msg);
	}
	public static void error(Object msg, Throwable t) {
		severe(msg,t);
	}

	public static void severe(Object msg) {
		if(msg instanceof Throwable) {
			severe(msg,(Throwable)msg);
		}else {
			severe(msg,null);
		}
	}
	public static void severe(Object msg, Throwable t) {
		if(isExistSLF4J) {
			slf4jLog(iSEVERE,toString(msg,t));
		}else {
			ScudataLogger.severe(msg, t);
		}
	}

	/**
	 * ͬwarning������������ǰ�汾�ļ��ݣ���Ҫ���ø÷�����
	 * @param msg
	 */
	public static void warn(Object msg) {
		warning(msg);
	}

	public static void warn(Object msg, Throwable t) {
		if(msg instanceof Throwable) {
			warning(msg,(Throwable)msg);
		}else {
			warning(msg,null);
		}
	}

	/**
	 * ��¼һ��������Ϣ
	 * @param msg	����¼����Ϣ
	 */
	public static void warning(Object msg) {
		warning(msg,null);
	}
	/**
	 * ��ϸ��¼һ��������Ϣ�Լ���Ӧ�쳣
	 * @param msg	����¼����Ϣ
	 * @param t	��Ӧ����ϸ�쳣
	 */
	public static void warning(Object msg, Throwable t) {
		if(isExistSLF4J) {
			slf4jLog(iWARNING,toString(msg,t));
		}else {
			ScudataLogger.warning(msg, t);
		}
	}


	/**
	 * ��ϸ��¼һ����ͨ��Ϣ�Լ���Ӧ�쳣
	 * @param msg	����¼����Ϣ
	 * @param t	��Ӧ����ϸ�쳣
	 */
	public static void info(Object msg, Throwable t) {
		if(isExistSLF4J) {
			slf4jLog(iINFO,toString(msg,t));
		}else {
			ScudataLogger.info(msg, t);
		}
	}

	/**
	 * �򵥼�¼һ����ͨ��Ϣ
	 * @param msg	����¼����Ϣ
	 */
	public static void info(Object msg) {
		if(msg instanceof Throwable) {
			info(msg,(Throwable)msg);
		}else {
			info(msg,null);
		}
	}

	/**
	 * ��ϸ��¼һ��������Ϣ�Լ���Ӧ�쳣
	 * @param msg	����¼����Ϣ
	 * @param t	��Ӧ����ϸ�쳣
	 */
	public static void debug(Object msg, Throwable t) {
		if(isExistSLF4J) {
			slf4jLog(iDEBUG,toString(msg,t));
		}else {
			ScudataLogger.debug(msg, t);
		}
	}

	/**
	 * �򵥼�¼һ��������Ϣ
	 * @param msg	����¼����Ϣ
	 */
	public static void debug(Object msg) {
		if(msg instanceof Throwable) {
			debug(msg,(Throwable)msg);
		}else {
			debug(msg,null);
		}
	}

	/**
	 * ǿ�Ƽ�¼��־�����slfû���������ֱ�ӵ���ScudataLoggerʵ��
	 * @param msg
	 */
	public static void doLog(Object msg) {
		ScudataLogger.doLog(msg);
	}
	public static void doLog(Object msg,Throwable t) {
		ScudataLogger.doLog(msg,t);
	}

	/**
	 * �жϵ�ǰ��־�Ŀ��ż����Ƿ�Ϊ����ģʽ
	 * @return �Ƿ����ģʽ
	 */
	public static boolean isDebugLevel() {
		return ScudataLogger.isDebugLevel();
	}

	/**
	 * ͨ�������£�����ģʽ��������м������־��������ǰ�汾���Ƿ���Լ���д������Ҫ���ø÷�����
	 * @return �Ƿ����ģʽ
	 */
	public static boolean isAllLevel() {
		return isDebugLevel();
	}

	/**
	 * ���õ�ǰ��־�ļ�¼����
	 * @param level	���ļ��������־��������
	 */
	public static void setLevel(String level) {
		ScudataLogger.setLevel(level);
	}
	
	/**
	 * ���õ�ǰ��־������
	 * @param logType ConfigConsts.LOG_DEFAULT,ConfigConsts.LOG_SLF
	 */
	public static void setLogType(String logType){
		
	}

	/**
	 * ��ȡ��ǰ��־��¼����
	 * @return	���ؼ�¼�����
	 */
	public static int getLevel() {
		return ScudataLogger.getLevel();
	}
	
	public static void setPropertyConfig(Properties p) throws Exception {
		ScudataLogger.setPropertyConfig(p);
	}
	
}
