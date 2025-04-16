package com.creda.coin.price.service.data.doris;

import com.creda.coin.price.entity.doris.coins.TokenStatsHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.service.BaseDoris;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
public interface ITokenStatsHistoryService extends IService<TokenStatsHistory>, BaseDoris<TokenStatsHistory> {

    TokenStatsHistory getTopCoinsAtTime(String tokenAddress, long timestampMinutesAgo);
}
