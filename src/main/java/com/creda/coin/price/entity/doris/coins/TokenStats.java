package com.creda.coin.price.entity.doris.coins;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TokenStats{
    @TableId
    private String tokenAddress; // Meme coin 名称

    private BigDecimal traderPnl; // PnL 第一用户的收益

    private BigDecimal traderRoi; // PnL 第一用户的投资回报率

    private Long tradeCount; // 总交易数量

    private Long boughtCount; // 买入交易数量

    private Long soldCount; // 卖出交易数量

    private Long swapCount;

    private Long transferInCount; // transfer in交易数量

    private Long transferOutCount; // transfer out交易数量

    private Long transferCount;

    private BigDecimal marketCap; // 市值

    private BigDecimal liquidity; // DEX 流动性

    private Long holdersCount; // 持有人数

    private BigDecimal volume; // 交易额

    private BigDecimal currentPrice; // 当前价格

    private BigDecimal priceChange5m; // 过去5分钟涨跌幅

    private BigDecimal priceChange1h; // 过去1小时涨跌幅

    private BigDecimal priceChange24h; // 过去24小时涨跌幅

    private BigDecimal priceChange7d; // 过去7天涨跌幅

    private Long blockHeight;
    private Date blockTime;
}
