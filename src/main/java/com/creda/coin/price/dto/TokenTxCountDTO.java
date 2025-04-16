package com.creda.coin.price.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TokenTxCountDTO {
	private String assetAddress;
	private int buyCount;
	private int sellCount;
	private BigDecimal volume; // 交易额
}
