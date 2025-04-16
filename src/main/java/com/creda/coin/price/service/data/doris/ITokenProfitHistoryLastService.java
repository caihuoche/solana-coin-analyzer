package com.creda.coin.price.service.data.doris;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.dto.LastHistoryDTO;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.entity.doris.TokenProfitHistoryLast;
import com.creda.coin.price.service.BaseDoris;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
public interface ITokenProfitHistoryLastService extends IService<TokenProfitHistoryLast> , BaseDoris {

    TokenProfitHistoryLast getLastByAddressFromCache(String userAddress, String tokenAddress);

    void updateCache(List<LastHistoryDTO> lastHistoryDTOS);

    void updateProfitHistoryByCache(TokenProfitHistory profitHistory);

    void updateProfitHistoryByCache(TokenProfitHistoryLast profitHistoryLast);

    TokenProfitHistoryLast createDefaultProfitHistory(LastHistoryDTO lastHistoryDTO);
}
