package com.yuanhonglong.debug;

/**
 * 调试的断点
 *
 * @author 天命剑主<br>
 *         on 2015年11月6日
 */
public class DebugBreakPoint {

	public boolean	enable;		// 是否已经启用
	public int		lineNumber;	// 行号

	public DebugBreakPoint(int lineNumber) {
		this.enable = true;
		this.lineNumber = lineNumber;
	}
}
