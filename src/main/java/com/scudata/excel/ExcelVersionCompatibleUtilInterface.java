package com.scudata.excel;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;


/**
 * ����poi�汾�������ݹ��߽ӿڣ�ʵ�����ֶ���ӵ�poi-5.0.0.jar��poi-3.17.jar 
 * com/scudata/excel/ExcelVersionCompatibleUtil.class
 * ���ݰ汾��ͬ������ʵ��������
*/
public interface ExcelVersionCompatibleUtilInterface {
	
	/**
	 * @return xlsx������״���½�������㻻�����
	 */
	public int getXSSFShape_EMU_PER_PIXEL() ;

	/**
	 * ��ȡ��Ԫ������
	 * @param c Ŀ�굥Ԫ��
	 * @return ��Ԫ������ö��
	 */
	public CellType getCellType(Cell c);
	
	/**
	 * 
	 * @return xls͸��ɫshortֵ
	 */
	public short getHSSFColor_AUTOMATIC_Index();

	/**
	 * xlsˮƽλ�ó���
	 * @param style
	 * @return
	 */
	public HorizontalAlignment getHSSFAlignmentEnum(HSSFCellStyle style);

	/**
	 * xls��ֱλ�ó���
	 * @param style
	 * @return
	 */
	public VerticalAlignment getHSSFVerticalAlignmentEnum(HSSFCellStyle style);

	/**
	 * xls��߿���
	 * @param style
	 * @return
	 */
	public short getHSSFBorderLeft(HSSFCellStyle style);
	/**
	 * xls�ұ߿���
	 * @param style
	 * @return
	 */
	public short getHSSFBorderRight(HSSFCellStyle style);
	/**
	 * xls�ϱ߿���
	 * @param style
	 * @return
	 */
	public short getHSSFBorderTop(HSSFCellStyle style);
	/**
	 * xls�±߿���
	 * @param style
	 * @return
	 */
	public short getHSSFBorderBottom(HSSFCellStyle style);
	/**
	 * xlsxˮƽλ�ó���
	 * @param style
	 * @return
	 */
	public HorizontalAlignment getXSSFAlignmentEnum(XSSFCellStyle style);
	/**
	 * xlsx��ֱλ�ó���
	 * @param style
	 * @return
	 */
	public VerticalAlignment getXSSFVerticalAlignmentEnum(XSSFCellStyle style);
	/**
	 * xlsx��߿���
	 * @param style
	 * @return
	 */
	public short getXSSFBorderLeft(XSSFCellStyle style);
	/**
	 * xlsx�ұ߿���
	 * @param style
	 * @return
	 */
	public short getXSSFBorderRight(XSSFCellStyle style);
	/**
	 * xlsx�ϱ߿���
	 * @param style
	 * @return
	 */
	public short getXSSFBorderTop(XSSFCellStyle style);
	/**
	 * xlsx�±߿���
	 * @param style
	 * @return
	 */
	public short getXSSFBorderBottom(XSSFCellStyle style);

	/**
	 * ��ȡxlsx��ɫ�����ARGBֵ
	 * @param xc
	 * @param defColor ���xcΪnull���򷵻ش�Color��ARGB
	 * @return
	 */
	public int getColor(XSSFColor xc, Color defColor);
	
	/**
	 * ������תint����
	 * @param b
	 * @return
	 */
	public int byteToInt(byte b);
	
	/**
	 * �ж�cell�Ƿ�Ϊ��ʽ��
	 * @param cell
	 * @return
	 */
	public boolean isCellTypeFomula(Cell cell);
	/**
	 * ����xlsx����ͼ
	 * @param wbp
	 * @param img ����ͼ
	 * @param s Ҫ��ӱ���ͼ��sheetҳ����
	 * @throws IOException
	 */
	public void addWaterRemarkToExcel(Workbook wbp, BufferedImage img, Sheet s) throws IOException;
	
	/** ��ñ߿���ʽ
	 * @param borderStyle ��Ǭ�����ж���ı߿���ʽ
	 * @param borderWidth �߿���
	 * @return Excel�ı߿���ʽ
	 */
	public short getBorderStyle( byte borderStyle, float width );
	

	/**
	 * ��ñ߿���ʽ
	 * 
	 * @param borderStyle
	 *            ��Ǭ�����ж���ı߿���ʽ
	 * @param borderWidth
	 *            �߿���
	 * @return Excel�ı߿���ʽ
	 */
	public short getISheetBorderStyle(byte borderStyle);

	/**
	 * ��ȡ��Ԫ������
	 * @param value Ŀ�굥Ԫ��ֵ
	 * @return ��Ԫ������ö��
	 */
	public CellType getCellType(CellValue value);
	
	/**
	 * ��ȡsheet��ĳһ�����ı�����
	 * @param sst
	 * @param idx
	 * @return
	 */
	public RichTextString getItemAt(SharedStrings sst, int idx);
	/**
	 * ��ȡsheetҳ���ͼƬ����
	 * @param sheet
	 * @param graphMap
	 */
	public void getSheetPictures(XSSFSheet sheet, Map<String, byte[]> graphMap);
	/**
	 * ��ȡָ��font�ı��
	 * @param font
	 * @return ͳһ����int
	 */
	public int getFontIndex(Font font);
	/**
	 * ��ȡָ����Ԫ����ʽ�������font�ı��
	 * @param style
	 * @return ͳһ����int
	 */
	public int getFontIndex(CellStyle style);
	/**
	 * ����Workbook.getNumberOfFonts()
	 * @param wb
	 * @return ͳһ����int
	 */
	public int getNumberOfFonts(Workbook wb);
	/**
	 * ����Workbook.getNumberOfSheets()
	 * @param wb
	 * @return ͳһ����int
	 */
	public int getNumberOfSheets(Workbook wb);

	/**
	 * ��ȡָ����ŵ�����Font����
	 * @param wb
	 * @param index
	 * @return
	 */
	public Font getFontAt(Workbook wb, Number index);
	
	/**
	 * ���ص�Ԫ��ʽ����������
	 * @param cell
	 * @return
	 */
	public CellType getCachedFormulaResultType(Cell cell) ;
	
	public XSSFColor getXSSFColor(int color);

	public SharedStrings readSharedStrings(XSSFReader xssfReader);
}
