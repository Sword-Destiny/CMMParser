package com.yuanhonglong.debug;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.yuanhonglong.cmm.MainFrame;

/**
 * 函数帧面板单元格显示样式
 *
 * @author 天命剑主 <br>
 *         create by eclipse<br>
 *         on 2015年11月14日 <br>
 */
public class FunctionFrameCellRenderer implements TableCellRenderer {
	private final VariableFrame variableFrame;

	FunctionFrameCellRenderer(VariableFrame variableFrame) {
		this.variableFrame = variableFrame;
	}

	@Override
	/**
	 * 表格控件的外观设置
	 *
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object,
	 *      boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable arg0, Object value, boolean arg2, boolean arg3, int row, int column) {
		String text = value.toString();
		JLabel label = new JLabel(text);
		if (text.getBytes().length == text.toCharArray().length) {
			if (this.variableFrame.selected_button_index != VariableFrame.REG_INDEX) {
				if (column == 0) {
					label.setFont(MainFrame.ITAIC_COURIERNEW);
					label.setForeground(Color.BLUE);
				} else {
					label.setFont(MainFrame.PLAIN_COURIERNEW);
					if (column == 2) {
						if (this.variableFrame.functionFrameModel.isCellEditable(row, column)) {
							label.setForeground(MainFrame.NUMBER_COLOR);
						} else {
							label.setForeground(Color.BLACK);
						}
					}
				}
			} else {
				if (column == 1) {
					label.setFont(MainFrame.ITAIC_COURIERNEW);
					label.setForeground(Color.BLUE);
				} else if (column == 2) {
					label.setFont(MainFrame.PLAIN_COURIERNEW);
					label.setForeground(new Color(176, 142, 59));
				} else {
					label.setFont(MainFrame.PLAIN_COURIERNEW);
				}
			}
		} else {
			label.setFont(MainFrame.MICROSOFT_YAHEI);
		}
		return label;
	}
}
