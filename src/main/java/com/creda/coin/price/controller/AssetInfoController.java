package com.creda.coin.price.controller;

import com.creda.coin.price.job.CandleCalculationJob;
import com.creda.coin.price.test.ClickHouseDataGenerator;
import com.creda.coin.price.test.ClickHousePerformanceTest;
import com.creda.coin.price.test.ElasticsearchPerformanceTest;
import com.creda.coin.price.job.TopCoinJob;
import com.creda.coin.price.service.data.doris.ITokenProfitHistoryService;
import com.creda.coin.price.service.data.jdbc.impl.AssetInfoServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author
 * gavin
 * @date 2024/10/10
 **/
@RestController
@RequestMapping("/assetInfo")
public class AssetInfoController {

    @Resource
    private TopCoinJob topCoinJob;
    @Resource
    private AssetInfoServiceImpl assetInfoService;
    @Resource
    private CandleCalculationJob candleCalculationJob;
    @Resource
    private ITokenProfitHistoryService tokenProfitHistoryService;

    @GetMapping("/supply")
    public void updateTotalSupply() {
        assetInfoService.updateTotalSupply();
    }
    @GetMapping("/topcoins")
    public void setTopCoinJob() {
        topCoinJob.run();
    }
    @GetMapping("/can")
    public void calculateCandles() {
        candleCalculationJob.calculateCandles();;
    }
    @GetMapping("/count/{token}")
    public long calculateCandles(@PathVariable String token) {
        return tokenProfitHistoryService.searchCountByTokenAddress(token);
    }
    @GetMapping("/click/{num}")
    public void click(@PathVariable String num) throws Exception {
        String[] arr = {num};
        ClickHousePerformanceTest.main(arr);
    }
    @GetMapping("/es/{num}")
    public void es(@PathVariable String num) throws IOException, InterruptedException {
        String[] arr = {num};
        ElasticsearchPerformanceTest.main(arr);
    }
    @GetMapping("/click/test/{num}")
    public void clickTest(@PathVariable String num) throws IOException, InterruptedException {
        String[] arr = {num};
        ClickHouseDataGenerator.main(arr);
    }
    @GetMapping("/click/test/trans/{num}/{startId}")
    public void trans(@PathVariable Integer num, @PathVariable Integer startId) throws IOException, InterruptedException {
        ClickHouseDataGenerator.Transactions(num, startId);
    }
    @GetMapping("/click/test/ins/{num}/{startId}")
    public void ins(@PathVariable Integer num, @PathVariable Integer startId) throws IOException, InterruptedException {
        ClickHouseDataGenerator.Instructions(num, startId);
    }
}
