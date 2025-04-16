package com.creda.coin.price.config;

import java.util.List;

import lombok.Data;

@Data
public class TokenConfig {
	// 存放多个 token 的合约地址
	private List<String> tokenAddresses;      // 启用计算的 token 合约地址列表
	private boolean enableMainCoinCalculation;// 是否启用主币的计算

}
