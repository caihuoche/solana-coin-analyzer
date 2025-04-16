package com.creda.coin.price.service.data.doris;

import com.creda.coin.price.entity.doris.coins.TokenStatsLastTrader;
import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.service.BaseDoris;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
public interface ITokenStatsLastTraderService extends IService<TokenStatsLastTrader> , BaseDoris {

    TokenStatsLastTrader getTopCoinsLastTrader(String tokenAddress);

    void updateTopCoinsLastTraderByCache(TokenStatsLastTrader topCoinsLastTrader);

    List<TokenStatsLastTrader> getAll();
}
