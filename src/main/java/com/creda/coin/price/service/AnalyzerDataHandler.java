package com.creda.coin.price.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import com.creda.coin.price.ProfitAnalyzerContext;
import com.creda.coin.price.constant.SolConstant;
import com.creda.coin.price.dto.Erc20HandlerDTO;
import com.creda.coin.price.dto.LastHistoryDTO;
import com.creda.coin.price.dto.SwapInstructionExtraInfo;
import com.creda.coin.price.entity.AssetInfo;
import com.creda.coin.price.entity.doris.CurrentPrice;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.entity.doris.TokenProfitHistoryLast;
import com.creda.coin.price.entity.doris.TokenSwapPriceHistory;
import com.creda.coin.price.entity.doris.coins.TokenStats;
import com.creda.coin.price.entity.doris.coins.TokenStatsHistory;
import com.creda.coin.price.entity.doris.coins.TokenStatsLastTrader;
import com.creda.coin.price.entity.doris.traders.TraderStatsByToken;
import com.creda.coin.price.entity.es.SolanaTransaction;
import com.creda.coin.price.service.data.doris.ICurrentPriceService;
import com.creda.coin.price.service.data.doris.ITokenProfitHistoryLastService;
import com.creda.coin.price.service.data.doris.ITokenStatsHistoryService;
import com.creda.coin.price.service.data.doris.ITokenStatsLastTraderService;
import com.creda.coin.price.service.data.doris.ITokenSwapPriceHistoryService;
import com.creda.coin.price.service.data.jdbc.IAssetInfoService;
import com.creda.coin.price.util.BigDecimalUtils;
import com.creda.coin.price.util.UniqueIdUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author gavin
 * @date 2024/10/15
 **/
@Service
@Slf4j
public class AnalyzerDataHandler {

    @Resource
    private ITokenProfitHistoryLastService tokenProfitHistoryLastService;
    @Resource
    private ICurrentPriceService currentPriceService;
    @Resource
    private IAssetInfoService assetInfoService;
    @Resource
    private ITokenSwapPriceHistoryService tokenSwapPriceHistoryService;
    @Resource
    private ITokenStatsLastTraderService tokenStatsLastTraderService;
    @Resource
    private ITokenStatsHistoryService tokenStatsHistoryService;

    public void preHandleErc20(List<Erc20HandlerDTO> erc20HandlerDTOS) {
        List<LastHistoryDTO> lastHistoryDTOS = new ArrayList<>();
        for (Erc20HandlerDTO erc20HandlerDTO : erc20HandlerDTOS) {
            TokenProfitHistoryLast lastByAddressFromCache = tokenProfitHistoryLastService.getLastByAddressFromCache(erc20HandlerDTO.getUserAddress(), erc20HandlerDTO.getTokenAddress());
            if (lastByAddressFromCache == null) {
                lastHistoryDTOS.add(new LastHistoryDTO(erc20HandlerDTO.getUserAddress(), erc20HandlerDTO.getTokenAddress()));
            }

        }
        lastHistoryDTOS = removeDuplicates(lastHistoryDTOS);
        long start = System.currentTimeMillis();
        tokenProfitHistoryLastService.updateCache(lastHistoryDTOS);
        log.info("updateCache cost {} ms", System.currentTimeMillis() - start);

    }

    public List<LastHistoryDTO> removeDuplicates(List<LastHistoryDTO> lastHistoryDTOS) {
        if (lastHistoryDTOS == null) {
            return Collections.emptyList(); // Return an empty list if input is null
        }

        Set<String> seen = new HashSet<>();
        List<LastHistoryDTO> uniqueList = new ArrayList<>();

        for (LastHistoryDTO dto : lastHistoryDTOS) {
            String uniqueKey = dto.getUserAddress() + "_" + dto.getTokenAddress();
            if (seen.add(uniqueKey)) { // Add returns false if the key was already present
                uniqueList.add(dto);
            }
        }

        return uniqueList;
    }

    /**
     * 卖出 erc20
     */
    public void sellErc20(SolanaTransaction transaction, String userAddress, String tokenAddress, SwapInstructionExtraInfo extraInfo) {
        Erc20HandlerDTO handlerDTO = new Erc20HandlerDTO(transaction, userAddress, tokenAddress, extraInfo, null, 2);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getErc20HandlerDTOS().add(handlerDTO);
    }

    public void sellErc20Ext(SolanaTransaction transaction, String userAddress, String tokenAddress, SwapInstructionExtraInfo extraInfo) {
        try {
            if (StringUtils.isBlank(userAddress)) {
                return;
            }
            Pair<BigDecimal, BigDecimal> pair = getPrice(tokenAddress, extraInfo, transaction,2);
//            log.info("sellErc20Ext :{}, extraInfo:{}, hash:{}", JsonUtil.toJson(pair),JsonUtil.toJson(extraInfo),transaction);
            if (pair == null) {
                return;
            }
            if (tokenAddress.equals(SolConstant.USDC_ADDRESS) || tokenAddress.equals(SolConstant.SOL_ADDRESS)) {
                return;
            }
            BigDecimal currentPrice = pair.getKey();
            BigDecimal currentSoldAmount = pair.getValue();
            //	判断是 balance=0,就返回 null
            if (currentSoldAmount.compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
            long start = System.currentTimeMillis();
            TokenProfitHistoryLast oldProfitHistory = tokenProfitHistoryLastService.getLastByAddressFromCache(userAddress, tokenAddress);
            if (System.currentTimeMillis() - start > 30) {
                log.info("getLastByAddress sellErc20:{}", System.currentTimeMillis() - start);
            }

            TokenProfitHistory profitHistory = new TokenProfitHistory();
            long tokenProfitHistoryId = transaction.getTokenProfitHistoryId();
            profitHistory.setId(transaction.getId() * 1000 + tokenProfitHistoryId);
            tokenProfitHistoryId++;
            transaction.setTokenProfitHistoryId(tokenProfitHistoryId);
            profitHistory.setAccount(userAddress);
            profitHistory.setTokenAddress(tokenAddress);
            profitHistory.setAmount(currentSoldAmount);
            profitHistory.setType(2);

            // 卖出时总成本不变;
            BigDecimal totalCost = oldProfitHistory.getTotalCost();
            profitHistory.setTotalCost(totalCost);

            profitHistory.setCurrentPrice(currentPrice);
            profitHistory.setBoughtCount(oldProfitHistory.getBoughtCount());

            BigDecimal totalAmount = extraInfo.getPostAmount();                 // 当前交易完成后拥有的token数量
            profitHistory.setTotalAmount(totalAmount);
            profitHistory.setBoughtAmount(oldProfitHistory.getBoughtAmount());

            BigDecimal oldSoldAmount = oldProfitHistory.getSoldAmount();
            BigDecimal totalSoldAmount = oldSoldAmount.add(currentSoldAmount);
            profitHistory.setSoldAmount(totalSoldAmount);

            BigDecimal historicalHoldingAvgPrice;
            Integer isWin;
            if (oldProfitHistory.getId() == null) {
                isWin = null;
                historicalHoldingAvgPrice = BigDecimal.ZERO;
            } else {
                BigDecimal oldHoldingAvgPrice = oldProfitHistory.getHistoricalHoldingAvgPrice();
                BigDecimal oldBoughtAmount = oldProfitHistory.getBoughtAmount();
                if (oldBoughtAmount.compareTo(BigDecimal.ZERO) == 0) {
                    isWin = null;
                    historicalHoldingAvgPrice = BigDecimal.ZERO;
                } else {
                    // null, 0, 1
                    isWin = currentPrice.compareTo(oldHoldingAvgPrice) > 0 ? 1 : 0;
                    historicalHoldingAvgPrice = oldHoldingAvgPrice;
                }
            }
            profitHistory.setHistoricalHoldingAvgPrice(historicalHoldingAvgPrice);
            profitHistory.setIsWin(isWin);
            long sellCountWin = oldProfitHistory.getSoldCountWin() + (isWin == null ? 0 : isWin);
            profitHistory.setSoldCountWin(sellCountWin);

            long sellCount = oldProfitHistory.getSoldCount() + 1;
            profitHistory.setSoldCount(sellCount);

            Long oldSoldCountBoughtByUser = oldProfitHistory.getSoldCountBoughtByUser();
            profitHistory.setSoldCountBoughtByUser(
                oldSoldCountBoughtByUser == null
                ? 0
                : oldSoldCountBoughtByUser + (isWin == null ? 0 : 1)
            );
            // 卖出均价 = 历史卖出均价*历史卖出数量+当前卖出数量*当前卖出价格/总数量
            // 当前卖出 = 当前数量 * 当前币价
            // 已实现利润 = 代币价格*已售数量-平均持仓成本*已售数量+之前的已实现利润
            // 已实现利润=总收入−总成本, 总收入=代币价格卖出均价×已售数量, 总成本=平均持仓成本×已售数量,
            // 未实现利润=持仓价值−持仓成本，持仓价值=当前市场价格×持有数量，持仓成本=平均购买价格×持有数量+相关交易费用

            BigDecimal oldSoldAvgPrice = oldProfitHistory.getHistoricalSoldAvgPrice();
            BigDecimal oldSoldBalance = oldSoldAvgPrice.multiply(oldSoldAmount);
            BigDecimal currentSoldBalance = currentPrice.multiply(currentSoldAmount);
            BigDecimal historicalSoldAvgPrice = oldSoldBalance.add(currentSoldBalance).divide(oldSoldAmount.add(currentSoldAmount), 18, RoundingMode.HALF_UP);
            profitHistory.setHistoricalSoldAvgPrice(historicalSoldAvgPrice);

            BigDecimal totalBoughtAmountHasBeenLeft = oldProfitHistory.getTotalBoughtAmountHasBeenLeft() == null ? BigDecimal.ZERO : oldProfitHistory.getTotalBoughtAmountHasBeenLeft();
            BigDecimal holdingAvgPrice = oldProfitHistory.getHoldingAvgPrice() == null ? BigDecimal.ZERO : oldProfitHistory.getHoldingAvgPrice();
            if (totalBoughtAmountHasBeenLeft.compareTo(BigDecimal.ZERO) > 0) {
                if (currentSoldAmount.subtract(totalBoughtAmountHasBeenLeft).compareTo(BigDecimal.ZERO) > 0 ) {
                    BigDecimal realizedProfit = currentPrice.subtract(holdingAvgPrice).multiply(totalBoughtAmountHasBeenLeft).add(oldProfitHistory.getRealizedProfit());
                    BigDecimal unrealizedProfit = BigDecimal.ZERO;
                    profitHistory.setRealizedProfit(realizedProfit);
                    profitHistory.setUnrealizedProfit(unrealizedProfit);

                    BigDecimal roi = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : realizedProfit.add(unrealizedProfit).divide(totalCost, 4, RoundingMode.HALF_UP);
                    profitHistory.setRoi(roi);
                    BigDecimal unrealizedROI = totalCost.compareTo(BigDecimal.ZERO) == 0 ?BigDecimal.ZERO :  unrealizedProfit.divide(totalCost, 4, RoundingMode.HALF_UP);
                    BigDecimal realizedROI = totalCost.compareTo(BigDecimal.ZERO) == 0 ?BigDecimal.ZERO :  realizedProfit.divide(totalCost, 4, RoundingMode.HALF_UP);
                    profitHistory.setUnrealizedRoi(unrealizedROI);
                    profitHistory.setRealizedRoi(realizedROI); // profitHistory

                    profitHistory.setTotalBoughtAmountHasBeenLeft(BigDecimal.ZERO);
                    holdingAvgPrice = BigDecimal.ZERO;
                } else {
                    BigDecimal realizedProfit = currentPrice.subtract(holdingAvgPrice).multiply(currentSoldAmount).add(oldProfitHistory.getRealizedProfit());
                    BigDecimal unrealizedProfit = currentPrice.subtract(holdingAvgPrice).multiply(totalBoughtAmountHasBeenLeft.subtract(currentSoldAmount));
                    profitHistory.setRealizedProfit(realizedProfit);
                    profitHistory.setUnrealizedProfit(unrealizedProfit);

                    BigDecimal roi = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : realizedProfit.add(unrealizedProfit).divide(totalCost, 4, RoundingMode.HALF_UP);
                    profitHistory.setRoi(roi);
                    BigDecimal unrealizedROI = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : unrealizedProfit.divide(totalCost, 4, RoundingMode.HALF_UP);
                    BigDecimal realizedROI = totalCost.compareTo(BigDecimal.ZERO) == 0 ?BigDecimal.ZERO :  realizedProfit.divide(totalCost, 4, RoundingMode.HALF_UP);
                    profitHistory.setUnrealizedRoi(unrealizedROI);
                    profitHistory.setRealizedRoi(realizedROI); //

                    BigDecimal currentTotalBoughtAmountHasBeenLeft = totalBoughtAmountHasBeenLeft.subtract(currentSoldAmount);
                    profitHistory.setTotalBoughtAmountHasBeenLeft(currentTotalBoughtAmountHasBeenLeft);

                    if (totalBoughtAmountHasBeenLeft.subtract(currentSoldAmount).compareTo(BigDecimal.ZERO) == 0) {
                        holdingAvgPrice = BigDecimal.ZERO;
                    }
                }
            } else {
                profitHistory.setHistoricalSoldAvgPrice(oldSoldAvgPrice);
                profitHistory.setRealizedProfit(oldProfitHistory.getRealizedProfit());
                profitHistory.setUnrealizedProfit(oldProfitHistory.getUnrealizedProfit());
                profitHistory.setRoi(oldProfitHistory.getRoi());
                profitHistory.setRealizedRoi(oldProfitHistory.getRealizedRoi());
                profitHistory.setUnrealizedRoi(oldProfitHistory.getUnrealizedRoi());
                profitHistory.setTotalBoughtAmountHasBeenLeft(BigDecimal.ZERO);
            }
            profitHistory.setHoldingAvgPrice(holdingAvgPrice); // 一定要在这里调用，不能调顺序

            profitHistory.setTransferInAmount(oldProfitHistory.getTransferInAmount());
            profitHistory.setTransferOutAmount(oldProfitHistory.getTransferOutAmount());
            profitHistory.setTransferInCount(oldProfitHistory.getTransferInCount());
            profitHistory.setTransferOutCount(oldProfitHistory.getTransferOutCount());

            profitHistory.setBlockTime(transaction.getBlockTime());
            profitHistory.setBlockHeight(transaction.getSlot());
            profitHistory.setTxHash(transaction.getHash());

            profitHistory.formatAmountFields();
            ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenProfitHistoryList().add(profitHistory);
            TokenProfitHistoryLast tokenProfitHistoryLast = new TokenProfitHistoryLast(profitHistory);
            ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenProfitHistoryLastMap().put(tokenProfitHistoryLast.getAccount() + "_" + tokenProfitHistoryLast.getTokenAddress(), tokenProfitHistoryLast);
            tokenProfitHistoryLastService.updateProfitHistoryByCache(profitHistory);
        } catch (Exception e) {
            log.error("sellErc20 error: {}", e);
            // 停止程序
            System.exit(0);
        }
    }

    /**
     * 买入 erc20
     */
    public void buyErc20(SolanaTransaction transaction, String userAddress, String tokenAddress, SwapInstructionExtraInfo extraInfo) {
        Erc20HandlerDTO handlerDTO = new Erc20HandlerDTO(transaction, userAddress, tokenAddress, extraInfo, null, 1);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getErc20HandlerDTOS().add(handlerDTO);
    }

    public void buyErc20Ext(SolanaTransaction transaction, String userAddress, String tokenAddress, SwapInstructionExtraInfo extraInfo) {
        try {
            if (StringUtils.isBlank(userAddress)) {
                return;
            }
            long start = System.currentTimeMillis();

            Pair<BigDecimal, BigDecimal> pair = getPrice(tokenAddress, extraInfo, transaction,1);
//            log.info("buyErc20Ext :{}, extraInfo:{}, hash:{}", JsonUtil.toJson(pair),JsonUtil.toJson(extraInfo),transaction);

            if (System.currentTimeMillis() - start > 30) {
                log.info("getPrice:{}", System.currentTimeMillis() - start);
            }

            if (pair == null) {
                return;
            }
            if (tokenAddress.equals(SolConstant.USDC_ADDRESS) || tokenAddress.equals(SolConstant.SOL_ADDRESS)) {
                return;
            }
            BigDecimal currentPrice = pair.getKey();
            BigDecimal currentBoughtAmount = pair.getValue();
            if (currentBoughtAmount.compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
            start = System.currentTimeMillis();
            TokenProfitHistoryLast oldProfitHistory = tokenProfitHistoryLastService.getLastByAddressFromCache(userAddress, tokenAddress);
            if (System.currentTimeMillis() - start > 30) {
                log.info("getLastByAddress: buyErc20{}", System.currentTimeMillis() - start);
            }

            long tokenProfitHistoryId = transaction.getTokenProfitHistoryId();
            TokenProfitHistory profitHistory = new TokenProfitHistory();
            profitHistory.setId(transaction.getId() * 1000 + tokenProfitHistoryId);
            tokenProfitHistoryId++;
            transaction.setTokenProfitHistoryId(tokenProfitHistoryId);
            profitHistory.setTokenAddress(tokenAddress);
            profitHistory.setAmount(currentBoughtAmount);
            profitHistory.setAccount(userAddress);
            profitHistory.setType(1);
            profitHistory.setCurrentPrice(currentPrice);

            BigDecimal totalAmount = extraInfo.getPostAmount();  // 当前交易结束时拥有的token数量
            profitHistory.setTotalAmount(totalAmount);

            BigDecimal oldTotalBoughtAmountHasBeenLeft = oldProfitHistory.getTotalBoughtAmountHasBeenLeft() == null ? BigDecimal.ZERO : oldProfitHistory.getTotalBoughtAmountHasBeenLeft();
            BigDecimal currentLeft = oldTotalBoughtAmountHasBeenLeft.add(currentBoughtAmount);
            profitHistory.setTotalBoughtAmountHasBeenLeft(currentLeft);

            // 持仓均价 = 总成本 / 总的买入数量
            BigDecimal currentHoldingAvgPrice;
            if (oldTotalBoughtAmountHasBeenLeft.compareTo(BigDecimal.ZERO) == 0) {
                // 说明上一个买卖循环结束了, 这次是重新购买的一笔
                currentHoldingAvgPrice = currentPrice;
            } else {
                // 说明买的 token 尚未卖完,
                BigDecimal oldHoldingAvgPrice = oldProfitHistory.getHoldingAvgPrice();
                currentHoldingAvgPrice = (oldTotalBoughtAmountHasBeenLeft.multiply(oldHoldingAvgPrice).add(currentPrice.multiply(currentBoughtAmount)))
                    .divide(oldTotalBoughtAmountHasBeenLeft.add(currentBoughtAmount), 18, RoundingMode.HALF_UP);
            }
            profitHistory.setHoldingAvgPrice(currentHoldingAvgPrice);

            // 卖出均价 = 历史卖出均价*历史卖出数量+当前卖出数量*当前卖出价格/总数量    买入不变
            BigDecimal totalSoldAmount = oldProfitHistory.getSoldAmount();
            profitHistory.setSoldAmount(totalSoldAmount);

            BigDecimal soldAvgPrice = oldProfitHistory.getHistoricalSoldAvgPrice();
            profitHistory.setHistoricalSoldAvgPrice(soldAvgPrice);

            // 总成本 = 充值金额 + 总成本;
            BigDecimal currentBoughtBalance = currentPrice.multiply(currentBoughtAmount);
            BigDecimal totalCost = oldProfitHistory.getTotalCost().add(currentBoughtBalance);
            profitHistory.setTotalCost(totalCost);

            BigDecimal oldBoughtAmount = oldProfitHistory.getBoughtAmount();
            BigDecimal totalBoughtAmount = oldBoughtAmount.add(currentBoughtAmount);
            profitHistory.setBoughtAmount(totalBoughtAmount);

            BigDecimal holdingAvgPrice = totalCost.divide(totalBoughtAmount, 18, RoundingMode.HALF_UP);
            profitHistory.setHistoricalHoldingAvgPrice(holdingAvgPrice);

            // 已实现利润 = 原先不变  =已实现\利润=代币\价格*已售\数量-平均\持仓\成本*已售\数量
            BigDecimal realizedProfit = oldProfitHistory.getRealizedProfit();
            profitHistory.setRealizedProfit(realizedProfit);
            // 未实现利润 = 未实现\利润=持仓\价值-持仓\成本
            BigDecimal unrealizedProfit = currentPrice.subtract(currentHoldingAvgPrice).multiply(profitHistory.getTotalBoughtAmountHasBeenLeft()).stripTrailingZeros();
            profitHistory.setUnrealizedProfit(unrealizedProfit);

            BigDecimal roi = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : realizedProfit.add(unrealizedProfit).divide(totalCost, 4, RoundingMode.HALF_UP);
            //unrealizedROI = unrealizedPnL / totalCost
            //	realizedROI = realizedPnL / totalCost
            BigDecimal unrealizedROI = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : unrealizedProfit.divide(totalCost, 4, RoundingMode.HALF_UP);
            BigDecimal realizedROI = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : realizedProfit.divide(totalCost, 4, RoundingMode.HALF_UP);


            profitHistory.setRoi(roi);

            profitHistory.setRealizedRoi(realizedROI);
            profitHistory.setUnrealizedRoi(unrealizedROI);
            long sellCount = oldProfitHistory.getSoldCount();
            long sellCountWin = oldProfitHistory.getSoldCountWin();
            long oldSoldCountBoughtByUser = oldProfitHistory.getSoldCountBoughtByUser();
            profitHistory.setIsWin(null);
            profitHistory.setSoldCount(sellCount);
            profitHistory.setSoldCountWin(sellCountWin);
            profitHistory.setSoldCountBoughtByUser(oldSoldCountBoughtByUser);

            long buyCount = oldProfitHistory.getBoughtCount() + 1;
            profitHistory.setBoughtCount(buyCount);

            profitHistory.setBlockTime(transaction.getBlockTime());
            profitHistory.setBlockHeight(transaction.getSlot());
            profitHistory.setTxHash(transaction.getHash());

            profitHistory.setTransferInAmount(oldProfitHistory.getTransferInAmount());
            profitHistory.setTransferOutAmount(oldProfitHistory.getTransferOutAmount());
            profitHistory.setTransferInCount(oldProfitHistory.getTransferInCount());
            profitHistory.setTransferOutCount(oldProfitHistory.getTransferOutCount());

            profitHistory.formatAmountFields();

            ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenProfitHistoryList().add(profitHistory);
            TokenProfitHistoryLast tokenProfitHistoryLast = new TokenProfitHistoryLast(profitHistory);
            ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenProfitHistoryLastMap().put(tokenProfitHistoryLast.getAccount() + "_" + tokenProfitHistoryLast.getTokenAddress(), tokenProfitHistoryLast);
            tokenProfitHistoryLastService.updateProfitHistoryByCache(profitHistory);
        } catch (Exception e) {
            log.error("buyErc20 error", e);
            System.exit(0);
        }
    }

    /**
     * top coins
     * 1. Token: meme coin   1s     (不受右上角时间筛选影响)
     * 2. Top 1 trader:    读取历史记录表6000w的数据 , 读取 address_profit  受右上角时间筛选影响
     * 3. TXs:  第一个193s  读取历史记录表6000w的数据,  实时存储数据到新token历史表   受右上角时间筛选影响
     * 4. MCAP:  基于Price,259s 	  读取历史记录表6000w的数据, 实时计算异步处理  (不受右上角时间筛选影响)
     * 5. LIquidity:   ????  (不受右上角时间筛选影响)
     * 6. Holders:   32s    读取历史记录表6000w的数据, 实时计算异步处理  (不受右上角时间筛选影响)
     * 7. Volume:     第一个193s   读取历史记录表6000w的数据 , 实时存储数据到新token历史表  受右上角时间筛选影响
     * 8. Price:		  259s 		 读取历史记录表6000w的数据, 实时计算异步处理  (不受右上角时间筛选影响)
     * 9. 5m, 1h, 24h, 7d: 过去 5 分钟、1 小时、24 小时、7 天涨跌幅,   566s  读取历史记录表6000w的数据,  ???实时计算看看效果 试试swap表价格计算  (不受右上角时间筛选影响)
     * <p>
     * 不受时间影响
     * 1,4,5,8,    3,7
     * <p>
     * 受时间影响
     * 2, 3,7
     * 3,7
     * token, 截止当前txcount,  tx_time, 截止当前交易额,
     * <p>
     * 5,1   开始,结束
     * <p>
     * 2,5,9
     *
     * @param profitHistory
     */
    public Triple<TokenStatsLastTrader, TokenStats, TokenStatsHistory> calculateTopCoins(TokenProfitHistory profitHistory) {
        TokenStatsLastTrader topCoinsLastTrader = tokenStatsLastTraderService.getTopCoinsLastTrader(profitHistory.getTokenAddress());
        if (profitHistory.getType() == 1) {
            topCoinsLastTrader.setBoughtCount(topCoinsLastTrader.getBoughtCount() + 1);
        } else if (profitHistory.getType() == 2) {
            topCoinsLastTrader.setSoldCount(topCoinsLastTrader.getSoldCount() + 1);
        } else if (profitHistory.getType() == 3) {
            topCoinsLastTrader.setTransferInCount(topCoinsLastTrader.getTransferInCount() + 1);
        } else if (profitHistory.getType() == 4) {
            topCoinsLastTrader.setTransferOutCount(topCoinsLastTrader.getTransferOutCount() + 1);
        }
        topCoinsLastTrader.setVolume(topCoinsLastTrader.getVolume().add(profitHistory.getCurrentPrice().multiply(profitHistory.getAmount())));
        topCoinsLastTrader.setBlockTime(profitHistory.getBlockTime());
        topCoinsLastTrader.setBlockHeight(profitHistory.getBlockHeight());
        topCoinsLastTrader.setTokenAddress(profitHistory.getTokenAddress());
        tokenStatsLastTraderService.updateTopCoinsLastTraderByCache(topCoinsLastTrader);

        TokenStatsHistory tokenStatsHistory = new TokenStatsHistory();
        tokenStatsHistory.setId(UniqueIdUtil.nextId());
        tokenStatsHistory.setVolume(topCoinsLastTrader.getVolume());
        tokenStatsHistory.setBoughtCount(topCoinsLastTrader.getBoughtCount());
        tokenStatsHistory.setSoldCount(topCoinsLastTrader.getSoldCount());
        tokenStatsHistory.setTransferInCount(topCoinsLastTrader.getTransferInCount());
        tokenStatsHistory.setTransferOutCount(topCoinsLastTrader.getTransferOutCount());
        tokenStatsHistory.setTokenAddress(topCoinsLastTrader.getTokenAddress());
        tokenStatsHistory.setBlockTime(topCoinsLastTrader.getBlockTime());
        tokenStatsHistory.setBlockHeight(topCoinsLastTrader.getBlockHeight());

        BigDecimal currentPrice = profitHistory.getCurrentPrice();
        AssetInfo assetInfo = assetInfoService.getByAddress(profitHistory.getTokenAddress());
        BigDecimal marketCap = null;
        if (assetInfo != null && assetInfo.getTotalSupply() != null) {
            marketCap = assetInfo.getTotalSupply().multiply(currentPrice);
        }

        TokenStats topCoins = new TokenStats();
        // 不受时间影响
        topCoins.setTokenAddress(profitHistory.getTokenAddress());
        topCoins.setMarketCap(marketCap);
        topCoins.setCurrentPrice(currentPrice);
        topCoins.setBlockHeight(profitHistory.getBlockHeight());
        topCoins.setBlockTime(profitHistory.getBlockTime());
        return Triple.of(topCoinsLastTrader, topCoins, tokenStatsHistory);

/*
        // 通过 tokenSwapPriceHistoryESService 获取某个时间的价格
        BigDecimal price5mAgo = tokenSwapPriceHistoryESService.getPriceAtTime(profitHistory.getTokenAddress(), getTimestampMinutesAgo(5));
        BigDecimal price1hAgo = tokenSwapPriceHistoryESService.getPriceAtTime(profitHistory.getTokenAddress(), getTimestampMinutesAgo(60));
        BigDecimal price24hAgo = tokenSwapPriceHistoryESService.getPriceAtTime(profitHistory.getTokenAddress(), getTimestampMinutesAgo(1440)); // 1440分钟 = 24小时
        BigDecimal price7dAgo = tokenSwapPriceHistoryESService.getPriceAtTime(profitHistory.getTokenAddress(), getTimestampMinutesAgo(10080)); // 10080分钟 = 7天

        TokenStats topCoins5m = createTopCoins(profitHistory, currentPrice, marketCap, price5mAgo, price1hAgo, price24hAgo, price7dAgo, topCoinsLastTrader,5);
        TokenStats topCoins1h = createTopCoins(profitHistory, currentPrice, marketCap, price5mAgo, price1hAgo, price24hAgo, price7dAgo, topCoinsLastTrader,60);
        TokenStats topCoins24h = createTopCoins(profitHistory, currentPrice, marketCap, price5mAgo, price1hAgo, price24hAgo, price7dAgo, topCoinsLastTrader,1440);
        TokenStats topCoins7d = createTopCoins(profitHistory, currentPrice, marketCap, price5mAgo, price1hAgo, price24hAgo, price7dAgo, topCoinsLastTrader,10080);
        TokenStats topCoins30d = createTopCoins(profitHistory, currentPrice, marketCap, price5mAgo, price1hAgo, price24hAgo, price7dAgo, topCoinsLastTrader,2592000);

        ProfitAnalyzerContext.getProfitAnalyzerDTO().getTopCoins5minList().add(topCoins5m);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getTopCoins1hList().add(topCoins1h);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getTopCoins24hList().add(topCoins24h);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getTopCoins7dList().add(topCoins7d);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getTopCoins30dList().add(topCoins30d);*/
    }

    public void calculateChange(Date blockTime, TokenStats topCoins) {
        BigDecimal price5mAgo = tokenSwapPriceHistoryService.getPriceAtTime(topCoins.getTokenAddress(), getTimestampMinutesAgo(blockTime, 5));
        BigDecimal price1hAgo = tokenSwapPriceHistoryService.getPriceAtTime(topCoins.getTokenAddress(), getTimestampMinutesAgo(blockTime, 60));
        BigDecimal price24hAgo = tokenSwapPriceHistoryService.getPriceAtTime(topCoins.getTokenAddress(), getTimestampMinutesAgo(blockTime, 1440)); // 1440分钟 = 24小时
        BigDecimal price7dAgo = tokenSwapPriceHistoryService.getPriceAtTime(topCoins.getTokenAddress(), getTimestampMinutesAgo(blockTime, 10080)); // 10080分钟 = 7天

        topCoins.setPriceChange5m(calculateChangePercentage(price5mAgo, topCoins.getCurrentPrice()));
        topCoins.setPriceChange1h(calculateChangePercentage(price1hAgo, topCoins.getCurrentPrice()));
        topCoins.setPriceChange24h(calculateChangePercentage(price24hAgo, topCoins.getCurrentPrice()));
        topCoins.setPriceChange7d(calculateChangePercentage(price7dAgo, topCoins.getCurrentPrice()));
    }

    public TokenStats createTopCoins(
            String tokenAddress,
            TokenStatsLastTrader topCoinsLastTrader,
            int durationMinutes,
            Date endTime
    ) {
        TokenStats topCoins = new TokenStats();
        // 不受时间影响
        topCoins.setTokenAddress(tokenAddress);

        // 查询时间段范围内的初始数据
//        TokenStatsHistory initialData = topCoinsHistoryESService.getTopCoinsAtTime(tokenAddress, getTimestampMinutesAgo(endTime, durationMinutes));
        TokenStatsHistory initialData = tokenStatsHistoryService.getTopCoinsAtTime(tokenAddress, getTimestampMinutesAgo(endTime, durationMinutes));

        // 计算当前与初始数据的差值
        if (initialData != null) {
            topCoins.setVolume(topCoinsLastTrader.getVolume().subtract(initialData.getVolume()));

            long boughtCount = topCoinsLastTrader.getBoughtCount() - initialData.getBoughtCount();
            long soldCount = topCoinsLastTrader.getSoldCount() - initialData.getSoldCount();
            long swapCount = boughtCount + soldCount;
            topCoins.setBoughtCount(boughtCount);
            topCoins.setSoldCount(soldCount);
            topCoins.setSwapCount(swapCount);

            long transferInCount = topCoinsLastTrader.getTransferInCount() - initialData.getTransferInCount();
            long transferOutCount = topCoinsLastTrader.getTransferOutCount() - initialData.getTransferOutCount();
            long transferCount = transferInCount + transferOutCount;
            topCoins.setTransferInCount(transferInCount);
            topCoins.setTransferOutCount(transferOutCount);
            topCoins.setTransferCount(transferCount);

            topCoins.setTradeCount(swapCount + transferCount);

            topCoins.setBlockHeight(topCoinsLastTrader.getBlockHeight());
            topCoins.setBlockTime(topCoinsLastTrader.getBlockTime());
        } else {
            // 如果没有历史数据，则设置为0或其他默认值
            topCoins.setVolume(topCoinsLastTrader.getVolume());

            long boughtCount = topCoinsLastTrader.getBoughtCount();
            long soldCount = topCoinsLastTrader.getSoldCount();
            long swapCount = boughtCount + soldCount;
            topCoins.setBoughtCount(boughtCount);
            topCoins.setSoldCount(soldCount);
            topCoins.setSwapCount(swapCount);

            long transferInCount = topCoinsLastTrader.getTransferInCount();
            long transferOutCount = topCoinsLastTrader.getTransferOutCount();
            long transferCount = transferInCount + transferOutCount;
            topCoins.setTransferInCount(transferInCount);
            topCoins.setTransferOutCount(transferOutCount);
            topCoins.setTransferCount(transferCount);

            topCoins.setTradeCount(swapCount + transferCount);

            topCoins.setBlockHeight(topCoinsLastTrader.getBlockHeight());
            topCoins.setBlockTime(topCoinsLastTrader.getBlockTime());
        }
        return topCoins;
    }

    public long getTimestampMinutesAgo(Date blockTime, int minutes) {
        ZonedDateTime zonedDateTime = blockTime.toInstant().atZone(ZoneId.of("Asia/Shanghai"));
        return zonedDateTime.toInstant().toEpochMilli() - TimeUnit.MINUTES.toMillis(minutes);
    }

    private BigDecimal calculateChangePercentage(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null || newPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return newPrice.subtract(oldPrice).divide(oldPrice, 4, RoundingMode.HALF_UP);
    }


    public static TraderStatsByToken transformTokenProfitToTraderStats(TokenProfitHistory p) {
        TraderStatsByToken stats = new TraderStatsByToken();
        stats.setPartitionKey(String.valueOf(p.getAccount().charAt(0)).toLowerCase());
        stats.setAccount(p.getAccount());
        stats.setTokenAddress(p.getTokenAddress());
        stats.setAmount(p.getTotalAmount());
        stats.setRoi(p.getRoi());
        stats.setRealizedRoi(p.getRealizedRoi());
        stats.setUnrealizedRoi(p.getUnrealizedRoi());
        stats.setTotalCost(p.getTotalCost());

        BigDecimal winRate = BigDecimalUtils.divideReturnZero(new BigDecimal(p.getSoldCountWin()), new BigDecimal(p.getSoldCountBoughtByUser()));
        stats.setWinRate(winRate);

        stats.setProfit(p.getRealizedProfit().add(p.getUnrealizedProfit()));
        stats.setRealizedProfit(p.getRealizedProfit());
        stats.setUnrealizedProfit(p.getUnrealizedProfit());

        stats.setHoldingAvgPrice(p.getHistoricalHoldingAvgPrice());
        stats.setSoldAvgPrice(p.getHistoricalSoldAvgPrice());

        stats.setBoughtAmount(p.getBoughtAmount());
        stats.setSoldAmount(p.getSoldAmount());

        Long boughtCount = p.getBoughtCount();
        Long soldCount = p.getSoldCount();
        long swapCount = boughtCount + soldCount;
        stats.setBoughtCount(boughtCount);
        stats.setSoldCount(soldCount);
        stats.setSoldCountBoughtByUser(p.getSoldCountBoughtByUser());
        stats.setSwapCount(swapCount);

        Long transferInCount = p.getTransferInCount();
        Long transferOutCount = p.getTransferOutCount();
        long transferCount =  transferInCount + transferOutCount;
        stats.setTransferInCount(transferInCount);
        stats.setTransferOutCount(transferOutCount);
        stats.setTransferCount(transferCount);

        stats.setTransferInAmount(p.getTransferInAmount());
        stats.setTransferOutAmount(p.getTransferOutAmount());

        long tradeCount = swapCount + transferCount;
        stats.setTradeCount(tradeCount);

        stats.setSoldCountWin(p.getSoldCountWin());

        stats.setBlockHeight(p.getBlockHeight());
        stats.setTxHash(p.getTxHash());
        stats.setBlockTime(p.getBlockTime());
        return stats;
    }

    public Pair<BigDecimal, BigDecimal> getPrice(String tokenAddress, SwapInstructionExtraInfo extraInfo, SolanaTransaction transaction,int type) {
        if (tokenAddress.equals(SolConstant.USDC_ADDRESS)){
           BigDecimal balance=  extraInfo.getToken0().equals(tokenAddress)?extraInfo.getToken0Amount():extraInfo.getToken1Amount();
            return Pair.of(BigDecimal.ONE,balance);
        }
        if (extraInfo.getToken0().equals(SolConstant.SOL_ADDRESS) && extraInfo.getToken1().equals(SolConstant.USDC_ADDRESS)) {
            // usdc sol 对
            List<CurrentPrice> currentPriceList = ProfitAnalyzerContext.getProfitAnalyzerDTO().getCurrentPriceList();
            BigDecimal price = extraInfo.getToken1Amount().divide(extraInfo.getToken0Amount(), 18, RoundingMode.HALF_UP).stripTrailingZeros();

            CurrentPrice currentPrice = new CurrentPrice();
            currentPrice.setTokenAddress(SolConstant.SOL_ADDRESS);
            currentPrice.setPrice(price);
            currentPrice.setBlockTime(transaction.getBlockTime());
            currentPrice.setBlockHeight(transaction.getSlot());
            currentPrice.setTxHash(transaction.getHash());
            currentPriceList.add(currentPrice);
            currentPriceService.updateCurrentPriceByCache(currentPrice);
            createSwapPrice(SolConstant.SOL_ADDRESS, transaction, price);

            if (tokenAddress.equals(SolConstant.USDC_ADDRESS)) {
                return Pair.of(BigDecimal.ONE, extraInfo.getToken1Amount());
            }
            if (tokenAddress.equals(SolConstant.SOL_ADDRESS)) {
                return Pair.of(price, extraInfo.getToken0Amount());
            }
            return null;

        }else if (extraInfo.getToken1().equals(SolConstant.SOL_ADDRESS) && extraInfo.getToken0().equals(SolConstant.USDC_ADDRESS)){
            // usdc sol 对
            List<CurrentPrice> currentPriceList = ProfitAnalyzerContext.getProfitAnalyzerDTO().getCurrentPriceList();
            BigDecimal price = extraInfo.getToken0Amount().divide(extraInfo.getToken1Amount(), 18, RoundingMode.HALF_UP);

            CurrentPrice currentPrice = new CurrentPrice();
            currentPrice.setTokenAddress(SolConstant.SOL_ADDRESS);
            currentPrice.setPrice(price);
            currentPrice.setBlockTime(transaction.getBlockTime());
            currentPrice.setBlockHeight(transaction.getSlot());
            currentPrice.setTxHash(transaction.getHash());
            currentPriceList.add(currentPrice);
            currentPriceService.updateCurrentPriceByCache(currentPrice);

            createSwapPrice(SolConstant.SOL_ADDRESS, transaction, price);
            if (tokenAddress.equals(SolConstant.USDC_ADDRESS)) {
                return Pair.of(BigDecimal.ONE, extraInfo.getToken0Amount());
            }
            if (tokenAddress.equals(SolConstant.SOL_ADDRESS)) {
                return Pair.of(price, extraInfo.getToken1Amount());
            }
            return null;
        }
        return getSolPrice(tokenAddress, extraInfo, transaction,type);

        // 返回计算出的价格和 token 的余额
//        return Pair.of(price, tokenBalance);
    }

    private static void createSwapPrice(String tokenAddress, SolanaTransaction transaction, BigDecimal price) {
        List<TokenSwapPriceHistory> tokenSwapPriceHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenSwapPriceHistoryList();
        long tokenSwapPriceHistoryId = transaction.getTokenSwapPriceHistoryId();
        TokenSwapPriceHistory newPriceHistory = new TokenSwapPriceHistory();
        newPriceHistory.setId(transaction.getId() * 1000 + tokenSwapPriceHistoryId);
        tokenSwapPriceHistoryId++;
        transaction.setTokenSwapPriceHistoryId(tokenSwapPriceHistoryId);
        newPriceHistory.setTokenAddress(tokenAddress);
        newPriceHistory.setPrice(price);
        newPriceHistory.setBlockTime(transaction.getBlockTime());
        newPriceHistory.setBlockHeight(transaction.getSlot());
        newPriceHistory.setTxId(transaction.getId());
        newPriceHistory.setTxHash(transaction.getHash());
        tokenSwapPriceHistoryList.add(newPriceHistory);
    }

    public BigDecimal getSolUSDCPrice() {
        CurrentPrice solUSDC = currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS);
        if (solUSDC != null) {
            return solUSDC.getPrice();
        }
        return null;
    }

    public Pair<BigDecimal, BigDecimal> getSolPrice(String tokenAddress, SwapInstructionExtraInfo extraInfo, SolanaTransaction transaction,int type) {
        BigDecimal solUSDCPrice = getSolUSDCPrice();
        if (solUSDCPrice == null) {
            return null;
        }
        // 从 extraInfo 中获取 token0 和 token1 的地址和数量
        String token0 = extraInfo.getToken0();
        String token1 = extraInfo.getToken1();
        BigDecimal token0Amount = extraInfo.getToken0Amount();
        BigDecimal token1Amount = extraInfo.getToken1Amount();

        BigDecimal solBalance;
        BigDecimal tokenBalance;

        // 如果 token0 和 token1 都不匹配 tokenAddress，返回 null
        if (!tokenAddress.equals(token0) && !tokenAddress.equals(token1)) {
            log.warn("not found tokenAddress: {} in swap price history list, hash: {}", tokenAddress, transaction.getHash());
            return null; // 如果没有找到历史价格，返回 null
        }

        // 如果 tokenAddress 是 SOL 地址，从 extraInfo 中获取相应的余额
        if (SolConstant.SOL_ADDRESS.equals(tokenAddress)) {
            if (SolConstant.SOL_ADDRESS.equals(token0)) {
                solBalance = token0Amount;
            } else {
                solBalance = token1Amount;
            }
            // SOL 地址不需要存储价格，直接返回 SOL 的价格（1）和余额
            return Pair.of(solUSDCPrice, solBalance);
        }

        // 判断 token0 或 token1 是否是 SOL 地址，并根据匹配的地址计算价格和余额
        if (SolConstant.SOL_ADDRESS.equals(token0)) {
            solBalance = token0Amount; // SOL 的余额在 token0Amount 中
            tokenBalance = token1Amount; // token 的余额在 token1Amount 中
        } else if (SolConstant.SOL_ADDRESS.equals(token1)) {
            solBalance = token1Amount; // SOL 的余额在 token1Amount 中
            tokenBalance = token0Amount; // token 的余额在 token0Amount 中
        } else {
            // 从 tokenSwapPriceHistory 中查找小于当前 transaction.getId() 的最新价格记录
            // A B.  A
            // wsol ->a.     toekn0 toke1. tokenadres wsol
            //. a->b.  a
            if (type == 1){
                return getPriceByOther(tokenAddress, transaction, token0, token1, token1Amount, token0Amount);

            }else {
                return getPriceBySelf(tokenAddress, transaction, token0, token1, token1Amount, token0Amount);
            }

        }

        // 计算当前 token 的 SOL 单位价格: solBalance / tokenBalance
        BigDecimal price = solBalance.divide(tokenBalance, 18, RoundingMode.HALF_UP).multiply(solUSDCPrice).stripTrailingZeros();

        long tokenSwapPriceHistoryId = transaction.getTokenSwapPriceHistoryId();

        // 将当前交易的价格存入到 tokenSwapPriceHistories 中（如果不是 SOL）
        List<TokenSwapPriceHistory> tokenSwapPriceHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenSwapPriceHistoryList();
        List<CurrentPrice> currentPriceList = ProfitAnalyzerContext.getProfitAnalyzerDTO().getCurrentPriceList();
        TokenSwapPriceHistory newPriceHistory = new TokenSwapPriceHistory();
        newPriceHistory.setId(transaction.getId() * 1000 + tokenSwapPriceHistoryId);
        tokenSwapPriceHistoryId++;
        transaction.setTokenSwapPriceHistoryId(tokenSwapPriceHistoryId);
        newPriceHistory.setTokenAddress(tokenAddress);
        newPriceHistory.setPrice(price);
        newPriceHistory.setBlockTime(transaction.getBlockTime());
        newPriceHistory.setBlockHeight(transaction.getSlot());
        newPriceHistory.setTxId(transaction.getId());
        newPriceHistory.setTxHash(transaction.getHash());
        tokenSwapPriceHistoryList.add(newPriceHistory);

        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setTokenAddress(tokenAddress);
        currentPrice.setPrice(price);
        currentPrice.setBlockTime(transaction.getBlockTime());
        currentPrice.setBlockHeight(transaction.getSlot());
        currentPrice.setTxHash(transaction.getHash());
        currentPriceList.add(currentPrice);
        currentPriceService.updateCurrentPriceByCache(currentPrice);

        // 返回计算出的价格和 token 的余额
        return Pair.of(price, tokenBalance);
    }

    @Nullable
    private static Pair<BigDecimal, BigDecimal> getPriceByCurrentToken(String tokenAddress,  CurrentPrice currentPrice, String token0, BigDecimal token0Amount, BigDecimal token1Amount) {
        BigDecimal tokenBalance;
        if (token0.equals(tokenAddress)) {
            tokenBalance = token0Amount;
        } else {
            tokenBalance = token1Amount;
        }
        return Pair.of(currentPrice.getPrice(), tokenBalance);
    }

    @Nullable
    private Pair<BigDecimal, BigDecimal> getPriceByOther(String tokenAddress, SolanaTransaction transaction, String token0, String token1, BigDecimal token1Amount, BigDecimal token0Amount) {
        BigDecimal tokenBalance;
        CurrentPrice currentPrice;
        String otherToken = token0.equals(tokenAddress) ? token1 : token0;
        CurrentPrice  currentPriceOther = currentPriceService.getCurrentPrice(otherToken);
        if (currentPriceOther == null || currentPriceOther.getPrice()==null){
            log.warn("not found first tokenAddress: {} in swap price history list, hash: {}", tokenAddress, transaction.getHash());
            return null;
        }
        BigDecimal otherTokenBalance = token0.equals(tokenAddress) ? token1Amount : token0Amount;
        tokenBalance = token0.equals(tokenAddress) ? token0Amount : token1Amount;
        BigDecimal     price =otherTokenBalance.multiply(currentPriceOther.getPrice()).divide(tokenBalance,18, RoundingMode.HALF_UP).stripTrailingZeros();
        long tokenSwapPriceHistoryId = transaction.getTokenSwapPriceHistoryId();

        List<TokenSwapPriceHistory> tokenSwapPriceHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenSwapPriceHistoryList();
        List<CurrentPrice> currentPriceList = ProfitAnalyzerContext.getProfitAnalyzerDTO().getCurrentPriceList();
        TokenSwapPriceHistory newPriceHistory = new TokenSwapPriceHistory();
        newPriceHistory.setId(transaction.getId() * 1000 + tokenSwapPriceHistoryId);
        tokenSwapPriceHistoryId++;
        transaction.setTokenSwapPriceHistoryId(tokenSwapPriceHistoryId);
        newPriceHistory.setTokenAddress(tokenAddress);
        newPriceHistory.setPrice(price);
        newPriceHistory.setBlockTime(transaction.getBlockTime());
        newPriceHistory.setBlockHeight(transaction.getSlot());
        newPriceHistory.setTxId(transaction.getId());
        newPriceHistory.setTxHash(transaction.getHash());
        tokenSwapPriceHistoryList.add(newPriceHistory);

        currentPrice = new CurrentPrice();
        currentPrice.setTokenAddress(tokenAddress);
        currentPrice.setPrice(price);
        currentPrice.setBlockTime(transaction.getBlockTime());
        currentPrice.setBlockHeight(transaction.getSlot());
        currentPrice.setTxHash(transaction.getHash());
        currentPriceList.add(currentPrice);
        currentPriceService.updateCurrentPriceByCache(currentPrice);
        return Pair.of(price, tokenBalance);
    }
    private Pair<BigDecimal, BigDecimal> getPriceBySelf(String tokenAddress, SolanaTransaction transaction, String token0, String token1, BigDecimal token1Amount, BigDecimal token0Amount) {
        BigDecimal tokenBalance;
        CurrentPrice  currentPriceOther = currentPriceService.getCurrentPrice(tokenAddress);
        if (currentPriceOther == null || currentPriceOther.getPrice()==null){
            log.warn("not found first tokenAddress: {} in swap price history list, hash: {}", tokenAddress, transaction.getHash());
            return null;
        }
        tokenBalance = token0.equals(tokenAddress) ? token0Amount : token1Amount;
        return Pair.of(currentPriceOther.getPrice(), tokenBalance);
    }


    public void handleErc20(List<Erc20HandlerDTO> erc20HandlerDTOS) {
        for (Erc20HandlerDTO erc20HandlerDTO : erc20HandlerDTOS) {
            if (erc20HandlerDTO.getType() == 1) {
                buyErc20Ext(erc20HandlerDTO.getTransaction(), erc20HandlerDTO.getUserAddress(), erc20HandlerDTO.getTokenAddress(), erc20HandlerDTO.getExtraInfo());
            } else if (erc20HandlerDTO.getType() == 2) {
                sellErc20Ext(erc20HandlerDTO.getTransaction(), erc20HandlerDTO.getUserAddress(), erc20HandlerDTO.getTokenAddress(), erc20HandlerDTO.getExtraInfo());
            } else if (erc20HandlerDTO.getType() == 3) {
                transferInErc20Ext(erc20HandlerDTO.getTransaction(), erc20HandlerDTO.getUserAddress(), erc20HandlerDTO.getTokenAddress(), erc20HandlerDTO.getTransferAmountDecimal(), erc20HandlerDTO.getExtraInfo());
            } else if (erc20HandlerDTO.getType() == 4) {
                transferOutErc20Ext(erc20HandlerDTO.getTransaction(), erc20HandlerDTO.getUserAddress(), erc20HandlerDTO.getTokenAddress(), erc20HandlerDTO.getTransferAmountDecimal(), erc20HandlerDTO.getExtraInfo());
            }
        }
    }

    public void transferOutErc20(SolanaTransaction transaction, String fromAccount, String tokenAddress, BigDecimal transferAmountDecimal, SwapInstructionExtraInfo extraInfo) {
        Erc20HandlerDTO handlerDTO = new Erc20HandlerDTO(transaction, fromAccount, tokenAddress, extraInfo, transferAmountDecimal, 4);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getErc20HandlerDTOS().add(handlerDTO);
    }

    public void transferOutErc20Ext(SolanaTransaction transaction, String fromAccount, String tokenAddress, BigDecimal transferAmountDecimal, SwapInstructionExtraInfo extraInfo) {
        try {
            if (StringUtils.isBlank(fromAccount)) {
                return;
            }
            if (tokenAddress.equals(SolConstant.USDC_ADDRESS) || tokenAddress.equals(SolConstant.SOL_ADDRESS)) {
                return;
            }
            long start = System.currentTimeMillis();

            CurrentPrice currentPriceEntity = currentPriceService.getCurrentPrice(tokenAddress);
            BigDecimal currentPrice;
            if (currentPriceEntity == null || currentPriceEntity.getPrice() == null) {
                log.warn("not found tokenAddress: {} in swap price history list, hash: {}", tokenAddress, transaction.getHash());
                currentPrice = BigDecimal.ZERO;
            } else {
                currentPrice = currentPriceEntity.getPrice();
            }

            if (System.currentTimeMillis() - start > 30) {
                log.info("getPrice:{}", System.currentTimeMillis() - start);
            }

            BigDecimal currentTransferAmount = transferAmountDecimal;
            if (currentTransferAmount.compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
            start = System.currentTimeMillis();
            TokenProfitHistoryLast oldProfitHistory = tokenProfitHistoryLastService.getLastByAddressFromCache(fromAccount, tokenAddress);
            if (System.currentTimeMillis() - start > 30) {
                log.info("getLastByAddress: transferInErc20{}", System.currentTimeMillis() - start);
            }

            long tokenProfitHistoryId = transaction.getTokenProfitHistoryId();
            TokenProfitHistory profitHistory = new TokenProfitHistory();
            profitHistory.setId(transaction.getId() * 1000 + tokenProfitHistoryId);
            tokenProfitHistoryId++;
            transaction.setTokenProfitHistoryId(tokenProfitHistoryId);
            profitHistory.setTokenAddress(tokenAddress);
            profitHistory.setAmount(currentTransferAmount);

            profitHistory.setAccount(fromAccount);
            profitHistory.setType(4);
            profitHistory.setCurrentPrice(currentPrice);

            BigDecimal totalAmount = extraInfo.getPostAmount(); // 当前交易结束时拥有的token数量, todo
            profitHistory.setTotalAmount(totalAmount);

            // 卖出均价 = 历史卖出均价*历史卖出数量+当前卖出数量*当前卖出价格/总数量    买入不变
            profitHistory.setSoldAmount(oldProfitHistory.getSoldAmount());
            profitHistory.setHistoricalSoldAvgPrice(oldProfitHistory.getHistoricalSoldAvgPrice());

            profitHistory.setBoughtAmount(oldProfitHistory.getBoughtAmount());
            profitHistory.setHistoricalHoldingAvgPrice(oldProfitHistory.getHistoricalHoldingAvgPrice());

            BigDecimal holdingAvgPrice = oldProfitHistory.getHoldingAvgPrice() == null ? BigDecimal.ZERO : oldProfitHistory.getHoldingAvgPrice();
            BigDecimal totalBoughtAmountHasBeenLeft = oldProfitHistory.getTotalBoughtAmountHasBeenLeft() == null ? BigDecimal.ZERO : oldProfitHistory.getTotalBoughtAmountHasBeenLeft();
            BigDecimal unrealizedProfit = oldProfitHistory.getUnrealizedProfit() == null ? BigDecimal.ZERO : oldProfitHistory.getUnrealizedProfit();
            if (oldProfitHistory.getId() != null) {
                BigDecimal unKnowAmount = oldProfitHistory.getTotalAmount().subtract(oldProfitHistory.getTotalBoughtAmountHasBeenLeft());
                if (currentTransferAmount.compareTo(unKnowAmount) >= 0) {
                    BigDecimal transferNotBoughtAmount = currentTransferAmount.subtract(unKnowAmount);
                    if (oldProfitHistory.getTotalBoughtAmountHasBeenLeft().compareTo(transferNotBoughtAmount) > 0) {
                        totalBoughtAmountHasBeenLeft = oldProfitHistory.getTotalBoughtAmountHasBeenLeft().subtract(transferNotBoughtAmount);
                        unrealizedProfit = currentPrice.subtract(holdingAvgPrice).multiply(totalBoughtAmountHasBeenLeft);

                        if (totalBoughtAmountHasBeenLeft.compareTo(BigDecimal.ZERO) == 0) {
                            holdingAvgPrice = BigDecimal.ZERO;
                        }
                    } else {
                        totalBoughtAmountHasBeenLeft = BigDecimal.ZERO;
                        holdingAvgPrice = BigDecimal.ZERO;
                        unrealizedProfit = BigDecimal.ZERO;
                    }
                }
            }
            profitHistory.setHoldingAvgPrice(holdingAvgPrice);
            profitHistory.setUnrealizedProfit(unrealizedProfit);
            profitHistory.setTotalBoughtAmountHasBeenLeft(totalBoughtAmountHasBeenLeft);

            BigDecimal totalCost = oldProfitHistory.getTotalCost();
            profitHistory.setTotalCost(totalCost);
            BigDecimal realizedProfit = oldProfitHistory.getRealizedProfit();
            profitHistory.setRealizedProfit(realizedProfit);

            BigDecimal roi = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : realizedProfit.add(unrealizedProfit).divide(totalCost, 4, RoundingMode.HALF_UP);
            profitHistory.setRoi(roi);
            BigDecimal unrealizedRoi = totalCost.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : unrealizedProfit.divide(totalCost, 4, RoundingMode.HALF_UP);
            profitHistory.setUnrealizedRoi(unrealizedRoi);
            profitHistory.setRealizedRoi(oldProfitHistory.getRealizedRoi());


            profitHistory.setIsWin(0);
            profitHistory.setSoldCount(oldProfitHistory.getSoldCount());
            profitHistory.setSoldCountWin(oldProfitHistory.getSoldCountWin());
            profitHistory.setSoldCountBoughtByUser(oldProfitHistory.getSoldCountBoughtByUser() == null ? 0 : oldProfitHistory.getSoldCountBoughtByUser());

            profitHistory.setBoughtCount(oldProfitHistory.getBoughtCount());

            profitHistory.setTransferInCount(oldProfitHistory.getTransferInCount());
            profitHistory.setTransferOutCount(oldProfitHistory.getTransferOutCount() == null ? 0 : oldProfitHistory.getTransferOutCount() + 1);

            profitHistory.setTransferInAmount(oldProfitHistory.getTransferInAmount());
            profitHistory.setTransferOutAmount(oldProfitHistory.getTransferOutAmount() == null ? BigDecimal.ZERO : oldProfitHistory.getTransferOutAmount().add(currentTransferAmount));

            profitHistory.setBlockTime(transaction.getBlockTime());
            profitHistory.setBlockHeight(transaction.getSlot());
            profitHistory.setTxHash(transaction.getHash());

            profitHistory.formatAmountFields();

            ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenProfitHistoryList().add(profitHistory);
            TokenProfitHistoryLast tokenProfitHistoryLast = new TokenProfitHistoryLast(profitHistory);
            ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenProfitHistoryLastMap().put(tokenProfitHistoryLast.getAccount() + "_" + tokenProfitHistoryLast.getTokenAddress(), tokenProfitHistoryLast);
            tokenProfitHistoryLastService.updateProfitHistoryByCache(profitHistory);
            // tokenProfitHistoryESService.updateProfitHistoryByCache(profitHistory);

        } catch (Exception e) {
            log.error("transferOutErc20 error", e);
            System.exit(0);
        }
    }

    public void transferInErc20(SolanaTransaction transaction, String toAccount, String tokenAddress, BigDecimal transferAmountDecimal,  SwapInstructionExtraInfo extraInfo) {
        Erc20HandlerDTO handlerDTO = new Erc20HandlerDTO(transaction, toAccount, tokenAddress, extraInfo, transferAmountDecimal, 3);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getErc20HandlerDTOS().add(handlerDTO);
    }


    public void transferInErc20Ext(SolanaTransaction transaction, String toAccount, String tokenAddress, BigDecimal transferAmountDecimal, SwapInstructionExtraInfo extraInfo) {
        try {
            if (StringUtils.isBlank(toAccount)) {
                return;
            }
            if (tokenAddress.equals(SolConstant.USDC_ADDRESS) || tokenAddress.equals(SolConstant.SOL_ADDRESS)) {
                return;
            }
            long start = System.currentTimeMillis();

            CurrentPrice currentPriceEntity = currentPriceService.getCurrentPrice(tokenAddress);
            BigDecimal currentPrice;
            if (currentPriceEntity == null || currentPriceEntity.getPrice() == null) {
                log.warn("not found tokenAddress: {} in swap price history list, hash: {}", tokenAddress, transaction.getHash());
                currentPrice = BigDecimal.ZERO;
            } else {
                currentPrice = currentPriceEntity.getPrice();
            }

            if (System.currentTimeMillis() - start > 30) {
                log.info("getPrice:{}", System.currentTimeMillis() - start);
            }

            if (transferAmountDecimal.compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
            start = System.currentTimeMillis();
            TokenProfitHistoryLast oldProfitHistory = tokenProfitHistoryLastService.getLastByAddressFromCache(toAccount, tokenAddress);
            if (System.currentTimeMillis() - start > 30) {
                log.info("getLastByAddress: transferInErc20{}", System.currentTimeMillis() - start);
            }

            long tokenProfitHistoryId = transaction.getTokenProfitHistoryId();
            TokenProfitHistory profitHistory = new TokenProfitHistory();
            profitHistory.setId(transaction.getId() * 1000 + tokenProfitHistoryId);
            tokenProfitHistoryId++;
            transaction.setTokenProfitHistoryId(tokenProfitHistoryId);
            profitHistory.setTokenAddress(tokenAddress);
            profitHistory.setAmount(transferAmountDecimal);
            profitHistory.setTotalBoughtAmountHasBeenLeft(oldProfitHistory.getTotalBoughtAmountHasBeenLeft() == null ? BigDecimal.ZERO : oldProfitHistory.getTotalBoughtAmountHasBeenLeft());
            profitHistory.setAccount(toAccount);
            profitHistory.setType(3);
            profitHistory.setCurrentPrice(currentPrice);

            BigDecimal totalAmount = extraInfo.getPostAmount();  // 当前交易结束时拥有的token数量, todo
            profitHistory.setTotalAmount(totalAmount);

            // 卖出均价 = 历史卖出均价*历史卖出数量+当前卖出数量*当前卖出价格/总数量    买入不变
            profitHistory.setSoldAmount(oldProfitHistory.getSoldAmount());
            profitHistory.setHistoricalSoldAvgPrice(oldProfitHistory.getHistoricalSoldAvgPrice());

            profitHistory.setBoughtAmount(oldProfitHistory.getBoughtAmount());
            profitHistory.setHistoricalHoldingAvgPrice(oldProfitHistory.getHistoricalHoldingAvgPrice());
            BigDecimal circleHoldingAvgPrice = oldProfitHistory.getHoldingAvgPrice() == null ? BigDecimal.ZERO : oldProfitHistory.getHoldingAvgPrice();
            profitHistory.setHoldingAvgPrice(circleHoldingAvgPrice);

            profitHistory.setTotalCost(oldProfitHistory.getTotalCost());
            profitHistory.setRealizedProfit(oldProfitHistory.getRealizedProfit());
            profitHistory.setUnrealizedProfit(oldProfitHistory.getUnrealizedProfit());
            profitHistory.setRoi(oldProfitHistory.getRoi());
            profitHistory.setUnrealizedRoi(oldProfitHistory.getUnrealizedRoi());
            profitHistory.setRealizedRoi(oldProfitHistory.getRealizedRoi());

            profitHistory.setIsWin(0);
            profitHistory.setSoldCount(oldProfitHistory.getSoldCount());
            profitHistory.setSoldCountWin(oldProfitHistory.getSoldCountWin());
            profitHistory.setSoldCountBoughtByUser(oldProfitHistory.getSoldCountBoughtByUser());

            profitHistory.setBoughtCount(oldProfitHistory.getBoughtCount());

            profitHistory.setTransferInCount(oldProfitHistory.getTransferInCount()==null?0:oldProfitHistory.getTransferInCount() + 1);
            profitHistory.setTransferOutCount(oldProfitHistory.getTransferOutCount());

            profitHistory.setTransferInAmount(oldProfitHistory.getTransferInAmount()==null?BigDecimal.ZERO:oldProfitHistory.getTransferInAmount().add(transferAmountDecimal));
            profitHistory.setTransferOutAmount(oldProfitHistory.getTransferOutAmount());

            profitHistory.setBlockTime(transaction.getBlockTime());
            profitHistory.setBlockHeight(transaction.getSlot());
            profitHistory.setTxHash(transaction.getHash());

            profitHistory.formatAmountFields();

            ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenProfitHistoryList().add(profitHistory);
            TokenProfitHistoryLast tokenProfitHistoryLast = new TokenProfitHistoryLast(profitHistory);
            ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenProfitHistoryLastMap().put(tokenProfitHistoryLast.getAccount() + "_" + tokenProfitHistoryLast.getTokenAddress(), tokenProfitHistoryLast);
            tokenProfitHistoryLastService.updateProfitHistoryByCache(profitHistory);
            // tokenProfitHistoryESService.updateProfitHistoryByCache(profitHistory);

        } catch (Exception e) {
            log.error("transferInErc20 error", e);
            System.exit(0);
        }
    }
}
