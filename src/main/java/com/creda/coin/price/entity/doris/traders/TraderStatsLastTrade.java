package com.creda.coin.price.entity.doris.traders;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@TableName("trader_stats_last_trade")
@Data
public class TraderStatsLastTrade implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 交易账户
     */
    private String account;

    /**
     * 区块高度
     */
    private Long blockHeight;

    /**
     * 区块时间
     */
    private Date blockTime;

    /**
     * 买入次数
     */
    private Long boughtCount;

    /**
     * 利润
     */
    private BigDecimal profit;

    /**
     * 已实现利润
     */
    private BigDecimal realizedProfit;

    /**
     * 投资回报率
     */
    private BigDecimal roi;

    /**
     * 卖出次数
     */
    private Long soldCount;

    /**
     * 盈利卖出次数
     */
    private Long soldCountWin;

    private Long soldCountBoughtByUser;

    /**
     * 交换次数
     */
    private Long swapCount;

    /**
     * 交易次数
     */
    private Long tradeCount;

    /**
     * 转账次数
     */
    private Long transferCount;

    /**
     * 转入次数
     */
    private Long transferInCount;

    /**
     * 转出次数
     */
    private Long transferOutCount;

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 未实现利润
     */
    private BigDecimal unrealizedProfit;

    /**
     * 胜率
     */
    private BigDecimal winRate;


}
