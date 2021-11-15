package com.raqsoft.chart.element;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.raqsoft.chart.Consts;
import com.raqsoft.chart.DataElement;
import com.raqsoft.chart.Engine;
import com.raqsoft.chart.IAxis;
import com.raqsoft.chart.ICoor;
import com.raqsoft.chart.ObjectElement;
import com.raqsoft.chart.Para;
import com.raqsoft.chart.Utils;
import com.raqsoft.chart.edit.ParamInfo;
import com.raqsoft.chart.edit.ParamInfoList;
import com.raqsoft.common.StringUtils;
import com.raqsoft.dm.Sequence;
import com.raqsoft.util.Variant;

public class TimeAxis extends ObjectElement implements IAxis{
	// ������
	public String name;
	// �Զ��������Сֵ�ķ�Χ
	public boolean autoCalcValueRange = true;

	// �����켣
	public boolean keepTrack = true;

	// ��ʼʱ�䣬 ʱ�����Ϊ���ڣ���ֵ
	public Object beginTime = 0;
	// ����ʱ��
	public Object endTime = 10;


	// ͼƬ���½���ʾ��ע
	public boolean displayMark = true;
	public String textFont = "Dialog";
	public int textStyle = new Integer(0);
	public int textSize = new Integer(14);
	public Color textColor = Color.RED;
	public Color backColor = null;
	public double markX = 0.1;
	public double markY = 0.9;
	public String format = null;
	
	private transient double t_maxDate=0, t_minDate=Long.MAX_VALUE;
	private transient boolean isDateType = false;//��ǰʱ����������Ƿ��������ͣ���ͬ����ʹ�ò�ͬ��format
	public TimeAxis() {
	}

	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(TimeAxis.class, this);

		paramInfos.add(new ParamInfo("name"));
		String group = "axisTime";
		paramInfos.add(group, new ParamInfo("autoCalcValueRange",
				Consts.INPUT_CHECKBOX));
		paramInfos.add(group, new ParamInfo("keepTrack",Consts.INPUT_CHECKBOX));
		paramInfos.add(group, new ParamInfo("beginTime"));
		paramInfos.add(group, new ParamInfo("endTime"));
		
		group = "marker";
		paramInfos.add(group, new ParamInfo("displayMark", Consts.INPUT_CHECKBOX));
		paramInfos.add(group, new ParamInfo("textFont", Consts.INPUT_FONT));
		paramInfos.add(group,
				new ParamInfo("textStyle", Consts.INPUT_FONTSTYLE));
		paramInfos.add(group, new ParamInfo("textSize", Consts.INPUT_FONTSIZE));
		paramInfos.add(group, new ParamInfo("textColor", Consts.INPUT_COLOR));
		paramInfos.add(group, new ParamInfo("backColor", Consts.INPUT_COLOR));
		paramInfos.add(group, new ParamInfo("format"));
		paramInfos.add(group, new ParamInfo("markX", Consts.INPUT_DOUBLE));
		paramInfos.add(group, new ParamInfo("markY", Consts.INPUT_DOUBLE));

		return paramInfos;
	}

	public void prepare(ArrayList<DataElement> dataElements) {
		if (autoCalcValueRange) {
			for (int i = 0; i < dataElements.size(); i++) {
				DataElement de = dataElements.get(i);
				Sequence data = de.getAxisData(name);
				t_minDate = DateAxis.min(t_minDate, data);
				t_maxDate = DateAxis.max(t_maxDate, data);
			}
		} else {
			double begin = DateAxis.getDoubleDate(beginTime);
			double end = DateAxis.getDoubleDate(endTime);
			t_maxDate = Math.max(begin, end);
			t_minDate = Math.min(begin, end);
		}
//��ʱ����ʱ����Ҫ���Ƚ��������ͼԪ�����ݰ�ʱ������
		for(DataElement de:dataElements){
			Sequence posIndex = de.dataTime.psort(null);
			de.dataTime = de.dataTime.get( posIndex );
			Object tmp = de.dataTime.get(1);
			if (tmp instanceof Date) {
				isDateType = true;
			} else {
				Object obj = Variant.parseDate(tmp.toString());
				if (obj instanceof Date) {
					isDateType = true;
				}
			}

			de.data1 = de.data1.get( posIndex );
			de.data2 = de.data2.get( posIndex );
		}
	}

	public double getMaxDate(){
		return t_maxDate;
	}
	public double getMinDate(){
		return t_minDate;
	}
	
	public boolean isKeepTrack(){
		return keepTrack;
	}
	
	public boolean isVisible() {
		return false;
	}

	public void beforeDraw() {
	}

	public void drawBack() {
	}

	public void draw() {
	}

	public void drawFore() {
	}

	public ArrayList<Shape> getShapes() {
		return null;
	}

	public ArrayList<String> getLinks() {
		return null;
	}

	public Point2D getBasePoint(ICoor coor) {
		return null;
	}

	public String getName() {
		return name;
	}

	public int getLocation() {
		return 0;
	}

	public void setEngine(Engine e) {
		this.e = e;
	}

	public Engine getEngine() {
		return e;
	}

	public Text getMarkElement(double timeLocation){
		Object obj;
		if( isDateType ){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis((long)timeLocation);
			obj = calendar.getTime();
			if(!StringUtils.isValidString(format)){
				format="yyyy/MM/dd";
			}
		}else{
			obj = timeLocation;
			if(!StringUtils.isValidString(format)){
				format="###";
			}
		}
		String mark = Utils.format(obj, format);
		Text txt = new Text();
		txt.setEngine(e);
		txt.text = new Para(mark);
		txt.textFont = new Para(textFont);
		txt.textSize = new Para(textSize);
		txt.textStyle = new Para(textStyle);
		txt.textColor = new Para(textColor);
		txt.backColor = new Para(backColor);
		txt.data1 = new Sequence(new Double[]{markX});
		txt.data2 = new Sequence(new Double[]{markY});
		return txt;
	}
	
	public double animateDoubleValue(Object val){
		return DateAxis.getDoubleDate(val);
	}
	
	protected transient Engine e;
}