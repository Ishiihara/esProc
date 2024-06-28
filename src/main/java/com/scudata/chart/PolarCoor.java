package com.scudata.chart;

import java.awt.geom.*;

import com.scudata.chart.element.*;

/**
 * ������ϵ
 * @author Joancy
 *
 */
public class PolarCoor implements ICoor {
	TickAxis a1, a2;

	/**
	 * ���ÿ̶���1
	 */
	public void setAxis1(TickAxis axis) {
		this.a1 = axis;
	}

	/**
	 * ��ȡ�̶���1
	 */
	public TickAxis getAxis1() {
		return a1;
	}

	/**
	 * ��ȡ����
	 * @return �������
	 */
	public TickAxis getPolarAxis() {
		if (a1.getLocation() == Consts.AXIS_LOC_POLAR) {
			return a1;
		}
		return a2;
	}

	/**
	 * ���ÿ̶���2
	 */
	public void setAxis2(TickAxis axis) {
		this.a2 = axis;
	}

	/**
	 * ��ȡ�̶���2
	 */
	public TickAxis getAxis2() {
		return a2;
	}

	/**
	 * ��ȡ����
	 * @return �������
	 */
	public TickAxis getAngleAxis() {
		if (a1.getLocation() == Consts.AXIS_LOC_ANGLE) {
			return a1;
		}
		return a2;
	}

	/**
	 * ���ؼ�����ϵ�µ���ֵ�㣻���� P.x = ���᳤�ȣ� P.y=����Ƕ�
	 * @param val1 �߼�����1
	 * @param val2 �߼�����2
	 * @return Point ��ֵ����
	 */
	public Point2D getPolarPoint(Object val1, Object val2) {
		return getNumericPoint(val1,val2);
	}
	public Point2D getNumericPoint(Object val1, Object val2) {
		double i1 = a1.getValueLen(val1);
		double i2 = a2.getValueLen(val2);
		double r,a;
		if (getPolarAxis() == a1) {
			r = i1;
			a = i2;
		}else{
			r = i2;
			a = i1;
		}
		return new Point2D.Double(r, a);
	}

	/**
	 * ���������ת��Ϊͼ����Ļ����λ��
	 * @param polarPoint �������
	 * @return ��Ļ���ؾ�������
	 */
	public Point2D getScreenPoint(Point2D numericPoint) {
		TickAxis polarAxis = (TickAxis) getPolarAxis();
		TickAxis angleAxis = (TickAxis) getAngleAxis();
		double r,a;
		r = numericPoint.getX();
		a = numericPoint.getY()+angleAxis.getBottomY();
		double radAngle = a * Math.PI / 180;
		double x = polarAxis.getLeftX() + r * Math.cos(radAngle);
		double y = polarAxis.getBottomY() - r * Math.sin(radAngle);
		return new Point2D.Double(x, y);
	}

	/**
	 * ֱ�Ӹ����߼�����ֵ���������Ļ���ؾ�������
	 * @param �߼�����1
	 * @param �߼�����2
	 * @return ��Ļ���ؾ�������
	 */
	public Point2D getScreenPoint(Object val1, Object val2) {
		Point2D nDot = getNumericPoint(val1, val2);
		return getScreenPoint(nDot);
	}

	/**
	 * ��ȡ���ԭ�㼫�᳤ΪpolarLen����Բ�߽�
	 * @param polarLen ���᳤��
	 * @return Double �߽����
	 */
	public Rectangle2D getEllipseBounds(double polarLen) {
		TickAxis polarAxis = (TickAxis) getPolarAxis();
		double x = polarAxis.getLeftX() - polarLen;
		double y = polarAxis.getBottomY() - polarLen;
		double w = polarLen * 2;
		double h = w;
		Rectangle2D ellipseBounds = new Rectangle2D.Double(x, y, w, h);
		return ellipseBounds;
	}

	/**
	 * �ж���������ϵ�Ƿ����
	 * @param ��һ������ϵ
	 * @return ��ȷ���true�� ���򷵻�false
	 */
	public boolean equals(Object another) {
		if (another instanceof PolarCoor) {
			PolarCoor apc = (PolarCoor) another;
			return apc.getPolarAxis() == getPolarAxis()
					&& apc.getAngleAxis() == getAngleAxis();
		}
		return false;
	}

	/**
	 * �÷�ͬCartesianCoor
	 */
	public NumericAxis getNumericAxis() {
		TickAxis axis = CartesianCoor.getAxis(this, NumericAxis.class);
		if (axis != null)
			return (NumericAxis) axis;
		return null;
	}

	/**
	 * �÷�ͬCartesianCoor
	 */
	public EnumAxis getEnumAxis() {
		TickAxis axis = CartesianCoor.getAxis(this, EnumAxis.class);
		if (axis != null)
			return (EnumAxis) axis;
		return null;
	}

	/**
	 * ����ϵ��������Ϣ
	 */
	public String toString() {
		return "PolarCoor Axis1:" + a1.getName() + " Axis2:" + a2.getName();
	}

	/**
	 * �Ƿ�Ϊ������ϵ
	 * @return true
	 */
	public boolean isPolarCoor() {
		return true;
	}

	/**
	 * �Ƿ�Ϊֱ������ϵ
	 * @return false
	 */
	public boolean isCartesianCoor() {
		return false;
	}

	/**
	 * �����1����ö���ᣬ�����ǻ���ö����
	 */
	public boolean isEnumBased() {
			TickAxis ta = getAxis1();
			return ta.isEnumAxis();
	}
}
