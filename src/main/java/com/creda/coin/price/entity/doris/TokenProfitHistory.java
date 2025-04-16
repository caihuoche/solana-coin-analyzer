package com.creda.coin.price.entity.doris;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("token_profit_history")
public class TokenProfitHistory {

    private Long id;
    
    private String account;

    private String tokenAddress;

    private Integer type; // 1买入2卖出3transferin4transferout

    
    private BigDecimal amount; // varchar(50)

    
    private BigDecimal roi;

    private BigDecimal unrealizedRoi;

    private BigDecimal realizedRoi;

    
    private BigDecimal totalAmount; // varchar(50)

    
    private BigDecimal totalCost; // 总成本

    
    private BigDecimal historicalHoldingAvgPrice; // 持仓成本

    private BigDecimal holdingAvgPrice;

    
    private BigDecimal realizedProfit; // 已实现利润

    
    private BigDecimal unrealizedProfit; // 未实现利润

    
    private BigDecimal currentPrice; // 当前价格

    
    private BigDecimal historicalSoldAvgPrice; // 卖出均价

    
    private BigDecimal soldAmount; // varchar(50)

    
    private BigDecimal boughtAmount; // varchar(50)

    private Integer isWin;

    private Date blockTime;

    private Long blockHeight;

    
    private String txHash;

    private Long soldCount;

    private Long soldCountWin;

    private Long boughtCount;

    private Long transferInCount;

    private Long transferOutCount;

    private BigDecimal transferInAmount;

    private BigDecimal transferOutAmount;

    private Long soldCountBoughtByUser;

    private BigDecimal totalBoughtAmountHasBeenLeft;

    public void formatAmountFields() {
        this.amount = formatAmount(this.amount);
        this.totalAmount = formatAmount(this.totalAmount);
        this.boughtAmount = formatAmount(this.boughtAmount);
        this.soldAmount = formatAmount(this.soldAmount);
        this.totalCost = formatAmount(this.totalCost);
        this.historicalHoldingAvgPrice = formatAmount(this.historicalHoldingAvgPrice);
        this.realizedProfit = formatAmount(this.realizedProfit);
        this.unrealizedProfit = formatAmount(this.unrealizedProfit);
        this.currentPrice = formatAmount(this.currentPrice);
        this.historicalHoldingAvgPrice = formatAmount(this.historicalHoldingAvgPrice);
        this.holdingAvgPrice = formatAmount(this.holdingAvgPrice);
        this.historicalSoldAvgPrice = formatAmount(this.historicalSoldAvgPrice);
        this.transferInAmount = formatAmount(this.transferInAmount);
        this.transferOutAmount = formatAmount(this.transferOutAmount);
        this.totalBoughtAmountHasBeenLeft = formatAmount(this.totalBoughtAmountHasBeenLeft);
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
