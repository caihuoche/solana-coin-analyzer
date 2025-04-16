package com.creda.coin.price.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SwapInstructionExtraInfo {
    private String token0;
    private String token1;
    private BigDecimal token0Amount;
    private BigDecimal token1Amount;
    private Integer parentInstructionIndex;
    private Integer innerInstructionIndex;
    private BigDecimal postAmount;
}
