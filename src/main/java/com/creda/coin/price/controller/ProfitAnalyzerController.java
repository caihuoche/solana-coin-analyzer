package com.creda.coin.price.controller;

import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.service.ProfitAnalyzerService;
import com.creda.coin.price.service.TopCoinsAsyncService;
import com.creda.coin.price.service.TopTradersAsyncService;
import com.creda.coin.price.service.data.doris.ITokenProfitHistoryService;
import com.creda.coin.price.util.JsonUtil;
import com.creda.coin.price.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author gavin
 * @date 2024/10/10
 **/
@Slf4j
@RestController
@RequestMapping("/sol")
public class ProfitAnalyzerController {

    @Resource
    private ProfitAnalyzerService profitAnalyzerService;
    @Resource
    private ITokenProfitHistoryService tokenProfitHistoryService;

    @Resource
    private TopCoinsAsyncService topCoinsAsyncService;
    @Resource
    private TopTradersAsyncService topTradersAsyncService;

    @GetMapping("/stop")
    public void updateTotalSupply() {
        log.info("stop");
        profitAnalyzerService.setEnable(false);
    }

    @GetMapping("/thread")
    public String getThread() {
        Map<String, Object> threadPoolStats = ThreadUtils.getThreadPoolStats(topCoinsAsyncService.executorService);
        Map<String, Object> threadPoolStats1 = ThreadUtils.getThreadPoolStats(topTradersAsyncService.executorService);
        Map<String, Object> threadPoolStats2 = ThreadUtils.getThreadPoolStats(topTradersAsyncService.executorService2);

        return "topCoinsAsyncService:" + JsonUtil.toJson(threadPoolStats) + "\n" + "topTradersAsyncService.executorService:" + JsonUtil.toJson(threadPoolStats1) + "\n" + "topTradersAsyncService.executorService2" + JsonUtil.toJson(threadPoolStats2);
    }

    @GetMapping("/health")
    public String health() {
        TokenProfitHistory last = tokenProfitHistoryService.findLast();
        if (last == null) {
            return "down";
        }
        return "ok";
    }

}
