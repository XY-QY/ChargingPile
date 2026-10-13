package com.chargingpile.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chargingpile.entity.CpUser;
import com.chargingpile.mapper.CpUserMapper;
import com.chargingpile.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuthService {

    @Autowired
    private CpUserMapper userMapper;

    public String register(String username, String password, String nickname, String phone) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return "用户名和密码不能为空";
        }
        Long count = userMapper.selectCount(new QueryWrapper<CpUser>().eq("username", username.trim()));
        if (count > 0) {
            return "用户名已存在";
        }
        CpUser user = new CpUser();
        user.setUsername(username.trim());
        user.setPassword(Md5Util.md5(password));
        user.setRole("USER");
        user.setNickname(nickname == null || nickname.isEmpty() ? username.trim() : nickname);
        user.setPhone(phone);
        user.setBalance(new BigDecimal("0.00"));
        userMapper.insert(user);
        return null;
    }

    public CpUser login(String username, String password) {
        CpUser user = userMapper.selectOne(new QueryWrapper<CpUser>().eq("username", username.trim()));
        if (user == null) {
            return null;
        }
        if (!user.getPassword().equals(Md5Util.md5(password))) {
            return null;
        }
        return user;
    }

    public CpUser getById(Integer id) {
        return userMapper.selectById(id);
    }
}
