package com.scudata.expression;

import java.util.ArrayList;
import java.util.List;

import com.scudata.cellset.INormalCell;
import com.scudata.dm.*;

/**
 * ���������ӿ�
 * �����������ö�����ṹ�洢
 * @author RunQian
 *
 */
public interface IParam {
	static final char NONE = 0; // ��û�в����ڵ㣬���ڲ�����������
	
	// �����ڵ������
	public static final char Semicolon = ';'; // �����ֺŷָ���
	public static final char Comma = ',';     // �������ŷָ���
	public static final char Colon = ':';     // ����ð�ŷָ���
	public static final char Normal = 0;      // Ҷ�ӽڵ�

	/**
	 * ���ؽڵ������
	 * @return char Normal��Semicolon��Comma��Colon
	 */
	char getType();

	/**
	 * �����Ƿ���Ҷ�ӽڵ�
	 * @return boolean true���ǣ�false������
	 */
	boolean isLeaf();

	/**
	 * �����ӽڵ�����Ҷ�ӽڵ㷵��0
	 * @return int �ӽڵ���
	 */
	int getSubSize();

	/**
	 * ����ĳһ�ӽڵ�
	 * @param index ��ţ���0��ʼ����
	 * @return IParam �ӽڵ�
	 */
	IParam getSub(int index);

	/**
	 * ���ص�ǰ�ڵ�ı��ʽ�ַ�������ǰ�ڵ�ΪҶ�ӽڵ�
	 * @return Expression
	 */
	Expression getLeafExpression();

	/**
	 * ȡ����Ҷ�ӽڵ�ı��ʽ
	 * @param List ���ֵ�����ڴ�ű��ʽ
	 */
	void getAllLeafExpression(ArrayList<Expression> list);

	/**
	 * ���ر��ʽ���飬ֻ֧�ֵ���Ĳ���
	 * @param function �������������׳��쳣
	 * @param canNull �����Ƿ�ɿ�
	 * @return ���ʽ����
	 */
	Expression[] toArray(String function, boolean canNull);
	
	/**
	 * ���ر��ʽ�ַ������飬ֻ֧�ֵ���Ĳ���
	 * @param function �������������׳��쳣
	 * @param canNull �����Ƿ�ɿ�
	 * @return ���ʽ������
	 */
	String []toStringArray(String function, boolean canNull);
	
	/**
	 * �����ֶ�������
	 * @param function �������������׳��쳣
	 * @return �ֶ�������
	 */
	String []toIdentifierNames(String function);
	
	/**
	 * �����Ƿ����ָ������
	 * @param name ������
	 * @return boolean true��������false��������
	 */
	boolean containParam(String name);

	/**
	 * ���ұ��ʽ���õ�����
	 * @param ctx ����������
	 * @param resultList ���ֵ���õ��Ĳ�������ӵ�������
	 */
	void getUsedParams(Context ctx, ParamList resultList);
	
	/**
	 * ���ұ��ʽ�п����õ����ֶΣ�����ȡ�ò�׼ȷ���߰���������
	 * @param ctx ����������
	 * @param resultList ���ֵ���õ����ֶ�������ӵ�������
	 */
	void getUsedFields(Context ctx, List<String> resultList);

	/**
	 * ���ұ��ʽ���õ���Ԫ��
	 * @param resultList ���ֵ���õ��ĵ�Ԫ�����ӵ�������
	 */
	void getUsedCells(List<INormalCell> resultList);

	/**
	 * �Ż��������ʽ���������в����Ƿ��ǳ���
	 * @param ctx ����������
	 * @return boolean true���ǣ�false������
	 */
	boolean optimize(Context ctx);

	/**
	 * ��start��end�ӽڵ㴴��һ���²���
	 * @param start ��ʼλ�ã�����
	 * @param end ����λ�ã�������
	 * @return IParam �²���
	 */
	IParam create(int start, int end);

	/**
	 * �ж��Ƿ���Լ���ȫ����ֵ���и�ֵ����ʱֻ��һ���м���
	 * @return
	 */
	boolean canCalculateAll();
	
	/**
	 * ���ñ��ʽ�����ڱ��ʽ���棬���ִ��ʹ�ò�ͬ�������ģ�������������йصĻ�����Ϣ
	 */
	void reset();
}
