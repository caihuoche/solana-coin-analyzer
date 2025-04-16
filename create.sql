# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20033
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 127.0.0.1 (MySQL 8.0.28)
# 数据库: solana_0xsniper
# 生成时间: 2024-08-18 06:54:44 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# 转储表 address_profit
# ------------------------------------------------------------

DROP TABLE IF EXISTS `address_profit`;

CREATE TABLE `address_profit` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `asset_symbol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `asset_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `balance` decimal(30,10) NOT NULL COMMENT '总数量',
    `total_profit` decimal(30,10) NOT NULL COMMENT '利润',
    `roi` decimal(30,10) NOT NULL,
    `win_ratio` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '胜率',
    `block_height` int NOT NULL COMMENT '高度',
    `block_time` timestamp NOT NULL COMMENT '高度时间',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_address_asset_symbol` (`address`,`asset_symbol`,`asset_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户最新盈利';


# 转储表 coin_prices
# ------------------------------------------------------------

DROP TABLE IF EXISTS `coin_prices`;

CREATE TABLE `coin_prices` (
    `id` int NOT NULL AUTO_INCREMENT,
    `asset_symbol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `asset_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `asset_type` int DEFAULT NULL COMMENT '1主币 2token',
    `closing_price` varchar(255) NOT NULL,
    `price_date` date NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='币价';



# 转储表 coin_prices_cal_record
# ------------------------------------------------------------

DROP TABLE IF EXISTS `coin_prices_cal_record`;

CREATE TABLE `coin_prices_cal_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `asset_symbol` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `block_height` bigint DEFAULT '0',
    `offset` bigint DEFAULT '0',
    `token0_balance` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0',
    `token1_balance` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0',
    `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='币价计算记录';



# 转储表 profit_cal_record
# ------------------------------------------------------------

DROP TABLE IF EXISTS `profit_cal_record`;

CREATE TABLE `profit_cal_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `block_height` bigint DEFAULT '0',
    `offset` bigint DEFAULT '0',
    `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='程序计算';



# 转储表 profit_history
# ------------------------------------------------------------

DROP TABLE IF EXISTS `profit_history`;

CREATE TABLE `profit_history` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `type` int NOT NULL COMMENT '1买入2卖出3充值',
    `balance` decimal(30,10) NOT NULL COMMENT '当前交易的数量',
    `total_cost` decimal(30,10) NOT NULL COMMENT '总成本',
    `total_balance` decimal(30,10) NOT NULL COMMENT '总数量',
    `holding_avg_price` decimal(30,10) NOT NULL COMMENT '持仓成本',
    `realized_profit` decimal(30,10) NOT NULL COMMENT '已实现利润',
    `unrealized_profit` decimal(30,10) NOT NULL COMMENT '未实现利润',
    `is_win` int unsigned DEFAULT NULL,
    `current_price` decimal(30,10) NOT NULL COMMENT '卖出成本',
    `tx_time` timestamp NOT NULL COMMENT '交易时间',
    `tx_block` int NOT NULL COMMENT '交易区块',
    `tx_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `roi` decimal(30,10) NOT NULL,
    `sell_count` int NOT NULL COMMENT '卖出次数',
    `sell_count_win` int NOT NULL COMMENT '卖出胜利次数',
    `buy_count` int NOT NULL COMMENT '买入次数',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主币历史盈利';



# 转储表 token_balance_history
# ------------------------------------------------------------

DROP TABLE IF EXISTS `token_balance_history`;

CREATE TABLE `token_balance_history` (
    `id` int NOT NULL AUTO_INCREMENT,
    `lp_token_address` varchar(255) NOT NULL,
    `block_height` int NOT NULL,
    `block_time` timestamp NULL DEFAULT NULL,
    `token0_balance` varchar(255) NOT NULL,
    `token1_balance` varchar(255) NOT NULL,
    `token0_address` varchar(255) NOT NULL,
    `token1_address` varchar(255) NOT NULL,
    `tx_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `created_at` datetime NOT NULL,
    `updated_at` datetime NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 token_profit_history
# ------------------------------------------------------------

DROP TABLE IF EXISTS `token_profit_history`;

CREATE TABLE `token_profit_history` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `asset_symbol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `asset_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `type` int NOT NULL COMMENT '1买入2卖出3充值',
    `balance` decimal(30,10) NOT NULL COMMENT '当前交易的数量',
    `total_cost` decimal(30,10) NOT NULL COMMENT '总成本',
    `total_balance` decimal(30,10) NOT NULL COMMENT '总数量',
    `holding_avg_price` decimal(30,10) NOT NULL COMMENT '持仓成本',
    `realized_profit` decimal(30,10) NOT NULL COMMENT '已实现利润',
    `unrealized_profit` decimal(30,10) NOT NULL COMMENT '未实现利润',
    `is_win` int unsigned DEFAULT NULL,
    `current_price` decimal(30,10) NOT NULL COMMENT '卖出成本',
    `tx_time` timestamp NOT NULL COMMENT '交易时间',
    `tx_block` int NOT NULL COMMENT '交易区块',
    `tx_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `roi` decimal(30,10) NOT NULL,
    `sell_count` int NOT NULL COMMENT '卖出次数',
    `sell_count_win` int NOT NULL COMMENT '卖出胜利次数',
    `buy_count` int NOT NULL COMMENT '买入次数',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主币历史盈利';

DROP TABLE IF EXISTS `vault_tokens`;
CREATE TABLE `vault_tokens` (
    `vault_token_address` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `mint_token_address` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `mint_token_decimals` INT NOT NULL,
     PRIMARY KEY (`vault_token_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='vault mint token关系';

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
