/*
package com.creda.coin.price.service.data.jdbc.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.dto.AddressTransactionDTO;
import com.creda.coin.price.dto.TokenHoldersCountDTO;
import com.creda.coin.price.dto.TokenPriceDTO;
import com.creda.coin.price.dto.TokenTxCountDTO;
import com.creda.coin.price.entity.TokenProfitHistory;
import com.creda.coin.price.mapper.TokenProfitHistoryMapper;
import com.creda.coin.price.service.data.jdbc.ITokenProfitHistoryService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

*/
/**
 * <p>
 * 主币历史盈利 服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-08-15
 *//*

@Service
public class TokenProfitHistoryServiceImpl extends ServiceImpl<TokenProfitHistoryMapper, TokenProfitHistory> implements ITokenProfitHistoryService {
	// 创建一个 LRU 缓存，最多存储 100 个条目，且在 10 分钟内未被访问的条目会被移除
	private final Cache<String, Map<String, TokenProfitHistory>> cache = CacheBuilder.newBuilder()
			.maximumSize(10000)
			.expireAfterAccess(100, TimeUnit.MINUTES)
			.build();

	@Override
	public TokenProfitHistory getLastByAddress(String address, String assetAddress) {
		// 首先尝试从缓存中获取数据
		Map<String, TokenProfitHistory> tokenProfitHistoryMap = cache.getIfPresent(assetAddress);
		if (MapUtils.isNotEmpty(tokenProfitHistoryMap)) {
			TokenProfitHistory tokenProfitHistory = tokenProfitHistoryMap.get(address);
			if (tokenProfitHistory != null) {
				return tokenProfitHistory;
			}
		}

		// 如果缓存中没有，则从数据库查询
		TokenProfitHistory profitHistory = this.getBaseMapper().selectLastByAddress(address, assetAddress);

		// 如果数据库中没有找到记录，使用默认值
		if (profitHistory == null) {
			profitHistory = createDefaultProfitHistory(address, assetAddress);
		}
		if (MapUtils.isEmpty(tokenProfitHistoryMap)) {
			tokenProfitHistoryMap = new HashMap<>();
		}
		tokenProfitHistoryMap.put(address, profitHistory);
		cache.put(assetAddress, tokenProfitHistoryMap);

		return profitHistory;
	}

	private TokenProfitHistory createDefaultProfitHistory(String address, String assetAddress) {
		TokenProfitHistory defaultHistory = new TokenProfitHistory();
		defaultHistory.setAssetAddress(assetAddress);
		defaultHistory.setAddress(address);
		// 设置默认值
		defaultHistory.setSellCount(0);
		defaultHistory.setSellCountWin(0);
		defaultHistory.setBalance("0");
		defaultHistory.setTotalBalance("0");

		defaultHistory.setTotalCost("0");
		defaultHistory.setHoldingAvgPrice("0");
		defaultHistory.setRealizedProfit("0");
		defaultHistory.setUnrealizedProfit("0");
		defaultHistory.setCurrentPrice(null);

		defaultHistory.setTotalCostInSol("0");
		defaultHistory.setHoldingAvgPriceInSol("0");
		defaultHistory.setRealizedProfitInSol("0");
		defaultHistory.setUnrealizedProfitInSol("0");
		defaultHistory.setCurrentPriceInSol(null);

		defaultHistory.setSellBalance("0");
		defaultHistory.setSellAvgPriceInSol("0");

		defaultHistory.setIsWin(0);
		defaultHistory.setTxTime(null);
		defaultHistory.setTxBlock(null);
		defaultHistory.setTxHash(null);
		defaultHistory.setRoi(null);
		defaultHistory.setBuyCount(0);
		defaultHistory.setId(0L);
		return defaultHistory;
	}

	@Override
	public void updateProfitHistory(TokenProfitHistory profitHistory) {
		// 更新缓存中的记录
		Map<String, TokenProfitHistory> historyMap = cache.getIfPresent(profitHistory.getAssetAddress());

		if (historyMap == null) {
			// 如果缓存中不存在对应的Map，创建一个新的Map
			historyMap = new HashMap<>();
			// 将新的Map放入缓存
			cache.put(profitHistory.getAssetAddress(), historyMap);
		}

		// 更新Map，放入新的profitHistory
		historyMap.put(profitHistory.getAddress(), profitHistory);
	}

	@Override
	public List<TokenProfitHistory> getProfitHistoryByTime(String baseToken, Date startTime) {
		return null;
	}

	@Override
	public List<TokenTxCountDTO> getTokenTradeCounts(Date startTime, Date endTime) {
		return this.getBaseMapper().getTokenTradeCounts(startTime, endTime);
	}

	@Override
	public List<TokenPriceDTO> getLatestIds() {
		return this.getBaseMapper().getLatestIds();
	}

	@Override
	public List<TokenPriceDTO> getCurrentPricesByIds(List<Long> latestIdList) {
		return this.getBaseMapper().getCurrentPricesByIds(latestIdList);
	}

	@Override
	public List<TokenPriceDTO> getPriceAtTime(Date startTime, Date endTime) {
		return this.getBaseMapper().getPriceAtTime(startTime, endTime);
	}

	@Override
	public List<TokenHoldersCountDTO> getHoldersMap() {
		return this.getBaseMapper().getHoldersMap();
	}

	@Override
	public List<String> listTokenAddress() {
		return this.getBaseMapper().listTokenAddress();
	}

	@Override
	public List<String> getAllAddressByAsset(String assetAddress) {
		return this.getBaseMapper().getAllAddressByAsset(assetAddress);
	}

	@Override
	public List<TokenProfitHistory> queryLastTransactionBefore(String assetAddress, List<String> addresses, Date time) {
		// Step 1: 获取符合条件的地址及其最新交易时间
		List<AddressTransactionDTO> transactions = this.getBaseMapper().getLatestAddressAndTime(assetAddress, time, addresses);

		if (CollectionUtils.isEmpty(transactions)) {
			return Collections.emptyList();
		}
		Map<String, AddressTransactionDTO> latestTransactionMap = transactions.stream()
				.collect(Collectors.toMap(
						AddressTransactionDTO::getAddress, // 按地址分组
						Function.identity(),                // 保留原始对象
						(existing, replacement) -> {
							// 如果当前记录时间较新，替换现有记录
							return existing.getTxTime().before(replacement.getTxTime()) ? replacement : existing;
						}
				));


// 将分组后的数据转换为列表，用于后续查询
		List<AddressTransactionDTO> latestTransactions = new ArrayList<>(latestTransactionMap.values());

// Step 3: 执行批量查询
	return this.getBaseMapper().getTransactionsByAddressAndTimeBatch(latestTransactions, assetAddress);
	}

	@Override
	public List<TokenProfitHistory> getTransactionsForAsset(String assetAddress, Date startTime, Date endTime) {
		return this.getBaseMapper().getTransactionsForAsset(assetAddress, startTime, endTime);
	}

	@Override
	public TokenProfitHistory getLastTransactionBeforeEnd(String assetAddress, Date endTime) {
		return this.getBaseMapper().getLastTransactionBeforeEnd(assetAddress, endTime);
	}
}
*/
