package com.wealth.platform.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.message.dto.MessageDTO;
import com.wealth.platform.message.entity.WeaMessage;
import com.wealth.platform.message.vo.MessageVO;
import java.util.List;

/**
 * 站内消息推送业务层接口
 */
public interface MessageService extends IService<WeaMessage> {

    MessageVO getMessageById(Long id);

    List<MessageVO> getMessageList(Integer pageNum, Integer pageSize);

    IPage<MessageVO> pageMessages(Page<WeaMessage> page, Long userId, String msgTitle, Integer msgType, Integer readFlag);

    boolean createMessage(MessageDTO dto);

    boolean updateMessage(Long id, MessageDTO dto);

    boolean markAsRead(Long id);

    void batchMarkAsRead(List<Long> ids);

    boolean deleteMessage(Long id);
}