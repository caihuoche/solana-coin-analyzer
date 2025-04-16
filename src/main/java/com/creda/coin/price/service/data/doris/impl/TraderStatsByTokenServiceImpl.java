package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.doris.traders.TraderStatsByToken;
import com.creda.coin.price.mapper.TraderStatsByTokenMapper;
import com.creda.coin.price.service.data.doris.ITraderStatsByTokenService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
public class TraderStatsByTokenServiceImpl extends ServiceImpl<TraderStatsByTokenMapper, TraderStatsByToken> implements ITraderStatsByTokenService {
    String tableName = "trader_stats_by_token";
    private final Cache<String, Map<String, TraderStatsByToken>> cache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(100, TimeUnit.MINUTES)

            .build();
    @Override
    public List<TraderStatsByToken> getTraderStatsByAccounts(Set<String> accounts) {
        // 查询数据库
        List<TraderStatsByToken> traderStatsList = this.getBaseMapper().getTraderStatsByAccounts(accounts);

        // 按 tokenAddress 构建 Map
        Map<String, TraderStatsByToken> statsMap = traderStatsList.stream()
                .collect(Collectors.toMap(
                        TraderStatsByToken::getTokenAddress,  // 使用 tokenAddress 作为键
                        traderStats -> traderStats,           // 使用 TraderStatsByToken 作为值
                        (existing, replacement) -> replacement // 合并重复键，保留最新值
                ));

        // 缓存每个账户的统计数据
        accounts.forEach(account -> {
            Map<String, TraderStatsByToken> existingStats = cache.getIfPresent(account);
            if (existingStats == null) {
                existingStats = new HashMap<>();
            }
            existingStats.putAll(statsMap); // 合并新数据
            cache.put(account, existingStats); // 更新缓存
        });

        return traderStatsList;
    }

    @Override
    public Map<String, Pair<BigDecimal, BigDecimal>> findTopProfitByTokenAddresses(List<String> tokenAddresses) {
        Map<String, Pair<BigDecimal, BigDecimal>> result = new HashMap<>();

        List<TraderStatsByToken> topProfits = this.getBaseMapper().findTopProfitByTokenAddresses(tokenAddresses);

        for (TraderStatsByToken stats : topProfits) {
            result.put(stats.getTokenAddress(), Pair.of(stats.getProfit(), stats.getRoi() != null ? stats.getRoi() : BigDecimal.ZERO));
        }

        return result;
    }

    @Override
    public void distintSaveOrUpdateBatch(List<TraderStatsByToken> traderStats) {
        List<TraderStatsByToken> latestRecords = getLatestRecords(traderStats);

        // 遍历时直接赋值首字母
        for (TraderStatsByToken record : latestRecords) {
            if (record != null && record.getAccount() != null && !record.getAccount().isEmpty()) {
                // 获取 partitionKey 并取首字母赋值给 account 字段
                record.setPartitionKey(String.valueOf(record.getAccount().charAt(0)).toLowerCase());
            }
        }
        saveOrUpdateBatchStreamLoad(latestRecords);
    }

    private List<TraderStatsByToken> getLatestRecords(List<TraderStatsByToken> traderStatsAllPerToken) {
        Map<String, TraderStatsByToken> latestRecordsMap = new HashMap<>();

        for (TraderStatsByToken stats : traderStatsAllPerToken) {
            latestRecordsMap.put(stats.getAccount() + "_" + stats.getTokenAddress(), stats);
        }

        return new ArrayList<>(latestRecordsMap.values());
    }

    @Override
    public String getTableName() {
        return tableName;

    }
}
