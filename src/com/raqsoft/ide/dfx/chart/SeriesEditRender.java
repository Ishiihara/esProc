package com.raqsoft.ide.dfx.chart;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import com.raqsoft.chart.edit.ParamInfo;
import com.raqsoft.ide.common.swing.*;
import com.raqsoft.ide.dfx.resources.*;

/**
 * ��������ֵ�ı�����Ⱦ��
 * 
 * @author Joancy
 *
 */
public class SeriesEditRender extends JLabel implements TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4706869567924036200L;

	/**
	 * ȱʡֵ�Ĺ��캯��
	 */
	public SeriesEditRender() {
		setHorizontalAlignment(JLabel.CENTER);
		this.setToolTipText( ChartMessage.get().getMessage( "tips.toseriesedit" ) );  //"���뵽����¼����༭" );
	}

	/**
	 * ʵ�ָ�����󷽷�
	 */
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
		setBorder( BorderFactory.createEmptyBorder() );
		if ( ( ( JTableEx ) table ).data.getValueAt( row, 5 ) == null ) {
			setIcon( null );
			setBackground( new Color( 240, 240, 240 ) );
		}
		else {
			if ( isSelected ) {
				setForeground( table.getSelectionForeground() );
				setBackground( table.getSelectionBackground() );
			}
			else {
				setForeground( table.getForeground() );
				setBackground( table.getBackground() );
			}
			TableParamEdit paramTable = ( TableParamEdit ) table;
			ParamInfo pi = (ParamInfo)paramTable.data.getValueAt(row, TableParamEdit.iOBJCOL);
			if(pi!=null && pi.isAxisEnable()){
				setIcon( SeriesEditor.enabledIcon );
			}else{
				setIcon( null );
				setBackground( new Color( 240, 240, 240 ) );
			}
		}
		return this;
	}
}

