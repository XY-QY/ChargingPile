package com.chargingpile.controller;

import com.chargingpile.service.PileService;
import com.chargingpile.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/piles")
public class PileController {

    @Autowired
    private PileService pileService;

    /**
     * 充电桩列表
     * @param area 区域筛选(柳园/荷园/菊园/松园，可选)
     * @param lat 当前纬度(定位，可选)
     * @param lng 当前经度(定位，可选)
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) String area,
                          @RequestParam(required = false) BigDecimal lat,
                          @RequestParam(required = false) BigDecimal lng) {
        return Result.ok(pileService.listPiles(area, lat, lng));
    }

    /** 区域列表 */
    @GetMapping("/areas")
    public Result<?> areas() {
        return Result.ok(List.of("柳园", "荷园", "菊园", "松园"));
    }
}
