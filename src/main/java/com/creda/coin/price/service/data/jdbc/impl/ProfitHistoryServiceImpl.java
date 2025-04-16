package com.creda.coin.price.service.data.jdbc.impl;

import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.ProfitHistory;
import com.creda.coin.price.mapper.ProfitHistoryMapper;
import com.creda.coin.price.service.data.jdbc.IProfitHistoryService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-08-04
 */
@Service
public class ProfitHistoryServiceImpl extends ServiceImpl<ProfitHistoryMapper, ProfitHistory> implements IProfitHistoryService {
	// 创建一个 LRU 缓存，最多存储 100 个条目，且在 10 分钟内未被访问的条目会被移除
	private final Cache<String, ProfitHistory> cache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterAccess(100, TimeUnit.MINUTES)
			.build();
	@Override
	public ProfitHistory getLastByAddress(String address) {
		// 首先尝试从缓存中获取数据
		ProfitHistory cached = cache.getIfPresent(address);
		if (cached != null) {
			return cached;
		}

		// 如果缓存中没有，则从数据库查询
		ProfitHistory profitHistory = this.getBaseMapper().selectLastByAddress(address);

		// 如果数据库中没有找到记录，使用默认值
		if (profitHistory == null) {
			profitHistory = createDefaultProfitHistory(address);
		}

		// 将查询结果放入缓存
		cache.put(address, profitHistory);

		return profitHistory;
	}

	private ProfitHistory createDefaultProfitHistory(String address) {
		ProfitHistory defaultHistory = new ProfitHistory();
		defaultHistory.setAddress(address);
		// 设置默认值
		defaultHistory.setSellCount(0);
		defaultHistory.setSellCountWin(0);
		defaultHistory.setBalance("0");
		defaultHistory.setTotalCost("0");
		defaultHistory.setTotalBalance("0");
		defaultHistory.setHoldingAvgPrice("0");
		defaultHistory.setRealizedProfit("0");
		defaultHistory.setUnrealizedProfit("0");
		defaultHistory.setIsWin(0);
		defaultHistory.setCurrentPrice(null);
		defaultHistory.setTxTime(null);
		defaultHistory.setTxBlock(null);
		defaultHistory.setTxHash(null);
		defaultHistory.setRoi(null);
		defaultHistory.setBuyCount(0);
		return defaultHistory;
	}

	@Override
	public void updateProfitHistory(ProfitHistory profitHistory) {
		// 更新缓存中的记录
		cache.put(profitHistory.getAddress(), profitHistory);
	}
}
