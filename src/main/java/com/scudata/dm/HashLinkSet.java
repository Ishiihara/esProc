package com.scudata.dm;

import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;

public class HashLinkSet {
	private static final int DEFAULT_CAPACITY = 0xFF;
	private static final int MAX_CAPACITY = 0x3fffffff;
	
	private IArray elementArray; // ��ϣ����ŵ���Ԫ�ص�λ�ã���Ҫ����λ�õ�Դ��ȡԪ��
	private int []entries; // ��ϣ��������Ź�ϣֵ��Ӧ�����һ����¼��λ��
	private int []linkArray; // ��ϣֵ��ͬ�ļ�¼����
	private int capacity;
	
	public HashLinkSet() {
		capacity = DEFAULT_CAPACITY;
		entries = new int[capacity + 1];
		elementArray = new ObjectArray(capacity);
		linkArray = new int[capacity + 1];
	}
	
	public HashLinkSet(IArray src) {
		capacity = DEFAULT_CAPACITY;
		entries = new int[capacity + 1];
		elementArray = src.newInstance(capacity);
		linkArray = new int[capacity + 1];
	}
	
	public HashLinkSet(int capacity) {
		int n = 0xF;
		while (capacity < n) {
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
	
	public int size() {
		return elementArray.size();
	}
	
	private int hashCode(Object value) {
		int h = value != null ? value.hashCode() : 0;
		return (h + (h >> 16)) & capacity;
	}
	
	private int hashCode(int h) {
		return (h + (h >> 16)) & capacity;
	}
	
	public void putAll(IArray array) {
		for (int i = 1, len = array.size(); i <= len; ++i) {
			put(array, i);
		}
	}
	
	public void put(IArray array, int index) {
		IArray elementArray = this.elementArray;
		int hash = hashCode(array.hashCode(index));
		int seq = entries[hash];

		while (seq != 0) {
			if (elementArray.isEquals(seq, array, index)) {
				return;
			} else {
				seq = linkArray[seq];
			}
		}
		
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
		} else if (count < MAX_CAPACITY) {
			// Ԫ�������������������ϣ��
			capacity = (capacity << 1) + 1;
			entries = new int[capacity + 1];
						
			linkArray = new int[capacity + 1];
			elementArray.ensureCapacity(capacity);
			elementArray.push(value);
			
			resize();
		} else {
			throw new RuntimeException();
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
	
	public void putAll(HashLinkSet set) {
		if (set == null || set.size() == 0) {
			return;
		}

		int capacity = this.capacity;
		if (set.capacity == capacity) {
			IArray elementArray = this.elementArray;
			int []entries = this.entries;
			int []linkArray = this.linkArray;
			IArray elementArray2 = set.elementArray;
			int []entries2 = set.entries;
			int []linkArray2 = set.linkArray;

			int newCapacity = capacity;
			int totalCount = elementArray.size();
			
			for (int h = 0; h <= capacity; ++h) {
				int seq1 = entries[h];
				
				Next:
				for (int seq2 = entries2[h]; seq2 != 0; seq2 = linkArray2[seq2]) {
					for (int q = seq1; q != 0; q = linkArray[q]) {
						if (elementArray.isEquals(q, elementArray2, seq2)) {
							continue Next;
						}
					}
					
					if (newCapacity > capacity) {
						elementArray.push(elementArray2, seq2);
					} else {
						// �Ҳ�set�ĳ�Ա���ڵ�ǰset��
						totalCount++;
						if (totalCount <= newCapacity) {
							elementArray.push(elementArray2, seq2);
							linkArray[totalCount] = entries[h];
							entries[h] = totalCount;
						} else if (totalCount < MAX_CAPACITY) {
							newCapacity = (newCapacity << 1) + 1;
							elementArray.ensureCapacity(newCapacity);
							elementArray.push(elementArray2, seq2);
						}
					}
				}
			}
			
			if (newCapacity > capacity) {
				entries = new int[newCapacity + 1];
				linkArray = new int[newCapacity + 1];
				resize();
			}
		} else {
			putAll(set.elementArray);
		}
	}
	
	public IArray getElementArray() {
		return elementArray;
	}
}