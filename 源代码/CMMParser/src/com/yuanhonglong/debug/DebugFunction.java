package com.yuanhonglong.debug;

import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.Function;

/**
 * 调试时候使用的函数
 *
 * @author 天命剑主 <br>
 *         create by eclipse on 2015年11月11日 <br>
 */
class DebugFunction {
	Function	function;	// 函数
	int			bp;			// 帧指针
	int			tp;			// 栈指针

	public DebugFunction(Function function, int bp, int tp) {
		this.function = function;
		this.bp = bp;
		this.tp = tp;
	}

}