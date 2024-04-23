package com.scudata.dm;

import java.util.HashMap;

/**
 * ��������������lock(n,s)����
 * @author WangXiaoJun
 *
 */
public final class LockManager {
	private static HashMap<Object, LockObject> lockMap = new HashMap<Object, LockObject>();
	
	/**
	 * ��������ֵ
	 * @param key ��ֵ
	 * @param ms �ȴ���������С��0��ʾ������ʱ
	 * @param ctx ����������
	 * @return ����ɹ�true��ʧ�ܷ���false
	 */
	public final static Object lock(Object key, long ms, Context ctx) {
		LockObject lock;
		synchronized(lockMap) {
			lock = lockMap.get(key);
			if (lock == null) {
				lock = new LockObject(ctx);
				lockMap.put(key, lock);
			}
		}
		
		if (lock.lock(ms, ctx)) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
	
	/**
	 * ��������ֵ
	 * @param key ��ֵ
	 * @param ctx
	 * @return true���ɹ���false��ʧ��
	 */
	public final static boolean unLock(Object key, Context ctx) {
		LockObject lock;
		synchronized(lockMap) {
			lock = lockMap.get(key);
			if (lock == null) {
				return false;
			}
		}
		
		return lock.unlock(ctx);
	}
}