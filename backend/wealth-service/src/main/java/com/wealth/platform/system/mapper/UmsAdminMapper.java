package com.wealth.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.system.entity.UmsAdmin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理员表数据访问层。
 */
@Mapper
public interface UmsAdminMapper extends BaseMapper<UmsAdmin> {
}

