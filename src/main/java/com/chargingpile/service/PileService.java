package com.chargingpile.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chargingpile.entity.ChargingPile;
import com.chargingpile.entity.ChargingSession;
import com.chargingpile.mapper.ChargingPileMapper;
import com.chargingpile.mapper.ChargingSessionMapper;
import com.chargingpile.vo.PileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PileService {

    @Autowired
    private ChargingPileMapper pileMapper;

    @Autowired
    private ChargingSessionMapper sessionMapper;

    /**
     * 查询充电桩列表
     * @param area 区域筛选(可为空)
     * @param lat 当前纬度(可为空)
     * @param lng 当前经度(可为空)
     */
    public List<PileVO> listPiles(String area, BigDecimal lat, BigDecimal lng) {
        QueryWrapper<ChargingPile> qw = new QueryWrapper<>();
        if (area != null && !area.trim().isEmpty()) {
            qw.eq("area", area.trim());
        }
        List<ChargingPile> piles = pileMapper.selectList(qw);

        List<PileVO> vos = new ArrayList<>();
        for (ChargingPile p : piles) {
            PileVO vo = new PileVO();
            vo.setId(p.getId());
            vo.setName(p.getName());
            vo.setArea(p.getArea());
            vo.setAddress(p.getAddress());
            vo.setStatus(p.getStatus());
            vo.setPower(p.getPower());
            vo.setPrice(p.getPrice());
            vo.setLongitude(p.getLongitude());
            vo.setLatitude(p.getLatitude());
            // 使用中 -> 查当前会话剩余分钟
            if (p.getStatus() != null && p.getStatus() == 1) {
                QueryWrapper<ChargingSession> qw2 = new QueryWrapper<>();
                qw2.eq("pile_id", p.getId()).eq("status", "CHARGING").last("limit 1");
                ChargingSession s = sessionMapper.selectOne(qw2);
                if (s != null && s.getEndTime() != null) {
                    long remain = ChronoUnit.MINUTES.between(LocalDateTime.now(), s.getEndTime());
                    vo.setRemainMinutes(Math.max(remain, 0));
                }
            }
            vos.add(vo);
        }

        // 定位：按距离排序
        if (lat != null && lng != null) {
            final BigDecimal userLat = lat;
            final BigDecimal userLng = lng;
            for (PileVO vo : vos) {
                double dist = distanceKm(userLat.doubleValue(), userLng.doubleValue(),
                        vo.getLatitude().doubleValue(), vo.getLongitude().doubleValue());
                vo.setDistanceKm(Math.round(dist * 1000) / 1000.0);
            }
            vos.sort(Comparator.comparingDouble(PileVO::getDistanceKm));
        }
        return vos;
    }

    /** 计算两个经纬度点之间的距离(公里) Haversine 公式 */
    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        return s * 6371.0;
    }
}
