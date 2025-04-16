package com.creda.coin.price.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author gavin
 * @since 2024-11-23
 */
@TableName("candle_trade")
@Data
public class CandleTrade implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户地址
     */
    private String account;

    /**
     * token地址
     */
    private String tokenAddress;

    /**
     * 粒度
     */
    private Long granularity;

    /**
     * 开始时间
     */
    private Long time;

    /**
     * 买入金额
     */
    private BigDecimal amount;

    /**
     * 卖出金额
     */
    private Long count;
    private int type;


}
