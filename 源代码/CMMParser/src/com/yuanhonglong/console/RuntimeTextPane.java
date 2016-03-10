package com.yuanhonglong.console;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import com.yuanhonglong.analysis.grammarAnalysis.GrammarAnalysis;
import com.yuanhonglong.analysis.grammarAnalysis.GrammarTree;
import com.yuanhonglong.analysis.grammarAnalysis.InternalCode;
import com.yuanhonglong.analysis.grammarAnalysis.MemoryArea;
import com.yuanhonglong.analysis.lexical_analysis.CMMToken;
import com.yuanhonglong.analysis.lexical_analysis.TokenType;
import com.yuanhonglong.cmm.MainFrame;
import com.yuanhonglong.debug.DebugBreakPoint;
import com.yuanhonglong.run.CMMVirtualMachine;
import com.yuanhonglong.source_editor.SourceTextPane;

/**
 * 运行区域面板
 *
 * @author 天命剑主<br>
 *         on 2015/9/19.
 */
public class RuntimeTextPane extends JTextPane {

	private static final long			serialVersionUID	= 1775972770841502333L;
	public MainFrame					frame;										// 主窗口
	public RuntimeDocument				document;									// 运行界面文档文档
	public CMMVirtualMachine			virtualMachine;								// CMM虚拟机
	public ArrayList<DebugBreakPoint>	breakLines;									// 断点所在行数

	public RuntimeTextPane(MainFrame frame, GrammarAnalysis grammarAnalysis) {
		super();
		this.frame = frame;
		breakLines = new ArrayList<>();
		this.document = new RuntimeDocument(this);
		setDocument(document);
		this.virtualMachine = new CMMVirtualMachine(grammarAnalysis, this);
	}

	/**
	 * 输出异常和错误信息
	 */
	public void exceptionOut(Throwable e) {
		errorOut(e.getClass().getName());
		append(" :" + e.getMessage() + "\n");
		StackTraceElement[] elements = e.getStackTrace();
		String str = "";
		for (StackTraceElement element : elements) {
			str += "    at " + element.getClassName() + "." + element.getMethodName() + " ( " + element.getFileName() + " line:" + element.getLineNumber() + " )" + "\n";
		}
		errorOut(str);
	}

	@Override
	public void setText(String newText) {
		document.userEdit = false;
		super.setText(newText);
		document.userEdit = true;
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

	public void run(final boolean debug) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					virtualMachine.runProgram(debug);
				} catch (Throwable e) {
					exceptionOut(e);
				}
			}
		});
	}

	/**
	 * 输出错误颜色信息
	 */
	public void errorOut(String msg) {
		document.userEdit = false;
		int len = document.getLength();
		try {
			document.insertString(len, msg, SourceTextPane.error_en);
			setCaretPosition(getText().replace("\r", "").length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		document.userEdit = true;
	}

	/**
	 * 输出注释颜色信息
	 */
	public void scommentOut(String msg) {
		document.userEdit = false;
		int len = document.getLength();
		try {
			document.insertString(len, msg, SourceTextPane.single_omment);
			setCaretPosition(getText().replace("\r", "").length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		document.userEdit = true;
	}

	/**
	 * 输出注释颜色信息
	 */
	public void mcommentOut(String msg) {
		document.userEdit = false;
		int len = document.getLength();
		try {
			document.insertString(len, msg, SourceTextPane.multi_comment);
			setCaretPosition(getText().replace("\r", "").length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		document.userEdit = true;
	}

	/**
	 * 输出关键字颜色信息
	 */
	public void keywordOut(String msg) {
		document.userEdit = false;
		int len = document.getLength();
		try {
			document.insertString(len, msg, SourceTextPane.keyword);
			setCaretPosition(getText().replace("\r", "").length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		document.userEdit = true;
	}

	/**
	 * 输出中间代码信息
	 */
	public void codesOut(ArrayList<InternalCode> internalCodes) {
		for (int i = 0; i < internalCodes.size(); i++) {
			InternalCode internalCode = internalCodes.get(i);
			preOut(String.format("0x%08x", i));
			append(" | " + String.format("%3s:  ", internalCode.lineNumber));
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
					keywordOut(internalCode.op.toString());
					break;
				case write:
					keywordOut(internalCode.op.toString());
					if (internalCode.memeryArea == MemoryArea.STR_CON) {
						strOut("  " + internalCode.getHexAddressString());
					}
					break;
				case space:
				case jmp:
				case pusht:
				case cvt:
					keywordOut(internalCode.op.toString());
					numberOut("  " + internalCode.getHexAddressString());
					break;
				default:
					keywordOut(internalCode.op.toString());
					numberOut("  " + internalCode.getHexAddressString());
					append("(");
					symbolOut(internalCode.memeryArea.toString());
					append(")");
					break;
			}
			if (internalCode.extra != null) {
				append(",");
				scommentOut(internalCode.getHexExtraString());
			}
			append("\n");
		}
	}

	/**
	 * 输出词法分析结果
	 */
	public void lexicalOut(ArrayList<CMMToken> tokens) {
		for (CMMToken token : tokens) {
			if (token.type == TokenType.SPACE) {
				continue;
			}
			if (token.isErrorToken()) {
				errorOut(token.toString());
			} else {
				append(token.type.getTypeName() + ":");
				String word = String.format("%16s  ", token.word);
				switch (token.type) {
					case DECIMAL_NUMBER:
					case REAL_NUMBER:
						numberOut(word);
						break;
					case ERROR_NUMBER:
					case UNKNOWN_SYMBOL:
					case ERROR_STR:
						errorOut(word);
						break;
					case KEYWORD:
						keywordOut(word);
						break;
					case LINE_COMMENT:
						scommentOut(word);
						break;
					case MULTI_COMMENT:
						mcommentOut(word);
						break;
					case SPECIAL_SYMBAL:
						symbolOut(word);
						break;
					case STR:
						strOut(word);
						break;
					case PRE_PROCESS:
						preOut(word);
						break;
					case IDENTIFIER:
						append(word);
						break;
					default:
						break;
				}
				preOut(String.format(":  %3s", token.sourceLine));
				if (token.isErrorToken()) {
					errorOut("  " + token.lexicalError);
				}
				append("\n");
			}
		}
	}

	/**
	 * 输出字符串颜色信息
	 */
	public void strOut(String msg) {
		document.userEdit = false;
		int len = document.getLength();
		try {
			document.insertString(len, msg, SourceTextPane.strStyle);
			setCaretPosition(getText().replace("\r", "").length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		document.userEdit = true;
	}

	/**
	 * 输出符号颜色信息
	 */
	public void symbolOut(String msg) {
		document.userEdit = false;
		int len = document.getLength();
		try {
			document.insertString(len, msg, SourceTextPane.symbol);
			setCaretPosition(getText().replace("\r", "").length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		document.userEdit = true;
	}

	/**
	 * 输出数字颜色信息
	 */
	public void numberOut(String msg) {
		document.userEdit = false;
		int len = document.getLength();
		try {
			document.insertString(len, msg, SourceTextPane.number);
			setCaretPosition(getText().replace("\r", "").length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		document.userEdit = true;
	}

	/**
	 * 输出预处理颜色信息
	 */
	public void preOut(String msg) {
		document.userEdit = false;
		int len = document.getLength();
		try {
			document.insertString(len, msg, SourceTextPane.pre_process);
			setCaretPosition(getText().replace("\r", "").length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		document.userEdit = true;
	}

	/**
	 * 输出文字信息
	 */
	public void append(String msg) {
		document.userEdit = false;
		int len = document.getLength();
		try {
			document.insertString(len, msg, SourceTextPane.plain_en);
			setCaretPosition(getText().replace("\r", "").length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		document.userEdit = true;
	}

	/**
	 * 输出语法树
	 */
	public void grammarTreeOut(GrammarTree t, String pre) {
		if (t.children.size() == 0) {
			append(t.value + "\n");
			return;
		}
		preOut(t.value + "\n");
		for (int i = 0; i < t.children.size(); i++) {
			GrammarTree ct = t.children.get(i);
			if ((ct.value == null) || (ct.value.length() == 0)) {
				if (ct.children.size() == 0) {
					continue;
				}
			}
			numberOut(pre);
			if (i == (t.children.size() - 1)) {
				numberOut("└─> ");
				grammarTreeOut(ct, pre + "    ");
			} else {
				numberOut("├─> ");
				grammarTreeOut(ct, pre + "│   ");
			}
		}
	}
}
