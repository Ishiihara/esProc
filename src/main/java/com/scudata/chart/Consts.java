package com.scudata.chart;

import com.scudata.cellset.IStyle;

/**
 * ��ͼ�ĸ��ֳ���ֵ����
 * @author Joancy
 *
 */
public class Consts {
//	��ֵ�����ֵ�任����
  public final static int TRANSFORM_NONE = 0;//���任
  public final static int TRANSFORM_SCALE = 1;//�������任
  public final static int TRANSFORM_LOG = 2;//�������任
  public final static int TRANSFORM_EXP = 3;//��ָ���任

//  ͼ�����Եı༭����
  public final static int INPUT_NORMAL = 1;//��ͨ���ַ���
  public final static int INPUT_EXP = 2;//��ͨ�ı��ʽ
  public final static int INPUT_COLOR = 3;//��ɫ
  public final static int INPUT_LINESTYLE = 4;//����
  public final static int INPUT_FONT = 5;//����
  public final static int INPUT_TEXTURE = 6;//�������
  public final static int INPUT_POINTSTYLE = 7;//�����״
  public final static int INPUT_FONTSTYLE = 8;//�����񣬰������壬б�壬�»���
  public final static int INPUT_COLUMNSTYLE = 9;//��ͼ����
  public final static int INPUT_CHECKBOX = 10;//��ѡ��
  public final static int INPUT_DROPDOWN = 11;//�����б�
  public final static int INPUT_CHARTCOLOR = 12;//�����ɫ�����Զ��彥��ɫ���Ƿ�����ɫ
  public final static int INPUT_DATE = 13;//���ڱ༭
  public final static int INPUT_ARROW = 14;//��ͷ
  public final static int INPUT_TICKS = 15;//�̶�
  public final static int INPUT_ANGLE = 16;//�Ƕ�
  public final static int INPUT_UNIT = 17;//��λ
  public final static int INPUT_COORDINATES = 18;//����ϵ
  public final static int INPUT_AXISLOCATION = 19;//��λ�ã����ᡢ���ᣬ���ᡢ����
  public final static int INPUT_FONTSIZE = 20;//�ֺ�
  public final static int INPUT_HALIGN = 21;//ˮƽ����
  public final static int INPUT_VALIGN = 22;//��ֱ����
  public final static int INPUT_LEGENDICON = 23;//ͼ��ͼ��
  public final static int INPUT_INTEGER = 24;//����
  public final static int INPUT_DOUBLE = 25;//ʵ��
  public final static int INPUT_DATEUNIT = 26;//����
  public final static int INPUT_TRANSFORM = 27;//�任
  
  //������ͼ�β������ӵ����뷽ʽ��added by sjr
  public final static int INPUT_URLTARGET = 28;//url����
  public final static int INPUT_STACKTYPE = 29;//�ѻ�����
  
  public final static int INPUT_DISPLAYDATA = 40;//��ʾ����
  public final static int INPUT_LEGENDLOCATION = 41;//ͼ����λ
  public final static int INPUT_COLUMNTYPE = 42;//��ͼ����
  public final static int INPUT_LINETYPE = 43;//��������
  public final static int INPUT_PIETYPE = 44;//��ͼ����
  public final static int INPUT_2AXISTYPE = 45;//˫��ͼ����
  public final static int INPUT_IMAGEMODE = 46;//ͼ���ʽ
  public final static int INPUT_SIMPLE_ARROW = 47;//���׼�ͷ
  
  /** �Զ������� */
  public final static int INPUT_CUSTOMDROPDOWN = 50;
  /** ѡ���ļ��� */
  public final static int INPUT_FILE = 51;
  /**ͼԪ����*/
  public final static int INPUT_POINTERTYPE = 52;
  
  
//  �������������
  public final static int INPUT_BARTYPE = 60;
  public final static int INPUT_CHARSET = 61;
  public final static int INPUT_RECERROR = 62;
  

  public final static int COORDINATES_CARTESIAN = 0; //�ѿ�������ϵ����ֱ������ϵ
  public final static int COORDINATES_POLAR = 1; //������ϵ
  public final static int COORDINATES_CARTE_3D = 2; //����չ��ֱ������ϵ
  public final static int COORDINATES_CARTE_VIRTUAL_3D = 3; //����Ч����ƽ��ֱ������ϵ
  public final static int COORDINATES_POLAR_3D = 4; //����չ�ּ�����ϵ
  public final static int COORDINATES_POLAR_VIRTUAL_3D = 5; //����Ч����ƽ�漫����ϵ
  public final static int COORDINATES_LEGEND = 6; //ͼ������ϵ�������Զ���ͼ������
  public final static int COORDINATES_FREE = 9;

  public final static int AXIS_LOC_H = 1;
  public final static int AXIS_LOC_V = 2;
  public final static int AXIS_LOC_POLAR = 3;
  public final static int AXIS_LOC_ANGLE = 4;
  public final static int AXIS_LOC_3D = 5;

  /** ������λ�� -- ��ֵ�� */
  public static final int GRID_VALUE = 0;
  /** ������λ�� -- ������ */
  public static final int GRID_CATEGORY = 1;
  /** ������λ�� -- ȫ��*/
  public static final int GRID_BOTH = 2;

  //ע�⣺�������������ͷ��ӵ���
  //����
  public final static int LINE_NONE = IStyle.LINE_NONE; //��
  public final static int LINE_SOLID = IStyle.LINE_SOLID; //ʵ��
  public final static int LINE_DASHED = IStyle.LINE_DASHED; //����
  public final static int LINE_DOTTED = IStyle.LINE_DOT; //����
  public final static int LINE_DOUBLE = IStyle.LINE_DOUBLE; //˫ʵ��
  public final static int LINE_DOTDASH = IStyle.LINE_DOTDASH; //�㻮��

  //�ߵļ�ͷֻ�ܰ��ٵ����������壬������ȡbyte��������ص�ʮλ����λ
  public final static int LINE_ARROW_NONE = 0x0; //��
  public final static int LINE_ARROW = 0x100; //����ͷ
  public final static int LINE_ARROW_BOTH = 0x200; //˫��ͷ
  public final static int LINE_ARROW_HEART = 0x300; //��״ͷ
  public final static int LINE_ARROW_CIRCEL = 0x400; //Բ״ͷ
  public final static int LINE_ARROW_DIAMOND = 0x500; //��״ͷ
  public final static int LINE_ARROW_L = 0x600; //�󵥼�ͷ

  /**----------------------�̶���λ��----------------------------*/
  public final static int TICK_RIGHTUP = 0; // ���һ���
  public final static int TICK_LEFTDOWN = 1; // �������
  public final static int TICK_CROSS = 2; // ѹ��
  public final static int TICK_NONE = 3; // �޿̶���

  //����
  public final static int PT_NONE = 0; //��
  public final static int PT_CIRCLE = 1; //Բ
  public final static int PT_SQUARE = 2; //������
  public final static int PT_TRIANGLE = 3; //������
  public final static int PT_RECTANGLE = 4; //������
  public final static int PT_STAR = 5; //����
  public final static int PT_DIAMOND = 6; //����
  public final static int PT_CORSS = 7; //����
  public final static int PT_PLUS = 8; //�Ӻ�
  public final static int PT_D_CIRCEL = 9; //˫Բ
  public final static int PT_D_SQUARE = 10; //˫������
  public final static int PT_D_TRIANGLE = 11; //˫������
  public final static int PT_D_RECTANGLE = 12; //˫������
  public final static int PT_D_DIAMOND = 13; //˫����
  public final static int PT_CIRCLE_PLUS = 14; //Բ�ڼӺ�
  public final static int PT_SQUARE_PLUS = 15; //���ڼӺ�
  public final static int PT_TRIANGLE_PLUS = 16; //�����ڼӺ�
  public final static int PT_RECTANGLE_PLUS = 17; //�������ڼӺ�
  public final static int PT_DIAMOND_PLUS = 18; //���ڼӺ�
  public final static int PT_DOT = 19; //ʵ�ĵ�,��ʹ�����ɫ��ֻ��ǰ��ɫ

  //����
  public final static int COL_COBOID = 1; //����
  public final static int COL_CUBE = 2; //���巽��
  public final static int COL_CYLINDER = 3; //Բ��
//  public final static int COL_CONE = 4; //Բ׶


  public final static int PATTERN_DEFAULT = 0; //���ͼ����ȫ���
  public final static int PATTERN_H_THIN_LINE = 1; //���ͼ����ˮƽϸ��
  public final static int PATTERN_H_THICK_LINE = 2; //���ͼ����ˮƽ����
  public final static int PATTERN_V_THIN_LINE = 3; //���ͼ������ֱϸ��
  public final static int PATTERN_V_THICK_LINE = 4; //���ͼ������ֱ����
  public final static int PATTERN_THIN_SLASH = 5; //���ͼ����ϸб��
  public final static int PATTERN_THICK_SLASH = 6; //���ͼ������б��
  public final static int PATTERN_THIN_BACKSLASH = 7; //���ͼ����ϸ��б��
  public final static int PATTERN_THICK_BACKSLASH = 8; //���ͼ�����ַ�б��
  public final static int PATTERN_THIN_GRID = 9; //���ͼ����ϸ����
  public final static int PATTERN_THICK_GRID = 10; //���ͼ����������
  public final static int PATTERN_THIN_BEVEL_GRID = 11; //���ͼ����ϸб����
  public final static int PATTERN_THICK_BEVEL_GRID = 12; //���ͼ������б����
  public final static int PATTERN_DOT_1 = 13; //���ͼ����ϡ���
  public final static int PATTERN_DOT_2 = 14; //���ͼ������ϡ��
  public final static int PATTERN_DOT_3 = 15; //���ͼ�������ܵ�
  public final static int PATTERN_DOT_4 = 16; //���ͼ�������ܵ�
  public final static int PATTERN_SQUARE_FLOOR = 17; //���ͼ����������ذ�ש
  public final static int PATTERN_DIAMOND_FLOOR = 18; //���ͼ�������εذ�ש
  public final static int PATTERN_BRICK_WALL = 19; //���ͼ����שǽ

  //�����񣬸�λΪ1Ϊ��ʾ�ǣ�Ϊ0��ʾ��
  public final static int FONT_BOLD = 1; //����
  public final static int FONT_ITALIC = 2; //б��
  public final static int FONT_UNDERLINE = 4; //�»���
  public final static int FONT_VERTICAL = 8; //����


  public final static int NUNIT_NONE = 0; //�ޣ�1
  public final static int NUNIT_HUNDREDS = 2; //�٣�10^2
  public final static int NUNIT_THOUSANDS = 3; //ǧ��10^3
  public final static int NUNIT_TEN_THOUSANDS = 4; //��10^4
  public final static int NUNIT_HUNDRED_THOUSANDS = 5; //ʮ��10^5
  public final static int NUNIT_MILLIONS = 6; //����10^6
  public final static int NUNIT_TEN_MILLIONS = 7; //ǧ��10^7
  public final static int NUNIT_HUNDRED_MILLIONS = 8; //�ڣ�10^8
  public final static int NUNIT_THOUSAND_MILLIONS = 9; //ʮ�ڣ�10^9
  public final static int NUNIT_BILLIONS = 12; //���ڣ�10^12

  public final static int LEGEND_RECT = 1;
  public final static int LEGEND_POINT = 2;
  public final static int LEGEND_LINE = 3;
  public final static int LEGEND_LINEPOINT = 4;
  public final static int LEGEND_NONE = 5;

  /**----------------------ˮƽ����ȡֵ----------------------------*/
  public final static int HALIGN_LEFT = IStyle.HALIGN_LEFT; // �����
  public final static int HALIGN_CENTER = IStyle.HALIGN_CENTER; // �ж���
  public final static int HALIGN_RIGHT = IStyle.HALIGN_RIGHT; // �Ҷ���

  /**----------------------��ֱ����ȡֵ----------------------------*/
  public final static int VALIGN_TOP = IStyle.VALIGN_TOP; // ����
  public final static int VALIGN_MIDDLE = IStyle.VALIGN_MIDDLE; // ����
  public final static int VALIGN_BOTTOM = IStyle.VALIGN_BOTTOM; // ����

  public final static int LOCATION_LT = HALIGN_LEFT + VALIGN_TOP; //���Ͻ�
  public final static int LOCATION_LM = HALIGN_LEFT + VALIGN_MIDDLE; //������ĵ�
  public final static int LOCATION_LB = HALIGN_LEFT + VALIGN_BOTTOM; //���½�
  public final static int LOCATION_CT = HALIGN_CENTER + VALIGN_TOP; //�ϱ����ĵ�
  public final static int LOCATION_CM = HALIGN_CENTER + VALIGN_MIDDLE; //���ĵ�
  public final static int LOCATION_CB = HALIGN_CENTER + VALIGN_BOTTOM; //�±����ĵ�
  public final static int LOCATION_RT = HALIGN_RIGHT + VALIGN_TOP; //���Ͻ�
  public final static int LOCATION_RM = HALIGN_RIGHT + VALIGN_MIDDLE; //�ұ����ĵ�
  public final static int LOCATION_RB = HALIGN_RIGHT + VALIGN_BOTTOM; //���½�


  //���ڵ�λ
  public final static int DATEUNIT_YEAR = 1; //��
  public final static int DATEUNIT_MONTH = 2; //��
  public final static int DATEUNIT_DAY = 3; //��
  public final static int DATEUNIT_HOUR = 4; //ʱ
  public final static int DATEUNIT_MINUTE = 5; //��
  public final static int DATEUNIT_SECOND = 6; //��
  public final static int DATEUNIT_MILLISECOND = 7; //����

  /** ͼ�θ�ʽ -- JPG  */
  public static final byte IMAGE_JPG = (byte) 1;
  public static final byte IMAGE_GIF = (byte) 2;
  public static final byte IMAGE_PNG = (byte) 3;
  public static final byte IMAGE_FLASH = (byte) 4;
  public static final byte IMAGE_SVG = (byte) 5;
  public static final byte IMAGE_TIFF = (byte) 6;
  
  //�ѻ�����
  public final static int STACK_NONE = 0; //���ѻ�
  public final static int STACK_PERCENT = 1; //�ٷֱȶѻ�
  public final static int STACK_VALUE = 2; //ԭֵ�ѻ�
  
  
//����������
	public static final int TYPE_NONE = 0;
	public static final int TYPE_CODABAR = 1;
	public static final int TYPE_CODE39 = 2;
	public static final int TYPE_CODE128 = 3;
	public static final int TYPE_CODE128A = 4;
	public static final int TYPE_CODE128B = 5;
	public static final int TYPE_CODE128C = 6;
	
	public static final int TYPE_EAN13 = 7;
	public static final int TYPE_EAN8 = 8;
	public static final int TYPE_UPCA = 9;
	public static final int TYPE_ITF = 23;
	public static final int TYPE_PDF417 = 25;
	public static final int TYPE_QRCODE = 256;

//	����ͼԪ���ģʽ
	public static final int MODE_NONE = 0; // ȱʡ
	public static final int MODE_FILL = 1; // ���
	public static final int MODE_TILE = 2; // ƽ��
	
	//˫��ͼ��ʱ������ָ��ϵ�����ĸ�����
	public static byte AXIS_LEFT = 1;
	public static byte AXIS_RIGHT = 2;

	//���Զ�Ӧͼ����������
	public static final byte LEGEND_P_LINECOLOR = 1;//����ɫ
	public static final byte LEGEND_P_MARKERSTYLE = 2;//��״
	public static final byte LEGEND_P_FILLCOLOR = 3;//���ɫ
	
  }
