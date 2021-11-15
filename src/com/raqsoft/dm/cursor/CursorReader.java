package com.raqsoft.dm.cursor;

import com.raqsoft.dm.Sequence;
import com.raqsoft.thread.Job;
import com.raqsoft.thread.ThreadPool;

/**
 * ���ڶ��̴߳��α��ȡ���ݵ����񣬽����ȡ�ߺ������ᱻ���뵽�̳߳��У�����ȡ����
 * @author WangXiaoJun
 *
 */
class CursorReader extends Job {
	private ThreadPool threadPool; // �̳߳�
	private ICursor cursor; // Ҫȡ�����α�
	private int fetchCount; // ÿ�ζ�ȡ��������
	private Sequence table; // ��ȡ������

	/**
	 * �������α�ȡ��������ʹ��getTable�õ�ȡ�����
	 * @param threadPool �̳߳�
	 * @param cursor �α�
	 * @param fetchCount ÿ�ζ�ȡ��������
	 */
	public CursorReader(ThreadPool threadPool, ICursor cursor, int fetchCount) {
		this.threadPool = threadPool;
		this.cursor = cursor;
		this.fetchCount = fetchCount;
		threadPool.submit(this);
	}
	
	/**
	 * ��ȡ���ݣ��˷������ύ�����̳߳ؼ�������
	 * @return
	 */
	public Sequence getTable() {
		join();
		if (table != null) {
			Sequence table = this.table;
			this.table = null;

			threadPool.submit(this);
			return table;
		} else {
			return null;
		}
	}

	/**
	 * ��ȡ��������ݣ�������ȡ�������ύ�����̳߳�
	 * @return
	 */
	public Sequence getCatch() {
		join();
		if (table != null) {
			Sequence table = this.table;
			this.table = null;
			return table;
		} else {
			return null;
		}
	}

	/**
	 * ���̳߳�����̵߳��ã����α��ȡ����
	 */
	public void run() {
		table = cursor.fetch(fetchCount);
	}
}