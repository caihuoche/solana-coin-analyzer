package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.dto.TokenLiquidityDTO;
import com.creda.coin.price.entity.TokenBalanceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gain
 * @since 2024-09-13
 */
@Mapper
public interface TokenBalanceHistoryMapper extends BaseMapper<TokenBalanceHistory> {
	@Select("SELECT t1.token0_address, t1.token1_address, t1.token0_balance, t1.token1_balance, t1.block_time " +
			"FROM token_balance_history t1 " +
			"INNER JOIN ( " +
			"    SELECT token1_address, MAX(block_time) AS latest_block_time " +
			"    FROM token_balance_history " +
			"    GROUP BY token1_address " +
			") t2 ON t1.token1_address = t2.token1_address AND t1.block_time = t2.latest_block_time")
	List<TokenLiquidityDTO> getLatestTokenLiquidity();


}
