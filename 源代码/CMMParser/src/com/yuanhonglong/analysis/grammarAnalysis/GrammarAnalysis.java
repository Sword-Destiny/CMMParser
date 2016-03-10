package com.yuanhonglong.analysis.grammarAnalysis;

import java.util.ArrayList;

import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.CMMVariable;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.Function;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.ReturnType;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.ReturnVariable;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.Symbol;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.SymbolTable;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.VariableAddress;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.VariableType;
import com.yuanhonglong.analysis.lexical_analysis.CMMToken;
import com.yuanhonglong.analysis.lexical_analysis.LexicalAnalysis;
import com.yuanhonglong.analysis.lexical_analysis.TokenType;
import com.yuanhonglong.run.Calculator;

/**
 * 语法分析器
 *
 * @author 天命剑主<br>
 *         on 2015/9/28.
 */
public class GrammarAnalysis {

	public ArrayList<InternalCode>		internalCodes;		// 中间代码
	private int							currentScopeDepth;	// 当前作用域深度
	public int							length;				// 短语数量
	public SymbolTable					symbolTable;		// 符号表
	public int							currentStaticIndex;	// 当前静态变量区
	public Function						currentFunction;	// 当前函数
	private int							cycle_start;		// 循环起始处
	private int							cycle_end;			// 循环结束处
	public LexicalAnalysis				lexicalAnalysis;	// 词法分析器
	public GrammarTree					grammarTree;		// 语法树当前节点
	private boolean						recoed;				// 是否记录语法树
	public boolean						hasMainFunction;	// 是否有mian函数
	public int							currentLine;		// 当前源代码行
	public ArrayList<VariableAddress>	staticNameTable;	// 储存静态变量名称等调试信息

	public GrammarAnalysis(LexicalAnalysis lexicalAnalysis) {
		this.lexicalAnalysis = lexicalAnalysis;
		internalCodes = new ArrayList<>();
		staticNameTable = new ArrayList<>();
	}

	/**
	 * 返回可以写入文件的字符串
	 */
	public String getCodesStr() {
		String str = "";
		ArrayList<InternalCode> internalCodes1 = this.internalCodes;
		for (int i = 0; i < internalCodes1.size(); i++) {
			InternalCode internalCode = internalCodes1.get(i);
			str += String.format("0x%08x  |%4s:  ", i, internalCode.lineNumber) + internalCode.toString();
		}
		return str;
	}

	/**
	 * 返回短语
	 */
	public CMMToken getToken(int i) {
		CMMToken token = this.lexicalAnalysis.tokens.get(i);
		currentLine = token.sourceLine;
		return token;
	}

	/**
	 * 记录语法树
	 */
	public void grammarTreeRecord(Object out) {
		if (recoed) {
			String string = out.toString();
			if (((grammarTree.value == null) || grammarTree.value.equals("")) && (grammarTree.children.size() == 0)) {
				grammarTree.value = string;
			} else {
				if (grammarTree.parent != null) {
					grammarTree.parent.addChild();
					grammarTree = grammarTree.parent.lastChild();
					grammarTree.value = string;
				}
			}
		}
	}

	/**
	 * 初始化变量
	 */
	public void initSymbol(Symbol symbol) {
		InternalCode load;
		InternalCode mov;
		switch (symbol.getType()) {
			case INT:
				load = new InternalCode(CMMOperator.load, 0, MemoryArea.INT_CON, currentLine);
				internalCodes.add(load);
				break;
			case REAL:
				load = new InternalCode(CMMOperator.load, 0.0, MemoryArea.REAL_CON, currentLine);
				internalCodes.add(load);
				break;
			case BOOLEAN:
				load = new InternalCode(CMMOperator.load, false, MemoryArea.BOOL_CON, currentLine);
				internalCodes.add(load);
				break;
			default:
				return;
		}
		if (symbol.variable.length > 0) {
			mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, symbol.variable.length, currentLine);
			internalCodes.add(mov);
		} else {
			mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
			internalCodes.add(mov);
		}
	}

	/**
	 * 生成加载常量的中间代码
	 */
	public void loadConstSymbol(Symbol symbol) {
		InternalCode load = new InternalCode(CMMOperator.load, symbol.value, symbol.memoryArea, currentLine);
		internalCodes.add(load);
	}

	/**
	 * 生成加载左值的中间代码
	 */
	public void loadLeftSymbol(Symbol symbol) {
		InternalCode load = new InternalCode(CMMOperator.load, symbol.address, symbol.memoryArea, currentLine);
		internalCodes.add(load);
	}

	/**
	 * 移除最后的加载代码
	 */
	public void removeLastLoad() {
		if (internalCodes.size() == 0) {
			return;
		}
		int index = internalCodes.size() - 1;
		if (internalCodes.get(index).op == CMMOperator.load) {
			internalCodes.remove(index);
		}
	}

	/**
	 * 开始语法分析
	 */
	public void startAnalysis() {
		currentLine = 1;
		recoed = true;
		cycle_start = -1;
		cycle_end = -1;
		hasMainFunction = false;
		currentScopeDepth = 0;
		internalCodes.clear();
		grammarTree = new GrammarTree(-1, treeNodeLSymbol + "CMM program" + treeNodeRSymbol);
		grammarTree.addChild();
		grammarTree = grammarTree.lastChild();
		int i = -1;
		length = this.lexicalAnalysis.tokens.size();
		currentStaticIndex = 0;
		symbolTable = new SymbolTable();
		staticNameTable.clear();
		while (i < length) {
			i = tokenSkip(i);
			if (i >= length) {
				break;
			}
			CMMToken token = getToken(i);
			int tmp_index = i;
			identifier: {
				if (!token.in(new String[] { VOID, INT, BOOLEAN, REAL })) {
					GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { VOID, INT, BOOLEAN, REAL });
					ErrorReturn result = GrammarError.skipTo(this, i,
							new TokenType[] {},
							new String[] { VOID, INT, BOOLEAN, REAL },
							new TokenType[] { TokenType.IDENTIFIER },
							new String[] {},
							new TokenType[] {},
							new String[] { SEMICOLON, R_LARGE_BRACKET });
					i = result.i;
					if (result.type == ErrorReturnType.END_TYPE) {
						continue;
					} else if (result.type == ErrorReturnType.AFTER_TYPE) {
						break identifier;
					} else {
						token = getToken(i);
					}
				}
				if (token.word.equals(VOID)) {
					grammarRecordInc("function");
					i = statementFunction(i);
					grammarRecorddec();
					continue;
				}
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return;
				}
				token = getToken(i);
				CMMToken unmatchedSymbolToken = token;
				if (token.word.equals(L_MIDDLE_BRACKET)) {
					i = tokenSkip(i);
					if (i >= length) {
						GrammarError.unexpectedEndOfFile(this, i);
						return;
					}
					token = getToken(i);
					if (token.type != TokenType.DECIMAL_NUMBER) {
						GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.DECIMAL_NUMBER }, new String[] {});
						ErrorReturn result = GrammarError.skipTo(this, i,
								new TokenType[] { TokenType.DECIMAL_NUMBER },
								new String[] {},
								new TokenType[] {},
								new String[] { R_MIDDLE_BRACKET },
								new TokenType[] {},
								new String[] { SEMICOLON, R_LARGE_BRACKET });
						i = result.i;
						if (result.type == ErrorReturnType.END_TYPE) {
							continue;
						}
					}
					i = tokenSkip(i);
					if (i >= length) {
						GrammarError.unexpectedEndOfFile(this, i);
						return;
					}
					token = getToken(i);
					if (!token.word.equals(R_MIDDLE_BRACKET)) {
						GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_MIDDLE_BRACKET });
						GrammarError.unmatchedSymbol(unmatchedSymbolToken);
						ErrorReturn result = GrammarError.skipTo(this, i,
								new TokenType[] {},
								new String[] { R_MIDDLE_BRACKET },
								new TokenType[] { TokenType.IDENTIFIER },
								new String[] {},
								new TokenType[] {},
								new String[] { SEMICOLON, R_LARGE_BRACKET });
						i = result.i;
						if (result.type == ErrorReturnType.END_TYPE) {
							continue;
						}
					}
					i = tokenSkip(i);
					if (i >= length) {
						GrammarError.unexpectedEndOfFile(this, i);
						return;
					}
					token = getToken(i);
				}
				if (!token.type.equals(TokenType.IDENTIFIER)) {
					GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER }, new String[] {});
					ErrorReturn result = GrammarError.skipTo(this, i,
							new TokenType[] { TokenType.IDENTIFIER },
							new String[] {},
							new TokenType[] {},
							new String[] { ASSIGN, COMMA, SEMICOLON, L_SMALL_BRACKET },
							new TokenType[] {},
							new String[] { SEMICOLON, R_LARGE_BRACKET });
					i = result.i;
					if (result.type == ErrorReturnType.END_TYPE) {
						continue;
					}
				}
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return;
			}
			token = getToken(i);
			if (!token.in(new String[] { ASSIGN, COMMA, SEMICOLON, L_SMALL_BRACKET })) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { ASSIGN, COMMA, SEMICOLON, L_SMALL_BRACKET });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] {},
						new String[] { ASSIGN, COMMA, SEMICOLON, L_SMALL_BRACKET },
						new TokenType[] { TokenType.IDENTIFIER },
						new String[] { INT, BOOLEAN, REAL },
						new TokenType[] {},
						new String[] { R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					continue;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					i = tokenSkip(i);
					token = getToken(i);
					if (token.in(new String[] { INT, BOOLEAN, REAL })) {
						grammarRecordInc("function");
						i = statementFunction(tmp_index);
						grammarRecorddec();
					} else {
						grammarRecordInc("static variable");
						i = statementDeclarationStaticVariable(tmp_index);
						grammarRecorddec();
					}
				} else {
					token = getToken(i);
				}
			}
			switch (token.word) {
				case ASSIGN:
				case COMMA:
				case SEMICOLON:
					grammarRecordInc("static variable");
					i = statementDeclarationStaticVariable(tmp_index);
					grammarRecorddec();
					break;
				case L_SMALL_BRACKET:
					grammarRecordInc("function");
					i = statementFunction(tmp_index);
					grammarRecorddec();
					break;
				default:
					break;
			}

		}
		// 检查是否存在main函数
		for (Symbol symbol : symbolTable.symbols) {
			if (symbol.identifier.equals("main") && (symbol.getType() == VariableType.FUNCTION)) {
				hasMainFunction = true;
				int mainSize = ((Function) symbol.variable).stackSize;
				// 开辟mian函数空间
				InternalCode space = new InternalCode(CMMOperator.space, mainSize, MemoryArea.INT_CON, symbol.address, 0);// 本需回填
				internalCodes.add(space);
				// main函数返回地址
				InternalCode load_return_addr = new InternalCode(CMMOperator.load, "main", MemoryArea.INT_CON, 0);// 待回填
				internalCodes.add(load_return_addr);
				InternalCode mov_return_addr = new InternalCode(CMMOperator.mov, 1, MemoryArea.ARGUMENT, 0);
				internalCodes.add(mov_return_addr);
				// 压栈
				InternalCode pushb = new InternalCode(CMMOperator.pushb, 0);// 运行时bp初始为0,sp初始为0
				internalCodes.add(pushb);
				InternalCode pusht = new InternalCode(CMMOperator.pusht, mainSize, MemoryArea.INT_CON, 0);// 本需回填
				internalCodes.add(pusht);
				// 跳转
				InternalCode start = new InternalCode(CMMOperator.jmp, symbol.address, MemoryArea.ADDRESS, 0);// 本需回填
				internalCodes.add(start);
				// 返填地址
				load_return_addr.addrOrValue = internalCodes.size();// main函数返回地址
				// 弹栈
				InternalCode popt = new InternalCode(CMMOperator.popt, 0);// 运行时bp初始为0,sp初始为0
				internalCodes.add(popt);
				InternalCode popb = new InternalCode(CMMOperator.popb, 0);
				internalCodes.add(popb);
				// 结束
				InternalCode halt = new InternalCode(CMMOperator.halt, 0);
				internalCodes.add(halt);
				break;
			}
		}
	}

	/**
	 * 获取上一个有效的符号
	 */
	public int tokenPreview(int i) {
		i--;
		while ((i >= 0) && (i < length)) {
			CMMToken token = getToken(i);
			switch (token.type) {
				case LINE_COMMENT:
				case MULTI_COMMENT:
				case SPACE:
				case UNKNOWN_SYMBOL:
					i--;
					break;
				default:
					return i;
			}
		}
		return i;
	}

	/**
	 * 获取下一个有效的符号
	 */
	public int tokenSkip(int i) {
		i++;
		while ((i < length) && (i >= 0)) {
			CMMToken token = getToken(i);
			switch (token.type) {
				case LINE_COMMENT:
				case MULTI_COMMENT:
				case SPACE:
				case UNKNOWN_SYMBOL:
					i++;
					break;
				default:
					return i;
			}
		}
		return i;
	}

	/**
	 * 返填函数调用地址和函数信息
	 */
	private void backFillFunctionCall(Function f, int address, String id) {
		for (InternalCode code : internalCodes) {
			if ((code.addrOrValue instanceof Symbol) && f.equals(((Symbol) code.addrOrValue).variable) && id.equals(((Symbol) code.addrOrValue).identifier)) {
				if (code.extra.equals(SIZE)) {
					code.addrOrValue = f.stackSize;
					if (code.op == CMMOperator.space) {
						code.extra = address;
					} else {
						code.extra = null;
					}
				} else if (code.extra.equals(ADDRESS)) {
					code.extra = null;
					code.addrOrValue = address;
					code.extra = null;
				}
			}
		}
	}

	/**
	 * 清理符号表
	 */
	private void clearSymbolTable(int scopeDepth) {
		for (int i = 0; i < symbolTable.symbols.size(); i++) {
			if (symbolTable.symbols.get(i).scopeDepth > scopeDepth) {
				symbolTable.symbols.remove(i);
				i--;
			}
		}
	}

	/**
	 * 与
	 */
	private int expAnd(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expEquality(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		while (token.word.equals(BIT_AND)) {
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			if (lSymbol.isRightValue()) {
				if (symbol.memoryArea == MemoryArea.STATIC) {
					lSymbol.address = currentStaticIndex;
					staticNameTable.add(new VariableAddress(currentStaticIndex, expToken.word, 0));
					currentStaticIndex++;
					if (currentStaticIndex > MAX_STATIC_SIZE) {
						GrammarError.staticAreaOverflow(token);
					}
				} else {
					lSymbol.address = currentFunction.stackSize;
					currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, expToken.word, 0));
					currentFunction.stackSize++;
				}
				lSymbol.memoryArea = symbol.memoryArea;
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				lSymbol.setLeft();
			} else {
				removeLastLoad();
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			i = expEquality(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			CalculateResult result = lSymbol.calcuAnd(this, lSymbol, rSymbol);
			expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		return i - 1;
	}

	/**
	 * 数组下标处理
	 */
	private int expArrayIndex(int i, Symbol symbol) {
		CMMToken lMidBracketToken = getToken(i);
		grammarTreeRecord(lMidBracketToken.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		CMMToken token = getToken(i);
		if (!token.startOfExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] { R_MIDDLE_BRACKET },
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				token = getToken(i);
				grammarTreeRecord(token.word);
				return i;
			}
		}
		Symbol rSymbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
		grammarRecordInc("expression");
		i = expression(i, rSymbol);
		grammarRecorddec();
		if (rSymbol.getType() != VariableType.INT) {
			GrammarError.variableTypeNotMatch(token, rSymbol.getType(), VariableType.INT);
			InternalCode cvt = new InternalCode(CMMOperator.cvt, VariableType.INT, currentLine);
			internalCodes.add(cvt);
		}
		if (symbol.variable.length > 0) {
			if (rSymbol.isConstValue()) {
				removeLastLoad();
				symbol.variable.length = 0;
				symbol.setLeft();
				symbol.address = symbol.address + (int) Calculator.cvt(VariableType.INT, rSymbol.value);
				loadLeftSymbol(symbol);
			} else {
				InternalCode plus = new InternalCode(CMMOperator.plus, symbol.address, MemoryArea.INT_CON, currentLine);
				internalCodes.add(plus);
				if (symbol.memoryArea == MemoryArea.STATIC) {
					symbol.address = currentStaticIndex;
					staticNameTable.add(new VariableAddress(currentStaticIndex, "arrayIndex", 0));
					currentStaticIndex++;
					if (currentStaticIndex > MAX_STATIC_SIZE) {
						GrammarError.statementOutOfFunction(token);
					}
				} else {
					symbol.address = currentFunction.stackSize;
					currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, "arrayIndex", 0));
					currentFunction.stackSize++;
				}
				InternalCode mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
				internalCodes.add(mov);
				if (symbol.memoryArea == MemoryArea.STATIC) {
					symbol.memoryArea = MemoryArea.STATIC_INDIRECT;
				} else {
					symbol.memoryArea = MemoryArea.LOCAL_INDIRECT;
				}
				symbol.setLeft();
				symbol.variable.length = 0;
				loadLeftSymbol(symbol);
			}
		} else {
			GrammarError.noArrayNoSub(lMidBracketToken);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(R_MIDDLE_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { R_MIDDLE_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * 大于,小于,大于等于,小于等于
	 */
	private int expCompare(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expShift(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		while (token.word.equals(GREATER_THAN) || token.word.equals(GREATER_EQUAL) || token.word.equals(LESS_THAN) || token.word.equals(LESS_EQUAL)) {
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			if (lSymbol.isRightValue()) {
				if (symbol.memoryArea == MemoryArea.STATIC) {
					lSymbol.address = currentStaticIndex;
					staticNameTable.add(new VariableAddress(currentStaticIndex, expToken.word, 0));
					currentStaticIndex++;
					if (currentStaticIndex > MAX_STATIC_SIZE) {
						GrammarError.staticAreaOverflow(token);
					}
				} else {
					lSymbol.address = currentFunction.stackSize;
					currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, expToken.word, 0));
					currentFunction.stackSize++;
				}
				lSymbol.memoryArea = symbol.memoryArea;
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				lSymbol.setLeft();
			} else {
				removeLastLoad();
			}
			String word = token.word;
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			i = expShift(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			switch (word) {
				case GREATER_EQUAL:
					expResultTypeError(lSymbol.calcuGe(this, lSymbol, rSymbol), expToken, oldLSymbol, rSymbol);
					break;
				case GREATER_THAN:
					expResultTypeError(lSymbol.calcuGt(this, lSymbol, rSymbol), expToken, oldLSymbol, rSymbol);
					break;
				case LESS_EQUAL:
					expResultTypeError(lSymbol.calcuLe(this, lSymbol, rSymbol), expToken, oldLSymbol, rSymbol);
					break;
				default:
					expResultTypeError(lSymbol.calcuLt(this, lSymbol, rSymbol), expToken, oldLSymbol, rSymbol);
					break;
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		return i - 1;
	}

	/**
	 * ++和--
	 */
	private int expCrement(int i, Symbol symbol) {
		CMMToken token = getToken(i);
		switch (token.word) {
			case INCREMENT:
				expIncrementBefore(i, symbol);
				return i;
			case DECREMENT:
				expDecrementBefore(i, symbol);
				return i;
			default:
				break;
		}
		if (!token.startOfPrimaryExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		i = expPrimary(i, symbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		switch (token.word) {
			case INCREMENT: {
				grammarTreeRecord(token.word);
				if (!symbol.variable.left) {
					GrammarError.onlyLeftValueCanCrement(token);
				}
				if (symbol.getType() == VariableType.BOOLEAN) {
					GrammarError.variableTypeNotMatch(token, VariableType.BOOLEAN, VariableType.INT, VariableType.REAL);
					symbol.addCvtCode(this, VariableType.INT);
					symbol.variable.type = VariableType.INT;
				}
				InternalCode inc = new InternalCode(CMMOperator.inc, currentLine);
				internalCodes.add(inc);
				InternalCode mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
				internalCodes.add(mov);
				InternalCode dec = new InternalCode(CMMOperator.dec, currentLine);
				internalCodes.add(dec);
				symbol.setRight();
				return i;
			}
			case DECREMENT: {
				grammarTreeRecord(token.word);
				if (!symbol.variable.left) {
					GrammarError.onlyLeftValueCanCrement(token);
				}
				if (symbol.getType() == VariableType.BOOLEAN) {
					GrammarError.variableTypeNotMatch(token, VariableType.BOOLEAN, VariableType.INT, VariableType.REAL);
					symbol.addCvtCode(this, VariableType.INT);
					symbol.variable.type = VariableType.INT;
				}
				InternalCode dec = new InternalCode(CMMOperator.dec, currentLine);
				internalCodes.add(dec);
				InternalCode mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
				internalCodes.add(mov);
				InternalCode inc = new InternalCode(CMMOperator.inc, currentLine);
				internalCodes.add(inc);
				symbol.setRight();
				return i;
			}
			default:
				return i - 1;
		}
	}

	/**
	 * 前置--
	 */
	private int expDecrementBefore(int i, Symbol symbol) {
		CMMToken token = getToken(i);
		CMMToken crementToken = token;
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.startOfPrimaryExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		i = expPrimary(i, symbol);
		if (!symbol.isLeftValue()) {
			GrammarError.onlyLeftValueCanCrement(crementToken);
		}
		if (symbol.getType() == VariableType.BOOLEAN) {
			GrammarError.variableTypeNotMatch(crementToken, VariableType.BOOLEAN, VariableType.INT, VariableType.REAL);
			symbol.addCvtCode(this, VariableType.INT);
			symbol.variable.type = VariableType.INT;
		}
		InternalCode dec = new InternalCode(CMMOperator.dec, currentLine);
		internalCodes.add(dec);
		InternalCode mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
		internalCodes.add(mov);
		symbol.variable.left = false;// --操作后变为右值
		return i;
	}

	/**
	 * 等于,不等于
	 */
	private int expEquality(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expCompare(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		while (token.word.equals(DOUBEL_EQUAL) || token.word.equals(NOT_EQUAL)) {
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			if (lSymbol.isRightValue()) {
				if (symbol.memoryArea == MemoryArea.STATIC) {
					lSymbol.address = currentStaticIndex;
					staticNameTable.add(new VariableAddress(currentStaticIndex, expToken.word, 0));
					currentStaticIndex++;
					if (currentStaticIndex > MAX_STATIC_SIZE) {
						GrammarError.staticAreaOverflow(token);
					}
				} else {
					lSymbol.address = currentFunction.stackSize;
					currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, expToken.word, 0));
					currentFunction.stackSize++;
				}
				lSymbol.memoryArea = symbol.memoryArea;
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				lSymbol.setLeft();
			} else {
				removeLastLoad();
			}
			String word = token.word;
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			i = expCompare(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			if (word.equals(NOT_EQUAL)) {
				CalculateResult result = lSymbol.calcuNe(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			} else {
				CalculateResult result = lSymbol.calcuEq(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		return i - 1;
	}

	/**
	 * 前置++
	 */
	private int expIncrementBefore(int i, Symbol symbol) {
		CMMToken token = getToken(i);
		CMMToken crementToken = token;
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.startOfPrimaryExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		i = expPrimary(i, symbol);
		if (!symbol.isLeftValue()) {
			GrammarError.onlyLeftValueCanCrement(crementToken);
		}
		if (symbol.getType() == VariableType.BOOLEAN) {
			GrammarError.variableTypeNotMatch(crementToken, VariableType.BOOLEAN, VariableType.INT, VariableType.REAL);
			symbol.addCvtCode(this, VariableType.INT);
			symbol.variable.type = VariableType.INT;
		}
		InternalCode inc = new InternalCode(CMMOperator.inc, currentLine);
		internalCodes.add(inc);
		InternalCode mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
		internalCodes.add(mov);
		symbol.variable.left = false;// ++操作后变为右值
		return i;
	}

	/**
	 * 短路与
	 */
	private int expLogicAnd(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expOr(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		InternalCode jmpc = new InternalCode(CMMOperator.jmpc, -1, MemoryArea.ADDRESS, false, currentLine);
		boolean firstExp = true;
		while (token.word.equals(LOGIC_AND)) {
			if (firstExp) {
				lSymbol.addCvtCode(this, VariableType.BOOLEAN);
			}
			firstExp = false;
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			internalCodes.add(jmpc);
			i = expOr(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			CalculateResult result = lSymbol.calcuLogicAnd(this, lSymbol, rSymbol);
			expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		jmpc.addrOrValue = internalCodes.size();
		return i - 1;
	}

	/**
	 * 短路或
	 */
	private int expLogicOr(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expLogicAnd(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		InternalCode jmpc = new InternalCode(CMMOperator.jmpc, -1, MemoryArea.ADDRESS, true, currentLine);
		boolean firstExp = true;
		while (token.word.equals(LOGIC_OR)) {
			if (firstExp) {
				lSymbol.addCvtCode(this, VariableType.BOOLEAN);
			}
			firstExp = false;
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			internalCodes.add(jmpc);
			i = expLogicAnd(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			CalculateResult result = lSymbol.calcuLogicOr(this, lSymbol, rSymbol);
			expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		jmpc.addrOrValue = internalCodes.size();
		return i - 1;
	}

	/**
	 * 乘法和除法
	 */
	private int expMulDiv(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expUnary(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		while (token.word.equals(SLASH) || token.word.equals(STAR) || token.word.equals(PERCENT)) {
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			if (lSymbol.isRightValue()) {
				if (symbol.memoryArea == MemoryArea.STATIC) {
					lSymbol.address = currentStaticIndex;
					staticNameTable.add(new VariableAddress(currentStaticIndex, expToken.word, 0));
					currentStaticIndex++;
					if (currentStaticIndex > MAX_STATIC_SIZE) {
						GrammarError.staticAreaOverflow(token);
					}
				} else {
					lSymbol.address = currentFunction.stackSize;
					currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, expToken.word, 0));
					currentFunction.stackSize++;
				}
				lSymbol.memoryArea = symbol.memoryArea;
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				lSymbol.setLeft();
			} else {
				removeLastLoad();
			}
			String word = token.word;
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			i = expUnary(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			switch (word) {
				case STAR:
					expResultTypeError(lSymbol.calcuMul(this, lSymbol, rSymbol), expToken, oldLSymbol, rSymbol);
					break;
				case SLASH:
					expResultTypeError(lSymbol.calcuDiv(this, lSymbol, rSymbol, expToken), expToken, oldLSymbol, rSymbol);
					break;
				default:
					expResultTypeError(lSymbol.calcuMod(this, lSymbol, rSymbol, expToken), expToken, oldLSymbol, rSymbol);
					break;
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		return i - 1;
	}

	/**
	 * 或
	 */
	private int expOr(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expXor(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		while (token.word.equals(BIT_OR)) {
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			if (lSymbol.isRightValue()) {
				if (symbol.memoryArea == MemoryArea.STATIC) {
					lSymbol.address = currentStaticIndex;
					staticNameTable.add(new VariableAddress(currentStaticIndex, expToken.word, 0));
					currentStaticIndex++;
					if (currentStaticIndex > MAX_STATIC_SIZE) {
						GrammarError.staticAreaOverflow(token);
					}
				} else {
					lSymbol.address = currentFunction.stackSize;
					currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, expToken.word, 0));
					currentFunction.stackSize++;
				}
				lSymbol.memoryArea = symbol.memoryArea;
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				lSymbol.setLeft();
			} else {
				removeLastLoad();
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			i = expXor(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			CalculateResult result = lSymbol.calcuOr(this, lSymbol, rSymbol);
			expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		return i - 1;
	}

	/**
	 * 加法和减法
	 */
	private int expPlusMinus(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expMulDiv(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		while (token.word.equals(PLUS) || token.word.equals(MINUS)) {
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			if (lSymbol.isRightValue()) {
				if (symbol.memoryArea == MemoryArea.STATIC) {
					lSymbol.address = currentStaticIndex;
					staticNameTable.add(new VariableAddress(currentStaticIndex, expToken.word, 0));
					currentStaticIndex++;
					if (currentStaticIndex > MAX_STATIC_SIZE) {
						GrammarError.staticAreaOverflow(token);
					}
				} else {
					lSymbol.address = currentFunction.stackSize;
					currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, expToken.word, 0));
					currentFunction.stackSize++;
				}
				lSymbol.memoryArea = symbol.memoryArea;
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				lSymbol.setLeft();
			} else {
				removeLastLoad();
			}
			String word = token.word;
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			i = expMulDiv(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			if (word.equals(PLUS)) {
				CalculateResult result = lSymbol.calcuPlus(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			} else {
				CalculateResult result = lSymbol.calcuMinus(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		return i - 1;
	}

	/**
	 * 单一的表达式项
	 */
	private int expPrimary(int i, Symbol symbol) {
		CMMToken token = getToken(i);
		CMMToken prefixToken = token;
		i = expPrimaryPrefix(i, symbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		switch (token.word) {
			case L_SMALL_BRACKET:
				grammarRecordInc("function call");
				grammarTreeRecord(prefixToken.word);
				i = statementFunctionCall(i, symbol);
				grammarRecorddec();
				break;
			case L_MIDDLE_BRACKET:
				removeLastLoad();
				i = expArrayIndex(i, symbol);
				break;
			case PRE_PROCESS_ADDRESS:
				if (!symbol.isConstValue()) {
					grammarRecordInc("pre_processor_address");
					symbol.setConst(symbol.address, MemoryArea.INT_CON);
					loadConstSymbol(symbol);
					grammarTreeRecord(token.word);
					grammarRecorddec();
				} else {
					GrammarError.constValueHasNoAddress(token);
				}
				break;
			case PRE_PROCESS_LENGTH:
				if (symbol.variable.length > 0) {
					grammarRecordInc("pre_processor_length");
					symbol.setConst(symbol.variable.length, MemoryArea.INT_CON);
					loadConstSymbol(symbol);
					grammarTreeRecord(token.word);
					grammarRecorddec();
				} else {
					GrammarError.onlyArrayUseLength(token);
				}
				break;
			default:
				if (symbol.variable.length > 0) {
					// 数组转换为int
					GrammarError.useArrayAsIntConst(prefixToken);
					symbol.setConst(symbol.address, MemoryArea.INT_CON);
				} else if (!symbol.variable.isVariable()) {
					if (prefixToken.type == TokenType.IDENTIFIER) {
						GrammarError.undefinedIdentifier(prefixToken);
						symbol.variable = new CMMVariable(VariableType.INT, 0);
						symbolTable.add(symbol);
						// 未定义的标识符,假设为int
					} else {
						GrammarError.unknownExpression(prefixToken);
						// 未能解析的表达式
					}
				}
				return i - 1;
		}
		return i;
	}

	/**
	 * 单一的表达式项前缀
	 */
	private int expPrimaryPrefix(int i, Symbol symbol) {
		CMMToken token = getToken(i);
		switch (token.type) {
			case DECIMAL_NUMBER: {
				grammarTreeRecord(token.word);
				symbol.setConst(Integer.parseInt(token.word), MemoryArea.INT_CON);
				loadConstSymbol(symbol);
				break;
			}
			case REAL_NUMBER: {
				grammarTreeRecord(token.word);
				symbol.setConst(Double.parseDouble(token.word), MemoryArea.REAL_CON);
				loadConstSymbol(symbol);
				break;
			}
			case IDENTIFIER: {
				int maxScopeDeepth = -1;
				int index = -1;
				ArrayList<Symbol> symbols = symbolTable.symbols;
				for (int j = 0; j < symbols.size(); j++) {
					Symbol s = symbols.get(j);
					if (s.identifier.equals(token.word) && s.variable.isVariable()) {
						if (s.scopeDepth > maxScopeDeepth) {
							maxScopeDeepth = s.scopeDepth;
							index = j;
						}
					}
				}
				if (index == -1) {
					// GrammarError.undefinedIdentifier(token);
					// this.symbolTable.add(new Symbol(token.word, -1, MemoryArea.LOCAL, currentScopeDepth));
					return i;
				} else {
					grammarTreeRecord(token.word);
					symbol.clone(symbolTable.get(index));
					if (symbol.variable.length > 0) {
						symbol.value = symbol.address;
						InternalCode load = new InternalCode(CMMOperator.load, symbol.value, MemoryArea.INT_CON, currentLine);
						internalCodes.add(load);
					} else {
						loadLeftSymbol(symbol);
					}
				}
				break;
			}
			default:
				switch (token.word) {
					case TRUE: {
						grammarTreeRecord(token.word);
						symbol.setConst(true, MemoryArea.BOOL_CON);
						loadConstSymbol(symbol);
						break;
					}
					case FALSE: {
						grammarTreeRecord(token.word);
						symbol.setConst(false, MemoryArea.BOOL_CON);
						loadConstSymbol(symbol);
						break;
					}
					case L_SMALL_BRACKET: {
						CMMToken L_BRACKET = token;
						grammarTreeRecord(token.word);
						i = tokenSkip(i);
						if (i >= length) {
							GrammarError.unexpectedEndOfFile(this, i);
							return i;
						}
						token = getToken(i);
						if (!token.startOfExpression()) {
							GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
									new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
							ErrorReturn result = GrammarError.skipTo(this, i,
									new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
									new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
									new TokenType[] {},
									new String[] {},
									new TokenType[] {},
									new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
							i = result.i;
							if (result.type == ErrorReturnType.END_TYPE) {
								return i;
							}
						}
						grammarRecordInc("expression");
						i = expression(i, symbol);
						grammarRecorddec();
						i = tokenSkip(i);
						if (i >= length) {
							GrammarError.unexpectedEndOfFile(this, i);
							return i;
						}
						token = getToken(i);
						if (!token.word.equals(R_SMALL_BRACKET)) {
							GrammarError.unmatchedSymbol(L_BRACKET);
							GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_SMALL_BRACKET });
							ErrorReturn result = GrammarError.skipTo(this, i,
									new TokenType[] {},
									new String[] { R_SMALL_BRACKET },
									new TokenType[] {},
									new String[] {},
									new TokenType[] {},
									new String[] { SEMICOLON, R_LARGE_BRACKET });
							i = result.i;
							if (result.type == ErrorReturnType.END_TYPE) {
								return i;
							} else {
								token = getToken(i);
							}
						}
						grammarTreeRecord(token.word);
						break;
					}
					default:
						break;
				}
				break;
		}
		return i;
	}

	/**
	 * 表达式计算
	 */
	private int expression(int i, Symbol symbol) {
		CMMToken token = getToken(i);
		if (!token.startOfExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			}
		}
		Symbol lSymbol = new Symbol(symbol);
		i = expLogicOr(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		token = getToken(i);
		switch (token.word) {
			case ASSIGN:
			case PLUSASSIGN:
			case MINUSASSIGN:
			case STARASSIGN:
			case SLASHASSIGN:
			case ANDASSIGN:
			case ORASSIGN:
			case XORASSIGN:
			case PERCENTASSIGN:
			case LSHIFTASSIGN:
			case RSHIFTASSIGN:
				grammarTreeRecord(token.word);
				if (!lSymbol.isLeftValue()) {
					// 只有左值才能赋值
					GrammarError.onlyLeftValueCanAssign(token);
				}
				removeLastLoad();
				CMMToken expToken = token;
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				token = getToken(i);
				if (!token.startOfExpression()) {
					GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
							new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
					ErrorReturn result = GrammarError.skipTo(this, i,
							new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
							new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
							new TokenType[] {},
							new String[] {},
							new TokenType[] {},
							new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
					i = result.i;
					if (result.type == ErrorReturnType.END_TYPE) {
						return i - 1;
					}
				}
				Symbol rSymbol = new Symbol(symbol);
				grammarRecordInc("expression");
				i = expression(i, rSymbol);
				grammarRecorddec();
				statementAssignOps(expToken, lSymbol, rSymbol, symbol);
				CalculateResult result = symbol.calcuAssign(this, lSymbol, symbol);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			default:
				// 表达式已经结束了
				symbol.clone(lSymbol);
				return i - 1;
		}
		return i;
	}

	/**
	 * 表达式运算结果类型错误或者不能进行运算
	 */
	private void expResultTypeError(CalculateResult result, CMMToken expToken, Symbol symbol) {
		if (result == CalculateResult.WARNING) {
			GrammarError.variableTypeWarningOnOp(expToken, expToken.word, symbol.getType());
		} else if (result == CalculateResult.ERROR) {
			GrammarError.canNotDoOpOnTheTypes(expToken, expToken.word, symbol.getType());
		}
	}

	/**
	 * 表达式运算结果类型错误或者不能进行运算
	 */
	private void expResultTypeError(CalculateResult result, CMMToken expToken, Symbol lSymbol, Symbol rSymbol) {
		if (result == CalculateResult.WARNING) {
			GrammarError.variableTypeWarningOnOp(expToken, expToken.word, lSymbol.getType(), rSymbol.getType());
		} else if (result == CalculateResult.ERROR) {
			GrammarError.canNotDoOpOnTheTypes(expToken, expToken.word, lSymbol.getType(), rSymbol.getType());
		}
	}

	/**
	 * 左移和右移
	 */
	private int expShift(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expPlusMinus(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		while (token.word.equals(LSHIFT) || token.word.equals(RSHIFT)) {
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			if (lSymbol.isRightValue()) {
				if (symbol.memoryArea == MemoryArea.STATIC) {
					lSymbol.address = currentStaticIndex;
					staticNameTable.add(new VariableAddress(currentStaticIndex, expToken.word, 0));
					currentStaticIndex++;
					if (currentStaticIndex > MAX_STATIC_SIZE) {
						GrammarError.staticAreaOverflow(token);
					}
				} else {
					lSymbol.address = currentFunction.stackSize;
					currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, expToken.word, 0));
					currentFunction.stackSize++;
				}
				lSymbol.memoryArea = symbol.memoryArea;
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				lSymbol.setLeft();
			} else {
				removeLastLoad();
			}
			String word = token.word;
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			i = expPlusMinus(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			if (word.equals(LSHIFT)) {
				CalculateResult result = lSymbol.calcuLsh(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			} else {
				CalculateResult result = lSymbol.calcuRsh(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		return i - 1;
	}

	/**
	 * 一元操作符
	 */
	private int expUnary(int i, Symbol symbol) {
		CMMToken token = getToken(i);
		switch (token.word) {
			case PLUS:
				grammarTreeRecord(token.word);
				break;
			case MINUS: {
				grammarTreeRecord(token.word);
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				Symbol rSymbol = new Symbol(symbol);
				i = expUnary(i, rSymbol);
				CalculateResult result = symbol.calcuOpps(this, rSymbol);
				expResultTypeError(result, token, rSymbol);
				break;
			}
			case LOGIC_NOT: {
				grammarTreeRecord(token.word);
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				Symbol rSymbol = new Symbol(symbol);
				i = expUnary(i, rSymbol);
				CalculateResult result = symbol.calcuNot(this, rSymbol);
				expResultTypeError(result, token, rSymbol);
				break;
			}
			case BIT_NOT: {
				grammarTreeRecord(token.word);
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				Symbol rSymbol = new Symbol(symbol);
				i = expUnary(i, rSymbol);
				CalculateResult result = symbol.calcuTilde(this, rSymbol);
				expResultTypeError(result, token, rSymbol);
				break;
			}
			default:
				i = expCrement(i, symbol);
				break;
		}
		return i;
	}

	/**
	 * 异或
	 */
	private int expXor(int i, Symbol symbol) {
		Symbol lSymbol = new Symbol(symbol);
		i = expAnd(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			symbol.clone(lSymbol);
			return i;
		}
		CMMToken token = getToken(i);
		while (token.word.equals(BIT_XOR)) {
			CMMToken expToken = token;
			grammarTreeRecord(token.word);
			if (lSymbol.isRightValue()) {
				if (symbol.memoryArea == MemoryArea.STATIC) {
					lSymbol.address = currentStaticIndex;
					staticNameTable.add(new VariableAddress(currentStaticIndex, expToken.word, 0));
					currentStaticIndex++;
					if (currentStaticIndex > MAX_STATIC_SIZE) {
						GrammarError.staticAreaOverflow(token);
					}
				} else {
					lSymbol.address = currentFunction.stackSize;
					currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, expToken.word, 0));
					currentFunction.stackSize++;
				}
				lSymbol.memoryArea = symbol.memoryArea;
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				lSymbol.setLeft();
			} else {
				removeLastLoad();
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			Symbol rSymbol = new Symbol(symbol);
			i = expAnd(i, rSymbol);
			Symbol oldLSymbol = new Symbol(lSymbol);
			CalculateResult result = lSymbol.calcuXor(this, lSymbol, rSymbol);
			expResultTypeError(result, expToken, oldLSymbol, rSymbol);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		symbol.clone(lSymbol);
		return i - 1;
	}

	/**
	 * 在符号表中查找函数
	 */
	private Symbol findFunction(Symbol s) {
		for (Symbol f : symbolTable.symbols) {
			if ((f.variable instanceof Function) && f.identifier.equals(s.identifier)) {
				Function function = (Function) f.variable;
				if (function.equals(s.variable)) {
					Symbol ret = new Symbol("");
					ret.clone(f);
					return ret;
				}
			}
		}
		return null;
	}

	/**
	 * 返填break语句的地址
	 */
	private void generateBreakAddress(int start, int end) {
		for (int i = start; i < end; i++) {
			InternalCode code = internalCodes.get(i);
			if ((code.op == CMMOperator.jmp) && (code.extra instanceof CMMToken) && code.addrOrValue.equals("break")) {
				((CMMToken) code.extra).grammarErrors.clear();
				code.extra = null;
				code.addrOrValue = end;
			}
		}
	}

	/**
	 * 生成默认的返回语句
	 */
	private void generateDefaultReturnCode() {
		if (internalCodes.get(internalCodes.size() - 1).op == CMMOperator.ret) {
			return;
		}
		switch (currentFunction.getReturnType()) {
			case VOID:
				break;
			case INT:
				if (currentFunction.returnVariable.length > 1) {
					InternalCode loadIntArray = new InternalCode(CMMOperator.load, -1, MemoryArea.INT_CON, currentLine);
					internalCodes.add(loadIntArray);
				} else {
					InternalCode loadInt = new InternalCode(CMMOperator.load, 0, MemoryArea.INT_CON, currentLine);
					internalCodes.add(loadInt);
				}
				break;
			case REAL:
				if (currentFunction.returnVariable.length > 1) {
					InternalCode loadRealArray = new InternalCode(CMMOperator.load, -1, MemoryArea.INT_CON, currentLine);
					internalCodes.add(loadRealArray);
				} else {
					InternalCode loadReal = new InternalCode(CMMOperator.load, 0.0, MemoryArea.REAL_CON, currentLine);
					internalCodes.add(loadReal);
				}
				break;
			case BOOLEAN:
				if (currentFunction.returnVariable.length > 1) {
					InternalCode loadBoolArray = new InternalCode(CMMOperator.load, -1, MemoryArea.INT_CON, currentLine);
					internalCodes.add(loadBoolArray);
				} else {
					InternalCode loadBool = new InternalCode(CMMOperator.load, false, MemoryArea.BOOL_CON, currentLine);
					internalCodes.add(loadBool);
				}
				break;
			default:
				break;

		}
		InternalCode retCode = new InternalCode(CMMOperator.ret, -1, MemoryArea.ADDRESS, currentLine);
		internalCodes.add(retCode);
	}

	/**
	 * 构造返回类型
	 */
	private ReturnVariable generateReturnVariable(CMMToken token) {
		switch (token.word) {
			case VOID:
				return new ReturnVariable(ReturnType.VOID);
			case INT:
				return new ReturnVariable(ReturnType.INT);
			case REAL:
				return new ReturnVariable(ReturnType.REAL);
			case BOOLEAN:
				return new ReturnVariable(ReturnType.BOOLEAN);
			default:
				return new ReturnVariable(ReturnType.UNKNOWN);
		}
	}

	/**
	 * 构造扩展数据类型
	 */
	private CMMVariable generateVariable(CMMToken token) {
		switch (token.word) {
			case INT:
				return new CMMVariable(VariableType.INT);
			case REAL:
				return new CMMVariable(VariableType.REAL);
			case BOOLEAN:
				return new CMMVariable(VariableType.BOOLEAN);
			default:
				return new CMMVariable(VariableType.UNKNOWN);
		}
	}

	/**
	 * 获取函数返回数据类型的长度(如果是数组的话)
	 */
	private int generrateFunctionReturnVariableLength(int i, ReturnVariable rt) {
		CMMToken token = getToken(i);
		length: {
			if (token.type != TokenType.DECIMAL_NUMBER) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.DECIMAL_NUMBER }, new String[] {});
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.DECIMAL_NUMBER },
						new String[] {},
						new TokenType[] {},
						new String[] { R_MIDDLE_BRACKET },
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					rt.length = 0;
					break length;
				} else {
					token = getToken(i);
				}
			}
			if (rt.type == ReturnType.VOID) {
				GrammarError.voidTypeCanNotBeArray(token);
				rt.length = 0;// void类型不能为数组,强行将长度变成0
			}
			rt.length = Integer.parseInt(token.word);
			grammarTreeRecord(token.word);
			if (rt.length < 1) {
				GrammarError.arrayLengthNotLargerThanZero(token);
			}
		}
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(R_MIDDLE_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_MIDDLE_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { R_MIDDLE_BRACKET },
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * 计算出数组变量的定义时长度
	 */
	private int generrateVariablelength(int i, CMMVariable vt) {
		CMMToken token = getToken(i);
		length: {
			if (token.type != TokenType.DECIMAL_NUMBER) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.DECIMAL_NUMBER }, new String[] {});
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.DECIMAL_NUMBER },
						new String[] {},
						new TokenType[] {},
						new String[] { R_MIDDLE_BRACKET },
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					vt.length = 0;
					break length;
				} else {
					token = getToken(i);
				}
			}
			vt.length = Integer.parseInt(token.word);
			vt.left = false;
			if (vt.length < 1) {
				GrammarError.arrayLengthNotLargerThanZero(token);
			}
			grammarTreeRecord(token.word);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(R_MIDDLE_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_MIDDLE_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { R_MIDDLE_BRACKET },
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * 语法树向上一层
	 */
	private void grammarRecorddec() {
		if (recoed) {
			grammarTree = grammarTree.parent;
		}
	}

	/**
	 * 语法树向下一层
	 */
	private void grammarRecordInc(String str) {
		if (recoed) {
			if ((grammarTree.parent != null) && (((grammarTree.value != null) && !grammarTree.value.equals("")) || (grammarTree.children.size() > 0))) {
				grammarTree.parent.addChild(treeNodeLSymbol + str + treeNodeRSymbol);
				grammarTree = grammarTree.parent.lastChild();
			} else {
				grammarTree.value = treeNodeLSymbol + str + treeNodeRSymbol;
			}
			grammarTree.addChild();
			grammarTree = grammarTree.lastChild();
		}
	}

	/**
	 * 作用域深度减一
	 */
	private void scopeDeepthDec() {
		currentScopeDepth--;
		if (recoed) {
			grammarTree = grammarTree.parent;
		}
	}

	/**
	 * 作用域深度加一
	 */
	private void scopeDeepthInc(String str) {
		currentScopeDepth++;
		grammarRecordInc(str);
	}

	/**
	 * 语句
	 */
	private int statement(int i) {
		CMMToken token = getToken(i);
		if (currentFunction == null) {
			GrammarError.statementOutOfFunction(token);
			currentFunction = new Function(new ReturnVariable(ReturnType.VOID));// 假设存在一个空函数声明
		}
		switch (token.type) {
			case DECIMAL_NUMBER:
			case REAL_NUMBER:
			case IDENTIFIER:
				grammarRecordInc("statement expression");
				i = statementExpression(i);
				grammarRecorddec();
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				token = getToken(i);
				if (!token.word.equals(SEMICOLON)) {
					GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
					ErrorReturn result = GrammarError.skipTo(this, i,
							new TokenType[] {},
							new String[] { SEMICOLON },
							new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
							new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE,
									READ },
							new TokenType[] {},
							new String[] { R_LARGE_BRACKET });
					i = result.i;
					if (result.type == ErrorReturnType.EXPECTED_TYPE) {
						token = getToken(i);
					} else if (result.type == ErrorReturnType.AFTER_TYPE) {
						return i;
					} else {
						return i - 1;
					}
				}
				grammarTreeRecord(token.word);
				break;
			default:
				switch (token.word) {
					case L_LARGE_BRACKET:
						i = statementBlock(i);
						break;
					case SEMICOLON:
						grammarRecordInc("empty statement");
						i = statementEmpty(i);
						grammarRecorddec();
						break;
					case TRUE:
					case FALSE:
					case L_SMALL_BRACKET:
					case INCREMENT:
					case DECREMENT:
						grammarRecordInc("statement expression");
						i = statementExpression(i);
						grammarRecorddec();
						i = tokenSkip(i);
						if (i >= length) {
							GrammarError.unexpectedEndOfFile(this, i);
							return i;
						}
						token = getToken(i);
						if (!token.word.equals(SEMICOLON)) {
							GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
							ErrorReturn result = GrammarError.skipTo(this, i,
									new TokenType[] {},
									new String[] { SEMICOLON },
									new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
									new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN,
											WRITE, READ },
									new TokenType[] {},
									new String[] { R_LARGE_BRACKET });
							i = result.i;
							if (result.type == ErrorReturnType.EXPECTED_TYPE) {
								token = getToken(i);
							} else if (result.type == ErrorReturnType.AFTER_TYPE) {
								return i;
							} else {
								return i - 1;
							}
						}
						grammarTreeRecord(token.word);
						break;
					case IF:
						grammarRecordInc("if statement");
						i = statementIf(i);
						grammarRecorddec();
						break;
					case WHILE:
						grammarRecordInc("while statement");
						i = statementWhile(i);
						grammarRecorddec();
						break;
					case FOR:
						grammarRecordInc("for statement");
						i = statementFor(i);
						grammarRecorddec();
						break;
					case BREAK:
						grammarRecordInc("break statement");
						i = statementBreak(i);
						grammarRecorddec();
						break;
					case CONTINUE:
						grammarRecordInc("continue statement");
						i = statementContinue(i);
						grammarRecorddec();
						break;
					case RETURN:
						grammarRecordInc("return statement");
						i = statementReturn(i);
						grammarRecorddec();
						break;
					case WRITE:
						grammarRecordInc("write statement");
						i = statementWrite(i);
						grammarRecorddec();
						break;
					case READ:
						grammarRecordInc("read statement");
						i = statementRead(i);
						grammarRecorddec();
						break;
					default:
						// GrammarError.redundantOrUnknownToken(this, token);
						break;
				}
				break;
		}
		return i;
	}

	/**
	 * 解析实参
	 */
	private int statementArguments(int i, String identifier, Function f, Symbol v) {
		ArrayList<CMMVariable> parameters = f.parametersList;
		CMMToken token = getToken(i);
		int arguAddress = 2;
		for (CMMVariable p : parameters) {
			if (!token.startOfExpression()) {
				if (token.word.equals(R_SMALL_BRACKET)) {
					return i - 1;
				}
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i - 1;
				}
			}
			Symbol symbol;
			if (v.memoryArea == MemoryArea.STATIC) {
				symbol = new Symbol(identifier, currentStaticIndex, MemoryArea.STATIC, currentScopeDepth);
			} else {
				symbol = new Symbol(identifier, currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
			}
			grammarRecordInc("expression");
			i = expression(i, symbol);
			grammarRecorddec();
			p = parameters.get(0);
			if (p.type != symbol.getType()) {
				GrammarError.variableTypeNotMatch(token, symbol.getType(), p.type);
				InternalCode cvt = new InternalCode(CMMOperator.cvt, p.type, currentLine);
				internalCodes.add(cvt);
			}
			InternalCode mov = new InternalCode(CMMOperator.mov, arguAddress, MemoryArea.ARGUMENT, currentLine);
			internalCodes.add(mov);
			arguAddress++;
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.word.equals(COMMA)) {
				if (token.word.equals(R_SMALL_BRACKET)) {
					break;
				}
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { COMMA });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] {},
						new String[] { COMMA },
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i - 1;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					i = tokenSkip(i);
					continue;
				} else {
					token = getToken(i);
				}
			}
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		return i - 1;
	}

	/**
	 * 赋值语句
	 */
	private void statementAssignOps(CMMToken expToken, Symbol lSymbol, Symbol rSymbol, Symbol symbol) {
		switch (expToken.word) {
			case ASSIGN:
				symbol.clone(rSymbol);
				break;
			case PLUSASSIGN: {
				CalculateResult result = symbol.calcuPlus(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			case MINUSASSIGN: {
				CalculateResult result = symbol.calcuMinus(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			case STARASSIGN: {
				CalculateResult result = symbol.calcuMul(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			case SLASHASSIGN: {
				CalculateResult result = symbol.calcuDiv(this, lSymbol, rSymbol, expToken);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			case ANDASSIGN: {
				CalculateResult result = symbol.calcuAnd(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			case ORASSIGN: {
				CalculateResult result = symbol.calcuOr(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			case XORASSIGN: {
				CalculateResult result = symbol.calcuXor(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			case PERCENTASSIGN: {
				CalculateResult result = symbol.calcuMod(this, lSymbol, rSymbol, expToken);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			case LSHIFTASSIGN: {
				CalculateResult result = symbol.calcuLsh(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			case RSHIFTASSIGN: {
				CalculateResult result = symbol.calcuRsh(this, lSymbol, rSymbol);
				expResultTypeError(result, expToken, lSymbol, rSymbol);
				break;
			}
			default:
				break;
		}
	}

	/**
	 * 解析块
	 */
	private int statementBlock(int i) {
		scopeDeepthInc("block");// 作用域深度+1
		CMMToken token = getToken(i);
		if (token.word.equals(L_LARGE_BRACKET)) {
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		while ((i < length) && !token.word.equals(R_LARGE_BRACKET)) {
			i = statementInBlock(i);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		if (!token.word.equals(R_LARGE_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_LARGE_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] {});
			i = result.i;
			if (result.type == ErrorReturnType.EXPECTED_TYPE) {
				token = getToken(i);
			} else {
				return i;
			}
		}
		grammarTreeRecord(token.word);
		scopeDeepthDec();// 作用域深度-1
		clearSymbolTable(currentScopeDepth);
		return i;
	}

	/**
	 * break语句
	 */
	private int statementBreak(int i) {
		CMMToken token = getToken(i);
		grammarTreeRecord(token.word);
		if (cycle_start == -1) {
			GrammarError.noCycleNoBreak(token);
			return i;
		}
		InternalCode breakCode = new InternalCode(CMMOperator.jmp, "break", MemoryArea.ADDRESS, currentLine);
		internalCodes.add(breakCode);
		breakCode.extra = token;
		GrammarError.breakJmpNowhere(token);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE,
							READ },
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * continue语句
	 */
	private int statementContinue(int i) {
		CMMToken token = getToken(i);
		grammarTreeRecord(token.word);
		if (cycle_start == -1) {
			GrammarError.noCycleNoContinue(token);
			return i;
		}
		InternalCode continueCode = new InternalCode(CMMOperator.jmp, cycle_start, MemoryArea.ADDRESS, currentLine);
		internalCodes.add(continueCode);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE,
							READ },
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * 函数声明语句
	 */
	private int statementDeclarationFunction(int i) {
		Symbol symbol;
		CMMToken token = getToken(i);
		ReturnVariable rt = generateReturnVariable(token);
		if (rt.type == ReturnType.UNKNOWN) {
			GrammarError.unknownReturnType(token);
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { VOID, INT, BOOLEAN, REAL },
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				rt.type = ReturnType.VOID;
				grammarTreeRecord("<unknown type>");
			} else {
				token = getToken(i);
				rt = generateReturnVariable(token);
				grammarTreeRecord(token.word);
			}
		} else {
			grammarTreeRecord(token.word);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (token.word.equals(L_MIDDLE_BRACKET)) {
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			i = generrateFunctionReturnVariableLength(i, rt);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		Function f = new Function(rt);
		if (token.type != TokenType.IDENTIFIER) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { L_SMALL_BRACKET },
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				token = new CMMToken("<unknown identifier>", TokenType.IDENTIFIER, token.sourceLine, token.offset);
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		symbol = new Symbol(token.word, currentScopeDepth);// 默认作用域深度
		symbol.variable = f;
		symbol.address = -1;// 地址待回填
		f.name = symbol.identifier;
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		scopeDeepthInc("parameters");
		i = statementParameters(i, f);
		scopeDeepthDec();
		clearSymbolTable(currentScopeDepth);
		if (!symbolTable.contains(symbol)) {
			symbolTable.add(symbol);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE,
							READ },
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.EXPECTED_TYPE) {
				token = getToken(i);
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				return i - 1;
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * 局部变量声明
	 */
	private int statementDeclarationLocalVariable(int i) {
		Symbol symbol;
		CMMToken token = getToken(i);
		CMMVariable vt = generateVariable(token);
		if (vt.type == VariableType.UNKNOWN) {
			GrammarError.unknownVariableType(token);
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { INT, BOOLEAN, REAL },
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				vt.type = VariableType.INT;
				grammarTreeRecord("<unknown type>");
			} else {
				token = getToken(i);
				vt = generateVariable(token);
				grammarTreeRecord(token.word);
			}
		} else {
			grammarTreeRecord(token.word);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (token.word.equals(L_MIDDLE_BRACKET)) {
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			i = generrateVariablelength(i, vt);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		if (token.type != TokenType.IDENTIFIER) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER }, new String[] {});
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { ASSIGN, COMMA, SEMICOLON },
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				token = new CMMToken("<unknown identifier>", TokenType.IDENTIFIER, token.sourceLine, token.offset);// 假设存在一个标识符
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		symbol = new Symbol(token.word, currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
		symbol.variable = vt;
		currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, token.word, vt.length));
		currentFunction.stackSize += vt.length > 0 ? vt.length : 1;
		if (symbolTable.contains(symbol)) {
			GrammarError.redefineError(token);
		} else {
			symbolTable.add(symbol);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(ASSIGN)) {
			initSymbol(symbol);
		} else {
			CMMToken assignToken = token;
			grammarTreeRecord(token.word);
			if (symbol.variable.length > 0) {
				GrammarError.arrayCanNotBeAssign(assignToken);
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			// token = getToken(i);
			Symbol s = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
			grammarRecordInc("expression");
			i = expression(i, s);
			grammarRecorddec();
			if (symbol.getType() != s.getType()) {
				GrammarError.variableTypeNotMatch(assignToken, s.getType(), symbol.getType());
				s.addCvtCode(this, symbol.getType());
			}
			InternalCode mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
			internalCodes.add(mov);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		while (token.word.equals(COMMA)) {
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (token.type != TokenType.IDENTIFIER) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER }, new String[] {});
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER },
						new String[] {},
						new TokenType[] {},
						new String[] { ASSIGN, COMMA, SEMICOLON },
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					token = new CMMToken("<unknown identifier>", TokenType.IDENTIFIER, token.sourceLine, token.offset);// 假设存在一个标识符
				} else {
					token = getToken(i);
				}
			}
			grammarTreeRecord(token.word);
			symbol = new Symbol(token.word, currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
			symbol.variable = vt;
			currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, token.word, vt.length));
			currentFunction.stackSize += vt.length > 0 ? vt.length : 1;
			if (symbolTable.contains(symbol)) {
				GrammarError.redefineError(token);
			} else {
				symbolTable.add(symbol);
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.word.equals(ASSIGN)) {
				initSymbol(symbol);
			} else {
				CMMToken assignToken = token;
				grammarTreeRecord(token.word);
				if (symbol.variable.length > 0) {
					GrammarError.arrayCanNotBeAssign(assignToken);
				}
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				// token = getToken(i);
				Symbol s = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
				i = expression(i, s);
				if (symbol.getType() != s.getType()) {
					GrammarError.variableTypeNotMatch(assignToken, s.getType(), symbol.getType());
					s.addCvtCode(this, symbol.getType());
				}
				InternalCode mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
				internalCodes.add(mov);
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				token = getToken(i);
			}
		}
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE,
							READ },
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * 静态变量声明
	 */
	private int statementDeclarationStaticVariable(int i) {
		Symbol symbol;
		CMMToken token = getToken(i);
		CMMVariable vt = generateVariable(token);
		if (vt.type == VariableType.UNKNOWN) {
			GrammarError.unknownVariableType(token);
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { INT, BOOLEAN, REAL },
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				vt.type = VariableType.INT;
				grammarTreeRecord("<unknown type>");
			} else {
				token = getToken(i);
				vt = generateVariable(token);
				grammarTreeRecord(token.word);
			}
		} else {
			grammarTreeRecord(token.word);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (token.word.equals(L_MIDDLE_BRACKET)) {
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			i = generrateVariablelength(i, vt);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		if (token.type != TokenType.IDENTIFIER) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER }, new String[] {});
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { ASSIGN, COMMA, SEMICOLON },
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				token = new CMMToken("<unknown identifier>", TokenType.IDENTIFIER, token.sourceLine, token.offset);// 假设存在一个标识符
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		symbol = new Symbol(token.word, currentStaticIndex, MemoryArea.STATIC, currentScopeDepth);
		symbol.variable = vt;
		staticNameTable.add(new VariableAddress(currentStaticIndex, token.word, vt.length));
		currentStaticIndex += vt.length > 0 ? vt.length : 1;
		if (currentStaticIndex > MAX_STATIC_SIZE) {
			GrammarError.staticAreaOverflow(token);
		}
		if (symbolTable.contains(symbol)) {
			GrammarError.redefineError(token);
		} else {
			symbolTable.add(symbol);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(ASSIGN)) {
			initSymbol(symbol);
		} else {
			CMMToken assignToken = token;
			grammarTreeRecord(token.word);
			if (symbol.variable.length > 0) {
				GrammarError.arrayCanNotBeAssign(assignToken);
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			Symbol s = new Symbol("", currentStaticIndex, MemoryArea.STATIC, currentScopeDepth);
			grammarRecordInc("expression");
			i = expression(i, s);
			grammarRecorddec();
			if (symbol.getType() != s.getType()) {
				GrammarError.variableTypeNotMatch(assignToken, s.getType(), symbol.getType());
				s.addCvtCode(this, symbol.getType());
			}
			InternalCode mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
			internalCodes.add(mov);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		while (token.word.equals(COMMA)) {
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (token.type != TokenType.IDENTIFIER) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER }, new String[] {});
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER },
						new String[] {},
						new TokenType[] {},
						new String[] { ASSIGN, COMMA, SEMICOLON },
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					token = new CMMToken("<unknown identifier>", TokenType.IDENTIFIER, token.sourceLine, token.offset);// 假设存在一个标识符
				} else {
					token = getToken(i);
				}
			}
			grammarTreeRecord(token.word);
			symbol = new Symbol(token.word, currentStaticIndex, MemoryArea.STATIC, currentScopeDepth);
			symbol.variable = vt;
			staticNameTable.add(new VariableAddress(currentStaticIndex, token.word, vt.length));
			currentStaticIndex += vt.length > 0 ? vt.length : 1;
			if (currentStaticIndex > MAX_STATIC_SIZE) {
				GrammarError.staticAreaOverflow(token);
			}
			if (symbolTable.contains(symbol)) {
				GrammarError.redefineError(token);
			} else {
				symbolTable.add(symbol);
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.word.equals(ASSIGN)) {
				initSymbol(symbol);
			} else {
				CMMToken assignToken = token;
				grammarTreeRecord(token.word);
				if (symbol.variable.length > 0) {
					GrammarError.arrayCanNotBeAssign(assignToken);
				}
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				// token = getToken(i);
				Symbol s = new Symbol("", currentStaticIndex, MemoryArea.STATIC, currentScopeDepth);
				grammarRecordInc("expression");
				i = expression(i, s);
				grammarRecorddec();
				if (symbol.getType() != s.getType()) {
					GrammarError.variableTypeNotMatch(assignToken, s.getType(), symbol.getType());
					s.addCvtCode(this, symbol.getType());
				}
				InternalCode mov = new InternalCode(CMMOperator.mov, symbol.address, symbol.memoryArea, currentLine);
				internalCodes.add(mov);
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				token = getToken(i);
			}
		}
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] {},
					new String[] { VOID, INT, BOOLEAN, REAL },
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * 空语句
	 */
	private int statementEmpty(int i) {
		grammarTreeRecord(SEMICOLON);
		return i;
	}

	/**
	 * 表达式赋值语句
	 */
	private int statementExpression(int i) {
		CMMToken token = getToken(i);
		switch (token.word) {
			case INCREMENT: {
				Symbol symbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
				i = expIncrementBefore(i, symbol);
				return i;
			}
			case DECREMENT: {
				Symbol symbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
				i = expDecrementBefore(i, symbol);
				return i;
			}
			default:
				break;
		}
		Symbol lSymbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
		i = expPrimary(i, lSymbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		switch (token.word) {
			case SEMICOLON:
				removeLastLoad();
				// StatementExpression执行的是类似于函数调用等单指令
				return i - 1;
			case INCREMENT: {
				grammarTreeRecord(token.word);
				if (!lSymbol.variable.left) {
					GrammarError.onlyLeftValueCanCrement(token);
					return i;
				}
				if (lSymbol.getType() == VariableType.BOOLEAN) {
					GrammarError.variableTypeNotMatch(token, VariableType.BOOLEAN, VariableType.INT, VariableType.REAL);
					lSymbol.addCvtCode(this, VariableType.INT);
					lSymbol.variable.type = VariableType.INT;
				}
				InternalCode inc = new InternalCode(CMMOperator.inc, currentLine);
				internalCodes.add(inc);
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				InternalCode dec = new InternalCode(CMMOperator.dec, currentLine);
				internalCodes.add(dec);
				lSymbol.setRight();
				return i;
			}
			case DECREMENT: {
				grammarTreeRecord(token.word);
				if (!lSymbol.variable.left) {
					GrammarError.onlyLeftValueCanCrement(token);
					return i;
				}
				if (lSymbol.getType() == VariableType.BOOLEAN) {
					GrammarError.variableTypeNotMatch(token, VariableType.BOOLEAN, VariableType.INT, VariableType.REAL);
					lSymbol.addCvtCode(this, VariableType.INT);
					lSymbol.variable.type = VariableType.INT;
				}
				InternalCode dec = new InternalCode(CMMOperator.dec, currentLine);
				internalCodes.add(dec);
				InternalCode mov = new InternalCode(CMMOperator.mov, lSymbol.address, lSymbol.memoryArea, currentLine);
				internalCodes.add(mov);
				InternalCode inc = new InternalCode(CMMOperator.inc, currentLine);
				internalCodes.add(inc);
				lSymbol.setRight();
				return i;
			}
			case ASSIGN:
			case PLUSASSIGN:
			case MINUSASSIGN:
			case STARASSIGN:
			case SLASHASSIGN:
			case ANDASSIGN:
			case ORASSIGN:
			case XORASSIGN:
			case PERCENTASSIGN:
			case LSHIFTASSIGN:
			case RSHIFTASSIGN:
				grammarTreeRecord(token.word);
				if (!lSymbol.variable.left) {
					GrammarError.onlyLeftValueCanAssign(token);
					// 只有左值才能赋值
					// return i;
				}
				removeLastLoad();
				CMMToken expToken = token;
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				token = getToken(i);
				if (!token.startOfExpression()) {
					GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
							new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
					ErrorReturn result = GrammarError.skipTo(this, i,
							new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
							new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
							new TokenType[] {},
							new String[] {},
							new TokenType[] {},
							new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
					i = result.i;
					if (result.type == ErrorReturnType.END_TYPE) {
						return i - 1;
					}
				}
				Symbol rSymbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
				grammarRecordInc("expression");
				i = expression(i, rSymbol);
				grammarRecorddec();
				Symbol symbol = new Symbol(lSymbol);
				statementAssignOps(expToken, lSymbol, rSymbol, symbol);
				Symbol old = new Symbol(symbol);
				CalculateResult result = symbol.calcuAssign(this, lSymbol, symbol);
				expResultTypeError(result, expToken, lSymbol, old);
				break;
			default:
				// 单独的表达式不能成为语句
				return i - 1;
		}
		return i;
	}

	/**
	 * 多条表达式赋值语句
	 */
	private int statementExpressionList(int i) {
		CMMToken token;
		CMMToken comma = null;
		while (true) {
			token = getToken(i);
			if (!token.startOfStatementExpression()) {
				if (token.in(new String[] { R_SMALL_BRACKET, SEMICOLON })) {
					if (comma != null) {
						comma.lexicalError = "多余的短语: " + "' " + comma.word + " '";
					}
					return i - 1;
				}
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER, TokenType.IDENTIFIER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER, TokenType.IDENTIFIER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					break;
				}
			}
			grammarRecordInc("statement expression");
			i = statementExpression(i);
			grammarRecorddec();
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.word.equals(COMMA)) {
				if (token.in(new String[] { R_SMALL_BRACKET, SEMICOLON })) {
					break;
				}
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { COMMA });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] {},
						new String[] { COMMA },
						new TokenType[] { TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER, TokenType.IDENTIFIER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i - 1;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					comma = null;
					i = tokenSkip(i);
				} else {
					comma = getToken(i);
					i = tokenSkip(i);
					if (i >= length) {
						GrammarError.unexpectedEndOfFile(this, i);
						return i;
					}
				}
			} else {
				comma = getToken(i);
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
			}
		}
		return i - 1;
	}

	/**
	 * for语句
	 */
	private int statementFor(int i) {
		CMMToken token = getToken(i);
		scopeDeepthInc("for head");// For循环初始化变量作用域介于for外层与for内层之间,记得在返回前恢复
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(L_SMALL_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { L_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { L_SMALL_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.startOfStatementExpressionOrDeclaration()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER, TokenType.IDENTIFIER },
					new String[] { TRUE, FALSE, REAL, INT, BOOLEAN, L_SMALL_BRACKET, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER, TokenType.IDENTIFIER },
					new String[] { TRUE, FALSE, REAL, INT, BOOLEAN, L_SMALL_BRACKET, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		grammarRecordInc("for init");
		i = statementForInit(i);
		grammarRecorddec();
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		int forExpStart = internalCodes.size();
		grammarRecordInc("for condition");
		if (!token.startOfExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			}
		}
		grammarRecordInc("expression");
		Symbol symbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
		i = expression(i, symbol);
		grammarRecorddec();
		if (symbol.getType() != VariableType.BOOLEAN) {
			GrammarError.variableTypeNotMatch(token, symbol.getType(), VariableType.BOOLEAN);
			symbol.addCvtCode(this, VariableType.BOOLEAN);
		}
		InternalCode jmpToForBodyEnd = new InternalCode(CMMOperator.jmpc, -1, MemoryArea.ADDRESS, false, currentLine);// 跳转到for循环结束,待返填
		internalCodes.add(jmpToForBodyEnd);
		InternalCode jmpToForBody = new InternalCode(CMMOperator.jmp, -1, MemoryArea.ADDRESS, currentLine);// 跳转到for循环主体,地址待返填
		internalCodes.add(jmpToForBody);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		grammarRecorddec();
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.startOfStatementExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER, TokenType.IDENTIFIER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER, TokenType.IDENTIFIER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		int forUpdateStart = internalCodes.size();
		grammarRecordInc("for update");
		i = statementForUpdate(i);
		grammarRecorddec();
		InternalCode jmpToExp = new InternalCode(CMMOperator.jmp, forExpStart, MemoryArea.ADDRESS, currentLine);
		internalCodes.add(jmpToExp);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(R_SMALL_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		grammarRecorddec();
		grammarRecordInc("for body");
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.startOfStatement()) {
			GrammarError.expectedWordNotSatisfied(token,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE, READ, SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE, READ, SEMICOLON },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		jmpToForBody.addrOrValue = internalCodes.size();
		int old_start = cycle_start;
		int old_end = cycle_end;
		cycle_start = forUpdateStart;
		grammarRecordInc("statement");
		i = statement(i);
		grammarRecorddec();
		scopeDeepthDec();
		clearSymbolTable(currentScopeDepth);
		InternalCode jmpToForUpdate = new InternalCode(CMMOperator.jmp, forUpdateStart, MemoryArea.ADDRESS, currentLine);
		internalCodes.add(jmpToForUpdate);
		cycle_end = internalCodes.size();
		jmpToForBodyEnd.addrOrValue = cycle_end;
		generateBreakAddress(cycle_start, cycle_end);
		cycle_start = old_start;
		cycle_end = old_end;
		return i;
	}

	/**
	 * for语句初始化语句
	 */
	private int statementForInit(int i) {
		CMMToken token = getToken(i);
		if (token.startOfVariableDeclaration()) {
			grammarRecordInc("local variable");
			i = statementDeclarationLocalVariable(i);
			grammarRecorddec();
			return i;
		}
		i = statementExpressionList(i);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * for语句更新语句
	 */
	private int statementForUpdate(int i) {
		return statementExpressionList(i);
	}

	/**
	 * 函数定义
	 */
	private int statementFunction(int i) {
		// 返回前将currentFunction恢复
		Function oldFunction = currentFunction;
		InternalCode jmpToEnd = new InternalCode(CMMOperator.jmp, -1, MemoryArea.ADDRESS, currentLine);
		internalCodes.add(jmpToEnd);// 跳转到函数末尾指令
		int address = internalCodes.size();
		Symbol symbol;
		CMMToken token = getToken(i);
		ReturnVariable rt = generateReturnVariable(token);
		if (rt.type == ReturnType.UNKNOWN) {
			GrammarError.unknownVariableType(token);
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { INT, BOOLEAN, REAL, VOID },
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				rt.type = ReturnType.INT;
				grammarTreeRecord("<unknown type>");
			} else {
				token = getToken(i);
				rt = generateReturnVariable(token);
				grammarTreeRecord(token.word);
			}
		} else {
			grammarTreeRecord(token.word);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (token.word.equals(L_MIDDLE_BRACKET)) {
			grammarTreeRecord(token.word);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			i = generrateFunctionReturnVariableLength(i, rt);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
		}
		currentFunction = new Function(rt);
		if (token.type != TokenType.IDENTIFIER) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER }, new String[] {});
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] {},
					new TokenType[] {},
					new String[] { L_SMALL_BRACKET },
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				token = new CMMToken("<unknown identifier>", TokenType.IDENTIFIER, token.sourceLine, token.offset);// 假设存在一个标识符
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		symbol = new Symbol(token.word, currentScopeDepth);
		symbol.variable = currentFunction;
		currentFunction.name = symbol.identifier;
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		// token = getToken(i);
		grammarRecordInc("parameters");
		i = statementParameters(i, currentFunction);
		grammarRecorddec();
		if (symbolTable.contains(symbol)) {
			int index = symbolTable.symbols.indexOf(symbol);
			Symbol oldSym = symbolTable.get(index);
			if (oldSym.address != -1) {
				GrammarError.redefineError(token);
			} else {
				oldSym.variable = currentFunction;// 返填
			}
		}
		symbolTable.add(symbol);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(L_LARGE_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { L_LARGE_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { L_LARGE_BRACKET },
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN,
							WRITE, READ },
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		i = statementBlock(i);
		// 检查返回值,添加缺省的返回语句,如果缺少返回值,则返回0值
		generateDefaultReturnCode();
		jmpToEnd.addrOrValue = internalCodes.size();
		symbol.address = address;
		backFillFunctionCall(currentFunction, symbol.address, symbol.identifier);
		currentFunction = oldFunction;
		return i;
	}

	/**
	 * 预解析实参类型
	 */
	private int statementFunctionArguments(int i, Symbol s) {
		Function function = new Function(new ReturnVariable(ReturnType.VOID));
		ArrayList<CMMVariable> arguments = new ArrayList<>();
		function.parametersList = arguments;
		s.variable = function;
		s.scopeDepth = 0;
		CMMToken token = getToken(i);
		if (!token.startOfExpression()) {
			if (token.word.equals(R_SMALL_BRACKET)) {
				return i - 1;
			}
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			}
		}
		Symbol symbol;
		while (true) {
			if (s.memoryArea == MemoryArea.STATIC) {
				symbol = new Symbol(s.identifier, currentStaticIndex, MemoryArea.STATIC, currentScopeDepth);
			} else {
				symbol = new Symbol(s.identifier, currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
			}
			int size = internalCodes.size();
			i = expression(i, symbol);
			int newSize = internalCodes.size();
			while (newSize > size) {
				newSize--;
				internalCodes.remove(newSize);
			}
			arguments.add(symbol.variable);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.word.equals(COMMA)) {
				if (token.word.equals(R_SMALL_BRACKET)) {
					break;
				}
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { COMMA });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] {},
						new String[] { COMMA },
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i - 1;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					i = tokenSkip(i);
					continue;
				}
			}
			CMMToken comma = token;
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfExpression()) {
				if (token.word.equals(R_SMALL_BRACKET)) {
					comma.lexicalError = "多余的短语: " + "' " + comma.word + " '";
					return i - 1;
				}
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					comma.lexicalError = "多余的短语: " + "' " + comma.word + " '";
					return i - 1;
				}
			}
		}
		return i - 1;
	}

	/**
	 * 函数调用语句
	 */
	private int statementFunctionCall(int i, Symbol symbol) {
		CMMToken startToken = getToken(tokenPreview(i));
		String identifier = startToken.word;
		CMMToken token = getToken(i);
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		// token = getToken(i);
		Symbol s;
		if (symbol.memoryArea == MemoryArea.STATIC) {
			s = new Symbol("", currentStaticIndex, MemoryArea.STATIC, currentScopeDepth);
		} else {
			s = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
		}
		int index = i;
		recoed = false;
		i = statementFunctionArguments(i, s);
		recoed = true;
		s.identifier = identifier;
		s = findFunction(s);
		if (s == null) {
			GrammarError.functionUndefine(startToken);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.word.equals(R_SMALL_BRACKET)) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_SMALL_BRACKET });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i - 1;
				} else {
					token = getToken(i);
				}
			}
			grammarTreeRecord(token.word);
			symbol.identifier = identifier;
			symbol.address = -1;
			symbol.scopeDepth = currentScopeDepth;
			symbol.variable = new CMMVariable(VariableType.UNKNOWN);
			symbol.variable.length = 0;
			symbol.setRight();
			return i;
		}
		i = index;
		if (s.address == -1) {
			// 开辟函数空间
			InternalCode space = new InternalCode(CMMOperator.space, s, MemoryArea.INT_CON, SIZE, currentLine);// 回填
			internalCodes.add(space);
			// 函数返回地址
			InternalCode load_return_addr = new InternalCode(CMMOperator.load, -1, MemoryArea.INT_CON, currentLine);// 待回填
			internalCodes.add(load_return_addr);
			InternalCode mov_return_addr = new InternalCode(CMMOperator.mov, 1, MemoryArea.ARGUMENT, currentLine);
			internalCodes.add(mov_return_addr);
			grammarRecordInc("arguments");
			i = statementArguments(i, s.identifier, (Function) s.variable, symbol);
			grammarRecorddec();
			// 压栈
			InternalCode pushb = new InternalCode(CMMOperator.pushb, currentLine);
			internalCodes.add(pushb);
			InternalCode pusht = new InternalCode(CMMOperator.pusht, s, MemoryArea.INT_CON, SIZE, currentLine);// 回填
			internalCodes.add(pusht);
			// 跳转
			InternalCode start = new InternalCode(CMMOperator.jmp, s, MemoryArea.ADDRESS, ADDRESS, currentLine);// 回填
			internalCodes.add(start);
			// 返填地址
			load_return_addr.addrOrValue = internalCodes.size();// 函数返回地址
			// 弹栈
			InternalCode popt = new InternalCode(CMMOperator.popt, currentLine);// 运行时bp初始为0,sp初始为0
			internalCodes.add(popt);
			InternalCode popb = new InternalCode(CMMOperator.popb, currentLine);
			internalCodes.add(popb);
		} else {
			Function fun = (Function) s.variable;
			// 开辟函数空间
			InternalCode space = new InternalCode(CMMOperator.space, fun.stackSize, MemoryArea.INT_CON, s.address, currentLine);// 回填
			internalCodes.add(space);
			// 函数返回地址
			InternalCode load_return_addr = new InternalCode(CMMOperator.load, -1, MemoryArea.INT_CON, currentLine);// 待回填
			internalCodes.add(load_return_addr);
			InternalCode mov_return_addr = new InternalCode(CMMOperator.mov, 1, MemoryArea.ARGUMENT, currentLine);
			internalCodes.add(mov_return_addr);
			grammarRecordInc("arguments");
			i = statementArguments(i, s.identifier, (Function) s.variable, symbol);
			grammarRecorddec();
			// 压栈
			InternalCode pushb = new InternalCode(CMMOperator.pushb, currentLine);
			internalCodes.add(pushb);
			InternalCode pusht = new InternalCode(CMMOperator.pusht, fun.stackSize, MemoryArea.INT_CON, currentLine);// 回填
			internalCodes.add(pusht);
			// 跳转
			InternalCode start = new InternalCode(CMMOperator.jmp, s.address, MemoryArea.ADDRESS, currentLine);// 回填
			internalCodes.add(start);
			// 返填地址
			load_return_addr.addrOrValue = internalCodes.size();// 函数返回地址
			// 弹栈
			InternalCode popt = new InternalCode(CMMOperator.popt, currentLine);// 运行时bp初始为0,sp初始为0
			internalCodes.add(popt);
			InternalCode popb = new InternalCode(CMMOperator.popb, currentLine);
			internalCodes.add(popb);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(R_SMALL_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i - 1;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		symbol.identifier = s.identifier;
		symbol.address = -1;
		symbol.memoryArea = s.memoryArea;
		symbol.scopeDepth = currentScopeDepth;
		symbol.variable = new CMMVariable(((Function) s.variable).returnTypeToVariableType());
		symbol.variable.length = 0;
		symbol.setRight();
		return i;
	}

	/**
	 * if语句
	 */
	private int statementIf(int i) {
		CMMToken token = getToken(i);
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(L_SMALL_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { L_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { L_SMALL_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.startOfExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		Symbol symbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
		grammarRecordInc("expression");
		i = expression(i, symbol);
		grammarRecorddec();
		if (symbol.getType() != VariableType.BOOLEAN) {
			GrammarError.variableTypeNotMatch(token, symbol.getType(), VariableType.BOOLEAN);
			symbol.addCvtCode(this, VariableType.BOOLEAN);
		}
		InternalCode jmpIfEnd = new InternalCode(CMMOperator.jmpc, -1, MemoryArea.ADDRESS, false, currentLine);// 跳转地址待返填
		internalCodes.add(jmpIfEnd);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(R_SMALL_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET },
					new TokenType[] {},
					new String[] { L_LARGE_BRACKET },
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.startOfStatement()) {
			GrammarError.expectedWordNotSatisfied(token,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE, READ, SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE, READ, SEMICOLON },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		scopeDeepthInc("statement");
		i = statement(i);
		scopeDeepthDec();
		clearSymbolTable(currentScopeDepth);
		int tmp_index = i;
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (token.word.equals(ELSE)) {
			grammarRecordInc("else statement");
			grammarTreeRecord(token.word);
			InternalCode jmpElseEnd = new InternalCode(CMMOperator.jmp, -1, MemoryArea.ADDRESS, currentLine);
			internalCodes.add(jmpElseEnd);
			jmpIfEnd.addrOrValue = internalCodes.size();
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.startOfStatement()) {
				GrammarError.expectedWordNotSatisfied(token,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE, READ, SEMICOLON });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
						new String[] { L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE, READ, SEMICOLON },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				}
			}
			scopeDeepthInc("statement");
			i = statement(i);
			scopeDeepthDec();
			grammarRecorddec();
			clearSymbolTable(currentScopeDepth);
			jmpElseEnd.addrOrValue = internalCodes.size();
		} else {
			jmpIfEnd.addrOrValue = internalCodes.size();
			return tmp_index;
		}
		return i;
	}

	/**
	 * 块里面的单条语句
	 */
	private int statementInBlock(int i) {
		int tmp_index = i;
		CMMToken token = getToken(i);
		switch (token.type) {
			case DECIMAL_NUMBER:
			case REAL_NUMBER:
			case IDENTIFIER:
				grammarRecordInc("statement");
				i = statement(i);
				grammarRecorddec();
				break;
			default:
				switch (token.word) {
					case TRUE:
					case FALSE:
					case IF:
					case READ:
					case WRITE:
					case WHILE:
					case FOR:
					case BREAK:
					case CONTINUE:
					case RETURN:
					case L_SMALL_BRACKET:
					case L_LARGE_BRACKET:
					case SEMICOLON:
					case INCREMENT:
					case DECREMENT:
						grammarRecordInc("statement");
						i = statement(i);
						grammarRecorddec();
						break;
					case INT:
					case REAL:
					case BOOLEAN: {
						i = tokenSkip(i);
						if (i >= length) {
							GrammarError.unexpectedEndOfFile(this, i);
							return i;
						}
						token = getToken(i);
						CMMToken unmatchedSymbolToken = token;
						if (token.word.equals(L_MIDDLE_BRACKET)) {
							i = tokenSkip(i);
							if (i >= length) {
								GrammarError.unexpectedEndOfFile(this, i);
								return i;
							}
							token = getToken(i);
							if (token.type != TokenType.DECIMAL_NUMBER) {
								GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.DECIMAL_NUMBER }, new String[] {});
								ErrorReturn result = GrammarError.skipTo(this, i,
										new TokenType[] { TokenType.DECIMAL_NUMBER },
										new String[] {},
										new TokenType[] {},
										new String[] { R_MIDDLE_BRACKET },
										new TokenType[] {},
										new String[] { SEMICOLON, R_LARGE_BRACKET });
								i = result.i;
								if (result.type == ErrorReturnType.END_TYPE) {
									return i;
								}
							}
							i = tokenSkip(i);
							if (i >= length) {
								GrammarError.unexpectedEndOfFile(this, i);
								return i;
							}
							token = getToken(i);
							if (!token.word.equals(R_MIDDLE_BRACKET)) {
								GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_MIDDLE_BRACKET });
								GrammarError.unmatchedSymbol(unmatchedSymbolToken);
								ErrorReturn result = GrammarError.skipTo(this, i,
										new TokenType[] {},
										new String[] { R_MIDDLE_BRACKET },
										new TokenType[] { TokenType.IDENTIFIER },
										new String[] {},
										new TokenType[] {},
										new String[] { SEMICOLON, R_LARGE_BRACKET });
								i = result.i;
								if (result.type == ErrorReturnType.END_TYPE) {
									return i;
								}
							}
							i = tokenSkip(i);
							if (i >= length) {
								GrammarError.unexpectedEndOfFile(this, i);
								return i;
							}
							token = getToken(i);
						}
						if (!token.type.equals(TokenType.IDENTIFIER)) {
							GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER }, new String[] {});
							ErrorReturn result = GrammarError.skipTo(this, i,
									new TokenType[] { TokenType.IDENTIFIER },
									new String[] {},
									new TokenType[] {},
									new String[] { ASSIGN, COMMA, SEMICOLON, L_SMALL_BRACKET },
									new TokenType[] {},
									new String[] { SEMICOLON, R_LARGE_BRACKET });
							i = result.i;
							if (result.type == ErrorReturnType.END_TYPE) {
								return i;
							}
						}
						i = tokenSkip(i);
						if (i >= length) {
							GrammarError.unexpectedEndOfFile(this, i);
							return i;
						}
						token = getToken(i);
						if (!token.in(new String[] { ASSIGN, COMMA, SEMICOLON, L_SMALL_BRACKET })) {
							GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { ASSIGN, COMMA, SEMICOLON, L_SMALL_BRACKET });
							ErrorReturn result = GrammarError.skipTo(this, i,
									new TokenType[] {},
									new String[] { ASSIGN, COMMA, SEMICOLON, L_SMALL_BRACKET },
									new TokenType[] { TokenType.IDENTIFIER },
									new String[] { INT, BOOLEAN, REAL },
									new TokenType[] {},
									new String[] { R_LARGE_BRACKET });
							i = result.i;
							if (result.type == ErrorReturnType.END_TYPE) {
								return i;
							} else if (result.type == ErrorReturnType.AFTER_TYPE) {
								i = tokenSkip(i);
								token = getToken(i);
								if (token.in(new String[] { INT, BOOLEAN, REAL })) {
									grammarRecordInc("function declaration");
									i = statementDeclarationFunction(tmp_index);
									grammarRecorddec();
								} else {
									grammarRecordInc("local variable");
									i = statementDeclarationLocalVariable(tmp_index);
									grammarRecorddec();
								}
							} else {
								token = getToken(i);
							}
						}
						switch (token.word) {
							case ASSIGN:
							case COMMA:
							case SEMICOLON:
								grammarRecordInc("local variable");
								i = statementDeclarationLocalVariable(tmp_index);
								grammarRecorddec();
								break;
							case L_SMALL_BRACKET:
								grammarRecordInc("function declaration");
								i = statementDeclarationFunction(tmp_index);
								grammarRecorddec();
								break;
							default:
								break;
						}
						break;
					}
					case VOID:
						grammarRecordInc("function declaration");
						i = statementDeclarationFunction(tmp_index);
						grammarRecorddec();
						break;
					default:
						GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
								new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN,
										WRITE, READ, SEMICOLON });
						break;
				}
				break;
		}
		return i;
	}

	/**
	 * 解析形参类型
	 */
	private int statementParameters(int i, Function f) {
		CMMToken token = getToken(i);
		if (!token.word.equals(L_SMALL_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { L_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { L_SMALL_BRACKET },
					new TokenType[] {},
					new String[] { INT, BOOLEAN, READ },
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.EXPECTED_TYPE) {
				token = getToken(i);
				grammarTreeRecord(token.word);
			}
		} else {
			grammarTreeRecord(token.word);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.in(new TokenType[] { TokenType.IDENTIFIER }, new String[] { INT, BOOLEAN, REAL })) {
			if (token.word.equals(R_SMALL_BRACKET)) {
				grammarTreeRecord(token.word);
				return i;
			}
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER },
					new String[] { INT, BOOLEAN, REAL },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		while (true) {
			token = getToken(i);
			CMMVariable vt = generateVariable(token);
			if (vt.type == VariableType.UNKNOWN) {
				GrammarError.unknownVariableType(token);
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] {},
						new String[] { INT, BOOLEAN, REAL },
						new TokenType[] { TokenType.IDENTIFIER },
						new String[] {},
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					vt.type = VariableType.INT;
					grammarTreeRecord("<unknown type>");
				} else {
					token = getToken(i);
					vt = generateVariable(token);
					grammarTreeRecord(token.word);
				}
			} else {
				grammarTreeRecord(token.word);
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			CMMToken L_BRACKET = token;
			if (token.word.equals(L_MIDDLE_BRACKET)) {
				grammarTreeRecord(token.word);
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				i = generrateVariablelength(i, vt);
				i = tokenSkip(i);
				if (i >= length) {
					GrammarError.unexpectedEndOfFile(this, i);
					return i;
				}
				token = getToken(i);
			}
			if (vt.length > 0) {
				GrammarError.arrayCanNotBeParameter(L_BRACKET);
			}
			f.addParameter(vt);
			if (token.type != TokenType.IDENTIFIER) {
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER }, new String[] {});
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER },
						new String[] {},
						new TokenType[] {},
						new String[] { COMMA, R_SMALL_BRACKET },
						new TokenType[] {},
						new String[] { SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				} else if (result.type == ErrorReturnType.AFTER_TYPE) {
					token = new CMMToken("<unknown identifier>", TokenType.IDENTIFIER, token.sourceLine, token.offset);// 假设存在一个标识符
				} else {
					token = getToken(i);
				}
			}
			grammarTreeRecord(token.word);
			Symbol symbol = new Symbol(token.word, currentScopeDepth + 1);
			symbol.variable = vt;
			symbol.address = f.stackSize;
			currentFunction.localVariableAddressTable.add(new VariableAddress(currentFunction.stackSize, "参数:" + token.word, vt.length));
			f.stackSize++;
			if (symbolTable.contains(symbol)) {
				GrammarError.redefineError(token);
			}
			symbolTable.add(symbol);
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			CMMToken comma;
			if (!token.word.equals(COMMA)) {
				if (token.word.equals(R_SMALL_BRACKET)) {
					break;
				}
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { COMMA });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] {},
						new String[] { COMMA },
						new TokenType[] { TokenType.IDENTIFIER },
						new String[] { INT, BOOLEAN, REAL },
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					return i;
				} else if (result.type == ErrorReturnType.EXPECTED_TYPE) {
					token = getToken(i);
					comma = token;
					grammarTreeRecord(token.word);
				} else {
					i = tokenSkip(i);
					continue;
				}
			} else {
				comma = token;
				grammarTreeRecord(token.word);
			}
			i = tokenSkip(i);
			if (i >= length) {
				GrammarError.unexpectedEndOfFile(this, i);
				return i;
			}
			token = getToken(i);
			if (!token.in(new TokenType[] { TokenType.IDENTIFIER }, new String[] { INT, BOOLEAN, REAL })) {
				if (token.word.equals(R_SMALL_BRACKET)) {
					comma.lexicalError = "多余的短语: " + "' " + comma.word + " '";
					break;
				}
				GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { INT, BOOLEAN, REAL });
				ErrorReturn result = GrammarError.skipTo(this, i,
						new TokenType[] { TokenType.IDENTIFIER },
						new String[] { INT, BOOLEAN, REAL },
						new TokenType[] {},
						new String[] {},
						new TokenType[] {},
						new String[] { R_SMALL_BRACKET, SEMICOLON, R_LARGE_BRACKET });
				i = result.i;
				if (result.type == ErrorReturnType.END_TYPE) {
					comma.lexicalError = "多余的短语: " + "' " + comma.word + " '";
					return i;
				}
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * read语句
	 */
	private int statementRead(int i) {
		CMMToken readToken = getToken(i);
		grammarTreeRecord(readToken.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		CMMToken token = getToken(i);
		if (!token.startOfExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		Symbol symbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
		grammarRecordInc("expression");
		i = expression(i, symbol);
		removeLastLoad();
		grammarRecorddec();
		if (!symbol.isLeftValue()) {
			GrammarError.onlyLeftValueCanAssign(readToken);
		}
		InternalCode readCode = new InternalCode(CMMOperator.read, symbol.address, symbol.memoryArea, currentLine);
		internalCodes.add(readCode);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE,
							READ },
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.EXPECTED_TYPE) {
				token = getToken(i);
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				return i - 1;
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * return语句
	 */
	private int statementReturn(int i) {
		CMMToken returnToken = getToken(i);
		grammarTreeRecord(returnToken.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		CMMToken token = getToken(i);
		if (token.word.equals(SEMICOLON)) {
			grammarTreeRecord(token.word);
			generateDefaultReturnCode();
			return i;
		}
		if (!token.startOfExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		Symbol symbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
		grammarRecordInc("expression");
		i = expression(i, symbol);
		grammarRecorddec();
		if (!symbol.getType().equals(currentFunction.getReturnType())) {
			GrammarError.returnTypeNotMatch(returnToken, symbol.getType(), currentFunction.getReturnType());
		}
		InternalCode retCode = new InternalCode(CMMOperator.ret, currentLine);
		internalCodes.add(retCode);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE,
							READ },
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.EXPECTED_TYPE) {
				token = getToken(i);
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				return i - 1;
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	/**
	 * while语句
	 */
	private int statementWhile(int i) {
		CMMToken token = getToken(i);
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(L_SMALL_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { L_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { L_SMALL_BRACKET },
					new TokenType[] {},
					new String[] { INT, BOOLEAN, READ },
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else if (result.type == ErrorReturnType.EXPECTED_TYPE) {
				token = getToken(i);
				grammarTreeRecord(token.word);
			}
		} else {
			grammarTreeRecord(token.word);
		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		int old_start = cycle_start;// 函数返回前恢复这两个值
		int old_end = cycle_end;// 函数返回前恢复这两个值
		cycle_start = internalCodes.size();
		if (!token.startOfExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		Symbol symbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
		grammarRecordInc("expression");
		i = expression(i, symbol);
		grammarRecorddec();
		if (symbol.getType() != VariableType.BOOLEAN) {
			GrammarError.variableTypeNotMatch(token, symbol.getType(), VariableType.BOOLEAN);
			symbol.addCvtCode(this, VariableType.BOOLEAN);
		}
		InternalCode jmpc = new InternalCode(CMMOperator.jmpc, -1, MemoryArea.ADDRESS, false, currentLine);// 跳转地址待返填cycle_end
		internalCodes.add(jmpc);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(R_SMALL_BRACKET)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { R_SMALL_BRACKET });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { R_SMALL_BRACKET },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			} else {
				token = getToken(i);
			}
		}
		grammarTreeRecord(token.word);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.startOfStatement()) {
			GrammarError.expectedWordNotSatisfied(token,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE, READ, SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE, READ, SEMICOLON },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		scopeDeepthInc("statement");
		i = statement(i);
		scopeDeepthDec();
		InternalCode jmp = new InternalCode(CMMOperator.jmp, cycle_start, MemoryArea.ADDRESS, currentLine);
		internalCodes.add(jmp);
		cycle_end = internalCodes.size();
		jmpc.addrOrValue = cycle_end;
		clearSymbolTable(currentScopeDepth);
		generateBreakAddress(cycle_start, cycle_end);
		cycle_start = old_start;
		cycle_end = old_end;
		return i;
	}

	/**
	 * write语句
	 */
	private int statementWrite(int i) {
		grammarTreeRecord(WRITE);
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		CMMToken token = getToken(i);
		if (!token.startOfStrOrExpression()) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] { TokenType.STR, TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] { TokenType.STR, TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { TRUE, FALSE, L_SMALL_BRACKET, LOGIC_NOT, BIT_NOT, PLUS, MINUS, INCREMENT, DECREMENT },
					new TokenType[] {},
					new String[] {},
					new TokenType[] {},
					new String[] { SEMICOLON, R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.END_TYPE) {
				return i;
			}
		}
		if (token.type.equals(TokenType.STR)) {
			grammarTreeRecord(token.word);
			InternalCode writeCode = new InternalCode(CMMOperator.write, token.word, MemoryArea.STR_CON, currentLine);
			internalCodes.add(writeCode);
		} else {
			Symbol symbol = new Symbol("", currentFunction.stackSize, MemoryArea.LOCAL, currentScopeDepth);
			grammarRecordInc("expression");
			i = expression(i, symbol);
			grammarRecorddec();
			InternalCode writeCode = new InternalCode(CMMOperator.write, currentLine);
			internalCodes.add(writeCode);

		}
		i = tokenSkip(i);
		if (i >= length) {
			GrammarError.unexpectedEndOfFile(this, i);
			return i;
		}
		token = getToken(i);
		if (!token.word.equals(SEMICOLON)) {
			GrammarError.expectedWordNotSatisfied(token, new TokenType[] {}, new String[] { SEMICOLON });
			ErrorReturn result = GrammarError.skipTo(this, i,
					new TokenType[] {},
					new String[] { SEMICOLON },
					new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
					new String[] { INT, BOOLEAN, REAL, L_LARGE_BRACKET, TRUE, FALSE, L_SMALL_BRACKET, INCREMENT, DECREMENT, IF, WHILE, FOR, BREAK, CONTINUE, RETURN, WRITE,
							READ },
					new TokenType[] {},
					new String[] { R_LARGE_BRACKET });
			i = result.i;
			if (result.type == ErrorReturnType.EXPECTED_TYPE) {
				token = getToken(i);
			} else if (result.type == ErrorReturnType.AFTER_TYPE) {
				return i;
			} else {
				return i - 1;
			}
		}
		grammarTreeRecord(token.word);
		return i;
	}

	private static final String	treeNodeLSymbol		= "<";

	private static final String	treeNodeRSymbol		= ">";

	public static final String	ADDRESS				= "address";

	public static final String	SIZE				= "size";

	public static final int		MAX_STATIC_SIZE		= 4096;			// 静态变量区大小

	public static final int		FUNCTION_STACK_SIZE	= 262144;		// 函数栈总大小

	public static final String	TRUE				= "true";

	public static final String	FALSE				= "false";

	public static final String	REAL				= "real";

	public static final String	INT					= "int";

	public static final String	BOOLEAN				= "boolean";

	public static final String	VOID				= "void";

	public static final String	IF					= "if";

	public static final String	ELSE				= "else";

	public static final String	READ				= "read";

	public static final String	WRITE				= "write";

	public static final String	WHILE				= "while";

	public static final String	FOR					= "for";

	public static final String	BREAK				= "break";

	public static final String	CONTINUE			= "continue";

	public static final String	RETURN				= "return";

	public static final String	L_SMALL_BRACKET		= "(";

	public static final String	R_SMALL_BRACKET		= ")";

	public static final String	L_LARGE_BRACKET		= "{";

	public static final String	R_LARGE_BRACKET		= "}";

	public static final String	L_MIDDLE_BRACKET	= "[";

	public static final String	R_MIDDLE_BRACKET	= "]";

	public static final String	SEMICOLON			= ";";

	public static final String	COMMA				= ",";

	public static final String	ASSIGN				= "=";

	public static final String	GREATER_THAN		= ">";

	public static final String	LESS_THAN			= "<";

	public static final String	LOGIC_NOT			= "!";

	public static final String	BIT_NOT				= "~";

	public static final String	DOUBEL_EQUAL		= "==";

	public static final String	LESS_EQUAL			= "<=";

	public static final String	GREATER_EQUAL		= ">=";

	public static final String	NOT_EQUAL			= "!=";

	public static final String	LOGIC_OR			= "||";

	public static final String	LOGIC_AND			= "&&";

	public static final String	INCREMENT			= "++";

	public static final String	DECREMENT			= "--";

	public static final String	PLUS				= "+";

	public static final String	MINUS				= "-";

	public static final String	STAR				= "*";

	public static final String	SLASH				= "/";

	public static final String	BIT_AND				= "&";

	public static final String	BIT_OR				= "|";

	public static final String	BIT_XOR				= "^";

	public static final String	PERCENT				= "%";

	public static final String	LSHIFT				= "<<";

	public static final String	RSHIFT				= ">>";

	public static final String	PLUSASSIGN			= "+=";

	public static final String	MINUSASSIGN			= "-=";

	public static final String	STARASSIGN			= "*=";

	public static final String	SLASHASSIGN			= "/=";

	public static final String	ANDASSIGN			= "&=";

	public static final String	ORASSIGN			= "|=";

	public static final String	XORASSIGN			= "^=";

	public static final String	PERCENTASSIGN		= "%=";

	public static final String	LSHIFTASSIGN		= "<<=";

	public static final String	RSHIFTASSIGN		= ">>=";

	public static final String	PRE_PROCESS_LENGTH	= "#length";

	public static final String	PRE_PROCESS_ADDRESS	= "#address";

}
