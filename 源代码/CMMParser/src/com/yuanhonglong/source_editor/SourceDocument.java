package com.yuanhonglong.source_editor;

import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.PlainDocument;

import com.yuanhonglong.cmm.MainFrame;
import com.yuanhonglong.debug.DebugBreakPoint;
import com.yuanhonglong.undoRedoManager.Edit;
import com.yuanhonglong.undoRedoManager.UndoRedoManager;
import com.yuanhonglong.undoRedoManager.UndoRedoManager.UndoRedoType;

/**
 * 编辑器文档
 *
 * @author 天命剑主<br>
 *         on 2015/9/19.
 */
public class SourceDocument extends DefaultStyledDocument {

	private static final long	serialVersionUID	= -6092242484534642187L;

	public SourceTextPane		parentPane;									// 父容器,编辑面板
	public boolean				pre_matched			= false;				// 是否进行成对匹配
	public char					pre_match_char		= ' ';					// 待匹配字符
	public int					pre_offset			= -1;					// 待匹配字符位置
	public int					caret_offset		= 0;					// 光标偏移量
	public boolean				ignoreTabAndEnter	= false;				// 是否要忽略tab与换行键入
	public UndoRedoManager		undoRedoManager;							// 负责管理重做和撤销的操作

	public SourceDocument(SourceTextPane pane) {
		super();
		this.parentPane = pane;
		putProperty(PlainDocument.tabSizeAttribute, 4);
		undoRedoManager = new UndoRedoManager(this, 200);
	}

	/**
	 * 撤销操作
	 */
	public void undo() {
		if (undoRedoManager.canUndo()) {
			undoRedoManager.undo();
			parentPane.updatePane();
		}
	}

	/**
	 * 重做操作
	 */
	public void redo() {
		if (undoRedoManager.canRedo()) {
			undoRedoManager.redo();
			parentPane.updatePane();
		}
	}

	/**
	 * 设置光标偏移量,重新初始化配对
	 */
	private void offset_after(int offset) {
		caret_offset = offset;
		pre_matched = false;
		pre_match_char = ' ';
		pre_offset = -1;
	}

	@Override
	/**
	 * 处理字符串键入的时候进行代码格式化
	 *
	 * @see javax.swing.text.AbstractDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		if (ignoreTabAndEnter) {
			str = str.replace("\n", "").replace("\t", "");
			ignoreTabAndEnter = false;
		}
		str = str.replace("\r", "").replace("\t", "    ");
		if (str.length() == 0) {
			return;
		}
		char c = str.charAt(str.length() - 1);
		if (str.equals("(")) {
			pre_matched = true;
			str += ")";
			pre_match_char = c;
			pre_offset = offs;
		} else if (str.equals("'")) {
			if (pre_matched && (pre_match_char == '\'') && (pre_offset == (offs - 1))) {
				str = "";
				offset_after(1);
			} else {
				if ((offs > 0) && this.getText(offs - 1, 1).equals("\\")) {
					pre_matched = false;
					pre_offset = -1;
					pre_match_char = ' ';
				} else {
					pre_matched = true;
					str += "'";
					pre_match_char = c;
					pre_offset = offs;
				}
			}
		} else if (str.equals("[")) {
			pre_matched = true;
			str += "]";
			pre_match_char = c;
			pre_offset = offs;
		} else if (str.equals("{")) {
			pre_matched = true;
			str += "}";
			pre_match_char = c;
			pre_offset = offs;
		} else if (str.equals("\"")) {
			if (pre_matched && (pre_match_char == '\"') && (pre_offset == (offs - 1))) {
				str = "";
				offset_after(1);
			} else {
				if ((offs > 0) && this.getText(offs - 1, 1).equals("\\")) {
					pre_matched = false;
					pre_offset = -1;
					pre_match_char = ' ';
				} else {
					pre_matched = true;
					str += "\"";
					pre_match_char = c;
					pre_offset = offs;
				}
			}
		} else if (str.equals(")") && pre_matched && (pre_match_char == '(') && (pre_offset == (offs - 1))) {
			str = "";
			offset_after(1);
		} else if (str.equals("]") && pre_matched && (pre_match_char == '[') && (pre_offset == (offs - 1))) {
			str = "";
			offset_after(1);
		} else if (str.equals("}") && pre_matched && (pre_match_char == '}') && (pre_offset == (offs - 1))) {
			str = "";
			offset_after(1);
		} else if (str.equals("\n") && pre_matched && (pre_match_char == '{') && (pre_offset == (offs - 1))) {
			String doc = getText(0, offs);// 获取换行符之前的所有字符
			char[] docArr = doc.toCharArray();
			String space = "";
			int n = -1;// 层数
			for (char element : docArr) {
				if (element == '{') {
					n++;
				} else if (element == '}') {
					n--;
				}
			}
			while (n > 0) {
				n--;
				space += "    ";
			}
			str = str + space + "    \n" + space;
			offset_after(-space.length() - 1);
		} else if (str.equals("\n")) {
			String doc = getText(0, offs);
			char[] docArr = doc.toCharArray();
			String space = "";
			int n = 0;
			for (char element : docArr) {
				if (element == '{') {
					n++;
				} else if (element == '}') {
					n--;
				}
			}
			while (n > 0) {
				n--;
				space += "    ";
			}
			str = str + space;
		} else {
			pre_matched = false;
			pre_offset = -1;
			pre_match_char = ' ';
		}
		/// 更改断点的行号
		String beforeText = getText(0, offs);
		int beforeLineNum = SourceTextPane.countChar(beforeText, '\n') + 1;
		int insertLineNum = SourceTextPane.countChar(str, '\n');
		ArrayList<DebugBreakPoint> breakLines = parentPane.frame.runtimeTextPane.breakLines;
		for (DebugBreakPoint breakPoint : breakLines) {
			if (breakPoint.lineNumber >= beforeLineNum) {
				breakPoint.lineNumber += insertLineNum;
			}
		}
		/// 更改断点的行号
		undoRedoManager.addEdit(new Edit(UndoRedoType.INSERT, offs, str));// 记录每次编辑的位置和内容,便于撤销和重做的操作
		super.insertString(offs, str, a);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				parentPane.updatePane();
			}
		});
	}

	@Override
	/**
	 * 处理删除字符串
	 *
	 * @see javax.swing.text.AbstractDocument#remove(int, int)
	 */
	public void remove(int offs, int len) throws BadLocationException {
		if (pre_matched && (offs == pre_offset) && (len == 1)) {
			len += 1;
		}
		pre_matched = false;
		pre_match_char = ' ';
		pre_offset = -1;
		/// 更改断点的行号
		String beforeText = getText(0, offs);
		String removeText = getText(offs, len);
		int beforeLineNum = SourceTextPane.countChar(beforeText, '\n') + 1;
		int removeLineNum = SourceTextPane.countChar(removeText, '\n');
		ArrayList<DebugBreakPoint> breakLines = parentPane.frame.runtimeTextPane.breakLines;
		for (int i = 0; i < breakLines.size(); i++) {
			DebugBreakPoint breakPoint = breakLines.get(i);
			if (breakPoint.lineNumber > beforeLineNum) {
				if ((breakPoint.lineNumber - beforeLineNum) <= removeLineNum) {
					parentPane.frame.runtimeTextPane.breakLines.remove(breakPoint);
					i--;
				} else {
					breakPoint.lineNumber -= removeLineNum;
				}
			}
		}
		/// 更改断点的行号
		undoRedoManager.addEdit(new Edit(UndoRedoType.REMOVE, offs, getText(offs, len)));
		super.remove(offs, len);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				parentPane.updatePane();
			}
		});
	}

	@Override
	protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
		MainFrame.changed = true;
		super.insertUpdate(chng, attr);
		parentPane.popAssistMenu();
	}

	@Override
	protected void removeUpdate(DefaultDocumentEvent chng) {
		MainFrame.changed = true;
		super.removeUpdate(chng);
		parentPane.popAssistMenu();
	}

}
