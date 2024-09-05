package com.scudata.dm;

import java.util.ArrayList;

import com.scudata.array.IArray;
import com.scudata.array.NumberArray;
import com.scudata.array.ObjectArray;
import com.scudata.array.StringArray;
import com.scudata.thread.Job;
import com.scudata.thread.ThreadPool;
import com.scudata.util.Variant;

public class HashLinkSet {
	private static final int DEFAULT_CAPACITY = 0xFF;
	private static final int MAX_CAPACITY = 0x3fffffff;
	
	private IArray elementArray; // ��ϣ���ŵ���Ԫ�ص�λ�ã���Ҫ����λ�õ�Դ��ȡԪ��
	private int []entries; // ��ϣ������Ź�ϣֵ��Ӧ�����һ����¼��λ��
	private int []linkArray; // ��ϣֵ��ͬ�ļ�¼����
	private int capacity;
	
	private ArrayList<HashLinkSet> setList; // ���ڶ��̷߳���ʱÿ�����ĺϲ�
	
	public HashLinkSet() {
		capacity = DEFAULT_CAPACITY;
		entries = new int[capacity + 1];
		elementArray = new ObjectArray(capacity);
		linkArray = new int[capacity + 1];
	}
	
	public HashLinkSet(int capacity) {
		int n = 0xF;
		while (capacity > n) {
			n = (n << 1)+1;
			if (n < 0) {
				n = 0x3fffffff;
				break;
			}
		}
		
		this.capacity = n;
		entries = new int[n + 1];
		elementArray = new ObjectArray(n);
		linkArray = new int[n + 1];
	}
	
	public HashLinkSet(IArray src) {
		capacity = DEFAULT_CAPACITY;
		entries = new int[capacity + 1];
		elementArray = src.newInstance(capacity);
		linkArray = new int[capacity + 1];
	}
	
	public HashLinkSet(IArray src, int capacity) {
		int n = 0xF;
		while (capacity > n) {
			n = (n << 1)+1;
			if (n < 0) {
				n = 0x3fffffff;
				break;
			}
		}
		
		this.capacity = n;
		entries = new int[n + 1];
		elementArray = src.newInstance(n);
		linkArray = new int[n + 1];
	}
	
	public int size() {
		if (setList != null) {
			int setCount = setList.size();
			HashLinkSet []setArray;
			if (elementArray.size() > 0) {
				setArray = new HashLinkSet[setCount + 1];
				setList.toArray(setArray);
				setArray[setCount++] = this;
			} else if (setCount > 0) {
				setArray = new HashLinkSet[setCount];
				setList.toArray(setArray);
			} else {
				return 0;
			}
			
			if (setCount == 1) {
				return setArray[0].elementArray.size();
			} else {
				return size(setArray);
			}
		} else {
			return elementArray.size();
		}
	}
	
	// ���Ҫ�ϲ���set��������ͬ���ȱ����ͬ������
	private static class ResizeJob extends Job {
		private HashLinkSet set;
		public ResizeJob(HashLinkSet set) {
			this.set = set;
		}
		
		public void run() {
			set.resize();
		}
	}
	
	/**
	 * �������ڶ��߳�ͳ����ͬ�����Ķ����ϣ���Ԫ�������ѹ�ϣ��ƽ����������߳�
	 * @author WangXiaoJun
	 *
	 */
	private static class SizeJob extends Job {
		private HashLinkSet []setArray;
		private int seq; // �߳���ţ���0��ʼ����
		private int threadCount; // �߳���
		private int result; // ���������
		
		public SizeJob(HashLinkSet []setArray, int seq, int threadCount) {
			this.setArray = setArray;
			this.seq = seq;
			this.threadCount = threadCount;
		}
		
		public int size() {
			return result;
		}
		
		public void run() {
			IArray elementArray = setArray[0].elementArray;
			if (elementArray instanceof NumberArray) {
				sizeOfNumber();
			} else if (elementArray instanceof StringArray) {
				sizeOfString();
			} else {
				sizeOfObject();
			}
		}
		
		private void sizeOfNumber() {
			HashLinkSet []setArray = this.setArray;
			int threadCount = this.threadCount; // �߳���
			int capacity = setArray[0].capacity;
			int setCount = setArray.length;
			
			int valueCapacity = 128;
			IArray values = setArray[0].elementArray.newInstance(valueCapacity);
			int result = 0;
			
			// ѭ����ϣ��
			for (int h = seq; h <= capacity; h += threadCount) {
				// ��ǰ��ϣֵ�ϲ��ظ�Ԫ����
				int valueCount = 0;
				
				// ѭ��ÿ����ϣ��ǰ��ϣ���ϵ�ֵ
				for (int s = 0; s < setCount; ++s) {
					HashLinkSet set = setArray[s];
					IArray elementArray = set.elementArray;
					int prevValueCount = valueCount;
					
					Next:
					for (int seq = set.entries[h]; seq != 0; seq = set.linkArray[seq]) {
						for (int i = 0; i < prevValueCount; ++i) {
							if (values.isEquals(i, elementArray, seq)) {
								continue Next;
							}
						}
						
						if (valueCount == valueCapacity) {
							valueCapacity *= 2;
							values.ensureCapacity(valueCapacity);
						}
						
						valueCount++;
						values.push(elementArray, seq);
					}
				}
				
				values.clear();
				result += valueCount;
			}
			
			this.result = result;
		}
		
		private void sizeOfString() {
			HashLinkSet []setArray = this.setArray;
			int threadCount = this.threadCount; // �߳���
			int capacity = setArray[0].capacity;
			int setCount = setArray.length;
			
			int valueCapacity = 128;
			String []values = new String[valueCapacity];
			int result = 0;
			
			// ѭ����ϣ��
			for (int h = seq; h <= capacity; h += threadCount) {
				// ��ǰ��ϣֵ�ϲ��ظ�Ԫ����
				int valueCount = 0;
				
				// ѭ��ÿ����ϣ��ǰ��ϣ���ϵ�ֵ
				for (int s = 0; s < setCount; ++s) {
					HashLinkSet set = setArray[s];
					int prevValueCount = valueCount;
					
					Next:
					for (int seq = set.entries[h]; seq != 0; seq = set.linkArray[seq]) {
						String str = (String)set.elementArray.get(seq);
						for (int i = 0; i < prevValueCount; ++i) {
							if (values[i].equals(str)) {
								continue Next;
							}
						}
						
						if (valueCount == valueCapacity) {
							valueCapacity *= 2;
							String []tmp = new String[valueCapacity];
							System.arraycopy(values, 0, tmp, 0, valueCount);
							values = tmp;
						}
						
						values[valueCount++] = str;
					}
				}
				
				result += valueCount;
			}
			
			this.result = result;
		}
		
		private void sizeOfObject() {
			HashLinkSet []setArray = this.setArray;
			int threadCount = this.threadCount; // �߳���
			int capacity = setArray[0].capacity;
			int setCount = setArray.length;
			
			int valueCapacity = 128;
			Object []values = new Object[valueCapacity];
			int result = 0;
			
			// ѭ����ϣ��
			for (int h = seq; h <= capacity; h += threadCount) {
				// ��ǰ��ϣֵ�ϲ��ظ�Ԫ����
				int valueCount = 0;
				
				// ѭ��ÿ����ϣ��ǰ��ϣ���ϵ�ֵ
				for (int s = 0; s < setCount; ++s) {
					HashLinkSet set = setArray[s];
					int prevValueCount = valueCount;
					
					Next:
					for (int seq = set.entries[h]; seq != 0; seq = set.linkArray[seq]) {
						Object obj = set.elementArray.get(seq);
						for (int i = 0; i < prevValueCount; ++i) {
							if (Variant.isEquals(values[i], obj)) {
								continue Next;
							}
						}
						
						if (valueCount == valueCapacity) {
							valueCapacity *= 2;
							String []tmp = new String[valueCapacity];
							System.arraycopy(values, 0, tmp, 0, valueCount);
							values = tmp;
						}
						
						values[valueCount++] = obj;
					}
				}
				
				result += valueCount;
			}
			
			this.result = result;
		}
	}
	
	/**
	 * ͳ�ƶ��set����ϲ�ʣ��ĳ�Ա��
	 * @param setArray
	 * @return
	 */
	public static int size(HashLinkSet []setArray) {
		ThreadPool pool = ThreadPool.instance();
		int maxCapacity = setArray[0].capacity;
		int setCount = setArray.length;
		boolean isSameCapacity = true;
		
		for (int i = 1; i < setCount; ++i) {
			int curCapacity = setArray[i].capacity;
			if (curCapacity > maxCapacity) {
				maxCapacity = curCapacity;
				isSameCapacity = false;
			} else if (curCapacity < maxCapacity) {
				isSameCapacity = false;
			}
		}
		
		if (!isSameCapacity) {
			// ���Ҫ�ϲ���set��������ͬ���ȱ����ͬ������
			ArrayList<ResizeJob> jobList = new ArrayList<ResizeJob>();
			for (HashLinkSet set : setArray) {
				if (set.capacity != maxCapacity) {
					set.capacity = maxCapacity;
					set.entries = new int[maxCapacity + 1];
					set.linkArray = new int[maxCapacity + 1];

					ResizeJob job = new ResizeJob(set);
					pool.submit(job); // �ύ����
					jobList.add(job);
				}
			}
			
			for (ResizeJob job : jobList) {
				job.join();
			}
		}
		
		int threadCount = pool.getThreadCount();
		SizeJob []jobs = new SizeJob[threadCount];
		
		for (int i = 0; i < threadCount; ++i) {
			jobs[i] = new SizeJob(setArray, i, threadCount);
			pool.submit(jobs[i]); // �ύ����
		}
		
		int totalSize = 0;
		for (int i = 0; i < threadCount; ++i) {
			jobs[i].join();
			totalSize += jobs[i].size();
		}
		
		return totalSize;
	}
	
	private int hashCode(Object value) {
		int h = value != null ? value.hashCode() : 0;
		return (h + (h >> 16)) & capacity;
	}
	
	private int hashCode(int h) {
		return (h + (h >> 16)) & capacity;
	}
	
	public void putAll(IArray array) {
		if (array instanceof ObjectArray) {
			for (int i = 1, len = array.size(); i <= len; ++i) {
				put(array.get(i));
			}
		} else {
			for (int i = 1, len = array.size(); i <= len; ++i) {
				put(array, i);
			}
		}
	}
	
	public void put(IArray array, int index) {
		IArray elementArray = this.elementArray;
		int hash = hashCode(array.hashCode(index));

		for (int seq = entries[hash]; seq != 0; seq = linkArray[seq]) {
			if (elementArray.isEquals(seq, array, index)) {
				return;
			}
		}
		
		// ��set��û���ҵ���ǰֵ
		int count = elementArray.size() + 1;
		if (count <= capacity) {
			elementArray.push(array, index);
			linkArray[count] = entries[hash];
			entries[hash] = count;
		} else if (count < MAX_CAPACITY) {
			// Ԫ�������������������ϣ��
			capacity = (capacity << 1) + 1;
			entries = new int[capacity + 1];
			linkArray = new int[capacity + 1];
			elementArray.ensureCapacity(capacity);
			elementArray.push(array, index);
			
			resize();
		} else {
			throw new RuntimeException();
		}
	}
	
	public void put(Object value) {
		IArray elementArray = this.elementArray;
		int hash = hashCode(value);

		for (int seq = entries[hash]; seq != 0; seq = linkArray[seq]) {
			if (elementArray.isEquals(seq, value)) {
				return;
			}
		}
		
		// ��set��û���ҵ���ǰֵ
		int count = elementArray.size() + 1;
		if (count <= capacity) {
			elementArray.push(value);
			linkArray[count] = entries[hash];
			entries[hash] = count;
		} else if (count < MAX_CAPACITY) {
			// Ԫ�������������������ϣ��
			capacity = (capacity << 1) + 1;
			entries = new int[capacity + 1];
			linkArray = new int[capacity + 1];
			elementArray.ensureCapacity(capacity);
			elementArray.push(value);
			
			resize();
		} else {
			throw new OutOfMemoryError();
		}
	}
	
	private void resize() {
		IArray elementArray = this.elementArray;
		int []entries = this.entries;
		int []linkArray = this.linkArray;
		int hash;
		
		for (int i = 1, count = elementArray.size(); i <= count; ++i) {
			hash = hashCode(elementArray.hashCode(i));
			linkArray[i] = entries[hash];
			entries[hash] = i;
		}
	}
	
	public void addHashSet(HashLinkSet set) {
		if (set == null) {
			return;
		}
		
		if (setList == null) {
			setList = new ArrayList<HashLinkSet>();
		}
		
		if (set.elementArray.size() != 0) {
			setList.add(set);
		}
		
		if (set.setList != null) {
			setList.addAll(set.setList);
		}
	}
	
	public IArray getElementArray() {
		return elementArray;
	}
}
