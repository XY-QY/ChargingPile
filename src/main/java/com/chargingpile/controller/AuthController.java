package com.chargingpile.controller;

import com.chargingpile.entity.CpUser;
import com.chargingpile.service.AuthService;
import com.chargingpile.util.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> body) {
        String error = authService.register(body.get("username"), body.get("password"),
                body.get("nickname"), body.get("phone"));
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok("注册成功", null);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        CpUser user = authService.login(body.get("username"), body.get("password"));
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }
        session.setAttribute("userId", user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        return Result.ok("登录成功", data);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        session.invalidate();
        return Result.ok("已退出登录", null);
    }

    @GetMapping("/me")
    public Result<?> me(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        CpUser user = authService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        data.put("balance", user.getBalance());
        data.put("phone", user.getPhone());
        return Result.ok(data);
    }
}
