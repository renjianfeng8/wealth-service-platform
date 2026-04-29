package com.finance.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.platform.system.entity.UmsAdminRoleRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台用户和角色关系表数据访问层。
 */
@Mapper
public interface UmsAdminRoleRelationMapper extends BaseMapper<UmsAdminRoleRelation> {
}

