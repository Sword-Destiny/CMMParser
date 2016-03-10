package com.yuanhonglong.cmm;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import com.yuanhonglong.analysis.grammarAnalysis.GrammarAnalysis;
import com.yuanhonglong.analysis.grammarAnalysis.InternalCode;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.Symbol;
import com.yuanhonglong.analysis.javaccAnalysis.CMMParser;
import com.yuanhonglong.analysis.javaccAnalysis.ParseException;
import com.yuanhonglong.analysis.javaccAnalysis.TokenMgrError;
import com.yuanhonglong.analysis.lexical_analysis.CMMToken;
import com.yuanhonglong.analysis.lexical_analysis.LexicalAnalysis;
import com.yuanhonglong.console.RuntimeTextPane;
import com.yuanhonglong.debug.BreakPointItem;
import com.yuanhonglong.debug.DebugBreakPoint;
import com.yuanhonglong.debug.DebugType;
import com.yuanhonglong.debug.InternalCodeFrame;
import com.yuanhonglong.debug.VariableFrame;
import com.yuanhonglong.source_editor.GrammarErrorPanel;
import com.yuanhonglong.source_editor.LineNumberAdjusment;
import com.yuanhonglong.source_editor.MyIcons;
import com.yuanhonglong.source_editor.SourceTextPane;
import com.yuanhonglong.util.CMMFileUtils;

/**
 * 主界面
 *
 * @author 天命剑主<br>
 *         on 2015/9/17.
 */
public class MainFrame extends JFrame {

	private static final long		serialVersionUID;
	public JMenuBar					menuBar;						// 菜单栏

	public JMenu					fileMenu;						// 文件菜单
	public JMenuItem				newItem;						// 新建
	public JMenuItem				openItem;						// 打开
	public JMenuItem				saveItem;						// 保存
	public JMenuItem				exitMenuItem;					// 退出菜单

	public JMenu					runMenu;						// 运行菜单
	public JMenuItem				startRunItem;					// 不调试直接运行
	public JMenuItem				killItem;						// 杀死CMM程序

	public JMenu					analysisMenu;					// 分析
	public JMenuItem				showCMMGrammar;					// 显示CMM语法文件
	public JMenuItem				javaccAnalysis;					// javaCC分析
	public JMenuItem				lexicalAnalysisItem;			// 词法分析
	public JMenuItem				grammarAnalysisItem;			// 语法分析

	public JMenu					exampleMenu;					// 示例

	public JMenu					debugMenu;						// 调试菜单
	public JMenuItem				startDebugItem;					// 调试运行
	public JMenuItem				stopDebugItem;					// 停止调试
	public JMenuItem				stepIntoItem;					// 单步进入
	public JMenuItem				stepNextLineItem;				// 单步跳过
	public JMenuItem				stepOutItem;					// 单步跳出
	public JMenuItem				continueItem;					// 继续
	public JMenuItem				disableAllBreakPoint;			// 禁用所有断点
	public JMenuItem				enableAllBreakPoint;			// 启用所有断点
	public JMenuItem				removeAllBreakPoint;			// 删除所有断点

	public JMenu					internaiCodeMenu;				// 中间代码
	public JMenuItem				loadCodeItem;					// 加载中间代码
	public JMenuItem				saveCodeItem;					// 输出中间代码

	public JMenu					compileMenu;					// 编译菜单
	public JMenuItem				compileItem;					// 仅编译

	public JMenu					outputMenu;						// 输出菜单
	public JMenuItem				clearRuntimeArea;				// 清空输出区域

	public SourceTextPane			sourceCodeEditorPane;			// 源代码编辑面板
	public JScrollPane				sourceScroll;					// 源代码区域滚动条
	public JLabel					sourceLabel;					// 源代码区域标题

	public RuntimeTextPane			runtimeTextPane;				// 运行区域
	public JScrollPane				runtimeScroll;					// 运行区域滚动条
	public JLabel					runtimeLabel;					// 运行区域标题

	public JTextPane				lineNumberPane;					// 代码行号显示面板
	public LineNumberAdjusment		lineNumberAdjusment;			// 代码行号事件监听
	public JScrollPane				lineNumberScroll;				// 代码行数滚动条

	public GrammarErrorPanel		grammarErrorPanel;				// 语法错误面板
	public JScrollPane				grammarErrorScroll;				// 语法错误滚动条

	public File						currentFile			= null;		// 当前文件
	public static boolean			changed				= false;	// 文件是否已经修改

	public LexicalAnalysis			lexicalAnalysis		= null;		// 词法分析器
	public GrammarAnalysis			grammarAnalysis		= null;		// 语法分析器

	public JPanel					debugPanel;						// 调试面板

	public InternalCodeFrame		internalCodeFrame;				// 中间代码区域
	public VariableFrame			variablePanel;					// 变量窗口

	public static final Color		LIGHT_GRAY_COLOR;				// 浅灰色
	public static final Color		NUMBER_COLOR;					// 接近黄色

	public static final Font		MICROSOFT_YAHEI;				// 微软雅黑字体
	public static final Font		PLAIN_COURIERNEW;				// 常规字
	public static final Font		ITAIC_COURIERNEW;				// 斜体字

	public static final int			LINE_SOURCE_GAP		= 2;		// 行号面板和源代码面板之间的纵坐标间距
	public static final int			GRAMMAR_RUNTIME_GAP	= 17;		// 语法错误面板和输出面板之间的横坐标间距
	public static final int			DEBUG_RUNTIME_GAP	= 17;		// 调试面板和输出面板之间的纵坐标间距
	public static final int			CODE_VARIABLE_GAP	= 15;		// 中间代码面板和变量面板之间的横坐标差距
	public static final int			DEBUG_VARIABLE_GAP	= 10;		// 变量面板和调试面板之间的横坐标左边距

	public static final int			LABEL_START_Y;					// 标签左上角y坐标
	public static final int			LABEL_WIDTH			= 200;		// 标签宽
	public static final int			SOURCE_TOP			= 10;		// 源代码上部除去标签之后的纵向高度
	public static final int			LABEL_HEIGHT		= 20;		// 标签高

	public static final int			START_X				= 100;		// 主面板左上角横坐标
	public static final int			START_Y				= 20;		// 主面板左上角纵坐标
	public static final int			WIDTH				= 1200;		// 主面板宽
	public static final int			HEIGHT				= 700;		// 主面板高

	public static final int			LINE_NUMBER_START_X	= 7;		// 行号面板左上角横坐标
	public static final int			LINE_NUMBER_START_Y;			// 行号面板左上角纵坐标
	public static final int			LINE_NUMBER_WIDTH	= 50;		// 行号面板宽
	public static final int			LINE_NUMBER_HEIGHT;				// 行号面板高

	public static final int			SOURCE_START_X;					// 源代码面板左上角横坐标
	public static final int			SOURCE_START_Y;					// 源代码面板左上角纵坐标
	public static final int			SOURCE_WIDTH		= 460;		// 源代码面板宽
	public static final int			SOURCE_HEIGHT		= 600;		// 源代码面板高

	public static final int			GRAMMAR_START_X;				// 语法错误面板面板左上角横坐标
	public static final int			GRAMMAR_START_Y;				// 语法错误面板面板左上角纵坐标
	public static final int			GRAMMAR_WIDTH		= 14;		// 语法错误面板面板宽
	public static final int			GRAMMAR_HEIGHT;					// 语法错误面板面板高

	public static final int			RUNTIME_START_X;				// 输出面板面板左上角横坐标
	public static final int			RUNTIME_START_Y;				// 输出面板面板左上角纵坐标
	public static final int			RUNTIME_WIDTH		= 630;		// 输出面板面板宽
	public static final int			RUNTIME_HEIGHT		= 300;		// 输出面板面板高

	public static final int			DEBUG_START_X;					// 调试面板面板左上角横坐标
	public static final int			DEBUG_START_Y;					// 调试面板面板左上角纵坐标
	public static final int			DEBUG_WIDTH;					// 调试面板面板宽
	public static final int			DEBUG_HEIGHT;					// 调试面板面板高

	public static final int			CODE_START_X;					// 中间代码面板面板左上角横坐标
	public static final int			CODE_START_Y;					// 中间代码面板面板左上角纵坐标
	public static final int			CODE_WIDTH;						// 中间代码面板面板宽
	public static final int			CODE_HEIGHT;					// 中间代码面板面板高

	public static final int			VARIABLE_START_X;				// 变量面板面板左上角横坐标
	public static final int			VARIABLE_START_Y;				// 变量面板面板左上角纵坐标
	public static final int			VARIABLE_WIDTH		= 400;		// 变量面板面板宽
	public static final int			VARIABLE_HEIGHT;				// 变量面板面板高

	public static final String[]	EXAMPLE_FILES;					// 示例文件名列表

	public static long				LINE_PREVIEW_CLICK_TIME;		// 上一次单击行号的时间
	public static int				LINE_DOUBLE_CLICK_TIME_GAP;		// 超过LINE_DOUBLE_CLICK_TIME_GAP毫秒则视为双击

	static {
		serialVersionUID = -6374180970310561399L;
		LABEL_START_Y = SOURCE_TOP / 2;
		SOURCE_START_Y = SOURCE_TOP + LABEL_HEIGHT;
		LINE_NUMBER_HEIGHT = SOURCE_HEIGHT + LINE_SOURCE_GAP;
		LINE_NUMBER_START_Y = SOURCE_START_Y - LINE_SOURCE_GAP;
		SOURCE_START_X = LINE_NUMBER_START_X + LINE_NUMBER_WIDTH;
		GRAMMAR_START_X = SOURCE_WIDTH + SOURCE_START_X;
		GRAMMAR_START_Y = SOURCE_START_Y;
		GRAMMAR_HEIGHT = SOURCE_HEIGHT;
		RUNTIME_START_X = GRAMMAR_START_X + GRAMMAR_WIDTH + GRAMMAR_RUNTIME_GAP;
		RUNTIME_START_Y = SOURCE_START_Y;
		DEBUG_WIDTH = RUNTIME_WIDTH;
		DEBUG_HEIGHT = SOURCE_HEIGHT - RUNTIME_HEIGHT - DEBUG_RUNTIME_GAP;
		DEBUG_START_X = RUNTIME_START_X;
		DEBUG_START_Y = RUNTIME_START_Y + RUNTIME_HEIGHT + DEBUG_RUNTIME_GAP;
		VARIABLE_START_X = DEBUG_VARIABLE_GAP;
		VARIABLE_START_Y = LABEL_HEIGHT;
		VARIABLE_HEIGHT = DEBUG_HEIGHT - DEBUG_VARIABLE_GAP - LABEL_HEIGHT;
		CODE_WIDTH = DEBUG_WIDTH - VARIABLE_WIDTH - CODE_VARIABLE_GAP - CODE_VARIABLE_GAP;
		CODE_HEIGHT = VARIABLE_HEIGHT;
		CODE_START_X = VARIABLE_WIDTH + CODE_VARIABLE_GAP;
		CODE_START_Y = VARIABLE_START_Y;
		LINE_PREVIEW_CLICK_TIME = -LINE_DOUBLE_CLICK_TIME_GAP;
		LINE_DOUBLE_CLICK_TIME_GAP = 350;

		NUMBER_COLOR = new Color(176, 142, 59);
		LIGHT_GRAY_COLOR = new Color(222, 222, 222);
		ITAIC_COURIERNEW = new Font("Courier New", Font.ITALIC, SourceTextPane.TEXT_SIZE);
		PLAIN_COURIERNEW = new Font("Courier New", Font.PLAIN, SourceTextPane.TEXT_SIZE);
		MICROSOFT_YAHEI = new Font("Microsoft YaHei UI", Font.PLAIN, 14);
		EXAMPLE_FILES = new String[] { "hello_world.cmm", "test_input.cmm", "test_for.cmm", "test_while.cmm", "test_if_else.cmm", "test_arr.cmm",
				"test_break_continue.cmm", "test_boolean.cmm", "test_real.cmm", "recursive.cmm", "fib2.cmm", "sort.cmm" };
	}

	public MainFrame() {
		lexicalAnalysis = new LexicalAnalysis();
		grammarAnalysis = new GrammarAnalysis(lexicalAnalysis);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setResizable(false);
		this.setTitle("CMM IDE");
		this.setLayout(null);
		this.setSize(WIDTH, HEIGHT);
		this.setLocation(START_X, START_Y);
		menuBar = new JMenuBar();
		menuBar.setBorder(null);

		initFileMenu();
		initExampleMenu();
		initAnalysisMenu();
		initCompileMenu();
		initCodeMenu();
		initRunMenu();
		initDebugMenu();
		initOutPutMenu();
		this.setJMenuBar(menuBar);

		initLineNumberPane();
		initSourcePane();
		initGrammarErrorPane();
		initRuntimePane();
		initLabels();

		initDebugPanel();
		initInternalPanel();
		initVariablePanel();

		setWindowCloseAction();

	}

	/**
	 * 初始化调试面板
	 */
	private void initDebugPanel() {
		debugPanel = new JPanel();
		debugPanel.setLayout(null);
		debugPanel.setBorder(BorderFactory.createTitledBorder("调试面板(仅调试时可用)"));
		((TitledBorder) debugPanel.getBorder()).setTitleFont(MICROSOFT_YAHEI);
		debugPanel.setSize(DEBUG_WIDTH, DEBUG_HEIGHT);
		debugPanel.setLocation(DEBUG_START_X, DEBUG_START_Y);
		add(debugPanel);
	}

	/**
	 * 初始化变量窗口
	 */
	private void initVariablePanel() {
		variablePanel = new VariableFrame(runtimeTextPane.virtualMachine);
		variablePanel.setLocation(VARIABLE_START_X, VARIABLE_START_Y);
		variablePanel.setSize(VARIABLE_WIDTH, VARIABLE_HEIGHT);
		debugPanel.add(variablePanel);
	}

	/**
	 * 初始化中间代码窗口
	 */
	private void initInternalPanel() {
		internalCodeFrame = new InternalCodeFrame();
		internalCodeFrame.setSize(CODE_WIDTH, CODE_HEIGHT);
		internalCodeFrame.setLocation(CODE_START_X, CODE_START_Y);
		debugPanel.add(internalCodeFrame);
	}

	/**
	 * 初始化中间代码菜单
	 */
	private void initCodeMenu() {
		internaiCodeMenu = new JMenu("中间代码(I)");
		internaiCodeMenu.setMnemonic(KeyEvent.VK_I);
		menuBar.add(internaiCodeMenu);

		loadCodeItem = new JMenuItem("加载中间代码(O)");
		loadCodeItem.setMnemonic(KeyEvent.VK_O);
		loadCodeItem.setToolTipText("加载编译好的中间代码");
		loadCodeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		loadCodeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setCurrentDirectory(new File("."));
				chooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File file) {
						return file.getName().endsWith(".cmmclass") || file.isDirectory();
					}

					@Override
					public String getDescription() {
						return "CMM中间代码文件(*.cmmclass)";
					}

				});

				chooser.showDialog(null, null);
				File f = chooser.getSelectedFile();
				if (f != null) {
					try (FileInputStream fileInputStream = new FileInputStream(f); ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
						disableEdit();
						grammarAnalysis.currentStaticIndex = objectInputStream.readInt();
						grammarAnalysis.hasMainFunction = objectInputStream.readBoolean();
						@SuppressWarnings("unchecked")
						ArrayList<InternalCode> internalCodes = (ArrayList<InternalCode>) objectInputStream.readObject();
						grammarAnalysis.internalCodes.clear();
						grammarAnalysis.internalCodes.addAll(internalCodes);
						runtimeTextPane.codesOut(grammarAnalysis.internalCodes);
					} catch (ClassNotFoundException | IOException e1) {
						runtimeTextPane.errorOut("加载中间代码失败!\n");
						runtimeTextPane.exceptionOut(e1);
					}
				}
			}
		});
		internaiCodeMenu.add(loadCodeItem);

		saveCodeItem = new JMenuItem("保存中间代码(S)");
		saveCodeItem.setMnemonic(KeyEvent.VK_S);
		saveCodeItem.setToolTipText("保存编译好的中间代码");
		saveCodeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		saveCodeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						grammarAnalysisBegin();
						if (lexicalAnalysis.tokens.size() <= 0) {
							runtimeTextPane.errorOut("没有什么可以保存的!\n");
							return;
						}
						for (CMMToken token : lexicalAnalysis.tokens) {
							if (token.hasLexicalErrors() || (token.hasGrammarErrors() && !token.isGrammarWarning())) {
								runtimeTextPane.errorOut("line " + token.sourceLine + ":程序存在错误,请先修正错误!\n");
								return;
							}
						}
						for (InternalCode code : grammarAnalysis.internalCodes) {
							if (code.addrOrValue instanceof Symbol) {
								runtimeTextPane.errorOut("声明的函数(" + code.addrOrValue + ")未找到定义!\n");
								return;
							}
						}
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						chooser.setCurrentDirectory(new File("."));
						chooser.setFileFilter(new FileFilter() {

							@Override
							public boolean accept(File file) {
								return file.getName().endsWith(".cmmclass") || file.isDirectory();
							}

							@Override
							public String getDescription() {
								return "CMM中间代码文件(*.cmmclass)";
							}

						});
						chooser.showSaveDialog(null);
						File f = chooser.getSelectedFile();
						if (f != null) {
							String s = f.getAbsolutePath();
							if (!s.endsWith(".cmmclass")) {
								s = s + ".cmmclass";
								f = new File(s);
							}
							try (FileOutputStream fileOutputStream = new FileOutputStream(f); ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
								objectOutputStream.writeInt(grammarAnalysis.currentStaticIndex);
								objectOutputStream.writeBoolean(grammarAnalysis.hasMainFunction);
								objectOutputStream.writeObject(grammarAnalysis.internalCodes);
								String string = "\r\n" + grammarAnalysis.getCodesStr().replace("\n", "\r\n");
								fileOutputStream.write(string.getBytes());
							} catch (Exception e) {
								runtimeTextPane.errorOut("保存文件失败!\n");
								runtimeTextPane.exceptionOut(e);
							}
						}
					}
				});
			}
		});
		internaiCodeMenu.add(saveCodeItem);

	}

	/**
	 * 初始化输出菜单
	 */
	private void initOutPutMenu() {
		outputMenu = new JMenu("输出(O)");
		outputMenu.setMnemonic(KeyEvent.VK_O);
		menuBar.add(outputMenu);

		clearRuntimeArea = new JMenuItem("清空输出区域(C)");
		clearRuntimeArea.setMnemonic(KeyEvent.VK_C);
		clearRuntimeArea.setToolTipText("清空输出区域/Console");
		clearRuntimeArea.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		clearRuntimeArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runtimeTextPane.setText("");
			}
		});
		outputMenu.add(clearRuntimeArea);
	}

	/**
	 * 初始化编译菜单
	 */
	private void initCompileMenu() {
		compileMenu = new JMenu("编译(C)");
		compileMenu.setMnemonic(KeyEvent.VK_C);
		menuBar.add(compileMenu);

		compileItem = new JMenuItem("编译/生成中间代码(C)");
		compileItem.setMnemonic(KeyEvent.VK_C);
		compileItem.setToolTipText("仅编译生成CMM中间代码");
		compileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		compileItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runtimeTextPane.codesOut(grammarAnalysis.internalCodes);
			}
		});
		compileMenu.add(compileItem);
	}

	/**
	 * 调试的时候改变菜单
	 *
	 * @param debugingOrAfter
	 *            true表示调试结束/false表示正在调试
	 */
	public void changeMenuWhenDebuging(boolean debugingOrAfter) {
		loadCodeItem.setEnabled(debugingOrAfter);
		startDebugItem.setEnabled(debugingOrAfter);
		stopDebugItem.setEnabled(!debugingOrAfter);
		compileMenu.setEnabled(debugingOrAfter);
		runMenu.setEnabled(debugingOrAfter);
		sourceCodeEditorPane.setEditable(debugingOrAfter);
		newItem.setEnabled(debugingOrAfter);
		openItem.setEnabled(debugingOrAfter);
		exampleMenu.setEnabled(debugingOrAfter);
		grammarAnalysisItem.setEnabled(debugingOrAfter);
	}

	/**
	 * 运行的时候改变菜单
	 *
	 * @param runningOrAfter
	 *            true表示运行结束/false表示将要运行
	 */
	public void changeMenuWhenRunning(boolean runningOrAfter) {
		loadCodeItem.setEnabled(runningOrAfter);
		startRunItem.setEnabled(runningOrAfter);
		killItem.setEnabled(!runningOrAfter);
		compileMenu.setEnabled(runningOrAfter);
		debugMenu.setEnabled(runningOrAfter);
		sourceCodeEditorPane.setEditable(runningOrAfter);
		newItem.setEnabled(runningOrAfter);
		openItem.setEnabled(runningOrAfter);
		exampleMenu.setEnabled(runningOrAfter);
		grammarAnalysisItem.setEnabled(runningOrAfter);
	}

	/**
	 * 初始化调试菜单
	 */
	private void initDebugMenu() {
		debugMenu = new JMenu("调试(D)");
		debugMenu.setMnemonic(KeyEvent.VK_D);
		menuBar.add(debugMenu);

		startDebugItem = new JMenuItem("开始调试(D)");
		startDebugItem.setMnemonic(KeyEvent.VK_D);
		startDebugItem.setToolTipText("开始调试CMM程序,不需要先编译");
		startDebugItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		startDebugItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (grammarAnalysis.internalCodes.size() <= 0) {
					runtimeTextPane.errorOut("请输入CMM源代码!\n");
					return;
				}
				if (!grammarAnalysis.hasMainFunction) {
					runtimeTextPane.errorOut("没有找到main函数!\n");
					return;
				}
				for (CMMToken token : lexicalAnalysis.tokens) {
					if (token.hasLexicalErrors() || (token.hasGrammarErrors() && !token.isGrammarWarning())) {
						runtimeTextPane.errorOut("line " + token.sourceLine + ":程序存在错误,请先修正错误!\n");
						return;
					}
				}
				for (InternalCode code : grammarAnalysis.internalCodes) {
					if (code.addrOrValue instanceof Symbol) {
						runtimeTextPane.errorOut("声明的函数(" + code.addrOrValue + ")未找到定义!\n");
						return;
					}
				}
				runtimeTextPane.mcommentOut("start debuging!\n");
				changeMenuWhenDebuging(false);
				runtimeTextPane.run(true);
			}
		});
		startDebugItem.setIcon(MyIcons.DEBUG_ITEM);
		debugMenu.add(startDebugItem);

		stopDebugItem = new JMenuItem("停止调试(K)");
		stopDebugItem.setMnemonic(KeyEvent.VK_K);
		stopDebugItem.setToolTipText("停止调试CMM程序");
		stopDebugItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
		stopDebugItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopProgram();
			}
		});
		stopDebugItem.setEnabled(false);
		stopDebugItem.setIcon(MyIcons.STOP_ITEM);
		debugMenu.add(stopDebugItem);

		debugMenu.addSeparator();

		stepIntoItem = new JMenuItem("单步执行(1)");
		stepIntoItem.setMnemonic(KeyEvent.VK_1);
		stepIntoItem.setToolTipText("一条一条的执行CMM中间代码");
		stepIntoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		stepIntoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((runtimeTextPane.virtualMachine != null) && (runtimeTextPane.virtualMachine.thread != null)) {
					if (runtimeTextPane.virtualMachine.resumeExceptRead()) {
						runtimeTextPane.virtualMachine.debugType = DebugType.STEP_INTO;
					}
				}
			}
		});
		stepIntoItem.setIcon(MyIcons.STEP_1);
		debugMenu.add(stepIntoItem);

		stepNextLineItem = new JMenuItem("运行到下一行(2)");
		stepNextLineItem.setMnemonic(KeyEvent.VK_2);
		stepNextLineItem.setToolTipText("将此行代码执行完");
		stepNextLineItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		stepNextLineItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((runtimeTextPane.virtualMachine != null) && (runtimeTextPane.virtualMachine.thread != null)) {
					if (runtimeTextPane.virtualMachine.resumeExceptRead()) {
						runtimeTextPane.virtualMachine.debugType = DebugType.NEXT_LINE;
					}
				}
			}
		});
		stepNextLineItem.setIcon(MyIcons.STEP_2);
		debugMenu.add(stepNextLineItem);

		stepOutItem = new JMenuItem("运行到函数返回(3)");
		stepOutItem.setMnemonic(KeyEvent.VK_3);
		stepOutItem.setToolTipText("运行到当前函数返回");
		stepOutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		stepOutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((runtimeTextPane.virtualMachine != null) && (runtimeTextPane.virtualMachine.thread != null)) {
					if (runtimeTextPane.virtualMachine.resumeExceptRead()) {
						runtimeTextPane.virtualMachine.setDebugReturnAddress();
						runtimeTextPane.virtualMachine.debugType = DebugType.STEP_OUT;
					}
				}
			}
		});
		stepOutItem.setIcon(MyIcons.STEP_3);
		debugMenu.add(stepOutItem);

		continueItem = new JMenuItem("继续(4)");
		continueItem.setMnemonic(KeyEvent.VK_4);
		continueItem.setToolTipText("继续执行直到遇到下一个断点");
		continueItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		continueItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((runtimeTextPane.virtualMachine != null) && (runtimeTextPane.virtualMachine.thread != null)) {
					if (runtimeTextPane.virtualMachine.resumeExceptRead()) {
						runtimeTextPane.virtualMachine.debugType = DebugType.CONTINUE;
					}
				}
			}
		});
		continueItem.setIcon(MyIcons.STEP_4);
		debugMenu.add(continueItem);

		debugMenu.addSeparator();

		disableAllBreakPoint = new JMenuItem("禁用所有断点(D)");
		disableAllBreakPoint.setMnemonic(KeyEvent.VK_D);
		disableAllBreakPoint.setToolTipText("禁用所有断点");
		disableAllBreakPoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		disableAllBreakPoint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (runtimeTextPane.virtualMachine != null) {
					for (DebugBreakPoint breakPoint : runtimeTextPane.breakLines) {
						breakPoint.enable = false;
					}
					updateDebugPoint();
				}
			}
		});
		disableAllBreakPoint.setIcon(MyIcons.GRAY_CIRCLE);
		debugMenu.add(disableAllBreakPoint);

		enableAllBreakPoint = new JMenuItem("启用所有断点(E)");
		enableAllBreakPoint.setMnemonic(KeyEvent.VK_E);
		enableAllBreakPoint.setToolTipText("启用所有断点");
		enableAllBreakPoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		enableAllBreakPoint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (runtimeTextPane.virtualMachine != null) {
					for (DebugBreakPoint breakPoint : runtimeTextPane.breakLines) {
						breakPoint.enable = true;
					}
					updateDebugPoint();
				}
			}
		});
		enableAllBreakPoint.setIcon(MyIcons.RED_CIRCLE);
		debugMenu.add(enableAllBreakPoint);

		removeAllBreakPoint = new JMenuItem("移除所有断点(R)");
		removeAllBreakPoint.setMnemonic(KeyEvent.VK_R);
		removeAllBreakPoint.setToolTipText("移除所有断点");
		removeAllBreakPoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		removeAllBreakPoint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (runtimeTextPane.virtualMachine != null) {
					runtimeTextPane.breakLines.clear();
					updateDebugPoint();
				}
			}
		});
		removeAllBreakPoint.setIcon(MyIcons.REMOVE_ALL);
		debugMenu.add(removeAllBreakPoint);
	}

	/**
	 * 停止程序运行
	 */
	public void stopProgram() {
		if ((runtimeTextPane.virtualMachine != null) && (runtimeTextPane.virtualMachine.thread != null)) {
			runtimeTextPane.virtualMachine.stop();
			runtimeTextPane.errorOut("\nProgram was killed by user!\n");
		}
	}

	/**
	 * 设置窗口关闭时的操作
	 */
	private void setWindowCloseAction() {
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (changed) {
					int option = JOptionPane.showConfirmDialog(null, "有未保存的更改,是否保存?", "保存更改", JOptionPane.YES_NO_CANCEL_OPTION);
					if (option == JOptionPane.YES_OPTION) {
						if (saveFile()) {
							System.exit(0);
						}
					} else if (option == JOptionPane.NO_OPTION) {
						System.exit(0);
					}
				} else {
					System.exit(0);
				}
				super.windowClosing(e);
			}
		});
	}

	/**
	 * 初始化文本显示区域
	 */
	private void initLabels() {
		sourceLabel = new JLabel("CMM源代码编辑区");
		sourceLabel.setLocation(LINE_NUMBER_START_X, LABEL_START_Y);
		sourceLabel.setSize(LABEL_WIDTH, LABEL_HEIGHT);
		add(sourceLabel);

		runtimeLabel = new JLabel("输出区域");
		runtimeLabel.setLocation(RUNTIME_START_X, LABEL_START_Y);
		runtimeLabel.setSize(LABEL_WIDTH, LABEL_HEIGHT);
		add(runtimeLabel);
	}

	/**
	 * 初始化运行区域面板
	 */
	private void initRuntimePane() {
		runtimeTextPane = new RuntimeTextPane(this, grammarAnalysis);
		runtimeScroll = new JScrollPane(runtimeTextPane);
		runtimeScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		runtimeScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		runtimeScroll.setLocation(RUNTIME_START_X, RUNTIME_START_Y);
		runtimeScroll.setSize(RUNTIME_WIDTH, RUNTIME_HEIGHT);
		this.add(runtimeScroll);
	}

	/**
	 * 初始化源代码面板
	 */
	private void initSourcePane() {
		sourceCodeEditorPane = new SourceTextPane(this);
		sourceCodeEditorPane.setCaretColor(Color.BLACK);
		sourceCodeEditorPane.setSelectedTextColor(Color.WHITE);
		sourceScroll = new JScrollPane(sourceCodeEditorPane);
		sourceScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sourceScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		sourceScroll.setLocation(SOURCE_START_X, SOURCE_START_Y);
		sourceScroll.setSize(SOURCE_WIDTH, SOURCE_HEIGHT);
		this.add(sourceScroll);

		lineNumberAdjusment = new LineNumberAdjusment(this);
		sourceScroll.getVerticalScrollBar().addAdjustmentListener(lineNumberAdjusment);
	}

	/**
	 * 初始化语法错误面板
	 */
	private void initGrammarErrorPane() {
		grammarErrorPanel = new GrammarErrorPanel(sourceCodeEditorPane);
		grammarErrorPanel.setBackground(LIGHT_GRAY_COLOR);
		grammarErrorScroll = new JScrollPane(grammarErrorPanel);
		grammarErrorScroll.setLocation(GRAMMAR_START_X, GRAMMAR_START_Y);
		grammarErrorScroll.setSize(GRAMMAR_WIDTH, GRAMMAR_HEIGHT);
		this.add(grammarErrorScroll);
		final JScrollBar vBar = grammarErrorScroll.getVerticalScrollBar();
		vBar.setUI(null);
		grammarErrorScroll.setHorizontalScrollBar(null);
		grammarErrorScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		MouseWheelListener[] listeners = grammarErrorScroll.getMouseWheelListeners();
		for (MouseWheelListener l : listeners) {
			grammarErrorScroll.removeMouseWheelListener(l);
		}
		grammarErrorScroll.setBorder(null);
	}

	/**
	 * 初始化行号面板
	 */
	private void initLineNumberPane() {
		lineNumberPane = new JTextPane();
		lineNumberPane.setEditable(false);
		lineNumberPane.setForeground(Color.DARK_GRAY);
		lineNumberPane.setBackground(LIGHT_GRAY_COLOR);
		lineNumberPane.setFont(PLAIN_COURIERNEW);
		lineNumberPane.add(SourceTextPane.debugLabel);
		lineNumberScroll = new JScrollPane(lineNumberPane);
		lineNumberScroll.setLocation(LINE_NUMBER_START_X, LINE_NUMBER_START_Y);
		lineNumberScroll.setSize(LINE_NUMBER_WIDTH, LINE_NUMBER_HEIGHT);
		this.add(lineNumberScroll);
		lineNumberPane.setText("  1\n");
		lineNumberScroll.setBorder(null);
		final JScrollBar vBar = lineNumberScroll.getVerticalScrollBar();
		vBar.setUI(null);
		lineNumberScroll.setHorizontalScrollBar(null);
		MouseWheelListener[] listeners = lineNumberScroll.getMouseWheelListeners();
		for (MouseWheelListener l : listeners) {
			lineNumberScroll.removeMouseWheelListener(l);
		}
		lineNumberPane.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX();
				int y = e.getY() - LINE_SOURCE_GAP;
				int line = (y / (SourceTextPane.LINE_HEIGHT)) + 1;
				int maxLine = grammarAnalysis.currentLine;
				if ((line > 0) && (line <= maxLine)) {
					boolean hasBreakPoint = false;
					DebugBreakPoint point = null;
					for (DebugBreakPoint breakPoint : runtimeTextPane.breakLines) {
						if (breakPoint.lineNumber == line) {
							hasBreakPoint = true;
							point = breakPoint;
							break;
						}
					}
					if (e.getButton() == MouseEvent.BUTTON3) {
						lineNumberRightButtonClick(x, y, line, hasBreakPoint, point);
					} else if (e.getButton() == MouseEvent.BUTTON1) {
						lineNumberLeftButtonClick(line, hasBreakPoint, point);
					}
				}
				super.mouseClicked(e);
				sourceCodeEditorPane.requestFocus();
			}

		});
	}

	/**
	 * 当在行号面板上点击左键时触发
	 *
	 * @param line
	 *            行号
	 * @param hasBreakPoint
	 *            当前行是否有断点
	 * @param point
	 *            断点
	 */
	public void lineNumberLeftButtonClick(int line, boolean hasBreakPoint, DebugBreakPoint point) {
		long currentTime = System.currentTimeMillis();
		if ((currentTime - LINE_PREVIEW_CLICK_TIME) < LINE_DOUBLE_CLICK_TIME_GAP) {
			LINE_PREVIEW_CLICK_TIME = -LINE_DOUBLE_CLICK_TIME_GAP;
			if (hasBreakPoint) {
				runtimeTextPane.breakLines.remove(point);
			} else {
				DebugBreakPoint breakPoint = new DebugBreakPoint(line);
				runtimeTextPane.breakLines.add(breakPoint);
			}
			updateDebugPoint();
		} else {
			LINE_PREVIEW_CLICK_TIME = currentTime;
			if (hasBreakPoint) {
				point.enable = !point.enable;
				updateDebugPoint();
			}
		}
	}

	/**
	 * 当在行号面板上点击右键时触发
	 *
	 * @param x
	 *            x坐标
	 * @param y
	 *            y坐标
	 * @param line
	 *            行号
	 * @param hasBreakPoint
	 *            当前行是否有断点
	 * @param point
	 *            断点
	 */
	public void lineNumberRightButtonClick(int x, int y, final int line, boolean hasBreakPoint, DebugBreakPoint point) {
		JPopupMenu popupMenu = new JPopupMenu();
		final BreakPointItem add_del_item = new BreakPointItem(point);
		popupMenu.add(add_del_item);
		if (hasBreakPoint) {
			// 如果当前行有断点
			add_del_item.setText("删除断点");
			add_del_item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					runtimeTextPane.breakLines.remove(add_del_item.point);
					updateDebugPoint();
				}
			});
			final BreakPointItem enable_disable_item = new BreakPointItem(point);
			popupMenu.add(enable_disable_item);
			if (point.enable) {
				// 如果当前断点是启用状态
				enable_disable_item.setText("禁用断点");
			} else {
				// 如果当前断点是禁用状态
				enable_disable_item.setText("启用断点");
			}
			enable_disable_item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					enable_disable_item.point.enable = !enable_disable_item.point.enable;
					updateDebugPoint();
				}
			});
		} else {
			// 如果当前行没有断点
			add_del_item.setText("添加断点");
			add_del_item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					runtimeTextPane.breakLines.add(new DebugBreakPoint(line));
					updateDebugPoint();
				}
			});
		}
		popupMenu.show(lineNumberPane, x, y);
	}

	/**
	 * 更新调试断点
	 */
	public void updateDebugPoint() {
		Component[] components = lineNumberPane.getComponents();
		for (Component component : components) {
			if ((component instanceof JLabel) && (component != SourceTextPane.debugLabel)) {
				lineNumberPane.remove(component);
			}
		}
		for (DebugBreakPoint breakPoint : runtimeTextPane.breakLines) {
			JLabel label;
			if (breakPoint.enable) {
				label = new JLabel(MyIcons.RED_CIRCLE);
				lineNumberPane.add(label);
				label.setSize(SourceTextPane.TEXT_SIZE, SourceTextPane.TEXT_SIZE);
				label.setLocation(0, ((breakPoint.lineNumber - 1) * (SourceTextPane.LINE_HEIGHT)) + 2);
			} else {
				label = new JLabel(MyIcons.GRAY_CIRCLE);
				lineNumberPane.add(label);
				label.setSize(SourceTextPane.TEXT_SIZE, SourceTextPane.TEXT_SIZE);
				label.setLocation(0, ((breakPoint.lineNumber - 1) * (SourceTextPane.LINE_HEIGHT)) + 2);
			}
		}
		lineNumberPane.repaint();
	}

	/**
	 * 初始化运行菜单
	 */
	private void initRunMenu() {
		runMenu = new JMenu("运行(R)");
		runMenu.setMnemonic(KeyEvent.VK_R);
		menuBar.add(runMenu);

		startRunItem = new JMenuItem("开始运行(R)");
		startRunItem.setMnemonic(KeyEvent.VK_R);
		startRunItem.setToolTipText("直接运行CMM程序,不需要先编译");
		startRunItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		startRunItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (grammarAnalysis.internalCodes.size() <= 0) {
					runtimeTextPane.errorOut("请加载中间代码或者输入CMM源代码!\n");
					return;
				}
				if (!grammarAnalysis.hasMainFunction) {
					runtimeTextPane.errorOut("没有找到main函数!\n");
					return;
				}
				for (CMMToken token : lexicalAnalysis.tokens) {
					if (token.hasLexicalErrors() || (token.hasGrammarErrors() && !token.isGrammarWarning())) {
						runtimeTextPane.errorOut("line " + token.sourceLine + ":程序存在错误,请先修正错误!\n");
						return;
					}
				}
				for (InternalCode code : grammarAnalysis.internalCodes) {
					if (code.addrOrValue instanceof Symbol) {
						runtimeTextPane.errorOut("声明的函数(" + code.addrOrValue + ")未找到定义!\n");
						return;
					}
				}
				runtimeTextPane.mcommentOut("start running!\n");
				changeMenuWhenRunning(false);
				runtimeTextPane.run(false);
			}
		});
		startRunItem.setIcon(MyIcons.RUN_ITEM);
		runMenu.add(startRunItem);

		killItem = new JMenuItem("结束运行(K)");
		killItem.setMnemonic(KeyEvent.VK_K);
		killItem.setToolTipText("结束CMM程序的运行!");
		killItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_MASK | InputEvent.CTRL_MASK));
		killItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				stopProgram();
			}
		});
		killItem.setEnabled(false);
		killItem.setIcon(MyIcons.STOP_ITEM);
		runMenu.add(killItem);
	}

	/**
	 * 禁止编辑
	 */
	public void disableEdit() {
		sourceCodeEditorPane.setEditable(false);
		grammarAnalysisItem.setEnabled(false);
		compileMenu.setEnabled(false);
		debugMenu.setEnabled(false);
	}

	/**
	 * 允许编辑
	 */
	public void enableEdit() {
		sourceCodeEditorPane.setEditable(true);
		sourceCodeEditorPane.document.undoRedoManager.clear();
		grammarAnalysisItem.setEnabled(true);
		compileMenu.setEnabled(true);
		debugMenu.setEnabled(true);
		runtimeTextPane.breakLines.clear();
	}

	/**
	 * 初始化分析菜单
	 */
	private void initAnalysisMenu() {
		analysisMenu = new JMenu("分析(A)");
		analysisMenu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(analysisMenu);

		showCMMGrammar = new JMenuItem("显示CMM语法文件(M)");
		showCMMGrammar.setMnemonic(KeyEvent.VK_M);
		showCMMGrammar.setToolTipText("显示CMM的JavaCC语法文件");
		showCMMGrammar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));
		showCMMGrammar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runtimeTextPane.scommentOut(CMMFileUtils.readToEnd("/com/yuanhonglong/analysis/javaccAnalysis/cmm.jj"));
			}
		});
		showCMMGrammar.setIcon(MyIcons.M);
		analysisMenu.add(showCMMGrammar);

		analysisMenu.addSeparator();

		javaccAnalysis = new JMenuItem("JavaCC分析(J)");
		javaccAnalysis.setMnemonic(KeyEvent.VK_J);
		javaccAnalysis.setToolTipText("使用JavaCC工具进行词法和语法分析");
		javaccAnalysis.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));
		javaccAnalysis.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Date date = new Date();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String timestr = df.format(date);
				String str = sourceCodeEditorPane.getText();
				try (CharArrayReader reader = new CharArrayReader(str.toCharArray())) {
					CMMParser parser = new CMMParser(reader);
					parser.CompilationUnit();
					runtimeTextPane.strOut(timestr + " : " + "Javacc Parser passed. Correct!\n");
				} catch (ParseException | TokenMgrError e1) {
					runtimeTextPane.errorOut(e1.getMessage() + ".\n");
					runtimeTextPane.errorOut(timestr + " : " + "Javacc Parser passed. Error!\n");
				}
			}
		});
		javaccAnalysis.setIcon(MyIcons.J);
		analysisMenu.add(javaccAnalysis);

		lexicalAnalysisItem = new JMenuItem("词法分析(L)");
		lexicalAnalysisItem.setMnemonic(KeyEvent.VK_L);
		lexicalAnalysisItem.setToolTipText("使用自己的词法分析器进行词法分析");
		lexicalAnalysisItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		lexicalAnalysisItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								lexicalAnalysisBegin();
								runtimeTextPane.mcommentOut("Lexical Analysis:\n");
								runtimeTextPane.lexicalOut(lexicalAnalysis.tokens);
							}
						});
					}
				});
			}
		});
		lexicalAnalysisItem.setIcon(MyIcons.L);
		analysisMenu.add(lexicalAnalysisItem);

		grammarAnalysisItem = new JMenuItem("语法分析(G)");
		grammarAnalysisItem.setMnemonic(KeyEvent.VK_G);
		grammarAnalysisItem.setToolTipText("使用自己的语法分析器进行语法分析");
		grammarAnalysisItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		grammarAnalysisItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								while (grammarAnalysis.grammarTree.parent != null) {
									grammarAnalysis.grammarTree = grammarAnalysis.grammarTree.parent;
								}
								runtimeTextPane.mcommentOut("Grammar Analysis:\n");
								runtimeTextPane.grammarTreeOut(grammarAnalysis.grammarTree, "");
							}
						});
					}
				});
			}
		});
		grammarAnalysisItem.setIcon(MyIcons.G);
		analysisMenu.add(grammarAnalysisItem);
	}

	/**
	 * 开始语法分析
	 */
	public void grammarAnalysisBegin() {
		lexicalAnalysisBegin();
		grammarAnalysis.startAnalysis();
	}

	/**
	 * 开始词法分析
	 */
	public void lexicalAnalysisBegin() {
		String str;
		str = sourceCodeEditorPane.getText();
		lexicalAnalysis.startAnalysis(str);
	}

	/**
	 * 初始化示例菜单
	 */
	private void initExampleMenu() {
		exampleMenu = new JMenu("示例(E)");
		exampleMenu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(exampleMenu);

		for (int i = 0; i < EXAMPLE_FILES.length; i++) {
			final String name = EXAMPLE_FILES[i];
			JMenuItem example = new JMenuItem("示例文件" + (i + 1) + " : " + name);
			example.setToolTipText("显示CMM的示例文件");
			example.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					currentFile = null;
					sourceCodeEditorPane.setText(CMMFileUtils.readToEnd("/" + name));
					enableEdit();
					changed = false;
					sourceCodeEditorPane.requestFocus();
				}
			});
			exampleMenu.add(example);
		}
	}

	/**
	 * 初始化文件菜单
	 */
	private void initFileMenu() {
		fileMenu = new JMenu("文件(F)");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		newItem = new JMenuItem("新建(N)");
		newItem.setMnemonic(KeyEvent.VK_N);
		newItem.setToolTipText("新建CMM源文件");
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (changed) {
					int option = JOptionPane.showConfirmDialog(null, "有未保存的更改,是否保存?", "保存更改", JOptionPane.YES_NO_CANCEL_OPTION);
					if (option == JOptionPane.YES_OPTION) {
						if (saveFile()) {
							newSourceFile();
						}
					} else if (option == JOptionPane.NO_OPTION) {
						newSourceFile();
					}
				} else {
					newSourceFile();
				}
			}
		});
		fileMenu.add(newItem);

		openItem = new JMenuItem("打开(O)");
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.setToolTipText("打开CMM源文件");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (changed) {
					int option = JOptionPane.showConfirmDialog(null, "有未保存的更改,是否保存?", "保存更改", JOptionPane.YES_NO_CANCEL_OPTION);
					if (option == JOptionPane.YES_OPTION) {
						if (saveFile()) {
							chooseOpenFile();
						}
					} else if (option == JOptionPane.NO_OPTION) {
						chooseOpenFile();
					}
				} else {
					chooseOpenFile();
				}
			}
		});
		fileMenu.add(openItem);

		saveItem = new JMenuItem("保存(S)");
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setToolTipText("保存CMM源文件");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((currentFile == null) || changed) {
					saveFile();
				}
			}
		});
		fileMenu.add(saveItem);

		exitMenuItem = new JMenuItem("退出(E)");
		exitMenuItem.setMnemonic(KeyEvent.VK_E);
		exitMenuItem.setToolTipText("退出CMM IDE");
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (changed) {
					int option = JOptionPane.showConfirmDialog(null, "有未保存的更改,是否保存?", "保存更改", JOptionPane.YES_NO_CANCEL_OPTION);
					if (option == JOptionPane.YES_OPTION) {
						if (!saveFile()) {
							return;
						}
					} else if (option == JOptionPane.NO_OPTION) {
						newSourceFile();
					} else {
						return;
					}
				}
				MainFrame.this.dispose();
			}
		});
		exitMenuItem.setIcon(MyIcons.EXIT);
		fileMenu.add(exitMenuItem);
	}

	/**
	 * 选择要打开的文件
	 */
	public void chooseOpenFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(new File("."));
		chooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".cmm") || file.isDirectory();
			}

			@Override
			public String getDescription() {
				return "CMM源代码文件(*.cmm)";
			}

		});

		chooser.showDialog(null, null);
		currentFile = chooser.getSelectedFile();
		if (currentFile != null) {
			String str = CMMFileUtils.readToEnd(currentFile);
			sourceCodeEditorPane.setText(str);
			enableEdit();
		}
		changed = false;
	}

	/**
	 * 新建源文件
	 */
	public void newSourceFile() {
		currentFile = null;
		sourceCodeEditorPane.setText("void main(){\n" + "    return;\n" + "}");
		enableEdit();
		changed = false;
		runtimeTextPane.breakLines.clear();
	}

	/**
	 * 保存文件
	 *
	 * @return 是否保存成功
	 */
	public boolean saveFile() {
		String str = sourceCodeEditorPane.getText();
		if (currentFile == null) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setCurrentDirectory(new File("."));
			chooser.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return file.getName().endsWith(".cmm") || file.isDirectory();
				}

				@Override
				public String getDescription() {
					return "CMM源代码文件(*.cmm)";
				}

			});
			chooser.showSaveDialog(null);
			File f = chooser.getSelectedFile();
			if (f == null) {
				return false;
			}
			String s = f.getAbsolutePath();
			if (!s.endsWith(".cmm")) {
				s = s + ".cmm";
				f = new File(s);
			}
			if (f.exists()) {
				int option = JOptionPane.showConfirmDialog(null, "文件已经存在,是否覆盖?", "覆盖?", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					CMMFileUtils.writeToFile(f, str);
					currentFile = f;
				} else {
					return false;
				}
			} else {
				CMMFileUtils.writeToFile(f, str);
				currentFile = f;
			}

		} else {
			CMMFileUtils.writeToFile(currentFile, str);
		}
		changed = false;
		return true;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				UIManager.put("Label.font", MICROSOFT_YAHEI);
				UIManager.put("Button.font", MICROSOFT_YAHEI);
				UIManager.put("TextArea.font", MICROSOFT_YAHEI);
				UIManager.put("EditorPane.font", PLAIN_COURIERNEW);
				UIManager.put("TextPane.font", PLAIN_COURIERNEW);
				String systemUI = UIManager.getSystemLookAndFeelClassName();
				try {
					UIManager.setLookAndFeel(systemUI);
				} catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
					e.printStackTrace();
				}
				MainFrame cmmFrame = new MainFrame();
				cmmFrame.setVisible(true);
			}
		});
	}

}
