package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.doris.coins.TokenStats24Hour;
import com.creda.coin.price.mapper.TokenStats24HourMapper;
import com.creda.coin.price.service.data.doris.ITokenStats24HourService;
import org.springframework.stereotype.Service;

import java.util.Collection;
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
public class TokenStats24HourServiceImpl extends ServiceImpl<TokenStats24HourMapper, TokenStats24Hour> implements ITokenStats24HourService {
    String tableName = "token_stats_24_hour";

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public List<TokenStats24Hour> getExistingDataByIds(Collection<TokenStats24Hour> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return null;
        }
        QueryWrapper<TokenStats24Hour> wrapper = new QueryWrapper();
        wrapper.in("token_address", entityList.stream().map(TokenStats24Hour::getTokenAddress).collect(Collectors.toList()));
        return this.getBaseMapper().selectList(wrapper);
    }


    @Override
    public Object getUniqueId(TokenStats24Hour entity) {
        return entity.getTokenAddress();

    }
}