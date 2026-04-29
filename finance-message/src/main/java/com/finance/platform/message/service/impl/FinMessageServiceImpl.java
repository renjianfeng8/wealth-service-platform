package com.finance.platform.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.message.entity.FinMessage;
import com.finance.platform.message.mapper.FinMessageMapper;
import com.finance.platform.message.service.FinMessageService;
import org.springframework.stereotype.Service;

/**
 * 站内消息推送表业务层实现类。
 */
@Service
public class FinMessageServiceImpl extends ServiceImpl<FinMessageMapper, FinMessage>
    implements FinMessageService {

    /**
     * 默认构造器。
     */
    public FinMessageServiceImpl() {
    }
}

