package com.yuanhonglong.analysis.grammarAnalysis.symbol_table;

/**
 * 返回类型
 * 
 * @author 天命剑主<br>
 *         on 2015/10/1.
 */
public enum ReturnType {
	INT, // 整数
	REAL, // 实数
	BOOLEAN, // 布尔值
	FUNCTION, // 函数
	VOID, // 无返回类型
	UNKNOWN,// 未知类型
	;
	public boolean equals(VariableType vt) {
		if (vt == null) {
			return false;
		}
		if ((this == ReturnType.INT) && (vt == VariableType.INT)) {
			return true;
		}
		if ((this == ReturnType.BOOLEAN) && (vt == VariableType.BOOLEAN)) {
			return true;
		}
		if ((this == ReturnType.REAL) && (vt == VariableType.REAL)) {
			return true;
		}
		if ((this == ReturnType.UNKNOWN) && (vt == VariableType.UNKNOWN)) {
			return true;
		}
		return false;
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
			case VOID:
				return "void";
			default:
				return "unknown";
		}
	}
}
