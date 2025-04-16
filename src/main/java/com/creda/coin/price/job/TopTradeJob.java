package com.creda.coin.price.job;

import com.creda.coin.price.service.TopTraderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TopTradeJob {

    @Autowired
    private TopTraderService topTraderService;
    @Value("${job.enabled}")
    private boolean enabled;
//    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void run() {
        try {
            if (!enabled) {
                return;
            }
            log.info("TopTradeJob run");
            topTraderService.run();

            log.info("TopTradeJob run end");
        }catch (Exception e){
            log.error("TopTradeJob error", e);
        }
    }

}
