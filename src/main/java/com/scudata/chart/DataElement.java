package com.scudata.chart;

import java.awt.geom.Point2D;
import java.util.*;

import com.scudata.chart.edit.*;
import com.scudata.chart.element.Column;
import com.scudata.chart.element.DateAxis;
import com.scudata.chart.element.EnumAxis;
import com.scudata.chart.element.Line;
import com.scudata.chart.element.TickAxis;
import com.scudata.chart.element.TimeAxis;
import com.scudata.common.RQException;
import com.scudata.dm.*;

/**
 * ����ͼԪ������
 * @author Joancy
 *
 */
public abstract class DataElement extends LinkElement {
	// ʹ�õ�������1
	public String axis1 = null;

	// ʹ�õ�������2
	public String axis2 = null;

	// ʹ�õ�ʱ����
	public String axisTime = null;

	
	// ��1���߼����꣬��ʽ[v1,v2,...,vn]��v, û��ӳ���߼���ʱ�� data1Ϊ�����x����
	public Sequence data1 = null;

	// ��2���߼����꣬��ʽ[w1,w2,..,wn]��w������y����
	public Sequence data2 = null;

	// ʱ��������꣬��ʽ[t1,t2,..,tn],���ʹ����ʱ���ᣬ ��dataֻ��������
	public Sequence dataTime = null;

	public boolean visible = true;

	/**
	 * ͼԪ�Ƿ�ɼ�
	 * @return �ɼ�����true������false
	 */
	public boolean isVisible() {
		return visible;
	}

	//ͼԪ����������ϣ�������ϵķ����ϵ�У�������ͼԪ�ĺϲ�ֵ��
//	����ͼԪ����Ļ��Ʋ���ʹ�úϲ�ֵ�����ø����Լ��ķ����ϵ��ֵ
	// ����ͬͼԪ�ķ����ϵ�в�һ��ʱ���úϲ�ֵ����ȱ��ϵ�е�ͼԪ������
	public transient Sequence categories = null;
	public transient Sequence series = null;

	protected abstract String getText(int index);

	protected String getTipTitle(int index) {
		String superTitle = super.getTipTitle(index);
		if (superTitle != null) {
			return superTitle;
		}

		if (getText(index) != null) {
			return getText(index);
		}
		Object val1, val2;
		val1 = data1.get(index);
		val2 = data2.get(index);

		return val1.toString() + " " + val2.toString();
	}

	/**
	 * ��ȡͼԪ���ڵ�����ϵ
	 * @return ����ϵ�ӿ�
	 */
	public ICoor getCoor() {
		ArrayList<ICoor> coorList = e.getCoorList();
		int size = coorList.size();
		for (int i = 0; i < size; i++) {
			ICoor coor = coorList.get(i);
			if (coor.getAxis1().getName().equals(axis1)
					&& coor.getAxis2().getName().equals(axis2)) {
				return coor;
			}
		}
		return null;
	}

	/**
	 * ���ڶ������ɱ༭���Ĳ����б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(DataElement.class, this);
		paramInfos.add(new ParamInfo("visible", Consts.INPUT_CHECKBOX));
		String group = "data";
		paramInfos.add(group, new ParamInfo("axis1", Consts.INPUT_NORMAL));
		paramInfos.add(group, new ParamInfo("data1", Consts.INPUT_NORMAL));
		paramInfos.add(group, new ParamInfo("axis2", Consts.INPUT_NORMAL));
		paramInfos.add(group, new ParamInfo("data2", Consts.INPUT_NORMAL));

		paramInfos.add(group, new ParamInfo("axisTime", Consts.INPUT_NORMAL));
		paramInfos.add(group, new ParamInfo("dataTime", Consts.INPUT_NORMAL));

		paramInfos.addAll(super.getParamInfoList());
		return paramInfos;
	}

	/**
	 * ��ȡ1�������
	 * @return ������
	 */
	public String getAxis1Name() {
		return axis1;
	}

	/**
	 * ��ȡ2�������
	 * @return ������
	 */
	public String getAxis2Name() {
		return axis2;
	}
	
	/**
	 * ��ȡʱ��������
	 * @return ʱ����
	 */
	public String getAxisTimeName() {
		return axisTime;
	}

	private Object parseNumber(Object o) {
		if (o instanceof Number) {
			return o;
		} else {
			return new Double(o.toString());
		}
	}

	private void normalizeNumber(Sequence seq, String desc) {
		int size = seq.length();
		for (int i = 1; i <= size; i++) {
			Object o = seq.get(i);
			if (o == null) {
				throw new RuntimeException(desc
						+ " can not contain null values.");
			} else {
				seq.set(i, parseNumber(o));
			}
		}
	}

	/**
	 * �����ݼ����ԣ������ô�����ʾ��ֵ
	 * 
	 * @param numericAxis ��ֵ������
	 */
	public void parseNumericAxisData(String numericAxis) {
		if (isPhysicalCoor()) {
			return;
		}
		Sequence seq = getAxisData(numericAxis);
		normalizeNumber(seq, numericAxis + " Data");
		if (this instanceof Column) {
			Column col = (Column) this;
			Sequence data3 = col.getData3();
			if (data3 != null) {
				normalizeNumber(data3, numericAxis + " Data3");
			}
		}
	}

	/**
	 *��ȡ���������ֶ�Ӧ���߼��������� 
	 * @param axisName ���������� 
	 * @return ��Ӧ���߼���������
	 */
	public Sequence getAxisData(String axisName) {
		if (axisName.equals(axis1)) {
			return data1;
		}
		if (axisName.equals(axis2)) {
			return data2;
		}
		return dataTime;
	}

	/**
	 * ������Ӧ����߼���������
	 * @param axisName ����������
	 * @param data �߼���������
	 */
	public void setAxisData(String axisName, Sequence data) {
		if (data == null)
			return;
		if (axisName.equals(axis1)) {
			data1 = data;
		} else if (axisName.equals(axis2)) {
			data2 = data;
		} else {
			dataTime = data;
		}
	}

	/**
	 * ��ȡָ�������һ����߼���������
	 * @param axisName ������
	 * @return ����ϵ����һ����߼���������
	 */
	public Sequence getOppositeAxisData(String axisName) {
		if (axisName.equals(axis1)) {
			return data2;
		}
		return data1;
	}

	/**
	 * ׼����ͼǰ�����ݼ���׼������
	 */
	public void beforeDraw() {
		if (isPhysicalCoor()) {
			return;
		}
		ICoor coor = getCoor();
		EnumAxis ea = coor.getEnumAxis();
		if (ea != null) {
			Sequence enumData = getAxisData(ea.getName());
			Sequence idED = enumData.id(null);
			if (enumData.length() != idED.length()) {
				throw new RQException("EnumAxis [ " + ea.getName()
						+ " ]'s data exists duplicate item!");
			}
		}
	}

	/**
	 * ��������ͼԪ�Ƿ��õ��˽���ɫ������ͼԪ���ڵ�������Ƿ��н���ɫ������Ĳ�����ɫ�Զ�������ɫ �᱾�����ý�����ɫ
	 * @return
	 */
	public abstract boolean hasGradientColor();

	protected transient Engine e;

/**
 * ���û�ͼ����
 */
	public void setEngine(Engine e) {
		this.e = e;
	}

	/**
	 * ��ȡ��ͼ����
	 * @return ��ͼ����
	 */
	public Engine getEngine() {
		return e;
	}

	/**
	 * ��ǰͼԪ�Ƿ�Ϊ��������ϵ
	 * 
	 * @return ��������ϵʱ����true���߼�����ϵʱ����false
	 */
	public boolean isPhysicalCoor() {
		return axis1 == null && axis2 == null;
	}

	/**
	 * ����ͼԪ��׼��������Ҫ�Ǽ�����ԵĺϷ��ԣ� Para���������滷������
	 */
	public void prepare() {
		// ��������ͼԪ����û��֧��Para�Ķ��󣬹�ֻ����������һ�Σ�����������������Para�������Ե���������
		Utils.setParamsEngine(this);
		String msg = null;

		if (data1 == null) {
			msg = mm.getMessage("data1") + " can not be empty!";
		} else if (data2 == null) {
			msg = mm.getMessage("data2") + " can not be empty!";
		} else if (data1.length() != data2.length()) {
			msg = "DataElement property 'data' is not match: data1 length="
					+ data1.length() + " data2 length=" + data2.length();
		} else if (dataTime != null && data1.length() != dataTime.length()) {
			msg = "DataElement property 'data' is not match: data1 length="
					+ data1.length() + " dataTime length=" + dataTime.length();
		} else if (this instanceof Column) {
			Column col = (Column) this;
			Sequence data3 = col.getData3();
			if (data3 != null) {
				if (data1.length() != data3.length()) {
					msg = "DataElement property 'data' is not match: data1 length="
							+ data1.length()
							+ " data3 length="
							+ data3.length();
				}
			}
		}

		if (dataTime != null && dataTime.length() < 2) {
			msg = "Animate chart requires at least 2 data.";
		}
		if (msg != null) {
			throw new RuntimeException(msg);
		}
		// ��������ϵʱ���������û��Ҫ
		if (isPhysicalCoor()) {
			return;
		}

		TickAxis ta = e.getAxisByName(axis1);
		Sequence enumData = null;
		if (ta instanceof EnumAxis) {
			enumData = data1;
		} else {
			ta = e.getAxisByName(axis2);
			if (ta instanceof EnumAxis) {
				enumData = data2;
			}
		}
		if (enumData != null) {
			categories = EnumAxis.extractCatNames(enumData);
			series = EnumAxis.extractSerNames(enumData);
			// �����ö���ᣬ�����ǽ�ö������Ϊaxis1��
			// �����Ļ�ͼ���붼����axis1�����ƣ��Ķ�̫�鷳���������axis1��axis2�Ե�����
			if (enumData == data2) {
				ICoor ic = getCoor();
				ta = ic.getAxis2();
				ic.setAxis2(ic.getAxis1());
				ic.setAxis1(ta);

				data2 = data1;
				data1 = enumData;

				String tmp = axis2;
				axis2 = axis1;
				axis1 = tmp;
			}
		}
		// ��������Ӧ�����Ƿ�ƥ��
		ICoor ic = getCoor();
		ic.getAxis1().checkDataMatch(data1);
		ic.getAxis2().checkDataMatch(data2);
	}

	protected int pointSize() {
		return data1.length();
	}

	protected Point2D getScreenPoint(int index) {
		return getScreenPoint(index, false);
	}

	protected Point2D getScreenPoint(int index, boolean discardSeries) {
		Point2D p;
		if (isPhysicalCoor()) {
			double vx = ((Number) data1.get(index)).doubleValue();
			double vy = ((Number) data2.get(index)).doubleValue();
			double px = e.getXPixel(vx);
			double py = e.getYPixel(vy);
			p = new Point2D.Double(px, py);
		} else {
			ICoor coor = getCoor();
			Object v1 = data1.get(index);
			Object v2 = data2.get(index);
			if (discardSeries) {
				v1 = Column.discardSeries(v1);
				v2 = Column.discardSeries(v2);
			}

			if (coor.isCartesianCoor()) {
				p = coor.getScreenPoint(v1, v2);
			} else {
				PolarCoor pc = (PolarCoor) coor;
				p = pc.getScreenPoint(v1, v2);
			}
		}
		return p;
	}

	/**
	 * �������Բ�ֵ�㷨
	 * @param frameTime
	 * @return
	 */
	public DataElement getFrame(double frameTime) {
		DataElement de = (DataElement) deepClone();
		de.setEngine(e);
		int size = dataTime.length();
		int index = 1;
		for (int i = 1; i <= size; i++) {
			double tmp = DateAxis.getDoubleDate(dataTime.get(i));
			if (tmp > frameTime) {
				break;
			}
			index = i;
		}
		int index1,index2;
		if(index==size){
			index1=index-1;
			index2=index;
		}else{
			index1=index;
			index2=index+1;
		}
		Point2D.Double p1 = new Point2D.Double();
		Point2D.Double p2 = new Point2D.Double();
		double x,y;
		TickAxis ta1=null,ta2=null;
		if( isPhysicalCoor() ){
//			��������ϵʱ��ֱ��ʹ���߼���������
			x = ((Number)data1.get(index1)).doubleValue();
			y = ((Number)data2.get(index1)).doubleValue();
			p1.setLocation(x, y);
			x = ((Number)data1.get(index2)).doubleValue();
			y = ((Number)data2.get(index2)).doubleValue();
			p2.setLocation(x, y);
		}else{
//			��ʱ��֧�ּ�����ϵ
			ta1 = e.getAxisByName(axis1);
			ta2 = e.getAxisByName(axis2);
			Object val;
			if(ta1.getLocation()==Consts.AXIS_LOC_H){
				val = data1.get(index1);
				x = ta1.animateDoubleValue(val);
				val = data2.get(index1);
				y = ta2.animateDoubleValue(val);
				p1.setLocation(x,y);
				
				val = data1.get(index2);
				x = ta1.animateDoubleValue(val);
				val = data2.get(index2);
				y = ta2.animateDoubleValue(val);
				p2.setLocation(x,y);
			}else{
				val = data1.get(index1);
				y = ta1.animateDoubleValue(val);
				val = data2.get(index1);
				x = ta2.animateDoubleValue(val);
				p1.setLocation(x,y);
				
				val = data1.get(index2);
				y = ta1.animateDoubleValue(val);
				val = data2.get(index2);
				x = ta2.animateDoubleValue(val);
				p2.setLocation(x,y);
			}
		}
		
		TimeAxis ta = e.getTimeAxis(getAxisTimeName());
		double time1 = ta.animateDoubleValue( dataTime.get(index1) );
		double time2 = ta.animateDoubleValue( dataTime.get(index2) );
		double timeLength = time2-time1;
		double ratio = (frameTime-time1)/timeLength;//ʱ�������ڵ�1��ʱ�䳤�ȱ�ֵ
		double xLength = p2.x - p1.x;

		double cx,cy;//��ֵ�߼�����
		cx = p1.x+ ratio*xLength;
		cy = Utils.calcLineY(p1, p2, cx);

		Sequence d1 = new Sequence();
		Sequence d2 = new Sequence();
		if (ta.isKeepTrack()) {
			for (int i = 1; i <= index1; i++) {
				d1.add(data1.get(i));
				d2.add(data2.get(i));
			}
//			���ϲ�ֵ��
			if(ta1!=null && ta1.getLocation()==Consts.AXIS_LOC_V){
				d1.add(cy);
				d2.add(cx);
			}else{
				d1.add(cx);
				d2.add(cy);
			}
		} else {
			// ������ߣ��򱣳�ʱ������˵����ݵ�
			if (this instanceof Line) {
				d1.add(data1.get(index1));
				d2.add(data2.get(index1));
			}
//				���ϲ�ֵ��
			if(ta1!=null && ta1.getLocation()==Consts.AXIS_LOC_V){
				d1.add(cy);
				d2.add(cx);
			}else{
				d1.add(cx);
				d2.add(cy);
			}
		}
		de.data1 = d1;
		de.data2 = d2;
		return de;
	}

	
	/**
	 * ��¡����ͼԪ
	 * @param de
	 */
	public void clone(DataElement de) {
		de.axis1 = axis1;
		de.axis2 = axis2;
		de.axisTime = axisTime;
		de.data1 = data1;
		de.data2 = data2;
		de.dataTime = dataTime;
		de.visible = visible;
	}

	public abstract Object deepClone();
}
