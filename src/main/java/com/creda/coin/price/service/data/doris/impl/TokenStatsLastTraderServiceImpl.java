package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.doris.coins.TokenStatsLastTrader;
import com.creda.coin.price.mapper.TokenStatsLastTraderMapper;
import com.creda.coin.price.service.data.doris.ITokenStatsLastTraderService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
public class TokenStatsLastTraderServiceImpl extends ServiceImpl<TokenStatsLastTraderMapper, TokenStatsLastTrader> implements ITokenStatsLastTraderService {
   String tableName = "token_stats_last_trader";
    private final Cache<String, TokenStatsLastTrader> cache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(100, TimeUnit.MINUTES)

            .build();
    @Override
    public TokenStatsLastTrader getTopCoinsLastTrader(String tokenAddress) {
        // 先从缓存中获取
        TokenStatsLastTrader cachedTrader = cache.getIfPresent(tokenAddress);
        if (cachedTrader != null) {
            return cachedTrader;
        }


        TokenStatsLastTrader trader = this.getBaseMapper().getByTokenAddress(tokenAddress);

        if (trader == null) {
            TokenStatsLastTrader topCoinsLastTrader = new TokenStatsLastTrader();
            topCoinsLastTrader.setTokenAddress(tokenAddress);
            topCoinsLastTrader.setBoughtCount(0L);
            topCoinsLastTrader.setSoldCount(0L);
            topCoinsLastTrader.setTransferInCount(0L);
            topCoinsLastTrader.setTransferOutCount(0L);
            topCoinsLastTrader.setVolume(BigDecimal.ZERO);
            topCoinsLastTrader.setBlockTime(null);
            topCoinsLastTrader.setBlockHeight(null);
            return topCoinsLastTrader; // 未找到对应的记录
        }

        // 将结果放入缓存
        cache.put(tokenAddress, trader);
        return trader;
    }

    @Override
    public void updateTopCoinsLastTraderByCache(TokenStatsLastTrader topCoinsLastTrader) {
        cache.put(topCoinsLastTrader.getTokenAddress(), topCoinsLastTrader);

    }
    private static final int BATCH_SIZE = 1000;
    @Override
    public List<TokenStatsLastTrader> getAll() {
        List<TokenStatsLastTrader> allTokenStats = new ArrayList<>();
        int offset = 0;
        List<TokenStatsLastTrader> batch;

        do {
            // 使用分页查询
            batch = this.getBaseMapper().getTokenStatsPaged(BATCH_SIZE, offset);
            allTokenStats.addAll(batch);

            // 增加偏移量
            offset += BATCH_SIZE;
        } while (!batch.isEmpty()); // 当批次结果为空时，停止查询

        return allTokenStats;
    }

    @Override
    public String getTableName() {
        return tableName;
    }
}
