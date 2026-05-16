package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import java.util.List;

/**
 * 鍚庡彴鐢ㄦ埛鍜岃鑹插叧绯昏〃涓氬姟灞傛帴鍙ｃ€?
 */
public interface UmsAdminRoleRelationService extends IService<UmsAdminRoleRelation> {

    // 鏍规嵁绠＄悊鍛榠d鑾峰彇鎵€鏈夎鑹瞚d
    List<Long> getRoleIdByAdminId(Long adminId);
}

