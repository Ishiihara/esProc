package com.scudata.dm.cursor;

import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.dm.*;

/**
 * ����ִ�����񣬷������񷵻�ֵ���α�
 * @author RunQian
 *
 */
public class DFXCursor extends ICursor {
	private PgmCellSet pcs;	// �������
	
	// ����ķ���ֵ�������α�Ҳ���������У���������ж������ֵ����Ҫһ����ִ��
	private ICursor cursor;
	private Sequence data;
	
	// �Ƿ������������ж�ȡ�����񻺴棬�����������������Ҫ�����񻹸����������
	private boolean useCache; 
	private boolean isCalc; // �����Ƿ���ִ��

	/**
	 * ���������α�
	 * @param pcs �������
	 * @param ctx  ����������
	 * @param useCache
	 */
	public DFXCursor(PgmCellSet pcs, Context ctx, boolean useCache) {
		this.pcs = pcs;
		this.ctx = ctx;
		this.useCache = useCache;
		
		// 2024/4/8 ʹ�õ�ǰ�����JobSpace��
		//JobSpace js = new JobSpace("tmp");
		//pcs.getContext().setJobSpace(js);
		//if (ctx != null) {
		//	ctx.addResource(this);
		//}
	}
	
	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (pcs == null || n < 1) return null;
		
		if (data != null) {
			int len = data.length();
			if (len > n) {
				return data.split(1, n);
			} else if (len == n) {
				Sequence table = data;
				data = null;
				return table;
			}
		}
		
		if (cursor != null) {
			if (data != null) {
				Sequence tmp = cursor.fetch(n - data.length());
				data = append(data, tmp);
			} else {
				data = cursor.fetch(n);
			}
			
			if (data == null || data.length() < n) {
				cursor = null;
			} else {
				Sequence table = data;
				data = null;
				return table;
			}
		}
		
		if (!isCalc) {
			pcs.calculateResult();
			isCalc = true;
		}
		
		while (pcs.hasNextResult()) {
			Object result = pcs.nextResult();
			if (result instanceof Sequence) {
				Sequence sequence = (Sequence)result;
				if (data == null) {
					data = sequence;
				} else {
					data = append(data, sequence);
				}
				
				int len = data.length();
				if (len > n) {
					return data.split(1, n);
				} else if (len == n) {
					Sequence table = data;
					data = null;
					return table;
				}
			} else if (result instanceof ICursor) {
				cursor = (ICursor)result;
				if (data != null) {
					Sequence tmp = cursor.fetch(n - data.length());
					data = append(data, tmp);
				} else {
					data = cursor.fetch(n);
				}
				
				if (data == null || data.length() < n) {
					cursor = null;
				} else {
					Sequence table = data;
					data = null;
					return table;
				}
			} else {
				if (data == null) data = new Sequence();

				data.add(result);
				int len = data.length();
				if (len > n) {
					return data.split(1, n);
				} else if (len == n) {
					Sequence table = data;
					data = null;
					return table;
				}
			}
		}

		Sequence table = data;
		data = null;
		return table;
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (pcs == null || n < 1) return 0;
		
		long total = 0;
		if (data != null) {
			total = data.length();
			if (total > n) {
				data.split(1, (int)n);
				return n;
			} else if (total == n) {
				data = null;
				return n;
			} else {
				data = null;
			}
		}
		
		if (cursor != null) {
			total += cursor.skip(n - total);
			if (total < n) {
				cursor = null;
			} else {
				return total;
			}
		}
		
		if (!isCalc) {
			pcs.calculateResult();
			isCalc = true;
		}
		
		while (pcs.hasNextResult()) {
			Object result = pcs.nextResult();
			if (result instanceof Sequence) {
				Sequence sequence = (Sequence)result;
				long dif = n - total;
				int len = sequence.length();
				if (len > dif) {
					data = sequence.split(1, (int)dif);
					return n;
				} else if (len == dif) {
					return n;
				} else {
					total += len;
				}
			} else if (result instanceof ICursor) {
				ICursor cur = (ICursor)result;
				long dif = n - total;
				total += cur.skip(dif);
				
				if (total >= n) {
					cursor = cur;
					return total;
				}
			} else {
				total++;
				if (total == n) {
					return total;
				}
			}
		}

		return total;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (pcs != null) {
			// 2024/4/8 ʹ�õ�ǰ�����JobSpace��
			//if (ctx != null) ctx.removeResource(this);
			//pcs.getContext().getResourceManager().closeResource();
			
			if (useCache) {
				pcs.reset();
				DfxManager.getInstance().putDfx(pcs);
			}

			pcs = null;
			data = null;
		}
	}
}
