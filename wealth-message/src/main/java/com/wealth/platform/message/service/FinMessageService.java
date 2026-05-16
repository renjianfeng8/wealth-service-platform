package com.wealth.platform.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.message.dto.FinMessageDTO;
import com.wealth.platform.message.entity.WeaMessage;
import com.wealth.platform.message.vo.FinMessageVO;
import java.util.List;

/**
 * 绔欏唴娑堟伅鎺ㄩ€佷笟鍔″眰鎺ュ彛銆?
 */
public interface FinMessageService extends IService<WeaMessage> {

    FinMessageVO getMessageById(Long id);

    List<FinMessageVO> getMessageList();

    IPage<FinMessageVO> pageMessages(Page<WeaMessage> page, Long userId);

    boolean createMessage(FinMessageDTO dto);

    boolean updateMessage(Long id, FinMessageDTO dto);

    boolean deleteMessage(Long id);
}