package com.scudata.array;

import java.io.Externalizable;
import java.util.Comparator;

import com.scudata.common.IRecord;

/**
 * ����ӿڣ���1��ʼ����
 * @author WangXiaoJun
 *
 */
public interface IArray extends Externalizable, IRecord, Comparable<IArray> {
	static final int DEFAULT_LEN = 8; // Ĭ�Ϲ��캯�������������Ĭ�ϳ���
	
	/**
	 * ȡ��������ʹ������ڴ�����Ϣ��ʾ
	 * @return ���ʹ�
	 */
	String getDataType();
	
	/**
	 * ׷��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	void add(Object o);
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	void addAll(Object[] array);
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 */
	void addAll(IArray array);
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 * @param count Ԫ�ظ���
	 */
	void addAll(IArray array, int count);
	
	/**
	 * ׷��һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param array Ԫ������
	 * @param index Ҫ��������ݵ���ʼλ��
	 * @param count ����
	 */
	void addAll(IArray array, int index, int count);
	
	/**
	 * ����Ԫ�أ�������Ͳ��������׳��쳣
	 * @param index ����λ�ã���1��ʼ����
	 * @param o Ԫ��ֵ
	 */
	void insert(int index, Object o);
	
	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	void insertAll(int pos, IArray array);
	
	/**
	 * ��ָ��λ�ò���һ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param pos λ�ã���1��ʼ����
	 * @param array Ԫ������
	 */
	void insertAll(int pos, Object []array);
	
	/**
	 * ׷��Ԫ�أ��������������Ϊ���㹻�ռ���Ԫ�أ���������Ͳ��������׳��쳣
	 * @param o Ԫ��ֵ
	 */
	void push(Object o);
	
	/**
	 * ׷��һ���ճ�Ա���������������Ϊ���㹻�ռ���Ԫ�أ�
	 */
	void pushNull();
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	void push(IArray array, int index);
	
	/**
	 * ��array�еĵ�index��Ԫ����ӵ���ǰ�����У�������Ͳ��������׳��쳣
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	void add(IArray array, int index);
	
	/**
	 * ��array�еĵ�index��Ԫ���������ǰ�����ָ��Ԫ�أ�������Ͳ��������׳��쳣
	 * @param curIndex ��ǰ�����Ԫ����������1��ʼ����
	 * @param array ����
	 * @param index Ԫ����������1��ʼ����
	 */
	void set(int curIndex, IArray array, int index);
	
	/**
	 * ȡָ��λ��Ԫ��
	 * @param index ��������1��ʼ����
	 * @return
	 */
	Object get(int index);
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param indexArray λ������
	 * @return IArray
	 */
	IArray get(int []indexArray);
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param indexArray λ������
	 * @param start ��ʼλ�ã�����
	 * @param end ����λ�ã�����
	 * @param doCheck true��λ�ÿ��ܰ���0��0��λ����null��䣬false���������0
	 * @return IArray
	 */
	IArray get(int []indexArray, int start, int end, boolean doCheck);
	
	/**
	 * ȡĳһ�������������
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã���������
	 * @return IArray
	 */
	IArray get(int start, int end);
	
	/**
	 * ȡָ��λ��Ԫ�����������
	 * @param IArray λ������
	 * @return IArray
	 */
	IArray get(IArray indexArray);
	
	/**
	 * ȡָ��λ��Ԫ�ص�����ֵ
	 * @param index ��������1��ʼ����
	 * @return
	 */
	int getInt(int index);
	
	/**
	 * ȡָ��λ��Ԫ�صĳ�����ֵ
	 * @param index ��������1��ʼ����
	 * @return
	 */
	long getLong(int index);

	/**
	 * ʹ�б��������С��minCapacity
	 * @param minCapacity ��С����
	 */
	void ensureCapacity(int minCapacity);
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ��ǿ�
	 * @param index ��������1��ʼ����
	 * @return
	 */
	boolean isNull(int index);
	
	/**
	 * �ж�Ԫ���Ƿ�����
	 * @return BoolArray
	 */
	BoolArray isTrue();
	
	/**
	 * �ж�Ԫ���Ƿ��Ǽ�
	 * @return BoolArray
	 */
	BoolArray isFalse();
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ���True
	 * @param index ��������1��ʼ����
	 * @return
	 */
	boolean isTrue(int index);
	
	/**
	 * �ж�ָ��λ�õ�Ԫ���Ƿ���False
	 * @param index ��������1��ʼ����
	 * @return
	 */
	boolean isFalse(int index);
	
	/**
	 * �Ƿ��Ǽ����������ʱ���������飬��ʱ�����Ŀ��Ա��޸ģ����� f1+f2+f3��ֻ�����һ�������Ž��
	 * @return true������ʱ���������飬false��������ʱ����������
	 */
	boolean isTemporary();
	
	/**
	 * �����Ƿ��Ǽ����������ʱ����������
	 * @param ifTemporary true������ʱ���������飬false��������ʱ����������
	 */
	void setTemporary(boolean ifTemporary);
	
	/**
	 * ɾ�����һ��Ԫ��
	 */
	void removeLast();
	
	/**
	 * ɾ��ָ��λ�õ�Ԫ��
	 * @param index ��������1��ʼ����
	 * @return Object ��ɾ����Ԫ�ص�ֵ
	 */
	void remove(int index);
	
	/**
	 * ɾ��ָ��λ�õ�Ԫ��
	 * @param seqs ��������
	 */
	void remove(int []seqs);
	
	/**
	 * ɾ��ָ�������ڵ�Ԫ��
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 */
	void removeRange(int fromIndex, int toIndex);
	
	/**
	 * ���������Ԫ����Ŀ
	 * @return int
	 */
	int size();
	
	/**
	 * ���������Ԫ����Ŀ
	 * @param int
	 */
	void setSize(int size);
	
	/**
	 * �������鲼���ж�ȡֵΪ���Ԫ����Ŀ
	 * @return �ǿ�Ԫ����Ŀ
	 */
	int count();
	
	/**
	 * �ж������Ƿ���ȡֵΪtrue��Ԫ��
	 * @return true���У�false��û��
	 */
	boolean containTrue();
	
	/**
	 * ���ص�һ����Ϊ�յ�Ԫ��
	 * @return Object
	 */
	Object ifn();
	
	/**
	 * �޸�����ָ��Ԫ�ص�ֵ��������Ͳ��������׳��쳣
	 * @param index ��������1��ʼ����
	 * @param obj ֵ
	 */
	void set(int index, Object obj);
	
	/**
	 * ɾ�����е�Ԫ��
	 */
	void clear();
	
	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @return Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	int binarySearch(Object elem);
	
	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @param start ��ʼ����λ�ã�������
	 * @param end ��������λ�ã�������
	 * @return Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	int binarySearch(Object elem, int start, int end);
	
	/**
	 * �����б����Ƿ����ָ��Ԫ��
	 * @param elem Object �����ҵ�Ԫ��
	 * @return boolean true��������false��������
	 */
	boolean contains(Object elem);
	
	/**
	 * �ж������Ԫ���Ƿ��ڵ�ǰ������
	 * @param isSorted ��ǰ�����Ƿ�����
	 * @param array ����
	 * @param result ���ڴ�Ž����ֻ��ȡֵΪtrue��
	 */
	void contains(boolean isSorted, IArray array, BoolArray result);
	
	/**
	 * �����б����Ƿ����ָ��Ԫ�أ�ʹ�õȺűȽ�
	 * @param elem
	 * @return boolean true��������false��������
	 */
	boolean objectContains(Object elem);

	/**
	 * ����Ԫ�����������״γ��ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start ��ʼ����λ�ã�������
	 * @return ���Ԫ�ش����򷵻�ֵ����0�����򷵻�0
	 */
	int firstIndexOf(Object elem, int start);
	
	/**
	 * ����Ԫ���������������ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start �Ӻ��濪ʼ���ҵ�λ�ã�������
	 * @return ���Ԫ�ش����򷵻�ֵ����0�����򷵻�0
	 */
	int lastIndexOf(Object elem, int start);

	/**
	 * ����Ԫ�������������г��ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @param start ��ʼ����λ�ã�������
	 * @param isSorted ��ǰ�����Ƿ�����
	 * @param isFromHead true����ͷ��ʼ������false����β��ǰ��ʼ����
	 * @return IntArray
	 */
	IntArray indexOfAll(Object elem, int start, boolean isSorted, boolean isFromHead);
	
	/**
	 * ��������
	 * @return
	 */
	IArray dup();
	
	/**
	 * ����һ��ͬ���͵�����
	 * @param count
	 * @return
	 */
	IArray newInstance(int count);
	
	/**
	 * �������Ա�����ֵ
	 * @return IArray ����ֵ����
	 */
	IArray abs();
	
	/**
	 * �������Ա��
	 * @return IArray ��ֵ����
	 */
	IArray negate();
	
	/**
	 * �������Ա���
	 * @return IArray ��ֵ����
	 */
	IArray not();
	
	/**
	 * �ж�����ĳ�Ա�Ƿ����������԰���null��
	 * @return true����������false�����з�����ֵ
	 */
	boolean isNumberArray();
	
	/**
	 * ����������������Ӧ�ĳ�Ա�ĺ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	IArray memberAdd(IArray array);
	
	/**
	 * ��������ĳ�Ա��ָ�������ĺ�
	 * @param value ����
	 * @return ������
	 */
	IArray memberAdd(Object value);
	
	/**
	 * ����������������Ӧ�ĳ�Ա�Ĳ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	IArray memberSubtract(IArray array);
	
	/**
	 * ����������������Ӧ�ĳ�Ա�Ļ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	IArray memberMultiply(IArray array);

	/**
	 * ��������ĳ�Ա��ָ�������Ļ�
	 * @param value ����
	 * @return ������
	 */
	IArray memberMultiply(Object value);

	/**
	 * ����������������Ӧ�ĳ�Ա�ĳ�
	 * @param array �Ҳ�����
	 * @return ������
	 */
	IArray memberDivide(IArray array);
	
	/**
	 * ����������������Ӧ������Աȡ������г�Ա�����
	 * @param array �Ҳ�����
	 * @return ����������������������
	 */
	IArray memberMod(IArray array);
	
	/**
	 * �����������������Ա���������г�Ա�
	 * @param array �Ҳ�����
	 * @return ����ֵ��������в����
	 */
	IArray memberIntDivide(IArray array);

	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @return ��ϵ����������
	 */
	BoolArray calcRelation(IArray array, int relation);
	
	/**
	 * ��������ĳ�Ա��ָ��ֵ�Ĺ�ϵ����
	 * @param value �Ҳ�ֵ
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @return ��ϵ����������
	 */
	BoolArray calcRelation(Object value, int relation);
	
	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ���㣬ֻ����resultΪ�����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	void calcRelations(IArray array, int relation, BoolArray result, boolean isAnd);
	
	
	/**
	 * ����������������Ӧ�ĳ�Ա�Ĺ�ϵ���㣬ֻ����resultΪ�����
	 * @param array �Ҳ�����
	 * @param relation �����ϵ������Relation�����ڡ�С�ڡ����ڡ�...��
	 * @param result ������������ǰ��ϵ��������Ҫ����������߼�&&����||����
	 * @param isAnd true��������� && ���㣬false��������� || ����
	 */
	void calcRelations(Object value, int relation, BoolArray result, boolean isAnd);
	
	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ��
	 * @param array �Ҳ�����
	 * @return ��λ��������
	 */
	IArray bitwiseAnd(IArray array);
	
	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ��
	 * @param array �Ҳ�����
	 * @return ��λ��������
	 */
	IArray bitwiseOr(IArray array);
	
	/**
	 * ����������������Ӧ�ĳ�Ա�İ�λ���
	 * @param array �Ҳ�����
	 * @return ��λ���������
	 */
	IArray bitwiseXOr(IArray array);
	
	/**
	 * ���������Ա�İ�λȡ��
	 * @return ��Ա��λȡ���������
	 */
	IArray bitwiseNot();
	
	/**
	 * ���������2����Ա�ıȽ�ֵ
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	int memberCompare(int index1, int index2);
	
	/**
	 * �ж������������Ա�Ƿ����
	 * @param index1 ��Ա1
	 * @param index2 ��Ա2
	 * @return
	 */
	boolean isMemberEquals(int index1, int index2);

	/**
	 * �Ƚ���������Ĵ�С
	 * @param array �Ҳ�����
	 * @return 1����ǰ�����0������������ȣ�-1����ǰ����С
	 */
	int compareTo(IArray array);

	/**
	 * �Ƚ���������Ĵ�С
	 * @param array �Ҳ�����
	 * @param comparator �Ƚ�������������ȿ��õ�
	 * @return 1����ǰ�����0������������ȣ�-1����ǰ����С
	 */
	//int compareTo(IArray array, Comparator<Object> comparator);
	
	/**
	 * ȡָ����Ա�Ĺ�ϣֵ
	 * @param index ��Ա��������1��ʼ����
	 * @return ָ����Ա�Ĺ�ϣֵ
	 */
	int hashCode(int index);
	
	/**
	 * ���Ա��
	 * @return
	 */
	Object sum();

	/**
	 * ��ƽ��ֵ
	 * @return
	 */
	Object average();
	
	/**
	 * �õ����ĳ�Ա
	 * @return
	 */
	Object max();
	
	/**
	 * �õ���С�ĳ�Ա
	 * @return
	 */
	Object min();
	
	/**
	 * ����ָ�������ڵ�����
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã�������
	 */
	void reserve(int start, int end);
	
	/**
	 * �ѳ�Աת�ɶ������鷵��
	 * @return ��������
	 */
	Object[] toArray();
	
	/**
	 * �ѳ�Ա�ָ��������
	 * @param result ���ڴ�ų�Ա������
	 */
	void toArray(Object []result);
	
	/**
	 * �������ָ��λ�ò����������
	 * @param pos λ�ã�����
	 * @return ���غ�벿��Ԫ�ع��ɵ�����
	 */
	IArray split(int pos);
	
	/**
	 * ��ָ������Ԫ�ط���������������
	 * @param from ��ʼλ�ã�����
	 * @param to ����λ�ã�����
	 * @return
	 */
	IArray split(int from, int to);
	
	/**
	 * ����������ʹ����Ԫ�������
	 */
	void trimToSize();
	
	/**
	 * ȡ����ʶ����ȡֵΪ����ж�Ӧ�����ݣ����������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	IArray select(IArray signArray);
	
	/**
	 * ȡĳһ���α�ʶ����ȡֵΪ��������������
	 * @param start ��ʼλ�ã�������
	 * @param end ����λ�ã���������
	 * @param signArray ��ʶ����
	 * @return IArray
	 */
	IArray select(int start, int end, IArray signArray);
	
	/**
	 * �ж����������ָ��Ԫ���Ƿ���ͬ
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ�Ƚϵ�����
	 * @param index Ҫ�Ƚϵ������Ԫ�ص�����
	 * @return true����ͬ��false������ͬ
	 */
	boolean isEquals(int curIndex, IArray array, int index);
	
	/**
	 * �ж������ָ��Ԫ���Ƿ������ֵ���
	 * @param curIndex ����Ԫ����������1��ʼ����
	 * @param value ֵ
	 * @return true����ȣ�false�������
	 */
	boolean isEquals(int curIndex, Object value);
	
	/**
	 * �Ƚ����������ָ��Ԫ�صĴ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ�Ƚϵ�����
	 * @param index Ҫ�Ƚϵ������Ԫ�ص�����
	 * @return С�ڣ�С��0�����ڣ�0�����ڣ�����0
	 */
	int compareTo(int curIndex, IArray array, int index);

	/**
	 * �Ƚ������ָ��Ԫ�������ֵ�Ĵ�С
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param value Ҫ�Ƚϵ�ֵ
	 * @return
	 */
	int compareTo(int curIndex, Object value);
	
	/**
	 * ��array��ָ��Ԫ�ؼӵ���ǰ�����ָ��Ԫ����
	 * @param curIndex ��ǰ�����Ԫ�ص�����
	 * @param array Ҫ��ӵ�����
	 * @param index Ҫ��ӵ������Ԫ�ص�����
	 * @return IArray
	 */
	IArray memberAdd(int curIndex, IArray array, int index);
	
	/**
	 * �������Ԫ�ؽ�������
	 */
	void sort();
	
	/**
	 * �������Ԫ�ؽ�������
	 * @param comparator �Ƚ���
	 */
	void sort(Comparator<Object> comparator);
	
	/**
	 * �����������Ƿ��м�¼
	 * @return boolean
	 */
	boolean hasRecord();
	
	/**
	 * �����Ƿ��ǣ���������
	 * @param isPure true������Ƿ��Ǵ�����
	 * @return boolean true���ǣ�false������
	 */
	boolean isPmt(boolean isPure);
	
	/**
	 * ��������ķ�ת����
	 * @return IArray
	 */
	IArray rvs();
	
	/**
	 * ������Ԫ�ش�С����������ȡǰcount����λ��
	 * @param count ���countС��0��ȡ��|count|����λ��
	 * @param isAll countΪ����1ʱ�����isAllȡֵΪtrue��ȡ����������һ��Ԫ�ص�λ�ã�����ֻȡһ��
	 * @param isLast �Ƿ�Ӻ�ʼ��
	 * @param ignoreNull �Ƿ���Կ�Ԫ��
	 * @return IntArray
	 */
	IntArray ptop(int count, boolean isAll, boolean isLast, boolean ignoreNull);
	
	/**
	 * ������Ԫ�ش�С������������ȡǰcount����λ��
	 * @param count ���countС��0��Ӵ�С������
	 * @param ignoreNull �Ƿ���Կ�Ԫ��
	 * @param iopt �Ƿ�ȥ�ط�ʽ������
	 * @return IntArray
	 */
	IntArray ptopRank(int count, boolean ignoreNull, boolean iopt);
	
	/**
	 * �ѵ�ǰ����ת�ɶ������飬�����ǰ�����Ƕ��������򷵻����鱾��
	 * @return ObjectArray
	 */
	ObjectArray toObjectArray();
	
	/**
	 * �Ѷ�������ת�ɴ��������飬����ת���׳��쳣
	 * @return IArray
	 */
	IArray toPureArray();
	
	/**
	 * �����������������������л����
	 * @param refOrigin ����Դ�У�����������
	 * @return
	 */
	IArray reserve(boolean refOrigin);
	
	/**
	 * ������������������ѡ����Ա��������飬�ӵ�ǰ����ѡ����־Ϊtrue�ģ���other����ѡ����־Ϊfalse��
	 * @param signArray ��־����
	 * @param other ��һ������
	 * @return IArray
	 */
	IArray combine(IArray signArray, IArray other);

	/**
	 * ���������ӵ�ǰ����ѡ����־Ϊtrue�ģ���־Ϊfalse���ó�value
	 * @param signArray ��־����
	 * @param other ֵ
	 * @return IArray
	 */
	IArray combine(IArray signArray, Object value);
	
	/**
	 * ����ָ������ĳ�Ա�ڵ�ǰ�����е�λ��
	 * @param array �����ҵ�����
	 * @param opt ѡ�b��ͬ��鲢�����ң�i�����ص��������У�c����������
	 * @return λ�û���λ������
	 */
	Object pos(IArray array, String opt);
	
	/**
	 * ���������Ա�Ķ����Ʊ�ʾʱ1�ĸ�����
	 * @return
	 */
	int bit1();
	
	/**
	 * ���������Ա��λ���ֵ�Ķ����Ʊ�ʾʱ1�ĸ�����
	 * @param array �������
	 * @return 1�ĸ�����
	 */
	int bit1(IArray array);
}
