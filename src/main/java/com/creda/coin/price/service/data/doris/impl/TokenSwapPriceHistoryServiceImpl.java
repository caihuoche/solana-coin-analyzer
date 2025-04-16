package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.doris.TokenSwapPriceHistory;
import com.creda.coin.price.mapper.TokenSwapPriceHistoryMapper;
import com.creda.coin.price.service.data.doris.ITokenSwapPriceHistoryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
public class TokenSwapPriceHistoryServiceImpl extends ServiceImpl<TokenSwapPriceHistoryMapper, TokenSwapPriceHistory> implements ITokenSwapPriceHistoryService {

    String tableName = "token_swap_price_history";
    @Override
    public BigDecimal getPriceAtTime(String tokenAddress, long timestampMinutesAgo) {
        Date date = new Date(timestampMinutesAgo);
        return this.getBaseMapper().getPriceAtTime(tokenAddress, date);
    }


    @Override
    public String getTableName() {
        return tableName;
    }
}
