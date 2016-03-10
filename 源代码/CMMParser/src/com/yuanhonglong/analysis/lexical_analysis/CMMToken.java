package com.yuanhonglong.analysis.lexical_analysis;

import java.util.ArrayList;

import com.yuanhonglong.analysis.grammarAnalysis.GrammarAnalysis;
import com.yuanhonglong.analysis.grammarAnalysis.GrammarError;

/**
 * 词法分析,单个短语
 *
 * @author 天命剑主<br>
 *         on 2015/9/18.
 */
public class CMMToken {

	public String					word;			// 短语
	public TokenType				type;			// 短语类型
	public int						sourceLine;		// 源代码行数
	public String					lexicalError;	// 词法错误
	public int						offset;			// 在文档中的位置
	public ArrayList<GrammarError>	grammarErrors;	// 语法错误

	public CMMToken(String w, TokenType t, int l, int off) {
		this.word = w;
		this.type = t;
		this.sourceLine = l;
		this.lexicalError = null;
		this.offset = off;
		this.grammarErrors = new ArrayList<>();
	}

	@Override
	public String toString() {
		String tokenInfo = String.format("%s :%16s    Source Line : %3d", type.getTypeName(), word, sourceLine);
		if (isErrorToken()) {
			tokenInfo += "    " + lexicalError;
		}
		tokenInfo += "\n";
		return tokenInfo;

	}

	/**
	 * 短语是否包含错误
	 */
	public boolean hasErrors() {
		return this.hasLexicalErrors() || this.hasGrammarErrors();
	}

	/**
	 * 是否有语法错误(包含语法警告)
	 */
	public boolean hasGrammarErrors() {
		return this.grammarErrors.size() > 0;
	}

	/**
	 * 是否含有词法错误
	 */
	public boolean hasLexicalErrors() {
		return this.lexicalError != null;
	}

	/**
	 * 是否是语法警告
	 */
	public boolean isGrammarWarning() {
		if (this.grammarErrors.size() <= 0) {
			return false;
		}
		for (GrammarError error : grammarErrors) {
			if (!error.isWarning) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否是错误的短语
	 */
	public boolean isErrorToken() {
		return (this.type == TokenType.ERROR_NUMBER) || (this.type == TokenType.ERROR_STR) || (this.type == TokenType.UNKNOWN_SYMBOL);
	}

	public boolean in(String[] words) {
		for (String s : words) {
			if (word.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public boolean in(TokenType[] types) {
		for (TokenType t : types) {
			if (type == t) {
				return true;
			}
		}
		return false;
	}

	public boolean in(TokenType[] types, String[] words) {
		return in(words) || in(types);
	}

	/**
	 * @return 是否是表达式的起始短语或者初始化的起始短语
	 */
	public boolean startOfStatementExpressionOrDeclaration() {
		return in(new TokenType[] { TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER, TokenType.IDENTIFIER },
				new String[] { GrammarAnalysis.INT, GrammarAnalysis.BOOLEAN, GrammarAnalysis.REAL, GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET,
						GrammarAnalysis.INCREMENT, GrammarAnalysis.DECREMENT });
	}

	/**
	 * @return 是否是表达式的起始短语
	 */
	public boolean startOfStatementExpression() {
		return in(new TokenType[] { TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER, TokenType.IDENTIFIER },
				new String[] { GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET, GrammarAnalysis.INCREMENT, GrammarAnalysis.DECREMENT });
	}

	/**
	 * @return 是否是赋值表达式的起始短语
	 */
	public boolean startOfExpression() {
		return in(new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
				new String[] { GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET, GrammarAnalysis.LOGIC_NOT, GrammarAnalysis.BIT_NOT,
						GrammarAnalysis.PLUS, GrammarAnalysis.MINUS, GrammarAnalysis.INCREMENT, GrammarAnalysis.DECREMENT });
	}

	/**
	 * @return 是否是statement的开始
	 */
	public boolean startOfStatement() {
		return in(new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
				new String[] { GrammarAnalysis.L_LARGE_BRACKET, GrammarAnalysis.TRUE, GrammarAnalysis.FALSE,
						GrammarAnalysis.L_SMALL_BRACKET, GrammarAnalysis.INCREMENT, GrammarAnalysis.DECREMENT, GrammarAnalysis.IF, GrammarAnalysis.WHILE, GrammarAnalysis.FOR,
						GrammarAnalysis.BREAK, GrammarAnalysis.CONTINUE, GrammarAnalysis.RETURN, GrammarAnalysis.WRITE,
						GrammarAnalysis.READ, GrammarAnalysis.SEMICOLON });
	}

	/**
	 * @return 是否是variableDeclaration的开始
	 */
	public boolean startOfVariableDeclaration() {
		return in(new TokenType[] {},
				new String[] { GrammarAnalysis.INT, GrammarAnalysis.BOOLEAN, GrammarAnalysis.REAL });
	}

	/**
	 * @return 是否是Expression或者Str的开始
	 */
	public boolean startOfStrOrExpression() {
		return (this.type == TokenType.STR) || startOfExpression();
	}

	/**
	 * @return 是否是primaryExpression的开始
	 */
	public boolean startOfPrimaryExpression() {
		return in(new TokenType[] { TokenType.IDENTIFIER, TokenType.DECIMAL_NUMBER, TokenType.REAL_NUMBER },
				new String[] { GrammarAnalysis.TRUE, GrammarAnalysis.FALSE, GrammarAnalysis.L_SMALL_BRACKET });
	}
}
