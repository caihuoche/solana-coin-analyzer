package com.creda.coin.price.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.creda.coin.price.entity.TraderAccount;
import com.creda.coin.price.service.data.doris.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.creda.coin.price.ProfitAnalyzerContext;
import com.creda.coin.price.dto.Erc20HandlerDTO;
import com.creda.coin.price.dto.ProfitAnalyzerDTO;
import com.creda.coin.price.dto.TransactionCondition;
import com.creda.coin.price.entity.AssetInfo;
import com.creda.coin.price.entity.ProfitCalRecord;
import com.creda.coin.price.entity.RaydiumSwapPool;
import com.creda.coin.price.entity.doris.CurrentPrice;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.entity.doris.TokenProfitHistoryLast;
import com.creda.coin.price.entity.doris.TokenSwapPriceHistory;
import com.creda.coin.price.entity.doris.traders.TraderStatsByToken;
import com.creda.coin.price.entity.es.Instruction;
import com.creda.coin.price.entity.es.SolanaTransaction;
import com.creda.coin.price.mq.KafkaProducerService;
import com.creda.coin.price.service.data.jdbc.IAssetInfoService;
import com.creda.coin.price.service.data.jdbc.IProfitCalRecordService;
import com.creda.coin.price.service.data.jdbc.IVaultTokenService;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gavin
 * @date 2024/07/24
 *
 */
@Service
@Slf4j
public class ProfitAnalyzerService extends AbstractProfitAnalyzerService {

    @Resource
    private IAssetInfoService assetInfoService;
    @Resource
    private RaydiumSwapService raydiumSwapService;
    @Resource
    private TransferService transferService;
    @Resource
    private ITokenProfitHistoryService tokenProfitHistoryService;
    @Resource
    private ITokenSwapPriceHistoryService tokenSwapPriceHistoryService;
    @Resource
    private ICurrentPriceService currentPriceService;
    @Resource
    private AnalyzerDataHandler analyzerDataHandler;
    @Resource
    private ITraderStatsByTokenService traderStatsByTokenService;
    @Resource
    private ITokenProfitHistoryLastService tokenProfitHistoryLastService;
    @Resource
    private ISolanaTransactionsService solanaTransactionsService;
    @Resource
    private KafkaProducerService kafkaProducerService;
    @Resource
    private IVaultTokenService vaultTokenService;
    @Resource
    private ITraderAccountService traderAccountService;

    private boolean enable = true;

    public ProfitAnalyzerService(IProfitCalRecordService profitCalRecordService) {
        super(profitCalRecordService);
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    protected void processTransactions(ProfitCalRecord profitCalRecord) {
        boolean hasMoreResults = true;
        TransactionCondition condition = new TransactionCondition();
        Long lastId = profitCalRecord.getOffset();
        Date blockTime = profitCalRecord.getBlockTime();
        while (hasMoreResults && enable) {
            condition.setLastId(lastId);
            condition.setBlockTime(blockTime);
            long start = System.currentTimeMillis();
            List<SolanaTransaction> results = solanaTransactionsService.searchTransactions(condition);

            if (!results.isEmpty()) {
                long startTime = System.currentTimeMillis();
                lastId = results.get(results.size() - 1).getId();
                blockTime = results.get(results.size() - 1).getBlockTime();
                profitCal(results, lastId);
                long endTime = System.currentTimeMillis();

                long elapsedTime = endTime - startTime;
                long duration = endTime - start;
                log.info("processTransactions end executed in {} ms, profitCal :{} lastId: {}, results size: {}", duration, elapsedTime, lastId, results.size());
            } else {
                hasMoreResults = false;
            }
        }

        log.info("Completed processing for");
    }

    public void profitCal(List<SolanaTransaction> results, Long lastId) {
        ProfitAnalyzerDTO profitAnalyzerDTO = new ProfitAnalyzerDTO();
        long startTime1 = System.currentTimeMillis();
        try {
            ProfitAnalyzerContext.setProfitAnalyzerDTO(profitAnalyzerDTO);
            AtomicInteger atomicInteger = new AtomicInteger(0);
            long startTime2 = System.currentTimeMillis();
            for (SolanaTransaction transaction : results) {
                List<Instruction> instructions = transaction.getInstructions();
                if (CollectionUtil.isEmpty(instructions)) {
                    return;
                }

                long innerStart = System.currentTimeMillis();
                raydiumSwapService.processRaydiumSwap(transaction, atomicInteger);
                if (System.currentTimeMillis() - innerStart > 30) {
                    log.info("processRaydiumSwap Pre time:{}", System.currentTimeMillis() - innerStart);
                }

            }
            vaultTokenService.findByVaultAddresses(profitAnalyzerDTO.getVaultTokens());
            profitAnalyzerDTO.setVaultTokensPreFinish(true);
            log.info("for处理时间 Pre time:{}", System.currentTimeMillis() - startTime2);
            long forHandleTime = System.currentTimeMillis();

            for (SolanaTransaction transaction : results) {
                List<Instruction> instructions = transaction.getInstructions();
                if (CollectionUtil.isEmpty(instructions)) {
                    log.warn("Transaction {} has no instructions", transaction.getHash());
                    return;
                }

                long innerStart = System.currentTimeMillis();
                raydiumSwapService.processRaydiumSwap(transaction, atomicInteger);
                if (System.currentTimeMillis() - innerStart > 30) {
                    log.info("processRaydiumSwap time:{}", System.currentTimeMillis() - innerStart);
                }

                innerStart = System.currentTimeMillis();
                transferService.processTransfer(transaction);
                if (System.currentTimeMillis() - innerStart > 30) {
                    log.info("processTransfer time:{}", System.currentTimeMillis() - innerStart);
                }
            }
            log.info("for处理时间 time:{}", System.currentTimeMillis() - forHandleTime);
            List<Erc20HandlerDTO> erc20HandlerDTOS = profitAnalyzerDTO.getErc20HandlerDTOS();
            if (CollectionUtils.isNotEmpty(erc20HandlerDTOS)) {
                long startTime3 = System.currentTimeMillis();
                handleErc20(erc20HandlerDTOS);
                log.info("handleErc20 time:{}", System.currentTimeMillis() - startTime3);
                saveOrUpdateData(profitAnalyzerDTO);

            }
        } finally {
            ProfitAnalyzerContext.remove();
        }

        log.info("profitCal time:{}", System.currentTimeMillis() - startTime1);
        updateCalBlockRecord(lastId, results.get(results.size() - 1).getBlockTime());
    }

    private void handleErc20(List<Erc20HandlerDTO> erc20HandlerDTOS) {
        // 把last批量查询放入缓存
        analyzerDataHandler.preHandleErc20(erc20HandlerDTOS);
        analyzerDataHandler.handleErc20(erc20HandlerDTOS);

    }

    public List<TokenProfitHistory> getLatestTokenProfits(List<TokenProfitHistory> tokenProfitHistories) {
        Map<String, TokenProfitHistory> latestRecordMap = new HashMap<>();

        for (TokenProfitHistory tokenProfit: tokenProfitHistories) {
            String key = tokenProfit.getAccount() + "_" + tokenProfit.getTokenAddress();
            if (latestRecordMap.containsKey(key)) {
                if (tokenProfit.getId().compareTo(latestRecordMap.get(key).getId()) >= 0) {
                    latestRecordMap.put(key, tokenProfit);
                }
            } else {
                latestRecordMap.put(key, tokenProfit);
            }
        }

        return new ArrayList<>(latestRecordMap.values());
    }

    private void saveOrUpdateData(ProfitAnalyzerDTO profitAnalyzerDTO) {
        List<TokenProfitHistory> tokenProfitHistoryList = profitAnalyzerDTO.getTokenProfitHistoryList();
        if (CollectionUtils.isEmpty(tokenProfitHistoryList)) {
            return;
        }
        long start = System.currentTimeMillis();
        List<TokenSwapPriceHistory> tokenSwapPriceHistoryList = profitAnalyzerDTO.getTokenSwapPriceHistoryList();
        List<CurrentPrice> currentPriceList = profitAnalyzerDTO.getCurrentPriceList();
        Map<String, TokenProfitHistoryLast> tokenProfitHistoryLastMap = profitAnalyzerDTO.getTokenProfitHistoryLastMap();
        List<AssetInfo> assetInfos = profitAnalyzerDTO.getAssetInfos();

        tokenProfitHistoryList.sort(Comparator.comparing(TokenProfitHistory::getId));
        List<TokenProfitHistory> latestTokenProfits = getLatestTokenProfits(tokenProfitHistoryList);
        List<TraderStatsByToken> latestTraderStats = latestTokenProfits.stream()
            .map(AnalyzerDataHandler::transformTokenProfitToTraderStats)
            .collect(Collectors.toList());

        asyncSaveOperations(
            tokenProfitHistoryList,
            tokenSwapPriceHistoryList,
            currentPriceList,
            tokenProfitHistoryLastMap.values(),
            profitAnalyzerDTO.getRaydiumSwapPools(),
            assetInfos,
            latestTraderStats
        );

         // 更新account trade
        Map<String,TraderAccount> traderAccounts = new HashMap<>();
        for (TokenProfitHistory tokenProfitHistory : tokenProfitHistoryList) {
            TraderAccount traderAccount = new TraderAccount();
            traderAccount.setAccount(tokenProfitHistory.getAccount());
            traderAccount.setLastTraderTime(tokenProfitHistory.getBlockTime());
            traderAccounts.put(tokenProfitHistory.getAccount(),traderAccount);
        }
        traderAccountService.updateBatchStreamLoad(traderAccounts.values());
//        kafkaProducerService.sendMessage(tokenProfitHistoryList);

        /*   for (TokenProfitHistory tokenProfitHistory : tokenProfitHistoryList) {
            kafkaProducer.sendMessage(tokenProfitHistory);
        }
        topCoinsAsyncService.calHolders(tokenProfitHistoryList);
        long currentTimeMillis = System.currentTimeMillis();
        topCoinsAsyncService.asyncCalTopCoinsPriceChangeCMapAndTokenStatsLastTrader(tokenProfitHistoryList);
        topTradersAsyncService.asyncHandlerTopCoins(tokenProfitHistoryList);
        if (System.currentTimeMillis() - currentTimeMillis > 30) {
            log.info("async time:{}", System.currentTimeMillis() - currentTimeMillis);
        }*/
        log.info("saveOrUpdateEs time :{}", System.currentTimeMillis() - start);
    }

    public void asyncSaveOperations(
        List<TokenProfitHistory> tokenProfitHistoryList,
        List<TokenSwapPriceHistory> tokenSwapPriceHistoryList,
        List<CurrentPrice> currentPriceList,
        Collection<TokenProfitHistoryLast> tokenProfitHistoryLastCollection,
        List<RaydiumSwapPool> raydiumSwapPools,
        List<AssetInfo> assetInfos,
        List<TraderStatsByToken> traderStats
    ) {
        long l = System.currentTimeMillis();
        long currentTimeMillis = System.currentTimeMillis();
        tokenProfitHistoryService.saveBatchStreamLoad(tokenProfitHistoryList);
        if (System.currentTimeMillis() - currentTimeMillis > 30) {
            log.info("saveBatchStreamLoad time:{}", System.currentTimeMillis() - currentTimeMillis);
        }
        currentTimeMillis = System.currentTimeMillis();
        tokenSwapPriceHistoryService.saveBatchStreamLoad(tokenSwapPriceHistoryList);
        if (System.currentTimeMillis() - currentTimeMillis > 30) {
            log.info("saveBatchStreamLoad time:{}", System.currentTimeMillis() - currentTimeMillis);
        }

        currentTimeMillis = System.currentTimeMillis();
        currentPriceService.saveBatchStreamLoad(currentPriceList);
        if (System.currentTimeMillis() - currentTimeMillis > 30) {
            log.info("saveBatchStreamLoad time:{}", System.currentTimeMillis() - currentTimeMillis);
        }

        currentTimeMillis = System.currentTimeMillis();
        traderStatsByTokenService.distintSaveOrUpdateBatch(traderStats);
        if (System.currentTimeMillis() - currentTimeMillis > 30) {
            log.info("saveBatchStreamLoad time:{}", System.currentTimeMillis() - currentTimeMillis);
        }
        currentTimeMillis = System.currentTimeMillis();
        tokenProfitHistoryLastService.saveBatchStreamLoad(tokenProfitHistoryLastCollection);
        if (System.currentTimeMillis() - currentTimeMillis > 30) {
            log.info("saveBatchStreamLoad time:{}", System.currentTimeMillis() - currentTimeMillis);
        }

        currentTimeMillis = System.currentTimeMillis();
        raydiumSwapService.batchSaveOrUpdate(raydiumSwapPools);
        if (System.currentTimeMillis() - currentTimeMillis > 30) {
            log.info("saveBatchStreamLoad time:{}", System.currentTimeMillis() - currentTimeMillis);
        }

        currentTimeMillis = System.currentTimeMillis();
        List<AssetInfo> uniqueAssetInfos = assetInfos.stream()
                .filter(assetInfo -> StringUtils.isNotBlank(assetInfo.getAddress()) && assetInfo.getDecimals() != null) // 过滤
                .collect(Collectors.toMap(
                    AssetInfo::getAddress, // 使用地址作为 key
                    assetInfo -> assetInfo, // 保留的对象
                    (existing, replacement) -> existing // 如果有冲突，保留第一个
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        assetInfoService.saveOrUpdateBatchStreamLoad(uniqueAssetInfos);
        if (System.currentTimeMillis() - currentTimeMillis > 30) {
            log.info("saveBatchStreamLoad time:{}", System.currentTimeMillis() - currentTimeMillis);
        }

        log.info("asyncSaveOperations time :{}", System.currentTimeMillis() - l);
    }

    private void updateCalBlockRecord(Long lastId, Date blockTime) {
        ProfitCalRecord calBlockRecord = new ProfitCalRecord();
        calBlockRecord.setId(1L);
        calBlockRecord.setOffset(lastId);
        calBlockRecord.setBlockTime(blockTime);
        long start = System.currentTimeMillis();
        profitCalRecordService.saveBatchStreamLoad(List.of(calBlockRecord));
        if (System.currentTimeMillis() - start > 30) {
            log.info("updateCalBlockRecord time:{}", System.currentTimeMillis() - start);
        }
    }
}
