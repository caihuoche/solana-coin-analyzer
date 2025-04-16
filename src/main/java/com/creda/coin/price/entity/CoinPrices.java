package com.creda.coin.price.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author gavin
 * @since 2024-07-26
 */
@TableName("coin_prices")
@Data
public class CoinPrices implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String assetSymbol;
    private String assetAddress;
    private String closingPrice;
    private String priceInSol;
    private Date priceDate;
    private Integer assetType;
}
