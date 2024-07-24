package com.scudata.dm.cursor;

import com.scudata.dm.Sequence;
import com.scudata.thread.Job;
import com.scudata.thread.ThreadPool;

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
	
	public Sequence getTable(int n) {
		join();
		
		if (table != null) {
			Sequence result = table;
			table = null;
			
			if (fetchCount < n) {
				int diff = n - result.length();
				if (diff > 0) {
					fetchCount = diff;
					threadPool.submit(this);
					join();
					
					if (table != null) {
						result = result.append(table);
						table = null;
					}
				}
				
				fetchCount = n;
			}
			
			threadPool.submit(this);
			return result;
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
