package com.raqsoft.expression.fn.math;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.Sequence;
import com.raqsoft.expression.Function;
import com.raqsoft.expression.IParam;
import com.raqsoft.resources.EngineMessage;
import com.raqsoft.util.Variant;


/**
 * bits(xi,��) �Ѹ�����xi,...������λ˳��������֣�Ĭ�ϸ�λ��ǰ
 * @author yanjing
 *
 */
public class Bits extends Function {
	private final static char[] digits = { '0', '1', '2', '3', '4', 
			'5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	
	// �Ѵ�ת�����֣��ַ�������Ϊ1��������ʾ�������ܳ�������
	private static int toNum(String s, int radix) {
		if (s.length() != 1) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bits" + mm.getMessage("function.invalidParam"));
		}
		
		char c = s.charAt(0);
		int n;
		if (c < 'A') {
			n = c - '0';
		} else if (c < 'a') {
			n = c - 'A' + 10;
		} else {
			n = c - 'a' + 10;
		}
		
		if (n < 0 || n >= radix) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bits" + mm.getMessage("function.invalidParam"));
		}
		
		return n;
	}
	
	// ��ת�ַ���
	private static String reverse(String str) {
		char []chars = str.toCharArray();
		for (int i = 0, j = chars.length - 1; i < j; ++i, --j) {
			char c = chars[i];
			chars[i] = chars[j];
			chars[j] = c;
		}
		
		return new String(chars);
	}
	
	// ����ת�ɸ������ƶ�Ӧ�Ĵ���bigEnding������λ�ں�
	private static String toString(Number num, int radix, boolean bigEnding) {
		if (num instanceof BigDecimal) {
			BigDecimal decimal = (BigDecimal)num;
			if (decimal.scale() != 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("bits" + mm.getMessage("function.paramTypeError"));				
			}
			
			String str = decimal.unscaledValue().toString(radix);
			if (bigEnding) {
				return reverse(str);
			} else {
				return str;
			}
		} else if (num instanceof BigInteger) {
			String str = ((BigInteger)num).toString(radix);
			if (bigEnding) {
				return reverse(str);
			} else {
				return str;
			}
		} else {
			return toString(num.longValue(), radix, bigEnding);
		}
	}
	
	private static String toString(long v, int radix, boolean bigEnding) {
		if (bigEnding) {
			String str;
			switch (radix) {
			case 10:
				str = Long.toString(v);
				break;
			case 16:
				str = Long.toHexString(v);
				break;
			default:
				str = Long.toBinaryString(v);
			}
			
			return reverse(str);
		} else {
			switch (radix) {
			case 10:
				return Long.toString(v);
			case 16:
				return Long.toHexString(v);
			default:
				return Long.toBinaryString(v);
			}
		}
	}
	
	private static String toString(IParam param, Context ctx, int radix, boolean reverse) {
		int size = param.getSubSize();
		char []chars = new char[size];
		
		if (reverse) {
			for (int i = 0, j  = size - 1; i < size; ++i, --j) {
				IParam sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (obj instanceof Number) {
					int n = ((Number)obj).intValue();
					if (n < 0 || n >= radix) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("bits" + mm.getMessage("function.invalidParam"));
					}
					
					chars[j] = digits[n];
				} else if (obj instanceof String) {
					chars[j] = digits[toNum((String)obj, radix)];
				} else if (radix == 2 && obj instanceof Boolean) {
					if (((Boolean)obj).booleanValue()) {
						chars[j] = '1';
					} else {
						chars[j] = '0';
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.paramTypeError"));
				}
			}
		} else {
			for (int i = 0; i < size; ++i) {
				IParam sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (obj instanceof Number) {
					int n = ((Number)obj).intValue();
					if (n < 0 || n >= radix) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("bits" + mm.getMessage("function.invalidParam"));
					}
					
					chars[i] = digits[n];
				} else if (obj instanceof String) {
					chars[i] = digits[toNum((String)obj, radix)];
				} else if (radix == 2 && obj instanceof Boolean) {
					if (((Boolean)obj).booleanValue()) {
						chars[i] = '1';
					} else {
						chars[i] = '0';
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		
		return new String(chars);
	}
	
	private static String toString(Sequence seq, int radix, boolean reverse) {
		int size = seq.length();
		char []chars = new char[size];
		
		if (reverse) {
			for (int i = 1, j  = size - 1; i <= size; ++i, --j) {
				Object obj = seq.getMem(i);
				if (obj instanceof Number) {
					int n = ((Number)obj).intValue();
					if (n < 0 || n >= radix) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("bits" + mm.getMessage("function.invalidParam"));
					}
					
					chars[j] = digits[n];
				} else if (obj instanceof String) {
					chars[j] = digits[toNum((String)obj, radix)];
				} else if (radix == 2 && obj instanceof Boolean) {
					if (((Boolean)obj).booleanValue()) {
						chars[j] = '1';
					} else {
						chars[j] = '0';
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.paramTypeError"));
				}
			}
		} else {
			for (int i = 0; i < size; ++i) {
				Object obj = seq.getMem(i + 1);
				if (obj instanceof Number) {
					int n = ((Number)obj).intValue();
					if (n < 0 || n >= radix) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("bits" + mm.getMessage("function.invalidParam"));
					}
					
					chars[i] = digits[n];
				} else if (obj instanceof String) {
					chars[i] = digits[toNum((String)obj, radix)];
				} else if (radix == 2 && obj instanceof Boolean) {
					if (((Boolean)obj).booleanValue()) {
						chars[i] = '1';
					} else {
						chars[i] = '0';
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		
		return new String(chars);
	}
	
	private static long toLong(IParam param, Context ctx, int radix, boolean isBool, boolean bigEnding) {
		int size = param.getSubSize();
		long result = 0;
		
		if (bigEnding) {
			for (int i = size - 1; i >= 0; --i) {
				IParam sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (obj instanceof Number) {
					int n = ((Number)obj).intValue();
					if (n < 0 || n >= radix) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("bits" + mm.getMessage("function.invalidParam"));
					}
					
					result = result * radix + n;
				} else if (obj instanceof String) {
					result = result * radix + toNum((String)obj, radix);
				} else if (isBool && obj instanceof Boolean) {
					if (((Boolean)obj).booleanValue()) {
						result = result * 2 + 1;
					} else {
						result = result * 2;
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.paramTypeError"));
				}
			}
		} else {
			for (int i = 0; i < size; ++i) {
				IParam sub = param.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (obj instanceof Number) {
					int n = ((Number)obj).intValue();
					if (n < 0 || n >= radix) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("bits" + mm.getMessage("function.invalidParam"));
					}
					
					result = result * radix + n;
				} else if (obj instanceof String) {
					result = result * radix + toNum((String)obj, radix);
				} else if (isBool && obj instanceof Boolean) {
					if (((Boolean)obj).booleanValue()) {
						result = result * 2 + 1;
					} else {
						result = result * 2;
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		
		return result;
	}
	
	private static long toLong(Sequence seq, int radix, boolean isBool, boolean bigEnding) {
		int size = seq.length();
		long result = 0;
		
		if (bigEnding) {
			for (int i = size; i > 0; --i) {
				Object obj = seq.getMem(i);
				if (obj instanceof Number) {
					int n = ((Number)obj).intValue();
					if (n < 0 || n >= radix) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("bits" + mm.getMessage("function.invalidParam"));
					}
					
					result = result * radix + n;
				} else if (obj instanceof String) {
					result = result * radix + toNum((String)obj, radix);
				} else if (isBool && obj instanceof Boolean) {
					if (((Boolean)obj).booleanValue()) {
						result = result * 2 + 1;
					} else {
						result = result * 2;
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.paramTypeError"));
				}
			}
		} else {
			for (int i = 1; i <= size; ++i) {
				Object obj = seq.getMem(i);
				if (obj instanceof Number) {
					int n = ((Number)obj).intValue();
					if (n < 0 || n >= radix) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("bits" + mm.getMessage("function.invalidParam"));
					}
					
					result = result * radix + n;
				} else if (obj instanceof String) {
					result = result * radix + toNum((String)obj, radix);
				} else if (isBool && obj instanceof Boolean) {
					if (((Boolean)obj).booleanValue()) {
						result = result * 2 + 1;
					} else {
						result = result * 2;
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("bits" + mm.getMessage("function.paramTypeError"));
				}
			}
		}
		
		return result;
	}
	
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("bits" + mm.getMessage("function.missingParam"));
		}

		int radix = 2; // Ĭ��2����
		boolean isBool = false, returnString = false, returnDecimal = false, bigEnding = false;
		if (option != null) {
			if (option.indexOf('h') != -1) {
				radix = 16;
			} else if (option.indexOf('d') != -1) {
				radix = 10;
			} else if (option.indexOf('n') != -1) {
				isBool = true;
			}
			
			if (option.indexOf('s') != -1) {
				returnString = true;
			} else if (option.indexOf('l') != -1) {
				returnDecimal = true;
			}
			
			if (option.indexOf('r') != -1) bigEnding = true;
		}
		
		if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (obj instanceof String) {
				String str = (String)obj;
				if (bigEnding) {
					str = reverse(str);
				}
				
				if (returnDecimal) {
					BigInteger bi = new BigInteger(str, radix);
					return new BigDecimal(bi);
				} else {
					return Long.parseLong(str, radix);
				}
			} else if (obj instanceof Number) {
				if (returnString) {
					return toString((Number)obj, radix, bigEnding);
				} else if (returnDecimal) {
					if (obj instanceof BigDecimal) {
						return obj;
					} else if (obj instanceof BigInteger) {
						return new BigDecimal((BigInteger)obj);
					} else {
						return new BigDecimal(((Number)obj).longValue());
					}
				} else {
					return obj;
				}
			} else if (obj instanceof Sequence) {
				if (returnString) {
					return toString((Sequence)obj, radix, false);
				} else if (returnDecimal) {
					String str = toString((Sequence)obj, radix, bigEnding);
					BigInteger bi = new BigInteger(str, radix);
					return bi;
				} else {
					return toLong((Sequence)obj, radix, isBool, bigEnding);
				}
			} else if (isBool) {
				boolean b = Variant.isTrue(obj);
				if (returnString) {
					return b ? "1" : "0";
				} else if (returnDecimal) {
					return new BigDecimal(b ? 1 : 0);
				} else {
					return b ? 1L : 0L;
				}
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("bits" + mm.getMessage("function.paramTypeError"));
			}
		} else if (returnString) {
			return toString(param, ctx, radix, false);
		} else if (returnDecimal) {
			String str = toString(param, ctx, radix, bigEnding);
			BigInteger bi = new BigInteger(str, radix);
			return bi;
		} else {
			return toLong(param, ctx, radix, isBool, bigEnding);
		}
	}
}