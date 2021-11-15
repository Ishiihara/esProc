package com.raqsoft.cellset.graph.draw;

import java.awt.*;
import java.util.*;

import com.raqsoft.cellset.graph.*;
import com.raqsoft.cellset.graph.config.IGraphProperty;
import com.raqsoft.chart.ChartColor;
import com.raqsoft.chart.Consts;
import com.raqsoft.chart.Utils;
import com.raqsoft.common.*;
/**
 * ��ά�ѻ�����ͼ��ʵ��
 * @author Joancy
 *
 */

public class DrawBarStacked3DObj extends DrawBase {
	/**
	 * ʵ�ֻ�ͼ����
	 */
	public void draw(StringBuffer htmlLink) {
		drawing(this, htmlLink);
	}

	/**
	 * ���ݻ�ͼ����db��ͼ��������ͼ��ĳ����Ӵ���htmlLink
	 * @param db ����Ļ�ͼ����
	 * @param htmlLink �����ӻ���
	 */
	public static void drawing(DrawBase db, StringBuffer htmlLink) {
		// �ٸĶ����룬ͬ������Ҫ�õ���ʵ��
		GraphParam gp = db.gp;
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;

		double seriesWidth;
		double coorWidth;
		double categorySpan;
		double delx;
		int tmpInt;
		int x, y;

		gp.maxValue = gp.maxPositive;
		gp.minValue = gp.minNegative;
		db.initGraphInset();

		db.createCoorValue();

		db.drawLegend(htmlLink);
		db.drawTitle();
		db.drawLabel();
		db.keepGraphSpace();

		db.adjustCoorInset();
		gp.graphRect = new Rectangle(gp.leftInset, gp.topInset, gp.graphWidth
				- gp.leftInset - gp.rightInset, gp.graphHeight - gp.topInset
				- gp.bottomInset);
		if (gp.graphRect.width < 10 || gp.graphRect.height < 10) {
			return;
		}

		if (gp.coorWidth < 0 || gp.coorWidth > 10000) {
			gp.coorWidth = 0;
		}

		int serNum = 1;
		if (egp.category2 != null) {
			serNum = 2;
		}
		
		if (gp.barDistance > 0) {
			double maxCatSpan = (gp.graphRect.height - serNum * gp.catNum* 1.0f)
					/ (gp.catNum + 1.0f);
			if (gp.barDistance <= maxCatSpan) {
				categorySpan = gp.barDistance;
			} else {
				categorySpan = maxCatSpan;
			}
			seriesWidth = (gp.graphRect.height - (gp.catNum + 1) * categorySpan)
					/ (serNum * gp.catNum);
		} else {
			seriesWidth = (gp.graphRect.height / (((gp.catNum + 1)
					* gp.categorySpan / 100.0) + gp.coorWidth / 200.0 + gp.catNum * serNum));
			categorySpan = (seriesWidth * (gp.categorySpan / 100.0));
		}

		coorWidth = seriesWidth * (gp.coorWidth / 200.0);
		tmpInt = (int) ((gp.catNum + 1) * categorySpan + coorWidth + gp.catNum
				* serNum * seriesWidth);
		
		gp.graphRect.y += (gp.graphRect.height - tmpInt) / 2;
		gp.graphRect.height = tmpInt;

		delx = (gp.graphRect.width - coorWidth) / gp.tickNum;
		tmpInt = (int) (delx * gp.tickNum + coorWidth);
		gp.graphRect.x += (gp.graphRect.width - tmpInt) / 2;
		gp.graphRect.width = tmpInt;

		gp.gRect1 = new Rectangle(gp.graphRect);
		gp.gRect2 = new Rectangle(gp.graphRect);

		gp.gRect1.y += coorWidth;
		gp.gRect1.width -= coorWidth;
		gp.gRect1.height -= coorWidth;
		gp.gRect2.x += coorWidth;
		gp.gRect2.width -= coorWidth;
		gp.gRect2.height -= coorWidth;

		/* �������� */
		db.drawGraphRect();
		Point p;
		/* ��X�� */
		for (int i = 0; i <= gp.tickNum; i++) {
			db.drawGridLineV(delx, i);

			// ��x���ǩ
			Number coorx = (Number) gp.coorValue.get(i);
			String scoorx = db.getFormattedValue(coorx.doubleValue());
			p = db.getHTickPoint(i * delx);
			gp.GFV_XLABEL.outText(p.x, p.y + gp.tickLen, scoorx);
			// ���û���
			if (coorx.doubleValue() == gp.baseValue + gp.minValue) {
				gp.valueBaseLine = (int) (gp.gRect1.x + i * delx);
			}
		}

		/* �Ȼ��������� */
//		��������Ҫ������߿�ʼ�����Ա�֤�ұ߸�����ߣ��ϱ߸����±�
		ArrayList<Desc3DRect> negativeRects = new ArrayList<Desc3DRect>();
		ArrayList cats = egp.categories;
		int cc = cats.size();
		Color c;
		 for (int i = 0; i < cc; i++) {
			double dely = (i + 1) * categorySpan + i * seriesWidth * serNum
					+ seriesWidth * serNum / 2.0;
			boolean vis = i % (gp.graphXInterval + 1) == 0;
			if (vis) {
				int yy = gp.gRect2.y + (int)dely;
				db.drawGridLineCategory( yy );
			}
		 }

		
		if (gp.minNegative < 0) {
			 for (int i = 0; i < cc; i++) {
				ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
				double dely = (i + 1) * categorySpan + i * seriesWidth * serNum
						+ seriesWidth * serNum / 2.0;
				boolean vis = i % (gp.graphXInterval + 1) == 0;
				p = db.getVTickPoint(dely);
				if (vis) {
					c = egp.getAxisColor(GraphProperty.AXIS_LEFT);
					Utils.setStroke(g, c, Consts.LINE_SOLID, 1.0f);
					db.drawLine(p.x - gp.tickLen, p.y, p.x, p.y, c);
				}

				String value = egc.getNameString();
				x = gp.gRect1.x - gp.tickLen;// - TR.width
				y = gp.gRect1.y + (int)dely;// + TR.height / 2;
				gp.GFV_YLABEL.outText(x, y, value, vis);
				
				if (gp.graphTransparent) {
					g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, 0.60F));
				}
				int negativeBase = gp.valueBaseLine;
				double lb = gp.gRect1.y + (i + 1) * categorySpan + (i
						* serNum + 0)
						* seriesWidth;

				if (egp.category2 == null) {
					drawNegativeSeries(0, gp.serNames,egc,
							delx, db, lb, 
							seriesWidth, htmlLink, negativeBase,
							coorWidth, vis,negativeRects);
				}else{
					double lb2 = gp.gRect1.y + (i + 1) * categorySpan + (i
							* serNum + 1)
							* seriesWidth;
					egc = (ExtGraphCategory) egp.category2.get(i);
					drawNegativeSeries(gp.serNames.size(), gp.serNames2,egc,
							delx, db, lb2, 
							seriesWidth, htmlLink, negativeBase,
							coorWidth, vis,negativeRects);

					egc = (ExtGraphCategory) cats.get(i);
					drawNegativeSeries(0, gp.serNames,egc,
							delx, db, lb, 
							seriesWidth, htmlLink, negativeBase,
							coorWidth, vis,negativeRects);
					
				}

			 }
			
			for(int i = negativeRects.size()-1; i>=0; i--){
				Desc3DRect d3 = negativeRects.get(i);
				Utils.draw3DRect(g, d3);
			}
		}

		// �����и�ʱ���Ȼ��ƻ���͸��ƽ�棬
		db.drawLine(gp.valueBaseLine, gp.gRect1.y, gp.valueBaseLine,
				gp.gRect1.y + gp.gRect1.height,
				egp.getAxisColor(GraphProperty.AXIS_BOTTOM));

		if (gp.valueBaseLine != gp.gRect1.x) {
			int xx[] = { gp.valueBaseLine,
					(int) (gp.valueBaseLine + coorWidth),
					(int) (gp.valueBaseLine + coorWidth), gp.valueBaseLine };
			int yy[] = { gp.gRect1.y, (int) (gp.gRect1.y - coorWidth),
					(int) (gp.gRect1.y + gp.gRect1.height - coorWidth),
					(int) (gp.gRect1.y + gp.gRect1.height) };
			Polygon poly = new Polygon(xx, yy, 4);

			Color ccc = egp.getAxisColor(GraphProperty.AXIS_BOTTOM);
			if (ccc == null) {// ����ױ�Ϊ͸��ɫʱ��ʹ��ȱʡ��
				ccc = Color.lightGray;
			}
			float trans = 1.0f;
			if (gp.graphTransparent) {
				trans = 0.4f;
			}

			Utils.fill(g, poly, trans,ccc);
		}

		// ��������
		db.drawWarnLineH();

		/* ���������� */
		for (int i = cc - 1; i >= 0; i--) {
			ExtGraphCategory egc = (ExtGraphCategory) cats.get(i);
			double dely = (i + 1) * categorySpan + i * seriesWidth * serNum
					+ seriesWidth * serNum / 2.0;
			boolean vis = i % (gp.graphXInterval + 1) == 0;
			p = db.getVTickPoint(dely);
			if (vis) {
				c = egp.getAxisColor(GraphProperty.AXIS_LEFT);
				Utils.setStroke(g, c, Consts.LINE_SOLID, 1.0f);
				db.drawLine(p.x - gp.tickLen, p.y, p.x, p.y, c);
			}

			String value = egc.getNameString();
			x = gp.gRect1.x - gp.tickLen;
			y = gp.gRect1.y + (int)dely;
			gp.GFV_YLABEL.outText(x, y, value, vis);
			
			if (gp.graphTransparent) {
				g.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.60F));
			}
			int positiveBase = gp.valueBaseLine;
			double lb = gp.gRect1.y + (i + 1) * categorySpan + (i
					* serNum + 0)
					* seriesWidth;

			if (egp.category2 == null) {
				drawPositiveSeries(0, gp.serNames,egc,
						delx, db, lb,positiveBase, 
						seriesWidth, htmlLink,
						coorWidth, vis);
			}else{
				double lb2 = gp.gRect1.y + (i + 1) * categorySpan + (i
						* serNum + 1)
						* seriesWidth;
				egc = (ExtGraphCategory) egp.category2.get(i);
				drawPositiveSeries(gp.serNames.size(), gp.serNames2,egc,
						delx, db, lb2,positiveBase, 
						seriesWidth, htmlLink, 
						coorWidth, vis);

				egc = (ExtGraphCategory) cats.get(i);
				drawPositiveSeries(0, gp.serNames,egc,
						delx, db, lb,positiveBase, 
						seriesWidth, htmlLink,
						coorWidth, vis);
				
			}

		}

		db.outLabels();
		/* ��һ�»��� */
		if (gp.valueBaseLine != gp.gRect1.x) {
			db.drawLine(gp.valueBaseLine, gp.gRect1.y, gp.valueBaseLine,
					gp.gRect1.y + gp.gRect1.height,
					egp.getAxisColor(GraphProperty.AXIS_BOTTOM));
		}
	}
	
	private static void drawNegativeSeries(int serNumBase, Vector serNames,ExtGraphCategory egc,
			double delx, DrawBase db, double dlb, 
			double seriesWidth, StringBuffer htmlLink, int negativeBase,
			double coorWidth, boolean vis, ArrayList<Desc3DRect> negativeRects) {
		GraphParam gp = db.gp;
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;
		ArrayList<ValueLabel> labelList = db.labelList;
		
		int lb = (int)Math.round(dlb);
		int bs = Consts.LINE_SOLID;
		float bw = 1.0f;
		int serNum = serNames.size();

		for (int j = 0; j < serNum; j++) {
			ExtGraphSery egs = egc.getExtGraphSery(serNames.get(j));
			if (egs.isNull()) {
				continue;
			}
			double val = egs.getValue();
			double tmp = val - gp.baseValue;
			int len = (int) (delx * gp.tickNum * (tmp - gp.minValue) / (gp.maxValue * gp.coorScale));

			if (len == 0) {
				continue;
			}

			int xx, yy, ww, hh;
			ChartColor chartColor=db.getChartColor(db.getColor(j+serNumBase));
			if (len >= 0) {
				continue;
			} else {
				xx = negativeBase + len;
				yy = lb;
				ww = Math.abs(len);
				hh = (int) (seriesWidth);
			}
			Color bc = egp.getAxisColor(GraphProperty.AXIS_COLBORDER);
			int coorShift = (int) coorWidth;
			
			negativeRects.add( Utils.get3DRect(xx, yy, ww, hh, bc, bs, bw,
					egp.isDrawShade(), egp.isRaisedBorder(),
					db.getTransparent(), chartColor,
					true, coorShift ) );//!egp.isBarGraph(db)
			
			db.htmlLink(xx, yy, ww, hh, htmlLink, egc.getNameString(),
					egs);

			ValueLabel vl = null;
			String percentFmt = null;
			if (gp.dispValueType == 3 && vis) { // �����ʾ�ٷֱ�
				if (StringUtils.isValidString(gp.dataMarkFormat)) {
					percentFmt = gp.dataMarkFormat;
				} else {
					percentFmt = "0.00%";
				}
			}

			if (len > 0) {
				// �Ϸ��Ѵ���
			} else {
				String sval = null;
				if (percentFmt != null) {
					sval = db
							.getFormattedValue(
									egs.getValue()
											/ egc.getNegativeSumSeries(),
									percentFmt);
				} else if (gp.dispValueType == IGraphProperty.DISPDATA_TITLE) {
					sval = egs.getTips();
				}
				if (StringUtils.isValidString(sval)) {
					vl = new ValueLabel(sval, new Point(negativeBase
							+ len / 2, (int) (lb - seriesWidth / 2)),
							gp.GFV_VALUE.color,
							GraphFontView.TEXT_ON_CENTER);
				}

				negativeBase += len;
			}
			if (vl != null) {
				labelList.add(vl);
			}
		}

		if (gp.graphTransparent) {
			g.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 1.00F));
		}

		// ��������ʾ��ֵ
		if (gp.dispValueOntop && vis) {
			double val = db.getScaledValue(egc.getPositiveSumSeries(),
					true);
			String sval;
			ValueLabel vl = null;
			if (val > 0) {
			}
			val = db.getScaledValue(egc.getNegativeSumSeries(), true);
			if (val < 0) {
				sval = db.getFormattedValue(val);
				int x = negativeBase - 3;
				int y = (int) lb - (int) (seriesWidth / 2);
				vl = new ValueLabel(sval, new Point(x, y),
						gp.GFV_VALUE.color, GraphFontView.TEXT_ON_LEFT);
			}
			if (vl != null) {
				labelList.add(vl);
			}

		}
	}
	

	private static void drawPositiveSeries(int serNumBase, Vector serNames,ExtGraphCategory egc,
			double delx, DrawBase db, double dlb, int positiveBase,
			double seriesWidth, StringBuffer htmlLink,
			double coorWidth, boolean vis) {
		GraphParam gp = db.gp;
		ExtGraphProperty egp = db.egp;
		Graphics2D g = db.g;
		ArrayList<ValueLabel> labelList = db.labelList;
		
		int lb = (int)Math.round(dlb);
		int bs = Consts.LINE_SOLID;
		float bw = 1.0f;
		int serNum = serNames.size();

		for (int j = 0; j < serNum; j++) {
			ExtGraphSery egs = egc.getExtGraphSery(serNames.get(j));
			if (egs.isNull()) {
				continue;
			}
			double val = egs.getValue();
			double tmp = val - gp.baseValue;
			int len = (int) (delx * gp.tickNum * (tmp - gp.minValue) / (gp.maxValue * gp.coorScale));

			if (len == 0) {
				continue;
			}

			int xx, yy, ww, hh;
			ChartColor chartColor= db.getChartColor(db.getColor(j+serNumBase));
			if (len >= 0) {
				xx = positiveBase;
				yy = lb;
				ww = len;
				hh = (int) (seriesWidth);
			} else {
				continue;
			}
			Color bc = egp.getAxisColor(GraphProperty.AXIS_COLBORDER);
			int coorShift = (int) coorWidth;
			Utils.draw3DRect(g, xx, yy, ww, hh, bc, bs, bw,
					egp.isDrawShade(), egp.isRaisedBorder(),
					db.getTransparent(), chartColor, true,
					coorShift);
//			�˴��ĵ����ڶ�������isVertical�� ���ڲ��÷�ֵ��Ҳ��������ͼ���Ƿ�ֱ��true������Ϊ�����ۻ���һ��ʱ
//			Ϊ�������洦���������ݶ�ɫ���������������������棬���ÿ��� xq 2017��8��2��
//			���ƴ����Ļ���DrawColStatcked3DObj
			
			db.htmlLink(xx, yy, ww, hh, htmlLink, egc.getNameString(), egs);

			ValueLabel vl = null;
			String percentFmt = null;
			if (gp.dispValueType == 3 && vis) { // �����ʾ�ٷֱ�
				if (StringUtils.isValidString(gp.dataMarkFormat)) {
					percentFmt = gp.dataMarkFormat;
				} else {
					percentFmt = "0.00%";
				}
			}

			if (len > 0) {
				String sval = null;
				if (percentFmt != null) {
					sval = db.getFormattedValue(
							egs.getValue() / egc.getPositiveSumSeries(),
							percentFmt);
				} else if (gp.dispValueType == IGraphProperty.DISPDATA_TITLE) {
					sval = egs.getTips();
				}
				if (StringUtils.isValidString(sval)) {
					vl = new ValueLabel(sval, new Point(positiveBase + len
							/ 2, (int) (lb - seriesWidth / 2)),
							gp.GFV_VALUE.color,
							GraphFontView.TEXT_ON_CENTER);
				}

				positiveBase += len;
			} else {
				//
			}
			if (vl != null) {
				labelList.add(vl);
			}
		}
		if (gp.graphTransparent) {
			g.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 1.00F));
		}

		// ��������ʾ��ֵ
		if (gp.dispStackSumValue && vis) {
			double val = db
					.getScaledValue(egc.getPositiveSumSeries(), true);
			String sval;
			ValueLabel vl = null;
			if (val > 0) {
				sval = db.getFormattedValue(val);
				int x = positiveBase + 3;
				int y = (int) lb - (int) (seriesWidth / 2);
				vl = new ValueLabel(sval, new Point(x, y),
						gp.GFV_VALUE.color, GraphFontView.TEXT_ON_RIGHT);

			}
			val = db.getScaledValue(egc.getNegativeSumSeries(), true);
			if (val < 0) {
			}
			if (vl != null) {
				labelList.add(vl);
			}
		}
	}
	
}