package com.wealth.platform.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.message.entity.WeaNews;
import org.apache.ibatis.annotations.Mapper;

/**
 * 财经资讯公告表数据访问层
 */
@Mapper
public interface NewsMapper extends BaseMapper<WeaNews> {
}

