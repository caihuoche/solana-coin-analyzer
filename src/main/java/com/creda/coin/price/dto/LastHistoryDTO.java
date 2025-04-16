package com.creda.coin.price.dto;

import lombok.Data;

/**
 * @author gavin
 * @date 2024/10/21
 **/
@Data
public class LastHistoryDTO {

    private String userAddress;
    private String tokenAddress;


    public LastHistoryDTO(String userAddress, String tokenAddress) {
        this.userAddress = userAddress;
        this.tokenAddress = tokenAddress;
    }
}
