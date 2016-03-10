package com.yuanhonglong.run;

import com.yuanhonglong.analysis.grammarAnalysis.symbol_table.VariableType;

/**
 * 计算类
 * 
 * @author 天命剑主<br>
 *         on 2015年10月10日
 */
public class Calculator {
	/**
	 * 除法
	 */
	public static Object div(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (((boolean) l ? 1 : 0) / ((boolean) r ? 1 : 0)) != 0;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) / (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) / (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l / ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l / (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l / (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l / ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l / (int) r;
		} else {
			return (double) l / (double) r;
		}
	}

	/**
	 * 数据传送
	 */
	public static Object mov(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return r;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return (int) r != 0;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return (double) r != 0.0;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return ((boolean) r ? 1.0 : 0.0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) (int) r;
		} else if ((l instanceof Double) && (r instanceof Double)) {
			return r;
		} else {
			return r;
		}
	}

	/**
	 * 数据类型转换
	 */
	public static Object cvt(VariableType l, Object r) {
		if ((l == VariableType.BOOLEAN) && (r instanceof Boolean)) {
			return r;
		} else if ((l == VariableType.BOOLEAN) && (r instanceof Integer)) {
			return (int) r != 0;
		} else if ((l == VariableType.BOOLEAN) && (r instanceof Double)) {
			return (double) r != 0.0;
		} else if ((l == VariableType.INT) && (r instanceof Boolean)) {
			return ((boolean) r ? 1 : 0);
		} else if ((l == VariableType.INT) && (r instanceof Integer)) {
			return r;
		} else if ((l == VariableType.INT) && (r instanceof Double)) {
			return (int) (double) r;
		} else if ((l == VariableType.REAL) && (r instanceof Boolean)) {
			return ((boolean) r ? 1.0 : 0.0);
		} else if ((l == VariableType.REAL) && (r instanceof Integer)) {
			return (double) (int) r;
		} else {
			return r;
		}
	}

	/**
	 * 右移
	 */
	public static int rsh(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (((boolean) l ? 1 : 0) >> ((boolean) r ? 1 : 0));
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) >> (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) >> (int) (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l >> ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l >> (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l >> (int) (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (int) (double) l >> ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (int) (double) l >> (int) r;
		} else {
			return (int) (double) l >> (int) (double) r;
		}
	}

	/**
	 * lt 小于
	 */
	public static boolean lt(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (((boolean) l ? 1 : 0) < ((boolean) r ? 1 : 0));
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) < (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) < (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l < ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l < (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l < (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l < ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l < (int) r;
		} else {
			return (double) l < (double) r;
		}
	}

	/**
	 * eq等于
	 */
	public static boolean eq(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (boolean) l == (boolean) r;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) == (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) == (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l == ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l == (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l == (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l == ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l == (int) r;
		} else {
			return (double) l == (double) r;
		}
	}

	/**
	 * logicAnd,逻辑与操作
	 */
	public static boolean logic_and(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (boolean) l & (boolean) r;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return (boolean) l & ((int) r == 0);
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return (boolean) l & ((double) r == 0.0);
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return ((int) l == 0) & (boolean) r;
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return ((int) l == 0) & ((int) r == 0);
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return ((int) l == 0) & ((double) r == 0.0);
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return ((double) l == 0.0) & (boolean) r;
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return ((double) l == 0.0) & ((int) r == 0);
		} else {
			return ((double) l == 0.0) & ((double) r == 0.0);
		}
	}

	/**
	 * logicOr,逻辑或操作
	 */
	public static boolean logic_or(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (boolean) l | (boolean) r;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return (boolean) l | ((int) r == 0);
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return (boolean) l | ((double) r == 0.0);
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return ((int) l == 0) | (boolean) r;
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return ((int) l == 0) | ((int) r == 0);
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return ((int) l == 0) | ((double) r == 0.0);
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return ((double) l == 0.0) | (boolean) r;
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return ((double) l == 0.0) | ((int) r == 0);
		} else {
			return ((double) l == 0.0) | ((double) r == 0.0);
		}
	}

	/**
	 * and,按位与操作
	 */
	public static Object bit_and(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (boolean) l & (boolean) r;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) & (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) & (int) (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l & ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l & (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l & (int) (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (int) (double) l & ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (int) (double) l & (int) r;
		} else {
			return (int) (double) l & (int) (double) r;
		}
	}

	/**
	 * or,按位或操作
	 */
	public static Object bit_or(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (boolean) l | (boolean) r;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) | (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) | (int) (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l | ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l | (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l | (int) (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (int) (double) l | ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (int) (double) l | (int) r;
		} else {
			return (int) (double) l | (int) (double) r;
		}
	}

	/**
	 * xor,按位异或操作
	 */
	public static Object bit_xor(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (boolean) l ^ (boolean) r;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) ^ (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) ^ (int) (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l ^ ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l ^ (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l ^ (int) (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (int) (double) l ^ ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (int) (double) l ^ (int) r;
		} else {
			return (int) (double) l ^ (int) (double) r;
		}
	}

	/**
	 * ne不等于
	 */
	public static boolean ne(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (boolean) l != (boolean) r;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) != (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) != (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l != ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l != (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l != (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l != ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l != (int) r;
		} else {
			return (double) l != (double) r;
		}
	}

	/**
	 * le 小于等于
	 */
	public static boolean le(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (((boolean) l ? 1 : 0) <= ((boolean) r ? 1 : 0));
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) <= (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) <= (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l <= ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l <= (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l <= (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l <= ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l <= (int) r;
		} else {
			return (double) l <= (double) r;
		}
	}

	/**
	 * ge 大于等于
	 */
	public static boolean ge(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (((boolean) l ? 1 : 0) >= ((boolean) r ? 1 : 0));
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) >= (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) >= (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l >= ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l >= (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l >= (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l >= ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l >= (int) r;
		} else {
			return (double) l >= (double) r;
		}
	}

	/**
	 * gt 大于
	 */
	public static boolean gt(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (((boolean) l ? 1 : 0) > ((boolean) r ? 1 : 0));
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) > (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) > (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l > ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l > (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l > (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l > ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l > (int) r;
		} else {
			return (double) l > (double) r;
		}
	}

	/**
	 * 左移
	 */
	public static int lsh(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (((boolean) l ? 1 : 0) << ((boolean) r ? 1 : 0));
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) << (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) << (int) (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l << ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l << (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l << (int) (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (int) (double) l << ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (int) (double) l << (int) r;
		} else {
			return (int) (double) l << (int) (double) r;
		}
	}

	/**
	 * 乘法
	 */
	public static Object mul(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (((boolean) l ? 1 : 0) * ((boolean) r ? 1 : 0)) != 0;
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) * (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) * (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l * ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l * (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l * (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l * ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l * (int) r;
		} else {
			return (double) l * (double) r;
		}
	}

	/**
	 * 加法
	 */
	public static Object plus(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return ((boolean) l ? 1 : 0) + ((boolean) r ? 1 : 0);
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) + (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) + (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l + ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l + (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l + (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l + ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l + (int) r;
		} else {
			return (double) l + (double) r;
		}
	}

	/**
	 * 减法
	 */
	public static Object minus(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return ((boolean) l ? 1 : 0) - ((boolean) r ? 1 : 0);
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) - (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) - (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l - ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l - (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l - (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (double) l - ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (double) l - (int) r;
		} else {
			return (double) l - (double) r;
		}
	}

	/**
	 * 求余
	 */
	public static int mod(Object l, Object r) {
		if ((l instanceof Boolean) && (r instanceof Boolean)) {
			return (((boolean) l ? 1 : 0) % ((boolean) r ? 1 : 0));
		} else if ((l instanceof Boolean) && (r instanceof Integer)) {
			return ((boolean) l ? 1 : 0) % (int) r;
		} else if ((l instanceof Boolean) && (r instanceof Double)) {
			return ((boolean) l ? 1 : 0) % (int) (double) r;
		} else if ((l instanceof Integer) && (r instanceof Boolean)) {
			return (int) l % ((boolean) r ? 1 : 0);
		} else if ((l instanceof Integer) && (r instanceof Integer)) {
			return (int) l % (int) r;
		} else if ((l instanceof Integer) && (r instanceof Double)) {
			return (int) l % (int) (double) r;
		} else if ((l instanceof Double) && (r instanceof Boolean)) {
			return (int) (double) l % ((boolean) r ? 1 : 0);
		} else if ((l instanceof Double) && (r instanceof Integer)) {
			return (int) (double) l % (int) r;
		} else {
			return (int) (double) l % (int) (double) r;
		}
	}

	/**
	 * 逻辑非
	 */
	public static boolean logic_not(Object r) {
		if (r instanceof Boolean) {
			return !(boolean) r;
		} else if (r instanceof Integer) {
			return !((int) r == 0);
		} else {
			return !((double) r == 0.0);
		}
	}

	/**
	 * 按位取反
	 */
	public static int bit_not(Object r) {
		if (r instanceof Boolean) {
			return ~((boolean) r ? 1 : 0);
		} else if (r instanceof Integer) {
			return ~((int) r);
		} else {
			return ~(int) (double) r;
		}
	}

	/**
	 * opps:相反数
	 */
	public static Object opps(Object r) {
		if (r instanceof Boolean) {
			return -((boolean) r ? 1 : 0);
		} else if (r instanceof Integer) {
			return -(int) r;
		} else {
			return -((int) (double) r);
		}
	}

	/**
	 * inc:递增
	 */
	public static Object inc(Object r) {
		if (r instanceof Boolean) {
			return ((boolean) r ? 1 : 0) + 1;
		} else if (r instanceof Integer) {
			return (int) r + 1;
		} else {
			return ((int) (double) r) + 1;
		}
	}

	/**
	 * dec:递减
	 */
	public static Object dec(Object r) {
		if (r instanceof Boolean) {
			return ((boolean) r ? 1 : 0) - 1;
		} else if (r instanceof Integer) {
			return (int) r - 1;
		} else {
			return ((int) (double) r) - 1;
		}
	}

	/**
	 * 判断是否为0
	 */
	public static boolean isZero(Object r) {
		if (r instanceof Boolean) {
			return !((boolean) r);
		} else if (r instanceof Integer) {
			return (int) r == 0;
		} else if (r instanceof Double) {
			return (double) r == 0.0;
		}
        return false;
	}

}
