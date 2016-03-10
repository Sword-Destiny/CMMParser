package com.yuanhonglong.analysis.grammarAnalysis.symbol_table;

import java.util.ArrayList;

/**
 * 函数 <br>
 *
 * @author 天命剑主<br>
 *         on 2015/10/1.
 */
public class Function extends CMMVariable {

	public ArrayList<CMMVariable>		parametersList;				// 参数列表
	public ReturnVariable				returnVariable;				// 返回类型
	public int							stackSize;					// 栈大小
	public ArrayList<VariableAddress>	localVariableAddressTable;	// 局部变量名称和地址,用于调试
	public String						name;						// 函数名称

	public Function(ReturnVariable returnVariable) {
		super(VariableType.FUNCTION);
		this.returnVariable = returnVariable;
		this.parametersList = new ArrayList<>();
		this.stackSize = 2;
		this.localVariableAddressTable = new ArrayList<>();
		this.name = "";
	}

	public VariableType returnTypeToVariableType() {
		switch (this.returnVariable.type) {
			case INT:
				return VariableType.INT;
			case BOOLEAN:
				return VariableType.BOOLEAN;
			case REAL:
				return VariableType.REAL;

			default:
				return VariableType.UNKNOWN;
		}
	}

	public Function(Function f) {
		super(f.type);
		this.name = f.name;
		this.length = f.length;
		this.left = f.left;
		this.stackSize = f.stackSize;
		this.returnVariable = new ReturnVariable(f.returnVariable);
		this.parametersList = new ArrayList<>();
		for (CMMVariable vt : f.parametersList) {
			CMMVariable variable = new CMMVariable(vt);
			this.parametersList.add(variable);
		}
		this.localVariableAddressTable = f.localVariableAddressTable;
	}

	public void addParameter(CMMVariable v) {
		this.parametersList.add(v);
	}

	public ReturnType getReturnType() {
		return returnVariable.type;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((parametersList == null) ? 0 : parametersList.hashCode());
		result = (prime * result) + ((returnVariable == null) ? 0 : returnVariable.hashCode());
		return result;
	}

	/**
	 * 只要参数个数相同就视为相同的函数
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Function other = (Function) obj;
		if (parametersList == null) {
			return other.parametersList == null;
		} else {
			return (other.parametersList != null) && (this.parametersList.size() == other.parametersList.size());
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "function";
	}

}
