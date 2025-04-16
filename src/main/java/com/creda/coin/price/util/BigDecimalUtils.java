
package com.creda.coin.price.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class BigDecimalUtils {
	// 设置精度和舍入模式
	static int scale = 18; // 设定精度（小数位数）
	static RoundingMode roundingMode = RoundingMode.HALF_UP; // 四舍五入


	public static BigDecimal divide(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
		if (bigDecimal1 == null || bigDecimal2 == null) {
			return new BigDecimal(0);
		}
		if (bigDecimal2.compareTo(BigDecimal.ZERO) == 0) {
//			log.info("除数不能为0 bigDecimal1:{},bigDecimal2:{}", bigDecimal1, bigDecimal2);
			throw new IllegalArgumentException("除数不能为0");
		}
		return bigDecimal1.divide(bigDecimal2, scale, roundingMode).stripTrailingZeros();
	}

	public static BigDecimal divideReturnZero(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
		if (bigDecimal1 == null || bigDecimal2 == null) {
			return new BigDecimal(0);
		}
		if (bigDecimal2.compareTo(BigDecimal.ZERO) == 0) {
//			log.info("除数不能为0 bigDecimal1:{},bigDecimal2:{}", bigDecimal1, bigDecimal2);
			return new BigDecimal(0);
		}
		return bigDecimal1.divide(bigDecimal2, scale, roundingMode).stripTrailingZeros();
	}


}
