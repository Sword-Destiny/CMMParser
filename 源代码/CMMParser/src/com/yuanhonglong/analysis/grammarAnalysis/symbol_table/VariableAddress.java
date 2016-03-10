package com.yuanhonglong.analysis.grammarAnalysis.symbol_table;

import java.io.Serializable;

/**
 * @author 天命剑主<br>
 *         on 2015年11月6日
 */
public class VariableAddress implements Serializable {

	private static final long	serialVersionUID	= 8800007187373840333L;
	public int					address;									// 地址
	public String				name;										// 标识符名称
	public int					length;										// 数据类型长度

	public VariableAddress(int address, String name, int length) {
		this.address = address;
		this.name = name;
		this.length = length;
	}
}
