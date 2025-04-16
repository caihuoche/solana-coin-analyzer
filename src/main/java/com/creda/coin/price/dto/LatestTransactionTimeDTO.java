package com.creda.coin.price.dto;

import java.util.Date;

import lombok.Data;

@Data
public class LatestTransactionTimeDTO {
    private String address;
    private Date latestTxTime;

    // Getters and Setters
}
