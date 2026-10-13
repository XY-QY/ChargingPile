package com.chargingpile.controller;

import com.chargingpile.entity.CpUser;
import com.chargingpile.service.AdminService;
import com.chargingpile.service.AuthService;
import com.chargingpile.util.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthService authService;

    /** 管理员身份校验 */
    private boolean isAdmin(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return false;
        CpUser user = authService.getById(userId);
        return user != null && "ADMIN".equals(user.getRole());
    }

    /** 数据总览统计 */
    @GetMapping("/stats")
    public Result<?> stats(HttpSession session) {
        if (!isAdmin(session)) return Result.fail("无权限，仅管理员可访问");
        return Result.ok(adminService.stats());
    }

    /** 用户列表 */
    @GetMapping("/users")
    public Result<?> users(HttpSession session) {
        if (!isAdmin(session)) return Result.fail("无权限，仅管理员可访问");
        return Result.ok(adminService.allUsers());
    }

    /** 充电订单列表 */
    @GetMapping("/sessions")
    public Result<?> sessions(HttpSession session) {
        if (!isAdmin(session)) return Result.fail("无权限，仅管理员可访问");
        return Result.ok(adminService.allSessions());
    }

    /** 钱包流水列表 */
    @GetMapping("/transactions")
    public Result<?> transactions(HttpSession session) {
        if (!isAdmin(session)) return Result.fail("无权限，仅管理员可访问");
        return Result.ok(adminService.allTransactions());
    }
}
