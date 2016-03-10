package com.yuanhonglong.analysis.grammarAnalysis;

/**
 * 语法错误的返回对象
 * 
 * @author 天命剑主 <br>
 *         on 2015年10月24日
 */
public class ErrorReturn {
	public int				i;		// 索引
	public ErrorReturnType	type;	// 类型

	public ErrorReturn(int i, ErrorReturnType type) {
		this.i = i;
		this.type = type;
	}
}
