package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.doris.coins.TokenStats1Hour;
import com.creda.coin.price.entity.doris.coins.TokenStats5Min;
import com.creda.coin.price.mapper.TokenStats1HourMapper;
import com.creda.coin.price.service.data.doris.ITokenStats1HourService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
public class TokenStats1HourServiceImpl extends ServiceImpl<TokenStats1HourMapper, TokenStats1Hour> implements ITokenStats1HourService {
    String tableName = "token_stats_1_hour";

    @Override
    public String getTableName() {
        return tableName;
    }
    @Override
    public List<TokenStats1Hour> getExistingDataByIds(Collection<TokenStats1Hour> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return null;
        }
        QueryWrapper<TokenStats1Hour> wrapper = new QueryWrapper();
        wrapper.in("token_address", entityList.stream().map(TokenStats1Hour::getTokenAddress).collect(Collectors.toList()));
        return this.getBaseMapper().selectList(wrapper);
    }

    @Override
    public Object getUniqueId(TokenStats1Hour entity) {
        return entity.getTokenAddress();

    }
}
