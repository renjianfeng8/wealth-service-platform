package com.wealth.common.contract;

import com.wealth.common.dto.AdminIdentityDTO;

/**
 * 管理员身份读取契约，避免 user 域直接依赖 system 域实体。
 */
public interface AdminIdentityProvider {
    AdminIdentityDTO findByUsername(String username);
}
