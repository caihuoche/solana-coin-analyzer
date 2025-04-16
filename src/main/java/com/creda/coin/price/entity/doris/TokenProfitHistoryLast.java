package com.creda.coin.price.entity.doris;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@TableName("token_profit_history_last")
@Data
@NoArgsConstructor
public class TokenProfitHistoryLast implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识符
     */
    private String id;

    /**
     * 交易账户
     */
    private String account;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 区块高度
     */
    private Long blockHeight;

    /**
     * 区块时间
     */
    private Date blockTime;

    /**
     * 买入金额
     */
    private BigDecimal boughtAmount;

    /**
     * 买入次数
     */
    private Long boughtCount;

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 持有均价
     */
    private BigDecimal historicalHoldingAvgPrice;

    private BigDecimal holdingAvgPrice;

    /**
     * 是否盈利
     */
    private Integer isWin;

    /**
     * 已实现利润
     */
    private BigDecimal realizedProfit;

    /**
     * 投资回报率
     */
    private BigDecimal roi;

    private BigDecimal unrealizedRoi;

    private BigDecimal realizedRoi;

    /**
     * 卖出金额
     */
    private BigDecimal soldAmount;

    /**
     * 卖出均价
     */
    private BigDecimal historicalSoldAvgPrice;

    /**
     * 卖出次数
     */
    private Long soldCount;

    /**
     * 盈利卖出次数
     */
    private Long soldCountWin;

    /**
     * 代币地址
     */
    private String tokenAddress;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 总成本
     */
    private BigDecimal totalCost;

    /**
     * 转入金额
     */
    private BigDecimal transferInAmount;

    /**
     * 转入次数
     */
    private Long transferInCount;

    /**
     * 转出金额
     */
    private BigDecimal transferOutAmount;

    /**
     * 转出次数
     */
    private Long transferOutCount;

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 未实现利润
     */
    private BigDecimal unrealizedProfit;

    private BigDecimal totalBoughtAmountHasBeenLeft;

    private Long soldCountBoughtByUser;

    public void formatAmountFields() {
        this.amount = formatAmount(this.amount);
        this.totalAmount = formatAmount(this.totalAmount);
        this.soldAmount = formatAmount(this.soldAmount);
        this.boughtAmount = formatAmount(this.boughtAmount);
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
    public TokenProfitHistoryLast(TokenProfitHistory profitHistory) {
        this.id = profitHistory.getAccount() + "_" + profitHistory.getTokenAddress();
        this.account = profitHistory.getAccount();
        this.tokenAddress = profitHistory.getTokenAddress();
        this.type = profitHistory.getType();
        this.amount = profitHistory.getAmount();
        this.totalAmount = profitHistory.getTotalAmount();
        this.totalCost = profitHistory.getTotalCost();
        this.historicalHoldingAvgPrice = profitHistory.getHistoricalHoldingAvgPrice();
        this.holdingAvgPrice = profitHistory.getHoldingAvgPrice();
        this.realizedProfit = profitHistory.getRealizedProfit();
        this.unrealizedProfit = profitHistory.getUnrealizedProfit();
        this.currentPrice = profitHistory.getCurrentPrice();
        this.historicalSoldAvgPrice = profitHistory.getHistoricalSoldAvgPrice();
        this.soldAmount = profitHistory.getSoldAmount();
        this.isWin = profitHistory.getIsWin();
        this.blockTime = profitHistory.getBlockTime();
        this.blockHeight = profitHistory.getBlockHeight();
        this.txHash = profitHistory.getTxHash();
        this.roi = profitHistory.getRoi();
        this.unrealizedRoi = profitHistory.getUnrealizedRoi();
        this.realizedRoi = profitHistory.getRealizedRoi();
        this.soldCount = profitHistory.getSoldCount();
        this.soldCountWin = profitHistory.getSoldCountWin();
        this.boughtCount = profitHistory.getBoughtCount();
        this.boughtAmount = profitHistory.getBoughtAmount();
        this.transferInAmount = profitHistory.getTransferInAmount();
        this.transferOutAmount = profitHistory.getTransferOutAmount();
        this.transferInCount = profitHistory.getTransferInCount();
        this.transferOutCount = profitHistory.getTransferOutCount();
        this.totalBoughtAmountHasBeenLeft = profitHistory.getTotalBoughtAmountHasBeenLeft();
        this.soldCountBoughtByUser  = profitHistory.getSoldCountBoughtByUser();
    }

}
