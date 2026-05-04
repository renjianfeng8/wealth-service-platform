package com.finance.platform.message.controller;

import com.finance.common.result.Result;
import com.finance.platform.message.dto.FinNewsDTO;
import com.finance.platform.message.service.FinNewsService;
import com.finance.platform.message.vo.FinNewsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FinNewsController {

    private final FinNewsService finNewsService;

    /**
     * 根据 ID 查询财经资讯公告信息。
     *
     * @param id 财经资讯公告 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询财经资讯公告")
    @GetMapping("/{id}")
    public Result<FinNewsVO> getById(@PathVariable Long id) {
        return Result.success(finNewsService.getNewsById(id));
    }

    /**
     * 查询财经资讯公告列表（不分页）。
     *
     * @return 财经资讯公告列表
     */
    @Operation(summary = "查询财经资讯公告列表")
    @GetMapping
    public Result<List<FinNewsVO>> list() {
        return Result.success(finNewsService.getNewsList());
    }

    /**
     * 创建财经资讯公告。
     *
     * @param dto 财经资讯公告入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建财经资讯公告")
    @PostMapping
    public Result<Boolean> create(@RequestBody FinNewsDTO dto) {
        return Result.success(finNewsService.createNews(dto));
    }

    /**
     * 更新财经资讯公告信息。
     *
     * @param id 财经资讯公告 ID
     * @param dto 财经资讯公告入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新财经资讯公告信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FinNewsDTO dto) {
        return Result.success(finNewsService.updateNews(id, dto));
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
        return Result.success(finNewsService.deleteNews(id));
    }
}