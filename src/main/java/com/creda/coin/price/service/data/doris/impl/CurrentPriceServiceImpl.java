package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.constant.SolConstant;
import com.creda.coin.price.entity.doris.CurrentPrice;
import com.creda.coin.price.mapper.CurrentPriceMapper;
import com.creda.coin.price.service.data.doris.ICurrentPriceService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
public class CurrentPriceServiceImpl extends ServiceImpl<CurrentPriceMapper, CurrentPrice> implements ICurrentPriceService {
    String tableName = "current_price";
    private final Cache<String, CurrentPrice> cache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .expireAfterAccess(100, TimeUnit.MINUTES)

            .build();
    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public CurrentPrice getCurrentPrice(String tokenAddress) {
        if (SolConstant.USDC_ADDRESS.equals(tokenAddress)){
            CurrentPrice currentPrice = new CurrentPrice();
            currentPrice.setPrice(BigDecimal.ONE);
            currentPrice.setTokenAddress(tokenAddress);
            return currentPrice;
        }
        // 1. 检查缓存中是否存在
        CurrentPrice currentPrice = cache.getIfPresent(tokenAddress);
        if (currentPrice != null) {
            return currentPrice; // 如果缓存存在，直接返回
        }

        // 2. 如果缓存中不存在，则从数据库查询
        currentPrice = this.getBaseMapper().getCurrentPriceByTokenAddress(tokenAddress);

        // 3. 如果找到了结果，将结果存入缓存
        if (currentPrice != null) {
            cache.put(tokenAddress, currentPrice); // 更新缓存
            return currentPrice; // 返回最新价格
        }

        // 4. 如果没有找到结果，返回一个空的 CurrentPrice，并缓存空值
        CurrentPrice nullCurrentPrice = new CurrentPrice();
        cache.put(tokenAddress, nullCurrentPrice); // 更新缓存
        return nullCurrentPrice; // 如果没有找到，返回空的 CurrentPrice
    }

    @Override
    public List<String> listAll() {
        List<String> tokenAddresses = new ArrayList<>();
        int offset = 0;
        int limit = 5000; // 每批获取的记录数

        List<String> batch;
        do {
            batch = this.getBaseMapper().selectTokenIds(offset, limit);
            tokenAddresses.addAll(batch);
            offset += limit; // 下次查询的起始位置
        } while (batch.size() == limit); // 如果获取的数据量小于 limit，则说明已取完

        return tokenAddresses;
    }

    public void updateCurrentPriceByCache(CurrentPrice currentPrice) {
        // 同步更新缓存
        cache.put(currentPrice.getTokenAddress(), currentPrice);
    }

}
