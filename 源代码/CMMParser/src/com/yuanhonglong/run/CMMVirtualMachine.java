package com.yuanhonglong.run;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import com.yuanhonglong.analysis.grammarAnalysis.GrammarAnalysis;
import com.yuanhonglong.analysis.grammarAnalysis.InternalCode;
import com.yuanhonglong.analysis.grammarAnalysis.MemoryArea;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.Function;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.Symbol;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.VariableType;
import com.yuanhonglong.console.RuntimeTextPane;
import com.yuanhonglong.debug.DebugBreakPoint;
import com.yuanhonglong.debug.DebugType;
import com.yuanhonglong.source_editor.SourceTextPane;

/**
 * CMM虚拟机,负责执行中间代码
 *
 * @author 天命剑主<br>
 *         on 2015年10月18日
 */
public class CMMVirtualMachine implements Runnable {

	public GrammarAnalysis			grammarAnalysis;	// 语法分析器分析结果

	public Object					ac;					// CMM虚拟寄存器:算数累加器
	public int						base_pointer;		// CMM虚拟寄存器:基址指针
	public int						top_pointer;		// CMM虚拟寄存器:栈顶指针
	public int						pc;					// CMM虚拟寄存器:程序计数器

	public Object[]					staticVariables;	// CMM程序静态变量区
	public Object[]					functionStack;		// CMM程序函数栈区
	public ArrayList<InternalCode>	internalCodes;		// 代码区
	public String					buffer;				// CMM程序输入缓冲区(单行缓冲)
	public InternalCode				previewCode;		// 上一条指令
	public InternalCode				codeBuffer;			// 指令寄存器,存储当前的指令

	public Thread					thread;				// CMM程序主线程
	public boolean					suspendFlag;		// CMM程序挂起标志
	public boolean					stopFlag;			// 线程停止标志
	public boolean					debugFlag;			// 是否调试
	public DebugType				debugType;			// 调试类型
	public boolean					waitForInput;		// 是否等待输入

	public int						debugReturnAddress;	// 调试函数的返回地址

	public RuntimeTextPane			console;			// CMM程序输出区域

	public CMMVirtualMachine(GrammarAnalysis grammarAnalysis, RuntimeTextPane textPane) {
		this.grammarAnalysis = grammarAnalysis;
		this.console = textPane;
		functionStack = new Object[GrammarAnalysis.FUNCTION_STACK_SIZE];
	}

	/**
	 * 运行程序
	 */
	public void runProgram(boolean debug) {
		if (thread != null) {
			stop();
		}
		this.debugFlag = debug;
		suspendFlag = false;
		stopFlag = false;
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * 挂起线程
	 */
	public void suspend() {
		suspendFlag = true;
		if (debugFlag) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					console.frame.variablePanel.updateContent();
					console.frame.internalCodeFrame.updateFrame(pc);
					console.frame.sourceCodeEditorPane.showDebugLine(codeBuffer.lineNumber);
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							if (waitForInput) {
								console.requestFocus();
							} else {
								console.frame.sourceCodeEditorPane.requestFocus();
								console.frame.sourceCodeEditorPane.selectLine(codeBuffer.lineNumber);
							}
						}
					});
				}
			});
		}
	}

	/**
	 * 结束线程
	 */
	public void stop() {
		stopFlag = true;
		resume();
	}

	/**
	 * 如果不是正在等待输入,唤醒线程,继续执行
	 */
	public synchronized boolean resumeExceptRead() {
		if (waitForInput) {
			return false;
		}
		suspendFlag = false;
		notify();
		return true;
	}

	/**
	 * 唤醒线程,继续执行
	 */
	public synchronized void resume() {
		suspendFlag = false;
		notify();
	}

	@Override
	public void run() {
		initVirtualRegistersAndVirtualMemory();
		program: while (true) {
			if ((pc < 0) || (pc > internalCodes.size())) {
				instructionAddressOutOfBounds();
				break;
			}
			previewCode = codeBuffer;
			codeBuffer = internalCodes.get(pc);// 取指令
			if (debugFlag) {
				if (suspendIfDebug(codeBuffer)) {
					break;
				}
			}
			switch (codeBuffer.op) {
				case jmp: {
					// 无条件跳转
					if (!(codeBuffer.addrOrValue instanceof Integer)) {
						instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
						break program;
					}
					pc = (int) codeBuffer.addrOrValue;
					continue program;
				}
				case jmpc: {
					// 有条件跳转
					if (!(codeBuffer.extra instanceof Boolean)) {
						instruction_data_error(codeBuffer.extra.getClass(), Boolean.class);
						break program;
					}
					boolean v = (boolean) codeBuffer.extra;
					boolean c = (boolean) Calculator.cvt(VariableType.BOOLEAN, ac);
					if (v == c) {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						pc = (int) codeBuffer.addrOrValue;
						continue program;
					}
					break;
				}
				case load: {
					// 将内存中的数据加载到ac
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = codeBuffer.addrOrValue;
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = staticVariables[addr];
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = staticVariables[addr];
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = functionStack[addr + base_pointer];
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = functionStack[addr + base_pointer];
						}
					}
					break;
				}
				case mov: {
					// 将累加器的值写回内存
					if (!(codeBuffer.addrOrValue instanceof Integer)) {
						instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
						break program;
					}
					int addr = (int) codeBuffer.addrOrValue;
					int length = 1;
					if (codeBuffer.extra != null) {
						if (!(codeBuffer.extra instanceof Integer)) {
							instruction_data_error(codeBuffer.extra.getClass(), Integer.class);
							break program;
						}
						length = (int) codeBuffer.extra;
					}
					if (codeBuffer.memeryArea == MemoryArea.STATIC) {
						for (int i = 0; i < length; i++) {
							if (((addr + i) < 0) || ((addr + i) > staticVariables.length)) {
								memoryAddressOutOfBounds(addr + i);
								break program;
							}
							staticVariables[addr + i] = Calculator.mov(staticVariables[addr + i], ac);
						}
					} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
						if ((addr < 0) || (addr > staticVariables.length)) {
							memoryAddressOutOfBounds(addr);
							break program;
						}
						if (!(staticVariables[addr] instanceof Integer)) {
							memoryBroken(addr, MemoryArea.STATIC);
							break program;
						}
						addr = (int) staticVariables[addr];
						for (int i = 0; i < length; i++) {
							if (((addr + i) < 0) || ((addr + i) > staticVariables.length)) {
								memoryAddressOutOfBounds(addr + i);
								break program;
							}
							staticVariables[addr + i] = Calculator.mov(staticVariables[addr + i], ac);
						}
					} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
						if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
							memoryAddressOutOfBounds(addr);
							break program;
						}
						if (!(functionStack[addr + base_pointer] instanceof Integer)) {
							memoryBroken(addr, MemoryArea.LOCAL);
							break program;
						}
						addr = (int) functionStack[addr + base_pointer];
						for (int i = 0; i < length; i++) {
							if (((addr + i) < 1) || ((addr + i + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr + i);
								break program;
							}
							functionStack[addr + i + base_pointer] = Calculator.mov(functionStack[addr + i + base_pointer], ac);
						}
					} else if (codeBuffer.memeryArea == MemoryArea.ARGUMENT) {
						if (addr < 0) {
							memoryAddressOutOfBounds(addr);
							break program;
						}
						functionStack[addr + top_pointer + 1] = Calculator.mov(functionStack[addr + top_pointer + 1], ac);
					} else {
						for (int i = 0; i < length; i++) {
							if (((addr + i) < 1) || ((addr + i + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr + i);
								break program;
							}
							functionStack[addr + i + base_pointer] = Calculator.mov(functionStack[addr + i + base_pointer], ac);
						}
					}
					break;
				}
				case space: {
					// 开辟函数栈空间
					int max = functionStack.length;
					if (!(codeBuffer.addrOrValue instanceof Integer)) {
						instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
						break program;
					}
					int v = (int) codeBuffer.addrOrValue;
					if ((top_pointer + 1 + v) > max) {
						stackOverflow();
						break program;
					}
					if (debugFlag) {
						if (!(codeBuffer.extra instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int address = (int) codeBuffer.extra;
						for (Symbol symbol : grammarAnalysis.symbolTable.symbols) {
							if ((symbol.variable instanceof Function) && (symbol.address == address)) {
								console.frame.variablePanel.addFunction((Function) symbol.variable, v);
								break;
							}
						}
					}
					break;
				}
				case ret: {
					// 函数返回
					if (debugFlag) {
						console.frame.variablePanel.removeFunction();
					}
					if (!(functionStack[base_pointer + 1] instanceof Integer)) {
						memoryBroken(1, MemoryArea.LOCAL);
						break program;
					}
					pc = (int) functionStack[base_pointer + 1];
					continue program;
				}
				case pusht: {
					// 栈顶指针,入栈操作
					if (!(codeBuffer.addrOrValue instanceof Integer)) {
						instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
						break program;
					}
					int v = (int) codeBuffer.addrOrValue;
					top_pointer += v;
					break;
				}
				case popt: {
					Arrays.fill(functionStack, base_pointer + 1, top_pointer + 1, null);
					// 栈顶指针,出栈操作
					top_pointer = base_pointer - 1;
					break;
				}
				case pushb: {
					// 栈底指针,入栈操作
					functionStack[top_pointer + 1] = base_pointer;
					base_pointer = top_pointer + 1;
					break;
				}
				case popb: {
					// 栈底指针,出栈操作
					if (!(functionStack[base_pointer] instanceof Integer)) {
						memoryBroken(0, MemoryArea.LOCAL);
						break program;
					}
					int addr = (int) functionStack[base_pointer];
					functionStack[base_pointer] = null;
					base_pointer = addr;
					break;
				}
				case halt: {
					// 程序结束
					console.mcommentOut("\nProgram exit correct!\n");
					break program;
				}
				case read: {
					// 读操作
					if (!(codeBuffer.addrOrValue instanceof Integer)) {
						instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
						break program;
					}
					int addr = (int) codeBuffer.addrOrValue;
					VariableType inputType;
					if (codeBuffer.memeryArea == MemoryArea.STATIC) {
						if ((addr < 0) || (addr > staticVariables.length)) {
							memoryAddressOutOfBounds(addr);
							break program;
						}
						inputType = objectToVariable(staticVariables[addr]);
					} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
						if ((addr < 0) || (addr > staticVariables.length)) {
							memoryAddressOutOfBounds(addr);
							break program;
						}
						if (!(staticVariables[addr] instanceof Integer)) {
							memoryBroken(addr, MemoryArea.STATIC);
							break program;
						}
						addr = (int) staticVariables[addr];
						if ((addr < 0) || (addr > staticVariables.length)) {
							memoryAddressOutOfBounds(addr);
							break program;
						}
						inputType = objectToVariable(staticVariables[addr]);
					} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
						if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
							memoryAddressOutOfBounds(addr);
							break program;
						}
						if (!(functionStack[addr + base_pointer] instanceof Integer)) {
							memoryBroken(addr, MemoryArea.LOCAL);
							break program;
						}
						addr = (int) functionStack[addr + base_pointer];
						if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
							memoryAddressOutOfBounds(addr);
							break program;
						}
						inputType = objectToVariable(functionStack[base_pointer + addr]);
					} else {
						if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
							memoryAddressOutOfBounds(addr);
							break program;
						}
						inputType = objectToVariable(functionStack[base_pointer + addr]);
					}
					while (true) {
						waitForInput = true;
						suspend();
						try {
							if (programSuspend()) {
								break program;
							}
						} catch (InterruptedException e) {
							waitForInput = false;
							e.printStackTrace();
						}
						waitForInput = false;
						Object v;
						if (inputType == VariableType.INT) {
							try {
								v = Integer.parseInt(buffer.trim());
							} catch (Exception e) {
								console.errorOut("Input error! Please input a 32-bit integer:");
								buffer = null;
								continue;
							}
						} else if (inputType == VariableType.BOOLEAN) {
							try {
								v = Boolean.parseBoolean(buffer);
							} catch (Exception e) {
								console.errorOut("Input error! Please input true/false:");
								buffer = null;
								continue;
							}
						} else if (inputType == VariableType.REAL) {
							try {
								v = Double.parseDouble(buffer);
							} catch (Exception e) {
								console.errorOut("Input error! Please input a 64-bit real number:");
								buffer = null;
								continue;
							}
						} else {
							console.errorOut("Internal Error! Data type not matched! Program Crashed!");
							break program;
						}
						if ((codeBuffer.memeryArea == MemoryArea.STATIC) || (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT)) {
							staticVariables[addr] = v;
						} else {
							functionStack[addr + base_pointer] = v;
						}
						buffer = null;
						break;
					}
					break;
				}
				case write: {
					// 写操作
					if (codeBuffer.memeryArea == MemoryArea.STR_CON) {
						console.append(strToOutputString(codeBuffer.addrOrValue.toString()));
					} else {
						console.append(ac.toString());
					}
					break;
				}
				case inc: {
					// ++
					ac = Calculator.inc(ac);
					break;
				}
				case dec: {
					// --
					ac = Calculator.dec(ac);
					break;
				}
				case mul: {
					// 乘法
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.mul(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.mul(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.mul(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.mul(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.mul(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case minus: {
					// 减法
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.minus(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.minus(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.minus(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.minus(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.minus(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case plus: {
					// 加法
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.plus(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.plus(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.plus(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.plus(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.plus(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case div: {
					// 除法
					if (codeBuffer.memeryArea.isConstNumber()) {
						if (Calculator.isZero(codeBuffer.addrOrValue)) {
							dividedByZeroError();
							break program;
						}
						ac = Calculator.div(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (Calculator.isZero(staticVariables[addr])) {
								dividedByZeroError();
								break program;
							}
							ac = Calculator.div(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (Calculator.isZero(staticVariables[addr])) {
								dividedByZeroError();
								break program;
							}
							ac = Calculator.div(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (Calculator.isZero(functionStack[base_pointer + addr])) {
								dividedByZeroError();
								break program;
							}
							ac = Calculator.div(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (Calculator.isZero(functionStack[base_pointer + addr])) {
								dividedByZeroError();
								break program;
							}
							ac = Calculator.div(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case mod: {
					// 求余
					if (codeBuffer.memeryArea.isConstNumber()) {
						if (Calculator.isZero(codeBuffer.addrOrValue)) {
							dividedByZeroError();
							break program;
						}
						ac = Calculator.mod(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (Calculator.isZero(staticVariables[addr])) {
								dividedByZeroError();
								break program;
							}
							ac = Calculator.mod(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (Calculator.isZero(staticVariables[addr])) {
								dividedByZeroError();
								break program;
							}
							ac = Calculator.mod(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (Calculator.isZero(functionStack[base_pointer + addr])) {
								dividedByZeroError();
								break program;
							}
							ac = Calculator.mod(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (Calculator.isZero(functionStack[base_pointer + addr])) {
								dividedByZeroError();
								break program;
							}
							ac = Calculator.mod(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case lsh: {
					// 左移
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.lsh(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.lsh(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.lsh(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.lsh(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.lsh(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case rsh: {
					// 右移
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.rsh(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.rsh(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.rsh(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.rsh(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.rsh(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case bit_and: {
					// 按位与
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.bit_and(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_and(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_and(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_and(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_and(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case bit_or: {
					// 按位或
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.bit_or(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_or(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_or(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_or(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_or(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case logic_and: {
					// 逻辑与,短路与
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.logic_and(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.logic_and(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.logic_and(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.logic_and(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.logic_and(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case logic_or: {
					// 逻辑或,短路或
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.logic_or(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.logic_or(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.logic_or(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.logic_or(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.logic_or(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case logic_not: {
					// 逻辑非操作
					ac = Calculator.logic_not(ac);
					break;
				}
				case bit_xor: {
					// 按位异或操作
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.bit_xor(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_xor(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_xor(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_xor(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.bit_xor(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case bit_not: {
					// 按位取反
					ac = Calculator.bit_not(ac);
					break;
				}
				case gt: {
					// 大于
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.gt(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.gt(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.gt(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.gt(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.gt(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case lt: {
					// 小于
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.lt(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.lt(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.lt(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.lt(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.lt(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case ge: {
					// 大于或等于
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.ge(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.ge(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.ge(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.ge(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.ge(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case le: {
					// 小于或等于
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.le(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.le(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.le(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.le(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.le(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case ne: {
					// 不等于
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.ne(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.ne(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.ne(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.ne(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.ne(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case eq: {
					// 等于
					if (codeBuffer.memeryArea.isConstNumber()) {
						ac = Calculator.eq(codeBuffer.addrOrValue, ac);
					} else {
						if (!(codeBuffer.addrOrValue instanceof Integer)) {
							instruction_data_error(codeBuffer.addrOrValue.getClass(), Integer.class);
							break program;
						}
						int addr = (int) codeBuffer.addrOrValue;
						if (codeBuffer.memeryArea == MemoryArea.STATIC) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.eq(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.STATIC_INDIRECT) {
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(staticVariables[addr] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.STATIC);
								break program;
							}
							addr = (int) staticVariables[addr];
							if ((addr < 0) || (addr > staticVariables.length)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.eq(staticVariables[addr], ac);
						} else if (codeBuffer.memeryArea == MemoryArea.LOCAL_INDIRECT) {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							if (!(functionStack[addr + base_pointer] instanceof Integer)) {
								memoryBroken(addr, MemoryArea.LOCAL);
								break program;
							}
							addr = (int) functionStack[addr + base_pointer];
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.eq(functionStack[base_pointer + addr], ac);
						} else {
							if ((addr < 1) || ((addr + base_pointer) > top_pointer)) {
								memoryAddressOutOfBounds(addr);
								break program;
							}
							ac = Calculator.eq(functionStack[base_pointer + addr], ac);
						}
					}
					break;
				}
				case cvt: {
					// 数据类型转换命令
					ac = Calculator.cvt((VariableType) codeBuffer.addrOrValue, ac);
					break;
				}
				case opps: {
					// 取相反数
					ac = Calculator.opps(ac);
					break;
				}
				default:
					break;
			}
			pc++;// 程序计数器+1
		}
		endProgram();
		thread = null;
	}

	/**
	 * 初始化CMM程序的虚拟的内存和虚拟的寄存器
	 */
	private void initVirtualRegistersAndVirtualMemory() {
		Arrays.fill(functionStack, null);// 初始化函数栈
		staticVariables = new Object[grammarAnalysis.currentStaticIndex];// 初始化静态变量区
		base_pointer = 0;
		top_pointer = 0;
		buffer = null;// 初始化输入缓冲区
		internalCodes = grammarAnalysis.internalCodes;// 加载中间代码
		pc = 0;
		suspendFlag = false;
		stopFlag = false;
		previewCode = null;
		codeBuffer = null;
		debugReturnAddress = -1;
		waitForInput = false;
		if (debugFlag) {
			debugType = DebugType.CONTINUE;
			console.frame.internalCodeFrame.initFrame(grammarAnalysis.internalCodes);
			console.frame.variablePanel.init();
		}
	}

	/**
	 * 如果调试就挂起
	 *
	 * @param code
	 *            中间代码
	 * @return 是否要挂起
	 */
	private boolean suspendIfDebug(InternalCode code) {
		if (debugType == DebugType.CONTINUE) {
			if ((previewCode != null) && (code.lineNumber != previewCode.lineNumber)) {
				boolean suspend = false;// 是否挂起
				for (DebugBreakPoint debugBreakPoint : console.breakLines) {
					if (debugBreakPoint.enable && (debugBreakPoint.lineNumber == code.lineNumber)) {
						suspend = true;
						break;
					}
				}
				if (suspend) {
					suspend();
					try {
						if (programSuspend()) {
							return true;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} else if (debugType == DebugType.STEP_INTO) {
			suspend();
			try {
				if (programSuspend()) {
					return true;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if (debugType == DebugType.STEP_OUT) {
			if (pc == debugReturnAddress) {
				suspend();
				try {
					if (programSuspend()) {
						return true;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if (debugType == DebugType.NEXT_LINE) {
			if ((previewCode != null) && (code.lineNumber != previewCode.lineNumber)) {
				suspend();
				try {
					if (programSuspend()) {
						return true;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private boolean programSuspend() throws InterruptedException {
		synchronized (this) {
			while (suspendFlag) {
				wait();
			}
		}
		return stopFlag;
	}

	/**
	 * 程序结束
	 */
	private void endProgram() {
		stopFlag = true;
		if (debugFlag) {
			SourceTextPane.debugLabel.setLocation(-20, 0);
			console.frame.lineNumberPane.repaint();
			console.frame.internalCodeFrame.clear();
			console.frame.variablePanel.clear();
			console.frame.changeMenuWhenDebuging(true);
		}
		console.frame.changeMenuWhenRunning(true);
	}

	/**
	 * 除0错误
	 */
	private void dividedByZeroError() {
		console.errorOut("Divided by 0! Program crashed!\n");
		print_register_info();
		print_instruction_info();
	}

	/**
	 * 指令地址越界
	 */
	private void instructionAddressOutOfBounds() {
		console.errorOut("Instruction address out of bounds! Program crashed!\n");
		print_instruction_info();
		print_register_info();
	}

	/**
	 * 栈溢出
	 */
	private void stackOverflow() {
		console.errorOut("Stack overflow! Program crashed!\n");
		print_register_info();
		print_instruction_info();
	}

	/**
	 * 内存地址访问越界
	 */
	private void memoryAddressOutOfBounds(int addr) {
		console.errorOut("Memory address out of bounds! Program crashed!\n");
		console.errorOut("addr : " + addr + "\n");
		print_register_info();
		print_instruction_info();
	}

	/**
	 * 内存损坏
	 */
	private void memoryBroken(int addr, MemoryArea area) {
		console.errorOut("Memory Broken! Program crashed! Check you code!\n");
		print_register_info();
		print_instruction_info();
		print_current_stack(addr, area);
	}

	/**
	 * 打印栈信息
	 */
	public void print_current_stack(int addr, MemoryArea area) {
		if (area == MemoryArea.STATIC) {
			console.errorOut(staticVariables[addr].toString() + "\n");
		} else {
			for (int i = ((addr - 10) > 0 ? addr - 10 : 0); i < addr; i++) {
				console.errorOut((base_pointer + i) + " : " + functionStack[base_pointer + i].toString() + "\n");
			}
			console.errorOut((base_pointer + addr) + " : " + functionStack[base_pointer + addr].toString() + "  " + functionStack[base_pointer + addr].getClass().getName() + "\n");
			for (int i = addr + 1; i < ((addr + 10) > top_pointer ? top_pointer : addr + 10); i++) {
				console.errorOut((base_pointer + i) + " : " + functionStack[base_pointer + i].toString() + "\n");
			}
		}
	}

	/**
	 * 打印寄存器信息
	 */
	private void print_register_info() {
		console.errorOut("register info: \n");
		console.errorOut("base_pointer : " + base_pointer + "\n");
		console.errorOut("top_pointer : " + top_pointer + "\n");
		console.errorOut("pc : " + pc + "\n");
		console.errorOut("ac : " + ac + "\n\n");
	}

	/**
	 * 指令数据类型错误
	 */
	private void instruction_data_error(Class<?> dataType, Class<?> requiedType) {
		console.errorOut("Required " + requiedType.getName() + ", but got " + dataType.getName() + "!\n");
		print_instruction_info();
		print_register_info();
	}

	/**
	 * 打印指令信息
	 */
	private void print_instruction_info() {
		console.errorOut(grammarAnalysis.internalCodes.get(pc).toString() + "\n");
	}

	/**
	 * 变量类型判断
	 */
	private VariableType objectToVariable(Object o) {
		if (o instanceof Integer) {
			return VariableType.INT;
		} else if (o instanceof Double) {
			return VariableType.REAL;
		} else if (o instanceof Boolean) {
			return VariableType.BOOLEAN;
		} else {
			return VariableType.UNKNOWN;
		}
	}

	/**
	 * 处理转义字符
	 */
	public static String strToOutputString(String str) {
		return str.substring(1, str.length() - 1).replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r").replace("\\b", "\b").replace("\\t", "\t").replace("\\f", "\f")
				.replace("\\'", "'").replace("\\\"", "\"");
	}

	/**
	 * 设置当前调试函数的返回地址
	 */
	public void setDebugReturnAddress() {
		if ((top_pointer - base_pointer) > 0) {
			if (functionStack[base_pointer + 1] instanceof Integer) {
				debugReturnAddress = (int) functionStack[base_pointer + 1];
				if ((debugReturnAddress < 0) || (debugReturnAddress >= internalCodes.size())) {
					memoryAddressOutOfBounds(debugReturnAddress);
				}
			} else {
				memoryBroken(base_pointer + 1, MemoryArea.LOCAL);
			}
		}
	}

}
