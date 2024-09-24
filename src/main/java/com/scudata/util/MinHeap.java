package com.scudata.util;

import java.util.Comparator;

import com.scudata.dm.comparator.BaseComparator;

/**
 * ����ȡ��С��n��ֵ�Ķ�
 * @author WangXiaoJun
 *
 */
public class MinHeap {
	// �����(�Ѷ�Ԫ�����)������Ԫ�ؽ���ʱ�ȸ��Ѷ����бȽϣ�����ȶѶ�������
	private Object []heap; // ���ڱ�������������ڵ��ֵ������0��λ�ÿ���
	private int maxSize; // ��ౣ����ֵ������
	private int currentSize; // ��ǰ���е�ֵ������
	private Comparator<Object> comparator; // ֵ�Ƚ���

	/**
	 * ����ȡmaxSize����Сֵ�Ķ�
	 * @param maxSize ����
	 */
	public MinHeap(int maxSize) {
		this(maxSize, new BaseComparator());
	}
	
	/**
	 * ����ȡmaxSize����Сֵ�Ķ�
	 * @param maxSize ����
	 * @param comparator �Ƚ���
	 */
	public MinHeap(int maxSize, Comparator<Object> comparator) {
		this.heap = new Object[maxSize + 1];
		this.maxSize = maxSize;
		this.currentSize = 0;
		this.comparator = comparator;
	}

	/**
	 * ���ص�ǰ��ֵ����
	 * @return ����
	 */
	public int size() {
		return currentSize;
	}

	/**
	 * ������ֵ
	 * @param o ֵ
	 * @return true����ǰֵ��ʱ����С��maxSize��ֵ��Χ�ڣ�false����ǰֵ̫�󱻶���
	 */
	public boolean insert(Object o) {
		Object []heap = this.heap;
		if (currentSize == maxSize) {
			if (comparator.compare(o, heap[1]) >= 0) {
				return false;
			} else {
				deleteRoot();
				return insert(o);
			}
		} else {
			int i = ++currentSize;
			while (i != 1 && comparator.compare(o, heap[i/2]) > 0) {
				heap[i] = heap[i/2]; // ��Ԫ������
				i /= 2;              // ���򸸽ڵ�
			}

			heap[i] = o;
			return true;
		}
	}
	
	/**
	 * ����һ���ѵ����ݼӵ���ǰ��
	 * @param other
	 */
	public void insertAll(MinHeap other) {
		Object []heap = other.heap;
		for (int i = 1, currentSize = other.currentSize; i <= currentSize; ++i) {
			insert(heap[i]);
		}
	}

	/**
	 * ɾ�����ڵ�
	 */
	private void deleteRoot() {
		// �����һ��Ԫ�ط��ڶѶ���Ȼ���Զ����µ���
		Object []heap = this.heap;
		int currentSize = this.currentSize;
		Object o = heap[currentSize];

		int i = 1;
		int c = 2; // �ӽڵ�
		while(c < currentSize) {
			// �ҳ��ϴ���ӽڵ�
			int rc = c + 1;  // ���ӽڵ�
			if (rc < currentSize && comparator.compare(heap[rc], heap[c]) > 0) {
				c = rc;
			}

			if (comparator.compare(o, heap[c]) < 0) {
				heap[i] = heap[c];
				i = c;
				c *= 2;
			} else {
				break;
			}
		}

		heap[i] = o;
		heap[currentSize] = null;
		this.currentSize--;
	}

	/**
	 * ��������Ԫ��
	 * @return Ԫ������
	 */
	public Object[] toArray() {
		Object []objs = new Object[currentSize];
		System.arraycopy(heap, 1, objs, 0, currentSize);
		//Arrays.sort(objs);
		return objs;
	}
	
	/**
	 * ȡ�Ѷ�Ԫ��
	 * @return
	 */
	public Object getTop() {
		return heap[1];
	}
}
