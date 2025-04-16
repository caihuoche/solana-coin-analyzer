package com.creda.coin.price.util;

public class MainUtil {


    public static double calculateWinRatio(int sellCountWin, int sellCount) {
        return sellCount == 0 ? 0.0 : (double) sellCountWin / sellCount;
    }

	public static double roundToFourDecimalPlaces(int a, int b) {
		double value = (double) a / b;
		return Math.round(value * 10000.0) / 10000.0;
	}
}
