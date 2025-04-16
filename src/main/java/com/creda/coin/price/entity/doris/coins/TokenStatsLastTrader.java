package com.creda.coin.price.entity.doris.coins;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

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
@TableName("token_stats_last_trader")
@Data
public class TokenStatsLastTrader implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    /**
     * token地址
     */
    private String tokenAddress;

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
     * 卖出次数
     */
    private Long soldCount;


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
