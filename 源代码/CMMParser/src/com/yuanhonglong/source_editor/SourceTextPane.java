package com.yuanhonglong.source_editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.yuanhonglong.analysis.grammarAnalysis.GrammarAnalysis;
import com.yuanhonglong.analysis.grammarAnalysis.GrammarError;
import com.yuanhonglong.analysis.lexical_analysis.CMMToken;
import com.yuanhonglong.analysis.lexical_analysis.LexicalAnalysis;
import com.yuanhonglong.cmm.MainFrame;

/**
 * CMM编辑面板
 *
 * @author 天命剑主<br>
 *         on 2015/9/18.
 */
public class SourceTextPane extends JTextPane {

	private static final long				serialVersionUID			= 4676491331702873950L;

	public MainFrame						frame;												// 窗口
	public SourceDocument					document;											// 文档
	public static final int					TEXT_SIZE					= 14;					// 文字大小
	public static final int					LINE_HEIGHT;										// 行高
	public static final int					SMALL_TEXT_SIZE				= 12;					// 小文字大小
	public static final int					MAX_LINE;											// 可显示的最大行数

	public static final SimpleAttributeSet	symbol;												// 符号风格
	public static final SimpleAttributeSet	plain_en;											// 默认风格
	public static final SimpleAttributeSet	error_en;											// 错误风格
	public static final SimpleAttributeSet	single_omment;										// 单行风格
	public static final SimpleAttributeSet	pre_process;										// 预处理
	public static final SimpleAttributeSet	multi_comment;										// 多行风格
	public static final SimpleAttributeSet	keyword;											// 关键字风格
	public static final SimpleAttributeSet	number;												// 数字风格
	public static final SimpleAttributeSet	warning;											// 警告风格
	public static final SimpleAttributeSet	strStyle;											// 符号风格

	public static JLabel					debugLabel;											// 调试的当前行(箭头)

	public CodingAssist						assist						= null;					// 代码辅助窗口
	public int								beforePopupCaretPosition	= -1;					// 编辑之前的光标位置
	public GrammarAnalysis					grammarAnalysis;									// 语法分析器
	public LexicalAnalysis					lexicalAnalysis;									// 词法分析器
	public ErrorWindow						popupMenu;											// 弹出错误提示菜单

	static {
		LINE_HEIGHT = TEXT_SIZE + 3;
		MAX_LINE = MainFrame.SOURCE_HEIGHT / LINE_HEIGHT;
		symbol = createStyle(TEXT_SIZE, false, false, false, new Color(40, 174, 209), null, "Courier New");
		plain_en = createStyle(TEXT_SIZE, false, false, false, new Color(0, 0, 0), null, "Courier New");
		error_en = createStyle(TEXT_SIZE, false, false, false, new Color(255, 0, 0), null, "Courier New");
		single_omment = createStyle(TEXT_SIZE, false, false, false, new Color(128, 128, 128), null, "Courier New");
		pre_process = createStyle(TEXT_SIZE, false, true, false, new Color(0, 0, 255), null, "Courier New");
		multi_comment = createStyle(TEXT_SIZE, false, false, false, new Color(14, 143, 20), null, "Courier New");
		keyword = createStyle(TEXT_SIZE, true, false, false, new Color(170, 71, 123), null, "Courier New");
		number = createStyle(TEXT_SIZE, false, false, false, new Color(176, 142, 59), null, "Courier New");
		warning = createStyle(TEXT_SIZE, false, false, false, null, new Color(255, 255, 0), "Courier New");
		strStyle = createStyle(TEXT_SIZE, false, false, false, new Color(15, 128, 12), null, "Courier New");
		debugLabel = new JLabel(MyIcons.RIGHT);
		debugLabel.setSize(19, 12);
		debugLabel.setLocation(-20, 0);
	}

	public SourceTextPane(MainFrame f) {
		super();
		this.frame = f;
		this.document = new SourceDocument(this);
		setDocument(this.document);
		setBorder(null);
		lexicalAnalysis = new LexicalAnalysis();
		grammarAnalysis = new GrammarAnalysis(lexicalAnalysis);
		assist = new CodingAssist(frame, grammarAnalysis);
		// 语法辅助菜单键盘监视
		addKeyListener(new KeyAdapter() {

			/**
			 * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_Z)) {
					document.undo();
					popAssistMenu();
				} else if (e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_R)) {
					document.redo();
					popAssistMenu();
				}
				if (assist.isVisible()) {
					switch (keyCode) {
						case KeyEvent.VK_ENTER:
						case KeyEvent.VK_TAB:
							assist.items.get(assist.currentSelected).doClick();
							SourceTextPane.this.document.ignoreTabAndEnter = true;
							break;
						case KeyEvent.VK_DOWN: {
							assist.requestFocus();
							assist.items.get(assist.currentSelected).setSelected(false);
							assist.currentSelected = (assist.currentSelected + 1) % assist.items.size();
							assist.items.get(assist.currentSelected).setSelected(true);
							break;
						}
						case KeyEvent.VK_UP: {
							assist.requestFocus();
							assist.items.get(assist.currentSelected).setSelected(false);
							assist.currentSelected = ((assist.currentSelected - 1) + assist.items.size()) % assist.items.size();
							assist.items.get(assist.currentSelected).setSelected(true);
							break;
						}
						case KeyEvent.VK_LEFT:
						case KeyEvent.VK_RIGHT:
							popAssistMenu();
							break;
						default:
							break;
					}
					super.keyPressed(e);
					if ((keyCode == KeyEvent.VK_UP) || (keyCode == KeyEvent.VK_DOWN)) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								setCaretPosition(beforePopupCaretPosition);
							}
						});

					}
					return;
				}
				super.keyPressed(e);
			}

		});
		popupMenu = new ErrorWindow(frame);
		// 监视鼠标,判断距离,适时弹出
		addMouseMotionListener(new MouseMotionAdapter() {

			/**
			 * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseMoved(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int pos = viewToModel(new Point(x, y));
				if (pos >= 0) {
					try {
						Rectangle rectangle = modelToView(pos);
						int text_x = rectangle.x, text_y = rectangle.y;
						if ((x >= text_x) && (x <= (text_x + TEXT_SIZE)) && (y >= text_y) && (y <= (text_y + TEXT_SIZE))) {
							boolean find = false;
							for (CMMToken token : frame.lexicalAnalysis.tokens) {
								if (token.hasErrors() && (token.offset <= pos) && ((token.offset + token.word.length()) > pos)) {
									Rectangle showRect = modelToView(token.offset);
									find = true;
									final int pop_x = showRect.x + frame.getX() + MainFrame.SOURCE_START_X;
									final int pop_y = (showRect.y - frame.lineNumberScroll.getVerticalScrollBar().getValue()) + frame.getY() + MainFrame.SOURCE_START_Y + TEXT_SIZE
											+ 45;
									if ((popupMenu.getX() == pop_x) && (popupMenu.getY() == pop_y)) {
										if (!popupMenu.isVisible()) {
											popupMenu.setVisible(true);
										}
										break;
									}
									popupMenu.clear();
									if (token.hasLexicalErrors()) {
										popupMenu.addItem(token.lexicalError);
									}
									if (token.hasGrammarErrors()) {
										for (GrammarError error : token.grammarErrors) {
											popupMenu.addItem(error);
										}
									}
									popupMenu.setLocation(pop_x, pop_y);
									popupMenu.setVisible(true);
								}
							}
							if (!find && popupMenu.isVisible()) {
								popupMenu.setVisible(false);
							}
						}

					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}
				super.mouseMoved(e);
			}

		});

		addMouseListener(new MouseAdapter() {
			/**
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if (popupMenu.isVisible()) {
					popupMenu.setVisible(false);
				}
				super.mouseClicked(e);
			}
		});
	}

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

	/**
	 * 弹出自动补全菜单
	 */
	public void popAssistMenu() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (assist.isVisible()) {
					assist.setVisible(false);
				}
				String preStr = getPreviewStr();
				if (!preStr.equals("")) {
					String str = getText().replace("\r", "").substring(0, getCaretPosition());
					lexicalAnalysis.startAnalysis(str);
					grammarAnalysis.startAnalysis();
					assist.generateAssistsStr(preStr);
					if (assist.items.size() > 0) {
						assist.showPop();
						requestFocus();
						beforePopupCaretPosition = getCaretPosition();
					}
				}
			}
		});
	}

	/**
	 * 计算文本中字符出现的次数
	 *
	 * @param text
	 *            文本
	 * @param ch
	 *            字符
	 * @return 出现次数
	 */
	public static int countChar(String text, char ch) {
		int count = 0;
		char[] arr = text.toCharArray();
		for (char c : arr) {
			if (c == ch) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 显示当前调试的行
	 *
	 * @param line
	 *            当前调试行
	 */
	public void showDebugLine(int line) {
		int y = (line - 1) * LINE_HEIGHT;
		frame.sourceScroll.getVerticalScrollBar().setValue(y);
		debugLabel.setLocation(0, y + 2);
		frame.lineNumberPane.repaint();
	}

	/**
	 * 将一行选中
	 *
	 * @param line
	 *            要选中的行
	 */
	public void selectLine(int line) {
		String text = getText().replace("\r", "");
		char[] doc = text.toCharArray();
		int start = -1, end = -1;
		if (line == 1) {
			start = 0;
		}
		for (int i = 0; i < doc.length; i++) {
			if (doc[i] == '\n') {
				line--;
				if (line == 1) {
					start = i;
				} else if (line == 0) {
					end = i;
				}
			}
		}
		if ((start != -1) && (end != -1)) {
			select(start, end);
		}
	}

	/**
	 * 返回行数
	 *
	 * @return 行数
	 */
	public int getLineCount() {
		return countChar(getText(), '\n') + 1;
	}

	/**
	 * 更新其他面板,比如行号面板和语法错误面板
	 */
	public void updateOtherPane() {
		final JScrollBar bar = frame.sourceScroll.getVerticalScrollBar();
		final int line_num = getLineCount();
		String str = "";
		for (int i = 1; i <= line_num; i++) {
			str = str + "  " + i + "\n";
		}
		frame.lineNumberPane.setText(str);
		frame.grammarErrorPanel.freshAllLabels(frame.grammarAnalysis);
		frame.updateDebugPoint();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.lineNumberAdjusment.update(bar.getValue());
			}
		});
	}

	/**
	 * 获取前导字符串
	 */
	public String getPreviewStr() {
		int pos = this.getCaretPosition();
		String doc = this.getText().replace("\r", "");
		String preStr = "";
		int i = pos - 1;
		for (; i >= 0; i--) {
			char c = doc.charAt(i);
			if (LexicalAnalysis.isLetter(c) || LexicalAnalysis.isNumber(c) || (c == LexicalAnalysis.UNDER_LINE)) {
				preStr = c + preStr;
			} else {
				break;
			}
		}
		if ((i >= 0) && (doc.charAt(i) == LexicalAnalysis.SHARP)) {
			preStr = LexicalAnalysis.SHARP + preStr;
		}
		if ((preStr.length() > 0) && (!LexicalAnalysis.isNumber(preStr.charAt(0)))) {
			return preStr;
		}
		return "";
	}

	/**
	 * 更新界面颜色
	 */
	public void updatePane() {
		String str;
		try {
			str = this.document.getText(0, this.document.getLength());
		} catch (BadLocationException e) {
			str = "";
		}
		frame.lexicalAnalysis.startAnalysis(str);
		frame.grammarAnalysis.startAnalysis();
		for (int i = 0; i < frame.lexicalAnalysis.tokens.size(); i++) {
			CMMToken token = frame.lexicalAnalysis.tokens.get(i);
			switch (token.type) {
				case DECIMAL_NUMBER:
				case REAL_NUMBER:
					document.setCharacterAttributes(token.offset, token.word.length(), number, true);
					break;
				case ERROR_NUMBER:
				case UNKNOWN_SYMBOL:
				case ERROR_STR:
					document.setCharacterAttributes(token.offset, token.word.length(), error_en, false);
					break;
				case KEYWORD:
					document.setCharacterAttributes(token.offset, token.word.length(), keyword, true);
					break;
				case LINE_COMMENT:
					document.setCharacterAttributes(token.offset, token.word.length(), single_omment, true);
					break;
				case MULTI_COMMENT:
					document.setCharacterAttributes(token.offset, token.word.length(), multi_comment, true);
					break;
				case SPACE:
					document.setCharacterAttributes(token.offset, token.word.length(), plain_en, true);
					break;
				case SPECIAL_SYMBAL:
					document.setCharacterAttributes(token.offset, token.word.length(), symbol, true);
					break;
				case STR:
					document.setCharacterAttributes(token.offset, token.word.length(), strStyle, true);
					break;
				case PRE_PROCESS:
					document.setCharacterAttributes(token.offset, token.word.length(), pre_process, true);
					break;
				default:
					document.setCharacterAttributes(token.offset, token.word.length(), plain_en, true);
					break;
			}
			if (token.hasGrammarErrors() && token.isGrammarWarning()) {
				document.setCharacterAttributes(token.offset, token.word.length(), warning, false);
			}
			if (token.hasLexicalErrors() || (token.hasGrammarErrors() && !token.isGrammarWarning())) {
				document.setCharacterAttributes(token.offset, token.word.length(), error_en, false);
			}
		}
		if (document.pre_matched) {
			setCaretPosition(getCaretPosition() - 1);
		}
		if (document.caret_offset != 0) {
			setCaretPosition(getCaretPosition() + document.caret_offset);
			document.caret_offset = 0;
		}
		updateOtherPane();
	}

	/**
	 * 创建各种字体风格
	 *
	 * @param size
	 *            字体大小
	 * @param bold
	 *            字体是否粗体
	 * @param italic
	 *            字体是否斜体
	 * @param underline
	 *            字体是否下划线
	 * @param color
	 *            前景色
	 * @param backColor
	 *            背景色
	 * @param fontName
	 *            字体名称
	 * @return 字体样式
	 */
	public static SimpleAttributeSet createStyle(int size, boolean bold, boolean italic, boolean underline, Color color, Color backColor, String fontName) {
		SimpleAttributeSet s = new SimpleAttributeSet();
		StyleConstants.setFontSize(s, size); // 大小
		StyleConstants.setBold(s, bold); // 粗体
		StyleConstants.setItalic(s, italic); // 斜体
		StyleConstants.setUnderline(s, underline); // 下划线
		if (color != null) {
			StyleConstants.setForeground(s, color); // 颜色
		}
		StyleConstants.setFontFamily(s, fontName); // 字体
		if (backColor != null) {
			StyleConstants.setBackground(s, backColor);// 背景色
		}
		return s;
	}

}
