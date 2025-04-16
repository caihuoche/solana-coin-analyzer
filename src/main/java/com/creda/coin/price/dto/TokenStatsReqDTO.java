package com.creda.coin.price.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.creda.coin.price.entity.AssetInfo;

import lombok.Data;

/**
 *
 * @author gavin
 * @date 2024/09/13
 **/
@Data
public class TokenStatsReqDTO {
	// 获取每个 token 的最新当前价格
	private Map<String, BigDecimal> latestCurrentPriceMap;
	// 获取每个 token 的过去 5 分钟、1 小时、24 小时和 7 天的价格信息
	private Map<String, Map<String, BigDecimal>> historicalPricesMap;

	// 创建一个 token1_address 到流动性记录的映射
	private Map<String, TokenLiquidityDTO> tokenLiquidityMap;
	// 获取每个token的holders
	private Map<String, Integer> holdersMap;

	private Map<String, AssetInfo> assetInfoMap;

}
