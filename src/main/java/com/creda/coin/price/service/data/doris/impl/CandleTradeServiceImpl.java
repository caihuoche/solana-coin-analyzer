package com.creda.coin.price.service.data.doris.impl;

import com.creda.coin.price.entity.CandleTrade;
import com.creda.coin.price.mapper.CandleDataMapper;
import com.creda.coin.price.service.data.doris.ICandleTradeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-23
 */
@Service
public class CandleTradeServiceImpl extends ServiceImpl<CandleDataMapper, CandleTrade> implements ICandleTradeService {

    @Override
    public String getTableName() {
        return "candle_trade";
    }

    @Override
    public CandleTrade findLast() {
        return this.getBaseMapper().findLast();
    }
}
