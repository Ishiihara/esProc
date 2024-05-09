package com.scudata.dw;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import com.scudata.array.LongArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Env;
import com.scudata.resources.EngineMessage;

/**
 * �д������
 * @author runqian
 *
 */
public class ColComTable extends ComTable {
	/**
	 * ���Ѿ����ڵ����
	 * @param file
	 * @param raf
	 * @param ctx
	 * @throws IOException
	 */
	public ColComTable(File file, RandomAccessFile raf, Context ctx) throws IOException {
		this.file = file;
		this.raf = raf;
		this.ctx = ctx;
		if (ctx != null) 
			ctx.addResource(this);
		readHeader();
	}

	/**
	 * ���Ѿ����ڵ����
	 * @param file
	 * @param ctx
	 * @throws IOException
	 */
	public ColComTable(File file, Context ctx) throws IOException {
		this.file = file;
		this.raf = new RandomAccessFile(file, "rw");
		this.ctx = ctx;
		if (ctx != null) 
			ctx.addResource(this);
		readHeader();
	}
	
	/**
	 * ���Ѿ����ڵ����,����������־�����ڲ�ʹ��
	 * @param file
	 * @throws IOException
	 */
	public ColComTable(File file) throws IOException {
		this.file = file;
		this.raf = new RandomAccessFile(file, "rw");
		readHeader();
	}

	/**
	 * �������
	 * @param file ���ļ�
	 * @param colNames ������
	 * @param distribute �ֲ����ʽ
	 * @param opt u����ѹ�����ݣ�p������һ�ֶηֶ�
	 * @param ctx ������
	 * @throws IOException
	 */
	public ColComTable(File file, String []colNames, String distribute, String opt, Context ctx) 
			throws IOException {
		this(file, colNames, distribute, opt, null, ctx);
	}
	
	/**
	 * �������
	 * @param file ���ļ�
	 * @param colNames ������
	 * @param distribute �ֲ����ʽ
	 * @param opt u����ѹ�����ݣ�p������һ�ֶηֶ�
	 * @param blockSize �����С
	 * @param ctx ������
	 * @throws IOException
	 */
	public ColComTable(File file, String []colNames, String distribute, String opt, Integer blockSize, Context ctx) 
			throws IOException {
		this(file, null, colNames, distribute, opt, blockSize, ctx);
	}
	
	/**
	 * �������
	 * @param file ���ļ�
	 * @param raf ָ����raf
	 * @param colNames ������
	 * @param distribute �ֲ����ʽ
	 * @param opt u����ѹ�����ݣ�p������һ�ֶηֶ�
	 * @param blockSize �����С
	 * @param ctx ������
	 * @throws IOException
	 */
	public ColComTable(File file, RandomAccessFile raf, String []colNames, String distribute, String opt, Integer blockSize, Context ctx) 
			throws IOException {
		file.delete();
		File parent = file.getParentFile();
		if (parent != null) {
			// ����Ŀ¼���������Ŀ¼������RandomAccessFile�����쳣
			parent.mkdirs();
		}
		
		this.file = file;
		if (raf == null) {
			this.raf = new RandomAccessFile(file, "rw");
		} else {
			this.raf = raf;
		}
		this.ctx = ctx;
		if (ctx != null) {
			ctx.addResource(this);
		}
		
		// �Ƿ�ѹ��
		if (opt != null && opt.indexOf('u') != -1) {
			setCompress(false);
		}
		// �Ƿ����������ʹ�
		if (opt != null && opt.indexOf('v') != -1) {
			setCheckDataPure(true);
		}
		// �Ƿ���ʱ���
		if (opt != null && opt.indexOf('t') != -1) {
			setTimeKey(true);
		}
		// �Ƿ���ɾ����
		if (opt != null && opt.indexOf('d') != -1) {
			setDeleteKey(true);
		}
				
		if (blockSize == null)
			blockSize = Env.getBlockSize();
		else {
			int tempSize = blockSize % MIN_BLOCK_SIZE;
			if (tempSize != 0) 
				blockSize = blockSize - tempSize + MIN_BLOCK_SIZE;//4K����
			if (blockSize < MIN_BLOCK_SIZE)
				blockSize = MIN_BLOCK_SIZE;
		}
		setBlockSize(blockSize);
		
		enlargeSize = blockSize * 16;
		headerBlockLink = new BlockLink(this);
		headerBlockLink.setFirstBlockPos(applyNewBlock());

		baseTable = new ColPhyTable(this, colNames);
		structManager = new StructManager();
		
		// ����һ�ֶηֶ�
		if (opt != null && opt.indexOf('p') != -1) {
			baseTable.segmentCol = baseTable.getColName(0);
		}
		
		this.distribute = distribute;
		this.reserve[0] = 4;
		save();
	}
	
	/**
	 * ����src�Ľṹ����һ��������ļ�
	 * @param file �±���ļ�
	 * @param src ԭ���
	 * @throws IOException
	 */
	public ColComTable(File file, ColComTable src) throws IOException {
		this.file = file;
		this.raf = new RandomAccessFile(file, "rw");
		this.ctx = src.ctx;
		if (ctx != null) {
			ctx.addResource(this);
		}
		
		System.arraycopy(src.reserve, 0, reserve, 0, reserve.length);
		blockSize = src.blockSize;
		enlargeSize = src.enlargeSize;
		
		headerBlockLink = new BlockLink(this);
		headerBlockLink.setFirstBlockPos(applyNewBlock());
		writePswHash = src.writePswHash;
		readPswHash = src.readPswHash;
		distribute = src.distribute;
		structManager = src.structManager;
		
		try {
			baseTable = new ColPhyTable(this, null, (ColPhyTable) src.baseTable);
		} catch (Exception e) {
			if (raf != null) {
				raf.close();
			}
		}
		save();
	}
	
	/**
	 * ���´�����ļ������������޸�
	 * @throws IOException
	 */
	protected void reopen() throws IOException {
		// ��д�ļ�ͷʱ��ͬ����֧��ͬʱ��д
		raf = new RandomAccessFile(file, "rw");
		Object syncObj = getSyncObject();
		synchronized(syncObj) {
			restoreTransaction();
			raf.seek(0);
			byte []bytes = new byte[32];
			raf.read(bytes);
			if (bytes[0] != 'r' || bytes[1] != 'q' || bytes[2] != 'd' || bytes[3] != 'w' || bytes[4] != 'g' || bytes[5] != 't') {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			if (bytes[6] != 'c' && bytes[6] != 'C') {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			
			BufferReader reader = new BufferReader(structManager, bytes, 7, 25);
			setBlockSize(reader.readInt32());
			headerBlockLink = new BlockLink(this);
			headerBlockLink.readExternal(reader);
			
			BlockLinkReader headerReader = new BlockLinkReader(headerBlockLink);
			bytes = headerReader.readBlocks();
			headerReader.close();
			reader = new BufferReader(structManager, bytes);
			reader.read(); // r
			reader.read(); // q
			reader.read(); // d
			reader.read(); // w
			reader.read(); // g
			reader.read(); // t
			reader.read(); // c
			
			blockSize = reader.readInt32();
			headerBlockLink.readExternal(reader);
			
			reader.read(reserve); // ����λ
			freePos = reader.readLong40();
			fileSize = reader.readLong40();
			
			if (reserve[0] > 0) {
				writePswHash = reader.readString();
				readPswHash = reader.readString();
				checkPassword(null);
				
				if (reserve[0] > 1) {
					distribute = reader.readString();
				}
			}
			
			int dsCount = reader.readInt();
			if (dsCount > 0) {
				ArrayList<DataStruct> dsList = new ArrayList<DataStruct>(dsCount);
				for (int i = 0; i < dsCount; ++i) {
					String []fieldNames = reader.readStrings();
					DataStruct ds = new DataStruct(fieldNames);
					dsList.add(ds);
				}
				
				structManager = new StructManager(dsList);
			} else {
				structManager = new StructManager();
			}
			
			//baseTable = new ColumnTableMetaData(this, null);
			baseTable.readExternal(reader);
		}
	}
	
	/**
	 * ��ȡ�ļ�ͷ
	 * �޸Ķ�дʱ��Ҫͬ���޸�reopen����
	 */
	protected void readHeader() throws IOException {
		// ��д�ļ�ͷʱ��ͬ����֧��ͬʱ��д
		Object syncObj = getSyncObject();
		synchronized(syncObj) {
			//restoreTransaction();
			raf.seek(0);
			byte []bytes = new byte[32];
			raf.read(bytes);
			if (bytes[0] != 'r' || bytes[1] != 'q' || bytes[2] != 'd' || bytes[3] != 'w' || bytes[4] != 'g' || bytes[5] != 't') {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			if (bytes[6] != 'c' && bytes[6] != 'C') {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("license.fileFormatError"));
			}
			
			BufferReader reader = new BufferReader(structManager, bytes, 7, 25);
			setBlockSize(reader.readInt32());
			headerBlockLink = new BlockLink(this);
			headerBlockLink.readExternal(reader);
			
			BlockLinkReader headerReader = new BlockLinkReader(headerBlockLink);
			bytes = headerReader.readBlocks();
			headerReader.close();
			reader = new BufferReader(structManager, bytes);
			reader.read(); // r
			reader.read(); // q
			reader.read(); // d
			reader.read(); // w
			reader.read(); // g
			reader.read(); // t
			reader.read(); // c
			
			blockSize = reader.readInt32();
			headerBlockLink.readExternal(reader);
			
			reader.read(reserve); // ����λ
			freePos = reader.readLong40();
			fileSize = reader.readLong40();
			
			if (reserve[0] > 0) {
				writePswHash = reader.readString();
				readPswHash = reader.readString();
				checkPassword(null);
				
				if (reserve[0] > 1) {
					distribute = reader.readString();
				}
			}
			
			int dsCount = reader.readInt();
			if (dsCount > 0) {
				ArrayList<DataStruct> dsList = new ArrayList<DataStruct>(dsCount);
				for (int i = 0; i < dsCount; ++i) {
					String []fieldNames = reader.readStrings();
					DataStruct ds = new DataStruct(fieldNames);
					dsList.add(ds);
				}
				
				structManager = new StructManager(dsList);
			} else {
				structManager = new StructManager();
			}
			
			baseTable = new ColPhyTable(this);
			baseTable.readExternal(reader);
		}
	}
	
	/**
	 * д�ļ�ͷ
	 */
	protected void writeHeader() throws IOException {
		//����д�Ͱ汾���ļ�
		if (reserve[0] < 4) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.oldVersion2"));
		}
		
		// ��д�ļ�ͷʱ��ͬ����֧��ͬʱ��д
		Object syncObj = getSyncObject();
		synchronized(syncObj) {
			beginTransaction(null);
			BufferWriter writer = new BufferWriter(structManager);
			writer.write('r');
			writer.write('q');
			writer.write('d');
			writer.write('w');
			writer.write('g');
			writer.write('t');
			writer.write('C');
			
			writer.writeInt32(blockSize);
			headerBlockLink.writeExternal(writer);
			
			reserve[0] = 4; // 1�������룬2���ӷֲ�������3����Ԥ���飬4��ʽ�洢���������ͣ��ֵ�ȣ�
			writer.write(reserve); // ����λ
			
			writer.writeLong40(freePos);
			writer.writeLong40(fileSize);
			
			// ����������Ա�汾1���ӵ�
			writer.writeString(writePswHash);
			writer.writeString(readPswHash);
			
			writer.writeString(distribute); // �汾2����
			
			ArrayList<DataStruct> dsList = structManager.getStructList();
			if (dsList != null) {
				writer.writeInt(dsList.size());
				for (DataStruct ds : dsList) {
					String []fieldNames = ds.getFieldNames();
					writer.writeStrings(fieldNames);
				}
			} else {
				writer.writeInt(0);
			}
			
			baseTable.writeExternal(writer);
			
			BlockLinkWriter headerWriter = new BlockLinkWriter(headerBlockLink, false);
			headerWriter.rewriteBlocks(writer.finish());
			headerWriter.close();
			//headerWriter.finishWrite();
			
			// ��дheaderBlockLink
			writer.write('r');
			writer.write('q');
			writer.write('d');
			writer.write('w');
			writer.write('g');
			writer.write('t');
			writer.write('C');
			
			writer.writeInt32(blockSize);
			headerBlockLink.writeExternal(writer);
			raf.seek(0);
			raf.write(writer.finish());
			raf.getChannel().force(true);
			commitTransaction(0);
		}
	}
	
	/**
	 * �����������Ϣ,���������header�Ͳ���
	 */
	public long[] getBlockLinkInfo() {
		LongArray info = new LongArray(1024);
		ColPhyTable baseTable = (ColPhyTable) this.baseTable;
		//segment block link
		BlockLink segmentBlockLink = baseTable.segmentBlockLink;
		info.add(segmentBlockLink.firstBlockPos);
		info.add(segmentBlockLink.lastBlockPos);
		info.add(segmentBlockLink.freeIndex);
		info.add(segmentBlockLink.blockCount);
		
		//columns block link
		ColumnMetaData []columns = baseTable.getColumns();
		for (ColumnMetaData col : columns) {
			col.getBlockLinkInfo(info);
		}
		
		for (PhyTable table : baseTable.tableList) {
			//segment block link
			segmentBlockLink = table.segmentBlockLink;
			info.add(segmentBlockLink.firstBlockPos);
			info.add(segmentBlockLink.lastBlockPos);
			info.add(segmentBlockLink.freeIndex);
			info.add(segmentBlockLink.blockCount);
			//���� block link
			((ColPhyTable) table).getGuideColumn().getBlockLinkInfo(info);
			//columns block link
			columns = ((ColPhyTable) table).getColumns();
			for (ColumnMetaData col : columns) {
				col.getBlockLinkInfo(info);
			}
		}
		return info.getDatas();
	}
	
	public boolean isPureFormat() {
		return reserve[0] > 3;
	}
}