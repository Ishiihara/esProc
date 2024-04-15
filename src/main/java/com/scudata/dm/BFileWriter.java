package com.scudata.dm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;

/**
 * ���ļ�д����
 * @author WangXiaoJun
 *
 */
public class BFileWriter {
	public static int TYPE_BLOCK = 0x10; // �ɷֶζ�ȡ�ļ��ļ�
	public static int TYPE_NORMAL = 0x50; // ��ͨ���ļ�û�зֶ���Ϣ
	public static int TYPE_GROUP = 0x70; // ��ĳ���ֶε�ֵ�ֶεļ��ļ���ֵͬ�Ĳ��ᱻ������
	
	public static final int BLOCKCOUNT = 1024; // ������
	public static final int MINBLOCKRECORDCOUNT = 1024; // ÿ����С��¼��
	public static final String S_FIELDNAME = "_1"; // ��������ʱĬ�ϵ��ֶ���
	
	private FileObject file; // �ļ�����
	private boolean isAppend; // �Ƿ�׷��д�����false��Ḳ�����е��ļ�
	private boolean isBlock; // �Ƿ������зֶ���Ϣ�ļ��ļ�
	private RandomOutputStream ros;
	private RandomObjectWriter writer; // ����д������
	private ObjectWriter normalWriter; // ����д�����ɷֶμ��ļ�
	private DataStruct ds; // �ļ������ݽṹ
	
	private long []blocks; // ÿһ��Ľ���λ��
	private int lastBlock; // ���һ�������
	private long totalRecordCount; // �ܼ�¼��
	private long blockRecordCount; // ÿ��ļ�¼�������鵼��ʱ����ÿ�������
	private long lastRecordCount; // ���һ��ļ�¼��
	
	private long oldFileSize; // ׷��дʱԴ�ļ���С���������ʱ���ļ��ָ�
	
	/**
	 * ���켯�ļ�д����
	 * @param file �ļ�����
	 * @param opt ѡ�a��׷��д��z�������зֶ���Ϣ�ļ��ļ�
	 */
	public BFileWriter(FileObject file, String opt) {
		this.file = file;
		if (opt != null) {
			if (opt.indexOf('a') != -1) isAppend = true;
			if (opt.indexOf('z') != -1) isBlock = true;
		}
	}
	
	/**
	 * ����ʱ�Ƿ������ɼ��ļ���ѡ��
	 * @param opt ѡ������b����zʱ���ɼ��ļ�
	 * @return
	 */
	public static boolean isBtxOption(String opt) {
		return opt != null && (opt.indexOf('b') != -1 || opt.indexOf('z') != -1);
	}
	
	/**
	 * �����ļ�����
	 * @return FileObject
	 */
	public FileObject getFile() {
		return file;
	}
	
	// д�ļ�ͷ
	public void writeHeader(boolean isGroup) throws IOException {
		RandomObjectWriter writer = this.writer;
		writer.position(0);
		writer.write('r');
		writer.write('q');
		writer.write('t');
		writer.write('b');
		writer.write('x');
		
		if (isGroup) {
			writer.write(TYPE_GROUP);
			writer.writeInt32(1); // ����
			writer.writeLong64(totalRecordCount);
			writer.writeLong64(blockRecordCount);
			writer.writeLong64(lastRecordCount);
			writer.writeInt32(lastBlock);
			
			long []blocks = this.blocks;
			writer.writeInt32(blocks.length);
			for (long b : blocks) {
				writer.writeLong64(b);
			}
		} else if (isBlock) {
			writer.write(TYPE_BLOCK);
			writer.writeInt32(0); // ����
			writer.writeLong64(totalRecordCount);
			writer.writeLong64(blockRecordCount);
			writer.writeLong64(lastRecordCount);
			writer.writeInt32(lastBlock);
			
			long []blocks = this.blocks;
			writer.writeInt32(blocks.length);
			for (long b : blocks) {
				writer.writeLong64(b);
			}
		} else {
			writer.write(TYPE_NORMAL);
			writer.writeInt32(0); // ����
			writer.writeLong64(totalRecordCount);
		}
		
		writer.writeStrings(ds.getFieldNames());
	}
	
	// ���ļ�ͷ
	private void readHeader(boolean isGroup) throws IOException {
		InputStream is = ros.getInputStream(0);
		if (is == null) {
			is = file.getInputStream();
		} 
		
		ObjectReader in = new ObjectReader(is);
		
		try {
			if (in.read() != 'r' || in.read() != 'q' || in.read() != 't' || 
					in.read() != 'b' || in.read() != 'x') {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			
			int type = in.read();
			int ver = in.readInt32();
			if (type == TYPE_NORMAL) {
				if (isGroup || isBlock) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("license.fileFormatError"));
				}
				
				totalRecordCount = in.readLong64();
			} else if (type == TYPE_BLOCK) {
				if (isGroup) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("license.fileFormatError"));
				}

				isBlock = true;
				totalRecordCount = in.readLong64();
				blockRecordCount = in.readLong64();
				lastRecordCount = in.readLong64();
				lastBlock = in.readInt32();
				
				int count = in.readInt32();
				long []blocks = new long[count];
				this.blocks = blocks;
				for (int i = 0; i < count; ++i) {
					blocks[i] = in.readLong64();
				}
			} else if (type == TYPE_GROUP) {
				if (!isGroup || ver == 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("license.fileFormatError"));
				}

				totalRecordCount = in.readLong64();
				blockRecordCount = in.readLong64();
				lastRecordCount = in.readLong64();
				lastBlock = in.readInt32();
				
				int count = in.readInt32();
				long []blocks = new long[count];
				this.blocks = blocks;
				for (int i = 0; i < count; ++i) {
					blocks[i] = in.readLong64();
				}
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			
			ds = new DataStruct(in.readStrings());
		} finally {
			in.close();
		}
	}
	
	/**
	 * ���ļ�׼��д
	 * @param ds Ҫд������ݵ����ݽṹ
	 * @param isGroup �Ƿ��з����ֶ�
	 * @throws IOException
	 */
	public void prepareWrite(DataStruct ds, boolean isGroup) throws IOException {
		if (isAppend ) {
			// ���������жϴ�С
			ros = file.getRandomOutputStream(true);
			writer = new RandomObjectWriter(ros);
			oldFileSize = file.size();
			
			if (oldFileSize > 0) {
				readHeader(isGroup);
				writer.position(oldFileSize);
			} else {
				if (isBlock) {
					blocks = new long[BLOCKCOUNT];
					if (isGroup) {
						blockRecordCount = 1;
					} else {
						blockRecordCount = MINBLOCKRECORDCOUNT;
					}
				}
				
				this.ds = ds; 
				writeHeader(isGroup);
			}
		} else {
			ros = file.getRandomOutputStream(false);
			writer = new RandomObjectWriter(ros);
			oldFileSize = 0;
			if (isBlock) {
				blocks = new long[BLOCKCOUNT];
				if (isGroup) {
					blockRecordCount = 1;
				} else {
					blockRecordCount = MINBLOCKRECORDCOUNT;
				}
			}
			
			this.ds = ds; 
			writeHeader(isGroup);
		}
	}
	
	/**
	 * д�������ر��ļ�
	 */
	public void close() {
		if (writer != null) {
			try {
				writer.close();
				writer = null;
			} catch (IOException e) {
				throw new RQException(e);
			}
		} else if (normalWriter != null) {
			try {
				normalWriter.close();
				normalWriter = null;
			} catch (IOException e) {
				throw new RQException(e);
			}
		}
	}
	
	// ȡ���ݵ����ݽṹ
	private DataStruct getDataStruct(Sequence seq, Expression []exps, String []names) {
		int fcount = exps.length;
		String []tmps = new String[fcount];
		if (names != null) {
			System.arraycopy(names, 0, tmps, 0, fcount);
		}
		
		seq.getNewFieldNames(exps, tmps, "export");
		return new DataStruct(tmps);
	}
	
	// �����ֶε�˳�򣬸�֮ǰ�Ľṹ����
	private Expression[] adjustDataStruct(DataStruct ds, Expression []exps, String []names) {
		DataStruct srcDs = this.ds;
		if (srcDs.isCompatible(ds)) {
			return exps;
		}
		
		int fcount = srcDs.getFieldCount();
		if (fcount != ds.getFieldCount()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.dsNotMatch"));
		}
		
		if (exps == null) {
			exps = new Expression[fcount];
			String []fields = srcDs.getFieldNames();
			for (int i = 0; i < fcount; ++i) {
				int index = ds.getFieldIndex(fields[i]);
				if (index < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("engine.dsNotMatch"));
				}
				
				exps[i] = new Expression("#" + (index + 1));
			}
			
			return exps;
		} else if (names != null) {
			Expression []tmp = new Expression[fcount];
			for (int i = 0; i < fcount; ++i) {
				if (names[i] != null) {
					int index = ds.getFieldIndex(names[i]);
					if (index < 0 || tmp[index] != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.dsNotMatch"));
					}
					
					tmp[index] = exps[i];
				} else {
					if (tmp[i] != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.dsNotMatch"));
					}
					
					tmp[i] = exps[i];
				}
			}
			
			return tmp;
		} else {
			return exps;
		}
	}

	// �ı��ļ��洢��ʽΪ�ɷֶ��ļ�
	private void changeToSegmentFile(Context ctx) throws IOException {
		Sequence seq = null;
		BFileReader reader = new BFileReader(file);
		try {
			reader.open();
			seq = reader.readAll();
		} finally {
			reader.close();
		}
		
		BFileWriter writer = new BFileWriter(file, "z");
		writer.export(seq, null, null, ctx);
	}
	
	/**
	 * ��������
	 * @param data ����
	 * @param exps �������ֶα��ʽ���飬ʡ���򵼳������ֶ�
	 * @param names �ֶ�������
	 * @param ctx ����������
	 */
	public void export(Sequence data, Expression []exps, String []names, Context ctx) {
		if (data == null || data.length() == 0) {
			if (!isAppend) file.delete();
			return;
		}
		
		if (!isAppend && data.length() > MINBLOCKRECORDCOUNT) {
			isBlock = true;
		}
		
		DataStruct ds;
		if (exps != null) {
			ds = getDataStruct(data, exps, names);
		} else {
			ds = data.dataStruct();
			if (ds == null) {
				ds = new DataStruct(new String[]{S_FIELDNAME});
			}
		}
		
		try {
			prepareWrite(ds, false);
			if (isAppend && !isBlock) {
				if (data.length() + totalRecordCount > MINBLOCKRECORDCOUNT) {
					close();
					changeToSegmentFile(ctx);
					isBlock = true;
					prepareWrite(ds, false);
				}
			}
			
			adjustDataStruct(ds, exps, names);
			if (isBlock) {
				exportBlock(data, exps, ctx);
			} else {
				exportNormal(data, exps, ctx);
			}
			
			writer.flush();
			writeHeader(false);
		} catch (Exception e) {
			file.setFileSize(oldFileSize);
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e);
			}
		} finally {
			close();
		}
	}
	
	/**
	 * ��������
	 * @param cursor �α�
	 * @param exps �������ֶα��ʽ���飬ʡ���򵼳������ֶ�
	 * @param names �ֶ�������
	 * @param ctx ����������
	 */
	public void export(ICursor cursor, Expression []exps, String []names, Context ctx) {
		Sequence data = cursor.fetch(ICursor.FETCHCOUNT);
		if (data == null || data.length() == 0) {
			if (!isAppend) file.delete();
			return;
		}
		
		if (!isAppend) {
			isBlock = true;
		}
		
		DataStruct ds;
		if (exps != null) {
			ds = getDataStruct(data, exps, names);
		} else {
			ds = data.dataStruct();
			if (ds == null) {
				ds = new DataStruct(new String[]{S_FIELDNAME});
			}
		}
		
		try {
			prepareWrite(ds, false);
			if (isAppend && !isBlock) {
				if (data.length() + totalRecordCount > MINBLOCKRECORDCOUNT) {
					close();
					changeToSegmentFile(ctx);
					isBlock = true;
					prepareWrite(ds, false);
				}
			}
			
			adjustDataStruct(ds, exps, names);
			if (isBlock) {
				while (data != null && data.length() > 0) {
					exportBlock(data, exps, ctx);
					data = cursor.fetch(ICursor.FETCHCOUNT);
				}
			} else {
				while (data != null && data.length() > 0) {
					exportNormal(data, exps, ctx);
					data = cursor.fetch(ICursor.FETCHCOUNT);
				}
			}
			
			writer.flush();
			writeHeader(false);
		} catch (Exception e) {
			file.setFileSize(oldFileSize);
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e);
			}
		} finally {
			close();
		}
	}
	
	// �зֶ���Ϣ�ĵ���
	private void exportBlock(Sequence data, Expression []exps, Context ctx) throws IOException {
		RandomObjectWriter writer = this.writer;
		long []blocks = this.blocks;
		int blockCount = blocks.length;
		int lastBlock = this.lastBlock;
		long blockRecordCount = this.blockRecordCount;
		long lastRecordCount = this.lastRecordCount;
		int fcount = ds.getFieldCount();
		int len = data.length();
		
		if (exps == null) {
			boolean isTable = data.getMem(1) instanceof BaseRecord;
			for (int i = 1; i <= len; ++i) {
				if (lastRecordCount == blockRecordCount) {
					blocks[lastBlock++] = writer.position();
					lastRecordCount = 0;
					if (lastBlock == blockCount) {
						blockRecordCount += blockRecordCount;
						lastBlock = blockCount / 2;
						for (int b = 0, j = 1; b < lastBlock; ++b, j += 2) {
							blocks[b] = blocks[j];
						}
					}
				}
				
				lastRecordCount++;
				if (isTable) {
					BaseRecord r = (BaseRecord)data.getMem(i);
					Object []vals = r.getFieldValues();
					for (int f = 0; f < fcount; ++f) {
						writer.writeObject(vals[f]);
					}
				} else {
					writer.writeObject(data.getMem(i));
				}
			}
		} else {
			ComputeStack stack = ctx.getComputeStack();
			Current current = new Current(data);
			stack.push(current);
			
			try {
				for (int i = 1; i <= len; ++i) {
					if (lastRecordCount == blockRecordCount) {
						blocks[lastBlock++] = writer.position();
						lastRecordCount = 0;
						if (lastBlock == blockCount) {
							blockRecordCount += blockRecordCount;
							lastBlock = blockCount / 2;
							for (int b = 0, j = 1; b < lastBlock; ++b, j += 2) {
								blocks[b] = blocks[j];
							}
						}
					}
					
					lastRecordCount++;
					current.setCurrent(i);
					for (int f = 0; f < fcount; ++f) {
						writer.writeObject(exps[f].calculate(ctx));
					}
				}
			} finally {
				stack.pop();
			}
		}
		
		blocks[lastBlock] = writer.position();
		this.totalRecordCount += len;
		this.lastBlock = lastBlock;
		this.blockRecordCount = blockRecordCount;
		this.lastRecordCount = lastRecordCount;
	}
	
	// �з�����Ϣ�ĵ���
	private void exportGroup(Sequence data, Expression []exps, Context ctx) throws IOException {
		RandomObjectWriter writer = this.writer;
		int fcount = ds.getFieldCount();
		int len = data.length();
		
		if (exps == null) {
			boolean isTable = data.getMem(1) instanceof BaseRecord;
			for (int i = 1; i <= len; ++i) {
				if (isTable) {
					BaseRecord r = (BaseRecord)data.getMem(i);
					Object []vals = r.getFieldValues();
					for (int f = 0; f < fcount; ++f) {
						writer.writeObject(vals[f]);
					}
				} else {
					writer.writeObject(data.getMem(i));
				}
			}
		} else {
			ComputeStack stack = ctx.getComputeStack();
			Current current = new Current(data);
			stack.push(current);
			
			try {
				for (int i = 1; i <= len; ++i) {
					current.setCurrent(i);
					for (int f = 0; f < fcount; ++f) {
						writer.writeObject(exps[f].calculate(ctx));
					}
				}
			} finally {
				stack.pop();
			}
		}
		
		this.totalRecordCount += len;
		long []blocks = this.blocks;
		
		if (lastRecordCount == blockRecordCount) {
			lastBlock++;
			lastRecordCount = 0;
			if (lastBlock == blocks.length) {
				blockRecordCount += blockRecordCount;
				lastBlock = blocks.length / 2;
				for (int b = 0, j = 1; b < lastBlock; ++b, j += 2) {
					blocks[b] = blocks[j];
				}
			}
		}
		
		lastRecordCount++;
		blocks[lastBlock] = writer.position();
	}
	
	// û�зֶ���Ϣ�ĵ���
	private void exportNormal(Sequence data, Expression []exps, Context ctx) throws IOException {
		RandomObjectWriter writer = this.writer;
		int fcount = ds.getFieldCount();
		int len = data.length();
		
		if (exps == null) {
			boolean isTable = data.getMem(1) instanceof BaseRecord;
			if (isTable) {
				for (int i = 1; i <= len; ++i) {
					BaseRecord r = (BaseRecord)data.getMem(i);
					Object []vals = r.getFieldValues();
					for (int f = 0; f < fcount; ++f) {
						writer.writeObject(vals[f]);
					}
				}
			} else {
				for (int i = 1; i <= len; ++i) {
					writer.writeObject(data.getMem(i));
				}
			}
		} else {
			ComputeStack stack = ctx.getComputeStack();
			Current current = new Current(data);
			stack.push(current);
			
			try {
				for (int i = 1; i <= len; ++i) {
					current.setCurrent(i);
					for (int f = 0; f < fcount; ++f) {
						writer.writeObject(exps[f].calculate(ctx));
					}
				}
			} finally {
				stack.pop();
			}
		}

		this.totalRecordCount += len;		
	}

	/**
	 * ��������
	 * @param cursor �α�
	 * @param exps �������ֶα��ʽ���飬ʡ���򵼳������ֶ�
	 * @param names �ֶ�������
	 * @param gexp ������ʽ��ʹ�ñ��ʽ���ؽ����ͬ�ļ�¼���ᱻ�������
	 * @param ctx ����������
	 */
	public void export(ICursor cursor, Expression []exps, String []names, Expression gexp, Context ctx) {
		Sequence data = cursor.fetchGroup(gexp, ctx);
		if (data == null || data.length() == 0) {
			if (!isAppend) file.delete();
			return;
		}
		
		if (!isAppend) {
			isBlock = true;
		}
		
		DataStruct ds;
		if (exps != null) {
			ds = getDataStruct(data, exps, names);
		} else {
			ds = data.dataStruct();
			if (ds == null) {
				ds = new DataStruct(new String[]{S_FIELDNAME});
			}
		}
		
		try {
			prepareWrite(ds, true);
			adjustDataStruct(ds, exps, names);
			
			while (data != null && data.length() > 0) {
				exportGroup(data, exps, ctx);
				data = cursor.fetchGroup(gexp, ctx);
			}
			
			writer.flush();
			writeHeader(true);
		} catch (Exception e) {
			file.setFileSize(oldFileSize);
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e);
			}
		} finally {
			close();
		}
	}

	// д���ֿ��ļ�ͷ
	private static void writeHeader(ObjectWriter writer, DataStruct ds) throws IOException {
		writer.write('r');
		writer.write('q');
		writer.write('t');
		writer.write('b');
		writer.write('x');
		
		writer.write(TYPE_NORMAL);
		writer.writeInt32(0); // ����
		writer.writeLong64(0);
		
		if (ds != null) {
			writer.writeStrings(ds.getFieldNames());
		} else {
			writer.writeStrings(new String[] {S_FIELDNAME});
		}
	}

	/**
	 * �������ֿ�����
	 * @param writer д����
	 * @param data ����
	 * @param ds ���ݵ����ݽṹ����������д��
	 * @param writeHeader �Ƿ�д�ļ�ͷ
	 * @throws IOException
	 */
	public static void export(ObjectWriter writer, Sequence data, 
			DataStruct ds, boolean writeHeader) throws IOException {
		if (writeHeader) {
			writeHeader(writer, ds);
		}
		
		int len = data.length();
		if (ds != null) {
			int fcount = ds.getFieldCount();
			for (int i = 1; i <= len; ++i) {
				BaseRecord r = (BaseRecord)data.getMem(i);
				Object []vals = r.getFieldValues();
				for (int f = 0; f < fcount; ++f) {
					writer.writeObject(vals[f]);
				}
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				writer.writeObject(data.getMem(i));
			}
		}
	}

	/**
	 * �������ֿ��α�
	 * @param writer д����
	 * @param cursor �α�
	 * @param ds ���ݵ����ݽṹ����������д��
	 * @param writeHeader �Ƿ�д�ļ�ͷ
	 * @throws IOException
	 */
	public static void export(ObjectWriter writer, ICursor cursor, boolean writeHeader) throws IOException {
		Sequence data = cursor.fetch(ICursor.FETCHCOUNT);
		if (data == null || data.length() == 0) {
			return;
		}
		
		DataStruct ds = data.dataStruct();
		if (writeHeader) {
			writeHeader(writer, ds);
		}

		while (data != null && data.length() > 0) {
			export(writer, data, ds, false);
			data = cursor.fetch(ICursor.FETCHCOUNT);
		}
	}
	
	/**
	 * ���ֶε���������ⲿ���ƴ򿪹ر��ļ�
	 * @param data
	 * @throws IOException
	 */
	public void write(Sequence data) throws IOException {
		RandomObjectWriter writer = this.writer;
		int fcount = ds.getFieldCount();
		int len = data.length();
		
		for (int i = 1; i <= len; ++i) {
			BaseRecord r = (BaseRecord)data.getMem(i);
			Object []vals = r.getFieldValues();
			for (int f = 0; f < fcount; ++f) {
				writer.writeObject(vals[f]);
			}
		}

		this.totalRecordCount += len;		
	}
	
	/**
	 * д�ɴ��ֶ���Ϣ������
	 * @param data
	 * @throws IOException
	 */
	public void writeBlock(Sequence data) throws IOException {
		RandomObjectWriter writer = this.writer;
		boolean isTable = data.getMem(1) instanceof BaseRecord;
		long []blocks = this.blocks;
		int blockCount = blocks.length;
		int lastBlock = this.lastBlock;
		long blockRecordCount = this.blockRecordCount;
		long lastRecordCount = this.lastRecordCount;
		int fcount = ds.getFieldCount();
		int len = data.length();
		
		for (int i = 1; i <= len; ++i) {
			if (lastRecordCount == blockRecordCount) {
				blocks[lastBlock++] = writer.position();
				lastRecordCount = 0;
				if (lastBlock == blockCount) {
					blockRecordCount += blockRecordCount;
					lastBlock = blockCount / 2;
					for (int b = 0, j = 1; b < lastBlock; ++b, j += 2) {
						blocks[b] = blocks[j];
					}
				}
			}
			
			lastRecordCount++;
			if (isTable) {
				BaseRecord r = (BaseRecord)data.getMem(i);
				Object []vals = r.getFieldValues();
				for (int f = 0; f < fcount; ++f) {
					writer.writeObject(vals[f]);
				}
			} else {
				writer.writeObject(data.getMem(i));
			}
		}
		
		blocks[lastBlock] = writer.position();
		this.totalRecordCount += len;
		this.lastBlock = lastBlock;
		this.blockRecordCount = blockRecordCount;
		this.lastRecordCount = lastRecordCount;
	}
	
	/**
	 * �������Ƶ�������
	 * @param cursor �α�
	 * @param exps �������ֶα��ʽ���飬ʡ���򵼳������ֶ�
	 * @param names �ֶ�������
	 * @param ctx ����������
	 */
	public void exportBinary(ICursor cursor, DataStruct ds, int fieldIndex, Context ctx) {
		Sequence data = cursor.fetch(ICursor.FETCHCOUNT);
		if (data == null || data.length() == 0) {
			if (!isAppend) file.delete();
			return;
		}
		
		if (!isAppend) {
			isBlock = true;
		}

		if (ds == null) {
			ds = new DataStruct(new String[]{S_FIELDNAME});
		}
		
		try {
			prepareWrite(ds, false);
			if (isAppend && !isBlock) {
				if (data.length() + totalRecordCount > MINBLOCKRECORDCOUNT) {
					close();
					changeToSegmentFile(ctx);
					isBlock = true;
					prepareWrite(ds, false);
				}
			}
			
			if (isBlock) {
				while (data != null && data.length() > 0) {
					exportBinaryBlock(data, fieldIndex, ctx);
					data = cursor.fetch(ICursor.FETCHCOUNT);
				}
			} else {
				while (data != null && data.length() > 0) {
					exportBinaryNormal(data, fieldIndex, ctx);
					data = cursor.fetch(ICursor.FETCHCOUNT);
				}
			}
			
			writer.flush();
			writeHeader(false);
		} catch (Exception e) {
			file.setFileSize(oldFileSize);
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e);
			}
		} finally {
			close();
		}
	}
	
	// �зֶ���Ϣ�ĵ���
	private void exportBinaryBlock(Sequence data, int fieldIndex, Context ctx) throws IOException {
		RandomObjectWriter writer = this.writer;
		long []blocks = this.blocks;
		int blockCount = blocks.length;
		int lastBlock = this.lastBlock;
		long blockRecordCount = this.blockRecordCount;
		long lastRecordCount = this.lastRecordCount;
		int len = data.length();
		
		for (int i = 1; i <= len; ++i) {
			if (lastRecordCount == blockRecordCount) {
				blocks[lastBlock++] = writer.position();
				lastRecordCount = 0;
				if (lastBlock == blockCount) {
					blockRecordCount += blockRecordCount;
					lastBlock = blockCount / 2;
					for (int b = 0, j = 1; b < lastBlock; ++b, j += 2) {
						blocks[b] = blocks[j];
					}
				}
			}
			
			lastRecordCount++;
			
			BaseRecord r = (BaseRecord)data.getMem(i);
			byte[] bytes = (byte[]) r.getNormalFieldValue(fieldIndex);
			writer.write(bytes);
		}
		
		blocks[lastBlock] = writer.position();
		this.totalRecordCount += len;
		this.lastBlock = lastBlock;
		this.blockRecordCount = blockRecordCount;
		this.lastRecordCount = lastRecordCount;
	}
	
	// û�зֶ���Ϣ�ĵ���
	private void exportBinaryNormal(Sequence data, int fieldIndex, Context ctx) throws IOException {
		RandomObjectWriter writer = this.writer;
		int len = data.length();
		
		for (int i = 1; i <= len; ++i) {
			BaseRecord r = (BaseRecord)data.getMem(i);
			byte[] bytes = (byte[]) r.getNormalFieldValue(fieldIndex);
			writer.write(bytes);
		}
		
		this.totalRecordCount += len;		
	}
	
	/**
	 * ׼��д���ɷֶμ��ļ�
	 * @throws IOException
	 */
	public void prepareWriteNormal(DataStruct ds) throws IOException {
		this.ds = ds;
		OutputStream os = file.getOutputStream(false);
		normalWriter = new ObjectWriter(os);
		normalWriter.write('r');
		normalWriter.write('q');
		normalWriter.write('t');
		normalWriter.write('b');
		normalWriter.write('x');
		
		normalWriter.write(TYPE_NORMAL);
		normalWriter.writeInt32(0); // ����
		normalWriter.writeLong64(totalRecordCount);
		normalWriter.writeStrings(ds.getFieldNames());
	}

	/**
	 * ��������������ɷֶμ��ļ�
	 * @param data
	 */
	public void exportNormal(Sequence data) {
		if (data == null || data.length() == 0) {
			file.delete();
			return;
		}
		
		totalRecordCount = data.length();
		ds = data.dataStruct();
		if (ds == null) {
			ds = new DataStruct(new String[]{S_FIELDNAME});
		}
		
		try {
			prepareWriteNormal(ds);
			writeNormal(data);
		} catch (Exception e) {
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e);
			}
		} finally {
			close();
		}
	}
	
	/**
	 * �������д�����ɷֶμ��ļ�
	 * @param data
	 */
	public void writeNormal(Sequence data) throws IOException {
		ObjectWriter writer = this.normalWriter;
		int fcount = ds.getFieldCount();
		int len = data.length();
		
		boolean isTable = data.getMem(1) instanceof BaseRecord;
		if (isTable) {
			for (int i = 1; i <= len; ++i) {
				BaseRecord r = (BaseRecord)data.getMem(i);
				Object []vals = r.getFieldValues();
				for (int f = 0; f < fcount; ++f) {
					writer.writeObject(vals[f]);
				}
			}
		} else {
			for (int i = 1; i <= len; ++i) {
				writer.writeObject(data.getMem(i));
			}
		}
	}
}