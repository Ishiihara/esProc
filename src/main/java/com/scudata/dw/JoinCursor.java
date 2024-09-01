package com.scudata.dw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.ObjectReader;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.expression.FieldRef;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Moves;
import com.scudata.expression.Node;
import com.scudata.expression.ParamInfo2;
import com.scudata.expression.UnknownSymbol;
import com.scudata.expression.mfn.sequence.Avg;
import com.scudata.expression.mfn.sequence.Count;
import com.scudata.expression.mfn.sequence.Max;
import com.scudata.expression.mfn.sequence.Min;
import com.scudata.expression.mfn.sequence.New;
import com.scudata.expression.mfn.sequence.Sum;
import com.scudata.expression.operator.And;
import com.scudata.parallel.ClusterPhyTable;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * ����T.new T.derive T.news���α�
 * @author runqian
 *
 */
public class JoinCursor extends ICursor {
	private IPhyTable table;
	private Expression []exps;//ȡ�����ʽ
	private String []fields;//ȡ���ֶ���
	
	//filters���
	private String []fkNames;
	private Sequence []codes;
	private String []opts;
	private IFilter []filters;
	private FindFilter []findFilters;
	private Expression unknownFilter;
	private int keyColCount;//�����ֶ��� (ȥ��֮��ģ�������������������)
	private int keyColIndex[];//������T��ȡ���е��±�
	private int keyOffset;//������ȥ�غ���T��ȡ���еĿ�ʼλ��
	
	private boolean isClosed;
	private boolean isNew;//��new����
	private boolean isNews;//��news����
	private DataStruct ds;

	private int endBlock; // ������
	private int curBlock = 0;
	private ColumnMetaData []columns;
	private BlockLinkReader rowCountReader;
	private BlockLinkReader []colReaders;
	private ObjectReader []segmentReaders;
	private BufferReader []bufReaders;
	private int len1;//��ǰ�������
	private Object []keys1;//��ǰkeyֵ
	private BaseRecord r;//��ǰ��¼
	
	private ICursor cursor2;//A/cs
	private String[] csNames;//A/cs:K��K������ָ��A/cs�������ӵ��ֶ�
	private Sequence cache2;

	private int cur1 = -1;
	private int cur2 = -1;
	
	private int keyCount;//�����ֶ���
	
	private int csFieldsCount;//A/cs���ֶθ���
	private int []keyIndex2;//A/cs�������±�
	
	private int []fieldIndex1;//�����ֶ���Tȡ���ֶε��±�
	private int []fieldIndex2;//�����ֶ���csȡ���ֶε��±�
	
	private boolean hasExps;//�б��ʽ
	private boolean hasR;//������r
	private DataStruct ds1;//��T��ȡ��һ�����ݻ���ʱ��
	
	private boolean needSkipSeg;//ȡ��ǰ��Ҫ����
	private Node nodes[];//�б��ʽʱ�����ʽ��home�ڵ��������

	/**
	 * 
	 * @param table ����
	 * @param exps ȡ�����ʽ
	 * @param fields ȡ���ֶ�����
	 * @param cursor2 ����cs
	 * @param csNames ����K��ָ��A/cs�������ӵ��ֶ�
	 * @param type	�������ͣ�0:derive; 1:new; 2:news; 0x1X ��ʾ����;
	 * @param option ѡ��	
	 * @param filter ��table�Ĺ�������
	 * @param fkNames ��table��Switch��������
	 * @param codes
	 * @param ctx
	 */
	public JoinCursor(IPhyTable table, Expression []exps, String []fields, ICursor cursor2, String[] csNames, 
			int type, String option, Expression filter, String []fkNames, Sequence []codes, String[] opts, Context ctx) {
		this.table = table;
		this.cursor2 = cursor2;
		this.csNames = csNames;
		this.exps = exps;
		this.fields = fields;
		this.fkNames = fkNames;
		this.codes = codes;
		this.opts = opts;
		
		if (option != null && option.indexOf("r") != -1) {
			this.hasR = true;
		}
		needSkipSeg = (type & 0x10) == 0x10;
		type &= 0x0f;
		this.isNew = type == 1;
		this.isNews = type == 2;
		this.ctx = ctx;
		
		if (ctx != null) {
			ctx.addResource(this);
		}
		
		if (filter != null) {
			parseFilter((ColPhyTable) table, filter, ctx);
		}
		
		if (fkNames != null) {
			parseSwitch((ColPhyTable) table, ctx);
		}
		
		//��filters���ki=wi�ŵ�FindFilters��
		if (filters != null) {
			int len = filters.length;
			for (int i = 0; i < len; i++) {
				if (filters[i] instanceof FindsFilter) {
					if (findFilters == null) {
						findFilters = new FindFilter[len];
					}
					findFilters[i] = (FindsFilter) filters[i];
				}
			}
		}
		
		init();
	}
	
	private void parseSwitch(ColPhyTable table, Context ctx) {
		int fcount = fkNames.length;
		ArrayList<IFilter> filterList = new ArrayList<IFilter>();
		ArrayList<FindFilter> findFilterList = new ArrayList<FindFilter>();
		if (filters != null) {
			for (IFilter filter : filters) {
				filterList.add(filter);
				findFilterList.add(null);
			}
		}
		
		int fltCount = filterList.size();
		
		Next:
		for (int f = 0; f < fcount; ++f) {
			ColumnMetaData column = table.getColumn(fkNames[f]);
			if (column == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(fkNames[f] + mm.getMessage("ds.fieldNotExist"));
			}
			
			int pri = table.getColumnFilterPriority(column);
			FindFilter find;
			if (opts[f] != null && opts[f].indexOf("#") != -1) {
				find = new MemberFilter(column, pri, codes[f], null);
			} else if (opts[f] != null && opts[f].indexOf("null") != -1) {
				find = new NotFindFilter(column, pri, codes[f], null);
			} else {
				find = new FindFilter(column, pri, codes[f], null);
			}
			
			for (int i = 0; i < fltCount; ++i) {
				IFilter filter = filterList.get(i);
				if (filter.isSameColumn(find)) {
					LogicAnd and = new LogicAnd(filter, find);
					filterList.set(i, and);
					findFilterList.set(i, find);
					continue Next;
				}
			}
			
			filterList.add(find);
			findFilterList.add(find);
		}
		
		int total = filterList.size();
		filters = new IFilter[total];
		findFilters = new FindFilter[total];
		filterList.toArray(filters);
		findFilterList.toArray(findFilters);
	
	}
	
	private void parseFilter(ColPhyTable table, Expression exp, Context ctx) {
		Object obj = Cursor.parseFilter(table, exp, ctx);
		unknownFilter = null;
		
		if (obj instanceof IFilter) {
			filters = new IFilter[] {(IFilter)obj};
		} else if (obj instanceof ArrayList) {
			@SuppressWarnings("unchecked")
			ArrayList<Object> list = (ArrayList<Object>)obj;
			ArrayList<IFilter> filterList = new ArrayList<IFilter>();
			Node node = null;
			for (Object f : list) {
				if (f instanceof IFilter) {
					filterList.add((IFilter)f);
				} else {
					if (node == null) {
						node = (Node)f;
					} else {
						And and = new And();
						and.setLeft(node);
						and.setRight((Node)f);
						node = and;
					}
				}
			}
			
			int size = filterList.size();
			if (size > 0) {
				filters = new IFilter[size];
				filterList.toArray(filters);
				Arrays.sort(filters);
				
				if (node != null) {
					unknownFilter = new Expression(node);
				}
			} else {
				unknownFilter = exp;
			}
		} else if (obj instanceof ColumnsOr) {
			ArrayList<ModifyRecord> modifyRecords = table.getModifyRecords();
			if (modifyRecords != null || exps != null) {
				unknownFilter = exp;
			} else {
				//Ŀǰֻ�Ż�û�в�����û�б��ʽ�����
				filters = ((ColumnsOr)obj).toArray();
			}
		} else {
			unknownFilter = exp;
		}
	}
	
	private void init() {
		String []keyNames;//T������
		if (table instanceof IPhyTable) {
			keyNames = ((IPhyTable) table).getAllSortedColNames();
		} else {
			keyNames = ((ClusterPhyTable) table).getAllSortedColNames();
		}
		
		String []joinNames = keyNames;//join�ֶΣ�Ĭ��ȡT������
		
		//�õ�cs��ds
		DataStruct ds2;//cs�Ľṹ
		Sequence seq = cursor2.peek(1);
		if (seq == null) {
			isClosed = true;
			return;
		}
		ds2 = ((BaseRecord) seq.get(1)).dataStruct();
		
		if (isNew) {
			//newʱ����T������
			if (joinNames == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("ds.lessKey"));
			}
			keyCount = joinNames.length;
			keyIndex2 = new int[keyCount];
			if (csNames == null) {
				for (int i = 0; i < keyCount; i++) {
					keyIndex2[i] = i;
				}
			} else {
				if (csNames.length > keyCount) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("ds.lessKey"));
				}
				for (int i = 0; i < keyCount; i++) {
					keyIndex2[i] = ds2.getFieldIndex(csNames[i]);
				}
			}
		} else {
			//newsʱȡcs������
			
			//1.�õ�cs�������±�
			if (csNames == null) {
				keyIndex2 = ds2.getPKIndex();
			} else {
				int csNamesLen = csNames.length;
				keyIndex2 = new int[csNamesLen];
				for (int i = 0; i < csNamesLen; i++) {
					keyIndex2[i] = ds2.getFieldIndex(csNames[i]);
				}
			}
			if (keyIndex2 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("ds.lessKey"));
			}
			keyCount = keyIndex2.length;
			
			//2.ȡTǰ����ֶ�
			joinNames = new String[keyCount];//��ʱ����T������
			String[] allNames;
			if (table instanceof IPhyTable) {
				allNames = ((IPhyTable) table).getAllColNames();
			} else {
				allNames = ((ClusterPhyTable) table).getAllColNames();
			}
			for (int i = 0; i < keyCount; i++) {
				joinNames[i] = allNames[i];
			}
		}
		
		//��ʼ��֯ȡ���ֶ�: [filters��(���ܰ�������)]+[����w���õ������ֶ�]+[����]+[Tѡ���ֶ�]+[cs�ֶ�]
		ArrayList<String> allList = new ArrayList<String>();
		
		//1. filters�ֶΣ����ܰ�������
		ArrayList<String> filtersList = new ArrayList<String>();
		if (filters != null) {
			for (IFilter filter : filters) {
				String f = filter.getColumn().getColName();
				filtersList.add(f);
			}
		}
		
		//2. ����w��Ҫ������ȡ���ֶ�
		ArrayList<String> tempList = new ArrayList<String>();
		if (unknownFilter != null) {
			// ��鲻��ʶ��ı��ʽ���Ƿ�������û��ѡ�����ֶΣ��������������뵽ѡ���ֶ���
			unknownFilter.getUsedFields(ctx, tempList);
			if (tempList.size() > 0) {
				for (String name : tempList) {
					if (!filtersList.contains(name)
							&& ((ColPhyTable) table).getColumn(name) != null) {
						filtersList.add(name);
					}
				}
			}
		}
		allList.addAll(filtersList);
				
		//3. �����ֶ�
		ArrayList<String> keyList = new ArrayList<String>();//ȡT��cs����С����,����join
		for (int i = 0; i < keyCount; i++) {
			String f = joinNames[i];
			keyList.add(f);
			if (!allList.contains(f)) {
				allList.add(f);
			}
		}
		ArrayList<String> allkeyList = new ArrayList<String>();//T�����������ж�ȡ���ֶ����Ƿ������T�������У�
		if (keyNames != null) {
			for(String f : keyNames) {
				allkeyList.add(f);
			}
		}
		
		//4. ѡ���ֶΣ�exps�������{����}����ʱҪչ���õ�ȡ���ֶ�
		ArrayList<String> fetchKeyList = new ArrayList<String>();//����ȡ���ֶ���������������ֶ�
		int fetchKeyListFlag[] = new int[keyNames == null ? 0 : keyNames.length];//��־��T�������ֶ��Ƿ񶼳�����ȡ���ֶ�
		for (int i = 0, len = exps.length; i < len; i++) {
			Expression exp = exps[i];
			if (exp == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("Expression.missingParam"));
			}
			
			if (fields[i] == null) {
				fields[i] = exps[i].getFieldName();
			}
			
			Node home = exp.getHome();
			if (home instanceof UnknownSymbol) {
				String f = exp.getFieldName();
				if (!allList.contains(f) && ds2.getFieldIndex(f) < 0) {
					allList.add(f);
				}
				
				//�ж�f�Ƿ���������
				int idx = allkeyList.indexOf(f);
				if (idx >= 0) {
					fetchKeyList.add(fields[i]);
					fetchKeyListFlag[idx] = 1;
				}
			} else {
				hasExps = true;
				if (home instanceof Moves) {
					IParam fieldParam = ((Moves) exp.getHome()).getParam();
					ParamInfo2 pi = ParamInfo2.parse(fieldParam, "cursor", false, false);
					String []subFields = pi.getExpressionStrs1();
					for (String f : subFields) {
						if (!allList.contains(f))
							allList.add(f);
					}
				} else if (home instanceof com.scudata.expression.fn.gather.Top) {
					IParam fieldParam = ((com.scudata.expression.fn.gather.Top) exp.getHome()).getParam();
					if (fieldParam != null) {
						if (!fieldParam.isLeaf()) {
							IParam sub1 = fieldParam.getSub(1);
							String f = sub1.getLeafExpression().getFieldName();
							if (!allList.contains(f))
								allList.add(f);
						}
					}
				} else {
					tempList.clear();
					exp.getUsedFields(ctx, tempList);
					for (String f : tempList) {
						if (!allList.contains(f) && ds2.getFieldIndex(f) < 0)
							allList.add(f);
					}
				}
			}
		}
		
		//������Ҫ��ȡ����
		String[] allExpNames = new String[allList.size()];
		allList.toArray(allExpNames);
		
		//�б��ʽʱҪ�������node
		if (hasExps) {
			int len = exps.length;
			nodes = new Node[len];
			for (int i = 0; i < len; i++) {
				nodes[i] = parseNode(exps[i], ctx);
			}
		}
		
		//�õ����е�Column
		if (table instanceof ColPhyTable) {
			columns = ((ColPhyTable) table).getColumns(allExpNames);
			int colCount = columns.length;
			
			bufReaders = new BufferReader[colCount];
			colReaders = new BlockLinkReader[colCount];
			segmentReaders = new ObjectReader[colCount];
			for (int i = 0; i < colCount; ++i) {
				if (columns[i] != null) {
					colReaders[i] = columns[i].getColReader(true);
					segmentReaders[i] = columns[i].getSegmentReader();
				}
			}
			
			rowCountReader = ((ColPhyTable) table).getSegmentReader();
			endBlock = ((PhyTable) table).getDataBlockCount();
		}
		
		//�õ����ص�ds
		if (isNew || isNews) {
			ds = new DataStruct(fields);
		} else {
			//deriveʱҪ����cs���ֶ�
			csFieldsCount = ds2.getFieldCount();
			String[] fieldNames = new String[csFieldsCount + fields.length];
			System.arraycopy(ds2.getFieldNames(), 0, fieldNames, 0, csFieldsCount);
			System.arraycopy(fields, 0, fieldNames, csFieldsCount, fields.length);
			ds = new DataStruct(fieldNames);
		}
		
		//ȡ���ֶ����Ƿ��������
		boolean hasKey = true;
		for (int i : fetchKeyListFlag) {
			if (i != 1) {
				hasKey = false;
				break;
			}
		}
		if (hasKey) {
			String keys[] = new String[fetchKeyList.size()];
			fetchKeyList.toArray(keys);
			ds.setPrimary(keys);
		}
		
		//ѡ���ֶο�����T�Ҳ������cs�Ҳ������T�ı��ʽ
		ds1 = new DataStruct(allExpNames);
		if (!hasExps) {
			int len = fields.length;
			int expLen = exps.length;
			fieldIndex1 = new int[len];
			fieldIndex2 = new int[len];
			for (int i = 0; i < len; i++) {
				String f;
				if (i < expLen) {
					f = exps[i].getFieldName();
				} else {
					f = fields[i];
				}
				fieldIndex1[i] = ds1.getFieldIndex(f);
				if (fieldIndex1[i] < 0) {
					int idx = ds2.getFieldIndex(f);
					if (idx < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(f + mm.getMessage("ds.fieldNotExist"));
					} else {
						fieldIndex2[i] = idx;
					}
				}
			}
		} else {
			int len = fields.length;
			fieldIndex2 = new int[len];
			for (int i = 0; i < len; i++) {
				String f = exps[i].getFieldName();
				fieldIndex2[i] = ds2.getFieldIndex(f);
			}
		}
		
		int len = keyList.size();
		keyColIndex = new int[len];
		for (int i = 0; i < len; i++) {
			keyColIndex[i] = ds1.getFieldIndex(keyList.get(i));
		}
		if (filtersList.size() != 0) {
			keyList.removeAll(filtersList);
		}
		keyColCount = keyList.size();
		keyOffset = filtersList.size();
	}

	/**
	 * ��cs��ȡһ������
	 * @return
	 */
	private int loadBlock() {
		if (curBlock >= endBlock) return -1;
		
		cur1 = 1;
		try {
			if (filters == null) {
				curBlock++;
				int recordCount = rowCountReader.readInt32();
				int colCount = colReaders.length;
				for (int f = 0; f < colCount; ++f) {
					bufReaders[f] = colReaders[f].readBlockData(recordCount);
				}
				return recordCount;
			} else {
				while (curBlock < endBlock) {
					curBlock++;
					int colCount = colReaders.length;
					long []positions = new long[colCount];
					int recordCount = rowCountReader.readInt32();
					
					boolean sign = true;
					int f = 0;
					NEXT:
					for (; f < keyOffset; ++f) {
						positions[f] = segmentReaders[f].readLong40();
						if (columns[f].hasMaxMinValues()) {
							Object minValue = segmentReaders[f].readObject();
							Object maxValue = segmentReaders[f].readObject();
							segmentReaders[f].skipObject();
							for (int i = 0, len = keyColIndex.length; i < len; i++) {
								if (f == keyColIndex[i]) {
									if (!filters[f].match(minValue, maxValue)) {
										++f;
										sign = false;
										break NEXT;
									}
									break;
								}
							}
							
						}
					}
					
					for (; f < colCount; ++f) {
						positions[f] = segmentReaders[f].readLong40();
						if (columns[f].hasMaxMinValues()) {
							segmentReaders[f].skipObject();
							segmentReaders[f].skipObject();
							segmentReaders[f].skipObject();
						}
					}
					
					if (sign) {
						for (f = 0; f < colCount; ++f) {
							bufReaders[f] = colReaders[f].readBlockData(positions[f], recordCount);
						}
						return recordCount;
					}	
				}
				return -1;
			}
			
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}

	/**
	 * ����ǰ�ȸ���A/cs����ֵ��������
	 * @param vals A/cs������
	 */
	private void skipSegment(Object vals[]) {
		int blockCount = endBlock;
		int colCount = colReaders.length;
		Object []keys1 = new Object[keyCount];
		
		long lastPos[] = new long[colCount];
		long pos[] = new long[colCount];
		Object []blockMinVals = new Object[colCount];
		ObjectReader []readers = new ObjectReader[colCount];
		segmentReaders = new ObjectReader[colCount];
		
		for (int f = 0; f < colCount; ++f) {
			readers[f] = columns[f].getSegmentReader();
			segmentReaders[f] = columns[f].getSegmentReader();
		}
		
		try {
			for (int b = 0; b < blockCount; ++b) {
				for (int f = 0; f < colCount; ++f) {
					pos[f] = readers[f].readLong40();
					if (columns[f].hasMaxMinValues()) {
						readers[f].skipObject();
						readers[f].skipObject();
						blockMinVals[f] = readers[f].readObject(); //startValue
					}
				}
				
				for (int i = 0; i < keyCount; i++) {
					keys1[i] = blockMinVals[keyColIndex[i]];
				}
				
				int cmp = Variant.compareArrays(keys1, vals, keyCount);
				if (cmp >= 0) {
					if (b == 0) {
						return;
					}
					curBlock += (b - 1);
					for (int i = 0; i < colCount; ++i) {
						if (colReaders[i] != null) {
							colReaders[i].seek(lastPos[i]);
						}
					}
					for (int i = 0; i < b - 1; ++i) {
						rowCountReader.readInt32();
						for (int f = 0; f < colCount; ++f) {
							segmentReaders[f].readLong40();
							if (columns[f].hasMaxMinValues()) {
								segmentReaders[f].skipObject();
								segmentReaders[f].skipObject();
								segmentReaders[f].skipObject();
							}
						}
					}
					return;
				}
				
				for (int f = 0; f < colCount; ++f) {
					lastPos[f] = pos[f];
				}
			}
		} catch (IOException e) {
			throw new RQException(e);
		}
		isClosed = true;
	}
	
	protected Sequence get(int n) {
		if (isClosed || n < 1) {
			return null;
		}
		
		if (isNew) {
			return getForNew(n);
		} else {
			return getForNews(n);
		}
	}
	
	/**
	 * T.new��ȡ��
	 * @param n
	 * @return
	 */
	private Sequence getForNew(int n) {
		if (isClosed || n < 1) {
			return null;
		}
		
		if (hasExps) {
			return getData2ForNew(n);
		}
		
		if (filters != null || unknownFilter != null) {
			return getDataForNew(n);
		}

		int keyCount = this.keyCount;
		int csFieldsCount = this.csFieldsCount;
		int len = ds.getFieldCount();
		
		
		Object []keys2 = new Object[keyCount];
		Object []keys1 = this.keys1;
		
		if (cache2 == null || cache2.length() == 0) {
			cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
			cur2 = 1;
			BaseRecord record2 = (BaseRecord) cache2.get(1);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			if (needSkipSeg) {
				skipSegment(keys2);
				needSkipSeg = false;
			}
		}
		
		int cur1 = this.cur1;
		int len1 = this.len1;
		if (cur1 == -1) {
			len1 = loadBlock();
			cur1 = this.cur1;
		}

		BlockLinkReader []colReaders = this.colReaders;
		int colCount = colReaders.length;
		BufferReader []bufReaders = this.bufReaders;
		
		int cur2 = this.cur2;
		Sequence cache2 = this.cache2;
		IArray mems2 = cache2.getMems();
		int len2 = cache2.length();
		int []fieldIndex1 = this.fieldIndex1;
		int []fieldIndex2 = this.fieldIndex2;
		int []keyIndex2 = this.keyIndex2;
		boolean isNew = this.isNew;
		boolean isNews = this.isNews;
		boolean hasR = this.hasR;
		ICursor cursor2 = this.cursor2;
		
		Table newTable;
		if (n > INITSIZE) {
			newTable = new Table(ds, INITSIZE);
		} else {
			newTable = new Table(ds, n);
		}

		try {
			if (keys1 == null) {
				keys1 = new Object[colCount];
				for (int f = 0; f < keyCount; f++) {
					keys1[f] = bufReaders[f].readObject();
				}
			}
			BaseRecord record2 = (BaseRecord) mems2.get(cur2);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			
			while (true) {
				int cmp = Variant.compareArrays(keys2, keys1);
				if (cmp == 0) {
					for (int f = keyCount; f < colCount; f++) {
						keys1[f] = bufReaders[f].readObject();
					}
					
					BaseRecord record = newTable.newLast();
					if (isNew || isNews) {
						for (int i = 0; i < len; i++) {
							int idx = fieldIndex1[i];
							if (idx < 0)
								record.setNormalFieldValue(i, record2.getFieldValue(fieldIndex2[i]));
							else 
								record.setNormalFieldValue(i, keys1[idx]);
						}
					} else {
						Object []vals = record2.getFieldValues();
						System.arraycopy(vals, 0, record.getFieldValues(), 0, csFieldsCount);
						for (int i = 0; i < len; i++) {
							int idx = fieldIndex1[i];
							if (idx < 0)
								record.setNormalFieldValue(i + csFieldsCount, record2.getFieldValue(fieldIndex2[i]));
							else 
								record.setNormalFieldValue(i + csFieldsCount, keys1[fieldIndex1[i]]);
						}
					}
					

					if (hasR) {
						//����һ��ȡ��
						cur2++;
						if (cur2 > len2) {
							cur2 = 1;
							cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
							if (cache2 == null || cache2.length() == 0) {
								isClosed = true;
								break;
							}
							mems2 = cache2.getMems();
							len2 = cache2.length();
						}
						record2 = (BaseRecord) mems2.get(cur2);
						for (int i = 0; i < keyCount; i++) {
							keys2[i] = record2.getFieldValue(keyIndex2[i]);
						}
						
						while(0 == Variant.compareArrays(keys2, keys1)) {
							record = newTable.newLast();
							for (int i = 0; i < len; i++) {
								int idx = fieldIndex1[i];
								if (idx < 0)
									record.setNormalFieldValue(i, record2.getFieldValue(fieldIndex2[i]));
								else 
									record.setNormalFieldValue(i, keys1[idx]);
							}
							cur2++;
							if (cur2 > len2) {
								cur2 = 1;
								cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
								if (cache2 == null || cache2.length() == 0) {
									isClosed = true;
									break;
								}
								mems2 = cache2.getMems();
								len2 = cache2.length();
							}
							record2 = (BaseRecord) mems2.get(cur2);
							for (int i = 0; i < keyCount; i++) {
								keys2[i] = record2.getFieldValue(keyIndex2[i]);
							}
						}
					}

					cur1++;
					if (cur1 > len1) {
						cur1 = 1;
						len1 = loadBlock();
						colReaders = this.colReaders;
						if (len1 < 0) {
							isClosed = true;
							break;
						}
					}
					for (int f = 0; f < keyCount; f++) {
						keys1[f] = bufReaders[f].readObject();
					}
					
				} else if (cmp > 0) {
					for (int f = keyCount; f < colCount; f++) {
						bufReaders[f].skipObject();
					}
					cur1++;
					if (cur1 > len1) {
						cur1 = 1;
						len1 = loadBlock();
						colReaders = this.colReaders;
						if (len1 < 0) {
							isClosed = true;
							break;
						}
					}
					
					for (int f = 0; f < keyCount; f++) {
						keys1[f] = bufReaders[f].readObject();
					}
				} else if (cmp < 0) {
					cur2++;
					if (cur2 > len2) {
						cur2 = 1;
						cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
						if (cache2 == null || cache2.length() == 0) {
							isClosed = true;
							break;
						}
						mems2 = cache2.getMems();
						len2 = cache2.length();
					}
					record2 = (BaseRecord) mems2.get(cur2);
					for (int i = 0; i < keyCount; i++) {
						keys2[i] = record2.getFieldValue(keyIndex2[i]);
					}
				}
				
				if (newTable.length() >= n) {
					break;
				}
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
		
		this.len1 = len1;
		this.keys1 = keys1;
		this.colReaders = colReaders;
		this.bufReaders = bufReaders;
		this.cache2 = cache2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		
		if (newTable.length() > 0) {
			return newTable;
		} else {
			return null;
		}
	}

	/**
	 * T.new��filtersʱ��ȡ��¼(�޾ۺ�)
	 * @param n
	 * @return
	 */
	private Sequence getDataForNew(int n) {
		if (isClosed || n < 1) {
			return null;
		}

		int keyCount = this.keyCount;
		int len = ds.getFieldCount();
		
		Object []keys2 = new Object[keyCount];
		Object []keys1 = this.keys1;
		
		//ȡ����һ��cs������
		if (cache2 == null || cache2.length() == 0) {
			cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
			cur2 = 1;
			BaseRecord record2 = (BaseRecord) cache2.get(1);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			if (needSkipSeg) {
				skipSegment(keys2);
				needSkipSeg = false;
			}
		}
		
		int cur1 = this.cur1;
		int len1 = this.len1;
		if (cur1 == -1) {
			len1 = loadBlock();
			cur1 = this.cur1;
		}

		BlockLinkReader []colReaders = this.colReaders;
		int colCount = colReaders.length;
		BufferReader []bufReaders = this.bufReaders;
		
		int cur2 = this.cur2;
		Sequence cache2 = this.cache2;
		IArray mems2 = cache2.getMems();
		int len2 = cache2.length();
		int []fieldIndex1 = this.fieldIndex1;
		int []fieldIndex2 = this.fieldIndex2;
		int []keyIndex2 = this.keyIndex2;
		boolean hasR = this.hasR;
		ICursor cursor2 = this.cursor2;
		
		Context ctx = this.ctx;
		Expression unknownFilter = this.unknownFilter;
		ComputeStack stack = null;
		
		IFilter []filters = this.filters;
		int filterAllCount = filters == null ? 0 : filters.length;
		int keyOffset = this.keyOffset;
		int valueOffset = keyOffset + keyColCount;
		int keyColIndex[] = this.keyColIndex;
		FindFilter []findFilters = this.findFilters;
		BaseRecord r = this.r;
		Object objs[] = r == null ? null : r.getFieldValues();
		
		if (r != null && unknownFilter != null) {
			stack = ctx.getComputeStack();
			stack.push(r);
		}
		
		Table newTable;
		if (n > INITSIZE) {
			newTable = new Table(ds, INITSIZE);
		} else {
			newTable = new Table(ds, n);
		}

		try {
			if (keys1 == null) {
				keys1 = new Object[keyCount];
				r = new Record(ds1);
				objs = r.getFieldValues();
				if (unknownFilter != null) {
					stack = ctx.getComputeStack();
					stack.push(r);
				}
				
				//���˳�һ������������
				while (true) {
					//���filter
					boolean flag = true;
					int f = 0;
					//�ȶ�ȡfilter��
					for (; f < filterAllCount; f++) {
						objs[f] = bufReaders[f].readObject();
						flag = filters[f].match(objs[f]);
						if (!flag) {
							f++;
							break;
						}
						if (findFilters != null && findFilters[f] != null) {
							objs[f] = findFilters[f].getFindResult();
						}
					}
					if (flag && unknownFilter != null) {
						//��ƥ��,��Ҫ���unknownFilter
						for (; f < keyOffset; f++) {
							objs[f] = bufReaders[f].readObject();
						}
						flag = Variant.isTrue(unknownFilter.calculate(ctx));
					}
					if (flag) {
						//��ȡʣ��key�ֶ�,��֯keys1
						for (; f < valueOffset; f++) {
							objs[f] = bufReaders[f].readObject();
						}
						for (int i = 0; i < keyCount; i++) {
							keys1[i] = objs[keyColIndex[i]];
						}
						break;
					} else {
						//���������
						for (; f < colCount; f++) {
							 bufReaders[f].skipObject();
						}
					}
					
					//����һ��
					cur1++;
					if (cur1 > len1) {
						cur1 = 1;
						len1 = loadBlock();
						colReaders = this.colReaders;
						if (len1 < 0) {
							isClosed = true;
							return null;
						}
					}
				}
			}
			
			BaseRecord record2 = (BaseRecord) mems2.get(cur2);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			
			EXIT:
			while (true) {
				int cmp = Variant.compareArrays(keys2, keys1);
				if (cmp == 0) {
					//��ȡʣ���ֶ�
					for (int f = valueOffset; f < colCount; f++) {
						objs[f] = bufReaders[f].readObject();
					}
					BaseRecord record = newTable.newLast();
					for (int i = 0; i < len; i++) {
						int idx = fieldIndex1[i];
						if (idx < 0)
							record.setNormalFieldValue(i, record2.getFieldValue(fieldIndex2[i]));
						else 
							record.setNormalFieldValue(i, objs[idx]);
					}

					if (hasR) {
						//����һ��ȡ��
						cur2++;
						if (cur2 > len2) {
							cur2 = 1;
							cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
							if (cache2 == null || cache2.length() == 0) {
								isClosed = true;
								break;
							}
							mems2 = cache2.getMems();
							len2 = cache2.length();
						}
						record2 = (BaseRecord) mems2.get(cur2);
						for (int i = 0; i < keyCount; i++) {
							keys2[i] = record2.getFieldValue(keyIndex2[i]);
						}
						
						while(0 == Variant.compareArrays(keys2, keys1)) {
							record = newTable.newLast();
							for (int i = 0; i < len; i++) {
								int idx = fieldIndex1[i];
								if (idx < 0)
									record.setNormalFieldValue(i, record2.getFieldValue(fieldIndex2[i]));
								else 
									record.setNormalFieldValue(i, objs[idx]);
							}
							cur2++;
							if (cur2 > len2) {
								cur2 = 1;
								cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
								if (cache2 == null || cache2.length() == 0) {
									isClosed = true;
									break;
								}
								mems2 = cache2.getMems();
								len2 = cache2.length();
							}
							record2 = (BaseRecord) mems2.get(cur2);
							for (int i = 0; i < keyCount; i++) {
								keys2[i] = record2.getFieldValue(keyIndex2[i]);
							}
						}
					}
				
					
					//ȡ����һ������������
					while (true) {
						cur1++;
						if (cur1 > len1) {
							cur1 = 1;
							len1 = loadBlock();
							colReaders = this.colReaders;
							if (len1 < 0) {
								isClosed = true;
								break EXIT;
							}
						}
						//���filter
						boolean flag = true;
						int f = 0;
						//�ȶ�ȡfilter��
						for (; f < filterAllCount; f++) {
							objs[f] = bufReaders[f].readObject();
							flag = filters[f].match(objs[f]);
							if (!flag) {
								f++;
								break;
							}
							if (findFilters != null && findFilters[f] != null) {
								objs[f] = findFilters[f].getFindResult();
							}
						}
						if (flag && unknownFilter != null) {
							//��ƥ��,��Ҫ���unknownFilter
							for (; f < keyOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							flag = Variant.isTrue(unknownFilter.calculate(ctx));
						}
						if (flag) {
							//��ȡʣ��key�ֶ�,��֯keys1
							for (; f < valueOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							for (int i = 0; i < keyCount; i++) {
								keys1[i] = objs[keyColIndex[i]];
							}
							break;
						} else {
							//���������
							for (; f < colCount; f++) {
								 bufReaders[f].skipObject();
							}
						}
					}
				} else if (cmp > 0) {
					//��������
					for (int f = valueOffset; f < colCount; f++) {
						bufReaders[f].skipObject();
					}
					//ȡ����һ������������
					while (true) {
						cur1++;
						if (cur1 > len1) {
							cur1 = 1;
							len1 = loadBlock();
							colReaders = this.colReaders;
							if (len1 < 0) {
								isClosed = true;
								break EXIT;
							}
						}
						//���filter
						boolean flag = true;
						int f = 0;
						//�ȶ�ȡfilter��
						for (; f < filterAllCount; f++) {
							objs[f] = bufReaders[f].readObject();
							flag = filters[f].match(objs[f]);
							if (!flag) {
								f++;
								break;
							}
							if (findFilters != null && findFilters[f] != null) {
								objs[f] = findFilters[f].getFindResult();
							}
						}
						if (flag && unknownFilter != null) {
							//��ƥ��,��Ҫ���unknownFilter
							for (; f < keyOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							flag = Variant.isTrue(unknownFilter.calculate(ctx));
						}
						if (flag) {
							//��ȡʣ��key�ֶ�,��֯keys1
							for (; f < valueOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							for (int i = 0; i < keyCount; i++) {
								keys1[i] = objs[keyColIndex[i]];
							}
							break;
						} else {
							//���������
							for (; f < colCount; f++) {
								 bufReaders[f].skipObject();
							}
						}
					}
				} else if (cmp < 0) {
					cur2++;
					if (cur2 > len2) {
						cur2 = 1;
						cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
						if (cache2 == null || cache2.length() == 0) {
							isClosed = true;
							break;
						}
						mems2 = cache2.getMems();
						len2 = cache2.length();
					}
					record2 = (BaseRecord) mems2.get(cur2);
					for (int i = 0; i < keyCount; i++) {
						keys2[i] = record2.getFieldValue(keyIndex2[i]);
					}
				}
				
				if (newTable.length() >= n) {
					break;
				}
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			if (stack != null) 
				stack.pop();
		}
		
		this.len1 = len1;
		this.keys1 = keys1;
		this.colReaders = colReaders;
		this.bufReaders = bufReaders;
		this.cache2 = cache2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.r = r;
		
		if (newTable.length() > 0) {
			return newTable;
		} else {
			return null;
		}
	}

	/**
	 * T.newȡ���ֶ��Ǳ��ʽʱ
	 * @param n
	 * @return
	 */
	private Sequence getData2ForNew(int n) {
		if (isClosed || n < 1) {
			return null;
		}

		int keyCount = this.keyCount;
		int len = ds.getFieldCount();
		
		Object []keys2 = new Object[keyCount];
		Object []keys1 = this.keys1;
		
		//ȡ����һ��cs������
		if (cache2 == null || cache2.length() == 0) {
			cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
			cur2 = 1;
			BaseRecord record2 = (BaseRecord) cache2.get(1);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			if (needSkipSeg) {
				skipSegment(keys2);
				needSkipSeg = false;
			}
		}
		
		int cur1 = this.cur1;
		int len1 = this.len1;
		if (cur1 == -1) {
			len1 = loadBlock();
			cur1 = this.cur1;
		}

		BlockLinkReader []colReaders = this.colReaders;
		int colCount = colReaders.length;
		BufferReader []bufReaders = this.bufReaders;
		
		int cur2 = this.cur2;
		Sequence cache2 = this.cache2;
		IArray mems2 = cache2.getMems();
		int len2 = cache2.length();
		int []keyIndex2 = this.keyIndex2;
		ICursor cursor2 = this.cursor2;
		
		IFilter []filters = this.filters;
		int filterAllCount = filters == null ? 0 : filters.length;
		int keyOffset = this.keyOffset;
		int valueOffset = keyOffset + keyColCount;
		int keyColIndex[] = this.keyColIndex;
		FindFilter []findFilters = this.findFilters;
		BaseRecord r = this.r;
		Object objs[] = r == null ? null : r.getFieldValues();
		Context ctx = this.ctx;
		Expression unknownFilter = this.unknownFilter;
		ComputeStack stack = null;
		if (r != null && unknownFilter != null) {
			stack = ctx.getComputeStack();
			stack.push(r);
		}
		
		Table newTable;
		if (n > INITSIZE) {
			newTable = new Table(ds, INITSIZE);
		} else {
			newTable = new Table(ds, n);
		}

		Table tempTable = new Table(cache2.dataStruct());//���ڻ���
		
		try {
			if (keys1 == null) {
				keys1 = new Object[keyCount];
				r = new Record(ds1);
				objs = r.getFieldValues();
				if (unknownFilter != null) {
					stack = ctx.getComputeStack();
					stack.push(r);
				}
				
				//���˳�һ������������
				while (true) {
					//���filter
					boolean flag = true;
					int f = 0;
					//�ȶ�ȡfilter��
					for (; f < filterAllCount; f++) {
						objs[f] = bufReaders[f].readObject();
						flag = filters[f].match(objs[f]);
						if (!flag) {
							f++;
							break;
						}
						if (findFilters != null && findFilters[f] != null) {
							objs[f] = findFilters[f].getFindResult();
						}
					}
					if (flag && unknownFilter != null) {
						//��ƥ��,��Ҫ���unknownFilter
						for (; f < keyOffset; f++) {
							objs[f] = bufReaders[f].readObject();
						}
						flag = Variant.isTrue(unknownFilter.calculate(ctx));
					}
					if (flag) {
						//��ȡʣ��key�ֶ�,��֯keys1
						for (; f < valueOffset; f++) {
							objs[f] = bufReaders[f].readObject();
						}
						for (int i = 0; i < keyCount; i++) {
							keys1[i] = objs[keyColIndex[i]];
						}
						break;
					} else {
						//���������
						for (; f < colCount; f++) {
							 bufReaders[f].skipObject();
						}
					}
					
					//����һ��
					cur1++;
					if (cur1 > len1) {
						cur1 = 1;
						len1 = loadBlock();
						colReaders = this.colReaders;
						if (len1 < 0) {
							isClosed = true;
							return null;
						}
					}
				}
			}
			BaseRecord record2 = (BaseRecord) mems2.get(cur2);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			
			EXIT:
			while (true) {
				int cmp = Variant.compareArrays(keys2, keys1);
				if (cmp == 0) {
					//����һ��������ʱ����table
					tempTable.add(record2);
					
					cur2++;
					if (cur2 > len2) {
						cur2 = 1;
						cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
						if (cache2 == null || cache2.length() == 0) {
							isClosed = true;
							break;
						}
						mems2 = cache2.getMems();
						len2 = cache2.length();
					}
					record2 = (BaseRecord) mems2.get(cur2);
					for (int i = 0; i < keyCount; i++) {
						keys2[i] = record2.getFieldValue(keyIndex2[i]);
					}
					
					if (0 != Variant.compareArrays(keys2, keys1)) {
						//��ȡʣ���ֶ�
						for (int f = valueOffset; f < colCount; f++) {
							objs[f] = bufReaders[f].readObject();
						}
						//�������ȣ���ʾ��һ��ȡ���ˣ�������ʱ��������
						BaseRecord record = newTable.newLast();
						calcExpsForNew(record, tempTable, r, len);
						
						//ȡ����һ������������
						while (true) {
							cur1++;
							if (cur1 > len1) {
								cur1 = 1;
								len1 = loadBlock();
								colReaders = this.colReaders;
								if (len1 < 0) {
									isClosed = true;
									break EXIT;
								}
							}
							//���filter
							boolean flag = true;
							int f = 0;
							//�ȶ�ȡfilter��
							for (; f < filterAllCount; f++) {
								objs[f] = bufReaders[f].readObject();
								flag = filters[f].match(objs[f]);
								if (!flag) {
									f++;
									break;
								}
								if (findFilters != null && findFilters[f] != null) {
									objs[f] = findFilters[f].getFindResult();
								}
							}
							if (flag && unknownFilter != null) {
								//��ƥ��,��Ҫ���unknownFilter
								for (; f < keyOffset; f++) {
									objs[f] = bufReaders[f].readObject();
								}
								flag = Variant.isTrue(unknownFilter.calculate(ctx));
							}
							if (flag) {
								//��ȡʣ��key�ֶ�,��֯keys1
								for (; f < valueOffset; f++) {
									objs[f] = bufReaders[f].readObject();
								}
								for (int i = 0; i < keyCount; i++) {
									keys1[i] = objs[keyColIndex[i]];
								}
								break;
							} else {
								//���������
								for (; f < colCount; f++) {
									 bufReaders[f].skipObject();
								}
							}
						}
					}
				} else if (cmp > 0) {
					//��������
					for (int f = valueOffset; f < colCount; f++) {
						bufReaders[f].skipObject();
					}
					//ȡ����һ������������
					while (true) {
						cur1++;
						if (cur1 > len1) {
							cur1 = 1;
							len1 = loadBlock();
							colReaders = this.colReaders;
							if (len1 < 0) {
								isClosed = true;
								break EXIT;
							}
						}
						//���filter
						boolean flag = true;
						int f = 0;
						//�ȶ�ȡfilter��
						for (; f < filterAllCount; f++) {
							objs[f] = bufReaders[f].readObject();
							flag = filters[f].match(objs[f]);
							if (!flag) {
								f++;
								break;
							}
							if (findFilters != null && findFilters[f] != null) {
								objs[f] = findFilters[f].getFindResult();
							}
						}
						if (flag && unknownFilter != null) {
							//��ƥ��,��Ҫ���unknownFilter
							for (; f < keyOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							flag = Variant.isTrue(unknownFilter.calculate(ctx));
						}
						if (flag) {
							//��ȡʣ��key�ֶ�,��֯keys1
							for (; f < valueOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							for (int i = 0; i < keyCount; i++) {
								keys1[i] = objs[keyColIndex[i]];
							}
							break;
						} else {
							//���������
							for (; f < colCount; f++) {
								 bufReaders[f].skipObject();
							}
						}
					}
				} else if (cmp < 0) {
					cur2++;
					if (cur2 > len2) {
						cur2 = 1;
						cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
						if (cache2 == null || cache2.length() == 0) {
							isClosed = true;
							break;
						}
						mems2 = cache2.getMems();
						len2 = cache2.length();
					}
					record2 = (BaseRecord) mems2.get(cur2);
					for (int i = 0; i < keyCount; i++) {
						keys2[i] = record2.getFieldValue(keyIndex2[i]);
					}
				}
				
				if (newTable.length() == n) {
					break;
				}
			}
			
			if (isClosed && tempTable != null && tempTable.length() != 0) {
				//��ȡʣ���ֶ�
				for (int f = valueOffset; f < colCount; f++) {
					objs[f] = bufReaders[f].readObject();
				}
				//�������ȣ���ʾ��һ��ȡ���ˣ�������ʱ��������
				BaseRecord record = newTable.newLast();
				calcExpsForNew(record, tempTable, r, len);
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			if (stack != null) 
				stack.pop();
		}
		
		this.len1 = len1;
		this.keys1 = keys1;
		this.colReaders = colReaders;
		this.bufReaders = bufReaders;
		this.cache2 = cache2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.r = r;
		
		if (newTable.length() > 0) {
			return newTable;
		} else {
			return null;
		}
	}

	/**
	 * T.news��ȡ��
	 * @param n
	 * @return
	 */
	private Sequence getForNews(int n) {
		if (isClosed || n < 1) {
			return null;
		}

		if (filters != null || unknownFilter != null) {
			return getDataForNews(n);
		}

		int keyCount = this.keyCount;
		int len = ds.getFieldCount();
		
		
		Object []keys2 = new Object[keyCount];
		Object []keys1 = this.keys1;
		
		if (cache2 == null || cache2.length() == 0) {
			cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
			cur2 = 1;
			BaseRecord record2 = (BaseRecord) cache2.get(1);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			if (needSkipSeg) {
				skipSegment(keys2);
				needSkipSeg = false;
			}
		}
		
		int cur1 = this.cur1;
		int len1 = this.len1;
		if (cur1 == -1) {
			len1 = loadBlock();
			cur1 = this.cur1;
		}

		BlockLinkReader []colReaders = this.colReaders;
		int colCount = colReaders.length;
		BufferReader []bufReaders = this.bufReaders;
		
		int cur2 = this.cur2;
		Sequence cache2 = this.cache2;
		IArray mems2 = cache2.getMems();
		int len2 = cache2.length();
		int []fieldIndex1 = this.fieldIndex1;
		int []fieldIndex2 = this.fieldIndex2;
		int []keyIndex2 = this.keyIndex2;
		boolean hasR = this.hasR;
		boolean hasExps = this.hasExps;
		ICursor cursor2 = this.cursor2;
		
		Table newTable;
		if (n > INITSIZE) {
			newTable = new Table(ds, INITSIZE);
		} else {
			newTable = new Table(ds, n);
		}
		Table tempTable = new Table(ds1);//���ڻ���
		
		try {
			if (keys1 == null) {
				keys1 = new Object[colCount];
				for (int f = 0; f < keyCount; f++) {
					keys1[f] = bufReaders[f].readObject();
				}
			}
			BaseRecord record2 = (BaseRecord) mems2.get(cur2);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			
			while (true) {
				int cmp = Variant.compareArrays(keys2, keys1);
				if (cmp == 0) {
					for (int f = keyCount; f < colCount; f++) {
						keys1[f] = bufReaders[f].readObject();
					}
					
					BaseRecord record = newTable.newLast();
					if (hasExps) {
						tempTable.newLast(keys1);//��ӵ���ʱ����
					} else {
						for (int i = 0; i < len; i++) {
							int idx = fieldIndex1[i];
							if (idx < 0)
								record.setNormalFieldValue(i, record2.getFieldValue(fieldIndex2[i]));
							else 
								record.setNormalFieldValue(i, keys1[idx]);
						}
					}
					
					cur1++;
					if (cur1 > len1) {
						cur1 = 1;
						len1 = loadBlock();
						colReaders = this.colReaders;
						if (len1 < 0) {
							isClosed = true;
							break;
						}
					}
					for (int f = 0; f < keyCount; f++) {
						keys1[f] = bufReaders[f].readObject();
					}
				
					if (hasR) {
						if (hasExps) {
							//����һ��ȡ��
							while(Variant.compareArrays(keys2, keys1) == 0) {
								for (int f = keyCount; f < colCount; f++) {
									keys1[f] = bufReaders[f].readObject();
								}
								tempTable.newLast(keys1);//��ӵ���ʱ����
								cur1++;
								if (cur1 > len1) {
									cur1 = 1;
									len1 = loadBlock();
									colReaders = this.colReaders;
									if (len1 < 0) {
										isClosed = true;
										break;
									}
								}
								for (int f = 0; f < keyCount; f++) {
									keys1[f] = bufReaders[f].readObject();
								}
							}
							
							calcExpsForNews(record, tempTable, record2, len);
						}
						
						//����cs����
						cur2++;
						if (cur2 > len2) {
							cur2 = 1;
							cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
							if (cache2 == null || cache2.length() == 0) {
								isClosed = true;
								break;
							}
							mems2 = cache2.getMems();
							len2 = cache2.length();
						}
						record2 = (BaseRecord) mems2.get(cur2);
						for (int i = 0; i < keyCount; i++) {
							keys2[i] = record2.getFieldValue(keyIndex2[i]);
						}
					}
				} else if (cmp > 0) {
					for (int f = keyCount; f < colCount; f++) {
						bufReaders[f].skipObject();
					}
					cur1++;
					if (cur1 > len1) {
						cur1 = 1;
						len1 = loadBlock();
						colReaders = this.colReaders;
						if (len1 < 0) {
							isClosed = true;
							break;
						}
					}
					
					for (int f = 0; f < keyCount; f++) {
						keys1[f] = bufReaders[f].readObject();
					}
				} else if (cmp < 0) {
					cur2++;
					if (cur2 > len2) {
						cur2 = 1;
						cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
						if (cache2 == null || cache2.length() == 0) {
							isClosed = true;
							break;
						}
						mems2 = cache2.getMems();
						len2 = cache2.length();
					}
					record2 = (BaseRecord) mems2.get(cur2);
					for (int i = 0; i < keyCount; i++) {
						keys2[i] = record2.getFieldValue(keyIndex2[i]);
					}
				}
				
				if (newTable.length() >= n) {
					break;
				}
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
		
		this.len1 = len1;
		this.keys1 = keys1;
		this.colReaders = colReaders;
		this.bufReaders = bufReaders;
		this.cache2 = cache2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		
		if (newTable.length() > 0) {
			return newTable;
		} else {
			return null;
		}
	}
	
	/**
	 * T.news��filtersʱ��ȡ��¼
	 * @param n
	 * @return
	 */
	private Sequence getDataForNews(int n) {
		if (isClosed || n < 1) {
			return null;
		}

		int keyCount = this.keyCount;
		int len = ds.getFieldCount();
		
		Object []keys2 = new Object[keyCount];
		Object []keys1 = this.keys1;
		
		//ȡ����һ��cs������
		if (cache2 == null || cache2.length() == 0) {
			cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
			cur2 = 1;
			BaseRecord record2 = (BaseRecord) cache2.get(1);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			if (needSkipSeg) {
				skipSegment(keys2);
				needSkipSeg = false;
			}
		}
		
		int cur1 = this.cur1;
		int len1 = this.len1;
		if (cur1 == -1) {
			len1 = loadBlock();
			cur1 = this.cur1;
		}

		BlockLinkReader []colReaders = this.colReaders;
		int colCount = colReaders.length;
		BufferReader []bufReaders = this.bufReaders;
		
		int cur2 = this.cur2;
		Sequence cache2 = this.cache2;
		IArray mems2 = cache2.getMems();
		int len2 = cache2.length();
		int []fieldIndex1 = this.fieldIndex1;
		int []fieldIndex2 = this.fieldIndex2;
		int []keyIndex2 = this.keyIndex2;
		boolean hasR = this.hasR;
		boolean hasExps = this.hasExps;
		ICursor cursor2 = this.cursor2;
		
		Context ctx = this.ctx;
		Expression unknownFilter = this.unknownFilter;
		ComputeStack stack = null;
		
		IFilter []filters = this.filters;
		int filterAllCount = filters == null ? 0 : filters.length;
		int keyOffset = this.keyOffset;
		int valueOffset = keyOffset + keyColCount;
		int keyColIndex[] = this.keyColIndex;
		FindFilter []findFilters = this.findFilters;
		BaseRecord r = this.r;
		Object objs[] = r == null ? null : r.getFieldValues();
		
		if (r != null && unknownFilter != null) {
			stack = ctx.getComputeStack();
			stack.push(r);
		}
		
		Table newTable;
		if (n > INITSIZE) {
			newTable = new Table(ds, INITSIZE);
		} else {
			newTable = new Table(ds, n);
		}
		Table tempTable = new Table(ds1);//���ڻ���

		try {
			if (keys1 == null) {
				keys1 = new Object[keyCount];
				r = new Record(ds1);
				objs = r.getFieldValues();
				if (unknownFilter != null) {
					stack = ctx.getComputeStack();
					stack.push(r);
				}
				
				//���˳�һ������������
				while (true) {
					//���filter
					boolean flag = true;
					int f = 0;
					//�ȶ�ȡfilter��
					for (; f < filterAllCount; f++) {
						objs[f] = bufReaders[f].readObject();
						flag = filters[f].match(objs[f]);
						if (!flag) {
							f++;
							break;
						}
						if (findFilters != null && findFilters[f] != null) {
							objs[f] = findFilters[f].getFindResult();
						}
					}
					if (flag && unknownFilter != null) {
						//��ƥ��,��Ҫ���unknownFilter
						for (; f < keyOffset; f++) {
							objs[f] = bufReaders[f].readObject();
						}
						flag = Variant.isTrue(unknownFilter.calculate(ctx));
					}
					if (flag) {
						//��ȡʣ��key�ֶ�,��֯keys1
						for (; f < valueOffset; f++) {
							objs[f] = bufReaders[f].readObject();
						}
						for (int i = 0; i < keyCount; i++) {
							keys1[i] = objs[keyColIndex[i]];
						}
						break;
					} else {
						//���������
						for (; f < colCount; f++) {
							 bufReaders[f].skipObject();
						}
					}
					
					//����һ��
					cur1++;
					if (cur1 > len1) {
						cur1 = 1;
						len1 = loadBlock();
						colReaders = this.colReaders;
						if (len1 < 0) {
							isClosed = true;
							return null;
						}
					}
				}
			}
			
			BaseRecord record2 = (BaseRecord) mems2.get(cur2);
			for (int i = 0; i < keyCount; i++) {
				keys2[i] = record2.getFieldValue(keyIndex2[i]);
			}
			
			EXIT:
			while (true) {
				int cmp = Variant.compareArrays(keys2, keys1);
				if (cmp == 0) {
					//��ȡʣ���ֶ�
					for (int f = valueOffset; f < colCount; f++) {
						objs[f] = bufReaders[f].readObject();
					}
					BaseRecord record = newTable.newLast();
					if (hasExps) {
						tempTable.newLast(objs);//��ӵ���ʱ����
					} else {
						for (int i = 0; i < len; i++) {
							int idx = fieldIndex1[i];
							if (idx < 0)
								record.setNormalFieldValue(i, record2.getFieldValue(fieldIndex2[i]));
							else 
								record.setNormalFieldValue(i, objs[idx]);
						}
					}
					
					//ȡ����һ������������
					while (true) {
						cur1++;
						if (cur1 > len1) {
							cur1 = 1;
							len1 = loadBlock();
							colReaders = this.colReaders;
							if (len1 < 0) {
								isClosed = true;
								break EXIT;
							}
						}
						//���filter
						boolean flag = true;
						int f = 0;
						//�ȶ�ȡfilter��
						for (; f < filterAllCount; f++) {
							objs[f] = bufReaders[f].readObject();
							flag = filters[f].match(objs[f]);
							if (!flag) {
								f++;
								break;
							}
							if (findFilters != null && findFilters[f] != null) {
								objs[f] = findFilters[f].getFindResult();
							}
						}
						if (flag && unknownFilter != null) {
							//��ƥ��,��Ҫ���unknownFilter
							for (; f < keyOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							flag = Variant.isTrue(unknownFilter.calculate(ctx));
						}
						if (flag) {
							//��ȡʣ��key�ֶ�,��֯keys1
							for (; f < valueOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							for (int i = 0; i < keyCount; i++) {
								keys1[i] = objs[keyColIndex[i]];
							}
							break;
						} else {
							//���������
							for (; f < colCount; f++) {
								 bufReaders[f].skipObject();
							}
						}
					}
					
					if (hasR) {
						if (hasExps) {
							//����һ��ȡ��
							while(Variant.compareArrays(keys2, keys1) == 0) {
								//��ȡʣ���ֶ�
								for (int f = valueOffset; f < colCount; f++) {
									objs[f] = bufReaders[f].readObject();
								}
								tempTable.newLast(objs);//��ӵ���ʱ����
								//ȡ����һ������������
								while (true) {
									cur1++;
									if (cur1 > len1) {
										cur1 = 1;
										len1 = loadBlock();
										colReaders = this.colReaders;
										if (len1 < 0) {
											isClosed = true;
											break EXIT;
										}
									}
									//���filter
									boolean flag = true;
									int f = 0;
									//�ȶ�ȡfilter��
									for (; f < filterAllCount; f++) {
										objs[f] = bufReaders[f].readObject();
										flag = filters[f].match(objs[f]);
										if (!flag) {
											f++;
											break;
										}
										if (findFilters != null && findFilters[f] != null) {
											objs[f] = findFilters[f].getFindResult();
										}
									}
									if (flag && unknownFilter != null) {
										//��ƥ��,��Ҫ���unknownFilter
										for (; f < keyOffset; f++) {
											objs[f] = bufReaders[f].readObject();
										}
										flag = Variant.isTrue(unknownFilter.calculate(ctx));
									}
									if (flag) {
										//��ȡʣ��key�ֶ�,��֯keys1
										for (; f < valueOffset; f++) {
											objs[f] = bufReaders[f].readObject();
										}
										for (int i = 0; i < keyCount; i++) {
											keys1[i] = objs[keyColIndex[i]];
										}
										break;
									} else {
										//���������
										for (; f < colCount; f++) {
											 bufReaders[f].skipObject();
										}
									}
								}
							}
							calcExpsForNews(record, tempTable, record2, len);
						}
						
						//����cs����
						cur2++;
						if (cur2 > len2) {
							cur2 = 1;
							cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
							if (cache2 == null || cache2.length() == 0) {
								isClosed = true;
								break;
							}
							mems2 = cache2.getMems();
							len2 = cache2.length();
						}
						record2 = (BaseRecord) mems2.get(cur2);
						for (int i = 0; i < keyCount; i++) {
							keys2[i] = record2.getFieldValue(keyIndex2[i]);
						}
					}
				} else if (cmp > 0) {
					//��������
					for (int f = valueOffset; f < colCount; f++) {
						bufReaders[f].skipObject();
					}
					//ȡ����һ������������
					while (true) {
						cur1++;
						if (cur1 > len1) {
							cur1 = 1;
							len1 = loadBlock();
							colReaders = this.colReaders;
							if (len1 < 0) {
								isClosed = true;
								break EXIT;
							}
						}
						//���filter
						boolean flag = true;
						int f = 0;
						//�ȶ�ȡfilter��
						for (; f < filterAllCount; f++) {
							objs[f] = bufReaders[f].readObject();
							flag = filters[f].match(objs[f]);
							if (!flag) {
								f++;
								break;
							}
							if (findFilters != null && findFilters[f] != null) {
								objs[f] = findFilters[f].getFindResult();
							}
						}
						if (flag && unknownFilter != null) {
							//��ƥ��,��Ҫ���unknownFilter
							for (; f < keyOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							flag = Variant.isTrue(unknownFilter.calculate(ctx));
						}
						if (flag) {
							//��ȡʣ��key�ֶ�,��֯keys1
							for (; f < valueOffset; f++) {
								objs[f] = bufReaders[f].readObject();
							}
							for (int i = 0; i < keyCount; i++) {
								keys1[i] = objs[keyColIndex[i]];
							}
							break;
						} else {
							//���������
							for (; f < colCount; f++) {
								 bufReaders[f].skipObject();
							}
						}
					}
				} else if (cmp < 0) {
					cur2++;
					if (cur2 > len2) {
						cur2 = 1;
						cache2 = cursor2.fetch(ICursor.FETCHCOUNT);
						if (cache2 == null || cache2.length() == 0) {
							isClosed = true;
							break;
						}
						mems2 = cache2.getMems();
						len2 = cache2.length();
					}
					record2 = (BaseRecord) mems2.get(cur2);
					for (int i = 0; i < keyCount; i++) {
						keys2[i] = record2.getFieldValue(keyIndex2[i]);
					}
				}
				
				if (newTable.length() >= n) {
					break;
				}
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			if (stack != null) 
				stack.pop();
		}
		
		this.len1 = len1;
		this.keys1 = keys1;
		this.colReaders = colReaders;
		this.bufReaders = bufReaders;
		this.cache2 = cache2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.r = r;
		
		if (newTable.length() > 0) {
			return newTable;
		} else {
			return null;
		}
	}
	
	/**
	 * ����������¼���Լ�ͬ�����ݣ�������ʽ
	 * @param record
	 * @param tempTable ������ͬ��һ������
	 * @param r
	 * @param len
	 */
	private void calcExpsForNew(BaseRecord record, Table tempTable, BaseRecord r, int len) {
		Node nodes[] = this.nodes;
		int fieldIndex2[] = this.fieldIndex2;
		for (int i = 0; i < len; i++) {
			Node node = nodes[i];
			if (node instanceof FieldRef) {
				if (fieldIndex2[i] < 0) {
					node.setDotLeftObject(r);
				} else {
					node.setDotLeftObject(tempTable.get(1));
				}
			} else {
				node.setDotLeftObject(tempTable);
			}
			record.setNormalFieldValue(i, node.calculate(ctx));
		}
	
		tempTable.clear();
	}

	private void calcExpsForNews(BaseRecord record, Table tempTable, BaseRecord r, int len) {
		Node nodes[] = this.nodes;
		for (int i = 0; i < len; i++) {
			Node node = nodes[i];
			if (node instanceof FieldRef) {
				node.setDotLeftObject(r);
			} else {
				node.setDotLeftObject(tempTable);
			}
			//record.setNormalFieldValue(i, node.calculate(ctx));
			ComputeStack stack = ctx.getComputeStack();
			try {
				stack.push(r);
				record.setNormalFieldValue(i, node.calculate(ctx));
			} finally {
				stack.pop();
			}
		}
	
		tempTable.clear();
	}
	
	protected long skipOver(long n) {
		Sequence data;
		long rest = n;
		long count = 0;
		while (rest != 0) {
			if (rest > FETCHCOUNT) {
				data = get(FETCHCOUNT);
			} else {
				data = get((int)rest);
			}
			if (data == null) {
				break;
			} else {
				count += data.length();
			}
			rest -= data.length();
		}
		return count;
	}
	
	public void close() {
		super.close();
		isClosed = true;
		cache2 = null;
		cursor2.close();
		
		try {
			if (segmentReaders != null) {
				for (ObjectReader reader : segmentReaders) {
					if (reader != null) {
						reader.close();
					}
				}
			}
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			rowCountReader = null;
			colReaders = null;
			segmentReaders = null;
		}
		
	}
	
	public boolean reset() {
		close();
		
		if (!cursor2.reset()) {
			return false;
		} else {
			isClosed = false;
			cur1 = -1;
			cur2 = -1;
			return true;
		}
	}
	
	private static Node parseNode(Expression exp, Context ctx) {
		Node home = exp.getHome();
		Node node = null;
		
		if (home instanceof Moves) {
			node = new New();
			((Function) node).setParameter(null, ctx, ((Function)exp.getHome()).getParamString());
		} else if (home instanceof UnknownSymbol) {
			node = new FieldRef(exp.getFieldName());
		} else if (home instanceof Function) {
			String fname = ((Function)home).getFunctionName();
			if (fname.equals("sum")) {
				node = new Sum();
				((Function) node).setParameter(null, ctx, ((Function)exp.getHome()).getParamString());
			} else if (fname.equals("count")) {
				node = new Count();
				((Function) node).setParameter(null, ctx, ((Function)exp.getHome()).getParamString());
			} else if (fname.equals("min")) {
				node = new Min();
				((Function) node).setParameter(null, ctx, ((Function)exp.getHome()).getParamString());
			} else if (fname.equals("max")) {
				node = new Max();
				((Function) node).setParameter(null, ctx, ((Function)exp.getHome()).getParamString());
			} else if (fname.equals("avg")) {
				node = new Avg();
				((Function) node).setParameter(null, ctx, ((Function)exp.getHome()).getParamString());
			} else if (fname.equals("top")) {
				node = new com.scudata.expression.mfn.sequence.Top();
				((Function) node).setParameter(null, ctx, ((Function)exp.getHome()).getParamString());
			}
		}
		return node;
	}
	
	/**
	 * �ж��������
	 * @return true��û�в������д����false������
	 */
	public static boolean isColTable(Object table) {
		if (table == null) return false;
		if (table instanceof ColPhyTable) {
			if (((ColPhyTable)table).getParent() != null)
				return false;
			if (((ColPhyTable)table).getModifyRecords() == null)
				return true;
		}
		return false;
	}
}
