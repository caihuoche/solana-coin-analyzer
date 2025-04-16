package com.creda.coin.price.service.data.jdbc;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.entity.AssetInfo;
import com.creda.coin.price.service.BaseDoris;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 资产基础表 服务类
 * </p>
 *
 * @author gavin
 * @since 2024-08-16
 */
public interface IAssetInfoService extends IService<AssetInfo>, BaseDoris<AssetInfo> {
	public AssetInfo getByAssetSymbolAndAddress(String assetSymbol, String assetAddress);

	public BigDecimal getMainCoinDecimal(String assetSymbol);

	public BigDecimal getTokenDecimal(String assetAddress);

	AssetInfo getByAddress(String address);

	Map<String, AssetInfo> getAssetInfoMap();
}
