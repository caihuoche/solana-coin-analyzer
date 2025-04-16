package com.creda.coin.price.service.data.doris;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.entity.doris.TokenSwapPriceHistory;
import com.creda.coin.price.service.BaseDoris;

import java.math.BigDecimal;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
public interface ITokenSwapPriceHistoryService extends IService<TokenSwapPriceHistory>, BaseDoris {

    BigDecimal getPriceAtTime(String tokenAddress, long timestampMinutesAgo);
}
