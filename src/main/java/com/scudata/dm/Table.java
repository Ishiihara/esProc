package com.scudata.dm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import com.scudata.array.IArray;
import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.IntArrayList;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.comparator.RecordFieldComparator;
import com.scudata.dw.MemoryTable;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.thread.MultithreadUtil;
import com.scudata.util.Variant;

/**
 * �������࣬ӵ�����ݽṹ����ԱΪ�ṹ��ͬ�ļ�¼
 * @author WangXiaoJun
 *
 */
public class Table extends Sequence {
	private static final long serialVersionUID = 0x02010004;
	
	protected DataStruct ds; // ���ݽṹ
	protected transient IndexTable indexTable; // �������������������������Ӳ�����find����

	/**
	 * ���л�ʱʹ��
	 */
	public Table() {}

	/**
	 * ����һ��������
	 * @param createArray �Ƿ����IArray
	 */
	protected Table(boolean createArray) {
		super(createArray);
	}

	/**
	 * ����һ�����
	 * @param fields �ֶ�������
	 */
	public Table(String []fields) {
		this(new DataStruct(fields));
	}

	/**
	 * ��ָ�����ݽṹ����һ�����
	 * @param ds ���ݽṹ
	 */
	public Table(DataStruct ds) {
		this.ds = ds;
	}

	/**
	 * ����һ�����
	 * @param fields �ֶ�������
	 * @param initialCapacity ��ʼ����
	 */
	public Table(String []fields, int initialCapacity) {
		this(new DataStruct(fields), initialCapacity);
	}

	/**
	 * ��ָ�����ݽṹ����һ�����
	 * @param ds ���ݽṹ
	 * @param initialCapacity ��ʼ����
	 */
	public Table(DataStruct ds, int initialCapacity) {
		super(initialCapacity);
		this.ds = ds;
	}

	/**
	 * ��ȸ���һ�����
	 * @param src
	 */
	public Table(Table src) {
		super(src.length());
		DataStruct ds = src.dataStruct();
		this.ds = ds;
		int len = src.length();
		for (int i = 1; i<= len; i++) {
			BaseRecord rec = src.getRecord(i);
			newLast(rec.getFieldValues());
		}
	}
	
	/**
	 * �������еĹ�ϣֵ
	 */
	public int hashCode() {
		return mems.hashCode();
	}
	
	/**
	 * ���������Ƿ��м�¼
	 * @return boolean
	 */
	public boolean hasRecord() {
		return true;
	}
	
	/**
	 *  ���ͬ�ṹ�ļ�¼�����β��
	 * @param val ��¼
	 */
	public void add(Object val) {
		if (val instanceof BaseRecord && ((BaseRecord)val).dataStruct() == ds) {
			mems.add(val);
		} else {
			throw new RQException("'add' function is unimplemented in Table!");
		}
	}

	
	/**
	 * ��ָ��λ�ò���һ��ͬ�ṹ��¼
	 * @param pos int    λ�ã���1��ʼ������0��ʾ׷�ӣ�С��0��Ӻ���
	 * @param val Object ��Ҫ��ӵļ�¼
	 */
	public void insert(int pos, Object val) {
		if (val instanceof BaseRecord && ((BaseRecord)val).dataStruct() == ds) {
			super.insert(pos, val);
		} else {
			throw new RQException("'insert' function is unimplemented in Table!");
		}
	}

	/**
	 * �������ָ��λ�õļ�¼
	 * @param pos int λ��
	 * @param obj Object ͬ�ṹ���¼�¼
	 */
	public void set(int pos, Object val) {
		if (val instanceof BaseRecord && ((BaseRecord)val).dataStruct() == ds) {
			super.set(pos, val);
		} else {
			throw new RQException("'set' function is unimplemented in Table!");
		}
	}

	/**
	 * �˷����̳������У����֧��
	 */
	public Object modify(int pos, Object val, String opt) {
		throw new RQException("'modify' function is unimplemented in Table!");
	}

	/**
	 * ����ָ��λ�õļ�¼��Խ���Զ���
	 * @param pos int ��¼����
	 * @return BaseRecord
	 */
	public BaseRecord getRecord(int pos) {
		if (pos < 1) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(pos + mm.getMessage("engine.indexOutofBound"));
		}

		if (pos > length()) {
			insert(pos);
		}
		
		return (BaseRecord)mems.get(pos);
	}

	/**
	 * ����һ���¼�¼׷�ӵ����β���������ظü�¼
	 * @return BaseRecord
	 */
	public BaseRecord newLast() {
		Record r = new Record(ds);
		mems.add(r);
		return r;
	}

	/**
	 * ����һ��ָ����ֵ���¼�¼׷�ӵ����β��
	 * @param initVals Object[] ��ֵ
	 * @return BaseRecord
	 */
	public BaseRecord newLast(Object []initVals) {
		Record r = new Record(ds, initVals);
		mems.add(r);
		return r;
	}

	/**
	 * �����������ݽṹ
	 * @return DataStruct
	 */
	public DataStruct dataStruct() {
		return this.ds;
	}

	/**
	 * �������������ݽṹ��ͬ�Ŀ����
	 * @return Table
	 */
	public Table create() {
		Table table = new Table(ds);
		return table;
	}

	/**
	 * ��������л����ֽ�����
	 * @return �ֽ�����
	 */
	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeRecord(ds);

		IArray mems = getMems();
		int len = mems.size();
		out.writeInt(len);
		for (int i = 1; i <= len; ++i) {
			Record r = (Record)mems.get(i);
			out.writeRecord(r);
		}

		return out.toByteArray();
	}

	/**
	 * ���ֽ�����������
	 * @param buf �ֽ�����
	 */
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		ds = (DataStruct)in.readRecord(new DataStruct());

		int len = in.readInt();
		insert(0, len, null); // ����ռ�¼
		IArray mems = getMems();
		for (int i = 1; i <= len; ++i) {
			Record r = (Record)mems.get(i);
			in.readRecord(r);
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeByte(1); // �汾��
		out.writeObject(ds);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		in.readByte(); // �汾��
		ds = (DataStruct)in.readObject();
	}

	/**
	 * ���ص�ǰ�����Ƿ�������
	 * @return boolean true�������У�false��������
	 */
	public boolean isPmt() {
		return true;
	}

	/**
	 * ���ص�ǰ�����Ƿ��Ǵ�����
	 * @return boolean true���Ǵ����У��ṹ��ͬ��
	 */
	public boolean isPurePmt() {
		return true;
	}
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ���True
	 * @param index ��������1��ʼ����
	 * @return
	 */
	public boolean isTrue(int index) {
		return true;
	}
	
	/**
	 * ѡ��ָ���Ķ��й��������
	 * @param fieldNames ��������
	 */
	public Table fieldsValues(String[] fieldNames) {
		IArray mems = getMems();
		int len = mems.size();
		int newCount = fieldNames.length;
		int []index = new int[newCount];
		String []newNames = new String[newCount];

		for (int i = 0; i < newCount; ++i) {
			int q = ds.getFieldIndex(fieldNames[i]);
			if (q < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(fieldNames[i] + mm.getMessage("ds.fieldNotExist"));
			}

			index[i] = q;
			newNames[i] = ds.getFieldName(q);
		}

		Table table = new Table(newNames, len);
		for (int i = 1; i <= len; ++i) {
			BaseRecord nr = table.newLast();
			Record r = (Record)mems.get(i);
			for (int f = 0; f < newCount; ++f) {
				nr.setNormalFieldValue(f, r.getFieldValue(index[f]));
			}
		}
		
		return table;
	}
	
	/**
	 * �����ֶ�˳�򣬲�����û�б������ֶ�ɾ��
	 * @param fields �½ṹ���ֶ�
	 */
	public void alter(String []fields) {
		DataStruct oldDs = this.ds;
		int newCount = fields.length;
		int []index = new int[newCount];
		for (int i = 0; i < newCount; ++i) {
			index[i] = oldDs.getFieldIndex(fields[i]);
			if (index[i] != -1) {
				// �ֶο�����#i��ʾ
				fields[i] = oldDs.getFieldName(index[i]);
			}
		}
		
		DataStruct newDs = oldDs.create(fields);
		IArray mems = getMems();
		Object []newValues = new Object[newCount];
		
		for (int i = 1, len = mems.size(); i <= len; ++i) {
			Record r = (Record)mems.get(i);
			for (int c = 0; c < newCount; ++c) {
				if (index[c] == -1) {
					newValues[c] = null;
				} else {
					newValues[c] = r.getFieldValue(index[c]);
				}
			}

			r.alter(newDs, newValues);
		}

		this.ds = newDs;
	}
	
	/**
	 * �����������ݽṹ������
	 * @param fields String[] �½ṹ���ֶ�
	 * @param oldFields String[] ���ֶζ�Ӧ��Դ�ֶΣ���ͬ��ʡ��
	 */
	public void alter(String []fields, String []oldFields) {
		if (fields == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("alter" + mm.getMessage("function.paramValNull"));
		}

		DataStruct oldDs = this.ds;
		int newCount = fields.length;
		int []index = new int[newCount];

		// �Ƿ��Դ�ṹ��ͬ
		boolean isSame = newCount == oldDs.getFieldCount();
		if (oldFields == null) {
			for (int i = 0; i < newCount; ++i) {
				index[i] = oldDs.getFieldIndex(fields[i]);
				if (index[i] != i) isSame = false;
			}
		} else {
			for (int i = 0; i < newCount; ++i) {
				if (oldFields[i] == null) {
					index[i] = oldDs.getFieldIndex(fields[i]);
					if (index[i] != i) isSame = false;
				} else {
					index[i] = oldDs.getFieldIndex(oldFields[i]);
					if (index[i] != i || !oldFields[i].equals(fields[i])) {
						isSame = false;
					}
				}
			}
		}

		if (isSame) return; // ��Դ�ṹ��ͬ
		DataStruct newDs = oldDs.create(fields);

		IArray mems = getMems();
		Object []newValues = new Object[newCount];
		for (int i = 1, len = mems.size(); i <= len; ++i) {
			Record r = (Record)mems.get(i);
			for (int c = 0; c < newCount; ++c) {
				if (index[c] == -1) {
					newValues[c] = null;
				} else {
					newValues[c] = r.getFieldValue(index[c]);
				}
			}

			r.alter(newDs, newValues);
		}

		this.ds = newDs;
	}

	/**
	 * �޸����ݽṹ���ֶ���
	 * @param srcFields Դ�ֶ���
	 * @param newFields ���ֶ���
	 */
	public void rename(String []srcFields, String []newFields) {
		ds.rename(srcFields, newFields);
	}

	/**
	 * ���ص�ǰ�����ָ�������Ƿ����
	 * @param ����
	 * @return boolean true������ָ���������ǵ�ǰ���
	 */
	public boolean isEquals(Sequence table) {
		return table == this;
	}
	
	/**
	 * ȡ���зǿ�Ԫ�ظ���
	 * @return int
	 */
	public int count() {
		return getMems().size();
	}
	
	/**
	 * �������еķ��ظ�Ԫ������������null
	 * @param opt o����������
	 * @return
	 */
	public int icount(String opt) {
		return getMems().size();
	}
	
	/**
	 * ����ȥ���ظ���Ԫ�غ������
	 * @param opt String o��ֻ�����ڵĶԱȣ�u�������������h������������@o����
	 * @return Sequence
	 */
	public Sequence id(String opt) {
		return this;
	}
	
	/**
	 * �˷����̳������У����֧�ֱȴ�С
	 */
	public int cmp(Sequence table) {
		return table == this ? 0 : -1;
	}

	/**
	 * ���֧�ֱȴ�С
	 */
	public int compareTo(Sequence table) {
		return table == this ? 0 : -1;
	}

	/**
	 * ��ָ��λ�ò���һ���ռ�¼
	 * @param pos int λ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 */
	public BaseRecord insert(int pos) {
		if (pos == 0) { // ׷��
			return newLast();
		}
		
		IArray mems = getMems();
		int oldCount = mems.size();
		if (pos < 0) {
			pos += oldCount + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - oldCount - 1 + mm.getMessage("engine.indexOutofBound"));
			}
			
			Record r = new Record(ds);
			mems.insert(pos, r);
			return r;
		} else if (pos > oldCount) { // Խ���Զ���
			int count = pos - oldCount;
			Record []rs = new Record[count];
			for (int i = 0; i < count; ++i) {
				rs[i] = new Record(ds);
			}
			
			mems.addAll(rs);
			return rs[count - 1];
		} else {
			Record r = new Record(ds);
			mems.insert(pos, r);
			return r;
		}
	}

	/**
	 * ��ָ��λ�ò���һ����¼
	 * @param pos λ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param values ��¼�ֶ�ֵ��ɵ�����
	 * @return ����ļ�¼
	 */
	public BaseRecord insert(int pos, Object []values) {
		if (pos == 0) { // ׷��
			return newLast(values);
		} else if (pos < 0) {
			pos += mems.size() + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - mems.size() - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}
		
		Record r = new Record(ds, values);
		mems.insert(pos, r);
		return r;
	}

	/**
	 * ��ָ��λ�ò�������ռ�¼
	 * @param pos int λ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param count int ����
	 * @param opt String n�������²���ļ�¼���ɵ�����
	 * @return Sequence
	 */
	public Sequence insert(int pos, int count, String opt) {
		IArray mems = getMems();
		int oldCount = mems.size();
		if (pos < 0) {
			pos += oldCount + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - oldCount - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}

		boolean returnNew = opt != null && opt.indexOf('n') != -1;
		Sequence result;
		if (returnNew) {
			result = new Sequence(count);
		} else {
			result = this;
		}

		if (count < 1) return result;

		int last = oldCount + 1;
		if (pos == 0) { // ׷��
			pos = last;
		} else if (pos > last) { // Խ��
			count += (pos - last);
			pos = last;
		} // �����׷��

		// �����¼�¼
		DataStruct ds = this.ds;
		Record []rs = new Record[count];
		for (int i = 0; i < count; ++i) {
			rs[i] = new Record(ds);
		}

		if (pos <= oldCount) {
			mems.insertAll(pos, rs);
		} else {
			mems.addAll(rs);
		}
		
		if (returnNew) {
			result.addAll(rs);
		}
		
		return result;
	}

	/**
	 * ��ָ��λ�ò���һ����¼
	 * @param pos int λ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param values Object[] �ֶζ�Ӧ��ֵ
	 * @param fields String[] �ֶ���, ʡ�������θ�ֵ
	 */
	public void insert(int pos, Object []values, String []fields) {
		if (values == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("insert" + mm.getMessage("function.invalidParam"));
		}

		if (pos == 0) {
			newLast();
			modify(length(), values, fields);
		} else if (pos < 0) {
			pos += mems.size() + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - mems.size() - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}
		
		insert(pos);
		modify(pos, values, fields);
	}

	/**
	 * ������ʽ����ָ��λ�ò���һ����¼
	 * @param pos int λ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param exps Expression[] ֵ���ʽ�������ô�Table
	 * @param fields String[]�ֶ���, ʡ�������θ�ֵ
	 * @param ctx Context
	 * @return BaseRecord
	 */
	public BaseRecord insert(int pos, Expression[] exps, String[] fields, Context ctx) {
		if (exps == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("insert" + mm.getMessage("function.invalidParam"));
		}
		
		if (pos == 0) {
			newLast();
			return modify(length(), exps, fields, ctx);
		} else if (pos < 0) {
			pos += mems.size() + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - mems.size() - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}

		insert(pos);
		return modify(pos, exps, fields, ctx);
	}
	
	/**
	 * ����������򣬰�����ֵ���²����ļ�¼���뵽�ʵ���λ�ã�����Ѵ����򲻲���
	 * @param exps �ֶ�ֵ���ʽ����
	 * @param fields �ֶ�������
	 * @param ctx ����������
	 * @return �²���ļ�¼
	 */
	public BaseRecord sortedInsert(Expression[] exps, String[] fields, Context ctx) {
		Record r = new Record(ds);
		ComputeStack stack = ctx.getComputeStack();
		stack.push(r);

		try {
			// ���ɼ�¼��������ֶο�������ǰ��ղ������ֶ�
			int count = exps.length;
			if (fields == null) {
				for (int i = 0; i < count; ++i) {
					if (exps[i] != null) r.set(i, exps[i].calculate(ctx));
				}
			} else {
				if (fields.length != count) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("insert" + mm.getMessage("function.invalidParam"));
				}

				int prevIndex = -1;
				for (int i = 0; i < count; ++i) {
					if (fields[i] == null) {
						prevIndex++;
					} else {
						prevIndex = r.getFieldIndex(fields[i]);
						if (prevIndex < 0) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(fields[i] + mm.getMessage("ds.fieldNotExist"));
						}
					}
					
					r.set(prevIndex, exps[i].calculate(ctx));
				}
			}
		} finally {
			stack.pop();
		}
		
		// �����������Ҽ�¼λ��
		int index = pfindByKey(r.getPKValue(), true);
		if (index < 0) {
			mems.insert(-index, r);
			return r;
		} else {
			return null;
		}
	}

	// ������룬����Ѵ����򲻲���
	public Sequence sortedInsert(Sequence src, Expression[] exps, String[] fields, String opt, Context ctx) {
		int count = exps.length;
		int fcount = ds.getFieldCount();
		if (count == 0 || count > fcount) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("insert" + mm.getMessage("function.invalidParam"));
		}

		int mlen = src.length();
		boolean returnNew = opt!= null && opt.indexOf('n') != -1;
		Sequence result;
		if (returnNew) {
			result = new Sequence(mlen);
		} else {
			result = this;
		}
		
		if (mlen == 0) return result;

		int []index = new int[count];
		if (fields != null) {
			if (fields.length != count) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("insert" + "function.paramCountNotMatch"));
			}

			for (int i = 0; i < count; ++i) {
				if (fields[i] == null) {
					if (i == 0) {
						index[i] = 0;
					} else {
						index[i] = index[i - 1] + 1;
						if (index[i] == fcount) { // Խ��
							MessageManager mm = EngineMessage.get();
							throw new RQException("insert" + mm.getMessage("function.invalidParam"));
						}
					}
				} else {
					index[i] = ds.getFieldIndex(fields[i]);
					if (index[i] < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(fields[i] + mm.getMessage("ds.fieldNotExist"));
					}
				}
			}
		} else {
			for (int i = 0; i < count; ++i) {
				index[i] = i;
			}
		}

		DataStruct ds = this.ds;
		Record r = new Record(ds);
		ComputeStack stack = ctx.getComputeStack();
		Current srcCurrent = new Current(src);
		stack.push(r);
		stack.push(srcCurrent);

		try {
			for (int i = 1; i <= mlen; ++i) {
				srcCurrent.setCurrent(i);
				for (int c = 0; c < count; ++c) {
					if (exps[c] != null) {
						r.setNormalFieldValue(index[c], exps[c].calculate(ctx));
					}
				}
				
				int p = pfindByKey(r.getPKValue(), true);
				if (p < 0) {
					Record tmp = new Record(ds);
					tmp.set(r);
					mems.insert(-p, tmp);
					if (returnNew) result.add(tmp);
				}
			}
		} finally {
			stack.pop();
			stack.pop();
		}
		
		return result;
	}
	
	public Sequence sortedInsert(Sequence src, String opt) {
		boolean isName = false, returnNew = false;
		if (opt != null) {
			if (opt.indexOf('f') != -1) isName = true;
			if (opt.indexOf('n') != -1) returnNew = true;
		}

		if (src == null || src.length() == 0) {
			if (returnNew) {
				return new Sequence(0);
			} else {
				return this;
			}
		}
		
		IArray srcMems = src.getMems();
		int count = srcMems.size();

		if (!src.isPmt()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPmt"));
		}

		DataStruct ds = this.ds;
		Sequence result;
		if (returnNew) {
			result = new Sequence(count);
		} else {
			result = this;
		}

		if (isName) { // ֻ������ͬ���ֵ��ֶ�
			Record prev = null;
			int sameCount = 0;
			int []srcIndex = null;
			int []index = null;

			for (int i = 1; i <= count; ++i) {
				Record sr = (Record)srcMems.get(i);
				if (sr == null) continue;

				Record r = new Record(ds);
				if (prev != null && prev.isSameDataStruct(sr)) {
					for (int c = 0; c < sameCount; ++c) {
						r.setNormalFieldValue(index[c], sr.getFieldValue(srcIndex[c]));
					}
				} else {
					String []srcNames = sr.dataStruct().getFieldNames();
					int colCount = srcNames.length;

					prev = sr;
					sameCount = 0;
					srcIndex = new int[colCount];
					index = new int[colCount];

					for (int c = 0; c < colCount; ++c) {
						int tmp = ds.getFieldIndex(srcNames[c]);
						if (tmp >= 0) {
							r.setNormalFieldValue(tmp, sr.getFieldValue(c));

							srcIndex[sameCount] = c;
							index[sameCount] = tmp;
							sameCount++;
						}
					}
				}
				
				int p = pfindByKey(r.getPKValue(), true);
				if (p < 0) {
					mems.insert(-p, r);
					if (returnNew) result.add(r);
				}
			}
		} else {
			for (int i = 1; i <= count; ++i) {
				BaseRecord sr = (BaseRecord)srcMems.get(i);
				Record r = new Record(ds);
				r.paste(sr, false);
				
				int p = pfindByKey(r.getPKValue(), true);
				if (p < 0) {
					mems.insert(-p, r);
					if (returnNew) result.add(r);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * ��ָ��λ�ò��������¼
	 * @param pos int λ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param src Sequence ������ʽ����Ե�Դ����
	 * @param exps Expression[] ������ʽ
	 * @param optExps Expression[] �Ż����ʽ
	 * @param fields String[] �ֶ���, ʡ�������θ�ֵ
	 * @param ctx Context
	 * @param opt String n�������²���ļ�¼���ɵ�����
	 * @return Sequence
	 */
	public Sequence insert(int pos, Sequence src, Expression[] exps,
					   Expression[] optExps, String[] fields, String opt, Context ctx) {
		if (src == null || exps == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("insert" + mm.getMessage("function.invalidParam"));
		}
		
		IArray mems = getMems();
		if (pos == 0) {
			pos = mems.size() + 1;
		} else if (pos < 0) {
			pos += mems.size() + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - mems.size() - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}

		int count = exps.length;
		if (optExps == null) optExps = new Expression[count];

		int fcount = ds.getFieldCount();
		if (count == 0 || count > fcount) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("insert" + mm.getMessage("function.invalidParam"));
		}

		int mlen = src.length();
		boolean returnNew = opt!= null && opt.indexOf('n') != -1;
		Sequence result;
		if (returnNew) {
			result = new Sequence(mlen);
		} else {
			result = this;
		}
		
		if (mlen == 0) return result;

		int []index = new int[count];
		if (fields != null) {
			if (fields.length != count) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("insert" + "function.paramCountNotMatch"));
			}

			for (int i = 0; i < count; ++i) {
				if (fields[i] == null) {
					if (i == 0) {
						index[i] = 0;
					} else {
						index[i] = index[i - 1] + 1;
						if (index[i] == fcount) { // Խ��
							MessageManager mm = EngineMessage.get();
							throw new RQException("insert" + mm.getMessage("function.invalidParam"));
						}
					}
				} else {
					index[i] = ds.getFieldIndex(fields[i]);
					if (index[i] < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(fields[i] + mm.getMessage("ds.fieldNotExist"));
					}
				}
			}
		} else {
			for (int i = 0; i < count; ++i) {
				index[i] = i;
			}
		}

		insert(pos, mlen, null);

		Object []values = new Object[count];
		Object []lastOptVals = new Object[count];
		for (int i = 0; i < count; ++i) {
			lastOptVals[i] = new Object();
		}

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(this);
		stack.push(current);
		Current srcCurrent = new Current(src);
		stack.push(srcCurrent);

		try {
			for (int i = 1; i <= mlen; ++i, ++pos) {
				BaseRecord r = (BaseRecord)mems.get(pos);
				if (returnNew) result.add(r);
				
				current.setCurrent(pos);
				srcCurrent.setCurrent(i);
				for (int c = 0; c < count; ++c) {
					if (optExps[c] == null) {
						if (exps[c] != null) {
							r.setNormalFieldValue(index[c], exps[c].calculate(ctx));
						}
					} else {
						Object optVal = optExps[c].calculate(ctx);
						if (!Variant.isEquals(optVal, lastOptVals[c])) {
							lastOptVals[c] = optVal;
							if (exps[c] != null) {
								values[c] = exps[c].calculate(ctx);
							}
						}

						r.setNormalFieldValue(index[c], values[c]);
					}
				}
			}
		} finally {
			stack.pop();
			stack.pop();
		}
		
		return result;
	}

	/**
	 * �����table�ļ�¼��ӵ������ָ��λ�ã������table������ֶ�������ͬ
	 * @param pos int λ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param table Table
	 */
	public void insert(int pos, Table table) {
		if (table == null || table.length() == 0) return;
		
		IArray mems = getMems();
		int oldCount = mems.size();
		if (pos == 0) {
			pos = oldCount + 1; // 0��ʾ׷��
		} else if (pos < 0) {
			pos += oldCount + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - oldCount - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}

		if (table.ds.getFieldCount() != ds.getFieldCount()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.dsNotMatch"));
		}

		// ���ļ�¼�������������
		IArray addMems = table.getMems();
		int addCount = addMems.size();
		DataStruct ds = this.ds;
		for (int i = 1; i <= addCount; ++i) {
			Record r = (Record)addMems.get(i);
			r.setDataStruct(ds);
		}

		if (pos > oldCount) { // ׷��
			insert(oldCount + 1, pos - oldCount - 1, null); // Խ���Զ���
		}

		mems.insertAll(pos, addMems);
		addMems.clear();
	}
	
	/**
	 * �ϲ��������е����ݣ�������м����򷵻�ԭ���з��򷵻�������
	 * @param seq
	 * @return Sequence
	 */
	public Sequence append(Sequence seq) {
		DataStruct ds = this.ds;
		DataStruct ds2 = seq.dataStruct();
		if (ds2 == ds) {
			getMems().addAll(seq.getMems());
			return this;
		} else if (ds2 != null && ds2.isCompatible(ds2)) {
			IArray mems = getMems();
			IArray addMems = seq.getMems();
			for (int i = 1, addCount = addMems.size(); i <= addCount; ++i) {
				Record r = (Record)addMems.get(i);
				r.setDataStruct(ds);
			}

			mems.addAll(addMems);
			return this;
		} else {
			Sequence result = new Sequence(length() + seq.length());
			result.addAll(this);
			result.addAll(seq);
			return result;
		}
	}

	/**
	 * �����table�ļ�¼��ӵ�������У������table������ֶ�������ͬ
	 * @param table Table
	 * @param opt String p������������ȥ��Դ����������ظ��ļ�¼
	 */
	public void append(Table table, String opt) {
		if (table == null) return;
		if (table.ds.getFieldCount() != ds.getFieldCount()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.dsNotMatch"));
		}

		IArray addMems = table.getMems();
		IArray mems = getMems();
		int oldCount = mems.size();

		// ȥ��Դ���������ֵ��table�д��ڵļ�¼
		if (opt != null && opt.indexOf('p') != -1 && getPrimary() != null) {
			IntArrayList posArray = new IntArrayList();

			for (int i = 1; i <= oldCount; ++i) {
				int pos = table.pfindByKey(((BaseRecord)mems.get(i)).getPKValue(), false);
				if (pos > 0) {
					posArray.addInt(i);
				}
			}

			int delCount = posArray.size();
			if (delCount > 0) {
				int[] index = posArray.toIntArray();
				mems.remove(index);
				oldCount = mems.size();
			}
		}

		// ���ļ�¼�������������
		DataStruct ds = this.ds;
		for (int i = 1, addCount = addMems.size(); i <= addCount; ++i) {
			Record r = (Record)addMems.get(i);
			r.setDataStruct(ds);
		}

		mems.addAll(addMems);
		addMems.clear();
	}

	/**
	 * �����tables�ļ�¼��ӵ�������У������tables������ֶ�������ͬ
	 * @param tables Table[]
	 * @param opt String p������������ȥ��Դ����������ظ��ļ�¼
	 */
	public void append(Table []tables, String opt) {
		if (tables == null || tables.length == 0) return;
		if (opt != null && opt.indexOf('p') != -1 && getPrimary() != null) {
			for (int i = 0, len = tables.length; i < len; ++i) {
				append(tables[i], opt);
			}
			return;
		}

		int fcount = ds.getFieldCount();
		int total = 0;

		for (int i = 0, len = tables.length; i < len; ++i) {
			Table table = tables[i];
			if (table != null) {
				if (table.ds.getFieldCount() != fcount) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("engine.dsNotMatch"));
				}

				total += table.length();
			}
		}

		IArray mems = getMems();
		mems.ensureCapacity(mems.size() + total);
		DataStruct ds = this.ds;

		for (int i = 0, len = tables.length; i < len; ++i) {
			Table table = tables[i];
			if (table != null) {
				IArray addMems = table.getMems();

				// ���ļ�¼�������������
				for (int m = 1, addCount = addMems.size(); m <= addCount; ++m) {
					Record r = (Record)addMems.get(m);
					r.setDataStruct(ds);
				}

				mems.addAll(addMems);
				addMems.clear();
			}
		}
	}

	/**
	 * ��ָ�������¼�������
	 * @param from int ��ʼλ�ã�����
	 * @param to int ����λ�ã�����
	 * @return Sequence
	 */
	public Sequence split(int from, int to) {
		if (from < 1 || to < from || to > length()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(from + ":" + to + mm.getMessage("engine.indexOutofBound"));
		}

		Table table = new Table(ds, to - from + 1);
		IArray resultMems = table.getMems();
		IArray mems = getMems();

		for (int i = from; i <= to; ++i) {
			resultMems.push(mems.get(i));
		}

		mems.removeRange(from, to);
		return table;
	}

	/**
	 * �޸�ĳһ��¼��Խ������Ӽ�¼
	 * @param pos int ��¼��ʼλ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param values Object[] �ֶζ�Ӧ��ֵ
	 * @param fields String[] �ֶ���, ʡ�������θ�ֵ
	 */
	public void modify(int pos, Object []values, String []fields) {
		if (values == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("modify" + mm.getMessage("function.invalidParam"));
		}
		
		if (pos == 0) {
			pos = length() + 1;
		} else if (pos < 0) {
			pos += length() + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - length() - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}

		BaseRecord r = getRecord(pos);
		if (fields == null) {
			r.setStart(0, values);
		} else {
			int count = values.length;
			if (fields.length != count) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("modify" + mm.getMessage("function.invalidParam"));
			}

			int prevIndex = -1;
			for (int i = 0; i < count; ++i) {
				if (fields[i] == null) {
					prevIndex++;
				} else {
					prevIndex = r.getFieldIndex(fields[i]);
					if (prevIndex < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(fields[i] + mm.getMessage("ds.fieldNotExist"));
					}
				}
				r.set(prevIndex, values[i]);
			}
		}
	}

	/**
	 * ������ʽ���޸�ĳһ��¼��Խ������Ӽ�¼
	 * @param pos int ��¼��ʼλ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param exps Expression[] ֵ���ʽ�������ô�Table
	 * @param fields String[]�ֶ���, ʡ�������θ�ֵ
	 * @param ctx Context
	 * @return ���ر��޸ĵļ�¼
	 */
	public BaseRecord modify(int pos, Expression[] exps, String[] fields, Context ctx) {
		if (exps == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("modify" + mm.getMessage("function.invalidParam"));
		}
		
		if (pos == 0) {
			pos = length() + 1;
		} else if (pos < 0) {
			pos += length() + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - length() - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}

		BaseRecord r = getRecord(pos);
		int count = exps.length;

		// ��tableѹջ��������ʽ���õ�ǰ��¼���ֶ�
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(this);
		stack.push(current);

		try {
			current.setCurrent(pos);
			if (fields == null) {
				for (int i = 0; i < count; ++i) {
					if (exps[i] != null) r.set(i, exps[i].calculate(ctx));
				}
			} else {
				if (fields.length != count) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("modify" + mm.getMessage("function.invalidParam"));
				}

				int prevIndex = -1;
				for (int i = 0; i < count; ++i) {
					if (fields[i] == null) {
						prevIndex++;
					} else {
						prevIndex = r.getFieldIndex(fields[i]);
						if (prevIndex < 0) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(fields[i] + mm.getMessage("ds.fieldNotExist"));
						}
					}
					r.set(prevIndex, exps[i].calculate(ctx));
				}
			}
		} finally {
			stack.pop();
		}
		
		return r;
	}
	
	/**
	 * �޸Ķ�����¼��Խ������Ӽ�¼
	 * @param pos int ��¼��ʼλ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param src Sequence ������ʽ����Ե�Դ����
	 * @param exps Expression[] ������ʽ
	 * @param optExps Expression[] �Ż����ʽ
	 * @param fields String[] �ֶ���, ʡ�������θ�ֵ
	 * @param ctx Context
	 * @param n�������²���ļ�¼���ɵ�����
	 * @return
	 */
	public Sequence modify(int pos, Sequence src, Expression[] exps,
					   Expression[] optExps, String[] fields, String opt, Context ctx) {
		if (src == null || exps == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("modify" + mm.getMessage("function.invalidParam"));
		}
		
		if (pos == 0) {
			pos = length() + 1;
		} else if (pos < 0) {
			pos += length() + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - length() - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}

		int count = exps.length;
		if (optExps == null) optExps = new Expression[count];

		int fcount = ds.getFieldCount();
		if (count == 0 || count > fcount) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("modify" + mm.getMessage("function.invalidParam"));
		}

		int mlen = src.length();
		boolean returnNew = opt!= null && opt.indexOf('n') != -1;
		Sequence result;
		if (returnNew) {
			result = new Sequence(mlen);
		} else {
			result = this;
		}

		if (mlen == 0) {
			return result;
		}

		int []index = new int[count];
		if (fields != null) {
			if (fields.length != count) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("modify" + "function.paramCountNotMatch"));
			}

			for (int i = 0; i < count; ++i) {
				if (fields[i] == null) {
					if (i == 0) {
						index[i] = 0;
					} else {
						index[i] = index[i - 1] + 1;
						if (index[i] == fcount) { // Խ��
							MessageManager mm = EngineMessage.get();
							throw new RQException("insert" + mm.getMessage("function.invalidParam"));
						}
					}
				} else {
					index[i] = ds.getFieldIndex(fields[i]);
					if (index[i] < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(fields[i] + mm.getMessage("ds.fieldNotExist"));
					}
				}
			}
		} else {
			for (int i = 0; i < count; ++i) {
				index[i] = i;
			}
		}

		int last = pos + mlen - 1;
		getRecord(last); // ���Խ���Զ���

		Object []values = new Object[count];
		Object []lastOptVals = new Object[count];
		for (int i = 0; i < count; ++i) {
			lastOptVals[i] = new Object();
		}

		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(this);
		stack.push(current);
		Current srcCurrent = new Current(src);
		stack.push(srcCurrent);

		try {
			IArray mems = getMems();
			for (int i = 1; i <= mlen; ++i, ++pos) {
				BaseRecord r = (BaseRecord)mems.get(pos);
				if (returnNew) result.add(r);

				current.setCurrent(pos);
				srcCurrent.setCurrent(i);
				for (int c = 0; c < count; ++c) {
					if (optExps[c] == null) {
						if (exps[c] != null) {
							r.setNormalFieldValue(index[c], exps[c].calculate(ctx));
						} else {
							r.setNormalFieldValue(index[c], null);
						}
					} else {
						Object optVal = optExps[c].calculate(ctx);
						if (!Variant.isEquals(optVal, lastOptVals[c])) {
							lastOptVals[c] = optVal;
							if (exps[c] != null) {
								values[c] = exps[c].calculate(ctx);
							}
						}

						r.setNormalFieldValue(index[c], values[c]);
					}
				}
			}
		} finally {
			stack.pop();
			stack.pop();
		}
		
		return result;
	}

	public void run(Expression[] assignExps, Expression[] exps, String opt, Context ctx) {
		if (opt != null) {
			if (opt.indexOf('m') != -1) {
				MultithreadUtil.run(this, assignExps, exps, ctx);
			} else if (opt.indexOf('z') != -1) {
				int colCount = exps.length;
				int []fields = new int[colCount];

				for (int i = 0; i < colCount; ++i) {
					if (assignExps[i] != null) {
						fields[i] = assignExps[i].getFieldIndex(ds);
						if (fields[i] != -1) {
							continue;
						}
					}
					
					super.run(assignExps, exps, ctx);
					return;
				}
				
				ComputeStack stack = ctx.getComputeStack();
				Current current = new Current(this);
				stack.push(current);
				IArray mems = getMems();

				try {
					for (int i = length(); i > 0; --i) {
						current.setCurrent(i);
						BaseRecord r = (BaseRecord)mems.get(i);
						for (int c = 0; c < colCount; ++c) {
							r.setNormalFieldValue(fields[c], exps[c].calculate(ctx));
						}
					}
				} finally {
					stack.pop();
				}
			} else {
				run(assignExps, exps, ctx);
			}
		} else {
			run(assignExps, exps, ctx);
		}
	}

	/**
	 * ѭ������Ԫ�أ�������ʽ�����и�ֵ
	 * @param assignExps Expression[] ��ֵ���ʽ
	 * @param exps Expression[] ֵ���ʽ
	 * @param ctx Context
	 */
	public void run(Expression[] assignExps, Expression[] exps, Context ctx) {
		int colCount = exps.length;
		int []fields = new int[colCount];

		for (int i = 0; i < colCount; ++i) {
			if (assignExps[i] != null) {
				fields[i] = assignExps[i].getFieldIndex(ds);
				if (fields[i] != -1) {
					continue;
				}
			}
			
			super.run(assignExps, exps, ctx);
			return;
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Current current = new Current(this);
		stack.push(current);
		IArray mems = getMems();

		try {
			for (int i = 1, len = length(); i <= len; ++i) {
				current.setCurrent(i);
				BaseRecord r = (BaseRecord)mems.get(i);
				for (int c = 0; c < colCount; ++c) {
					r.setNormalFieldValue(fields[c], exps[c].calculate(ctx));
				}
			}
		} finally {
			stack.pop();
		}
	}
	
	/**
	 * �����е�Ԫ����Ϊ�ֶ�ֵ���ɼ�¼���뵽�����
	 * @param pos int λ�ã�0��ʾ׷��
	 * @param src Sequence ֵ����
	 * @param opt String i�������¼�¼��n�������²���ļ�¼���ɵ�����
	 */
	public Sequence record(int pos, Sequence src, String opt) {
		if (pos < 0 || src == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("record" + mm.getMessage("function.invalidParam"));
		}

		int fieldCount = ds.getFieldCount();
		IArray srcMems = src.getMems();
		int srcSize = srcMems.size();
		int recordCount = srcSize / fieldCount;
		int mod = srcSize % fieldCount;
		if (mod != 0) recordCount++;
		
		boolean isInsert = false, returnNew = false;
		if (opt != null) {
			if (opt.indexOf('i') != -1) isInsert = true;
			if (opt.indexOf('n') != -1) returnNew = true;
		}
		
		Sequence result;
		if (returnNew) {
			result = new Sequence(recordCount);
		} else {
			result = this;
		}
		
		if (recordCount == 0) return result;

		IArray mems = getMems();
		if (pos == 0) pos = mems.size() + 1;

		if (isInsert) {
			// ��pos������recordCount���¼�¼
			insert(pos, recordCount, null);
		} else {
			getRecord(pos + recordCount - 1); // ���Խ���Զ���
		}
		
		if (mod == 0) {
			int seq = 1;
			int last = pos + recordCount;
			for (int i = pos; i < last; ++i) {
				BaseRecord r = (BaseRecord)mems.get(i);
				if (returnNew) result.add(r);
				
				for (int f = 0; f < fieldCount; ++f) {
					r.setNormalFieldValue(f, srcMems.get(seq++));
				}
			}
		} else {
			int seq = 1;
			int last = pos + recordCount - 1;
			for (int i = pos; i < last; ++i) {
				BaseRecord r = (BaseRecord)mems.get(i);
				if (returnNew) result.add(r);

				for (int f = 0; f < fieldCount; ++f) {
					r.setNormalFieldValue(f, srcMems.get(seq++));
				}
			}
			
			BaseRecord r = (BaseRecord)mems.get(last);
			if (returnNew) result.add(r);
			
			for (int f = 0; seq <= srcSize; ++f) {
				r.setNormalFieldValue(f, srcMems.get(seq++));
			}
		}
		
		return result;
	}

	public void paste(Sequence []vals, String []fields, int pos, String opt) {
		if (pos < 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(pos + mm.getMessage("engine.indexOutofBound"));
		}

		IArray mems = getMems();
		int maxLen = 0;
		
		for (Sequence seq : vals) {
			if (seq != null && seq.length() > maxLen) {
				maxLen = seq.length();
			}
		}
		
		boolean isInsert = opt != null && opt.indexOf('i') != -1;
		if (pos == 0) {
			pos = mems.size() + 1;
			isInsert = true;
		}
		
		if (isInsert) {
			insert(pos, maxLen, null);
		} else if (pos > mems.size()) {
			return;
		}
		
		int len = mems.size() - pos + 1;		
		int prevField = -1;
		for (int f = 0, fcount = vals.length; f < fcount; ++f) {
			if (vals[f] == null) {
				continue;
			}
			
			IArray valMems = vals[f].getMems();
			int curLen = valMems.size();
			if (curLen > len) curLen = len;
			
			if (fields == null || fields[f] == null) {
				prevField++;
				if (prevField >= ds.getFieldCount()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(f + mm.getMessage("ds.fieldNotExist"));
				}
			} else {
				prevField = ds.getFieldIndex(fields[f]);
				if (prevField  < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(fields[f] + mm.getMessage("ds.fieldNotExist"));
				}
			}
			
			for (int i = 1, j = pos; i <= curLen; ++i, ++j) {
				BaseRecord r = (BaseRecord)mems.get(j);
				r.setNormalFieldValue(prevField, valMems.get(i));
			}
		}
	}

	protected int pos(Object obj) {
		IArray mems = getMems();
		for (int i = 1, size = mems.size(); i <= size; ++i) {
			if (mems.get(i) == obj) {
				return i;
			}
		}

		return 0;
	}

	/**
	 * ɾ��������¼
	 * @param sequence Sequence λ�����л��¼����
	 * @param opt String n ���ر�ɾ��Ԫ�ع��ɵ�����
	 */
	public Sequence delete(Sequence sequence, String opt) {
		if (sequence == null || sequence.length() == 0) {
			if (opt == null || opt.indexOf('n') == -1) {
				return this;
			} else {
				return new Sequence(0);
			}
		}

		int srcCount = length();
		int[] index = sequence.toIndexArray(srcCount);
		int delCount = 0;

		if (index == null) {
			IArray delMems = sequence.getMems();
			int count = delMems.size();
			index = new int[count];

			// ����Ҫɾ���ļ�¼������е�λ��
			for (int i = 1; i <= count; ++i) {
				Object obj = delMems.get(i);
				if (obj instanceof BaseRecord) {
					int seq = pos(obj);
					if (seq > 0) {
						index[delCount] = seq;
						delCount++;
					}
				}
			}

			if (delCount == 0) {
				if (opt == null || opt.indexOf('n') == -1) {
					return this;
				} else {
					return new Sequence(0);
				}
			}

			if (delCount < count) {
				int []tmp = new int[delCount];
				System.arraycopy(index, 0, tmp, 0, delCount);
				index = tmp;
			}
	
			// ��������������
			Arrays.sort(index);
		} else {
			delCount = index.length;
		}

		IArray mems = getMems();
		if (opt == null || opt.indexOf('n') == -1) {
			mems.remove(index);
			rebuildIndexTable();
			return this;
		} else {
			Sequence result = new Sequence(delCount);
			for (int i = 0; i < delCount; ++i) {
				result.add(mems.get(index[i]));
			}
			
			mems.remove(index);
			rebuildIndexTable();
			return result;
		}
	}
	
	/**
	 * �����src���ֶ�ֵ����������¼����Ӧ�ֶ�
	 * @param pos int ��¼��ʼλ�ã���1��ʼ������0��ʾ׷�ӣ�Խ���Զ�����С��0��Ӻ���
	 * @param src Sequence Դ����
	 * @param isInsert true:�����¼�¼
	 * @param opt f�����ֶ�����Ƚ��и��ƣ�n�������޸ĵļ�¼��ɵ�����
	 */
	public Sequence modify(int pos, Sequence src, boolean isInsert, String opt) {
		IArray mems = getMems();
		if (pos == 0) {
			pos = mems.size() + 1;
		} else if (pos < 0 ) {
			pos += mems.size() + 1;
			if (pos < 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(pos - mems.size() - 1 + mm.getMessage("engine.indexOutofBound"));
			}
		}

		boolean isName = false, returnNew = false;
		if (opt != null) {
			if (opt.indexOf('f') != -1) isName = true;
			if (opt.indexOf('n') != -1) returnNew = true;
		}

		if (src == null || src.length() == 0) {
			if (returnNew) {
				return new Sequence(0);
			} else {
				return this;
			}
		}
		
		IArray srcMems = src.getMems();
		int count = srcMems.size();

		if (!src.isPmt()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPmt"));
		}

		if (isInsert) {
			insert(pos, count, null);
		} else {
			getRecord(pos + count - 1); // ���Խ���Զ���
		}

		Sequence result;
		if (returnNew) {
			result = new Sequence(count);
			for (int i = pos, end = pos + count; i < end; ++i) {
				result.add(mems.get(i));
			}
		} else {
			result = this;
		}

		if (isName) { // ֻ������ͬ���ֵ��ֶ�
			DataStruct ds = this.ds;
			BaseRecord prev = null;
			int sameCount = 0;
			int []srcIndex = null;
			int []index = null;

			for (int i = 1; i <= count; ++i, ++pos) {
				BaseRecord sr = (BaseRecord)srcMems.get(i);
				if (sr == null) continue;

				BaseRecord r = (BaseRecord) mems.get(pos);
				if (prev != null && prev.isSameDataStruct(sr)) {
					for (int c = 0; c < sameCount; ++c) {
						r.setNormalFieldValue(index[c], sr.getFieldValue(srcIndex[c]));
					}
				} else {
					String []srcNames = sr.dataStruct().getFieldNames();
					int colCount = srcNames.length;

					prev = sr;
					sameCount = 0;
					srcIndex = new int[colCount];
					index = new int[colCount];

					for (int c = 0; c < colCount; ++c) {
						int tmp = ds.getFieldIndex(srcNames[c]);
						if (tmp >= 0) {
							r.setNormalFieldValue(tmp, sr.getFieldValue(c));

							srcIndex[sameCount] = c;
							index[sameCount] = tmp;
							sameCount++;
						}
					}
				}
			}
		} else {
			for (int i = 1; i <= count; ++i, ++pos) {
				BaseRecord sr = (BaseRecord)srcMems.get(i);
				BaseRecord r = (BaseRecord) mems.get(pos);
				r.paste(sr, false);
			}
		}
		
		return result;
	}

	/**
	 * ������������
	 * @param fields String[]
	 * @param opt String b��pfind/find/get/put�����Զ���@b
	 */
	public void setPrimary(String []fields) {
		ds.setPrimary(fields);
		indexTable = null;
	}
	
	/**
	 * ������������
	 * @param fields String[]
	 * @param timeKey String t�����һ��Ϊʱ���
	 * @param opt String b��pfind/find/get/put�����Զ���@b
	 */
	public void setPrimary(String []fields, String opt) {
		ds.setPrimary(fields, opt);
		indexTable = null;
	}

	/**
	 * ����������������
	 * @param opt m�����н�����s�������ź�ʱ�����ɶ����״������
	 * b��������������ö��ַ��ң���ѡ���ʺ�����Ϊ�ַ�����ά���¼�ٵ�������ַ������ϣ�Ƚ�����
	 */
	public void createIndexTable(String opt) {
		createIndexTable(length(), opt);
	}

	/**
	 * ����������������
	 * @param capacity ������ϣ������
	 * @param opt m�����н�����s�������ź�ʱ�����ɶ����״������
	 * b��������������ö��ַ��ң���ѡ���ʺ�����Ϊ�ַ�����ά���¼�ٵ�������ַ������ϣ�Ƚ�����
	 */
	public void createIndexTable(int capacity, String opt) {
		int []fields = ds.getPKIndex();
		if (fields == null) {
			ds.setPrimary(null, opt);
			if (ds.isSeqKey()) {
				// ���ź�����
				indexTable = new SeqIndexTable(this);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("ds.lessKey"));
			}
		} else if (ds.getTimeKeyCount() == 1) {
			indexTable = new TimeIndexTable(this, fields, capacity);
		} else if (ds.isSeqKey()) {
			// �����������
			indexTable = new SeqIndexTable(this);
		} else if (fields.length == 1 && opt != null && opt.indexOf('s') != -1) {
			SerialBytesIndexTable sbi = new SerialBytesIndexTable();
			sbi.create(this, fields[0]);
			indexTable = sbi;
		} else if (opt != null && opt.indexOf('b') != -1) {
			indexTable = new SortIndexTable(this, fields);
		} else {
			indexTable = newIndexTable(fields, capacity, opt);
		}
	}
	
	/**
	 * ������ݱ��޸ģ�����������Ѵ������ؽ�������
	 */
	public void rebuildIndexTable() {
		if (indexTable != null) {
			if (indexTable instanceof HashIndexTable) {
				createIndexTable(((HashIndexTable)indexTable).getCapacity(), null);
			} else { // SerialBytesIndexTable
				createIndexTable("s");
			}
		}
	}

	/**
	 * ��������������
	 * @param indexTable ������
	 */
	public void setIndexTable(IndexTable indexTable) {
		this.indexTable = indexTable;
	}
	
	/**
	 * ȡ����������
	 * @return IndexTable
	 */
	public IndexTable getIndexTable() {
		return indexTable;
	}
	
	/**
	 * ȡ�������������û�д����򷵻ؿ�
	 * @param exp �����ֶα��ʽ
	 * @param ctx ����������
	 * @return IndexTable
	 */
	public IndexTable getIndexTable(Expression exp, Context ctx) {
		if (exp == null) {
			return indexTable;
		} else if (indexTable == null) {
			return null;
		}
		
		int []index = ds.getPKIndex();
		if (index != null && index.length == 1 && index[0] == exp.getFieldIndex(ds)) {
			return indexTable;
		} else {
			return null;
		}
	}
	
	/**
	 * ȡ�������������û�д����򷵻ؿ�
	 * @param exps �����ֶα��ʽ����
	 * @param ctx ����������
	 * @return IndexTable
	 */
	public IndexTable getIndexTable(Expression []exps, Context ctx) {
		if (exps == null) {
			return indexTable;
		} else if (indexTable == null) {
			return null;
		}
		
		int keyCount = exps.length;
		int []index = ds.getPKIndex();
		if (index == null || index.length != keyCount) {
			return null;
		}
		
		for (int i = 0; i < keyCount; ++i) {
			if (index[i] != exps[i].getFieldIndex(ds)) {
				return null;
			}
		}
		
		return indexTable;
	}

	/**
	 * ɾ��������
	 */
	public void deleteIndexTable() {
		indexTable = null;
	}
	
	/**
	 * ������������
	 * @return String[]
	 */
	public String[] getPrimary() {
		return ds.getPrimary();
	}

	/**
	 * �����ָ���ֶ�����
	 * @param colIndex �ֶ�������飬��0��ʼ����
	 */
	public void sortFields(int []colIndex) {
		RecordFieldComparator comparator = new RecordFieldComparator(colIndex);
		mems.sort(comparator);
	}

	/**
	 * ����ֶ��Ƿ������ö���
	 * @return boolean true�������ö���false��û�����ö���
	 */
	public boolean checkReference() {
		IArray mems = getMems();
		for (int i = 1, len = mems.size(); i <= len; ++i) {
			Record r = (Record)mems.get(i);
			if (r.checkReference()) return true;
		}

		return false;
	}

	/**
	 * ������ʽ���ɴ�
	 */
	public String toString() {
		IArray mems = getMems();
		int len = mems.size();
		if (len > 10) len = 10;

		StringBuffer sb = new StringBuffer(50 * len);
		String []names = ds.getFieldNames();
		int fcount = names.length;
		int f = 0;
		
		while (true) {
			sb.append(names[f++]);
			if (f < fcount) {
				sb.append('\t');
			} else {
				sb.append("\r\n");
				break;
			}
		}

		for (int i = 1; i <= len; ++i) {
			BaseRecord r = (BaseRecord)mems.get(i);
			for (f = 0; ;) {
				String str = Variant.toString(r.getFieldValue(f));
				sb.append(str);
				f++;
				if (f < fcount) {
					sb.append('\t');
				} else {
					sb.append("\r\n");
					break;
				}
			}
		}

		return sb.toString();
	}

	/**
	 * ���������Ƿ����ָ���ֶ�
	 * @param fieldName �ֶ���
	 * @return true��������false��������
	 */
	public boolean containField(String fieldName) {
		return ds.getFieldIndex(fieldName) != -1;
	}
	
	/**
	 * ȡ�����ֶ���
	 * @return �ֶ���
	 */
	public int getFieldCount() {
		return ds.getFieldCount();
	}
	
	/**
	 * ȡ������¼�����ݽṹ�������һ��Ԫ�ز��Ǽ�¼�򷵻�null
	 * @return ��¼�����ݽṹ
	 */
	public DataStruct getFirstRecordDataStruct() {
		return ds;
	}
	
	/**
	 * �����ڱ�
	 * @param option ��������
	 * @return
	 */
	public Sequence memory(String option) {
		Table srcTable = this;
		if (option != null && option.indexOf('o') != -1) {
			return new MemoryTable(srcTable);
		} else {
			Table table = srcTable.derive("o");
			return new MemoryTable(table);
		}
	}
}
