package com.creda.coin.price.service.data.doris.impl;

import com.creda.coin.price.entity.doris.traders.TraderStats3Months;
import com.creda.coin.price.mapper.TraderStats3MonthsMapper;
import com.creda.coin.price.service.data.doris.ITraderStats3MonthsService;
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
public class TraderStats3MonthsServiceImpl extends ServiceImpl<TraderStats3MonthsMapper, TraderStats3Months> implements ITraderStats3MonthsService {
String tableName = "trader_stats_3_months";
    @Override
    public String getTableName() {
        return tableName;
    }
}
