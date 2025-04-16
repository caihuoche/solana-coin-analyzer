package com.creda.coin.price.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 * 
 * </p>
 *
 * @author gavin
 * @since 2024-07-24
 */
@TableName("token_balance_history")
public class TokenBalanceHistory implements Serializable {

    private static final long serialVersionUID = 1L;
	private String lpTokenAddress;

    private Integer blockHeight;
    private String token0Balance;
    private String token1Balance;
    private String token0Address;
    private String token1Address;
    private Date createdAt;
    private Date updatedAt;

	public String getLpTokenAddress() {
		return lpTokenAddress;
	}

	public void setLpTokenAddress(String lpTokenAddress) {
		this.lpTokenAddress = lpTokenAddress;
	}

	public Integer getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Integer blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getToken0Balance() {
        return token0Balance;
    }

    public void setToken0Balance(String token0Balance) {
        this.token0Balance = token0Balance;
    }

    public String getToken1Balance() {
        return token1Balance;
    }

    public void setToken1Balance(String token1Balance) {
        this.token1Balance = token1Balance;
    }

    public String getToken0Address() {
        return token0Address;
    }

    public void setToken0Address(String token0Address) {
        this.token0Address = token0Address;
    }

    public String getToken1Address() {
        return token1Address;
    }

    public void setToken1Address(String token1Address) {
        this.token1Address = token1Address;
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
        return "TokenBalanceHistory{" +
        ", blockHeight = " + blockHeight +
        ", token0Balance = " + token0Balance +
        ", token1Balance = " + token1Balance +
        ", token0Address = " + token0Address +
        ", token1Address = " + token1Address +
        ", createdAt = " + createdAt +
        ", updatedAt = " + updatedAt +
        "}";
    }
}
