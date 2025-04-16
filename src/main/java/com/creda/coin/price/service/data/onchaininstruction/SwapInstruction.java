package com.creda.coin.price.service.data.onchaininstruction;

import org.bouncycastle.util.test.FixedSecureRandom.BigInteger;

import com.syntifi.near.borshj.Borsh;
import com.syntifi.near.borshj.annotation.BorshField;

public class SwapInstruction implements Borsh {
    @BorshField(order = 1)
    public long discriminator;
    @BorshField(order = 2)
    public long amount;
    @BorshField(order = 3)
    public long otherAmountThreshold;
    @BorshField(order = 4)
    public BigInteger sqrtPriceLimitX64;
    @BorshField(order = 5)
    public boolean isBaseInput;

    public SwapInstruction() {}
  
    public SwapInstruction(long discriminator, long amount, long otherAmountThreshold, BigInteger sqrtPriceLimitX64, boolean isBaseInput) {
      this.discriminator = discriminator;
      this.amount = amount;
      this.otherAmountThreshold = otherAmountThreshold;
      this.sqrtPriceLimitX64 = sqrtPriceLimitX64;
      this.isBaseInput = isBaseInput;
    }
}