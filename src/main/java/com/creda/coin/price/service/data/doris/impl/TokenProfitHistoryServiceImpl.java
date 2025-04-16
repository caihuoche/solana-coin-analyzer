package com.creda.coin.price.service.data.doris.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.dto.TokenHoldersCountDTO;
import com.creda.coin.price.entity.CandleTrade;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.mapper.TokenProfitHistoryMapper;
import com.creda.coin.price.service.data.doris.ITokenProfitHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Service
@Slf4j
public class TokenProfitHistoryServiceImpl extends ServiceImpl<TokenProfitHistoryMapper, TokenProfitHistory> implements ITokenProfitHistoryService {
    String tableName = "token_profit_history";

    @Override
    public List<TokenHoldersCountDTO> searchCountByTokenAddresses(List<String> tokenAddresses) {
        long currentTimeMillis = System.currentTimeMillis();
        List<TokenHoldersCountDTO> tokenHoldersCountDTOS = this.getBaseMapper().searchCountByTokenAddresses(tokenAddresses);
        if (System.currentTimeMillis() - currentTimeMillis > 30) {
            log.info("searchCountByTokenAddresses 耗时:" + (System.currentTimeMillis() - currentTimeMillis));
        }
        return tokenHoldersCountDTOS;
    }

    @Override
    public long searchCountByTokenAddress(String tokenAddress) {
        TokenHoldersCountDTO result = this.getBaseMapper().searchCountByTokenAddress(tokenAddress);
        return result == null ? 0 : result.getHoldersCount();
    }

    @Override
    public List<TokenProfitHistory> getAccountsTokenProfitsAtTime(Set<String> accounts, long time) {
        return this.getBaseMapper().getAccountsTokenProfitsAtTime(accounts, time);
    }

    @Override
    public TokenProfitHistory findLast() {
        return this.getBaseMapper().findLast();
    }

    @Override
    public TokenProfitHistory findFirstByAddress(String baseToken) {
        return this.getBaseMapper().findFirstByAddress(baseToken);
    }

    @Override
    public List<TokenProfitHistory> findByAddressAndTimeRange(String assetAddress, Date nextCandleStartTime, Date nextCandleEndTime) {
        return this.getBaseMapper().findByAddressAndTimeRange(assetAddress, nextCandleStartTime, nextCandleEndTime);
    }

    @Override
    public TokenProfitHistory findLastBeforeTime(String assetAddress, Date nextCandleStartTime) {
        return this.getBaseMapper().findLastBeforeTime(assetAddress, nextCandleStartTime);
    }

    @Override
    public List<CandleTrade> getCandleDataByTimeRange(Date startTime, Date endTime) {
        return this.getBaseMapper().getCandleDataByTimeRange(startTime, endTime);
    }

    @Override
    public TokenProfitHistory finFirst() {
        return this.getBaseMapper().finFirst();
    }


    @Override
    public String getTableName() {
        return tableName;
    }
}
