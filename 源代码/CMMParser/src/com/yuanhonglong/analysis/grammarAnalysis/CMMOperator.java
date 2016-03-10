package com.yuanhonglong.analysis.grammarAnalysis;

/**
 * 操作符
 * 
 * @author 天命剑主<br>
 *         on 2015/9/28.
 */
public enum CMMOperator {
	load("load"), // 将内存中的数据加载到AC(累加器)
	mov("mov"), // 将累加器的值写回内存
	jmp("jmp"), // 无条件跳转
	jmpc("jmpc"), // 有条件跳转,boolean值测试
	space("space"), // 开辟函数栈空间
	ret("ret"), // 函数返回
	pusht("pusht"), // 栈顶指针,入栈操作
	popt("popt"), // 栈顶指针,出栈操作
	pushb("pushb"), // 栈底指针,入栈操作
	popb("popb"), // 栈底指针,出栈操作
	halt("halt"), // 程序结束
	read("read"), // 读操作
	write("write"), // 写操作
	inc("inc"), // 递增
	dec("dec"), // 递减
	mul("mul"), // 乘法
	minus("minus"), // 减法
	plus("plus"), // 加法
	div("div"), // 除法
	mod("mod"), // 求余
	lsh("lsh"), // 左移
	rsh("rsh"), // 右移
	bit_and("bit_and"), // 按位与
	bit_or("bit_or"), // 按位或
	logic_and("logic_and"), // 逻辑与,短路与
	logic_or("logic_or"), // 逻辑或,短路或
	logic_not("logic_not"), // 逻辑非操作
	bit_xor("bit_xor"), // 异或操作
	bit_not("bit_not"), // 按位取反
	gt("gt"), // 大于
	lt("lt"), // 小于
	ge("ge"), // 大于或等于
	le("le"), // 小于或等于
	ne("ne"), // 不等于
	eq("eq"), // 等于
	cvt("cvt"), // 类型转换命令
	opps("opps"),// 取相反数
	;
	public String name;

	CMMOperator(String n) {
		this.name = n;
	}
}
