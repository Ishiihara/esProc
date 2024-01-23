package com.scudata.array;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.expression.Relation;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.MultithreadUtil;
import com.scudata.util.Variant;

/**
 * �������飬��1��ʼ����
 * ʵ��ʹ��long�棬���ּ��������long����
 * ��Ҫ�ö������ʱ����һ��Date���󲻶ϵ��ã�date.setTime(long date)�����ɱ���һ��
 * @author LW
 *
 */
public class DateArray implements IArray {
	private static final long serialVersionUID = 1L;
	private static Date TEMP = new Date(); // ��ʱ��־
	
	private Date []datas;
	private int size;
	
	public DateArray() {
		datas = new Date[DEFAULT_LEN];
	}

	public DateArray(int initialCapacity) {
		datas = new Date[++initialCapacity];
	}
	
	public DateArray(Date []datas, int size) {
		this.datas = datas;
		this.size = size;
	}
	
	public static int compare(Date d1, Date d2) {
		if (d1 == null) {
			return d2 == null ? 0 : -1;
		} else if (d2 == null) {
			return 1;
		} else {
			long t1 = d1.getTime();
			long t2 = d2.getTime();
			return (t1 < t2 ? -1 : (t1 > t2 ? 1 : 0));
		}
	}
	
	private static int compare(Date d1, long t2) {
		if (d1 != null) {
			long t1 = d1.getTime();
			return (t1 < t2 ? -1 : (t1 > t2 ? 1 : 0));
		} else {
			return 0;
		}
	}
	
	private static int compare(Date d1, Object d2) {
		if (d2 == null) {
			return d1 == null ? 0 : 1;
		} else if (d2 instanceof Date) {
			if (d1 == null) {
				return -1;
			} else {
				long t1 = d1.getTime();
				long t2 = ((Date)d2).getTime();
				return (t1 < t2 ? -1 : (t1 > t2 ? 1 : 0));
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", d1, d2,
					mm.getMessage("DataType.Date"), Variant.getDataType(d2)));
		}
	}
	
	public Date[] getDatas() {
		return datas;
	}
	
	/**
	 * ȡ��������ʹ������ڴ�����Ϣ��ʾ
	 * @return ���ʹ�
	 */
	public String getDataType() {
		MessageManager mm = EngineMessage.get();
		return mm.getMessage("DataType.Date");
	}
	
	/**
	 * ��������
	 * @return
	 */
	public IArray dup() {
		int len = size + 1;
		Date []newDatas = new Date[len];
		System.arraycopy(datas, 0, newDatas, 0, len);
		return new DateArray(newDatas, size);
	}
	
	/**
	 * д���ݵ���
	 * @param out �����
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		int size = this.size;
		Date []datas = this.datas;
		
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
		Date []datas = this.datas = new Date[len];
		
		for (int i = 1; i < len; ++i) {
			datas[i] = (Date)in.readObject();
		}
	}
	
	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		int size = this.size;
		Date []datas = this.datas;
		
		out.writeByte(1);
		out.writeInt(size);
		for (int i = 1; i <= size; ++i) {
			out.writeObject(datas[i], true);
		}

		return out.toByteArray();
	}
	
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		in.readByte();
		size = in.readInt();
		int len = size + 1;
		Date []datas = this.datas = new Date[len];
		
		for (int i = 1; i < len; ++i) {
			datas[i] = (Date)in.readObject(true);
		}
	}
	
	/**
	 * ����һ��ͬ���͵�����
	 * @param count
	 * @return
	 */
	public IArray newInstance(int count) {
		return new DateArray(count);
	}

	/**
	 * ׷��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void add(Object o) {
		if (o instanceof Date) {
			ensureCapacity(size + 1);
			datas[++size] = (Date)o;
		} else if (o == null) {
			ensureCapacity(size + 1);
			datas[++size] = null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Date"), Variant.getDataType(o)));
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(IArray array) {
		int size2 = array.size();
		if (size2 == 0) {
		} else if (array instanceof DateArray) {
			DateArray dateArray = (DateArray)array;
			ensureCapacity(size + size2);
			
			System.arraycopy(dateArray.datas, 1, datas, size + 1, size2);
			size += size2;
		} else if (array instanceof ConstArray) {
			Object obj = array.get(1);
			if (obj instanceof Date) {
				ensureCapacity(size + size2);
				Date v = (Date)obj;
				Date []datas = this.datas;
				
				for (int i = 0; i < size2; ++i) {
					datas[++size] = v;
				}
			} else if (obj == null) {
				ensureCapacity(size + size2);
				Date []datas = this.datas;
				
				for (int i = 0; i < size2; ++i) {
					datas[++size] = null;
				}
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Date"), array.getDataType()));
			}
		} else {
			//MessageManager mm = EngineMessage.get();
			//throw new RQException(mm.getMessage("pdm.arrayTypeError", 
			//		mm.getMessage("DataType.Date"), array.getDataType()));
			ensureCapacity(size + size2);
			Date []datas = this.datas;
			
			for (int i = 1; i <= size2; ++i) {
				Object obj = array.get(i);
				if (obj instanceof Date) {
					datas[++size] = (Date)obj;
				} else if (obj == null) {
					datas[++size] = null;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("pdm.arrayTypeError", 
							mm.getMessage("DataType.Date"), Variant.getDataType(obj)));
				}
			}
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 * @param count Ԫ�ظ���
	 */
	public void addAll(IArray array, int count) {
		if (count == 0) {
		} else if (array instanceof DateArray) {
			DateArray dateArray = (DateArray)array;
			ensureCapacity(size + count);
			
			System.arraycopy(dateArray.datas, 1, datas, size + 1, count);
			size += count;
		} else if (array instanceof ConstArray) {
			Object obj = array.get(1);
			if (obj instanceof Date) {
				ensureCapacity(size + count);
				Date v = (Date)obj;
				Date []datas = this.datas;
				
				for (int i = 0; i < count; ++i) {
					datas[++size] = v;
				}
			} else if (obj == null) {
				ensureCapacity(size + count);
				Date []datas = this.datas;
				
				for (int i = 0; i < count; ++i) {
					datas[++size] = null;
				}
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Date"), array.getDataType()));
			}
		} else {
			//MessageManager mm = EngineMessage.get();
			//throw new RQException(mm.getMessage("pdm.arrayTypeError", 
			//		mm.getMessage("DataType.Date"), array.getDataType()));
			ensureCapacity(size + count);
			Date []datas = this.datas;
			
			for (int i = 1; i <= count; ++i) {
				Object obj = array.get(i);
				if (obj instanceof Date) {
					datas[++size] = (Date)obj;
				} else if (obj == null) {
					datas[++size] = null;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("pdm.arrayTypeError", 
							mm.getMessage("DataType.Date"), Variant.getDataType(obj)));
				}
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
		if (array instanceof DateArray) {
			DateArray dateArray = (DateArray)array;
			ensureCapacity(size + count);
			
			System.arraycopy(dateArray.datas, index, datas, size + 1, count);
			size += count;
		} else if (array instanceof ConstArray) {
			Object obj = array.get(1);
			if (obj instanceof Date) {
				ensureCapacity(size + count);
				Date v = (Date)obj;
				Date []datas = this.datas;
				
				for (int i = 0; i < count; ++i) {
					datas[++size] = v;
				}
			} else if (obj == null) {
				ensureCapacity(size + count);
				Date []datas = this.datas;
				
				for (int i = 0; i < count; ++i) {
					datas[++size] = null;
				}
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Date"), array.getDataType()));
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Date"), array.getDataType()));
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(Object []array) {
		for (Object obj : array) {
			if (obj != null && !(obj instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Date"), Variant.getDataType(obj)));
			}
		}
		
		int size2 = array.length;
		ensureCapacity(size + size2);
		Date []datas = this.datas;
		
		for (int i = 0; i < size2; ++i) {
			datas[++size] = (Date)array[i];
		}
	}
	
	/**
	 * ����Ԫ�أ�������Ͳ��������׳��쳣
	 * @param index ����λ�ã���1��ʼ����
	 * @param o Ԫ��ֵ
	 */
	public void insert(int index, Object o) {
		if (o instanceof Date) {
			ensureCapacity(size + 1);
			
			size++;
			System.arraycopy(datas, index, datas, index + 1, size - index);
			datas[index] = (Date)o;
		} else if (o == null) {
			ensureCapacity(size + 1);
			
			size++;
			System.arraycopy(datas, index, datas, index + 1, size - index);
			datas[index] = null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Date"), Variant.getDataType(o)));
		}
	}
	
	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, IArray array) {
		if (array instanceof DateArray) {
			int numNew = array.size();
			DateArray dateArray = (DateArray)array;
			ensureCapacity(size + numNew);
			
			System.arraycopy(datas, pos, datas, pos + numNew, size - pos + 1);
			System.arraycopy(dateArray.datas, 1, datas, pos, numNew);
			
			size += numNew;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Date"), array.getDataType()));
		}
	}
	
	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, Object []array) {
		for (Object obj : array) {
			if (obj != null && !(obj instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Date"), Variant.getDataType(obj)));
			}
		}
		
		int numNew = array.length;
		ensureCapacity(size + numNew);
		
		System.arraycopy(datas, pos, datas, pos + numNew, size - pos + 1);
		System.arraycopy(array, 0, datas, pos, numNew);
		size += numNew;
	}

	public void push(Date date) {
		datas[++size] = date;
	}
	
	public void pushDate(Date date) {
		datas[++size] = date;
	}
	
	/**
	 * ׷��һ���ճ�Ա���������������Ϊ���㹻�ռ���Ԫ�أ�
	 */
	public void pushNull() {
		datas[++size] = null;
	}
	
	/**
	 * ׷��Ԫ�أ��������������Ϊ���㹻�ռ���Ԫ�أ���������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void push(Object o) {
		if (o instanceof Date) {
			datas[++size] = (Date)o;
		} else if (o == null) {
			datas[++size] = null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Date"), Variant.getDataType(o)));
		}
	}
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void push(IArray array, int index) {
		push(array.get(index));
	}
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void add(IArray array, int index) {
		add(array.get(index));
	}
	
	/**
	 * ��array�еĵ�index��Ԫ���������ǰ�����ָ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param curIndex ��ǰ�����Ԫ����������1��ʼ����
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void set(int curIndex, IArray array, int index) {
		set(curIndex, array.get(index));
	}
	
	/**
	 * ȡָ��λ��Ԫ��
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public Object get(int index) {
		return datas[index];
	}
	
	public Date getDate(int index) {
		return datas[index];
	}

	/**
	 * ȡָ��λ��Ԫ�ص�����ֵ
	 * @param index ��������1��ʼ����
	 * @return ������ֵ
	 */
	public int getInt(int index) {
		throw new RuntimeException();
	}
	
	/**
	 * ȡָ��λ��Ԫ�صĳ�����ֵ
	 * @param index ��������1��ʼ����
	 * @return ������ֵ
	 */
	public long getLong(int index) {
		throw new RuntimeException();
	}
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param indexArray λ������
	 * @return IArray
	 */
	public IArray get(int []indexArray) {
		Date []datas = this.datas;
		int len = indexArray.length;
		DateArray result = new DateArray(len);
		
		for (int i : indexArray) {
			result.pushDate(datas[i]);
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
		Date []datas = this.datas;
		int len = end - start + 1;
		Date []resultDatas = new Date[len + 1];
		
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
		
		return new DateArray(resultDatas, len);
	}
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param IArray λ������
	 * @return IArray
	 */
	public IArray get(IArray indexArray) {
		Date []datas = this.datas;
		int len = indexArray.size();
		DateArray result = new DateArray(len);
		
		for (int i = 1; i <= len; ++i) {
			if (indexArray.isNull(i)) {
				result.pushNull();
			} else {
				result.pushDate(datas[indexArray.getInt(i)]);
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
		Date []newDatas = new Date[newSize + 1];
		System.arraycopy(datas, start, newDatas, 1, newSize);
		return new DateArray(newDatas, newSize);
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

			Date []newDatas = new Date[newCapacity];
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
			Date []newDatas = new Date[newLen];
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
		Date []datas = this.datas;
		boolean []resultDatas = new boolean[size + 1];
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = datas[i] != null;
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
		Date []datas = this.datas;
		boolean []resultDatas = new boolean[size + 1];
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = datas[i] == null;
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
		// �ǿ�����true
		return datas[index] != null;
	}
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ���False
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isFalse(int index) {
		// ������false
		return datas[index] == null;
	}

	/**
	 * �Ƿ��Ǽ����������ʱ���������飬��ʱ�����Ŀ��Ա��޸ģ����� f1+f2+f3��ֻ�����һ�������Ž��
	 * @return true������ʱ���������飬false��������ʱ����������
	 */
	public boolean isTemporary() {
		return datas[0] != null;
	}

	/**
	 * �����Ƿ��Ǽ����������ʱ����������
	 * @param ifTemporary true������ʱ���������飬false��������ʱ����������
	 */
	public void setTemporary(boolean ifTemporary) {
		datas[0] = ifTemporary ? TEMP : null;
	}
	
	/**
	 * ɾ�����һ��Ԫ��
	 */
	public void removeLast() {
		datas[size--] = null;
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
		Date []datas = this.datas;
		
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
	 * ��������ķǿ�Ԫ����Ŀ
	 * @return �ǿ�Ԫ����Ŀ
	 */
	public int count() {
		Date []datas = this.datas;
		int size = this.size;
		int count = size;
		
		for (int i = 1; i <= size; ++i) {
			if (datas[i] == null) {
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
		
		Date []datas = this.datas;
		for (int i = 1; i <= size; ++i) {
			if (datas[i] != null) {
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
		Date []datas = this.datas;
		
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
		if (obj instanceof Date) {
			datas[index] = (Date)obj;
		} else if (obj == null) {
			datas[index] = null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Date"), Variant.getDataType(obj)));
		}
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
	 * @return int Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem) {
		if (elem instanceof Date) {
			Date v = (Date)elem;
			Date []datas = this.datas;
			int low = 1, high = size;
			
			while (low <= high) {
				int mid = (low + high) >> 1;
				int cmp = compare(datas[mid], v);
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
			if (size > 0 && datas[1] == null) {
				return 1;
			} else {
				return -1;
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", datas[1], elem,
					getDataType(), Variant.getDataType(elem)));
		}
	}
	
	// ���鰴�������򣬽��н�����ֲ���
	private int descBinarySearch(Date elem) {
		Date []datas = this.datas;
		int low = 1, high = size;
		
		while (low <= high) {
			int mid = (low + high) >> 1;
			int cmp = compare(datas[mid], elem);
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
		if (elem instanceof Date) {
			Date v = (Date)elem;
			Date []datas = this.datas;
			int low = start, high = end;
			
			while (low <= high) {
				int mid = (low + high) >> 1;
				int cmp = compare(datas[mid], v);
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
			if (end > 0 && datas[start] == null) {
				return 1;
			} else {
				return -1;
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", datas[1], elem,
					getDataType(), Variant.getDataType(elem)));
		}
	}
	
	/**
	 * �����б����Ƿ����ָ��Ԫ��
	 * @param elem Object �����ҵ�Ԫ��
	 * @return boolean true��������false��������
	 */
	public boolean contains(Object elem) {
		if (elem instanceof Date) {
			long v = ((Date)elem).getTime();
			Date []datas = this.datas;
			int size = this.size;
			
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null && datas[i].getTime() == v) {
					return true;
				}
			}
			
			return false;
		} else if (elem == null) {
			int size = this.size;
			Date []datas = this.datas;
			for (int i = 1; i <= size; ++i) {
				if (datas[i] == null) {
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
		if (elem instanceof Date) {
			long v = ((Date)elem).getTime();
			Date []datas = this.datas;
			int size = this.size;
			
			for (int i = start; i <= size; ++i) {
				if (datas[i] != null && datas[i].getTime() == v) {
					return i;
				}
			}
			
			return 0;
		} else if (elem == null) {
			int size = this.size;
			Date []datas = this.datas;
			for (int i = start; i <= size; ++i) {
				if (datas[i] == null) {
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
		if (elem instanceof Date) {
			long v = ((Date)elem).getTime();
			Date []datas = this.datas;
			
			for (int i = start; i > 0; --i) {
				if (datas[i] != null && datas[i].getTime() == v) {
					return i;
				}
			}
			
			return 0;
		} else if (elem == null) {
			Date []datas = this.datas;
			for (int i = start; i > 0; --i) {
				if (datas[i] == null) {
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
		Date []datas = this.datas;
		
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
		} else if (!(elem instanceof Date)) {
			return new IntArray(1);
		}

		Date date = (Date)elem;
		if (isSorted) {
			int end = size;
			if (isFromHead) {
				end = start;
				start = 1;
			}
			
			int index = binarySearch(date, start, end);
			if (index < 1) {
				return new IntArray(1);
			}
			
			// �ҵ���һ��
			int first = index;
			while (first > start && compare(datas[first - 1], date) == 0) {
				first--;
			}
			
			// �ҵ����һ��
			int last = index;
			while (last < end && compare(datas[last + 1], date) == 0) {
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
					if (compare(datas[i], date) == 0) {
						result.addInt(i);
					}
				}
			} else {
				for (int i = start; i > 0; --i) {
					if (compare(datas[i], date) == 0) {
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
		MessageManager mm = EngineMessage.get();
		throw new RuntimeException(getDataType() + mm.getMessage("Variant2.illAbs"));
	}

	/**
	 * �������Ա��
	 * @return IArray ��ֵ����
	 */
	public IArray negate() {
		int size = this.size;
		Date []datas = this.datas;
		
		// ����Ҫ�жϳ�Ա�Ƿ���null
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null) {
					datas[i] = Variant.negate(datas[i]);
				}
			}
			
			return this;
		} else {
			Date []newDatas = new Date[size + 1];
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null) {
					newDatas[i] = Variant.negate(datas[i]);
				}
			}
			
			DateArray  result = new DateArray(newDatas, size);
			result.setTemporary(true);
			return result;
		}
	}

	/**
	 * �������Ա���
	 * @return IArray ��ֵ����
	 */
	public IArray not() {
		Date []datas = this.datas;
		int size = this.size;
		
		boolean []newDatas = new boolean[size + 1];
		for (int i = 1; i <= size; ++i) {
			newDatas[i] = datas[i] == null;
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
		if (array instanceof ConstArray) {
			return memberAdd(array.get(1));
		} else if (array instanceof NumberArray) {
			return memberAdd((NumberArray)array);
		} else if (array instanceof ObjectArray) {
			return memberAdd((ObjectArray)array);
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
		if (value instanceof Number) {
			int v = ((Number)value).intValue();
			int size = this.size;
			Date []datas = this.datas;
			Calendar calendar = Calendar.getInstance();
			
			if (isTemporary()) {
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						datas[i] = Variant.dayElapse(calendar, datas[i], v);
					}
				}
				
				return this;
			} else {
				Date []newDatas = new Date[size + 1];				
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						newDatas[i] = Variant.dayElapse(calendar, datas[i], v);
					}
				}
				
				IArray  result = new DateArray(newDatas, size);
				result.setTemporary(true);
				return result;
			}
		} else if (value == null) {
			return this;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					Variant.getDataType(value) + mm.getMessage("Variant2.illAdd"));
		}
	}
	
	protected IArray memberAdd(NumberArray array) {
		int size = this.size;
		Date []datas = this.datas;
		Calendar calendar = Calendar.getInstance();
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null && !array.isNull(i)) {
					datas[i] = Variant.dayElapse(calendar, datas[i], array.getInt(i));
				}
			}
			
			return this;
		} else {
			Date []newDatas = new Date[size + 1];
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null) {
					if (!array.isNull(i)) {
						newDatas[i] = Variant.dayElapse(calendar, datas[i], array.getInt(i));
					} else {
						newDatas[i] = datas[i];
					}
				}
			}
			
			IArray  result = new DateArray(newDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	protected IArray memberAdd(ObjectArray array) {
		if (!array.isNumberArray()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illAdd"));
		}
		
		int size = this.size;
		Date []datas = this.datas;
		Calendar calendar = Calendar.getInstance();
		
		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null && !array.isNull(i)) {
					datas[i] = Variant.dayElapse(calendar, datas[i], array.getInt(i));
				}
			}
			
			return this;
		} else {
			Date []newDatas = new Date[size + 1];
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null) {
					if (!array.isNull(i)) {
						newDatas[i] = Variant.dayElapse(calendar, datas[i], array.getInt(i));
					} else {
						newDatas[i] = datas[i];
					}
				}
			}
			
			IArray  result = new DateArray(newDatas, size);
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
		if (array instanceof ConstArray) {
			return memberSubtract(array.get(1));
		} else if (array instanceof DateArray) {
			return memberSubtract((DateArray)array);
		} else if (array.isNumberArray()) {
			int size = this.size;
			Date []datas = this.datas;
			Calendar calendar = Calendar.getInstance();
			
			if (isTemporary()) {
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null && !array.isNull(i)) {
						datas[i] = Variant.dayElapse(calendar, datas[i], -array.getInt(i));
					}
				}
				
				return this;
			} else {
				Date []newDatas = new Date[size + 1];
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						if (!array.isNull(i)) {
							newDatas[i] = Variant.dayElapse(calendar, datas[i], -array.getInt(i));
						} else {
							newDatas[i] = datas[i];
						}
					}
				}
				
				IArray  result = new DateArray(newDatas, size);
				result.setTemporary(true);
				return result;
			}
		} else if (array instanceof ObjectArray) {
			return memberSubtract((ObjectArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illSubtract"));
		}
	}
	
	private IArray memberSubtract(Object value) {
		if (value == null) {
			return this;
		}

		int size = this.size;
		Date []datas = this.datas;

		if (value instanceof Number) {
			int n = ((Number)value).intValue();
			if (n == 0) {
				return this;
			}
			
			Calendar calendar = Calendar.getInstance();
			if (isTemporary()) {
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						datas[i] = Variant.dayElapse(calendar, datas[i], -n);
					}
				}
				
				return this;
			} else {
				Date []resultDatas = new Date[size + 1];
				for (int i = 1; i <= size; ++i) {
					if (datas[i] != null) {
						resultDatas[i] = Variant.dayElapse(calendar, datas[i], -n);
					}
				}
				
				return new DateArray(resultDatas, size);
			}
		} else if (value instanceof Date) {
			Date date = (Date)value;
			long []resultDatas = new long[size + 1];
			boolean []resultSigns = null;
			
			for (int i = 1; i <= size; ++i) {
				if (datas[i] != null) {
					resultDatas[i] = Variant.dayInterval(date, datas[i]);
				} else {
					if (resultSigns == null) {
						resultSigns = new boolean[size + 1];
					}
					
					resultSigns[i] = true;
				}
			}
			
			LongArray result = new LongArray(resultDatas, resultSigns, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					Variant.getDataType(value) + mm.getMessage("Variant2.illSubtract"));
		}
	}

	private LongArray memberSubtract(DateArray array) {
		int size = this.size;
		Date []d1 = this.datas;
		Date []d2 = array.datas;
		
		long []resultDatas = new long[size + 1];
		boolean []resultSigns = null;
		
		for (int i = 1; i <= size; ++i) {
			if (d1[i] != null && d2[i] != null) {
				resultDatas[i] = Variant.dayInterval(d2[i], d1[i]);
			} else {
				if (resultSigns == null) {
					resultSigns = new boolean[size + 1];
				}
				
				resultSigns[i] = true;
			}
		}
		
		LongArray result = new LongArray(resultDatas, resultSigns, size);
		result.setTemporary(true);
		return result;
	}
	
	private LongArray memberSubtract(ObjectArray array) {
		int size = this.size;
		Date []datas = this.datas;
		Object []d2 = array.getDatas();
		
		long []resultDatas = new long[size + 1];
		boolean []resultSigns = null;
		
		for (int i = 1; i <= size; ++i) {
			if (datas[i] == null || d2[i] == null) {
				if (resultSigns == null) {
					resultSigns = new boolean[size + 1];
				}
				
				resultSigns[i] = true;
			} else if (d2[i] instanceof Date) {
				resultDatas[i] = Variant.dayInterval((Date)d2[i], datas[i]);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
						Variant.getDataType(d2[i]) + mm.getMessage("Variant2.illSubtract"));
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
		if (array instanceof StringArray) {
			return memberDivide((StringArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}
	
	private StringArray memberDivide(StringArray array) {
		int size = this.size;
		Date []d1 = this.datas;
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
		if (array instanceof DateArray) {
			return calcRelation((DateArray)array, relation);
		} else if (array instanceof ConstArray) {
			return calcRelation(array.get(1), relation);
		} else if (array instanceof ObjectArray) {
			return calcRelation((ObjectArray)array, relation);
		} else if (array instanceof BoolArray) {
			return ((BoolArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof IntArray) {
			return ((IntArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof LongArray) {
			return ((LongArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof DoubleArray) {
			return ((DoubleArray)array).calcRelation(this, Relation.getInverseRelation(relation));
		} else if (array instanceof StringArray) {
			return calcRelation((StringArray)array, relation);
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
		if (value instanceof Date) {
			return calcRelation((Date)value, relation);
		} else if (value == null) {
			return ArrayUtil.calcRelationNull(datas, size, relation);
		} else {
			boolean b = Variant.isTrue(value);
			int size = this.size;
			Date []datas = this.datas;
			
			if (relation == Relation.AND) {
				BoolArray result;
				if (!b) {
					result = new BoolArray(false, size);
				} else {
					boolean []resultDatas = new boolean[size + 1];
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = datas[i] != null;
					}
					
					result = new BoolArray(resultDatas, size);
				}
				
				result.setTemporary(true);
				return result;
			} else if (relation == Relation.OR) {
				BoolArray result;
				if (b) {
					result = new BoolArray(true, size);
				} else {
					boolean []resultDatas = new boolean[size + 1];
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = datas[i] != null;
					}
					
					result = new BoolArray(resultDatas, size);
				}
				
				result.setTemporary(true);
				return result;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Variant2.illCompare", get(1), value,
						getDataType(), Variant.getDataType(value)));
			}
		}
	}
	
	private BoolArray calcRelation(Date value, int relation) {
		int size = this.size;
		Date []d1 = this.datas;
		boolean []resultDatas = new boolean[size + 1];
		long time = value.getTime();
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], time) == 0;
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], time) > 0;
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], time) >= 0;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], time) < 0;
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], time) <= 0;
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], time) != 0;
			}
		} else if (relation == Relation.AND) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = d1[i] != null;
			}
		} else { // Relation.OR
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = true;
			}
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}
	
	private BoolArray calcRelation(DateArray array, int relation) {
		int size = this.size;
		Date []d1 = this.datas;
		Date []d2 = array.datas;
		boolean []resultDatas = new boolean[size + 1];
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) == 0;
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) > 0;
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) >= 0;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) < 0;
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) <= 0;
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) != 0;
			}
		} else if (relation == Relation.AND) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = d1[i] != null && d2[i] != null;
			}
		} else { // Relation.OR
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = d1[i] != null || d2[i] != null;
			}
		}
		
		BoolArray result = new BoolArray(resultDatas, size);
		result.setTemporary(true);
		return result;
	}
	
	protected BoolArray calcRelation(StringArray array, int relation) {
		Date []d1 = this.datas;
		String []d2 = array.getDatas();
		
		if (relation == Relation.AND) {
			boolean []resultDatas = new boolean[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = d1[i] != null && d2[i] != null;
			}
			
			BoolArray result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (relation == Relation.OR) {
			boolean []resultDatas = new boolean[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = d1[i] != null || d2[i] != null;
			}
			
			BoolArray result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), array.get(1),
					getDataType(), array.getDataType()));
		}
	}
	
	protected BoolArray calcRelation(ObjectArray array, int relation) {
		int size = this.size;
		Date []d1 = this.datas;
		Object []d2 = array.getDatas();
		boolean []resultDatas = new boolean[size + 1];
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) == 0;
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) > 0;
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) >= 0;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) < 0;
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) <= 0;
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = compare(d1[i], d2[i]) != 0;
			}
		} else if (relation == Relation.AND) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = d1[i] != null && Variant.isTrue(d2[i]);
			}
		} else { // Relation.OR
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = d1[i] != null || Variant.isTrue(d2[i]);
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
		int size1 = this.size;
		int size2 = array.size();
		Date []d1 = this.datas;
		
		int size = size1;
		int result = 0;
		if (size1 < size2) {
			result = -1;
		} else if (size1 > size2) {
			result = 1;
			size = size2;
		}

		if (array instanceof DateArray) {
			DateArray array2 = (DateArray)array;
			Date []d2 = array2.datas;
			
			for (int i = 1; i <= size; ++i) {
				int cmp = compare(d1[i], d2[i]);
				if (cmp != 0) {
					return cmp;
				}
			}
		} else if (array instanceof ConstArray) {
			Object value = array.get(1);
			if (value instanceof Date) {
				Date d2 = (Date)value;
				for (int i = 1; i <= size; ++i) {
					int cmp = compare(d1[i], d2);
					if (cmp != 0) {
						return cmp;
					}
				}
			} else if (value == null) {
				for (int i = 1; i <= size; ++i) {
					if (d1[i] != null) {
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
				int cmp = compare(d1[i], d2[i]);
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
	 * ���������2����Ա�ıȽ�ֵ
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	public int memberCompare(int index1, int index2) {
		return compare(datas[index1], datas[index2]);
	}
	
	/**
	 * �ж������������Ա�Ƿ����
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	public boolean isMemberEquals(int index1, int index2) {
		if (datas[index1] == null) {
			return datas[index2] == null;
		} else if (datas[index2] == null) {
			return false;
		} else {
			return datas[index1].getTime() == datas[index2].getTime();
		}
	}
	
	/**
	 * �ж����������ָ��Ԫ���Ƿ���ͬ
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ�Ƚϵ�����
	 * @param index Ҫ�Ƚϵ������Ԫ�ص�����
	 * @return true����ͬ��false������ͬ
	 */
	public boolean isEquals(int curIndex, IArray array, int index) {
		Object value = array.get(index);
		if (value instanceof Date) {
			return ((Date)value).equals(datas[curIndex]);
		} else if (value == null) {
			return datas[curIndex] == null;
		} else {
			return false;
		}
	}
	
	/**
	 * �ж������ָ��Ԫ���Ƿ������ֵ���
	 * @param curIndex ����Ԫ����������1��ʼ����
	 * @param value ֵ
	 * @return true����ȣ�false�������
	 */
	public boolean isEquals(int curIndex, Object value) {
		if (value instanceof Date) {
			return ((Date)value).equals(datas[curIndex]);
		} else if (value == null) {
			return datas[curIndex] == null;
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
		return compare(datas[curIndex], array.get(index));
	}
	
	/**
	 * �Ƚ������ָ��Ԫ�������ֵ�Ĵ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param value Ҫ�Ƚϵ�ֵ
	 * @return
	 */
	public int compareTo(int curIndex, Object value) {
		return compare(datas[curIndex], value);
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

		Date []datas = this.datas;
		Date max = null;
		
		int i = 1;
		for (; i <= size; ++i) {
			if (datas[i] != null) {
				max = datas[i];
				break;
			}
		}
		
		for (++i; i <= size; ++i) {
			if (datas[i] != null && max.getTime() < datas[i].getTime()) {
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

		Date []datas = this.datas;
		Date min = null;
		
		int i = 1;
		for (; i <= size; ++i) {
			if (datas[i] != null) {
				min = datas[i];
				break;
			}
		}
		
		for (++i; i <= size; ++i) {
			if (datas[i] != null && min.getTime() > datas[i].getTime()) {
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
		if (array instanceof DateArray) {
			calcRelations((DateArray)array, relation, result, isAnd);
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
		if (value instanceof Date) {
			calcRelations((Date)value, relation, result, isAnd);
		} else if (value == null) {
			ArrayUtil.calcRelationsNull(datas, size, relation, result, isAnd);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), value,
					getDataType(), Variant.getDataType(value)));
		}
	}

	private void calcRelations(Date value, int relation, BoolArray result, boolean isAnd) {
		int size = this.size;
		Date []d1 = this.datas;
		boolean []resultDatas = result.getDatas();
		long time = value.getTime();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], time) != 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], time) <= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], time) < 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], time) >= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], time) > 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], time) == 0) {
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
					if (!resultDatas[i] && compare(d1[i], time) == 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], time) > 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], time) >= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], time) < 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], time) <= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], time) != 0) {
						resultDatas[i] = true;
					}
				}
			} else {
				throw new RuntimeException();
			}
		}
	}

	private void calcRelations(DateArray array, int relation, BoolArray result, boolean isAnd) {
		int size = this.size;
		Date []d1 = this.datas;
		Date []d2 = array.datas;
		boolean []resultDatas = result.getDatas();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) != 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) <= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) < 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) >= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) > 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) == 0) {
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
					if (!resultDatas[i] && compare(d1[i], d2[i]) == 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) > 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) >= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) < 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) <= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) != 0) {
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
		Date []d1 = this.datas;
		Object []d2 = array.getDatas();
		boolean []resultDatas = result.getDatas();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) != 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) <= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) < 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) >= 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) > 0) {
						resultDatas[i] = false;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (resultDatas[i] && compare(d1[i], d2[i]) == 0) {
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
					if (!resultDatas[i] && compare(d1[i], d2[i]) == 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) > 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) >= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) < 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) <= 0) {
						resultDatas[i] = true;
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				for (int i = 1; i <= size; ++i) {
					if (!resultDatas[i] && compare(d1[i], d2[i]) != 0) {
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
	 * ȡ����ʶ����ȡֵΪ����ж�Ӧ�����ݣ����������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	public IArray select(IArray signArray) {
		int size = signArray.size();
		Date []d1 = this.datas;
		Date []resultDatas = new Date[size + 1];
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
		
		return new DateArray(resultDatas, count);
	}
	
	/**
	 * ȡĳһ���α�ʶ����ȡֵΪ��������������
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã���������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	public IArray select(int start, int end, IArray signArray) {
		Date []d1 = this.datas;
		Date []resultDatas = new Date[end - start + 1];
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
		
		return new DateArray(resultDatas, count);
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
		Date []datas = this.datas;
		int size = this.size;
		int resultSize = size - pos + 1;
		Date []resultDatas = new Date[resultSize + 1];
		System.arraycopy(datas, pos, resultDatas, 1, resultSize);
		
		for (int i = pos; i <= size; ++i) {
			datas[i] = null;
		}
		
		this.size = pos - 1;
		return new DateArray(resultDatas, resultSize);
	}
	
	/**
	 * ��ָ������Ԫ�ط���������������
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 * @return
	 */
	public IArray split(int from, int to) {
		Date []datas = this.datas;
		int oldSize = this.size;
		int resultSize = to - from + 1;
		Date []resultDatas = new Date[resultSize + 1];
		System.arraycopy(datas, from, resultDatas, 1, resultSize);
		
		System.arraycopy(datas, to + 1, datas, from, oldSize - to);
		this.size -= resultSize;
		
		for (int i = this.size + 1; i <= oldSize; ++i) {
			datas[i] = null;
		}
		
		return new DateArray(resultDatas, resultSize);
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
		Date []datas = this.datas;
		Date []resultDatas = new Date[size + 1];
		
		for (int i = 1, q = size; i <= size; ++i) {
			resultDatas[i] = datas[q--];
		}
		
		return new DateArray(resultDatas, size);
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
		
		Date []datas = this.datas;
		if (ignoreNull) {
			if (count == 1) {
				// ȡ��Сֵ��λ��
				Date minValue = null;
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
							int cmp = compare(datas[i], minValue);
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
						if (datas[i] != null && compare(datas[i], minValue) < 0) {
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
						if (datas[i] != null && compare(datas[i], minValue) < 0) {
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
				DateArray valueArray = new DateArray(next);
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
				Date maxValue = null;
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
							int cmp = compare(datas[i], maxValue);
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
						if (datas[i] != null && compare(datas[i], maxValue) > 0) {
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
						if (datas[i] != null && compare(datas[i], maxValue) > 0) {
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
				DateArray valueArray = new DateArray(next);
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
					Date minValue = datas[1];
					
					for (int i = 2; i <= size; ++i) {
						int cmp = compare(datas[i], minValue);
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
					Date minValue = datas[size];
					int pos = size;
					
					for (int i = size - 1; i > 0; --i) {
						if (compare(datas[i], minValue) < 0) {
							minValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					result.pushInt(pos);
					return result;
				} else {
					Date minValue = datas[1];
					int pos = 1;
					
					for (int i = 2; i <= size; ++i) {
						if (compare(datas[i], minValue) < 0) {
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
				DateArray valueArray = new DateArray(next);
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
					Date maxValue = datas[1];
					result.addInt(1);
					
					for (int i = 2; i <= size; ++i) {
						int cmp = compare(datas[i], maxValue);
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
					Date maxValue = datas[size];
					int pos = size;
					
					for (int i = size - 1; i > 0; --i) {
						if (compare(datas[i], maxValue) > 0) {
							maxValue = datas[i];
							pos = i;
						}
					}
					
					IntArray result = new IntArray(1);
					result.pushInt(pos);
					return result;
				} else {
					Date maxValue = datas[1];
					int pos = 1;
					
					for (int i = 2; i <= size; ++i) {
						if (compare(datas[i], maxValue) > 0) {
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
				DateArray valueArray = new DateArray(next);
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
		
		Date []datas = this.datas;		
		if (count > 0) {
			// ȡ��С��count��Ԫ�ص�λ��
			int next = count + 1;
			DateArray valueArray = new DateArray(next);
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
									long value = valueArray.getDate(curSize).getTime();
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize - 1; j >= count; --j) {
										if (valueArray.getDate(j).getTime() == value) {
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
								long value = valueArray.getDate(curSize).getTime();
								valueArray.removeLast();
								posArray.removeLast();
								for (int j = curSize - 1; j >= count; --j) {
									if (valueArray.getDate(j).getTime() == value) {
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
									long value = valueArray.getDate(curSize + 1).getTime();
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize; j > count; --j) {
										if (valueArray.getDate(j).getTime() == value) {
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
									long value = valueArray.getDate(curSize + 1).getTime();
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize; j > count; --j) {
										if (valueArray.getDate(j).getTime() == value) {
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
			DateArray valueArray = new DateArray(next);
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
									long value = valueArray.getDate(curSize).getTime();
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize - 1; j >= count; --j) {
										if (valueArray.getDate(j).getTime() == value) {
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
									long value = valueArray.getDate(curSize + 1).getTime();
									valueArray.removeLast();
									posArray.removeLast();
									for (int j = curSize; j > count; --j) {
										if (valueArray.getDate(j).getTime() == value) {
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
		Object []resultDatas = new Object[size + 1];
		System.arraycopy(datas, 1, resultDatas, 1, size);
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
		Date []datas = this.datas;
		
		if (other instanceof DateArray) {
			Date []otherDatas = ((DateArray)other).getDatas();
			
			if (isTemporary()) {
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						datas[i] = otherDatas[i];
					}
				}
				
				return this;
			} else {
				Date []resultDatas = new Date[size + 1];
				System.arraycopy(datas, 1, resultDatas, 1, size);
				
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						resultDatas[i] = otherDatas[i];
					}
				}
				
				IArray result = new DateArray(resultDatas, size);
				result.setTemporary(true);
				return result;
			}
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
		Date []datas = this.datas;
		
		if (value instanceof Date || value == null) {
			Date date = (Date)value;
			if (isTemporary()) {
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						datas[i] = date;
					}
				}
				
				return this;
			} else {
				Date []resultDatas = new Date[size + 1];
				System.arraycopy(datas, 1, resultDatas, 1, size);
				
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						resultDatas[i] = date;
					}
				}
				
				IArray result = new DateArray(resultDatas, size);
				result.setTemporary(true);
				return result;
			}
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
}
