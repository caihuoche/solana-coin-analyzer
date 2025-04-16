package com.creda.coin.price.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 资产基础表
 * </p>
 *
 * @author gavin
 * @since 2024-08-15
 */
@TableName("asset_info")
@Data
public class AssetInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String symbol;
	@TableId(value = "address")
	private String address;
	private String name;
	private Integer decimals;
	private BigDecimal totalSupply;
}
