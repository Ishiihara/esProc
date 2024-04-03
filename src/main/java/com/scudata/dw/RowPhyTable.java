package com.scudata.dw;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.LongArray;
import com.scudata.dm.ObjectReader;
import com.scudata.dm.ObjectWriter;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ConjxCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.cursor.MergesCursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.expression.Constant;
import com.scudata.expression.Expression;
import com.scudata.expression.Node;
import com.scudata.expression.UnknownSymbol;
import com.scudata.expression.operator.Equals;
import com.scudata.expression.operator.Greater;
import com.scudata.expression.operator.NotEquals;
import com.scudata.expression.operator.NotGreater;
import com.scudata.expression.operator.NotSmaller;
import com.scudata.expression.operator.Smaller;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �д������
 * @author runqian
 *
 */
public class RowPhyTable extends PhyTable {
	protected transient String []sortedColNames; // �����ֶ�(ά�ֶ�)
	private transient String []allSortedColNames; // �����ֶ�(ά�ֶ�) ������
	protected transient String []allKeyColNames; // key�ֶ�
	protected BlockLink dataBlockLink; // �п�������

	protected transient BlockLinkWriter colWriter;
	protected transient ObjectWriter objectWriter;
	protected int sortedColStartIndex;////�����ά�ֶθ���
	
	protected boolean[] isDim;//�Ƿ���ά�ֶ�
	protected boolean[] isKey;//�Ƿ���key�ֶ�
	protected int []serialBytesLen;//ÿ���е��źų��ȣ�0��ʾ�����ź�����
	
	/**
	 * �������л�
	 * @param groupTable
	 */
	public RowPhyTable(ComTable groupTable) {
		this.groupTable = groupTable;
		dataBlockLink = new BlockLink(groupTable);
		segmentBlockLink = new BlockLink(groupTable);
		this.modifyBlockLink1 = new BlockLink(groupTable);
		this.modifyBlockLink2 = new BlockLink(groupTable);
	}
	
	/**
	 * �������л�
	 * @param groupTable
	 * @param parent
	 */
	public RowPhyTable(ComTable groupTable, RowPhyTable parent) {
		this.groupTable = groupTable;
		this.parent = parent;
		dataBlockLink = new BlockLink(groupTable);
		segmentBlockLink = new BlockLink(groupTable);
		this.modifyBlockLink1 = new BlockLink(groupTable);
		this.modifyBlockLink2 = new BlockLink(groupTable);
	}
	
	/**
	 * �½�����
	 * @param groupTable
	 * @param colNames
	 * @throws IOException
	 */
	public RowPhyTable(ComTable groupTable, String []colNames) throws IOException {
		// ���������飬�����ļ���ʱ����Ӱ�쵽�����������Ĵ���
		String []tmp = new String[colNames.length];
		System.arraycopy(colNames, 0, tmp, 0, colNames.length);
		colNames = tmp;

		this.groupTable = groupTable;
		this.tableName = "";
		dataBlockLink = new BlockLink(groupTable);
		this.segmentBlockLink = new BlockLink(groupTable);
		this.modifyBlockLink1 = new BlockLink(groupTable);
		this.modifyBlockLink2 = new BlockLink(groupTable);
		
		int len = colNames.length;
		isDim = new boolean[len];
		isKey = new boolean[len];
		serialBytesLen = new int[len];
		
		int keyStart = -1; // ��������ʼ�ֶ�
		
		// ������ʼ�ֶ�ǰ����ֶ���Ϊ�������ֶ�
		for (int i = 0; i < len; ++i) {
			if (colNames[i].startsWith(KEY_PREFIX)) {
				keyStart = i;
				break;
			}
		}
		
		for (int i = 0; i < len; ++i) {
			if (colNames[i].startsWith(KEY_PREFIX)) {
				colNames[i] = colNames[i].substring(KEY_PREFIX.length());
				isDim[i] = true;
				isKey[i] = true;
			} else if (i < keyStart) {
				isDim[i] = true;
			}
		}

		this.colNames = colNames;
		init();
		
		if (sortedColNames == null) {
			hasPrimaryKey = false;
			isSorted = false;
		}
		tableList = new ArrayList<PhyTable>();
	}
	
	/**
	 * ����Ĵ���
	 * @param groupTable Ҫ������������
	 * @param colNames ������
	 * @param serialBytesLen �źų���
	 * @param tableName ������
	 * @param parent �������
	 * @throws IOException
	 */
	public RowPhyTable(ComTable groupTable, String []colNames, int []serialBytesLen,
			String tableName, RowPhyTable parent) throws IOException {
		// ���������飬�����ļ���ʱ����Ӱ�쵽�����������Ĵ���
		String []tmp = new String[colNames.length];
		System.arraycopy(colNames, 0, tmp, 0, colNames.length);
		colNames = tmp;

		this.groupTable = groupTable;
		this.tableName = tableName;
		this.parent = parent;
		dataBlockLink = new BlockLink(groupTable);
		this.segmentBlockLink = new BlockLink(groupTable);
		this.modifyBlockLink1 = new BlockLink(groupTable);
		this.modifyBlockLink2 = new BlockLink(groupTable);
		
		this.serialBytesLen = serialBytesLen;
		int len = colNames.length;
		isDim = new boolean[len];
		isKey = new boolean[len];
		for (int i = 0; i < len; ++i) {
			String name = colNames[i];
			if (name.startsWith("#")) {
				colNames[i] = name.substring(1);
				isDim[i] = isKey[i] = true;
			} else {
				isDim[i] = isKey[i] = false;
			}
		}
		this.colNames = colNames;
		init();
		
		if (getAllSortedColNames() == null) {
			hasPrimaryKey = false;
			isSorted = false;
		}
		
		if (parent != null) {
			//Ŀǰ����ֻ����һ��
			if (parent.parent != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("ds.dsNotMatch"));
			}
			
			PhyTable primaryTable = parent;
			String []primarySortedColNames = primaryTable.getSortedColNames();
			String []primaryColNames = primaryTable.getColNames();
			ArrayList<String> collist = new ArrayList<String>();
			for (String name : primaryColNames) {
				collist.add(name);
			}
			
			//�ֶβ����������ֶ��ظ�
			for (int i = 0; i < len; i++) {
				if (collist.contains(colNames[i])) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(colNames[i] + mm.getMessage("dw.fieldSameToPrimaryTable"));
				}
			}
			
			for (String name : primarySortedColNames) {
				collist.remove(name);
			}

			sortedColStartIndex = primarySortedColNames.length;
		}
		
		tableList = new ArrayList<PhyTable>();
	}
	
	/**
	 * ����src����һ��ͬ���Ļ���
	 * @param groupTable Ҫ������������
	 * @param parent �������
	 * @param src �ṩ�ṹ��Դ����
	 * @throws IOException
	 */
	public RowPhyTable(ComTable groupTable, RowPhyTable parent, RowPhyTable src) throws IOException {
		this.groupTable = groupTable;
		this.parent = parent;
		
		System.arraycopy(src.reserve, 0, reserve, 0, reserve.length);
		segmentCol = src.segmentCol;
		segmentSerialLen = src.segmentSerialLen;
		tableName = src.tableName;
		colNames = src.colNames;

		isDim = src.isDim;
		isKey = src.isKey;
		serialBytesLen = src.serialBytesLen;
		
		dataBlockLink = new BlockLink(groupTable);
		this.segmentBlockLink = new BlockLink(groupTable);
		this.modifyBlockLink1 = new BlockLink(groupTable);
		this.modifyBlockLink2 = new BlockLink(groupTable);
		
		init();
		
		if (getAllSortedColNames() == null) {
			hasPrimaryKey = false;
			isSorted = false;
		}
		
		tableList = new ArrayList<PhyTable>();
		for (PhyTable srcSub : src.tableList) {
			tableList.add(new RowPhyTable(groupTable, this, (RowPhyTable)srcSub));
		}
	}

	/**
	 * ��ʼ������ȡά�������Ȼ�����Ϣ
	 */
	protected void init() {
		String[] col = colNames;
		int dimCount = getDimCount();
		int keyCount = getKeyCount();
		int count = col.length;

		if (dimCount > 0) {
			sortedColNames = new String[dimCount];
			int j = 0;
			for (int i = 0; i < count; ++i) {
				if (isDim(col[i])) {
					sortedColNames[j++] = col[i];
				}
			}
		}
		
		if (keyCount > 0) {
			allKeyColNames = new String[keyCount];
			int j = 0;
			for (int i = 0; i < count; ++i) {
				if (isKey(col[i])) {
					allKeyColNames[j++] = col[i];
				}
			}
		}
		
		if (parent != null) {
			PhyTable primaryTable = parent;
			String []primarySortedColNames = primaryTable.getAllSortedColNames();
			sortedColStartIndex = primarySortedColNames.length;

			allColNames = new String[sortedColStartIndex + colNames.length];
			int i = 0;
			for (String colName : primarySortedColNames) {
				allColNames[i++] = colName;
			}
			for (String colName : colNames) {
				allColNames[i++] = colName;
			}
			
			allSortedColNames = new String[sortedColStartIndex + dimCount];
			i = 0;
			if (primarySortedColNames != null) {
				for (String colName : primarySortedColNames) {
					allSortedColNames[i++] = colName;
				}
			}
			if (sortedColNames != null) {
				for (String colName : sortedColNames) {
					allSortedColNames[i++] = colName;
				}
			}
			ds = new DataStruct(getAllColNames());
		} else {
			ds = new DataStruct(colNames);
		}
	}
	
	/**
	 * ��������ά������
	 * @return
	 */
	public String[] getSortedColNames() {
		return sortedColNames;
	}

	/**
	 * ��������key������
	 * @return
	 */
	public String[] getAllKeyColNames() {
		return allKeyColNames;
	}
	
	/**
	 * �����һ��ռ�
	 */
	protected void applyFirstBlock() throws IOException {
		if (segmentBlockLink.isEmpty()) {
			segmentBlockLink.setFirstBlockPos(groupTable.applyNewBlock());
			
			if (dataBlockLink.isEmpty()) {
				dataBlockLink.setFirstBlockPos(groupTable.applyNewBlock());
			}
		}
	}
	
	/**
	 * ׼��д����׷�ӡ�ɾ�����޸�����ǰ���á�
	 * ���ú��Թؼ���Ϣ���б��ݣ���ֹдһ�������ʱ��������
	 */
	protected void prepareAppend() throws IOException {
		applyFirstBlock();
		
		colWriter = new BlockLinkWriter(dataBlockLink, true);
		segmentWriter = new BlockLinkWriter(segmentBlockLink, true);
		objectWriter = new ObjectWriter(segmentWriter, groupTable.getBlockSize() - ComTable.POS_SIZE);
	}
	
	/**
	 * ����д
	 */
	protected void finishAppend() throws IOException {
		colWriter.finishWrite();
		colWriter = null;
		
		objectWriter.flush();
		segmentWriter.finishWrite();
		segmentWriter = null;
		objectWriter = null;
		
		groupTable.save();
		updateIndex();
	}
	
	/**
	 * ��ȡ��ͷ����
	 */
	public void readExternal(BufferReader reader) throws IOException {
		reader.read(reserve);
		tableName = reader.readUTF();
		colNames = reader.readStrings();
		dataBlockCount = reader.readInt32();
		totalRecordCount = reader.readLong40();
		segmentBlockLink.readExternal(reader);
		dataBlockLink.readExternal(reader);
		curModifyBlock = reader.readByte();
		modifyBlockLink1.readExternal(reader);
		modifyBlockLink2.readExternal(reader);
		
		int count = reader.readInt();
		colNames = new String[count];
		for (int i = 0; i < count; ++i) {
			colNames[i] = reader.readUTF();
		}
		serialBytesLen = new int[count];
		for (int i = 0; i < count; ++i) {
			serialBytesLen[i] = reader.readInt();
		}
		isDim = new boolean[count];
		for (int i = 0; i < count; ++i) {
			isDim[i] = reader.readBoolean();
		}
		
		if (reserve[0] > 0) {
			isKey = new boolean[count];
			for (int i = 0; i < count; ++i) {
				isKey[i] = reader.readBoolean();
			}
		}
		
		count = reader.readInt();
		if (count > 0) {
			maxValues = new Object[count];
			for (int i = 0; i < count; ++i) {
				maxValues[i] = reader.readObject();
			}
		}
		
		hasPrimaryKey = reader.readBoolean();
		isSorted = reader.readBoolean();
		
		indexNames = reader.readStrings();
		if (indexNames == null) {
			indexFields = null;
			indexValueFields = null;
		} else {
			int indexCount = indexNames.length;
			indexFields = new String[indexCount][];
			for (int i = 0; i < indexCount; i++) {
				indexFields[i] = reader.readStrings();
			}
			indexValueFields = new String[indexCount][];
			for (int i = 0; i < indexCount; i++) {
				indexValueFields[i] = reader.readStrings();
			}
		}
		
		if (groupTable.reserve[0] > 2) {
			cuboids = reader.readStrings();//�汾3����
		}
		segmentCol = (String)reader.readObject();
		segmentSerialLen = reader.readInt();
		init();
		
		count = reader.readInt();
		tableList = new ArrayList<PhyTable>(count);
		for (int i = 0; i < count; ++i) {
			PhyTable table = new RowPhyTable(groupTable, this);
			table.readExternal(reader);
			tableList.add(table);
		}
	}
	
	/**
	 * д����ͷ����
	 */
	public void writeExternal(BufferWriter writer) throws IOException {
		reserve[0] = 1;
		writer.write(reserve);
		writer.writeUTF(tableName);
		writer.writeStrings(colNames);
		writer.writeInt32(dataBlockCount);
		writer.writeLong40(totalRecordCount);
		segmentBlockLink.writeExternal(writer);
		dataBlockLink.writeExternal(writer);
		writer.writeByte(curModifyBlock);
		modifyBlockLink1.writeExternal(writer);
		modifyBlockLink2.writeExternal(writer);
		
		String []colNames = this.colNames;
		int count = colNames.length;
		writer.writeInt(count);
		for (int i = 0; i < count; ++i) {
			writer.writeUTF(colNames[i]);
		}
		for (int i = 0; i < count; ++i) {
			writer.writeInt(serialBytesLen[i]);
		}
		for (int i = 0; i < count; ++i) {
			writer.writeBoolean(isDim[i]);
		}
		
		//�汾1����
		for (int i = 0; i < count; ++i) {
			writer.writeBoolean(isKey[i]);
		}
		
		if (maxValues == null) {
			writer.writeInt(0);
		} else {
			writer.writeInt(maxValues.length);
			for (Object val : maxValues) {
				writer.writeObject(val);
			}
			
			writer.flush();
		}
		
		writer.writeBoolean(hasPrimaryKey);
		writer.writeBoolean(isSorted);
		
		writer.writeStrings(indexNames);
		if (indexNames != null) {
			for (int i = 0, indexCount = indexNames.length; i < indexCount; i++) {
				writer.writeStrings(indexFields[i]);
			}
			for (int i = 0, indexCount = indexNames.length; i < indexCount; i++) {
				writer.writeStrings(indexValueFields[i]);
			}
		}
		
		writer.writeStrings(cuboids);//�汾3����
		
		writer.writeObject(segmentCol);
		writer.flush();
		writer.writeInt(segmentSerialLen);
		
		ArrayList<PhyTable> tableList = this.tableList;
		count = tableList.size();
		writer.writeInt(count);
		for (int i = 0; i < count; ++i) {
			tableList.get(i).writeExternal(writer);
		}
	}
	
	/**
	 * ׷�Ӹ����һ������
	 * @param data
	 * @param start ��ʼ����
	 * @param recList ��������
	 * @throws IOException
	 */
	private void appendAttachedDataBlock(Sequence data, boolean []isMyCol, LongArray recList) throws IOException {
		Record r;
		int count = allColNames.length;
		boolean isDim[] = getDimIndex();
		Object []minValues = null;//һ�����Сάֵ
		Object []maxValues = null;//һ������άֵ
		
		if (sortedColNames != null) {
			minValues = new Object[count];
			maxValues = new Object[count];
		}
		
		RowBufferWriter bufferWriter= new RowBufferWriter(groupTable.getStructManager());
		long recNum = totalRecordCount;
		
		int end = data.length();
		for (int i = 1; i <= end; ++i) {
			//дα��
			bufferWriter.writeObject(++recNum);
			//д����
			bufferWriter.writeObject(recList.get(i - 1));
			
			r = (Record) data.get(i);
			Object[] vals = r.getFieldValues();
			//дһ����buffer
			for (int j = 0; j < count; j++) {
				if (!isMyCol[j]) continue;
				Object obj = vals[j];
				bufferWriter.writeObject(obj);
			}
			for (int j = 0; j < count; j++) {
				if (!isMyCol[j]) continue;
				Object obj = vals[j];
				if (isDim[j]) {
					if (Variant.compare(obj, maxValues[j], true) > 0)
						maxValues[j] = obj;
					if (i == 1)
						minValues[j] = obj;//��һ��Ҫ��ֵ����Ϊnull��ʾ��С
					if (Variant.compare(obj, minValues[j], true) < 0)
						minValues[j] = obj;
				}
			}
		}

		if (recList.size() == 0) {
			//����ǿտ飬��дһ��null
			bufferWriter.writeObject(null);
		}
		
		if (sortedColNames == null) {
			//�ύbuffer���п�
			long pos = colWriter.writeDataBuffer(bufferWriter.finish());
			//���·ֶ���Ϣ
			appendSegmentBlock(end);
			objectWriter.writeLong40(pos);
		} else {
			//�ύbuffer���п�
			long pos = colWriter.writeDataBuffer(bufferWriter.finish());
			//���·ֶ���Ϣ
			appendSegmentBlock(end);
			objectWriter.writeLong40(pos);
			for (int i = 0; i < count; ++i) {
				if (!isMyCol[i]) continue;
				if (isDim[i]) {
					objectWriter.writeObject(minValues[i]);
					objectWriter.writeObject(maxValues[i]);
				}
			}
		}
	}
	
	/**
	 * ��data���е�ָ����Χ������д��
	 * @param data ��������
	 * @param start ��ʼλ��
	 * @param end ����λ��
	 * @throws IOException
	 */
	protected void appendDataBlock(Sequence data, int start, int end) throws IOException {
		Record r;
		int count = colNames.length;
		boolean isDim[] = getDimIndex();
		Object []minValues = null;//һ�����Сάֵ
		Object []maxValues = null;//һ������άֵ

		if (sortedColNames != null) {
			minValues = new Object[count];
			maxValues = new Object[count];
		}

		RowBufferWriter bufferWriter= new RowBufferWriter(groupTable.getStructManager());
		long recNum = totalRecordCount;
		
		for (int i = start; i <= end; ++i) {
			r = (Record) data.get(i);
			Object[] vals = r.getFieldValues();
			//��һ����������д��buffer
			bufferWriter.writeObject(++recNum);//�д�Ҫ��дһ��α��
			for (int j = 0; j < count; j++) {
				Object obj = vals[j];
				bufferWriter.writeObject(obj);
				if (isDim[j]) {
					if (Variant.compare(obj, maxValues[j], true) > 0)
						maxValues[j] = obj;
					if (i == start)
						minValues[j] = obj;//��һ��Ҫ��ֵ����Ϊnull��ʾ��С
					if (Variant.compare(obj, minValues[j], true) < 0)
						minValues[j] = obj;
				}
			}
		}
		
		//д����ʱ��ѹ��
		if (sortedColNames == null) {
			//�ύbuffer���п�
			long pos = colWriter.writeDataBuffer(bufferWriter.finish());
			//���·ֶ���Ϣ
			appendSegmentBlock(end - start + 1);
			objectWriter.writeLong40(pos);
		} else {
			//�ύbuffer���п�
			long pos = colWriter.writeDataBuffer(bufferWriter.finish());
			//���·ֶ���Ϣ
			appendSegmentBlock(end - start + 1);
			objectWriter.writeLong40(pos);
			for (int i = 0; i < count; ++i) {
				if (isDim[i]) {
					objectWriter.writeObject(minValues[i]);
					objectWriter.writeObject(maxValues[i]);
				}
			}
		}
	}

	/**
	 * ���α������д��
	 * @param cursor
	 * @throws IOException
	 */
	private void appendNormal(ICursor cursor) throws IOException {
		Sequence data = cursor.fetch(MIN_BLOCK_RECORD_COUNT);
		while (data != null && data.length() > 0) {
			appendDataBlock(data, 1, data.length());
			data = cursor.fetch(MIN_BLOCK_RECORD_COUNT);
		}
	}
	
	/**
	 * ���α������д�� ������
	 * @param cursor
	 * @throws IOException
	 */
	private void appendAttached(ICursor cursor) throws IOException {
		PhyTable primaryTable = parent;
		int pBlockCount = primaryTable.getDataBlockCount();//����������ܿ���
		int curBlockCount = dataBlockCount;//Ҫ׷�ӵĿ�ʼ���
		int pkeyEndIndex = sortedColStartIndex;
		
		String []primaryTableKeys = primaryTable.getSortedColNames();
		ArrayList<String> primaryTableKeyList = new ArrayList<String>();
		for (String name : primaryTableKeys) {
			primaryTableKeyList.add(name);
		}
		String []colNames = getAllColNames();
		int fcount = colNames.length;
		boolean []isMyCol = new boolean[fcount];
		for (int i = 0; i < fcount; i++) {
			if (primaryTableKeyList.contains(colNames[i])) {
				isMyCol[i] = false;
			} else {
				isMyCol[i] = true;
			}
		}
		
		RowCursor cs;
		if (primaryTable.totalRecordCount == 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.baseTableNull"));
		}
		cs = (RowCursor) primaryTable.cursor(primaryTableKeys);
		cs.setSegment(curBlockCount, curBlockCount + 1);
		Sequence pkeyData = cs.fetch(ICursor.MAXSIZE);
		int pkeyIndex = 1;

		int pkeyDataLen = pkeyData.length();
		ComTableRecord curPkey = (ComTableRecord) pkeyData.get(1);
		Object []curPkeyVals = curPkey.getFieldValues();

		String []allSortedColNames = getAllSortedColNames();
		int sortedColCount = allSortedColNames.length;
		Object []tableMaxValues = this.maxValues;
		Object []lastValues = new Object[sortedColCount];//��һ��ά��ֵ
		
		LongArray guideCol = new LongArray(MIN_BLOCK_RECORD_COUNT);
		Sequence seq = new Sequence(MIN_BLOCK_RECORD_COUNT);
		Sequence data = cursor.fetch(ICursor.FETCHCOUNT);
		Record r;
		Object []vals = new Object[sortedColCount];
		int []findex = new int[sortedColCount];
		DataStruct ds = data.dataStruct();
		for (int f = 0; f < sortedColCount; ++f) {
			findex[f] = ds.getFieldIndex(allSortedColNames[f]);
		}
		
		while (data != null && data.length() > 0) {
			int len = data.length();
			for (int i = 1; i <= len; ++i) {
				r = (Record) data.get(i);
				for (int f = 0; f < sortedColCount; ++f) {
					Object obj = r.getNormalFieldValue(findex[f]);
					vals[f] = obj;
				}
				
				//�ұ�����������Ӧ�ļ�¼
				while (true) {
					int cmp = Variant.compareArrays(curPkeyVals, vals, pkeyEndIndex);
					if (cmp == 0) {
						break;
					} else if (cmp < 0) {
						pkeyIndex++;
						if (pkeyIndex > pkeyDataLen) {
							//ע�⣺��ʱ�п���seq��û�м�¼�����Ҫ׷��һ���տ�
							//������һ���˾��ύһ��
							appendAttachedDataBlock(seq, isMyCol, guideCol);
							seq.clear();
							guideCol = new LongArray(MIN_BLOCK_RECORD_COUNT);
							
							//ȡ��һ����������
							curBlockCount++;
							if (curBlockCount >= pBlockCount) {
								//����ȡ������ˣ������ﲻӦ�û������ݣ����쳣
								MessageManager mm = EngineMessage.get();
								throw new RQException(mm.getMessage("dw.appendNotMatch") + r.toString(null));
							}
							cs = (RowCursor) primaryTable.cursor(primaryTableKeys);
							cs.setSegment(curBlockCount, curBlockCount + 1);
							pkeyData = cs.fetch(ICursor.MAXSIZE);
							pkeyIndex = 1;
							pkeyDataLen = pkeyData.length();
						}
						curPkey = (ComTableRecord) pkeyData.get(pkeyIndex);
						curPkeyVals = curPkey.getFieldValues();
					} else if (cmp > 0) {
						//��Ӧ�ó��֣����쳣
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("dw.appendNotMatch") + r.toString(null));
					}
				}
				
				//���������ȷ��Ҫ׷��һ����
				guideCol.add(curPkey.getRecordSeq());//��ӵ���
				
				//��������������
				if (isSorted) {
					if (tableMaxValues != null) {
						int cmp = Variant.compareArrays(vals, tableMaxValues, sortedColCount);
						if (cmp < 0) {
							//����׷�ӵ����ݱ�������
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("dw.appendAttachedTable"));
						} else if (cmp == 0){
							if (hasPrimaryKey) hasPrimaryKey = false;
						} else {
							System.arraycopy(vals, 0, tableMaxValues, 0, sortedColCount);
						}
					} else {
						tableMaxValues = maxValues = new Object[sortedColCount];
						System.arraycopy(vals, 0, tableMaxValues, 0, sortedColCount);
					}
				}
				
				seq.add(r);//�������ݴ�				
				System.arraycopy(vals, 0, lastValues, 0, sortedColCount);//����һ��άֵ
			}
			data = cursor.fetch(ICursor.FETCHCOUNT);
		}
		
		//�������һ���п� (������ȡ������ˣ������ﻹ�еĻ����Ͳ�����)
		if (seq.length() > 0) {
			appendAttachedDataBlock(seq, isMyCol, guideCol);
		}
		
	}
	
	/**
	 * ���α������д����д��ʱ��Ҫ���зֶΡ�
	 * @param cursor
	 * @throws IOException
	 */
	private void appendSegment(ICursor cursor) throws IOException {
		int recCount = 0;
		int sortedColCount = sortedColNames.length;
		Object []tableMaxValues = this.maxValues;

		String []sortedColNames = this.sortedColNames;
		String segmentCol = groupTable.baseTable.getSegmentCol();
		int segmentSerialLen = groupTable.baseTable.getSegmentSerialLen();
		int segmentIndex = 0;

		for (int i = 0; i < sortedColCount; i++) {
			if (segmentCol.equals(sortedColNames[i])) {
				segmentIndex = i;
				break;
			}
		}
		int cmpLen = segmentIndex + 1;
		int serialBytesLen = getSerialBytesLen(segmentIndex);
		if (segmentSerialLen == 0 || segmentSerialLen > serialBytesLen) {
			segmentSerialLen = serialBytesLen;
		}
		Object []lastValues = new Object[cmpLen];//��һ��ά��ֵ
		Object []curValues = new Object[cmpLen];//��ǰ��ά��ֵ
		
		Sequence seq = new Sequence(MIN_BLOCK_RECORD_COUNT);
		Sequence data = cursor.fetch(ICursor.FETCHCOUNT);
		Record r;
		Object []vals = new Object[sortedColCount];
		int []findex = getSortedColIndex();
		
		while (data != null && data.length() > 0) {
			int len = data.length();
			for (int i = 1; i <= len; ++i) {
				r = (Record) data.get(i);
				for (int f = 0; f < sortedColCount; ++f) {
					vals[f] = r.getNormalFieldValue(findex[f]);
				}

				//�����ж��Ƿ�һ���п���
				if (recCount >= MIN_BLOCK_RECORD_COUNT){
					if (serialBytesLen < 1) {
						System.arraycopy(vals, 0, curValues, 0, cmpLen);
					} else {
						System.arraycopy(vals, 0, curValues, 0, segmentIndex);
						Long val;
						Object  obj = vals[segmentIndex];
						if (obj instanceof Integer) {
							val = (Integer)obj & 0xFFFFFFFFL;
						} else {
							val = (Long) obj;
						}
						curValues[segmentIndex] = val >>> (serialBytesLen - segmentSerialLen) * 8;
					}
					if (0 != Variant.compareArrays(lastValues, curValues, cmpLen)) {
						appendDataBlock(seq, 1, seq.length());
						seq.clear();
						recCount = 0;
					}
				}
				
				//��������������
				if (isSorted) {
					if (tableMaxValues != null) {
						int cmp = Variant.compareArrays(vals, tableMaxValues, sortedColCount);
						if (cmp < 0) {
							//hasPrimaryKey = false;//���ٴ�������
							isSorted = false;
							maxValues = null;
						} else if (cmp == 0){
							//if (hasPrimaryKey) hasPrimaryKey = false;//���ٴ�������
						} else {
							System.arraycopy(vals, 0, tableMaxValues, 0, sortedColCount);
						}
						if (tableList.size() > 0 && !hasPrimaryKey) {
							//���ڸ���ʱ������׷�ӵ����ݱ�������Ψһ
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("dw.appendPrimaryTable"));
						}
					} else {
						tableMaxValues = maxValues = new Object[sortedColCount];
						System.arraycopy(vals, 0, tableMaxValues, 0, sortedColCount);
					}
				}
				
				//�������ݴ�
				seq.add(r);
				
				if (serialBytesLen < 1) {
					System.arraycopy(vals, 0, lastValues, 0, cmpLen);
				} else {
					System.arraycopy(vals, 0, lastValues, 0, segmentIndex);//����һ��άֵ
					Long val;
					Object  obj = vals[segmentIndex];
					if (obj instanceof Integer) {
						val = (Integer)obj & 0xFFFFFFFFL;
					} else {
						val = (Long) obj;
					}
					lastValues[segmentIndex] = val >>> (serialBytesLen - segmentSerialLen) * 8;
				}
				recCount++;
			}
			data = cursor.fetch(ICursor.FETCHCOUNT);
		}
		
		//�������һ���п�
		if (seq.length() > 0) {
			appendDataBlock(seq, 1, seq.length());
		}
		
	}
	
	/**
	 * ���α������д����д��ʱ��Ҫ�ж������Ƿ��ά����
	 * @param cursor
	 * @throws IOException
	 */
	private void appendSorted(ICursor cursor) throws IOException {
		int recCount = 0;
		int sortedColCount = sortedColNames.length;
		Object []tableMaxValues = this.maxValues;
		Object []lastValues = new Object[sortedColCount];//��һ��ά��ֵ
		
		Sequence seq = new Sequence(MIN_BLOCK_RECORD_COUNT);
		Sequence data = cursor.fetch(ICursor.FETCHCOUNT);
		Record r;
		Object []vals = new Object[sortedColCount];
		int []findex = getSortedColIndex();

		while (data != null && data.length() > 0) {
			int len = data.length();
			for (int i = 1; i <= len; ++i) {
				r = (Record) data.get(i);
				for (int f = 0; f < sortedColCount; ++f) {
					vals[f] = r.getNormalFieldValue(findex[f]);
				}

				//�����ж��Ƿ�һ���п���
				if (recCount >= MAX_BLOCK_RECORD_COUNT) {
					//��ʱ�ύһ��
					appendDataBlock(seq, 1, MAX_BLOCK_RECORD_COUNT/2);
					seq = (Sequence) seq.get(MAX_BLOCK_RECORD_COUNT/2 + 1, seq.length() + 1);
					recCount = seq.length(); 
				} else if (recCount >= MIN_BLOCK_RECORD_COUNT){
					boolean doAppend = true;
					int segLen;
					if (sortedColCount > 1) {
						segLen = sortedColCount / 2;
					} else {
						segLen = 1;
					}
					for (int c = 0; c < segLen; c++) {
						int cmp = Variant.compare(lastValues[c], vals[c], true);
						if (cmp == 0) {
							doAppend = false;
							break;
						}
					}
					if (doAppend) {
						appendDataBlock(seq, 1, seq.length());
						seq.clear();
						recCount = 0;
					}
				}
				
				//��������������
				if (isSorted) {
					if (tableMaxValues != null) {
						int cmp = Variant.compareArrays(vals, tableMaxValues, sortedColCount);
						if (cmp < 0) {
							//hasPrimaryKey = false;//���ٴ�������
							isSorted = false;
							maxValues = null;
						} else if (cmp == 0){
							//if (hasPrimaryKey) hasPrimaryKey = false;//���ٴ�������
						} else {
							System.arraycopy(vals, 0, tableMaxValues, 0, sortedColCount);
						}
						if (tableList.size() > 0 && !hasPrimaryKey) {
							//���ڸ���ʱ������׷�ӵ����ݱ�������Ψһ
							MessageManager mm = EngineMessage.get();
							throw new RQException(mm.getMessage("dw.appendPrimaryTable"));
						}
					} else {
						tableMaxValues = maxValues = new Object[sortedColCount];
						System.arraycopy(vals, 0, tableMaxValues, 0, sortedColCount);
					}
				}
				
				seq.add(r);//�������ݴ�				
				System.arraycopy(vals, 0, lastValues, 0, sortedColCount);//����һ��άֵ				
				recCount++;
			}
			data = cursor.fetch(ICursor.FETCHCOUNT);
		}
		
		//�������һ���п�
		if (seq.length() > 0) {
			appendDataBlock(seq, 1, seq.length());
		}
		
	}
	
	/**
	 * ׷������
	 */
	public void append(ICursor cursor) throws IOException {
		// ���û��ά�ֶ���ȡGroupTable.MIN_BLOCK_RECORD_COUNT����¼
		// ���������3��ά�ֶ�d1��d2��d3������ά�ֶε�ֵȡ������MIN_BLOCK_RECORD_COUNT����¼
		// ���[d1,d2,d3]��������Ҫ��[d1,d2]ֵ��ͬ�ĸ������������֮��Ҫ��[d1,d2,d3]ֵ��ͬ�Ĳ���������
		// �����ͬ�ĳ�����MAX_BLOCK_RECORD_COUNT������MAX_BLOCK_RECORD_COUNT / 2��Ϊһ��
		// ��ÿһ�е�����д��BufferWriterȻ�����finish�õ��ֽ����飬�ٵ���compressѹ�����ݣ����д��ColumnMetaData
		// ��ά�ֶ�ʱҪ����maxValues��hasPrimaryKey������Ա�����hasPrimaryKeyΪfalse���ٸ���
		if (cursor == null) {
			return;
		}

		getGroupTable().checkWritable();
		Sequence data = cursor.peek(MIN_BLOCK_RECORD_COUNT);		
		if (data == null || data.length() <= 0) {
			return;
		}
		
		//�жϽṹƥ��
		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		String []colNames = getAllColNames();
		int count = colNames.length;
		for (int i = 0; i < count; i++) {
			if (!ds.getFieldName(i).equals(colNames[i])) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.dsNotMatch"));
			}
		}
		
		//����α����ݲ���1��
		if (data.length() < MIN_BLOCK_RECORD_COUNT) {
			if (appendCache == null) {
				appendCache = data;
			} else {
				appendCache.addAll(data);
			}
			data = null;
			cursor.close();
			if (appendCache.length() >= MIN_BLOCK_RECORD_COUNT) {
				appendCache();
			}
			return;
		}
		
		//����л�������
		if (appendCache != null) {
			ICursor []cursorArray = new ICursor[2];
			cursorArray[0] = new MemoryCursor(appendCache);
			cursorArray[1] = cursor;
			cursor = new ConjxCursor(cursorArray);
			appendCache = null;
		}
		
		// ׼��д����
		prepareAppend();
		
		if (parent != null) {
			parent.appendCache();
			appendAttached(cursor);
		} else if (sortedColNames == null) {
			appendNormal(cursor);
		} else if (groupTable.baseTable.getSegmentCol() == null) {
			appendSorted(cursor);
		} else {
			appendSegment(cursor);
		}
		
		// ����д���ݣ����浽�ļ�
		finishAppend();
	}

	protected void appendSegmentBlock(int recordCount) throws IOException {
		dataBlockCount++;
		totalRecordCount += recordCount;
		objectWriter.writeInt32(recordCount);
	}
	
	/**
	 * ȡ�ֶι������ȼ�
	 * @param name
	 * @return
	 */
	int getColumnFilterPriority(String name) {
		if (sortedColNames != null) {
			int len = sortedColNames.length;
			for (int i = 0; i < len; ++i) {
				if (sortedColNames[i] == name) {
					return i;
				}
			}
			
			return len;
		} else {
			return 0;
		}
	}
	
	/**
	 * �������������α�
	 */
	public ICursor cursor() {
		getGroupTable().checkReadable();
		ICursor cs = new RowCursor(this);
		PhyTable tmd = getSupplementTable(false);
		if (tmd == null) {
			return cs;
		} else {
			ICursor cs2 = tmd.cursor();
			return merge(cs, cs2);
		}
	}
	
	public ICursor cursor(String []fields) {
		getGroupTable().checkReadable();
		ICursor cs = new RowCursor(this, fields);
		PhyTable tmd = getSupplementTable(false);
		if (tmd == null) {
			return cs;
		} else {
			ICursor cs2 = tmd.cursor(fields);
			return merge(cs, cs2);
		}
	}
	
	public ICursor cursor(String []fields, Expression exp, Context ctx) {
		getGroupTable().checkReadable();
		ICursor cs = new RowCursor(this, fields, exp, ctx);
		PhyTable tmd = getSupplementTable(false);
		if (tmd == null) {
			return cs;
		} else {
			ICursor cs2 = tmd.cursor(fields, exp, ctx);
			return merge(cs, cs2);
		}
	}

	public ICursor cursor(Expression[] exps, String[] fields, Expression filter, Context ctx) {
		getGroupTable().checkReadable();
		ICursor cs = new RowCursor(this, null, filter, exps, fields, ctx);
		PhyTable tmd = getSupplementTable(false);
		if (tmd == null) {
			return cs;
		} else {
			ICursor cs2 = tmd.cursor(exps, fields, filter, null, null, null, null, ctx);
			return merge(cs, cs2);
		}
	}

	public ICursor cursor(String []fields, Expression filter, Context ctx, int segSeq, int segCount) {
		getGroupTable().checkReadable();
		return cursor(null, fields, filter, ctx, segSeq, segCount);
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, Context ctx, int segSeq, int segCount) {
		getGroupTable().checkReadable();
		if (filter != null) {
			filter = filter.newExpression(ctx); // �ֶβ��ж�ȡʱ��Ҫ���Ʊ��ʽ��ͬһ�����ʽ��֧�ֲ�������
		}
		
		String []fetchFields;
		if (exps != null) {
			int colCount = exps.length;
			fetchFields = new String[colCount];
			for (int i = 0; i < colCount; ++i) {
				if (exps[i] == null) {
					exps[i] = Expression.NULL;
				}
				
				if (exps[i].getHome() instanceof UnknownSymbol) {
					fetchFields[i] = exps[i].getIdentifierName();
				}
			}
		} else {
			fetchFields = fields;
			fields = null;
		}
		
		RowCursor cursor = new RowCursor(this, fetchFields, filter, exps, fields, ctx);
		if (segCount < 2) {
			return cursor;
		}
		
		int startBlock = 0;
		int endBlock = -1;
		int avg = dataBlockCount / segCount;
		
		if (avg < 1) {
			// ÿ�β���һ��
			if (segSeq <= dataBlockCount) {
				startBlock = segSeq - 1;
				endBlock = segSeq;
			}
		} else {
			if (segSeq > 1) {
				endBlock = segSeq * avg;
				startBlock = endBlock - avg;
				
				// ʣ��Ŀ�����ÿ�ζ�һ��
				int mod = dataBlockCount % segCount;
				int n = mod - (segCount - segSeq);
				if (n > 0) {
					endBlock += n;
					startBlock += n - 1;
				}
			} else {
				endBlock = avg;
			}
		}

		cursor.setSegment(startBlock, endBlock);
		return cursor;
	
	}
	public ICursor cursor(String []fields, Expression exp, MultipathCursors mcs, Context ctx) {
		getGroupTable().checkReadable();
		//�д治֧�ֶ�·
		MessageManager mm = EngineMessage.get();
		throw new RQException("cursor" + mm.getMessage("dw.needMCursor"));
	}
	
	public ICursor cursor(String []fields, Expression filter, String []fkNames, Sequence []codes, String []opts, Context ctx) {
		if (fkNames == null) {
			return cursor(fields, filter, ctx);
		} else {
			throw new RQException("K:T param is unimplemented in row group table!");
		}
	}

	public ICursor cursor(String []fields, Expression filter, String []fkNames, Sequence []codes, 
			String []opts, Context ctx, int pathCount) {
		if (fkNames == null) {
			return cursor(fields, filter, ctx, pathCount);
		} else {
			throw new RQException("K:T param is unimplemented in row group table!");
		}
	}
	
	public ICursor cursor(String []fields, Expression filter, String []fkNames, Sequence []codes, 
			String []opts, Context ctx, int pathSeq, int pathCount) {
		if (fkNames == null) {
			return cursor(fields, filter, ctx, pathSeq, pathCount);
		} else {
			throw new RQException("K:T param is unimplemented in row group table!");
		}
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, String []fkNames, Sequence []codes,
			String []opts, String opt, Context ctx) {
		if (fkNames == null) {
			return cursor(exps, fields, filter, ctx);
		} else {
			throw new RQException("K:T param is unimplemented in row group table!");
		}
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, String []fkNames, Sequence []codes,
			String []opts, Context ctx, int pathCount) {
		if (fkNames == null) {
			return cursor(exps, fields, filter, ctx, pathCount);
		} else {
			throw new RQException("K:T param is unimplemented in row group table!");
		}
	}
	
	public ICursor cursor(Expression []exps, String []fields, Expression filter, String []fkNames, Sequence []codes,
			String []opts, Context ctx, int pathSeq, int pathCount) {
		if (fkNames == null) {
			return cursor(exps, fields, filter, ctx, pathSeq, pathCount);
		} else {
			throw new RQException("K:T param is unimplemented in row group table!");
		}
	}
	
	public ICursor cursor(String []fields, Expression exp, String []fkNames, Sequence []codes, 
			String []opts, MultipathCursors mcs, String opt, Context ctx) {
		if (fkNames == null) {
			return cursor(fields, exp, mcs, ctx);
		} else {
			throw new RQException("K:T param is unimplemented in row group table!");
		}
	}

	/**
	 * �в��ļ�ʱ�����ݸ���
	 * @param stmd
	 * @param data
	 * @param opt
	 * @return
	 * @throws IOException
	 */
	private Sequence update(PhyTable stmd, Sequence data, String opt) throws IOException {
		boolean isUpdate = true, isInsert = true;
		Sequence result = null;
		if (opt != null) {
			if (opt.indexOf('i') != -1) isUpdate = false;
			if (opt.indexOf('u') != -1) {
				if (!isUpdate) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(opt + mm.getMessage("engine.optConflict"));
				}
				
				isInsert = false;
			}
			
			if (opt.indexOf('n') != -1) result = new Sequence();
		}
		
		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		if (!ds.isCompatible(this.ds)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.dsNotMatch"));
		}
		
		// �Ը������ݽ�������
		data.sortFields(getAllSortedColNames());
		appendCache();
		
		String[] columns = getAllSortedColNames();
		int keyCount = columns.length;
		int []keyIndex = new int[keyCount];
		for (int k = 0; k < keyCount; ++k) {
			keyIndex[k] = ds.getFieldIndex(columns[k]);
			if (keyIndex[k] < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(columns[k] + mm.getMessage("ds.fieldNotExist"));
			}
		}
		
		boolean isPrimaryTable = parent == null;
		int len = data.length();
		long []seqs = new long[len + 1];
		int []block = new int[len + 1];//�Ƿ���һ���εĵײ�insert(�ӱ�)
		long []recNum = null;
		int []temp = new int[1];
		
		if (isPrimaryTable) {
			RowRecordSeqSearcher searcher = new RowRecordSeqSearcher(this);
			if (keyCount == 1) {
				int k = keyIndex[0];
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					seqs[i] = searcher.findNext(r.getFieldValue(k));
				}
			} else {
				Object []keyValues = new Object[keyCount];
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					for (int k = 0; k < keyCount; ++k) {
						keyValues[k] = r.getFieldValue(keyIndex[k]);
					}
					
					seqs[i] = searcher.findNext(keyValues);
				}
			}
		} else {
			recNum  = new long[len + 1];//�ӱ��Ӧ�������α�ţ�0��ʾ��������
			RowPhyTable baseTable = (RowPhyTable) this.groupTable.baseTable;
			RowRecordSeqSearcher baseSearcher = new RowRecordSeqSearcher(baseTable);
			RowRecordSeqSearcher2 searcher = new RowRecordSeqSearcher2(this);
			if (keyCount == 1) {
				int k = keyIndex[0];
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					seqs[i] = searcher.findNext(r.getFieldValue(k), temp);
					block[i] = temp[0];
					if (seqs[i] < 0) {
						//����ǲ��룬Ҫ�ж�һ���Ƿ������������
						long seq = baseSearcher.findNext(r.getFieldValue(k));
						if (seq > 0) {
							recNum[i] = seq;
						} else {
							if (baseSearcher.isEnd()) {
								//�ӱ��������ݱ���������
								MessageManager mm = EngineMessage.get();
								throw new RQException(r.toString(null) + mm.getMessage("grouptable.invalidData"));	
							}
							recNum[i] = 0;//�����������������ᴦ��
						}
					} else {
						recNum[i] = searcher.getRecNum();
					}
				}
			} else {
				Object []keyValues = new Object[keyCount];
				int baseKeyCount = sortedColStartIndex;
				Object []baseKeyValues = new Object[baseKeyCount];
				
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					for (int k = 0; k < keyCount; ++k) {
						keyValues[k] = r.getFieldValue(keyIndex[k]);
						if (k < baseKeyCount) {
							baseKeyValues[k] = keyValues[k]; 
						}
					}
					
					seqs[i] = searcher.findNext(keyValues, temp);
					block[i] = temp[0];
					if (seqs[i] < 0 || block[i] > 0) {
						//����ǲ��룬Ҫ�ж�һ���Ƿ������������
						long seq = baseSearcher.findNext(baseKeyValues);
						if (seq > 0) {
							recNum[i] = seq;
						} else {
							if (baseSearcher.isEnd()) {
								//�ӱ��������ݱ���������
								MessageManager mm = EngineMessage.get();
								throw new RQException(r.toString(null) + mm.getMessage("grouptable.invalidData"));	
							}
							recNum[i] = 0;//�����������������ᴦ��
						}
					} else {
						recNum[i] = searcher.getRecNum();
					}
				}
			}
		}
		
		// ��Ҫ��������ĵ���append׷��
		Sequence append = new Sequence();
		ArrayList<ModifyRecord> modifyRecords = getModifyRecords();
		boolean needUpdateSubTable = false;
		
		if (modifyRecords == null) {
			modifyRecords = new ArrayList<ModifyRecord>(len);
			this.modifyRecords = modifyRecords;
			for (int i = 1; i <= len; ++i) {
				Record sr = (Record)data.getMem(i);
				if (seqs[i] > 0) {
					if (isUpdate) {
						ModifyRecord r = new ModifyRecord(seqs[i], ModifyRecord.STATE_UPDATE, sr);
						modifyRecords.add(r);
						if (result != null) {
							result.add(sr);
						}
					}
				} else {
					append.add(sr);
				}
			}
		} else {
			int srcLen = modifyRecords.size();
			ArrayList<ModifyRecord> tmp = new ArrayList<ModifyRecord>(len + srcLen);
			int s = 0;
			int t = 1;
			
			while (s < srcLen && t <= len) {
				ModifyRecord mr = modifyRecords.get(s);
				long seq1 = mr.getRecordSeq();
				long seq2 = seqs[t];
				if (seq2 > 0) {
					if (seq1 < seq2) {
						s++;
						tmp.add(mr);
					} else if (seq1 == seq2) {
						if (mr.getState() == ModifyRecord.STATE_INSERT) {
							s++;
							tmp.add(mr);
						} else {
							if ((mr.getState() == ModifyRecord.STATE_UPDATE && isUpdate) || 
									(mr.getState() == ModifyRecord.STATE_DELETE && isInsert)) {
								// ״̬����update
								Record sr = (Record)data.getMem(t);
								mr.setRecord(sr, ModifyRecord.STATE_UPDATE);
								if (result != null) {
									result.add(sr);
								}
							}

							s++;
							t++;
							tmp.add(mr);
						}
					} else {
						if (isUpdate) {
							Record sr = (Record)data.getMem(t);
							mr = new ModifyRecord(seq2, ModifyRecord.STATE_UPDATE, sr);
							tmp.add(mr);
							
							if (result != null) {
								result.add(sr);
							}
						}
						
						t++;
					}
				} else {
					seq2 = -seq2;
					if (seq1 < seq2) {
						s++;
						tmp.add(mr);
					} else if (seq1 == seq2) {
						if (mr.getState() == ModifyRecord.STATE_INSERT) {
							int cmp = mr.getRecord().compare((Record)data.getMem(t), keyIndex);
							if (cmp < 0) {
								s++;
								tmp.add(mr);
							} else if (cmp == 0) {
								if (isUpdate) {
									Record sr = (Record)data.getMem(t);
									mr.setRecord(sr);
									if (result != null) {
										result.add(sr);
									}
								}
								
								tmp.add(mr);
								s++;
								t++;
							} else {
								append.add(data.getMem(t));
								t++;
							}
						} else {
							append.add(data.getMem(t));
							t++;
						}
					} else {
						append.add(data.getMem(t));
						t++;
					}
				}
			}
			
			for (; s < srcLen; ++s) {
				tmp.add(modifyRecords.get(s));
			}
			
			for (; t <= len; ++t) {
				Record sr = (Record)data.getMem(t);
				if (seqs[t] > 0) {
					if (isUpdate) {
						ModifyRecord r = new ModifyRecord(seqs[t], ModifyRecord.STATE_UPDATE, sr);
						tmp.add(r);
						if (result != null) {
							result.add(sr);
						}
					}
				} else {
					append.add(sr);
				}
			}
			
			this.modifyRecords = tmp;
			if (srcLen != tmp.size()) {
				needUpdateSubTable = true;
			}
		}
		
		if (!isPrimaryTable) {
			//�ӱ������Ҫ�����������޸�
			update(parent.getModifyRecords());
			
			for (ModifyRecord r : modifyRecords) {
				if (r.getState() == ModifyRecord.STATE_INSERT) {
					if (r.getParentRecordSeq() == 0) {
						this.modifyRecords = null;
						this.modifyRecords = getModifyRecords();
						//�ӱ��������ݱ���������
						MessageManager mm = EngineMessage.get();
						throw new RQException(r.getRecord().toString(null) + mm.getMessage("grouptable.invalidData"));
					}
				}
			}
			
		}
		
		saveModifyRecords();
		
		if (isPrimaryTable && needUpdateSubTable) {
			//������insert���ͱ�����������ӱ���
			ArrayList<PhyTable> tableList = getTableList();
			for (int i = 0, size = tableList.size(); i < size; ++i) {
				RowPhyTable t = ((RowPhyTable)tableList.get(i));
				boolean needSave = t.update(modifyRecords);
				if (needSave) {
					t.saveModifyRecords();
				}
			}
		}
		
		if (append.length() > 0) {
			Sequence seq = stmd.update(append, opt);
			if (result != null) {
				result.addAll(seq);
			}
		}
		
		groupTable.save();
		return result;
	}
	
	/**
	 * ����
	 */
	public Sequence update(Sequence data, String opt) throws IOException {
		if (!hasPrimaryKey) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.lessKey"));
		}
		
		ComTable groupTable = getGroupTable();
		groupTable.checkWritable();
		PhyTable tmd = getSupplementTable(false);
		if (tmd != null) {
			return update(tmd, data, opt);
		}
		
		boolean isInsert = true,isUpdate = true;
		Sequence result = null;
		if (opt != null) {
			if (opt.indexOf('i') != -1) isUpdate = false;
			if (opt.indexOf('u') != -1) {
				if (!isUpdate) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(opt + mm.getMessage("engine.optConflict"));
				}
				
				isInsert = false;
			}
			
			if (opt.indexOf('n') != -1) result = new Sequence();
		}
		
		long totalRecordCount = this.totalRecordCount;
		if (totalRecordCount == 0) {
			if (isInsert) {
				ICursor cursor = new MemoryCursor(data);
				append(cursor);
				appendCache();
				if (result != null) {
					result.addAll(data);
				}
			}
			
			return result;
		}
		
		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		if (!ds.isCompatible(this.ds)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.dsNotMatch"));
		}
		
		// �Ը������ݽ�������
		data.sortFields(getAllSortedColNames());
		appendCache();
		
		String[] columns = getAllSortedColNames();
		int keyCount = columns.length;
		int []keyIndex = new int[keyCount];
		for (int k = 0; k < keyCount; ++k) {
			keyIndex[k] = ds.getFieldIndex(columns[k]);
			if (keyIndex[k] < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(columns[k] + mm.getMessage("ds.fieldNotExist"));
			}
		}
		
		boolean isPrimaryTable = parent == null;
		int len = data.length();
		long []seqs = new long[len + 1];
		int []block = new int[len + 1];//�Ƿ���һ���εĵײ�insert(�ӱ�)
		long []recNum = null;
		int []temp = new int[1];
		
		if (isPrimaryTable) {
			RowRecordSeqSearcher searcher = new RowRecordSeqSearcher(this);
			
			if (keyCount == 1) {
				int k = keyIndex[0];
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					seqs[i] = searcher.findNext(r.getFieldValue(k));
				}
			} else {
				Object []keyValues = new Object[keyCount];
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					for (int k = 0; k < keyCount; ++k) {
						keyValues[k] = r.getFieldValue(keyIndex[k]);
					}
					
					seqs[i] = searcher.findNext(keyValues);
				}
			}
		} else {
			recNum  = new long[len + 1];//�ӱ��Ӧ�������α�ţ�0��ʾ��������
			RowPhyTable baseTable = (RowPhyTable) this.groupTable.baseTable;
			RowRecordSeqSearcher baseSearcher = new RowRecordSeqSearcher(baseTable);
			RowRecordSeqSearcher2 searcher = new RowRecordSeqSearcher2(this);
			if (keyCount == 1) {
				int k = keyIndex[0];
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					seqs[i] = searcher.findNext(r.getFieldValue(k), temp);
					block[i] = temp[0];
					if (seqs[i] < 0) {
						//����ǲ��룬Ҫ�ж�һ���Ƿ������������
						long seq = baseSearcher.findNext(r.getFieldValue(k));
						if (seq > 0) {
							recNum[i] = seq;
						} else {
							if (baseSearcher.isEnd()) {
								//�ӱ��������ݱ���������
								MessageManager mm = EngineMessage.get();
								throw new RQException(r.toString(null) + mm.getMessage("grouptable.invalidData"));	
							}
							recNum[i] = 0;//�����������������ᴦ��
						}
					} else {
						recNum[i] = searcher.getRecNum();
					}
				}
			} else {
				Object []keyValues = new Object[keyCount];
				int baseKeyCount = sortedColStartIndex;
				Object []baseKeyValues = new Object[baseKeyCount];
				
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					for (int k = 0; k < keyCount; ++k) {
						keyValues[k] = r.getFieldValue(keyIndex[k]);
						if (k < baseKeyCount) {
							baseKeyValues[k] = keyValues[k]; 
						}
					}
					
					seqs[i] = searcher.findNext(keyValues, temp);
					block[i] = temp[0];
					if (seqs[i] < 0 || block[i] > 0) {
						//����ǲ��룬Ҫ�ж�һ���Ƿ������������
						long seq = baseSearcher.findNext(baseKeyValues);
						if (seq > 0) {
							recNum[i] = seq;
						} else {
							if (baseSearcher.isEnd()) {
								//�ӱ��������ݱ���������
								MessageManager mm = EngineMessage.get();
								throw new RQException(r.toString(null) + mm.getMessage("grouptable.invalidData"));	
							}
							recNum[i] = 0;//�����������������ᴦ��
						}
					} else {
						recNum[i] = searcher.getRecNum();
					}
				}
			}
		}
		
		// ��Ҫ��������ĵ���append׷��
		Sequence append = new Sequence();
		ArrayList<ModifyRecord> modifyRecords = getModifyRecords();
		boolean needUpdateSubTable = false;
		
		if (modifyRecords == null) {
			modifyRecords = new ArrayList<ModifyRecord>(len);
			this.modifyRecords = modifyRecords;
			for (int i = 1; i <= len; ++i) {
				Record sr = (Record)data.getMem(i);
				if (seqs[i] > 0) {
					if (isUpdate) {
						ModifyRecord r = new ModifyRecord(seqs[i], ModifyRecord.STATE_UPDATE, sr);
						if (!isPrimaryTable) {
							r.setParentRecordSeq(recNum[i]);
						}
						modifyRecords.add(r);
						if (result != null) {
							result.add(sr);
						}
					}
				} else if (isInsert) {
					long seq = -seqs[i];
					if (seq <= totalRecordCount || block[i] > 0) {
						ModifyRecord r = new ModifyRecord(seq, ModifyRecord.STATE_INSERT, sr);
						r.setBlock(block[i]);
						//������ӱ�insert Ҫ����parentRecordSeq����Ϊ�ӱ�insert�Ŀ���ָ����������
						//����������Ϊָ������α�ţ���������������޸�
						if (!isPrimaryTable) {
							r.setParentRecordSeq(recNum[i]);
						}
						modifyRecords.add(r);
					} else {
						append.add(sr);
					}
					
					if (result != null) {
						result.add(sr);
					}
				}
			}
		} else {
			int srcLen = modifyRecords.size();
			ArrayList<ModifyRecord> tmp = new ArrayList<ModifyRecord>(len + srcLen);
			int s = 0;
			int t = 1;
			
			while (s < srcLen && t <= len) {
				ModifyRecord mr = modifyRecords.get(s);
				long seq1 = mr.getRecordSeq();
				long seq2 = seqs[t];
				if (seq2 > 0) {
					if (seq1 < seq2) {
						s++;
						tmp.add(mr);
					} else if (seq1 == seq2) {
						if (mr.getState() == ModifyRecord.STATE_INSERT) {
							s++;
							tmp.add(mr);
						} else {
							if ((mr.getState() == ModifyRecord.STATE_UPDATE && isUpdate) || 
									(mr.getState() == ModifyRecord.STATE_DELETE && isInsert)) {
								// ״̬����update
								Record sr = (Record)data.getMem(t);
								mr.setRecord(sr, ModifyRecord.STATE_UPDATE);
								if (!isPrimaryTable) {
									mr.setParentRecordSeq(recNum[t]);
								}
								if (result != null) {
									result.add(sr);
								}
							}

							s++;
							t++;
							tmp.add(mr);
						}
					} else {
						if (isUpdate) {
							Record sr = (Record)data.getMem(t);
							mr = new ModifyRecord(seq2, ModifyRecord.STATE_UPDATE, sr);
							if (!isPrimaryTable) {
								mr.setParentRecordSeq(recNum[t]);
							}
							tmp.add(mr);
							
							if (result != null) {
								result.add(sr);
							}
						}
						
						t++;
					}
				} else {
					seq2 = -seq2;
					if (seq1 < seq2) {
						s++;
						tmp.add(mr);
					} else if (seq1 == seq2) {
						if (mr.getState() == ModifyRecord.STATE_INSERT) {
							int cmp = mr.getRecord().compare((Record)data.getMem(t), keyIndex);
							if (cmp < 0) {
								s++;
								tmp.add(mr);
							} else if (cmp == 0) {
								if (isUpdate) {
									Record sr = (Record)data.getMem(t);
									mr.setRecord(sr);
									if (result != null) {
										result.add(sr);
									}
								}
								
								tmp.add(mr);
								s++;
								t++;
							} else {
								if (isInsert) {
									Record sr = (Record)data.getMem(t);
									mr = new ModifyRecord(seq2, ModifyRecord.STATE_INSERT, sr);
									mr.setBlock(block[t]);
									//������ӱ�insert Ҫ����parentRecordSeq����Ϊ�ӱ�insert�Ŀ���ָ����������
									//����������Ϊָ������α�ţ���������������޸�
									if (!isPrimaryTable) {
										mr.setParentRecordSeq(recNum[t]);
									}
									modifyRecords.add(mr);
									tmp.add(mr);
									if (result != null) {
										result.add(sr);
									}
								}
								
								t++;
							}
						} else {
							if (isInsert) {
								Record sr = (Record)data.getMem(t);
								mr = new ModifyRecord(seq2, ModifyRecord.STATE_INSERT, sr);
								mr.setBlock(block[t]);
								//������ӱ�insert Ҫ����parentRecordSeq����Ϊ�ӱ�insert�Ŀ���ָ����������
								//����������Ϊָ������α�ţ���������������޸�
								if (!isPrimaryTable) {
									mr.setParentRecordSeq(recNum[t]);
								}
								modifyRecords.add(mr);
								tmp.add(mr);
								if (result != null) {
									result.add(sr);
								}
							}
							
							t++;
						}
					} else {
						if (isInsert) {
							Record sr = (Record)data.getMem(t);
							mr = new ModifyRecord(seq2, ModifyRecord.STATE_INSERT, sr);
							mr.setBlock(block[t]);
							//������ӱ�insert Ҫ����parentRecordSeq����Ϊ�ӱ�insert�Ŀ���ָ����������
							//����������Ϊָ������α�ţ���������������޸�
							if (!isPrimaryTable) {
								mr.setParentRecordSeq(recNum[t]);
							}
							modifyRecords.add(mr);
							tmp.add(mr);
							if (result != null) {
								result.add(sr);
							}
						}
						
						t++;
					}
				}
			}
			
			for (; s < srcLen; ++s) {
				tmp.add(modifyRecords.get(s));
			}
			
			for (; t <= len; ++t) {
				Record sr = (Record)data.getMem(t);
				if (seqs[t] > 0) {
					if (isUpdate) {
						ModifyRecord r = new ModifyRecord(seqs[t], ModifyRecord.STATE_UPDATE, sr);
						tmp.add(r);
						if (result != null) {
							result.add(sr);
						}
					}
				} else if (isInsert) {
					long seq = -seqs[t];
					if (seq <= totalRecordCount) {
						ModifyRecord r = new ModifyRecord(seq, ModifyRecord.STATE_INSERT, sr);
						r.setBlock(block[t]);
						//������ӱ�insert Ҫ����parentRecordSeq����Ϊ�ӱ�insert�Ŀ���ָ����������
						//����������Ϊָ������α�ţ���������������޸�
						if (!isPrimaryTable) {
							r.setParentRecordSeq(recNum[t]);
						}
						modifyRecords.add(r);
						tmp.add(r);
					} else {
						append.add(sr);
					}
					
					if (result != null) {
						result.add(sr);
					}
				}
			}
			
			this.modifyRecords = tmp;
			if (srcLen != tmp.size()) {
				needUpdateSubTable = true;
			}
		}
		
		if (!isPrimaryTable) {
			//�ӱ������Ҫ�����������޸�
			update(parent.getModifyRecords());
			
			for (ModifyRecord r : modifyRecords) {
				if (r.getState() == ModifyRecord.STATE_INSERT) {
					if (r.getParentRecordSeq() == 0) {
						this.modifyRecords = null;
						this.modifyRecords = getModifyRecords();
						//�ӱ��������ݱ���������
						MessageManager mm = EngineMessage.get();
						throw new RQException(r.getRecord().toString(null) + mm.getMessage("grouptable.invalidData"));
					}
				}
			}
			
		}
		
		saveModifyRecords();
		
		if (isPrimaryTable && needUpdateSubTable) {
			//������insert���ͱ�����������ӱ���
			ArrayList<PhyTable> tableList = getTableList();
			for (int i = 0, size = tableList.size(); i < size; ++i) {
				RowPhyTable t = ((RowPhyTable)tableList.get(i));
				boolean needSave = t.update(modifyRecords);
				if (needSave) {
					t.saveModifyRecords();
				}
			}
		}
		
		if (append.length() > 0) {
			ICursor cursor = new MemoryCursor(append);
			append(cursor);
		} else {
			groupTable.save();
		}
		
		return result;
	}
	
	public Sequence delete(Sequence data, String opt) throws IOException {
		if (!hasPrimaryKey) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.lessKey"));
		}
		
		ComTable groupTable = getGroupTable();
		groupTable.checkWritable();
		
		Sequence result1 = null;
		PhyTable tmd = getSupplementTable(false);
		if (tmd != null) {
			// �в��ļ�ʱ��ɾ�����ļ��е����ݣ����ļ��в����ڵ�����Դ�ļ���ɾ��
			result1 = tmd.delete(data, "n");
			data = (Sequence) data.diff(result1, false);
		}
		
		appendCache();
		boolean nopt = opt != null && opt.indexOf('n') != -1;
		long totalRecordCount = this.totalRecordCount;
		if (totalRecordCount == 0 || data == null || data.length() == 0) {
			return nopt ? result1 : null;
		}
		
		Sequence result = null;
		if (nopt) {
			result = new Sequence();
		}
		
		boolean deleteByBaseKey = false;//ֻ�����ڲ�ɾ���ӱ����������ʱ������@n
		if (opt != null && opt.indexOf('s') != -1) {
			deleteByBaseKey = true;
		}
		
		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
				
		String[] columns = getAllSortedColNames();
		int keyCount = columns.length;
		if (deleteByBaseKey) {
			keyCount = sortedColStartIndex;
		}
		int []keyIndex = new int[keyCount];
		for (int k = 0; k < keyCount; ++k) {
			keyIndex[k] = ds.getFieldIndex(columns[k]);
			if (keyIndex[k] < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(columns[k] + mm.getMessage("ds.fieldNotExist"));
			}
		}
		
		boolean isPrimaryTable = parent == null;
		if (deleteByBaseKey) {
			data.sortFields(parent.getSortedColNames());
		} else {
			data.sortFields(getAllSortedColNames());
		}
		int len = data.length();
		long []seqs = new long[len + 1];
		int temp[] = new int[1];
		LongArray seqList = null;
		Sequence seqListData = null;
		if (deleteByBaseKey) {
			seqList = new LongArray(len * 10);
			seqList.add(0);
			seqListData = new Sequence(len);
		}
		
		if (isPrimaryTable) {
			RowRecordSeqSearcher searcher = new RowRecordSeqSearcher(this);
			
			if (keyCount == 1) {
				int k = keyIndex[0];
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					seqs[i] = searcher.findNext(r.getFieldValue(k));
				}
			} else {
				Object []keyValues = new Object[keyCount];
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)data.getMem(i);
					for (int k = 0; k < keyCount; ++k) {
						keyValues[k] = r.getFieldValue(keyIndex[k]);
					}
					
					seqs[i] = searcher.findNext(keyValues);
				}
			}
		} else {
			RowRecordSeqSearcher2 searcher = new RowRecordSeqSearcher2(this);
			
			Object []keyValues = new Object[keyCount];
			int baseKeyCount = sortedColStartIndex;
			Object []baseKeyValues = new Object[baseKeyCount];
			
			for (int i = 1; i <= len;) {
				Record r = (Record)data.getMem(i);
				for (int k = 0; k < keyCount; ++k) {
					keyValues[k] = r.getFieldValue(keyIndex[k]);
					if (k < baseKeyCount) {
						baseKeyValues[k] = keyValues[k]; 
					}
				}
				
				if (deleteByBaseKey) {
					long s = searcher.findNext(keyValues, keyCount);
					if (s <= 0) {
						i++;//���Ҳ���ʱ��++
					} else {
						seqList.add(s);
						seqListData.add(r);
					}

				} else {
					seqs[i] = searcher.findNext(keyValues, temp);
					i++;
				}
				
			}
		}
		
		if (deleteByBaseKey) {
			len = seqList.size() - 1;
			if (0 == len) {
				return result;
			}
			seqs = seqList.toArray();
			data = seqListData;
		}
		
		ArrayList<ModifyRecord> modifyRecords = getModifyRecords();
		boolean modified = true;
		
		if (modifyRecords == null) {
			modifyRecords = new ArrayList<ModifyRecord>(len);
			for (int i = 1; i <= len; ++i) {
				if (seqs[i] > 0) {
					ModifyRecord r = new ModifyRecord(seqs[i]);
					modifyRecords.add(r);
					
					if (result != null) {
						result.add(data.getMem(i));
					}
				}
			}
			
			if (modifyRecords.size() > 0) {
				this.modifyRecords = modifyRecords;
			} else {
				modified = false;
			}
		} else {
			int srcLen = modifyRecords.size();
			ArrayList<ModifyRecord> tmp = new ArrayList<ModifyRecord>(len + srcLen);
			int s = 0;
			int t = 1;
			
			while (s < srcLen && t <= len) {
				ModifyRecord mr = modifyRecords.get(s);
				long seq1 = mr.getRecordSeq();
				long seq2 = seqs[t];
				if (seq2 > 0) {
					if (seq1 < seq2) {
						s++;
					} else if (seq1 == seq2) {
						if (mr.getState() == ModifyRecord.STATE_INSERT) {
							int cmp = mr.getRecord().compare((Record)data.getMem(t), keyIndex);
							if (cmp < 0) {
								tmp.add(mr);
							} else if (cmp == 0) {
								if (result != null) {
									result.add(data.getMem(t));
								}
							} else {
								if (result != null) {
									result.add(data.getMem(t));
								}

								ModifyRecord r = new ModifyRecord(seqs[t]);
								tmp.add(r);
								tmp.add(mr);
								t++;
							}
							s++;
							continue;
						} else {
							if (result != null && mr.getState() == ModifyRecord.STATE_UPDATE) {
								result.add(data.getMem(t));
							}
							
							mr.setDelete();
							s++;
							t++;
						}
					} else {
						mr = new ModifyRecord(seq2);
						if (result != null) {
							result.add(data.getMem(t));
						}
	
						t++;
					}
					
					tmp.add(mr);
				} else {
					seq2 = -seq2;
					if (seq1 < seq2) {
						s++;
						tmp.add(mr);
					} else if (seq1 == seq2) {
						if (mr.getState() == ModifyRecord.STATE_INSERT) {
							int cmp = mr.getRecord().compare((Record)data.getMem(t), keyIndex);
							if (cmp < 0) {
								s++;
								tmp.add(mr);
							} else if (cmp == 0) {
								if (result != null) {
									result.add(data.getMem(t));
								}
	
								s++;
								t++;
							} else {
								t++;
							}
						} else {
							s++;
							t++;
							tmp.add(mr);
						}
					} else {
						t++;
					}
				}
			}
			
			for (; s < srcLen; ++s) {
				tmp.add(modifyRecords.get(s));
			}
			
			for (; t <= len; ++t) {
				if (seqs[t] > 0) {
					if (result != null) {
						result.add(data.getMem(t));
					}

					ModifyRecord r = new ModifyRecord(seqs[t]);
					tmp.add(r);
				}
			}
			
			this.modifyRecords = tmp;
		}
		
		if (modified) {
			if (isPrimaryTable) {
				//������delete���ͱ���ͬ��delete�ӱ�
				ArrayList<PhyTable> tableList = getTableList();
				int size = tableList.size();
				for (int i = 0; i < size; ++i) {
					RowPhyTable t = ((RowPhyTable)tableList.get(i));
					t.delete(data, "s");//ɾ���ӱ�����
					t.delete(data);//ɾ���ӱ���
				}
				
				//������ɾ����������λ�û�仯����Ҫͬ���ӱ���
				for (int i = 0; i < size; ++i) {
					RowPhyTable t = ((RowPhyTable)tableList.get(i));
					t.update(this.modifyRecords);
					t.saveModifyRecords();
				}
			}
			
			if (!deleteByBaseKey) {
				saveModifyRecords();
			}
		}
		
		if (!deleteByBaseKey) {
			groupTable.save();
		}
		
		if (nopt) {
			result.addAll(result1);
		}
		
		return result;
	}
	
	/**
	 * ���ݻ���Ĳ�����ͬ�������Լ��Ĳ���
	 * @param baseModifyRecords ����Ĳ�����¼
	 * @return
	 * @throws IOException
	 */
	private boolean update(ArrayList<ModifyRecord> baseModifyRecords) throws IOException {
		getGroupTable().checkWritable();
		
		if (baseModifyRecords == null) return false;
		ArrayList<ModifyRecord> modifyRecords = getModifyRecords();
		if (modifyRecords == null) {
			return false;
		}
		//int fieldsLen = columns.length;
		int len = sortedColStartIndex;
		int []index = new int[len];
		int []findex = getSortedColIndex();
		for (int i = 0; i < len; ++i) {
			index[i] = findex[i];
		}
		
		boolean find = false;
		int parentRecordSeq = 0;
		for (ModifyRecord mr : baseModifyRecords) {
			parentRecordSeq++;
			Record mrec = mr.getRecord();
			
			if (mr.getState() != ModifyRecord.STATE_DELETE) {
				for (ModifyRecord r : modifyRecords) {
					if (r.getState() == ModifyRecord.STATE_DELETE) {
						continue;
					}
					
					Record rec = r.getRecord();
					int cmp = rec.compare(mrec, index);
					if (cmp == 0) {
						if (mr.getState() == ModifyRecord.STATE_INSERT) {
							r.setParentRecordSeq(-parentRecordSeq);
						} else {
							r.setParentRecordSeq(mr.getRecordSeq());
						}
						find = true;
					}
				}
			}
		}
		return find;
	}
	
	/**
	 * ����dataɾ���ӱ�Ĳ���
	 * @param data
	 * @return
	 * @throws IOException
	 */
	private boolean delete(Sequence data) throws IOException {
		ArrayList<ModifyRecord> tmp = new ArrayList<ModifyRecord>();
		ArrayList<ModifyRecord> srcModifyRecords = new ArrayList<ModifyRecord>();
		ArrayList<ModifyRecord> modifyRecords = getModifyRecords();
		if (modifyRecords == null) {
			return false;
		}
		tmp.addAll(modifyRecords);
		
		int len = sortedColStartIndex;
		int []index = new int[len];
		int []findex = getSortedColIndex();
		for (int i = 0; i < len; ++i) {
			index[i] = findex[i];
		}
		
		len = data.length();
		boolean delete = false;
		for (int i = 1; i <= len; i++) {
			Record mrec = (Record) data.get(i);
			srcModifyRecords.clear();
			srcModifyRecords.addAll(tmp);
			tmp.clear();
			for (ModifyRecord r : srcModifyRecords) {
				int state = r.getState();
				if (state == ModifyRecord.STATE_UPDATE) {
					Record rec = r.getRecord();
					int cmp = rec.compare(mrec, index);
					if (cmp == 0) {
						r.setDelete();
						r.setRecord(null);
						delete = true;
					}
					tmp.add(r);
				} else if (state == ModifyRecord.STATE_INSERT) {
					Record rec = r.getRecord();
					int cmp = rec.compare(mrec, index);
					if (cmp == 0) {
						delete = true;
					} else {
						tmp.add(r);
					}
				} else {
					tmp.add(r);
				}
			}
		}
		
		if (delete) {
			this.modifyRecords = tmp;
		}
		return delete;
	}
	
	/**
	 * ����reader����ѡ�Ƿ����һ�飩
	 * @param isLoadFirstBlock
	 * @return
	 */
	public BlockLinkReader getRowReader(boolean isLoadFirstBlock) {
		BlockLinkReader reader = new BlockLinkReader(dataBlockLink);
		reader.setDecompressBufferSize(4096);
		
		if (isLoadFirstBlock) {
			try {
				reader.loadFirstBlock();
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		return reader;
	}

	public ObjectReader getSegmentObjectReader() {
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
	 * ���ά�ֶε�λ��
	 * @return
	 */
	public boolean[] getDimIndex() {
		String[] col = getAllColNames();
		boolean []isDim = new boolean[col.length];

		for (int i = 0, len = col.length; i < len; i++) {
			if (isDim(col[i])) {
				isDim[i] = true;
			} else {
				isDim[i] = false;
			}
		}
		return isDim;
	}

	/**
	 * �ж��Ƿ���ά
	 * @param colName
	 * @return
	 */
	protected boolean isDim(String colName) {
		boolean isDim[] = this.isDim;
		String colNames[] = this.colNames;
		int len = colNames.length;
		
		for (int i = 0; i < len; ++i) {
			if (colName.equals(colNames[i])) {
				return isDim[i];
			}
		}
		return false;
	}
	
	/**
	 * �ж��Ƿ���key
	 * @param colName
	 * @return
	 */
	protected boolean isKey(String colName) {
		boolean isKey[] = this.isDim;
		String colNames[] = this.colNames;
		int len = colNames.length;
		
		for (int i = 0; i < len; ++i) {
			if (colName.equals(colNames[i])) {
				return isKey[i];
			}
		}
		return false;
	}
	
	/**
	 * �õ�ά�ֶθ���
	 * @return
	 */
	protected int getDimCount() {
		boolean isDim[] = this.isDim; 
		int len = isDim.length;
		int count = 0;
		
		for (int i = 0; i < len; ++i) {
			if (isDim[i]) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * �õ�ά�ֶθ���
	 * @return
	 */
	protected int getKeyCount() {
		boolean isKey[] = this.isKey; 
		int len = isKey.length;
		int count = 0;
		
		for (int i = 0; i < len; ++i) {
			if (isKey[i]) {
				count++;
			}
		}
		return count;
	}
	
	protected int getSerialBytesLen(int index) {
		return serialBytesLen[index];
	}
	
	public int[] getSerialBytesLen() {
		return serialBytesLen;
	}
	
	public String[] getAllSortedColNames() {
		if (parent == null) return getSortedColNames();
		return allSortedColNames;
	}
	
	/**
	 * ���������ֶ����ƣ����������ӱ������ֶ�
	 * @return
	 */
	public String[] getTotalColNames() {
		if (parent == null) return colNames;
		int len = getTotalColCount();
		int baseColCount = parent.colNames.length;
		String[] names = new String[len];
		System.arraycopy(parent.colNames, 0, names, 0, baseColCount);
		System.arraycopy(colNames, 0, names, baseColCount, colNames.length);		
		return names;
	}
	
	/**
	 * ���ֶ���
	 * @return
	 */
	public int getTotalColCount() {
		if (parent == null) return colNames.length;
		return parent.colNames.length + colNames.length;
	}
	
	/**
	 * �����ֶε��źų��ȣ����������ӱ������ֶΣ�
	 * @return
	 */
	public int[] getTotalSerialBytesLen() {
		if (parent == null) return serialBytesLen;
		int len = getTotalColCount();
		int []baseSerialBytesLen = parent.getSerialBytesLen();
		int baseColCount = baseSerialBytesLen.length;
		int[] serialBytesLen = new int[len];
		System.arraycopy(baseSerialBytesLen, 0, serialBytesLen, 0, baseColCount);
		System.arraycopy(this.serialBytesLen, 0, serialBytesLen, baseColCount, this.serialBytesLen.length);		
		return serialBytesLen;
	}
	
	/**
	 * �д治֧�ֶ�·
	 * @param fields
	 * @param filter
	 * @param ctx
	 * @param pathCount
	 * @return
	 */
	public ICursor cursor(String[] fields, Expression filter, Context ctx, int pathCount) {
		MessageManager mm = EngineMessage.get();
		throw new RQException("cursor" + mm.getMessage("dw.needMCursor"));
	}
	
	public ICursor cursor(Expression[] exps, String[] fields, Expression filter, Context ctx, int pathCount) {
		return cursor(exps, fields, filter, null, null, null, pathCount, null, ctx);
	}
	
	public Table finds(Sequence values) throws IOException {
		return finds(values, null);
	}

	public Table finds(Sequence values, String[] selFields) throws IOException {
		getGroupTable().checkReadable();
		
		if (!hasPrimaryKey()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("dw.lessKey"));
		}
		
		String []keys = getAllSortedColNames();
		int keyCount = keys.length;
		Expression exp;
		Sequence keyValues = values;
		
		if (keyCount == 1) {
			exp = new Expression("null.contain(" + keys[0] + ")"); 
			Object obj = values.getMem(1);
			int valueLen = values.length();
			if (valueLen == 0) {
				return null;
			}
			if (obj instanceof Sequence) {
				Sequence seq = (Sequence)obj;
				int dimCount = seq.length();
				if (dimCount > keyCount) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("find" + mm.getMessage("function.invalidParam"));
				}
				
				keyValues = new Sequence();
				for (int i = 1; i <= valueLen; ++i) {
					seq = (Sequence)values.getMem(i);
					keyValues.add(seq.getMem(1));
				}
			}
		} else {
			String str = "null.contain([";
			for (int i = 0; i < keyCount; i++) {
				str += keys[i];
				if (i != keyCount - 1) {
					str += ",";
				}
			}
			str += "])";
			exp = new Expression(str); 
		}

		Context ctx = new Context();
		exp.getHome().setLeft(new Constant(keyValues));
		Sequence result = cursor(selFields, exp, ctx).fetch();
		if (result == null) return null;
		Table table = new Table(result.dataStruct());
		table.addAll(result);
		return table;
	}

	public int getFirstBlockFromModifyRecord() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long resetByBlock(int block) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	long[] checkDim(String field, Node node, Context ctx) {
		if (!isDim(field) || (parent != null)) {
			return null;
		}
		
		int operator = 0;
		if (node instanceof Equals) {
			operator = IFilter.EQUAL;
		} else if (node instanceof Greater) {
			operator = IFilter.GREATER;
		} else if (node instanceof NotSmaller) {
			operator = IFilter.GREATER_EQUAL;
		} else if (node instanceof Smaller) {
			operator = IFilter.LESS;
		} else if (node instanceof NotGreater) {
			operator = IFilter.LESS_EQUAL;
		} else if (node instanceof NotEquals) {
			operator = IFilter.NOT_EQUAL;
		} else {
			return null;
		}
		
		Object value;
		if (node.getRight() instanceof UnknownSymbol) {
			value = node.getLeft().calculate(ctx);
		} else {
			value = node.getRight().calculate(ctx);
		}
		
		int blockCount = dataBlockCount;
		ColumnFilter filter = new ColumnFilter(field, 0, operator, value);
		LongArray intervals = new LongArray();
		ObjectReader segmentReader = getSegmentObjectReader();
		int index = -1;
		String []colNames = this.colNames;
		for (int i = 0; i < colNames.length; ++i) {
			if (field.equals(colNames[i])) {
				index = i;
				break;
			}
		}
		
		try {
			boolean flag = false;
			Object maxValue = null, minValue = null;
			int keyCount = getAllSortedColNamesLength();
			
			for (int i = 0; i < blockCount; ++i) {
				int recordCount = segmentReader.readInt32();
				long pos = segmentReader.readLong40();
				if (flag) {
					intervals.add(pos - 1);
				}
				flag = false;
				for (int k = 0; k < keyCount; ++k) {
					if (k == index) {
						minValue = segmentReader.readObject();
						maxValue = segmentReader.readObject();
					} else {
						segmentReader.skipObject();
						segmentReader.skipObject();
					}
				}
				if (filter.match(minValue, maxValue) && recordCount != 1) {
					intervals.add(pos);
					flag = true;
				}
			}
			if (flag) {
				intervals.add(getGroupTable().fileSize);
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
		if (intervals.size() == 0) {
			return null;
		}
		return intervals.toArray();
	}

	// @m �Թ鲢��ʽ׷�ӣ��ݲ�֧���и�������
	public void append(ICursor cursor, String opt) throws IOException {
		if (isSorted && opt != null) {
			if (opt.indexOf('a') != -1) {
				RowPhyTable ctmd = (RowPhyTable)getSupplementTable(true);
				ctmd.mergeAppend(cursor, opt);
			} else if (opt.indexOf('m') != -1) {
				mergeAppend(cursor, opt);
			} else {
				append(cursor);
				if (opt.indexOf('i') != -1) {
					appendCache();
				}
			}
		} else {
			append(cursor);
		}
	}
	
	private void mergeAppend(ICursor cursor, String opt) throws IOException {
		// ��֧�ִ���������鲢׷��
		if (!isSingleTable()) {
			throw new RQException("'append@m' is unimplemented in annex table!");
		}
		// ������ݽṹ�Ƿ����
		Sequence data = cursor.peek(ICursor.FETCHCOUNT);		
		if (data == null || data.length() <= 0) {
			return;
		}
		
		//�жϽṹƥ��
		DataStruct ds = data.dataStruct();
		if (ds == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.needPurePmt"));
		}
		
		String []columns = this.colNames;
		int colCount = columns.length;
		if (colCount != ds.getFieldCount()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("engine.dsNotMatch"));
		}
		
		for (int i = 0; i < colCount; i++) {
			if (!ds.getFieldName(i).equals(columns[i])) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.dsNotMatch"));
			}
		}
				
		// �鲢���������ȱ��浽��ʱ�ļ�
		RowComTable groupTable = (RowComTable)getGroupTable();
		File srcFile = groupTable.getFile();
		File tmpFile = File.createTempFile("tmpdata", "", srcFile.getParentFile());
		try {
			Context ctx = new Context();
			String colNames[] = this.colNames.clone();
			for (int i = 0; i < colNames.length; i++) {
				if (isDim[i]) {
					colNames[i] = "#" + colNames[i];
				}
			}
			RowComTable tmpGroupTable = new RowComTable(tmpFile, colNames, groupTable.getDistribute(), null, ctx);
			tmpGroupTable.writePswHash = groupTable.writePswHash;
			tmpGroupTable.readPswHash = groupTable.readPswHash;
			
			PhyTable baseTable = tmpGroupTable.getBaseTable();
			if (segmentCol != null) {
				baseTable.setSegmentCol(segmentCol, segmentSerialLen);
			}
			
			int dcount = sortedColNames.length;
			Expression []mergeExps = new Expression[dcount];
			for (int i = 0; i < dcount; ++i) {
				mergeExps[i] = new Expression(sortedColNames[i]);
			}
			
			// ���鲢
			RowCursor srcCursor = new RowCursor(this);
			ICursor []cursors = new ICursor[]{srcCursor, cursor};
			MergesCursor mergeCursor = new MergesCursor(cursors, mergeExps, ctx);
			baseTable.append(mergeCursor);
			baseTable.close();
			
			// �رղ�ɾ������ļ�������ʱ�ļ�������Ϊ����ļ���
			groupTable.raf.close();
			groupTable.file.delete();
			tmpFile.renameTo(groupTable.file);
			
			// ���´����
			groupTable.reopen();
		} finally {
			tmpFile.delete();
		}	
	}
	
	//д�뻺�������
	public void appendCache() throws IOException {
		if (appendCache == null) return;
		
		ICursor cursor = new MemoryCursor(appendCache);
		// ׼��д����
		prepareAppend();
		
		if (parent != null) {
			parent.appendCache();
			appendAttached(cursor);
		} else if (sortedColNames == null) {
			appendNormal(cursor);
		} else if (groupTable.baseTable.getSegmentCol() == null) {
			appendSorted(cursor);
		} else {
			appendSegment(cursor);
		}
		
		// ����д���ݣ����浽�ļ�
		finishAppend();
		appendCache = null;
	}

	public ICursor cursor(String[] fields, Expression filter, String[] fkNames, Sequence[] codes, 
			String []opts, Context ctx, int pathSeq, int pathCount, int pathCount2) {
		getGroupTable().checkReadable();
		//�д治֧�ֶ�·
		MessageManager mm = EngineMessage.get();
		throw new RQException("cursor" + mm.getMessage("dw.needMCursor"));
	}

	public ICursor cursor(String[] fields, Expression exp, String[] fkNames, Sequence[] codes, 
			String []opts, ICursor cs, int seg, Object [][]endValues, Context ctx) {
		getGroupTable().checkReadable();
		//�д治֧�ֶ�·
		MessageManager mm = EngineMessage.get();
		throw new RQException("cursor" + mm.getMessage("dw.needMCursor"));
	}

	public ICursor cursor(Expression[] exps, String[] fields, Expression filter, String[] fkNames, 
			Sequence[] codes,String []opts, int pathCount, String opt, Context ctx) {
		if (fkNames != null || codes != null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cursor" + mm.getMessage("dw.needMCursor"));
		}
		
		if (pathCount < 2) {
			return cursor(exps, fields, filter, fkNames, codes, opts, opt, ctx);
		}
		
		PhyTable tmd = getSupplementTable(false);
		int blockCount = getDataBlockCount();
		if (blockCount == 0) {
			if (tmd == null) {
				return new MemoryCursor(null);
			} else {
				return tmd.cursor(exps, fields, filter, fkNames, codes,  opts, pathCount, opt, ctx);
			}
		}
		
		ICursor []cursors;

		int avg = blockCount / pathCount;
		if (avg < 1) {
			avg = 1;
			pathCount = blockCount;
		}
		
		// ǰ��Ŀ�ÿ�ζ�һ��
		int mod = blockCount % pathCount;
		cursors = new ICursor[pathCount];
		int start = 0;
		for (int i = 0; i < pathCount; ++i) {
			int end = start + avg;
			if (i < mod) {
				end++;
			}
			
			if (filter != null) {
				// �ֶβ��ж�ȡʱ��Ҫ���Ʊ��ʽ��ͬһ�����ʽ��֧�ֲ�������
				filter = filter.newExpression(ctx);
			}
			
			RowCursor cursor = new RowCursor(this, null, filter, exps, fields, ctx);
			
			cursor.setSegment(start, end);

			cursors[i] = cursor;
			start = end;
		}
		
		MultipathCursors mcs = new MultipathCursors(cursors, ctx);
		if (tmd == null) {
			return mcs;
		}
		
		String []sortFields = ((IDWCursor)cursors[0]).getSortFields();
		if (sortFields != null) {
			ICursor cs2 = tmd.cursor(exps, fields, filter, fkNames, codes,  opts, mcs, opt, ctx);
			return merge(mcs, (MultipathCursors)cs2, sortFields);
		} else {
			ICursor cs2 = tmd.cursor(exps, fields, filter, fkNames, codes,  opts, pathCount, opt, ctx);
			return conj(mcs, cs2);
		}
	
	}

	public ICursor cursor(Expression[] exps, String[] fields, Expression filter, String[] fkNames, 
			Sequence[] codes,String []opts, int segSeq, int segCount, String opt, Context ctx) {

		getGroupTable().checkReadable();
		
		if (filter != null) {
			// �ֶβ��ж�ȡʱ��Ҫ���Ʊ��ʽ��ͬһ�����ʽ��֧�ֲ�������
			filter = filter.newExpression(ctx);
		}
		
		RowCursor cursor = new RowCursor(this, null, filter, exps, fields, ctx);
		
		if (segCount < 2) {
			return cursor;
		}
		
		int startBlock = 0;
		int endBlock = -1;
		int avg = dataBlockCount / segCount;
		
		if (avg < 1) {
			// ÿ�β���һ��
			if (segSeq <= dataBlockCount) {
				startBlock = segSeq - 1;
				endBlock = segSeq;
			}
		} else {
			if (segSeq > 1) {
				endBlock = segSeq * avg;
				startBlock = endBlock - avg;
				
				// ʣ��Ŀ�����ÿ�ζ�һ��
				int mod = dataBlockCount % segCount;
				int n = mod - (segCount - segSeq);
				if (n > 0) {
					endBlock += n;
					startBlock += n - 1;
				}
			} else {
				endBlock = avg;
			}
		}

		cursor.setSegment(startBlock, endBlock);
		return cursor;
	}

	public ICursor cursor(Expression[] exps, String[] fields, Expression filter, String[] fkNames, 
			Sequence[] codes,String []opts, MultipathCursors mcs, String opt, Context ctx) {
		//�д治֧�ֶ�·
		MessageManager mm = EngineMessage.get();
		throw new RQException("cursor" + mm.getMessage("dw.needMCursor"));
	}

	public ICursor cursor(Expression[] exps, String[] fields, Expression filter, String[] fkNames, 
			Sequence[] codes,String []opts, int pathSeq, int pathCount, int pathCount2, String opt, Context ctx) {
		//�д治֧�ֶ�·
		MessageManager mm = EngineMessage.get();
		throw new RQException("cursor" + mm.getMessage("dw.needMCursor"));
	}

	public ICursor cursor(Expression[] exps, String[] fields, Expression filter, String[] fkNames, 
			Sequence[] codes,String []opts, ICursor cs, int seg, Object[][] endValues, Context ctx) {
		//�д治֧�ֶ�·
		MessageManager mm = EngineMessage.get();
		throw new RQException("cursor" + mm.getMessage("dw.needMCursor"));
	}

	public void addColumn(String colName, Expression exp, Context ctx) {
		//�д治֧���޸���
		MessageManager mm = EngineMessage.get();
		throw new RQException("cursor" + mm.getMessage("dw.needMCursor"));
	}

	public void deleteColumn(String colName) {
	}
	
	/**
	 * �����е������Сֵ
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public Object[] getMaxMinValue(String key) throws IOException {
		if (this.totalRecordCount == 0) {
			return null;
		}
		boolean isDim[] = this.isDim;
		String colNames[] = this.colNames;
		int len = colNames.length;
		
		boolean isColumn = false;
		int col = -1;
		for (int i = 0; i < len; ++i) {
			if (key.equals(colNames[i])) {
				isColumn = true;
				col = i;
				break;
			}
		}
		
		if (!isColumn) {
			return null;
		}
		
		if (!isDim[col]) {
			Expression max = new Expression("max(" + key +")");
			Expression min = new Expression("min(" + key +")");
			Expression[] exps = new Expression[] {max, min};
			Sequence seq = cursor(new String[] {key}).groups(null, null, exps, null, null, new Context());
			return ((Record)seq.get(1)).getFieldValues();
		}
		
		ObjectReader segmentReader = getSegmentObjectReader();
		
		int colCount = getAllColNames().length;
		int blockCount = getDataBlockCount();
		Object max = null, min = null;
		
		segmentReader.readLong40();
		for (int c = 0; c < colCount; c++) {
			Object minValue = segmentReader.readObject();
			Object maxValue = segmentReader.readObject();
			if (c == col) {
				min = minValue;
				max = maxValue;
			}
		}
		
		for (int i = 1; i < blockCount; ++i) {
			segmentReader.readLong40();
			for (int c = 0; c < colCount; c++) {
				Object minValue = segmentReader.readObject();
				Object maxValue = segmentReader.readObject();
				if (c == col) {
					if (Variant.compare(minValue, min) < 0) {
						min = minValue;
					}
					segmentReader.readObject();
					if (Variant.compare(maxValue, max) > 0) {
						max = maxValue;
					}
				}
			}
		}
		return new Object[] {max, min};
	}

	public void append(PhyTable table) throws IOException {
		throw new RuntimeException();
	}
}