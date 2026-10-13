package com.chargingpile.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chargingpile.entity.ChargingPile;
import com.chargingpile.entity.ChargingSession;
import com.chargingpile.entity.CpUser;
import com.chargingpile.entity.WalletTransaction;
import com.chargingpile.mapper.ChargingPileMapper;
import com.chargingpile.mapper.ChargingSessionMapper;
import com.chargingpile.mapper.CpUserMapper;
import com.chargingpile.mapper.WalletTransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChargingService {

    @Autowired
    private ChargingPileMapper pileMapper;

    @Autowired
    private ChargingSessionMapper sessionMapper;

    @Autowired
    private CpUserMapper userMapper;

    @Autowired
    private WalletTransactionMapper walletMapper;

    /**
     * 开始充电
     * 校验：桩空闲、余额足够支付"预估费用"
     */
    @Transactional
    public String startCharging(Integer userId, Integer pileId, Integer minutes) {
        if (pileId == null || minutes == null || minutes <= 0) {
            return "请选择充电桩并设置充电时长";
        }
        ChargingPile pile = pileMapper.selectById(pileId);
        if (pile == null) {
            return "充电桩不存在";
        }
        if (pile.getStatus() != null && pile.getStatus() == 1) {
            return "该充电桩正在使用中，请选择其他空闲桩";
        }
        CpUser user = userMapper.selectById(userId);
        if (user == null) {
            return "用户不存在";
        }
        // 预估费用 = 向上取整小时数 × 功率 × 单价
        BigDecimal hours = BigDecimal.valueOf(Math.ceil(minutes / 60.0));
        BigDecimal estAmount = hours.multiply(pile.getPower()).multiply(pile.getPrice())
                .setScale(2, RoundingMode.HALF_UP);
        if (user.getBalance().compareTo(estAmount) < 0) {
            return "余额不足，预估本次充电费用 " + estAmount + " 元，请先充值（当前余额 " + user.getBalance() + " 元）";
        }

        // 创建会话
        ChargingSession session = new ChargingSession();
        session.setUserId(userId);
        session.setPileId(pileId);
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now().plusMinutes(minutes));
        session.setStatus("CHARGING");
        sessionMapper.insert(session);

        // 桩标记使用中
        pile.setStatus(1);
        pileMapper.updateById(pile);
        return null;
    }

    /**
     * 手动结束充电（结算）
     */
    @Transactional
    public String stopCharging(Integer userId, Integer sessionId) {
        ChargingSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return "充电订单不存在";
        }
        if (!session.getUserId().equals(userId)) {
            return "无权操作该订单";
        }
        if (!"CHARGING".equals(session.getStatus())) {
            return "该订单已结束";
        }
        return settle(session);
    }

    /**
     * 定时任务：自动结算所有已到结束时间的充电订单
     */
    @Transactional
    public int autoSettleOverdue() {
        QueryWrapper<ChargingSession> qw = new QueryWrapper<>();
        qw.eq("status", "CHARGING").lt("end_time", LocalDateTime.now());
        List<ChargingSession> list = sessionMapper.selectList(qw);
        for (ChargingSession s : list) {
            settle(s);
        }
        return list.size();
    }

    /**
     * 结算核心逻辑：计算费用 -> 扣费 -> 释放充电桩 -> 更新订单
     */
    private String settle(ChargingSession session) {
        ChargingPile pile = pileMapper.selectById(session.getPileId());
        CpUser user = userMapper.selectById(session.getUserId());

        LocalDateTime now = LocalDateTime.now();
        long minutes = Math.max(ChronoUnit.MINUTES.between(session.getStartTime(), now), 1);
        // 计费规则：充电时长不满整数按向上取整（不足1小时按1小时）
        BigDecimal hours = BigDecimal.valueOf(Math.ceil(minutes / 60.0));
        BigDecimal kwh = hours.multiply(pile.getPower()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = kwh.multiply(pile.getPrice()).setScale(2, RoundingMode.HALF_UP);

        // 扣费
        BigDecimal newBalance = user.getBalance().subtract(amount);
        user.setBalance(newBalance);
        userMapper.updateById(user);

        // 钱包流水
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(user.getId());
        tx.setType("CHARGE");
        tx.setAmount(amount.negate());
        tx.setBalanceAfter(newBalance);
        tx.setDescription("充电消费：" + pile.getName() + "，" + kwh + " 度 × " + pile.getPrice() + " 元/度");
        walletMapper.insert(tx);

        // 更新订单
        session.setActualEndTime(now);
        session.setStatus("FINISHED");
        session.setDurationHours(hours);
        session.setKwh(kwh);
        session.setAmount(amount);
        sessionMapper.updateById(session);

        // 释放充电桩
        pile.setStatus(0);
        pileMapper.updateById(pile);
        return null;
    }

    /** 我的充电记录（关联桩名） */
    public List<Map<String, Object>> mySessions(Integer userId) {
        QueryWrapper<ChargingSession> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("start_time");
        List<ChargingSession> list = sessionMapper.selectList(qw);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (ChargingSession s : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("pileId", s.getPileId());
            ChargingPile pile = pileMapper.selectById(s.getPileId());
            m.put("pileName", pile == null ? "未知" : pile.getName());
            m.put("area", pile == null ? "" : pile.getArea());
            m.put("startTime", s.getStartTime());
            m.put("endTime", s.getEndTime());
            m.put("actualEndTime", s.getActualEndTime());
            m.put("status", s.getStatus());
            m.put("durationHours", s.getDurationHours());
            m.put("kwh", s.getKwh());
            m.put("amount", s.getAmount());
            result.add(m);
        }
        return result;
    }

    /** 当前正在充电中的订单（含桩信息与剩余分钟） */
    public Map<String, Object> currentSession(Integer userId) {
        QueryWrapper<ChargingSession> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("status", "CHARGING").orderByDesc("start_time").last("limit 1");
        ChargingSession s = sessionMapper.selectOne(qw);
        if (s == null) {
            return null;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId());
        m.put("pileId", s.getPileId());
        ChargingPile pile = pileMapper.selectById(s.getPileId());
        m.put("pileName", pile == null ? "未知" : pile.getName());
        m.put("area", pile == null ? "" : pile.getArea());
        m.put("power", pile == null ? "" : pile.getPower());
        m.put("price", pile == null ? "" : pile.getPrice());
        m.put("startTime", s.getStartTime());
        m.put("endTime", s.getEndTime());
        m.put("remainMinutes", Math.max(ChronoUnit.MINUTES.between(LocalDateTime.now(), s.getEndTime()), 0));
        return m;
    }
}
