package com.yuanhonglong.debug;

import com.yuanhonglong.analysis.grammarAnalysis.MemoryArea;

/**
 * 调试时可编辑修改的变量
 *
 * @author 天命剑主 <br>
 *         create by eclipse on 2015年11月11日 <br>
 */
class EditableVariable {

	@Override
	public String toString() {
		return text;
	}

	public int			address;	// 地址
	public MemoryArea	area;		// 内存区域
	public String		text;		// 显示的文本

	public EditableVariable(String text, int address, MemoryArea area) {
		this.address = address;
		this.area = area;
		this.text = text;
	}

}
