package com.creda.coin.price.entity.doris.traders;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Data
public class TraderStats {
    private String account;   // 用户地址

    private BigDecimal roi;        // 截止1月前拥有的roi
    private BigDecimal winRate;    // 截止1月前拥有的winRate
    private BigDecimal profit;     // realizedProfit + unrealizedProfit
    private BigDecimal realizedProfit;
    private BigDecimal unrealizedProfit;

    private long tradeCount;

    private long boughtCount;
    private long soldCount;
    private long soldCountBoughtByUser;
    private long soldCountWin;
    private long swapCount;

    private long transferInCount;
    private long transferOutCount;
    private long transferCount;

    private Long blockHeight;
    private String txHash;
    private Date blockTime;

    public void formatAllAmounts() {
        this.profit = formatAmount(this.profit);
        this.realizedProfit = formatAmount(this.realizedProfit);
        this.unrealizedProfit = formatAmount(this.unrealizedProfit);
    }

    private BigDecimal formatAmount(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return null;
        }
        // 设置为18位小数
        bigDecimal = bigDecimal.setScale(18, RoundingMode.DOWN);

        // 去除无意义的零
        return bigDecimal.stripTrailingZeros();
    }

}
