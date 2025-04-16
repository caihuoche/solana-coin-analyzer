package com.creda.coin.price.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2024-09-05
 */
@Data
public class Candle implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * token地址
     */
    private String address;

    /**
     * 粒度
     */
    private Long granularity;

    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 最高价
     */
    private BigDecimal high;

    /**
     * 开盘价
     */
    private BigDecimal open;

    /**
     * 收盘价
     */
    private BigDecimal close;

    /**
     * 开始时间
     */
    private Long time;

    private Date timeDate;
}
