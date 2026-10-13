package com.chargingpile.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 充电桩视图对象：桩信息 + 剩余充电时长 + 距离 */
@Data
public class PileVO {
    private Integer id;
    private String name;
    private String area;
    private String address;
    /** 0=空闲 1=使用中 */
    private Integer status;
    private BigDecimal power;
    private BigDecimal price;
    private BigDecimal longitude;
    private BigDecimal latitude;
    /** 使用中：剩余分钟数 */
    private Long remainMinutes;
    /** 与用户距离(公里)，无定位时为 null */
    private Double distanceKm;
}
