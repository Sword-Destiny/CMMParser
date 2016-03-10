package com.yuanhonglong.console;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import com.yuanhonglong.source_editor.SourceTextPane;

/**
 * 运行终端
 *
 * @author 天命剑主<br>
 *         on 2015/9/19.
 */
public class RuntimeDocument extends DefaultStyledDocument {

	private static final long	serialVersionUID	= 4153922024672157808L;
	public RuntimeTextPane		textPane;									// 父编辑窗口
	int							currentIndex		= 0;					// 当前输入输出偏移量
	public boolean				userEdit			= true;					// 是否是用户在编辑

	public RuntimeDocument(RuntimeTextPane textPane) {
		super();
		this.textPane = textPane;
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		if (userEdit) {
			if (offs >= currentIndex) {
				if (str.endsWith("\n")) {
					int index = getLength();
					textPane.virtualMachine.buffer = getText(currentIndex, index - currentIndex);// 输入缓冲区
					textPane.virtualMachine.resume();// 唤醒线程
					super.insertString(offs, str, a);
					currentIndex = index + 1;
				} else {
					super.insertString(offs, str, SourceTextPane.number);
				}
			}
		} else {
			super.insertString(offs, str, a);
			currentIndex = getLength();
		}
		textPane.requestFocus();
	}

	@Override
	public void remove(int offs, int len) throws BadLocationException {
		if (userEdit) {
			if (offs >= currentIndex) {
				super.remove(offs, len);
			}
		} else {
			super.remove(offs, len);
			currentIndex = offs;
		}
	}
}
