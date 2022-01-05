package com.scudata.expression.mfn.file;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BFileReader;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.FileObject;
import com.scudata.dm.IFile;
import com.scudata.dm.LineImporter;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.FileCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.expression.Expression;
import com.scudata.expression.FileFunction;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �ӶԱ���ʽ������ļ����ı��ļ����ļ����в��Ҽ�¼
 * f.iselect(A,x;Fi,��;s) ����x������A�еļ�¼�����α�
 * f.iselect(a:b,x;Fi,��;s) ����x��[a,b]����ļ�¼�����α�
 * @author RunQian
 *
 */
public class ISelect extends FileFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("iselect" + mm.getMessage("function.missingParam"));
		}

		IParam valParam;
		String []selFields = null;
		String s = null;
		if (param.getType() == IParam.Semicolon) {
			valParam = param.getSub(0);
			IParam fieldParam = param.getSub(1);
			if (fieldParam == null) {
			} else if (fieldParam.isLeaf()) {
				selFields = new String[]{fieldParam.getLeafExpression().getIdentifierName()};
			} else {
				int size = fieldParam.getSubSize();
				selFields = new String[size];
				for (int i = 0; i < size; ++i) {
					IParam sub = fieldParam.getSub(i);
					if (sub == null || !sub.isLeaf()) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("iselect" + mm.getMessage("function.invalidParam"));
					}

					selFields[i] = sub.getLeafExpression().getIdentifierName();
				}
			}

			if (param.getSubSize() > 2) {
				IParam sParam = param.getSub(2);
				if (sParam != null) {
					Object obj = sParam.getLeafExpression().calculate(ctx);
					if (!(obj instanceof String)) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("iselect" + mm.getMessage("function.paramTypeError"));
					}

					s = (String)obj;
				}
			}
		} else {
			valParam = param;
		}

		if (valParam.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("iselect" + mm.getMessage("function.invalidParam"));
		}

		IParam sub0 = valParam.getSub(0);
		IParam sub1 = valParam.getSub(1);
		if (sub0 == null || sub1 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("iselect" + mm.getMessage("function.invalidParam"));
		}

		if (sub0.isLeaf()) {
			Object key = sub0.getLeafExpression().calculate(ctx);
			Sequence values;
			if (key == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("iselect" + mm.getMessage("function.invalidParam"));
			}
			if (key instanceof Sequence) {
				values = (Sequence)key;
			} else {
				values = new Sequence(1);
				values.add(key);
			}
			
			Expression exp = sub1.getLeafExpression();
			if (null == exp){
				MessageManager mm = EngineMessage.get();
				throw new RQException("iselect" + mm.getMessage("function.paramTypeError"));
			}
				
			return search(file, exp, values, selFields, s, option, ctx);
		} else {
			if (sub0.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("iselect" + mm.getMessage("function.invalidParam"));
			}
			
			Object startVal = null;
			Object endVal = null;
			IParam startParam = sub0.getSub(0);
			if (startParam != null) {
				startVal = startParam.getLeafExpression().calculate(ctx);
			}
			
			IParam endParam = sub0.getSub(1);
			if (endParam != null) {
				endVal = endParam.getLeafExpression().calculate(ctx);
			}
			
			Expression exp = sub1.getLeafExpression();
			if (null == exp){
				MessageManager mm = EngineMessage.get();
				throw new RQException("iselect" + mm.getMessage("function.paramTypeError"));
			}
				
			return search(file, exp, startVal, endVal, selFields, s, option, ctx);
		}
	}	
	
	private static ICursor search(FileObject fo, Expression exp, Sequence values,
								 String []selFields, String s, String opt, Context ctx) {
		boolean isCsv = false;
		boolean isMultiId = false;
		boolean isExist = true;
		if (opt != null) {
			if (opt.indexOf('b') != -1) {
				BFileReader reader = new BFileReader(fo);
				return reader.iselect(exp, values, selFields, ctx);
			}
			
			if (opt.indexOf('c') != -1) isCsv = true;
			if (opt.indexOf('e') != -1) isExist = false;
			if (opt.indexOf('r') != -1) isMultiId = true;
		}

		String charset = fo.getCharset();
		byte[] separator;
		if (s != null && s.length() > 0) {
			try {
				separator = s.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				throw new RQException(e.getMessage(), e);
			}
		} else if (isCsv) {
			separator = new byte[]{(byte)','};
		} else {
			separator = FileObject.COL_SEPARATOR;
		}


		Table table = iselect_t(fo, exp, values, separator, opt, isMultiId, ctx);
		
		if (selFields != null) {
			DataStruct ds = table.dataStruct();
			int fcount = selFields.length;
			int []index = new int[fcount];
			String []names = ds.getFieldNames();
			for (int f = 0; f < fcount; ++f) {
				index[f] = ds.getFieldIndex(selFields[f]);
				if (index[f] < 0) {
					if (isExist) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(selFields[f] + mm.getMessage("ds.fieldNotExist"));
					}
				} else {
					selFields[f] = names[index[f]];
				}
			}

			int len = table.length();
			Table selTable = new Table(selFields, len);
			for (int i = 1; i <= len; ++i) {
				Record nr = selTable.newLast();
				Record r = (Record)table.get(i);
				for (int f = 0; f < fcount; ++f) {
					if (index[f] >= 0) {
						nr.setNormalFieldValue(f, r.getFieldValue(index[f]));
					}
				}
			}

			table = selTable;
		}
		
		if (table != null && table.length() > 0) {
			return new MemoryCursor(table);
		} else {
			return null;
		}
	}

	/**
	 * ���ݶԱ����ݺͶԱȱ���ʽ���ļ��в��ҽ���������cursor���
	 * 
	 * @param fo			�ļ�����
	 * @param fieldName		�Աȱ���ʽ
	 * @param startVal		��ʼֵ
	 * @param endVal		����ֵ
	 * @param selFields		��ɽ�����ֶ�
	 * @param s				
	 * @param opt			��ȡ�ļ�����
	 * @param ctx			�����ı���
	 * @return				���ؽ��cursor
	 */
	private static ICursor search(FileObject fo, Expression exp, Object startVal,
			 Object endVal, String []selFields, String s, String opt, Context ctx) {
		boolean isCsv = false;
		if (opt != null) {
			// ����Ƕ������ļ������ɶ������ļ���cursor
			if (opt.indexOf('b') != -1) {
				BFileReader reader = new BFileReader(fo);
				return reader.iselect(exp, startVal, endVal, selFields, ctx);
			}
			
			if (opt.indexOf('c') != -1) isCsv = true;
		}

		String charset = fo.getCharset();
		byte[] separator;
		if (s != null && s.length() > 0) {
			try {
				separator = s.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				throw new RQException(e.getMessage(), e);
			}
		} else if (isCsv) {
			separator = new byte[]{(byte)','};
		} else {
			separator = FileObject.COL_SEPARATOR;
		}

		LineImporter importer = null;
		Object []line = null;

		try {
			importer = new LineImporter(fo.getInputStream(), charset, separator, opt, 1024);
			line = importer.readLine();
		} catch(IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			try {
				if (importer != null) {
					importer.close();
				}
			} catch (IOException ie) {
			}
		}

		if (line == null || line.length == 0) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(exp.getIdentifierName() + mm.getMessage("ds.fieldNotExist"));
		}
		
		long start = 0;
		if (startVal != null) {
			start = iselect_t(fo, exp, startVal, separator, opt, true, ctx);
			if (start < 0) {
				start = -start;
			}
		}
		
		long end;
		if (endVal != null) {
			end = iselect_t(fo, exp, endVal, separator, opt, false, ctx);
			if (end < 0) {
				end = -end;
			}
		} else {
			end = fo.size();
		}
		
		FileCursor cursor = new FileCursor(fo, 0, 0, selFields, null, s, opt, ctx);
		cursor.setStart(start);
		cursor.setEnd(end);
		return cursor;
	}
		
	/**
	 * �������ļ��У�ȡ�ü�¼����ʽ��ֵ����values�еļ�¼��
	 * 
	 * @param fo			�����ļ�����
	 * @param exp			����ʽ
	 * @param values		�ο�ֵ
	 * @param separator		��foΪ�ı��ļ����ñ���Ϊ�ָ���.
	 * @param opt			�ļ���д����
	 * @param isMultiId		�Ƿ��ID
	 * 
	 * @return				��ȡ�õļ�¼��ɵ�table
	 */
	private static Table iselect_t(FileObject fo, Expression exp, Sequence values, 
			byte[] separator, String opt, boolean isMultiId, Context ctx) {
		boolean isTitle = false;
		if (opt != null) {
			if (opt.indexOf('t') != -1) isTitle = true;
		}
		
		IFile file = fo.getFile();
		String charset = fo.getCharset();
		LineImporter importer = null;
		Object []line = null;
		long start = 0;
		
		Record rec = null;

		try {
			importer = new LineImporter(file.getInputStream(), charset, separator, opt, 1024);
			line = importer.readLine();
			if (line == null) {
				return null;
			}
			
			// ��ʼ����¼����
			String[] fields = new String[line.length];
			for (int i = 0; i < line.length; i++) {
				fields[i] = line[i].toString();
			}
			
			byte []colTypes = new byte[line.length];
			importer.setColTypes(colTypes, null);

			if (isTitle) {
				start = importer.getCurrentPosition() - 2;
			}
		} catch(IOException e) {
			try {
				if (importer != null) {
					importer.close();
					importer = null;
				}
			} catch (IOException ie) {
			}
			
			throw new RQException(e.getMessage(), e);
		}

		if (line == null || line.length == 0) {
			try {
				if (importer != null) {
					importer.close();
					importer = null;
				}
			} catch (IOException e) {
			}

			MessageManager mm = EngineMessage.get();
			throw new RQException(exp.getIdentifierName() + mm.getMessage("ds.fieldNotExist"));
		}

		int fcount = line.length;
		DataStruct ds;
		if (isTitle) {
			String[] items = new String[fcount];
			for (int f = 0; f < fcount; ++f) {
				items[f] = Variant.toString(line[f]);
			}

			ds = new DataStruct(items);
		} else {
			String[] items = new String[fcount];
			ds = new DataStruct(items);
		}
		
		rec = new Record(ds);
		int valCount = values.length();
		Table table = new Table(ds, valCount);
		long size = file.size();

		try {
			for (int i = 1; i <= valCount; ++i) {
				long low = start;
				long high = size;
				Object key = values.get(i);

				boolean isExist = false;
				while (low <= high) {
					long mid = (low + high) >> 1;
					long pos = importer.getCurrentPosition();
					if (pos > mid) {
						importer.close();
						LineImporter tmp = new LineImporter(file.getInputStream(), charset, separator, opt, 1024);
						tmp.copyProperty(importer);
						importer = tmp;
					}
					
					if (isMultiId && low>=high) break;
					importer.seek(mid);
					Object []objs = importer.readLine();
					if (objs == null) { // �����β
						high = mid - 1;
					} else {
						rec.setStart(0, objs);
						int cmp = 0;
						try {
							cmp = Variant.compare(rec.calc(exp, ctx), key);
						} catch (RQException e) {
							low = mid + 1;
							start = low;
							continue;
						}
						if (isMultiId){
							if (cmp < 0) {
								low = mid + 1;
								start = low;
							} else if (cmp > 0) {
								high = mid;
							} else {
								high = mid;
								isExist = true;
							}
						} else {
							if (cmp < 0) {
								low = mid + 1;
								start = low;
							} else if (cmp > 0) {
								high = mid - 1;
							} else {
								if (objs.length <= fcount) {
									table.newLast(objs);
								} else {
									Record r = table.newLast();
									for (int f = 0; f < fcount; ++f) {
										r.setNormalFieldValue(f, objs[f]);
									}
								}
	
								pos = importer.getCurrentPosition();
								if (pos < size) {
									start = pos - 2;
								} else {
									start = size;
								}
	
								break;
							}
						}
					}/* else {
						low = mid + 1;
						start = low;
					}*/
				}
				if (isMultiId && isExist){
					start = iselect_i(importer, table,
							low, exp, rec, key, fcount, size, ctx);
				}

				if (start >= size) {
					break;
				}
			}
		} catch(IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			try {
				if (importer != null) importer.close();
			} catch (Exception e) {
			}
		}

		return table;
	}
	
	private static long iselect_i(LineImporter importer, Table table,long pos,
			Expression exp, Record rec, Object key, int fcount, long size, Context ctx)
					throws IOException {
		long lastpos;
		while (true)
		{
			lastpos = pos;
			if(pos+2 >= size) break;
			importer.seek(pos);			
			Object []objs = importer.readLine();
			pos = importer.getCurrentPosition()-2;
			if (objs == null) { // �����β
				return importer.getCurrentPosition();
			}
			rec.setStart(0, objs);

			//if (field >= objs.length) return importer.getCurrentPosition();
			try {
				if (0 == Variant.compare(rec.calc(exp, ctx), key)){
					if (objs.length <= fcount) {
						table.newLast(objs);
					} else {
						Record r = table.newLast();
						for (int f = 0; f < fcount; ++f) {
							r.setNormalFieldValue(f, objs[f]);
						}
					}
					
				} else {
					break;
				}
			} catch (RQException e) {
				return importer.getCurrentPosition();
			}
		}
		return lastpos;
	}
		
	/**
	 * ��һ���ı��ļ��У���λ����ʽexp�Ľ����ӽ�value�ļ�¼��λ��
	 * 
	 * @param fo		�ı��ļ����ļ�����
	 * @param exp		����ʽ
	 * @param value		�ο�ֵ
	 * @param separator	�ı��ļ��ķָ���
	 * @param opt		�ļ���ȡ����
	 * @param isStart	true	С��value�ļ�¼
	 * 					false	����value�ļ�¼
	 * 
	 * @return			���ض�Ӧ�����λ��
	 * 
	 */
	private static long iselect_t(FileObject fo, Expression exp, Object value, 
			byte[] separator, String opt, boolean isStart, Context ctx) {
		boolean isTitle = false;
		if (opt != null) {
			if (opt.indexOf('t') != -1) isTitle = true;
		}

		IFile file = fo.getFile();
		String charset = fo.getCharset();
		LineImporter importer = new LineImporter(file.getInputStream(), charset, separator, opt, 1024);

		long low = 0;
		long high = file.size();

		try {
			Object []line = importer.readLine();
			if (line == null) {
				return -1;
			}
			
			byte []colTypes = new byte[line.length];
			importer.setColTypes(colTypes, null);
			
			if (isTitle) {
				low = importer.getCurrentPosition() - 2;
			}
			
			// ��ʼ����¼����
			String[] fields = new String[line.length];
			for (int i = 0; i < line.length; i++) {
				fields[i] = line[i].toString();
			}
			DataStruct ds = new DataStruct(fields);
			Record rec = new Record(ds);
			
			while (low <= high) {
				long mid = (low + high) >> 1;
				long pos = importer.getCurrentPosition();
				if (pos > mid) {
					importer.close();
					LineImporter tmp = new LineImporter(file.getInputStream(), charset, separator, opt, 1024);
					tmp.copyProperty(importer);
					importer = tmp;
				}
				
				importer.seek(mid);
				Object []objs = importer.readLine();
				if (objs == null) { // �����β
					high = mid - 1;
				} else {
					rec.setStart(0, objs);
					
					int cmp = Variant.compare(rec.calc(exp, ctx), value);					
					if (cmp < 0) {
						low = mid + 1;
					} else if (cmp > 0) {
						high = mid - 1;
					} else {
						if (isStart) {
							return mid;
						} else {
							return importer.getCurrentPosition() - 2;
						}
					}
				}
			}
		} catch(IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			try {
				if (importer != null) importer.close();
			} catch (IOException e) {
			}
		}

		return -low;
	}
}