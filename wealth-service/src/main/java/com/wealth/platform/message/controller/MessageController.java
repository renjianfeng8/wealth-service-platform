package com.wealth.platform.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.platform.message.dto.FinMessageDTO;
import com.wealth.platform.message.entity.WeaMessage;
import com.wealth.platform.message.service.FinMessageService;
import com.wealth.platform.message.vo.FinMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "消息管理", description = "wea_message 站内消息相关接口")
@RequestMapping("/message/wea-message")
@RequiredArgsConstructor
@Validated
public class MessageController {

    private final FinMessageService finMessageService;

    @Operation(summary = "根据ID查询站内消息推送信息")
    @GetMapping("/{id}")
    public Result<FinMessageVO> getById(@PathVariable Long id) {
        FinMessageVO vo = finMessageService.getMessageById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询站内消息推送列表")
    @GetMapping
    public Result<List<FinMessageVO>> list() {
        return Result.success(finMessageService.getMessageList());
    }

    @Operation(summary = "分页查询站内消息推送")
    @GetMapping("/page")
    public Result<IPage<FinMessageVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String msgTitle,
            @RequestParam(required = false) Integer msgType,
            @RequestParam(required = false) Integer readFlag) {
        Page<WeaMessage> page = new Page<>(pageNum, pageSize);
        return Result.success(finMessageService.pageMessages(page, userId, msgTitle, msgType, readFlag));
    }

    @Operation(summary = "创建站内消息推送")
    @PostMapping
    @AuditLog(module = "消息管理", operation = "创建消息")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody FinMessageDTO dto) {
        return Result.success(finMessageService.createMessage(dto));
    }

    @Operation(summary = "更新站内消息推送信息")
    @PutMapping("/{id}")
    @AuditLog(module = "消息管理", operation = "更新消息")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinMessageDTO dto) {
        boolean success = finMessageService.updateMessage(id, dto);
        if (!success) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(true);
    }

    @Operation(summary = "删除站内消息推送（逻辑删除）")
    @DeleteMapping("/{id}")
    @AuditLog(module = "消息管理", operation = "删除消息")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = finMessageService.deleteMessage(id);
        if (!success) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(true);
    }
}
