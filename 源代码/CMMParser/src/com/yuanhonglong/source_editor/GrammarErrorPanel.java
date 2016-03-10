package com.yuanhonglong.source_editor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.JPanel;

import com.yuanhonglong.analysis.grammarAnalysis.GrammarAnalysis;
import com.yuanhonglong.analysis.lexical_analysis.CMMToken;

/**
 * 语法错误面板
 *
 * @author 天命剑主<br>
 *         on 2015年10月22日
 */
public class GrammarErrorPanel extends JPanel {

	private static final long	serialVersionUID	= 1420750121380996217L;

	public ErrorMenu			popupMenu;									// 语法错误菜单
	public SourceTextPane		textPane;									// 源代码编辑区域

	public GrammarErrorPanel(SourceTextPane textPane) {
		super();
		this.textPane = textPane;
		popupMenu = new ErrorMenu();
		setLayout(null);
	}

	/**
	 * 移除所有的标签
	 */
	public void removeAllLabels() {
		removeAll();
	}

	/**
	 * 刷新所有的标签
	 */
	public void freshAllLabels(GrammarAnalysis grammarAnalysis) {
		removeAllLabels();
		int maxLine = grammarAnalysis.currentLine > SourceTextPane.MAX_LINE ? grammarAnalysis.currentLine : SourceTextPane.MAX_LINE;
		int height = getHeight();
		boolean isWarning = true;
		ArrayList<CMMToken> items = new ArrayList<>();
		int currentLine = 1;
		for (final CMMToken token : grammarAnalysis.lexicalAnalysis.tokens) {
			if (token.hasErrors()) {
				if ((token.sourceLine != currentLine) && (items.size() > 0)) {
					final int x = 0;
					final int y = ((currentLine - 1) * (height - SourceTextPane.LINE_HEIGHT)) / maxLine;
					final GrammarErrorLabel label = new GrammarErrorLabel(currentLine, items, this, x, y);
					add(label);
					label.setSize(SourceTextPane.TEXT_SIZE, SourceTextPane.TEXT_SIZE);
					label.setLocation(x, y);
					label.addMouseMotionListener(new MouseMotionAdapter() {
						@Override
						public void mouseMoved(MouseEvent e) {
							label.run();
							super.mouseMoved(e);
						}
					});
					if (isWarning) {
						label.setIcon(MyIcons.YELLOW_RECTANGLE);
					} else {
						label.setIcon(MyIcons.RED_RECTANGLE);
					}
					isWarning = true;
					items = new ArrayList<>();
				}
				currentLine = token.sourceLine;
				items.add(token);
				if (token.hasLexicalErrors() || ((token.hasGrammarErrors()) && !token.isGrammarWarning())) {
					isWarning = false;
				}
			}
		}
		if (items.size() > 0) {
			final int x = 0;
			final int y = ((currentLine - 1) * (height - SourceTextPane.LINE_HEIGHT)) / maxLine;
			final GrammarErrorLabel label = new GrammarErrorLabel(currentLine, items, this, x, y);
			add(label);
			label.setSize(SourceTextPane.TEXT_SIZE, SourceTextPane.TEXT_SIZE);
			label.setLocation(x, y);
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label.run();
				}
			});
			label.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					label.run();
					super.mouseMoved(e);
				}
			});
			if (isWarning) {
				label.setIcon(MyIcons.YELLOW_RECTANGLE);
			} else {
				label.setIcon(MyIcons.RED_RECTANGLE);
			}
		}
		repaint();
	}

}
