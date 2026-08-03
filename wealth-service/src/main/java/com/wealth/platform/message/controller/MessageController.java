package com.wealth.platform.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.platform.message.dto.BatchReadDTO;
import com.wealth.platform.message.dto.MessageDTO;
import com.wealth.platform.message.entity.WeaMessage;
import com.wealth.platform.message.service.MessageService;
import com.wealth.platform.message.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "消息管理", description = "wea_message 站内消息相关接口")
@RequestMapping("/message/wea-message")
@RequiredArgsConstructor
@Validated
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "根据ID查询站内消息推送信息")
    @GetMapping("/{id}")
    public Result<MessageVO> getById(@PathVariable Long id) {
        return Result.success(messageService.getMessageById(id));
    }

    @Operation(summary = "分页查询站内消息推送")
    @GetMapping("/page")
    public Result<IPage<MessageVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String msgTitle,
            @RequestParam(required = false) Integer msgType,
            @RequestParam(required = false) Integer readFlag) {
        Page<WeaMessage> page = new Page<>(pageNum, pageSize);
        return Result.success(messageService.pageMessages(page, userId, msgTitle, msgType, readFlag));
    }

    @Operation(summary = "创建站内消息推送")
    @PostMapping
    @AuditLog(module = "消息管理", operation = "创建消息")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody MessageDTO dto) {
        return Result.success(messageService.createMessage(dto));
    }

    @Operation(summary = "更新站内消息推送信息")
    @PutMapping("/{id}")
    @AuditLog(module = "消息管理", operation = "更新消息")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody MessageDTO dto) {
        return Result.success(messageService.updateMessage(id, dto));
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@PathVariable Long id) {
        return Result.success(messageService.markAsRead(id));
    }

    @Operation(summary = "批量标记消息为已读")
    @PutMapping("/batch-read")
    public Result<Boolean> batchMarkAsRead(@RequestBody BatchReadDTO dto) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR);
        }
        messageService.batchMarkAsRead(dto.getIds());
        return Result.success(true);
    }

    @Operation(summary = "删除站内消息推送（逻辑删除）")
    @DeleteMapping("/{id}")
    @AuditLog(module = "消息管理", operation = "删除消息")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(messageService.deleteMessage(id));
    }
}
