package com.creda.coin.price.service.data.jdbc.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.dto.TokenLiquidityDTO;
import com.creda.coin.price.entity.TokenBalanceHistory;
import com.creda.coin.price.mapper.TokenBalanceHistoryMapper;
import com.creda.coin.price.service.data.jdbc.ITokenBalanceHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gain
 * @since 2024-09-13
 */
@Service
public class TokenBalanceHistoryServiceImpl extends ServiceImpl<TokenBalanceHistoryMapper, TokenBalanceHistory> implements ITokenBalanceHistoryService {

	@Override
	public List<TokenLiquidityDTO> getLatestTokenLiquidity() {
		return null;
	}
}
