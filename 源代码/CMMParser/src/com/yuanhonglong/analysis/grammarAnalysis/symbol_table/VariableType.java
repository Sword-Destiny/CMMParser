package com.yuanhonglong.analysis.grammarAnalysis.symbol_table;

/**
 * 变量类型
 *
 * @author 天命剑主<br>
 *         on 2015/10/1.
 */
public enum VariableType {
	INT, // 整数
	REAL, // 实数
	BOOLEAN, // 布尔值
	FUNCTION, // 函数
	UNKNOWN, // 未知类型
	;
	public boolean equals(ReturnType rt) {
		return (rt != null) && rt.equals(this);
	}

	@Override
	public String toString() {
		switch (this) {
			case INT:
				return "int";
			case REAL:
				return "real";
			case BOOLEAN:
				return "boolean";
			case FUNCTION:
				return "function";
			default:
				return "unknown";
		}
	}
}
