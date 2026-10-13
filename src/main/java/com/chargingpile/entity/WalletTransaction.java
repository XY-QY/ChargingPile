package com.chargingpile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_transaction")
public class WalletTransaction {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    /** RECHARGE/CHARGE/REFUND */
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private LocalDateTime createTime;
}
