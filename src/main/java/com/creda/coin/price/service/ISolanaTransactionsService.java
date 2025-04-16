package com.creda.coin.price.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.dto.TransactionCondition;
import com.creda.coin.price.entity.SolanaTransactions;
import com.creda.coin.price.entity.es.SolanaTransaction;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-16
 */
public interface ISolanaTransactionsService extends IService<SolanaTransactions> {

    List<SolanaTransaction> searchTransactions(TransactionCondition condition);
}
