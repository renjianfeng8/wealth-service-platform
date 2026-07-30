package com.wealth.platform.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.product.entity.WeaUserFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 用户自选关注表数据访问层（从 wealth-account 迁移合并）
 */
public interface UserFavoriteMapper extends BaseMapper<WeaUserFavorite> {

    /**
     * 物理删除（wea_user_favorite 表无 del_flag 列，不能使用 MP 逻辑删除）
     */
    @Delete("DELETE FROM wea_user_favorite WHERE id = #{id}")
    int deletePhysicalById(@Param("id") Long id);
}
