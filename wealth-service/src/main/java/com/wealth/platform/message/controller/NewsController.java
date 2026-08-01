package com.wealth.platform.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.platform.message.dto.NewsDTO;
import com.wealth.platform.message.entity.WeaNews;
import com.wealth.platform.message.service.NewsService;
import com.wealth.platform.message.vo.NewsVO;
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

import java.util.List;

@RestController
@Tag(name = "资讯管理", description = "wea_news 财经资讯相关接口")
@RequestMapping("/message/wea-news")
@RequiredArgsConstructor
@Validated
public class NewsController {

    private final NewsService newsService;

    @Operation(summary = "根据ID查询财经资讯公告")
    @GetMapping("/{id}")
    public Result<NewsVO> getById(@PathVariable Long id) {
        return Result.success(newsService.getNewsById(id));
    }

    @Operation(summary = "查询财经资讯公告列表")
    @GetMapping
    public Result<List<NewsVO>> list(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(newsService.getNewsList(pageNum, pageSize));
    }

    @Operation(summary = "分页查询财经资讯公告")
    @GetMapping("/page")
    public Result<IPage<NewsVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer newsType) {
        Page<WeaNews> page = new Page<>(pageNum, pageSize);
        return Result.success(newsService.pageNews(page, title, source, newsType));
    }

    @Operation(summary = "创建财经资讯公告")
    @PostMapping
    @AuditLog(module = "资讯管理", operation = "创建资讯")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody NewsDTO dto) {
        return Result.success(newsService.createNews(dto));
    }

    @Operation(summary = "更新财经资讯公告信息")
    @PutMapping("/{id}")
    @AuditLog(module = "资讯管理", operation = "更新资讯")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody NewsDTO dto) {
        return Result.success(newsService.updateNews(id, dto));
    }

    @Operation(summary = "删除财经资讯公告（逻辑删除）")
    @DeleteMapping("/{id}")
    @AuditLog(module = "资讯管理", operation = "删除资讯")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(newsService.deleteNews(id));
    }
}
