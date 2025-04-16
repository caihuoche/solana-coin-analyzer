package com.creda.coin.price.service.data.doris;

import com.creda.coin.price.entity.CandleTrade;
import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.service.BaseDoris;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-23
 */
public interface ICandleTradeService extends IService<CandleTrade> , BaseDoris {

    CandleTrade findLast();
}
