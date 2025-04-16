package com.creda.coin.price.service.data.doris.impl;

import com.creda.coin.price.entity.doris.traders.TraderStats1Day;
import com.creda.coin.price.mapper.TraderStats1DayMapper;
import com.creda.coin.price.service.data.doris.ITraderStats1DayService;
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
public class TraderStats1DayServiceImpl extends ServiceImpl<TraderStats1DayMapper, TraderStats1Day> implements ITraderStats1DayService {
String tableName = "trader_stats_1_day";
    @Override
    public String getTableName() {
        return tableName;
    }
}
