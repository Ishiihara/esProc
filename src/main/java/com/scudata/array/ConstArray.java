package com.scudata.array;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Sequence;
import com.scudata.expression.Relation;
import com.scudata.expression.fn.math.And;
import com.scudata.expression.fn.math.Bit1;
import com.scudata.expression.fn.math.Or;
import com.scudata.expression.fn.math.Xor;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ��������
 * @author LW
 *
 */
public class ConstArray implements IArray {
	private static final long serialVersionUID = 1L;

	private Object data;
	private int size;

	// ���������л�
	public ConstArray() {
	}
	
	public ConstArray(Object data, int size) {
		this.data = data;
		this.size = size;
	}
	
	/**
	 * ȡ��������ʹ������ڴ�����Ϣ��ʾ
	 * @return ���ʹ�
	 */
	public String getDataType() {
		return Variant.getDataType(data);
	}
	
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	/**
	 * ׷��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void add(Object o) {
		if (Variant.isEquals(data, o)) {
			size++;
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(IArray array) {
		if (array.size() == 0) {
			return;
		}
		
		if (array instanceof ConstArray && Variant.isEquals(data, array.get(1))) {
			size += array.size();
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 * @param count Ԫ�ظ���
	 */
	public void addAll(IArray array, int count) {
		if (count == 0) {
			return;
		}
		
		if (array instanceof ConstArray && Variant.isEquals(data, array.get(1))) {
			size += count;
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 * @param index Ҫ��������ݵ���ʼλ��
	 * @param count ����
	 */
	public void addAll(IArray array, int index, int count) {
		if (array instanceof ConstArray && Variant.isEquals(data, array.get(1))) {
			size += count;
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(Object []array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	/**
	 * ����Ԫ�أ�������Ͳ��������׳��쳣
	 * @param index ����λ�ã���1��ʼ����
	 * @param o Ԫ��ֵ
	 */
	public void insert(int index, Object o) {
		if (Variant.isEquals(data, o)) {
			size++;
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}

	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, IArray array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, Object []array) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}

	/**
	 * ׷��Ԫ�أ��������������Ϊ���㹻�ռ���Ԫ�أ���������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void push(Object o) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	/**
	 * ׷��һ���ճ�Ա���������������Ϊ���㹻�ռ���Ԫ�أ�
	 */
	public void pushNull() {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void push(IArray array, int index) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void add(IArray array, int index) {
		if (Variant.isEquals(data, array.get(index))) {
			size++;
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	/**
	 * ��array�еĵ�index��Ԫ���������ǰ�����ָ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param curIndex ��ǰ�����Ԫ����������1��ʼ����
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void set(int curIndex, IArray array, int index) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}	
	
	/**
	 * ȡָ��λ��Ԫ��
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public Object get(int index) {
		return data;
	}

	/**
	 * ȡָ��λ��Ԫ�ص�����ֵ
	 * @param index ��������1��ʼ����
	 * @return ����ֵ
	 */
	public int getInt(int index) {
		return ((Number)data).intValue();
	}
	
	/**
	 * ȡָ��λ��Ԫ�صĳ�����ֵ
	 * @param index ��������1��ʼ����
	 * @return ������ֵ
	 */
	public long getLong(int index) {
		return ((Number)data).longValue();
	}
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param indexArray λ������
	 * @return IArray
	 */
	public IArray get(int []indexArray) {
		return new ConstArray(data, indexArray.length);
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
		int len = end - start + 1;
		Object data = this.data;
		
		if (doCheck && data != null) {
			Object []resultDatas = new Object[len + 1];
			for (int i = 1; start <= end; ++start, ++i) {
				int q = indexArray[start];
				if (q > 0) {
					resultDatas[i] = data;
				}
			}
			
			return new ObjectArray(resultDatas, len);
		} else {
			return new ConstArray(data, len);
		}
	}
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param IArray λ������
	 * @return IArray
	 */
	public IArray get(IArray indexArray) {
		Object data = this.data;
		int len = indexArray.size();
		ObjectArray result = new ObjectArray(len);
		
		for (int i = 1; i <= len; ++i) {
			if (indexArray.isNull(i)) {
				result.pushNull();
			} else {
				result.push(data);
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
		return new ConstArray(data, end - start);
	}
	
	/**
	 * ʹ�б��������С��minCapacity
	 * @param minCapacity ��С����
	 */
	public void ensureCapacity(int minCapacity) {
	}
	
	/**
	 * ����������ʹ����Ԫ�������
	 */
	public void trimToSize() {
	}
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ��ǿ�
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isNull(int index) {
		return data == null;
	}
	
	/**
	 * �ж�Ԫ���Ƿ���True
	 * @return BoolArray
	 */
	public BoolArray isTrue() {
		int size = this.size;
		boolean []resultDatas = new boolean[size + 1];
		boolean value = Variant.isTrue(data);
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = value;
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
		boolean []resultDatas = new boolean[size + 1];
		boolean value = Variant.isFalse(data);
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = value;
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
		return Variant.isTrue(data);
	}
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ���False
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isFalse(int index) {
		return Variant.isFalse(data);
	}

	/**
	 * �Ƿ��Ǽ����������ʱ���������飬��ʱ�����Ŀ��Ա��޸ģ����� f1+f2+f3��ֻ�����һ�������Ž��
	 * @return true������ʱ���������飬false��������ʱ����������
	 */
	public boolean isTemporary() {
		return false;
	}

	/**
	 * �����Ƿ��Ǽ����������ʱ����������
	 * @param ifTemporary true������ʱ���������飬false��������ʱ����������
	 */
	public void setTemporary(boolean ifTemporary) {
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
		size--;
	}
	
	/**
	 * ɾ��ָ�������ڵ�Ԫ��
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 */
	public void removeRange(int fromIndex, int toIndex) {
		size -= (toIndex - fromIndex + 1);
	}
	
	/**
	 * ɾ��ָ��λ�õ�Ԫ�أ���Ŵ�С��������
	 * @param seqs ��������
	 */
	public void remove(int []seqs) {
		size -= seqs.length;
	}
	
	/**
	 * ����ָ�������ڵ�����
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã�������
	 */
	public void reserve(int start, int end) {
		size = end - start + 1;
	}

	public int size() {
		return size;
	}
	
	/**
	 * ��������ķǿ�Ԫ����Ŀ
	 * @return �ǿ�Ԫ����Ŀ
	 */
	public int count() {
		return data != null ? size : 0;
	}
	
	/**
	 * �ж������Ƿ���ȡֵΪtrue��Ԫ��
	 * @return true���У�false��û��
	 */
	public boolean containTrue() {
		return Variant.isTrue(data);
	}
	
	/**
	 * ���ص�һ����Ϊ�յ�Ԫ��
	 * @return Object
	 */
	public Object ifn() {
		return data;
	}

	/**
	 * �޸�����ָ��Ԫ�ص�ֵ��������Ͳ��������׳��쳣
	 * @param index ��������1��ʼ����
	 * @param obj ֵ
	 */
	public void set(int index, Object obj) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("pdm.modifyConstArrayError"));
	}
	
	/**
	 * ɾ�����е�Ԫ��
	 */
	public void clear() {
		data = null;
		size = 0;
	}

	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @return int Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem) {
		if (size == 0) {
			return -1;
		}
		
		int cmp = Variant.compare(data, elem, true);
		if (cmp == 0) {
			return 1;
		} else if (cmp < 0) {
			return -1;
		} else {
			return -size - 1;
		}
	}
	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @param start ��ʼ����λ�ã�������
	 * @param end ��������λ�ã�������
	 * @return Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem, int start, int end) {
		if (end == 0) {
			return -1;
		}
		
		int cmp = Variant.compare(data, elem, true);
		if (cmp == 0) {
			return start;
		} else if (cmp < 0) {
			return -1;
		} else {
			return -size - 1;
		}
	}
	
	/**
	 * �����б����Ƿ����ָ��Ԫ��
	 * @param elem Object �����ҵ�Ԫ��
	 * @return boolean true��������false��������
	 */
	public boolean contains(Object elem) {
		if (size == 0) {
			return false;
		}
		
		return Variant.isEquals(data, elem);
	}
	/**
	 * �ж������Ԫ���Ƿ��ڵ�ǰ������
	 * @param isSorted ��ǰ�����Ƿ�����
	 * @param array ����
	 * @param result ���ڴ�Ž����ֻ��ȡֵΪtrue��
	 */
	public void contains(boolean isSorted, IArray array, BoolArray result) {
		int resultSize = result.size();
		if (size > 0) {
			Object data = this.data;
			for (int i = 1; i <= resultSize; ++i) {
				if (result.isTrue(i) && !Variant.isEquals(data, array.get(i))) {
					result.set(i, false);
				}
			}
		} else {
			for (int i = 1; i <= resultSize; ++i) {
				result.set(i, false);
			}
		}
	}
	
	/**
	 * �����б����Ƿ����ָ��Ԫ�أ�ʹ�õȺűȽ�
	 * @param elem
	 * @return boolean true��������false��������
	 */
	public boolean objectContains(Object elem) {
		return data == elem;
	}
	
	/**
	 * ����Ԫ�����������״γ��ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start ��ʼ����λ�ã�������
	 * @return ���Ԫ�ش����򷵻�ֵ����0�����򷵻�0
	 */
	public int firstIndexOf(Object elem, int start) {
		if (size == 0) {
			return 0;
		}
		
		return Variant.isEquals(data, elem) ? start : 0;
	}
	
	/**
	 * ����Ԫ���������������ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start �Ӻ��濪ʼ���ҵ�λ�ã�������
	 * @return ���Ԫ�ش����򷵻�ֵ����0�����򷵻�0
	 */
	public int lastIndexOf(Object elem, int start) {
		if (size == 0) {
			return 0;
		}
		
		return Variant.isEquals(data, elem) ? start : 0;
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
		if (size > 0 && Variant.isEquals(data, elem)) {
			int end = size;
			if (!isFromHead) {
				end = start;
				start = 1;
			}
			
			IntArray result = new IntArray(end - start + 1);
			if (isFromHead) {
				for (; start <= end; ++start) {
					result.pushInt(start);
				}
			} else {
				for (; end >= start; --end) {
					result.pushInt(end);
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
		return new ConstArray(data, size);
	}
	
	/**
	 * д���ݵ���
	 * @param out �����
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(1);
		out.writeInt(size);
		out.writeObject(data);
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
		data = in.readObject();
	}
	
	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeByte(1);
		out.writeInt(size);
		out.writeObject(data, true);
		return out.toByteArray();
	}
	
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		in.readByte();
		size = in.readInt();
		data = in.readObject(true);
	}
	
	/**
	 * ����һ��ͬ���͵�����
	 * @param count
	 * @return
	 */
	public IArray newInstance(int count) {
		if (data instanceof Integer) {
			return new IntArray(count);
		} else if (data instanceof Long) {
			return new LongArray(count);
		} else if (data instanceof Double) {
			return new DoubleArray(count);
		} else if (data instanceof String) {
			return new StringArray(count);
		} else if (data instanceof Date) {
			return new DateArray(count);
		} else if (data instanceof Boolean) {
			return new BoolArray(count);
		} else {
			return new ObjectArray(count);
		}
	}

	/**
	 * �������Ա�����ֵ
	 * @return IArray ����ֵ����
	 */
	public IArray abs() {
		if (data == null) {
			return this;
		} else {
			return new ConstArray(Variant.abs(data), size);
		}
	}
	
	/**
	 * �������Ա��
	 * @return IArray ��ֵ����
	 */
	public IArray negate() {
		if (data == null) {
			return this;
		} else {
			return new ConstArray(Variant.negate(data), size);
		}
	}

	/**
	 * �������Ա���
	 * @return IArray ��ֵ����
	 */
	public IArray not() {
		Boolean b = Boolean.valueOf(Variant.isFalse(data));
		return new ConstArray(b, size);
	}

	/**
	 * �ж�����ĳ�Ա�Ƿ����������԰���null��
	 * @return true����������false�����з�����ֵ
	 */
	public boolean isNumberArray() {
		return data == null || data instanceof Number;
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�ĺ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberAdd(IArray array) {
		return array.memberAdd(data);
	}

	/**
	 * ��������ĳ�Ա��ָ�������ĺ�
	 * @param value ����
	 * @return ������
	 */
	public IArray memberAdd(Object value) {
		value = Variant.add(data, value);
		return new ConstArray(value, size);
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
			Object value = array.get(1);
			value = Variant.subtract(data, value);
			return new ConstArray(value, size);
		} else if (array instanceof ObjectArray) {
			return memberSubtract((ObjectArray)array);
		} else if (array instanceof DateArray) {
			return memberSubtract((DateArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illSubtract"));
		}
	}
	
	private IArray memberSubtract(IntArray array) {
		int size = this.size;
		int []datas = array.getDatas();
		boolean []signs = array.getSigns();
		
		if (data instanceof Long) {
			long v = ((Long)data).longValue();
			long []resultDatas = new long[size + 1];
			
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v - datas[i];
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (signs[i]) {
						resultDatas[i] = v;
					} else {
						resultDatas[i] = v - datas[i];
					}
				}
			}
			
			IArray result = new LongArray(resultDatas, null, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Double || data instanceof Float) {
			double v = ((Number)data).doubleValue();
			double []resultDatas = new double[size + 1];
			
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v - datas[i];
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (signs[i]) {
						resultDatas[i] = v;
					} else {
						resultDatas[i] = v - datas[i];
					}
				}
			}
			
			IArray result = new DoubleArray(resultDatas, null, size);
			result.setTemporary(true);
			return result;
		} else if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigDecimal decimal;
			if (data instanceof BigDecimal) {
				decimal = (BigDecimal)data;
			} else {
				decimal = new BigDecimal((BigInteger)data);
			}
			
			Object []resultDatas = new Object[size + 1];
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = decimal.subtract(new BigDecimal(datas[i]));
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (signs[i]) {
						resultDatas[i] = decimal;
					} else {
						resultDatas[i] = decimal.subtract(new BigDecimal(datas[i]));
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			int v = ((Number)data).intValue();
			if (array.isTemporary()) {
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						datas[i] = v - datas[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							datas[i] = v;
						} else {
							datas[i] = v - datas[i];
						}
					}
					
					array.setSigns(null);
				}
				
				return array;
			} else {
				int []resultDatas = new int[size + 1];
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = v - datas[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							resultDatas[i] = v;
						} else {
							resultDatas[i] = v - datas[i];
						}
					}
				}
				
				IArray result = new IntArray(resultDatas, null, size);
				result.setTemporary(true);
				return result;
			}
		} else if (data instanceof Date) {
			Date date = (Date)data;
			long time = date.getTime();
			Calendar calendar = Calendar.getInstance();
			Object []resultDatas = new Object[size + 1];
			
			for (int i = 1; i <= size; ++i) {
				if (signs == null || !signs[i]) {
					calendar.setTimeInMillis(time);
					calendar.add(Calendar.DATE, -datas[i]);
					Date resultDate = (Date)date.clone();
					resultDate.setTime(calendar.getTimeInMillis());
					resultDatas[i] = resultDate;
				} else {
					resultDatas[i] = date;
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data == null) {
			return array.negate();
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illSubtract"));
		}
	}
	
	private IArray memberSubtract(LongArray array) {
		int size = this.size;
		long []datas = array.getDatas();
		boolean []signs = array.getSigns();
		
		if (data instanceof Double || data instanceof Float) {
			double v = ((Number)data).doubleValue();
			double []resultDatas = new double[size + 1];
			
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v - datas[i];
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (signs[i]) {
						resultDatas[i] = v;
					} else {
						resultDatas[i] = v - datas[i];
					}
				}
			}
			
			IArray result = new DoubleArray(resultDatas, null, size);
			result.setTemporary(true);
			return result;
		} else if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigDecimal decimal;
			if (data instanceof BigDecimal) {
				decimal = (BigDecimal)data;
			} else {
				decimal = new BigDecimal((BigInteger)data);
			}
			
			Object []resultDatas = new Object[size + 1];
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = decimal.subtract(new BigDecimal(datas[i]));
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (signs[i]) {
						resultDatas[i] = decimal;
					} else {
						resultDatas[i] = decimal.subtract(new BigDecimal(datas[i]));
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			long v = ((Number)data).longValue();
			if (array.isTemporary()) {
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						datas[i] = v - datas[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							datas[i] = v;
						} else {
							datas[i] = v - datas[i];
						}
					}
					
					array.setSigns(null);
				}
				
				return array;
			} else {
				long []resultDatas = new long[size + 1];
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = v - datas[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							resultDatas[i] = v;
						} else {
							resultDatas[i] = v - datas[i];
						}
					}
				}
				
				IArray result = new LongArray(resultDatas, null, size);
				result.setTemporary(true);
				return result;
			}
		} else if (data instanceof Date) {
			Date date = (Date)data;
			long time = date.getTime();
			Calendar calendar = Calendar.getInstance();
			Object []resultDatas = new Object[size + 1];
			
			for (int i = 1; i <= size; ++i) {
				if (signs == null || !signs[i]) {
					calendar.setTimeInMillis(time);
					calendar.add(Calendar.DATE, -(int)datas[i]);
					Date resultDate = (Date)date.clone();
					resultDate.setTime(calendar.getTimeInMillis());
					resultDatas[i] = resultDate;
				} else {
					resultDatas[i] = date;
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data == null) {
			return array.negate();
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illSubtract"));
		}
	}

	private IArray memberSubtract(DoubleArray array) {
		int size = this.size;
		double []datas = array.getDatas();
		boolean []signs = array.getSigns();
		
		if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigDecimal decimal;
			if (data instanceof BigDecimal) {
				decimal = (BigDecimal)data;
			} else {
				decimal = new BigDecimal((BigInteger)data);
			}
			
			Object []resultDatas = new Object[size + 1];
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = decimal.subtract(new BigDecimal(datas[i]));
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (signs[i]) {
						resultDatas[i] = decimal;
					} else {
						resultDatas[i] = decimal.subtract(new BigDecimal(datas[i]));
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			double v = ((Number)data).doubleValue();
			if (array.isTemporary()) {
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						datas[i] = v - datas[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							datas[i] = v;
						} else {
							datas[i] = v - datas[i];
						}
					}
					
					array.setSigns(null);
				}
				
				return array;
			} else {
				double []resultDatas = new double[size + 1];
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = v - datas[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (signs[i]) {
							resultDatas[i] = v;
						} else {
							resultDatas[i] = v - datas[i];
						}
					}
				}
				
				IArray result = new DoubleArray(resultDatas, null, size);
				result.setTemporary(true);
				return result;
			}
		} else if (data instanceof Date) {
			Date date = (Date)data;
			long time = date.getTime();
			Calendar calendar = Calendar.getInstance();
			Object []resultDatas = new Object[size + 1];
			
			for (int i = 1; i <= size; ++i) {
				if (signs == null || !signs[i]) {
					calendar.setTimeInMillis(time);
					calendar.add(Calendar.DATE, -(int)datas[i]);
					Date resultDate = (Date)date.clone();
					resultDate.setTime(calendar.getTimeInMillis());
					resultDatas[i] = resultDate;
				} else {
					resultDatas[i] = date;
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data == null) {
			return array.negate();
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illSubtract"));
		}
	}
	
	private IArray memberSubtract(DateArray array) {
		if (data instanceof Date) {
			int size = this.size;
			Date date = (Date)data;
			Date []datas = array.getDatas();
			
			long []resultDatas = new long[size + 1];
			boolean []resultSigns = null;
			for (int i = 1; i <= size; ++i) {
				if (datas[i] == null) {
					if (resultSigns == null) {
						resultSigns = new boolean[size + 1];
					}
					
					resultSigns[i] = true;
				} else {
					resultDatas[i] = Variant.dayInterval(datas[i], date);
				}
			}
			
			IArray result = new LongArray(resultDatas, resultSigns, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illSubtract"));
		}
	}

	private ObjectArray memberSubtract(ObjectArray array) {
		Object data = this.data;
		int size = this.size;
		Object []datas = array.getDatas();
		
		if (array.isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = Variant.subtract(data, datas[i]);
			}
			
			return array;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.subtract(data, datas[i]);
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ļ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	public IArray memberMultiply(IArray array) {
		return array.memberMultiply(data);
	}

	/**
	 * ��������ĳ�Ա��ָ�������Ļ�
	 * @param value ����
	 * @return ������
	 */
	public IArray memberMultiply(Object value) {
		value = Variant.multiply(data, value);
		return new ConstArray(value, size);
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
			Object value = array.get(1);
			value = Variant.divide(data, value);
			return new ConstArray(value, size);
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
	
	private StringArray memberDivide(StringArray array) {
		if (data == null) {
			return array;
		}
		
		String str = data.toString();
		int size = this.size;
		String []d2 = array.getDatas();

		if (isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				if (d2[i] != null) {
					d2[i] = str + d2[i];
				} else {
					d2[i] = str;
				}
			}
			
			return array;
		} else {
			String []resultDatas = new String[size + 1];
			for (int i = 1; i <= size; ++i) {
				if (d2[i] != null) {
					resultDatas[i] = str + d2[i];
				} else {
					resultDatas[i] = str;
				}
			}
			
			StringArray result = new StringArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	private IArray memberDivide(IntArray array) {
		Object data = this.data;
		int size = this.size;
		int []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		
		if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigDecimal decimal;
			if (data instanceof BigDecimal) {
				decimal = (BigDecimal)data;
			} else {
				decimal = new BigDecimal((BigInteger)data);
			}
			
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = decimal.divide(new BigDecimal(d2[i]), Variant.Divide_Scale, Variant.Divide_Round);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = decimal.divide(new BigDecimal(d2[i]), Variant.Divide_Scale, Variant.Divide_Round);
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			double v = ((Number)data).doubleValue();
			double []resultDatas = new double[size + 1];
			boolean []resultSigns = null;
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v / (double)d2[i];
				}
			} else {
				resultSigns = new boolean[size + 1];
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultSigns[i] = true;
					} else {
						resultDatas[i] = v / (double)d2[i];
					}
				}
			}
			
			IArray result = new DoubleArray(resultDatas, resultSigns, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof String) {
			String str = (String)data;
			Object []resultDatas = new Object[size + 1];
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = str + d2[i];
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = str + d2[i];
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;			
		} else if (data == null) {
			return new ConstArray(null, size);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") + 
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}

	private IArray memberDivide(LongArray array) {
		Object data = this.data;
		int size = this.size;
		long []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		
		if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigDecimal decimal;
			if (data instanceof BigDecimal) {
				decimal = (BigDecimal)data;
			} else {
				decimal = new BigDecimal((BigInteger)data);
			}
			
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = decimal.divide(new BigDecimal(d2[i]), Variant.Divide_Scale, Variant.Divide_Round);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = decimal.divide(new BigDecimal(d2[i]), Variant.Divide_Scale, Variant.Divide_Round);
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			double v = ((Number)data).doubleValue();
			double []resultDatas = new double[size + 1];
			boolean []resultSigns = null;
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v / (double)d2[i];
				}
			} else {
				resultSigns = new boolean[size + 1];
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultSigns[i] = true;
					} else {
						resultDatas[i] = v / (double)d2[i];
					}
				}
			}
			
			IArray result = new DoubleArray(resultDatas, resultSigns, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof String) {
			String str = (String)data;
			Object []resultDatas = new Object[size + 1];
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = str + d2[i];
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = str + d2[i];
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;			
		} else if (data == null) {
			return new ConstArray(null, size);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") + 
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}

	private IArray memberDivide(DoubleArray array) {
		Object data = this.data;
		int size = this.size;
		double []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		
		if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigDecimal decimal;
			if (data instanceof BigDecimal) {
				decimal = (BigDecimal)data;
			} else {
				decimal = new BigDecimal((BigInteger)data);
			}
			
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = decimal.divide(new BigDecimal(d2[i]), Variant.Divide_Scale, Variant.Divide_Round);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = decimal.divide(new BigDecimal(d2[i]), Variant.Divide_Scale, Variant.Divide_Round);
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			double v = ((Number)data).doubleValue();
			if (array.isTemporary()) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						d2[i] = v / d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!s2[i]) {
							d2[i] = v / d2[i];
						}
					}
				}
				
				return array;
			} else {
				double []resultDatas = new double[size + 1];
				boolean []resultSigns = null;
				
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = v / d2[i];
					}
				} else {
					resultSigns = new boolean[size + 1];
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultSigns[i] = true;
						} else {
							resultDatas[i] = v / d2[i];
						}
					}
				}
				
				IArray result = new DoubleArray(resultDatas, resultSigns, size);
				result.setTemporary(true);
				return result;
			}
		} else if (data instanceof String) {
			String str = (String)data;
			Object []resultDatas = new Object[size + 1];
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = str + d2[i];
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = str + d2[i];
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;			
		} else if (data == null) {
			return new ConstArray(null, size);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") + 
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}

	private ObjectArray memberDivide(ObjectArray array) {
		Object data = this.data;
		int size = this.size;
		Object []datas = array.getDatas();
		
		if (array.isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = Variant.divide(data, datas[i]);
			}
			
			return array;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Variant.divide(data, datas[i]);
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
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
		if (array instanceof IntArray) {
			return memberMod((IntArray)array);
		} else if (array instanceof LongArray) {
			return memberMod((LongArray)array);
		} else if (array instanceof DoubleArray) {
			return memberMod((DoubleArray)array);
		} else if (array instanceof ConstArray) {
			Object value = ArrayUtil.mod(data, array.get(1));
			return new ConstArray(value, size);
		} else if (array instanceof ObjectArray) {
			return memberMod((ObjectArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illMod"));
		}
	}
	
	private IArray memberMod(IntArray array) {
		Object data = this.data;
		int size = this.size;
		int []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		
		if (data instanceof Long) {
			long v = ((Number)data).longValue();
			long []resultDatas = new long[size + 1];
			boolean []resultSigns = null;
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v % d2[i];
				}
			} else {
				resultSigns = new boolean[size + 1];
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultSigns[i] = true;
					} else {
						resultDatas[i] = v % d2[i];
					}
				}
			}
			
			IArray result = new LongArray(resultDatas, resultSigns, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Double || data instanceof Float) {
			double v = ((Number)data).doubleValue();
			double []resultDatas = new double[size + 1];
			boolean []resultSigns = null;
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v % d2[i];
				}
			} else {
				resultSigns = new boolean[size + 1];
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultSigns[i] = true;
					} else {
						resultDatas[i] = v % d2[i];
					}
				}
			}
			
			IArray result = new DoubleArray(resultDatas, resultSigns, size);
			result.setTemporary(true);
			return result;
		} else if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigInteger v;
			if (data instanceof BigDecimal) {
				v = ((BigDecimal)data).toBigInteger();
			} else {
				v = (BigInteger)data;
			}
			
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = new BigDecimal(v.mod(BigInteger.valueOf(d2[i])));
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = new BigDecimal(v.mod(BigInteger.valueOf(d2[i])));
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			int v = ((Number)data).intValue();
			if (array.isTemporary()) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						d2[i] = v % d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!s2[i]) {
							d2[i] = v % d2[i];
						}
					}
				}
				
				return array;
			} else {
				int []resultDatas = new int[size + 1];
				boolean []resultSigns = null;
				
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = v % d2[i];
					}
				} else {
					resultSigns = new boolean[size + 1];
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultSigns[i] = true;
						} else {
							resultDatas[i] = v % d2[i];
						}
					}
				}
				
				IArray result = new IntArray(resultDatas, resultSigns, size);
				result.setTemporary(true);
				return result;
			}
		} else if (data == null) {
			return new ConstArray(null, size);
		} else if (data instanceof Sequence) {
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = ArrayUtil.mod(data, d2[i]);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultDatas[i] = data;
					} else {
						resultDatas[i] = ArrayUtil.mod(data, d2[i]);
					}
				}				
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") + 
					array.getDataType() + mm.getMessage("Variant2.illMod"));
		}
	}
	
	private IArray memberMod(LongArray array) {
		Object data = this.data;
		int size = this.size;
		long []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		
		if (data instanceof Double || data instanceof Float) {
			double v = ((Number)data).doubleValue();
			double []resultDatas = new double[size + 1];
			boolean []resultSigns = null;
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v % d2[i];
				}
			} else {
				resultSigns = new boolean[size + 1];
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultSigns[i] = true;
					} else {
						resultDatas[i] = v % d2[i];
					}
				}
			}
			
			IArray result = new DoubleArray(resultDatas, resultSigns, size);
			result.setTemporary(true);
			return result;
		} else if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigInteger v;
			if (data instanceof BigDecimal) {
				v = ((BigDecimal)data).toBigInteger();
			} else {
				v = (BigInteger)data;
			}
			
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = new BigDecimal(v.mod(BigInteger.valueOf(d2[i])));
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = new BigDecimal(v.mod(BigInteger.valueOf(d2[i])));
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			long v = ((Number)data).longValue();
			if (array.isTemporary()) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						d2[i] = v % d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!s2[i]) {
							d2[i] = v % d2[i];
						}
					}
				}
				
				return array;
			} else {
				long []resultDatas = new long[size + 1];
				boolean []resultSigns = null;
				
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = v % d2[i];
					}
				} else {
					resultSigns = new boolean[size + 1];
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultSigns[i] = true;
						} else {
							resultDatas[i] = v % d2[i];
						}
					}
				}
				
				IArray result = new LongArray(resultDatas, resultSigns, size);
				result.setTemporary(true);
				return result;
			}
		} else if (data == null) {
			return new ConstArray(null, size);
		} else if (data instanceof Sequence) {
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = ArrayUtil.mod(data, d2[i]);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultDatas[i] = data;
					} else {
						resultDatas[i] = ArrayUtil.mod(data, d2[i]);
					}
				}				
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") + 
					array.getDataType() + mm.getMessage("Variant2.illMod"));
		}
	}
	
	private IArray memberMod(DoubleArray array) {
		Object data = this.data;
		int size = this.size;
		double []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		
		if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigInteger v;
			if (data instanceof BigDecimal) {
				v = ((BigDecimal)data).toBigInteger();
			} else {
				v = (BigInteger)data;
			}
			
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = new BigDecimal(v.mod(BigInteger.valueOf((long)d2[i])));
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = new BigDecimal(v.mod(BigInteger.valueOf((long)d2[i])));
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			double v = ((Number)data).longValue();
			if (array.isTemporary()) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						d2[i] = v % d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!s2[i]) {
							d2[i] = v % d2[i];
						}
					}
				}
				
				return array;
			} else {
				double []resultDatas = new double[size + 1];
				boolean []resultSigns = null;
				
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = v % d2[i];
					}
				} else {
					resultSigns = new boolean[size + 1];
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultSigns[i] = true;
						} else {
							resultDatas[i] = v % d2[i];
						}
					}
				}
				
				IArray result = new DoubleArray(resultDatas, resultSigns, size);
				result.setTemporary(true);
				return result;
			}
		} else if (data == null) {
			return new ConstArray(null, size);
		} else if (data instanceof Sequence) {
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = ArrayUtil.mod(data, d2[i]);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultDatas[i] = data;
					} else {
						resultDatas[i] = ArrayUtil.mod(data, d2[i]);
					}
				}				
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") + 
					array.getDataType() + mm.getMessage("Variant2.illMod"));
		}
	}
	
	private IArray memberMod(ObjectArray array) {
		Object data = this.data;
		int size = this.size;
		Object []datas = array.getDatas();
		
		if (array.isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = ArrayUtil.mod(data, datas[i]);
			}
			
			return array;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = ArrayUtil.mod(data, datas[i]);
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
			Object value = ArrayUtil.intDivide(data, array.get(1));
			return new ConstArray(value, size);
		} else if (array instanceof ObjectArray) {
			return memberIntDivide((ObjectArray)array);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType() + mm.getMessage("Variant2.with") +
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}
	
	private IArray memberIntDivide(IntArray array) {
		Object data = this.data;
		int size = this.size;
		int []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		
		if (data instanceof Long || data instanceof Double || data instanceof Float) {
			long v = ((Number)data).longValue();
			long []resultDatas = new long[size + 1];
			boolean []resultSigns = null;
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v / d2[i];
				}
			} else {
				resultSigns = new boolean[size + 1];
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultSigns[i] = true;
					} else {
						resultDatas[i] = v / d2[i];
					}
				}
			}
			
			IArray result = new LongArray(resultDatas, resultSigns, size);
			result.setTemporary(true);
			return result;
		} else if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigInteger v;
			if (data instanceof BigDecimal) {
				v = ((BigDecimal)data).toBigInteger();
			} else {
				v = (BigInteger)data;
			}
			
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = new BigDecimal(v.divide(BigInteger.valueOf(d2[i])));
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = new BigDecimal(v.divide(BigInteger.valueOf(d2[i])));
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			int v = ((Number)data).intValue();
			if (array.isTemporary()) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						d2[i] = v / d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!s2[i]) {
							d2[i] = v / d2[i];
						}
					}
				}
				
				return array;
			} else {
				int []resultDatas = new int[size + 1];
				boolean []resultSigns = null;
				
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = v / d2[i];
					}
				} else {
					resultSigns = new boolean[size + 1];
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultSigns[i] = true;
						} else {
							resultDatas[i] = v / d2[i];
						}
					}
				}
				
				IArray result = new IntArray(resultDatas, resultSigns, size);
				result.setTemporary(true);
				return result;
			}
		} else if (data == null) {
			return new ConstArray(null, size);
		} else if (data instanceof Sequence) {
			Sequence seq1 = (Sequence)data;
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					Sequence seq2 = new Sequence(1);
					seq2.add(d2[i]);
					resultDatas[i] = seq1.diff(seq2, false);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultDatas[i] = seq1;
					} else {
						Sequence seq2 = new Sequence(1);
						seq2.add(d2[i]);
						resultDatas[i] = seq1.diff(seq2, false);
					}
				}				
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") + 
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}
	
	private IArray memberIntDivide(LongArray array) {
		Object data = this.data;
		int size = this.size;
		long []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		
		if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigInteger v;
			if (data instanceof BigDecimal) {
				v = ((BigDecimal)data).toBigInteger();
			} else {
				v = (BigInteger)data;
			}
			
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = new BigDecimal(v.divide(BigInteger.valueOf(d2[i])));
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = new BigDecimal(v.divide(BigInteger.valueOf(d2[i])));
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			long v = ((Number)data).longValue();
			if (array.isTemporary()) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						d2[i] = v / d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!s2[i]) {
							d2[i] = v / d2[i];
						}
					}
				}
				
				return array;
			} else {
				long []resultDatas = new long[size + 1];
				boolean []resultSigns = null;
				
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = v / d2[i];
					}
				} else {
					resultSigns = new boolean[size + 1];
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultSigns[i] = true;
						} else {
							resultDatas[i] = v / d2[i];
						}
					}
				}
				
				IArray result = new LongArray(resultDatas, resultSigns, size);
				result.setTemporary(true);
				return result;
			}
		} else if (data == null) {
			return new ConstArray(null, size);
		} else if (data instanceof Sequence) {
			Sequence seq1 = (Sequence)data;
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					Sequence seq2 = new Sequence(1);
					seq2.add(d2[i]);
					resultDatas[i] = seq1.diff(seq2, false);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultDatas[i] = seq1;
					} else {
						Sequence seq2 = new Sequence(1);
						seq2.add(d2[i]);
						resultDatas[i] = seq1.diff(seq2, false);
					}
				}				
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") + 
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}
	
	private IArray memberIntDivide(DoubleArray array) {
		Object data = this.data;
		int size = this.size;
		double []d2 = array.getDatas();
		boolean []s2 = array.getSigns();
		
		if ((data instanceof BigDecimal) || (data instanceof BigInteger)) {
			BigInteger v;
			if (data instanceof BigDecimal) {
				v = ((BigDecimal)data).toBigInteger();
			} else {
				v = (BigInteger)data;
			}
			
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = new BigDecimal(v.divide(BigInteger.valueOf((long)d2[i])));
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i]) {
						resultDatas[i] = new BigDecimal(v.divide(BigInteger.valueOf((long)d2[i])));
					}
				}
			}
			
			IArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else if (data instanceof Number) {
			long v = ((Number)data).longValue();
			long []resultDatas = new long[size + 1];
			boolean []resultSigns = null;
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = v / (long)d2[i];
				}
			} else {
				resultSigns = new boolean[size + 1];
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultSigns[i] = true;
					} else {
						resultDatas[i] = v / (long)d2[i];
					}
				}
			}
			
			IArray result = new LongArray(resultDatas, resultSigns, size);
			result.setTemporary(true);
			return result;
		} else if (data == null) {
			return new ConstArray(null, size);
		} else if (data instanceof Sequence) {
			Sequence seq1 = (Sequence)data;
			Object []resultDatas = new Object[size + 1];
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					Sequence seq2 = new Sequence(1);
					seq2.add(d2[i]);
					resultDatas[i] = seq1.diff(seq2, false);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s2[i]) {
						resultDatas[i] = seq1;
					} else {
						Sequence seq2 = new Sequence(1);
						seq2.add(d2[i]);
						resultDatas[i] = seq1.diff(seq2, false);
					}
				}				
			}
			
			ObjectArray result = new ObjectArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(Variant.getDataType(data) + mm.getMessage("Variant2.with") + 
					array.getDataType() + mm.getMessage("Variant2.illDivide"));
		}
	}
	
	private IArray memberIntDivide(ObjectArray array) {
		Object data = this.data;
		int size = this.size;
		Object []datas = array.getDatas();
		
		if (array.isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = ArrayUtil.intDivide(data, datas[i]);
			}
			
			return array;
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = ArrayUtil.intDivide(data, datas[i]);
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
		return array.calcRelation(data, Relation.getInverseRelation(relation));
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @return ��ϵ����������
	 */
	public BoolArray calcRelation(Object value, int relation) {
		boolean result;
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			result = Variant.compare(data, value, true) == 0;
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			result = Variant.compare(data, value, true) > 0;
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			result = Variant.compare(data, value, true) >= 0;
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			result = Variant.compare(data, value, true) < 0;
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			result = Variant.compare(data, value, true) <= 0;
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			result = Variant.compare(data, value, true) != 0;
		} else if (relation == Relation.AND) {
			result = Variant.isTrue(data) && Variant.isTrue(value);
		} else { // Relation.OR
			result = Variant.isTrue(data) || Variant.isTrue(value);
		}
		
		return new BoolArray(result, size);
	}

	/**
	 * �Ƚ���������Ĵ�С
	 * @param array �Ҳ�����
	 * @return 1����ǰ�����0������������ȣ�-1����ǰ����С
	 */
	public int compareTo(IArray array) {
		if (array instanceof ConstArray) {
			int cmp = Variant.compare(data, array.get(1), true);
			if (cmp != 0) {
				return cmp;
			} else if (size == array.size()) {
				return 0;
			} else if (size < array.size()) {
				return -1;
			} else {
				return 1;
			}
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
		return 0;
	}
	
	/**
	 * �ж������������Ա�Ƿ����
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	public boolean isMemberEquals(int index1, int index2) {
		return true;
	}
	
	/**
	 * �ж����������ָ��Ԫ���Ƿ���ͬ
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ�Ƚϵ�����
	 * @param index Ҫ�Ƚϵ������Ԫ�ص�����
	 * @return true����ͬ��false������ͬ
	 */
	public boolean isEquals(int curIndex, IArray array, int index) {
		return Variant.isEquals(data, array.get(index));
	}
	
	/**
	 * �ж������ָ��Ԫ���Ƿ������ֵ���
	 * @param curIndex ����Ԫ����������1��ʼ����
	 * @param value ֵ
	 * @return true����ȣ�false�������
	 */
	public boolean isEquals(int curIndex, Object value) {
		return Variant.isEquals(data, value);
	}
	
	/**
	 * �ж����������ָ��Ԫ�صĴ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ�Ƚϵ�����
	 * @param index Ҫ�Ƚϵ������Ԫ�ص�����
	 * @return С�ڣ�С��0�����ڣ�0�����ڣ�����0
	 */
	public int compareTo(int curIndex, IArray array, int index) {
		return Variant.compare(data, array.get(index), true);
	}
	
	/**
	 * �Ƚ������ָ��Ԫ�������ֵ�Ĵ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param value Ҫ�Ƚϵ�ֵ
	 * @return
	 */
	public int compareTo(int curIndex, Object value) {
		return Variant.compare(data, value, true);
	}
	
	/**
	 * ȡָ����Ա�Ĺ�ϣֵ
	 * @param index ��Ա��������1��ʼ����
	 * @return ָ����Ա�Ĺ�ϣֵ
	 */
	public int hashCode(int index) {
		if (data != null) {
			return data.hashCode();
		} else {
			return 0;
		}
	}
	
	/**
	 * ���Ա��
	 * @return
	 */
	public Object sum() {
		if (data instanceof Number) {
			return Variant.multiply(data, size);
		} else {
			return null;
		}
	}
	
	/**
	 * ��ƽ��ֵ
	 * @return
	 */
	public Object average() {
		if (data instanceof Number) {
			if (data instanceof BigDecimal || data instanceof Double) {
				return data;
			} else if (data instanceof BigInteger) {
				return new BigDecimal((BigInteger)data);
			} else {
				return ((Number)data).doubleValue();
			}
		} else {
			return null;
		}
	}

	/**
	 * �õ����ĳ�Ա
	 * @return
	 */
	public Object max() {
		return data;
	}

	/**
	 * �õ���С�ĳ�Ա
	 * @return
	 */
	public Object min() {
		return data;
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ���㣬ֻ����resultΪ�����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	public void calcRelations(IArray array, int relation, BoolArray result, boolean isAnd) {
		array.calcRelations(data, Relation.getInverseRelation(relation), result, isAnd);
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ���㣬ֻ����resultΪ�����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	public void calcRelations(Object value, int relation, BoolArray result, boolean isAnd) {
		boolean []resultDatas = result.getDatas();
		boolean b;
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			b = Variant.compare(data, value, true) == 0;
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			b = Variant.compare(data, value, true) > 0;
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			b = Variant.compare(data, value, true) >= 0;
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			b = Variant.compare(data, value, true) < 0;
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			b = Variant.compare(data, value, true) <= 0;
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			b = Variant.compare(data, value, true) != 0;
		} else {
			throw new RuntimeException();
		}
		
		if (isAnd && !b) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = false;
			}
		} else if (!isAnd && b) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = true;
			}
		}
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ��
	 * @param array �Ҳ�����
	 * @return ��λ��������
	 */
	public IArray bitwiseAnd(IArray array) {
		if (array instanceof ConstArray) {
			Object value = And.and(data, array.get(1));
			return new ConstArray(value, size);
		} else {
			return array.bitwiseAnd(this);
		}
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ��
	 * @param array �Ҳ�����
	 * @return ��λ��������
	 */
	public IArray bitwiseOr(IArray array) {
		if (array instanceof ConstArray) {
			Object value = Or.or(data, array.get(1));
			return new ConstArray(value, size);
		} else {
			return array.bitwiseOr(this);
		}
	}
	
	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ���
	 * @param array �Ҳ�����
	 * @return ��λ���������
	 */
	public IArray bitwiseXOr(IArray array) {
		if (array instanceof ConstArray) {
			Object value = Xor.xor(data, array.get(1));
			return new ConstArray(value, size);
		} else {
			return array.bitwiseXOr(this);
		}
	}

	/**
	 * ���������Ա�İ�λȡ��
	 * @return ��Ա��λȡ���������
	 */
	public IArray bitwiseNot() {
		if (data instanceof BigDecimal) {
			BigInteger bi = ((BigDecimal)data).toBigInteger().not();
			return new ConstArray(bi, size);
		} else if (data instanceof BigInteger) {
			BigInteger bi = ((BigInteger)data).not();
			return new ConstArray(bi, size);
		} else if (data instanceof Number) {
			long v = ~((Number)data).longValue();
			return new ConstArray(v, size);
		} else if (data == null) {
			return new ConstArray(null, size);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("not" + mm.getMessage("function.paramTypeError"));
		}
	}
	
	/**
	 * ȡ����ʶ����ȡֵΪ����ж�Ӧ�����ݣ����������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	public IArray select(IArray signArray) {
		int size = signArray.size();
		int count = 0;
		
		if (signArray instanceof BoolArray) {
			BoolArray array = (BoolArray)signArray;
			boolean []d2 = array.getDatas();
			boolean []s2 = array.getSigns();
			
			if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					if (d2[i]) {
						count++;
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!s2[i] && d2[i]) {
						count++;
					}
				}
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				if (signArray.isTrue(i)) {
					count++;
				}
			}
		}
		
		return new ConstArray(data, count);
	}
	
	/**
	 * ȡĳһ���α�ʶ����ȡֵΪ��������������
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã���������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	public IArray select(int start, int end, IArray signArray) {
		int count = 0;
		if (signArray instanceof BoolArray) {
			BoolArray array = (BoolArray)signArray;
			boolean []d2 = array.getDatas();
			boolean []s2 = array.getSigns();
			
			if (s2 == null) {
				for (int i = start; i < end; ++i) {
					if (d2[i]) {
						count++;
					}
				}
			} else {
				for (int i = start; i < end; ++i) {
					if (!s2[i] && d2[i]) {
						count++;
					}
				}
			}
		} else {
			for (int i = start; i < end; ++i) {
				if (signArray.isTrue(i)) {
					count++;
				}
			}
		}
		
		return new ConstArray(data, count);
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
		int size = this.size;
		Object data = this.data;
		
		Object []result = new Object[size];
		for (int i = 0; i < size; ++i) {
			result[i] = data;
		}
		
		return result;
	}
	
	/**
	 * �ѳ�Ա�ָ��������
	 * @param result ���ڴ�ų�Ա������
	 */
	public void toArray(Object []result) {
		int size = this.size;
		Object data = this.data;
		
		for (int i = 0; i < size; ++i) {
			result[i] = data;
		}
	}

	/**
	 * �������ָ��λ�ò����������
	 * @param pos λ�ã�����
	 * @return ���غ�벿��Ԫ�ع��ɵ�����
	 */
	public IArray split(int pos) {
		int resultSize = size - pos + 1;
		this.size = pos - 1;
		return new ConstArray(data, resultSize);
	}
	
	/**
	 * ��ָ������Ԫ�ط���������������
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 * @return
	 */
	public IArray split(int from, int to) {
		int resultSize = to - from + 1;
		this.size -= resultSize;
		return new ConstArray(data, resultSize);
	}
	
	/**
	 * �������Ԫ�ؽ�������
	 */
	public void sort() {
	}
	
	/**
	 * �������Ԫ�ؽ�������
	 * @param comparator �Ƚ���
	 */
	public void sort(Comparator<Object> comparator) {
	}
	
	/**
	 * �����������Ƿ��м�¼
	 * @return boolean
	 */
	public boolean hasRecord() {
		return data instanceof BaseRecord;
	}
	
	/**
	 * �����Ƿ��ǣ���������
	 * @param isPure true������Ƿ��Ǵ�����
	 * @return boolean true���ǣ�false������
	 */
	public boolean isPmt(boolean isPure) {
		return data instanceof BaseRecord;
	}
	
	/**
	 * ��������ķ�ת����
	 * @return IArray
	 */
	public IArray rvs() {
		return new ConstArray(data, size);
	}

	/**
	 * ������Ԫ�ش�С������������ȡǰcount����λ��
	 * @param count ���countС��0��ȡ��|count|����λ��
	 * @param isAll countΪ����1ʱ�����isAllȡֵΪtrue��ȡ����������һ��Ԫ�ص�λ�ã�����ֻȡһ��
	 * @param isLast �Ƿ�Ӻ�ʼ��
	 * @param ignoreNull
	 * @return IntArray
	 */
	public IntArray ptop(int count, boolean isAll, boolean isLast, boolean ignoreNull) {
		if (size == 0 || (data == null && ignoreNull)) {
			return new IntArray(0);
		}
		
		if (count == 1 || count == -1) {
			// ȡ��Сֵ��λ��
			if (isAll) {
				return new IntArray(1, size);
			} else if (isLast) {
				IntArray result = new IntArray(1);
				result.pushInt(size);
				return result;
			} else {
				IntArray result = new IntArray(1);
				result.pushInt(1);
				return result;
			}
		} else if (count > 1) {
			// ȡ��С��count��Ԫ�ص�λ��
			return new IntArray(1, count);
		} else if (count < -1) {
			return new IntArray(1, -count);
		} else {
			return new IntArray(1);
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
		if (size == 0 || (data == null && ignoreNull)) {
			return new IntArray(0);
		}
		
		return new IntArray(1, size);
	}
	
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * �ѵ�ǰ����ת�ɶ������飬�����ǰ�����Ƕ��������򷵻����鱾��
	 * @return ObjectArray
	 */
	public ObjectArray toObjectArray() {
		int size = this.size;
		Object data = this.data;
		Object []resultDatas = new Object[size + 1];
		
		for (int i = 1; i <= size; ++i) {
			resultDatas[i] = data;
		}
		
		return new ObjectArray(resultDatas, size);
	}
	
	/**
	 * �Ѷ�������ת�ɴ��������飬����ת���׳��쳣
	 * @return IArray
	 */
	public IArray toPureArray() {
		int size = this.size;
		Object data = this.data;
		
		if (data instanceof String) {
			String str = (String)data;
			String []resultDatas = new String[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = str;
			}
			
			return new StringArray(resultDatas, size);
		} else if (data instanceof Date) {
			Date date = (Date)data;
			Date []resultDatas = new Date[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = date;
			}
			
			return new DateArray(resultDatas, size);
		} else if (data instanceof Double) {
			double d = (Double)data;
			double []resultDatas = new double[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = d;
			}
			
			return new DoubleArray(resultDatas, null, size);
		} else if (data instanceof Long) {
			long d = (Long)data;
			long []resultDatas = new long[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = d;
			}
			
			return new LongArray(resultDatas, null, size);
		} else if (data instanceof Integer) {
			int n = (Integer)data;
			int []resultDatas = new int[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = n;
			}
			
			return new IntArray(resultDatas, null, size);
		} else if (data instanceof Boolean) {
			return new BoolArray(((Boolean)data).booleanValue(), size);
		} else if (data == null) {
			Object []resultDatas = new Object[size + 1];
			return new ObjectArray(resultDatas, size);
		} else {
			Object []resultDatas = new Object[size + 1];
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = data;
			}
			
			return new ObjectArray(resultDatas, size);
		}
	}
	
	/**
	 * �����������������������л����
	 * @param refOrigin ����Դ�У�����������
	 * @return
	 */
	public IArray reserve(boolean refOrigin) {
		return toPureArray();
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
		} else {
			return other.combine(signArray.isFalse(), data);
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
		Object data = this.data;
		Object []resultDatas = new Object[size + 1];
		
		for (int i = 1; i <= size; ++i) {
			if (signArray.isTrue(i)) {
				resultDatas[i] = data;
			} else {
				resultDatas[i] = value;
			}
		}
		
		IArray result = new ObjectArray(resultDatas, size);
		result.setTemporary(true);
		return result;
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
		int count = 0;
		Object data = this.data;
		
		for (int i = 1; i <= size; ++i) {
			count += Bit1.bitCount(data, array.get(i));
		}
		
		return count;
	}
}
