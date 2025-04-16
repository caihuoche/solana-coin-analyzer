/*
package com.creda.coin.price.service.data.jdbc;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.dto.TokenHoldersCountDTO;
import com.creda.coin.price.dto.TokenPriceDTO;
import com.creda.coin.price.dto.TokenTxCountDTO;
import com.creda.coin.price.entity.TokenProfitHistory;

*/
/**
 * <p>
 * 主币历史盈利 服务类
 * </p>
 *
 * @author gavin
 * @since 2024-08-15
 *//*

public interface ITokenProfitHistoryService extends IService<TokenProfitHistory> {

	TokenProfitHistory getLastByAddress(String address, String assetAddress);

	void updateProfitHistory(TokenProfitHistory profitHistory);

	List<TokenProfitHistory> getProfitHistoryByTime(String baseToken, Date startTime);

	List<TokenTxCountDTO> getTokenTradeCounts(Date startTime, Date endTime);


	List<TokenPriceDTO> getLatestIds();


	List<TokenPriceDTO> getCurrentPricesByIds(List<Long> latestIdList);


	List<TokenPriceDTO> getPriceAtTime(Date startTime, Date endTime);

	List<TokenHoldersCountDTO> getHoldersMap();


	List<String> listTokenAddress();

	List<String> getAllAddressByAsset(String assetAddress);

	List<TokenProfitHistory> queryLastTransactionBefore(String assetAddress, List<String> users, Date time);

	List<TokenProfitHistory> getTransactionsForAsset(String assetAddress, Date startTime, Date endTime);

	TokenProfitHistory getLastTransactionBeforeEnd(String assetAddress, Date endTime);
}
*/
