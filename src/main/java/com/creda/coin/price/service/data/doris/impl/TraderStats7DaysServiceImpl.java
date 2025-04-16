package com.creda.coin.price.service.data.doris.impl;

import com.creda.coin.price.entity.doris.traders.TraderStats7Days;
import com.creda.coin.price.mapper.TraderStats7DaysMapper;
import com.creda.coin.price.service.data.doris.ITraderStats7DaysService;
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
public class TraderStats7DaysServiceImpl extends ServiceImpl<TraderStats7DaysMapper, TraderStats7Days> implements ITraderStats7DaysService {
String tableName = "trader_stats_7_days";
    @Override
    public String getTableName() {
        return tableName;
    }
}
