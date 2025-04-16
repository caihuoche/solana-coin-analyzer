package com.creda.coin.price.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DorisConfig {
    
    @Value("${doris.stream-load.url}")
    private String streamLoadUrl;

    @Value("${doris.stream-load.username}")
    private String username;

    @Value("${doris.stream-load.password}")
    private String password;

    @Value("${doris.stream-load.database}")
    private String database;

    public String getStreamLoadUrl(String database, String table) {
        return streamLoadUrl.replace("{database}", database).replace("{table}", table);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }
}
