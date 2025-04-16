package com.creda.coin.price.dto;

import java.util.Date;

import lombok.Data;

@Data
public class AddressTransactionDTO {
    private String address;
    private Date txTime;

    // getters and setters
}