package com.creda.coin.price.test;

public class TokenBalance {
    private long accountIndex;
    private String owner;
    private String mint;
    private long amount;
    private long decimals;
    private long uiAmount;

    public TokenBalance(long accountIndex, String owner, String mint, long amount, long decimals, long uiAmount) {
        this.accountIndex = accountIndex;
        this.owner = owner;
        this.mint = mint;
        this.amount = amount;
        this.decimals = decimals;
        this.uiAmount = uiAmount;
    }

    // Getter方法（可以根据需要添加）
    public long getAccountIndex() {
        return accountIndex;
    }

    public String getOwner() {
        return owner;
    }

    public String getMint() {
        return mint;
    }

    public long getAmount() {
        return amount;
    }

    public long getDecimals() {
        return decimals;
    }

    public long getUiAmount() {
        return uiAmount;
    }
}
