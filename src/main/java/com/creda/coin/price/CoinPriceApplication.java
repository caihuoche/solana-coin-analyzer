package com.creda.coin.price;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableScheduling
//@EnableSentry(dsn = "https://9a35adb1a990b264abf57a8d3468fd78@o4508359334100992.ingest.us.sentry.io/4508376313823232")
public class CoinPriceApplication {
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	public static void main(String[] args) {
		SpringApplication.run(CoinPriceApplication.class, args);

		System.out.println("Hello World!");
	}
	@PostConstruct
	public void init() {
		// 设置应用程序的默认时区为 UTC
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Chongqing"));
	}
}
