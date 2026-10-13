package com.chargingpile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cp_user")
public class CpUser {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String role;
    private String nickname;
    private String phone;
    private BigDecimal balance;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalDateTime createTime;
}
