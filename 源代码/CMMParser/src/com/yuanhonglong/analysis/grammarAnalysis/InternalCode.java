package com.yuanhonglong.analysis.grammarAnalysis;

import java.io.Serializable;

/**
 * 中间代码(四元式)
 *
 * @author 天命剑主<br>
 *         on 2015/9/28.
 */
public class InternalCode implements Serializable {

	private static final long	serialVersionUID	= -254505040528736473L;
	public CMMOperator			op;											// 操作符
	public Object				addrOrValue;								// 操作数地址或者值
	public MemoryArea			memeryArea;									// 操作数所在内存区域,寻址方式
	public Object				extra;										// 第四个额外的参数
	public int					lineNumber;									// 行数,用于调试

	public InternalCode(CMMOperator op, int lineNumber) {
		this(op, -1, MemoryArea.LOCAL, null, lineNumber);
	}

	public InternalCode(CMMOperator op, Object addrOrValue, int lineNumber) {
		this(op, addrOrValue, MemoryArea.LOCAL, null, lineNumber);
	}

	public InternalCode(CMMOperator op, Object addrOrValue, MemoryArea area, int lineNumber) {
		this(op, addrOrValue, area, null, lineNumber);
	}

	public InternalCode(CMMOperator op, Object addrOrValue, MemoryArea area, Object extra, int lineNumber) {
		this.op = op;
		this.addrOrValue = addrOrValue;
		this.memeryArea = area;
		this.extra = extra;
		this.lineNumber = lineNumber;
	}

	public String getHexAddressString() {
		if ((this.addrOrValue != null) && (this.addrOrValue instanceof Integer)) {
			return String.format("0x%x", addrOrValue);
		}
		return addrOrValue.toString();
	}

	public String getHexExtraString() {
		if ((this.extra != null) && (this.extra instanceof Integer)) {
			return String.format("0x%x", extra);
		}
		return extra.toString();
	}

	@Override
	public String toString() {
		String str = "";
		switch (op) {
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
				str += this.op;
				break;
			case write:
				str += this.op;
				if (this.memeryArea == MemoryArea.STR_CON) {
					str += "  " + this.getHexAddressString();
				}
				break;
			case space:
			case jmp:
			case pusht:
			case cvt:
				str += this.op + "  " + this.getHexAddressString();
				break;
			default:
				str += this.op + "  " + this.getHexAddressString() + "(" + this.memeryArea + ")";
				break;
		}
		return str + (this.extra == null ? "" : "," + this.getHexExtraString()) + "\n";
	}
}
