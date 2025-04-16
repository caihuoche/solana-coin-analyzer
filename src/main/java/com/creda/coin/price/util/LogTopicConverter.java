package com.creda.coin.price.util;

public class LogTopicConverter {

    public static String logTopicToAddress(String logTopic) {
        if (logTopic == null || logTopic.length() < 40) {
            throw new IllegalArgumentException("Invalid logTopic length");
        }
        // 提取最后的 40 个字符并在前面加上 "0x"
        String address = "0x" + logTopic.substring(logTopic.length() - 40);
        return address;
    }


}
