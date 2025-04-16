package com.creda.coin.price.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

/**
 *
 * @author gavin
 * @date 2024/08/03
 **/
@Data
public class TransactionCondition {
	private Long lastId;
	private Date blockTime;
	private LocalDate startDate;
	private LocalDate endDate;

}
