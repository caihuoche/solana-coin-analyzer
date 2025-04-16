package com.creda.coin.price.dto;

import lombok.Data;

@Data
public class TokenHoldersCountDTO {
	private String tokenAddress;
	private long holdersCount;  // 持有人数
}
