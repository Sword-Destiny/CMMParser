/**
 *
 */
package com.yuanhonglong.source_editor;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JMenuItem;

/**
 * 代码辅助类
 *
 * @author 天命剑主<br>
 *         on 2015年10月11日
 */
public class AssistItem extends JMenuItem {

	private static final long serialVersionUID = -1134562674557685925L;

	enum AssistType {
		KEYWORD, IDENTIFIER, FUNCTION, PRE_PROCESS
	}

	public String		asistWord;	// 代码辅助,补全的词
	public static Font	POP_PLAIN;	// courier new字体,常规
	public static Font	POP_BOLD;	// courier new字体,粗体
	public AssistType	type;		// 代码补全的类型

	static {
		POP_PLAIN = new Font("Courier New", Font.PLAIN, SourceTextPane.SMALL_TEXT_SIZE);
		POP_BOLD = new Font("Courier New", Font.BOLD, SourceTextPane.SMALL_TEXT_SIZE);
	}

	public AssistItem(String asistWord, String label, AssistType type) {
		super(label);
		this.type = type;
		this.asistWord = asistWord;
		setOpaque(true);
	}

	/**
	 * @see javax.swing.AbstractButton#setSelected(boolean)
	 */
	@Override
	public void setSelected(boolean b) {
		heightlight();
		if (b) {
			setBackground(new Color(222, 222, 222));
		}
	}

	/**
	 * 设置代码辅助高亮
	 */
	public void heightlight() {
		if (type == AssistType.KEYWORD) {
			setIcon(MyIcons.ICON_K);
			setBackground(new Color(244, 244, 244));
			setForeground(new Color(170, 71, 123));
			setFont(POP_PLAIN);
		} else if (type == AssistType.FUNCTION) {
			setIcon(MyIcons.ICON_F);
			setBackground(new Color(244, 244, 244));
			setForeground(new Color(0, 0, 0));
			setFont(POP_PLAIN);
		} else if (type == AssistType.PRE_PROCESS) {
			setIcon(MyIcons.ICON_P);
			setBackground(new Color(244, 244, 244));
			setForeground(Color.BLUE);
			setFont(POP_PLAIN);
		} else {
			setIcon(MyIcons.ICON_V);
			setBackground(new Color(244, 244, 244));
			setForeground(new Color(0, 0, 0));
			setFont(POP_PLAIN);
		}
	}

}
