package com.creda.coin.price.service.data.jdbc;

import com.creda.coin.price.entity.AssetInfo;
import com.creda.coin.price.entity.Candle;
import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.service.BaseDoris;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-09-05
 */
public interface ICandleService extends IService<Candle> , BaseDoris<Candle> {

	Candle findLastCandle(String baseToken, long interval);
}
