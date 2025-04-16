package com.creda.coin.price.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.dto.TransactionCondition;
import com.creda.coin.price.entity.SolanaTransactions;
import com.creda.coin.price.entity.es.Instruction;
import com.creda.coin.price.entity.es.SolanaTransaction;
import com.creda.coin.price.mapper.SolanaTransactionsMapper;
import com.creda.coin.price.service.ISolanaTransactionsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-11-16
 */
@Service
@Slf4j
public class SolanaTransactionsServiceImpl extends ServiceImpl<SolanaTransactionsMapper, SolanaTransactions> implements ISolanaTransactionsService {

    @Value("${es.search.limit:5000}")
    private Integer dorisSearchLimit;

    @Override
    public List<SolanaTransaction> searchTransactions(TransactionCondition condition) {
        long currentTimeMillis = System.currentTimeMillis();
        // 初始化日期范围
        if (condition.getStartDate() == null || condition.getEndDate() == null) {
            LocalDate blockTime = condition.getBlockTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            condition.setStartDate(blockTime);
            condition.setEndDate(blockTime.plusDays(1));
        }
        log.info("searchTransactions startDate: {}, endDate: {}, lastId: {}", condition.getStartDate(), condition.getEndDate(), condition.getLastId());
        // 第一次查询
        List<SolanaTransactions> solanaTransactions = this.baseMapper.searchTransactions(
                condition.getLastId(),
                condition.getStartDate(),
                condition.getEndDate(),
                dorisSearchLimit // 每次查询限制 5000 条
        );

        log.info("searchTransactions not convert cost: {} ms", System.currentTimeMillis() - currentTimeMillis);
        List<SolanaTransaction> transactions = convert(solanaTransactions);
        if (transactions != null && !transactions.isEmpty()) {
            log.info("searchTransactions cost: {} ms", System.currentTimeMillis() - currentTimeMillis);
            return transactions; // 如果查询到数据，直接返回
        }

        // 扩展日期范围多查一天
        condition.setStartDate(condition.getEndDate());
        condition.setEndDate(condition.getEndDate().plusDays(1));

        // 第二次查询
        solanaTransactions = this.baseMapper.searchTransactions(
            condition.getLastId(),
            condition.getStartDate(),
            condition.getEndDate(),
            dorisSearchLimit
        );
        transactions = convert(solanaTransactions);

        if (transactions != null && !transactions.isEmpty()) {
            return transactions; // 如果多查一天有数据，返回
        }

        return Collections.emptyList(); // 多查一天仍无数据，返回空列表
    }

    @Autowired
    private ObjectMapper objectMapper; // 用于解析 JSON 数据

    public List<SolanaTransaction> convert(List<SolanaTransactions> solanaTransactions) {
        return solanaTransactions.stream().map(this::convertSingle).collect(Collectors.toList());
    }

    private SolanaTransaction convertSingle(SolanaTransactions tx) {
        SolanaTransaction transaction = new SolanaTransaction();

        transaction.setId(tx.getId());
        transaction.setBlockTime(tx.getBlockTime());
        transaction.setSlot(tx.getSlot());
        transaction.setBlockHeight(tx.getBlockHeight());
        transaction.setIndex(tx.getIndex());
        transaction.setHash(tx.getHash());
        transaction.setCreatedAt(tx.getCreatedAt());
        transaction.setUpdatedAt(tx.getUpdatedAt());

        // 解析 JSON 字段
        transaction.setAccountKeys(parseJsonArray(tx.getAccountKeys(), new TypeReference<List<String>>() {}));
        transaction.setInstructions(parseJsonArray(tx.getInstructions(), new TypeReference<List<Instruction>>() {}));

        // 构建 Meta 信息
        SolanaTransaction.Meta meta = new SolanaTransaction.Meta();
        meta.setFee(tx.getFee());
        meta.setPreBalances(parseJsonArray(tx.getPreBalances(), new TypeReference<List<Long>>() {}));
        meta.setPostBalances(parseJsonArray(tx.getPostBalances(), new TypeReference<List<Long>>() {}));
        meta.setPreTokenBalances(parseJsonArray(tx.getPreTokenBalances(), new TypeReference<List<SolanaTransaction.Meta.TokenBalance>>() {}));
        meta.setPostTokenBalances(parseJsonArray(tx.getPostTokenBalances(), new TypeReference<List<SolanaTransaction.Meta.TokenBalance>>() {}));
        meta.setInnerInstructions(parseJsonArray(tx.getInnerInstructions(), new TypeReference<List<SolanaTransaction.Meta.InnerInstruction>>() {}));
        transaction.setMeta(meta);

        return transaction;
    }

    private <T> T parseJsonArray(Object json, TypeReference<T> typeReference) {
        if (json == null || !(json instanceof String)) {
            return null;
        }
        try {
            return objectMapper.readValue((String) json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("JSON parsing error: " + json, e);
        }
    }

}
