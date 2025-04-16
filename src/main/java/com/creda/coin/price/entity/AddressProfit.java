package com.creda.coin.price.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户最新盈利
 * </p>
 *
 * @author gavin
 * @since 2024-08-26
 */
@Data
@TableName("address_profit")
public class AddressProfit implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String address;
    private String assetAddress;

    /**
     * 总数量
     */
    private String balance;

    /**
     * 利润
     */
    private String totalProfit;
    private String totalProfitInSol;
    private Double roi;
    /**
     * 胜率
     */
    private Double winRatio;

    /**
     * 高度
     */
    private Integer blockHeight;

    /**
     * 高度时间
     */
    private Date blockTime;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;


}
