package com.creda.coin.price.service.data.doris;


import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.dto.TokenHoldersCountDTO;
import com.creda.coin.price.entity.CandleTrade;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.service.BaseDoris;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
public interface ITokenProfitHistoryService extends IService<TokenProfitHistory>, BaseDoris {
    List<TokenHoldersCountDTO> searchCountByTokenAddresses(List<String> tokenAddresses);

    long searchCountByTokenAddress(String tokenAddress);

    List<TokenProfitHistory> getAccountsTokenProfitsAtTime(Set<String> accounts, long time);

    TokenProfitHistory findLast();

    TokenProfitHistory findFirstByAddress(String baseToken);

    List<TokenProfitHistory> findByAddressAndTimeRange(String baseToken, Date date, Date date1);

    TokenProfitHistory findLastBeforeTime(String baseToken, Date date);

    List<CandleTrade> getCandleDataByTimeRange(Date startTime, Date endTime);

    TokenProfitHistory finFirst();
}
