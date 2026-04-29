package com.finance.platform.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.platform.message.entity.FinNews;
import org.apache.ibatis.annotations.Mapper;

/**
 * 财经资讯公告表数据访问层。
 */
@Mapper
public interface FinNewsMapper extends BaseMapper<FinNews> {
}

