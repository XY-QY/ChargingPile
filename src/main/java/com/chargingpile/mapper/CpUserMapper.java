package com.chargingpile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargingpile.entity.CpUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CpUserMapper extends BaseMapper<CpUser> {
}
