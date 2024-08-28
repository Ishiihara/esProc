package com.scudata.expression.fn.gather;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.array.LongArray;
import com.scudata.array.ObjectArray;
import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.HashLinkSet;
import com.scudata.dm.ObjectWriter;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.MultithreadUtil;
import com.scudata.util.HashUtil;
import com.scudata.util.Variant;


/**
 * ȡ���ظ���Ԫ�ظ�����ȥ��ȡֵΪfalse��Ԫ��
 * icount(x1,��)
 * @author RunQian
 *
 */
public class ICount extends Gather {
	private Expression exp; // ���ʽ
	private boolean isSorted = false; // �����Ƿ񰴱��ʽ����
	private boolean optB = false; // ʹ��λģʽ
	private int maxSize = 0;
	
	// ʹ��������icount
	public static class ICountFile {
		private final int MAX_SIZE;
		private IArray elementArray; // ��ϣ���ŵ���Ԫ�ص�λ�ã���Ҫ����λ�õ�Դ��ȡԪ��
		private int []linkArray; // ��ϣֵ��ͬ�ļ�¼����
		private HashUtil hashUtil; // ���ڼ����ϣֵ
		private int []entries; // ��ϣ������Ź�ϣֵ��Ӧ�����һ����¼��λ��
		private ArrayList<FileObject> fileList = new ArrayList<FileObject>();
		
		public ICountFile(int maxSize) {
			this.MAX_SIZE = maxSize;
			elementArray = new ObjectArray(maxSize);
			linkArray = new int[maxSize + 1];
			hashUtil = new HashUtil(maxSize);
			entries = new int[hashUtil.getCapacity()];
		}
		
		public ICountFile(IArray valueArray, int maxSize) {
			this.MAX_SIZE = maxSize;
			elementArray = valueArray.newInstance(maxSize);
			linkArray = new int[maxSize + 1];
			hashUtil = new HashUtil(maxSize);
			entries = new int[hashUtil.getCapacity()];
		}
		
		public void addValue(Object value) {
			int []entries = this.entries;
			int hash = hashUtil.hashCode(value);
			int seq = entries[hash];
			
			while (seq != 0) {
				if (elementArray.isEquals(seq, value)) {
					return;
				} else {
					seq = linkArray[seq];
				}
			}
			
			int count = elementArray.size();
			if (count == MAX_SIZE) {
				Object []values = elementArray.toArray();
				MultithreadUtil.sort(values);
				FileObject fo = FileObject.createTempFileObject();
				fileList.add(fo);
				ObjectWriter writer = new ObjectWriter(fo.getOutputStream(false));
				
				try {
					writer.writeInt(count);
					for (Object obj : values) {
						writer.writeObject(obj);
					}
				} catch (IOException e) {
					throw new RQException(e.getMessage(), e);
				} finally {
					try {
						writer.close();
					} catch (IOException e) {
						throw new RQException(e.getMessage(), e);
					}
				}
				
				elementArray.clear();
				for (int i = 0, size = entries.length; i < size; ++i) {
					entries[i] = 0;
				}
				
				int []linkArray = this.linkArray;
				for (int i = 1, size = MAX_SIZE; i <= size; ++i) {
					linkArray[i] = 0;
				}
				
				elementArray.push(value);
				entries[hash] = 1;
			} else {
				elementArray.push(value);
				linkArray[count + 1] = entries[hash];
				entries[hash] = count + 1;
			}
		}
		
		public void addAll(ICountFile cf) {
			fileList.addAll(cf.fileList);
			
			IArray elementArray = this.elementArray;
			int size1 = elementArray.size();
			if (size1 == 0) {
				this.elementArray = cf.elementArray;
				this.linkArray = cf.linkArray;
				this.entries = cf.entries;
				return;
			}
			
			IArray elementArray2 = cf.elementArray;
			int size2 = elementArray2.size();
			if (size2 == 0) {
				return;
			}
			
			int []entries = this.entries;
			int []linkArray = this.linkArray;
			int []entries2 = cf.entries;
			int []linkArray2 = cf.linkArray;

			int oldCapacity = MAX_SIZE;
			int newCapacity = MAX_SIZE;
			int totalCount = size1;
			
			for (int h = 0, capacity = entries.length; h < capacity; ++h) {
				int seq1 = entries[h];
				
				Next:
				for (int seq2 = entries2[h]; seq2 != 0; seq2 = linkArray2[seq2]) {
					for (int q = seq1; q != 0; q = linkArray[q]) {
						if (elementArray.isEquals(q, elementArray2, seq2)) {
							continue Next;
						}
					}
					
					if (newCapacity > oldCapacity) {
						elementArray.push(elementArray2, seq2);
					} else {
						// �Ҳ�set�ĳ�Ա���ڵ�ǰset��
						totalCount++;
						if (totalCount <= newCapacity) {
							elementArray.push(elementArray2, seq2);
							linkArray[totalCount] = entries[h];
							entries[h] = totalCount;
						} else {
							newCapacity = size1 + size2;
							elementArray.ensureCapacity(newCapacity);
							elementArray.push(elementArray2, seq2);
						}
					}
				}
			}
			
			if (newCapacity > oldCapacity) {
				Object []values = elementArray.toArray();
				MultithreadUtil.sort(values);
				FileObject fo = FileObject.createTempFileObject();
				fileList.add(fo);
				ObjectWriter writer = new ObjectWriter(fo.getOutputStream(false));
				
				try {
					writer.writeInt(size1);
					for (Object obj : values) {
						writer.writeObject(obj);
					}
				} catch (IOException e) {
					throw new RQException(e.getMessage(), e);
				} finally {
					try {
						writer.close();
					} catch (IOException e) {
						throw new RQException(e.getMessage(), e);
					}
				}
				
				this.elementArray = elementArray2;
				this.elementArray.clear();
				for (int i = 0, size = entries.length; i < size; ++i) {
					entries[i] = 0;
				}
				
				for (int i = 1, size = MAX_SIZE; i <= size; ++i) {
					linkArray[i] = 0;
				}
			}
		}
		
		public void add(Object value) {
			if (value instanceof ICountFile) {
				addAll((ICountFile)value);
			} else if (value != null) {
				addValue(value);
			}
		}
		
		public void add(IArray array, int index) {
			IArray elementArray = this.elementArray;
			int []entries = this.entries;
			int hash = hashUtil.hashCode(array.hashCode(index));
			int seq = entries[hash];
			
			while (seq != 0) {
				if (elementArray.isEquals(seq, array, index)) {
					return;
				} else {
					seq = linkArray[seq];
				}
			}
			
			int count = elementArray.size();
			if (count == MAX_SIZE) {
				Object []values = elementArray.toArray();
				MultithreadUtil.sort(values);
				FileObject fo = FileObject.createTempFileObject();
				fileList.add(fo);
				ObjectWriter writer = new ObjectWriter(fo.getOutputStream(false));
				
				try {
					writer.writeInt(count);
					for (Object obj : values) {
						writer.writeObject(obj);
					}
				} catch (IOException e) {
					throw new RQException(e.getMessage(), e);
				} finally {
					try {
						writer.close();
					} catch (IOException e) {
						throw new RQException(e.getMessage(), e);
					}
				}
				
				elementArray.clear();
				for (int i = 0, size = entries.length; i < size; ++i) {
					entries[i] = 0;
				}
				
				int []linkArray = this.linkArray;
				for (int i = 1, size = MAX_SIZE; i <= size; ++i) {
					linkArray[i] = 0;
				}
				
				elementArray.push(array, index);
				entries[hash] = 1;
			} else {
				elementArray.push(array, index);
				linkArray[count + 1] = entries[hash];
				entries[hash] = count + 1;
			}
		}
		
		public long result() {
			int fileCount = fileList.size();
			if (fileCount == 0) {
				return elementArray.size();
			}
			
			Object []objs = elementArray.toArray();
			MultithreadUtil.sort(objs);
			IValues []valuesArray = new IValues[fileCount + 1];
			valuesArray[0] = new Values(objs);
			
			for (int i = 0; i < fileCount; ++i) {
				valuesArray[i + 1] = new FileValues(fileList.get(i));
			}
			
			long result = count(valuesArray);
			return result;
		}
		
		private static long count(IValues []valuesArray, int path, Object value) {
			if (valuesArray[path] == null) {
				return 0;
			}
			
			long result = 0;
			Object curValue = valuesArray[path].getTop();
			int pathCount = valuesArray.length;
			int nextPath = path + 1;
			
			while (curValue != null) {
				int cmp = Variant.compare(value, curValue, true);
				if (cmp < 0) {
					if (nextPath < valuesArray.length) {
						return result + count(valuesArray, nextPath, value);
					} else {
						return result;
					}
				} else if (cmp == 0) {
					valuesArray[path].pop();
					if (nextPath < valuesArray.length) {
						return result + count(valuesArray, nextPath, value);
					} else {
						return result;
					}
				} else {
					result++;
					if (nextPath < valuesArray.length) {
						result += count(valuesArray, nextPath, curValue);
					}
					
					valuesArray[path].pop();
					curValue = valuesArray[path].getTop();
				}
			}
			
			pathCount--;
			if (path < pathCount) {
				System.arraycopy(valuesArray, path + 1, valuesArray, path, pathCount - path);
				valuesArray[pathCount] = null;
				return result + count(valuesArray, path, value);
			} else {
				valuesArray[pathCount] = null;
				return result;
			}
		}
		
		private static long count(IValues []valuesArray) {
			int pathCount = valuesArray.length;
			long result = 0;
			while (true) {
				Object value = valuesArray[0].pop();
				if (value != null) {
					result += count(valuesArray, 1, value) + 1;
				} else if (valuesArray[1] != null){
					pathCount--;
					System.arraycopy(valuesArray, 1, valuesArray, 0, pathCount);
					valuesArray[pathCount] = null;
				} else {
					break;
				}
			}
			
			return result;
		}
	}
	
	// ����icount���м�����Ϣ
	public static class ICountInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private int count;
		private Object startValue;
		private Object endValue;
		
		public ICountInfo() {
		}
		
		public ICountInfo(Object startValue) {
			if (startValue != null) {
				count = 1;
				this.startValue = startValue;
				this.endValue = startValue;
			}
		}
		
		public void put(Object value) {
			if (value instanceof ICountInfo) {
				ICountInfo next = (ICountInfo)value;
				if (count == 0) {
					count = next.count;
					startValue = next.startValue;
					endValue = next.endValue;
				} else if (next.count != 0) {
					if (Variant.isEquals(endValue, next.startValue)) {
						count += next.count - 1;
					} else {
						count += next.count;
					}
					
					endValue = next.endValue;
				}
			} else if (value != null) {
				if (endValue == null) {
					// ǰ��Ķ��ǿ�
					startValue = value;
					endValue = value;
					count = 1;
				} else if (!Variant.isEquals(endValue, value)) {
					count++;
					endValue = value;
				}
			}
		}
	}

	//û��ʹ�ã��ȷ�������
	public static class ICountHashSet implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private static final int INIT_SIZE = 64;
		private IArray elementArray; // ��ϣ���ŵ���Ԫ�ص�λ�ã���Ҫ����λ�õ�Դ��ȡԪ��
		private HashUtil hashUtil; // ���ڼ����ϣֵ
		private int count = 0;// ��ǰԪ�ظ���
		private int []entries; // ��ϣ������Ź�ϣֵ��Ӧ�����һ����¼��λ��
		private IntArray linkArray; // ��ϣֵ��ͬ�ļ�¼����
		
		public ICountHashSet(IArray src) {
			hashUtil = new HashUtil(INIT_SIZE);
			entries = new int[hashUtil.getCapacity()];
			linkArray = new IntArray(INIT_SIZE);
			elementArray = src.newInstance(INIT_SIZE);
		}
		
		public void add(IArray array, int index) {
			if (array.isNull(index)) {
				return;
			}
			
			IArray elementArray = this.elementArray;
			int[] linkArray = this.linkArray.getDatas();
			int[] entries = this.entries;
			int hash = hashUtil.hashCode(array.hashCode(index));
			int seq = entries[hash];
			while (seq != 0) {
				if (elementArray.isEquals(seq, array, index)) {
					return;
				} else {
					seq = linkArray[seq];
				}
			}
			
			count++;
			int count = this.count;
			if (count == linkArray.length) {
				this.linkArray.setSize(count - 1);
				elementArray.ensureCapacity(count);
				this.linkArray.ensureCapacity(count);
				linkArray = this.linkArray.getDatas();
			}
			
			elementArray.push(array, index);
			linkArray[count] = entries[hash];
			entries[hash] = count;
		}
		
		public void addInt(int key) {
			int[] elementArray = ((IntArray) this.elementArray).getDatas();
			int[] linkArray = this.linkArray.getDatas();
			int[] entries = this.entries;
			int hash = hashUtil.hashCode(key);//key % INIT_SIZE;//
			int seq = entries[hash];
			
			while (seq != 0) {
				if (elementArray[seq] == key) {
					return;
				} else {
					seq = linkArray[seq];
				}
			}
			
			count++;
			int count = this.count;
			if (count == linkArray.length) {
				this.elementArray.setSize(count - 1);
				this.linkArray.setSize(count - 1);
				this.elementArray.ensureCapacity(count);
				this.linkArray.ensureCapacity(count);
				elementArray = ((IntArray) this.elementArray).getDatas();
				linkArray = this.linkArray.getDatas();
			}
			
			elementArray[count] = key;
			linkArray[count] = entries[hash];
			entries[hash] = count;
		}
		
		public void addAll(IArray array) {
			if (elementArray == null) {
				elementArray = array.newInstance(INIT_SIZE);
			}
			
			IArray elementArray = this.elementArray;
			int[] linkArray = this.linkArray.getDatas();
			int[] entries = this.entries;
			HashUtil hashUtil = this.hashUtil;
			int count = this.count;
			
			for (int i = 1, len = array.size(); i <= len; i++) {
				if (array.isNull(i)) {
					continue;
				}
				
				int hash = hashUtil.hashCode(array.hashCode(i));
				int seq = entries[hash];
				boolean find = false;
				while (seq != 0) {
					if (elementArray.isEquals(seq, array, i)) {
						find = true;
						break;
					} else {
						seq = linkArray[seq];
					}
				}
				
				if (find) continue;
				
				count++;
				if (count == linkArray.length) {
					elementArray.ensureCapacity(count);
					this.linkArray.setSize(count - 1);
					this.linkArray.ensureCapacity(count);
					linkArray = this.linkArray.getDatas();
				}
				
				elementArray.push(array, i);
				linkArray[count] = entries[hash];
				entries[hash] = count;
			}
			
			this.count = count;
		}
		
		public int size() {
			return count;
		}
	}
	
	//��bitλ�ж��Ƿ��ظ�
	public static class ICountBitSet implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private static final int INIT_BIT_SIZE = 1024;
		private int count = 0;
		private long[] bitArray;
		
		public ICountBitSet() {
			bitArray = new long[INIT_BIT_SIZE];
		}
		
		public ICountBitSet(long[] bitArray) {
			this.bitArray = bitArray;
			count = countBit(bitArray);
		}
		
		public boolean add(int num) {
			//IntArray elementArray = this.elementArray;
			int idx = (num / 64);
			long bit = (1L << (num % 64));
			
			long[] bitArray = this.bitArray;
			if (idx >= bitArray.length) {
				int newSize = idx + idx / 3;
				long[] newBitArray = new long[newSize];
				System.arraycopy(bitArray, 0, newBitArray, 0, bitArray.length);
				bitArray = this.bitArray = newBitArray;
			}
			
			long cur = bitArray[idx];
			if ((cur & bit) != 0) {
				return false;
			} else {
				bitArray[idx] = cur | bit;
				count++;
			}
			
			return true;
		}
		
		public boolean add(IArray array, int index) {
			if (array.isNull(index)) {
				return false;
			}
			
			int num = array.getInt(index);
			int idx = (num / 64);
			long bit = (1L << (num % 64));
			
			long[] bitArray = this.bitArray;
			if (idx >= bitArray.length) {
				int newSize = idx + idx / 3;
				long[] newBitArray = new long[newSize];
				System.arraycopy(bitArray, 0, newBitArray, 0, bitArray.length);
				bitArray = this.bitArray = newBitArray;
			}
			
			long cur = bitArray[idx];
			if ((cur & bit) != 0) {
				return false;
			} else {
				bitArray[idx] = cur | bit;
				count++;;
			}
			
			return true;
		}
		
		public static int countBit(long[] bitArray) {
			int count = 0;
			for (long i : bitArray) {
				i = i - ((i >>> 1) & 0x5555555555555555L);
		        i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
		        i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
		        i = i + (i >>> 8);
		        i = i + (i >>> 16);
		        i = i + (i >>> 32);
				count += (int)i & 0x7f;
			}
			return count;
		}
		
		public int size() {
			return count;
		}

		public void addAll(long[] newBits) {
			long[] curBits = bitArray;
			int newLen = newBits.length;
			int curLen = curBits.length;
			
			if (curLen >= newLen) {
				for (int i = 0; i <= newLen; i++) {
					curBits[i] |= newBits[i];
				}
			} else {
				long[] temp = new long[newLen];
				System.arraycopy(newBits, curLen, temp, curLen, newLen - curLen);
				for (int i = 0; i <= curLen; i++) {
					temp[i] = curBits[i] | newBits[i];
				}
				bitArray = temp;
			}
			count = countBit(bitArray);
		}
		
		public void addAll(ICountBitSet set) {
			long[] newBits = set.bitArray;
			addAll(newBits);
		}
	}
	
	//��λ���ж��Ƿ��ظ�
	public static class ICountPositionSet implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private static final int INIT_SIZE = 65536;
		private int count = 0;
		private boolean[] posArray;
		
		public ICountPositionSet() {
			posArray = new boolean[INIT_SIZE];
		}
		
		public boolean add(int num) {
			boolean[] posArray = this.posArray;
			if (num >= posArray.length) {
				int newSize = num + num / 3;
				boolean[] newPosArray = new boolean[newSize];
				System.arraycopy(posArray, 0, newPosArray, 0, posArray.length);
				posArray = this.posArray = newPosArray;
			}
			
			if (posArray[num]) {
				return false;
			} else {
				posArray[num] = true;
				count++;
			}
			
			return true;
		}
		
		public boolean add(IArray array, int index) {
			int num = array.getInt(index);
			
			boolean[] posArray = this.posArray;
			if (num >= posArray.length) {
				int newSize = num + num / 3;
				boolean[] newPosArray = new boolean[newSize];
				System.arraycopy(posArray, 0, newPosArray, 0, posArray.length);
				posArray = this.posArray = newPosArray;
			}
			
			if (posArray[num]) {
				return false;
			} else {
				posArray[num] = true;
				count++;;
			}
			
			return true;
		}
		
		
		public int size() {
			return count;
		}
	}
	
	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("icount" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (obj instanceof Sequence) {
				return ((Sequence)obj).icount(option);
			} else {
				if (Variant.isTrue(obj)) {
					return ObjectCache.getInteger(1);
				} else {
					return ObjectCache.getInteger(0);
				}
			}
		}

		int size = param.getSubSize();
		HashSet<Object> set = new HashSet<Object>(size);
		for (int i = 0; i < size; ++i) {
			IParam sub = param.getSub(i);
			if (sub != null) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (Variant.isTrue(obj)) {
					set.add(obj);
				}
			}
		}

		return set.size();
	}
	
	public void prepare(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("icount" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			exp = param.getLeafExpression();
		} else if (param.getSubSize() == 2) {
			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			if (sub0 == null || sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("icount" + mm.getMessage("function.invalidParam"));
			}
			
			exp = sub0.getLeafExpression();
			Object value = sub1.getLeafExpression().calculate(ctx);
			if (value instanceof Number) {
				maxSize = ((Number)value).intValue();
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("icount" + mm.getMessage("function.paramTypeError"));
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("icount" + mm.getMessage("function.invalidParam"));
		}
		
		isSorted = option != null && option.indexOf('o') != -1;
		optB = option != null && option.indexOf('b') != -1;
	}

	/**
	 * ��һ����¼����������ݣ���ӵ���ʱ�м�����
	 */
	public Object gather(Context ctx) {
		// ���ݰ�icount�ֶ�����
		if (isSorted) {
			Object val = exp.calculate(ctx);
			if (val instanceof ICountInfo){
				return val;
			} else {
				return new ICountInfo(val);
			}
		}

		Object val = exp.calculate(ctx);
		if (val instanceof HashSet || val instanceof ICountFile) {
			return val;
		} else if (val instanceof Sequence){
			Sequence seq = (Sequence)val;
			int len = seq.length();
			HashSet<Object> set = new HashSet<Object>(len + 8);
			for (int i = 1; i <= len; ++i) {
				set.add(seq.getMem(i));
			}
			
			return set;
		} else if (val != null) {
			if (maxSize > 0) {
				ICountFile icf = new ICountFile(maxSize);
				icf.add(val);
				return icf;
			} else {
				HashSet<Object> set = new HashSet<Object>();
				set.add(val);
				return set;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * �������������ϵ���ʱ�м�����
	 * 		
	 * @param	oldValue	�ϵ�����(������null�����Ϊ��ϣ)
	 * @param	ctx			�����ı���
	 */
	public Object gather(Object oldValue, Context ctx) {
		Object val = exp.calculate(ctx);
		if (val == null) {
			return oldValue;
		}
		
		// ���ݰ�icount�ֶ�����
		if (isSorted) {
			((ICountInfo)oldValue).put(val);
			return oldValue;
		}
		
		if (oldValue instanceof HashSet) {
			HashSet<Object> set = ((HashSet<Object>)oldValue);
			if (val instanceof HashSet) {
				set.addAll((HashSet<Object>)val);
			} else if (val instanceof Sequence){
				Sequence seq = (Sequence)val;
				int len = seq.length();
				for (int i = 1; i <= len; ++i) {
					set.add(seq.getMem(i));
				}
			} else {
				set.add(val);
			}
			
			return oldValue;
		} else if (oldValue instanceof ICountFile) {
			ICountFile icf = (ICountFile)oldValue;
			icf.add(val);
			return icf;
		} else { // oldValue == null
			if (val instanceof HashSet || val instanceof ICountFile) {
				return val;
			} else if (val instanceof Sequence){
				Sequence seq = (Sequence)val;
				int len = seq.length();
				HashSet<Object> set = new HashSet<Object>(len + 8);
				for (int i = 1; i <= len; ++i) {
					set.add(seq.getMem(i));
				}
				
				return set;
			} else {
				if (maxSize > 0) {
					ICountFile icf = new ICountFile(maxSize);
					icf.add(val);
					return icf;
				} else {
					HashSet<Object> set = new HashSet<Object>();
					set.add(val);
					return set;
				}
			}
		}
	}
	
	/**
	 * ȡ���λ���ʱ�þۺ��ֶζ�Ӧ�ı��ʽ
	 * @param q	��ǰ�����ֶε����
	 * @return	���ܱ��ʽ
	 */
	public Expression getRegatherExpression(int q) {
		if (isSorted) {
			String str = "icount@o(#" + q + ")";
			return new Expression(str);
		} else if (maxSize > 0) {
			String str = "icount(#" + q + "," + maxSize + ")";
			return new Expression(str);
		} else {
			String str = "icount(#" + q + ")";
			return new Expression(str);
		}
	}
	
	/**
	 * �Ƿ���Ҫ�����м���ʱ���ݡ�
	 */
	public boolean needFinish1() {
		return true;
	}
	
	/**
	 * �Ƿ���Ҫ�����м�����ͳ���������ս��
	 */
	public boolean needFinish() {
		return true;
	}
	
	public IArray finish1(IArray array) {
		if (optB) {
			return finish1_b(array);
		} else {	
			return array;
		}
	}

	/**
	 * ���ڴ��е��м�����ת���ɴ������С�
	 * 		
	 * @param	val	��ת��������
	 * @return	����ת����Ľ��
	 */
	public Object finish1(Object val) {
		if (val instanceof HashSet) {
			HashSet<Object> set = (HashSet<Object>)val;
			Sequence seq = new Sequence(set.size());
			
			Iterator<Object> iter = set.iterator();
			while (iter.hasNext()) {
				seq.add(iter.next());
			}
			
			return seq;
		} else {
			return val;
		}
	}
	
	/**
	 * �Է�������õ��Ļ����н������մ���
	 * @param array �����е�ֵ
	 * @return IArray
	 */
	public IArray finish(IArray array) {
		if (optB) {
			return finish_b(array);
		}
		
		int size = array.size();
		if (isSorted) {
			IntArray result = new IntArray(size);
			for (int i = 1; i <= size; ++i) {
				ICountInfo val = (ICountInfo)array.get(i);
				result.pushInt(val.count);
			}
			
			return result;
		} else if (maxSize > 0) {
			LongArray result = new LongArray(size);
			for (int i = 1; i <= size; ++i) {
				Object val = array.get(i);
				if (val instanceof ICountFile) {
					result.pushLong(((ICountFile)val).result());
				} else {
					result.pushLong(0);
				}
			}
			
			return result;
		} else {
			IntArray result = new IntArray(size);
			for (int i = 1; i <= size; ++i) {
				Object val = array.get(i);
				if (val instanceof HashLinkSet) {
					result.pushInt(((HashLinkSet)val).size());
				} else if (val instanceof Sequence) {
					result.pushInt(((Sequence)val).length());
				} else {
					result.pushInt(0);
				}
			}
			
			return result;
		}
	}
	
	/**
	 * ͳ����ʱ�м����ݣ��������ս����
	 */
	public Object finish(Object val) {
		if (val instanceof ICountInfo) {
			return ((ICountInfo)val).count;
		} else if (val instanceof HashSet) {
			return ObjectCache.getInteger(((HashSet<Object>)val).size());
		} else if (val instanceof Sequence) {
			return ObjectCache.getInteger(((Sequence)val).length());
		} else if (val instanceof ICountFile) {
			return ((ICountFile)val).result();
		} else {
			return ObjectCache.getInteger(0);
		}
	}
	
	public Expression getExp() {
		return exp;
	}
	
	public boolean isSorted() {
		return isSorted;
	}

	/**
	 * �������м�¼��ֵ�����ܵ����������
	 * @param result �������
	 * @param resultSeqs ÿ����¼��Ӧ�Ľ����������
	 * @param ctx ����������
	 * @return IArray �������
	 */
	public IArray gather(IArray result, int []resultSeqs, Context ctx) {
		if (optB) {
			return gather_b(result, resultSeqs, ctx);
		}
		
		IArray array = exp.calculateAll(ctx);
		if (result == null) {
			result = new ObjectArray(Env.INITGROUPSIZE);
		}
		
		if (isSorted) {
			for (int i = 1, len = array.size(); i <= len; ++i) {
				Object val = array.get(i);
				if (result.size() < resultSeqs[i]) {
					if (val instanceof ICountInfo){
						result.add(val);
					} else {
						result.add(new ICountInfo(val));
					}
				} else {
					ICountInfo oldValue = (ICountInfo)result.get(resultSeqs[i]);
					oldValue.put(val);
				}
			}
		} else if (maxSize > 0) {
			int maxSize = this.maxSize;
			if (array instanceof ObjectArray) {
				for (int i = 1, size = array.size(); i <= size; ++i) {
					Object val = array.get(i);
					if (result.size() < resultSeqs[i]) {
						if (val instanceof ICountFile){
							result.add(val);
						} else if (val != null) {
							ICountFile icf = new ICountFile(maxSize);
							icf.addValue(val);
							result.add(icf);
						} else {
							result.add(new ICountFile(maxSize));
						}
					} else {
						ICountFile icf = (ICountFile)result.get(resultSeqs[i]);
						icf.add(val);
					}
				}
			} else {
				for (int i = 1, size = array.size(); i <= size; ++i) {
					if (result.size() < resultSeqs[i]) {
						ICountFile icf = new ICountFile(maxSize);
						if (!array.isNull(i)) {
							icf.add(array, i);
						}
						
						result.add(icf);
					} else if (!array.isNull(i)) {
						ICountFile icf = (ICountFile)result.get(resultSeqs[i]);
						icf.add(array, i);
					}
				}
			}
		} else {
			if (array instanceof ObjectArray) {
				for (int i = 1, size = array.size(); i <= size; ++i) {
					Object val = array.get(i);
					if (result.size() < resultSeqs[i]) {
						if (val instanceof HashLinkSet){
							result.add(val);
						} else if (val instanceof Sequence) {
							Sequence seq = (Sequence)val;
							IArray datas = seq.getMems();
							HashLinkSet set = new HashLinkSet(array);
							set.putAll(datas);
							
							result.add(set);
						} else if (val != null) {
							HashLinkSet set = new HashLinkSet(array);
							set.put(val);
							result.add(set);
						} else {
							result.add(new HashLinkSet(array));
						}
					} else {
						HashLinkSet set = (HashLinkSet)result.get(resultSeqs[i]);
						if (val instanceof HashLinkSet) {
							set.putAll((HashLinkSet)val);
						} else if (val instanceof Sequence){
							Sequence seq = (Sequence)val;
							IArray datas = seq.getMems();
							set.putAll(datas);
						} else if (val != null) {
							set.put(val);
						}
					}
				}
			} else {
				for (int i = 1, size = array.size(); i <= size; ++i) {
					if (result.size() < resultSeqs[i]) {
						HashLinkSet set = new HashLinkSet(array);
						if (!array.isNull(i)) {
							set.put(array, i);
						}
						
						result.add(set);
					} else if (!array.isNull(i)) {
						HashLinkSet set = ((HashLinkSet)result.get(resultSeqs[i]));
						set.put(array, i);
					}
				}
			}
		}
		
		return result;
	}
	
	private IArray finish1_b(IArray array) {
		int size = array.size();
		if (!isSorted) {
			for (int i = 1; i <= size; ++i) {
				Object val = array.get(i);
				if (val instanceof ICountBitSet) {
					ICountBitSet set = (ICountBitSet)val;
					array.set(i, set.bitArray);
				}
			}
		}
		
		return array;
	}
	
	private IArray finish_b(IArray array) {
		int size = array.size();
		IntArray result = new IntArray(size);
		
		for (int i = 1; i <= size; ++i) {
			Object val = array.get(i);
			if (val instanceof ICountBitSet) {
				result.pushInt(((ICountBitSet)val).size());
			} else if (val instanceof long[]) {
				result.pushInt(ICountBitSet.countBit((long[]) val));
			} else {
				result.pushInt(0);
			}
		}
		return result;
	}
	
	private IArray gather_b(IArray result, int []resultSeqs, Context ctx) {
		IArray array = exp.calculateAll(ctx);
		if (result == null) {
			result = new ObjectArray(Env.INITGROUPSIZE);
		}
		
		if (array instanceof IntArray && ((IntArray)array).getSigns() == null) {
			for (int i = 1, size = array.size(); i <= size; ++i) {
				if (result.size() < resultSeqs[i]) {
					ICountBitSet set = new ICountBitSet();
					set.add(array, i);
					result.add(set);
				} else {
					Object oldValue = result.get(resultSeqs[i]);
					if (oldValue == null) {
						ICountBitSet set = new ICountBitSet();
						set.add(array, i);
						result.set(resultSeqs[i], set);
					} else {
						ICountBitSet set = ((ICountBitSet)oldValue);
						set.add(array, i);
					}
				}
			}
		
		} else {
			for (int i = 1, size = array.size(); i <= size; ++i) {
				Object val = array.get(i);
				if (result.size() < resultSeqs[i]) {
					if (val instanceof ICountBitSet){
						result.add(val);
					} else if (val instanceof long[]) {
						long[] seq = (long[])val;
						ICountBitSet set = new ICountBitSet(seq);
						result.add(set);
					} else if (val != null) {
						ICountBitSet set = new ICountBitSet();
						set.add((Integer) val);
						result.add(set);
					} else {
						result.add(null);
					}
				} else {
					Object oldValue = result.get(resultSeqs[i]);
					if (oldValue == null) {
						if (val instanceof ICountBitSet) {
							oldValue = val;
						} else if (val instanceof long[]){
							long[] seq = (long[])val;
							ICountBitSet set = new ICountBitSet(seq);
							
							oldValue = set;
						} else if (val != null) {
							ICountBitSet set = new ICountBitSet();
							set.add((Integer) val);
							oldValue = set;
						}
					} else {
						ICountBitSet set = ((ICountBitSet)oldValue);
						if (val instanceof ICountBitSet) {
							set.addAll((ICountBitSet)val);
						} else if (val instanceof long[]){
							long[] seq = (long[])val;
							set.addAll(seq);
						} else if (val != null) {
							set.add((Integer) val);
						}
					}
					
					result.set(resultSeqs[i], oldValue);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * ���̷߳���Ķ��λ�������
	 * @param result һ���̵߳ķ�����
	 * @param result2 ��һ���̵߳ķ�����
	 * @param seqs ��һ���̵߳ķ������һ���̷߳���Ķ�Ӧ��ϵ
	 * @param ctx ����������
	 * @return
	 */
	public void gather2(IArray result, IArray result2, int []seqs, Context ctx) {
		if (optB) {
			for (int i = 1, len = result2.size(); i <= len; ++i) {
				if (seqs[i] != 0) {
					ICountBitSet value1 = (ICountBitSet) result.get(seqs[i]);
					ICountBitSet value2 = (ICountBitSet) result2.get(i);
					value1.addAll(value2);
				}
			}
		} else if (maxSize > 0) {
			for (int i = 1, len = result2.size(); i <= len; ++i) {
				if (seqs[i] != 0) {
					ICountFile value1 = (ICountFile) result.get(seqs[i]);
					value1.addAll((ICountFile)result2.get(i));
				}
			}
		} else if (!isSorted) {
			for (int i = 1, len = result2.size(); i <= len; ++i) {
				if (seqs[i] != 0) {
					HashLinkSet value1 = (HashLinkSet) result.get(seqs[i]);
					value1.putAll((HashLinkSet)result2.get(i));
				}
			}
		} else {
			for (int i = 1, len = result2.size(); i <= len; ++i) {
				if (seqs[i] != 0) {
					ICountInfo value1 = (ICountInfo) result.get(seqs[i]);
					ICountInfo value2 = (ICountInfo) result2.get(i);
					value1.put(value2);
				}
			}
		}
	}
}
