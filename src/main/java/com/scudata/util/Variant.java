package com.scudata.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import com.scudata.common.DateFactory;
import com.scudata.common.DateFormatFactory;
import com.scudata.common.Escape;
import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.common.Sentence;
import com.scudata.common.Types;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Sequence;
import com.scudata.dm.SerialBytes;
import com.scudata.resources.EngineMessage;

/**
 * �����࣬�ṩ�����ַ���֮���໥���ͣ�����Ƚϣ���ѧ���㣬���������
 * @author RunQian
 *
 */
public class Variant {
	public static Double INFINITY = new Double(Double.POSITIVE_INFINITY);
	public static final int Divide_Scale = 16;
	public static final int Divide_Round = BigDecimal.ROUND_HALF_UP;

	// �ڴ��п��ܴ��ڵ���ֵ����������
	public static final int DT_INT = 1; // Integer
	public static final int DT_LONG = 2; // Long
	public static final int DT_DOUBLE = 3; // Double
	public static final int DT_DECIMAL = 4; // BigDecimal

	// ÿ�µ�����
	private static final int []DAYS = new int[] {31,28,31,30,31,30,31,31,30,31,30,31};
	private static final int []LEEPYEARDAYS = new int[] {31,29,31,30,31,30,31,31,30,31,30,31};
	
	static final long BASEDATE; // 1992��֮ǰ�е����ڲ��ܱ�86400000����
	static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, java.util.Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(java.util.Calendar.MILLISECOND, 0);
		BASEDATE = calendar.getTimeInMillis();
	}

	/**
	 * ���ض����Ƿ�Ϊ�棬����Ϊ���Ҳ���false��Ϊ��
	 * @param o ����
	 * @return true������Ϊ�棬false������Ϊ��
	 */
	public static boolean isTrue(Object o) {
		return o != null && (!(o instanceof Boolean) || ((Boolean)o).booleanValue());
	}

	/**
	 * ���ض����Ƿ�Ϊ�٣�����Ϊ�ջ���Ϊfalse��Ϊ��
	 * @param o ����
	 * @return true������Ϊ�٣�false������Ϊ��
	 */
	public static boolean isFalse(Object o) {
		return o == null || ((o instanceof Boolean) && !((Boolean)o).booleanValue());
	}

	/**
	 * ����ָ���������ָ������������
	 * @param date ����
	 * @param n ����
	 * @return Date
	 */
	public static Date add(Date date, int n) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, n);

		Date result = (Date)date.clone();
		result.setTime(calendar.getTimeInMillis());
		return result;
	}
	
	/**
	 * ����o1��o2�ĺ�
	 * @param o1 Object Number��String
	 * @param o2 Object Number��String
	 * @return Object
	 */
	public static Object add(Object o1, Object o2) {
		if (o1 == null) return o2;
		if (o2 == null) return o1;

		if (o1 instanceof Number) {
			if (o2 instanceof Number) {
				return addNum((Number)o1, (Number)o2);
			} else if (o2 instanceof String) {
				Number n2 = parseNumber((String)o2);
				if (n2 == null) return o1;
				return addNum((Number)o1, n2);
			} else if (o2 instanceof Date) {
				Date date1 = (Date)o2;
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date1);
				calendar.add(Calendar.DATE, ((Number)o1).intValue());
	
				Date date = (Date)date1.clone();
				date.setTime(calendar.getTimeInMillis());
				return date;
			}
		} else if (o1 instanceof Date) {
			if (o2 instanceof Number) {
				Date date1 = (Date)o1;
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date1);
				calendar.add(Calendar.DATE, ((Number)o2).intValue());
	
				Date date = (Date)date1.clone();
				date.setTime(calendar.getTimeInMillis());
				return date;
			}
		} else if (o1 instanceof String) {
			if (o2 instanceof String) {
				return (String)o1 + o2;
			} else if (o2 instanceof Number) {
				Number n1 = parseNumber((String)o1);
				if (n1 == null) return o2;
				return addNum(n1, (Number)o2);
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType(o1) + mm.getMessage("Variant2.with") +
							  getDataType(o2) + mm.getMessage("Variant2.illAdd"));
	}

	/**
	 * �����������ĺ�
	 * @param n1
	 * @param n2
	 * @return
	 */
	public static Number addNum(Number n1, Number n2) {
		int type = getMaxNumberType(n1, n2);
		switch (type) {
		case DT_INT: // Ϊ�˷�ֹ���ת��long����
			//return new Integer(n1.intValue() + n2.intValue());
		case DT_LONG:
			return new Long(n1.longValue() + n2.longValue());
		case DT_DOUBLE:
			return new Double(n1.doubleValue() + n2.doubleValue());
		case DT_DECIMAL:
			return toBigDecimal(n1).add(toBigDecimal(n2));
		default:
			throw new RQException();
		}
	}

	/**
	 * �Ѷ���ֵ��1
	 * @param o1 Object
	 * @return Object
	 */
	public static Object add1(Object o1) {
		if (o1 == null) return new Integer(1);
		int type = getNumberType(o1);
		switch (type) {
		case DT_INT: // Ϊ�˷�ֹ���ת��long����
			//return new Integer(((Number)o1).intValue() + 1);
		case DT_LONG:
			return new Long(((Number)o1).longValue() + 1);
		case DT_DOUBLE:
			return new Double(((Number)o1).doubleValue() + 1);
		case DT_DECIMAL:
			return toBigDecimal((Number)o1).add(new BigDecimal(1));
		default:
			throw new RQException();
		}
	}

	/**
	 * ����o1 - o2
	 * @param o1 Object Number
	 * @param o2 Object Number
	 * @return Object
	 */
	public static Object subtract(Object o1, Object o2) {
		if (o2 == null) return o1;
		if (o1 == null) return negate(o2);

		if (o1 instanceof Number) {
			if (o2 instanceof Number) {
				int type = getMaxNumberType(o1, o2);
				switch (type) {
				case DT_INT:
					return new Integer(((Number)o1).intValue() - ((Number)o2).intValue());
				case DT_LONG:
					return new Long(((Number)o1).longValue() - ((Number)o2).longValue());
				case DT_DOUBLE:
					return new Double(((Number)o1).doubleValue() -
									  ((Number)o2).doubleValue());
				case DT_DECIMAL:
					return toBigDecimal((Number)o1).subtract(toBigDecimal((Number)o2));
				default:
					throw new RQException();
				}
			}
		} else if (o1 instanceof Date) {
			if (o2 instanceof Date) {
				return new Long(interval((Date)o2, (Date)o1, null));
			} else if (o2 instanceof Number) {
				Date date1 = (Date)o1;
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date1);
				calendar.add(Calendar.DATE, -((Number)o2).intValue());

				Date date = (Date)date1.clone();
				date.setTime(calendar.getTimeInMillis());
				return date;
				//return new java.sql.Timestamp(calendar.getTimeInMillis());
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType(o1) + mm.getMessage("Variant2.with") +
							  getDataType(o2) + mm.getMessage("Variant2.illSubtract"));
	}

	/**
	 * ���ض����ƽ��
	 * @param obj Object
	 * @return Object
	 */
	public static Object square(Object obj) {
		if (obj == null) return null;
		int type = getNumberType(obj);

		switch (type) {
		case DT_INT:
			int i = ((Number)obj).intValue();
			return new Integer(i * i);
		case DT_LONG:
			long l = ((Number)obj).longValue();
			return new Long(l * l);
		case DT_DOUBLE:
			double d = ((Number)obj).doubleValue();
			return new Double(d * d);
		case DT_DECIMAL:
			BigDecimal bd = toBigDecimal((Number)obj);
			return bd.multiply(bd);
		default:
			throw new RQException();
		}
	}

	/**
	 * ����o1 * o2
	 * @param o1 Object
	 * @param o2 Object
	 * @return Object
	 */
	public static Object multiply(Object o1, Object o2) {
		if (o1 instanceof Number) {
			if (o2 instanceof Number) {
				int type = getMaxNumberType(o1, o2);
				switch (type) {
				case DT_INT: // Ϊ�˷�ֹ���ת��long����
				case DT_LONG:
					return new Long(((Number)o1).longValue() * ((Number)o2).longValue());
				case DT_DOUBLE:
					return new Double(((Number)o1).doubleValue() *
									  ((Number)o2).doubleValue());
				case DT_DECIMAL:
					return toBigDecimal((Number)o1).multiply(toBigDecimal((Number)o2));
				default:
					throw new RQException();
				}
			} else if (o2 instanceof Sequence) {
				return ((Sequence)o2).multiply(((Number)o1).intValue());
			} else if (o2 == null) {
				return null;
			}
		} else if (o1 instanceof Sequence) {
			if (o2 instanceof Number) {
				return ((Sequence)o1).multiply(((Number)o2).intValue());
			} else if (o2 == null) {
				return null;
			}
		} else if (o1 == null) {
			return null;
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType(o1) + mm.getMessage("Variant2.with") +
							  getDataType(o2) + mm.getMessage("Variant2.illMultiply"));
	}

	/**
	 * ������������ȡ��
	 * @param o1 ��ֵ
	 * @param o2 ��ֵ
	 * @return Object
	 */
	public static Object mod(Object o1, Object o2) {
		if (o1 == null || o2 == null) {
			return null;
		} else if (o1 instanceof Number && o2 instanceof Number) {
			int type = getMaxNumberType(o1, o2);
			switch (type) {
			case DT_INT:
				return new Integer(((Number)o1).intValue() % ((Number)o2).intValue());
			case DT_LONG:
				return new Long(((Number)o1).longValue() % ((Number)o2).longValue());
			case DT_DOUBLE:
				return new Double(((Number)o1).doubleValue() % ((Number)o2).doubleValue());
			default://case DT_DECIMAL:
				return new BigDecimal(toBigInteger((Number)o1).mod(toBigInteger((Number)o2)));
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType(o1) + mm.getMessage("Variant2.with") +
							  getDataType(o2) + mm.getMessage("Variant2.illMod"));
	}
	
	/**
	 * ������������ȡ��
	 * @param n1 ��ֵ
	 * @param n2 ��ֵ
	 * @return Number
	 */
	public static Number mod(Number n1, Number n2) {
		int type = getMaxNumberType(n1, n2);
		switch (type) {
		case DT_INT:
			return ObjectCache.getInteger(n1.intValue() % n2.intValue());
		case DT_LONG:
			return new Long(n1.longValue() % n2.longValue());
		case DT_DOUBLE:
			return new Double(n1.doubleValue() % n2.doubleValue());
		default://case DT_DECIMAL:
			return new BigDecimal(toBigInteger(n1).mod(toBigInteger(n2)));
		}
	}
	
	/**
	 * ����remainder����������������ȡ������
	 * @param o1 ������ֵ
	 * @param o2 ������ֵ
	 * @return Number
	 */
	public static Number remainder(Object o1, Object o2) {
		if (o1 == null || o2 == null) {
			return null;
		} else if (o1 instanceof Number && o2 instanceof Number) {
			int type = getMaxNumberType(o1, o2);
			if (type == DT_INT) {
				int left = ((Number)o1).intValue();
				int right = ((Number)o2).intValue();
				if (right > 0) {
					if (left > 0) {
						return left % right;
					} else {
						int x = left % right;
						return x == 0 ? 0 : x + right;
					}
				} else {
					if (left >= 0) {
						int x = left %(right * 2); // �õ�����
						return x < -right ? x : x + right * 2;
					} else {
						int x = left %(right * 2); // �õ�����
						return x < right ? x - right * 2 : x;
					}
				}
			} else if (type == DT_LONG) {
				long left = ((Number)o1).longValue();
				long right = ((Number)o2).longValue();
				if (right > 0) {
					if (left > 0) {
						return left % right;
					} else {
						long x = left % right;
						return x == 0 ? 0 : x + right;
					}
				} else {
					if (left >= 0) {
						long x = left %(right * 2); // �õ�����
						return x < -right ? x : x + right * 2;
					} else {
						long x = left %(right * 2); // �õ�����
						return x < right ? x - right * 2 : x;
					}
				}
			} else {
				double left = ((Number)o1).doubleValue();
				double right = ((Number)o2).doubleValue();
				if (right > 0) {
					if (left > 0) {
						return left % right;
					} else {
						double x = left % right;
						return isRoughlyEquals(x, 0) ? 0 : x + right;
					}
				} else {
					if (left >= 0) {
						double x = left %(right * 2); // �õ�����
						return x > -right || isRoughlyEquals(x, -right) ? x + right * 2 : x;
					} else {
						double x = left %(right * 2); // �õ�����
						return x < right && !isRoughlyEquals(x, right) ? x - right * 2 : x;
					}
				}
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType(o1) + mm.getMessage("Variant2.with") +
							  getDataType(o2) + mm.getMessage("Variant2.illMod"));
	}

	// �ж������������Ƿ������
	private static boolean isRoughlyEquals(double d1, double d2) {
		d1 -= d2;
		return d1 > -0.0000001 && d1 < 0.0000001;
	}

	/**
	 * ����o1 / o2
	 * @param o1 Object Number
	 * @param o2 Object Number
	 * @return Object
	 */
	public static Object divide(Object o1, Object o2) {
		if (o1 instanceof Number && o2 instanceof Number) {
			// ��������ͬ�����ͬ
			//if (((Number)o2).doubleValue() == 0) {
			//	return INFINITY;
			//}
			
			int type = getMaxNumberType(o1, o2);
			try {
				if (type == DT_DECIMAL) {
					return toBigDecimal((Number)o1).divide(
						toBigDecimal((Number)o2), Divide_Scale, Divide_Round);
				} else {
					return new Double(((Number) o1).doubleValue() /
									  ((Number) o2).doubleValue());
				}
			} catch (java.lang.ArithmeticException e){
				throw new RQException(e.getMessage());
			}
		}

		if (o1 instanceof String) {
			if (o2 == null) {
				return o1;
			} else {
				return (String)o1 + o2;
			}
		} else if (o2 instanceof String) {
			if (o1 == null) {
				return o2;
			} else {
				return o1 + (String)o2;
			}
		} else if (o1 == null || o2 == null) {
			return null;
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType(o1) + mm.getMessage("Variant2.with") +
							  getDataType(o2) + mm.getMessage("Variant2.illDivide"));
	}

	/**
	 * ��ƽ��ֵ
	 * @param sum ��
	 * @param count ����
	 * @return ƽ��ֵ
	 */
	public static Object avg(Object sum, int count) {
		if (sum instanceof BigDecimal) {
			return ((BigDecimal)sum).divide(new BigDecimal(count), Divide_Scale, Divide_Round);
		} else if (sum instanceof BigInteger) {
			BigDecimal decimal = new BigDecimal((BigInteger)sum);
			return decimal.divide(new BigDecimal(count), Divide_Scale, Divide_Round);
		} else if (sum instanceof Number) {
			return new Double(((Number)sum).doubleValue() / count);
		} else if (sum == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(getDataType(sum) + mm.getMessage("engine.illEverage"));
		}
	}

	/**
	 * ����������������
	 * @param o1 ��ֵ
	 * @param o2 ��ֵ
	 * @return Number
	 */
	public static Number intDivide(Object o1, Object o2) {
		if (o1 == null || o2 == null) {
			return null;
		} else if (o1 instanceof Number && o2 instanceof Number) {
			int type = getMaxNumberType(o1, o2);
			switch (type) {
			case DT_INT:
				return new Integer(((Number) o1).intValue() / ((Number) o2).intValue());
			case DT_LONG:
			case DT_DOUBLE:
				return new Long(((Number) o1).longValue() / ((Number) o2).longValue());
			default:// DT_DECIMAL:
				return new BigDecimal(toBigInteger((Number)o1).divide(toBigInteger((Number)o2)));
			}
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(getDataType(o1) + mm.getMessage("Variant2.with") +
							  getDataType(o2) + mm.getMessage("Variant2.illDivide"));
	}
	
	/**
	 * ����������������ֵ
	 * @param n1 ��ֵ
	 * @param n2 ��ֵ
	 * @return Number
	 */
	public static Number intDivide(Number n1, Number n2) {
		int type = getMaxNumberType(n1, n2);
		switch (type) {
		case DT_INT:
			return ObjectCache.getInteger(n1.intValue() / n2.intValue());
		case DT_LONG:
		case DT_DOUBLE:
			return new Long(n1.longValue() / n2.longValue());
		default:// DT_DECIMAL:
			return new BigDecimal(toBigInteger(n1).divide(toBigInteger(n2)));
		}
	}

	/**
	 * ��o�ľ���ֵ
	 * @param o Object
	 * @return Object
	 */
	public static Object abs(Object o) {
		if (o == null) {
			return null;
		}

		if (!(o instanceof Number)) {
			MessageManager mm = EngineMessage.get();
			throw new RuntimeException(getDataType(o) + mm.getMessage("Variant2.illAbs"));
		}

		int type = getNumberType(o);
		switch (type) {
		case DT_INT:
			return new Integer(Math.abs(((Number)o).intValue()));
		case DT_LONG:
			return new Long(Math.abs(((Number)o).longValue()));
		case DT_DOUBLE:
			return new Double(Math.abs(((Number)o).doubleValue()));
		case DT_DECIMAL:
			return toBigDecimal((Number)o).abs();
		default:
			throw new RQException();
		}
	}
	
	public static Number abs(Number o) {
		int type = getNumberType(o);
		switch (type) {
		case DT_INT:
			return new Integer(Math.abs(((Number)o).intValue()));
		case DT_LONG:
			return new Long(Math.abs(((Number)o).longValue()));
		case DT_DOUBLE:
			return new Double(Math.abs(((Number)o).doubleValue()));
		case DT_DECIMAL:
			return toBigDecimal((Number)o).abs();
		default:
			throw new RQException();
		}
	}

	/**
	 * �Ƚ���������Ĵ�С�����ܱȽ�ʱ�׳��쳣��null��С
	 * @param o1 �����
	 * @param o2 �Ҷ���
	 * @return 1��������0��ͬ����-1���Ҷ����
	 */
	public static int compare(Object o1, Object o2) {
		return compare(o1, o2, true);
	}

	/**
	 * �Ƚ���������Ԫ�صĴ�С������Ԫ��������ͬ��null��С
	 * @param o1 ������
	 * @param o2 ������
	 * @return 1��������0��ͬ����-1���Ҷ����
	 */
	public static int compareArrays(Object []o1, Object []o2) {
		for (int i = 0, len = o1.length; i < len; ++i) {
			int cmp = compare(o1[i], o2[i], true);
			if (cmp != 0) return cmp;
		}

		return 0;
	}
	
	/**
	 * �Ƚ���������Ԫ�صĴ�С������Ԫ��������ͬ��null�������
	 * @param o1 ������
	 * @param o2 ������
	 * @return 1���������0��ͬ����-1���������
	 */
	public static int compareArrays_0(Object []o1, Object []o2) {
		for (int i = 0, len = o1.length; i < len; ++i) {
			int cmp = compare_0(o1[i], o2[i]);
			if (cmp != 0) {
				return cmp;
			}
		}
		
		return 0;
	}
	
	/**
	 * �Ƚ���������Ԫ�صĴ�С��null�������
	 * @param o1 ������
	 * @param o2 ������
	 * @param len ����
	 * @return 1���������0��ͬ����-1���������
	 */
	public static int compareArrays_0(Object []o1, Object []o2, int len) {
		for (int i = 0; i < len; ++i) {
			int cmp = compare_0(o1[i], o2[i]);
			if (cmp != 0) {
				return cmp;
			}
		}
		
		return 0;
	}
	
	/**
	 * �Ƚ���������Ԫ�صĴ�С��null�������
	 * @param o1 ������
	 * @param o2 ������
	 * @param len ����
	 * @param  locCmp �ַ����������ԱȽ���
	 * @return 1���������0��ͬ����-1���������
	 */
	public static int compareArrays_0(Object []o1, Object []o2, int len, Comparator<Object> locCmp) {
		for (int i = 0; i < len; ++i) {
			int cmp = compare_0(o1[i], o2[i], locCmp);
			if (cmp != 0) {
				return cmp;
			}
		}
		
		return 0;
	}
	
	/**
	 * �Ƚ���������Ԫ�صĴ�С��null��С
	 * @param o1 ������
	 * @param o2 ������
	 * @param len ����
	 * @param  locCmp �ַ����������ԱȽ���
	 * @return 1���������0��ͬ����-1���������
	 */
	public static int compareArrays(Object []o1, Object []o2, int len, Comparator<Object> locCmp) {
		for (int i = 0; i < len; ++i) {
			int cmp = compare(o1[i], o2[i], locCmp, true);
			if (cmp != 0) {
				return cmp;
			}
		}

		return 0;
	}

	/**
	 * �Ƚ���������Ԫ�صĴ�С��null��С
	 * @param o1 ������
	 * @param o2 ������
	 * @param len ����
	 * @return 1���������0��ͬ����-1���������
	 */
	public static int compareArrays(Object []o1, Object []o2, int len) {
		for (int i = 0; i < len; ++i) {
			int cmp = compare(o1[i], o2[i], true);
			if (cmp != 0) return cmp;
		}

		return 0;
	}

	/**
	 * �Ƚ�������Ĵ�С��null��С
	 * @param o1 �����
	 * @param o2 �Ҷ���
	 * @param throwExcept true�����ܱȽ�ʱ�׳��쳣��false�����ܱȽ�ʱ����-1�����ڲ���
	 * @return int 1��������0��ͬ����-1���Ҷ����
	 */
	public static int compare(Object o1, Object o2, boolean throwExcept) {
		if (o1 == o2)return 0;
		if (o1 == null)return -1;
		if (o2 == null)return 1;

		if (o1 instanceof Number && o2 instanceof Number) {
			int type = getMaxNumberType(o1, o2);
			switch (type) {
			case DT_INT:
				int num1 = ( (Number) o1).intValue();
				int num2 = ( (Number) o2).intValue();
				return (num1 < num2 ? -1 : (num1 == num2 ? 0 : 1));
			case DT_LONG:
				long long1 = ((Number)o1).longValue();
				long long2 = ( (Number)o2).longValue();
				return (long1 < long2 ? -1 : (long1 == long2 ? 0 : 1));
			case DT_DOUBLE:
				return Double.compare(((Number)o1).doubleValue(),
									  ((Number)o2).doubleValue());
			case DT_DECIMAL:
				return toBigDecimal((Number)o1).compareTo(toBigDecimal((Number)o2));
			default:
				throw new RQException();
			}
		}

		if (o1 instanceof String && o2 instanceof String) {
			int cmp =  ((String)o1).compareTo((String)o2);
			return cmp < 0 ? -1 : (cmp > 0 ? 1 : 0);
		}

		if (o1 instanceof Date && o2 instanceof Date) {
			long thisTime = ((Date)o1).getTime();
			long anotherTime = ((Date)o2).getTime();
			return (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
		}

		if (o1 instanceof Boolean && o2 instanceof Boolean) {
			return compare(((Boolean)o1).booleanValue(), ((Boolean)o2).booleanValue());
		}

		if (o1 instanceof Sequence && o2 instanceof Sequence) {
			return ((Sequence)o1).compareTo((Sequence)o2);
		}

		// Ϊ�˱�֤group��id��join������������������Сû����
		if (o1 instanceof BaseRecord && o2 instanceof BaseRecord) {
			return ((BaseRecord)o1).compareTo((BaseRecord)o2);
		}

		if (o1 instanceof byte[] && o2 instanceof byte[]) {
			return compareArrays((byte[])o1, (byte[])o2);
		}
		
		if (o1 instanceof SerialBytes && o2 instanceof SerialBytes) {
			return ((SerialBytes)o1).compareTo((SerialBytes)o2);
		}
		
		// if (o1 instanceof Comparable) {
		//	return ((Comparable)o1).compareTo(o2);
		
		if (throwExcept) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", o1, o2,
					getDataType(o1), getDataType(o2)));
		} else {
			return getType(o1) < getType(o2) ? -1 : 1;
		}
	}

	/**
	 * �Ƚ���������Ĵ�С�����ܱȽ�ʱ�׳��쳣��null�������
	 * @param o1 �����
	 * @param o2 �Ҷ���
	 * @return 1��������0��ͬ����-1���Ҷ����
	 */
	public static int compare_0(Object o1, Object o2) {
		if (o1 == o2) return 0;
		if (o1 == null)return 1;
		if (o2 == null)return -1;

		if (o1 instanceof Number && o2 instanceof Number) {
			int type = getMaxNumberType(o1, o2);
			switch (type) {
			case DT_INT:
				int num1 = ( (Number) o1).intValue();
				int num2 = ( (Number) o2).intValue();
				return (num1 < num2 ? -1 : (num1 == num2 ? 0 : 1));
			case DT_LONG:
				long long1 = ((Number)o1).longValue();
				long long2 = ( (Number)o2).longValue();
				return (long1 < long2 ? -1 : (long1 == long2 ? 0 : 1));
			case DT_DOUBLE:
				return Double.compare(((Number)o1).doubleValue(),
									  ((Number)o2).doubleValue());
			case DT_DECIMAL:
				return toBigDecimal((Number)o1).compareTo(toBigDecimal((Number)o2));
			default:
				throw new RQException();
			}
		}

		if (o1 instanceof String && o2 instanceof String) {
			int cmp =  ((String)o1).compareTo((String)o2);
			return cmp < 0 ? -1 : (cmp > 0 ? 1 : 0);
		}

		if (o1 instanceof Date && o2 instanceof Date) {
			long thisTime = ((Date)o1).getTime();
			long anotherTime = ((Date)o2).getTime();
			return (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
		}

		if (o1 instanceof Boolean && o2 instanceof Boolean) {
			return compare(((Boolean)o1).booleanValue(), ((Boolean)o2).booleanValue());
		}

		if (o1 instanceof Sequence && o2 instanceof Sequence) {
			return ((Sequence)o1).compareTo((Sequence)o2);
		}
		
		// Ϊ�˱�֤group��id��join������������������Сû����
		if (o1 instanceof BaseRecord && o2 instanceof BaseRecord) {
			int h1 = o1.hashCode();
			int h2 = o2.hashCode();
			if (h1 < h2) {
				return -1;
			} else if (h1 > h2) {
				return 1;
			} else {
				return compare_0(((BaseRecord)o1).value(), ((BaseRecord)o2).value());
			}
		}

		if (o1 instanceof byte[] && o2 instanceof byte[]) {
			return compareArrays((byte[])o1, (byte[])o2);
		}
		
		if (o1 instanceof SerialBytes && o2 instanceof SerialBytes) {
			return ((SerialBytes)o1).compareTo((SerialBytes)o2);
		}
		
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("Variant2.illCompare", o1, o2,
				getDataType(o1), getDataType(o2)));
	}

	private static int getType(Object o) {
		if (o instanceof Number) {
			return 1;
		} else if (o instanceof String) {
			return 2;
		} else if (o instanceof Date) {
			return 3;
		} else if (o instanceof Boolean) {
			return 4;
		} else if (o == null) {
			return 0;
		} else {
			return 5;
		}
	}
	
	/**
	 * �Ƚ�������Ĵ�С��null��С
	 * @param o1 �����
	 * @param o2 �Ҷ���
	 * @param  locCmp �ַ����������ԱȽ���
	 * @param throwExcept true�����ܱȽ�ʱ�׳��쳣��false�����ܱȽ�ʱ����-1�����ڲ���
	 * @return int 1��������0��ͬ����-1���Ҷ����
	 */
	public static int compare(Object o1, Object o2, Comparator<Object> locCmp, boolean throwExcept) {
		if (o1 == o2)return 0;
		if (o1 == null)return -1;
		if (o2 == null)return 1;

		if (o1 instanceof String && o2 instanceof String) {
			return locCmp.compare(o1, o2);
		}

		if (o1 instanceof Number && o2 instanceof Number) {
			int type = getMaxNumberType(o1, o2);
			switch (type) {
			case DT_INT:
				int num1 = ( (Number) o1).intValue();
				int num2 = ( (Number) o2).intValue();
				return (num1 < num2 ? -1 : (num1 == num2 ? 0 : 1));
			case DT_LONG:
				long long1 = ((Number)o1).longValue();
				long long2 = ( (Number)o2).longValue();
				return (long1 < long2 ? -1 : (long1 == long2 ? 0 : 1));
			case DT_DOUBLE:
				return Double.compare(((Number)o1).doubleValue(),
									  ((Number)o2).doubleValue());
			case DT_DECIMAL:
				return toBigDecimal((Number)o1).compareTo(toBigDecimal((Number)o2));
			default:
				throw new RQException();
			}
		}

		if (o1 instanceof Date && o2 instanceof Date) {
			long thisTime = ((Date)o1).getTime();
			long anotherTime = ((Date)o2).getTime();
			return (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
		}

		if (o1 instanceof Boolean && o2 instanceof Boolean) {
			return compare(((Boolean)o1).booleanValue(), ((Boolean)o2).booleanValue());
		}

		if (o1 instanceof Sequence && o2 instanceof Sequence) {
			return ((Sequence)o1).compareTo((Sequence)o2, locCmp);
		}
		
		// Ϊ�˱�֤group��id��join������������������Сû����
		if (o1 instanceof BaseRecord && o2 instanceof BaseRecord) {
			return ((BaseRecord)o1).compareTo((BaseRecord)o2);
		}
		
		if (o1 instanceof byte[] && o2 instanceof byte[]) {
			return compareArrays((byte[])o1, (byte[])o2);
		}
		
		if (o1 instanceof SerialBytes && o2 instanceof SerialBytes) {
			return ((SerialBytes)o1).compareTo((SerialBytes)o2);
		}

		if (throwExcept) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Variant2.illCompare", o1, o2,
					getDataType(o1), getDataType(o2)));
		} else {
			return getType(o1) < getType(o2) ? -1 : 1;
		}
	}

	/**
	 * �Ƚ�������Ĵ�С�����ܱȽ�ʱ�׳��쳣��null�������
	 * @param o1 �����
	 * @param o2 �Ҷ���
	 * @param  locCmp �ַ����������ԱȽ���
	 * @return int 1��������0��ͬ����-1���Ҷ����
	 */
	public static int compare_0(Object o1, Object o2, Comparator<Object> locCmp) {
		if (o1 == o2) return 0;
		if (o1 == null)return 1;
		if (o2 == null)return -1;

		if (o1 instanceof String && o2 instanceof String) {
			return locCmp.compare(o1, o2);
		}

		if (o1 instanceof Number && o2 instanceof Number) {
			int type = getMaxNumberType(o1, o2);
			switch (type) {
			case DT_INT:
				int num1 = ( (Number) o1).intValue();
				int num2 = ( (Number) o2).intValue();
				return (num1 < num2 ? -1 : (num1 == num2 ? 0 : 1));
			case DT_LONG:
				long long1 = ((Number)o1).longValue();
				long long2 = ( (Number)o2).longValue();
				return (long1 < long2 ? -1 : (long1 == long2 ? 0 : 1));
			case DT_DOUBLE:
				return Double.compare(((Number)o1).doubleValue(),
									  ((Number)o2).doubleValue());
			case DT_DECIMAL:
				return toBigDecimal((Number)o1).compareTo(toBigDecimal((Number)o2));
			default:
				throw new RQException();
			}
		}

		if (o1 instanceof Date && o2 instanceof Date) {
			long thisTime = ((Date)o1).getTime();
			long anotherTime = ((Date)o2).getTime();
			return (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
		}

		if (o1 instanceof Boolean && o2 instanceof Boolean) {
			return compare(((Boolean)o1).booleanValue(), ((Boolean)o2).booleanValue());
		}

		if (o1 instanceof Sequence && o2 instanceof Sequence) {
			return ((Sequence)o1).compareTo((Sequence)o2, locCmp);
		}
		
		// Ϊ�˱�֤group��id��join������������������Сû����
		if (o1 instanceof BaseRecord && o2 instanceof BaseRecord) {
			int h1 = o1.hashCode();
			int h2 = o2.hashCode();
			if (h1 < h2) {
				return -1;
			} else if (h1 > h2) {
				return 1;
			} else {
				return compare_0(((BaseRecord)o1).value(), ((BaseRecord)o2).value(), locCmp);
			}
		}
		
		if (o1 instanceof byte[] && o2 instanceof byte[]) {
			return compareArrays((byte[])o1, (byte[])o2);
		}
		
		if (o1 instanceof SerialBytes && o2 instanceof SerialBytes) {
			return ((SerialBytes)o1).compareTo((SerialBytes)o2);
		}

		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("Variant2.illCompare", o1, o2,
				getDataType(o1), getDataType(o2)));
	}

	/**
	 * �����������Ƿ����
	 * @param o1 Object
	 * @param o2 Object
	 * @return boolean
	 */
	public static boolean isEquals(Object o1, Object o2) {
		if (o1 == o2) return true;

		if (o1 instanceof Number && o2 instanceof Number) {
			int type = getMaxNumberType(o1, o2);
			switch (type) {
			case DT_INT:
				return ((Number)o1).intValue() == ((Number)o2).intValue();
			case DT_LONG:
				return ((Number)o1).longValue() == ((Number)o2).longValue();
			case DT_DOUBLE:
				return Double.compare(((Number)o1).doubleValue(),
									  ((Number)o2).doubleValue()) == 0;
			case DT_DECIMAL:
				// ����ʹ��equals����Ϊscale���ܲ�ͬ
				return toBigDecimal((Number)o1).compareTo(toBigDecimal((Number)o2)) == 0;
			default:
				throw new RQException();
			}
		}

		if (o1 instanceof String && o2 instanceof String) {
			return ((String)o1).equals(o2);
		}

		if (o1 instanceof Date && o2 instanceof Date) {
			return ((Date)o1).getTime() == ((Date)o2).getTime();
		}

		if (o1 instanceof Boolean && o2 instanceof Boolean) {
			return ((Boolean)o1).booleanValue() == ((Boolean)o2).booleanValue();
		}

		if (o1 instanceof Sequence && o2 instanceof Sequence) {
			return ((Sequence)o1).isEquals((Sequence)o2);
		}

		// ���к����ıȽ���cmp������֧�֣����л�ʱ[0,0]��0������Ϊ���
		/*if (o1 instanceof Sequence) {
			if (o2 instanceof Sequence) {
				return ((Sequence)o1).isEquals((Sequence)o2);
			}
			if (o2 instanceof Number && ((Number)o2).intValue() == 0) {
				return ((Sequence)o1).cmp0() == 0;
			}
		} else if (o1 instanceof Number && ((Number)o1).intValue() == 0) {
			if (o2 instanceof Sequence) {
				return ((Sequence)o2).cmp0() == 0;
			}
		}*/
		
		if (o1 instanceof byte[] && o2 instanceof byte[]) {
			return isEquals((byte[])o1, (byte[])o2);
		}
		
		if (o1 instanceof SerialBytes && o2 instanceof SerialBytes) {
			return ((SerialBytes)o1).equals((SerialBytes)o2);
		}
		
		return false;
	}

	/**
	 * ȥ��С��λ������С����С��1������
	 * @param o ��С������
	 * @return û��С������
	 */
	public static Object round(Object o) {
		if (o instanceof BigDecimal) {
			return ((BigDecimal)o).setScale(0, BigDecimal.ROUND_HALF_UP);
		} else if (o instanceof Double || o instanceof Float) {
			double d = ((Number)o).doubleValue();
			if (d > Long.MIN_VALUE && d < Long.MAX_VALUE) {
				return new Double(Math.round(d));
			} else {
				return o;
			}
		} else if (o instanceof Number) {
			return o;
		} else if (o == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("round" + mm.getMessage("function.paramTypeError"));
		}
	}

	/**
	 * ����ָ��λ����С��
	 * @param o ��
	 * @param scale С��λ��
	 * @return ��
	 */
	public static Object round(Object o, int scale) {
		if (o instanceof BigDecimal) {
			if (scale < 0) {
				return ((BigDecimal)o).setScale(scale, BigDecimal.ROUND_HALF_UP).setScale(0);
			} else {
				return ((BigDecimal)o).setScale(scale, BigDecimal.ROUND_HALF_UP);
			}
		} else if (o instanceof Double || o instanceof Float) {
			double s = Math.pow(10, scale);
			double d = ((Number)o).doubleValue() * s;
			if (d > Long.MIN_VALUE && d < Long.MAX_VALUE) {
				return new Double(Math.round(d)/s);
			} else {
				return new Double(d/s);
			}
		} else if (o instanceof Number) {
			double s = Math.pow(10, scale);
			double d = ((Number)o).longValue() * s;
			if (o instanceof Integer) {
				return new Integer((int)(Math.round(d) / s));
			} else {
				return new Long((long)(Math.round(d) / s));
			}
		} else if (o == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("round" + mm.getMessage("function.paramTypeError"));
		}
	}

	/**
	 * ���ظ�ֵ
	 * @param o Object Number
	 * @return Object
	 */
	public static Object negate(Object o) {
		if (o == null) return null;

		if (!(o instanceof Number)) {
			MessageManager mm = EngineMessage.get();
			throw new RuntimeException(getDataType(o) + mm.getMessage("Variant2.illNegate"));
		}

		int type = getNumberType(o);
		switch (type) {
		case DT_INT:
			return new Integer(-((Number)o).intValue());
		case DT_LONG:
			return new Long(-((Number)o).longValue());
		case DT_DOUBLE:
			return new Double(-((Number)o).doubleValue());
		case DT_DECIMAL:
			return toBigDecimal((Number)o).negate();
		default:
			throw new RQException();
		}
	}
	
	/**
	 * ���ظ�ֵ
	 * @param o Number
	 * @return Number
	 */
	public static Number negate(Number o) {
		int type = getNumberType(o);
		switch (type) {
		case DT_INT:
			return new Integer(-((Number)o).intValue());
		case DT_LONG:
			return new Long(-((Number)o).longValue());
		case DT_DOUBLE:
			return new Double(-((Number)o).doubleValue());
		case DT_DECIMAL:
			return toBigDecimal((Number)o).negate();
		default:
			throw new RQException();
		}
	}

	/**
	 * ���ַ�����
	 * @param str
	 * @return
	 */
	public static String negate(String str) {
		char []chars = str.toCharArray();
		int last = chars.length - 1;
		
		if (last >= 0) {
			for (int i = 0; i < last; ++i) {
				chars[i] = (char)(0xFFFF - chars[i]);
			}
			
			chars[last] = (char)(0x10000 - chars[last]);
		}
		
		return new String(chars);
	}

	/**
	 * ���ַ�����
	 * @param str
	 * @return
	 */
	public static Date negate(Date date) {
		Date result = (Date)date.clone();
		result.setTime(-date.getTime());
		return result;
	}
	
	public static int compare(boolean b1, boolean b2) {
		if (b1) {
			return b2 ? 0 : 1;
		} else {
			return b2 ? -1 : 0;
		}
	}

	private static int getNumberType(Object o) {
		if (o instanceof Integer) {
			return DT_INT;
		} else if (o instanceof Double) {
			return DT_DOUBLE;
		} else if (o instanceof BigDecimal){
			return DT_DECIMAL;
		} else if (o instanceof Long) {
			return DT_LONG;
		} else if (o instanceof BigInteger){
			return DT_DECIMAL;
		} else if (o instanceof Float) {
			return DT_DOUBLE;
		} else if (o instanceof Number) { // Byte  Short
			return DT_INT;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(o.getClass().getName() + ": " + mm.getMessage("DataType.UnknownNum"));
		}
	}

	public static int getMaxNumberType(Object o1, Object o2) {
		int type1 = getNumberType(o1);
		int type2 = getNumberType(o2);

		return type1 > type2 ? type1 : type2;
	}

	// ����ת��BigDecimal
	private static BigDecimal toBigDecimal(Number o) {
		if (o instanceof BigDecimal) {
			return (BigDecimal)o;
		} else if (o instanceof BigInteger) {
			return new BigDecimal((BigInteger)o);
		} else if (o instanceof Long) { // ת��double���ܶ�����
			return new BigDecimal(((Long)o).longValue());
		} else {
			return new BigDecimal(o.doubleValue());
		}
	}
	
	// ����ת��BigInteger
	public static BigInteger toBigInteger(Number o) {
		if (o instanceof BigDecimal) {
			return ((BigDecimal)o).toBigInteger();
		} else if (o instanceof BigInteger) {
			return (BigInteger)o;
		} else {
			return BigInteger.valueOf(o.longValue());
		}
	}

	/**
	 * ת��oΪString����
	 * @param o Object
	 * @return String
	 */
	public static String toString(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof java.sql.Date) {
			return DateFormatFactory.get().getDateFormat().format((Date)o);
		} else if (o instanceof java.sql.Time) {
			return DateFormatFactory.get().getTimeFormat().format((Date)o);
		} else if (o instanceof java.sql.Timestamp) {
			return DateFormatFactory.get().getDateTimeFormat().format((Date)o);
		} else if (o instanceof java.util.Date) {
			return DateFormatFactory.get().getDateTimeFormat().format((Date)o);
		} else if (o instanceof byte[]) {
			return new String( (byte[]) o);
		} else {
			return o.toString();
		}
	}

	/**
	 * �Ѷ����ɵ����ı�ʱ��Ӧ�Ĵ�
	 * @param o ����
	 * @return String
	 */
	public static String toExportString(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof java.sql.Date) {
			return DateFormatFactory.get().getDateFormat().format((Date)o);
		} else if (o instanceof java.sql.Timestamp) {
			return DateFormatFactory.get().getDateTimeFormat().format((Date)o);
		} else if (o instanceof java.sql.Time) {
			return DateFormatFactory.get().getTimeFormat().format((Date)o);
		} else if (o instanceof Sequence) {
			return JSONUtil.toJSON((Sequence)o);
		} else if (o instanceof byte[]) {
			return new String( (byte[]) o);
		} else if (o instanceof BaseRecord) {
			return JSONUtil.toJSON((BaseRecord)o);
		} else {
			return o.toString();
		}
	}
	
	/**
	 * �Ѷ����ɵ����ı�ʱ��Ӧ�Ĵ����ַ���������
	 * @param o ����
	 * @param escapeChar ת���
	 * @return String
	 */
	public static String toExportString(Object o, char escapeChar) {
		if (o == null) {
			return null;
		} else if (o instanceof java.sql.Date) {
			return DateFormatFactory.get().getDateFormat().format((Date)o);
		} else if (o instanceof java.sql.Timestamp) {
			return DateFormatFactory.get().getDateTimeFormat().format((Date)o);
		} else if (o instanceof java.sql.Time) {
			return DateFormatFactory.get().getTimeFormat().format((Date)o);
		} else if (o instanceof String) {
			if (escapeChar == '"') {
				return Escape.addExcelQuote((String)o);
			} else {
				return Escape.addEscAndQuote((String)o, escapeChar);
			}
		} else if (o instanceof Sequence) {
			return JSONUtil.toJSON((Sequence)o);
		} else if (o instanceof byte[]) {
			return new String( (byte[]) o);
		} else if (o instanceof BaseRecord) {
			return JSONUtil.toJSON((BaseRecord)o);
		} else {
			return o.toString();
		}
	}

	/**
	 * ���ض����Ƿ����תΪ�ı�
	 * @param obj Object
	 * @return boolean
	 */
	public static boolean canConvertToString(Object obj) {
		if (obj instanceof BaseRecord) return false;
		if (obj instanceof Sequence && ((Sequence)obj).hasRecord()) return false;
		return true;
	}

	/**
	 * ��o��һ���ĸ�ʽת��Ϊ�ַ���
	 * @param o Object
	 * @param format ת����ʽ
	 * @return String
	 */
	public static String format(Object o, String format) {
		if (o instanceof Date) {
			if(format == null) {
				 if (o instanceof java.sql.Date) {
					 return DateFormatFactory.get().getDateFormat().format((Date)o);
				 } else if (o instanceof java.sql.Time) {
					 return DateFormatFactory.get().getTimeFormat().format((Date)o);
				 } else {
					 return DateFormatFactory.get().getDateTimeFormat().format((Date)o);
				 }
			} else {
				DateFormat sdf = new SimpleDateFormat(format);
				return sdf.format(o);
			}
		} else if (o instanceof Number) {
			com.ibm.icu.text.DecimalFormat nf = new com.ibm.icu.text.DecimalFormat(format);
			nf.setRoundingMode(BigDecimal.ROUND_HALF_UP);
			return nf.format(o);
		} else if (o instanceof Sequence) {
			Sequence series = (Sequence) o;
			StringBuffer sb = new StringBuffer();
			for (int i = 1, size = series.length(); i <= size; ++i) {
				if (i > 1) {
					sb.append(',');
				}
				sb.append(format(series.getMem(i), format));
			}
			return sb.toString();
		} else if (o == null) {
			return null;
		} else if (o instanceof byte[]) {
			try {
				return new String((byte[])o, format);
			} catch (UnsupportedEncodingException e) {
				throw new RQException(e.getMessage(), e);
			}
		} else {
			return o.toString();
		}
	}

	/**
	 * ��o��һ���ĸ�ʽת��Ϊ�ַ���
	 * @param o Object
	 * @param format ת����ʽ
	 * @param locale ����
	 * @return String
	 */
	public static String format(Object o, String format, String locale) {
		if (o instanceof Date) {
			return DateFormatFactory.get().getFormat(format, locale).format((Date)o);
		} else if (o instanceof Number) {
			com.ibm.icu.text.DecimalFormat nf = new com.ibm.icu.text.DecimalFormat(format);
			nf.setRoundingMode(BigDecimal.ROUND_HALF_UP);
			return nf.format(o);
		} else if (o instanceof Sequence) {
			Sequence series = (Sequence) o;
			StringBuffer sb = new StringBuffer();
			for (int i = 1, size = series.length(); i <= size; ++i) {
				if (i > 1) {
					sb.append(',');
				}
				sb.append(format(series.getMem(i), format, locale));
			}
			return sb.toString();
		} else if (o == null) {
			return null;
		} else if (o instanceof byte[]) {
			String str = new String((byte[])o);
			return format(str, format, locale);
		} else {
			return o.toString();
		}
	}

	/**
	 *  �� long ��ʽ����o��Ӧ������ֵ����ֵ
	 * @param o Object
	 * @return long
	 */
	public static long longValue(Object o) {
		if (o instanceof Number) {
			return ( (Number) o).longValue();
		}
		MessageManager mm = EngineMessage.get();
		throw new RuntimeException(o + mm.getMessage("Variant2.longValue"));
	}

	/**
	 *  �� double ��ʽ����o��Ӧ����ֵ
	 * @param o Object
	 * @return double
	 */
	public static double doubleValue(Object o) {
		if (o instanceof Number) {
			return ( (Number) o).doubleValue();
		}
		MessageManager mm = EngineMessage.get();
		throw new RuntimeException(o + mm.getMessage("Variant2.doubleValue"));
	}

	/**
	 * ��oת��ΪBigDecimal
	 * @param o Object
	 * @return BigDecimal
	 */
	public static BigDecimal toBigDecimal(Object o) {
		if (o instanceof BigDecimal) {
			return (BigDecimal) o;
		} else if (o instanceof BigInteger) {
			return new BigDecimal( (BigInteger) o);
		} else if (o instanceof Long) { // ת��double���ܶ�����
			return new BigDecimal(((Long)o).longValue());
		} else if (o instanceof Number) {
			return new BigDecimal(((Number)o).doubleValue());
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RuntimeException(o + mm.getMessage("Variant2.doubleValue"));
		}
	}

	/**
	 * ȥ���������
	 * @param obj ����
	 * @return byte ������Types��
	 */
	public static byte getObjectType(Object obj) {
		if (obj instanceof String) {
			return Types.DT_STRING;
		} else if (obj instanceof Integer) {
			return Types.DT_INT;
		} else if (obj instanceof Double) {
			return Types.DT_DOUBLE;
		} else if (obj instanceof java.sql.Date) {
			return Types.DT_DATE;
		} else if (obj instanceof BigDecimal) {
			return Types.DT_DECIMAL;
		} else if (obj instanceof Long) {
			return Types.DT_LONG;
		} else if (obj instanceof java.sql.Timestamp) {
			return Types.DT_DATETIME;
		} else if (obj instanceof java.sql.Time) {
			return Types.DT_TIME;
		} else if (obj instanceof Boolean) {
			return Types.DT_BOOLEAN;
		} else {
			return Types.DT_DEFAULT;
		}
	}

	/**
	 * �Ѷ���ת��ָ��������
	 * @param val ����
	 * @param type ���ͣ�������Types��
	 * @return
	 */
	public static Object convert(Object val, byte type) {
		if (val instanceof String) {
			return parseCellValue((String)val, type);
		} else if (val == null) {
			return null;
		}
		
		switch (type) {
		case Types.DT_STRING:
			return toString(val);
		case Types.DT_INT:
			if (val instanceof Integer) {
				return val;
			} else if (val instanceof Number) {
				return new Integer(((Number)val).intValue());
			}
			
			break;
		case Types.DT_DOUBLE:
			if (val instanceof Double) {
				return val;
			} else if (val instanceof Number) {
				return new Double(((Number)val).doubleValue());
			}

			break;
		case Types.DT_DATE:
			if (val instanceof java.sql.Date) {
				return val;
			} else if (val instanceof java.util.Date) {
				return new java.sql.Date(((java.util.Date)val).getTime());
			} else if (val instanceof Number) {
				return new java.sql.Date(((Number)val).longValue());
			}

			break;
		case Types.DT_DECIMAL:
			if (val instanceof Number) {
				return toBigDecimal((Number)val);
			}

			break;
		case Types.DT_LONG:
			if (val instanceof Long) {
				return val;
			} else if (val instanceof Number) {
				return new Long(((Number)val).longValue());
			}

			break;
		case Types.DT_DATETIME:
			if (val instanceof java.sql.Timestamp) {
				return val;
			} else if (val instanceof java.util.Date) {
				return new java.sql.Timestamp(((java.util.Date)val).getTime());
			} else if (val instanceof Number) {
				return new java.sql.Timestamp(((Number)val).longValue());
			}

			break;
		case Types.DT_TIME:
			if (val instanceof java.sql.Time) {
				return val;
			} else if (val instanceof java.util.Date) {
				return new java.sql.Time(((java.util.Date)val).getTime());
			} else if (val instanceof Number) {
				return new java.sql.Time(((Number)val).longValue());
			}

			break;
		default:
			break;
		}

		return val;
	}

	/**
	 * �����ʽת��Ϊ��Ӧ��ֵ
	 * @param text String
	 * @return Object
	 */
	public static Object parse(String text) {
		return parse(text, true);
	}

	/**
	 * �����ʽת��Ϊ��Ӧ��ֵ��ע�⣬�����ַ��������еĿո���trim��
	 * @param text String ��ת�����ַ���
	 * @param removeEscAndQuote boolean �Ƿ�ɾ��ת���������
	 * @return Object
	 */
	public static Object parse(String text, boolean removeEscAndQuote) {
		if (text == null || text.length() == 0) {
			return null;
		}
		
		String s = text.trim();
		int len = s.length();
		if (len == 0) {
			return text;
		}

		char ch0 = s.charAt(0);
		if (ch0 == '"'|| ch0 == '\'') {
			if (removeEscAndQuote) {
				int match = Sentence.scanQuotation(s, 0);
				if (match == len -1) {
					return Escape.remove(s.substring(1, match));
				}
			}
			return text;
		}

		Number numObj = parseInt(s);
		if (numObj != null) return numObj;

		numObj = parseLong(s);
		if (numObj != null) return numObj;

		if (s.endsWith("%")) { // 5%
			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(s.
					substring(0, s.length() - 1));
				if (fd != null) return new Double(fd.doubleValue() / 100);
			} catch (RuntimeException e) {
			}
		} else {
			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(s);
				if (fd != null) return new Double(fd.doubleValue());
			} catch (RuntimeException e) {
			}
		}

		if (len > 2 && ch0 == '0' && (s.charAt(1) == 'X' || s.charAt(1) == 'x')) {
			numObj = parseLong(s.substring(2), 16);
			if (numObj != null) return numObj;
		}

		if (s.equals("null")) return null; // IgnoreCase
		if (s.equals("true")) return Boolean.TRUE; // IgnoreCase
		if (s.equals("false")) return Boolean.FALSE; // IgnoreCase

		// [1,2,3]
		if (ch0 == '[' || ch0 == '{') {
			char[] chars = s.toCharArray();
			Object obj = JSONUtil.parseJSON(chars, 0, chars.length - 1);
			if (obj != null) {
				return obj;
			} else {
				return text;
			}
		}

		return parseDate(text);
	}
	
	/**
	 * ������Ϊֵ������trim��ȥ����
	 * @param s ��
	 * @return Object
	 */
	public static Object parseDirect(String s) {
		if (s == null || s.length() == 0) {
			return null;
		}

		char ch0 = s.charAt(0);
		if (ch0 == '"'|| ch0 == '\'') {
			return s;
		}

		Number numObj = parseInt(s);
		if (numObj != null) return numObj;

		numObj = parseLong(s);
		if (numObj != null) return numObj;

		if (s.endsWith("%")) { // 5%
			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(s.
					substring(0, s.length() - 1));
				if (fd != null) return new Double(fd.doubleValue() / 100);
			} catch (RuntimeException e) {
			}
		} else {
			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(s);
				if (fd != null) return new Double(fd.doubleValue());
			} catch (RuntimeException e) {
			}
		}

		int len = s.length();
		if (len > 2 && ch0 == '0' && (s.charAt(1) == 'X' || s.charAt(1) == 'x')) {
			numObj = parseLong(s.substring(2), 16);
			if (numObj != null) return numObj;
		}

		if (s.equals("null")) return null; // IgnoreCase
		if (s.equals("true")) return Boolean.TRUE; // IgnoreCase
		if (s.equals("false")) return Boolean.FALSE; // IgnoreCase

		// [1,2,3]
		if (ch0 == '[' || ch0 == '{') {
			char[] chars = s.toCharArray();
			Object obj = JSONUtil.parseJSON(chars, 0, chars.length - 1);
			if (obj != null) {
				return obj;
			} else {
				return s;
			}
		}

		return parseDate(s);
	}
	
	/**
	 * ���ַ���������ָ�����͵Ķ���
	 * @param text �ַ���
	 * @param types ÿ�е�������ɵ����飬����Types
	 * @param col �к�
	 * @return ������Ķ���
	 */
	public static Object parse(String text, byte []types, int col) {
		if (text == null) {
			return null;
		}
		
		int len = text.length();
		if (len == 0) {
			return null;
		}

		switch (types[col]) {
		case Types.DT_STRING:
			return text;
		case Types.DT_INT:
			Number numObj = parseInt(text);
			if (numObj != null) return numObj;

			numObj = parseLong(text);
			if (numObj != null) {
				types[col] = Types.DT_LONG;
				return numObj;
			}

			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(text);
				if (fd != null) {
					types[col] = Types.DT_DOUBLE;
					return new Double(fd.doubleValue());
				}
			} catch (RuntimeException e) {
			}

			break;
		case Types.DT_DOUBLE:
			if (text.endsWith("%")) { // 5%
				try {
					FloatingDecimal fd = FloatingDecimal.readJavaFormatString(
						text.substring(0, text.length() - 1));
					if (fd != null) {
						return new Double(fd.doubleValue() / 100);
					}
				} catch (RuntimeException e) {
				}
			} else {
				try {
					FloatingDecimal fd = FloatingDecimal.readJavaFormatString(text);
					if (fd != null) {
						return new Double(fd.doubleValue());
					}
				} catch (RuntimeException e) {
				}
			}

			break;
		case Types.DT_DATE:
			Date date = DateFormatFactory.get().getDateFormatX().parse(text);
			if (date != null) {
				return new java.sql.Date(date.getTime());
			}

			break;
		case Types.DT_DECIMAL:
			try {
				return new BigDecimal(text);
			} catch (NumberFormatException e) {}

			break;
		case Types.DT_LONG:
			if (len > 2 && text.charAt(0) == '0' &&
				(text.charAt(1) == 'X' || text.charAt(1) == 'x')) {
				numObj = parseLong(text.substring(2), 16);
				if (numObj != null) {
					return numObj;
				}
			} else {
				numObj = parseLong(text);
				if (numObj != null) {
					return numObj;
				}
			}

			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(text);
				if (fd != null) {
					types[col] = Types.DT_DOUBLE;
					return new Double(fd.doubleValue());
				}
			} catch (RuntimeException e) {}

			break;
		case Types.DT_DATETIME:
			date = DateFormatFactory.get().getDateTimeFormatX().parse(text);
			if (date != null) {
				return new java.sql.Timestamp(date.getTime());
			}

			break;
		case Types.DT_TIME:
			date = DateFormatFactory.get().getTimeFormatX().parse(text);
			if (date != null) {
				return new java.sql.Time(date.getTime());
			}

			break;
		case Types.DT_BOOLEAN:
			if (text.equals("true")) {
				return Boolean.TRUE;
			} else if (text.equals("false")) {
				return Boolean.FALSE;
			}

			break;
		default:
			Object val = parse(text, false);
			types[col] = getObjectType(val);
			return val;
		}

		if (text.equals("null")) {
			return null;
		}

		Object val = parse(text, false);
		//types[col] = getObjectType(val);
		return val;
	}

	/**
	 * ���ڵ�Ԫ��excel�����ַ�������
	 * @param text �ַ���
	 * @return Object �������ֵ
	 */
	public static Object parseCellValue(String text) {
		if (text == null || text.length() == 0) {
			return null;
		}
		
		String s = text.trim();
		int len = s.length();
		if (len == 0) {
			return text;
		}

		char ch0 = s.charAt(0);
		if (ch0 == '"'|| ch0 == '\'') {
			return text;
		}

		Number numObj = parseInt(s);
		if (numObj != null) return numObj;

		numObj = parseLong(s);
		if (numObj != null) return numObj;

		if (s.endsWith("%")) { // 5%
			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(s.
					substring(0, s.length() - 1));
				if (fd != null) return new Double(fd.doubleValue() / 100);
			} catch (RuntimeException e) {
			}
		} else {
			try {
				FloatingDecimal fd = FloatingDecimal.readJavaFormatString(s);
				if (fd != null) return new Double(fd.doubleValue());
			} catch (RuntimeException e) {
			}
		}

		if (len > 2 && ch0 == '0' && (s.charAt(1) == 'X' || s.charAt(1) == 'x')) {
			numObj = parseLong(s.substring(2), 16);
			if (numObj != null) return numObj;
		}

		if (s.equals("null")) return null; // IgnoreCase
		if (s.equals("true")) return Boolean.TRUE; // IgnoreCase
		if (s.equals("false")) return Boolean.FALSE; // IgnoreCase

		// [1,2,3]
		if (ch0 == '[' || ch0 == '{') {
			char[] chars = s.toCharArray();
			Object obj = JSONUtil.parseJSON(chars, 0, chars.length - 1);
			if (obj != null) {
				return obj;
			} else {
				return text;
			}
		}

		return parseCellDate(text);
	}

	// ���ڵ�Ԫ��excel�������ڽ���
	private static Object parseCellDate(String text) {
		DateFormatFactory dff = DateFormatFactory.get();
		Date date = dff.getDateTimeFormatX().parse(text);
		if (date != null) return new java.sql.Timestamp(date.getTime());

		date = dff.getFormatX("yyyy/MM/dd HH:mm:ss").parse(text);
		if (date != null) return new java.sql.Timestamp(date.getTime());

		date = dff.getFormatX("yyyy-MM-dd HH:mm:ss").parse(text);
		if (date != null) return new java.sql.Timestamp(date.getTime());

		date = dff.getFormatX("yyyy/MM/dd HH:mm").parse(text);
		if (date != null) return new java.sql.Timestamp(date.getTime());

		date = dff.getFormatX("yyyy-MM-dd HH:mm").parse(text);
		if (date != null) return new java.sql.Timestamp(date.getTime());

		date = dff.getDateFormatX().parse(text);
		if (date != null) return new java.sql.Date(date.getTime());

		date = dff.getFormatX("yyyy/MM/dd").parse(text);
		if (date != null) return new java.sql.Date(date.getTime());

		date = dff.getFormatX("yyyy-MM-dd").parse(text);
		if (date != null) return new java.sql.Date(date.getTime());

		date = dff.getTimeFormatX().parse(text);
		if (date != null) return new java.sql.Time(date.getTime());

		date = dff.getFormatX("HH:mm").parse(text);
		if (date != null) return new java.sql.Time(date.getTime());

		// 5-2  5/2 12:16
		int index = text.indexOf('-');
		if (index != -1 || (index = text.indexOf('/')) != -1) {
			int month = parseUnsignedInt(text.substring(0, index));
			if (month < 1 || month > 12) return text;

			int day = parseUnsignedInt(text.substring(index + 1));
			if (day < 1 || day > 31) return text;

			long cur = System.currentTimeMillis();
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(cur);
			calendar.set(calendar.get(Calendar.YEAR), month - 1, 1, 0, 0, 0);
			if (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) < day) return text;

			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.DAY_OF_MONTH, day);
			return new java.sql.Date(calendar.getTimeInMillis());
		}

		return text;
	}

	/**
	 * ���ַ���ת����ֵ���������ת���򷵻ؿա���int��long��double��
	 * @param s String
	 * @return Number
	 */
	public static Number parseNumber(String s) {
		if (s == null) return null;
		s = s.trim();
		int len = s.length();
		if (len == 0) return null;

		Number numObj = parseInt(s);
		if (numObj != null) return numObj;

		numObj = parseLong(s);
		if (numObj != null) return numObj;

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

		if (len > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'X' || s.charAt(1) == 'x')) {
			numObj = parseLong(s.substring(2), 16);
			if (numObj != null) return numObj;
		}

		return null;
	}

	public static Double parseDouble(String s) {
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
	
	/**
	 * ��ʱ��תΪ��Ӧ��ʱ��ֵ
	 * @param text String
	 * @return Object
	 */
	public static Object parseDate(String text) {
		Date date = DateFormatFactory.get().getDateTimeFormatX().parse(text);
		if (date != null) return new java.sql.Timestamp(date.getTime());

		date = DateFormatFactory.get().getDateFormatX().parse(text);
		if (date != null) return new java.sql.Date(date.getTime());

		date = DateFormatFactory.get().getTimeFormatX().parse(text);
		if (date != null) return new java.sql.Time(date.getTime());

		return text;
	}

	/**
	 * ȡ�������������������ʾ��Ϣ
	 * @param o ����
	 * @return String
	 */
	public static String getDataType(Object o) {
		MessageManager mm = EngineMessage.get();

		if ( o == null ) return mm.getMessage("DataType.Null");
		if ( o instanceof String ) return mm.getMessage("DataType.String");
		if ( o instanceof Integer ) return mm.getMessage("DataType.Integer");
		if ( o instanceof Long ) return mm.getMessage("DataType.Long");
		if ( o instanceof Double ) return mm.getMessage("DataType.Double");
		if ( o instanceof Boolean ) return mm.getMessage("DataType.Boolean");
		if ( o instanceof BigDecimal ) return mm.getMessage("DataType.BigDecimal");
		if ( o instanceof Sequence ) return mm.getMessage("DataType.Series");
		if ( o instanceof BaseRecord ) return mm.getMessage("DataType.Record");
		if ( o instanceof byte[] ) return mm.getMessage("DataType.ByteArray");
		if ( o instanceof java.sql.Date ) return mm.getMessage("DataType.Date");
		if ( o instanceof java.sql.Time ) return mm.getMessage("DataType.Time");
		if ( o instanceof java.sql.Timestamp ) return mm.getMessage("DataType.Timestamp");
		if ( o instanceof Byte ) return mm.getMessage("DataType.Byte");
		if ( o instanceof Short ) return mm.getMessage("DataType.Short");
		return o.getClass().getName();
	}

	/**
	 * ���ڵ�Ԫ��excel�����ַ�������
	 * @param text �ַ���
	 * @param type ���ͣ�������Types��
	 * @return Object �������ֵ
	 */
	public static Object parseCellValue(String text, byte type) {
		if (type == Types.DT_DEFAULT) return parseCellValue(text);

		if (text == null || text.length() == 0) return null;
		String s = text.trim();
		int len = s.length();
		if (len == 0) return text;

		switch (type) {
		case Types.DT_STRING:
			return text;
		case Types.DT_INT:
			Number numObj = parseInt(s);
			if (numObj != null) return numObj;
			break;
		case Types.DT_LONG:
			if (len > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'X' || s.charAt(1) == 'x')) {
				numObj = parseLong(s.substring(2), 16);
				if (numObj != null) return numObj;
			} else {
				numObj = parseLong(s);
				if (numObj != null) return numObj;
			}
			break;
		case Types.DT_DOUBLE:
			if (s.endsWith("%")) { // 5%
				try {
					FloatingDecimal fd = FloatingDecimal.readJavaFormatString(
						s.substring(0, s.length() - 1));
					if (fd != null) return new Double(fd.doubleValue() / 100);
				} catch (RuntimeException e) {
				}
			} else {
				try {
					FloatingDecimal fd = FloatingDecimal.readJavaFormatString(s);
					if (fd != null) return new Double(fd.doubleValue());
				} catch (RuntimeException e) {
				}
			}
			break;
		case Types.DT_DATE:
			Date date = DateFormatFactory.get().getDateFormatX().parse(s);
			if (date != null) return new java.sql.Date(date.getTime());

			return parseCellDate(s);
		case Types.DT_TIME:
			date = DateFormatFactory.get().getTimeFormatX().parse(s);
			if (date != null) return new java.sql.Time(date.getTime());

			return parseCellDate(s);
		case Types.DT_DATETIME:
			date = DateFormatFactory.get().getDateTimeFormatX().parse(s);
			if (date != null) return new java.sql.Timestamp(date.getTime());

			return parseCellDate(s);
		default:
			break;
		}

		return text;
	}
	
	private static boolean isEquals(byte[] b1, byte[] b2) {
		int len1 = b1.length;
		if (b2.length != len1) {
			return false;
		}
		
		for(int i = 0; i < len1; ++i) {
			if (b1[i] != b2[i]) {
				return false;
			}
		}
		
		return true;
	}

	public static int compareArrays(byte[] b1, byte[] b2) {
		int len1 = b1.length;
		int len2 = b2.length;
		if (len1 == len2) {
			for(int i = 0; i < len1; ++i) {
				if (b1[i] < b2[i]) {
					return -1;
				} else if (b1[i] > b2[i]) {
					return 1;
				}
			}
			
			return 0;
		} else if (len1 < len2) {
			for(int i = 0; i < len1; ++i) {
				if (b1[i] < b2[i]) {
					return -1;
				} else if (b1[i] > b2[i]) {
					return 1;
				}
			}
			
			return -1;
		} else {
			for(int i = 0; i < len2; ++i) {
				if (b1[i] < b2[i]) {
					return -1;
				} else if (b1[i] > b2[i]) {
					return 1;
				}
			}
			
			return 1;
		}
	}

	private static int parseUnsignedInt(String s) {
		if ( s== null || s.length() == 0) return -1;

		int result = 0;
		int i = 0, max = s.length();
		int digit;
		int limit = -Integer.MAX_VALUE;
		int multmin = limit / 10;

		if (i < max) {
			digit = Character.digit(s.charAt(i++), 10);
			if (digit < 0) {
				return -1;
			} else {
				result = -digit;
			}
		}

		while (i < max) {
			// Accumulating negatively avoids surprises near MAX_VALUE
			digit = Character.digit(s.charAt(i++), 10);
			if (digit < 0) {
				return -1;
			}
			if (result < multmin) {
				return -1;
			}
			result *= 10;
			if (result < limit + digit) {
				return -1;
			}
			result -= digit;
		}

		return -result;
	}

	public static Integer parseInt(String s) {
		int result = 0;
		boolean negative = false;
		int i = 0, max = s.length();
		int limit;
		int multmin;
		int digit;

		if (max > 0) {
			if (s.charAt(0) == '-') {
				negative = true;
				limit = Integer.MIN_VALUE;
				i++;
			} else {
				limit = -Integer.MAX_VALUE;
			}
			multmin = limit / 10;
			if (i < max) {
				digit = Character.digit(s.charAt(i++), 10);
				if (digit < 0) {
					return null;
				} else {
					result = -digit;
				}
			} while (i < max) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				digit = Character.digit(s.charAt(i++), 10);
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
		} else {
			return null;
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

	public static Long parseLong(String s) {
		long result = 0;
		boolean negative = false;
		int i = 0, max = s.length();
		long limit;
		long multmin;
		int digit;

		if (max > 0) {
			// 1L
			if (max > 1 && s.charAt(max - 1) == 'L') max--;

			if (s.charAt(0) == '-') {
				negative = true;
				limit = Long.MIN_VALUE;
				i++;
			} else {
				limit = -Long.MAX_VALUE;
			}
			multmin = limit / 10;
			if (i < max) {
				digit = Character.digit(s.charAt(i++), 10);
				if (digit < 0) {
					return null;
				} else {
					result = -digit;
				}
			} while (i < max) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				digit = Character.digit(s.charAt(i++), 10);
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
		} else {
			return null;
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

	/**
	 * ����ָ�����Ƶĳ�����
	 * @param s �������ַ���
	 * @param radix ����
	 * @return Long
	 */
	public static Long parseLong(String s, int radix) {
		int len = s.length();
		if (len == 0) {
			return null;
		} else if (s.charAt(0) == '-') {
			if (len == 1) {
				return null;
			}
			
			long result = 0;
			int digit = Character.digit(s.charAt(1), radix);
			if (digit < 0) {
				return null;
			} else {
				result = -digit;
			}
			
			long limit = Long.MIN_VALUE;
			long multmin = limit / radix;
			
			for (int i = 2; i < len; ++i) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				digit = Character.digit(s.charAt(i), radix);
				if (digit < 0) {
					return null;
				} else if (result < multmin) {
					return null;
				}
				
				result *= radix;
				if (result < limit + digit) {
					return null;
				}
				result -= digit;
			}
			
			return result;
		} else {
			long result = Character.digit(s.charAt(0), radix);
			if (result < 0) {
				return null;
			}
			
			for (int i = 1; i < len; ++i) {
				int digit = Character.digit(s.charAt(i), radix);
				if (digit < 0) {
					return null;
				}
				
				result = result * radix + digit;
			}
			
			return result;
		}
	}
	
	/**
	 * ����ָ�����Ƶĳ�����
	 * @param s �������ַ���
	 * @param radix ����
	 * @return long�����ʽ�������׳�NumberFormatException�쳣
	 */
	public static long parseLongValue(String s, int radix) {
		int len = s.length();
		if (len == 0) {
			throw new NumberFormatException("null");
		} else if (s.charAt(0) == '-') {
			if (len == 1) {
				 throw new NumberFormatException(s);
			}
			
			long result = 0;
			int digit = Character.digit(s.charAt(1), radix);
			if (digit < 0) {
				throw new NumberFormatException(s);
			} else {
				result = -digit;
			}
			
			long limit = Long.MIN_VALUE;
			long multmin = limit / radix;
			
			for (int i = 2; i < len; ++i) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				digit = Character.digit(s.charAt(i), radix);
				if (digit < 0) {
					throw new NumberFormatException(s);
				} else if (result < multmin) {
					throw new NumberFormatException(s);
				}
				
				result *= radix;
				if (result < limit + digit) {
					throw new NumberFormatException(s);
				}
				result -= digit;
			}
			
			return result;
		} else {
			long result = Character.digit(s.charAt(0), radix);
			if (result < 0) {
				throw new NumberFormatException(s);
			}
			
			for (int i = 1; i < len; ++i) {
				int digit = Character.digit(s.charAt(i), radix);
				if (digit < 0) {
					throw new NumberFormatException(s);
				}
				
				result = result * radix + digit;
			}
			
			return result;
		}
	}
	
	/**
	 * ����ָ���������ָ������������
	 * @param date ����
	 * @param n ����
	 * @return Date
	 */
	public static Date dayElapse(Calendar calendar, Date date, int n) {
		calendar.setTime(date);
		calendar.add(Calendar.DATE, n);
		date = (Date)date.clone();
		date.setTime(calendar.getTimeInMillis());
		return date;
	}

	/**
	 * ����ָ�����ڼ��ָ��ʱ��������
	 * @param date ����
	 * @param diff �������
	 * @param opt ѡ�y�������λΪ�꣬q�������λΪ����m�������λΪ�£�s�������λΪ�룬ms�������λΪ����
	 * e���������µף�ȱʡ�����µ������µף���@yqm���
	 * @return ����
	 */
	public static Date elapse(Date date, int diff, String opt) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		if (opt == null) {
			c.add(Calendar.DATE, diff);
		} else if (opt.indexOf('y') != -1) {
			if (opt.indexOf('e') != -1 || c.get(Calendar.DATE) != c.getActualMaximum(Calendar.DATE)) {
				c.add(Calendar.YEAR, diff);
			} else {
				c.add(Calendar.YEAR, diff);
				c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
			}
		} else if (opt.indexOf('q') != -1) { // ��
			if (opt.indexOf('e') != -1 || c.get(Calendar.DATE) != c.getActualMaximum(Calendar.DATE)) {
				c.add(Calendar.MONTH, diff * 3);
			} else {
				c.add(Calendar.MONTH, diff * 3);
				c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
			}
		} else if (opt.indexOf("ms") != -1) {
			c.add(Calendar.MILLISECOND, diff);
		} else if (opt.indexOf('m') != -1) {
			if (opt.indexOf('e') != -1 || c.get(Calendar.DATE) != c.getActualMaximum(Calendar.DATE)) {
				c.add(Calendar.MONTH, diff);
			} else {
				c.add(Calendar.MONTH, diff);
				c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
			}
		} else if (opt.indexOf('s') != -1) {
			c.add(Calendar.SECOND, diff);
		} else {
			c.add(Calendar.DATE, diff);
		}

		date = (Date)date.clone();
		date.setTime(c.getTimeInMillis());
		return date;
	}

	private static int dayElapse(int date, int diff) {
		int y, m, d;
		if (date < 0) {
			y = date / 384 + 1969;
			m = date / 32 % 12 + 11;
			d = date % 32 + 32;
		} else {
			y = date / 384 + 1970;
			m = date / 32 % 12;
			d = date % 32;
		}
		
		if (diff > 0) {
			while (true) {
				int maxDay;
				if (y % 400 == 0 || (y % 4 == 0 && y % 100 != 0)) {
					maxDay = LEEPYEARDAYS[m];
				} else {
					maxDay = DAYS[m];
				}
				
				int n = d + diff;
				if (n <= maxDay) {
					d = n;
					break;
				} else {
					diff -= (maxDay - d + 1);
					d = 1;
					m++;
					
					if (m == 12) {
						m = 0;
						y++;
					}
				}
			}
		} else {
			diff = -diff;
			while (true) {
				if (d > diff) {
					d -= diff;
					break;
				} else {
					diff -= d;
					m--;
					
					if (m < 0) {
						m = 11;
						y--;
					}
					
					if (y % 400 == 0 || (y % 4 == 0 && y % 100 != 0)) {
						d = LEEPYEARDAYS[m];
					} else {
						d = DAYS[m];
					}
				}
			}
		}
		
		return DateFactory.toDays(y, m, d);
	}
	
	private static int yearElapse(int date, int diff, boolean adjustEnd) {
		int y, m, d;
		if (date < 0) {
			y = date / 384 + 1969;
			m = date / 32 % 12 + 11;
			d = date % 32 + 32;
		} else {
			y = date / 384 + 1970;
			m = date / 32 % 12;
			d = date % 32;
		}
		
		y += diff;
		if (adjustEnd) {
			if (y % 400 == 0 || (y % 4 == 0 && y % 100 != 0)) {
				d = LEEPYEARDAYS[m];
			} else {
				d = DAYS[m];
			}
		} else {
			if (y % 400 == 0 || (y % 4 == 0 && y % 100 != 0)) {
				if (d > LEEPYEARDAYS[m]) {
					d = LEEPYEARDAYS[m];
				}
			} else {
				if (d > DAYS[m]) {
					d = DAYS[m];
				}
			}
		}
		
		return DateFactory.toDays(y, m, d);
	}
	
	private static int monthElapse(int date, int diff, boolean adjustEnd) {
		int y, m, d;
		if (date < 0) {
			y = date / 384 + 1969;
			m = date / 32 % 12 + 11;
			d = date % 32 + 32;
		} else {
			y = date / 384 + 1970;
			m = date / 32 % 12;
			d = date % 32;
		}
		
		if (diff > 0) {
			while (true) {
				int n = m + diff;
				if (n < 12) {
					m = n;
					break;
				} else {
					diff -= (12 - m);
					m = 0;
					y++;
				}
			}
		} else {
			diff = -diff;
			while (true) {
				if (m >= diff) {
					m -= diff;
					break;
				} else {
					diff -= (m + 1);
					m = 11;
					y--;
				}
			}
		}
		
		if (adjustEnd) {
			if (y % 400 == 0 || (y % 4 == 0 && y % 100 != 0)) {
				d = LEEPYEARDAYS[m];
			} else {
				d = DAYS[m];
			}
		} else {
			if (y % 400 == 0 || (y % 4 == 0 && y % 100 != 0)) {
				if (d > LEEPYEARDAYS[m]) {
					d = LEEPYEARDAYS[m];
				}
			} else {
				if (d > DAYS[m]) {
					d = DAYS[m];
				}
			}
		}
		
		return DateFactory.toDays(y, m, d);
	}
	
	/**
	 * ����ָ�����ڼ��ָ��ʱ��������
	 * @param date date@o�������������
	 * @param diff �������
	 * @param opt ѡ�y�������λΪ�꣬q�������λΪ����m�������λΪ�£�e���������µף�ȱʡ�����µ������µף���@yqm���
	 * @return int
	 */
	public static int elapse(int date, int diff, String opt) {
		if (diff == 0) {
			return date;
		}
		
		if (opt == null) {
			return dayElapse(date, diff);
		} else if (opt.indexOf('y') != -1) {
			return yearElapse(date, diff, opt.indexOf('e') == -1);
		} else if (opt.indexOf('q') != -1) { // ��
			return monthElapse(date, diff * 3, opt.indexOf('e') == -1);
		} else if (opt.indexOf('m') != -1) {
			return monthElapse(date, diff, opt.indexOf('e') == -1);
		} else {
			return dayElapse(date, diff);
		}
	}
	
	private static long yearInterval(Date date1, Date date2) {
		DateFactory df = DateFactory.get();
		return df.year(date2) - df.year(date1);
	}

	private static long quaterInterval(Date date1, Date date2) {
		DateFactory df = DateFactory.get();
		Calendar calendar = df.getCalendar();
		
		calendar.setTime(date1);
		int y1 = calendar.get(Calendar.YEAR);
		int m1 = calendar.get(Calendar.MONTH);
		
		calendar.setTime(date2);
		int y2 = calendar.get(Calendar.YEAR);
		int m2 = calendar.get(Calendar.MONTH);

		if (m1 < 3) {
			m1 = 1;
		} else if (m1 < 6) {
			m1 = 2;
		} else if (m1 < 9) {
			m1 = 3;
		} else {
			m1 = 4;
		}

		if (m2 < 3) {
			m2 = 1;
		} else if (m2 < 6) {
			m2 = 2;
		} else if (m2 < 9) {
			m2 = 3;
		} else {
			m2 = 4;
		}

		return (y2 - y1) * 4 + (m2 - m1);
	}

	private static long monthInterval(Date date1, Date date2) {
		DateFactory df = DateFactory.get();
		int yearDiff = df.year(date2) - df.year(date1);
		int monthDiff = df.month(date2) - df.month(date1);
		return yearDiff * 12 + monthDiff;
	}

	private static long weekInterval(Date date1, Date date2) {
		long day = dayInterval(date1, date2);
		return day / 7;
	}
	
	// ���������������ұ������
	private static long weekInterval_7(Date date1, Date date2) {
		long day = dayInterval(date1, date2);
		if (day < 0) {
			day = -day;
			Date tmp = date1;
			date1 = date2;
			date2 = tmp;
		}
		
		int week = DateFactory.get().week((Date)date1);
		if (week != Calendar.SUNDAY) {
			// ����������
			int n = Calendar.SATURDAY - week + 1;
			day -= n;
			if (day >= 0) {
				return day / 7 + 1;
			} else {
				return 0;
			}
		} else {
			return day / 7;
		}
	}
	
	// ��һ�����������ұ������
	private static long weekInterval_1(Date date1, Date date2) {
		long day = dayInterval(date1, date2);
		if (day < 0) {
			day = -day;
			Date tmp = date1;
			date1 = date2;
			date2 = tmp;
		}
		
		int week = DateFactory.get().week((Date)date1);
		if (week != Calendar.MONDAY) {
			// ��������һ
			int n = week == Calendar.SUNDAY ? 1 : Calendar.SATURDAY - week + 2;
			day -= n;
			if (day >= 0) {
				return day / 7 + 1;
			} else {
				return 0;
			}
		} else {
			return day / 7;
		}
	}
	
	/**
	 * �����������ڼ��������
	 * @param date1 Date
	 * @param date2 Date
	 * @return long ����
	 */
	public static long dayInterval(Date date1, Date date2) {
		return (date2.getTime() - BASEDATE) / 86400000 - (date1.getTime() - BASEDATE) / 86400000;
	}
	
	/**
	 * �����������ڼ��������
	 * @param date1 Date
	 * @param date2 Date
	 * @return long ����
	 */
	public static long dayInterval(long date1, long date2) {
		return (date2 - BASEDATE) / 86400000 - (date1 - BASEDATE) / 86400000;
	}

	private static int innerDayInterval(int date1, int date2) {
		if (date1 < date2) {
			int y1, m1, d1, y2, m2, d2;
			if (date1 < 0) {
				y1 = date1 / 384 + 1969;
				m1 = date1 / 32 % 12 + 11;
				d1 = date1 % 32 + 32;
			} else {
				y1 = date1 / 384 + 1970;
				m1 = date1 / 32 % 12;
				d1 = date1 % 32;
			}
			
			if (date2 < 0) {
				y2 = date2 / 384 + 1969;
				m2 = date2 / 32 % 12 + 11;
				d2 = date2 % 32 + 32;
			} else {
				y2 = date2 / 384 + 1970;
				m2 = date2 / 32 % 12;
				d2 = date2 % 32;
			}
			
			int interval = 0;
			for (int y = y1; y < y2; ++y) {
				if (y % 400 == 0 || (y % 4 == 0 && y % 100 != 0)) {
					interval += 366;
				} else {
					interval += 365;
				}
			}
			
			interval -= d1;
			if (y1 % 400 == 0 || (y1 % 4 == 0 && y1 % 100 != 0)) {
				for (int m = 0; m < m1; ++m) {
					interval -= LEEPYEARDAYS[m];
				}
			} else {
				for (int m = 0; m < m1; ++m) {
					interval -= DAYS[m];
				}
			}
			
			interval += d2;
			if (y2 % 400 == 0 || (y2 % 4 == 0 && y2 % 100 != 0)) {
				for (int m = 0; m < m2; ++m) {
					interval += LEEPYEARDAYS[m];
				}
			} else {
				for (int m = 0; m < m2; ++m) {
					interval += DAYS[m];
				}
			}
			
			return interval;
		} else if (date1 == date2) {
			return 0;
		} else {
			return -innerDayInterval(date2, date1);
		}
	}
	
	private static int innerYearInterval(int date1, int date2) {
		int y1, y2;
		if (date1 < 0) {
			y1 = date1 / 384 + 1969;
		} else {
			y1 = date1 / 384 + 1970;
		}
		
		if (date2 < 0) {
			y2 = date2 / 384 + 1969;
		} else {
			y2 = date2 / 384 + 1970;
		}
		
		return y2 - y1;
	}
	
	private static int innerMonthInterval(int date1, int date2) {
		int y1, m1, y2, m2;
		if (date1 < 0) {
			y1 = date1 / 384 + 1969;
			m1 = date1 / 32 % 12 + 11;
		} else {
			y1 = date1 / 384 + 1970;
			m1 = date1 / 32 % 12;
		}
		
		if (date2 < 0) {
			y2 = date2 / 384 + 1969;
			m2 = date2 / 32 % 12 + 11;
		} else {
			y2 = date2 / 384 + 1970;
			m2 = date2 / 32 % 12;
		}
		
		return (y2 - y1) * 12 + (m2 - m1);
	}
	
	private static long innerQuaterInterval(int date1, int date2) {
		int y1, m1, y2, m2;
		if (date1 < 0) {
			y1 = date1 / 384 + 1969;
			m1 = date1 / 32 % 12 + 11;
		} else {
			y1 = date1 / 384 + 1970;
			m1 = date1 / 32 % 12;
		}
		
		if (date2 < 0) {
			y2 = date2 / 384 + 1969;
			m2 = date2 / 32 % 12 + 11;
		} else {
			y2 = date2 / 384 + 1970;
			m2 = date2 / 32 % 12;
		}
		
		if (m1 < 3) {
			m1 = 1;
		} else if (m1 < 6) {
			m1 = 2;
		} else if (m1 < 9) {
			m1 = 3;
		} else {
			m1 = 4;
		}

		if (m2 < 3) {
			m2 = 1;
		} else if (m2 < 6) {
			m2 = 2;
		} else if (m2 < 9) {
			m2 = 3;
		} else {
			m2 = 4;
		}

		return (y2 - y1) * 4 + (m2 - m1);
	}
	
	/**
	 * �����������ڼ��������
	 * @param date1 Date
	 * @param date2 Date
	 * @return long ����
	 */
	public static long secondInterval(Date date1, Date date2) {
		return (date2.getTime() - date1.getTime()) / 1000;
	}

	/**
	 * �����������ڼ�����ٺ���
	 * @param date1 Date
	 * @param date2 Date
	 * @return long ������
	 */
	private static long millisecondInterval(Date date1, Date date2) {
		return date2.getTime() - date1.getTime();
	}

	/**
	 * �����������ڵĲ����ļ�ǰ���
	 * @param date1 Date
	 * @param date2 Date
	 * @param opt String y���꣬q������m���£�s���룬ms������
	 * @return long
	 */
	public static long interval(Date date1, Date date2, String opt) {
		if (opt == null) {
			return dayInterval(date1, date2);
		} else if (opt.indexOf("ymd") != -1) { // ���س�����yymmdd������
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date1);
			int y1 = calendar.get(Calendar.YEAR);
			int m1 = calendar.get(Calendar.MONTH) + 1;
			int d1 = calendar.get(Calendar.DAY_OF_MONTH);
			int maxDay1 = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			calendar.setTime(date2);
			int y2 = calendar.get(Calendar.YEAR);
			int m2 = calendar.get(Calendar.MONTH) + 1;
			int d2 = calendar.get(Calendar.DAY_OF_MONTH);
			int maxDay2 = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			int dayDiff;
			int monthDiff = 0;
			
			if (d2 >= d1) {
				if (d1 == maxDay1) {
					if (d2 == maxDay2) {
						dayDiff = 0;
					} else {
						dayDiff = d2;
						monthDiff = -1;
					}
				} else {
					dayDiff = d2 - d1;
				}
			} else {
				if (d2 == maxDay2) {
					dayDiff = 0;
				} else {
					//dayDiff = maxDay1 - d1 + d2;
					// ��������Ϊǰһ���µ�d1��
					calendar.set(Calendar.MONTH, m2 - 2);
					calendar.set(Calendar.DATE, d1);
					dayDiff = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - d1;
					if (dayDiff < 0) {
						dayDiff = d2;
					} else {
						dayDiff += d2;
					}
					
					monthDiff = -1;
				}
			}
			
			monthDiff += (m2 - m1);
			int yearDiff = y2 - y1;
			if (monthDiff < 0) {
				yearDiff -= 1;
				monthDiff += 12;
			}
			
			return yearDiff * 10000 + monthDiff * 100 + dayDiff;
		} else if (opt.indexOf("ym") != -1) { // ���س�����yymm������
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date1);
			int y1 = calendar.get(Calendar.YEAR);
			int m1 = calendar.get(Calendar.MONTH) + 1;
			int d1 = calendar.get(Calendar.DAY_OF_MONTH);
			int maxDay1 = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			calendar.setTime(date2);
			int y2 = calendar.get(Calendar.YEAR);
			int m2 = calendar.get(Calendar.MONTH) + 1;
			int d2 = calendar.get(Calendar.DAY_OF_MONTH);
			int maxDay2 = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			int monthDiff = 0;
			if (d2 >= d1) {
				if (d1 == maxDay1 && d2 != maxDay2) {
					monthDiff = -1;
				}
			} else {
				if (d2 != maxDay2) {
					monthDiff = -1;
				}
			}
			
			monthDiff += (m2 - m1);
			int yearDiff = y2 - y1;
			if (monthDiff < 0) {
				yearDiff -= 1;
				monthDiff += 12;
			}
			
			return yearDiff * 100 + monthDiff;
		} else if (opt.indexOf("md") != -1) { // ���س�����mmdd������
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date1);
			int y1 = calendar.get(Calendar.YEAR);
			int m1 = calendar.get(Calendar.MONTH) + 1;
			int d1 = calendar.get(Calendar.DAY_OF_MONTH);
			int maxDay1 = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			calendar.setTime(date2);
			int y2 = calendar.get(Calendar.YEAR);
			int m2 = calendar.get(Calendar.MONTH) + 1;
			int d2 = calendar.get(Calendar.DAY_OF_MONTH);
			int maxDay2 = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			int dayDiff;
			int monthDiff = 0;
			
			if (d2 >= d1) {
				if (d1 == maxDay1) {
					if (d2 == maxDay2) {
						dayDiff = 0;
					} else {
						dayDiff = d2;
						monthDiff = -1;
					}
				} else {
					dayDiff = d2 - d1;
				}
			} else {
				if (d2 == maxDay2) {
					dayDiff = 0;
				} else {
					//dayDiff = maxDay1 - d1 + d2;
					// ��������Ϊǰһ���µ�d1��
					calendar.set(Calendar.MONTH, m2 - 2);
					calendar.set(Calendar.DATE, d1);
					dayDiff = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - d1;
					if (dayDiff < 0) {
						dayDiff = d2;
					} else {
						dayDiff += d2;
					}
					
					monthDiff = -1;
				}
			}
			
			monthDiff += (m2 - m1) + (y2 - y1) * 12;			
			return monthDiff * 100 + dayDiff;
		} else if (opt.indexOf("ms") != -1) { // ����
			return millisecondInterval(date1, date2);
		} else if (opt.indexOf('y') != -1) { // ��
			return yearInterval(date1, date2);
		} else if (opt.indexOf('q') != -1) { // ��
			return quaterInterval(date1, date2);
		} else if (opt.indexOf('m') != -1) { // ��
			return monthInterval(date1, date2);
		} else if (opt.indexOf('s') != -1) { // ��
			return secondInterval(date1, date2);
		} else if (opt.indexOf('w') != -1) { // ��
			return weekInterval(date1, date2);
		} else if (opt.indexOf('7') != -1) { // ��
			return weekInterval_7(date1, date2);
		} else if (opt.indexOf('1') != -1) { // ��
			return weekInterval_1(date1, date2);
		} else {
			return dayInterval(date1, date2);
		}
	}

	/**
	 * �����������ڵĲ����ļ�ǰ���
	 * @param date1 date@o�������������
	 * @param date2 date@o�������������
	 * @param opt String y���꣬q������m���£�s���룬ms������
	 * @return long
	 */
	public static long interval(int date1, int date2, String opt) {
		if (opt == null) {
			return innerDayInterval(date1, date2);
		} else if (opt.indexOf('y') != -1) { // ��
			return innerYearInterval(date1, date2);
		} else if (opt.indexOf('q') != -1) { // ��
			return innerQuaterInterval(date1, date2);
		} else if (opt.indexOf('m') != -1) { // ��
			return innerMonthInterval(date1, date2);
		} else if (opt.indexOf('w') != -1) { // ��
			return weekInterval(DateFactory.toDate(date1), DateFactory.toDate(date2));
		} else if (opt.indexOf('7') != -1) { // ��
			return weekInterval_7(DateFactory.toDate(date1), DateFactory.toDate(date2));
		} else if (opt.indexOf('1') != -1) { // ��
			return weekInterval_1(DateFactory.toDate(date1), DateFactory.toDate(date2));
		} else {
			return innerDayInterval(date1, date2);
		}
	}
	
	/**
	 * �����������ڵľ�ȷ�����ļ�ǰ���
	 * @param date1 date@o�������������
	 * @param date2 date@o�������������
	 * @param opt String y���꣬q������m���£�s���룬ms������
	 * @return long
	 */
	public static double realInterval(int date1, int date2, String opt) {
		double dayDiff = innerDayInterval(date1, date2);
		
		if (opt == null) {
			return dayDiff;
		} else if (opt.indexOf('y') != -1) { // ��
			return dayDiff / 365;
		} else if (opt.indexOf('q') != -1) { // ��
			return dayDiff / 90;
		} else if (opt.indexOf('m') != -1) { // ��
			return dayDiff / 30;
		} else if (opt.indexOf('w') != -1) { // ��
			return dayDiff / 7;
		} else {
			return dayDiff;
		}
	}
	
	/**
	 * �����������ڵľ�ȷ�����ļ�ǰ���
	 * @param date1 Date
	 * @param date2 Date
	 * @param opt String y���꣬q������m���£�s���룬ms������
	 * @return long
	 */
	public static double realInterval(Date date1, Date date2, String opt) {
		if (opt == null) {
			double msDiff = millisecondInterval(date1, date2);
			return msDiff / 86400000;
		} else if (opt.indexOf("ms") != -1) { // ����
			return millisecondInterval(date1, date2);
		} else if (opt.indexOf('y') != -1) { // ��
			double dayDiff = dayInterval(date1, date2);
			return dayDiff / 365;
		} else if (opt.indexOf('q') != -1) { // ��
			double dayDiff = dayInterval(date1, date2);
			return dayDiff / 90;
		} else if (opt.indexOf('m') != -1) { // ��
			double dayDiff = dayInterval(date1, date2);
			return dayDiff / 30;
		} else if (opt.indexOf('s') != -1) { // ��
			double msDiff = millisecondInterval(date1, date2);
			return msDiff / 1000;
		} else if (opt.indexOf('w') != -1) { // ��
			double dayDiff = dayInterval(date1, date2);
			return dayDiff / 7;
		} else {
			double msDiff = millisecondInterval(date1, date2);
			return msDiff / 86400000;
		}
	}

	/**
	 * ��������ʱ���Ƿ���ȣ�Ĭ�ϱȽϵ���
	 * @param date1 Date
	 * @param date2 Date
	 * @param opt String y���꣬q������m���£�t��Ѯ��w����
	 * @return boolean
	 */
	public static boolean isEquals(Date date1, Date date2, String opt) {
		DateFactory df = DateFactory.get();
		if (df.year(date1) != df.year(date2)) return false;

		if (opt == null) {
			return df.month(date1) == df.month(date2) && df.day(date1) == df.day(date2);
		} else if (opt.indexOf('y') != -1) { // ��
			return true;
		} else if (opt.indexOf('q') != -1) { // ��
			int m1 = df.month(date1);
			int m2 = df.month(date2);
			if (m1 <= 3) {
				return m2 <= 3;
			} else if (m1 <= 6) {
				return m2 > 3 && m2 <= 6;
			} else if (m1 <= 9) {
				return m2 > 6 && m2 <= 9;
			} else {
				return m2 > 9;
			}
		} else if (opt.indexOf('m') != -1) { // ��
			return df.month(date1) == df.month(date2);
		} else if (opt.indexOf('t') != -1) { // Ѯ
			if (df.month(date1) != df.month(date2)) return false;
			int d1 = df.day(date1);
			int d2 = df.day(date2);
			if (d1 == d2) return true;

			if (d1 <= 10) {
				return d2 <= 10;
			} else if (d1 <= 20) {
				return d2 > 10 && d2 <= 20;
			} else {
				return d2 > 20;
			}
		} else if (opt.indexOf('w') != -1) { // ��
			int dayDiff = (int)dayInterval(date1, date2);
			if (dayDiff == 0) {
				return true;
			} else if (dayDiff > 6 || dayDiff < -6) {
				return false;
			}

			if (opt.indexOf('1') == -1) {
				// �������ܵĵ�һ��
				int week2 = df.week(date1) + dayDiff;
				return week2 >= Calendar.SUNDAY && week2 <= Calendar.SATURDAY;
			} else {
				int week = df.week(date1);
				if (week == Calendar.SUNDAY) {
					return dayDiff < 0 && dayDiff > -7;
				} else {
					week += dayDiff;
					return week >= Calendar.MONDAY && week <= Calendar.SATURDAY + 1;
				}
			}
		} else {
			return df.month(date1) == df.month(date2) && df.day(date1) == df.day(date2);
		}
	}

	/**
	 * �����������İ�λ��
	 * @param n1 Number
	 * @param n2 Number
	 * @return Number
	 */
	public static Number and(Number n1, Number n2) {
		if (n1 instanceof Integer && n2 instanceof Integer) {
			return n1.intValue() & n2.intValue();
		} else if (n1 instanceof BigInteger) {
			BigInteger b1 = (BigInteger)n1;
			BigInteger b2 = toBigInteger(n2);
			return b1.and(b2);
		} else if (n1 instanceof BigDecimal) {
			BigInteger b1 = ((BigDecimal)n1).toBigInteger();
			BigInteger b2 = toBigInteger(n2);
			return b1.and(b2);
		} else if (n2 instanceof BigInteger) {
			BigInteger b1 = toBigInteger(n1);
			BigInteger b2 = (BigInteger)n2;
			return b1.and(b2);
		} else if (n2 instanceof BigDecimal) {
			BigInteger b1 = toBigInteger(n1);
			BigInteger b2 = ((BigDecimal)n2).toBigInteger();
			return b1.and(b2);
		} else {
			return n1.longValue() & n2.longValue();
		}
	}
}
