package com.creda.coin.price.service.data.jdbc;

import java.util.List;

import com.creda.coin.price.dto.TokenLiquidityDTO;
import com.creda.coin.price.entity.TokenBalanceHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gain
 * @since 2024-09-13
 */
public interface ITokenBalanceHistoryService extends IService<TokenBalanceHistory> {

	List<TokenLiquidityDTO> getLatestTokenLiquidity();

}
