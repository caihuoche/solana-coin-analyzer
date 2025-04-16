package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.doris.coins.TokenStats5Min;
import com.creda.coin.price.mapper.TokenStats5MinMapper;
import com.creda.coin.price.service.data.doris.ITokenStats5MinService;
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
public class TokenStats5MinServiceImpl extends ServiceImpl<TokenStats5MinMapper, TokenStats5Min> implements ITokenStats5MinService {
String tableName = "token_stats_5_min";
    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public List<TokenStats5Min> getExistingDataByIds(Collection<TokenStats5Min> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return null;
        }
        QueryWrapper<TokenStats5Min> wrapper = new QueryWrapper();
        wrapper.in("token_address", entityList.stream().map(TokenStats5Min::getTokenAddress).collect(Collectors.toList()));
        return this.getBaseMapper().selectList(wrapper);
    }

    @Override
    public Object getUniqueId(TokenStats5Min entity) {
        return entity.getTokenAddress();
    }
}
