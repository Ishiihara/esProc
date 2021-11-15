package com.raqsoft.expression.mfn.file;

import java.io.File;
import java.io.IOException;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.FileObject;
import com.raqsoft.dw.ColumnGroupTable;
import com.raqsoft.dw.GroupTable;
import com.raqsoft.dw.RowGroupTable;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.FileFunction;
import com.raqsoft.expression.IParam;
import com.raqsoft.parallel.ClusterFile;
import com.raqsoft.resources.EngineMessage;

/**
 * ��������ļ�
 * f.create(C,��;x)
 * @author RunQian
 *
 */
public class Create extends FileFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("create" + mm.getMessage("function.missingParam"));
		}
		
		IParam colParam = param;
		Expression distributeExp = null; // �ֲ�����ʽ
		String distribute = null;
		
		if (param.getType() == IParam.Semicolon) {
			int size = param.getSubSize();
			if (size != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("create" + mm.getMessage("function.invalidParam"));
			}
			
			colParam = param.getSub(0);
			if (colParam == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("create" + mm.getMessage("function.invalidParam"));
			}
			
			IParam expParam = param.getSub(1);
			if (expParam != null) {
				distributeExp = expParam.getLeafExpression();
				distribute = distributeExp.toString();
			}
		}
		
		String []cols;
		if (colParam.isLeaf()) {
			cols = new String[]{colParam.getLeafExpression().getIdentifierName()};
		} else {
			int size = colParam.getSubSize();
			cols = new String[size];
			for (int i = 0; i < size; ++i) {
				IParam sub = colParam.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("create" + mm.getMessage("function.invalidParam"));
				}
				
				cols[i] = sub.getLeafExpression().getIdentifierName();
			}
		}

		if (file.isRemoteFile()) {
			// Զ���ļ�
			String host = file.getIP();
			int port = file.getPort();
			String fileName = file.getFileName();
			Integer partition = file.getPartition();
			int p = partition == null ? -1 : partition.intValue();
			ClusterFile cf = new ClusterFile(host, port, fileName, p, ctx);
			return cf.createGroupTable(cols, distributeExp, option, ctx);
		} else {
			
		}
		
		FileObject fo = file;
		String opt = option;
		File file = fo.getLocalFile().file();
		Integer partition = fo.getPartition();

		if ((opt == null || opt.indexOf('y') == -1) && file.exists()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("file.fileAlreadyExist", fo.getFileName()));
		} else if (opt != null && opt.indexOf('y') != -1 && file.exists()) {
			try {
				GroupTable table = GroupTable.open(file, ctx);
				table.delete();
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}

		try {
			GroupTable table;
			if (opt != null && opt.indexOf('r') != -1) {
				table = new RowGroupTable(file, cols, distribute, opt, ctx);
			} else {
				table = new ColumnGroupTable(file, cols, distribute, opt, ctx);
			}
			
			table.setPartition(partition);
			return table.getBaseTable();
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
}