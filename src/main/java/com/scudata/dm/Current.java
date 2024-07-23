package com.scudata.dm;

/**
 * ���ڼ������к���ʱ����ѹջ
 * @author WangXiaoJun
 *
 */
public class Current implements IComputeItem {
	private Sequence sequence;
	private int curIndex; // ��ǰ�������ڽ��м����Ԫ�ص���������1��ʼ����
	private boolean isInStack = true; // �Ƿ��ڼ����ջ��

	public Current(Sequence sequence) {
		this.sequence = sequence;
	}
	
	public Current(Sequence sequence, int index) {
		this.sequence = sequence;
		curIndex = index;
	}

	/**
	 * ���ص�ǰ���ڼ����Ԫ��
	 * @return Object
	 */
	public Object getCurrent() {
		return sequence.getCurrent(curIndex);
	}

	/**
	 * ���ص�ǰ���ڼ����Ԫ����������1��ʼ����
	 * @return int
	 */
	public int getCurrentIndex() {
		return curIndex;
	}
	
	/**
	 * ȡԴ����
	 */
	public Sequence getCurrentSequence() {
		return sequence;
	}
	
	/**
	 * �ж������Ƿ��ڶ�ջ��
	 */
	public boolean isInStack(ComputeStack stack) {
		return isInStack;
	}
	
	/**
	 * ������ɣ����г�ջ
	 */
	public void popStack() {
		isInStack = false;
	}
	
	/**
	 * �жϵ�ǰ�����Ƿ�͸���������ͬһ������
	 * @param seq
	 * @return
	 */
	public boolean equalSequence(Sequence seq) {
		return sequence == seq;
	}

	/**
	 * ȡ���еĳ���
	 * @return
	 */
	public int length() {
		return sequence.length();
	}

	/**
	 * �����ȡ���еĳ�Ա
	 * @param i ��ţ���1��ʼ����
	 * @return
	 */
	public Object get(int i) {
		return sequence.get(i);
	}

	/**
	 * ���õ�ǰ���ڼ����Ԫ������
	 * @param index int ��1��ʼ����
	 */
	public void setCurrent(int index) {
		this.curIndex = index;
	}

	/**
	 * �޸����еĵ�ǰԪ��Ϊָ��ֵ
	 * @param val
	 */
	public void assign(Object val) {
		sequence.set(curIndex, val);
	}

	/**
	 * �޸�����ָ��λ�õ�Ԫ��
	 * @param index ��ţ���1��ʼ����
	 * @param val
	 */
	public void assign(int index, Object val) {
		sequence.set(index, val);
	}
	
	/**
	 * ȡ��ǰ��¼���ֶ�ֵ
	 * @param field
	 * @return
	 */
	public Object getFieldValue2(int field) {
		return sequence.getFieldValue2(curIndex, field);
	}
}
