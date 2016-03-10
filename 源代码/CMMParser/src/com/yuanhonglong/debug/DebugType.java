package com.yuanhonglong.debug;

/**
 * 调试类型
 * 
 * @author 天命剑主 <br>
 *         on 2015年11月7日
 */
public enum DebugType {
	STEP_INTO, // 一次执行一条中间代码
	NEXT_LINE, // 执行到下一行
	STEP_OUT, // 执行到函数返回
	CONTINUE,// 执行到下一个断点
	;
}
