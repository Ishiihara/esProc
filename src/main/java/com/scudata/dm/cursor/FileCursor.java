package com.scudata.dm.cursor;

import java.io.IOException;
import java.io.InputStream;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.KeyWord;
import com.scudata.dm.LineImporter;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �ļ��α꣬���ڶ�ȡ�ı��ļ�
 * @author WangXiaoJun
 *
 */
public class FileCursor extends ICursor {
	private FileObject fileObject; // �ļ�����
	private LineImporter importer; // �ı������࣬���ڰ��ı����ж����ֶ�����
	private DataStruct ds; // �ļ���Ӧ�����ݽṹ
	
	private long start; // ��ȡ����ʼλ�ã�Ҫ����ͷȥβ�������ڲ��ж��ļ�
	private long end = -1; // ��ȡ�Ľ���λ�ã�Ҫ����ͷȥβ�������ڲ��ж��ļ�

	private String []selFields; // ѡ���ֶ�������
	private byte []types; // �ֶ�����
	private String []fmts; // �ֶ�ֵ��ʽ����������ʱ��
	private int []selIndex; // ѡ���ֶ���Դ�ṹ�е����
	private DataStruct selDs; // ��������ݽṹ
	private String opt; // ѡ��
	
	private byte [] colSeparator; // �зָ��
	private boolean isTitle; // �ļ��Ƿ��б��⣬����н���Ϊ�ṹ��
	private boolean isDeleteFile; // ������Ƿ�ɾ���ļ�
	private boolean isSingleField; // �Ƿ񷵻ص�����ɵ�����
	private boolean isSequenceMember; // �Ƿ񷵻�������ɵ�����
	private int sigleFieldIndex; // ����ʱ���ֶ�����
	private boolean isExist = true; // �ֶ��Ƿ����ļ���
	private boolean isEnd = false;
	
	private boolean optimize = true; // ide��Ҫ�ã�parseʱ�Ƿ����ж��ܲ���ת����һ����¼������
	
	/**
	 * ����һ���ı��ļ����α�
	 * @param fileObject �ı��ļ�
	 * @param segSeq �κţ���1��ʼ����
	 * @param segCount �ֶ���
	 * @param s �зָ���
	 * @param opt ѡ��  t����һ��Ϊ���⣬b���������ļ���c��д�ɶ��ŷָ���csv�ļ�
	 * 	s��������ֶΣ����ɵ��ֶδ����ɵ����i�������ֻ��1��ʱ���س�����
	 * 	q������ֶδ������������Ȱ��룬�������ⲿ�֣�k���������������˵Ŀհ׷���ȱʡ���Զ���trim
	 * 	e��Fi���ļ��в�����ʱ������null��ȱʡ������
	 * @param ctx
	 */
	public FileCursor(FileObject fileObject, int segSeq, int segCount, 
			String s, String opt, Context ctx) {
		this(fileObject, segSeq, segCount, null, null, s, opt, ctx);
	}

	/**
	 * ����һ���ı��ļ����α�
	 * @param fileObject �ı��ļ�
	 * @param segSeq �κţ���1��ʼ����
	 * @param segCount �ֶ���
	 * @param fields ѡ���ֶ�������
	 * @param types ѡ���ֶ��������飨�ɿգ�������com.scudata.common.Types
	 * @param s �зָ���
	 * @param opt ѡ��  t����һ��Ϊ���⣬b���������ļ���c��д�ɶ��ŷָ���csv�ļ�
	 * 	s��������ֶΣ����ɵ��ֶδ����ɵ����i�������ֻ��1��ʱ���س�����
	 * 	q������ֶδ������������Ȱ��룬�������ⲿ�֣�k���������������˵Ŀհ׷���ȱʡ���Զ���trim
	 * 	e��Fi���ļ��в�����ʱ������null��ȱʡ������
	 * @param ctx
	 */
	public FileCursor(FileObject fileObject, int segSeq, int segCount, 
			String []fields, byte []types, String s, String opt, Context ctx) {
		if (segCount > 1) {
			if (segSeq < 1 || segSeq > segCount) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(segSeq + mm.getMessage("function.invalidParam"));
			}

			long size = fileObject.size();
			long blockSize = size / segCount;
			if (segSeq == segCount) {
				end = size;
				start = blockSize * (segSeq - 1);
			} else {
				end = blockSize * segSeq;
				start = blockSize * (segSeq - 1);
			}
		}

		this.fileObject = fileObject;
		this.types = types;
		this.opt = opt;
		this.ctx = ctx;
		
		if (fields != null) {
			selFields = new String[fields.length];
			System.arraycopy(fields, 0, selFields, 0, fields.length);
		}
		
		boolean isCsv = false;
		if (opt != null) {
			if (opt.indexOf('t') != -1) isTitle = true;
			if (opt.indexOf('c') != -1) isCsv = true;
			if (opt.indexOf('i') != -1) isSingleField = true;
			if (opt.indexOf('e') != -1) isExist = false;
						
			if (opt.indexOf('x') != -1) {
				isDeleteFile = true;
				if (ctx != null) ctx.addResource(this);
			}
			
			if (opt.indexOf('w') != -1) isSequenceMember = true;
		}

		if (s != null && s.length() > 0) {
			String charset = fileObject.getCharset();
			try {
				colSeparator = s.getBytes(charset);
			} catch (Exception e) {
				throw new RQException(e.getMessage(), e);
			}
		} else if (isCsv) {
			colSeparator = new byte[]{(byte)','};
		} else {
			colSeparator = FileObject.COL_SEPARATOR;
		}
	}

	/**
	 * ���ö��ļ�����ʼλ��
	 * @param startPos ��ʼλ�ã�������ͷȥβ����
	 */
	public void setStart(long start) {
		this.start = start;
	}
	
	/**
	 * ���ö��ļ��Ľ���λ��
	 * @param endPos ����λ�ã�������ͷȥβ����
	 */
	public void setEnd(long end) {
		this.end = end;
	}
	
	/**
	 * ��������ʱ���ֶεĸ�ʽ
	 * @param fmts ����ʱ���ʽ����
	 */
	public void setFormats(String []fmts) {
		this.fmts = fmts;
	}
	
	/**
	 * ȡ�ļ��α��Ӧ���ļ�����
	 * @return FileObject
	 */
	public FileObject getFileObject() {
		return fileObject;
	}

	/**
	 * ȡ�α��ѡ��
	 * @return String
	 */
	public String getOption() {
		return opt;
	}

	private LineImporter open() {
		if (importer != null) {
			return importer;
		} else if (fileObject == null || isEnd) {
			return null;
		}

		if (!isDeleteFile && ctx != null) {
			ctx.addResource(this);
		}
		
		InputStream in = null;
		String []selFields = null;
		if (this.selFields != null) {
			// �����ֶ�������ֹû���ֶ���ʱ��#1ȡ��������reset����ȡ�������Ҳ����ֶ�
			selFields = new String[this.selFields.length];
			System.arraycopy(this.selFields, 0, selFields, 0, selFields.length);
		}
		
		try {
			in = fileObject.getBlockInputStream();
			String charset = fileObject.getCharset();
			importer = new LineImporter(in, charset, colSeparator, opt);
			
			if (isTitle) {
				// ��һ���Ǳ���
				Object []line = importer.readFirstLine();
				if (line == null) {
					return null;
				}

				int fcount = line.length;
				String []fieldNames = new String[fcount];
				for (int f = 0; f < fcount; ++f) {
					fieldNames[f] = Variant.toString(line[f]);
				}

				ds = new DataStruct(fieldNames);
				if (selFields != null) {
					if (isSingleField) isSingleField = selFields.length == 1;
					
					int maxSeq = 0;
					int []index = new int[fcount];
					for (int i = 0; i < fcount; ++i) {
						index[i] = -1;
					}

					for (int i = 0, count = selFields.length; i < count; ++i) {
						int q = ds.getFieldIndex(selFields[i]);
						if (q >= 0) {
							if (index[q] != -1) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(selFields[i] + mm.getMessage("ds.colNameRepeat"));
							}
			
							index[q] = i;
							selFields[i] = ds.getFieldName(q);
							sigleFieldIndex = q;
							
							if (q > maxSeq) {
								maxSeq = q;
							}
						} else if (isExist) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(selFields[i] + mm.getMessage("ds.fieldNotExist"));
						}
					}

					this.selDs = new DataStruct(selFields);
					setDataStruct(selDs);
					
					maxSeq++;
					if (maxSeq < fcount) {
						int []tmp = new int[maxSeq];
						System.arraycopy(index, 0, tmp, 0, maxSeq);
						index = tmp;
					}
					
					this.selIndex = index;
					importer.setColSelectIndex(index);
					
					if (optimize) {
						byte []colTypes = new byte[maxSeq];
						String []colFormats = new String[maxSeq];
						if (types != null) {
							for (int i = 0; i < maxSeq; ++i) {
								if (index[i] != -1) {
									colTypes[i] = types[index[i]];
								}
							}
						}
						
						if (fmts != null) {
							for (int i = 0; i < maxSeq; ++i) {
								if (index[i] != -1) {
									colFormats[i] = fmts[index[i]];
								}
							}
						}
						
						importer.setColTypes(colTypes, colFormats);
					}
				} else {
					setDataStruct(ds);
					if (isSingleField && fcount != 1) {
						isSingleField = false;
					}
					
					if (optimize) {
						byte []colTypes = new byte[fcount];
						importer.setColTypes(colTypes, fmts);
					}
				}
			} else {
				if (selFields != null) {
					if (isSingleField) {
						isSingleField = selFields.length == 1;
					}
					
					int fcount = 0;
					for (int i = 0, count = selFields.length; i < count; ++i) {
						if (KeyWord.isFieldId(selFields[i])) {
							int f =  KeyWord.getFiledId(selFields[i]);
							if (f > fcount) {
								fcount = f;
							}
						} else if (isExist) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(selFields[i] + mm.getMessage("ds.fieldNotExist"));
						}
					}
	
					int []index = new int[fcount];
					for (int i = 0; i < fcount; ++i) {
						index[i] = -1;
					}

					String[] fieldNames = new String[fcount];
					ds = new DataStruct(fieldNames);
					for (int i = 0, count = selFields.length; i < count; ++i) {
						int q = ds.getFieldIndex(selFields[i]);
						if (q >= 0) {
							if (index[q] != -1) {
								MessageManager mm = EngineMessage.get();
								throw new RQException(selFields[i] + mm.getMessage("ds.colNameRepeat"));
							}
			
							index[q] = i;
							selFields[i] = ds.getFieldName(q);
							sigleFieldIndex = q;
						}
					}

					this.selDs = new DataStruct(selFields);
					this.selIndex = index;
					importer.setColSelectIndex(index);
					
					if (optimize) {
						byte []colTypes = new byte[fcount];
						String []colFormats = new String[fcount];
						if (types != null) {
							for (int i = 0; i < fcount; ++i) {
								if (index[i] != -1) {
									colTypes[i] = types[index[i]];
								}
							}
						}
						
						if (fmts != null) {
							for (int i = 0; i < fcount; ++i) {
								if (index[i] != -1) {
									colFormats[i] = fmts[index[i]];
								}
							}
						}
						
						importer.setColTypes(colTypes, colFormats);
					}
				}
			}

			importer.seek(start);
			if (end != -1 && importer.getCurrentPosition() > end) {
				return null;
			}

			return importer;
		} catch (Exception e) {
			// importer���������п��ܳ��쳣
			if (in != null && importer == null) {
				try {
					in.close();
				} catch (IOException ie) {
				}
			}
			
			close();
			
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e.getMessage(), e);
			}
		}
	}

	// ȡ�����ֶ�
	private Sequence fetchAll(LineImporter importer, int n) throws IOException {
		Object []line;
		long end = this.end;
		int initSize = n > INITSIZE ? INITSIZE : n;
		if (isSequenceMember) {
			Sequence seq = new Sequence(initSize);
			for (int i = 0; i < n; ++i) {
				if (end != -1 && importer.getCurrentPosition() > end) {
					break;
				}

				line = importer.readLine();
				if (line == null) {
					break;
				} else {
					seq.add(new Sequence(line));
				}
			}

			if (seq.length() != 0) {
				return seq;
			} else {
				return null;
			}
		}
		
		if (end > 0 && importer.getCurrentPosition() > end) {
			return null;
		}
		
		int fcount;
		if (ds == null) {
			// �״ζ���û�б���
			line = importer.readFirstLine();
			if (line == null) {
				return null;
			}

			fcount = line.length;
			String []fieldNames = new String[fcount];
			ds = new DataStruct(fieldNames);
			
			if (isSingleField && fcount != 1) {
				isSingleField = false;
			}
			
			if (optimize) {
				byte []colTypes = new byte[fcount];
				for (int i = 0; i < fcount; ++i) {
					colTypes[i] = Variant.getObjectType(line[i]);
				}
	
				importer.setColTypes(colTypes, fmts);
			}
		} else {
			fcount = ds.getFieldCount();
			line = importer.readLine();
			if (line == null) {
				return null;
			}
		}
		
		if (isSingleField) {
			Sequence seq = new Sequence(initSize);
			seq.add(line[0]);

			for (int i = 1; i < n; ++i) {
				if (end != -1 && importer.getCurrentPosition() > end) {
					break;
				}

				line = importer.readLine();
				if (line == null) {
					break;
				}

				seq.add(line[0]);
			}

			return seq;
		} else {
			Table table = new Table(ds, initSize);
			BaseRecord r = table.newLast();
			int curLen = line.length;
			if (curLen > fcount) curLen = fcount;
			for (int f = 0; f < curLen; ++f) {
				r.setNormalFieldValue(f, line[f]);
			}

			for (int i = 1; i < n; ++i) {
				if (end != -1 && importer.getCurrentPosition() > end) {
					break;
				}

				line = importer.readLine();
				if (line == null) {
					break;
				}

				r = table.newLast();
				curLen = line.length;
				if (curLen > fcount) curLen = fcount;
				for (int f = 0; f < curLen; ++f) {
					r.setNormalFieldValue(f, line[f]);
				}
			}

			return table;
		}
	}

	// ��ѡ���ֶ�ʱ��ȡ��
	private Sequence fetchFields(LineImporter importer, int n) throws IOException {
		Object []line;
		long end = this.end;
		int initSize = n > INITSIZE ? INITSIZE : n;
		int []selIndex = this.selIndex;
		
		if (isSingleField) {
			int index = this.sigleFieldIndex;
			Sequence seq = new Sequence(initSize);
			for (int i = 0; i < n; ++i) {
				if (end != -1 && importer.getCurrentPosition() > end) {
					break;
				}

				line = importer.readLine();
				if (line == null) {
					break;
				}

				if (index < line.length) {
					seq.add(line[index]);
				} else {
					seq.add(null);
				}
			}

			if (seq.length() != 0) {
				return seq;
			} else {
				return null;
			}
		} else {
			int curLen;
			BaseRecord r;
			Table table = new Table(selDs, initSize);
			
			for (int i = 0; i < n; ++i) {
				if (end != -1 && importer.getCurrentPosition() > end) {
					break;
				}

				line = importer.readLine();
				if (line == null) {
					break;
				}

				r = table.newLast();
				curLen = line.length;
				for (int f = 0; f < curLen; ++f) {
					if (selIndex[f] != -1) r.setNormalFieldValue(selIndex[f], line[f]);
				}
			}

			if (table.length() != 0) {
				return table;
			} else {
				return null;
			}
		}
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (n < 1) return null;
		LineImporter importer = open();
		if (importer == null) return null;

		try {
			if (selFields == null) {
				return fetchAll(importer, n);
			} else {
				return fetchFields(importer, n);
			}
		} catch (IOException e) {
			close();
			throw new RQException(e.getMessage(), e);
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (n < 1) return 0;

		LineImporter importer = open();
		if (importer == null) return 0;

		try {
			long end = this.end;
			for (long i = 0; i < n; ++i) {
				if (end != -1 && importer.getCurrentPosition() > end) {
					break;
				}

				if (!importer.skipLine()) {
					return i;
				}
			}
		} catch (IOException e) {
			close();
			throw new RQException(e.getMessage(), e);
		}

		return n;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (fileObject != null) {
			isEnd = true;
			if (importer != null) {
				if (ctx != null) ctx.removeResource(this);
				try {
					importer.close();
				} catch (IOException e) {
				}
			}

			if (isDeleteFile) {
				fileObject.delete();
				fileObject = null;
			}

			importer = null;
			ds = null;
			selDs = null;
		}
	}

	protected void finalize() throws Throwable {
		close();
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		if (fileObject != null) {
			isEnd = false;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * �Ƿ���ֶ�parse���Ż����Ż�ʱ���Ȱ���һ����¼���ֶ�����ת
	 * @param optimize true���Ż���false�����Ż�
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	public LineImporter getImporter() {
		return importer;
	}
}
