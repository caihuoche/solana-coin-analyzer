package com.creda.coin.price.dto;

import com.creda.coin.price.entity.AssetInfo;
import com.creda.coin.price.entity.RaydiumSwapPool;
import com.creda.coin.price.entity.doris.CurrentPrice;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.entity.doris.TokenProfitHistoryLast;
import com.creda.coin.price.entity.doris.TokenSwapPriceHistory;
import com.creda.coin.price.entity.doris.coins.TokenStats;
import com.creda.coin.price.entity.doris.coins.TokenStatsLastTrader;
import com.creda.coin.price.entity.doris.traders.TraderStats;
import com.creda.coin.price.entity.doris.traders.TraderStatsByToken;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gavin
 * @date 2024/10/15
 **/
@Data
public class ProfitAnalyzerDTO {
    private List<TokenProfitHistory> tokenProfitHistoryList;
    private List<Erc20HandlerDTO> erc20HandlerDTOS;
    private Map<String, TokenProfitHistoryLast> tokenProfitHistoryLastMap;
    private List<TokenSwapPriceHistory> tokenSwapPriceHistoryList;
    private List<CurrentPrice> currentPriceList;     // todo -> map
    // private List<TraderStats1Day> traderStats1DayList;
    // private List<TraderStats1Month> traderStats1MonthList;
    // private List<TraderStats3Months> traderStats3MonthsList;
    // private List<TraderStats7Days> traderStats7DaysList;
    private List<TraderStats> traderStatsList;
    private List<TraderStatsByToken> traderStatsByTokenList;
    private Map<String, TokenStats> tokenStatsMap;
    private Map<String, TokenStatsLastTrader> tokenStatsLastTraderMap;
    private List<TokenStatsLastTrader> topCoinsLastTraderList;
    private List<RaydiumSwapPool> raydiumSwapPools;
    private List<AssetInfo> assetInfos;

    private List<String> vaultTokens;

    private boolean vaultTokensPreFinish = false;


    public ProfitAnalyzerDTO() {
        erc20HandlerDTOS = new ArrayList<>();
        tokenProfitHistoryLastMap = new HashMap<>();
        tokenProfitHistoryList = new ArrayList<>();
        tokenSwapPriceHistoryList = new ArrayList<>();
        currentPriceList = new ArrayList<>();
        // traderStats1DayList = new ArrayList<>();
        // traderStats1MonthList = new ArrayList<>();
        // traderStats3MonthsList = new ArrayList<>();
        // traderStats7DaysList = new ArrayList<>();
        traderStatsList = new ArrayList<>();
        traderStatsByTokenList = new ArrayList<>();
        tokenStatsMap = new HashMap<>();
        tokenStatsLastTraderMap = new HashMap<>();
        raydiumSwapPools =  new ArrayList<>();;
        assetInfos = new ArrayList<>();
        vaultTokens = new ArrayList<>();
    }

}
