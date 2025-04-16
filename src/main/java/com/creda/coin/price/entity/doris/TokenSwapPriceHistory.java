package com.creda.coin.price.entity.doris;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TokenSwapPriceHistory {
    private Long id;

    private String tokenAddress;

    private BigDecimal price;
    private Date blockTime;   // 交易时间

    private Long blockHeight; // 交易区块

    private Long txId;        // 交易ID

    private String txHash;    // 交易哈希
}
