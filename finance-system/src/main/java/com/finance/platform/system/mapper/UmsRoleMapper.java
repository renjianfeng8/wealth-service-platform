package com.finance.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.platform.system.entity.UmsRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台角色表数据访问层。
 */
@Mapper
public interface UmsRoleMapper extends BaseMapper<UmsRole> {
}

