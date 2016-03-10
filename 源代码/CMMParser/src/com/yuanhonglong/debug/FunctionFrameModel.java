package com.yuanhonglong.debug;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

/**
 * 函数帧表格模式
 *
 * @author 天命剑主 <br>
 *         create by eclipse on 2015年11月11日 <br>
 */
public class FunctionFrameModel extends DefaultTableModel {
	private static final long	serialVersionUID	= 5147477807270723701L;

	public int					columnNum;									// 列数
	public ArrayList<Object[]>	datas;										// 单元格存储的数据(旧数据)

	public FunctionFrameModel() {
		columnNum = 0;
		datas = new ArrayList<>();
	}

	/**
	 * 当前单元格是否可编辑
	 *
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		if ((row < 0) || (row >= datas.size()) || (column < 0) || (column >= columnNum)) {
			return false;
		}
		Object data = datas.get(row)[column];
		return (data instanceof EditableVariable);
	}

	/**
	 * 替换全部数据
	 *
	 * @see javax.swing.table.DefaultTableModel#setDataVector(java.lang.Object[][], java.lang.Object[])
	 */
	@Override
	public void setDataVector(Object[][] dataVector, Object[] columnIdentifiers) {
		datas.clear();
		columnNum = columnIdentifiers.length;
		super.setDataVector(dataVector, columnIdentifiers);
	}

	/**
	 * 添加一行
	 *
	 * @see javax.swing.table.DefaultTableModel#addRow(java.lang.Object[])
	 */
	@Override
	public void addRow(Object[] rowData) {
		datas.add(rowData);
		super.addRow(rowData);
	}

	/**
	 * 返回旧数据
	 *
	 * @param row
	 *            行数
	 * @param column
	 *            列数
	 * @return 旧数据
	 */
	public Object getOldValue(int row, int column) {
		return datas.get(row)[column];
	}

	/**
	 * 返回新数据
	 *
	 * @param row
	 *            行数
	 * @param column
	 *            列数
	 * @return 新数据
	 */
	public Object getNewValue(int row, int column) {
		return getValueAt(row, column);
	}

	/**
	 * 更新数据
	 *
	 * @param row
	 *            行数
	 * @param column
	 *            列数
	 * @param updatValue
	 *            新数据
	 */
	public void updateVariable(int row, int column, Object updatValue) {
		((EditableVariable) datas.get(row)[column]).text = updatValue.toString();
	}

}
