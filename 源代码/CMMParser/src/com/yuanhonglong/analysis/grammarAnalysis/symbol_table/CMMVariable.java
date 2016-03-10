package com.yuanhonglong.analysis.grammarAnalysis.symbol_table;

/**
 * 符号表变量<br>
 * 
 * @author 天命剑主<br>
 *         on 2015/10/1.
 */
public class CMMVariable {

	public VariableType	type;	// 变量类型
	public int			length;	// 长度
	public boolean		left;	// 是否左值

	public CMMVariable(VariableType type, int length) {
		this.type = type;
		this.length = length;
		if (length > 0) {
			left = false;
		} else {
			switch (type) {
				case INT:
				case BOOLEAN:
				case REAL:
					// case ALL:
					left = true;
					break;
				default:
					left = false;
					break;
			}
		}
	}

	public CMMVariable(CMMVariable e) {
		this.type = e.type;
		this.length = e.length;
		this.left = e.left;
	}

	public boolean isVariable() {
		switch (type) {
			case INT:
			case REAL:
			case BOOLEAN:
				return true;
			default:
				return false;
		}
	}

	public CMMVariable(VariableType type) {
		this(type, 0);
	}

	@Override
	public String toString() {
		return type + " " + "length=" + length;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + length;
		result = (prime * result) + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
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
		CMMVariable other = (CMMVariable) obj;
		return (length == other.length) && (type == other.type);
	}

}
