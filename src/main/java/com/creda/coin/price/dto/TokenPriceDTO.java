package com.creda.coin.price.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class TokenPriceDTO {
	private String assetAddress;
	private Long latestId;
	private BigDecimal currentPriceInSol;
	private Date timestamp;
}
