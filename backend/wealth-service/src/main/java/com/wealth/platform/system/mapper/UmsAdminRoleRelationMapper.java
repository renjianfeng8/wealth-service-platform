package com.wealth.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台用户和角色关系表数据访问层。
 */
@Mapper
public interface UmsAdminRoleRelationMapper extends BaseMapper<UmsAdminRoleRelation> {
}

