package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.AddressProfit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-08-05
 */
@Mapper
public interface AddressProfitMapper extends BaseMapper<AddressProfit> {
	@Insert({
			"<script>",
			"INSERT INTO address_profit (address, asset_address, balance, total_profit,total_profit_in_sol, roi, block_height, block_time, win_ratio) VALUES ",
			"<foreach collection='list' item='item' separator=','>",
			"(#{item.address}, #{item.assetAddress}, #{item.balance}, #{item.totalProfit}, #{item.totalProfitInSol}, #{item.roi}, #{item.blockHeight}, #{item.blockTime}, #{item.winRatio})",
			"</foreach>",
			"ON DUPLICATE KEY UPDATE",
			"balance = VALUES(balance),",
			"total_profit = VALUES(total_profit),",
			"total_profit_in_sol = VALUES(total_profit_in_sol),",
			"roi = VALUES(roi),",
			"block_height = VALUES(block_height),",
			"block_time = VALUES(block_time),",
			"win_ratio = VALUES(win_ratio)",
			"</script>"
	})
	void saveOrUpdateBatchByAddressAndAssetSymbol(@Param("list") List<AddressProfit> addressProfits);
}
