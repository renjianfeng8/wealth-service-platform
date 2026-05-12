package com.finance.platform.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.platform.message.dto.FinMessageDTO;
import com.finance.platform.message.entity.FinMessage;
import com.finance.platform.message.vo.FinMessageVO;
import java.util.List;

/**
 * 站内消息推送业务层接口。
 */
public interface FinMessageService extends IService<FinMessage> {

    FinMessageVO getMessageById(Long id);

    List<FinMessageVO> getMessageList();

    IPage<FinMessageVO> pageMessages(Page<FinMessage> page, Long userId);

    boolean createMessage(FinMessageDTO dto);

    boolean updateMessage(Long id, FinMessageDTO dto);

    boolean deleteMessage(Long id);
}