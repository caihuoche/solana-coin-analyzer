package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.doris.coins.TokenStatsHistory;
import com.creda.coin.price.mapper.TokenStatsHistoryMapper;
import com.creda.coin.price.service.data.doris.ITokenStatsHistoryService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
public class TokenStatsHistoryServiceImpl extends ServiceImpl<TokenStatsHistoryMapper, TokenStatsHistory> implements ITokenStatsHistoryService {

    String tableName = "token_stats_history";
    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public TokenStatsHistory getTopCoinsAtTime(String tokenAddress, long timestampMinutesAgo) {
        Date date = new Date(timestampMinutesAgo);
        return this.getBaseMapper().getTopCoinsAtTime(tokenAddress, date);
    }
    @Override
    public List<TokenStatsHistory> getExistingDataByIds(Collection<TokenStatsHistory> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return null;
        }
        QueryWrapper<TokenStatsHistory> wrapper = new QueryWrapper();
        wrapper.in("token_address", entityList.stream().map(TokenStatsHistory::getTokenAddress).collect(Collectors.toList()));
        return this.getBaseMapper().selectList(wrapper);  }


    @Override
    public Object getUniqueId(TokenStatsHistory entity) {
        return entity.getTokenAddress();

    }
}
