package com.scudata.array;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.SerialBytes;
import com.scudata.expression.Relation;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.MultithreadUtil;
import com.scudata.util.HashUtil;
import com.scudata.util.Variant;

/**
 * �ź�����
 * @author WangXiaoJun
 *
 */
public class SerialBytesArray implements IArray {
	private static final long NULL = 0;
	
	// ��0��Ԫ�ر�ʾ�Ƿ�����ʱ����
	private long []datas1; 
	private long []datas2;
	private int size;
	
	public SerialBytesArray() {
		this(DEFAULT_LEN);
	}

	public SerialBytesArray(int initialCapacity) {
		++initialCapacity;
		datas1 = new long[initialCapacity];
		datas2 = new long[initialCapacity];
	}
	
	public SerialBytesArray(long []datas1, long []datas2, int size) {
		this.datas1 = datas1;
		this.datas2 = datas2;
		this.size = size;
	}
	
	/**
	 * д���ݵ���
	 * @param out �����
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		int size = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		out.writeInt(size);
		for (int i = 1; i <= size; ++i) {
			out.writeLong(datas1[i]);
		}
		
		for (int i = 1; i <= size; ++i) {
			out.writeLong(datas2[i]);
		}
	}

	/**
	 * �����ж�����
	 * @param in ������
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		size = in.readInt();
		int len = size + 1;
		
		long []datas = this.datas1 = new long[len];
		for (int i = 1; i < len; ++i) {
			datas[i] = in.readLong();
		}
		
		datas = this.datas2 = new long[len];
		for (int i = 1; i < len; ++i) {
			datas[i] = in.readLong();
		}
	}

	public byte[] serialize() throws IOException {
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		int size = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		out.writeInt(size);
		for (int i = 1; i <= size; ++i) {
			out.writeLong(datas1[i]);
		}
		
		for (int i = 1; i <= size; ++i) {
			out.writeLong(datas2[i]);
		}

		return out.toByteArray();
	}

	public void fillRecord(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(bytes);
		size = in.readInt();
		int len = size + 1;
		
		long []datas = this.datas1 = new long[len];
		for (int i = 1; i < len; ++i) {
			datas[i] = in.readLong();
		}
		
		datas = this.datas2 = new long[len];
		for (int i = 1; i < len; ++i) {
			datas[i] = in.readLong();
		}
	}

	public String getDataType() {
		MessageManager mm = EngineMessage.get();
		return mm.getMessage("DataType.SerialBytes");
	}
	
	/**
	 * ׷���ź�
	 * @param value1 ֵ1
	 * @param value2 ֵ2
	 */
	public void add(long value1, long value2) {
		int newSize = size + 1;
		ensureCapacity(newSize);
		datas1[newSize] = value1;
		datas2[newSize] = value1;
		size = newSize;
	}

	/**
	 * ׷��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void add(Object o) {
		if (o instanceof SerialBytes) {
			ensureCapacity(size + 1);
			size++;
			
			SerialBytes sb = (SerialBytes)o;
			datas1[size] = sb.getValue1();
			datas2[size] = sb.getValue2();
		} else if (o == null) {
			ensureCapacity(size + 1);
			size++;
			datas1[size] = NULL;
			datas2[size] = NULL;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.SerialBytes"), Variant.getDataType(o)));
		}
	}

	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(Object[] array) {
		for (Object obj : array) {
			if (obj != null && !(obj instanceof SerialBytes)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.SerialBytes"), Variant.getDataType(obj)));
			}
		}
		
		int size2 = array.length;
		int size = this.size;
		ensureCapacity(size + size2);
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		for (int i = 0; i < size2; ++i) {
			size++;
			if (array[i] != null) {
				SerialBytes sb = (SerialBytes)array[i];
				datas1[size] = sb.getValue1();
				datas2[size] = sb.getValue2();
			} else {
				datas1[size] = NULL;
				datas2[size] = NULL;
			}
		}
		
		this.size = size;
	}

	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(IArray array) {
		int size2 = array.size();
		if (size2 == 0) {
		} else if (array instanceof SerialBytesArray) {
			ensureCapacity(size + size2);
			SerialBytesArray sba = (SerialBytesArray)array;
			System.arraycopy(sba.datas1, 1, datas1, size + 1, size2);
			System.arraycopy(sba.datas2, 1, datas2, size + 1, size2);
			size += size2;
		} else {
			int size = this.size;
			ensureCapacity(size + size2);
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			
			for (int i = 1; i <= size2; ++i) {
				size++;
				Object obj = array.get(i);
				if (obj instanceof SerialBytes) {
					SerialBytes sb = (SerialBytes)array.get(i);
					datas1[size] = sb.getValue1();
					datas2[size] = sb.getValue2();
				} else if (obj == null) {
					datas1[size] = NULL;
					datas2[size] = NULL;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("pdm.arrayTypeError", 
							mm.getMessage("DataType.SerialBytes"), Variant.getDataType(obj)));
				}
			}
			
			this.size = size;
		}
	}

	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 * @param count Ԫ�ظ���
	 */
	public void addAll(IArray array, int count) {
		if (count == 0) {
		} else if (array instanceof SerialBytesArray) {
			ensureCapacity(size + count);
			SerialBytesArray sba = (SerialBytesArray)array;
			System.arraycopy(sba.datas1, 1, datas1, size + 1, count);
			System.arraycopy(sba.datas2, 1, datas2, size + 1, count);
			size += count;
		} else {
			int size = this.size;
			ensureCapacity(size + count);
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			
			for (int i = 1; i <= count; ++i) {
				size++;
				Object obj = array.get(i);
				if (obj instanceof SerialBytes) {
					SerialBytes sb = (SerialBytes)obj;
					datas1[size] = sb.getValue1();
					datas2[size] = sb.getValue2();
				} else if (obj == null) {
					datas1[size] = NULL;
					datas2[size] = NULL;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("pdm.arrayTypeError", 
							mm.getMessage("DataType.SerialBytes"), Variant.getDataType(obj)));
				}
			}
			
			this.size = size;
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 * @param index Ҫ��������ݵ���ʼλ��
	 * @param count ����
	 */
	public void addAll(IArray array, int index, int count) {
		if (array instanceof SerialBytesArray) {
			ensureCapacity(size + count);
			SerialBytesArray sba = (SerialBytesArray)array;
			System.arraycopy(sba.datas1, index, datas1, size + 1, count);
			System.arraycopy(sba.datas2, index, datas2, size + 1, count);
			size += count;
		} else {
			int size = this.size;
			ensureCapacity(size + count);
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			
			for (int i = 1; i <= count; ++i, ++index) {
				size++;
				Object obj = array.get(index);
				if (obj instanceof SerialBytes) {
					SerialBytes sb = (SerialBytes)obj;
					datas1[size] = sb.getValue1();
					datas2[size] = sb.getValue2();
				} else if (obj == null) {
					datas1[size] = NULL;
					datas2[size] = NULL;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("pdm.arrayTypeError", 
							mm.getMessage("DataType.SerialBytes"), Variant.getDataType(obj)));
				}
			}
			
			this.size = size;
		}
	}
	
	/**
	 * ����Ԫ�أ�������Ͳ��������׳��쳣
	 * @param index ����λ�ã���1��ʼ����
	 * @param o Ԫ��ֵ
	 */
	public void insert(int index, Object o) {
		if (o instanceof SerialBytes) {
			ensureCapacity(size + 1);
			size++;
			
			System.arraycopy(datas1, index, datas1, index + 1, size - index);
			System.arraycopy(datas2, index, datas2, index + 1, size - index);
			
			SerialBytes sb = (SerialBytes)o;
			datas1[index] = sb.getValue1();
			datas2[index] = sb.getValue2();
		} else if (o == null) {
			ensureCapacity(size + 1);
			size++;
			
			System.arraycopy(datas1, index, datas1, index + 1, size - index);
			System.arraycopy(datas2, index, datas2, index + 1, size - index);
			datas1[index] = NULL;
			datas2[index] = NULL;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Long"), Variant.getDataType(o)));
		}
	}
	
	public void insert(int index, long value1, long value2) {
		ensureCapacity(size + 1);
		size++;
		
		System.arraycopy(datas1, index, datas1, index + 1, size - index);
		System.arraycopy(datas2, index, datas2, index + 1, size - index);

		datas1[index] = value1;
		datas2[index] = value2;
	}

	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, IArray array) {
		if (array instanceof SerialBytesArray) {
			int numNew = array.size();
			SerialBytesArray sbArray = (SerialBytesArray)array;
			ensureCapacity(size + numNew);
			
			System.arraycopy(datas1, pos, datas1, pos + numNew, size - pos + 1);
			System.arraycopy(datas2, pos, datas2, pos + numNew, size - pos + 1);
			System.arraycopy(sbArray.datas1, 1, datas1, pos, numNew);
			System.arraycopy(sbArray.datas2, 1, datas2, pos, numNew);
			size += numNew;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.SerialBytes"), array.getDataType()));
		}
	}

	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, Object[] array) {
		for (Object obj : array) {
			if (obj != null && !(obj instanceof SerialBytes)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.SerialBytes"), Variant.getDataType(obj)));
			}
		}

		int numNew = array.length;
		ensureCapacity(size + numNew);
		
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		System.arraycopy(datas1, pos, datas1, pos + numNew, size - pos + 1);
		System.arraycopy(datas2, pos, datas2, pos + numNew, size - pos + 1);			
		
		for (int i = 0; i < numNew; ++i, ++pos) {
			if (array[i] != null) {
				SerialBytes sb = (SerialBytes)array[i];
				datas1[pos] = sb.getValue1();
				datas2[pos] = sb.getValue2();
			} else {
				datas1[pos] = NULL;
				datas2[pos] = NULL;
			}
		}
		
		size += numNew;
	}
	
	/**
	 * ׷���źţ��������������Ϊ���㹻�ռ���Ԫ�أ�
	 * @param value1 ֵ1
	 * @param value2 ֵ2
	 */
	public void push(long value1, long value2) {
		size++;
		datas1[size] = value1;
		datas2[size] = value1;
	}

	/**
	 * ׷��Ԫ�أ��������������Ϊ���㹻�ռ���Ԫ�أ���������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void push(Object o) {
		if (o instanceof SerialBytes) {
			size++;
			SerialBytes sb = (SerialBytes)o;
			datas1[size] = sb.getValue1();
			datas2[size] = sb.getValue2();
		} else if (o == null) {
			size++;
			datas1[size] = NULL;
			datas2[size] = NULL;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.SerialBytes"), Variant.getDataType(o)));
		}
	}

	/**
	 * ׷�ӿ�Ԫ��
	 */
	public void pushNull() {
		size++;
		datas1[size] = NULL;
		datas2[size] = NULL;
	}

	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void push(IArray array, int index) {
		if (array instanceof SerialBytesArray) {
			SerialBytesArray sba = (SerialBytesArray)array;
			size++;
			datas1[size] = sba.datas1[index];
			datas2[size] = sba.datas2[index];
		} else {
			push(array.get(index));
		}
	}

	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void add(IArray array, int index) {
		if (array instanceof SerialBytesArray) {
			SerialBytesArray sba = (SerialBytesArray)array;
			ensureCapacity(size + 1);
			size++;
			datas1[size] = sba.datas1[index];
			datas2[size] = sba.datas2[index];
		} else {
			add(array.get(index));
		}
	}

	/**
	 * ��array�еĵ�index��Ԫ���������ǰ�����ָ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param curIndex ��ǰ�����Ԫ����������1��ʼ����
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void set(int curIndex, IArray array, int index) {
		if (array instanceof SerialBytesArray) {
			SerialBytesArray sba = (SerialBytesArray)array;
			datas1[curIndex] = sba.datas1[index];
			datas2[curIndex] = sba.datas2[index];
		} else {
			set(curIndex, array.get(index));
		}
	}

	/**
	 * ȡָ��λ��Ԫ��
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public Object get(int index) {
		if (datas1[index] != NULL || datas2[index] != NULL) {
			return new SerialBytes(datas1[index], datas2[index]);
		} else {
			return null;
		}
	}

	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param indexArray λ������
	 * @return IArray
	 */
	public IArray get(int[] indexArray) {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int len = indexArray.length;
		long []resultDatas1 = new long[len + 1];
		long []resultDatas2 = new long[len + 1];
		
		int seq = 1;
		for (int i : indexArray) {
			resultDatas1[seq] = datas1[i];
			resultDatas2[seq] = datas2[i];
			seq++;
		}
		
		return new SerialBytesArray(resultDatas1, resultDatas2, len);
	}

	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param indexArray λ������
	 * @param start ��ʼλ�ã�����
	 * @param end ����λ�ã�����
	 * @param doCheck true��λ�ÿ��ܰ���0��0��λ����null��䣬false���������0
	 * @return IArray
	 */
	public IArray get(int[] indexArray, int start, int end, boolean doCheck) {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int len = end - start + 1;
		long []resultDatas1 = new long[len + 1];
		long []resultDatas2 = new long[len + 1];
		
		if (doCheck) {
			for (int seq = 1; start <= end; ++start, ++seq) {
				int index = indexArray[start];
				if (index > 0) {
					resultDatas1[seq] = datas1[index];
					resultDatas2[seq] = datas2[index];
				} else {
					resultDatas1[seq] = NULL;
					resultDatas2[seq] = NULL;
				}
			}
		} else {
			for (int seq = 1; start <= end; ++start, ++seq) {
				resultDatas1[seq] = datas1[indexArray[start]];
				resultDatas2[seq] = datas2[indexArray[start]];
			}
		}
		
		return new SerialBytesArray(resultDatas1, resultDatas2, len);
	}

	/**
	 * ȡĳһ�������������
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã���������
	 * @return IArray
	 */
	public IArray get(int start, int end) {
		int newSize = end - start;
		long []newDatas1 = new long[newSize + 1];
		long []newDatas2 = new long[newSize + 1];
		
		System.arraycopy(datas1, start, newDatas1, 1, newSize);
		System.arraycopy(datas2, start, newDatas2, 1, newSize);
		return new SerialBytesArray(newDatas1, newDatas2, newSize);
	}

	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param IArray λ������
	 * @return IArray
	 */
	public IArray get(IArray indexArray) {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int len = indexArray.size();
		long []resultDatas1 = new long[len + 1];
		long []resultDatas2 = new long[len + 1];
		
		for (int i = 1; i <= len; ++i) {
			if (indexArray.isNull(i)) {
				resultDatas1[i] = NULL;
				resultDatas2[i] = NULL;
			} else {
				resultDatas1[i] = datas1[indexArray.getInt(i)];
				resultDatas2[i] = datas2[indexArray.getInt(i)];
			}
		}

		return new SerialBytesArray(resultDatas1, resultDatas2, len);
	}

	public int getInt(int index) {
		throw new RuntimeException();
	}

	public long getLong(int index) {
		throw new RuntimeException();
	}

	/**
	 * ʹ�б��������С��minCapacity
	 * @param minCapacity ��С����
	 */
	public void ensureCapacity(int minCapacity) {
		if (datas1.length <= minCapacity) {
			int newCapacity = (datas1.length * 3) / 2;
			if (newCapacity <= minCapacity) {
				newCapacity = minCapacity + 1;
			}

			long []newDatas1 = new long[newCapacity];
			long []newDatas2 = new long[newCapacity];
			System.arraycopy(datas1, 0, newDatas1, 0, size + 1);
			System.arraycopy(datas2, 0, newDatas2, 0, size + 1);
			datas1 = newDatas1;
			datas2 = newDatas2;
		}
	}

	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ��ǿ�
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isNull(int index) {
		return datas1[index] == NULL && datas2[index] == NULL;
	}

	/**
	 * �ж�Ԫ���Ƿ���True
	 * @return BoolArray
	 */
	public BoolArray isTrue() {
		int size = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		boolean []resultDatas = new boolean[size + 1];
		
		for (int i = 1; i <= size; ++i) {
			if (datas1[i] != NULL || datas2[i] != NULL) {
				resultDatas[i] = true;
			}
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
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		boolean []resultDatas = new boolean[size + 1];
		
		for (int i = 1; i <= size; ++i) {
			if (datas1[i] == NULL && datas2[i] == NULL) {
				resultDatas[i] = true;
			}
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
		return datas1[index] != NULL || datas2[index] != NULL;
	}

	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ���False
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isFalse(int index) {
		return datas1[index] == NULL && datas2[index] == NULL;
	}

	/**
	 * �Ƿ��Ǽ����������ʱ���������飬��ʱ�����Ŀ��Ա��޸ģ����� f1+f2+f3��ֻ�����һ�������Ž��
	 * @return true������ʱ���������飬false��������ʱ����������
	 */
	public boolean isTemporary() {
		return datas1[0] == 1;
	}

	/**
	 * �����Ƿ��Ǽ����������ʱ����������
	 * @param ifTemporary true������ʱ���������飬false��������ʱ����������
	 */
	public void setTemporary(boolean ifTemporary) {
		datas1[0] = ifTemporary ? 1 : 0;
	}

	/**
	 * ɾ�����һ��Ԫ��
	 */
	public void removeLast() {
		size--;
	}

	/**
	 * ɾ��ָ��λ�õ�Ԫ��
	 * @param index ��������1��ʼ����
	 */
	public void remove(int index) {
		System.arraycopy(datas1, index + 1, datas1, index, size - index);
		System.arraycopy(datas2, index + 1, datas2, index, size - index);
		--size;
	}

	/**
	 * ɾ��ָ��λ�õ�Ԫ�أ���Ŵ�С��������
	 * @param seqs ��������
	 */
	public void remove(int[] seqs) {
		int delCount = 0;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
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
				System.arraycopy(datas1, cur + 1, datas1, cur - delCount, moveCount);
				System.arraycopy(datas2, cur + 1, datas2, cur - delCount, moveCount);
			}
			
			delCount++;
		}
		
		size -= delCount;
	}

	/**
	 * ɾ��ָ�������ڵ�Ԫ��
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 */
	public void removeRange(int fromIndex, int toIndex) {
		System.arraycopy(datas1, toIndex + 1, datas1, fromIndex, size - toIndex);
		System.arraycopy(datas2, toIndex + 1, datas2, fromIndex, size - toIndex);
		size -= (toIndex - fromIndex + 1);
	}

	public int size() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * ��������ķǿ�Ԫ����Ŀ
	 * @return �ǿ�Ԫ����Ŀ
	 */
	public int count() {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int size = this.size;
		int count = size;
		
		for (int i = 1; i <= size; ++i) {
			if (datas1[i] == NULL && datas2[i] == NULL) {
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
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int size = this.size;
		for (int i = 1; i <= size; ++i) {
			if (datas1[i] != NULL || datas2[i] != NULL) {
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
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int size = this.size;
		for (int i = 1; i <= size; ++i) {
			if (datas1[i] != NULL || datas2[i] != NULL) {
				new SerialBytes(datas1[i], datas2[i]);
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
		if (obj instanceof SerialBytes) {
			SerialBytes sb = (SerialBytes)obj;
			datas1[index] = sb.getValue1();
			datas2[index] = sb.getValue2();
		} else if (obj == null) {
			datas1[index] = NULL;
			datas2[index] = NULL;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.SerialBytes"), Variant.getDataType(obj)));
		}
	}

	public void clear() {
		size = 0;
	}

	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @return Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem) {
		return binarySearch(elem, 1, size);
	}
	
	/**
	 * ���ַ�����ָ���ź�
	 * @param value1 �źŵ�ֵ1
	 * @param value2 �źŵ�ֵ2
	 * @param start ��ʼ����λ�ã�������
	 * @param end ��������λ�ã�������
	 * @return
	 */
	public int binarySearch(long value1, long value2, int start, int end) {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int low = start, high = end;
		
		while (low <= high) {
			int mid = (low + high) >> 1;
			int cmp = SerialBytes.compare(datas1[mid], datas2[mid], value1, value2);
			
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
	
	// ���鰴�������򣬽��н�����ֲ���
	private int descBinarySearch(long value1, long value2) {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int low = 1, high = size;
		
		while (low <= high) {
			int mid = (low + high) >> 1;
			int cmp = SerialBytes.compare(datas1[mid], datas2[mid], value1, value2);
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
		if (elem instanceof SerialBytes) {
			SerialBytes sb = (SerialBytes)elem;
			long value1 = sb.getValue1();
			long value2 = sb.getValue2();
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			int low = start, high = end;
			
			while (low <= high) {
				int mid = (low + high) >> 1;
				int cmp = SerialBytes.compare(datas1[mid], datas2[mid], value1, value2);
				
				if (cmp < 0) {
					low = mid + 1;
				} else if (cmp > 0) {
					high = mid - 1;
				} else {
					return mid; // key found
				}
			}

			return -low; // key not found
		} else if (elem == null) {
			if (end > 0 && datas1[start] == NULL && datas1[end] == NULL) {
				return start;
			} else {
				return -1;
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), elem,
					getDataType(), Variant.getDataType(elem)));
		}
	}

	/**
	 * �ж��������Ƿ����ָ���ź�
	 * @param value1
	 * @param value2
	 * @return
	 */
	public boolean contains(long value1, long value2) {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int size = this.size;
		
		for (int i = 1; i <= size; ++i) {
			if (datas1[i] == value1 && datas2[i] == value2) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * �����б����Ƿ����ָ��Ԫ��
	 * @param elem Object �����ҵ�Ԫ��
	 * @return boolean true��������false��������
	 */
	public boolean contains(Object elem) {
		if (elem instanceof SerialBytes) {
			SerialBytes sb = (SerialBytes)elem;
			long value1 = sb.getValue1();
			long value2 = sb.getValue2();
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			int size = this.size;
			
			for (int i = 1; i <= size; ++i) {
				if (datas1[i] == value1 && datas2[i] == value2) {
					return true;
				}
			}
			
			return false;
		} else if (elem == null) {
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			int size = this.size;
			
			for (int i = 1; i <= size; ++i) {
				if (datas1[i] == NULL && datas2[i] == NULL) {
					return true;
				}
			}
			
			return false;
		} else {
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
		if (array instanceof SerialBytesArray) {
			SerialBytesArray sba = (SerialBytesArray)array;
			long []datas1 = sba.datas1;
			long []datas2 = sba.datas2;
			int size = this.size;
			
			if (isSorted) {
				for (int i = 1; i <= resultSize; ++i) {
					if (result.isTrue(i) && binarySearch(datas1[i], datas2[i], 1, size) < 1) {
						result.set(i, false);
					}
				}
			} else {
				for (int i = 1; i <= resultSize; ++i) {
					if (result.isTrue(i) && !contains(datas1[i], datas2[i])) {
						result.set(i, false);
					}
				}
			}
		} else {
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
	}

	/**
	 * �����б����Ƿ����ָ��Ԫ�أ�ʹ�õȺűȽ�
	 * @param elem
	 * @return boolean true��������false��������
	 */
	public boolean objectContains(Object elem) {
		return false;
	}

	/**
	 * ����Ԫ�����������״γ��ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start ��ʼ����λ�ã�������
	 * @return ���Ԫ�ش����򷵻�ֵ����0�����򷵻�0
	 */
	public int firstIndexOf(Object elem, int start) {
		if (elem instanceof SerialBytes) {
			SerialBytes sb = (SerialBytes)elem;
			long value1 = sb.getValue1();
			long value2 = sb.getValue2();
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			int size = this.size;
			
			for (int i = start; i <= size; ++i) {
				if (datas1[i] == value1 && datas2[i] == value2) {
					return i;
				}
			}
			
			return 0;
		} else if (elem == null) {
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			int size = this.size;
			
			for (int i = start; i <= size; ++i) {
				if (datas1[i] == NULL && datas2[i] == NULL) {
					return i;
				}
			}
			
			return 0;
		} else {
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
		if (elem instanceof SerialBytes) {
			SerialBytes sb = (SerialBytes)elem;
			long value1 = sb.getValue1();
			long value2 = sb.getValue2();
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			
			for (int i = start; i > 0; --i) {
				if (datas1[i] == value1 && datas2[i] == value2) {
					return i;
				}
			}
			
			return 0;
		} else if (elem == null) {
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			
			for (int i = start; i > 0; --i) {
				if (datas1[i] == NULL && datas2[i] == NULL) {
					return i;
				}
			}
			
			return 0;
		} else {
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
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;

		if (elem instanceof SerialBytes) {
			SerialBytes sb = (SerialBytes)elem;
			long value1 = sb.getValue1();
			long value2 = sb.getValue2();
			
			if (isSorted) {
				int end = size;
				if (!isFromHead) {
					end = start;
					start = 1;
				}
				
				int index = binarySearch(sb, start, end);
				if (index < 1) {
					return new IntArray(1);
				}
				
				// �ҵ���һ��
				int first = index;
				while (first > start && datas1[first - 1] == value1 && datas2[first - 1] == value2) {
					first--;
				}
				
				// �ҵ����һ��
				int last = index;
				while (last < end && datas1[last + 1] == value1 && datas2[last + 1] == value2) {
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
						if (datas1[i] == value1 && datas2[i] == value2) {
							result.addInt(i);
						}
					}
				} else {
					for (int i = start; i > 0; --i) {
						if (datas1[i] == value1 && datas2[i] == value2) {
							result.addInt(i);
						}
					}
				}
				
				return result;
			}
		} else if (elem == null) {
			IntArray result = new IntArray(7);
			if (isSorted) {
				if (isFromHead) {
					for (int i = start; i <= size; ++i) {
						if (datas1[i] == NULL && datas2[i] == NULL) {
							result.addInt(i);
						} else {
							break;
						}
					}
				} else {
					for (int i = start; i > 0; --i) {
						if (datas1[i] == NULL && datas2[i] == NULL) {
							result.addInt(i);
						}
					}
				}
			} else {
				if (isFromHead) {
					for (int i = start; i <= size; ++i) {
						if (datas1[i] == NULL && datas2[i] == NULL) {
							result.addInt(i);
						}
					}
				} else {
					for (int i = start; i > 0; --i) {
						if (datas1[i] == NULL && datas2[i] == NULL) {
							result.addInt(i);
						}
					}
				}
			}
			
			return result;
		} else {
			return new IntArray(1);
		}
	}

	/**
	 * ��������
	 * @return
	 */
	public IArray dup() {
		int len = size + 1;
		long []newDatas1 = new long[len];
		long []newDatas2 = new long[len];
		System.arraycopy(datas1, 0, newDatas1, 0, len);
		System.arraycopy(datas2, 0, newDatas2, 0, len);
		
		return new SerialBytesArray(newDatas1, newDatas2, size);
	}

	/**
	 * ����һ��ͬ���͵�����
	 * @param count
	 * @return
	 */
	public IArray newInstance(int count) {
		return new SerialBytesArray(count);
	}

	/**
	 * �������Ա�����ֵ
	 * @return IArray ����ֵ����
	 */
	public IArray abs() {
		MessageManager mm = EngineMessage.get();
		throw new RuntimeException(getDataType() + mm.getMessage("Variant2.illAbs"));
	}

	/**
	 * �������Ա��
	 * @return IArray ��ֵ����
	 */
	public IArray negate() {
		MessageManager mm = EngineMessage.get();
		throw new RuntimeException(getDataType() + mm.getMessage("Variant2.illNegate"));
	}

	/**
	 * �������Ա���
	 * @return IArray ��ֵ����
	 */
	public IArray not() {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int size = this.size;
		
		boolean []newDatas = new boolean[size + 1];
		for (int i = 1; i <= size; ++i) {
			newDatas[i] = datas1[i] == NULL && datas2[i] == NULL;
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
		return false;
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�ĺ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberAdd(IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
				array.getDataType() + mm.getMessage("Variant2.illAdd"));
	}

	/**
	 * ��������ĳ�Ա��ָ�������ĺ�
	 * @param value ����
	 * @return ������
	 */
	public IArray memberAdd(Object value) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
				Variant.getDataType(value) + mm.getMessage("Variant2.illAdd"));
	}
	
	/**
	 * ��array��ָ��Ԫ�ؼӵ���ǰ�����ָ��Ԫ����
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ��ӵ�����
	 * @param index Ҫ��ӵ������Ԫ�ص�����
	 * @return IArray
	 */
	public IArray memberAdd(int curIndex, IArray array, int index) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
				array.getDataType() + mm.getMessage("Variant2.illAdd"));
	}	

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĳ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberSubtract(IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
				array.getDataType() + mm.getMessage("Variant2.illSubtract"));
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ļ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberMultiply(IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
				array.getDataType() + mm.getMessage("Variant2.illMultiply"));
	}

	/**
	 * ��������ĳ�Ա��ָ�������Ļ�
	 * @param value ����
	 * @return ������
	 */
	public IArray memberMultiply(Object value) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
				Variant.getDataType(value) + mm.getMessage("Variant2.illMultiply"));
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�ĳ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberDivide(IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
				array.getDataType() + mm.getMessage("Variant2.illDivide"));
	}

	/**
	 * ����������������Ӧ������Աȡ������г�Ա�����
	 * @param array �Ҳ�����
	 * @return ����������������������
	 */
	public IArray memberMod(IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
				array.getDataType() + mm.getMessage("Variant2.illMod"));
	}
	
	/**
	 * �����������������Ա���������г�Ա�
	 * @param array �Ҳ�����
	 * @return ����ֵ��������в����
	 */
	public IArray memberIntDivide(IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
				array.getDataType() + mm.getMessage("Variant2.illDivide"));
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @return ��ϵ����������
	 */
	public BoolArray calcRelation(IArray array, int relation) {
		if (array instanceof SerialBytesArray) {
			return calcRelation((SerialBytesArray)array, relation);
		} else if (array instanceof ConstArray) {
			return calcRelation(array.get(1), relation);
		} else if (array instanceof ObjectArray) {
			return calcRelation((ObjectArray)array, relation);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), array.get(1),
					getDataType(), array.getDataType()));
		}
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @return ��ϵ����������
	 */
	public BoolArray calcRelation(Object value, int relation) {
		if (value instanceof SerialBytes) {
			int size = this.size;
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			SerialBytes sb = (SerialBytes)value;
			long value1 = sb.getValue1();
			long value2 = sb.getValue2();
			boolean []resultDatas = new boolean[size + 1];
			
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], value1, value2) == 0;
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], value1, value2) > 0;
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], value1, value2) >= 0;
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], value1, value2) < 0;
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], value1, value2) <= 0;
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], value1, value2) != 0;
				}
			} else if (relation == Relation.AND) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = (datas1[i] != NULL || datas2[i] != NULL) && (value1 != NULL || value2 != NULL);
				}
			} else { // Relation.OR
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = datas1[i] != NULL || datas2[i] != NULL || value1 != NULL || value2 != NULL;
				}
			}
			
			BoolArray result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (value == null) {
			boolean []resultDatas = new boolean[size + 1];		
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas1[i] == NULL && datas2[i] == NULL) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas1[i] == NULL || datas2[i] == NULL) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas1[i] == NULL && datas2[i] == NULL) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (datas1[i] != NULL || datas2[i] != NULL) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.OR) {
				for (int i = 1; i <= size; ++i) {
					if (datas1[i] != NULL || datas2[i] != NULL) {
						resultDatas[i] = true;
					}
				}
			}
			
			BoolArray result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), value,
					getDataType(), Variant.getDataType(value)));
		}
	}
	
	private BoolArray calcRelation(SerialBytesArray other, int relation) {
		int size = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		long []otherDatas1 = other.datas1;
		long []otherDatas2 = other.datas2;
		boolean []resultDatas = new boolean[size + 1];
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) == 0;
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) > 0;
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) >= 0;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) < 0;
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) <= 0;
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) != 0;
			}
		} else if (relation == Relation.AND) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = (datas1[i] != NULL || datas2[i] != NULL) && (otherDatas1[i] != NULL || otherDatas2[i] != NULL);
			}
		} else { // Relation.OR
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = datas1[i] != NULL || datas2[i] != NULL || otherDatas1[i] != NULL || otherDatas2[i] != NULL;
			}
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}
	
	protected BoolArray calcRelation(ObjectArray array, int relation) {
		int size = this.size;
		Object []d2 = array.getDatas();
		boolean []resultDatas = new boolean[size + 1];
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compareTo(i, d2[i]) == 0;
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compareTo(i, d2[i]) > 0;
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compareTo(i, d2[i]) >= 0;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compareTo(i, d2[i]) < 0;
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compareTo(i, d2[i]) <= 0;
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compareTo(i, d2[i]) != 0;
			}
		} else if (relation == Relation.AND) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = isTrue(i) && Variant.isTrue(d2[i]);
			}
		} else { // Relation.OR
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = isTrue(i) || Variant.isTrue(d2[i]);
			}
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ���㣬ֻ����resultΪ�����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	public void calcRelations(IArray array, int relation, BoolArray result, boolean isAnd) {
		if (array instanceof SerialBytesArray) {
			calcRelations((SerialBytesArray)array, relation, result, isAnd);
		} else if (array instanceof ConstArray) {
			calcRelations(array.get(1), relation, result, isAnd);
		} else if (array instanceof ObjectArray) {
			calcRelations((ObjectArray)array, relation, result, isAnd);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), array.get(1),
					getDataType(), array.getDataType()));
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
		if (value instanceof SerialBytes) {
			int size = this.size;
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			SerialBytes sb = (SerialBytes)value;
			long value1 = sb.getValue1();
			long value2 = sb.getValue2();
			boolean []resultDatas = result.getDatas();
			
			if (isAnd) {
				// �������ִ��&&����
				if (relation == Relation.EQUAL) {
					// �Ƿ�����ж�
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] &&SerialBytes.compare(datas1[i], datas2[i], value1, value2) != 0) {
							resultDatas[i] = false;
						}
					}
				} else if (relation == Relation.GREATER) {
					// �Ƿ�����ж�
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) <= 0) {
							resultDatas[i] = false;
						}
					}
				} else if (relation == Relation.GREATER_EQUAL) {
					// �Ƿ���ڵ����ж�
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) < 0) {
							resultDatas[i] = false;
						}
					}
				} else if (relation == Relation.LESS) {
					// �Ƿ�С���ж�
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) >= 0) {
							resultDatas[i] = false;
						}
					}
				} else if (relation == Relation.LESS_EQUAL) {
					// �Ƿ�С�ڵ����ж�
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) > 0) {
							resultDatas[i] = false;
						}
					}
				} else if (relation == Relation.NOT_EQUAL) {
					// �Ƿ񲻵����ж�
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) == 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					throw new RuntimeException();
				}
			} else {
				// �������ִ��||����
				if (relation == Relation.EQUAL) {
					// �Ƿ�����ж�
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) == 0) {
							resultDatas[i] = true;
						}
					}
				} else if (relation == Relation.GREATER) {
					// �Ƿ�����ж�
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) > 0) {
							resultDatas[i] = true;
						}
					}
				} else if (relation == Relation.GREATER_EQUAL) {
					// �Ƿ���ڵ����ж�
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) >= 0) {
							resultDatas[i] = true;
						}
					}
				} else if (relation == Relation.LESS) {
					// �Ƿ�С���ж�
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) < 0) {
							resultDatas[i] = true;
						}
					}
				} else if (relation == Relation.LESS_EQUAL) {
					// �Ƿ�С�ڵ����ж�
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) <= 0) {
							resultDatas[i] = true;
						}
					}
				} else if (relation == Relation.NOT_EQUAL) {
					// �Ƿ񲻵����ж�
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], value1, value2) != 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					throw new RuntimeException();
				}
			}
		} else if (value == null) {
			long []datas1 = this.datas1;
			long []datas2 = this.datas2;
			boolean []resultDatas = result.getDatas();
			
			if (isAnd) {
				// �������ִ��&&����
				if (relation == Relation.EQUAL) {
					// �Ƿ�����ж�
					for (int i = 1; i <= size; ++i) {
						if (datas1[i] != NULL || datas2[i] != NULL) {
							resultDatas[i] = false;
						}
					}
				} else if (relation == Relation.GREATER) {
					// �Ƿ�����ж�
					for (int i = 1; i <= size; ++i) {
						if (datas1[i] == NULL && datas2[i] == NULL) {
							resultDatas[i] = false;
						}
					}
				} else if (relation == Relation.GREATER_EQUAL) {
					// �Ƿ���ڵ����ж�
				} else if (relation == Relation.LESS) {
					// �Ƿ�С���ж�
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = false;
					}
				} else if (relation == Relation.LESS_EQUAL) {
					// �Ƿ�С�ڵ����ж�
					for (int i = 1; i <= size; ++i) {
						if (datas1[i] != NULL || datas2[i] != NULL) {
							resultDatas[i] = false;
						}
					}
				} else if (relation == Relation.NOT_EQUAL) {
					// �Ƿ񲻵����ж�
					for (int i = 1; i <= size; ++i) {
						if (datas1[i] == NULL && datas2[i] == NULL) {
							resultDatas[i] = false;
						}
					}
				} else {
					throw new RuntimeException();
				}
			} else {
				// �������ִ��||����
				if (relation == Relation.EQUAL) {
					// �Ƿ�����ж�
					for (int i = 1; i <= size; ++i) {
						if (datas1[i] == NULL && datas2[i] == NULL) {
							resultDatas[i] = true;
						}
					}
				} else if (relation == Relation.GREATER) {
					// �Ƿ�����ж�
					for (int i = 1; i <= size; ++i) {
						if (datas1[i] != NULL || datas2[i] != NULL) {
							resultDatas[i] = true;
						}
					}
				} else if (relation == Relation.GREATER_EQUAL) {
					// �Ƿ���ڵ����ж�
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = true;
					}
				} else if (relation == Relation.LESS) {
					// �Ƿ�С���ж�
				} else if (relation == Relation.LESS_EQUAL) {
					// �Ƿ�С�ڵ����ж�
					for (int i = 1; i <= size; ++i) {
						if (datas1[i] == NULL && datas2[i] == NULL) {
							resultDatas[i] = true;
						}
					}
				} else if (relation == Relation.NOT_EQUAL) {
					// �Ƿ񲻵����ж�
					for (int i = 1; i <= size; ++i) {
						if (datas1[i] != NULL || datas2[i] != NULL) {
							resultDatas[i] = true;
						}
					}
				} else {
					throw new RuntimeException();
				}
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), value,
					getDataType(), Variant.getDataType(value)));
		}
	}
	
	private void calcRelations(SerialBytesArray other, int relation, BoolArray result, boolean isAnd) {
		int size = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		long []otherDatas1 = other.datas1;
		long []otherDatas2 = other.datas2;
		boolean []resultDatas = result.getDatas();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) != 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) <= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) < 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) >= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) > 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) == 0) {
						resultDatas[i] = false;
					}
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// �������ִ��||����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) == 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) > 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) >= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) < 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) <= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]) != 0) {
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
		Object []d2 = array.getDatas();
		boolean []resultDatas = result.getDatas();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compareTo(i, d2[i]) != 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compareTo(i, d2[i]) <= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compareTo(i, d2[i]) < 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compareTo(i, d2[i]) >= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compareTo(i, d2[i]) > 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compareTo(i, d2[i]) == 0) {
						resultDatas[i] = false;
					}
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// �������ִ��||����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compareTo(i, d2[i]) == 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compareTo(i, d2[i]) > 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compareTo(i, d2[i]) >= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compareTo(i, d2[i]) < 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compareTo(i, d2[i]) <= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compareTo(i, d2[i]) != 0) {
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
		MessageManager mm = EngineMessage.get();
		throw new RQException("and" + mm.getMessage("function.paramTypeError"));
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ��
	 * @param array �Ҳ�����
	 * @return ��λ��������
	 */
	public IArray bitwiseOr(IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException("or" + mm.getMessage("function.paramTypeError"));
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ���
	 * @param array �Ҳ�����
	 * @return ��λ���������
	 */
	public IArray bitwiseXOr(IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException("xor" + mm.getMessage("function.paramTypeError"));
	}
	
	/**
	 * ���������Ա�İ�λȡ��
	 * @return ��Ա��λȡ���������
	 */
	public IArray bitwiseNot() {
		MessageManager mm = EngineMessage.get();
		throw new RQException("not" + mm.getMessage("function.paramTypeError"));
	}

	/**
	 * ���������2����Ա�ıȽ�ֵ
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	public int memberCompare(int index1, int index2) {
		return SerialBytes.compare(datas1[index1], datas2[index1], datas1[index2], datas2[index2]);
	}

	/**
	 * �ж������������Ա�Ƿ����
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	public boolean isMemberEquals(int index1, int index2) {
		return datas1[index1] == datas1[index2] &&  datas2[index1] == datas2[index2];
	}

	/**
	 * �Ƚ���������Ĵ�С
	 * @param array �Ҳ�����
	 * @return 1����ǰ�����0������������ȣ�-1����ǰ����С
	 */
	public int compareTo(IArray array) {
		int size1 = this.size;
		int size2 = array.size();
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		int size = size1;
		int result = 0;
		if (size1 < size2) {
			result = -1;
		} else if (size1 > size2) {
			result = 1;
			size = size2;
		}

		if (array instanceof SerialBytesArray) {
			SerialBytesArray other = (SerialBytesArray)array;
			long []otherDatas1 = other.datas1;
			long []otherDatas2 = other.datas2;
			
			for (int i = 1; i <= size; ++i) {
				int cmp = SerialBytes.compare(datas1[i], datas2[i], otherDatas1[i], otherDatas2[i]);
				if (cmp != 0) {
					return cmp;
				}
			}
		} else if (array instanceof ConstArray) {
			Object value = array.get(1);
			if (value instanceof SerialBytes) {
				SerialBytes sb = (SerialBytes)value;
				long value1 = sb.getValue1();
				long value2 = sb.getValue2();
				
				for (int i = 1; i <= size; ++i) {
					int cmp = SerialBytes.compare(datas1[i], datas2[i], value1, value2);
					if (cmp != 0) {
						return cmp;
					}
				}
			} else if (value == null) {
				for (int i = 1; i <= size; ++i) {
					if (datas1[i] != NULL || datas2[i] != NULL) {
						return 1;
					}
				}
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Variant2.illCompare", get(1), value,
						getDataType(), array.getDataType()));
			}
		} else if (array instanceof ObjectArray) {
			ObjectArray array2 = (ObjectArray)array;
			Object []d2 = array2.getDatas();
			
			for (int i = 1; i <= size; ++i) {
				int cmp = compareTo(i, d2[i]);
				if (cmp != 0) {
					return cmp;
				}
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), array.get(1),
					getDataType(), array.getDataType()));
		}
		
		return result;
	}

	/**
	 * ȡָ����Ա�Ĺ�ϣֵ
	 * @param index ��Ա��������1��ʼ����
	 * @return ָ����Ա�Ĺ�ϣֵ
	 */
	public int hashCode(int index) {
		return HashUtil.hashCode(datas1[index] + datas2[index]);
	}

	/**
	 * ���Ա��
	 * @return
	 */
	public Object sum() {
		return null;
	}
	
	/**
	 * ��ƽ��ֵ
	 * @return
	 */
	public Object average() {
		return null;
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

		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		long max1 = datas1[1];
		long max2 = datas2[1];
		
		for (int i = 2; i <= size; ++i) {
			if (SerialBytes.compare(max1, max2, datas1[i], datas2[i]) < 0) {
				max1 = datas1[i];
				max2 = datas2[i];
			}
		}
		
		if (max1 != NULL || max2 != NULL) {
			return new SerialBytes(max1, max2);
		} else {
			return null;
		}
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

		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		long min1 = 0;
		long min2 = 0;
		
		int i = 1;
		for (; i <= size; ++i) {
			if (datas1[i] != NULL || datas2[i] != NULL) {
				min1 = datas1[i];
				min2 = datas2[i];
				break;
			}
		}
		
		for (++i; i <= size; ++i) {
			if ((datas1[i] != NULL || datas2[i] != NULL) && 
					SerialBytes.compare(min1, min2, datas1[i], datas2[i]) > 0) {
				min1 = datas1[i];
				min2 = datas2[i];
			}
		}
		
		if (min1 != NULL || min2 != NULL) {
			return new SerialBytes(min1, min2);
		} else {
			return null;
		}
	}

	/**
	 * ����ָ�������ڵ�����
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã�������
	 */
	public void reserve(int start, int end) {
		int newSize = end - start + 1;
		System.arraycopy(datas1, start, datas1, 1, newSize);
		System.arraycopy(datas2, start, datas2, 1, newSize);
		size = newSize;
	}

	/**
	 * �ѳ�Աת�ɶ������鷵��
	 * @return ��������
	 */
	public Object[] toArray() {
		int size = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		Object []result = new Object[size];
		
		for (int i = 1; i <= size; ++i) {
			if (datas1[i] != NULL || datas2[i] != NULL) {
				result[i - 1] = new SerialBytes(datas1[i], datas2[i]);
			}
		}
		
		return result;
	}

	/**
	 * �ѳ�Ա�ָ��������
	 * @param result ���ڴ�ų�Ա������
	 */
	public void toArray(Object []result) {
		int size = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;

		for (int i = 1; i <= size; ++i) {
			if (datas1[i] != NULL || datas2[i] != NULL) {
				result[i - 1] = new SerialBytes(datas1[i], datas2[i]);
			} else {
				result[i - 1] = null;
			}
		}
	}

	/**
	 * �������ָ��λ�ò����������
	 * @param pos λ�ã�����
	 * @return ���غ�벿��Ԫ�ع��ɵ�����
	 */
	public IArray split(int pos) {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int size = this.size;
		int resultSize = size - pos + 1;
		
		long []resultDatas1 = new long[resultSize + 1];
		long []resultDatas2 = new long[resultSize + 1];
		System.arraycopy(datas1, pos, resultDatas1, 1, resultSize);
		System.arraycopy(datas2, pos, resultDatas2, 1, resultSize);
		
		
		this.size = pos - 1;
		return new SerialBytesArray(resultDatas1, resultDatas2, resultSize);
	}

	/**
	 * ��ָ������Ԫ�ط���������������
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 * @return
	 */
	public IArray split(int from, int to) {
		int oldSize = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		int resultSize = to - from + 1;
		long []resultDatas1 = new long[resultSize + 1];
		long []resultDatas2 = new long[resultSize + 1];
		System.arraycopy(datas1, from, resultDatas1, 1, resultSize);
		System.arraycopy(datas2, from, resultDatas2, 1, resultSize);
		
		System.arraycopy(datas1, to + 1, datas1, from, oldSize - to);
		System.arraycopy(datas2, to + 1, datas2, from, oldSize - to);
		
		this.size -= resultSize;
		return new SerialBytesArray(resultDatas1, resultDatas2, resultSize);
	}

	/**
	 * ����������ʹ����Ԫ�������
	 */
	public void trimToSize() {
		int newLen = size + 1;
		if (newLen < datas1.length) {
			long []newDatas1 = new long[newLen];
			long []newDatas2 = new long[newLen];
			System.arraycopy(datas1, 0, newDatas1, 0, newLen);
			System.arraycopy(datas2, 0, newDatas2, 0, newLen);
			datas1 = newDatas1;
			datas2 = newDatas2;
		}
	}

	/**
	 * ȡ����ʶ����ȡֵΪ����ж�Ӧ�����ݣ����������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	public IArray select(IArray signArray) {
		int size = signArray.size();
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;

		long []newDatas1 = new long[size + 1];
		long []newDatas2 = new long[size + 1];
		int count = 0;
		
		if (signArray instanceof BoolArray) {
			BoolArray array = (BoolArray)signArray;
			boolean []d2 = array.getDatas();
			boolean []s2 = array.getSigns();
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					if (d2[i]) {
						++count;
						newDatas1[count] = datas1[i];
						newDatas2[count] = datas2[i];
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i] && d2[i]) {
						++count;
						newDatas1[count] = datas1[i];
						newDatas2[count] = datas2[i];
					}
				}
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				if (signArray.isTrue(i)) {
					++count;
					newDatas1[count] = datas1[i];
					newDatas2[count] = datas2[i];
				}
			}
		}
		
		return new SerialBytesArray(newDatas1, newDatas2, count);
	}
	
	/**
	 * ȡĳһ���α�ʶ����ȡֵΪ��������������
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã���������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	public IArray select(int start, int end, IArray signArray) {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		long []newDatas1 = new long[end - start + 1];
		long []newDatas2 = new long[end - start + 1];
		int count = 0;
		
		if (signArray instanceof BoolArray) {
			BoolArray array = (BoolArray)signArray;
			boolean []d2 = array.getDatas();
			boolean []s2 = array.getSigns();
			
			if (s2 == null) {
				for (int i = start; i < end; ++i) {
					if (d2[i]) {
						++count;
						newDatas1[count] = datas1[i];
						newDatas2[count] = datas2[i];
					}
				}
			} else {
				for (int i = start; i < end; ++i) {
					if (!s2[i] && d2[i]) {
						++count;
						newDatas1[count] = datas1[i];
						newDatas2[count] = datas2[i];
					}
				}
			}
		} else {
			for (int i = start; i < end; ++i) {
				if (signArray.isTrue(i)) {
					++count;
					newDatas1[count] = datas1[i];
					newDatas2[count] = datas2[i];
				}
			}
		}
		
		return new SerialBytesArray(newDatas1, newDatas2, count);
	}

	/**
	 * �ж����������ָ��Ԫ���Ƿ���ͬ
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ�Ƚϵ�����
	 * @param index Ҫ�Ƚϵ������Ԫ�ص�����
	 * @return true����ͬ��false������ͬ
	 */
	public boolean isEquals(int curIndex, IArray array, int index) {
		if (array instanceof SerialBytesArray) {
			SerialBytesArray sba = (SerialBytesArray)array;
			return datas1[curIndex] == sba.datas1[index] && datas2[curIndex] == sba.datas2[index];
		} else {
			return isEquals(curIndex, array.get(index));
		}
	}

	/**
	 * �ж������ָ��Ԫ���Ƿ������ֵ���
	 * @param curIndex ����Ԫ����������1��ʼ����
	 * @param value ֵ
	 * @return true����ȣ�false�������
	 */
	public boolean isEquals(int curIndex, Object value) {
		if (value instanceof SerialBytes) {
			SerialBytes sb = (SerialBytes)value;
			return datas1[curIndex] == sb.getValue1() && datas2[curIndex] == sb.getValue2();
		} else if (value == null) {
			return datas1[curIndex] == NULL && datas2[curIndex] == NULL;
		} else {
			return false;
		}
	}

	/**
	 * �ж����������ָ��Ԫ�صĴ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ�Ƚϵ�����
	 * @param index Ҫ�Ƚϵ������Ԫ�ص�����
	 * @return С�ڣ�С��0�����ڣ�0�����ڣ�����0
	 */
	public int compareTo(int curIndex, IArray array, int index) {
		if (array instanceof SerialBytesArray) {
			SerialBytesArray sba = (SerialBytesArray)array;
			return SerialBytes.compare(datas1[curIndex], datas2[curIndex], sba.datas1[index], sba.datas2[index]);
		} else {
			return compareTo(curIndex, array.get(index));
		}
	}

	/**
	 * �Ƚ������ָ��Ԫ�������ֵ�Ĵ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param value Ҫ�Ƚϵ�ֵ
	 * @return
	 */
	public int compareTo(int curIndex, Object value) {
		if (value instanceof SerialBytes) {
			SerialBytes sb = (SerialBytes)value;
			return SerialBytes.compare(datas1[curIndex], datas2[curIndex], sb.getValue1(), sb.getValue2());
		} else if (value == null) {
			return datas1[curIndex] != NULL || datas2[curIndex] != NULL ? 1 : 0;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(curIndex), value,
					mm.getMessage("DataType.SerialBytes"), Variant.getDataType(value)));
		}
	}

	/**
	 * �������Ԫ�ؽ�������
	 */
	public void sort() {
		SerialBytes []sbs = new SerialBytes[size];
		toArray(sbs);
		MultithreadUtil.sort(sbs);
		
		int i = 0;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		for (SerialBytes sb : sbs) {
			i++;
			if (sb == null) {
				datas1[i] = NULL;
				datas2[i] = NULL;
			} else {
				datas1[i] = sb.getValue1();
				datas2[i] = sb.getValue2();
			}
		}
	}

	/**
	 * �������Ԫ�ؽ�������
	 * @param comparator �Ƚ���
	 */
	public void sort(Comparator<Object> comparator) {
		SerialBytes []sbs = new SerialBytes[size];
		toArray(sbs);
		MultithreadUtil.sort(sbs, comparator);
		
		int i = 0;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		for (SerialBytes sb : sbs) {
			i++;
			if (sb == null) {
				datas1[i] = NULL;
				datas2[i] = NULL;
			} else {
				datas1[i] = sb.getValue1();
				datas2[i] = sb.getValue2();
			}
		}
	}

	/**
	 * �����������Ƿ��м�¼
	 * @return boolean
	 */
	public boolean hasRecord() {
		return false;
	}

	/**
	 * �����Ƿ��ǣ���������
	 * @param isPure true������Ƿ��Ǵ�����
	 * @return boolean true���ǣ�false������
	 */
	public boolean isPmt(boolean isPure) {
		return false;
	}

	/**
	 * ��������ķ�ת����
	 * @return IArray
	 */
	public IArray rvs() {
		int size = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		long []resultDatas1 = new long[size + 1];
		long []resultDatas2 = new long[size + 1];
		
		for (int i = 1, q = size; i <= size; ++i, --q) {
			resultDatas1[i] = datas1[q];
			resultDatas2[i] = datas2[q];
		}
		
		return new SerialBytesArray(resultDatas1, resultDatas2, size);
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
		
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		if (ignoreNull) {
			if (count == 1) {
				// ȡ��Сֵ��λ��
				long minValue1 = NULL;
				long minValue2 = NULL;
				
				if (isAll) {
					IntArray result = new IntArray(8);
					int i = 1;
					for (; i <= size; ++i) {
						if (datas1[i] != NULL || datas2[i] != NULL) {
							minValue1 = datas1[i];
							minValue2 = datas2[i];
							result.addInt(i);
							break;
						}
					}
					
					for (++i; i <= size; ++i) {
						if (datas1[i] != NULL || datas2[i] != NULL) {
							int cmp = SerialBytes.compare(datas1[i], datas2[i], minValue1, minValue2);
							if (cmp < 0) {
								minValue1 = datas1[i];
								minValue2 = datas2[i];
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
						if (datas1[i] != NULL || datas2[i] != NULL) {
							minValue1 = datas1[i];
							minValue2 = datas2[i];
							pos = i;
							break;
						}
					}
					
					for (--i; i > 0; --i) {
						if ((datas1[i] != NULL || datas2[i] != NULL) && 
								SerialBytes.compare(datas1[i], datas2[i], minValue1, minValue2) < 0) {
							minValue1 = datas1[i];
							minValue2 = datas2[i];
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
						if (datas1[i] != NULL || datas2[i] != NULL) {
							minValue1 = datas1[i];
							minValue2 = datas2[i];
							pos = i;
							break;
						}
					}
					
					for (++i; i <= size; ++i) {
						if ((datas1[i] != NULL || datas2[i] != NULL) && 
								SerialBytes.compare(datas1[i], datas2[i], minValue1, minValue2) < 0) {
							minValue1 = datas1[i];
							minValue2 = datas2[i];
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
				SerialBytesArray valueArray = new SerialBytesArray(next);
				IntArray posArray = new IntArray(next);
				for (int i = 1; i <= size; ++i) {
					if (datas1[i] != NULL || datas2[i] != NULL) {
						int index = valueArray.binarySearch(datas1[i], datas2[i], 1, valueArray.size);
						if (index < 1) {
							index = -index;
						}
						
						if (index <= count) {
							valueArray.insert(index, datas1[i], datas2[i]);
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
				long maxValue1 = NULL;
				long maxValue2 = NULL;

				if (isAll) {
					IntArray result = new IntArray(8);
					int i = 1;
					for (; i <= size; ++i) {
						if (datas1[i] != NULL || datas2[i] != NULL) {
							maxValue1 = datas1[i];
							maxValue2 = datas2[i];
							result.addInt(i);
							break;
						}
					}
					
					for (++i; i <= size; ++i) {
						if (datas1[i] != NULL || datas2[i] != NULL) {
							int cmp = SerialBytes.compare(datas1[i], datas2[i], maxValue1, maxValue2);
							if (cmp > 0) {
								maxValue1 = datas1[i];
								maxValue2 = datas2[i];
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
						if (datas1[i] != NULL || datas2[i] != NULL) {
							maxValue1 = datas1[i];
							maxValue2 = datas2[i];
							pos = i;
							break;
						}
					}
					
					for (--i; i > 0; --i) {
						if ((datas1[i] != NULL || datas2[i] != NULL) && 
								SerialBytes.compare(datas1[i], datas2[i], maxValue1, maxValue2) > 0) {
							maxValue1 = datas1[i];
							maxValue2 = datas2[i];
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
						if (datas1[i] != NULL || datas2[i] != NULL) {
							maxValue1 = datas1[i];
							maxValue2 = datas2[i];
							pos = i;
							break;
						}
					}
					
					for (++i; i <= size; ++i) {
						if ((datas1[i] != NULL || datas2[i] != NULL) && 
								SerialBytes.compare(datas1[i], datas2[i], maxValue1, maxValue2) > 0) {
							maxValue1 = datas1[i];
							maxValue2 = datas2[i];
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
				SerialBytesArray valueArray = new SerialBytesArray(next);
				IntArray posArray = new IntArray(next);
				
				for (int i = 1; i <= size; ++i) {
					if (datas1[i] != NULL || datas2[i] != NULL) {
						int index = valueArray.descBinarySearch(datas1[i], datas2[i]);
						if (index < 1) {
							index = -index;
						}
						
						if (index <= count) {
							valueArray.insert(index, datas1[i], datas2[i]);
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
					long minValue1 = datas1[1];
					long minValue2 = datas2[1];
					
					for (int i = 2; i <= size; ++i) {
						int cmp = SerialBytes.compare(datas1[i], datas2[i], minValue1, minValue2);
						if (cmp < 0) {
							minValue1 = datas1[i];
							minValue2 = datas2[i];
							result.clear();
							result.addInt(i);
						} else if (cmp == 0) {
							result.addInt(i);
						}
					}
					
					return result;
				} else if (isLast) {
					long minValue1 = datas1[size];
					long minValue2 = datas2[size];
					int pos = size;
					
					for (int i = size - 1; i > 0; --i) {
						if (SerialBytes.compare(datas1[i], datas2[i], minValue1, minValue2) < 0) {
							minValue1 = datas1[i];
							minValue2 = datas2[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					result.pushInt(pos);
					return result;
				} else {
					long minValue1 = datas1[1];
					long minValue2 = datas2[1];
					int pos = 1;
					
					for (int i = 2; i <= size; ++i) {
						if (SerialBytes.compare(datas1[i], datas2[i], minValue1, minValue2) < 0) {
							minValue1 = datas1[i];
							minValue2 = datas2[i];
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
				SerialBytesArray valueArray = new SerialBytesArray(next);
				IntArray posArray = new IntArray(next);
				
				for (int i = 1; i <= size; ++i) {
					int index = valueArray.binarySearch(datas1[i], datas2[i], 1, valueArray.size);
					if (index < 1) {
						index = -index;
					}
					
					if (index <= count) {
						valueArray.insert(index, datas1[i], datas2[i]);
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
					long maxValue1 = datas1[1];
					long maxValue2 = datas2[1];
					result.addInt(1);
					
					for (int i = 2; i <= size; ++i) {
						int cmp = SerialBytes.compare(datas1[i], datas2[i], maxValue1, maxValue2);
						if (cmp > 0) {
							maxValue1 = datas1[i];
							maxValue2 = datas2[i];
							result.clear();
							result.addInt(i);
						} else if (cmp == 0) {
							result.addInt(i);
						}
					}
					
					return result;
				} else if (isLast) {
					long maxValue1 = datas1[size];
					long maxValue2 = datas2[size];
					int pos = size;
					
					for (int i = size - 1; i > 0; --i) {
						if (SerialBytes.compare(datas1[i], datas2[i], maxValue1, maxValue2) > 0) {
							maxValue1 = datas1[i];
							maxValue2 = datas2[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					result.pushInt(pos);
					return result;
				} else {
					long maxValue1 = datas1[1];
					long maxValue2 = datas2[1];
					int pos = 1;
					
					for (int i = 2; i <= size; ++i) {
						if (SerialBytes.compare(datas1[i], datas2[i], maxValue1, maxValue2) > 0) {
							maxValue1 = datas1[i];
							maxValue2 = datas2[i];
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
				SerialBytesArray valueArray = new SerialBytesArray(next);
				IntArray posArray = new IntArray(next);
				
				for (int i = 1; i <= size; ++i) {
					int index = valueArray.descBinarySearch(datas1[i], datas2[i]);
					if (index < 1) {
						index = -index;
					}
					
					if (index <= count) {
						valueArray.insert(index, datas1[i], datas2[i]);
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
		throw new RuntimeException();
	}

	/**
	 * �ѵ�ǰ����ת�ɶ������飬�����ǰ�����Ƕ��������򷵻����鱾��
	 * @return ObjectArray
	 */
	public ObjectArray toObjectArray() {
		int size = this.size;
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		Object []resultDatas = new Object[size + 1];
		
		for (int i = 1; i <= size; ++i) {
			if (datas1[i] != NULL || datas2[i] != NULL) {
				resultDatas[i] = new SerialBytes(datas1[i], datas2[i]);
			}
		}
		
		return new ObjectArray(resultDatas, size);
	}

	/**
	 * �Ѷ�������ת�ɴ��������飬����ת���׳��쳣
	 * @return IArray
	 */
	public IArray toPureArray() {
		return this;
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
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		if (other instanceof SerialBytesArray) {
			SerialBytesArray sba = (SerialBytesArray)other;
			long []otherDatas1 = sba.datas1;
			long []otherDatas2 = sba.datas2;
			
			if (isTemporary()) {
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						datas1[i] = otherDatas1[i];
						datas2[i] = otherDatas2[i];
					}
				}
				
				return this;
			} else {
				long []resultDatas1 = new long[size + 1];
				long []resultDatas2 = new long[size + 1];
				System.arraycopy(datas1, 1, resultDatas1, 1, size);
				System.arraycopy(datas2, 1, resultDatas2, 1, size);
				
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						resultDatas1[i] = otherDatas1[i];
						resultDatas2[i] = otherDatas2[i];
					}
				}
				
				IArray result = new SerialBytesArray(resultDatas1, resultDatas2, size);
				result.setTemporary(true);
				return result;
			}
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				if (signArray.isFalse(i)) {
					resultDatas[i] = other.get(i);
				} else {
					resultDatas[i] = get(i);
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
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		
		if (value instanceof SerialBytes || value == null) {
			long value1 = NULL;
			long value2 = NULL;
			
			if (value != null) {
				SerialBytes sb = (SerialBytes)value;
				value1 = sb.getValue1();
				value2 = sb.getValue2();
			}
			
			if (isTemporary()) {
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						datas1[i] = value1;
						datas2[i] = value2;
					}
				}
				
				return this;
			} else {
				long []resultDatas1 = new long[size + 1];
				long []resultDatas2 = new long[size + 1];
				System.arraycopy(datas1, 1, resultDatas1, 1, size);
				System.arraycopy(datas2, 1, resultDatas2, 1, size);
				
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						resultDatas1[i] = value1;
						resultDatas2[i] = value2;
					}
				}
				
				IArray result = new SerialBytesArray(resultDatas1, resultDatas2, size);
				result.setTemporary(true);
				return result;
			}
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				if (signArray.isFalse(i)) {
					resultDatas[i] = value;
				} else {
					resultDatas[i] = get(i);
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	public long[] getDatas1() {
		return datas1;
	}

	public long[] getDatas2() {
		return datas2;
	}
	
	public long getData1(int index) {
		return datas1[index];
	}

	public long getData2(int index) {
		return datas2[index];
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
		MessageManager mm = EngineMessage.get();
		throw new RQException("bit1" + mm.getMessage("function.paramTypeError"));
	}

	/**
	 * ���������Ա��λ���ֵ�Ķ����Ʊ�ʾʱ1�ĸ�����
	 * @param array �������
	 * @return 1�ĸ�����
	 */
	public int bit1(IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException("bit1" + mm.getMessage("function.paramTypeError"));
	}
	
	/**
	 * ȡָ��λ��������ͬ��Ԫ������
	 * @param index λ��
	 * @return ������ͬ��Ԫ������
	 */
	public int getNextEqualCount(int index) {
		long []datas1 = this.datas1;
		long []datas2 = this.datas2;
		int size = this.size;
		int count = 1;
		
		long value1 = datas1[index];
		long value2 = datas2[index];
		for (++index; index <= size; ++index) {
			if (datas1[index] == value1 && datas2[index] == value2) {
				count++;
			} else {
				break;
			}
		}
		
		return count;
	}
}
