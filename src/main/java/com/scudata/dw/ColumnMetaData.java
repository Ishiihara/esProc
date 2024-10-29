package com.scudata.dw;

import java.io.IOException;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.array.LongArray;
import com.scudata.dm.ObjectReader;
import com.scudata.dm.ObjectWriter;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;

// �ֶ�Ԫ����
public class ColumnMetaData {
	protected ComTable groupTable;
	private String colName; // ��������#��ͷ��ʾά�������а�#ȥ��
	private boolean isDim; // �Ƿ�ά�ֶε�һ���֣��������ֶ�
	private boolean isKey; // �Ƿ���������һ����
	
	// �ѷ���
	private int serialBytesLen = 0; // �������0��Ϊ�źż�
	
	private BlockLink dataBlockLink; // �п�������
	private BlockLink segmentBlockLink; // �ֶ���Ϣ�����������μ�¼ÿ���п������λ�ã������ά�ֶ��ټ�����Сֵ�����ֵ

	private transient BlockLinkWriter colWriter;
	private transient BlockLinkWriter segmentWriter;
	private transient ObjectWriter objectWriter;
	
	private Sequence dict;//�ֵ�汾4����
	private Object dictArray;//�ֵ�������ʽ
	private boolean hasMaxMinValues;//�汾4����
	private int dataType = DataBlockType.EMPTY;//���������� �汾5����
	
	public ColumnMetaData() {	
	}
	
	public ColumnMetaData(ColPhyTable table) {
		groupTable = table.groupTable;
		dataBlockLink = new BlockLink(groupTable);
		segmentBlockLink = new BlockLink(groupTable);
		dict = new Sequence();
	}
	
	public ColumnMetaData(ColPhyTable table, ColumnMetaData src) {
		this(table);
		colName = src.colName;
		isDim = src.isDim;
		isKey = src.isKey;
		hasMaxMinValues = src.hasMaxMinValues;
		serialBytesLen = src.serialBytesLen;
	}
	
	public ColumnMetaData(ColumnMetaData src) {
		groupTable = src.groupTable;
		dataBlockLink = src.dataBlockLink;
		segmentBlockLink = src.segmentBlockLink;
		colName = src.colName;
		isDim = src.isDim;
		isKey = src.isKey;
		serialBytesLen = src.serialBytesLen;
		hasMaxMinValues = src.hasMaxMinValues;
	}
	
	/**
	 * �����ж���
	 * @param table �����ı�
	 * @param name ����
	 * @param isDim �Ƿ�ά�ֶε�һ���֣��������ֶ�
	 * @param isKey �Ƿ���������һ����
	 */
	public ColumnMetaData(ColPhyTable table, String name, boolean isDim, boolean isKey) {
		this(table);
		
		this.colName = name;
		this.isDim = isDim;
		this.isKey = isKey;
		hasMaxMinValues = true;
	}
	
	public ColumnMetaData(ColPhyTable table, String name, int serialBytesLen) throws IOException {
		this(table);
		if (name.startsWith("#")) {
			colName = name.substring(1);
			isDim = true;
		} else {
			colName = name;
			isDim = false;
		}
		
		this.serialBytesLen = serialBytesLen;
		hasMaxMinValues = true;
	}
	
	public boolean isSerialBytes() {
		return serialBytesLen > 0;
	}
	
	public int getSerialBytesLen() {
		return serialBytesLen;
	}
	
	public String getColName() {
		return colName;
	}
	
	public void setColName(String colName) {
		this.colName = colName;
	}

	/**
	 * �����ֶ��Ƿ���ά��һ���֣��������ֶΣ�
	 * @return
	 */
	public boolean isDim() {
		return isDim;
	}
	
	/**
	 * �����ֶ��Ƿ���������һ����
	 * @return
	 */
	public boolean isKey() {
		return isKey;
	}
	
	void applyDataFirstBlock() throws IOException {
		if (dataBlockLink.isEmpty()) {
			dataBlockLink.setFirstBlockPos(groupTable.applyNewBlock());
		}
	}
	
	void applySegmentFirstBlock() throws IOException {
		if (dataBlockLink.isEmpty()) {
			segmentBlockLink.setFirstBlockPos(groupTable.applyNewBlock());
		}
	}
	
	public boolean isColumn(String name) {
		return colName.equals(name);
	}
	
	public void readExternal(BufferReader reader, byte version) throws IOException {
		colName = reader.readUTF();
		isDim = reader.readBoolean();
		serialBytesLen = reader.readInt();
		dataBlockLink.readExternal(reader);
		segmentBlockLink.readExternal(reader);
		
		if (version > 0) {
			isKey = reader.readBoolean();
		} else {
			isKey = isDim;
		}
		
		hasMaxMinValues = isDim;
		if (version > 3) {
			dict =  (Sequence) reader.readObject();
			if (dict == null) {
				dict = new Sequence();
			}
			hasMaxMinValues = true;
		}
		if (version > 4) {
			reader.readInt();//reserve
			dataType = reader.readInt();
			initDictArray();
		} else {
			dataType = DataBlockType.EMPTY;
		}
	}
	
	public void writeExternal(BufferWriter writer) throws IOException {
		writer.writeUTF(colName);
		writer.writeBoolean(isDim);
		writer.writeInt(serialBytesLen);
		dataBlockLink.writeExternal(writer);
		segmentBlockLink.writeExternal(writer);
		
		writer.writeBoolean(isKey); // �汾1����
		
		// �汾4����
		Sequence dict = this.dict;
		if (dict != null && dict.length() == 0) {
			dict = null;
		}
		writer.flush();
		writer.writeObject(dict);
		writer.flush();
		
		// �汾5����
		writer.writeInt(0);
		writer.writeInt(dataType);
	}
	
	public void prepareWrite() throws IOException {
		colWriter = new BlockLinkWriter(dataBlockLink, true);
		segmentWriter = new BlockLinkWriter(segmentBlockLink, true);
		objectWriter = new ObjectWriter(segmentWriter, groupTable.getBlockSize() - ComTable.POS_SIZE);
	}
	
	public void finishWrite() throws IOException {
		colWriter.finishWrite();
		colWriter = null;
		
		objectWriter.flush();
		segmentWriter.finishWrite();
		segmentWriter = null;
		objectWriter = null;
	}
	
	// ׷��һ���п飬ͬʱ��Ҫ�޸ķֶ���Ϣ������
	public void appendColBlock(byte []bytes) throws IOException {
		long pos = colWriter.writeDataBlock(bytes);
		objectWriter.writeLong40(pos);
	}
	
	// ׷��һ��ά�п飬ͬʱ��Ҫ�޸ķֶ���Ϣ������
	public void appendColBlock(byte []bytes, Object minValue, Object maxValue, Object startValue) throws IOException {
		long pos = colWriter.writeDataBlock(bytes);
		objectWriter.writeLong40(pos);
		objectWriter.writeObject(minValue);
		objectWriter.writeObject(maxValue);
		objectWriter.writeObject(startValue);
	}
	
	public void copyColBlock(BlockLinkReader colReader, ObjectReader segmentReader) throws IOException {
		byte[] data = colReader.readDataBlock0();
		long pos = colWriter.writeDataBlock0(data);
		
		segmentReader.readLong40();
		objectWriter.writeLong40(pos);
		if (hasMaxMinValues()) {
			objectWriter.writeObject(segmentReader.readObject());
			objectWriter.writeObject(segmentReader.readObject());
			objectWriter.writeObject(segmentReader.readObject());
		}
	}
	
	public void copyColBlock(BlockLinkReader colReader, ObjectReader segmentReader, 
			BufferWriter bufferWriter,byte[] dict) throws IOException {
		byte[] data = colReader.readDataBlock();
		if (data[0] == DataBlockType.DICT && data[1] == DataBlockType.DICT_PUBLIC) {
			int len = data.length - 2;
			bufferWriter.reset();
			bufferWriter.write(DataBlockType.DICT);
			bufferWriter.write(DataBlockType.DICT_PRIVATE);
			bufferWriter.write(dict);
			bufferWriter.write(data, 2, len);
			data = bufferWriter.finish();
		}
		
		long pos = colWriter.writeDataBlock(data);
		
		segmentReader.readLong40();
		objectWriter.writeLong40(pos);
		if (hasMaxMinValues()) {
			objectWriter.writeObject(segmentReader.readObject());
			objectWriter.writeObject(segmentReader.readObject());
			objectWriter.writeObject(segmentReader.readObject());
		}
	}
	
	public BlockLinkReader getColReader(boolean isLoadFirstBlock) {
		BlockLinkReader reader = new BlockLinkReader(dataBlockLink, serialBytesLen);
		reader.setDecompressBufferSize(4096);
		
		if (isLoadFirstBlock) {
			try {
				reader.loadFirstBlock();
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		reader.setDict(dict);
		return reader;
	}
	
	public ObjectReader getSegmentReader() {
		BlockLinkReader segmentReader = new BlockLinkReader(segmentBlockLink);
		try {
			segmentReader.loadFirstBlock();
			return new ObjectReader(segmentReader, groupTable.getBlockSize() - ComTable.POS_SIZE);
		} catch (IOException e) {
			segmentReader.close();
			throw new RQException(e.getMessage(), e);
		}
	}
	
	/**
	 * ȡ�п��������
	 * @return
	 */
	public BufferWriter getColDataBufferWriter() {
		return new BufferWriter(groupTable.getStructManager());
	}
	
	/**
	 * �ѵ�ǰcolumn���blockLink��Ϣ��䵽info������
	 * @param info
	 */
	public void getBlockLinkInfo(LongArray info) {
		info.add(segmentBlockLink.firstBlockPos);
		info.add(segmentBlockLink.lastBlockPos);
		info.add(segmentBlockLink.freeIndex);
		info.add(segmentBlockLink.blockCount);
		info.add(dataBlockLink.firstBlockPos);
		info.add(dataBlockLink.lastBlockPos);
		info.add(dataBlockLink.freeIndex);
		info.add(dataBlockLink.blockCount);
	}
	
	public BlockLink getSegmentBlockLink() {
		return segmentBlockLink;
	}
	
	public BlockLink getDataBlockLink() {
		return dataBlockLink;
	}

	public Sequence getDict() {
		return dict;
	}

	public void setDict(Sequence dict) {
		this.dict = dict;
	}

	public boolean hasMaxMinValues() {
		return hasMaxMinValues;
	}
	
	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public String getDataLen() {
		return DataBlockType.getTypeLen(dataType);
	}
	
	/**
	 * �����µ�����������
	 * @param dataType
	 * @param checkDataPure ��������Ƿ�
	 */
	public void adjustDataType(int newType, boolean checkDataPure) {
		if (checkDataPure) {
			if (!checkDataPure(newType)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("pdm.arrayTypeError", 
						DataBlockType.getTypeName(dataType), DataBlockType.getTypeName(newType)));
			}
		}
		
		int curType = dataType;
		if (curType == newType || newType == DataBlockType.NULL) {
			return;
		}
		
		if (curType == DataBlockType.STRING && newType == DataBlockType.STRING_ASSIC) {
			return;
		}
		if (newType == DataBlockType.STRING && curType == DataBlockType.STRING_ASSIC) {
			return;
		}
		
		switch (curType) {
		case DataBlockType.EMPTY:
			dataType = newType;
			break;
		case DataBlockType.OBJECT:
			break;
		case DataBlockType.RECORD:
		case DataBlockType.DATE:
		case DataBlockType.DECIMAL:
		case DataBlockType.STRING:
			dataType = DataBlockType.OBJECT;//����Ϊ��������
			break;
		case DataBlockType.SEQUENCE:
			if (newType != DataBlockType.TABLE)
				dataType = DataBlockType.OBJECT;
			break;
		case DataBlockType.TABLE:
			if (newType == DataBlockType.SEQUENCE)
				dataType = DataBlockType.SEQUENCE;//����ΪSEQUENCE
			else
				dataType = DataBlockType.OBJECT;
			break;
		case DataBlockType.INT:
		case DataBlockType.INT8:
		case DataBlockType.INT16:
		case DataBlockType.INT32:
			if ((newType & 0xF0) == DataBlockType.INT) {
				dataType = DataBlockType.INT;
			} else {
				dataType = DataBlockType.OBJECT;
			}
			break;
		case DataBlockType.LONG:
		case DataBlockType.LONG8:
		case DataBlockType.LONG16:
		case DataBlockType.LONG32:
		case DataBlockType.LONG64:
			if ((newType & 0xF0) == DataBlockType.LONG) {
				dataType = DataBlockType.LONG;
			} else {
				dataType = DataBlockType.OBJECT;
			}
			break;
		case DataBlockType.DOUBLE:
			if (newType == DataBlockType.DOUBLE64) {
				dataType = DataBlockType.DOUBLE;
			} else {
				dataType = DataBlockType.OBJECT;
			}
			break;
		case DataBlockType.DOUBLE64:
			if (newType == DataBlockType.DOUBLE) {
				dataType = DataBlockType.DOUBLE;
			} else {
				dataType = DataBlockType.OBJECT;
			}
			break;
		default:
			dataType = DataBlockType.OBJECT;
		}
	}

	/**
	 * �����µ����������Ƿ�
	 * @param newType
	 * @return true: ���ݴ�, false:���ݲ���
	 */
	private boolean checkDataPure(int newType) {
		int curType = dataType;
		if (curType == newType || newType == DataBlockType.NULL) {
			return true;
		}
		
		switch (curType) {
		case DataBlockType.EMPTY:
			break;
		case DataBlockType.OBJECT:
			break;
		case DataBlockType.RECORD:
		case DataBlockType.DATE:
		case DataBlockType.DECIMAL:
		case DataBlockType.STRING:
			return false;
		case DataBlockType.SEQUENCE:
			if (newType != DataBlockType.TABLE)
				return false;
		case DataBlockType.TABLE:
			if (newType == DataBlockType.SEQUENCE)
				return false;
			break;
		case DataBlockType.INT:
		case DataBlockType.INT8:
		case DataBlockType.INT16:
		case DataBlockType.INT32:
			if ((newType & 0xF0) == DataBlockType.INT) {
				break;
			} else {
				return false;
			}
		case DataBlockType.LONG:
		case DataBlockType.LONG8:
		case DataBlockType.LONG16:
		case DataBlockType.LONG32:
		case DataBlockType.LONG64:
			if ((newType & 0xF0) == DataBlockType.LONG) {
				break;
			} else {
				return false;
			}
		case DataBlockType.DOUBLE:
			if (newType == DataBlockType.DOUBLE64) {
				break;
			} else {
				return false;
			}
		case DataBlockType.DOUBLE64:
			if (newType == DataBlockType.DOUBLE) {
				break;
			} else {
				return false;
			}
		default:
			return false;
		}
		return true;
	}
	
	public Object getDictArray() {
		return dictArray;
	}
	
	public void initDictArray() {
		if (dict == null || dict.length() == 0) return;
		dictArray = DataBlockType.dictToArray(dict, this.dataType);
	}
}