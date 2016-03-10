package com.yuanhonglong.debug;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import com.yuanhonglong.analysis.grammarAnalysis.MemoryArea;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.Function;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.VariableAddress;
import com.yuanhonglong.cmm.MainFrame;
import com.yuanhonglong.run.CMMVirtualMachine;
import com.yuanhonglong.util.VerticalLayout;

/**
 * 函数调用窗口<br>
 *
 * @author 天命剑主<br>
 *         on 2015/11/7.
 */
public class VariableFrame extends JPanel {

	private static final long		serialVersionUID	= 2842194769398340157L;

	public JPanel					memory_blocks_panel;						// 内存分区面板
	public JScrollPane				memory_blocks_scoll;						// 内存分区面板滚动条
	public ArrayList<DebugFunction>	functionCallList;							// 函数调用,活动记录
	public DebugButton				reg_button;									// 寄存器按钮
	public DebugButton				staticButton;								// 静态变量区按钮
	public JLabel					memoryLabel;								// 内存标签

	public JLabel					functionFrameLabel;							// 函数帧标签
	public JTable					functionFrameTable;							// 函数帧表格
	public JScrollPane				functionFrameScroll;						// 函数帧滚动条
	public FunctionFrameModel		functionFrameModel;							// 函数帧表格显示模式
	public JPanel					functionFramePanel;							// 函数帧面板

	public int						selected_button_index;						// 调试时选中的按钮
	public static final int			REG_INDEX			= 0;					// 寄存器按钮索引
	public static final int			STATIC_INDEX		= 1;					// 静态变量按钮索引

	public CMMVirtualMachine		machine;									// CMM虚拟机

	public static final int			BUTTON_HEIGHT		= 20;					// 按钮高度
	public static final int			MEMORY_DETAIL_GAP	= 15;					// 函数帧面板和CMM内存面板间距
	public static final int			MEMORY_WIDTH		= 100;					// CMM内存面板宽度
	public static final int			MEMORY_HEIGHT;								// CMM内存面板高度
	public static final int			FUN_FRAME_WIDTH;							// 函数帧面板宽度
	public static final int			FUN_FRAME_HEIGHT;							// 函数帧面板高度
	public static final int			FUN_FRAME_X;								// 函数帧面板在变量面板中的x坐标

	static {
		MEMORY_HEIGHT = MainFrame.VARIABLE_HEIGHT - MainFrame.LABEL_HEIGHT;
		FUN_FRAME_WIDTH = MainFrame.VARIABLE_WIDTH - MEMORY_WIDTH - (MEMORY_DETAIL_GAP * 2);
		FUN_FRAME_HEIGHT = MainFrame.VARIABLE_HEIGHT - MainFrame.LABEL_HEIGHT;
		FUN_FRAME_X = MEMORY_DETAIL_GAP + MEMORY_WIDTH;
	}

	public VariableFrame(CMMVirtualMachine machine) {
		this.machine = machine;
		setLayout(null);

		selected_button_index = -1;

		memory_blocks_panel = new JPanel();
		memory_blocks_panel.setLayout(new VerticalLayout());
		memory_blocks_panel.setBackground(Color.WHITE);
		memory_blocks_scoll = new JScrollPane(memory_blocks_panel);
		memory_blocks_scoll.setSize(MEMORY_WIDTH, MEMORY_HEIGHT);
		memory_blocks_scoll.setLocation(0, MainFrame.LABEL_HEIGHT);
		memory_blocks_scoll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		memory_blocks_scoll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.add(memory_blocks_scoll);

		functionCallList = new ArrayList<>();

		memoryLabel = new JLabel("CMM 内存");
		memoryLabel.setSize(MEMORY_WIDTH, MainFrame.LABEL_HEIGHT);
		memoryLabel.setLocation(0, 0);
		memoryLabel.setFont(MainFrame.MICROSOFT_YAHEI);
		memoryLabel.setForeground(MainFrame.NUMBER_COLOR);
		add(memoryLabel);

		reg_button = new DebugButton("寄存器");
		reg_button.setSize(MEMORY_WIDTH, BUTTON_HEIGHT);
		reg_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (selected_button_index != -1) {
					((DebugButton) memory_blocks_panel.getComponent(selected_button_index)).unselect();
				}
				selected_button_index = REG_INDEX;
				updateContent();
			}
		});

		staticButton = new DebugButton("静态变量");
		staticButton.setSize(MEMORY_WIDTH, BUTTON_HEIGHT);
		staticButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selected_button_index != -1) {
					((DebugButton) memory_blocks_panel.getComponent(selected_button_index)).unselect();
				}
				selected_button_index = STATIC_INDEX;
				updateContent();
			}
		});

		functionFrameTable = new JTable();
		functionFrameModel = new FunctionFrameModel();
		functionFrameTable.setModel(functionFrameModel);
		functionFrameTable.setFont(MainFrame.MICROSOFT_YAHEI);
		functionFrameTable.getTableHeader().setFont(MainFrame.MICROSOFT_YAHEI);

		functionFrameTable.setDefaultRenderer(Object.class, new FunctionFrameCellRenderer(this));

		functionFrameModel.addTableModelListener(new EditFunctionFrameVariableListener(this));

		functionFrameLabel = new JLabel("变量");
		functionFrameLabel.setSize(FUN_FRAME_WIDTH, MainFrame.LABEL_HEIGHT);
		functionFrameLabel.setLocation(FUN_FRAME_X, 0);
		functionFrameLabel.setFont(MainFrame.MICROSOFT_YAHEI);
		functionFrameLabel.setForeground(MainFrame.NUMBER_COLOR);
		add(functionFrameLabel);

		functionFrameScroll = new JScrollPane(functionFrameTable);
		functionFrameScroll.setSize(FUN_FRAME_WIDTH, FUN_FRAME_HEIGHT);
		functionFrameScroll.setLocation(0, 0);
		functionFrameScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		functionFrameScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		functionFrameScroll.getViewport().setBackground(Color.WHITE);

		functionFramePanel = new JPanel();
		functionFramePanel.setLayout(null);
		functionFramePanel.add(functionFrameScroll);
		functionFramePanel.setSize(FUN_FRAME_WIDTH, FUN_FRAME_HEIGHT);
		functionFramePanel.setLocation(FUN_FRAME_X, MainFrame.LABEL_HEIGHT);
		add(functionFramePanel);
	}

	/**
	 * 更新内存中的变量的值
	 *
	 * @param variable
	 *            内存中的变量
	 * @param newValue
	 *            新的值的字符串表示
	 * @return 即将更新的值
	 * @throws Exception
	 *             如果输入错误
	 */
	public Object update(Object variable, String newValue) throws Exception {
		boolean b;
		switch (newValue) {
			case "true":
				b = true;
				break;
			case "false":
				b = false;
				break;
			default:
				char[] cs = newValue.toCharArray();
				boolean integer = true;
				for (int i = 0; i < cs.length; i++) {
					char c = cs[i];
					if ((c > '9') || (c < '0')) {
						if (((c == '+') || (c == '-')) && (i == 0)) {
							continue;
						}
						integer = false;
						break;
					}
				}
				if (integer) {
					int i = Integer.parseInt(newValue);
					return cvt(variable, i);
				} else {
					double d = Double.parseDouble(newValue);
					return cvt(variable, d);
				}
		}
		return cvt(variable, b);
	}

	/**
	 * 将布尔值转换为内存中变量的类型
	 *
	 * @param variable
	 *            内存中变量
	 * @param b
	 *            布尔值
	 * @return 转换后的值
	 */
	public Object cvt(Object variable, boolean b) {
		if (variable instanceof Integer) {
			return b ? 1 : 0;
		} else if (variable instanceof Double) {
			return b ? 1.0 : 0.0;
		} else {
			return b;
		}
	}

	/**
	 * 将整数值转换为内存中变量的类型
	 *
	 * @param variable
	 *            内存中变量
	 * @param i
	 *            整数值
	 * @return 转换后的值
	 */
	public Object cvt(Object variable, int i) {
		if (variable instanceof Boolean) {
			return i != 0;
		} else if (variable instanceof Double) {
			return (double) i;
		} else {
			return i;
		}
	}

	/**
	 * 将浮点值转换为内存中变量的类型
	 *
	 * @param variable
	 *            内存中变量
	 * @param d
	 *            浮点值
	 * @return 转换后的值
	 */
	public Object cvt(Object variable, double d) {
		if (variable instanceof Boolean) {
			return d != 0.0;
		} else if (variable instanceof Integer) {
			return (int) d;
		} else {
			return d;
		}
	}

	/**
	 * 调试时初始化面板
	 */
	public void init() {
		functionCallList.clear();
		selected_button_index = -1;
		memory_blocks_panel.removeAll();
		memory_blocks_panel.add(reg_button);
		memory_blocks_panel.add(staticButton);
		// memory_blocks_panel.revalidate();
	}

	/**
	 * 函数调用前更新函数调用记录列表
	 *
	 * @param function
	 *            要调用函数
	 * @param size
	 *            函数所需的(新开辟的)空间大小
	 */
	public void addFunction(Function function, int size) {
		DebugFunction debugFunction = new DebugFunction(function, machine.top_pointer + 1, machine.top_pointer + size);
		final int button_index = functionCallList.size() + 2;
		functionCallList.add(debugFunction);
		DebugButton button = new DebugButton(function.name);
		button.setSize(MEMORY_WIDTH, BUTTON_HEIGHT);
		button.setFont(MainFrame.PLAIN_COURIERNEW);
		button.setForeground(MainFrame.NUMBER_COLOR);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (selected_button_index != -1) {
					((DebugButton) memory_blocks_panel.getComponent(selected_button_index)).unselect();
				}
				selected_button_index = button_index;
				updateContent();
			}
		});
		memory_blocks_panel.add(button);
	}

	/**
	 * 函数返回后移除函数
	 */
	public void removeFunction() {
		if (functionCallList.size() > 0) {
			functionCallList.remove(functionCallList.size() - 1);
			memory_blocks_panel.remove(memory_blocks_panel.getComponentCount() - 1);
		} else {
			// 函数栈异常为空
			machine.console.errorOut("函数栈错误,函数栈异常为空!");
		}
	}

	/**
	 * 寄存器详细信息
	 */
	public void updateContentPanelWithRegister() {
		Object[] identifier = new Object[] { "寄存器", "简称", "值" };
		functionFrameModel.setDataVector(null, identifier);
		functionFrameModel.addRow(new Object[] { "累加器", "ac", "" + machine.ac });
		functionFrameModel.addRow(new Object[] { "程序计数器", "pc", "" + machine.pc });
		functionFrameModel.addRow(new Object[] { "帧指针", "bp", "" + machine.base_pointer });
		functionFrameModel.addRow(new Object[] { "栈顶指针", "tp", "" + machine.top_pointer });
		functionFrameModel.addRow(new Object[] { "输入缓冲区", "in_buf", machine.buffer == null ? "" : machine.buffer });
		functionFrameModel.addRow(new Object[] { "挂起标志", "suspend", "" + (machine.suspendFlag ? 1 : 0) });
		functionFrameModel.addRow(new Object[] { "停止标志", "stop", "" + (machine.stopFlag ? 1 : 0) });
		functionFrameModel.addRow(new Object[] { "调试标志", "debug", "" + (machine.debugFlag ? 1 : 0) });
		functionFrameModel.addRow(new Object[] { "调试类型", "t_debug", "" + machine.debugType });
		functionFrameModel.addRow(new Object[] { "输入标志", "wait_in", "" + (machine.waitForInput ? 1 : 0) });
		functionFrameModel.addRow(new Object[] { "上一条指令", "pre_code", (machine.previewCode == null ? "" : machine.internalCodes.indexOf(machine.previewCode)) });
		functionFrameModel.addRow(new Object[] { "指令寄存器", "code_buf", machine.codeBuffer == null ? "" : machine.internalCodes.indexOf(machine.codeBuffer) });
	}

	/**
	 * 静态变量区详细信息
	 */
	public void updateContentPanelWithStatic() {
		Object[] identifier = new Object[] { "地址", "静态变量", "值" };
		functionFrameModel.setDataVector(null, identifier);
		ArrayList<VariableAddress> variableAddresses = machine.grammarAnalysis.staticNameTable;
		for (VariableAddress variableAddress : variableAddresses) {
			if (variableAddress.length == 0) {
				String address = variableAddress.address + "";
				String var = variableAddress.name;
				EditableVariable value = new EditableVariable(
						machine.staticVariables[variableAddress.address] == null ? "" : machine.staticVariables[variableAddress.address].toString(), variableAddress.address,
						MemoryArea.STATIC);
				functionFrameModel.addRow(new Object[] { address, var, value });
			} else {
				String address = variableAddress.address + "";
				String var = variableAddress.name;
				String empty = "";
				functionFrameModel.addRow(new Object[] { address, var, empty });

				for (int index = 0; index < variableAddress.length; index++) {
					address = (variableAddress.address + index) + "(" + variableAddress.address + "[" + index + "])";
					var = variableAddress.name + "[" + index + "]";
					EditableVariable value = new EditableVariable(
							machine.staticVariables[variableAddress.address + index] == null ? "" : machine.staticVariables[variableAddress.address + index].toString(),
							variableAddress.address + index, MemoryArea.STATIC);
					functionFrameModel.addRow(new Object[] { address, var, value });
				}
			}
		}
	}

	/**
	 * 函数帧详细信息
	 *
	 * @param watchedFunction
	 *            当前保持关注的函数
	 */
	public void updateContentPanelWithFunction(DebugFunction watchedFunction) {
		Object[] identifier = new Object[] { "地址", "本地变量", "值" };
		functionFrameModel.setDataVector(null, identifier);
		int bp = watchedFunction.bp;
		functionFrameModel.addRow(new Object[] { bp + "", "帧指针", machine.functionStack[bp] == null ? "" : machine.functionStack[bp].toString() });
		functionFrameModel.addRow(new Object[] { (bp + 1) + "", "返回地址", machine.functionStack[bp + 1] == null ? "" : machine.functionStack[bp + 1].toString() });
		ArrayList<VariableAddress> variableAddresses = watchedFunction.function.localVariableAddressTable;
		for (VariableAddress variableAddress : variableAddresses) {
			if (variableAddress.length == 0) {
				String address = (bp + variableAddress.address) + "(bp+" + variableAddress.address + ")";
				String var = variableAddress.name;
				EditableVariable value = new EditableVariable(
						machine.functionStack[bp + variableAddress.address] == null ? "" : machine.functionStack[bp + variableAddress.address].toString(),
						bp + variableAddress.address, MemoryArea.LOCAL);
				functionFrameModel.addRow(new Object[] { address, var, value });
			} else {
				String address = (bp + variableAddress.address) + "(bp+" + variableAddress.address + ")";
				String var = variableAddress.name;
				String empty = "";
				functionFrameModel.addRow(new Object[] { address, var, empty });

				for (int index = 0; index < variableAddress.length; index++) {
					address = (bp + variableAddress.address + index) + "(bp+" + variableAddress.address + "[" + index + "]" + ")";
					var = variableAddress.name + "[" + index + "]";
					EditableVariable value = new EditableVariable(
							machine.functionStack[bp + variableAddress.address + index] == null ? "" : machine.functionStack[bp + variableAddress.address + index].toString(),
							bp + variableAddress.address + index, MemoryArea.LOCAL);
					functionFrameModel.addRow(new Object[] { address, var, value });
				}
			}
		}
	}

	/**
	 * 更新详细信息面板
	 */
	public void updateContent() {
		if (selected_button_index == REG_INDEX) {
			((DebugButton) memory_blocks_panel.getComponent(REG_INDEX)).select();
			updateContentPanelWithRegister();
		} else if (selected_button_index == STATIC_INDEX) {
			((DebugButton) memory_blocks_panel.getComponent(STATIC_INDEX)).select();
			updateContentPanelWithStatic();
		} else {
			if (selected_button_index > 1) {
				((DebugButton) memory_blocks_panel.getComponent(selected_button_index)).select();
				updateContentPanelWithFunction(functionCallList.get(selected_button_index - 2));
			} else if (functionCallList.size() > 0) {
				updateContentPanelWithFunction(functionCallList.get(functionCallList.size() - 1));
			} else {
				updateContentPanelWithStatic();
			}
		}
		functionFrameTable.revalidate();
		memory_blocks_panel.revalidate();
	}

	/**
	 * 清空
	 */
	public void clear() {
		if ((selected_button_index > -1) && (selected_button_index < memory_blocks_panel.getComponentCount())) {
			((DebugButton) memory_blocks_panel.getComponent(selected_button_index)).unselect();
		}
		memory_blocks_panel.removeAll();
		memory_blocks_panel.repaint();
		functionFrameModel.setColumnCount(0);
		functionFrameTable.repaint();
	}
}
