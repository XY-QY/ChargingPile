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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/** 管理员统计数据服务 */
@Service
public class AdminService {

    @Autowired
    private ChargingPileMapper pileMapper;

    @Autowired
    private ChargingSessionMapper sessionMapper;

    @Autowired
    private CpUserMapper userMapper;

    @Autowired
    private WalletTransactionMapper walletMapper;

    public Map<String, Object> stats() {
        Map<String, Object> result = new HashMap<>();

        // 基础指标
        long totalPiles = pileMapper.selectCount(null);
        long idlePiles = pileMapper.selectCount(new QueryWrapper<ChargingPile>().eq("status", 0));
        long usingPiles = totalPiles - idlePiles;
        long totalUsers = userMapper.selectCount(new QueryWrapper<CpUser>().eq("role", "USER"));
        long totalSessions = sessionMapper.selectCount(null);
        long todaySessions = sessionMapper.selectCount(
                new QueryWrapper<ChargingSession>().apply("start_time >= CURRENT_DATE"));
        List<ChargingSession> finished = sessionMapper.selectList(
                new QueryWrapper<ChargingSession>().eq("status", "FINISHED"));
        BigDecimal totalIncome = finished.stream()
                .map(ChargingSession::getAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalKwh = finished.stream()
                .map(ChargingSession::getKwh).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("totalPiles", totalPiles);
        result.put("idlePiles", idlePiles);
        result.put("usingPiles", usingPiles);
        result.put("totalUsers", totalUsers);
        result.put("totalSessions", totalSessions);
        result.put("todaySessions", todaySessions);
        result.put("totalIncome", totalIncome);
        result.put("totalKwh", totalKwh);

        // 区域分布统计
        List<ChargingPile> piles = pileMapper.selectList(null);
        Map<String, Long> areaTotal = piles.stream()
                .collect(Collectors.groupingBy(ChargingPile::getArea, Collectors.counting()));
        Map<String, Long> areaUsing = piles.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .collect(Collectors.groupingBy(ChargingPile::getArea, Collectors.counting()));
        // 区域收入
        Map<String, BigDecimal> areaIncome = new HashMap<>();
        for (ChargingSession s : finished) {
            ChargingPile pile = pileMapper.selectById(s.getPileId());
            if (pile == null) continue;
            areaIncome.merge(pile.getArea(), s.getAmount() == null ? BigDecimal.ZERO : s.getAmount(), BigDecimal::add);
        }
        List<Map<String, Object>> areaStats = new ArrayList<>();
        for (String area : new String[]{"柳园", "荷园", "菊园", "松园"}) {
            Map<String, Object> m = new HashMap<>();
            m.put("area", area);
            m.put("total", areaTotal.getOrDefault(area, 0L));
            m.put("using", areaUsing.getOrDefault(area, 0L));
            m.put("idle", areaTotal.getOrDefault(area, 0L) - areaUsing.getOrDefault(area, 0L));
            m.put("income", areaIncome.getOrDefault(area, BigDecimal.ZERO));
            areaStats.add(m);
        }
        result.put("areaStats", areaStats);

        // 近7天每日收入
        List<Map<String, Object>> dailyIncome = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            List<ChargingSession> daySessions = sessionMapper.selectList(new QueryWrapper<ChargingSession>()
                    .eq("status", "FINISHED")
                    .apply("actual_end_time >= '" + day.atStartOfDay() + "' AND actual_end_time < '" + day.plusDays(1).atStartOfDay() + "'"));
            BigDecimal income = daySessions.stream().map(ChargingSession::getAmount).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> m = new HashMap<>();
            m.put("date", day.format(fmt));
            m.put("income", income);
            dailyIncome.add(m);
        }
        result.put("dailyIncome", dailyIncome);

        return result;
    }

    /** 全部用户列表（隐藏密码） */
    public List<Map<String, Object>> allUsers() {
        List<CpUser> users = userMapper.selectList(new QueryWrapper<CpUser>().orderByDesc("id"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (CpUser u : users) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("phone", u.getPhone());
            m.put("role", u.getRole());
            m.put("balance", u.getBalance());
            m.put("createTime", u.getCreateTime());
            result.add(m);
        }
        return result;
    }

    /** 全部充电订单 */
    public List<Map<String, Object>> allSessions() {
        List<ChargingSession> sessions = sessionMapper.selectList(new QueryWrapper<ChargingSession>().orderByDesc("start_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChargingSession s : sessions) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("pileId", s.getPileId());
            ChargingPile pile = pileMapper.selectById(s.getPileId());
            m.put("pileName", pile == null ? "未知" : pile.getName());
            m.put("area", pile == null ? "" : pile.getArea());
            CpUser user = userMapper.selectById(s.getUserId());
            m.put("username", user == null ? "未知" : user.getUsername());
            m.put("startTime", s.getStartTime());
            m.put("actualEndTime", s.getActualEndTime());
            m.put("status", s.getStatus());
            m.put("durationHours", s.getDurationHours());
            m.put("kwh", s.getKwh());
            m.put("amount", s.getAmount());
            result.add(m);
        }
        return result;
    }

    /** 全部钱包流水 */
    public List<Map<String, Object>> allTransactions() {
        List<WalletTransaction> txs = walletMapper.selectList(new QueryWrapper<WalletTransaction>().orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (WalletTransaction t : txs) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            CpUser user = userMapper.selectById(t.getUserId());
            m.put("username", user == null ? "未知" : user.getUsername());
            m.put("type", t.getType());
            m.put("amount", t.getAmount());
            m.put("balanceAfter", t.getBalanceAfter());
            m.put("description", t.getDescription());
            m.put("createTime", t.getCreateTime());
            result.add(m);
        }
        return result;
    }
}
