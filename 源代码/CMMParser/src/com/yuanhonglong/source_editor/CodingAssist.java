package com.yuanhonglong.source_editor;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;

import com.yuanhonglong.analysis.grammarAnalysis.GrammarAnalysis;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.CMMVariable;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.Function;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.Symbol;
import com.yuanhonglong.analysis.lexical_analysis.LexicalAnalysis;
import com.yuanhonglong.cmm.MainFrame;
import com.yuanhonglong.source_editor.AssistItem.AssistType;

/**
 * 代码辅助弹出窗口
 *
 * @author 天命剑主<br>
 *         on 2015年10月11日
 */
public class CodingAssist extends JPopupMenu {

	private static final long		serialVersionUID	= -5344331167861470010L;
	public GrammarAnalysis			analysis;																																		// 语法分析器
	public ArrayList<AssistItem>	items;																																			// 辅助菜单项
	protected int					currentSelected;																																// 当前选择的菜单项
	public String[]					keywords			= { "true", "false", "real", "int", "boolean", "void", "if", "else", "read", "write", "while", "for", "break", "continue",
																"return" };																											// 关键字
	public String[]					pre_processers		= { LexicalAnalysis.PRE_PROCESS_ADDRESS, LexicalAnalysis.PRE_PROCESS_LENGTH };												// 预处理
	public MainFrame				f;																																				// 主窗口

	public CodingAssist(final MainFrame f, GrammarAnalysis grammarAnalysis) {
		this.analysis = grammarAnalysis;
		this.f = f;
		setFocusable(false);
		currentSelected = 0;
		items = new ArrayList<>();
	}

	/**
	 * 显示代码辅助菜单
	 */
	public void showPop() {
		int x = 0, y = 0;
		try {
			Rectangle rectangle = f.sourceCodeEditorPane.modelToView(f.sourceCodeEditorPane.getCaretPosition());
			x = rectangle.x;
			y = rectangle.y + SourceTextPane.TEXT_SIZE;
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		this.removeAll();
		for (final AssistItem popItem : items) {
			this.add(popItem);
			popItem.setSize(150, 14);
			popItem.setSelected(false);
			popItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					f.sourceCodeEditorPane.replaceSelection(popItem.asistWord);
					CodingAssist.this.setVisible(false);
					f.sourceCodeEditorPane.requestFocus();
				}
			});
		}
		currentSelected = 0;
		items.get(0).setSelected(true);
		show(f.sourceCodeEditorPane, x, y);
	}

	/**
	 * 生成辅助字符串
	 *
	 * @param preStr
	 *            前导字符串
	 */
	public void generateAssistsStr(String preStr) {
		items.clear();
		if ((analysis != null) && (analysis.symbolTable != null)) {
			for (Symbol symbol : analysis.symbolTable.symbols) {
				if ((symbol.identifier != null) && (symbol.identifier.length() > 0) && (symbol.identifier.length() >= preStr.length())) {
					if (symbol.identifier.startsWith(preStr)) {
						String assistStr = symbol.identifier.substring(preStr.length());
						String showStr = symbol.identifier;
						if (symbol.variable instanceof Function) {
							showStr += "(";
							assistStr += "(";
							ArrayList<CMMVariable> types = ((Function) symbol.variable).parametersList;
							for (CMMVariable type : types) {
								showStr += type.type + ", ";
								assistStr += ", ";
							}
							if (types.size() > 0) {
								showStr = showStr.substring(0, showStr.length() - 2);
								assistStr = assistStr.substring(0, assistStr.length() - 2);
							}
							showStr += ")";
							assistStr += ")";
							showStr += " : " + ((Function) symbol.variable).returnVariable.type;
							showStr += "    address:" + symbol.address;
							AssistItem popMenuItem = new AssistItem(assistStr, showStr, AssistType.FUNCTION);
							items.add(popMenuItem);
						} else {
							if (symbol.identifier.length() > preStr.length()) {
								showStr += " : " + symbol.variable.type + "    address:" + symbol.address;
								assistStr += " ";
								AssistItem popMenuItem = new AssistItem(assistStr, showStr, AssistType.IDENTIFIER);
								items.add(popMenuItem);
							}
						}
					}
				}
			}
		}
		for (String keyword : keywords) {
			if ((preStr.length() < keyword.length()) && keyword.startsWith(preStr)) {
				AssistItem popMenuItem = new AssistItem(keyword.substring(preStr.length()) + " ", keyword, AssistType.KEYWORD);
				items.add(popMenuItem);
			}
		}
		for (String processor : pre_processers) {
			if ((preStr.length() < processor.length()) && processor.startsWith(preStr)) {
				AssistItem popMenuItem = new AssistItem(processor.substring(preStr.length()) + " ", processor, AssistType.PRE_PROCESS);
				items.add(popMenuItem);
			}
		}
	}

}
