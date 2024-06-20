package com.scudata.dm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.scudata.app.common.AppConsts;
import com.scudata.app.common.AppUtil;
import com.scudata.cellset.ICellSet;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.common.Escape;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.FileCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.query.SimpleSQL;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CellSetUtil;
import com.scudata.util.Variant;

/**
 * �ļ�����
 * file(...)�����ķ���ֵ
 * @author WangXiaoJun
 *
 */
public class FileObject implements Externalizable, IQueryable {
	public static final String TEMPFILE_PREFIX = "tmpdata"; // ���ȱ������3
	
	public static final int FILETYPE_TEXT = 0; // �ı�
	public static final int FILETYPE_BINARY = 1; // ���ļ�
	public static final int FILETYPE_GROUPTABLE = 2; // ���
	public static final int FILETYPE_GROUPTABLE_ROW = 3; // ��ʽ���
	public static final int LOCK_SLEEP_TIME = 100; // ������ʱ��
	
	// {(byte)'\r', (byte)'\n'}; // �н�����־
	// \r\n �� \n д��ʱ����ݲ���ϵͳ����������ʱ����Ҫ���ֶ�����
	public static final byte[] LINE_SEPARATOR = System.getProperty("line.separator").getBytes();
	public static final byte[] DM_LINE_SEPARATOR = new byte[] {'\r', '\n'};
	public static final byte[] COL_SEPARATOR = new byte[] {(byte)'\t'}; // �м��
	private static final int BLOCKCOUNT = 999; // �������ļ����С
	public static final String S_FIELDNAME = "_1"; // ��������ʱĬ�ϵ��ֶ���

	private String fileName; // �ļ�·����
	private String charset; // �ı��ļ����ַ���
	private String opt; // ѡ��

	private Integer partition; // ����
	
	// �ļ����ڻ�����ip�Ͷ˿�
	private String ip;
	private int port;
	
	private boolean isSimpleSQL; // �Ƿ����ڼ�SQL
	
	transient private IFile file; // ��Ӧ��ʵ���ļ��������Ǳ����ļ���Զ���ļ���HDFS�ļ�
	transient private Context ctx; // ����������
	
	transient private boolean isRemoteFileWritable = false; // Զ���ļ��Ƿ��д
	
	// �������л�
	public FileObject() {
	}

	/**
	 * ����һ���ļ����󹹽��ļ�����
	 * @param fo
	 */
	public FileObject(FileObject fo) {
		this.fileName = fo.fileName;
		this.charset = fo.charset;
		this.opt = fo.opt;
		this.partition = fo.partition;
		this.ip = fo.ip;
		this.port = fo.port;
		this.file = fo.file;
		this.ctx = fo.ctx;
	}

	/**
	 * �����ļ�������һ�ļ�����
	 * @param name �ļ�·����
	 */
	public FileObject(String name) {
		this(name, null);
	}
	
	/**
	 * �����ļ�������һ�ļ�����
	 * @param name �ļ�·����
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public FileObject(String name, String opt, Context ctx) {
		this(name, null, opt, ctx);
	}

	/**
	 * ����Զ���ļ�����
	 * @param name �ļ�·����
	 * @param ip
	 * @param port
	 */
	public FileObject(String name, String ip, int port) {
		this(name, null);
		this.ip = ip;
		this.port = port;
	}

	/**
	 * �������ֹ���һ�ļ�����
	 * @param name String
	 * @param opt String s������·��������·������ֻ����p����path�б����Ҳ���ֻ��
	 */
	public FileObject(String name, String opt) {
		this(name, null, opt, null);
	}

	/**
	 * �����ļ�����һ�ļ�����
	 * @param file IFile
	 * @param name �ļ���
	 * @param cs �ı��ļ��ַ���
	 * @param opt ѡ��
	 */
	public FileObject(IFile file, String name, String cs, String opt) {
		this(name, cs, opt, null);
		this.file = file;
	}

	/**
	 * �������ֹ���һ�ļ�����
	 * @param name String
	 * @param cs String �ַ���
	 * @param opt String s������·��������·������ֻ����p����path�б����Ҳ���ֻ��
	 * @param ctx Context ����Ҫʱ��null
	 */
	public FileObject(String name, String cs, String opt, Context ctx) {
		this.ctx = ctx;
		setOption(opt);
		setCharset(cs);

		// "remote://ip:port/��" Զ���ļ�
		final String remotePrefix = "remote://";
		if (name.startsWith(remotePrefix)) {
			int start = remotePrefix.length();
			int index = name.indexOf(':', start);
			if (index == -1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("file.fileNotExist", name));
			}

			this.ip = name.substring(start, index).trim();
			index++;
			int index2 = name.indexOf('/', index);
			if (index2 == -1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("file.fileNotExist", name));
			}

			String port = name.substring(index, index2).trim();
			this.port = Integer.parseInt(port);
			this.fileName = name.substring(index2 + 1);
		} else {
			this.fileName = name;
			//this.ip = Env.getLocalHost();
			//this.port = Env.getLocalPort();
		}
	}

	/**
	 * �����Ƿ��Ǽ�SQL
	 * @param b true����
	 */
	public void setIsSimpleSQL(boolean b) {
		isSimpleSQL = b;
	}
	
	/**
	 * ȡ�Ƿ��Ǽ�SQL
	 * @return true����
	 */
	public boolean getIsSimpleSQL() {
		return isSimpleSQL;
	}
	
	/**
	 * ����Զ���ļ���д
	 */
	public void setRemoteFileWritable(){
		isRemoteFileWritable = true;
	}
	
	/**
	 * ���ü���������
	 * @param ctx
	 */
	public void setContext(Context ctx) {
		this.ctx = ctx;
	}
	
	/**
	 * ȡ�����ļ�ʱָ����ѡ��
	 * @return
	 */
	public String getOption() {
		return opt;
	}

	/**
	 * ����ѡ��
	 * @param opt
	 */
	public void setOption(String opt) {
		this.opt = opt;
	}
	
	/**
	 * �����ı��ļ��ַ���
	 * @param cs �ַ���
	 */
	public void setCharset(String cs) {
		if (cs == null || cs.length() == 0) {
			charset = Env.getDefaultCharsetName();
		} else {
			charset = cs;
		}
	}
	
	/**
	 * ���÷���
	 * @param p
	 */
	public void setPartition(Integer p) {
		this.partition = p;
		this.file = null;
	}

	/**
	 * ȡ����
	 * @return
	 */
	public Integer getPartition() {
		return partition;
	}

	/**
	 * ����Զ�̻���IP
	 * @param ip
	 */
	public void setIP(String ip) {
		this.ip = ip;
	}

	/**
	 * ȡԶ�̻���IP
	 * @return IP
	 */
	public String getIP() {
		return ip;
	}

	/**
	 * ����Զ�̻����˿�
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * ȡԶ�̻����˿�
	 * @return �˿�
	 */
	public int getPort() {
		return port;
	}

	/**
	 * ȡ�ļ���
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * �����ļ���
	 * @param name
	 */
	public void setFileName(String name) {
		this.fileName = name;
	}

	/**
	 * ȡ�ַ���
	 * @return
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * �����ļ���
	 * @return String
	 */
	public String toString() {
		return fileName;
	}

	/**
	 * ����һ���ļ����γ����з���
	 * @param opt String t����һ��Ϊ���⣬b���������ļ���c��д�ɶ��ŷָ���csv�ļ�
	 * 	s��������ֶΣ����ɵ��ֶδ����ɵ����i�������ֻ��1��ʱ���س�����
	 * 	q������ֶδ������������Ȱ��룬�������ⲿ�֣�k���������������˵Ŀհ׷���ȱʡ���Զ���trim
	 * @throws IOException
	 * @return Sequence
	 */
	public Sequence importSeries(String opt) throws IOException {
		ICursor cursor;
		if (opt != null && opt.indexOf('b') != -1) {
			cursor = new BFileCursor(this, null, opt, null);
		} else {
			cursor = new FileCursor(this, 0, -1, null, opt, null);
		}
		
		try {
			return cursor.fetch();
		} finally {
			cursor.close();
		}
	}

	/**
	 * ����һ���ļ����γ����з���
	 * @param segSeq int �κţ���1��ʼ����
	 * @param segCount int �ֶ���
	 * @param fields String[] ѡ���ֶ���
	 * @param types byte[] ѡ���ֶ����ͣ��ɿա�����com.scudata.common.Types
	 * @param s Object excel sheet������ţ����зָ���
	 * @param opt String t����һ��Ϊ���⣬b���������ļ���c��д�ɶ��ŷָ���csv�ļ�
	 * 	s��������ֶΣ����ɵ��ֶδ����ɵ����i�������ֻ��1��ʱ���س�����
	 * 	q������ֶδ������������Ȱ��룬�������ⲿ�֣�k���������������˵Ŀհ׷���ȱʡ���Զ���trim
	 * 	e��Fi���ļ��в�����ʱ������null��ȱʡ������
	 * @param ctx Context
	 * @throws IOException
	 * @return Sequence
	 */
	public Sequence importSeries(int segSeq, int segCount, String []fields, byte[] types,
							  Object s, String opt) throws IOException {
		ICursor cursor;
		if (opt != null && opt.indexOf('b') != -1) {
			cursor = new BFileCursor(this, fields, segSeq, segCount, opt, null);
		} else {
			String sep;
			if (s instanceof String) {
				sep = (String)s;
			} else if (s == null) {
				sep = null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("import" + mm.getMessage("function.paramTypeError"));
			}

			cursor = new FileCursor(this, segSeq, segCount, fields, types, sep, opt, null);
		}
		
		try {
			return cursor.fetch();
		} finally {
			cursor.close();
		}
	}

	/**
	 * �����ı���xls��xlsx�ļ�
	 * @param importer ������LineImporter��ExcelTool��ExcelXTool
	 * @param opt t����һ��Ϊ����
	 * @return ���
	 * @throws IOException
	 */
	public static Table import_x(ILineInput importer, String opt) throws IOException {
		Object []line = importer.readLine();
		if (line == null)return null;
		int fcount = line.length;
		if (fcount == 0)return null;

		Table table;
		if (opt != null && opt.indexOf('t') != -1) {
			String[] items = new String[fcount];
			for (int f = 0; f < fcount; ++f) {
				items[f] = Variant.toString(line[f]);
			}

			table = new Table(items);
		} else {
			String[] items = new String[fcount];
			table = new Table(items);

			BaseRecord r = table.newLast();
			for (int f = 0; f < fcount; ++f) {
				r.setNormalFieldValue(f, line[f]);
			}
		}

		while (true) {
			line = importer.readLine();
			if (line == null)break;

			int curLen = line.length;
			if (curLen > fcount) curLen = fcount;
			BaseRecord r = table.newLast();
			for (int f = 0; f < curLen; ++f) {
				r.setNormalFieldValue(f, line[f]);
			}
		}

		table.trimToSize();
		return table;
	}
	
	/**
	 * ȡ���е����г�Ա����󳤶�
	 * @param seq ������ɵ�����
	 * @return ��󳤶�
	 */
	public static int getMaxMemberCount(Sequence seq) {
		int len = seq.length();
		int maxCount = -1;
		for (int i = 1; i <= len; ++i) {
			Object obj = seq.getMem(i);
			if (obj instanceof Sequence) {
				int count = ((Sequence)obj).length();
				if (maxCount < count) {
					maxCount = count;
				}
			} else if (obj != null) {
				return -1;
			}
		}
		
		return maxCount;
	}

	/**
	 * ��������
	 * @param exporter ������LineExporter��ExcelTool��ExcelXTool
	 * @param series ����
	 * @param exps Ҫ�������ֶα��ʽ�����null�򵼳������ֶ�
	 * @param names ��������ֶ���
	 * @param bTitle �Ƿ񵼳��ֶ���
	 * @param ctx
	 * @throws IOException
	 */
	public static void export_x(ILineOutput exporter, Sequence series, Expression []exps,
							   String []names, boolean bTitle, Context ctx) throws IOException {
		if (exps == null) {
			int fcount = 1;
			DataStruct ds = series.dataStruct();
			if (ds == null) {
				int len = series.length();
				fcount = getMaxMemberCount(series);
				
				if (fcount < 1) {
					if (bTitle && len > 0) {
						exporter.writeLine(new String[]{S_FIELDNAME});
					}
					
					Object []lineObjs = new Object[1];
					for (int i = 1; i <= len; ++i) {
						lineObjs[0] = series.getMem(i);
						exporter.writeLine(lineObjs);
					}
				} else {
					// A�����е�����ʱ�������ޱ���/�ֶ����Ķ����ı�
					Object []lineObjs = new Object[fcount];
					for (int i = 1; i <= len; ++i) {
						Sequence seq = (Sequence)series.getMem(i);
						if (seq == null) {
							for (int f = 0; f < fcount; ++f) {
								lineObjs[f] = null;
							}
						} else {
							seq.toArray(lineObjs);
							for (int f = seq.length(); f < fcount; ++f) {
								lineObjs[f] = null;
							}
						}

						exporter.writeLine(lineObjs);
					}
				}
			} else {
				fcount = ds.getFieldCount();
				if (bTitle) exporter.writeLine(ds.getFieldNames());
	
				Object []lineObjs = new Object[fcount];
				for (int i = 1, len = series.length(); i <= len; ++i) {
					BaseRecord r = (BaseRecord)series.getMem(i);
					Object []vals = r.getFieldValues();
					for (int f = 0; f < fcount; ++f) {
						if (vals[f] instanceof BaseRecord) {
							lineObjs[f] = ((BaseRecord)vals[f]).value();
						} else {
							lineObjs[f] = vals[f];
						}
					}
	
					exporter.writeLine(lineObjs);
				}
			}
		} else {
			ComputeStack stack = ctx.getComputeStack();
			Current current = new Current(series);
			stack.push(current);

			try {
				int fcount = exps.length;
				if (bTitle) {
					if (names == null) names = new String[fcount];
					series.getNewFieldNames(exps, names, "export");
					exporter.writeLine(names);
				}

				Object []lineObjs = new Object[fcount];
				for (int i = 1, len = series.length(); i <= len; ++i) {
					current.setCurrent(i);
					for (int f = 0; f < fcount; ++f) {
						lineObjs[f] = exps[f].calculate(ctx);
						if (lineObjs[f] instanceof BaseRecord) {
							lineObjs[f] = ((BaseRecord)lineObjs[f]).value();
						}
					}

					exporter.writeLine(lineObjs);
				}
			} finally {
				stack.pop();
			}
		}
	}

	/**
	 * ��������
	 * @param exporter ������LineExporter��ExcelTool��ExcelXTool
	 * @param cursor �α�
	 * @param exps Ҫ�������ֶα��ʽ�����null�򵼳������ֶ�
	 * @param names ��������ֶ���
	 * @param bTitle �Ƿ񵼳��ֶ���
	 * @param ctx
	 * @throws IOException
	 */
	public static void export_x(ILineOutput exporter, ICursor cursor, Expression []exps,
							   String []names, boolean bTitle, Context ctx) throws IOException {
		Sequence table = cursor.fetch(BLOCKCOUNT);
		if (table == null || table.length() == 0) return;

		if (exps == null) {
			int fcount = 1;
			DataStruct ds = table.dataStruct();
			if (ds == null) {
				if (bTitle) {
					exporter.writeLine(new String[]{S_FIELDNAME});
				}
			} else {
				fcount = ds.getFieldCount();
				if (bTitle) {
					exporter.writeLine(ds.getFieldNames());
				}
			}
			
			Object []lineObjs = new Object[fcount];
			while (true) {
				if (ds == null) {
					for (int i = 1, len = table.length(); i <= len; ++i) {
						lineObjs[0] = table.getMem(i);
						exporter.writeLine(lineObjs);
					}
				} else {
					for (int i = 1, len = table.length(); i <= len; ++i) {
						BaseRecord r = (BaseRecord)table.getMem(i);
						Object []vals = r.getFieldValues();
						for (int f = 0; f < fcount; ++f) {
							if (vals[f] instanceof BaseRecord) {
								lineObjs[f] = ((BaseRecord)vals[f]).value();
							} else {
								lineObjs[f] = vals[f];
							}
						}

						exporter.writeLine(lineObjs);
					}
				}

				table = cursor.fetch(BLOCKCOUNT);
				if (table == null || table.length() == 0) {
					break;
				}
			}
		} else {
			int fcount = exps.length;
			Object []lineObjs = new Object[fcount];
			if (bTitle) {
				if (names == null) names = new String[fcount];
				table.getNewFieldNames(exps, names, "export");
				exporter.writeLine(names);
			}

			ComputeStack stack = ctx.getComputeStack();
			while (true) {
				Current current = new Current(table);
				stack.push(current);

				try {
					for (int i = 1, len = table.length(); i <= len; ++i) {
						current.setCurrent(i);
						for (int f = 0; f < fcount; ++f) {
							lineObjs[f] = exps[f].calculate(ctx);
							if (lineObjs[f] instanceof BaseRecord) {
								lineObjs[f] = ((BaseRecord)lineObjs[f]).value();
							}
						}

						exporter.writeLine(lineObjs);
					}
				} finally {
					stack.pop();
				}

				table = cursor.fetch(BLOCKCOUNT);
				if (table == null || table.length() == 0) {
					break;
				}
			}
		}
	}

	/**
	 * �����������ֶε������ļ���
	 * @param series Sequence
	 * @param opt String �����������⣬c��д�ɶ��ŷָ���csv�ļ���b���������ļ���a��׷��д
	 * @param s Object excel sheet�����зָ���������Ĭ�ϵ�
	 */
	public void exportSeries(Sequence series, String opt, Object s) {
		exportSeries(series, null, null, opt, s, null);
	}

	/**
	 * �����е�ָ���ֶε������ļ���
	 * @param series Sequence
	 * @param exps Expression[] Ҫ������ֵ���ʽ���ձ�ʾ���������ֶ�
	 * @param names String[] ֵ���ʽ��Ӧ�����֣�ʡ����ֵ���ʽ��
	 * @param opt String t���������⣬c��д�ɶ��ŷָ���csv�ļ���b���������ļ���a��׷��д
	 * @param s Object excel sheet�����зָ���������Ĭ�ϵ�
	 * @param ctx Context
	 */
	public void exportSeries(Sequence series, Expression []exps,
							 String []names, String opt, Object s, Context ctx) {
		if (BFileWriter.isBtxOption(opt)) {
			if (opt.indexOf('w') != -1) {
				series = series.toTable();
			}
			
			BFileWriter writer = new BFileWriter(this, opt);
			writer.export(series, exps, names, ctx);
			return;
		}
		
		boolean isTitle = false, isCsv = false, isAppend = false, isQuote = false, isQuoteEscape = false;
		byte[] LINE_SEPARATOR = FileObject.LINE_SEPARATOR;

		if (opt != null) {
			if (opt.indexOf('t') != -1) isTitle = true;
			if (opt.indexOf('c') != -1) isCsv = true;
			if (opt.indexOf('a') != -1) isAppend = true;
			if (opt.indexOf('q') != -1) isQuote = true;
			if (opt.indexOf('o') != -1) {
				isQuote = true;
				isQuoteEscape = true;
			}

			if (opt.indexOf('w') != -1) {
				LINE_SEPARATOR = FileObject.DM_LINE_SEPARATOR;
			}
		}

		if (series == null) {
			if (!isAppend) delete();
			return;
		}

		OutputStream os;
		if (isAppend) {
			os = getBufferedOutputStream(true);
			if (size() > 0) {
				isTitle = false;
			} else {
				isAppend = false;
			}
		} else {
			isAppend = false;
			os = getBufferedOutputStream(false);
		}

		try {
			byte []colSeparator = COL_SEPARATOR;
			if (isCsv) colSeparator = new byte[]{(byte)','};

			if (s instanceof String) {
				String str = (String)s;
				// ����Ϊ0ʱ��ʾ��Ҫ�ָ��
				//if (str.length() > 0) {
					colSeparator = str.getBytes(charset);
				//}
			} else if (s != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("import" + mm.getMessage("function.paramTypeError"));
			}

			LineExporter exporter = new LineExporter(os, charset, colSeparator, LINE_SEPARATOR, isAppend);
			exporter.setQuote(isQuote);
			if (isQuoteEscape) {
				exporter.setEscapeChar('"');
			}
			
			export_x(exporter, series, exps, names, isTitle, ctx);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
	}
		
	/**
	 * ����cursor���ı�
	 * @param cursor ICursor
	 * @param exps Expression[] Ҫ������ֵ���ʽ���ձ�ʾ���������ֶ�
	 * @param names String[] ֵ���ʽ��Ӧ�����֣�ʡ����ֵ���ʽ��
	 * @param opt String �����������⣬c��д�ɶ��ŷָ���csv�ļ���b���������ļ���a��׷��д
	 * @param s Object �зָ���
	 * @param ctx Context
	 */
	public void exportCursor(ICursor cursor, Expression []exps,
							 String []names, String opt, Object s, Context ctx) {
		if (BFileWriter.isBtxOption(opt)) {
			BFileWriter writer = new BFileWriter(this, opt);
			writer.export(cursor, exps, names, ctx);
			return;
		}
		
		boolean isTitle = false, isCsv = false, isAppend = false, isQuote = false, isQuoteEscape = false;
		byte[] LINE_SEPARATOR = FileObject.LINE_SEPARATOR;
		if (opt != null) {
			if (opt.indexOf('t') != -1) isTitle = true;
			if (opt.indexOf('c') != -1) isCsv = true;
			if (opt.indexOf('a') != -1) isAppend = true;
			if (opt.indexOf('q') != -1) isQuote = true;
			if (opt.indexOf('o') != -1) {
				isQuote = true;
				isQuoteEscape = true;
			}
			
			if (opt.indexOf('w') != -1) {
				LINE_SEPARATOR = FileObject.DM_LINE_SEPARATOR;
			}
		}
		
		if (cursor == null) {
			if (!isAppend) delete();
			return;
		}

		OutputStream os;
		if (isAppend && size() > 0) {
			os = getBufferedOutputStream(true);
			if (size() > 0) {
				isTitle = false;
			} else {
				isAppend = false;
			}
		} else {
			isAppend = false;
			os = getBufferedOutputStream(false);
		}

		try {
			byte []colSeparator = COL_SEPARATOR;
			if (isCsv) colSeparator = new byte[]{(byte)','};

			if (s instanceof String) {
				String str = (String)s;
				// ����Ϊ0ʱ��ʾ��Ҫ�ָ��
				//if (str.length() > 0) {
					colSeparator = str.getBytes(charset);
				//}
			} else if (s != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("import" + mm.getMessage("function.paramTypeError"));
			}

			LineExporter exporter = new LineExporter(os, charset, colSeparator, LINE_SEPARATOR, isAppend);
			exporter.setQuote(isQuote);
			if (isQuoteEscape) {
				exporter.setEscapeChar('"');
			}
			
			export_x(exporter, cursor, exps, names, isTitle, ctx);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * ���鵼���α꣬���ɿɰ���ֶεĶ������ļ�
	 * @param cursor �α�
	 * @param exps �����ֶ�ֵ���ʽ
	 * @param names �����ֶ���
	 * @param gexp ������ʽ��ֻ�Ƚ�����
	 * @param opt
	 * @param ctx
	 */
	public void export_g(ICursor cursor, Expression []exps,
			 String []names, Expression gexp, String opt, Context ctx) {
		BFileWriter writer = new BFileWriter(this, opt);
		writer.export(cursor, exps, names, gexp, ctx);
	}
	
	/**
	 * ������������ʼλ�ö�������λ�ã����ᱣ���������ݣ����ڲ���Ӳ���ٶ�
	 * @param in ������
	 * @param start ��ʼλ��
	 * @param end ����λ��
	 * @return �����ֽ���
	 * @throws IOException
	 */
	private static Object read0(InputStream in, long start, long end) throws IOException {
		byte []buf = new byte[Env.FILE_BUFSIZE];
		if (start != 0) {
			long total = 0;
			long cur = 0;
			while ((total<start) && ((cur = in.skip(start-total)) > 0)) {
				total += cur;
			}

			if (total < start) return null;
		}

		long size = 0;
		int cur;
		if (end > 0) {
			while ((cur = in.read(buf)) != -1) {
				size += cur;
				if (size + start >= end) break;
			}
		} else {
			while ((cur = in.read(buf)) != -1) {
				size += cur;
			}
		}

		return new Long(size);
	}

	/**
	 * ��ȡ�������е������ֽ�
	 * @param in InputStream
	 * @param start long ��ʼλ�ã���0��ʼ����������
	 * @param end long ����λ�ã���0��ʼ������������-1��ʾ��β
	 * @throws IOException
	 * @return byte[]
	 */
	private static byte[] read(InputStream in, long start, long end) throws IOException {
		if (start != 0) {
			long total = 0;
			long cur = 0;
			while ((total<start) && ((cur = in.skip(start-total)) > 0)) {
				total += cur;
			}

			if (total < start) return null;
		}

		if (end > 0) {
			int total = 0;
			int count = (int)(end - start + 1);
			ByteArrayOutputStream out = new ByteArrayOutputStream(count);
			byte[] buf = new byte[4096];

			while(total < count) {
				int rest = count - total;
				int cur = in.read(buf, 0, rest < 4096 ? rest : 4096);
				if (cur == -1) break;

				out.write(buf, 0, cur);
				total += cur;
			}

			return out.toByteArray();
		} else {
			ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
			int count = 0;
			byte[] buf = new byte[4096];
			while( (count=in.read(buf)) != -1 ) {
				out.write(buf, 0, count);
			}

			return out.toByteArray();
		}
	}

	/**
	 * ���ļ�����ת���ַ�������
	 * @param start long ��ʼλ�ã���0��ʼ����������
	 * @param end long ����λ�ã���0��ʼ������������-1��ʾ��β
	 * @param opt String n�����سɴ����У�b������byte���飬v������ֵ
	 * @throws IOException
	 * @return Object
	 */
	public Object read(long start, long end, String opt) throws IOException {
		if (end > 0 && end < start) return null;

		boolean isMultiLine = false, isBinary = false, isValue = false, isTest = false;
		if (opt != null) {
			if (opt.indexOf('n') != -1) isMultiLine = true;
			if (opt.indexOf('b') != -1) isBinary = true;
			if (opt.indexOf('v') != -1) isValue = true;
			if (opt.indexOf('0') != -1) isTest = true;
		}

		InputStream in = getInputStream();
		try {
			if (isTest) {
				return read0(in, start, end);
			} else if (isBinary) {
				return read(in, start, end);
			} else if (isMultiLine) {
				InputStreamReader isr = new InputStreamReader(in, charset);
				BufferedReader br = new BufferedReader(isr);
				Sequence retSeries = new Sequence();
				for (; ;) {
					String str = br.readLine();
					if (str == null) break;
					if (isValue) {
						retSeries.add(Variant.parse(str, false));
					} else {
						retSeries.add(str);
					}
				}

				retSeries.trimToSize();
				return retSeries;
			} else {
				byte []bts = read(in, start, end);
				if (bts == null) {
					return null;
				}

				// ȥ��bomͷ
				String str;
				if (start == 0 && bts.length > 3 && bts[0] == (byte)0xEF && bts[1] == (byte)0xBB && bts[2] == (byte)0xBF) {
					charset = "UTF-8";
					str = new String(bts, 3, bts.length - 3, charset);
				} else {
					str = new String(bts, charset);
				}
				
				if (isValue) {
					return Variant.parse(str, false);
				} else {
					return str;
				}
			}
		} finally {
			in.close();
		}
	}

	/**
	 * �Ѷ���д�뵽�ļ���
	 * @param obj Objcet
	 * @param opt String a��׷��д��b��д��byte����
	 * @throws IOException
	 */
	public void write(Object obj, String opt) throws IOException {
		boolean isAppend = false, isBinary = false;
		byte[] LINE_SEPARATOR = FileObject.LINE_SEPARATOR;
		
		if (opt != null) {
			if (opt.indexOf('a') != -1) isAppend = true;
			if (opt.indexOf('b') != -1) isBinary = true;
			if (opt.indexOf('w') != -1) {
				LINE_SEPARATOR = FileObject.DM_LINE_SEPARATOR;
			}
		}

		OutputStream os = getBufferedOutputStream(isAppend);

		try {
			if (isBinary) {
				if (obj instanceof byte[]) {
					os.write((byte[])obj);
				} else {
					String str = Variant.toString(obj);
					if (str != null) os.write(str.getBytes(charset));
				}
			} else {
				if (isAppend && size() > 0) {
					os.write(LINE_SEPARATOR);
				}

				if (obj instanceof Sequence) {
					Sequence series = (Sequence)obj;
					int len = series.length();
					if (len > 0) {
						Object mem = series.getMem(1);
						String str = Variant.toString(mem);
						if (str != null) os.write(str.getBytes(charset));
					}

					for (int i = 2; i <= len; ++i) {
						os.write(LINE_SEPARATOR);
						Object mem = series.getMem(i);
						String str = Variant.toString(mem);
						if (str != null) os.write(str.getBytes(charset));
					}
				} else {
					String str = Variant.toString(obj);
					if (str != null) os.write(str.getBytes(charset));
				}
			}
		} finally {
			os.close();
		}
	}

	/**
	 * ȡ������
	 * @return InputStream
	 */
	public InputStream getInputStream() {
		return getFile().getInputStream();
	}

	/**
	 * ȡ��������������
	 * @return
	 */
	public BlockInputStream getBlockInputStream() {
		return getBlockInputStream(Env.FILE_BUFSIZE);
	}

	/**
	 * ȡ��������������
	 * @param bufSize ���С
	 * @return BlockInputStream
	 */
	public BlockInputStream getBlockInputStream(int bufSize) {
		InputStream is = getInputStream();
		return new BlockInputStream(is, bufSize);
	}

	/**
	 * ȡ�����
	 * @param isAppend �Ƿ�׷��д
	 * @return OutputStream
	 */
	public OutputStream getOutputStream(boolean isAppend) {
		OutputStream os = getFile().getOutputStream(isAppend);
		
		if (os instanceof FileOutputStream) {
			FileOutputStream fos = (FileOutputStream)os;
			FileLock lock = null;
			if (opt == null || opt.indexOf('a') == -1) {
				try {
					lock = fos.getChannel().tryLock();
				} catch (Exception e) {
				}
				
				if (lock == null) {
					throw new RQException("��һ�������������ļ���һ���֣������޷�����");
				}
			} else {
				FileChannel channel = fos.getChannel();
				while (true) {
					try {
						channel.lock();
						break;
					} catch (OverlappingFileLockException e) {
						try {
							Thread.sleep(LOCK_SLEEP_TIME);
						} catch (InterruptedException ie) {
						}
					} catch (Exception e) {
						throw new RQException(e.getMessage(), e);
					}
				}
			}
		}

		return os;
	}

	/**
	 * ȡ������������
	 * @param isAppend �Ƿ�׷��д
	 * @return OutputStream
	 */
	public OutputStream getBufferedOutputStream(boolean isAppend) {
		OutputStream os = getOutputStream(isAppend);
		return new BufferedOutputStream(os, Env.FILE_BUFSIZE);
	}
	
	/**
	 * ȡ�ɸ������λ�õ������
	 * @param isAppend �Ƿ�׷��д
	 * @return RandomOutputStream
	 */
	public RandomOutputStream getRandomOutputStream(boolean isAppend) {
		RandomOutputStream os = getFile().getRandomOutputStream(isAppend);
		
		boolean lock = false;
		try {
			if (opt == null || opt.indexOf('a') == -1) {
				lock = os.tryLock();
			} else {
				lock = os.lock();
			}
		} catch (Exception e) {
		}
		
		if (!lock) {
			try {
				os.close();
			} catch (IOException e) {
			}
			
			throw new RQException("��һ�������������ļ���һ���֣������޷�����");
		}

		return os;
	}

	/**
	 * ȡ�����ļ�
	 * @return LocalFile
	 */
	public LocalFile getLocalFile() {
		if (partition == null) {
			return new LocalFile(fileName, opt, ctx);
		} else {
			return new LocalFile(fileName, opt, partition);
		}
	}
	
	/**
	 * ȡ�ļ�
	 * @return IFile
	 */
	public IFile getFile() {
		if (file != null) {
			return file;
		}

		//if (ip == null || (ip.equals(Env.getLocalHost()) && port == Env.getLocalPort())) {
		if (ip == null) {
			if (partition == null) {
				file = new LocalFile(fileName, opt, ctx);
			} else {
				file = new LocalFile(fileName, opt, partition);
			}
		} else {
			String pathFile = fileName;
			RemoteFile rf = new RemoteFile(ip, port, pathFile, partition);
			rf.setOpt(opt);
			if( isRemoteFileWritable ){
				// �����Ƿ��д����
				rf.setWritable();
			}
			
			file = rf;
		}
		
		if (opt != null && opt.indexOf('i') != -1) {
			file = new MemoryFile(file);
		}
		
		return file;
	}

	/**
	 * �����Ƿ���Զ���ļ�
	 * @return true����Զ���ļ���false������Զ���ļ�
	 */
	public boolean isRemoteFile() {
		return ip != null;// && !(ip.equals(Env.getLocalHost()) && port == Env.getLocalPort());
	}
	
	/**
	 * �����ļ��Ƿ����
	 * @return boolean true�����ڣ�false��������
	 */
	public boolean isExists() {
		return getFile().exists();
	}

	/**
	 * �����ļ��Ĵ�С
	 * @return
	 */
	public long size() {
		return getFile().size();
	}

	/**
	 * �����ļ�����޸�ʱ�䣬�����URL�ļ���jar������ļ��򷵻�-1
	 * @return Date
	 */
	public Timestamp lastModified() {
		long date = getFile().lastModified();
		if (date > 0) {
			return new Timestamp(date);
		} else {
			return null;
		}
	}

	/**
	 * ɾ���ļ�
	 * @return true���ɹ���false��ʧ��
	 */
	public boolean delete() {
		return getFile().delete();
	}
	
	/**
	 * ɾ��Ŀ¼������Ŀ¼
	 * @return true���ɹ���false��ʧ��
	 */
	public boolean deleteDir() {
		return getFile().deleteDir();
	}

	/**
	 *
	 * @param dest String
	 * @param opt String y��Ŀ���ļ��Ѵ���ʱǿ�и���ȱʡ��ʧ�ܣ�c�����ƣ�
	 * 					 p��Ŀ���ļ������Ŀ¼���������Ŀ¼��Ĭ���������Դ�ļ��ĸ�Ŀ¼
	 * @return boolean
	 */
	public boolean move(String dest, String opt) {
		return getFile().move(dest, opt);
	}

	/**
	 * ȡ�����ļ�������
	 * @param key String ��ֵ
	 * @param opt String v����parse
	 * @throws IOException
	 * @return Object
	 */
	public Object getProperty(String key, String opt) throws IOException {
		boolean isValue = opt != null && opt.indexOf('v') != -1;
		
		// httpfile(...).property("header")
		if (file instanceof HttpFile) {
			String str = ((HttpFile)file).getResponseHeader(key);
			if (isValue) {
				return Variant.parse(str);
			} else {
				return str;
			}
		}
		
		InputStream in = getInputStream();
		try {
			Properties properties = new Properties();
			properties.load(new InputStreamReader(in, charset));
			String str = properties.getProperty(key);
			if (isValue) {
				return Variant.parse(str);
			} else {
				return str;
			}
		} finally {
			in.close();
		}
	}

	/**
	 * �������ļ�������{"name", "value"}Ϊ�ṹ�����
	 * @param opt ѡ��
	 * @return ���
	 * @throws IOException
	 */
	public Table getProperties(String opt) throws IOException {
		InputStream in = null;
		try{
			in = getInputStream();
			Properties properties = new Properties();
			properties.load(new InputStreamReader(in, charset));
			return getProperties(properties,opt);
		} finally {
			if(in!=null)
				in.close();
		}
	}
	
	/**
	 * ����������{"name", "value"}Ϊ�ṹ�����
	 * @param properties Properties
	 * @param opt ѡ��
	 * @return ���
	 * @throws IOException
	 */
	public static Table getProperties(Properties properties, String opt) {
		boolean isValue = opt != null && opt.indexOf('v') != -1;
		boolean isQuote = opt != null && opt.indexOf('q') != -1;
		Table table = new Table(new String[] {"name", "value"});
		Iterator<Map.Entry<Object, Object>> itr = properties.entrySet().iterator();
		
		if (isValue || isQuote) {
			while (itr.hasNext()) {
				Map.Entry<Object, Object> entry = itr.next();
				BaseRecord r = table.newLast();
				r.setNormalFieldValue(0, entry.getKey());
				Object v = entry.getValue();
				if (v instanceof String) {
					if(isValue){
						v = Variant.parse((String)v);
					}else{
						v = Escape.addEscAndQuote((String)v);
					}
				}

				r.setNormalFieldValue(1, v);
			}
		} else {
			while (itr.hasNext()) {
				Map.Entry<Object, Object> entry = itr.next();
				BaseRecord r = table.newLast();
				r.setNormalFieldValue(0, entry.getKey());
				r.setNormalFieldValue(1, entry.getValue());
			}
		}

		return table;
	}

	/**
	 * ���ļ��������ڵ�Ŀ¼����һ����ʱ�ļ�
	 * @param prefix ǰ׺
	 * @return ��ʱ�ļ�·����
	 */
	public String createTempFile(String prefix) {
		return getFile().createTempFile(prefix);
	}

	/**
	 * ���ļ��������ڵ�Ŀ¼����һ����ʱ�ļ�
	 * @return ��ʱ�ļ�·����
	 */
	public String createTempFile() {
		return getFile().createTempFile(TEMPFILE_PREFIX);
	}
	
	/**
	 * ���ļ��������ڵ�Ŀ¼����һ����ʱ�ļ�
	 * @return FileObject ��ʱ�ļ�����
	 */
	public static FileObject createTempFileObject() {
		String path = Env.getTempPath();
		if (path != null && path.length() > 0) {
			FileObject fo = new FileObject(path);
			return new FileObject(fo.createTempFile(TEMPFILE_PREFIX));
		} else {
			try {
				File tmpFile = File.createTempFile(TEMPFILE_PREFIX, "");
				return new FileObject(tmpFile.getAbsolutePath());
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
	}

	/**
	 * ��dfx�ļ������������
	 * @return PgmCellSet
	 */
	public PgmCellSet readPgmCellSet() {
		InputStream is = getInputStream();
		try {
			is = new BufferedInputStream(is);
			PgmCellSet pcs;
			if (fileName.toLowerCase().endsWith("." + AppConsts.FILE_SPL)) {
				pcs = AppUtil.readSPL(is);
			} else {
				pcs = CellSetUtil.readPgmCellSet(is);
			}
			
			File file = new File(fileName);
			pcs.setName(file.getPath());
			return pcs;
		} catch (Exception e) {
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e);
			}
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(1); // �汾��

		out.writeObject(fileName);
		out.writeObject(charset);
		out.writeObject(opt);
		out.writeObject(partition);
		out.writeObject(ip);
		out.writeInt(port);
		out.writeBoolean(isSimpleSQL);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		in.readByte(); // �汾��

		fileName = (String) in.readObject();
		charset = (String) in.readObject();
		opt = (String) in.readObject();
		partition = (Integer) in.readObject();
		ip = (String)in.readObject();
		port = in.readInt();
		isSimpleSQL = in.readBoolean();
	}
	
	/**
	 * �����ļ���С
	 * @param size
	 */
	public void setFileSize(long size) {
		IFile file = getFile();
		if (file instanceof LocalFile) {
			((LocalFile)file).setFileSize(size);
		}
	}
	
	/**
	 * ȡ�ļ�����
	 * @return ����FILETYPE_TEXT��FILETYPE_BINARY��FILETYPE_SIMPLETABLE��FILETYPE_GROUPTABLE
	 */
	public int getFileType() {
		InputStream is = null;
		try {
			// ���(rqgt)�����(rqst)�������Ƽ��ļ�(rqtbx)
			is = getFile().getInputStream();
			if (is.read() == 'r' && is.read() == 'q') {
				int b = is.read();
				if (b == 'd') {
					if (is.read() == 'w' && is.read() == 'g' && is.read() == 't') {
						b = is.read();
						if (b == 'c') {
							return FILETYPE_GROUPTABLE;
						} else if (b == 'r') {
							return FILETYPE_GROUPTABLE_ROW;
						}
					}
				} else if (b == 't') {
					if (is.read() == 'b' && is.read() == 'x') {
						return FILETYPE_BINARY;
					}
				}
			}
		} catch (Exception e) {
		} finally {
			try {
				if (is != null) is.close();
			} catch(IOException e) {
			}
		}

		return FILETYPE_TEXT;
	}
	
	/**
	 * ȡ��������ļ����������֧���򷵻�null
	 * @return RandomAccessFile
	 */
	public RandomAccessFile getRandomAccessFile() {
		return getFile().getRandomAccessFile();
	}
	
	/**
	 * ���������ļ��ļ�SQL��ѯ
	 * @return FileObject
	 */
	public static FileObject createSimpleQuery() {
		FileObject fo = new FileObject();
		fo.setIsSimpleSQL(true);
		return fo;
	}
	
	/**
	 * ִ�в�ѯ���
	 * @param sql String ��ѯ���
	 * @param params Object[] ����ֵ
	 * @param cs ICellSet �������
	 * @param ctx Context
	 * @return Object
	 */
	public Object query(String sql, Object []params, ICellSet cs, Context ctx) {
		ArrayList<Object> list = null;
		if (params != null) {
			list = new ArrayList<Object>(params.length);
			for (Object obj : params) {
				list.add(obj);
			}
		}

		SimpleSQL lq = new SimpleSQL(cs, sql, list, ctx);
		Object val = lq.execute();
		if (val instanceof ICursor) {
			return ((ICursor)val).fetch();
		} else {
			return val;
		}
	}
}
