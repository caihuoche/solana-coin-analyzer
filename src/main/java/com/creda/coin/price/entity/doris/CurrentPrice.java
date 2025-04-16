package com.creda.coin.price.entity.doris;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@TableName("current_price")
public class CurrentPrice implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    /**
     * token地址
     */
    private String tokenAddress;


    /**
     * 区块高度
     */
    private Long blockHeight;

    /**
     * 区块时间
     */
    private Date blockTime;

    /**
     * 当前价格
     */
    private BigDecimal price;


    /**
     * 交易哈希
     */
    private String txHash;



    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Date getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Date blockTime) {
        this.blockTime = blockTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    @Override
    public String toString() {
        return "CurrentPrice{" +
                ", blockHeight = " + blockHeight +
                ", blockTime = " + blockTime +
                ", price = " + price +
                ", tokenAddress = " + tokenAddress +
                ", txHash = " + txHash +
                "}";
    }
}
