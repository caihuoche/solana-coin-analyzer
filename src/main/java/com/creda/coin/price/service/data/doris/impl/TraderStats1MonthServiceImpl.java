package com.creda.coin.price.service.data.doris.impl;

import com.creda.coin.price.entity.doris.traders.TraderStats1Month;
import com.creda.coin.price.mapper.TraderStats1MonthMapper;
import com.creda.coin.price.service.data.doris.ITraderStats1MonthService;
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
public class TraderStats1MonthServiceImpl extends ServiceImpl<TraderStats1MonthMapper, TraderStats1Month> implements ITraderStats1MonthService {
String tableName = "trader_stats_1_month";
    @Override
    public String getTableName() {
        return tableName;
    }
}
