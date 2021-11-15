package com.raqsoft.cellset.graph.draw;

import java.awt.*;

import com.raqsoft.chart.Consts;

/**
 * ��װ����״����ɫ�����Ե����ݵ�
 * �������ı���ǩ�����ݵ�����һ��Ƚ�С��Ҳ��Ҫ�ں���ͳһ����
 * ��ֹ������
 * @author Joancy
 *
 */
public class ValuePoint {
	public Point p;
	public Color borderColor,fillColor=null;
	public int shape = Consts.PT_CIRCLE;
	public int radius = -1;

	/**
	 * ����һ�����ݵ�
	 * @param p ����
	 * @param bc �߿���ɫ
	 */
	public ValuePoint(Point p, Color bc) {
		this.p = p;
		this.borderColor = bc;
	}

	/**
	 * ����һ�����ݵ�
	 * @param p ����
	 * @param bc �߿���ɫ
	 * @param fillColor �����ɫ
	 * @param shape �����״
	 * @param radius ��İ뾶
	 */
	public ValuePoint(Point p, Color bc,Color fillColor, int shape, int radius) {
		this(p, bc);
		this.fillColor = fillColor;
		this.shape = shape;
		this.radius = radius;
	}
}