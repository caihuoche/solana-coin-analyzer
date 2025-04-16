package com.creda.coin.price.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName("raydium_swap_pools")
@Data
public class RaydiumSwapPool implements Serializable {
    @TableId(value = "pair_address")
    private String pairAddress;
    private String baseToken;
    private String baseVault;

    private String quoteToken;
    private String quoteVault;

    private String programId;
    private String type; // type: 'Standard' | 'Concentrated'
    private Date createdAt;
    private Date updatedAt;
}
