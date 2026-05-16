package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import java.util.List;

/**
 * 鍚庡彴瑙掕壊璧勬簮鍏崇郴琛ㄤ笟鍔″眰鎺ュ彛銆?
 */
public interface UmsRoleResourceRelationService extends IService<UmsRoleResourceRelation> {

    // 鏍规嵁瑙掕壊id鍒楄〃锛岃幏鍙栨墍鏈夎祫婧恑d
    List<Long> getResourceIdByRoleIds(List<Long> roleIds);
}

