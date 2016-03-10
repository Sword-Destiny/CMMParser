package com.yuanhonglong.source_editor;

import java.util.ArrayList;

import javax.swing.JLabel;

import com.yuanhonglong.analysis.lexical_analysis.CMMToken;

/**
 * 语法错误标签
 *
 * @author 天命剑主<br>
 *         on 2015年10月22日
 */
public class GrammarErrorLabel extends JLabel {

	private static final long	serialVersionUID	= 7809977423342954051L;
	public int					line;										// 源代码行
	ArrayList<CMMToken>			errorItems			= new ArrayList<>();	// 存储错误信息
	private GrammarErrorPanel	panel;										// 语法错误面板
	private int					x;											// 横坐标
	private int					y;											// 纵坐标

	public GrammarErrorLabel(int l, ArrayList<CMMToken> list, GrammarErrorPanel panel, int x, int y) {
		super();
		this.x = x;
		this.y = y;
		this.line = l;
		this.errorItems = list;
		this.panel = panel;
	}

	/**
	 * 弹出语法错误菜单
	 */
	public void run() {
		panel.popupMenu = new ErrorMenu();
		for (CMMToken errorToken : errorItems) {
			panel.popupMenu.addToken(errorToken, panel.textPane);
		}
		panel.popupMenu.show(panel, x, y);
	}
}
