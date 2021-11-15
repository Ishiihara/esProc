package com.raqsoft.dw.compress;

import java.io.IOException;
import java.util.ArrayList;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dw.BufferReader;
import com.raqsoft.resources.EngineMessage;

public class StringColumn extends Column {
	private static final int NULL = -1;
	
	// ���ݰ���洢��ÿ����Column.BLOCK_RECORD_COUNT����¼
	private ArrayList<char[]> blockList = new ArrayList<char[]>(1024);
	
	// ������blockList���Ӧ���λ��, -1��ʾnull
	private ArrayList<int[]> posList = new ArrayList<int[]>(1024);
	private int lastRecordCount = Column.BLOCK_RECORD_COUNT; // ���һ��ļ�¼��
	private int nextPos = -1; // ��һ��д�뵽buffer�е�λ��
	
	// �����¿�ʱʹ��buffer��buffer����ʱ��������1.5�����ȵ���buffer�����¼��д��������ʵ�ʵĴ�С���浽blockList��
	private char []buffer = new char[65536];
	
	// ׷��һ�е�����
	public void addData(Object data) {
		if (lastRecordCount == Column.BLOCK_RECORD_COUNT) {
			if (blockList.size() > 0) {
				// ���¼��д����������СΪʵ�ʴ�С
				char []block = new char[nextPos];
				System.arraycopy(buffer, 0, block, 0, nextPos);
				blockList.set(blockList.size() - 1, block);
			}
			
			int []posBlock = new int[Column.BLOCK_RECORD_COUNT];
			blockList.add(buffer);
			posList.add(posBlock);
			lastRecordCount = 0;
			nextPos = 0;
		}
		
		if (data instanceof String) {
			String str = (String)data;
			int len = str.length();
			int free = buffer.length - nextPos;
			
			// ������һ��Ļ������ռ䲻������������һ���Ļ�����
			if (free < len) {
				char []tmp = new char[buffer.length * 3 / 2];
				System.arraycopy(buffer, 0, tmp, 0, buffer.length);
				buffer = tmp;
				blockList.set(blockList.size() - 1, tmp);
			}
			
			str.getChars(0, len, buffer, nextPos);
			int []posBlock = posList.get(posList.size() - 1);
			posBlock[lastRecordCount++] = nextPos;
			nextPos += len;
		} else if (data == null) {
			int []posBlock = posList.get(posList.size() - 1);
			posBlock[lastRecordCount++] = NULL;
		} else {
			// ���쳣
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("ds.colTypeDif"));
		}
	}
	
	// ȡ��row�е�����
	public Object getData(int row) {
		// row�кţ���1��ʼ����
		row--;
		int b = row / Column.BLOCK_RECORD_COUNT;
		int index = row % Column.BLOCK_RECORD_COUNT;
		
		int []posBlock = posList.get(b);
		int startPos = posBlock[index];
		if (startPos == NULL) {
			return null;
		}
		
		char []block = blockList.get(b);
		int endPos;
		
		// �Ƿ����һ��
		if (b == blockList.size() - 1) {
			endPos = nextPos;
			for (int i = index + 1, end = lastRecordCount; i < end; ++i) {
				if (posBlock[i] != NULL) {
					endPos = posBlock[i];
					break;
				}
			}
		} else {
			endPos = block.length;
			for (int i = index + 1; i < Column.BLOCK_RECORD_COUNT; ++i) {
				if (posBlock[i] != NULL) {
					endPos = posBlock[i];
					break;
				}
			}
		}
		
		return new String(block, startPos, endPos - startPos);
	}
	
	public Column clone() {
		return new StringColumn();
	}
	
	public void appendData(BufferReader br) throws IOException {
		addData(br.readObject());
	}
}