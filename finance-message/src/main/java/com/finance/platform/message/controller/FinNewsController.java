package com.finance.platform.message.controller;

import com.finance.common.result.Result;
import com.finance.platform.message.entity.FinNews;
import com.finance.platform.message.service.FinNewsService;
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
 * 财经资讯公告控制器。
 */
@RestController
@Tag(name = "资讯管理", description = "fin_news 财经资讯相关接口")
@RequestMapping("/finNews")
public class FinNewsController {

    private final FinNewsService finNewsService;

    /**
     * 财经资讯公告控制器构造器。
     *
     * @param finNewsService 财经资讯公告业务服务
     */
    public FinNewsController(FinNewsService finNewsService) {
        this.finNewsService = finNewsService;
    }

    /**
     * 根据 ID 查询财经资讯公告信息。
     *
     * @param id 财经资讯公告 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询财经资讯公告")
    @GetMapping("/{id}")
    public Result<FinNews> getById(@PathVariable Long id) {
        return Result.success(finNewsService.getById(id));
    }

    /**
     * 查询财经资讯公告列表（不分页）。
     *
     * @return 财经资讯公告列表
     */
    @Operation(summary = "查询财经资讯公告列表")
    @GetMapping
    public Result<List<FinNews>> list() {
        return Result.success(finNewsService.list());
    }

    /**
     * 创建财经资讯公告。
     *
     * @param finNews 财经资讯公告入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建财经资讯公告")
    @PostMapping
    public Result<Boolean> create(@RequestBody FinNews finNews) {
        boolean saved = finNewsService.save(finNews);
        return Result.success(saved);
    }

    /**
     * 更新财经资讯公告信息。
     *
     * @param id 财经资讯公告 ID
     * @param finNews 财经资讯公告入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新财经资讯公告信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FinNews finNews) {
        finNews.setId(id);
        boolean updated = finNewsService.updateById(finNews);
        return Result.success(updated);
    }

    /**
     * 删除财经资讯公告（逻辑删除）。
     *
     * @param id 财经资讯公告 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除财经资讯公告（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = finNewsService.removeById(id);
        return Result.success(removed);
    }
}

