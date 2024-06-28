package com.scudata.chart;

import java.awt.geom.Point2D;

import com.scudata.chart.element.*;

/**
 * ����һ�����ڻ�ͼ���߼�ֱ������ϵ
 * 
 * @author Joancy
 *
 */
public class CartesianCoor implements ICoor {
	// ��������ϵ�������̶��ᣬ�����᱾������ỹ�����ᣬ��������ϵ�в������������ᣬֻҪ��ϵ�������ֱ��к������ἴ��
	TickAxis a1, a2;

	/**
	 * ���õ�һ���̶���
	 * @param axis	�̶���
	 */
	public void setAxis1(TickAxis axis) {
		this.a1 = axis;
	}

	/**
	 * ��ȡ��һ���̶���
	 * @return �̶���1
	 */
	public TickAxis getAxis1() {
		return a1;
	}

	/**
	 * ��ȡx�ᣬҲ������
	 * @return ����ϵ��x��
	 */
	public TickAxis getXAxis() {
		if (a1.getLocation() == Consts.AXIS_LOC_H) {
			return a1;
		}
		return a2;
	}

	/**
	 * ���õڶ����̶���
	 * @param axis	�̶���
	 */
	public void setAxis2(TickAxis axis) {
		this.a2 = axis;
	}

	/**
	 * ��ȡ�ڶ����̶���
	 * @return �̶���2
	 */
	public TickAxis getAxis2() {
		return a2;
	}

	/**
	 * ��ȡy�ᣬҲ������
	 * @return ����ϵ��y��
	 */
	public TickAxis getYAxis() {
		if (a1.getLocation() == Consts.AXIS_LOC_V) {
			return a1;
		}
		return a2;
	}

	/**
	 * �������Ӧ��������߼�ֵ����������
	 * @param val1	��Ӧ1����߼�ֵ
	 * @param val2	��Ӧ2����߼�ֵ
	 * @return Point2D ����Ϊdouble����������
	 */
	public Point2D getNumericPoint(Object val1, Object val2) {
		double i1 = a1.getValueLen(val1);
		double i2 = a2.getValueLen(val2);
		return new Point2D.Double(i1, i2);
	}

	public Point2D getScreenPoint(Point2D numericPoint) {
		double i1 = numericPoint.getX();
		double i2 = numericPoint.getY();
		double x, y;
		if (getXAxis() == a1) {
			x = a1.getLeftX() + i1;
			y = a2.getBottomY() - i2;
		} else {
			x = a2.getLeftX() + i2;
			y = a1.getBottomY() - i1;
		}
		return new Point2D.Double(x, y);
	}
	 public Point2D getScreenPoint(Object val1, Object val2) {
		  return getScreenPoint(getNumericPoint(val1,val2));
	  };

	/**
	 * 3dƫ��������,�����ö���ᣬ��ȡö�����thickRate������ȡaxis1�ĸ�ֵ��
	 * 
	 * @return int 
	 */
	public int get3DShift() {
		boolean is3D = ((TickAxis) a1).is3D || ((TickAxis) a2).is3D;
		if (!is3D)
			return 0;
		int maxShift = 60;
		int shift;
		EnumAxis ea = getEnumAxis();
		if (ea == null) {
			shift = (int) ((TickAxis) a1).getEngine().getYPixel(
					((TickAxis) a1).threeDThickRatio);
		} else {
			shift = (int) (ea.getSeriesWidth() * ea.threeDThickRatio);
		}
		return Math.min(shift, maxShift);
	}

	protected static TickAxis getAxis(ICoor coor, Class axisType) {
		TickAxis axis = coor.getAxis1();
		if (axisType.isInstance(axis)) {
			return axis;
		}
		axis = coor.getAxis2();
		if (axisType.isInstance(axis)) {
			return axis;
		}
		return null;
	}

	/**
	 * ��ȡ����ϵ�е���ֵ�ᣬ�÷���ͨ������ö�������ֵ����ϵ�����ϵʱ����������ᶼ����ֵ�ᣬ��ֻ����1�ᡣ
	 * 
	 * @return ��ֵ��
	 */
	public NumericAxis getNumericAxis() {
		TickAxis axis = getAxis(this, NumericAxis.class);
		if (axis != null)
			return (NumericAxis) axis;
		return null;
	}

	/**
	 * ��ȡ����ϵ�е�ö���ᣬ�÷�ͬgetNumericAxis
	 * 
	 * @return ö����
	 */
	public EnumAxis getEnumAxis() {
		TickAxis axis = getAxis(this, EnumAxis.class);
		if (axis != null)
			return (EnumAxis) axis;
		return null;
	}

	/**
	 * ��������ϵ��������Ϣ��ͨ�����ڵ��ԡ�
	 */
	public String toString() {
		return "CartesianCoor Axis1:" + a1.getName() + " Axis2:" + a2.getName();
	}

	/**
	 * �ж���������ϵ�Ƿ����
	 * @return ���ʱ����true������false
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof CartesianCoor) {
			CartesianCoor cc = (CartesianCoor) obj;
			return cc.a1.equals(a1) && cc.a2.equals(a2);
		}
		return false;
	}

	/**
	 * �Ƿ�Ϊ������ϵ
	 */
	public boolean isPolarCoor() {
		return false;
	}

	/**
	 * �Ƿ�Ϊֱ������ϵ
	 */
	public boolean isCartesianCoor() {
		return true;
	}

	/**
	 * �Ƿ�Ϊ����ö���������ϵ
	 */
	public boolean isEnumBased() {
		TickAxis ta = getAxis1();
		return ta.isEnumAxis();
	}
}
