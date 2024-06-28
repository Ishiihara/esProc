package com.scudata.chart.element;

import java.awt.geom.*;
import java.awt.*;
import java.util.*;

import com.scudata.chart.*;
import com.scudata.chart.edit.*;
import com.scudata.common.*;
import com.scudata.dm.*;

/**
 * �̶��� ����ʵ������ֵ�ĸ���
 * 
 * @author Joancy
 */

public abstract class TickAxis extends ObjectElement implements IAxis {
	private static int ARROW_SHIFT = 8;
	/***** �߼������� *****/
	// ������
	public String name; // ����

	// �Ƿ�ɼ�
	public boolean visible = true;

	// ��λ��
	public int location = Consts.AXIS_LOC_H;

	// �������ԣ���ʼ���������(xStart>xEnd��ʾ����)
	public double xStart = 0.1;

	// �������ԣ�������������꣨������ͷ��
	public double xEnd = 0.8;

	// �������ԣ�������
	public double xPosition = 0.8;

	// �������ԣ���ʼ����������(yStart<yEnd��ʾ����)
	public double yStart = 0.8;

	// �������ԣ���������������
	public double yEnd = 0.1;

	// �������ԣ�������
	public double yPosition = 0.1;

	// �������ԣ�ԭ�����������
	public double polarX = 0.4;

	// �������ԣ�ԭ������������
	public double polarY = 0.5;

	// ���᳤��
	public double polarLength = 0.3;

	// ����ĸ߿�ȣ�������1����Բ
	// public double HWRage = 1;

	// �������ԣ���ʼ�Ƕ�(0-360��)
	public double startAngle = 0;

	// �������ԣ������Ƕ�(0-360��)
	public double endAngle = 360;

	// ϵ�����ԣ�������,�ò��� ���岻�󣬿���ֱ��ָ��series���ԴӶ�ȷ��ϵ�е���ʾ����
	// public int seriesNum = 0;

	// ���ᡢ���ᶨ�壺�Ƿ�����
	// ֻ���ߡ���������֧������
	public boolean is3D = false;

	// 3d��ȱ��ʣ�ֱ������ϵ��Ϊ�����ϵ�п�ȱȣ���ֵ��ʱ��������ֵ����ֵ��������ϵ���ݲ�֧��
	public double threeDThickRatio = 0.38;

	/***** �̶���������� *****/
	// ��ɫ
	public Color axisColor = Color.LIGHT_GRAY;

	// ������
	public int axisLineStyle = Consts.LINE_SOLID;

	// ���߿�
	public int axisLineWeight = 1;

	// ���ͷ���̶����ȣ�
	public int axisArrow = Consts.LINE_ARROW_NONE;

	// ���������
	public String title;

	// �������������
	public String titleFont;// = "����";

	// �������������
	public int titleStyle;

	// ����������ֺ�
	public int titleSize = 16;

	// �����������գ�Ϊ�����������̶ȱ�ǩ�������������ģ��޿̶ȱ�ǩʱ���ľ���
	public int titleIndent = 2;

	// �����������ɫ
	public Color titleColor = Color.black;

	// �������б�Ƕ�
	public int titleAngle;

	// �Ƿ��п̶ȱ�ǩ
	public boolean allowLabels = true;

	// �̶ȱ�ǩ����
	public String labelFont;// = "����";

	// �̶ȱ�ǩ����
	public int labelStyle;

	// �̶ȱ�ǩ�ֺ�
	public int labelSize = 12;

	// �̶ȱ�ǩ���գ�Ϊ�̶ȱ�ǩ�������������ߵľ���
	public int labelIndent = 2;

	// �̶ȱ�ǩ��ɫ
	public Color labelColor = Color.darkGray;

	// �̶ȱ�ǩ��ʾ���
	public int labelStep = 0;

	// �̶ȱ�ǩ��б�Ƕ�
	public int labelAngle = 0;

	// �̶ȱ�ǩ�ܷ��ص�
	public boolean labelOverlapping = false;

	// �̶���λ��
	public int scalePosition = Consts.TICK_LEFTDOWN;

	// �̶�������
	public int scaleStyle = Consts.LINE_SOLID;

	// �̶����߿�
	public int scaleWeight = 1;

	// �̶��߳ߴ�
	public int scaleLength = 3;

	// �̶�����ʾ���
	public int displayStep = 0;

	// ��������ԣ��Ƿ��п̶ȼ����
	public boolean allowRegions = true;

	// // ��������ԣ��������ֹ�ڿ̶��м�λ��
	// public boolean regionMiddle = true;

	// ��������ԣ��̶ȼ����
	public int regionLineStyle = Consts.LINE_SOLID;

	// ��������ԣ��̶ȼ����ɫ
	public Color regionLineColor = Color.lightGray;

	// ��������ԣ��̶ȼ���߿�
	public int regionLineWeight = 1;

	// ��������ԣ��̶ȼ������ɫ
	public Para regionColors = new Para(null);// new Color(241,243,235)

	// ��������ԣ��̶ȼ����͸����(�����������)
	public float regionTransparent = 0.6f;

	// ��������ԣ��̶ȼ�����Ƿ�Ϊ����Σ�ֻ�ڼ���������Ч��ΪfalseʱΪ���λ��Σ�
	public boolean isPolygonalRegion = false;

	/**
	 * ȱʡֵ���캯��
	 */
	public TickAxis() {
		// ȱʡ���ɫΪ͸��ɫ�����ɫ
		Sequence seq = new Sequence();
		ChartColor c1 = new ChartColor(Color.white);
		c1.setGradient(false);
		seq.add(c1);
		ChartColor c2 = new ChartColor(new Color(241, 243, 235));
		c2.setGradient(false);
		seq.add(c2);
		regionColors = new Para(seq);// ������ֵ��ʼ��ɫ
	}

	/**
	 * ��ȡ�༭������Ϣ�б�
	 * 
	 * @return ������Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(TickAxis.class, this);
		paramInfos.add(new ParamInfo("name"));
		paramInfos.add(new ParamInfo("visible", Consts.INPUT_CHECKBOX));
		paramInfos.add(new ParamInfo("location", Consts.INPUT_AXISLOCATION));
		paramInfos.add(new ParamInfo("is3D", Consts.INPUT_CHECKBOX));
		paramInfos.add(new ParamInfo("threeDThickRatio", Consts.INPUT_DOUBLE));

		String group = "xaxis";
		paramInfos.add(group, new ParamInfo("xStart", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("xEnd", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("xPosition", Consts.INPUT_DOUBLE));

		group = "yaxis";
		paramInfos.add(group, new ParamInfo("yStart", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("yEnd", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("yPosition", Consts.INPUT_DOUBLE));

		group = "polaraxis";
		paramInfos.add(group, new ParamInfo("polarX", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("polarY", Consts.INPUT_DOUBLE));
		paramInfos
				.add(group, new ParamInfo("polarLength", Consts.INPUT_DOUBLE));

		group = "angleAxis";
		paramInfos.add(group, new ParamInfo("startAngle", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("endAngle", Consts.INPUT_DOUBLE));

		group = "axisLine";
		paramInfos.add(group, new ParamInfo("axisColor", Consts.INPUT_COLOR));
		paramInfos.add(group, new ParamInfo("axisLineStyle",
				Consts.INPUT_LINESTYLE));
		paramInfos.add(group, new ParamInfo("axisLineWeight",
				Consts.INPUT_INTEGER));
		paramInfos.add(group, new ParamInfo("axisArrow", Consts.INPUT_ARROW));

		group = "axisTitle";
		paramInfos.add(group, new ParamInfo("title"));
		paramInfos.add(group, new ParamInfo("titleFont", Consts.INPUT_FONT));
		paramInfos.add(group, new ParamInfo("titleStyle",
				Consts.INPUT_FONTSTYLE));
		paramInfos
				.add(group, new ParamInfo("titleSize", Consts.INPUT_FONTSIZE));
		paramInfos.add(group,
				new ParamInfo("titleIndent", Consts.INPUT_INTEGER));
		paramInfos.add(group, new ParamInfo("titleColor", Consts.INPUT_COLOR));
		paramInfos
				.add(group, new ParamInfo("titleAngle", Consts.INPUT_INTEGER));

		group = "labels";
		paramInfos.add(group, new ParamInfo("allowLabels",
				Consts.INPUT_CHECKBOX));
		paramInfos.add(group, new ParamInfo("labelFont", Consts.INPUT_FONT));
		paramInfos.add(group, new ParamInfo("labelStyle",
				Consts.INPUT_FONTSTYLE));
		paramInfos
				.add(group, new ParamInfo("labelSize", Consts.INPUT_FONTSIZE));
		paramInfos.add(group,
				new ParamInfo("labelIndent", Consts.INPUT_INTEGER));
		paramInfos.add(group, new ParamInfo("labelColor", Consts.INPUT_COLOR));
		paramInfos.add(group, new ParamInfo("labelStep", Consts.INPUT_INTEGER));
		paramInfos
				.add(group, new ParamInfo("labelAngle", Consts.INPUT_INTEGER));
		paramInfos.add(group, new ParamInfo("labelOverlapping",
				Consts.INPUT_CHECKBOX));

		group = "scaleLine";
		paramInfos.add(group,
				new ParamInfo("scalePosition", Consts.INPUT_TICKS));
		paramInfos.add(group, new ParamInfo("scaleStyle",
				Consts.INPUT_LINESTYLE));
		paramInfos.add(group,
				new ParamInfo("scaleWeight", Consts.INPUT_INTEGER));
		paramInfos.add(group,
				new ParamInfo("scaleLength", Consts.INPUT_INTEGER));
		paramInfos.add(group,
				new ParamInfo("displayStep", Consts.INPUT_INTEGER));

		group = "region";
		paramInfos.add(group, new ParamInfo("allowRegions",
				Consts.INPUT_CHECKBOX));
		paramInfos.add(group, new ParamInfo("regionLineStyle",
				Consts.INPUT_LINESTYLE));
		paramInfos.add(group, new ParamInfo("regionLineColor",
				Consts.INPUT_COLOR));
		paramInfos.add(group, new ParamInfo("regionLineWeight",
				Consts.INPUT_INTEGER));
		paramInfos.add(group, new ParamInfo("regionColors",
				Consts.INPUT_CHARTCOLOR));
		paramInfos.add(group, new ParamInfo("regionTransparent",
				Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("isPolygonalRegion",
				Consts.INPUT_CHECKBOX));

		return paramInfos;
	}

	/**
	 * ��ȡ�������
	 * 
	 * @return ����
	 */
	public String getName() {
		return name;
	}

	/**
	 * ��ȡ��ķ�λ
	 * 
	 * @return ��λ�ã�ֵ�ο�Consts.AXIS_LOC_X;
	 */
	public int getLocation() {
		return location;
	}

	/**
	 * ��ǰ���Ƿ����
	 * @return ���ӷ���true�����򷵻�false
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * ��ͼǰ������׼��
	 * ����һЩ���ò���������ԣ����߸�����ʹ�õ�����ֵ
	 * 
	 * @param dataElements ����ͼԪ
	 */
	public void prepare(ArrayList<DataElement> dataElements) {
		// ������
		startAngle = in360(startAngle);
		endAngle = in360(endAngle);
		if (startAngle > endAngle) {
			double d = startAngle;
			startAngle = endAngle;
			endAngle = d;
		}

		// �̶ȼ���ĸ�����1��ֱ������(%)������Ƿ�Ϊ0���ɣ��̶ȼ��Ϊ1��ʾΪ��1����ʾ��Ҳ�� %2 �Ľ��
		displayStep = displayStep + 1;
		labelStep = labelStep + 1;

		for (int i = 0; i < dataElements.size(); i++) {
			DataElement de = dataElements.get(i);
			if (de.hasGradientColor()) {
				useGradient = true;
				break;
			}
		}

	}

	/**
	 * �жϽ�����������Ƿ��Ǹ�Բ
	 * Բ������������ɿ̶ȱ�ʶ�Ĳ�ͬ
	 * 
	 * @return boolean ��Բ�򷵻�true�����򷵻�false
	 */
	public boolean isCircleAngle() {
		double angleRange = endAngle - startAngle;
		return angleRange == 360;
	}

	/**
	 * ��ȡ������س���
	 * ���н�����Ϊ���᷶Χ
	 * @return ��ĳ���
	 */
	public double getAxisLength() {
		Point2D p1, p2;
		p1 = getStartPoint();
		p2 = getEndPoint();
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			return Math.abs(p2.getX() - p1.getX());
		case Consts.AXIS_LOC_ANGLE:
		case Consts.AXIS_LOC_V:
			return Math.abs(p2.getY() - p1.getY());
		}
		return 0;
	}

	double in360(double angle) {
		double d = angle;
		while (d < 0) {
			d = d + 360;
		}
		while (d > 360) {
			d = d - 360;
		}
		return d;
	}

	private Point2D getPoint(boolean getStart) {
		double x = 0, y = 0;
		switch (location) {
		case Consts.AXIS_LOC_H:
			if (getStart) {
				x = e.getXPixel(xStart);
			} else {
				x = e.getXPixel(xEnd);
			}
			y = e.getYPixel(xPosition);
			break;
		case Consts.AXIS_LOC_V:
			x = e.getXPixel(yPosition);
			if (getStart) {
				y = e.getYPixel(yStart);
			} else {
				y = e.getYPixel(yEnd);
			}
			break;
		case Consts.AXIS_LOC_POLAR:
			if (getStart) {
				x = e.getXPixel(polarX);
			} else {
				x = e.getXPixel(polarX) + e.getXPixel(polarLength);
			}
			y = e.getYPixel(polarY);
			break;
		case Consts.AXIS_LOC_ANGLE:
			if (getStart) {
				y = startAngle;
			} else {
				y = endAngle;
			}
			break;
		}
		return new Point2D.Double(x, y);
	}

	Point2D getStartPoint() {
		return getPoint(true);
	}

	Point2D getEndPoint() {
		return getPoint(false);
	}

	/**
	 * ��ȡ������Ķ���Y����
	 * ����ʱ����endAngle
	 * @return y����ֵ
	 */
	public double getTopY() {
		switch (location) {
		case Consts.AXIS_LOC_H:
			return e.getYPixel(xPosition);
		case Consts.AXIS_LOC_V:
			return Math.min(e.getYPixel(yStart), e.getYPixel(yEnd));
		case Consts.AXIS_LOC_POLAR:
			return e.getYPixel(polarY);
		case Consts.AXIS_LOC_ANGLE:
			return (int) endAngle;
		}
		return 0;
	}

	/**
	 * ��ȡ������ĵ׵�Y����
	 * ����ʱ����startAngle
	 * @return y����ֵ
	 */
	public double getBottomY() {
		switch (location) {
		case Consts.AXIS_LOC_H:
			return e.getYPixel(xPosition);
		case Consts.AXIS_LOC_V:
			return Math.max(e.getYPixel(yStart), e.getYPixel(yEnd));
		case Consts.AXIS_LOC_POLAR:
			return e.getYPixel(polarY);
		case Consts.AXIS_LOC_ANGLE:
			return (int) startAngle;
		}
		return 0;
	}

	/**
	 * ��ȡ����������X����
	 * ����ʱ������
	 * @return x����ֵ
	 */
	public double getLeftX() {
		switch (location) {
		case Consts.AXIS_LOC_H:
			return Math.min(e.getXPixel(xStart), e.getXPixel(xEnd));
		case Consts.AXIS_LOC_V:
			return e.getXPixel(yPosition);
		case Consts.AXIS_LOC_POLAR:
			return e.getXPixel(polarX);
		}
		return 0;
	}

	/**
	 * ��ȡ��������ұ�X����
	 * ����ʱ������
	 * @return x����ֵ
	 */
	public double getRightX() {
		switch (location) {
		case Consts.AXIS_LOC_H:
			return Math.max(e.getXPixel(xStart), e.getXPixel(xEnd));
		case Consts.AXIS_LOC_V:
			return e.getXPixel(yPosition);
		case Consts.AXIS_LOC_POLAR:
			return e.getXPixel(polarX) + e.getXPixel(polarLength);
		}
		return 0;
	}

	/*
	 * ֱ������ϵʱ�������ǹ���ľ��Σ����̶���ʱֻ��һ���ߣ� ���������regionShape�����������ʱ�ص����������߿���������
	 * ��һ�������ɫ���ڶ������Ǳ��ߣ�����󻭵�����Ḳ�Ǳ��ߣ���Ҫ�󻭡�
	 * 
	 * @param regionShape
	 *            Shape
	 * @param regionColor
	 *            Color
	 * @param location
	 *            int ÿ������ֻ���Ҷ��ߣ�����᲻�����ʱ����ȱ�����ߣ����Ե�һ���̶�ʱ��ʹ��doubleEdge������
	 */
	private void drawRegion(Shape regionShape, ChartColor regionColor, int step) {
		drawRegion(regionShape, regionColor, step, false);
	}

	private void drawRegion(Shape regionShape, ChartColor regionColor,
			int step, boolean doubleEdge) {
		Graphics2D g = e.getGraphics();
		Rectangle rect = regionShape.getBounds();
		if (step == 1) {
			boolean isSet = false;
			if (regionColor.isDazzle()) {
				CubeColor ccr = new CubeColor(regionColor.getColor1());
				ChartColor tmpcc = new ChartColor();
				tmpcc.setColor1(ccr.getF2());
				tmpcc.setColor2(ccr.getF1());
				tmpcc.setAngle(regionColor.getAngle());
				isSet = Utils.setPaint(g, rect.x, rect.y, rect.width,
						rect.height, tmpcc);
			} else {
				isSet = Utils.setPaint(g, rect.x, rect.y, rect.width,
						rect.height, regionColor);
			}
			if (isSet) {
				Utils.fillPaint(g, regionShape, regionTransparent);
			}
		} else if (step == 2) {
			if (Utils.setStroke(g, regionLineColor, regionLineStyle,
					regionLineWeight)) {
				int x, y;
				if (location == Consts.AXIS_LOC_H) {
					x = rect.x;
					y = rect.y;
					g.drawLine(x + rect.width, y, x + rect.width, y
							+ rect.height);
					if (doubleEdge) {
						g.drawLine(x, y, x, y + rect.height);
					}
				} else if (location == Consts.AXIS_LOC_V) {
					x = rect.x;
					y = rect.y;
					// �������±���Ȼ���ص����ƣ����������ױߵ�����Ų���м�󣬲�����ֵױ߿�ȱ
					g.drawLine(x, y, x + rect.width, y);
					if (doubleEdge) {
						g.drawLine(x, y + rect.height, x + rect.width, y
								+ rect.height);
					}
				} else {
					g.draw(regionShape);
				}
			}
		}
	}

	// ���������ת��ֱ������㣬����ӵ��������
	private void addPolarPoint(java.awt.Polygon polygon, PolarCoor pc,
			double polarLen, double angle) {
		Point2D p = new Point2D.Double(polarLen, angle);
		p = pc.getScreenPoint(p);
		polygon.addPoint((int) p.getX(), (int) p.getY());
	}

	/**
	 * ���Ʊ�����
	 */
	public void drawBack() {
		if (!isVisible()) {
			return;
		}
		// ��Ҫ�Ƚ����仭��
		drawRegionStep(1);
		drawRegionStep(2);
	}

	void drawAxisBorder() {
		Graphics2D g = e.getGraphics();
		ArrayList coorList = e.getCoorList();
		// ������
		if (Utils.setStroke(g, axisColor, axisLineStyle, axisLineWeight)) {
			double x, y, w, h;
			switch (location) {
			case Consts.AXIS_LOC_H:
			case Consts.AXIS_LOC_POLAR:
				Utils.setStroke(g, axisColor, axisLineStyle, axisLineWeight);
				// 1.������
				Utils.drawLine(g, getStartPoint(), getEndPoint());
				x = getRightX();
				y = getBottomY();

				int style = Utils.getArrow(axisArrow);
				if (style != Consts.LINE_ARROW_NONE) {
					if (style == Consts.LINE_ARROW_L) {
						x = getLeftX();
						Utils.drawLine(g, x, y, x - ARROW_SHIFT, y);
						Utils.drawLineArrow(g, x - ARROW_SHIFT, y, 0, axisArrow);
					} else {
						Utils.drawLine(g, x, y, x + ARROW_SHIFT, y);
						Utils.drawLineArrow(g, x + ARROW_SHIFT, y, 0, axisArrow);
					}
				}

				if (location == Consts.AXIS_LOC_H) { // ֱ������ϵ����3Dƽ���ı���
					for (int i = 0; i < coorList.size(); i++) {
						Object coor = coorList.get(i);
						if (!(coor instanceof CartesianCoor)) {
							continue;
						}
						CartesianCoor cc = (CartesianCoor) coor;
						if (cc.getXAxis() != this) {
							continue;
						}
						int coorShift = cc.get3DShift();
						TickAxis yAxis = cc.getYAxis();
						x = getLeftX();
						y = yAxis.getBottomY();
						Utils.setStroke(g, axisColor, axisLineStyle,
								axisLineWeight);
						double x2 = getRightX();
						if (coorShift == 0) {
							// 2.����3D��ʱ���Ѿ���1�����ߣ��˴������ٻ���������᲻�ڵױ�ʱ������������ͬ����
							// g.drawLine((int)x, (int)y, (int)x2, (int)y);
						} else {
							java.awt.Polygon polygon = new java.awt.Polygon();
							polygon.addPoint((int) x, (int) y);
							polygon.addPoint((int) x + coorShift, (int) y
									- coorShift);
							polygon.addPoint((int) x2 + coorShift, (int) y
									- coorShift);
							polygon.addPoint((int) x2, (int) y);
							g.draw(polygon);
						}
					}
				}
				break;
			case Consts.AXIS_LOC_V:
				Utils.setStroke(g, axisColor, axisLineStyle, axisLineWeight);
				Utils.drawLine(g, getStartPoint(), getEndPoint());
				x = getLeftX();
				y = getTopY();

				int styl = Utils.getArrow(axisArrow);
				if (styl != Consts.LINE_ARROW_NONE) {
					if (styl == Consts.LINE_ARROW_L) {
						y = getBottomY();
						Utils.drawLine(g, x, y, x, y + ARROW_SHIFT);
						Utils.drawLineArrow(g, x, y + ARROW_SHIFT,
								-Math.PI / 2, axisArrow);
					} else {
						Utils.drawLine(g, x, y, x, y - ARROW_SHIFT);
						Utils.drawLineArrow(g, x, y - ARROW_SHIFT,
								-Math.PI / 2, axisArrow);
					}
				}

				for (int i = 0; i < coorList.size(); i++) {
					Object coor = coorList.get(i);
					if (!(coor instanceof CartesianCoor)) {
						continue;
					}
					CartesianCoor cc = (CartesianCoor) coor;
					if (cc.getYAxis() != this) {
						continue;
					}
					int coorShift = cc.get3DShift();
					Utils.setStroke(g, axisColor, axisLineStyle, axisLineWeight);
					TickAxis xAxis = (TickAxis) cc.getXAxis();
					double x1 = xAxis.getLeftX();
					double x2 = xAxis.getRightX();
					double thisX = getLeftX(); // ��ֱ�������x
					double d1 = Math.abs(x1 - thisX);
					double d2 = Math.abs(x2 - thisX);
					x = (d1 < d2) ? x1 : x2; // ˭��ý���ȡ˭
					y = getBottomY();
					double y2 = getTopY();
					if (coorShift == 0) {
						// ע�͵���ͬ��
						// g.drawLine((int)x, (int)y, (int)x, (int)y2);
					} else {
						// ����ʱ��ƽ���ı������ǿ�����ǰ���x
						java.awt.Polygon polygon = new java.awt.Polygon();
						polygon.addPoint((int) x, (int) y);
						polygon.addPoint((int) x, (int) y2);
						polygon.addPoint((int) x + coorShift, (int) y2
								- coorShift);
						polygon.addPoint((int) x + coorShift, (int) y
								- coorShift);
						g.draw(polygon);
					}
				}
				break;
			case Consts.AXIS_LOC_ANGLE:
				Point2D p1 = null,
				p2;
				for (int i = 0; i < coorList.size(); i++) {
					Object coor = coorList.get(i);
					if (!(coor instanceof PolarCoor)) {
						continue;
					}
					PolarCoor pc = (PolarCoor) coor;
					if (pc.getAngleAxis() != this) {
						continue;
					}
					TickAxis polarAxis = (TickAxis) pc.getPolarAxis();
					double polarLen = polarAxis.getAxisLength();
					if (isPolygonalRegion) {
						int tCount = t_coorValue.length();
						for (int t = 1; t <= tCount; t++) {
							Object tickVal = t_coorValue.get(t);
							double angle = getTickPosition(tickVal);
							p2 = new Point2D.Double(polarLen, angle);
							p2 = pc.getScreenPoint(p2);
							Utils.drawLine(g, p1, p2);
							p1 = p2;
							if (isCircleAngle() && t == tCount) {
								p2 = new Point2D.Double(polarLen, 360);
								p2 = pc.getScreenPoint(p2);
								Utils.drawLine(g, p1, p2);
							}
						}
					} else { // ����
						Point2D orginalPoint = new Point2D.Double(
								polarAxis.getLeftX(), polarAxis.getBottomY()); // ԭ��
						x = orginalPoint.getX() - polarLen;
						y = orginalPoint.getY() - polarLen;
						w = polarLen * 2;
						h = w;
						java.awt.geom.Arc2D axisArc = new java.awt.geom.Arc2D.Double(
								x, y, w, h, startAngle, endAngle - startAngle,
								java.awt.geom.Arc2D.OPEN);
						g.draw(axisArc);
					}
				}
				break;
			}
		} else if (useGradient) {// �ޱ߿����ʹ���˽���ɫ��3D�����ᣬ����3d��ƽ̨
			double x, y, w, h;
			if (location == Consts.AXIS_LOC_H) { // ֱ������ϵ����3DCubeƽ̨
				for (int i = 0; i < coorList.size(); i++) {
					Object coor = coorList.get(i);
					if (!(coor instanceof CartesianCoor)) {
						continue;
					}
					CartesianCoor cc = (CartesianCoor) coor;
					if (cc.getXAxis() != this) {
						continue;
					}
					int coorShift = cc.get3DShift();
					if (coorShift == 0) {
						continue;
					}
					TickAxis yAxis = (TickAxis) cc.getYAxis();
					x = (int) getLeftX();
					y = (int) yAxis.getBottomY();
					double x2 = getRightX();
					w = (int) x2 - x;
					h = Utils.getPlatformH(coorShift);
					coorThick = h;
					ChartColor fillColor = new ChartColor(axisColor);
					Utils.draw3DRect(g, x, y, w, h, null, 0, 0, false, false,
							1, fillColor, true, coorShift);
				}
			} else if (location == Consts.AXIS_LOC_V) {
				for (int i = 0; i < coorList.size(); i++) {
					Object coor = coorList.get(i);
					if (!(coor instanceof CartesianCoor)) {
						continue;
					}
					CartesianCoor cc = (CartesianCoor) coor;
					if (cc.getYAxis() != this) {
						continue;
					}
					int coorShift = cc.get3DShift();
					if (coorShift == 0) {
						continue;
					}
					// ����ʱ��ƽ���ı������ǿ�����ǰ���x
					TickAxis xAxis = (TickAxis) cc.getXAxis();
					double x1 = xAxis.getLeftX();
					double x2 = xAxis.getRightX();
					double thisX = getLeftX(); // ��ֱ�������x
					double d1 = Math.abs(x1 - thisX);
					double d2 = Math.abs(x2 - thisX);
					w = Utils.getPlatformH(coorShift);
					x = (int) (((d1 < d2) ? x1 : x2) - w); // ˭��ý���ȡ˭
					y = (int) getTopY();
					h = (int) getBottomY() - y;
					coorThick = w;
					ChartColor fillColor = new ChartColor(axisColor);
					Utils.draw3DRect(g, x, y, w, h, null, 0, 0, false, false,
							1, fillColor, false, coorShift);
				}

			}

		}

	}

	/*
	 * ���㡾�߼�ֵ��val�����ϳ��ȡ��ó��������������ϵ����ֵ���꡿�� �����ּ��㷽ʽ��
	 * 1�����Գ��� ��val�������̶ȷ�Χ����ռ�еĳ��ȣ�һ�����ڰ뾶���㡣
	 * ö������val��ϵ�п�ȣ��������ǰ�val���㳤�ȣ���ֵ����val�㳤�ȣ����ִ�С��1������� 
	 * 2:�̶ȳ���  ��val��Ϊ�̶ȷ�Χ�������������ԭ��ĳ��� 
	 * ��������Ŀ̶ȷ�Χ��[50,60,70,80,90,100] ����ֵ60�ĳ��ȣ�
	 * 1��ʽΪaxisLen*60/(100-50)�� 
	 * 2��ʽΪ�̶�60���̶�50�ĳ��ȣ�
	 */
	abstract double getValueLength(Object val, boolean isAbsolute);

	/**
	 * ���ڶ�����ֵ����ʱ������������ת��Ϊ��ֵ���ꡣ
	 * @param val Ҫת���Ŀ̶�ֵ
	 * @return double���ȵ���ֵ����
	 */
	public abstract double animateDoubleValue(Object val);
	
	/**
	 * ��ȡ��ֵval�����ϵ���Ϊ�뾶�õĳ���
	 * 
	 * @param val �߼���ֵ
	 * @return 
	 */
	public double getValueRadius(double val) {
		return getValueLength(new Double(val), true);
	}

	/**
	 * ��ȡ����ֵval�����ϵĳ���
	 * 
	 * @param val ����ֵ
	 * @return ���ص�λ�ĳ���
	 */
	public double getValueLen(Object val) {
		return getValueLength(val, false);
	}

	/**
	 * ����̶�ֵ������������λ�ã�λ����Ļ������ֵ ������ֵ���б任��������ֵ���tickValҪ�ȷ��任�����ܼ���̶�λ��
	 * 
	 * @param tickVal
	 * @return
	 */
	protected double getTickPosition(Object tickVal) {
		double len = getValueLength(tickVal, false);// ����̶�λ��
		double pos = 0;
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			pos = getLeftX() + len;
			break;
		case Consts.AXIS_LOC_V:
			pos = getBottomY() - len;
			break;
		case Consts.AXIS_LOC_ANGLE:
			pos = startAngle + len;
			break;
		}
		return pos;
	}

	private void drawRegionStep(int step) {
		if (!allowRegions)
			return;
		// ֻ�е�һ��������Ҫ��˫��
		boolean doubleEdge = true;

		ArrayList<ICoor> coorList = e.getCoorList();
		ChartColor tmpcc;
		Shape regionShape;
		Point2D p1, p2;
		Graphics2D g = e.getGraphics();
		switch (location) {
		case Consts.AXIS_LOC_H:
			for (int i = 0; i < coorList.size(); i++) {
				ICoor coor = coorList.get(i);
				if (coor.isPolarCoor()) {
					continue;
				}
				CartesianCoor cc = (CartesianCoor) coor;
				if (cc.getXAxis() != this) {
					continue;
				}
				int coorShift = cc.get3DShift();
				TickAxis yAxis = (TickAxis) cc.getYAxis();
				p1 = new Point2D.Double(getLeftX() + coorShift, yAxis.getTopY()
						- coorShift);
				int tCount = t_coorValue.length();
				int rc = 0;
				for (int t = 1; t <= tCount; t++) {
					if ((t - 1) % displayStep != 0) {
						continue;
					}
					Object tickVal = t_coorValue.get(t);
					double tickPosition = getTickPosition(tickVal);
					// ��ͬ���͵�����߲�ͬλ�õ��ᣬ�̶ȱ�ע������λ�ò�ͬ
					if (t == 1 && tickPosition == getLeftX()) {
						continue;
					}
					if (Utils.setStroke(g, axisColor, scaleStyle, scaleWeight)) { // 3D��Ŀ̶���
						Utils.drawLine(g, tickPosition, yAxis.getBottomY(),
								tickPosition + coorShift, yAxis.getBottomY()
										- coorShift);
					}

					p2 = new Point2D.Double(tickPosition + coorShift,
							yAxis.getTopY() - coorShift);
					regionShape = new Rectangle2D.Double(p1.getX(), p1.getY(),
							p2.getX() - p1.getX(), yAxis.getAxisLength());
					tmpcc = regionColors.chartColorValue(++rc);
					drawRegion(regionShape, tmpcc, step, doubleEdge);
					doubleEdge = false;
					p1 = p2;

					if (t == tCount && tickPosition < getRightX()) {
						p2 = new Point2D.Double(getRightX() + coorShift, 0);
						regionShape = new Rectangle2D.Double(p1.getX(),
								p1.getY(), p2.getX() - p1.getX(),
								yAxis.getAxisLength());
						tmpcc = regionColors.chartColorValue(++rc);
						drawRegion(regionShape, tmpcc, step);
					}
				}
			}
			break;
		case Consts.AXIS_LOC_V:
			for (int i = 0; i < coorList.size(); i++) {
				ICoor coor = coorList.get(i);
				if (coor.isPolarCoor()) {
					continue;
				}
				CartesianCoor cc = (CartesianCoor) coor;
				if (cc.getYAxis() != this) {
					continue;
				}
				int coorShift = cc.get3DShift();
				TickAxis xAxis = (TickAxis) cc.getXAxis();
				p1 = new Point2D.Double(xAxis.getLeftX() + coorShift,
						getBottomY() - coorShift);
				int tCount = t_coorValue.length();
				int rc = 0;
				for (int t = 1; t <= tCount; t++) {
					if ((t - 1) % displayStep != 0) {
						continue;
					}
					Object tickVal = t_coorValue.get(t);
					double tickPosition = getTickPosition(tickVal);
					if (t == 1 && tickPosition == getBottomY()) {
						continue;
					}
					if (t != 1
							&& t != tCount
							&& Utils.setStroke(g, axisColor, scaleStyle,
									scaleWeight)) { // 3D��Ŀ̶���
						// ��ͷ���߻���ı����ص����Ͳ�Ҫ��
						// ����ʱ��ƽ���ı������ǿ�����ǰ���x
						double x1 = xAxis.getLeftX();
						double x2 = xAxis.getRightX();
						double thisX = getLeftX(); // ��ֱ�������x
						double d1 = Math.abs(x1 - thisX);
						double d2 = Math.abs(x2 - thisX);
						double x = (d1 < d2) ? x1 : x2; // ˭��ý���ȡ˭
						Utils.drawLine(g, x, tickPosition, x + coorShift,
								tickPosition - coorShift);
					}

					p2 = new Point2D.Double(p1.getX(), tickPosition - coorShift);
					regionShape = new Rectangle2D.Double(p2.getX(), p2.getY(),
							xAxis.getAxisLength(), p1.getY() - p2.getY());

					tmpcc = regionColors.chartColorValue(++rc);
					drawRegion(regionShape, tmpcc, step, doubleEdge);
					doubleEdge = false;
					p1 = p2;

					if (t == tCount && tickPosition != getTopY()) {
						p2 = new Point2D.Double(p1.getX(),// + coorShift
								getTopY() - coorShift);
						regionShape = new Rectangle2D.Double(p2.getX(),
								p2.getY(), xAxis.getAxisLength(), p1.getY()
										- p2.getY());
						tmpcc = regionColors.chartColorValue(++rc);
						drawRegion(regionShape, tmpcc, step);
					}
				}
			}
			break;
		case Consts.AXIS_LOC_POLAR:
			for (int i = 0; i < coorList.size(); i++) {
				ICoor coor = coorList.get(i);
				if (coor.isCartesianCoor()) {
					continue;
				}
				PolarCoor pc = (PolarCoor) coor;
				if (pc.getPolarAxis() != this) {
					continue;
				}
				TickAxis angleAxis = (TickAxis) pc.getAngleAxis();
				Point2D orginalPoint = new Point2D.Double(getLeftX(),
						getBottomY()); // ԭ��
				java.awt.geom.Area area1 = null, area2;
				int tCount = t_coorValue.length();
				int rc = 0;
				for (int t = 1; t <= tCount; t++) {
					if ((t - 1) % displayStep != 0) {
						continue;
					}
					Object tickVal = t_coorValue.get(t);
					double tickPosition = getTickPosition(tickVal);
					if (t == 1 && tickPosition == getLeftX()) {
						continue;
					}

					if (isPolygonalRegion) {
						java.awt.Polygon polygon = new java.awt.Polygon();
						double polarLen = getTickPosition(tickVal) - getLeftX();
						for (int n = 1; n <= angleAxis.t_coorValue.length(); n++) {
							Object angleTick = angleAxis.t_coorValue.get(n);
							double angle = angleAxis.getTickPosition(angleTick);
							Point2D polarPoint = new Point2D.Double(polarLen,
									angle);
							Point2D p = pc.getScreenPoint(polarPoint);
							polygon.addPoint((int) p.getX(), (int) p.getY());
						}
						if (!angleAxis.isCircleAngle()) {
							polygon.addPoint((int) orginalPoint.getX(),
									(int) orginalPoint.getY());
						}
						area2 = new java.awt.geom.Area(polygon);
						regionShape = new java.awt.geom.Area(area2);
						if (area1 != null) {
							((java.awt.geom.Area) regionShape).subtract(area1);
						}
						tmpcc = regionColors.chartColorValue(++rc);
						drawRegion(regionShape, tmpcc, step);
						area1 = area2;

						if (t == tCount && tickPosition != getRightX()) {
							polarLen = getAxisLength();
							polygon = new java.awt.Polygon();
							for (int n = 1; n <= angleAxis.t_coorValue.length(); n++) {
								Object angleTick = angleAxis.t_coorValue.get(n);
								double angle = angleAxis
										.getTickPosition(angleTick);
								Point2D polarPoint = new Point2D.Double(
										polarLen, angle);
								Point2D p = pc.getScreenPoint(polarPoint);
								polygon.addPoint((int) p.getX(), (int) p.getY());
							}
							if (!angleAxis.isCircleAngle()) {
								polygon.addPoint((int) orginalPoint.getX(),
										(int) orginalPoint.getY());
							}
							regionShape = new java.awt.geom.Area(polygon);
							((java.awt.geom.Area) regionShape).subtract(area1);
							tmpcc = regionColors.chartColorValue(++rc);
							drawRegion(regionShape, tmpcc, step);
						}
					} else { // ����
						double x, y, w, h, tmpLen;
						tmpLen = tickPosition - orginalPoint.getX();
						x = orginalPoint.getX() - tmpLen;
						y = orginalPoint.getY() - tmpLen;
						w = tmpLen * 2;
						h = w;

						Arc2D sector = new Arc2D.Double(x, y, w, h,
								angleAxis.startAngle, angleAxis.endAngle
										- angleAxis.startAngle,
								java.awt.geom.Arc2D.PIE);

						area2 = new java.awt.geom.Area(sector);
						regionShape = new java.awt.geom.Area(area2);
						if (area1 != null) {
							((java.awt.geom.Area) regionShape).subtract(area1);
						}
						tmpcc = regionColors.chartColorValue(++rc);
						drawRegion(regionShape, tmpcc, step);
						area1 = area2;

						if (t == tCount && tickPosition != getRightX()) {
							tmpLen = getRightX() - orginalPoint.getX();
							x = orginalPoint.getX() - tmpLen;
							y = orginalPoint.getY() - tmpLen;
							w = tmpLen * 2;
							h = w;

							sector = new java.awt.geom.Arc2D.Double(x, y, w, h,
									angleAxis.startAngle, angleAxis.endAngle
											- angleAxis.startAngle,
									java.awt.geom.Arc2D.PIE);

							regionShape = new java.awt.geom.Area(sector);
							if (area1 != null) {
								((java.awt.geom.Area) regionShape)
										.subtract(area1);
							}
							tmpcc = regionColors.chartColorValue(++rc);
							drawRegion(regionShape, tmpcc, step);
						}
					}
				}
			}
			break;
		case Consts.AXIS_LOC_ANGLE:
			for (int i = 0; i < coorList.size(); i++) {
				ICoor coor = coorList.get(i);
				if (coor.isCartesianCoor()) {
					continue;
				}
				PolarCoor pc = (PolarCoor) coor;
				if (pc.getAngleAxis() != this) {
					continue;
				}
				TickAxis polarAxis = (TickAxis) pc.getPolarAxis();
				Point2D orginalPoint = new Point2D.Double(polarAxis.getLeftX(),
						polarAxis.getBottomY()); // ԭ��
				double angle1 = 0, angle2, polarLen;
				polarLen = polarAxis.getAxisLength();
				int tCount = t_coorValue.length();

				double x, y, w, h;
				x = orginalPoint.getX() - polarLen;
				y = orginalPoint.getY() - polarLen;
				w = polarLen * 2;
				h = w;
				int rc = 0;
				for (int t = 1; t <= tCount; t++) {
					Object tickVal = t_coorValue.get(t);
					double tickPosition = getTickPosition(tickVal);
					if (t == 1) {
						angle1 = tickPosition;
						continue;
					}
					angle2 = tickPosition;

					if (isPolygonalRegion) {
						java.awt.Polygon polygon = new java.awt.Polygon();
						addPolarPoint(polygon, pc, polarLen, angle1);
						addPolarPoint(polygon, pc, polarLen, angle2);
						polygon.addPoint((int) orginalPoint.getX(),
								(int) orginalPoint.getY());

						regionShape = polygon;
						tmpcc = regionColors.chartColorValue(++rc);
						drawRegion(regionShape, tmpcc, step);

						angle1 = angle2;

						if (isCircleAngle() && t == tCount) {
							polygon = new java.awt.Polygon();
							addPolarPoint(polygon, pc, polarLen, angle1);
							addPolarPoint(polygon, pc, polarLen, 360);
							polygon.addPoint((int) orginalPoint.getX(),
									(int) orginalPoint.getY());
							regionShape = polygon;
							tmpcc = regionColors.chartColorValue(++rc);
							drawRegion(regionShape, tmpcc, step);
						}
					} else { // ����
						Arc2D sector = new Arc2D.Double(x, y, w, h, angle1,
								angle2 - angle1, java.awt.geom.Arc2D.PIE);

						regionShape = new java.awt.geom.Area(sector);
						tmpcc = regionColors.chartColorValue(++rc);
						drawRegion(regionShape, tmpcc, step);
						angle1 = angle2;

						if (isCircleAngle() && t == tCount) {
							sector = new java.awt.geom.Arc2D.Double(x, y, w, h,
									angle1, 360 - angle1,
									java.awt.geom.Arc2D.PIE);
							regionShape = new java.awt.geom.Area(sector);
							tmpcc = regionColors.chartColorValue(++rc);
							drawRegion(regionShape, tmpcc, step);
						}
					}
				}
			}
			break;
		}

		Utils.setTransparent(g, 1.0f);

	}

	/**
	 * �����м��
	 */
	public void draw() {
		if (!isVisible()) {
			return;
		}
		drawAxisBorder();

		int tickSize = t_coorValue.length();
		if (tickSize == 0) {
			return;
		}

		ArrayList coorList = e.getCoorList();
		double x, y;
		Color tickColor = axisColor;
		Graphics2D g = e.getGraphics();
		if (Utils.setStroke(g, tickColor, scaleStyle, scaleWeight)) {
			switch (location) {
			case Consts.AXIS_LOC_H:
			case Consts.AXIS_LOC_POLAR:
				// ���ƿ̶���
				y = getBottomY() + coorThick;
				for (int t = 1; t <= tickSize; t++) {
					if ((t - 1) % displayStep != 0) { // t-1�� ��1���̶ȿ�ʼ���ƣ�Ȼ���ټ��
						continue;
					}
					x = getTickPosition(t_coorValue.get(t));
					if (scalePosition == Consts.TICK_RIGHTUP) {
						Utils.drawLine(g, x, y - scaleLength, x, y);
					} else if (this.scalePosition == Consts.TICK_LEFTDOWN) {
						Utils.drawLine(g, x, y, x, y + scaleLength);
					} else if (this.scalePosition == Consts.TICK_CROSS) {
						Utils.drawLine(g, x, y - scaleLength / 2, x, y
								+ scaleLength / 2);
					}
				}
				break;
			case Consts.AXIS_LOC_V:
				x = getLeftX() - coorThick;
				for (int t = 1; t <= tickSize; t++) {
					if ((t - 1) % displayStep != 0) {
						continue;
					}
					y = getTickPosition(t_coorValue.get(t));
					if (scalePosition == Consts.TICK_RIGHTUP) {
						Utils.drawLine(g, x, y, x + scaleLength, y);
					} else if (this.scalePosition == Consts.TICK_LEFTDOWN) {
						Utils.drawLine(g, x - scaleLength, y, x, y);
					} else if (this.scalePosition == Consts.TICK_CROSS) {
						Utils.drawLine(g, x - scaleLength / 2, y, x
								+ scaleLength / 2, y);
					}
				}
				break;
			case Consts.AXIS_LOC_ANGLE:
				for (int i = 0; i < coorList.size(); i++) {
					Object coor = coorList.get(i);
					if (!(coor instanceof PolarCoor)) {
						continue;
					}
					PolarCoor pc = (PolarCoor) coor;
					if (pc.getAngleAxis() != this) {
						continue;
					}
					TickAxis polarAxis = (TickAxis) pc.getPolarAxis();
					double angle, polarLen;
					polarLen = polarAxis.getAxisLength();
					int tCount = t_coorValue.length();
					for (int t = 1; t <= tCount; t++) {
						if ((t - 1) % displayStep != 0) {
							continue;
						}
						Object tickVal = t_coorValue.get(t);
						angle = getTickPosition(tickVal);

						Point2D b = null, e = null;
						if (scalePosition == Consts.TICK_RIGHTUP) {
							b = new Point2D.Double(polarLen, angle);
							e = new Point2D.Double(polarLen + scaleLength,
									angle);
						} else if (this.scalePosition == Consts.TICK_LEFTDOWN) {
							b = new Point2D.Double(polarLen - scaleLength,
									angle);
							e = new Point2D.Double(polarLen, angle);
						} else if (this.scalePosition == Consts.TICK_CROSS) {
							b = new Point2D.Double(polarLen - scaleLength / 2,
									angle);
							e = new Point2D.Double(polarLen + scaleLength / 2,
									angle);
						}
						if (b != null) {
							b = pc.getScreenPoint(b);
							e = pc.getScreenPoint(e);
							Utils.drawLine(g, b, e);
						}
					}
				}
				break;
			}
		}

	}

	protected int adjustLabelPosition(Point2D p) {
		double x = p.getX(), y = p.getY();
		int locationType = Consts.LOCATION_CB;
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			if (scalePosition == Consts.TICK_RIGHTUP) {
				y -= scaleLength;
				y -= labelIndent;
				locationType = Consts.LOCATION_CB;
			} else if (this.scalePosition == Consts.TICK_LEFTDOWN) {
				y += scaleLength;
				y += labelIndent;
				locationType = Consts.LOCATION_CT;
			} else { // ������޿̶�ʱ����ǩĬ�ϻ����±ߣ��±���ã�
				y += scaleLength / 2;
				y += labelIndent;
				locationType = Consts.LOCATION_CT;
			}
			break;
		case Consts.AXIS_LOC_V:
			if (scalePosition == Consts.TICK_RIGHTUP) {
				x += scaleLength;
				x += labelIndent;
				locationType = Consts.LOCATION_LM;
			} else if (this.scalePosition == Consts.TICK_LEFTDOWN) {
				x -= scaleLength;
				x -= labelIndent;
				locationType = Consts.LOCATION_RM;
			} else { // ������޿̶�ʱ����ǩĬ�ϻ�����ߣ������ã�
				x -= scaleLength / 2;
				x -= labelIndent;
				locationType = Consts.LOCATION_RM;
			}
			break;
		case Consts.AXIS_LOC_ANGLE:
			locationType = Utils.getAngleTextLocation(y);
			break;
		}
		p.setLocation(x, y);
		return locationType;
	}

	/**
	 * ����ǰ����
	 */
	public void drawFore() {
		if (!isVisible()) {
			return;
		}
		int tickSize = t_coorValue.length();
		if (tickSize == 0) {
			return;
		}

		double x, y, length;
		int locationType;
		length = getAxisLength();
		Color c;
		ArrayList<ICoor> coorList = e.getCoorList();
		Point2D p;
		Font font;
		if (allowLabels) {
			if (labelColor == null) {
				c = axisColor;
			} else {
				c = labelColor;
			}
			font = Utils.getFont(labelFont, labelStyle, labelSize);
			switch (location) {
			case Consts.AXIS_LOC_H:
			case Consts.AXIS_LOC_POLAR:
				for (int t = 1; t <= tickSize; t++) {
					if ((t - 1) % labelStep != 0) {
						continue;
					}
					x = getTickPosition(t_coorValue.get(t));
					y = getBottomY() + coorThick;
					p = new Point2D.Double(x, y);
					locationType = adjustLabelPosition(p);
					String txt = getCoorText(t_coorValue.get(t));
					Utils.drawText(e, txt, p.getX(), p.getY(), font, c,
							labelStyle, labelAngle, locationType,
							labelOverlapping);
				}
				break;
			case Consts.AXIS_LOC_V:
				for (int t = 1; t <= tickSize; t++) {
					if ((t - 1) % labelStep != 0) {
						continue;
					}
					x = getLeftX() - coorThick;
					y = getTickPosition(t_coorValue.get(t));
					p = new Point2D.Double(x, y);
					locationType = adjustLabelPosition(p);
					String txt = getCoorText(t_coorValue.get(t));
					Utils.drawText(e, txt, p.getX(), p.getY(), font, c,
							labelStyle, labelAngle, locationType,
							labelOverlapping);
				}
				break;
			case Consts.AXIS_LOC_ANGLE:
				for (int i = 0; i < coorList.size(); i++) {
					ICoor coor = coorList.get(i);
					if (coor.isCartesianCoor()) {
						continue;
					}
					PolarCoor pc = (PolarCoor) coor;
					if (pc.getAngleAxis() != this) {
						continue;
					}
					TickAxis polarAxis = (TickAxis) pc.getPolarAxis();
					double angle, polarLen;
					polarLen = polarAxis.getAxisLength();
					int tCount = t_coorValue.length();
					for (int t = 1; t <= tCount; t++) {
						if ((t - 1) % labelStep != 0) {
							continue;
						}
						Object tickVal = t_coorValue.get(t);
						angle = getTickPosition(tickVal);
						p = new Point2D.Double(polarLen, angle);
						locationType = adjustLabelPosition(p);
						p = pc.getScreenPoint(p);
						String txt = getCoorText(tickVal);
						Utils.drawText(e, txt, p.getX(), p.getY(), font, c,
								labelStyle, labelAngle, locationType,
								labelOverlapping);
					}
				}
				break;
			}
		}

		// Draw axis title
		if (!StringUtils.isValidString(title)) {
			return;
		}
		font = Utils.getFont(titleFont, titleStyle, titleSize);
		if (titleColor == null) {
			c = axisColor;
		} else {
			c = titleColor;
		}
		double tmp;
		switch (location) {
		case Consts.AXIS_LOC_H:
		case Consts.AXIS_LOC_POLAR:
			x = getLeftX() + length / 2;
			tmp = maxLabelHeight() + coorThick;
			y = getBottomY();
			if (scalePosition == Consts.TICK_RIGHTUP) {
				y -= scaleLength;
				y -= labelIndent * 2; // �������ǩ֮��Ҳ�ճ�indent
				y -= tmp;
				y -= titleIndent;
				locationType = Consts.LOCATION_CB;
			} else if (this.scalePosition == Consts.TICK_LEFTDOWN) {
				y += scaleLength;
				y += labelIndent * 2;
				y += tmp;
				y += titleIndent;
				locationType = Consts.LOCATION_CT;
			} else { // ������޿̶�ʱ������Ĭ�ϻ����±ߣ��±���ã�
				y += scaleLength / 2;
				y += labelIndent * 2;
				y += tmp;
				y += titleIndent;
				locationType = Consts.LOCATION_CT;
			}
			Utils.drawText(e, title, x, y, font, c, titleStyle, titleAngle,
					locationType, true);
			break;
		case Consts.AXIS_LOC_V:
			tmp = maxLabelWidth();
			x = getLeftX();
			y = getTopY() + length / 2;
			if (scalePosition == Consts.TICK_RIGHTUP) {
				x += scaleLength;
				x += labelIndent * 2;
				x += tmp;
				x += titleIndent;
				locationType = Consts.LOCATION_LM;
			} else if (this.scalePosition == Consts.TICK_LEFTDOWN) {
				x -= scaleLength;
				x -= labelIndent * 2;
				x -= tmp;
				x -= titleIndent;
				locationType = Consts.LOCATION_RM;
			} else { // ������޿̶�ʱ������Ĭ�ϻ�����ߣ������ã�
				x -= scaleLength / 2;
				x -= labelIndent * 2;
				x -= tmp;
				x -= titleIndent;
				locationType = Consts.LOCATION_RM;
			}
			Utils.drawText(e, title, x, y, font, c, titleStyle, titleAngle,
					locationType, true);
			break;
		case Consts.AXIS_LOC_ANGLE:
			// ������ⲻ����
			break;
		}
	}

	String getCoorText(Object coorValue) {
		return coorValue.toString();
	}

	private int maxLabelSize(boolean getHeight) {
		if (!allowLabels) {
			return 0;
		}
		int size = t_coorValue.length();
		Graphics2D g = e.getGraphics();
		int max = 0;
		for (int i = 1; i <= size; i++) {
			Object coory = t_coorValue.get(i);
			if (coory == null) {
				continue;
			}
			String txt = getCoorText(coory);
			Font font = Utils.getFont(labelFont, labelStyle, labelSize);
			Rectangle rect = Utils.getTextSize(txt, g, labelStyle, labelAngle,
					font);
			if (getHeight) {
				if (rect.height > max) {
					max = rect.height;
				}
			} else {
				if (rect.width > max) {
					max = rect.width;
				}
			}
		}
		return max;
	}

	/**
	 * ��ȡ�̶ȱ�ǩ�ĸ߶�
	 * @return ���ر�ǩ����ߵ�ֵ
	 */
	public int maxLabelHeight() {
		return maxLabelSize(true);
	}

	/**
	 * ��ȡ�̶ȱ�ǩ�Ŀ��
	 * @return ���ر�ǩ������ֵ
	 */
	public int maxLabelWidth() {
		return maxLabelSize(false);
	}

	/**
	 * ��ȡ��ͼ�ο��㣨���㣩������
	 * Ĭ������£�����Ϊ��������µ㣬Ŀǰ��֧������ֵ���������Լ�����������Ρ�
	 * ��ֵ�ᣨNumericAxis���Ļ���ᱻ�û����ã���ֵ��ʱ���Ǹ÷�����
	 * ����û�л���
	 */
	public Point2D getBasePoint(ICoor coor) {
		TickAxis otherAxis;
		if (coor.getAxis1() == this) {
			otherAxis = coor.getAxis2();
		} else {
			otherAxis = coor.getAxis1();
		}
		switch (location) {
		case Consts.AXIS_LOC_H:
			return new Point2D.Double(getLeftX(), otherAxis.getBottomY());
		case Consts.AXIS_LOC_POLAR:
			return new Point2D.Double(getLeftX(), getBottomY());
		case Consts.AXIS_LOC_V:
			return new Point2D.Double(otherAxis.getLeftX(), getBottomY());
		}
		return null;
	}

	transient Sequence t_coorValue = new Sequence();
	transient int t_coorWidth = 0;
	transient Engine e;
	transient double coorThick = 0;// ȱʡ3D����̨�ĺ��
	transient boolean useGradient = false;// �����Ը������ϵ�ͼԪ�Ƿ�ʹ���˽���ɫ������һЩ��Ч��������3Dƽ̨

	/**
	 * ����ͼ������
	 * @param e ͼ������
	 */
	public void setEngine(Engine e) {
		this.e = e;
		// Ŀǰ��������û��֧��Para����ģ�Ҳ����֧������ʹ��ӳ����
	}

	/**
	 * ��ȡͼ������
	 * @return ͼ������
	 */
	public Engine getEngine() {
		return e;
	}

	/**
	 * �Ƚ��������Ƿ����
	 * ����ͼ���������������Ψһ�ģ�
	 * ���ԣ��÷����н����������ж��Ƿ���ȣ������رȽ���ϸ���ԡ�
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		String otherName = ((IAxis) obj).getName();
		return otherName.equals(name);
	}

	/**
	 * ��ȡ��������������
	 * 
	 * @return Shape �÷��������壬����null
	 */
	public ArrayList<Shape> getShapes() {
		return null;
	}

	/**
	 * ��ȡ������
	 * 
	 * @return �÷��������壬����null
	 */
	public ArrayList<String> getLinks() {
		return null;
	}

	public abstract boolean isEnumAxis();

	public abstract boolean isDateAxis();

	public abstract boolean isNumericAxis();
	public abstract void checkDataMatch(Sequence data);

}
