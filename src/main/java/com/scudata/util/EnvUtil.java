package com.scudata.util;

import com.scudata.common.ISessionFactory;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.JobSpace;
import com.scudata.dm.Param;
import com.scudata.dm.Sequence;

/**
 * ������صĹ�����
 * @author RunQian
 *
 */
public class EnvUtil {
	//private static final long G = 1024 * 1024 * 1024; // 1G�Ĵ�С
	private static final int FIELDSIZE = 50; // �����ڴ�ʱÿ���ֶ�ռ�õĿռ��С
	private static final int MAXRECORDCOUNT = 20000000; // �ڴ��б��������¼����
	private static double MAX_USEDMEMORY_PERCENT = 0.4;
	
	public static void setMaxUsedMemoryPercent(double d) {
		MAX_USEDMEMORY_PERCENT = d;
	}
	
	public static double getMaxUsedMemoryPercent() {
		return MAX_USEDMEMORY_PERCENT;
	}
	
	// ȡ��ǰ�����ڴ���Դ�Լ�����ɶ�������¼
	/**
	 * ȡ��ǰ�����ڴ��Լ���Դ�Ŷ�������¼
	 * @param fcount �ֶ���
	 * @return ��¼����
	 */
	public static int getCapacity(int fcount) {
		Runtime rt = Runtime.getRuntime();
		runGC(rt);
	
		long freeMemory = rt.maxMemory() - rt.totalMemory() + rt.freeMemory();
		long recordCount = freeMemory / fcount / FIELDSIZE / 3;
		if (recordCount > MAXRECORDCOUNT) {
			return MAXRECORDCOUNT;
		} else {
			return (int)recordCount;
		}
	}
	
	/**
	 * �����Ƿ��п����ڴ�������α����
	 * @param rt Runtime
	 * @param table �Ѷ�������
	 * @param readSize ÿ�ζ�������ռ���ڴ��С����׼ȷ��
	 * @return true�����Լ�������false���������ٶ���
	 */
	public static boolean memoryTest(Runtime rt, Sequence table, long readSize) {
		int len = table.length();
		if (len >= MAXRECORDCOUNT) return false;
		
		long maxUseMemory = (long)(rt.maxMemory() * MAX_USEDMEMORY_PERCENT);
		long usedMemory = rt.totalMemory() - rt.freeMemory();
		
		if (usedMemory > maxUseMemory || usedMemory + readSize > maxUseMemory) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * ִ�������ռ�
	 * @param rt Runtime
	 */
	public static void runGC(Runtime rt) {
		for (int i = 0; i < 2; ++i) {
			//rt.runFinalization();
			rt.gc();
			Thread.yield();
		}
	}

	/**
	 * ���ұ����������������еı���������Session�еģ������Env�е�ȫ�ֱ�����
	 * @param varName String ������
	 * @param ctx Context ����������
	 * @return Param
	 */
	public static Param getParam(String varName, Context ctx) {
		if (ctx != null) {
			Param p = ctx.getParam(varName);
			if (p != null)return p;

			JobSpace js = ctx.getJobSpace();
			if (js != null) {
				p = js.getParam(varName);
				if (p != null)return p;
			}
		}

		return Env.getParam(varName);
	}

	/**
	 * ɾ������
	 * @param varName ������
	 * @param ctx ����������
	 * @return Param ɾ���ı�����û�ҵ��򷵻ؿ�
	 */
	public static Param removeParam(String varName, Context ctx) {
		if (ctx != null) {
			Param p = ctx.removeParam(varName);
			if (p != null) {
				return p;
			}

			JobSpace js = ctx.getJobSpace();
			if (js != null) {
				p = js.removeParam(varName);
				if (p != null) {
					return p;
				}
			}
		}

		return Env.removeParam(varName);
	}

	/**
	 * ȡ���ݿ����ӹ���
	 * @param dbName ���ݿ���
	 * @param ctx ����������
	 * @return ISessionFactory
	 */
	public static ISessionFactory getDBSessionFactory(String dbName, Context ctx) {
		if (ctx == null) {
			return Env.getDBSessionFactory(dbName);
		} else {
			ISessionFactory dbsf = ctx.getDBSessionFactory(dbName);
			return dbsf == null ? Env.getDBSessionFactory(dbName) : dbsf;
		}
	}
}
