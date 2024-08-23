package com.scudata.dm;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import com.scudata.common.DateFormatFactory;
import com.scudata.common.IOUtils;
import com.scudata.common.ISessionFactory;
import com.scudata.common.Logger;
import com.scudata.expression.Expression;

/**
 * ȫ�ֻ�������
 * @author WangXiaoJun
 *
 */
public class Env {
	private static ParamList paramList = new ParamList(); // ���ȫ�̱���
	private static Map<String, ISessionFactory> dbsfs;
	private static String mainPath; // ��Ŀ¼
	private static String tempPath; // ��ʱĿ¼
	private static String[] paths; // dfx����Ŀ¼
	private static String defCharsetName = System.getProperty("file.encoding"); // "GBK"
																				// UTF-8
	//private static String localHost;
	//private static int localPort;
	private static int parallel = (Runtime.getRuntime().availableProcessors()+1)/2;
	private static int csParallel = (Runtime.getRuntime().availableProcessors()+1)/2;

	public static int FILE_BUFSIZE = 1024 * 64; // ���ļ�ʱ��Ĭ�ϻ�������С
	public static int DEFAULT_HASHCAPACITY = 204047; // �α����������Ĭ�Ϲ�ϣ������������ѡ������
	public static int MAX_HASHCAPACITY = 22949669; // �α��������������ϣ������
	public static final int INITGROUPSIZE = 8191; // �������ĳ�ʼ��С


	public static int BLOCK_SIZE = 1024 * 1024; // �ļ������С����С4096��2^n��n>=16

	// ȱʧֵ���壬�����ִ�Сд���Ǵ�����ʱ�����ͳ�null
	private static String[] nullStrings = new String[] { "nan", "null", "n/a" };
	
	// �Ƿ��Ǩע�͸��еĵ�Ԫ��
	private static boolean adjustNoteCell = true;

	private static String DEFAULT_TASK = "_default_task_";
	private static Map<String,Integer> areaNo = Collections.synchronizedMap(new HashMap<String,Integer>());

	private static ServletContext sc = null;

	private static Comparator<String> unicodeCollator = new Comparator<String>() {
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	};

	private Env() {
	}

	/**
	 * ��ȡ����j�ĵ��ڴ�����
	 * @param j ��������
	 * @return �ڴ�����,�ڴ����Ų�����ʱ������null
	 */
	public static Integer getAreaNo(String j) {
		if(j==null){
			j = DEFAULT_TASK;
		}
		return areaNo.get(j);
	}

	/**
	 * ��������j���ڴ�����Ϊi
	 * @param j ��������
	 * @param i �ڴ�����
	 */
	public static void setAreaNo(String j, int i) {
		if(j==null){
			j = DEFAULT_TASK;
		}
		if(i==0){
			areaNo.remove(j);
		}else{
			areaNo.put(j, i);
		}
	}

	/**
	 * ��ȡ�ڴ�����ӳ���
	 * @return ӳ���
	 */
	public static Map<String, Integer> getAreaNo(){
		return areaNo;
	}
	/**
	 * ���ز����б�
	 * 
	 * @return ParamList
	 */
	public static ParamList getParamList() {
		return paramList;
	}

	/**
	 * ������ȡ����
	 * 
	 * @param name ������
	 * @return DataStruct
	 */
	public static Param getParam(String name) {
		synchronized (paramList) {
			return paramList.get(name);
		}
	}

	/**
	 * ��ӱ���
	 * 
	 * @param param ����
	 */
	public static void addParam(Param param) {
		synchronized (paramList) {
			paramList.add(param);
		}
	}

	/**
	 * ������ɾ������
	 * 
	 * @param name String
	 * @return Param
	 */
	public static Param removeParam(String name) {
		synchronized (paramList) {
			return paramList.remove(name);
		}
	}

	/**
	 * ɾ�����б���
	 */
	public static void clearParam() {
		areaNo.clear();
		synchronized (paramList) {
			paramList.clear();
		}
	}

	/**
	 * ���ñ�����ֵ��������������������һ��
	 * 
	 * @param name String ������
	 * @param value Object ����ֵ
	 */
	public static void setParamValue(String name, Object value) {
		synchronized (paramList) {
			Param p = paramList.get(name);
			if (p == null) {
				paramList.add(new Param(name, Param.VAR, value));
			} else {
				p.setValue(value);
			}
		}
	}
	
	// ����ס�����ټ���x��Ϊ��֧��ͬ����env(v,v+n)
	public static Object setParamValue(String name, Expression x, Context ctx) {
		Param p;
		synchronized (paramList) {
			p = paramList.get(name);
			if (p == null) {
				p = new Param(name, Param.VAR, null);
				paramList.add(p);
			}
		}
		
		synchronized(p) {
			Object value = x.calculate(ctx);
			p.setValue(value);
			return value;
		}
	}

	/**
	 * ����Ĭ���ַ�������
	 * 
	 * @param name String
	 */
	public static void setDefaultChartsetName(String name) {
		defCharsetName = name;
	}

	/**
	 * ����Ĭ���ַ�������
	 * 
	 * @return String
	 */
	public static String getDefaultCharsetName() {
		return defCharsetName;
	}

	/**
	 * ������Ŀ¼
	 * 
	 * @param path String
	 */
	public static void setMainPath(String path) {
		mainPath = path;
	}

	/**
	 * ������Ŀ¼
	 * 
	 * @return String
	 */
	public static String getMainPath() {
		return mainPath;
	}

	/**
	 * ������ʱĿ¼
	 * 
	 * @param path
	 *            String
	 */
	public static void setTempPath(String path) {
		tempPath = path;
	}

	/**
	 * ������ʱĿ¼
	 * 
	 * @return String
	 */
	public static String getTempPath() {
		return tempPath;
	}
	
	/**
	 * ȡ��һ����ʱ�ļ���
	 * 		��ʱ�ļ����ļ���ΪRQT+��ǰ����ʱ�䣬��չ��Ϊ�������(Ŀ���ǰѲ�ͬ���͵��ļ��ֿ����Է������)
	 * @param exName	��ʱ�ļ���չ��(���������)
	 * @return	����һ����ʱ�ļ���·����
	 */
	public static String getTempPathName(String exName) {
		Date date = new Date();
		String resPath = new String();
		if (null != getTempPath())
			resPath += getTempPath();
		String temp = date.toString();
		temp = temp.replaceAll(":", "");
		temp = temp.replaceAll(" ", "");
		resPath += temp;
		return resPath+'.'+exName;
	}

	public static String[] getPaths() {
		return paths;
	}

	public static void setPaths(String[] paths) {
		Env.paths = paths;
	}

	/**
	 * ��ȡ���ݿ����ӹ���
	 * 
	 * @param name String ���ݿ�����
	 * @return ISessionFactory ���ݿ����ӹ���
	 */
	public static ISessionFactory getDBSessionFactory(String name) {
		if (dbsfs == null)
			return null;
		return dbsfs.get(name);
	}

	/**
	 * �������ݿ����ӹ���
	 * 
	 * @param name String ���ݿ�����
	 * @param sf ISessionFactory ���ݿ����ӹ���
	 */
	public static void setDBSessionFactory(String name, ISessionFactory sf) {
		if (dbsfs == null) {
			dbsfs = new HashMap<String, ISessionFactory>();
		}
		
		dbsfs.put(name, sf);
	}

	/**
	 * ������ɾ�����ݿ����ӹ���
	 * 
	 * @param name String
	 */
	public static void deleteDBSessionFactory(String name) {
		if (dbsfs != null) {
			dbsfs.remove(name);
		}
	}

	/**
	 * ɾ�����е����ݿ����ӹ���
	 */
	public static void clearDBSessionFactories() {
		if (dbsfs != null) {
			dbsfs.clear();
		}
	}

	/**
	 * ȡ���ݿ����ӹ���ӳ��
	 * @return
	 */
	public static Map<String, ISessionFactory> getDBSessionFactories() {
		return dbsfs;
	}

	/**
	 * ��ȡʱ���ʽ
	 * 
	 * @return String ʱ���ʽ�趨
	 */
	public static String getTimeFormat() {
		return DateFormatFactory.getDefaultTimeFormat();
	}

	/**
	 * ����ʱ���ʽ
	 * 
	 * @param format String ʱ���ʽ�趨
	 */
	public static void setTimeFormat(String format) {
		DateFormatFactory.setDefaultTimeFormat(format);
	}

	/**
	 * ��ȡ���ڸ�ʽ
	 * 
	 * @return String ���ڸ�ʽ�趨
	 */
	public static String getDateFormat() {
		return DateFormatFactory.getDefaultDateFormat();
	}

	/**
	 * �������ڸ�ʽ
	 * 
	 * @param format
	 *            String ���ڸ�ʽ�趨
	 */
	public static void setDateFormat(String format) {
		DateFormatFactory.setDefaultDateFormat(format);
	}

	/**
	 * ��ȡ����ʱ���ʽ
	 * 
	 * @return String ����ʱ���ʽ�趨
	 */
	public static String getDateTimeFormat() {
		return DateFormatFactory.getDefaultDateTimeFormat();
	}

	/**
	 * ��������ʱ���ʽ
	 * 
	 * @param format String ����ʱ���ʽ�趨
	 */
	public static void setDateTimeFormat(String format) {
		DateFormatFactory.setDefaultDateTimeFormat(format);
	}

	/**
	 * �������ݱȽ���ʹ�õıȽ���
	 * 
	 * @return Comparator<String>
	 */
	public static Comparator<String> getCollator() {
		return unicodeCollator;
	}

	/**
	 * ȡ��д�ļ���������С
	 * 
	 * @return int
	 */
	public static int getFileBufSize() {
		return FILE_BUFSIZE;
	}

	/**
	 * ȡ���ļ��鲢���ļ���������С
	 * 
	 * @param fcount int
	 * @return int
	 */
	public static int getMergeFileBufSize(int fcount) {
		Runtime rt = Runtime.getRuntime();
		long size = (rt.maxMemory() - rt.totalMemory() + rt.freeMemory() - 1024 * 1024 * 128)
				/ fcount / 2;
		if (size >= FILE_BUFSIZE)
			return FILE_BUFSIZE;

		int n = (int) size / 1024;
		if (n > 1) {
			return n * 1024;
		} else {
			return 1024;
		}
	}

	/**
	 * ���ö�д�ļ���������С
	 * 
	 * @param size
	 *            int
	 */
	public static void setFileBufSize(int size) {
		if (size < 4096) {
			FILE_BUFSIZE = 4096;
		} else {
			FILE_BUFSIZE = size;
		}
	}

	/**
	 * ȡ�޷�֪������ʱ��Ĭ�Ϲ�ϣ������
	 * 
	 * @return int
	 */
	public static int getDefaultHashCapacity() {
		return DEFAULT_HASHCAPACITY;
	}

	/**
	 * ���޷�֪������ʱ��Ĭ�Ϲ�ϣ������
	 * 
	 * @param n int
	 */
	public static void setDefaultHashCapacity(int n) {
		DEFAULT_HASHCAPACITY = n;
	}

	public static int getMaxHashCapacity() {
		return MAX_HASHCAPACITY;
	}

	public static void setMaxHashCapacity(int n) {
		MAX_HASHCAPACITY = n;
	}

	/**
	 * ȡ�ʺϲ�������������ͨ���������߳���
	 * @return int
	 */
	public static int getParallelNum() {
		return parallel > 1 ? parallel : 1;
	}

	/**
	 * ���ʺϲ�����
	 * @param num
	 */
	public static void setParallelNum(int num) {
		parallel = num;
	}
	
	/**
	 * ȡȱʡ�Ķ�·�α�·�������ڲ�����·�α�
	 * @return
	 */
	public static int getCursorParallelNum() {
		return csParallel > 1 ? csParallel : 1;
	}
	
	/**
	 * ����ȱʡ�Ķ�·�α�·��
	 * @param num
	 */
	public static void setCursorParallelNum(int num) {
		csParallel = num;
	}

	/**
	 * ȡwebӦ��������
	 */
	public static ServletContext getApplication() {
		return sc;
	}

	/**
	 * ����webӦ��������
	 */
	public static void setApplication(ServletContext app) {
		sc = app;
	}

	/**
	 * ��webӦ���ﰴѰַ·������ָ����Դ��URL
	 */
	public static URL getResourceFromApp(String fileName) {
		if (sc == null)
			return null;

		URL u = null;
		if (IOUtils.isAbsolutePath(fileName)) {
			try {
				return sc.getResource(fileName.replace('\\', '/'));
			} catch (Exception e) {
				return null;
			}
		}
		if (paths != null) {
			for (String path : paths) {
				File f = new File(path, fileName);
				try {
					u = sc.getResource(f.getPath().replace('\\', '/'));
				} catch (Exception e) {
				}
				if (u != null)
					return u;
			}// for
		}// if paths
		return null;
	}

	/**
	 * ��webӦ�û�ȡָ����Դ��������
	 */
	public static InputStream getStreamFromApp(String fileName) {
		URL u = getResourceFromApp(fileName);
		if (u == null)
			return null;
		try {
			InputStream is = u.openStream();
			Logger.debug(u);
			return is;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * ȡ�ļ������С
	 * 
	 * @return
	 */
	public static int getBlockSize() {
		return BLOCK_SIZE;
	}

	/**
	 * �����ļ������С
	 * 
	 * @param int size 2^n��n>=16
	 */
	public static void setBlockSize(int size) {
		BLOCK_SIZE = size;
	}

	/**
	 * ȡȱʧֵ���壬�����ִ�Сд���Ǵ�����ʱ�����ͳ�null
	 * 
	 * @return
	 */
	public static String[] getNullStrings() {
		return nullStrings;
	}

	/**
	 * ����ȱʧֵ���壬�����ִ�Сд���Ǵ�����ʱ�����ͳ�null
	 * 
	 * @param nss
	 */
	public static void setNullStrings(String[] nss) {
		nullStrings = nss;
	}
	
	/**
	 * ȡָ���������ļ�
	 * @param partition ����
	 * @param name �ļ���
	 * @return
	 */
	public static File getPartitionFile(int partition, String name) {
		//String path = getMappingPath(partition);
		//return new File(path, name);
		FileObject fo = new FileObject(name);
		fo.setPartition(partition);
		return fo.getLocalFile().file();
	}

	/**
	 * �Ƿ��Ǩע�͸��еĴ�
	 * @return
	 */
	public static boolean isAdjustNoteCell() {
		return adjustNoteCell;
	}

	/**
	 * �����Ƿ��Ǩע�͸��еĴ�
	 * @param adjustNoteCell
	 */
	public static void setAdjustNoteCell(boolean adjustNoteCell) {
		Env.adjustNoteCell = adjustNoteCell;
	}
}
