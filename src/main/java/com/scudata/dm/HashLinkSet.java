package com.scudata.dm;

import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.util.HashUtil;

public class HashLinkSet {
	private IArray elementArray; // ��ϣ���ŵ���Ԫ�ص�λ�ã���Ҫ����λ�õ�Դ��ȡԪ��
	private HashUtil hashUtil; // ���ڼ����ϣֵ
	private int []entries; // ��ϣ������Ź�ϣֵ��Ӧ�����һ����¼��λ��
	private int []linkArray; // ��ϣֵ��ͬ�ļ�¼����
	private int capacity;
	
	public HashLinkSet() {
		this(Env.getDefaultHashCapacity());
	}
	
	public HashLinkSet(int capacity) {
		hashUtil = new HashUtil(capacity);
		capacity = hashUtil.getCapacity();
		entries = new int[capacity];
		elementArray = new ObjectArray(capacity);
		linkArray = new int[capacity + 1];
		this.capacity = capacity;
	}
	
	public void put(Object value) {
		IArray elementArray = this.elementArray;
		int hash = hashUtil.hashCode(value);
		int seq = entries[hash];

		while (seq != 0) {
			if (elementArray.isEquals(seq, value)) {
				return;
			} else {
				seq = linkArray[seq];
			}
		}
		
		int count = elementArray.size() + 1;
		if (count <= capacity) {
			elementArray.push(value);
			linkArray[count] = entries[hash];
			entries[hash] = count;
		} else {
			// Ԫ�������������������ϣ��
			hashUtil = new HashUtil(capacity * 2);
			capacity = hashUtil.getCapacity();
			entries = new int[capacity];
			
			if (capacity < count) {
				// ��ϣ�������ﵽ�趨�����ֵ
				capacity = (int)((long)capacity * 3  / 2);
				if (capacity < 0) {
					capacity = Integer.MAX_VALUE;
				}
			}
			
			linkArray = new int[capacity + 1];
			elementArray.ensureCapacity(capacity);
			elementArray.push(value);
			
			HashUtil hashUtil = this.hashUtil;
			int []entries = this.entries;
			int []linkArray = this.linkArray;
			
			for (int i = 1; i <= count; ++i) {
				hash = hashUtil.hashCode(elementArray.get(i));
				linkArray[i] = entries[hash];
				entries[hash] = i;
			}
		}
	}
	
	public IArray getElementArray() {
		return elementArray;
	}
}
