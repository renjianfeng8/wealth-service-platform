package com.finance.platform.message.controller;

import com.finance.common.result.Result;
import com.finance.platform.message.entity.FinMessage;
import com.finance.platform.message.service.FinMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 站内消息推送控制器。
 */
@RestController
@Tag(name = "消息管理", description = "fin_message 站内消息相关接口")
@RequestMapping("/finMessage")
public class FinMessageController {

    private final FinMessageService finMessageService;

    /**
     * 站内消息推送控制器构造器。
     *
     * @param finMessageService 站内消息业务服务
     */
    public FinMessageController(FinMessageService finMessageService) {
        this.finMessageService = finMessageService;
    }

    /**
     * 根据 ID 查询站内消息推送信息。
     *
     * @param id 站内消息推送 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询站内消息推送信息")
    @GetMapping("/{id}")
    public Result<FinMessage> getById(@PathVariable Long id) {
        return Result.success(finMessageService.getById(id));
    }

    /**
     * 查询站内消息推送列表（不分页）。
     *
     * @return 站内消息推送列表
     */
    @Operation(summary = "查询站内消息推送列表")
    @GetMapping
    public Result<List<FinMessage>> list() {
        return Result.success(finMessageService.list());
    }

    /**
     * 创建站内消息推送。
     *
     * @param finMessage 站内消息推送入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建站内消息推送")
    @PostMapping
    public Result<Boolean> create(@RequestBody FinMessage finMessage) {
        boolean saved = finMessageService.save(finMessage);
        return Result.success(saved);
    }

    /**
     * 更新站内消息推送信息。
     *
     * @param id 站内消息推送 ID
     * @param finMessage 站内消息推送入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新站内消息推送信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FinMessage finMessage) {
        finMessage.setId(id);
        boolean updated = finMessageService.updateById(finMessage);
        return Result.success(updated);
    }

    /**
     * 删除站内消息推送（逻辑删除）。
     *
     * @param id 站内消息推送 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除站内消息推送（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = finMessageService.removeById(id);
        return Result.success(removed);
    }
}

