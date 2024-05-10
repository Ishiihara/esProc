package com.scudata.dw;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

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
public class RowComTable extends ComTable {
	/**
	 * ���Ѿ����ڵ����
	 * @param file
	 * @param raf
	 * @param ctx
	 * @throws IOException
	 */
	public RowComTable(File file, RandomAccessFile raf, Context ctx) throws IOException {
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
	public RowComTable(File file, Context ctx) throws IOException {
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
	public RowComTable(File file) throws IOException {
		this.file = file;
		this.raf = new RandomAccessFile(file, "rw");
		readHeader();
	}
	
	/**
	 * �������
	 * @param file ���ļ�
	 * @param colNames ������
	 * @param distribute �ֲ�
	 * @param opt p������һ�ֶηֶ�
	 * @param ctx ������
	 * @throws IOException
	 */
	public RowComTable(File file, String []colNames, String distribute, String opt, Context ctx) throws IOException {
		this(file, colNames, distribute, opt, null, ctx);
	}
	
	/**
	 * �������
	 * @param file ���ļ�
	 * @param colNames ������
	 * @param distribute �ֲ�
	 * @param opt p������һ�ֶηֶ�
	 * @param blockSize �����С
	 * @param ctx ������
	 * @throws IOException
	 */
	public RowComTable(File file, String []colNames, String distribute, String opt, Integer blockSize, Context ctx) throws IOException {
		this(file, null, colNames, distribute, opt, blockSize, ctx);
	}
	
	public RowComTable(File file, RandomAccessFile raf, String []colNames, String distribute, String opt, Integer blockSize, Context ctx) throws IOException {
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
		ctx.addResource(this);
		
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
		
		baseTable = new RowPhyTable(this, colNames);
		structManager = new StructManager();
		
		// ����һ�ֶηֶ�
		if (opt != null && opt.indexOf('p') != -1) {
			baseTable.segmentCol = baseTable.getColName(0);
		}

		this.distribute = distribute;
		save();
	}
	
	/**
	 * ����src�Ľṹ����һ��������ļ�
	 * @param file �±���ļ�
	 * @param src ԭ���
	 * @throws IOException
	 */
	public RowComTable(File file, RowComTable src) throws IOException {
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
		try{
			baseTable = new RowPhyTable(this, null, (RowPhyTable)src.baseTable);
		} catch (Exception e) {
			if (raf != null) {
				raf.close();
			}
		}
		save();
	}
	
	public RowComTable() {
	}

	/**
	 * ���´�����ļ������������޸�
	 * @throws IOException
	 */
	protected void reopen() throws IOException {
		raf = new RandomAccessFile(file, "rw");
		Object syncObj = getSyncObject();
		synchronized(syncObj) {
			restoreTransaction();
			raf.seek(0);
			byte []bytes = new byte[32];
			raf.read(bytes);
			if (bytes[0] != 'r' || bytes[1] != 'q' || bytes[2] != 'd' || bytes[3] != 'w' || bytes[4] != 'g' || bytes[5] != 't' || bytes[6] != 'r') {
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
			reader.read(); // r
			
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
			
			baseTable = new RowPhyTable(this);
			baseTable.readExternal(reader);
		}
	}
	
	/**
	 * ��ȡ�ļ�ͷ
	 * �޸Ķ�дʱ��Ҫͬ���޸�reopen����
	 */
	protected void readHeader() throws IOException {
		Object syncObj = getSyncObject();
		synchronized(syncObj) {
			restoreTransaction();
			raf.seek(0);
			byte []bytes = new byte[32];
			raf.read(bytes);
			if (bytes[0] != 'r' || bytes[1] != 'q' || bytes[2] != 'd' || bytes[3] != 'w' || bytes[4] != 'g' || bytes[5] != 't' || bytes[6] != 'r') {
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
			reader.read(); // r
			
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
			
			baseTable = new RowPhyTable(this);
			baseTable.readExternal(reader);
		}
	}
	
	/**
	 * д�ļ�ͷ
	 */
	protected void writeHeader() throws IOException {
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
			writer.write('r');
			
			writer.writeInt32(blockSize);
			headerBlockLink.writeExternal(writer);
			
			reserve[0] = 3; // 1�������룬2���ӷֲ�������3����Ԥ����
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
			writer.write('r');
			
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
		int count = 1 + baseTable.tableList.size();
		
		long []blockInfo = new long[count * 8];
		int c = 0;
		
		blockInfo[c++] = baseTable.segmentBlockLink.firstBlockPos;
		blockInfo[c++] = baseTable.segmentBlockLink.lastBlockPos;
		blockInfo[c++] = baseTable.segmentBlockLink.freeIndex;
		blockInfo[c++] = baseTable.segmentBlockLink.blockCount;
		blockInfo[c++] = ((RowPhyTable)baseTable).dataBlockLink.firstBlockPos;
		blockInfo[c++] = ((RowPhyTable)baseTable).dataBlockLink.lastBlockPos;
		blockInfo[c++] = ((RowPhyTable)baseTable).dataBlockLink.freeIndex;
		blockInfo[c++] = ((RowPhyTable)baseTable).dataBlockLink.blockCount;
		
		for (PhyTable table : baseTable.tableList) {
			blockInfo[c++] = ((RowPhyTable)table).segmentBlockLink.firstBlockPos;
			blockInfo[c++] = ((RowPhyTable)table).segmentBlockLink.lastBlockPos;
			blockInfo[c++] = ((RowPhyTable)table).segmentBlockLink.freeIndex;
			blockInfo[c++] = ((RowPhyTable)table).segmentBlockLink.blockCount;
			blockInfo[c++] = ((RowPhyTable)table).dataBlockLink.firstBlockPos;
			blockInfo[c++] = ((RowPhyTable)table).dataBlockLink.lastBlockPos;
			blockInfo[c++] = ((RowPhyTable)table).dataBlockLink.freeIndex;
			blockInfo[c++] = ((RowPhyTable)table).dataBlockLink.blockCount;
		}
		return blockInfo;
	}

	public boolean isPureFormat() {
		return false;
	}
	
	// �����Ƿ�ѹ��������
	public boolean isCompress() {
		return false;
	}
}