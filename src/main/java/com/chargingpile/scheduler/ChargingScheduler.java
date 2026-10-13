package com.chargingpile.scheduler;

import com.chargingpile.service.ChargingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 每分钟自动结算已到结束时间的充电订单 */
@Component
public class ChargingScheduler {

    @Autowired
    private ChargingService chargingService;

    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void settleOverdueSessions() {
        try {
            int count = chargingService.autoSettleOverdue();
            if (count > 0) {
                System.out.println("[" + LocalDateTime.now() + "] 定时结算完成，共结算 " + count + " 笔充电订单");
            }
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] 定时结算异常: " + e.getMessage());
        }
    }
}
