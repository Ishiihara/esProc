package com.scudata.dw;

import java.io.IOException;

/**
 * �洢������
 * @author runqian
 *
 */
public class BlockLink {	
	private static final long EMPTY = 1;
	private transient IBlockStorage storage;
	long firstBlockPos = EMPTY; // �������׿�����ļ��е�λ��
	long lastBlockPos; // ������β�����ļ��е�λ�ã��ò����Ŀ��ܲ���
	int freeIndex = 0; // ����������ĩ���е��������ò����Ŀ��ܲ���
	int blockCount;
	
	/**
	 * 
	 * @param storage
	 */
	public BlockLink(IBlockStorage storage) {
		this.storage = storage;
	}
		
	public boolean isEmpty() {
		return firstBlockPos == EMPTY;
	}
	
	public void readExternal(BufferReader reader) throws IOException {
		firstBlockPos = reader.readLong40();
		lastBlockPos = reader.readLong40();
		freeIndex = reader.readInt32();
		blockCount = reader.readInt32();
	}
	
	public void writeExternal(BufferWriter writer) throws IOException {
		writer.writeLong40(firstBlockPos);
		writer.writeLong40(lastBlockPos);
		writer.writeInt32(freeIndex);
		writer.writeInt32(blockCount);
	}
	
	public IBlockStorage getBlockStorage() {
		return storage;
	}
	
	public long getFirstBlockPos() {
		return firstBlockPos;
	}
	
	public void setFirstBlockPos(long pos) {
		firstBlockPos = pos;
		lastBlockPos = pos;
		blockCount = 1;
	}
	
	public long getLastBlockPos() {
		return lastBlockPos;
	}
	
	public void appendBlock(long pos) {
		lastBlockPos = pos;
		blockCount++;
	}
	
	public int getFreeIndex() {
		return freeIndex;
	}

	public int getBlockCount() {
		return blockCount;
	}	
}
