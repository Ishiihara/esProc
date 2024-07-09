package com.scudata.dm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.IRecord;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

/**
 * �������ݽṹ
 * @author WangXiaoJun
 *
 */
public class DataStruct implements Externalizable, IRecord {
	public static final byte Col_AutoIncrement = 0x01; // ���Զ���������
	
	private static final long serialVersionUID = 0x02010001;
	private static final String DefNamePrefix = "_"; // Ĭ���ֶ���ǰ׺
	
	private static final int SIGN_TIMEKEY = 0x01;
	private static final int SIGN_SEQKEY = 0x02;

	private String[] fieldNames; // �ֶ�����
	private String[] primary; // �ṹ����
	private int sign = 0; // ��־��ȡֵΪ���涨��ĳ���
	transient private int[] pkIndex; // ��������

	// ���л�ʱʹ��
	public DataStruct() {}

	/**
	 * �������ݽṹ
	 * @param fields �ֶ�������
	 */
	public DataStruct(String[] fields) {
		if (fields == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("ds.colSize"));
		}

		int count = fields.length;
		this.fieldNames = fields;

		for (int i = 0; i < count; ++i) {
			String name = fields[i];
			if (name== null || name.length() == 0) {
				name = DefNamePrefix + (i + 1);
				fields[i] = name;
			} /*else if (name.charAt(0) == '#') {
				// �ֶ���������#abc�����ٵ�����ָʾ����
				name = name.substring(1);
				if (name.length() == 0) {
					name = DefNamePrefix + (i + 1);
				}

				if (pkList == null) {
					pkList = new ArrayList<String>();
				}
				
				fields[i] = name;
				pkList.add(name);
			}*/

			// ��������ֵĺϷ��ԣ�sql�����ܲ��������ֶλ���ͬ���ֵ��ֶ�
		}
	}

	/**
	 * �����ݽṹ���л����ֽ�����
	 * @return �ֽ�����
	 */
	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeStrings(fieldNames);
		out.writeStrings(primary);
		out.writeInt(sign);
		return out.toByteArray();
	}

	/**
	 * ���ֽ�����������ݽṹ
	 * @param buf �ֽ�����
	 */
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		fieldNames = in.readStrings();
		if (fieldNames == null) {
			// ����Ϊ0�������������null
			fieldNames = new String[0];
		}
		
		setPrimary(in.readStrings());
		if (in.available() > 0) {
			sign = in.readInt();
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(3); // �汾��
		out.writeObject(fieldNames);
		out.writeObject(primary);
		out.writeInt(sign); // �汾3���
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int v = in.readByte(); // �汾��
		fieldNames = (String[])in.readObject();
		setPrimary((String[])in.readObject());
		
		if (v > 2) { // �汾3���
			sign = in.readInt();
		}
	}

	/**
	 * �������ݽṹ
	 */
	public DataStruct dup() {
		String []names = new String[fieldNames.length];
		System.arraycopy(fieldNames, 0, names, 0, names.length);
		DataStruct ds = new DataStruct(names);
		ds.setPrimary(primary);
		ds.sign = sign;
		return ds;
	}

	/**
	 * ����һ���½ṹ��ʹ��Դ�ṹ��������Ϣ
	 * @param newFields �½ṹ�ֶ���
	 * @return
	 */
	public DataStruct create(String []newFields) {
		DataStruct ds = new DataStruct(newFields);
		String []primary = this.primary;
		
		if (primary != null) {
			int keyCount = primary.length;
			int delCount = 0;
			boolean []signs = new boolean[keyCount];
			for (int i = 0; i < keyCount; ++i) {
				if (ds.getFieldIndex(primary[i]) == -1) {
					delCount++;
					signs[i] = true;
				}
			}

			// ĳ������û�ˣ���������
			if (delCount > 0) {
				if (delCount < keyCount) {
					String []newPrimary = new String[keyCount - delCount];
					for (int i = 0, seq = 0; i < keyCount; ++i) {
						if (!signs[i]) {
							newPrimary[seq] = primary[i];
							seq++;
						}
					}

					ds.setPrimary(newPrimary);
				}
			} else {
				ds.setPrimary(primary);
				ds.sign = sign;
			}
		}

		return ds;
	}

	/**
	 * �����ֶε�����������ֶβ����ڷ���-1
	 * @param fieldName String
	 * @return int
	 */
	public int getFieldIndex(String fieldName) {
		if (fieldName == null || fieldName.length() == 0) return -1;

		int fcount = fieldNames.length;
		for (int i = 0; i < fcount; ++i) {
			if (fieldName.equals(fieldNames[i])) {
				return i;
			}
		}

		// ����id
		if (KeyWord.isFieldId(fieldName)) {
			int i = KeyWord.getFiledId(fieldName);
			if (i > 0 && i <= fcount) {
				return i - 1;
			}
		}
		
		return -1;
	}

	/**
	 * �����ֶε�������
	 * @param int index �ֶ���������0��ʼ����
	 * @return String
	 */
	public String getFieldName(int index) {
		if (index < 0 || index >= fieldNames.length) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(index + mm.getMessage("ds.fieldNotExist"));
		}

		return fieldNames[index];
	}

	/**
	 * �����ֶ�����
	 * @param fieldNames
	 */
	public void setFieldName(String[] fieldNames) {
		this.fieldNames = fieldNames;
	}
	
	/**
	 * �����ֶ���Ŀ
	 * @return int
	 */
	public int getFieldCount() {
		return fieldNames.length;
	}

	/**
	 * �����ֶ���������
	 * @return String[]
	 */
	public String[] getFieldNames() {
		return fieldNames;
	}

	/**
	 * ���������ݽṹ�Ƿ����
	 * @param other DataStruct
	 * @return boolean
	 */
	public boolean isCompatible(DataStruct other) {
		if (other == this) return true;
		if (other == null) return false;

		String []names = other.fieldNames;
		if (fieldNames.length != names.length) return false;

		// �ֶ�˳����Ҫһ��
		for (int i = 0, count = names.length; i < count; ++i) {
			if (names[i] == null || names[i].length() == 0) {
				if (fieldNames[i] != null && fieldNames[i].length() != 0) {
					return false;
				}
			} else {
				if (!names[i].equals(fieldNames[i])) return false;
			}
		}
		
		return true;
	}
	
	/**
	 * �жϽṹ�Ƿ���������ֶ���������ݣ���Ҫ�ֶ�����ͬ�����ֶ�˳����ͬ
	 * @param names �ֶ�������
	 * @return
	 */
	public boolean isCompatible(String []names) {
		if (fieldNames.length != names.length) return false;

		// �ֶ�˳����Ҫһ��
		for (int i = 0, count = names.length; i < count; ++i) {
			if (names[i] == null || names[i].length() == 0) {
				if (fieldNames[i] != null && fieldNames[i].length() != 0) {
					return false;
				}
			} else {
				if (!names[i].equals(fieldNames[i])) return false;
			}
		}
		
		return true;
	}

	/**
	 * ���ýṹ������
	 * @param names String[]
	 */
	public void setPrimary(String []names) {
		setPrimary(names, null);
	}
	
	/**
	 * ���ýṹ������
	 * @param names String[]
	 * @param opt String t�����һ��Ϊ
	 */
	public void setPrimary(String []names, String opt) {
		sign = 0;
		if (names == null || names.length == 0) {
			this.primary = null;
			pkIndex = null;
			
			if (opt != null && opt.indexOf('n') != -1) {
				sign |= SIGN_SEQKEY;
			}
		} else {
			int count = names.length;
			int []tmpIndex = new int[count];
			for (int i = 0; i < count; ++i) {
				tmpIndex[i] = getFieldIndex(names[i]);
				if (tmpIndex[i] == -1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(names[i] + mm.getMessage("ds.fieldNotExist"));
				}
			}

			this.primary = new String[count];
			System.arraycopy(names, 0, this.primary, 0, count);
			pkIndex = tmpIndex;
			if (opt != null) {
				if (opt.indexOf('t') != -1) {
					sign |= SIGN_TIMEKEY;
				}
				
				if (opt.indexOf('n') != -1) {
					sign |= SIGN_SEQKEY;
				}
			}
		}
	}

	/**
	 * ���ؽṹ������
	 * @return String[]
	 */
	public String[] getPrimary() {
		return primary;
	}

	/**
	 * ���������Ƿ�����ż�
	 * @return
	 */
	public boolean isSeqKey() {
		return (sign & SIGN_SEQKEY) == SIGN_SEQKEY;
	}
	
	/**
	 * ȡʱ���������û��ʱ����򷵻�0
	 * @return
	 */
	public int getTimeKeyCount() {
		return (sign & SIGN_TIMEKEY) == SIGN_TIMEKEY ? 1 : 0;
	}

	/**
	 * ���������ڽṹ�е�������û�ж��������򷵻ؿ�
	 * @return int[]
	 */
	public int[] getPKIndex() {
		return pkIndex;
	}

	/**
	 * ȡ�����������û�����������򷵻�0
	 * @return
	 */
	public int getPKCount() {
		if (primary != null) {
			return primary.length;
		} else {
			return 0;
		}
	}
	
	/**
	 * ȡ�����������������������¼�
	 * @return int[]
	 */
	public int[] getBaseKeyIndex() {
		if (getTimeKeyCount() == 0) {
			return pkIndex;
		} else {
			int count = pkIndex.length - 1;
			int []index = new int[count];
			System.arraycopy(pkIndex, 0, index, 0, count);
			return index;
		}
	}
	
	/**
	 * ȡ����ʱ���������
	 * @return
	 */
	public int getTimeKeyIndex() {
		return pkIndex[pkIndex.length - 1];
	}
	
	/**
	 * ������ָ���ֶ�
	 * @param srcFields
	 * @param newFields
	 */
	public void rename(String []srcFields, String []newFields) {
		if (srcFields == null) {
			return;
		}
		
		String[] fieldNames = this.fieldNames; // ��ͨ�ֶ�
		for (int i = 0, count = srcFields.length; i < count; ++i) {
			int f = getFieldIndex(srcFields[i]);
			if (f < 0) {
				continue; // ������ʱ���ԣ����ٱ���
				//MessageManager mm = EngineMessage.get();
				//throw new RQException(srcFields[i] + mm.getMessage("ds.fieldNotExist"));
			}
			
			if (newFields[i] != null) {
				fieldNames[f] = newFields[i];
			} else {
				fieldNames[f] = DefNamePrefix + (f + 1);
			}
			
			if (pkIndex != null) {
				for (int k = 0; k < pkIndex.length; ++k) {
					if (pkIndex[k] == f) {
						primary[k] = fieldNames[f];
						break;
					}
				}
			}
		}
	}
	
	/**
	 * �жϱ��ʽ�Ƿ���ָ�����ֶ�
	 * @param exps ���ʽ����
	 * @param fields �ֶ���������
	 * @return true������ͬ�ֶ�
	 */
	public boolean isSameFields(Expression []exps, int []fields) {
		int len = exps.length;
		if (len != fields.length) {
			return false;
		}
		
		for (int i = 0; i < len; ++i) {
			String field = exps[i].getIdentifierName();
			if (fields[i] != getFieldIndex(field)) {
				return false;
			}
		}
		
		return true;
	}
}
