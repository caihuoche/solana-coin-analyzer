package com.creda.coin.price.util;

import cn.hutool.core.lang.generator.SnowflakeGenerator;

/**
 * @author gavin
 * @date 2024/10/16
 **/
public class UniqueIdUtil {
    private static final SnowflakeGenerator snowflakeGenerator = new SnowflakeGenerator();
    public static long nextId() {
        return snowflakeGenerator.next();
    }
}
