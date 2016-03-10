package com.yuanhonglong.analysis.grammarAnalysis;

import java.util.Arrays;

import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.ReturnType;
import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.VariableType;
import com.yuanhonglong.analysis.lexical_analysis.CMMToken;
import com.yuanhonglong.analysis.lexical_analysis.TokenType;

/**
 * 语法错误,语法警告
 *
 * @author 天命剑主 <br>
 *         on 2015年10月21日
 */
public class GrammarError {

	public enum GrammarErrorType {
		ARRAY_CAN_NOT_BE_ASSIGN, // 未预料的文件结尾
		ARRAY_CAN_NOT_BE_PARAMETER, // 未知符号
		ARRAY_LENGTH_TO_SMALL, // 需要的短语没有找到
		ARRAY_USE_AS_INT_CONST, // 未知的数据类型
		BREAK_JMP_NOWHERE, // 未知的返回类型
		CANNOT_DO_OP_ON_THE_TYPES, // 静态区溢出
		EXPECTED_WORD_NOT_SATISFIED, // 只有变量才有地址
		FUNCTION_UNDEFINE, // 返回类型不匹配
		NO_ARRAY_NO_SUB, // 重定义错误
		NO_CYCLE_NO_BREAK, // 数组不允许赋值
		NO_CYCLE_NO_CONTINUE, // 数组长度必须大于0
		ONLY_ARRAY_USE_LENGTH, // void类型不能为数组
		ONLY_LEFT_VALUE_ASSIGN, // 语句必须出现在函数中
		ONLY_LEFT_VALUE_CREMENT, // 未定义的函数
		ONLY_VARIABLE_HAS_ADDRESS, // 如果没有循环,就不能使用continue
		REDEFINE_ERROR, // 如果没有循环,就不能使用break
		RETURN_TYPE_NOT_MATCH, // break跳转地址未知
		STATEMENT_OUT_OF_FUNCTION, // 如果不是数组,就不能进行下标运算
		STATIC_AREA_OVERFLOW, // 只有左值才能自增或者自减
		UNDEFINED_IDENTIFIER, // 只有左值才能被赋值
		UNEXPECTED_EOF, // 在此操作上操作数变量类型不匹配
		UNKNOWN_EXPRESSION, // 未能解析的表达式
		UNKNOWN_RETURN_TYPE, // 该类型变量之间不能进行此操作
		UNKNOWN_VARIABLE_TYPE, // 数组不能作为参数
		UNMATCHED_SYMBOL, // 常量值没有地址
		VARIABLE_TYPE_NOT_MATCH, // 只有数组才能使用长度
		VARIABLE_TYPE_WARNING_ON_OP, // 将数组作为整数常量使用(即数组地址)
		VOID_TYPE_ARRAY, // 未定义的标识符
		DEVIDED_BY_ZERO,// 除0错误
	}

	public String			errorStr;		// 错误字符串
	public GrammarErrorType	grammarErrorID;	// 错误ID
	public boolean			isWarning;		// 是否是警告

	public GrammarError(String errorInfo, GrammarErrorType id, boolean warning) {
		this.errorStr = errorInfo;
		this.grammarErrorID = id;
		this.isWarning = warning;
	}

	/**
	 * 数组不能被赋值
	 *
	 * @param token
	 *            短语
	 */
	public static void arrayCanNotBeAssign(CMMToken token) {
		token.grammarErrors.add(new GrammarError("数组不能被赋值", GrammarErrorType.ARRAY_CAN_NOT_BE_ASSIGN, false));
	}

	/**
	 * 除0错误
	 *
	 * @param token
	 *            短语
	 */
	public static void devidedByZero(CMMToken token) {
		token.grammarErrors.add(new GrammarError("除0错误", GrammarErrorType.DEVIDED_BY_ZERO, false));
	}

	/**
	 * 数组不能作为参数
	 *
	 * @param token
	 *            短语
	 */
	public static void arrayCanNotBeParameter(CMMToken token) {
		token.grammarErrors.add(new GrammarError("数组不能作为参数", GrammarErrorType.ARRAY_CAN_NOT_BE_PARAMETER, false));
	}

	/**
	 * 数组长度必须大于0
	 *
	 * @param token
	 *            短语
	 */
	public static void arrayLengthNotLargerThanZero(CMMToken token) {
		token.grammarErrors.add(new GrammarError("数组长度必须大于0", GrammarErrorType.ARRAY_LENGTH_TO_SMALL, false));
	}

	/**
	 * break跳转地址未知
	 *
	 * @param token
	 *            短语
	 */
	public static void breakJmpNowhere(CMMToken token) {
		token.grammarErrors.add(new GrammarError("break跳转地址未知", GrammarErrorType.BREAK_JMP_NOWHERE, false));
	}

	/**
	 * 该类型变量上不能进行此操作
	 *
	 * @param token
	 *            短语
	 * @param op
	 *            操作符
	 * @param type
	 *            操作数
	 */
	public static void canNotDoOpOnTheTypes(CMMToken token, String op, VariableType type) {
		token.grammarErrors
				.add(new GrammarError("该类型变量上(" + type.toString() + ")不能进行此操作(" + op + ")", GrammarErrorType.CANNOT_DO_OP_ON_THE_TYPES, false));
	}

	/**
	 * 该类型变量之间不能进行此操作
	 *
	 * @param token
	 *            短语
	 * @param op
	 *            操作符
	 * @param lType
	 *            左操作数
	 * @param rType
	 *            右操作数
	 */
	public static void canNotDoOpOnTheTypes(CMMToken token, String op, VariableType lType, VariableType rType) {
		token.grammarErrors
				.add(new GrammarError("该类型变量之间(" + lType.toString() + "," + rType.toString() + ")不能进行此操作(" + op + ")", GrammarErrorType.CANNOT_DO_OP_ON_THE_TYPES, false));
	}

	/**
	 * 常量值没有地址
	 *
	 * @param token
	 *            短语
	 */
	public static void constValueHasNoAddress(CMMToken token) {
		token.grammarErrors.add(new GrammarError("常量值没有地址", GrammarErrorType.ONLY_VARIABLE_HAS_ADDRESS, false));
	}

	/**
	 * 需要的短语没有满足
	 *
	 * @param token
	 *            当前短语
	 * @param expectedTypes
	 *            需要的类型
	 * @param expectedWords
	 *            需要的词
	 */
	public static void expectedWordNotSatisfied(CMMToken token, TokenType[] expectedTypes, String[] expectedWords) {
		String string = "\" " + token.word + " \": " + "此处缺少";
		for (TokenType tokenType : expectedTypes) {
			string += " " + tokenType;
		}
		for (String word : expectedWords) {
			string += " " + word;
		}
		token.grammarErrors.add(new GrammarError(string, GrammarErrorType.EXPECTED_WORD_NOT_SATISFIED, false));
	}

	/**
	 * 未定义的函数
	 *
	 * @param token
	 *            短语
	 */
	public static void functionUndefine(CMMToken token) {
		token.grammarErrors.add(new GrammarError("未定义的函数", GrammarErrorType.FUNCTION_UNDEFINE, false));
	}

	/**
	 * 重定义错误
	 *
	 * @param token
	 *            短语
	 */
	public static void noArrayNoSub(CMMToken token) {
		token.grammarErrors.add(new GrammarError("如果不是数组,就不能进行下标运算", GrammarErrorType.NO_ARRAY_NO_SUB, false));
	}

	/**
	 * 如果没有循环,就不能使用break
	 *
	 * @param token
	 *            短语
	 */
	public static void noCycleNoBreak(CMMToken token) {
		token.grammarErrors.add(new GrammarError("如果没有循环,就不能使用break", GrammarErrorType.NO_CYCLE_NO_BREAK, false));
	}

	/**
	 * 如果没有循环,就不能使用continue
	 *
	 * @param token
	 *            短语
	 */
	public static void noCycleNoContinue(CMMToken token) {
		token.grammarErrors.add(new GrammarError("如果没有循环,就不能使用continue", GrammarErrorType.NO_CYCLE_NO_CONTINUE, false));
	}

	/**
	 * 只有数组才能使用长度
	 *
	 * @param token
	 *            短语
	 */
	public static void onlyArrayUseLength(CMMToken token) {
		token.grammarErrors.add(new GrammarError("只有数组才能使用长度", GrammarErrorType.ONLY_ARRAY_USE_LENGTH, false));
	}

	/**
	 * 只有左值才能被赋值
	 *
	 * @param token
	 *            短语
	 */
	public static void onlyLeftValueCanAssign(CMMToken token) {
		token.grammarErrors.add(new GrammarError("只有左值才能被赋值", GrammarErrorType.ONLY_LEFT_VALUE_ASSIGN, false));
	}

	/**
	 * 只有左值才能自增或者自减
	 *
	 * @param token
	 *            短语
	 */
	public static void onlyLeftValueCanCrement(CMMToken token) {
		token.grammarErrors.add(new GrammarError("只有左值才能自增或者自减", GrammarErrorType.ONLY_LEFT_VALUE_CREMENT, false));
	}

	/**
	 * 重定义错误
	 *
	 * @param token
	 *            短语
	 */
	public static void redefineError(CMMToken token) {
		token.grammarErrors.add(new GrammarError("重定义错误", GrammarErrorType.REDEFINE_ERROR, false));
	}

	/**
	 * 返回类型不匹配
	 *
	 * @param token
	 *            短语
	 */
	public static void returnTypeNotMatch(CMMToken token, VariableType errorType, ReturnType expectedType) {
		token.grammarErrors.add(new GrammarError("返回类型不匹配,需要的是 ' " + expectedType + " ',但是得到的是 " + errorType + " ",
				GrammarErrorType.RETURN_TYPE_NOT_MATCH, true));
	}

	/**
	 * 当语法错误发生时,需要跳过一些短语,查找需要识别的下一个短语
	 *
	 * @param grammarAnalysis
	 *            语法分析器
	 * @param i
	 *            -1:代表结束,若不是-1则代表下一个短语的index
	 * @param expectedTypes
	 *            期待的短语类型
	 * @param expectedWords
	 *            期待的短语
	 * @param afterTypes
	 *            期待的语法结构之后的短语类型
	 * @param afterWords
	 *            期待的语法结构之后的短语
	 * @param endTypes
	 *            代表当前语法结构结束的短语类型
	 * @param endWords
	 *            代表当前语法结构结束的短语
	 * @return 找到的是期待的短语还是之后的短语还是结束短语
	 */
	public static ErrorReturn skipTo(GrammarAnalysis grammarAnalysis, int i,
			final TokenType[] expectedTypes,
			final String[] expectedWords,
			final TokenType[] afterTypes,
			final String[] afterWords,
			final TokenType[] endTypes,
			final String[] endWords) {
		while (true) {
			if (i >= grammarAnalysis.length) {
				return new ErrorReturn(i, ErrorReturnType.END_TYPE);
			}
			CMMToken token = grammarAnalysis.getToken(i);
			if (token.in(expectedWords) || token.in(expectedTypes)) {
				return new ErrorReturn(i, ErrorReturnType.EXPECTED_TYPE);
			}
			if (token.in(afterTypes) || token.in(afterWords)) {
				return new ErrorReturn(/* (i - 1) < 0 ? 0 : */ i - 1, ErrorReturnType.AFTER_TYPE);
			}
			if (token.in(endTypes) || token.in(endWords)) {
				return new ErrorReturn(i, ErrorReturnType.END_TYPE);
			}
			token.lexicalError = "多余的短语: ' " + token.word + " '";
			grammarAnalysis.grammarTreeRecord(token.word);
			i = grammarAnalysis.tokenSkip(i);
		}
	}

	/**
	 * 语句必须出现在函数中
	 *
	 * @param token
	 *            短语
	 */
	public static void statementOutOfFunction(CMMToken token) {
		token.grammarErrors.add(new GrammarError("语句必须出现在函数中", GrammarErrorType.STATEMENT_OUT_OF_FUNCTION, false));
	}

	/**
	 * 静态区溢出
	 *
	 * @param token
	 *            短语
	 */
	public static void staticAreaOverflow(CMMToken token) {
		token.grammarErrors.add(new GrammarError("静态区溢出", GrammarErrorType.STATIC_AREA_OVERFLOW, false));
	}

	/**
	 * 未定义的标识符
	 *
	 * @param token
	 *            短语
	 */
	public static void undefinedIdentifier(CMMToken token) {
		token.grammarErrors.add(new GrammarError("未定义的标识符", GrammarErrorType.UNDEFINED_IDENTIFIER, false));
	}

	/**
	 * 未预料的文件结尾
	 *
	 * @param grammarAnalysis
	 *            语法分析器
	 * @param i
	 *            当前index
	 */
	public static void unexpectedEndOfFile(GrammarAnalysis grammarAnalysis, int i) {
		i = grammarAnalysis.tokenPreview(i);
		if ((i >= 0) && (i < grammarAnalysis.lexicalAnalysis.tokens.size())) {
			CMMToken token = grammarAnalysis.getToken(i);
			token.grammarErrors.add(new GrammarError("\" " + token.word + " \": " + "语法分析还没有完成,但是意外的遇到了文档结尾!", GrammarErrorType.UNEXPECTED_EOF, false));
		}
	}

	/**
	 * 未能解析的表达式
	 *
	 * @param token
	 *            短语
	 */
	public static void unknownExpression(CMMToken token) {
		token.grammarErrors.add(new GrammarError("未能解析的表达式", GrammarErrorType.UNKNOWN_EXPRESSION, false));
	}

	/**
	 * 未知的返回类型
	 *
	 * @param token
	 *            短语
	 */
	public static void unknownReturnType(CMMToken token) {
		token.grammarErrors.add(new GrammarError("未知的返回类型", GrammarErrorType.UNKNOWN_RETURN_TYPE, false));
	}

	/**
	 * 变量类型未知
	 *
	 * @param token
	 *            短语
	 */
	public static void unknownVariableType(CMMToken token) {
		token.grammarErrors.add(new GrammarError("变量类型未知,临时假设为int", GrammarErrorType.UNKNOWN_VARIABLE_TYPE, false));
	}

	/**
	 * 未匹配的符号
	 *
	 * @param token
	 *            当前短语
	 */
	public static void unmatchedSymbol(CMMToken token) {
		token.grammarErrors.add(new GrammarError("\" " + token.word + " \": " + "未匹配的符号  \" " + token.word + " \"", GrammarErrorType.UNMATCHED_SYMBOL, false));
	}

	/**
	 * 将数组作为整数常量使用(即数组地址)
	 *
	 * @param token
	 *            短语
	 */
	public static void useArrayAsIntConst(CMMToken token) {
		token.grammarErrors.add(new GrammarError("将数组作为整数常量使用(即数组地址)", GrammarErrorType.ARRAY_USE_AS_INT_CONST, true));
	}

	/**
	 * 变量类型不匹配
	 *
	 * @param token
	 *            短语
	 */
	public static void variableTypeNotMatch(CMMToken token, VariableType errorType, VariableType... expectedType) {
		token.grammarErrors.add(new GrammarError("变量类型不匹配,需要的是  " + Arrays.toString(expectedType) + " ,但是得到的是 '" + errorType + "' ",
				GrammarErrorType.VARIABLE_TYPE_NOT_MATCH, true));
	}

	/**
	 * 在此操作上操作数变量类型不匹配
	 *
	 * @param token
	 *            短语
	 * @param op
	 *            操作符
	 * @param type
	 *            操作数
	 */
	public static void variableTypeWarningOnOp(CMMToken token, String op, VariableType type) {
		token.grammarErrors
				.add(new GrammarError("在此操作上(" + op + ")操作数变量类型(" + type.toString() + ")不合适,将面临隐式类型装换", GrammarErrorType.VARIABLE_TYPE_WARNING_ON_OP, true));
	}

	/**
	 * 在此操作上操作数变量类型不匹配
	 *
	 * @param token
	 *            短语
	 * @param op
	 *            操作符
	 * @param lType
	 *            左操作数
	 * @param rType
	 *            右操作数
	 */
	public static void variableTypeWarningOnOp(CMMToken token, String op, VariableType lType, VariableType rType) {
		token.grammarErrors
				.add(new GrammarError("在此操作上(" + op + ")操作数变量类型(" + lType.toString() + "," + rType.toString() + ")不合适,将面临隐式类型装换", GrammarErrorType.VARIABLE_TYPE_WARNING_ON_OP,
						true));
	}

	/**
	 * void类型不能为数组
	 *
	 * @param token
	 *            短语
	 */
	public static void voidTypeCanNotBeArray(CMMToken token) {
		token.grammarErrors.add(new GrammarError("void类型不能为数组", GrammarErrorType.VOID_TYPE_ARRAY, false));
	}

	/**
	 * 返回语法错误信息
	 */
	public String getInfoStr() {
		return grammarErrorID + " :    " + errorStr;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getInfoStr();
	}

}
