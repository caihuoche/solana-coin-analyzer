package com.creda.coin.price.service.data.doris;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.entity.doris.traders.TraderStatsByToken;
import com.creda.coin.price.service.BaseDoris;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
public interface ITraderStatsByTokenService extends IService<TraderStatsByToken>, BaseDoris {

    List<TraderStatsByToken> getTraderStatsByAccounts(Set<String> accounts);

    Map<String, Pair<BigDecimal, BigDecimal>> findTopProfitByTokenAddresses(List<String> tokenList);

    void distintSaveOrUpdateBatch(List<TraderStatsByToken> traderStats);

}
