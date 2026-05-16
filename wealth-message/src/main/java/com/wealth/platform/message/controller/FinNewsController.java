package com.wealth.platform.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.message.dto.FinNewsDTO;
import com.wealth.platform.message.entity.WeaNews;
import com.wealth.platform.message.service.FinNewsService;
import com.wealth.platform.message.vo.FinNewsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "资讯管理", description = "wea_news 财经资讯相关接口")
@RequestMapping("/WeaNews")
@RequiredArgsConstructor
public class FinNewsController {

    private final FinNewsService finNewsService;

    @Operation(summary = "根据ID查询财经资讯公告")
    @GetMapping("/{id}")
    public Result<FinNewsVO> getById(@PathVariable Long id) {
        FinNewsVO vo = finNewsService.getNewsById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询财经资讯公告列表")
    @GetMapping
    public Result<List<FinNewsVO>> list() {
        return Result.success(finNewsService.getNewsList());
    }

    @Operation(summary = "分页查询财经资讯公告")
    @GetMapping("/page")
    public Result<IPage<FinNewsVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer newsType) {
        Page<WeaNews> page = new Page<>(pageNum, pageSize);
        return Result.success(finNewsService.pageNews(page, newsType));
    }

    @Operation(summary = "创建财经资讯公告")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody FinNewsDTO dto) {
        return Result.success(finNewsService.createNews(dto));
    }

    @Operation(summary = "更新财经资讯公告信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinNewsDTO dto) {
        return Result.success(finNewsService.updateNews(id, dto));
    }

    @Operation(summary = "删除财经资讯公告（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(finNewsService.deleteNews(id));
    }
}
