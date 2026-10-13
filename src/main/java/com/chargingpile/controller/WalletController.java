package com.chargingpile.controller;

import com.chargingpile.service.WalletService;
import com.chargingpile.util.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /** 充值 */
    @PostMapping("/recharge")
    public Result<?> recharge(@RequestBody Map<String, Object> body, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String error = walletService.recharge(userId, amount);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok("充值成功", null);
    }

    /** 钱包流水 */
    @GetMapping("/transactions")
    public Result<?> transactions(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        return Result.ok(walletService.transactions(userId));
    }
}
