package com.scudata.dm;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import com.scudata.common.DateFormatFactory;
import com.scudata.common.DateFormatX;
import com.scudata.common.Escape;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Types;
import com.scudata.resources.EngineMessage;
import com.scudata.util.FloatingDecimal;
import com.scudata.util.Variant;

/**
 * ���ڰ��ı��ļ��������
 * @author RunQian
 *
 */
public final class LineImporter implements ILineInput {
	private static final int BOM_SIZE = 4; // BOMͷ���Ĵ�С
	
	private static final int PARSEMODE_DEFAULT = 0; // ���н�������ת�������ת����ָ������������ת����������
	private static final int PARSEMODE_DELETE = 1; // ���н�������ת�������ת����ָ����������ɾ����
	private static final int PARSEMODE_EXCEPTION = 2; // ���н�������ת�������ת����ָ�������������쳣
	private static final int PARSEMODE_MULTI_STRING = 3; // �������ͽ��������ض��ֶ��ַ���
	private static final int PARSEMODE_SINGLE_STRING = 4; // �����в�֣�ÿ�з��س��ַ���
	
	private static final byte CR = (byte)'\r';
	private static final byte LF = (byte)'\n';
	private static final byte CONTINUECHAR = '\\'; // ���з�
	
	private InputStream is; // ������
	private byte[] buffer; // ÿ�ζ�����ֽڻ���
	private int index; // ��һ����buffer�е�����
	private int count; // ����buffer��ʵ���ֽ���Ŀ
	private long position; // �����������е�λ��
	private boolean isEof = false; // �Ƿ��Ѿ��ļ�����

	private String charset; // �ַ���
	private byte colSeparator; // �м��
	private byte []colSeparators; // ���ַ��м���������Ϊ�������colSeparator
	
	private byte []colTypes; // ������
	private DateFormatX []fmts; // ����ʱ��ĸ�ʽ
	private int []serialByteLens; // �ź��ֶεĳ���
	private int []selIndex; // ���Ƿ�ѡ����С��0��ѡ��

	private int parseMode = PARSEMODE_DEFAULT; // ����ֵ��ģʽ
	private char escapeChar = '\\'; // ת�����@oѡ��ʱʹ��excel��׼��ת���Ϊ"����������βʱ���������ڵĻ��з�
	private boolean isQuote = false; // �����������������ţ��������⣬����ת��
	private boolean isSingleQuote = false; // �������������˵����ţ��������⣬����ת��
	private boolean doQuoteMatch; // �Ƿ�������ƥ��
	private boolean doSingleQuoteMatch; // �Ƿ���������ƥ��
	private boolean doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
	private boolean isTrim = true; // �Ƿ�ȡ�����ߵĿհ�
	private boolean isContinueLine = false; // �Ƿ�������
	private boolean checkColCount = false; // ����������������ɾ��ѡ��Ե�һ��Ϊ׼
	private boolean checkValueType = false; // �������ͺ͸�ʽ�Ƿ�ƥ��
	
	private boolean isStringMode = false; // �Ƿ��Ȱ��ж���String�ٷ��У���ֹ�еı���ĺ��ֵĵڶ����ֽڵ�ֵ�����зָ���
	
	/**
	 * ���ڱ�ʾÿ�����ݶ�Ӧ���ֽ�����
	 * @author RunQian
	 *
	 */
	private class LineBytes {
		private byte[] buffer; // ������
		private int i; // �п�ʼλ�ã�����
		private int count; // ���ֽ���
		
		public LineBytes(byte[] buffer, int i, int count) {
			this.buffer = buffer;
			this.i = i;
			this.count = count;
		}
	}
	
	/**
	 * �������ı��ļ������������ݶ���
	 * @param is ������
	 * @param charset �ַ���
	 * @param colSeparator �зָ���
	 * @param opt ѡ��
	 * 		@s	������ֶΣ����ɵ��ֶδ����ɵ�������Բ���
	 * 		@q	�����������������ţ��������⣬����ת�壻�м�����Ų���
	 * 		@a	�ѵ�����Ҳ��Ϊ���Ŵ���ȱʡ������
	 * 		@o	ʹ��Excel��׼ת�壬����˫�����ű�ʾһ�����ţ������ַ���ת��
	 * 		@p	����ʱ�������ź�����ƥ�䣬�����ڷָ������㣬������ת��Ҳ����
	 * 		@f	�����κν��������÷ָ�����ɴ�
	 * 		@l	�������У���β��ת���ַ�\��
	 * 		@k	�������������˵Ŀհ׷���ȱʡ���Զ���trim
	 * 		@e	Fi�ڴ��в�����ʱ������null��ȱʡ������
	 * 		@d	���������ݲ�ƥ�����ͺ͸�ʽʱɾ�����У���������ƥ�������ƥ��
	 * 		@n	������ƥ���������󣬺��Դ���
	 * 		@v	@d@nʱ����ʱ�׳�Υ�����жϳ�����������е�����
	 */
	public LineImporter(InputStream is, String charset, byte[] colSeparator, String opt) {
		this(is, charset, colSeparator, opt, Env.FILE_BUFSIZE);
	}

	/**
	 * �������ı��ļ������������ݶ���
	 * @param is ������
	 * @param charset �ַ���
	 * @param colSeparator �зָ���
	 * @param opt ѡ��
	 * 		@s	������ֶΣ����ɵ��ֶδ����ɵ�������Բ���
	 * 		@q	�����������������ţ��������⣬����ת�壻�м�����Ų���
	 * 		@a	�ѵ�����Ҳ��Ϊ���Ŵ���ȱʡ������
	 * 		@o	ʹ��Excel��׼ת�壬����˫�����ű�ʾһ�����ţ������ַ���ת��
	 * 		@p	����ʱ�������ź�����ƥ�䣬�����ڷָ������㣬������ת��Ҳ����
	 * 		@f	�����κν��������÷ָ�����ɴ�
	 * 		@l	�������У���β��ת���ַ�\��
	 * 		@k	�������������˵Ŀհ׷���ȱʡ���Զ���trim
	 * 		@e	Fi�ڴ��в�����ʱ������null��ȱʡ������
	 * 		@d	���������ݲ�ƥ�����ͺ͸�ʽʱɾ�����У���������ƥ�������ƥ��
	 * 		@n	������ƥ���������󣬺��Դ���
	 * 		@v	@d@nʱ����ʱ�׳�Υ�����жϳ�����������е�����
	 * @param bufSize ����������С
	 */
	public LineImporter(InputStream is, String charset, byte[] colSeparator, String opt, int bufSize) {
		if (colSeparator.length == 1) {
			this.colSeparator = colSeparator[0];
		} else {
			this.colSeparators = colSeparator;
		}

		this.is = is;
		this.charset = charset;
		buffer = new byte[bufSize];

		if (opt != null) {
			if (opt.indexOf('s') != -1) {
				// ������ֶΣ����ɵ��ֶδ����ɵ����
				parseMode = PARSEMODE_SINGLE_STRING;
			} else if (opt.indexOf('f') != -1) {
				// �������ͽ��������÷ָ�����ɴ�
				parseMode = PARSEMODE_MULTI_STRING;
			} else {
				if (opt.indexOf('d') != -1) {
					checkValueType = true;
					parseMode = LineImporter.PARSEMODE_DELETE;
				}
				
				if (opt.indexOf('n') != -1) {
					checkColCount = true;
					parseMode = LineImporter.PARSEMODE_DELETE;
				}
				
				if (opt.indexOf('v') != -1) {
					parseMode = LineImporter.PARSEMODE_EXCEPTION;
				}
			}
			
			if (opt.indexOf('q') != -1) {
				// �����������������ţ��������⣬����ת�壬��������ƥ��
				isQuote = true;
				doQuoteMatch = true;
			}
			
			if (opt.indexOf('a') != -1) {
				// �������������˵����ţ��������⣬����ת�壬��������ƥ��
				isSingleQuote = true;
				doSingleQuoteMatch = true;
			}
			
			if (opt.indexOf('o') != -1) {
				// ʹ��Excel��׼ת�壬����˫�����ű�ʾһ�����ţ������ַ���ת�壬��������ƥ��
				escapeChar = '"';
				doQuoteMatch = true;
			}
			
			// ����ʱ�������ź����ţ��������ţ�ƥ��
			if (opt.indexOf('p') != -1) {
				doQuoteMatch = true;
				//doSingleQuoteMatch = true;
				doBracketsMatch = true;
			}
			
			// �������У���β��ת���\��
			if (opt.indexOf('l') != -1) isContinueLine = true;
			
			// �������������˵Ŀհ׷���ȱʡ���Զ���trim
			if (opt.indexOf('k') != -1) isTrim = false;
			
			if (colSeparators == null && opt.indexOf('r') != -1) isStringMode = true;
		}
		
		// ����BOMͷ
		init();
	}

	/**
	 * ����ָ��LineImporter������
	 * @param other
	 */
	public void copyProperty(LineImporter other) {
		this.charset = other.charset;
		this.colSeparator = other.colSeparator;
		this.colSeparators = other.colSeparators;
		this.colTypes = other.colTypes;
		this.fmts = other.fmts;
		this.serialByteLens = other.serialByteLens;
		this.selIndex = other.selIndex;
		
		this.parseMode = other.parseMode;
		this.escapeChar = other.escapeChar;
		this.isQuote = other.isQuote;
		this.isSingleQuote = other.isSingleQuote;
		this.doQuoteMatch = other.doQuoteMatch;
		this.doSingleQuoteMatch = other.doSingleQuoteMatch;
		this.doBracketsMatch = other.doBracketsMatch;

		this.isTrim = other.isTrim;
		this.isContinueLine = other.isContinueLine;
		this.checkColCount = other.checkColCount;
		this.checkValueType = other.checkValueType;
	}
	
	private void init() {
		// ����Ƿ���BOMͷ
		try {
			count = is.read(buffer);
			position = count;
			index = 0;
			
			if (count < BOM_SIZE) {
				return;
			} else if (buffer[0] == (byte)0xEF && buffer[1] == (byte)0xBB && buffer[2] == (byte)0xBF) {
				charset = "UTF-8";
				index = 3;
			} /*else if (buffer[0] == (byte)0xFF && buffer[1] == (byte)0xFE && buffer[2] == (byte)0x00 && buffer[3] == (byte)0x00) {
				charset = "UTF-32LE";
				index = 4;
			} else if (buffer[0] == (byte)0x00 && buffer[1] == (byte)0x00 && buffer[2] == (byte)0xFE && buffer[3] == (byte)0xFF) {
				charset = "UTF-32BE";
				index = 4;
			} else if (buffer[0] == (byte)0xFF && buffer[1] == (byte)0xFE) {
				charset = "UTF-16LE";
				index = 2;
			} else if (buffer[0] == (byte)0xFE && buffer[1] == (byte)0xFF) {
				charset = "UTF-16BE";
				index = 2;				
			} */else {
				return;
			}
			
			// UTF-16��UTF-32�зָ�ͻس���ռ�ö���ֽڣ�Ŀǰû�д���
		} catch (IOException e) {
			throw new RQException(e);
		}
	}
	
	/**
	 * ȡת���
	 * @return char
	 */
	public char getEscapeChar() {
		return escapeChar;
	}
	
	// �����ļ����ݵ�������
	private int readBuffer() throws IOException {
		if (!isEof) {
			do {
				count = is.read(buffer);
			} while (count == 0);

			if (count > 0) {
				position += count;
			} else {
				isEof = true;
			}

			index = 0;
			return count;
		} else {
			return -1;
		}
	}

	/**
	 * ȡ��ǰ�Ķ���λ��
	 * @return
	 */
	public long getCurrentPosition() {
		return position - count + index;
	}

	// ����������ָ���ֽ�
	static private long skip(InputStream is, long count) throws IOException {
		long old = count;
		while (count > 0) {
			long num = is.skip(count);
			if (num <= 0) break;

			count -= num;
		}

		return old - count;
	}

	/**
	 * ����ָ��λ�ã����ڶ��̶߳�ȡ���ݣ�������ͷȥβ����
	 * @param pos λ��
	 * @throws IOException
	 */
	public void seek(long pos) throws IOException {
		if (pos <= 0) {
		} else if (pos < position) {
			long dif = position - pos;
			if (dif < count) {
				index = count - (int)dif;
				skipLine();
			} else { // ֻ����ǰseek
				throw new RuntimeException();
			}
		} else {
			long skipCount = skip(is, pos - position);
			position += skipCount;
			readBuffer();
			skipLine();
		}
	}

	/**
	 * �����ֶ�����
	 * @param types ��������
	 * @param strFmts ��ʽ���飬��������ʱ��
	 */
	public void setColTypes(byte []types, String []strFmts) {
		int count = types.length;
		this.colTypes = types;
		this.fmts = new DateFormatX[count];
		this.serialByteLens = new int[count];
		
		for (int i = 0; i < count; ++i) {
			if (types[i] == Types.DT_DATE) {
				if (strFmts == null || strFmts[i] == null) {
					fmts[i] = DateFormatFactory.get().getDateFormatX();
				} else {
					fmts[i] = DateFormatFactory.get().getFormatX(strFmts[i]);
				}
			} else if (types[i] == Types.DT_DATETIME) {
				if (strFmts == null || strFmts[i] == null) {
					fmts[i] = DateFormatFactory.get().getDateTimeFormatX();
				} else {
					fmts[i] = DateFormatFactory.get().getFormatX(strFmts[i]);
				}
			} else if (types[i] == Types.DT_TIME) {
				if (strFmts == null || strFmts[i] == null) {
					fmts[i] = DateFormatFactory.get().getTimeFormatX();
				} else {
					fmts[i] = DateFormatFactory.get().getFormatX(strFmts[i]);
				}
			} else if (types[i] == Types.DT_SERIALBYTES) {
				serialByteLens[i] = Integer.parseInt(strFmts[i]);
			}
		}
	}

	/**
	 * ȡ�ֶ�����
	 * @return ��������
	 */
	public byte[] getColTypes() {
		return colTypes;
	}

	/**
	 * ����ѡȡ����
	 * @param index ��������ɵ����飬��0��ʼ����
	 */
	public void setColSelectIndex(int []index) {
		this.selIndex = index;
	}

	/**
	 * ȡѡ�����������ɵ����飬��0��ʼ����
	 * @return
	 */
	public int[] getColSelectIndex() {
		return selIndex;
	}
	
	// ������һ��������ռ���ֽ�
	private LineBytes readLineBytes() throws IOException {
		// �Ƿ����������ڵĻس�
		boolean skipQuoteEnter = escapeChar == '"';
		byte[] buffer = this.buffer;
		byte []prevBuffer = null; // �ϴ�ʣ����ֽ�
		LineBytes line = null;
		
		int count = this.count;
		int index = this.index;
		int start = index;
		
		Next:
		while (true) {
			if (index >= count) {
				// ��ǰ����������Ѿ������꣬���浱ǰ���ݵ�prevBuffer��
				int curCount = count - start;
				if (curCount > 0) {
					if (prevBuffer == null) {
						prevBuffer = new byte[curCount];
						System.arraycopy(buffer, start, prevBuffer, 0, curCount);
					} else {
						int prevLen = prevBuffer.length;
						byte[] temp = new byte[curCount + prevLen];
						System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
						System.arraycopy(buffer, start, temp, prevLen, curCount);
						prevBuffer = temp;
					}
				}

				// ���������ֽ�
				if (readBuffer() <= 0) {
					if (prevBuffer != null) { // ���һ��
						return new LineBytes(prevBuffer, 0, prevBuffer.length);
					} else {
						return null;
					}
				} else {
					count = this.count;
					start = 0;
					index = 0;
				}
			}
			
			if (buffer[index] == LF) {
				// �ҵ��н������������������к�
				this.index = index + 1;
				
				if (index > start) {
					// ���LFǰ�Ƿ���CR
					if (buffer[index - 1] == CR) {
						index--;
					}
					
					int curLen = index - start;
					if (prevBuffer == null) {
						line = new LineBytes(buffer, start, curLen);
					} else if (curLen > 0) {
						int prevLen = prevBuffer.length;
						byte []temp = new byte[prevLen + curLen];
						System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
						System.arraycopy(buffer, start, temp, prevLen, curLen);
						line = new LineBytes(temp, 0, temp.length);
					} else {
						return new LineBytes(prevBuffer, 0, prevBuffer.length);
					}
				} else {
					if (prevBuffer != null) {
						// ���ᳵǰ�Ƿ��ǻ��з�
						int prevLen = prevBuffer.length;
						if (prevBuffer[prevLen - 1] == CR) { // \r����һ�ζ����Ļ������У�index����0
							line = new LineBytes(prevBuffer, 0, prevLen -1);
						} else {
							line = new LineBytes(prevBuffer, 0, prevLen);
						}
					} else {
						// ��������Ϊ�գ�ֻ�лس�
						line = new LineBytes(buffer, start, 0);
					}
				}
				
				return line;
			} else if (skipQuoteEnter && buffer[index] == '"') {
				// ������ƥ�䣬���������ŵ���һ���ַ�
				++index;
				while (true) {
					if (index == count) {
						// ���浱ǰ���ݵ�prevBuffer��
						int curCount = count - start;
						if (prevBuffer == null) {
							prevBuffer = new byte[curCount];
							System.arraycopy(buffer, start, prevBuffer, 0, curCount);
						} else {
							int prevLen = prevBuffer.length;
							byte[] temp = new byte[curCount + prevLen];
							System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
							System.arraycopy(buffer, start, temp, prevLen, curCount);
							prevBuffer = temp;
						}

						// ���������ֽ�
						if (readBuffer() <= 0) {
							return new LineBytes(prevBuffer, 0, prevBuffer.length);
						} else {
							count = this.count;
							start = 0;
							index = 0;
						}
					}
					
					if (buffer[index] == '"') {
						++index;
						if (index < count) {
							if (buffer[index] != '"') {
								// �ҵ�����ƥ��
								continue Next;
							} else {
								// ��������˫�����Ƕ�����ת��
								++index;
							}
						} else {
							// ���浱ǰ���ݵ�prevBuffer��
							int curCount = count - start;
							if (prevBuffer == null) {
								prevBuffer = new byte[curCount];
								System.arraycopy(buffer, start, prevBuffer, 0, curCount);
							} else {
								int prevLen = prevBuffer.length;
								byte[] temp = new byte[curCount + prevLen];
								System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
								System.arraycopy(buffer, start, temp, prevLen, curCount);
								prevBuffer = temp;
							}
	
							// ���������ֽ�
							if (readBuffer() <= 0) {
								return new LineBytes(prevBuffer, 0, prevBuffer.length);
							} else {
								count = this.count;
								start = 0;
								if (buffer[0] != '"') {
									// �ҵ�����ƥ��
									index = 0;
									continue Next;
								} else {
									// ��������˫�����Ƕ�����ת��
									index = 1;
								}
							}
						}
					} else {
						++index;
					}
				}
			} else if (isContinueLine && buffer[index] == CONTINUECHAR) {
				// ����������У���鵱ǰ�ַ��Ƿ��ǡ�\��
				++index;
				if (index < count) {
					if (buffer[index] == LF) { // \n
						// ���浱ǰ���ݵ�prevBuffer��
						int curCount = index - start - 1;
						if (prevBuffer == null) {
							prevBuffer = new byte[curCount];
							System.arraycopy(buffer, start, prevBuffer, 0, curCount);
						} else {
							int prevLen = prevBuffer.length;
							byte[] temp = new byte[curCount + prevLen];
							System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
							System.arraycopy(buffer, start, temp, prevLen, curCount);
							prevBuffer = temp;
						}
						
						start = ++index;
					} else if (buffer[index] == CR) { // \r\n
						// ���浱ǰ���ݵ�prevBuffer��
						int curCount = index - start - 1;
						if (prevBuffer == null) {
							prevBuffer = new byte[curCount];
							System.arraycopy(buffer, start, prevBuffer, 0, curCount);
						} else {
							int prevLen = prevBuffer.length;
							byte[] temp = new byte[curCount + prevLen];
							System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
							System.arraycopy(buffer, start, temp, prevLen, curCount);
							prevBuffer = temp;
						}
						
						// CR�������LF������LF
						++index;
						if (index == count) {
							// \n����һ��������
							if (readBuffer() <= 0) {
								return new LineBytes(prevBuffer, 0, prevBuffer.length);
							} else {
								count = this.count;
								index = 1;
							}
						} else {
							++index;
						}
						
						start = index;
					}
				} else {
					// ���浱ǰ���ݵ�prevBuffer��
					int curCount = index - start - 1;
					if (curCount > 0) {
						if (prevBuffer == null) {
							prevBuffer = new byte[curCount];
							System.arraycopy(buffer, start, prevBuffer, 0, curCount);
						} else {
							int prevLen = prevBuffer.length;
							byte[] temp = new byte[curCount + prevLen];
							System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
							System.arraycopy(buffer, start, temp, prevLen, curCount);
							prevBuffer = temp;
						}
					}
					
					if (readBuffer() <= 0) {
						if (prevBuffer != null) {
							return new LineBytes(prevBuffer, 0, prevBuffer.length);
						} else {
							return null;
						}
					} else {
						count = this.count;
						if (buffer[0] == LF) { // \n
							index = 1;
							start = 1;
						} else if (buffer[0] == CR) { // \r\n
							index = 2;
							start = 2;
						} else {
							index = 0;
							start = 0;
							
							// �����з����뵽֮ǰ�Ļ�����
							if (prevBuffer == null) {
								prevBuffer = new byte[]{CONTINUECHAR};
							} else {
								int prevLen = prevBuffer.length;
								byte[] temp = new byte[prevLen + 1];
								System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
								temp[prevLen] = CONTINUECHAR;
								prevBuffer = temp;
							}
						}
					}
				}
			} else {
				++index;
			}
		}
	}
	
	// ����һ�����ݶ����ַ���
	private String readLineString() throws IOException {
		// �Ƿ����������ڵĻس�
		boolean skipQuoteEnter = escapeChar == '"';
		byte[] buffer = this.buffer;
		byte []prevBuffer = null; // �ϴ�ʣ����ֽ�
		int count = this.count;
		int index = this.index;
		int start = index;
		
		Next:
		while (true) {
			if (index >= count) {
				// ��ǰ����������Ѿ������꣬���浱ǰ���ݵ�prevBuffer��
				int curCount = count - start;
				if (curCount > 0) {
					if (prevBuffer == null) {
						prevBuffer = new byte[curCount];
						System.arraycopy(buffer, start, prevBuffer, 0, curCount);
					} else {
						int prevLen = prevBuffer.length;
						byte[] temp = new byte[curCount + prevLen];
						System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
						System.arraycopy(buffer, start, temp, prevLen, curCount);
						prevBuffer = temp;
					}
				}

				// ���������ֽ�
				if (readBuffer() <= 0) {
					if (prevBuffer != null) { // ���һ��
						return new String(prevBuffer, 0, prevBuffer.length, charset);
					} else {
						return null;
					}
				} else {
					count = this.count;
					start = 0;
					index = 0;
				}
			}
			
			if (buffer[index] == LF) {
				// �ҵ��н������������������к�
				this.index = index + 1;
				
				if (index > start) {
					// ���LFǰ�Ƿ���CR
					if (buffer[index - 1] == CR) {
						index--;
					}
					
					int curLen = index - start;
					if (prevBuffer == null) {
						return new String(buffer, start, curLen, charset);
					} else if (curLen > 0) {
						int prevLen = prevBuffer.length;
						byte []temp = new byte[prevLen + curLen];
						System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
						System.arraycopy(buffer, start, temp, prevLen, curLen);
						return new String(temp, 0, temp.length, charset);
					} else {
						return new String(prevBuffer, 0, prevBuffer.length, charset);
					}
				} else {
					if (prevBuffer != null) {
						// ���ᳵǰ�Ƿ��ǻ��з�
						int prevLen = prevBuffer.length;
						if (prevBuffer[prevLen - 1] == CR) { // \r����һ�ζ����Ļ������У�index����0
							return new String(prevBuffer, 0, prevLen -1, charset);
						} else {
							return new String(prevBuffer, 0, prevLen, charset);
						}
					} else {
						// ��������Ϊ�գ�ֻ�лس�
						return new String(buffer, start, 0, charset);
					}
				}
			} else if (skipQuoteEnter && buffer[index] == '"') {
				// ������ƥ�䣬���������ŵ���һ���ַ�
				++index;
				while (true) {
					if (index == count) {
						// ���浱ǰ���ݵ�prevBuffer��
						int curCount = count - start;
						if (prevBuffer == null) {
							prevBuffer = new byte[curCount];
							System.arraycopy(buffer, start, prevBuffer, 0, curCount);
						} else {
							int prevLen = prevBuffer.length;
							byte[] temp = new byte[curCount + prevLen];
							System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
							System.arraycopy(buffer, start, temp, prevLen, curCount);
							prevBuffer = temp;
						}

						// ���������ֽ�
						if (readBuffer() <= 0) {
							return new String(prevBuffer, 0, prevBuffer.length, charset);
						} else {
							count = this.count;
							start = 0;
							index = 0;
						}
					}
					
					if (buffer[index] == '"') {
						++index;
						if (index < count) {
							if (buffer[index] != '"') {
								// �ҵ�����ƥ��
								continue Next;
							} else {
								// ��������˫�����Ƕ�����ת��
								++index;
							}
						} else {
							// ���浱ǰ���ݵ�prevBuffer��
							int curCount = count - start;
							if (prevBuffer == null) {
								prevBuffer = new byte[curCount];
								System.arraycopy(buffer, start, prevBuffer, 0, curCount);
							} else {
								int prevLen = prevBuffer.length;
								byte[] temp = new byte[curCount + prevLen];
								System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
								System.arraycopy(buffer, start, temp, prevLen, curCount);
								prevBuffer = temp;
							}
	
							// ���������ֽ�
							if (readBuffer() <= 0) {
								return new String(prevBuffer, 0, prevBuffer.length, charset);
							} else {
								count = this.count;
								start = 0;
								if (buffer[0] != '"') {
									// �ҵ�����ƥ��
									index = 0;
									continue Next;
								} else {
									// ��������˫�����Ƕ�����ת��
									index = 1;
								}
							}
						}
					} else {
						++index;
					}
				}
			} else if (isContinueLine && buffer[index] == CONTINUECHAR) {
				// ����������У���鵱ǰ�ַ��Ƿ��ǡ�\��
				++index;
				if (index < count) {
					if (buffer[index] == LF) { // \n
						// ���浱ǰ���ݵ�prevBuffer��
						int curCount = index - start - 1;
						if (prevBuffer == null) {
							prevBuffer = new byte[curCount];
							System.arraycopy(buffer, start, prevBuffer, 0, curCount);
						} else {
							int prevLen = prevBuffer.length;
							byte[] temp = new byte[curCount + prevLen];
							System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
							System.arraycopy(buffer, start, temp, prevLen, curCount);
							prevBuffer = temp;
						}
						
						start = ++index;
					} else if (buffer[index] == CR) { // \r\n
						// ���浱ǰ���ݵ�prevBuffer��
						int curCount = index - start - 1;
						if (prevBuffer == null) {
							prevBuffer = new byte[curCount];
							System.arraycopy(buffer, start, prevBuffer, 0, curCount);
						} else {
							int prevLen = prevBuffer.length;
							byte[] temp = new byte[curCount + prevLen];
							System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
							System.arraycopy(buffer, start, temp, prevLen, curCount);
							prevBuffer = temp;
						}
						
						// CR�������LF������LF
						++index;
						if (index == count) {
							// \n����һ��������
							if (readBuffer() <= 0) {
								return new String(prevBuffer, 0, prevBuffer.length, charset);
							} else {
								count = this.count;
								index = 1;
							}
						} else {
							++index;
						}
						
						start = index;
					}
				} else {
					// ���浱ǰ���ݵ�prevBuffer��
					int curCount = index - start - 1;
					if (curCount > 0) {
						if (prevBuffer == null) {
							prevBuffer = new byte[curCount];
							System.arraycopy(buffer, start, prevBuffer, 0, curCount);
						} else {
							int prevLen = prevBuffer.length;
							byte[] temp = new byte[curCount + prevLen];
							System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
							System.arraycopy(buffer, start, temp, prevLen, curCount);
							prevBuffer = temp;
						}
					}
					
					if (readBuffer() <= 0) {
						if (prevBuffer != null) {
							return new String(prevBuffer, 0, prevBuffer.length, charset);
						} else {
							return null;
						}
					} else {
						count = this.count;
						if (buffer[0] == LF) { // \n
							index = 1;
							start = 1;
						} else if (buffer[0] == CR) { // \r\n
							index = 2;
							start = 2;
						} else {
							index = 0;
							start = 0;
							
							// �����з����뵽֮ǰ�Ļ�����
							if (prevBuffer == null) {
								prevBuffer = new byte[]{CONTINUECHAR};
							} else {
								int prevLen = prevBuffer.length;
								byte[] temp = new byte[prevLen + 1];
								System.arraycopy(prevBuffer, 0, temp, 0, prevLen);
								temp[prevLen] = CONTINUECHAR;
								prevBuffer = temp;
							}
						}
					}
				}
			} else {
				++index;
			}
		}
	}

	// ÿ�����ݶ���һ���ַ���
	private String readLineString(LineBytes line) throws IOException {
		byte[] buffer = line.buffer;
		int count = line.count;
		if (count < 1) {
			return "";
		} else if (count < 2) {
			return new String(buffer, line.i, count, charset);
		}
		
		int i = line.i;
		if (isQuote && buffer[i] == '"' && buffer[i + count - 1] == '"') {
			String str = new String(buffer, i + 1, count - 2, charset);
			return Escape.remove(str, escapeChar);
		} else if (isSingleQuote && buffer[i] == '\'' && buffer[i + count - 1] == '\'') {
			String str = new String(buffer, i + 1, count - 2, charset);
			return Escape.remove(str, '\\');
		} else {
			return new String(buffer, i, count, charset);
		}
	}
	
	// �����ֶ����ͰѲ���е��У���ת�ɶ���
	private Object[] readLine(LineBytes line, byte []colTypes) throws IOException {
		if (colSeparators != null) {
			return readLine2(line, colTypes);
		}
		
		int colCount = colTypes.length;
		Object []values = new Object[colCount];
		int count = line.count;
		if (count < 1) {
			return values;
		}
		
		byte[] buffer = line.buffer;
		int index = line.i;
		int end = index + count;
		
		byte colSeparator = this.colSeparator;
		int []selIndex = this.selIndex;
		char escapeChar = this.escapeChar;
		boolean doQuoteMatch = this.doQuoteMatch; // �Ƿ�������ƥ��
		boolean doSingleQuoteMatch = this.doSingleQuoteMatch; // �Ƿ���������ƥ��
		boolean doBracketsMatch = this.doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
		
		int colIndex = 0;
		int start = index;
		int BracketsLevel = 0; // ���ŵĲ�������pѡ��ʱ��Ϊ��������ƥ����ֵ�
		
		while (index < end && colIndex < colCount) {
			byte c = buffer[index];
			if (BracketsLevel == 0 && c == colSeparator) {
				// �н���
				if (selIndex == null || selIndex[colIndex] != -1) {
					values[colIndex] = parse(buffer, start, index, colIndex);
				}
				
				colIndex++;
				start = ++index;
			} else if (doQuoteMatch && c == '"') {
				// ������ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '"') {
						index++;
						if (escapeChar != '"' || index == end || buffer[index] != '"') {
							break;
						}
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
			} else if (doSingleQuoteMatch && c == '\'') {
				// �ҵ�����ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '\'') {
						index++;
						break;
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
			} else if (doBracketsMatch) {
				if (c == '(' || c == '[' || c == '{') {
					BracketsLevel++;
				} else if (BracketsLevel > 0 && (c == ')' || c == ']' || c == '}')) {
					BracketsLevel--;
				}
				
				index++;
			} else {
				index++;
			}
		}
		
		if (colIndex < colCount && (selIndex == null || selIndex[colIndex] != -1)) {
			values[colIndex] = parse(buffer, start, end, colIndex);
		}

		return values;
	}
	
	// ���ַ��зָ���
	// �����ֶ����ͰѲ���е��У���ת�ɶ���
	private Object[] readLine2(LineBytes line, byte []colTypes) throws IOException {
		int colCount = colTypes.length;
		Object []values = new Object[colCount];
		int count = line.count;
		if (count < 1) {
			return values;
		}
		
		byte[] buffer = line.buffer;
		int index = line.i;
		int end = index + count;
		
		byte []colSeparators = this.colSeparators;
		int sepLen = colSeparators.length;
		int []selIndex = this.selIndex;
		char escapeChar = this.escapeChar;
		boolean doQuoteMatch = this.doQuoteMatch; // �Ƿ�������ƥ��
		boolean doSingleQuoteMatch = this.doSingleQuoteMatch; // �Ƿ���������ƥ��
		boolean doBracketsMatch = this.doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
		
		int colIndex = 0;
		int start = index;
		int BracketsLevel = 0; // ���ŵĲ�������pѡ��ʱ��Ϊ��������ƥ����ֵ�
		
		while (index < end && colIndex < colCount) {
			if (BracketsLevel == 0 && isColSeparators(buffer, index, end, colSeparators, sepLen)) {
				// �н���
				if (selIndex == null || selIndex[colIndex] != -1) {
					values[colIndex] = parse(buffer, start, index, colIndex);
				}
				
				colIndex++;
				index += sepLen;
				start = index;
				continue;
			}

			byte c = buffer[index];
			if (doQuoteMatch && c == '"') {
				// ������ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '"') {
						index++;
						if (escapeChar != '"' || index == end || buffer[index] != '"') {
							break;
						}
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
			} else if (doSingleQuoteMatch && c == '\'') {
				// �ҵ�����ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '\'') {
						index++;
						break;
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
			} else if (doBracketsMatch) {
				if (c == '(' || c == '[' || c == '{') {
					BracketsLevel++;
				} else if (BracketsLevel > 0 && (c == ')' || c == ']' || c == '}')) {
					BracketsLevel--;
				}
				
				index++;
			} else {
				index++;
			}
		}
		
		if (colIndex < colCount && (selIndex == null || selIndex[colIndex] != -1)) {
			values[colIndex] = parse(buffer, start, end, colIndex);
		}

		return values;
	}
	
	// ������ʱ�����������͡�����ƥ�䡢����ƥ��ȼ��
	// �����ֶ����ͰѲ���е��У���ת�ɶ���
	private Object[] readLineOnCheck(LineBytes line, byte []colTypes) throws IOException {
		if (colSeparators != null) {
			return readLineOnCheck2(line, colTypes);
		}

		int count = line.count;
		if (count < 1) {
			return null;
		}
		
		byte[] buffer = line.buffer;
		int index = line.i;
		int end = index + count;
		int colCount = colTypes.length;
		Object []values = new Object[colCount];
		
		byte colSeparator = this.colSeparator;
		int []selIndex = this.selIndex;
		char escapeChar = this.escapeChar;
		boolean doQuoteMatch = this.doQuoteMatch; // �Ƿ�������ƥ��
		boolean doSingleQuoteMatch = this.doSingleQuoteMatch; // �Ƿ���������ƥ��
		boolean doBracketsMatch = this.doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
		boolean checkValueType = this.checkValueType;
		
		int colIndex = 0;
		int start = index;
		int BracketsLevel = 0; // ���ŵĲ�������pѡ��ʱ��Ϊ��������ƥ����ֵ�
		
		Next:
		while (index < end && colIndex < colCount) {
			byte c = buffer[index];
			if (BracketsLevel == 0 && c == colSeparator) {
				// �н���
				if (selIndex == null || selIndex[colIndex] != -1) {
					if (checkValueType) {
						if (!parse(buffer, start, index, colIndex, values)) {
							return null;
						}
					} else {
						values[colIndex] = parse(buffer, start, index, colIndex);
					}
				}
				
				colIndex++;
				start = ++index;
			} else if (doQuoteMatch && c == '"') {
				// ������ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '"') {
						index++;
						if (escapeChar != '"' || index == end || buffer[index] != '"') {
							continue Next;
						}
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
				
				// û�ҵ�ƥ������ŷ��ؿ�
				return null;
			} else if (doSingleQuoteMatch && c == '\'') {
				// �ҵ�����ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '\'') {
						index++;
						continue Next;
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
				
				// û�ҵ�ƥ��ĵ����ŷ��ؿ�
				return null;
			} else if (doBracketsMatch) {
				if (c == '(' || c == '[' || c == '{') {
					BracketsLevel++;
				} else if (BracketsLevel > 0 && (c == ')' || c == ']' || c == '}')) {
					BracketsLevel--;
				}
				
				index++;
			} else {
				index++;
			}
		}
		
		if (BracketsLevel != 0) {
			// �в�ƥ�������
			return null;
		}
		
		if (colIndex < colCount) {
			if (checkColCount && colIndex + 1 < colCount) {
				return null;
			}
			
			if (selIndex == null || selIndex[colIndex] != -1) {
				if (checkValueType) {
					if (!parse(buffer, start, end, colIndex, values)) {
						return null;
					}
				} else {
					values[colIndex] = parse(buffer, start, end, colIndex);
				}
			}
		}

		return values;
	}

	// ���ַ��зָ���
	// �����ֶ����ͰѲ���е��У���ת�ɶ���
	private Object[] readLineOnCheck2(LineBytes line, byte []colTypes) throws IOException {
		int count = line.count;
		if (count < 1) {
			return null;
		}
		
		byte[] buffer = line.buffer;
		int index = line.i;
		int end = index + count;
		int colCount = colTypes.length;
		Object []values = new Object[colCount];
		
		byte []colSeparators = this.colSeparators;
		int sepLen = colSeparators.length;
		int []selIndex = this.selIndex;
		char escapeChar = this.escapeChar;
		boolean doQuoteMatch = this.doQuoteMatch; // �Ƿ�������ƥ��
		boolean doSingleQuoteMatch = this.doSingleQuoteMatch; // �Ƿ���������ƥ��
		boolean doBracketsMatch = this.doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
		boolean checkValueType = this.checkValueType;
		
		int colIndex = 0;
		int start = index;
		int BracketsLevel = 0; // ���ŵĲ�������pѡ��ʱ��Ϊ��������ƥ����ֵ�
		
		Next:
		while (index < end && colIndex < colCount) {
			if (BracketsLevel == 0 && isColSeparators(buffer, index, end, colSeparators, sepLen)) { // �н���
				// �н���
				if (selIndex == null || selIndex[colIndex] != -1) {
					if (checkValueType) {
						if (!parse(buffer, start, index, colIndex, values)) {
							return null;
						}
					} else {
						values[colIndex] = parse(buffer, start, index, colIndex);
					}
				}
				
				colIndex++;
				index += sepLen;
				start = index;
				continue;
			}
			
			byte c = buffer[index];
			if (doQuoteMatch && c == '"') {
				// ������ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '"') {
						index++;
						if (escapeChar != '"' || index == end || buffer[index] != '"') {
							continue Next;
						}
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
				
				// û�ҵ�ƥ������ŷ��ؿ�
				return null;
			} else if (doSingleQuoteMatch && c == '\'') {
				// �ҵ�����ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '\'') {
						index++;
						continue Next;
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
				
				// û�ҵ�ƥ��ĵ����ŷ��ؿ�
				return null;
			} else if (doBracketsMatch) {
				if (c == '(' || c == '[' || c == '{') {
					BracketsLevel++;
				} else if (BracketsLevel > 0 && (c == ')' || c == ']' || c == '}')) {
					BracketsLevel--;
				}
				
				index++;
			} else {
				index++;
			}
		}
		
		if (BracketsLevel != 0) {
			// �в�ƥ�������
			return null;
		}
		
		if (colIndex < colCount) {
			if (checkColCount && colIndex + 1 < colCount) {
				return null;
			}
			
			if (selIndex == null || selIndex[colIndex] != -1) {
				if (checkValueType) {
					if (!parse(buffer, start, end, colIndex, values)) {
						return null;
					}
				} else {
					values[colIndex] = parse(buffer, start, end, colIndex);
				}
			}
		}

		return values;
	}

	private Object[] readLine(LineBytes line) throws IOException {
		if (colSeparators != null) {
			return readLine2(line);
		}
		
		int count = line.count;
		if (count < 1) {
			return new Object[0];
		}
		
		byte[] buffer = line.buffer;
		int index = line.i;
		int end = index + count;
		
		String charset = this.charset;
		byte colSeparator = this.colSeparator;
		char escapeChar = this.escapeChar;
		boolean isTrim = this.isTrim;
		boolean doQuoteMatch = this.doQuoteMatch; // �Ƿ�������ƥ��
		boolean doSingleQuoteMatch = this.doSingleQuoteMatch; // �Ƿ���������ƥ��
		boolean doBracketsMatch = this.doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
		
		ArrayList<Object> list = new ArrayList<Object>();
		int BracketsLevel = 0; // ���ŵĲ�������pѡ��ʱ��Ϊ��������ƥ����ֵ�
		int start = index; // �е���ʼλ��
		
		while (index < end) {
			byte c = buffer[index];
			if (BracketsLevel == 0 && c == colSeparator) {
				// �н���
				int len = index - start;
				if (len > 0) {
					String str = new String(buffer, start, len, charset);
					if (isTrim) {
						str = str.trim();
					}
					
					list.add(parse(str));
				} else {
					list.add(null);
				}
				
				start = ++index;
			} else if (doQuoteMatch && c == '"') {
				// ������ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '"') {
						index++;
						if (escapeChar != '"' || index == end || buffer[index] != '"') {
							break;
						}
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
			} else if (doSingleQuoteMatch && c == '\'') {
				// �ҵ�����ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '\'') {
						index++;
						break;
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
			} else if (doBracketsMatch) {
				if (c == '(' || c == '[' || c == '{') {
					BracketsLevel++;
				} else if (BracketsLevel > 0 && (c == ')' || c == ']' || c == '}')) {
					BracketsLevel--;
				}
				
				index++;
			} else {
				index++;
			}
		}
		
		int len = end - start;
		if (len > 0) {
			String str = new String(buffer, start, len, charset);
			if (isTrim) {
				str = str.trim();
			}
			
			list.add(parse(str));
		} else {
			list.add(null);
		}
		
		return list.toArray();
	}
	
	private static boolean isColSeparators(byte []buffer, int index, int end, byte []colSeparators, int len) {
		if (end - index < len) {
			return false;
		}
		
		for (int i = 0; i < len; ++i, ++index) {
			if (buffer[index] != colSeparators[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	// ���ַ��зָ���
	private Object[] readLine2(LineBytes line) throws IOException {
		int count = line.count;
		if (count < 1) {
			return new Object[0];
		}
		
		byte[] buffer = line.buffer;
		int index = line.i;
		int end = index + count;
		
		String charset = this.charset;
		byte []colSeparators = this.colSeparators;
		int sepLen = colSeparators.length;
		char escapeChar = this.escapeChar;
		boolean isTrim = this.isTrim;
		boolean doQuoteMatch = this.doQuoteMatch; // �Ƿ�������ƥ��
		boolean doSingleQuoteMatch = this.doSingleQuoteMatch; // �Ƿ���������ƥ��
		boolean doBracketsMatch = this.doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
		
		ArrayList<Object> list = new ArrayList<Object>();
		int start = index;
		int BracketsLevel = 0; // ���ŵĲ�������pѡ��ʱ��Ϊ��������ƥ����ֵ�
		
		while (index < end) {
			if (BracketsLevel == 0 && isColSeparators(buffer, index, end, colSeparators, sepLen)) {
				// �н���
				int len = index - start;
				if (len > 0) {
					String str = new String(buffer, start, len, charset);
					if (isTrim) {
						str = str.trim();
					}
					
					list.add(parse(str));
				} else {
					list.add(null);
				}
				
				index += sepLen;
				start = index;
				continue;
			}

			byte c = buffer[index];
			if (doQuoteMatch && c == '"') {
				// ������ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '"') {
						index++;
						if (escapeChar != '"' || index == end || buffer[index] != '"') {
							break;
						}
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
			} else if (doSingleQuoteMatch && c == '\'') {
				// �ҵ�����ƥ�䣬���������ڵ��зָ���
				for (++index; index < end; ++index) {
					if (buffer[index] == '\'') {
						index++;
						break;
					} else if (buffer[index] == escapeChar) {
						index++;
					}
				}
			} else if (doBracketsMatch) {
				if (c == '(' || c == '[' || c == '{') {
					BracketsLevel++;
				} else if (BracketsLevel > 0 && (c == ')' || c == ']' || c == '}')) {
					BracketsLevel--;
				}
				
				index++;
			} else {
				index++;
			}
		}
		
		int len = end - start;
		if (len > 0) {
			String str = new String(buffer, start, len, charset);
			if (isTrim) {
				str = str.trim();
			}
			
			list.add(parse(str));
		} else {
			list.add(null);
		}
		
		return list.toArray();
	}
	
	/**
	 * ��������
	 * @return ��ֵ��ɵ�����
	 * @throws IOException
	 */
	public Object[] readFirstLine() throws IOException {
		if (isStringMode) {
			String line = readLineString();
			if (line == null) {
				return null;
			} else if (parseMode == PARSEMODE_SINGLE_STRING) {
				if (isQuote || isSingleQuote) {
					line = Escape.removeEscAndQuote(line, escapeChar);
				}
				
				return new Object[] {line};
			} else if (colTypes != null) {
				return readLine(line, colTypes);
			} else {
				return readLine(line);
			}
		}
		
		LineBytes line = readLineBytes();
		if (line == null) {
			return null;
		} else if (parseMode == PARSEMODE_SINGLE_STRING) {
			return new Object[] {readLineString(line)};
		} else if (colTypes != null) {
			return readLine(line, colTypes);
		} else {
			return readLine(line);
		}
	}
	
	/**
	 * ������һ�У�����������򷵻�null
	 * @return Object[]
	 * @throws IOException
	 */
	public Object[] readLine() throws IOException {
		if (isStringMode) {
			if (parseMode == PARSEMODE_DELETE) {
				while (true) {
					String line = readLineString();
					if (line == null) {
						return null;
					}
					
					Object []vals = readLineOnCheck(line, colTypes);
					if (vals != null) {
						return vals;
					}
				}
			} else if (parseMode == PARSEMODE_EXCEPTION) {
				String line = readLineString();
				if (line == null) {
					return null;
				}
				
				Object []vals = readLineOnCheck(line, colTypes);
				if (vals != null) {
					return vals;
				} else {
					if (line.length() > 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(line + mm.getMessage("file.rowDataError"));
					} else {
						return null;
					}
				}
			} else {
				String line = readLineString();
				if (line == null) {
					return null;
				} else if (parseMode == PARSEMODE_SINGLE_STRING) {
					if (isQuote || isSingleQuote) {
						line = Escape.removeEscAndQuote(line, escapeChar);
					}
					
					return new Object[] {line};
				} else if (colTypes != null) {
					return readLine(line, colTypes);
				} else {
					return readLine(line);
				}
			}
		} else if (parseMode == PARSEMODE_DELETE) {
			while (true) {
				LineBytes line = readLineBytes();
				if (line == null) {
					return null;
				}
				
				Object []vals = readLineOnCheck(line, colTypes);
				if (vals != null) {
					return vals;
				}
			}
		} else if (parseMode == PARSEMODE_EXCEPTION) {
			LineBytes line = readLineBytes();
			if (line == null) {
				return null;
			}
			
			Object []vals = readLineOnCheck(line, colTypes);
			if (vals != null) {
				return vals;
			} else {
				if (line.count > 0) {
					String strLine = new String(line.buffer, line.i, line.count);
					MessageManager mm = EngineMessage.get();
					throw new RQException(strLine + mm.getMessage("file.rowDataError"));
				} else {
					return null;
				}
			}
		} else {
			// PARSEMODE_DEFAULT��PARSEMODE_MULTI_STRING��PARSEMODE_SINGLE_STRING
			LineBytes line = readLineBytes();
			if (line == null) {
				return null;
			} else if (parseMode == PARSEMODE_SINGLE_STRING) {
				return new Object[] {readLineString(line)};
			} else if (colTypes != null) {
				return readLine(line, colTypes);
			} else {
				return readLine(line);
			}
		}
	}

	/**
	 * ������һ�У�����������򷵻�false�����򷵻�true
	 * @return boolean
	 * @throws IOException
	 */
	public boolean skipLine() throws IOException {
		// �Ƿ����������ڵĻس�
		boolean skipQuoteEnter = escapeChar == '"';
		byte[] buffer = this.buffer;
		int count = this.count;
		int index = this.index;
		boolean sign = index < count;
		
		Next:
		while (true) {
			if (index >= count) {
				// ���������ֽ�
				if (readBuffer() <= 0) {
					return sign;
				} else {
					count = this.count;
					index = 0;
					sign = true;
				}
			}
			
			if (buffer[index] == LF) {
				this.index = index + 1;
				return true;
			} else if (skipQuoteEnter && buffer[index] == '"') {
				// ������ƥ�䣬���������ŵ���һ���ַ�
				++index;
				while (true) {
					if (index == count) {
						// ���������ֽ�
						if (readBuffer() <= 0) {
							return true;
						} else {
							count = this.count;
							index = 0;
						}
					}
					
					if (buffer[index] == '"') {
						++index;
						if (index < count) {
							if (buffer[index] != '"') {
								// �ҵ�����ƥ��
								continue Next;
							} else {
								// ��������˫�����Ƕ�����ת��
								++index;
							}
						} else {
							// ���������ֽ�
							if (readBuffer() <= 0) {
								return true;
							} else {
								count = this.count;
								if (buffer[0] != '"') {
									// �ҵ�����ƥ��
									index = 0;
									continue Next;
								} else {
									// ��������˫�����Ƕ�����ת��
									index = 1;
								}
							}
						}
					} else {
						++index;
					}
				}
			} else if (isContinueLine && buffer[index] == CONTINUECHAR) {
				// ����������У���鵱ǰ�ַ��Ƿ��ǡ�\��
				++index;
				if (index < count) {
					if (buffer[index] == LF) { // \n
						++index;
					} else if (buffer[index] == CR) { // \r\n
						// CR�������LF������LF
						++index;
						if (index == count) {
							// \n����һ��������
							if (readBuffer() <= 0) {
								return true;
							} else {
								count = this.count;
								index = 1;
							}
						} else {
							++index;
						}
					}
				} else {
					if (readBuffer() <= 0) {
						return true;
					} else {
						count = this.count;
						if (buffer[0] == LF) { // \n
							index = 1;
						} else if (buffer[0] == CR) { // \r\n
							index = 2;
						} else {
							index = 0;
						}
					}
				}
			} else {
				index++;
			}
		}
	}

	// ������һ�е���ʼλ�ã���������-1
	public static int readLine(char []buffer, int index, char colSeparator, ArrayList<String> line) {
		int start = index;
		int count = buffer.length;
		while (index < count) {
			if (buffer[index] == colSeparator) { // �н���
				line.add(new String(buffer, start, index - start));
				index++;
				start = index;
			} else if (buffer[index] == LineImporter.LF) { // \n
				// \r\n �� \n���ֻ��з���Ҫ����
				if (index > start) {
					if (buffer[index - 1] == LineImporter.CR) {
						line.add(new String(buffer, start, index - start - 1));
					} else {
						line.add(new String(buffer, start, index - start));
					}
				} else {
					line.add(null);
				}

				index++;
				start = index;
				return index;
			} else {
				index++;
			}
		}

		if (count > start) {
			line.add(new String(buffer, start, count - start));
		} else if (line.size() > 0) { // ���һ���ֶο�
			line.add(null);
		} // ����

		return -1;
	}

	/**
	 * �ر�����
	 * @throws IOException
	 */
	public void close() throws IOException {
		is.close();
	}
	
	// �ж��ַ����Ƿ��Ǳ�ʾnull�ı�ʶ��
	private static boolean isNull(String str) {
		String []nulls = Env.getNullStrings();
		if (nulls != null) {
			for (String nullStr : nulls) {
				if (str.equalsIgnoreCase(nullStr)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	// ���ַ��������ɶ���
	private Object parse(String str) {
		int len = str.length();
		if (len > 1) {
			char c = str.charAt(0);
			if ((isQuote && c == '"') || (isSingleQuote && c == '\'')) {
				// �ڽ�β�Ƿ�������
				if (str.charAt(len - 1) == c) {
					return Escape.remove(str.substring(1, len - 1), escapeChar);
				} else {
					return str;
				}
			}
		}
		
		if (parseMode == PARSEMODE_MULTI_STRING) {
			if (isNull(str)) {
				return null;
			} else {
				return str;
			}
		} else {
			return Variant.parseDirect(str);
		}
	}
	
	// ���ֽ���������ɶ���
	private Object parse(byte []bytes, int start, int end, int col) throws UnsupportedEncodingException {
		if (isTrim) {
			while (start < end && Character.isWhitespace(bytes[start])) {
				start++;
			}
			
			while (end > start && Character.isWhitespace(bytes[end - 1])) {
				end--;
			}
		}
		
		if (start >= end) {
			return null;
		}

		byte []types = this.colTypes;
		byte c = bytes[start];
		if ((isQuote && c == '"') || (isSingleQuote && c == '\'')) {
			// �ڽ�β�Ƿ�������
			if (bytes[end - 1] == c) {
				start++;
				end--;
				if (start < end) {
					if (types[col] == Types.DT_DEFAULT || types[col] == Types.DT_STRING) {
						String str = new String(bytes, start, end - start, charset);
						return Escape.remove(str, escapeChar);
					}
				} else if (start == end) {
					return "";
				} else {
					return String.valueOf(c);
				}
			} else {
				return new String(bytes, start, end - start, charset);
			}
		} else if (parseMode == PARSEMODE_MULTI_STRING) {
			// ֱ�ӷ��ش�
			String str = new String(bytes, start, end - start, charset);
			if (isNull(str)) {
				return null;
			} else {
				return str;
			}
		}

		switch (types[col]) {
		case Types.DT_STRING:
			String str = new String(bytes, start, end - start, charset);
			if (isNull(str)) {
				return null;
			} else {
				return str;
			}
		case Types.DT_INT:
			Number num = parseInt(bytes, start, end);
			if (num != null) return num;

			num = parseLong(bytes, start, end);
			if (num != null) {
				types[col] = Types.DT_LONG;
				return num;
			}

			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(bytes, start, end);
				if (fd != null) {
					types[col] = Types.DT_DOUBLE;
					return new Double(fd.doubleValue());
				}
			} catch (RuntimeException e) {
			}

			break;
		case Types.DT_DOUBLE:
			if (bytes[end - 1] == '%') { // 5%
				try {
					FloatingDecimal fd = FloatingDecimal.readJavaFormatString(bytes, start, end - 1);
					if (fd != null) return new Double(fd.doubleValue() / 100);
				} catch (RuntimeException e) {
				}
			} else {
				try {
					FloatingDecimal fd = FloatingDecimal.readJavaFormatString(bytes, start, end);
					if (fd != null) return new Double(fd.doubleValue());
				} catch (RuntimeException e) {
				}
			}

			break;
		case Types.DT_DATE:
			String text = new String(bytes, start, end - start, charset);
			Date date = fmts[col].parse(text);
			if (date != null) return new java.sql.Date(date.getTime());

			break;
		case Types.DT_DECIMAL:
			try {
				return new BigDecimal(new String(bytes, start, end - start, charset));
			} catch (NumberFormatException e) {
			}

			break;
		case Types.DT_LONG:
			if (end - start > 2 && bytes[start] == '0' &&
				(bytes[start + 1] == 'X' || bytes[start + 1] == 'x')) {
				num = parseLong(bytes, start + 2, end, 16);
			} else {
				num = parseLong(bytes, start, end);
			}

			if (num != null) {
				return num;
			}

			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(bytes, start, end);
				if (fd != null) {
					types[col] = Types.DT_DOUBLE;
					return new Double(fd.doubleValue());
				}
			} catch (RuntimeException e) {
			}

			break;
		case Types.DT_DATETIME:
			text = new String(bytes, start, end - start, charset);
			date = fmts[col].parse(text);
			if (date != null) return new java.sql.Timestamp(date.getTime());

			break;
		case Types.DT_TIME:
			text = new String(bytes, start, end - start, charset);
			date = fmts[col].parse(text);
			if (date != null) return new java.sql.Time(date.getTime());

			break;
		case Types.DT_BOOLEAN:
			Boolean b = parseBoolean(bytes, start, end);
			if (b != null) return b;

			break;
		case Types.DT_SERIALBYTES:
			num = parseLong(bytes, start, end);
			if (num != null) {
				return new SerialBytes(num.longValue(), serialByteLens[col]);
			}
			
			break;
		default:
			str = new String(bytes, start, end - start, charset);
			if (isNull(str)) {
				return null;
			}
			
			Object val = Variant.parseDirect(str);
			types[col] = Variant.getObjectType(val);
			
			if (types[col] == Types.DT_DATE) {
				fmts[col] = DateFormatFactory.get().getDateFormatX();
			} else if (types[col] == Types.DT_DATETIME) {
				fmts[col] = DateFormatFactory.get().getDateTimeFormatX();
			} else if (types[col] == Types.DT_TIME) {
				fmts[col] = DateFormatFactory.get().getTimeFormatX();
			}
			
			return val;
		}
		
		//types[col] = getObjectType(val);
		String str = new String(bytes, start, end - start, charset);
		if (isNull(str)) {
			return null;
		} else {
			return Variant.parseDirect(str);
		}
	}
	
	private Object parse(String str, int col) throws UnsupportedEncodingException {
		int start = 0, end = str.length();
		
		if (isTrim) {
			while (start < end && Character.isWhitespace(str.charAt(start))) {
				start++;
			}
			
			while (end > start && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		}
		
		if (start >= end) {
			return null;
		}

		byte []types = this.colTypes;
		char c = str.charAt(start);
		if ((isQuote && c == '"') || (isSingleQuote && c == '\'')) {
			// �ڽ�β�Ƿ�������
			if (str.charAt(end - 1) == c) {
				start++;
				end--;
				if (start < end) {
					if (types[col] == Types.DT_DEFAULT || types[col] == Types.DT_STRING) {
						return Escape.removeEscAndQuote(str, escapeChar);
					}
				} else if (start == end) {
					return "";
				} else {
					return String.valueOf(c);
				}
			} else {
				return str;
			}
		} else if (parseMode == PARSEMODE_MULTI_STRING) {
			// ֱ�ӷ��ش�
			if (isNull(str)) {
				return null;
			} else {
				return str;
			}
		}

		switch (types[col]) {
		case Types.DT_STRING:
			if (isNull(str)) {
				return null;
			} else {
				return str;
			}
		case Types.DT_INT:
			Number num = Variant.parseNumber(str);
			if (num != null) {
				return num;
			} else {
				break;
			}
		case Types.DT_DOUBLE:
			num = Variant.parseDouble(str);
			if (num != null) {
				return num;
			} else {
				break;
			}
		case Types.DT_DATE:
			Date date = fmts[col].parse(str);
			if (date != null) return new java.sql.Date(date.getTime());

			break;
		case Types.DT_DECIMAL:
			try {
				return new BigDecimal(str);
			} catch (NumberFormatException e) {
			}

			break;
		case Types.DT_LONG:
			num = parseLong(str);
			if (num != null) {
				return num;
			}

			break;
		case Types.DT_DATETIME:
			date = fmts[col].parse(str);
			if (date != null) return new java.sql.Timestamp(date.getTime());

			break;
		case Types.DT_TIME:
			date = fmts[col].parse(str);
			if (date != null) return new java.sql.Time(date.getTime());

			break;
		case Types.DT_BOOLEAN:
			if (str.equals("true")) return Boolean.TRUE;
			if (str.equals("false")) return Boolean.FALSE;

			break;
		case Types.DT_SERIALBYTES:
			num = Variant.parseLong(str);
			if (num != null) {
				return new SerialBytes(num.longValue(), serialByteLens[col]);
			}
			
			break;
		default:
			if (isNull(str)) {
				return null;
			}
			
			Object val = Variant.parseDirect(str);
			types[col] = Variant.getObjectType(val);
			
			if (types[col] == Types.DT_DATE) {
				fmts[col] = DateFormatFactory.get().getDateFormatX();
			} else if (types[col] == Types.DT_DATETIME) {
				fmts[col] = DateFormatFactory.get().getDateTimeFormatX();
			} else if (types[col] == Types.DT_TIME) {
				fmts[col] = DateFormatFactory.get().getTimeFormatX();
			}
			
			return val;
		}
		
		if (isNull(str)) {
			return null;
		} else {
			return Variant.parseDirect(str);
		}
	}
	
	// ���ֽ���������ɶ���������Ͳ����򷵻�false
	private boolean parse(String str, int col, Object []outValue) throws UnsupportedEncodingException {
		int start = 0, end = str.length();
		
		if (isTrim) {
			while (start < end && Character.isWhitespace(str.charAt(start))) {
				start++;
			}
			
			while (end > start && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		}
		
		if (start >= end) {
			return true;
		}

		byte []types = this.colTypes;
		char c = str.charAt(start);
		if ((isQuote && c == '"') || (isSingleQuote && c == '\'')) {
			// �ڽ�β�Ƿ�������
			if (str.charAt(end - 1) == c) {
				start++;
				end--;
				if (start < end) {
					if (types[col] == Types.DT_DEFAULT || types[col] == Types.DT_STRING) {
						outValue[col] = Escape.remove(str, escapeChar);
						return true;
					}
				} else if (start == end) {
					outValue[col] = "";
					return true;
				} else {
					outValue[col] = String.valueOf(c);
					return true;
				}
			} else {
				outValue[col] = str;
				return true;
			}
		} else if (parseMode == PARSEMODE_MULTI_STRING) {
			// ֱ�ӷ��ش�
			if (isNull(str)) {
				outValue[col] = null;
				return true;
			} else {
				outValue[col] = str;
				return true;
			}
		}

		switch (types[col]) {
		case Types.DT_STRING:
			if (!isNull(str)) {
				outValue[col] = str;
			}
			
			return true;
		case Types.DT_INT:
			Number num = Variant.parseNumber(str);
			if (num != null) {
				outValue[col] = num;
				return true;
			}

			break;
		case Types.DT_DOUBLE:
			num = Variant.parseDouble(str);
			if (num != null) {
				outValue[col] = num;
				return true;
			}

			break;
		case Types.DT_DATE:
			Date date = fmts[col].parse(str);
			if (date != null) {
				outValue[col] = new java.sql.Date(date.getTime());
				return true;
			}

			break;
		case Types.DT_DECIMAL:
			try {
				outValue[col] = new BigDecimal(str);
				return true;
			} catch (NumberFormatException e) {
			}

			break;
		case Types.DT_LONG:
			num = Variant.parseLong(str);
			if (num != null) {
				outValue[col] = num;
				return true;
			}

			break;
		case Types.DT_DATETIME:
			date = fmts[col].parse(str);
			if (date != null) {
				outValue[col] = new java.sql.Timestamp(date.getTime());
				return true;
			}

			break;
		case Types.DT_TIME:
			date = fmts[col].parse(str);
			if (date != null) {
				outValue[col] = new java.sql.Time(date.getTime());
				return true;
			}

			break;
		case Types.DT_BOOLEAN:
			if (str.equals("true")) {
				outValue[col] = Boolean.TRUE;
				return true;
			} else if (str.equals("false")) {
				outValue[col] = Boolean.FALSE;
				return true;
			}

			break;
		case Types.DT_SERIALBYTES:
			num = Variant.parseLong(str);
			if (num != null) {
				outValue[col] = new SerialBytes(num.longValue(), serialByteLens[col]);
				return true;
			}
			
			break;
		default:
			if (isNull(str)) {
				return true;
			}
			
			outValue[col] = Variant.parseDirect(str);
			types[col] = Variant.getObjectType(outValue[col]);
			
			if (types[col] == Types.DT_DATE) {
				fmts[col] = DateFormatFactory.get().getDateFormatX();
			} else if (types[col] == Types.DT_DATETIME) {
				fmts[col] = DateFormatFactory.get().getDateTimeFormatX();
			} else if (types[col] == Types.DT_TIME) {
				fmts[col] = DateFormatFactory.get().getTimeFormatX();
			}
			
			return true;
		}

		if (isNull(str)) {
			return true;
		} else {
			return false;
		}
	}
	
	// ���ֽ���������ɶ���������Ͳ����򷵻�false
	private boolean parse(byte []bytes, int start, int end, int col, Object []outValue) throws UnsupportedEncodingException {
		if (isTrim) {
			while (start < end && bytes[start] == ' ') {
				start++;
			}
			
			while (end > start && bytes[end - 1] == ' ') {
				end--;
			}
		}
		
		if (start >= end) {
			return true;
		}

		byte []types = this.colTypes;
		byte c = bytes[start];
		if ((isQuote && c == '"') || (isSingleQuote && c == '\'')) {
			// �ڽ�β�Ƿ�������
			if (bytes[end - 1] == c) {
				start++;
				end--;
				if (start < end) {
					if (types[col] == Types.DT_DEFAULT || types[col] == Types.DT_STRING) {
						String str = new String(bytes, start, end - start, charset);
						outValue[col] = Escape.remove(str, escapeChar);
						return true;
					}
				} else if (start == end) {
					outValue[col] = "";
					return true;
				} else {
					outValue[col] = String.valueOf(c);
					return true;
				}
			} else {
				outValue[col] = new String(bytes, start, end - start, charset);
				return true;
			}
		} else if (parseMode == PARSEMODE_MULTI_STRING) {
			// ֱ�ӷ��ش�
			String str = new String(bytes, start, end - start, charset);
			if (isNull(str)) {
				outValue[col] = null;
				return true;
			} else {
				outValue[col] = str;
				return true;
			}
		}

		switch (types[col]) {
		case Types.DT_STRING:
			String str = new String(bytes, start, end - start, charset);
			if (!isNull(str)) {
				outValue[col] = str;
			}
			
			return true;
		case Types.DT_INT:
			Number num = parseInt(bytes, start, end);
			if (num != null) {
				outValue[col] = num;
				return true;
			}

			num = parseLong(bytes, start, end);
			if (num != null) {
				types[col] = Types.DT_LONG;
				outValue[col] = num;
				return true;
			}

			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(bytes, start, end);
				if (fd != null) {
					types[col] = Types.DT_DOUBLE;
					outValue[col] = new Double(fd.doubleValue());
					return true;
				}
			} catch (RuntimeException e) {
			}

			break;
		case Types.DT_DOUBLE:
			if (bytes[end - 1] == '%') { // 5%
				try {
					FloatingDecimal fd = FloatingDecimal.readJavaFormatString(bytes, start, end - 1);
					outValue[col] = new Double(fd.doubleValue() / 100);
					return true;
				} catch (RuntimeException e) {
				}
			} else {
				try {
					FloatingDecimal fd = FloatingDecimal.readJavaFormatString(bytes, start, end);
					outValue[col] = new Double(fd.doubleValue());
					return true;
				} catch (RuntimeException e) {
				}
			}

			break;
		case Types.DT_DATE:
			String text = new String(bytes, start, end - start, charset);
			Date date = fmts[col].parse(text);
			if (date != null) {
				outValue[col] = new java.sql.Date(date.getTime());
				return true;
			}

			break;
		case Types.DT_DECIMAL:
			try {
				outValue[col] = new BigDecimal(new String(bytes, start, end - start, charset));
				return true;
			} catch (NumberFormatException e) {
			}

			break;
		case Types.DT_LONG:
			if (end - start > 2 && bytes[start] == '0' &&
				(bytes[start + 1] == 'X' || bytes[start + 1] == 'x')) {
				num = parseLong(bytes, start + 2, end, 16);
			} else {
				num = parseLong(bytes, start, end);
			}

			if (num != null) {
				outValue[col] = num;
				return true;
			}

			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(bytes, start, end);
				if (fd != null) {
					types[col] = Types.DT_DOUBLE;
					outValue[col] = new Double(fd.doubleValue());
					return true;
				}
			} catch (RuntimeException e) {
			}

			break;
		case Types.DT_DATETIME:
			text = new String(bytes, start, end - start, charset);
			date = fmts[col].parse(text);
			if (date != null) {
				outValue[col] = new java.sql.Timestamp(date.getTime());
				return true;
			}

			break;
		case Types.DT_TIME:
			text = new String(bytes, start, end - start, charset);
			date = fmts[col].parse(text);
			if (date != null) {
				outValue[col] = new java.sql.Time(date.getTime());
				return true;
			}

			break;
		case Types.DT_BOOLEAN:
			Boolean b = parseBoolean(bytes, start, end);
			if (b != null) {
				outValue[col] = b;
				return true;
			}

			break;
		case Types.DT_SERIALBYTES:
			num = parseLong(bytes, start, end);
			if (num != null) {
				outValue[col] = new SerialBytes(num.longValue(), serialByteLens[col]);
				return true;
			}
			
			break;
		default:
			str = new String(bytes, start, end - start, charset);
			if (isNull(str)) {
				return true;
			}
			
			outValue[col] = Variant.parseDirect(str);
			types[col] = Variant.getObjectType(outValue[col]);
			
			if (types[col] == Types.DT_DATE) {
				fmts[col] = DateFormatFactory.get().getDateFormatX();
			} else if (types[col] == Types.DT_DATETIME) {
				fmts[col] = DateFormatFactory.get().getDateTimeFormatX();
			} else if (types[col] == Types.DT_TIME) {
				fmts[col] = DateFormatFactory.get().getTimeFormatX();
			}
			
			return true;
		}

		String str = new String(bytes, start, end - start, charset);
		if (isNull(str)) {
			return true;
		} else {
			return false;
		}
	}
	
	// ��������ֵ����������ֵ�򷵻ؿ�
	private static Integer parseInt(byte []bytes, int i, int e) {
		int result = 0;
		boolean negative = false;
		int limit;
		int multmin;
		int digit;

		if (bytes[i] == '-') {
			negative = true;
			limit = Integer.MIN_VALUE;
			i++;
		} else {
			limit = -Integer.MAX_VALUE;
		}
		multmin = limit / 10;

		if (i < e) {
			digit = Character.digit((char)bytes[i++], 10);
			if (digit < 0) {
				return null;
			} else {
				result = -digit;
			}
		}

		while (i < e) {
			// Accumulating negatively avoids surprises near MAX_VALUE
			digit = Character.digit((char)bytes[i++], 10);
			if (digit < 0) {
				return null;
			}
			if (result < multmin) {
				return null;
			}
			result *= 10;
			if (result < limit + digit) {
				return null;
			}
			result -= digit;
		}

		if (negative) {
			if (i > 1) {
				return new Integer(result);
			} else { /* Only got "-" */
				return null;
			}
		} else {
			return new Integer( -result);
		}
	}
	
	// ����Longֵ������Longֵ�򷵻ؿ�
	private static Long parseLong(byte []bytes, int i, int e) {
		// 1L
		if (e - i > 1 && bytes[e - 1] == 'L') e--;
		
		long result = 0;
		boolean negative = false;
		long limit;
		long multmin;
		int digit;

		if (bytes[i] == '-') {
			negative = true;
			limit = Long.MIN_VALUE;
			i++;
		} else {
			limit = -Long.MAX_VALUE;
		}

		multmin = limit / 10;
		if (i < e) {
			digit = Character.digit((char)bytes[i++], 10);
			if (digit < 0) {
				return null;
			} else {
				result = -digit;
			}
		}

		while (i < e) {
			// Accumulating negatively avoids surprises near MAX_VALUE
			digit = Character.digit((char)bytes[i++], 10);
			if (digit < 0) {
				return null;
			}
			if (result < multmin) {
				return null;
			}
			result *= 10;
			if (result < limit + digit) {
				return null;
			}
			result -= digit;
		}

		if (negative) {
			if (i > 1) {
				return new Long(result);
			} else { /* Only got "-" */
				return null;
			}
		} else {
			return new Long( -result);
		}
	}

	// ����Longֵ������Longֵ�򷵻ؿ�
	private static Long parseLong(byte []bytes, int i, int e, int radix) {
		long result = 0;
		boolean negative = false;
		//int i = 0, max = s.length();
		long limit;
		long multmin;
		int digit;

		if (bytes[i] == '-') {
			negative = true;
			limit = Long.MIN_VALUE;
			i++;
		} else {
			limit = -Long.MAX_VALUE;
		}
		multmin = limit / radix;
		if (i < e) {
			digit = Character.digit((char)bytes[i++], radix);
			if (digit < 0) {
				return null;
			} else {
				result = -digit;
			}
		}
		while (i < e) {
			// Accumulating negatively avoids surprises near MAX_VALUE
			digit = Character.digit((char)bytes[i++], radix);
			if (digit < 0) {
				return null;
			}
			if (result < multmin) {
				return null;
			}
			result *= radix;
			if (result < limit + digit) {
				return null;
			}
			result -= digit;
		}

		if (negative) {
			if (i > 1) {
				return new Long(result);
			} else { /* Only got "-" */
				return null;
			}
		} else {
			return new Long(-result);
		}
	}
	
	private static Number parseLong(String s) {
		s = s.trim();
		int len = s.length();
		if (len == 0) return null;


		Long numObj = Variant.parseLong(s);
		if (numObj != null) return numObj;

		if (len > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'X' || s.charAt(1) == 'x')) {
			numObj = Variant.parseLong(s.substring(2), 16);
			if (numObj != null) return numObj;
		}

		if (s.endsWith("%")) { // 5%
			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(s.
					substring(0, s.length() - 1));
				if (fd != null)return new Double(fd.doubleValue() / 100);
			} catch (RuntimeException e) {
			}
		} else {
			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(s);
				if (fd != null) return new Double(fd.doubleValue());
			} catch (RuntimeException e) {
			}
		}

		return null;
	}

	// ��������ֵ�����ǲ���ֵ�򷵻ؿ�
	private static Boolean parseBoolean(byte []bytes, int i, int e) {
		int count = e - i;
		if (count == 4) {
			if (bytes[i] == 't' && bytes[i+1] == 'r' && bytes[i+2] == 'u' && bytes[i+3] == 'e')
				return Boolean.TRUE;
		} else if (count == 5) {
			if (bytes[i] == 'f' && bytes[i+1] == 'a' && bytes[i+2] == 'l' && bytes[i+3] == 's' && bytes[i+4] == 'e')
				return Boolean.FALSE;
		}

		return null;
	}
	
	private Object[] readLineOnCheck(String line, byte []colTypes) throws IOException {
		int count = line.length();
		if (count < 1) {
			return null;
		}
		
		int colCount = colTypes.length;
		Object []values = new Object[colCount];
		
		byte colSeparator = this.colSeparator;
		int []selIndex = this.selIndex;
		char escapeChar = this.escapeChar;
		boolean doQuoteMatch = this.doQuoteMatch; // �Ƿ�������ƥ��
		boolean doSingleQuoteMatch = this.doSingleQuoteMatch; // �Ƿ���������ƥ��
		boolean doBracketsMatch = this.doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
		boolean checkValueType = this.checkValueType;
		
		int colIndex = 0;
		int index = 0, start = 0;
		int BracketsLevel = 0; // ���ŵĲ�������pѡ��ʱ��Ϊ��������ƥ����ֵ�
		
		Next:
		while (index < count && colIndex < colCount) {
			char c = line.charAt(index);
			if (BracketsLevel == 0 && c == colSeparator) {
				// �н���
				if (selIndex == null || selIndex[colIndex] != -1) {
					String str = line.substring(start, index);
					if (checkValueType) {
						if (!parse(str, colIndex, values)) {
							return null;
						}
					} else {
						values[colIndex] = parse(str, colIndex);
					}
				}
				
				colIndex++;
				start = ++index;
			} else if (doQuoteMatch && c == '"') {
				// ������ƥ�䣬���������ڵ��зָ���
				for (++index; index < count; ++index) {
					if (line.charAt(index) == '"') {
						index++;
						if (escapeChar != '"' || index == count || line.charAt(index) != '"') {
							continue Next;
						}
					} else if (line.charAt(index) == escapeChar) {
						index++;
					}
				}
				
				// û�ҵ�ƥ������ŷ��ؿ�
				return null;
			} else if (doSingleQuoteMatch && c == '\'') {
				// �ҵ�����ƥ�䣬���������ڵ��зָ���
				for (++index; index < count; ++index) {
					if (line.charAt(index) == '\'') {
						index++;
						continue Next;
					} else if (line.charAt(index) == escapeChar) {
						index++;
					}
				}
				
				// û�ҵ�ƥ��ĵ����ŷ��ؿ�
				return null;
			} else if (doBracketsMatch) {
				if (c == '(' || c == '[' || c == '{' || c == '<' || c == '��' || c == '��' || c == '��') {
					BracketsLevel++;
				} else if (BracketsLevel > 0 && (c == ')' || c == ']' || c == '}' || c == '>' || c == '��' || c == '��' || c == '��')) {
					BracketsLevel--;
				}
				
				index++;
			} else {
				index++;
			}
		}
		
		if (BracketsLevel != 0) {
			// �в�ƥ�������
			return null;
		}
		
		if (colIndex < colCount) {
			if (checkColCount && colIndex + 1 < colCount) {
				return null;
			}
			
			if (selIndex == null || selIndex[colIndex] != -1) {
				String str = line.substring(start, index);
				if (checkValueType) {
					if (!parse(str, colIndex, values)) {
						return null;
					}
				} else {
					values[colIndex] = parse(str, colIndex);
				}
			}
		}

		return values;
	}
	
	private Object[] readLine(String line, byte []colTypes) throws IOException {
		int colCount = colTypes.length;
		Object []values = new Object[colCount];
		int count = line.length();
		if (count < 1) {
			return values;
		}
				
		byte colSeparator = this.colSeparator;
		int []selIndex = this.selIndex;
		char escapeChar = this.escapeChar;
		boolean doQuoteMatch = this.doQuoteMatch; // �Ƿ�������ƥ��
		boolean doSingleQuoteMatch = this.doSingleQuoteMatch; // �Ƿ���������ƥ��
		boolean doBracketsMatch = this.doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
		
		int colIndex = 0;
		int index = 0, start = 0;
		int BracketsLevel = 0; // ���ŵĲ�������pѡ��ʱ��Ϊ��������ƥ����ֵ�
		
		while (index < count && colIndex < colCount) {
			char c = line.charAt(index);
			if (BracketsLevel == 0 && c == colSeparator) {
				// �н���
				if (selIndex == null || selIndex[colIndex] != -1) {
					String str = line.substring(start, index);
					values[colIndex] = parse(str, colIndex);
				}
				
				colIndex++;
				start = ++index;
			} else if (doQuoteMatch && c == '"') {
				// ������ƥ�䣬���������ڵ��зָ���
				for (++index; index < count; ++index) {
					if (line.charAt(index) == '"') {
						index++;
						if (escapeChar != '"' || index == count || line.charAt(index) != '"') {
							break;
						}
					} else if (line.charAt(index) == escapeChar) {
						index++;
					}
				}
			} else if (doSingleQuoteMatch && c == '\'') {
				// �ҵ�����ƥ�䣬���������ڵ��зָ���
				for (++index; index < count; ++index) {
					if (line.charAt(index) == '\'') {
						index++;
						break;
					} else if (line.charAt(index) == escapeChar) {
						index++;
					}
				}
			} else if (doBracketsMatch) {
				if (c == '(' || c == '[' || c == '{' || c == '<' || c == '��' || c == '��' || c == '��') {
					BracketsLevel++;
				} else if (BracketsLevel > 0 && (c == ')' || c == ']' || c == '}' || c == '>' || c == '��' || c == '��' || c == '��')) {
					BracketsLevel--;
				}
				
				index++;
			} else {
				index++;
			}
		}
		
		if (colIndex < colCount && (selIndex == null || selIndex[colIndex] != -1)) {
			String str = line.substring(start, index);
			values[colIndex] = parse(str, colIndex);
		}

		return values;
	}
	
	private Object[] readLine(String line) throws IOException {
		int count = line.length();
		if (count < 1) {
			return new Object[0];
		}
		
		byte colSeparator = this.colSeparator;
		char escapeChar = this.escapeChar;
		boolean isTrim = this.isTrim;
		boolean doQuoteMatch = this.doQuoteMatch; // �Ƿ�������ƥ��
		boolean doSingleQuoteMatch = this.doSingleQuoteMatch; // �Ƿ���������ƥ��
		boolean doBracketsMatch = this.doBracketsMatch; // �Ƿ�������ƥ�䣨����Բ���š������š������ţ�
		
		ArrayList<Object> list = new ArrayList<Object>();
		int BracketsLevel = 0; // ���ŵĲ�������pѡ��ʱ��Ϊ��������ƥ����ֵ�
		int index = 0, start = 0;
		
		while (index < count) {
			char c = line.charAt(index);
			if (BracketsLevel == 0 && c == colSeparator) {
				// �н���
				if (index > start) {
					String str = line.substring(start, index);
					if (isTrim) {
						str = str.trim();
					}
					
					list.add(parse(str));
				} else {
					list.add(null);
				}
				
				start = ++index;
			} else if (doQuoteMatch && c == '"') {
				// ������ƥ�䣬���������ڵ��зָ���
				for (++index; index < count; ++index) {
					if (line.charAt(index) == '"') {
						index++;
						if (escapeChar != '"' || index == count || line.charAt(index) != '"') {
							break;
						}
					} else if (line.charAt(index) == escapeChar) {
						index++;
					}
				}
			} else if (doSingleQuoteMatch && c == '\'') {
				// �ҵ�����ƥ�䣬���������ڵ��зָ���
				for (++index; index < count; ++index) {
					if (line.charAt(index) == '\'') {
						index++;
						break;
					} else if (line.charAt(index) == escapeChar) {
						index++;
					}
				}
			} else if (doBracketsMatch) {
				if (c == '(' || c == '[' || c == '{' || c == '<' || c == '��' || c == '��' || c == '��') {
					BracketsLevel++;
				} else if (BracketsLevel > 0 && (c == ')' || c == ']' || c == '}' || c == '>' || c == '��' || c == '��' || c == '��')) {
					BracketsLevel--;
				}
				
				index++;
			} else {
				index++;
			}
		}
		
		if (count > start) {
			String str = line.substring(start, index);
			if (isTrim) {
				str = str.trim();
			}
			
			list.add(parse(str));
		} else {
			list.add(null);
		}
		
		return list.toArray();
	}
}
