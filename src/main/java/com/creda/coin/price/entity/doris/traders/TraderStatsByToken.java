package com.creda.coin.price.entity.doris.traders;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName("trader_stats_by_token")
@Data
public class TraderStatsByToken implements Serializable {

    private static final long serialVersionUID = 1L;
    private String account;
    private String tokenAddress;

    private BigDecimal amount;

    private Long blockHeight;
    private Date blockTime;
    private String txHash;

    private BigDecimal boughtAmount;
    private long boughtCount;

    private BigDecimal holdingAvgPrice;

    private BigDecimal totalCost;

    private BigDecimal profit;
    private BigDecimal realizedProfit;
    private BigDecimal unrealizedProfit;

    private BigDecimal roi;
    private BigDecimal unrealizedRoi;
    private BigDecimal realizedRoi;

    private BigDecimal soldAmount;
    private BigDecimal soldAvgPrice;

    private BigDecimal winRate;
    private long soldCount;
    private long soldCountWin;
    private long soldCountBoughtByUser;
    private long swapCount;
    private long transferCount;
    private long transferInCount;
    private long transferOutCount;
    private long tradeCount;

    private BigDecimal transferInAmount;
    private BigDecimal transferOutAmount;
    private String partitionKey;
}
