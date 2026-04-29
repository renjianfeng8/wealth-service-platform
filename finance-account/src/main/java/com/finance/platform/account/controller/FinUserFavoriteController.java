package com.finance.platform.account.controller;

import com.finance.common.result.Result;
import com.finance.platform.account.entity.FinUserFavorite;
import com.finance.platform.account.service.FinUserFavoriteService;
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
 * 用户自选关注表控制器。
 */
@RestController
@Tag(name = "用户自选管理", description = "fin_user_favorite 用户自选相关接口")
@RequestMapping("/finUserFavorite")
public class FinUserFavoriteController {

    private final FinUserFavoriteService finUserFavoriteService;

    /**
     * 用户自选关注表控制器构造器。
     *
     * @param finUserFavoriteService 用户自选业务服务
     */
    public FinUserFavoriteController(FinUserFavoriteService finUserFavoriteService) {
        this.finUserFavoriteService = finUserFavoriteService;
    }

    /**
     * 根据 ID 查询用户自选关注信息。
     *
     * @param id 用户自选关注 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询用户自选关注信息")
    @GetMapping("/{id}")
    public Result<FinUserFavorite> getById(@PathVariable Long id) {
        return Result.success(finUserFavoriteService.getById(id));
    }

    /**
     * 查询用户自选关注列表（不分页）。
     *
     * @return 用户自选关注列表
     */
    @Operation(summary = "查询用户自选关注列表")
    @GetMapping
    public Result<List<FinUserFavorite>> list() {
        return Result.success(finUserFavoriteService.list());
    }

    /**
     * 创建用户自选关注。
     *
     * @param finUserFavorite 用户自选关注入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建用户自选关注")
    @PostMapping
    public Result<Boolean> create(@RequestBody FinUserFavorite finUserFavorite) {
        boolean saved = finUserFavoriteService.save(finUserFavorite);
        return Result.success(saved);
    }

    /**
     * 更新用户自选关注信息。
     *
     * @param id 用户自选关注 ID
     * @param finUserFavorite 用户自选关注入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新用户自选关注信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FinUserFavorite finUserFavorite) {
        finUserFavorite.setId(id);
        boolean updated = finUserFavoriteService.updateById(finUserFavorite);
        return Result.success(updated);
    }

    /**
     * 删除用户自选关注（物理删除）。
     *
     * @param id 用户自选关注 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除用户自选关注（物理删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = finUserFavoriteService.removeById(id);
        return Result.success(removed);
    }
}

