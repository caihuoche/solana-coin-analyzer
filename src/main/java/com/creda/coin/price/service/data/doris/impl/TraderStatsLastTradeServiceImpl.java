package com.creda.coin.price.service.data.doris.impl;

import com.creda.coin.price.entity.doris.traders.TraderStatsLastTrade;
import com.creda.coin.price.mapper.TraderStatsLastTradeMapper;
import com.creda.coin.price.service.data.doris.ITraderStatsLastTradeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
public class TraderStatsLastTradeServiceImpl extends ServiceImpl<TraderStatsLastTradeMapper, TraderStatsLastTrade> implements ITraderStatsLastTradeService {
String tableName = "trader_stats_last_trade";
    @Override
    public String getTableName() {
        return tableName;
    }
}
