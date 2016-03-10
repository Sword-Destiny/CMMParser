package com.yuanhonglong.undoRedoManager;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.yuanhonglong.source_editor.SourceTextPane;
import com.yuanhonglong.util.ListQueue;

/**
 * 撤销和重做管理<br>
 * 用来管理Undo/Redo操作,替换系统自带的UndoManager
 *
 * @author 天命剑主 <br>
 *         create by eclipse<br>
 *         on 2015年11月14日 <br>
 */
public class UndoRedoManager {

	/**
	 * 编辑类型,是插入还是删除,注意 <br>
	 * 一个替换操作 = 一个删除操作 + 一个插入操作
	 *
	 * @author 天命剑主 <br>
	 *         create by eclipse<br>
	 *         on 2015年11月14日 <br>
	 */
	public enum UndoRedoType {
		INSERT, // 插入
		REMOVE, // 删除
		;
	}

	public Document			document;	// 文档
	public ListQueue<Edit>	edits;		// 记录每一次进行的编辑
	public int				history;	// 当前撤销/重做到编辑历史的哪一步
	public boolean			enable;		// 是否启用undoRedoManager

	public UndoRedoManager(Document doc, int size) {
		this.document = doc;
		this.edits = new ListQueue<>(size);
		this.history = -1;
		this.enable = true;
	}

	/**
	 * 设置启用.禁用manager
	 *
	 * @param enable
	 *            true启用/false禁用
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	/**
	 * 设置队列的最大大小/容量
	 */
	@SuppressWarnings("unused")
	public void setLimit(int size) {
		this.edits.setCapacity(size);
	}

	/**
	 * 添加一个编辑操作
	 */
	public void addEdit(Edit edit) {
		if (!enable) {
			return;
		}
		if (edits.isFull()) {
			edits.removeHead();
			edits.addTail(edit);
		} else {
			edits.cut(history + 1);// Undo之后新的编辑将会覆盖旧的编辑
			edits.addTail(edit);
			history++;
		}
	}

	/**
	 * @return 此时是否可以undo
	 */
	public boolean canUndo() {
		return history >= 0;
	}

	/**
	 * @return 此时是否可以redo
	 */
	public boolean canRedo() {
		return (history > -2) && (history < (edits.size() - 1));
	}

	/**
	 * 撤销
	 */
	public void undo() {
		Edit edit = edits.get(history);
		history--;
		setEnable(false);
		if (edit.type == UndoRedoType.INSERT) {
			try {
				document.remove(edit.offset, edit.str.length());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				document.insertString(edit.offset, edit.str, SourceTextPane.plain_en);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		setEnable(true);
	}

	/**
	 * 重做
	 */
	public void redo() {
		history++;
		Edit edit = edits.get(history);
		setEnable(false);
		if (edit.type == UndoRedoType.INSERT) {
			try {
				document.insertString(edit.offset, edit.str, SourceTextPane.plain_en);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				document.remove(edit.offset, edit.str.length());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		setEnable(true);
	}

	/**
	 * 清空
	 */
	public void clear() {
		edits.cut(0);
		history = -1;
	}

}
