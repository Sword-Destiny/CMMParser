package com.yuanhonglong.analysis.grammarAnalysis.symbol_table;

/**
 * 返回变量
 * 
 * @author 天命剑主<br>
 *         on 2015/10/1.
 */
public class ReturnVariable {

	public ReturnType	type;
	public int			length;

	public ReturnVariable(ReturnType type) {
		this.type = type;
		this.length = 0;
	}

	public ReturnVariable(ReturnVariable rt) {
		this.length = rt.length;
		this.type = rt.type;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 331;
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
		ReturnVariable other = (ReturnVariable) obj;
		return (length == other.length) && (type == other.type);
	}

}
