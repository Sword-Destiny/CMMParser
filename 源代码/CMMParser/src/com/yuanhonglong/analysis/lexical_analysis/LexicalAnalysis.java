package com.yuanhonglong.analysis.lexical_analysis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * 词法分析器
 * 
 * @author 天命剑主<br>
 *         on 2015/9/18.
 */
public class LexicalAnalysis {

	public ArrayList<CMMToken>	tokens		= null;	// 分词结果保存在其中
	public int					currentLine	= 1;	// 当前源代码行

	public LexicalAnalysis() {
		tokens = new ArrayList<>();
	}

	// 词法分析
	public void startAnalysis(String sourceCode) {
		sourceCode = sourceCode.replace("\r", "");
		currentLine = 1;
		tokens.clear();
		char[] c = sourceCode.toCharArray();
		for (int i = 0; i < c.length; i++) {
			int currentOffset = i;
			if ((c[i] == SLASH) && ((i + 1) < c.length) && (c[i + 1] == SLASH)) {
				// 单行注释
				int index = sourceCode.indexOf(SKIP_NEW_LINE, i);
				if (index == -1) {
					index = c.length;
				} else {
					index += 1;
				}
				CMMToken token = new CMMToken(sourceCode.substring(i, index), TokenType.LINE_COMMENT, currentLine, currentOffset);
				tokens.add(token);
				i = index - 1;
				currentLine += 1;
			} else if ((c[i] == SLASH) && ((i + 1) < c.length) && (c[i + 1] == STAR)) {
				// 多行注释
				int index = sourceCode.indexOf(SKIP_COMMENT_END, i);
				if (index == -1) {
					index = c.length;
				} else {
					index += 2;
				}
				CMMToken token = new CMMToken(sourceCode.substring(i, index), TokenType.MULTI_COMMENT, currentLine, currentOffset);
				tokens.add(token);
				i = index - 1;
				for (int j = 0; j < token.word.length(); j++) {
					if (token.word.charAt(j) == SKIP_NEW_LINE) {
						currentLine++;
					}
				}
			} else if (c[i] == SHARP) {
				int index = sourceCode.indexOf(PRE_PROCESS_ADDRESS, i);
				if (index >= 0) {
					CMMToken token = new CMMToken(PRE_PROCESS_ADDRESS, TokenType.PRE_PROCESS, currentLine, currentOffset);
					tokens.add(token);
					i = (index + PRE_PROCESS_ADDRESS.length()) - 1;
					continue;
				}
				index = sourceCode.indexOf(PRE_PROCESS_LENGTH, i);
				if (index >= 0) {
					CMMToken token = new CMMToken(PRE_PROCESS_LENGTH, TokenType.PRE_PROCESS, currentLine, currentOffset);
					tokens.add(token);
					i = (index + PRE_PROCESS_LENGTH.length()) - 1;
					continue;
				}
				CMMToken token = new CMMToken("#", TokenType.UNKNOWN_SYMBOL, currentLine, currentOffset);
				tokens.add(token);
			} else if (isLetter(c[i]) || (c[i] == UNDER_LINE)) {
				// 关键字或者标识符
				i = keywordAndIdentifier(c, i);
			} else if (isSymbol(c[i])) {
				// 符号
				i = symbols(c, i);
			} else if (isNumber(c[i])) {
				// 数字
				i = numbers(c, i);
			} else if (c[i] == SKIP_NEW_LINE) {
				// 换行
				CMMToken token = new CMMToken("\n", TokenType.SPACE, currentLine, currentOffset);
				tokens.add(token);
				currentLine++;
			} else if (c[i] == STRING) {
				// 字符串
				i = str(c, i);
			} else if ((c[i] == SKIP_SPACE) || (c[i] == SKIP_TAB) || (c[i] == SKIP_RETURN)) {
				// 跳过空格和tab
				String s = "";
				s += c[i];
				CMMToken token = new CMMToken(s, TokenType.SPACE, currentLine, currentOffset);
				tokens.add(token);
			} else {
				String s = "";
				s += c[i];
				CMMToken token = new CMMToken(s, TokenType.UNKNOWN_SYMBOL, currentLine, currentOffset);
				token.lexicalError = "未定义字符 \" " + s + " \"";
				tokens.add(token);
			}
		}
	}

	// 字符串
	private int str(char[] c, int i) {
		int currentOffset = i;
		String word = "";
		word += '\"';
		boolean error = false;
		i++;
		CMMToken token;
		while (i < c.length) {
			if (c[i] == '\\') {
				if ((i + 1) >= c.length) {
					word += c[i];
					break;
				}
				switch (c[i + 1]) {
					case '\\':
					case 'r':
					case 'n':
					case 't':
					case 'b':
					case 'f':
					case '\"':
					case '\'':
						word += c[i];
						i++;
						word += c[i];
						break;
					default:
						word += c[i];
						i++;
						word += c[i];
						error = true;
						break;
				}
			} else if (c[i] == '\"') {
				word += c[i];
				break;
			} else if (c[i] == '\n') {
				token = new CMMToken(word, TokenType.ERROR_STR, currentLine, currentOffset);
				token.lexicalError = "未关闭的字符串";
				tokens.add(token);
				return i;
			} else {
				word += c[i];
			}
			i++;
		}
		if (i >= c.length) {
			token = new CMMToken(word, TokenType.ERROR_STR, currentLine, currentOffset);
			token.lexicalError = "未关闭的字符串";
			tokens.add(token);
			return i;
		} else {
			if (error) {
				token = new CMMToken(word, TokenType.ERROR_STR, currentLine, currentOffset);
				token.lexicalError = "字符串中包含未定义的转义字符";
			} else {
				token = new CMMToken(word, TokenType.STR, currentLine, currentOffset);
			}
			tokens.add(token);
			return i;
		}
	}

	// 数字
	private int numbers(char[] c, int i) {
		int currentOffset = i;
		CMMToken token;
		String numberStr = "";
		boolean isint = true;
		while ((i < c.length) && (isNumber(c[i]) || (c[i] == DOT))) {
			numberStr += c[i];
			if (c[i] == DOT) {
				isint = false;
			}
			i++;
		}
		if ((i < c.length) && (isLetter(c[i]) || (c[i] == UNDER_LINE))) {
			while ((i < c.length) && (isNumber(c[i]) || isLetter(c[i]) || (c[i] == UNDER_LINE))) {
				numberStr += c[i];
				i++;
			}
			token = new CMMToken(numberStr, TokenType.ERROR_NUMBER, currentLine, currentOffset);
			token.lexicalError = "数字中间或者末尾不能含有字母或者下划线";
			tokens.add(token);
			return i - 1;
		}
		if (isint) {
			BigInteger bigInteger = new BigInteger(numberStr);
			BigInteger maxInt = BigInteger.valueOf(Integer.MAX_VALUE);
			if (bigInteger.compareTo(maxInt) > 0) {
				token = new CMMToken(numberStr, TokenType.ERROR_NUMBER, currentLine, currentOffset);
				token.lexicalError = "数字太大,不能大于" + maxInt.toString();
				tokens.add(token);
			} else {
				token = new CMMToken(numberStr, TokenType.DECIMAL_NUMBER, currentLine, currentOffset);
				tokens.add(token);
			}
		} else {
			if (numberStr.indexOf(DOT) != numberStr.lastIndexOf('.')) {
				token = new CMMToken(numberStr, TokenType.ERROR_NUMBER, currentLine, currentOffset);
				token.lexicalError = "错误的浮点数";
				tokens.add(token);
			} else {
				BigDecimal bigDecimal = new BigDecimal(numberStr);
				BigDecimal maxDoble = BigDecimal.valueOf(Double.MAX_VALUE);
				if (bigDecimal.compareTo(maxDoble) > 0) {
					token = new CMMToken(numberStr, TokenType.ERROR_NUMBER, currentLine, currentOffset);
					token.lexicalError = "数字太大,不能大于" + maxDoble.toString();
					tokens.add(token);
				} else {
					token = new CMMToken(numberStr, TokenType.REAL_NUMBER, currentLine, currentOffset);
					tokens.add(token);
				}
			}
		}
		return i - 1;
	}

	// 特殊符号
	private int symbols(char[] c, int i) {
		int currentOffset = i;
		CMMToken token;
		switch (c[i]) {
			case L_LARGE_BRACKET:
				token = new CMMToken("{", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
				tokens.add(token);
				break;
			case R_LARGE_BRACKET:
				token = new CMMToken("}", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
				tokens.add(token);
				break;
			case L_SMALL_BRACKET:
				token = new CMMToken("(", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
				tokens.add(token);
				break;
			case R_SMALL_BRACKET:
				token = new CMMToken(")", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
				tokens.add(token);
				break;
			case L_MIDDLE_BRACKET:
				token = new CMMToken("[", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
				tokens.add(token);
				break;
			case R_MIDDLE_BRACKET:
				token = new CMMToken("]", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
				tokens.add(token);
				break;
			case SEMICOLON:
				token = new CMMToken(";", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
				tokens.add(token);
				break;
			case COMMA:
				token = new CMMToken(",", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
				tokens.add(token);
				break;
			case ASSIGN:
				if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(DOUBEL_EQUAL, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("=", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case GREATER_THAN:
				if (((i + 2) < c.length) && (c[i + 1] == GREATER_THAN) && (c[i + 2] == ASSIGN)) {
					token = new CMMToken(RSHIFTASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 2;
				} else if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(GREATER_EQUAL, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else if (((i + 1) < c.length) && (c[i + 1] == GREATER_THAN)) {
					token = new CMMToken(RSHIFT, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken(">", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case LESS_THAN:
				if (((i + 2) < c.length) && (c[i + 1] == LESS_THAN) && (c[i + 2] == ASSIGN)) {
					token = new CMMToken(LSHIFTASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 2;
				} else if (((i + 1) < c.length) && (c[i + 1] == LESS_THAN)) {
					token = new CMMToken(LSHIFT, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(LESS_EQUAL, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("<", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case LOGIC_NOT:
				if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(NOT_EQUAL, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("!", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case BIT_NOT:
				token = new CMMToken("~", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
				tokens.add(token);
				break;
			case PLUS:
				if (((i + 1) < c.length) && (c[i + 1] == PLUS)) {
					token = new CMMToken(INCREMENT, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(PLUSASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("+", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case MINUS:
				if (((i + 1) < c.length) && (c[i + 1] == MINUS)) {
					token = new CMMToken(DECREMENT, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(MINUSASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("-", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case STAR:
				if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(STARASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("*", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case SLASH:
				if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(SLASHASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("/", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case BIT_AND:
				if (((i + 1) < c.length) && (c[i + 1] == BIT_AND)) {
					token = new CMMToken(LOGIC_AND, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(ANDASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("&", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case BIT_OR:
				if (((i + 1) < c.length) && (c[i + 1] == BIT_OR)) {
					token = new CMMToken(LOGIC_OR, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(ORASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("|", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case BIT_XOR:
				if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(XORASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("^", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			case PERCENT:
				if (((i + 1) < c.length) && (c[i + 1] == ASSIGN)) {
					token = new CMMToken(PERCENTASSIGN, TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
					i += 1;
				} else {
					token = new CMMToken("%", TokenType.SPECIAL_SYMBAL, currentLine, currentOffset);
					tokens.add(token);
				}
				break;
			default:
				break;
		}
		return i;
	}

	// 关键字和标识符
	private int keywordAndIdentifier(char[] c, int i) {
		int currentOffset = i;
		String id = "";
		while ((i < c.length) && (isLetter(c[i]) || isNumber(c[i]) || (c[i] == UNDER_LINE))) {
			id += c[i];
			i++;
		}
		i--;
		if (isKeyword(id)) {
			CMMToken token = new CMMToken(id, TokenType.KEYWORD, currentLine, currentOffset);
			tokens.add(token);
		} else {
			CMMToken token = new CMMToken(id, TokenType.IDENTIFIER, currentLine, currentOffset);
			tokens.add(token);
		}
		return i;
	}

	// 是否关键字
	public static boolean isKeyword(String str) {
		return str.equals(TRUE) || str.equals(FALSE) || str.equals(READ) || str.equals(REAL) || str.equals(INT) || str.equals(BOOLEAN) || str.equals(VOID) || str.equals(IF)
				|| str.equals(ELSE) || str.equals(WRITE) || str.equals(WHILE) || str.equals(FOR) || str.equals(BREAK) || str.equals(CONTINUE) || str.equals(RETURN);
	}

	// 是否特殊符号
	public static boolean isSymbol(char c) {
		return (c == L_LARGE_BRACKET) || (c == R_LARGE_BRACKET) || (c == L_SMALL_BRACKET) || (c == R_SMALL_BRACKET) || (c == L_MIDDLE_BRACKET) || (c == R_MIDDLE_BRACKET)
				|| (c == SEMICOLON) || (c == COMMA) || (c == ASSIGN) || (c == GREATER_THAN) || (c == LESS_THAN) || (c == LOGIC_NOT) || (c == BIT_NOT) || (c == PLUS) || (c == MINUS)
				|| (c == STAR) || (c == SLASH) || (c == BIT_AND) || (c == BIT_OR) || (c == BIT_XOR) || (c == PERCENT);
	}

	// 是否字母
	public static boolean isLetter(char c) {
		return ((c <= 'z') && (c >= 'a')) || ((c <= 'Z') && (c >= 'A'));
	}

	// 是否数字
	public static boolean isNumber(char c) {
		return (c >= '0') && (c <= '9');
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (CMMToken token : tokens) {
			if (token.type == TokenType.SPACE) {
				continue;
			}
			builder.append(token.toString());
		}
		return builder.toString();
	}

	public static final char	SKIP_SPACE			= ' ';
	public static final char	SHARP				= '#';
	public static final char	SKIP_TAB			= '\t';
	public static final char	SKIP_RETURN			= '\r';
	public static final char	SKIP_NEW_LINE		= '\n';
	public static final char	UNDER_LINE			= '_';
	public static final String	SKIP_COMMENT_END	= "*/";
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
	public static final char	L_SMALL_BRACKET		= '(';
	public static final char	R_SMALL_BRACKET		= ')';
	public static final char	L_LARGE_BRACKET		= '{';
	public static final char	R_LARGE_BRACKET		= '}';
	public static final char	L_MIDDLE_BRACKET	= '[';
	public static final char	R_MIDDLE_BRACKET	= ']';
	public static final char	SEMICOLON			= ';';
	public static final char	COMMA				= ',';
	public static final char	DOT					= '.';
	public static final char	ASSIGN				= '=';
	public static final char	GREATER_THAN		= '>';
	public static final char	LESS_THAN			= '<';
	public static final char	LOGIC_NOT			= '!';
	public static final char	BIT_NOT				= '~';
	public static final String	DOUBEL_EQUAL		= "==";
	public static final String	LESS_EQUAL			= "<=";
	public static final String	GREATER_EQUAL		= ">=";
	public static final String	NOT_EQUAL			= "!=";
	public static final String	LOGIC_OR			= "||";
	public static final String	LOGIC_AND			= "&&";
	public static final String	INCREMENT			= "++";
	public static final String	DECREMENT			= "--";
	public static final char	PLUS				= '+';
	public static final char	MINUS				= '-';
	public static final char	STAR				= '*';
	public static final char	SLASH				= '/';
	public static final char	BIT_AND				= '&';
	public static final char	BIT_OR				= '|';
	public static final char	BIT_XOR				= '^';
	public static final char	PERCENT				= '%';
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
	public static final char	STRING				= '\"';
	public static final String	PRE_PROCESS_LENGTH	= "#length";
	public static final String	PRE_PROCESS_ADDRESS	= "#address";

}
