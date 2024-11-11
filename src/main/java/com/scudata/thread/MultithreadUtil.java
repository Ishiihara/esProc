package com.scudata.thread;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Date;

import com.scudata.array.DateArray;
import com.scudata.array.StringArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Env;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CursorUtil;
import com.scudata.util.Variant;

/**
 * ���̼߳��㹤����
 * @author WangXiaoJun
 *
 */
public final class MultithreadUtil {
	public static int SINGLE_PROSS_COUNT = 20480; // Ԫ�������ڴ�ֵʱ�����õ��̴߳���
	private static final int INSERTIONSORT_THRESHOLD = 9; // ��������Ԫ������ֵ
	

	/**
	 * ȡ���̴߳����Ԫ�����������������������ö��̴߳���
	 * @return int ����
	 */
	public static int getSingleThreadProssCount() {
		return SINGLE_PROSS_COUNT;
	}

	/**
	 * ���õ��̴߳����Ԫ�����������������������ö��̴߳���
	 * @param count int ����
	 */
	public static void setSingleThreadProssCount(int count) {
		SINGLE_PROSS_COUNT = count;
	}

	private static int getParallelNum() {
		return Env.getParallelNum();
	}

	/**
	 * ��������ж��߳�����
	 * @param vals ��������
	 */
	public static void sort(Object []vals) {
		sort(vals, 0, vals.length);
	}

	/**
	 * ��������ж��߳�����
	 * @param vals ��������
	 * @param fromIndex ��ʼλ�ã�����
	 * @param toIndex ����λ�ã�������
	 */
	public static void sort(Object []vals, int fromIndex, int toIndex) {
		rangeCheck(vals.length, fromIndex, toIndex);
		Object []aux = cloneSubarray(vals, fromIndex, toIndex);
		mergeSort(aux, vals, fromIndex, toIndex, -fromIndex, Env.getParallelNum());
	}

	/**
	 * ��������ж��߳�����
	 * @param vals ��������
	 * @param c �Ƚ���
	 */
	public static void sort(Object []vals, Comparator<Object> c) {
		sort(vals, 0, vals.length, c);
	}

	/**
	 * ��������ж��߳�����
	 * @param vals ��������
	 * @param fromIndex ��ʼλ�ã�����
	 * @param toIndex ����λ�ã�������
	 * @param c �Ƚ���
	 */
	public static void sort(Object []vals, int fromIndex, int toIndex, Comparator<Object> c) {
		rangeCheck(vals.length, fromIndex, toIndex);
		Object []aux = cloneSubarray(vals, fromIndex, toIndex);
		mergeSort(aux, vals, fromIndex, toIndex, -fromIndex, c, Env.getParallelNum());
	}

	static void mergeSort(Object []src, Object[] dest, int low, int high, int off, Comparator<Object> c, int threadCount) {
		// ���Ԫ����С���趨ֵ�����߳���С��2���߳�����
		int length = high - low;
		if (length <= SINGLE_PROSS_COUNT || threadCount < 2) {
			mergeSort(src, dest, low, high, off, c);
			return;
		}
		
		// ���ݷֳ������֣���ǰ�̶߳�ǰ�벿������Ȼ������һ���̶߳Ժ�벿������
		// ÿһ���ֿ��ܻ���������߳�����
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		
		SortJob job1 = new SortJob(dest, src, low, mid, -off, c, threadCount / 2);
		SortJob job2 = new SortJob(dest, src, mid, high, -off, c, threadCount / 2);
		
		// ����һ���̶߳Ժ�벿�ֽ�������
		new JobThread(job2).start();
		
		// ��ǰ�̶߳�ǰ�벿�ֽ�������
		job1.run();
		
		// �ȴ�����ִ�����
		job2.join();
		
		if (c.compare(src[mid - 1], src[mid]) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	// ���̹߳鲢����
	private static void mergeSort(Object []src, Object []dest, int low, int high, int off, Comparator<Object> c) {
		int length = high - low;
		if (length < INSERTIONSORT_THRESHOLD) {
			// Insertion sort on smallest arrays
			Object tmp;
			for (int i = low + 1, j = i; i < high; j = ++i) {
				tmp = dest[i];
				for (; j > low && c.compare(dest[j - 1], tmp) > 0; --j) {
					dest[j] = dest[j - 1];
					//swap(dest, j, j - 1);
				}
				
				dest[j] = tmp;
			}
			
			return;
		}

		// Recursively sort halves of dest into src
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSort(dest, src, low, mid, -off, c);
		mergeSort(dest, src, mid, high, -off, c);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (c.compare(src[mid-1], src[mid]) <= 0) {
		   System.arraycopy(src, low, dest, destLow, length);
		   return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}
	static void mergeSort(Object []src, Object[] dest, int low, int high, int off, int threadCount) {
		// ���Ԫ����С���趨ֵ�����߳���С��2���߳�����
		int length = high - low;
		if (length <= SINGLE_PROSS_COUNT || threadCount < 2) {
			mergeSort(src, dest, low, high, off);
			return;
		}
		
		// ���ݷֳ������֣���ǰ�̶߳�ǰ�벿������Ȼ������һ���̶߳Ժ�벿������
		// ÿһ���ֿ��ܻ���������߳�����
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		
		SortJob job1 = new SortJob(dest, src, low, mid, -off, threadCount / 2);
		SortJob job2 = new SortJob(dest, src, mid, high, -off, threadCount / 2);
		
		// ����һ���̶߳Ժ�벿�ֽ�������
		new JobThread(job2).start();
		
		// ��ǰ�̶߳�ǰ�벿�ֽ�������
		job1.run();
		
		// �ȴ�����ִ�����
		job2.join();
		
		if (Variant.compare(src[mid - 1], src[mid], true) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && Variant.compare(src[p], src[q], true) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	// ���̹߳鲢����
	private static void mergeSort(Object []src, Object []dest, int low, int high, int off) {
		int length = high - low;
		if (length < INSERTIONSORT_THRESHOLD) {
			// Insertion sort on smallest arrays
			Object tmp;
			for (int i = low + 1, j = i; i < high; j = ++i) {
				tmp = dest[i];
				for (; j > low && Variant.compare(dest[j - 1], tmp, true) > 0; --j) {
					dest[j] = dest[j - 1];
					//swap(dest, j, j - 1);
				}
				
				dest[j] = tmp;
			}
			
			return;
		}

		// Recursively sort halves of dest into src
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (Variant.compare(src[mid-1], src[mid], true) <= 0) {
		   System.arraycopy(src, low, dest, destLow, length);
		   return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && Variant.compare(src[p], src[q], true) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	private static Object []cloneSubarray(Object[] vals, int from, int to) {
		int n = to - from;
		Object result = Array.newInstance(vals.getClass().getComponentType(), n);
		System.arraycopy(vals, from, result, 0, n);
		return (Object [])result;
	}

	private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex+")");
		if (fromIndex < 0)
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		if (toIndex > arrayLen)
			throw new ArrayIndexOutOfBoundsException(toIndex);
	}
	
	/**
	 * ���̶߳�����ִ��calc����
	 * @param src Դ����
	 * @param exp ������ʽ
	 * @param ctx ����������
	 * @return ���������
	 */
	public static Sequence calc(Sequence src, Expression exp, Context ctx) {
		if (exp == null) {
			return src;
		}
		
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len < 2 || parallelNum < 2) {
			return src.calc(exp, ctx);
		}
		
		if (parallelNum > len) {
			parallelNum = len;
		}
		
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / parallelNum;
		CalcJob []jobs = new CalcJob[parallelNum];

		// ǰ��Ŀ�ÿ�ζ�һ
		int mod = len % parallelNum;
		int start = 1;
		int end; // ������
		Sequence result = new Sequence(new Object[len]);
		
		for (int i = 0; i < parallelNum; ++i) {
			if (i + 1 == parallelNum) {
				end = len + 1;
			} else if (i < mod) {
				end = start + singleCount + 1;
			} else {
				end = start + singleCount;
			}

			Context tmpCtx = ctx.newComputeContext();
			Expression tmpExp = exp.newExpression(tmpCtx);
			jobs[i] = new CalcJob(src, start, end, tmpExp, tmpCtx, result);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}

		// �ȴ�����ִ�����
		for (int i = 0; i < parallelNum; ++i) {
			jobs[i].join();
		}

		return result;
	}
	
	/**
	 * ���̶߳�����ִ��run����
	 * @param src Դ����
	 * @param exp ������ʽ
	 * @param ctx ����������
	 */
	public static void run(Sequence src, Expression exp, Context ctx) {
		if (exp == null) {
			return;
		}
		
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len < 2 || parallelNum < 2) {
			src.run(exp, ctx);
			return;
		}
		
		if (parallelNum > len) {
			parallelNum = len;
		}
		
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / parallelNum;
		RunJob []jobs = new RunJob[parallelNum];

		// ǰ��Ŀ�ÿ�ζ�һ
		int mod = len % parallelNum;
		int start = 1;
		int end; // ������
		
		for (int i = 0; i < parallelNum; ++i) {
			if (i + 1 == parallelNum) {
				end = len + 1;
			} else if (i < mod) {
				end = start + singleCount + 1;
			} else {
				end = start + singleCount;
			}

			Context tmpCtx = ctx.newComputeContext();
			Expression tmpExp = exp.newExpression(tmpCtx);

			jobs[i] = new RunJob(src, start, end, tmpExp, tmpCtx);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}
		
		// �ȴ�����ִ�����
		for (int i = 0; i < parallelNum; ++i) {
			jobs[i].join();
		}
	}
	
	/**
	 * ���̶߳�����ִ��run����
	 * @param src Դ����
	 * @param assignExps Expression[] ��ֵ���ʽ
	 * @param exps Expression[] ֵ���ʽ
	 * @param ctx Context
	 */
	public static void run(Sequence src, Expression[] assignExps, Expression[] exps, Context ctx) {
		if (exps == null || exps.length == 0) {
			return;
		}

		int colCount = exps.length;
		if (assignExps == null) {
			assignExps = new Expression[colCount];
		} else if (assignExps.length != colCount) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("run" + mm.getMessage("function.invalidParam"));
		}
		
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len <= SINGLE_PROSS_COUNT || parallelNum < 2) {
			src.run(assignExps, exps, ctx);
			return;
		}
		
		int threadCount = (len - 1) / SINGLE_PROSS_COUNT + 1;
		if (threadCount > parallelNum) {
			threadCount = parallelNum;
		}
		
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / threadCount;
		RunJob []jobs = new RunJob[threadCount];

		int start = 1;
		int end; // ������
		for (int i = 0; i < threadCount; ++i) {
			if (i + 1 == threadCount) {
				end = len + 1;
			} else {
				end = start + singleCount;
			}

			Context tmpCtx = ctx.newComputeContext();
			Expression []tempAssignExps = new Expression[colCount];
			Expression []tmpExps = new Expression[colCount];
			for (int k = 0; k < colCount; ++k) {
				tmpExps [k] = exps[k].newExpression(tmpCtx);
				if (assignExps[k] != null) {
					tempAssignExps [k] = assignExps[k].newExpression(tmpCtx);
				}
			}
			
			jobs[i] = new RunJob(src, start, end, tempAssignExps, tmpExps, tmpCtx);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}
		
		// �ȴ�����ִ�����
		for (int i = 0; i < threadCount; ++i) {
			jobs[i].join();
		}
	}
	
	/**
	 * ���̶߳�����ִ�й�������
	 * @param src Դ����
	 * @param exp ���˱��ʽ
	 * @param ctx ����������
	 * @return Object
	 */
	public static Object select(Sequence src, Expression exp, Context ctx) {
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len <= SINGLE_PROSS_COUNT || parallelNum < 2) {
			return src.select(exp, null, ctx);
		}
		
		int threadCount = (len - 1) / SINGLE_PROSS_COUNT + 1;
		if (threadCount > parallelNum) {
			threadCount = parallelNum;
		}
		
		// ����new�����ύ���̳߳�
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / threadCount;
		SelectJob []jobs = new SelectJob[threadCount];

		int start = 1;
		int end; // ������
		for (int i = 0; i < threadCount; ++i) {
			if (i + 1 == threadCount) {
				end = len + 1;
			} else {
				end = start + singleCount;
			}

			Context tmpCtx = ctx.newComputeContext();
			Expression tmpExp = exp.newExpression(tmpCtx);

			jobs[i] = new SelectJob(src, start, end, tmpExp, tmpCtx);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}

		// �ȴ�����ִ����ϲ�ȡ�����
		Sequence result = new Sequence();
		for (int i = 0; i < threadCount; ++i) {
			jobs[i].join();
			jobs[i].getResult(result);
		}

		return result;
	}
	
	/**
	 * ���̶߳�����ִ�й������㣬������ʽ�ļ������͸���ֵ���������
	 * @param src Դ����
	 * @param fltExps ������ʽ����
	 * @param vals ֵ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Object
	 */
	public static Object select(Sequence src, Expression[] fltExps, Object[] vals, 
			String opt, Context ctx) {
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len <= SINGLE_PROSS_COUNT || parallelNum < 2) {
			return src.select(fltExps, vals, null, ctx);
		}
		
		int threadCount = (len - 1) / SINGLE_PROSS_COUNT + 1;
		if (threadCount > parallelNum) {
			threadCount = parallelNum;
		}
		
		// ����new�����ύ���̳߳�
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / threadCount;
		SelectJob []jobs = new SelectJob[threadCount];

		int expCount = fltExps.length;
		int start = 1;
		int end; // ������
		for (int i = 0; i < threadCount; ++i) {
			if (i + 1 == threadCount) {
				end = len + 1;
			} else {
				end = start + singleCount;
			}

			Context tmpCtx = ctx.newComputeContext();
			Expression []tmpExps = new Expression[expCount];
			for (int k = 0; k < expCount; ++k) {
				tmpExps [k] = fltExps[k].newExpression(tmpCtx);
			}
			
			jobs[i] = new SelectJob(src, start, end, tmpExps, vals, tmpCtx);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}

		// �ȴ�����ִ����ϲ�ȡ�����
		Sequence result = new Sequence();
		for (int i = 0; i < threadCount; ++i) {
			jobs[i].join();
			jobs[i].getResult(result);
		}

		return result;
	}

	/**
	 * ���̶߳�����ִ��new����
	 * @param src Դ����
	 * @param ds ��������ݽṹ
	 * @param exps ������ʽ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return ��������
	 */
	public static Table newTable(Sequence src, DataStruct ds, Expression[] exps, String opt, Context ctx) {
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len <= SINGLE_PROSS_COUNT || parallelNum < 2) {
			return src.newTable(ds, exps, opt, ctx);
		}
		
		int threadCount = (len - 1) / SINGLE_PROSS_COUNT + 1;
		if (threadCount > parallelNum) {
			threadCount = parallelNum;
		}
		
		// ����new�����ύ���̳߳�
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / threadCount;
		NewJob []jobs = new NewJob[threadCount];
		int expCount = exps.length;
		
		int start = 1;
		int end; // ������
		for (int i = 0; i < threadCount; ++i) {
			if (i + 1 == threadCount) {
				end = len + 1;
			} else {
				end = start + singleCount;
			}

			Context tmpCtx = ctx.newComputeContext();
			Expression []tmpExps = new Expression[expCount];
			for (int k = 0; k < expCount; ++k) {
				tmpExps [k] = exps[k].newExpression(tmpCtx);
			}

			jobs[i] = new NewJob(src, start, end, ds, tmpExps, opt, tmpCtx);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}

		// �ȴ�����ִ����ϲ�ȡ�����
		Table result = new Table(ds, len);
		for (int i = 0; i < threadCount; ++i) {
			jobs[i].join();
			jobs[i].getResult(result);
		}

		return result;
	}
	
	/**
	 * �������е��ֶ�ֵ�ϲ����������
	 * @param src Դ����
	 * @param gexp ����ֵΪ���еı��ʽ
	 * @param exps ��������ʽ����
	 * @param ds ��������ݽṹ
	 * @param opt 1��gexpΪ��ʱ����һ����¼��Ĭ�ϲ�����
	 * @param ctx ����������
	 * @return ���
	 */
	public static Table newTables(Sequence src, Expression gexp, Expression[] exps, DataStruct ds, String opt, Context ctx) {
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len <= SINGLE_PROSS_COUNT || parallelNum < 2) {
			return src.newTables(gexp, exps, ds, opt, ctx);
		}
		
		int threadCount = (len - 1) / SINGLE_PROSS_COUNT + 1;
		if (threadCount > parallelNum) {
			threadCount = parallelNum;
		}
		
		// ����new�����ύ���̳߳�
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / threadCount;
		NewsJob []jobs = new NewsJob[threadCount];
		int expCount = exps.length;
		
		int start = 1;
		int end; // ������
		for (int i = 0; i < threadCount; ++i) {
			if (i + 1 == threadCount) {
				end = len + 1;
			} else {
				end = start + singleCount;
			}

			Context tmpCtx = ctx.newComputeContext();
			Expression tmpGExp = gexp.newExpression(tmpCtx);
			Expression []tmpExps = new Expression[expCount];
			for (int k = 0; k < expCount; ++k) {
				tmpExps [k] = exps[k].newExpression(tmpCtx);
			}

			jobs[i] = new NewsJob(src, start, end, tmpGExp, tmpExps, ds, opt, tmpCtx);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}

		// �ȴ�����ִ����ϲ�ȡ�����
		int totalLen = 0;
		for (int i = 0; i < threadCount; ++i) {
			jobs[i].join();
			Table curResult = jobs[i].getResult();
			totalLen += curResult.length();
		}

		Table result = new Table(ds, totalLen);
		for (int i = 0; i < threadCount; ++i) {
			Table curResult = jobs[i].getResult();
			result.getMems().addAll(curResult.getMems());
		}
		
		return result;
	}
	
	/**
	 * ���̶߳�����ִ��derive����
	 * @param src Դ����
	 * @param names �ֶ�������
	 * @param exps ������ʽ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return ��������
	 */
	public static Table derive(Sequence src, String []names, Expression []exps, String opt, Context ctx) {
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len <= SINGLE_PROSS_COUNT || parallelNum < 2) {
			opt = opt.replace('m', ' ');
			return src.derive(names, exps, opt, ctx);
		}
		
		DataStruct srcDs = src.dataStruct();
		if (srcDs == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		int colCount = names.length;
		for (int i = 0; i < colCount; ++i) {
			if (names[i] == null || names[i].length() == 0) {
				if (exps[i] == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("new" + mm.getMessage("function.invalidParam"));
				}

				names[i] = exps[i].getIdentifierName();
			} else {
				if (exps[i] == null) {
					exps[i] = Expression.NULL;
				}
			}
		}

		String []srcNames = srcDs.getFieldNames();
		int srcColCount = srcNames.length;

		// �ϲ��ֶ�
		String []totalNames = new String[srcColCount + colCount];
		System.arraycopy(srcNames, 0, totalNames, 0, srcColCount);
		System.arraycopy(names, 0, totalNames, srcColCount, colCount);

		// �����м�¼�����ֶΣ��Ա�����ļ�¼��������ǰ���¼���ֶ�
		DataStruct newDs = srcDs.create(totalNames);

		int threadCount = (len - 1) / SINGLE_PROSS_COUNT + 1;
		if (threadCount > parallelNum) {
			threadCount = parallelNum;
		}
		
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / threadCount;
		DeriveJob []jobs = new DeriveJob[threadCount];
		int expCount = exps.length;
		
		int start = 1;
		int end; // ������
		for (int i = 0; i < threadCount; ++i) {
			if (i + 1 == threadCount) {
				end = len + 1;
			} else {
				end = start + singleCount;
			}

			Context tmpCtx = ctx.newComputeContext();
			Expression []tmpExps = new Expression[expCount];
			for (int k = 0; k < expCount; ++k) {
				tmpExps [k] = exps[k].newExpression(tmpCtx);
			}

			jobs[i] = new DeriveJob(src, start, end, newDs, tmpExps, opt, tmpCtx);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}

		// �ȴ�����ִ����ϲ�ȡ�����
		Table result = new Table(newDs, len);
		for (int i = 0; i < threadCount; ++i) {
			jobs[i].join();
			jobs[i].getResult(result);
		}

		return result;
	}

	/**
	 * ������������ж��߳�����
	 * @param vals ��������
	 * @param fromIndex ��ʼλ�ã�����
	 * @param toIndex ����λ�ã�������
	 */
	public static void sort(int []vals, int fromIndex, int toIndex) {
		rangeCheck(vals.length, fromIndex, toIndex);
		int n = toIndex - fromIndex;
		int []aux = new int[n];
		System.arraycopy(vals, fromIndex, aux, 0, n);
		mergeSort(aux, vals, fromIndex, toIndex, -fromIndex, Env.getParallelNum());
	}

	static void mergeSort(int []src, int[] dest, int low, int high, int off, int threadCount) {
		// ���Ԫ����С���趨ֵ�����߳���С��2���߳�����
		int length = high - low;
		if (length <= SINGLE_PROSS_COUNT || threadCount < 2) {
			mergeSort(src, dest, low, high, off);
			return;
		}
		
		// ���ݷֳ������֣���ǰ�̶߳�ǰ�벿������Ȼ������һ���̶߳Ժ�벿������
		// ÿһ���ֿ��ܻ���������߳�����
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		
		IntSortJob job1 = new IntSortJob(dest, src, low, mid, -off, threadCount / 2);
		IntSortJob job2 = new IntSortJob(dest, src, mid, high, -off, threadCount / 2);
		
		// ����һ���̶߳Ժ�벿�ֽ�������
		new JobThread(job2).start();
		
		// ��ǰ�̶߳�ǰ�벿�ֽ�������
		job1.run();
		
		// �ȴ�����ִ�����
		job2.join();
		
		if (src[mid - 1] <= src[mid]) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && src[p] <= src[q]) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	// ���̹߳鲢����
	private static void mergeSort(int []src, int []dest, int low, int high, int off) {
		int length = high - low;
		if (length < INSERTIONSORT_THRESHOLD) {
			// Insertion sort on smallest arrays
			int tmp;
			for (int i = low + 1, j = i; i < high; j = ++i) {
				tmp = dest[i];
				for (; j > low && dest[j - 1] > tmp; --j) {
					dest[j] = dest[j - 1];
					//swap(dest, j, j - 1);
				}
				
				dest[j] = tmp;
			}
			
			return;
		}

		// Recursively sort halves of dest into src
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (src[mid-1] <= src[mid]) {
		   System.arraycopy(src, low, dest, destLow, length);
		   return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && src[p] <= src[q]) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	/**
	 * �Գ�����������ж��߳�����
	 * @param vals ����������
	 * @param fromIndex ��ʼλ�ã�����
	 * @param toIndex ����λ�ã�������
	 */
	public static void sort(long []vals, int fromIndex, int toIndex) {
		rangeCheck(vals.length, fromIndex, toIndex);
		int n = toIndex - fromIndex;
		long []aux = new long[n];
		System.arraycopy(vals, fromIndex, aux, 0, n);
		mergeSort(aux, vals, fromIndex, toIndex, -fromIndex, Env.getParallelNum());
	}

	static void mergeSort(long []src, long[] dest, int low, int high, int off, int threadCount) {
		// ���Ԫ����С���趨ֵ�����߳���С��2���߳�����
		int length = high - low;
		if (length <= SINGLE_PROSS_COUNT || threadCount < 2) {
			mergeSort(src, dest, low, high, off);
			return;
		}
		
		// ���ݷֳ������֣���ǰ�̶߳�ǰ�벿������Ȼ������һ���̶߳Ժ�벿������
		// ÿһ���ֿ��ܻ���������߳�����
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		
		LongSortJob job1 = new LongSortJob(dest, src, low, mid, -off, threadCount / 2);
		LongSortJob job2 = new LongSortJob(dest, src, mid, high, -off, threadCount / 2);
		
		// ����һ���̶߳Ժ�벿�ֽ�������
		new JobThread(job2).start();
		
		// ��ǰ�̶߳�ǰ�벿�ֽ�������
		job1.run();
		
		// �ȴ�����ִ�����
		job2.join();
		
		if (src[mid - 1] <= src[mid]) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && src[p] <= src[q]) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	// ���̹߳鲢����
	private static void mergeSort(long []src, long []dest, int low, int high, int off) {
		int length = high - low;
		if (length < INSERTIONSORT_THRESHOLD) {
			// Insertion sort on smallest arrays
			long tmp;
			for (int i = low + 1, j = i; i < high; j = ++i) {
				tmp = dest[i];
				for (; j > low && dest[j - 1] > tmp; --j) {
					dest[j] = dest[j - 1];
					//swap(dest, j, j - 1);
				}
				
				dest[j] = tmp;
			}
			
			return;
		}

		// Recursively sort halves of dest into src
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (src[mid-1] <= src[mid]) {
		   System.arraycopy(src, low, dest, destLow, length);
		   return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && src[p] <= src[q]) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	/**
	 * ��˫���ȸ�����������ж��߳�����
	 * @param vals ˫���ȸ���������
	 * @param fromIndex ��ʼλ�ã�����
	 * @param toIndex ����λ�ã�������
	 */
	public static void sort(double []vals, int fromIndex, int toIndex) {
		rangeCheck(vals.length, fromIndex, toIndex);
		int n = toIndex - fromIndex;
		double []aux = new double[n];
		System.arraycopy(vals, fromIndex, aux, 0, n);
		mergeSort(aux, vals, fromIndex, toIndex, -fromIndex, Env.getParallelNum());
	}

	static void mergeSort(double []src, double[] dest, int low, int high, int off, int threadCount) {
		// ���Ԫ����С���趨ֵ�����߳���С��2���߳�����
		int length = high - low;
		if (length <= SINGLE_PROSS_COUNT || threadCount < 2) {
			mergeSort(src, dest, low, high, off);
			return;
		}
		
		// ���ݷֳ������֣���ǰ�̶߳�ǰ�벿������Ȼ������һ���̶߳Ժ�벿������
		// ÿһ���ֿ��ܻ���������߳�����
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		
		DoubleSortJob job1 = new DoubleSortJob(dest, src, low, mid, -off, threadCount / 2);
		DoubleSortJob job2 = new DoubleSortJob(dest, src, mid, high, -off, threadCount / 2);
		
		// ����һ���̶߳Ժ�벿�ֽ�������
		new JobThread(job2).start();
		
		// ��ǰ�̶߳�ǰ�벿�ֽ�������
		job1.run();
		
		// �ȴ�����ִ�����
		job2.join();
		
		if (Double.compare(src[mid - 1], src[mid]) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && Double.compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	// ���̹߳鲢����
	private static void mergeSort(double []src, double []dest, int low, int high, int off) {
		int length = high - low;
		if (length < INSERTIONSORT_THRESHOLD) {
			// Insertion sort on smallest arrays
			double tmp;
			for (int i = low + 1, j = i; i < high; j = ++i) {
				tmp = dest[i];
				for (; j > low && Double.compare(dest[j - 1], tmp) > 0; --j) {
					dest[j] = dest[j - 1];
					//swap(dest, j, j - 1);
				}
				
				dest[j] = tmp;
			}
			
			return;
		}

		// Recursively sort halves of dest into src
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (Double.compare(src[mid-1], src[mid]) <= 0) {
		   System.arraycopy(src, low, dest, destLow, length);
		   return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && Double.compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	/**
	 * ���ַ���������ж��߳�����
	 * @param vals �ַ�������
	 * @param fromIndex ��ʼλ�ã�����
	 * @param toIndex ����λ�ã�������
	 */
	public static void sort(String []vals, int fromIndex, int toIndex) {
		rangeCheck(vals.length, fromIndex, toIndex);
		int n = toIndex - fromIndex;
		String []aux = new String[n];
		System.arraycopy(vals, fromIndex, aux, 0, n);
		mergeSort(aux, vals, fromIndex, toIndex, -fromIndex, Env.getParallelNum());
	}

	static void mergeSort(String []src, String[] dest, int low, int high, int off, int threadCount) {
		// ���Ԫ����С���趨ֵ�����߳���С��2���߳�����
		int length = high - low;
		if (length <= SINGLE_PROSS_COUNT || threadCount < 2) {
			mergeSort(src, dest, low, high, off);
			return;
		}
		
		// ���ݷֳ������֣���ǰ�̶߳�ǰ�벿������Ȼ������һ���̶߳Ժ�벿������
		// ÿһ���ֿ��ܻ���������߳�����
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		
		StringSortJob job1 = new StringSortJob(dest, src, low, mid, -off, threadCount / 2);
		StringSortJob job2 = new StringSortJob(dest, src, mid, high, -off, threadCount / 2);
		
		// ����һ���̶߳Ժ�벿�ֽ�������
		new JobThread(job2).start();
		
		// ��ǰ�̶߳�ǰ�벿�ֽ�������
		job1.run();
		
		// �ȴ�����ִ�����
		job2.join();
		
		if (StringArray.compare(src[mid - 1], src[mid]) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && StringArray.compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	// ���̹߳鲢����
	private static void mergeSort(String []src, String []dest, int low, int high, int off) {
		int length = high - low;
		if (length < INSERTIONSORT_THRESHOLD) {
			// Insertion sort on smallest arrays
			String tmp;
			for (int i = low + 1, j = i; i < high; j = ++i) {
				tmp = dest[i];
				for (; j > low && StringArray.compare(dest[j - 1], tmp) > 0; --j) {
					dest[j] = dest[j - 1];
					//swap(dest, j, j - 1);
				}
				
				dest[j] = tmp;
			}
			
			return;
		}

		// Recursively sort halves of dest into src
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (StringArray.compare(src[mid-1], src[mid]) <= 0) {
		   System.arraycopy(src, low, dest, destLow, length);
		   return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && StringArray.compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	/**
	 * ������������ж��߳�����
	 * @param vals ��������
	 * @param fromIndex ��ʼλ�ã�����
	 * @param toIndex ����λ�ã�������
	 */
	public static void sort(Date []vals, int fromIndex, int toIndex) {
		rangeCheck(vals.length, fromIndex, toIndex);
		int n = toIndex - fromIndex;
		Date []aux = new Date[n];
		System.arraycopy(vals, fromIndex, aux, 0, n);
		mergeSort(aux, vals, fromIndex, toIndex, -fromIndex, Env.getParallelNum());
	}

	static void mergeSort(Date []src, Date[] dest, int low, int high, int off, int threadCount) {
		// ���Ԫ����С���趨ֵ�����߳���С��2���߳�����
		int length = high - low;
		if (length <= SINGLE_PROSS_COUNT || threadCount < 2) {
			mergeSort(src, dest, low, high, off);
			return;
		}
		
		// ���ݷֳ������֣���ǰ�̶߳�ǰ�벿������Ȼ������һ���̶߳Ժ�벿������
		// ÿһ���ֿ��ܻ���������߳�����
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		
		DateSortJob job1 = new DateSortJob(dest, src, low, mid, -off, threadCount / 2);
		DateSortJob job2 = new DateSortJob(dest, src, mid, high, -off, threadCount / 2);
		
		// ����һ���̶߳Ժ�벿�ֽ�������
		new JobThread(job2).start();
		
		// ��ǰ�̶߳�ǰ�벿�ֽ�������
		job1.run();
		
		// �ȴ�����ִ�����
		job2.join();
		
		if (DateArray.compare(src[mid - 1], src[mid]) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && DateArray.compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	// ���̹߳鲢����
	private static void mergeSort(Date []src, Date []dest, int low, int high, int off) {
		int length = high - low;
		if (length < INSERTIONSORT_THRESHOLD) {
			// Insertion sort on smallest arrays
			Date tmp;
			for (int i = low + 1, j = i; i < high; j = ++i) {
				tmp = dest[i];
				for (; j > low && DateArray.compare(dest[j - 1], tmp) > 0; --j) {
					dest[j] = dest[j - 1];
					//swap(dest, j, j - 1);
				}
				
				dest[j] = tmp;
			}
			
			return;
		}

		// Recursively sort halves of dest into src
		int destLow  = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (DateArray.compare(src[mid-1], src[mid]) <= 0) {
		   System.arraycopy(src, low, dest, destLow, length);
		   return;
		}

		// Merge sorted halves (now in src) into dest
		for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && DateArray.compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}
	
	/**
	 * ���̶߳����н���HASHȥ��
	 * @param src Դ����
	 * @param opt ѡ��
	 * @return ��������
	 */
	public static Sequence hashId(Sequence src, String opt) {
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len <= SINGLE_PROSS_COUNT || parallelNum < 2) {
			opt = opt.replace('m', ' ');
			return CursorUtil.hashId(src, opt);
		}

		int threadCount = (len - 1) / SINGLE_PROSS_COUNT + 1;
		if (threadCount > parallelNum) {
			threadCount = parallelNum;
		}
		
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / threadCount;
		HashIdJob []jobs = new HashIdJob[threadCount];
		
		int start = 1;
		int end; // ������
		for (int i = 0; i < threadCount; ++i) {
			if (i + 1 == threadCount) {
				end = len + 1;
			} else {
				end = start + singleCount;
			}

			jobs[i] = new HashIdJob(src, start, end, opt);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}

		// �ȴ�����ִ����ϲ�ȡ�����
		Sequence result = new Sequence(len);
		for (int i = 0; i < threadCount; ++i) {
			jobs[i].join();
			jobs[i].getResult(result);
		}
		
		opt = opt.replace('m', ' ');
		return CursorUtil.hashId(result, opt);
	}

	/**
	 * ���̼߳�������bits����
	 * @param src Դ����
	 * @param opt n����ԱȡֵΪ���
	 * @return Sequence
	 */
	public static Sequence bits(Sequence src, String opt) {
		opt = opt.replace('m', ' ');
		int len = src.length();
		int parallelNum = getParallelNum();

		if (len <= SINGLE_PROSS_COUNT || parallelNum < 2) {
			return src.bits(opt);
		}
		
		int threadCount = (len - 1) / SINGLE_PROSS_COUNT + 1;
		if (threadCount > parallelNum) {
			threadCount = parallelNum;
		}
		
		// ����new�����ύ���̳߳�
		ThreadPool pool = ThreadPool.instance();
		int singleCount = len / threadCount;
		singleCount -= singleCount % 64;
		BitsJob []jobs = new BitsJob[threadCount];

		int start = 1;
		int end; // ������
		for (int i = 0; i < threadCount; ++i) {
			if (i + 1 == threadCount) {
				end = len + 1;
			} else {
				end = start + singleCount;
			}

			jobs[i] = new BitsJob(src, start, end, opt);
			pool.submit(jobs[i]); // �ύ����
			start = end;
		}

		// �ȴ�����ִ����ϲ�ȡ�����
		jobs[0].join();
		Sequence result = jobs[0].getResult();
		for (int i = 1; i < threadCount; ++i) {
			jobs[i].join();
			result.append(jobs[i].getResult());
		}

		return result;
	}
}
