package com.creda.coin.price.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UserProfitDTO {
	// 用户地址
	private String address;

	// 投资回报率 ROI
	private BigDecimal roi;

	private BigDecimal pnl;


}
