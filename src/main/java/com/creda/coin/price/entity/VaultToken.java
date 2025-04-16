package com.creda.coin.price.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName("vault_tokens")
@Data
public class VaultToken {
    @TableId(value = "vault_token_address")
    private String vaultTokenAddress;
    private String mintTokenAddress;
    private Integer mintTokenDecimals;
}
