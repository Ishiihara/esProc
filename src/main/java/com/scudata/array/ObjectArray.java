package com.scudata.array;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.expression.Relation;
import com.scudata.expression.fn.math.And;
import com.scudata.expression.fn.math.Bit1;
import com.scudata.expression.fn.math.Not;
import com.scudata.expression.fn.math.Or;
import com.scudata.expression.fn.math.Xor;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.MultithreadUtil;
import com.scudata.util.CursorUtil;
import com.scudata.util.Variant;

/**
 * �������飬��1��ʼ����
 * @author WangXiaoJun
 *
 */
public class ObjectArray implements IArray {
	private static final long serialVersionUID = 1L;

	private Object []datas;
	private int size;

	public ObjectArray() {
		datas = new Object[DEFAULT_LEN];
	}
	
	public ObjectArray(int initialCapacity) {
		datas = new Object[++initialCapacity];
	}
	
	public ObjectArray(Object []values) {
		size = values.length;
		datas = new Object[size + 1];
		System.arraycopy(values, 0, datas, 1, size);
	}
	
	/**
	 * ֱ�����ô�������鴴��ObjectArray
	 * @param datas ���飬��0��λ�ÿյ�
	 * @param size Ԫ����
	 */
	public ObjectArray(Object []datas, int size) {
		this.datas = datas;
		this.size = size;
	}
	
	public Object[] getDatas() {
		return datas;
	}
	
	/**
	 * ȡ��������ʹ������ڴ�����Ϣ��ʾ
	 * @return ���ʹ�
	 */
	public String getDataType() {
		Object []datas = this.datas;
		for (int i = 1, size = this.size; i <= size; ++i) {
			if (datas[i] != null) {
				return Variant.getDataType(datas[i]);
			}
		}
		
		MessageManager mm = EngineMessage.get();
		return mm.getMessage("DataType.Null");
	}
	
	/**
	 * ��������
	 * @return
	 */
	public IArray dup() {
		int len = size + 1;
		Object []newDatas = new Object[len];
		System.arraycopy(datas, 0, newDatas, 0, len);
		return new ObjectArray(newDatas, size);
	}
	
	/**
	 * д���ݵ���
	 * @param out �����
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		int size = this.size;
		Object []datas = this.datas;
		
		out.writeByte(1);
		out.writeInt(size);
		for (int i = 1; i <= size; ++i) {
			out.writeObject(datas[i]);
		}
	}
	
	/**
	 * �����ж�����
	 * @param in ������
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		in.readByte();
		size = in.readInt();
		int len = size + 1;
		Object []datas = this.datas = new Object[len];
		
		for (int i = 1; i < len; ++i) {
			datas[i] = in.readObject();
		}
	}
	
	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		int size = this.size;
		Object []datas = this.datas;
		
		out.writeInt(size);
		out.writeInt(datas.length); // Ϊ�˼���֮ǰ��ListBase1
		
		for (int i = 1; i <= size; ++i) {
			out.writeObject(datas[i], true);
		}

		return out.toByteArray();
	}
	
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		int size = this.size = in.readInt();
		in.readInt(); // Ϊ�˼���֮ǰ��ListBase1
		
		Object []datas = this.datas = new Object[size + 1];
		for (int i = 1; i <= size; ++i) {
			datas[i] = in.readObject(true);
		}
	}
	
	/**
	 * ����һ��ͬ���͵�����
	 * @param count
	 * @return
	 */
	public IArray newInstance(int count) {
		return new ObjectArray(count);
	}
	
	/**
	 * ׷��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void add(Object o) {
		ensureCapacity(size + 1);
		datas[++size] = o;
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(IArray array) {
		int size2 = array.size();
		if (size2 == 0) {
		} else if (array instanceof ObjectArray) {
			ObjectArray objectArray = (ObjectArray)array;
			ensureCapacity(size + size2);
			
			System.arraycopy(objectArray.datas, 1, datas, size + 1, size2);
			size += size2;
		} else {
			ensureCapacity(size + size2);
			Object []datas = this.datas;
			
			for (int i = 1; i <= size2; ++i) {
				datas[++size] = array.get(i);
			}
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 * @param count Ԫ�ظ���
	 */
	public void addAll(IArray array, int count) {
		if (array instanceof ObjectArray) {
			ObjectArray objectArray = (ObjectArray)array;
			ensureCapacity(size + count);
			
			System.arraycopy(objectArray.datas, 1, datas, size + 1, count);
			size += count;
		} else {
			ensureCapacity(size + count);
			Object []datas = this.datas;
			
			for (int i = 1; i <= count; ++i) {
				datas[++size] = array.get(i);
			}
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 * @param index Ҫ��������ݵ���ʼλ��
	 * @param count ����
	 */
	public void addAll(IArray array, int index, int count) {
		if (array instanceof ObjectArray) {
			ObjectArray objectArray = (ObjectArray)array;
			ensureCapacity(size + count);
			
			System.arraycopy(objectArray.datas, index, datas, size + 1, count);
			size += count;
		} else {
			ensureCapacity(size + count);
			Object []datas = this.datas;
			
			for (int i = 1; i <= count; ++i, ++index) {
				datas[++size] = array.get(index);
			}
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(Object []array) {
		int size2 = array.length;
		ensureCapacity(size + size2);
		System.arraycopy(array, 0, datas, size + 1, size2);
		size += size2;
	}
	
	/**
	 * ����Ԫ�أ�������Ͳ��������׳��쳣
	 * @param index ����λ�ã���1��ʼ����
	 * @param o Ԫ��ֵ
	 */
	public void insert(int index, Object o) {
		ensureCapacity(size + 1);
		
		size++;
		System.arraycopy(datas, index, datas, index + 1, size - index);
		
		datas[index] = o;
	}

	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, IArray array) {
		int numNew = array.size();
		ensureCapacity(size + numNew);
		
		Object []datas = this.datas;
		System.arraycopy(datas, pos, datas, pos + numNew, size - pos + 1);
		
		for (int i = 1; i <= numNew; ++i, ++pos) {
			datas[pos] = array.get(i);
		}
		
		size += numNew;
	}
	
	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, Object []array) {
		int numNew = array.length;
		ensureCapacity(size + numNew);
		
		System.arraycopy(datas, pos, datas, pos + numNew, size - pos + 1);
		System.arraycopy(array, 0, datas, pos, numNew);
		size += numNew;
	}
	
	/**
	 * ׷��Ԫ�أ��������������Ϊ���㹻�ռ���Ԫ�أ���������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void push(Object o) {
		datas[++size] = o;
	}
	
	/**
	 * ׷��һ���ճ�Ա���������������Ϊ���㹻�ռ���Ԫ�أ�
	 */
	public void pushNull() {
		datas[++size] = null;
	}
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void push(IArray array, int index) {
		datas[++size] = array.get(index);
	}
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void add(IArray array, int index) {
		ensureCapacity(size + 1);
		datas[++size] = array.get(index);
	}
	
	/**
	 * ��array�еĵ�index��Ԫ���������ǰ�����ָ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param curIndex ��ǰ�����Ԫ����������1��ʼ����
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void set(int curIndex, IArray array, int index) {
		datas[curIndex] = array.get(index);
	}
	
	/**
	 * ȡָ��λ��Ԫ��
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public Object get(int index) {
		return datas[index];
	}

	/**
	 * ȡָ��λ��Ԫ�ص�����ֵ
	 * @param index ��������1��ʼ����
	 * @return ����ֵ
	 */
	public int getInt(int index) {
		return ((Number)datas[index]).intValue();
	}
	
	/**
	 * ȡָ��λ��Ԫ�صĳ�����ֵ
	 * @param index ��������1��ʼ����
	 * @return ������ֵ
	 */
	public long getLong(int index) {
		return ((Number)datas[index]).longValue();
	}
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param indexArray λ������
	 * @return IArray
	 */
	public IArray get(int []indexArray) {
		Object []datas = this.datas;
		int len = indexArray.length;
		ObjectArray result = new ObjectArray(len);
		
		for (int i : indexArray) {
			result.push(datas[i]);
		}
		
		return result;
	}
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param indexArray λ������
	 * @param start ��ʼλ�ã�����
	 * @param end ����λ�ã�����
	 * @param doCheck true��λ�ÿ��ܰ���0��0��λ����null��䣬false���������0
	 * @return IArray
	 */
	public IArray get(int []indexArray, int start, int end, boolean doCheck) {
		Object []datas = this.datas;
		int len = end - start + 1;
		Object []resultDatas = new Object[len + 1];
		
		if (doCheck) {
			for (int i = 1; start <= end; ++start, ++i) {
				int q = indexArray[start];
				if (q > 0) {
					resultDatas[i] = datas[q];
				}
			}
		} else {
			for (int i = 1; start <= end; ++start) {
				resultDatas[i++] = datas[indexArray[start]];
			}
		}
		
		return new ObjectArray(resultDatas, len);
	}
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param IArray λ������
	 * @return IArray
	 */
	public IArray get(IArray indexArray) {
		Object []datas = this.datas;
		int len = indexArray.size();
		ObjectArray result = new ObjectArray(len);
		
		for (int i = 1; i <= len; ++i) {
			if (indexArray.isNull(i)) {
				result.pushNull();
			} else {
				result.push(datas[indexArray.getInt(i)]);
			}
		}
		
		return result;
	}
	
	/**
	 * ȡĳһ�������������
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã���������
	 * @return IArray
	 */
	public IArray get(int start, int end) {
		int newSize = end - start;
		Object []newDatas = new Object[newSize + 1];
		System.arraycopy(datas, start, newDatas, 1, newSize);
		return new ObjectArray(newDatas, newSize);
	}
	
	/**
	 * ʹ�б��������С��minCapacity
	 * @param minCapacity ��С����
	 */
	public void ensureCapacity(int minCapacity) {
		if (datas.length <= minCapacity) {
			int newCapacity = (datas.length * 3) / 2;
			if (newCapacity <= minCapacity) {
				newCapacity = minCapacity + 1;
			}

			Object []newDatas = new Object[newCapacity];
			System.arraycopy(datas, 0, newDatas, 0, size + 1);
			datas = newDatas;
		}
	}
	
	/**
	 * ����������ʹ����Ԫ�������
	 */
	public void trimToSize() {
		int newLen = size + 1;
		if (newLen < datas.length) {
			Object []newDatas = new Object[newLen];
			System.arraycopy(datas, 0, newDatas, 0, newLen);
			datas = newDatas;
		}
	}
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ��ǿ�
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isNull(int index) {
		return datas[index] == null;
	}
	
	/**
	 * �ж�Ԫ���Ƿ���True
	 * @return BoolArray
	 */
	public BoolArray isTrue() {
		int size = this.size;
		Object []datas = this.datas;
		boolean []resultDatas = new boolean[size + 1];
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = Variant.isTrue(datas[i]);
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}
	
	/**
	 * �ж�Ԫ���Ƿ��Ǽ�
	 * @return BoolArray
	 */
	public BoolArray isFalse() {
		int size = this.size;
		Object []datas = this.datas;
		boolean []resultDatas = new boolean[size + 1];
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = Variant.isFalse(datas[i]);
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ���True
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isTrue(int index) {
		return Variant.isTrue(datas[index]);
	}
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ���False
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isFalse(int index) {
		return Variant.isFalse(datas[index]);
	}

	/**
	 * �Ƿ��Ǽ����������ʱ���������飬��ʱ�����Ŀ��Ա��޸ģ����� f1+f2+f3��ֻ�����һ�������Ž��
	 * @return true������ʱ���������飬false��������ʱ����������
	 */
	public boolean isTemporary() {
		return datas[0] == Boolean.TRUE;
	}

	/**
	 * �����Ƿ��Ǽ����������ʱ����������
	 * @param ifTemporary true������ʱ���������飬false��������ʱ����������
	 */
	public void setTemporary(boolean ifTemporary) {
		datas[0] = Boolean.valueOf(isTemporary());
	}
	
	/**
	 * ɾ��ָ��λ�õ�Ԫ��
	 * @param index ��������1��ʼ����
	 */
	public void remove(int index) {
		System.arraycopy(datas, index + 1, datas, index, size - index);
		datas[size--] = null;
	}
	
	/**
	 * ɾ�����һ��Ԫ��
	 */
	public void removeLast() {
		datas[size--] = null;
	}
	
	/**
	 * ɾ��ָ�������ڵ�Ԫ��
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 */
	public void removeRange(int fromIndex, int toIndex) {
		System.arraycopy(datas, toIndex + 1, datas, fromIndex, size - toIndex);
		
		int newSize = size - (toIndex - fromIndex + 1);
		while (size != newSize) {
			datas[size--] = null;
		}
	}
	
	/**
	 * ɾ��ָ��λ�õ�Ԫ�أ���Ŵ�С��������
	 * @param seqs ��������
	 */
	public void remove(int []seqs) {
		int delCount = 0;
		Object []datas = this.datas;
		
		for (int i = 0, len = seqs.length; i < len; ) {
			int cur = seqs[i];
			i++;

			int moveCount;
			if (i < len) {
				moveCount = seqs[i] - cur - 1;
			} else {
				moveCount = size - cur;
			}

			if (moveCount > 0) {
				System.arraycopy(datas, cur + 1, datas, cur - delCount, moveCount);
			}
			
			delCount++;
		}

		for (int i = 0, q = size; i < delCount; ++i) {
			datas[q - i] = null;
		}
		
		size -= delCount;
	}
	
	/**
	 * ����ָ�������ڵ�����
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã�������
	 */
	public void reserve(int start, int end) {
		int newSize = end - start + 1;
		System.arraycopy(datas, start, datas, 1, newSize);
		
		for (int i = size; i > newSize; --i) {
			datas[i] = null;
		}
		
		size = newSize;
	}
	
	public int size() {
		return size;
	}
	
	/**
	 * ��������ȡֵΪ���Ԫ����Ŀ
	 * @return ȡֵΪ���Ԫ����Ŀ
	 */
	public int count() {
		Object []datas = this.datas;
		int size = this.size;
		int count = size;
		
		for (int i = 1; i <= size; ++i) {
			if (Variant.isFalse(datas[i])) {
				count--;
			}
		}
		
		return count;
	}
	
	/**
	 * �ж������Ƿ���ȡֵΪtrue��Ԫ��
	 * @return true���У�false��û��
	 */
	public boolean containTrue() {
		int size = this.size;
		if (size == 0) {
			return false;
		}
		
		Object []datas = this.datas;
		for (int i = 1; i <= size; ++i) {
			if (Variant.isTrue(datas[i])) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * ���ص�һ����Ϊ�յ�Ԫ��
	 * @return Object
	 */
	public Object ifn() {
		int size = this.size;
		Object []datas = this.datas;
		
		for (int i = 1; i <= size; ++i) {
			if (datas[i] != null) {
				return datas[i];
			}
		}
		
		return null;
	}

	/**
	 * �޸�����ָ��Ԫ�ص�ֵ��������Ͳ��������׳��쳣
	 * @param index ��������1��ʼ����
	 * @param obj ֵ
	 */
	public void set(int index, Object obj) {
		datas[index] = obj;
	}
	
	/**
	 * ɾ�����е�Ԫ��
	 */
	public void clear() {
		Object []datas = this.datas;
		int size = this.size;
		this.size = 0;
		
		while (size > 0) {
			datas[size--] = null;
		}
	}
	
	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @param comparator �Ƚ���
	 * @return int Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem, Comparator<Object> comparator) {
		Object []datas = this.datas;
		int low = 1, high = size;
		
		while (low <= high) {
			int mid = (low + high) >> 1;
			int cmp = comparator.compare(datas[mid], elem);
			if (cmp < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				return mid; // key found
			}
		}

		return -low; // key not found
	}
	
	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @return int Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem) {
		if (elem != null) {
			Object []datas = this.datas;
			int low = 1, high = size;
			
			while (low <= high) {
				int mid = (low + high) >> 1;
				int cmp = Variant.compare(datas[mid], elem, true);
				if (cmp < 0) {
					low = mid + 1;
				} else if (cmp > 0) {
					high = mid - 1;
				} else {
					return mid; // key found
				}
			}

			return -low; // key not found
		} else {
			if (size > 0 && datas[1] == null) {
				return 1;
			} else {
				return -1;
			}
		}
	}
	
	// ���鰴�������򣬽��н�����ֲ���
	private int descBinarySearch(Object elem) {
		Object []datas = this.datas;
		int low = 1, high = size;
		
		while (low <= high) {
			int mid = (low + high) >> 1;
			int cmp = Variant.compare(datas[mid], elem, true);
			if (cmp < 0) {
				high = mid - 1;
			} else if (cmp > 0) {
				low = mid + 1;
			} else {
				return mid; // key found
			}
		}

		return -low; // key not found
	}
	
	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @param start ��ʼ����λ�ã�������
	 * @param end ��������λ�ã�������
	 * @return Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem, int start, int end) {
		if (elem != null) {
			Object []datas = this.datas;
			int low = start, high = end;
			
			while (low <= high) {
				int mid = (low + high) >> 1;
				int cmp = Variant.compare(datas[mid], elem, true);
				if (cmp < 0) {
					low = mid + 1;
				} else if (cmp > 0) {
					high = mid - 1;
				} else {
					return mid; // key found
				}
			}

			return -low; // key not found
		} else {
			if (end > 0 && datas[start] == null) {
				return start;
			} else {
				return -1;
			}
		}
	}
	
	/**
	 * �����б����Ƿ����ָ��Ԫ��
	 * @param elem Object �����ҵ�Ԫ��
	 * @return boolean true��������false��������
	 */
	public boolean contains(Object elem) {
		if (elem != null) {
			Object []datas = this.datas;
			int size = this.size;
			
			for (int i = 1; i <= size; ++i) {
				if (Variant.isEquals(datas[i], elem)) {
					return true;
				}
			}
			
			return false;
		} else {
			int size = this.size;
			Object []datas = this.datas;
			for (int i = 1; i <= size; ++i) {
				if (datas[i] == null) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	/**
	 * �ж������Ԫ���Ƿ��ڵ�ǰ������
	 * @param isSorted ��ǰ�����Ƿ�����
	 * @param array ����
	 * @param result ���ڴ�Ž����ֻ��ȡֵΪtrue��
	 */
	public void contains(boolean isSorted, IArray array, BoolArray result) {
		int resultSize = result.size();
		if (isSorted) {
			for (int i = 1; i <= resultSize; ++i) {
				if (result.isTrue(i) && binarySearch(array.get(i)) < 1) {
					result.set(i, false);
				}
			}
		} else {
			for (int i = 1; i <= resultSize; ++i) {
				if (result.isTrue(i) && !contains(array.get(i))) {
					result.set(i, false);
				}
			}
		}
	}
	
	/**
	 * �����б����Ƿ����ָ��Ԫ�أ�ʹ�õȺűȽ�
	 * @param elem
	 * @return boolean true��������false��������
	 */
	public boolean objectContains(Object elem) {
		Object []datas = this.datas;
		for (int i = 1, size = this.size; i <= size; ++i) {
			if (datas[i] == elem) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * ����Ԫ�����������״γ��ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start ��ʼ����λ�ã�������
	 * @return ���Ԫ�ش����򷵻�ֵ����0�����򷵻�0
	 */
	public int firstIndexOf(Object elem, int start) {
		if (elem != null) {
			Object []datas = this.datas;
			int size = this.size;
			
			for (int i = start; i <= size; ++i) {
				if (Variant.isEquals(datas[i], elem)) {
					return i;
				}
			}
			
			return 0;
		} else {
			int size = this.size;
			Object []datas = this.datas;
			for (int i = start; i <= size; ++i) {
				if (datas[i] == null) {
					return i;
				}
			}
			
			return 0;
		}
	}
	
	/**
	 * ����Ԫ���������������ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start �Ӻ��濪ʼ���ҵ�λ�ã�������
	 * @return ���Ԫ�ش����򷵻�ֵ����0�����򷵻�0
	 */
	public int lastIndexOf(Object elem, int start) {
		if (elem != null) {
			Object []datas = this.datas;
			
			for (int i = start; i > 0; --i) {
				if (Variant.isEquals(datas[i], elem)) {
					return i;
				}
			}
			
			return 0;
		} else {
			Object []datas = this.datas;
			for (int i = start; i > 0; --i) {
				if (datas[i] == null) {
					return i;
				}
			}
			
			return 0;
		}
	}
	
	/**
	 * ����Ԫ�������������г��ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start ��ʼ����λ�ã�������
	 * @param isSorted ��ǰ�����Ƿ�����
	 * @param isFromHead true����ͷ��ʼ������false����β��ǰ��ʼ����
	 * @return IntArray
	 */
	public IntArray indexOfAll(Object elem, int start, boolean isSorted, boolean isFromHead) {
		int size = this.size;
		Object []datas = this.datas;
		
		if (elem == null) {
			IntArray result = new IntArray(7);
			if (isSorted) {
				if (isFromHead) {
					for (int i = start; i <= size; ++i) {
						if (datas[i] == null) {
							result.addInt(i);
						} else {
							break;
						}
					}
				} else {
					for (int i = start; i > 0; --i) {
						if (datas[i] == null) {
							result.addInt(i);
						}
					}
				}
			} else {
				if (isFromHead) {
					for (int i = start; i <= size; ++i) {
						if (datas[i] == null) {
							result.addInt(i);
						}
					}
				} else {
					for (int i = start; i > 0; --i) {
						if (datas[i] == null) {
							result.addInt(i);
						}
					}
				}
			}
			
			return result;
		}

		if (isSorted) {
			int end = size;
			if (isFromHead) {
				end = start;
				start = 1;
			}
			
			int index = binarySearch(elem, start, end);
			if (index < 1) {
				return new IntArray(1);
			}
			
			// �ҵ���һ��
			int first = index;
			while (first > start && Variant.isEquals(datas[first - 1], elem)) {
				first--;
			}
			
			// �ҵ����һ��
			int last = index;
			while (last < end && Variant.isEquals(datas[last + 1], elem)) {
				last++;
			}
			
			IntArray result = new IntArray(last - first + 1);
			if (isFromHead) {
				for (; first <= last; ++first) {
					result.pushInt(first);
				}
			} else {
				for (; last >= first; --last) {
					result.pushInt(last);
				}
			}
			
			return result;
		} else {
			IntArray result = new IntArray(7);
			if (isFromHead) {
				for (int i = start; i <= size; ++i) {
					if (Variant.isEquals(datas[i], elem)) {
						result.addInt(i);
					}
				}
			} else {
				for (int i = start; i > 0; --i) {
					if (Variant.isEquals(datas[i], elem)) {
						result.addInt(i);
					}
				}
			}
			
			return result;
		}
	}
	
	/**
	 * �������Ա�����ֵ
	 * @return IArray ����ֵ����
	 */
	public IArray abs() {
		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = Variant.abs(datas[i]);
			}
			
			return this;
		} else {
			Object []newDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				newDatas[i] = Variant.abs(datas[i]);
			}
			
			ObjectArray result = new ObjectArray(newDatas, size);
			result.setTemporary(true);
			return result;
		}
	}

	/**
	 * �������Ա��
	 * @return IArray ��ֵ����
	 */
	public IArray negate() {
		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				Object obj = datas[i];
				if (obj instanceof Number) {
					datas[i] = Variant.negate((Number)obj);
				} else if (obj instanceof Date) {
					datas[i] = Variant.negate((Date)obj);
				} else if (obj instanceof String) {
					datas[i] = Variant.negate((String)obj);
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("\"-\"" +mm.getMessage("operator.numberRightOperation"));
				}
			}
			
			return this;
		} else {
			Object []newDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				Object obj = datas[i];
				if (obj instanceof Number) {
					newDatas[i] = Variant.negate((Number)obj);
				} else if (obj instanceof Date) {
					newDatas[i] = Variant.negate((Date)obj);
				} else if (obj instanceof String) {
					newDatas[i] = Variant.negate((String)obj);
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("\"-\"" +mm.getMessage("operator.numberRightOperation"));
				}
			}
			
			ObjectArray result = new ObjectArray(newDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	/**
	 * �������Ա���s
	 * @return IArray ��ֵ����
	 */
	public IArray not() {
		Object []datas = this.datas;
		int size = this.size;
		
		boolean []newDatas = new boolean[size + 1];
		for (int i = 1; i <= size; ++i) {
			newDatas[i] = Variant.isFalse(datas[i]);
		}
		
		IArray  result = new BoolArray(newDatas, size);
		result.setTemporary(true);
		return result;
	}

	/**
	 * �ж�����ĳ�Ա�Ƿ����������԰���null��
	 * @return true����������false�����з�����ֵ
	 */
	public boolean isNumberArray() {
		Object []datas = this.datas;
		int size = this.size;
		
		for (int i = 1; i <= size; ++i) {
			if (datas[i] != null && !(datas[i] instanceof Number)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�ĺ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberAdd(IArray array) {
		if (array instanceof IntArray) {
			return memberAdd((IntArray)array);
		} else if (array instanceof LongArray) {
			return memberAdd((LongArray)array);
		} else if (array instanceof DoubleArray) {
			return memberAdd((DoubleArray)array);
		} else if (array instanceof ConstArray) {
			return memberAdd(array.get(1));
		} else if (array instanceof ObjectArray) {
			return memberAdd((ObjectArray)array);
		} else if (array instanceof DateArray) {
			return ((DateArray)array).memberAdd(this);
		} else if (array instanceof StringArray) {
			return memberAdd((StringArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illAdd"));
		}
	}
	
	/**
	 * ��������ĳ�Ա��ָ�������ĺ�
	 * @param value ����
	 * @return ������
	 */
	public IArray memberAdd(Object value) {
		if (value == null) {
			return this;
		}

		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = Variant.add(datas[i], value);
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.add(datas[i], value);
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	protected IArray memberAdd(IntArray array) {
		int size = this.size;
		Object []datas = this.datas;
		int []d2 = array.getDatas();
		IArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (v == null) {
				if (!array.isNull(i)) {
					resultDatas[i] = d2[i];
				}
			} else if (array.isNull(i)) {
				resultDatas[i] = v;
			} else if (v instanceof Double || v instanceof Float) {
				resultDatas[i] = ((Number)v).doubleValue() + d2[i];
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).add(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.add(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).longValue() + d2[i];
			} else if (v instanceof String) {
				Number number = Variant.parseNumber((String)v);
				if (number == null) {
					resultDatas[i] = d2[i];
				} else if (number instanceof Double) {
					resultDatas[i] = number.doubleValue() + d2[i];
				} else {
					resultDatas[i] = number.longValue() + d2[i];
				}
			} else if (v instanceof Date) {
				resultDatas[i] = Variant.elapse((Date)v, d2[i], null);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illAdd"));
			}
		}
		
		return result;
	}
	
	protected IArray memberAdd(LongArray array) {
		int size = this.size;
		Object []datas = this.datas;
		long []d2 = array.getDatas();
		IArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (v == null) {
				if (!array.isNull(i)) {
					resultDatas[i] = d2[i];
				}
			} else if (array.isNull(i)) {
				resultDatas[i] = v;
			} else if (v instanceof Double || v instanceof Float) {
				resultDatas[i] = ((Number)v).doubleValue() + d2[i];
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).add(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.add(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).longValue() + d2[i];
			} else if (v instanceof String) {
				Number number = Variant.parseNumber((String)v);
				if (number == null) {
					resultDatas[i] = d2[i];
				} else if (number instanceof Double) {
					resultDatas[i] = number.doubleValue() + d2[i];
				} else {
					resultDatas[i] = number.longValue() + d2[i];
				}
			} else if (v instanceof Date) {
				resultDatas[i] = Variant.elapse((Date)v, (int)d2[i], null);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illAdd"));
			}
		}
		
		return result;
	}
	
	protected IArray memberAdd(DoubleArray array) {
		int size = this.size;
		Object []datas = this.datas;
		double []d2 = array.getDatas();
		IArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (v == null) {
				if (!array.isNull(i)) {
					resultDatas[i] = d2[i];
				}
			} else if (array.isNull(i)) {
				resultDatas[i] = v;
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).add(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.add(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).doubleValue() + d2[i];
			} else if (v instanceof String) {
				Number number = Variant.parseNumber((String)v);
				if (number == null) {
					resultDatas[i] = d2[i];
				} else {
					resultDatas[i] = number.doubleValue() + d2[i];
				}
			} else if (v instanceof Date) {
				resultDatas[i] = Variant.elapse((Date)v, (int)d2[i], null);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illAdd"));
			}
		}
		
		return result;
	}
	
	protected ObjectArray memberAdd(StringArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		String []d2 = array.getDatas();
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				d1[i] = Variant.add(d1[i], d2[i]);
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.add(d1[i], d2[i]);
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	private ObjectArray memberAdd(ObjectArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		Object []d2 = array.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				d1[i] = Variant.add(d1[i], d2[i]);
			}
			
			return this;
		} else if (array.isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				d2[i] = Variant.add(d1[i], d2[i]);
			}
			
			return array;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.add(d1[i], d2[i]);
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�Ĳ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberSubtract(IArray array) {
		if (array instanceof IntArray) {
			return memberSubtract((IntArray)array);
		} else if (array instanceof LongArray) {
			return memberSubtract((LongArray)array);
		} else if (array instanceof DoubleArray) {
			return memberSubtract((DoubleArray)array);
		} else if (array instanceof ConstArray) {
			return memberSubtract(array.get(1));
		} else if (array instanceof ObjectArray) {
			return memberSubtract((ObjectArray)array);
		} else if (array instanceof DateArray) {
			return memberSubtract((DateArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illSubtract"));
		}
	}

	/**
	 * ��������ĳ�Ա��ָ�������Ĳ�
	 * @param value ����
	 * @return ������
	 */
	private IArray memberSubtract(Object value) {
		if (value == null) {
			return this;
		}

		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = Variant.subtract(datas[i], value);
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.subtract(datas[i], value);
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}

	private IArray memberSubtract(IntArray array) {
		int size = this.size;
		Object []datas = this.datas;
		int []d2 = array.getDatas();
		IArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (v == null) {
				if (!array.isNull(i)) {
					resultDatas[i] = -d2[i];
				}
			} else if (array.isNull(i)) {
				resultDatas[i] = v;
			} else if (v instanceof Long) {
				resultDatas[i] = ((Long)v).longValue() - d2[i];
			} else if (v instanceof Double || v instanceof Float) {
				resultDatas[i] = ((Number)v).doubleValue() - d2[i];
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).subtract(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.subtract(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).intValue() - d2[i];
			} else if (v instanceof Date) {
				resultDatas[i] = Variant.elapse((Date)v, -d2[i], null);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illSubtract"));
			}
		}
		
		return result;
	}

	private IArray memberSubtract(LongArray array) {
		int size = this.size;
		Object []datas = this.datas;
		long []d2 = array.getDatas();
		IArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (v == null) {
				if (!array.isNull(i)) {
					resultDatas[i] = -d2[i];
				}
			} else if (array.isNull(i)) {
				resultDatas[i] = v;
			} else if (v instanceof Double || v instanceof Float) {
				resultDatas[i] = ((Number)v).doubleValue() - d2[i];
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).subtract(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.subtract(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).longValue() - d2[i];
			} else if (v instanceof Date) {
				resultDatas[i] = Variant.elapse((Date)v, -(int)d2[i], null);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illSubtract"));
			}
		}
		
		return result;
	}
	private IArray memberSubtract(DoubleArray array) {
		int size = this.size;
		Object []datas = this.datas;
		double []d2 = array.getDatas();
		IArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (v == null) {
				if (!array.isNull(i)) {
					resultDatas[i] = -d2[i];
				}
			} else if (array.isNull(i)) {
				resultDatas[i] = v;
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).subtract(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.subtract(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).doubleValue() - d2[i];
			} else if (v instanceof Date) {
				resultDatas[i] = Variant.elapse((Date)v, -(int)d2[i], null);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illSubtract"));
			}
		}
		
		return result;
	}

	private ObjectArray memberSubtract(ObjectArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		Object []d2 = array.datas;
		
		ObjectArray result;
		Object []resultDatas;
		if (isTemporary()) {
			result = this;
			resultDatas = d1;
		} else if (array.isTemporary()) {
			result = array;
			resultDatas = d2;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = Variant.subtract(d1[i], d2[i]);
		}
		
		return result;
	}
	
	private LongArray memberSubtract(DateArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		Date []d2 = array.getDatas();
		
		long []resultDatas = new long[size + 1];
		boolean []resultSigns = null;

		for (int i = 1; i <= size; ++i) {
			if (d1[i] == null || d2[i] == null) {
				if (resultSigns == null) {
					resultSigns = new boolean[size + 1];
				}
				
				resultSigns[i] = true;
			} if (d1[i] instanceof Date) {
				resultDatas[i] = Variant.dayInterval(d2[i], (Date)d1[i]);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(d1[i]) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illSubtract"));
			}
		}
		
		LongArray result = new LongArray(resultDatas, resultSigns, size);
		result.setTemporary(true);
		return result;
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ļ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberMultiply(IArray array) {
		if (array instanceof IntArray) {
			return memberMultiply((IntArray)array);
		} else if (array instanceof LongArray) {
			return memberMultiply((LongArray)array);
		} else if (array instanceof DoubleArray) {
			return memberMultiply((DoubleArray)array);
		} else if (array instanceof ConstArray) {
			return memberMultiply(array.get(1));
		} else if (array instanceof ObjectArray) {
			return memberMultiply((ObjectArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illMultiply"));
		}
	}

	/**
	 * ��������ĳ�Ա��ָ�������Ļ�
	 * @param value ����
	 * @return ������
	 */
	public IArray memberMultiply(Object value) {
		if (value == null) {
			return new ConstArray(null, size);
		}

		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = Variant.multiply(datas[i], value);
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.multiply(datas[i], value);
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}

	protected ObjectArray memberMultiply(IntArray array) {
		int size = this.size;
		Object []datas = this.datas;
		int []d2 = array.getDatas();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (array.isNull(i)) {
				resultDatas[i] = null;
			} else if (v instanceof Double || v instanceof Float) {
				resultDatas[i] = ((Number)v).doubleValue() * d2[i];
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).multiply(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.multiply(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).longValue() * d2[i];
			} else if (v instanceof Sequence) {
				Sequence sequence = (Sequence)v;
				resultDatas[i] = sequence.multiply(d2[i]);
			} else if (v != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illMultiply"));
			}
		}
		
		return result;
	}

	protected ObjectArray memberMultiply(LongArray array) {
		int size = this.size;
		Object []datas = this.datas;
		long []d2 = array.getDatas();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (array.isNull(i)) {
				resultDatas[i] = null;
			} else if (v instanceof Double || v instanceof Float) {
				resultDatas[i] = ((Number)v).doubleValue() * d2[i];
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).multiply(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.multiply(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).longValue() * d2[i];
			} else if (v instanceof Sequence) {
				Sequence sequence = (Sequence)v;
				resultDatas[i] = sequence.multiply((int)d2[i]);
			} else if (v != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illMultiply"));
			}
		}
		
		return result;
	}
	
	protected ObjectArray memberMultiply(DoubleArray array) {
		int size = this.size;
		Object []datas = this.datas;
		double []d2 = array.getDatas();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (array.isNull(i)) {
				resultDatas[i] = null;
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).multiply(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.multiply(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).doubleValue() * d2[i];
			} else if (v instanceof Sequence) {
				Sequence sequence = (Sequence)v;
				resultDatas[i] = sequence.multiply((int)d2[i]);
			} else if (v != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illMultiply"));
			}
		}
		
		return result;
	}

	private ObjectArray memberMultiply(ObjectArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		Object []d2 = array.datas;
		
		ObjectArray result;
		Object []resultDatas;
		if (isTemporary()) {
			result = this;
			resultDatas = d1;
		} else if (array.isTemporary()) {
			result = array;
			resultDatas = d2;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = Variant.multiply(d1[i], d2[i]);
		}
		
		return result;
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�ĳ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberDivide(IArray array) {
		if (array instanceof IntArray) {
			return memberDivide((IntArray)array);
		} else if (array instanceof LongArray) {
			return memberDivide((LongArray)array);
		} else if (array instanceof DoubleArray) {
			return memberDivide((DoubleArray)array);
		} else if (array instanceof ConstArray) {
			return memberDivide(array.get(1));
		} else if (array instanceof ObjectArray) {
			return memberDivide((ObjectArray)array);
		} else if (array instanceof StringArray) {
			return memberDivide((StringArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}

	private IArray memberDivide(Object value) {
		if (value == null) {
			return new ConstArray(null, size);
		}

		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = Variant.divide(datas[i], value);
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.divide(datas[i], value);
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}

	private ObjectArray memberDivide(IntArray array) {
		int size = this.size;
		Object []datas = this.datas;
		int []d2 = array.getDatas();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (array.isNull(i)) {
				if (v instanceof String) {
					resultDatas[i] = v;
				} else {
					resultDatas[i] = null;
				}
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).divide(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.divide(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).doubleValue() / d2[i];
			} else if (v instanceof String) {
				resultDatas[i] = (String)v + d2[i];
			} else if (v != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illMultiply"));
			}
		}
		
		return result;
	}

	private ObjectArray memberDivide(LongArray array) {
		int size = this.size;
		Object []datas = this.datas;
		long []d2 = array.getDatas();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (array.isNull(i)) {
				if (v instanceof String) {
					resultDatas[i] = v;
				} else {
					resultDatas[i] = null;
				}
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).divide(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.divide(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).doubleValue() / d2[i];
			} else if (v instanceof String) {
				resultDatas[i] = (String)v + d2[i];
			} else if (v != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illMultiply"));
			}
		}
		
		return result;
	}

	private ObjectArray memberDivide(DoubleArray array) {
		int size = this.size;
		Object []datas = this.datas;
		double []d2 = array.getDatas();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = datas;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = datas[i];
			if (array.isNull(i)) {
				if (v instanceof String) {
					resultDatas[i] = v;
				} else {
					resultDatas[i] = null;
				}
			} else if (v instanceof BigDecimal) {
				resultDatas[i] = ((BigDecimal)v).divide(new BigDecimal(d2[i]));
			} else if (v instanceof BigInteger) {
				BigDecimal decimal = new BigDecimal((BigInteger)v);
				resultDatas[i] = decimal.divide(new BigDecimal(d2[i]));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).doubleValue() / d2[i];
			} else if (v instanceof String) {
				resultDatas[i] = (String)v + d2[i];
			} else if (v != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illMultiply"));
			}
		}
		
		return result;
	}
	
	private StringArray memberDivide(StringArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		String []d2 = array.getDatas();
		
		if (array.isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				if (d1 != null) {
					if (d2[i] != null) {
						d2[i] = d1[i] + d2[i];
					} else {
						d2[i] = d1[i].toString();
					}
				}
			}
			
			return array;
		} else {
			String []resultDatas = new String[size + 1];
			for (int i = 1; i <= size; ++i) {
				if (d2[i] != null) {
					if (d1 != null) {
						resultDatas[i] = d1[i] + d2[i];
					} else {
						resultDatas[i] = d2[i];
					}
				} else if (d1 != null) {
					resultDatas[i] = d1[i].toString();
				}
			}
			
			StringArray result = new StringArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	private ObjectArray memberDivide(ObjectArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		Object []d2 = array.datas;
		
		ObjectArray result;
		Object []resultDatas;
		if (isTemporary()) {
			result = this;
			resultDatas = d1;
		} else if (array.isTemporary()) {
			result = array;
			resultDatas = d2;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = Variant.divide(d1[i], d2[i]);
		}
		
		return result;
	}
	
	/**
	 * ����������������Ӧ������Աȡ������г�Ա�����
	 * @param array �Ҳ�����
	 * @return ����������������������
	 */
	public IArray memberMod(IArray array) {
		if (array instanceof IntArray) {
			return memberMod((IntArray)array);
		} else if (array instanceof LongArray) {
			return memberMod((LongArray)array);
		} else if (array instanceof DoubleArray) {
			return memberMod((DoubleArray)array);
		} else if (array instanceof ConstArray) {
			return memberMod(array.get(1));
		} else if (array instanceof ObjectArray) {
			return memberMod((ObjectArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illMod"));
		}
	}
	
	private IArray memberMod(IntArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		int []d2 = array.getDatas();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = d1;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = d1[i];
			if (array.isNull(i)) {
				if (v instanceof Number || v == null) {
					resultDatas[i] = null;
				} else if (v instanceof Sequence) {
					resultDatas[i] = v;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
							array.getDataType() + mm.getMessage("Variant2.illMod"));
				}
			} else if (v instanceof Long) {
				resultDatas[i] = ((Number)v).longValue() % d2[i];
			} else if (v instanceof Double || v instanceof Float) {
				resultDatas[i] = ((Number)v).doubleValue() % d2[i];
			} else if (v instanceof BigDecimal) {
				BigInteger bi = ((BigDecimal)v).toBigInteger();
				resultDatas[i] = new BigDecimal(bi.mod(BigInteger.valueOf(d2[i])));
			} else if (v instanceof BigInteger) {
				BigInteger bi = (BigInteger)v;
				resultDatas[i] = new BigDecimal(bi.mod(BigInteger.valueOf(d2[i])));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).intValue() % d2[i];
			} else if (v instanceof Sequence) {
				Sequence seq2 = new Sequence(1);
				seq2.add(d2[i]);
				resultDatas[i] = CursorUtil.xor(((Sequence)v), seq2);
			} else if (v != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illMod"));
			}
		}
		
		return result;
	}
	
	private IArray memberMod(LongArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		long []d2 = array.getDatas();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = d1;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = d1[i];
			if (array.isNull(i)) {
				if (v instanceof Number || v == null) {
					resultDatas[i] = null;
				} else if (v instanceof Sequence) {
					resultDatas[i] = v;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
							array.getDataType() + mm.getMessage("Variant2.illMod"));
				}
			} else if (v instanceof Double || v instanceof Float) {
				resultDatas[i] = ((Number)v).doubleValue() % d2[i];
			} else if (v instanceof BigDecimal) {
				BigInteger bi = ((BigDecimal)v).toBigInteger();
				resultDatas[i] = new BigDecimal(bi.mod(BigInteger.valueOf(d2[i])));
			} else if (v instanceof BigInteger) {
				BigInteger bi = (BigInteger)v;
				resultDatas[i] = new BigDecimal(bi.mod(BigInteger.valueOf(d2[i])));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).longValue() % d2[i];
			} else if (v instanceof Sequence) {
				Sequence seq2 = new Sequence(1);
				seq2.add(d2[i]);
				resultDatas[i] = CursorUtil.xor(((Sequence)v), seq2);
			} else if (v != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illMod"));
			}
		}
		
		return result;
	}
	
	private IArray memberMod(DoubleArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		double []d2 = array.getDatas();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = d1;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = d1[i];
			if (array.isNull(i)) {
				if (v instanceof Number || v == null) {
					resultDatas[i] = null;
				} else if (v instanceof Sequence) {
					resultDatas[i] = v;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
							array.getDataType() + mm.getMessage("Variant2.illMod"));
				}
			} else if (v instanceof BigDecimal) {
				BigInteger bi = ((BigDecimal)v).toBigInteger();
				resultDatas[i] = new BigDecimal(bi.mod(BigInteger.valueOf((long)d2[i])));
			} else if (v instanceof BigInteger) {
				BigInteger bi = (BigInteger)v;
				resultDatas[i] = new BigDecimal(bi.mod(BigInteger.valueOf((long)d2[i])));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).doubleValue() % d2[i];
			} else if (v instanceof Sequence) {
				Sequence seq2 = new Sequence(1);
				seq2.add(d2[i]);
				resultDatas[i] = CursorUtil.xor(((Sequence)v), seq2);
			} else if (v != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(Variant.getDataType(v) + mm.getMessage("Variant2.with") +
						array.getDataType() + mm.getMessage("Variant2.illMod"));
			}
		}
		
		return result;
	}
	
	private IArray memberMod(Object value) {
		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = ArrayUtil.mod(datas[i], value);
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = ArrayUtil.mod(datas[i], value);
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	private IArray memberMod(ObjectArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		Object []d2 = array.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				d1[i] = ArrayUtil.mod(d1[i], d2[i]);
			}
			
			return this;
		} else if (array.isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				d2[i] = ArrayUtil.mod(d1[i], d2[i]);
			}
			
			return array;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = ArrayUtil.mod(d1[i], d2[i]);
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	/**
	 * �����������������Ա���������г�Ա�
	 * @param array �Ҳ�����
	 * @return ����ֵ��������в����
	 */
	public IArray memberIntDivide(IArray array) {
		if (array instanceof IntArray) {
			return memberIntDivide((IntArray)array);
		} else if (array instanceof LongArray) {
			return memberIntDivide((LongArray)array);
		} else if (array instanceof DoubleArray) {
			return memberIntDivide((DoubleArray)array);
		} else if (array instanceof ConstArray) {
			return memberIntDivide(array.get(1));
		} else if (array instanceof ObjectArray) {
			return memberIntDivide((ObjectArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}
	
	private IArray memberIntDivide(IntArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		int []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = d1;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = d1[i];
			if (v == null || (s2 != null && s2[i])) {
				resultDatas[i] = null;
			} else if (v instanceof Long || v instanceof Double || v instanceof Float) {
				resultDatas[i] = ((Number)v).longValue() / d2[i];
			} else if (v instanceof BigDecimal) {
				BigInteger bi1 = ((BigDecimal)v).toBigInteger();
				BigInteger bi2 = BigInteger.valueOf(d2[i]);
				resultDatas[i] = new BigDecimal(bi1.divide(bi2));
			} else if (v instanceof BigInteger) {
				BigInteger bi2 = BigInteger.valueOf(d2[i]);
				resultDatas[i] = new BigDecimal(((BigInteger)v).divide(bi2));
			} else if (v instanceof Number) {
				resultDatas[i] = ObjectCache.getInteger(((Number)v).intValue() / d2[i]);
			} else if (v instanceof Sequence) {
				Sequence seq2 = new Sequence(1);
				seq2.add(d2[i]);
				resultDatas[i] = ((Sequence)v).diff(seq2, false);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
						Variant.getDataType(v) + mm.getMessage("Variant2.illDivide"));
			}
		}
		
		return result;
	}
	
	private IArray memberIntDivide(LongArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		long []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = d1;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = d1[i];
			if (v == null || (s2 != null && s2[i])) {
				resultDatas[i] = null;
			} else if (v instanceof BigDecimal) {
				BigInteger bi1 = ((BigDecimal)v).toBigInteger();
				BigInteger bi2 = BigInteger.valueOf(d2[i]);
				resultDatas[i] = new BigDecimal(bi1.divide(bi2));
			} else if (v instanceof BigInteger) {
				BigInteger bi2 = BigInteger.valueOf(d2[i]);
				resultDatas[i] = new BigDecimal(((BigInteger)v).divide(bi2));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).longValue() / d2[i];
			} else if (v instanceof Sequence) {
				Sequence seq2 = new Sequence(1);
				seq2.add(d2[i]);
				resultDatas[i] = ((Sequence)v).diff(seq2, false);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
						Variant.getDataType(v) + mm.getMessage("Variant2.illDivide"));
			}
		}
		
		return result;
	}
	
	private IArray memberIntDivide(DoubleArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		double []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		ObjectArray result;
		Object []resultDatas;
		
		if (isTemporary()) {
			result = this;
			resultDatas = d1;
		} else {
			resultDatas = new Object[size + 1];
			result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		for (int i = 1; i <= size; ++i) {
			Object v = d1[i];
			if (v == null || (s2 != null && s2[i])) {
				resultDatas[i] = null;
			} else if (v instanceof BigDecimal) {
				BigInteger bi1 = ((BigDecimal)v).toBigInteger();
				BigInteger bi2 = BigInteger.valueOf((long)d2[i]);
				resultDatas[i] = new BigDecimal(bi1.divide(bi2));
			} else if (v instanceof BigInteger) {
				BigInteger bi2 = BigInteger.valueOf((long)d2[i]);
				resultDatas[i] = new BigDecimal(((BigInteger)v).divide(bi2));
			} else if (v instanceof Number) {
				resultDatas[i] = ((Number)v).longValue() / d2[i];
			} else if (v instanceof Sequence) {
				Sequence seq2 = new Sequence(1);
				seq2.add(d2[i]);
				resultDatas[i] = ((Sequence)v).diff(seq2, false);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
						Variant.getDataType(v) + mm.getMessage("Variant2.illDivide"));
			}
		}
		
		return result;
	}
	
	private IArray memberIntDivide(Object value) {
		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = ArrayUtil.intDivide(datas[i], value);
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = ArrayUtil.intDivide(datas[i], value);
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	private IArray memberIntDivide(ObjectArray array) {
		int size = this.size;
		Object []d1 = this.datas;
		Object []d2 = array.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				d1[i] = ArrayUtil.intDivide(d1[i], d2[i]);
			}
			
			return this;
		} else if (array.isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				d2[i] = ArrayUtil.intDivide(d1[i], d2[i]);
			}
			
			return array;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = ArrayUtil.intDivide(d1[i], d2[i]);
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @return ��ϵ����������
	 */
	public BoolArray calcRelation(IArray array, int relation) {
		if (array instanceof IntArray) {
			return ((IntArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof LongArray) {
			return ((LongArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof DoubleArray) {
			return ((DoubleArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof StringArray) {
			return ((StringArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof DateArray) {
			return ((DateArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof BoolArray) {
			return ((BoolArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof ConstArray) {
			return calcRelation(array.get(1), relation);
		} else if (array instanceof ObjectArray) {
			return calcRelation((ObjectArray)array, relation);
		} else {
			return array.calcRelation(this, Relation.getInverseRelation(relation));
		}
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @return ��ϵ����������
	 */
	public BoolArray calcRelation(Object value, int relation) {
		if (value == null) {
			return ArrayUtil.calcRelationNull(datas, size, relation);
		}
		
		int size = this.size;
		Object []d1 = this.datas;
		boolean []resultDatas = new boolean[size + 1];
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], value) == 0;
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], value) > 0;
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], value) >= 0;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], value) < 0;
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], value) <= 0;
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], value) != 0;
			}
		} else if (relation == Relation.AND) {
			boolean b = Variant.isTrue(value);
			if (b) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = Variant.isTrue(d1[i]);
				}
			}
		} else { // Relation.OR
			boolean b = Variant.isTrue(value);
			if (b) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = Variant.isTrue(d1[i]);
				}
			}
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}

	private BoolArray calcRelation(ObjectArray array, int relation) {
		int size = this.size;
		Object []d1 = this.datas;
		Object []d2 = array.datas;
		boolean []resultDatas = new boolean[size + 1];
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], d2[i]) == 0;
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], d2[i]) > 0;
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], d2[i]) >= 0;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], d2[i]) < 0;
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], d2[i]) <= 0;
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.compare(d1[i], d2[i]) != 0;
			}
		} else if (relation == Relation.AND) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.isTrue(d1[i]) && Variant.isTrue(d2[i]);
			}
		} else { // Relation.OR
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.isTrue(d1[i]) || Variant.isTrue(d2[i]);
			}
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}
	
	/**
	 * �Ƚ���������Ĵ�С
	 * @param array �Ҳ�����
	 * @return 1����ǰ�����0������������ȣ�-1����ǰ����С
	 */
	public int compareTo(IArray array) {
		if (array instanceof ObjectArray) {
			int size1 = this.size;
			int size2 = array.size();
			Object []d1 = this.datas;
			
			int size = size1;
			int result = 0;
			if (size1 < size2) {
				result = -1;
			} else if (size1 > size2) {
				result = 1;
				size = size2;
			}
			
			ObjectArray array2 = (ObjectArray)array;
			Object []d2 = array2.getDatas();
			
			for (int i = 1; i <= size; ++i) {
				int cmp = Variant.compare(d1[i], d2[i], true);
				if (cmp != 0) {
					return cmp;
				}
			}
			
			return result;
		} else if (array instanceof ConstArray) {
			int size1 = this.size;
			int size2 = array.size();
			Object []d1 = this.datas;
			Object d2 = array.get(1);
			
			int size = size1;
			int result = 0;
			if (size1 < size2) {
				result = -1;
			} else if (size1 > size2) {
				result = 1;
				size = size2;
			}
			
			for (int i = 1; i <= size; ++i) {
				int cmp = Variant.compare(d1[i], d2, true);
				if (cmp != 0) {
					return cmp;
				}
			}
			
			return result;
		} else {
			return -array.compareTo(this);
		}
	}
	
	/**
	 * ���������2����Ա�ıȽ�ֵ
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	public int memberCompare(int index1, int index2) {
		return Variant.compare(datas[index1], datas[index2]);
	}
	
	/**
	 * �ж������������Ա�Ƿ����
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	public boolean isMemberEquals(int index1, int index2) {
		return Variant.isEquals(datas[index1], datas[index2]);
	}
	
	/**
	 * �ж����������ָ��Ԫ���Ƿ���ͬ
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ�Ƚϵ�����
	 * @param index Ҫ�Ƚϵ������Ԫ�ص�����
	 * @return true����ͬ��false������ͬ
	 */
	public boolean isEquals(int curIndex, IArray array, int index) {
		return Variant.isEquals(datas[curIndex], array.get(index));
	}
	
	
	/**
	 * �ж������ָ��Ԫ���Ƿ������ֵ���
	 * @param curIndex ����Ԫ����������1��ʼ����
	 * @param value ֵ
	 * @return true����ȣ�false�������
	 */
	public boolean isEquals(int curIndex, Object value) {
		return Variant.isEquals(datas[curIndex], value);
	}
	
	/**
	 * �ж����������ָ��Ԫ�صĴ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ�Ƚϵ�����
	 * @param index Ҫ�Ƚϵ������Ԫ�ص�����
	 * @return С�ڣ�С��0�����ڣ�0�����ڣ�����0
	 */
	public int compareTo(int curIndex, IArray array, int index) {
		return Variant.compare(datas[curIndex], array.get(index), true);
	}
	
	/**
	 * �Ƚ������ָ��Ԫ�������ֵ�Ĵ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param value Ҫ�Ƚϵ�ֵ
	 * @return
	 */
	public int compareTo(int curIndex, Object value) {
		return Variant.compare(datas[curIndex], value, true);
	}
	
	/**
	 * ��array��ָ��Ԫ�ؼӵ���ǰ�����ָ��Ԫ����
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ��ӵ�����
	 * @param index Ҫ��ӵ������Ԫ�ص�����
	 * @return IArray
	 */
	public IArray memberAdd(int curIndex, IArray array, int index) {
		if (!array.isNull(index)) {
			Object obj = array.get(index);
			datas[curIndex] = Variant.add(datas[curIndex], obj);
		}
		
		return this;
	}
	
	/**
	 * ȡָ����Ա�Ĺ�ϣֵ
	 * @param index ��Ա��������1��ʼ����
	 * @return ָ����Ա�Ĺ�ϣֵ
	 */
	public int hashCode(int index) {
		if (datas[index] != null) {
			return datas[index].hashCode();
		} else {
			return 0;
		}
	}
	
	/**
	 * ���Ա��
	 * @return
	 */
	public Object sum() {
		int size = this.size;
		if (size < 1) {
			return null;
		}
		
		Object []datas = this.datas;
		Object result = datas[1];
		
		for (int i = 2; i <= size; ++i) {
			result = Variant.add(result, datas[i]);
		}
		
		return result;
	}
	
	/**
	 * ��ƽ��ֵ
	 * @return
	 */
	public Object average() {
		Object []datas = this.datas;
		int size = this.size;
		Number sum = null;
		int count = 0;
		int i = 1;

		for (; i <= size; ++i) {
			if (datas[i] instanceof Number) {
				count++;
				sum = (Number)datas[i];
				break;
			}
		}

		for (++i; i <= size; ++i) {
			if (datas[i] instanceof Number) {
				sum = Variant.addNum(sum, (Number)datas[i]);
				count++;
			}
		}

		return Variant.avg(sum, count);
	}
	
	/**
	 * �õ����ĳ�Ա
	 * @return
	 */
	public Object max() {
		int size = this.size;
		if (size == 0) {
			return null;
		}

		Object []datas = this.datas;
		Object max = null;
		
		int i = 1;
		for (; i <= size; ++i) {
			if (datas[i] != null) {
				max = datas[i];
				break;
			}
		}
		
		for (++i; i <= size; ++i) {
			if (datas[i] != null && Variant.compare(max, datas[i], true) < 0) {
				max = datas[i];
			}
		}
		
		return max;
	}
	
	/**
	 * �õ���С�ĳ�Ա
	 * @return
	 */
	public Object min() {
		int size = this.size;
		if (size == 0) {
			return null;
		}

		Object []datas = this.datas;
		Object min = null;
		
		int i = 1;
		for (; i <= size; ++i) {
			if (datas[i] != null) {
				min = datas[i];
				break;
			}
		}
		
		for (++i; i <= size; ++i) {
			if (datas[i] != null && Variant.compare(min, datas[i], true) > 0) {
				min = datas[i];
			}
		}
		
		return min;
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ���㣬ֻ����resultΪ�����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	public void calcRelations(IArray array, int relation, BoolArray result, boolean isAnd) {
		if (array instanceof IntArray) {
			((IntArray)array).calcRelations(this, Relation.getInverseRelation(relation), result, isAnd);
		} else if (array instanceof LongArray) {
			((LongArray)array).calcRelations(this, Relation.getInverseRelation(relation), result, isAnd);
		} else if (array instanceof DoubleArray) {
			((DoubleArray)array).calcRelations(this, Relation.getInverseRelation(relation), result, isAnd);
		} else if (array instanceof StringArray) {
			((StringArray)array).calcRelations(this, Relation.getInverseRelation(relation), result, isAnd);
		} else if (array instanceof DateArray) {
			((DateArray)array).calcRelations(this, Relation.getInverseRelation(relation), result, isAnd);
		} else if (array instanceof BoolArray) {
			((BoolArray)array).calcRelations(this, Relation.getInverseRelation(relation), result, isAnd);
		} else if (array instanceof ConstArray) {
			calcRelations(array.get(1), relation, result, isAnd);
		} else if (array instanceof ObjectArray) {
			calcRelations((ObjectArray)array, relation, result, isAnd);
		} else {
			array.calcRelations(this, Relation.getInverseRelation(relation), result, isAnd);
		}
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ���㣬ֻ����resultΪ�����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	public void calcRelations(Object value, int relation, BoolArray result, boolean isAnd) {
		if (value == null) {
			ArrayUtil.calcRelationsNull(datas, size, relation, result, isAnd);
		}

		int size = this.size;
		Object []d1 = this.datas;
		boolean []resultDatas = result.getDatas();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], value) != 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], value) <= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], value) < 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], value) >= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], value) > 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], value) == 0) {
						resultDatas[i] = false;
					}
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// �������ִ��||����
			// �������ִ��||����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], value) == 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], value) > 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], value) >= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], value) < 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], value) <= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], value) != 0) {
						resultDatas[i] = true;
					}
				}
			} else {
				throw new RuntimeException();
			}
		}
	}

	protected void calcRelations(ObjectArray array, int relation, BoolArray result, boolean isAnd) {
		int size = this.size;
		Object []d1 = this.datas;
		Object []d2 = array.getDatas();
		boolean []resultDatas = result.getDatas();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], d2[i]) != 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], d2[i]) <= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], d2[i]) < 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], d2[i]) >= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], d2[i]) > 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && Variant.compare(d1[i], d2[i]) == 0) {
						resultDatas[i] = false;
					}
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// �������ִ��||����
			// �������ִ��||����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) == 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) > 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) >= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) < 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) <= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) != 0) {
						resultDatas[i] = true;
					}
				}
			} else {
				throw new RuntimeException();
			}
		}
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ��
	 * @param array �Ҳ�����
	 * @return ��λ��������
	 */
	public IArray bitwiseAnd(IArray array) {
		int size = this.size;
		Object []datas = this.datas;
		
		if (array instanceof ConstArray) {
			Object value = array.get(1);
			if (value == null) {
				return new ConstArray(null, size);
			} else if (!(value instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("and" + mm.getMessage("function.paramTypeError"));
			}
			
			ObjectArray result = new ObjectArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				result.push(And.and(datas[i], value));
			}
			
			return result;
		} else {
			Object []d2 = ((ObjectArray)array).datas;
			ObjectArray result = new ObjectArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				result.push(And.and(datas[i], d2[i]));
			}
			
			return result;
		}
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ��
	 * @param array �Ҳ�����
	 * @return ��λ��������
	 */
	public IArray bitwiseOr(IArray array) {
		int size = this.size;
		Object []datas = this.datas;
		
		if (array instanceof ConstArray) {
			Object value = array.get(1);
			if (value == null) {
				return new ConstArray(null, size);
			}
			
			ObjectArray result = new ObjectArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				result.push(Or.or(datas[i], value));
			}
			
			return result;
		} else {
			ObjectArray result = new ObjectArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				result.push(Or.or(datas[i], array.get(i)));
			}
			
			return result;
		}
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ���
	 * @param array �Ҳ�����
	 * @return ��λ���������
	 */
	public IArray bitwiseXOr(IArray array) {
		int size = this.size;
		Object []datas = this.datas;
		
		if (array instanceof ConstArray) {
			Object value = array.get(1);
			if (value == null) {
				return new ConstArray(null, size);
			}
			
			ObjectArray result = new ObjectArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				result.push(Xor.xor(datas[i], value));
			}
			
			return result;
		} else {
			ObjectArray result = new ObjectArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				result.push(Xor.xor(datas[i], array.get(i)));
			}
			
			return result;
		}
	}
	
	/**
	 * ���������Ա�İ�λȡ��
	 * @return ��Ա��λȡ���������
	 */
	public IArray bitwiseNot() {
		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = Not.not(datas[i]);
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Not.not(datas[i]);
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	/**
	 * ȡ����ʶ����ȡֵΪ����ж�Ӧ�����ݣ����������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	public IArray select(IArray signArray) {
		int size = signArray.size();
		Object []d1 = this.datas;
		Object []resultDatas = new Object[size + 1];
		int count = 0;
		
		if (signArray instanceof BoolArray) {
			BoolArray array = (BoolArray)signArray;
			boolean []d2 = array.getDatas();
			boolean []s2 = array.getSigns();
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					if (d2[i]) {
						resultDatas[++count] = d1[i];
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i] && d2[i]) {
						resultDatas[++count] = d1[i];
					}
				}
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				if (signArray.isTrue(i)) {
					resultDatas[++count] = d1[i];
				}
			}
		}
		
		return new ObjectArray(resultDatas, count);
	}

	/**
	 * ȡĳһ���α�ʶ����ȡֵΪ��������������
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã���������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	public IArray select(int start, int end, IArray signArray) {
		Object []d1 = this.datas;
		Object []resultDatas = new Object[end - start + 1];
		int count = 0;
		
		if (signArray instanceof BoolArray) {
			BoolArray array = (BoolArray)signArray;
			boolean []d2 = array.getDatas();
			boolean []s2 = array.getSigns();
			
			if (s2 == null) {
				for (int i = start; i < end; ++i) {
					if (d2[i]) {
						resultDatas[++count] = d1[i];
					}
				}
			} else {
				for (int i = start; i < end; ++i) {
					if (!s2[i] && d2[i]) {
						resultDatas[++count] = d1[i];
					}
				}
			}
		} else {
			for (int i = start; i < end; ++i) {
				if (signArray.isTrue(i)) {
					resultDatas[++count] = d1[i];
				}
			}
		}
		
		return new ObjectArray(resultDatas, count);
	}

	/**
	 * �ѳ�Աת�ɶ������鷵��
	 * @return ��������
	 */
	public Object[] toArray() {
		Object []result = new Object[size];
		System.arraycopy(datas, 1, result, 0, size);
		return result;
	}
	
	/**
	 * �ѳ�Ա�ָ��������
	 * @param result ���ڴ�ų�Ա������
	 */
	public void toArray(Object []result) {
		System.arraycopy(datas, 1, result, 0, size);
	}
	
	/**
	 * �������ָ��λ�ò����������
	 * @param pos λ�ã�����
	 * @return ���غ�벿��Ԫ�ع��ɵ�����
	 */
	public IArray split(int pos) {
		Object []datas = this.datas;
		int size = this.size;
		int resultSize = size - pos + 1;
		Object []resultDatas = new Object[resultSize + 1];
		System.arraycopy(datas, pos, resultDatas, 1, resultSize);
		
		for (int i = pos; i <= size; ++i) {
			datas[i] = null;
		}
		
		this.size = pos - 1;
		return new ObjectArray(resultDatas, resultSize);
	}
	
	/**
	 * ��ָ������Ԫ�ط���������������
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 * @return
	 */
	public IArray split(int from, int to) {
		Object []datas = this.datas;
		int oldSize = this.size;
		int resultSize = to - from + 1;
		Object []resultDatas = new Object[resultSize + 1];
		System.arraycopy(datas, from, resultDatas, 1, resultSize);
		
		System.arraycopy(datas, to + 1, datas, from, oldSize - to);
		this.size -= resultSize;
		
		for (int i = this.size + 1; i <= oldSize; ++i) {
			datas[i] = null;
		}
		
		return new ObjectArray(resultDatas, resultSize);
	}
	
	/**
	 * �������Ԫ�ؽ�������
	 */
	public void sort() {
		MultithreadUtil.sort(datas, 1, size + 1);
	}
	
	/**
	 * �������Ԫ�ؽ�������
	 * @param comparator �Ƚ���
	 */
	public void sort(Comparator<Object> comparator) {
		MultithreadUtil.sort(datas, 1, size + 1, comparator);
	}
	
	/**
	 * �����������Ƿ��м�¼
	 * @return boolean
	 */
	public boolean hasRecord() {
		Object []datas = this.datas;
		for (int i = 1, size = this.size; i <= size; ++i) {
			if (datas[i] instanceof BaseRecord) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * �����Ƿ��ǣ���������
	 * @param isPure true������Ƿ��Ǵ�����
	 * @return boolean true���ǣ�false������
	 */
	public boolean isPmt(boolean isPure) {
		Object []datas = this.datas;
		int size = this.size;
		if (size == 0) {
			return false;
		}
		
		if (isPure) {
			if (!(datas[1] instanceof BaseRecord)) {
				return false;
			}
			
			DataStruct ds = ((BaseRecord)datas[1]).dataStruct();
			for (int i = 2; i <= size; ++i) {
				if (!(datas[i] instanceof BaseRecord) || !((BaseRecord)datas[i]).dataStruct().isCompatible(ds)) {
					return false;
				}
			}
			
			return true;
		} else {
			boolean hasRecord = false;
			for (int i = 1; i <= size; ++i) {
				if (datas[i] instanceof BaseRecord) {
					hasRecord = true;
				} else if (datas[i] != null) {
					return false;
				}
			}
			
			return hasRecord;
		}
	}
	
	/**
	 * ��������ķ�ת����
	 * @return IArray
	 */
	public IArray rvs() {
		int size = this.size;
		Object []datas = this.datas;
		Object []resultDatas = new Object[size + 1];
		
		for (int i = 1, q = size; i <= size; ++i) {
			resultDatas[i] = datas[q--];
		}
		
		return new ObjectArray(resultDatas, size);
	}
	
	/**
	 * ������Ԫ�ش�С����������ȡǰcount����λ��
	 * @param count ���countС��0��ȡ��|count|����λ��
	 * @param isAll countΪ����1ʱ�����isAllȡֵΪtrue��ȡ����������һ��Ԫ�ص�λ�ã�����ֻȡһ��
	 * @param isLast �Ƿ�Ӻ�ʼ��
	 * @param ignoreNull �Ƿ���Կ�Ԫ��
	 * @return IntArray
	 */
	public IntArray ptop(int count, boolean isAll, boolean isLast, boolean ignoreNull) {
		int size = this.size;
		if (size == 0) {
			return new IntArray(0);
		}
		
		
		Object []datas = this.datas;
		if (ignoreNull) {
			if (count == 1) {
				// ȡ��Сֵ��λ��
				Object minValue = null;
				if (isAll) {
					IntArray result = new IntArray(8);
					int i = 1;
					for (; i <= size; ++i) {
						if (datas[i] != null) {
							minValue = datas[i];
							result.addInt(i);
							break;
						}
					}
					
					for (++i; i <= size; ++i) {
						if (datas[i] != null) {
							int cmp = Variant.compare(datas[i], minValue, true);
							if (cmp < 0) {
								minValue = datas[i];
								result.clear();
								result.addInt(i);
							} else if (cmp == 0) {
								result.addInt(i);
							}
						}
					}
					
					return result;
				} else if (isLast) {
					int i = size;
					int pos = 0;
					for (; i > 0; --i) {
						if (datas[i] != null) {
							minValue = datas[i];
							pos = i;
							break;
						}
					}
					
					for (--i; i > 0; --i) {
						if (datas[i] != null && Variant.compare(datas[i], minValue, true) < 0) {
							minValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					if (pos != 0) {
						result.pushInt(pos);
					}
					
					return result;
				} else {
					int i = 1;
					int pos = 0;
					for (; i <= size; ++i) {
						if (datas[i] != null) {
							minValue = datas[i];
							pos = i;
							break;
						}
					}
					
					for (++i; i <= size; ++i) {
						if (datas[i] != null && Variant.compare(datas[i], minValue, true) < 0) {
							minValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					if (pos != 0) {
						result.pushInt(pos);
					}
					
					return result;
				}
			} else if (count > 1) {
				// ȡ��С��count��Ԫ�ص�λ��
				int next = count + 1;
				ObjectArray valueArray = new ObjectArray(next);
				IntArray posArray = new IntArray(next);
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						int index = valueArray.binarySearch(datas[i]);
						if (index < 1) {
							index = -index;
						}
						
						if (index <= count) {
							valueArray.insert(index, datas[i]);
							posArray.insertInt(index, i);
							if (valueArray.size() == next) {
								valueArray.removeLast();
								posArray.removeLast();
							}
						}
					}
				}
				
				return posArray;
			} else if (count == -1) {
				// ȡ���ֵ��λ��
				Object maxValue = null;
				if (isAll) {
					IntArray result = new IntArray(8);
					int i = 1;
					for (; i <= size; ++i) {
						if (datas[i] != null) {
							maxValue = datas[i];
							result.addInt(i);
							break;
						}
					}
					
					for (++i; i <= size; ++i) {
						if (datas[i] != null) {
							int cmp = Variant.compare(datas[i], maxValue, true);
							if (cmp > 0) {
								maxValue = datas[i];
								result.clear();
								result.addInt(i);
							} else if (cmp == 0) {
								result.addInt(i);
							}
						}
					}
					
					return result;
				} else if (isLast) {
					int i = size;
					int pos = 0;
					for (; i > 0; --i) {
						if (datas[i] != null) {
							maxValue = datas[i];
							pos = i;
							break;
						}
					}
					
					for (--i; i > 0; --i) {
						if (datas[i] != null && Variant.compare(datas[i], maxValue, true) > 0) {
							maxValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					if (pos != 0) {
						result.pushInt(pos);
					}
					
					return result;
				} else {
					int i = 1;
					int pos = 0;
					for (; i <= size; ++i) {
						if (datas[i] != null) {
							maxValue = datas[i];
							pos = i;
							break;
						}
					}
					
					for (++i; i <= size; ++i) {
						if (datas[i] != null && Variant.compare(datas[i], maxValue, true) > 0) {
							maxValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					if (pos != 0) {
						result.pushInt(pos);
					}
					
					return result;
				}
			} else if (count < -1) {
				// ȡ����count��Ԫ�ص�λ��
				count = -count;
				int next = count + 1;
				ObjectArray valueArray = new ObjectArray(next);
				IntArray posArray = new IntArray(next);
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						int index = valueArray.descBinarySearch(datas[i]);
						if (index < 1) {
							index = -index;
						}
						
						if (index <= count) {
							valueArray.insert(index, datas[i]);
							posArray.insertInt(index, i);
							if (valueArray.size() == next) {
								valueArray.remove(next);
								posArray.remove(next);
							}
						}
					}
				}
				
				return posArray;
			} else {
				return new IntArray(1);
			}
		} else {
			if (count == 1) {
				// ȡ��Сֵ��λ��
				if (isAll) {
					IntArray result = new IntArray(8);
					result.addInt(1);
					Object minValue = datas[1];

					for (int i = 2; i <= size; ++i) {
						int cmp = Variant.compare(datas[i], minValue, true);
						if (cmp < 0) {
							minValue = datas[i];
							result.clear();
							result.addInt(i);
						} else if (cmp == 0) {
							result.addInt(i);
						}
					}
					
					return result;
				} else if (isLast) {
					Object minValue = datas[size];
					int pos = size;
					
					for (int i = size - 1; i > 0; --i) {
						if (Variant.compare(datas[i], minValue, true) < 0) {
							minValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					result.pushInt(pos);
					return result;
				} else {
					Object minValue = datas[1];
					int pos = 1;
					
					for (int i = 2; i <= size; ++i) {
						if (Variant.compare(datas[i], minValue, true) < 0) {
							minValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					result.pushInt(pos);
					return result;
				}
			} else if (count > 1) {
				// ȡ��С��count��Ԫ�ص�λ��
				int next = count + 1;
				ObjectArray valueArray = new ObjectArray(next);
				IntArray posArray = new IntArray(next);
				
				for (int i = 1; i <= size; ++i) {
					int index = valueArray.binarySearch(datas[i]);
					if (index < 1) {
						index = -index;
					}
					
					if (index <= count) {
						valueArray.insert(index, datas[i]);
						posArray.insertInt(index, i);
						if (valueArray.size() == next) {
							valueArray.removeLast();
							posArray.removeLast();
						}
					}
				}
				
				return posArray;
			} else if (count == -1) {
				// ȡ���ֵ��λ��
				if (isAll) {
					IntArray result = new IntArray(8);
					Object maxValue = datas[1];
					result.addInt(1);
					
					for (int i = 2; i <= size; ++i) {
						int cmp = Variant.compare(datas[i], maxValue, true);
						if (cmp > 0) {
							maxValue = datas[i];
							result.clear();
							result.addInt(i);
						} else if (cmp == 0) {
							result.addInt(i);
						}
					}
					
					return result;
				} else if (isLast) {
					Object maxValue = datas[size];
					int pos = size;
					
					for (int i = size - 1; i > 0; --i) {
						if (Variant.compare(datas[i], maxValue, true) > 0) {
							maxValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					result.pushInt(pos);
					return result;
				} else {
					Object maxValue = datas[1];
					int pos = 1;
					
					for (int i = 2; i <= size; ++i) {
						if (Variant.compare(datas[i], maxValue, true) > 0) {
							maxValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					result.pushInt(pos);
					return result;
				}
			} else if (count < -1) {
				// ȡ����count��Ԫ�ص�λ��
				count = -count;
				int next = count + 1;
				ObjectArray valueArray = new ObjectArray(next);
				IntArray posArray = new IntArray(next);
				
				for (int i = 1; i <= size; ++i) {
					int index = valueArray.descBinarySearch(datas[i]);
					if (index < 1) {
						index = -index;
					}
					
					if (index <= count) {
						valueArray.insert(index, datas[i]);
						posArray.insertInt(index, i);
						if (valueArray.size() == next) {
							valueArray.remove(next);
							posArray.remove(next);
						}
					}
				}
				
				return posArray;
			} else {
				return new IntArray(1);
			}
		}
	}
	
	/**
	 * ������Ԫ�ش�С������������ȡǰcount����λ��
	 * @param count ���countС��0��Ӵ�С������
	 * @param ignoreNull �Ƿ���Կ�Ԫ��
	 * @param iopt �Ƿ�ȥ�ط�ʽ������
	 * @return IntArray
	 */
	public IntArray ptopRank(int count, boolean ignoreNull, boolean iopt) {
		int size = this.size;
		if (size == 0 || count == 0) {
			return new IntArray(0);
		}
		
		Object []datas = this.datas;		
		if (count > 0) {
			// ȡ��С��count��Ԫ�ص�λ��
			int next = count + 1;
			ObjectArray valueArray = new ObjectArray(next);
			IntArray posArray = new IntArray(next);

			if (iopt) {
				int curCount = 0;
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						if (curCount < count) {
							int index = valueArray.binarySearch(datas[i]);
							if (index < 1) {
								curCount++;
								index = -index;
							}
							
							valueArray.insert(index, datas[i]);
							posArray.insertInt(index, i);
						} else {
							int curSize = valueArray.size();
							int cmp = compareTo(i, valueArray, curSize);
							if (cmp < 0) {
								int index = valueArray.binarySearch(datas[i]);
								if (index < 1) {
									index = -index;
									
									// ɾ�������ͬ�ĳ�Ա
									Object value = valueArray.get(curSize);
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize - 1; j >= count; --j) {
										if (Variant.isEquals(valueArray.get(j), value)) {
											valueArray.removeLast();
											posArray.removeLast();
										} else {
											break;
										}
									}
								}
								
								valueArray.insert(index, datas[i]);
								posArray.insertInt(index, i);
							} else if (cmp == 0) {
								valueArray.add(datas[i]);
								posArray.addInt(i);
							}
						}
					} else if (!ignoreNull) {
						if (curCount < count) {
							if (curCount == 0 || !valueArray.isNull(1)) {
								curCount++;
							}
							
							valueArray.insert(1, null);
							posArray.insertInt(1, i);
						} else {
							if (valueArray.isNull(1)) {
								valueArray.add(datas[i]);
								posArray.addInt(i);
							} else {
								// ɾ�������ͬ�ĳ�Ա
								int curSize = valueArray.size();
								Object value = valueArray.get(curSize);
								valueArray.removeLast();
								posArray.removeLast();
								for (int j = curSize - 1; j >= count; --j) {
									if (Variant.isEquals(valueArray.get(j), value)) {
										valueArray.removeLast();
										posArray.removeLast();
									} else {
										break;
									}
								}
								
								valueArray.insert(1, null);
								posArray.insertInt(1, i);
							}
						}
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						int curSize = valueArray.size();
						if (curSize < count) {
							int index = valueArray.binarySearch(datas[i]);
							if (index < 1) {
								index = -index;
							}
							
							valueArray.insert(index, datas[i]);
							posArray.insertInt(index, i);
						} else {
							int cmp = compareTo(i, valueArray, curSize);
							if (cmp < 0) {
								int index = valueArray.binarySearch(datas[i]);
								if (index < 1) {
									index = -index;
								}
								
								valueArray.insert(index, datas[i]);
								posArray.insertInt(index, i);
								
								if (valueArray.memberCompare(count, curSize + 1) != 0) {
									// ɾ�������ͬ�ĳ�Ա
									Object value = valueArray.get(curSize + 1);
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize; j > count; --j) {
										if (Variant.isEquals(valueArray.get(j), value)) {
											valueArray.removeLast();
											posArray.removeLast();
										} else {
											break;
										}
									}
								}
							} else if (cmp == 0) {
								valueArray.add(datas[i]);
								posArray.addInt(i);
							}
						}
					} else if (!ignoreNull) {
						int curSize = valueArray.size();
						if (curSize < count) {
							valueArray.insert(1, null);
							posArray.insertInt(1, i);
						} else {
							if (valueArray.isNull(curSize)) {
								valueArray.add(datas[i]);
								posArray.addInt(i);
							} else {
								valueArray.insert(1, null);
								posArray.insertInt(1, i);
								
								if (valueArray.memberCompare(count, curSize + 1) != 0) {
									// ɾ�������ͬ�ĳ�Ա
									Object value = valueArray.get(curSize + 1);
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize; j > count; --j) {
										if (Variant.isEquals(valueArray.get(j), value)) {
											valueArray.removeLast();
											posArray.removeLast();
										} else {
											break;
										}
									}
								}
							}
						}
					}
				}
			}
			
			return posArray;
		} else {
			// ȡ����count��Ԫ�ص�λ��
			count = -count;
			int next = count + 1;
			ObjectArray valueArray = new ObjectArray(next);
			IntArray posArray = new IntArray(next);

			if (iopt) {
				int curCount = 0;
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						if (curCount < count) {
							int index = valueArray.descBinarySearch(datas[i]);
							if (index < 1) {
								curCount++;
								index = -index;
							}
							
							valueArray.insert(index, datas[i]);
							posArray.insertInt(index, i);
						} else {
							int curSize = valueArray.size();
							int cmp = compareTo(i, valueArray, curSize);
							if (cmp > 0) {
								int index = valueArray.descBinarySearch(datas[i]);
								if (index < 1) {
									index = -index;
									
									// ɾ�������ͬ�ĳ�Ա
									Object value = valueArray.get(curSize);
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize - 1; j >= count; --j) {
										if (Variant.isEquals(valueArray.get(j), value)) {
											valueArray.removeLast();
											posArray.removeLast();
										} else {
											break;
										}
									}
								}
								
								valueArray.insert(index, datas[i]);
								posArray.insertInt(index, i);
							} else if (cmp == 0) {
								valueArray.add(datas[i]);
								posArray.addInt(i);
							}
						}
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						int curSize = valueArray.size();
						if (curSize < count) {
							int index = valueArray.descBinarySearch(datas[i]);
							if (index < 1) {
								index = -index;
							}
							
							valueArray.insert(index, datas[i]);
							posArray.insertInt(index, i);
						} else {
							int cmp = compareTo(i, valueArray, curSize);
							if (cmp > 0) {
								int index = valueArray.descBinarySearch(datas[i]);
								if (index < 1) {
									index = -index;
								}
								
								valueArray.insert(index, datas[i]);
								posArray.insertInt(index, i);
								
								if (valueArray.memberCompare(count, curSize + 1) != 0) {
									// ɾ�������ͬ�ĳ�Ա
									Object value = valueArray.get(curSize + 1);
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize; j > count; --j) {
										if (Variant.isEquals(valueArray.get(j), value)) {
											valueArray.removeLast();
											posArray.removeLast();
										} else {
											break;
										}
									}
								}
							} else if (cmp == 0) {
								valueArray.add(datas[i]);
								posArray.addInt(i);
							}
						}
					}
				}
			}
			
			return posArray;
		}
	}

	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * �ѵ�ǰ����ת�ɶ������飬�����ǰ�����Ƕ��������򷵻����鱾��
	 * @return ObjectArray
	 */
	public ObjectArray toObjectArray() {
		return this;
	}
	
	/**
	 * �Ѷ�������ת�ɴ��������飬����ת���׳��쳣
	 * @return IArray
	 */
	public IArray toPureArray() {
		int size = this.size;
		Object []datas = this.datas;
		IArray resultArray = null;
		int numType = 0; // 1:int, 2:long
		
		for (int i = 1; i <= size; ++i) {
			Object obj = datas[i];
			if (obj instanceof String) {
				resultArray = new StringArray(size);
				break;
			} else if (obj instanceof Date) {
				resultArray = new DateArray(size);
				break;
			} else if (obj instanceof Double) {
				resultArray = new DoubleArray(size);
				break;
			} else if (obj instanceof Long) {
				numType = 2;
			} else if (obj instanceof Integer) {
				if (numType < 1) {
					numType = 1;
				}
			} else if (obj instanceof Boolean) {
				resultArray = new BoolArray(size);
				break;
			} else if (obj != null) {
				return this;
			}
		}
		
		if (resultArray == null) {
			if (numType == 1) {
				resultArray = new IntArray(size);
			} else if (numType == 2) {
				resultArray = new LongArray(size);
			} else {
				return this;
			}
		}
		
		for (int i = 1; i <= size; ++i) {
			resultArray.push(datas[i]);
		}
		
		return resultArray;
	}
	
	/**
	 * �����������������������л����
	 * @param refOrigin ����Դ�У�����������
	 * @return
	 */
	public IArray reserve(boolean refOrigin) {
		if (isTemporary()) {
			setTemporary(false);
			return this;
		} else if (refOrigin) {
			return this;
		} else {
			return dup();
		}
	}
	
	/**
	 * ������������������ѡ����Ա��������飬�ӵ�ǰ����ѡ����־Ϊtrue�ģ���other����ѡ����־Ϊfalse��
	 * @param signArray ��־����
	 * @param other ��һ������
	 * @return IArray
	 */
	public IArray combine(IArray signArray, IArray other) {
		if (other instanceof ConstArray) {
			return combine(signArray, ((ConstArray)other).getData());
		}
		
		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				if (signArray.isFalse(i)) {
					datas[i] = other.get(i);
				}
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			System.arraycopy(datas, 1, resultDatas, 1, size);
			
			for (int i = 1; i <= size; ++i) {
				if (signArray.isFalse(i)) {
					resultDatas[i] = other.get(i);
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}

	/**
	 * ���������ӵ�ǰ����ѡ����־Ϊtrue�ģ���־Ϊfalse���ó�value
	 * @param signArray ��־����
	 * @param other ֵ
	 * @return IArray
	 */
	public IArray combine(IArray signArray, Object value) {
		int size = this.size;
		Object []datas = this.datas;
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				if (signArray.isFalse(i)) {
					datas[i] = value;
				}
			}
			
			return this;
		} else {
			Object []resultDatas = new Object[size + 1];
			System.arraycopy(datas, 1, resultDatas, 1, size);
			
			for (int i = 1; i <= size; ++i) {
				if (signArray.isFalse(i)) {
					resultDatas[i] = value;
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	/**
	 * ����ָ������ĳ�Ա�ڵ�ǰ�����е�λ��
	 * @param array �����ҵ�����
	 * @param opt ѡ�b��ͬ��鲢�����ң�i�����ص��������У�c����������
	 * @return λ�û���λ������
	 */
	public Object pos(IArray array, String opt) {
		return ArrayUtil.pos(this, array, opt);
	}

	/**
	 * ���������Ա�Ķ����Ʊ�ʾʱ1�ĸ�����
	 * @return
	 */
	public int bit1() {
		Object []datas = this.datas;
		int size = this.size;
		int sum = 0;
		
		for (int i = 1; i <= size; ++i) {
			sum += Bit1.bitCount(datas[i]);
		}
		
		return sum;
	}
	
	/**
	 * ���������Ա��λ���ֵ�Ķ����Ʊ�ʾʱ1�ĸ�����
	 * @param array �������
	 * @return 1�ĸ�����
	 */
	public int bit1(IArray array) {
		Object []datas = this.datas;
		int size = this.size;
		int count = 0;
		
		for (int i = 1; i <= size; ++i) {
			count += Bit1.bitCount(datas[i], array.get(i));
		}
		
		return count;
	}
}
