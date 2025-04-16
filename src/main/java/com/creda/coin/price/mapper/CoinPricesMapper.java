package com.creda.coin.price.mapper;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.CoinPrices;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-07-26
 */
@Mapper

public interface CoinPricesMapper extends BaseMapper<CoinPrices> {
	@Select("SELECT price_date FROM coin_prices WHERE chain_name = #{chainName} AND asset_address = #{assetAddress} ORDER BY price_date DESC LIMIT 1")
	Date getLatestPriceDate(@Param("chainName") String chainName, @Param("assetAddress") String assetAddress);

	@Select("SELECT * FROM coin_prices WHERE chain_name = #{chainName} AND asset_address = #{assetAddress} and price_date = #{date} ")
	CoinPrices getByDateAndAddress(@Param("chainName")String chainName,@Param("assetAddress")String assetAddress, @Param("date")Date date);

	@Select("SELECT * FROM coin_prices WHERE chain_name = #{chainName} AND coin_type = 1 and price_date = #{date} ")
	CoinPrices getByDateAndMainCoin(String chainName, String date);
	@Select("SELECT * FROM coin_prices WHERE chain_name = #{chainName} AND coin_type = 1")
	List<CoinPrices> listAll(String chainName);
	@Select("SELECT * FROM coin_prices WHERE asset_symbol = #{assetSymbol}")
	List<CoinPrices> listAllByAssetSymbol(String assetSymbol);
}
