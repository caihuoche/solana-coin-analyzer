package com.creda.coin.price.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * <p>
 * 主币历史盈利
 * </p>
 *
 * @author gavin
 * @since 2024-08-26
 */
@TableName("profit_history")
@Data
public class ProfitHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String address;

    /**
     * 1买入2卖出3充值
     */
    private Integer type;

    /**
     * 当前交易的数量
     */
    private String balance;

    /**
     * 总成本
     */
    private String totalCost;

    /**
     * 总数量
     */
    private String totalBalance;

    /**
     * 持仓成本
     */
    private String holdingAvgPrice;

    /**
     * 已实现利润
     */
    private String realizedProfit;

    /**
     * 未实现利润
     */
    private String unrealizedProfit;
    private Integer isWin;

    /**
     * 卖出成本
     */
    private String currentPrice;

    /**
     * 交易时间
     */
    private Date txTime;

    /**
     * 交易区块
     */
    private Integer txBlock;
    private String txHash;
    private Double roi;

    /**
     * 卖出次数
     */
    private Integer sellCount;

    /**
     * 卖出胜利次数
     */
    private Integer sellCountWin;

    /**
     * 买入次数
     */
    private Integer buyCount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;


}
