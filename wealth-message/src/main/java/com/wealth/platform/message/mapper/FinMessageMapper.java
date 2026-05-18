package com.wealth.platform.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.message.entity.WeaMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内消息推送表数据访问层
 */
@Mapper
public interface FinMessageMapper extends BaseMapper<WeaMessage> {
}

