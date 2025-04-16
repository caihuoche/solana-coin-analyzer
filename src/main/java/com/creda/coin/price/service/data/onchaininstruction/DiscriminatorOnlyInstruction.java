package com.creda.coin.price.service.data.onchaininstruction;

import com.syntifi.near.borshj.Borsh;
import com.syntifi.near.borshj.annotation.BorshField;

public class DiscriminatorOnlyInstruction implements Borsh {

    @BorshField(order = 1)
    public byte discriminator;

    public DiscriminatorOnlyInstruction() {}
  
    public DiscriminatorOnlyInstruction(byte discriminator) {
      this.discriminator = discriminator;
    }
  }