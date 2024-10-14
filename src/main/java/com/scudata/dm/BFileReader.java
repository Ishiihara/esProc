package com.scudata.dm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.scudata.array.IArray;
import com.scudata.array.IntArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.cursor.PFileCursor;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ���ļ���ȡ����
 * @author WangXiaoJun
 *
 */
public class BFileReader {
	private FileObject file; // ���ļ���Ӧ���ļ�����
	private int type; // ���ļ�����
	private long []blocks; // ÿһ��Ľ���λ��
	private int lastBlock; // ���һ�������
	private long totalRecordCount;	// ��¼����
	private long blockRecordCount;	// ��������
	private long firstRecordPos; // ��һ����¼��λ��
	
	private DataStruct ds; // ���ļ����ݽṹ
	private DataStruct readDs; // ѡ���ֶ���ɵ����ݽṹ
	private String []readFields; // ѡ���ֶ�
	private int []readIndex; // ѡ���ֶζ�Ӧ�����
	private boolean isSingleField; // �Ƿ񷵻ص�����ɵ�����
	private boolean isSequenceMember; // �Ƿ񷵻�������ɵ�����
	private boolean isExist = true; // �ֶ��Ƿ����ļ���

	private int segSeq; // �ֶ���ţ���1��ʼ����
	private int segCount; // �ֶ���
	private long endPos = -1; // ��ȡ�Ľ���λ�ã����ڶ��̷ֶ߳ζ�ȡ
	
	private ObjectReader importer; // �����ȡ��
	
	/**
	 * ���ļ����󴴽����ļ���ȡ��
	 * 
	 * @param file	�ļ�����
	 */
	public BFileReader(FileObject file) {
		this(file, null, null);
	}
	
	/**
	 * ���ļ����������Ͷ�д�ַ��������������ļ�
	 * @param file �ļ�����
	 * @param fields ѡ���ֶ�
	 * @param opt ѡ�i�������ֻ��1��ʱ���س����У�e�����ļ��в�����ʱ������null��ȱʡ������w����ÿ�ж�������
	 */
	public BFileReader(FileObject file, String []fields, String opt) {
		this(file, fields, 1, 1, opt);
	}

	/**
	 * ���ļ����������Ͷ�д�ַ��������������ļ�
	 * @param file �ļ�����
	 * @param fields ѡ���ֶ�
	 * @param segSeq Ҫ��ȡ�Ķκţ���1��ʼ����
	 * @param segCount �ֶ���
	 * @param opt ѡ�i�������ֻ��1��ʱ���س����У�e�����ļ��в�����ʱ������null��ȱʡ������w����ÿ�ж�������
	 */
	public BFileReader(FileObject file, String []fields, int segSeq, int segCount, String opt) {
		this.file = file;
		this.segSeq = segSeq;
		this.segCount = segCount;
		
		if (fields != null) {
			readFields = new String[fields.length];
			System.arraycopy(fields, 0, readFields, 0, fields.length);
		}
		
		if (opt != null) {
			if (opt.indexOf('i') != -1) isSingleField = true;
			if (opt.indexOf('e') != -1) isExist = false;
			if (opt.indexOf('w') != -1) isSequenceMember = true;
		}
		
		if (segCount > 1) {
			if (segSeq < 0 || segSeq > segCount) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(segSeq + mm.getMessage("function.invalidParam"));
			}
		}
	}
	
	/**
	 * �����нṹ
	 * @return DataStruct
	 */
	public DataStruct getFileDataStruct() {
		return ds;
	}
	
	/**
	 * ���ļ����зֶ�
	 * 
	 * @throws IOException
	 */
	private void doSegment() throws IOException {
		int segSeq = this.segSeq - 1;
		int avg = (lastBlock + 1) / segCount;
		
		if (avg < 1) {
			// ÿ�β���һ��
			if (segSeq > lastBlock) {
				endPos = 0;
			} else {
				endPos = blocks[segSeq];
				if (segSeq > 0) {
					importer.seek(blocks[segSeq - 1]);
				}
			}
		} else {
			if (segSeq > 0) {
				int s = segSeq * avg - 1;
				int e = s + avg;
				
				// ʣ��Ŀ�����ÿ�ζ�һ��
				int mod = (lastBlock + 1) % segCount;
				int n = mod - (segCount - segSeq - 1);
				if (n > 0) {
					e += n;
					s += n - 1;
				}
				
				endPos = blocks[e];
				importer.seek(blocks[s]);
			} else {
				endPos = blocks[avg - 1];
			}
		}
	}
	
	/**
	 * ��ǰ����������λ��
	 * @return	���ص�ǰ������λ��
	 */
	public long position() {
		return importer.position();
	}
	
	/**
	 * ��λ��������λ�á�
	 * ֧�����λ����ǰ��λ�Ļ�����������������Χ���ͻ����쳣��
	 * @param pos	Ҫ��λ��λ��
	 * @throws IOException
	 */
	public void seek(long pos) throws IOException {
		importer.seek(pos);
	}
	
	/**
	 * ��ǰ��ȡ���Ƿ��
	 * 
	 * @return
	 */
	public boolean isOpen() {
		return importer != null;
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException {
		open(Env.FILE_BUFSIZE);
	}
	
	/**
	 * ���´�
	 * 
	 * @param bufSize	�������Ĵ�С
	 */
	private void reopen(int bufSize) {
		InputStream in = file.getBlockInputStream(bufSize);
		ObjectReader importer = new ObjectReader(in, bufSize);
		this.importer = importer;
	}
	
	/**
	 * ��һ���������ļ�������bufSize��ʼ���ļ���ȡ��Ļ�������С��
	 * 
	 * @param bufSize	�������Ĵ�С
	 * @throws IOException
	 */
	public void open(int bufSize) throws IOException {
		InputStream in = file.getBlockInputStream(bufSize);
		ObjectReader importer = new ObjectReader(in, bufSize);
		this.importer = importer;
		
		if (importer.read() != 'r' || importer.read() != 'q' || importer.read() != 't' || 
				importer.read() != 'b' || importer.read() != 'x') {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("license.fileFormatError"));
		}
		
		type = importer.read();
		int ver = importer.readInt32();
		
		if (type == BFileWriter.TYPE_NORMAL) {
			if (segCount > 1) {
				//MessageManager mm = EngineMessage.get();
				//throw new RQException(mm.getMessage("engine.needZFile"));
				if (segSeq > 1) {
					endPos = 0;
				}
			}
			
			totalRecordCount = importer.readLong64();
			ds = new DataStruct(importer.readStrings());
			firstRecordPos = position();
		} else if (type == BFileWriter.TYPE_BLOCK) {
			totalRecordCount = importer.readLong64();
			blockRecordCount = importer.readLong64();
			importer.readLong64(); // lastRecordCount
			lastBlock = importer.readInt32();
			
			int count = importer.readInt32();
			long []blocks = new long[count];
			this.blocks = blocks;
			for (int i = 0; i < count; ++i) {
				blocks[i] = importer.readLong64();
			}
			
			ds = new DataStruct(importer.readStrings());
			firstRecordPos = position();
			
			if (segCount > 1) {
				doSegment();
			}
		} else if (type == BFileWriter.TYPE_GROUP) {
			totalRecordCount = importer.readLong64();
			if (ver > 0) {
				blockRecordCount = importer.readLong64();
				importer.readLong64(); // lastRecordCount
			}
			
			lastBlock = importer.readInt32();
			int count = importer.readInt32();
			long []blocks = new long[count];
			this.blocks = blocks;
			for (int i = 0; i < count; ++i) {
				blocks[i] = importer.readLong64();
			}
			
			ds = new DataStruct(importer.readStrings());
			firstRecordPos = position();
			
			if (segCount > 1) {
				doSegment();
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("license.fileFormatError"));
		}
		
		String []fields = ds.getFieldNames();
		int fcount = fields.length;
		
		if (readFields != null) {
			if (isSingleField) {
				isSingleField = readFields.length == 1;
			}
			
			readIndex = new int[fcount];
			for (int i = 0; i < fcount; ++i) {
				readIndex[i] = -1;
			}

			for (int i = 0, count = readFields.length; i < count; ++i) {
				int q = ds.getFieldIndex(readFields[i]);
				if (q >= 0) {
					if (readIndex[q] != -1) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(readFields[i] + mm.getMessage("ds.colNameRepeat"));
					}

					readIndex[q] = i;
					readFields[i] = fields[q];
				} else if (isExist) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(readFields[i] + mm.getMessage("ds.fieldNotExist"));
				}
			}

			readDs = new DataStruct(readFields);
		} else {
			if (isSingleField) isSingleField = fcount == 1;
		}
	}
	
	/**
	 * �ر��ļ����ر���
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (importer != null) {
			importer.close();
			importer = null;
		}
	}
	
	/**
	 * ��ȡ�ļ������еļ�¼����������з��ء�
	 * 
	 * @return
	 * @throws IOException
	 */
	public Sequence readAll() throws IOException {
		return read(ICursor.MAXSIZE);
	}
	
	/**
	 * ��ȡָ�������ļ�¼
	 * @param n	Ҫ��ȡ�ļ�¼��
	 * @return	�����б���ļ�¼��
	 * @throws IOException
	 */
	public Sequence read(int n) throws IOException {
		long endPos = this.endPos;
		ObjectReader importer = this.importer;
		if (n < 1 || (endPos != -1 && importer.position() >= endPos)) {
			return null;
		}

		int fcount = ds.getFieldCount();
		int initSize;
		if (n <= ICursor.FETCHCOUNT) {
			initSize = n;
		} else if (n >= totalRecordCount && totalRecordCount > 0) {
			// �ֶζ����Ƽ��˼�¼����;
			initSize = (int)totalRecordCount;
		} else if (n < ICursor.MAXSIZE) {
			initSize = n;
		} else {
			initSize = ICursor.INITSIZE;
		}
		
		if (isSingleField) {
			Sequence seq = new Sequence(initSize);
			if (readFields == null) {
				for (int i = 0; i < n; ++i) {
					if (importer.hasNext() && (endPos == -1 || importer.position() < endPos)) {
						seq.add(importer.readObject());
					} else {
						break;
					}
				}
			} else {
				int []readIndex = this.readIndex;
				for (int i = 0; i < n; ++i) {
					if (importer.hasNext() && (endPos == -1 || importer.position() < endPos)) {
						for (int f = 0; f < fcount; ++f) {
							if (readIndex[f] != -1) {
								seq.add(importer.readObject());
							} else {
								importer.skipObject();
							}
						}
					} else {
						break;
					}
				}
			}

			if (seq.length() != 0) {
				return seq;
			} else {
				return null;
			}
		} else if (isSequenceMember) {
			Sequence seq = new Sequence(initSize);
			if (readFields == null) {
				Sequence tmp = new Sequence(ds.getFieldNames());
				seq.add(tmp);
				Object []values = new Object[fcount];
				for (int i = 0; i < n; ++i) {
					if (importer.hasNext() && (endPos == -1 || importer.position() < endPos)) {
						for (int f = 0; f < fcount; ++f) {
							values[f] = importer.readObject();
						}
						
						seq.add(new Sequence(values));
					} else {
						break;
					}
				}
			} else {
				Sequence tmp = new Sequence(readFields);
				seq.add(tmp);
				Object []values = new Object[readFields.length];
				int []readIndex = this.readIndex;
				for (int i = 0; i < n; ++i) {
					if (importer.hasNext() && (endPos == -1 || importer.position() < endPos)) {
						for (int f = 0; f < fcount; ++f) {
							if (readIndex[f] != -1) {
								values[readIndex[f]] = importer.readObject();
							} else {
								importer.skipObject();
							}
						}
						
						seq.add(new Sequence(values));
					} else {
						break;
					}
				}
			}

			if (seq.length() != 0) {
				//seq.trimToSize();
				return seq;
			} else {
				return null;
			}
		} else {
			Table table;
			if (readFields == null) {
				table = new Table(ds, initSize);
				for (int i = 0; i < n; ++i) {
					if (importer.hasNext() && (endPos == -1 || importer.position() < endPos)) {
						BaseRecord cur = table.newLast();
						for (int f = 0; f < fcount; ++f) {
							cur.setNormalFieldValue(f, importer.readObject());
						}
					} else {
						break;
					}
				}
			} else {
				int []readIndex = this.readIndex;
				table = new Table(readDs, initSize);

				for (int i = 0; i < n; ++i) {
					if (importer.hasNext() && (endPos == -1 || importer.position() < endPos)) {
						BaseRecord cur = table.newLast();
						for (int f = 0; f < fcount; ++f) {
							if (readIndex[f] != -1) {
								cur.setNormalFieldValue(readIndex[f], importer.readObject());
							} else {
								importer.skipObject();
							}
						}
					} else {
						break;
					}
				}
			}

			if (table.length() != 0) {
				//table.trimToSize();
				return table;
			} else {
				return null;
			}
		}
	}
	
	/**
	 * ����������ָ���ļ�¼��
	 * 
	 * @param n	Ҫ�����ļ�¼��
	 * @return	����ʵ�ʵ������ļ�¼��
	 * @throws IOException
	 */
	public long skip(long n) throws IOException {
		if (totalRecordCount > 0 && segCount <= 1 && firstRecordPos == position() && endPos == -1) {
			if (totalRecordCount <= n) {
				seek(file.size());
				return totalRecordCount;
			}

			if (type == BFileWriter.TYPE_BLOCK && blockRecordCount < n) {
				int i = (int)(n / blockRecordCount);
				seek(blocks[i - 1]);
				skip(n - blockRecordCount * i);
				return n;
			}
		}
		
		ObjectReader importer = this.importer;
		if (n < 1 || (endPos != -1 && importer.position() >= endPos)) {
			return 0;
		}

		int fcount = ds.getFieldCount();
		for (long i = 0; i < n; ++i) {
			if (importer.hasNext() && (endPos == -1 || importer.position() < endPos)) {
				for (int f = 0; f < fcount; ++f) {
					importer.skipObject();
				}
			} else {
				return i;
			}
		}

		return n;
	}
	
	/**
	 * ȡ��ǰ��¼��ָ���ֶ�
	 * 
	 * @param fields	�ֶα�ǣ�ֵΪ-1�Ĳ�ȡ����
	 * @param values	�������
	 * @return		true	ȡ���ɹ�
	 * 				false	ȡ��ʧ��
	 * @throws IOException
	 */
	public boolean readRecord(int []fields, Object []values) throws IOException {
		ObjectReader importer = this.importer;
		if (importer.hasNext()) {
			for (int f = 0, fcount = fields.length; f < fcount; ++f) {
				if (fields[f] != -1) {
					values[fields[f]] = importer.readObject();
				} else {
					importer.skipObject();
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * ��ȡһ��һ����¼��ĳ���ֶΡ�
	 * @param field	ָ�����ֶ�������
	 * @return	����ֵ��ȡ�Ľ��
	 * @throws IOException
	 */
	private Object readRecordField(int field) throws IOException {
		ObjectReader importer = this.importer;
		for (int f = 0; f < field; ++f) {
			importer.skipObject();
		}
		
		return importer.readObject();
	}
	
	// ȡ��ǰ��¼���ֶ�
	/**
	 * ��ȡһ����¼
	 * 
	 * @param values	�����¼�Ķ�������
	 * @return			true	ȡ���ɹ�
	 * 					false	ȡ��ʧ��
	 * @throws IOException
	 */
	public boolean readRecord(Object []values) throws IOException {
		ObjectReader importer = this.importer;
		if (importer.hasNext()) {
			for (int f = 0, fcount = values.length; f < fcount; ++f) {
				values[f] = importer.readObject();
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * ������¼��������ļ�β�򷵻�false
	 * @return
	 * @throws IOException
	 */
	public boolean skipRecord() throws IOException {
		ObjectReader importer = this.importer;
		if (importer.hasNext()) {
			for (int f = 0, fcount = ds.getFieldCount(); f < fcount; ++f) {
				importer.skipObject();
			}
			
			return true;
		} else {
			return false;
		}
	}
			
	/**
	 * �Ӷ�x������ļ�f����x������A�еļ�¼�����α�
	 * �ӵ�ǰ���ݼ��У�ѡ��keyֵ��values�еļ�¼��ѡȡfields�ֶ�ֵ�����±��������±���αꡣ
	 * @param key		�Ѿ��ź�����ֶε��ֶ�����
	 * @param values	�ο�ֵ����key�ֶ�����Щֵ���Ա�
	 * @param fields	�ֶ����б����յý��������Щ�ֶ����
	 * @param opt		e���ֶ���Դ����в�����ʱ������null��ȱʡ������
	 * @param ctx		�����ı���
	 * @return			����ɸѡ�������ݼ����α�
	 */
	public ICursor iselect(String key, Sequence values, String []fields, String opt, Context ctx) {
		int count = values.length();
		if (count == 0) {
			//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
			return new MemoryCursor(null);
		}
		
		try {
			// �򿪶������ļ��������û�������С
			open(1024);
			long []blocks = this.blocks;
			if (blocks == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			
			// ȡ���ֶ�����
			int keyField = ds.getFieldIndex(key);
			if (keyField < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(key + mm.getMessage("ds.fieldNotExist"));
			}
			
			int lastBlock = this.lastBlock;
			int fcount = ds.getFieldCount();
			Object []vals = new Object[fcount];
			
			// ����ֶ�
			LongArray posArray = new LongArray(count > 5 ? count * 2 : 10);
			long prevEnd = position();
			int nextBlock = 0;
			Object nextBlockVal = null;
			if (lastBlock > 0) {
				seek(blocks[0]);
				readRecord(vals);
				nextBlockVal = vals[keyField];
			}
			
			int i = 1;
			while (i <= count && nextBlock < lastBlock) {
				Object val = values.getMem(i);
				int cmp = Variant.compare(val, nextBlockVal);
				
				// ��Ϊ�ļ��������ظ���ֵ����ȵ�ʱ����Ҫ��ǰһ�鿪ʼ��
				if (cmp <= 0) {
					if (position() > prevEnd) {
						close();
						reopen(1024);
						seek(prevEnd);
					}
					
					while (true) {
						readRecord(vals);
						cmp = Variant.compare(val, vals[keyField]);
						if (cmp > 0) {
							prevEnd = position();
							if (prevEnd == blocks[nextBlock]) {
								nextBlock++;
								if (nextBlock < lastBlock) {
									seek(blocks[nextBlock]);
									readRecord(vals);
									nextBlockVal = vals[keyField];
								}
								
								break;
							}
						} else if (cmp == 0) {
							posArray.add(prevEnd);
							prevEnd = position();
							if (prevEnd == blocks[nextBlock]) {
								nextBlock++;
								if (nextBlock < lastBlock) {
									seek(blocks[nextBlock]);
									readRecord(vals);
									nextBlockVal = vals[keyField];
								}
								
								break;
							}

							// �ļ��������ظ���ֵ����������û�к͵�ǰֵ�ظ���
							continue;
						} else {
							i++;
							while (i <= count) {
								val = values.getMem(i);
								cmp = Variant.compare(val, vals[keyField]);
								if (cmp > 0) {
									break;
								} else if (cmp == 0) {
									posArray.add(prevEnd);
									break;
								} else {
									i++;
								}
							}
							
							prevEnd = position();
							if (prevEnd == blocks[nextBlock]) {
								nextBlock++;
								if (nextBlock < lastBlock) {
									seek(blocks[nextBlock]);
									readRecord(vals);
									nextBlockVal = vals[keyField];
								}
								
								break;
							}
							
							break;
						}
					}
				} else {
					prevEnd = blocks[nextBlock];					
					nextBlock++;
					if (nextBlock < lastBlock) {
						seek(blocks[nextBlock]);
						readRecord(vals);
						nextBlockVal = vals[keyField];
					}
				}
			}
			
			if (i <= count) {
				if (position() > prevEnd) {
					close();
					reopen(1024);
					seek(prevEnd);
				}
				
				Object val = values.getMem(i);
				while (i <= count && readRecord(vals)) {
					int cmp = Variant.compare(val, vals[keyField]);
					if (cmp > 0) {
						prevEnd = position();
					} else if (cmp == 0) {
						posArray.add(prevEnd);
						prevEnd = position();
					} else {
						i++;
						while (i <= count) {
							val = values.getMem(i);
							cmp = Variant.compare(val, vals[keyField]);
							if (cmp > 0) {
								break;
							} else if (cmp == 0) {
								posArray.add(prevEnd);
								break;
							} else {
								i++;
							}
						}
						
						prevEnd = position();
					}
				}
			}
			
			if (posArray.size() == 0) {
				//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
				return new MemoryCursor(null);
			}
						
			return new PFileCursor(file, posArray.toArray(), 1024, fields, opt, ctx);
		} catch (IOException e) {
			throw new RQException(e);
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * �Ӷ�x������ļ�f����x��[a,b]����ļ�¼�����α�
	 * �ӵ�ǰ���ݼ��У�ѡ��keyֵ��startVal��endVal֮��ļ�¼��ѡȡfields�ֶ�ֵ�����±��������±���αꡣ
	 * @param key		�Ѿ��ź�����ֶε��ֶ�����
	 * @param startVal	ɸѡ���ݵ���ʼֵ
	 * @param endVal	ɸѡ���ݵĽ���ֵ
	 * @param fields	�ֶ����б����յý��������Щ�ֶ����
	 * @param opt		e���ֶ���Դ����в�����ʱ������null��ȱʡ������
	 * @param ctx		�����ı���
	 * @return
	 */
	public ICursor iselect(String key, Object startVal, Object endVal, String []fields, String opt, Context ctx) {
		int startBlock;
		int endBlock ;
		int keyField;
		long firstPos;
		
		try {
			open(1024);
			firstPos = position();
			long []blocks = this.blocks;
			if (blocks == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			
			keyField = ds.getFieldIndex(key);
			if (keyField < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(key + mm.getMessage("ds.fieldNotExist"));
			}
			
			int lastBlock = this.lastBlock;
			startBlock = lastBlock;
			endBlock = lastBlock;
			
			for (int i = 0; i < lastBlock; ++i) {
				seek(blocks[i]);
				Object val = readRecordField(keyField);
				if (Variant.compare(val, startVal) >= 0) {
					startBlock = i;
					if (endVal != null && Variant.compare(val, endVal) > 0) {
						endBlock = i;
					}
					
					break;
				}
			}
			
			if (endVal != null && endBlock != startBlock) {
				for (int i = startBlock + 1; i < lastBlock; ++i) {
					seek(blocks[i]);
					Object val = readRecordField(keyField);
					if (Variant.compare(val, endVal) > 0) {
						endBlock = i;
						break;
					}
				}
			}
		} catch (IOException e) {
			throw new RQException(e);
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
		
		try {
			reopen(1024);
			seek(firstPos);
			
			long []blocks = this.blocks;
			long startPos = blocks[startBlock];
			int fcount = ds.getFieldCount();
			Object []vals = new Object[fcount];
			
			if (startBlock > 0) {
				seek(blocks[startBlock - 1]);
			}
			
			long pos = firstPos;
			while (pos < startPos) {
				readRecord(vals);
				if (Variant.compare(vals[keyField], startVal) >= 0) {
					if (endVal != null && Variant.compare(vals[keyField], endVal) > 0) {
						//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
						return new MemoryCursor(null);
					}
					
					startPos = pos;
					break;
				}
				
				pos = position();
			}
			
			long endPos = blocks[endBlock];
			if (endVal != null) {
				if (endBlock > 0 && position() < blocks[endBlock - 1]) {
					seek(blocks[endBlock - 1]);
				}
				
				pos = position();
				while (pos < endPos) {
					readRecord(vals);
					if (Variant.compare(vals[keyField], endVal) > 0) {
						endPos = pos;
						break;
					}
					
					pos = position();
				}
			}
			
			if (startPos < endPos) {
				BFileCursor cursor = new BFileCursor(file, fields, opt, ctx);
				cursor.setPosRange(startPos, endPos);
				return cursor;
			} else {
				//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
				return new MemoryCursor(null);
			}
		} catch (IOException e) {
			throw new RQException(e);
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * ���ö�ȡ�Ľ���λ�ã����ڶ��̷ֶ߳ζ�ȡ
	 * @param pos λ��
	 */
	public void setEndPos(long pos) {
		this.endPos = pos;
	}
	
	/**
	 * ȡ��������ݽṹ
	 * @return
	 */
	public DataStruct getResultSetDataStruct() {
		if (readDs == null) {
			return ds;
		} else {
			return readDs;
		}
	}
	
	/**
	 * �Աȶ���ֶε�ֵ
	 * 
	 * @param fieldsValue	�ֶ�ֵ
	 * @param refValues		�ο�ֵ
	 * @return	1	fieldsValue��ֵ�Ƚϴ�
	 * 			0	��������һ����
	 * 			-1	refValues��ֵ�Ƚϴ�
	 */
	private int	compareFields(Object[] fieldsValue, Object refValues ) {
		Object refObj = null;
		for (int i = 0; i < fieldsValue.length; i++) {
			if (refValues instanceof Sequence) {
				refObj = ((Sequence)refValues).get(i+1);
			} else
				refObj = refValues;
			
			int res = Variant.compare(fieldsValue[i], refObj);
			if (res > 0)
				return 1;
			else if (res < 0)
				return -1;
		}
		return 0;
	}
	
	/**
	 * �Աȶ���ֶε�ֵ
	 * 
	 * @param fieldsValue	�ֶ�ֵ
	 * @param refValues		�ο�ֵ
	 * @return	1	refValues��ֵ�Ƚϴ�
	 * 			0	��������һ����
	 * 			-1	fieldsValue��ֵ�Ƚϴ�
	 */	
	private int compareFields(Object refValues, Object[] fieldsValue) {
		Object refObj = null;
		for (int i = 0; i < fieldsValue.length; i++) {
			if (refValues instanceof Sequence) {
				refObj = ((Sequence)refValues).get(i+1);
			} else
				refObj = refValues;
			
			int res = Variant.compare(fieldsValue[i], refObj);
			if (res > 0)
				return -1;
			else if (res < 0)
				return 1;
		}
		return 0;		
	}
	
	/**
	 * ѡ�����ʽ�ļ�������values�еļ�¼
	 * @param exp		���ʽ
	 * @param values	�ԱȽ��
	 * @param fields	��ɽ�����ֶ�
	 * @param opt		e���ֶ���Դ����в�����ʱ������null��ȱʡ������
	 * @param ctx		�����ı���
	 * @return			����ɸѡ���ļ�¼
	 */
	public ICursor iselect(Expression exp, Sequence values, String []fields, String opt, Context ctx) {
		if (exp == null) {
			//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
			return new MemoryCursor(null);
		}
		
		// ���ֶα��ʽ������������̣����Դ�����Ч��
		//  ������ʽ�Ƿ�ʽ���ַ���
		String[] fieldNames = exp.toFields();
		
		try {
			open(1024);
			
			if (blocks == null) {
				// ���ֶμ��ļ�����˳�����
				Sequence result = readAll();
				Sequence fieldValues = result.calc(exp, ctx);
				IArray valueArray = fieldValues.getMems();
				IntArray seqArray = null;
				
				for (int i = 1, len = values.length(); i <= len; ++i) {
					IntArray array = valueArray.indexOfAll(values.getMem(i), 1, true, true);
					if (seqArray == null) {
						seqArray = array;
					} else {
						seqArray.addAll(array);
					}
				}
				
				if (seqArray == null || seqArray.size() == 0) {
					return new MemoryCursor(null);
				} else if (fields == null) {
					result = result.get(new Sequence(seqArray));
					return new MemoryCursor(result);
				} else {
					result = result.get(new Sequence(seqArray));
					result = result.fieldsValues(fields, opt);
					return new MemoryCursor(result);
				}
			}
			
			if (fieldNames != null) {
				for (String name : fieldNames) {
					if (ds.getFieldIndex(name) == -1) {
						fieldNames = null;
						break;
					}
				}
			}
		} catch (IOException e) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("file.fileNotExist", file.getFileName()));
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
		
				
		if (fieldNames != null) {
			return iselectFields(fieldNames, values, fields, opt, ctx);
		} else {
			return iselectExpression(exp, values, fields, opt, ctx);
		}
	}
	
	/**
	 * ���ֶΡ����ֶδӵ�ǰ���ݼ���ѡ���� values�еļ�¼
	 * 
	 * @param	refFields	�ο��ֶ�
	 * @param	values		�ο��ֶεĲο�ֵ
	 * @param	fields      �����±���ֶΣ����Բ������ο��ֶ�
	 * @param	opt			e���ֶ���Դ����в�����ʱ������null��ȱʡ������
	 * @param	ctx			�����ı���
	 * 
	 * @return	����ɸѡ�������ݼ�cursor
	 * 
	*/
	public ICursor iselectFields(String[] refFields, Sequence values, String []fields, String opt, Context ctx) {
		// ���Ҷ�Ӧ�е�����
		int fcount = ds.getFieldCount();
		int[] selFields = new int[fcount];
		for (int i = 0; i < fcount; i++) {
			selFields[i] = -1;
		}

		int fcou = 0;
		for (int i = 0; i < refFields.length; i++) {
			int index = ds.getFieldIndex(refFields[i]);
			if (0 > index ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(refFields[i] + mm.getMessage("ds.fieldNotExist"));
			}
			selFields[index] = fcou;
			fcou++;
		}
		
		// ȡ�ü�¼����
		int count = values.length();
		if (count == 0) {
			//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
			return new MemoryCursor(null);
		}
		
		try {
			// ���ļ�
			open(1024);
			long []blocks = this.blocks;
			if (blocks == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
					
			int lastBlock = this.lastBlock;
			Object []vals = new Object[fcou];
			
			LongArray posArray = new LongArray(count > 5 ? count * 2 : 10);
			long prevEnd = position();
			int nextBlock = 0;
			Object[] nextBlockVal = null;
			if (lastBlock > 0) {
				seek(blocks[0]);
				readRecord(selFields, vals);
				nextBlockVal = vals.clone();
			}
			
			int i = 1;
			while (i <= count && nextBlock < lastBlock) {
				// ȡ��һ���ο�ֵ
				Object val = values.getMem(i);
				int cmp = compareFields(val, nextBlockVal);
				if (cmp <= 0) {
					if (position() > prevEnd) {
						close();
						reopen(1024);
						seek(prevEnd);
					}
					
					while (true) {
						readRecord(selFields, vals);
						cmp = compareFields(val, vals);
						if (cmp > 0) {
							prevEnd = position();
							if (prevEnd == blocks[nextBlock]) {
								nextBlock++;
								if (nextBlock < lastBlock) {
									seek(blocks[nextBlock]);
									readRecord(selFields, vals);
									nextBlockVal = vals.clone();
								}
								
								break;
							}
						} else if (cmp == 0) {
							posArray.add(prevEnd);
							prevEnd = position();
							if (prevEnd == blocks[nextBlock]) {
								nextBlock++;
								if (nextBlock < lastBlock) {
									seek(blocks[nextBlock]);
									readRecord(selFields, vals);
									nextBlockVal = vals.clone();
								}
								
								break;
							}

							continue;
						} else {
							i++;
							while (i <= count) {
								// ȡ��һ���ο�ֵ
								val = values.getMem(i);
								cmp = compareFields(val, vals);
								if (cmp > 0) {
									break;
								} else if (cmp == 0) {
									posArray.add(prevEnd);
									break;
								} else {
									i++;
								}
							}
							
							prevEnd = position();
							if (prevEnd == blocks[nextBlock]) {
								nextBlock++;
								if (nextBlock < lastBlock) {
									seek(blocks[nextBlock]);
									readRecord(selFields, vals);
									nextBlockVal = vals.clone();
								}
								
								break;
							}
							
							break;
						}
					}
				} else {
					prevEnd = blocks[nextBlock];					
					nextBlock++;
					if (nextBlock < lastBlock) {
						seek(blocks[nextBlock]);
						readRecord(selFields, vals);
						nextBlockVal = vals.clone();
					}
				}
			}
			
			if (i <= count) {
				if (position() > prevEnd) {
					close();
					reopen(1024);
					seek(prevEnd);
				}
				
				Object val = values.getMem(i);
				while (i <= count && readRecord(selFields, vals)) {
					int cmp = compareFields(val, vals);
					if (cmp > 0) {
						prevEnd = position();
					} else if (cmp == 0) {
						posArray.add(prevEnd);
						prevEnd = position();
					} else {
						i++;
						while (i <= count) {
							val = values.getMem(i);
							cmp = compareFields(val, vals);
							if (cmp > 0) {
								break;
							} else if (cmp == 0) {
								posArray.add(prevEnd);
								break;
							} else {
								i++;
							}
						}
						
						prevEnd = position();
					}
				}
			}
			
			if (posArray.size() == 0) {
				//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
				return new MemoryCursor(null);
			}
						
			return new PFileCursor(file, posArray.toArray(), 1024, fields, opt, ctx);
		} catch (IOException e) {
			throw new RQException(e);
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
	}
	
	private ICursor iselectExpression(Expression exp, Sequence values, String []fields, String opt, Context ctx) {
		int count = values.length();
		if (count == 0) {
			//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
			return new MemoryCursor(null);
		}
		
		try {
			open(1024);
			long []blocks = this.blocks;
			if (blocks == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			
			int lastBlock = this.lastBlock;
			int fcount = ds.getFieldCount();
			Object []vals = new Object[fcount];
			Record rec = new Record(ds);
			
			LongArray posArray = new LongArray(count > 5 ? count * 2 : 10);
			long prevEnd = position();
			int nextBlock = 0;
			Object nextBlockVal = null;
			if (lastBlock > 0) {
				seek(blocks[0]);
				readRecord(vals);
				rec.values = vals;
				nextBlockVal = rec.calc(exp, ctx);
			}
			
			int i = 1;
			while (i <= count && nextBlock < lastBlock) {
				Object val = values.getMem(i);
				int cmp = Variant.compare(val, nextBlockVal);
				if (cmp <= 0) {
					if (position() > prevEnd) {
						close();
						reopen(1024);
						seek(prevEnd);
					}
					
					while (true) {
						readRecord(vals);
						rec.values = vals;
						Object reCal = rec.calc(exp, ctx);
						cmp = Variant.compare(val, reCal);
						if (cmp > 0) {
							prevEnd = position();
							if (prevEnd == blocks[nextBlock]) {
								nextBlock++;
								if (nextBlock < lastBlock) {
									seek(blocks[nextBlock]);
									readRecord(vals);
									rec.values = vals;
									nextBlockVal = rec.calc(exp, ctx);
								}
								
								break;
							}
						} else if (cmp == 0) {
							posArray.add(prevEnd);
							prevEnd = position();
							if (prevEnd == blocks[nextBlock]) {
								nextBlock++;
								if (nextBlock < lastBlock) {
									seek(blocks[nextBlock]);
									readRecord(vals);
									rec.values = vals;
									nextBlockVal = rec.calc(exp, ctx);
								}
								
								break;
							}

							continue;
						} else {
							i++;
							while (i <= count) {
								val = values.getMem(i);
								cmp = Variant.compare(val, rec.calc(exp, ctx));
								if (cmp > 0) {
									break;
								} else if (cmp == 0) {
									posArray.add(prevEnd);
									break;
								} else {
									i++;
								}
							}
							
							prevEnd = position();
							if (prevEnd == blocks[nextBlock]) {
								nextBlock++;
								if (nextBlock < lastBlock) {
									seek(blocks[nextBlock]);
									readRecord(vals);
									rec.values = vals;
									nextBlockVal = rec.calc(exp, ctx);
								}
								
								break;
							}
							
							break;
						}
					}
				} else {
					prevEnd = blocks[nextBlock];					
					nextBlock++;
					if (nextBlock < lastBlock) {
						seek(blocks[nextBlock]);
						readRecord(vals);
						rec.values = vals;
						nextBlockVal = rec.calc(exp, ctx);
					}
				}
			}
			
			if (i <= count) {
				if (position() > prevEnd) {
					close();
					reopen(1024);
					seek(prevEnd);
				}
				
				Object val = values.getMem(i);
				while (i <= count && readRecord(vals)) {
					rec.values = vals;
					int cmp = Variant.compare(val, rec.calc(exp, ctx));
					if (cmp > 0) {
						prevEnd = position();
					} else if (cmp == 0) {
						posArray.add(prevEnd);
						prevEnd = position();
					} else {
						i++;
						while (i <= count) {
							val = values.getMem(i);
							cmp = Variant.compare(val, rec.calc(exp, ctx));
							if (cmp > 0) {
								break;
							} else if (cmp == 0) {
								posArray.add(prevEnd);
								break;
							} else {
								i++;
							}
						}
						
						prevEnd = position();
					}
				}
			}
			
			if (posArray.size() == 0) {
				//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
				return new MemoryCursor(null);
			}
			
			return new PFileCursor(file, posArray.toArray(), 1024, fields, opt, ctx);
		} catch (IOException e) {
			throw new RQException(e);
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * ���ʽ��ֵ����startVal��endVal֮��ļ�¼
	 * @param	exp			���ʽ����eΪnull, ����ҲΪnull
	 * @param	startVal	��ʼֵ
	 * @param	endVal		����ֵ
	 * @param	fields      �����±���ֶΣ����Բ������ο��ֶ�
	 * @param	opt			e���ֶ���Դ����в�����ʱ������null��ȱʡ������
	 * @param	ctx			�����ı���
	 * 
	 * @return	���ض�Ӧ���α�
	*/
	public ICursor iselect(Expression exp, Object startVal,
			Object endVal, String []fields, String opt, Context ctx) {
		if (exp == null) {
			//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
			return new MemoryCursor(null);
		}
		
		try {
			open(1024);

			if (blocks == null) {
				// ���ֶμ��ļ�����˳�����
				Sequence result = readAll();
				Sequence values = result.calc(exp, ctx);
				int len = values.length();
				int start = -1;
				for (int i = 1; i <= len; ++i) {
					if (Variant.compare(values.getMem(i), startVal) >= 0) {
						start = i;
						break;
					}
				}
				
				if (start == -1) {
					return new MemoryCursor(null);
				}
				
				int end = -1;
				for (int i = len; i >= 1; --i) {
					if (Variant.compare(values.getMem(i), endVal) <= 0) {
						end = i;
						break;
					}
				}
				
				if (end < start) {
					return new MemoryCursor(null);
				} else if (fields == null) {
					return new MemoryCursor(result, start, end + 1);
				} else {
					result = result.get(start, end + 1);
					result = result.fieldsValues(fields, opt);
					return new MemoryCursor(result);
				}
			}
		} catch (IOException e) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("file.fileNoExist", file.getFileName()));
		}

		// ���ֶα��ʽ������������̣����Դ�����Ч��
		//  ������ʽ�Ƿ�ʽ���ַ���
		String[] fieldNames = exp.toFields();
		if (null != fieldNames) {
			boolean multi = true;
			// �ж��Ƿ�ʽ���ֶ�
			loop:for (int i = 0; i < fieldNames.length; i++) {
				for (int j = 0; j < ds.getFieldCount(); j ++) {
					if (fieldNames[i].equals(ds.getFieldName(j)))
						continue loop;
				}
				
				multi = false;
				break;
			}
			
			if (multi) {
				return iselectFields(fieldNames, startVal, endVal, fields, opt, ctx);
			}
		}
		
		// ����ͨ������
		
		return iselectExpression(exp, startVal, endVal, fields, opt, ctx);
	}
	
	/**
	 * ���ֶΡ����ֶ�ֵ����startVal��endVal֮��ļ�¼
	 * 
	 * @param	refFields	�ο��ֶ�
	 * @param	startVal	��ʼֵ
	 * @param	endVal		����ֵ
	 * @param	fields      �����±���ֶΣ����Բ������ο��ֶ�
	 * @param	opt			e���ֶ���Դ����в�����ʱ������null��ȱʡ������
	 * @param	ctx			�����ı���
	 * 
	 * @return	���ض�Ӧ���α�
	*/
	private ICursor iselectFields(String[] refFields, Object startVal, Object endVal, String []fields, String opt, Context ctx) {
		int startBlock;
		int endBlock ;
		long firstPos;
		Object[] vals = null; // ��ȡ�ļ�¼����,�����ǵ��ֶλ���ֶ�
		int fcount = ds.getFieldCount();
		int[] selFields = new int[fcount]; // Ҫ��ȡ���ֶ�
		
		// ���Ҷ�Ӧ�е�����
		for (int i = 0; i < fcount; i++) {
			selFields[i] = -1;
		}

		int fcou = 0;
		for (int i = 0; i < refFields.length; i++) {
			int index = ds.getFieldIndex(refFields[i]);
			if (0 > index ) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(refFields[i] + mm.getMessage("ds.fieldNotExist"));
			}
			selFields[index] = fcou;
			fcou++;
		}
				
		try {
			open(1024);
			firstPos = position();
			long []blocks = this.blocks;
			if (blocks == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			
			int lastBlock = this.lastBlock;
			startBlock = lastBlock;
			endBlock = lastBlock;
			vals = new Object[fcou];
			
			for (int i = 0; i < lastBlock; ++i) {
				seek(blocks[i]);
				readRecord(selFields, vals);
				if (compareFields(vals, startVal) >= 0) {
					startBlock = i;
					if (endVal != null && compareFields(vals, endVal) > 0) {
						endBlock = i;
					}
					
					break;
				}
			}
			
			if (endVal != null && endBlock != startBlock) {
				for (int i = startBlock + 1; i < lastBlock; ++i) {
					seek(blocks[i]);
					readRecord(selFields, vals);
					if (compareFields(vals, endVal) > 0) {
						endBlock = i;
						break;
					}
				}
			}
		} catch (IOException e) {
			throw new RQException(e);
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
		
		try {
			reopen(1024);
			seek(firstPos);
			
			long []blocks = this.blocks;
			long startPos = blocks[startBlock];
			
			if (startBlock > 0) {
				seek(blocks[startBlock - 1]);
			}
			
			long pos = firstPos;
			while (pos < startPos) {
				readRecord(selFields, vals);
				if (compareFields(vals, startVal) >= 0) {
					if (endVal != null && compareFields(vals, endVal) > 0) {
						//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
						return new MemoryCursor(null);
					}
					
					startPos = pos;
					break;
				}
				
				pos = position();
			}
			
			long endPos = blocks[endBlock];
			if (endVal != null) {
				if (endBlock > 0 && position() < blocks[endBlock - 1]) {
					seek(blocks[endBlock - 1]);
				}
				
				pos = position();
				while (pos < endPos) {
					readRecord(selFields, vals);
					if (compareFields(vals, endVal) > 0) {
						endPos = pos;
						break;
					}
					
					pos = position();
				}
			}
			
			if (startPos < endPos) {
				BFileCursor cursor = new BFileCursor(file, fields, opt, ctx);
				cursor.setPosRange(startPos, endPos);
				return cursor;
			} else {
				//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
				return new MemoryCursor(null);
			}
		} catch (IOException e) {
			throw new RQException(e);
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * ���ʽ����startVal��endVal֮��ļ�¼
	 * 
	 * @param	e			������ʽ
	 * @param	startVal	��ʼֵ
	 * @param	endVal		����ֵ
	 * @param	fields      �����±���ֶΣ����Բ������ο��ֶ�
	 * @param	opt			e���ֶ���Դ����в�����ʱ������null��ȱʡ������
	 * @param	ctx			�����ı���
	 * 
	 * @return	���ض�Ӧ���α�
	*/
	private ICursor iselectExpression(Expression exp, Object startVal, Object endVal, String []fields, String opt, Context ctx) {
		int startBlock;
		int endBlock ;
		long firstPos;
		Object[] vals = null; // ��ȡ�ļ�¼����,�����ǵ��ֶλ���ֶ�
		Record rec = new Record(ds);
		
		// ����ʼֵ���Ƚ�
		try {
			open(1024);
			firstPos = position();
			long []blocks = this.blocks;
			if (blocks == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			
			int lastBlock = this.lastBlock;
			startBlock = lastBlock;
			endBlock = lastBlock;
			vals = new Object[ds.getFieldCount()];
			
			for (int i = 0; i < lastBlock; ++i) {
				seek(blocks[i]);
				readRecord(vals);
				rec.values = vals;
				if (Variant.compare(rec.calc(exp, ctx), startVal) >= 0) {
					startBlock = i;
					if (endVal != null && Variant.compare(rec.calc(exp, ctx), endVal) > 0) {
						endBlock = i;
					}
					
					break;
				}
			}
			
			if (endVal != null && endBlock != startBlock) {
				for (int i = startBlock + 1; i < lastBlock; ++i) {
					seek(blocks[i]);
					readRecord(vals);
					rec.values = vals;
					if (Variant.compare(rec.calc(exp, ctx), endVal) > 0) {
						endBlock = i;
						break;
					}
				}
			}
		} catch (IOException e) {
			throw new RQException(e);
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
		
		// �����ֵ���Ƚ�
		try {
			reopen(1024);
			seek(firstPos);
			
			long []blocks = this.blocks;
			long startPos = blocks[startBlock];
			
			if (startBlock > 0) {
				seek(blocks[startBlock - 1]);
			}
			
			long pos = firstPos;
			while (pos < startPos) {
				readRecord(vals);
				rec.values = vals;
				if (Variant.compare(rec.calc(exp, ctx), startVal) >= 0) {
					if (endVal != null && Variant.compare(rec.calc(exp, ctx), endVal) > 0) {
						//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
						return new MemoryCursor(null);
					}
					
					startPos = pos;
					break;
				}
				
				pos = position();
			}
			
			long endPos = blocks[endBlock];
			if (endVal != null) {
				if (endBlock > 0 && position() < blocks[endBlock - 1]) {
					seek(blocks[endBlock - 1]);
				}
				
				pos = position();
				while (pos < endPos) {
					readRecord(vals);
					rec.values = vals;
					if (Variant.compare(rec.calc(exp, ctx), endVal) > 0) {
						endPos = pos;
						break;
					}
					
					pos = position();
				}
			}
			
			if (startPos < endPos) {
				BFileCursor cursor = new BFileCursor(file, fields, opt, ctx);
				cursor.setPosRange(startPos, endPos);
				return cursor;
			} else {
				//return null; �ĳɷ��ؿ��α꣬����cs.groups@t�᷵�ؿ����
				return new MemoryCursor(null);
			}
		} catch (IOException e) {
			throw new RQException(e);
		} finally {
			try {
				close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * ����n���طֶε�ֵ��ÿ������
	 * @param list ���ص�ÿ������
	 * @param values ���طֶε�ֵ
	 * @param n ������ÿ������
	 * @throws IOException 
	 */
	public void getSegmentInfo(ArrayList<Integer> list, Sequence values, int n) throws IOException {
		open();
		long []blocks = this.blocks;
		if (blocks == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("license.fileFormatError"));
		}
		
		int blockCount = lastBlock + 1;
		long blockRecordCount = this.blockRecordCount;
		int sum = (int) blockRecordCount;
		int colCount = readFields.length;
		
		for (int i = 1; i < blockCount; ++i) {
			if (sum + blockRecordCount > n) {
				list.add(sum);
				sum = (int) blockRecordCount;
				seek(blocks[i - 1]);
				Object []vals = new Object[colCount];
				readRecord(readIndex, vals);
				values.add(vals);
			} else {
				sum += blockRecordCount;
			}
		}
		
		list.add(sum);//���һ���ֶ��������п���ֻ����һ��
		close();
	}
	
	public int[] getReadIndex() {
		return readIndex;
	}
	
	public ObjectReader getImporter() {
		return importer;
	}
	
	public long getFirstRecordPos() {
		return firstRecordPos;
	}
	
	public long[] getBlocks() {
		return blocks;
	}

	public int getLastBlock() {
		return lastBlock;
	}
	
	public long getTotalRecordCount() {
		return totalRecordCount;
	}
	
	public DataStruct getDataStruct() {
		return ds;
	}
}