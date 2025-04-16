package com.creda.coin.price.service.data.jdbc.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.RaydiumSwapPool;
import com.creda.coin.price.mapper.RaydiumSwapPoolMapper;
import com.creda.coin.price.service.data.jdbc.IRaydiumSwapPoolService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RaydiumSwapPoolServiceImpl extends ServiceImpl<RaydiumSwapPoolMapper, RaydiumSwapPool> implements IRaydiumSwapPoolService {

    String tableName = "raydium_swap_pools";
    // 创建一个 LRU 缓存，最多存储 300 个条目，且在 10 分钟内未被访问的条目会被移除
	private final Cache<String, List<RaydiumSwapPool>> cache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterAccess(300, TimeUnit.MINUTES)
			.build();

    @Override
    public 
    List<RaydiumSwapPool> findAll() {
        String cachedKey = "raydium_swap_pools";
        List<RaydiumSwapPool> cachedPoolInfo = cache.getIfPresent(cachedKey);
        if (cachedPoolInfo != null) {
			return cachedPoolInfo;
		}

        List<RaydiumSwapPool> poolInfo = this.getBaseMapper().findAll();
        cache.put(cachedKey, poolInfo);
        return poolInfo;
    }

    @Override
    public RaydiumSwapPool findByPairAddress(String pairAddress) {
        RaydiumSwapPool poolInfo = this.getBaseMapper().findByPairAddress(pairAddress);
        return poolInfo;
    }

    @Override
    public String getTableName() {
        return tableName;
    }
}
