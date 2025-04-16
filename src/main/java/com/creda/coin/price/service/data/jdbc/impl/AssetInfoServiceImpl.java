package com.creda.coin.price.service.data.jdbc.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.AssetInfo;
import com.creda.coin.price.mapper.AssetInfoMapper;
import com.creda.coin.price.service.data.jdbc.IAssetInfoService;
import com.creda.coin.price.third.SolanaApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 资产基础表 服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-08-16
 */
@Service
@Slf4j
public class AssetInfoServiceImpl extends ServiceImpl<AssetInfoMapper, AssetInfo> implements IAssetInfoService {
	String tableName = "asset_info";
	Map<String, Integer> tokenDecimalsMap = new HashMap<>();
	Map<String, BigDecimal> tokenDecimalMap = new HashMap<>();
	Map<String, BigDecimal> mainCoInDecimalMap = new HashMap<>();
	Map<String, AssetInfo> assetInfoMap = new HashMap<>();
	@Autowired
	private SolanaApiService solanaApiService;  // 调用 Solana API 的服务类

	@PostConstruct
	public void init() {
		assetInfoMap = this.list().stream().collect(Collectors.toMap(AssetInfo::getAddress, assetInfo -> assetInfo));
	}

	public int getDecimals(String assetSymbol, String assetAddress) {
		// 检查缓存是否已经存在精度
		if (tokenDecimalsMap.containsKey(assetSymbol + ":" + assetAddress)) {
			return tokenDecimalsMap.get(assetSymbol + ":" + assetAddress);
		}

		// 如果缓存中不存在，则通过 assetInfoService 获取精度
		AssetInfo assetInfo = getByAssetSymbolAndAddress(assetSymbol, assetAddress);
		if (assetInfo == null) {
			throw new IllegalArgumentException("无法找到该代币的资产信息: " + assetAddress);
		}

		int decimals = assetInfo.getDecimals();
		// 将精度存入缓存
		tokenDecimalsMap.put(assetSymbol + ":" + assetAddress, decimals);
		return decimals;

	}

	public AssetInfo getByAssetSymbolAndAddress(String assetSymbol, String assetAddress) {
		QueryWrapper<AssetInfo> wrapper = new QueryWrapper<>();
		wrapper.lambda().eq(AssetInfo::getSymbol, assetSymbol).eq(AssetInfo::getAddress, assetAddress);
		return this.getOne(wrapper);
	}

	@Override
	public BigDecimal getMainCoinDecimal(String assetSymbol) {
		if (mainCoInDecimalMap.containsKey(assetSymbol)) {
			return mainCoInDecimalMap.get(assetSymbol);
		}
		AssetInfo assetInfo = getBySymbol(assetSymbol);
		if (assetInfo != null) {
			BigDecimal decimal = BigDecimal.TEN.pow(assetInfo.getDecimals());
			tokenDecimalMap.put(assetSymbol, decimal);
			return decimal;
		}
		throw new IllegalArgumentException("无法找到该代币的资产信息: " + assetSymbol);
	}

	private AssetInfo getBySymbol(String assetSymbol) {
		QueryWrapper<AssetInfo> wrapper = new QueryWrapper<>();
		wrapper.lambda().eq(AssetInfo::getSymbol, assetSymbol);
		return this.getOne(wrapper);
	}

	@Override
	public BigDecimal getTokenDecimal(String assetAddress) {
		if (tokenDecimalMap.containsKey(assetAddress)) {
			return tokenDecimalMap.get(assetAddress);
		}
		AssetInfo assetInfo = getByAddress(assetAddress);
		if (assetInfo != null) {
			BigDecimal decimal = BigDecimal.TEN.pow(assetInfo.getDecimals());
			tokenDecimalMap.put(assetAddress, decimal);
			return decimal;
		}
		throw new IllegalArgumentException("无法找到该代币的资产信息: " + assetAddress);
	}

	public AssetInfo getByAddress(String assetAddress) {
		return assetInfoMap.get(assetAddress);
	}


	// 定时更新供应量
//	@Scheduled(cron = "0 0 1 * * ?")  // 每天凌晨 1 点执行
	public void updateTotalSupply() {
		// 获取所有资产地址
		List<String> assetAddresses = assetInfoMap.keySet().stream().collect(Collectors.toList());

		for (String address : assetAddresses) {
			try {
				log.info("Updating total supply for address: " + address);
				// 调用 Solana API 获取该地址的总供应量
				BigDecimal totalSupply = solanaApiService.getTotalSupply(address);
				Thread.sleep(1000);
				// 更新数据库中的供应量
				this.getBaseMapper().updateTotalSupply(address, totalSupply);
				log.info("Updated total supply for address: " + address + ", totalSupply: " + totalSupply);
			} catch (Exception e) {
				log.error("Failed to update total supply for address: " + address, e);
			}
		}
	}

	public Map<String, AssetInfo> getAssetInfoMap() {
		return assetInfoMap;
	}

	@Override
	public String getTableName() {
		return tableName;
	}
}
