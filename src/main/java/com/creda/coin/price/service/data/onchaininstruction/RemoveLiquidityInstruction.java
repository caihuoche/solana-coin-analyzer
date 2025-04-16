package com.creda.coin.price.service.data.onchaininstruction;

import com.syntifi.near.borshj.Borsh;
import com.syntifi.near.borshj.annotation.BorshField;

public class RemoveLiquidityInstruction implements Borsh {

    @BorshField(order = 1)
    public byte discriminator;
    @BorshField(order = 2)
    public long amount;

    public RemoveLiquidityInstruction() {}

    public RemoveLiquidityInstruction(byte discriminator, long amount) {
        this.discriminator = discriminator;
        this.amount = amount;
    }
}