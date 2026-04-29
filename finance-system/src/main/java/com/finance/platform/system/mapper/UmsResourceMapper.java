package com.finance.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.platform.system.entity.UmsResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台资源表数据访问层。
 */
@Mapper
public interface UmsResourceMapper extends BaseMapper<UmsResource> {
}

