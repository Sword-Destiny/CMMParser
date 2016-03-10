package com.yuanhonglong.analysis.grammarAnalysis.symbol_table;

import java.util.ArrayList;

/**
 * 符号表
 * 
 * @author 天命剑主<br>
 *         on 2015/10/1.
 */
public class SymbolTable {

	public ArrayList<Symbol> symbols;

	public SymbolTable() {
		this.symbols = new ArrayList<>();
	}

	public void add(Symbol symbol) {
		this.symbols.add(symbol);
	}

	public boolean contains(Symbol obj) {
		return this.symbols.contains(obj);
	}

	public Symbol get(int i) {
		return this.symbols.get(i);
	}
}
