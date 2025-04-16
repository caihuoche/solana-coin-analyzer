package com.creda.coin.price.runner;

import com.creda.coin.price.service.ProfitAnalyzerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {

    @Resource
    private ProfitAnalyzerService profitAnalyzerService;


    @Override
    public void run(String... args) {
        try {
            new Thread(() -> {
                profitAnalyzerService.profitAnalyzer();
            }).start();
        } catch (Exception e) {
            log.error("StartupRunner encountered an error", e);
        }
    }


}
