package com.creda.coin.price.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("account_owners")
public class AccountOwner implements Serializable {
    @TableId(value = "owner")
	private String owner;
    private String account;
}
