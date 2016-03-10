package com.yuanhonglong.source_editor;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import com.yuanhonglong.cmm.MainFrame;

/**
 * 行号显示,监听源代码区域滚动条的运动.<br>
 * 
 * @author 天命剑主<br>
 *         on 2015/9/19.
 */
public class LineNumberAdjusment implements AdjustmentListener {
	public MainFrame frame;// 主窗口

	public LineNumberAdjusment(MainFrame frame) {
		super();
		this.frame = frame;
	}

	/**
	 * 更新行号面板的高度
	 */
	public void update(int height) {
		frame.lineNumberScroll.getVerticalScrollBar().setValue(height);
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		int height = e.getValue();
		update(height);
	}
}
