package com.creda.coin.price.dto;

import com.creda.coin.price.entity.es.SolanaTransaction;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author gavin
 * @date 2024/10/21
 **/
@Data
public class Erc20HandlerDTO {

    private SolanaTransaction transaction;
    private String userAddress;
    private String tokenAddress;
    private SwapInstructionExtraInfo extraInfo;
    private BigDecimal transferAmountDecimal;
    private int type;

    public Erc20HandlerDTO(SolanaTransaction transaction, String userAddress, String tokenAddress, SwapInstructionExtraInfo extraInfo,BigDecimal transferAmountDecimal, int type) {
        this.transaction = transaction;
        this.userAddress = userAddress;
        this.tokenAddress = tokenAddress;
        this.extraInfo = extraInfo;
        this.transferAmountDecimal = transferAmountDecimal;
        this.type = type;
    }
}
