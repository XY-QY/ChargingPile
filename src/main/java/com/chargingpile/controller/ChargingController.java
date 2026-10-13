package com.chargingpile.controller;

import com.chargingpile.service.ChargingService;
import com.chargingpile.util.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/charge")
public class ChargingController {

    @Autowired
    private ChargingService chargingService;

    /** 开始充电 */
    @PostMapping("/start")
    public Result<?> start(@RequestBody Map<String, Object> body, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        Integer pileId = (Integer) body.get("pileId");
        Integer minutes = (Integer) body.get("minutes");
        String error = chargingService.startCharging(userId, pileId, minutes);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok("开始充电成功", null);
    }

    /** 手动结束充电 */
    @PostMapping("/stop")
    public Result<?> stop(@RequestBody Map<String, Object> body, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        Integer sessionId = (Integer) body.get("sessionId");
        String error = chargingService.stopCharging(userId, sessionId);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok("充电结束，费用已结算", null);
    }

    /** 我的充电记录 */
    @GetMapping("/my")
    public Result<?> my(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        return Result.ok(chargingService.mySessions(userId));
    }

    /** 当前充电中订单 */
    @GetMapping("/current")
    public Result<?> current(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        return Result.ok(chargingService.currentSession(userId));
    }
}
