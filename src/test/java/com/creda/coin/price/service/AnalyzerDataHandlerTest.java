package com.creda.coin.price.service;

import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creda.coin.price.ProfitAnalyzerContext;
import com.creda.coin.price.constant.SolConstant;
import com.creda.coin.price.dto.LastHistoryDTO;
import com.creda.coin.price.dto.ProfitAnalyzerDTO;
import com.creda.coin.price.dto.SwapInstructionExtraInfo;
import com.creda.coin.price.entity.doris.CurrentPrice;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.entity.doris.TokenProfitHistoryLast;
import com.creda.coin.price.entity.es.SolanaTransaction;
import com.creda.coin.price.service.data.doris.ICurrentPriceService;
import com.creda.coin.price.service.data.doris.ITokenProfitHistoryService;
import com.creda.coin.price.service.data.doris.ITokenStatsHistoryService;
import com.creda.coin.price.service.data.doris.ITokenStatsLastTraderService;
import com.creda.coin.price.service.data.doris.ITokenSwapPriceHistoryService;
import com.creda.coin.price.service.data.doris.impl.TokenProfitHistoryLastServiceImpl;
import com.creda.coin.price.service.data.jdbc.IAssetInfoService;
import com.opencsv.CSVReader;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class AnalyzerDataHandlerTest {

    @InjectMocks
    private AnalyzerDataHandler analyzerDataHandler;
    @Mock
    private ITokenProfitHistoryService tokenProfitHistoryService;
    @Spy
    private TokenProfitHistoryLastServiceImpl tokenProfitHistoryLastService;
    @Mock
    private ICurrentPriceService currentPriceService;
    @Mock
    private IAssetInfoService assetInfoService;
    @Mock
    private ITokenSwapPriceHistoryService tokenSwapPriceHistoryService;
    @Mock
    private ITokenStatsLastTraderService tokenStatsLastTraderService;
    @Mock
    private ITokenStatsHistoryService tokenStatsHistoryService;

    private SolanaTransaction mockTransaction;
    private SwapInstructionExtraInfo mockExtraInfo;
    private TokenProfitHistoryLast mockOldProfitHistory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock transaction
        mockTransaction = new SolanaTransaction();
        mockTransaction.setId(123L);
        mockTransaction.setTokenProfitHistoryId(1L);
        mockTransaction.setBlockTime(new Date());
        mockTransaction.setSlot(9999L);
        mockTransaction.setHash("mockHash");

        // ProfitAnalyzerContext initialization
        ProfitAnalyzerDTO profitAnalyzerDTO = new ProfitAnalyzerDTO();
        ProfitAnalyzerContext.setProfitAnalyzerDTO(profitAnalyzerDTO);
    }


   /* @Test
    public void test1BuyErc20() {

        // Mock extra info
        mockExtraInfo = new SwapInstructionExtraInfo();
        mockExtraInfo.setPostAmount(new BigDecimal("200"));
        mockExtraInfo.setToken0("cd");
        mockExtraInfo.setToken1("So11111111111111111111111111111111111111112");
        mockExtraInfo.setToken0Amount(new BigDecimal("10"));
        mockExtraInfo.setToken1Amount(new BigDecimal("1"));

        // Mock old profit history
        mockOldProfitHistory = new TokenProfitHistoryLast();
        mockOldProfitHistory.setBoughtAmount(new BigDecimal("0"));
        mockOldProfitHistory.setTotalCost(new BigDecimal("0"));
        mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal("0"));
        mockOldProfitHistory.setRealizedProfit(new BigDecimal("0"));
        mockOldProfitHistory.setSoldAmount(new BigDecimal("0"));
        mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal("0"));
        mockOldProfitHistory.setSoldCount(0l);
        mockOldProfitHistory.setSoldCountWin(0l);
        mockOldProfitHistory.setBoughtCount(1l);
        mockOldProfitHistory.setSoldCountBoughtByUser(0l);
        String userAddress = "BY";
        String tokenAddress = "cd";

        when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress), eq(tokenAddress)))
                .thenReturn(mockOldProfitHistory);


        analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

        TokenProfitHistory generatedProfitHistory = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                .getTokenProfitHistoryList()
                .get(0);

        BigDecimal expectedTotalCost = mockOldProfitHistory.getTotalCost().add(new BigDecimal("10").multiply(new BigDecimal("0.1")));
        BigDecimal expectedUnrealizedProfit = new BigDecimal("0.1")
                .subtract(expectedTotalCost.divide(mockOldProfitHistory.getBoughtAmount().add(new BigDecimal("10")), 18, BigDecimal.ROUND_HALF_UP))
                .multiply(mockOldProfitHistory.getTotalBoughtAmountHasBeenLeft().add(new BigDecimal("10")));
        BigDecimal expectedRoi = expectedTotalCost.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : mockOldProfitHistory.getRealizedProfit().add(expectedUnrealizedProfit).divide(expectedTotalCost, 4, BigDecimal.ROUND_HALF_UP);

        assertEquals(expectedRoi, generatedProfitHistory.getRoi(), "ROI calculation is incorrect.");

        assertEquals(mockOldProfitHistory.getSoldCountWin(), generatedProfitHistory.getSoldCountWin(), "Win count is incorrect.");
        assertEquals(mockOldProfitHistory.getSoldCount(), generatedProfitHistory.getSoldCount(), "Sold count is incorrect.");

        assertEquals(1, ProfitAnalyzerContext.getProfitAnalyzerDTO().getTokenProfitHistoryList().size(),
                "Profit history list should contain one entry.");
        System.out.printf(JSONUtil.toJsonStr(generatedProfitHistory));

    }*/

    @SneakyThrows
    @Test
    public void testCase1() {
        // 定义 CSV 文件路径和需要读取的行范围
        String csvFilePath = "/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/test.csv";
        int startRow = 4; // n行（从1开始计数，包括标题）
        int endRow = 5;   // m行（包括endRow这一行）

        // 读取 CSV 文件
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();

        // 存储处理后的 profitHistory 以供校验
        TokenProfitHistory lastGeneratedHistory = null;

        String userAddress1 = allRows.get(startRow - 1)[1]; // userAddress
        String tokenAddress1 = allRows.get(startRow - 1)[2]; // tokenAddress
        LastHistoryDTO lstHistory = new LastHistoryDTO(userAddress1, tokenAddress1);
        TokenProfitHistoryLast defaultProfitHistory = tokenProfitHistoryLastService.createDefaultProfitHistory(lstHistory);
        tokenProfitHistoryLastService.updateProfitHistoryByCache(defaultProfitHistory);
       /* when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress1), eq(userAddress1)))
                .thenReturn(mockOldProfitHistory);*/
        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setPrice(BigDecimal.ONE);
        when(currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS)).thenReturn(currentPrice);

        // 遍历指定行范围内的记录
        for (int i = startRow - 1; i < endRow; i++) { // CSV 的行索引从 0 开始
            String[] csvData = allRows.get(i);

            // Mock extra info from CSV
            mockExtraInfo = new SwapInstructionExtraInfo();
            mockExtraInfo.setPostAmount(new BigDecimal(csvData[7])); // postAmount
            mockExtraInfo.setToken0(csvData[3]); // token0
            mockExtraInfo.setToken1(csvData[4]); // token1
            mockExtraInfo.setToken0Amount(new BigDecimal(csvData[5])); // token0Amount
            mockExtraInfo.setToken1Amount(new BigDecimal(csvData[6])); // token1Amount

            // Mock old profit history from CSV
            mockOldProfitHistory = new TokenProfitHistoryLast();
            mockOldProfitHistory.setBoughtAmount(new BigDecimal(csvData[12])); // oldBoughtAmount
            mockOldProfitHistory.setTotalCost(new BigDecimal(csvData[10])); // totalCost
            mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal(csvData[8])); // oldTotalBoughtAmountHasBeenLeft
            mockOldProfitHistory.setRealizedProfit(new BigDecimal(csvData[14])); // realizedProfit
            mockOldProfitHistory.setSoldAmount(new BigDecimal(csvData[9])); // totalSoldAmount
            mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal(csvData[10])); // soldAvgPrice
            mockOldProfitHistory.setSoldCount(Long.parseLong(csvData[16])); // sellCount
            mockOldProfitHistory.setSoldCountWin(Long.parseLong(csvData[17])); // sellCountWin
            mockOldProfitHistory.setBoughtCount(1l); // Assuming initial bought count
            mockOldProfitHistory.setSoldCountBoughtByUser(Long.parseLong(csvData[18])); // oldSoldCountBoughtByUser

            String userAddress = csvData[1]; // userAddress
            String tokenAddress = csvData[2]; // tokenAddress

            // 模拟服务返回的旧记录

            // 调用目标方法
            if (csvData[0].equals("Buy")) {
                analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Sell")) {
                analyzerDataHandler.sellErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Transfer in")) {
                analyzerDataHandler.transferInErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            } else if (csvData[0].equals("Transfer out")) {
                analyzerDataHandler.transferOutErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            }

            // 获取生成的 profitHistory
            List<TokenProfitHistory> tokenProfitHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                    .getTokenProfitHistoryList();
            lastGeneratedHistory = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1);

        }
        //    Case 1: 先买, 然后全部卖完, repnl = 10, unrepnl = 0, win rate 100%, roi 100%

        // 校验最终生成的 TokenProfitHistory 对象是否符合预期
        assertNotNull(lastGeneratedHistory, "Generated TokenProfitHistory should not be null.");

        // 校验已实现盈亏（repnl）
        assertEquals(new BigDecimal("1"), lastGeneratedHistory.getRealizedProfit().stripTrailingZeros(), "Realized PnL (repnl) is incorrect.");

        // 校验未实现盈亏（unrepnl）
        assertEquals(new BigDecimal("0"), lastGeneratedHistory.getUnrealizedProfit().stripTrailingZeros(), "Unrealized PnL (unrepnl) should be zero after selling all.");

        // 校验胜率（win rate）
        BigDecimal winRate = new BigDecimal(lastGeneratedHistory.getSoldCountWin())
                .divide(new BigDecimal(lastGeneratedHistory.getSoldCountBoughtByUser()), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("100").stripTrailingZeros().toPlainString(), winRate.stripTrailingZeros().toPlainString(), "Win rate calculation is incorrect.");

        // 校验投资回报率（ROI）
        assertEquals(new BigDecimal("100").stripTrailingZeros().toPlainString(), lastGeneratedHistory.getRoi().multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() ,"ROI calculation is incorrect.");
    }

    @SneakyThrows
    @Test
    public void testCase2() {
        // 定义 CSV 文件路径和需要读取的行范围
        String csvFilePath = "/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/test.csv";
        int startRow = 8; // n行（从1开始计数，包括标题）
        int endRow = 10;   // m行（包括endRow这一行）

        // 读取 CSV 文件
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();

        // 存储处理后的 profitHistory 以供校验
        TokenProfitHistory lastGeneratedHistory = null;

        String userAddress1 = allRows.get(startRow - 1)[1]; // userAddress
        String tokenAddress1 = allRows.get(startRow - 1)[2]; // tokenAddress
        LastHistoryDTO lstHistory = new LastHistoryDTO(userAddress1, tokenAddress1);
        TokenProfitHistoryLast defaultProfitHistory = tokenProfitHistoryLastService.createDefaultProfitHistory(lstHistory);
        tokenProfitHistoryLastService.updateProfitHistoryByCache(defaultProfitHistory);
       /* when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress1), eq(userAddress1)))
                .thenReturn(mockOldProfitHistory);*/
        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setPrice(BigDecimal.ONE);
        when(currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS)).thenReturn(currentPrice);

        // 遍历指定行范围内的记录
        for (int i = startRow - 1; i < endRow; i++) { // CSV 的行索引从 0 开始
            String[] csvData = allRows.get(i);

            // Mock extra info from CSV
            mockExtraInfo = new SwapInstructionExtraInfo();
            mockExtraInfo.setPostAmount(new BigDecimal(csvData[7])); // postAmount
            mockExtraInfo.setToken0(csvData[3]); // token0
            mockExtraInfo.setToken1(csvData[4]); // token1
            mockExtraInfo.setToken0Amount(new BigDecimal(csvData[5])); // token0Amount
            mockExtraInfo.setToken1Amount(new BigDecimal(csvData[6])); // token1Amount

            // Mock old profit history from CSV
            mockOldProfitHistory = new TokenProfitHistoryLast();
            mockOldProfitHistory.setBoughtAmount(new BigDecimal(csvData[12])); // oldBoughtAmount
            mockOldProfitHistory.setTotalCost(new BigDecimal(csvData[10])); // totalCost
            mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal(csvData[8])); // oldTotalBoughtAmountHasBeenLeft
            mockOldProfitHistory.setRealizedProfit(new BigDecimal(csvData[14])); // realizedProfit
            mockOldProfitHistory.setSoldAmount(new BigDecimal(csvData[9])); // totalSoldAmount
            mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal(csvData[10])); // soldAvgPrice
            mockOldProfitHistory.setSoldCount(Long.parseLong(csvData[16])); // sellCount
            mockOldProfitHistory.setSoldCountWin(Long.parseLong(csvData[17])); // sellCountWin
            mockOldProfitHistory.setBoughtCount(1l); // Assuming initial bought count
            mockOldProfitHistory.setSoldCountBoughtByUser(Long.parseLong(csvData[18])); // oldSoldCountBoughtByUser

            String userAddress = csvData[1]; // userAddress
            String tokenAddress = csvData[2]; // tokenAddress

            // 模拟服务返回的旧记录

            // 调用目标方法
            if (csvData[0].equals("Buy")) {
                analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Sell")) {
                analyzerDataHandler.sellErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Transfer in")) {
                analyzerDataHandler.transferInErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            } else if (csvData[0].equals("Transfer out")) {
                analyzerDataHandler.transferOutErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            }

            // 获取生成的 profitHistory
            List<TokenProfitHistory> tokenProfitHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                    .getTokenProfitHistoryList();
            lastGeneratedHistory = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1);

        }
        // Case 2: 先买, 然后卖出一部分, 然后全部卖完, re pnl = 0.1, unre pnl = 0, win rate 50%, roi 10%
        // 校验最终生成的 TokenProfitHistory 对象是否符合预期
        assertNotNull(lastGeneratedHistory, "Generated TokenProfitHistory should not be null.");

        // 校验已实现盈亏（repnl）
        assertEquals(new BigDecimal("0.1").stripTrailingZeros(),
                lastGeneratedHistory.getRealizedProfit().stripTrailingZeros(),
                "Realized PnL (repnl) is incorrect.");

        // 校验未实现盈亏（unrepnl）
        assertEquals(new BigDecimal("0").stripTrailingZeros(),
                lastGeneratedHistory.getUnrealizedProfit().stripTrailingZeros(),
                "Unrealized PnL (unrepnl) should be zero after selling all.");

        // 校验胜率（win rate）
        BigDecimal winRate = new BigDecimal(lastGeneratedHistory.getSoldCountWin())
                .divide(new BigDecimal(lastGeneratedHistory.getSoldCountBoughtByUser()), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("50").stripTrailingZeros().toPlainString(),
                winRate.stripTrailingZeros().toPlainString(),
                "Win rate calculation is incorrect.");

        // 校验投资回报率（ROI）
        BigDecimal roi = lastGeneratedHistory.getRoi().multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("10").stripTrailingZeros().toPlainString(),
                roi.stripTrailingZeros().toPlainString(),
                "ROI calculation is incorrect.");


    }
    @SneakyThrows
    @Test
    public void testCase3() {
        // 定义 CSV 文件路径和需要读取的行范围
        String csvFilePath = "/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/test.csv";
        int startRow = 13; // n行（从1开始计数，包括标题）
        int endRow = 16;   // m行（包括endRow这一行）

        // 读取 CSV 文件
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();

        // 存储处理后的 profitHistory 以供校验
        TokenProfitHistory lastGeneratedHistory = null;

        String userAddress1 = allRows.get(startRow - 1)[1]; // userAddress
        String tokenAddress1 = allRows.get(startRow - 1)[2]; // tokenAddress
        LastHistoryDTO lstHistory = new LastHistoryDTO(userAddress1, tokenAddress1);
        TokenProfitHistoryLast defaultProfitHistory = tokenProfitHistoryLastService.createDefaultProfitHistory(lstHistory);
        tokenProfitHistoryLastService.updateProfitHistoryByCache(defaultProfitHistory);
       /* when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress1), eq(userAddress1)))
                .thenReturn(mockOldProfitHistory);*/
        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setPrice(BigDecimal.ONE);
        when(currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS)).thenReturn(currentPrice);

        // 遍历指定行范围内的记录
        for (int i = startRow - 1; i < endRow; i++) { // CSV 的行索引从 0 开始
            String[] csvData = allRows.get(i);

            // Mock extra info from CSV
            mockExtraInfo = new SwapInstructionExtraInfo();
            mockExtraInfo.setPostAmount(new BigDecimal(csvData[7])); // postAmount
            mockExtraInfo.setToken0(csvData[3]); // token0
            mockExtraInfo.setToken1(csvData[4]); // token1
            mockExtraInfo.setToken0Amount(new BigDecimal(csvData[5])); // token0Amount
            mockExtraInfo.setToken1Amount(new BigDecimal(csvData[6])); // token1Amount

            // Mock old profit history from CSV
            mockOldProfitHistory = new TokenProfitHistoryLast();
            mockOldProfitHistory.setBoughtAmount(new BigDecimal(csvData[12])); // oldBoughtAmount
            mockOldProfitHistory.setTotalCost(new BigDecimal(csvData[10])); // totalCost
            mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal(csvData[8])); // oldTotalBoughtAmountHasBeenLeft
            mockOldProfitHistory.setRealizedProfit(new BigDecimal(csvData[14])); // realizedProfit
            mockOldProfitHistory.setSoldAmount(new BigDecimal(csvData[9])); // totalSoldAmount
            mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal(csvData[10])); // soldAvgPrice
            mockOldProfitHistory.setSoldCount(Long.parseLong(csvData[16])); // sellCount
            mockOldProfitHistory.setSoldCountWin(Long.parseLong(csvData[17])); // sellCountWin
            mockOldProfitHistory.setBoughtCount(1l); // Assuming initial bought count
            mockOldProfitHistory.setSoldCountBoughtByUser(Long.parseLong(csvData[18])); // oldSoldCountBoughtByUser

            String userAddress = csvData[1]; // userAddress
            String tokenAddress = csvData[2]; // tokenAddress

            // 模拟服务返回的旧记录

            // 调用目标方法
            if (csvData[0].equals("Buy")) {
                analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Sell")) {
                analyzerDataHandler.sellErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Transfer in")) {
                analyzerDataHandler.transferInErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            } else if (csvData[0].equals("Transfer out")) {
                analyzerDataHandler.transferOutErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            }

            // 获取生成的 profitHistory
            List<TokenProfitHistory> tokenProfitHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                    .getTokenProfitHistoryList();
            lastGeneratedHistory = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1);

        }
        // Case 3: 先买, 然后卖出一部分, 然后再买入,  然后全部卖出, re pnl = -1, unre pnl = 0, win rate 50%, roi -33.333%
        // 校验最终生成的 TokenProfitHistory 对象是否符合预期
        assertNotNull(lastGeneratedHistory, "Generated TokenProfitHistory should not be null.");

        // 校验已实现盈亏（repnl）
        assertEquals(new BigDecimal("-2.00000000000000001").stripTrailingZeros(),
                lastGeneratedHistory.getRealizedProfit().stripTrailingZeros(),
                "Realized PnL (repnl) is incorrect.");

        // 校验未实现盈亏（unrepnl）
        assertEquals(new BigDecimal("0").stripTrailingZeros(),
                lastGeneratedHistory.getUnrealizedProfit().stripTrailingZeros(),
                "Unrealized PnL (unrepnl) should be zero after selling all.");

        // 校验胜率（win rate）
        BigDecimal winRate = new BigDecimal(lastGeneratedHistory.getSoldCountWin())
                .divide(new BigDecimal(lastGeneratedHistory.getSoldCountBoughtByUser()), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("50").stripTrailingZeros().toPlainString(),
                winRate.stripTrailingZeros().toPlainString(),
                "Win rate calculation is incorrect.");

        // 校验投资回报率（ROI）
        BigDecimal roi = lastGeneratedHistory.getRoi().multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("-66.67").setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                roi.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                "ROI calculation is incorrect.");
    }
    @SneakyThrows
    @Test
    public void testCase4() {
        // 定义 CSV 文件路径和需要读取的行范围
        String csvFilePath = "/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/test.csv";
        int startRow = 19; // n行（从1开始计数，包括标题）
        int endRow = 21;   // m行（包括endRow这一行）

        // 读取 CSV 文件
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();

        // 存储处理后的 profitHistory 以供校验
        TokenProfitHistory lastGeneratedHistory = null;

        String userAddress1 = allRows.get(startRow - 1)[1]; // userAddress
        String tokenAddress1 = allRows.get(startRow - 1)[2]; // tokenAddress
        LastHistoryDTO lstHistory = new LastHistoryDTO(userAddress1, tokenAddress1);
        TokenProfitHistoryLast defaultProfitHistory = tokenProfitHistoryLastService.createDefaultProfitHistory(lstHistory);
        tokenProfitHistoryLastService.updateProfitHistoryByCache(defaultProfitHistory);
        /* when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress1), eq(userAddress1)))
                .thenReturn(mockOldProfitHistory);*/
        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setPrice(BigDecimal.ONE);
        when(currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS)).thenReturn(currentPrice);

        // 遍历指定行范围内的记录
        for (int i = startRow - 1; i < endRow; i++) { // CSV 的行索引从 0 开始
            String[] csvData = allRows.get(i);

            // Mock extra info from CSV
            mockExtraInfo = new SwapInstructionExtraInfo();
            mockExtraInfo.setPostAmount(new BigDecimal(csvData[7])); // postAmount
            mockExtraInfo.setToken0(csvData[3]); // token0
            mockExtraInfo.setToken1(csvData[4]); // token1
            mockExtraInfo.setToken0Amount(new BigDecimal(csvData[5])); // token0Amount
            mockExtraInfo.setToken1Amount(new BigDecimal(csvData[6])); // token1Amount

            // Mock old profit history from CSV
            mockOldProfitHistory = new TokenProfitHistoryLast();
            mockOldProfitHistory.setBoughtAmount(new BigDecimal(csvData[12])); // oldBoughtAmount
            mockOldProfitHistory.setTotalCost(new BigDecimal(csvData[10])); // totalCost
            mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal(csvData[8])); // oldTotalBoughtAmountHasBeenLeft
            mockOldProfitHistory.setRealizedProfit(new BigDecimal(csvData[14])); // realizedProfit
            mockOldProfitHistory.setSoldAmount(new BigDecimal(csvData[9])); // totalSoldAmount
            mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal(csvData[10])); // soldAvgPrice
            mockOldProfitHistory.setSoldCount(Long.parseLong(csvData[16])); // sellCount
            mockOldProfitHistory.setSoldCountWin(Long.parseLong(csvData[17])); // sellCountWin
            mockOldProfitHistory.setBoughtCount(1l); // Assuming initial bought count
            mockOldProfitHistory.setSoldCountBoughtByUser(Long.parseLong(csvData[18])); // oldSoldCountBoughtByUser

            String userAddress = csvData[1]; // userAddress
            String tokenAddress = csvData[2]; // tokenAddress

            // 模拟服务返回的旧记录

            // 调用目标方法
            if (csvData[0].equals("Buy")) {
                analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Sell")) {
                analyzerDataHandler.sellErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Transfer in")) {
                analyzerDataHandler.transferInErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            } else if (csvData[0].equals("Transfer out")) {
                analyzerDataHandler.transferOutErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            }

            // 获取生成的 profitHistory
            List<TokenProfitHistory> tokenProfitHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                    .getTokenProfitHistoryList();
            lastGeneratedHistory = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1);

        }
        // Case 4: 先买，买入两次，然后全部卖完（盈利）： PNL:  0.1   ，WinRate 33.33%  ROI = 3.33%
        // 校验最终生成的 TokenProfitHistory 对象是否符合预期
        assertNotNull(lastGeneratedHistory, "Generated TokenProfitHistory should not be null.");

        // 校验已实现盈亏（repnl）
        assertEquals(new BigDecimal("0.1").stripTrailingZeros(),
                lastGeneratedHistory.getRealizedProfit().stripTrailingZeros(),
                "Realized PnL (repnl) is incorrect.");

        // 校验未实现盈亏（unrepnl）
        assertEquals(new BigDecimal("0").stripTrailingZeros(),
                lastGeneratedHistory.getUnrealizedProfit().stripTrailingZeros(),
                "Unrealized PnL (unrepnl) should be zero after selling all.");

        // 校验胜率（win rate）
        BigDecimal winRate = new BigDecimal(lastGeneratedHistory.getSoldCountWin())
                .divide(new BigDecimal(lastGeneratedHistory.getSoldCountBoughtByUser()), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("100").stripTrailingZeros().toPlainString(),
                winRate.stripTrailingZeros().toPlainString(),
                "Win rate calculation is incorrect.");

        // 校验投资回报率（ROI）
        BigDecimal roi = lastGeneratedHistory.getRoi().multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("3.33").setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                roi.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                "ROI calculation is incorrect.");

    }
    @SneakyThrows
    @Test
    public void testCase5() {
        // 定义 CSV 文件路径和需要读取的行范围
        String csvFilePath = "/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/test.csv";
        int startRow = 24; // n行（从1开始计数，包括标题）
        int endRow = 26;   // m行（包括endRow这一行）

        // 读取 CSV 文件
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();

        // 存储处理后的 profitHistory 以供校验
        TokenProfitHistory lastGeneratedHistory = null;

        String userAddress1 = allRows.get(startRow - 1)[1]; // userAddress
        String tokenAddress1 = allRows.get(startRow - 1)[2]; // tokenAddress
        LastHistoryDTO lstHistory = new LastHistoryDTO(userAddress1, tokenAddress1);
        TokenProfitHistoryLast defaultProfitHistory = tokenProfitHistoryLastService.createDefaultProfitHistory(lstHistory);
        tokenProfitHistoryLastService.updateProfitHistoryByCache(defaultProfitHistory);
        /* when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress1), eq(userAddress1)))
                .thenReturn(mockOldProfitHistory);*/
        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setPrice(BigDecimal.ONE);
        when(currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS)).thenReturn(currentPrice);

        // 遍历指定行范围内的记录
        for (int i = startRow - 1; i < endRow; i++) { // CSV 的行索引从 0 开始
            String[] csvData = allRows.get(i);

            // Mock extra info from CSV
            mockExtraInfo = new SwapInstructionExtraInfo();
            mockExtraInfo.setPostAmount(new BigDecimal(csvData[7])); // postAmount
            mockExtraInfo.setToken0(csvData[3]); // token0
            mockExtraInfo.setToken1(csvData[4]); // token1
            mockExtraInfo.setToken0Amount(new BigDecimal(csvData[5])); // token0Amount
            mockExtraInfo.setToken1Amount(new BigDecimal(csvData[6])); // token1Amount

            // Mock old profit history from CSV
            mockOldProfitHistory = new TokenProfitHistoryLast();
            mockOldProfitHistory.setBoughtAmount(new BigDecimal(csvData[12])); // oldBoughtAmount
            mockOldProfitHistory.setTotalCost(new BigDecimal(csvData[10])); // totalCost
            mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal(csvData[8])); // oldTotalBoughtAmountHasBeenLeft
            mockOldProfitHistory.setRealizedProfit(new BigDecimal(csvData[14])); // realizedProfit
            mockOldProfitHistory.setSoldAmount(new BigDecimal(csvData[9])); // totalSoldAmount
            mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal(csvData[10])); // soldAvgPrice
            mockOldProfitHistory.setSoldCount(Long.parseLong(csvData[16])); // sellCount
            mockOldProfitHistory.setSoldCountWin(Long.parseLong(csvData[17])); // sellCountWin
            mockOldProfitHistory.setBoughtCount(1L); // Assuming initial bought count
            mockOldProfitHistory.setSoldCountBoughtByUser(Long.parseLong(csvData[18])); // oldSoldCountBoughtByUser

            String userAddress = csvData[1]; // userAddress
            String tokenAddress = csvData[2]; // tokenAddress

            // 模拟服务返回的旧记录

            // 调用目标方法
            if (csvData[0].equals("Buy")) {
                analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Sell")) {
                analyzerDataHandler.sellErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Transfer in")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                currentPriceEntity.setPrice(mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount()));
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferInErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            } else if (csvData[0].equals("Transfer out")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                currentPriceEntity.setPrice(mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount()));
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferOutErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            }

            // 获取生成的 profitHistory
            List<TokenProfitHistory> tokenProfitHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                    .getTokenProfitHistoryList();
            lastGeneratedHistory = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1);

        }
        // Case 5: A先买入，然后将部分 Token  Transfer 给账户 B，B再买入一部分，B再全部卖出, RePnL = 0.7, UnRePnL = 0, win rate 140%, roi 100%
        // 校验最终生成的 TokenProfitHistory 对象是否符合预期
        assertNotNull(lastGeneratedHistory, "Generated TokenProfitHistory should not be null.");

        // 校验已实现盈亏（repnl）
        assertEquals(new BigDecimal("0.7").stripTrailingZeros(),
                lastGeneratedHistory.getRealizedProfit().stripTrailingZeros(),
                "Realized PnL (repnl) is incorrect.");

        // 校验未实现盈亏（unrepnl）
        assertEquals(new BigDecimal("0").stripTrailingZeros(),
                lastGeneratedHistory.getUnrealizedProfit().stripTrailingZeros(),
                "Unrealized PnL (unrepnl) should be zero after selling all.");

        // 校验胜率（win rate）
        BigDecimal winRate = new BigDecimal(lastGeneratedHistory.getSoldCountWin())
                .divide(new BigDecimal(lastGeneratedHistory.getSoldCountBoughtByUser()), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("100").stripTrailingZeros().toPlainString(),
                winRate.stripTrailingZeros().toPlainString(),
                "Win rate calculation is incorrect.");

        // 校验投资回报率（ROI）
        BigDecimal roi = lastGeneratedHistory.getRoi().multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("140").setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                roi.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                "ROI calculation is incorrect.");


    }
    @SneakyThrows
    @Test
    public void testCase6() {
        // 定义 CSV 文件路径和需要读取的行范围
        String csvFilePath = "/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/test.csv";
        int startRow = 29; // n行（从1开始计数，包括标题）
        int endRow = 31;   // m行（包括endRow这一行）

        // 读取 CSV 文件
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();

        // 存储处理后的 profitHistory 以供校验
        TokenProfitHistory lastGeneratedHistory = null;

        String userAddress1 = allRows.get(startRow - 1)[1]; // userAddress
        String tokenAddress1 = allRows.get(startRow - 1)[2]; // tokenAddress
        LastHistoryDTO lstHistory = new LastHistoryDTO(userAddress1, tokenAddress1);
        TokenProfitHistoryLast defaultProfitHistory = tokenProfitHistoryLastService.createDefaultProfitHistory(lstHistory);
        tokenProfitHistoryLastService.updateProfitHistoryByCache(defaultProfitHistory);
        /* when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress1), eq(userAddress1)))
                .thenReturn(mockOldProfitHistory);*/
        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setPrice(BigDecimal.ONE);
        when(currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS)).thenReturn(currentPrice);

        // 遍历指定行范围内的记录
        for (int i = startRow - 1; i < endRow; i++) { // CSV 的行索引从 0 开始
            String[] csvData = allRows.get(i);

            // Mock extra info from CSV
            mockExtraInfo = new SwapInstructionExtraInfo();
            mockExtraInfo.setPostAmount(new BigDecimal(csvData[7])); // postAmount
            mockExtraInfo.setToken0(csvData[3]); // token0
            mockExtraInfo.setToken1(csvData[4]); // token1
            mockExtraInfo.setToken0Amount(new BigDecimal(csvData[5])); // token0Amount
            mockExtraInfo.setToken1Amount(new BigDecimal(csvData[6])); // token1Amount

            // Mock old profit history from CSV
            mockOldProfitHistory = new TokenProfitHistoryLast();
            mockOldProfitHistory.setBoughtAmount(new BigDecimal(csvData[12])); // oldBoughtAmount
            mockOldProfitHistory.setTotalCost(new BigDecimal(csvData[10])); // totalCost
            mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal(csvData[8])); // oldTotalBoughtAmountHasBeenLeft
            mockOldProfitHistory.setRealizedProfit(new BigDecimal(csvData[14])); // realizedProfit
            mockOldProfitHistory.setSoldAmount(new BigDecimal(csvData[9])); // totalSoldAmount
            mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal(csvData[10])); // soldAvgPrice
            mockOldProfitHistory.setSoldCount(Long.parseLong(csvData[16])); // sellCount
            mockOldProfitHistory.setSoldCountWin(Long.parseLong(csvData[17])); // sellCountWin
            mockOldProfitHistory.setBoughtCount(1l); // Assuming initial bought count
            mockOldProfitHistory.setSoldCountBoughtByUser(Long.parseLong(csvData[18])); // oldSoldCountBoughtByUser

            String userAddress = csvData[1]; // userAddress
            String tokenAddress = csvData[2]; // tokenAddress

            // 模拟服务返回的旧记录

            // 调用目标方法
            if (csvData[0].equals("Buy")) {
                analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Sell")) {
                analyzerDataHandler.sellErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Transfer in")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                currentPriceEntity.setPrice(mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount()));
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferInErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            } else if (csvData[0].equals("Transfer out")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                currentPriceEntity.setPrice(mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount()));
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferOutErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            }

            // 获取生成的 profitHistory
            List<TokenProfitHistory> tokenProfitHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                    .getTokenProfitHistoryList();
            lastGeneratedHistory = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1);

        }
        //      Case 6: A 买入，部分 Token Transfer 给账户 B，A 卖出部分，剩余部分未售出 ： RePnL = 20, UnRePnL = 13.3, win rate 100%, roi 33.3%
        // 校验最终生成的 TokenProfitHistory 对象是否符合预期
        assertNotNull(lastGeneratedHistory, "Generated TokenProfitHistory should not be null.");

        // 校验已实现盈亏（RePnL）
        assertEquals(new BigDecimal("20.00000000000000001").stripTrailingZeros(),
                lastGeneratedHistory.getRealizedProfit().stripTrailingZeros(),
                "Realized PnL (RePnL) is incorrect.");

        // 校验未实现盈亏（UnRePnL）
        assertEquals(new BigDecimal("13.33333333333333334").stripTrailingZeros(),
                lastGeneratedHistory.getUnrealizedProfit().stripTrailingZeros(),
                "Unrealized PnL (UnRePnL) is incorrect.");

        // 校验胜率（Win Rate）
        BigDecimal winRate = new BigDecimal(lastGeneratedHistory.getSoldCountWin())
                .divide(new BigDecimal(lastGeneratedHistory.getSoldCountBoughtByUser()), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("100").stripTrailingZeros().toPlainString(),
                winRate.stripTrailingZeros().toPlainString(),
                "Win rate calculation is incorrect.");

        // 校验投资回报率（ROI）
        BigDecimal roi = lastGeneratedHistory.getRoi().multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("33.33").setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                roi.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                "ROI calculation is incorrect.");
    }
    @SneakyThrows
    @Test
    public void testCase7() {
        // 定义 CSV 文件路径和需要读取的行范围
        String csvFilePath = "/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/test.csv";
        int startRow = 37; // n行（从1开始计数，包括标题）
        int endRow = 43;   // m行（包括endRow这一行）

        // 读取 CSV 文件
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();

        // 存储处理后的 profitHistory 以供校验
        TokenProfitHistory lastGeneratedHistory = null;

        String userAddress1 = allRows.get(startRow - 1)[1]; // userAddress
        String tokenAddress1 = allRows.get(startRow - 1)[2]; // tokenAddress
        LastHistoryDTO lstHistory = new LastHistoryDTO(userAddress1, tokenAddress1);
        TokenProfitHistoryLast defaultProfitHistory = tokenProfitHistoryLastService.createDefaultProfitHistory(lstHistory);
        tokenProfitHistoryLastService.updateProfitHistoryByCache(defaultProfitHistory);
       /* when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress1), eq(userAddress1)))
                .thenReturn(mockOldProfitHistory);*/
        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setPrice(BigDecimal.ONE);
        when(currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS)).thenReturn(currentPrice);

        // 遍历指定行范围内的记录
        for (int i = startRow - 1; i < endRow; i++) { // CSV 的行索引从 0 开始
            String[] csvData = allRows.get(i);

            // Mock extra info from CSV
            mockExtraInfo = new SwapInstructionExtraInfo();
            mockExtraInfo.setPostAmount(new BigDecimal(csvData[7])); // postAmount
            mockExtraInfo.setToken0(csvData[3]); // token0
            mockExtraInfo.setToken1(csvData[4]); // token1
            mockExtraInfo.setToken0Amount(new BigDecimal(csvData[5])); // token0Amount
            mockExtraInfo.setToken1Amount(new BigDecimal(csvData[6])); // token1Amount

            // Mock old profit history from CSV
            mockOldProfitHistory = new TokenProfitHistoryLast();
            mockOldProfitHistory.setBoughtAmount(new BigDecimal(csvData[12])); // oldBoughtAmount
            mockOldProfitHistory.setTotalCost(new BigDecimal(csvData[10])); // totalCost
            mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal(csvData[8])); // oldTotalBoughtAmountHasBeenLeft
            mockOldProfitHistory.setRealizedProfit(new BigDecimal(csvData[14])); // realizedProfit
            mockOldProfitHistory.setSoldAmount(new BigDecimal(csvData[9])); // totalSoldAmount
            mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal(csvData[10])); // soldAvgPrice
            mockOldProfitHistory.setSoldCount(Long.parseLong(csvData[16])); // sellCount
            mockOldProfitHistory.setSoldCountWin(Long.parseLong(csvData[17])); // sellCountWin
            mockOldProfitHistory.setBoughtCount(1l); // Assuming initial bought count
            mockOldProfitHistory.setSoldCountBoughtByUser(Long.parseLong(csvData[18])); // oldSoldCountBoughtByUser

            String userAddress = csvData[1]; // userAddress
            String tokenAddress = csvData[2]; // tokenAddress

            // 模拟服务返回的旧记录

            // 调用目标方法
            if (csvData[0].equals("Buy")) {
                analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Sell")) {
                analyzerDataHandler.sellErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Transfer in")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                currentPriceEntity.setPrice(mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount()));
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferInErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            } else if (csvData[0].equals("Transfer out")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                currentPriceEntity.setPrice(mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount()));
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferOutErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            }

            // 获取生成的 profitHistory
            List<TokenProfitHistory> tokenProfitHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                    .getTokenProfitHistoryList();
            lastGeneratedHistory = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1);

        }
        //     Case 7:  用户买入、接收 Transfer In、部分卖出、再次 Transfer In、Transfer Out 部分后再次买入并卖出 50% ： RePnL = -1.4, UnRePnL = 3.3, win rate 50%, roi 2.71%
        // 校验最终生成的 TokenProfitHistory 对象是否符合预期
        assertNotNull(lastGeneratedHistory, "Generated TokenProfitHistory should not be null.");

        // 校验已实现盈亏（repnl）
        assertEquals(new BigDecimal("-1.33333333333333334"),
            lastGeneratedHistory.getRealizedProfit().stripTrailingZeros(),
            "Realized PnL (repnl) is incorrect.");

        // 校验未实现盈亏（unrepnl）
        assertEquals(new BigDecimal("3.33333333333333333"),
            lastGeneratedHistory.getUnrealizedProfit().stripTrailingZeros(),
            "Unrealized PnL (unrepnl) is incorrect.");

        // 校验胜率（win rate）
        BigDecimal winRate = new BigDecimal(lastGeneratedHistory.getSoldCountWin())
            .divide(new BigDecimal(lastGeneratedHistory.getSoldCountBoughtByUser()), 2, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("50.00").stripTrailingZeros().toPlainString(),
            winRate.stripTrailingZeros().toPlainString(),
            "Win rate calculation is incorrect.");

        // 校验投资回报率（ROI）
        BigDecimal roi = lastGeneratedHistory.getRoi()
            .multiply(new BigDecimal("100")) // 转换为百分比
            .setScale(2, RoundingMode.HALF_UP); // 保留两位小数
        assertEquals(new BigDecimal("2.86").stripTrailingZeros().toPlainString(),
            roi.stripTrailingZeros().toPlainString(),
            "ROI calculation is incorrect.");
    }
    @SneakyThrows
    @Test
    public void testCase8() {
        // 定义 CSV 文件路径和需要读取的行范围
        String csvFilePath = "/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/test.csv";
        int startRow = 46; // n行（从1开始计数，包括标题）
        int endRow = 49;   // m行（包括endRow这一行）

        // 读取 CSV 文件
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();

        // 存储处理后的 profitHistory 以供校验
        TokenProfitHistory lastGeneratedHistory = null;

        String userAddress1 = allRows.get(startRow - 1)[1]; // userAddress
        String tokenAddress1 = allRows.get(startRow - 1)[2]; // tokenAddress
        LastHistoryDTO lstHistory = new LastHistoryDTO(userAddress1, tokenAddress1);
        TokenProfitHistoryLast defaultProfitHistory = tokenProfitHistoryLastService.createDefaultProfitHistory(lstHistory);
        tokenProfitHistoryLastService.updateProfitHistoryByCache(defaultProfitHistory);
       /* when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress1), eq(userAddress1)))
                .thenReturn(mockOldProfitHistory);*/
        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setPrice(BigDecimal.ONE);
        when(currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS)).thenReturn(currentPrice);

        // 遍历指定行范围内的记录
        for (int i = startRow - 1; i < endRow; i++) { // CSV 的行索引从 0 开始
            String[] csvData = allRows.get(i);

            // Mock extra info from CSV
            mockExtraInfo = new SwapInstructionExtraInfo();
            mockExtraInfo.setPostAmount(new BigDecimal(csvData[7])); // postAmount
            mockExtraInfo.setToken0(csvData[3]); // token0
            mockExtraInfo.setToken1(csvData[4]); // token1
            mockExtraInfo.setToken0Amount(new BigDecimal(csvData[5])); // token0Amount
            mockExtraInfo.setToken1Amount(new BigDecimal(csvData[6])); // token1Amount

            // Mock old profit history from CSV
            mockOldProfitHistory = new TokenProfitHistoryLast();
            mockOldProfitHistory.setBoughtAmount(new BigDecimal(csvData[12])); // oldBoughtAmount
            mockOldProfitHistory.setTotalCost(new BigDecimal(csvData[10])); // totalCost
            mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal(csvData[8])); // oldTotalBoughtAmountHasBeenLeft
            mockOldProfitHistory.setRealizedProfit(new BigDecimal(csvData[14])); // realizedProfit
            mockOldProfitHistory.setSoldAmount(new BigDecimal(csvData[9])); // totalSoldAmount
            mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal(csvData[10])); // soldAvgPrice
            mockOldProfitHistory.setSoldCount(Long.parseLong(csvData[16])); // sellCount
            mockOldProfitHistory.setSoldCountWin(Long.parseLong(csvData[17])); // sellCountWin
            mockOldProfitHistory.setBoughtCount(1l); // Assuming initial bought count
            mockOldProfitHistory.setSoldCountBoughtByUser(Long.parseLong(csvData[18])); // oldSoldCountBoughtByUser

            String userAddress = csvData[1]; // userAddress
            String tokenAddress = csvData[2]; // tokenAddress

            // 模拟服务返回的旧记录

            // 调用目标方法
            if (csvData[0].equals("Buy")) {
                analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Sell")) {
                analyzerDataHandler.sellErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Transfer in")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                currentPriceEntity.setPrice(mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount()));
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferInErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            } else if (csvData[0].equals("Transfer out")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                currentPriceEntity.setPrice(mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount()));
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferOutErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            }

            // 获取生成的 profitHistory
            List<TokenProfitHistory> tokenProfitHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                    .getTokenProfitHistoryList();
            lastGeneratedHistory = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1);

        }
        //     Case 7:  用户买入、接收 Transfer In、部分卖出、再次 Transfer In、Transfer Out 部分后再次买入并卖出 50% ： RePnL = -1.4, UnRePnL = 3.3, win rate 50%, roi 2.71%
        // 校验最终生成的 TokenProfitHistory 对象是否符合预期
        assertNotNull(lastGeneratedHistory, "Generated TokenProfitHistory should not be null.");

        // 校验已实现盈亏（repnl）
        assertEquals(new BigDecimal("0"),
            lastGeneratedHistory.getRealizedProfit().stripTrailingZeros(),
            "Realized PnL (repnl) is incorrect.");

        // 校验未实现盈亏（unrepnl）
        assertEquals(new BigDecimal("0"),
            lastGeneratedHistory.getUnrealizedProfit().stripTrailingZeros(),
            "Unrealized PnL (unrepnl) is incorrect.");

        // 校验胜率（win rate）
        BigDecimal winRate = new BigDecimal(lastGeneratedHistory.getSoldCountWin())
            .divide(new BigDecimal(lastGeneratedHistory.getSoldCountBoughtByUser()), 2, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100")); // 转换为百分比形式
        assertEquals(new BigDecimal("0").stripTrailingZeros().toPlainString(),
            winRate.stripTrailingZeros().toPlainString(),
            "Win rate calculation is incorrect.");

        // 校验投资回报率（ROI）
        BigDecimal roi = lastGeneratedHistory.getRoi()
            .multiply(new BigDecimal("100")) // 转换为百分比
            .setScale(2, RoundingMode.HALF_UP); // 保留两位小数
        assertEquals(new BigDecimal("0").stripTrailingZeros().toPlainString(),
            roi.stripTrailingZeros().toPlainString(),
            "ROI calculation is incorrect.");
    }
    @SneakyThrows
    @Test
    public void testCase9() {
        // 定义 CSV 文件路径和需要读取的行范围
        String csvFilePath = "/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/test.csv";
        int startRow = 52; // n行（从1开始计数，包括标题）
        int endRow = 55;   // m行（包括endRow这一行）

        // 读取 CSV 文件
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();

        // 存储处理后的 profitHistory 以供校验
        TokenProfitHistory lastGeneratedHistory = null;

        String userAddress1 = allRows.get(startRow - 1)[1]; // userAddress
        String tokenAddress1 = allRows.get(startRow - 1)[2]; // tokenAddress
        LastHistoryDTO lstHistory = new LastHistoryDTO(userAddress1, tokenAddress1);
        TokenProfitHistoryLast defaultProfitHistory = tokenProfitHistoryLastService.createDefaultProfitHistory(lstHistory);
        tokenProfitHistoryLastService.updateProfitHistoryByCache(defaultProfitHistory);
       /* when(tokenProfitHistoryLastService.getLastByAddressFromCache(eq(userAddress1), eq(userAddress1)))
                .thenReturn(mockOldProfitHistory);*/
        CurrentPrice currentPrice = new CurrentPrice();
        currentPrice.setPrice(BigDecimal.ONE);
        when(currentPriceService.getCurrentPrice(SolConstant.SOL_ADDRESS)).thenReturn(currentPrice);

        // 遍历指定行范围内的记录
        for (int i = startRow - 1; i < endRow; i++) { // CSV 的行索引从 0 开始
            String[] csvData = allRows.get(i);

            // Mock extra info from CSV
            mockExtraInfo = new SwapInstructionExtraInfo();
            mockExtraInfo.setPostAmount(new BigDecimal(csvData[7])); // postAmount
            mockExtraInfo.setToken0(csvData[3]); // token0
            mockExtraInfo.setToken1(csvData[4]); // token1
            mockExtraInfo.setToken0Amount(new BigDecimal(csvData[5])); // token0Amount
            mockExtraInfo.setToken1Amount(new BigDecimal(csvData[6])); // token1Amount

            // Mock old profit history from CSV
            mockOldProfitHistory = new TokenProfitHistoryLast();
            mockOldProfitHistory.setBoughtAmount(new BigDecimal(csvData[12])); // oldBoughtAmount
            mockOldProfitHistory.setTotalCost(new BigDecimal(csvData[10])); // totalCost
            mockOldProfitHistory.setTotalBoughtAmountHasBeenLeft(new BigDecimal(csvData[8])); // oldTotalBoughtAmountHasBeenLeft
            mockOldProfitHistory.setRealizedProfit(new BigDecimal(csvData[14])); // realizedProfit
            mockOldProfitHistory.setSoldAmount(new BigDecimal(csvData[9])); // totalSoldAmount
            mockOldProfitHistory.setHistoricalSoldAvgPrice(new BigDecimal(csvData[10])); // soldAvgPrice
            mockOldProfitHistory.setSoldCount(Long.parseLong(csvData[16])); // sellCount
            mockOldProfitHistory.setSoldCountWin(Long.parseLong(csvData[17])); // sellCountWin
            mockOldProfitHistory.setBoughtCount(1l); // Assuming initial bought count
            mockOldProfitHistory.setSoldCountBoughtByUser(Long.parseLong(csvData[18])); // oldSoldCountBoughtByUser

            String userAddress = csvData[1]; // userAddress
            String tokenAddress = csvData[2]; // tokenAddress

            // 模拟服务返回的旧记录

            // 调用目标方法
            if (csvData[0].equals("Buy")) {
                analyzerDataHandler.buyErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Sell")) {
                analyzerDataHandler.sellErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo);

            } else if (csvData[0].equals("Transfer in")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                currentPriceEntity.setPrice(mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount()));
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferInErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            } else if (csvData[0].equals("Transfer out")) {
                CurrentPrice currentPriceEntity = new CurrentPrice();
                BigDecimal price = mockExtraInfo.getToken1Amount().divide(mockExtraInfo.getToken0Amount(), 18, RoundingMode.HALF_UP);
                currentPriceEntity.setPrice(price);
                when(currentPriceService.getCurrentPrice(eq(tokenAddress))).thenReturn(currentPriceEntity);
                analyzerDataHandler.transferOutErc20Ext(mockTransaction, userAddress, tokenAddress, mockExtraInfo.getToken0Amount(), mockExtraInfo);

            }

            // 获取生成的 profitHistory
            List<TokenProfitHistory> tokenProfitHistoryList = ProfitAnalyzerContext.getProfitAnalyzerDTO()
                    .getTokenProfitHistoryList();
            lastGeneratedHistory = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1);

        }
        assertNotNull(lastGeneratedHistory, "Generated TokenProfitHistory should not be null.");

        // 校验已实现盈亏（repnl）
        assertEquals(new BigDecimal("0"),
            lastGeneratedHistory.getRealizedProfit().stripTrailingZeros(),
            "Realized PnL (repnl) is incorrect.");

        // 校验未实现盈亏（unrepnl）
        assertEquals(new BigDecimal("0"),
            lastGeneratedHistory.getUnrealizedProfit().stripTrailingZeros(),
            "Unrealized PnL (unrepnl) is incorrect.");

        // 校验胜率（win rate）
        BigDecimal winRate = BigDecimal.ZERO;
        assertEquals(new BigDecimal("0").stripTrailingZeros().toPlainString(),
            winRate.stripTrailingZeros().toPlainString(),
            "Win rate calculation is incorrect.");

        // 校验投资回报率（ROI）
        BigDecimal roi = lastGeneratedHistory.getRoi()
            .multiply(new BigDecimal("100")) // 转换为百分比
            .setScale(2, RoundingMode.HALF_UP); // 保留两位小数
        assertEquals(new BigDecimal("0").stripTrailingZeros().toPlainString(),
            roi.stripTrailingZeros().toPlainString(),
            "ROI calculation is incorrect.");

        Long boughtCount = lastGeneratedHistory.getBoughtCount();
        assertEquals(3l,boughtCount,"Bought count calculation is incorrect.");

        Long soldCount = lastGeneratedHistory.getSoldCount();
        assertEquals(0l,soldCount,"Sold count calculation is incorrect.");

        Long transferInCount = lastGeneratedHistory.getTransferInCount();
        assertEquals(0l,transferInCount,"Transfer in count calculation is incorrect.");

        Long transferOutCount = lastGeneratedHistory.getTransferOutCount();
        assertEquals(1l,transferOutCount,"Transfer out count calculation is incorrect.");
    }
}