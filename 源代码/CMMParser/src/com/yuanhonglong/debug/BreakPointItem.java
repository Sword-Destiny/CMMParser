package com.yuanhonglong.debug;

import javax.swing.JMenuItem;

/**
 * 断点弹出菜单项
 *
 * @author 天命剑主<br>
 *         on 2015/11/7.
 */
public class BreakPointItem extends JMenuItem {
	private static final long	serialVersionUID	= -6355367901023257509L;
	public DebugBreakPoint		point;										// 断点

	public BreakPointItem(final DebugBreakPoint point) {
		this.point = point;
	}
}
