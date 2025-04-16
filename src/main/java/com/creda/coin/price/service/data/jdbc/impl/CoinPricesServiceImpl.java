package com.creda.coin.price.service.data.jdbc.impl;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.CoinPrices;
import com.creda.coin.price.mapper.CoinPricesMapper;
import com.creda.coin.price.service.data.jdbc.ICoinPricesService;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-07-26
 */
@Service
public class CoinPricesServiceImpl extends ServiceImpl<CoinPricesMapper, CoinPrices> implements ICoinPricesService {

	@Override
	public CoinPrices getByDateAndAddress(String chainName,String token0Address, Date date) {
		return this.getBaseMapper().getByDateAndAddress(chainName,token0Address,date);
	}

	@Override
	public Date getLatestPriceDate(String chainName, String assetAddress) {
		return this.getBaseMapper().getLatestPriceDate(chainName,assetAddress);
	}

	@Override
	public CoinPrices getByDateAndMainCoin(String chainName, String date) {
		return this.getBaseMapper().getByDateAndMainCoin(chainName,date);

	}

	@Override
	public List<CoinPrices> listAll(String chainName) {
		return this.getBaseMapper().listAll(chainName);
	}

	@Override
	public List<CoinPrices> listAllByAssetSymbol(String assetSymbol) {
		return this.getBaseMapper().listAllByAssetSymbol(assetSymbol);

	}

	@Override
	public List<CoinPrices> listAllMainCoin() {
		QueryWrapper<CoinPrices> coinPricesQueryWrapper = new QueryWrapper<>();
		coinPricesQueryWrapper.lambda().eq(CoinPrices::getAssetAddress, "So11111111111111111111111111111111111111112");
		return this.list(coinPricesQueryWrapper);
	}

	@Override
	public List<CoinPrices> listAllByAddresses(List<String> tokenAddresses) {
		QueryWrapper<CoinPrices> coinPricesQueryWrapper = new QueryWrapper<>();
		coinPricesQueryWrapper.lambda().in(CoinPrices::getAssetAddress, tokenAddresses).eq(CoinPrices::getAssetType, 2);
		return this.list(coinPricesQueryWrapper);
	}
}
