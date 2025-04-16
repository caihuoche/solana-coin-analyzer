package com.creda.coin.price.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 程序计算
 * </p>
 *
 * @author gavin
 * @since 2024-08-10
 */
@TableName("profit_cal_record")
@Data
public class ProfitCalRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long blockHeight;
    private Long offset;
    private Date blockTime;


}
