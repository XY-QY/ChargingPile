package com.chargingpile.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chargingpile.entity.CpUser;
import com.chargingpile.entity.WalletTransaction;
import com.chargingpile.mapper.CpUserMapper;
import com.chargingpile.mapper.WalletTransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    @Autowired
    private CpUserMapper userMapper;

    @Autowired
    private WalletTransactionMapper walletMapper;

    /** 钱包充值 */
    @Transactional
    public String recharge(Integer userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "充值金额必须大于0";
        }
        CpUser user = userMapper.selectById(userId);
        BigDecimal newBalance = user.getBalance().add(amount);
        user.setBalance(newBalance);
        userMapper.updateById(user);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType("RECHARGE");
        tx.setAmount(amount);
        tx.setBalanceAfter(newBalance);
        tx.setDescription("钱包充值");
        walletMapper.insert(tx);
        return null;
    }

    /** 钱包流水 */
    public List<WalletTransaction> transactions(Integer userId) {
        QueryWrapper<WalletTransaction> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("create_time");
        return walletMapper.selectList(qw);
    }
}
