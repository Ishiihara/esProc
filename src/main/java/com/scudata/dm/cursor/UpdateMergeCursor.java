package com.scudata.dm.cursor;

import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dw.IDWCursor;

/**
 * �����������·ֱ�������ϲ�
 * T.cursor@w(...)
 * @author RunQian
 *
 */
public class UpdateMergeCursor extends ICursor {
	private ICursor []cursors; // �α��������Ѿ����鲢�ֶ���������
	private int []fields; // �鲢�ֶ�
	private int deleteField; // ɾ����ʶ�ֶΣ����û��ɾ����ʶ�ֶ���Ϊ-1
	
	private int field = -1; // ���ֶι鲢ʱʹ��
	private ICursor cs1;
	private ICursor cs2; // ������α��ȹ鲢��һ���α�
	private Sequence data1; // �α�1���������
	private Sequence data2; // �α�2���������
	private int cur1; // �α�1��ǰ��¼�ڻ��������е�����
	private int cur2; // �α�2��ǰ��¼�ڻ��������е�����
	
	//private boolean isSubCursor = false; // �Ƿ������α꣬���α���Ҫ����ɾ���ļ�¼
	
	/**
	 * �����������·ֱ���ɵ��α�
	 * @param cursors �α�����
	 * @param fields �����ֶ�����
	 * @param deleteField ɾ����ʶ�ֶ�����
	 * @param ctx ����������
	 */
	public UpdateMergeCursor(ICursor []cursors, int []fields, int deleteField, Context ctx) {
		this.cursors = cursors;
		this.fields = fields;
		this.deleteField = deleteField;
		this.ctx = ctx;
		
		dataStruct = cursors[0].getDataStruct();
		if (fields.length == 1) {
			field = fields[0];
		}
		
		init();
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	public void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			for (ICursor cursor : cursors) {
				cursor.resetContext(ctx);
			}

			super.resetContext(ctx);
		}
	}
	
	/**
	 * ����ʼ������
	 */
	private void init() {
		int count = cursors.length;
		cs1 = cursors[0];
		
		if (count == 2) {
			cs2 = cursors[1];
		} else {
			// ����α��������������������α��ȹ鲢��һ���α�
			ICursor []subs = new ICursor[count - 1];
			System.arraycopy(cursors, 1, subs, 0, count - 1);
			UpdateMergeCursor subCursor = new UpdateMergeCursor(subs, fields, deleteField, ctx);
			cs2 = subCursor;
		}
		
		data1 = cs1.fuzzyFetch(FETCHCOUNT);
		data2 = cs2.fuzzyFetch(FETCHCOUNT);
		if (data1 != null && data1.length() > 0) {
			cur1 = 1;
		} else {
			cur1 = 0;
		}
		
		if (data2 != null && data2.length() > 0) {
			cur2 = 1;
		} else {
			cur2 = 0;
		}
	}
	
	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		Sequence table;
		if (n > INITSIZE) {
			table = new Sequence(INITSIZE);
		} else {
			table = new Sequence(n);
		}

		if (field != -1) {
			if (deleteField == -1) {
				merge(n, field, table);
			} else {
				merge(n, field, deleteField, table);
			}
		} else {
			if (deleteField == -1) {
				merge(n, fields, table);
			} else {
				merge(n, fields, deleteField, table);
			}
		}
		
		if (table.length() > 0) {
			return table;
		} else {
			return null;
		}
	}
	
	// ���ֶ�����û��ɾ����ʶ�ֶ�ʱ�ĺϲ�
	private Sequence merge(int n, int field, Sequence table) {
		Sequence data1 = this.data1;
		Sequence data2 = this.data2;
		int cur1 = this.cur1;
		int cur2 = this.cur2;
		int count = 0;
		
		if (cur1 != 0 && cur2 != 0) {
			int len1 = data1.length();
			int len2 = data2.length();
			BaseRecord r1 = (BaseRecord)data1.getMem(cur1);
			BaseRecord r2 = (BaseRecord)data2.getMem(cur2);
			
			while (count < n) {
				++count;
				int cmp = r1.compare(r2, field);
				
				if (cmp < 0) {
					table.add(r1);
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else if (cmp == 0) {
					table.add(r2);
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							if (cur1 == len1) {
								data1 = cs1.fetch(FETCHCOUNT);
								if (data1 != null && data1.length() > 0) {
									cur1 = 1;
								} else {
									cur1 = 0;
								}
							} else {
								++cur1;
							}
							
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
					
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else {
					table.add(r2);
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
				}
			}
		}
		
		if (count < n && cur1 != 0) {
			int len1 = data1.length();
			while (count < n) {
				++count;
				table.add(data1.getMem(cur1));
				
				if (cur1 < len1) {
					cur1++;
				} else {
					data1 = cs1.fetch(FETCHCOUNT);
					if (data1 != null && data1.length() > 0) {
						cur1 = 1;
						len1 = data1.length();
					} else {
						cur1 = 0;
						break;
					}
				}
			}
		} else if (count < n && cur2 != 0) {
			int len2 = data2.length();
			while (count < n) {
				++count;
				table.add(data2.getMem(cur2));
				
				if (cur2 < len2) {
					cur2++;
				} else {
					data2 = cs2.fetch(FETCHCOUNT);
					if (data2 != null && data2.length() > 0) {
						cur2 = 1;
						len2 = data2.length();
					} else {
						cur2 = 0;
						break;
					}
				}
			}
		}
		
		this.data1 = data1;
		this.data2 = data2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		
		if (count > 0) {
			return table;
		} else {
			return null;
		}
	}
	
	// ���ֶ�����û��ɾ����ʶ�ֶ�ʱ�ĺϲ�
	private Sequence merge(int n, int []fields, Sequence table) {
		Sequence data1 = this.data1;
		Sequence data2 = this.data2;
		int cur1 = this.cur1;
		int cur2 = this.cur2;
		int count = 0;
		
		if (cur1 != 0 && cur2 != 0) {
			int len1 = data1.length();
			int len2 = data2.length();
			BaseRecord r1 = (BaseRecord)data1.getMem(cur1);
			BaseRecord r2 = (BaseRecord)data2.getMem(cur2);
			
			while (count < n) {
				++count;
				int cmp = r1.compare(r2, fields);
				
				if (cmp < 0) {
					table.add(r1);
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else if (cmp == 0) {
					table.add(r2);
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							if (cur1 == len1) {
								data1 = cs1.fetch(FETCHCOUNT);
								if (data1 != null && data1.length() > 0) {
									cur1 = 1;
								} else {
									cur1 = 0;
								}
							} else {
								++cur1;
							}
							
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
					
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else {
					table.add(r2);
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
				}
			}
		}
		
		if (count < n && cur1 != 0) {
			int len1 = data1.length();
			while (count < n) {
				++count;
				table.add(data1.getMem(cur1));
				
				if (cur1 < len1) {
					cur1++;
				} else {
					data1 = cs1.fetch(FETCHCOUNT);
					if (data1 != null && data1.length() > 0) {
						cur1 = 1;
						len1 = data1.length();
					} else {
						cur1 = 0;
						break;
					}
				}
			}
		} else if (count < n && cur2 != 0) {
			int len2 = data2.length();
			while (count < n) {
				++count;
				table.add(data2.getMem(cur2));
				
				if (cur2 < len2) {
					cur2++;
				} else {
					data2 = cs2.fetch(FETCHCOUNT);
					if (data2 != null && data2.length() > 0) {
						cur2 = 1;
						len2 = data2.length();
					} else {
						cur2 = 0;
						break;
					}
				}
			}
		}
		
		this.data1 = data1;
		this.data2 = data2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		
		if (count > 0) {
			return table;
		} else {
			return null;
		}
	}

	// ����false��ʾɾ����¼��true��ʾ������¼
	public static boolean merge(BaseRecord r1, BaseRecord r2, int deleteField) {
		Boolean b1 = (Boolean)r1.getNormalFieldValue(deleteField);
		Boolean b2 = (Boolean)r2.getNormalFieldValue(deleteField);
		
		if (b1 == null) { // ����
			if (b2 == null) {
				return true; // ��+��-->��
			} else if (b2.booleanValue()) { // ɾ��
				return false; // ��+ɾ-->��
			} else {
				r2.setNormalFieldValue(deleteField, null); // ��+��-->��
				return true;
			}
		} else if (b1.booleanValue()) { // ɾ��
			if (b2 == null) {
				r2.setNormalFieldValue(deleteField, Boolean.FALSE); // ɾ+��-->��
			}
			
			// ɾ+ɾ-->ɾ
			// ɾ+��-->��
			return true;
		} else { // �޸�
			if (b2 == null) {
				r2.setNormalFieldValue(deleteField, b1); // ��+��-->��
			}
			
			// ��+ɾ-->ɾ
			// ��+��-->��
			return true;
		}
	}
	
	// ���ֶ���������ɾ����ʶ�ֶ�ʱ�ĺϲ�
	private Sequence merge(int n, int field, int deleteField, Sequence table) {
		Sequence data1 = this.data1;
		Sequence data2 = this.data2;
		int cur1 = this.cur1;
		int cur2 = this.cur2;
		int count = 0;
		
		if (cur1 != 0 && cur2 != 0) {
			int len1 = data1.length();
			int len2 = data2.length();
			BaseRecord r1 = (BaseRecord)data1.getMem(cur1);
			BaseRecord r2 = (BaseRecord)data2.getMem(cur2);
			
			while (count < n) {
				int cmp = r1.compare(r2, field);
				if (cmp < 0) {
					++count;
					table.add(r1);
					
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else if (cmp == 0) {
					if (merge(r1, r2, deleteField)) {
						++count;
						table.add(r2);
					}
					
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							if (cur1 == len1) {
								data1 = cs1.fetch(FETCHCOUNT);
								if (data1 != null && data1.length() > 0) {
									cur1 = 1;
								} else {
									cur1 = 0;
								}
							} else {
								++cur1;
							}
							
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
					
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else {
					++count;
					table.add(r2);
					
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
				}
			}
		}
		
		if (count < n && cur1 != 0) {
			int len1 = data1.length();
			while (count < n) {
				++count;
				table.add(data1.getMem(cur1));
				
				if (cur1 < len1) {
					cur1++;
				} else {
					data1 = cs1.fetch(FETCHCOUNT);
					if (data1 != null && data1.length() > 0) {
						cur1 = 1;
						len1 = data1.length();
					} else {
						cur1 = 0;
						break;
					}
				}
			}
		} else if (count < n && cur2 != 0) {
			int len2 = data2.length();
			while (count < n) {
				++count;
				table.add(data2.getMem(cur2));
				
				if (cur2 < len2) {
					cur2++;
				} else {
					data2 = cs2.fetch(FETCHCOUNT);
					if (data2 != null && data2.length() > 0) {
						cur2 = 1;
						len2 = data2.length();
					} else {
						cur2 = 0;
						break;
					}
				}
			}
		}
		
		this.data1 = data1;
		this.data2 = data2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		
		if (count > 0) {
			return table;
		} else {
			return null;
		}
	}

	// ���ֶ���������ɾ����ʶ�ֶ�ʱ�ĺϲ�
	private Sequence merge(int n, int []fields, int deleteField, Sequence table) {
		Sequence data1 = this.data1;
		Sequence data2 = this.data2;
		int cur1 = this.cur1;
		int cur2 = this.cur2;
		int count = 0;
		
		if (cur1 != 0 && cur2 != 0) {
			int len1 = data1.length();
			int len2 = data2.length();
			BaseRecord r1 = (BaseRecord)data1.getMem(cur1);
			BaseRecord r2 = (BaseRecord)data2.getMem(cur2);
			
			while (count < n) {
				int cmp = r1.compare(r2, fields);
				if (cmp < 0) {
					++count;
					table.add(r1);
					
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else if (cmp == 0) {
					if (merge(r1, r2, deleteField)) {
						++count;
						table.add(r2);
					}
					
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							if (cur1 == len1) {
								data1 = cs1.fetch(FETCHCOUNT);
								if (data1 != null && data1.length() > 0) {
									cur1 = 1;
								} else {
									cur1 = 0;
								}
							} else {
								++cur1;
							}
							
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
					
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else {
					++count;
					table.add(r2);
					
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
				}
			}
		}
		
		if (count < n && cur1 != 0) {
			int len1 = data1.length();
			while (count < n) {
				++count;
				table.add(data1.getMem(cur1));
				
				if (cur1 < len1) {
					cur1++;
				} else {
					data1 = cs1.fetch(FETCHCOUNT);
					if (data1 != null && data1.length() > 0) {
						cur1 = 1;
						len1 = data1.length();
					} else {
						cur1 = 0;
						break;
					}
				}
			}
		} else if (count < n && cur2 != 0) {
			int len2 = data2.length();
			while (count < n) {
				++count;
				table.add(data2.getMem(cur2));
				
				if (cur2 < len2) {
					cur2++;
				} else {
					data2 = cs2.fetch(FETCHCOUNT);
					if (data2 != null && data2.length() > 0) {
						cur2 = 1;
						len2 = data2.length();
					} else {
						cur2 = 0;
						break;
					}
				}
			}
		}
		
		this.data1 = data1;
		this.data2 = data2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		
		if (count > 0) {
			return table;
		} else {
			return null;
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		int []fields = this.fields;
		int deleteField = this.deleteField;
		Sequence data1 = this.data1;
		Sequence data2 = this.data2;
		int cur1 = this.cur1;
		int cur2 = this.cur2;
		long count = 0;
		
		if (cur1 != 0 && cur2 != 0) {
			int len1 = data1.length();
			int len2 = data2.length();
			BaseRecord r1 = (BaseRecord)data1.getMem(cur1);
			BaseRecord r2 = (BaseRecord)data2.getMem(cur2);
			
			while (count < n) {
				int cmp = r1.compare(r2, fields);
				if (cmp < 0) {
					++count;
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else if (cmp == 0) {
					if (deleteField == -1 || merge(r1, r2, deleteField)) {
						++count;
					}
					
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							if (cur1 == len1) {
								data1 = cs1.fetch(FETCHCOUNT);
								if (data1 != null && data1.length() > 0) {
									cur1 = 1;
								} else {
									cur1 = 0;
								}
							} else {
								++cur1;
							}
							
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
					
					if (cur1 == len1) {
						data1 = cs1.fetch(FETCHCOUNT);
						if (data1 != null && data1.length() > 0) {
							cur1 = 1;
							len1 = data1.length();
							r1 = (BaseRecord)data1.getMem(1);
						} else {
							cur1 = 0;
							break;
						}
					} else {
						r1 = (BaseRecord)data1.getMem(++cur1);
					}
				} else {
					++count;
					if (cur2 == len2) {
						data2 = cs2.fetch(FETCHCOUNT);
						if (data2 != null && data2.length() > 0) {
							cur2 = 1;
							len2 = data2.length();
							r2 = (BaseRecord)data2.getMem(1);
						} else {
							cur2 = 0;
							break;
						}
					} else {
						r2 = (BaseRecord)data2.getMem(++cur2);
					}
				}
			}
		}
		
		if (count < n && cur1 != 0) {
			int len1 = data1.length();
			while (count < n) {
				++count;
				if (cur1 < len1) {
					cur1++;
				} else {
					data1 = cs1.fetch(FETCHCOUNT);
					if (data1 != null && data1.length() > 0) {
						cur1 = 1;
						len1 = data1.length();
					} else {
						cur1 = 0;
						break;
					}
				}
			}
		} else if (count < n && cur2 != 0) {
			int len2 = data2.length();
			while (count < n) {
				++count;
				
				if (cur2 < len2) {
					cur2++;
				} else {
					data2 = cs2.fetch(FETCHCOUNT);
					if (data2 != null && data2.length() > 0) {
						cur2 = 1;
						len2 = data2.length();
					} else {
						cur2 = 0;
						break;
					}
				}
			}
		}
		
		this.data1 = data1;
		this.data2 = data2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		return count;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (cursors != null) {
			for (int i = 0, count = cursors.length; i < count; ++i) {
				cursors[i].close();
			}

			cs1 = null;
			cs2 = null;
			data1 = null;
			data2 = null;
		}
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		ICursor []cursors = this.cursors;
		int count = cursors.length;
		for (int i = 0; i < count; ++i) {
			if (!cursors[i].reset()) {
				return false;
			}
		}
		
		init();
		return true;
	}
	
	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	public String[] getSortFields() {
		return cursors[0].getSortFields();
	}
	
	/**
	 * �������� ��Ŀǰ����@x��
	 * @param opt
	 */
	public void setOption(String opt) {
		ICursor []cursors = this.cursors;
		int count = cursors.length;
		for (int i = 0; i < count; ++i) {
			ICursor cs = cursors[i];
			if (cs instanceof IDWCursor) {
				((IDWCursor) cs).setOption(opt);
			}
		}
	}
}
