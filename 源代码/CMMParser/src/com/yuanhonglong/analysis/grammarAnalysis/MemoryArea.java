package com.yuanhonglong.analysis.grammarAnalysis;

/**
 * 变量所在内存区域
 * 
 * @author 天命剑主<br>
 *         on 2015/9/28.
 */
public enum MemoryArea {
	LOCAL, // 本地变量
	ADDRESS, // 代码地址
	STATIC, // 静态变量
	ARGUMENT, // 函数参数变量
	INT_CON, // 整数常数
	REAL_CON, // 实数常数
	BOOL_CON, // 布尔常数
	STR_CON, // 字符串常量
	STATIC_INDIRECT, // 静态变量,间接寻址
	LOCAL_INDIRECT,// 局部变量,间接寻址
	;

	/**
	 * 中间代码操作数是否常数
	 */
	public boolean isConstNumber() {
		return (this == INT_CON) || (this == BOOL_CON) || (this == REAL_CON);
	}
}
