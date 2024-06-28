package com.scudata.chart;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Writer;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.*;
import java.io.InputStream;
import java.lang.reflect.*;
import java.text.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import javax.swing.ImageIcon;

import com.scudata.cellset.graph.draw.Desc3DRect;
import com.scudata.chart.element.*;
import com.scudata.chart.resources.ChartMessage;
import com.scudata.common.*;
import com.scudata.dm.*;
import com.scudata.expression.*;
import com.scudata.util.*;

/**
 * ��ͼ��һЩ�������÷���
 * ���Ƿ����������1.2.3����ģ���ΪͬһͼԪ��Ҫ���������Ƶķֽ⶯��
 * ��ÿһ����������Ҫ���϶����ķ����ɫ
 * ����1Ϊͼ�����Ӱ����drawBack�е��ã�
 * ����2Ϊͼ������ɫ����draw�е��ã�
 * ����3Ϊͼ��ı߿���drawFore�е��á� 
 * �������ֵķ�����Ϊ�򵥺���������Ҫ�ֲ���ͼ���ҷ�����ϲ�����Ѿ�����
 * 
 */

public class Utils {
	public static int SHADE_SPAN = 3;
	private static ArrayList globalFonts = new ArrayList();
	private static int[] shadowColors = { 203, 208, 198, 201, 205, 196, 181,
			186, 178, 155, 158, 151, 135, 138, 131 };
	static MessageManager mm = ChartMessage.get();

	private static Shape getLine1ShapeArea(double shift, Point2D b, Point2D e,
			float weight) {
		double x1 = b.getX() + shift;// SHADE_SPAN;
		double y1 = b.getY() + shift;// SHADE_SPAN;
		double x2 = e.getX() + shift;// SHADE_SPAN;
		double y2 = e.getY() + shift;// SHADE_SPAN;
		// �����ͼ�ο���д���±� �� ֱ����Ӱ����ͼ�� ҳ
		// Point2D P1 = new Point2D.Double(x1, y1);
		// Point2D P2 = new Point2D.Double(x2, y2);
		double P2e = Math.abs(y2 - y1);
		double P1e = Math.abs(x2 - x1);
		double P1P2 = Math.sqrt(P2e * P2e + P1e * P1e);
		double AP1 = weight / 2f;
		double sina2 = P1e / P1P2;
		double Af = AP1 * sina2;
		double cosa2 = P2e / P1P2;
		double fP1 = AP1 * cosa2;
		Point2D A = new Point2D.Double(x1 - fP1, y1 - Af);
		Point2D D = new Point2D.Double(x1 + fP1, y1 + Af);
		Point2D B = new Point2D.Double(x2 - fP1, y2 - Af);
		Point2D C = new Point2D.Double(x2 + fP1, y2 + Af);
		Shape polygon = newPolygon2DShape(
				new double[] { A.getX(), B.getX(), C.getX(), D.getX() },
				new double[] { A.getY(), B.getY(), C.getY(), D.getY() });
		return polygon;
	}

	/**
	 * ��ֱ�ߵ���Ӱ
	 * @param g ͼ���豸
	 * @param b �߶����
	 * @param e �߶��յ�
	 * @param style �ߵķ��
	 * @param weight �ߵĴֶ�
	 * @return �߶εĳ�������״
	 */
	public static Shape drawLine1(Graphics2D g, Point2D b, Point2D e,
			int style, float weight) {
		if (b == null || e == null) {
			return null;
		}
		double shift = weight / 2 + 1;
		if (shift < 2)
			shift = 2;
		Shape s = getLine1ShapeArea(shift, b, e, weight);

		double x1 = b.getX() + shift;// SHADE_SPAN;
		double y1 = b.getY() + shift;// SHADE_SPAN;
		double x2 = e.getX() + shift;// SHADE_SPAN;
		double y2 = e.getY() + shift;// SHADE_SPAN;
		double P2e = Math.abs(y2 - y1);
		double P1e = Math.abs(x2 - x1);
		double P1P2 = Math.sqrt(P2e * P2e + P1e * P1e);
		double AP1 = weight / 2f;
		double sina2 = P1e / P1P2;
		double Af = AP1 * sina2;
		double cosa2 = P2e / P1P2;
		double fP1 = AP1 * cosa2;
		Point2D A = new Point2D.Double(x1 - fP1, y1 - Af);
		Point2D D = new Point2D.Double(x1 + fP1, y1 + Af);

		Color bright = new Color(243, 248, 239);
		Color dark = getShadeColor(1);
		GradientPaint gp = new GradientPaint(A, dark, D, bright);
		g.setPaint(gp);
		setTransparent(g, 0.8f);
		g.fill(s);
		setTransparent(g, 1);

		return getLine1ShapeArea(0, b, e, weight);// ����û��λ�ƵĶ����ֱ��״����
	}

	/**
	 * ����һ��ֱ��
	 * @param g ͼ���豸
	 * @param b �ߵ����
	 * @param e �ߵ��յ�
	 * @param color ��ɫ
	 * @param style ����
	 * @param weight �ֶ�
	 */
	public static void drawLine2(Graphics2D g, Point2D b, Point2D e,
			Color color, int style, float weight) {
		if (b == null || e == null) {
			return;
		}
		if (setStroke(g, color, style, weight)) {
			Utils.drawLine(g, b, e);
		}
	}

	public static String xToChinese(double dd) {
		try {
			String s = "��Ҽ��������½��ƾ�";
			// String s1 = "ʰ��Ǫ��ʰ��Ǫ��ʰ��Ǫ��";
			String s1 = "ʮ��ǧ��ʮ��ǧ��ʮ��ǧ��";
			String m;
			int j;
			StringBuffer k = new StringBuffer();
			m = String.valueOf(Math.round(dd));
			for (j = m.length(); j >= 1; j--) {
				char n = s.charAt(Integer.parseInt(m.substring(m.length() - j,
						m.length() - j + 1)));
				if (n == '��' && k.charAt(k.length() - 1) == '��') {
					continue;
				}
				k.append(n);
				if (n == '��') {
					continue;
				}
				int u = j - 2;
				if (u >= 0) {
					k.append(s1.charAt(u));
				}
				if (u > 3 && u < 7) {
					k.append('��');
				}
				if (u > 7) {
					k.append('��');
				}
			}
			if (k.length() > 0 && k.charAt(k.length() - 1) == '��') {
				k.deleteCharAt(k.length() - 1);
			}
			if (k.length() > 0 && k.charAt(0) == 'Ҽ') {
				k.deleteCharAt(0);
			}
			return k.toString();
		} catch (Exception x) {
			NumberFormat df = new DecimalFormat("###,#.#");
			return df.format(dd);
		}
	}

	/**
	 * ���б��������������
	 * @param list �б����ݣ�Ҫ��ֵΪ�ɱȽϵ�
	 * @param ascend �Ƿ�����
	 * @return ������ɷ���true�����򷵻�false
	 */
	public static boolean sort(AbstractList list, boolean ascend) {
		Comparable ci, cj;
		int i, j;
		boolean lb_exchange;
		for (i = 0; i < list.size(); i++) {
			Object o = list.get(i);
			if (o != null && !(o instanceof Comparable)) {
				return false;
			}
		}

		for (i = 0; i < list.size() - 1; i++) {
			for (j = i + 1; j < list.size(); j++) {
				ci = (Comparable) list.get(i);
				cj = (Comparable) list.get(j);
				if (ascend) {
					if (ci == null || cj == null) {
						lb_exchange = (cj == null);
					} else {
						lb_exchange = ci.compareTo(cj) > 0;
					}
				} else {
					if (ci == null || cj == null) {
						lb_exchange = (ci == null);
					} else {
						lb_exchange = ci.compareTo(cj) < 0;
					}
				}
				if (lb_exchange) {
					Object o, o2;
					o = list.get(i);
					o2 = list.get(j);
					list.set(i, o2);
					list.set(j, o);
				}
			}
		}
		return true;
	}

	/**
	 * �����ĵ� cx,cy��c1��c2����ɫ��״��ɢ�ݶ������״s
	 * 
	 * @param s
	 *            Shape
	 * @param c1
	 *            Color
	 * @param c2
	 *            Color
	 */
	public static synchronized void fillRadioGradientShape(Graphics2D g,
			Shape s, Color c1, Color c2, float transparent) {
		// �򵥻�ɫ������
		setTransparent(g, transparent);
		g.setColor(Utils.getShadeColor(1));
		g.fill(s);
		setTransparent(g, 1);
	}

	/**
	 * ���㷨ʹ�þ��εĶԽ���Ϊ����ݶȵĳ��ȣ��Ƕ�angleΪ�Խ��ߴӵױ���ʱ�����ת�Ƕȣ�
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param color1
	 * @param color2
	 * @param angle
	 * @return
	 */
	private static synchronized Paint getGradientPaint(double x, double y, double width,
			double height, Color color1, Color color2, int angle) {
		double x1 = 0, y1 = 0, x2 = 0, y2 = 0, h = 0;
		h = height;
		double antiAngleLen = Math.sqrt(width * width + h * h);// �Խ��߳���
		double rad = angle * Math.PI / 180;// �ǶȵĻ�����
		if (angle >= 0 && angle <= 90) {
			if (angle == 0) {
				x1 = x;
				y1 = y + h / 2;
				x2 = x + width;
				y2 = y1;
			} else if (angle == 90) {
				x1 = x + width / 2;
				y1 = y + h;
				x2 = x1;
				y2 = y;
			} else {
				x1 = x;
				y1 = y + h;
				x2 = x1 + antiAngleLen * Math.cos(rad);
				y2 = y1 - antiAngleLen * Math.sin(rad);
			}
		} else if (angle > 90 && angle <= 180) {
			if (angle == 180) {
				x1 = x + width;
				y1 = y + h / 2;
				x2 = x;
				y2 = y1;
			} else {
				x1 = x + width;
				y1 = y + h;
				x2 = x1 + antiAngleLen * Math.cos(rad);
				y2 = y1 - antiAngleLen * Math.sin(rad);
			}
		} else if (angle > 180 && angle <= 270) {
			if (angle == 270) {
				x1 = x + width / 2;
				y1 = y;
				x2 = x1;
				y2 = y + h;
			} else {
				x1 = x + width;
				y1 = y;
				x2 = x1 + antiAngleLen * Math.cos(rad);
				y2 = y1 - antiAngleLen * Math.sin(rad);
			}
		} else if (angle > 270 && angle <= 360) {
			if (angle == 360) {
				x1 = x;
				y1 = y + h / 2;
				x2 = x + width;
				y2 = y1;
			} else {
				x1 = x;
				y1 = y;
				x2 = x1 + antiAngleLen * Math.cos(rad);
				y2 = y1 - antiAngleLen * Math.sin(rad);
			}
		}

		return new GradientPaint((int)x1, (int)y1, color1, (int)x2, (int)y2, color2, false);
	}

	/**
	 * ���û�ͼ�������
	 * @param g ͼ���豸
	 * @param x ����x����
	 * @param y ����y����
	 * @param width ���Ŀ��
	 * @param height ���ĸ߶�
	 * @param cc ������������ �����ɫ��
	 * @return ���������ɷ���true�����򷵻�false
	 */
	public static boolean setPaint(Graphics2D g, double x, double y, double width,
			double height, ChartColor cc) {
		Rectangle2D.Double rect = null;
		BufferedImage tempbi = null;
		Graphics2D tempG = null;
		Paint paint = null;
		int pattern = cc.getType();
		Color c1 = cc.getColor1();
		if (c1 == null)
			return false;
		Color c2 = cc.getColor2();
		if (c2 == null)
			return false;

		switch (pattern) {
		case Consts.PATTERN_DEFAULT: // ���ͼ����ȫ���
			if (cc.isGradient()) {
				paint = getGradientPaint(x, y, width, height, c1, c2,
						cc.getAngle());
			} else {
				g.setColor(c1);
				return true;
			}
			break;
		case Consts.PATTERN_H_THIN_LINE: // ���ͼ����ˮƽϸ��
			rect = new Rectangle2D.Double(x + 1, y + 1, 6, 6);
			tempbi = new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 6, 6);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(0.1f));
			tempG.drawLine(0, 1, 6, 1);
			tempG.drawLine(0, 3, 6, 3);
			tempG.drawLine(0, 5, 6, 5);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_H_THICK_LINE: // ���ͼ����ˮƽ����
			rect = new Rectangle2D.Double(x + 1, y + 1, 6, 6);
			tempbi = new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 6, 6);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.5f));
			tempG.drawLine(0, 2, 6, 2);
			tempG.drawLine(0, 5, 6, 5);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_V_THIN_LINE: // ���ͼ������ֱϸ��
			rect = new Rectangle2D.Double(x + 1, y + 1, 6, 6);
			tempbi = new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 6, 6);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(0.1f));
			tempG.drawLine(1, 0, 1, 6);
			tempG.drawLine(3, 0, 3, 6);
			tempG.drawLine(5, 0, 5, 6);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_V_THICK_LINE: // ���ͼ������ֱ����
			rect = new Rectangle2D.Double(x + 1, y + 1, 6, 6);
			tempbi = new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 6, 6);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.5f));
			tempG.drawLine(2, 0, 2, 6);
			tempG.drawLine(5, 0, 5, 6);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_THIN_SLASH: // ���ͼ����ϸб��
			rect = new Rectangle2D.Double(x + 1, y + 1, 3, 3);
			tempbi = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 3, 3);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(0.1f));
			tempG.drawLine(0, 0, 3, 3);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_THICK_SLASH: // ���ͼ������б��
			rect = new Rectangle2D.Double(x + 1, y + 1, 4, 4);
			tempbi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			setGraphAntiAliasingOn(tempG);
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 4, 4);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, null, 0.0f));
			tempG.drawLine(0, 0, 4, 4);
			tempG.drawLine(3, -1, 5, 1);
			tempG.drawLine(-1, 3, 1, 5);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_THIN_BACKSLASH: // ���ͼ����ϸ��б��
			rect = new Rectangle2D.Double(x + 1, y + 1, 3, 3);
			tempbi = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 3, 3);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(0.1f));
			tempG.drawLine(2, 0, -1, 3);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_THICK_BACKSLASH: // ���ͼ�����ַ�б��
			rect = new Rectangle2D.Double(x + 1, y + 1, 4, 4);
			tempbi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			setGraphAntiAliasingOn(tempG);
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 4, 4);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, null, 0.0f));
			tempG.drawLine(4, 0, 0, 4);
			tempG.drawLine(-1, 1, 1, -1);
			tempG.drawLine(3, 5, 5, 3);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_THIN_GRID: // ���ͼ����ϸ����
			rect = new Rectangle2D.Double(x + 1, y + 1, 3, 3);
			tempbi = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 3, 3);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(0.1f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_MITER, 10.0f, null, 0.0f));
			tempG.drawLine(1, 0, 1, 3);
			tempG.drawLine(0, 1, 3, 1);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_THICK_GRID: // ���ͼ����������
			rect = new Rectangle2D.Double(x + 1, y + 1, 5, 5);
			tempbi = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 5, 5);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_MITER, 10.0f, null, 0.0f));
			tempG.drawLine(3, 0, 3, 5);
			tempG.drawLine(0, 3, 5, 3);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_THIN_BEVEL_GRID: // ���ͼ����ϸб����
			rect = new Rectangle2D.Double(x + 1, y + 1, 5, 5);
			tempbi = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 5, 5);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_MITER, 10.0f, null, 0.0f));
			tempG.drawLine(0, 0, 5, 5);
			tempG.drawLine(0, 5, 5, 0);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_THICK_BEVEL_GRID: // ���ͼ������б����
			rect = new Rectangle2D.Double(x + 1, y + 1, 6, 6);
			tempbi = new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			setGraphAntiAliasingOn(tempG);
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 6, 6);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, null, 0.0f));
			tempG.drawLine(0, 0, 6, 6);
			tempG.drawLine(0, 6, 6, 0);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_DOT_1: // ���ͼ����ϡ���
			rect = new Rectangle2D.Double(x, y, 12, 12);
			tempbi = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			setGraphAntiAliasingOn(tempG);
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 12, 12);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, null, 0.0f));
			tempG.drawLine(2, 3, 2, 3);
			tempG.drawLine(8, 9, 8, 9);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_DOT_2: // ���ͼ������ϡ��
			rect = new Rectangle2D.Double(x, y, 12, 12);
			tempbi = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 12, 12);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, null, 0.0f));
			tempG.drawLine(2, 3, 2, 3);
			tempG.drawLine(6, 11, 6, 11);
			tempG.drawLine(10, 7, 10, 7);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_DOT_3: // ���ͼ�������ܵ�
			rect = new Rectangle2D.Double(x, y, 9, 9);
			tempbi = new BufferedImage(9, 9, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			setGraphAntiAliasingOn(tempG);
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 9, 9);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, null, 0.0f));
			tempG.drawLine(2, 2, 2, 2);
			tempG.drawLine(5, 8, 5, 8);
			tempG.drawLine(8, 5, 8, 5);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_DOT_4: // ���ͼ�������ܵ�
			rect = new Rectangle2D.Double(x, y, 4, 4);
			tempbi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 4, 4);
			tempG.setColor(c2);
			tempG.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, null, 0.0f));
			tempG.drawLine(1, 3, 1, 3);
			tempG.drawLine(3, 1, 3, 1);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_SQUARE_FLOOR: // ���ͼ����������ذ�ש
			rect = new Rectangle2D.Double(0, 0, 8, 8);
			tempbi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 8, 8);
			tempG.setColor(c2);
			tempG.fillRect(0, 0, 4, 4);
			tempG.fillRect(4, 4, 4, 4);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_DIAMOND_FLOOR: // ���ͼ�������εذ�ש
			rect = new Rectangle2D.Double(x + 1, y + 1, 8, 8);
			tempbi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 8, 8);
			tempG.setColor(c2);
			int[] xs = { 4, 0, 4, 8 };
			int[] ys = { 0, 4, 8, 4 };
			tempG.fillPolygon(xs, ys, 4);
			paint = new TexturePaint(tempbi, rect);
			break;
		case Consts.PATTERN_BRICK_WALL: // ���ͼ����שǽ
			rect = new Rectangle2D.Double(x + 1, y + 1, 12, 12);
			tempbi = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
			tempG = (Graphics2D) tempbi.getGraphics();
			tempG.setColor(c1);
			tempG.fillRect(0, 0, 12, 12);
			tempG.setStroke(new BasicStroke(0.1f));
			tempG.setColor(c2);
			tempG.drawLine(0, 0, 12, 0);
			tempG.drawLine(0, 3, 12, 3);
			tempG.drawLine(0, 6, 12, 6);
			tempG.drawLine(0, 9, 12, 9);
			tempG.drawLine(2, 0, 2, 3);
			tempG.drawLine(8, 3, 8, 6);
			tempG.drawLine(2, 6, 2, 9);
			tempG.drawLine(8, 9, 8, 12);
			paint = new TexturePaint(tempbi, rect);
			break;
		}
		g.setPaint(paint);
		if(tempG!=null){
			tempG.dispose();
		}
		return true;
	}

	private static ArrayList solidStrokes = null;
	private static ArrayList dashedStrokes = null;
	private static ArrayList dottedStrokes = null;
	private static ArrayList dotdashStrokes = null;

	private static ArrayList doubleStrokes = null;

	/**
	 * ���û���ֱ�ߵķ��
	 * @param g
	 *            Graphics2D
	 * @param c
	 *            Color,Ϊnull�����ã���ʾ�õ�ǰ����ɫ ��ע��null�����Ǳ�ʾ��ǰ������ɫ����͸��ɫ��
	 * @param style ���
	 * @param weight �ֶ�
	 * @return boolean ���óɹ�����true�����򷵻�false
	 */
	public static boolean setStroke(Graphics2D g, Color c, int style,
			float weight) {
		float w2 = weight;
		if (w2 == 0) {
			return false;
		}
		if (w2 < 1) {
			w2 = 1;
		}
		if (c != null) {
			g.setColor(c);
		}
		style = style & 0x0f;
		ListIterator li = null;
		BasicStroke stroke;
		switch (style) {
		case Consts.LINE_SOLID:
			if (solidStrokes == null) {
				solidStrokes = new ArrayList();
			} else {
				li = solidStrokes.listIterator();
				while (li.hasNext()) {
					BasicStroke bs = (BasicStroke) li.next();
					if (bs.getLineWidth() == weight) {
						g.setStroke(bs);
						return true;
					}
				}
			}
			stroke = new BasicStroke(weight);
			g.setStroke(stroke);
			solidStrokes.add(stroke);
			break;
		case Consts.LINE_DASHED:
			if (dashedStrokes == null) {
				dashedStrokes = new ArrayList();
			} else {
				li = dashedStrokes.listIterator();
				while (li.hasNext()) {
					BasicStroke bs = (BasicStroke) li.next();
					if (bs.getLineWidth() == weight) {
						g.setStroke(bs);
						return true;
					}
				}
			}
			float[] dashes1 = { 6 * w2, 6 * w2 };
			stroke = new BasicStroke(weight, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, dashes1, 0.0f);
			g.setStroke(stroke);
			dashedStrokes.add(stroke);
			break;
		case Consts.LINE_DOTTED:
			if (dottedStrokes == null) {
				dottedStrokes = new ArrayList();
			} else {
				li = dottedStrokes.listIterator();
				while (li.hasNext()) {
					BasicStroke bs = (BasicStroke) li.next();
					if (bs.getLineWidth() == weight) {
						g.setStroke(bs);
						return true;
					}
				}
			}
			float[] dashes2 = { w2, 3 * w2 };
			stroke = new BasicStroke(weight, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, dashes2, 0.0f);
			g.setStroke(stroke);
			dottedStrokes.add(stroke);
			break;
		case Consts.LINE_DOTDASH:
			if (dotdashStrokes == null) {
				dotdashStrokes = new ArrayList();
			} else {
				li = dotdashStrokes.listIterator();
				while (li.hasNext()) {
					BasicStroke bs = (BasicStroke) li.next();
					if (bs.getLineWidth() == weight) {
						g.setStroke(bs);
						return true;
					}
				}
			}
			float[] lp1 = { 6 * w2, 2 * w2, w2, 2 * w2 };
			stroke = new BasicStroke(weight, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, lp1, 0.0f);
			g.setStroke(stroke);
			dotdashStrokes.add(stroke);
			break;
		case Consts.LINE_DOUBLE:
			if (doubleStrokes == null) {
				doubleStrokes = new ArrayList();
			} else {
				li = doubleStrokes.listIterator();
				while (li.hasNext()) {
					BasicStroke bs = (BasicStroke) li.next();
					if (bs.getLineWidth() == weight) {
						g.setStroke(bs);
						return true;
					}
				}
			}
			float[] dashes3 = { w2, 2 * w2, w2, 2 * w2, 8 * w2, 2 * w2 };
			stroke = new BasicStroke(weight, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL, 10.0f, dashes3, 0.0f);
			g.setStroke(stroke);
			doubleStrokes.add(stroke);
			break;
		default:

			// stroke = new BasicStroke(0f);
			return false;
		}
		g.setStroke(stroke);
		return true;
	}

	public static int getArrow(int style) {
		return style & 0xF00;
	}

	/**
	 * �Ի��ȽǶ�radian����һ����ͷ��ʼ��dx,dy�ļ�ͷ
	 * @param g ͼ���豸
	 * @param dx ʵ�����ȵ�x����
	 * @param dy y����
	 * @param radian �����ƵĽǶ�ֵ
	 * @param style ��ͷ���
	 */
	public static void drawLineArrow(Graphics2D g, double dx, double dy,
			double radian, int style) {
		style = getArrow(style);
		if (style == Consts.LINE_ARROW_NONE) {
			return;
		}
		int x = (int) dx;
		int y = (int) dy;
		AffineTransform at = g.getTransform();
		AffineTransform at1 = AffineTransform.getRotateInstance(radian, x, y);
		g.transform(at1);
		switch (style) {
		case Consts.LINE_ARROW_NONE: // �޼�ͷ
			break;
		case Consts.LINE_ARROW:
			x += 8;
			int[] xs_arr = { x - 8, x - 12, x, x - 12 };
			int[] ys_arr = { y, y - 4, y, y + 4 };
			g.fillPolygon(xs_arr, ys_arr, 4);
			break;
		case Consts.LINE_ARROW_L://���ͷ
			x -= 8;
			int[] xl_arr = { x, x + 12, x+8, x + 12 };
			int[] yl_arr = { y, y - 4, y, y + 4 };
			g.fillPolygon(xl_arr, yl_arr, 4);
			break;
		case Consts.LINE_ARROW_BOTH: // ˫��ͷ
			x += 8;
			int[] xs_bot = { x - 8, x - 12, x, x - 12 };
			int[] ys_bot = { y, y - 4, y, y + 4 };
			g.fillPolygon(xs_bot, ys_bot, 4);
			int[] xs_bot2 = { x - 14, x - 18, x - 6, x - 18 };
			int[] ys_bot2 = { y, y - 4, y, y + 4 };
			g.fillPolygon(xs_bot2, ys_bot2, 4);
			break;
		case Consts.LINE_ARROW_HEART: // ���μ�ͷ
			int r_h = 4;
			int cdy = (int) (r_h * 1.732d);
			int cdx = 3 * r_h;
			x += cdx;
			int[] xs_heart = { x - cdx, x - cdx, x };
			int[] ys_heart = { y - cdy, y + cdy, y };
			g.fillPolygon(xs_heart, ys_heart, 3);
			int d2y = (int) (r_h * 1.732d / 2);
			int d2x = (int) 3.5 * r_h;
			g.fillOval(x - d2x - r_h, y - d2y - r_h, 2 * r_h - 1, 2 * r_h - 1);
			g.fillOval(x - d2x - r_h, y + d2y - r_h, 2 * r_h - 1, 2 * r_h - 1);
			// ��ν���Σ�ʵ���Ͼ���һ�������μ����������������е�Բ������������������������
			break;
		case Consts.LINE_ARROW_CIRCEL: // Բ�μ�ͷ
			x += 8;
			g.fillOval(x - 8, y - 4, 8, 8);
			break;
		case Consts.LINE_ARROW_DIAMOND: // ���μ�ͷ
			x += 14;
			int[] xs_dia = { x - 14, x - 7, x, x - 7 };
			int[] ys_dia = { y, y - 4, y, y + 4 };
			g.fillPolygon(xs_dia, ys_dia, 4);
			break;
		}
		g.setTransform(at);
	}

	/**
	 * ����fontStyle�ж��Ƿ�Ϊ��������
	 * @param fontStyle ������
	 * @return ���ŷ���true�����򷵻�false
	 */
	public static boolean isVertical(int fontStyle) {
		return (fontStyle & Consts.FONT_VERTICAL) != 0;
	}

	/**
	 * ����fontStyle�ж��Ƿ�Ϊ�»�������
	 * @param fontStyle ������
	 * @return ���»��߷���true�����򷵻�false
	 */
	public static boolean isUnderline(int fontStyle) {
		return (fontStyle & Consts.FONT_UNDERLINE) != 0;
	}

	/**
	 * ����fontStyle�ж��Ƿ�Ϊ��������
	 * @param fontStyle ������
	 * @return �д��巵��true�����򷵻�false
	 */
	public static boolean isBold(int fontStyle) {
		return (fontStyle & Consts.FONT_BOLD) != 0;
	}

	/**
	 * ��ȡ�ı�ռ�õĿ�ߣ�ʹ��Rectangle�Ŀ�͸ߴ洢��Ϣ
	 * @param text Ҫ�������ı�
	 * @param g ͼ���豸
	 * @param fontStyle ���
	 * @param angle �ı�����ת�Ƕ�
	 * @param font ����
	 * @return ���δ洢�Ŀ����Ϣ
	 */
	public static Rectangle getTextSize(String text, java.awt.Graphics g,
			int fontStyle, int angle, Font font) {
		boolean vertical = isVertical(fontStyle);
		return getTextSize(text, g, vertical, angle, font);
	}

	/**
	 * ��ȡ�ı�ռ�õĿ�ߣ�ʹ��Rectangle�Ŀ�͸ߴ洢��Ϣ
	 * @param text Ҫ�������ı�
	 * @param g ͼ���豸
	 * @param vertical �Ƿ�ֱ����
	 * @param angle ��ת�Ƕ�
	 * @param font ����
	 * @return ���δ洢�Ŀ����Ϣ
	 */
	public static Rectangle getTextSize(String text, java.awt.Graphics g,
			boolean vertical, int angle, Font font) {
		if (text == null) {
			return new Rectangle();
		}
		Rectangle rect = null;
		if (vertical) {
			rect = getVerticalArea(text, g, angle, font);
		} else if (angle == 0) {
			rect = getHorizonArea(text, g, font);
		} else {
			rect = getRotationArea(text, g, angle, font);
		}
		// added by bdl, 2009.5.8, ����ת�ǶȲ�Ϊ0ʱ��rect�Ŀ�Ȼ��߸߶��п��ܲ�����ֵ�������յȲ��������鷳��ȫ��Ϊ��ֵ
		if (rect.width < 0) {
			rect.width = -rect.width;
		}
		if (rect.height < 0) {
			rect.height = -rect.height;
		}
		return rect;
	}

	private static String getChar(String text) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c > 255) {
				return "��";
			}
		}
		return "A";
	}

	/**
	 * �ı������Ű�ʱ�Ŀ��
	 * @param text Ҫ�������ı�
	 * @param g ͼ���豸
	 * @param angle ��ת�Ƕ�
	 * @param font ����
	 * @return ���δ洢�Ŀ����Ϣ
	 */
	public static Rectangle getVerticalArea(String text, java.awt.Graphics g,
			int angle, Font font) {
		if (!StringUtils.isValidString(text)) {
			text = "A";
		}
		Rectangle area = new Rectangle();
		if (angle == 0) {
			FontMetrics fm = g.getFontMetrics(font);
			int hh = fm.getAscent();
			area.width = fm.stringWidth(getChar(text));
			area.height = hh * text.length();
		} else {
			angle = angle % 360;
			if (angle < 0) {
				angle += 360;
			}
			Rectangle area0 = getVerticalArea(text, g, 0, font);
			double sin = Math.sin(angle * Math.PI / 180);
			double cos = Math.cos(angle * Math.PI / 180);
			if (sin < 0) {
				sin = -sin;
			}
			if (cos < 0) {
				cos = -cos;
			}
			int aw = (int) (area0.height * sin + area0.width * cos);
			int ah = (int) (area0.width * sin + area0.height * cos);
			area.width = aw;
			area.height = ah;
		}
		return area;
	}

	/**
	 * �ı�ˮƽ�Ű�ʱ�Ŀ��
	 * @param text Ҫ�������ı�
	 * @param g ͼ���豸
	 * @param font ����
	 * @return ���δ洢�Ŀ����Ϣ
	 */
	public static Rectangle getHorizonArea(String text, java.awt.Graphics g,
			Font font) {
		Rectangle area = new Rectangle();
		FontMetrics fm = g.getFontMetrics(font);
		int hw = fm.stringWidth(text);
		int hh = fm.getAscent() + fm.getDescent(); // .getAscent();
		area.width = hw;
		area.height = hh;
		return area;
	}

	/**
	 * �ı���ת�Ƕ��Ű�ʱռ�õĿ��
	 * @param text Ҫ�������ı�
	 * @param g ͼ���豸
	 * @param angle ��ת�Ƕ�
	 * @param font ����
	 * @return ���δ洢�Ŀ����Ϣ
	 */
	public static Rectangle getRotationArea(String text, java.awt.Graphics g,
			int angle, Font font) {
		Rectangle area = new Rectangle();
		angle = angle % 360;
		if (angle < 0) {
			angle += 360;
		}
		Rectangle area0 = getTextSize(text, g, 0, 0, font);
		double sin = Math.sin(angle * Math.PI / 180);
		double cos = Math.cos(angle * Math.PI / 180);
		if (sin < 0) {
			sin = -sin;
		}
		if (cos < 0) {
			cos = -cos;
		}
		int aw = (int) (area0.height * sin + area0.width * cos);
		int ah = (int) (area0.width * sin + area0.height * cos);
		area.width = aw;
		area.height = ah;
		return area;
	}

	/**
	 * ����ָ��������λ�û���һ���ı�
	 * @param g ͼ���豸
	 * @param txt Ҫ���Ƶ��ı�
	 * @param dx ������x
	 * @param dy ������y
	 * @param font ����
	 * @param c ǰ��ɫ
	 * @param fontStyle ���
	 * @param angle ��ת�Ƕ�
	 * @param location ������ı����ĵ��ƫ��λ�ã�ֵ�ο�Consts.LOCATION_XX
	 */
	public static void drawText(Graphics2D g, String txt, double dx, double dy,
			Font font, Color c, int fontStyle, int angle, int location) {
		drawText(g, txt, dx, dy, font, c, null, fontStyle, angle, location);
	}

	/**
	 * ����ָ����������һ���ı�
	 * @param g ͼ���豸
	 * @param txt Ҫ���Ƶ��ı�
	 * @param dx ������x
	 * @param dy ������y
	 * @param font ����
	 * @param c ǰ��ɫ
	 * @param backC ����ɫ
	 * @param fontStyle ���
	 * @param angle ��ת�Ƕ�
	 * @param location ������ı����ĵ��ƫ��λ�ã�ֵ�ο�Consts.LOCATION_XX
	 */
	public static void drawText(Graphics2D g, String txt, double dx, double dy,
			Font font, Color c, Color backC, int fontStyle, int angle,
			int location) {
		drawText(null, txt, dx, dy, font, c, backC, fontStyle, angle, location,
				true, g);
	}

	/**
	 * ����ָ����������һ���ı�
	 * @param e ��ͼ����
	 * @param txt Ҫ���Ƶ��ı�
	 * @param dx ������x
	 * @param dy ������y
	 * @param font ����
	 * @param c ǰ��ɫ
	 * @param fontStyle ���
	 * @param angle ��ת�Ƕ�
	 * @param location ������ı����ĵ��ƫ��λ�ã�ֵ�ο�Consts.LOCATION_XX
	 * @param allowIntersect �Ƿ����������ص�(�������ص�ʱ��������ص��ı�������)
	 */
	public static void drawText(Engine e, String txt, double dx, double dy,
			Font font, Color c, int fontStyle, int angle, int location,
			boolean allowIntersect) {
		drawText(e, txt, dx, dy, font, c, null, fontStyle, angle, location,
				allowIntersect);
	}

	/**
	 * ����ָ����������һ���ı�
	 * @param e ��ͼ����
	 * @param txt Ҫ���Ƶ��ı�
	 * @param dx ������x
	 * @param dy ������y
	 * @param font ����
	 * @param c ǰ��ɫ
	 * @param fontStyle ���
	 * @param angle ��ת�Ƕ�
	 * @param location ������ı����ĵ��ƫ��λ�ã�ֵ�ο�Consts.LOCATION_XX
	 * @param allowIntersect �Ƿ����������ص�(�������ص�ʱ��������ص��ı�������)
	 */
	public static void drawText(Engine e, String txt, double dx, double dy,
			Font font, Color c, Color backC, int fontStyle, int angle,
			int location, boolean allowIntersect) {
		drawText(e, txt, dx, dy, font, c, backC, fontStyle, angle, location,
				allowIntersect, e.getGraphics());
	}

	/**
	 * ���������ı�λ��ʱ�����õ������ĵ㣻��ʵ�ʻ����ı�����ͼƬʱ��
	 * isImageʱ��Ҫ�任Ϊ���Ͻǣ� �ı�ʱ�任Ϊ���½ǣ�
	 * ��Ϊg�����½ǻ����ı��� ���Ͻǻ���ͼ�Ρ�
	 * @param posDesc �������ĵ��������ı�λ��
	 * @param location �ı�������ĵ�ķ�λ
	 * @param isImage �Ƿ����ͼ�Σ������ı��㷨
	 * @return ͼ���豸ֱ�������ʵ������
	 */
	public static Point getRealDrawPoint(Rectangle posDesc, int location,boolean isImage) {
		Rectangle rect = posDesc;
		// ��ͼ���ĵ�
		int xloc = rect.x;
		int yloc = rect.y;

		if (location == Consts.LOCATION_LT || location == Consts.LOCATION_CT
				|| location == Consts.LOCATION_RT) {
			// ���������ϱߣ���Ҫ�����½ǵ�y����
			if (isImage) {
				yloc -= rect.height;
			} else {
				yloc += rect.height;
			}
		} else if (location == Consts.LOCATION_LM
				|| location == Consts.LOCATION_CM
				|| location == Consts.LOCATION_RM) {
			// �����ο������м䣬��Ҫ�����½�y����
			if (isImage) {
				yloc -= rect.height / 2;
			} else {
				yloc += rect.height / 2;
			}
		} else {
			yloc -= 1;
		}
		if (location == Consts.LOCATION_RT || location == Consts.LOCATION_RM
				|| location == Consts.LOCATION_RB) {
			// ���������ұߣ���Ҫ�����½ǵ�x����
			xloc -= rect.width;
		} else if (location == Consts.LOCATION_CT
				|| location == Consts.LOCATION_CM
				|| location == Consts.LOCATION_CB) {
			// �����ο������м䣬��Ҫ�����½�x����
			xloc -= rect.width / 2;
		} else {
			xloc += 1;
		}
		return new Point(xloc, yloc);
	}

	/**
	 * ����ָ����������һ���ı�
	 * @param e ��ͼ����
	 * @param txt Ҫ���Ƶ��ı�
	 * @param dx ������x
	 * @param dy ������y
	 * @param font ����
	 * @param c ǰ��ɫ
	 * @param backC ����ɫ
	 * @param fontStyle ���
	 * @param angle ��ת�Ƕ�
	 * @param location ������ı����ĵ��ƫ��λ�ã�ֵ�ο�Consts.LOCATION_XX
	 * @param allowIntersect �Ƿ����������ص�(�������ص�ʱ��������ص��ı�������)
	 * @param g ͼ���豸
	 */
	public static void drawText(Engine e, String txt, double dx, double dy,
			Font font, Color c, Color backC, int fontStyle, int angle,
			int location, boolean allowIntersect, Graphics2D g) {
		if (txt == null || txt.trim().length() < 1 || font.getSize() == 0) {
			return;
		}
		int x = (int) dx;
		int y = (int) dy;
		boolean vertical = isVertical(fontStyle);
		FontMetrics fm = g.getFontMetrics(font);

		// ���ֲ��ص�
		Rectangle rect = getTextSize(txt, g, vertical, angle, font);
		rect.x = x;
		rect.y = y;
		if (e != null) {
			if (!allowIntersect && e.intersectTextArea(rect)) {
				return;
			} else {
				e.addTextArea(rect);
			}
		}

		g.setFont(font);
		g.setColor(c);

		Point drawPoint = getRealDrawPoint(rect, location, false);
		int xloc = drawPoint.x;
		int yloc = drawPoint.y;

		Utils.setGraphAntiAliasingOff(g);

		if (!vertical) {
			// ����������
			if (angle != 0) {
				AffineTransform at = g.getTransform();
				Rectangle rect2 = getTextSize(txt, g, vertical, 0, font);
				rect2.setLocation(xloc, yloc - rect2.height);
				int delx = 0, dely = 0;
				angle = angle % 360;
				if (angle < 0) {
					angle += 360;
				}
				if (angle >= 0 && angle < 90) {
					delx = 0;
					dely = (int) (rect2.width * Math.sin(angle * Math.PI / 180));
				} else if (angle < 180) {
					dely = rect.height;
					delx = (int) (rect2.width * Math.cos(angle * Math.PI / 180));
				} else if (angle < 270) {
					delx = -rect.width;
					dely = (int) (-rect2.height * Math.sin(angle * Math.PI
							/ 180));
				} else {
					dely = 0;
					delx = (int) (rect2.height * Math
							.sin(angle * Math.PI / 180));
				}
				AffineTransform at1 = AffineTransform.getRotateInstance(angle
						* Math.PI / 180, xloc - delx, yloc - dely);
				g.transform(at1);

				if (backC != null) {
					g.setColor(backC);
					g.fillRect(xloc - delx, yloc - dely - fm.getAscent(),
							rect2.width, rect2.height);
				}
				g.setColor(c);
				g.drawString(txt, xloc - delx, yloc - dely);

				g.setTransform(at);
			} else {
				if (backC != null) {
					g.setColor(backC);
					g.fillRect(xloc, yloc - fm.getAscent(), rect.width,
							rect.height);
				}

				g.setColor(c);
				g.drawString(txt, xloc, yloc);
			}
		} else {
			// �������֣����������������ת�ǶȵĻ������ڱȽ���ֵ�ʹ��
			// ����ÿ����ȥ��תȻ�����ŵĻ����п��ܲ����ڵ��������������֧������������ת�ķ�ʽ
			// �ڿ���������ת������ʱ��Ϊ��Ч�ʣ�ֻʹ�õ�һ���ַ����㣬�����Ҫ������Ӣ���ŵ����֣���ô����Ҳû�а취
			AffineTransform at = g.getTransform();
			Rectangle rect2 = getTextSize(txt, g, vertical, 0, font);
			rect2.setLocation(xloc, yloc - rect2.height);
			// this.addShapeInfo(rect2, si);
			Rectangle rect3 = getTextSize(txt.substring(0, 1), g, vertical, 0,
					font);
			int delx = 0, dely = 0;
			angle = angle % 360;
			if (angle < 0) {
				angle += 360;
			}
			if (angle >= 0 && angle < 90) {
				delx = 0;
				dely = (int) (rect2.width * Math.sin(angle * Math.PI / 180));
			} else if (angle < 180) {
				dely = rect.height;
				delx = (int) (rect2.width * Math.cos(angle * Math.PI / 180));
			} else if (angle < 270) {
				delx = -rect.width;
				dely = (int) (-rect2.height * Math.sin(angle * Math.PI / 180));
			} else {
				dely = 0;
				delx = (int) (-rect2.height * Math.cos(angle * Math.PI / 180));
			}
			AffineTransform at1 = AffineTransform.getRotateInstance(angle
					* Math.PI / 180, xloc - delx, yloc - dely);
			g.transform(at1);
			int length = txt.length();
			for (int i = length; i > 0; i--) {
				g.drawString(txt.substring(i - 1, i), xloc - delx, yloc - dely
						- rect3.height * (length - i));
			}
			g.setTransform(at);
		}
		if (isUnderline(fontStyle)) {
			// ��Ҫ�»��ߣ���һ����Ŀǰֻ�ں������֣��Ҳ���ת��ʱ�򻭣�
			// /���ں��Ż�������ת���ǶȵĻ���ô�����ú�
			if (!vertical && angle == 0) {
				setStroke(g, null, Consts.LINE_SOLID, 0.5f);
				drawLine(g, xloc, yloc + 2, xloc + rect.width, yloc + 2);
			}
		}
		Utils.setGraphAntiAliasingOn(g);
	}

	/**
	 * ���������ı�ʱ��������������ͬ��Ϊ�˻������ܣ��������new�������
	 * �������建���ȡ��Ӧ����
	 * @param fontName ��������
	 * @param fontStyle ���
	 * @param fontSize �ֺ�
	 * @return �������
	 */
	public synchronized static Font getFont(String fontName, int fontStyle,
			int fontSize) {
		if (fontName == null || fontName.trim().length() < 1) {
			fontName = "dialog";
		}
		ListIterator li = globalFonts.listIterator();
		fontStyle = fontStyle & 0x03;
		while (li.hasNext()) {
			Font f = (Font) li.next();
			if (f.getFontName().equalsIgnoreCase(fontName)
					&& f.getStyle() == fontStyle && f.getSize() == fontSize) {
				return f;
			}
		}
		Font f = new Font(fontName, fontStyle, fontSize);
		globalFonts.add(f);
		return f;
	}

	/**
	 * �����ı�����ת�Ƕȼ����ʵ�ʻ����ı�ʱ��
	 * �ı���������ĵ�ķ�λ
	 * @param angle ��ת�Ƕ�
	 * @return ������ĵ�ķ�λ��ֵΪConsts.LOCATION_XX
	 */
	public static int getAngleTextLocation(double angle) {
		if (angle == 0) {
			return Consts.LOCATION_LM;
		} else if (angle < 90) {
			return Consts.LOCATION_LB;
		} else if (angle == 90) {
			return Consts.LOCATION_CB;
		} else if (angle < 180) {
			return Consts.LOCATION_RB;
		} else if (angle == 180) {
			return Consts.LOCATION_RM;
		} else if (angle < 270) {
			return Consts.LOCATION_RT;
		} else if (angle == 270) {
			return Consts.LOCATION_CT;
		} else if (angle < 360) {
			return Consts.LOCATION_LT;
		} else if (angle == 360) {
			return Consts.LOCATION_LM;
		}
		return 0;
	}

	/**
	 * �ж�����ͼԪ�б������Ƿ��жѻ��������ԣ��ѻ�ͼ�����귶Χ��Ҫ�ۼƼ���
	 * @param dataElements ����ͼԪ�б�
	 * @return ����жѻ����ͷ���true�����򷵻�false
	 */
	public static boolean isStackedGraph(ArrayList dataElements) {
		for (int i = 0; i < dataElements.size(); i++) {
			DataElement de = (DataElement) dataElements.get(i);
			if (de instanceof Column) {
				if (((Column) de).isStacked()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ��ʵ��o����format��ʽ��Ϊ�ı�
	 * @param o ��ֵ
	 * @param format ��ʽ��Ϣ
	 * @return ��ʽ������ı�
	 */
	public static String format(double o, String format) {
		return format(new java.lang.Double(o), format);
	}

	/**
	 * ������o����format��ʽ��Ϊ�ı�
	 * @param o �������͵�����(���õ����ڣ���ֵ���Լ��б��)
	 * @param format ��ʽ��Ϣ
	 * @return ��ʽ������ı�
	 */
	public static String format(Object o, String format) {
		if (o instanceof Date) {
			DateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(o);
		} else if (o instanceof Number) {
			NumberFormat nf = new DecimalFormat(format);
			return nf.format(o);
		} else if (o instanceof ArrayList) {
			ArrayList series = (ArrayList) o;
			StringBuffer sb = new StringBuffer();
			for (int i = 0, size = series.size(); i <= size; ++i) {
				if (i > 1) {
					sb.append(',');
				}
				sb.append(format(series.get(i), format));
			}
			return sb.toString();
		} else {
			return Variant.toString(o);
		}
	}

	public static void drawRect(Graphics2D g,double x, double y,double w,double h) {
		Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);
		g.draw(rect);
	}

	public static void fillRect(Graphics2D g,double x, double y,double w,double h) {
		Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);
		g.fill(rect);
	}
	
	public static void fillPolygon(Graphics2D g,double[] x, double[] y) {
		Shape s = newPolygon2D(x,y);
		g.fill(s);
	};
	
	public static void drawPolygon(Graphics2D g,double[] x, double[] y) {
		Shape s = newPolygon2D(x,y);
		g.draw(s);
	};

	/**
	 * ����ָ����������״�������
	 * @param g ͼ���豸
	 * @param shape ��Ҫ������״
	 * @param transparent ���͸����
	 * @param c �����ɫ�������ֵΪnull�����ʾ��ǰ��״͸����������䡣
	 */
	public static void fill(Graphics2D g, Shape shape, float transparent,
			Color c) {
		if (c == null) {// ͸��ɫ�������
			return;
		}
		g.setColor(c);
		setTransparent(g, transparent);
		g.fill(shape);
		setTransparent(g, 1);
	}

	/**
	 * ʹ��ͼ���豸��ǰ��ɫ���ٰ���ָ����������״�������
	 * @param g ͼ���豸
	 * @param shape ��Ҫ������״
	 * @param transparent ���͸����
	 */
	public static void fillPaint(Graphics2D g, Shape shape, float transparent) {
		setTransparent(g, transparent);
		g.fill(shape);
		setTransparent(g, 1);
	}

	/**
	 * ����ͼ���豸��ǰ��͸����
	 * @param g ͼ���豸
	 * @param transparent ͸���ȣ�ȡֵ��ΧΪ����[0,1]��Խ�����ֵ��0��1����
	 */
	public static void setTransparent(Graphics2D g, float transparent) {
		if (transparent > 1) {
			transparent = 1f;
		} else if (transparent < 0) {
			transparent = 0f;
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				transparent));
	}

	/**
	 * �ڵ�ǰ��ͼ���豸�����»���һ���߶�
	 * @param g ͼ���豸
	 * @param b ��ʼ��
	 * @param e ������
	 */
	public static void drawLine(Graphics2D g, Point2D b, Point2D e) {
		drawLine(g,b,e,Consts.LINE_ARROW_NONE);
	}
	
	/**
	 * �ڵ�ǰ��ͼ���豸�����»���һ������ͷ���߶�
	 * @param g ͼ���豸
	 * @param b ��ʼ��
	 * @param e ������
	 * @param arrow ��ͷ��״��ֵ��֧��
	 * Consts.LINE_ARROW ������������Ҽ�ͷ
	 * Consts.LINE_ARROW_L ��ʼ�㴦�������ͷ 
	 */
	public static void drawLine(Graphics2D g, Point2D b, Point2D e, int arrow) {
		if (b == null || e == null) {
			return;
		}
		drawLine(g, b.getX(), b.getY(), e.getX(), e.getY(), arrow);
	}

	/**
	 * ����ָ����������һ�����ľ��Σ���Ӧ����ͼԪ
	 * @param g ͼ���豸
	 * @param x x����
	 * @param y y����
	 * @param w ���
	 * @param h �߶�
	 * @param borderColor �߿���ɫ
	 * @param borderStyle �߿���
	 * @param borderWeight �߿�ֶ�
	 * @param drawShade ������Ӱ
	 * @param convexEdge �Ƿ�͹���߿�
	 * @param transparent ���͸����
	 * @param fillColor �����ɫ
	 * @param isVertical �Ƿ���������(���ӵĺ��ݲ�ͬʱ�����õ���佥��ɫ��ͬ)
	 */
	public static void draw2DRect(Graphics2D g, double x, double y, double w, double h,
			Color borderColor, int borderStyle, float borderWeight,
			boolean drawShade, boolean convexEdge, float transparent,
			ChartColor fillColor, boolean isVertical) {
		if (drawShade && fillColor.getColor1() != null) {
			drawRectShadow(g, x, y, w, h);
		}

		setTransparent(g, transparent);
		if(fillColor.getType()!=Consts.PATTERN_DEFAULT){
			Utils.setPaint(g, x, y, w, h, fillColor);
			fillRect(g, x, y, w, h);
		}else if (fillColor.getColor1() != null) {
			// ��Ч��
			if (fillColor.isDazzle()) {
				CubeColor ccr = new CubeColor(fillColor.getColor1());
				Color c1 = ccr.getR1(), c2 = ccr.getT1();
				double x1, y1, x2, y2;
				if (isVertical) {
					x1 = x;
					y1 = y;
					x2 = x1 + w / 2;
					y2 = y1;
				} else {
					x1 = x;
					y1 = y;
					x2 = x1;
					y2 = y1 + h / 2;
				}
				if (c1 != null && c2 != null) {
					GradientPaint paint = new GradientPaint((int)x1, (int)y1, c1, (int)x2, (int)y2,
							c2, true);
					g.setPaint(paint);
					fillRect(g,x1, y1, w, h);
				}
			} else if (fillColor.isGradient()) {
				if (setPaint(g, x, y, w, h, fillColor)) {
					fillRect(g,x, y, w, h);
				}
			} else {
				if (convexEdge) {
					Color change = CubeColor.getDazzelColor(fillColor
							.getColor1());
					g.setColor(change);
				} else {
					g.setColor(fillColor.getColor1());
				}
				fillRect(g,x, y, w, h);
			}
		}

		setTransparent(g, 1);

		if (convexEdge && !fillColor.isGradient() && w > 10 && h > 10) {
			drawRaisedBorder(g, x, y, w, h, fillColor.getColor1());
		}
		if (setStroke(g, borderColor, borderStyle, borderWeight)) {
			if (borderColor != null) {// �߿�colorΪnull��ʾ�õ�ǰʹ���еĻ���
				g.setColor(borderColor);
			}
			drawRect(g,x, y, w, h);
		}
		setTransparent(g, transparent);
	}

	/**
	 * ��ȡ��������Ļ���·��(��Ƚ���ʱ׼ȷ��������ܼ�ʱ��java��bug�����Ƶ��ߴ���)
	 * ����ʹ�ø÷������ƶ����߶�
	 * @param points ������
	 * @param closePath �Ƿ���·��
	 * @return ����·��
	 */
	public static Path2D getPath2D(ArrayList<Point2D> points, boolean closePath) {
		Path2D.Double path2D = new Path2D.Double();
		for (int i = 0; i < points.size(); i++) {
			Point2D p = points.get(i);
			if (i == 0) {
				path2D.moveTo(p.getX(), p.getY());
			} else {
				path2D.lineTo(p.getX(), p.getY());
			}
		}
		if (points.size() > 0 && closePath) {
			path2D.closePath();
		}
		return path2D;
	}

	public static Shape newPolygon2D(double[] x, double[] y) {
		return newPolygon2DShape(x,y);
	}
	/**
	 * ����·���������Ӹ����ĵ�����
	 * @param x x����
	 * @param y y����
	 * @return ���·������״
	 */
	public static Shape newPolygon2DShape(double[] x, double[] y) {
		Path2D.Double polygon2D = new Path2D.Double();
		polygon2D.moveTo(x[0], y[0]);
		for (int i = 1; i < x.length; i++) {
			polygon2D.lineTo(x[i], y[i]);
		}
		polygon2D.closePath();
		return polygon2D;
	}

	private static void drawRaisedBorder(Graphics2D g, double x, double y, double w,
			double h, Color borderColor) {
		// ̫ϸ������Ҳ������͹���߿�
		// T1������R1��԰�.����ɫ��͹���߿򲻺ÿ�������ɫʱ����͹���߿����ԣ�
		Color dazzel = CubeColor.getDazzelColor(borderColor);
		CubeColor ccr = new CubeColor(dazzel);
		int d = 5;
		for (int i = 0; i < d; i++) {
			Color tmp = ccr.getLight((i + 1) * 0.2f);
			Utils.setStroke(g, tmp, Consts.LINE_SOLID, 1);
			drawLine(g,x + i, y + i, x + i, y + h - i);
			drawLine(g,x + i, y + i, x + w - i, y + i);
		}
		for (int i = 0; i < d; i++) {
			Color tmp = ccr.getDark((i + 1) * 0.2f);
			Utils.setStroke(g, tmp, Consts.LINE_SOLID, 1);
			drawLine(g,x + w - i, y + i, x + w - i, y + h - i);
			drawLine(g,x + i, y + h - i, x + w - i, y + h - i);
		}
	}

	/**
	 * ��ָ��������ƾ�����Ӱ
	 * @param g ͼ���豸
	 * @param x x����
	 * @param y y����
	 * @param w ���
	 * @param h �߶�
	 * @return ��Ӱͨ����Ҫƫ�ƣ��������ƫ��������λ����
	 */
	public static int drawRectShadow(Graphics2D g, double x, double y, double w, double h) {
		if (w == 0 || h == 0)
			return 0;
		int dShadow = 4;
		w -= dShadow;
		h -= dShadow;
		double x1, y1, x2, y2;
		int z = 0;
		Color cz = new Color(shadowColors[z * 3], shadowColors[z * 3 + 1],
				shadowColors[z * 3 + 2]);
		z = 1;
		cz = new Color(shadowColors[z * 3], shadowColors[z * 3 + 1],
				shadowColors[z * 3 + 2]);
		Utils.setStroke(g, cz, Consts.LINE_SOLID, 1);
		x1 = x + w + dShadow;
		y1 = y - 1;
		x2 = x1;
		y2 = y + h - 1;
		drawLine(g,x1, y1, x2, y2);
		x1 = x + w + 3;
		y1 = y - 2;
		x2 = x1;
		y2 = y1;
		drawLine(g,x1, y1, x2, y2);

		z = 2;
		cz = new Color(shadowColors[z * 3], shadowColors[z * 3 + 1],
				shadowColors[z * 3 + 2]);
		Utils.setStroke(g, cz, Consts.LINE_SOLID, 1);
		x1 = x + 1;
		y1 = y - 2;
		x2 = x + w + 2;
		y2 = y1;
		drawLine(g,x1, y1, x2, y2);
		x1 = x + w + 3;
		y1 = y - 1;
		x2 = x1;
		y2 = y + h - 1;
		drawLine(g,x1, y1, x2, y2);

		z = 3;
		cz = new Color(shadowColors[z * 3], shadowColors[z * 3 + 1],
				shadowColors[z * 3 + 2]);
		Utils.setStroke(g, cz, Consts.LINE_SOLID, 1);
		x1 = x + 1;
		y1 = y - 1;
		x2 = x + w + 2;
		y2 = y1;
		drawLine(g,x1, y1, x2, y2);
		x1 = x + w + 2;
		y1 = y - 1;
		x2 = x1;
		y2 = y + h - 1;
		drawLine(g,x1, y1, x2, y2);

		z = 4;
		cz = new Color(shadowColors[z * 3], shadowColors[z * 3 + 1],
				shadowColors[z * 3 + 2]);
		Utils.setStroke(g, cz, Consts.LINE_SOLID, 2);
		x1 = x + w;
		y1 = y;
		fillRect(g,x1, y1, 2, h);
		return dShadow;
	}

	/**
	 * ��ȡ���ʵ�3D����ƽ̨�ĺ�ȣ�����̫��Ҳ����̫��
	 * @param coorShift 3D����ƫ����
	 * @return �����������ƫ����
	 */
	public static double getPlatformH(double coorShift) {
		double h = coorShift;
		if (h < 2)
			h = 2;
		if (h > 6)
			h = 10;
		return h;
	}

	/**
	 * ��ָ��������װΪ��ά��������������ʹ��������
	 * @param x x����
	 * @param y y����
	 * @param w ���
	 * @param h �߶�
	 * @param borderColor �߿���ɫ
	 * @param borderStyle �߿���
	 * @param borderWeight �߿�ֶ�
	 * @param drawShade �Ƿ������Ӱ
	 * @param convexEdge �Ƿ�͹���߿�
	 * @param transparent ���͸����
	 * @param fillColor �����ɫ
	 * @param isVertical �Ƿ���������
	 * @param coorShift ��ά���
	 * @return �������������ķ�װ��
	 */
	public static Desc3DRect get3DRect(double x, double y, double w, double h,
			Color borderColor, int borderStyle, float borderWeight,
			boolean drawShade, boolean convexEdge, float transparent,
			ChartColor fillColor, boolean isVertical, double coorShift) {
		Desc3DRect d3 = new Desc3DRect();
		d3.x = x;
		d3.y = y;
		d3.w = w;
		d3.h = h;
		d3.borderColor = borderColor;
		d3.borderStyle = borderStyle;
		d3.borderWeight = borderWeight;
		d3.drawShade = drawShade;
		d3.convexEdge = convexEdge;
		d3.transparent = transparent;
		d3.fillColor = fillColor;
		d3.isVertical = isVertical;
		d3.coorShift = coorShift;
		return d3;
	}

	/**
	 * ʹ����ά������������������һ��������
	 * @param g ͼ���豸
	 * @param d3 ��װ���������������������
	 */
	public static void draw3DRect(Graphics2D g, Desc3DRect d3) {
		draw3DRect(g, d3.x, d3.y, d3.w, d3.h, d3.borderColor, d3.borderStyle,
				d3.borderWeight, d3.drawShade, d3.convexEdge, d3.transparent,
				d3.fillColor, d3.isVertical, d3.coorShift);
	}

	/**
	 * ��ָ��λ�ð������û���һ���������� 3D���ӻ��ϱ߿�Ͳ��ÿ��ˣ�����borderColorΪnull��Ϊ͸��ɫ��
	 * ������Ϊʹ�õ�ǰ��ɫ
	 * ���س����ӵ�������״ isDrawTop,isDrawRight,
	 * �Ƿ���ƶ��ߣ��������ӣ��������ұߣ�������״������ѡ�������и�ֵ�ۻ�ʱ
	 * �����ۻ������෴�����ԣ�����һ������
	 * �������ȫ��������Ķ������ƶ����������������Ӽ串��
	 */
	public static void draw3DRect(Graphics2D g, double x, double y, double w, double h,
			Color borderColor, int borderStyle, float borderWeight,
			boolean drawShade, boolean convexEdge, float transparent,
			ChartColor fillColor, boolean isVertical, double coorShift) {
		Shape poly;
		if (drawShade && fillColor.getColor1() != null) {
			drawRectShadow(g, x + coorShift, y - coorShift, w, h);
		}
		CubeColor ccr = new CubeColor(fillColor.getColor1());
		if (transparent < 1) {
			// 1:���ڵ��ı��棬͸��ʱ���ܿ�����
			// g.setColor(fillColor.getColor1());
			Utils.fill(g, new Rectangle2D.Double(x + coorShift, y - coorShift, w, h),
					transparent, fillColor.getColor1());
			if (Utils.setStroke(g, borderColor, borderStyle, borderWeight)
					&& borderColor != null) {
				drawRect(g,x + coorShift, y - coorShift, w, h);
			}
			// 2:���ڵ��ĵ���
			double[] xPointsB = { x, x + w, x + w + coorShift, x + coorShift };
			double[] yPointsB = { y + h, y + h, y + h - coorShift,y + h - coorShift };
			poly = newPolygon2D(xPointsB, yPointsB);
			// g.setColor(fillColor.getColor1());
			Utils.fill(g, poly, transparent, fillColor.getColor1());
			if (Utils.setStroke(g, borderColor, borderStyle, borderWeight)
					&& borderColor != null) {
				g.draw(poly);
			}

			// 3:���ڵ��������
			double[] xPointsL = { x, x, x + coorShift, x + coorShift };
			double[] yPointsL = { y, y + h, y + h - coorShift, y - coorShift };
			// g.setColor(fillColor.getColor1());
			poly = newPolygon2D(xPointsL, yPointsL);
			Utils.fill(g, poly,transparent, fillColor.getColor1());
			if (Utils.setStroke(g, borderColor, borderStyle, borderWeight)
					&& borderColor != null) {
				g.draw( poly );
			}
		}
		Rectangle bound;
		// 4:�ұ߲���
		// if (isDrawRight) {
		double[] xPointsR = { x + w, x + w, x + w + coorShift, x + w + coorShift };
		double[] yPointsR = { y, y + h, y + h - coorShift, y - coorShift };
		poly = newPolygon2D(xPointsR, yPointsR);
		bound = poly.getBounds();
		if (fillColor.isGradient()) {
			ChartColor tmpcc = new ChartColor();
			tmpcc.setGradient(true);
			tmpcc.setAngle(270);
			if (isVertical) {
				tmpcc.setColor1(ccr.getR1());
				tmpcc.setColor2(ccr.getR2());
			} else {
				tmpcc.setColor1(ccr.getT1());
				tmpcc.setColor2(ccr.getT2());
			}
			if (Utils.setPaint(g, bound.x, bound.y, bound.width, bound.height,
					tmpcc)) {
				Utils.fillPaint(g, poly, transparent);
			}
		} else {
			// g.setColor(ccr.getR1());
			Utils.fill(g, poly, transparent, ccr.getR1());
		}

		if (Utils.setStroke(g, borderColor, borderStyle, borderWeight)
				&& borderColor != null) {
			g.draw(poly);
		}
		// }

		// 5:����
		// if (isDrawTop) {
		double[] xPointsT = { x, x + w, x + w + coorShift, x + coorShift };
		double[] yPointsT = { y, y, y - coorShift, y - coorShift };
		poly = newPolygon2D(xPointsT, yPointsT);
		bound = poly.getBounds();
		if (fillColor.isGradient()) {// ������Ͷ��涼ʹ�ý���ʱ����
			ChartColor tmpcc = new ChartColor();
			tmpcc.setGradient(true);
			tmpcc.setAngle(180);
			if (isVertical) {
				tmpcc.setColor1(ccr.getT1());
				tmpcc.setColor2(ccr.getT2());
			} else {
				tmpcc.setColor1(ccr.getR1());
				tmpcc.setColor2(ccr.getR2());
			}
			if (Utils.setPaint(g, bound.x, bound.y, bound.width, bound.height,
					tmpcc)) {
				Utils.fillPaint(g, poly, transparent);
			}
		} else {
			// g.setColor(ccr.getT2());
			Utils.fill(g, poly, transparent, ccr.getT2());
		}

		if (Utils.setStroke(g, borderColor, borderStyle, borderWeight)
				&& borderColor != null) {
			g.draw(poly);
		}
		// }

		boolean isSet = false;
		if (fillColor.isDazzle()) {// ֻ��ǰ��������Ƿ���Ч��
			// ǰ��
			ChartColor tmpcc = new ChartColor();
			if (isVertical) {
				tmpcc.setColor1(ccr.getF1());
				tmpcc.setColor2(ccr.getF2());
				tmpcc.setAngle(270);
			} else {
				tmpcc.setColor1(ccr.getF1());
				tmpcc.setColor2(ccr.getF2());
				tmpcc.setAngle(180);
			}
			isSet = Utils.setPaint(g, x, y, w, h, tmpcc);
		} else {
			isSet = Utils.setPaint(g, x, y, w, h, fillColor);
		}
		if (isSet) {
			Utils.fillPaint(g, new Rectangle2D.Double(x, y, w, h), transparent);
		}

		if (convexEdge && !fillColor.isGradient() && w > 10 && h > 10) {
			drawRaisedBorder(g, x, y, w, h, fillColor.getColor1());
		}

		if (Utils.setStroke(g, borderColor, borderStyle, borderWeight)
				&& borderColor != null) {
			drawRect(g,x, y, w, h);
		}
	}

	private static Color getShadeColor(int z) {
		if (z > 4)
			z = 4;
		return new Color(shadowColors[z * 3], shadowColors[z * 3 + 1],
				shadowColors[z * 3 + 2]);
	}

	private static Rectangle2D getSmallerBounds(Rectangle2D orginal,
			int deltaSize) {
		if (deltaSize == 0)
			return orginal;
		double x = orginal.getX();
		double y = orginal.getY();
		double w = orginal.getWidth();
		double h = orginal.getHeight();
		double scale;
		if (w >= h) {
			scale = h / w;
			w -= deltaSize * 2;
			h -= deltaSize * 2 * scale;
			x += deltaSize;
			y += deltaSize * scale;
		} else {
			scale = w / h;
			w -= deltaSize * 2 * scale;
			h -= deltaSize * 2;
			x += deltaSize * scale;
			y += deltaSize;
		}
		return new Rectangle2D.Double(x, y, w, h);
	}

	/**
	 * ����ָ����������һ��ƽ���ͼ
	 * @param g ͼ���豸
	 * @param ellipseBounds ��ͼ�����εı߽�
	 * @param startAngle ���ε���ʼ�Ƕ�
	 * @param extentAngle �Ƕȳ��ȷ�Χ(������ʼ5�ȣ����Ƴ���45�ȣ���ʾ��5�Ȼ���50�ȵ�һ������)
	 * @param borderColor �߿���ɫ
	 * @param borderStyle �߿���
	 * @param borderWeight �߿�ֶ�
	 * @param transparent ͸����
	 * @param fillColor �����ɫ
	 * @param dazzelCount �߿��ŵĺ��
	 */
	public static void draw2DPie(Graphics2D g, Rectangle2D ellipseBounds,
			double startAngle, double extentAngle, Color borderColor,
			int borderStyle, float borderWeight, float transparent,
			ChartColor fillColor, int dazzelCount) {
		draw2DArc(g, ellipseBounds, startAngle, extentAngle, borderColor,
				borderStyle, borderWeight, transparent, fillColor, dazzelCount,
				Arc2D.PIE);
	}

	/**
	 * ����ָ�������������ε�ʵ��
	 * @param g ͼ���豸
	 * @param ellipseBounds ��ͼ�����εı༭
	 * @param startAngle ��ʼ�Ƕ�
	 * @param extentAngle �Ƕȷ�Χ
	 * @param borderColor �߿���ɫ
	 * @param borderStyle �߿���
	 * @param borderWeight �߿�ֶ�
	 * @param transparent ͸����
	 * @param fillColor �����ɫ
	 * @param dazzelCount �߿��ŵĺ��
	 * @param arcType �������ͣ�ֵ�ο���Arc2D.XXX
	 */
	public static void draw2DArc(Graphics2D g, Rectangle2D ellipseBounds,
			double startAngle, double extentAngle, Color borderColor,
			int borderStyle, float borderWeight, float transparent,
			ChartColor fillColor, int dazzelCount, int arcType) {
		Arc2D arc = new Arc2D.Double(ellipseBounds, startAngle, extentAngle,
				arcType);
		Rectangle rect = ellipseBounds.getBounds();
		ChartColor cc = fillColor;
		if (cc.isDazzle()) {
			Color dazzel = CubeColor.getDazzelColor(cc.getColor1());
			CubeColor ccr = new CubeColor(dazzel);
			GradientPaint gp;
			// ���⼰������ɫ���仭���߿����һȦС��ʹ�ý���ɫ���ͻ����
			// setTransparent(g, transparent);
			if (dazzelCount > 10) {// ����ֵ����̫��û���㹻����ɫ�Լ�������
				dazzelCount = 10;
			}
			for (int k = 0; k < dazzelCount; k++) {
				Rectangle2D tmpBounds = getSmallerBounds(ellipseBounds, k);
				Arc2D tmpArc = new Arc2D.Double(tmpBounds, startAngle,
						extentAngle, arcType);
				Color tmp = ccr.getDark(0.5f + k * 0.07f);
				rect = tmpBounds.getBounds();
				if (k == (dazzelCount - 1)) {
					if (ccr.getF2() != null && ccr.getF1() != null) {
						gp = new GradientPaint(rect.x, rect.y + rect.height,
								ccr.getF2(), rect.x + rect.width / 2, rect.y
										+ rect.height / 2, ccr.getF1(), true);
						g.setPaint(gp);
						Utils.fillPaint(g, tmpArc, transparent);
					}
				} else {
					// g.setColor(tmp);
					Utils.fill(g, tmpArc, transparent, tmp);
				}
			}
		} else if (cc.color1 != null) {// �������͸��ɫ
			if (Utils.setPaint(g, rect.x, rect.y, rect.width, rect.height, cc)) {
				Utils.fillPaint(g, arc, transparent);
			}
		}// ����Ϊ͸��ɫ�����û���
		if (Utils.setStroke(g, borderColor, borderStyle, borderWeight)) {
			g.draw(arc);
		}

	}

	/**
	 * ���ո��������Լ���ǰͼ���豸�Ļ�������һ���߶�
	 * drawLine��drawLineShade֮����Ҫ�ֿ�������Ϊ��ͼʱ��Ҫ�Ȱ���Ӱ���꣬Ȼ����ܻ��ߣ����ܽ�����
	 * @param g ͼ���豸
	 * @param x1 ���x����
	 * @param y1 ���y����
	 * @param x2 �յ�x����
	 * @param y2 �յ�y����
	 */
	public static void drawLine(Graphics2D g, double x1, double y1, double x2,
			double y2) {
		drawLine(g,x1,y1,x2,y2,Consts.LINE_ARROW_NONE);
	}
	
	/**
	 * ���ո��������Լ���ǰͼ���豸�Ļ�������һ������ͷ���߶�
	 * @param g ͼ���豸
	 * @param x1 ���x����
	 * @param y1 ���y����
	 * @param x2 �յ�x����
	 * @param y2 �յ�y����
	 * @param arrow ��ͷλ�ã�ȡֵֻ��Ϊ
	 * �� Consts.LINE_ARROW_NONE (����ͨ�߶�)
	 * �� Consts.LINE_ARROW_L(��㴦)
	 * �� Consts.LINE_ARROW  (�յ㴦)
	 */
	public static void drawLine(Graphics2D g, double x1, double y1, double x2,
			double y2, int arrow) {
		Line2D line = new Line2D.Double(x1, y1, x2, y2);
		g.draw(line);
//		�ߵļ�ͷ��ʱֻ֧����ͨ�����Ҽ�ͷ
		if(arrow==Consts.LINE_ARROW || arrow==Consts.LINE_ARROW_L){
//			���ȱ�ʾ������֮���б��
			double radian = Math.atan2(y2-y1,x2-x1);
			double x = x2, y = y2;
			if(arrow==Consts.LINE_ARROW_L){//���ͷʱ����Ҫ����x1
				x = x1;
				y = y1;
			}
			Utils.drawLineArrow(g, x, y, radian, arrow);
		}
	}

	/**
	 * ����ͼ���豸�ĵ�ǰ������ɫ
	 * @param g ͼ���豸
	 * @param c ������ɫ����ɫΪnullʱ�����ı䵱ǰ������ɫ
	 */
	public static void setColor(Graphics2D g, Color c) {
		if (c != null) {
			g.setColor(c);
		}
	}

	private static boolean setPointPaint(Graphics2D g, ChartColor cc,
			Shape pShape) {
		if (cc.getColor1() == null) {
			return false;
		}
		Rectangle2D bound = pShape.getBounds2D();

		boolean isCircle = (pShape instanceof Ellipse2D.Double);
		if (cc.isDazzle()) {
			Paint paint = null;
			if (isCircle) {
				Ball ball = new Ball(cc.getColor1(), 0);
				double x, y, w, h, L = 2;
				x = bound.getX() - L;
				y = bound.getY() - L;
				w = bound.getWidth() + L * 2;
				h = bound.getHeight() + L * 2;

				Rectangle2D bd = new Rectangle2D.Double(x, y, w, h);
				paint = new TexturePaint(ball.imgs[4], bd);
			} else {
				CubeColor cuc = new CubeColor(cc.getColor1());
				paint = new GradientPaint((int) bound.getX(),
						(int) (bound.getY() + bound.getHeight()), cuc.getF2(),
						(int) (bound.getX() + bound.getWidth() / 2),
						(int) (bound.getY() + bound.getHeight() / 2),
						cuc.getF1(), true);
			}
			g.setPaint(paint);
		} else {
			setPaint(g, (int) bound.getX(), (int) bound.getY(),
					(int) bound.getWidth(), (int) bound.getHeight(), cc);
		}
		return true;
	}

	private static Shape getCPoint1ShapeArea(int shape, double x, double y,
			double radiusx, double radiusy) {
		Shape pShape = null;
		switch (shape) {
		case Consts.PT_NONE: // ��
			break;
		case Consts.PT_CIRCLE: // Բ
			pShape = new Ellipse2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_SQUARE: // ������
			pShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_TRIANGLE: // ������
			double[] xs_tr = new double[] {
					x - radiusx * Math.cos(Math.toRadians(30)), x,
					x + radiusx * Math.cos(Math.toRadians(30)) };
			double[] ys_tr = new double[] { y + radiusy / 2, y - radiusy,
					y + radiusy / 2 };
			pShape = newPolygon2DShape(xs_tr, ys_tr);
			break;
		case Consts.PT_RECTANGLE: // ������
			pShape = new Rectangle2D.Double(x - radiusx, y - radiusy * 7 / 10,
					radiusx * 2, radiusy * 7 / 5);
			break;
		case Consts.PT_STAR: // ����
			pShape = new Ellipse2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_DIAMOND: // ����
			double[] xs_di,
			ys_di;
			xs_di = new double[] { x - radiusx, x, x + radiusx, x };
			ys_di = new double[] { y, y - radiusy, y, y + radiusy };
			pShape = newPolygon2DShape(xs_di, ys_di);
			break;
		case Consts.PT_CORSS: // ����
			pShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_PLUS: // �Ӻ�
			pShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_D_CIRCEL: // ˫Բ
			pShape = new Ellipse2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_D_SQUARE: // ˫������
			pShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_D_TRIANGLE: // ˫������
			double delta = radiusx * Math.cos(Math.toRadians(30));
			double[] xs_dtr1,
			ys_dtr1;
			xs_dtr1 = new double[] { x - delta, x, x + delta };
			ys_dtr1 = new double[] { y + radiusy / 2, y - radiusy,
					y + radiusy / 2 };
			pShape = newPolygon2DShape(xs_dtr1, ys_dtr1);
			break;
		case Consts.PT_D_RECTANGLE: // ˫������
			pShape = new Rectangle2D.Double(x - radiusx, y - radiusy * 7 / 10,
					radiusx * 2, radiusy * 7 / 5);
			break;
		case Consts.PT_D_DIAMOND: // ˫����
			double[] xs_ddi2,
			ys_ddi2;
			xs_ddi2 = new double[] { x - radiusx, x, x + radiusx, x };
			ys_ddi2 = new double[] { y, y - radiusy, y, y + radiusy };
			pShape = newPolygon2DShape(xs_ddi2, ys_ddi2);

			break;
		case Consts.PT_CIRCLE_PLUS: // Բ�ڼӺ�
			pShape = new Ellipse2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_SQUARE_PLUS: // ���ڼӺ�
			pShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_TRIANGLE_PLUS: // �����ڼӺ�
			double[] xs_trp,
			ys_trp;
			xs_trp = new double[] { x - radiusx * Math.cos(Math.toRadians(30)),
					x, x + radiusx * Math.cos(Math.toRadians(30)) };
			ys_trp = new double[] { y + radiusy / 2, y - radiusy,
					y + radiusy / 2 };
			pShape = newPolygon2DShape(xs_trp, ys_trp);
			break;
		case Consts.PT_RECTANGLE_PLUS: // �������ڼӺ�
			pShape = new Rectangle2D.Double(x - radiusx, y - radiusy * 7 / 10,
					radiusx * 2, radiusy * 7 / 5);
			break;
		case Consts.PT_DIAMOND_PLUS: // ���ڼӺ�
			double[] xs_dip,
			ys_dip;
			xs_dip = new double[] { x - radiusx, x, x + radiusx, x };
			ys_dip = new double[] { y, y - radiusy, y, y + radiusy };
			pShape = newPolygon2DShape(xs_dip, ys_dip);
			break;
		}
		return pShape;
	}

	/**
	 * ����ֱ������ϵ��һ����Ĳ���1(ͼԪ֮���Էֲ���������Է�ֹ����ɫ���Ǳ��ǰ������)
	 * @param g ͼ���豸
	 * @param point ������
	 * @param shape �����״��ȡֵ�ο�Consts.PT_XXX
	 * @param radiusx x���ϵ�ҵ��뾶�����������ص�λ���׳�Ϊ���ϵ�ҵ��뾶
	 * @param radiusy y���ϵ�ҵ��뾶�����������ص�λ
	 * @param rw x��y���ϵ�ҵ��뾶��Ϊ0ʱ��ʹ�öԳƵİ뾶���ص�λ���׳�Ϊҵ���޹ص����ذ뾶
	 * @param style �߿���
	 * @param weight �߿�ֶ� 
	 * @param transparent ͸����
	 * @return �����״����(���ڼ��㳬����)
	 */
	public static Shape drawCartesianPoint1(Graphics2D g, Point2D point,
			int shape, double radiusx, double radiusy, double rw, int style,
			float weight, float transparent) {
		double x = point.getX();
		double y = point.getY();
		// û��ָ������ֵҵ��뾶ʱ��ʹ�öԳƵ����ذ뾶rw radiusWeight:�뾶��ȣ���ҵ��뾶ʱʹ�õĶԳư뾶
		if (radiusx == 0 && radiusy == 0) {
			radiusx = rw;
			radiusy = rw;
		}
		if (radiusx + radiusy == 0)
			return null;// ����ҵ��뾶�������ذ뾶��Ϊ��ʱ��������
		Point2D p1, p2;
		if (radiusx == 0) {// x����Ϊ0ʱ��������Ч��������ֱ��
			p1 = new Point2D.Double(x, y - radiusy);
			p2 = new Point2D.Double(x, y + radiusy);
			return drawLine1(g, p1, p2, style, weight);
		}
		if (radiusy == 0) {// ������ֱ��
			p1 = new Point2D.Double(x - radiusx, y);
			p2 = new Point2D.Double(x + radiusx, y);
			return drawLine1(g, p1, p2, style, weight);
		}

		Shape linkShape = null, pShape = null;
		int shadowShift = (int) (radiusx * 0.2);
		if (shadowShift < SHADE_SPAN) {
			shadowShift = SHADE_SPAN;
		}
		linkShape = getCPoint1ShapeArea(shape, x, y, radiusx, radiusy);
		x += shadowShift;
		y += shadowShift;
		pShape = getCPoint1ShapeArea(shape, x, y, radiusx, radiusy);
		if (pShape != null) {
			fillRadioGradientShape(g, pShape, Color.darkGray, Color.white,
					transparent / 2);// ��Ӱ��͸���ȼ���
		}
		return linkShape;
	}

	/**
	 * ��ȡ�οյ���״
	 * 
	 * @param outer
	 *            ,����״
	 * @param inner
	 *            ,�οյ���С��״
	 * @return
	 */
	private static Shape getLouKongShape(Shape outer, Shape inner) {
		java.awt.geom.Area area = new java.awt.geom.Area(outer);
		java.awt.geom.Area sArea = new java.awt.geom.Area(inner);
		area.subtract(sArea);
		return area;
	}

	/**
	 * ����ֱ������ϵ��һ����Ĳ���2
	 * @param g ͼ���豸
	 * @param point ������
	 * @param shape �����״��ȡֵ�ο�Consts.PT_XXX
	 * @param radiusx x���ϵ�ҵ��뾶�����������ص�λ���׳�Ϊ���ϵ�ҵ��뾶
	 * @param radiusy y���ϵ�ҵ��뾶�����������ص�λ
	 * @param rw x��y���ϵ�ҵ��뾶��Ϊ0ʱ��ʹ�öԳƵİ뾶���ص�λ���׳�Ϊҵ���޹ص����ذ뾶
	 * @param style �߿���
	 * @param weight �߿�ֶ� 
	 * @param ccr �����ɫ 
	 * @param foreColor ǰ��ɫ���߿���ɫ 
	 * @param transparent ͸����
	 * @return �����״����(���ڼ��㳬����)
	 */
	public static Shape drawCartesianPoint2(Graphics2D g, Point2D point,
			int shape, double radiusx, double radiusy, double rw, int style,
			float weight, ChartColor ccr, Color foreColor, float transparent) {
		double x = point.getX();
		double y = point.getY();
		if (rw == 0) {// rwΪ0��ʾ�����ߴְ뾶����
			rw = weight / 2 + 1;
			if (rw < 4)
				rw = 3;
		}

		// û��ָ������ֵҵ��뾶ʱ��ʹ�öԳƵ����ذ뾶radius
		if (radiusx == 0 && radiusy == 0) {
			radiusx = rw;
			radiusy = rw;
		}
		if (radiusx == radiusy) {// �Գư뾶ʱ����ʹ���ߴ����ԣ��������ߴ�ʹ��1
			weight = 1;
		}

		Point2D p1, p2;
		if (radiusx == 0) {// x����Ϊ0ʱ��������Ч��������ֱ��
			p1 = new Point2D.Double(x, y - radiusy);
			p2 = new Point2D.Double(x, y + radiusy);
			drawLine2(g, p1, p2, ccr.color1, style, weight);
			return new Rectangle((int)(x-2),(int)(y-radiusy),4,(int)(2*radiusy));
		}
		if (radiusy == 0) {
			p1 = new Point2D.Double(x - radiusx, y);
			p2 = new Point2D.Double(x + radiusx, y);
			drawLine2(g, p1, p2, ccr.color1, style, weight);
			return new Rectangle((int)(x- radiusx),(int)(y-2),(int)(2*radiusx),4);
		}

		Shape fillShape = null, outerShape, innerShape;
		ArrayList<Shape> drawShapes = new ArrayList<Shape>();
		switch (shape) {
		case Consts.PT_NONE: // ��
			break;
		case Consts.PT_CIRCLE: // Բ
			fillShape = new Ellipse2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_DOT: // ʵ�ĵ�
			fillShape = new Ellipse2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			g.setColor(foreColor);
			fillPaint(g, fillShape, transparent);
			return fillShape;
		case Consts.PT_SQUARE: // ������
			fillShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			break;
		case Consts.PT_TRIANGLE: // ������
			double[] xs_tr,
			ys_tr;
			xs_tr = new double[] { x - radiusx * Math.cos(Math.toRadians(30)),
					x, x + radiusx * Math.cos(Math.toRadians(30)) };
			ys_tr = new double[] { y + radiusy / 2, y - radiusy,
					y + radiusy / 2 };
			fillShape = newPolygon2DShape(xs_tr, ys_tr);
			break;
		case Consts.PT_RECTANGLE: // ������
			fillShape = new Rectangle2D.Double(x - radiusx, y - radiusy * 7
					/ 10, radiusx * 2, radiusy * 7 / 5);
			break;
		case Consts.PT_STAR: // ����
			fillShape = new Ellipse2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			if (setStroke(g, foreColor, style, weight)) {
				double deltax = radiusx * Math.cos(Math.toRadians(45));
				double deltay = radiusy * Math.cos(Math.toRadians(45));
				drawShapes
						.add(new Line2D.Double(x, y - radiusy, x, y + radiusy));
				drawShapes.add(new Line2D.Double(x - deltax, y - deltay, x
						+ deltax, y + deltay));
				drawShapes.add(new Line2D.Double(x - deltax, y + deltay, x
						+ deltax, y - deltay));
			}
			break;
		case Consts.PT_DIAMOND: // ����
			double[] xs_di,
			ys_di;
			xs_di = new double[] { x - radiusx, x, x + radiusx, x };
			ys_di = new double[] { y, y - radiusy, y, y + radiusy };
			fillShape = newPolygon2DShape(xs_di, ys_di);
			break;
		case Consts.PT_CORSS: // ����
			fillShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			if (setStroke(g, foreColor, style, weight)) {
				double deltax = radiusx * Math.cos(Math.toRadians(45));
				double deltay = radiusy * Math.cos(Math.toRadians(45));
				drawShapes.add(new Line2D.Double(x - deltax, y - deltay, x
						+ deltax, y + deltay));
				drawShapes.add(new Line2D.Double(x - deltax, y + deltay, x
						+ deltax, y - deltay));
			}
			break;
		case Consts.PT_PLUS: // �Ӻ�
			fillShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			if (setStroke(g, foreColor, style, weight)) {
				drawShapes
						.add(new Line2D.Double(x - radiusx, y, x + radiusx, y));
				drawShapes
						.add(new Line2D.Double(x, y + radiusy, x, y - radiusy));
			}
			break;
		case Consts.PT_D_CIRCEL: // ˫Բ
			outerShape = new Ellipse2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			innerShape = new Ellipse2D.Double(x - radiusx / 2, y - radiusy / 2,
					radiusx, radiusy);
			fillShape = getLouKongShape(outerShape, innerShape);
			break;
		case Consts.PT_D_SQUARE: // ˫������
			outerShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			innerShape = new Rectangle2D.Double(x - radiusx / 2, y - radiusy
					/ 2, radiusx, radiusy);
			fillShape = getLouKongShape(outerShape, innerShape);
			break;
		case Consts.PT_D_TRIANGLE: // ˫������
			double delta = radiusx * Math.cos(Math.toRadians(30));
			double[] xs_dtr1,
			ys_dtr1;
			xs_dtr1 = new double[] { x - delta, x, x + delta };
			ys_dtr1 = new double[] { y + radiusy / 2, y - radiusy,
					y + radiusy / 2 };
			outerShape = newPolygon2DShape(xs_dtr1, ys_dtr1);

			double[] xs_dtr2 = {
					x - radiusx / 2 * Math.cos(Math.toRadians(30)), x,
					x + radiusx / 2 * Math.cos(Math.toRadians(30)) };
			double[] ys_dtr2 = { y + radiusy / 4, y - radiusy / 2,
					y + radiusy / 4 };
			innerShape = newPolygon2DShape(xs_dtr2, ys_dtr2);
			fillShape = getLouKongShape(outerShape, innerShape);
			break;
		case Consts.PT_D_RECTANGLE: // ˫������
			outerShape = new Rectangle2D.Double(x - radiusx, y - radiusy * 7
					/ 10, radiusx * 2, radiusy * 7 / 5);
			innerShape = new Rectangle2D.Double(x - radiusx / 2, y - radiusy
					* 7 / 20, radiusx, radiusy * 7 / 10);
			fillShape = getLouKongShape(outerShape, innerShape);
			break;
		case Consts.PT_D_DIAMOND: // ˫����
			double[] xs_ddi2,
			ys_ddi2;
			xs_ddi2 = new double[] { x - radiusx, x, x + radiusx, x };
			ys_ddi2 = new double[] { y, y - radiusy, y, y + radiusy };
			outerShape = newPolygon2DShape(xs_ddi2, ys_ddi2);
			double[] xs_ddi1 = { x - radiusx / 2, x, x + radiusx / 2, x };
			double[] ys_ddi1 = { y, y - radiusy / 2, y, y + radiusy / 2 };
			innerShape = newPolygon2DShape(xs_ddi1, ys_ddi1);
			fillShape = getLouKongShape(outerShape, innerShape);
			break;
		case Consts.PT_CIRCLE_PLUS: // Բ�ڼӺ�
			fillShape = new Ellipse2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			if (setStroke(g, foreColor, style, weight)) {
				drawShapes.add(fillShape);
				drawShapes
						.add(new Line2D.Double(x, y - radiusy, x, y + radiusy));
				drawShapes
						.add(new Line2D.Double(x - radiusx, y, x + radiusx, y));
			}
			break;
		case Consts.PT_SQUARE_PLUS: // ���ڼӺ�
			fillShape = new Rectangle2D.Double(x - radiusx, y - radiusy,
					radiusx * 2, radiusy * 2);
			if (setStroke(g, foreColor, style, weight)) {
				drawShapes.add(fillShape);
				drawShapes
						.add(new Line2D.Double(x, y - radiusy, x, y + radiusy));
				drawShapes
						.add(new Line2D.Double(x - radiusx, y, x + radiusx, y));
			}
			break;
		case Consts.PT_TRIANGLE_PLUS: // �����ڼӺ�
			double[] xs_trp,
			ys_trp;
			xs_trp = new double[] { x - radiusx * Math.cos(Math.toRadians(30)),
					x, x + radiusx * Math.cos(Math.toRadians(30)) };
			ys_trp = new double[] { y + radiusy / 2, y - radiusy,
					y + radiusy / 2 };
			fillShape = newPolygon2DShape(xs_trp, ys_trp);
			if (setStroke(g, foreColor, style, weight)) {
				drawShapes.add(fillShape);
				drawShapes.add(new Line2D.Double(x, y - radiusy, x, y + radiusy
						/ 2));
				drawShapes.add(new Line2D.Double(x - radiusx
						* Math.tan(Math.toRadians(30)), y, x + radiusx
						* Math.tan(Math.toRadians(30)), y));
			}
			break;
		case Consts.PT_RECTANGLE_PLUS: // �������ڼӺ�
			fillShape = new Rectangle2D.Double(x - radiusx, y - radiusy * 7
					/ 10, radiusx * 2, radiusy * 7 / 5);
			if (setStroke(g, foreColor, style, weight)) {
				drawShapes.add(fillShape);
				drawShapes.add(new Line2D.Double(x, y - radiusy * 7 / 10, x, y
						+ radiusy * 7 / 10));
				drawShapes
						.add(new Line2D.Double(x - radiusx, y, x + radiusx, y));
			}
			break;
		case Consts.PT_DIAMOND_PLUS: // ���ڼӺ�
			double[] xs_dip,
			ys_dip;
			xs_dip = new double[] { x - radiusx, x, x + radiusx, x };
			ys_dip = new double[] { y, y - radiusy, y, y + radiusy };
			fillShape = newPolygon2DShape(xs_dip, ys_dip);
			if (setStroke(g, foreColor, style, weight)) {
				drawShapes.add(fillShape);
				drawShapes
						.add(new Line2D.Double(x - radiusx, y, x + radiusx, y));
				drawShapes
						.add(new Line2D.Double(x, y - radiusy, x, y + radiusy));
			}
			break;
		}
		if (fillShape != null) {
			if (setPointPaint(g, ccr, fillShape)) {
				fillPaint(g, fillShape, transparent);
			}
			if (setStroke(g, foreColor, style, weight)) {
				if (drawShapes.isEmpty()) {
					g.draw(fillShape);
				} else {
					for (int i = 0; i < drawShapes.size(); i++) {
						Shape s = drawShapes.get(i);
						g.draw(s);
					}
				}
			}
		}
		return fillShape;
	}

	private static Shape getPPoint1ShapeArea(int shape, PolarCoor pc, double r,
			double a, double radiusR, double radiusA, int shadowShift) {
		Shape pShape = null;
		Rectangle2D bound;
		Arc2D arc;
		Point2D polarDot, p1, p2;

		switch (shape) {
		case Consts.PT_NONE: // ��
			break;
		case Consts.PT_RECTANGLE: // ������,�������£������Σ�������ͬ������
		case Consts.PT_SQUARE: // ������,���������ݱ�Ϊһ�λ�
			bound = pc.getEllipseBounds(r + radiusR);
			arc = new Arc2D.Double(bound.getX() + shadowShift, bound.getY()
					+ shadowShift, bound.getWidth(), bound.getHeight(), a
					- radiusA, radiusA * 2, Arc2D.PIE);
			java.awt.geom.Area bigArea = new java.awt.geom.Area(arc);
			if (r > radiusR) {
				bound = pc.getEllipseBounds(r - radiusR);
				arc = new Arc2D.Double(bound.getX() + shadowShift, bound.getY()
						+ shadowShift, bound.getWidth(), bound.getHeight(), a
						- radiusA - 5, radiusA * 2 + 10, Arc2D.PIE);// С���ζ�10�ȣ������������ܼ��ɾ�
				java.awt.geom.Area sArea = new java.awt.geom.Area(arc);
				bigArea.subtract(sArea);
			}// else ��������С�ڵ��ڰ뾶ʱ��ֱ�ӻ�����
			pShape = bigArea;
			break;
		case Consts.PT_DIAMOND: // ���Σ���ʱ������Ϊ���е�ı�������
			double[] xs_di,
			ys_di;
			xs_di = new double[4];
			ys_di = new double[4];
			polarDot = new Point2D.Double(r + radiusR, a);
			p1 = pc.getScreenPoint(polarDot);
			xs_di[0] = p1.getX();
			ys_di[0] = p1.getY();

			polarDot = new Point2D.Double(r, a + radiusA);
			p1 = pc.getScreenPoint(polarDot);
			xs_di[1] = p1.getX();
			ys_di[1] = p1.getY();

			polarDot = new Point2D.Double(r - radiusR, a);
			p1 = pc.getScreenPoint(polarDot);
			xs_di[2] = p1.getX();
			ys_di[2] = p1.getY();

			polarDot = new Point2D.Double(r, a - radiusA);
			p1 = pc.getScreenPoint(polarDot);
			xs_di[3] = p1.getX();
			ys_di[3] = p1.getY();

			pShape = newPolygon2DShape(xs_di, ys_di);
			break;
		case Consts.PT_CIRCLE: // Բ
		case Consts.PT_TRIANGLE: // ������
		case Consts.PT_STAR: // ����
		case Consts.PT_CORSS: // ����
		case Consts.PT_PLUS: // �Ӻ�
		case Consts.PT_D_CIRCEL: // ˫Բ
		case Consts.PT_D_SQUARE: // ˫������
		case Consts.PT_D_TRIANGLE: // ˫������
		case Consts.PT_D_RECTANGLE: // ˫������
		case Consts.PT_D_DIAMOND: // ˫����
		case Consts.PT_CIRCLE_PLUS: // Բ�ڼӺ�
		case Consts.PT_SQUARE_PLUS: // ���ڼӺ�
		case Consts.PT_TRIANGLE_PLUS: // �����ڼӺ�
		case Consts.PT_RECTANGLE_PLUS: // �������ڼӺ�
		case Consts.PT_DIAMOND_PLUS: // ���ڼӺ�
			throw new RuntimeException("Unsupportted dot shape:" + shape
					+ " in polar coordinate system.");
		}
		return pShape;
	}

	/**
	 * ���Ƽ�����ϵ��һ����Ĳ���1
	 * @param g ͼ���豸
	 * @param point ������(������)
	 * @param shape �����״��ȡֵ�ο�Consts.PT_XXX
	 * @param radiusR �����ϵ�ҵ��뾶�����������ص�λ���׳�Ϊ���ϵ�ҵ��뾶
	 * @param radiusA �����ϵ�ҵ��뾶
	 * @param rw û��ָ������ֵҵ��뾶ʱ��ʹ�öԳƵ����ذ뾶rw����ҵ��뾶ʱʹ�õĶԳư뾶
	 * 			  ����������ҵ��뾶ʱ�������ֱͬ������ϵ��
	 * @param style �߿���
	 * @param weight �߿�ֶ� 
	 * @param pc ������ϵ 
	 * @param transparent ͸����
	 * @return �����״����(���ڼ��㳬����)
	 */
	public static Shape drawPolarPoint1(Graphics2D g, Point2D point, int shape,
			double radiusR, double radiusA, double rw, int style, float weight,
			PolarCoor pc, float transparent) {
		if (radiusR == 0 && radiusA == 0) {
			return drawCartesianPoint1(g, point, shape, 0, 0, rw, style,
					weight, transparent);
		}
		double r = point.getX();
		double a = point.getY();
		Rectangle2D bound;
		Point2D polarDot, p1, p2;
		Arc2D arc, arcSmall;
		java.awt.geom.Area arcArea, arcSmallArea;
		int shadowShift = (int) (radiusR * 0.2), dLinkShift = 1;
		if (shadowShift < SHADE_SPAN) {
			shadowShift = SHADE_SPAN;
		}
		if (radiusR == 0) {// radiusRΪ0ʱ��������Ч��Ϊһ�λ���
			Color dark = getShadeColor(1);
			bound = pc.getEllipseBounds(r);
			arc = new Arc2D.Double(bound.getX() + shadowShift, bound.getY()
					+ shadowShift, bound.getWidth(), bound.getHeight(), a
					- radiusA, radiusA * 2, Arc2D.OPEN);
			if (setStroke(g, dark, style, weight)) {
				g.draw(arc);
			}
			arc = new Arc2D.Double(bound.getX() - dLinkShift, bound.getY()
					- dLinkShift, bound.getWidth() + dLinkShift * 2,
					bound.getHeight() + dLinkShift * 2, a - radiusA,
					radiusA * 2, Arc2D.PIE);

			arcSmall = new Arc2D.Double(bound.getX() + dLinkShift, bound.getY()
					+ dLinkShift, bound.getWidth() - dLinkShift * 2,
					bound.getHeight() - dLinkShift * 2, a - radiusA - 10,
					radiusA * 2 + 5, Arc2D.PIE);
			arcArea = new java.awt.geom.Area(arc);
			arcSmallArea = new java.awt.geom.Area(arcSmall);
			arcArea.subtract(arcSmallArea);
			return arcArea;
		} else if (radiusA == 0) {// radiusAΪ0ʱ��Ϊһ�ΰ뾶ֱ��
			polarDot = new Point2D.Double(r + radiusR, a);
			p1 = pc.getScreenPoint(polarDot);
			polarDot = new Point2D.Double(r - radiusR, a);
			p2 = pc.getScreenPoint(polarDot);
			return drawLine1(g, p1, p2, style, weight);
		}

		Shape pShape = getPPoint1ShapeArea(shape, pc, r, a, radiusR, radiusA,
				shadowShift);
		if (pShape != null) {
			fillRadioGradientShape(g, pShape, Color.darkGray, Color.white,
					transparent / 2);
		}

		Shape linkShape = getPPoint1ShapeArea(shape, pc, r, a, radiusR,
				radiusA, 0);
		return linkShape;
	}

	/**
	 * ���Ƽ�����ϵ��һ����Ĳ���2
	 * @param g ͼ���豸
	 * @param point ������(��ֵ���꣬�˴�Ϊ������)
	 * @param shape �����״��ȡֵ�ο�Consts.PT_XXX
	 * @param radiusR �����ϵ�ҵ��뾶�����������ص�λ���׳�Ϊ���ϵ�ҵ��뾶
	 * @param radiusA �����ϵ�ҵ��뾶
	 * @param rw û��ָ������ֵҵ��뾶ʱ��ʹ�öԳƵ����ذ뾶rw����ҵ��뾶ʱʹ�õĶԳư뾶
	 * 			  ����������ҵ��뾶ʱ�������ֱͬ������ϵ��
	 * @param style �߿���
	 * @param weight �߿�ֶ� 
	 * @param pc ������ϵ 
	 * @param ccr �����ɫ 
	 * @param foreColor �߿���ɫ 
	 * @param transparent ͸����
	 * @return �����״����(���ڼ��㳬����)
	 */
	public static void drawPolarPoint2(Graphics2D g, Point2D point, int shape,
			double radiusR, double radiusA, double rw, int style, float weight,
			PolarCoor pc, ChartColor ccr, Color foreColor, float transparent) {
		// û��ָ������ֵҵ��뾶ʱ��ʹ�öԳƵ����ذ뾶rw radiusWeight:�뾶��ȣ���ҵ��뾶ʱʹ�õĶԳư뾶
		// ����������ҵ��뾶ʱ�������ֱͬ������ϵ��
		if (radiusR == 0 && radiusA == 0) {
			point = pc.getScreenPoint(point);
			drawCartesianPoint2(g, point, shape, 0, 0, rw, style, weight, ccr,
					foreColor, transparent);
			return;
		}
		double r = point.getX();
		double a = point.getY();
		Rectangle2D bound;
		Point2D polarDot, p1, p2;
		Arc2D arc;
		if (radiusR == 0) {// radiusRΪ0ʱ��������Ч��Ϊһ�λ���
			bound = pc.getEllipseBounds(r);
			arc = new Arc2D.Double(bound.getX(), bound.getY(),
					bound.getWidth(), bound.getHeight(), a - radiusA,
					radiusA * 2, Arc2D.OPEN);
			if (setStroke(g, foreColor, style, weight)) {
				g.draw(arc);
			}
			return;
		} else if (radiusA == 0) {// radiusAΪ0ʱ��Ϊһ�ΰ뾶ֱ��
			polarDot = new Point2D.Double(r + radiusR, a);
			p1 = pc.getScreenPoint(polarDot);
			polarDot = new Point2D.Double(r - radiusR, a);
			p2 = pc.getScreenPoint(polarDot);
			drawLine2(g, p1, p2, foreColor, style, weight);
			return;
		}

		Shape pShape = null;
		switch (shape) {
		case Consts.PT_NONE: // ��
			break;
		case Consts.PT_RECTANGLE: // ������,�������£������Σ�������ͬ������
		case Consts.PT_SQUARE: // ������,���������ݱ�Ϊһ�λ�
			Rectangle2D bigBounds = pc.getEllipseBounds(r + radiusR);
			Rectangle2D smallBounds = pc.getEllipseBounds(r - radiusR);
			draw2DRing(g, bigBounds, smallBounds, a - radiusA, radiusA * 2,
					foreColor, style, weight, transparent, ccr);
			return;
		case Consts.PT_DIAMOND: // ���Σ���ʱ������Ϊ���е�ı�������
			double[] xs_di,
			ys_di;
			xs_di = new double[4];
			ys_di = new double[4];
			polarDot = new Point2D.Double(r + radiusR, a);
			p1 = pc.getScreenPoint(polarDot);
			xs_di[0] = p1.getX();
			ys_di[0] = p1.getY();

			polarDot = new Point2D.Double(r, a + radiusA);
			p1 = pc.getScreenPoint(polarDot);
			xs_di[1] = p1.getX();
			ys_di[1] = p1.getY();

			polarDot = new Point2D.Double(r - radiusR, a);
			p1 = pc.getScreenPoint(polarDot);
			xs_di[2] = p1.getX();
			ys_di[2] = p1.getY();

			polarDot = new Point2D.Double(r, a - radiusA);
			p1 = pc.getScreenPoint(polarDot);
			xs_di[3] = p1.getX();
			ys_di[3] = p1.getY();

			pShape = newPolygon2DShape(xs_di, ys_di);
			break;
		case Consts.PT_CIRCLE: // Բ
		case Consts.PT_TRIANGLE: // ������
		case Consts.PT_STAR: // ����
		case Consts.PT_CORSS: // ����
		case Consts.PT_PLUS: // �Ӻ�
		case Consts.PT_D_CIRCEL: // ˫Բ
		case Consts.PT_D_SQUARE: // ˫������
		case Consts.PT_D_TRIANGLE: // ˫������
		case Consts.PT_D_RECTANGLE: // ˫������
		case Consts.PT_D_DIAMOND: // ˫����
		case Consts.PT_CIRCLE_PLUS: // Բ�ڼӺ�
		case Consts.PT_SQUARE_PLUS: // ���ڼӺ�
		case Consts.PT_TRIANGLE_PLUS: // �����ڼӺ�
		case Consts.PT_RECTANGLE_PLUS: // �������ڼӺ�
		case Consts.PT_DIAMOND_PLUS: // ���ڼӺ�
			throw new RQException("Unsupportted dot shape:" + shape
					+ " in polar coordinate system.");
		}
		if (pShape != null) {
			Rectangle bd = pShape.getBounds();
			if (setPointPaint(g, ccr, pShape)) {
				g.fill(pShape);
			}
			if (setStroke(g, foreColor, style, weight)) {
				g.draw(pShape);
			}
		}
	}

	/**
	 * ͼԪ�Ĳ��������Para����ʱ��Ҳ��Ҫ�������滷��
	 * @param chartElement ͼԪ
	 */
	public static void setParamsEngine(IElement chartElement) {
		try {
			Field[] fields = chartElement.getClass().getFields();
			int size = fields.length;
			for (int i = 0; i < size; i++) {
				Field f = fields[i];
				Object param = f.get(chartElement);
				if (!(param instanceof Para)) {
					continue;
				}
				((Para) param).setEngine(chartElement.getEngine());
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	private static ThreadLocal<Boolean> tlIsGif = new ThreadLocal<Boolean>() {
		protected synchronized Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	/**
	 * �̰߳�ȫ�µľ�ֵ̬��GIF��ʽ��ɫ����̫�࣬����ʹ���ı��Ĺ⻬Ч��
	 * �ô˿���������ͼ�εĻ���ģʽ
	 */
	public static void setIsGif(boolean isGif) {
		tlIsGif.set( isGif );
	}

	/**
	 * �Ƿ��̰߳�ȫ�µ�GIF��ʽ
	 */
	public static boolean isGif() {
		return (tlIsGif.get()).booleanValue();
	}

	// Ϊ�˱�֤Բ����Բ�����Լ����ֵĲ�ʹ�þ�ݵ�����״̬��ʹ�����¿��������ֻ����ֺ�Բ����������
	// ����gif��ʽʱ������ʹ�ÿ�������ԣ��������ɫ���ࡣ
//	Ŀǰ���÷��ǻ�ͼǰ�ͽ�ƽ���򿪣�Ȼ���ڻ����ı�ʱ���ȹص��������ı����ٴ򿪣���֤ͼ��ƽ��������������
	public static void setGraphAntiAliasingOn(Graphics2D g) {
		// gif��ʽʱ���ܴ򿪾�ݿ��أ��Է���ɫ���ࣻ
		if (isGif()) {//gif��ʽʱ�����ǹرգ��˴�ǿ�йرգ��Է�ͬ�߳��б�ĸ�ʽ�Ѿ���Ϊon
			setGraphAntiAliasingOff(g);
			return;
		}
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
	}

	public static void setGraphAntiAliasingOff(Graphics2D g) {
//		if (isGif())
//			return;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	/**
	 * ������seq���������������ɫת��Ϊ��Ӧ�� ��ʵ��
	 * @param seq ��������
	 * @return ChartColor�������ɫʵ����ת��ʧ��(�������ĸ�ʽ)ʱ����null
	 */
	public static ChartColor sequenceToChartColor(Sequence seq) {
		if (seq.length() < 1)
			return null;
		Object prefix = seq.get(1);
		if (StringUtils.isValidString(prefix)) {
			prefix = Escape.removeEscAndQuote((String) prefix);
			if (prefix.equals("ChartColor")) {
				return ChartColor.getInstance(seq);
			}
		}
		return null;
	}

	/**
	 * �ı����������߼����ݣ����ö��� (����ΪӢ�Ķ���) �ָ������ϵ��
	 * �÷�����ȡ���еķ���ֵ
	 * (����ֵΪ   "����,����" ʱ�����ط���ֵ  "����")
	 * @param data ����ʽ���߼�����
	 * @return ����ֵ
	 */
	public static Object parseCategory(Object data) {
		if (data == null) {
			return null;
		}
		if (!(data instanceof String)) {
			return data;
		}
		String val = (String) data;
		int commaIndex = val.indexOf(",");
		if (commaIndex > -1) {
			return val.substring(0, commaIndex);
		}
		return val;
	}

	/**
	 * �ı����������߼����ݣ����ö��� (����ΪӢ�Ķ���) �ָ������ϵ��
	 * �÷�����ȡ���е�ϵ��ֵ�����û�ж��ŷָ������ݣ����ʾû��ϵ��
	 * (����ֵΪ   "����,����" ʱ������ϵ��ֵ  "����")
	 * @param data ����ʽ���߼�����
	 * @return ϵ��ֵ
	 */
	public static Object parseSeries(Object data) {
		if (data == null) {
			return null;
		}
		if (!(data instanceof String)) {
			return data;
		}

		String val = (String) data;
		int commaIndex = val.indexOf(",");
		if (commaIndex > -1) {
			return val.substring(commaIndex + 1);
		}

		if (val.indexOf(",") > -1) {
			StringTokenizer st = new StringTokenizer(val, ",");
			st.nextToken();
			String ser = st.nextToken();
			return ser;
		}
		return null;
	}

	/**
	 * �ҳ���ǰ�ķ���catName��ϵ��serName��ö��enumData�����е��������
	 * @param enumData ����ʽ���߼�����
	 * @param catName ��������
	 * @param serName ϵ������
	 * @return int ���(��1��ʼ)
	 */
	public static int indexOf(Sequence enumData, String catName, String serName) {
		if (catName == null)
			return 0;
		String tmp = catName;
		if (serName != null) {
			tmp += "," + serName;
		}
		return enumData.firstIndexOf(tmp);
	}

	/**
	 * ͳ�Ʒ���catName�Ļ���ֵ
	 * @param catName ��������
	 * @param enumData ö��ֵ����������
	 * @param numData ��ֵ����������
	 * @return ����ֵ
	 */
	public static double sumCategory(String catName, Sequence enumData,
			Sequence numData) {
		double sum = 0;
		int size = enumData.length();
		for (int i = 1; i <= size; i++) {
			Object enumObj = enumData.get(i);
			Object cat = parseCategory(enumObj);
			if (catName.equals(cat)) {
				double d = ((Number) numData.get(i)).doubleValue();
				// �ۻ�ֵ��������
				if (d > 0) {
					sum += d;
				}
			}
		}
		return sum;
	}

	/**
	 * ����ָ����������Բ����Ķ���
	 * @param g ͼ���豸
	 * @param topOval ����Բ
	 * @param bc �߿���ɫ
	 * @param bs �߿���
	 * @param bw �߿�ֶ�
	 * @param transparent ͸����
	 * @param chartColor �����ɫ
	 * @param isVertical �Ƿ�����Բ��
	 */
	public static void drawCylinderTop(Graphics2D g, Arc2D.Double topOval,
			Color bc, int bs, float bw, float transparent,
			ChartColor chartColor, boolean isVertical) {
		Rectangle bound = topOval.getBounds();
		CubeColor ccr = new CubeColor(chartColor.getColor1());
		// ����ԲֻҪ����ɫ��ʹ����Ч��
		if (chartColor.isDazzle()) {
			if (ccr.getT1() != null && ccr.getT2() != null) {
				GradientPaint gp;
				if (isVertical) {
					gp = new GradientPaint(bound.x, bound.y, ccr.getT2(),
							bound.x + bound.width / 2, bound.y + bound.height
									/ 2, ccr.getT1(), true);
				} else {
					gp = new GradientPaint(bound.x + bound.width, bound.y,
							ccr.getT2(), bound.x + bound.width / 2, bound.y
									+ bound.height / 2, ccr.getT1(), true);
				}
				g.setPaint(gp);
				Utils.fillPaint(g, topOval, transparent);
			}
		} else if (chartColor.isGradient()) {
			ChartColor tmpcc = new ChartColor();
			tmpcc.setAngle(180);
			tmpcc.setGradient(true);
			tmpcc.setColor1(ccr.getT1());
			tmpcc.setColor2(ccr.getT2());
			if (Utils.setPaint(g, bound.x, bound.y, bound.width, bound.height,
					tmpcc)) {
				Utils.fillPaint(g, topOval, transparent);
			}
		} else {
			// g.setColor(ccr.getOrigin());// ccr.getF1());
			Utils.fill(g, topOval, transparent, ccr.getOrigin());
		}
		if (Utils.setStroke(g, bc, bs, bw)) {
			g.draw(topOval);
		}
	}

	/**
	 * ����ָ����������Բ���������
	 * @param g ͼ���豸
	 * @param front ��������
	 * @param bc �߿���ɫ
	 * @param bs �߿���
	 * @param bw �߿�ֶ�
	 * @param transparent ͸����
	 * @param chartColor �����ɫ
	 * @param isVertical �Ƿ�����Բ��
	 */
	public static void drawCylinderFront(Graphics2D g,
			java.awt.geom.Area front, Color bc, int bs, float bw,
			float transparent, ChartColor chartColor, boolean isVertical) {
		drawCylinderFront(g, front, bc, bs, bw, transparent, chartColor,
				isVertical, null, false);
	}

	/**
	 * ����ָ����������Բ���������
	 * @param g ͼ���豸
	 * @param front ��������
	 * @param bc �߿���ɫ
	 * @param bs �߿���
	 * @param bw �߿�ֶ�
	 * @param transparent ͸����
	 * @param chartColor �����ɫ
	 * @param isVertical �Ƿ�����Բ��
	 * @param shinningRange ��Ч������
	 * @param isCurve �Ƿ�����
	 */
	public static void drawCylinderFront(Graphics2D g,
			java.awt.geom.Area front, Color bc, int bs, float bw,
			float transparent, ChartColor chartColor, boolean isVertical,
			Rectangle2D shinningRange, boolean isCurve) {
		double x1, y1, x2, y2;
		CubeColor ccr = new CubeColor(chartColor.getColor1());
		Rectangle bound = front.getBounds();
		GradientPaint paint;
		Color c1, c2;
		// ��Ч��
		if (chartColor.isDazzle()) {
			Rectangle2D r2d = shinningRange;
			if (r2d == null) {
				r2d = bound.getBounds2D();
			}
			java.awt.geom.Area tmp;
			if (isVertical) {
				tmp = new java.awt.geom.Area(new Rectangle2D.Double(r2d.getX(),
						r2d.getY(), r2d.getWidth() * 2 / 3, r2d.getHeight()));

			} else {
				tmp = new java.awt.geom.Area(new Rectangle2D.Double(r2d.getX(),
						r2d.getY(), r2d.getWidth(), r2d.getHeight() * 2 / 3));

			}
			java.awt.geom.Area leftOrTop = (java.awt.geom.Area) front.clone();
			leftOrTop.intersect(tmp);
			if (!leftOrTop.isEmpty()) {
				bound = tmp.getBounds();
				if (isVertical) {
					x1 = bound.x;
					y1 = bound.y;
					x2 = bound.x + bound.width / 2;
					y2 = y1;
				} else {
					x1 = bound.x;
					y1 = bound.y;
					x2 = x1;
					y2 = y1 + bound.height / 2;
				}
				if (isCurve) {
					c1 = ccr.getRelativeBrighter("T2", 1);
					c2 = ccr.getRelativeBrighter("T1", 1);
				} else {
					c1 = ccr.getT2();
					c2 = ccr.getT1();
				}
				if (c1 != null && c2 != null) {
					paint = new GradientPaint((int) x1, (int) y1, c1, (int) x2,
							(int) y2, c2, true);

					g.setPaint(paint);
					Utils.fillPaint(g, leftOrTop, transparent);
				}
			}

			java.awt.geom.Area rightOrBottom = (java.awt.geom.Area) front
					.clone();
			double d;
			if (isVertical) {
				d = r2d.getWidth() * 2 / 3;
				tmp = new java.awt.geom.Area(new Rectangle2D.Double(r2d.getX()
						+ d - 1, r2d.getY(), r2d.getBounds().width - d + 1,
						r2d.getHeight()));// ������������������ұ���������һ������
			} else {
				d = r2d.getHeight() * 2 / 3;
				tmp = new java.awt.geom.Area(new Rectangle2D.Double(r2d.getX(),
						r2d.getY() + d - 1, r2d.getWidth(),
						r2d.getBounds().height - d + 1));
			}

			rightOrBottom.intersect(tmp);
			if (!rightOrBottom.isEmpty()) {
				bound = tmp.getBounds();
				if (isVertical) {
					x1 = bound.x;
					y1 = bound.y;
					x2 = bound.x + bound.width * 0.6f;
					y2 = y1;
				} else {
					x1 = bound.x;
					y1 = bound.y;
					x2 = x1;
					y2 = bound.y + bound.height * 0.6f;
				}
				if (isCurve) {
					c1 = ccr.getRelativeBrighter("T2", 1);
					c2 = ccr.getRelativeBrighter("F2", 1);
				} else {
					c1 = ccr.getT2();
					c2 = ccr.getF2();
				}
				if (c1 != null && c2 != null) {
					paint = new GradientPaint((int) x1, (int) y1, c1, (int) x2,
							(int) y2, c2, false);
					g.setPaint(paint);
					Utils.fillPaint(g, rightOrBottom, transparent);
				}
			}
		} else {
			if (Utils.setPaint(g, bound.x, bound.y, bound.width, bound.height,
					chartColor)) {
				Utils.fillPaint(g, front, transparent);
			}
		}

		// ���ӵı߿�
		if (Utils.setStroke(g, bc, bs, bw)) {
			g.draw(front);
		}
	}

	/**
	 * ���Ƽ�����ϵ�µ��ı�
	 * @param e ��ͼ����
	 * @param txt �ı�
	 * @param pc ������ϵ
	 * @param txtPolarPoint �������
	 * @param fontName ����
	 * @param fontStyle ���
	 * @param fontSize �ֺ�
	 * @param c ǰ��ɫ
	 * @param textOverlapping �����ı��ص�
	 */
	public static void drawPolarPointText(Engine e, String txt, PolarCoor pc,
			Point2D txtPolarPoint, String fontName, int fontStyle,
			int fontSize, Color c, boolean textOverlapping) {
		drawPolarPointText(e, txt, pc, txtPolarPoint, fontName, fontStyle,
				fontSize, c, textOverlapping, 0);

	}

	/**
	 * ���Ƽ�����ϵ�µ��ı�
	 * @param e ��ͼ����
	 * @param txt �ı�
	 * @param pc ������ϵ
	 * @param txtPolarPoint �������
	 * @param fontName ����
	 * @param fontStyle ���
	 * @param fontSize �ֺ�
	 * @param c ǰ��ɫ
	 * @param textOverlapping �����ı��ص�
	 * @param specifiedLocation ������ĵ㷽λ
	 */
	public static void drawPolarPointText(Engine e, String txt, PolarCoor pc,
			Point2D txtPolarPoint, String fontName, int fontStyle,
			int fontSize, Color c, boolean textOverlapping,
			int specifiedLocation) {
		if (!StringUtils.isValidString(txt)) {
			return;
		}
		double angle = txtPolarPoint.getY();
		int locationType = Consts.LOCATION_CM;
		if (specifiedLocation > 0) {
			locationType = specifiedLocation;
		} else {
			locationType = getAngleTextLocation(angle);
		}
		Font font = getFont(fontName, fontStyle, fontSize);
		Point2D txtP = pc.getScreenPoint(txtPolarPoint);
		drawText(e, txt, txtP.getX(), txtP.getY(), font, c, null, fontStyle, 0,
				locationType, textOverlapping);

	}

	/**
	 * ָ����������ƽ�滷
	 * @param g ͼ���豸
	 * @param bigBounds �⻷�߽�
	 * @param smallBounds �ڻ��߽�
	 * @param start ��ʼ�Ƕ�
	 * @param extent �Ƕȷ�Χ
	 * @param borderColor �߿���ɫ
	 * @param borderStyle �߿���
	 * @param borderWeight �߿�ֶ�
	 * @param transparent ͸����
	 * @param fillColor �����ɫ
	 * @return ��Ӧ��������״
	 */
	public static Shape draw2DRing(Graphics2D g, Rectangle2D bigBounds,
			Rectangle2D smallBounds, double start, double extent,
			Color borderColor, int borderStyle, float borderWeight,
			float transparent, ChartColor fillColor) {
		return draw2DRing(g, bigBounds, smallBounds, start, extent,
				borderColor, borderStyle, borderWeight, transparent, fillColor,
				false);
	}

	/**
	 * ָ����������ƽ�滷
	 * @param g ͼ���豸
	 * @param bigBounds �⻷�߽�
	 * @param smallBounds �ڻ��߽�
	 * @param start ��ʼ�Ƕ�
	 * @param extent �Ƕȷ�Χ
	 * @param borderColor �߿���ɫ
	 * @param borderStyle �߿���
	 * @param borderWeight �߿�ֶ�
	 * @param transparent ͸����
	 * @param fillColor �����ɫ
	 * @param fillAsPie �ڰ뾶Ϊ0,��ֻ��һ������ʱ�����˻�Ϊ�ȣ�ֱ�ӻ����Σ����ϲ��������Ƿ�ֻ��һ������
	 * @return ��Ӧ��������״
	 */
	public static Shape draw2DRing(Graphics2D g, Rectangle2D bigBounds,
			Rectangle2D smallBounds, double start, double extent,
			Color borderColor, int borderStyle, float borderWeight,
			float transparent, ChartColor fillColor, boolean fillAsPie) {
		double x, y, w, h;
		x = (bigBounds.getX() + smallBounds.getX()) / 2;
		y = (bigBounds.getY() + smallBounds.getY()) / 2;
		w = (bigBounds.getWidth() + smallBounds.getWidth()) / 2;
		h = (bigBounds.getHeight() + smallBounds.getHeight()) / 2;
		Rectangle2D midBounds = new Rectangle2D.Double(x, y, w, h);

		// С��Բ����������10�ȣ���ֹ������ͬʱ�����㾫������������������һ��ֱ�ߵ�״̬
		Arc2D smallSector = new Arc2D.Double(smallBounds, start - 5,
				extent + 10, Arc2D.PIE);
		Arc2D bigSector = new Arc2D.Double(bigBounds, start, extent, Arc2D.PIE);
		java.awt.geom.Area ring = new java.awt.geom.Area(bigSector);
		ring.subtract(new java.awt.geom.Area(smallSector));

		Rectangle rect = bigBounds.getBounds();
		ChartColor cc = fillColor;
		double endAngle = start + extent;

		if (cc.isDazzle()) {
			Color dazzel = CubeColor.getDazzelColor(cc.getColor1());
			CubeColor ccr = new CubeColor(dazzel);
			if (fillAsPie) {
				if (ccr.getF1() != null && ccr.getF2() != null) {
					GradientPaint gp = new GradientPaint(rect.x, rect.y
							+ rect.height, ccr.getF2(),
							rect.x + rect.width / 2, rect.y + rect.height / 2,
							ccr.getF1(), true);
					g.setPaint(gp);
					Utils.fillPaint(g, ring, transparent);
				}
			} else {// Բ��,donut
				double dAngle = 3;
				double sAngle = start;
				while (sAngle <= endAngle) {
					smallSector = new Arc2D.Double(smallBounds, sAngle - 5,
							dAngle + 10, Arc2D.PIE);
					double tmps = sAngle, tmpa = dAngle;
					if (tmps != start) {// ��ÿһ��dring�˴��ص�������delta֮��Ŀ�϶
						tmps -= dAngle;
						tmpa += dAngle * 2;
					}
					if (tmps + tmpa > endAngle) {
						tmpa = endAngle - tmps;
					}
					bigSector = new Arc2D.Double(bigBounds, tmps, tmpa,
							Arc2D.PIE);
					java.awt.geom.Area dRing = new java.awt.geom.Area(bigSector);
					dRing.subtract(new java.awt.geom.Area(smallSector));
					smallSector = new Arc2D.Double(smallBounds, sAngle,
							dAngle / 2f, Arc2D.PIE);
					bigSector = new Arc2D.Double(midBounds, sAngle,
							dAngle / 2f, Arc2D.PIE);
					// delta���Ļ��е�
					Point2D dp1 = smallSector.getEndPoint();
					Point2D dp2 = bigSector.getEndPoint();
					if (ccr.getF1() != null && ccr.getF2() != null) {
						g.setPaint(new GradientPaint(dp1, ccr.getF2(), dp2, ccr
								.getF1(), true));
						g.fill(dRing);
					}
					sAngle += dAngle;
				}

			}
		} else {
			if (Utils.setPaint(g, rect.x, rect.y, rect.width, rect.height,
					fillColor)) {
				Utils.fillPaint(g, ring, transparent);
			}
		}

		int style = borderStyle;
		float weight = borderWeight;
		if (Utils.setStroke(g, borderColor, style, weight)) {
			bigSector = new Arc2D.Double(bigBounds, start, extent, Arc2D.OPEN);
			g.draw(bigSector);
			Line2D line;
			Point2D spb, spe;
			if (smallBounds.getWidth() == 0) {
				spe = new Point2D.Double(bigBounds.getCenterX(),
						bigBounds.getCenterY());
				spb = spe;
			} else {
				smallSector = new Arc2D.Double(smallBounds, start, extent,
						Arc2D.OPEN);
				g.draw(smallSector);
				spb = smallSector.getStartPoint();
				spe = smallSector.getEndPoint();
			}
			line = new Line2D.Double(spb, bigSector.getStartPoint());
			g.draw(line);
			line = new Line2D.Double(spe, bigSector.getEndPoint());
			g.draw(line);
		}

		return ring;
	}

	/**
	 * ��װ�����滻�����������������ŵ�ת������ݣ�Ҳ���ǳ����滻
	 * @param src Դ�ı���
	 * @param sold ��Ҫ�滻�����ı�
	 * @param snew ���ı�
	 * @return �滻��ɵ�Ŀ�괮
	 */
	public static String replaceAll(String src, String sold, String snew) {
		return Sentence.replace(src, 0, sold, snew, Sentence.IGNORE_CASE
				+ Sentence.IGNORE_PARS + Sentence.IGNORE_QUOTE
				+ Sentence.ONLY_PHRASE);
	}

	/**
	 * ���������ı������������滻����Ӧ�Ĳ�����ʽ
	 * @param link Ҫ�滻�İ���������ʽ�Ĵ�
	 * @param data1  �滻��ʽ"@data1"����
	 * @param data2  �滻��ʽ"@data2"����
	 * @param text  �滻��ʽ"@text"����
	 * @param legend  �滻��ʽ"@legend"����
	 * @return �滻��Ĵ�
	 */
	public static String getHtmlLink(String link, Object data1, Object data2,
			String text, String legend) {
		if (!StringUtils.isValidString(link))
			return null;

		if (data1 != null) {
			link = replaceAll(link, "@data1", data1.toString());
		}
		if (data2 != null) {
			link = replaceAll(link, "@data2", data2.toString());
		}
		if (StringUtils.isValidString(text)) {
			link = replaceAll(link, "@text", text);
		}
		if (StringUtils.isValidString(legend)) {
			link = replaceAll(link, "@legend", legend);
		}
		return link;
	}

	/**
	 * �ı����Ϊ������ʱ������Ϊ����ͼ�������
	 * @param value �ı�ͼԪ
	 * @param index �ı�����Ϊ����ʱ���ı����
	 * @param fore ǰ��ɫ
	 * @param back ����ɫ
	 * @return ����ͼ��
	 */
	public static BufferedImage calcBarcodeImage(Text value, int index, Color fore,
			Color back) {
		int TRANSPARENT = 0x00FFFFFF;// ��������ȫ0������ɵ���pdfʧ�ܣ�
		int BLACK = 0xFF000000;
		int WHITE = 0xFFFFFFFF;

		int w = value.getWidth(index);
		int h = value.getHeight(index);
		if (w <= 0) {
			w = 40;
		}
		if (h <= 0) {
			h = 40;
		}
		int backColor = WHITE;
		int foreColor = BLACK;
		if (back != null) {
			backColor = back.getRGB();
		}
		if (fore != null) {
			foreColor = fore.getRGB();
		}

		if (value.barType == Consts.TYPE_QRCODE) {
			// ����Ƕ�ά�룬����������������ͼ����������QuietZone�İױߣ����30���������ƣ�Ȼ��ȡ��������
			int minWH = w < h ? w : h;
			w = h = (minWH + 30);// ����30�߿��Ա�ü���������󣬸��ӽ���ʵ�ʿ��
		} else if (value.dispText) {
			// ����������룬����ʾ���֣��������ֿռ�
			Rectangle rect = getTextRect(value,index);
			h -= (rect.height + 10);
		}

		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
		// ˵���ַ�������Сд��ĳЩɨ��ǹ�������������ж����ַ� xq 2016��12��1��
		hints.put(EncodeHintType.CHARACTER_SET, value.charSet.toLowerCase());
		char errLevel = value.recError.charAt(0);
		ErrorCorrectionLevel ecl;
		if (errLevel == 'L') {
			ecl = ErrorCorrectionLevel.L;
		} else if (errLevel == 'M') {
			ecl = ErrorCorrectionLevel.M;
		} else if (errLevel == 'Q') {
			ecl = ErrorCorrectionLevel.Q;
		} else {
			ecl = ErrorCorrectionLevel.H;
		}
		hints.put(EncodeHintType.ERROR_CORRECTION, ecl);
		if(value.barType==Consts.TYPE_QRCODE) {
//			��ά��ĳ�Ĭ��Ϊ������
			hints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_SQUARE);
		}
		try {
			BarcodeFormat bf = convertBarcodeFormat(value.barType);
			BitMatrix bitMatrix;
			if(isCode128ABC(value.barType)) {
		         Writer writer = new Code128ABC(value.barType);
		         bitMatrix = writer.encode(value.getDispText(index), bf, w,h, hints);
			}else {
				bitMatrix = multiFormatWriter.encode(value.getDispText(index), bf, w,
						h, hints);
			}
			if (value.barType == Consts.TYPE_QRCODE) {
				return toBufferedQRImage(value, bitMatrix, foreColor, backColor);
			} else {
				return toBufferedImage(value,index, bitMatrix, foreColor, backColor);
			}
		} catch (Exception x) {
			String val = value.toString();
			String upper = val.toUpperCase();
			boolean isUpper = upper.equals(val);
			if (value.barType == Consts.TYPE_CODE39 && !isUpper) {
				throw new RQException(
						"Only upper case characters were supported by Code39!");
			}
			throw new RQException(x.getMessage(), x);
		}
	}

	private static Point get417Margin(BitMatrix matrix) {
		Point p = new Point();
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (matrix.get(x, y))
					return new Point(x - 1, y - 1);
			}
		}
		return p;
	}

	/**
	 * �����ı�ͼԪ�е�index���ı��Ŀռ�λ��
	 * @param value �ı�ͼԪ
	 * @param index �������
	 * @return ���������Ŀռ�
	 */
	public static Rectangle getTextRect(Text value,int index) {
		Font font = new Font(value.textFont.stringValue(index), Font.PLAIN, value.textSize.intValue(index));
		Graphics2D g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
				.createGraphics();
		FontMetrics fm = g.getFontMetrics(font);
		int txtHeight = fm.getHeight();
		g.dispose();
		return new Rectangle(0, 0, fm.stringWidth(value.getDispText(index)), txtHeight);
	}

	private static BufferedImage toBufferedImage(Text value, int index,BitMatrix matrix,
			int foreColor, int backColor) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int x1 = 0, x2 = width;
		int y1 = 0, y2 = height;
		// pdf417, ����ߴ���Զ��Ŵ�
		if (value.barType == Consts.TYPE_PDF417) {
			Point leftUpCorner = get417Margin(matrix);
			x1 = leftUpCorner.x;
			y1 = leftUpCorner.y;
			x2 = width - x1;
			y2 = height - y1;
		}

		int dw = x2 - x1;
		int dh = y2 - y1;
		Font font = new Font(value.textFont.stringValue(index), Font.PLAIN, value.textSize.intValue(index));
		if (value.dispText) {
			Rectangle rect = getTextRect(value,index);
			dh = dh + rect.height;
		}
		BufferedImage image = new BufferedImage(dw, dh,
				BufferedImage.TYPE_INT_RGB);// ����ʹ��ARGB�����򵼳���Excel���а�͸��Ч�� xq
											// 2016��11��22��
		Graphics2D g = image.createGraphics();
		g.setBackground(new Color(backColor));
		g.clearRect(0, 0, dw, dh);
		FontMetrics fm = g.getFontMetrics(font);
		int txtHeight = 0;

		if (value.dispText) {
			// ���������Զ��������ָ߶Ⱥ󣬲���Ҫ�жϸ߶ȿռ���
			// txtHeight = fm.getHeight();
			boolean isSpaceEnough = (txtHeight < dh - 10);
			int txtWidth = fm.stringWidth(value.getDispText(index));
			int x = (dw - txtWidth) / 2;
			isSpaceEnough = x >= 0;
			if (!isSpaceEnough) {
				throw new RQException(
						"Barcode is too narrow to draw text on it.Please set smaller font size or hide text.");
			}

		}

		for (int x = x1; x < x2; x++) {
			for (int y = y1; y < y2; y++) {
				image.setRGB(x - x1, y - y1, matrix.get(x, y) ? foreColor
						: backColor);// BLACK:NULL);
			}
		}

		if (value.dispText) {
			g.setColor(new Color(foreColor));
			g.setFont(font);
			int txtWidth = fm.stringWidth(value.getDispText(index));
			txtHeight = fm.getHeight();
			int x = (dw - txtWidth) / 2;
			int y = dh - fm.getDescent();
			g.drawString(value.getDispText(index), x, y);
		}
		g.dispose();
		return image;
	}

	/**
	 * ����ά����Ϣmatrix�ں��ı����ݣ������ӡlogo����ά��
	 * @param text �ı�ͼԪ
	 * @param matrix ��ά����Ϣ����
	 * @param foreColor ǰ��ɫ
	 * @param backColor ����ɫ
	 * @return �ں��ı���Ϣ�Ķ�ά�뻺��ͼ��
	 */
	public static BufferedImage toBufferedQRImage(Text text, BitMatrix matrix,
			int foreColor, int backColor) {
		int width = matrix.getWidth();

		int margin = getMarginIndex(matrix);
		int dataSize = width - margin * 2;
		int dataEnd = margin + dataSize;
		BufferedImage image = new BufferedImage(dataSize, dataSize,
				BufferedImage.TYPE_INT_RGB);
		for (int x = margin; x < dataEnd; x++) {
			for (int y = margin; y < dataEnd; y++) {
				image.setRGB(x - margin, y - margin,
						matrix.get(x, y) ? foreColor : backColor);
			}
		}
		int scale = (int) ((width - 30) * 100 / dataSize);
		image = scaleImage(image, scale);
		image = printLogo(image, text);
		return image;
	}

	private static int getMarginIndex(BitMatrix matrix) {
		int width = matrix.getWidth();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < width; y++) {
				if (matrix.get(x, y))
					return x - 1;
			}
		}
		return 0;
	}

	private static BufferedImage scaleImage(BufferedImage src, int scale) {
		if (scale == 100) {
			return src;
		}
		float width = src.getWidth();
		int scaleWidth = (int) (width * scale * 0.01);
		Image image = src.getScaledInstance(scaleWidth, scaleWidth,
				Image.SCALE_DEFAULT);

		BufferedImage buffer = new BufferedImage(scaleWidth, scaleWidth,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = buffer.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		return buffer;
	}

	/**
	 * ���������������ļ������ݣ�����λ������·���µ��ļ�
	 * @param fileValue �ļ��������ڼ��ݵ��ã��ļ�����������Ѿ�Ϊ�ֽ�����
	 * @return �ļ����ݵ��ֽ�����
	 */
	public static byte[] getFileBytes(Object fileValue) {
		Object imgVal = fileValue;
		byte[] imageBytes = null;
		if (imgVal instanceof byte[]) {
			imageBytes = (byte[]) imgVal;
		} else if (imgVal instanceof String) {
			try {
				String path = (String) imgVal;
				FileObject fo = new FileObject(path, "s");
				int size = (int) fo.size();
				imageBytes = new byte[size];
				InputStream is = fo.getInputStream();
				is.read(imageBytes);
				is.close();
			} catch (Exception x) {
				throw new RQException(x.getMessage(),x);
			}
		}else if(imgVal!=null){
			throw new RQException(mm.getMessage("Utils.invalidfile",fileValue));
		}
		return imageBytes;
	}

	private static BufferedImage printLogo(BufferedImage src, Text value) {
		Para logoValue = value.logoValue;
		byte[] bytes = getFileBytes(logoValue.getValue());
		if(bytes==null) return src;
		
		ImageIcon icon = new ImageIcon(bytes);
		Image image = icon.getImage();

		int width = src.getWidth();
		double center = width / 2;
		double logoPercent = value.logoSize / 100.0f;
		int size = (int) (width * logoPercent);
		if (size % 2 != 0) {
			size += 1;
		}
		double shift = size / 2;
		image = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		image = new ImageIcon(image).getImage();

		Graphics2D g2 = src.createGraphics();
		int x = (int) (center - shift);
		if (value.logoFrame) {
			g2.setColor(Color.white);
			int edge = 2;
			Rectangle rect = new Rectangle(x - edge, x - edge, size + edge * 2
					- 1, size + edge * 2 - 1);
			g2.fill(rect);
			g2.setColor(Color.lightGray);
			g2.draw(rect);
		}

		boolean b = g2.drawImage(image, x, x, null);
		g2.dispose();
		return src;
	}
	
	/**
	 * zxing��code28ֻ��auto���ͣ�����һ����Code128ABC
	  *  ʹ��ǿ�Ƶ�ABC����
	 * @param raqType
	 * @return
	 */
	public static boolean isCode128ABC(int raqType) {
		return raqType==Consts.TYPE_CODE128A ||
				raqType==Consts.TYPE_CODE128B ||
				raqType==Consts.TYPE_CODE128C;
	}

	private static BarcodeFormat convertBarcodeFormat(int raqType) {
		switch (raqType) {
		case Consts.TYPE_CODABAR:
			return BarcodeFormat.CODABAR;
		case Consts.TYPE_CODE128:
		case Consts.TYPE_CODE128A:
		case Consts.TYPE_CODE128B:
		case Consts.TYPE_CODE128C:
			return BarcodeFormat.CODE_128;
		case Consts.TYPE_CODE39:
			// Code 39ֻ��������43����Ч�����ַ��� ����26����д��ĸ��A - Z���� ����ʮ�����֣�0 - 9��
			// ע��Сд��ĸ�ᱨ��
			return BarcodeFormat.CODE_39;
		case Consts.TYPE_EAN13:// ean13�Ա������ϸ���򣬲�������������,ʾ��ֵ��7501054530107
			return BarcodeFormat.EAN_13;
		case Consts.TYPE_EAN8:
			return BarcodeFormat.EAN_8;
		case Consts.TYPE_ITF:
			return BarcodeFormat.ITF;
		case Consts.TYPE_PDF417:
			return BarcodeFormat.PDF_417;
		case Consts.TYPE_UPCA:
			return BarcodeFormat.UPC_A;
		}

		return BarcodeFormat.QR_CODE;
	}

	/**
	 * ���������p1��p2��ֱ���ϵĵ�
	 * @param p1 �˵�1
	 * @param p2 �˵�2
	 * @param x, ���������ֱ�ߣ�������x����
	 * @return x��Ӧ��y����
	 */
	public static double calcLineY(Point2D.Double p1, Point2D.Double p2, double x){
//		ֱ�ߵķ���Ϊy=kx+b
//		y1=kx1+b;y2=kx2+b;
//		k=(y2-y1)/(x2-x1)
//		b = y1-kx1
		double x1, y1, x2, y2;
		x1 = p1.x;
		y1 = p1.y;
		x2 = p2.x;
		y2 = p2.y;
		double k, b;
		k = (y2-y1)/(x2-x1);
		b = y1-k*x1;
		double y = k*x+b;
		return y;
	}
	
	public static void main(String[] args) {
		IParam param = ParamParser.parse("a,b:c,d", null, null);
		ArrayList al = new ArrayList();
		param.getAllLeafExpression(al);

		for (int i = 0; i < al.size(); i++) {
			System.out.println(al.get(i).toString());
		}
	}

}
