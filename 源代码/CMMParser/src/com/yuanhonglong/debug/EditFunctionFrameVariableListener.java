package com.yuanhonglong.debug;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.yuanhonglong.analysis.grammarAnalysis.MemoryArea;

/**
 * 编辑函数帧变量时激活此触发器
 *
 * @author 天命剑主 <br>
 *         create by eclipse<br>
 *         on 2015年11月14日 <br>
 */
public class EditFunctionFrameVariableListener implements TableModelListener {
	private final VariableFrame variableFrame;

	EditFunctionFrameVariableListener(VariableFrame variableFrame) {
		this.variableFrame = variableFrame;
	}

	@Override
	/**
	 * 更改表格控件值时会触发此操作
	 *
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent event) {
		int column = event.getColumn();
		int row = event.getFirstRow();
		if (!this.variableFrame.functionFrameModel.isCellEditable(row, column)) {
			return;
		}
		EditableVariable label = (EditableVariable) this.variableFrame.functionFrameModel.getOldValue(row, column);
		String newValue = this.variableFrame.functionFrameModel.getNewValue(row, column).toString().trim();
		if (label.area == MemoryArea.LOCAL) {
			Object variable = this.variableFrame.machine.functionStack[label.address];
			try {
				Object updateValue = this.variableFrame.update(variable, newValue);
				this.variableFrame.machine.functionStack[label.address] = updateValue;
				this.variableFrame.functionFrameModel.updateVariable(row, column, updateValue);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.variableFrame, "变量类型不对,你输入内容的无法转换为boolean,int或者real");
			}
		} else {
			Object variable = this.variableFrame.machine.staticVariables[label.address];
			try {
				Object updateValue = this.variableFrame.update(variable, newValue);
				this.variableFrame.machine.staticVariables[label.address] = updateValue;
				this.variableFrame.functionFrameModel.updateVariable(row, column, updateValue);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.variableFrame, "变量类型不对,你输入内容的无法转换为boolean,int或者real");
			}
		}
	}
}
