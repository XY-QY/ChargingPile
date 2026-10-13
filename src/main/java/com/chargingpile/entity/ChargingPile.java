package com.chargingpile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("charging_pile")
public class ChargingPile {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String area;
    private String address;
    /** 0=空闲 1=使用中 */
    private Integer status;
    /** 功率 kW */
    private BigDecimal power;
    /** 每度电价(元) */
    private BigDecimal price;
    private BigDecimal longitude;
    private BigDecimal latitude;
}
