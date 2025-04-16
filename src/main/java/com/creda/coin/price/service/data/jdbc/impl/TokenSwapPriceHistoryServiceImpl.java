/*
package com.creda.coin.price.service.data.jdbc.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.TokenSwapPriceHistory;
import com.creda.coin.price.mapper.TokenSwapPriceHistoryMapper;
import com.creda.coin.price.service.data.jdbc.ITokenSwapPriceHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

*/
/**
 * <p>
 * swap计算的token价格 服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-09-26
 *//*

@Service
@Slf4j
public class TokenSwapPriceHistoryServiceImpl extends ServiceImpl<TokenSwapPriceHistoryMapper, TokenSwapPriceHistory> implements ITokenSwapPriceHistoryService {

	// Map for storing the latest price history per token address
	private final Map<String, TokenSwapPriceHistory> priceCache = new ConcurrentHashMap<>();

	@Override
	public TokenSwapPriceHistory getLatestPrice(String tokenAddress, Long txId) {
		// Check if the price is available in the cache
		TokenSwapPriceHistory cachedPrice = priceCache.get(tokenAddress);

		// If the cached price exists and its transaction ID is less than the current txId, return it
		if (cachedPrice != null && cachedPrice.getTxId() < txId) {
			return cachedPrice;
		}

		// If not found in the cache or the cached price is outdated, query the database
		TokenSwapPriceHistory latestPriceHistory = baseMapper.getLatestPrice(tokenAddress, txId);

		// If a valid record is found, update the cache
		if (latestPriceHistory != null) {
			priceCache.put(tokenAddress, latestPriceHistory);
		}else {
//			log.warn("tokenAddress: {} txId: {} not found", tokenAddress, txId);
		}

		return latestPriceHistory;
	}

	// Method to save new price history and update the map cache
	public void UpdatePriceHistoryCache(TokenSwapPriceHistory newPriceHistory) {
		priceCache.put(newPriceHistory.getAssetAddress(), newPriceHistory);
	}
}
*/
