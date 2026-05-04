package com.finance.platform.account.controller;

import com.finance.common.result.Result;
import com.finance.platform.account.dto.FinUserFavoriteDTO;
import com.finance.platform.account.service.FinUserFavoriteService;
import com.finance.platform.account.vo.FinUserFavoriteVO;
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
 * 用户自选关注表控制器。
 */
@RestController
@Tag(name = "用户自选管理", description = "fin_user_favorite 用户自选相关接口")
@RequestMapping("/finUserFavorite")
@RequiredArgsConstructor
public class FinUserFavoriteController {

    private final FinUserFavoriteService finUserFavoriteService;

    /**
     * 根据 ID 查询用户自选关注信息。
     *
     * @param id 用户自选关注 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询用户自选关注信息")
    @GetMapping("/{id}")
    public Result<FinUserFavoriteVO> getById(@PathVariable Long id) {
        return Result.success(finUserFavoriteService.getFavoriteById(id));
    }

    /**
     * 查询用户自选关注列表（不分页）。
     *
     * @return 用户自选关注列表
     */
    @Operation(summary = "查询用户自选关注列表")
    @GetMapping
    public Result<List<FinUserFavoriteVO>> list() {
        return Result.success(finUserFavoriteService.getFavoriteList());
    }

    /**
     * 创建用户自选关注。
     *
     * @param dto 用户自选关注入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建用户自选关注")
    @PostMapping
    public Result<Boolean> create(@RequestBody FinUserFavoriteDTO dto) {
        return Result.success(finUserFavoriteService.createFavorite(dto));
    }

    /**
     * 更新用户自选关注信息。
     *
     * @param id 用户自选关注 ID
     * @param dto 用户自选关注入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新用户自选关注信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FinUserFavoriteDTO dto) {
        return Result.success(finUserFavoriteService.updateFavorite(id, dto));
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
        return Result.success(finUserFavoriteService.deleteFavorite(id));
    }
}