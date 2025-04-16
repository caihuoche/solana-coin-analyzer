package com.creda.coin.price.entity.doris.coins;

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
@TableName("token_stats_history")
@Data
public class TokenStatsHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;

    /**
     * 区块时间
     */
    private Date blockTime;

    /**
     * 区块高度
     */
    private Long blockHeight;

    /**
     * 买入次数
     */
    private Long boughtCount;

    /**
     * 卖出次数
     */
    private Long soldCount;

    /**
     * Token 地址
     */
    private String tokenAddress;

    /**
     * 转入次数
     */
    private Long transferInCount;

    /**
     * 转出次数
     */
    private Long transferOutCount;

    /**
     * 交易量
     */
    private BigDecimal volume;

}
