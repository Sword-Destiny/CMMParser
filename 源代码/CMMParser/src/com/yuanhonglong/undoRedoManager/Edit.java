package com.yuanhonglong.undoRedoManager;

import com.yuanhonglong.undoRedoManager.UndoRedoManager.UndoRedoType;

/**
 * 代表一个编辑操作
 *
 * @author 天命剑主 <br>
 *         create by eclipse<br>
 *         on 2015年11月14日 <br>
 */
public class Edit {
	public UndoRedoType	type;	// 编辑的类型
	public int			offset;	// 编辑操作在文档中的起始偏移量
	public String		str;	// 编辑(插入或者删除)的文字

	public Edit(UndoRedoType type, int offset, String str) {
		this.type = type;
		this.offset = offset;
		this.str = str;
	}

}
