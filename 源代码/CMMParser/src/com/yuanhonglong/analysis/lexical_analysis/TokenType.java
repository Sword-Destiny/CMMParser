package com.yuanhonglong.analysis.lexical_analysis;

/**
 * 短语的类型
 *
 * @author 天命剑主<br>
 *         on 2015/9/18.
 */
public enum TokenType {
	UNKNOWN_SYMBOL("Unknown Symbol", "未知短语或符号"), // 未知短语或符号
	SPACE("Space         ", "空白符"), // 空白符
	SPECIAL_SYMBAL("Special Symbol", "特殊符号"), // 特殊符号
	IDENTIFIER("Identifier    ", "标识符"), // 标识符
	STR("String        ", "字符串"), // 字符串
	DECIMAL_NUMBER("Decimal Number", "十进制整数"), // 十进制整数
	ERROR_STR("Error String  ", "错误的字符串"), // 错误的字符串
	REAL_NUMBER("Real Number   ", "实数"), // 实数
	KEYWORD("Keyword       ", "关键字"), // 关键字
	LINE_COMMENT("Single Comment", "单行注释"), // 单行注释
	MULTI_COMMENT("Multi Comment ", "多行注释"), // 多行注释
	ERROR_NUMBER("Error Number  ", "错误的数字"), // 错误的数字
	PRE_PROCESS("Preprocessor  ", "预处理符"), // 预处理符
	;

	TokenType(String en, String cn) {
		this.chineseName = cn;
		this.englishName = en;
	}

	public String	chineseName;	// 中文名称
	public String	englishName;	// 英文名称

	/**
	 * 返回短语类型名称
	 */
	public String getTypeName() {
		return englishName;
	}

	/**
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return chineseName;
	}
}
