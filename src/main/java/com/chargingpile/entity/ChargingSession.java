package com.chargingpile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charging_session")
public class ChargingSession {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer pileId;
    /** 开始充电时间 */
    private LocalDateTime startTime;
    /** 预计结束时间 */
    private LocalDateTime endTime;
    /** 实际结束时间 */
    private LocalDateTime actualEndTime;
    /** CHARGING/FINISHED/CANCELLED */
    private String status;
    /** 计费时长(小时,向上取整) */
    private BigDecimal durationHours;
    /** 充电度数 */
    private BigDecimal kwh;
    /** 费用(元) */
    private BigDecimal amount;
    private LocalDateTime createTime;
}
