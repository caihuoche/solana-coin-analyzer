package com.creda.coin.price.service.data.doris.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.dto.LastHistoryDTO;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.entity.doris.TokenProfitHistoryLast;
import com.creda.coin.price.mapper.TokenProfitHistoryLastMapper;
import com.creda.coin.price.service.data.doris.ITokenProfitHistoryLastService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
public class TokenProfitHistoryLastServiceImpl extends ServiceImpl<TokenProfitHistoryLastMapper, TokenProfitHistoryLast> implements ITokenProfitHistoryLastService {

    private final Cache<String, TokenProfitHistoryLast> cache = CacheBuilder.newBuilder()
            .maximumSize(50000)
            .expireAfterAccess(50, TimeUnit.MINUTES)
            .build();

    String tableName = "token_profit_history_last";
    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public TokenProfitHistoryLast getLastByAddressFromCache(String userAddress, String tokenAddress) {
        // 先从缓存中尝试获取数据
        String cacheKey = userAddress + "_" + tokenAddress;
        return cache.getIfPresent(cacheKey);
    }

    @Override
    public void updateCache(List<LastHistoryDTO> lastHistoryDTOS) {
        if (lastHistoryDTOS == null || lastHistoryDTOS.isEmpty()) {
            return;
        }

        // Construct the list of IDs for batch query
        List<String> ids = lastHistoryDTOS.stream()
                .map(dto -> dto.getUserAddress() + "_" + dto.getTokenAddress())
                .collect(Collectors.toList());

        List<TokenProfitHistoryLast> tokenProfitHistoryLasts = search(ids);

        // Create a set for quick lookup of existing keys
        Set<String> existingKeys = tokenProfitHistoryLasts.stream()
                .map(profitHistory -> profitHistory.getAccount() + "_" + profitHistory.getTokenAddress())
                .collect(Collectors.toSet());

        // Update cache with the retrieved data
        for (TokenProfitHistoryLast profitHistory : tokenProfitHistoryLasts) {
            cache.put(profitHistory.getAccount() + "_" + profitHistory.getTokenAddress(), profitHistory);
        }

        // Write default data for any missing records
        for (LastHistoryDTO dto : lastHistoryDTOS) {
            String key = dto.getUserAddress() + "_" + dto.getTokenAddress();
            if (!existingKeys.contains(key)) {
                TokenProfitHistoryLast defaultData = createDefaultProfitHistory(dto); // Create default data
                cache.put(key, defaultData);
            }
        }
    }

    @Override
    public void updateProfitHistoryByCache(TokenProfitHistory profitHistory) {
        TokenProfitHistoryLast profitHistoryLast = new TokenProfitHistoryLast(profitHistory);
        cache.put(profitHistory.getAccount() + "_" + profitHistory.getTokenAddress(), profitHistoryLast);
    }

    @Override
    public void updateProfitHistoryByCache(TokenProfitHistoryLast profitHistoryLast) {
		cache.put(profitHistoryLast.getAccount() + "_" + profitHistoryLast.getTokenAddress(), profitHistoryLast);
	}

    public List<TokenProfitHistoryLast> search(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList(); // 如果没有提供 ID 则返回空列表
        }
        return this.getBaseMapper().selectBatchIds(ids); // 查询并返回结果
    }

    @Override
    public TokenProfitHistoryLast createDefaultProfitHistory(LastHistoryDTO lastHistoryDTO) {
        String tokenAddress = lastHistoryDTO.getTokenAddress();
        String userAddress = lastHistoryDTO.getUserAddress();
        TokenProfitHistoryLast defaultHistory = new TokenProfitHistoryLast();
        defaultHistory.setTokenAddress(tokenAddress);
        defaultHistory.setAccount(userAddress);
        // 设置默认值
        defaultHistory.setSoldCount(0L);
        defaultHistory.setSoldCountWin(0L);
        defaultHistory.setAmount(BigDecimal.ZERO);
        defaultHistory.setTotalAmount(BigDecimal.ZERO);

        defaultHistory.setTotalCost(BigDecimal.ZERO);
        defaultHistory.setHistoricalHoldingAvgPrice(BigDecimal.ZERO);
        defaultHistory.setRealizedProfit(BigDecimal.ZERO);
        defaultHistory.setUnrealizedProfit(BigDecimal.ZERO);
        defaultHistory.setCurrentPrice(null);


        defaultHistory.setSoldAmount(BigDecimal.ZERO);
        defaultHistory.setHistoricalSoldAvgPrice(BigDecimal.ZERO);
        defaultHistory.setBoughtAmount(BigDecimal.ZERO);

        defaultHistory.setTransferInCount(0L);
        defaultHistory.setTransferOutCount(0L);
        defaultHistory.setTransferInAmount(BigDecimal.ZERO);
        defaultHistory.setTransferOutAmount(BigDecimal.ZERO);
        defaultHistory.setSoldCountWin(0L);
        defaultHistory.setIsWin(0);
        defaultHistory.setBlockTime(null);
        defaultHistory.setBlockHeight(null);
        defaultHistory.setTxHash(null);
        defaultHistory.setRoi(null);
        defaultHistory.setUnrealizedRoi(null);
        defaultHistory.setRealizedRoi(null);
        defaultHistory.setBoughtCount(0L);
        defaultHistory.setSoldCountBoughtByUser(0L);
        return defaultHistory;
    }

}
