package com.scudata.dm.op;

import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;

/**
 * ����������������
 * @author WangXiaoJun
 *
 */
public abstract class IHugeGroupsResult {
	public abstract Table groups(ICursor []cursors);
}
