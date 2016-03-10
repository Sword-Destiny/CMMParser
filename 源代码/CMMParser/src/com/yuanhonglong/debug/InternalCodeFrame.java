package com.yuanhonglong.debug;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.yuanhonglong.analysis.grammarAnalysis.InternalCode;
import com.yuanhonglong.analysis.grammarAnalysis.MemoryArea;
import com.yuanhonglong.cmm.MainFrame;
import com.yuanhonglong.source_editor.MyIcons;
import com.yuanhonglong.source_editor.SourceTextPane;

/**
 * 中间代码窗口
 *
 * @author 天命剑主<br>
 *         on 2015/11/7.
 */
public class InternalCodeFrame extends JPanel {

	private static final long		serialVersionUID	= -6041218347117984023L;
	public ArrayList<InternalCode>	internalCodes;								// 中间代码
	public JScrollPane				scrollPane;									// 滚动条
	public JTextPane				textPane;									// 文本区域
	public int						selectedLine;								// 选择的行
	private JLabel					currentLineLabel;							// 当前行的标签

	public JLabel					codeLabel;									// 中间代码标签

	/**
	 * 记录每一行中间代码的起始位置
	 *
	 * @author 天命剑主 <br>
	 *         create by eclipse on 2015年11月11日 <br>
	 */
	class LineStartEnd {
		int	start;
		int	end;

		public LineStartEnd() {
			this.start = 0;
			this.end = 0;
		}

	}

	public LineStartEnd[] lines;// 所有的中间代码行的起始位置

	public InternalCodeFrame() {
		super();
		selectedLine = -1;

		codeLabel = new JLabel("中间代码");
		codeLabel.setSize(MainFrame.CODE_WIDTH, MainFrame.LABEL_HEIGHT);
		codeLabel.setLocation(0, 0);
		codeLabel.setFont(MainFrame.MICROSOFT_YAHEI);
		codeLabel.setForeground(MainFrame.NUMBER_COLOR);
		add(codeLabel);

		setLayout(null);
		textPane = new JTextPane() {
			private static final long serialVersionUID = 7949682446004040757L;

			@Override
			public boolean getScrollableTracksViewportWidth() {
				return (getSize().width < getParent().getSize().width);
			}

			@Override
			public void setSize(Dimension d) {
				if (d.width < getParent().getSize().width) {
					d.width = getParent().getSize().width;
				}
				super.setSize(d);
			}
		};
		textPane.setEditable(false);
		currentLineLabel = new JLabel(MyIcons.RED_CIRCLE);
		currentLineLabel.setSize(14, 14);
		scrollPane = new JScrollPane(textPane);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setLocation(0, MainFrame.LABEL_HEIGHT);
		scrollPane.setSize(MainFrame.CODE_WIDTH, MainFrame.CODE_HEIGHT - MainFrame.LABEL_HEIGHT);
		add(scrollPane);
	}

	/**
	 * 初始化窗口
	 *
	 * @param internalCodes
	 *            中间代码
	 */
	public void initFrame(ArrayList<InternalCode> internalCodes) {
		textPane.removeAll();
		this.internalCodes = internalCodes;
		this.lines = new LineStartEnd[internalCodes.size()];
		Document document = textPane.getDocument();
		for (int i = 0; i < internalCodes.size(); i++) {
			InternalCode internalCode = internalCodes.get(i);
			LineStartEnd line = new LineStartEnd();
			lines[i] = line;
			try {
				int len = document.getLength();
				line.start = len;
				document.insertString(len, String.format("  0x%08x", i), SourceTextPane.pre_process);
				len = document.getLength();
				document.insertString(len, " | " + String.format("%3s:  ", internalCode.lineNumber), SourceTextPane.plain_en);
				switch (internalCode.op) {
					case ret:
					case pushb:
					case popb:
					case popt:
					case halt:
					case inc:
					case dec:
					case logic_not:
					case bit_not:
					case opps:
						len = document.getLength();
						document.insertString(len, internalCode.op.toString(), SourceTextPane.keyword);
						break;
					case write:
						len = document.getLength();
						document.insertString(len, internalCode.op.toString(), SourceTextPane.keyword);
						if (internalCode.memeryArea == MemoryArea.STR_CON) {
							len = document.getLength();
							document.insertString(len, "  " + internalCode.getHexAddressString(), SourceTextPane.strStyle);
						}
						break;
					case space:
					case jmp:
					case pusht:
					case cvt:
						len = document.getLength();
						document.insertString(len, internalCode.op.toString(), SourceTextPane.keyword);
						len = document.getLength();
						document.insertString(len, "  " + internalCode.getHexAddressString(), SourceTextPane.number);
						break;
					default:
						len = document.getLength();
						document.insertString(len, internalCode.op.toString(), SourceTextPane.keyword);
						len = document.getLength();
						document.insertString(len, "  " + internalCode.getHexAddressString(), SourceTextPane.number);
						len = document.getLength();
						document.insertString(len, "(" + internalCode.memeryArea + ")", SourceTextPane.symbol);
						break;
				}
				if (internalCode.extra != null) {
					len = document.getLength();
					document.insertString(len, "," + internalCode.getHexExtraString(), SourceTextPane.single_omment);
				}
				len = document.getLength();
				document.insertString(len, "\n", SourceTextPane.plain_en);
				line.end = document.getLength();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 更新窗口
	 *
	 * @param line
	 *            选择的行
	 */
	public void updateFrame(int line) {
		if (selectedLine != -1) {
			remove(currentLineLabel);
		}
		if ((line >= 0) && (line < lines.length)) {
			selectedLine = line;
			int y = (selectedLine * (SourceTextPane.LINE_HEIGHT)) + 3;
			currentLineLabel.setLocation(0, y);
			textPane.add(currentLineLabel);
			textPane.repaint();
			scrollPane.getVerticalScrollBar().setValue(y);
		}
	}

	/**
	 * 清空
	 */
	public void clear() {
		textPane.setText("");
		textPane.removeAll();
		textPane.repaint();
	}
}
