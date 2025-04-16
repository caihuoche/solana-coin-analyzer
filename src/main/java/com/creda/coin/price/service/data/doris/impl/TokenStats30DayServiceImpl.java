package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.doris.coins.TokenStats30Day;
import com.creda.coin.price.mapper.TokenStats30DayMapper;
import com.creda.coin.price.service.data.doris.ITokenStats30DayService;
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
public class TokenStats30DayServiceImpl extends ServiceImpl<TokenStats30DayMapper, TokenStats30Day> implements ITokenStats30DayService {
    String tableName = "token_stats_30_day";

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public List<TokenStats30Day> getExistingDataByIds(Collection<TokenStats30Day> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return null;
        }
        QueryWrapper<TokenStats30Day> wrapper = new QueryWrapper();
        wrapper.in("token_address", entityList.stream().map(TokenStats30Day::getTokenAddress).collect(Collectors.toList()));
        return this.getBaseMapper().selectList(wrapper);
    }

    @Override
    public Object getUniqueId(TokenStats30Day entity) {
        return entity.getTokenAddress();

    }
}
