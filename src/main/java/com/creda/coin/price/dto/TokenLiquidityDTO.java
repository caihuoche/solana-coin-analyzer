package com.creda.coin.price.dto;

import lombok.Data;

@Data
public class TokenLiquidityDTO {
    private String token0Address;
    private String token1Address;
    private String token0Balance;
    private String token1Balance;

    // getters and setters
}
