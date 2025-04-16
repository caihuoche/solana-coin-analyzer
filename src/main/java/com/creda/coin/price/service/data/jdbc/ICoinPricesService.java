package com.creda.coin.price.service.data.jdbc;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.entity.CoinPrices;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-07-26
 */
public interface ICoinPricesService extends IService<CoinPrices> {

	CoinPrices getByDateAndAddress(String chainName,String token0Address, Date date);

	Date getLatestPriceDate(String chainName, String assetAddress);


	CoinPrices getByDateAndMainCoin(String esc, String date);

	List<CoinPrices> listAll(String eth);

	List<CoinPrices> listAllByAssetSymbol(String assetSymbol);

	List<CoinPrices> listAllMainCoin();

	List<CoinPrices> listAllByAddresses( List<String> tokenAddresses);
}
