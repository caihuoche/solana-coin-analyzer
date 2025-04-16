package com.creda.coin.price.service;

import com.creda.coin.price.entity.ProfitCalRecord;
import com.creda.coin.price.entity.doris.coins.*;
import com.creda.coin.price.service.data.doris.*;
import com.creda.coin.price.service.data.jdbc.IProfitCalRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gavin
 * @date 2024/10/30
 **/
@Slf4j
@Component
public class TopTraderService {
    List<BaseDoris> tokenStatsListDoris;

    @Resource
    private ITokenStats1HourService tokenStats1HourService;
    @Resource
    private ITokenStats5MinService tokenStats5MinService;
    @Resource
    private ITokenStats7DayService tokenStats7DayService;
    @Resource
    private ITokenStats30DayService tokenStats30DayService;
    @Resource
    private ITokenStats24HourService tokenStats24HourService;
    @Resource
    private ITokenStatsLastTraderService tokenStatsLastTraderService;
    @Resource
    private ITraderStatsByTokenService traderStatsByTokenService;
    @Resource
    private IProfitCalRecordService profitCalRecordService;

    public void run() {
        log.info("TopTraderService run");
        ProfitCalRecord profitCalRecord = profitCalRecordService.getOne();
        Date endTime = profitCalRecord.getBlockTime();

        if (endTime == null) {
            log.info("TopTraderService run: no profitCalRecord found");
            return;
        }

        Map<String, Pair<BigDecimal, BigDecimal>> stringUserProfitDTOMap = calculateTopProfits();
        log.info("TopTraderService run: stringUserProfitDTOMap size: {}", stringUserProfitDTOMap.size());
        if (MapUtils.isEmpty(stringUserProfitDTOMap)) {
            return;
        }
        List<TokenStats> tokenStatsList = new ArrayList<>();

        for (String token : stringUserProfitDTOMap.keySet()) {
            TokenStats tokenStats = new TokenStats();
            tokenStats.setTokenAddress(token);
            tokenStats.setTraderPnl(stringUserProfitDTOMap.get(token).getLeft());
            tokenStats.setTraderRoi(stringUserProfitDTOMap.get(token).getRight());
            tokenStatsList.add(tokenStats);
        }
        ArrayList<BaseDoris> tokenStatsListDoris = new ArrayList<>();
        tokenStatsListDoris.add(tokenStats5MinService);
        tokenStatsListDoris.add(tokenStats1HourService);
        tokenStatsListDoris.add(tokenStats7DayService);
        tokenStatsListDoris.add(tokenStats30DayService);
        tokenStatsListDoris.add(tokenStats24HourService);
        for (int i = 0; i < tokenStatsListDoris.size(); i++) {
            if (tokenStatsListDoris.get(i) instanceof ITokenStats5MinService) {
                List<TokenStats5Min> tokenStats5Mins = new ArrayList<>();
                for (String token : stringUserProfitDTOMap.keySet()) {
                    TokenStats5Min tokenStats5Min = new TokenStats5Min();
                    tokenStats5Min.setTokenAddress(token);
                    tokenStats5Min.setTraderPnl(stringUserProfitDTOMap.get(token).getLeft());
                    tokenStats5Min.setTraderRoi(stringUserProfitDTOMap.get(token).getRight());
                    tokenStats5Mins.add(tokenStats5Min);
                }

                ((ITokenStats5MinService) tokenStatsListDoris.get(i)).updateBatchStreamLoad(tokenStats5Mins);
            } else if (tokenStatsListDoris.get(i) instanceof ITokenStats1HourService) {
                List<TokenStats1Hour> tokenStats1Hours = new ArrayList<>();
                for (String token : stringUserProfitDTOMap.keySet()) {
                    TokenStats1Hour tokenStats1Hour = new TokenStats1Hour();
                    tokenStats1Hour.setTokenAddress(token);
                    tokenStats1Hour.setTraderPnl(stringUserProfitDTOMap.get(token).getLeft());
                    tokenStats1Hour.setTraderRoi(stringUserProfitDTOMap.get(token).getRight());
                    tokenStats1Hours.add(tokenStats1Hour);
                }

                ((ITokenStats1HourService) tokenStatsListDoris.get(i)).updateBatchStreamLoad(tokenStats1Hours);
            } else if (tokenStatsListDoris.get(i) instanceof ITokenStats7DayService) {
                List<TokenStats7Day> tokenStats7Days = new ArrayList<>();
                for (String token : stringUserProfitDTOMap.keySet()) {
                    TokenStats7Day tokenStats7Day = new TokenStats7Day();
                    tokenStats7Day.setTokenAddress(token);
                    tokenStats7Day.setTraderPnl(stringUserProfitDTOMap.get(token).getLeft());
                    tokenStats7Day.setTraderRoi(stringUserProfitDTOMap.get(token).getRight());
                    tokenStats7Days.add(tokenStats7Day);
                }

                ((ITokenStats7DayService) tokenStatsListDoris.get(i)).updateBatchStreamLoad(tokenStats7Days);
            } else if (tokenStatsListDoris.get(i) instanceof ITokenStats30DayService) {
                List<TokenStats30Day> tokenStats30Days = new ArrayList<>();
                for (String token : stringUserProfitDTOMap.keySet()) {
                    TokenStats30Day tokenStats30Day = new TokenStats30Day();
                    tokenStats30Day.setTokenAddress(token);
                    tokenStats30Day.setTraderPnl(stringUserProfitDTOMap.get(token).getLeft());
                    tokenStats30Day.setTraderRoi(stringUserProfitDTOMap.get(token).getRight());
                    tokenStats30Days.add(tokenStats30Day);
                }

                ((ITokenStats30DayService) tokenStatsListDoris.get(i)).updateBatchStreamLoad(tokenStats30Days);
            } else if (tokenStatsListDoris.get(i) instanceof ITokenStats24HourService) {
                List<TokenStats24Hour> tokenStats24Hours = new ArrayList<>();
                for (String token : stringUserProfitDTOMap.keySet()) {
                    TokenStats24Hour tokenStats24Hour = new TokenStats24Hour();
                    tokenStats24Hour.setTokenAddress(token);
                    tokenStats24Hour.setTraderPnl(stringUserProfitDTOMap.get(token).getLeft());
                    tokenStats24Hour.setTraderRoi(stringUserProfitDTOMap.get(token).getRight());
                    tokenStats24Hours.add(tokenStats24Hour);
                }
                ((ITokenStats24HourService) tokenStatsListDoris.get(i)).updateBatchStreamLoad(tokenStats24Hours);
            }
        }
        log.info("TopTraderService run: done");
    }

    public Map<String, Pair<BigDecimal, BigDecimal>> calculateTopProfits() {
        Map<String, Pair<BigDecimal, BigDecimal>> result = new HashMap<>();
        List<TokenStatsLastTrader> tokenStatsLastTraders = tokenStatsLastTraderService.getAll();

        for (List<TokenStatsLastTrader> statsLastTraders : ListUtils.partition(tokenStatsLastTraders, 5000)) {
            List<String> tokenList = statsLastTraders.stream().map(TokenStatsLastTrader::getTokenAddress).collect(Collectors.toList());
            Map<String, Pair<BigDecimal, BigDecimal>> topProfitByTokenAddresses = traderStatsByTokenService.findTopProfitByTokenAddresses(tokenList);
            if (MapUtils.isEmpty(topProfitByTokenAddresses)) {
                continue;
            }
            result.putAll(topProfitByTokenAddresses);

        }
        return result;
    }
}
