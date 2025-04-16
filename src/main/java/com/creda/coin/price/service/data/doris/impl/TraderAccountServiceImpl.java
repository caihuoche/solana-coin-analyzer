package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.TraderAccount;
import com.creda.coin.price.entity.doris.TokenSwapPriceHistory;
import com.creda.coin.price.mapper.TokenSwapPriceHistoryMapper;
import com.creda.coin.price.mapper.TraderAccountMapper;
import com.creda.coin.price.service.data.doris.ITokenSwapPriceHistoryService;
import com.creda.coin.price.service.data.doris.ITraderAccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
public class TraderAccountServiceImpl extends ServiceImpl<TraderAccountMapper, TraderAccount> implements ITraderAccountService {

    String tableName = "trader_account";

    @Override
    public String getTableName() {
        return tableName;
    }
}
