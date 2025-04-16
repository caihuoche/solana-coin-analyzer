package com.creda.coin.price.util;

import java.math.BigInteger;

public class HexToDecimalConverter {
    public static void main(String[] args) {
        String hexValue = "0x1A";
        String decimalString = hexToNumberString(hexValue);
        System.out.println(decimalString); // Output: 300000000000000000000
    }

    public static String hexToNumberString(String hexValue) {
        if (hexValue.startsWith("0x")) {
            hexValue = hexValue.substring(2);
        }
        BigInteger bigInt = new BigInteger(hexValue, 16);
        return bigInt.toString(10);
    }

    public static BigInteger hexToNumber(String hexValue) {
        if (hexValue.startsWith("0x")) {
            hexValue = hexValue.substring(2);
        }
        return new BigInteger(hexValue, 16);
    }
}
