package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.dto.TokenHoldersCountDTO;
import com.creda.coin.price.entity.CandleTrade;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Set;


@Mapper
public interface TokenProfitHistoryMapper extends BaseMapper<TokenProfitHistory> {
	@Select({
			"<script>",
			"SELECT token_address, COUNT(DISTINCT account) AS holders_count",
			"FROM token_profit_history",
			"WHERE token_address IN",
			"<foreach item='token' collection='tokenAddresses' open='(' separator=',' close=')'>",
			"#{token}",
			"</foreach>",
			"AND total_amount > 0",
			"GROUP BY token_address",
			"</script>"
	})
	List<TokenHoldersCountDTO> searchCountByTokenAddresses(List<String> tokenAddresses);

/*	@Select({
		"<script>",
		"SELECT token_address, COUNT(DISTINCT account) AS holders_count",
		"FROM token_profit_history",
		"WHERE token_address = #{tokenAddress}",
		"AND total_amount > 0",
		"</script>"
	})*/
	@Select("SELECT COUNT(DISTINCT account) as holders_count \n" +
			"FROM token_profit_history \n" +
			"WHERE token_address = #{tokenAddress} \n" +
			"AND total_amount > 0 \n" +
			"LIMIT 1")
	TokenHoldersCountDTO searchCountByTokenAddress(String tokenAddress);

	// 定义方法，接受 accounts 和 time 参数
	List<TokenProfitHistory> getAccountsTokenProfitsAtTime(@Param("accounts") Set<String> accounts, @Param("time") long time);

	@Select("SELECT * FROM token_profit_history ORDER BY id DESC LIMIT 1")
	TokenProfitHistory findLast();

	@Select("  SELECT *\n" +
			"    FROM token_profit_history\n" +
			"    WHERE token_address = #{assetAddress}\n" +
			"    ORDER BY id ASC\n" +
			"    LIMIT 1")
	TokenProfitHistory findFirstByAddress(@Param("assetAddress") String assetAddress);

	@Select("SELECT *\n" +
			"    FROM token_profit_history\n" +
			"    WHERE token_address = #{assetAddress}\n" +
			"      AND block_time >= #{nextCandleStartTime}\n" +
			"      AND block_time < #{nextCandleEndTime}\n" +
			"    ORDER BY block_time ASC")
	List<TokenProfitHistory> findByAddressAndTimeRange(@Param("assetAddress") String assetAddress,
													   @Param("nextCandleStartTime") Date nextCandleStartTime,
													   @Param("nextCandleEndTime") Date nextCandleEndTime);

	@Select(" SELECT *\n" +
			"    FROM token_profit_history\n" +
			"    WHERE token_address = #{assetAddress}\n" +
			"      AND block_time < #{nextCandleStartTime}\n" +
			"    ORDER BY block_time DESC\n" +
			"    LIMIT 1")
	TokenProfitHistory findLastBeforeTime(@Param("assetAddress") String assetAddress,
										  @Param("nextCandleStartTime") Date nextCandleStartTime);


	@Select({
			"SELECT",
			"    account,",
			"    token_address,",
			"    type,",
			"    COUNT(*) AS count,",
			"    SUM(amount) AS amount",
			"FROM",
			"    token_profit_history",
			"WHERE",
			"    block_time >= #{startTime}",
			"    AND block_time < #{endTime}",
			"GROUP BY",
			"    account, token_address, type",
			"ORDER BY",
			"    account, token_address, type"
	})
	List<CandleTrade> getCandleDataByTimeRange(
			@Param("startTime") Date startTime,
			@Param("endTime") Date endTime
	);

	@Select("SELECT * FROM token_profit_history ORDER BY id ASC LIMIT 1")
	TokenProfitHistory finFirst();
}
