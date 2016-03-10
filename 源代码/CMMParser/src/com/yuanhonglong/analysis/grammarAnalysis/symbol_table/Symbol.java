package com.yuanhonglong.analysis.grammarAnalysis.symbol_table;

import com.yuanhonglong.analysis.grammarAnalysis.CMMOperator;
import com.yuanhonglong.analysis.grammarAnalysis.CalculateResult;
import com.yuanhonglong.analysis.grammarAnalysis.GrammarAnalysis;
import com.yuanhonglong.analysis.grammarAnalysis.GrammarError;
import com.yuanhonglong.analysis.grammarAnalysis.InternalCode;
import com.yuanhonglong.analysis.grammarAnalysis.MemoryArea;
import com.yuanhonglong.analysis.lexical_analysis.CMMToken;
import com.yuanhonglong.run.Calculator;

/**
 * 符号
 *
 * @author 天命剑主<br>
 *         on 2015/10/1.
 */
public class Symbol {

	public String		identifier;	// 标识符
	public int			address;	// 地址,-1代表未填
	public MemoryArea	memoryArea;	// 内存区域
	public int			scopeDepth;	// 作用域深度
	public CMMVariable	variable;	// 变量类型(符号类型),包括函数
	public Object		value;		// 值

	public Symbol(String identifier) {
		this(identifier, 0);
	}

	public Symbol(String identifier, int scopeDepth) {
		this(identifier, -1, MemoryArea.LOCAL, scopeDepth);
	}

	public Symbol(String identifier, int address, MemoryArea area, int scopeDepth) {
		this.identifier = identifier;
		this.address = address;
		this.memoryArea = area;
		this.scopeDepth = scopeDepth;
		this.variable = new CMMVariable(VariableType.UNKNOWN);
		this.value = null;
	}

	public Symbol(Symbol s) {
		identifier = s.identifier;
		address = s.address;
		memoryArea = s.memoryArea;
		scopeDepth = s.scopeDepth;
		value = s.value;
		if (s.variable instanceof Function) {
			variable = new Function((Function) s.variable);
		} else {
			variable = new CMMVariable(s.variable);
		}
	}

	public VariableType getType() {
		return variable.type;
	}

	public static void addBitNotCode(GrammarAnalysis grammarAnalysis) {
		InternalCode bit_not = new InternalCode(CMMOperator.bit_not, grammarAnalysis.currentLine);
		grammarAnalysis.internalCodes.add(bit_not);
	}

	public static void addNotCode(GrammarAnalysis grammarAnalysis) {
		InternalCode not = new InternalCode(CMMOperator.logic_not, grammarAnalysis.currentLine);
		grammarAnalysis.internalCodes.add(not);
	}

	public void addAndCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode and = new InternalCode(CMMOperator.bit_and, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(and);
		} else {
			InternalCode and = new InternalCode(CMMOperator.bit_and, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(and);
		}
	}

	public void addCvtCode(GrammarAnalysis grammarAnalysis, VariableType type) {
		InternalCode cvt = new InternalCode(CMMOperator.cvt, type, grammarAnalysis.currentLine);
		grammarAnalysis.internalCodes.add(cvt);
	}

	public void addDivCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode div = new InternalCode(CMMOperator.div, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(div);
		} else {
			InternalCode div = new InternalCode(CMMOperator.div, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(div);
		}
	}

	public void addEqCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode eq = new InternalCode(CMMOperator.eq, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(eq);
		} else {
			InternalCode eq = new InternalCode(CMMOperator.eq, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(eq);
		}
	}

	public void addGeCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode ge = new InternalCode(CMMOperator.ge, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(ge);
		} else {
			InternalCode ge = new InternalCode(CMMOperator.ge, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(ge);
		}
	}

	public void addGtCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode gt = new InternalCode(CMMOperator.gt, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(gt);
		} else {
			InternalCode gt = new InternalCode(CMMOperator.gt, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(gt);
		}
	}

	public void addLeCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode le = new InternalCode(CMMOperator.le, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(le);
		} else {
			InternalCode le = new InternalCode(CMMOperator.le, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(le);
		}
	}

	public void addLshCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode lsh = new InternalCode(CMMOperator.lsh, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(lsh);
		} else {
			InternalCode lsh = new InternalCode(CMMOperator.lsh, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(lsh);
		}
	}

	public void addLtCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode lt = new InternalCode(CMMOperator.lt, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(lt);
		} else {
			InternalCode lt = new InternalCode(CMMOperator.lt, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(lt);
		}
	}

	public void addMinusCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode minus = new InternalCode(CMMOperator.minus, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(minus);
		} else {
			InternalCode minus = new InternalCode(CMMOperator.minus, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(minus);
		}
	}

	public void addMovCode(GrammarAnalysis grammarAnalysis) {
		InternalCode mov = new InternalCode(CMMOperator.mov, address, memoryArea, grammarAnalysis.currentLine);
		grammarAnalysis.internalCodes.add(mov);
	}

	public void addMulCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode mul = new InternalCode(CMMOperator.mul, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(mul);
		} else {
			InternalCode mul = new InternalCode(CMMOperator.mul, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(mul);
		}
	}

	public void addNeCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode ne = new InternalCode(CMMOperator.ne, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(ne);
		} else {
			InternalCode ne = new InternalCode(CMMOperator.ne, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(ne);
		}
	}

	public void addOrCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode or = new InternalCode(CMMOperator.bit_or, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(or);
		} else {
			InternalCode or = new InternalCode(CMMOperator.bit_or, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(or);
		}
	}

	public void addModCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode mod = new InternalCode(CMMOperator.mod, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(mod);
		} else {
			InternalCode mod = new InternalCode(CMMOperator.mod, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(mod);
		}
	}

	public void addPlusCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode plus = new InternalCode(CMMOperator.plus, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(plus);
		} else {
			InternalCode plus = new InternalCode(CMMOperator.plus, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(plus);
		}
	}

	public void addRshCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode rsh = new InternalCode(CMMOperator.rsh, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(rsh);
		} else {
			InternalCode rsh = new InternalCode(CMMOperator.rsh, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(rsh);
		}
	}

	public void addXorCode(GrammarAnalysis grammarAnalysis) {
		if (isConstValue()) {
			InternalCode xor = new InternalCode(CMMOperator.bit_xor, value, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(xor);
		} else {
			InternalCode xor = new InternalCode(CMMOperator.bit_xor, address, memoryArea, grammarAnalysis.currentLine);
			grammarAnalysis.internalCodes.add(xor);
		}
	}

	/**
	 * And:按位与操作,只要包含浮点值都要转换为INT值 布尔值与布尔值的结果为布尔值,其他为INT值
	 */
	public CalculateResult calcuAnd(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addAndCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_and(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addAndCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_and(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addAndCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_and(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addAndCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_and(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addAndCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_and(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addAndCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_and(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addAndCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_and(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addAndCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_and(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addAndCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuAssign(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (!lSymbol.isLeftValue()) {
			return CalculateResult.ERROR;
		}
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			lSymbol.addMovCode(grammarAnalysis);
			setRight(lSymbol.variable.type);
			return CalculateResult.CORRECT;
		}
		if (lSymbol.isBoolean() && rSymbol.isInt()) {
			lSymbol.addMovCode(grammarAnalysis);
			setRight(lSymbol.variable.type);
			return CalculateResult.WARNING;
		}
		if (lSymbol.isBoolean() && rSymbol.isReal()) {
			lSymbol.addMovCode(grammarAnalysis);
			setRight(lSymbol.variable.type);
			return CalculateResult.WARNING;
		}
		if (lSymbol.isInt() && rSymbol.isBoolean()) {
			lSymbol.addMovCode(grammarAnalysis);
			setRight(lSymbol.variable.type);
			return CalculateResult.WARNING;
		}
		if (lSymbol.isInt() && rSymbol.isInt()) {
			lSymbol.addMovCode(grammarAnalysis);
			setRight(lSymbol.variable.type);
			return CalculateResult.CORRECT;
		}
		if (lSymbol.isInt() && rSymbol.isReal()) {
			lSymbol.addMovCode(grammarAnalysis);
			setRight(lSymbol.variable.type);
			return CalculateResult.WARNING;
		}
		if (lSymbol.isReal() && rSymbol.isBoolean()) {
			lSymbol.addMovCode(grammarAnalysis);
			setRight(lSymbol.variable.type);
			return CalculateResult.WARNING;
		}
		if (lSymbol.isReal() && rSymbol.isInt()) {
			lSymbol.addMovCode(grammarAnalysis);
			setRight(lSymbol.variable.type);
			return CalculateResult.CORRECT;
		}
		if (lSymbol.isReal() && rSymbol.isReal()) {
			lSymbol.addMovCode(grammarAnalysis);
			setRight(lSymbol.variable.type);
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuDiv(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol, CMMToken expToken) {
		if (rSymbol.isZero()) {
			// 除0错误
			GrammarError.devidedByZero(expToken);
			return CalculateResult.CORRECT;
		}
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.div(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addDivCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.div(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addDivCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.div(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addDivCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.div(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addDivCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.div(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addDivCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.div(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addDivCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.div(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addDivCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.div(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addDivCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.div(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addDivCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuEq(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.eq(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addEqCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.eq(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addEqCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.eq(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addEqCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.eq(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addEqCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.eq(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addEqCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.eq(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addEqCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.eq(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addEqCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.eq(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addEqCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.eq(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addEqCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuGe(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ge(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ge(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ge(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ge(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ge(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ge(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ge(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ge(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ge(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuGt(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.gt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.gt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.gt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.gt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.gt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.gt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.gt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.gt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.gt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addGtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuLe(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.le(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.le(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.le(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.le(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.le(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.le(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.le(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.le(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.le(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	/**
	 * LogicAnd:短路与
	 */
	public CalculateResult calcuLogicAnd(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_and(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		}
		return CalculateResult.ERROR;
	}

	/**
	 * LogicOr:短路或
	 */
	public CalculateResult calcuLogicOr(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.logic_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				rSymbol.addCvtCode(grammarAnalysis, VariableType.BOOLEAN);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuLsh(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuLt(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.lt(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addLtCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuMinus(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.minus(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMinusCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.minus(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMinusCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.minus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMinusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.minus(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMinusCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.minus(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMinusCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.minus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMinusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.minus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMinusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.minus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMinusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.minus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMinusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuMul(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mul(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMulCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mul(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMulCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mul(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMulCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mul(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMulCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mul(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMulCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mul(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMulCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mul(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMulCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mul(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMulCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mul(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addMulCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuNe(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ne(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addNeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ne(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addNeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ne(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addNeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ne(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addNeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ne(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addNeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ne(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addNeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ne(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addNeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ne(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addNeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.ne(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addNeCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuNot(GrammarAnalysis grammarAnalysis, Symbol rSymbol) {
		switch (rSymbol.variable.type) {
			case BOOLEAN:
				if (rSymbol.isConstValue()) {
					grammarAnalysis.removeLastLoad();
					setConst(Calculator.logic_not(rSymbol.value), MemoryArea.BOOL_CON);
					grammarAnalysis.loadConstSymbol(this);
				} else {
					setRight(VariableType.BOOLEAN);
					addNotCode(grammarAnalysis);
				}
				return CalculateResult.CORRECT;
			case INT:
				if (rSymbol.isConstValue()) {
					grammarAnalysis.removeLastLoad();
					setConst(Calculator.logic_not(rSymbol.value), MemoryArea.BOOL_CON);
					grammarAnalysis.loadConstSymbol(this);
				} else {
					setRight(VariableType.BOOLEAN);
					addNotCode(grammarAnalysis);
				}
				return CalculateResult.WARNING;
			case REAL:
				if (rSymbol.isConstValue()) {
					grammarAnalysis.removeLastLoad();
					setConst(Calculator.logic_not(rSymbol.value), MemoryArea.BOOL_CON);
					grammarAnalysis.loadConstSymbol(this);
				} else {
					setRight(VariableType.BOOLEAN);
					addNotCode(grammarAnalysis);
				}
				return CalculateResult.WARNING;
			default:
				return CalculateResult.ERROR;
		}
	}

	/**
	 * opps:相反数
	 */
	public CalculateResult calcuOpps(GrammarAnalysis grammarAnalysis, Symbol rSymbol) {
		switch (rSymbol.variable.type) {
			case BOOLEAN:
				if (rSymbol.isConstValue()) {
					grammarAnalysis.removeLastLoad();
					setConst(Calculator.opps(rSymbol.value), MemoryArea.INT_CON);
					grammarAnalysis.loadConstSymbol(this);
				} else {
					setRight(VariableType.INT);
					InternalCode opps = new InternalCode(CMMOperator.opps, grammarAnalysis.currentLine);
					grammarAnalysis.internalCodes.add(opps);
				}
				return CalculateResult.WARNING;
			case INT:
				if (rSymbol.isConstValue()) {
					grammarAnalysis.removeLastLoad();
					setConst(Calculator.opps(rSymbol.value), MemoryArea.INT_CON);
					grammarAnalysis.loadConstSymbol(this);
				} else {
					setRight(VariableType.INT);
					InternalCode opps = new InternalCode(CMMOperator.opps, grammarAnalysis.currentLine);
					grammarAnalysis.internalCodes.add(opps);
				}
				return CalculateResult.CORRECT;
			case REAL:
				if (rSymbol.isConstValue()) {
					grammarAnalysis.removeLastLoad();
					setConst(Calculator.opps(rSymbol.value), MemoryArea.REAL_CON);
					grammarAnalysis.loadConstSymbol(this);
				} else {
					setRight(VariableType.REAL);
					InternalCode opps = new InternalCode(CMMOperator.opps, grammarAnalysis.currentLine);
					grammarAnalysis.internalCodes.add(opps);
				}
				return CalculateResult.CORRECT;
			default:
				return CalculateResult.ERROR;
		}
	}

	/**
	 * Or:按位或操作,只要包含浮点值都要转换为INT值 布尔值与布尔值的结果为布尔值,其他为INT值
	 */
	public CalculateResult calcuOr(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_or(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addOrCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_or(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addOrCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_or(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addOrCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_or(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addOrCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_or(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addOrCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_or(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addOrCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_or(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addOrCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_or(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addOrCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_or(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addOrCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuMod(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol, CMMToken expToken) {
		if (rSymbol.isZero()) {
			// 除0错误
			GrammarError.devidedByZero(expToken);
			return CalculateResult.CORRECT;
		}
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mod(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addModCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mod(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addModCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mod(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addModCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mod(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addModCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mod(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addModCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mod(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addModCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mod(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addModCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mod(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addModCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.mod(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addModCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuPlus(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.plus(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addPlusCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.plus(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addPlusCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.plus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addPlusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.plus(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addPlusCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.plus(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addPlusCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.plus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addPlusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.plus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addPlusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.plus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addPlusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.plus(lSymbol.value, rSymbol.value), MemoryArea.REAL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addPlusCode(grammarAnalysis);
				setRight(VariableType.REAL);
			}
			return CalculateResult.CORRECT;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuRsh(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.rsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addRshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.rsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addRshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.rsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addRshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.rsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addRshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.rsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addRshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.rsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addRshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.rsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addRshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.rsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addRshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.rsh(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addRshCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		}
		return CalculateResult.ERROR;
	}

	public CalculateResult calcuTilde(GrammarAnalysis grammarAnalysis, Symbol rSymbol) {
		switch (rSymbol.variable.type) {
			case BOOLEAN:
				if (rSymbol.isConstValue()) {
					grammarAnalysis.removeLastLoad();
					setConst(Calculator.bit_not(rSymbol.value), MemoryArea.INT_CON);
					grammarAnalysis.loadConstSymbol(this);
				} else {
					setRight(VariableType.INT);
					addBitNotCode(grammarAnalysis);
				}
				return CalculateResult.WARNING;
			case INT:
				if (rSymbol.isConstValue()) {
					grammarAnalysis.removeLastLoad();
					setConst(Calculator.bit_not(rSymbol.value), MemoryArea.INT_CON);
					grammarAnalysis.loadConstSymbol(this);
				} else {
					setRight(VariableType.INT);
					addBitNotCode(grammarAnalysis);
				}
				return CalculateResult.CORRECT;
			case REAL:
				return CalculateResult.ERROR;
			default:
				return CalculateResult.ERROR;
		}
	}

	/**
	 * Xor:按位异或操作,只要包含浮点值都要转换为INT值 布尔值与布尔值的结果为布尔值,其他为INT值
	 */
	public CalculateResult calcuXor(GrammarAnalysis grammarAnalysis, Symbol lSymbol, Symbol rSymbol) {
		if (lSymbol.isBoolean() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_xor(lSymbol.value, rSymbol.value), MemoryArea.BOOL_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addXorCode(grammarAnalysis);
				setRight(VariableType.BOOLEAN);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_xor(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addXorCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isBoolean() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_xor(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addXorCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isInt() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_xor(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addXorCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_xor(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addXorCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.CORRECT;
		} else if (lSymbol.isInt() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_xor(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addXorCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isBoolean()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_xor(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addXorCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isInt()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_xor(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addXorCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		} else if (lSymbol.isReal() && rSymbol.isReal()) {
			if (lSymbol.isConstValue() && rSymbol.isConstValue()) {
				grammarAnalysis.removeLastLoad();
				setConst(Calculator.bit_xor(lSymbol.value, rSymbol.value), MemoryArea.INT_CON);
				grammarAnalysis.loadConstSymbol(this);
			} else {
				lSymbol.addXorCode(grammarAnalysis);
				setRight(VariableType.INT);
			}
			return CalculateResult.WARNING;
		}
		return CalculateResult.ERROR;
	}

	public void clone(Symbol s) {
		identifier = s.identifier;
		address = s.address;
		memoryArea = s.memoryArea;
		scopeDepth = s.scopeDepth;
		value = s.value;
		if (s.variable instanceof Function) {
			variable = new Function((Function) s.variable);
		} else {
			variable = new CMMVariable(s.variable);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}
		Symbol symbol = (Symbol) o;
		return (scopeDepth == symbol.scopeDepth) && identifier.equals(symbol.identifier) && variable.equals(symbol.variable);

	}

	@Override
	public int hashCode() {
		int result = identifier.hashCode();
		result = (31 * result) + scopeDepth;
		result = (31 * result) + variable.hashCode();
		return result;
	}

	public boolean isBoolean() {
		return this.variable.type == VariableType.BOOLEAN;
	}

	/**
	 * 是否常数
	 */
	public boolean isConstValue() {
		return (this.value != null) && !this.variable.left;
	}

	public boolean isInt() {
		return this.variable.type == VariableType.INT;
	}

	/**
	 * 是否左值
	 */
	public boolean isLeftValue() {
		return this.variable.left;
	}

	public boolean isReal() {
		return this.variable.type == VariableType.REAL;
	}

	/**
	 * 是否右值
	 */
	public boolean isRightValue() {
		return (this.value == null) && !this.variable.left;
	}

	public boolean isZero() {
		if (!this.isConstValue()) {
			return false;
		}
		if ((this.variable.type == VariableType.BOOLEAN) && !(boolean) this.value) {
			return true;
		}
		if ((this.variable.type == VariableType.INT) && ((int) this.value == 0)) {
			return true;
		}
		if ((this.variable.type == VariableType.REAL) && ((double) this.value == 0.0)) {
			return true;
		}
		return false;
	}

	public void setConst(Object value, MemoryArea aera) {
		this.value = value;
		this.memoryArea = aera;
		this.variable.left = false;
		this.variable.length = 0;
		switch (aera) {
			case INT_CON:
				this.variable.type = VariableType.INT;
				break;
			case BOOL_CON:
				this.variable.type = VariableType.BOOLEAN;
				break;
			case REAL_CON:
				this.variable.type = VariableType.REAL;
				break;
			default:
				break;
		}
	}

	public void setLeft() {
		this.value = null;
		this.variable.left = true;
	}

	public void setRight() {
		this.value = null;
		this.variable.left = false;
	}

	public void setRight(VariableType type) {
		this.value = null;
		this.variable.left = false;
		this.variable.type = type;
	}

	@Override
	public String toString() {
		return identifier + " addr:" + address + " dep:" + scopeDepth + " " + variable;
	}

}
