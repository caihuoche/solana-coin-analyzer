package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.doris.coins.TokenStats7Day;
import com.creda.coin.price.mapper.TokenStats7DayMapper;
import com.creda.coin.price.service.data.doris.ITokenStats7DayService;
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
public class TokenStats7DayServiceImpl extends ServiceImpl<TokenStats7DayMapper, TokenStats7Day> implements ITokenStats7DayService {
    String tableName = "token_stats_7_day";

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public List<TokenStats7Day> getExistingDataByIds(Collection<TokenStats7Day> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return null;
        }
        QueryWrapper<TokenStats7Day> wrapper = new QueryWrapper();
        wrapper.in("token_address", entityList.stream().map(TokenStats7Day::getTokenAddress).collect(Collectors.toList()));
        return this.getBaseMapper().selectList(wrapper);  }

    @Override
    public Object getUniqueId(TokenStats7Day entity) {
        return entity.getTokenAddress();
    }
}
