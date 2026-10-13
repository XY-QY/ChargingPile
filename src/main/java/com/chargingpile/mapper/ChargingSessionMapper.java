package com.chargingpile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargingpile.entity.ChargingSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChargingSessionMapper extends BaseMapper<ChargingSession> {
}
