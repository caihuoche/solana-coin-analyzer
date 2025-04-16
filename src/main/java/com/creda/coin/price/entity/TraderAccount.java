package com.creda.coin.price.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("trader_account")
@Data
public class TraderAccount {
    private String account; // 用户账户
    private Date lastTraderTime; // 最近交易时间
    private Date last1DayHandlerTime; // 最近1天处理时间
    private Date last7DayHandlerTime; // 最近7天处理时间

    // Getter and Setter methods

}
