package com.yuanhonglong.source_editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.yuanhonglong.analysis.grammarAnalysis.GrammarError;
import com.yuanhonglong.analysis.lexical_analysis.CMMToken;

/**
 * 语法错误弹出菜单
 *
 * @author 天命剑主<br>
 *         on 2015年10月21日
 */
public class ErrorMenu extends JPopupMenu {
	private static final long	serialVersionUID	= 4973546159470583046L;

	public static final Font	YAHEI_11			= new Font("Microsoft YaHei UI", Font.PLAIN, SourceTextPane.SMALL_TEXT_SIZE);	// 雅黑字体
	public static final Color	LIGHT_LIGHT_GRAY	= new Color(244, 244, 244);														// 极浅的灰色

	public ErrorMenu() {
		addMouseListener(new MouseAdapter() {
			/**
			 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseExited(MouseEvent e) {
				ErrorMenu.this.setVisible(false);
				super.mouseExited(e);
			}
		});
	}

	/**
	 * 添加短语
	 *
	 * @param token
	 *            短语
	 * @param textPane
	 *            源代码编辑窗口
	 */
	public void addToken(final CMMToken token, final SourceTextPane textPane) {
		if (token.hasLexicalErrors()) {
			JMenuItem item = new JMenuItem();
			item.setOpaque(true);
			item.setFont(YAHEI_11);
			item.setText(token.lexicalError);
			item.setBackground(LIGHT_LIGHT_GRAY);
			add(item);
			item.setIcon(MyIcons.RED_CIRCLE);
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					textPane.select(token.offset, token.offset + token.word.length());
				}
			});
		}
		if (token.hasGrammarErrors()) {
			for (GrammarError error : token.grammarErrors) {
				JMenuItem item = new JMenuItem();
				item.setOpaque(true);
				item.setFont(YAHEI_11);
				item.setText(error.getInfoStr());
				item.setBackground(LIGHT_LIGHT_GRAY);
				if (error.isWarning) {
					item.setIcon(MyIcons.YELLOW_CIRCLE);
				} else {
					item.setIcon(MyIcons.RED_CIRCLE);
				}
				add(item);
				item.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						textPane.select(token.offset, token.offset + token.word.length());
					}
				});
			}
		}

	}
}
