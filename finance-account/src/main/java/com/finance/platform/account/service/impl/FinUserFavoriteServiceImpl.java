package com.finance.platform.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.account.entity.FinUserFavorite;
import com.finance.platform.account.mapper.FinUserFavoriteMapper;
import com.finance.platform.account.service.FinUserFavoriteService;
import org.springframework.stereotype.Service;

/**
 * 用户自选关注表业务层实现类。
 */
@Service
public class FinUserFavoriteServiceImpl extends ServiceImpl<FinUserFavoriteMapper, FinUserFavorite>
    implements FinUserFavoriteService {

    /**
     * 默认构造器。
     */
    public FinUserFavoriteServiceImpl() {
    }
}

