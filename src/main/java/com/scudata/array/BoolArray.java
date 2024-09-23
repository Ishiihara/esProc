package com.scudata.array;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Sequence;
import com.scudata.expression.Relation;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.MultithreadUtil;
import com.scudata.util.Variant;

/**
 * �������飬��1��ʼ����
 * @author LW
 *
 */
public class BoolArray implements IArray {
	private static final long serialVersionUID = 1L;
	private static final byte NULL_SIGN = 60; // �������л�ʱ��ʾsigns�Ƿ�Ϊ��

	private boolean []datas; // ��0��Ԫ�ر�ʾ�Ƿ�����ʱ����
	private boolean []signs; // ��ʾ��Ӧλ�õ�Ԫ���Ƿ���null����null��Աʱ�Ų���
	private int size;
	
	public BoolArray() {
		datas = new boolean[DEFAULT_LEN];
	}
	
	public BoolArray(int initialCapacity) {
		datas = new boolean[++initialCapacity];
	}
	
	public BoolArray(boolean value, int size) {
		boolean []datas = this.datas = new boolean[size + 1];
		this.size = size;
		
		if (value) {
			for (int i = 1; i <= size; ++i) {
				datas[i] = true;
			}
		}
	}
	
	public BoolArray(boolean []datas, int size) {
		this.datas = datas;
		this.size = size;
	}
	
	public BoolArray(boolean []datas, boolean []signs, int size) {
		this.datas = datas;
		this.signs = signs;
		this.size = size;
	}
	
	public boolean[] getDatas() {
		return datas;
	}
	
	public boolean[] getSigns() {
		return signs;
	}
	
	public void setSigns(boolean []signs) {
		this.signs = signs;
	}
	
	/**
	 * ȡ��������ʹ������ڴ�����Ϣ��ʾ
	 * @return ���ʹ�
	 */
	public String getDataType() {
		MessageManager mm = EngineMessage.get();
		return mm.getMessage("DataType.Boolean");
	}
	
	/**
	 * ��������
	 * @return
	 */
	public IArray dup() {
		int len = size + 1;
		boolean []newDatas = new boolean[len];
		System.arraycopy(datas, 0, newDatas, 0, len);
		
		boolean []newSigns = null;
		if (signs != null) {
			newSigns = new boolean[len];
			System.arraycopy(signs, 0, newSigns, 0, len);
		}
		
		return new BoolArray(newDatas, newSigns, size);
	}
	
	/**
	 * д���ݵ���
	 * @param out �����
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		int size = this.size;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		if (signs == null) {
			out.writeByte(1);
		} else {
			out.writeByte(NULL_SIGN + 1);
		}
		
		out.writeInt(size);
		for (int i = 1; i <= size; ++i) {
			out.writeBoolean(datas[i]);
		}
		
		if (signs != null) {
			for (int i = 1; i <= size; ++i) {
				out.writeBoolean(signs[i]);
			}
		}
	}
	
	/**
	 * �����ж�����
	 * @param in ������
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		byte sign = in.readByte();
		size = in.readInt();
		int len = size + 1;
		boolean []datas = this.datas = new boolean[len];
		
		for (int i = 1; i < len; ++i) {
			datas[i] = in.readBoolean();
		}
		
		if (sign > NULL_SIGN) {
			boolean []signs = this.signs = new boolean[len];
			for (int i = 1; i < len; ++i) {
				signs[i] = in.readBoolean();
			}
		}
	}
	
	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		int size = this.size;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		if (signs == null) {
			out.writeByte(1);
		} else {
			out.writeByte(NULL_SIGN + 1);
		}
		
		out.writeInt(size);
		for (int i = 1; i <= size; ++i) {
			out.writeBoolean(datas[i]);
		}
		
		if (signs != null) {
			for (int i = 1; i <= size; ++i) {
				out.writeBoolean(signs[i]);
			}
		}

		return out.toByteArray();
	}
	
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		byte sign = in.readByte();
		size = in.readInt();
		int len = size + 1;
		boolean []datas = this.datas = new boolean[len];
		
		for (int i = 1; i < len; ++i) {
			datas[i] = in.readBoolean();
		}
		
		if (sign > NULL_SIGN) {
			boolean []signs = this.signs = new boolean[len];
			for (int i = 1; i < len; ++i) {
				signs[i] = in.readBoolean();
			}
		}
	}
	
	/**
	 * ����һ��ͬ���͵�����
	 * @param count
	 * @return
	 */
	public IArray newInstance(int count) {
		return new BoolArray(count);
	}
	
	/**
	 * ׷��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void add(Object o) {
		if (o instanceof Boolean) {
			ensureCapacity(size + 1);
			datas[++size] = (Boolean)o;
		} else if (o == null) {
			ensureCapacity(size + 1);
			if (signs == null) {
				signs = new boolean[datas.length];
			}
			
			signs[++size] = true;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Boolean"), Variant.getDataType(o)));
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(IArray array) {
		int size2 = array.size();
		if (size2 == 0) {
		} else if (array instanceof BoolArray) {
			BoolArray boolArray = (BoolArray)array;
			ensureCapacity(size + size2);
			
			System.arraycopy(boolArray.datas, 1, datas, size + 1, size2);
			if (boolArray.signs != null) {
				if (signs == null) {
					signs = new boolean[datas.length];
				}
				
				System.arraycopy(boolArray.signs, 1, signs, size + 1, size2);
			}
			
			size += size2;
		} else if (array instanceof ConstArray) {
			Object obj = array.get(1);
			if (obj instanceof Boolean) {
				ensureCapacity(size + size2);
				boolean v = ((Boolean)obj).booleanValue();
				boolean []datas = this.datas;
				
				for (int i = 0; i < size2; ++i) {
					datas[++size] = v;
				}
			} else if (obj == null) {
				ensureCapacity(size + size2);
				boolean []signs = this.signs;
				if (signs == null) {
					this.signs = signs = new boolean[datas.length];
				}
				
				for (int i = 0; i < size2; ++i) {
					signs[++size] = true;
				}
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Boolean"), array.getDataType()));
			}
		} else {
			//MessageManager mm = EngineMessage.get();
			//throw new RQException(mm.getMessage("pdm.arrayTypeError", 
			//		mm.getMessage("DataType.Boolean"), array.getDataType()));
			ensureCapacity(size + size2);
			boolean []datas = this.datas;
			
			for (int i = 1; i <= size2; ++i) {
				Object obj = array.get(i);
				if (obj instanceof Boolean) {
					datas[++size] = ((Boolean)obj).booleanValue();
				} else if (obj == null) {
					if (signs == null) {
						signs = new boolean[datas.length];
					}
					
					signs[++size] = true;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("pdm.arrayTypeError", 
							mm.getMessage("DataType.Boolean"), Variant.getDataType(obj)));
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
		} else if (array instanceof BoolArray) {
			BoolArray boolArray = (BoolArray)array;
			ensureCapacity(size + count);
			
			System.arraycopy(boolArray.datas, 1, datas, size + 1, count);
			if (boolArray.signs != null) {
				if (signs == null) {
					signs = new boolean[datas.length];
				}
				
				System.arraycopy(boolArray.signs, 1, signs, size + 1, count);
			}
			
			size += count;
		} else if (array instanceof ConstArray) {
			Object obj = array.get(1);
			if (obj instanceof Boolean) {
				ensureCapacity(size + count);
				boolean v = ((Boolean)obj).booleanValue();
				boolean []datas = this.datas;
				
				for (int i = 0; i < count; ++i) {
					datas[++size] = v;
				}
			} else if (obj == null) {
				ensureCapacity(size + count);
				boolean []signs = this.signs;
				if (signs == null) {
					this.signs = signs = new boolean[datas.length];
				}
				
				for (int i = 0; i < count; ++i) {
					signs[++size] = true;
				}
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Boolean"), array.getDataType()));
			}
		} else {
			//MessageManager mm = EngineMessage.get();
			//throw new RQException(mm.getMessage("pdm.arrayTypeError", 
			//		mm.getMessage("DataType.Boolean"), array.getDataType()));
			ensureCapacity(size + count);
			boolean []datas = this.datas;
			
			for (int i = 1; i <= count; ++i) {
				Object obj = array.get(i);
				if (obj instanceof Boolean) {
					datas[++size] = ((Boolean)obj).booleanValue();
				} else if (obj == null) {
					if (signs == null) {
						signs = new boolean[datas.length];
					}
					
					signs[++size] = true;
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("pdm.arrayTypeError", 
							mm.getMessage("DataType.Boolean"), Variant.getDataType(obj)));
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
		if (array instanceof BoolArray) {
			BoolArray boolArray = (BoolArray)array;
			ensureCapacity(size + count);
			
			System.arraycopy(boolArray.datas, index, datas, size + 1, count);
			if (boolArray.signs != null) {
				if (signs == null) {
					signs = new boolean[datas.length];
				}
				
				System.arraycopy(boolArray.signs, index, signs, size + 1, count);
			}
			
			size += count;
		} else if (array instanceof ConstArray) {
			Object obj = array.get(1);
			if (obj instanceof Boolean) {
				ensureCapacity(size + count);
				boolean v = ((Boolean)obj).booleanValue();
				boolean []datas = this.datas;
				
				for (int i = 0; i < count; ++i) {
					datas[++size] = v;
				}
			} else if (obj == null) {
				ensureCapacity(size + count);
				boolean []signs = this.signs;
				if (signs == null) {
					this.signs = signs = new boolean[datas.length];
				}
				
				for (int i = 0; i < count; ++i) {
					signs[++size] = true;
				}
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Boolean"), array.getDataType()));
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Boolean"), array.getDataType()));
		}
	}
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	public void addAll(Object []array) {
		for (Object obj : array) {
			if (obj != null && !(obj instanceof Boolean)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Boolean"), Variant.getDataType(obj)));
			}
		}
		
		int size2 = array.length;		
		ensureCapacity(size + size2);
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		for (int i = 0; i < size2; ++i) {
			if (array[i] != null) {
				datas[++size] = ((Boolean)array[i]).booleanValue();
			} else {
				if (signs == null) {
					this.signs = signs = new boolean[datas.length];
				}
				
				signs[++size] = true;
			}
		}
	}
	
	/**
	 * ����Ԫ�أ�������Ͳ��������׳��쳣
	 * @param index ����λ�ã���1��ʼ����
	 * @param o Ԫ��ֵ
	 */
	public void insert(int index, Object o) {
		if (o instanceof Boolean) {
			ensureCapacity(size + 1);
			
			size++;
			System.arraycopy(datas, index, datas, index + 1, size - index);
			
			if (signs != null) {
				System.arraycopy(signs, index, signs, index + 1, size - index);
			}
			
			datas[index] = (Boolean)o;
		} else if (o == null) {
			ensureCapacity(size + 1);
			
			size++;
			System.arraycopy(datas, index, datas, index + 1, size - index);
			
			if (signs == null) {
				signs = new boolean[datas.length];
			} else {
				System.arraycopy(signs, index, signs, index + 1, size - index);
			}
			
			signs[index] = true;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Boolean"), Variant.getDataType(o)));
		}
	}
	
	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, IArray array) {
		if (array instanceof BoolArray) {
			int numNew = array.size();
			BoolArray boolArray = (BoolArray)array;
			ensureCapacity(size + numNew);
			
			System.arraycopy(datas, pos, datas, pos + numNew, size - pos + 1);
			if (signs != null) {
				System.arraycopy(signs, pos, signs, pos + numNew, size - pos + 1);
			}
			
			System.arraycopy(boolArray.datas, 1, datas, pos, numNew);
			if (boolArray.signs == null) {
				if (signs != null) {
					boolean []signs = this.signs;
					for (int i = 0; i < numNew; ++i) {
						signs[pos + i] = false;
					}
				}
			} else {
				if (signs == null) {
					signs = new boolean[datas.length];
				}
				
				System.arraycopy(boolArray.signs, 1, signs, pos, numNew);
			}
			
			size += numNew;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Boolean"), array.getDataType()));
		}
	}
	
	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	public void insertAll(int pos, Object []array) {
		boolean containNull = false;
		for (Object obj : array) {
			if (obj == null) {
				containNull = true;
			} else if (!(obj instanceof Boolean)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						mm.getMessage("DataType.Boolean"), Variant.getDataType(obj)));
			}
		}

		int numNew = array.length;
		ensureCapacity(size + numNew);
		
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		System.arraycopy(datas, pos, datas, pos + numNew, size - pos + 1);
		if (signs != null) {
			System.arraycopy(signs, pos, signs, pos + numNew, size - pos + 1);
		}
		
		if (containNull) {
			if (signs == null) {
				this.signs = signs = new boolean[datas.length];
			}
			
			for (int i = 0; i < numNew; ++i) {
				if (array[i] == null) {
					signs[pos + i] = true;
				} else {
					datas[pos + i] = (Boolean)array[i];
					signs[pos + i] = false;
				}
			}
		} else {
			for (int i = 0; i < numNew; ++i) {
				datas[pos + i] = (Boolean)array[i];
				if (signs != null) {
					signs[pos + i] = false;
				}
			}
		}
		
		size += numNew;
	}

	/**
	 * ׷��Ԫ�أ��������������Ϊ���㹻�ռ���Ԫ�أ���������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	public void push(Object o) {
		if (o instanceof Boolean) {
			datas[++size] = (Boolean)o;
		} else if (o == null) {
			if (signs == null) {
				signs = new boolean[datas.length];
			}
			
			signs[++size] = true;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Boolean"), Variant.getDataType(o)));
		}
	}
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void push(IArray array, int index) {
		if (array instanceof BoolArray) {
			if (array.isNull(index)) {
				if (signs == null) {
					signs = new boolean[datas.length];
				}
				
				signs[++size] = true;
			} else {
				datas[++size] = ((BoolArray)array).getBool(index);
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Boolean"), array.getDataType()));
		}
	}
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void add(IArray array, int index) {
		if (array.isNull(index)) {
			ensureCapacity(size + 1);
			if (signs == null) {
				signs = new boolean[datas.length];
			}
			
			signs[++size] = true;
		} else if (array instanceof BoolArray) {
			ensureCapacity(size + 1);
			datas[++size] = ((BoolArray)array).getBool(index);
		} else {
			add(array.get(index));
			//MessageManager mm = EngineMessage.get();
			//throw new RQException(mm.getMessage("pdm.arrayTypeError", 
			//		mm.getMessage("DataType.Boolean"), array.getDataType()));
		}
	}
	
	/**
	 * ��array�еĵ�index��Ԫ���������ǰ�����ָ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param curIndex ��ǰ�����Ԫ����������1��ʼ����
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	public void set(int curIndex, IArray array, int index) {
		if (array.isNull(index)) {
			if (signs == null) {
				signs = new boolean[datas.length];
			}
			
			signs[curIndex] = true;
		} else if (array instanceof BoolArray) {
			datas[curIndex] = ((BoolArray)array).getBool(index);
		} else {
			set(curIndex, array.get(index));
		}
	}

	/**
	 * ׷��Ԫ��
	 * @param b ֵ
	 */
	public void addBool(boolean b) {
		ensureCapacity(size + 1);
		datas[++size] = b;
	}
	
	/**
	 * ׷��Ԫ�أ��������������Ϊ���㹻�ռ���Ԫ�أ�
	 * @param n ֵ
	 */
	public void push(boolean n) {
		datas[++size] = n;
	}
	
	/**
	 * ׷��Ԫ�أ��������������Ϊ���㹻�ռ���Ԫ�أ�
	 * @param n ֵ
	 */
	public void pushBool(boolean n) {
		datas[++size] = n;
	}
	
	/**
	 * ׷�ӿ�Ԫ��
	 */
	public void pushNull() {
		if (signs == null) {
			signs = new boolean[datas.length];
		}
		
		signs[++size] = true;
	}
	
	/**
	 * ȡָ��λ��Ԫ��
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public Object get(int index) {
		if (signs == null || !signs[index]) {
			return Boolean.valueOf(datas[index]);
		} else {
			return null;
		}
	}

	/**
	 * ȡָ��λ��Ԫ��
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean getBool(int index) {
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
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		int len = indexArray.length;
		BoolArray result = new BoolArray(len);
		
		if (signs == null) {
			for (int i : indexArray) {
				result.pushBool(datas[i]);
			}
		} else {
			for (int i : indexArray) {
				if (signs[i]) {
					result.pushNull();
				} else {
					result.pushBool(datas[i]);
				}
			}
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
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		int len = end - start + 1;
		BoolArray result = new BoolArray(len);
		
		if (doCheck) {
			if (signs == null) {
				for (; start <= end; ++start) {
					int q = indexArray[start];
					if (q > 0) {
						result.pushBool(datas[q]);
					} else {
						result.pushNull();
					}
				}
			} else {
				for (; start <= end; ++start) {
					int q = indexArray[start];
					if (q < 1 || signs[q]) {
						result.pushNull();
					} else {
						result.pushBool(datas[q]);
					}
				}
			}
		} else {
			if (signs == null) {
				for (; start <= end; ++start) {
					result.pushBool(datas[indexArray[start]]);
				}
			} else {
				for (; start <= end; ++start) {
					int q = indexArray[start];
					if (signs[q]) {
						result.pushNull();
					} else {
						result.pushBool(datas[q]);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param IArray λ������
	 * @return IArray
	 */
	public IArray get(IArray indexArray) {
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		int len = indexArray.size();
		BoolArray result = new BoolArray(len);
		
		if (signs == null) {
			for (int i = 1; i <= len; ++i) {
				result.pushBool(datas[indexArray.getInt(i)]);
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				int index = indexArray.getInt(i);
				if (signs[index]) {
					result.pushNull();
				} else {
					result.pushBool(datas[index]);
				}
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
		boolean []newDatas = new boolean[newSize + 1];
		System.arraycopy(datas, start, newDatas, 1, newSize);
		
		if (signs == null) {
			return new BoolArray(newDatas, null, newSize);
		} else {
			boolean []newSigns = new boolean[newSize + 1];
			System.arraycopy(signs, start, newSigns, 1, newSize);
			return new BoolArray(newDatas, newSigns, newSize);
		}
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

			boolean []newDatas = new boolean[newCapacity];
			System.arraycopy(datas, 0, newDatas, 0, size + 1);
			datas = newDatas;
			
			if (signs != null) {
				boolean []newSigns = new boolean[newCapacity];
				System.arraycopy(signs, 0, newSigns, 0, size + 1);
				signs = newSigns;
			}
		}
	}
	
	/**
	 * ����������ʹ����Ԫ�������
	 */
	public void trimToSize() {
		int newLen = size + 1;
		if (newLen < datas.length) {
			boolean []newDatas = new boolean[newLen];
			System.arraycopy(datas, 0, newDatas, 0, newLen);
			datas = newDatas;
			
			if (signs != null) {
				boolean []newSigns = new boolean[newLen];
				System.arraycopy(signs, 0, newSigns, 0, newLen);
				signs = newSigns;
			}
		}
	}
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ��ǿ�
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isNull(int index) {
		return signs != null && signs[index];
	}
	
	/**
	 * �ж�Ԫ���Ƿ���True
	 * @return BoolArray
	 */
	public BoolArray isTrue() {
		if (signs == null) {
			if (isTemporary()) {
				return this;
			} else {
				boolean []resultDatas = new boolean[size + 1];
				System.arraycopy(datas, 1, resultDatas, 1, size);
				BoolArray result = new BoolArray(resultDatas, size);
				result.setTemporary(true);
				return result;
			}
		} else {
			int size = this.size;
			boolean []resultDatas = new boolean[size + 1];
			System.arraycopy(datas, 1, resultDatas, 1, size);
			boolean []signs = this.signs;
			
			for (int i = 1; i <= size; ++i) {
				if (signs[i]) {
					resultDatas[i] = false;
				}
			}
			
			BoolArray result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	/**
	 * �ж�Ԫ���Ƿ��Ǽ�
	 * @return BoolArray
	 */
	public BoolArray isFalse() {
		int size = this.size;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		boolean []resultDatas = new boolean[size + 1];
		
		if (signs == null) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = !datas[i];
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = signs[i] || !datas[i];
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
		return datas[index] && (signs == null || !signs[index]);
	}
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ���False
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isFalse(int index) {
		return !datas[index] || (signs != null && signs[index]);
	}

	/**
	 * �Ƿ��Ǽ����������ʱ���������飬��ʱ�����Ŀ��Ա��޸ģ����� f1+f2+f3��ֻ�����һ�������Ž��
	 * @return true������ʱ���������飬false��������ʱ����������
	 */
	public boolean isTemporary() {
		return datas[0];
	}

	/**
	 * �����Ƿ��Ǽ����������ʱ����������
	 * @param ifTemporary true������ʱ���������飬false��������ʱ����������
	 */
	public void setTemporary(boolean ifTemporary) {
		datas[0] = ifTemporary;
	}
	
	/**
	 * ɾ�����һ��Ԫ��
	 */
	public void removeLast() {
		if (signs != null) {
			signs[size] = false;
		}
		
		size--;
	}
	
	/**
	 * ɾ��ָ��λ�õ�Ԫ��
	 * @param index ��������1��ʼ����
	 */
	public void remove(int index) {
		System.arraycopy(datas, index + 1, datas, index, size - index);
		if (signs != null) {
			System.arraycopy(signs, index + 1, signs, index, size - index);
			signs[size] = false;
		}
		
		--size;
	}
	
	/**
	 * ɾ��ָ�������ڵ�Ԫ��
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 */
	public void removeRange(int fromIndex, int toIndex) {
		System.arraycopy(datas, toIndex + 1, datas, fromIndex, size - toIndex);
		if (signs != null) {
			System.arraycopy(signs, toIndex + 1, signs, fromIndex, size - toIndex);
			for (int i = size - toIndex + fromIndex; i <= size; ++i) {
				signs[i] = false;
			}
		}
		
		size -= (toIndex - fromIndex + 1);
	}
	
	/**
	 * ɾ��ָ��λ�õ�Ԫ�أ���Ŵ�С��������
	 * @param seqs ��������
	 */
	public void remove(int []seqs) {
		int delCount = 0;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
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
				if (signs != null) {
					System.arraycopy(signs, cur + 1, signs, cur - delCount, moveCount);
				}
			}
			
			delCount++;
		}

		if (signs != null) {
			for (int i = 0, q = size; i < delCount; ++i) {
				signs[q - i] = false;
			}
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
		if (signs != null) {
			System.arraycopy(signs, start, signs, 1, newSize);
			for (int i = size; i > newSize; --i) {
				signs[i] = false;
			}
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
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		int size = this.size;
		int count = size;
		
		if (signs == null) {
			for (int i = 1; i <= size; ++i) {
				if (!datas[i]) {
					count--;
				}
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				if (signs[i] || !datas[i]) {
					count--;
				}
			}
		}
		
		return count;
	}
	
	/**
	 * �ж������Ƿ���ȡֵΪtrue��Ԫ��
	 * @return true���У�false��û��
	 */
	public boolean containTrue() {
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		int size = this.size;
		
		if (signs == null) {
			for (int i = 1; i <= size; ++i) {
				if (datas[i]) {
					return true;
				}
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				if (!signs[i] && datas[i]) {
					return true;
				}
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
		if (size == 0) {
			return null;
		}
		
		boolean []signs = this.signs;
		if (signs == null) {
			return Boolean.valueOf(datas[1]);
		}
		
		for (int i = 1; i <= size; ++i) {
			if (!signs[i]) {
				return Boolean.valueOf(datas[i]);
			}
		}
		
		return null;
	}

	public void set(int index, boolean b) {
		datas[index] = b;
		if (signs != null) {
			signs[index] = false;
		}
	}
	
	/**
	 * �޸�����ָ��Ԫ�ص�ֵ��������Ͳ��������׳��쳣
	 * @param index ��������1��ʼ����
	 * @param obj ֵ
	 */
	public void set(int index, Object obj) {
		if (obj == null) {
			if (signs == null) {
				signs = new boolean[datas.length];
			}
			
			signs[index] = true;
		} else if (obj instanceof Boolean) {
			datas[index] = (Boolean)obj;
			if (signs != null) {
				signs[index] = false;
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("pdm.arrayTypeError", 
					mm.getMessage("DataType.Boolean"), Variant.getDataType(obj)));
		}
	}
	
	/**
	 * ɾ�����е�Ԫ��
	 */
	public void clear() {
		signs = null;
		size = 0;
	}
	
	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @return int Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem) {
		if (elem instanceof Boolean) {
			int size = this.size;
			boolean []datas = this.datas;
			boolean []signs = this.signs;
			
			if (((Boolean)elem).booleanValue()) {
				if (size == 0) {
					return -1;
				} else if ((signs == null || !signs[size]) && datas[size]) {
					return size;
				} else {
					return -size - 1;
				}
			} else {
				if (size == 0) {
					return -1;
				} else if (signs == null) {
					return datas[1] ? -1 : 1;
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!signs[i]) {
							return datas[i] ? -i : i;
						}
					}
					
					// ����Ԫ��ȫ��Ϊ��
					return -size - 1;
				}
			}
		} else if (elem == null) {
			if (size > 0 && signs != null && signs[1]) {
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
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @param start ��ʼ����λ�ã�������
	 * @param end ��������λ�ã�������
	 * @return Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem, int start, int end) {
		if (elem instanceof Boolean) {
			boolean []datas = this.datas;
			boolean []signs = this.signs;
			
			if (((Boolean)elem).booleanValue()) {
				if (size == 0) {
					return -1;
				} else if ((signs == null || !signs[end]) && datas[end]) {
					return end;
				} else {
					return -end - 1;
				}
			} else {
				if (end == 0) {
					return -1;
				} else if (signs == null) {
					return datas[start] ? -1 : start;
				} else {
					for (int i = start; i <= end; ++i) {
						if (!signs[i]) {
							return datas[i] ? -i : i;
						}
					}
					
					// ����Ԫ��ȫ��Ϊ��
					return -end - 1;
				}
			}
		} else if (elem == null) {
			if (end > 0 && signs != null && signs[start]) {
				return start;
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
		if (elem instanceof Boolean) {
			boolean v = ((Boolean)elem).booleanValue();
			boolean []datas = this.datas;
			boolean []signs = this.signs;
			int size = this.size;
			
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					if (datas[i] == v) {
						return true;
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (!signs[i] && datas[i] == v) {
						return true;
					}
				}
			}
			
			return false;
		} else if (elem == null) {
			boolean []signs = this.signs;
			if (signs == null) {
				return false;
			}
			
			int size = this.size;
			for (int i = 1; i <= size; ++i) {
				if (signs[i]) {
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
		return false;
	}
	
	/**
	 * ����Ԫ�����������״γ��ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start ��ʼ����λ�ã�������
	 * @return ���Ԫ�ش����򷵻�ֵ����0�����򷵻�0
	 */
	public int firstIndexOf(Object elem, int start) {
		if (elem instanceof Boolean) {
			boolean v = ((Boolean)elem).booleanValue();
			boolean []datas = this.datas;
			boolean []signs = this.signs;
			int size = this.size;
			
			if (signs == null) {
				for (int i = start; i <= size; ++i) {
					if (datas[i] == v) {
						return i;
					}
				}
			} else {
				for (int i = start; i <= size; ++i) {
					if (!signs[i] && datas[i] == v) {
						return i;
					}
				}
			}
			
			return 0;
		} else if (elem == null) {
			boolean []signs = this.signs;
			if (signs == null) {
				return 0;
			} else {
				for (int i = start, size = this.size; i <= size; ++i) {
					if (signs[i]) {
						return i;
					}
				}
				
				return 0;
			}
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
		if (elem instanceof Boolean) {
			boolean v = ((Boolean)elem).booleanValue();
			boolean []datas = this.datas;
			boolean []signs = this.signs;
			
			if (signs == null) {
				for (int i = start; i > 0; --i) {
					if (datas[i] == v) {
						return i;
					}
				}
			} else {
				for (int i = start; i > 0; --i) {
					if (!signs[i] && datas[i] == v) {
						return i;
					}
				}
			}
			
			return 0;
		} else if (elem == null) {
			boolean []signs = this.signs;
			if (signs == null) {
				return 0;
			} else {
				for (int i = start; i > 0; --i) {
					if (signs[i]) {
						return i;
					}
				}
				
				return 0;
			}
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
		boolean []signs = this.signs;
		
		if (elem == null) {
			IntArray result = new IntArray(7);
			if (signs != null) {
				if (isSorted) {
					if (isFromHead) {
						for (int i = start; i <= size; ++i) {
							if (signs[i]) {
								result.addInt(i);
							} else {
								break;
							}
						}
					} else {
						for (int i = start; i > 0; --i) {
							if (signs[i]) {
								result.addInt(i);
							}
						}
					}
				} else {
					if (isFromHead) {
						for (int i = start; i <= size; ++i) {
							if (signs[i]) {
								result.addInt(i);
							}
						}
					} else {
						for (int i = start; i > 0; --i) {
							if (signs[i]) {
								result.addInt(i);
							}
						}
					}
				}
			}
			
			return result;
		} else if (!(elem instanceof Boolean)) {
			return new IntArray(1);
		}

		boolean b = ((Boolean)elem).booleanValue();
		boolean []datas = this.datas;
		IntArray result = new IntArray(7);
		
		if (isFromHead) {
			if (signs == null) {
				for (int i = start; i <= size; ++i) {
					if (datas[i] == b) {
						result.addInt(i);
					}
				}
			} else {
				for (int i = start; i <= size; ++i) {
					if (!signs[i] && datas[i] == b) {
						result.addInt(i);
					}
				}
			}
		} else {
			if (signs == null) {
				for (int i = start; i > 0; --i) {
					if (datas[i] == b) {
						result.addInt(i);
					}
				}
			} else {
				for (int i = start; i > 0; --i) {
					if (!signs[i] && datas[i] == b) {
						result.addInt(i);
					}
				}
			}
		}
		
		return result;
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
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		int size = this.size;
		
		if (isTemporary()) {
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					datas[i] = !datas[i];
				}
			} else {
				this.signs = null;
				for (int i = 1; i <= size; ++i) {
					datas[i] = signs[i] || !datas[i];
				}
			}
			
			return this;
		} else {
			boolean []newDatas = new boolean[size + 1];
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					newDatas[i] = !datas[i];
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					newDatas[i] = signs[i] || !datas[i];
				}
			}
			
			IArray  result = new BoolArray(newDatas, size);
			result.setTemporary(true);
			return result;
		}
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
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		String []d2 = array.getDatas();
		
		if (array.isTemporary()) {
			for (int i = 1; i <= size; ++i) {
				if (s1 == null || !s1[i]) {
					if (d2[i] != null) {
						d2[i] = d1[i] + d2[i];
					} else {
						d2[i] = Boolean.toString(d1[i]);
					}
				}
			}
			
			return array;
		} else {
			String []resultDatas = new String[size + 1];
			for (int i = 1; i <= size; ++i) {
				if (d2[i] != null) {
					if (s1 == null || !s1[i]) {
						resultDatas[i] = d1[i] + d2[i];
					} else {
						resultDatas[i] = d2[i];
					}
				} else if (s1 == null || !s1[i]) {
					resultDatas[i] = Boolean.toString(d1[i]);
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
		if (array instanceof BoolArray) {
			return calcRelation((BoolArray)array, relation);
		} else if (array instanceof ConstArray) {
			return calcRelation(array.get(1), relation);
		} else if (array instanceof IntArray) {
			return calcRelation((IntArray)array, relation);
		} else if (array instanceof LongArray) {
			return calcRelation((LongArray)array, relation);
		} else if (array instanceof DoubleArray) {
			return calcRelation((DoubleArray)array, relation);
		} else if (array instanceof DateArray) {
			return calcRelation((DateArray)array, relation);
		} else if (array instanceof StringArray) {
			return calcRelation((StringArray)array, relation);
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
		if (value instanceof Boolean) {
			return calcRelation(((Boolean)value).booleanValue(), relation);
		} else if (value == null) {
			return calcRelationNull(relation);
		} else {
			int size = this.size;
			boolean []d1 = this.datas;
			boolean []s1 = this.signs;
			boolean b = Variant.isTrue(value);
			
			if (relation == Relation.AND) {
				BoolArray result;
				boolean []resultDatas;
				if (isTemporary()) {
					resultDatas = d1;
					result = this;
					signs = null;
				} else {
					resultDatas = new boolean[size + 1];
					result = new BoolArray(resultDatas, size);
					result.setTemporary(true);
				}
				
				if (!b) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = false;
					}
				} else if (s1 == null) {
					if (resultDatas != d1) {
						System.arraycopy(d1, 1, resultDatas, 1, size);
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = !s1[i] && d1[i];
					}
				}
				
				return result;
			} else if (relation == Relation.OR) {
				BoolArray result;
				boolean []resultDatas;
				if (isTemporary()) {
					resultDatas = d1;
					result = this;
					signs = null;
				} else {
					resultDatas = new boolean[size + 1];
					result = new BoolArray(resultDatas, size);
					result.setTemporary(true);
				}
				
				if (b) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = true;
					}
				} else if (s1 == null) {
					if (resultDatas != d1) {
						System.arraycopy(d1, 1, resultDatas, 1, size);
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = !s1[i] && d1[i];
					}
				}
				
				return result;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Variant2.illCompare", get(1), value,
						getDataType(), Variant.getDataType(value)));
			}
		}
	}
	
	private BoolArray calcRelation(BoolArray array, int relation) {
		int size = this.size;
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		boolean []d2 = array.datas;
		boolean []s2 = array.signs;
		
		BoolArray result;
		boolean []resultDatas;
		if (isTemporary()) {
			resultDatas = d1;
			result = this;
			result.signs = null;
		} else if (array.isTemporary()) {
			resultDatas = d2;
			result = array;
			result.signs = null;
		} else {
			resultDatas = new boolean[size + 1];
			result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			if (s1 == null) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = d1[i] == d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultDatas[i] = false;
						} else {
							resultDatas[i] = d1[i] == d2[i];
						}
					}
				}
			} else if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = d1[i] == d2[i];
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = s2[i];
					} else if (s2[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = d1[i] == d2[i];
					}
				}
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			if (s1 == null) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) > 0;
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultDatas[i] = true;
						} else {
							resultDatas[i] = Variant.compare(d1[i], d2[i]) > 0;
						}
					}
				}
			} else if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) > 0;
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = false;
					} else if (s2[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) > 0;
					}
				}
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			if (s1 == null) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) >= 0;
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultDatas[i] = true;
						} else {
							resultDatas[i] = Variant.compare(d1[i], d2[i]) >= 0;
						}
					}
				}
			} else if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) >= 0;
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = s2[i];
					} else if (s2[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) >= 0;
					}
				}
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			if (s1 == null) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) < 0;
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultDatas[i] = false;
						} else {
							resultDatas[i] = Variant.compare(d1[i], d2[i]) < 0;
						}
					}
				}
			} else if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) < 0;
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = !s2[i];
					} else if (s2[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) < 0;
					}
				}
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			if (s1 == null) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) <= 0;
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultDatas[i] = false;
						} else {
							resultDatas[i] = Variant.compare(d1[i], d2[i]) <= 0;
						}
					}
				}
			} else if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) <= 0;
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = true;
					} else if (s2[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = Variant.compare(d1[i], d2[i]) <= 0;
					}
				}
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			if (s1 == null) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = d1[i] != d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (s2[i]) {
							resultDatas[i] = true;
						} else {
							resultDatas[i] = d1[i] != d2[i];
						}
					}
				}
			} else if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = d1[i] != d2[i];
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = !s2[i];
					} else if (s2[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = d1[i] != d2[i];
					}
				}
			}
		} else if (relation == Relation.AND) {
			if (s1 == null) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = d1[i] && d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = d1[i] && !s2[i] && d2[i];
					}
				}
			} else if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !s1[i] && d1[i] && d2[i];
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !s1[i] && d1[i] && !s2[i] && d2[i];
				}
			}
		} else { // Relation.OR
			if (s1 == null) {
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = d1[i] || d2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = d1[i] || (!s2[i] && d2[i]);
					}
				}
			} else if (s2 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = (!s1[i] && d1[i]) || d2[i];
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = (!s1[i] && d1[i]) || (!s2[i] && d2[i]);
				}
			}
		}
		
		return result;
	}
	
	private BoolArray calcRelation(boolean []s2, int relation) {
		int size = this.size;
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		
		if (isTemporary()) {
			if (relation == Relation.AND) {
				if (s1 == null) {
					if (s2 != null) {
						for (int i = 1; i <= size; ++i) {
							if (s2[i]) {
								d1[i] = false;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (s1[i]) {
							d1[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (s1[i] || s2[i]) {
							d1[i] = false;
						}
					}
				}
			} else { // Relation.OR
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						d1[i] = true;
					}
				} else if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!s2[i]) {
							d1[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!s2[i]) {
							d1[i] = true;
						} else if (s1[i]) {
							d1[i] = false;
						}
					}
				}
			}
			
			signs = null;
			return this;
		} else {
			boolean []resultDatas = new boolean[size + 1];
			if (relation == Relation.AND) {
				if (s1 == null) {
					if (s2 == null) {
						System.arraycopy(d1, 1, resultDatas, 1, size);
					} else {
						for (int i = 1; i <= size; ++i) {
							resultDatas[i] = d1[i] && !s2[i];
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = !s1[i] && d1[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = !s2[i] && !s1[i] && d1[i];
					}
				}
			} else { // Relation.OR
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = true;
					}
				} else if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = d1[i] || !s2[i];
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						resultDatas[i] = !s2[i] || (!s1[i] && d1[i]);
					}
				}
			}
			
			BoolArray result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
			return result;
		}
	}
	
	protected BoolArray calcRelation(IntArray array, int relation) {
		if (relation != Relation.AND && relation != Relation.OR) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), array.get(1),
					getDataType(), array.getDataType()));
		}
		
		return calcRelation(array.getSigns(), relation);
	}
	
	protected BoolArray calcRelation(LongArray array, int relation) {
		if (relation != Relation.AND && relation != Relation.OR) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), array.get(1),
					getDataType(), array.getDataType()));
		}

		return calcRelation(array.getSigns(), relation);
	}
	
	protected BoolArray calcRelation(DoubleArray array, int relation) {
		if (relation != Relation.AND && relation != Relation.OR) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), array.get(1),
					getDataType(), array.getDataType()));
		}

		return calcRelation(array.getSigns(), relation);
	}
	
	BoolArray calcRelation(DateArray array, int relation) {
		int size = this.size;
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		Object []d2 = array.getDatas();
		
		if (relation == Relation.AND) {
			BoolArray result;
			boolean []resultDatas;
			if (isTemporary()) {
				resultDatas = d1;
				result = this;
				signs = null;
			} else {
				resultDatas = new boolean[size + 1];
				result = new BoolArray(resultDatas, size);
				result.setTemporary(true);
			}
			
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = d1[i] && Variant.isTrue(d2[i]);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !s1[i] && d1[i] && Variant.isTrue(d2[i]);
				}
			}
			
			return result;
		} else if (relation == Relation.OR) {
			BoolArray result;
			boolean []resultDatas;
			if (isTemporary()) {
				resultDatas = d1;
				result = this;
				signs = null;
			} else {
				resultDatas = new boolean[size + 1];
				result = new BoolArray(resultDatas, size);
				result.setTemporary(true);
			}
			
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = d1[i] || Variant.isTrue(d2[i]);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = (!s1[i] && d1[i]) || Variant.isTrue(d2[i]);
				}
			}
			
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), array.get(1),
					getDataType(), array.getDataType()));
		}
	}
	
	BoolArray calcRelation(StringArray array, int relation) {
		int size = this.size;
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		Object []d2 = array.getDatas();
		
		if (relation == Relation.AND) {
			BoolArray result;
			boolean []resultDatas;
			if (isTemporary()) {
				resultDatas = d1;
				result = this;
				signs = null;
			} else {
				resultDatas = new boolean[size + 1];
				result = new BoolArray(resultDatas, size);
				result.setTemporary(true);
			}
			
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = d1[i] && Variant.isTrue(d2[i]);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !s1[i] && d1[i] && Variant.isTrue(d2[i]);
				}
			}
			
			return result;
		} else if (relation == Relation.OR) {
			BoolArray result;
			boolean []resultDatas;
			if (isTemporary()) {
				resultDatas = d1;
				result = this;
				signs = null;
			} else {
				resultDatas = new boolean[size + 1];
				result = new BoolArray(resultDatas, size);
				result.setTemporary(true);
			}
			
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = d1[i] || Variant.isTrue(d2[i]);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = (!s1[i] && d1[i]) || Variant.isTrue(d2[i]);
				}
			}
			
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), array.get(1),
					getDataType(), array.getDataType()));
		}
	}
	
	/**
	 * �Ƚ�ָ������ָ������Ĵ�С��null��С
	 * @param b1 ��ֵ
	 * @param o2 ��ֵ
	 * @return 1����ֵ��0��ͬ����-1����ֵ��
	 */
	private static int compare(boolean b1, Object o2) {
		if (o2 instanceof Boolean) {
			return Variant.compare(b1, ((Boolean)o2).booleanValue());
		} else if (o2 == null) {
			return 1;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", b1, o2,
					mm.getMessage("DataType.Boolean"), Variant.getDataType(o2)));
		}
	}
	
	BoolArray calcRelation(ObjectArray array, int relation) {
		int size = this.size;
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		Object []d2 = array.getDatas();
		
		BoolArray result;
		boolean []resultDatas;
		if (isTemporary()) {
			resultDatas = d1;
			result = this;
			signs = null;
		} else {
			resultDatas = new boolean[size + 1];
			result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = compare(d1[i], d2[i]) == 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = d2[i] == null;
					} else {
						resultDatas[i] = compare(d1[i], d2[i]) == 0;
					}
				}
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = compare(d1[i], d2[i]) > 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = compare(d1[i], d2[i]) > 0;
					}
				}
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = compare(d1[i], d2[i]) >= 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = d2[i] == null;
					} else {
						resultDatas[i] = compare(d1[i], d2[i]) >= 0;
					}
				}
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = compare(d1[i], d2[i]) < 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = d2[i] != null;
					} else {
						resultDatas[i] = compare(d1[i], d2[i]) < 0;
					}
				}
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = compare(d1[i], d2[i]) <= 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = compare(d1[i], d2[i]) <= 0;
					}
				}
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = compare(d1[i], d2[i]) != 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = d2[i] != null;
					} else {
						resultDatas[i] = compare(d1[i], d2[i]) != 0;
					}
				}
			}
		} else if (relation == Relation.AND) {
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = d1[i] && Variant.isTrue(d2[i]);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !s1[i] && d1[i] && Variant.isTrue(d2[i]);
				}
			}
		} else { // Relation.OR
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = d1[i] || Variant.isTrue(d2[i]);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = (!s1[i] && d1[i]) || Variant.isTrue(d2[i]);
				}
			}
		}
		
		return result;
	}
	
	private BoolArray calcRelation(boolean value, int relation) {
		int size = this.size;
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		
		BoolArray result;
		boolean []resultDatas;
		if (isTemporary()) {
			resultDatas = d1;
			result = this;
			result.signs = null;
		} else {
			resultDatas = new boolean[size + 1];
			result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = Variant.compare(d1[i], value) == 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = Variant.compare(d1[i], value) == 0;
					}
				}
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = Variant.compare(d1[i], value) > 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = Variant.compare(d1[i], value) > 0;
					}
				}
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = Variant.compare(d1[i], value) >= 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = false;
					} else {
						resultDatas[i] = Variant.compare(d1[i], value) >= 0;
					}
				}
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = Variant.compare(d1[i], value) < 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = Variant.compare(d1[i], value) < 0;
					}
				}
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = Variant.compare(d1[i], value) <= 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = Variant.compare(d1[i], value) <= 0;
					}
				}
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			if (s1 == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = Variant.compare(d1[i], value) != 0;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (s1[i]) {
						resultDatas[i] = true;
					} else {
						resultDatas[i] = Variant.compare(d1[i], value) != 0;
					}
				}
			}
		} else if (relation == Relation.AND) {
			if (!value) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = false;
				}
			} else if (s1 == null) {
				if (resultDatas != d1) {
					System.arraycopy(d1, 1, resultDatas, 1, size);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !s1[i] && d1[i];
				}
			}
		} else { // Relation.OR
			if (value) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else if (s1 == null) {
				if (resultDatas != d1) {
					System.arraycopy(d1, 1, resultDatas, 1, size);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !s1[i] && d1[i];
				}
			}
		}
		
		return result;
	}
	
	private BoolArray calcRelationNull(int relation) {
		int size = this.size;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		BoolArray result;
		boolean []resultDatas;
		if (isTemporary()) {
			resultDatas = datas;
			result = this;
			result.signs = null;
		} else {
			resultDatas = new boolean[size + 1];
			result = new BoolArray(resultDatas, size);
			result.setTemporary(true);
		}
		
		if (relation == Relation.EQUAL) {
			// �Ƿ�����ж�
			if (signs != null) {
				System.arraycopy(signs, 1, resultDatas, 1, size);
			} else if (resultDatas == datas) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = false;
				}
			}
		} else if (relation == Relation.GREATER) {
			// �Ƿ�����ж�
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !signs[i];
				}
			}
		} else if (relation == Relation.GREATER_EQUAL) {
			// �Ƿ���ڵ����ж�
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = true;
			}
		} else if (relation == Relation.LESS) {
			// �Ƿ�С���ж�
			if (resultDatas == datas) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = false;
				}
			}
		} else if (relation == Relation.LESS_EQUAL) {
			// �Ƿ�С�ڵ����ж�
			if (signs != null) {
				System.arraycopy(signs, 1, resultDatas, 1, size);
			} else if (resultDatas == datas) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = false;
				}
			}
		} else if (relation == Relation.NOT_EQUAL) {
			// �Ƿ񲻵����ж�
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = true;
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !signs[i];
				}
			}
		} else if (relation == Relation.AND) {
			if (resultDatas == datas) {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = false;
				}
			}
		} else { // Relation.OR
			if (signs == null) {
				if (resultDatas != datas) {
					System.arraycopy(datas, 1, resultDatas, 1, size);
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					resultDatas[i] = !signs[i] && datas[i];
				}
			}
		}
		
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
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		
		int size = size1;
		int result = 0;
		if (size1 < size2) {
			result = -1;
		} else if (size1 > size2) {
			result = 1;
			size = size2;
		}

		if (array instanceof BoolArray) {
			BoolArray array2 = (BoolArray)array;
			boolean []d2 = array2.datas;
			boolean []s2 = array2.signs;
			
			for (int i = 1; i <= size; ++i) {
				if (s1 == null || !s1[i]) {
					if (s2 == null || !s2[i]) {
						int cmp = Variant.compare(d1[i], d2[i]);
						if (cmp != 0) {
							return cmp;
						}
					} else {
						return 1;
					}
				} else if (s2 == null || !s2[i]) {
					return -1;
				}
			}
		} else if (array instanceof ConstArray) {
			Object value = array.get(1);
			if (value instanceof Boolean) {
				boolean d2 = ((Boolean)value).booleanValue();
				for (int i = 1; i <= size; ++i) {
					if (s1 == null || !s1[i]) {
						int cmp = Variant.compare(d1[i], d2);
						if (cmp != 0) {
							return cmp;
						}
					} else {
						return -1;
					}
				}
			} else if (value == null) {
				if (s1 == null) {
					return 1;
				}
				
				for (int i = 1; i <= size; ++i) {
					if (!s1[i]) {
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
				if (s1 == null || !s1[i]) {
					int cmp = compare(d1[i], d2[i]);
					if (cmp != 0) {
						return cmp;
					}
				} else if (d2[i] != null) {
					return -1;
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
		if (signs == null) {
			return Variant.compare(datas[index1], datas[index2]);
		} else if (signs[index1]) {
			return signs[index2] ? 0 : -1;
		} else if (signs[index2]) {
			return 1;
		} else {
			return Variant.compare(datas[index1], datas[index2]);
		}
	}
	
	/**
	 * �ж������������Ա�Ƿ����
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	public boolean isMemberEquals(int index1, int index2) {
		if (signs == null) {
			return datas[index1] == datas[index2];
		} else if (signs[index1]) {
			return signs[index2];
		} else if (signs[index2]) {
			return false;
		} else {
			return datas[index1] == datas[index2];
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
		if (isNull(curIndex)) {
			return array.isNull(index);
		} else if (array.isNull(index)) {
			return false;
		} else if (array instanceof BoolArray) {
			return datas[curIndex] == ((BoolArray)array).getBool(index);
		} else {
			Object obj = array.get(index);
			return obj instanceof Boolean && ((Boolean)obj).booleanValue() == datas[curIndex];
		}
	}
	
	/**
	 * �ж������ָ��Ԫ���Ƿ������ֵ���
	 * @param curIndex ����Ԫ����������1��ʼ����
	 * @param value ֵ
	 * @return true����ȣ�false�������
	 */
	public boolean isEquals(int curIndex, Object value) {
		if (value instanceof Boolean) {
			if (signs == null || !signs[curIndex]) {
				return datas[curIndex] == ((Boolean)value).booleanValue();
			} else {
				return false;
			}
		} else if (value == null) {
			return signs != null && signs[curIndex];
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
		if (isNull(curIndex)) {
			return array.isNull(index) ? 0 : -1;
		} else if (array.isNull(index)) {
			return 1;
		} else if (array instanceof BoolArray) {
			return Variant.compare(datas[curIndex], ((BoolArray)array).getBool(index));
		} else {
			return compare(datas[curIndex], array.get(index));
		}
	}
	
	/**
	 * �Ƚ������ָ��Ԫ�������ֵ�Ĵ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param value Ҫ�Ƚϵ�ֵ
	 * @return
	 */
	public int compareTo(int curIndex, Object value) {
		if (isNull(curIndex)) {
			return value == null ? 0 : -1;
		} else if (value == null) {
			return 1;
		} else {
			return compare(datas[curIndex], value);
		}
	}
	
	/**
	 * ȡָ����Ա�Ĺ�ϣֵ
	 * @param index ��Ա��������1��ʼ����
	 * @return ָ����Ա�Ĺ�ϣֵ
	 */
	public int hashCode(int index) {
		if (signs == null || !signs[index]) {
			return Boolean.hashCode(datas[index]);
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
		
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		if (signs == null) {
			for (int i = 1; i <= size; ++i) {
				if (datas[i]) {
					return Boolean.TRUE;
				}
			}
			
			return Boolean.FALSE;
		} else {
			for (int i = 1; i <= size; ++i) {
				if (!signs[i] && datas[i]) {
					return Boolean.TRUE;
				}
			}
			
			return Boolean.FALSE;
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
		
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		if (signs == null) {
			for (int i = 1; i <= size; ++i) {
				if (!datas[i]) {
					return Boolean.FALSE;
				}
			}
			
			return Boolean.TRUE;
		} else {
			for (int i = 1; i <= size; ++i) {
				if (!signs[i] && !datas[i]) {
					return Boolean.FALSE;
				}
			}
			
			return Boolean.TRUE;
		}
	}

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ���㣬ֻ����resultΪ�����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	public void calcRelations(IArray array, int relation, BoolArray result, boolean isAnd) {
		if (array == this) {
		} else if (array instanceof BoolArray) {
			calcRelations((BoolArray)array, relation, result, isAnd);
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
		if (value instanceof Boolean) {
			calcRelations(((Boolean)value).booleanValue(), relation, result, isAnd);
		} else if (value == null) {
			ArrayUtil.calcRelationsNull(signs, size, relation, result, isAnd);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", get(1), value,
					getDataType(), Variant.getDataType(value)));
		}
	}
	
	private void calcRelations(BoolArray array, int relation, BoolArray result, boolean isAnd) {
		int size = this.size;
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		boolean []d2 = array.datas;
		boolean []s2 = array.signs;
		boolean []resultDatas = result.getDatas();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && d1[i] != d2[i]) {
								resultDatas[i] = false;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && (s2[i] || d1[i] != d2[i])) {
								resultDatas[i] = false;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] || d1[i] != d2[i])) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] != s2[i] || (!s1[i] && d1[i] != d2[i]))) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && Variant.compare(d1[i], d2[i]) <= 0) {
								resultDatas[i] = false;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && !s2[i] && Variant.compare(d1[i], d2[i]) <= 0) {
								resultDatas[i] = false;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] || Variant.compare(d1[i], d2[i]) <= 0)) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] || (!s2[i] && Variant.compare(d1[i], d2[i]) <= 0))) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && Variant.compare(d1[i], d2[i]) < 0) {
								resultDatas[i] = false;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && !s2[i] && Variant.compare(d1[i], d2[i]) < 0) {
								resultDatas[i] = false;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] || Variant.compare(d1[i], d2[i]) < 0)) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && !s2[i] && (s1[i] || Variant.compare(d1[i], d2[i]) < 0)) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && Variant.compare(d1[i], d2[i]) >= 0) {
								resultDatas[i] = false;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && (s2[i] || Variant.compare(d1[i], d2[i]) >= 0)) {
								resultDatas[i] = false;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && !s1[i] && Variant.compare(d1[i], d2[i]) >= 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s2[i] || (!s1[i] && Variant.compare(d1[i], d2[i]) >= 0))) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && Variant.compare(d1[i], d2[i]) > 0) {
								resultDatas[i] = false;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && (s2[i] || Variant.compare(d1[i], d2[i]) > 0)) {
								resultDatas[i] = false;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && !s1[i] && Variant.compare(d1[i], d2[i]) > 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && !s1[i] && (s2[i] || Variant.compare(d1[i], d2[i]) > 0)) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && d1[i] == d2[i]) {
								resultDatas[i] = false;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (resultDatas[i] && !s2[i] && d1[i] == d2[i]) {
								resultDatas[i] = false;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && !s1[i] && d1[i] == d2[i]) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && s1[i] == s2[i] && (s1[i] || d1[i] == d2[i])) {
							resultDatas[i] = false;
						}
					}
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// �������ִ��||����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && d1[i] == d2[i]) {
								resultDatas[i] = true;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && !s2[i] && d1[i] == d2[i]) {
								resultDatas[i] = true;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && !s1[i] && d1[i] == d2[i]) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] ? s2[i] : !s2[i] && d1[i] == d2[i])) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) > 0) {
								resultDatas[i] = true;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && (s2[i] || Variant.compare(d1[i], d2[i]) > 0)) {
								resultDatas[i] = true;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && !s1[i] && Variant.compare(d1[i], d2[i]) > 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && !s1[i] && (s2[i] || Variant.compare(d1[i], d2[i]) > 0)) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) >= 0) {
								resultDatas[i] = true;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && (s2[i] || Variant.compare(d1[i], d2[i]) >= 0)) {
								resultDatas[i] = true;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && !s1[i] && Variant.compare(d1[i], d2[i]) >= 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s2[i] || (!s1[i] && Variant.compare(d1[i], d2[i]) >= 0))) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) < 0) {
								resultDatas[i] = true;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && !s2[i] && Variant.compare(d1[i], d2[i]) < 0) {
								resultDatas[i] = true;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] || Variant.compare(d1[i], d2[i]) < 0)) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && !s2[i] && (s1[i] || (Variant.compare(d1[i], d2[i]) < 0))) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && Variant.compare(d1[i], d2[i]) <= 0) {
								resultDatas[i] = true;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && !s2[i] && Variant.compare(d1[i], d2[i]) <= 0) {
								resultDatas[i] = true;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] || Variant.compare(d1[i], d2[i]) <= 0)) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] || (!s2[i] && Variant.compare(d1[i], d2[i]) <= 0))) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				if (s1 == null) {
					if (s2 == null) {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && d1[i] != d2[i]) {
								resultDatas[i] = true;
							}
						}
					} else {
						for (int i = 1; i <= size; ++i) {
							if (!resultDatas[i] && (s2[i] || d1[i] != d2[i])) {
								resultDatas[i] = true;
							}
						}
					}
				} else if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] || d1[i] != d2[i])) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] ? !s2[i] : (s2[i] || d1[i] != d2[i]))) {
							resultDatas[i] = true;
						}
					}
				}
			} else {
				throw new RuntimeException();
			}
		}
	}

	private void calcRelations(boolean value, int relation, BoolArray result, boolean isAnd) {
		int size = this.size;
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		boolean []resultDatas = result.getDatas();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && d1[i] != value) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] || d1[i] != value)) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && Variant.compare(d1[i], value) <= 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] || Variant.compare(d1[i], value) <= 0)) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && Variant.compare(d1[i], value) < 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] || Variant.compare(d1[i], value) < 0)) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && Variant.compare(d1[i], value) >= 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && !s1[i] && Variant.compare(d1[i], value) >= 0) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && Variant.compare(d1[i], value) > 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && !s1[i] && Variant.compare(d1[i], value) > 0) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && d1[i] == value) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && !s1[i] && d1[i] == value) {
							resultDatas[i] = false;
						}
					}
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// �������ִ��||����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && d1[i] == value) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && !s1[i] && d1[i] == value) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && Variant.compare(d1[i], value) > 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && !s1[i] && Variant.compare(d1[i], value) > 0) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && Variant.compare(d1[i], value) >= 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && !s1[i] && Variant.compare(d1[i], value) >= 0) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && Variant.compare(d1[i], value) < 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] || Variant.compare(d1[i], value) < 0)) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && Variant.compare(d1[i], value) <= 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] || Variant.compare(d1[i], value) <= 0)) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && d1[i] != value) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] || d1[i] != value)) {
							resultDatas[i] = true;
						}
					}
				}
			} else {
				throw new RuntimeException();
			}
		}
	}

	void calcRelations(ObjectArray array, int relation, BoolArray result, boolean isAnd) {
		int size = this.size;
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		Object []d2 = array.getDatas();
		boolean []resultDatas = result.getDatas();
		
		if (isAnd) {
			// �������ִ��&&����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && compare(d1[i], d2[i]) != 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] ? d2[i] != null : compare(d1[i], d2[i]) != 0)) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && compare(d1[i], d2[i]) <= 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] || compare(d1[i], d2[i]) <= 0)) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && compare(d1[i], d2[i]) < 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] ? d2[i] != null : compare(d1[i], d2[i]) < 0)) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && compare(d1[i], d2[i]) >= 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] ? d2[i] == null : compare(d1[i], d2[i]) >= 0)) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && compare(d1[i], d2[i]) > 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && !s1[i] && compare(d1[i], d2[i]) > 0) {
							resultDatas[i] = false;
						}
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && compare(d1[i], d2[i]) == 0) {
							resultDatas[i] = false;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (resultDatas[i] && (s1[i] ? d2[i] == null : compare(d1[i], d2[i]) == 0)) {
							resultDatas[i] = false;
						}
					}
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			// �������ִ��||����
			if (relation == Relation.EQUAL) {
				// �Ƿ�����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && compare(d1[i], d2[i]) == 0) {
							resultDatas[i] = true;
						}
					}

				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] ? d2[i] == null : compare(d1[i], d2[i]) == 0)) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.GREATER) {
				// �Ƿ�����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && compare(d1[i], d2[i]) > 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && !s1[i] && compare(d1[i], d2[i]) > 0) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.GREATER_EQUAL) {
				// �Ƿ���ڵ����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && compare(d1[i], d2[i]) >= 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (d2[i] == null || (!s1[i] && compare(d1[i], d2[i]) >= 0))) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.LESS) {
				// �Ƿ�С���ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && compare(d1[i], d2[i]) < 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && d2[i] != null && (s1[i] || (compare(d1[i], d2[i]) < 0))) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.LESS_EQUAL) {
				// �Ƿ�С�ڵ����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && compare(d1[i], d2[i]) <= 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] || compare(d1[i], d2[i]) <= 0)) {
							resultDatas[i] = true;
						}
					}
				}
			} else if (relation == Relation.NOT_EQUAL) {
				// �Ƿ񲻵����ж�
				if (s1 == null) {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && compare(d1[i], d2[i]) != 0) {
							resultDatas[i] = true;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!resultDatas[i] && (s1[i] ? d2[i] != null : compare(d1[i], d2[i]) != 0)) {
							resultDatas[i] = true;
						}
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
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		boolean []resultDatas = new boolean[size + 1];
		int resultSize = 0;
		
		if (s1 == null) {
			if (signArray instanceof BoolArray) {
				BoolArray array = (BoolArray)signArray;
				boolean []d2 = array.getDatas();
				boolean []s2 = array.getSigns();
				
				if (s2 == null) {
					for (int i = 1; i <= size; ++i) {
						if (d2[i]) {
							resultDatas[++resultSize] = d1[i];
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (!s2[i] && d2[i]) {
							resultDatas[++resultSize] = d1[i];
						}
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (signArray.isTrue(i)) {
						resultDatas[++resultSize] = d1[i];
					}
				}
			}
			
			return new BoolArray(resultDatas, null, resultSize);
		} else {
			boolean []resultSigns = new boolean[size + 1];
			for (int i = 1; i <= size; ++i) {
				if (signArray.isTrue(i)) {
					++resultSize;
					if (s1[i]) {
						resultSigns[resultSize] = true;
					} else {
						resultDatas[resultSize] = d1[i];
					}
				}
			}
			
			return new BoolArray(resultDatas, resultSigns, resultSize);
		}
	}
	
	/**
	 * ȡĳһ���α�ʶ����ȡֵΪ��������������
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã���������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	public IArray select(int start, int end, IArray signArray) {
		boolean []d1 = this.datas;
		boolean []s1 = this.signs;
		boolean []resultDatas = new boolean[end - start + 1];
		int resultSize = 0;
		
		if (s1 == null) {
			if (signArray instanceof BoolArray) {
				BoolArray array = (BoolArray)signArray;
				boolean []d2 = array.getDatas();
				boolean []s2 = array.getSigns();
				
				if (s2 == null) {
					for (int i = start; i < end; ++i) {
						if (d2[i]) {
							resultDatas[++resultSize] = d1[i];
						}
					}
				} else {
					for (int i = start; i < end; ++i) {
						if (!s2[i] && d2[i]) {
							resultDatas[++resultSize] = d1[i];
						}
					}
				}
			} else {
				for (int i = start; i < end; ++i) {
					if (signArray.isTrue(i)) {
						resultDatas[++resultSize] = d1[i];
					}
				}
			}
			
			return new BoolArray(resultDatas, null, resultSize);
		} else {
			boolean []resultSigns = new boolean[end - start + 1];
			for (int i = start; i < end; ++i) {
				if (signArray.isTrue(i)) {
					++resultSize;
					if (s1[i]) {
						resultSigns[resultSize] = true;
					} else {
						resultDatas[resultSize] = d1[i];
					}
				}
			}
			
			return new BoolArray(resultDatas, resultSigns, resultSize);
		}
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
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		Object []result = new Object[size];
		if (signs == null) {
			for (int i = 1; i <= size; ++i) {
				result[i - 1] = Boolean.valueOf(datas[i]);
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				if (!signs[i]) {
					result[i - 1] = Boolean.valueOf(datas[i]);
				}
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
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		if (signs == null) {
			for (int i = 1; i <= size; ++i) {
				result[i - 1] = Boolean.valueOf(datas[i]);
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				if (!signs[i]) {
					result[i - 1] = Boolean.valueOf(datas[i]);
				}
			}
		}
	}
	
	/**
	 * �������ָ��λ�ò����������
	 * @param pos λ�ã�����
	 * @return ���غ�벿��Ԫ�ع��ɵ�����
	 */
	public IArray split(int pos) {
		int size = this.size;
		boolean []signs = this.signs;
		int resultSize = size - pos + 1;
		boolean []resultDatas = new boolean[resultSize + 1];
		System.arraycopy(datas, pos, resultDatas, 1, resultSize);
		
		if (signs == null) {
			this.size = pos - 1;
			return new BoolArray(resultDatas, null, resultSize);
		} else {
			boolean []resultSigns = new boolean[resultSize + 1];
			System.arraycopy(signs, pos, resultSigns, 1, resultSize);
			for (int i = pos; i <= size; ++i) {
				signs[i] = false;
			}
			
			this.size = pos - 1;
			return new BoolArray(resultDatas, resultSigns, resultSize);
		}
	}
	
	/**
	 * ��ָ������Ԫ�ط���������������
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 * @return
	 */
	public IArray split(int from, int to) {
		int oldSize = this.size;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		int resultSize = to - from + 1;
		boolean []resultDatas = new boolean[resultSize + 1];
		System.arraycopy(datas, from, resultDatas, 1, resultSize);
		
		System.arraycopy(datas, to + 1, datas, from, oldSize - to);
		this.size -= resultSize;
		
		if (signs == null) {
			return new BoolArray(resultDatas, null, resultSize);
		} else {
			boolean []resultSigns = new boolean[resultSize + 1];
			System.arraycopy(signs, from, resultSigns, 1, resultSize);
			System.arraycopy(signs, to + 1, signs, from, oldSize - to);
			return new BoolArray(resultDatas, resultSigns, resultSize);
		}
	}
	
	/**
	 * �������Ԫ�ؽ�������
	 */
	public void sort() {
		int size = this.size;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		
		if (signs == null) {
			int trueCount = 0;
			for (int i = 1; i <= size; ++i) {
				if (datas[i]) {
					trueCount++;
				}
			}
			
			int falseCount = size - trueCount;
			for (int i = 1; i <= falseCount; ++i) {
				datas[i] = false;
			}
			
			for (int i = falseCount + 1; i <= size; ++i) {
				datas[i] = true;
			}
		} else {
			int nullCount = 0;
			int trueCount = 0;
			for (int i = 1; i <= size; ++i) {
				if (signs[i]) {
					signs[i] = false;
					nullCount++;
				} else if (datas[i]) {
					trueCount++;
				}
			}
			
			for (int i = 1; i <= nullCount; ++i) {
				signs[i] = true;
			}
			
			int falseEnd = size - trueCount;
			for (int i = nullCount + 1; i <= falseEnd; ++i) {
				datas[i] = false;
			}
			
			for (int i = falseEnd + 1; i <= size; ++i) {
				datas[i] = true;
			}
		}
	}

	/**
	 * �������Ԫ�ؽ�������
	 * @param comparator �Ƚ���
	 */
	public void sort(Comparator<Object> comparator) {
		int size = this.size;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		Boolean []values = new Boolean[size + 1];
		
		if (signs == null) {
			for (int i = 1; i <= size; ++i) {
				values[i] = Boolean.valueOf(datas[i]);
			}
			
			MultithreadUtil.sort(values, 1, size + 1, comparator);
			
			for (int i = 1; i <= size; ++i) {
				datas[i] = values[i].booleanValue();
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				if (!signs[i]) {
					values[i] = Boolean.valueOf(datas[i]);
				}
			}			
			
			MultithreadUtil.sort(values, 1, size + 1, comparator);
			
			for (int i = 1; i <= size; ++i) {
				if (values[i] != null) {
					datas[i] = values[i].booleanValue();
					signs[i] = false;
				} else {
					signs[i] = true;
				}
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
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		boolean []resultDatas = new boolean[size + 1];
		
		if (signs == null) {
			for (int i = 1, q = size; i <= size; ++i) {
				resultDatas[i] = datas[q--];
			}
			
			return new BoolArray(resultDatas, null, size);
		} else {
			boolean []resultSigns = new boolean[size + 1];
			for (int i = 1, q = size; i <= size; ++i, --q) {
				if (signs[q]) {
					resultSigns[i] = true;
				} else {
					resultDatas[i] = datas[q];
				}
			}
			
			return new BoolArray(resultDatas, resultSigns, size);
		}
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
		throw new RuntimeException();
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

	public void setSize(int size) {
		this.size = size;
	}
	
	/**
	 * �ѵ�ǰ����ת�ɶ������飬�����ǰ�����Ƕ��������򷵻����鱾��
	 * @return ObjectArray
	 */
	public ObjectArray toObjectArray() {
		int size = this.size;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		Object []resultDatas = new Object[size + 1];
		
		if (signs == null) {
			for (int i = 1; i <= size; ++i) {
				resultDatas[i] = Boolean.valueOf(datas[i]);
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				if (!signs[i]) {
					resultDatas[i] = Boolean.valueOf(datas[i]);
				}
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
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		IArray result;
		
		if (other instanceof BoolArray) {
			BoolArray otherArray = (BoolArray)other;
			if (isTemporary()) {
				result = this;
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						if (otherArray.isNull(i)) {
							if (signs == null) {
								signs = new boolean[size + 1];
								this.signs = signs;
							}
							
							signs[i] = true;
						} else {
							datas[i] = otherArray.getBool(i);
							if (signs != null) {
								signs[i] = false;
							}
						}
					}
				}
			} else {
				BoolArray resultArray = new BoolArray(size);
				result = resultArray;
				
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						if (signArray.isTrue(i)) {
							resultArray.pushBool(datas[i]);
						} else if (otherArray.isNull(i)) {
							resultArray.pushNull();
						} else {
							resultArray.pushBool(otherArray.getBool(i));
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (signArray.isTrue(i)) {
							if (signs[i]) {
								resultArray.pushNull();
							} else {
								resultArray.pushBool(datas[i]);
							}
						} else if (otherArray.isNull(i)) {
							resultArray.pushNull();
						} else {
							resultArray.pushBool(otherArray.getBool(i));
						}
					}
				}
			}
		} else {
			Object []resultDatas = new Object[size + 1];
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					if (signArray.isTrue(i)) {
						resultDatas[i] = Boolean.valueOf(datas[i]);
					} else {
						resultDatas[i] = other.get(i);
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (signArray.isTrue(i)) {
						if (!signs[i]) {
							resultDatas[i] = Boolean.valueOf(datas[i]);
						}
					} else {
						resultDatas[i] = other.get(i);
					}
				}
			}
			
			result = new ObjectArray(resultDatas, size);
		}
		
		result.setTemporary(true);
		return result;
	}

	/**
	 * ���������ӵ�ǰ����ѡ����־Ϊtrue�ģ���־Ϊfalse���ó�value
	 * @param signArray ��־����
	 * @param other ֵ
	 * @return IArray
	 */
	public IArray combine(IArray signArray, Object value) {
		int size = this.size;
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		IArray result;
		
		if (value instanceof Boolean) {
			boolean v = ((Boolean)value).booleanValue();
			if (isTemporary()) {
				result = this;
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						if (signArray.isFalse(i)) {
							datas[i] = v;
						}
					}
				} else {
					for (int i = 1; i <= size; ++i) {
						if (signArray.isFalse(i)) {
							datas[i] = v;
							signs[i] = false;
						}
					}
				}
			} else {
				boolean []resultDatas = new boolean[size + 1];
				boolean []resultSigns = null;
				
				if (signs == null) {
					for (int i = 1; i <= size; ++i) {
						if (signArray.isTrue(i)) {
							resultDatas[i] = datas[i];
						} else {
							resultDatas[i] = v;
						}
					}
				} else {
					resultSigns = new boolean[size + 1];
					for (int i = 1; i <= size; ++i) {
						if (signArray.isTrue(i)) {
							if (signs[i]) {
								resultSigns[i] = true;
							} else {
								resultDatas[i] = datas[i];
							}
						} else {
							resultDatas[i] = v;
						}
					}
				}
				
				result = new BoolArray(resultDatas, resultSigns, size);
			}
		} else if (value == null) {
			if (isTemporary()) {
				result = this;
				if (signs == null) {
					signs = new boolean[size + 1];
					this.signs = signs;
				}
				
				for (int i = 1; i <= size; ++i) {
					if (signArray.isFalse(i)) {
						signs[i] = true;
					}
				}
			} else {
				boolean []resultSigns = new boolean[size + 1];
				boolean []resultDatas = new boolean[size + 1];
				
				for (int i = 1; i <= size; ++i) {
					if (signArray.isTrue(i) && (signs == null || !signs[i])) {
						resultDatas[i] = Boolean.valueOf(datas[i]);
					} else {
						resultSigns[i] = true;
					}
				}
				
				result = new BoolArray(resultDatas, resultSigns, size);
			}
		} else {
			Object []resultDatas = new Object[size + 1];
			if (signs == null) {
				for (int i = 1; i <= size; ++i) {
					if (signArray.isTrue(i)) {
						resultDatas[i] = Boolean.valueOf(datas[i]);
					} else {
						resultDatas[i] = value;
					}
				}
			} else {
				for (int i = 1; i <= size; ++i) {
					if (signArray.isTrue(i)) {
						if (!signs[i]) {
							resultDatas[i] = Boolean.valueOf(datas[i]);
						}
					} else {
						resultDatas[i] = value;
					}
				}
			}
			
			result = new ObjectArray(resultDatas, size);
		}
		
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
		if (array instanceof BoolArray) {
			BoolArray boolArray = (BoolArray)array;
			int len = this.size;
			int subLen = boolArray.size;
			if (len < subLen) {
				return null;
			}
			
			boolean isSorted = false, isIncre = false, isContinuous = false;
			if (opt != null) {
				if (opt.indexOf('b') != -1) isSorted = true;
				if (opt.indexOf('i') != -1) isIncre = true;
				if (opt.indexOf('c') != -1) isContinuous = true;
			}

			// Ԫ�����γ�����Դ������
			if (isIncre) {
				IntArray result = new IntArray(subLen);

				if (isSorted) { // Դ��������
					int pos = 1;
					for (int t = 1; t <= subLen; ++t) {
						pos = binarySearch(boolArray.get(t), pos, len);
						if (pos > 0) {
							result.pushInt(pos);
							pos++;
						} else {
							return null;
						}
					}
				} else {
					int pos = 1;
					for (int t = 1; t <= subLen; ++t) {
						pos = firstIndexOf(boolArray.get(t), pos);
						if (pos > 0) {
							result.pushInt(pos);
							pos++;
						} else {
							return null;
						}
					}
				}

				return new Sequence(result);
			} else if (isContinuous) {
				int maxCandidate = len - subLen + 1; // �ȽϵĴ���
				if (isSorted) {
					int candidate = 1;

					// �ҵ���һ����ȵ�Ԫ�ص����
					Next:
					while (candidate <= maxCandidate) {
						int result = compareTo(candidate, boolArray, 1);

						if (result < 0) {
							candidate++;
						} else if (result == 0) {
							for (int i = 2, j = candidate + 1; i <= subLen; ++i, ++j) {
								if (!isEquals(j, boolArray, i)) {
									candidate++;
									continue Next;
								}
							}

							return candidate;
						} else {
							return null;
						}
					}
				} else {
					nextCand:
					for (int candidate = 1; candidate <= maxCandidate; ++candidate) {
						for (int i = 1, j = candidate; i <= subLen; ++i, ++j) {
							if (!isEquals(j, boolArray, i)) {
								continue nextCand;
							}
						}

						return candidate;
					}
				}

				return null;
			} else {
				IntArray result = new IntArray(subLen);
				int pos;
				
				if (isSorted) { // Դ��������
					for (int t = 1; t <= subLen; ++t) {
						pos = binarySearch(boolArray.get(t));
						if (pos > 0) {
							result.pushInt(pos);
						} else {
							return null;
						}
					}
				} else {
					for (int t = 1; t <= subLen; ++t) {
						pos = firstIndexOf(boolArray.get(t), 1);
						if (pos > 0) {
							result.pushInt(pos);
						} else {
							return null;
						}
					}
				}

				return new Sequence(result);
			}
		} else {
			return ArrayUtil.pos(this, array, opt);
		}
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
	
	public boolean hasSigns() {
		return signs != null;
	}
	
	/**
	 * ȡָ��λ��������ͬ��Ԫ������
	 * @param index λ��
	 * @return ������ͬ��Ԫ������
	 */
	public int getNextEqualCount(int index) {
		boolean []datas = this.datas;
		boolean []signs = this.signs;
		int size = this.size;
		int count = 1;
		
		if (signs == null) {
			if (datas[index]) {
				for (++index; index <= size; ++index) {
					if (datas[index]) {
						count++;
					} else {
						break;
					}
				}
			} else {
				for (++index; index <= size; ++index) {
					if (!datas[index]) {
						count++;
					} else {
						break;
					}
				}
			}
		} else {
			if (signs[index]) {
				for (++index; index <= size; ++index) {
					if (signs[index]) {
						count++;
					} else {
						break;
					}
				}
			} else if (datas[index]) {
				for (++index; index <= size; ++index) {
					if (!signs[index] && datas[index]) {
						count++;
					} else {
						break;
					}
				}
			} else {
				for (++index; index <= size; ++index) {
					if (!signs[index] && !datas[index]) {
						count++;
					} else {
						break;
					}
				}
			}
		}
		
		return count;
	}
}
