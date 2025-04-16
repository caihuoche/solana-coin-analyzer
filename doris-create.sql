-- delete from token_profit_history;
-- delete from token_profit_history_last;

-- delete from token_stats_1_hour;
-- delete from token_stats_24_hour;
-- delete from token_stats_30_day;
-- delete from token_stats_5_min;
-- delete from token_stats_7_day;

-- delete from token_stats_history;
-- delete from token_stats_last_trader;

-- delete from trader_stats_1_day;
-- delete from trader_stats_1_month;
-- delete from trader_stats_3_months;
-- delete from trader_stats_7_days;
-- delete from trader_stats_last_trade;
-- delete from trader_stats_by_token;

-- delete from token_swap_price_history;
-- delete from candle;
-- delete from current_price;
-- delete from profit_cal_record;

-- insert into profit_cal_record values (1,300660261, 30066026100000, "2024-11-10 00:35:04");

---------------------------------------------------
CREATE TABLE `current_price` (
  `token_address` varchar(255) NULL COMMENT 'token地址',
  `block_height` bigint NULL COMMENT '区块高度',
  `block_time` datetime NULL COMMENT '区块时间',
  `price` decimal(38,18) NULL COMMENT '当前价格',
  `tx_hash` varchar(255) NULL COMMENT '交易哈希'
) ENGINE=OLAP
UNIQUE KEY(`token_address`)
DISTRIBUTED BY HASH(`token_address`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);

CREATE TABLE `candle` (
  `address` varchar(255) NOT NULL COMMENT 'token地址',
  `granularity` bigint NOT NULL COMMENT '粒度',
  `time` bigint NOT NULL COMMENT '开始时间',
  `time_date` datetime NULL,
  `low` decimal(38,18) NOT NULL COMMENT '最低价',
  `high` decimal(38,18) NOT NULL COMMENT '最高价',
  `open` decimal(38,18) NOT NULL COMMENT '开盘价',
  `close` decimal(38,18) NOT NULL COMMENT '收盘价'
) ENGINE=OLAP
UNIQUE KEY(`address`, `granularity`, `time`)
DISTRIBUTED BY HASH(`address`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);

CREATE TABLE `trader_stats_1_day` (
  `account` varchar(255) NULL COMMENT '交易账户地址',
  `block_height` bigint NULL COMMENT '区块高度',
  `block_time` datetime NULL COMMENT '区块时间',
  `bought_count` bigint NULL DEFAULT "0" COMMENT '买入次数',
  `profit` decimal(38,18) NULL DEFAULT "0" COMMENT '利润',
  `realized_profit` decimal(38,18) NULL DEFAULT "0" COMMENT '已实现利润',
  `unrealized_profit` decimal(38,18) NULL DEFAULT "0" COMMENT '未实现利润',
  `roi` decimal(10,4) NULL DEFAULT "0" COMMENT '投资回报率',
  `realized_roi`  decimal(10,4) NULL,
  `unrealized_roi`  decimal(10,4) NULL,
  `win_rate` decimal(10,4) NULL DEFAULT "0" COMMENT '胜率',
  `sold_count` bigint NULL DEFAULT "0" COMMENT '卖出次数',
  `sold_count_bought_by_user` bigint NULL DEFAULT "0",
  `sold_count_win` bigint NULL DEFAULT "0" COMMENT '卖出获利次数',
  `swap_count` bigint NULL DEFAULT "0" COMMENT '交换次数',
  `trade_count` bigint NULL DEFAULT "0" COMMENT '交易次数',
  `transfer_count` bigint NULL DEFAULT "0" COMMENT '转账次数',
  `transfer_in_count` bigint NULL DEFAULT "0" COMMENT '转入次数',
  `transfer_out_count` bigint NULL DEFAULT "0" COMMENT '转出次数',
  `tx_hash` varchar(255) NULL COMMENT '交易哈希'
) ENGINE=OLAP
UNIQUE KEY(`account`)
DISTRIBUTED BY HASH(`account`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "account",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);

CREATE TABLE `trader_stats_by_token` (
  `account` varchar(255) NOT NULL COMMENT '交易账户',
  `token_address` varchar(255) NOT NULL COMMENT '代币地址',
  `amount` decimal(38,18) NULL DEFAULT "0" COMMENT '金额',
  `block_height` bigint NOT NULL COMMENT '区块高度',
  `block_time` datetime NOT NULL COMMENT '区块时间',
  `bought_amount` decimal(38,18) NULL DEFAULT "0" COMMENT '买入金额',
  `bought_count` bigint NULL DEFAULT "0" COMMENT '买入次数',
  `holding_avg_price` decimal(38,18) NULL DEFAULT "0" COMMENT '持有均价',
  `profit` decimal(38,18) NULL DEFAULT "0" COMMENT '利润',
  `realized_profit` decimal(38,18) NULL DEFAULT "0" COMMENT '已实现利润',
  `roi` decimal(10,4) NULL DEFAULT "0" COMMENT '投资回报率',
  `realized_roi`  decimal(10,4) NULL,
  `unrealized_roi`  decimal(10,4) NULL,
  `sold_amount` decimal(38,18) NULL DEFAULT "0" COMMENT '卖出金额',
  `sold_avg_price` decimal(38,18) NULL DEFAULT "0" COMMENT '卖出均价',
  `sold_count` bigint NULL DEFAULT "0" COMMENT '卖出次数',
  `sold_count_win` bigint NULL DEFAULT "0" COMMENT '盈利卖出次数',
  `swap_count` bigint NULL DEFAULT "0" COMMENT '交换次数',
  `total_cost` decimal(38,18) NULL DEFAULT "0" COMMENT '总成本',
  `trade_count` bigint NULL DEFAULT "0" COMMENT '交易次数',
  `transfer_count` bigint NULL DEFAULT "0" COMMENT '转账次数',
  `transfer_in_count` bigint NULL DEFAULT "0" COMMENT '转入次数',
  `transfer_out_count` bigint NULL DEFAULT "0" COMMENT '转出次数',
  `transfer_in_amount` decimal(38,18) NULL COMMENT '转入金额',
  `transfer_out_amount` decimal(38,18) NULL COMMENT '转出金额',
  `tx_hash` varchar(255) NOT NULL COMMENT '交易哈希',
  `unrealized_profit` decimal(38,18) NULL DEFAULT "0" COMMENT '未实现利润',
  `win_rate` decimal(10,4) NULL DEFAULT "0" COMMENT '胜率',
  `sold_count_bought_by_user` bigint NULL DEFAULT "0"
) ENGINE=OLAP
UNIQUE KEY(`account`, `token_address`)
DISTRIBUTED BY HASH(`account`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "account",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);


CREATE TABLE `token_swap_price_history` (
  `id` bigint NOT NULL AUTO_INCREMENT(1),
  `block_time` datetime NOT NULL COMMENT '区块时间',
  `block_height` bigint NOT NULL COMMENT '区块高度',
  `price` decimal(38,18) NOT NULL COMMENT '交易价格',
  `token_address` varchar(255) NOT NULL COMMENT 'Token 地址',
  `tx_hash` varchar(255) NOT NULL COMMENT '交易哈希',
  `tx_id` bigint NOT NULL COMMENT '交易 ID'
) ENGINE=OLAP
UNIQUE KEY(`id`, `block_time`)
AUTO PARTITION BY RANGE (date_trunc(`block_time`, 'day'))
()
DISTRIBUTED BY HASH(`id`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);


CREATE TABLE `token_stats_last_trader` (
  `token_address` varchar(255) NULL COMMENT 'token地址',
  `block_height` bigint NULL COMMENT '区块高度',
  `block_time` datetime NULL COMMENT '区块时间',
  `bought_count` bigint NULL DEFAULT "0" COMMENT '买入次数',
  `sold_count` bigint NULL DEFAULT "0" COMMENT '卖出次数',
  `transfer_in_count` bigint NULL DEFAULT "0" COMMENT '转入次数',
  `transfer_out_count` bigint NULL DEFAULT "0" COMMENT '转出次数',
  `volume` decimal(38,18) NULL DEFAULT "0" COMMENT '交易量'
) ENGINE=OLAP
UNIQUE KEY(`token_address`)
DISTRIBUTED BY HASH(`token_address`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);


CREATE TABLE `token_stats_history` (
  `id` bigint NOT NULL AUTO_INCREMENT(1),
  `block_time` datetime NOT NULL COMMENT '区块时间',
  `block_height` bigint NOT NULL COMMENT '区块高度',
  `bought_count` bigint NULL DEFAULT "0" COMMENT '买入次数',
  `sold_count` bigint NULL DEFAULT "0" COMMENT '卖出次数',
  `token_address` varchar(255) NOT NULL COMMENT 'Token 地址',
  `transfer_in_count` bigint NULL DEFAULT "0" COMMENT '转入次数',
  `transfer_out_count` bigint NULL DEFAULT "0" COMMENT '转出次数',
  `volume` decimal(38,18) NOT NULL COMMENT '交易量'
) ENGINE=OLAP
UNIQUE KEY(`id`, `block_time`)
AUTO PARTITION BY RANGE (date_trunc(`block_time`, 'day'))
()
DISTRIBUTED BY HASH(`id`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "token_address",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);

CREATE TABLE `token_stats_1_hour` (
  `token_address` varchar(255) NULL COMMENT 'Token 地址',
  `bought_count` bigint NULL DEFAULT "0" COMMENT '买入次数',
  `current_price` decimal(38,18) NULL COMMENT '当前价格',
  `holders_count` bigint NULL DEFAULT "0" COMMENT '持有者数量',
  `liquidity` decimal(38,18) NULL COMMENT '流动性',
  `market_cap` decimal(38,18) NULL COMMENT '市值',
  `price_change1h` decimal(10,4) NULL DEFAULT "0" COMMENT '1小时价格变动百分比',
  `price_change24h` decimal(10,4) NULL DEFAULT "0" COMMENT '24小时价格变动百分比',
  `price_change5m` decimal(10,4) NULL DEFAULT "0" COMMENT '5分钟价格变动百分比',
  `price_change7d` decimal(10,4) NULL DEFAULT "0" COMMENT '7天价格变动百分比',
  `sold_count` bigint NULL DEFAULT "0" COMMENT '卖出次数',
  `swap_count` bigint NULL DEFAULT "0" COMMENT '交换次数',
  `trade_count` bigint NULL DEFAULT "0" COMMENT '交易次数',
  `trader_pnl` decimal(38,18) NULL DEFAULT "0" COMMENT '交易者盈亏',
  `trader_roi` decimal(10,4) NULL DEFAULT "0" COMMENT '交易者ROI百分比',
  `transfer_count` bigint NULL DEFAULT "0" COMMENT '转账次数',
  `transfer_in_count` bigint NULL DEFAULT "0" COMMENT '转入次数',
  `transfer_out_count` bigint NULL DEFAULT "0" COMMENT '转出次数',
  `volume` decimal(38,18) NULL COMMENT '交易量'
) ENGINE=OLAP
UNIQUE KEY(`token_address`)
DISTRIBUTED BY HASH(`token_address`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "token_address",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);

CREATE TABLE `token_profit_history` (
    `id` bigint NOT NULL,
    `block_time` datetime NOT NULL COMMENT '交易时间',
    `account` varchar(255) NULL,
    `token_address` varchar(255) NULL,
    `type` int NULL,
    `amount` decimal(38,18) NULL,
    `roi` decimal(10,4) NULL,
    `realized_roi`  decimal(10,4) NULL,
    `unrealized_roi`  decimal(10,4) NULL,
    `total_amount` decimal(38,18) NULL,
    `total_cost` decimal(38,18) NULL,
    `historical_holding_avg_price` decimal(38,18) NULL,
    `holding_avg_price` decimal(38,18) NULL,
    `realized_profit` decimal(38,18) NULL,
    `unrealized_profit` decimal(38,18) NULL,
    `current_price` decimal(38,18) NULL,
    `historical_sold_avg_price` decimal(38,18) NULL,
    `sold_amount` decimal(38,18) NULL,
    `bought_amount` decimal(38,18) NULL,
    `is_win` int NULL,
    `sold_count` bigint NULL,
    `sold_count_bought_by_user` bigint NULL,
    `sold_count_win` bigint NULL,
    `bought_count` bigint NULL,
    `transfer_in_count` bigint NULL,
    `transfer_out_count` bigint NULL,
    `transfer_in_amount` decimal(38,18) NULL,
    `transfer_out_amount` decimal(38,18) NULL,
    `total_bought_amount_has_been_left` decimal(38,18) NULL,
    `total_bought_amount_has_been_sold` decimal(38,18) NULL,
    `block_height` bigint NULL,
    `tx_hash` varchar(255) NULL
) ENGINE=OLAP
UNIQUE KEY(`id`, `block_time`)
AUTO PARTITION BY RANGE (date_trunc(`block_time`, 'day'))
()
DISTRIBUTED BY HASH(`id`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);

CREATE TABLE `token_profit_history_last` (
  `id` varchar(255) NULL COMMENT '唯一',
  `account` varchar(255) NULL COMMENT '交易账户',
  `token_address` varchar(255) NULL COMMENT '代币地址',
  `amount` decimal(38,18) NULL COMMENT '金额',
  `bought_amount` decimal(38,18) NULL COMMENT '买入金额',
  `bought_count` bigint NULL COMMENT '买入次数',
  `current_price` decimal(38,18) NULL COMMENT '当前价格',
  `historical_holding_avg_price` decimal(38,18) NULL COMMENT '持有均价',
  `holding_avg_price` decimal(38,18) NULL COMMENT '持有均价',
  `is_win` int NULL COMMENT '是否盈利',
  `realized_profit` decimal(38,18) NULL COMMENT '已实现利润',
  `roi` decimal(10,4) NULL COMMENT '投资回报率',
  `realized_roi`  decimal(10,4) NULL,
  `unrealized_roi`  decimal(10,4) NULL,
  `sold_amount` decimal(38,18) NULL COMMENT '卖出金额',
  `historical_sold_avg_price` decimal(38,18) NULL COMMENT '卖出均价',
  `sold_count` bigint NULL COMMENT '卖出次数',
  `sold_count_bought_by_user` bigint NULL,
  `sold_count_win` bigint NULL COMMENT '盈利卖出次数',
  `total_amount` decimal(38,18) NULL COMMENT '总金额',
  `total_cost` decimal(38,18) NULL COMMENT '总成本',
  `transfer_in_amount` decimal(38,18) NULL COMMENT '转入金额',
  `transfer_in_count` bigint NULL COMMENT '转入次数',
  `transfer_out_amount` decimal(38,18) NULL COMMENT '转出金额',
  `transfer_out_count` bigint NULL COMMENT '转出次数',
  `type` int NULL COMMENT '类型',
  `unrealized_profit` decimal(38,18) NULL COMMENT '未实现利润',
  `total_bought_amount_has_been_left` decimal(38,18) NULL,
  `block_height` bigint NULL COMMENT '区块高度',
  `block_time` datetime NULL COMMENT '区块时间',
  `tx_hash` varchar(255) NULL COMMENT '交易哈希'
) ENGINE=OLAP
UNIQUE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS AUTO
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);

CREATE TABLE `profit_cal_record` (
    `id` BIGINT  AUTO_INCREMENT COMMENT '唯一标识符',
    `block_height` bigint DEFAULT '0',
    `offset` bigint DEFAULT '0',
    `block_time` DATETIME  COMMENT '区块时间'
    )
UNIQUE KEY(id)
DISTRIBUTED BY HASH(`id`) BUCKETS AUTO
PROPERTIES (
    "replication_num" = "1"
);


CREATE TABLE `vault_tokens` (
    `vault_token_address` varchar(255),
    `mint_token_address` varchar(255),
    `mint_token_decimals` int
) 
UNIQUE KEY(`vault_token_address`)
DISTRIBUTED BY HASH(`vault_token_address`) BUCKETS AUTO
PROPERTIES (
    "replication_num" = "1"
);


CREATE TABLE `asset_info` (
    `address` varchar(255) ,
    `symbol` varchar(255) DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    `decimals` int ,
    `logo_uri` text DEFAULT NULL,
    `total_supply` varchar(250) DEFAULT NULL,
    `disabled` tinyint  DEFAULT '0',
    `metadata_account` varchar(255) DEFAULT NULL,
    `asset_created_at` DATETIME
) 
UNIQUE KEY(address)
DISTRIBUTED BY HASH(`address`) BUCKETS AUTO
PROPERTIES (
    "replication_num" = "1"
);

CREATE TABLE `raydium_swap_pools` (
    `pair_address` varchar(255) ,
    `base_token` varchar(255) DEFAULT NULL,
    `quote_token` varchar(255) DEFAULT NULL,
    `base_vault` varchar(255) DEFAULT NULL,
    `quote_vault` varchar(255) DEFAULT NULL,
    `program_id` varchar(255) ,
    `type` varchar(255) ,
    `base_token_amount` varchar(255) DEFAULT NULL,
    `quote_token_amount` varchar(255) DEFAULT NULL
) 
UNIQUE KEY(pair_address)
DISTRIBUTED BY HASH(`pair_address`) BUCKETS AUTO
PROPERTIES (
    "replication_num" = "1"
);