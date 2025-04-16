/*
package com.creda.coin.price.service.data.jdbc.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.AssetInfo;
import com.creda.coin.price.entity.TokenStats;
import com.creda.coin.price.job.TokenStatsJob;
import com.creda.coin.price.mapper.TokenStatsMapper;
import com.creda.coin.price.service.data.jdbc.IAssetInfoService;
import com.creda.coin.price.service.data.jdbc.ITokenStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

*/
/**
 * <p>
 * Meme coin 统计数据表，包含 PnL、交易量、价格变化等信息 服务实现类
 * </p>
 *
 * @author gain
 * @since 2024-09-11
 *//*

@Service
public class TokenStatsServiceImpl extends ServiceImpl<TokenStatsMapper, TokenStats> implements ITokenStatsService {
	private final static Logger log = LoggerFactory.getLogger("tokenStats");


	@Autowired
	private IAssetInfoService assetInfoService;

	@Override
	public List<TokenStats> listByGranularity(Integer granularity) {
		return this.baseMapper.listByGranularity(granularity);
	}

	@Override
	public void initStats() {
		List<String> baseTokens = assetInfoService.list().stream().map(AssetInfo::getAddress).collect(Collectors.toList());

		// 获取 tokenStats 表中所有已存在的 asset_address 列表
		List<String> existingTokens = this.list()
				.stream()
				.map(TokenStats::getAssetAddress)
				.collect(Collectors.toList());

		// 筛选出 baseTokens 中不存在于 existingTokens 的 tokens
		List<String> newTokens = baseTokens.stream()
				.filter(token -> !existingTokens.contains(token))
				.collect(Collectors.toList());

		// 为不存在的 tokens 初始化插入到 token_stats 表中
		List<TokenStats> newTokenStats = new ArrayList<>();
		for (String token : newTokens) {
			for (Integer integer : TokenStatsJob.granularityList) {
				TokenStats tokenStats = new TokenStats();
				tokenStats.setAssetAddress(token);
				tokenStats.setGranularity(integer.longValue());  // 设置默认值
				tokenStats.setBuyCount(null);
				tokenStats.setSellCount(null);
				tokenStats.setHoldersCount(null);
				tokenStats.setMarketCap(null);
				tokenStats.setLiquidity(null);
				tokenStats.setVolume(null);
				tokenStats.setCurrentPrice(null);
				tokenStats.setPriceChange5m(null);
				tokenStats.setPriceChange1h(null);
				tokenStats.setPriceChange24h(null);
				tokenStats.setPriceChange7d(null);
				newTokenStats.add(tokenStats);
			}
		}

		// 批量插入新的 tokenStats 数据
		if (!newTokenStats.isEmpty()) {
			log.warn("newTokenStats size: {}", newTokenStats.size());
			this.saveBatch(newTokenStats);
		}
		log.warn("newTokenStats end" );
	}
}
*/
