package com.creda.coin.price.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author gavin
 * @since 2024-11-16
 */
@TableName("solana_transactions")
public class SolanaTransactions implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Date blockTime;
    private Long slot;
    private Long blockHeight;
    private Integer index;
    private String hash;
    private Long fee;
    private Object preBalances;
    private Object postBalances;
    private String preTokenBalances;
    private String postTokenBalances;
    private Object accountKeys;
    private String instructions;
    private String innerInstructions;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Date blockTime) {
        this.blockTime = blockTime;
    }

    public Long getSlot() {
        return slot;
    }

    public void setSlot(Long slot) {
        this.slot = slot;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Object getPreBalances() {
        return preBalances;
    }

    public void setPreBalances(Object preBalances) {
        this.preBalances = preBalances;
    }

    public Object getPostBalances() {
        return postBalances;
    }

    public void setPostBalances(Object postBalances) {
        this.postBalances = postBalances;
    }

    public String getPreTokenBalances() {
        return preTokenBalances;
    }

    public void setPreTokenBalances(String preTokenBalances) {
        this.preTokenBalances = preTokenBalances;
    }

    public String getPostTokenBalances() {
        return postTokenBalances;
    }

    public void setPostTokenBalances(String postTokenBalances) {
        this.postTokenBalances = postTokenBalances;
    }

    public Object getAccountKeys() {
        return accountKeys;
    }

    public void setAccountKeys(Object accountKeys) {
        this.accountKeys = accountKeys;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getInnerInstructions() {
        return innerInstructions;
    }

    public void setInnerInstructions(String innerInstructions) {
        this.innerInstructions = innerInstructions;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "SolanaTransactions{" +
        ", id = " + id +
        ", blockTime = " + blockTime +
        ", slot = " + slot +
        ", blockHeight = " + blockHeight +
        ", index = " + index +
        ", hash = " + hash +
        ", fee = " + fee +
        ", preBalances = " + preBalances +
        ", postBalances = " + postBalances +
        ", preTokenBalances = " + preTokenBalances +
        ", postTokenBalances = " + postTokenBalances +
        ", accountKeys = " + accountKeys +
        ", instructions = " + instructions +
        ", innerInstructions = " + innerInstructions +
        ", createdAt = " + createdAt +
        ", updatedAt = " + updatedAt +
        "}";
    }
}
