/*
package com.creda.coin.price.service;

import com.creda.coin.price.dto.*;
import com.creda.coin.price.entity.AssetInfo;
import com.creda.coin.price.entity.TokenProfitHistory;
import com.creda.coin.price.entity.TokenStats;
import com.creda.coin.price.service.data.jdbc.IAssetInfoService;
import com.creda.coin.price.service.data.jdbc.ITokenBalanceHistoryService;
import com.creda.coin.price.service.data.jdbc.ITokenProfitHistoryService;
import com.creda.coin.price.service.data.jdbc.ITokenStatsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

*/
/**
 *
 * @author gavin
 * @date 2024/09/11
 **//*

@Service
public class TokenStatsCalService {
	private final static Logger log = LoggerFactory.getLogger("tokenStats");

	@Autowired
	private ITokenProfitHistoryService tokenProfitHistoryService;
	@Autowired
	private ITokenBalanceHistoryService tokenBalanceHistoryService;
	@Autowired
	private IAssetInfoService assetInfoService;


	@Autowired
	private ITokenStatsService tokenStatsService;

	public TokenStatsReqDTO initTokenStatsReqDTO(Date endTime) {
		TokenStatsReqDTO tokenStatsReqDTO = new TokenStatsReqDTO();
		// 获取每个 token 的最新当前价格
		Map<String, BigDecimal> latestCurrentPriceMap = getLatestCurrentPriceMap();
		// 获取每个 token 的过去 5 分钟、1 小时、24 小时和 7 天的价格信息
		Map<String, Map<String, BigDecimal>> historicalPricesMap = getHistoricalPrices(endTime);
		// 查询每个 token1_address 的最新流动性数据
	*/
/*	List<TokenLiquidityDTO> tokenLiquidityList = tokenBalanceHistoryService.getLatestTokenLiquidity();

		// 创建一个 token1_address 到流动性记录的映射
		Map<String, TokenLiquidityDTO> tokenLiquidityMap = tokenLiquidityList.stream()
				.collect(Collectors.toMap(TokenLiquidityDTO::getToken1Address, tokenLiquidityDTO -> tokenLiquidityDTO));
*//*

		// 获取每个token的holders
		Map<String, Integer> holdersMap = getHoldersMap();

		Map<String, AssetInfo> assetInfoMap = assetInfoService.getAssetInfoMap();

		tokenStatsReqDTO.setLatestCurrentPriceMap(latestCurrentPriceMap);
		tokenStatsReqDTO.setHistoricalPricesMap(historicalPricesMap);
//		tokenStatsReqDTO.setTokenLiquidityMap(tokenLiquidityMap);
		tokenStatsReqDTO.setHoldersMap(holdersMap);
		tokenStatsReqDTO.setAssetInfoMap(assetInfoMap);
		return tokenStatsReqDTO;

	}

	private Map<String, Integer> getHoldersMap() {
		long start = System.currentTimeMillis();
		List<TokenHoldersCountDTO> tokenHoldersCountDTOS = tokenProfitHistoryService.getHoldersMap();
		Map<String, Integer> collect = ListUtils.emptyIfNull(tokenHoldersCountDTOS).stream().collect(Collectors.toMap(TokenHoldersCountDTO::getAssetAddress, TokenHoldersCountDTO::getHoldersCount));
		log.info("getHoldersMap cost: {}ms", System.currentTimeMillis() - start);
		return collect;
	}

	public void calculateTokenStats(Date startTime, Date endTime, Integer granularity, TokenStatsReqDTO tokenStatsReqDTO) {
		List<TokenStats> tokenStatsList = tokenStatsService.listByGranularity(granularity);

		// 获取指定时间范围内的交易数据
		long start = System.currentTimeMillis();
		List<TokenTxCountDTO> tokenTradeCounts = tokenProfitHistoryService.getTokenTradeCounts(startTime, endTime);
		Map<String, TokenTxCountDTO> tokenTradeCountDTOMap = tokenTradeCounts.stream().collect(Collectors.toMap(TokenTxCountDTO::getAssetAddress, tokenTxCountDTO -> tokenTxCountDTO));
		log.info("getTokenTradeCounts cost: {}ms", System.currentTimeMillis() - start);
		// 获取每个 token 的最新当前价格
		Map<String, BigDecimal> latestCurrentPriceMap = tokenStatsReqDTO.getLatestCurrentPriceMap();

		// 获取每个 token 的过去 5 分钟、1 小时、24 小时和 7 天的价格信息
		Map<String, Map<String, BigDecimal>> historicalPricesMap = tokenStatsReqDTO.getHistoricalPricesMap();
		log.warn("historicalPricesMap size: {}", historicalPricesMap.size());

		// 创建一个 token1_address 到流动性记录的映射
//		Map<String, TokenLiquidityDTO> tokenLiquidityMap = tokenStatsReqDTO.getTokenLiquidityMap();
		Map<String, Integer> holdersMap = tokenStatsReqDTO.getHoldersMap();
		log.warn("holdersMap size: {}", holdersMap.size());
		start = System.currentTimeMillis();

//		Map<String, UserProfitDTO> stringUserProfitDTOMap = calculateTopProfits(startTime, endTime);
		log.info("calculateTopProfits cost: {}ms", System.currentTimeMillis() - start);
		for (TokenStats tokenStats : tokenStatsList) {
			String assetAddress = tokenStats.getAssetAddress();
			TokenTxCountDTO tokenTxCountDTO = tokenTradeCountDTOMap.get(assetAddress);

			if (tokenTxCountDTO != null) {
				tokenStats.setBuyCount(tokenTxCountDTO.getBuyCount());
				tokenStats.setSellCount(tokenTxCountDTO.getSellCount());
				tokenStats.setVolume(tokenTxCountDTO.getVolume());
				tokenStats.setTradeCount(tokenTxCountDTO.getBuyCount() + tokenTxCountDTO.getSellCount());
			}

			Integer holdersCount = holdersMap.get(assetAddress);
			if (holdersCount != null) {
				tokenStats.setHoldersCount(holdersCount);
			}

			// 更新最新当前价格
			BigDecimal latestPrice = latestCurrentPriceMap.get(assetAddress);
			if (latestPrice != null) {
				tokenStats.setCurrentPrice(latestPrice);
			} else {
				latestPrice = tokenStats.getCurrentPrice();
			}

			// 更新涨跌幅信息
			Map<String, BigDecimal> historicalPrices = historicalPricesMap.get(assetAddress);
			if (historicalPrices != null) {
				// 使用非空值或默认值 BigDecimal.ZERO
				BigDecimal price5m = historicalPrices.getOrDefault("5m", BigDecimal.ZERO);
				BigDecimal price1h = historicalPrices.getOrDefault("1h", BigDecimal.ZERO);
				BigDecimal price24h = historicalPrices.getOrDefault("24h", BigDecimal.ZERO);
				BigDecimal price7d = historicalPrices.getOrDefault("7d", BigDecimal.ZERO);

				// 确保 latestPrice 也不为空
				latestPrice = (latestPrice != null) ? latestPrice : BigDecimal.ZERO;

				// 计算并设置价格变化百分比
				tokenStats.setPriceChange5m(calculateChangePercentage(price5m, latestPrice));
				tokenStats.setPriceChange1h(calculateChangePercentage(price1h, latestPrice));
				tokenStats.setPriceChange24h(calculateChangePercentage(price24h, latestPrice));
				tokenStats.setPriceChange7d(calculateChangePercentage(price7d, latestPrice));
			}

			// 计算流动性
		*/
/*	TokenLiquidityDTO tokenLiquidity = tokenLiquidityMap.get(assetAddress);
			if (tokenLiquidity != null) {
				BigDecimal token0Price = latestCurrentPriceMap.get(tokenLiquidity.getToken0Address());
				BigDecimal token1Price = latestCurrentPriceMap.get(tokenLiquidity.getToken1Address());

				if (token0Price == null) {
					token0Price = BigDecimal.ZERO;
				}
				if (token1Price == null) {
					token1Price = BigDecimal.ZERO;
				}

				// 计算流动性：token0_balance * token0_price + token1_balance * token1_price
				BigDecimal token0Balance = new BigDecimal(tokenLiquidity.getToken0Balance());
				BigDecimal token1Balance = new BigDecimal(tokenLiquidity.getToken1Balance());
				BigDecimal liquidity = token0Balance.multiply(token0Price).add(token1Balance.multiply(token1Price));

				tokenStats.setLiquidity(liquidity);
			}*//*


			Map<String, AssetInfo> assetInfoList = tokenStatsReqDTO.getAssetInfoMap();
			if (latestPrice != null) {
				AssetInfo assetInfo = assetInfoList.get(assetAddress);
				if (assetInfo != null && assetInfo.getTotalSupply() != null) {
					tokenStats.setMarketCap(assetInfo.getTotalSupply().multiply(latestPrice));
				}
			}

			*/
/*UserProfitDTO userProfitDTO = stringUserProfitDTOMap.get(assetAddress);
			if (userProfitDTO != null) {
				tokenStats.setTraderPnl(userProfitDTO.getPnl());
				tokenStats.setTraderRoi(userProfitDTO.getRoi());
			}*//*


		}
		ListUtils.partition(tokenStatsList, 500).forEach(tokenStatsService::updateBatchById);
	}

	private BigDecimal calculateChangePercentage(BigDecimal oldPrice, BigDecimal newPrice) {
		if (oldPrice == null || newPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return newPrice.subtract(oldPrice).divide(oldPrice, 4, RoundingMode.HALF_UP);
	}

	private Map<String, BigDecimal> getLatestCurrentPriceMap() {
		long start = System.currentTimeMillis();
		// 1. 获取每个 asset_address 的最新交易记录 ID
		List<TokenPriceDTO> latestIds = tokenProfitHistoryService.getLatestIds();

		// 2. 将结果转换为 Map，以便后续查询
		Map<String, Long> latestIdMap = latestIds.stream()
				.collect(Collectors.toMap(TokenPriceDTO::getAssetAddress, TokenPriceDTO::getLatestId));

		// 3. 获取所有最新的 ID 列表
		List<Long> latestIdList = latestIdMap.values().stream()
				.collect(Collectors.toList());

		// 4. 批量获取每个 token 地址的最新当前价格
		List<TokenPriceDTO> priceDTOs = tokenProfitHistoryService.getCurrentPricesByIds(latestIdList);

		// 5. 将价格数据转换为 Map
		Map<String, BigDecimal> latestPriceMap = priceDTOs.stream()
				.collect(Collectors.toMap(TokenPriceDTO::getAssetAddress, TokenPriceDTO::getCurrentPriceInSol));

		log.info("获取每个 token 地址的最新当前价格耗时：{}ms", System.currentTimeMillis() - start);
		return latestPriceMap;

	}

	public Map<String, Map<String, BigDecimal>> getHistoricalPrices(Date endTime) {
		long start = System.currentTimeMillis();

		Map<String, Map<String, BigDecimal>> historicalPricesMap = new HashMap<>();

		// 查询过去 5 分钟、1 小时、24 小时和 7 天的价格

		List<TokenPriceDTO> price5mList = tokenProfitHistoryService.getPriceAtTime(DateUtils.addMinutes(endTime, -5), endTime);
		List<TokenPriceDTO> price1hList = tokenProfitHistoryService.getPriceAtTime(DateUtils.addMinutes(endTime, -60), endTime);
		List<TokenPriceDTO> price24hList = tokenProfitHistoryService.getPriceAtTime(DateUtils.addMinutes(endTime, -60 * 24), endTime);
		List<TokenPriceDTO> price7dList = tokenProfitHistoryService.getPriceAtTime(DateUtils.addMinutes(endTime, -60 * 24 * 7), endTime);

		// 将查询结果映射到每个 token 上
		mapPrices(historicalPricesMap, price5mList, "5m");
		mapPrices(historicalPricesMap, price1hList, "1h");
		mapPrices(historicalPricesMap, price24hList, "24h");
		mapPrices(historicalPricesMap, price7dList, "7d");
		log.info("获取每个 token 地址的历史价格耗时：{}ms", System.currentTimeMillis() - start);
		return historicalPricesMap;
	}

	private void mapPrices(Map<String, Map<String, BigDecimal>> historicalPricesMap, List<TokenPriceDTO> priceList, String timeFrame) {
		for (TokenPriceDTO priceDTO : priceList) {
			historicalPricesMap.computeIfAbsent(priceDTO.getAssetAddress(), k -> new HashMap<>())
					.put(timeFrame, priceDTO.getCurrentPriceInSol());
		}
	}

	private Date getPastTime(int amount, TimeUnit unit) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MILLISECOND, (int) -unit.toMillis(amount));
		return calendar.getTime();
	}

	*/
/**
	 * 计算所有资产的用户盈利和 ROI，返回每个资产下盈利最高的用户。
	 *//*

	public Map<String, UserProfitDTO> calculateTopProfits(Date startTime, Date endTime) {
		// 获取所有资产信息
		List<AssetInfo> assets = assetInfoService.list();

		// 创建最终的 topProfits 列表
		Map<String, UserProfitDTO> topProfits = new HashMap<>();
		log.warn("start calculate topProfits, startTime: {}, endTime: {}", startTime, endTime);
		int i =0;
		// 遍历每个资产
		for (AssetInfo asset : assets) {
			i++;
			log.warn("calculate topProfits for asset: {}, index: {}", asset.getAddress(), i);
			String assetAddress = asset.getAddress();

			List<String> users = tokenProfitHistoryService.getAllAddressByAsset(assetAddress);
			if (CollectionUtils.isEmpty(users)) {
				continue;
			}
			// 获取当前 asset 的所有交易记录
			List<TokenProfitHistory> transactions = tokenProfitHistoryService.getTransactionsForAsset( assetAddress, startTime, endTime);

			if (CollectionUtils.isEmpty(transactions)) {
				continue;
			}
			// 计算每个用户的盈利和 ROI
			TokenProfitHistory endPriceHistory = tokenProfitHistoryService.getLastTransactionBeforeEnd(assetAddress,endTime);
			if (endPriceHistory == null) {
				continue;
			}
			Map<String, UserProfitDTO> userProfitMap = calculateProfitForUsers(assetAddress,new BigDecimal(endPriceHistory.getCurrentPriceInSol()), users,
					transactions, startTime);

			// 对当前 asset 下所有用户按盈利进行排序，并取出盈利最高的用户
			if (userProfitMap.isEmpty()) {
				continue;
			}
			Optional<UserProfitDTO> topProfitForAsset = userProfitMap.values().stream().max(Comparator.comparing(UserProfitDTO::getPnl));

			topProfits.put(assetAddress, topProfitForAsset.get());
		}

		// 返回所有资产中盈利最高的用户
		return topProfits;
	}

	*/
/**
	 * 计算每个用户的盈利和 ROI。
	 *//*

	private Map<String, UserProfitDTO> calculateProfitForUsers(
			String assetAddress, BigDecimal endPrice, List<String> users,
			List<TokenProfitHistory> transactions, Date startTime) {

		Map<String, UserProfitDTO> userProfitMap = new HashMap<>();

		// 1. 按 address 对 transactions 分组，map 的 key 是 address，value 是该地址的最后一条交易记录
		Map<String, TokenProfitHistory> latestTransactionMap = transactions.stream()
				.collect(Collectors.toMap(
						TokenProfitHistory::getAddress,
						transaction -> transaction,
						(oldValue, newValue) -> newValue)); // 取最新的交易记录

		Map<String, TokenProfitHistory> firstTransactionMap = transactions.stream()
				.collect(Collectors.toMap(
						TokenProfitHistory::getAddress,
						transaction -> transaction,
						(oldValue, newValue) -> oldValue)); // 取最新的交易记录


		// 3. 统一查询，在开始时间之前的最后一条记录
		Map<String, TokenProfitHistory> lastRecordsBeforeStart = queryLastTransactionBefore(assetAddress,users, startTime);

		// 4. 遍历所有用户，计算他们的盈利
		for (String user : users) {
			UserProfitDTO userProfit = new UserProfitDTO();
			userProfit.setAddress(user);
			userProfit.setPnl(BigDecimal.ZERO);
			userProfit.setRoi(BigDecimal.ZERO);

			// 如果用户有交易记录
			TokenProfitHistory transaction = latestTransactionMap.get(user);
			if (transaction != null) {
				// 继续按之前逻辑计算有交易记录用户的盈利
				// 没有交易记录的用户
				TokenProfitHistory lastRecord = lastRecordsBeforeStart.get(user);

				if (lastRecord != null) {
					BigDecimal realizedProfitInSol = new BigDecimal(transaction.getRealizedProfitInSol()).subtract(new BigDecimal(lastRecord.getRealizedProfitInSol()));

					BigDecimal balance = new BigDecimal(lastRecord.getBalance());
					BigDecimal unRealizedProfitInSol = balance.multiply(endPrice.subtract(new BigDecimal(transaction.getHoldingAvgPrice())));
					userProfit.setPnl(unRealizedProfitInSol.add(realizedProfitInSol));

					// 计算 ROI
					BigDecimal totalCost = new BigDecimal(lastRecord.getTotalCostInSol());
					if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
						userProfit.setRoi(userProfit.getPnl().divide(totalCost,4, RoundingMode.HALF_UP).max(BigDecimal.ZERO));
					}else {
						userProfit.setRoi(BigDecimal.ZERO);
					}
				}else {
					// 第一笔记录 取出时间断的第一条记录, 用最后的记录减去第一条记录realizedProfitInSol就是已盈利的  ,然后计算浮盈 用结束之间的价格减去最后一条的持仓均价 乘以balance
					TokenProfitHistory first = firstTransactionMap.get(user);
					BigDecimal realizedProfitInSol = new BigDecimal(transaction.getRealizedProfitInSol()).subtract(new BigDecimal(first.getRealizedProfitInSol()));

					BigDecimal balance = new BigDecimal(transaction.getBalance());
					BigDecimal unRealizedProfitInSol = balance.multiply(endPrice.subtract(new BigDecimal(transaction.getHoldingAvgPrice())));
					userProfit.setPnl(unRealizedProfitInSol.add(realizedProfitInSol));

					// 计算 ROI
					BigDecimal totalCost = new BigDecimal(transaction.getTotalCostInSol());
					if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
						userProfit.setRoi(userProfit.getPnl().divide(totalCost,4, RoundingMode.HALF_UP).max(BigDecimal.ZERO));
					}else {
						userProfit.setRoi(BigDecimal.ZERO);
					}
				}

				userProfitMap.put(user, userProfit);

			} else {
				// 没有交易记录的用户
				TokenProfitHistory lastRecord = lastRecordsBeforeStart.get(user);

				if (lastRecord != null) {
					// 5. 计算利润：balance * (endPrice - startPrice)
					BigDecimal balance = new BigDecimal(lastRecord.getBalance());
					BigDecimal unRealizedProfitInSol = balance.multiply(endPrice.subtract(new BigDecimal(lastRecord.getHoldingAvgPrice())));
					userProfit.setPnl(unRealizedProfitInSol);

					// 计算 ROI
					BigDecimal totalCost = new BigDecimal(lastRecord.getTotalCostInSol());
					if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
						userProfit.setRoi(unRealizedProfitInSol.divide(totalCost,4, RoundingMode.HALF_UP).max(BigDecimal.ZERO));
					}else {
						userProfit.setRoi(BigDecimal.ZERO);
					}
				}else {
					log.error("没有交易记录的用户: " + user);
				}

				userProfitMap.put(user, userProfit);
			}
		}

		return userProfitMap;
	}

	// 批量查询用户在指定时间点之前的最后一条记录
	private Map<String, TokenProfitHistory> queryLastTransactionBefore(String assetAddress, List<String> users, Date time) {
		// 实现数据库查询，返回按用户分组的最后一条交易记录
		// 例如：SELECT * FROM token_profit_history WHERE address IN (users) AND tx_time <= time ORDER BY tx_time DESC LIMIT 1;
		List<TokenProfitHistory> tokenProfitHistories = tokenProfitHistoryService.queryLastTransactionBefore(assetAddress,users, time);
		return tokenProfitHistories.stream()
				.collect(Collectors.toMap(
						TokenProfitHistory::getAddress, // Key extractor
						Function.identity(), // Value extractor
						(existing, replacement) -> existing // Merge function, keep the existing one
				));

	}


}

*/
